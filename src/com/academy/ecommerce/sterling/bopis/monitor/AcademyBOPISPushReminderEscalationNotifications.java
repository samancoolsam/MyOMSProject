package com.academy.ecommerce.sterling.bopis.monitor;

/**#########################################################################################
 *
 * Project Name                : OMS_Nov_08_2019_Rel1
 * Module                      : OMNI-488
 * Author                      : Radhakrishna Mediboina(C0015568)
 * Author Group				   : CTS-POD
 * Date                        : 23-OCT-2019 
 * Description				   : This agent publish auto BOPIS Push Notifications(Reminder/Escalation) to ESB queue.
 * 								 
 * ---------------------------------------------------------------------------------
 * Date            Author         		Version#       		Remarks/Description                      
 * ---------------------------------------------------------------------------------
 * 23-OCT-2019		CTS  	 			  1.0           	Initial version
 *
 * #########################################################################################*/

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.text.SimpleDateFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.util.YCPBaseTaskAgent;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyBOPISPushReminderEscalationNotifications extends YCPBaseTaskAgent{

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyBOPISPushReminderEscalationNotifications.class);    
	
	/* 
	 * This method invokes getShipmentList API to get the shipment details 
	 * If shipment createTS < 2hrs of system date push Reminder notification else push Escalation notification to MQQT through ESB.
	 */
	public Document executeTask(YFSEnvironment env, Document docInput) throws Exception {
		
		log.beginTimer("AcademyBOPISPushReminderEscalationNotifications.executeTask() method");
		log.verbose(" Indoc XML \n"+XMLUtil.getXMLString(docInput));
		
		Document outDocgetShipmentList = null;
		Element eleTransactionFilters = null;
		Element eleShipment = null;
		SimpleDateFormat sdfDateFormat = null;
		Date dtEscalationHourTime = null;
		Date dtCurrentDate = null;
		String strCurrentShmtStatus = null;
		String strShmntCreateTS = null;
		String strCurrentDate = null;
		String strEscalationHourTime = null;
		String strTaskQueueKey = null;
		String strTaskQAvailableDate = null;
		String strShipmentKey = null;
		Calendar cal = null;
		
		//Fetch the TaskQueue record attribute values
		strShipmentKey = docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_DATA_KEY);
		strTaskQueueKey = docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_TASK_Q_KEY);
		strTaskQAvailableDate= docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_AVAIL_DATE);
		
		//Fetch the Agent criteria parameters
		eleTransactionFilters = (Element) docInput.getElementsByTagName(AcademyConstants.ATTR_TRANSACTION_FILTERS).item(0);
		String strShipmentStatus = eleTransactionFilters.getAttribute(AcademyConstants.STR_SHIPMENT_STATUS);
		String strEscalationSLAHours = eleTransactionFilters.getAttribute(AcademyConstants.STR_ESCALATION_SLA_HOURS);
		String strReminderSLAMinutes = eleTransactionFilters.getAttribute(AcademyConstants.STR_REMINDER_SLA_MINUTES);
		String strEscalationSLAMinutes = eleTransactionFilters.getAttribute(AcademyConstants.STR_ESCALATION_SLA_MINUTES);
		
		//Invoking the getShipmentList API for Shipment Details
		outDocgetShipmentList = getShipmentList(env, strShipmentKey);
		
		//Remove the taskQ entry, if getShipmentList API outDoc is empty
		if (outDocgetShipmentList.getDocumentElement().hasChildNodes()) {
			
			eleShipment = (Element) outDocgetShipmentList.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);
			strCurrentShmtStatus = eleShipment.getAttribute(AcademyConstants.ATTR_STATUS);
			log.verbose("Shipment Current Status :: "+strCurrentShmtStatus);		
			strShmntCreateTS = eleShipment.getAttribute(AcademyConstants.ATTR_CREATETS);
			log.verbose("Shipment CREATETS :: "+strShmntCreateTS);
			
			//Type-cast StringArray to List<String> for fast iterator/compare
			String[] strShmtStatus = strShipmentStatus.split(AcademyConstants.STR_COMMA);      
	        List<String> listShmtStatus = Arrays.asList(strShmtStatus);
	        
	        //Shipment current status should be 1100.70.06.10/1100.70.06.20(ReadyForBackroomPick/BackRoomPickInProgress), 
	        //if not Remove the Entry from taskQ table.
	        if(listShmtStatus.contains(strCurrentShmtStatus)){       
				cal = Calendar.getInstance();
		        sdfDateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
		        strCurrentDate = sdfDateFormat.format(cal.getTime());
		        log.verbose("executeTask() strCurrentDate :: "+strCurrentDate);
		        dtCurrentDate = sdfDateFormat.parse(strCurrentDate);
		        
		        //Set the shipment createts to Calendar and Add the EscalationSLAHours(configured value)
		        cal.setTime(sdfDateFormat.parse(strShmntCreateTS));
		        cal.add(Calendar.HOUR, Integer.parseInt(strEscalationSLAHours));
		        strEscalationHourTime = sdfDateFormat.format(cal.getTime());
		        log.verbose("executeTask() strEscalationHourTime(i.e ShipmnetCreatets+EscalationSLAHours) :: "+strEscalationHourTime);
		        dtEscalationHourTime = sdfDateFormat.parse(strEscalationHourTime);
		        
		        //if System time < ShipmentCreatets+EscalationSLAHours(configured value) then push Reminder Notification else push Escalation Notification
		        if(dtCurrentDate.before(dtEscalationHourTime)) {
		        	
		        	//SystemTime < ShipmentCreatets+EscalationSLAHours(configured value) push Reminder Notification
					log.verbose("Invoking AcademySendReminderShipmentNotification service, docInput :\n" + XMLUtil.getXMLString(outDocgetShipmentList));
					AcademyUtil.invokeService(env,AcademyConstants.SERVICE_ACAD_SEND_REMINDER_NOTIFICATION, outDocgetShipmentList);
					
		        	//Updating TaskQ entry with next available date 
					manageTaskQueueRecord(env, strReminderSLAMinutes, strTaskQueueKey, strTaskQAvailableDate);	
					
		        }else {
		        	//SystemTime > ShipmentCreatets+EscalationSLAHours(configured value) push Escalation Notification
					log.verbose("Invoking AcademySendEscalationShipmentNotification service, docInput :\n" + XMLUtil.getXMLString(outDocgetShipmentList));
					AcademyUtil.invokeService(env,AcademyConstants.SERVICE_ACAD_SEND_ESCALATION_NOTIFICATION, outDocgetShipmentList);
					
		        	//Updating TaskQ entry with next available date
					manageTaskQueueRecord(env, strEscalationSLAMinutes, strTaskQueueKey, strTaskQAvailableDate);
		        }		        
	        }else {
	        	
	    		//Remove Entry from taskQ table where shipment current status is not in 1100.70.06.10/1100.70.06.20(ReadyForBackroomPick/BackRoomPickInProgress)
	    		removeTaskQueueRecord(env, strTaskQueueKey);
	        }        
		}else {
			
			//Remove taskQ entry if shipmneKey invalid
			removeTaskQueueRecord(env, strTaskQueueKey);
		}
  
		log.endTimer("AcademyBOPISPushReminderEscalationNotifications.executeTask() method");
		
		return docInput;
	}

	/** This method executes getShipmentList API to fetch the shipment details.
	 * Sample Input XML
	 * 	<Shipment ShipmentKey="201910230103162120810024"/>
	 * @param env
	 * @param inDoc
	 * @return outDoc
	 * @throws Exception 
	 **/	
	private Document getShipmentList(YFSEnvironment env, String strShipmentKey) throws Exception {	
		
		log.beginTimer("AcademyPushBOPISReminderEscalationNotifications.getShipmentList() method");
		
		Document docIngetShipmentList = null;
		Document docOutgetShipmentList = null;
		
		//getShipmentList API inDoc
		docIngetShipmentList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		docIngetShipmentList.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
		
		//getShipmentList API Template
		Document docgetShipmentListTemplate = XMLUtil.getDocument(
					 "<Shipments>"
					+ 	"<Shipment ShipmentKey=\"\" ShipmentNo=\"\" OrderNo=\"\" Status=\"\" Createts=\"\" ShipNode=\"\" DeliveryMethod=\"\"></Shipment>"
					+"</Shipments>");		
		log.verbose("getShipmentList API indoc XML : \n" + XMLUtil.getXMLString(docIngetShipmentList));
		
		//Invoking the getShipmentList API
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, docgetShipmentListTemplate);		
		docOutgetShipmentList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST, docIngetShipmentList);
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
		
		log.verbose("Outdoc of getShipmentList :: \n"+ XMLUtil.getXMLString(docOutgetShipmentList));				
		log.endTimer("AcademyPushBOPISReminderEscalationNotifications.getShipmentList() method");

		return docOutgetShipmentList;
	}	
	
	/** This method executes manageTaskQueue API to update the taskQ entry(AvailableDate)
	 * 
	 * Sample Input XML
	 * 	<TaskQueue AvailableDate="2019-10-25T05:45:02" TaskQKey="201910230332062120822109" Operation="Modify"/>
	 * 
	 * @param env
	 * @param strrSLAMinutes
	 * @param strTaskQueueKey
	 */	
	private void manageTaskQueueRecord(YFSEnvironment env, String strSLAMinutes, String strTaskQueueKey, String strTaskQAvailableDate) {
		
		log.beginTimer("AcademyPushBOPISReminderEscalationNotifications.manageTaskQueueRecord() method");
		
		SimpleDateFormat sdfDateFormat = null;		
		String strSystemDatePlusSLAMnts = null;
		String strTaskQNextAvailDate = null;
		String strTaskQAvailDatePlusSLAMnts = null;
		Document manageTaskQueueinDoc = null;
		Date dtSystemDate = null;
		Date dtTaskNxtAvailDate = null;
		Calendar cal = null;
		
		try {
			//Fetch the System time and Add the Reminder/Escalation SLA (configured value)
			cal = Calendar.getInstance();
	        sdfDateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
	        log.verbose("manageTaskQueueRecord() SystemDate :: "+sdfDateFormat.format(cal.getTime()));
	        dtSystemDate = sdfDateFormat.parse(sdfDateFormat.format(cal.getTime()));
	        log.verbose("manageTaskQueueRecord() strSLAMinutes :: "+strSLAMinutes);
	        cal.add(Calendar.MINUTE, Integer.parseInt(strSLAMinutes));
	        strSystemDatePlusSLAMnts = sdfDateFormat.format(cal.getTime());
	        log.verbose("manageTaskQueueRecord() strSystemDatePlusSLA :: "+strSystemDatePlusSLAMnts);
	        
	        //Set the TaskQueueAvailableDate to Calendar and Add the SLAMinutes(configured value)
	        log.verbose("manageTaskQueueRecord() TaskQAvailableDate :: "+strTaskQAvailableDate);
	        cal.setTime(sdfDateFormat.parse(strTaskQAvailableDate));
	        cal.add(Calendar.MINUTE, Integer.parseInt(strSLAMinutes));
	        strTaskQAvailDatePlusSLAMnts = sdfDateFormat.format(cal.getTime());
	        log.verbose("manageTaskQueueRecord() strTaskQAvailDatePlusSLAMnts :: "+strTaskQAvailDatePlusSLAMnts);
	        dtTaskNxtAvailDate = sdfDateFormat.parse(strTaskQAvailDatePlusSLAMnts);	     
	        
	        /**
	         * if NextTaskQAvailDate(TaskQAvailDate+SLAMinutes) < SystemDate
			 * 	Then
			 * 		NextTaskQAvailDate = SystemDate+SLAMinutes;		
			 * else
			 * 		NextTaskQAvailDate = TaskQAvailDate+SLAMinutes;
	         */
	        if(dtTaskNxtAvailDate.before(dtSystemDate)) {
	        	
	        	strTaskQNextAvailDate = strSystemDatePlusSLAMnts;
	        	log.verbose("\nTaskQNextAvailDate(CurrentTaskQAvailDate+SLAMnts) < system date :: TaskQNextAvailDate is (SystemDate+SLAMinutes) :: "+strTaskQNextAvailDate);
	        	
	        }else {
	        	
	        	strTaskQNextAvailDate = strTaskQAvailDatePlusSLAMnts;
	        	log.verbose("\nTaskQNextAvailDate(CurrentTaskQAvailDate+SLAMnts) > system date :: TaskQNextAvailDate is (TaskQAvailDate+SLAMinutes) :: "+strTaskQNextAvailDate);	        
	        }
	        
	        //manageTaskQueue API inDoc
			manageTaskQueueinDoc = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
			manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_OPERATION, AcademyConstants.STR_ACTION_MODIFY);
			manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_TASK_Q_KEY, strTaskQueueKey);
			manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_AVAIL_DATE, strTaskQNextAvailDate);								
			//Invoking the manageTaskQueue API
			log.verbose("Input to manageTaskQueue API :: "+XMLUtil.getXMLString(manageTaskQueueinDoc));			
			AcademyUtil.invokeAPI(env,AcademyConstants.API_MANAGE_TASK_QUEUE, manageTaskQueueinDoc);
		} 
		catch (Exception e) {
			log.info("Exception occured in AcademyPushBOPISReminderEscalationNotifications.manageTaskQueueRecord() method for strTaskQueueKey : "+strTaskQueueKey);
			log.verbose(e.getMessage());
			throw new YFSException(e.getMessage());
		}
		log.endTimer("AcademyPushBOPISReminderEscalationNotifications.manageTaskQueueRecord() method");
	}

	
	/** This method executes manageTaskQueue API to remove processed shipments
	 * Sample Input XML
	 * 	<TaskQueue Operation="Delete" TaskQKey="2018090110050322699633"/>
	 * @param env
	 * @param inDoc
	 * @return
	 */
	private void removeTaskQueueRecord(YFSEnvironment env, String strTaskQueueKey) {
		
		log.beginTimer("AcademyPushBOPISReminderEscalationNotifications.removeTaskQueueRecord() method");
		
		Document manageTaskQueueinDoc = null;
		
		try {
			//manageTaskQueue API inDoc
			manageTaskQueueinDoc = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
			manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_OPERATION, AcademyConstants.STR_OPERATION_VAL_DELETE);
			manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_TASK_Q_KEY, strTaskQueueKey);
			
			//Invoking the manageTaskQueue API
			log.verbose("Indoc to manageTaskQueue API :: "+XMLUtil.getXMLString(manageTaskQueueinDoc));					
			AcademyUtil.invokeAPI(env,AcademyConstants.API_MANAGE_TASK_QUEUE, manageTaskQueueinDoc);			
		}
		catch (Exception e) {
			log.info("Exception occured in AcademyPushBOPISReminderEscalationNotifications.removeTaskQueueRecord() method for strTaskQueueKey : "+strTaskQueueKey);
			log.verbose(e.getMessage());
			throw new YFSException(e.getMessage());
		}
		log.endTimer("AcademyPushBOPISReminderEscalationNotifications.removeTaskQueueRecord() method");
	}
	
}
