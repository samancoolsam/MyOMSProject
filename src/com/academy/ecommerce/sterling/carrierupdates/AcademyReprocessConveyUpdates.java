package com.academy.ecommerce.sterling.carrierupdates;

/**#########################################################################################
 *
 * Project Name                : Convey
 * Author                      : Fulfillment POD
 * Author Group				  : DSV
 * Date                        : 20-APR-2023 
 * Description				  : This class fetches all the records which were not processed
 * 								and reprocess those records for carrier updates
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
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyReprocessConveyUpdates extends YCPBaseAgent {

	private static YFCLogCategory logger = YFCLogCategory.instance(AcademyReprocessConveyUpdates.class);

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
		logger.beginTimer("AcademyReprocessConveyUpdates::getJobs");
		logger.verbose("Inside getJobs.The Input xml is : " + XMLUtil.getXMLString(docInput));

		List<Document> outputList = new ArrayList<>();
		NodeList nlConveyTrackingUpdates = null;
		Element eleConveyTrackingUpdates = null;
		String strNumOfRecords = null;
		String strFromDateRange = null;
		String strToDateRange = null;

		strNumOfRecords = docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_NUM_RECORDS);
		strFromDateRange = docInput.getDocumentElement().getAttribute(AcademyConstants.STR_FROM_DATE_RANGE);
		strToDateRange = docInput.getDocumentElement().getAttribute(AcademyConstants.STR_TO_DATE_RANGE);

		if (YFCObject.isNull(strNumOfRecords) && YFCObject.isVoid(strNumOfRecords)) {
			strNumOfRecords = "500";
		}

		Calendar calFromDate = Calendar.getInstance();
		Calendar calToDate = Calendar.getInstance();

		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_SIMPLE_DATE_PATTERN);// "yyyy-MM-dd"

		calFromDate.add(Calendar.DATE, (-Integer.parseInt(strFromDateRange)));
		String strFromOrderDate = sdf.format(calFromDate.getTime());// oldest date

		calToDate.add(Calendar.DATE, (-Integer.parseInt(strToDateRange)));
		String strToOrderDate = sdf.format(calToDate.getTime());// earliest date

		Document docGetConveyTrackingUpdatesListInp = XMLUtil.createDocument(AcademyConstants.ELE_ACAD_CONVEY_TRACKING_UPDATES);
		Element eleGetConveyTrackingUpdatesListInp = docGetConveyTrackingUpdatesListInp.getDocumentElement();

		//Last message present in the request
		if (!YFCObject.isVoid(docLastMessage)) {
			logger.verbose("LastMessage is present for the AcadConveyTrackingUpdates" + XMLUtil.getXMLString(docLastMessage));
			String strLastConveyTrackingUpdateKey = docLastMessage.getDocumentElement().getAttribute(AcademyConstants.ATTR_CNVY_TRCK_KEY);
			eleGetConveyTrackingUpdatesListInp.setAttribute(AcademyConstants.ATTR_CNVY_TRCK_KEY, strLastConveyTrackingUpdateKey);
			eleGetConveyTrackingUpdatesListInp.setAttribute(AcademyConstants.ATTR_CNVY_TRCK_KEY_QRY_TYPE,
					AcademyConstants.GT_QRY_TYPE);
		}
		
		eleGetConveyTrackingUpdatesListInp.setAttribute(AcademyConstants.ATTR_STATUS_DATE_QRY_TYPE,
				AcademyConstants.BETWEEN);
		eleGetConveyTrackingUpdatesListInp.setAttribute(AcademyConstants.ATTR_FROM_STATUS_DATE,
				strFromOrderDate + "T00:00:00");
		eleGetConveyTrackingUpdatesListInp.setAttribute(AcademyConstants.ATTR_TO_STATUS_DATE,
				strToOrderDate + "T23:59:59");
		eleGetConveyTrackingUpdatesListInp.setAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED,
				AcademyConstants.STR_NO);

		eleGetConveyTrackingUpdatesListInp.setAttribute(AcademyConstants.ATTR_MAX_RECORD, strNumOfRecords);
		
		//Order by the AcadConveyTrackingUpdatesKey
		Element eleOrderBy = docGetConveyTrackingUpdatesListInp.createElement(AcademyConstants.ELE_ORDERBY);
		eleGetConveyTrackingUpdatesListInp.appendChild(eleOrderBy);

		Element eleAttribute = docGetConveyTrackingUpdatesListInp.createElement(AcademyConstants.ELE_ATTRIBUTE);
		eleOrderBy.appendChild(eleAttribute);
		eleAttribute.setAttribute(AcademyConstants.ATTR_DESC_SHORT, AcademyConstants.STR_NO);
		eleAttribute.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ATTR_CNVY_TRCK_KEY);

		logger.debug("Input Doc to get Tracker Details:: \n" + XMLUtil.getXMLString(docGetConveyTrackingUpdatesListInp));

		Document docGetConveyTrackingUpdatesListOut = AcademyUtil.invokeService(env,
				AcademyConstants.GET_CONVEY_TRACKING_UPDATES_LIST, docGetConveyTrackingUpdatesListInp);
	
		//Check if the output has any valid records
		if (!YFCObject.isNull(docGetConveyTrackingUpdatesListOut)) {
			nlConveyTrackingUpdates = docGetConveyTrackingUpdatesListOut
					.getElementsByTagName(AcademyConstants.ELE_ACAD_CONVEY_TRACKING_UPDATES);
		}

		if (!YFCObject.isNull(nlConveyTrackingUpdates) & nlConveyTrackingUpdates.getLength() > 0 ) {
			for (int i = 0; i < nlConveyTrackingUpdates.getLength(); i++) {
				eleConveyTrackingUpdates = (Element) nlConveyTrackingUpdates.item(i);

				if (!YFCObject.isNull(eleConveyTrackingUpdates)) {
					outputList.add(XMLUtil.getDocumentForElement(eleConveyTrackingUpdates));
				}
			}
		}
		logger.verbose(" Final output list " + outputList.size());
		logger.endTimer("AcademyReprocessConveyUpdates::getJobs");
		return outputList;
	}


	/**
	 * This method is reprocesses all the failed messages
	 * 
	 * @param env
	 * @param docInput
	 **Sample message input:
	 */
	@Override
	public void executeJob(YFSEnvironment env, Document docInput) throws Exception {
		logger.beginTimer("AcademyReprocessConveyUpdates::executeJob");
		logger.verbose("Inside executeJobs.The Input xml is : " + XMLUtil.getXMLString(docInput));
		try {
			Element eleInput = docInput.getDocumentElement();
			String strTrackingNo = eleInput.getAttribute(AcademyConstants.ATTR_TRACKING_NO);
			String strConveyStatus = eleInput.getAttribute(AcademyConstants.ATTR_CONVEY_STATUS);
			String strStatusDate = eleInput.getAttribute(AcademyConstants.ATTR_STATUS_DATE);
			
			// Convey - reprocessing - Start
			// Reprocess CONVEY error records by invoking sync service
			Document docReprocessShipment = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			Element eleShipment = docReprocessShipment.getDocumentElement();
			eleShipment.setAttribute(AcademyConstants.ATTR_TRACKING_NO, strTrackingNo);
			eleShipment.setAttribute(AcademyConstants.ATTR_STATUS, strConveyStatus);
			eleShipment.setAttribute(AcademyConstants.CONVEY_EVENT_TYPE,
					AcademyConstants.CONVEY_EVENT_TYPE_TRACKING);
			eleShipment.setAttribute(AcademyConstants.CONVEY_REPROCESS, AcademyConstants.STR_YES);

			Element eleEventDetails = docReprocessShipment.createElement(AcademyConstants.ELE_EVENT_DETAILS);
			eleEventDetails.setAttribute(AcademyConstants.CONVEY_EVENT_DATE,
					strStatusDate.substring(0, 10));
			eleEventDetails.setAttribute(AcademyConstants.CONVEY_EVENT_TIME,
					strStatusDate.substring(11, 19));
			eleEventDetails.setAttribute(AcademyConstants.CONVEY_EVENT_UTC, strStatusDate.substring(19));
			eleShipment.appendChild(eleEventDetails);
			
			logger.verbose("Input to Sync service AcademyProcessConveyUpdates ::: \n"
					+ XMLUtil.getXMLString(docReprocessShipment));
			AcademyUtil.invokeService(env, AcademyConstants.ACAD_PROCESS_CONVEY_UPDATES, docReprocessShipment);

			// Convey - reprocessing - End
		} catch (Exception e) {
			logger.error("Exception inside AcademyReprocessConveyUpdates : executeJob " + e.getMessage());
			throw new YFSException(e.getMessage());
		}
		logger.endTimer("AcademyReprocessConveyUpdates::executeJob");
	}

}
