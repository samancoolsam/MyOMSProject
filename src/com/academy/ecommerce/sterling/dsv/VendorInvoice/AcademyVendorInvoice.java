package com.academy.ecommerce.sterling.dsv.VendorInvoice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.date.YTimestamp;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyVendorInvoice extends com.yantra.ycp.japi.util.YCPBaseAgent {

	private static YIFApi api = null;

	static {
		try {
			api = YIFClientFactory.getInstance().getApi();
		} catch (YIFClientCreationException e) {
			e.printStackTrace();
		}

	}

	private static Logger log = Logger.getLogger(AcademyVendorInvoice.class
			.getName());

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yantra.ycp.japi.util.YCPBaseAgent#getJobs(com.yantra.yfs.japi.YFSEnvironment,
	 *      org.w3c.dom.Document)
	 */
	/*
	 * Method to collect all vendorInvoices which have not been published to
	 * Business.
	 */
	public List getJobs(YFSEnvironment env, Document inXML, Document lastMessage)
			throws Exception {
		// System.out.println("input xml coming to getjobs"
		// + XMLUtil.getXMLString(inXML));
		log.verbose("Input inXML -->" + XMLUtil.getXMLString(inXML));
		List<Document> valueList = new ArrayList<Document>();

		Document getVendorInvoiceListInput = null;

		YTimestamp date = YTimestamp.newMutableTimestamp();
		String currentDate = date.getString();

		if (YFCObject.isVoid(lastMessage)) {

			getVendorInvoiceListInput = XMLUtil
					.getDocument("<ExtnVendorInvoice Ispublished=\"N\" AvailableDate=\""
							+ currentDate
							+ "\" AvailableDateQryType=\"LE\">"
							+ "<OrderBy>"
							+ "<Attribute Name=\"VendorInvoiceKey\" Desc=\"N\"/>"
							+ "</OrderBy>" + "</ExtnVendorInvoice>");

			getVendorInvoiceListInput.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_MAX_RECORD,
					inXML.getDocumentElement().getAttribute(
							AcademyConstants.ATTR_NUM_RECORDS));
		} else {
			String vendorInvoiceKeyInLastCreatedMessage = lastMessage
					.getDocumentElement().getAttribute("VendorInvoiceKey");
			getVendorInvoiceListInput = XMLUtil
					.getDocument("<ExtnVendorInvoice Ispublished=\"N\" IspublishedQryType=\"EQ\" VendorInvoiceKey=\""
							+ vendorInvoiceKeyInLastCreatedMessage
							+ "\" VendorInvoiceKeyQryType=\"GT\" AvailableDate=\""
							+ currentDate
							+ "\" AvailableDateQryType=\"LE\">"
							+ "<OrderBy>"
							+ "<Attribute Name=\"VendorInvoiceKey\" Desc=\"N\"/>"
							+ "</OrderBy>" + "</ExtnVendorInvoice>");
		}

		// Set the vendor invoice list template
		Document getVendorInvoiceListTemplate = XMLUtil
				.getDocument("<ExtnVendorInvoiceList>"
						+ "<ExtnVendorInvoice APVendor=\"\" AccountID=\"\" Company=\"\" Department=\"\" Division=\"\" DutyAmount=\"\" "
						+ "FreightCharges=\"\" HandlingFee=\"\" InvoiceDate=\"\" InvoiceNO=\"\" InvoiceTotal=\"\" Ispublished=\"\" OrderNO=\"\" "
						+ "PurchaseOrderNO=\"\" PurchaseOrderTerms=\"\" ShipToKey=\"\" TaxAmount=\"\" VendorInvoiceKey=\"\" VendorNO=\"\" AvailableDate=\"\" Counter=\"\" VendorShipmentNo=\"\">"
						+ "<ExtnVendorInvoiceDetailsList><ExtnVendorInvoiceDetails ItemID=\"\" UnitCost=\"\"/>"
						+ "</ExtnVendorInvoiceDetailsList>"
						+ "</ExtnVendorInvoice>" + "</ExtnVendorInvoiceList>");
		env.setApiTemplate("AcademyVendorInvoiceList",
				getVendorInvoiceListTemplate);

		log.verbose("  getVendorInvoiceListTemplate -->"
				+ XMLUtil.getXMLString(getVendorInvoiceListTemplate));
		// Invoking the Vendorinvoicelist service
		Document getVendorInvoiceListOutput = AcademyUtil.invokeService(env,
				"AcademyVendorInvoiceList", getVendorInvoiceListInput);
		// Clearing the template
		env.clearApiTemplate("AcademyVendorInvoiceList");
		NodeList vendorInvoiceList = getVendorInvoiceListOutput
				.getElementsByTagName("ExtnVendorInvoice");

		// populating ExtnVendorInvoice in the
		// ArrayList
		for (int i = 0; i < vendorInvoiceList.getLength(); i++) {
			Element eleVendorInvoice = (Element) vendorInvoiceList.item(i);
			Document vendorInvoiceDoc = XMLUtil
					.getDocumentForElement(eleVendorInvoice);
			valueList.add(vendorInvoiceDoc);
		}

		return valueList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yantra.ycp.japi.util.YCPBaseAgent#executeJob(com.yantra.yfs.japi.YFSEnvironment,
	 *      org.w3c.dom.Document)
	 * 
	 * 1)Get the complete order details of PO and check if the Order is
	 * completely Shipped and all the Shipments are in Invoiced status 2)get the
	 * IP PO number for all the shipments for PO 3)Form the vendor invoice xml
	 * 4)Publish the vendor invoice XML to MQ 5)Make the IsPublished flag ="Y"
	 * in ExtnVendorInvoice Table.
	 */

	public void executeJob(YFSEnvironment env, Document inDoc) throws Exception {

		// get the purchase order complete details and check if the order is in
		// shipped status and Shipments of the order in invoiced status
		Document publishVendorInvoiceDoc = null;
		String vendorShipmentNo = inDoc.getDocumentElement().getAttribute(
				"VendorShipmentNo");
		//SOF :: WN-2035 Start - Vendor Invoicing
		String strPOLineType = null;
		Double dShippedStatus = 0.0;
		Double dMaxOrderStatus = 0.0;
		String strShipmentKeyCmpDTL = null;
		boolean bPublishInvoiceToRMS = false;
		boolean bIsPOCancelled = false;
		//boolean flag = false;	
		//SOF :: WN-2035 End - Vendor Invoicing
		//WN-2362 Start - DSV Vendor Invoicing for Multiple shipments
		double dShipmentStatus = 0.0;
		double dShipmentInvoicedStatus = 0.0;
		String strShipmentStatus = "";
		//WN-2362 End - DSV Vendor Invoicing for Multiple shipments
		

		log.verbose("Shipment Key  strShipmentCmpDTL -->" + vendorShipmentNo);
		Document inputOrderDetailDoc = XMLUtil.createDocument("Order");

		Element eleInputOrderDetail = inputOrderDetailDoc.getDocumentElement();
		eleInputOrderDetail.setAttribute("OrderNo", inDoc.getDocumentElement()
				.getAttribute("PurchaseOrderNO"));
		eleInputOrderDetail.setAttribute("EnterpriseCode", "Academy_Direct");
		eleInputOrderDetail.setAttribute("DocumentType", "0005");
		// form the GetCompleteOrderDetailsTemplate for PO
		//SOF :: WN-2035 - Vendor Invoicing -> Making a template change to get OrderLines/OrderLine/@LineType
		Document getCompleteOrderDetailsTemplate = XMLUtil
				.getDocument("<Order MaxOrderStatus=\"\">"
						+"<OrderLines> <OrderLine LineType='' /> </OrderLines>"
						+ "<Shipments>"
						+ "<Shipment ShipmentKey=\"\" Status=\"\">"
						+ "<Extn ExtnVendorShipmentNo=\"\"/>"
						+ "<ShipmentLines>"
						+ "<ShipmentLine ItemID=\"\" Quantity=\"\" ItemDesc=\"\"/>"
						+ "</ShipmentLines>" + "</Shipment>" + "</Shipments>"
						+ "</Order>");

		log.verbose("getCompleteOrderDetailsTemplate----->"
				+ XMLUtil.getXMLString(getCompleteOrderDetailsTemplate));

		/*
		 * New Template for to get the Complete Order Detail along with
		 * VendorShipmentNo
		 * 
		 * 
		 * <Order MaxOrderStatus= " "> <Shipments> <Shipment ShipmentKey= " "
		 * Status= " "> <Extn ExtnVendorShipmentNo=""/> <ShipmentLines>
		 * <ShipmentLine ItemID= " " Quantity= " " ItemDesc= " "/>
		 * </ShipmentLines> </Shipment> </Shipments> </Order>
		 * 
		 * 
		 */
		env.setApiTemplate("getCompleteOrderDetails",
				getCompleteOrderDetailsTemplate);
		// invoke complete order details
		Document orderDetailsDoc = AcademyUtil.invokeAPI(env,
				"getCompleteOrderDetails", inputOrderDetailDoc);
		log.verbose("getPOCompleteOrderdetails Output----->"
				+ XMLUtil.getXMLString(orderDetailsDoc));

		/*
		 * Element eleVendorInvoice = (Element)
		 * inDoc.getDocumentElement().getElementsByTagName("ExtnVendorInvoice").item(0);
		 * String vendorShipmentNo =
		 * eleVendorInvoice.getAttribute("VendorShipmentNo");
		 */

		Node eleExtnVendorShipment = XMLUtil.getNode(orderDetailsDoc
				.getDocumentElement(),
				"/Order/Shipments/Shipment/Extn[@ExtnVendorShipmentNo='"
						+ vendorShipmentNo + "']");
		
		//Start : OMNI-78997 : VendorInvoice Agent Errors
		if (!YFCObject.isNull(eleExtnVendorShipment)) {

		Element eleShipment = (Element) eleExtnVendorShipment
				.getParentNode();

		strShipmentKeyCmpDTL = eleShipment.getAttribute("ShipmentKey");

		log.verbose("Shipment Key  strShipmentCmpDTL -->" + strShipmentKeyCmpDTL);

		Element eleOrderDetails = orderDetailsDoc.getDocumentElement();
		String strStatus = eleOrderDetails.getAttribute("MaxOrderStatus");		
		
		//SOF :: WN-2035 Start - Vendor Invoicing		
		//As part of existing DSV flow - if PO Status > 'Shipped' && All PO Shipments > 'Invoiced' , then only publish invoice details to RMS.
		//As part of SOF flow - Rather than checking if all PO Shipments are invoiced, we are checking if the PO Shipment is 'Received'.
		//SOF flow : Soon after we receive 'shipment confirmation' msg, we immediately do recieveOrder. 
		//Hence if PO MaxOrderStatus is Shipped it means all shipments are received
		
		//Commenting out old code - 
		//Double dStatus = Double.parseDouble(strStatus);
		//boolean flag = checkShipmentStatus(eleOrderDetails);
		//if (dStatus >= 3700 && flag) {
		
		dMaxOrderStatus = Double.parseDouble(strStatus);
		dShippedStatus = Double.parseDouble(AcademyConstants.STR_PO_SHIPPED_STATUS);
		strPOLineType = XPathUtil.getString(orderDetailsDoc, AcademyConstants.XPATH_ORDER_ORDERLINE_LINETYPE);
		
		//In SOF flow, soon after PO shipment is created we are doing auto receiving. So we are only checking for lineType='SOF', then bPublishInvoiceToRMS=true
		if(AcademyConstants.STR_SPECIAL_ORDER_FIREARMS.equals(strPOLineType)){
			log.verbose("SOF : Eligible for publish");
			bPublishInvoiceToRMS =  true;	
		}else{
			log.verbose("DSV : Evaluate status conditions");
			//WN-2362 Start - DSV Vendor Invoicing for Multiple shipments
			/*flag = checkShipmentStatus(eleOrderDetails);
			if(dMaxOrderStatus >= dShippedStatus && flag)
				bPublishInvoiceToRMS =  true;*/
			strShipmentStatus = eleShipment.getAttribute(AcademyConstants.ATTR_STATUS);
			//If we receive Shipment invoice message after Shipment Deliver status, double conversion will fail. Added below code to avoid this issue.
			if (strShipmentStatus.length() > 4) {
				strShipmentStatus = strShipmentStatus.substring(0, 7);				
			}
			dShipmentStatus = Double.parseDouble(strShipmentStatus);
			dShipmentInvoicedStatus = Double.parseDouble(AcademyConstants.INVOICED_SHIPMENT_STATUS);
			
			if(dShipmentStatus >= dShipmentInvoicedStatus)
				bPublishInvoiceToRMS =  true;
			//WN-2362 End - DSV Vendor Invoicing for Multiple shipments
			
		}
		}
		else {
			log.verbose("No Valid shipment is is present for the PO. Check PO Status ");
			String strMaxOrderStatus = XMLUtil.getAttributeFromXPath(orderDetailsDoc, "/Order/@MaxOrderStatus" );
			if(!YFCObject.isVoid(strMaxOrderStatus) 
					&& AcademyConstants.VAL_CANCELLED_STATUS.equals(strMaxOrderStatus)) {
				log.verbose("PO Is cancelled. Ignore this Vendor Invoice");
				bIsPOCancelled =true;
			}
		}
		//End : OMNI-78997 : VendorInvoice Agent Errors
		
		if (bPublishInvoiceToRMS) {
		//SOF :: WN-2035 End - Vendor Invoicing
			
			// get the IP PO Number for all the shipment
			Document inputPOLookUpDoc = XMLUtil
					.createDocument("ExtnIPPOLookup");
			Element elePOLookUp = inputPOLookUpDoc.getDocumentElement();

			elePOLookUp.setAttribute("ShipmentKey", strShipmentKeyCmpDTL);
			// invoke getIPPOLookup List to get PO Number
			Document ipPOlookUpDoc = AcademyUtil.invokeService(env,
					"AcademyIPPOLookupList", inputPOLookUpDoc);

			NodeList ipPODocList = (NodeList) ipPOlookUpDoc
					.getDocumentElement();

			// If IPPO not created by IP System , should not publish the Vendor
			// Invoice Details to MQ.
			if (ipPODocList.getLength() > 0) {
				publishVendorInvoiceDoc = VendorInvoiceDoc(env, ipPOlookUpDoc,
						inDoc);
			}
			// System.out.println("IPPO not created for the
			// Shipment"+strShipmentKey);

			if (!YFCObject.isVoid(publishVendorInvoiceDoc)) {
				AcademyUtil.invokeService(env, "AcademyVendorInvoiceMQ",
						publishVendorInvoiceDoc);
			//OMNI-485 : Start Changes to mark IsPublished Flag as Y only if the invoice message is posted to MQ
				vendorInvoicePublished(env, inDoc);
			}
			//vendorInvoicePublished(env, inDoc);
            //OMNI-485 : End Changes to mark IsPublished Flag as Y only if the invoice message is posted to MQ

		} else {
			// getting Currentdate and Adding 3 hrs
			YTimestamp date = YTimestamp.newMutableTimestamp();
			date.addHours(3);
			String modifiedDate = date.getString();
			//Start : OMNI-78997 : VendorInvoice Agent Errors
			if(bIsPOCancelled) {
				modifiedDate = AcademyConstants.FINAL_AVAILABLE_DATE;
			}
			
			//End : OMNI-78997 : VendorInvoice Agent Errors
			// getting Counter and adding value '1'
			String strCounter = inDoc.getDocumentElement().getAttribute(
					"Counter");
			int intCounter = Integer.parseInt(strCounter);
			int modifiedCounter = intCounter + 1;
			String strModifiedCounter = String.valueOf(modifiedCounter);
			// setting the new values to Document
			Document vendorInvoiceDoc = XMLUtil
					.createDocument("ExtnVendorInvoice");
			vendorInvoiceDoc.getDocumentElement()
					.setAttribute(
							"VendorInvoiceKey",
							inDoc.getDocumentElement().getAttribute(
									"VendorInvoiceKey"));
			vendorInvoiceDoc.getDocumentElement().setAttribute("AvailableDate",
					modifiedDate);
			vendorInvoiceDoc.getDocumentElement().setAttribute("Counter",
					strModifiedCounter);
			// updating the Extn_Vendor_Invoice table with updated values.
			AcademyUtil.invokeService(env, "AcademyModifyVendorInvoice",
					vendorInvoiceDoc);

		}

	}

	/**
	 * @param env
	 * @param inDoc
	 * @throws Exception
	 *             Method to mark the flag IsPublished as Y
	 */
	private void vendorInvoicePublished(YFSEnvironment env, Document inDoc)
			throws Exception {
		Document vendorInvoiceDoc = XMLUtil.createDocument("ExtnVendorInvoice");
		vendorInvoiceDoc.getDocumentElement().setAttribute("VendorInvoiceKey",
				inDoc.getDocumentElement().getAttribute("VendorInvoiceKey"));
		vendorInvoiceDoc.getDocumentElement().setAttribute("Ispublished", "Y");
		AcademyUtil.invokeService(env, "AcademyModifyVendorInvoice",
				vendorInvoiceDoc);

	}

	/**
	 * @param env
	 * @param ipPOlookUpDoc
	 * @param inDoc
	 * @return Method to form the vendor invpice XML which sterling will publish
	 *         to business
	 * @throws IOException
	 * @throws SAXException
	 * @throws
	 */
	private Document VendorInvoiceDoc(YFSEnvironment env,
			Document ipPOlookUpDoc, Document inDoc) throws Exception {

		Element eleVendorInvoiceDetails = null;
		Element elementVendorInvoice;
		String strExtendedCost;
		String strOrderQty;
		String strItemID;
		String strUnitCost;
		String strItemDesc = null;

		log.verbose("In FormOutPutDoc Key  inDoc -->"
				+ XMLUtil.getXMLString(inDoc));

		log.verbose("In FormOutPutDoc Key  ipPOlookUpDoc -->"
				+ XMLUtil.getXMLString(ipPOlookUpDoc));

		Document getShipNodeListInput = XMLUtil
				.getDocument("<ShipNode ShipnodeKey=\"\" />");

		String VendorNo = inDoc.getDocumentElement().getAttribute("VendorNO");
		getShipNodeListInput.getDocumentElement().setAttribute("ShipnodeKey",
				VendorNo);

		Document getShipNodeListTemplate = XMLUtil.getDocument("<ShipNodeList>"
				+ "<ShipNode ShipnodeKey=\"\" Description=\"\" />"
				+ "</ShipNodeList>");
		env.setApiTemplate("getShipNodeList", getShipNodeListTemplate);
		Document getShipNodeListOutput = AcademyUtil.invokeAPI(env,
				"getShipNodeList", getShipNodeListInput);

		// System.out.println("getShipNodeListOutput
		// is"+XMLUtil.getXMLString(getShipNodeListOutput));

		String vendorName = ((Element) getShipNodeListOutput
				.getElementsByTagName("ShipNode").item(0))
				.getAttribute("Description");
		// System.out.println("vendor name is "+ vendorName);

		inDoc.getDocumentElement().setAttribute("VendorName", vendorName);

		Element eleIPPO = inDoc.createElement("IpPurchaseOrder");
		String shipmentKey = ((Element) ipPOlookUpDoc.getElementsByTagName(
				"ExtnIPPOLookup").item(0)).getAttribute("ShipmentKey");
		String poNumber = ((Element) ipPOlookUpDoc.getElementsByTagName(
				"ExtnIPPOLookup").item(0)).getAttribute("PoNo");
		eleIPPO.setAttribute("ShipmentKey", shipmentKey);
		eleIPPO.setAttribute("PONumber", poNumber);
		inDoc.getDocumentElement().appendChild(eleIPPO);

		Element eleVendorInvoiceDetailsList = inDoc
				.createElement("ExtnVendorInvoiceDetailsList");
		eleIPPO.appendChild(eleVendorInvoiceDetailsList);

		NodeList nListExtnVendorInoviceDetails = XMLUtil
				.getNodeList(inDoc,
						"/ExtnVendorInvoice/ExtnVendorInvoiceDetailsList/ExtnVendorInvoiceDetails");
		int iNoEleOrLnInDoc = nListExtnVendorInoviceDetails.getLength();
		log.verbose("nListExtnVendorInoviceDetails -->" + iNoEleOrLnInDoc);
		for (int i = 0; i < iNoEleOrLnInDoc; i++) {

			eleVendorInvoiceDetails = inDoc
					.createElement("ExtnVendorInvoiceDetails");

			elementVendorInvoice = (Element) nListExtnVendorInoviceDetails
					.item(i);
			strExtendedCost = elementVendorInvoice.getAttribute("ExtendedCost");  
			strOrderQty = elementVendorInvoice.getAttribute("InvoiceQty");
			strItemID = elementVendorInvoice.getAttribute("ItemID");
			strUnitCost = elementVendorInvoice.getAttribute("UnitCost");
			eleVendorInvoiceDetails.setAttribute("Extendedprice",
					strExtendedCost);
			eleVendorInvoiceDetails.setAttribute("Quantity", strOrderQty);
			eleVendorInvoiceDetails.setAttribute("ItemId", strItemID);
			eleVendorInvoiceDetails.setAttribute("UnitCost", strUnitCost);
			eleVendorInvoiceDetails.setAttribute("ItemDesc", strItemDesc);
			eleVendorInvoiceDetailsList.appendChild(eleVendorInvoiceDetails);

		}

		Element eleInput = inDoc.getDocumentElement();
		NodeList detailslist = eleInput
				.getElementsByTagName("ExtnVendorInvoiceDetailsList");
		Element eleExtnVenInv = (Element) detailslist.item(0);
		eleInput.removeChild(eleExtnVenInv);
		inDoc.getDocumentElement().removeAttribute("VendorShipmentNo");
		// eleVendorInvoiceDetailsList.appendChild(eleVendorInvoiceDetails);
		log.verbose("VendorInvoiceDoc----->" + XMLUtil.getXMLString(inDoc));

		return inDoc;

	}

	/**
	 * @param env
	 * @param inDoc
	 * @param hmItemCost
	 *            Method to populate the hmItemCost hashmap with the unit cost
	 *            of the item from Extn vendor invoice details table. This
	 *            information is used in forming the vendor invoice XML
	 */
	/*
	 
	 //WN-2362 Start - DSV Vendor Invoicing for Multiple shipments - Commenting the below method
	  */
	 /**
	 * @param eleOrderDetails
	 *            Method to check the ShipmentStatus is ShipmentInvoiced or not.
	 */
	/*private boolean checkShipmentStatus(Element eleOrderDetails) {
		NodeList nlShipment = eleOrderDetails.getElementsByTagName("Shipment");
		boolean flag = true;
		for (int i = 0; i < nlShipment.getLength(); i++) {
			Element eleshipment = (Element) nlShipment.item(i);
			String strShipmentStatus = eleshipment.getAttribute("Status");

			Double dShipmentStatus = 0.0;

			if (strShipmentStatus.length() > 4) {
				String strModifiedStatus = strShipmentStatus.substring(0, 7);
				dShipmentStatus = Double.parseDouble(strModifiedStatus);
			}

			if (dShipmentStatus < 1600.02) {
				flag = false;
				break;
			}
		}
		return flag;
	}*/
	//WN-2362 End - DSV Vendor Invoicing for Multiple shipments

}
