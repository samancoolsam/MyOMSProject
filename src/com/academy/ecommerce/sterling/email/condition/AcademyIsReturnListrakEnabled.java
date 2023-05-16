package com.academy.ecommerce.sterling.email.condition;

import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyBOPISUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XPathUtil;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyIsReturnListrakEnabled implements YCPDynamicConditionEx {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyIsReturnListrakEnabled.class);
	private Map props = null;	
	
	@Override
	public boolean evaluateCondition(YFSEnvironment envObj, String arg1, Map arg2, Document inDoc) {
		log.verbose("AcademyIsReturnInitiationEnabled.evaluateCondition() :: ");
		try {
			String returnFlagTobeChecked = (String) props.get(AcademyConstants.RETURN_LISTRAK_CODE_VALUE);
			Document docOutGetCommonCodeList = AcademyBOPISUtil.getCommonCodeList(envObj,
					AcademyConstants.LISTRAK_EMAIL_CODES, AcademyConstants.PRIMARY_ENTERPRISE);
			
			String returnListrakFlag = fetchCommonCodeShortDesc(docOutGetCommonCodeList, 
					returnFlagTobeChecked);
			
			if (!YFCCommon.isVoid(returnListrakFlag) && AcademyConstants.STR_YES.equals(returnListrakFlag)) {
				log.verbose("AcademyIsReturnInitiationEnabled.evaluateCondition() :: true");
				return true;
			} else {
				log.verbose("AcademyIsReturnInitiationEnabled.evaluateCondition() :: false");
				return false;
			}			
			
		} catch(Exception ex) {
			String strErrorMessage = ex.getMessage();
			YFCDocument inEx = YFCDocument.getDocumentFor(strErrorMessage);
			String strErrorCode = inEx.getDocumentElement().getChildElement(
					AcademyConstants.ELE_ERROR).getAttribute(AcademyConstants.ATTR_ERROR_CODE);
			YFSException e = new YFSException();
			e.setErrorCode(strErrorCode);
			e.setErrorDescription(strErrorMessage);
			throw e;
		}
	}
	
	public String fetchCommonCodeShortDesc(Document docOutGetCommonCodeList, String listrakFlag) throws Exception {
		Element eleCommonCode = (Element) XPathUtil.getNode(docOutGetCommonCodeList,
				"/CommonCodeList/CommonCode[@CodeValue='" + listrakFlag + "']");
		String sCodeShortDesc = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
		log.verbose("codeShortDesc :: " + sCodeShortDesc);
		return sCodeShortDesc;
		
	}
	
	public void setProperties(Map props) {
		this.props = props;
		log.verbose("setup the PropMap");
	}

}