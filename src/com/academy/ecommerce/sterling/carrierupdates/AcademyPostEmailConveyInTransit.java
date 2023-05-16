package com.academy.ecommerce.sterling.carrierupdates;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.email.api.AcademyPostEmailContentPickedUp;
import com.academy.ecommerce.sterling.email.util.AcademyEmailUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;

import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyPostEmailConveyInTransit {
	
	

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPostEmailContentPickedUp.class);

	/**
	 * 
	 * This method customizes input xml to post Convey in_transit email
	 * message to
	 * 
	 * ESB queue
	 * 
	 * 
	 * 
	 * @param inDoc
	 * 
	 * @return docSMSMessage
	 * 
	 * @throws Exception
	 * 
	 */

	public Document prepareEmailContent(YFSEnvironment env, Document inDoc) throws Exception {

		log.verbose("AcademyPostEmailConveyInTransit.prepareEmailContent()_InXML:" + XMLUtil.getXMLString(inDoc));

		Document emailContent=updateEMailTemplateForInTransit(env, inDoc);

		sendEmail(env, emailContent);

		log.verbose("AcademyPostEmailConveyInTransit.prepareEmailContent()_returnDoc:" + XMLUtil.getXMLString(emailContent));

		return inDoc;

	}
	
	private Document updateEMailTemplateForInTransit(YFSEnvironment env, Document inDoc) throws Exception {
		Document inputXML=null;
		Document outXML=null;
		try {
			Element eleIndoc = inDoc.getDocumentElement();
			String strOHKey = eleIndoc.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
			String strInMessage = eleIndoc.getAttribute(AcademyConstants.ATTR_MESSAGE);
			String strCurrentShipment = eleIndoc.getAttribute(AcademyConstants.SHIPMENT_KEY);
			String strCurrentShipmentTrackingNo = eleIndoc.getAttribute("TrackingNoForEmail");
			inputXML = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			inputXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,strOHKey );
			
					env.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS, "global/template/api/getCompleteOrderDetails.ToSendEmail.xml");	
				
			
			outXML = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS, inputXML);
		
			Element eleOutdoc = outXML.getDocumentElement();
			
			
			eleOutdoc.setAttribute("CurrentShipmentKey", strCurrentShipment);
			
			eleOutdoc.setAttribute("EmailTrackingNo", strCurrentShipmentTrackingNo);
			String sCarrierScac = XPathUtil.getString(eleOutdoc, "/Order/Shipments/Shipment[@ShipmentKey='"
					+ strCurrentShipment + "']/@SCAC");
			eleOutdoc.setAttribute("EmailCarrierServiceCode",fetchCarrierServiceCode(sCarrierScac));
			
			AcademyEmailUtil.removeOrderlines(outXML, env, eleOutdoc);
		
			NodeList nlOrderLines = AcademyEmailUtil.getCompleteOrderLineLines(eleOutdoc);

			for (int i = 0; i < nlOrderLines.getLength(); i++) {

				Element eleOrderline = (Element) nlOrderLines.item(i);

				// populate ProductInfo elements

				createProductInfoElement(outXML);
				AddShippedItem(outXML, eleOrderline);
				log.verbose("End of updateEMailTemplateForShipment Method");

			}
			
			NodeList nlPaymentMethods=XPathUtil.getNodeList(eleOutdoc, "/Order/PaymentMethods/PaymentMethod");
			for (int i = 0; i < nlPaymentMethods.getLength(); i++) {
				Element elePaymentMethod = (Element) nlPaymentMethods.item(i);
				String sCreditCardType = elePaymentMethod.getAttribute("CreditCardType");
				
				
				String strPaymentType = elePaymentMethod.getAttribute(AcademyConstants.ATTR_PAYMENT_TYPE);			
				if (!YFCObject.isNull(strPaymentType) && (AcademyConstants.PAYPAL.equalsIgnoreCase(strPaymentType)
						|| AcademyConstants.STR_PLCC_PAYMENT.equalsIgnoreCase(strPaymentType))) {
					sCreditCardType=strPaymentType;
				}			
				
				String strPaymentRef5 = elePaymentMethod.getAttribute(AcademyConstants.ATTR_PAYMENT_REF_5);
				if (!YFCObject.isNull(strPaymentRef5) && (AcademyConstants.KLARNA_PAY_TYP.equalsIgnoreCase(strPaymentRef5))) {
					sCreditCardType=strPaymentRef5;
				}
				
				
				String strCommonCodeCCType = getCommonCodeForCrediCardType(env, sCreditCardType);
				log.verbose("AcademyPostEmailContentOnShipment-updateEMailTemplateForShipment - strCommonCodeCCType :: "+strCommonCodeCCType);
				elePaymentMethod.setAttribute("CreditCardType",	strCommonCodeCCType);
			}
			
			String sItemCount=String.valueOf(nlOrderLines.getLength());
			eleOutdoc.setAttribute("EmailItemCount",sItemCount);
			// Populate MessageId and MessageType
			if ("PARTIAL".equals(strInMessage)) {
				AcademyEmailUtil.updateMessageRef(env, eleOutdoc, "CONVEY_IN_TRANSIT_MSG_ID_PROD", "CONVEY_IN_TRANSIT_MSG_ID",
						"CONVEY_IN_TRANSIT_MSG_TYPE_PARTIAL");
				
			}
			else {
				AcademyEmailUtil.updateMessageRef(env, eleOutdoc, "CONVEY_IN_TRANSIT_MSG_ID_PROD", "CONVEY_IN_TRANSIT_MSG_ID",
						"CONVEY_IN_TRANSIT_MSG_TYPE");
				
			}

			// Add Urls
			addUrls(outXML, env, eleOutdoc ,strCurrentShipmentTrackingNo, strCurrentShipment);
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		return outXML;
		
	}
	
	private void AddShippedItem(Document inDoc, Element eleOrderline) throws Exception {
		Element eleShipped = null;

		if (!YFCObject.isVoid(XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo/Shipped"))) {

			eleShipped = (Element) XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo/Shipped");

		} else {

			eleShipped = XMLUtil.createElement(inDoc, "Shipped", null);

			Element eleProductInfo = (Element) XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo");

			eleProductInfo.appendChild(eleShipped);

		}

		String orderQty = eleOrderline.getAttribute("OrderedQty");
		String olkey = eleOrderline.getAttribute("OrderLineKey");

		String olUnitPrice = XMLUtil.getString(inDoc,

				"/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/LinePriceInfo/@UnitPrice");

		Element eleItemInfo = createItemInfo(inDoc, olkey, orderQty, olUnitPrice);

		eleShipped.appendChild(eleItemInfo);

	}

	private Element createItemInfo(Document inDoc, String olkey, String orderQty, String olUnitPrice) throws Exception {
		Element eleItemInfo = XMLUtil.createElement(inDoc, "ItemInfo", null);

		eleItemInfo.setAttribute("OrderLineKey", olkey);

		eleItemInfo.setAttribute("ItemID", XMLUtil.getString(inDoc,

				"/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/ItemDetails/@ItemID"));

		eleItemInfo.setAttribute("Description", XMLUtil.getString(inDoc, "/Order/OrderLines/OrderLine[@OrderLineKey='"

				+ olkey + "']/ItemDetails/PrimaryInformation/@Description"));

		eleItemInfo.setAttribute("OrderQty", orderQty);

		eleItemInfo.setAttribute("InvoicedQty", XMLUtil.getString(inDoc, "/Order/OrderLines/OrderLine[@OrderLineKey='"

				+ olkey + "']/@InvoicedQty"));

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
		String strURL_TrackingDetails = null;
		String strSCAC=null;
		
		String strZipCode = null;
		
		String strShipmentTrackingDetails = null;
		
		String strOrderNo = eleIndoc.getAttribute(AcademyConstants.ATTR_ORDER_NO);

		Element elePersonInfoBillTo = SCXmlUtil.getChildElement(eleIndoc, AcademyConstants.ELE_PERSON_INFO_BILL_TO);

		if (!YFCCommon.isVoid(elePersonInfoBillTo)) {

			strZipCode = elePersonInfoBillTo.getAttribute(AcademyConstants.ZIP_CODE);

		}

		
		
		
		// Fetching ORDERDETAILS URL from COP file

		String strViewOrderDetails = YFSSystem.getProperty(AcademyConstants.PROP_VIEW_ORDERDETAILS_URL);

		

		

		// https://uat7www.academy.com/myaccount/orderSearch/@@@@/$$$$

		if (!YFCCommon.isVoid(strViewOrderDetails)) {

			strURL_ViewOrderDetails = strViewOrderDetails.replace("@@@@", strOrderNo);

			strURL_ViewOrderDetails = strURL_ViewOrderDetails.replace("$$$$", strZipCode);

			eleIndoc.setAttribute("ViewOrderDetails", strURL_ViewOrderDetails);

		}
		
		Element eleShipment = XMLUtil.getElementByXPath(inDoc, "/Order/Shipments/Shipment[@ShipmentKey='"+ currentShipmentKey +"']");
		log.verbose("eleShipment :" + XMLUtil.getElementXMLString(eleShipment));
		if (!YFCObject.isVoid(eleShipment)) {		
			
				strSCAC=eleShipment.getAttribute("ExtnSCAC");
				log.verbose("AcademyPostEmailContentOnShipment- addURLS - strExtnSCAC :: "+strSCAC);
				
			
			
			
			
		}
		// fetch tracking url
		// https://tracking.getconvey.com/academy/{$scac}?tn={$currTrackingNo}
				
						
							 strShipmentTrackingDetails = YFSSystem.getProperty(AcademyConstants.PROP_TRACKINGURL_CONVEY);
						
					
			if (!YFCCommon.isVoid(strShipmentTrackingDetails)) 
			{
				
					strURL_TrackingDetails = strShipmentTrackingDetails.replace("{$scac}",strSCAC );
					strURL_TrackingDetails = strURL_TrackingDetails.replace("{$currTrackingNo}", sTrackingNo);

			
				eleIndoc.setAttribute("EmailTrackingURL", strURL_TrackingDetails);
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
		log.verbose( "AcademyPostEmailContentOnShipment-getCommonCodeForCrediCardType :: "+  strCreditCardType);
		Document inDocGetCommonCodeList = null;
		Document outDocGetCommonCodeList = null;
		String strCommonCodeCCType = "";		
		try {			
			inDocGetCommonCodeList = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
			inDocGetCommonCodeList.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, AcademyConstants.STR_PACK_SLIP_PAY_TYPE);
			inDocGetCommonCodeList.getDocumentElement().setAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE, strCreditCardType);
			outDocGetCommonCodeList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMON_CODELIST, inDocGetCommonCodeList);
			log.verbose( "AcademyPostEmailContentOnShipment-getCommonCodeForCrediCardType - outDocGetCommonCodeList :: "+  XMLUtil.getXMLString(outDocGetCommonCodeList));
			if(!YFCObject.isVoid(outDocGetCommonCodeList) && 
					outDocGetCommonCodeList.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_COMMON_CODE).getLength() > 0)
			{			
				Element eleCommonCode = XMLUtil.getElementByXPath(outDocGetCommonCodeList, AcademyConstants.XPATH_COMMON_CODE_ELE);
				strCommonCodeCCType = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
				log.verbose( "AcademyPostEmailContentOnShipment-getCommonCodeForCrediCardType - strCommonCodeCCType :: "+  strCommonCodeCCType);
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return strCommonCodeCCType;
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

	private void sendEmail(YFSEnvironment env, Document inDoc) throws Exception {

		// send customer email

		log.verbose("Before Sending customer email :" + XMLUtil.getXMLString(inDoc));

		String customerEMailID = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID);

		if (!YFCCommon.isVoid(customerEMailID) && customerEMailID.contains(",")) {

			customerEMailID = customerEMailID.substring(0, customerEMailID.indexOf(","));

			inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID, customerEMailID);

		}

	 Document emailSentOutDoc=AcademyUtil.invokeService(env,

				"AcademySendInTransitEmailToListrak", inDoc);

		log.verbose("Sent InTransit customer email :" + XMLUtil.getXMLString(emailSentOutDoc));

		// check and send alternate email

		if (AcademyEmailUtil.isAlternamePickUpCustomerEmailExist(inDoc.getDocumentElement())) {

			AcademyEmailUtil.setAlternamePickUpCustomerEmailExist(inDoc.getDocumentElement());

			log.verbose("Before Sending InTransit email :" + XMLUtil.getXMLString(inDoc));

			// emailSentOutDoc = AcademyUtil.invokeService(env,

			// AcademyConstants.ACAD_RFCP_LISTRAK_EMAIL_SERVICE, inDoc);

			emailSentOutDoc=	 AcademyUtil.invokeService(env, "AcademySendPickedUpEmailToListrak", inDoc);

			log.verbose("Sent InTransit mail :" + XMLUtil.getXMLString(emailSentOutDoc));

		}

	}

}
