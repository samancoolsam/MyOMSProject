package com.academy.ecommerce.sterling.email.api;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.email.util.AcademyEmailUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author LAP
 *  OMNI-38141 : Push Notification for Package Shipped
 */
public class AcademyPushNotificationOnShipConfirm implements YIFCustomApi {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPushNotificationOnShipConfirm.class);

	@Override
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	public Document prepareInputForShipConfirm(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("OMNI-38141 - Push Notification Ship Confirm Start - input " + XMLUtil.getXMLString(inDoc));
		Element eleOrder = inDoc.getDocumentElement();
		
		Element eleInDoc = inDoc.getDocumentElement();
		AcademyEmailUtil.removeOrderlines(inDoc, env, eleInDoc);
		AcademyEmailUtil.updateMessageRef(env, eleOrder, "STH_SHP_CONF_MSG_ID_PROD", "STH_SHP_CONF_MSG_ID", "STH_SHP_CONF_MSG_TYPE");
		log.verbose("OMNI-38141 - Push Notification Ship Confirm Start - output  " + XMLUtil.getXMLString(inDoc));
		return inDoc;
	}
}
