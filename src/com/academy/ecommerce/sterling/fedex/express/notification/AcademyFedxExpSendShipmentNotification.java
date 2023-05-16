package com.academy.ecommerce.sterling.fedex.express.notification;

import java.util.Properties;
import java.util.TimeZone;

import javax.xml.soap.SOAPException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/*#############################################################################################################
*
* Project Name                : POD MAY Release
* Module                      : OMS
* Author                      : CTS
* Date                        : 08-MAY-2020
* Description                 : This file implements the logic to 
* 								1. Set Timezone based on store locale.
*                               2. Invoke web-service PickupAvailability Request and get cut-off time for 
*                                  the store.
*                               3. Create pickup request using web-service and park request 
*                                  in ACAD_FEDX_EXPRESS_PING table for next day with IS_PROCESSED = 'N' when
*                                  current time is before cut-off.
*								4. Park request in ACAD_FEDX_EXPRESS_PING table for next day and 
*								   the day after tomorrow  with IS_PROCESSED = 'N' when
*                                  current time is after cut-off.
*                               5. This file is configured as a service, which will be invoked from servers
*                                  a. AcademyFedexExpProcessSameDayShipments
*                                  b. AcademyFedexExpProcessParkedShipments	   
*
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author                  Version#       Remarks/Description                     
* ---------------------------------------------------------------------------------
* 08-MAY-2020     CTS                      1.0            Initial version
*###############################################################################################################*/

public class AcademyFedxExpSendShipmentNotification implements YIFCustomApi {

	private static YFCLogCategory logger = YFCLogCategory.instance(AcademyFedxExpSendShipmentNotification.class);
	private Properties props;

	@Override
	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}

	public Document processFedexExpressShipments(YFSEnvironment envObj, Document docInput) throws Exception {

		Document docAcadExpPingList = null;

		Element eleInput = null;
		Element eleShipNode = null;
		Element eleAcadExpPing = null;

		String strCurrentTimestamp = null;
		String strCurrentdate = null;
		String strShipNode = null;
		String strLocalecode = null;
		String strTimezone = null;
		String strValidTill = null;
		String strShipmentNo = null;
		String strReadyTimestamp = null;
		String strReadyTimestampParm = null;
		String strIsProcessed = null;
		String strIsParkedRequest = null;

		try {

			logger.verbose("Input - processFedexExpressShipments() :: " + XMLUtil.getXMLString(docInput));

			eleInput = docInput.getDocumentElement();
			eleShipNode = (Element) eleInput.getElementsByTagName(AcademyConstants.ELE_SHIP_NODE).item(0);
			strShipmentNo = eleInput.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
			strShipNode = eleInput.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
			strLocalecode = eleShipNode.getAttribute(AcademyConstants.ATTR_LOCALECODE);
			strCurrentdate = AcademyFedexExpressShipmentNotificationUtils.getCurrentdate();

			/*
			 * Below variable is to check whether the service has been invoked from
			 * AcademyFedexExpProcessParkedShipments server. It will be set to 'Y' when it
			 * is invoked from the same server.
			 */
			strIsParkedRequest = (String) envObj.getTxnObject(AcademyConstants.STR_IS_PARKED_REQUEST);

			if (!StringUtil.isEmpty(strIsParkedRequest)) {
				logger.verbose("IsParkedRequest :: " + strIsParkedRequest);
			}

			logger.verbose("Before, Timezone is set :: ");
			logger.verbose("Current Timestamp :: " + strCurrentTimestamp);
			logger.verbose("Current date :: " + strCurrentdate);

			/*
			 * Get Timezone of the store
			 */
			strTimezone = AcademyFedexExpressShipmentNotificationUtils.getLocalecodeTimezone(envObj, strLocalecode);

			/*
			 * Set timezone before start processing the record.
			 */
			TimeZone.setDefault(TimeZone.getTimeZone(strTimezone));
			strCurrentTimestamp = AcademyFedexExpressShipmentNotificationUtils.getCurrentTimestamp();
			strCurrentdate = AcademyFedexExpressShipmentNotificationUtils.getCurrentdate();

			logger.verbose("After, Timezone is set :: ");
			logger.verbose("Current Timestamp :: " + strCurrentTimestamp);
			logger.verbose("Current date :: " + strCurrentdate);

			eleInput.setAttribute(AcademyConstants.FDX_PICK_REQ_DISPATCH_DATE, strCurrentTimestamp.substring(0, 10));
			eleInput.setAttribute(AcademyConstants.ATTR_CURRENT_DATE, strCurrentdate);
			eleInput.setAttribute(AcademyConstants.ATTR_FEDEX_PICKUP_REQ_TYPE, AcademyConstants.FDX_PICK_REQ_TYPE);

			logger.verbose("Document CommonInput :: " + XMLUtil.getXMLString(docInput));

			/*
			 * Below line of code will fetch records from ACAD_FEDX_EXPRESS_PING table
			 */
			docAcadExpPingList = AcademyFedexExpressShipmentNotificationUtils.getListAcadFedxExpressPing(envObj,
					strCurrentdate, strShipNode);

			eleAcadExpPing = (Element) XPathUtil.getNode(docAcadExpPingList.getDocumentElement(),
					"/AcadFedxExpressPingList/AcadFedxExpressPing[@ProcessDate='" + strCurrentdate + "'"
							+ "and @IsProcessed='Y']");
			/*
			 * If below condition TRUE, which means there is no entry in PING table for
			 * current day with IS_PROCESSED = Y. OMS has to proceed and create pickup
			 * request for the day.
			 */
			if (null == eleAcadExpPing) {

				eleAcadExpPing = (Element) XPathUtil.getNode(docAcadExpPingList.getDocumentElement(),
						"/AcadFedxExpressPingList/AcadFedxExpressPing[@ProcessDate='" + strCurrentdate + "'"
								+ "and @IsProcessed='N']");

				/*
				 * If below condition TRUE, which means there is an entry in PING table for
				 * current day with IS_PROCESSED = N. It try fetch the value of VALID_TILL
				 * column.
				 * 
				 * It might be populated, when an error occurred while invoking create pickup
				 * request web-service and parked the request in ping table to re-process later
				 * on same day.
				 * 
				 * As OMS would have the cut-off from fedex as part of pickup availability
				 * web-service and saved along with the parked request.
				 */
				if (null != eleAcadExpPing) {

					strValidTill = eleAcadExpPing.getAttribute(AcademyConstants.ATTR_VALID_TILL);
				}

				/*
				 * If the value of VALID_TILL still empty, then OMS will invoke FedEx pickup
				 * availability web-service and receive cut-off.
				 */
				if (StringUtil.isEmpty(strValidTill)) {

					strValidTill = AcademyFedexExpressShipmentNotificationUtils
							.invokePickupAvailServiceAndGetValidTill(envObj, docInput, docAcadExpPingList, props);
				}
				/*
				 * Below condition is executed when there is an entry for current day with
				 * IS_PROCESSED = Y. IS_PROCESSED is set to 'Y' only when OMS has made a
				 * successful pickup request. If 'Y' then OMS need not make pickup availability
				 * web-service for current day.
				 */
			} else {

				strValidTill = eleAcadExpPing.getAttribute(AcademyConstants.ATTR_VALID_TILL);
				strIsProcessed = eleAcadExpPing.getAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED);
			}

			/*
			 * OMS will proceed to make create pickup web-service call only when validTill
			 * is not null and not 'SERVICE_UNAVAILABLE'. When FedEx is unavailable for a
			 * day, then they will respond with a code 5035. Upon receiving, OMS will set
			 * valid till as 'SERVICE_UNAVAILABLE'. These values will get reflected PING
			 * table.
			 */
			if (!StringUtil.isEmpty(strValidTill)
					&& !AcademyConstants.STR_SERVICE_UNAVAILABLE.equalsIgnoreCase(strValidTill)) {

				logger.verbose("Valid Till :: " + strValidTill);

				eleInput.setAttribute(AcademyConstants.ATTR_VALID_TILL, strValidTill);
				strReadyTimestampParm = props.getProperty(AcademyConstants.STR_PARM_FOR_READY_TS_CALC);
				strReadyTimestamp = AcademyFedexExpressShipmentNotificationUtils.addHoursAndMinutes(strValidTill,
						AcademyConstants.STR_ZERO, strReadyTimestampParm);
				eleInput.setAttribute(AcademyConstants.FDX_CREATE_PICK_REQ_READYTIMESTAMP, strReadyTimestamp);
				strCurrentTimestamp = AcademyFedexExpressShipmentNotificationUtils.getCurrentTimestamp();

				/*
				 * Once the cut-off is received, OMS will check if current time is before
				 * cut-off, If Yes then OMS will invoke create pickup web-service and park the
				 * request for next day.
				 */
				if (AcademyFedexExpressShipmentNotificationUtils.compareTimestamps(strCurrentTimestamp,
						AcademyConstants.STR_CONDITION_BEFORE, strReadyTimestamp)) {

					logger.verbose("Before cut-off :: ");

					if (!AcademyConstants.STR_YES.equalsIgnoreCase(strIsProcessed)) {

						AcademyFedexExpressShipmentNotificationUtils.createPickupReqAndGetResponse(envObj,
								docAcadExpPingList, docInput, props);
					}

					/*
					 * For a parked request, OMS need to invoke create pickup web-service and
					 * parking requests for future day is not required.
					 */
					if (!AcademyConstants.STR_YES.equalsIgnoreCase(strIsParkedRequest)) {

						AcademyFedexExpressShipmentNotificationUtils
								.parkCreatePickupReq(
										envObj, docAcadExpPingList, AcademyFedexExpressShipmentNotificationUtils
												.addDaysToDate(strCurrentdate, AcademyConstants.STR_DIGIT_ONE),
										strShipNode, strShipmentNo);
					}

				} else if (AcademyFedexExpressShipmentNotificationUtils.compareTimestamps(strCurrentTimestamp,
						AcademyConstants.STR_CONDITION_EQUALS, strReadyTimestamp)
						|| AcademyFedexExpressShipmentNotificationUtils.compareTimestamps(strCurrentTimestamp,
								AcademyConstants.STR_CONDITION_AFTER, strReadyTimestamp)) {

					logger.verbose("After cut-off :: ");

					/*
					 * If the first shipment created/packed or parked request processed after
					 * cut-off, then OMS will create a dummy record in PING table with Shipment No
					 * as DUMMY_ShipmentNo and IS_PROCESSED = Y to notify that its a dummy record
					 * and pickup request is not invoked.
					 */
					if (AcademyFedexExpressShipmentNotificationUtils.IsPingEntryExistsWithIsProcessedFlag(
							docAcadExpPingList,
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_CURRENT_DATE),
							AcademyConstants.STR_NO)) {
						AcademyFedexExpressShipmentNotificationUtils.modifyAcadFedxExpressPing(envObj,
								docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_CURRENT_DATE),
								docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE),
								AcademyConstants.STR_DUMMY_HYPHEN
										+ docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO),
								strValidTill, AcademyConstants.STR_YES);
					} else if (!AcademyFedexExpressShipmentNotificationUtils.IsPingEntryExistsWithIsProcessedFlag(
							docAcadExpPingList,
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_CURRENT_DATE),
							AcademyConstants.STR_YES)) {
						AcademyFedexExpressShipmentNotificationUtils.createAcadFedxExpressPing(envObj,
								docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_CURRENT_DATE),
								docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE),
								AcademyConstants.STR_DUMMY_HYPHEN
										+ docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO),
								strValidTill, AcademyConstants.STR_YES);
					}

					/*
					 * The logic to park request for N+1 and N+2 days (Current day - N). If a parked
					 * request is processed after cut off then new park entry for only N+1 is
					 * created.
					 */
					AcademyFedexExpressShipmentNotificationUtils
							.parkCreatePickupReq(
									envObj, docAcadExpPingList, AcademyFedexExpressShipmentNotificationUtils
											.addDaysToDate(strCurrentdate, AcademyConstants.STR_DIGIT_ONE),
									strShipNode, strShipmentNo);

					if (!AcademyConstants.STR_YES.equalsIgnoreCase(strIsParkedRequest)) {

						AcademyFedexExpressShipmentNotificationUtils
								.parkCreatePickupReq(
										envObj, docAcadExpPingList, AcademyFedexExpressShipmentNotificationUtils
												.addDaysToDate(strCurrentdate, AcademyConstants.STR_DIGIT_TWO),
										strShipNode, strShipmentNo);
					}
				}

				/*
				 * When fedex is unavailable and if future request were not made then below
				 * logic would create the same.
				 */
			} else if (AcademyConstants.STR_SERVICE_UNAVAILABLE.equalsIgnoreCase(strValidTill)
					&& AcademyConstants.STR_YES.equalsIgnoreCase(strIsProcessed)) {

				logger.verbose("SERVICE UNAVAILABLE Today :: ");

				AcademyFedexExpressShipmentNotificationUtils.parkCreatePickupReq(envObj, docAcadExpPingList,
						AcademyFedexExpressShipmentNotificationUtils.addDaysToDate(strCurrentdate,
								AcademyConstants.STR_DIGIT_ONE),
						strShipNode, strShipmentNo);

				if (!AcademyConstants.STR_YES.equalsIgnoreCase(strIsParkedRequest)) {

					AcademyFedexExpressShipmentNotificationUtils
							.parkCreatePickupReq(
									envObj, docAcadExpPingList, AcademyFedexExpressShipmentNotificationUtils
											.addDaysToDate(strCurrentdate, AcademyConstants.STR_DIGIT_TWO),
									strShipNode, strShipmentNo);
				}

			}

		} catch (YFSException yfsEx) {
			logger.info("Error Code :: " + yfsEx.getErrorCode());
			logger.info("Exception Message :: " + yfsEx.getMessage());
			logger.info("Exception Description :: " + yfsEx.getErrorDescription());
			throw yfsEx;

		} catch (SOAPException soapEx) {

			logger.info("SOAP Exception Message:: " + soapEx.getMessage());
			YFSException yfse = new YFSException();
			yfse.setErrorCode(AcademyConstants.ERR_CODE_FEDX_PING_01);
			yfse.setErrorDescription(soapEx.getMessage());
			throw yfse;

		} catch (Exception e) {

			logger.info("Exception Message :: " + e.getMessage());
			throw e;

		}

		return docInput;
	}

}
