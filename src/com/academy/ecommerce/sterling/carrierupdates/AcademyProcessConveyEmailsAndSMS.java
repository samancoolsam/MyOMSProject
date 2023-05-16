package com.academy.ecommerce.sterling.carrierupdates;

import java.text.ParseException;

/**#########################################################################################
 *
 * Project Name                : Convey
 * Author                      : Fulfillment POD
 * Author Group				  : DSV
 * Date                        : 20-APR-2023 
 * Description				  : This class fetches all the records which are eligible to 
 * 								trigger a notification to the customer based on the update
 * 								from Convey
 * 								 
 * ---------------------------------------------------------------------------------
 * Date            	Author         		Version#       		Remarks/Description                      
 * ---------------------------------------------------------------------------------
 * 20-Apr-2023		Everest  	 		1.0           		Initial version
 *
 * #########################################################################################*/


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.soap.providers.com.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyProcessConveyEmailsAndSMS extends YCPBaseAgent {

	private static YFCLogCategory logger = YFCLogCategory.instance(AcademyProcessConveyEmailsAndSMS.class);

	/**
	 * This method is fetches all the list of records which are to be reporcessed by the agent
	 * 
	 * @param env
	 * @param docInput
	 * @param docLastMessage
	 **Sample message input:
	 */
	@Override
	public List<Document> getJobs(YFSEnvironment env, Document docInput, Document docLastMessage) throws Exception {
		logger.beginTimer("AcademyProcessConveyEmailsAndSMS::getJobs");
		logger.verbose("Inside getJobs.The Input xml is : " + XMLUtil.getXMLString(docInput));

		List<Document> outputList = new ArrayList<>();

		String strNumOfRecords = docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_NUM_RECORDS);
		String strNoOfDays = docInput.getDocumentElement().getAttribute(AcademyConstants.STR_NO_OF_DAYS);
		String strConsolidateEmailWaitTime = docInput.getDocumentElement().getAttribute("ConsolidatedEmailTimeInHours");
		String strServiceToBeInvoked = docInput.getDocumentElement().getAttribute("ServiceToBeInvoked");
		String strConveyStatus = docInput.getDocumentElement().getAttribute("ConveyStatus");

		if (YFCObject.isVoid(strNumOfRecords)) {
			strNumOfRecords = "500";
		}

		if (YFCObject.isVoid(strConsolidateEmailWaitTime)) {
			strConsolidateEmailWaitTime = "1";
		}

		Calendar calFromDate = Calendar.getInstance();
		Calendar calToDate = Calendar.getInstance();

		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_SIMPLE_DATE_PATTERN);// "yyyy-MM-dd"

		calFromDate.add(Calendar.DATE, (-Integer.parseInt(strNoOfDays)));
		String strFromOrderDate = sdf.format(calFromDate.getTime());// oldest date

		String strToOrderDate = sdf.format(calToDate.getTime());// earliest date

		Document docGetConveyTrackingUpdatesListInp = XMLUtil.createDocument(AcademyConstants.ELE_ACAD_CONVEY_TRACKING_UPDATES);
		Element eleGetConveyTrackingUpdatesListInp = docGetConveyTrackingUpdatesListInp.getDocumentElement();

		//Last message present in the request
		if (!YFCObject.isVoid(docLastMessage)) {
			logger.verbose("LastMessage is present for the AcadConveyTrackingUpdates" + XMLUtil.getXMLString(docLastMessage));
			String strShipmentNo = docLastMessage.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
			eleGetConveyTrackingUpdatesListInp.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
			eleGetConveyTrackingUpdatesListInp.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO + AcademyConstants.ATTR_QRY_TYPE,
					AcademyConstants.GT_QRY_TYPE);
		}

		eleGetConveyTrackingUpdatesListInp.setAttribute(AcademyConstants.ATTR_STATUS_DATE_QRY_TYPE,	AcademyConstants.BETWEEN);
		eleGetConveyTrackingUpdatesListInp.setAttribute(AcademyConstants.ATTR_FROM_STATUS_DATE, strFromOrderDate + "T00:00:00");
		eleGetConveyTrackingUpdatesListInp.setAttribute(AcademyConstants.ATTR_TO_STATUS_DATE, strToOrderDate + "T23:59:59");
		eleGetConveyTrackingUpdatesListInp.setAttribute("IsCustomerToBeNotified", AcademyConstants.STR_YES);
		eleGetConveyTrackingUpdatesListInp.setAttribute(AcademyConstants.ATTR_CONVEY_STATUS, strConveyStatus);
		
		eleGetConveyTrackingUpdatesListInp.setAttribute(AcademyConstants.ATTR_MAX_RECORD, strNumOfRecords);

		//Order by the AcadConveyTrackingUpdatesKey
		Element eleOrderBy = docGetConveyTrackingUpdatesListInp.createElement(AcademyConstants.ELE_ORDERBY);
		eleGetConveyTrackingUpdatesListInp.appendChild(eleOrderBy);

		Element eleAttribute = docGetConveyTrackingUpdatesListInp.createElement(AcademyConstants.ELE_ATTRIBUTE);
		eleOrderBy.appendChild(eleAttribute);
		eleAttribute.setAttribute(AcademyConstants.ATTR_DESC_SHORT, AcademyConstants.STR_NO);
		eleAttribute.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_SHIPMENT_NO);

		logger.debug("Input Doc to get Tracker Details:: \n" + XMLUtil.getXMLString(docGetConveyTrackingUpdatesListInp));

		Document docGetConveyTrackingUpdatesListOut = AcademyUtil.invokeService(env,
				AcademyConstants.GET_CONVEY_TRACKING_UPDATES_LIST, docGetConveyTrackingUpdatesListInp);

		//Check if the output has any valid records
		outputList = filterRecordsBasedOnShipmentNo(env, docGetConveyTrackingUpdatesListOut, strConsolidateEmailWaitTime, strServiceToBeInvoked);
		
		logger.verbose(" Final output list " + outputList.size());
		logger.endTimer("AcademyProcessConveyEmailsAndSMS::getJobs");
		return outputList;
	}


	/**
	 * This method is triggers the corresponding emails to Customer
	 * 
	 * @param env
	 * @param docInput
	 **Sample message input:
	 */
	@Override
	public void executeJob(YFSEnvironment env, Document docInput) throws Exception {
		logger.beginTimer("AcademyProcessConveyEmailsAndSMS::executeJob");
		logger.verbose("Inside executeJobs.The Input xml is : " + XMLUtil.getXMLString(docInput));
		try {
			Element eleShipment=null;
			
			Element eleInput = docInput.getDocumentElement();
			String strShipmentNo = eleInput.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
			String strServiceToBeInvoked = eleInput.getAttribute(AcademyConstants.STR_SERVICE_NAME);
			String strConveyTrackingUpdateKey = eleInput.getAttribute(AcademyConstants.ATTR_CNVY_TRCK_KEY);
			
			String strTrackingNo = eleInput.getAttribute(AcademyConstants.ATTR_TRACKING_NO);

			//Define Service to fetch all the details needed for Shipment email
			Document docGetShipmentListInp = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			docGetShipmentListInp.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
			
			Document docGetShipmentListOut = AcademyUtil.invokeService(env, "AcademyGetShipmentListForConveyEmails",
					docGetShipmentListInp);
			
			//TODO : Add Logic to check if it is Complete Or Partial. If partial add a dummy attribute at each container level/Line level
			eleShipment = (Element) docGetShipmentListOut.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);
			eleShipment.setAttribute("TrackingNoForEmail", strTrackingNo);
			
			NodeList nlContainers = docGetShipmentListOut.getElementsByTagName(AcademyConstants.ELE_CONTAINER);
			if(nlContainers.getLength() == strTrackingNo.split(AcademyConstants.STR_COMMA).length) {
				logger.verbose("Complete Shipment is in InTransit :: ");
				eleShipment.setAttribute(AcademyConstants.ATTR_MESSAGE, "COMPLETE");
			}
			else {
				eleShipment.setAttribute(AcademyConstants.ATTR_MESSAGE, "PARTIAL");
			}
			
			//Updating individual Container level info if the tracking update was received
			for(int iContainer =0; iContainer < nlContainers.getLength(); iContainer ++ ) {
				Element eleContainer = (Element) nlContainers.item(iContainer);
				String strCntrTrackingNo = eleContainer.getAttribute(AcademyConstants.ATTR_TRACKING_NO);
				if(strTrackingNo.contains(strCntrTrackingNo)) {
					logger.verbose("Updating the Container status for "+strCntrTrackingNo);
					eleContainer.setAttribute("ConveyUpadtesRecevied", AcademyConstants.STR_YES);
				}
				else {
					logger.verbose("Container not processed by COnvey ::  "+strCntrTrackingNo);
					eleContainer.setAttribute("ConveyUpadtesRecevied", AcademyConstants.STR_NO);
				}
			}
			logger.verbose("Input to Sync service AcademyProcessConveyEmailsAndSMS ::: \n"
					+ XMLUtil.getXMLString(docGetShipmentListOut));
			AcademyUtil.invokeService(env, strServiceToBeInvoked, XMLUtil.getDocumentForElement(eleShipment));

			if(strConveyTrackingUpdateKey.contains(AcademyConstants.STR_COMMA)) {
				String[] strConveyTrackingKeys = strConveyTrackingUpdateKey.split(AcademyConstants.STR_COMMA);
				for (int iTrkKey=0; iTrkKey < strConveyTrackingKeys.length; iTrkKey++) {
					eleInput.setAttribute(AcademyConstants.ATTR_CNVY_TRCK_KEY, strConveyTrackingKeys[iTrkKey]);
					updateRecordForCustomerNotified(env, eleInput);
				}
			}
			else {
				updateRecordForCustomerNotified(env, eleInput);
			}

			
		} catch (Exception e) {
			logger.error("Exception inside AcademyProcessConveyEmailsAndSMS : executeJob " + e.getMessage());
			throw new YFSException(e.getMessage());
		}
		logger.endTimer("AcademyProcessConveyEmailsAndSMS::executeJob");
	}

	/**
	 * This method is filters all the records based on ShipmentNo to see if 
	 * the Shipment overall is eligible for Emails
	 * 
	 * @param env
	 * @param docConveyTrackingUpdates
	 * @param strConsolidateEmailWaitTime
	 * @return List<Document>
	 */
	private List<Document> filterRecordsBasedOnShipmentNo(YFSEnvironment env, Document docConveyTrackingUpdates, 
			String strConsolidateEmailWaitTime, String strServiceToBeInvoked) throws Exception {
		logger.beginTimer("AcademyProcessConveyEmailsAndSMS::filterRecordsBasedOnShipmentNo");
		List<Document> outputList = new ArrayList<>();
		HashMap<String, HashMap<String, Document>> hmConsolidatedShipment = new HashMap<String,HashMap<String, Document>>(); 

		logger.verbose("Inside filterRecordsBasedOnShipmentNo.The Input xml is : " + XMLUtil.getXMLString(docConveyTrackingUpdates));

		NodeList nlAcadConveyTrackingUpdates = docConveyTrackingUpdates.getElementsByTagName(AcademyConstants.ELE_ACAD_CONVEY_TRACKING_UPDATES);
		for (int iTrackingUpdate = 0; iTrackingUpdate < nlAcadConveyTrackingUpdates.getLength(); iTrackingUpdate++) {
			Element eleConveyTrackingUpdate = (Element) nlAcadConveyTrackingUpdates.item(iTrackingUpdate);
			String strStatusDateOld = null;
			Document docOldConveyTrackingUpdate = null;
			logger.verbose(":: Element being Iterated is :: " + SCXmlUtil.getString(eleConveyTrackingUpdate));

			String strShipmentNo = eleConveyTrackingUpdate.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
			String strTrackingNo = eleConveyTrackingUpdate.getAttribute(AcademyConstants.ATTR_TRACKING_NO);
			String strStatusDateNew = eleConveyTrackingUpdate.getAttribute(AcademyConstants.ATTR_STATUS_DATE);
			
			//Check if ShipmentNo is present in the request
			if(!YFCObject.isVoid(strShipmentNo)) {
				HashMap<String,Document> hmTrackingInfo = new HashMap<String,Document>();

				if(hmConsolidatedShipment.containsKey(strShipmentNo)) {
					hmTrackingInfo = hmConsolidatedShipment.get(strShipmentNo);
				}
				//This is to handle a case where multiple updates are present for same tracking no
				if(hmTrackingInfo.containsKey(strTrackingNo)) {
					docOldConveyTrackingUpdate = hmTrackingInfo.get(strTrackingNo);
					strStatusDateOld = docOldConveyTrackingUpdate.getDocumentElement()
							.getAttribute(AcademyConstants.ATTR_STATUS_DATE);
				}

				//Handling cases where duplicate records of In Transit Update has been present for same tracking no 
				if(!YFCObject.isVoid(strStatusDateOld) && !YFCObject.isVoid(strStatusDateNew)) {
					//Compare which is the older record and consider that for the notification. 
					if(checkifNewUpdateisLatest(strStatusDateNew, strStatusDateOld)) {
						//Update the New record In Transit update in the map.
						hmTrackingInfo.put(strTrackingNo, XMLUtil.getDocumentForElement(eleConveyTrackingUpdate));
						//Also add logic to update the newer record to be ignored.
						updateRecordForCustomerNotified(env, docOldConveyTrackingUpdate.getDocumentElement());
					}
					else {
						//Update newer records to be ignored
						updateRecordForCustomerNotified(env, eleConveyTrackingUpdate);
					}
				}
				else {
					hmTrackingInfo.put(strTrackingNo, XMLUtil.getDocumentForElement(eleConveyTrackingUpdate));
				}
				//Add Different Shipment and corresponding TrackingNo in the Map
				hmConsolidatedShipment.put(strShipmentNo, hmTrackingInfo);
				logger.verbose(":: Updated hmConsolidatedShipment :: " + hmConsolidatedShipment.toString());
			}
			else {
				logger.verbose("ShipmentNo is not present for the request. " + SCXmlUtil.getString(docConveyTrackingUpdates));
				//Update the transaction to be skipped for emails
				updateRecordForCustomerNotified(env, eleConveyTrackingUpdate);
			}
		}

		outputList = sortShipmentsAndConsolidateContainers(env, hmConsolidatedShipment, strConsolidateEmailWaitTime, strServiceToBeInvoked);

		logger.endTimer("AcademyProcessConveyEmailsAndSMS::filterRecordsBasedOnShipmentNo");
		return outputList;
	}



	/**
	 * This method sorts by the map and creates a single record for each Shipment which 
	 * will contain multiple containers
	 * 
	 * @param env
	 * @param docInput
	 * @param docLastMessage
	 **Sample message input:
	 */
	private List<Document> sortShipmentsAndConsolidateContainers(YFSEnvironment env, HashMap<String, HashMap<String, Document>> hmConsolidatedShipment, 
			String strConsolidateEmailWaitTime, String strServiceToBeInvoked) throws Exception {
		logger.beginTimer("AcademyProcessConveyEmailsAndSMS::sortShipmentsAndConsolidateContainers");
		List<Document> outputList = new ArrayList<>();
		logger.verbose("Inside sortShipmentsAndConsolidateContainers.The Input map is : " + hmConsolidatedShipment.toString());
		
		if(hmConsolidatedShipment.size() > 0) {
			Iterator<Entry<String, HashMap<String, Document>>> iterShipmentNo = hmConsolidatedShipment.entrySet().iterator();
			
			//Iterating through each shipment
			while (iterShipmentNo.hasNext()) {	
				Entry<String, HashMap<String, Document>> entryShipment = iterShipmentNo.next();
				String strShipmentNo = entryShipment.getKey();
				Document docConveyTrackingUpdatesFinal = null;
				
				logger.verbose(":: Shipment being Iterated is :: " + strShipmentNo);
				
				HashMap<String, Document> hmTrackingInfo = entryShipment.getValue();
				Iterator<Entry<String, Document>> iterTrackingNo = hmTrackingInfo.entrySet().iterator();

				//Iterating through each tracking number update in Shipment
				while (iterTrackingNo.hasNext()) {	
					Entry<String, Document> entryTrackingNo = iterTrackingNo.next();
					String strTrackingNo = entryTrackingNo.getKey();

					logger.verbose(":: TrackingNo being Iterated is :: " + strTrackingNo);

					Document docConveyTrackingUpdates = entryTrackingNo.getValue();
					if(YFCObject.isVoid(docConveyTrackingUpdatesFinal)) {
						logger.verbose("First Tracking Number present for Shipment");
						docConveyTrackingUpdatesFinal = docConveyTrackingUpdates;
					}
					else {
						
						logger.verbose("Multiple Tracking No. Compare the Status Dates ");
						String strStatusDateNew = docConveyTrackingUpdates.getDocumentElement().getAttribute(AcademyConstants.ATTR_STATUS_DATE);
						String strStatusDateOld = docConveyTrackingUpdatesFinal.getDocumentElement().getAttribute(AcademyConstants.ATTR_STATUS_DATE);

						//Check if the other tracking update was received earlier than the previous one
						if(checkifNewUpdateisLatest(strStatusDateNew, strStatusDateOld)) {
							logger.verbose("Updating the Status date in the previous update : "+strStatusDateNew);
							docConveyTrackingUpdatesFinal.getDocumentElement()
							.setAttribute(AcademyConstants.ATTR_STATUS_DATE, strStatusDateNew);
						}
						//Updating the Tracking number in the request to concatenate with other tracking no
						String strUpdatedTrackingNo = docConveyTrackingUpdatesFinal.getDocumentElement()
								.getAttribute(AcademyConstants.ATTR_TRACKING_NO) + AcademyConstants.STR_COMMA +
								strTrackingNo;

						docConveyTrackingUpdatesFinal.getDocumentElement()
						.setAttribute(AcademyConstants.ATTR_TRACKING_NO, strUpdatedTrackingNo);
						
						docConveyTrackingUpdatesFinal.getDocumentElement()
						.setAttribute(AcademyConstants.ATTR_CNVY_TRCK_KEY, docConveyTrackingUpdatesFinal.getDocumentElement()
								.getAttribute(AcademyConstants.ATTR_CNVY_TRCK_KEY) + AcademyConstants.STR_COMMA +
								docConveyTrackingUpdates.getDocumentElement()
								.getAttribute(AcademyConstants.ATTR_CNVY_TRCK_KEY));
					}

				}
				
				//Retrieve the final Status Date and check if the Shipment is eligible in consolidation window
				String strFinalStatusDate = docConveyTrackingUpdatesFinal.getDocumentElement().getAttribute(AcademyConstants.ATTR_STATUS_DATE);
				if(checkIfShipmentIsEligibleForEmails(strConsolidateEmailWaitTime, strFinalStatusDate)) {
					logger.verbose("Shipment is eligible for emails :"+strShipmentNo);
					docConveyTrackingUpdatesFinal.getDocumentElement().setAttribute(AcademyConstants.STR_SERVICE_NAME, strServiceToBeInvoked);
					outputList.add(docConveyTrackingUpdatesFinal);
				}
				else if(checkIfCompleteShipmentIsInTransit(env, hmTrackingInfo.size(), strShipmentNo)) {
					logger.verbose("All the containers on Shipment are In Trasit to skip waiting time");
					docConveyTrackingUpdatesFinal.getDocumentElement().setAttribute(AcademyConstants.STR_SERVICE_NAME, strServiceToBeInvoked);
					outputList.add(docConveyTrackingUpdatesFinal);
				}
			}
		}
		
		//Adding logic to remove the last shipment as that can have more tracking no in next API Call.
		if(outputList.size() > 1) {
			logger.verbose(" Removing last record " + SCXmlUtil.getString(outputList.get(outputList.size() - 1 )));
			outputList.remove(outputList.size() - 1 );
		}
		//Check if only 1 record was retrieved in getShipmentList.
		else if (hmConsolidatedShipment.size() > 1 && outputList.size() == 1) {
			logger.verbose(" More than 1 record retrieved. Recheck for last record " + SCXmlUtil.getString(outputList.get(outputList.size() - 1 )));
			outputList.remove(outputList.size() - 1 );
		}

		if(outputList.size()==0) {
			logger.verbose("No Shipments eligible for emails");
		}
		
		logger.endTimer("AcademyProcessConveyEmailsAndSMS::sortShipmentsAndConsolidateContainers");
		return outputList;
	}




	/**
	 * This method checks if the Existing Status date is older than the new one.
	 * 
	 * @param strStatusDateNew
	 * @param strStatusDateOld
	 * @return boolean
	 */
	private boolean checkifNewUpdateisLatest(String strStatusDateNew, String strStatusDateOld) {
		logger.beginTimer("AcademyProcessConveyEmailsAndSMS::checkifNewUpdateisLatest");

		Date dtStatusDateOld = null;
		Date dtStatusDateNew = null;
		String strTimeStamp = null;
		boolean bIsDataToBeUpdated = false;

		logger.verbose(":strStatusDateNew :: " + strStatusDateNew + " : strStatusDateOld : " +  strStatusDateOld);

		try {
			//Check if the latest Carrier update as per the Staus Date
			if (!YFCObject.isVoid(strStatusDateOld)) {	
				strTimeStamp = stampTimezone(strStatusDateOld);
				logger.verbose("timeStamp for LatestDate : " + strTimeStamp);
				dtStatusDateOld = new SimpleDateFormat(strTimeStamp).parse(strStatusDateOld);
				logger.verbose("dtLastStatusDate : " + dtStatusDateOld);
			}
			if (!YFCObject.isVoid(strStatusDateNew)) {	
				strTimeStamp = stampTimezone(strStatusDateNew);
				logger.verbose("timeStamp for Incoming StatusDate : " + strTimeStamp);
				dtStatusDateNew = new SimpleDateFormat(strTimeStamp).parse(strStatusDateNew);
				logger.verbose("dtStatusDate : " + dtStatusDateNew);
			}

			if (YFCObject.isVoid(dtStatusDateOld) || (dtStatusDateNew.before(dtStatusDateOld))) {
				bIsDataToBeUpdated = true;
			}
		} catch (Exception g) {
			g.printStackTrace();
			logger.info(" ErrorTrace :: " + g.toString());
		}
		logger.verbose("Final condition output is "+ bIsDataToBeUpdated);
		logger.endTimer("AcademyProcessConveyEmailsAndSMS::checkifNewUpdateisLatest");
		return bIsDataToBeUpdated;
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
	 * This method updates the record in DB to have IsCustomerToBeNotified=N.
	 * 
	 * @param statusDateStr
	 * @return String
	 */
	private void updateRecordForCustomerNotified(YFSEnvironment env, Element eleConveyTrackingUpdates) {
		logger.beginTimer("AcademyProcessConveyEmailsAndSMS::updateRecordForCustomerNotified");
		try {

			String strCarrierUpdateTrackerKey = eleConveyTrackingUpdates.getAttribute(AcademyConstants.ATTR_CONVEY_TRACKER_KEY);

			Document docAcadTrackingUpdates = XMLUtil
					.createDocument(AcademyConstants.ELE_ACAD_CONVEY_TRACKING_UPDATES);
			Element eleAcadTrackingUpdates = docAcadTrackingUpdates.getDocumentElement();
			eleAcadTrackingUpdates.setAttribute(AcademyConstants.ATTR_CONVEY_TRACKER_KEY, strCarrierUpdateTrackerKey);
			eleAcadTrackingUpdates.setAttribute("IsCustomerToBeNotified", AcademyConstants.STR_NO);

			logger.verbose("Input to AcademyChangeTrackingUpdates: \n"
					+ XMLUtil.getXMLString(docAcadTrackingUpdates));

			AcademyUtil.invokeService(env, AcademyConstants.CHANGE_CONVEY_TRACKING_UPDATES,
					docAcadTrackingUpdates);				

		} catch(Exception exec) {
			logger.info("Convey Carrier Status Updates Error. Error in changeTrackingUpdatesRecord :: ");
		}
		logger.endTimer("AcademyProcessConveyEmailsAndSMS::updateRecordForCustomerNotified");

	}
	
	
	/**
	 * This method checks if the Shipment is eligible for email with this consolidation window
	 * 
	 * @param strStatusDateNew
	 * @param strStatusDateOld
	 * @return boolean
	 * @throws Exception 
	 */
	private boolean checkIfShipmentIsEligibleForEmails(String strConsolidateEmailWaitTime, String strFinalStatusDate) throws Exception {
		logger.beginTimer("AcademyProcessConveyEmailsAndSMS::checkIfShipmentIsEligibleForEmails");
		logger.verbose(":strStatusDateNew :: " + strConsolidateEmailWaitTime + " : strFinalStatusDate : " +  strFinalStatusDate);

		Date dtFinalStatusDate = null;
		boolean bIsShipmentEligibleForEmail = false;

		String strTimeStamp = stampTimezone(strFinalStatusDate);
		logger.verbose("timeStamp for LatestDate : " + strTimeStamp);
		dtFinalStatusDate = new SimpleDateFormat(strTimeStamp).parse(strFinalStatusDate);
		logger.verbose("dtLastStatusDate : " + dtFinalStatusDate);
		
		Calendar calConsolidationTime = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(strTimeStamp);// "yyyy-MM-dd"

		calConsolidationTime.add(Calendar.HOUR, (-Integer.parseInt(strConsolidateEmailWaitTime)));
		String strConsolidationDate = sdf.format(calConsolidationTime.getTime());// oldest date
		logger.verbose(":strConsolidationDate :: " + strConsolidationDate);
		
		Date dtConsolidationDate = new SimpleDateFormat(strTimeStamp).parse(strConsolidationDate);

		if (dtFinalStatusDate.before(dtConsolidationDate) || dtFinalStatusDate.equals(dtConsolidationDate)) {
			logger.verbose("Eligible for emails ");
			bIsShipmentEligibleForEmail = true;
		}
		logger.verbose("Final bIsShipmentEligibleForEmail is "+ bIsShipmentEligibleForEmail);
		logger.endTimer("AcademyProcessConveyEmailsAndSMS::checkIfShipmentIsEligibleForEmails");
		return bIsShipmentEligibleForEmail;
	}
	
	/**
	 * This method checks if the Shipment is eligible for email with this consolidation window
	 * or see if the shipment has all the containers on Shipment are in Transit.
	 * 
	 * @param strStatusDateNew
	 * @param strStatusDateOld
	 * @return boolean
	 * @throws Exception 
	 */
	private boolean checkIfCompleteShipmentIsInTransit(YFSEnvironment env, int iTrackingUpdateReceived, String strShipmentNo) throws Exception {
		logger.beginTimer("AcademyProcessConveyEmailsAndSMS::checkIfCompleteShipmentIsInTransit");
		logger.verbose(":iTrackingUpdateReceived :: " + iTrackingUpdateReceived + " : strShipmentNo : " +  strShipmentNo);

		boolean bIsShipmentEligibleForEmail = false;
		
		Document docGetShipmentListInp = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		docGetShipmentListInp.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
		
		// Preparing the API template
		Document docShipmentListTemplate = XMLUtil.getDocument("<Shipments><Shipment ShipmentNo=''><Containers><Container TrackingNo='' />"
				+ "</Containers></Shipment></Shipments>");

		// Setting template for API and invoking getShipmentList API
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, docShipmentListTemplate);
		Document docGetShipmentListOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST,
				docGetShipmentListInp);
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
		
		NodeList nlContainers = docGetShipmentListOut.getElementsByTagName(AcademyConstants.ELE_CONTAINER);
		logger.verbose(" nlContainers.getLength :: " + nlContainers.getLength());
		if(nlContainers.getLength() == iTrackingUpdateReceived) {
			logger.verbose("Eligible for emails ");
			bIsShipmentEligibleForEmail = true;
		}
		logger.verbose("Final bIsShipmentEligibleForEmail is "+ bIsShipmentEligibleForEmail);
		logger.endTimer("AcademyProcessConveyEmailsAndSMS::checkIfCompleteShipmentIsInTransit");
		return bIsShipmentEligibleForEmail;
	}

}
