package com.academy.ecommerce.sterling.bopis.order.api;

/**#########################################################################################
*
* Project Name					: OMNI-41165,OMNI-41165
* Module						: OMNI-41165
* Author						: C0027905
* Author Group					: EVEREST
* Date							: 23-Jul-2021 
* Description	: This class publish SMS to ESB queue when STH email send.
* 
* ---------------------------------------------------------------------------------
* Date			Author			Version#		Remarks/Description
* ---------------------------------------------------------------------------------
* 23-jul-2021 Everest	1.0			Updated version
* #########################################################################################*/

import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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

public class AcademySTHCheckAndSendSMSAPI implements YIFCustomApi {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademySTHCheckAndSendSMSAPI.class);
	private Properties props;

	public Document checkAndSendSMS(YFSEnvironment env, Document inDoc) throws Exception {

		log.verbose("Start of method checkAndSendSMS : input is :: " + XMLUtil.getXMLString(inDoc));

		Document docSMSMessage = null;
		String smsFOR = "SHIP_TO_HOME_SMS";
		String messageType = "ShiptoHome";
		String primaryPhoneNo = "";
		String alternatePhoneNo = "";

		Element eleIndoc = inDoc.getDocumentElement();
		String strURL_ViewOrderDetails = null;

		String strZipCode = null;

		String strOrderNo = eleIndoc.getAttribute(AcademyConstants.ATTR_ORDER_NO);
		String strTemplateType = eleIndoc.getAttribute(AcademyConstants.ATTR_EMAIL_TEMPLATE_TYPE);
		String strCurrentShipment = eleIndoc.getAttribute("CurrentShipmentKey");
		String strCurrentShipmentTrackingNo = SCXmlUtil.getXpathAttribute(eleIndoc,
				"Shipments/Shipment[@ShipmentKey='" + strCurrentShipment + "']/Containers/Container/@TrackingNo");
		// Code Changes for OMNI-48852
		String sProNo = "";
		String sShipmentNo = "";
		String strURL_help = YFSSystem.getProperty("URL_Help_Academy");
		// Code Changes for OMNI-41737
		if (YFCCommon.isVoid(strCurrentShipment) &&  !(AcademyConstants.STR_PACK.equals(strTemplateType))) {
			smsFOR = "PT_SHIP_TO_HOME_SMS";
			messageType = "PartialShiptoHome";
		} else {
			if (YFCCommon.isVoid(strCurrentShipmentTrackingNo)) {
				// CEVA carrier
				sProNo = SCXmlUtil.getXpathAttribute(eleIndoc,
						"Shipments/Shipment[@ShipmentKey='" + strCurrentShipment + "']/@ProNo");
				if (!YFCCommon.isVoid(sProNo)) {
					strCurrentShipmentTrackingNo = sProNo;
				} else {
					// EFW carrier
					sShipmentNo = SCXmlUtil.getXpathAttribute(eleIndoc,
							"Shipments/Shipment[@ShipmentKey='" + strCurrentShipment + "']/@ShipmentNo");
					strCurrentShipmentTrackingNo = sShipmentNo;
				}
			}
			// Code Changes for OMNI-48852
			Element elePersonInfoBillTo = SCXmlUtil.getChildElement(eleIndoc, AcademyConstants.ELE_PERSON_INFO_BILL_TO);

			if (!YFCCommon.isVoid(elePersonInfoBillTo)) {

				strZipCode = elePersonInfoBillTo.getAttribute(AcademyConstants.ZIP_CODE);

			}

			String strViewOrderDetails = YFSSystem.getProperty(AcademyConstants.PROP_VIEW_ORDERDETAILS_URL);

			if (!YFCCommon.isVoid(strViewOrderDetails)) {

				strURL_ViewOrderDetails = strViewOrderDetails.replace("@@@@", strOrderNo);

				strURL_ViewOrderDetails = strURL_ViewOrderDetails.replace("$$$$", strZipCode);
			}
		}
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

			docSMSMessage = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, messageType);
			String orderNo = eleIndoc.getAttribute(AcademyConstants.ATTR_ORDER_NO);
			if(!YFCCommon.isVoid(strTemplateType) && AcademyConstants.STR_PACK.equals(strTemplateType)) {
				String strMessage = YFSSystem.getProperty(AcademyConstants.PROP_PACKED_SMS);
                preparePackedMessage(docSMSMessage,strMessage,strOrderNo,strURL_help,strURL_ViewOrderDetails);
			} 
			else {
			Document outDocGetCommonCodeList = getCommonCodeList(env, smsFOR);
			String strMessage = getSMSMessage(outDocGetCommonCodeList, smsFOR);
			strMessage = strMessage.replace(AcademyConstants.ATT_$ORDER_NO, orderNo);
			strMessage = strMessage.replace("$Academy_Support_Link", strURL_help);
			// Code Changes for OMNI-41737
			if (!YFCCommon.isVoid(strCurrentShipment)) {
				strMessage = strMessage.replace("$Order_Details_Link", strURL_ViewOrderDetails + ".");
				strMessage = strMessage.replace("$Tracking_No", strCurrentShipmentTrackingNo + ".");
			}
			strMessage = strMessage.replace("#", "");
			docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_MESSAGE, strMessage);
			// End OMNI-5398,5399
			}
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
    
	private void preparePackedMessage(Document docSMSMessage,String strMessage,String orderNo,
			String strURL_help,String strURL_ViewOrderDetails) {
		strMessage = strMessage.replace(AcademyConstants.ATT_$ORDER_NO, orderNo);
		strMessage = strMessage.replace("$Academy_Support_Link", strURL_help);
		strMessage = strMessage.replace("$Order_Details_Link", strURL_ViewOrderDetails);
		docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_MESSAGE, strMessage);		
	}
	
	public void setProperties(Properties props) throws Exception {
		this.props = props;

	}
}