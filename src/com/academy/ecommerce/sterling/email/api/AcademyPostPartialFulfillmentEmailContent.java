package com.academy.ecommerce.sterling.email.api;

import org.w3c.dom.DOMException;

/**#########################################################################################

 *

 * Project Name                : ESP

 * Module                      : OMNI-40848

 * Date                        : 17-AUG-2021 

 * Description				  : This class generates partial fulfillment Email message xml posted to ESB queue.

 *

 * #########################################################################################*/

import org.w3c.dom.Document;

import com.academy.ecommerce.sterling.email.util.AcademyEmailUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;

import com.academy.util.xml.XMLUtil;

import com.academy.util.xml.XPathUtil;

import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.ui.web.framework.utils.SCUIUtils;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;

import com.yantra.yfc.log.YFCLogCategory;

import com.yantra.yfc.util.YFCCommon;

import com.yantra.yfs.core.YFSSystem;

import com.yantra.yfs.japi.YFSEnvironment;

import org.w3c.dom.Element;

import org.w3c.dom.NodeList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

public class AcademyPostPartialFulfillmentEmailContent implements YIFCustomApi {

	private Properties props;

	public void setProperties(Properties props) throws Exception {

		this.props = props;

	}

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPostPartialFulfillmentEmailContent.class);

	/**
	 * 
	 * This method customizes input xml to post shipped email message to
	 * 
	 * ESB queue
	 * 
	 * @param inDoc
	 * 
	 * @return docSMSMessage
	 * 
	 * @throws Exception
	 * 
	 */

	public Document prepareEmailContent(YFSEnvironment env, Document inDoc) throws Exception {

		log.verbose("AcademyPostPartialFulfillmentEmailContent.prepareEmailContent()_InXML:" + XMLUtil.getXMLString(inDoc));

		updateEMailTemplateForShipment(env, inDoc);

		log.verbose("AcademyPostPartialFulfillmentEmailContent.prepareEmailContent()_returnDoc:" + XMLUtil.getXMLString(inDoc));

		return inDoc;

	}

	/**
	 * 
	 * This method updates shipped email template XML to post to ESB
	 * 
	 * queue.
	 * 
	 * 
	 * 
	 * @param inDoc
	 * 
	 * @throws Exception
	 * 
	 */

	private void updateEMailTemplateForShipment(YFSEnvironment env, Document inDoc) throws Exception {

		log.verbose("Inside updateEMailTemplateForShipment Method"+XMLUtil.getXMLString(inDoc));
		setOrderPlacedDate(inDoc);
		Element eleIndoc = inDoc.getDocumentElement();
		NodeList nlOrderLines = AcademyEmailUtil.getCompleteOrderLineLines(eleIndoc);
		String strInvoicedQty=null;
		String strProcessingQty=null;
		String strCancelledQty=null;
		Double dOriginalOrderQty=0.00;
 		Double dOrderedQty=0.00;
 		Double dInvoicedQty=0.00;
 		int iProcesingItemCount=0;
		for (int i = 0; i < nlOrderLines.getLength(); i++) {
			Element eleOrderline = (Element) nlOrderLines.item(i);
			createProductInfoElement(inDoc);	
			String sDeliveryMethod=eleOrderline.getAttribute(AcademyConstants.ATTR_DEL_METHOD);
			strInvoicedQty=eleOrderline.getAttribute("InvoicedQty");
			dOriginalOrderQty=Double.parseDouble(eleOrderline.getAttribute(AcademyConstants.ATTR_ORIGL_ORDERED_Qty));
			dOrderedQty=Double.parseDouble(eleOrderline.getAttribute(AcademyConstants.ATTR_ORDERED_QTY));
			strCancelledQty=String.valueOf(dOriginalOrderQty-dOrderedQty);
			dInvoicedQty=Double.parseDouble(strInvoicedQty);
			strProcessingQty=String.valueOf(dOrderedQty-dInvoicedQty);
			if (!YFCCommon.isVoid(sDeliveryMethod) && sDeliveryMethod.equals(AcademyConstants.STR_SHIP_DELIVERY_METHOD)) {
				if (!YFCCommon.isVoid(strCancelledQty) && !strCancelledQty.equals(AcademyConstants.STR_ZERO_IN_DECIMAL)) {
					AddCancelledItem(inDoc, eleOrderline,strCancelledQty);
				}
				if (!YFCCommon.isVoid(strProcessingQty) && !strProcessingQty.equals(AcademyConstants.STR_ZERO_IN_DECIMAL)) {
					AddProcessingItem(inDoc, eleOrderline,strProcessingQty);
					iProcesingItemCount=iProcesingItemCount+1;
				}
			}
		}
		eleIndoc.setAttribute("ProcesingItemCount", String.valueOf(iProcesingItemCount));
		updateShippedItemDetails(inDoc);
		updatePaymentDetails(env, eleIndoc,inDoc);
		AcademyEmailUtil.updateMessageRef(env, eleIndoc, "STH_PARTIAL_FULFILL_MSG_ID_PROD", "STH_PARTIAL_FULFILL_MSG_ID_STG",
				"STH_PARTIAL_FULFILL_MSG_TYPE");
		// Add Urls
		addUrls(inDoc, env, eleIndoc ,"", "");
		log.verbose("End of updateEMailTemplateForShipment Method");
	}

	
	/**
	 * This method updates tracking no
	 * 
	 * @param eleOrderLine
	 * @throws Exception
	 */
	public static String fetchTrackingNumber(Element eleIndoc,String currentShipmentKey) throws Exception {
		String sTrackingNo = XPathUtil.getString(eleIndoc, "/Order/Shipments/Shipment[@ShipmentKey='"
				+ currentShipmentKey + "']/Containers/Container/@TrackingNo");
		String sProNo="";
		String sShipmentNo="";
		if (YFCCommon.isVoid(sTrackingNo)) {
			//CEVA carrier
			sProNo=XPathUtil.getString(eleIndoc, "/Order/Shipments/Shipment[@ShipmentKey='"
					+ currentShipmentKey + "']/@ProNo");
			if (!YFCCommon.isVoid(sProNo)) {
				sTrackingNo=sProNo;
			}else {
			//EFW carrier
			sShipmentNo=XPathUtil.getString(eleIndoc, "/Order/Shipments/Shipment[@ShipmentKey='"
					+ currentShipmentKey + "']/@ShipmentNo");
			sTrackingNo=sShipmentNo;
			}
		}
			
		return sTrackingNo;
	}
	
	
	

	/**
	 * Format order date in required format(MMM DD)
	 * 
	 * @param eleIndoc
	 */
	private void setOrderPlacedDate(Document inDoc) {
		Date orderDate = null;
		String strOrderDate = inDoc.getDocumentElement().getAttribute("OrderDate");
		strOrderDate = strOrderDate.substring(0, 10);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String strRequiredDate = "";
		try {
			orderDate = sdf.parse(strOrderDate);
			strRequiredDate = orderDate.toString();
			strRequiredDate = strRequiredDate.substring(4, 10);
			log.verbose("strRequiredDate : " + strRequiredDate);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!YFCCommon.isVoid(strRequiredDate))
			inDoc.getDocumentElement().setAttribute("OrderPlacedDate", strRequiredDate);

	}
	
	private void updateShippedItemDetails(Document inDoc) throws Exception {
		Element eleIndoc = inDoc.getDocumentElement();
		Element elePackage=null;
		Element eleShipment=null;
		Element eleShipped = XMLUtil.createElement(inDoc, "Shipped", null);
		Element eleProductInfo = (Element) XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo");
		eleProductInfo.appendChild(eleShipped);
		
		//OMNI-62935 - Changes START
		NodeList nlShipments = XPathUtil.getNodeList(eleIndoc, 
				"/Order/Shipments/Shipment[@DeliveryMethod='SHP' and @ActualShipmentDate != '' and @ShipmentType != 'STS']");
		//OMNI-62935 - Changes END
		
		//OMNI-51004 - Start changes - Recently shipped items are not appearing on top 
		 ArrayList<String> list = new ArrayList<String>();
         for (int i = 0; i < nlShipments.getLength(); i++) {
        	Element eleCurrentShipment = (Element) nlShipments.item(i);
        	String sActualDate=eleCurrentShipment.getAttribute("ActualShipmentDate");
        	list.add(sActualDate);   
         }
         Collections.sort(list, Collections.reverseOrder()); 
       //OMNI-51004 - End changes - Recently shipped items are not appearing on top 
		int iNoOfShipment=nlShipments.getLength();
		eleShipped.setAttribute("ShipmentCount", String.valueOf(iNoOfShipment));
		if(iNoOfShipment!=0) {
			eleShipment = (Element) nlShipments.item(iNoOfShipment-1);
			//eleIndoc.setAttribute("CurrentShipmentKey", eleShipment.getAttribute(AcademyConstants.SHIPMENT_KEY));
		}
		boolean bLineBreak=false;
		//OMNI-51004 - Start changes - Recently shipped items are not appearing on top 
		int PackageCount=list.size();
		   Iterator<String> shipmentItr = list.iterator();
           while (shipmentItr.hasNext())
           {
        	String sActualDate = shipmentItr.next();
            eleShipment = (Element) XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/Shipments/Shipment[@ActualShipmentDate='" + sActualDate + "']");
			elePackage = XMLUtil.createElement(inDoc, "Package", null);
			elePackage.setAttribute("PackageNo",String.valueOf(PackageCount));
			PackageCount=PackageCount-1;
			eleShipped.appendChild(elePackage);
			//eleShipment = (Element) nlShipments.item(i-1);
		 //OMNI-51004 - End changes - Recently shipped items are not appearing on top 	
			NodeList nlShipmentsLines=null;
			Element eleShipmentLine =null;
			Element orderLine=null;
			int itemCount=0;
			if(bLineBreak) {
				elePackage.setAttribute("LineBreak","Y");
			}
			bLineBreak=true;
			String sShipmentKey=eleShipment.getAttribute(AcademyConstants.SHIPMENT_KEY);
			String sNodeType = XMLUtil.getString(eleShipment, "//Shipment[@ShipmentKey='"+sShipmentKey+"']/ShipNode/@NodeType");
			String sInvoiceNumber = XMLUtil.getString(inDoc,"/Order/OrderInvoiceList/OrderInvoice[@ShipmentKey='" + sShipmentKey + "' and @InvoiceType = 'SHIPMENT']/@InvoiceNo");
	        elePackage.setAttribute("InvoiceNumber",sInvoiceNumber);
				nlShipmentsLines = XPathUtil.getNodeList(eleShipment,  "//Shipment/ShipmentLines/ShipmentLine[@ShipmentKey='"+sShipmentKey+"']");
				for (int j = 0; j < nlShipmentsLines.getLength(); j++) {
					eleShipmentLine = (Element) nlShipmentsLines.item(j);
					orderLine = (Element) eleShipmentLine.getElementsByTagName("OrderLine").item(0);
					String strInvoicedQty=orderLine.getAttribute("InvoicedQty");
					if(!strInvoicedQty.equals("0.00")) {
						//
						//OMNI-51289 - Start changes - Email showing wrong qty
						/*itemCount=(int) (itemCount+Double.parseDouble(strInvoicedQty));
						AddPackageItem(elePackage,strInvoicedQty,orderLine,inDoc,sShipmentKey,false);*/
						String sShippedQty=eleShipmentLine.getAttribute("Quantity");
						itemCount=(int) (itemCount+Double.parseDouble(sShippedQty));
						AddPackageItem(elePackage,sShippedQty,orderLine,inDoc,sShipmentKey,false);
						//OMNI-51289 - End changes - Email showing wrong qty
					}else if (AcademyConstants.DROP_SHIP_NODE_TYPE.equalsIgnoreCase(sNodeType)) {
					    //Node Type Condition added for OMNI-66448
						//DSV shipment order lines, pick shipped qty as invoiced qty will be '0'
						String sShippedQty=eleShipmentLine.getAttribute("Quantity");
						itemCount=(int) (itemCount+Double.parseDouble(sShippedQty));
						AddPackageItem(elePackage,sShippedQty,orderLine,inDoc,sShipmentKey,true);
					}
					}//end of shipment line iteration
				elePackage.setAttribute("ItemCount",String.valueOf(itemCount));
				AddCarrierInfo(inDoc, eleShipment);
				AddTrackingInfo(inDoc, eleShipment);
		}
	}
	
	private void updatePaymentDetails(YFSEnvironment env,Element eleIndoc,Document inDoc) throws Exception {
		NodeList nlPaymentMethods=XPathUtil.getNodeList(eleIndoc, "/Order/PaymentMethods/PaymentMethod");
		for (int i = 0; i < nlPaymentMethods.getLength(); i++) {
			Element elePaymentMethod = (Element) nlPaymentMethods.item(i);
			String sCreditCardType = elePaymentMethod.getAttribute("CreditCardType");
			
			//OMNI-61094 : START
			String strPaymentType = elePaymentMethod.getAttribute(AcademyConstants.ATTR_PAYMENT_TYPE);			
			if (!YFCObject.isNull(strPaymentType) && (AcademyConstants.PAYPAL.equalsIgnoreCase(strPaymentType)
					|| AcademyConstants.STR_PLCC_PAYMENT.equalsIgnoreCase(strPaymentType))) {
				sCreditCardType=strPaymentType;
			}			
			//OMNI-61094 : END
			
			//OMNI-65601 - START
			String strPaymentRef5 = elePaymentMethod.getAttribute(AcademyConstants.ATTR_PAYMENT_REF_5);
			if (!YFCObject.isNull(strPaymentRef5) && (AcademyConstants.KLARNA_PAY_TYP.equalsIgnoreCase(strPaymentRef5))) {
				sCreditCardType=strPaymentRef5;
			}
			//OMNI-65601 - END
			
			String strCommonCodeCCType = getCommonCodeForCrediCardType(env, sCreditCardType);
			log.verbose("AcademyPostEmailContentOnShipment-updateEMailTemplateForShipment - strCommonCodeCCType :: "+strCommonCodeCCType);
			elePaymentMethod.setAttribute("CreditCardType",	strCommonCodeCCType);
		}
	}
	
	private String formatDate(String strDate) {
		Date orderDate = null;
		String strRequiredDate = "";
		try {
			if (!YFCCommon.isVoid(strDate)) {
			strDate = strDate.substring(0, 10);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			orderDate = sdf.parse(strDate);
			strRequiredDate = orderDate.toString();
			strRequiredDate = strRequiredDate.substring(4, 10);
			}
			log.verbose("strRequiredDate : " + strRequiredDate);

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strRequiredDate;

	}

	
	private void AddCarrierInfo(Document inDoc, Element eleShipment) throws Exception {
		Element eleShipped = null;
		Element eleShippingCarier = XMLUtil.createElement(inDoc, "ShippingCarrierInfo", null);
		String sCarrierScac = eleShipment.getAttribute(AcademyConstants.ATTR_SCAC);
		eleShippingCarier.setAttribute("ShippingCarrier", fetchCarrierServiceCode(sCarrierScac));
		if (!YFCObject.isVoid(XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo/Shipped"))) {
			eleShipped = (Element) XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo/Shipped");
			eleShipped.appendChild(eleShippingCarier);
		} 
	}
	
	private void AddTrackingInfo(Document inDoc, Element eleShipment) throws Exception {
		Element eleIndoc = inDoc.getDocumentElement();
		String sShipmentKey=eleShipment.getAttribute(AcademyConstants.SHIPMENT_KEY);
		String sTrackingNo=fetchTrackingNumber(eleIndoc,sShipmentKey);
		Element eleShipped = null;
		Element eleTrackingInfo = XMLUtil.createElement(inDoc, "TrackingInfo", null);
		String sTrackingURL=fetchTrackingUrl(inDoc, eleShipment, sTrackingNo);
		eleTrackingInfo.setAttribute("TrackingNumber", sTrackingNo);
		if (!YFCCommon.isVoid(sTrackingURL)) {
		eleTrackingInfo.setAttribute("TrackingURL", sTrackingURL);
		}
		if (!YFCObject.isVoid(XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo/Shipped"))) {
			eleShipped = (Element) XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo/Shipped");
			eleShipped.appendChild(eleTrackingInfo);
		} 
	}
	
	
	private String  fetchTrackingUrl(Document inDoc, Element eleShipment, String sTrackingNo) throws Exception {
		Element eleIndoc = inDoc.getDocumentElement();
		String strEmailTemplateType = eleIndoc.getAttribute(AcademyConstants.ATTR_EMAIL_TEMPLATE_TYPE);
		String strURL_TrackingDetails = null;
		String strSCAC=null;
		String serviceCode=null;
		String originZipCode=null;
		String strZipCodeShipTo = null;
		String strShipmentTrackingDetails = null;
		log.verbose("eleShipment :" + XMLUtil.getElementXMLString(eleShipment));
		
		Element elePersonInfoShipTo = SCXmlUtil.getChildElement(eleIndoc, "PersonInfoShipTo");

		if (!YFCCommon.isVoid(elePersonInfoShipTo)) {

			strZipCodeShipTo = elePersonInfoShipTo.getAttribute(AcademyConstants.ZIP_CODE);

		}
		//OMNI-108945 Start
		if (!YFCObject.isVoid(eleShipment)) {	
			if(!YFCObject.isVoid(strEmailTemplateType) && AcademyConstants.STR_PACK.equals(strEmailTemplateType))
			{
				strSCAC=eleShipment.getAttribute("ExtnSCAC");
				log.verbose("AcademyPostEmailContentOnShipment-updateEMailTemplateForShipment - strExtnSCAC :: "+strSCAC);
				
			}
			else
			{
				strSCAC=eleShipment.getAttribute("ExtnSCAC");
				serviceCode=eleShipment.getAttribute("ExtnServiceCode");
				originZipCode = XPathUtil.getString(eleShipment, "//Shipment/ShipNode/ShipNodePersonInfo/@ZipCode");
				log.verbose("originZipCode :::" + originZipCode);
			}
			
			
		}
		
		if(!YFCObject.isVoid(strEmailTemplateType) && AcademyConstants.STR_PACK.equals(strEmailTemplateType))
		{
			 strShipmentTrackingDetails = YFSSystem.getProperty(AcademyConstants.PROP_TRACKINGURL_CONVEY);
		}
		else
		{

		 strShipmentTrackingDetails = YFSSystem.getProperty(AcademyConstants.PROP_VIEW_TRACKING_URL);
		}
		if (!YFCCommon.isVoid(strShipmentTrackingDetails)) {
	          if(!YFCObject.isVoid(strEmailTemplateType) && "PACK".equals(strEmailTemplateType)) {
	        	   strURL_TrackingDetails = strShipmentTrackingDetails.replace("{$scac}",strSCAC );
	        	   strURL_TrackingDetails = strURL_TrackingDetails.replace("{$currTrackingNo}", sTrackingNo);
	           }
	           else {
				strURL_TrackingDetails = strShipmentTrackingDetails.replace("{$scac}",strSCAC );
				strURL_TrackingDetails = strURL_TrackingDetails.replace("{$currTrackingNo}", sTrackingNo);
				strURL_TrackingDetails = strURL_TrackingDetails.replace("{$serviceCode}", serviceCode);
				strURL_TrackingDetails = strURL_TrackingDetails.replace("{$originZipCode}",originZipCode );
				strURL_TrackingDetails = strURL_TrackingDetails.replace("{$customerZipCode}", strZipCodeShipTo);
			
	           }
	//OMNI-108945 End
			}
			return strURL_TrackingDetails;
	}
	
	
	private void AddPackageItem(Element elePackage, String strInvoicedQty,Element eleOrderline,Document inDoc,String sShipmentKey,boolean isDSVLine) throws Exception {
		
		log.verbose("AddPackageItem Method :::");
		log.verbose("strInvoicedQty :::"+strInvoicedQty);
		
		String olkey = eleOrderline.getAttribute("OrderLineKey");

		if(isDSVLine) {
		//OMNI-51290 - Start changes - Same item is shown for DSV shipment	
		// olkey = XMLUtil.getString(inDoc,
				//		"/Order/OrderLines/OrderLine[./ShipmentLines/ShipmentLine[@ShipmentKey="+sShipmentKey+"]]/@OrderLineKey");
		 olkey = XMLUtil.getString(inDoc,
					"/Order/OrderLines/OrderLine[./ShipmentLines/ShipmentLine[@OrderLineKey='"+olkey+"']]/@OrderLineKey");
		//OMNI-51290 - End changes - Same item is shown for DSV shipment	 
		}

		String olUnitPrice = XMLUtil.getString(inDoc,

				"/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/LinePriceInfo/@UnitPrice");

		Element eleItemInfo = createItemInfo(inDoc, olkey, strInvoicedQty, olUnitPrice);

		elePackage.appendChild(eleItemInfo);

	}
	
	
	private void AddProcessingItem(Document inDoc, Element eleOrderline,String strProcessingQty) throws Exception {
		
		log.verbose("AddProcessingItem Method :::");
		log.verbose("strProcessingQty :::"+strProcessingQty);
		Element eleProcessing = null;

		if (!YFCObject.isVoid(XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo/Shipped"))) {

			eleProcessing = (Element) XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo/Shipped");

		} else {

			eleProcessing = XMLUtil.createElement(inDoc, "Processing", null);

			Element eleProductInfo = (Element) XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo");

			eleProductInfo.appendChild(eleProcessing);

		}

		String olkey = eleOrderline.getAttribute("OrderLineKey");

		String olUnitPrice = XMLUtil.getString(inDoc,

				"/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/LinePriceInfo/@UnitPrice");

		Element eleItemInfo = createItemInfo(inDoc, olkey, strProcessingQty, olUnitPrice);

		eleProcessing.appendChild(eleItemInfo);

	}
	
	
	private void AddCancelledItem(Document inDoc, Element eleOrderline,String sCancelledQty ) throws Exception {
		
		log.verbose("AddCancelledItem Method :::");
		log.verbose("sCancelledQty :::"+sCancelledQty);
		Element eleCancelled = null;

		if (!YFCObject.isVoid(XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo/Shipped"))) {

			eleCancelled = (Element) XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo/Shipped");

		} else {

			eleCancelled = XMLUtil.createElement(inDoc, "Cancelled", null);

			Element eleProductInfo = (Element) XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo");

			eleProductInfo.appendChild(eleCancelled);

		}

		String olkey = eleOrderline.getAttribute("OrderLineKey");

		String olUnitPrice = XMLUtil.getString(inDoc,

				"/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/LinePriceInfo/@UnitPrice");

		Element eleItemInfo = createItemInfo(inDoc, olkey, sCancelledQty, olUnitPrice);

		eleCancelled.appendChild(eleItemInfo);

	}

	private Element createItemInfo(Document inDoc, String olkey, String orderQty, String olUnitPrice) throws Exception {
		Element eleItemInfo = XMLUtil.createElement(inDoc, "ItemInfo", null);

		eleItemInfo.setAttribute("OrderLineKey", olkey);

		eleItemInfo.setAttribute("ItemID", XMLUtil.getString(inDoc,

				"/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/ItemDetails/@ItemID"));

		eleItemInfo.setAttribute("Description", XMLUtil.getString(inDoc, "/Order/OrderLines/OrderLine[@OrderLineKey='"

				+ olkey + "']/ItemDetails/PrimaryInformation/@Description"));

		eleItemInfo.setAttribute("OrderQty", orderQty);
		//OMNI-51289 - Start changes - Email showing wrong qty
		/*eleItemInfo.setAttribute("InvoicedQty", XMLUtil.getString(inDoc, "/Order/OrderLines/OrderLine[@OrderLineKey='"

				+ olkey + "']/@InvoicedQty"));*/
		eleItemInfo.setAttribute("InvoicedQty",orderQty);
		//OMNI-51289 - End  changes - Email showing wrong qty
		eleItemInfo.setAttribute("ImageLoc", XMLUtil.getString(inDoc, "/Order/OrderLines/OrderLine[@OrderLineKey='"

				+ olkey + "']/ItemDetails/PrimaryInformation/@ImageLocation"));

		eleItemInfo.setAttribute("ImageID", XMLUtil.getString(inDoc,

				"/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/ItemDetails/PrimaryInformation/@ImageID"));
		
		
		
		String sPromiseDate=  formatDate(XMLUtil.getString(inDoc,"/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/Extn/@ExtnInitialPromiseDate"));
		
		eleItemInfo.setAttribute("PromiseDate",sPromiseDate);

		eleItemInfo.setAttribute("UnitPrice", olUnitPrice);

		return eleItemInfo;
	}

	

	/**
	 * 
	 * This method updates urls to the output email messsage xml
	 * 
	 * 
	 * 
	 * @param inDoc
	 * 
	 * @param env
	 * 
	 * @param eleIndoc
	 * 
	 * @throws Exception
	 * 
	 */

	private static void addUrls(Document inDoc, YFSEnvironment env, Element eleIndoc,String sTrackingNo,String currentShipmentKey) throws Exception {

		String strURL_ViewOrderDetails = null;
		String strZipCode = null;
		String strZipCodeShipTo = null;

		String strOrderNo = eleIndoc.getAttribute(AcademyConstants.ATTR_ORDER_NO);

		Element elePersonInfoBillTo = SCXmlUtil.getChildElement(eleIndoc, AcademyConstants.ELE_PERSON_INFO_BILL_TO);

		if (!YFCCommon.isVoid(elePersonInfoBillTo)) {

			strZipCode = elePersonInfoBillTo.getAttribute(AcademyConstants.ZIP_CODE);

		}

		
		Element elePersonInfoShipTo = SCXmlUtil.getChildElement(eleIndoc, "PersonInfoShipTo");

		if (!YFCCommon.isVoid(elePersonInfoShipTo)) {

			strZipCodeShipTo = elePersonInfoShipTo.getAttribute(AcademyConstants.ZIP_CODE);

		}
		// Fetching ORDERDETAILS URL from CO file

		String strViewOrderDetails = YFSSystem.getProperty(AcademyConstants.PROP_VIEW_ORDERDETAILS_URL);

		// fetch tracking url

		if (!YFCCommon.isVoid(strViewOrderDetails)) {

			strURL_ViewOrderDetails = strViewOrderDetails.replace("@@@@", strOrderNo);

			strURL_ViewOrderDetails = strURL_ViewOrderDetails.replace("$$$$", strZipCode);

			eleIndoc.setAttribute("ViewOrderDetails", strURL_ViewOrderDetails);

		}
		
	}

	/**
	 * 
	 * This method updates carrier sercive code
	 * 
	 * 
	 * @param inDoc
	 * 
	 * @throws Exception
	 * 
	 */

	private static String fetchCarrierServiceCode(String strSCAC) throws Exception {

		String sCarrierServiceCode="";
		if(strSCAC.equalsIgnoreCase("UPSN")) {
			sCarrierServiceCode="UPS";
		}
		else if(strSCAC.equalsIgnoreCase("USPS-Letter")) {
			sCarrierServiceCode="USPS";
		}
		else if(strSCAC.equalsIgnoreCase("USPS-Endicia")) {
			sCarrierServiceCode="USPS";
		}
		else if(strSCAC.equalsIgnoreCase("FEDX")) {
			sCarrierServiceCode="FEDEX";
		}else {
			sCarrierServiceCode=strSCAC;
		}
		return sCarrierServiceCode;

		}
		
		
	/**
	 * 
	 * This method creates ProductInfo elements and appends it to the output email
	 * 
	 * message xml
	 * 
	 * 
	 * 
	 * @param inDoc
	 * 
	 * @throws Exception
	 * 
	 */

	private void createProductInfoElement(Document inDoc) throws Exception {

		if (YFCObject.isVoid(XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo"))) {

			Element eleProductInfo = XMLUtil.createElement(inDoc, "ProductInfo", null);

			inDoc.getDocumentElement().appendChild(eleProductInfo);

		}

	}
	
	/**
	 * This method return the common code short description value.
	 * @param env
	 * @param strCreditCardType
	 * @return
	 */
	/**inDoc:
	 * <CommonCode CodeType="PACK_SLIP_PAY_TYPE" CodeValue="MasterCard"/>
	 * 
		outDoc:
		<CommonCodeList>
			<CommonCode CodeLongDescription="MASTER" CodeShortDescription="MASTER" CodeType="PACK_SLIP_PAY_TYPE" CodeValue="MasterCard"/>
		</CommonCodeList>
	 *
	 */
	private String getCommonCodeForCrediCardType(YFSEnvironment env, String strCreditCardType) {
		log.verbose( "AcademyPostPartialFulfillmentEmailContent-getCommonCodeForCrediCardType :: "+  strCreditCardType);
		Document inDocGetCommonCodeList = null;
		Document outDocGetCommonCodeList = null;
		String strCommonCodeCCType = "";		
		try {			
			inDocGetCommonCodeList = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
			inDocGetCommonCodeList.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, AcademyConstants.STR_PACK_SLIP_PAY_TYPE);
			inDocGetCommonCodeList.getDocumentElement().setAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE, strCreditCardType);
			outDocGetCommonCodeList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMON_CODELIST, inDocGetCommonCodeList);
			log.verbose( "AcademyPostPartialFulfillmentEmailContent-getCommonCodeForCrediCardType - outDocGetCommonCodeList :: "+  XMLUtil.getXMLString(outDocGetCommonCodeList));
			if(!YFCObject.isVoid(outDocGetCommonCodeList) && 
					outDocGetCommonCodeList.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_COMMON_CODE).getLength() > 0)
			{			
				Element eleCommonCode = XMLUtil.getElementByXPath(outDocGetCommonCodeList, AcademyConstants.XPATH_COMMON_CODE_ELE);
				strCommonCodeCCType = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
				log.verbose( "AcademyPostPartialFulfillmentEmailContent-getCommonCodeForCrediCardType - strCommonCodeCCType :: "+  strCommonCodeCCType);
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return strCommonCodeCCType;
	}

}