package com.academy.ecommerce.sterling.condition;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyBOPISUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyIsListrakEnabledForShortage implements YCPDynamicConditionEx {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyIsListrakEnabledForShortage.class);
	
	@Override
	public boolean evaluateCondition(YFSEnvironment envObj, String arg1, Map arg2, Document arg3) {
		log.verbose("AcademyIsListrakEnabledForShortage.evaluateCondition() :: ");
		log.verbose("input doc for AcademyIsListrakEnabledForShortage--evaluateCondition :: "+XMLUtil.getXMLString(arg3));
		try {
			Document docOutGetCommonCodeList = AcademyBOPISUtil.getCommonCodeList(envObj,
					AcademyConstants.LISTRAK_EMAIL_CODES, AcademyConstants.PRIMARY_ENTERPRISE);
			String sShortageEmailEnabled=fetchCommonCodeShortDesc(docOutGetCommonCodeList,"SHORTAGE_LISTRAK_EMAIL_ENABLED");
			
			if( "Y".equals(sShortageEmailEnabled)) {
				log.verbose("AcademyIsListrakEnabledForShortage.evaluateCondition() :: true");
				return true;
			}else {
				log.verbose("AcademyIsListrakEnabledForShortage.evaluateCondition() :: false");
				return false;
			}
				
		}catch(Exception ex){
			String strErrorMessage = ex.getMessage();
			YFCDocument inEx = YFCDocument.getDocumentFor(strErrorMessage);
			String strErrorCode = inEx.getDocumentElement().getChildElement("Error").getAttribute("ErrorCode");
			YFSException e = new YFSException();
			e.setErrorCode(strErrorCode);
			e.setErrorDescription(strErrorMessage);
			throw e;
		}
	}
	
	public String fetchCommonCodeShortDesc(Document docOutGetCommonCodeList,String listrakFlag) throws Exception {
		Element eleCommonCode = (Element) XPathUtil.getNode(docOutGetCommonCodeList,
				"/CommonCodeList/CommonCode[@CodeValue='" + listrakFlag + "']");
		String sCodeShortDesc = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
		log.verbose("codeShortDesc :: "+sCodeShortDesc);
		return sCodeShortDesc;
		
	}


	@Override
	public void setProperties(Map arg0) {
		// TODO Auto-generated method stub
		
	}

}
