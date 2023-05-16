package com.academy.ecommerce.sterling.bopis.sfspacking.api;

import java.util.Properties;

import org.w3c.dom.Document;

import com.academy.util.common.AcademyUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademySFSModifyLOS implements YIFCustomApi{

	private Properties props;
	
	@Override
	public void setProperties(Properties props1) throws Exception {
		// TODO Auto-generated method stub	
		this.props = props1;
       		
	}
	
	public Document beforeCreateContainersAndPrintService(YFSEnvironment env, Document inDoc) throws Exception {
		
		String serviceName = props.getProperty("AcademySFSModifyLOSService");
		
		AcademyUtil.invokeService(env, serviceName, inDoc);
		
		return inDoc;
		
	}

}
