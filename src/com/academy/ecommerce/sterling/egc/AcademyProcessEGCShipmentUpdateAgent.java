package com.academy.ecommerce.sterling.egc;

/**#########################################################################################
 *
 * Project Name                : EGC
 * Module                      : OMNI-13739, OMNI-5004, OMNI-5006, OMNI-5008
 * Author                      : C0023461, C0015568, C0015606
 * Author Group				   : CTS - POD
 * Date                        : 11-NOV-2020 
 * Description				   : This class process the TaskQ and updates the below and removes TaskQ record
 * 								 Normal orders - Remove task queue entry
 * 								 Fraud Hold Resolved Orders -  Updates EGC Fraud status to WCS via Rest WS call and then remove task queue entry
 * 								 Appeasement/Refund Orders - Invoke WCS WS and gets First data Cart ID and then remove task queue entry
 * 								 For Fraud orders, if WCS is down, updates the TaskQ Available date
 * Notes 					   : 1. Agent "AcademyEGCFraudAppeaseShipmentServer" will be running as part of startSterlingServer script - Criteria ID = "ACAD_EGC_FRAUD_APPEASE_SHIP"
	 								This agent will process Fraud updates to WCS and Appeasement/Refund Orders
	 
	 							 2.	If there are issues with First Data (First data is down), then set the value of the Criteria Parameter IsFDDown = "Y"
	 								Restart the agent "AcademyEGCFraudAppeaseShipmentServer"
	 								Agent will then process only Fraud Status Updates to WCS until the issue is resolved and the criteria parameter is updated to "N" and agent is restarted
	  
								 3.	If there are issues with WCS (WCS Down), then set the value of the Criteria Parameter IsWCSDown = "Y"
									Restart the agent "AcademyEGCFraudAppeaseShipmentServer
									Agent will not process EGC Appeasement/Refund and Fraud Orders until the issue is resolved and the criteria parameter is updated to "N" and agent is restarted
								
								 4. Agent does not do confirmShipment as part of execute task
									 
 * ---------------------------------------------------------------------------------
 * Date            	Author         		Version#       		Remarks/Description                      
 * ---------------------------------------------------------------------------------
 * 11-NOV-2020		CTS  	 			 1.0           		Initial version
 * 26-DEC-2022		CTS					 2.0 				Revised Version (Decommission
 * 															CARTID for Appease/Refund)
 *
 * #########################################################################################*/

import org.apache.commons.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.yantra.ycp.japi.util.YCPBaseTaskAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AcademyProcessEGCShipmentUpdateAgent extends YCPBaseTaskAgent {

	String strStatusCode = "";
	String strSenderFirstName = null;
	String strSenderLastName = null;
	String strSenderEmailID = null;
	String strIsFDDown = null;
	String strIsWCSDown = null;
	String strTaskQueueKey = null;
	String strTaskQAvailableDate = null;
	String strOrderHeaderKey = null;
	String strRetryIntervelSLAMinutes = null;

	private static final YFCLogCategory logger = YFCLogCategory.instance(AcademyProcessEGCShipmentUpdateAgent.class);

	/** docInput:
	 * 
	 * <TaskQueue AvailableDate="2020-11-26T22:21:46-06:00"
		    Createprogid="SterlingHttpTester"
		    Createts="2020-11-26T22:21:46-06:00" Createuserid="C0015568"
		    DataKey="20201126222140177146458" DataType="OrderHeaderKey"
		    HoldFlag="N" Lockid="1" Modifyprogid="Console"
		    Modifyts="2020-11-26T22:27:50-06:00" Modifyuserid="C0015568"
		    TaskQKey="20201126222146177146492" TransactionKey="20201103045242172777702">
		    <TransactionFilters Action="Get"
		        AppeasementSenderEmailID="appeasement@academy.com"
		        AppeasementSenderFirstName="Academy Sports + "
		        AppeasementSenderLastName="Outdoors" DocumentParamsKey="0001"
		        DocumentType="0001" IsFDDown="N" IsWCSDown="N"
		        NumRecordsToBuffer="5000" ProcessType="ORDER_FULFILLMENT"
		        ProcessTypeKey="ORDER_FULFILLMENT"
		        RefundSenderEmailID="refund@academy.com"
		        RefundSenderFirstName="Academy Sports + "
		        RefundSenderLastName="Outdoors" RetryIntervalSLAMinutes="15"
		        StatusCode="Success"
		        TransactionId="ACAD_EGC_CONFIRM_SHP.0001.ex" TransactionKey="20201103045242172777702"/>
		</TaskQueue>
	 */

	/**
	 * Agent "AcademyEGCFraudAppeaseShipmentServer" will be running  a part of startSterlingServer script
	 * Agent will process Normal Orders, Fraud updates to WCS and Appeasement/Refund Orders
	 * 
	 * If there are issues with First Data (First data is down), then set the value of the Criteria Parameter IsFDDown = "Y"
	 * Restart the agent "AcademyEGCFraudAppeaseShipmentServer"
	 * Agent will process only Fraud Update Status to WCS until the issue is resolved and the criteria parameter is updated to "N"
	 * 
	 * If there are issues with WCS (WCS Down), then  then set the value of the Criteria Parameter IsWCSDown = "Y"
	 * Restart the agents "AcademyEGCFraudAppeaseShipmentServer
	 * Agent will only not process EGC Appeasement/Refund and Fraud Orders until the issue is resolved and the criteria parameter is updated to "N"
	 * 
	 **/

	@Override
	public Document executeTask(YFSEnvironment env, Document docInput) throws Exception {

		logger.beginTimer("AcademyProcessEGCShipmentUpdateAgent.executeTask()");
		logger.debug("Input to AcademyProcessEGCShipmentUpdateAgent.executeTask() :: " + XMLUtil.getXMLString(docInput));

		try {
			Document docGetOrderListOut = null;			
			String strWSResponse = "";
			String strAction = "";
			String strOrderPickedFor = null;
			String strModifyProgId = null;
			Element eleOrder = null;
			Element eleTransactionFilters = null;

			//Fetch the TaskQueue record attribute values
			strOrderHeaderKey = docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_DATA_KEY);
			strTaskQueueKey = docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_TASK_Q_KEY);
			strTaskQAvailableDate = docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_AVAIL_DATE);
			strModifyProgId = docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_MODIFYPROGID);

			//Fetch the Agent criteria parameters
			eleTransactionFilters = (Element) docInput.getElementsByTagName(AcademyConstants.ATTR_TRANSACTION_FILTERS).item(0);
			strRetryIntervelSLAMinutes = eleTransactionFilters.getAttribute(AcademyConstants.STR_RETRY_INTERVEL_SLA_MINTS);
			strStatusCode = eleTransactionFilters.getAttribute(AcademyConstants.ATTR_STATUS_CODE);
			strIsFDDown = eleTransactionFilters.getAttribute(AcademyConstants.STR_IS_FD_DOWN);
			strIsWCSDown = eleTransactionFilters.getAttribute(AcademyConstants.STR_IS_WCS_DOWN);

			//Call getOrderList to get the order details
			docGetOrderListOut = prepareAndInvokeGetOrderList(env, strOrderHeaderKey);

			if (!YFCObject.isVoid(docGetOrderListOut)) {

				Element eleOrderListOut = docGetOrderListOut.getDocumentElement();
				eleOrder = (Element) eleOrderListOut.getElementsByTagName(AcademyConstants.ELE_ORDER).item(0);

				//OrderType will be "APPEASEMENT" for Appeasement Orders
				String strOrderType = eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_TYPE);

				//OrderPurpose will be "REFUND" for Refund orders
				String strOrderPurpose = eleOrder.getAttribute(AcademyConstants.STR_ORDER_PURPOSE);

				NodeList nlEGCNodeList = XPathUtil.getNodeList(docGetOrderListOut, "/OrderList/Order/OrderLines/OrderLine[@FulfillmentType='EGC']");

				//Only when there are EGC lines
				if (nlEGCNodeList.getLength() > 0) {
					
					String strMaxOrderStatus = XMLUtil.getAttributeFromXPath(docGetOrderListOut, AcademyConstants.XPATH_MAX_ORDER_STATUS);
					logger.verbose("executeTask() - strMaxOrderStatus = "+ strMaxOrderStatus);
					
					String strFraudResHoldType = XMLUtil.getAttributeFromXPath(docGetOrderListOut, AcademyConstants.XPATH_FRAUD_CHECK_KOUNT);
					
					logger.verbose("executeTask() - Check if order is Fraud Order or Appeasement/Refund Order or Normal Order");
					//Checking the Order for HappyPath/Fraud/Refund/Appeasement.
					if ((!YFCCommon.isVoid(strOrderType) && strOrderType.equals(AcademyConstants.STR_APPEASEMENT))
							|| (!YFCCommon.isVoid(strOrderPurpose) && strOrderPurpose.equals(AcademyConstants.STR_REFUND))) {

						strOrderPickedFor = AcademyConstants.STR_APPEASE_REFUND;
						logger.verbose("Order is picked by agent for " + strOrderPickedFor);
						//OMNI-38272 Preventing Cancelled Appesement Orders from Reaching WCS
						if ((!YFCCommon.isVoid(strMaxOrderStatus)) && (strMaxOrderStatus.equals(AcademyConstants.VAL_CANCELLED_STATUS))) {
							
							//Remove taskQ entry
							logger.verbose("Order is in Cancelled status. Removing the task Q entry");
							removeTaskQueueRecord(env, strTaskQueueKey);
								return docInput;
							
						}

					} else if (!YFCCommon.isVoid(strFraudResHoldType) &&
							(AcademyConstants.STR_FRAUD_REVIEW_KOUNT.equalsIgnoreCase(strFraudResHoldType) 
							|| AcademyConstants.STR_FRAUD_ESCALATE_KOUNT.equalsIgnoreCase(strFraudResHoldType))) {						
						// Order with fraud hold resolved
						logger.verbose("executeTask(); - strFraudResolvedHoldTypes = " + strFraudResHoldType);
						strOrderPickedFor = AcademyConstants.STR_FRAUD;
						logger.verbose("Order is picked by agent for " + strOrderPickedFor);

					} else {
						// Normal Orders
						strOrderPickedFor = AcademyConstants.STR_HAPPY_PATH;
						logger.verbose("Order is picked by agent for " + strOrderPickedFor);
											
						// Remove taskQ entry if Order is a Normal Order
						removeTaskQueueRecord(env, strTaskQueueKey);
						return docInput;
					}

					if ((strOrderPickedFor.equals(AcademyConstants.STR_APPEASE_REFUND)
							&& (strIsFDDown.equalsIgnoreCase(AcademyConstants.STR_YES)
									|| strIsWCSDown.equalsIgnoreCase(AcademyConstants.STR_YES)))
							|| (strOrderPickedFor.equals(AcademyConstants.STR_FRAUD)
									&& strIsWCSDown.equalsIgnoreCase(AcademyConstants.STR_YES))) {

						logger.verbose("Is WCS Down? " + strIsWCSDown + " Is FirstData Down? " + strIsFDDown + " Updating TaskQ - " +strTaskQueueKey+ " - avalilable date for retry.\"");
						manageTaskQueueRecord(env, strRetryIntervelSLAMinutes, strTaskQueueKey, strTaskQAvailableDate);

					} 
					//For Fraud Scenarios 
					else if (strOrderPickedFor.equals(AcademyConstants.STR_FRAUD)) {

						logger.verbose("executeTask() - Inside Fraud Update to WCS Flow :: Begin");


						if ((!YFCObject.isVoid(strMaxOrderStatus)) && (strMaxOrderStatus.equals(AcademyConstants.VAL_CANCELLED_STATUS))) {								
							logger.verbose("strModifyProgId :: " + strModifyProgId);
							//If order cancelled via ENS Update Action is 'DECLINE'.
							//Else if order cancelled by business from Console with 'Fraud' Reason Code then Action is 'DECLINE' else 'ACCEPT'.

							if(!YFCObject.isVoid(strModifyProgId) && strModifyProgId.equalsIgnoreCase(AcademyConstants.STR_CONSOLE)) {

								logger.verbose("Order cancelled by Business from Console, ModifyProgId :: " +strModifyProgId);
								Document docgetOrderAuditListInDoc = null;
								Document docgetOrderAuditListOutDoc = null;
								String strReasonCode = null;

								docgetOrderAuditListInDoc = XMLUtil.getDocument("<OrderAudit OrderHeaderKey='"+strOrderHeaderKey+"' EnterpriseCode='Academy_Direct'/>");
								logger.verbose("getOrderAuditList API inDoc: \n" + SCXmlUtil.getString(docgetOrderAuditListInDoc));

								Document docgetOrderAuditListTemplate = XMLUtil.getDocument(
										"<OrderAuditList LastOrderAuditKey=\"\" LastRecordSet=\"\" ReadFromHistory=\"\" TotalOrderAuditList=\"\">" + 
												"    <OrderAudit OrderAuditKey=\"\" OrderHeaderKey=\"\" ReasonCode=\"\" ReasonText=\"\">" + 
												"        <OrderAuditLevels>" + 
												"            <OrderAuditLevel ModificationLevel=\"\" />" + 
												"        </OrderAuditLevels>" + 
												"    </OrderAudit>" + 
										"</OrderAuditList>");
								//Invoking getOrderAuditList API
								env.setApiTemplate(AcademyConstants.API_GET_ORDER_AUDIT_LIST, docgetOrderAuditListTemplate);
								docgetOrderAuditListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_AUDIT_LIST, docgetOrderAuditListInDoc);
								env.clearApiTemplate(AcademyConstants.API_GET_ORDER_AUDIT_LIST);

								logger.verbose("getOrderAuditList API ouDoc: \n" + SCXmlUtil.getString(docgetOrderAuditListOutDoc));

								if(!YFCObject.isVoid(docgetOrderAuditListOutDoc)) {

									strReasonCode = XMLUtil.getAttributeFromXPath(docgetOrderAuditListOutDoc, AcademyConstants.XPATH_ATTR_CONSOLE_REASONCODE);
									logger.verbose("Cancellation ReasonCode: "+strReasonCode);

									if(!YFCObject.isVoid(strReasonCode) && strReasonCode.equals(AcademyConstants.STR_FRAUD)) {
										strAction = "DECLINE";
									}else {
										strAction = "ACCEPT";
									}
								}																	
							} else {
								logger.info("Order got cancelled via ENS update.");
								strAction = "DECLINE";
							}																
						} else {
							strAction = "ACCEPT";
						}

						// Prepare WS input and Invoke same on Fraud hold resolve
						logger.verbose("Invoking Web Service with Action : " + strAction);
						strWSResponse = invokeWCSWebserviceAndUpdateFraudStatus(eleOrder, strAction);

						if (!YFCObject.isVoid(strWSResponse)) {
							if (strWSResponse.equals(AcademyConstants.VALUE_SUCCESSS)) {

								if ((!YFCObject.isVoid(strMaxOrderStatus)) && (!strMaxOrderStatus.equals(AcademyConstants.VAL_CANCELLED_STATUS))) {

									//Remove taskQ entry																	
									removeTaskQueueRecord(env, strTaskQueueKey);

								} else {
									//Remove taskQ entry if WS Response is Success and order cancelled.
									removeTaskQueueRecord(env, strTaskQueueKey);
								}									
							} else if (strWSResponse.equals(AcademyConstants.VALUE_FAILURE)) {

								logger.info("AcademyProcessEGCShipmentUpdateAgent.execute() Fraud Update Webservice Call failed :: " + strWSResponse);
								logger.verbose("Updating TaskQ - " +strTaskQueueKey+ " - avalilable date for retry.");
								//Updating TaskQ entry with next available date
								manageTaskQueueRecord(env, strRetryIntervelSLAMinutes, strTaskQueueKey, strTaskQAvailableDate);
							}
						}	
						else {
							logger.info("Exception occurred in AcademyProcessEGCShipmentUpdateAgent.execute() Fraud Update Webservice Call");
							logger.verbose("Updating TaskQ - " +strTaskQueueKey+ " - avalilable date for retry.");
							manageTaskQueueRecord(env, strRetryIntervelSLAMinutes, strTaskQueueKey, strTaskQAvailableDate);								
						}

						logger.verbose("executeTask() - Inside Fraud Update to WCS Flow :: End");
					}

					else if (strOrderPickedFor.equals(AcademyConstants.STR_APPEASE_REFUND)) {

						logger.verbose("executeTask() - Inside Appeasement/Refund Flow :: Begin");

						//Invoke Web-service exposed by WCS to get the Cart ID for EGC Order
						//Appeasement Order
						if (AcademyConstants.STR_APPEASEMENT.equals(strOrderType)) {
							logger.verbose("executeTask() - OrderType : " + strOrderType);
							strSenderFirstName = eleTransactionFilters.getAttribute(AcademyConstants.STR_APPEASEMENT_FNAME);
							strSenderLastName = eleTransactionFilters.getAttribute(AcademyConstants.STR_APPEASEMENT_LNAME);
							strSenderEmailID = eleTransactionFilters.getAttribute(AcademyConstants.STR_APPEASEMENT_EMAIL);
						}
						//Refund Order
						else if (AcademyConstants.STR_REFUND.equals(strOrderPurpose)) {
							logger.verbose("executeTask() - OrderPurpose : " + strOrderPurpose);
							strSenderFirstName = eleTransactionFilters.getAttribute(AcademyConstants.STR_REFUND_FNAME);
							strSenderLastName = eleTransactionFilters.getAttribute(AcademyConstants.STR_REFUND_LNAME);
							strSenderEmailID = eleTransactionFilters.getAttribute(AcademyConstants.STR_REFUND_EMAIL);
						}

						logger.verbose("Invoke WCS Webserviceto update Order Details");
						String strOrderNo = invokeWCSWebserviceAndGetCartId(eleOrder);

						if (!YFCCommon.isVoid(strOrderNo)) {

							logger.verbose("Order Details updated to WCS Successfully for EGC Order : " + strOrderNo);
							
							// Remove taskQ entry if WS Response is Success
							removeTaskQueueRecord(env, strTaskQueueKey);

						} else {
							logger.info("AcademyProcessEGCShipmentUpdateAgent.execute() Webservice call to get CartId failed");
							logger.verbose("Updating TaskQ - " +strTaskQueueKey+ " - avalilable date for retry.");
							manageTaskQueueRecord(env, strRetryIntervelSLAMinutes, strTaskQueueKey, strTaskQAvailableDate);
						}
						logger.verbose("executeTask() - Inside Appeasement/Refund Update to WCS Flow :: End");
					}
				} else {
					removeTaskQueueRecord(env, strTaskQueueKey);
				}
			}
		} catch (Exception e) {
			logger.info("Exception occurred in AcademyProcessEGCShipmentUpdateAgent.executeTask() method : " + e);
			e.printStackTrace();
			logger.info("Updating TaskQ - " +strTaskQueueKey+ " - avalilable date for retry.");
			manageTaskQueueRecord(env, strRetryIntervelSLAMinutes, strTaskQueueKey, strTaskQAvailableDate);
		}

		logger.endTimer("AcademyProcessEGCShipmentUpdateAgent.executeTask()");
		return docInput;
	}


	/**
	 * This method invokes the changeOrder API to update CartID
	 * 
	 * @param env
	 * @param strWCSCartId
	 * @param strOrderHeaderKey
	 * @param strTaskQueueKey
	 * @param eleOrder
	 * @return
	 * @throws Exception
	 **/



	/**
	 * This method connect WCS via WS and get the CartId
	 * 
	 * @param eleOrder
	 * @return outDoc
	 * @throws Exception
	 **/
	public String invokeWCSWebserviceAndGetCartId(Element eleOrder) {

		logger.beginTimer("AcademyProcessEGCShipmentUpdateAgent.invokeWCSWebserviceAndGetCartId()");
		String strGCOrderNumber = eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_NO);
		String strOrderNo = "";
		Element eleOrderLine = (Element) eleOrder.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE).item(0);
		Element eleLinePriceInfo = (Element) eleOrderLine.getElementsByTagName(AcademyConstants.ELE_LINEPRICE_INFO).item(0);
		String strAmount = eleLinePriceInfo.getAttribute(AcademyConstants.ATTR_UNIT_PRICE);

		Element eleItem = (Element) eleOrderLine.getElementsByTagName(AcademyConstants.ELEM_ITEM).item(0);
		String strGCItemId = eleItem.getAttribute(AcademyConstants.ATTR_ITEM_ID);

		Element elePersoninfoBillTo = (Element) eleOrder.getElementsByTagName(AcademyConstants.ELE_PERSON_INFO_BILL_TO).item(0);
		String strFirstName = elePersoninfoBillTo.getAttribute(AcademyConstants.ATTR_FNAME);
		String strLastName = elePersoninfoBillTo.getAttribute(AcademyConstants.ATTR_LNAME);
		String strCustomerEMailID = eleOrder.getAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID);
		/*
		 * Sample JSON Payload
		 { 
		 	"Order": { "ItemId": "123408604", // Is the E Gift Card Item
		  	"RecipentFirstName" : "AAA",
		  	"RecipientLastName" : "BBB", 
		  	"SenderFirstName" :	"Academy Sports",
		  	"SenderLastName" : "Outdoors",
		  	"RecipientEmailId": "AAA.BBB@gmail.com",
		  	"SenderEmailId": "appeasement@academy.com / refund@academy.com", 
		  	"OrderAmount": "10.00",
		  	"OrderNo": "XXXXXX" 
		  } 
		 */

		try {

			JSONObject jsonInput = new JSONObject();
			JSONObject order = new JSONObject();
			order.put("ItemId", strGCItemId);
			order.put("RecipientFirstName", strFirstName);
			order.put("RecipientLastName", strLastName);
			order.put("RecipientEmailId", strCustomerEMailID);
			order.put("SenderFirstName", strSenderFirstName);
			order.put("SenderLastName", strSenderLastName);
			order.put("SenderEmailId", strSenderEmailID);
			order.put("OrderAmount", strAmount);
			order.put("OrderNo", strGCOrderNumber);
			jsonInput.put(AcademyConstants.ELE_ORDER, order);

			String strEndPointUrl =  YFSSystem.getProperty(AcademyConstants.STR_EGC_WCS_APPEASE_REFUND_END_POINT);

			if(YFCCommon.isVoid(strEndPointUrl)) {
				logger.info("Exception occurred in AcademyProcessEGCShipmentUpdateAgent.invokeWCSWebserviceAndGetCartId()- EndPoint URL is missing in Customer Override Property file");
				throw new YFSException("EndPoint URL is missing in customer overrides property file");
			}

			//Invoke Webservice

			Document WCSResponseDocument = AcademyEGCRestWebServiceUtil.invokeEGCWCSRestService(jsonInput, strEndPointUrl,
					AcademyConstants.WEBSERVICE_POST);

			logger.verbose("URL for Appeasement/Refund :"  + strEndPointUrl);

			String strWCSRestServiceResponseCode = WCSResponseDocument.getDocumentElement()
					.getAttribute(AcademyConstants.ATTR_RESPONSE_CODE);

			if (!(YFCCommon.isVoid(strWCSRestServiceResponseCode))) {

				Element eleResponseOrder = (Element) WCSResponseDocument.getDocumentElement()
						.getElementsByTagName(AcademyConstants.ELE_ORDER).item(0);
				if ("SUCCESS".equalsIgnoreCase(eleResponseOrder.getAttribute(AcademyConstants.ATTR_STATUS))
						&& (strWCSRestServiceResponseCode.equalsIgnoreCase("200")
								|| strWCSRestServiceResponseCode.equalsIgnoreCase("201"))) {
					
					strOrderNo = eleResponseOrder.getAttribute(AcademyConstants.ATTR_ORDER_NO);
					logger.verbose("AcademyProcessEGCShipmentUpdateAgent.invokeWCSWebserviceAndGetCartId() is Success with HTTP Response Code :: " + strWCSRestServiceResponseCode +  
							"\nInput to WCS Webservice call for Appease/Refund: \n" + jsonInput +
							"\nOutput from WCS Webservice call for Appease/Refund : \n" + XMLUtil.getXMLString(WCSResponseDocument));
					
				} else {
					logger.info("AcademyProcessEGCShipmentUpdateAgent.invokeWCSWebserviceAndGetCartId() Failed with HTTP Error Response Code :: " + strWCSRestServiceResponseCode +  
							"\nInput to WCS Webservice call for Appease/Refund: \n" + jsonInput +
							"\nOutput from WCS Webservice call for Appease/Refund : \n" + XMLUtil.getXMLString(WCSResponseDocument));
				}
			} else {
				logger.info("Exception occurred in AcademyProcessEGCShipmentUpdateAgent.invokeWCSWebserviceAndGetCartId() while retrieving Cart ID from WCS : " +
								"\nInput to WCS Webservice call for Appease/Refund: \n" + jsonInput +
								"\nOutput from WCS Webservice call for Appease/Refund: \n" + XMLUtil.getXMLString(WCSResponseDocument));
			}

		} catch (Exception e) {
			logger.info(
					"Exception occurred in AcademyProcessEGCShipmentUpdateAgent.invokeWCSWebserviceAndGetCartId() while retrieving Cart ID from WCS : "
							+ e);
			e.printStackTrace();
		}
		logger.endTimer("AcademyProcessEGCShipmentUpdateAgent.invokeWCSWebserviceAndGetCartId()");

		return strOrderNo;
	}

	/**
	 * This method executes getOrderList API to fetch the Order details. Sample
	 * Input XML <Order OrderHeaderKey="20201020223512171744804"/>
	 * 
	 * @param env
	 * @param inDoc
	 * @return outDoc
	 * @throws Exception
	 **/
	private Document prepareAndInvokeGetOrderList(YFSEnvironment env, String strOrderHeaderKey) {
		logger.beginTimer("AcademyProcessEGCShipmentUpdateAgent.prepareAndInvokeGetOrderList()");
		logger.verbose("AcademyProcessEGCShipmentUpdateAgent.prepareAndInvokeGetOrderList() :: Input OrderHeaderKey : "+ strOrderHeaderKey);
		Document docGetOrderListOutput = null;
		Document docGetOrderListInput = null;

		if (!YFCObject.isVoid(strOrderHeaderKey)) {
			try {
				docGetOrderListInput = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
				Element eleGetOrderListInput = docGetOrderListInput.getDocumentElement();
				eleGetOrderListInput.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
				logger.verbose("AcademyProcessEGCShipmentUpdateAgent.prepareAndInvokeGetOrderList() :: Input to getOrderList API : "+ XMLUtil.getXMLString(docGetOrderListInput));
				// Invoking getOrderList API
				docGetOrderListOutput = AcademyUtil.invokeService(env, AcademyConstants.ACADEMY_GETORDERLIST_FOR_EGC_SERVICE, docGetOrderListInput);
				logger.verbose("AcademyProcessEGCShipmentUpdateAgent.prepareAndInvokeGetOrderList() :: Output of getOrderList API : "+ XMLUtil.getXMLString(docGetOrderListOutput));
			} catch (Exception e) {
				logger.info("Exception occurred in AcademyProcessEGCShipmentUpdateAgent.prepareAndInvokeGetOrderList()" + e.getMessage());
				e.printStackTrace();
			}
		}
		logger.endTimer("AcademyProcessEGCShipmentUpdateAgent.prepareAndInvokeGetOrderList()");

		return docGetOrderListOutput;
	}


	/**
	 * This method executes manageTaskQueue API to update the taskQentry(AvailableDate)
	 * 
	 * Sample Input XML <TaskQueue AvailableDate="2019-10-25T05:45:02" TaskQKey="201910230332062120822109" Operation="Modify"/>
	 * 
	 * @param env
	 * @param strRetryIntervelSLAMinutes
	 * @param strTaskQueueKey
	 * @param strTaskQAvailableDate
	 */
	private void manageTaskQueueRecord(YFSEnvironment env, String strRetryIntervelSLAMinutes, String strTaskQueueKey, String strTaskQAvailableDate) {

		logger.beginTimer("AcademyProcessEGCShipmentUpdateAgent.manageTaskQueueRecord()");
		SimpleDateFormat sdfDateFormat = null;
		String strSystemDatePlusRetrySLAMnts = null;
		String strTaskQNextAvailDate = null;
		String strTaskQAvailDatePlusRetrySLAMnts = null;
		Document manageTaskQueueinDoc = null;
		Date dtSystemDate = null;
		Date dtTaskNxtAvailDate = null;
		Calendar cal = null;

		try {
			// Fetch the System time and Add the Reminder/Escalation SLA (configured value)
			cal = Calendar.getInstance();
			sdfDateFormat = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			logger.verbose("manageTaskQueueRecord() SystemDate :: " + sdfDateFormat.format(cal.getTime()));
			dtSystemDate = sdfDateFormat.parse(sdfDateFormat.format(cal.getTime()));
			logger.verbose("manageTaskQueueRecord() strRetryIntervelSLAMinutes :: " + strRetryIntervelSLAMinutes);
			cal.add(Calendar.MINUTE, Integer.parseInt(strRetryIntervelSLAMinutes));
			strSystemDatePlusRetrySLAMnts = sdfDateFormat.format(cal.getTime());
			logger.verbose("manageTaskQueueRecord() strSystemDatePlusRetrySLA :: " + strSystemDatePlusRetrySLAMnts);

			// Set the TaskQueueAvailableDate to Calendar and Add the SLAMinutes(configured value)
			logger.verbose("manageTaskQueueRecord() TaskQAvailableDate :: " + strTaskQAvailableDate);
			cal.setTime(sdfDateFormat.parse(strTaskQAvailableDate));
			cal.add(Calendar.MINUTE, Integer.parseInt(strRetryIntervelSLAMinutes));
			strTaskQAvailDatePlusRetrySLAMnts = sdfDateFormat.format(cal.getTime());
			logger.verbose("manageTaskQueueRecord() strTaskQAvailDatePlusRetrySLAMnts :: "+ strTaskQAvailDatePlusRetrySLAMnts);
			dtTaskNxtAvailDate = sdfDateFormat.parse(strTaskQAvailDatePlusRetrySLAMnts);

			/**
			 * if NextTaskQAvailDate(TaskQAvailDate+RetrySLAMinutes) < SystemDate
			 * 		NextTaskQAvailDate = SystemDate+RetrySLAMinutes; 
			 * else 
			 * 		NextTaskQAvailDate = TaskQAvailDate+RetrySLAMinutes;
			 */
			if (dtTaskNxtAvailDate.before(dtSystemDate)) {
				strTaskQNextAvailDate = strSystemDatePlusRetrySLAMnts;
				logger.verbose("\nTaskQNextAvailDate(CurrentTaskQAvailDate+SLAMnts) < system date :: TaskQNextAvailDate is (SystemDate+SLAMinutes) :: "+ strSystemDatePlusRetrySLAMnts);
			} else {
				strTaskQNextAvailDate = strTaskQAvailDatePlusRetrySLAMnts;
				logger.verbose("\nTaskQNextAvailDate(CurrentTaskQAvailDate+SLAMnts) > system date :: TaskQNextAvailDate is (TaskQAvailDate+SLAMinutes) :: "+ strTaskQNextAvailDate);
			}

			// manageTaskQueue API inDoc
			manageTaskQueueinDoc = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
			manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_OPERATION, AcademyConstants.STR_ACTION_MODIFY);
			manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_TASK_Q_KEY, strTaskQueueKey);
			manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_AVAIL_DATE, strTaskQNextAvailDate);

			logger.verbose("Input to manageTaskQueue API :: " + XMLUtil.getXMLString(manageTaskQueueinDoc));
			// Invoking the manageTaskQueue API
			AcademyUtil.invokeAPI(env, AcademyConstants.API_MANAGE_TASK_QUEUE, manageTaskQueueinDoc);

		} catch (Exception e) {
			logger.info("Exception occurred in AcademyProcessEGCShipmentUpdateAgent.manageTaskQueueRecord() method for strTaskQueueKey : "+ strTaskQueueKey);
			logger.info("Exception : " + e + "Exception Message : " +e.getMessage());
			e.printStackTrace();
		}
		logger.endTimer("AcademyProcessEGCShipmentUpdateAgent.manageTaskQueueRecord()");
	}


	/**
	 * This method executes manageTaskQueue API to remove processed TaskQ Sample
	 * Input XML <TaskQueue Operation="Delete" TaskQKey="2018090110050322699633"/>
	 * 
	 * @param env
	 * @param inDoc
	 */
	private void removeTaskQueueRecord(YFSEnvironment env, String strTaskQueueKey) {

		logger.beginTimer("AcademyProcessEGCShipmentUpdateAgent.removeTaskQueueRecord()");
		Document manageTaskQueueinDoc = null;

		try {
			// manageTaskQueue API inDoc
			manageTaskQueueinDoc = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
			manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_OPERATION, AcademyConstants.STR_OPERATION_VAL_DELETE);
			manageTaskQueueinDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_TASK_Q_KEY, strTaskQueueKey);

			// Invoking the manageTaskQueue API
			logger.verbose("Input document to manageTaskQueue API :: " + XMLUtil.getXMLString(manageTaskQueueinDoc));
			AcademyUtil.invokeAPI(env, AcademyConstants.API_MANAGE_TASK_QUEUE, manageTaskQueueinDoc);

		} catch (Exception e) {
			logger.info("Exception occurred in AcademyProcessEGCShipmentUpdateAgent.removeTaskQueueRecord() method for strTaskQueueKey : "+ strTaskQueueKey);
			logger.info("Exception : " + e + "Exception Message : " +e.getMessage());
			e.printStackTrace();
		}
		logger.endTimer("AcademyProcessEGCShipmentUpdateAgent.removeTaskQueueRecord()");
	}

	/**
	 * This method prepare WS input and invoke the same to update fraud status to WCS
	 * 
	 * Request JSON:
	 * {"Action":"ACCEPT/DECLINE", "OrderNo":"Y100210356", "TransactionID":"764X0XXJ9NNJ"}
	 * 
	 * Response JSON:
	 * Success: {"Status": {"TransactionID": "70NX0ZQM2KMN", "OrderNo": "Y100208926", "Response": "Success"} }
	 * Failure: {"Status": {"TransactionID": "123ABCZ", "OrderNo": "Y100208926", "Response": "Failure", "Error": "Transaction id not found [123ABCZ]"} }
	 * 
	 * @Retun strWSResponse
	 **/

	public String invokeWCSWebserviceAndUpdateFraudStatus(Element eleOrder, String strAction) {

		logger.beginTimer("AcademyProcessEGCShipmentUpdateAgent.invokeWCSWebserviceAndUpdateFraudStatus()");
		String strTranID = "";
		String strWSResponse = "";
		String strGCOrderNumber = eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_NO);
		Element eleExtn = XmlUtils.getChildElement(eleOrder, AcademyConstants.ELE_EXTN);

		if (!YFCObject.isVoid(eleExtn) && eleExtn.hasAttributes()) {
			strTranID = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_TRANSACTION_ID);
		}

		try {
			JSONObject jsonInput = new JSONObject();
			jsonInput.put("OrderNo", strGCOrderNumber);
			jsonInput.put("TransactionID", strTranID);
			jsonInput.put("Action", strAction);

			String strEndPointUrl =  YFSSystem.getProperty(AcademyConstants.STR_EGC_WCS_FRAUD_END_POINT);

			if(YFCCommon.isVoid(strEndPointUrl)) {
				logger.info("Exception occurred in AcademyProcessEGCShipmentUpdateAgent.invokeWCSWebserviceAndUpdateFraudStatus() - EndPoint URL is missing in Customer Override Property file");
				throw new YFSException("EndPoint URL is missing in customer overrides property file");
			}

			//Invoke WebService
			Document WCSResponseforFraud =AcademyEGCRestWebServiceUtil.invokeEGCWCSRestService(jsonInput, strEndPointUrl,
					AcademyConstants.WEBSERVICE_POST);

			logger.verbose("URL for Fraud Status Update: " +strEndPointUrl);

			String strWCSRestServiceResponseCode = WCSResponseforFraud.getDocumentElement()
					.getAttribute(AcademyConstants.ATTR_RESPONSE_CODE);

			if (!YFCCommon.isVoid(strWCSRestServiceResponseCode)) {
				strWSResponse = WCSResponseforFraud.getDocumentElement().getAttribute(AcademyConstants.ATTR_STATUS);

				if (("SUCCESS".equalsIgnoreCase(strWSResponse) && (strWCSRestServiceResponseCode.equalsIgnoreCase("200")
						|| strWCSRestServiceResponseCode.equalsIgnoreCase("201")))) {
					logger.verbose(
							"AcademyProcessEGCShipmentUpdateAgent.invokeWCSWebserviceAndUpdateFraudStatus() is Success with HTTP Response Code :: "
									+ strWCSRestServiceResponseCode
									+ "\nInput to WCS Webservice call for Fraud Status Update: \n" + jsonInput
									+ "\nOutput from WCS Webservice call for Fraud Status Update: \n"
									+ XMLUtil.getXMLString(WCSResponseforFraud));
				} else {
					logger.info(
							"AcademyProcessEGCShipmentUpdateAgent.invokeWCSWebserviceAndUpdateFraudStatus() Failed with HTTP Error Response Code :: "
									+ strWCSRestServiceResponseCode
									+ "\nInput to WCS Webservice call for Fraud Status Update: \n" + jsonInput
									+ "\nOutput from WCS Webservice call for Fraud Status Update: \n"
									+ XMLUtil.getXMLString(WCSResponseforFraud));
				}
			} else {
				logger.info(
						"Exception occurred in AcademyProcessEGCShipmentUpdateAgent.invokeWCSWebserviceAndUpdateFraudStatus() while updating Fraud Order Status to WCS : "
								+ "\nInput to WCS Webservice call for Fraud Status Update: \n" + jsonInput
								+ "\nOutput from WCS Webservice call for Fraud Status Update: \n"
								+ XMLUtil.getXMLString(WCSResponseforFraud));
			}

		} catch (Exception e) {
			logger.info("Exception occurred in AcademyProcessEGCShipmentUpdateAgent.invokeWCSWebserviceAndUpdateFraudStatus()" + e.getMessage());
			e.printStackTrace();
		}
		logger.endTimer("AcademyProcessEGCShipmentUpdateAgent.invokeWCSWebserviceAndUpdateFraudStatus()");

		return strWSResponse;
	}

}
