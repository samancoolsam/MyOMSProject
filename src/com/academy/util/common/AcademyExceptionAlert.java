package com.academy.util.common;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.constants.AcademyConstants;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantriks.yih.adapter.util.YantriksCommonUtil;

public class AcademyExceptionAlert {

	static YFCLogCategory log = YFCLogCategory.instance(AcademyExceptionAlert.class);

	/**
	 * This method is generate an alert if retry for order reservation fails.
	 * @param aEnvironment
	 * @param expType
	 * @param orderHdrKey
	 * @param queueID
	 * @param strDesc
	 * @throws Exception
	 */
	public static void raiseAlert(YFSEnvironment aEnvironment, String expType, String orderHdrKey, String queueID,
			String strDesc) throws Exception {
		
		Document inDocInbox = SCXmlUtil.createDocument(AcademyConstants.A_INBOX);
		Element inEleInbox = inDocInbox.getDocumentElement();
		inEleInbox.setAttribute(AcademyConstants.A_ENTERPRISE_KEY, AcademyConstants.A_ACADEMY_DIRECT);

		inEleInbox.setAttribute(AcademyConstants.A_ACTIVE_FLAG, AcademyConstants.V_ACTIVE_FLAG);
		inEleInbox.setAttribute(AcademyConstants.A_CONSOLIDATE, AcademyConstants.V_CONSOLIDATE);
		inEleInbox.setAttribute(AcademyConstants.A_EXCEPTION_TYPE, expType);
		inEleInbox.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, orderHdrKey);
		inEleInbox.setAttribute(AcademyConstants.A_QUEUE_ID, queueID);//
		inEleInbox.setAttribute(AcademyConstants.A_DESCRIPTION, strDesc);
		inEleInbox.setAttribute(AcademyConstants.A_DETAIL_DESCRIPTION, strDesc);
		if (log.isDebugEnabled()) {
			log.debug(
					"AcademyExceptionAlert:: raiseAlert():: In raiseAlert createExcpetion Input:: BEGIN"
							+ SCXmlUtil.getString(inDocInbox));
	}

	Document outDoc = YantriksCommonUtil.invokeAPI(aEnvironment, AcademyConstants.A_CREATE_EXCEPTION , inDocInbox);
	if(log.isDebugEnabled()) {
		log.debug("TBPaymentsAlertsAPI:: raiseAlert():: In raiseAlert  createExcpetion output :: TBPaymentsAlertsAPI(): BEGIN"+SCXmlUtil.getString(outDoc));
}

}

}
