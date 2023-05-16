package com.academy.ecommerce.sterling.dsv.shipment;

import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * 
 * @author <a href="mailto:Kruthi.KM@cognizant.com">Kruthi KM</a>, 
 * Created on 09/14/2012
 * This class stamps the ShipmentType as WG or NWG based on the value of
 * ExtnWhiteGloveEligible for the item in the input document.
 * Also VendorNet will not send OrderHeaderKey and OrderLineKey and hence we need
 * to fetch and set it in the document, which will be used for confirmShipment API
 *
 */
 /*##################################################################################
 *
 * Project Name                : Release 3
 * Module                      : OMS
 * Author                      : CTS
 * Date                        : 30-JUL-2019
 * Description				   : changes done in this class does following 
 * 								  1.Updated trackingNo without spaces in it by using trim method.
 *                                2.It is updated as per JIRA FPT-193	
 * Change Revision
 * ---------------------------------------------------------------------------------
 * Date            Author         		Version#       Remarks/Description                      
 * ---------------------------------------------------------------------------------
 *30-JUL-2019	   CTS  	 			  2.0           	changed version
 * ##################################################################################*/
 


public class AcademyDSVPOShipmentConfirmation implements YIFCustomApi {
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyDSVPOShipmentConfirmation.class);
	public static final String CLASS_NAME = AcademyDSVPOShipmentConfirmation.class.getName();
	 
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	public Document stampShipmentType(YFSEnvironment env,
			Document shipmentConfDoc) throws Exception {
		log.debug(CLASS_NAME+" :stampShipmentType:Entry");
		log.debug("\nInput XML to AcademyDSVPOShipmentConfirmation API:\n"+XMLUtil.getXMLString(shipmentConfDoc));

		boolean flag = false;
		String strOrderNo = "";
		Document getOrderListInDoc = null;
		Document getOrderListOutDoc = null;
		String strOrdHdrKey = "";
		String isWhiteglove= "N";
				
		flag = ValidateXML(env, shipmentConfDoc);

		if (flag) {
			log.verbose("input document is"
					+ XMLUtil.getXMLString(shipmentConfDoc));
			Element eleInDoc = shipmentConfDoc.getDocumentElement();
			//Stamping SCAC and CarrierServiceCode at Extn level
			String sScac=eleInDoc.getAttribute(AcademyConstants.ATTR_SCAC);
			String sCarrServiceCode=eleInDoc.getAttribute(AcademyConstants.CARRIER_SERVICE_CODE);
			String ExtnVendorShipmentNo = eleInDoc.getAttribute(AcademyConstants.STR_VENDOR_SHIPMENT_NO);
			Element eExtn=shipmentConfDoc.createElement(AcademyConstants.ELE_EXTN);
			eExtn.setAttribute(AcademyConstants.STR_ORIGINALSHIPMENT_SCAC, sScac);
			/*Start Jira#DSA-9 Extn_Vendor_Shipment_No*/
			eExtn.setAttribute(AcademyConstants.STR_EXTN_VENDOR_SHIPMENT_NO, ExtnVendorShipmentNo);
			/* End Jira#DSA-9 Extn_Vendor_Shipment_No*/
			eExtn.setAttribute(AcademyConstants.STR_ORIGINALSHIPMENT_LOS, sCarrServiceCode);
			eleInDoc.appendChild(eExtn);
			//get OrderNo and call getOrderList API to fetch the OrderHeaderKey and set it in the input document
			strOrderNo = eleInDoc.getAttribute(AcademyConstants.ATTR_ORDER_NO);
			//prepare input to getOrderList API
			getOrderListInDoc = XMLUtil
					.createDocument(AcademyConstants.ELE_ORDER);
			Element eleOrder = getOrderListInDoc.getDocumentElement();
			eleOrder.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE,
					AcademyConstants.ENTERPRISE_CODE_SHIPMENT);
			eleOrder.setAttribute(AcademyConstants.ATTR_DOC_TYPE,
					AcademyConstants.DOCUMENT_TYPE_SHIPMENT);
			eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
			log.verbose("input to getOrderList API"
					+ XMLUtil.getXMLString(getOrderListInDoc));
            
			Document outputTemplate = XMLUtil
					.getDocument("<OrderList><Order OrderHeaderKey =''><OrderLines><OrderLine OrderLineKey='' PrimeLineNo='' SubLineNo=''></OrderLine></OrderLines></Order></OrderList>");
			env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST,
					outputTemplate);
			getOrderListOutDoc = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_ORDER_LIST, getOrderListInDoc);
			env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);

			log.verbose("Output from getOrderList API"
					+ XMLUtil.getXMLString(getOrderListOutDoc));
				
			//fetch OrderHeaderKey from output of getOrderList API
			Element eleOrderList = getOrderListOutDoc.getDocumentElement();
			Element eleOrd = (Element) eleOrderList.getElementsByTagName(
					AcademyConstants.ELE_ORDER).item(0);
			strOrdHdrKey = eleOrd
					.getAttribute(AcademyConstants.STR_ORDR_HDR_KEY);

			//set OrderHeaderKey at root level of the input Document i.e at <Shipment> level and also in <ShipmentLine> level
			eleInDoc.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY,
					strOrdHdrKey);	
			eleInDoc.setAttribute(AcademyConstants.ATTR_DOC_TYPE,
					"0005");
			NodeList OrderLinesList=eleOrd.getElementsByTagName(AcademyConstants.ELE_ORDER_LINES);
			Element eleOrderLines=(Element)OrderLinesList.item(0);
			NodeList OrderLineList=eleOrderLines.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
			NodeList shipmentLinesList = eleInDoc
					.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINES);
			Element eleShipmentLines = (Element) shipmentLinesList.item(0);
			NodeList ShipmentLineNL = eleShipmentLines
					.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);

			for (int iCounter = 0; iCounter < ShipmentLineNL.getLength(); iCounter++) {
				Element eleNode = (Element) ShipmentLineNL.item(iCounter);
				eleNode.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY,
						strOrdHdrKey);
				//Fetching PrimeLineNo from Shipment xml
				String sLineNo=eleNode.getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO);
								//Looping through each OrderLine tag
				for(int jcounter=0;jcounter<OrderLineList.getLength();jcounter++){
					Element eleOrderLine=(Element)OrderLineList.item(jcounter);
					//Matching the line No
					if(sLineNo.equals(eleOrderLine.getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO)))
					{
						//Stamping OrderLineKey to the ShipmentLine.
						String sOrderLineKey=eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
						eleNode.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, sOrderLineKey);
					}
					
				}
								
				String strItemID = eleNode.getAttribute(AcademyConstants.ITEM_ID);

				/**
				 * invoke getItemList API to check whether the item is WG or NWG
				 *            
				 */

				Document getItemListInDoc = XMLUtil
						.createDocument(AcademyConstants.ITEM);
				Element getItemListInEle = getItemListInDoc
						.getDocumentElement();
				getItemListInEle.setAttribute(AcademyConstants.ITEM_ID,
						strItemID);
				Document outputTemplateForItemList = XMLUtil
						.getDocument("<ItemList><Item ItemID='' UnitOfMeasure=''><Extn ExtnWhiteGloveEligible=''/></Item></ItemList>");
				env.setApiTemplate(AcademyConstants.API_GET_ITEM_LIST,
						outputTemplateForItemList);
				log.verbose("getItemListInDoc input XML: --> "
						+ XMLUtil.getXMLString(getItemListInDoc));
				
				Document getItemListOutDoc = AcademyUtil.invokeAPI(env,
						AcademyConstants.API_GET_ITEM_LIST, getItemListInDoc);
				env.clearApiTemplate(AcademyConstants.API_GET_ITEM_LIST);

				log.verbose("getItemListOutDoc out XML: --> "
						+ XMLUtil.getXMLString(getItemListOutDoc));

				Element eleGetItemListOutDoc = getItemListOutDoc
						.getDocumentElement();
				Element item = (Element) eleGetItemListOutDoc
						.getElementsByTagName(AcademyConstants.ITEM).item(0);
				
				//stamp UOM at <ShipmentLine> level
				String sUOM= item.getAttribute(AcademyConstants.ATTR_UOM);                              
                eleNode.setAttribute(AcademyConstants.ATTR_UOM, sUOM);  
                
                //stamp UOM at <ContainerDetail> level
                Element eleContainers = XMLUtil.getElementByXPath(shipmentConfDoc, "/Shipment/Containers");
                NodeList containerNL = eleContainers.getElementsByTagName(AcademyConstants.ELE_CONTAINER);
                //loop through the <Container> tag
                   for(int cCounter=0;cCounter<containerNL.getLength();cCounter++)
                   {
                	   Element eleContainer = (Element)containerNL.item(cCounter);
                	   Element eleContainerDtls = (Element) eleContainer.getElementsByTagName(AcademyConstants.ELE_CONTAINER_DTLS).item(0);
                	   NodeList containerDtlNL = eleContainerDtls.getElementsByTagName(AcademyConstants.CONTAINER_DETAIL);
                	     //inner loop to loop through <ContainerDetail> tag
                	        for(int cdCounter=0;cdCounter<containerDtlNL.getLength();cdCounter++)
                	        {
                	        	Element eleContainerDtl = (Element)containerDtlNL.item(cdCounter);
                	        	NodeList shipLineNL = eleContainerDtl.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
                	        	  //loop through <ShipmentLine> tag
                	        	    for(int sCounter=0;sCounter<shipLineNL.getLength();sCounter++)
                	        	    {
                	        	    	Element eleShipmntLine = (Element)shipLineNL.item(sCounter);
                	        	    	String strItemId = eleShipmntLine.getAttribute(AcademyConstants.ITEM_ID);
                	        	    	    if(strItemId.equals(strItemID))
                	        	    	    {
                	        	    	    	eleShipmntLine.setAttribute(AcademyConstants.ATTR_UOM, sUOM);
                	        	    	    }
                	        	    }
                	        }
                	   
                   }
				Element eleExtnList = (Element) item.getElementsByTagName(
						AcademyConstants.ELE_EXTN).item(0);
				

				if (AcademyConstants.ATTR_Y
						.equalsIgnoreCase(eleExtnList
								.getAttribute(AcademyConstants.ATTR_EXTN_WHITE_GLOVE_ELIGIBLE))) {
					
					isWhiteglove="Y";					

				} 
				
			}
			if (isWhiteglove.equals("Y")){
				//SET SHIPMENT_TYPE as WG
				// if shipment contains mix items, then SHIPMENT_TYPE should be WG.
				eleInDoc.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE,
						AcademyConstants.WG);
				}
			else
			{
				//SET SHIPMENT_TYPE as NWG
				// if shipment contains mix items, then SHIPMENT_TYPE should be NWG.
				eleInDoc.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE,
						AcademyConstants.NWG);
		}

		}
				
		log.debug("output XML from the service AcademyDSVPOShipmentConfirmation after setting ShipmentType and OrderHeaderKey and UOM"
						+ XMLUtil.getXMLString(shipmentConfDoc));
		log.debug(CLASS_NAME+" :stampShipmentType:Exit");
		return shipmentConfDoc;
	}

	private boolean ValidateXML(YFSEnvironment env, Document shipmentConfDoc) {
		log.debug(CLASS_NAME+" :ValidateXML:Entry");
		log.debug("Input XML for Validation"
				+ XMLUtil.getXMLString(shipmentConfDoc));

		String ErrorMsg = "";
		String orderLineKey = "";
		String shipOrderLineKey = "";
		String CarrierServiceCode = shipmentConfDoc.getDocumentElement()
				.getAttribute(AcademyConstants.CARRIER_SERVICE_CODE);
		String strSCAC = shipmentConfDoc.getDocumentElement().getAttribute(
				AcademyConstants.ATTR_SCAC);
				String OrderNo = shipmentConfDoc.getDocumentElement().getAttribute(
				AcademyConstants.ATTR_ORDER_NO);
		String Country = ((Element) shipmentConfDoc.getElementsByTagName(
				AcademyConstants.SHIP_NODE_ADD).item(0))
				.getAttribute(AcademyConstants.COUNTRY);
		String ZipCode = ((Element) shipmentConfDoc.getElementsByTagName(
				AcademyConstants.SHIP_NODE_ADD).item(0))
				.getAttribute(AcademyConstants.ZIP_CODE);
		
		if (isEmptyOrNull(CarrierServiceCode)) {
			ErrorMsg = ErrorMsg + "CarrierServiceCode is missing";
		}

		if (isEmptyOrNull(strSCAC)) {
			ErrorMsg = ErrorMsg + "SCAC is missing \n";
		}
		if (isEmptyOrNull(OrderNo)) {
			ErrorMsg = ErrorMsg + "OrderNo is missing \n";
		}
		if (isEmptyOrNull(Country)) {
			ErrorMsg = ErrorMsg + "Country is missing \n";
		}
		if (isEmptyOrNull(ZipCode)) {
			ErrorMsg = ErrorMsg + "ZipCode is missing \n";
		}

		NodeList containerList = shipmentConfDoc
				.getElementsByTagName(AcademyConstants.ELE_CONTAINER);
		for (int i = 0; i < containerList.getLength(); i++) {

			Element containerEle = (Element) containerList.item(i);			
			//FPT-193-Vendornet trackingNo space issue -JIRA start			
			String trackingNo = containerEle
			.getAttribute(AcademyConstants.ATTR_TRACKING_NO).trim();			
			containerEle.setAttribute(AcademyConstants.ATTR_TRACKING_NO, trackingNo);
			log.verbose("The valueof trackingNo After trimming" + trackingNo);
			//FPT-193-Vendornet trackingNo space issue-JIRA end			
			Element containerDetailEle = (Element) containerEle
					.getElementsByTagName(AcademyConstants.CONTAINER_DETAIL)
					.item(0);
			String quantity = containerDetailEle
					.getAttribute(AcademyConstants.ATTR_QUANTITY);
			Element shipmentLineEle = (Element) containerDetailEle
					.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE)
					.item(0);
			String documentType = shipmentLineEle
					.getAttribute(AcademyConstants.ATTR_DOC_TYPE);
			String itemID = shipmentLineEle
					.getAttribute(AcademyConstants.ITEM_ID);
			String primeLineNo = shipmentLineEle
					.getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO);
			String subLineNo = shipmentLineEle
					.getAttribute(AcademyConstants.SUB_LINE_NO);
			orderLineKey = shipmentLineEle
					.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
			String orderNo = shipmentLineEle
					.getAttribute(AcademyConstants.ATTR_ORDER_NO);
			String orderReleaseKey = shipmentLineEle
					.getAttribute(AcademyConstants.ORDER_RELEASE_KEY);
			if (isEmptyOrNull(orderLineKey)) {
				boolean primeLineNoMissing = false;
				boolean subLineNoMissing = false;
				if (isEmptyOrNull(primeLineNo)) {
					primeLineNoMissing = true;
				}
				if (isEmptyOrNull(subLineNo)) {
					subLineNoMissing = true;
				}

				if (primeLineNoMissing && !subLineNoMissing) {
					ErrorMsg = ErrorMsg + "PrimeLineNo is missing  \n";
				} else if (!primeLineNoMissing && subLineNoMissing) {
					ErrorMsg = ErrorMsg + "SubLineNo is missing  \n";
				} else if (primeLineNoMissing && subLineNoMissing) {
					ErrorMsg = ErrorMsg
							+ "PrimeLineNo,SubLineNo and OrderLineKey are missing  \n";
				}

			}

			if (isEmptyOrNull(trackingNo)) {
				ErrorMsg = ErrorMsg + "TrackingNo is missing";
			}

			if (isEmptyOrNull(quantity)) {
				ErrorMsg = ErrorMsg + "Quantity is missing \n";
			}
			if (isEmptyOrNull(documentType)) {
				ErrorMsg = ErrorMsg + "DocumentType is missing \n";
			}
			if (isEmptyOrNull(itemID)) {
				ErrorMsg = ErrorMsg + "ItemID is missing \n";
			}

			if (isEmptyOrNull(orderReleaseKey)) {
				ErrorMsg = ErrorMsg + "OrderReleaseKey is missing \n";
			}

			if (isEmptyOrNull(orderNo)) {
				ErrorMsg = ErrorMsg + "OrderNo is missing \n";
			}
			
		}
		Element shipmentLinesEle = (Element) shipmentConfDoc
				.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINES)
				.item(0);
		NodeList shipmentLineList = shipmentLinesEle
				.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		for (int j = 0; j < shipmentLineList.getLength(); j++) {
			Element shipmentEle = (Element) shipmentLineList.item(j);
			String shipitemID = shipmentEle
					.getAttribute(AcademyConstants.ITEM_ID);
			shipOrderLineKey = shipmentEle
					.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
			String shipOrderNo = shipmentEle
					.getAttribute(AcademyConstants.ATTR_ORDER_NO);
			String shipPrimeNo = shipmentEle
					.getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO);
			String shipSubNo = shipmentEle
					.getAttribute(AcademyConstants.SUB_LINE_NO);
			String shipOrderReleaseKey = shipmentEle
					.getAttribute(AcademyConstants.ORDER_RELEASE_KEY);
			String shipQuantity = shipmentEle
					.getAttribute(AcademyConstants.ATTR_QUANTITY);
			if (isEmptyOrNull(shipOrderLineKey)) {
				boolean shipPrimeLineNoMissing = false;
				boolean shipSubLineNoMissing = false;
				if (isEmptyOrNull(shipPrimeNo)) {
					shipPrimeLineNoMissing = true;
				}
				if (isEmptyOrNull(shipSubNo)) {
					shipSubLineNoMissing = true;
				}

				if (shipPrimeLineNoMissing && !shipSubLineNoMissing) {
					ErrorMsg = ErrorMsg
							+ "PrimeLineNo is missing at ShipmentLine Level \n";
				} else if (!shipPrimeLineNoMissing && shipSubLineNoMissing) {
					ErrorMsg = ErrorMsg
							+ "SubLineNo is missing at ShipmentLine Level \n";
				} else if (shipPrimeLineNoMissing && shipSubLineNoMissing) {
					ErrorMsg = ErrorMsg
							+ "PrimeLineNo,SubLineNo are missing at ShipmentLine Level \n";
				}

			}
			if (isEmptyOrNull(shipitemID)) {
				ErrorMsg = ErrorMsg
						+ "ShipItemID is missing at ShipmentLine Level \n";
			}

			if (isEmptyOrNull(shipOrderNo)) {
				ErrorMsg = ErrorMsg
						+ "ShipOrderNo is missing at ShipmentLine Level \n";
			}

			if (isEmptyOrNull(shipOrderReleaseKey)) {
				ErrorMsg = ErrorMsg
						+ "ShipOrderReleaseKey is missing at ShipmentLine Level \n";
			}
			if (isEmptyOrNull(shipQuantity)) {
				ErrorMsg = ErrorMsg
						+ "ShipQuantity is missing at ShipmentLine Level \n";
			}
		}

		boolean flag = true;
		if (!isEmptyOrNull(ErrorMsg)) {
			flag = false;
			YFSException excep = new YFSException(ErrorMsg);
			excep.setErrorCode("Invalid Inforamtion");
			excep.setErrorDescription("Mandatory Feilds are missing");
			throw excep;
		}
		log.debug("Document is validated...."+XMLUtil.getXMLString(shipmentConfDoc));
		log.debug(CLASS_NAME+" :ValidateXML:Exit");
		return flag;

	}
	private boolean isEmptyOrNull(final String argS) {
		if (null == argS) {
			return true;
		}
		for (int ln = 0; ln < argS.length()
				&& !Character.isWhitespace(argS.charAt(ln)); ln++) {
			return false;
		}
		return true;
	}

}
