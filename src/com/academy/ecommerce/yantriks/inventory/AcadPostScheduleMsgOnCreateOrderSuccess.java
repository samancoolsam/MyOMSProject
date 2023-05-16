package com.academy.ecommerce.yantriks.inventory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcadPostScheduleMsgOnCreateOrderSuccess {

	private static YFCLogCategory log = YFCLogCategory.instance(AcadPostScheduleMsgOnCreateOrderSuccess.class);

	/**
	 * OMNI -25858
	 * This method is to schedule the order
	 * 
	 * @param env
	 * @param inDoc
	 * @return inDoc
	 */

	public Document processCreateOrderSuccessXML(YFSEnvironment env, Document inDoc) {

		String methodName = "processCreateOrderSuccessXML";
		log.beginTimer(methodName);
		if (log.isDebugEnabled())
			log.debug("Input AcadPostScheduleMsgOnCreateOrderSuccess : " + methodName + " : " + SCXmlUtil.getString(inDoc));

		Element eleRootOrderList = inDoc.getDocumentElement();
		String orderHeaderKey = eleRootOrderList.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
		log.debug("orderHeaderKey :: " + orderHeaderKey);
		System.out.println("orderHeaderKey :" + orderHeaderKey);
		
			Document scheduleDoc = SCXmlUtil.createDocument(AcademyConstants.ELE_SCHEDULE_ORDER);
			Element inEleInbox = scheduleDoc.getDocumentElement();
			inEleInbox.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY, orderHeaderKey);
			String serviceName = AcademyConstants.ACADEMY_POST_ORDER_SCHEDULE;
			if (log.isDebugEnabled())
				log.debug("Input AcadPostScheduleMsgOnCreateOrderSuccess : " + "Schedule Doc" + " : " + SCXmlUtil.getString(scheduleDoc));

			try {
				AcademyUtil.invokeService(env, serviceName, scheduleDoc);
			} catch (Exception e) {
				log.error("Exception occur in AcadPostScheduleMsgOnCreateOrderSuccess :: " + e.getMessage());
			}
		return inDoc;
	}


}
