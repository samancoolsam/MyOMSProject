package com.academy.ecommerce.sterling.email.api;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.email.util.AcademyEmailUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

	public class AcademyPostPushNotificationContentOnRFCPReminder implements YIFCustomApi {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPostPushNotificationContentOnRFCPReminder.class);
	/**
	 * This method customizes input xml to post Push Notification xml message to ESB queue
	 * 
	 * @param inDoc
	 * @return inDoc
	 * @throws Exception
	 */
	public Document preparePNContentRFCPReminder(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("AcademyPostPushNotificationContentOnRFCPReminder.preparePNContentRFCPReminder() start :" + XMLUtil.getXMLString(inDoc));
		updatePNContent(env, inDoc);
		log.verbose("AcademyPostPushNotificationContentOnRFCPReminder.preparePNContentRFCPReminder() end :" + XMLUtil.getXMLString(inDoc));
		return inDoc;
	}	
	
	private void updatePNContent(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("Inside preparePNContentRFCPReminder for "+XMLUtil.getXMLString(inDoc));
		Element eleIndoc = inDoc.getDocumentElement();
		String strIsFinalReminder = eleIndoc.getAttribute("IsFinalReminder");
		
		//populate messageType and messageID
		if (!YFCCommon.isVoid(strIsFinalReminder) && (strIsFinalReminder.equals(AcademyConstants.ATTR_Y)))
		{
			AcademyEmailUtil.updateMessageRef(env, eleIndoc, "RFCP_FINAL_REM_MSG_ID_PROD", "RFCP_FINAL_REM_MSG_ID_STAGE", "PN_RFCP_FINAL_REM_MSG_TYPE");
		}
		else {
			AcademyEmailUtil.updateMessageRef(env, eleIndoc, "RFCP_REM_MSG_ID_PROD", "RFCP_REM_MSG_ID_STAGE", "PN_RFCP_REM_MSG_TYPE");
		}
		
		//Start : OMNI-74228 : BOPIS/STS Consolidated Pickup Reminders
		String isReminderConsolidation = eleIndoc.getAttribute(AcademyConstants.STR_IS_REMINDER_CONSOLIDATION_ENABLED);
		if (!AcademyConstants.STR_YES.equals(isReminderConsolidation)) {
			//Remove Orderlines			
			AcademyEmailUtil.removeOrderlines(inDoc, env, eleIndoc);
		} else {
			log.verbose("Removing nonRFCP lines as isReminderConsolidation is: " + isReminderConsolidation);
			AcademyEmailUtil.removeNonRFCPOrderlines(eleIndoc);
		}
		//End : OMNI-74228 : BOPIS/STS Consolidated Pickup Reminders
			
		log.verbose("Done preparePNContentRFCPReminder"+XMLUtil.getXMLString(inDoc));
			
	}

	@Override
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
}
