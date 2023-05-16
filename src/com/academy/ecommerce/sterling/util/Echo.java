package com.academy.ecommerce.sterling.util;

import java.util.Enumeration;
import java.util.Properties;

import org.w3c.dom.Document;

import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class Echo implements YIFCustomApi {
	/**
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(Echo.class);

	protected Properties oProperties = new Properties();    
	public void setProperties(Properties properties) {
		if (properties != null) {
			oProperties = properties;
		} 
	}

	/**
	 * Use this method to read the configured properties
	 * from the custom API
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	    public String getProp(String  key, String defaultValue){
	        if (oProperties!=null && oProperties.getProperty(key)!=null) return oProperties.getProperty(key);
	        return defaultValue;
	    }

	    public Properties getProp(){
	        return oProperties;
	    }
	
	
	
	public Document echo (YFSEnvironment env, Document inputXML) throws Exception {
        log.verbose("----------- [ECHO] -----------");
       

        
        if (null != oProperties) {
            Enumeration keys = oProperties.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                log.verbose(key + ":" + oProperties.getProperty(key));
            }
        }
        log.verbose(XMLUtil.getXMLString(inputXML));
        log.verbose("----------- [ECHO] -----------");
        if ("Y".equals(getProp("ERROR", "N"))) {
            throw new YFSException(getProp("ERRMSG", "STOP_ME"));
        }
        return inputXML;
    }

}
