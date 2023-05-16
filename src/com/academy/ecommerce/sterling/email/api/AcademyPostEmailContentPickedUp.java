package com.academy.ecommerce.sterling.email.api;

import org.w3c.dom.DOMException;

/**#########################################################################################

 *

 * Project Name                : ESP

 * Module                      : OMNI-20307,20308

 * Date                        : 18-JUN-2021 

 * Description				  : This class translates/updates PickedUp  Email message xml posted to ESB queue.

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

public class AcademyPostEmailContentPickedUp implements YIFCustomApi {

	private Properties props;

	public void setProperties(Properties props) throws Exception {

		this.props = props;

	}

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPostEmailContentPickedUp.class);

	/**
	 * 
	 * This method customizes input xml to post PickedUp email
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

		log.verbose("AcademyPostEmailContentPickedUp.prepareEmailContent()_InXML:" + XMLUtil.getXMLString(inDoc));

		updateEMailTemplateForPickedUpBOPIS(env, inDoc);

		sendEmail(env, inDoc);

		log.verbose("AcademyPostEmailContentPickedUp.prepareEmailContent()_returnDoc:" + XMLUtil.getXMLString(inDoc));

		return inDoc;

	}

	/**
	 * 
	 * This method updates pickedup email template XML to post to ESB
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

	private void updateEMailTemplateForPickedUpBOPIS(YFSEnvironment env, Document inDoc) throws Exception {

		log.verbose("Inside updateEMailTemplateForPickedUpBOPIS Method");

		Element eleIndoc = inDoc.getDocumentElement();

		AcademyEmailUtil.removeOrderlines(inDoc, env, eleIndoc);

		setOrderPlacedDate(inDoc);
		
		updatePickedUpTotal(inDoc);
		
		//OMNI-49424--START
		NodeList nlPaymentMethods=XPathUtil.getNodeList(eleIndoc, "/Order/PaymentMethods/PaymentMethod");
		for (int i = 0; i < nlPaymentMethods.getLength(); i++) {
			Element elePaymentMethod = (Element) nlPaymentMethods.item(i);
			String sCreditCardType = elePaymentMethod.getAttribute("CreditCardType");
			
			//OMNI-58481 : START
			String strPaymentType = elePaymentMethod.getAttribute(AcademyConstants.ATTR_PAYMENT_TYPE);			
			if (!YFCObject.isNull(strPaymentType) && (AcademyConstants.PAYPAL.equalsIgnoreCase(strPaymentType)
					|| "PLCC".equalsIgnoreCase(strPaymentType))) {
				sCreditCardType=strPaymentType;
			}			
			//OMNI-58481 : END
			
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
		//OMNI-49424--END

		NodeList nlOrderLines = AcademyEmailUtil.getCompleteOrderLineLines(eleIndoc);

		for (int i = 0; i < nlOrderLines.getLength(); i++) {

			Element eleOrderline = (Element) nlOrderLines.item(i);

				// populate messageType,messageID and ProductInfo elements

				createProductInfoElement(inDoc);
				if (checkPickedLine(eleOrderline) || checkPartialPickedLine(eleOrderline))
					AddPickedUpItem(inDoc, eleOrderline);

				AcademyEmailUtil.updateMessageRef(env, eleIndoc, "RFCP_PICKEDUP_MSG_ID_PROD",
						"RFCP_PICKEDUP_MSG_ID_STAGE", "PN_RFCP_PICKEDUP_MSG_TYPE");

				// Add Urls
				addUrls(inDoc, env, eleIndoc);

				// Update Order Level Attributes
				eleIndoc.setAttribute("CustomerName", AcademyEmailUtil.getBillToCustomerName(eleIndoc));

				if (!YFCCommon.isVoid(AcademyEmailUtil.getAlternamePickUpCustomerName(eleIndoc)))
					eleIndoc.setAttribute("AlternamePickUpPerson",
							AcademyEmailUtil.getAlternamePickUpCustomerName(eleIndoc));

				eleIndoc.setAttribute("DayPhone", AcademyEmailUtil.getDayPhone(eleIndoc));

				log.verbose("End of updateEMailTemplateForPickedUpBOPIS Method");

		}

	}

	private void updatePickedUpTotal(Document inDoc) throws Exception {
		Double dSubTotal = 0.00;
		Double dShippingCharge = 0.00;
		Double dTaxes = 0.00;
		Double dSpecialOrderTotal = 0.00;
		Double dDiscount = 0.00;
		Double dGiftCardAmount = 0.00;
		Double dOrderTotal = 0.00;
		
		Element eleInputdoc = inDoc.getDocumentElement();
		
		NodeList nOrderLines = XPathUtil.getNodeList(eleInputdoc, "/Order/OrderLines/OrderLine");
		
		for (int i = 0; i < nOrderLines.getLength(); i++) {

			Element eleOrderline = (Element) nOrderLines.item(i);
			
			Element eleLineTotal = SCXmlUtil.getChildElement(eleOrderline, "LineOverallTotals");
			String strExtendedPrice = eleLineTotal.getAttribute("ExtendedPrice");
			String strTax = eleLineTotal.getAttribute("Tax");
			String strDiscount = eleLineTotal.getAttribute("Discount");
			String strLineTotal = eleLineTotal.getAttribute("LineTotal");
			
			dSubTotal = dSubTotal + Double.parseDouble(strExtendedPrice);
			dTaxes = dTaxes + Double.parseDouble(strTax);
			dDiscount = dDiscount + Double.parseDouble(strDiscount);
			dOrderTotal = dOrderTotal + Double.parseDouble(strLineTotal);
			
			Element eleShippingLineCharge = SCXmlUtil.getXpathElement(eleOrderline, "LineCharges/LineCharge[@ChargeName='ShippingCharge']");
			if (!YFCCommon.isVoid(eleShippingLineCharge)) {
				String strShippingCharge = eleShippingLineCharge.getAttribute("ChargeAmount");
				dShippingCharge = dShippingCharge + Double.parseDouble(strShippingCharge);
				
			}
			
		}
		
		Element eleOverAllTotals = SCXmlUtil.getChildElement(eleInputdoc, "OverallTotals");
		eleOverAllTotals.setAttribute("LineSubTotal", String.format("%.2f", dSubTotal));
		eleOverAllTotals.setAttribute("GrandDiscount", String.format("%.2f", dDiscount));
		eleOverAllTotals.setAttribute("GrandTax", String.format("%.2f", dTaxes));
		eleOverAllTotals.setAttribute("GrandTotal", String.format("%.2f", dOrderTotal));
		
		if (!YFCCommon.isVoid(dShippingCharge))
			eleOverAllTotals.setAttribute("ShippingCost", String.format("%.2f", dShippingCharge));
		
		if (!YFCObject.isVoid(XPathUtil.getNode(eleInputdoc, "/Order/PaymentMethods/PaymentMethod[@PaymentType='GIFT_CARD']"))) {
			NodeList nlGiftCard = XPathUtil.getNodeList(eleInputdoc, 
					"/Order/PaymentMethods/PaymentMethod[@PaymentType='GIFT_CARD']");
			
			for (int i = 0; i < nlGiftCard.getLength(); i++) {
				Element elePaymentGiftCard = (Element) nlGiftCard.item(i);
				
				String strGiftCardAmount = elePaymentGiftCard.getAttribute("MaxChargeLimit");
				dGiftCardAmount = dGiftCardAmount + Double.parseDouble(strGiftCardAmount);
			}
			eleOverAllTotals.setAttribute("GiftCardUsed", String.format("%.2f", dGiftCardAmount));
			
		}
		
		log.verbose("Updated Document is : " +SCXmlUtil.getString(inDoc));
		
	}

	/**
	 * This method checks if the orderline is Picked line or not message xml
	 * 
	 * @param eleOrderLine
	 * @throws Exception
	 */
	private boolean checkPickedLine(Element eleOrderLine) throws Exception {
		//OMNI-50925 Start changes -cancelled Items details populated in picked up email
		String sInvoicedQty=eleOrderLine.getAttribute("InvoicedQty");
		return (Double.parseDouble(sInvoicedQty)!=0.00)
		//OMNI-50925 End changes -cancelled Items details populated in picked up email
				&& (!YFCObject.isVoid(eleOrderLine.getAttribute("OrderedQty"))
						&& !YFCObject.isVoid(eleOrderLine.getAttribute("OriginalOrderedQty"))
						&& Double.parseDouble(eleOrderLine.getAttribute("OrderedQty")) == Double
								.parseDouble(eleOrderLine.getAttribute("OriginalOrderedQty"))) ? true : false;
	}

	/**
	 * This method checks if the orderline is Partial Picked line or not message xml
	 * 
	 * @param eleOrderLine
	 * @throws Exception
	 */
	private boolean checkPartialPickedLine(Element eleOrderLine) throws Exception {
		//OMNI-50925 Start changes -cancelled Items details populated in picked up email
		String sInvoicedQty=eleOrderLine.getAttribute("InvoicedQty");
		return (Double.parseDouble(sInvoicedQty)!=0.00)
		//OMNI-50925 End changes -cancelled Items details populated in picked up email
				&& (!YFCObject.isVoid(eleOrderLine.getAttribute("OrderedQty"))
						&& !YFCObject.isVoid(eleOrderLine.getAttribute("OriginalOrderedQty"))
						&& Double.parseDouble(eleOrderLine.getAttribute("OrderedQty")) != Double
								.parseDouble(eleOrderLine.getAttribute("OriginalOrderedQty"))) ? true : false;
	}

	/**
	 * Format order date in required format(MMM DD)
	 * 
	 * @param eleIndoc
	 */
	private void setOrderPlacedDate(Document inDoc) {
		// TODO Auto-generated method stub
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
			inDoc.getDocumentElement().setAttribute("CurrentDate", strRequiredDate);

	}

	private void AddPickedUpItem(Document inDoc, Element eleOrderline) throws Exception {
		// TODO Auto-generated method stub
		Element elePickedUP = null;

		if (!YFCObject.isVoid(XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo/RFCP"))) {

			elePickedUP = (Element) XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo/RFCP");

		} else {

			elePickedUP = XMLUtil.createElement(inDoc, "RFCP", null);

			Element eleProductInfo = (Element) XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/ProductInfo");

			eleProductInfo.appendChild(elePickedUP);

		}

		String orderQty = eleOrderline.getAttribute("OrderedQty");
		String olkey = eleOrderline.getAttribute("OrderLineKey");

		String olUnitPrice = XMLUtil.getString(inDoc,

				"/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/LinePriceInfo/@UnitPrice");

		Element eleItemInfo = createItemInfo(inDoc, olkey, orderQty, olUnitPrice);

		elePickedUP.appendChild(eleItemInfo);

	}

	private Element createItemInfo(Document inDoc, String olkey, String orderQty, String olUnitPrice) throws Exception {
		// TODO Auto-generated method stub
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

		eleItemInfo.setAttribute("UnitPrice", olUnitPrice);

		return eleItemInfo;
	}

	private void sendEmail(YFSEnvironment env, Document inDoc) throws Exception {

		// send customer email

		log.verbose("Before Sending customer email :" + XMLUtil.getXMLString(inDoc));

		String customerEMailID = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID);

		if (!YFCCommon.isVoid(customerEMailID) && customerEMailID.contains(",")) {

			customerEMailID = customerEMailID.substring(0, customerEMailID.indexOf(","));

			inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID, customerEMailID);

		}

		Document emailSentOutDoc = AcademyUtil.invokeService(env,

				"AcademySendPickedUpEmailToListrak", inDoc);

		log.verbose("Sent customer email :" + XMLUtil.getXMLString(emailSentOutDoc));

		// check and send alternate email

		if (AcademyEmailUtil.isAlternamePickUpCustomerEmailExist(inDoc.getDocumentElement())) {

			AcademyEmailUtil.setAlternamePickUpCustomerEmailExist(inDoc.getDocumentElement());

			log.verbose("Before Sending Alternate pickup email :" + XMLUtil.getXMLString(inDoc));

			// emailSentOutDoc = AcademyUtil.invokeService(env,

			// AcademyConstants.ACAD_RFCP_LISTRAK_EMAIL_SERVICE, inDoc);

			emailSentOutDoc = AcademyUtil.invokeService(env, "AcademySendPickedUpEmailToListrak", inDoc);

			log.verbose("Sent Alternate pickup email :" + XMLUtil.getXMLString(emailSentOutDoc));

		}

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

	private static void addUrls(Document inDoc, YFSEnvironment env, Element eleIndoc) throws Exception {

		String strURL_ViewOrderDetails = null;

		String strZipCode = null;

		String strOrderNo = eleIndoc.getAttribute(AcademyConstants.ATTR_ORDER_NO);

		Element elePersonInfoBillTo = SCXmlUtil.getChildElement(eleIndoc, AcademyConstants.ELE_PERSON_INFO_BILL_TO);

		if (!YFCCommon.isVoid(elePersonInfoBillTo)) {

			strZipCode = elePersonInfoBillTo.getAttribute(AcademyConstants.ZIP_CODE);

		}

		// Fetching ORDERDETAILS URL from CO file

		String strViewOrderDetails = YFSSystem.getProperty(AcademyConstants.PROP_VIEW_ORDERDETAILS_URL);

		// https://uat4www.academy.com/webapp/wcs/stores/servlet/UserAccountOrderStatus?orderId=@@@@&zipCode=$$$$&langId=-1&storeId=10151&catalogId=10051&isSubmitted=true&URL=NonAjaxOrderDetail&errorViewName=GuestOrderStatusView&splitshipstatus=true&isDisplayLeftNav=false

		if (!YFCCommon.isVoid(strViewOrderDetails)) {

			strURL_ViewOrderDetails = strViewOrderDetails.replace("@@@@", strOrderNo);

			strURL_ViewOrderDetails = strURL_ViewOrderDetails.replace("$$$$", strZipCode);

			eleIndoc.setAttribute("ViewOrderDetails", strURL_ViewOrderDetails);

		}

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

	//OMNI-49424--START
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
	//OMNI-49424--END
}