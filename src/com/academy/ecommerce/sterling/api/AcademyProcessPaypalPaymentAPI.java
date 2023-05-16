//package declaration
package com.academy.ecommerce.sterling.api;

import java.io.InputStream;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.json.utils.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.academy.ecommerce.sterling.util.AcademyPayPalRestUtil;
import com.academy.ecommerce.sterling.util.AcademyPaypalPaymentProcessingUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCStringUtil;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;

/**
 * This class will be used to process Paypal Decoupled payment type
 * 
 * @author Cognizant Technology Solutions
 *
 */

public class AcademyProcessPaypalPaymentAPI implements YIFCustomApi {

	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyProcessPaypalPaymentAPI.class);

	/**
	 * It holds the properties configured in AcademyProcessPaypalPaymentAPI service
	 * 
	 */
	private Properties props;

	/**
	 * It sets the properties
	 * 
	 * @param props
	 */
	public void setProperties(Properties props) {
		this.props = props;
	}

	private static String strApiKey = null;
	private static String strToken = null;
	private static String strCancelDeclinedAuth = null;
	private static String strCancelDeclinedCharge = null;

	private static int iMaxRetryCount;

	private static String strPayPalUser = null;
	private static String strPayPalPass = null;
	private static String strPayPalUrlToken = null;
	private static String strPayPalUrlAuthorization = null;
	private static String strPayPalUrlVoid = null;
	private static String strPayPalUrlCapture = null;
	private static String strPayPalUrlRefund = null;
	private static String strConnectionTimeout = null;
	private static String strResponseTimeout = null;
	private static String strPPAuthExpiryDays = null;
	private static String strProxyHost = null;
	private static String strProxyPort = null;

	private static List<String> listPayPalAuthRetryErrorName = null;
	private static List<String> listDirectPaypalSuccessResName = null;
	private static List<String> listDirectPaypalDummySettlementErrorName = null;
	private static List<String> listDirectPaypalHardDeclineErrorName = null;
	private static List<String> listDirectPaypalRateLimitRetryErrorName = null;
	private static List<String> listDirectPaypalReprocessibleErrorName = null;
	private static List<String> listDirectPaypalCodeCaughtErrorName = null;
	private static List<String> listDirectPaypalAccessTokenErrorName = null;
	private static List<String> listDirectPaypalIgnoreAllErrorName = null;
	private static ConcurrentMap<String, HashMap> mapTaskPropDtls = new ConcurrentHashMap<>();

	Document docOrderList = null;
	HashMap<String, Document> hmFailedReversalId = new HashMap<String, Document>();
	Map<String, String> restParameters = new HashMap<String, String>();
	static {
		strPPAuthExpiryDays = YFSSystem.getProperty(AcademyConstants.PROP_PP_AUTH_EXPIRY_DAYS);
		strPayPalUser = YFSSystem.getProperty(AcademyConstants.PROP_PAYPAL_USER_ID);
		strPayPalPass = YFSSystem.getProperty(AcademyConstants.PROP_PAYPAL_PASSWORD);
		strPayPalUrlToken = YFSSystem.getProperty(AcademyConstants.PROP_PAYPAL_URL_TOKEN);
		strPayPalUrlAuthorization = YFSSystem.getProperty(AcademyConstants.PROP_PAYPAL_URL_AUTHORIZATION);
		strPayPalUrlCapture = YFSSystem.getProperty(AcademyConstants.PROP_PAYPAL_URL_CAPTURE);
		strPayPalUrlVoid = YFSSystem.getProperty(AcademyConstants.PROP_PAYPAL_URL_VOID);
		strPayPalUrlRefund = YFSSystem.getProperty(AcademyConstants.PROP_PAYPAL_URL_REFUND);
		strConnectionTimeout = YFSSystem.getProperty(AcademyConstants.PROP_CONNECTION_TIMEOUT);
		strResponseTimeout = YFSSystem.getProperty(AcademyConstants.PROP_RESPONSE_TIMEOUT);
		strProxyHost = YFSSystem.getProperty(AcademyConstants.PROP_PAYZ_PROXY_HOST);
		strProxyPort = YFSSystem.getProperty(AcademyConstants.PROP_PAYZ_PROXY_PORT);

		listPayPalAuthRetryErrorName = Arrays.asList(
				YFSSystem.getProperty(AcademyConstants.PROP_PAYPAL_RETRY_ERROR_NAME).split(AcademyConstants.STR_COMMA));
		listDirectPaypalSuccessResName = Arrays.asList(
				YFSSystem.getProperty(AcademyConstants.PROP_PAYPAL_SUCCESS_NAME).split(AcademyConstants.STR_COMMA));
		listDirectPaypalDummySettlementErrorName = Arrays.asList(YFSSystem
				.getProperty(AcademyConstants.PROP_PAYPAL_DUMMY_SETTLE_ERROR_NAME).split(AcademyConstants.STR_COMMA));
		listDirectPaypalHardDeclineErrorName = Arrays.asList(YFSSystem
				.getProperty(AcademyConstants.PROP_PAYPAL_HARD_DECLINE_ERROR_NAME).split(AcademyConstants.STR_COMMA));
		listDirectPaypalRateLimitRetryErrorName = Arrays.asList(YFSSystem
				.getProperty(AcademyConstants.PROP_PAYPAL_LIMIT_RETRY_ERROR_NAME).split(AcademyConstants.STR_COMMA));
		listDirectPaypalReprocessibleErrorName = Arrays.asList(YFSSystem
				.getProperty(AcademyConstants.PROP_PAYPAL_REPROCESS_ERROR_NAME).split(AcademyConstants.STR_COMMA));
		listDirectPaypalCodeCaughtErrorName = Arrays.asList(YFSSystem
				.getProperty(AcademyConstants.PROP_PAYPAL_CODECAUGHT_ERROR_NAME).split(AcademyConstants.STR_COMMA));
		listDirectPaypalAccessTokenErrorName = Arrays.asList(YFSSystem
				.getProperty(AcademyConstants.PROP_PAYPAL_TOKEN_AUTH_ERROR_NAME).split(AcademyConstants.STR_COMMA));
		if(YFCObject.isVoid(YFSSystem.getProperty(AcademyConstants.PROP_PAYPAL_IGNORE_ALL_ERROR_NAME)) || YFCObject.isVoid(YFSSystem
				.getProperty(AcademyConstants.PROP_PAYPAL_IGNORE_ALL_ERROR_NAME).split(AcademyConstants.STR_COMMA)))
		{
			listDirectPaypalIgnoreAllErrorName=new ArrayList();
		}else {
			listDirectPaypalIgnoreAllErrorName = Arrays.asList(YFSSystem
					.getProperty(AcademyConstants.PROP_PAYPAL_IGNORE_ALL_ERROR_NAME).split(AcademyConstants.STR_COMMA));
		}
		populateResponsekProcessStaticMap();

	}

	/**
	 * This method creates and sends the payment request to Paypal gateway directly
	 * for all transaction types.
	 * 
	 * @param env
	 * @param inStruct
	 * @return outStruct
	 * @throws YFSUserExitException
	 */

	public Document processDirectPaypalPayment(YFSEnvironment env, Document docPaymentIn) throws Exception {
		log.beginTimer("AcademyProcessPaypalPaymentAPI.processDirectPaypalPayment()");
		log.debug("Begin of AcademyProcessPaypalPaymentAPI.processDirectPaypalPayment() method after direct paypal.");
		log.verbose("Paypal Direct:: AcademyProcessPaypalPaymentAPI.processDirectPaypalPayment() Payment Input XML ::" + XMLUtil.getXMLString(docPaymentIn));

		restParameters.put(AcademyConstants.WEBSERVICE_APIKEY, strApiKey);
		restParameters.put(AcademyConstants.WEBSERVICE_TOKEN, strToken);

		restParameters.put(AcademyConstants.WEBSERVICE_PAYPAL_URL_TOKEN, strPayPalUrlToken);
		restParameters.put(AcademyConstants.WEBSERVICE_PAYPAL_USER_ID, strPayPalUser);
		restParameters.put(AcademyConstants.WEBSERVICE_PAYPAL_PASSWORD, strPayPalPass);
		restParameters.put(AcademyConstants.ATTR_CONNECTION_TIMEOUT, strConnectionTimeout);
		restParameters.put(AcademyConstants.ATTR_RESPONSE_TIMEOUT, strResponseTimeout);
		restParameters.put(AcademyConstants.WEBSERVICE_PROXY_HOST, strProxyHost);
		restParameters.put(AcademyConstants.WEBSERVICE_PROXY_PORT, strProxyPort);

		// This is for time out validation
		// TODO Time Out Handing needs to be revisited
		if (!YFSObject.isVoid(props.getProperty(AcademyConstants.ATTR_CONNECTION_TIMEOUT))) {
			restParameters.put(AcademyConstants.ATTR_CONNECTION_TIMEOUT, props.getProperty(AcademyConstants.ATTR_CONNECTION_TIMEOUT));
		}
		if (!YFSObject.isVoid(props.getProperty(AcademyConstants.ATTR_RESPONSE_TIMEOUT))) {
			restParameters.put(AcademyConstants.ATTR_RESPONSE_TIMEOUT, props.getProperty(AcademyConstants.ATTR_RESPONSE_TIMEOUT));
		}

		String strIsDBLoggingEnableForAuth = props.getProperty(AcademyConstants.STR_DB_LOGGING_FOR_AUTH);
		String strIsDBLoggingEnableForCharge = props.getProperty(AcademyConstants.STR_DB_LOGGING_FOR_CHARGE);

		strCancelDeclinedAuth = props.getProperty(AcademyConstants.STR_CANCEL_DECLINED_AUTH);
		strCancelDeclinedCharge = props.getProperty(AcademyConstants.STR_CANCEL_DECLINED_CHARGE);
		if (!YFCObject.isVoid(props.getProperty(AcademyConstants.STR_MAX_RETRY_LIMIT)))
			iMaxRetryCount = Integer.parseInt(props.getProperty(AcademyConstants.STR_MAX_RETRY_LIMIT));
		//OMNI-71639 - START
		// Get Expiry Buffer in Secs from Service Argument
		if (!YFSObject.isVoid(props.getProperty(AcademyConstants.STR_PAYPAL_TOKEN_EXPIRY_BUFFER_IN_SECS))) {
			restParameters.put(AcademyConstants.STR_PAYPAL_TOKEN_EXPIRY_BUFFER_IN_SECS,
					props.getProperty(AcademyConstants.STR_PAYPAL_TOKEN_EXPIRY_BUFFER_IN_SECS));
		}
		// Get Old Token Enable=N from Service Argument
		if (!YFSObject.isVoid(props.getProperty(AcademyConstants.STR_PAYPAL_REUSE_ACCESS_TOKEN_DIRECT_CALL))) {
			restParameters.put(AcademyConstants.STR_PAYPAL_REUSE_ACCESS_TOKEN_DIRECT_CALL, props.getProperty(AcademyConstants.STR_PAYPAL_REUSE_ACCESS_TOKEN_DIRECT_CALL));
		}
		//OMNI-71639 - END
		// Start : OMNI-69010
		// Fetches a Service Argument named PaypalProxyEnable and it decides if proxy to
		// be enabled/disabled for
		// 1. Getting Access Token rest call for both payzee-paypal & paypal decouple
		// 2. All rest calls for paypal decoupled scenarios
		log.verbose("Is PROXY enabled:: " + props.getProperty(AcademyConstants.ATTR_PAYPAL_PROXY_ENABLE));
		if (!YFSObject.isVoid(props.getProperty(AcademyConstants.ATTR_PAYPAL_PROXY_ENABLE))) {
			restParameters.put(AcademyConstants.ATTR_PAYPAL_PROXY_ENABLE, props.getProperty(AcademyConstants.ATTR_PAYPAL_PROXY_ENABLE));
		}
		// End : OMNI-69010

		boolean bIsPaypalReAuthCall = false;
		boolean bIgnoreAuth = false;
		boolean bIsCompleteAuthVoided = false;
		String strTransactionType = null;
		String strSplitShipment = null;
		Date dateCurrAuthExpiryDate = null;
		String strChargeTransactionKey=null;
		Document docRespPaypal = null;
		Document docPaymentOut = null;
		String strPaypalInp = null;
		String strPaymentReferenceValue = null;

		log.verbose("Paypal Direct:: ****Inside processPaypalPayment for Direct Paypal Interaction after Paypal Decoupling*******");
		try {

			String strReqAmt = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);
			String strAuthId = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_AUTHORIZATION_ID);
			String chargeType = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH + AcademyConstants.ATTR_CHARGE_TYPE_AT);
			String strOrderHeaderKey = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_HEADER_KEY);
			strChargeTransactionKey = XPathUtil.getString(docPaymentIn,
					AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY);

			/*
			 * 1)For auth expiry product will make one entry for auth with -ve amount and
			 * one for new auth with +ve amount. As the auth is already expired so we cannot
			 * void it and this auth with -ve amount will be processed as dummy i.e APPROVED
			 * response.
			 * 
			 * 2)For partial/full cancellation the Payment input xml has
			 * CurrentAuthorizationExpirationDate attribute and the date is in future. If
			 * the Authorization ID has any existing settlement against it.If the
			 * Authorization ID has no settlements against it, then auth with -ve amount for
			 * cancellation would need VOID call. Here bIgnoreAuth value will remain false.
			 */

			// Below condition is true for both cancellation and Auth expiry
			if (AcademyConstants.STR_CHRG_TYPE_AUTH.equalsIgnoreCase(chargeType) && strReqAmt.startsWith(AcademyConstants.STR_HYPHEN)) {

				strTransactionType = AcademyConstants.TRANSACTION_TYPE_VOID;
				String strCurrAuthExpiry = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CURR_AUTH_EXP_DATE);
				// For cancel : true, For re-auth on auth expiry its false
				if (!YFCObject.isVoid(strCurrAuthExpiry)) {
					dateCurrAuthExpiryDate = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN).parse(strCurrAuthExpiry);
				}

				log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " the charge type is -ve AUTH. Date is " + dateCurrAuthExpiryDate + " and "
						+ Calendar.getInstance().getTime());
				// Ignoring the void call if authorization has expired
				if (YFCObject.isVoid(dateCurrAuthExpiryDate) || dateCurrAuthExpiryDate.before(Calendar.getInstance().getTime())) {
					docPaymentIn = getLatestExistingAuth(env, docPaymentIn);
					// No Paypal Void will be done on Complete Auth Expiry
					bIgnoreAuth = true; // DUMMY_AUTH
					bIsCompleteAuthVoided = false;

				} else {
					//START - Authreversal if rest call to be made Logic 
					//below code decides if a void call to be made or dummy-auth
					//Till OMNI-57431, all below scenarios are dummy-auth
					
					//Check if the complete Order is being voided with any settled lines
					bIgnoreAuth = validateOpenSettledLinesOnOrder(env, docPaymentIn);

					//check if Order has a full void or partial
					if(!bIgnoreAuth) {
						bIgnoreAuth = AcademyPaypalPaymentProcessingUtil.validateCompleteOrderCancel(docPaymentIn);
						//Complete Void call being invoked. complete Order cancelled
						if(!bIgnoreAuth){
							bIsCompleteAuthVoided = true;
						}
					}
					//END -Authreversal rest call to be made Logic 
				}
			}else if (AcademyConstants.STR_CHRG_TYPE_CHARGE.equalsIgnoreCase(chargeType)) {

				if (strReqAmt.startsWith(AcademyConstants.STR_HYPHEN)) {
					// Refund transaction has been triggered.
					log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " the charge type is -ve CHARGE i.e refund. Amount :: " + strReqAmt);
					strTransactionType = AcademyConstants.TRANSACTION_TYPE_REFUND;
					
				} else {
					//Start : OMNI-57724 
					/**Below part finds from charge transaction input 1. if
					 * transaction type is capture 2. Find the possibilibily of last shipment 3.
					 * Find json input for paypal rest call 4. Make rest call
					 * 
					 */
					// Order being Settled.
					log.verbose("Paypal Direct::  the charge type is CHARGE i.e settlement. Amount :: " + strReqAmt);
					// Settlement being done on existing AUTH
					// If we dont have valid auth for paypal capture call will fail.
					log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " Settlement being done on existing AUTH :: " + strAuthId
							);
					strTransactionType = AcademyConstants.TRANSACTION_TYPE_CAPTURE;
					// Split Shipment count is reqd to find if final settlement true or false.
					strSplitShipment = AcademyPaypalPaymentProcessingUtil.retrieveSplitShipmentCount(docPaymentIn);
					log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " strSplitShipment:" + strSplitShipment );
					if (YFCObject.isVoid(strAuthId)) { // No Valid Auth Id
						bIsPaypalReAuthCall = true;
						strTransactionType = AcademyConstants.TRANSACTION_TYPE_PURCHASE;
					}
				}
				// End : OMNI-57724
			}else if (AcademyConstants.STR_CHRG_TYPE_AUTH.equalsIgnoreCase(chargeType)) {// This is for Reauth call

				strTransactionType = AcademyConstants.TRANSACTION_TYPE_AUTHORIZE;
				bIsPaypalReAuthCall = true;
				log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " bIsPaypalReAuthCall: " + bIsPaypalReAuthCall );

			}

			log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " strTransactionType: " + strTransactionType + " bIgnoreAuth: " + bIgnoreAuth
					+ " bIsCompleteAuthVoided: "+bIsCompleteAuthVoided);

			if (bIgnoreAuth) { // No Call to be made to direct paypal
				log.verbose("Paypal Direct:: DUMMY_AUTH for CTK::"
						+ XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY)
						+ " :after direct paypal");
			}else if (AcademyConstants.TRANSACTION_TYPE_VOID.equalsIgnoreCase(strTransactionType)) {				
				//OMNI-57431 - Starts
				// AcademyConstants.TRANSACTION_TYPE_VOID - Last Cancellation/Full Cancellation
				log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " doing void transactions API call. strTransactionType: "
						+ strTransactionType );
				// Do a void on the AuthorizationID for request amount.
				restParameters.put(AcademyConstants.WEBSERVICE_PAYPAL_URL, strPayPalUrlVoid);
				// Get new Time-Out Reversal id if old one does not exist
				String strPaypalReqId = getRequestIdOfTimeOutIfExists(env, strOrderHeaderKey, strChargeTransactionKey,
						AcademyConstants.STR_SERVICE_UNAVAILABLE) == null ? AcademyPaypalPaymentProcessingUtil.getAutoGeneratedUUID()
								: getRequestIdOfTimeOutIfExists(env, strOrderHeaderKey, strChargeTransactionKey, AcademyConstants.STR_SERVICE_UNAVAILABLE);
				log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " strPaypalReqId: " + strPaypalReqId );
				restParameters.put(AcademyConstants.STR_PAYPAL_REQUEST_ID, strPaypalReqId);
				strPaypalInp = AcademyPaypalPaymentProcessingUtil.preparePaypalTransInp(docPaymentIn, AcademyConstants.TRANSACTION_TYPE_VOID, null);
				log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " JSON Input: " + strPaypalInp );
				// Make Direct Paypal call
				docRespPaypal = AcademyPayPalRestUtil.callDirectPaypalRestService(env,docPaymentIn, restParameters, strPaypalInp,strTransactionType);				
				docRespPaypal.getDocumentElement().setAttribute(AcademyConstants.ATTR_PAYPAL_REQUEST_ID, strPaypalReqId);
				
				log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " response from Paypal" + XMLUtil.getXMLString(docRespPaypal));
			} else if (AcademyConstants.TRANSACTION_TYPE_REFUND.equalsIgnoreCase(strTransactionType)) {
				//OMNI-57906 - Starts
				// TRANSACTION_TYPE_REFUND
				log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " doing refund transactions API call." + strTransactionType
						);
				// Process refund based on capture
				docRespPaypal = processTaggedRefunds(env, docPaymentIn, strPayPalUrlRefund, restParameters);
				if (!YFCObject.isVoid(docRespPaypal)) {					
					log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " response from Paypal" + XMLUtil.getXMLString(docRespPaypal));
				} else {
					log.verbose("Paypal Direct::  No Valid CHARGE available for Refund  after direct paypal.");
					docRespPaypal = XMLUtil.createDocument(AcademyConstants.ELE_ERROR);
					docRespPaypal.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, AcademyConstants.STATUS_CODE_NO_VALID_SETTLEMENT);
					docRespPaypal.getDocumentElement().setAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_NAME, AcademyConstants.NO_VALID_SETTLEMENT);
					docRespPaypal.getDocumentElement().setAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_MESSAGE, AcademyConstants.STR_DPP_NO_VALID_SETTLEMENT); 
				}
				
				//OMNI-57906 - Ends
			} 
			else if (bIsPaypalReAuthCall) {
				log.verbose("Paypal Direct::  strChargeTransactionKey: " + strChargeTransactionKey + " doing paypal auth transactions API call. strTransactionType: " +
				        strTransactionType );
				//For Purchase find if previous capture call timed out
				String strPaypalAuthId = AcademyConstants.TRANSACTION_TYPE_PURCHASE.equals(strTransactionType) ?
				        getPreviousAuthIdForPurchase(env, docPaymentIn, strTransactionType):null;
				if (YFCObject.isVoid(strPaypalAuthId)) { // Invoke Paypal system to get authorization
					restParameters.put(AcademyConstants.WEBSERVICE_PAYPAL_URL, strPayPalUrlAuthorization);
				    String strStoredPaypalReqId =  AcademyConstants.TRANSACTION_TYPE_PURCHASE.equals(strTransactionType)?
				       		getPaypalReqIdForPurchase(env, strOrderHeaderKey, strChargeTransactionKey,
				      				AcademyConstants.STR_SERVICE_UNAVAILABLE,AcademyConstants.TRANSACTION_TYPE_AUTHORIZE):
				      					getRequestIdOfTimeOutIfExists(env, strOrderHeaderKey, strChargeTransactionKey,
										AcademyConstants.STR_SERVICE_UNAVAILABLE);
				    String strPaypalReqId = YFCObject.isVoid(strStoredPaypalReqId) ? AcademyPaypalPaymentProcessingUtil.getAutoGeneratedUUID():strStoredPaypalReqId;										
				    restParameters.put(AcademyConstants.STR_PAYPAL_REQUEST_ID, strPaypalReqId);
				    // Get the Paypal REST request in JSON format
				    strPaypalInp = AcademyPaypalPaymentProcessingUtil.preparePaypalTransInp(docPaymentIn, AcademyConstants.TRANSACTION_TYPE_AUTHORIZE, null);
				    log.verbose("Paypal Direct::  strChargeTransactionKey: " + strChargeTransactionKey + " JSON Input: " + strPaypalInp );
				    // Call paypal rest service
				    docRespPaypal = AcademyPayPalRestUtil.callDirectPaypalRestService(env,docPaymentIn, restParameters, strPaypalInp,AcademyConstants.TRANSACTION_TYPE_AUTHORIZE);
				    if(AcademyConstants.TRANSACTION_TYPE_PURCHASE.equals(strTransactionType))
				    {
				    	docRespPaypal.getDocumentElement().setAttribute(AcademyConstants.ATTR_PAYPAL_REQUEST_ID, strPaypalReqId+AcademyConstants.STR_AT+AcademyConstants.TRANSACTION_TYPE_AUTHORIZE);
				    }else {
				      	docRespPaypal.getDocumentElement().setAttribute(AcademyConstants.ATTR_PAYPAL_REQUEST_ID, strPaypalReqId);
				    }
				    // Form PaymentAuthreferece in case capture will fail
				    log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + "  response from Paypal after auth call: " +
				    XMLUtil.getXMLString(docRespPaypal));
				    if (AcademyConstants.TRANSACTION_TYPE_PURCHASE.equalsIgnoreCase(strTransactionType) && isPaypalAuthCallSuccess(docRespPaypal)) {
				    	strPaymentReferenceValue = setPaypalPurchaseAuthSuccessDtls(docRespPaypal);
				        strPaypalAuthId = docRespPaypal.getDocumentElement().getAttribute(AcademyConstants.PAYPAL_RESPONSE_ID); // AuthId Received
				        log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " strPaymentReferenceValue:" + strPaymentReferenceValue +
				                    " after direct paypal.");
				    }
				 }

				 log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + "  strPaypalAuthId: " + strPaypalAuthId );
				 if (AcademyConstants.TRANSACTION_TYPE_PURCHASE.equalsIgnoreCase(strTransactionType) && !YFCStringUtil.isVoid(strPaypalAuthId)) {
					 // Get new Time-Out Reversal id if old one does not exist
					 String strPaypalReqId = getPaypalReqIdForPurchase(env, strOrderHeaderKey, strChargeTransactionKey,
		        	 AcademyConstants.STR_SERVICE_UNAVAILABLE,AcademyConstants.TRANSACTION_TYPE_CAPTURE) == null ? AcademyPaypalPaymentProcessingUtil.getAutoGeneratedUUID()
							: getPaypalReqIdForPurchase(env, strOrderHeaderKey, strChargeTransactionKey,
					     				AcademyConstants.STR_SERVICE_UNAVAILABLE,AcademyConstants.TRANSACTION_TYPE_CAPTURE);
					 restParameters.put(AcademyConstants.STR_PAYPAL_REQUEST_ID, strPaypalReqId);
					 strPaypalInp = AcademyPaypalPaymentProcessingUtil.preparePaypalTransInp(docPaymentIn, AcademyConstants.TRANSACTION_TYPE_CAPTURE,
							 "1/99");
					 log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " JSON Input: " + strPaypalInp );
					 restParameters.put(AcademyConstants.WEBSERVICE_PAYPAL_URL, strPayPalUrlCapture);
					 docPaymentIn.getDocumentElement().setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, strPaypalAuthId);
					 docRespPaypal = AcademyPayPalRestUtil.callDirectPaypalRestService(env,docPaymentIn, restParameters, strPaypalInp,AcademyConstants.TRANSACTION_TYPE_CAPTURE);				        
				    
					 docRespPaypal.getDocumentElement().setAttribute(AcademyConstants.ATTR_PAYPAL_REQUEST_ID, strPaypalReqId+AcademyConstants.STR_AT+AcademyConstants.TRANSACTION_TYPE_CAPTURE);
					 docRespPaypal.getDocumentElement().setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, strPaypalAuthId);
					 log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " response from Paypal after capture call: " + XMLUtil.getXMLString(docRespPaypal));
					 docPaymentIn.getDocumentElement().removeAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID);
				 }			
				 
			}else if (!YFCObject.isVoid(strTransactionType)) { // AcademyConstants.TRANSACTION_TYPE_CAPTURE
				log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + "  doing paypal capture transactions API call. strTransactionType: "
						+ strTransactionType );
				//OMNI-72334 - Start
				if (AcademyConstants.TRANSACTION_TYPE_CAPTURE.equalsIgnoreCase(strTransactionType))
					restParameters.put(AcademyConstants.WEBSERVICE_PAYPAL_URL, strPayPalUrlCapture);
				
				// Get new Time-Out Reversal id if old one does not exist
				String strPaypalReqId = getRequestIdOfTimeOutIfExists(env, strOrderHeaderKey, strChargeTransactionKey,
						AcademyConstants.STR_SERVICE_UNAVAILABLE) == null ? AcademyPaypalPaymentProcessingUtil.getAutoGeneratedUUID()
								: getRequestIdOfTimeOutIfExists(env, strOrderHeaderKey, strChargeTransactionKey, AcademyConstants.STR_SERVICE_UNAVAILABLE);
				log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " strPaypalReqId: " + strPaypalReqId );
				restParameters.put(AcademyConstants.STR_PAYPAL_REQUEST_ID, strPaypalReqId);
				//OMNI-72334 - End
				// Get the Paypal REST resquest in JSON format
				strPaypalInp = AcademyPaypalPaymentProcessingUtil.preparePaypalTransInp(docPaymentIn, strTransactionType, strSplitShipment);
				log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + "  JSON Input: " + strPaypalInp );
				// Call the paypal rest service				
				docRespPaypal = AcademyPayPalRestUtil.callDirectPaypalRestService(env,docPaymentIn, restParameters, strPaypalInp,strTransactionType);				
				docRespPaypal.getDocumentElement().setAttribute(AcademyConstants.ATTR_PAYPAL_REQUEST_ID, strPaypalReqId);			
				log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " response from Paypal" + XMLUtil.getXMLString(docRespPaypal));
			}else {
				log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey
						+ " Exception scenario where webservice call is not eligible.");				
				docRespPaypal = XMLUtil.createDocument(AcademyConstants.ELE_ERROR);
				docRespPaypal.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, AcademyConstants.STR_RESPONSE_UNDEFINED);
				docRespPaypal.getDocumentElement().setAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_NAME, AcademyConstants.STATUS_CODE_ERROR);
				docRespPaypal.getDocumentElement().setAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_MESSAGE, "Webservice Call is not eligible");
     		}
			log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " forming response: bIgnoreAuth: " + bIgnoreAuth);
			// Parse the Paypal response to form Payment UE Output XML
			if (AcademyConstants.STR_CHRG_TYPE_AUTH.equalsIgnoreCase(chargeType)) {
				// Form Payment UE Output for paypal for charge type AUTHORIZATION
				docPaymentOut = formPaymentOutputForAuth(env, docPaymentIn, docRespPaypal, strTransactionType, strPaymentReferenceValue, 
						bIgnoreAuth, bIsCompleteAuthVoided, strIsDBLoggingEnableForAuth, strPaypalInp);
			}else if (AcademyConstants.STR_CHRG_TYPE_CHARGE.equalsIgnoreCase(chargeType)) {
				// Form Payment UE Output for paypal for charge type CHARGE
				//Start : OMNI-57724
				docPaymentOut = formPaymentOutputForCharge(env, docPaymentIn, docRespPaypal, strTransactionType, strPaymentReferenceValue, 
						strIsDBLoggingEnableForCharge, strPaypalInp, strSplitShipment);
				//End : OMNI-57724
			}
			// Updating the TranType for each transaction
			docPaymentOut.getDocumentElement().setAttribute(AcademyConstants.ATTR_TRAN_TYPE, chargeType);
			log.verbose("Paypal Direct:: AcademyProcessPaypalPaymentAPI.processDirectPaypalPayment() strChargeTransactionKey: " + strChargeTransactionKey
					+ " Payment Output XML:: " + XMLUtil.getXMLString(docPaymentOut) );
		} catch (Exception exp) {//Any Exception while making rest call, eg. get access token call, paypal-response null
			String strOrderNo=XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_NO);
			String strSplunkDtls =AcademyConstants.PAYPAL_TRACE_SPLUNK.replace("{APIName}",strTransactionType);
			strSplunkDtls = strSplunkDtls.replace("{CTK}",strChargeTransactionKey);
			strSplunkDtls = strSplunkDtls.replace("{OrderNo}",strOrderNo);
			strSplunkDtls = strSplunkDtls.replace("{ResponseCode}",AcademyConstants.REASON_CODE);
			log.info("Paypal Direct:: Exception :: " +exp.toString() + strSplunkDtls);
			log.error(exp);
			docPaymentOut = XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT);
			Element elePaymentOut = docPaymentOut.getDocumentElement();
			AcademyPaypalPaymentProcessingUtil.retryTransaction(elePaymentOut,
					props.getProperty(AcademyConstants.STR_NEXT_TRIGGER_IN_MIN), AcademyConstants.STR_DPP_WEB_SRV_CALL_ERROR,AcademyConstants.STR_SERVICE_UNAVAILABLE);
		} finally {
			log.endTimer("AcademyProcessPaypalPaymentAPI.processDirectPaypalPayment()");
			log.debug("Exiting AcademyProcessPaypalPaymentAPI.processDirectPaypalPayment()");
		}
		return docPaymentOut;
	}

	/**
	 * This method prepares and updates TASK_Q table with orders which need to be
	 * cancelled *
	 * 
	 * @param env
	 * @param docPaymentInp
	 * @throws Exception
	 */
	private void createManageTaskQueue(YFSEnvironment env, Document docPaymentInp) throws Exception {
		
		log.debug("Begin of AcademyProcessPaypalPaymentAPI.createManageTaskQueue() method after direct paypal.");
		Document docTaskQueue = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
		Element eleTaskQueue = docTaskQueue.getDocumentElement();

		eleTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_KEY,
				docPaymentInp.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));

		eleTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_TYPE, AcademyConstants.ATTR_ORDER_HEADER_KEY);
		eleTaskQueue.setAttribute(AcademyConstants.ATTR_TRANSID, AcademyConstants.TRANSACTION_DECLINED_ORDER_AGENT);

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
		eleTaskQueue.setAttribute(AcademyConstants.ATTR_AVAIL_DATE, sdf.format(cal.getTime()));

		log.verbose("Paypal Direct:: Input to manageTaskQueue :: \n" + XMLUtil.getXMLString(docTaskQueue) );
		AcademyUtil.invokeAPI(env, "manageTaskQueue", docTaskQueue);
		log.debug("End of AcademyProcessPaypalPaymentAPI.createManageTaskQueue() method after direct paypal.");
	}

	/**
	 * This method retrieves PayPal AUTH details if available
	 * 
	 * @param env
	 * @param docPaymentInp
	 * @return
	 * @throws Exception
	 */
	private String retrievePaypalAuthDetails(YFSEnvironment env, Document docPaymentInp) throws Exception {
		log.debug(
				"Begin of AcademyProcessPaypalPaymentAPI.retrievePaypalAuthDetails() method after direct paypal.");
		log.verbose("Paypal Direct:: docPaymentInp:" + XMLUtil.getXMLString(docPaymentInp));
		String strPayPalAuth = null;
		List lstPaymentTransactionError = null;
		String strOrderHeaderKey = docPaymentInp.getDocumentElement()
				.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
		String strChargeTransactionKey = docPaymentInp.getDocumentElement()
				.getAttribute(AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY);

		if (YFCObject.isVoid(docOrderList)) {
			docOrderList = getOrderList(env, strOrderHeaderKey);
		}
		lstPaymentTransactionError = XMLUtil.getElementListByXpath(docOrderList,
				"OrderList/Order/ChargeTransactionDetails/ChargeTransactionDetail[@ChargeTransactionKey='"
						+ strChargeTransactionKey + "']/PaymentTransactionErrorList/PaymentTransactionError["
						+ "@MessageType='" + AcademyConstants.STR_PAYPAL_PURCHASE + "']");

		if (lstPaymentTransactionError.size() > 0) {
			Element elePaymentTransactionError = (Element) lstPaymentTransactionError.get(0);
			strPayPalAuth = elePaymentTransactionError.getAttribute(AcademyConstants.ATTR_MESSAGE);
		}

		log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + ":: strPayPalAuth :: " + strPayPalAuth);
		log.debug("End of AcademyProcessPaypalPaymentAPI.retrievePaypalAuthDetails() method after direct paypal.");
		return strPayPalAuth;
	}

	/**
	 * This method invokes getOrderList API
	 * 
	 * @param env
	 * @param strOrderHeaderKey
	 * @return
	 * @throws Exception
	 */
	private static Document getOrderList(YFSEnvironment env, String strOrderHeaderKey) throws Exception {

		log.debug("Begin of AcademyProcessPaypalPaymentAPI.getOrderList() method after direct paypal.");
		log.verbose("Paypal Direct:: strOrderHeaderKey:" + strOrderHeaderKey);
		Document docGetOrderListInp = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		docGetOrderListInp.getDocumentElement().setAttribute(AcademyConstants.STR_ORDR_HDR_KEY, strOrderHeaderKey);

		Document docGetOrderListOut = AcademyUtil.invokeService(env,
				AcademyConstants.SERV_ACAD_GET_ORDER_LIST_FOR_PAYMENT_SERVICE, docGetOrderListInp);

		log.debug("End of AcademyProcessPaypalPaymentAPI.getOrderList() method after direct paypal.");
		return docGetOrderListOut;
	}

	/**
	 * This method parses the PayPal webservice response to Payment output XML
	 * 
	 * @param env
	 * @param docPaymentIn
	 * @param docRespPaypal
	 * @param strTransactionType
	 * @param objParameter
	 * @return
	 * @throws Exception
	 */

	private Document translatePaypalResp(YFSEnvironment env, Document docPaymentIn, Document docRespPaypal,
			String strTransactionType, Object objParameter) throws Exception {
		log.debug("Begin of AcademyProcessPaypalPaymentAPI.translatePaypalResp() method after direct paypal.");
		log.verbose("Paypal Direct:: docPaymentIn:" + XMLUtil.getXMLString(docPaymentIn));
		log.verbose("Paypal Direct:: docRespPaypal:" + XMLUtil.getXMLString(docRespPaypal));
		log.verbose("Paypal Direct:: strTransactionType:" + strTransactionType );
		log.verbose("Paypal Direct:: objParameter:" + objParameter );
		String strChargeTransactionKey = XPathUtil.getString(docPaymentIn,
				AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY);
		Document docPaymentOut = XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT);
		Element elePaymentOut = docPaymentOut.getDocumentElement();
		Element elePaypalResp = docRespPaypal.getDocumentElement();
		String strHttpStatusCode = elePaypalResp.getAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE);
		String strState = elePaypalResp.getAttribute(AcademyConstants.ATTR_PAYPAL_RESPONSE_STATE);// eg.completed for
																									// capture
		String strAmount = XMLUtil.getString(elePaypalResp, AcademyConstants.JSON_PP_ATTR_AMOUNT);
		if (strHttpStatusCode.startsWith(AcademyConstants.STATUS_CODE_2XX)) {// web service call is successful
			log.verbose("Paypal Direct:: Valid HTTP Status response. " + strHttpStatusCode );
			log.verbose(
					"Paypal Direct:: For OrderNo :" + XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH + "@OrderNo")
							+ ", Amount : " + strAmount + " , strState of  call :" + strState + " , strState of  call :"
							+ strState );
			if (strState == null) {
				elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE,
						AcademyConstants.STR_SERVICE_UNAVAILABLE);
				elePaymentOut.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_YES);
				elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE,
						AcademyConstants.STR_SERVICE_UNAVAILABLE_DESC);
				return docPaymentOut;
			}
			// Is response is approved ?
			if (AcademyPaypalPaymentProcessingUtil.isRestResponsePropType(mapTaskPropDtls,
					AcademyConstants.PROP_PAYPAL_SUCCESS_NAME, strTransactionType, strHttpStatusCode, strState)) {
				AcademyPaypalPaymentProcessingUtil.prepareApprovedResp(docPaymentIn, elePaymentOut, docRespPaypal,
						strTransactionType, strPPAuthExpiryDays);
			} else {// web service call response is 2XX & response is denied/failed
				// Check if
				// yfs.academy.paypal.dummysettlement.error.name==2XX/denied/capture,2XX/denied/void,2XX/failed/refund,4XX/X/X
				if (AcademyPaypalPaymentProcessingUtil.isRestResponsePropType(mapTaskPropDtls,
						AcademyConstants.PROP_PAYPAL_DUMMY_SETTLE_ERROR_NAME, strTransactionType, strHttpStatusCode,
						strState)) {
					String strAlertErrorShortDesc = AcademyConstants.STR_PAYPAL_DENIED_ERROR;//"Paypal Denied";
					log.debug("Paypal Direct:: Calling AcademyPaypalPaymentProcessingUtil.prepareDisapprovedResponse() method");
					AcademyPaypalPaymentProcessingUtil.prepareDisapprovedResponse(docPaymentIn, elePaymentOut,
							docRespPaypal, strTransactionType, strAlertErrorShortDesc);
				}else {//unhandled alert to be raised and charge should remain open
					if(!AcademyConstants.TRANSACTION_TYPE_REFUND.equals(strTransactionType)||
							(AcademyConstants.TRANSACTION_TYPE_REFUND.equals(strTransactionType)&& objParameter instanceof Boolean))
					{
						String strOrderNo = XPathUtil.getString(docPaymentIn,AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_NO);
						//log.info("Paypal Direct:: Unexpected Business Response ::" +strSplunkDtls);
						log.info(AcademyPaypalPaymentProcessingUtil.getLoggerInfoString(AcademyConstants.STR_LOG_INFO_UNEXPECTED_ERROR,elePaypalResp,
								strTransactionType,strHttpStatusCode,strChargeTransactionKey, strOrderNo ));
						AcademyPaypalPaymentProcessingUtil.raiseUnHandledErrorAlert(env,docPaymentIn, 
							docRespPaypal, strTransactionType,AcademyConstants.STR_UNEXPECTED_ERROR,AcademyConstants.STR_ACAD_UNHANDLED_ERROR_DPP_ALERT);
					}
					// To log to yfs_export table but not to raise collection failure event
					elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
					elePaymentOut.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_NO);
					elePaymentOut.setAttribute(AcademyConstants.ATTR_SUSPEND_PAYMENT, AcademyConstants.STR_NO);
					elePaymentOut.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_MESSAGE, AcademyConstants.STR_UNHANDLED_ERROR);
				}
				// Check if
				// yfs.academy.paypal.harddecline.error.name=2XX/denied/capture,2XX/denied/void,2XX/failed/refund
				if (AcademyPaypalPaymentProcessingUtil.isRestResponsePropType(mapTaskPropDtls,
						AcademyConstants.PROP_PAYPAL_HARD_DECLINE_ERROR_NAME, strTransactionType, strHttpStatusCode,
						strState)) {
					// Business Cancellation
					hardDeclineOrder(env, docPaymentIn, strTransactionType);
				}
			}

		} else {// web service call response is not 2XX
			log.debug("Paypal Direct:: Calling translatePaypalFailedlResp method");
			translatePaypalFailedlResp(env, docPaymentIn, elePaymentOut, docRespPaypal, strTransactionType,
					objParameter);

		}
		log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " docPaymentOut: "
				+ XMLUtil.getXMLString(docPaymentOut));
		log.debug("End of AcademyProcessPaypalPaymentAPI.translatePaypalResp() method after direct paypal.");
		return docPaymentOut;

	}

	/**
	 * This method finds latest valid Authorization ID and added to Input docF
	 * 
	 * @param env
	 * @param docPaymentIn
	 * @return
	 */

	private Document getLatestExistingAuth(YFSEnvironment env, Document docPaymentIn) throws Exception {
		log.debug("Begin of AcademyProcessPaypalPaymentAPI.getLatestExistingAuth() method after direct paypal.");
		log.verbose("Paypal Direct:: docPaymentIn:" + XMLUtil.getXMLString(docPaymentIn) );
		Element eleChargeTransactionDetail = null;
		String strAuthorizationID = "";
		String strPaypalAuthId = "";
		String strOrderHeaderKey = docPaymentIn.getDocumentElement()
				.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);

		if (YFCObject.isVoid(docOrderList)) {
			docOrderList = getOrderList(env, strOrderHeaderKey);
		}
		if (!YFSObject.isVoid(docOrderList)) {
			log.verbose("Paypal Direct:: getOrderList output::" + XMLUtil.getXMLString(docOrderList));
			NodeList nCreatedRetOrderLines = XPathUtil.getNodeList(docOrderList,
					"OrderList/Order/ChargeTransactionDetails/ChargeTransactionDetail[@ChargeType='AUTHORIZATION' and @RequestAmount>0 and @AuthorizationID!='']");
			int iChargeTransactionDetailCount = nCreatedRetOrderLines.getLength();

			log.verbose("Paypal Direct:: iChargeTransactionDetailCount:  " + iChargeTransactionDetailCount );
			for (int iCount = 0; iCount < iChargeTransactionDetailCount; iCount++) {
				eleChargeTransactionDetail = (Element) nCreatedRetOrderLines.item(iCount);
				strAuthorizationID = eleChargeTransactionDetail
						.getAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID_CAP);
				strPaypalAuthId = XPathUtil.getString(eleChargeTransactionDetail,
						AcademyConstants.PAYMENT_PATH_FOR_AUTH_CODE);
			}

			log.verbose("Paypal Direct:: strAuthorizationID:  " + strAuthorizationID );
			log.verbose("Paypal Direct:: strPaypalAuthId:  " + strPaypalAuthId );
			docPaymentIn.getDocumentElement().setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, strAuthorizationID);
			if (!YFSObject.isVoid(strPaypalAuthId))
				docPaymentIn.getDocumentElement().setAttribute(AcademyConstants.PAYPAL_RESPONSE_ID, strPaypalAuthId);
		}

		log.debug("End of AcademyProcessPaypalPaymentAPI.getLatestExistingAuth() method after direct paypal.");
		return docPaymentIn;
	}

	/**
	 * This Method sets paypal retry attributes and update payment error for
	 * purchase
	 * @param docPaymentIn
	 * @param docRespPaypal
	 * @param elePaymentOut
	 * @param objParameter
	 * @return
	 * @throws Exception
	 */

	private Element setPurchaseCaptureRetryAttributes(Document docPaymentIn, Document docRespPaypal,
			Element elePaymentOut, Object objParameter) throws Exception {
		log.debug(
				"Begin of AcademyProcessPaypalPaymentAPI.setPayPalAuthRetryAttributes() method after direct paypal.");
		elePaymentOut = AcademyPaypalPaymentProcessingUtil.updateFailedForPurchase(elePaymentOut,
				objParameter + AcademyConstants.STR_AT + AcademyConstants.STR_SUSPEND_AUTH);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, AcademyConstants.STR_FORCED_RETRY);
		log.debug("End of AcademyProcessPaypalPaymentAPI.setPayPalAuthRetryAttributes() metho dafter direct paypal.");
		return elePaymentOut;
	}

	/**
	 * This method log details to DB
	 * 
	 * @param env
	 * @param strPaypalInp
	 * @param docPaymentIn
	 * @param docRespPaypal
	 * @throws Exception
	 */
	private void logDetailsToDB(YFSEnvironment env, String strPaypalInp, Document docPaymentIn, Document docRespPaypal)
			throws Exception {
		log.debug(
				"Begin of AcademyProcessPaypalPaymentAPI.logDetailsToDB() method after direct paypal.");
		log.verbose("Paypal Direct:: Logging request and response in DB" );
		if (!YFSObject.isVoid(strPaypalInp)) {
			Document docReqPaypal=null;
			try {
				InputStream in = IOUtils.toInputStream(strPaypalInp);
				String xml = XML.toXml(in);
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				docReqPaypal = builder.parse(new InputSource(new StringReader(xml)));
			}catch(Exception exp) {//Syntactical Error to parse to xml
				log.verbose(exp);
				docReqPaypal = XMLUtil.createDocument(AcademyConstants.ELE_PAYPAL_REQ_INPUT);
				docReqPaypal.getDocumentElement().setAttribute(AcademyConstants.ATTR_TEXT, strPaypalInp);
			}
			AcademyPaypalPaymentProcessingUtil.logPaypalReqAndRespToDB(env, docPaymentIn, docReqPaypal, docRespPaypal,
					AcademyConstants.STR_DB_SERVICE_FOR_PAYPAL);
		} else if (!YFSObject.isVoid(docRespPaypal)) {
			AcademyPaypalPaymentProcessingUtil.logPaypalReqAndRespToDB(env, docPaymentIn, null, docRespPaypal,
					AcademyConstants.STR_DB_SERVICE_FOR_PAYPAL);
		}
		log.debug("End of AcademyProcessPaypalPaymentAPI.logDetailsToDB() metho dafter direct paypal.");
	}

	/**
	 * This method prepares the UE output document in case of SERVICE_UNAVAILABLE
	 * scenarios
	 * yfs.academy.paypal.srvunavlb.error.name=500/X/X,501/X/X,502/X/X,503/X/X,505/X/X,506/X/X,507/X/X,508/X/X,510/X/X,511/X/X,4XX/INTERNAL_SERVICE_ERROR/X
	 * 
	 * @param env
	 * @param docPaymentIn
	 * @param elePaymentOut
	 * @param docRespPaypal
	 * @param strTransactionType
	 * @return
	 * @throws Exception
	 */
	private Element prepareServiceUnavailableResp(YFSEnvironment env, Document docPaymentIn, Element elePaymentOut,
			Document docRespPaypal, String strTransactionType) throws Exception {
		log.debug("Begin of AcademyProcessPaypalPaymentAPI.prepareServiceUnavailableResp() method after direct paypal.");
		String strOrderHeaderKey = XPathUtil.getString(docPaymentIn,
				AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_HEADER_KEY);
		String strChargeTransactionKey = XPathUtil.getString(docPaymentIn,
				AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY);
		int iErrorCount = getCountOfPaymentTransactionError(env, strOrderHeaderKey, strChargeTransactionKey);
		if (iErrorCount >= iMaxRetryCount) {
			log.verbose("Paypal Direct:: Max Retry Limit Reached :: " + iMaxRetryCount + " <= " + iErrorCount);
			String strTrasErrorShortDesc = AcademyConstants.STR_MAX_RETRY_LIMIT_REACHED;

			AcademyPaypalPaymentProcessingUtil.prepareDummyRespForAllTransType(docPaymentIn, elePaymentOut,
					docRespPaypal, strTransactionType, strTrasErrorShortDesc);
			hardDeclineOrder(env, docPaymentIn, strTransactionType);
		} else {
			formReprocessibleResponse(env, docPaymentIn, elePaymentOut, docRespPaypal, strTransactionType);
		}

		log.debug("End of AcademyProcessPaypalPaymentAPI.prepareServiceUnavailableResp() method after direct paypal.");
		return elePaymentOut;
	}
	/**
	 * This method is used to invoke paypal void in case a re-auth fails in paypal
	 * 
	 * @param docPaymentIn
	 * @param strEndpointURL
	 * @param restParameters
	 * @return docPaymentOut
	 */
	private Document invokePaypalVoidTransaction(YFSEnvironment env, Document docPaymentIn,
			Map<String, String> restParameters) throws Exception {
		log.debug(
				"Begin of AcademyProcessPaypalPaymentAPI.invokePayPalVoidTransaction() method after direct paypal.");
		Document docPayalVoidRes = null;
		// RequestId is not available as part of input xml
		docPaymentIn = getLatestExistingAuth(env, docPaymentIn);
		// Updated the Paypal VOID URL.
		restParameters.put(AcademyConstants.WEBSERVICE_PAYPAL_URL, strPayPalUrlVoid);
		// Invoke PayPal system to do a auth void call
		String strPaypalInp = AcademyPaypalPaymentProcessingUtil.preparePaypalTransInp(docPaymentIn,
				AcademyConstants.TRANSACTION_TYPE_VOID, null);
		docPayalVoidRes = AcademyPayPalRestUtil.callDirectPaypalRestService(env,docPaymentIn, restParameters, strPaypalInp,AcademyConstants.TRANSACTION_TYPE_VOID);
		log.debug("End of AcademyProcessPaypalPaymentAPI.invokePayPalVoidTransaction() method after direct paypal.");
		return docPayalVoidRes;
	}

	/**
	 * This method is used to get the List of Error Transaction for the Payment Type
	 * of the Order.
	 * 
	 * @param env
	 * @param strOrderHeaderKey
	 * @param strChargeTransactionKey
	 * @return
	 * @throws Exception
	 */
	private int getCountOfPaymentTransactionError(YFSEnvironment env, String strOrderHeaderKey,
			String strChargeTransactionKey) throws Exception {
		log.debug("Begin of AcademyProcessPaypalPaymentAPI.getCountOfPaymentTransactionError() method after direct paypal.");
		int iErrorCount = 0;

		if (YFCObject.isVoid(docOrderList)) {
			docOrderList = getOrderList(env, strOrderHeaderKey);
		}

		List lstPaymentTransactionError = XMLUtil.getElementListByXpath(docOrderList,
				"OrderList/Order/ChargeTransactionDetails/ChargeTransactionDetail[@ChargeTransactionKey='"
						+ strChargeTransactionKey + "']/PaymentTransactionErrorList/PaymentTransactionError");

		iErrorCount = lstPaymentTransactionError.size();

		log.verbose("Paypal Direct:: iErrorCount:" + iErrorCount);
		log.debug("End of AcademyProcessPaypalPaymentAPI.getCountOfPaymentTransactionError() method after direct paypal.");
		return iErrorCount;
	}

	/**
	 * This method returns if paypal authorized
	 * 
	 * @param docRespPaypal
	 * @return
	 */
	private boolean isPaypalAuthCallSuccess(Document docRespPaypal) {
		log.debug("Begin of AcademyProcessPaypalPaymentAPI.isPaypalAuthCallSuccess() method after direct paypal.");
		log.verbose("Paypal Direct:: docRespPaypal:"+ XMLUtil.getXMLString(docRespPaypal) );
		String strPayPalState = docRespPaypal.getDocumentElement()
				.getAttribute(AcademyConstants.ATTR_PAYPAL_RESPONSE_STATE);
		return (AcademyConstants.STR_PAYPAL_RESPONSE_AUTHOSIZED.equals(strPayPalState));
	}

	/**
	 * This method returns paypal authorization details
	 * 
	 * @param docRespPaypal
	 * @param strTransactionType
	 * @return
	 */
	private String setPaypalPurchaseAuthSuccessDtls(Document docRespPaypal) {
		log.debug("Begin of AcademyProcessPaypalPaymentAPI.setPaypalPurchaseAuthSuccessDtls() method after direct paypal.");
		log.verbose("Paypal Direct:: docRespPaypal:"+ XMLUtil.getXMLString(docRespPaypal) );
		String strPaymentReferenceValue = null;
		String strPayPalAuthTime = docRespPaypal.getDocumentElement()
				.getAttribute(AcademyConstants.ATTR_PAYPAL_RESPONSE_CREATE_TIME);
		String strId = docRespPaypal.getDocumentElement().getAttribute(AcademyConstants.PAYPAL_RESPONSE_ID);
		String strPayPalDebugId = docRespPaypal.getDocumentElement()
				.getAttribute(AcademyConstants.ATTR_PAYPAL_DEBUG_ID);
		strPaymentReferenceValue = AcademyConstants.STR_PP_PURCHASE_CAPTURE_FAIL + AcademyConstants.STR_AT + strId
				+ AcademyConstants.STR_AT + strPayPalDebugId + AcademyConstants.STR_AT + strPayPalAuthTime;
		log.debug("End of AcademyProcessPaypalPaymentAPI.setPaypalPurchaseAuthSuccessDtls() strPaymentReferenceValue:"
				+ strPaymentReferenceValue );
		return strPaymentReferenceValue;
	}

	/**
	 * This method finds if there is any prior authorization id for purchase call
	 * 
	 * @param env
	 * @param docPaymentIn
	 * @param strTransactionType
	 * @return
	 * @throws Exception
	 */
	private String getPreviousAuthIdForPurchase(YFSEnvironment env, Document docPaymentIn, String strTransactionType)
			throws Exception {
		log.debug("Begin of AcademyProcessPaypalPaymentAPI.getPreviousAuthIdForPurchase() method"
				+ XMLUtil.getXMLString(docPaymentIn) );
		String strId = null;
		if (AcademyConstants.TRANSACTION_TYPE_PURCHASE.equals(strTransactionType)) {
			String strPaymentReferenceValue = retrievePaypalAuthDetails(env, docPaymentIn);
			if (!YFCObject.isVoid(strPaymentReferenceValue)
					&& strPaymentReferenceValue.startsWith(AcademyConstants.STR_PP_PURCHASE_CAPTURE_FAIL)) {

				log.verbose("Paypal Direct:: :: strPaymentReferenceValue ::  " + strPaymentReferenceValue);
				String strPaymentReference[] = strPaymentReferenceValue.split(AcademyConstants.STR_AT);
				strId = strPaymentReference[1];

			}
		}
		log.debug("End of AcademyProcessPaypalPaymentAPI.paypalAuthSuccessDtls() getPreviousAuthIdForPurchase:"
				+ strId );
		return strId;
	}

	/**
	 * This method forms UE output for Charge Type AUTHORIZATION
	 * 
	 * @param env
	 * @param docPaymentIn
	 * @param docRespPaypal
	 * @param strTransactionType
	 * @param strPaymentReferenceValue
	 * @param bIgnoreAuth
	 * @param bIsCompleteAuthVoided
	 * @param strIsDBLoggingEnableForAuth
	 * @param strPaypalInp
	 * @return
	 * @throws Exception
	 */
	private Document formPaymentOutputForAuth(YFSEnvironment env, Document docPaymentIn, Document docRespPaypal,
			String strTransactionType, String strPaymentReferenceValue, boolean bIgnoreAuth,
			boolean bIsCompleteAuthVoided, String strIsDBLoggingEnableForAuth, String strPaypalInp) throws Exception {
		log.debug("Begin of AcademyProcessPaypalPaymentAPI.formPaymentOutputForAuth() method after direct paypal");
		log.verbose("Paypal Direct:: docPaymentIn:" + XMLUtil.getXMLString(docPaymentIn));
		log.verbose("Paypal Direct:: strTransactionType:" + strTransactionType);
		log.verbose("Paypal Direct:: strPaymentReferenceValue:" + strPaymentReferenceValue);
		log.verbose("Paypal Direct:: bIgnoreAuth:" + bIgnoreAuth);
		log.verbose("Paypal Direct:: bIsCompleteAuthVoided:" + bIsCompleteAuthVoided);
		log.verbose("Paypal Direct:: strIsDBLoggingEnableForAuth:" + strIsDBLoggingEnableForAuth);
		log.verbose("Paypal Direct:: strPaypalInp:" + strPaypalInp);		
		String strReqAmt = XPathUtil.getString(docPaymentIn,
				AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);
		String strAuthId = XPathUtil.getString(docPaymentIn,
				AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_AUTHORIZATION_ID);
		String strChargeTransactionKey = XPathUtil.getString(docPaymentIn,
				AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY);
		log.verbose("Paypal Direct:: strReqAmt:" + strReqAmt);
		log.verbose("Paypal Direct:: strAuthId:" + strAuthId);
		log.verbose("Paypal Direct:: strChargeTransactionKey:" + strChargeTransactionKey);
		Document docPaymentOut = null;
		if (!bIgnoreAuth) { // Paypal call made
			log.verbose("Paypal Direct:: docRespPaypal:" + XMLUtil.getXMLString(docRespPaypal));
			log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " Forming response: docRespPaypal: "
					+ XMLUtil.getXMLString(docRespPaypal) + " strTransactionType :" + strTransactionType);
			docPaymentOut = translatePaypalResp(env, docPaymentIn, docRespPaypal, strTransactionType,
					strPaymentReferenceValue);
			log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " translatePaypalResp: "
					+ XMLUtil.getXMLString(docPaymentOut));
		} else {// bIgnoreAuth=true VOID scenario no direct paypal call because no valid auth-id
				// exists
			docPaymentOut = XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT);
			Element elePaymentOut = docPaymentOut.getDocumentElement();
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, strAuthId);
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_CODE, strAuthId);
			elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_AMT, strReqAmt);
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_AMOUNT, strReqAmt);
			AcademyPaypalPaymentProcessingUtil.prepareDummyAuthResponse(elePaymentOut, AcademyConstants.STR_APPROVED,
					AcademyConstants.STR_DUMMY_AUTH, AcademyConstants.STR_DUMMY_AUTH, AcademyConstants.STR_DUMMY_AUTH,
					AcademyConstants.STR_DUMMY_AUTH);
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN_NEW);
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_TIME, sdf.format(cal.getTime()));
		}

		// As Id Functionality- PaymentReference9 contains split shipment count and
		// PaymentReference8
		// contains the Settled amount on the AUTH // If Complete AUTH has been voided
		// then reset the PaymentRef9 and PaymentRef8
		if (bIsCompleteAuthVoided) {
			docPaymentOut.getDocumentElement().setAttribute(AcademyConstants.ATTR_PAYMENT_REF_9, "");
			docPaymentOut.getDocumentElement().setAttribute(AcademyConstants.ATTR_PAYMENT_REF_8, "");

		}
		bIgnoreAuth = false;// reset the flag
		// Log failed web service response in DB if db logging is enabled
		if (AcademyConstants.STR_YES.equalsIgnoreCase(strIsDBLoggingEnableForAuth)
				&& !docPaymentOut.getDocumentElement().getAttribute(AcademyConstants.ATTR_RESPONSE_CODE)
						.equalsIgnoreCase(AcademyConstants.STR_APPROVED)) {
			logDetailsToDB(env, strPaypalInp, docPaymentIn, docRespPaypal);
		}
		log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " Payment authorization xml::"
				+ XMLUtil.getXMLString(docPaymentOut) );
		log.debug("End of AcademyProcessPaypalPaymentAPI.formPaymentOutputForAuth() method after direct paypal.");

		return docPaymentOut;

	}

	/**
	 * This method forms output of UE for Charge Type CHARGE
	 * 
	 * @param env
	 * @param docPaymentIn
	 * @param docRespPaypal
	 * @param strTransactionType
	 * @param strPaymentReferenceValue
	 * @param strIsDBLoggingEnableForCharge
	 * @param strPaypalInp
	 * @param strSplitShipment
	 * @return
	 * @throws Exception
	 */
	private Document formPaymentOutputForCharge(YFSEnvironment env, Document docPaymentIn, Document docRespPaypal,
			String strTransactionType, String strPaymentReferenceValue, String strIsDBLoggingEnableForCharge,
			String strPaypalInp, String strSplitShipment) throws Exception {
		log.debug("Begin of AcademyProcessPaypalPaymentAPI.formPaymentOutputForCharge() method after direct paypal.");
		log.verbose("Paypal Direct:: docPaymentIn:" + XMLUtil.getXMLString(docPaymentIn));
		log.verbose("Paypal Direct:: docRespPaypal:" + XMLUtil.getXMLString(docRespPaypal));
		log.verbose("Paypal Direct:: strTransactionType:" + strTransactionType);
		log.verbose("Paypal Direct:: strIsDBLoggingEnableForCharge:" + strIsDBLoggingEnableForCharge);
		log.verbose("Paypal Direct:: strPaypalInp:" + strPaypalInp);
		log.verbose("Paypal Direct:: strSplitShipment:" + strSplitShipment);
		String strReqAmt = XPathUtil.getString(docPaymentIn,
				AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);
		String strChargeTransactionKey = XPathUtil.getString(docPaymentIn,
				AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY);
		log.verbose("Paypal Direct:: strReqAmt:" + strReqAmt);
		log.verbose("Paypal Direct:: strChargeTransactionKey:" + strChargeTransactionKey);
		Document docPaymentOut = null;
		
		docPaymentOut = translatePaypalResp(env, docPaymentIn, docRespPaypal, strTransactionType,
				strPaymentReferenceValue);
		// Refund part will be here
		if (!YFCObject.isVoid(strTransactionType)
				&& AcademyConstants.TRANSACTION_TYPE_REFUND.equals(strTransactionType)) {
			// Suspend Payment if the web service output is declined for refunds.
			if (docPaymentOut.getDocumentElement().getAttribute(AcademyConstants.ATTR_RESPONSE_CODE)
					.equalsIgnoreCase(AcademyConstants.STR_HARD_DECLINED)) {
				docPaymentOut.getDocumentElement().setAttribute(AcademyConstants.ATTR_SUSPEND_PAYMENT,
						AcademyConstants.STR_YES);
			}
			log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " hmFailedReversalId:"
					+ hmFailedReversalId.toString() + " docPaymentOut " + XMLUtil.getXMLString(docPaymentOut)
					+ " strTransactionType :" + strTransactionType );
			if (hmFailedReversalId.size() > 0) {
				log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey
						+ "  Failed Reversals available for Refund after direct paypal.");
				// update payment error element for failed refund for trying,eg service
				// unavailable error
				docPaymentOut = updateFailedReversalsForRefund(env, docPaymentIn, docPaymentOut);
			}
		}
		String strIsDummySettlement = docPaymentOut.getDocumentElement()
				.getAttribute(AcademyConstants.ATTR_IS_DUMMY_SETTLEMENT);
		log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " Payment settlement/refund xml::"
				+ XMLUtil.getXMLString(docPaymentOut) );
		// Log failed web service response in DB if db logging is enabled
		if (AcademyConstants.STR_YES.equalsIgnoreCase(strIsDBLoggingEnableForCharge)
				&& (!docPaymentOut.getDocumentElement().getAttribute(AcademyConstants.ATTR_RESPONSE_CODE)
						.equalsIgnoreCase(AcademyConstants.STR_APPROVED))
				|| AcademyConstants.STR_YES.equalsIgnoreCase(strIsDummySettlement)) {
			logDetailsToDB(env, strPaypalInp, docPaymentIn, docRespPaypal);

		}

		if (docPaymentOut.getDocumentElement().getAttribute(AcademyConstants.ATTR_RESPONSE_CODE)
				.equalsIgnoreCase(AcademyConstants.STR_APPROVED)) {
			// Update SplitShipment count in PaymentReference9
			docPaymentOut.getDocumentElement().setAttribute(AcademyConstants.ATTR_PAYMENT_REF_9, strSplitShipment);
			// Update Settled amount in PaymentReference8
			double dSettledAmount = 0.0;
			double dReqAmount = 0.0;
			DecimalFormat df = new DecimalFormat("0.00");
			String strSettledAmount = XPathUtil.getString(docPaymentIn,
					AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_PAYMENT_REF_8);
			if (!YFCObject.isVoid(strSettledAmount)) {
				dSettledAmount = Double.parseDouble(strSettledAmount);
			}
			if (!YFCObject.isVoid(strReqAmt)) {
				dReqAmount = Double.parseDouble(strReqAmt);
			}
			docPaymentOut.getDocumentElement().setAttribute(AcademyConstants.ATTR_PAYMENT_REF_8,
					df.format(dSettledAmount + dReqAmount));
		} else {
			// Update the original value itself in the output
			docPaymentOut.getDocumentElement().setAttribute(AcademyConstants.ATTR_PAYMENT_REF_9, XPathUtil.getString(
					docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_PAYMENT_REF_9));
			docPaymentOut.getDocumentElement().setAttribute(AcademyConstants.ATTR_PAYMENT_REF_8, XPathUtil.getString(
					docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_PAYMENT_REF_8));
		}
		log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " Payment charge xml::"
				+ XMLUtil.getXMLString(docPaymentOut) );
		log.debug("End of AcademyProcessPaypalPaymentAPI.formPaymentOutputForCharge() method after direct paypal.");
		return docPaymentOut;

	}

	/**
	 * This method is used to get the ReversalID of the last timed out error
	 * 
	 * @param env
	 * @param strOrderHeaderKey
	 * @param strChargeTransactionKey
	 * @param strMessageType
	 * @return
	 * @throws Exception
	 */
	private String getRequestIdOfTimeOutIfExists(YFSEnvironment env, String strOrderHeaderKey,
			String strChargeTransactionKey, String strMessageType) throws Exception {
		log.debug("Begin of AcademyProcessPaypalPaymentAPI.getRequestIdOfTimeOutIfExists() method after direct paypal.");
		log.verbose("Paypal Direct:: strOrderHeaderKey:" + strOrderHeaderKey);
		log.verbose("Paypal Direct:: strChargeTransactionKey:" + strChargeTransactionKey);
		log.verbose("Paypal Direct:: strMessageType:" + strMessageType);
		String strReversalId = null;
		List lstPaymentTransactionError = null;

		if (YFCObject.isVoid(docOrderList)) {
			docOrderList = getOrderList(env, strOrderHeaderKey);
		}
		if (!YFCObject.isVoid(strChargeTransactionKey)) {
			lstPaymentTransactionError = XMLUtil.getElementListByXpath(docOrderList,
					"OrderList/Order/ChargeTransactionDetails/ChargeTransactionDetail[@ChargeTransactionKey='"
							+ strChargeTransactionKey + "']/PaymentTransactionErrorList/PaymentTransactionError["
							+ "@MessageType='" + strMessageType + "']");
		} else {
			lstPaymentTransactionError = XMLUtil.getElementListByXpath(docOrderList,
					"OrderList/Order/ChargeTransactionDetails/ChargeTransactionDetail/PaymentTransactionErrorList"
							+ "/PaymentTransactionError[@MessageType='" + strMessageType + "']");
		}

		if (!YFCObject.isVoid(lstPaymentTransactionError) && lstPaymentTransactionError.size() > 0) {
			for (int iPErr = 0; iPErr < lstPaymentTransactionError.size(); iPErr++) {
				Element elePaymentTransactionError = (Element) lstPaymentTransactionError.get(iPErr);
				strReversalId = elePaymentTransactionError.getAttribute(AcademyConstants.ATTR_MESSAGE);
			}
		}

		log.verbose("Paypal Direct:: strReversalId:" + strReversalId);
		log.debug("End of AcademyProcessPaypalPaymentAPI.getRequestIdOfTimeOutIfExists() method after direct paypal.");
		return strReversalId;
	}

	/**
	 * This method will create task map which will be used in translating paypal
	 * response
	 * 
	 */
	private static void populateResponsekProcessStaticMap() {

		log.debug(
				"Begin of AcademyProcessPaypalPaymentAPI.populateResponsekProcessStaticMap() method after direct paypal.");
		log.verbose("Paypal Direct:: mapTaskPropDtls :: " + mapTaskPropDtls);
		if (!mapTaskPropDtls.isEmpty()) {
			return;
		}
		log.verbose("Paypal Direct:: listDirectPaypalSuccessResName.size():" + listDirectPaypalSuccessResName.size());
		addToTaskMap(listDirectPaypalSuccessResName, AcademyConstants.PROP_PAYPAL_SUCCESS_NAME);
		log.verbose(
				"listDirectPaypalDummySettlementErrorName.size():" + listDirectPaypalDummySettlementErrorName.size());
		addToTaskMap(listDirectPaypalDummySettlementErrorName, AcademyConstants.PROP_PAYPAL_DUMMY_SETTLE_ERROR_NAME);
		log.verbose("Paypal Direct:: listDirectPaypalHardDeclineErrorName.size():" + listDirectPaypalHardDeclineErrorName.size());
		addToTaskMap(listDirectPaypalHardDeclineErrorName, AcademyConstants.PROP_PAYPAL_HARD_DECLINE_ERROR_NAME);
		log.verbose("Paypal Direct:: listDirectPaypalRateLimitRetryErrorName.size():" + listDirectPaypalRateLimitRetryErrorName.size());
		addToTaskMap(listDirectPaypalRateLimitRetryErrorName, AcademyConstants.PROP_PAYPAL_LIMIT_RETRY_ERROR_NAME);
		log.verbose("Paypal Direct:: listDirectPaypalReprocessibleErrorName.size():" + listDirectPaypalReprocessibleErrorName.size());
		addToTaskMap(listDirectPaypalReprocessibleErrorName, AcademyConstants.PROP_PAYPAL_REPROCESS_ERROR_NAME);
		log.verbose("Paypal Direct:: listDirectPaypalCodeCaughtErrorName.size():" + listDirectPaypalCodeCaughtErrorName.size());
		addToTaskMap(listDirectPaypalCodeCaughtErrorName, AcademyConstants.PROP_PAYPAL_CODECAUGHT_ERROR_NAME);
		log.verbose("Paypal Direct:: listDirectPaypalAccessTokenErrorName.size():" + listDirectPaypalAccessTokenErrorName.size());
		addToTaskMap(listDirectPaypalAccessTokenErrorName, AcademyConstants.PROP_PAYPAL_TOKEN_AUTH_ERROR_NAME);
		log.verbose("Paypal Direct:: listDirectPaypalIgnoreAllErrorName.size():" + listDirectPaypalIgnoreAllErrorName.size());
		addToTaskMap(listDirectPaypalIgnoreAllErrorName, AcademyConstants.PROP_PAYPAL_IGNORE_ALL_ERROR_NAME);
		
		log.debug("Paypal Direct:: mapTaskPropDtls :: " + mapTaskPropDtls);
		log.debug(
				"End of AcademyProcessPaypalPaymentAPI.populateResponsekProcessStaticMap() method after direct paypal.");

	}

	/**
	 * This method will create task map which will be used in translating paypal
	 * response
	 * 
	 * @param listProp
	 * @param strPropName
	 */
	private static void addToTaskMap(List<String> listProp, String strPropName) {
		log.debug("Begin of AcademyProcessPaypalPaymentAPI.addToTaskMap() method after direct paypal.");
		log.verbose("Paypal Direct:: listProp.size():" + listProp.size());
		log.verbose("Paypal Direct:: strPropName::" + strPropName);
		log.verbose("Paypal Direct:: mapTaskPropDtls :: " + mapTaskPropDtls);
		Iterator<String> itr = listProp.iterator(); // 2XX/authorized/authorize,2XX/completed/capture
		HashMap<String, HashMap> mapHttpStatusMsgTransType = YFCObject.isNull(mapTaskPropDtls.get(strPropName))
				? new HashMap()
				: mapTaskPropDtls.get(strPropName);
		while (itr.hasNext()) {
			String strValue = itr.next();
			log.verbose("Paypal Direct:: strValue:" + strValue);
			String[] arrOfStr = strValue.split("/");
			log.verbose("Paypal Direct:: arrOfStr[0]:" + arrOfStr[0]);
			log.verbose("Paypal Direct:: arrOfStr[1]:" + arrOfStr[1]);
			log.verbose("Paypal Direct:: arrOfStr[2]:" + arrOfStr[2]);
			HashMap<String, List<String>> mapMsgTransType = YFCObject.isNull(mapHttpStatusMsgTransType.get(arrOfStr[0]))
					? new HashMap()
					: mapHttpStatusMsgTransType.get(arrOfStr[0]);
			List<String> listTransType = YFCObject.isNull(mapMsgTransType.get(arrOfStr[1])) ? new ArrayList()
					: mapMsgTransType.get(arrOfStr[1]);
			listTransType.add(arrOfStr[2]);
			log.verbose("Paypal Direct:: listTransType.size():" + listTransType.size());
			mapMsgTransType.put(arrOfStr[1], listTransType);
			log.verbose("Paypal Direct:: mapMsgTransType: " + mapMsgTransType.toString());
			mapHttpStatusMsgTransType.put(arrOfStr[0], mapMsgTransType);
			log.verbose("Paypal Direct:: mapHttpStatusMsgTransType: " + mapHttpStatusMsgTransType.toString());

		}
		mapTaskPropDtls.put(strPropName, mapHttpStatusMsgTransType);
		log.verbose("Paypal Direct:: mapTaskPropDtls:" + mapTaskPropDtls);
		log.debug("End of AcademyProcessPaypalPaymentAPI.addToTaskMap() method after direct paypal.");
	}

	/**
	 * This method checks if any settlement is already done for a given
	 * Authorization ID
	 * 
	 * @param env
	 * @param docPaymentIn
	 * @return
	 * @throws Exception
	 */
	private boolean validateOpenSettledLinesOnOrder(YFSEnvironment env, Document docPaymentIn) throws Exception {

		log.debug(
				"Begin of AcademyProcessPaypalPaymentAPI.validateOpenSettledLinesOnOrder() method after direct paypal.");
		boolean bIsOpenChargeAgainstAuthID = false;
		bIsOpenChargeAgainstAuthID = validateOpenSettlementCTRs(env, docPaymentIn);
		log.verbose("Paypal Direct:: bIsOpenChargeAgainstAuthID :: " + bIsOpenChargeAgainstAuthID );
		log.debug(
				"End of AcademyProcessPaypalPaymentAPI.validateOpenSettledLinesOnOrder() method after direct paypal.");
		return bIsOpenChargeAgainstAuthID;
	}

	/**
	 * This method is used check if the AuthId has any valid Settlement against it.
	 * 
	 * @param env
	 * @param docPaymentIn
	 * @return
	 * @throws Exception
	 */
	private boolean validateOpenSettlementCTRs(YFSEnvironment env, Document docPaymentIn) throws Exception {
		log.debug("Begin of AcademyProcessPaypalPaymentAPI.validateSettlementCTRs() method  after direct paypal.");
		log.verbose("Paypal Direct:: docPaymentIn: " + XMLUtil.getXMLString(docPaymentIn) );
		boolean hasValidOpenChargeAgainstAuthID = false;

		String strAuthId = XPathUtil.getString(docPaymentIn,
				AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_AUTHORIZATION_ID);
		String strOrderHeaderKey = XPathUtil.getString(docPaymentIn,
				AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_HEADER_KEY);

		if (!YFCObject.isVoid(strAuthId) && !YFCObject.isVoid(strOrderHeaderKey)) {
			log.verbose("Paypal Direct::  Validate the CHARGE based on AuthID :: " + strAuthId);
			if (YFCObject.isVoid(docOrderList)) {
				docOrderList = getOrderList(env, strOrderHeaderKey);
			}

			NodeList nlValidChargeCTRs = XPathUtil.getNodeList(docOrderList.getDocumentElement(),
					"/OrderList/Order/ChargeTransactionDetails/ChargeTransactionDetail[@AuthorizationID='" + strAuthId
							+ "' and @ChargeType='CHARGE' and @Status='OPEN']");

			log.verbose("Paypal Direct::  nlValidChargeCTRs :: " + nlValidChargeCTRs.getLength() );

			if (nlValidChargeCTRs.getLength() > 0) {
				return true;
			}
		}
		log.verbose("Paypal Direct:: hasValidOpenChargeAgainstAuthID :: " + hasValidOpenChargeAgainstAuthID );
		log.debug("End of AcademyProcessPaypalPaymentAPI.validateSettlementCTRs() method after direct paypal.");
		return hasValidOpenChargeAgainstAuthID;
	}
	
	/**
	 * This method 1. Finds valid capture IDs for tagged refunds 2. For each capture
	 * Id make direct pay pal call 3. If call fails due to time-out scenario add a a
	 * hash table to try later with same reversal id
	 * 
	 * @param env
	 * @param docPaymentIn
	 * @param strEndpointURL
	 * @param restParameters
	 * @return
	 * @throws Exception
	 */
	private Document processTaggedRefunds(YFSEnvironment env, Document docPaymentIn, String strEndpointURL,
			Map<String, String> restParameters) throws Exception {
		Document docRespPaypal = null;
		Document docFailedResponse = null;
		log.debug(
				"Begin of AcademyProcessPaypalPaymentAPI.processTaggedRefunds() method docPaymentIn after direct paypal.");
		log.verbose("Paypal Direct:: docPaymentIn:"+ XMLUtil.getXMLString(docPaymentIn));
		Document docInput = XMLUtil.cloneDocument(docPaymentIn);
		HashMap<String, String> hmRefundedAmount = new HashMap<String, String>();
		HashMap<String, String> hmSettledAmount = new HashMap<String, String>();
		DecimalFormat df = new DecimalFormat("0.00");

		String strOrderHeaderKey = XPathUtil.getString(docPaymentIn,
				AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.STR_ORDR_HDR_KEY);
		String strPaymentKey = XPathUtil.getString(docPaymentIn,
				AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_PAYMENT_KEY);
		String strChargeTransactionKey = XPathUtil.getString(docPaymentIn,
				AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY);

		if (YFCObject.isVoid(docOrderList)) {
			docOrderList = getOrderList(env, strOrderHeaderKey);
		}
		// Find refunded amount for this order
		hmRefundedAmount = AcademyPaypalPaymentProcessingUtil.getRefundedAmount(docOrderList);
		// Find settled amount for this payment
		hmSettledAmount = AcademyPaypalPaymentProcessingUtil.getSettledAmounts(docOrderList, strPaymentKey);
		log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey
				+ " doing refund transactions API call. hmRefundedAmount: " + hmRefundedAmount.toString());
		log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey
				+ " doing refund transactions API call. hmSettledAmount: " + hmSettledAmount.toString());
		double dRefundAmount = 0.0;
		// Find the hashmap - capture id and amount for which call to be make
		HashMap<String, String> hmRefundTransactionsDetails = AcademyPaypalPaymentProcessingUtil
				.retrieveValidSettledTransactions(docPaymentIn, hmSettledAmount, hmRefundedAmount);
		log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey
				+ " doing refund transactions API. This is a map between capture Id and amountto be refunded."
				+ "hmRefundTransactionsDetails: " + hmRefundTransactionsDetails.toString());
		if (hmRefundTransactionsDetails.size() > 0) {
			Iterator<Map.Entry<String, String>> iter = hmRefundTransactionsDetails.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, String> entry = iter.next();
				String strTransactionKey = entry.getKey();
				String strAmount = entry.getValue();
				String strPaypalReqId = null;
				String strAuthorizationId = strTransactionKey; // Capture Id

				docInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT, strAmount);
				docInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, strAuthorizationId);

				// Retrieving RequestID ID to be re-used if previous timeout reversals.
				String strPaypalRequestId_strAmt_strCaptureID = getRequestIdOfTimeOutIfExists(env, strOrderHeaderKey,
						strChargeTransactionKey, "TIMEOUT");
				if (!YFCObject.isVoid(strPaypalRequestId_strAmt_strCaptureID))// eg.
																				// da643222-ad12-4eed-a443-a3f74d1f5ea8_99.99@8L751181GS752802Y
				{
					String[] strMsgSplittedArray = strPaypalRequestId_strAmt_strCaptureID
							.split(AcademyConstants.STR_UNDERSCORE);
					String strAmt_strCaptureID = strAmount + AcademyConstants.STR_AT + strAuthorizationId;
					if (strAmt_strCaptureID.equals(strMsgSplittedArray[1])) {
						strPaypalReqId = strMsgSplittedArray[0];
					}
				} else {
					strPaypalReqId = AcademyPaypalPaymentProcessingUtil.getAutoGeneratedUUID();
				}

				log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " strPaypalRequestId: "
						+ strPaypalReqId );
				restParameters.put(AcademyConstants.STR_PAYPAL_REQUEST_ID, strPaypalReqId);
				// Get the Paypal resquest in JSON format
				String strPaypalInp = AcademyPaypalPaymentProcessingUtil.preparePaypalTransInp(docInput,
						AcademyConstants.TRANSACTION_TYPE_REFUND, null);
				log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " JSON Input: " + strPaypalInp
						);
				log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " strAuthorizationId: "
						+ strAuthorizationId );
				restParameters.put(AcademyConstants.WEBSERVICE_PAYPAL_URL, strPayPalUrlRefund);
				restParameters.put(AcademyConstants.ATTR_AUTHORIZATION_ID, strAuthorizationId);
				// Call the Direct Paypal gateway service
				Document docPaypalRefundResp = AcademyPayPalRestUtil.callDirectPaypalRestService(env, docPaymentIn,restParameters,
						strPaypalInp,AcademyConstants.TRANSACTION_TYPE_REFUND);
				log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " docPaypalRefundResp: "
						+ XMLUtil.getXMLString(docPaypalRefundResp));
				if (!YFCObject.isVoid(docPaypalRefundResp)) {
					// docPaypalRefundResp.getDocumentElement().setAttribute(AcademyConstants.JSON_ATTR_TRANSACTION_TYPE,
					// AcademyConstants.TRANSACTION_TYPE_REFUND);
					docPaypalRefundResp.getDocumentElement().setAttribute(AcademyConstants.ATTR_PAYPAL_REQUEST_ID,
							strPaypalReqId);
				}
				String strHttpStatusCode = null;
				String strState = null;
				if (!YFCObject.isVoid(docPaypalRefundResp)) {
					strHttpStatusCode = docPaypalRefundResp.getDocumentElement()
							.getAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE);
					log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " strHttpStatusCode ::"
							+ strHttpStatusCode);
					strState = docPaypalRefundResp.getDocumentElement()
							.getAttribute(AcademyConstants.ATTR_PAYPAL_RESPONSE_STATE);
				}

				if (AcademyPaypalPaymentProcessingUtil.isRestResponsePropType(mapTaskPropDtls,
						AcademyConstants.PROP_PAYPAL_SUCCESS_NAME, AcademyConstants.TRANSACTION_TYPE_REFUND,
						strHttpStatusCode, strState)) {
					docRespPaypal = XMLUtil.cloneDocument(docPaypalRefundResp);
					log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " approved response from Paypal"
							+ XMLUtil.getXMLString(docRespPaypal));
					dRefundAmount = dRefundAmount + Double.parseDouble(strAmount);
					docRespPaypal.getDocumentElement().setAttribute(AcademyConstants.JSON_ATTR_AMOUNT,
							df.format(dRefundAmount));
				} else {
					if (!YFCObject.isVoid(docPaypalRefundResp)) {
						docFailedResponse = XMLUtil.cloneDocument(docPaypalRefundResp);
						log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey
								+ " webservice call in Refund either Failed/unapproved "
								+ XMLUtil.getXMLString(docFailedResponse));
					} else {
						log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " null response recieved from webservice ");
					}
					// Updated the failed Reversal IDs in the Output doc.
					hmFailedReversalId.put(strTransactionKey + AcademyConstants.STR_UNDERSCORE + strAmount,
							docPaypalRefundResp);
					iter.remove();
				}

			}
		} else {
			log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " No Valid Settlements available on Order.");
		}

		hmRefundTransactionsDetails = AcademyPaypalPaymentProcessingUtil.updateRefundedAmount(hmRefundedAmount, hmRefundTransactionsDetails);

		if (YFCObject.isVoid(docRespPaypal) && !YFCObject.isVoid(docFailedResponse)) {
			log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + " complete transaction has failed.");
			docRespPaypal = XMLUtil.cloneDocument(docFailedResponse);
		}
		// Update changeOrder API to update the webservice responses
		prepareAndInvokeChangeOrder(env, hmRefundTransactionsDetails, strOrderHeaderKey);
		log.debug("End of AcademyProcessPaypalPaymentAPI.processTaggedRefunds() method after direct paypal.");
		return docRespPaypal;
	}
	
	/**
	 * This method prepares and invokes changeOrder to update refunded details on
	 * order references
	 * 
	 * @param env
	 * @param hmRefundTransactionsDetails
	 * @param strOrderHeaderKey
	 * @throws Exception
	 */
	private void prepareAndInvokeChangeOrder(YFSEnvironment env, HashMap<String, String> hmRefundTransactionsDetails,
			String strOrderHeaderKey) throws Exception {
		log.debug(
				"Begin of AcademyProcessPaypalPaymentAPI.prepareAndInvokeChangeOrder() method after direct paypal.");

		Document docChangeOrder = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		Element eleOrder = docChangeOrder.getDocumentElement();
		eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
		eleOrder.setAttribute(AcademyConstants.ATTR_OVERRIDE, AcademyConstants.STR_YES);
		Element eleReferences = docChangeOrder.createElement("References");
		eleOrder.appendChild(eleReferences);

		if (hmRefundTransactionsDetails.size() > 0) {
			Iterator<Map.Entry<String, String>> iter = hmRefundTransactionsDetails.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, String> entry = iter.next();
				String strTransactionKey = entry.getKey();// capture Id
				String strAmount = entry.getValue();

				Element eleReference = docChangeOrder.createElement("Reference");
				eleReference.setAttribute(AcademyConstants.ATTR_NAME,
						AcademyConstants.TRANSACTION_TYPE_REFUND + AcademyConstants.STR_UNDERSCORE + strTransactionKey);
				eleReference.setAttribute(AcademyConstants.ATTR_VALUE, strAmount);
				eleReferences.appendChild(eleReference);
			}
			log.verbose("Paypal Direct:: Input to changeOrder :: " + XMLUtil.getXMLString(docChangeOrder));
			AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER, docChangeOrder);
		} else {
			log.verbose(
					" No references to be updated. So skipping the changeOrder API call.");
		}

		log.debug(
				"End of AcademyProcessPaypalPaymentAPI.prepareAndInvokeChangeOrder() method after direct paypal.");
	}
	/**
	 * This method is used to update the web service errors in payment error case of
	 * Tagged refunds for relevant responses
	 * 
	 * @param env
	 * @param docPaymentIn
	 * @param docPaymentOut
	 * @return
	 * @throws Exception
	 */
	private Document updateFailedReversalsForRefund(YFSEnvironment env, Document docPaymentIn, Document docPaymentOut)
			throws Exception {
		log.debug(
				"Begin of AcademyProcessPaypalPaymentAPI.updateFailedReversalsForRefund() method after direct paypal.");
		Element elePaymentOut = docPaymentOut.getDocumentElement();
		Element elePaymentErrorList = (Element) elePaymentOut
				.getElementsByTagName(AcademyConstants.ELE_PAYMENT_TRAN_ERROR_LIST).item(0);

		// Remove if any error handling added prior
		if (!YFCObject.isVoid(elePaymentErrorList)) {
			elePaymentOut.removeChild(elePaymentErrorList);
			
		}
		elePaymentErrorList = docPaymentOut.createElement(AcademyConstants.ELE_PAYMENT_TRAN_ERROR_LIST);
		elePaymentOut.appendChild(elePaymentErrorList);	
		Iterator<Map.Entry<String, Document>> iter = hmFailedReversalId.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Document> entry = iter.next();
			String strId_Amount = entry.getKey();
			Document docRespPaypal = entry.getValue();
			if (!YFCObject.isNull(docRespPaypal)) {
				String strPaypalReqId = docRespPaypal.getDocumentElement()
						.getAttribute(AcademyConstants.ATTR_PAYPAL_REQUEST_ID);
				log.verbose("Paypal Direct::  strPaypalReqId: " + strPaypalReqId );
				Document docFailedPaymentOut = translatePaypalResp(env, docPaymentIn, docRespPaypal,
						AcademyConstants.TRANSACTION_TYPE_REFUND, new Boolean(true)); //raise alert
				String strResponse = docFailedPaymentOut.getDocumentElement()
						.getAttribute(AcademyConstants.ATTR_RESPONSE_CODE);
				String strAuthorizationId = docFailedPaymentOut.getDocumentElement()
						.getAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID);
				if (!AcademyConstants.STR_APPROVED.equals(strResponse)
						|| AcademyConstants.STR_DUMMY_SETTLEMENT.equals(strAuthorizationId)) {
					log.verbose("Paypal Direct:: Error Update final Payment Output doc.");

					String strId = strId_Amount.split(AcademyConstants.STR_UNDERSCORE)[0];// capture id
					String strAmount = strId_Amount.split(AcademyConstants.STR_UNDERSCORE)[1];

					Element elePaymentError = elePaymentOut.getOwnerDocument()
							.createElement(AcademyConstants.ELE_PAYMENT_TRAN_ERROR);
					elePaymentErrorList.appendChild(elePaymentError);

					if (AcademyConstants.STR_SERVICE_UNAVAILABLE.equalsIgnoreCase(strResponse)) {
						//Data bound is only 20 characters
						elePaymentError.setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, "TIMEOUT");
					} else if (AcademyConstants.STR_HARD_DECLINED.equalsIgnoreCase(strResponse)) {
						//Data bound is only 20 characters
						elePaymentError.setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, "DECLINED");
					} else {
						//Data bound is only 20 characters
						elePaymentError.setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, "DECLINED");
					}

					elePaymentError.setAttribute(AcademyConstants.ATTR_MESSAGE, strPaypalReqId
							+ AcademyConstants.STR_UNDERSCORE + strAmount + AcademyConstants.STR_AT + strId);
				} else {
					log.verbose("Paypal Direct::  Showing as approved which is not a valid scenario "
							+ XMLUtil.getXMLString(docRespPaypal));
				}
			} else {
				log.verbose("Paypal Direct::  Null document output. Not a valid scenario.");
			}
		}
		log.debug(
				"End of AcademyProcessPaypalPaymentAPI.updateFailedReversalsForRefund() method after direct paypal.");
		return docPaymentOut;
	}

	/**
	 * This method cancel as and when required,
	 * 
	 * @param env
	 * @param docPaymentIn
	 * @param strChargeType
	 * @param strTransactionType
	 * @throws Exception
	 */

	private void hardDeclineOrder(YFSEnvironment env, Document docPaymentIn, String strTransactionType)
			throws Exception {
		log.debug("Begin of AcademyProcessPaypalPaymentAPI.hardDeclineOrder() method after direct paypal.");
		// Prepare taskQ record for hard declined cancellations
		String strChargeType = XPathUtil.getString(docPaymentIn,
				AcademyConstants.STR_PAYMENT_PATH + AcademyConstants.ATTR_CHARGE_TYPE_AT);
		log.verbose("Paypal Direct::  strCancelDeclinedAuth :: " + strCancelDeclinedAuth + " strCancelDeclinedCharge :: "
				+ strCancelDeclinedCharge );
		if ((!YFCObject.isVoid(strCancelDeclinedAuth) && strCancelDeclinedAuth.equals(AcademyConstants.STR_YES)
				&& strChargeType.equals(AcademyConstants.STR_CHRG_TYPE_AUTH)
				&& !strTransactionType.equalsIgnoreCase(AcademyConstants.TRANSACTION_TYPE_VOID))
				|| (!YFCObject.isVoid(strCancelDeclinedCharge)
						&& strCancelDeclinedCharge.equals(AcademyConstants.STR_YES)
						&& strChargeType.equals(AcademyConstants.STR_CHRG_TYPE_CHARGE))) {
			createManageTaskQueue(env, docPaymentIn);
		}
		log.debug("End of AcademyProcessPaypalPaymentAPI.hardDeclineOrder() method after direct paypal.");
	}
	
	/**
	 * This method parses the paypal failed scenarios response to Payment output XML
	 * when rest call returns not 2XX.
	 * @param env
	 * @param docPaymentIn
	 * @param elePaymentOut
	 * @param docRespPaypal
	 * @param strTransactionType
	 * @return
	 * @throws Exception
	 */
	private void translatePaypalFailedlResp(YFSEnvironment env, Document docPaymentIn, Element elePaymentOut,
			Document docRespPaypal, String strTransactionType, Object objParameter) throws Exception {
		log.debug(
				"Begin of AcademyProcessPaypalPaymentAPI.translatePaypalFailedlResp() method::  after direct paypal.");
		String strChargeTransactionKey = XPathUtil.getString(docPaymentIn,
				AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY);
		Element elePaypalResp = docRespPaypal.getDocumentElement();
		String strHttpStatusCode = elePaypalResp.getAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE);
		//e.g.AUTHORIZATION_AMOUNT_LIMIT_EXCEEDED, AUTH_SETTLE_NOT_ALLOWED,X
		String strErrorName = elePaypalResp.getAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_NAME);
		log.verbose("Paypal Direct:: strName:  " + strErrorName );
		String strError = elePaypalResp.getAttribute(AcademyConstants.ATTR_SMALL_ERROR);
		String strErrorDesc = elePaypalResp.getAttribute(AcademyConstants.ATTR_SMALL_ERROR_DESCRIPTION);
		log.verbose("Paypal Direct:: strError:  " + strError );
		log.verbose("Paypal Direct:: strErrorDesc:  " + strErrorDesc );
		String strOrderNo = XPathUtil.getString(docPaymentIn,AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_NO);
		//Check if yfs.academy.paypal.cautght.error.name=504/SOCKET_READ_TIMED_OUT/X,ERROR/X/X,retry after eg. x mins 
		if(AcademyPaypalPaymentProcessingUtil.isRestResponsePropType(mapTaskPropDtls,AcademyConstants.PROP_PAYPAL_CODECAUGHT_ERROR_NAME,
				strTransactionType,strHttpStatusCode,strErrorName))
		{
			log.verbose("Paypal Direct::  Code Retry/Socket-Time-Out/Read-Time-Out/Error Response code  :"+strHttpStatusCode+ ". Retry after a configurable no. of mins.");
			formReprocessibleResponse(env,docPaymentIn, elePaymentOut, docRespPaypal,strTransactionType);
			if (AcademyConstants.TRANSACTION_TYPE_PURCHASE.equalsIgnoreCase(strTransactionType) && !YFCObject.isVoid(objParameter)) {

				setPurchaseCaptureRetryAttributes(docPaymentIn, docRespPaypal, elePaymentOut, objParameter);
			}
		}
		// Check if Paypal returns webservice error
		// yfs.academy.paypal.srvunavlb.error.name=5XX/X/X,4XX/INTERNAL_SERVICE_ERROR/X, retry after eg. x mins till maxretrylimit
		else if (AcademyPaypalPaymentProcessingUtil.isRestResponsePropType(mapTaskPropDtls,
				AcademyConstants.PROP_PAYPAL_REPROCESS_ERROR_NAME, strTransactionType, strHttpStatusCode,
				strErrorName)||AcademyPaypalPaymentProcessingUtil.isRestResponsePropType(mapTaskPropDtls,
						AcademyConstants.PROP_PAYPAL_REPROCESS_ERROR_NAME, strTransactionType, strHttpStatusCode,
						strError)) {
			log.verbose("Paypal Direct:: Paypal internal error Response code  :" + strHttpStatusCode + ". Retry after some time.");
			log.info(AcademyPaypalPaymentProcessingUtil.getLoggerInfoString(AcademyConstants.STR_LOG_INFO_INTERNAL_REPROCESS_ERROR,elePaypalResp,
					strTransactionType,strHttpStatusCode,strChargeTransactionKey, strOrderNo ));
			prepareServiceUnavailableResp(env, docPaymentIn, elePaymentOut, docRespPaypal, strTransactionType);
			if (AcademyConstants.TRANSACTION_TYPE_PURCHASE.equalsIgnoreCase(strTransactionType)
					&& !YFCObject.isVoid(objParameter)) {

				setPurchaseCaptureRetryAttributes(docPaymentIn, docRespPaypal, elePaymentOut, objParameter);
			}

		} else if (listPayPalAuthRetryErrorName.contains(strErrorName) //retry after eg. 120 mins
				&& (AcademyConstants.TRANSACTION_TYPE_AUTHORIZE.equals(strTransactionType)||AcademyConstants.TRANSACTION_TYPE_PURCHASE.equals(strTransactionType))) {// AUTHORIZATION_AMOUNT_LIMIT_EXCEEDED
			log.verbose("Paypal Direct:: Retrying Autorization due to PayPal response:  " + strErrorName);
			log.verbose("Paypal Direct:: AcademyConstants.STR_NEXT_TRIGGER_IN_MIN_AUTH_ERROR:  "
					+ AcademyConstants.STR_NEXT_TRIGGER_IN_MIN_AUTH_ERROR);
			log.verbose("Paypal Direct:: value for AcademyConstants.STR_NEXT_TRIGGER_IN_MIN_AUTH_ERROR:  "
					+ props.getProperty(AcademyConstants.STR_NEXT_TRIGGER_IN_MIN_AUTH_ERROR));
			// Invoke a void call since no amount is available for AUTH.
			invokePaypalVoidTransaction(env, docPaymentIn, restParameters);
			// This error will come when PayPal has active auth and we are trying to get a
			// new auth.
			AcademyPaypalPaymentProcessingUtil.retryTransaction(elePaymentOut,
					props.getProperty(AcademyConstants.STR_NEXT_TRIGGER_IN_MIN_AUTH_ERROR), strErrorName,AcademyConstants.STR_SERVICE_UNAVAILABLE);
			elePaymentOut.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_MESSAGE, AcademyConstants.STR_AUTH_VOID);

		} 
		//Check if,yfs.academy.paypal.ratelimit.retry.error.name=400/RATE_LIMIT_REACHED/void,400/RATE_LIMIT_REACHED/refund, retry after 15 mins
		else if (AcademyPaypalPaymentProcessingUtil.isRestResponsePropType(mapTaskPropDtls,
				AcademyConstants.PROP_PAYPAL_LIMIT_RETRY_ERROR_NAME, strTransactionType, strHttpStatusCode,
				strErrorName)) {
			AcademyPaypalPaymentProcessingUtil.retryTransaction(elePaymentOut,
					props.getProperty(AcademyConstants.STR_NEXT_TRIGGER_RATE_LIMIT_ERROR), strErrorName,AcademyConstants.STR_SERVICE_UNAVAILABLE);
			if(!AcademyConstants.TRANSACTION_TYPE_REFUND.equals(strTransactionType)||
					(AcademyConstants.TRANSACTION_TYPE_REFUND.equals(strTransactionType)&& objParameter instanceof Boolean))
			{
				log.info(AcademyPaypalPaymentProcessingUtil.getLoggerInfoString(AcademyConstants.STR_LOG_INFO_RATE_LIMIT_ERROR,elePaypalResp,
						strTransactionType,strHttpStatusCode,strChargeTransactionKey, strOrderNo ));
				AcademyPaypalPaymentProcessingUtil.raiseUnHandledErrorAlert(env, docPaymentIn, docRespPaypal, strTransactionType,
						AcademyConstants.STR_RATELIMIT_ERROR,AcademyConstants.STR_ACAD_RATELIMIT_ERROR_DPP_ALERT);
			}	

		}
		//Check if yfs.academy.paypal.token.auth.error.name=4XX/AUTHENTICATION_FAILURE/X, retry after x mins
		else if (AcademyPaypalPaymentProcessingUtil.isRestResponsePropType(mapTaskPropDtls,
				AcademyConstants.PROP_PAYPAL_TOKEN_AUTH_ERROR_NAME, strTransactionType, strHttpStatusCode,
				strErrorName)|| AcademyPaypalPaymentProcessingUtil.isRestResponsePropType(mapTaskPropDtls,
						AcademyConstants.PROP_PAYPAL_TOKEN_AUTH_ERROR_NAME, strTransactionType, strHttpStatusCode,
						strError)) {
			log.verbose("Paypal Direct:: Access Token Expired Eror Response code  :" + strHttpStatusCode + ". Retry after some time.");
			log.info(AcademyPaypalPaymentProcessingUtil.getLoggerInfoString(AcademyConstants.STR_LOG_INFO_TOKEN_EXPIRED_ERROR,elePaypalResp,
					strTransactionType,strHttpStatusCode,strChargeTransactionKey, strOrderNo ));
			String strAuthResMsg= YFCObject.isVoid(elePaypalResp.getAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_MESSAGE))?
					elePaypalResp.getAttribute(AcademyConstants.ATTR_SMALL_ERROR_DESCRIPTION):elePaypalResp.getAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_MESSAGE);
			AcademyPaypalPaymentProcessingUtil.retryTransaction(elePaymentOut,
					props.getProperty(AcademyConstants.STR_NEXT_TRIGGER_IN_MIN), strAuthResMsg,AcademyConstants.STR_SERVICE_UNAVAILABLE);
			elePaymentOut.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_MESSAGE, AcademyConstants.STR_ACCESS_TOKEN_FAILED);
			if (AcademyConstants.TRANSACTION_TYPE_PURCHASE.equalsIgnoreCase(strTransactionType)
					&& !YFCObject.isVoid(objParameter)) {
				setPurchaseCaptureRetryAttributes(docPaymentIn, docRespPaypal, elePaymentOut, objParameter);
			}

		}
		// 4XX/X/X Dummy Settement or ignore all
		else if(AcademyPaypalPaymentProcessingUtil.isRestResponsePropType(mapTaskPropDtls,
				AcademyConstants.PROP_PAYPAL_DUMMY_SETTLE_ERROR_NAME, strTransactionType, strHttpStatusCode,
				strErrorName)|| AcademyPaypalPaymentProcessingUtil.isRestResponsePropType(mapTaskPropDtls,
						AcademyConstants.PROP_PAYPAL_DUMMY_SETTLE_ERROR_NAME, strTransactionType, strHttpStatusCode,
						strError)) {
			log.info(AcademyPaypalPaymentProcessingUtil.getLoggerInfoString(AcademyConstants.STR_LOG_INFO_4XX_ERROR,elePaypalResp,
					strTransactionType,strHttpStatusCode,strChargeTransactionKey, strOrderNo ));
			//ignore all
			if(AcademyPaypalPaymentProcessingUtil.isRestResponsePropType(mapTaskPropDtls,
								AcademyConstants.PROP_PAYPAL_IGNORE_ALL_ERROR_NAME, strTransactionType, strHttpStatusCode,
								strErrorName)|| AcademyPaypalPaymentProcessingUtil.isRestResponsePropType(mapTaskPropDtls,
										AcademyConstants.PROP_PAYPAL_IGNORE_ALL_ERROR_NAME, strTransactionType, strHttpStatusCode,
										strError))
			{
				//e.g. 4XX/INVALID_REQUEST/X,4XX/VALIDATION_ERROR/X /to log to yfs_export table but not to raise collection failure event	
				elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
				elePaymentOut.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_YES);
				elePaymentOut.setAttribute(AcademyConstants.ATTR_SUSPEND_PAYMENT, AcademyConstants.STR_NO);
				elePaymentOut.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_MESSAGE, AcademyConstants.STR_UNHANDLED_ERROR);
				elePaymentOut.setAttribute(AcademyConstants.ATTR_COLLECTION_DATE,AcademyConstants.STR_LONG_COLLECTION_DT);
			}	
			else {	
				AcademyPaypalPaymentProcessingUtil.prepareDummyRespForAllTransType(docPaymentIn, elePaymentOut,
						docRespPaypal, strTransactionType, null);			
			}
		} else {//unexpected error
			if(!AcademyConstants.TRANSACTION_TYPE_REFUND.equals(strTransactionType)||
					(AcademyConstants.TRANSACTION_TYPE_REFUND.equals(strTransactionType)&& objParameter instanceof Boolean))
			{	
				log.info(AcademyPaypalPaymentProcessingUtil.getLoggerInfoString(AcademyConstants.STR_LOG_INFO_UNEXPECTED_ERROR,elePaypalResp,
						strTransactionType,strHttpStatusCode,strChargeTransactionKey, strOrderNo ));
				AcademyPaypalPaymentProcessingUtil.raiseUnHandledErrorAlert(env,docPaymentIn, 
					docRespPaypal, strTransactionType,AcademyConstants.STR_UNEXPECTED_ERROR,AcademyConstants.STR_ACAD_UNHANDLED_ERROR_DPP_ALERT);
				
			}
			// To log to yfs_export table but not to raise collection failure event
			elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
			elePaymentOut.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_YES);
			elePaymentOut.setAttribute(AcademyConstants.ATTR_SUSPEND_PAYMENT, AcademyConstants.STR_NO);
			elePaymentOut.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_MESSAGE, AcademyConstants.STR_UNHANDLED_ERROR);
			elePaymentOut.setAttribute(AcademyConstants.ATTR_COLLECTION_DATE,AcademyConstants.STR_LONG_COLLECTION_DT);
		}

		log.verbose("Paypal Direct:: strChargeTransactionKey: " + strChargeTransactionKey + "docPaymentOut: "
				+ XMLUtil.getElementXMLString(elePaymentOut));
		log.debug("End of AcademyProcessPaypalPaymentAPI.translatePaypalFailedlResp() method  after direct paypal.");
		return;
	}

	/**
	 * 
	 * @param env
	 * @param docPaymentIn
	 * @param elePaymentOut
	 * @param docRespPaypal
	 * @param strTransactionType
	 * @throws Exception
	 */
	private void formReprocessibleResponse(YFSEnvironment env, Document docPaymentIn, Element elePaymentOut,
			Document docRespPaypal, String strTransactionType) throws Exception {
		log.debug(
				"Begin of AcademyProcessPaypalPaymentAPI.formReprocessibleResponse() method::  after direct paypal.");
		Element elePaypalResp = docRespPaypal.getDocumentElement();
		String strHttpRespCode = elePaypalResp.getAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE);// eg 5XX,
		String strAuthReturnMsg = YFCObject.isVoid(elePaypalResp.getAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_MESSAGE))?
				elePaypalResp.getAttribute(AcademyConstants.ATTR_SMALL_ERROR_DESCRIPTION):elePaypalResp.getAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_MESSAGE);// INTERNAL_SERVER_ERROR,SOCKET_READ_TIMED_OUT,SSL_HANDSHAKE_FAILURE
		String strPaypalReqestID = elePaypalResp.getAttribute(AcademyConstants.ATTR_PAYPAL_REQUEST_ID);
		String strTransRespMsg = elePaypalResp.getAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_MESSAGE);//
		String strAuthRespCode = (AcademyConstants.STATUS_CODE_504.equals(strHttpRespCode)
				|| AcademyConstants.STATUS_CODE_ERROR.equals(strHttpRespCode)) ? "Webservice Error"
						: "Paypal Internal Error";

		String strFinTransTrackID = elePaypalResp.getAttribute(AcademyConstants.ATTR_PAYPAL_DEBUG_ID);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_YES);

		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, strAuthRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, strAuthReturnMsg);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_COLLECTION_DATE, AcademyPaypalPaymentProcessingUtil
				.nextCollectionDate(props.getProperty(AcademyConstants.STR_NEXT_TRIGGER_IN_MIN)));
		// Contains Http Response Code and Message
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE, strHttpRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_MESSAGE, strTransRespMsg);//e.g.ERROR/504/5XX
		elePaymentOut.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_CODE, strFinTransTrackID);// Paypal-Debug-ID
		elePaymentOut.setAttribute(AcademyConstants.ATTR_REQUEST_ID, strPaypalReqestID);// Paypal-Request-ID

		// Updating error message in the Payment Error List
		Element elePaymentErrorList = elePaymentOut.getOwnerDocument()
				.createElement(AcademyConstants.ELE_PAYMENT_TRAN_ERROR_LIST);
		elePaymentOut.appendChild(elePaymentErrorList);

		Element elePaymentError = elePaymentOut.getOwnerDocument()
				.createElement(AcademyConstants.ELE_PAYMENT_TRAN_ERROR);
		elePaymentErrorList.appendChild(elePaymentError);
		// For service unavailable scenario this would be Paypal-Request-ID
		elePaymentError.setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
		elePaymentError.setAttribute(AcademyConstants.ATTR_MESSAGE, strPaypalReqestID);
		log.debug(
				"End of AcademyProcessPaypalPaymentAPI.formReprocessibleResponse() method::  after direct paypal.");
	}

	/**
	 * This method is used to get the PaypalRequestID of the last timed out error
	 * for Purchase
	 * 
	 * @param env
	 * @param strOrderHeaderKey
	 * @param strChargeTransactionKey
	 * @param strMessageType
	 * @return
	 * @throws Exception
	 */
	private String getPaypalReqIdForPurchase(YFSEnvironment env, String strOrderHeaderKey,
			String strChargeTransactionKey, String strMessageType, String strInternalTransactionType) throws Exception {
		log.debug("Begin of AcademyProcessPaypalPaymentAPI.getRequestIdOfTimeOutIfExists() method");
		String strPaypalReqID = null;
		String strMessage = getRequestIdOfTimeOutIfExists(env, strOrderHeaderKey, strChargeTransactionKey,
				AcademyConstants.STR_SERVICE_UNAVAILABLE);
		if (!YFCObject.isVoid(strMessage)) {
			String[] msgArray = strMessage.split(AcademyConstants.STR_AT);
			if (strInternalTransactionType.equals(msgArray[1])) {
				strPaypalReqID = msgArray[0];
				log.verbose("Paypal Direct:: strPaypalReqID:: " + strPaypalReqID);
				return strPaypalReqID;
			}
		}
		log.debug("End of AcademyProcessPaypalPaymentAPI.getRequestIdOfTimeOutIfExists() method");
		return strPaypalReqID;
	}
	
}
