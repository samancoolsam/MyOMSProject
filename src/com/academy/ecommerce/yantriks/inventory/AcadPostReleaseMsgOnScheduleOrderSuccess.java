package com.academy.ecommerce.yantriks.inventory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcadPostReleaseMsgOnScheduleOrderSuccess {

	private static YFCLogCategory log = YFCLogCategory.instance(AcadPostReleaseMsgOnScheduleOrderSuccess.class);

	/**
	 * OMNI- 25858
	 * This method is to check if the order is not hold and contains any non hold bopis line on schedule
	 * order success, if yes create a service
	 * 
	 * @param env
	 * @param inDoc
	 * @return inDoc
	 */

	public Document postReleaseMsgForBopisLineWithNoHold(YFSEnvironment env, Document inDoc) {

		String methodName = "postReleaseMsgForBopisLineWithNoHold";
		log.beginTimer(methodName);
		if (log.isDebugEnabled())
			log.debug("Input AcadPostReleaseMsgOnScheduleOrderSuccess : " + methodName + " : " + SCXmlUtil.getString(inDoc));
		
		log.verbose("Reading Transaction Object ");
		Document docScheduleOnSuccess=(Document) env.getTxnObject(AcademyConstants.STR_SCH_SUCCESS_DOC);
		if(YFCObject.isVoid(docScheduleOnSuccess)) {
			log.verbose("Transaction object document is null");
			return inDoc;
		}
		log.verbose("Document from transaction is :\n"+SCXmlUtil.getString(docScheduleOnSuccess));
		
		Element eleRootOrderList = docScheduleOnSuccess.getDocumentElement();
		Element eleOrderLines = SCXmlUtil.getChildElement(eleRootOrderList, AcademyConstants.ELE_ORDER_LINES);
		NodeList nlOrderLines = eleOrderLines.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);

		String orderHeaderKey = eleRootOrderList.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
		String isHold = eleRootOrderList.getAttribute(AcademyConstants.ATTR_HOLD_FLAG);

		if (isHold.equals(AcademyConstants.STR_NO)) {

			log.debug("orderHeaderKey :: " + orderHeaderKey);
			/**
			 * Conditions that needs to check:
			 * 1.If order is not on hold 2. A bopis line without hold  
			 */

			boolean IsBopisLinePresent = checkBopisWithNoHoldLine(nlOrderLines);

			if (IsBopisLinePresent) {
				Document releaseDoc = SCXmlUtil.createDocument(AcademyConstants.ELE_RELEASE_ORDER);
				Element inEleInbox = releaseDoc.getDocumentElement();
				inEleInbox.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY, orderHeaderKey);
				String serviceName =AcademyConstants.ACADEMY_POST_BOPIS_ORDER_RELEASE; // Need to check the value for
																							// this
				try {
				AcademyUtil.invokeService(env, serviceName, releaseDoc);
				} catch (Exception e) {
					log.error("Exception occur in AcadPostReleaseMsgOnScheduleOrderSuccess :: " + e.getMessage());
				}
				//return releaseDoc;
			}
		}
		return docScheduleOnSuccess;
	}

	private boolean checkBopisWithNoHoldLine(NodeList nlOrderLines) {
		boolean isBopis = false;
		int orderLineLength = nlOrderLines.getLength();
		for (int i = 0; i < orderLineLength; i++) {
			Element orderLineEle = (Element) nlOrderLines.item(i);

			String isHold = orderLineEle.getAttribute(AcademyConstants.ATTR_HOLD_FLAG);
			String strDeliveryMethod = orderLineEle.getAttribute(AcademyConstants.ATTR_DELIVERY_METHOD);
			String fulfillmentType = orderLineEle.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);

			if (!YFCCommon.isVoid(strDeliveryMethod) && (strDeliveryMethod.equals(AcademyConstants.STR_PICK)
					&& fulfillmentType.equals(AcademyConstants.STR_BUY_ONLINE_PICKUP_FROM_STORE)
					&& isHold.equals(AcademyConstants.STR_NO))) {
				isBopis = true;
				return isBopis;
			}
		}
		return isBopis;
	}

//	public static void main(String args[]) throws Exception {
//		Document doc = SCXmlUtil.createFromFileOrUrl("I:\\Sandhya\\Documents\\schedule.xml");
//		ScheduleOrderOnSuccessUEImpl obj = new ScheduleOrderOnSuccessUEImpl();
//		Document op = obj.processSuccessDbOrderXML(null, doc);
//		System.out.println(SCXmlUtil.getString(op));
//	}

}