package com.academy.ecommerce.sterling.condition;


import java.util.Map;
import java.util.Properties;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;

import com.yantra.ycp.japi.YCPDynamicCondition;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyDoesDocumentExistInEnvCondition implements YCPDynamicCondition{

	
private Properties props;
    
    /*
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademyDoesDocumentExistInEnvCondition.class);
   
    /**
     * Stores properties configured in configurator. The property expected by this API
     * is: academy.helpdesk.getdoc.env.key
     * @param props Properties configured in Configurator.
     */
    public void setProperties(Properties props) {
        this.props = props;
    }
    
    // to check if docuemnt exisit in Env
	public boolean evaluateCondition(YFSEnvironment env, String sName,
			Map mapData, String sXMLData) {
		String sEnvcheck=null;
		
		//to check if input XML is from web. Web order will have isNewOrder as Y
		
		log.beginTimer(" Begining of AcademyDoesDocumentExistInEnvCondition-> evaluateCondition Api");

		if(!YFCObject.isVoid(mapData)){
		
			Object obj = AcademyUtil.getContextObject(env, "OrderPricing");	    	
	    	
	    	if(!YFCObject.isVoid(obj)){
	    		//System.out.println("Object found");
	    		return true;
	    	}
		}
		
		YFCDocument inXml = YFCDocument.getDocumentFor(sXMLData);
		if(!YFCObject.isVoid(inXml)){
			String sIsNewOrder = inXml.getDocumentElement().getAttribute("IsNewOrder");
			String sOrdPurpose = inXml.getDocumentElement().getAttribute("OrderPurpose");
			String sPurpose = inXml.getDocumentElement().getAttribute("Purpose");
		if(!YFCObject.isVoid(sIsNewOrder)){
			if (sIsNewOrder.equals(AcademyConstants.STR_YES) && (!sOrdPurpose.equals("REFUND") || sPurpose.equals("APPEASEMENT"))){
				log.beginTimer(" Ending of AcademyDoesDocumentExistInEnvCondition-> evaluateCondition Api");
				return true;
			}
		}
		}
		
		log.beginTimer(" End of AcademyDoesDocumentExistInEnvCondition-> evaluateCondition Api");
		return false;
		
	}

}
