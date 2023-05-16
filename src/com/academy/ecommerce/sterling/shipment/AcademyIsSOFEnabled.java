package com.academy.ecommerce.sterling.shipment;

import java.util.Map;

import org.w3c.dom.Document;

import com.academy.util.constants.AcademyConstants;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
/** This method returns true or false based on ISSOFEnabled TxnObject */

public class AcademyIsSOFEnabled implements YCPDynamicConditionEx {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyIsSOFEnabled.class);
	
	@Override
	public boolean evaluateCondition(YFSEnvironment envObj, String arg1, Map arg2, Document inDoc) {
		log.verbose("AcademyIsSOFEnabled.evaluateCondition() :: ");
		try {
				if(!YFCObject.isVoid(envObj.getTxnObject("ISSOFEnabled"))) {
					log.verbose("Returning True ");
					return true;
		
			}
				else {
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

	@Override
	public void setProperties(Map arg0) {
		log.verbose("props ");
	}
	

}