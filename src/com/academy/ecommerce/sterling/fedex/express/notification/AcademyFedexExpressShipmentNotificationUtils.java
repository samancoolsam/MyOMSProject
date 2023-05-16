package com.academy.ecommerce.sterling.fedex.express.notification;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/*#############################################################################################################
*
* Project Name                : POD MAY Release
* Module                      : OMS
* Author                      : CTS
* Date                        : 08-MAY-2020
* Description                 : This file contains the methods   
* 								1. Prepare input for PickupAvailabilty web-service and parse response.
*                               2. Prepare input for CreatePickupRequest web-service and parse response.
*                               3. Methods to Create, Modify, GetList of ACAD_FEDX_EXPRESS_PING table.                                  .
*
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author                  Version#       Remarks/Description                     
* ---------------------------------------------------------------------------------
* 08-MAY-2020     CTS                      1.0            Initial version
*###############################################################################################################*/

public class AcademyFedexExpressShipmentNotificationUtils {

	private static YFCLogCategory logger = YFCLogCategory.instance(AcademyFedexExpressShipmentNotificationUtils.class);

	/*
	 * Get current Timestamp in 'yyyy-MM-dd'T'HH:mm:ss' format.
	 */

	public static String getCurrentTimestamp() throws Exception {

		String strCurrentTimestamp = null;

		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
		strCurrentTimestamp = sdf.format(new Date());

		logger.verbose("Current Timestamp - " + strCurrentTimestamp);

		return strCurrentTimestamp;
	}

	/*
	 * Get current date in 'YYYYMMDD' format.
	 */
	public static String getCurrentdate() throws Exception {

		String strCurrentDate = null;

		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.DATE_YYYYMMDD_FORMAT);
		strCurrentDate = sdf.format(new Date());

		logger.verbose("Current Date - " + strCurrentDate);

		return strCurrentDate;
	}

	/*
	 * Add Hours & Minutes to the date provided and return output.
	 */
	public static String addHoursAndMinutes(String strDate, String strHours, String strMinutes) throws Exception {

		logger.verbose("Begin - addHoursAndMinutes() :: ");
		String strModifiedDate = null;

		logger.verbose("input strDate ::" + strDate + " :: strHours ::" + strHours + ":: strMinutes ::" + strMinutes);
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
		int iHours = Integer.parseInt(strHours);
		int iMinutes = Integer.parseInt(strMinutes);

		Date objDate = sdf.parse(strDate);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(objDate);
		calendar.add(Calendar.HOUR, iHours);
		calendar.add(Calendar.MINUTE, iMinutes);
		calendar.set(Calendar.SECOND, 0);

		strModifiedDate = sdf.format(calendar.getTime());
		logger.verbose("Modified date :: " + strModifiedDate);

		logger.verbose("End - addHoursAndMinutes() :: ");
		return strModifiedDate;
	}

	/*
	 * Results a date after adding 'N' number of days to input.
	 */

	public static String addDaysToDate(String strDate, String strDays) throws Exception {

		logger.verbose("Begin - addDaysToDate() :: ");
		String strModifiedDate = null;

		logger.verbose("input strDate ::" + strDate + " :: strDays ::" + strDays);
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.DATE_YYYYMMDD_FORMAT);
		int iDays = Integer.parseInt(strDays);

		Date objDate = sdf.parse(strDate);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(objDate);
		calendar.add(Calendar.DATE, iDays);

		strModifiedDate = sdf.format(calendar.getTime());
		logger.verbose("Modified date :: " + strModifiedDate);

		logger.verbose("End - - addDaysToDate() :: ");
		return strModifiedDate;
	}

	/*
	 * Calculate cut-off timestamp 'ValidTill' based on PickupTime and Access hours
	 * from FedEx.
	 */
	public static String calculateValidTill(String strPickupDateTime, String strAccessTime) throws Exception {

		logger.verbose("Begin - calculateValidTill() :: ");
		String strValidTill = null;

		String strAccessHour[] = strAccessTime.split("H");
		String strAccessMinutes[] = strAccessHour[1].split("M");

		strValidTill = addHoursAndMinutes(strPickupDateTime, "-" + strAccessHour[0], "-" + strAccessMinutes[0]);

		logger.verbose("Valid Till :: " + strValidTill);

		logger.verbose("End - calculateValidTill() :: ");
		return strValidTill;

	}

	/*
	 * Compares the timestamp provided along with the action and results true or
	 * false. EQUALS, BEFORE & AFTER are the actions.
	 */
	public static boolean compareTimestamps(String strDateOne, String strAction, String strDateTwo) throws Exception {

		logger.verbose("Begin - compareTimestamp() :: ");

		if (!StringUtil.isEmpty(strDateOne) && !StringUtil.isEmpty(strAction) && !StringUtil.isEmpty(strDateTwo)) {

			logger.verbose("strDateOne:: " + strDateOne + " strAction:: " + strAction + " strDateTwo:: " + strDateTwo);

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

		return false;
	}

	/*
	 * Retrieves Timezone from Sterling DB/Cache using Localecode using
	 * getLocaleList API
	 */
	public static String getLocalecodeTimezone(YFSEnvironment env, String strLocalecode) throws Exception {

		logger.verbose("Begin - getLocalecodeTimezone() :: ");

		Document docGetLocaleListInput = null;
		Document docGetLocaleListOutput = null;
		String strTimezone = null;

		docGetLocaleListInput = XMLUtil.createDocument(AcademyConstants.ELE_LOCALE);
		docGetLocaleListInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_LOCALECODE, strLocalecode);

		logger.verbose("API getLocaleList In Doc :\n" + XMLUtil.getXMLString(docGetLocaleListInput));
		docGetLocaleListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_LOCALE_LIST,
				docGetLocaleListInput);
		logger.verbose("API getLocaleList Out Doc :\n" + XMLUtil.getXMLString(docGetLocaleListOutput));
		strTimezone = XMLUtil.getAttributeFromXPath(docGetLocaleListOutput, AcademyConstants.XPATH_ATTR_TIMEZONE);
		logger.verbose("Timezone is : " + strTimezone + ", for the Localecode : " + strLocalecode);

		logger.verbose("End - getLocalecodeTimezone() :: ");
		return strTimezone;
	}

	/*
	 * Checks if a node exists in GetList of ACAD_FEDX_EXPRESS_PING with the process
	 * date and isProcessed values provided.
	 */
	public static boolean IsPingEntryExistsWithIsProcessedFlag(Document docFedxPingList, String strProcessdate,
			String strIsProcessed) throws Exception {

		logger.verbose("Begin - IsPingEntryExistsWithIsProcessedFlag() :: ");

		logger.verbose("ProcessDate :: " + strProcessdate + ", IsProcessed :: " + strIsProcessed);

		boolean isPingExists = false;

		Element eleFedxPing = (Element) XPathUtil.getNode(docFedxPingList.getDocumentElement(),
				"/AcadFedxExpressPingList/AcadFedxExpressPing[@ProcessDate='" + strProcessdate + "'"
						+ "and @IsProcessed='" + strIsProcessed + "']");
		if (null != eleFedxPing) {

			logger.verbose("Element Fedex Ping :: " + XMLUtil.getElementXMLString(eleFedxPing));
			isPingExists = true;
		}

		logger.verbose("End - IsPingEntryExistsWithIsProcessedFlag() :: ");

		return isPingExists;
	}

	/*
	 * Create a new record in ACAD_FEDX_EXPRESS_PING table with provided values.
	 */
	public static void createAcadFedxExpressPing(YFSEnvironment envObj, String strProcessDate, String strStoreNo,
			String strShipmentNo, String strValidTill, String strIsProcessed) throws Exception {

		logger.verbose("Begin - createAcadFedxExpressPing() :: ");

		Document docCreateAcadExpressFedxPing = XMLUtil.createDocument(AcademyConstants.ELE_ACAD_FEDX_EXP_PING);
		Element eleAcadFedxExpressPing = docCreateAcadExpressFedxPing.getDocumentElement();
		eleAcadFedxExpressPing.setAttribute(AcademyConstants.ATTR_EXPRESS_KEY, strProcessDate + strStoreNo);
		eleAcadFedxExpressPing.setAttribute(AcademyConstants.ATTR_PROCESS_DATE, strProcessDate);
		eleAcadFedxExpressPing.setAttribute(AcademyConstants.ATTR_STORE_NO, strStoreNo);

		if (!StringUtil.isEmpty(strValidTill)) {
			eleAcadFedxExpressPing.setAttribute(AcademyConstants.ATTR_VALID_TILL, strValidTill);
		}

		eleAcadFedxExpressPing.setAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED, strIsProcessed);
		eleAcadFedxExpressPing.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);

		logger.verbose("Input to API :: " + XMLUtil.getXMLString(docCreateAcadExpressFedxPing));
		AcademyUtil.invokeService(envObj, AcademyConstants.CREATE_ACAD_FEDX_EXPRESS_PING, docCreateAcadExpressFedxPing);

		logger.verbose("End - createAcadFedxExpressPing() :: ");

	}

	/*
	 * Modify a record of ACAD_FEDX_EXPRESS_PING table based on the input
	 */
	public static void modifyAcadFedxExpressPing(YFSEnvironment envObj, String strProcessDate, String strStoreNo,
			String strShipmentNo, String strValidTill, String strIsProcessed) throws Exception {

		logger.verbose("Begin - modifyAcadFedxExpressPing() :: ");

		Document docModifyAcadExpressFedxPing = XMLUtil.createDocument(AcademyConstants.ELE_ACAD_FEDX_EXP_PING);
		Element eleAcadFedxExpressPing = docModifyAcadExpressFedxPing.getDocumentElement();
		eleAcadFedxExpressPing.setAttribute(AcademyConstants.ATTR_EXPRESS_KEY, strProcessDate + strStoreNo);

		if (!StringUtil.isEmpty(strValidTill)) {
			eleAcadFedxExpressPing.setAttribute(AcademyConstants.ATTR_VALID_TILL, strValidTill);
		}

		if (!StringUtil.isEmpty(strShipmentNo)) {
			eleAcadFedxExpressPing.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
		}

		eleAcadFedxExpressPing.setAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED, strIsProcessed);

		logger.verbose("input to API :: " + XMLUtil.getXMLString(docModifyAcadExpressFedxPing));

		AcademyUtil.invokeService(envObj, AcademyConstants.MODIFY_ACAD_FEDX_EXPRESS_PING, docModifyAcadExpressFedxPing);

		logger.verbose("End - modifyAcadFedxExpressPing() :: ");
	}

	/*
	 * Returns a list of records/none from ACAD_FEDX_EXPRESS_PING based on the
	 * input.
	 */
	public static Document getListAcadFedxExpressPing(YFSEnvironment envObj, String strProcessDate, String strStoreNo)
			throws Exception {

		logger.verbose("Begin - getListAcadFedxExpressPing() :: ");

		Document docAcadExpressPingList = XMLUtil.createDocument(AcademyConstants.ELE_ACAD_FEDX_EXP_PING);
		Element eleAcadFedxExpressPing = docAcadExpressPingList.getDocumentElement();

		eleAcadFedxExpressPing.setAttribute(AcademyConstants.ATTR_PROCESS_DATE, strProcessDate);

		if (!StringUtil.isEmpty(strStoreNo)) {
			eleAcadFedxExpressPing.setAttribute(AcademyConstants.ATTR_STORE_NO, strStoreNo);

			eleAcadFedxExpressPing.setAttribute(AcademyConstants.ATTR_PROCESS_DATE_QRY_TYPE,
					AcademyConstants.STR_GREATER_THAN_OR_EQUALS);

		} else {
			eleAcadFedxExpressPing.setAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED, AcademyConstants.STR_NO);

			Element eleOrderBy = docAcadExpressPingList.createElement(AcademyConstants.ELE_ORDERBY);

			eleAcadFedxExpressPing.appendChild(eleOrderBy);

			Element eleAttribute = docAcadExpressPingList.createElement(AcademyConstants.ELE_ATTRIBUTE);

			eleOrderBy.appendChild(eleAttribute);

			eleAttribute.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_STORE_NO);
		}

		logger.verbose("input to API :: " + XMLUtil.getXMLString(docAcadExpressPingList));

		Document docListAcadExpPing = AcademyUtil.invokeService(envObj,
				AcademyConstants.GET_LIST_ACAD_FEDX_EXPRESS_PING, docAcadExpressPingList);

		logger.verbose("Output from API :: " + XMLUtil.getXMLString(docListAcadExpPing));
		logger.verbose("End - getListAcadFedxExpressPing() :: ");

		return docListAcadExpPing;
	}

	/*
	 * Returns a list of records/none from ACAD_FEDX_EXPRESS_PING based on ascending
	 * order of store. Store No of output will be greater than that of input.
	 */
	public static Document getListAcadFedxExpressPingStoreQry(YFSEnvironment envObj, String strProcessDate,
			String strStoreNo) throws Exception {

		logger.verbose("Begin - getListAcadFedxExpressPing() :: ");

		Document docAcadExpressPing = XMLUtil.createDocument(AcademyConstants.ELE_ACAD_FEDX_EXP_PING);
		Element eleAcadFedxExpressPing = docAcadExpressPing.getDocumentElement();

		eleAcadFedxExpressPing.setAttribute(AcademyConstants.ATTR_PROCESS_DATE, strProcessDate);

		eleAcadFedxExpressPing.setAttribute(AcademyConstants.ATTR_STORE_NO, strStoreNo);

		eleAcadFedxExpressPing.setAttribute(AcademyConstants.ATTR_STORE_NO_QRY_TYPE, AcademyConstants.GT_QRY_TYPE);

		eleAcadFedxExpressPing.setAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED, AcademyConstants.STR_NO);

		Element eleOrderBy = docAcadExpressPing.createElement(AcademyConstants.ELE_ORDERBY);

		eleAcadFedxExpressPing.appendChild(eleOrderBy);

		Element eleAttribute = docAcadExpressPing.createElement(AcademyConstants.ELE_ATTRIBUTE);

		eleOrderBy.appendChild(eleAttribute);

		eleAttribute.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_STORE_NO);

		logger.verbose("input to API :: " + XMLUtil.getXMLString(docAcadExpressPing));

		Document docListAcadExpPing = AcademyUtil.invokeService(envObj,
				AcademyConstants.GET_LIST_ACAD_FEDX_EXPRESS_PING, docAcadExpressPing);

		logger.verbose("Output from API :: " + XMLUtil.getXMLString(docListAcadExpPing));
		logger.verbose("End - getListAcadFedxExpressPing() :: ");

		return docListAcadExpPing;
	}

	/*
	 * Read FedEx PickupAvailability/CreatePickup response.
	 * 
	 * For PickupAvailability Response, get HighestSeveriry, PickupDate, CutOffTime
	 * & AccessTime
	 * 
	 * For CreatePickup Response, get HighestSeverity, ErrorCode & Message
	 */
	public static Map<String, String> parseFedexResAndGetDetails(Document docPickupAvailRes, String strRequestType)
			throws Exception {

		logger.verbose("Begin - parseFedexResAndGetDetails() :: ");

		Map<String, String> hmPickAvailRes = new HashMap<String, String>();

		Element eleHighSeverity = (Element) docPickupAvailRes.getElementsByTagName("HighestSeverity").item(0);

		if (null != eleHighSeverity) {

			String strResponse = eleHighSeverity.getTextContent();

			if (AcademyConstants.STR_SUCCESS.equalsIgnoreCase(strResponse)
					|| AcademyConstants.STR_WARNING.equalsIgnoreCase(strResponse)
					|| AcademyConstants.STR_NOTE.equalsIgnoreCase(strResponse)) {

				logger.verbose("Response :: " + strResponse);

				hmPickAvailRes.put(AcademyConstants.STR_RESPONSE, AcademyConstants.STR_SUCCESS);

				if (AcademyConstants.STR_PICKUP_AVAIL_RES.equalsIgnoreCase(strRequestType)) {
					Element eleOptions = (Element) docPickupAvailRes.getElementsByTagName("Options").item(0);
					if (!YFCObject.isVoid(eleOptions)) {
						String strPickupAvlCutOffDateTime = docPickupAvailRes.getElementsByTagName("PickupDate").item(0)
								.getTextContent() + "T"
								+ docPickupAvailRes.getElementsByTagName("CutOffTime").item(0).getTextContent();

						String strAccessTime = docPickupAvailRes.getElementsByTagName("AccessTime").item(0)
								.getTextContent();

						logger.verbose("PickupAvlCutoffDateTme :: " + strPickupAvlCutOffDateTime);
						logger.verbose("AccessTime :: " + strAccessTime);

						hmPickAvailRes.put(AcademyConstants.STR_PICKUP_DATE_TIME, strPickupAvlCutOffDateTime);
						hmPickAvailRes.put(AcademyConstants.STR_ACCESS_TIME, strAccessTime.substring(2));
					}
				}

			} else if (AcademyConstants.STATUS_CODE_ERROR.equalsIgnoreCase(strResponse)
					|| AcademyConstants.STR_FAILURE.equalsIgnoreCase(strResponse)) {

				logger.verbose("Response :: " + strResponse);

				hmPickAvailRes.put(AcademyConstants.STR_RESPONSE, strResponse);

				Element eleNotifications = (Element) docPickupAvailRes.getElementsByTagName("Notifications").item(0);
				if (!YFCObject.isVoid(eleNotifications)) {

					String strCode = docPickupAvailRes.getElementsByTagName("Code").item(0).getTextContent();
					String strMessage = docPickupAvailRes.getElementsByTagName("Message").item(0).getTextContent();

					logger.verbose("Error Code :: " + strCode);
					logger.verbose("Error Message :: " + strMessage);

					hmPickAvailRes.put(AcademyConstants.STR_CODE, strCode);
					hmPickAvailRes.put(AcademyConstants.STR_MESSAGE, strMessage);
				}
			}
		} else {

			logger.verbose("No HighSeverity message :: ");
			hmPickAvailRes.put(AcademyConstants.STR_RESPONSE, AcademyConstants.STATUS_CODE_ERROR);
		}

		logger.verbose("End - parseFedexResAndGetDetails() :: ");

		return hmPickAvailRes;
	}

	/*
	 * If PickupAvailability/CreatePickup response contains error codes then, Park
	 * requests in ACAD_FEDX_EXPRESS_PING table based on configuration.
	 */
	public static void parkRequestsOnError(YFSEnvironment envObj, Document docInput, Document docAcadFedxPingList,
			Properties props, Map<String, String> mFedxRes) throws Exception {

		logger.verbose("Begin - parkRequestsOnError() :: ");

		String strIsParkedReq = (String) envObj.getTxnObject(AcademyConstants.STR_IS_PARKED_REQUEST);
		String strCode = mFedxRes.get(AcademyConstants.STR_CODE);
		String strSeverity = mFedxRes.get(AcademyConstants.STR_RESPONSE);
		String strNextDayErrorCode = props.getProperty(AcademyConstants.STR_REPROCESS_NEXT_DAY_FEDX_ERROR_CODES);
		String strSameDayErrorCode = props.getProperty(AcademyConstants.STR_REPROCESS_SAME_DAY_FEDX_ERROR_CODES);
		String strSameDaySeverity = props.getProperty(AcademyConstants.STR_REPROCESS_SAME_DAY_FEDX_SEVERITY);
		String strFedxSeverity = props.getProperty(AcademyConstants.STR_REPROCESS_FEDX_SEVERITY);

		/*
		 * If FedEx error severity is FAILURE then it needs to be reprocessed on same
		 * day. A request will be parked in PING table for current day.
		 */
		if (!StringUtil.isEmpty(strSameDaySeverity) && !StringUtil.isEmpty(strSeverity)) {

			logger.verbose("SameDaySeverity :: " + strSameDaySeverity + " - ResponseSeverity :: " + strSeverity);

			if (strSameDaySeverity.contains(strSeverity)) {

				logger.verbose("Parking request for Same Day !! ");

				if (IsPingEntryExistsWithIsProcessedFlag(docAcadFedxPingList,
						docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_CURRENT_DATE),
						AcademyConstants.STR_NO)) {

					modifyAcadFedxExpressPing(envObj,
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_CURRENT_DATE),
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE),
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO),
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_VALID_TILL),
							AcademyConstants.STR_NO);

				} else {

					createAcadFedxExpressPing(envObj,
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_CURRENT_DATE),
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE),
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO),
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_VALID_TILL),
							AcademyConstants.STR_NO);
				}

			}
		}

		/*
		 * If FedEx error severity is ERROR and error code is in the list of error codes
		 * to be reprocessed today then OMS will park the request for same day.
		 */
		if (!StringUtil.isEmpty(strFedxSeverity) && !StringUtil.isEmpty(strSeverity)
				&& !StringUtil.isEmpty(strSameDayErrorCode) && !StringUtil.isEmpty(strCode)) {

			logger.verbose("FedxSeverity :: " + strFedxSeverity + " - ResponseSeverity :: " + strSeverity
					+ " - SameDayErrorCode :: " + strSameDayErrorCode + " - ResponseCode :: " + strCode);

			if (strFedxSeverity.contains(strSeverity) && strSameDayErrorCode.contains(strCode)) {

				logger.verbose("Parking request for Same Day !! ");

				if (IsPingEntryExistsWithIsProcessedFlag(docAcadFedxPingList,
						docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_CURRENT_DATE),
						AcademyConstants.STR_NO)) {

					modifyAcadFedxExpressPing(envObj,
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_CURRENT_DATE),
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE),
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO),
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_VALID_TILL),
							AcademyConstants.STR_NO);

				} else {

					createAcadFedxExpressPing(envObj,
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_CURRENT_DATE),
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE),
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO),
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_VALID_TILL),
							AcademyConstants.STR_NO);
				}

			}
		}

		/*
		 * If FedEx error severity is ERROR and error code is in the list of error codes
		 * to be reprocessed next day then OMS will park the request for next day.
		 */
		if (!StringUtil.isEmpty(strFedxSeverity) && !StringUtil.isEmpty(strSeverity)
				&& !StringUtil.isEmpty(strNextDayErrorCode) && !StringUtil.isEmpty(strCode)) {

			logger.verbose("FedxSeverity :: " + strFedxSeverity + " - ResponseSeverity :: " + strSeverity
					+ " - NextDayErrorCode :: " + strNextDayErrorCode + " - ResponseCode :: " + strCode);

			if (strFedxSeverity.contains(strSeverity) && strNextDayErrorCode.contains(strCode)) {

				String strCurrentDate = docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_CURRENT_DATE);

				/*
				 * If error occurred while processing a parked request, then OMS would park the
				 * request for next day N+1 is there is not parked request for N+1 day.
				 * 
				 * If parked request for N+1 is already present then OMS will park the request
				 * for N+2 day if there is no entry exists
				 */
				if (AcademyConstants.STR_YES.equalsIgnoreCase(strIsParkedReq)) {

					logger.verbose("IsParkedRequest :: " + strIsParkedReq);

					String strNextdate = addDaysToDate(strCurrentDate, AcademyConstants.STR_DIGIT_ONE);
					Element eleFedxPing = (Element) XPathUtil.getNode(docAcadFedxPingList.getDocumentElement(),
							"/AcadFedxExpressPingList/AcadFedxExpressPing[@ProcessDate='" + strNextdate + "'"
									+ "and @IsProcessed='N']");

					if (null != eleFedxPing) {

						logger.verbose("Next Day request has already been parked !! ");

						strNextdate = addDaysToDate(strCurrentDate, AcademyConstants.STR_DIGIT_TWO);

						parkCreatePickupReq(envObj, docAcadFedxPingList, strNextdate,
								docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE),
								docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));

					} else {

						logger.verbose("Next Day request is unavailable !! ");

						parkCreatePickupReq(envObj, docAcadFedxPingList, strNextdate,
								docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE),
								docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
					}

				} else {

					/*
					 * For a regular/Same day request OMS would park the request for N+1 & N+2 days.
					 */

					logger.verbose("Parking Next day (N+1) request !! ");

					parkCreatePickupReq(envObj, docAcadFedxPingList,
							addDaysToDate(strCurrentDate, AcademyConstants.STR_DIGIT_ONE),
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE),
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));

					logger.verbose("Parking day after tomorrow (N+2) request !! ");

					parkCreatePickupReq(envObj, docAcadFedxPingList,
							addDaysToDate(strCurrentDate, AcademyConstants.STR_DIGIT_TWO),
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE),
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
				}

				logger.verbose("Updating/Creating current day ping entry :: ");
				
				/*
				 * OMS will be updating ping entry for current day with VAILD_TILL - SERVICE_UNAVAILABLE
				 * as FedEx is not processing the request for current day 
				 */
				
				if (IsPingEntryExistsWithIsProcessedFlag(docAcadFedxPingList,
						docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_CURRENT_DATE),
						AcademyConstants.STR_NO)) {
					modifyAcadFedxExpressPing(envObj,
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_CURRENT_DATE),
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE),
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO),
							AcademyConstants.STR_SERVICE_UNAVAILABLE, AcademyConstants.STR_YES);
				} else {
					createAcadFedxExpressPing(envObj,
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_CURRENT_DATE),
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE),
							docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO),
							AcademyConstants.STR_SERVICE_UNAVAILABLE, AcademyConstants.STR_YES);
				}

			}
		}

		logger.verbose("End - parkRequestsOnError() :: ");
	}

	/*
	 * Invoke FedEx Pickup Availability Service and get response. Take action based
	 * on HighestSeverity.
	 */
	public static String invokePickupAvailServiceAndGetValidTill(YFSEnvironment envObj, Document docPickAvailServiceReq,
			Document docAcadFedxPingList, Properties props) throws Exception {

		logger.verbose("Begin - invokePickupAvailServiceAndGetValidTill() :: ");

		String strValidTill = null;

		Document docPickAvailServiceRes = AcademyUtil.invokeService(envObj,
				AcademyConstants.SERVICE_FEDEX_EXPRESS_PICKUP_AVAIL_REQ, docPickAvailServiceReq);

		Map<String, String> mPickAvailRes = parseFedexResAndGetDetails(docPickAvailServiceRes,
				AcademyConstants.STR_PICKUP_AVAIL_RES);

		String strResponse = mPickAvailRes.get(AcademyConstants.STR_RESPONSE);

		if (AcademyConstants.STR_SUCCESS.equalsIgnoreCase(strResponse)) {

			String strPickupDateTime = mPickAvailRes.get(AcademyConstants.STR_PICKUP_DATE_TIME);
			String strAccessTime = mPickAvailRes.get(AcademyConstants.STR_ACCESS_TIME);
			if (!StringUtil.isEmpty(strAccessTime) && !StringUtil.isEmpty(strPickupDateTime)) {
				strValidTill = calculateValidTill(strPickupDateTime, strAccessTime);
			}

		} else {

			parkRequestsOnError(envObj, docPickAvailServiceReq, docAcadFedxPingList, props, mPickAvailRes);
		}

		logger.verbose("End - invokePickupAvailServiceAndGetValidTill() :: ");

		return strValidTill;
	}

	/*
	 * Invoke FedEx CreatePickup Service and get response. Take action based on
	 * HighestSeverity.
	 */
	public static void createPickupReqAndGetResponse(YFSEnvironment envObj, Document docAcadFedxExpPingList,
			Document docCreatePickupServiceReq, Properties props) throws Exception {

		logger.verbose("Begin - invokePickupAvailServiceAndGetValidTill() :: ");

		Document docCreatePickupServiceRes = AcademyUtil.invokeService(envObj,
				AcademyConstants.FEDEX_EXPRESS_PICKUP_REQ_SERVICE, docCreatePickupServiceReq);

		Map<String, String> mCreatePickupRes = parseFedexResAndGetDetails(docCreatePickupServiceRes,
				AcademyConstants.STR_CREATE_PICKUP_RES);

		String strResponse = mCreatePickupRes.get(AcademyConstants.STR_RESPONSE);

		if (AcademyConstants.STR_SUCCESS.equalsIgnoreCase(strResponse)) {

			logger.verbose("Success Response :: ");

			if (IsPingEntryExistsWithIsProcessedFlag(docAcadFedxExpPingList,
					docCreatePickupServiceReq.getDocumentElement().getAttribute(AcademyConstants.ATTR_CURRENT_DATE),
					AcademyConstants.STR_NO)) {
				modifyAcadFedxExpressPing(envObj,
						docCreatePickupServiceReq.getDocumentElement().getAttribute(AcademyConstants.ATTR_CURRENT_DATE),
						docCreatePickupServiceReq.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE),
						docCreatePickupServiceReq.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO),
						docCreatePickupServiceReq.getDocumentElement().getAttribute(AcademyConstants.ATTR_VALID_TILL),
						AcademyConstants.STR_YES);
			} else {
				createAcadFedxExpressPing(envObj,
						docCreatePickupServiceReq.getDocumentElement().getAttribute(AcademyConstants.ATTR_CURRENT_DATE),
						docCreatePickupServiceReq.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE),
						docCreatePickupServiceReq.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO),
						docCreatePickupServiceReq.getDocumentElement().getAttribute(AcademyConstants.ATTR_VALID_TILL),
						AcademyConstants.STR_YES);
			}
		} else {

			logger.verbose("Error Response :: ");

			parkRequestsOnError(envObj, docCreatePickupServiceReq, docAcadFedxExpPingList, props, mCreatePickupRes);
		}

		parseCreatePickupResAndRaiseAlert(envObj, docCreatePickupServiceReq, docCreatePickupServiceRes);

		logger.verbose("End - invokePickupAvailServiceAndGetValidTill() :: ");
	}

	/*
	 * Create entries in ACAD_FEDX_EXPRESS_PING to get processed SAME/FUTURE Day.
	 */
	public static void parkCreatePickupReq(YFSEnvironment envObj, Document docFedxPingList, String strRequestedDate,
			String strShipNode, String strShipmentNo) throws Exception {

		logger.verbose("Begin - parkCreatePickupReq() :: ");

		if (!IsPingEntryExistsWithIsProcessedFlag(docFedxPingList, strRequestedDate, AcademyConstants.STR_NO)) {

			createAcadFedxExpressPing(envObj, strRequestedDate, strShipNode, strShipmentNo, AcademyConstants.STR_BLANK,
					AcademyConstants.STR_NO);
		}

		logger.verbose("End - parkCreatePickupReq() :: ");
	}

	/*
	 * Fetch ShipNode details using getShipNodeList API.
	 */
	public static Document invokeGetShipNodeListApi(YFSEnvironment env, String strShipNode) throws Exception {

		Document docInpShipNodeList = null;
		Document docOutShipNodeList = null;

		logger.verbose("Begin - invokeGetShipNodeListApi() :: ");

		docInpShipNodeList = XMLUtil.createDocument(AcademyConstants.ELE_SHIP_NODE);
		Element eleInpShipNodeList = docInpShipNodeList.getDocumentElement();
		eleInpShipNodeList.setAttribute(AcademyConstants.ATTR_SHIP_NODE, strShipNode);

		env.setApiTemplate(AcademyConstants.API_GET_SHIP_NODE_LIST,
				AcademyConstants.STR_TEMPLATE_FILE_GET_SHIP_NODE_LIST_FEDX_PING);

		logger.verbose("Input - getShipNodeList API :: " + XMLUtil.getXMLString(docInpShipNodeList));

		docOutShipNodeList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIP_NODE_LIST, docInpShipNodeList);

		logger.verbose("Output - getShipNodeList API :: " + XMLUtil.getXMLString(docOutShipNodeList));

		env.clearApiTemplate(AcademyConstants.API_GET_SHIP_NODE_LIST);

		logger.verbose("End - invokeGetShipNodeListApi() :: ");

		return docOutShipNodeList;
	}

	/**
	 * This method will validate whether the Fedx response is Success or Failure
	 * 
	 * @param env
	 * @param input
	 * @param docResponse
	 * @throws Exception
	 */
	private static void parseCreatePickupResAndRaiseAlert(YFSEnvironment env, Document input, Document docResponse)
			throws Exception {
		logger.verbose("parseCreatePickupResAndRaiseAlert: START");
		Element eleHighestSeverity = (Element) docResponse.getElementsByTagName(AcademyConstants.ELE_HIGHEST_SEVERITY)
				.item(0);
		if (!YFCObject.isVoid(eleHighestSeverity)) {
			// if the element is not void
			if (AcademyConstants.STR_SUCCESS.equalsIgnoreCase(eleHighestSeverity.getTextContent())
					|| AcademyConstants.STR_WARNING.equalsIgnoreCase(eleHighestSeverity.getTextContent())
					|| AcademyConstants.STR_NOTE.equalsIgnoreCase(eleHighestSeverity.getTextContent())) {
				createAlert(env, input, docResponse, true);
			} else {
				createAlert(env, input, docResponse, false);
			}
		} else {
			logger.verbose("Fedx has not returned HighestSeverity Element ");
			createAlert(env, input, docResponse, false);
		}
		logger.verbose("parseCreatePickupResAndRaiseAlert: END");
	}

	/**
	 * This method will raise an alert in both the cases that is Success and Failure
	 * 
	 * @param env
	 * @param input
	 * @param docResponse
	 * @param isSuccess
	 * @throws Exception
	 */
	private static void createAlert(YFSEnvironment env, Document input, Document docResponse, boolean isSuccess)
			throws Exception {
		logger.verbose("createAlert Method: START");
		Element eleRoot = null;
		String strShipmentNo = "";
		String strShipNode = "";
		Element eleInboxRef = null;
		Element eleInboxRefLst = null;
		String sErrorDesc = "";
		String sErrorCode = "";
		Element eleMessage = null;
		Element eleCode = null;
		Element eleDesc = null;

		eleRoot = input.getDocumentElement();
		strShipmentNo = eleRoot.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
		strShipNode = eleRoot.getAttribute(AcademyConstants.ATTR_SHIP_NODE);

		eleMessage = (Element) docResponse.getElementsByTagName(AcademyConstants.ATTR_MESSAGE).item(0);
		if (!YFCObject.isVoid(eleMessage)) {
			sErrorDesc = eleMessage.getTextContent();
		} else {
			// Message element will not present if ContainerGrossWeightUOM is not LB.we have
			// put this logic to handle similar type of error
			eleDesc = (Element) docResponse.getElementsByTagName(AcademyConstants.ATTR_FEDX_PICKUP_FAILURE_DESC)
					.item(0);
			sErrorDesc = eleDesc.getTextContent();
		}

		eleCode = (Element) docResponse.getElementsByTagName(AcademyConstants.ATTR_CODE).item(0);
		if (!YFCObject.isVoid(eleCode)) {
			sErrorCode = eleCode.getTextContent();
		}

		Document inputCreateException = XMLUtil.createDocument(AcademyConstants.ELE_INBOX);
		Element rootEle = inputCreateException.getDocumentElement();
		rootEle.setAttribute(AcademyConstants.ATTR_ACTIVE_FLAG, AcademyConstants.STR_YES);
		rootEle.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
		rootEle.setAttribute(AcademyConstants.ATTR_SHIP_NODE_KEY, strShipNode);

		eleInboxRefLst = inputCreateException.createElement(AcademyConstants.ELE_INBOX_REF_LIST);
		rootEle.appendChild(eleInboxRefLst);
		// InboxRef for Fedex Response Code
		eleInboxRef = inputCreateException.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
		eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_FEDX_RESP_CODE);
		eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE, AcademyConstants.STR_EXCPTN_REF_VALUE);
		eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE, sErrorCode);
		eleInboxRefLst.appendChild(eleInboxRef);
		// InboxRef for Fedex Response Message
		eleInboxRef = inputCreateException.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
		eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_FEDX_RESP_MSG);
		eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE, AcademyConstants.STR_EXCPTN_REF_VALUE);
		eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE, sErrorDesc);
		eleInboxRefLst.appendChild(eleInboxRef);

		if (isSuccess) {
			rootEle.setAttribute(AcademyConstants.ATTR_EXCPTN_TYPE,
					AcademyConstants.FDX_CREATE_PICK_REQ_SUCCESS_ALERT_TYPE);
			rootEle.setAttribute("Description",
					"Fedex Express Pickup Request successfully created for Shipment No:" + strShipmentNo);

			String sPickConfirmNo = docResponse.getElementsByTagName(AcademyConstants.ELE_PICKUP_CONF_NUM).item(0)
					.getTextContent();
			eleInboxRef = inputCreateException.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_PICKUP_CONF_NUM);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE, AcademyConstants.STR_EXCPTN_REF_VALUE);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE, sPickConfirmNo);
			eleInboxRefLst.appendChild(eleInboxRef);

		} else {
			rootEle.setAttribute(AcademyConstants.ATTR_EXCPTN_TYPE,
					AcademyConstants.FDX_CREATE_PICK_REQ_FAILURE_ALERT_TYPE);
			rootEle.setAttribute("Description", "Fedex Express Pickup Request failed for Shipment No:" + strShipmentNo);
		}

		logger.verbose("createAlert : Input Doc" + XMLUtil.getXMLString(inputCreateException));
		AcademyUtil.invokeAPI(env, AcademyConstants.API_CREATE_EXCEPTION, inputCreateException);
		logger.verbose("createAlert Method: END");
	}

}
