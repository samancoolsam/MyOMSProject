package com.academy.ecommerce.sterling.bopis.order.api;

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

/*This class publish the SMS to ESB queue when order is moved
* to Assembly In Progress Status */

public class AcademyAssemblyInProgressCheckAndSendSMSAPI implements YIFCustomApi {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyAssemblyInProgressCheckAndSendSMSAPI.class);
	private Properties props;

	/**
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	 //This Methos is used to send SMS message to ESB Queue. 
	public Document checkAndSendSMS(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer("AcademyAssemblyInProgressCheckAndSendSMSAPI.checkAndSendSMS");
		log.verbose("Start of method checkAndSendSMS : input is :: " + XMLUtil.getXMLString(inDoc));

		Document docSMSMessage = null;
		String strSmsFOR = AcademyConstants.STR_MESSAGE_TYPE_FOR_ASSEMBLY_IN_PROGRESS;
		String strMessageType = AcademyConstants.STR_MESSAGE_TYPE_FOR_ASSEMBLY_IN_PROGRESS;
		String strPrimaryPhoneNo = "";
		String strMessage = "";

		Element eleIndoc = inDoc.getDocumentElement();
		String strStatus = eleIndoc.getAttribute(AcademyConstants.ATTR_STATUS);
		Element elePersonInfoBillToOut = SCXmlUtil.getChildElement(eleIndoc, AcademyConstants.ELE_BILL_TO_ADDRESS);
		if (!YFCCommon.isVoid(elePersonInfoBillToOut)) {
			strPrimaryPhoneNo = elePersonInfoBillToOut.getAttribute(AcademyConstants.ATTR_DAY_PHONE);
		}
		if (strStatus.equals(AcademyConstants.STR_ASSEMBLY_IN_PROGRESS_STATUS)
				&& !YFCCommon.isVoid(strPrimaryPhoneNo)) {
			Document outDocGetCommonCodeList = getCommonCodeList(env, strSmsFOR);
			String strSMSMessage = getSMSMessage(outDocGetCommonCodeList, strSmsFOR);
			docSMSMessage = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, strMessageType);
			String orderNo = eleIndoc.getAttribute(AcademyConstants.ATTR_ORDER_NO);
			strMessage = strSMSMessage.replace(AcademyConstants.ATT_$ORDER_NO, orderNo);
			strMessage = strMessage.replace("$$", " ");
			docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_MESSAGE, strMessage);
			docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_DAY_PHONE, strPrimaryPhoneNo);
			log.verbose("primary customer SMS message : " + XMLUtil.getXMLString(docSMSMessage));
			AcademyUtil.invokeService(env, AcademyConstants.ACD_BOPIS_POST_MES_Q_SERVICE, docSMSMessage);
		}
		log.endTimer("AcademyAssemblyInProgressCheckAndSendSMSAPI.checkAndSendSMS");
		return docSMSMessage;
	}
	//This method is used to prepare the SMS Message from the common code list output document. 
	/**
	 * @param outDocGetCommonCodeList
	 * @param smsFOR
	 * @return
	 * @throws Exception
	 */
	private String getSMSMessage(Document outDocGetCommonCodeList, String smsFOR) throws Exception {
		log.beginTimer("AcademyAssemblyInProgressCheckAndSendSMSAPI.getSMSMessage");
		log.verbose("Start AcademyAssemblyInProgressCheckAndSendSMSAPI.getSMSMessage()  strSMSType ::" + smsFOR);

		String strSMSMessage = "";
		int iMsgCount = 1;
		String strMessage = "";

		do {
			strMessage = XPathUtil.getString(outDocGetCommonCodeList, "/CommonCodeList/CommonCode[@CodeValue='" + smsFOR
					+ "_Mes" + iMsgCount + "']/@CodeLongDescription");
			log.verbose("strMessage" + strMessage);
			if (YFCObject.isVoid(strMessage)) {
				iMsgCount = 0;
			} else {
				strSMSMessage = strSMSMessage + strMessage;
				iMsgCount++;
			}
		} while (iMsgCount != 0);

		log.verbose("End AcademyAssemblyInProgressCheckAndSendSMSAPI.getSMSMessage() Output message " + strSMSMessage);
		log.endTimer("AcademyAssemblyInProgressCheckAndSendSMSAPI.getSMSMessage");
		return strSMSMessage;
	}
//This method is used to prepare input for commoncode and call getCommonCodeList and return the output document.
/**
	 * @param env
	 * @param strCodeType
	 * @return
	 * @throws Exception
	 */
	private Document getCommonCodeList(YFSEnvironment env, String strCodeType) throws Exception {
		log.beginTimer("AcademyAssemblyInProgressCheckAndSendSMSAPI.getCommonCodeList");
		log.verbose(
				"Start AcademyAssemblyInProgressCheckAndSendSMSAPI.getCommonCodeList() strCodeType :: " + strCodeType);
		Document inDocGetCommonCodeList = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
		inDocGetCommonCodeList.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, strCodeType);
		log.verbose("input to getCommonCodeList " + XMLUtil.getXMLString(inDocGetCommonCodeList));
		Document outDocGetCommonCodeList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMON_CODELIST,
				inDocGetCommonCodeList);
		log.verbose("End AcademyAssemblyInProgressCheckAndSendSMSAPI.getCommonCodeList() output XML: "
				+ XMLUtil.getXMLString(outDocGetCommonCodeList));
		log.endTimer("AcademyAssemblyInProgressCheckAndSendSMSAPI.getCommonCodeList");
		return outDocGetCommonCodeList;
	}

	public void setProperties(Properties props) throws Exception {
		this.props = props;

	}
}