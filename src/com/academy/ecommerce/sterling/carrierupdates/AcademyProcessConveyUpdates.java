package com.academy.ecommerce.sterling.carrierupdates;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.ibm.icu.text.SimpleDateFormat;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dblayer.YFCContext;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyProcessConveyUpdates {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyProcessConveyUpdates.class);
	String strTrackingNo = null;
	String strShipmentNo = null;
	String strOrderNo = null;
	String strMasterStatus = null;
	String conveyReprocess = null;
	String strStatus = null;
	Document docGetConveyTrackingUpdateListOutput = null;
	
	//Define properties to fetch argument value from service configuration
    private Properties props;
	public void setProperties(Properties props) {
		this.props = props;
	}

	/**
	 * This method is invoked to read the messages from Convey and also
	 * translate the same and update the Shipment and container status
	 * 
	 * @param env
	 * @param inDoc
	 **Sample message from Convey
	<Shipment SCAC="" CarrierServiceCode="" TrackingNo="72806792562" EventType="tracking" OrderNo="" ShipmentNo="" Status="IT" >
		<Containers>
			<Container ContainerNo="" >
			</Container>
		</Containers>
		<EventDetails Description="" EventStatus="in_transit" EventDate="2023-04-21" EventTime="14:47:16" EventUtc="Z" />
	</Shipment>
	 */
	public Document processConveyUpdates(YFSEnvironment env, Document inDoc) {
		log.beginTimer("AcademyProcessConveyUpdates::processConveyUpdates");
		Document docGetShipmentList = null;
 
		String strStatusDate = null;
		String strDocumentType = null;
		String strShipmentStatus = null;
		String strStatusDesc = null; 
		String strLineType = null;

		try {
			// 0. concatenate StatusDate based on EventDate, EventTime, EventUtc
			Element eleInput = inDoc.getDocumentElement();
			strTrackingNo = eleInput.getAttribute(AcademyConstants.ATTR_TRACKING_NO);
			strStatus = eleInput.getAttribute(AcademyConstants.ATTR_STATUS);
			strStatusDate = getEventStatusDate(eleInput);
			log.verbose("0. After concatenation: StatusDate :: \n" + strStatusDate);
			conveyReprocess = eleInput.getAttribute(AcademyConstants.CONVEY_REPROCESS);

			// 1. getShipmentList to get ExtnTrackingStatus and other details
			log.verbose("1. Invoke getShipmentList for Convey with input :: \n" + XMLUtil.getXMLString(inDoc));
			docGetShipmentList = getShipmentListForConvey(env, inDoc);

			// 2. Check lookup table based on input status
			log.verbose("2. Check lookup table based on status : " + strStatus + " and SCAC : CONVEY");
			if (!YFCObject.isVoid(strStatus) && !YFCObject.isVoid(docGetShipmentList)) {
				strShipmentNo = XPathUtil.getString(docGetShipmentList, AcademyConstants.XPATH_SHIPMENT_SHIPMENTNO);
				strDocumentType = XPathUtil.getString(docGetShipmentList, AcademyConstants.XPATH_SHP_DOCUMENT_TYPE);
				strShipmentStatus = XPathUtil.getString(docGetShipmentList, AcademyConstants.XPATH_STATUS);
				strLineType = XPathUtil.getString(docGetShipmentList, AcademyConstants.XPATH_SHIPMENT_ORDERLINE_LINETYPE);
				strOrderNo = XPathUtil.getString(docGetShipmentList, "Shipments/Shipment/@OrderNo");

				//Retrieve the Carrier lookup status for the received tracking update
				Document docCarrierStatusLookup = AcademyCarrierStatusLookup.getCarrierStatusDetails(env, strStatus, AcademyConstants.CONVEY);				

				// if status is available in Carrier lookup, proceed
				if (!YFCObject.isVoid(docCarrierStatusLookup)) {
					log.verbose("Output from AcademyCarrierStatusLookup API:: \n" 
							+ XMLUtil.getXMLString(docCarrierStatusLookup));
					strStatusDesc = docCarrierStatusLookup.getDocumentElement()
							.getAttribute(AcademyConstants.ATTR_STATUS_DESCRIPTION);
					strMasterStatus = docCarrierStatusLookup.getDocumentElement()
							.getAttribute(AcademyConstants.ATTR_MASTER_STATUS);
					
				} else {
					//Error Scenarios where the tracking no is not matching any shipments in OMS
					log.info("Convey Carrier Status Updates Error. No status in CarrierLookup " + strStatus);
					createTrackingUpdatesRecord(env, strTrackingNo,  
							strStatus, strStatusDate, AcademyConstants.STR_V, AcademyConstants.STR_NO);
					return inDoc;
				}

				// 3. Check if the shipment is Invoiced
				boolean bIsInvoiced = checkIfInvoiced(strDocumentType, strShipmentStatus, strLineType);

				if(!bIsInvoiced) {
					log.info("Processing the tracking updates for non-Invoiced Shipments ");
					processNonInvoicedShipments(env, strStatusDate);
				} else {
					log.info("Processing the tracking updates for Invoiced Shipments ");
					processInvoicedShipments(env, docGetShipmentList, 
							strStatus, strStatusDate, strStatusDesc);
				}
			} else {
				//Error Scenarios where the tracking no is not matching any shipments in OMS
				log.info("Convey Carrier Status Updates Error. No Valid Shipment Key for Tracking no. " + strTrackingNo);
				createTrackingUpdatesRecord(env, strTrackingNo,  
						strStatus, strStatusDate, AcademyConstants.STR_E, AcademyConstants.STR_NO);
			}
		} catch (Exception exce) {
			exce.printStackTrace();
			log.verbose("Exception occurred in processConveyUpdates with input:\n" + XMLUtil.getXMLString(inDoc));
			log.info("Convey Carrier Status Updates Error. Error in processConveyUpdates :: " + exce.toString());

			//Updating the record in the re-process table to be re-tried again
			createTrackingUpdatesRecord(env, strTrackingNo,  
					strStatus, strStatusDate, AcademyConstants.STR_NO, AcademyConstants.STR_NO);
		}

		log.endTimer("AcademyProcessConveyUpdates::processConveyUpdates");
		return inDoc;
	}


	private void processNonInvoicedShipments(YFSEnvironment env, String strStatusDate) {
		log.beginTimer("AcademyProcessConveyUpdates::processNonInvoicedShipments");
		try {
			if (AcademyConstants.STR_YES.equalsIgnoreCase(conveyReprocess)) {
				log.verbose("Consecutive reprocess update from "
						+ "ProcessConveyUpdates agent for non-invoiced shipment. No updates made here." );
			} else {
				docGetConveyTrackingUpdateListOutput = getTrackingUpdatesRecord(env, strTrackingNo, null);
				// Direct update from Convey - 1st or consecutive update from convey directly
				// Check for any previous N records  
				NodeList errorRecords = XPathUtil.getNodeList(docGetConveyTrackingUpdateListOutput,
						AcademyConstants.ERROR_TRACKING_UPDATE_LIST);
				if (errorRecords.getLength() > 0) {
					log.verbose("Existing records :: \n" 
							+ XMLUtil.getXMLString(docGetConveyTrackingUpdateListOutput));
					// ignore those previous N records
					log.info("Updating previous N records to I ");
					changeTrackingUpdatesRecord(env, errorRecords, AcademyConstants.STR_IGNORED, AcademyConstants.STR_NO);
				}
				// and create a new one with N (so that agent will consider latest one while reprocessing)
				log.info("creating a new N record as it is not invoiced ");
				createTrackingUpdatesRecord(env, strTrackingNo, 
						strStatus, strStatusDate, AcademyConstants.STR_NO, AcademyConstants.STR_NO);
			}
		} catch (Exception i) {
			i.printStackTrace();
			log.verbose("Exception occurred in processNonInvoicedShipments \n" );
		}
		log.endTimer("AcademyProcessConveyUpdates::processNonInvoicedShipments");
	}


	/**
	 * This method validates if hte message should be process or ignored
	 * 
	 * @param env
	 * @param docGetShipmentList
	 * @param strShipmentNo
	 * @param strMasterStatus
	 * @param strStatus
	 * @param strStatusDate
	 * @param strStatusDesc
	 * @return 
	 */

	private void processInvoicedShipments(YFSEnvironment env, Document docGetShipmentList, 
			String strStatus, String strStatusDate, String strStatusDesc) {
		log.beginTimer("AcademyProcessConveyUpdates::processInvoicedShipments");
		boolean returnFlag = false;
		try {

			if (!YFCObject.isVoid(strStatusDesc)) {
				//Shipment and Container Status needs to be updated
				if(!YFCObject.isVoid(strMasterStatus)) {
					docGetConveyTrackingUpdateListOutput = getTrackingUpdatesRecord(env, strTrackingNo, null);
					NodeList receivedRecords = XPathUtil.getNodeList(docGetConveyTrackingUpdateListOutput,
							AcademyConstants.RECEIVED_TRACKING_UPDATE_LIST);
					if (receivedRecords.getLength() > 0) {
						log.verbose("Existing records :: \n" 
								+ XMLUtil.getXMLString(docGetConveyTrackingUpdateListOutput));
						returnFlag = checkForLatestStatus(env, receivedRecords, strStatus, strStatusDate);
					}
					
					if (!YFCObject.isVoid(docGetConveyTrackingUpdateListOutput) && !returnFlag) {
						processBasedonLatestStatusDate(env, docGetShipmentList, 
								docGetConveyTrackingUpdateListOutput,  
								strStatusDate, strStatus, strStatusDesc);
					}					
				}
				//Update the Carrier Update in ACAD_CONVEY_TRACKING_UPDATES table to be Ignored
				else {
					log.info("Convey Carrier Status Updates Error. MasterStatus is void for the status: " + strStatus);
					createTrackingUpdatesRecord(env, strTrackingNo,  
							strStatus, strStatusDate, AcademyConstants.STR_IGNORED, AcademyConstants.STR_NO);
				}
			} else {
				log.info("Convey Carrier Status Updates Error. New Tracking status received from Convey. " + strStatus);
				//Update the Carrier Update in ACAD_CONVEY_TRACKING_UPDATES table denoting "Void"
				createTrackingUpdatesRecord(env, strTrackingNo,  
						strStatus, strStatusDate, AcademyConstants.STR_V, AcademyConstants.STR_NO);
			}
		
		} catch (Exception g) {
			g.printStackTrace();
			log.info(" ErrorTrace :: " + g.toString());
		}
		log.endTimer("AcademyProcessConveyUpdates::processInvoicedShipments");
	}

	private boolean checkForLatestStatus(YFSEnvironment env, NodeList receivedRecords, 
			String strStatus, String strStatusDate) {
		log.beginTimer("AcademyProcessConveyUpdates::checkForLatestStatus");
		boolean returnFlag = false;
		Element eleTrackUpdate = (Element) receivedRecords.item(0);
		String status = eleTrackUpdate.getAttribute(AcademyConstants.ATTR_STATUS);
		if (AcademyConstants.STATUS_CARRIER_OUT_FOR_DELIVERY.equalsIgnoreCase(status) 
				|| AcademyConstants.STATUS_CARRIER_DELIVERED.equalsIgnoreCase(status)) {
			int previousStatus = Integer.parseInt(status.substring(9));
			int currentStatus = Integer.parseInt(strMasterStatus.substring(9));
			if (previousStatus > currentStatus) {
				log.verbose("Creating an E record as Previous Status is greater than the current status");
				createTrackingUpdatesRecord(env, strTrackingNo,  
						strStatus, strStatusDate, AcademyConstants.STR_E, AcademyConstants.STR_NO);
				returnFlag = true;
			}							
		}
		log.endTimer("AcademyProcessConveyUpdates::checkForLatestStatus");
		return returnFlag;		
	}


	/**
	 * This method is process all the carrier updates for all the Invoiced Shipments
	 * 
	 * @param env
	 * @param docGetShipmentList
	 * @param docGetConveyTrackingUpdateListOutput
	 * @param strMasterStatus
	 * @param strStatusDate
	 * @param strStatusDesc
	 * @param strShipmentNo
	 * @return 
	 */

	private void processBasedonLatestStatusDate(YFSEnvironment env, Document docGetShipmentList, 
			Document docGetConveyTrackingUpdateListOutput, String strStatusDate, 
			String strStatus, String strStatusDesc) {
		log.beginTimer("AcademyProcessConveyUpdates::processBasedonLatestStatusDate");
		Date dtLastStatusDate = null;
		Date dtStatusDate = null;
		String timeStamp = null;

		try {
			//Check if the latest Carrier update as per the Staus Date
			String strLastStatusDate = XPathUtil.getString(docGetConveyTrackingUpdateListOutput,
					AcademyConstants.TRACKING_UPDATE_STATUS_DATE);
			log.verbose("Latest StatusDate : " + strLastStatusDate);
			
			if (!YFCObject.isVoid(strLastStatusDate)) {	
				timeStamp = stampTimezone(strLastStatusDate);
				log.verbose("timeStamp for LatestDate : " + timeStamp);
				dtLastStatusDate = new SimpleDateFormat(timeStamp).parse(strLastStatusDate);
				log.verbose("dtLastStatusDate : " + dtLastStatusDate);
				
				timeStamp = stampTimezone(strStatusDate);
				log.verbose("timeStamp for Incoming StatusDate : " + timeStamp);
				dtStatusDate = new SimpleDateFormat(timeStamp).parse(strStatusDate);
				log.verbose("dtStatusDate : " + dtStatusDate);
			}

			if (YFCObject.isVoid(strLastStatusDate) || dtLastStatusDate.before(dtStatusDate) 
					|| AcademyConstants.STR_YES.equalsIgnoreCase(conveyReprocess)) {
				// 5. Process carrier updates for different order types
				processCarrierUpdates(env, docGetShipmentList, strStatusDate, strStatus, strStatusDesc);
			} else {
				log.info("Convey Carrier Status Updates Error. Older Status update received from Convey. ");
				//Update the Carrier Update in ACAD_CONVEY_TRACKING_UPDATES table denoting "Void"
				createTrackingUpdatesRecord(env, strTrackingNo,  
						strStatus, strStatusDate, AcademyConstants.STR_IGNORED, AcademyConstants.STR_NO);
			}
		} catch (Exception g) {
			g.printStackTrace();
			log.info(" ErrorTrace :: " + g.toString());
		}
		log.endTimer("AcademyProcessConveyUpdates::processBasedonLatestStatusDate");
	}



	/**
	 * This method is used to concatenate all the event dates to understand the event time
	 * 
	 * @param eleInput
	 * @return String
	 */

	private String getEventStatusDate(Element eleInput) {
		log.beginTimer("AcademyProcessConveyUpdates::getEventStatusDate");
		Element eleEventDetails = XmlUtils.getChildElement(eleInput, AcademyConstants.ELE_EVENT_DETAILS);
		String eventDate = eleEventDetails.getAttribute(AcademyConstants.CONVEY_EVENT_DATE);
		String eventTime = eleEventDetails.getAttribute(AcademyConstants.CONVEY_EVENT_TIME);
		String eventUtc = eleEventDetails.getAttribute(AcademyConstants.CONVEY_EVENT_UTC);
		

		//2023-02-21T05:45:40-06:00	yyyy-MM-dd'T'HH:mm:ssZ //STR_DATE_FORMAT
		StringBuilder statusDate = new StringBuilder();
		statusDate.append(eventDate);
		if(!YFCObject.isVoid(eventTime)) {
			statusDate.append(AcademyConstants.STR_TIME_CHAR);
			statusDate.append(eventTime);
			if (!eventUtc.equalsIgnoreCase(AcademyConstants.STR_UTC_Z)) {
				statusDate.append(eventUtc);
			}
		}
		log.verbose("statusDate updated : " + statusDate.toString());
		log.endTimer("AcademyProcessConveyUpdates::getEventStatusDate");
		return statusDate.toString();
	}


	/**
	 * This method update the time zone on the event time provided by Convey
	 * 
	 * @param statusDateStr
	 * @return String
	 */
	private String stampTimezone(String statusDateStr) {
		String timeStamp = null;
		if (statusDateStr.length() > 19 && 
				(!statusDateStr.contains(AcademyConstants.STR_UTC_Z) 
						|| !statusDateStr.contains(AcademyConstants.STR_UTC_Z_SMALL))) {
			timeStamp = AcademyConstants.STR_DATE_FORMAT;
		} else if (statusDateStr.length() <= 19) {
			timeStamp = AcademyConstants.STR_DATE_TIME_PATTERN;
		}
		return timeStamp;
	}



	/**
	 * This method invokes the getShipmentList API to fetch all the shipment details 
	 * present for the specific tracking no and the corresponding containers in the 
	 * same Shipment
	 * 
	 * @param env
	 * @param eleInput
	 * @return Document
	 */
	private Document getShipmentListForConvey(YFSEnvironment env, Document inDoc) {
		log.beginTimer("AcademyProcessConveyUpdates::getShipmentListForTrackingStatus");
		Document docGetShipmentListInp = null;
		Document docGetShipmentListOut = null;		
		try {

			Element inEle = inDoc.getDocumentElement();
			String shipmentNo = inEle.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
			docGetShipmentListInp = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);

			//Adding ShipmentNo as part of the request
			if (!YFCObject.isNull(shipmentNo) && !YFCObject.isVoid(shipmentNo)) {
				docGetShipmentListInp.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, shipmentNo);
			} 

			//Adding TrackingNo as part of the request
			if (!YFCObject.isNull(strTrackingNo) && !YFCObject.isVoid(strTrackingNo)) {
				
				Element eleContainers = docGetShipmentListInp.createElement(AcademyConstants.ELE_CONTAINERS);
				Element eleContainer = docGetShipmentListInp.createElement(AcademyConstants.ELE_CONTAINER);
				eleContainer.setAttribute(AcademyConstants.ATTR_TRACKING_NO, strTrackingNo);

				docGetShipmentListInp.getDocumentElement().appendChild(eleContainers);
				eleContainers.appendChild(eleContainer);
			}

			//If data is present in request, then invoke getShipmentList
			if (!YFCObject.isNull(docGetShipmentListInp)) {

				log.verbose("Input to AcademyGetShipmentListForConvey::" + XMLUtil.getXMLString(docGetShipmentListInp));				
				docGetShipmentListOut = AcademyUtil.invokeService(env, AcademyConstants.SERV_ACADEMY_GET_SHIPMENT_LIST_FOR_CONVEY, docGetShipmentListInp);
				log.verbose("AcademyGetShipmentListForConvey output :: \n" + XMLUtil.getXMLString(docGetShipmentListOut));
			}

		} catch (Exception exce) {
			exce.printStackTrace();
			log.verbose("Exception occurred in getShipmentListForConvey with input:\n" + XMLUtil.getXMLString(inDoc));
			log.info("Convey Carrier Status Updates Error. Error in getShipmentListForTrackingStatus :: " + exce.toString());
		}
		log.endTimer("AcademyProcessConveyUpdates::getShipmentListForTrackingStatus");
		return docGetShipmentListOut;
	}


	/**
	 * This method is invoked to check if the order has any invoices created. This validation
	 * is done based on the Shipment Status
	 * 
	 * @param strDocumentType
	 * @param strShipmentStatus
	 * @return Boolean
	 */
	private boolean checkIfInvoiced(String strDocumentType, String strShipmentStatus, String strLineType) {
		log.beginTimer("AcademyProcessConveyUpdates::checkIfInvoiced");
		try {
			log.verbose("3. Check if the order is Invoiced");			
			if ((AcademyConstants.DOCUMENT_TYPE_PO.equalsIgnoreCase(strDocumentType)
					&& (AcademyConstants.INVOICED_SHIPMENT_STATUS.equalsIgnoreCase(strShipmentStatus) 
							|| AcademyConstants.STR_SPECIAL_ORDER_FIREARMS.equals(strLineType)))
			|| (AcademyConstants.SALES_DOCUMENT_TYPE.equalsIgnoreCase(strDocumentType)
					&& AcademyConstants.VAL_SHIPMENT_INVOICED_STATUS.equalsIgnoreCase(strShipmentStatus))
			|| AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE.equals(strDocumentType)
			//After 1st carrier update, we'll need to consider them as invoiced only to process consecutive updates.
			|| AcademyConstants.STATUS_CARRIER_IN_TRANSIT.equalsIgnoreCase(strShipmentStatus)
			|| AcademyConstants.STATUS_CARRIER_OUT_FOR_DELIVERY.equalsIgnoreCase(strShipmentStatus)
			|| AcademyConstants.STATUS_CARRIER_PARTIALLY_DELIVERED.equalsIgnoreCase(strShipmentStatus)
			|| AcademyConstants.STATUS_CARRIER_DELIVERED.equalsIgnoreCase(strShipmentStatus))  {
				log.verbose(" Validation successful. Return true");
				return true;
			}

		} catch (Exception exce) {
			exce.printStackTrace();
			log.info("Convey Carrier Status Updates Error. Error in checkIfInvoiced :: " + exce.toString());
		}
		log.endTimer("AcademyProcessConveyUpdates::checkIfInvoiced");
		return false;
	}


	/**
	 * This method is invoked to retrieve all Tracking updates for CONVEY Shipments
	 * 
	 * Sort by 
	 * <OrderBy><Attribute Name="StatusDate" Desc="Y" /></OrderBy>
	 * 
	 * @param env
	 * @param strTrackingNo
	 * @param strMasterStatus
	 * @return Document
	 */
	private Document getTrackingUpdatesRecord(YFSEnvironment env, String strTrackingNo, String strMasterStatus) {
		log.beginTimer("AcademyProcessConveyUpdates::getTrackingUpdatesRecord");
		Document docGetTrackingUpdateList = null;
		try {
			docGetTrackingUpdateList = XMLUtil.createDocument(AcademyConstants.ELE_ACAD_CONVEY_TRACKING_UPDATES);
			Element eleAcadTrackingUpdatesIp = docGetTrackingUpdateList.getDocumentElement();
			eleAcadTrackingUpdatesIp.setAttribute(AcademyConstants.ATTR_TRACKING_NO, strTrackingNo);
			if (!YFCObject.isVoid(strMasterStatus)) {
				eleAcadTrackingUpdatesIp.setAttribute(AcademyConstants.ATTR_STATUS, strMasterStatus);
			}

			Element eleOrderBy = docGetTrackingUpdateList.createElement(AcademyConstants.ELE_ORDERBY);
			Element eleAttribute = docGetTrackingUpdateList.createElement(AcademyConstants.ELE_ATTRIBUTE);
			eleAttribute.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_STATUS_DATE);
			eleAttribute.setAttribute(AcademyConstants.ATTR_ORD_BY_DESC, AcademyConstants.STR_YES);

			docGetTrackingUpdateList.getDocumentElement().appendChild(eleOrderBy);
			eleOrderBy.appendChild(eleAttribute);


			log.verbose("Input to AcadGetTrackingUpdatesList: \n"
					+ XMLUtil.getXMLString(docGetTrackingUpdateList));
			Document docGetTrackingUpdateListOutput = AcademyUtil.invokeService(env, 
					AcademyConstants.GET_CONVEY_TRACKING_UPDATES_LIST, docGetTrackingUpdateList);
			log.verbose("Output from AcadGetTrackingUpdatesList: \n"
					+ XMLUtil.getXMLString(docGetTrackingUpdateListOutput));

			if (!YFCObject.isVoid(docGetTrackingUpdateListOutput) && docGetTrackingUpdateListOutput.getChildNodes().getLength()> 0) {
				return docGetTrackingUpdateListOutput;
			}

		} catch(Exception exce) {
			log.info("Convey Carrier Status Updates Error. Error in getTrackingUpdatesRecord :: " + exce.toString());
		}

		log.endTimer("AcademyProcessConveyUpdates::getTrackingUpdatesRecord");
		return null;
	}


	/**
	 * This method is invoked to invoke changeTrackingUpdates API to update the existing tracking update
	 * 
	 * @param env
	 * @param nlRecords
	 * @param strMasterStatus
	 * @return 
	 */
	private void changeTrackingUpdatesRecord(YFSEnvironment env, NodeList nlRecords, 
			String strIsProcessed, String strIsCustomerToBeNotified) {
		log.beginTimer("AcademyProcessConveyUpdates::changeTrackingUpdatesRecord");
		try {
			String carrierUpdateTrackerKey = null;
			String eligibleStatusForCustomerNotif = props.getProperty(
					AcademyConstants.ELIGIBLE_STATUS_FOR_CUSTOMER_NOTIFICATION);
			
			for (int i = 0; i < nlRecords.getLength(); i++) {
				Element eleTrackUpdate = (Element) nlRecords.item(i);
				carrierUpdateTrackerKey = eleTrackUpdate.getAttribute(AcademyConstants.ATTR_CONVEY_TRACKER_KEY);

				Document docAcadTrackingUpdates = XMLUtil
						.createDocument(AcademyConstants.ELE_ACAD_CONVEY_TRACKING_UPDATES);
				Element eleAcadTrackingUpdates = docAcadTrackingUpdates.getDocumentElement();
				eleAcadTrackingUpdates.setAttribute(AcademyConstants.ATTR_CONVEY_TRACKER_KEY, carrierUpdateTrackerKey);
				eleAcadTrackingUpdates.setAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED, strIsProcessed);
				
				if(AcademyConstants.STR_YES.equals(strIsCustomerToBeNotified)
						&& (!YFCObject.isVoid(eligibleStatusForCustomerNotif) 
								&& eligibleStatusForCustomerNotif.contains(strStatus))) {
					eleAcadTrackingUpdates.setAttribute(AcademyConstants.IS_CUSTOMER_TO_BE_NOTIFIED, strIsCustomerToBeNotified);
				}

				log.verbose("Input to AcademyChangeTrackingUpdates: \n"
						+ XMLUtil.getXMLString(docAcadTrackingUpdates));

				AcademyUtil.invokeService(env, AcademyConstants.CHANGE_CONVEY_TRACKING_UPDATES,
						docAcadTrackingUpdates);				
			}			
		} catch(Exception exec) {
			log.info("Convey Carrier Status Updates Error. Error in changeTrackingUpdatesRecord :: ");
		}
		log.endTimer("AcademyProcessConveyUpdates::changeTrackingUpdatesRecord");

	}


	/**
	 * This method invokes createTrackingUpdates API
	 * if Status = Y --> The Tracking update was saved in Shipment & Container
	 * if Status = N --> The Tracking update error out and need to be re-processed
	 * if Status = I --> The Tracking update was 'Ignored'
	 * if Status = V --> The Tracking update was 'Void' 
	 * if Status = E --> The Tracking update was 'Error' or no Data in OMS
	 * 
	 * 
	 * @param env
	 * @param nlRecords
	 * @param strMasterStatus
	 * @return 
	 */
	private void createTrackingUpdatesRecord(YFSEnvironment env, String strTrackingNo, 
			String strStatus, String strStatusDate, 
			String strIsProcessed, String strIsCustomerToBeNotified) {
		log.beginTimer("AcademyProcessConveyUpdates::createTrackingUpdatesRecord");

		try {
			String eligibleStatusForCustomerNotif = props.getProperty(
					AcademyConstants.ELIGIBLE_STATUS_FOR_CUSTOMER_NOTIFICATION);
			
			Document docAcadTrackingUpdatesIp = XMLUtil
					.createDocument(AcademyConstants.ELE_ACAD_CONVEY_TRACKING_UPDATES);
			Element eleAcadTrackingUpdatesIp = docAcadTrackingUpdatesIp.getDocumentElement();
			eleAcadTrackingUpdatesIp.setAttribute(AcademyConstants.ATTR_TRACKING_NO, strTrackingNo);

			eleAcadTrackingUpdatesIp.setAttribute(AcademyConstants.ATTR_STATUS, strMasterStatus);
			eleAcadTrackingUpdatesIp.setAttribute(AcademyConstants.ATTR_CONVEY_STATUS, strStatus);
			eleAcadTrackingUpdatesIp.setAttribute(AcademyConstants.ATTR_STATUS_DATE, strStatusDate);
			eleAcadTrackingUpdatesIp.setAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED, strIsProcessed);
			eleAcadTrackingUpdatesIp.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
			eleAcadTrackingUpdatesIp.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);

			if(AcademyConstants.STR_YES.equals(strIsCustomerToBeNotified)
					&& (!YFCObject.isVoid(eligibleStatusForCustomerNotif) 
							&& eligibleStatusForCustomerNotif.contains(strStatus))) {
				log.verbose("Stamping IsCustomerToBeNotified as Y for the status: " + strStatus);
				eleAcadTrackingUpdatesIp.setAttribute(AcademyConstants.IS_CUSTOMER_TO_BE_NOTIFIED, strIsCustomerToBeNotified);
			}
			
			log.verbose("Input to AcademyCreateTrackingUpdates: \n"
					+ XMLUtil.getXMLString(docAcadTrackingUpdatesIp));

			AcademyUtil.invokeService(env, AcademyConstants.CREATE_CONVEY_TRACKING_UPDATES,
					docAcadTrackingUpdatesIp);
		} catch(Exception exce) {
			log.info("Convey Carrier Status Updates Error. Error in createTrackingUpdatesRecord :: " + exce.toString());
		}
		log.endTimer("AcademyProcessConveyUpdates::createTrackingUpdatesRecord");

	}

	/**
	 * This method validates and process the carrier update for the specific shipment/Container
	 * 
	 * @param env
	 * @param docShipmentList
	 * @param strMasterStatus
	 * @param strStatusDate
	 * @return 
	 */
	private void processCarrierUpdates(YFSEnvironment env, Document docGetShipmentList, 
			String strStatusDate, String strStatus, String strStatusDesc) {
		log.beginTimer("AcademyProcessConveyUpdates::processCarrierUpdates");

		try {			
			String strLineType = XPathUtil.getString(docGetShipmentList, AcademyConstants.XPATH_SHIPMENT_ORDERLINE_LINETYPE);
			String strProcureFromNode = XPathUtil.getString(docGetShipmentList, AcademyConstants.XPATH_SHIPMENT_PROCURE_FROM_NODE);
			String strDocumentType = XPathUtil.getString(docGetShipmentList, AcademyConstants.XPATH_SHP_DOCUMENT_TYPE);
			String strDeliveryMethod = XPathUtil.getString(docGetShipmentList, AcademyConstants.XPATH_SHP_DELIVERY_METHOD);
			
			//Shipment should be Shipping Order i.e Sales order shipment
			if(AcademyConstants.SALES_DOCUMENT_TYPE.equals(strDocumentType) 
					&& AcademyConstants.STR_SHIP_DELIVERY_METHOD.equals(strDeliveryMethod)) {
				log.verbose("Updating the Carrier updates for Shipping Order. i.e SFS and STH");
				checkAndUpdateContainerTrackingAndShipmentStatus(env, docGetShipmentList,  
						strStatusDate, strStatus, strStatusDesc, true, true);
			}
			else if (AcademyConstants.DOCUMENT_TYPE_PO.equals(strDocumentType)) {
				log.verbose("Updating the Carrier updates for DSV Order.");

				if(AcademyConstants.STR_SPECIAL_ORDER_FIREARMS.equals(strLineType)) {
					log.verbose("Updating the Carrier updates for DSV Order. DSV-SOF");
					checkAndUpdateContainerTrackingAndShipmentStatus(env, docGetShipmentList,  
							strStatusDate, strStatus, strStatusDesc, false, false);
				}
				else {
					log.verbose("Updating the Carrier updates for Regular DSV Order which is non-SOF.");
					checkAndUpdateContainerTrackingAndShipmentStatus(env, docGetShipmentList,  
							strStatusDate, strStatus, strStatusDesc, true, true);
				}
			}
			else if(AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE.equals(strDocumentType)) {
				log.verbose("Updating the Carrier updates for STS Order.");
				if(YFCObject.isVoid(strProcureFromNode)) {
					log.verbose("Updating the Carrier updates for STS2.0 Order. STS from Stores");
					checkAndUpdateContainerTrackingAndShipmentStatus(env, docGetShipmentList,  
							strStatusDate, strStatus, strStatusDesc, false, false);
				}
				else {
					log.info("Convey Carrier Status Updates Error.Invalid carrier update for Shipment :: " 
							+ strShipmentNo + " : strMasterStatus : " + strMasterStatus );
				}

			}
			else {
				log.info("Convey Carrier Status Updates Error.Invalid carrier update for Shipment :: " 
						+ strShipmentNo + " : strMasterStatus : " + strMasterStatus );

			}

		} catch (Exception exce) {
			exce.printStackTrace();
			log.verbose("Exception occurred in processCarrierUpdates");
			log.info("Convey Carrier Status Updates Error. Error in processCarrierUpdates :: " + exce.toString());
		}

		log.endTimer("AcademyProcessConveyUpdates::processCarrierUpdates");

	}

	/**
	 * This method validates and process the carrier update for the specific shipment/Container
	 * 
	 * @param env
	 * @param docShipmentList
	 * @param strMasterStatus
	 * @param strStatusDate
	 * @return 
	 * @throws Exception 
	 */	
	private void checkAndUpdateContainerTrackingAndShipmentStatus(YFSEnvironment env, Document docGetShipmentList, 
			String strStatusDate, String strStatus, String strStatusDesc, 
			boolean bUpdateShipmentStatus, boolean bUpdateContainerStatus) throws Exception {
		log.beginTimer("AcademyProcessConveyUpdates::checkAndUpdateContainerTrackingAndShipmentStatus");

		Set<String> setTrackStatus = new HashSet<>();
		int iContainersLength = 0;
		
		try {
			String strShipmentKey = XPathUtil.getString(docGetShipmentList, AcademyConstants.XPATH_SHIPMENT_SHP_KEY);
			String strDocumentType = XPathUtil.getString(docGetShipmentList, AcademyConstants.XPATH_SHP_DOCUMENT_TYPE);

			Document docChangeShipmentInp = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			Element eleShipment = docChangeShipmentInp.getDocumentElement();
			eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
			eleShipment.setAttribute(AcademyConstants.ATTR_OVERRIDE_MODIFICATION_RULES, AcademyConstants.STR_YES);

			Element eleContainers = docChangeShipmentInp.createElement(AcademyConstants.ELE_CONTAINERS);

			NodeList nlContainers = XPathUtil.getNodeList(docGetShipmentList.getDocumentElement(), AcademyConstants.XPATH_CONTAINER_PATH);
			iContainersLength = nlContainers.getLength();
			
			if (iContainersLength > 0) {
				log.verbose("Number of containers present in the current Shipment :: " + nlContainers.getLength());					
				for (int i = 0; i < nlContainers.getLength(); i++) {
					Element eleCurrentContainer = (Element) nlContainers.item(i);
					String strShipmentContainerKey = eleCurrentContainer.getAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY);
					String strCurrentTrackingNo = eleCurrentContainer.getAttribute(AcademyConstants.ATTR_TRACKING_NO);

					Element eleCurrentExtn = XmlUtils.getChildElement(eleCurrentContainer, AcademyConstants.ELE_EXTN);
					String strExtnTrackingStatus = eleCurrentExtn.getAttribute(AcademyConstants.ATTR_EXTN_TRACKING_STATUS);
					if(YFCObject.isVoid(strExtnTrackingStatus)) {
						strExtnTrackingStatus = "IS_NULL";
					}
					
					log.verbose("strExtnTrackingStatus :: " + strExtnTrackingStatus);

					if (strTrackingNo.equalsIgnoreCase(strCurrentTrackingNo)) {
						// update Extn_tracking_status at container level
						Element eleContainer = docChangeShipmentInp.createElement(AcademyConstants.ELE_CONTAINER);
						eleContainer.setAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY, strShipmentContainerKey);
						if (AcademyConstants.STATUS_CARRIER_DELIVERED.equalsIgnoreCase(strMasterStatus)
								&& bUpdateContainerStatus) {
							eleContainer.setAttribute(AcademyConstants.ATTR_STATUS, AcademyConstants.STATUS_CONTAINER_DELIVERED);
						}						

						Element eleExtn = docChangeShipmentInp.createElement(AcademyConstants.ELE_EXTN);
						eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_TRACKING_STATUS, strStatusDesc);
						eleContainer.appendChild(eleExtn);
						eleContainers.appendChild(eleContainer);
						setTrackStatus.add(strStatusDesc);

					} else {
						setTrackStatus.add(strExtnTrackingStatus);
					}
					log.verbose("Added Statuses so far :: " + setTrackStatus);
				}
			}

			eleShipment.appendChild(eleContainers);
			log.verbose("changeShipment api input to update ExtnTrackingStatus:: \n "
					+ XMLUtil.getXMLString(docChangeShipmentInp));
			AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, docChangeShipmentInp);

			log.verbose("bUpdateShipmentStatus:::: " + bUpdateShipmentStatus);
			log.verbose("Final set to changeShipmentStatus:::: " + setTrackStatus);
			if(bUpdateShipmentStatus && iContainersLength > 0) {
				log.verbose("Tracking Statuses for all the TrackingNos in the current Shipment :: "
						+ setTrackStatus + "\n With Length: " + setTrackStatus.size());
				String strShipmentStatus = XPathUtil.getString(docGetShipmentList, AcademyConstants.XPATH_STATUS);
				updateShipmentStatus(env, setTrackStatus, strShipmentStatus, strShipmentKey, strDocumentType);
			}
			
			updateSuccessConveyTrackingRecord(env, strTrackingNo,  
					strStatus, strStatusDate);
			
			
		} catch (YFSException yfsEx) {
			log.verbose("YFSException Received. Based on the error code, initate DB update");
			String strErrorCode = yfsEx.getErrorCode();
			//Handling for the Issue where Closed shipment cannot be modified error is received.
			handleClosedShipmentError(env, yfsEx, docGetShipmentList, 
					strErrorCode, strStatus, strStatusDate, strStatusDesc);			
			log.verbose("Exception occurred in checkAndUpdateContainerTrackingAndShipmentStatus");
			log.info("Convey Carrier Status Updates Error. Error in checkAndUpdateContainerTrackingAndShipmentStatus :: " + yfsEx.toString());
		} catch (Exception exce) {
			log.verbose("Creating an E record due to Exception");
			createTrackingUpdatesRecord(env, strTrackingNo,  
					strStatus, strStatusDate, AcademyConstants.STR_E, AcademyConstants.STR_NO);
			exce.printStackTrace();
			log.verbose("Exception occurred in checkAndUpdateContainerTrackingAndShipmentStatus");
			log.info("Convey Carrier Status Updates Error. Error in checkAndUpdateContainerTrackingAndShipmentStatus :: " + exce.toString());
		}
		log.endTimer("AcademyProcessConveyUpdates::checkAndUpdateContainerTrackingAndShipmentStatus");

	}


	private void updateSuccessConveyTrackingRecord(YFSEnvironment env, String strTrackingNo, String strStatus,
			String strStatusDate) {
		log.beginTimer("AcademyProcessConveyUpdates::updateSuccessConveyTrackingRecord");
		try {
			// Check for any previous N records  
			NodeList errorRecords = XPathUtil.getNodeList(docGetConveyTrackingUpdateListOutput,
					AcademyConstants.ERROR_TRACKING_UPDATE_LIST);
			if (AcademyConstants.STR_YES.equalsIgnoreCase(conveyReprocess) && errorRecords.getLength() > 0) {
				log.verbose("Consecutive reprocess update from ProcessConveyUpdates agent for invoiced shipment." );
				log.verbose("Hence updating the existing 'N' record to 'Y' " );					
				log.verbose("Previous N records :: \n" 
						+ XMLUtil.getXMLString(docGetConveyTrackingUpdateListOutput));
				// ignore those previous N records
				log.info("Updating previous N records to Y ");
				changeTrackingUpdatesRecord(env, errorRecords, AcademyConstants.STR_YES, AcademyConstants.STR_YES);				
			} else {
				// Direct update from Convey - create a new success record
				createTrackingUpdatesRecord(env, strTrackingNo,  
						strStatus, strStatusDate, AcademyConstants.STR_YES, AcademyConstants.STR_YES);
			}
		} catch (Exception u) {
			u.printStackTrace();
			log.verbose("Exception in updateConveyTrackingRecord");			
		}		
		log.endTimer("AcademyProcessConveyUpdates::updateSuccessConveyTrackingRecord");
	}

	private void handleClosedShipmentError(YFSEnvironment env, YFSException yfsEx, 
			Document docGetShipmentList, String strErrorCode, String strStatus,
			String strStatusDate, String strStatusDesc) throws Exception {
		log.beginTimer("AcademyProcessConveyUpdates::handleClosedShipmentError");
		if(!YFCObject.isVoid(strErrorCode) && (AcademyConstants.CLOSED_SHIPMENT_ERROR.equals(strErrorCode))) {

			/*String strShipmentContainerKey = XPathUtil.getString(docGetShipmentList, 
					"/Shipments/Shipment/Containers/Container[@TrackingNo='" + strTrackingNo + "']/@ShipmentContainerKey");
			updateExtnTrackingStatusViaDBQuery(env, strShipmentContainerKey, strStatusDesc);*/
			
			//Updating the Tracking No update as processed if DB query is processed
			createTrackingUpdatesRecord(env, strTrackingNo,  
					strStatus, strStatusDate, AcademyConstants.STR_YES, AcademyConstants.STR_NO);
			yfsEx.printStackTrace();
		}
		else {
			createTrackingUpdatesRecord(env, strTrackingNo,  
					strStatus, strStatusDate, AcademyConstants.STR_E, AcademyConstants.STR_NO);
			yfsEx.printStackTrace();
		}
		log.endTimer("AcademyProcessConveyUpdates::handleClosedShipmentError");
	}


	/**
	 * This method validates expected Shipment Status and decides the final Shipment status
	 * 
	 * @param env
	 * @param docShipmentList
	 * @param strMasterStatus
	 * @param strStatusDate
	 * @return 
	 */	
	private void updateShipmentStatus(YFSEnvironment env, Set<String> setTrackStatus, 
			String strShipmentStatus, String strShipmentKey, 
			String strDocumentType) {
		log.beginTimer("AcademyProcessConveyUpdates::updateShipmentStatus");
		String strDropStatus = strMasterStatus;
		int iTrackingStatus = setTrackStatus.size();
		
		try {
			if(setTrackStatus.contains(AcademyConstants.CONVEY_DELIVERED) && iTrackingStatus > 1) {
				//If atleast one status is Delivered, then the Shipment is partially Delivered.
				strDropStatus = AcademyConstants.STATUS_CARRIER_PARTIALLY_DELIVERED;
			}
			//Edge Case where the Out for Delivery Update was received on the order
			else if (setTrackStatus.contains(AcademyConstants.CONVEY_OUT_FOR_DELIVERY) 
					&& iTrackingStatus > 1 && !setTrackStatus.contains(AcademyConstants.CONVEY_DELIVERED)
					&& !setTrackStatus.contains(AcademyConstants.CONVEY_IN_TRANSIT)) {
				//Shipment received only Out for delivery update. So updating as In Transit
				strDropStatus = AcademyConstants.STATUS_CARRIER_IN_TRANSIT;
			}
			else if(setTrackStatus.contains(AcademyConstants.CONVEY_OUT_FOR_DELIVERY) 
					&& iTrackingStatus > 1) {
				//Only if all the containers on Shipment have OOD, then status will be changed
				//Shipment has more than 1 tracking no status. So not eligible for Out for Delivery
				strDropStatus = null;
			}
			
			log.verbose("Current ShipmentStatus : " + strShipmentStatus + " And Updated Status is :: " + strDropStatus);
			if(!YFCObject.isVoid(strDropStatus) && !strShipmentStatus.equals(strDropStatus)) {
				changeShipmentStatusForConvey(env, strShipmentKey, strDropStatus, strDocumentType);
			}
			else {
				log.verbose("Shipment Status update has been skipped");
			}

		} catch (Exception exce) {
			exce.printStackTrace();
			log.info("Convey Carrier Status Updates Error. Error in updateShipmentStatus :: " + exce.toString());

		}
		log.endTimer("AcademyProcessConveyUpdates::updateShipmentStatus");

	}

	/**
	 * This method validates invokes the changeShipmentStatus
	 * 
	 * @param env
	 * @param strShipmentKey
	 * @param strDropStatus
	 * @return 
	 */	
	private void changeShipmentStatusForConvey(YFSEnvironment env, String strShipmentKey, 
			String strDropStatus, String strDocumentType) {			
		log.beginTimer("AcademyProcessConveyUpdates::changeShipmentStatusForConvey");
		try {
			Document changeShipmentStatusInDoc = null;
			changeShipmentStatusInDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			changeShipmentStatusInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
			
			//Updated the code to include the Academy_Deliver_Shipment.0001.ex for STH,SFS status
			if (AcademyConstants.SALES_DOCUMENT_TYPE.equalsIgnoreCase(strDocumentType) && 
					AcademyConstants.STATUS_SHIPMENT_DELIVERED.equals(strDropStatus)) {
				changeShipmentStatusInDoc.getDocumentElement().setAttribute(
						AcademyConstants.ATTR_TRANSID, "Academy_Deliver_Shipment.0001.ex");
			} else if (AcademyConstants.SALES_DOCUMENT_TYPE.equalsIgnoreCase(strDocumentType)) {
				changeShipmentStatusInDoc.getDocumentElement().setAttribute(
						AcademyConstants.ATTR_TRANSID, AcademyConstants.TRAN_UPDATE_SO_TRACKING_STATUS);
			} else if (AcademyConstants.DOCUMENT_TYPE_PO.equalsIgnoreCase(strDocumentType)) {
				changeShipmentStatusInDoc.getDocumentElement().setAttribute(
						AcademyConstants.ATTR_TRANSID, AcademyConstants.TRAN_UPDATE_PO_TRACKING_STATUS);
			}
						
			changeShipmentStatusInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_BASEDROP_STATUS, strDropStatus);
			log.debug("Input to changeShipmentStatus API::" + XMLUtil.getXMLString(changeShipmentStatusInDoc));
			if(!YFCObject.isNull(changeShipmentStatusInDoc)) {
				log.debug("Executing ChangeShipmentStatus API ##");

				AcademyUtil.invokeAPI(env, AcademyConstants.CHANGE_SHIPMENT_STATUS_API, 
						changeShipmentStatusInDoc);
			}
		}  catch (Exception exce) {
			exce.printStackTrace();
			log.info("Convey Carrier Status Updates Error. Error in changeShipmentStatusForConvey :: " + exce.toString());

		}
		log.endTimer("AcademyProcessConveyUpdates::changeShipmentStatusForConvey");
	}
	
	/**
	 * This method does a DB update for COntainer Info if Shipment is closed
	 * 
	 * @param env
	 * @param strShipmentKey
	 * @param strDropStatus
	 * @return 
	 * @throws SQLException 
	 */
	private void updateExtnTrackingStatusViaDBQuery(YFSEnvironment env, 
			String strShipmentContainerKey, String strStatusDesc) throws SQLException {
		log.beginTimer("AcademyProcessConveyUpdates::updateExtnTrackingStatusViaDBQuery");
		Statement stmt = null;

		log.verbose("The ShipmentContainerKey " + strShipmentContainerKey + " has Tracking Status as " + strStatusDesc);
		if(!YFCObject.isVoid(strShipmentContainerKey)) {
			String strUpdateManifestStatus = "UPDATE YFS_SHIPMENT_CONTAINER set EXTN_TRACKING_STATUS='"+ strStatusDesc+ "' where SHIPMENT_CONTAINER_KEY='" + strShipmentContainerKey + "'";
			log.verbose("Update Query:" + strUpdateManifestStatus);
			try {
				YFCContext ctxt = (YFCContext) env;
				stmt = ctxt.getConnection().createStatement();
				int hasUpdated = stmt.executeUpdate(strUpdateManifestStatus);
				if (hasUpdated > 0) {
					log.verbose("Shipment Container has been updated");
				}
			} catch (SQLException sqlEx) {
				log.verbose("Error occured while updating the Shipment Container Tracking status");
				sqlEx.printStackTrace();
				throw sqlEx;
			} finally {
				if (stmt != null)
					stmt.close();
				stmt = null;
			}

		}
		log.endTimer("AcademyProcessConveyUpdates::updateExtnTrackingStatusViaDBQuery");
	}
}