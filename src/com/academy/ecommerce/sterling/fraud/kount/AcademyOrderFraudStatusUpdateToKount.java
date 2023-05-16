package com.academy.ecommerce.sterling.fraud.kount;

/*##################################################################################
 *
 * Project Name                : Kount Integration
 * Module                      : OMS
 * Author                      : CTS
 * Date                        : 10-MAY-2019 
 * Description				   : This class does following 
 * 								  1.Updating the order fraud status at kount through web-service call in below scenarios
 * 								  2.On hold resolution(FRAUD_REVIEW_KOUNT/FRAUD_ESCALATE_KOUNT) resolve by business team from console
 * 								  3.On order cancellation
 * 								  4.On return order invoice updating the refund status as 'R' to Kount.
 * Change Revision
 * ---------------------------------------------------------------------------------
 * Date            Author         		Version#       Remarks/Description                      
 * ---------------------------------------------------------------------------------
 * 10-MAY-2019		CTS  	 			  1.0           	Initial version
 * 22-MAY-2019		CTS  	 			  1.1           	Updated for Return scenarios(FPT-22,28,54)
 * ##################################################################################*/

import org.w3c.dom.Document;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyOrderFraudStatusUpdateToKount {

	/**
	 * Instance of logger
	 */
	private static YFCLogCategory logger = YFCLogCategory.instance(AcademyOrderFraudStatusUpdateToKount.class.getName());
	
	/** FPT - 1, 2, 6
	 * This method Post the inDoc as a message to queue, if hold has been resolved through Application Console
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public Document onHoldResolvePostingMsgToQueue(YFSEnvironment env, Document inDoc) throws Exception {

		String strProgId = null;

		logger.verbose("Begin AcademyOrderFraudStatusUpdateToKount.onHoldResolvePostingMsgToQueue() \n");
		logger.verbose("AcademyOrderFraudStatusUpdateToKount:onHoldResolvePostingMsgToQueue: InDoc \n" + XMLUtil.getXMLString(inDoc));

		strProgId = env.getProgId();
		logger.verbose(" strProgId : " + strProgId);
		
		if(strProgId.equalsIgnoreCase(AcademyConstants.STR_CONSOLE)) {
			
			inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_TRANSACTIONTYPE,AcademyConstants.STR_ONHOLD);
			AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACADEMY_FRAUD_UPDATE_STATUS_MSG_QUEUE, inDoc);
		}
		logger.verbose("End AcademyOrderFraudStatusUpdateToKount.onHoldResolvePostingMsgToQueue() \n");
		return inDoc;
	}

	/**This method Post the inDoc as a message to queue, if order cancelled by manually
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public Document onOrderCancelPostingMsgToQueue(YFSEnvironment env, Document inDoc) throws Exception {

		logger.verbose("Begin AcademyOrderFraudStatusUpdateToKount.onOrderCancelPostingMsgToQueue() \n");
		logger.verbose("AcademyOrderFraudStatusUpdateToKount:onOrderCancelPostingMsgToQueue: InDoc \n " + XMLUtil.getXMLString(inDoc));
		
		inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_TRANSACTIONTYPE,AcademyConstants.STR_ONCANCEL);
		AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACADEMY_FRAUD_UPDATE_STATUS_MSG_QUEUE, inDoc);
		
		logger.verbose("End AcademyOrderFraudStatusUpdateToKount.onOrderCancelPostingMsgToQueue() \n");
		return inDoc;
	}

	/**This method Process messages from queue and Posting order fraud status to Kount on Hold Resolve/Cancel/Return
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public  void fraudStatusUpdateToKount(YFSEnvironment env, Document inDoc) throws Exception {
		
		logger.verbose("Begin AcademyOrderFraudStatusUpdateToKount.fraudStatusUpdateToKount() \n");
		logger.verbose("AcademyOrderFraudStatusUpdateToKount:postFraudStatusToKount:Input \n" + XMLUtil.getXMLString(inDoc));

		String strKountStatusInput = null;
		String strURLType = null;
		String srtTransactionType = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_TRANSACTIONTYPE);
		
		if(!YFSObject.isVoid(srtTransactionType) && srtTransactionType.equals(AcademyConstants.STR_ONHOLD)) {
			
			String strHoldType = XMLUtil.getAttributeFromXPath(inDoc, AcademyConstants.XPATH_ATTR_HOLDTYPE);
			String strMaxOrderStatus = XMLUtil.getAttributeFromXPath(inDoc, AcademyConstants.XPATH_ATTR_MAXORDERSTATUS);

			if(!YFSObject.isVoid(strHoldType) && 
				((strHoldType.equals(AcademyConstants.STR_FRAUD_REVIEW_KOUNT)) || 
				(strHoldType.equals(AcademyConstants.STR_FRAUD_ESCALATE_KOUNT))) && 
				(!strMaxOrderStatus.equals(AcademyConstants.VAL_CANCELLED_STATUS))) {
				
				String strExtnTransactionID = XMLUtil.getAttributeFromXPath(inDoc, AcademyConstants.XPATH_ATTR_ORDERHOLDTYPE_EXTN_TRANSACTIONID);
				logger.verbose(":: strExtnTransactionID :: " + strExtnTransactionID);

				if (!YFSObject.isVoid(strExtnTransactionID)) {
					
					strKountStatusInput = prepareKountWSRequestInput(strExtnTransactionID, AcademyConstants.STR_KOUNT_STATUS_A, AcademyConstants.STR_OMS_ACCEPT);
					strURLType = AcademyConstants.STR_KOUNT_URL_STATUS;
				}
				else {
					YFSException yfsExec = new YFSException("Mandatory parameters are missing : ExtnTransactionID");
					yfsExec.setErrorCode(AcademyConstants.ERR_CODE_20);
					throw yfsExec;
				}
			}
		}else if(!YFSObject.isVoid(srtTransactionType) && srtTransactionType.equals(AcademyConstants.STR_ONCANCEL)) {
			
			String strExtnTransactionID = XMLUtil.getAttributeFromXPath(inDoc, AcademyConstants.XPATH_ATTR_ORDER_EXTN_TRANSACTIONID);
			String strReasonCode = XMLUtil.getAttributeFromXPath(inDoc, AcademyConstants.XPATH_ATTR_REASONCODE);
			String strHoldTypeValue = XMLUtil.getAttributeFromXPath(inDoc, AcademyConstants.XPATH_ATTR_HOLDTYPEVALUE);
			strURLType = AcademyConstants.STR_KOUNT_URL_STATUS;
			logger.verbose(":: strExtnTransactionID :: " + strExtnTransactionID);
			logger.verbose(":: strReasonCode :: " + strReasonCode);
			
			if(strReasonCode.equals(AcademyConstants.STR_FRAUD) && !YFSObject.isVoid(strExtnTransactionID)) {
				
				strKountStatusInput = prepareKountWSRequestInput(strExtnTransactionID, AcademyConstants.STR_KOUNT_STATUS_D, AcademyConstants.STR_OMS_FRAUD);
			}
			else if(!YFSObject.isVoid(strHoldTypeValue) && 
					!strReasonCode.equals(AcademyConstants.STR_KOUNT_DENY) && 
					(strHoldTypeValue.equals(AcademyConstants.STR_FRAUD_REVIEW_KOUNT) || 
					 strHoldTypeValue.equals(AcademyConstants.STR_FRAUD_ESCALATE_KOUNT))) {	
				
				if(!YFSObject.isVoid(strExtnTransactionID)) {
					
					strKountStatusInput = prepareKountWSRequestInput(strExtnTransactionID, AcademyConstants.STR_KOUNT_STATUS_A, AcademyConstants.STR_OMS_ACCEPT);	
				}
				else {
					YFSException yfsExec = new YFSException("Mandatory parameters are missing : ExtnTransactionID");
					yfsExec.setErrorCode(AcademyConstants.ERR_CODE_20);
					throw yfsExec;
				}						
			}		
		}
		//Start : FPT-22,28,54
		else if(!YFSObject.isVoid(srtTransactionType) && srtTransactionType.equals(AcademyConstants.STR_ONRETURN)) {
			
			String strExtnTransactionID = null;
			
			if(inDoc.getDocumentElement().getNodeName().equals(AcademyConstants.ELE_ORDER)) {
				strExtnTransactionID = XMLUtil.getAttributeFromXPath(inDoc, AcademyConstants.XPATH_ATTR_RETURN_ORDER_EXTN_TRANSACTIONID);
			}else {
				strExtnTransactionID = XMLUtil.getAttributeFromXPath(inDoc, AcademyConstants.XPATH_ATTR_RETURN_ORDERINVOICE_EXTN_TRANSACTIONID);
			}		
			if(!YFSObject.isVoid(strExtnTransactionID)) {		
				strKountStatusInput = prepareKountWSRequestInput(strExtnTransactionID, AcademyConstants.STR_KOUNT_STATUS_R, null);
				strURLType = AcademyConstants.STR_KOUNT_URL_RFCB;
			}
			else {
				YFSException yfsExec = new YFSException("Mandatory parameters are missing : ExtnTransactionID");
				yfsExec.setErrorCode(AcademyConstants.ERR_CODE_20);
				throw yfsExec;
			}
		}
		//End : FPT-22,28,54
		logger.verbose(":: strKountInput :: "+strKountStatusInput);
		if(strKountStatusInput != null) {
			AcademyInvokeKountWebService.invokeWebservice(strKountStatusInput, strURLType);
		}
		logger.verbose("End AcademyOrderFraudStatusUpdateToKount.fraudStatusUpdateToKount()");
	}


	/** This method prepares webservice input for status/rfcb
	 * @param strExtnTransactionID
	 * @param strKountStatus
	 * @return
	 * @throws Exception
	 */
	/**Json input to Kount::
	 * if:staus {"status[ExtnTransactionId]=A/D&reason[ExtnTransactionId]=OMS_ACCEPT/OMS_FRAUD"} 
	 * if:rfcb  {"rfcb[strExtnTransactionID]="+R;}
	 */
	private String prepareKountWSRequestInput(String strExtnTransactionID, String strKountStatus, String strReasonCode) throws Exception {

		logger.verbose("Begin AcademyOrderFraudStatusUpdateToKount.prepareKountStatusWebserviceRequest()");
		String strKountStatusInput = null; 
		//Start : FPT-22,28,54
		if(strKountStatus.equals(AcademyConstants.STR_KOUNT_STATUS_R)) {
			strKountStatusInput = "rfcb["+strExtnTransactionID+"]="+strKountStatus;
		}//End : FPT-22,28,54
		else {
			strKountStatusInput = "status["+strExtnTransactionID+"]="+strKountStatus+"&reason["+strExtnTransactionID+"]="+strReasonCode;
		}
		logger.verbose("End AcademyOrderFraudStatusUpdateToKount.prepareKountStatusWebserviceRequest()");
		return strKountStatusInput;
	}
}
