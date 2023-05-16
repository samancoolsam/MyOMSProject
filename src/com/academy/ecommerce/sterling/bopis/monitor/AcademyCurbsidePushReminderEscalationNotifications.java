package com.academy.ecommerce.sterling.bopis.monitor;

/**#########################################################################################
 *
 * Project Name                : OMS_NOV_26TH_2021_Rel13
 * Module                      : OMNI-54336 & 54337
 * Author                      : GunaSupriyaVeluru (C0023633) & ChoppavarapuDivyaSree (C0027758)
 * Author Group				   : CTS-POD
 * Date                        : 18-NOV-2021 
 * Description				   : This agent publishes Curbside Push Notifications(Reminder/Escalation) to ESB queue.
 * 								 
 * ---------------------------------------------------------------------------------
 * Date            Author         		Version#       		Remarks/Description                      
 * ---------------------------------------------------------------------------------
 * 18-NOV-2021		CTS  	 			  1.0           	Initial version
 *
 * #########################################################################################*/

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.text.SimpleDateFormat;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.ycp.japi.util.YCPBaseTaskAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyCurbsidePushReminderEscalationNotifications extends YCPBaseTaskAgent {

	private static final YFCLogCategory log = YFCLogCategory
			.instance(AcademyCurbsidePushReminderEscalationNotifications.class);

	/*
	 * This method invokes getShipmentList API to get the shipment details If
	 * Shipment status is in ReadyForCustomerPickup and Curbside is opted, then if
	 * task_q (available date - createTS) <= ReminderSLAMinutes push Reminder
	 * notification, update the task_q available date with System Date +
	 * EscalationSLAMinutes else push Escalation notification to MQQT through ESB.
	 */

	public Document executeTask(YFSEnvironment env, Document docInput) throws Exception {

		log.beginTimer("AcademyCurbsidePushReminderEscalationNotifications.executeTask() method");
		log.verbose(" Indoc XML \n" + XMLUtil.getXMLString(docInput));

		Document outDocgetShipmentList = null;
		Element eleTransactionFilters = null;
		Element eleShipment = null;
		String strCurrentShmtStatus = null;
		String strTaskQueueKey = null;
		String strTaskQAvailableDate = null;
		String strShipmentKey = null;
		String strTaskQCreatets = null;

		// Fetch the TaskQueue record attribute values
		strShipmentKey = docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_DATA_KEY);
		strTaskQueueKey = docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_TASK_Q_KEY);
		strTaskQAvailableDate = docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_AVAIL_DATE);
		strTaskQCreatets = docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_CREATETS);

		// Fetch the Agent criteria parameters
		eleTransactionFilters = (Element) docInput.getElementsByTagName(AcademyConstants.ATTR_TRANSACTION_FILTERS)
				.item(0);
		String strShipmentStatus = eleTransactionFilters.getAttribute(AcademyConstants.STR_SHIPMENT_STATUS);
		String strReminderSLAMinutes = eleTransactionFilters.getAttribute(AcademyConstants.STR_REMINDER_SLA_MINUTES);
		String strEscalationSLAMinutes = eleTransactionFilters
				.getAttribute(AcademyConstants.STR_ESCALATION_SLA_MINUTES);
		String strEscalationSLADays = eleTransactionFilters.getAttribute(AcademyConstants.STR_ESCALATION_SLA_DAYS);

		// Invoking the getShipmentList API for Shipment Details
		outDocgetShipmentList = getShipmentList(env, strShipmentKey);

		// Remove the taskQ entry, if getShipmentList API outDoc is empty
		if (outDocgetShipmentList.getDocumentElement().hasChildNodes()) {

			Element eleShipments = outDocgetShipmentList.getDocumentElement();
			eleShipment = SCXmlUtil.getChildElement(eleShipments, AcademyConstants.ELE_SHIPMENT);
			strCurrentShmtStatus = eleShipment.getAttribute(AcademyConstants.ATTR_STATUS);
			log.verbose("Shipment Current Status :: " + strCurrentShmtStatus);

			String strExtnIsCurbsidePickupOpted = XPathUtil.getString(outDocgetShipmentList,
					AcademyConstants.XPATH_EXTN_IS_CURBSIDE_PICKUP_OPTED);
			log.verbose("strExtnIsCurbsidePickupOpted :: " + strExtnIsCurbsidePickupOpted);

			// Type-cast StringArray to List<String> for fast iterator/compare
			String[] strShmtStatus = strShipmentStatus.split(AcademyConstants.STR_COMMA);
			List<String> listShmtStatus = Arrays.asList(strShmtStatus);

			SimpleDateFormat dateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			Calendar cal = Calendar.getInstance();
			int intEscalationDays = Integer.parseInt(strEscalationSLADays);
			log.verbose("intEscalationDays :: " + intEscalationDays);
			cal.add(Calendar.DATE, -(intEscalationDays));
			Date dtSysDateMinusEscalationSLADays = cal.getTime();
			String strSysDateMinusEscalationDays = dateFormat.format(dtSysDateMinusEscalationSLADays);
			log.verbose("System Date Minus EscalationDays :: " + strSysDateMinusEscalationDays);
			log.verbose("TaskQCreatets :: " + strTaskQCreatets);

			// if (Createts < SystemDate - EscalationSLADays)
			if (strTaskQCreatets.compareTo(strSysDateMinusEscalationDays) < 0) {
				// Updating TaskQ entry with High available date (2500-01-01 00:00:00.0)
				// manageTaskQueue API inDoc
				manageTaskQueueRecord(env, AcademyConstants.STR_BLANK, strTaskQueueKey);
			}

			// Shipment current status should be 1100.70.06.30.5(ReadyForCustomerPickUp) and
			// Curbside is opted
			// if not Remove the Entry from taskQ table.
			else if (listShmtStatus.contains(strCurrentShmtStatus) && !YFCObject.isVoid(strExtnIsCurbsidePickupOpted)
					&& strExtnIsCurbsidePickupOpted.equals(AcademyConstants.STR_YES)) {
				log.verbose("Shipment current status is " + strCurrentShmtStatus + " and curbside is opted");

				Date dtTaskQAvailableDate = dateFormat.parse(strTaskQAvailableDate);
				log.verbose("TaskQ AvailableDate :: " + dtTaskQAvailableDate);
				Date dtTaskQCreatets = dateFormat.parse(strTaskQCreatets);
				log.verbose("TaskQ Createts :: " + dtTaskQCreatets);

				long timeDiffOfAvailableDateMinusCreatets = Math
						.abs(dtTaskQAvailableDate.getTime() - dtTaskQCreatets.getTime());
				log.verbose("Time difference of AvailableDate and Createts in milliseconds:: "
						+ timeDiffOfAvailableDateMinusCreatets);

				int minDiffOfAvailableDateMinusCreatets = (int) TimeUnit.MINUTES
						.convert(timeDiffOfAvailableDateMinusCreatets, TimeUnit.MILLISECONDS);
				log.verbose("Minute difference of AvailableDate and Createts : " + minDiffOfAvailableDateMinusCreatets);

				// if AvailableDate - createts <= ReminderSLAMinutes (10)
				if (minDiffOfAvailableDateMinusCreatets <= Integer.parseInt(strReminderSLAMinutes)) {
					log.verbose("Invoking AcademySendCurbsideReminderNotification service, docInput :\n"
							+ XMLUtil.getXMLString(outDocgetShipmentList));
					AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACAD_SEND_CURBSIDE_REMINDER_NOTIFICATION,
							outDocgetShipmentList);

					// Updating TaskQ entry with next available date
					manageTaskQueueRecord(env, strEscalationSLAMinutes, strTaskQueueKey);
				}
				// if AvailableDate - createts >= EscalationSLAMinutes (5)
				else if (minDiffOfAvailableDateMinusCreatets >= Integer.parseInt(strEscalationSLAMinutes)) {
					log.verbose("Invoking AcademySendCurbsideEscalationNotification service, docInput :\n"
							+ XMLUtil.getXMLString(outDocgetShipmentList));
					AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACAD_SEND_CURBSIDE_ESCALATION_NOTIFICATION,
							outDocgetShipmentList);

					// Remove taskQ entry, after escalation notification is sent
					removeTaskQueueRecord(env, strTaskQueueKey);
				}
			} else {

				// Remove Entry from taskQ table where shipment current status is not in
				// 1100.70.06.30.5(ReadyForCustomerPickUp) and curbside is not opted
				removeTaskQueueRecord(env, strTaskQueueKey);
			}
		} else {

			// Remove taskQ entry if shipmentKey is invalid
			removeTaskQueueRecord(env, strTaskQueueKey);
		}
		log.endTimer("AcademyCurbsidePushReminderEscalationNotifications.executeTask() method");
		return docInput;
	}

	/**
	 * This method executes getShipmentList API to fetch the shipment details.
	 * Sample Input XML <Shipment ShipmentKey="201910230103162120810024"/>
	 * 
	 * @param env
	 * @param ShipmentKey
	 * @return outDoc
	 * @throws Exception
	 **/
	private Document getShipmentList(YFSEnvironment env, String strShipmentKey) throws Exception {

		log.beginTimer("AcademyCurbsidePushReminderEscalationNotifications.getShipmentList() method");

		Document docIngetShipmentList = null;
		Document docOutgetShipmentList = null;

		// getShipmentList API inDoc
		docIngetShipmentList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		docIngetShipmentList.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);

		// getShipmentList API Template
		Document docgetShipmentListTemplate = XMLUtil.getDocument(
				"<Shipments>" + "<Shipment ShipmentNo=\"\" OrderNo=\"\" Status=\"\"  ShipNode=\"\" DeliveryMethod=\"\">"
						+ "<Extn ExtnIsCurbsidePickupOpted=\"\" />" + "</Shipment>" + "</Shipments>");
		log.verbose("getShipmentList API indoc XML : \n" + XMLUtil.getXMLString(docIngetShipmentList));

		// Invoking the getShipmentList API
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, docgetShipmentListTemplate);
		docOutgetShipmentList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST,
				docIngetShipmentList);
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);

		log.verbose("Outdoc of getShipmentList :: \n" + XMLUtil.getXMLString(docOutgetShipmentList));
		log.endTimer("AcademyCurbsidePushReminderEscalationNotifications.getShipmentList() method");

		return docOutgetShipmentList;
	}

	/**
	 * This method executes manageTaskQueue API to update the taskQ
	 * entry(AvailableDate)
	 * 
	 * Sample Input XML <TaskQueue AvailableDate="2019-10-25T05:45:02" TaskQKey=
	 * "201910230332062120822109" Operation="Modify"/>
	 * 
	 * @param env
	 * @param strEscalationSLAMinutes
	 * @param strTaskQueueKey
	 */
	private void manageTaskQueueRecord(YFSEnvironment env, String strEscalationSLAMinutes, String strTaskQueueKey)
			throws Exception {

		log.beginTimer("AcademyCurbsidePushReminderEscalationNotifications.manageTaskQueueRecord() method");

		Document manageTaskQueueinDoc = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);

		// Fetch the System time and Add the EscalationSLAMinutes (configured value)
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdfDateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
		log.verbose("manageTaskQueueRecord() SystemDate :: " + sdfDateFormat.format(cal.getTime()));
		log.verbose("manageTaskQueueRecord() strEscalationSLAMinutes :: " + strEscalationSLAMinutes);

		// manageTaskQueue API inDoc
		manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_OPERATION,
				AcademyConstants.STR_ACTION_MODIFY);
		manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_TASK_Q_KEY, strTaskQueueKey);

		if (!YFCObject.isVoid(strEscalationSLAMinutes)) {
			cal.add(Calendar.MINUTE, Integer.parseInt(strEscalationSLAMinutes));
			String strSystemDatePlusSLAMnts = sdfDateFormat.format(cal.getTime());
			log.verbose("manageTaskQueueRecord() strSystemDatePlusSLA :: " + strSystemDatePlusSLAMnts);
			manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_AVAIL_DATE,
					strSystemDatePlusSLAMnts);
		} else {
			manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_AVAIL_DATE,
					AcademyConstants.FINAL_AVAILABLE_DATE);
		}

		// Invoking the manageTaskQueue API
		log.verbose("Input to manageTaskQueue API :: " + XMLUtil.getXMLString(manageTaskQueueinDoc));
		AcademyUtil.invokeAPI(env, AcademyConstants.API_MANAGE_TASK_QUEUE, manageTaskQueueinDoc);

		log.endTimer("AcademyCurbsidePushReminderEscalationNotifications.manageTaskQueueRecord() method");
	}

	/**
	 * This method executes manageTaskQueue API to remove processed shipments Sample
	 * Input XML <TaskQueue Operation="Delete" TaskQKey="2018090110050322699633"/>
	 * 
	 * @param env
	 * @param strTaskQueueKey
	 */
	private void removeTaskQueueRecord(YFSEnvironment env, String strTaskQueueKey) throws Exception {

		log.beginTimer("AcademyCurbsidePushReminderEscalationNotifications.removeTaskQueueRecord() method");

		Document manageTaskQueueinDoc = null;

		// manageTaskQueue API inDoc
		manageTaskQueueinDoc = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
		manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_OPERATION,
				AcademyConstants.STR_OPERATION_VAL_DELETE);
		manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_TASK_Q_KEY, strTaskQueueKey);

		// Invoking the manageTaskQueue API
		log.verbose("Indoc to manageTaskQueue API :: " + XMLUtil.getXMLString(manageTaskQueueinDoc));
		AcademyUtil.invokeAPI(env, AcademyConstants.API_MANAGE_TASK_QUEUE, manageTaskQueueinDoc);

		log.endTimer("AcademyCurbsidePushReminderEscalationNotifications.removeTaskQueueRecord() method");
	}

}
