package com.academy.ecommerce.sterling.util;

import java.text.DecimalFormat;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.ConcurrentMap;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * This is a Utility Method for Paypal decoupling calls
 * 
 * @author C0028732
 *
 */

public class AcademyPaypalPaymentProcessingUtil {
	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPaypalPaymentProcessingUtil.class);

	/**
	 * This method will create json input for direct paypal call based on
	 * transaction type
	 * 
	 * @param inDoc
	 * @param strTransactionType
	 * @param strParameter
	 * @return
	 * @throws Exception
	 */
	public static String preparePaypalTransInp(Document inDoc, String strTransactionType, String strParameter) throws Exception {
		log.debug("Begin of AcademyPaypalPaymentProcessingUtil.preparePaypalTransInp() method after direct paypal.");
		if (AcademyConstants.TRANSACTION_TYPE_CAPTURE.equals(strTransactionType)) {
			return preparePaypalCaptureInp(inDoc, strParameter);
		}else if (AcademyConstants.TRANSACTION_TYPE_AUTHORIZE.equals(strTransactionType)||
				AcademyConstants.TRANSACTION_TYPE_REFUND.equals(strTransactionType) || AcademyConstants.TRANSACTION_TYPE_VOID.equals(strTransactionType)) {
			return preparePaypalAuthVoidRefundInp(inDoc);
		}
		return "";
	}
	
	

	/**
	 * This method will prepare PayPal JSON input for reauth
	 * 
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	private static String preparePaypalAuthVoidRefundInp(Document inDoc) throws Exception {
		log.debug("Begin of AcademyPaypalPaymentProcessingUtil.preparePaypalAuthVoidRefundInp() method after direct paypal.");

		String strRequestAmount = XPathUtil.getString(inDoc, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);

		DecimalFormat decFormat = new DecimalFormat("0.00");
		strRequestAmount = decFormat.format(Double.valueOf(strRequestAmount));
		String strJsonInput = "{\"amount\": {\"currency\": \"USD\",\"total\": \"" + strRequestAmount + "\"}}";
		log.debug("End of AcademyPaypalPaymentProcessingUtil.preparePaypalAuthVoidRefundInp() method after direct paypal.");
		return strJsonInput;
	}
	
	/**This method prepares the UE output document with a dummy Auth in case of a
	 * void failure scenarios
	 * 
	 * @param docPaymentIn
	 * @param elePaymentOut
	 * @param strAuthRespCode
	 * @param strAuthReasonMessage
	 * @param strTransRespCode
	 * @param strTransRespMsg
	 * @param strFinTransTrackID
	 * @param strPaypalRequestID
	 * @param strErrorDesc
	 * @param strResponseID
	 * @return
	 * @throws Exception
	 */
	private static Element prepareDummyVoidResponse(Document docPaymentIn, Element elePaymentOut, String strAuthRespCode, String strAuthReasonMessage,
			String strTransRespCode,String strTransRespMsg, String strFinTransTrackID, String strPaypalRequestID,String strErrorDesc,String strResponseID) throws Exception {
		log.debug("Begin of AcademyPaypalPaymentProcessingUtil.prepareDummyVoidResponse() method  after direct paypal.");
		log.verbose("Paypal Direct:: docPaymentIn:" + XMLUtil.getXMLString(docPaymentIn));
		log.verbose("Paypal Direct:: elePaymentOut:" + XMLUtil.getElementXMLString(elePaymentOut));
		log.verbose("Paypal Direct:: strAuthRespCode: " + strAuthRespCode);
		log.verbose("Paypal Direct:: strAuthReasonMessage:" + strAuthReasonMessage);
		log.verbose("Paypal Direct:: strTransRespCode:" + strTransRespCode);
		log.verbose("Paypal Direct:: strTransRespMsg:" + strTransRespMsg);
		log.verbose("Paypal Direct:: strFinTransTrackID:" + strFinTransTrackID);
		log.verbose("Paypal Direct:: strPaypalRequestID:" + strPaypalRequestID);
		log.verbose("Paypal Direct:: strErrorDesc:" + strErrorDesc);
		log.verbose("Paypal Direct:: strResponseID:" + strResponseID);
		String strReqAmt = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);
		String strTranType = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CHARGE_TYPE);
		log.verbose("Paypal Direct:: strReqAmt:" + strReqAmt);
		log.verbose("Paypal Direct:: strTranType:" + strTranType);
		// Dummy Auth since void had been declined
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_TYPE, strTranType);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_IS_DUMMY_SETTLEMENT, AcademyConstants.STR_YES);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_STATUS_CODE, AcademyConstants.STR_NO);// To raise alert using existing service
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, AcademyConstants.STR_DUMMY_AUTH);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_CODE, AcademyConstants.STR_DUMMY_AUTH);
		
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_AMT, strReqAmt);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_AMOUNT, strReqAmt);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_APPROVED);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, strAuthRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, strAuthReasonMessage);
		
		// Contains HttpStatusCode Response Code and Message
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE, strTransRespCode);//eg. HttpStatusCode=400
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_MESSAGE, strTransRespMsg);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_CODE, strFinTransTrackID);//Paypal-Debug-ID
		elePaymentOut.setAttribute(AcademyConstants.ATTR_REQUEST_ID, strPaypalRequestID);//Paypal-Request-ID
		elePaymentOut.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_FLAG, strResponseID);//Paypal-Response Id for 2xx
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN_NEW);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_TIME, sdf.format(cal.getTime()));
		// Setting order details to raise alert
		elePaymentOut.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY,
				XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_HEADER_KEY));
		elePaymentOut.setAttribute(AcademyConstants.ATTR_ORDER_NO,
				XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_NO));
		elePaymentOut.setAttribute(AcademyConstants.ATTR_PAYMENT_TYPE,
				XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH + AcademyConstants.PAYMENT_TYPE_XPATH));
		
		elePaymentOut.setAttribute(AcademyConstants.ATTR_REASON_CODE, strAuthRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_REASON_DESC, strAuthReasonMessage);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_ERROR_NO, strTransRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_ERROR_DESC_SHORT, strErrorDesc);
		log.debug("End of AcademyPaypalPaymentProcessingUtil.prepareDummyVoidResponse() method  after direct paypal.");
		return elePaymentOut;
	}

	/**This method prepares the UE output document with a dummy settlement in case
	 * of a capture denied scenarios
	 * 
	 * @param docPaymentIn
	 * @param elePaymentOut
	 * @param strAuthRespCode
	 * @param strAuthReasonMessage
	 * @param strTransRespCode
	 * @param strTransRespMsg
	 * @param strFinTransTrackID
	 * @param strPaypalRequestID
	 * @param strErrorDesc
	 * @param strResponseID
	 * @return
	 * @throws Exception
	 */
	private static Element prepareDummyCaptureResponse(Document docPaymentIn, Element elePaymentOut, String strAuthRespCode, String strAuthReasonMessage,
			String strTransRespCode,String strTransRespMsg,String strFinTransTrackID, String strPaypalRequestID,String strErrorDesc,String strResponseID) throws Exception {
		log.debug("Begin of AcademyPaypalPaymentProcessingUtil.prepareDummyCaptureResponse() method" + " after direct paypal.");
		log.verbose("Paypal Direct:: docPaymentIn:" + XMLUtil.getXMLString(docPaymentIn));
		log.verbose("Paypal Direct:: elePaymentOut:" + XMLUtil.getElementXMLString(elePaymentOut));
		log.verbose("Paypal Direct:: strAuthRespCode: " + strAuthRespCode);
		log.verbose("Paypal Direct:: strAuthReasonMessage:" + strAuthReasonMessage);
		log.verbose("Paypal Direct:: strTransRespCode:" + strTransRespCode);
		log.verbose("Paypal Direct:: strTransRespMsg:" + strTransRespMsg);
		log.verbose("Paypal Direct:: strFinTransTrackID:" + strFinTransTrackID);
		log.verbose("Paypal Direct:: strPaypalRequestID:" + strPaypalRequestID);
		log.verbose("Paypal Direct:: strErrorDesc:" + strErrorDesc);
		log.verbose("Paypal Direct:: strResponseID:" + strResponseID);
		String strReqAmt = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);
		String strTranType = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CHARGE_TYPE);
		log.verbose("Paypal Direct:: strReqAmt:" + strReqAmt);
		log.verbose("Paypal Direct:: strTranType:" + strTranType);
		// Dummy settlement since capture had been declined
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_TYPE, strTranType);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_IS_DUMMY_SETTLEMENT, AcademyConstants.STR_YES);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_STATUS_CODE, AcademyConstants.STR_NO);// To raise alert using
																								// existing service
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, AcademyConstants.STR_DUMMY_SETTLEMENT);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_CODE, AcademyConstants.STR_DUMMY_SETTLEMENT);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_AMT, strReqAmt);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_AMOUNT, strReqAmt);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_APPROVED);
		
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, strAuthRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, strAuthReasonMessage);
		// Contains HttpStatusCode Response Code and Message
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE, strTransRespCode);//eg. HttpStatusCode=400
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_MESSAGE, strTransRespMsg);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_CODE, strFinTransTrackID);//Paypal-Debug-ID
		elePaymentOut.setAttribute(AcademyConstants.ATTR_REQUEST_ID, strPaypalRequestID);//Paypal-Request-ID
		elePaymentOut.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_FLAG, strResponseID);//Paypal-Response Id for 2xx but denied
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN_NEW);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_TIME, sdf.format(cal.getTime()));
		// Setting order details to raise alert
		elePaymentOut.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY,
				XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_HEADER_KEY));
		elePaymentOut.setAttribute(AcademyConstants.ATTR_ORDER_NO,
				XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_NO));
		elePaymentOut.setAttribute(AcademyConstants.ATTR_PAYMENT_TYPE,
				XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH + AcademyConstants.PAYMENT_TYPE_XPATH));
		elePaymentOut.setAttribute(AcademyConstants.ATTR_REASON_CODE, strAuthRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_REASON_DESC, strAuthReasonMessage);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_ERROR_NO, strTransRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_ERROR_DESC_SHORT, strErrorDesc);//
		log.debug("End of AcademyPaypalPaymentProcessingUtil.prepareDummyCaptureResponse() method after direct paypal.");
		return elePaymentOut;
	}

	/**This method prepares the UE output document with a dummy settlement in case
	 * of a purchase denied scenarios
	 * @param docPaymentIn
	 * @param elePaymentOut
	 * @param strAuthRespCode
	 * @param strAuthReasonMessage
	 * @param strTransRespCode
	 * @param strTransRespMsg
	 * @param strFinTransTrackID
	 * @param strPaypalRequestID
	 * @param strErrorDesc
	 * @param strResponseID
	 * @return
	 * @throws Exception
	 */
	private static Element prepareDummyPurchaseResponse(Document docPaymentIn, Element elePaymentOut, String strAuthRespCode, String strAuthReasonMessage,
			String strTransRespCode, String strTransRespMsg,String strFinTransTrackID, String strPaypalRequestID,String strErrorDesc,String strResponseID) throws Exception {
		log.debug("Begin of AcademyPaypalPaymentProcessingUtil.prepareDummyPurchaseResponse() method after direct paypal.");
		log.verbose("Paypal Direct:: docPaymentIn:" + XMLUtil.getXMLString(docPaymentIn));
		log.verbose("Paypal Direct:: elePaymentOut:" + XMLUtil.getElementXMLString(elePaymentOut));
		log.verbose("Paypal Direct:: strAuthRespCode:" + strAuthRespCode);
		log.verbose("Paypal Direct:: strAuthReasonMessage:" + strAuthReasonMessage);
		log.verbose("Paypal Direct:: strTransRespCode:" + strTransRespCode);
		log.verbose("Paypal Direct:: strTransRespMsg:" + strTransRespMsg);
		log.verbose("Paypal Direct:: strFinTransTrackID:" + strFinTransTrackID);
		log.verbose("Paypal Direct:: strPaypalRequestID:" + strPaypalRequestID);
		log.verbose("Paypal Direct:: strErrorDesc:" + strErrorDesc);
		log.verbose("Paypal Direct:: strResponseID:" + strResponseID);
		String strReqAmt = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);
		String strTranType = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CHARGE_TYPE);
		log.verbose("Paypal Direct:: strReqAmt:" + strReqAmt);
		log.verbose("Paypal Direct:: strTranType:" + strTranType);
		// Dummy settlement since Purchase had been denied
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_TYPE, strTranType);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_IS_DUMMY_SETTLEMENT, AcademyConstants.STR_YES);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_STATUS_CODE, AcademyConstants.STR_NO);// To raise alert using existing service
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, AcademyConstants.STR_DUMMY_SETTLEMENT);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_CODE, AcademyConstants.STR_DUMMY_SETTLEMENT);
		
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_AMT, strReqAmt);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_AMOUNT, strReqAmt);

		
		elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_APPROVED);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, strAuthRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, strAuthReasonMessage);

		// Contains HttpStatusCode Response Code and Message
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE, strTransRespCode);//eg. HttpStatusCode=400
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_MESSAGE, strTransRespMsg);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_CODE, strFinTransTrackID);//Paypal-Debug-ID
		
		elePaymentOut.setAttribute(AcademyConstants.ATTR_REQUEST_ID, strPaypalRequestID);//Paypal-Request-ID
		elePaymentOut.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_FLAG, strResponseID);//Paypal-Response Id for 2xx
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN_NEW);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_TIME, sdf.format(cal.getTime()));

		// Reset PaymentReference9 is already set
		elePaymentOut.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_9, "");

		// Setting order details to raise alert
		elePaymentOut.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY,
				XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_HEADER_KEY));
		elePaymentOut.setAttribute(AcademyConstants.ATTR_ORDER_NO,
				XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_NO));
		elePaymentOut.setAttribute(AcademyConstants.ATTR_PAYMENT_TYPE,
				XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH + AcademyConstants.PAYMENT_TYPE_XPATH));
		// Below details will be shown in Alert console
		elePaymentOut.setAttribute(AcademyConstants.ATTR_REASON_CODE, strAuthRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_REASON_DESC, strAuthReasonMessage);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_ERROR_NO, strTransRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_ERROR_DESC_SHORT, strErrorDesc);
		log.debug("End of AcademyPaypalPaymentProcessingUtil.prepareDummyPurchaseResponse() method after direct paypal.");
		return elePaymentOut;
	}


	/**
	 * This method provides the next collection date for Payment Agent to process
	 * 
	 * @param strNxtTrigger
	 * @return
	 */
	public static String nextCollectionDate(String strNxtTrigger) {
		log.debug("Begin of AcademyPaypalPaymentProcessingUtil.nextCollectionDate() method after direct paypal.");
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN_1);
		int iNextTriggerIntervalInMin = Integer.parseInt(strNxtTrigger);
		cal.add(Calendar.MINUTE, iNextTriggerIntervalInMin);
		String strCollectionDate = sdf.format(cal.getTime());
		log.debug("End of AcademyPaypalPaymentProcessingUtil.nextCollectionDate() method after direct paypal.");
		return strCollectionDate;
	}

	

	/**
	 * This method prepares the UE output document with a dummy auth in case of a
	 * reauth failure scenarios
	 * 
	 * @param elePaymentOut
	 * @param strAuthRespCode
	 * @param strAuthReasonMessage
	 * @param strTransRespCode
	 * @param strTransMessage
	 * @param strPaypalDebugId
	 * @return
	 * @throws Exception
	 */
	public static Element prepareDummyAuthResponse(Element elePaymentOut, String strAuthRespCode, String strAuthReasonMessage,
			String strTransRespCode, String strTransMessage, String strPaypalDebugId) throws Exception {

		log.debug("Begin of AcademyPaypalPaymentProcessingUtil.prepareDummyAuthResponse() method  after direct paypal.");
		elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, strAuthRespCode);
		
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, strAuthRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, strAuthReasonMessage);

		//Contains Gateway Response Code and Message
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE, strTransRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_MESSAGE, strTransMessage);

		//Contains the ID for reference to reach out contact Payeezy for data against this request
		elePaymentOut.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_CODE, strPaypalDebugId);

		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN_NEW);
		Calendar cal = Calendar.getInstance();
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_TIME, sdf.format(cal.getTime()));

		log.debug("End of AcademyPaypalPaymentProcessingUtil.prepareDummyAuthResponse() method after direct paypal.");
		return elePaymentOut;
	}

	

	/**
	 * This method prepares the UE output document in case of APPROVED scenarios
	 * 
	 * @param docPaymentIn
	 * @param elePaymentOut
	 * @param docRespPaypal
	 * @param strTransactionType
	 * @param strPPAuthExpiryDays
	 * @return
	 * @throws Exception
	 */
	public static Element prepareApprovedResp(Document docPaymentIn, Element elePaymentOut, Document docRespPaypal, String strTransactionType,
			String strPPAuthExpiryDays) throws Exception {
		log.debug("Begin of AcademyPaypalPaymentProcessingUtil.prepareApprovedResp() method after direct paypal.");
		log.verbose("Paypal Direct:: docPaymentIn:"+XMLUtil.getXMLString(docPaymentIn));
		log.verbose("Paypal Direct:: docRespPaypal:"+XMLUtil.getXMLString(docRespPaypal));
		Element elePaypalResp = docRespPaypal.getDocumentElement();
		String strHttpStatusCode = elePaypalResp.getAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE);
		String strResponseId = elePaypalResp.getAttribute(AcademyConstants.PAYPAL_RESPONSE_ID);
		String strAmount = XMLUtil.getString(elePaypalResp, AcademyConstants.JSON_PP_ATTR_AMOUNT);
		String strTotRefundedAmount = elePaypalResp.getAttribute(AcademyConstants.JSON_ATTR_AMOUNT);
		String strState = elePaypalResp.getAttribute(AcademyConstants.ATTR_PAYPAL_RESPONSE_STATE);
		String strStateReason = elePaypalResp.getAttribute(AcademyConstants.JSON_ATTR_STATE_REASON);
		String strRequestAmount = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);
		String strResponseTime = docRespPaypal.getDocumentElement().getAttribute(AcademyConstants.ATTR_PAYPAL_RESPONSE_CREATE_TIME);
		String strAuthorizationId = docRespPaypal.getDocumentElement().getAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID);

		if(AcademyConstants.TRANSACTION_TYPE_REFUND.equalsIgnoreCase(strTransactionType))
		{
			strAmount = AcademyConstants.STR_HYPHEN + strTotRefundedAmount;
		}
		else if(AcademyConstants.TRANSACTION_TYPE_VOID.equalsIgnoreCase(strTransactionType))
		{
			strAmount = strRequestAmount;
		}
		
		elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_APPROVED);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_CODE, strResponseId);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_AMOUNT, strAmount);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_AMT, strAmount);
		// Setting the AuthId details in AUth Return Code.
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, strState);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, strStateReason);

		// For Reauth Scenario Update Auth ID
		if(AcademyConstants.TRANSACTION_TYPE_AUTHORIZE.equalsIgnoreCase(strTransactionType)) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, strResponseId);			
		}else if(AcademyConstants.TRANSACTION_TYPE_PURCHASE.equalsIgnoreCase(strTransactionType))
		{
			log.verbose("Paypal Direct:: prepareRestSuccessfulResp :: strAuthorizationId: " + strAuthorizationId);
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, strAuthorizationId);
		}else {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, docPaymentIn.getDocumentElement().getAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID));
		}
		
		int iExpirationDays = Integer.parseInt(strPPAuthExpiryDays);
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN_NEW);
		String strExpirationDate = "";
		if (!YFSObject.isVoid(strResponseTime)) {
			log.verbose("Paypal Direct:: setting actual Auth time");
			SimpleDateFormat sdfSource = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			Date date = sdfSource.parse(strResponseTime);
			strResponseTime = sdf.format(date);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.DATE, iExpirationDays);
			strExpirationDate = sdf.format(cal.getTime());
		} else {
			log.verbose("Paypal Direct:: Setting current time");
			Calendar cal = Calendar.getInstance();
			strResponseTime = sdf.format(cal.getTime());
			cal.add(Calendar.DATE, iExpirationDays);
			strExpirationDate = sdf.format(cal.getTime());
		}
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_TIME, strResponseTime);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_EXP_DATE, strExpirationDate);

		if(AcademyConstants.TRANSACTION_TYPE_PURCHASE.equalsIgnoreCase(strTransactionType))
		{
			String[] msgArray = elePaypalResp.getAttribute(AcademyConstants.STR_PAYPAL_REQUEST_ID).split(AcademyConstants.STR_AT);
			if (msgArray.length>1) {
				elePaymentOut.setAttribute(AcademyConstants.ATTR_REQUEST_ID, msgArray[0]);
			}
		}else {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_REQUEST_ID, elePaypalResp.getAttribute(AcademyConstants.STR_PAYPAL_REQUEST_ID));
		}
		
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE, strHttpStatusCode);
		//Start OMNI-72334 - Added as a part of JIRA PaypalRequestID
		
		elePaymentOut.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_CODE, elePaypalResp.getAttribute(AcademyConstants.ATTR_PAYPAL_DEBUG_ID));
		//End OMNI-72334 - Added as a part of JIRA PaypalRequestID
				
		log.verbose("Paypal Direct:: AcademyProcessPaypalPaymentAPI.prepareApprovedResp() method" + XMLUtil.getElementXMLString(elePaymentOut));
		log.debug("End of AcademyPaypalPaymentProcessingUtil.prepareApprovedResp() method after direct paypal.");
		return elePaymentOut;
	}

	/**
	 * This method prepare retry transaction element
	 * 
	 * @param docPaymentOut
	 * @param strNextTriggerIntervalInMin
	 * @return
	 */

	public static Element retryTransaction(Element elePaymentOut, String strNextTriggerIntervalInMin,String strErrorName,String strRespCode) {
		log.debug("Begin of AcademyPaypalPaymentProcessingUtil.retryTransaction() method after direct paypal.");
		log.verbose("Paypal Direct:: strNextTriggerIntervalInMin: " + strNextTriggerIntervalInMin);		
		elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, strRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_YES);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, strErrorName);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_COLLECTION_DATE, nextCollectionDate(strNextTriggerIntervalInMin));
		elePaymentOut.setAttribute(AcademyConstants.ATTR_SUSPEND_PAYMENT, AcademyConstants.STR_NO);
		log.debug("End of AcademyPaypalPaymentProcessingUtil.retryTransaction() method after direct paypal.");
		return elePaymentOut;

	}

	/**
	 * This method generates a 36 digit token using JAVA UUID
	 * 
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public static String getAutoGeneratedUUID() throws Exception {
		log.debug("Begin of AcademyPaypalPaymentProcessingUtil.getUUID() method");

		UUID uuid = UUID.randomUUID();
		String randomUUIDString = AcademyConstants.STR_PP+uuid.toString();
		log.verbose("Paypal Direct:: randomUUIDString ::" + randomUUIDString);
		log.debug("End of AcademyPaypalPaymentProcessingUtil.getUUID() method");
		return randomUUIDString;
	}

	/**
	 * This method logs the Paypal request and response to database based on the
	 * Paypal response code
	 * @param env
	 * @param docPayInput
	 * @param docReqPaypal
	 * @param docRespPaypal
	 * @param strDBServiceName
	 * @throws Exception
	 */
	public static void logPaypalReqAndRespToDB(YFSEnvironment env, Document docPayInput, Document docReqPaypal, Document docRespPaypal, String strDBServiceName)
			throws Exception {
		log.debug("Start AcademyPaypalPaymentProcessingUtil.logPaypalReqAndRespToDB() method after direct paypal.");
		Document docPaypalReqResp = XMLUtil.createDocument(AcademyConstants.ELE_PAYPAL_REQ_RESP);
		Element elePaypalReqResp = docPaypalReqResp.getDocumentElement();

		if (!YFSObject.isVoid(docReqPaypal)) {
			Element elePaypalReq = docPaypalReqResp.createElement(AcademyConstants.ELE_PAYPAL_REQ);
			Element elePaypalReqTemp = docReqPaypal.getDocumentElement();
			elePaypalReqResp.appendChild(elePaypalReq);
			XMLUtil.importElement(elePaypalReq, elePaypalReqTemp);
		}

		if (!YFSObject.isVoid(docRespPaypal)) {
			Element elePaypalResp = docPaypalReqResp.createElement(AcademyConstants.ELE_PAYPAL_RESP);
			Element elePaypalRespTemp = docRespPaypal.getDocumentElement();
			elePaypalReqResp.appendChild(elePaypalResp);
			XMLUtil.importElement(elePaypalResp, elePaypalRespTemp);
		}
		Element elePayInput = docPaypalReqResp.createElement(AcademyConstants.ELE_PAYMENT_INPUT);
		Element elePayInputTemp = docPayInput.getDocumentElement();
		elePaypalReqResp.appendChild(elePayInput);
		XMLUtil.importElement(elePayInput, elePayInputTemp);
		log.verbose("Paypal Direct:: logPaypalReqAndRespToDB() method docPaypalReqResp::" + XMLUtil.getXMLString(docPaypalReqResp));
		AcademyUtil.invokeService(env, strDBServiceName, docPaypalReqResp);
		log.debug("End AcademyPaypalPaymentProcessingUtil.logPaypalReqAndRespToDB() method  after direct paypal.");
	}
	
	/**
	 * This method is used to form payment error element for purchase capture call
	 * fail but auth successful for capture retry
	 * 
	 * @param elePaymentOut
	 * @param strPayPalAuth
	 * @return
	 * @throws Exception
	 */
	public static Element updateFailedForPurchase(Element elePaymentOut, String strPayPalAuth) throws Exception {
		log.debug("Begin of AcademyPaypalPaymentProcessingUtil.updateFailedForPurchase() method after direct paypal.");
		log.verbose("Paypal Direct:: elePaymentOut:"+XMLUtil.getElementXMLString(elePaymentOut));
		log.verbose("Paypal Direct:: strPayPalAuth:"+strPayPalAuth);
		Element elePaymentErrorList = (Element) elePaymentOut.getElementsByTagName(AcademyConstants.ELE_PAYMENT_TRAN_ERROR_LIST).item(0);

		// Remove any error handling done
		if (!YFCObject.isVoid(elePaymentErrorList)) {
			elePaymentOut.removeChild(elePaymentErrorList);
			elePaymentErrorList = elePaymentOut.getOwnerDocument().createElement(AcademyConstants.ELE_PAYMENT_TRAN_ERROR_LIST);
			elePaymentOut.appendChild(elePaymentErrorList);
		} else {
			elePaymentErrorList = elePaymentOut.getOwnerDocument().createElement(AcademyConstants.ELE_PAYMENT_TRAN_ERROR_LIST);
			elePaymentOut.appendChild(elePaymentErrorList);
		}

		Element elePaymentError = elePaymentOut.getOwnerDocument().createElement(AcademyConstants.ELE_PAYMENT_TRAN_ERROR);
		elePaymentErrorList.appendChild(elePaymentError);

		elePaymentError.setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, AcademyConstants.STR_PAYPAL_PURCHASE);
		elePaymentError.setAttribute(AcademyConstants.ATTR_MESSAGE, strPayPalAuth);

		log.debug("End of AcademyPaypalPaymentProcessingUtil.updateFailedForPurchase() method after direct paypal.");
		return elePaymentError;
	}
	
	/**
	 * This method retrieves the split shipment count based on the payment Reference
	 * 9 field.
	 * 
	 * @param docPaymentInp
	 * @return
	 * @throws Exception
	 */
	public static String retrieveSplitShipmentCount(Document docPaymentInp) throws Exception {
		log.debug("Begin of AcademyPaypalPaymentProcessingUtil.retrieveSplitShipmentCount() method after direct paypal.");
		log.verbose("Paypal Direct:: docPaymentInp:"+docPaymentInp);
		int iNoOfShipments = 1;
		String strSplitShipment = XPathUtil.getString(docPaymentInp, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_PAYMENT_REF_9);
		String strCurrentAuthAmount = XPathUtil.getString(docPaymentInp,
				AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CURRENT_AUTHORIZATION_AMT);
		String strRequestAmount = XPathUtil.getString(docPaymentInp, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);
		String strAuthAmount = XPathUtil.getString(docPaymentInp, "/Payment/CreditCardTransactions/CreditCardTransaction/@AuthAmount");
		if ((StringUtil.isEmpty(strAuthAmount))) {
			strAuthAmount = "0.00";
		}

		DecimalFormat df = new DecimalFormat("0.00");
		strCurrentAuthAmount = df.format(Double.parseDouble(strCurrentAuthAmount));
		strAuthAmount = df.format(Double.parseDouble(strAuthAmount));

		if (!YFCObject.isVoid(strSplitShipment)) {
			iNoOfShipments = Integer.parseInt(strSplitShipment.split("/")[0]) + 1;
		} else {
			log.verbose("Paypal Direct:: First shipment on line.");
		}

		// Check if the Shipment is last shipment on Order
		if (Double.parseDouble(strRequestAmount) == Double.parseDouble(strCurrentAuthAmount)) {
			log.verbose("Paypal Direct:: First of Last Shipment on Order.");
			strSplitShipment = Integer.toString(iNoOfShipments) + "/" + Integer.toString(iNoOfShipments);

		} else if ((!YFCObject.isVoid(strCurrentAuthAmount) && Double.parseDouble(strCurrentAuthAmount) == 0)
				|| (!YFCObject.isVoid(strAuthAmount) && Double.parseDouble(strAuthAmount) == 0)) {
			log.verbose("Paypal Direct:: Last shipment on Auth.");
			strSplitShipment = Integer.toString(iNoOfShipments) + "/" + Integer.toString(iNoOfShipments);

		} else {
			log.verbose("Paypal Direct:: Order yet to be shipped completely. Partially shipped");
			strSplitShipment = Integer.toString(iNoOfShipments) + "/99";
		}

		log.debug("End of AcademyPaypalPaymentProcessingUtil.retrieveSplitShipmentCount() method after direct paypal.");
		return strSplitShipment;
	}
	//Start : OMNI-57724
		/**
		 * This method will prepare capture json input
		 * 
		 * @param inDoc
		 * @param strSplitShipment
		 * @return
		 * @throws Exception
		 */
		private static String preparePaypalCaptureInp(Document inDoc, String strSplitShipment) throws Exception {
			log.debug("Begin of AcademyPaypalPaymentProcessingUtil.preparePaypalCaptureInp() after direct paypal.");
			log.debug("Paypal Direct:: inDoc:"+inDoc);
			log.debug("Paypal Direct:: strSplitShipment:"+strSplitShipment);
			String strRequestAmount = XPathUtil.getString(inDoc, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);
			boolean boolIsFinalCap = YFCObject.isVoid(strSplitShipment)?false:isFinalCapture(strSplitShipment);
			DecimalFormat decFormat = new DecimalFormat("0.00");
			strRequestAmount = decFormat.format(Double.valueOf(strRequestAmount));
			String strJsonInput = "{\"amount\": {\"currency\": \"USD\",\"total\": \"" + strRequestAmount + "\"},\"is_final_capture\":" + boolIsFinalCap + "}";
			log.debug("End of AcademyPaypalPaymentProcessingUtil.preparePaypalCaptureInp() method after direct paypal.");
			return strJsonInput;
		}
		//End : OMNI-57724
		//Start : OMNI-57724
		/**
		 * This method will find final shipment or not for capture call
		 * 
		 * @param strSplitShipment
		 * @return
		 */
		private static boolean isFinalCapture(String strSplitShipment) {
			log.debug("Begin of AcademyPaypalPaymentProcessingUtil.isFinalCapture() method  after direct paypal.");
			log.debug("Paypal Direct:: strSplitShipment:"+strSplitShipment);
			String[] arrOfStr = strSplitShipment.split("/");
			if(arrOfStr.length>1 && arrOfStr[0].equals(arrOfStr[1])) {
				return true;
			} else
			{
				return false;
			}
		}
		//End : OMNI-57724
		
		//Start : OMNI-57431
		/**
		 * This method checks if there is a pending auth to settle.
		 * 
		 * @param env
		 * @param docPaymentIn
		 * @return
		 * @throws Exception
		 */
		public static boolean validateCompleteOrderCancel(Document docPaymentIn) throws Exception {
			log.debug("Begin of AcademyProcessPaypalPaymentAPI.validateCompleteOrderCancel() method  after direct paypal.");
			log.verbose("Paypal Direct:: docPaymentIn:" + XMLUtil.getXMLString(docPaymentIn));
			boolean bIsOrderPartiallySettled = false;
			String strCurrentAuthAmount = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CURRENT_AUTHORIZATION_AMT);
			DecimalFormat df = new DecimalFormat("0.00");
			strCurrentAuthAmount = df.format(Double.parseDouble(strCurrentAuthAmount));

			if (!YFCObject.isVoid(strCurrentAuthAmount)) {
				log.verbose("Paypal Direct:: Paypal Direct:: strCurrentAuthAmount:" + strCurrentAuthAmount);
				double dCurrentAuthAmt = Double.parseDouble(strCurrentAuthAmount);

				if (dCurrentAuthAmt != 0.00) {
					bIsOrderPartiallySettled = true;
				}
			}

			log.verbose("Paypal Direct:: bIsOrderPartiallySettled: " + bIsOrderPartiallySettled );
			log.debug("End of AcademyProcessPaypalPaymentAPI.validateCompleteOrderCancel() method  after direct paypal.");
			return bIsOrderPartiallySettled;
		}
		//OMNI-72334 Start
		/**This method will return true if a paypal response combination is valid for a CO prop type
		 * 
		 * @param errorTaskPropDtls
		 * @param propName
		 * @param strTransactionType
		 * @param strHttpStatusCode
		 * @param strParameter
		 * @return
		 */
		public static boolean isRestResponsePropType(ConcurrentMap<String, HashMap> errorTaskPropDtls, String propName, String strTransactionType,
				String strHttpStatusCode, String strParameter) {
			log.debug("Begin of AcademyPaypalPaymentProcessingUtil.isRestResponsePropType() method after direct paypal.");
			log.verbose("Paypal Direct:: propName:: " + propName + "strTransactionType:: " +strTransactionType +"strHttpStatusCode:: :"+strHttpStatusCode+"strParameter:: "+strParameter);
			HashMap<String, HashMap> mapHttpStatusMsgTransType = errorTaskPropDtls.get(propName);
			String mapHttpStatusCode = mapHttpStatusMsgTransType.containsKey(strHttpStatusCode) ? strHttpStatusCode : strHttpStatusCode.substring(0, 1) + AcademyConstants.STR_ALL_TYPE_XX;
			if (mapHttpStatusMsgTransType.containsKey(mapHttpStatusCode)) {
				String mapHttpStatus = mapHttpStatusMsgTransType.containsKey(strHttpStatusCode) ? strHttpStatusCode : strHttpStatusCode.substring(0, 1) + AcademyConstants.STR_ALL_TYPE_XX;
				log.verbose("Paypal Direct:: mapHttpStatus:: " + mapHttpStatus);
				HashMap<String, List> mapMsgTransType = mapHttpStatusMsgTransType.get(mapHttpStatus);
				// String could be X or exact match
				if (mapMsgTransType.containsKey(strParameter) || mapMsgTransType.containsKey(AcademyConstants.STR_ALL_TYPE_X)) {
					// String could be X or exact match
					String mapStrName = mapMsgTransType.containsKey(strParameter) ? strParameter : AcademyConstants.STR_ALL_TYPE_X;
					log.verbose("Paypal Direct:: mapStrName:: " + mapStrName);
					log.verbose("Paypal Direct:: mapMsgTransType:: " + mapMsgTransType.toString());
					// Get the list of transactions
					List<String> listTransType = mapMsgTransType.get(mapStrName);
					log.verbose("Paypal Direct:: listTransType.size():: " + listTransType.size());
					if (listTransType.contains(strTransactionType) || listTransType.contains(AcademyConstants.STR_ALL_TYPE_X)) {
						log.verbose("Paypal Direct:: returning true");
						return true;
					}
				}
			}
			log.verbose("Paypal Direct:: returning false");
			log.debug("End of AcademyPaypalPaymentProcessingUtil.isRestResponseErrorType() method after direct paypal.");
			return false;
		}
		//OMNI-72334 End
		//Start : OMNI-57906
		
		/**
		 * This method prepares a hash map from order-reference the captureid-amount
		 * refunded on an order
		 * 
		 * @param docGetOrderListOut
		 * @return
		 * @throws Exception
		 */
		public static HashMap<String, String> getRefundedAmount(Document docGetOrderListOut) throws Exception {
			log.debug("Begin of AcademyPaypalPaymentProcessingUtil.getRefundedAmount()  method after direct paypal.");
			log.verbose("Paypal Direct:: docGetOrderListOut:" + XMLUtil.getXMLString(docGetOrderListOut));
			HashMap<String, String> hmRefundedAmount = new HashMap<String, String>();
			DecimalFormat df = new DecimalFormat("0.00");

			NodeList nlReferences = XPathUtil.getNodeList(docGetOrderListOut.getDocumentElement(), "/OrderList/Order/References/Reference");
			for (int iRef = 0; iRef < nlReferences.getLength(); iRef++) {
				Element eleReference = (Element) nlReferences.item(iRef);
				String strName = eleReference.getAttribute("Name");
				if (strName.startsWith(AcademyConstants.TRANSACTION_TYPE_REFUND)) {
					String strValue = eleReference.getAttribute("Value");
					String strCaptureId = strName.split(AcademyConstants.STR_UNDERSCORE)[1];
					String strAmount = strValue;
					hmRefundedAmount.put(strCaptureId, df.format(Double.parseDouble(strAmount)));
				}
			}
			log.verbose("Paypal Direct:: hmRefundedAmount:" + hmRefundedAmount.size() + " \n " + hmRefundedAmount.toString());
			log.debug("End of AcademyPaypalPaymentProcessingUtil.getRefundedAmount() method after direct paypal.");
			return hmRefundedAmount;
		}
		
		/**
		 * This method prepares a hash map from charge transaction the
		 * captureid-settledamount of an order
		 * 
		 * @param docGetOrderListOut
		 * @param strPaymentKey
		 * @return
		 * @throws Exception
		 */
		public static HashMap<String, String> getSettledAmounts(Document docGetOrderListOut, String strPaymentKey) throws Exception {

			log.debug("Begin of AcademyPaypalPaymentProcessingUtil.getSettledAmounts() method after direct paypal.");
			HashMap<String, String> hmSettledAmount = new HashMap<String, String>();
			DecimalFormat df = new DecimalFormat("0.00");

			NodeList nlSettledCharges = XPathUtil.getNodeList(docGetOrderListOut.getDocumentElement(),
					"/OrderList/Order/ChargeTransactionDetails/ChargeTransactionDetail[@PaymentKey='" + strPaymentKey + "' and @ChargeType='CHARGE']");

			for (int iStlCrgs = 0; iStlCrgs < nlSettledCharges.getLength(); iStlCrgs++) {
				Element eleChargeTranDetail = (Element) nlSettledCharges.item(iStlCrgs);
				String strRequestAmount = eleChargeTranDetail.getAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT);
				String strCaptureId = null;
				Element eleCreditCardTransactions = (Element) eleChargeTranDetail.getElementsByTagName("CreditCardTransactions").item(0);
				NodeList nlCreditCardTransaction = eleCreditCardTransactions.getElementsByTagName("CreditCardTransaction");
				for (int iCCtrn = 0; iCCtrn < nlCreditCardTransaction.getLength(); iCCtrn++) {
					Element eleCreditCardTrans = (Element) nlCreditCardTransaction.item(iCCtrn);
					if (!YFCObject.isVoid(eleCreditCardTrans.getAttribute("AuthCode")) && !AcademyConstants.STR_DUMMY_SETTLEMENT.equals(eleCreditCardTrans.getAttribute("AuthCode"))) {
						strCaptureId = eleCreditCardTrans.getAttribute("AuthCode");// capture-id
						break;
					}

				}
				if (!YFCObject.isVoid(strCaptureId) && !strRequestAmount.contains(AcademyConstants.STR_HYPHEN)) {
					hmSettledAmount.put(strCaptureId, df.format(Double.parseDouble(strRequestAmount)));
				} else {
					log.verbose("Paypal Direct:: Not valid Charge Settlement details. ::" + strCaptureId + "::" + strRequestAmount);
				}
			}

			log.verbose("Paypal Direct:: hmSettledAmount ::" + hmSettledAmount.size() + " \n " + hmSettledAmount.toString());
			log.debug("End of AcademyPaypalPaymentProcessingUtil.getSettledAmounts() method after direct paypal.");
			return hmSettledAmount;
		}


		/**
		 * This method forms a hashmap CaptureID-Amount to be requested for webservice
		 * call
		 * 
		 * @param docPaymentIn
		 * @param hmSettledAmount
		 * @param hmRefundedAmount
		 * @return
		 * @throws Exception
		 */
		public static HashMap<String, String> retrieveValidSettledTransactions(Document docPaymentIn, HashMap<String, String> hmSettledAmount,
				HashMap<String, String> hmRefundedAmount) throws Exception {
			log.debug("Begin of AcademyPaypalPaymentProcessingUtil.retrieveValidSettledTransactions() method after direct paypal.");
			log.debug("Paypal Direct:: docPaymentIn:" + XMLUtil.getXMLString(docPaymentIn));
			log.verbose("Paypal Direct:: hmSettledAmount:" + hmSettledAmount.toString());
			log.verbose("Paypal Direct:: hmRefundedAmount:"+ hmRefundedAmount.toString());
			HashMap<String, String> hmValidTaggedRefunds = new HashMap<String, String>();
			DecimalFormat df = new DecimalFormat("0.00");

			String strRefundReqAmt = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);
			if (strRefundReqAmt.startsWith(AcademyConstants.STR_HYPHEN))
				strRefundReqAmt = strRefundReqAmt.split(AcademyConstants.STR_HYPHEN)[1];

			hmSettledAmount = getAvailableAmountsForRefund(hmRefundedAmount, hmSettledAmount);

			// Check if the Refund request amount has a best match in the settled amounts.
			if (hmSettledAmount.containsValue(strRefundReqAmt)) {
				// Best match exisits. Use the same and refund.
				log.verbose("Paypal Direct:: strRefundReqAmt ::" + strRefundReqAmt);
				for (Map.Entry<String, String> entry : hmSettledAmount.entrySet()) {
					String strTransactionKey = entry.getKey();
					String strAmount = entry.getValue();
					if (strAmount.equals(strRefundReqAmt)) {
						hmValidTaggedRefunds.put(strTransactionKey, strAmount);
						break;
					}
				}
			}
			// No exact request. Check for best match
			else {
				hmSettledAmount = sortByDescending(hmSettledAmount);
				if (hmSettledAmount.size() > 0) {
					double dRefundReqAmt = Double.parseDouble(strRefundReqAmt);
					Iterator<Map.Entry<String, String>> iter = hmSettledAmount.entrySet().iterator();

					while (iter.hasNext()) {
						Map.Entry<String, String> entry = iter.next();
						String strTransactionKey = entry.getKey();
						String strAmount = entry.getValue();
						// Check if the amount on this entry is suffuicent for refund
						double dAmount = Double.parseDouble(strAmount);
						log.verbose("Paypal Direct:: dAmount ::" + dAmount + " :: dRefundReqAmt ::" + dRefundReqAmt);
						if (dRefundReqAmt > 0) {
							if (dRefundReqAmt > dAmount) {
								log.verbose("Paypal Direct:: Request amount is partailly settled." + strTransactionKey);
								hmValidTaggedRefunds.put(strTransactionKey, strAmount);
							} else {
								log.verbose("Paypal Direct:: Request amount is settled." + strTransactionKey);
								hmValidTaggedRefunds.put(strTransactionKey, df.format(dRefundReqAmt));
							}
							dRefundReqAmt = dRefundReqAmt - dAmount;
						} else {
							break;
						}
					}
				}
			}

			log.verbose("Paypal Direct:: hmValidTaggedRefunds ::" + hmValidTaggedRefunds.size() + " \n " + hmValidTaggedRefunds.toString());
			log.debug("End of AcademyPaypalPaymentProcessingUtil.retrieveValidSettledTransactions() method after direct paypal.");
			return hmValidTaggedRefunds;
		}

		/**
		 * This method prepares a hash map captureid-amount can be refunded of an order
		 * 
		 * @param hmRefundedAMount
		 * @param hmSettledAmount
		 * @return
		 * @throws Exception
		 */
		private static HashMap<String, String> getAvailableAmountsForRefund(HashMap<String, String> hmRefundedAMount, HashMap<String, String> hmSettledAmount)
				throws Exception {
			log.debug("Begin of AcademyPaypalPaymentProcessingUtil.getAvailableAmountsForRefund() method after direct paypal.");
			DecimalFormat df = new DecimalFormat("0.00");

			if (hmRefundedAMount.size() > 0) {
				Iterator<Map.Entry<String, String>> iter = hmSettledAmount.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String, String> entry = iter.next();
					String strTransactionKey = entry.getKey();// Capture ID
					String strAmount = entry.getValue();
					if (hmRefundedAMount.containsKey(strTransactionKey)) {
						String strRefundAmount = hmRefundedAMount.get(strTransactionKey);
						String strNewTotal = df.format(Double.parseDouble(strAmount) - Double.parseDouble(strRefundAmount));
						if (Double.parseDouble(strNewTotal) > 0) {
							hmSettledAmount.put(strTransactionKey, strNewTotal);
						} else {
							iter.remove();
						}
					}
				}
			}
			log.verbose("Paypal Direct:: hmSettledAmount ::" + hmSettledAmount.size() + " \n " + hmSettledAmount.toString());
			log.debug("End of AcademyPaypalPaymentProcessingUtil.getAvailableAmountsForRefund() method after direct paypal.");
			return hmSettledAmount;
		}

		/**
		 * This method sorts a hashmap
		 * 
		 * @param unsortMap
		 * @return
		 */
		private static HashMap<String, String> sortByDescending(HashMap<String, String> unsortMap) {
			log.debug("Begin of AcademyPaypalPaymentProcessingUtil.sortByDescending() method after direct paypal.");
			log.verbose("Paypal Direct:: unsortMap ::" + unsortMap.size() + " \n " + unsortMap.toString());

			List<Entry<String, String>> list = new LinkedList<Entry<String, String>>(unsortMap.entrySet());

			// Sorting the list based on values
			Collections.sort(list, new Comparator<Entry<String, String>>() {
				public int compare(Entry<String, String> o1, Entry<String, String> o2) {
					return o2.getValue().compareTo(o1.getValue());
				}
			});

			// Maintaining insertion order with the help of LinkedList
			HashMap<String, String> sortedMap = new LinkedHashMap<String, String>();
			for (Entry<String, String> entry : list) {
				sortedMap.put(entry.getKey(), entry.getValue());
			}

			log.verbose("Paypal Direct:: sortedMap ::" + sortedMap.size() + " \n " + sortedMap.toString());
			log.debug("End of AcademyPaypalPaymentProcessingUtil.sortByDescending() method after direct paypal.");
			return sortedMap;
		}
		
		/**
		 * This method gets the amounts refunded on the order, i.e. amount already
		 * successfully refunded
		 * 
		 * @param hmRefundedAmount
		 * @param hmRefundTransactionsDetails
		 * @return
		 * @throws Exception
		 */
		public static HashMap<String, String> updateRefundedAmount(HashMap<String, String> hmRefundedAmount, HashMap<String, String> hmRefundTransactionsDetails)
				throws Exception {
			log.debug("Begin of AcademyPaypalPaymentProcessingUtil.updateRefundedAmount() method after direct paypal.");
			DecimalFormat df = new DecimalFormat("0.00");

			if (hmRefundTransactionsDetails.size() > 0) {
				Iterator<Map.Entry<String, String>> iter = hmRefundTransactionsDetails.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String, String> entry = iter.next();
					String strTransactionKey = entry.getKey();
					String strAmount = entry.getValue();
					if (hmRefundedAmount.containsKey(strTransactionKey)) {
						String strRefundAmount = hmRefundedAmount.get(strTransactionKey);
						String strNewTotal = df.format(Double.parseDouble(strAmount) + Double.parseDouble(strRefundAmount));
						hmRefundTransactionsDetails.put(strTransactionKey, strNewTotal);
					}

				}
			}
			log.verbose("Paypal Direct:: hmRefundTransactionsDetails ::" + hmRefundTransactionsDetails.size() + " \n " + hmRefundTransactionsDetails.toString());
			log.debug("End of AcademyPaypalPaymentProcessingUtil.updateRefundedAmount() method after direct paypal.");
			return hmRefundTransactionsDetails;
		}
		
		//End : OMNI-57906
		/**
		 * 
		 * @param env
		 * @param serviceName
		 * @param orderHeaderKey
		 * @return
		 * @throws Exception
		 */
		public static Document getOrderList(YFSEnvironment env, String serviceName, String orderHeaderKey) throws Exception {
			log.debug("Begin of AcademyPaypalPaymentProcessingUtil.getOrderList() method after direct paypal.");
			Document docGetOrderListInp = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			docGetOrderListInp.getDocumentElement().setAttribute(AcademyConstants.STR_ORDR_HDR_KEY, orderHeaderKey);
			Document docGetOrderListOut = AcademyUtil.invokeService(env, serviceName, docGetOrderListInp);
			log.debug("End of AcademyPaypalPaymentProcessingUtil.getOrderList() method after direct paypal.");
			return docGetOrderListOut;
		}
		
		/**This method will create response for denied response
		 * 
		 * @param docPaymentIn
		 * @param elePaymentOut
		 * @param docRespPaypal
		 * @param strTransactionType
		 * @param strAlertErrorShortDesc
		 * @return
		 * @throws Exception
		 */
		public static Element prepareDisapprovedResponse(Document docPaymentIn, Element elePaymentOut, Document docRespPaypal,String strTransactionType,String strAlertErrorShortDesc)throws Exception{
			log.debug("Begin of AcademyPaypalPaymentProcessingUtil.prepareDisapprovedResponse() method after direct paypal.");
			return prepareDummyRespForAllTransType( docPaymentIn,  elePaymentOut,  docRespPaypal, strTransactionType, strAlertErrorShortDesc);
			
			
			
		}
		/**This method prepares the UE output document in case of Dummy response for all
		 * transaction types
		 * 
		 * @param docPaymentIn
		 * @param elePaymentOut
		 * @param docRespPaypal
		 * @param strTransactionType
		 * @param strErrorShortDesc
		 * @return
		 * @throws Exception
		 */
		public static Element prepareDummyRespForAllTransType(Document docPaymentIn, Element elePaymentOut, Document docRespPaypal,String strTransactionType,String strErrorShortDesc) throws Exception {
			log.debug("Begin of AcademyPaypalPaymentProcessingUtil.prepareDummyRespForAllTransType() method after direct paypal.");
			log.verbose("Paypal Direct:: strTransactionType: " + strTransactionType + " after direct paypal.");
			log.verbose("Paypal Direct:: docRespPaypal:" + XMLUtil.getXMLString(docRespPaypal));
			String strChargeType = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH + AcademyConstants.ATTR_CHARGE_TYPE_AT);
			Element elePaypalResp = docRespPaypal.getDocumentElement();
			String strResponseID = elePaypalResp.getAttribute(AcademyConstants.PAYPAL_RESPONSE_ID);
			//eg 2XX, 4XX,305
			String strTransRespCode = AcademyConstants.STATUS_CODE_NO_VALID_SETTLEMENT.equals(elePaypalResp.getAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE))?
					AcademyConstants.STATUS_CODE_ERROR:elePaypalResp.getAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE);
			//eg.denied,null
			String strAuthRespCode = elePaypalResp.getAttribute(AcademyConstants.ATTR_PAYPAL_RESPONSE_STATE);
			//eg. reason, null
			String strAuthResMsg = elePaypalResp.getAttribute(AcademyConstants.JSON_ATTR_STATE_REASON);
			//eg.unique id
			String strPaypalRequestID = elePaypalResp.getAttribute(AcademyConstants.ATTR_PAYPAL_REQUEST_ID);
			//debug id
			String strFinTransTrackID = elePaypalResp.getAttribute(AcademyConstants.ATTR_PAYPAL_DEBUG_ID);
			//eg.Max Retry Limit Reached/paypal Denied, Paypal Returned Failure
			String strTransRespMsg= YFCObject.isVoid(strErrorShortDesc)?AcademyConstants.STR_PAYPAL_RETURNED_FAILURE_ERROR:
				strErrorShortDesc;
			if(YFCObject.isVoid(strAuthRespCode))//4XX/X/X or 5XX/X/X. state is null.
			{
				//strAuthResMsge is set as description of eg.AUTH_SETTLE_NOT_ALLOWED,SOCKET_READ_TIMED_OUT,CAPTURE_AMOUNT_LIMIT_EXCEEDED--"Paypal Error"
				String strTempAuthResMsg= YFCObject.isVoid(elePaypalResp.getAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_MESSAGE))?
						elePaypalResp.getAttribute(AcademyConstants.ATTR_SMALL_ERROR_DESCRIPTION):elePaypalResp.getAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_MESSAGE);
				strAuthResMsg = YFCObject.isVoid(strTempAuthResMsg)?
						AcademyConstants.STR_PAYPAL_ERROR:strTempAuthResMsg;
						
			}
			//For 4XX/X/X-"Transaction Returned Failure"
			strErrorShortDesc=YFCObject.isVoid(strErrorShortDesc)?AcademyConstants.STR_PAYPAL_TRANS_RETURNED_ERROR:strErrorShortDesc;
			if (!YFSObject.isVoid(strTransactionType) && strTransactionType.equalsIgnoreCase(AcademyConstants.TRANSACTION_TYPE_PURCHASE)) {
				// Incase a purchase transaction falis, do a dummy settlement
				elePaymentOut = prepareDummyPurchaseResponse(docPaymentIn, elePaymentOut, strAuthRespCode, strAuthResMsg,
						strTransRespCode,strTransRespMsg, strFinTransTrackID,strPaypalRequestID,strErrorShortDesc,strResponseID);
			} else if (!YFSObject.isVoid(strTransactionType) && (strTransactionType.equalsIgnoreCase(AcademyConstants.TRANSACTION_TYPE_CAPTURE))) {
				// Incase a purchase transaction falis, do a dummy settlement
				elePaymentOut = prepareDummyCaptureResponse(docPaymentIn, elePaymentOut, strAuthRespCode, strAuthResMsg,
						strTransRespCode, strTransRespMsg,strFinTransTrackID, strPaypalRequestID,strErrorShortDesc,strResponseID);
			} else if (!YFSObject.isVoid(strTransactionType) && strTransactionType.equalsIgnoreCase(AcademyConstants.TRANSACTION_TYPE_VOID)) {
				// Incase a purchase transaction falis, do a dummy settlement
				elePaymentOut = prepareDummyVoidResponse(docPaymentIn, elePaymentOut, strAuthRespCode, strAuthResMsg,
						strTransRespCode,  strTransRespMsg,strFinTransTrackID, strPaypalRequestID,strErrorShortDesc,strResponseID);
			}else if(YFSObject.isVoid(strTransactionType) || 
					AcademyConstants.STR_CHRG_TYPE_CHARGE.equals(strChargeType)) {
				elePaymentOut = prepareDummyCaptureResponse(docPaymentIn, elePaymentOut, strAuthRespCode, strAuthResMsg,
						strTransRespCode,strTransRespMsg, strFinTransTrackID, strPaypalRequestID,strErrorShortDesc,strResponseID);
			}else {
				log.verbose("Paypal Direct:: strAuthRespCode: " + strAuthRespCode);
				log.verbose("Paypal Direct:: strAuthResMsge:" + strAuthResMsg);
				log.verbose("Paypal Direct:: strTransRespCode:" + strTransRespCode);
				log.verbose("Paypal Direct:: strTransRespMsg:" + strTransRespMsg);
				log.verbose("Paypal Direct:: strFinTransTrackID:" + strFinTransTrackID);
				log.verbose("Paypal Direct:: strPaypalRequestID:" + strPaypalRequestID);
				// Incase auth declination mark as hard decline
				elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_HARD_DECLINED);
				elePaymentOut.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_NO);
				elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE,strAuthRespCode);
				elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, strAuthResMsg);	
				
				elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE, strTransRespCode);//eg. HttpStatusCode=400
				elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_MESSAGE, strTransRespMsg);
				elePaymentOut.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_CODE, strFinTransTrackID);//Paypal-Debug-ID
				elePaymentOut.setAttribute(AcademyConstants.ATTR_REQUEST_ID, strPaypalRequestID);//Paypal-Request-ID
			}
			
			elePaymentOut.setAttribute(AcademyConstants.ATTR_TRANS_ID, docPaymentIn.getDocumentElement().getAttribute(AcademyConstants.ATTR_CHARGE_TYPE));
			log.verbose("Paypal Direct:: elePaymentOut:" + XMLUtil.getElementXMLString(elePaymentOut));
			log.debug("End of AcademyPaypalPaymentProcessingUtil.prepareDummyRespForAllTransType() method after direct paypal.");
			return elePaymentOut;
		}
		
		/**This method will raise alert for unhandled error
		 * 
		 * @param env
		 * @param docPaymentIn
		 * @param elePaymentOut
		 * @param docRespPaypal
		 * @param strTransactionType
		 * @throws Exception
		 */
		public static void raiseUnHandledErrorAlert(YFSEnvironment env, Document docPaymentIn, 
				Document docRespPaypal, String strTransactionType,String strTrasErrorShortDesc,String strType)throws Exception
		{
			log.debug("Begin of AcademyPaypalPaymentProcessingUtil.raiseUnHandledErrorAlert() method after direct paypal.");
			Element elePaypalResp = docRespPaypal.getDocumentElement();
			String strOrderNo = XPathUtil.getString(docPaymentIn,
					AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_NO);
			String strReqAmt = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);
			Document docHandledError = XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT);
			Element eleUnhandleErr = docHandledError.getDocumentElement();
			//String strTrasErrorShortDesc = "Paypal Returned Unhandled Error";
			String strTransRespCode = AcademyConstants.STATUS_CODE_NO_VALID_SETTLEMENT.equals(elePaypalResp.getAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE))?
					AcademyConstants.STATUS_CODE_ERROR:elePaypalResp.getAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE);//eg 2XX, 4XX,305
			String strAuthRespCode = elePaypalResp.getAttribute(AcademyConstants.ATTR_PAYPAL_RESPONSE_STATE);//eg.denied,null
			String strAuthResMsg = elePaypalResp.getAttribute(AcademyConstants.JSON_ATTR_STATE_REASON);//eg. reason, null
			prepareDummyRespForAllTransType(docPaymentIn, eleUnhandleErr,
					docRespPaypal, strTransactionType, strTrasErrorShortDesc);
			// Setting order details to raise alert
			eleUnhandleErr.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY, XPathUtil.getString(docPaymentIn,
					AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_HEADER_KEY));
			eleUnhandleErr.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
			eleUnhandleErr.setAttribute(AcademyConstants.ATTR_PAYMENT_TYPE, XPathUtil.getString(docPaymentIn,
					AcademyConstants.STR_PAYMENT_PATH + AcademyConstants.PAYMENT_TYPE_XPATH));
			eleUnhandleErr.setAttribute(AcademyConstants.ATTR_TRAN_AMT, strReqAmt);
			// Below details will be shown in Alert console
			if(YFCObject.isVoid(strAuthRespCode))//4XX/X/X or 5XX/X/X. state is null.
			{
				//strAuthResMsge is set as description of eg.AUTH_SETTLE_NOT_ALLOWED,SOCKET_READ_TIMED_OUT,CAPTURE_AMOUNT_LIMIT_EXCEEDED
				String strTempAuthResMsg= YFCObject.isVoid(elePaypalResp.getAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_MESSAGE))?
						elePaypalResp.getAttribute(AcademyConstants.ATTR_SMALL_ERROR_DESCRIPTION):elePaypalResp.getAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_MESSAGE);
				strAuthResMsg = YFCObject.isVoid(strTempAuthResMsg)?
						AcademyConstants.STR_PAYPAL_ERROR:strTempAuthResMsg;
						
			}
			eleUnhandleErr.setAttribute(AcademyConstants.ATTR_REASON_CODE, strAuthRespCode);
			eleUnhandleErr.setAttribute(AcademyConstants.ATTR_REASON_DESC, strAuthResMsg);
			eleUnhandleErr.setAttribute(AcademyConstants.ATTR_ERROR_NO, strTransRespCode);
			eleUnhandleErr.setAttribute(AcademyConstants.ATTR_ERROR_DESC_SHORT, strTrasErrorShortDesc);
			eleUnhandleErr.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_MESSAGE, strType);
			AcademyUtil.invokeService(env, AcademyConstants.SERV_ACAD_UNHANDLED_ERROR_DPP_ALERT, docHandledError);
			log.debug("End of AcademyPaypalPaymentProcessingUtil.raiseUnHandledErrorAlert() method after direct paypal.");
		}
		
		/**
		 *Eg.strDateTimeOffSet=2022-05-09T21:24:53-05:00
		 *formatter DateTimeFormatter.ISO_LOCAL_DATE_TIME
		 * @param strDateTimeOffSet
		 * @param formatter
		 * @return
		 */
		public static String getDateInJVMDateTimeStamp(String strDateTimeOffSet,DateTimeFormatter formatter)
		{
			log.debug("Begin of AcademyPaypalPaymentProcessingUtil.getDateInJVMDateTimeStamp() method after direct paypal.");
			ZonedDateTime serverZoneDateTime = ZonedDateTime.parse(strDateTimeOffSet);
			log.verbose("Paypal Direct:: JVM TimeZone: " + Calendar.getInstance().getTimeZone().getID());
			ZoneId clientTimeZone = ZoneId.of(Calendar.getInstance().getTimeZone().getID());
			//Convert serverZoneDateTime to JVM DateTime Zone
			ZonedDateTime clientZoneDateTime = serverZoneDateTime.withZoneSameInstant(clientTimeZone);
			String strJVMZoneDateTime=formatter.format(clientZoneDateTime);
			log.verbose("Paypal Direct:: serverZoneDateTime: " + formatter.format(serverZoneDateTime));		
			log.verbose("Paypal Direct:: JVMZoneDateTime: " + strJVMZoneDateTime);
			log.debug("End of AcademyPaypalPaymentProcessingUtil.getDateInJVMDateTimeStamp() method after direct paypal.");
			return strJVMZoneDateTime;
			
		}
		//OMNI-78920 -- START
		/**This will return message to print in log
		 * 
		 * @param elePaypalResp
		 * @return
		 */
		public static String getLoggerInfoString(String strInfoLog,Element elePaypalResp,String strTransactionType,String strHttpStatusCode,String strChargeTransactionKey,String strOrderNo) {
			String strSplunkDtls =AcademyConstants.PAYPAL_TRACE_SPLUNK.replace("{APIName}",strTransactionType);
			strSplunkDtls = strSplunkDtls.replace("{CTK}",strChargeTransactionKey);
			strSplunkDtls = strSplunkDtls.replace("{OrderNo}",strOrderNo);
			strSplunkDtls = strSplunkDtls.replace("{ResponseCode}",strHttpStatusCode);
			StringBuilder strBlrError = new StringBuilder(strInfoLog);
			String strErrorName = elePaypalResp.getAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_NAME);
			log.verbose("Paypal Direct:: strName:  " + strErrorName );
			String strError = elePaypalResp.getAttribute(AcademyConstants.ATTR_SMALL_ERROR);
			String strErrorDesc = elePaypalResp.getAttribute(AcademyConstants.ATTR_SMALL_ERROR_DESCRIPTION);
			log.verbose("Paypal Direct:: strError:  " + strError );
			log.verbose("Paypal Direct:: strErrorDesc:  " + strErrorDesc );
			if(YFCObject.isVoid(strErrorName))
			{
				strBlrError.append("\"").append(AcademyConstants.ATTR_SMALL_ERROR).append("\": \"").append(elePaypalResp.getAttribute(AcademyConstants.ATTR_SMALL_ERROR)).append("\",\"").append(AcademyConstants.ATTR_SMALL_ERROR_DESCRIPTION).append("\": \"").append(elePaypalResp.getAttribute(AcademyConstants.ATTR_SMALL_ERROR_DESCRIPTION)).append("\"");
				
			}else {
				strBlrError.append("\"").append(AcademyConstants.PAYPAL_ERROR_RESPONSE_NAME).append("\": \"").append(elePaypalResp.getAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_NAME)).append("\",\"").append(AcademyConstants.PAYPAL_ERROR_RESPONSE_MESSAGE).append("\": \"").append(elePaypalResp.getAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_MESSAGE)).append("\"");
			}
			return strBlrError.append(strSplunkDtls).toString();
			
		}
		//OMNI-78920 -- END		
}
