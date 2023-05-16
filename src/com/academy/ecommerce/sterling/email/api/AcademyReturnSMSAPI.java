package com.academy.ecommerce.sterling.email.api;

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
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyReturnSMSAPI implements YIFCustomApi {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyReturnSMSAPI.class);
	private Properties props;
	
	public Document prepareSMSContent(YFSEnvironment env, Document inDoc) {
		Document docSMSMessage = null;
		Element elePersonInfoBillTo = null;
		String primaryPhoneNo = "";
		String firstName = "";		
		String orderNo = "";
		String strSMS = "";
		try {
			
			String strSMSCodeType = props.getProperty(AcademyConstants.RETURN_SMS);
			String strMessageType = props.getProperty(AcademyConstants.RETURN_SMS_MSG_TYPE);
			String strMessage = getSMSMessage(env, strSMSCodeType);
			elePersonInfoBillTo = SCXmlUtil.getChildElement(inDoc.getDocumentElement(),
					AcademyConstants.ELE_PERSON_INFO_BILL_TO);
			
			if (!YFCCommon.isVoid(elePersonInfoBillTo)) {
				firstName = elePersonInfoBillTo.getAttribute(AcademyConstants.ATTR_FNAME);
				primaryPhoneNo = elePersonInfoBillTo.getAttribute(AcademyConstants.ATTR_DAY_PHONE);
			}
			orderNo = XMLUtil.getAttributeFromXPath(inDoc,
					AcademyConstants.XPATH_DERIVED_FROM_ORDER_NO);
			strSMS = strMessage.replace(AcademyConstants.ATT_$ORDER_NO, orderNo);
			//Return Initiation - Replace with First Name of Customer
			if (AcademyConstants.RETURN_INITIATION_SMS.equalsIgnoreCase(strSMSCodeType)) {
				strSMS = strSMS.replace(AcademyConstants.ATT_$FIRST_NAME, firstName);
			}						
			
			docSMSMessage = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, 
					strMessageType);
			docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_MESSAGE, strSMS);
			docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_DAY_PHONE, primaryPhoneNo);
			log.verbose("Customer SMS message : " + XMLUtil.getXMLString(docSMSMessage));
		} catch (Exception e) {
			e.printStackTrace();
			log.verbose("Exception in AcademyReturnSMSAPI.prepareSMSContent()");
		}
		return docSMSMessage;
	}
	
	private String getSMSMessage(YFSEnvironment env, String strCodeType) {
		log.verbose("Start AcademyReturnSMSAPI.getSMSMessage()  strSMSType :: " + strCodeType);		
		int iMsgCount = 1;
		String strMessage = "";
		StringBuilder strSMSMessage = new StringBuilder();
		try {
			
			log.verbose("Start AcademyReturnSMSAPI.getCommonCodeList() strCodeType :: " + strCodeType);
			Document inDocGetCommonCodeList = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
			inDocGetCommonCodeList.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, strCodeType);
			log.verbose("input to getCommonCodeList " + XMLUtil.getXMLString(inDocGetCommonCodeList));
			Document outDocGetCommonCodeList = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_COMMON_CODELIST, inDocGetCommonCodeList);
			log.verbose("End AcademyReturnSMSAPI.getCommonCodeList() output XML: " + XMLUtil.getXMLString(outDocGetCommonCodeList));
			do {
				strMessage = XPathUtil.getString(outDocGetCommonCodeList, "/CommonCodeList/CommonCode[@CodeValue='" 
						+ strCodeType + "_Msg" + iMsgCount + "']/@CodeLongDescription");
				if(YFCObject.isVoid(strMessage)) {
					iMsgCount = 0;
				} else {
					strSMSMessage.append(strMessage);
					iMsgCount++;
				}
			}
			while (iMsgCount !=0);
		} catch (Exception x) {
			x.printStackTrace();
			log.verbose("Exception in AcademyReturnSMSAPI.prepareSMSContent()");
		}		
		log.verbose("End AcademyReturnSMSAPI.getSMSMessage() Output message " + strSMSMessage);
		return strSMSMessage.toString();
	}
	
	public void setProperties(Properties props) {
		this.props = props;
	}

}