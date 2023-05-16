package com.academy.ecommerce.sterling.condition;

import java.util.Map;
import java.util.Properties;

import com.academy.util.common.AcademyUtil;
import com.yantra.ycp.japi.YCPDynamicCondition;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyCheckNewLineInEnv implements YCPDynamicCondition{
	
private Properties props;
    
    /*
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademyCheckNewLineInEnv.class);
   
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
		log.beginTimer(" Begining of AcademyCheckNewLineInEnv-> evaluateCondition Api");
		String sEnvcheck=null;
		if(!YFCObject.isVoid(mapData)){
			
			Object obj = AcademyUtil.getContextObject(env, "AddNewLine");	    
			
	    	
	    	if(!YFCObject.isVoid(obj)){
	    		//System.out.println("Object found");
	    		//AcademyUtil.removeContextObject(env, "AddNewLine");
	    		log.endTimer(" Ending of AcademyCheckNewLineInEnv-> evaluateCondition Api");
	    		return true;
	    	}
		}
		return false;
}
}
