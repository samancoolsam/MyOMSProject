package com.academy.ecommerce.sterling.condition;

import java.util.Map;
import java.util.Properties;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.yantra.ycp.japi.YCPDynamicCondition;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyIsExceptionScenario implements YCPDynamicCondition{
/*
 * This condition checks if variable (IsException) is set to "Y" in Environment, 
 * This will be set up by system when user enters SHORT as exception code.
 * In case of true Exception zone should be stamped.
 */
	private Properties props;
    
    /*
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademyIsExceptionScenario.class);
   
    /**
     * Stores properties configured in configurator. The property expected by this API
     * is: academy.helpdesk.getdoc.env.key
     * @param props Properties configured in Configurator.
     */
    public void setProperties(Properties props) {
        this.props = props;
    }
    public boolean evaluateCondition(YFSEnvironment env, String sName,
			Map mapData, String sXMLData) {
    	log.beginTimer(" Begining of AcademyIsExceptionScenario-> evaluateCondition Api");
    	Object sIsException = AcademyUtil.getContextObject(env,"IsException");
    	log.verbose("IsException variable set in env is" + sIsException);
    	if(YFCObject.equals(sIsException, AcademyConstants.STR_YES))
    	{
    		log.verbose("Entering true condtion" );
    		log.endTimer(" End of AcademyIsExceptionScenario-> evaluateCondition Api");
    		return true;
    	}
    	log.endTimer(" Begining of AcademyIsExceptionScenario-> evaluateCondition Api");
    	return false;
    }
}
