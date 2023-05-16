package com.academy.ecommerce.sterling.email.api;

import org.w3c.dom.DOMException;

/**#########################################################################################

 *

 * Project Name                : ESP

 * Module                      : OMNI-40289,40848

 * Date                        : 21-JULY-2021 

 * Description				  : This class translates/updates shipped Email message xml posted to ESB queue.

 *

 * #########################################################################################*/

import org.w3c.dom.Document;

import com.academy.ecommerce.sterling.email.util.AcademyEmailUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;

import com.academy.util.xml.XMLUtil;

import com.academy.util.xml.XPathUtil;

import com.sterlingcommerce.baseutil.SCXmlUtil;

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
import java.util.Date;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

public class AcademyPostEmailContentOnShipment implements YIFCustomApi {

	private Properties props;

	public void setProperties(Properties props) throws Exception {

		this.props = props;

	}

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPostEmailContentOnShipment.class);

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

		log.verbose("AcademyPostEmailContentOnShipment.prepareEmailContent()_InXML:" + XMLUtil.getXMLString(inDoc));

		updateEMailTemplateForShipment(env, inDoc);

		log.verbose("AcademyPostEmailContentOnShipment.prepareEmailContent()_returnDoc:" + XMLUtil.getXMLString(inDoc));

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

		log.verbose("Inside updateEMailTemplateForShipment Method");

		Element eleIndoc = inDoc.getDocumentElement();
		

		String currentShipmentKey = XPathUtil.getString(eleIndoc, "/Order/@CurrentShipmentKey");
		String sCarrierScac = XPathUtil.getString(eleIndoc, "/Order/Shipments/Shipment[@ShipmentKey='"
				+ currentShipmentKey + "']/@SCAC");
		eleIndoc.setAttribute("EmailCarrierServiceCode",fetchCarrierServiceCode(sCarrierScac));
		
		String sTrackingNo=fetchTrackingNumber(eleIndoc,currentShipmentKey);

		AcademyEmailUtil.removeOrderlines(inDoc, env, eleIndoc);

		setOrderPlacedDate(inDoc);

		NodeList nlOrderLines = AcademyEmailUtil.getCompleteOrderLineLines(eleIndoc);

		for (int i = 0; i < nlOrderLines.getLength(); i++) {

			Element eleOrderline = (Element) nlOrderLines.item(i);

			// populate messageType,messageID and ProductInfo elements

			createProductInfoElement(inDoc);
			AddShippedItem(inDoc, eleOrderline);
			log.verbose("End of updateEMailTemplateForShipment Method");

		}
		
		NodeList nlPaymentMethods=XPathUtil.getNodeList(eleIndoc, "/Order/PaymentMethods/PaymentMethod");
		for (int i = 0; i < nlPaymentMethods.getLength(); i++) {
			Element elePaymentMethod = (Element) nlPaymentMethods.item(i);
			String sCreditCardType = elePaymentMethod.getAttribute("CreditCardType");
			
			//OMNI-59714 : START
			String strPaymentType = elePaymentMethod.getAttribute(AcademyConstants.ATTR_PAYMENT_TYPE);			
			if (!YFCObject.isNull(strPaymentType) && (AcademyConstants.PAYPAL.equalsIgnoreCase(strPaymentType)
					|| AcademyConstants.STR_PLCC_PAYMENT.equalsIgnoreCase(strPaymentType))) {
				sCreditCardType=strPaymentType;
			}			
			//OMNI-59714 : END
			
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
		
		String sItemCount=String.valueOf(nlOrderLines.getLength());
		eleIndoc.setAttribute("EmailItemCount",sItemCount);
		
		AcademyEmailUtil.updateMessageRef(env, eleIndoc, "STH_SHP_CONF_MSG_ID_PROD", "STH_SHP_CONF_MSG_ID",
				"STH_SHP_CONF_MSG_TYPE");

		// Add Urls
		addUrls(inDoc, env, eleIndoc ,sTrackingNo, currentShipmentKey);

		// Update Order Level Attributes
		eleIndoc.setAttribute("EmailTrackingNo", sTrackingNo);
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
		String serviceCode=null;
		String originZipCode=null;
		String strZipCode = null;
		String strZipCodeShipTo = null;
		String strShipmentTrackingDetails = null;
		String strEmailTemplateType = eleIndoc.getAttribute(AcademyConstants.ATTR_EMAIL_TEMPLATE_TYPE);

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

		

		// https://academy.narvar.com/academy/tracking/{$scac}?tracking_numbers={$currTrackingNo}&amp;service={$serviceCode}&amp;ozip={$originZipCode}&amp;dzip={$customerZipCode}

		// https://uat4www.academy.com/webapp/wcs/stores/servlet/UserAccountOrderStatus?orderId=@@@@&zipCode=$$$$&langId=-1&storeId=10151&catalogId=10051&isSubmitted=true&URL=NonAjaxOrderDetail&errorViewName=GuestOrderStatusView&splitshipstatus=true&isDisplayLeftNav=false

		if (!YFCCommon.isVoid(strViewOrderDetails)) {

			strURL_ViewOrderDetails = strViewOrderDetails.replace("@@@@", strOrderNo);

			strURL_ViewOrderDetails = strURL_ViewOrderDetails.replace("$$$$", strZipCode);

			eleIndoc.setAttribute("ViewOrderDetails", strURL_ViewOrderDetails);

		}
		
		Element eleShipment = XMLUtil.getElementByXPath(inDoc, "/Order/Shipments/Shipment[@ShipmentKey='"+ currentShipmentKey +"']");
		log.verbose("eleShipment :" + XMLUtil.getElementXMLString(eleShipment));
		if (!YFCObject.isVoid(eleShipment)) {		
			// OMNI-108945 Start
			if(!YFCObject.isVoid(strEmailTemplateType) && AcademyConstants.STR_PACK.equals(strEmailTemplateType))
			{
				strSCAC=eleShipment.getAttribute("ExtnSCAC");
				log.verbose("AcademyPostEmailContentOnShipment-updateEMailTemplateForShipment - strExtnSCAC :: "+strSCAC);
				
			}
			else {
				strSCAC=eleShipment.getAttribute("ExtnSCAC");
				serviceCode=eleShipment.getAttribute("ExtnServiceCode");
				originZipCode = XPathUtil.getString(eleShipment, "//Shipment/ShipNode/ShipNodePersonInfo/@ZipCode");
				log.verbose("originZipCode :::" + originZipCode);
			}
			
			
		}
		// fetch tracking url
				
						if(!YFCObject.isVoid(strEmailTemplateType) && "PACK".equals(strEmailTemplateType))
						{
							 strShipmentTrackingDetails = YFSSystem.getProperty(AcademyConstants.PROP_TRACKINGURL_CONVEY);
						}
						else
						{

						 strShipmentTrackingDetails = YFSSystem.getProperty(AcademyConstants.PROP_VIEW_TRACKING_URL);
						}
						
						
			if (!YFCCommon.isVoid(strShipmentTrackingDetails)) 
			{
				if(!YFCObject.isVoid(strEmailTemplateType) && "PACK".equals(strEmailTemplateType))
				{
					strURL_TrackingDetails = strShipmentTrackingDetails.replace("{$scac}",strSCAC );
					strURL_TrackingDetails = strURL_TrackingDetails.replace("{$currTrackingNo}", sTrackingNo);
				}
				
				else
				{
				
	
				strURL_TrackingDetails = strShipmentTrackingDetails.replace("{$scac}",strSCAC );
				strURL_TrackingDetails = strURL_TrackingDetails.replace("{$currTrackingNo}", sTrackingNo);
				strURL_TrackingDetails = strURL_TrackingDetails.replace("{$serviceCode}", serviceCode);
				strURL_TrackingDetails = strURL_TrackingDetails.replace("{$originZipCode}",originZipCode );
				strURL_TrackingDetails = strURL_TrackingDetails.replace("{$customerZipCode}", strZipCodeShipTo);
				}
			
				eleIndoc.setAttribute("EmailTrackingURL", strURL_TrackingDetails);
			}
			}
			
	//OMNI-108945 End
	
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

}