package com.academy.util.common;

import java.util.Properties;

import org.w3c.dom.Document;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;


/**
 * This class is used to invoke a stream of Yantra Services. The names of the
 * Services to be invoked are passed using the argument tab in the Service Builder Screen.
 * The service sequence is specified by naming services as ServiceName_1, Service_2 and so on...


 */
public class AcademyInvokeService implements YIFCustomApi {

	
	  /**
     * Instance to store the properties configured for the condition in Configurator.
     */
    private Properties props;
    private static YFCLogCategory log = YFCLogCategory.instance(AcademyInvokeService.class);
    
	public void setProperties(Properties arg0) throws Exception {
		 this.props = arg0;
	}
	
	public Document invokeServiceInSequence(YFSEnvironment env, Document inDoc) 
    throws Exception {
		int iSequence = 1;
		if(!YFCObject.isVoid(props)){
			String strServiceName = props.getProperty("ServiceName_"+String.valueOf(iSequence));
			while (!YFCObject.isVoid(strServiceName)){
				AcademyUtil.invokeService(env, strServiceName, inDoc);
				iSequence++;
				strServiceName = props.getProperty("ServiceName_"+String.valueOf(iSequence));
			}
		}
	
		return inDoc;
	}

	/**
	 * Used to invoke a service by providing service name as argument. 
	 * @param env
	 * @param inDoc
	 * @return - Returns output of the service.
	 * @throws Exception
	 */
	public Document invokeServiceByArgument(YFSEnvironment env, Document inDoc) throws Exception {
		if(!YFCObject.isVoid(props)) {
			String strServiceName = props.getProperty("ServiceName_1");
			log.verbose("Inside invokeServiceByArgument method and invoking service " + strServiceName);
			if(!YFCObject.isVoid(strServiceName)) {
				return AcademyUtil.invokeService(env, strServiceName, inDoc);
			}
		}
		log.verbose("Inside invokeServiceByArgument method and failed to execute service " +
				"hence returning inDoc");
		return inDoc;
	}
	
}
