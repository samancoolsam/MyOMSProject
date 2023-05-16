package com.academy.ecommerce.sterling.condition;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyBOPISUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**#########################################################################################
*
* Project Name                : OMS_ESPPhase2_June2021
* Module                      : OMNI-20315
* Date                        : 20-APL-2021 
* Description				  : This class check condition Cancel EMail message xml posted to ESB queue.
* 								 
*
* #########################################################################################*/
public class AcademyIsListrakEmailCheckForCancel implements YCPDynamicConditionEx {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyIsListrakEmailCheckForCancel.class);
	
	@Override
	public boolean evaluateCondition(YFSEnvironment envObj, String arg1, Map arg2, Document arg3) {
		log.verbose("AcademyIsListrakEmailCheckForCancel.evaluateCondition() :: ");
		log.verbose("input DOC for AcademyIsListrakEmailCheckForCancel--evaluateCondition :: "+XMLUtil.getXMLString(arg3));
		try {
			String sCustomerCancel = XMLUtil.getAttributeFromXPath(arg3, AcademyConstants.XPATH_ATTR_CUSTOMER_CANCEL);			
			String sReasonCode = XMLUtil.getAttributeFromXPath(arg3, "Order/OrderAudit/@ReasonCode");
			log.verbose("input DOC for AcademyIsListrakEmailCheckForCancel--evaluateCondition--ReasonCode :: "+sReasonCode);			
			String sDeliveryMethod = XMLUtil.getAttributeFromXPath(arg3, "//OrderLine/@DeliveryMethod");
			log.verbose("input DOC for AcademyIsListrakEmailCheckForCancel--deliverymethod:: "+sDeliveryMethod);
			if(AcademyConstants.STR_PICK.equals(sDeliveryMethod) 
					&& "Customer Abandoned".equals(sReasonCode)) {
				log.verbose("AcademyIsListrakEmailCheckForCancel.evaluateCondition() Customer Abandoned :: true");
				return true;
			} 
			else if (AcademyConstants.STR_TRUE.equalsIgnoreCase(sCustomerCancel)) {
				log.verbose("AcademyIsListrakEmailCheckForCancel.evaluateCondition() Customer Cancel:: true");
				return true;
			}
			else { 
				if (!(StringUtil.isEmpty(sReasonCode))) {
				String strCodeType=null;
				Document outputCommomCodeList=null;
				Element commonCodeEle = null;
				NodeList nleleCommonCode=null;

				Document inputTocommonCodeList = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
				inputTocommonCodeList.getDocumentElement().setAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE, sReasonCode);
				inputTocommonCodeList.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC, sReasonCode);
				outputCommomCodeList = AcademyUtil.invokeAPI(envObj, AcademyConstants.API_GET_COMMONCODE_LIST, inputTocommonCodeList);
				nleleCommonCode = outputCommomCodeList.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);
				if (nleleCommonCode.getLength() > 0) {
					for (int i = 0; i < nleleCommonCode.getLength(); i++) {
						commonCodeEle = (Element) nleleCommonCode.item(i);
						if (commonCodeEle != null && commonCodeEle.hasAttributes()) {
							strCodeType = commonCodeEle.getAttribute(AcademyConstants.ATTR_CODE_TYPE);
							log.verbose("Code Type From Commom Code:" + strCodeType);

							if (strCodeType.equalsIgnoreCase("FraudCancel")) {
								log.verbose("AcademyIsListrakEmailCheckForCancel.evaluateCondition() Fraud Cancel:: true");
								return true;
							} else if (strCodeType.equalsIgnoreCase("ShortageCancel")) {
								log.verbose("AcademyIsListrakEmailCheckForCancel.evaluateCondition() Shortage Cancel:: true");
								return true;
							}
							else {
								log.verbose("AcademyIsListrakEmailCheckForCancel.evaluateCondition() Shortage Cancel:: true");
								return true;
							}
							
						} 
						
					}
				} 
				else {
					log.verbose("AcademyIsListrakEmailCheckForCancel.evaluateCondition() Shortage Cancel:: true");
					return true;
				}
			}
			else {
				log.verbose("AcademyIsListrakEmailCheckForCancel.evaluateCondition() Shortage Cancel:: true");
				return true;
			}
			
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
		log.verbose("AcademyIsListrakEmailCheckForCancel.evaluateCondition() Listrak Cancel:: false");
		return false;
	}

	@Override
	public void setProperties(Map arg0) {
		// TODO Auto-generated method stub
		
	}

}
