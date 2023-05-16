package com.academy.ecommerce.sterling.shipment;

/**#########################################################################################
 *
 * Project Name                : ASO
 * Module                      : OMNI-48139
 * Author                      : C0028009, C0023461
 * Author Group				   : CTS - POD
 * Date                        : 08-SEP-2021
 * Description				   : This class  Creates Record into TaskQ and updates the ShipmentType to STS for
 * 								 STS2.0 order and removes TaskQ record
 * 								 If Agent process and update shipment Type - Remove task queue entry
 * 								 In case of error, updates Task Queue with Available date + Retry Interval Minutes
 *                                
 * 								

 * ---------------------------------------------------------------------------------
 * Date            	Author         		Version#       		Remarks/Description                      
 * ---------------------------------------------------------------------------------
 * 08-SEP-2021		CTS  	 			 1.0           		Initial version
 *
 * #########################################################################################*/
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.util.YCPBaseTaskAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dblayer.YFCContext;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyShipmentTypeUpdateToSTS extends YCPBaseTaskAgent {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyShipmentTypeUpdateToSTS.class);
	private static Properties props;

	/**
	 * This method executes manageTaskQueue API to insert records into TaskQ Input
	 * XML for manageTaskQueue API
	 * <TaskQueue AvailableDate="2021-09-15T03:24:35" DataKey="2018090110050322699633" DataType="ShipmentKey"
	 * TransactionId= "ACAD_SHIPTYPE_UPDT_STS.0006.ex" />
	 * 
	 * @param env
	 * @param inXML
	 */
	public Document manageTaskQForSTS(YFSEnvironment env, Document inXML) throws Exception {
		log.beginTimer("AcademyShipmentTypeUpdateToSTS.manageTaskQForSTS()");

		Element eleShipment = inXML.getDocumentElement();

		String strShipmentKey = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		// manageTaskQueue API inDoc to create TaskQ entry
		if (!YFCCommon.isVoid(strShipmentKey)) {
			Document manageTaskQueueinDoc = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);

			manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_DATA_KEY, strShipmentKey);
			manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_DATA_TYPE,
					AcademyConstants.SHIPMENT_KEY);
			manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_TRANS_ID,
					AcademyConstants.TRXN_ACAD_SHIPTYPE_UPDT_STS);
			log.verbose(XMLUtil.getXMLString(manageTaskQueueinDoc));
			
			/* OMNI-48139: Start Change - Read the service argument for fetching delay in mins which is to be added to system date */
			String shipmentTypeSTSUpdtDelayInMins=props.getProperty(AcademyConstants.STR_SHIPMENT_TYPE_STS_UPDT_DELAY_MINS);
			log.verbose("shipmentTypeSTSUpdtDelayInMins:"+shipmentTypeSTSUpdtDelayInMins);
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdfDateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			log.verbose("manageTaskQForSTS() AvailableDate :: " + sdfDateFormat.format(cal.getTime()));
			if(!YFCObject.isVoid(shipmentTypeSTSUpdtDelayInMins)) {
			//Add the delay in mins to the system date and set it as available date
			cal.add(Calendar.MINUTE, Integer.parseInt(shipmentTypeSTSUpdtDelayInMins));
			String availableDate = sdfDateFormat.format(cal.getTime());
			log.verbose("manageTaskQForSTS() AvailableDate post adding Delay:: " + availableDate);
			manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_AVAIL_DATE, availableDate);
			}
			/* OMNI-48139: End Change*/
			// Invoking the manageTaskQueue API
			AcademyUtil.invokeAPI(env, AcademyConstants.API_MANAGE_TASK_QUEUE, manageTaskQueueinDoc);
		}
		log.endTimer("AcademyShipmentTypeUpdateToSTS.manageTaskQForSTS()");
		return inXML;
	}
	
	/*OMNI-48139: Start Change - Setter method for properties*/
	public void setProperties(Properties props) {
		AcademyShipmentTypeUpdateToSTS.props = props;
	}
	/*OMNI-48139: End Change*/

	/**
	 * This method executes manageTaskQueue API to remove processed TaskQ Sample
	 * Input XML <TaskQueue Operation="Delete" TaskQKey="2018090110050322699633"/>
	 * 
	 * @param env
	 * @param inDoc
	 */
	private void removeTaskQueueRecord(YFSEnvironment env, String strTaskQueueKey) {

		log.beginTimer("AcademyShipmentTypeUpdateToSTS.removeTaskQueueRecord()");

		try {
			// manageTaskQueue API inDoc to remove TaskQ entry
			Document removeTaskQueueinDoc = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
			removeTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_OPERATION,
					AcademyConstants.STR_OPERATION_VAL_DELETE);
			removeTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_TASK_Q_KEY, strTaskQueueKey);

			// Invoking the manageTaskQueue API
			log.verbose("Input document to manageTaskQueue API :: " + XMLUtil.getXMLString(removeTaskQueueinDoc));
			AcademyUtil.invokeAPI(env, AcademyConstants.API_MANAGE_TASK_QUEUE, removeTaskQueueinDoc);

		} catch (Exception e) {
			log.info(
					"Exception occurred in AcademyShipmentTypeUpdateToSTS.removeTaskQueueRecord() method for strTaskQueueKey : "
							+ strTaskQueueKey);
			log.info("Exception : " + e + "Exception Message : " + e.getMessage());
			e.printStackTrace();
		}
		log.endTimer("AcademyShipmentTypeUpdateToSTS.removeTaskQueueRecord()");
	}

	/**
	 * This method executes manageTaskQueue API to update the
	 * taskQentry(AvailableDate)
	 * 
	 * Sample Input XML <TaskQueue AvailableDate="2019-10-25T05:45:02" TaskQKey=
	 * "201910230332062120822109" Operation="Modify"/>
	 * 
	 * @param env
	 * @param strRetryIntervelSLAMinutes
	 * @param strTaskQueueKey
	 * @param strTaskQAvailableDate
	 */
	private void manageTaskQAvailableDate(YFSEnvironment env, String strRetryIntervelSLAMinutes, String strTaskQueueKey,
			String strTaskQAvailableDate) {

		log.beginTimer("AcademyShipmentTypeUpdateToSTS.manageTaskQAvailableDate()");

		String strTaskQNextAvailDate = null;

		try {
			// Fetch the System time and Add the Reminder/Escalation SLA (configured value)
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdfDateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			log.verbose("manageTaskQAvailableDate() SystemDate :: " + sdfDateFormat.format(cal.getTime()));
			Date dtSystemDate = sdfDateFormat.parse(sdfDateFormat.format(cal.getTime()));
			log.verbose("manageTaskQAvailableDate() strRetryIntervelSLAMinutes :: " + strRetryIntervelSLAMinutes);
			cal.add(Calendar.MINUTE, Integer.parseInt(strRetryIntervelSLAMinutes));
			String strSystemDatePlusRetrySLAMnts = sdfDateFormat.format(cal.getTime());
			log.verbose("manageTaskQAvailableDate() strSystemDatePlusRetrySLA :: " + strSystemDatePlusRetrySLAMnts);

			// Set the TaskQueueAvailableDate to Calendar and Add the SLAMinutes(configured
			// value)
			log.verbose("manageTaskQAvailableDate() TaskQAvailableDate :: " + strTaskQAvailableDate);
			cal.setTime(sdfDateFormat.parse(strTaskQAvailableDate));
			cal.add(Calendar.MINUTE, Integer.parseInt(strRetryIntervelSLAMinutes));
			String strTaskQAvailDatePlusRetrySLAMnts = sdfDateFormat.format(cal.getTime());
			log.verbose("manageTaskQAvailableDate() strTaskQAvailDatePlusRetrySLAMnts :: "
					+ strTaskQAvailDatePlusRetrySLAMnts);
			Date dtTaskNxtAvailDate = sdfDateFormat.parse(strTaskQAvailDatePlusRetrySLAMnts);

			/**
			 * if NextTaskQAvailDate(TaskQAvailDate+RetrySLAMinutes) < SystemDate
			 * NextTaskQAvailDate = SystemDate+RetrySLAMinutes; else NextTaskQAvailDate =
			 * TaskQAvailDate+RetrySLAMinutes;
			 */
			if (dtTaskNxtAvailDate.before(dtSystemDate)) {
				strTaskQNextAvailDate = strSystemDatePlusRetrySLAMnts;
				log.verbose(
						"\nTaskQNextAvailDate(CurrentTaskQAvailDate+SLAMnts) < system date :: TaskQNextAvailDate is (SystemDate+SLAMinutes) :: "
								+ strSystemDatePlusRetrySLAMnts);
			} else {
				strTaskQNextAvailDate = strTaskQAvailDatePlusRetrySLAMnts;
				log.verbose(
						"\nTaskQNextAvailDate(CurrentTaskQAvailDate+SLAMnts) > system date :: TaskQNextAvailDate is (TaskQAvailDate+SLAMinutes) :: "
								+ strTaskQNextAvailDate);
			}

			// manageTaskQueue API inDoc
			Document manageTaskQueueinDoc = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
			manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_OPERATION,
					AcademyConstants.STR_ACTION_MODIFY);
			manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_TASK_Q_KEY, strTaskQueueKey);
			manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_AVAIL_DATE,
					strTaskQNextAvailDate);

			log.verbose("Input to manageTaskQueue API :: " + XMLUtil.getXMLString(manageTaskQueueinDoc));
			// Invoking the manageTaskQueue API
			AcademyUtil.invokeAPI(env, AcademyConstants.API_MANAGE_TASK_QUEUE, manageTaskQueueinDoc);

		} catch (Exception e) {
			log.info(
					"Exception occurred in AcademyShipmentTypeUpdateToSTS.manageTaskQAvailableDate() method for strTaskQueueKey : "
							+ strTaskQueueKey);
			log.info("Exception : " + e + "Exception Message : " + e.getMessage());
			e.printStackTrace();
		}
		log.endTimer("AcademyShipmentTypeUpdateToSTS.manageTaskQAvailableDate()");
	}

	/**
	 * inXML <TaskQueue AvailableDate="2021-08-16T04:19:09-05:00" Createprogid=
	 * "SterlingHttpTester" Createts="2021-08-16T04:19:09-05:00" Createuserid=
	 * "C0023461" DataKey="20210813051808226626224" DataType="ShipmentKey" HoldFlag=
	 * "N" Lockid="0" Modifyprogid="SterlingHttpTester" Modifyts=
	 * "2021-08-16T04:19:09-05:00" Modifyuserid="C0023461" TaskQKey=
	 * "20210816041909226692719" TransactionKey="20210809012129226370166">
	 * <TransactionFilters Action="Get" DocumentParamsKey="0006" DocumentType="0006"
	 * NumRecordsToBuffer="5000" ProcessType="TO_DELIVERY" ProcessTypeKey=
	 * "200307081424521056" RetryIntervalSLAMinutes="15" TransactionId=
	 * "TASK_QUEUE_UPDATE_TO_STS.0006.ex" TransactionKey="20210809012129226370166"/>
	 * </TaskQueue>
	 **/
	/**
	 * Agent "AcademyShipmentTypeUpdateSTSAgentServer" will be pick the record If
	 * ShipmentType is not null,then we have to update the ShipmentType to STS by
	 * update the Query and at the same time it will remove record from the TaskQ
	 * Table If It will fail to Update the ShipmentType manageTaskQAvailableDate
	 * method is invoked to Update the NextAvailable Date
	 * 
	 **/
	@Override
	public Document executeTask(YFSEnvironment env, Document inXML) throws Exception {
		log.beginTimer("AcademyShipmentTypeUpdateToSTS.executeTask()");
		log.debug("Input to AcademyShipmentTypeUpdateToSTS.executeTask() :: " + XMLUtil.getXMLString(inXML));
		// Fetch the TaskQueue record attribute values
		Element eleShipment = inXML.getDocumentElement();
		String strShipmentKey = eleShipment.getAttribute(AcademyConstants.ATTR_DATA_KEY);
		String strTaskQueueKey = eleShipment.getAttribute(AcademyConstants.ATTR_TASK_Q_KEY);
		String strTaskQAvailableDate = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_AVAIL_DATE);
		// Fetch the Agent criteria parameters
		Element eleTransactionFilters = (Element) inXML.getElementsByTagName(AcademyConstants.ATTR_TRANSACTION_FILTERS)
				.item(0);
		String strRetryIntervelSLAMinutes = eleTransactionFilters
				.getAttribute(AcademyConstants.STR_RETRY_INTERVEL_SLA_MINTS);
		if (!YFCCommon.isVoid(strShipmentKey)) {
			String strUpdateShipmentTypeQry = "UPDATE STRLADM." + AcademyConstants.TABLE_YFS_SHIPMENT + " SET "
					+ "SHIPMENT_TYPE" + "='" + AcademyConstants.STR_SHIP_TO_STORE + "'  WHERE SHIPMENT_KEY='"
					+ strShipmentKey + "'";
			log.verbose("Query to update Shipment Type as STS: \n " + strUpdateShipmentTypeQry);
			Statement stmt = null;
			try {
				YFCContext ctxt = (YFCContext) env;
				stmt = ctxt.getConnection().createStatement();
				int hasUpdated = stmt.executeUpdate(strUpdateShipmentTypeQry);
				if (hasUpdated > 0) {
					log.verbose("Shipment type has been successfully updated to STS.");
					removeTaskQueueRecord(env, strTaskQueueKey);
				}
			} catch (SQLException sqlEx) {
				log.verbose("Error occured: Shipment type has not been updated.");
				manageTaskQAvailableDate(env, strRetryIntervelSLAMinutes, strTaskQueueKey, strTaskQAvailableDate);
				sqlEx.printStackTrace();
				throw sqlEx;
			} finally {
				if (stmt != null)
					stmt.close();
				stmt = null;
			}
		}

		log.endTimer("AcademyShipmentTypeUpdateToSTS.executeTask()");
		return inXML;

	}

}
