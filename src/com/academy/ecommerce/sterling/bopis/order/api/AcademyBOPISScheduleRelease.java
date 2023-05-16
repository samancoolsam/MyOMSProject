package com.academy.ecommerce.sterling.bopis.order.api;

/*##################################################################################
*
* Project Name                : Release 4 
* Module                      : OMS - POD
* Author                      : CTS
* Date                        : 26-SEP-2019 
* Jira#						  : OMNI-1585
* Description				  : Updated as part of OMNI - 1585  
* 								Updated the logic to conditionally process Schedule 
* 								& Release for PICK lines.  The process is skipped if
* 								any Kount related holds are present on the order during 
* 								order create. The schedule release process gets applied 
* 								when the kount holds are resolved, for such orders. 
* 								This class is expected to get invoked only when there is 
* 								atleast one bopis line on an order. 
* 							 
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author         		Version#       Remarks/Description                      
* ---------------------------------------------------------------------------------
* 26-SEP-2019	CTS - POD  	 			  1.0            Initial version
* ##################################################################################*/


import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @Author : Chiranthan(SapientRazorfish_)
 * @JIRA# : BOPIS-11 : ASO_Order Capture And Processing
 * @Date : Created on 30-July-2018
 * 
 * @Purpose : 
 * This class calls scheduleOrder API with ScheduleAndRelease=Y to schedule & release the order(BOPIS lines only)
 * In case of mixed cart orders, Hold is placed on Non-BOPIS lines to avoid scheduling of Non-BOPIS lines with BOPIS Schedule Rule ID,
 * After BOPIS lines are scheduled & released, line level hold on Non-BOPIS lines is resolved & Scheduling process through agent continues as-is for Non-BOPIS lines. 
 * 
 **/
public class AcademyBOPISScheduleRelease implements YIFCustomApi {

	private static Logger log = Logger.getLogger(AcademyBOPISScheduleRelease.class.getName());
	private Properties props;
	public void setProperties(Properties props) throws Exception{		
		this.props = props;
	}
	String strOrderHeaderKey = null;
	
	
	public void scheduleRelease(YFSEnvironment env, Document inDoc) throws Exception {		
		log.verbose("Entering AcademyBOPISScheduleRelease.scheduleRelease() :: "+XMLUtil.getXMLString(inDoc));
		strOrderHeaderKey = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
		
		/*//Start :BOPIS Fraud Hold
		String strHoldTypeStatus = XPathUtil.getString(inDoc, 
				"/Order/OrderHoldTypes/OrderHoldType[@HoldType='" + 
				AcademyConstants.STR_ACAD_BOPIS_FRAUD_HOLD + "']/@Status");
		
		//In case the hold is resolved vi changeOrder aPI
		if(YFCObject.isVoid(strHoldTypeStatus)) {
			strHoldTypeStatus = XPathUtil.getString(inDoc, 
			"/OrderHoldType[@HoldType='" +
			AcademyConstants.STR_ACAD_BOPIS_FRAUD_HOLD + "']/@Status");
		}*/
		
		
		// Start -  OMNI - 1585 BOPIS Orders with Fraud Status
		
		boolean boolSchRel = false;
		Element eleRoot = inDoc.getDocumentElement();
		Node eleOrderHoldTypeTag = null;
		
		try{
			Node ndFraudCheckNode = XPathUtil.getNode(eleRoot,AcademyConstants.XPATH_FRAUD_NODE_CHECK);
			///OrderHoldType[(@Status='1300') and (@HoldType='FRAUD_NOCHECK_KOUNT' or @HoldType='FRAUD_ESCALATE_KOUNT' or @HoldType='FRAUD_REVIEW_KOUNT')]
			/* This code is invoked as part of create Order Flow and condition check is as part of CreateOrder ONSUCESS event */
			if (eleRoot.getTagName().equals("Order")){
				  //In Create Order On Success if Kount Holds are present in created status the below element will not be null.
				///Order/OrderHoldTypes/OrderHoldType[(@Status='1100') and (@HoldType='FRAUD_NOCHECK_KOUNT' or @HoldType='FRAUD_ESCALATE_KOUNT' or @HoldType='FRAUD_REVIEW_KOUNT')]
				 eleOrderHoldTypeTag = XPathUtil.getNode(eleRoot,AcademyConstants.XPATH_KOUNT_ORDER_HOLD_TYPES);
				if(YFCObject.isVoid(eleOrderHoldTypeTag)){
					log.verbose("Order is flowing through Create Order Flow,No Kount holds exist");
					boolSchRel = true;
					//When this flag is true we invoke Bopis ScheduleRelease API.
					log.verbose("The boolSchRel flag is set to::" + boolSchRel );
					
				}
			/* This code is invoked as part of change Order Flow and condition check is as part of changeOrder HOLD_TYPE_STATUS_CHANGE event */
			}else if (!YFCObject.isVoid(ndFraudCheckNode) && eleRoot.getTagName().equals("OrderHoldType")){
				 log.verbose("Kount Hold is resolved");
				 eleOrderHoldTypeTag = XPathUtil.getNode(eleRoot,AcademyConstants.XPATH_EVENT_KOUNT_ORDER_HOLD_TYPES);
				 //We are now checking if any new Kount Holds got created though ENS or KountOrderDetailAgent.
				 ///OrderHoldType/Order/@MinOrderStatus
				 String strMinOrderStatus = XPathUtil.getString(eleRoot,AcademyConstants.XPATH_EVENT_MIN_ORDER_STATUS);
				 String strMaxOrderStatus = XPathUtil.getString(eleRoot,AcademyConstants.XPATH_EVENT_MAX_ORDER_STATUS);
				 ///OrderHoldType/Order/@MaxOrderStatus				 
				 int dMinOrderStatus = Integer.parseInt(strMinOrderStatus);
				 int dMaxOrderStatus = Integer.parseInt(strMaxOrderStatus);
				 int dAldMinOrderStatus = Integer.parseInt(AcademyConstants.STR_ALLOWED_MIN_ORDER_STATUS);
				 int dAldMaxOrdStatus = Integer.parseInt(AcademyConstants.STR_ALLOWED_MAX_ORDER_STATUS);
				 int dCanldOrdStatus = Integer.parseInt(AcademyConstants.VAL_CANCELLED_STATUS);
				 
				 log.verbose("Allowable MinOrderStatus :: "+Integer.toString(dAldMinOrderStatus)+ " Allowable MaxOrderStatus :: "+ Integer.toString(dAldMaxOrdStatus) 
						 +" MinOrderStatus of Order:: "+ Integer.toString(dMinOrderStatus) +" MaxOrderStatus of Order:: "+ Integer.toString(dMaxOrderStatus));
				
				 if(YFCObject.isVoid(eleOrderHoldTypeTag) &&  (dMinOrderStatus < dAldMinOrderStatus) && ((dMaxOrderStatus < dAldMaxOrdStatus) || (dMaxOrderStatus == dCanldOrdStatus)) ){
					 log.verbose("Order is flowing through change Order Hold Type Status Change Event Flow");
						boolSchRel = true;
					 log.verbose("The boolSchRel flag is set to::" + boolSchRel);
					}
			}
		}catch(Exception e){
			
			throw new YFSException(e.getMessage(), "SCHREL0002", "Exception occurred while processing Bopis Order XML");
		}
		log.verbose("Boolean Flag Value is::"+boolSchRel);
		/*//If Hold is resolved, then invoke the below logic
		if(YFCObject.isVoid(strHoldTypeStatus) || 
				(!YFCObject.isVoid(strHoldTypeStatus) && AcademyConstants.STR_HOLD_RESOLVED_STATUS.equals(strHoldTypeStatus))) {*/
		if(boolSchRel){
			//End -  OMNI - 1585 BOPIS Orders with Fraud Status
				
			log.verbose("BOPIS Order is eligible for schedule and Release");
		try {
			//Apply Line Level Hold on Non-BOPIS orderLines
			applyOrResolveHoldOnNonBopisLines(env, inDoc, AcademyConstants.STR_HOLD_CREATED_STATUS);
			
			//schedule & release BOPIS lines
			scheduleOrder(env);
			
			//Resolve Line Level Hold applied on Non-BOPIS orderLines
			applyOrResolveHoldOnNonBopisLines(env, inDoc, AcademyConstants.STR_HOLD_RESOLVED_STATUS);
			
		} catch (Exception e){
			e.printStackTrace();
			throw new YFSException(e.getMessage(), "SCHREL0001", "Exception occurred while scheduleRelease the order");
		}
		}
		//End : BOPIS fraud Hold
		
		log.verbose("Exiting AcademyBOPISScheduleRelease.scheduleRelease()");
	}
	
	/** This method applies/resolves orderLine level hold on Non-BOPIS lines, to avoid Non-BOPIS lines from getting scheduled & released with BOPIS Scheduling rule.
	 * @param env
	 * @param inDoc
	 */
	private void applyOrResolveHoldOnNonBopisLines(YFSEnvironment env, Document inDoc, String strHoldStatus) throws Exception {		
		log.verbose("Entering AcademyBOPISScheduleRelease.applyOrResolveHoldOnNonBopisLines() - HOLD Creation/Resolution - "+strHoldStatus);
		String strDeliveryMethod = null;		
		Element elemOrderLine = null;
		Element eleInChangeOrder = null;
		Element eleOrderLines = null;
		Element eleOrderLine = null;
		Element eleOrderHoldTypes = null;
		Element eleOrderHoldType = null;
		Document docInChangeOrder = null;
		Document docOutChangeOrder = null;
		
		docInChangeOrder = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		eleInChangeOrder = docInChangeOrder.getDocumentElement();
		eleInChangeOrder.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
		eleInChangeOrder.setAttribute(AcademyConstants.ATTR_OVERRIDE, AcademyConstants.STR_YES);
		eleOrderLines = docInChangeOrder.createElement(AcademyConstants.ELE_ORDER_LINES);
		eleInChangeOrder.appendChild(eleOrderLines);		
		
		NodeList nlOrderLine = inDoc.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
		log.verbose("nlOrderLine length : "+nlOrderLine.getLength());
		for (int i = 0; i < nlOrderLine.getLength(); i++) {
			elemOrderLine = (Element) nlOrderLine.item(i);			
			strDeliveryMethod = elemOrderLine.getAttribute(AcademyConstants.ATTR_DELIVERY_METHOD);
			
			if(!(AcademyConstants.STR_PICK_DELIVERY_METHOD.equals(strDeliveryMethod))){
				eleOrderLine = docInChangeOrder.createElement(AcademyConstants.ELE_ORDER_LINE);
				eleOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, elemOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
				eleOrderLines.appendChild(eleOrderLine);
				eleOrderHoldTypes = docInChangeOrder.createElement(AcademyConstants.ELE_ORDER_HOLD_TYPES);
				eleOrderLine.appendChild(eleOrderHoldTypes);
				eleOrderHoldType = docInChangeOrder.createElement(AcademyConstants.ELE_ORDER_HOLD_TYPE);
				eleOrderHoldType.setAttribute(AcademyConstants.ATTR_HOLD_TYPE, AcademyConstants.STR_ORDERLINE_SCHREL_HOLD_TYPE);
				eleOrderHoldType.setAttribute(AcademyConstants.ATTR_REASON_TEXT, AcademyConstants.STR_ORDERLINE_HOLD_REASON_TEXT);
				eleOrderHoldType.setAttribute(AcademyConstants.ATTR_STATUS, strHoldStatus);
				eleOrderHoldTypes.appendChild(eleOrderHoldType);
			}
		}
		
		if(eleInChangeOrder.getElementsByTagName(AcademyConstants.ELE_ORDER_HOLD_TYPE).getLength() > 0){
			log.verbose("Input to changeOrder API : "+XMLUtil.getXMLString(docInChangeOrder));
			docOutChangeOrder = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER, docInChangeOrder);
			log.verbose("Output from changeOrder API : "+XMLUtil.getXMLString(docOutChangeOrder));
		}
		
		log.verbose("Exiting AcademyBOPISScheduleRelease.applyOrResolveHoldOnNonBopisLines()");
	}
	
	/** This method calls scheduleOrder API with BOPIS Scheduling rule, to schedule only BOPIS lines
	 * @param env
	 */
	private void scheduleOrder(YFSEnvironment env) throws Exception {		
		log.verbose("Entering AcademyBOPISScheduleRelease.scheduleOrder() :: "+strOrderHeaderKey);
		Document docInScheduleOrder = null;
		Element eleInScheduleOrder = null;
		
		docInScheduleOrder = XMLUtil.createDocument(AcademyConstants.ELE_SCHEDULE_ORDER);
		eleInScheduleOrder = docInScheduleOrder.getDocumentElement();
		eleInScheduleOrder.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.SALES_DOCUMENT_TYPE);
		eleInScheduleOrder.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
		eleInScheduleOrder.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
		eleInScheduleOrder.setAttribute(AcademyConstants.ATTR_ALLOCATION_RULE_ID, props.getProperty(AcademyConstants.STR_BOPIS_SCHEDULE_RULE_ID));
		eleInScheduleOrder.setAttribute(AcademyConstants.CHECK_INVENTORY, AcademyConstants.STR_YES);
		eleInScheduleOrder.setAttribute(AcademyConstants.ATTR_SCHEDULE_AND_RELEASE, AcademyConstants.STR_YES);
		
		log.verbose("Input to scheduleOrder API : "+XMLUtil.getXMLString(docInScheduleOrder));
		AcademyUtil.invokeAPI(env, AcademyConstants.API_SCHEDULE_ORDER, docInScheduleOrder);
		log.verbose("ScheduleOrder API Successful...");
		
		log.verbose("Exiting AcademyBOPISScheduleRelease.scheduleOrder()");
	}
	
}
