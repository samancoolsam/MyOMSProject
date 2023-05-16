package com.academy.ecommerce.sterling.carrierupdates;

/**#########################################################################################
 *
 * Project Name					: OMNI-41165,OMNI-41165
 * Module						: OMNI-41165
 * Author						: C0027905
 * Author Group					: EVEREST
 * Date							: 27-APR-2023 
 * Description	: This class publish SMS to ESB queue when convey update sent.
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

public class AcademyConveyCheckAndSendSMSAPI implements YIFCustomApi {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyConveyCheckAndSendSMSAPI.class);
	private Properties props;

	public Document checkAndSendSMS(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer("AcademyConveyCheckAndSendSMSAPI::checkAndSendSMS");
		log.verbose("Start of method checkAndSendSMS : input is :: " + XMLUtil.getXMLString(inDoc));

		Document docSMSMessage = null;
		String strSMSCodeType = null ;
		String strMessageType = null;
		String strPrimaryPhoneNo = null;
		String strAlternatePhoneNo = null;

		Element eleIndoc = SCXmlUtil.getXpathElement(inDoc.getDocumentElement(), "/Shipment");
		String strURL_ViewOrderDetails = null;
		String strZipCode = null;

		String strOrderNo = eleIndoc.getAttribute(AcademyConstants.ATTR_ORDER_NO);
		String strInMessage = eleIndoc.getAttribute(AcademyConstants.ATTR_MESSAGE);
		String strCurrentShipment = eleIndoc.getAttribute(AcademyConstants.SHIPMENT_KEY);
		String strSCAC = eleIndoc.getAttribute(AcademyConstants.ATTR_SCAC);
		String strCurrentShipmentTrackingNo = eleIndoc.getAttribute("TrackingNoForEmail");
		// Code Changes for OMNI-48852
		String sProNo = "";
		String sShipmentNo = "";
		String strURL_help = props.getProperty("URL_Help_Academy");
		Element elePersonInfoBillTo = SCXmlUtil.getXpathElement(eleIndoc,
				"ShipmentLines/ShipmentLine/OrderLine/Order/PersonInfoBillTo");
		Element elePersonInfoMarkFor = SCXmlUtil.getXpathElement(eleIndoc,
				"ShipmentLines/ShipmentLine/OrderLine/Order/PersonInfoMarkFor");

		//Determining the code type and message type based on Partial or Complete
		if ("PARTIAL".equals(strInMessage)) {
			strSMSCodeType = props.getProperty("PARTIAL_CODE_TYPE");
			strMessageType = props.getProperty("PARTIAL_MESSAGE_TYPE");
		}
		else {
			strSMSCodeType = props.getProperty("COMPLETE_CODE_TYPE");
			strMessageType = props.getProperty("COMPLETE_MESSAGE_TYPE");
		}


		//Retrieving the Tracking Information
		if (YFCCommon.isVoid(strCurrentShipmentTrackingNo)) {
			sProNo = SCXmlUtil.getXpathAttribute(eleIndoc,
					"Shipment[@ShipmentKey='" + strCurrentShipment + "']/@ProNo");
			if (!YFCCommon.isVoid(sProNo)) {
				strCurrentShipmentTrackingNo = sProNo;
			} else {
				// EFW carrier
				sShipmentNo = eleIndoc.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
				strCurrentShipmentTrackingNo = sShipmentNo;
			}
		}

		//Retrieving the Zipcode based on Bill Information
		if (!YFCCommon.isVoid(elePersonInfoBillTo)) {
			strZipCode = elePersonInfoBillTo.getAttribute(AcademyConstants.ZIP_CODE);
		}

		//Updating the link for Order Details
		  String strViewOrderDetails = YFSSystem.getProperty(AcademyConstants.PROP_VIEW_ORDERDETAILS_URL);
		//String strViewOrderDetails = YFSSystem.getProperty("URL_ShipmentTracking_Convey");

		if (!YFCCommon.isVoid(strViewOrderDetails)) {
			//strURL_ViewOrderDetails = strViewOrderDetails.replace("$currTrackingNo", strCurrentShipmentTrackingNo);
			//strURL_ViewOrderDetails = strURL_ViewOrderDetails.replace("$scac", strSCAC);
			strURL_ViewOrderDetails = strViewOrderDetails.replace("@@@@", strOrderNo);
			strURL_ViewOrderDetails = strURL_ViewOrderDetails.replace("$$$$", strZipCode);
		}

		//Retrieving the Mobile Info
		strPrimaryPhoneNo = elePersonInfoBillTo.getAttribute(AcademyConstants.ATTR_DAY_PHONE);
		if (!YFCCommon.isVoid(elePersonInfoMarkFor)) {
			strAlternatePhoneNo = elePersonInfoMarkFor.getAttribute(AcademyConstants.ATTR_DAY_PHONE);
		}

		//Prepare the message to be sent
		if (!YFCCommon.isVoid(strPrimaryPhoneNo) || !YFCCommon.isVoid(strAlternatePhoneNo)) {

			Document outDocGetCommonCodeList = getCommonCodeList(env, strSMSCodeType);
			String strMessage = getSMSMessage(outDocGetCommonCodeList, strSMSCodeType);

			docSMSMessage = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, strMessageType);
			String orderNo = eleIndoc.getAttribute(AcademyConstants.ATTR_ORDER_NO);
			strMessage = strMessage.replace(AcademyConstants.ATT_$ORDER_NO, orderNo);
			strMessage = strMessage.replace("$Academy_Support_Link", strURL_help);
			// Code Changes for OMNI-41737
			if (!YFCCommon.isVoid(strCurrentShipment)) {
				strMessage = strMessage.replace("$Order_Details_Link", strURL_ViewOrderDetails + ".");
				strMessage = strMessage.replace("$Tracking_No", strCurrentShipmentTrackingNo + ".");
			}
			
			//Replacing extra characters
			strMessage = strMessage.replace(AcademyConstants.SPL_CHAR_HASH, "");
			docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_MESSAGE, strMessage);
			// End OMNI-5398,5399
			
			//Send the SMS to PRimary and Alternate Person Info
			if (!YFCCommon.isVoid(strAlternatePhoneNo)) {
				docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_DAY_PHONE, strAlternatePhoneNo);
				log.verbose("alternate person SMS message : " + XMLUtil.getXMLString(docSMSMessage));
				AcademyUtil.invokeService(env, AcademyConstants.ACD_BOPIS_POST_MES_Q_SERVICE, docSMSMessage);
			}
			if (!YFCCommon.isVoid(strPrimaryPhoneNo)) {
				docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_DAY_PHONE, strPrimaryPhoneNo);
				log.verbose("primary customer SMS message : " + XMLUtil.getXMLString(docSMSMessage));
				AcademyUtil.invokeService(env, AcademyConstants.ACD_BOPIS_POST_MES_Q_SERVICE, docSMSMessage);
			}
		}

		log.endTimer("AcademyConveyCheckAndSendSMSAPI::checkAndSendSMS");
		return docSMSMessage;
	}

	
	/**
	 * This method will fetch the common code information and format the message accordingly
	 * 
	 * @param outDocGetCommonCodeList
	 * @param strSMSCodeType
	 * @return String
	 */
	private String getSMSMessage(Document outDocGetCommonCodeList, String strSMSCodeType) throws Exception {
		log.beginTimer("AcademyConveyCheckAndSendSMSAPI::getSMSMessage");
		log.verbose("Start AcademySTSCheckAndSendSMSAPI.getSMSMessage()  strSMSType ::" + strSMSCodeType);

		String strSMSMessage = "";
		int iMsgCount = 1;
		String strMessage = "";
		
		//Preparing the message based on the data from the common code
		do {
			strMessage = XPathUtil.getString(outDocGetCommonCodeList, "/CommonCodeList/CommonCode[@CodeValue='" + strSMSCodeType
					+ "_Msg" + iMsgCount + "']/@CodeLongDescription");
			if (YFCObject.isVoid(strMessage)) {
				iMsgCount = 0;
			} else {
				strSMSMessage = strSMSMessage + strMessage;
				iMsgCount++;
			}
		} while (iMsgCount != 0);

		log.verbose("End AcademySTSCheckAndSendSMSAPI.getSMSMessage() Output message " + strSMSMessage);
		log.endTimer("AcademyConveyCheckAndSendSMSAPI::getSMSMessage");
		return strSMSMessage;
	}

	/**
	 * This method will fetch the common code based on the CodeType
	 * 
	 * @param env
	 * @param strCodeType
	 * @return Document
	 */
	private Document getCommonCodeList(YFSEnvironment env, String strCodeType) throws Exception {
		log.beginTimer("AcademyConveyCheckAndSendSMSAPI::getCommonCodeList");
		log.verbose("Start AcademySTSCheckAndSendSMSAPI.getCommonCodeList() strCodeType :: " + strCodeType);
		// TODO Auto-generated method stub
		Document inDocGetCommonCodeList = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
		inDocGetCommonCodeList.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, strCodeType);
		log.verbose("input to getCommonCodeList " + XMLUtil.getXMLString(inDocGetCommonCodeList));
		Document outDocGetCommonCodeList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMON_CODELIST,
				inDocGetCommonCodeList);
		log.verbose("End AcademySTSCheckAndSendSMSAPI.getCommonCodeList() output XML: "
				+ XMLUtil.getXMLString(outDocGetCommonCodeList));
		
		log.endTimer("AcademyConveyCheckAndSendSMSAPI::getCommonCodeList");
		return outDocGetCommonCodeList;
	}

	
	public void setProperties(Properties props) throws Exception {
		this.props = props;

	}
}