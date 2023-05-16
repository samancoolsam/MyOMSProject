package com.academy.ecommerce.sterling.bopis.monitor;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.axis.utils.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.los.XMLUtil;
import com.academy.util.common.AcademyBOPISUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sun.tools.internal.ws.util.xml.XmlUtil;
import com.yantra.ycp.japi.util.YCPBaseTaskAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfs.japi.YFSEnvironment;

/*##################################################################################
*
* Project Name                : POD February Release
* Module                      : OMS
* Author                      : CTS
* Date                        : 14-FEB-2020
* Description                 : This file implements the logic to 
* 								1. Send Customer Pick reminder email & SMS every 'x' (Reminder SLA) hours
* 									once it is in 'Ready For Customer Pick'.
*                               2. Cancel BOPIS shipments once cancellation SLA is met.
*                               3. Eligible statuses for Reminder & cancellation are configurable.
*								4. Actions Send Reminders & Cancellation are enables based on the argument.
*
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author                  Version#       Remarks/Description                     
* ---------------------------------------------------------------------------------
* 14-FEB-2020     CTS                      1.0            Initial version
* 10-APR-2020     CTS                      1.1            Updated for Last reminder Email
* 10-AUG-2022     Everest                  1.2            Updated code to handle Order level Emails
* ##################################################################################*/

public class AcademyBOPISCustomShipmentMonitor extends YCPBaseTaskAgent {

	private static Logger logger = Logger.getLogger(AcademyBOPISCustomShipmentMonitor.class.getName());

	public Document executeTask(YFSEnvironment envObj, Document docInput) {

		logger.verbose("AcademyBOPISCustomerPickReminderAndCancellation.executeTask() :: ");
		logger.verbose("executeTask() - Input document :: " + XMLUtil.getString(docInput));

		Document docShipmentDetails = null;
		Document docOutGetCommonCodeList = null;

		Element eleInput = null;

		String strShipmentKey = null;
		//Start : OMNI-74228 : BOPIS/STS – Consolidated Pickup Reminders Emails
		String strOrderHeaderKey = null;
		//End : OMNI-74228 : BOPIS/STS – Consolidated Pickup Reminders Emails

		String strTaskQKey = null;
		String strShipmentStatus = null;
		String strMaxCustPickDate = null;
		String strNextAvailableDate = null;
		String strCurrentTimestamp = null;

		// List of Cancellable statuses - Agent Criteria Parameter
		String strCancellableStatuses = null;

		// Reminder interval from CommonCode
		String strReminderInterval = null;

		// List of statuses for Reminder email/SMS - Agent Criteria Parameter
		String strEligibleStatusesReminder = null;

		// Switch to enable cancellation once SLA is met
		String strEnableCancellation = null;

		// Switch to send reminder email/SMS in every interval
		String strEnableReminder = null;

		// FinalReminderTimestamp
		String strFinalReminderTimestamp = null;

		// AvailableTimestamp - Task Queue
		String strAvailableTimestamp = null;

		String strFinalReminderBufferDays = null;
		String strFinalReminderTime = null;
		String strCancelBufferDays = null;
		String strCancelTime = null;
		String strEnableFinalReminder = null;
		String strSTHSOFSMSRequired = null;
		try {

			eleInput = docInput.getDocumentElement();
			//Start : OMNI-74228 : BOPIS/STS – Consolidated Pickup Reminders Emails
			String strDataType = eleInput.getAttribute(AcademyConstants.ATTR_DATA_TYPE); 
			if (!YFCObject.isVoid(strDataType) && AcademyConstants.ATTR_ORDER_HEADER_KEY.equals(strDataType)) {
				strOrderHeaderKey = eleInput.getAttribute(AcademyConstants.ATTR_DATA_KEY);
			} else {
				strShipmentKey = eleInput.getAttribute(AcademyConstants.ATTR_DATA_KEY);
			}
			//End : OMNI-74228 : BOPIS/STS – Consolidated Pickup Reminders Emails

			strTaskQKey = eleInput.getAttribute(AcademyConstants.ATTR_TASK_Q_KEY);
			strCancellableStatuses = XMLUtil.getAttributeFromXPath(docInput,
					AcademyConstants.XPATH_TASK_Q_TXN_CANCELLABLE_STATUS);
			strEligibleStatusesReminder = XMLUtil.getAttributeFromXPath(docInput,
					AcademyConstants.XPATH_TASK_Q_TXN_REMINDER_STATUS);
			strEnableCancellation = XMLUtil.getAttributeFromXPath(docInput,
					AcademyConstants.XPATH_TASK_Q_TXN_ENABLE_CANCELLATION);
			strEnableReminder = XMLUtil.getAttributeFromXPath(docInput,
					AcademyConstants.XPATH_TASK_Q_TXN_ENABLE_REMINDER);
			strEnableFinalReminder = XMLUtil.getAttributeFromXPath(docInput,
					AcademyConstants.XPATH_TASK_Q_TXN_ENABLE_FINAL_REMINDER);
			strAvailableTimestamp = eleInput.getAttribute(AcademyConstants.ATTR_AVAIL_DATE);
			strSTHSOFSMSRequired = XMLUtil.getAttributeFromXPath(docInput,
					"/TaskQueue/TransactionFilters/@STHSOFSMSRequired");
			
			// Start OMNI-56589
			List<String> liCancellableStatuses = new ArrayList<String>();
			List<String> liReminderStatuses = new ArrayList<String>();
			
			if (!StringUtil.isEmpty(strCancellableStatuses)) {
				String[] arrCancellableStatuses = strCancellableStatuses.split(",");
				liCancellableStatuses = Arrays.asList(arrCancellableStatuses);
			}
			
			if (!StringUtil.isEmpty(strCancellableStatuses)) {
				String[] arrReminderStatuses = strEligibleStatusesReminder.split(",");
				liReminderStatuses = Arrays.asList(arrReminderStatuses);
			}
			// End OMNI-56589

			/*
			 * Shipment details under MonitorConsolidation Tag Similar to ShipmentMonitor
			 * agent
			 */
			
			//Changes for OMNI-63471 Start
			//Start : OMNI-74228 : BOPIS/STS – Consolidated Pickup Reminders Emails
			 docOutGetCommonCodeList = AcademyBOPISUtil.getCommonCodeList(envObj, AcademyConstants.STR_BOPIS_CUSTOMER_PICKUP_SLA, AcademyConstants.PRIMARY_ENTERPRISE);
			//End : OMNI-74228 : BOPIS/STS – Consolidated Pickup Reminders Emails
			//logger.verbose("checkIfBOPISEmailNotToBeSent->docOutGetCommonCodeList"+XMLUtil.getXMLString(docOutGetCommonCodeList));
			String strStartTime = AcademyBOPISUtil.retrieveCodeShortDesc(envObj, docOutGetCommonCodeList, "BOPIS_COM_STOP_START_TIME");
			String strEndTime = AcademyBOPISUtil.retrieveCodeShortDesc(envObj, docOutGetCommonCodeList, "BOPIS_COM_STOP_END_TIME");
			String strEndHours = AcademyBOPISUtil.retrieveCodeShortDesc(envObj, docOutGetCommonCodeList, "BOPIS_COM_END_HOUR");
			//Start : OMNI-74228 : BOPIS/STS – Consolidated Pickup Reminders Emails
			String strConsolidationRemdrTime = AcademyBOPISUtil.retrieveCodeShortDesc(envObj, docOutGetCommonCodeList, AcademyConstants.STR_REMINDER_CUTOFF);
			logger.verbose("The Start time:" +strStartTime+ "End time:" +strEndTime+ "EndHours:" +strEndHours);
			//End : OMNI-74228 : BOPIS/STS – Consolidated Pickup Reminders Emails
			
			if(AcademyBOPISUtil.checkIfBOPISEmailNotToBeSent(envObj,strStartTime,strEndTime)) 
			{
			    Calendar cal = Calendar.getInstance();
			    int nowHour = cal.get(Calendar.HOUR_OF_DAY);
			    int nowMin  = cal.get(Calendar.MINUTE);
			    logger.verbose("nowHour "+nowHour+" nowMin "+nowMin+" strEndHours "+strEndHours);
				String strDealay = AcademyBOPISUtil.getHoursToAddForBOPISEmail(nowHour, nowMin, strEndHours);
				String sTemp[] = strDealay.split(":");
				String strHour=null;
				String strMin = null;
				
				if(sTemp.length > 0)
				{
					strHour = sTemp[0];
					strMin = sTemp[1];
				}
				AcademyBOPISUtil.putAheadAvailableDateInTaskQForBOPIS(envObj, strTaskQKey, Integer.parseInt(strHour), Integer.parseInt(strMin));
				logger.verbose("::: Email will be sent after "+strHour+" Hours ::: "+" Minutes ::: "+strMin+" "+" sTemp "+sTemp);
				return docInput;
			}
			//Changes for OMNI-63471 End
			
			//Start : OMNI-74228 : BOPIS/STS – Consolidated Pickup Reminders Emails
			if(!YFCObject.isVoid(strOrderHeaderKey)) {
				docShipmentDetails = prepareAndInvokeGetShipmentListForOrder(envObj, strOrderHeaderKey);
				if (!YFCObject.isVoid(docShipmentDetails)) {
					docShipmentDetails.getDocumentElement().setAttribute(
							AcademyConstants.STR_IS_REMINDER_CONSOLIDATION_ENABLED, AcademyConstants.STR_YES);
				}				
			}
			else {
				docShipmentDetails = prepareAndInvokeGetShipmentList(envObj, strShipmentKey);
			}
			
			//Start : OMNI-43224: Pick SLA dates to be updated for SOF
			//docShipmentDetails = prepareAndInvokeGetShipmentList(envObj, strShipmentKey);
			String strFulfillmentType =  SCXmlUtil.getXpathAttribute(docShipmentDetails.getDocumentElement(), 
						"Shipment/ShipmentLines/ShipmentLine/OrderLine[@FulfillmentType='SOF']/@FulfillmentType");
			logger.verbose("The fulfillmentType ::" +strFulfillmentType);
			//End : OMNI-74228 : BOPIS/STS – Consolidated Pickup Reminders Emails
			//Start : OMNI-93537 : DSV SOF- Consolidated Pickup Reminders Emails
			 String strDeliveryMethod = SCXmlUtil.getXpathAttribute(
                         docShipmentDetails.getDocumentElement(), "Shipment/@DeliveryMethod");
             logger.verbose("The DeliveryMethod ::" + strDeliveryMethod);
			//End : OMNI-93537 : DSV SOF- Consolidated Pickup Reminders Emails
			docShipmentDetails.getDocumentElement().setAttribute("STHSOFSMSRequired", strSTHSOFSMSRequired);
			//Start : OMNI-43224: Pick SLA dates to be updated for SOF
			if (!YFCObject.isNull(docShipmentDetails)) {

				logger.verbose("Shipment Details :: " + XMLUtil.getString(docShipmentDetails));

				strShipmentStatus = XMLUtil.getAttributeFromXPath(docShipmentDetails,
						AcademyConstants.XPATH_MONITOR_SHIPMENT_STATUS);
				logger.verbose("The Shipment status ::" +strShipmentStatus);

				/*
				 * If shipment status is matches with any of Reminder or Cancellation statuses
				 * configured under agent criteria parameter, then proceeds further
				 */

				/*
				 * OMNI-56589
				 * Changed from String contains to ArrayList contains
				 * */
				if ((!StringUtil.isEmpty(strCancellableStatuses) && liCancellableStatuses.contains(strShipmentStatus))
						|| (!StringUtil.isEmpty(strEligibleStatusesReminder)
								&& liReminderStatuses.contains(strShipmentStatus))) {
				//Start : OMNI-74228 : BOPIS/STS – Consolidated Pickup Reminders Emails

				//	docOutGetCommonCodeList = AcademyBOPISUtil.getCommonCodeList(envObj,
					//		AcademyConstants.STR_BOPIS_CUSTOMER_PICKUP_SLA, AcademyConstants.PRIMARY_ENTERPRISE);
				//End : OMNI-74228 : BOPIS/STS – Consolidated Pickup Reminders Emails

					//Start : OMNI-43224: Pick SLA dates to be updated for SOF
					if("SOF".equals(strFulfillmentType) && AcademyConstants.STR_SHIP_DELIVERY_METHOD.equals(strDeliveryMethod)) //OMNI-93537
					{
						strReminderInterval = retrieveCodeShortDesc(envObj, docOutGetCommonCodeList,
								AcademyConstants.STR_SOF_PICKUP_REMINDER_SLA);
					}
					else
					{
						strReminderInterval = retrieveCodeShortDesc(envObj, docOutGetCommonCodeList,
								AcademyConstants.STR_BOPIS_PICKUP_REMINDER_SLA);
					}
					//Start : OMNI-43224: Pick SLA dates to be updated for SOF
					strMaxCustPickDate = XMLUtil.getAttributeFromXPath(docShipmentDetails,
							AcademyConstants.XPATH_BOPIS_MAX_PICK_DATE_SHIPMENT);

					strCurrentTimestamp = getCurrentTimestampAsString();

					strFinalReminderBufferDays = retrieveCodeShortDesc(envObj, docOutGetCommonCodeList,
							AcademyConstants.STR_BOPIS_FINAL_REMINDER_DAYS);

					strFinalReminderTime = retrieveCodeShortDesc(envObj, docOutGetCommonCodeList,
							AcademyConstants.STR_BOPIS_FINAL_REMINDER_TIME);

					strCancelBufferDays = retrieveCodeShortDesc(envObj, docOutGetCommonCodeList,
							AcademyConstants.STR_BOPIS_CANCEL_BUFFER_DAYS);

					strCancelTime = retrieveCodeShortDesc(envObj, docOutGetCommonCodeList,
							AcademyConstants.STR_BOPIS_CANCEL_BUFFER_TIME);

					strFinalReminderTimestamp = calculateBufferTime(strMaxCustPickDate, strFinalReminderBufferDays,
							strFinalReminderTime);
					//Start : OMNI-74228 : BOPIS/STS – Consolidated Pickup Reminders Emails

					logger.verbose("The Reminder Interval:: " +strReminderInterval);
					logger.verbose(":: strCurrentTimestamp :: " + strCurrentTimestamp);
					logger.verbose(":: strMaxCustPickDate :: " + strMaxCustPickDate);
					logger.verbose(":: strFinalReminderTimestamp :: " + strFinalReminderTimestamp);
					logger.verbose(":: strEnableFinalReminder :: " + strEnableFinalReminder);
								
					//End : OMNI-74228 : BOPIS/STS – Consolidated Pickup Reminders Emails

					// Check if Shipment is eligible for Final Cancellation Email and Text
					if ((compareTimestamps(strAvailableTimestamp, AcademyConstants.STR_CONDITION_EQUALS,
							strFinalReminderTimestamp)
							|| strCurrentTimestamp.substring(0, 15).equals(strFinalReminderTimestamp.substring(0, 15)))
							&& !StringUtil.isEmpty(strEligibleStatusesReminder)
							&& liReminderStatuses.contains(strShipmentStatus)
							&& AcademyConstants.STR_YES.equalsIgnoreCase(strEnableFinalReminder)) {

						logger.verbose("Invoking service to trigger BOPIS final customer pick reminder :: ");
						Element eleShipment = (Element) docShipmentDetails
								.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);
						eleShipment.setAttribute(AcademyConstants.ATTR_FINAL_REMINDER, AcademyConstants.STR_YES);

						logger.verbose(
								"Shipment Details after modification :: " + XMLUtil.getString(docShipmentDetails));
						//Start : OMNI-43224: Pick SLA dates to be updated for SOF
						//Start OMNI-93522 DSV SOF Email Consolidation
						
						if(AcademyConstants.STR_PICK_DELIVERY_METHOD.equals(strDeliveryMethod))
                        {        
                        	AcademyUtil.invokeService(envObj, AcademyConstants.SERVICE_BOPIS_SHIPMENT_CUST_PICK_REMINDER,
									docShipmentDetails);
						
                        }
						else
						{
							AcademyUtil.invokeService(envObj, "AcademySOFCheckAndSendSMS",
                                    docShipmentDetails);
						}
						//End OMNI-93522 DSV SOF Email Consolidation
						//Start : OMNI-43224: Pick SLA dates to be updated for SOF
						strMaxCustPickDate = calculateBufferTime(strMaxCustPickDate, strCancelBufferDays,
								strCancelTime);
						logger.verbose("The MaxCustPickDate for final cancellation::" +strMaxCustPickDate);

						invokeManageTaskQueueAPI(envObj, strTaskQKey, strMaxCustPickDate, null);
					}

					/*
					 * Trigger reminder email and SMS & update Task Queue entry with next available
					 * date based on
					 * 
					 * 1. Current time stamp is before SLA 2. Shipment status should match with one
					 * of reminder eligible statuses listed 3. Switch to trigger reminder should be
					 * 'Y' or 'y'
					 * 
					 * for BOPIS REGULAR SHIPMENTS
					 */

					else if (compareTimestamps(strCurrentTimestamp, AcademyConstants.STR_CONDITION_BEFORE,
							strFinalReminderTimestamp) && !StringUtil.isEmpty(strEligibleStatusesReminder)
							&& liReminderStatuses.contains(strShipmentStatus)
							&& AcademyConstants.STR_YES.equalsIgnoreCase(strEnableReminder)) {
						// Shipment is eligile for a Reminder Emails. Triggerd before Final Reminder
						// Email
						logger.verbose("Invoking service to trigger BOPIS customer pick reminder :: ");
						//Start : OMNI-43224: Pick SLA dates to be updated for SOF
						//Start OMNI-93522 DSV SOF Email Consolidation
						
						if(AcademyConstants.STR_PICK_DELIVERY_METHOD.equals(strDeliveryMethod))
                        {        
                        	AcademyUtil.invokeService(envObj, AcademyConstants.SERVICE_BOPIS_SHIPMENT_CUST_PICK_REMINDER,
									docShipmentDetails);
						
                        }
						else
						{
							AcademyUtil.invokeService(envObj, "AcademySOFCheckAndSendSMS",
                                    docShipmentDetails);
						}
						//End OMNI-93522 DSV SOF Email Consolidation
						//Start : OMNI-43224: Pick SLA dates to be updated for SOF
						strNextAvailableDate = addHoursToTime(strCurrentTimestamp, strReminderInterval, strDataType, strConsolidationRemdrTime);
						logger.verbose(":: strNextAvailableDate for reminder:: " + strNextAvailableDate);
						logger.verbose(":: strFinalReminderTimestamp :: " + strFinalReminderTimestamp);

						// Shipment eligible for another reminder email
						if (AcademyConstants.STR_YES.equalsIgnoreCase(strEnableFinalReminder)
								&& (compareTimestamps(strNextAvailableDate, AcademyConstants.STR_CONDITION_EQUALS,
										strFinalReminderTimestamp)
										|| compareTimestamps(strNextAvailableDate, AcademyConstants.STR_CONDITION_AFTER,
												strFinalReminderTimestamp))) {
							logger.verbose(" Shipment Eligible for Last Reminder email ");
							invokeManageTaskQueueAPI(envObj, strTaskQKey, strFinalReminderTimestamp, null);
						}
						// Shipment Eligible for cancellation
						else if (compareTimestamps(strNextAvailableDate, AcademyConstants.STR_CONDITION_AFTER,
								strMaxCustPickDate)) {
							logger.verbose(" Shipment Eligible for Cancellation ");
							strMaxCustPickDate = calculateBufferTime(strMaxCustPickDate, strCancelBufferDays,
									strCancelTime);
							logger.verbose("The MaxCustPickDAte for cancellation::" +strMaxCustPickDate);
							invokeManageTaskQueueAPI(envObj, strTaskQKey, strMaxCustPickDate, null);

						} else {
							logger.verbose(" Shipment Eligible for another reminder email");
							invokeManageTaskQueueAPI(envObj, strTaskQKey, strNextAvailableDate, null);
						}
					}

					/*
					 * Trigger Shipment cancellation & remove Task Queue entry based on
					 * 
					 * 1. Current time stamp is on or after SLA 2. Shipment status should match with
					 * one of cancellable statuses listed 3. The switch to trigger cancellation
					 * should be 'Y' or 'y'
					 * 
					 * for BOPIS REGULAR/FIREARM SHIPMENTS
					 */

					else if ((compareTimestamps(strCurrentTimestamp, AcademyConstants.STR_CONDITION_AFTER,
							strMaxCustPickDate)
							|| compareTimestamps(strCurrentTimestamp, AcademyConstants.STR_CONDITION_EQUALS,
									strMaxCustPickDate))
							&& AcademyConstants.STR_YES.equalsIgnoreCase(strEnableCancellation)
							&& !StringUtil.isEmpty(strCancellableStatuses)
							&& liCancellableStatuses.contains(strShipmentStatus)) {

						logger.verbose("Invoking service to trigger BOPIS shipment cancellation :: ");
						//Start : OMNI-43224: Pick SLA dates to be updated for SOF
						//Start : OMNI-82023 : Consolidating the Cancellations at Order Level.
						if(AcademyConstants.ATTR_ORDER_HEADER_KEY.equals(strDataType)) {
							logger.verbose("Entered orderlevel cancellation");
							AcademyUtil.invokeService(envObj, "AcademyBOPISOrderLevelCustomerAbandonmentCancel", docShipmentDetails);
						}
						//End : OMNI-82023 : Consolidating the Cancellations at Order Level.
						else if(!"SOF".equals(strFulfillmentType)){
							AcademyUtil.invokeService(envObj, AcademyConstants.SERVICE_BOPIS_SHIPMENT_CANCELLATION,
								docShipmentDetails);
						}
						//Start : OMNI-43224: Pick SLA dates to be updated for SOF
						logger.verbose("Removing TaskQueue entry after Cancellation :: ");
						invokeManageTaskQueueAPI(envObj, strTaskQKey, null, AcademyConstants.STR_ACTION_DELETE);

					} else if (compareTimestamps(strCurrentTimestamp, AcademyConstants.STR_CONDITION_BEFORE,
							strMaxCustPickDate) && AcademyConstants.STR_YES.equalsIgnoreCase(strEnableCancellation)
							&& !StringUtil.isEmpty(strCancellableStatuses)
							&& liCancellableStatuses.contains(strShipmentStatus)) {
						
						logger.verbose(" Calculate next trigger interval ::  ");
						//Start : OMNI-74228 : BOPIS/STS – Consolidated Pickup Reminders Emails
						strNextAvailableDate = addHoursToTime(strCurrentTimestamp, strReminderInterval, strDataType, strConsolidationRemdrTime);
						//End : OMNI-74228 : BOPIS/STS – Consolidated Pickup Reminders Emails

						logger.verbose(":: strNextAvailableDate :: " + strNextAvailableDate);
						
						if (compareTimestamps(strNextAvailableDate, AcademyConstants.STR_CONDITION_AFTER,
								strMaxCustPickDate)) {
							logger.verbose(" Shipment Eligible for Cancellation, stamping max pickup date :: ");
							strMaxCustPickDate = calculateBufferTime(strMaxCustPickDate, strCancelBufferDays,
									strCancelTime);
							invokeManageTaskQueueAPI(envObj, strTaskQKey, strMaxCustPickDate, null);

						} else {
							logger.verbose(" Setting an interval to check eligibility :: ");
							invokeManageTaskQueueAPI(envObj, strTaskQKey, strNextAvailableDate, null);
						}
					} else {

						logger.verbose(" Exception Case. Not eligible for Reminder Emails. To retry after some time ");
						//Start : OMNI-74228 : BOPIS/STS – Consolidated Pickup Reminders Emails
						strNextAvailableDate = addHoursToTime(strCurrentTimestamp, strReminderInterval, strDataType, strConsolidationRemdrTime);
						//End : OMNI-74228 : BOPIS/STS – Consolidated Pickup Reminders Emails
						logger.verbose(":: strNextAvailableDate :: " + strNextAvailableDate);
						if (compareTimestamps(strNextAvailableDate, AcademyConstants.STR_CONDITION_AFTER,
								strMaxCustPickDate)) {
							logger.verbose(" Shipment Eligible for Cancellation ");
							strMaxCustPickDate = calculateBufferTime(strMaxCustPickDate, strCancelBufferDays,
									strCancelTime);
							invokeManageTaskQueueAPI(envObj, strTaskQKey, strMaxCustPickDate, null);

						} else {
							logger.verbose(" Shipment Eligible for another reminder email");
							invokeManageTaskQueueAPI(envObj, strTaskQKey, strNextAvailableDate, null);
						}

					}

				}

				// Remove TaskQueue entry if shipment is not in any of eligible statuses
				else {
					logger.verbose("Removing TaskQueue entry :: ");
					invokeManageTaskQueueAPI(envObj, strTaskQKey, null, AcademyConstants.STR_ACTION_DELETE);
				}

			}

		} catch (Exception e) {
			logger.error("Exception caught - executeTask( ) :: " + e);
		}

		return docInput;
	}

	/*
	 * Method prepareAndInvokeGetShipmentList() :: invoke getShipmentList append
	 * output to <MonitorConsolidation> element.
	 */

	private Document prepareAndInvokeGetShipmentList(YFSEnvironment envObj, String strShipmentKey) {

		Document docInGetShipmentList = null;
		Document docOutGetShipmentList = null;
		Document docOutMonitorConsolidation = null;
		Element eleOutShipment = null;
		Element eleInShipment = null;

		try {

			if (!StringUtils.isEmpty(strShipmentKey)) {

				docInGetShipmentList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
				eleInShipment = docInGetShipmentList.getDocumentElement();
				eleInShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);

				logger.verbose("getShipmentList input::" + XMLUtil.getString(docInGetShipmentList));

				envObj.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST,
						AcademyConstants.STR_TEMPLATEFILE_GET_SHIPMENT_LIST_BOPIS);

				docOutGetShipmentList = AcademyUtil.invokeAPI(envObj, AcademyConstants.API_GET_SHIPMENT_LIST,
						docInGetShipmentList);

				envObj.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);

				if (!YFCObject.isNull(docOutGetShipmentList)) {

					logger.verbose("getShipmentList output::" + XMLUtil.getString(docOutGetShipmentList));

					eleOutShipment = (Element) docOutGetShipmentList.getElementsByTagName(AcademyConstants.ELE_SHIPMENT)
							.item(0);
					if (!YFCObject.isNull(eleOutShipment)) {

						docOutMonitorConsolidation = XMLUtil.createDocument(AcademyConstants.ELE_MONITOR_CONSOLIDATION);
						Element eleMonitorConsolidation = docOutMonitorConsolidation.getDocumentElement();
						com.academy.util.xml.XMLUtil.importElement(eleMonitorConsolidation, eleOutShipment);

					}

				}

			}

		} catch (Exception e) {
			logger.error("Exception caught - invokeGetShipmentList() :: " + e);
		}

		return docOutMonitorConsolidation;
	}

	/*
	 * Method compareTimestamps() :: to compare the time provided based on action
	 */
	private boolean compareTimestamps(String strDateOne, String strAction, String strDateTwo) {

		logger.verbose("Start execution - compareTimestamp() :: ");

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

		return false;
	}

	/*
	 * Method getCurrentTimestampAsString() :: returns current timestamp as string
	 */
	private String getCurrentTimestampAsString() {

		String strCurrentTimestamp = null;
		try {

			SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			strCurrentTimestamp = sdf.format(new Date());

		} catch (Exception e) {
			logger.error("Exception caught - getCurrentTimestampAsString() ::" + e);
		}

		return strCurrentTimestamp;
	}

	/*
	 * Method addHoursToTime() :: add hours to the time given and return the result
	 */
	private String addHoursToTime(String strDate, String strHours, String strDataType, String strConsolidationRemdrTime) {
		logger.verbose("Begin - addHoursToTime() :: ");
		String strDateModified = null;
		try {						
			SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			int iHours = Integer.parseInt(strHours);
			Date objDate = sdf.parse(strDate);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(objDate);
			calendar.add(Calendar.HOUR_OF_DAY, iHours);
			//Start : OMNI-74228 : BOPIS/STS – Consolidated Pickup Reminders Emails

			if (!YFCObject.isVoid(strDataType) && AcademyConstants.ATTR_ORDER_HEADER_KEY.equals(strDataType)) {
				if(!YFCObject.isVoid(strConsolidationRemdrTime) && strConsolidationRemdrTime.contains(":")) {
					calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(strConsolidationRemdrTime.split(":")[0]));
					calendar.set(Calendar.MINUTE, Integer.parseInt(strConsolidationRemdrTime.split(":")[1]));
				}
			}
			//End : OMNI-74228 : BOPIS/STS – Consolidated Pickup Reminders Emails

			strDateModified = sdf.format(calendar.getTime());
			logger.verbose("Modified date :: " + strDateModified);
		} catch (Exception e) {
			logger.error("Exception caught - addHoursToTime() :: " + e);
		}
		logger.verbose("End - - addHoursToTime() :: ");
		return strDateModified;
	}

	/*
	 * Method retrieveReminderInterval() :: provides pick reminder SLA from common
	 * code
	 */
	private String retrieveCodeShortDesc(YFSEnvironment objEnv, Document docOutGetCommonCodeList, String strCodeValue) {

		Element eleCommonCode = null;

		String strCodeShortDesc = null;

		try {

			if (!YFCObject.isNull(docOutGetCommonCodeList)) {
				eleCommonCode = (Element) XPathUtil.getNode(docOutGetCommonCodeList,
						"/CommonCodeList/CommonCode[@CodeValue='" + strCodeValue + "']");
				strCodeShortDesc = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
			}
		} catch (Exception e) {
			logger.error("Exception caught - retrieveReminderInterval() :: " + e);
		}
		return strCodeShortDesc;
	}

	/*
	 * Method invokeManageTaskQueueAPI():: manage TaskQueue entries. MODIFY or
	 * DELETE
	 */
	private void invokeManageTaskQueueAPI(YFSEnvironment env, String strtaskQKey, String strNextTriggerTime,
			String strOperation) throws ParserConfigurationException, Exception {

		try {

			Document manageTaskQueueInDoc = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
			Element manageTaskQueueInEle = manageTaskQueueInDoc.getDocumentElement();
			if (!StringUtil.isEmpty(strNextTriggerTime)) {
				manageTaskQueueInEle.setAttribute(AcademyConstants.ATTR_AVAIL_DATE, strNextTriggerTime);
			}
			if (!StringUtil.isEmpty(strOperation)) {
				manageTaskQueueInEle.setAttribute(AcademyConstants.ATTR_OPERATION, strOperation);
			}
			manageTaskQueueInEle.setAttribute(AcademyConstants.ATTR_TASK_Q_KEY, strtaskQKey);
			logger.verbose("input - manageTaskQueue  API :: " + XMLUtil.getString(manageTaskQueueInDoc));
			AcademyUtil.invokeAPI(env, AcademyConstants.DSV_MANAGE_TASK_QUEUE_API, manageTaskQueueInDoc);
			logger.verbose("Exiting from callManageTaskQueueAPI Method");
		} catch (Exception e) {
			logger.error("Exception caught - invokeManageTaskQueueAPI ()");
		}
	}

	/*
	 * Method - calculateBufferTime() :: Results a date with time by adding number of days
	 * and set time to the result.   
	 */
	
	private String calculateBufferTime(String strDate, String strDays, String strTime) {
		logger.verbose("Begin - calculateBufferTime() :: ");
		String strFinalReminderDate = null;
		try {
			logger.verbose("input strDate ::" + strDate + " :: strDays ::" + strDays + ":: strTime ::" + strTime);
			SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			int iDays = Integer.parseInt(strDays);
			int iTime = Integer.parseInt(strTime);

			Date objDate = sdf.parse(strDate);

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(objDate);
			calendar.add(Calendar.DATE, iDays);
			calendar.set(Calendar.HOUR_OF_DAY, iTime);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);

			strFinalReminderDate = sdf.format(calendar.getTime());
			logger.verbose("Modified date :: " + strFinalReminderDate);
		} catch (Exception e) {
			logger.error("Exception caught - calculateBufferTime() :: " + e);
		}
		logger.verbose("End - - calculateBufferTime() :: ");
		return strFinalReminderDate;
	}
	//Start : OMNI-74228 : BOPIS/STS – Consolidated Pickup Reminders Emails

	/*
	 * Method prepareAndInvokeGetShipmentList() :: invoke getShipmentList append
	 * output to <MonitorConsolidation> element.
	 */

	private Document prepareAndInvokeGetShipmentListForOrder(YFSEnvironment envObj, String strOrderHeaderKey) {
		logger.verbose("Begin AcademyBOPISCustomShipmentMonitor.prepareAndInvokeGetShipmentListForOrder method ");
		logger.verbose(":: strOrderHeaderKey ::" + strOrderHeaderKey);

		Document docOutMonitorConsolidation = null;
		Document docGetShipmentListForOrderOut = null;
		Document docGetShipmentListForOrderInp = null;
		Element eleOutShipment = null;
		Element eleOrderInp = null;

		try {

			if (!StringUtils.isEmpty(strOrderHeaderKey)) {

				docGetShipmentListForOrderInp = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
				eleOrderInp = docGetShipmentListForOrderInp.getDocumentElement();
				eleOrderInp.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
				logger.verbose("getShipmentListForOrder input::" + XMLUtil.getString(docGetShipmentListForOrderInp));
				
				envObj.setApiTemplate(AcademyConstants.API_GET_SHIPMENTLIST_FORORDER,
						AcademyConstants.STR_TEMPLATEFILE_GET_SHIPMENT_LIST_BOPIS);
				docGetShipmentListForOrderOut  = AcademyUtil.invokeAPI(envObj, AcademyConstants.API_GET_SHIPMENTLIST_FORORDER,
						docGetShipmentListForOrderInp);
				envObj.clearApiTemplate(AcademyConstants.API_GET_SHIPMENTLIST_FORORDER);	

				if (!YFCObject.isNull(docGetShipmentListForOrderOut)) {

					logger.verbose("getShipmentListForOrder output::" + XMLUtil.getString(docGetShipmentListForOrderOut));

					//Retrieve the First Shipment
					eleOutShipment = (Element) docGetShipmentListForOrderOut.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);
					
					//If there are multiple shipments, loop through them and append order lines in First Shipment
					eleOutShipment = mergeMultipleShipments(docGetShipmentListForOrderOut);
					logger.verbose("The Document after merging eligible shipment lines:" +com.academy.util.xml.XMLUtil.getElementXMLString(eleOutShipment));
					if (!YFCObject.isNull(eleOutShipment)) {
						docOutMonitorConsolidation = XMLUtil.createDocument(AcademyConstants.ELE_MONITOR_CONSOLIDATION);
						Element eleMonitorConsolidation = docOutMonitorConsolidation.getDocumentElement();
						com.academy.util.xml.XMLUtil.importElement(eleMonitorConsolidation, eleOutShipment);

					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception caught - invokeGetShipmentListForOrder() :: " + e);
		}
		logger.verbose("End AcademyBOPISCustomShipmentMonitor.prepareAndInvokeGetShipmentListForOrder method ");
		return docOutMonitorConsolidation;
	}
	
	
	/*
	 * Method prepareAndInvokeGetShipmentList() :: invoke getShipmentList append
	 * output to <MonitorConsolidation> element.
	 */

	private Element mergeMultipleShipments(Document docGetShipmentListForOrderOut) {
		logger.verbose("Begin AcademyBOPISCustomShipmentMonitor.mergeMultipleShipments method ");
		Element eleFirstShipment = null;

		//Assumption that only the BOPIS/STS Shipments are eligible for Order level consolidation
		//Considering that it is only BOPIS/STS shipment, OMS expects all of them in RFCP status
		try {
			NodeList nlShipments = docGetShipmentListForOrderOut.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
			logger.verbose("nlShipments.getLEngth() ::  "+nlShipments.getLength());

			if(nlShipments.getLength() > 0) {
				//Navigate through each order line and fetch the initial promise date
				for (int iShipment = 0; iShipment < nlShipments.getLength(); iShipment++) {
					
					Element eleShipment = (Element) nlShipments.item(iShipment);
					String strShipmentStatus = eleShipment.getAttribute(AcademyConstants.ATTR_STATUS);
					String strFulfillmentType =  SCXmlUtil.getXpathAttribute(eleShipment, 
							"Shipment/ShipmentLines/ShipmentLine/OrderLine[@FulfillmentType='SOF']/@FulfillmentType");
					String strDeliveryMethod = eleShipment.getAttribute(AcademyConstants.ATTR_DELIVERY_METHOD);
					logger.verbose("strShipmentStatus ::  "+strShipmentStatus);
					logger.verbose("strDeliveryMethod ::  "+strDeliveryMethod);
					logger.verbose("strFulfillmentType ::  "+strFulfillmentType);

					
					if("PICK".equals(strDeliveryMethod) && !"SOF".equals(strFulfillmentType) 
							&& "1100.70.06.30.5".equals(strShipmentStatus)) {
						logger.verbose("strShipmentStatus ::  "+strShipmentStatus);
						if(YFCObject.isVoid(eleFirstShipment)) {
							eleFirstShipment = eleShipment;
						}
						else {
							Element eleFirstShipmentLines = (Element) eleFirstShipment.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINES).item(0);
							NodeList nlShipmentLine = eleShipment.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
							for (int iShipmentLine = 0; iShipmentLine < nlShipmentLine.getLength(); iShipmentLine++) {
								
								Element eleShipmentLine = (Element) nlShipmentLine.item(iShipmentLine);
								logger.verbose("The shipmentline eligible for consolidation::" +com.academy.util.xml.XMLUtil.getElementXMLString(eleShipmentLine));
								com.academy.util.xml.XMLUtil.importElement(eleFirstShipmentLines, eleShipmentLine);
							}
						}
					}
					else {
						logger.verbose("Not adding Shipment as it is in an invalid status ");
					}
				}
			}
			
			if (YFCObject.isNull(eleFirstShipment)) {
				logger.verbose(" First Shipment is null i.e no shipments eligible for Reminder");
				eleFirstShipment = (Element) docGetShipmentListForOrderOut.getElementsByTagName(
						AcademyConstants.ELE_SHIPMENT).item(0);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception caught - mergeMultipleShipments() :: " + e);
		}

		logger.verbose("End AcademyBOPISCustomShipmentMonitor.mergeMultipleShipments method ");
		return eleFirstShipment;
	}
	//End : OMNI-74228 : BOPIS/STS – Consolidated Pickup Reminders Emails

}
