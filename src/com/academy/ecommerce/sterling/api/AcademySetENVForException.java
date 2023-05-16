package com.academy.ecommerce.sterling.api;

import java.util.Properties;

import org.w3c.dom.Document;


import com.academy.util.common.AcademyUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/* This will set environment variable ISException to Y if SHORT exception code is entered.*/
public class AcademySetENVForException implements YIFCustomApi {

	private Properties props;
 /**
     * Instance of logger
 */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademySetENVForException.class);

    public void setProperties(Properties props) {
        this.props = props;
    }
    
    public void academysetinENV(YFSEnvironment env, Document inDoc) throws Exception {
		
    AcademyUtil.setContextObject(env, "IsException", "Y");
    	
    
    	
    }
}
