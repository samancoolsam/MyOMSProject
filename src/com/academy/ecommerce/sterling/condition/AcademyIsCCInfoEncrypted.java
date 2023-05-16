package com.academy.ecommerce.sterling.condition;

import java.util.Map;
import java.util.Properties;

import com.yantra.ycp.japi.YCPDynamicCondition;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyIsCCInfoEncrypted implements YCPDynamicCondition{
	
private Properties props;
    
    /*
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademyIsCCInfoEncrypted.class);
   
    /**
     * 
     */
    public void setProperties(Properties props) {
        this.props = props;
    }
    
	public boolean evaluateCondition(YFSEnvironment env, String sName,
			Map mapData, String sXMLData) {
		log.verbose("Invoking AcademyIsCCInfoEncrypted with input " + sXMLData);
		YFCDocument paymentDoc = YFCDocument.getDocumentFor(sXMLData);
		YFCElement paymentEle = paymentDoc.getDocumentElement();
		String chargeTransKey = paymentEle.getAttribute("ChargeTransactionKey");
		String chargeType = paymentEle.getAttribute("ChargeType");

		if(YFCObject.isVoid(chargeTransKey) && "AUTHORIZATION".equalsIgnoreCase(chargeType)){
			log.verbose("Credit card not encrypted");
			return false;
		}
		log.verbose("exiting Credit card encrypted");
		return true;
	}
}
