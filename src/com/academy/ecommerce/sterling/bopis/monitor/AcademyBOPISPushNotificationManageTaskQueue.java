package com.academy.ecommerce.sterling.bopis.monitor;

/**#########################################################################################
*
* Project Name                : OMS_Nov_08_2019_Rel1
* Module                      : OMNI-488
* Author                      : Radhakrishna Mediboina(C0015568)
* Author Group				  : CTS - POD
* Date                        : 23-Oct-2019 
* Description				  : Creating taskQ entry for BOPIS orders where on-success event of createShipment Transaction 
* 								where next available date is SystemDate + NotificationSLA(configured value) 
* 								 
* ---------------------------------------------------------------------------------
* Date            Author         		Version#       		Remarks/Description                      
* ---------------------------------------------------------------------------------
* 23-Oct-2019		CTS  	 			  1.0           	Initial version
*
* #########################################################################################*/

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyBOPISPushNotificationManageTaskQueue implements YIFCustomApi{
	
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyBOPISPushNotificationManageTaskQueue.class);    
	
	//Define properties to fetch service level argument values
    private Properties props;
	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}
	
	/**
	 * Preparing input for manageTaskQueue API
	 * @param strTaskQDataKey
	 * @return docInManageTaskQueue
	 * @throws Exception
	 * 
	 * <TaskQueue AvailableDate="SystemDate+15Mnts" DataKey="2019080906191417896577" DataType="ShipmentKey" TransactionId="ACAD_BOPIS_NOTIFICATIONS.0001.ex"/>
	 */
	public Document prepareManageTaskQueueInput(YFSEnvironment env, Document inXML) throws Exception {		
		log.beginTimer("AcademyBOPISPushNotificationManageTaskQueue.prepareManageTaskQueueInput()");
		
		Document docInManageTaskQueue = null;
		String strIsTaskQEntryEnabled = null;
		
		//Get the service level argument value
		strIsTaskQEntryEnabled = props.getProperty(AcademyConstants.STR_IS_TASKQ_ENTRY_ENABLED);
		log.verbose("AcademyBOPISPushNotificationManageTaskQueue.prepareManageTaskQueueInput() :: IsTaskQEntryEnabled : "+strIsTaskQEntryEnabled);
		
		//Service argument flag(IsTaskQEntryEnabled='Y') to decide the taskQ entry
		if(!YFCObject.isVoid(strIsTaskQEntryEnabled) && strIsTaskQEntryEnabled.equals(AcademyConstants.STR_YES)) {
						
			Element eleInManageTaskQueue = null;
			SimpleDateFormat sdfDateFormat = null;
			String strTaskQDataKey = null;
			String strInitialReminderSLA = null;
			String strNextAvailDate = null;
						
			strInitialReminderSLA = props.getProperty(AcademyConstants.STR_INITIAL_REMINDER_SLA_MINTS);
			
			//Get the SystemDate and Add NotificationSLA(configured value) as next available date
			Calendar cal = Calendar.getInstance();
	        sdfDateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
	        cal.add(Calendar.MINUTE, Integer.parseInt(strInitialReminderSLA));
	        strNextAvailDate = sdfDateFormat.format(cal.getTime());	        
			strTaskQDataKey = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			
			//manageTaskQueue API inDoc
			docInManageTaskQueue = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
			eleInManageTaskQueue = docInManageTaskQueue.getDocumentElement();
			eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_AVAIL_DATE, strNextAvailDate);
			eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_KEY, strTaskQDataKey);
			eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_TYPE, AcademyConstants.ATTR_SHIPMENT_KEY);
			eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_TRANS_ID, AcademyConstants.ACAD_BOPIS_NOTIFIXATIONS_TRAN_ID);
			
			log.verbose("TaskQ Entry for BOPIS Notiifcations :: "+SCXmlUtil.getString(docInManageTaskQueue)); 
		}
		log.endTimer("AcademyBOPISPushNotificationManageTaskQueue.prepareManageTaskQueueInput()");
		
		return docInManageTaskQueue;
	}

}
