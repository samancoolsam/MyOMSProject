package com.academy.ecommerce.sterling.email.api;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.email.util.AcademyEmailUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

	public class AcademyPostPushNotificationContentOnPickup implements YIFCustomApi {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPostPushNotificationContentOnPickup.class);
	/**
	 * This method customizes input xml to post Push Notification xml message to ESB queue
	 * 
	 * @param inDoc
	 * @return inDoc
	 * @throws Exception
	 */
	public Document preparePNContentRFCPickup(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("AcademyPostPushNotificationContentOnPickup.preparePNContentRFCPickup() start :" + XMLUtil.getXMLString(inDoc));
		updatePNContent(env, inDoc);
		log.verbose("AcademyPostPushNotificationContentOnPickup.preparePNContentRFCPickup() end :" + XMLUtil.getXMLString(inDoc));
		return inDoc;
	}	
	
	private void updatePNContent(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("Inside preparePNContentRFCPickup for "+XMLUtil.getXMLString(inDoc));
		Element eleIndoc = inDoc.getDocumentElement();
		
		//populate messageType and messageID
		
		AcademyEmailUtil.updateMessageRef(env, eleIndoc, "RFCP_PICKEDUP_MSG_ID_PROD", "RFCP_PICKEDUP_MSG_ID_STAGE", "PN_RFCP_PICKEDUP_MSG_TYPE");
		
		//Remove Orderlines
		
		AcademyEmailUtil.removeOrderlines(inDoc, env, eleIndoc);
			
		log.verbose("Done preparePNContentRFCPickup"+XMLUtil.getXMLString(inDoc));
		
	}

	@Override
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
}
