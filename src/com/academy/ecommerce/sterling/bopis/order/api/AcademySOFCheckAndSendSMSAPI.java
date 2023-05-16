package com.academy.ecommerce.sterling.bopis.order.api;

import java.io.IOException;

/**#########################################################################################
*
* Project Name					: OMNI-43227
* Module						: OMNI-43227
* Author						: C0027905
* Author Group					: EVEREST
* Date							: 16-Aug-2021 
* Description	: This class publish SMS to ESB queue when SOF order is moved
* to Ready For Customer Pick Status and Order Has Delayed Lines.
* 
* ---------------------------------------------------------------------------------
* Date			Author			Version#		Remarks/Description
* ---------------------------------------------------------------------------------
* 16-Aug-2021 Everest	1.0			Updated version
* #########################################################################################*/

import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademySOFCheckAndSendSMSAPI implements YIFCustomApi {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademySOFCheckAndSendSMSAPI.class);
	private Properties props;

	public Document checkAndSendSMS(YFSEnvironment env, Document inDoc) throws Exception {

		log.verbose("Start of method checkAndSendSMS : input is :: " + XMLUtil.getXMLString(inDoc));

		Document docSMSMessage = null;
		String smsFOR = "SOF_RFCP_SMS";
		String messageType = "SOF Ready for Pickup";
		String messageId = "5904";
		String primaryPhoneNo = "";
		String alternatePhoneNo = "";
		String strMessage = "";

		Element eleIndoc = inDoc.getDocumentElement();
		String currentShipmentKey = XPathUtil.getString(eleIndoc, "/Order/@CurrentShipmentKey");
		if (YFCCommon.isVoid(currentShipmentKey)) {
			currentShipmentKey = SCXmlUtil.getXpathAttribute(eleIndoc, "Shipment/@ShipmentKey");
			String strOrderHeaderKey = SCXmlUtil.getXpathAttribute(eleIndoc,
					"Shipment/ShipmentLines/ShipmentLine/@OrderHeaderKey");
			eleIndoc = getOrderList(env, strOrderHeaderKey);
			smsFOR = "SOF_RFCP_SMS_REM";
			messageType = "SOF Ready for Pickup Reminder";
			messageId = "5904";
		}
		Document outDocGetShipmentList = getShipmentList(env, currentShipmentKey);
		Element elePersonInfoBillToOut = SCXmlUtil.getChildElement(eleIndoc, AcademyConstants.ELE_PERSON_INFO_BILL_TO);
		Element elePersonInfoMarkForOut = SCXmlUtil.getChildElement(eleIndoc,
				AcademyConstants.ELE_PERSON_INFO_MARK_FOR);

		if (!YFCCommon.isVoid(elePersonInfoBillToOut)) {
			primaryPhoneNo = elePersonInfoBillToOut.getAttribute(AcademyConstants.ATTR_DAY_PHONE);
		}
		if (!YFCCommon.isVoid(elePersonInfoMarkForOut)) {
			alternatePhoneNo = elePersonInfoMarkForOut.getAttribute(AcademyConstants.ATTR_DAY_PHONE);
		}

		if (!YFCCommon.isVoid(primaryPhoneNo) || !YFCCommon.isVoid(alternatePhoneNo)) {

			Document outDocGetCommonCodeList = getCommonCodeList(env, smsFOR);
			String strSMSMessage = getSMSMessage(outDocGetCommonCodeList, smsFOR);

			docSMSMessage = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, messageType);
			docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.MESSAGE_ID, messageId);
			String orderNo = eleIndoc.getAttribute(AcademyConstants.ATTR_ORDER_NO);
			String strAddress = getStoreAddress(outDocGetShipmentList);
			String strStorePhoneNum = XMLUtil.getAttributeFromXPath(outDocGetShipmentList,
					AcademyConstants.XPATH_SHIPMENT_STORE_MOBILE);
			log.verbose("StoreMobile :: " + strStorePhoneNum);
			strMessage = strSMSMessage.replace(AcademyConstants.ATT_$ORDER_NO, orderNo);
			strMessage = strMessage.replace(AcademyConstants.ATT_$STORE_ADDRESS, strAddress);
			if (!YFCObject.isVoid(strStorePhoneNum)) {
				strMessage = strMessage.replace(AcademyConstants.ATT_$STORE_MOBILE, strStorePhoneNum);
			} else {
				strMessage = strMessage.replace(AcademyConstants.ATT_$STORE_MOBILE, "");
			}
			docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_MESSAGE, strMessage);
			if (!YFCCommon.isVoid(alternatePhoneNo)) {
				docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_DAY_PHONE, alternatePhoneNo);
				log.verbose("alternate person SMS message : " + XMLUtil.getXMLString(docSMSMessage));
				AcademyUtil.invokeService(env, AcademyConstants.ACD_BOPIS_POST_MES_Q_SERVICE, docSMSMessage);
			}
			if (!YFCCommon.isVoid(primaryPhoneNo)) {
				docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_DAY_PHONE, primaryPhoneNo);
				log.verbose("primary customer SMS message : " + XMLUtil.getXMLString(docSMSMessage));
				AcademyUtil.invokeService(env, AcademyConstants.ACD_BOPIS_POST_MES_Q_SERVICE, docSMSMessage);
			}
		}

		return docSMSMessage;
	}

	private Element getOrderList(YFSEnvironment env, String strOrderHeaderKey) throws Exception {

		log.verbose("Begin of AcademySOFCheckAndSendSMSAPI.getOrderList() method");

		Document getOrderListInp = SCXmlUtil.createDocument("Order");
		getOrderListInp.getDocumentElement().setAttribute("OrderHeaderKey", strOrderHeaderKey);
		log.verbose("Input of  inputXML : " + XMLUtil.getXMLString(getOrderListInp));
		Document docgetOrderListOutput = null;
		// get the attributes from the inputXML
		Document docgetOrderListTemplate = XMLUtil.getDocument("<OrderList>"
				+ "<Order OrderHeaderKey=\"\" OrderNo=\"\"><PersonInfoBillTo AddressLine1=\"\" AddressLine2=\"\" AddressLine3=\"\"\r\n"
				+ "        AddressLine4=\"\" AddressLine5=\"\" AddressLine6=\"\"\r\n"
				+ "        AlternateEmailID=\"\" Beeper=\"\" City=\"\" Company=\"\" Country=\"\"\r\n"
				+ "        DayFaxNo=\"\" DayPhone=\"\" Department=\"\" EMailID=\"\" EveningFaxNo=\"\"\r\n"
				+ "        EveningPhone=\"\" FirstName=\"\" JobTitle=\"\" LastName=\"\"\r\n"
				+ "        MiddleName=\"\" MobilePhone=\"\" OtherPhone=\"\" PersonID=\"\"\r\n"
				+ "        PersonInfoKey=\"\" State=\"\" Suffix=\"\" Title=\"\" ZipCode=\"\"/>\r\n"
				+ "    <PersonInfoMarkFor AddressLine1=\"\" AddressLine2=\"\" AddressLine3=\"\"\r\n"
				+ "        AddressLine4=\"\" AddressLine5=\"\" AddressLine6=\"\"\r\n"
				+ "        AlternateEmailID=\"\" Beeper=\"\" City=\"\" Company=\"\" Country=\"\"\r\n"
				+ "        DayFaxNo=\"\" DayPhone=\"\" Department=\"\" EMailID=\"\" EveningFaxNo=\"\"\r\n"
				+ "        EveningPhone=\"\" FirstName=\"\" JobTitle=\"\" LastName=\"\"\r\n"
				+ "        MiddleName=\"\" MobilePhone=\"\" OtherPhone=\"\" PersonID=\"\"\r\n"
				+ "        PersonInfoKey=\"\" State=\"\" Suffix=\"\" Title=\"\" ZipCode=\"\"/></Order></OrderList>");

		env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, docgetOrderListTemplate);
		docgetOrderListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, getOrderListInp);
		env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);

		log.verbose("output of  getOrderListOutput XML  : " + XMLUtil.getXMLString(docgetOrderListOutput));
		return SCXmlUtil.getXpathElement(docgetOrderListOutput.getDocumentElement(), "Order");
	}

	private Document getShipmentList(YFSEnvironment env, String strShipmentKey) throws Exception {

		// getShipmentList API for PersonInfoShipTo details
		// indoc getShipmentList
		Document inDocGetShipmentList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		inDocGetShipmentList.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
		log.verbose("Input to getShipmentList" + XMLUtil.getXMLString(inDocGetShipmentList));
		// template for getShipmentList
		Document tempDocGetShipmentList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENTS);
		Element eleTempShipment = SCXmlUtil.createChild(tempDocGetShipmentList.getDocumentElement(),
				AcademyConstants.ELE_SHIPMENT);
		Element elePersonInfoShip = SCXmlUtil.createChild(eleTempShipment, AcademyConstants.ELEM_TOADDRESS);
		Element eleTempShipmentLines = SCXmlUtil.createChild(eleTempShipment, AcademyConstants.ELE_SHIPMENT_LINES);
		Element eleTempShipmentLine = SCXmlUtil.createChild(eleTempShipmentLines, AcademyConstants.ELE_SHIPMENT_LINE);

		log.verbose("template for getShipmentList" + XMLUtil.getXMLString(tempDocGetShipmentList));

		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, tempDocGetShipmentList);
		// invoking getShipmmetList
		Document outDocGetShipmentList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST,
				inDocGetShipmentList);
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);

		log.verbose("Out Doc from getShipmentList" + XMLUtil.getXMLString(outDocGetShipmentList));
		return outDocGetShipmentList;
	}

	private String getSMSMessage(Document outDocGetCommonCodeList, String smsFOR) throws Exception {
		log.verbose("Start AcademySTSCheckAndSendSMSAPI.getSMSMessage()  strSMSType ::" + smsFOR);

		String strSMSMessage = "";
		int iMsgCount = 1;
		String strMessage = "";

		do {
			strMessage = XPathUtil.getString(outDocGetCommonCodeList, "/CommonCodeList/CommonCode[@CodeValue='" + smsFOR
					+ "_Mes" + iMsgCount + "']/@CodeLongDescription");
			if (YFCObject.isVoid(strMessage)) {
				iMsgCount = 0;
			} else {
				strSMSMessage = strSMSMessage + strMessage;
				iMsgCount++;
			}
		} while (iMsgCount != 0);

		log.verbose("End AcademySTSCheckAndSendSMSAPI.getSMSMessage() Output message " + strSMSMessage);
		return strSMSMessage;
	}

	private Document getCommonCodeList(YFSEnvironment env, String strCodeType) throws Exception {

		log.verbose("Start AcademySTSCheckAndSendSMSAPI.getCommonCodeList() strCodeType :: " + strCodeType);
// TODO Auto-generated method stub
		Document inDocGetCommonCodeList = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
		inDocGetCommonCodeList.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, strCodeType);
		log.verbose("input to getCommonCodeList " + XMLUtil.getXMLString(inDocGetCommonCodeList));
		Document outDocGetCommonCodeList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMON_CODELIST,
				inDocGetCommonCodeList);
		log.verbose("End AcademySTSCheckAndSendSMSAPI.getCommonCodeList() output XML: "
				+ XMLUtil.getXMLString(outDocGetCommonCodeList));
		return outDocGetCommonCodeList;
	}

	private String getStoreAddress(Document outDocGetShipmentList) {
		log.verbose("Start AcademyBOPISCheckAndSendSMSAPI.getStoreAddress()");

		String strAddress = "";
		Element eleShip = SCXmlUtil.getChildElement(outDocGetShipmentList.getDocumentElement(),
				AcademyConstants.ELE_SHIPMENT);

		Element eleToAddress = SCXmlUtil.getChildElement(eleShip, AcademyConstants.ELEM_TOADDRESS);
		// Start : OMNI-4868 : Ready for Pick : Extra space
		// + eleToAddress.getAttribute(AcademyConstants.ATTR_FNAME) + " "
		// End : OMNI-4868 : Ready for Pick : Extra space
		strAddress = eleToAddress.getAttribute(AcademyConstants.ATTR_LNAME) + " "
				+ eleToAddress.getAttribute(AcademyConstants.ATTR_ADDRESS_LINE_1) + ", "
				+ eleToAddress.getAttribute(AcademyConstants.ATTR_CITY) + ", "
				+ eleToAddress.getAttribute(AcademyConstants.ATTR_STATE) + " "
				+ eleToAddress.getAttribute(AcademyConstants.ZIP_CODE);

		log.verbose("End AcademyBOPISCheckAndSendSMSAPI.getStoreAddress()" + strAddress);
		return strAddress;
	}

	public void setProperties(Properties props) throws Exception {
		this.props = props;

	}
}