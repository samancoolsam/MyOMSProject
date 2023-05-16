package com.academy.ecommerce.sterling.fraud.kount;

/*##################################################################################
 *
 * Project Name                : Kount Integration
 * Module                      : OMS
 * Author                      : CTS
 * Date                        : 8-MAY-2019 
 * Description				   : This agent is used to Fetch the list of orders of 
 * 								 Fraud_Review_Kount and Fraud_Escalate_Kount hold 
 * 								 types that are for stuck more than 24 hours and call
 *                               the Kount system to update the Fraud status in OMS.
 * ---------------------------------------------------------------------------------
 * Date            Author         		Version#       Remarks/Description                      
 * ---------------------------------------------------------------------------------
 * 8-MAY-2019		CTS  	 			  1.0           	Initial version
 * 23-MAY-2019		CTS  	 			  1.1           	Updated to code to handle multiple order holds
 * ##################################################################################*/


import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.ibm.icu.text.SimpleDateFormat;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;


public class AcademyGetFraudStatusFromKount extends YCPBaseAgent{
	/* This method fetches orders of Fraud_Review_Kount and Fraud_Escalate_Kount hold types that are stuck more than 24 hours
	 * 
	 * 
	 * 
	 */
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyGetFraudStatusFromKount.class);    

	public List<Document> getJobs(YFSEnvironment env, Document inXML, Document docLastMessage) throws Exception {

		log.verbose("Begin of AcademyGetFraudStatusFromKount.getJobs() method");
		List<Document> lOrders = new ArrayList<Document>();
		Document docGetOrderListOut = null;
		Element eleCurrentOrder = null;
		String strLastOrderHeaderKey = null;

		String strNoOfProcessingHours = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_NO_OF_PROCESSING_HOURS);
		String strNoOfHoursForEmail = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_NO_OF_HOURS_FOR_EMAIL);
		String strMaximumRecords = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_NUM_RECORDS);
		String strNoOfFromProcessingHours = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_NO_OF_FROM_PROCESSING_HOURS);
		log.verbose("Input Message to the AcademyGetFraudStatusFromKount.getJobs()"+SCXmlUtil.getString(inXML));

		if (!YFCObject.isVoid(docLastMessage)) {
			log.verbose("LastMessage is present for AcademyGetFraudStatusFromKount"+XMLUtil.getXMLString(docLastMessage));
			strLastOrderHeaderKey = docLastMessage.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
		}

		// calling getOrderList
		docGetOrderListOut = callGetOrderList(env, inXML, strLastOrderHeaderKey, strNoOfProcessingHours, strNoOfFromProcessingHours,strMaximumRecords);
		log.verbose(" getOrderList output :: "+XMLUtil.getXMLString(docGetOrderListOut));

		NodeList nlOrderList = docGetOrderListOut.getElementsByTagName(AcademyConstants.ELE_ORDER);
		int iGetOrderListCount = nlOrderList.getLength();
		log.verbose("nlOrderList_Length:"+iGetOrderListCount);

		for (int iOrderCount = 0; iOrderCount < iGetOrderListCount; iOrderCount++) {
			eleCurrentOrder = (Element) nlOrderList.item(iOrderCount);	
			eleCurrentOrder.setAttribute(AcademyConstants.ATTR_NO_OF_HOURS_FOR_EMAIL, strNoOfHoursForEmail);
			Document docOrder = XMLUtil.getDocumentForElement(eleCurrentOrder);
			Element eleOrderHoldTypes = XMLUtil.getElementByXPath(docOrder, AcademyConstants.XPATH_ORDER_HOLD_TYPES);
			List lOrderHoldType = XMLUtil.getElementListByXpath(eleOrderHoldTypes.getOwnerDocument(),"Order/OrderHoldTypes/OrderHoldType");
			
			for (int iHoldType = 0; iHoldType < lOrderHoldType.size(); iHoldType++) {
				Element eleOrderHoldType = (Element) lOrderHoldType.get(iHoldType);
				String strHoldStatus = eleOrderHoldType.getAttribute(AcademyConstants.ATTR_STATUS);
				String strHoldType = eleOrderHoldType.getAttribute(AcademyConstants.ATTR_HOLD_TYPE);
				
				if(AcademyConstants.STR_HOLD_CREATED_STATUS.equals(strHoldStatus) && 
						(AcademyConstants.STR_FRAUD_REVIEW_KOUNT.equals(strHoldType) || 
							AcademyConstants.STR_FRAUD_ESCALATE_KOUNT.equals(strHoldType))){
					
				} else {
					eleOrderHoldTypes.removeChild(eleOrderHoldType);
				}				
			}
			lOrders.add(docOrder);
		}
		
		log.verbose("End of AcademyGetFraudStatusFromKount.getJobs() method");
		return lOrders;     

	}
	/** This method executes getOrderList API to get the orders of Fraud_Review_Kount and Fraud_Escalate_Kount hold types that are stuck more than 24 hours
	 * 
	 */
	private Document callGetOrderList(YFSEnvironment env, Document inXML, String strLastOrderHeaderKey, 
			String strNoOfProcessingHours,String strNoOfFromProcessingHours, String strMaximumRecords) throws Exception {
		log.verbose("Begin of AcademyGetFraudStatusFromKount.callGetOrderList() method");
		Document docgetOrderListOutput = null;

		String strLastHoldTypeDate = getDatefromCalendar(strNoOfProcessingHours);
		String strFromLastHoldTypeDate = getDatefromCalendar(strNoOfFromProcessingHours);
		log.verbose("From Date is "+strFromLastHoldTypeDate+"And"+" To Date is "+ strLastHoldTypeDate);		

		Document docgetOrderListInput = XMLUtil
		.getDocument(
				"<Order MaximumRecords='" + strMaximumRecords + "' >"+
				"<OrderHoldType Status='1100' LastHoldTypeDateQryType='BETWEEN' FromLastHoldTypeDate='"+ strFromLastHoldTypeDate+"' " +
				"ToLastHoldTypeDate='"+strLastHoldTypeDate+"'>"+
				"<ComplexQuery Operator='AND'>"+
				"<Or>"+
				"<Exp Name='HoldType' Value='FRAUD_REVIEW_KOUNT' HoldTypeQryType='EQ'/>"+
				"<Exp Name='HoldType' Value='FRAUD_ESCALATE_KOUNT' HoldTypeQryType='EQ'/>"+
				"</Or></ComplexQuery></OrderHoldType>" +
				"<OrderBy>" +
				"<Attribute Name='OrderHeaderKey' Desc='N'/>"+
				"</OrderBy>" +
				"</Order>");

		if(!YFCObject.isVoid(strLastOrderHeaderKey)) {
			docgetOrderListInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strLastOrderHeaderKey);
			docgetOrderListInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY +
					AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.GT_QRY_TYPE);
		}

		Document docgetOrderListTemplate = XMLUtil.getDocument("<OrderList><Order OrderNo = '' OrderHeaderKey=''> "+
				"<Extn ExtnTransactionID='' ExtnEventTime=''/>"+
				"<OrderHoldTypes>"+
				"<OrderHoldType HoldType='' LastHoldTypeDate='' Status =''/>"+
		"</OrderHoldTypes></Order></OrderList>");

		env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, docgetOrderListTemplate);
		docgetOrderListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, docgetOrderListInput);
		env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);

		log.verbose("output of  getOrderListOutput XML  : " + XMLUtil.getXMLString(docgetOrderListOutput));
		log.verbose("End of AcademyGetFraudStatusFromKount.callGetOrderList() method");
		return docgetOrderListOutput;
	}

	public String getDatefromCalendar(String strNoOfHours) {
		Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
        cal.add(Calendar.HOUR, -Integer.parseInt(strNoOfHours));
        return dateFormat.format(cal.getTime());
		
	}
	/* This method fetches orders of Fraud_Review_Kount and Fraud_Escalate_Kount hold types that are stuck more than 72 hours and publish Email alert to Support team
	 * (non-Javadoc)
	 * @see com.yantra.ycp.japi.util.YCPBaseAgent#executeJob(com.yantra.yfs.japi.YFSEnvironment, org.w3c.dom.Document)
	 * This method Call Order Details Kount Web Service
	 */
	public void executeJob(YFSEnvironment env, Document inXML) throws Exception {
		log.verbose("Begin of AcademyGetFraudStatusFromKount.executeJob() method");

		log.verbose(" inXML ::" + XMLUtil.getXMLString(inXML));
		String strHoldType = null;

		String strExtnTransactionId = XMLUtil.getAttributeFromXPath(inXML, AcademyConstants.XPATH_ATTR_ORDER_EXTN_TRANSACTIONID);

		Element eleOrder = inXML.getDocumentElement();
		String strOrderNo = eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_NO);

		Element eleHoldType =  XMLUtil.getElementByXPath(inXML, AcademyConstants.XPATH_ENS_CREATED_HOLD_STATUS);

		strHoldType = eleHoldType.getAttribute(AcademyConstants.ATTR_HOLD_TYPE);
		log.verbose("HoldType ::" + strHoldType);
		if (strHoldType.equals(AcademyConstants.STR_FRAUD_REVIEW_KOUNT) || strHoldType.equals(AcademyConstants.STR_FRAUD_ESCALATE_KOUNT)) {

			String strLastHoldTypeDate = eleHoldType.getAttribute(AcademyConstants.ATTR_LAST_HOLD_TYPE_DATE);
			log.verbose("LastHoldTypeDate ::" + strLastHoldTypeDate);

			SimpleDateFormat sdfSterlingFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			Date dLastHolddate = sdfSterlingFormat.parse(strLastHoldTypeDate);

			Date dCurrentDate = new Date();
			SimpleDateFormat sfdSystem = new SimpleDateFormat(AcademyConstants.STR_DATETIME_PATTERN);
			String strSysDate = sfdSystem.format(dCurrentDate);
			log.verbose("strSysDate ::" +strSysDate);
			log.verbose("strLastHoldDate :: " + strLastHoldTypeDate);

			long dateDiff = (dCurrentDate.getTime() - dLastHolddate.getTime())/ (60 * 60 * 1000);
			log.verbose("dateDiff :: " + dateDiff);
			String strNoOfHoursForEmail = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_NO_OF_HOURS_FOR_EMAIL);
			int iNoOfHoursForEmail = 0;
			if(!YFCObject.isVoid(strNoOfHoursForEmail)){
				iNoOfHoursForEmail = Integer.parseInt(strNoOfHoursForEmail);
			}

			if (dateDiff > iNoOfHoursForEmail) {
				String strNotify = "The Order " + strOrderNo+ "is being in hold more than " + strNoOfHoursForEmail +" hours";
				AcademyUtil.invokeService(env,AcademyConstants.SERVICE_ACADEMY_SEND_EMAIL_ON_DELAYED_KOUNT_HOLDS, inXML);
				log.verbose(strNotify);
			}

		}

		if(!YFCObject.isVoid(strExtnTransactionId)){

			String strKountInput = "trid="+strExtnTransactionId+"";
			Document outputDoc = AcademyInvokeKountWebService.invokeWebservice(strKountInput, AcademyConstants.STR_KOUNT_URL_DETAIL);

			log.verbose("Response from Webservice is" + SCXmlUtil.getString(outputDoc));
			/*Expected OutPut from WebService call
			 * <jsonObject status="ok">
		    		<result auth_status="" score="34" status="D"/>
		    		<count failure="0" success="1"/>
		   	  </jsonObject>*/
			if (!YFCObject.isVoid(outputDoc)) {
				String strStatus = XMLUtil.getAttributeFromXPath(outputDoc, AcademyConstants.XPATH_FRAUD_STATUS);

				if (!YFCObject.isVoid(strStatus)) {
						boolean callchangeOrder = false;
						boolean callCancelService = false;
						
					if (strStatus.equals(AcademyConstants.STR_KOUNT_STATUS_A)) {

						eleOrder.setAttribute(AcademyConstants.ATTR_ACTION,AcademyConstants.STR_ACTION_MODIFY_UPPR);
						eleHoldType.setAttribute(AcademyConstants.ATTR_STATUS,AcademyConstants.STR_HOLD_RESOLVED_STATUS);
						
						callchangeOrder = true;

					} else if (strStatus.equals(AcademyConstants.STR_KOUNT_STATUS_D)) {

						eleOrder.setAttribute(AcademyConstants.ATTR_ACTION,AcademyConstants.STR_CANCEL);
						eleHoldType.setAttribute(AcademyConstants.ATTR_STATUS,AcademyConstants.STR_HOLD_RESOLVED_STATUS);

						eleOrder.setAttribute(AcademyConstants.ATTR_REASON_CODE,AcademyConstants.STR_KOUNT_DENY);
						eleOrder.setAttribute(AcademyConstants.STR_CANCELLED_BY, AcademyConstants.STR_KOUNT);
						eleOrder.setAttribute(AcademyConstants.STR_CANCELLING_SYSTEM,AcademyConstants.STR_KOUNT);
						eleOrder.setAttribute(AcademyConstants.ATTR_DOC_TYPE,AcademyConstants.SALES_DOCUMENT_TYPE);
						
						callCancelService = true;

					} else if (strStatus.equals(AcademyConstants.STR_KOUNT_STATUS_R)) {

						eleOrder.setAttribute(AcademyConstants.ATTR_ACTION,AcademyConstants.STR_ACTION_MODIFY_UPPR);
						String strHold = eleHoldType.getAttribute(AcademyConstants.ATTR_HOLD_TYPE);
						if (strHold.equals(AcademyConstants.STR_FRAUD_ESCALATE_KOUNT)) {

							eleHoldType.setAttribute(AcademyConstants.ATTR_STATUS,AcademyConstants.STR_HOLD_RESOLVED_STATUS);
							Element eleOrderHoldTypes = XMLUtil.getElementByXPath(inXML, AcademyConstants.XPATH_ORDER_HOLD_TYPES);
							
							Element newOrderHoldType = SCXmlUtil.createChild(eleOrderHoldTypes,AcademyConstants.ELE_ORDER_HOLD_TYPE);
							newOrderHoldType.setAttribute(AcademyConstants.ATTR_HOLD_TYPE,AcademyConstants.STR_FRAUD_REVIEW_KOUNT);
							newOrderHoldType.setAttribute(AcademyConstants.ATTR_STATUS,AcademyConstants.STR_HOLD_CREATED_STATUS);
							callchangeOrder = true;

						}else{
							log.verbose("No Action Performed on this Order "+strOrderNo+". The Status from Kount is same");
						}
					}else if(strStatus.equals(AcademyConstants.STR_KOUNT_STATUS_E)){
							eleOrder.setAttribute(AcademyConstants.ATTR_ACTION,AcademyConstants.STR_ACTION_MODIFY_UPPR);
							String strOrdHold = eleHoldType.getAttribute(AcademyConstants.ATTR_HOLD_TYPE);
						if (strOrdHold.equals(AcademyConstants.STR_FRAUD_REVIEW_KOUNT)) {
							
							eleHoldType.setAttribute(AcademyConstants.ATTR_STATUS,AcademyConstants.STR_HOLD_RESOLVED_STATUS);
							Element eleOrderHoldTypes = XMLUtil.getElementByXPath(inXML, AcademyConstants.XPATH_ORDER_HOLD_TYPES);
							
							Element newOrderHoldType = SCXmlUtil.createChild(eleOrderHoldTypes,AcademyConstants.ELE_ORDER_HOLD_TYPE);
							newOrderHoldType.setAttribute(AcademyConstants.ATTR_HOLD_TYPE,AcademyConstants.STR_FRAUD_ESCALATE_KOUNT);
							newOrderHoldType.setAttribute(AcademyConstants.ATTR_STATUS,AcademyConstants.STR_HOLD_CREATED_STATUS);
							callchangeOrder = true;
						}else{
							log.verbose("No Action Performed on this Order"+strOrderNo+". The Status from Kount is same");
						}
					}
					if(callchangeOrder){
						log.verbose("CallChangeOrder API Input: =" + SCXmlUtil.getString(inXML));
						AcademyUtil.invokeAPI(env,AcademyConstants.API_CHANGE_ORDER, inXML);
						
					}
					if(callCancelService){
						log.verbose("CancelOrderService API Input: =" + SCXmlUtil.getString(inXML));
						AcademyUtil.invokeService(env,AcademyConstants.SERVICE_ACADEMY_KOUNT_CANCEL_SERVICE, inXML);
						
					}
				} else {
					log.verbose("No Action Performed on this Order"+strOrderNo+".No Response from Webservice invocation");
				}
			}
		}else{
			log.verbose("The Transaction ID is blank for this Order"+strOrderNo+".");
		}

		log.verbose("End of AcademyGetFraudStatusFromKount.executeJob() method");
		

	}

}

