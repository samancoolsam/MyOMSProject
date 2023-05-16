package com.academy.ecommerce.sterling.egc;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyBOPISUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.ycp.japi.util.YCPBaseTaskAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/*##################################################################################
*
* Project Name                : POD March Release
* Module                      : OMS
* Author                      : CTS
* Date                        : 26-FEB-2021
* Description                 : This file implements the logic to raise alerts based on, 
* 								1. A regular EGC order without fraud hold did not receive
* 									shipment confirmation updates from WCS within SLA.
* 									SLA: Order Created Time + 'X' hours (COMMON CODE VALUE - EGC_SHIP_PENDG_ALERT_SLA)
*                               2. An EGC order, with Fraud hold resolved, did not receive
*                                   shipment confirmation updates from WCS within SLA.
*                                   SLA: Hold Resolved Time + 'X' hours (COMMON CODE VALUE - EGC_SHIP_PENDG_ALERT_SLA) 
*
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author                  Version#       Remarks/Description                     
* ---------------------------------------------------------------------------------
* 26-FEB-2021     CTS                      1.0            Initial version
* ##################################################################################*/

public class AcademyEGCRaiseShipPendingAlert extends YCPBaseTaskAgent {

	private static final YFCLogCategory logger = YFCLogCategory.instance(AcademyEGCRaiseShipPendingAlert.class);

	@Override
	public Document executeTask(YFSEnvironment env, Document docInp) throws Exception {

		try {

			logger.verbose("Input - AcademyEGCRaiseShipPendingAlert.executeTask() :: " + XMLUtil.getXMLString(docInp));

			Document docOutGetCommonCodeList = null;
			String strHoldResolvedDate = null;

			Element eleTaskQ = docInp.getDocumentElement();

			String strTaskQKey = eleTaskQ.getAttribute(AcademyConstants.ATTR_TASK_Q_KEY);
			String strOrderHeaderKey = eleTaskQ.getAttribute(AcademyConstants.ATTR_DATA_KEY);
			String strTaskQCreateTs = eleTaskQ.getAttribute(AcademyConstants.ATTR_CREATETS);

			Document docGetOrderListOut = prepareAndInvokeGetOrderList(env, strOrderHeaderKey);

			if (!YFCObject.isNull(docGetOrderListOut)) {

				logger.verbose("getOrderList - Output :: " + XMLUtil.getXMLString(docGetOrderListOut));

				NodeList nlOrderLineEGC = XPathUtil.getNodeList(docGetOrderListOut,
						"/OrderList/Order/OrderLines/OrderLine[@FulfillmentType='EGC' and (@MaxLineStatus='1100' or @MaxLineStatus='1200')]/Extn[@ExtnIsPromoItem='N']/..");
				
				if (nlOrderLineEGC.getLength() > 0) {

					docOutGetCommonCodeList = AcademyBOPISUtil.getCommonCodeList(env,
							AcademyConstants.STR_EGC_PENDING_SHIP_CONFIRM_ALERT, AcademyConstants.PRIMARY_ENTERPRISE);

					String strEGCAlertSLA = retrieveCodeShortDesc(docOutGetCommonCodeList,
							AcademyConstants.STR_EGC_PENDING_SHIP_CONFIRM_ALERT_SLA);

					List<String> alOrderHoldTypes = retrieveCodeShortDescHolds(docOutGetCommonCodeList,
							AcademyConstants.STR_EGC_HOLDS_TO_MONITOR);

					String strHoldTypeQuery = formHoldTypeQueryString(alOrderHoldTypes);

					// Check: if SLA in future
					if (compareTimestamps(addHoursToTime(strTaskQCreateTs, strEGCAlertSLA),
							AcademyConstants.STR_CONDITION_AFTER, getCurrentTimestampAsString())) {

						logger.verbose("Updating Task Queue with SLA :: ");

						manageTaskQueueRecord(env, strTaskQKey, addHoursToTime(strTaskQCreateTs, strEGCAlertSLA));

						// Check: if Fraud Hold exists
					} else if (isFraudHoldDetailsPresent(docGetOrderListOut, AcademyConstants.STR_HOLD_CREATED_STATUS,
							strHoldTypeQuery)) {

						logger.verbose("Open hold exists :: ");

						String strTaskQAvailDate = addHoursToTime(getCurrentTimestampAsString(), strEGCAlertSLA);
						manageTaskQueueRecord(env, strTaskQKey, strTaskQAvailDate);

						// Check: if Fraud Hold is resolved
					} else if (isFraudHoldDetailsPresent(docGetOrderListOut, AcademyConstants.STR_HOLD_RESOLVED_STATUS,
							strHoldTypeQuery)) {

						logger.verbose("Resolved hold exists :: ");

						strHoldResolvedDate = getHoldResolvedDate(docGetOrderListOut, strHoldTypeQuery,
								AcademyConstants.STR_HOLD_RESOLVED_STATUS);

						// Check: if SLA in future
						if (compareTimestamps(addHoursToTime(strHoldResolvedDate, strEGCAlertSLA),
								AcademyConstants.STR_CONDITION_AFTER, getCurrentTimestampAsString())) {

							logger.verbose("Setting SLA based on Modifyts of Resolved hold :: ");

							manageTaskQueueRecord(env, strTaskQKey,
									addHoursToTime(strHoldResolvedDate, strEGCAlertSLA));
						} else {

							logger.verbose("Raising Alert :: ");

							raisePendingConfirmationAlert(env, docGetOrderListOut);
							removeTaskQueueRecord(env, strTaskQKey);
						}
					} else {

						logger.verbose("Raising Alert :: ");

						raisePendingConfirmationAlert(env, docGetOrderListOut);
						removeTaskQueueRecord(env, strTaskQKey);
					}

				} else {

					removeTaskQueueRecord(env, strTaskQKey);
				}
			}

		} catch (Exception e) {

			logger.error("Exception caught :: " + e.getMessage());
		}

		return docInp;
	}

	private Document prepareAndInvokeGetOrderList(YFSEnvironment env, String strOrderHeaderKey) throws Exception {
		logger.beginTimer("AcademyEGCRaiseShipPendingAlert.prepareAndInvokeGetOrderList() :: ");

		Document docGetOrderListOutput = null;

		if (!StringUtil.isEmpty(strOrderHeaderKey)) {

			logger.verbose("AcademyEGCRaiseShipPendingAlert.prepareAndInvokeChangeShipment() - "
					+ "Order Header Key :: " + strOrderHeaderKey);

			Document docGetOrderListInput = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			Element eleGetOrderListInput = docGetOrderListInput.getDocumentElement();
			eleGetOrderListInput.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);

			logger.verbose(
					"AcademyEGCRaiseShipPendingAlert.prepareAndInvokeGetOrderList() - Input to getOrderList API :: "
							+ XMLUtil.getXMLString(docGetOrderListInput));

			docGetOrderListOutput = AcademyUtil.invokeService(env,
					AcademyConstants.ACADEMY_GETORDERLIST_FOR_EGC_SERVICE, docGetOrderListInput);

			logger.verbose(
					"AcademyEGCRaiseShipPendingAlert.prepareAndInvokeGetOrderList() - Output of getOrderList API :: "
							+ XMLUtil.getXMLString(docGetOrderListOutput));

		}

		logger.endTimer("AcademyEGCRaiseShipPendingAlert.prepareAndInvokeGetOrderList() ::");

		return docGetOrderListOutput;
	}

	public boolean isFraudHoldDetailsPresent(Document docOrderList, String strStatus, String strHoldTypeQuery)
			throws Exception {

		logger.beginTimer("Begin - AcademyEGCRaiseShipPendingAlert.isFraudHoldDetailsPresent() :: ");

		boolean isFraudHoldDetailsPresent = false;

		if (!StringUtil.isEmpty(strStatus) && !StringUtil.isEmpty(strHoldTypeQuery)) {

			logger.verbose("Status :: " + strStatus);

			logger.verbose("HoldType Query :: " + strHoldTypeQuery);

			Element eleOrderHold = (Element) XPathUtil.getNode(docOrderList.getDocumentElement(),
					"/OrderList/Order/OrderHoldTypes/OrderHoldType[(@Status='" + strStatus + "') and ("
							+ strHoldTypeQuery + ")]");

			if (!YFCObject.isNull(eleOrderHold)) {

				logger.verbose("Element OrderHoldType :: " + XMLUtil.getElementXMLString(eleOrderHold));
				isFraudHoldDetailsPresent = true;
			}
		}

		logger.endTimer("End - AcademyEGCRaiseShipPendingAlert.isFraudHoldDetailsPresent() :: ");

		return isFraudHoldDetailsPresent;
	}

	private void removeTaskQueueRecord(YFSEnvironment env, String strTaskQueueKey) throws Exception {

		logger.beginTimer("Begin - AcademyEGCRaiseShipPendingAlert.removeTaskQueueRecord() :: ");

		// manageTaskQueue API inDoc
		Document manageTaskQueueinDoc = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
		manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_OPERATION,
				AcademyConstants.STR_OPERATION_VAL_DELETE);
		manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_TASK_Q_KEY, strTaskQueueKey);

		// Invoking the manageTaskQueue API
		logger.verbose("Input document to manageTaskQueue API :: " + XMLUtil.getXMLString(manageTaskQueueinDoc));

		AcademyUtil.invokeAPI(env, AcademyConstants.API_MANAGE_TASK_QUEUE, manageTaskQueueinDoc);

		logger.endTimer("End - AcademyEGCRaiseShipPendingAlert.removeTaskQueueRecord() :: ");
	}

	private void manageTaskQueueRecord(YFSEnvironment env, String strTaskQueueKey, String strTaskQAvailableDate)
			throws Exception {

		logger.beginTimer("Begin - AcademyEGCRaiseShipPendingAlert.manageTaskQueueRecord() :: ");

		// manageTaskQueue API inDoc
		Document manageTaskQueueinDoc = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
		manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_OPERATION,
				AcademyConstants.STR_ACTION_MODIFY);
		manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_TASK_Q_KEY, strTaskQueueKey);
		manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_AVAIL_DATE, strTaskQAvailableDate);

		logger.verbose("Input to manageTaskQueue API :: " + XMLUtil.getXMLString(manageTaskQueueinDoc));

		// Invoking the manageTaskQueue API
		AcademyUtil.invokeAPI(env, AcademyConstants.API_MANAGE_TASK_QUEUE, manageTaskQueueinDoc);

		logger.endTimer("End - AcademyEGCRaiseShipPendingAlert.manageTaskQueueRecord() :: ");
	}

	private void raisePendingConfirmationAlert(YFSEnvironment env, Document docInp) throws Exception {

		logger.beginTimer("Begin - AcademyEGCRaiseShipPendingAlert.raisePendingConfirmationAlert() :: ");

		Element eleOrder = XmlUtils.getChildElement(docInp.getDocumentElement(), AcademyConstants.ELE_ORDER);

		Document docCreateException = XMLUtil.createDocument(AcademyConstants.ELE_INBOX);

		Element eleInbox = docCreateException.getDocumentElement();

		eleInbox.setAttribute(AcademyConstants.ATTR_ACTIVE_FLAG, AcademyConstants.STR_YES);

		eleInbox.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,
				eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));

		eleInbox.setAttribute(AcademyConstants.ATTR_ORDER_NO, eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_NO));

		eleInbox.setAttribute(AcademyConstants.ATTR_FLOW_NAME, AcademyConstants.STR_EGC_PENDING_SHIP_CONFIRM_ALERT_API);

		eleInbox.setAttribute(AcademyConstants.ATTR_SUB_FLOW_NAME,
				AcademyConstants.STR_EGC_PENDING_SHIP_CONFIRM_ALERT_API);

		eleInbox.setAttribute(AcademyConstants.ATTR_API_NAME, AcademyConstants.STR_EGC_PENDING_SHIP_CONFIRM_ALERT_API);

		eleInbox.setAttribute(AcademyConstants.ATTR_EXCPTN_TYPE,
				AcademyConstants.STR_EGC_PENDING_SHIP_CONFIRM_EXCEPTION);

		eleInbox.setAttribute(AcademyConstants.ATTR_ENTERPRISE_KEY, AcademyConstants.PRIMARY_ENTERPRISE);

		eleInbox.setAttribute(AcademyConstants.ATTR_DESCRIPTION, AcademyConstants.STR_EGC_PENDING_SHIP_CONFIRM_DESC);

		eleInbox.setAttribute(AcademyConstants.ATTR_DETAIL_DESCRIPTION,
				AcademyConstants.STR_EGC_PENDING_SHIP_CONFIRM_DESC);

		logger.verbose("Input - createException API :: " + XMLUtil.getXMLString(docCreateException));

		AcademyUtil.invokeAPI(env, AcademyConstants.API_CREATE_EXCEPTION, docCreateException);

		logger.endTimer("End - AcademyEGCRaiseShipPendingAlert.raisePendingConfirmationAlert() :: ");
	}

	private String getHoldResolvedDate(Document docGetOrdList, String strHoldTypeQry, String strStatus)
			throws Exception {

		logger.beginTimer("Begin - AcademyEGCRaiseShipPendingAlert.getHoldResolvedDate() :: ");

		String strHoldResolvedDate = null;

		NodeList nlOrderHoldType = XPathUtil.getNodeList(docGetOrdList,
				"/OrderList/Order/OrderHoldTypes/OrderHoldType[(" + strHoldTypeQry + ") and (@Status='" + strStatus
						+ "')]");

		if (nlOrderHoldType.getLength() > 0) {

			for (int i = 0; i < nlOrderHoldType.getLength(); i++) {

				Element eleOrderHoldType = (Element) nlOrderHoldType.item(i);

				logger.verbose("OrderHoldType Element :: " + XMLUtil.getElementXMLString(eleOrderHoldType));

				NodeList nlOrderHoldTypeLogs = eleOrderHoldType
						.getElementsByTagName(AcademyConstants.ELE_ORDER_HOLD_TYPE_LOG);

				String strModifyTs = null;

				if (nlOrderHoldTypeLogs.getLength() > 0) {

					for (int j = 0; j < nlOrderHoldTypeLogs.getLength(); j++) {

						Element eleOrderHoldTypeLog = (Element) nlOrderHoldTypeLogs.item(j);

						logger.verbose(
								"OrderHoldTypeLog Element :: " + XMLUtil.getElementXMLString(eleOrderHoldTypeLog));

						String strHoldStatus = eleOrderHoldTypeLog.getAttribute(AcademyConstants.ATTR_STATUS);

						if (strStatus.equals(strHoldStatus)) {
							strModifyTs = eleOrderHoldTypeLog.getAttribute(AcademyConstants.ATTR_MODIFY_TS);
						}
					}

				}

				if (StringUtil.isEmpty(strModifyTs)) {
					strModifyTs = eleOrderHoldType.getAttribute(AcademyConstants.ATTR_MODIFY_TS);
				}

				if (StringUtil.isEmpty(strHoldResolvedDate)) {
					strHoldResolvedDate = strModifyTs;
				} else if (compareTimestamps(strModifyTs, AcademyConstants.STR_CONDITION_AFTER, strHoldResolvedDate)) {
					strHoldResolvedDate = strModifyTs;
				}

			}
		}

		logger.endTimer("End - AcademyEGCRaiseShipPendingAlert.getHoldResolvedDate() :: ");

		return strHoldResolvedDate;
	}

	private boolean compareTimestamps(String strDateOne, String strAction, String strDateTwo) {

		logger.beginTimer("Begin - AcademyEGCRaiseShipPendingAlert.compareTimestamps() :: ");

		try {

			if (!StringUtil.isEmpty(strDateOne) && !StringUtil.isEmpty(strAction) && !StringUtil.isEmpty(strDateTwo)) {

				logger.verbose(
						"strDateOne:: " + strDateOne + " strAction:: " + strAction + " strDateTwo:: " + strDateTwo);

				SimpleDateFormat objSDF = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);

				Date dtDateOne = objSDF.parse(strDateOne);
				Date dtDateTwo = objSDF.parse(strDateTwo);

				Timestamp tsDateOne = new Timestamp(dtDateOne.getTime());
				Timestamp tsDateTwo = new Timestamp(dtDateTwo.getTime());

				switch (strAction) {

				case AcademyConstants.STR_CONDITION_BEFORE:
					return tsDateOne.before(tsDateTwo);

				case AcademyConstants.STR_CONDITION_AFTER:
					return tsDateOne.after(tsDateTwo);

				case AcademyConstants.STR_CONDITION_EQUALS:
					return tsDateOne.equals(tsDateTwo);
				}
			}

		} catch (Exception e) {
			logger.error("Exception caught - compareTimestamps() :: " + e);
		}

		logger.endTimer("End - AcademyEGCRaiseShipPendingAlert.compareTimestamps() :: ");

		return false;
	}

	private String getCurrentTimestampAsString() {

		logger.beginTimer("Begin - AcademyEGCRaiseShipPendingAlert.getCurrentTimestampAsString() :: ");

		String strCurrentTimestamp = null;

		try {

			SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			strCurrentTimestamp = sdf.format(new Date());

		} catch (Exception e) {
			logger.error("Exception caught - getCurrentTimestampAsString() ::" + e.getMessage());
		}

		logger.endTimer("End - AcademyEGCRaiseShipPendingAlert.getCurrentTimestampAsString() :: ");

		return strCurrentTimestamp;
	}

	private String addHoursToTime(String strDate, String strHours) {

		logger.beginTimer("Begin - AcademyEGCRaiseShipPendingAlert.addHoursToTime() :: ");

		String strDateModified = null;

		try {

			if (!StringUtil.isEmpty(strDate)) {

				SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
				int iHours = Integer.parseInt(strHours);

				Date objDate = sdf.parse(strDate);

				Calendar calendar = Calendar.getInstance();
				calendar.setTime(objDate);
				calendar.add(Calendar.HOUR_OF_DAY, iHours);

				strDateModified = sdf.format(calendar.getTime());
				logger.verbose("Modified date :: " + strDateModified);
			}

		} catch (Exception e) {
			logger.error("Exception caught - addHoursToTime() :: " + e);
		}

		logger.endTimer("End - AcademyEGCRaiseShipPendingAlert.addHoursToTime() :: ");

		return strDateModified;
	}

	private String retrieveCodeShortDesc(Document docOutGetCommonCodeList, String strCodeValue) {

		logger.beginTimer("Begin - AcademyEGCRaiseShipPendingAlert.retrieveCodeShortDesc() :: ");

		Element eleCommonCode = null;
		String strCodeShortDesc = null;

		try {

			if (!YFCObject.isNull(docOutGetCommonCodeList)) {

				logger.verbose("CommonCodeList output :: " + XMLUtil.getXMLString(docOutGetCommonCodeList));

				eleCommonCode = (Element) XPathUtil.getNode(docOutGetCommonCodeList,
						"/CommonCodeList/CommonCode[@CodeValue='" + strCodeValue + "']");

				if (!YFCObject.isNull(eleCommonCode)) {

					strCodeShortDesc = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
				}

			}
		} catch (Exception e) {
			logger.error("Exception caught - retrieveCodeShortDesc() :: " + e.getMessage());
		}

		logger.endTimer("End - AcademyEGCRaiseShipPendingAlert.retrieveCodeShortDesc() :: ");

		return strCodeShortDesc;
	}

	private String formHoldTypeQueryString(List<String> alOrderHoldTypes) throws Exception {

		logger.beginTimer("Begin - AcademyEGCRaiseShipPendingAlert.formHoldTypeQueryString() :: ");

		String strHoldTypeQuery = null;

		if (alOrderHoldTypes.size() == 1) {

			strHoldTypeQuery = "@HoldType='" + alOrderHoldTypes.get(0) + "'";

		} else if (alOrderHoldTypes.size() > 1) {

			for (int i = 0; i < alOrderHoldTypes.size(); i++) {

				if (i == 0) {
					strHoldTypeQuery = "@HoldType='" + alOrderHoldTypes.get(i) + "' ";
				} else {
					strHoldTypeQuery = strHoldTypeQuery + "or @HoldType='" + alOrderHoldTypes.get(i) + "' ";
				}

			}
		}

		logger.endTimer("End - AcademyEGCRaiseShipPendingAlert.formHoldTypeQueryString() :: ");

		return strHoldTypeQuery;

	}

	private List<String> retrieveCodeShortDescHolds(Document docOutGetCommonCodeList, String strCodeValue) {

		logger.beginTimer("Begin - AcademyEGCRaiseShipPendingAlert.retrieveCodeShortDescHolds() :: ");

		List<String> alHoldTypes = new ArrayList<String>();

		try {

			if (!YFCObject.isNull(docOutGetCommonCodeList)) {

				NodeList nlCodeValues = XPathUtil.getNodeList(docOutGetCommonCodeList,
						"/CommonCodeList/CommonCode[(contains(@CodeValue,'" + strCodeValue + "'))]");

				if (nlCodeValues.getLength() > 0) {

					for (int j = 0; j < nlCodeValues.getLength(); j++) {

						Element eleCommonCode = (Element) nlCodeValues.item(j);

						logger.verbose("Common Code" + XMLUtil.getElementXMLString(eleCommonCode));

						alHoldTypes.add(eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC));

					}
				}

			}
		} catch (Exception e) {
			logger.verbose("Exception caught - retrieveCodeShortDesc() :: " + e.getMessage());
		}

		logger.endTimer("End - AcademyEGCRaiseShipPendingAlert.retrieveCodeShortDescHolds() :: ");

		return alHoldTypes;
	}

}
