package com.academy.ecommerce.sterling.bopis.shipment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyBOPISUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.sterlingcommerce.baseutil.SCXmlUtil;


/**
 * @Author : Chiranthan(SapientRazorfish_)
 * @JIRA# : BOPIS-<Stamp ACADEMY_MAX_CUSTOMER_PICK_DATE>
 * @Date : Created on 16-07-2018
 * 
 * @Purpose : 
 * This class calculates & stamps the ACADEMY_MAX_CUSTOMER_PICK_DATE for BOPIS shipments.
 * This class is invoked when BOPIS Shipment moves to 'Ready For Customer Pick'/'Paper Work Initiated'.
 * 
 * ACADEMY_MAX_CUSTOMER_PICK_DATE = Shipment StatusDate('Ready For Customer Pick'/'Paper Work Initiated') + Configurable SLA days(7-Regular BOPIS / 30-Firearms BOPIS)	
 *   	  
 **/

public class AcademyBOPISStampMaxCustomerPickDate {
	
	private static Logger log = Logger.getLogger(AcademyBOPISStampMaxCustomerPickDate.class.getName());
	private Properties props;
	public void setProperties(Properties props) throws Exception {		
		this.props = props;
	}
	
	
	public Document stampMaxCustPickDate(YFSEnvironment env, Document inDoc) throws Exception {	
		log.verbose("Entering AcademyBOPISStampMaxCustomerPickDate.stampMaxCustPickDate() :: "+XMLUtil.getXMLString(inDoc));		
		String strShipmentKey = null;
		String strStatus = null;
		String strStatusDate = null;
		String strMaxCustPickDate = null;
		String strCustPickupSLADays = null;
		Date dtCurrentDate = null;
		Date dtShipmentStatusDate = null;
		Date dtMaxCustPickDate = null;
		Element eleIn = inDoc.getDocumentElement();
		Element eleCommonCode = null;
		Document docOutGetCommonCodeList = null;
		Document outDocChangeShip = null;
		//OMNI-81851 START
		NodeList nlShipmentLine = eleIn.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		boolean bIsSOFShip = false;
		//OMNI-81851 END		
		try{
			strShipmentKey = eleIn.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			strStatus = eleIn.getAttribute(AcademyConstants.ATTR_STATUS);
			strStatusDate = eleIn.getAttribute(AcademyConstants.ATTR_STATUS_DATE);
			
			docOutGetCommonCodeList = AcademyBOPISUtil.getCommonCodeList(env, AcademyConstants.STR_BOPIS_CUSTOMER_PICKUP_SLA, AcademyConstants.PRIMARY_ENTERPRISE);
			
			SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			Calendar cal = Calendar.getInstance();
			dtCurrentDate = cal.getTime();
			log.verbose("Current System Date :: "+dtCurrentDate);
			
			dtShipmentStatusDate = (Date)sdf.parse(strStatusDate);
			log.verbose("Shipment StatusDate :: "+dtShipmentStatusDate);
			cal.setTime(dtShipmentStatusDate);
			if(AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS.equals(strStatus) 
					| AcademyConstants.STR_READYFORCUSTOMER_STATUS.equals(strStatus)){
				
				//Start : OMNI-8689: Pick SLA dates to be updated for STS
				//Check if Order contains any STS Lines
				//Changes for OMNI-89516---START Modified the path it should contain only STS non firearm
				NodeList nlSTSOrderLine = XPathUtil.getNodeList(inDoc, "/Shipment/ShipmentLines/ShipmentLine/OrderLine/Order/OrderLines/OrderLine[@PackListType!='FA' and @FulfillmentType='STS']");
				//Changes for OMNI-89516---END
				NodeList nlBOPISOrderLine = XPathUtil.getNodeList(inDoc, "/Shipment/ShipmentLines/ShipmentLine/OrderLine/Order/OrderLines/OrderLine[@FulfillmentType='BOPIS']");
				log.verbose(":: nlSTSOrderLine :: " + nlSTSOrderLine.getLength());
				log.verbose(":: nlBOPISOrderLine :: " + nlBOPISOrderLine.getLength());
				//Changes for OMNI-89516---START
				NodeList nlSTSFAOrderLine = XPathUtil.getNodeList(inDoc, "/Shipment/ShipmentLines/ShipmentLine/OrderLine/Order/OrderLines/OrderLine[@PackListType='FA' and @FulfillmentType='STS']");
				log.verbose(":: nlSTSFAOrderLine :: " + nlSTSFAOrderLine.getLength());
				//Changes for OMNI-89516---END
				//Start : OMNI-43224: Pick SLA dates to be updated for SOF, OMNI-93522 start
				NodeList nlSOFOrderLine = XPathUtil.getNodeList(inDoc, "/Shipment[@DeliveryMethod='SHP']/ShipmentLines/ShipmentLine/OrderLine/Order/OrderLines/OrderLine[@FulfillmentType='SOF']");
				
				//Start : OMNI-74228 : BOPIS/STS Consolidated Pickup Reminders, OMNI-93522 start
				if(nlSOFOrderLine.getLength() == 0) {
					nlSOFOrderLine = XPathUtil.getNodeList(inDoc, "/Shipment[@DeliveryMethod='SHP']/ShipmentLines/ShipmentLine/OrderLine[@FulfillmentType='SOF']");
				}
				//End : OMNI-74228 : BOPIS/STS Consolidated Pickup Reminders, OMNI-93522 end
				
				log.verbose(":: nlSOFOrderLine :: " + nlSOFOrderLine.getLength());
				//Start : OMNI-43224: Pick SLA dates to be updated for SOF
				
				//Changes for OMNI-89516---START
				if(nlSTSFAOrderLine.getLength() > 0){
					eleCommonCode = (Element) XPathUtil.getNode(docOutGetCommonCodeList, AcademyConstants.XPATH_SLA_FOR_EMAIL+AcademyConstants.STR_STS_SOF_PICKUP_SLA+"']");
					log.verbose("::nlSTSSOFOrderLine :: eleCommonCode :: " +SCXmlUtil.getString(eleCommonCode));
					strCustPickupSLADays = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
					log.verbose("::strCustPickupSLADays :: nlSTSFAOrderLine :: " +strCustPickupSLADays);
					
					//Changes to stamp same Max Cust Pickup date for order with STS FA
					String strInitialPromiseDate = XPathUtil.getString(inDoc, "/Shipment/ShipmentLines/ShipmentLine/OrderLine/Order/OrderLines/OrderLine"
							+ "[@PackListType='FA' and @FulfillmentType='STS']/Extn/@ExtnInitialPromiseDate");
					log.verbose("strInitialPromiseDate:: " + strInitialPromiseDate);
					if(YFCObject.isVoid(strInitialPromiseDate)) {
						strInitialPromiseDate = XPathUtil.getString(inDoc, "/Shipment/ShipmentLines/ShipmentLine/OrderLine/Order/OrderLines/OrderLine"
								+ "[@FulfillmentType='STS']/Extn/@ExtnInitialPromiseDate");						
					}
					log.verbose("strInitialPromiseDate::" + strInitialPromiseDate);
					if(!YFCObject.isVoid(strInitialPromiseDate)) {
						SimpleDateFormat sdfDateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
						cal.setTime(sdfDateFormat.parse(strInitialPromiseDate));
					}
					//Changes to stamp same Max Cust Pickup date for order with STS FA

					cal.add(Calendar.HOUR, Integer.parseInt(strCustPickupSLADays));
					dtMaxCustPickDate = cal.getTime();				
					strMaxCustPickDate = sdf.format(dtMaxCustPickDate);
				}
				//Changes for OMNI-89516---END
				
				//Only STS Lines on the order
				else if(nlSTSOrderLine.getLength() > 0 && nlBOPISOrderLine.getLength() == 0){
					eleCommonCode = (Element) XPathUtil.getNode(docOutGetCommonCodeList, AcademyConstants.XPATH_SLA_FOR_EMAIL+AcademyConstants.STR_STS_REGULAR_PICKUP_SLA+"']");
					strCustPickupSLADays = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
					cal.add(Calendar.HOUR, Integer.parseInt(strCustPickupSLADays));
					dtMaxCustPickDate = cal.getTime();				
					strMaxCustPickDate = sdf.format(dtMaxCustPickDate);
				}
				//Mixed Cart (BOPIS and STS) Lines on Order
				else if (nlSTSOrderLine.getLength() > 0 && nlBOPISOrderLine.getLength() > 0) {
					
					String strInitialPromiseDate = XPathUtil.getString(inDoc, "/Shipment/ShipmentLines/ShipmentLine/OrderLine/Order/OrderLines/OrderLine[@FulfillmentType='STS']/Extn/@ExtnInitialPromiseDate");
					log.verbose("strInitialPromiseDate :: " + strInitialPromiseDate);
					if(YFCObject.isVoid(strInitialPromiseDate)) {
						strInitialPromiseDate = XPathUtil.getString(inDoc, "/Shipment/ShipmentLines/ShipmentLine/OrderLine/Extn/@ExtnInitialPromiseDate");						
					}
					log.verbose("strInitialPromiseDate :: " + strInitialPromiseDate);
					if(!YFCObject.isVoid(strInitialPromiseDate)) {
						SimpleDateFormat sdfDateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
						cal.setTime(sdfDateFormat.parse(strInitialPromiseDate));
					}
					eleCommonCode = (Element) XPathUtil.getNode(docOutGetCommonCodeList,AcademyConstants.XPATH_SLA_FOR_EMAIL+AcademyConstants.STR_STS_REGULAR_PICKUP_SLA+"']");
					strCustPickupSLADays = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
					
					cal.add(Calendar.HOUR, Integer.parseInt(strCustPickupSLADays));
					dtMaxCustPickDate = cal.getTime();				
					strMaxCustPickDate = sdf.format(dtMaxCustPickDate);

				}
				//Start : OMNI-43224: Pick SLA dates to be updated for SOF
				//OMNI-74228 : BOPIS/STS Consolidated Pickup Reminders - Updating condition to have only SOF lines in the SOF flow
				else if (nlSOFOrderLine.getLength() > 0 && nlSTSOrderLine.getLength() == 0 && nlBOPISOrderLine.getLength() == 0) {
					//OMNI-81851 START
					bIsSOFShip = true;
					//OMNI-81851 END
					eleCommonCode = (Element) XPathUtil.getNode(docOutGetCommonCodeList, AcademyConstants.XPATH_SLA_FOR_EMAIL+AcademyConstants.STR_SOF_REGULAR_PICKUP_SLA+"']");
					strCustPickupSLADays = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
					cal.add(Calendar.HOUR, Integer.parseInt(strCustPickupSLADays));
					dtMaxCustPickDate = cal.getTime();				
					strMaxCustPickDate = sdf.format(dtMaxCustPickDate);
				}
				//Start : OMNI-43224: Pick SLA dates to be updated for SOF
				//Only BOPIS Lines on Order
				else {
					eleCommonCode = (Element) XPathUtil.getNode(docOutGetCommonCodeList, AcademyConstants.XPATH_SLA_FOR_EMAIL+AcademyConstants.STR_BOPIS_REGULAR_PICKUP_SLA+"']");
					strCustPickupSLADays = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
					
					cal.add(Calendar.HOUR, Integer.parseInt(strCustPickupSLADays));
					dtMaxCustPickDate = cal.getTime();				
					strMaxCustPickDate = sdf.format(dtMaxCustPickDate);
				}				
				//End : OMNI-8689: Pick SLA dates to be updated for STS
				log.verbose(":: dtMaxCustPickDate :: "+ strMaxCustPickDate);
				
				outDocChangeShip = changeShipment(env, strShipmentKey, AcademyConstants.STR_ACTION_CREATE, strMaxCustPickDate);
				
			}else if(AcademyConstants.STR_PAPER_WORK_INITIATED_STATUS.equals(strStatus)){
				eleCommonCode = (Element) XPathUtil.getNode(docOutGetCommonCodeList, AcademyConstants.XPATH_SLA_FOR_EMAIL+AcademyConstants.STR_BOPIS_FIREARMS_PICKUP_SLA+"']");
				strCustPickupSLADays = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
				
				cal.add(Calendar.HOUR, Integer.parseInt(strCustPickupSLADays));
				dtMaxCustPickDate = cal.getTime();				
				strMaxCustPickDate = sdf.format(dtMaxCustPickDate);
				
				outDocChangeShip = changeShipment(env, strShipmentKey, AcademyConstants.STR_ACTION_MODIFY, strMaxCustPickDate);
				
			}
			//Begin : OMNI-3733
			String strEnableCustomTaskQ = props.getProperty(AcademyConstants.STR_ENABLE_BOPIS_CUSTOM_SHIPMENT_MONITOR);
			if(AcademyConstants.STR_YES.equalsIgnoreCase(strEnableCustomTaskQ)) {
				log.verbose("Property - "+AcademyConstants.STR_ENABLE_BOPIS_CUSTOM_SHIPMENT_MONITOR
						+" is set to "+"'"+strEnableCustomTaskQ+"'");
				//Start : OMNI-43224: Pick SLA dates to be updated for SOF
				if(AcademyConstants.STR_READYFORCUSTOMER_STATUS.equals(strStatus))
				{
					eleCommonCode = (Element) XPathUtil.getNode(docOutGetCommonCodeList, AcademyConstants.XPATH_SLA_FOR_EMAIL+AcademyConstants.STR_SOF_PICKUP_REMINDER_SLA+"']");
				}
				else
				{
					eleCommonCode = (Element) XPathUtil.getNode(docOutGetCommonCodeList, AcademyConstants.XPATH_SLA_FOR_EMAIL+AcademyConstants.STR_BOPIS_PICKUP_REMINDER_SLA+"']");
				}
				//Start : OMNI-43224: Pick SLA dates to be updated for SOF
				
				//Start : OMNI-74228 : SOPIS/STS Consolidated Reminders
				String strReminderInterval = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
				log.verbose("Reminder Interval :: "+strReminderInterval);
				eleCommonCode = (Element) XPathUtil.getNode(docOutGetCommonCodeList, AcademyConstants.XPATH_SLA_FOR_EMAIL+AcademyConstants.STR_IS_REMINDER_CONSOLIDATION_ENABLED+"']");
				String strOrdLvlReminderConsolEnabled = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
				if(!YFCObject.isVoid(strOrdLvlReminderConsolEnabled) 
						&& AcademyConstants.ATTR_Y.equalsIgnoreCase(strOrdLvlReminderConsolEnabled)
						&& !(bIsSOFShip)){			
					log.verbose("ORder LEvel consolidation enabled for Shipment Reminders");
					String strOrderHeaderKey = XPathUtil.getString(inDoc, "/Shipment/ShipmentLines/ShipmentLine/@OrderHeaderKey");
					log.verbose(":: strOrderHeaderKey :: " + strOrderHeaderKey);
					
					String strNextReminderDate=getAvailDateForReminder(env, strStatusDate, strReminderInterval, docOutGetCommonCodeList);
					createTaskQueue(env, AcademyConstants.STR_TXN_ID_BOPIS_CUSTOM_MONITOR, strNextReminderDate, strOrderHeaderKey, AcademyConstants.ATTR_ORDER_HEADER_KEY);
					
				}
				else {
					Date objDate = sdf.parse(strStatusDate);
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(objDate);
					calendar.add(Calendar.HOUR_OF_DAY, Integer.parseInt(strReminderInterval));
					String strDateModified = sdf.format(calendar.getTime());
					
					log.verbose("Creating TASK_Q entries for custom monitor agent:: ");
					createTaskQueue(env, AcademyConstants.STR_TXN_ID_BOPIS_CUSTOM_MONITOR, strDateModified, strShipmentKey,AcademyConstants.ATTR_SHIPMENT_KEY);
				}
				//End : OMNI-74228 : SOPIS/STS Consolidated Reminders
			}
			//End : OMNI-3733
		}
		catch (Exception e){
			e.printStackTrace();		
			throw new YFSException(e.getMessage(), 
					"CHGSHP0001", "Failure occurring while Stamping ACADEMY_MAX_CUSTOMER_PICK_DATE");
		}
		
		 
		log.verbose("Exiting AcademyBOPISStampMaxCustomerPickDate.stampMaxCustPickDate()");	
		log.verbose("return doc "+XMLUtil.getXMLString(outDocChangeShip));
		return outDocChangeShip;
	}
	
	/**
	 * This method invokes changeShipment API, to stamp ACADEMY_MAX_CUSTOMER_PICK_DATE SLA Date in additional Date.
	 * @param shipmentKey
	 * @param strAction
	 * param strMaxCustPickDate
	 */
	public Document changeShipment(YFSEnvironment env, String strShipmentKey, String strAction, String strMaxCustPickDate) throws Exception {	
		log.verbose("Entering AcademyBOPISStampMaxCustomerPickDate.changeShipment() : "+strMaxCustPickDate);
		Element eleInChangeShipment = null;
		Element eleAdditionalDates = null;
		Element eleAdditionalDate = null;
		Document docInChangeShipment = null;		
		Document docOutChangeShipment = null;
		
		//Form changeShipment API input
		docInChangeShipment = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		eleInChangeShipment = docInChangeShipment.getDocumentElement();
		eleInChangeShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
		eleInChangeShipment.setAttribute(AcademyConstants.ATTR_OVERRIDE_MODIFICATION_RULES, AcademyConstants.STR_YES);
		eleAdditionalDates = docInChangeShipment.createElement(AcademyConstants.E_ADDITIONAL_DATES);
		XMLUtil.appendChild(eleInChangeShipment, eleAdditionalDates);			
		eleAdditionalDate = docInChangeShipment.createElement(AcademyConstants.E_ADDITIONAL_DATE);
		eleAdditionalDate.setAttribute(AcademyConstants.ATTR_ACTION, strAction);
		eleAdditionalDate.setAttribute(AcademyConstants.A_DATE_TYPE_ID, AcademyConstants.BOPIS_MAX_CUSTOMER_PICK_DATE_TYPE);
		eleAdditionalDate.setAttribute(AcademyConstants.ATTR_ACTUAL_DATE, strMaxCustPickDate);
		XMLUtil.appendChild(eleAdditionalDates, eleAdditionalDate);
		
		log.verbose("Input to changeShipment : "+XMLUtil.getXMLString(docInChangeShipment));
		//setting template
		env.setApiTemplate(AcademyConstants.API_CHANGE_SHIPMENT, AcademyConstants.STR_CHANGE_SHIP_ADDITIONAL_DATES_API);
		docOutChangeShipment = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, docInChangeShipment);
		env.clearApiTemplate(AcademyConstants.API_CHANGE_SHIPMENT);
		log.verbose("Output from changeShipment : "+XMLUtil.getXMLString(docOutChangeShipment));
		
		log.verbose("Exiting AcademyBOPISStampMaxCustomerPickDate.changeShipment()");
		return docOutChangeShipment;
	}
	
	//OMNI-3733
	private void createTaskQueue(YFSEnvironment objEnv, String strTxnId, String strAvailDate, String strDataKey, String strDataType) {
		try {
			
			Document docInManageTaskQueue = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
			Element eleInManageTaskQueue = docInManageTaskQueue.getDocumentElement();
			eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_AVAIL_DATE, strAvailDate);
			eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_TRANS_ID, strTxnId);
			//Start : OMNI-74228 : SOPIS/STS Consolidated Reminders
			eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_TYPE, strDataType);
			eleInManageTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_KEY, strDataKey);
			//End : OMNI-74228 : SOPIS/STS Consolidated Reminders
			log.verbose("input - manageTaskQueue  API :: " + XMLUtil.getXMLString(docInManageTaskQueue));
			AcademyUtil.invokeAPI(objEnv, AcademyConstants.API_MANAGE_TASK_QUEUE, docInManageTaskQueue);
						
		}catch(Exception e) {
			log.error("Exception caught -createTaskQueue() :: "+e);
		}
	}
	//OMNI-3733
	//Start : OMNI-74228 : SOPIS/STS Consolidated Reminders
	/**
	 * This method validates if the Reminder consolidation is to be enabled at Order Level
	 * 
	 * @return boolean
	 
	private boolean isOrderLevelReminderConsolidationEnabled()
	{
		log.verbose("Inside AcademyBOPISStampMaxCustomerPickDate isOrderLevelReminderConsolidationEnabled :: ");
		
		boolean bIsOrderLvelConsolidationEnabled = false;
		String strEnableConsolidation = props.getProperty(AcademyConstants.STR_IS_REMINDER_CONSOLIDATION_ENABLED);
		if(AcademyConstants.STR_YES.equals(strEnableConsolidation))	{
			log.verbose(" Order LEvel Consolidation Is enabled");
			return true;
		}
		log.verbose(" bIsOrderLvelConsolidationEnabled :: "+bIsOrderLvelConsolidationEnabled);
		return bIsOrderLvelConsolidationEnabled;
	}*/
	

	/**
	 * This method calcualtes the next eligible reminder time for STS/BOPIS reminders at ORder level 
	 * 
	 * @param env
	 * @param strStatusDate
	 * @param strReminderInterval
	 * @return boolean
	 * @throws Exception
	 */
	private String getAvailDateForReminder(YFSEnvironment env,String strStatusDate,String strReminderInterval,Document docOutGetCommonCodeList) throws Exception
	{
		log.verbose("Inside AcademyBOPISStampMaxCustomerPickDate getAvailDateForReminder :: ");

		Element eleCommonCode = null;
		String strDateModified=null;
		String strCutOffTime = null;
	/**	Document docOutGetCommonCodeList = AcademyBOPISUtil.getCommonCodeList(env, AcademyConstants.STR_REMINDER_CUTOFF, AcademyConstants.PRIMARY_ENTERPRISE);
		NodeList nlCommonCode = docOutGetCommonCodeList.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);
		if(nlCommonCode.getLength()>0)
		{
			Element eleCommonCode = (Element)nlCommonCode.item(0);
			strCutOffTime = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
			
			log.verbose("strCutOffTime--->"+strCutOffTime);
		}**/
		eleCommonCode = (Element) XPathUtil.getNode(docOutGetCommonCodeList, AcademyConstants.XPATH_SLA_FOR_EMAIL+AcademyConstants.STR_REMINDER_CUTOFF+"']");
		strCutOffTime = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
				
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
		
		Date objDate = sdf.parse(strStatusDate);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(objDate);
		calendar.add(Calendar.HOUR_OF_DAY, Integer.parseInt(strReminderInterval));
		log.verbose("Date after adding reminder interval--->"+calendar.getTime());
		if(!YFCObject.isVoid(strCutOffTime) && strCutOffTime.contains(":")) {
			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(strCutOffTime.split(":")[0]));
			calendar.set(Calendar.MINUTE, Integer.parseInt(strCutOffTime.split(":")[1]));
		}
		
		strDateModified = sdf.format(calendar.getTime());
		log.verbose("strDateModified--->"+strDateModified);
		return strDateModified;
	}
	//End : OMNI-74228 : SOPIS/STS Consolidated Reminders
}
