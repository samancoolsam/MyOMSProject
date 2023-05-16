package com.academy.util.xml;

import java.util.Properties;

import org.w3c.dom.Document;


import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyExecuteService {

		
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyExecuteService.class);
	 private Properties props;
		public void setProperties(Properties props) throws Exception {
			        this.props = props;
			    }
	public Document executeService(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer("AcademyExecuteService::executeService");
		String sServiceName = props.getProperty(AcademyConstants.STR_SERVICE_NAME);
		YFCDocument inYFCDocument = YFCDocument.getDocumentFor(inDoc);
		Document ourDoc = AcademyUtil.invokeService(env, sServiceName, inYFCDocument.getDocument());
		log.endTimer("AcademyExecuteService::executeService");
		return ourDoc;
	}

}
