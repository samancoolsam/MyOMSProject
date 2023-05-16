package com.academy.ecommerce.sterling.fraud.kount;

/*##################################################################################
 *
 * Project Name                : Kount Integration
 * Module                      : OMS
 * Author                      : CTS
 * Date                        : 10-MAY-2018 
 * Description				   : This class does following 
 * 								  1.OMS Receive ENS messages from ESB for this service 
 * 									to resolve/place the Holds on the Order.
 * 								  2.If the Message contains Action="MODIFY" then 
 * 									changeOrder API is called to resolve/place the 
 *  								hold on the Order
 * 								  3.If the Message contains Action="CANCEL" then it 
 * 								  	calls the CancelOrderService of WCS to emulate the 
 * 									same.
 * 
 * 
 * Change Revision
 * ---------------------------------------------------------------------------------
 * Date            Author         		Version#       Remarks/Description                      
 * ---------------------------------------------------------------------------------
 * 10-MAY-2018		CTS  	 			  1.0           	Initial version
 * ##################################################################################*/

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.cts.sterling.custom.accelerators.util.XMLUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyENSFraudStatusUpdates {

	private static Logger log = Logger
			.getLogger(AcademyENSFraudStatusUpdates.class.getName());
	
	private static Properties props;

	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}
	/**
	 * FPT - 11,13,14 This method process the ENS Updates from Kount and updates
	 * the fraud status in OMS.
	 * 
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public Document getENSFraudStatusUpdates(YFSEnvironment env, Document inDoc)
			throws Exception {
		log
				.verbose("***************Entering AcademyGetENSFraudStatusUpdatesFromKount.getENSFraudStatusUpdates()***************");
		
		Date formatKountEventTime = null;
		Date formatSterlingEventTime = null;
		
		Document outDoc = null;
		Document getOrderListoutDoc = null;
		
		Element eleENSMsgOrdHoldTypes = null;
		Element eleENSMsgResOrdHoldType = null;
		Element eleENSMsgNewOrdHoldType = null;
		Element elegetOrderListoutDoc = null;
		Element eleOrdergetOrderList = null;
		Element eleExtn = null;
		Element eleOrderHoldTypesTag = null;
		Element eleOrderHoldTypeTag = null;
		Element eleNewOrderHoldTypeTag = null;
		Element eleInputExtn = null;
		
		String strOrderNo = null;
		String strAction = null;
		String strKountEventTime = null;
		String strENSResolveHoldType = null;
		String strENSNewHoldType = null;
		String strSterlingEventTime = null;
		String strCurrentSterlingHoldType = null;


		boolean callChangeOrder = false;
		boolean callCancelService = false;

		Element eleInput = inDoc.getDocumentElement();
		eleInput.setAttribute(AcademyConstants.ATTR_DOC_TYPE,AcademyConstants.SALES_DOCUMENT_TYPE);
		eleInputExtn = SCXmlUtil.getChildElement(eleInput,AcademyConstants.ELE_EXTN);
		eleENSMsgOrdHoldTypes = SCXmlUtil.getChildElement(eleInput,
				AcademyConstants.ELE_ORDER_HOLD_TYPES);
		if (!YFCObject.isVoid(eleENSMsgOrdHoldTypes)
				&& eleENSMsgOrdHoldTypes.hasChildNodes()) {
			eleENSMsgResOrdHoldType = SCXmlUtil.getXpathElement(
					eleENSMsgOrdHoldTypes, AcademyConstants.XPATH_ENS_RES_HOLD_STATUS);
			eleENSMsgNewOrdHoldType = SCXmlUtil.getXpathElement(
					eleENSMsgOrdHoldTypes, AcademyConstants.XPATH_ENS_CREATED_HOLD_STATUS);
			strENSResolveHoldType = XMLUtil.getAttributeFromXPath(inDoc,AcademyConstants.XPATH_ENS_RES_HOLD_TYPE);
			strENSNewHoldType = XMLUtil.getAttributeFromXPath(inDoc,AcademyConstants.XPATH_ENS_NEW_HOLD_TYPE);
		}
		
		strOrderNo = XMLUtil.getAttributeFromXPath(inDoc,AcademyConstants.XPATH_ORDER_NO);
		
		strAction = XMLUtil.getAttributeFromXPath(inDoc, AcademyConstants.XPATH_ACTION);
		
		if (!YFCObject.isVoid(eleInputExtn)) {
			strKountEventTime = SCXmlUtil.getAttribute(eleInputExtn,AcademyConstants.STR_EXTN_EVENT_TIME);
		}
		if(YFCObject.isVoid(strOrderNo) || YFCObject.isVoid(strKountEventTime)){
			String errorDescription = "Order No or KountEventTime passed is blank  ";
			YFSException yfse = new YFSException();
			yfse.setErrorCode(AcademyConstants.ERR_CODE_20);
			yfse.setErrorDescription(errorDescription);
			throw yfse;
		}
		
		if(!YFCObject.isVoid(strKountEventTime)){
		
			formatKountEventTime = new SimpleDateFormat(
					AcademyConstants.STR_DATETIME_PATTERN).parse(strKountEventTime);
			DateFormat date = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			String newKountDateFormat = date.format(formatKountEventTime);
			eleInputExtn.setAttribute(AcademyConstants.ATTR_EXTN_EVENT_TIME, newKountDateFormat);
		}
		

		// Call the getOrderList API to get the Time Stamp and HoldType from
		// Sterling
		
		getOrderListoutDoc = callgetOrderList(env, strOrderNo);
		
		elegetOrderListoutDoc = getOrderListoutDoc.getDocumentElement();
		eleOrdergetOrderList = SCXmlUtil.getChildElement(elegetOrderListoutDoc,
				AcademyConstants.ELE_ORDER);
		eleExtn = SCXmlUtil.getXpathElement(eleOrdergetOrderList,
				AcademyConstants.ELE_EXTN);

		if (!YFCObject.isVoid(eleExtn) && eleExtn.hasAttributes()) {
			strSterlingEventTime = eleExtn.getAttribute(AcademyConstants.STR_EXTN_EVENT_TIME);
			if (!YFCObject.isVoid(strSterlingEventTime)) {

				formatSterlingEventTime = new SimpleDateFormat(
						AcademyConstants.STR_DATE_TIME_PATTERN)
						.parse(strSterlingEventTime);
				
			}
			
		}

		eleOrderHoldTypesTag = SCXmlUtil.getChildElement(eleOrdergetOrderList,
				AcademyConstants.ELE_ORDER_HOLD_TYPES);

		if (!YFCObject.isVoid(eleOrderHoldTypesTag)
				&& eleOrderHoldTypesTag.hasChildNodes()) {
			eleOrderHoldTypeTag = SCXmlUtil.getXpathElement(eleOrderHoldTypesTag,"OrderHoldType[@Status=1100 and (@HoldType='FRAUD_NOCHECK_KOUNT' or @HoldType='FRAUD_ESCALATE_KOUNT' or @HoldType='FRAUD_REVIEW_KOUNT')]");
			if (!YFCObject.isVoid(eleOrderHoldTypeTag) && eleOrderHoldTypeTag.hasAttributes()) {
				
				strCurrentSterlingHoldType = eleOrderHoldTypeTag.getAttribute(AcademyConstants.ATTR_HOLD_TYPE);
				
			}
		}
		
		int result = 1;
		
		if (!YFCObject.isVoid(formatSterlingEventTime)) {
			result = formatKountEventTime.compareTo(formatSterlingEventTime);
		}

		if (result > 0) {
			if (strAction.equals(AcademyConstants.STR_CANCEL)) {
				callCancelService = true;
			} else {

				if (!YFCObject.isVoid(strCurrentSterlingHoldType)
						&& !YFCObject.isVoid(strENSResolveHoldType)) {

					if (strCurrentSterlingHoldType.equals(strENSResolveHoldType)) {
						
						callChangeOrder = true;
						
					}else {
						
						if(!YFCObject.isVoid(strENSNewHoldType)){
							
							if(strCurrentSterlingHoldType.equals(strENSNewHoldType)){
								
								eleENSMsgOrdHoldTypes.removeChild(eleENSMsgResOrdHoldType);
								callChangeOrder = true;
							}else{
								eleENSMsgResOrdHoldType.setAttribute(AcademyConstants.ATTR_HOLD_TYPE,
										strCurrentSterlingHoldType);
								callChangeOrder = true;
							}
							
						}else{ // if strNewHoldType is void
							eleENSMsgResOrdHoldType.setAttribute(AcademyConstants.ATTR_HOLD_TYPE,
									strCurrentSterlingHoldType);
							callChangeOrder = true;
						}
						
					}
				} else {

					if (YFCObject.isVoid(strENSResolveHoldType)) {
						if (!YFCObject.isVoid(strCurrentSterlingHoldType) && strCurrentSterlingHoldType
								.equals(strENSNewHoldType)) {

							callChangeOrder = true;
						} else {
							
							if (!YFCObject.isVoid(eleOrderHoldTypeTag)) {
								eleOrderHoldTypeTag.setAttribute(AcademyConstants.STATUS,
										AcademyConstants.STR_HOLD_RESOLVED_STATUS);
								SCXmlUtil.importElement(eleENSMsgOrdHoldTypes,
										eleOrderHoldTypeTag);
								
							}
							callChangeOrder = true;
							
						}
					} else {

						if (!YFCObject.isVoid(strENSNewHoldType)
								&& !strENSResolveHoldType
										.equals(strENSNewHoldType)) {
							eleENSMsgOrdHoldTypes
									.removeChild(eleENSMsgResOrdHoldType);

							callChangeOrder = true;
						} else { // if(YFCObject.isVoid(strENSNewHoldType))
							eleENSMsgOrdHoldTypes.removeChild(eleENSMsgResOrdHoldType);
							callChangeOrder = true;
						}

					}

				}
				
			
			}
		} else {
			
				log.verbose("******************** Message Looks Invalid***********************");
				YFCException yfce = new YFCException("Incoming Message Event Time is older than the system Event Time");
				yfce.setAttribute("ErrorCode", AcademyConstants.ERR_CODE_11);
				yfce.setErrorDescription("Incoming Message Event Time is older than the system Event Time");
				throw yfce;
			

		}


			if (callCancelService) {
				log.verbose("CallCancelOrderService Input is " + SCXmlUtil.getString(inDoc));
				outDoc = callCancelOrderService(env, inDoc);
				log.verbose("CallCancelOrderService API Output is " + SCXmlUtil.getString(outDoc));
			}

			if (callChangeOrder) {

				log.verbose("changeOrder API Input is " + SCXmlUtil.getString(inDoc));
				outDoc = AcademyUtil.invokeAPI(env,
						AcademyConstants.API_CHANGE_ORDER, inDoc);
				log.verbose("changeOrder API Output is " + SCXmlUtil.getString(outDoc));
			}
		
		log.verbose("End of AcademyGetENSFraudStatusUpdatesFromKount.getENSFraudStatusUpdates() method");

		return outDoc;
	}

	public static Document callCancelOrderService(YFSEnvironment env, Document inDoc)
			throws Exception {
		// TODO Auto-generated method stub
		log.verbose("*************************Start of AcademyGetENSFraudStatusUpdatesFromKount.callCancelOrderService()");
		Document outputDocforService = null;
		
		Element eleinDocForService = inDoc.getDocumentElement();
		
		String strReasonCode = props.getProperty(AcademyConstants.ATTR_REASON_CODE);
		if(!YFCObject.isVoid(strReasonCode)){
			eleinDocForService.setAttribute(AcademyConstants.ATTR_REASON_CODE,strReasonCode);
		}else{
			eleinDocForService.setAttribute(AcademyConstants.ATTR_REASON_CODE,AcademyConstants.STR_KOUNT_DENY);
		}
		eleinDocForService.setAttribute(AcademyConstants.STR_CANCELLED_BY, AcademyConstants.STR_KOUNT);
		eleinDocForService.setAttribute(AcademyConstants.STR_CANCELLING_SYSTEM,AcademyConstants.STR_KOUNT);
		eleinDocForService.setAttribute(AcademyConstants.ATTR_DOC_TYPE,
				AcademyConstants.SALES_DOCUMENT_TYPE);
		
		log.verbose("CancelOrderService API Input: =" + SCXmlUtil.getString(inDoc));
		
		outputDocforService = AcademyUtil.invokeService(env,
				AcademyConstants.SERVICE_ACADEMY_KOUNT_CANCEL_SERVICE, inDoc);
		
		log.verbose("CancelOrderService API output: =" + SCXmlUtil.getString(outputDocforService));
		
		log.verbose("*************************End of AcademyGetENSFraudStatusUpdatesFromKount.callCancelOrderService()");
		return outputDocforService;
	}

	private Document callgetOrderList(YFSEnvironment env, String OrderNo)
			throws IllegalArgumentException, Exception {

		log.verbose("*************************Entering AcademyGetENSFraudStatusUpdatesFromKount.callgetOrderList()*************************");
		Document docgetOrderListInput = null;
		Document getOrderListTemplate = null;
		Document getOrderListOutputDoc = null;

		log.verbose("**********Order No is"+OrderNo+"**********");
		docgetOrderListInput = XMLUtil.getDocument("<Order OrderNo='" + OrderNo + "' DocumentType='0001' EnterpriseCode='Academy_Direct' />");

		log.verbose("Calling getOrderList API Input: =" + SCXmlUtil.getString(docgetOrderListInput));

		getOrderListTemplate = XMLUtil
				.getDocument("<OrderList>"
						+ "<Order OrderHeaderKey='' OrderNo='' EnterpriseCode='' DocumentType=''>"
						+ "<Extn ExtnEventTime='' ExtnTransactionID='' ExtnWebFraudCheck='' /> "
						+ "<OrderHoldTypes>"
						+ "<OrderHoldType HoldType='' Status='' />"
						+ "</OrderHoldTypes>" + "</Order>" + "</OrderList>");

		env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST,
				getOrderListTemplate);

		getOrderListOutputDoc = AcademyUtil.invokeAPI(env,
				AcademyConstants.API_GET_ORDER_LIST, docgetOrderListInput);

		log.verbose("Calling getOrderList API Output: ="
				+ SCXmlUtil.getString(getOrderListOutputDoc));

		NodeList NLgetOrderList = getOrderListOutputDoc
				.getElementsByTagName(AcademyConstants.ELE_ORDER);
		if (NLgetOrderList.getLength() <= 0) {

			String errorDescription = "Invalid Order";
			YFSException yfse = new YFSException();
			yfse.setErrorCode(AcademyConstants.ERR_CODE_21);
			yfse.setErrorDescription(errorDescription);
			throw yfse;
		}

		
		log.verbose("*************************End of AcademyGetENSFraudStatusUpdatesFromKount.callgetOrderList()");
		return getOrderListOutputDoc;
	}

}
