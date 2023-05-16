package com.academy.util.common;

import java.util.Properties;
import org.w3c.dom.Document;
import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author <a href="mailto:Netai.dey@academy.com">Netai Dey</a>, Created on
 *         05/15/2015. This Class will handle any exceptions that occur while sending the email.
 *         This class can be re-used for other email services also.
 *         EmailSeviceName - Actual Email service name should be configure as part of API argument.
 */
public class AcademySendEmailWrapperService {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademySendEmailWrapperService.class);
	private Properties props;
	private String EMAIL_SERVICE_NAME = "EmailServiceName";
	
	public Document handleExceptionsInSendingEmail(YFSEnvironment env, Document inXML)throws Exception {
		log.verbose("AcademySendEmailWrapperService_handleException()_InXML:" +XMLUtil.getXMLString(inXML));
		String strServiceName = props.getProperty(EMAIL_SERVICE_NAME);
		log.verbose("Service Name *************::" + strServiceName);
		
		try {
			AcademyUtil.invokeService(env, strServiceName, inXML);
		} catch (Exception ex) {
			ex.printStackTrace();
			log.info("An exception has occured while sending email for "+strServiceName);
			log.info("AcademySendEmailWrapperService_outXML: " + XMLUtil.getXMLString(inXML));
		}				
		return inXML;
	}
	
	public void setProperties(Properties props) {
		this.props = props;
	}
}
