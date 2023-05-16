package com.academy.ecommerce.sterling.order.api;

import java.util.Properties;

import org.w3c.dom.Document;

import com.academy.util.common.AcademyUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyChangeOrderForBOGOAPI implements YIFCustomApi  {
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
	}
	public Document changeOrderForBOGO(YFSEnvironment env,
			Document inDoc) throws Exception {
		
		AcademyUtil.removeContextObject(env, "AddNewLine");
		
		AcademyUtil.invokeAPI(env, "changeOrder", inDoc);
		return inDoc;
	}

}
