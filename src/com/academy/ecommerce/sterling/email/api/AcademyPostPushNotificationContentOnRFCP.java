package com.academy.ecommerce.sterling.email.api;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.email.util.AcademyEmailUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyPostPushNotificationContentOnRFCP implements YIFCustomApi {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPostPushNotificationContentOnRFCP.class);
	/**
	 * This method customizes input xml to post Push Notification xml message to ESB queue
	 * 
	 * @param inDoc
	 * @return inDoc
	 * @throws Exception
	 */
	public Document preparePushNotificationContentRFCP(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("AcademyPostPushNotificationContentOnRFCP.preparePushNotificationContentRFCP() start :" + XMLUtil.getXMLString(inDoc));
		updatePNContent(env, inDoc);
		log.verbose("AcademyPostPushNotificationContentOnRFCP.preparePushNotificationContentRFCP() end :" + XMLUtil.getXMLString(inDoc));
		return inDoc;

	}

        /**
	 * This method customizes input xml to post Push Notification xml message to ESB queue for Alternate Store
	 * 
	 * @param inDoc
	 * @return inDoc
	 * @throws Exception
	 */
	
	public Document preparePushNotificationContentRFCPForAS(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("AcademyPostPushNotificationContentOnRFCP.preparePushNotificationContentRFCPForAS() start :" + XMLUtil.getXMLString(inDoc));
		updatePNContentForAS(env, inDoc);
		log.verbose("AcademyPostPushNotificationContentOnRFCP.preparePushNotificationContentRFCPForAS() end :" + XMLUtil.getXMLString(inDoc));
		return inDoc;
	}
	
	private void updatePNContentForAS(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("Inside preparePushNotificationContentRFCPForAS"+XMLUtil.getXMLString(inDoc));
		Element eleIndoc = inDoc.getDocumentElement();
		//Remove Orderlines
		AcademyEmailUtil.removeOrderlines(inDoc, env, eleIndoc);
		boolean isASRFCPPushNotification = AcademyEmailUtil.isAlternateLineRFCP(inDoc);
		boolean bHasCancelledLine = eleIndoc.getAttribute(AcademyConstants.ATTR_HAS_CANCELLED_LINES).equalsIgnoreCase(AcademyConstants.ATTR_Y) ? true : false;

		if (isASRFCPPushNotification) {
			// AS shipment
			AcademyEmailUtil.updateMessageRef(env, eleIndoc, "PN_AS_RFCP_MSG_ID_PROD", "PN_AS_RFCP_MSG_ID_STAGE","PN_AS_RFCP_MSG_TYPE");
		} else {
			if (bHasCancelledLine) {
				AcademyEmailUtil.updateMessageRef(env, eleIndoc, "PARTIAL_RFCP_MSG_ID_PROD","PARTIAL_RFCP_MSG_ID_STAGE", "PN_PARTIAL_RFCP_MSG_TYPE");
			} else {
				AcademyEmailUtil.updateMessageRef(env, eleIndoc, "RFCP_MSG_ID_PROD", "RFCP_MSG_ID_STAGE","PN_RFCP_MSG_TYPE");
			}
		}

		log.verbose("Done preparePushNotificationContentRFCPForAS"+XMLUtil.getXMLString(inDoc));
	}	

	private void updatePNContent(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("Inside preparePushNotificationContentRFCP"+XMLUtil.getXMLString(inDoc));
		Element eleIndoc = inDoc.getDocumentElement();

		//Remove Orderlines
		AcademyEmailUtil.removeOrderlines(inDoc, env, eleIndoc);
		
		//Check if DeliveryMethod is PICK or not
		NodeList nlOrderLines = AcademyEmailUtil.getCompleteOrderLineLines(eleIndoc);
		
		String strHasCancelledLine = eleIndoc.getAttribute(AcademyConstants.ATTR_HAS_CANCELLED_LINES);
		String strIsDSVSOFShipment = eleIndoc.getAttribute("IsDSVSOFShipment");
		
		for (int i = 0; i < nlOrderLines.getLength(); i++) {
			
			Element eleOrderline = (Element) nlOrderLines.item(i);
			
			if(!YFCObject.isVoid(eleOrderline) && "PICK".equals(eleOrderline.getAttribute("DeliveryMethod")) ||
					(!YFCCommon.isVoid(strIsDSVSOFShipment) && (strIsDSVSOFShipment.equals(AcademyConstants.ATTR_Y))))
			{
				//populate messageType and messageID
				if (!YFCCommon.isVoid(strHasCancelledLine) && (strHasCancelledLine.equals(AcademyConstants.ATTR_Y)))
				{
					AcademyEmailUtil.updateMessageRef(env, eleIndoc, "PARTIAL_RFCP_MSG_ID_PROD", "PARTIAL_RFCP_MSG_ID_STAGE", "PN_PARTIAL_RFCP_MSG_TYPE");
				}
				else {
					AcademyEmailUtil.updateMessageRef(env, eleIndoc, "RFCP_MSG_ID_PROD", "RFCP_MSG_ID_STAGE", "PN_RFCP_MSG_TYPE");
				}

			}				

		}

		log.verbose("Done preparePushNotificationContentRFCP"+XMLUtil.getXMLString(inDoc));

	}

	@Override
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}

}
