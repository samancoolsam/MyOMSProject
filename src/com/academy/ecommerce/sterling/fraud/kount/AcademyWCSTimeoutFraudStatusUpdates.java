package com.academy.ecommerce.sterling.fraud.kount;

/*##################################################################################
 *
 * Project Name                : Kount Integration
 * Module                      : OMS
 * Author                      : CTS
 * Date                        : 22-MAY-2019 
 * Description				   : This class does following 
 * 								  1.OMS Receive  messages from WCS which has timeout hold
 * 									on the Order.
 * 								  2.If the Message contains Action="MODIFY" or "CANCEL" then 
 * 									changeOrder API is called to resolve/place the 
 *  								hold on the Order
 * 								  
 * 
 * 
 * Change Revision
 * ---------------------------------------------------------------------------------
 * Date            Author         		Version#       Remarks/Description                      
 * ---------------------------------------------------------------------------------
 * 22-MAY-2019		CTS  	 			  1.0           	Initial version
 * ##################################################################################*/


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
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyWCSTimeoutFraudStatusUpdates {

	private static Logger log = Logger
			.getLogger(AcademyWCSTimeoutFraudStatusUpdates.class.getName());


	/**
	 * FPT - 10 This method process the WCS Updates from Kount and updates
	 * the fraud status in OMS.
	 * 
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public Document getWCSFraudStatusUpdates(YFSEnvironment env, Document inDoc)
			throws Exception {
		log.verbose("***************Entering AcademyWCSTimeoutFraudStatusUpdates.getWCSFraudStatusUpdates()***************");

		Document outDoc = null;
		Document getOrderListoutDoc = null;

		Element eleInputExtn = null;

		String strExtnTransactionID = null;
		
		eleInputExtn=XMLUtil.getFirstElementByName(inDoc.getDocumentElement(), AcademyConstants.ELE_EXTN);
		String strOrderNo = XMLUtil.getAttributeFromXPath(inDoc,AcademyConstants.XPATH_ORDER_NO);
		log.verbose("The value of Order No from WCS input XML is "+ strOrderNo);

		if (!YFCObject.isVoid(eleInputExtn)) {
			strExtnTransactionID = XMLUtil.getAttributeFromXPath(inDoc,AcademyConstants.XPATH_EXTN_TRANSACTIONID );					
		}
		
		if (!YFCObject.isVoid(strOrderNo)) {
			log.verbose("The value of the Order No and ExtnTransactionID are "
					+ strOrderNo +"and" +strExtnTransactionID);

		// Call the getOrderList API to get the  HoldType from Sterling on the Order came from WCS input
		getOrderListoutDoc = callgetOrderList(env, strOrderNo);
		
		String strSterlingHoldType = XMLUtil.getAttributeFromXPath(getOrderListoutDoc,AcademyConstants.XPATH_GETORDERLIST_ORDER_HOLD_TYPE);
		log.verbose("The value of the strSterlingHoldType is "+ strSterlingHoldType);
		

			if (((AcademyConstants.STR_FRAUD_NOCHECK_KOUNT).equals(strSterlingHoldType))) {
				log.verbose("The hold Types are matching and we are calling changeOrder API");
				
					log.verbose("changeOrder API Input is " + XMLUtil.getString(inDoc));
							
					outDoc = AcademyUtil.invokeAPI(env,AcademyConstants.API_CHANGE_ORDER, inDoc);
					log.verbose("changeOrder API Output is "
							+ XMLUtil.getString(outDoc));			
			} 
			else{
				String errorDescription = "The ENS update might have resolved the FRAUD_NOCHECK_KOUNT Hold on order.Please validate and ignore the message.";
				YFSException yfse = new YFSException();
				yfse.setErrorCode(AcademyConstants.ERR_CODE_22);
				yfse.setErrorDescription(errorDescription);
				throw yfse;				
			}
		}
		else {
			String errorDescription = "The OrderNo is not present in the WCS Input Message.";
			YFSException yfse = new YFSException();
			yfse.setErrorCode(AcademyConstants.ERR_CODE_20);
			yfse.setErrorDescription(errorDescription);
			throw yfse;
		}
	
		log.verbose("End of AcademyWCSTimeoutFraudStatusUpdates.getWCSFraudStatusUpdates() method");
		return outDoc;
	}

	private Document callgetOrderList(YFSEnvironment env, String OrderNo)
			throws IllegalArgumentException, Exception {

		log.verbose("*************************Entering AcademyWCSTimeoutFraudStatusUpdates.callgetOrderList()*************************");
		Document docgetOrderListInput = null;
		Document getOrderListTemplate = null;
		Document getOrderListOutputDoc = null;

		log.verbose("**********Order No is" + OrderNo + "**********");
		docgetOrderListInput = XMLUtil.getDocument("<Order OrderNo='" + OrderNo
				+ "' DocumentType='0001' EnterpriseCode='Academy_Direct' />");

		log.verbose("Calling getOrderList API Input: ="
				+ XMLUtil.getString(docgetOrderListInput));

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
				+ XMLUtil.getString(getOrderListOutputDoc));

		NodeList NLgetOrderList = getOrderListOutputDoc
				.getElementsByTagName(AcademyConstants.ELE_ORDER);
		if (NLgetOrderList.getLength() <= 0) {

			String errorDescription = "Invalid Order";
			YFSException yfse = new YFSException();
			yfse.setErrorCode(AcademyConstants.ERR_CODE_21);
			yfse.setErrorDescription(errorDescription);
			throw yfse;
		}

		log.verbose("*************************End of AcademyWCSTimeoutFraudStatusUpdates.callgetOrderList()");
		return getOrderListOutputDoc;
	}

}
