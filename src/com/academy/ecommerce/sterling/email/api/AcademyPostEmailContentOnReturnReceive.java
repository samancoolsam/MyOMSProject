package com.academy.ecommerce.sterling.email.api;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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

/**#########################################################################################

*

* Project Name                : Fulfillment

* Module                      : Return Receive Email

* Description				  : This class generates return receive Email message xml posted to ESB queue.

*

* #########################################################################################*/
public class AcademyPostEmailContentOnReturnReceive implements YIFCustomApi{
	
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPostEmailContentOnReturnReceive.class);
	
	public void setProperties(Properties props) {
		log.verbose("setProperties :: ");
	}
	/**
	 * prepareEmailContent
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public Document prepareEmailContent(YFSEnvironment env, Document inDoc) {
		log.verbose("AcademyPostEmailContentOnReturnReceive.prepareEmailContent()_InXML:" + XMLUtil.getXMLString(inDoc));
		try {
			updateEMailTemplateForReturnReceive(env, inDoc);
			sendEmail(env, inDoc);
		} catch (Exception e) {
			e.printStackTrace();
			log.verbose("Exception at AcademyPostEmailContentOnReturnReceive.prepareEmailContent " + "with input:\n"
					+ XMLUtil.getXMLString(inDoc));
			log.info(" Error Trace :: " + e.toString());
		}		
		log.verbose("AcademyPostEmailContentOnReturnReceive.prepareEmailContent()_returnDoc:" + XMLUtil.getXMLString(inDoc));
		return inDoc;
	}
	
	/**
	 * updateEMailTemplateForReturnReceive
	 * @param env
	 * @param inDoc
	 */
	public void updateEMailTemplateForReturnReceive(YFSEnvironment env, Document inDoc) {
		try {
			log.verbose("Inside updateEmailTemplateForReturnInitiation Method");
			Element eleIndoc = inDoc.getDocumentElement();
			NodeList nlOrderLines = XPathUtil.getNodeList(eleIndoc, "/Order/OrderLines/OrderLine");
			Element eleOrderlineOne = (Element) nlOrderLines.item(0);
			//OMNI-77190 - Updating the path for PaymentMethods - start
			NodeList nlPaymentMethods = XPathUtil.getNodeList(eleOrderlineOne,
					"DerivedFromOrder/PaymentMethods/PaymentMethod");
			//OMNI-77190 - Updating the path for PaymentMethods - end
			int length = nlPaymentMethods.getLength();
			for (int i = 0; i < length; i++) {
				Element elePaymentMethod = (Element) nlPaymentMethods.item(i);
				String strCreditCardType = elePaymentMethod.getAttribute(AcademyConstants.ATTR_CREDIT_CARD_TYPE);
				String strPaymentType = elePaymentMethod.getAttribute(AcademyConstants.ATTR_PAYMENT_TYPE);
				if (!YFCObject.isNull(strPaymentType) && (AcademyConstants.PAYPAL.equalsIgnoreCase(strPaymentType)
						|| AcademyConstants.STR_PLCC_PAYMENT.equalsIgnoreCase(strPaymentType)
						|| AcademyConstants.KLARNA_PAY_TYP.equalsIgnoreCase(strPaymentType))) {
					strCreditCardType = strPaymentType;
					log.verbose("strCreditCardType :: " + strCreditCardType);
				}

				String strCommonCodeCCType = getCommonCodeForCrediCardType(env, strCreditCardType);
				log.verbose(
						"AcademyPostEmailContentOnReturnInitiation-updateEmailTemplateForReturnInitiation - strCommonCodeCCType :: "
								+ strCommonCodeCCType);
				elePaymentMethod.setAttribute("CreditCardType", strCommonCodeCCType);
			}
			
			int olLength= nlOrderLines.getLength();
			for (int i = 0; i < olLength; i++) {
				Element eleOrderline = (Element) nlOrderLines.item(i);
				createProductInfoElement(inDoc);
				addReturnItem(inDoc, eleOrderline);
			}
			// Update Order Level Attributes
			AcademyEmailUtil.updateMessageRef(env, eleIndoc, AcademyConstants.RETURN_RECEIVE_MSG_ID_PROD,
					AcademyConstants.RETURN_RECEIVE_MSG_ID_STAGE, AcademyConstants.RETURN_RECEIVE_MSG_TYPE);
			addUrls(inDoc, eleIndoc);

			Element eleExtn = SCXmlUtil.getChildElement(eleIndoc, AcademyConstants.ELE_EXTN);
			String strExtnTrackingno = null;
			if (!YFCObject.isVoid(eleExtn)) {
				strExtnTrackingno = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_TRACKING_NO);
				if (!YFCCommon.isVoid(strExtnTrackingno)) {
					String strReturnURL = YFSSystem.getProperty(AcademyConstants.PROP_RETURN_TRACKING_URL);
					if (!YFCCommon.isVoid(strReturnURL)) {
						String strEmailTrackingURL = strReturnURL + strExtnTrackingno;
						eleIndoc.setAttribute(AcademyConstants.EMAIL_TRACKING_NO, strExtnTrackingno);
						eleIndoc.setAttribute(AcademyConstants.EMAIL_TRACKING_URL, strEmailTrackingURL);
					}
				}
			}
			eleIndoc.setAttribute(AcademyConstants.ATTR_CUST_NAME, AcademyEmailUtil.getBillToCustomerName(eleIndoc));
			eleIndoc.setAttribute(AcademyConstants.ATTR_DAY_PHONE, AcademyEmailUtil.getDayPhone(eleIndoc));
			log.verbose("End of updateEmailTemplateForReturnInitiation Method");
		} catch (Exception ex) {
			ex.printStackTrace();
			log.verbose("Exception in updateEmailTemplateForReturnInitiation(): " + XMLUtil.getXMLString(inDoc));
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
	 * @param eleIndoc
	 * 
	 * @throws Exception
	 * 
	 */
	private static void addUrls(Document inDoc, Element eleIndoc) {
		String strViewOrderDetailsURL = null;
		String strZipCode = null;
		try {
			//OMNI-77190 - Updating the ViewOrderDetails URL
			String strOrderNo = XMLUtil.getString(inDoc, "//DerivedFromOrder/@OrderNo");

			Element elePersonInfoBillTo = SCXmlUtil.getChildElement(eleIndoc, AcademyConstants.ELE_PERSON_INFO_BILL_TO);
			if (!YFCCommon.isVoid(elePersonInfoBillTo)) {
				strZipCode = elePersonInfoBillTo.getAttribute(AcademyConstants.ZIP_CODE);
			}
			// Fetching ORDERDETAILS URL from COP file
			String strViewOrderDetails = YFSSystem.getProperty(AcademyConstants.PROP_VIEW_ORDERDETAILS_URL);
			// https://uat4www.academy.com/webapp/wcs/stores/servlet/UserAccountOrderStatus?orderId=@@@@&zipCode=$$$$&langId=-1&storeId=10151&catalogId=10051&isSubmitted=true&URL=NonAjaxOrderDetail&errorViewName=GuestOrderStatusView&splitshipstatus=true&isDisplayLeftNav=false
			if (!YFCCommon.isVoid(strViewOrderDetails)) {
				strViewOrderDetailsURL = strViewOrderDetails.replace("@@@@", strOrderNo);
				strViewOrderDetailsURL = strViewOrderDetailsURL.replace("$$$$", strZipCode);
				eleIndoc.setAttribute(AcademyConstants.VIEW_ORDER_DETAILS, strViewOrderDetailsURL);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.verbose("Exception in addUrls(): " + XMLUtil.getXMLString(inDoc));
		}
	}
	
	/**
	 * This method will add the returned item information to the output email messsage xml
	 * @param inDoc
	 * @param eleOrderLine
	 * @throws Exception
	 */
	private void addReturnItem(Document inDoc, Element eleOrderLine) {
		Element eleReturn = null;
		try {
			if (!YFCObject
					.isVoid(XPathUtil.getNode(inDoc.getDocumentElement(), AcademyConstants.RETURN_PRODUCT_INFO))) {
				eleReturn = (Element) XPathUtil.getNode(inDoc.getDocumentElement(), AcademyConstants.RETURN_PRODUCT_INFO);
			} else {
				eleReturn = XMLUtil.createElement(inDoc, AcademyConstants.STR_RETURN, null);
				Element eleProductInfo = (Element) XPathUtil.getNode(inDoc.getDocumentElement(),
						AcademyConstants.ORDER_PRODUCT_INFO);
				eleProductInfo.appendChild(eleReturn);
			}
			String strOrderQty = eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDERED_QTY);
			addItemInfo(inDoc, eleOrderLine, strOrderQty, eleReturn);
		} catch (Exception ex) {
			ex.printStackTrace();
			log.verbose("Exception in addReturnItem(): " + XMLUtil.getXMLString(inDoc));
		}
	}

	/**
	 * This method will add the returned item price information to the output email messsage xml
	 * @param inDoc
	 * @param eleOrderLine
	 * @param orderQty
	 * @param eleToAdd
	 * @throws Exception
	 */
	private void addItemInfo(Document inDoc, Element eleOrderLine, String orderQty, Element eleToAdd) throws Exception {
		try {
			String olkey = eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
			String olUnitPrice = XMLUtil.getString(inDoc,
					"/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/LineOverallTotals/@UnitPrice");
			Element eleItemInfo = createItemInfo(inDoc, olkey, orderQty, olUnitPrice);
			eleToAdd.appendChild(eleItemInfo);
		} catch (Exception ex) {
			ex.printStackTrace();
			log.verbose("Exception in addItemInfo(): " + XMLUtil.getXMLString(inDoc));
		}
	}

	/**
	 * This method creates ProductInfo elements and appends it to the output email
	 * message xml
	 * 
	 * @param inDoc
	 * @throws Exception
	 */
	private void createProductInfoElement(Document inDoc) throws Exception {
		if (YFCObject.isVoid(XPathUtil.getNode(inDoc.getDocumentElement(), AcademyConstants.ORDER_PRODUCT_INFO))) {
			Element eleProductInfo = XMLUtil.createElement(inDoc, AcademyConstants.PRODUCT_INFO, null);
			inDoc.getDocumentElement().appendChild(eleProductInfo);
		}
	}
	
	/**
	 * This method creates itemInfo and appends it to the output email
	 * message xml
	 * 
	 * @param inDoc
	 * @param olKey
	 * @param orderQty
	 * @param olUnitPrice
	 * @throws Exception
	 */

	private Element createItemInfo(Document inDoc, String olkey, String orderQty, String olUnitPrice) throws Exception {
		Element eleItemInfo = XMLUtil.createElement(inDoc, AcademyConstants.ITEM_INFO, null);
		eleItemInfo.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, olkey);
		eleItemInfo.setAttribute(AcademyConstants.ITEM_ID, XMLUtil.getString(inDoc,
				"/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/ItemDetails/@ItemID"));
		eleItemInfo.setAttribute(AcademyConstants.ATTR_DESC,
				XMLUtil.getString(inDoc, "/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey
						+ "']/ItemDetails/PrimaryInformation/@Description"));
		eleItemInfo.setAttribute(AcademyConstants.ATTR_ORDER_QTY, orderQty);
		eleItemInfo.setAttribute(AcademyConstants.IMAGE_LOC,
				XMLUtil.getString(inDoc, "/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey
						+ "']/ItemDetails/PrimaryInformation/@ImageLocation"));
		eleItemInfo.setAttribute(AcademyConstants.IMAGE_ID, XMLUtil.getString(inDoc,
				"/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/ItemDetails/PrimaryInformation/@ImageID"));
		eleItemInfo.setAttribute(AcademyConstants.ATTR_LINE_TOTAL, XMLUtil.getString(inDoc,
				"/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/LineOverallTotals/@LineTotal"));
		eleItemInfo.setAttribute(AcademyConstants.ATTR_UNIT_PRICE, olUnitPrice);
		return eleItemInfo;
	}

	/**
	 * This method will invoke a service to post email payload into the queue
	 * message xml
	 * @param env
	 * @param inDoc
	 */
	private void sendEmail(YFSEnvironment env, Document inDoc) {
		log.verbose("Before Sending email :" + XMLUtil.getXMLString(inDoc));
		Document emailSentOutDoc = null;
		try {
			String customerEMailID = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID);
			if (!YFCCommon.isVoid(customerEMailID) && customerEMailID.contains(",")) {
				customerEMailID = customerEMailID.substring(0, customerEMailID.indexOf(","));
				inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID, customerEMailID);
			}
			emailSentOutDoc = AcademyUtil.invokeService(env, AcademyConstants.ACAD_RETURN_RECEIVE_LISTRAK_EMAIL,
					inDoc);
		} catch (Exception x) {
			x.printStackTrace();
			log.verbose(
					"Exception at AcademyPostEmailContentOnReturnInitiation.sendEmail " + XMLUtil.getXMLString(inDoc));
		}
		log.verbose("Sent customer email :" + XMLUtil.getXMLString(emailSentOutDoc));
	}

	/**
	 * This method return the common code short description value.
	 * 
	 * @param env
	 * @param strCreditCardType
	 * @return
	 */
	/**
	 * inDoc: <CommonCode CodeType="PACK_SLIP_PAY_TYPE" CodeValue="MasterCard"/>
	 * 
	 * outDoc: <CommonCodeList>
	 * <CommonCode CodeLongDescription="MASTER" CodeShortDescription="MASTER"
	 * CodeType="PACK_SLIP_PAY_TYPE" CodeValue="MasterCard"/> </CommonCodeList>
	 *
	 */

	private String getCommonCodeForCrediCardType(YFSEnvironment env, String strCreditCardType) {
		log.verbose("AcademyPostEmailContentOnReturnInitiation-getCommonCodeForCrediCardType :: " + strCreditCardType);
		Document inDocGetCommonCodeList = null;
		Document outDocGetCommonCodeList = null;
		String strCommonCodeCCType = "";
		try {
			inDocGetCommonCodeList = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
			inDocGetCommonCodeList.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE,
					AcademyConstants.STR_PACK_SLIP_PAY_TYPE);
			inDocGetCommonCodeList.getDocumentElement().setAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE,
					strCreditCardType);
			outDocGetCommonCodeList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMON_CODELIST,
					inDocGetCommonCodeList);
			log.verbose(
					"AcademyPostEmailContentOnReturnInitiation-getCommonCodeForCrediCardType - outDocGetCommonCodeList :: "
							+ XMLUtil.getXMLString(outDocGetCommonCodeList));
			if (!YFCObject.isVoid(outDocGetCommonCodeList) && outDocGetCommonCodeList.getDocumentElement()
					.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE).getLength() > 0) {
				Element eleCommonCode = XMLUtil.getElementByXPath(outDocGetCommonCodeList,
						AcademyConstants.XPATH_COMMON_CODE_ELE);
				strCommonCodeCCType = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
				log.verbose(
						"AcademyPostEmailContentOnReturnInitiation-getCommonCodeForCrediCardType - strCommonCodeCCType :: "
								+ strCommonCodeCCType);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strCommonCodeCCType;
	}
}