package com.academy.ecommerce.sterling.shipment;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyFetchScanBatchDetails {
	private Properties props;

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyFetchScanBatchDetails.class);

	/**
	 * This method is invoked on click of  'Finish' button on UI, while Finishing the scan batch process for a particular Batch
	 * 
	 * This method takes batch key and Store No as input and returns Number of active shipments(Shipments in RFCP+Paper Work Initiated ),
	 * Total Shipments scanned in the batch, Number of shipments cancelled  and total duration of the shipment in ACAD_SCAN_BATCH_DETAILS 
	 * for the corresponding batch key and Store No
	 * 
	 * JIRA - OMNI - 67018 - Returning the Shipment details to the UI along with Scanned and Cancelled count in a batch
	 * 
	 * Sample I/p:
	 * <ACADScanBatchHeader AcadScanBatchHeaderKey ="20220426023735268415246" StoreNo="033"/>
	 *
	 *Sample Output:
	 *<ACADScanBatchDetails AcadScanBatchHeaderKey="20220328050809265477236"
	    	BatchScanDuration="24:0:55" ShipmentsCancelled="3" ShipmentsScanned="4" StagedShipmentsCount="5"/>
	 */

	public Document returnBatchSummaryDetails (YFSEnvironment env, Document inDoc)  {
		log.beginTimer(this.getClass() + ".returnBatchSummaryDetails");
		log.verbose("AcademyFetchScanBatchDetails.java:returnBatchSummaryDetails():InDoc " + XMLUtil.getXMLString(inDoc));
		Document docScanBatchDetailsOutDoc =null;
		try {
			
			// OMNI-70375 START
			String strCalculateScanSummaryCounts = props.getProperty(AcademyConstants.CALCULATE_SCAN_ACTIVITY_SUMMARY);
			log.verbose("CalculateScanSummaryCounts:: " + strCalculateScanSummaryCounts);
			// OMNI-70375 END
			String strAcadScanBatchHdrKey = XMLUtil.getAttributeFromXPath(inDoc, AcademyConstants.XPATH_ACAD_SCAN_BATCH_HEADER_KEY);
			//Fetching the current timestamp to calculate the time duration
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdfDateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			Date currentTimeStamp = sdfDateFormat.parse(sdfDateFormat.format(cal.getTime()));
			String strCurrentTimeStamp = sdfDateFormat.format(cal.getTime());
			log.verbose("SystemDate :: " + strCurrentTimeStamp);

			//Invoke AcademyGetScanBatchHeaderListService service to get startTime and TotalShipments for Batch
			Document docgetScanBatchHdrListOutput = AcademyUtil.invokeService(env, AcademyConstants.ACADEMY_GET_SCAN_BATCH_HDR_LIST_SERVICE, inDoc);
			log.verbose("Output of AcademyGetScanBatchHeaderListService :: " +XMLUtil.getXMLString(docgetScanBatchHdrListOutput));
			String strTotalNoOfShipments = XMLUtil.getAttributeFromXPath(docgetScanBatchHdrListOutput, AcademyConstants.XPATH_TOTAL_SHIPMENT_COUNT_FROM_HDR);
			String strBatchStartTime = XMLUtil.getAttributeFromXPath(docgetScanBatchHdrListOutput, AcademyConstants.XPATH_ACAD_SCAN_BATCH_START_TIME);
			//invoking calculateElapsedTime() to get elapsed time
			String strBatchScanDuration = calculateElapsedTime(env , strBatchStartTime, currentTimeStamp);
			//invoking getCancelledShipmentCount() to get total number of cancelled shipments scanned as part of that batch
			String strCancelledShipmentCount = getCancelledShipmentCount(env, inDoc);
			
			//Invoke AcademyStoreActiveShipmentStatusCountService to get Count of active shipments for store
			Element eleAcadScanBatchHdr= inDoc.getDocumentElement();
			eleAcadScanBatchHdr.setAttribute(AcademyConstants.STR_ACTION, AcademyConstants.STR_OPEN);
			
			// OMNI-70029 START
			if (!YFCCommon.isVoid(strCalculateScanSummaryCounts) && AcademyConstants.ATTR_Y.equals(strCalculateScanSummaryCounts)) {//Add jira commentsChange the condition constants.attribute
				eleAcadScanBatchHdr.setAttribute(AcademyConstants.ATTR_GET_SHIPMENT_NO_LIST, AcademyConstants.ATTR_Y);
			}
			// OMNI-70029 END
			log.verbose("Input to AcademyStoreActiveShipmentStatusCount:: " +XMLUtil.getXMLString(inDoc));
			Document docActiveShipmentsCountOutput=AcademyUtil.invokeService(env, AcademyConstants.ACADEMY_FETCH_ACTIVE_SHIPMENT_COUNT_SERVICE, inDoc);
			log.verbose("Output of AcademyStoreActiveShipmentStatusCount:: " +XMLUtil.getXMLString(docActiveShipmentsCountOutput));

			Element eleActiveShipmentsCountOutput= docActiveShipmentsCountOutput.getDocumentElement();
			String strStagedShipmentsCount=eleActiveShipmentsCountOutput.getAttribute(AcademyConstants.STR_STAGED_SHIPMENTS_COUNT);
			log.verbose("StagedShipmentsCount:: " + strStagedShipmentsCount);

			//Preparing Document with all the details to return to UI 
			docScanBatchDetailsOutDoc = XMLUtil.createDocument(AcademyConstants.STR_SCAN_BATCH_DTLS);
			Element eleACADScanBatchDetails= docScanBatchDetailsOutDoc.getDocumentElement();
			eleACADScanBatchDetails.setAttribute(AcademyConstants.ATTR_SCAN_BATCH_HDR_KEY, strAcadScanBatchHdrKey);
			eleACADScanBatchDetails.setAttribute(AcademyConstants.ATTR_SHIPMENTS_CANCELLED, strCancelledShipmentCount);
			eleACADScanBatchDetails.setAttribute(AcademyConstants.ATTR_SHIPMENTS_SCANNED, strTotalNoOfShipments); 
			eleACADScanBatchDetails.setAttribute(AcademyConstants.ATTR_BATCH_SCAN_DURATION, strBatchScanDuration);
			eleACADScanBatchDetails.setAttribute(AcademyConstants.STR_STAGED_SHIPMENTS_COUNT, strStagedShipmentsCount);
			
			// START - OMNI-70025,OMNI-70026,OMNI-70029, OMNI-70375
			String strCancelledAndNotScannedCount = null;
			
			/*OMNI-74019 Starts
			String strAbandonedAndNotScannedCount = null;
			OMNI-74019 Ends */
			
			String strMissedShipmentCount = null;
			Document docCancelledShipmentList = null;
			Set<String> hsCancelledAndScannedShipments = new HashSet<>();

			if (!YFCObject.isVoid(strCalculateScanSummaryCounts)
					&& (AcademyConstants.ATTR_Y.equals(strCalculateScanSummaryCounts))) {
				log.beginTimer("Entered the Scanning Activity Calculations");
				String strShipNode = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_STORE_NO);
				if (!YFCObject.isVoid(strShipNode)) {
					docCancelledShipmentList = invokeGetCancelledShipmentList(env, strShipNode);
					log.verbose("The TotalScannedShipment list for a batch:"
							+ XMLUtil.getXMLString(docgetScanBatchHdrListOutput));
					hsCancelledAndScannedShipments = getBatchCancelledAndScannedShipment(docgetScanBatchHdrListOutput);
				}
				strCancelledAndNotScannedCount = getCancelledAndNotScannedCount(docCancelledShipmentList,
						hsCancelledAndScannedShipments);
				
				/*OMNI-74019 Starts
				strAbandonedAndNotScannedCount = getAbdandonedAndNotScannedCount(docCancelledShipmentList,
						hsCancelledAndScannedShipments);
				OMNI-74019 Ends*/
				
				strMissedShipmentCount = getMissedShipmentCount(inDoc, docActiveShipmentsCountOutput,
						docgetScanBatchHdrListOutput);
				log.verbose("CancelledAndNotScannedCount:: " + strCancelledAndNotScannedCount);
				
				/*OMNI-74019 Starts
				log.verbose("AbandonedAndNotScannedCount:: " + strAbandonedAndNotScannedCount);
				OMNI-74019 Ends */
				
				log.verbose("MissedShipmentCount:: " + strMissedShipmentCount);
				eleACADScanBatchDetails.setAttribute(AcademyConstants.CANCELLED_AND_NOT_SCANNED,
						strCancelledAndNotScannedCount);
				
				/*OMNI-74019 Starts
				eleACADScanBatchDetails.setAttribute(AcademyConstants.ABANDONED_AND_NOT_SCANNED,
						strAbandonedAndNotScannedCount);
				OMNI-74019 Ends */
				
				eleACADScanBatchDetails.setAttribute(AcademyConstants.ATTR_MISSED_SHIPMENT, strMissedShipmentCount);
				eleACADScanBatchDetails.setAttribute(AcademyConstants.ATTR_SHOW_SCAN_ACTIVITY,
						strCalculateScanSummaryCounts);
				log.endTimer("completion of the Scanning Activity Calculations");
			} else {
				eleACADScanBatchDetails.setAttribute(AcademyConstants.ATTR_SHOW_SCAN_ACTIVITY, AcademyConstants.ATTR_N);
			}
			// END - OMNI-70025,OMNI-70026,OMNI-70029, OMNI-70375
			log.verbose("AcademyFetchScanBatchDetails.java:updateBacthHdrStatus():Output Document:: " +XMLUtil.getXMLString(docScanBatchDetailsOutDoc));
		}
		catch (Exception e){
			e.printStackTrace(); 
			log.verbose("Exception occurred while invoking AcademyFetchScanBatchDetails.java:returnBatchSummaryDetails() with input:\n"
					+ XMLUtil.getXMLString(inDoc));
		}

		return docScanBatchDetailsOutDoc;
	}
	
	public Document returnBatchSummaryDetailsForContinue (YFSEnvironment env, Document inDoc)  {
		log.beginTimer(this.getClass() + ".returnBatchSummaryDetailsForContinue");
		log.verbose("AcademyFetchScanBatchDetails.java:returnBatchSummaryDetailsForContinue():InDoc " + XMLUtil.getXMLString(inDoc));
		Document docScanBatchDetailsOutDoc =null;
		try {

			String strAcadScanBatchHdrKey = XMLUtil.getAttributeFromXPath(inDoc, AcademyConstants.XPATH_ACAD_SCAN_BATCH_HEADER_KEY);
			//Fetching the current timestamp to calculate the time duration
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdfDateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			Date currentTimeStamp = sdfDateFormat.parse(sdfDateFormat.format(cal.getTime()));
			String strCurrentTimeStamp = sdfDateFormat.format(cal.getTime());
			log.verbose("SystemDate :: " + strCurrentTimeStamp);

			//Invoke AcademyGetScanBatchHeaderListService service to get startTime and TotalShipments for Batch
			Document docgetScanBatchHdrListOutput = AcademyUtil.invokeService(env, AcademyConstants.ACADEMY_GET_SCAN_BATCH_HDR_LIST_SERVICE, inDoc);
			log.verbose("Output of AcademyGetScanBatchHeaderListService :: " +XMLUtil.getXMLString(docgetScanBatchHdrListOutput));
			String strTotalNoOfShipments = XMLUtil.getAttributeFromXPath(docgetScanBatchHdrListOutput, AcademyConstants.XPATH_TOTAL_SHIPMENT_COUNT_FROM_HDR);
			String strBatchStartTime = XMLUtil.getAttributeFromXPath(docgetScanBatchHdrListOutput, AcademyConstants.XPATH_ACAD_SCAN_BATCH_START_TIME);
			//invoking calculateElapsedTime() to get elapsed time
			String strBatchScanDuration = calculateElapsedTime(env , strBatchStartTime, currentTimeStamp);
			//invoking getCancelledShipmentCount() to get total number of cancelled shipments scanned as part of that batch
			String strCancelledShipmentCount = getCancelledShipmentCount(env, inDoc);

			//Invoke AcademyStoreActiveShipmentStatusCountService to get Count of active shipments for store
			Element eleAcadScanBatchHdr= inDoc.getDocumentElement();
			eleAcadScanBatchHdr.setAttribute(AcademyConstants.STR_ACTION, AcademyConstants.STR_OPEN);
			log.verbose("Input to AcademyStoreActiveShipmentStatusCount:: " +XMLUtil.getXMLString(inDoc));
			Document docActiveShipmentsCountOutput=AcademyUtil.invokeService(env, AcademyConstants.ACADEMY_FETCH_ACTIVE_SHIPMENT_COUNT_SERVICE, inDoc);
			log.verbose("Output of AcademyStoreActiveShipmentStatusCount:: " +XMLUtil.getXMLString(docActiveShipmentsCountOutput));

			Element eleActiveShipmentsCountOutput= docActiveShipmentsCountOutput.getDocumentElement();
			String strStagedShipmentsCount=eleActiveShipmentsCountOutput.getAttribute(AcademyConstants.STR_STAGED_SHIPMENTS_COUNT);
			log.verbose("StagedShipmentsCount:: " + strStagedShipmentsCount);

			//Preparing Document with all the details to return to UI 
			docScanBatchDetailsOutDoc = XMLUtil.createDocument(AcademyConstants.STR_SCAN_BATCH_HDR);
			Element eleACADScanBatchDetails= docScanBatchDetailsOutDoc.getDocumentElement();
			eleACADScanBatchDetails.setAttribute(AcademyConstants.ATTR_SCAN_BATCH_HDR_KEY, strAcadScanBatchHdrKey);
			eleACADScanBatchDetails.setAttribute(AcademyConstants.ATTR_SHIPMENTS_CANCELLED, strCancelledShipmentCount);
			eleACADScanBatchDetails.setAttribute(AcademyConstants.ATTR_SHIPMENTS_SCANNED, strTotalNoOfShipments); 
			eleACADScanBatchDetails.setAttribute(AcademyConstants.ATTR_BATCH_SCAN_DURATION, strBatchScanDuration);
			eleACADScanBatchDetails.setAttribute(AcademyConstants.STR_STAGED_SHIPMENTS_COUNT, strStagedShipmentsCount);
			log.verbose("AcademyFetchScanBatchDetails.java:returnBatchSummaryDetailsForContinue():Output Document:: " +XMLUtil.getXMLString(docScanBatchDetailsOutDoc));
		}
		catch (Exception e){
			e.printStackTrace(); 
			log.verbose("Exception occurred while invoking AcademyFetchScanBatchDetails.java:returnBatchSummaryDetailsForContinue() with input:\n"
					+ XMLUtil.getXMLString(inDoc));
		}

		return docScanBatchDetailsOutDoc;
	}

	private static String calculateElapsedTime(YFSEnvironment env, String strBatchStartTime,Date currentTimeStamp) throws ParseException {
		//fetch current timestamp
		Date dateBatchStartTime=new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN).parse(strBatchStartTime);  

		long lngDifference = currentTimeStamp.getTime() - dateBatchStartTime.getTime();

		log.verbose("Batch Start Time ::" + dateBatchStartTime);
		log.verbose("Batch End Time :: " + currentTimeStamp);
		log.verbose("Difference in milliseconds" + lngDifference);
		long secondsInMilli = 1000;
		long minutesInMilli = secondsInMilli * 60;
		long hoursInMilli = minutesInMilli * 60;
		//long daysInMilli = hoursInMilli * 24;

		long elapsedHours = lngDifference / hoursInMilli;
		lngDifference = lngDifference % hoursInMilli;
		String strElapsedHours = Long.toString(elapsedHours);
		if (elapsedHours < 10) {
			strElapsedHours = "0".concat(strElapsedHours);
		}

		long elapsedMinutes = lngDifference / minutesInMilli;
		lngDifference = lngDifference % minutesInMilli;
		String strElapsedMinutes = Long.toString(elapsedMinutes);
		if (elapsedMinutes < 10) {
			strElapsedMinutes = "0".concat(strElapsedMinutes);
		}

		long elapsedSeconds = lngDifference / secondsInMilli;
		String strElapsedSeconds = Long.toString(elapsedSeconds);
		if (elapsedSeconds < 10) {
			strElapsedSeconds = "0".concat(strElapsedSeconds);
		}


		String strBatchScanDuration = strElapsedHours.concat(":").concat(strElapsedMinutes).concat(":").concat(strElapsedSeconds);  
		log.verbose("Batch Scan Duration :: " + strBatchScanDuration);
		return strBatchScanDuration;
	}

	/*This methord gets the shipments cancelled count for particular batch by sending Is_Cancelled attribute as Y

	Sample i/p to service AcademyScanBatchDtlListService_getTotalRecords : <ACADScanBatchHeader AcadScanBatchHeaderKey="20220328050809265477236" IsCancelled="Y"/> 
	Sample O/p : <ACADScanBatchDetailsList TotalNumberOfRecords="3"/>*/

	private static String getCancelledShipmentCount(YFSEnvironment env, Document docCancelShipmentCountInDoc) throws Exception {
		log.verbose("Input of AcademyFetchScanBatchDetails.getCancelledShipmentCount() :: "   +XMLUtil.getXMLString(docCancelShipmentCountInDoc));
		Element eleACADScanBatchDetails= docCancelShipmentCountInDoc.getDocumentElement();
		eleACADScanBatchDetails.setAttribute(AcademyConstants.ATTR_IS_CANCELLED, AcademyConstants.STR_YES);
		Document docGetScanBatchDetailsOutput = AcademyUtil.invokeService(env, AcademyConstants.ACADEMY_SCAN_BATCH_DTL_LIST_SERVICE_TOTAL_RECORDS, docCancelShipmentCountInDoc);
		log.verbose("Output of AcademyScanBatchDtlListService_getTotalRecords :: "   +XMLUtil.getXMLString(docGetScanBatchDetailsOutput));
		String strCancelledShipmentCount = XMLUtil.getAttributeFromXPath(docGetScanBatchDetailsOutput, AcademyConstants.XPATH_CANCELLED_COUNT);
		log.verbose("Cancelled Shipment Count :: " + strCancelledShipmentCount);

		return strCancelledShipmentCount;
	}

	/*Input:
	<ACADScanBatchHeader   AcadScanBatchHeaderKey="20222403061135" />
	Output:
	<ACADScanBatchDetails ShipmentsCancelled="" ShipmentsScanned="" BatchScanDuration="" />*/

	public static Document getBatchShipmentCount (YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("Input of AcademyFetchScanBatchDetails.getBatchShipmentCount() :: "   +XMLUtil.getXMLString(inDoc));
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdfDateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
		Date currentTimeStamp = sdfDateFormat.parse(sdfDateFormat.format(cal.getTime()));
		log.verbose("SystemDate :: " + currentTimeStamp);
		env.setApiTemplate(AcademyConstants.ACADEMY_GET_SCAN_BATCH_HDR_LIST_SERVICE, AcademyConstants.ACADEMY_SCAN_BATCH_HDR_LIST_TEMPLATE);
		Document docGetScanBatchHdrListOutput = AcademyUtil.invokeService(env, AcademyConstants.ACADEMY_GET_SCAN_BATCH_HDR_LIST_SERVICE, inDoc);
		env.clearApiTemplates();
		String strTotalNoOfShipments = XMLUtil.getAttributeFromXPath(docGetScanBatchHdrListOutput, AcademyConstants.XPATH_TOTAL_SHIPMENT_COUNT);
		String strCancelledShipmentCount = getCancelledShipmentCount (env, inDoc);
		String strStartTime = XMLUtil.getAttributeFromXPath(docGetScanBatchHdrListOutput, AcademyConstants.XPATH_START_TIME);
		String strElapsedTime  = calculateElapsedTime(env,strStartTime,currentTimeStamp );
		Document docGetBatchShipmentCount = XMLUtil.createDocument(AcademyConstants.STR_SCAN_BATCH_DTLS);
		Element eleACADScanBatchDetails= docGetBatchShipmentCount.getDocumentElement();
		eleACADScanBatchDetails.setAttribute(AcademyConstants.ATTR_SHIPMENTS_CANCELLED, strCancelledShipmentCount);
		eleACADScanBatchDetails.setAttribute(AcademyConstants.ATTR_SHIPMENTS_SCANNED, strTotalNoOfShipments); 
		eleACADScanBatchDetails.setAttribute(AcademyConstants.ATTR_BATCH_SCAN_DURATION, strElapsedTime);
		log.verbose("Output of AcademyFetchScanBatchDetails.getBatchShipmentCount() :: "   +XMLUtil.getXMLString(docGetBatchShipmentCount));
		return docGetBatchShipmentCount;
	} 
	
	// OMNI-70373, OMNI-70026, OMNI-70029 START
	public void setProperties(Properties props) {
		this.props = props;
	}
	// OMNI-70373, OMNI-70026, OMNI-70029 START

	// OMNI-70026 START
	/**
	 * This method compares the two sets and finds the number of shipments which are
	 * not in the 2nd set. 
	 * 
	 * @param alCancelledShipNo
	 * @param alScanBatchShipNo
	 * @return
	 */
	private String getCount(Set<String> hsSet1, Set<String> hsSet2) {
		log.beginTimer(this.getClass() + ".getCount");
		String strFinalCount = "0";
		Set<String> hsFinal = new HashSet<>();
		try {
			if (!hsSet1.isEmpty()) {
				if (hsSet2.isEmpty()) {
					hsFinal = hsSet1;
				} else if (!hsSet2.isEmpty()) {
					for (String shipmentNo : hsSet1) {
						if (!hsSet2.contains(shipmentNo)) {
							hsFinal.add(shipmentNo);
						}
					}
				}
				strFinalCount = String.valueOf(hsFinal.size());				
			}
		} catch (Exception x) {
			x.printStackTrace();
			log.verbose("Exception in AcademyFetchScanBatchDetails.prepareScanBatchOutput()");
		}
		log.verbose("The final count is :: " + hsFinal + "\nLength: " + hsFinal.size());
		log.endTimer(this.getClass() + ".getCount");
		return strFinalCount;
	}

	/**
	 * Getting the Date of past n days in the format yyyy-MM-ddT00:00:00 
	 * with 12AM time.
	 * 
	 * @return strYesterday
	 */
	private String setPastDateToCancel(String strDaysToCancel) {

		log.beginTimer(this.getClass() + ".setPastDateToCancel");
		String strPastDay = "";
		int noOfDaysToCancel = Integer.parseInt(strDaysToCancel);
		Date pastday = DateUtils.addDays(new Date(), -noOfDaysToCancel);
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_SIMPLE_DATE_PATTERN);
		strPastDay = sdf.format(pastday);

		StringBuilder cancelDay = new StringBuilder();
		cancelDay.append(strPastDay);
		cancelDay.append(AcademyConstants.STR_12AM_TIME);

		log.verbose("**Past Date to consider for Cancel Shipments : " + cancelDay.toString());
		log.endTimer(this.getClass() + ".setPastDateToCancel");
		return cancelDay.toString();
	}

	/**
	 * This method prepares input using complex query and invokes the
	 * getShipmentList api. PICK Shipments at current ShipNode - that were Cancelled
	 * from RCP & Paperwork initiated status since past n days.
	 * 
	 * @param env
	 * @param strShipNode
	 * @return docGetShipmentListOutput
	 */
	private Document invokeGetCancelledShipmentList(YFSEnvironment env, String strShipNode) {
		log.beginTimer(this.getClass() + ".invokeGetCancelledShipmentList");
		log.verbose("**AcademyFetchScanBatchDetails.invokeGetCancelledShipmentList : Start **");
		Document docGetShipmentListOutput = null;
		Document docGetShipmentListTemplate = null;
		Document docGetShipmentListInput = null;
		Element eleShipment = null;
		Element eleShipmentStatusAudits = null;
		Element eleShipmentStatusAudit = null;
		Element eleComplexQuery = null;
		Element eleAnd = null;
		Element eleOr = null;
		Element eleExp = null;

		try {
			String strDaysToCancel = props.getProperty(AcademyConstants.NO_OF_DAYS_CANCEL);
			String strPastDay = setPastDateToCancel(strDaysToCancel);
			docGetShipmentListInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			eleShipment = docGetShipmentListInput.getDocumentElement();
			eleShipment.setAttribute(AcademyConstants.ATTR_SHIP_NODE, strShipNode);
			eleShipment.setAttribute(AcademyConstants.ATTR_DELIVERY_METHOD, AcademyConstants.STR_PICK_DELIVERY_METHOD);

			eleShipmentStatusAudits = docGetShipmentListInput
					.createElement(AcademyConstants.ELE_SHIPMENT_STATUS_AUDITS);
			eleShipmentStatusAudit = docGetShipmentListInput.createElement(AcademyConstants.ELE_SHIPMENT_STATUS_AUDIT);
			eleShipmentStatusAudit.setAttribute(AcademyConstants.ATTR_NEW_STATUS,
					AcademyConstants.VAL_CANCELLED_STATUS);

			eleComplexQuery = docGetShipmentListInput.createElement(AcademyConstants.COMPLEX_QRY_ELEMENT);
			eleAnd = docGetShipmentListInput.createElement(AcademyConstants.COMPLEX_AND_ELEMENT);
			eleOr = docGetShipmentListInput.createElement(AcademyConstants.COMPLEX_OR_ELEMENT);

			eleExp = docGetShipmentListInput.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
			eleExp.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_OLD_STATUS);
			eleExp.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
			eleExp.setAttribute(AcademyConstants.ATTR_VALUE, AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS);
			eleOr.appendChild(eleExp);

			eleExp = docGetShipmentListInput.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
			eleExp.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_OLD_STATUS);
			eleExp.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
			eleExp.setAttribute(AcademyConstants.ATTR_VALUE, AcademyConstants.STR_PAPER_WORK_INITIATED_STATUS);
			eleOr.appendChild(eleExp);
			eleAnd.appendChild(eleOr);

			eleExp = docGetShipmentListInput.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
			eleExp.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.STR_NEW_STATUS);
			eleExp.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.GT_QRY_TYPE);
			eleExp.setAttribute(AcademyConstants.ATTR_VALUE, strPastDay);
			eleAnd.appendChild(eleExp);
			eleComplexQuery.appendChild(eleAnd);
			eleShipmentStatusAudit.appendChild(eleComplexQuery);
			eleShipmentStatusAudits.appendChild(eleShipmentStatusAudit);
			eleShipment.appendChild(eleShipmentStatusAudits);

			docGetShipmentListTemplate = YFCDocument.getDocumentFor(AcademyConstants.ACAD_SCAN_GET_SHIPMENT_LIST)
					.getDocument();
			env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, docGetShipmentListTemplate);
			docGetShipmentListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST,
					docGetShipmentListInput);
			env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
			log.verbose("AcademyFetchScanBatchDetails.docgetShipmentListOutput:: \n"
					+ XMLUtil.getXMLString(docGetShipmentListOutput));
		} catch (Exception e) {
			e.printStackTrace();
			log.verbose("Exception in AcademyFetchScanBatchDetails.invokeGetCancelledShipmentList()");
		}

		log.verbose("**AcademyFetchScanBatchDetails.invokeGetCancelledShipmentList : End **");
		log.endTimer(this.getClass() + ".invokeGetCancelledShipmentList");
		return docGetShipmentListOutput;
	}

	/**
	 * Adding the set of Cancelled Shipment numbers to a set.
	 * 
	 * @param docCancelledShipmentList
	 * @return alCancelledShipNo
	 */
	private Set<String> getCancelledShipments(Document docCancelledShipmentList) {
		log.beginTimer(this.getClass() + ".getCancelledShipments");
		Set<String> hsCancelledShipments = new HashSet<>();
		try {
			if (!YFCCommon.isVoid(docCancelledShipmentList)) {
				NodeList nlShipment = docCancelledShipmentList.getDocumentElement()
						.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
				for (int i = 0; i < nlShipment.getLength(); i++) {
					Element eleShipment = (Element) nlShipment.item(i);
					String shipmentNo = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
					hsCancelledShipments.add(shipmentNo);
				}
				
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			log.verbose("Exception in AcademyFetchScanBatchDetails.addCancelledShipNoFromApi()");
		}
		log.verbose("Cancelled Shipments:: " + hsCancelledShipments + "\nLength:: " + hsCancelledShipments.size());
		log.beginTimer(this.getClass() + ".getCancelledShipments");
		return hsCancelledShipments;
	}

	/**
	 * Adding set of Scanned Shipment numbers to a set.
	 * 
	 * @param docScanBatchHdrListOutput
	 * @return alScanBatchShipNo
	 */
	private Set<String> getBatchCancelledAndScannedShipment(Document docScanBatchHdrListOutput) {
		log.beginTimer(this.getClass() + ".getBatchCancelledAndScannedShipment");
		log.verbose(
				"The input to getBatchCancelledAndScannedShipment" + XMLUtil.getXMLString(docScanBatchHdrListOutput));
		Set<String> hsScanBatchCancelledShipNo = new HashSet<>();
		try {
			if (!YFCCommon.isVoid(docScanBatchHdrListOutput)) {
				NodeList nlScanBatchDetails = XPathUtil.getNodeList(docScanBatchHdrListOutput.getDocumentElement(),
						AcademyConstants.XPATH_IS_CANCELLED);
				log.verbose("The Scanned Shipment List with Cancelled flag as Y" + nlScanBatchDetails);
				if (!YFCCommon.isVoid(nlScanBatchDetails)) {
					for (int i = 0; i < nlScanBatchDetails.getLength(); i++) {
						Element eleScanBatchDetails = (Element) nlScanBatchDetails.item(i);
						String shipmentNo = eleScanBatchDetails.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
						hsScanBatchCancelledShipNo.add(shipmentNo);
					}
				}
				log.verbose("Shipments from ScanBatch :: " + hsScanBatchCancelledShipNo + "\nLength: "
						+ hsScanBatchCancelledShipNo.size());
			}
		} catch (Exception xp) {
			xp.printStackTrace();
			log.verbose("Exception in AcademyFetchScanBatchDetails.getBatchCancelledAndScannedShipment()");
		}
		log.endTimer(this.getClass() + ".getBatchCancelledAndScannedShipment");
		return hsScanBatchCancelledShipNo;
	}

	/**
	 * Input: <ACADScanBatchHeader AcadScanBatchHeaderKey="202204211140473173585120"
	 * StoreNo="033"/> Output:
	 * <ACADScanBatchDetails ShipmentsCancelled="" ShipmentsScanned=""
	 * BatchScanDuration="" CancelledAndNotScanned="" />
	 * 
	 * @param env
	 * @param inDoc
	 * @return scanBatchDetails
	 */
	public String getCancelledAndNotScannedCount(Document docCancelledShipmentList,
			Set<String> hsCancelledAndScannedShipments) {
		log.beginTimer(this.getClass() + ".getCancelledAndNotScannedCount");
		log.verbose("Input of AcademyFetchScanBatchDetails.getCancelledAndNotScannedCount() :: "
				+ XMLUtil.getXMLString(docCancelledShipmentList));
		String strCancelledAndNotScanned = "";
		Set<String> hsCancelledShipNo = new HashSet<>();

		try {
			hsCancelledShipNo = getCancelledShipments(docCancelledShipmentList);
			strCancelledAndNotScanned = getCount(hsCancelledShipNo, hsCancelledAndScannedShipments);
		} catch (Exception e) {
			e.printStackTrace();
			log.verbose("Exception in AcademyFetchScanBatchDetails.getCancelledAndNotScannedCount() with input:\n"
					+ XMLUtil.getXMLString(docCancelledShipmentList));
		}
		log.endTimer(this.getClass() + ".getCancelledAndNotScannedCount");
		return strCancelledAndNotScanned;
	}

	// OMNI-70026 END

	// OMNI-70373 START
	/**
	 * Getting the ShipmentNos into a set where
	 * Modifyprogid="ACAD_BOPIS_SHIP_MONITOR" and NewStatus="9000".
	 * 
	 * @param docCancelledShipmentList
	 * @return
	 */
	
	 /* OMNI-74019 starts
	public Set<String> getAbandonedShipments(Document docCancelledShipmentList) {
		log.beginTimer(this.getClass() + ".getAbandonedShipments");
		Set<String> hsAbandonedShipments = new HashSet<>();
		Element eleShipment = null;
		Node nlStatusAudit = null;
		String shipmentNo = null;
		try {
			if (!YFCCommon.isVoid(docCancelledShipmentList)) {
				NodeList nlShipment = docCancelledShipmentList.getDocumentElement()
						.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
				for (int i = 0; i < nlShipment.getLength(); i++) {
					eleShipment = (Element) nlShipment.item(i);
					nlStatusAudit = XPathUtil.getNode(nlShipment.item(i), AcademyConstants.XPATH_SHIPMENT_STATUS_AUDIT);
					if (!YFCCommon.isVoid(nlStatusAudit)) {
						shipmentNo = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
						log.verbose("shipmentNo :: " + shipmentNo);
						hsAbandonedShipments.add(shipmentNo);
					}
				}
				
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			log.verbose("Exception in AcademyFetchScanBatchDetails.addCancelledShipNoFromApi()");
		}
		log.verbose("Cancelled Shipments by ACAD_BOPIS_SHIP_MONITOR :: " + hsAbandonedShipments + "\nLength:: "	+ hsAbandonedShipments.size());
		log.endTimer(this.getClass() + ".getAbandonedShipments");
		return hsAbandonedShipments;
	}
	OMNI-74019 Ends */
	

	/**
	 * @param docCancelledShipmentList
	 * @param hsCancelledAndScannedShipments
	 * @return
	 */
		
	/* OMNI-74019 Starts
	public String getAbdandonedAndNotScannedCount(Document docCancelledShipmentList,
			Set<String> hsCancelledAndScannedShipments) {
		log.beginTimer(this.getClass() + ".getAbdandonedAndNotScannedCount");
		Set<String> hsAbandonedShipments = new HashSet<>();
		String strAbandonedAndNotScanned = null;
		hsAbandonedShipments = getAbandonedShipments(docCancelledShipmentList);
		strAbandonedAndNotScanned = getCount(hsAbandonedShipments, hsCancelledAndScannedShipments);
		log.endTimer(this.getClass() + ".getAbdandonedAndNotScannedCount");
		return strAbandonedAndNotScanned;
	}
	OMNI-74019 Ends */
	
	// OMNI-70373 END

	// OMNI-70029 START

	/**
	 * Adding list of RFCP Scanned Shipment numbers to a set.
	 * 
	 * @param inDoc
	 * @return hsRFCPScannedShipmentList
	 */

	private Set<String> getRFCPScannedShipmentList(Document docgetScanBatchHdrListOutput) {
		log.beginTimer(this.getClass() + ".getRFCPScannedShipmentList");
		log.verbose("Input of AcademyFetchScanBatchDetails.getRFCPScannedShipmentList() :: "
				+ XMLUtil.getXMLString(docgetScanBatchHdrListOutput));
		Set<String> hsRFCPScannedShipmentList = new HashSet<>();

		if (docgetScanBatchHdrListOutput != null) {
			NodeList nlScannedShipmentList = docgetScanBatchHdrListOutput.getDocumentElement()
					.getElementsByTagName(AcademyConstants.ACAD_SCAN_BATCH_DETAILS);
			/* Loop through each Scanned ShipmentList and add shipment Nos to a set */
			for (int i = 0; i < nlScannedShipmentList.getLength(); i++) {
				Element eleScannedShipment = (Element) nlScannedShipmentList.item(i);
				hsRFCPScannedShipmentList.add(eleScannedShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
			}
			
		}
		log.verbose("Scanned Shipment list :: " + hsRFCPScannedShipmentList + "\n Length: "		+ hsRFCPScannedShipmentList.size());
		log.endTimer(this.getClass() + ".getRFCPScannedShipmentList");
		return hsRFCPScannedShipmentList;

	}

	/**
	 * Input: <ACADScanBatchHeader AcadScanBatchHeaderKey="202204211140473173585120"
	 * StoreNo="033"/> Output: Count of Missed Shipment
	 * 
	 * @param env
	 * @param inDoc
	 * @param strNoOfDays
	 * @param strStatus
	 * @param docget
	 * @param ScanBatchHdrListOutput
	 * @return missedShipmentCount
	 */

	public String getMissedShipmentCount(Document inDoc, Document docActiveShipmentsCountOutput,
			Document docgetScanBatchHdrListOutput) throws Exception {
		log.beginTimer(this.getClass() + ".getMissedShipmentCount");
		log.verbose("Input of AcademyFetchScanBatchDetails.getMissedShipmentCount() :: " + XMLUtil.getXMLString(inDoc));
		String strMissedShipmentCount = "";
		Set<String> hsRFCPShipmentList = new HashSet<>();
		Set<String> hsRFCPScannedShipmentList = new HashSet<>();
		String strShipNode = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_STORE_NO);

		if (!YFCObject.isVoid(strShipNode)) {

			log.verbose("Output of RFCP getShipmentList :: " + XMLUtil.getXMLString(docActiveShipmentsCountOutput));
			if (docActiveShipmentsCountOutput != null) {
				NodeList nlRFCPShipmentList = XPathUtil.getNodeList(docActiveShipmentsCountOutput,
						AcademyConstants.XPATH_SHIPMENT);
				/* Loop through each shipment list and add shipment Nos to a set */
				for (int i = 0; i < nlRFCPShipmentList.getLength(); i++) {
					Element eleShipment1 = (Element) nlRFCPShipmentList.item(i);
					hsRFCPShipmentList.add(eleShipment1.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
				}
				log.verbose("Shipments in RFCP status :: " + hsRFCPShipmentList + "\n Length: "
						+ hsRFCPShipmentList.size());
			}
			/*
			 * invoke getRFCPScannedShipmentList to get Scanned ShipmentList and add to a
			 * set
			 */
			hsRFCPScannedShipmentList = getRFCPScannedShipmentList(docgetScanBatchHdrListOutput);
			strMissedShipmentCount = getCount(hsRFCPShipmentList, hsRFCPScannedShipmentList);
		}
		log.endTimer(this.getClass() + ".getMissedShipmentCount");
		return strMissedShipmentCount;
	}
	// OMNI-70029 END
}
