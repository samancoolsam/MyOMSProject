package com.academy.ecommerce.sterling.userexits;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.core.YCPContext;
import com.yantra.ycp.japi.ue.YCPBeforeCreateExceptionUE;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;

public class AcademyGenerateAlertKeyBeforeCreateExceptionUEImpl implements YCPBeforeCreateExceptionUE
{
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyGenerateAlertKeyBeforeCreateExceptionUEImpl.class);

	public Document beforeCreateException(YFSEnvironment env, Document inDoc) throws YFSUserExitException
	{
		try{
			log.verbose("########## Input Document ########" + XMLUtil.getXMLString(inDoc));
			if(!YFCObject.isVoid(inDoc))
			{
			// Start of Fix For STL-455
			
				Element inputXML = inDoc.getDocumentElement();
				String flowName = inputXML.getAttribute("FlowName");
				
				if(flowName.equals("CLOSE_MANIFEST")) {
					AcademyUtil.invokeService(env, AcademyConstants.RAISE_ALERT_ON_CLOSE_MANIFEST_FAILURE, inDoc);
			}
			// End of Fix For STL-455		
				// Added by Keerthana
				// Logic to make the FollowupDate current day + 1
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, 1);
				String pattern = "yyyy-MM-dd";
				SimpleDateFormat formattedDate = new SimpleDateFormat(pattern);
				inputXML.setAttribute("FollowupDate", formattedDate.format(cal.getTime()));
				// End by Keerthana	
				
				String strExceptionType = inputXML.getAttribute("ExceptionType");
				long alertKey = AcademyGenerateAlertKeyBeforeCreateExceptionUEImpl.getNextDBSeqno(env, "SEQ_EXTN_ALERT_KEY");
				log.verbose("alert key ----------- " + alertKey);
				NodeList extnNodeList = inputXML.getElementsByTagName(AcademyConstants.ELE_EXTN);
				Element extnElement = null;
				if(YFCObject.isNull(extnNodeList) || extnNodeList.getLength() == 0)
				{
					extnElement = inDoc.createElement(AcademyConstants.ELE_EXTN);
				}
				else
				{
					extnElement = (Element)extnNodeList.item(0);
				}

				extnElement.setAttribute(AcademyConstants.ATTR_EXTN_ALERT_KEY, prefixWithRequiredZeroes(String.valueOf(alertKey)));
				
				/*Check if exception type is UIEXCEPTION or AGENTEXCEPTION or Agent Exception and set NON_CC_EXEPTION to Y in YFS_INBOX*/
				if(strExceptionType.equalsIgnoreCase("UIEXCEPTION")|| strExceptionType.equalsIgnoreCase("AGENTEXCEPTION") || strExceptionType.equalsIgnoreCase("Agent Exception")){
					extnElement.setAttribute("ExtnNonCCExceptionType", "Y");
				}
				inputXML.appendChild(extnElement);
				
				//START: STL-1626 setting ExpirationDays for auto close.
				String strExpirationDays = inputXML.getAttribute(AcademyConstants.ATTR_EXPIRATION_DAYS);
				//Checking if ExpirationDays is already coming in Input, If coming not doing any thing.
				if (YFCObject.isVoid(strExpirationDays) || AcademyConstants.ATTR_ZERO.equalsIgnoreCase(strExpirationDays)) {
					setExpirationDays(env, inputXML, strExceptionType);
				}
				// END: STL-1626
				
				log.verbose("########## output Document ########" + XMLUtil.getXMLString(inDoc));
			}

			return inDoc;
		}catch (Exception ex) {
			if (ex instanceof YFSUserExitException) {
				throw (YFSUserExitException)ex;
			}
			YFSUserExitException ueExce = new YFSUserExitException(ex.getMessage());
			throw ueExce;	
		}
	}

	private String prefixWithRequiredZeroes(String alertKey)
	{
		String mask = AcademyConstants.ALERT_MASKED_NUMBER;
		String updatedAlertKey = mask.substring(0, mask.length() - alertKey.length()) + alertKey;
		return updatedAlertKey;
	}

	/**
	 * Method getNextDBSeqno:: This is a method which reads the sequence and returns the same.
	 * 
	 * @param YFSEnvironment
	 * @param String
	 * @return long seqNo
	 */
	public static long getNextDBSeqno(YFSEnvironment yfsEnv, String dbSeqName)
	{

		// Get the Database Sequnce Number from the seq_inv_snap_shot sequence
		long seqNo = ((YCPContext)yfsEnv).getNextDBSeqNo(dbSeqName);

		// Return the Database Sequnce Number
		return seqNo;
	}
	
	 /**
     * This method is used to set ExpirationDays on the baisis of common code value configured.
     * @param env
     * @param inputXML
     * @param strExceptionType
     * @throws Exception
     */
	private void setExpirationDays(YFSEnvironment env, Element inputXML, String strExceptionType) throws Exception {
		log.verbose("AcademyGenerateAlertKeyBeforeCreateExceptionUEImpl::setExpirationDays::inputXML:" + XMLUtil.getElementXMLString(inputXML));
		log.verbose("strExceptionType::" + strExceptionType);
		int iLength;
		Element commonCode = null;
		String codeValue = "";
		Document docGetCommonCodeListInput = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
		docGetCommonCodeListInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, AcademyConstants.ALERT_EXP_DAYS);
		docGetCommonCodeListInput.getDocumentElement().setAttribute(AcademyConstants.ORG_CODE_ATTR, AcademyConstants.PRIMARY_ENTERPRISE);
		docGetCommonCodeListInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.SALES_DOCUMENT_TYPE);
		Document docOutputGetCommonCodeListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMONCODE_LIST,docGetCommonCodeListInput);
		NodeList nlCommonCode = docOutputGetCommonCodeListOutput.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);

		if (!YFCObject.isVoid(nlCommonCode)) {
			
			iLength = nlCommonCode.getLength();
			for (int iCommonCodeCount = 0; iCommonCodeCount < iLength; iCommonCodeCount++) {
				commonCode = (Element) nlCommonCode.item(iCommonCodeCount);
				codeValue = commonCode.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE);
				//Checking if codeValue is equal to ExceptionType.
				if (strExceptionType.equalsIgnoreCase(codeValue)) {
					//Setting the ExpirationDays according to ExceptionType that is configured in Custom Common Code.
					inputXML.setAttribute(AcademyConstants.ATTR_EXPIRATION_DAYS,commonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC));
					break;
				}
				// else if codeValue is Default Expiration Days
				else if (AcademyConstants.DEFAULT_EXP_DAYS.equalsIgnoreCase(codeValue)) {
					//Setting the ExpirationDays as DEFAULT_EXP_DAYS value that is configured in Custom common code.
					inputXML.setAttribute(AcademyConstants.ATTR_EXPIRATION_DAYS,commonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC));
					break;
				}
			}
		}
		log.verbose("AcademyGenerateAlertKeyBeforeCreateExceptionUEImpl::setExpirationDays::inputXML:" + XMLUtil.getElementXMLString(inputXML));
	}

}
