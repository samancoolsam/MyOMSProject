package com.academy.ecommerce.sterling.util.webservice;

import java.io.InputStream;
import java.io.StringReader;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.json.utils.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.academy.ecommerce.sterling.util.AcademyPayPalRestUtil;
import com.academy.ecommerce.sterling.util.AcademyPayeezyRestUtil;
import com.academy.ecommerce.sterling.util.AcademyPaymentProcessingUtil;
import com.academy.ecommerce.sterling.util.stub.AcademyPaymentStub;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * This service makes REST call to Payeezy payment gateway system for payment type of CREDIT_CARD
 * @author C0014737
 * @created on 2018-08-20
 */
public class AcademyInvokePayeezyRestWebservice {

	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyInvokePayeezyRestWebservice.class);

	/**
	 * It holds the properties configured in AcademyInvokePayeezyRestService service
	 * 
	 */
	private Properties props;

	/**It sets the properties
	 * @param props
	 */
	public void setProperties(Properties props) {
		this.props = props;
	}
	YFSEnvironment env = null;

	//This holds the Bank gateway successful response codes
	private static List<String> listPayZBankSuccessResponseCode = null;

	//This holds the Bank gateway Retry response codes(resent the requests)
	private static List<String> listPayZBankRetryResponseCode = null;

	//This holds the EXact_Resp_Code of PayZ under HARD_DECLINED category
	private static List<String> listPayZGatewaySuccessResponseCode = null;
	//This holds the Bank gateway Retry response codes(resent the requests)
	private static List<String> listPayPalRetryErrorName = null;	

	//Below are variables which holds the PayZ values from customer_overrides.properties file 
	//private static String strEndpointURL = null;
	private static String strApiKey = null;
	private static String strToken = null;
	private static String strApiSecret = null;
	private static String strCancelDeclinedAuth = null;
	private static String strCancelDeclinedCharge = null;
	private static String strHMACCode = null;
	private static String strProxyHost = null;
	private static String strProxyPort = null;
	private static String strPayZCCAuthExpiryDays = null;
	private static String strPayZPPAuthExpiryDays = null;
	private static int iMaxRetryCount;
	private static long nonce;
	private static String strPayPalUser = null;
	private static String strPayPalPass = null;
	private static String strPayPalUrlToken = null;
	private static String strPayPalUrlAuthorization = null;
	private static String strPayPalUrlVoid = null;
	private static String strConnectionTimeout = null;
	private static String strResponseTimeout = null;
	//OMNI-69067 Enable/Disable ApplePay Reauth - Start
	private static String strApplePayReauthFlag = null;
	//OMNI-69067 Enable/Disable ApplePay Reauth - End

	//OMNI-79750 Enable/Disable GooglePay Reauth - Start
	private static String strGooglePayReauthFlag = null;
	//OMNI-79750 Enable/Disable GooglePay Reauth - End
	
	//This holds the EXact_Resp_Code of PayZ which needs retry
	//private static List<String> listPayZRetryRespCode = null;
	private static String strInvokePaymentStub = null;//OMNI-99459

	/* This static block initializes the variables from customer_overrides.properties file
	 * 
	 */
	static {
		log.verbose("Begin of AcademyInvokePayeezyRestWebservice static block ...");

		listPayZBankSuccessResponseCode = Arrays.asList(YFSSystem.getProperty(AcademyConstants.PROP_PAYZ_BANK_SUCCESS_RESP_CODES)
				.split(AcademyConstants.STR_COMMA));
		listPayZBankRetryResponseCode = Arrays.asList(YFSSystem.getProperty(AcademyConstants.PROP_PAYZ_BANK_RETRY_RESP_CODES)
				.split(AcademyConstants.STR_COMMA));
		listPayZGatewaySuccessResponseCode = Arrays.asList(YFSSystem.getProperty(AcademyConstants.PROP_PAYZ_GATEWAY_SUCCESS_RESP_CODES)
				.split(AcademyConstants.STR_COMMA));
		listPayPalRetryErrorName = Arrays.asList(YFSSystem.getProperty(AcademyConstants.PROP_PAYPAL_RETRY_ERROR_NAME)
				.split(AcademyConstants.STR_COMMA));

		strApiKey = YFSSystem.getProperty(AcademyConstants.PROP_PAYZ_APIKEY);
		strToken = YFSSystem.getProperty(AcademyConstants.PROP_PAYZ_TOKEN);
		strApiSecret = YFSSystem.getProperty(AcademyConstants.PROP_PAYZ_APISECRET);
		strHMACCode = YFSSystem.getProperty(AcademyConstants.PROP_PAYZ_HMAC_CODE);
		strProxyHost = YFSSystem.getProperty(AcademyConstants.PROP_PAYZ_PROXY_HOST);
		strProxyPort = YFSSystem.getProperty(AcademyConstants.PROP_PAYZ_PROXY_PORT);
		strPayZCCAuthExpiryDays = YFSSystem.getProperty(AcademyConstants.PROP_PAYZ_CC_AUTH_EXPIRY_DAYS);
		strPayZPPAuthExpiryDays = YFSSystem.getProperty(AcademyConstants.PROP_PAYZ_PP_AUTH_EXPIRY_DAYS);

		strPayPalUser = YFSSystem.getProperty(AcademyConstants.PROP_PAYPAL_USER_ID);
		strPayPalPass = YFSSystem.getProperty(AcademyConstants.PROP_PAYPAL_PASSWORD);
		strPayPalUrlToken = YFSSystem.getProperty(AcademyConstants.PROP_PAYPAL_URL_TOKEN);
		strPayPalUrlAuthorization = YFSSystem.getProperty(AcademyConstants.PROP_PAYPAL_URL_AUTHORIZATION);
		strPayPalUrlVoid = YFSSystem.getProperty(AcademyConstants.PROP_PAYPAL_URL_VOID);
		strConnectionTimeout = YFSSystem.getProperty(AcademyConstants.PROP_CONNECTION_TIMEOUT);
		strResponseTimeout = YFSSystem.getProperty(AcademyConstants.PROP_RESPONSE_TIMEOUT);
		
		//OMNI-69067 Enable/Disable ApplePay Reauth - Start
		strApplePayReauthFlag = YFSSystem.getProperty(AcademyConstants.APPLE_PAY_REAUTH_FLAG);
		//OMNI-69067 Enable/Disable ApplePay Reauth - End
	
		//OMNI-79750 Enable/Disable GooglePay Reauth - Start
		strGooglePayReauthFlag = YFSSystem.getProperty(AcademyConstants.GOOGLE_PAY_REAUTH_FLAG);
		//OMNI-79750 Enable/Disable GooglePay Reauth - End


		/*
		listPayZBankSuccessResponseCode = Arrays.asList("100,101,102,103,104,105,106,107,108,109,110,111,164".split(AcademyConstants.STR_COMMA));
		listPayZBankRetryResponseCode = Arrays.asList("000,235,236,301,351,902,904".split(AcademyConstants.STR_COMMA));
		listPayZGatewaySuccessResponseCode = Arrays.asList("00".split(AcademyConstants.STR_COMMA));
		listPayPalRetryErrorName = Arrays.asList("AUTHORIZATION_AMOUNT_LIMIT_EXCEEDED".split(AcademyConstants.STR_COMMA));
		strApiKey = "yvmDvaGdgdFLoSrpAW19gIAx6ai2AzTT";
		strToken = "fdoa-3d01df7316dfa72aa2cdb3976d9291793d01df7316dfa72a";
		strApiSecret = "af781b52d8d03228c125929a58aa431c0fa001b21d833bc58aa3048db19d7bdb";
		strHMACCode = "HmacSHA256";
		strProxyHost = "52.22.64.70";
		strProxyPort = "8443";
		strPayZCCAuthExpiryDays = "7";
		strPayZPPAuthExpiryDays = "7";

		strPayPalUser = "AZIoy7wkXiDML9-c9Vz712AghG_XejVMyT0HHrUgxfGIt5Ch-V7i2SuUPiR4Xry-ds9kfsKZPdxDnyFL";
		strPayPalPass = "EPHFGXDN1_GrYqczB5AvaEL5YkZkBSZg85XLu0oSkTUN77dFUeXray2Q1ptM3vfYplVHynKmEW3IashF";
		strPayPalUrlToken = "https://api.sandbox.paypal.com/v1/oauth2/token";
		strPayPalUrlAuthorization = "https://api.sandbox.paypal.com/v1/payments/orders/{OrderId}/authorize";
		strPayPalUrlVoid = "https://api.sandbox.paypal.com/v1/payments/authorization/{AuthorizationId}/void";
		strConnectionTimeout = "30000";
		strResponseTimeout = "30000";
		 */
		//Start OMNI-99459 - Change for payment stub
		strInvokePaymentStub = YFSSystem.getProperty(AcademyConstants.INVOKE_PAYMENT_STUB); //OMNI-99459
		//End OMNI-99459
		log.verbose("AcademyInvokePayeezyRestWebservice static variable initialization successfull ...");

		log.verbose("End of AcademyInvokePayeezyRestWebservice static block ...");
	}

	//This flag used as SocketTimeoutException indicator
	private boolean bRetry = false;
	boolean bIsPaypalReAuthCall = false;//KER-[paypal JIRA]:
	boolean bSendPayPalAuthToPayZSuccess = false;
	boolean bIsPaypalAuthSuccess = false;
	boolean bIsPaypalSettlementRetry = false;
	boolean bPayZAuthRetry = false;
	boolean bIsPaypalOrder = false;
	Document docOrderList = null;
	HashMap<String, Document> hmFailedReversalId = new HashMap<String, Document>();;
	Map<String, String> restParameters = new HashMap<String, String>();
	String strEndpointURL = null;

	/**
	 * This method creates and sends the payment request to PayZ gateway.
	 * @param docPaymentIn - It's a Payment XML 
	 * @return docPaymentOut - Payment XML as response document
	 * @throws Exception
	 */
	public Document invokePAYZHTTPCCService(YFSEnvironment envt, Document docPaymentIn) throws Exception {

		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.invokePAYZHTTPCCService() method");
		log.verbose("AcademyInvokePayeezyRestWebservice.invokePAYZHTTPCCService() Payment Input XML ::"+XMLUtil.getXMLString(docPaymentIn));

		String strPayZInp = null;
		Document docRespPayZ = null;
		Document docPaymentOut = null;
		env =envt;

		nonce =  Math.abs(SecureRandom.getInstance(YFSSystem.getProperty(AcademyConstants.PROP_PAYZ_NONCE)).nextLong());
		strEndpointURL = YFSSystem.getProperty(AcademyConstants.PROP_PAYZ_ENDPOINT_URL);

		/*nonce = Math.abs(SecureRandom.getInstance("SHA1PRNG").nextLong());
		strEndpointURL = props.getProperty("PAYZ_ENDPOINT_URL");
		if(YFSObject.isVoid(strEndpointURL)){
			strEndpointURL = "https://api-cert.payeezy.com/v1/transactions";			
		}
		 */
		restParameters.put(AcademyConstants.WEBSERVICE_APIKEY, strApiKey);
		restParameters.put(AcademyConstants.WEBSERVICE_TOKEN, strToken);
		restParameters.put(AcademyConstants.WEBSERVICE_NONCE, Long.toString(nonce));
		restParameters.put(AcademyConstants.WEBSERVICE_API_SECRET, strApiSecret);
		restParameters.put(AcademyConstants.WEBSERVICE_PROXY_HOST, strProxyHost);
		restParameters.put(AcademyConstants.WEBSERVICE_PROXY_PORT, strProxyPort);
		restParameters.put(AcademyConstants.WEBSERVICE_HMAC, strHMACCode);

		restParameters.put(AcademyConstants.WEBSERVICE_PAYPAL_URL_AUTH, strPayPalUrlAuthorization);
		restParameters.put(AcademyConstants.WEBSERVICE_PAYPAL_URL_TOKEN, strPayPalUrlToken);
		restParameters.put(AcademyConstants.WEBSERVICE_PAYPAL_USER_ID, strPayPalUser);
		restParameters.put(AcademyConstants.WEBSERVICE_PAYPAL_PASSWORD, strPayPalPass);
		restParameters.put(AcademyConstants.ATTR_CONNECTION_TIMEOUT, strConnectionTimeout);
		restParameters.put(AcademyConstants.ATTR_RESPONSE_TIMEOUT, strResponseTimeout);

		//This is for time out validation
		if(!YFSObject.isVoid(props.getProperty(AcademyConstants.ATTR_CONNECTION_TIMEOUT))){
			restParameters.put(AcademyConstants.ATTR_CONNECTION_TIMEOUT, props.getProperty(AcademyConstants.ATTR_CONNECTION_TIMEOUT));			
		}
		if(!YFSObject.isVoid(props.getProperty(AcademyConstants.ATTR_RESPONSE_TIMEOUT))){
			restParameters.put(AcademyConstants.ATTR_RESPONSE_TIMEOUT, props.getProperty(AcademyConstants.ATTR_RESPONSE_TIMEOUT));
		}
		if(!YFSObject.isVoid(props.getProperty(AcademyConstants.STR_PAYEEZY_ENDPOINT_URL))){
			strEndpointURL = props.getProperty(AcademyConstants.STR_PAYEEZY_ENDPOINT_URL);			
		}
		//Start : OMNI-69010 
		//Fetches a Service Argument named PaypalProxyEnable and it decides if proxy to be enabled/disabled for
		//1. Getting Access Token rest call for both payzee-paypal & paypal decouple 
		//2. All rest calls for paypal decouple
		log.verbose("Is PROXY enabled:: "+props.getProperty(AcademyConstants.ATTR_PAYPAL_PROXY_ENABLE));
		if(!YFSObject.isVoid(props.getProperty(AcademyConstants.ATTR_PAYPAL_PROXY_ENABLE))){
			restParameters.put(AcademyConstants.ATTR_PAYPAL_PROXY_ENABLE, props.getProperty(AcademyConstants.ATTR_PAYPAL_PROXY_ENABLE));			
		}
		//End : OMNI-69010 

		String strIsDBLoggingEnableForAuth = props.getProperty(AcademyConstants.STR_DB_LOGGING_FOR_AUTH);
		String strIsDBLoggingEnableForCharge = props.getProperty(AcademyConstants.STR_DB_LOGGING_FOR_CHARGE);

		strCancelDeclinedAuth = props.getProperty(AcademyConstants.STR_CANCEL_DECLINED_AUTH);
		strCancelDeclinedCharge = props.getProperty(AcademyConstants.STR_CANCEL_DECLINED_CHARGE);

		if(!YFCObject.isVoid(props.getProperty(AcademyConstants.STR_MAX_RETRY_LIMIT)))
			iMaxRetryCount = Integer.parseInt(props.getProperty(AcademyConstants.STR_MAX_RETRY_LIMIT));
		
		//Start : OMNI-1435 : Changes to Enable/Disable Proxy
		log.verbose("Is PROXY enabled:: "+System.getProperty(AcademyConstants.PROP_ENABLE_PROXY));
				
		if(!YFCObject.isVoid(System.getProperty(AcademyConstants.PROP_ENABLE_PROXY))) {
			restParameters.put(AcademyConstants.PROP_ENABLE_PROXY, 
					System.getProperty(AcademyConstants.PROP_ENABLE_PROXY));
		}
		/* Commenting the code where the value is being fetched from customrt_overrides.properties
		if(!YFCObject.isVoid(YFSSystem.getProperty(AcademyConstants.PROP_ENABLE_PROXY))) {
			restParameters.put(AcademyConstants.PROP_ENABLE_PROXY, 
					YFSSystem.getProperty(AcademyConstants.PROP_ENABLE_PROXY));
		}
		*/
		//End : OMNI-1435 : Changes to Enable/Disable proxy

		boolean bIgnoreAuth = false;
		boolean bIsEWalletOrders = false;
		boolean bIsCompleteAuthVoided = false;

		String strTransactionType= null;
		String strSplitShipment= null;
		Date dateCurrAuthExpiryDate = null;

		//KER-[paypal JIRA]:
		boolean bVoidOnAuthExpiry = false;
		Document docRespPaypal = null;
		String strPayPalAuthID = null;
		//boolean bIsPaypalOrder = false;
		//boolean bIsPaypalReAuthCall = false;
		//boolean bIsPaypalAuthSuccess = false;
		//boolean bPayZAuthRetry = false;
		//boolean bSendPayPalAuthToPayZSuccess = false;
		//KER-[paypal JIRA]:

		String strCurrAuthExpiry = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CURR_AUTH_EXP_DATE);
		String strReqAmt = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);
		String strAuthId = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_AUTHORIZATION_ID);
		String strPaymentType = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_PAYMENT_TYPE);
		String chargeType =  XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH + AcademyConstants.ATTR_CHARGE_TYPE_AT);
		String strOrderHeaderKey =  XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_HEADER_KEY);
		String strChargeTransactionKey =  XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY);

		String strTransactionId = XPathUtil.getString(docPaymentIn, "/Payment/CreditCardTransactions/CreditCardTransaction" +
		"[@TranType='AUTHORIZATION' and @TranAmount > 0]/@RequestId");

		if(AcademyConstants.STR_PAYMENT_TYPE_PAYPAL_NEW.equals(strPaymentType)){
			bIsPaypalOrder = true;
		}

		if(!AcademyConstants.STR_PAYMENT_TYPE_CREDIT_CARD_NEW.equals(strPaymentType) && 
				!AcademyConstants.STR_PAYMENT_TYPE_PAYPAL_NEW.equals(strPaymentType)) {
			
			bIsEWalletOrders = true;
			
			//Begin : OMNI-32161, 32162, 32163, 32164, 32165, 32166, 32167, 32169
			/*
			 * A temporary attribute set only for e-wallet orders to send stored_credentials
			 * specific json attributes in the request
			 */
			restParameters.put(AcademyConstants.STR_IS_EWALLET_ORDER, AcademyConstants.STR_YES);
			//End : OMNI-32161, 32162, 32163, 32164, 32165, 32166, 32167, 32169
			
		}

		//In case of sceanrios where AUTHORIZATION is not presnet in input take from InternalReturnMessage
		log.verbose(" :: strTransactionId :: "+strTransactionId) ;
		if(YFCObject.isVoid(strTransactionId)){
			strTransactionId = XPathUtil.getString(docPaymentIn, "/Payment/CreditCardTransactions/CreditCardTransaction/@InternalReturnMessage");
		}

		//For cancel : true, For re-auth on auth expiry its false
		if(!YFCObject.isVoid(strCurrAuthExpiry)){
			dateCurrAuthExpiryDate = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN).parse(strCurrAuthExpiry);
		}

		/*1)For auth expiry product will make one entry for auth with -ve amount and one for new auth with +ve amount.
		 * As the auth is already expired so we cannot void it and this auth with -ve amount will be processed as dummy i.e APPROVED response.
		 * 
		 * 2)For partial/full cancllation the Payment input xml has CurrentAuthorizationExpirationDate attribute and the date is in future.
		 * If the Authorization ID has any existing settlement against it, Payeezy cannot vaod this partial amount.
		 * A dummy APPROVED response is updated in sterling. 
		 * 
		 * If the  Authorization ID has no settlements against it, then auth with -ve amount for cancellation would need VOID call. 
		 * Here bIgnoreAuth value will remain false. 
		 */

		//Condition is true for cancellation and Auth expiry
		if(AcademyConstants.STR_CHRG_TYPE_AUTH.equalsIgnoreCase(chargeType) && 
				strReqAmt.startsWith(AcademyConstants.STR_HYPHEN)) {
			strTransactionType = AcademyConstants.TRANSACTION_TYPE_VOID;
			strEndpointURL = strEndpointURL + "/" + strTransactionId;

			log.verbose(" the charge type is -AUTH. Date are "+dateCurrAuthExpiryDate+" and "+Calendar.getInstance().getTime()) ;

			//Ignoring the void call if authorization has expired
			if(YFCObject.isVoid(dateCurrAuthExpiryDate) || dateCurrAuthExpiryDate.before(Calendar.getInstance().getTime())) {
				//if(!bIsPaypalOrder ) { 
				bIgnoreAuth = true;
				bIsCompleteAuthVoided = true;
				//}
				if(bIsPaypalOrder){
					//RequestId is not available as part of input xml
					docPaymentIn = getLatestExistingAuth(env,docPaymentIn);

					//strAuthId = docPaymentIn.getDocumentElement().getAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID);
					strTransactionId = docPaymentIn.getDocumentElement().getAttribute(AcademyConstants.ATTR_REQUEST_ID_SMALL);
					strEndpointURL = strEndpointURL + strTransactionId;

					//No Paypal Void will be done on Complete Auth Expiry
					//bVoidOnAuthExpiry = true;
					//bIgnoreAuth = false
					bIgnoreAuth = true;
					bIsCompleteAuthVoided = false;					
				}
			}
			//Ignoring any partial void if payment is E-wallets
			else if(bIsEWalletOrders || bIsPaypalOrder) {
				//Check if the complete Order is being voided with any settled lines
				bIgnoreAuth = validateSettledLinesOnOrder(env, docPaymentIn);

				//check if Order has a full void or partial
				if(!bIgnoreAuth) {
					bIgnoreAuth = validateCompleteOrderCancel(env, docPaymentIn);
					//Complete Void call being invoked. complete Order cancelled
					if(!bIgnoreAuth){
						bIsCompleteAuthVoided = true;
					}
				}
			}
			else {
				//Check if Order has any settled transactions 
				bIgnoreAuth = validateSettledLinesOnOrder(env, docPaymentIn);
				//Complete Void call being invoked. complete Order cancelled
				if(!bIgnoreAuth){
					bIsCompleteAuthVoided = true;
				}
			}
		}
		else if(AcademyConstants.STR_CHRG_TYPE_CHARGE.equalsIgnoreCase(chargeType)) {

			if(strReqAmt.startsWith(AcademyConstants.STR_HYPHEN)) {
				//Refund transaction being triggered.
				log.verbose(" the charge type is -CHARGE i.e refund. Amount :: "+strReqAmt);
				strTransactionType = AcademyConstants.TRANSACTION_TYPE_REFUND;
			}
			else {
				//Order being Settled.
				log.verbose(" the charge type is CHARGE i.e settlement. Amount :: "+strReqAmt);
				//KER-[paypal JIRA]:- add || PayPal in this condition
				if ((!YFCObject.isVoid(strAuthId) && !YFCObject.isVoid(strTransactionId)) || bIsPaypalOrder ){
					//Settlement being done on existing AUTH
					//Paypal only support TransactionType=capture. If we dont have valid auth for paypal capture call will fail. 
					log.verbose(" Settlement being done on existing AUTH :: "+strAuthId);
					strTransactionType = AcademyConstants.TRANSACTION_TYPE_CAPTURE;
					strEndpointURL = strEndpointURL + "/" + strTransactionId;

					//KER-[paypal JIRA]: SplitShipment not allowed for Paypal order
					if(!bIsPaypalOrder){
						strSplitShipment = retrieveSplitShipmentCount(env, docPaymentIn, strAuthId);

						//Update transaction type as split shipments based on number of shipments
						if(!YFCObject.isVoid(strSplitShipment)) {
							strTransactionType = AcademyConstants.TRANSACTION_TYPE_SPLIT;
						}
					}else if(bIsPaypalOrder && YFCObject.isVoid(strAuthId)){

						//This is to make sure closing the exising paypal open auth.
						//Start : KER-15779 : Prod Issue PayPal (Payment): Multiple ReAuth and Voids calls triggered from OMS						
						/*String strPaymentReference7 = docPaymentIn.getDocumentElement().
						getAttribute(AcademyConstants.ATTR_PAYMENT_REF_7);
						 */

						//Retrieving the PaypalAuth Details from chargeTransactionErrorList.
						String strPayPalAuth = retrievePaypalAuthDetails(env, docPaymentIn);

						if(!YFCObject.isVoid(strPayPalAuth) 
								&& strPayPalAuth.startsWith(AcademyConstants.STR_PAYZ_AUTH_FAIL) 
								&& !strPayPalAuth.contains(AcademyConstants.STR_SUSPEND_AUTH)){
							bRetry = true;
							bIsPaypalSettlementRetry = true;
							log.verbose("bIsPaypalSettlementRetry: " +bIsPaypalSettlementRetry);
							log.verbose("bRetry: " +bRetry);
						}else{
							//docPaymentIn = getLatestExistingAuth(env,docPaymentIn);

							//strAuthId = docPaymentIn.getDocumentElement().getAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID);
							//strTransactionId = docPaymentIn.getDocumentElement().getAttribute(AcademyConstants.ATTR_REQUEST_ID_SMALL);

							//strEndpointURL = strEndpointURL + strTransactionId;
							//Since no valid AUTH iis present. Invoke paypal AUTH call and then purchase PayZ
							bIsPaypalReAuthCall = true;
							strTransactionType = AcademyConstants.TRANSACTION_TYPE_PURCHASE;
						}
						//End : KER-15779 : Prod Issue PayPal (Payment): Multiple ReAuth and Voids calls triggered from OMS
					}
					//KER-[paypal JIRA]: SplitShipment not allowed for Paypal order
				}
				//Begin : OMNI-32161, 32162, 32163, 32164, 32165, 32166, 32167, 32169
				//else if(!bIsEWalletOrders){ <-- commented as part of OMNI-32161, 32162, 32163, 32164, 32165, 32166, 32167, 32169
				
				/*
				 * Code changes to enable purchase call for e-wallet orders along with existing payment types
				 */
				
				else {
					//Order does not have a valid AUTH and invoking a purchase call.
					log.verbose(" Settlement does not have valid auth");
					//OMNI-79751, OMNI-79752, OMNI-79747, OMNI-79749 Enable/Disable ApplePay/ GPay Purchase calls - Start
					log.verbose("Payment Type : " + strPaymentType);
					log.verbose("Is ApplePay Purchase call enabled ?: " + strApplePayReauthFlag + "\n"
							+ "Is GooglePay Purchase call enabled ?: " + strGooglePayReauthFlag);
					if ((AcademyConstants.STR_APPLE_PAY_PAYMENT.equals(strPaymentType) && !AcademyConstants.STR_YES.equals(strApplePayReauthFlag)) ||
					(AcademyConstants.STR_GOOGLE_PAY_PAYMENT.equals(strPaymentType) && !AcademyConstants.STR_YES.equals(strGooglePayReauthFlag))) {
						strTransactionType = "";
					} else {
						strSplitShipment = null;
						strTransactionType = AcademyConstants.TRANSACTION_TYPE_PURCHASE;
					}
					log.verbose(":: strTransactionType :: " +strTransactionType );
					//OMNI-79751, OMNI-79752, OMNI-79747, OMNI-79749 Enable/Disable ApplePay/ GPay Purchase calls - End
				}
				/* OMNI-32161, 32162, 32163, 32164, 32165, 32166, 32167, 32169
				else {
					//Order is an eWallet Order and it doesnt have valid auth. Dummy Settlment and raise alert.
					log.verbose(" EWallet Order does not have a valid Auth");
					strSplitShipment = null;
					strTransactionType = null;
				}
				*/
				//End : OMNI-32161, 32162, 32163, 32164, 32165, 32166, 32167, 32169
			}	
		}
		else if(AcademyConstants.STR_CHRG_TYPE_AUTH.equalsIgnoreCase(chargeType)) {
			//Auth Call is not valid for eWallets. Handling exception scenario
			if(strPaymentType.equals("Credit_Card") || bIsPaypalOrder ) {
				strTransactionType = AcademyConstants.TRANSACTION_TYPE_AUTHORIZE;
				//KER-[paypal JIRA]
				if(bIsPaypalOrder){	
					bIsPaypalReAuthCall = true;	
					log.verbose("bIsPaypalReAuthCall: " +bIsPaypalReAuthCall );
				}
			}
			//Begin : OMNI-32161, 32162, 32163, 32164, 32165, 32166, 32167, 321699
			
			/*
			 * Enable re-auth for e-wallet orders
			 */
			
			log.verbose("Value of bIsEWalletOrders: " +bIsEWalletOrders );
			
			if(bIsEWalletOrders) {
				//OMNI-69067,79750 Enable/Disable ApplePay/GPay Reauth - Start
				log.verbose("Payment Type : " + strPaymentType);
				log.verbose("Re-auth enabled: ApplePay ::" + strApplePayReauthFlag + " GooglePay ::" +strGooglePayReauthFlag );
				if ((AcademyConstants.STR_APPLE_PAY_PAYMENT.equals(strPaymentType) && !AcademyConstants.STR_YES.equals(strApplePayReauthFlag))||
				(AcademyConstants.STR_GOOGLE_PAY_PAYMENT.equals(strPaymentType) && !AcademyConstants.STR_YES.equals(strGooglePayReauthFlag))) {
					strTransactionType = "";
					log.verbose(strPaymentType + " e-wallet reauthorization disabled");
				} else {
					strTransactionType = AcademyConstants.TRANSACTION_TYPE_AUTHORIZE;
				}
				//OMNI-69067,79750 Enable/Disable ApplePay/GPay Reauth - End
				log.verbose(":: strTransactionType :: " +strTransactionType );
			}
			//End : OMNI-32161, 32162, 32163, 32164, 32165, 32166, 32167, 32169
		}

		if(bIgnoreAuth){
			log.verbose("DUMMY_AUTH for CTK::"+XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT 
					+ AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY));
		}
		else if(!YFCObject.isVoid(strTransactionType) && strTransactionType.equalsIgnoreCase(AcademyConstants.TRANSACTION_TYPE_VOID)){
			log.verbose(" doing void transactions API call." + strTransactionType);

			if(!(bIsPaypalOrder && bVoidOnAuthExpiry)){		
				//Do a complete void on the AuthorizationID.
				String strAuthTotal = XPathUtil.getString(docPaymentIn, "/Payment/CreditCardTransactions/CreditCardTransaction/@AuthAmount");			

				DecimalFormat df = new DecimalFormat("0.00"); 
				strAuthTotal = df.format(Double.parseDouble(strAuthTotal));
				//Doing a complete Void of the Order Total 
				if(!strAuthTotal.startsWith(AcademyConstants.STR_HYPHEN) )
					docPaymentIn.getDocumentElement().setAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT, 
							AcademyConstants.STR_HYPHEN +strAuthTotal);
				
				//Start : KER-15764: Void call not sent after cancelling an entire order through multiple cancellations.
				if(bIsEWalletOrders) {
					//Retrieve original Auth
					bIsCompleteAuthVoided = true;
					String strOrigAuthAmount = retrieveOriginalAuthAmount(docPaymentIn);
					docPaymentIn.getDocumentElement().setAttribute(AcademyConstants.JSON_ATTR_AMOUNT, 
							docPaymentIn.getDocumentElement().getAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT));
					docPaymentIn.getDocumentElement().setAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT, 
							AcademyConstants.STR_HYPHEN +strOrigAuthAmount);
				}
				//End : KER-15764: Void call not sent after cancelling an entire order through multiple cancellations.
			}			

			//Reversal ID is not to be used in Void Transaction. timeout reversal is not eligible for void.
			restParameters.put(AcademyConstants.WEBSERVICE_REVERSAL_ID, null);

			strPayZInp = AcademyPayeezyRestUtil.createPAYZRestInputForCCAuth(docPaymentIn, strTransactionType, strSplitShipment, restParameters);
			log.verbose("AcademyInvokePayeezyRestWebservice.invokePAYZHTTPCCService() JSON Input" + strPayZInp);

			//Call the Payeezy gateway service
			//OMNI-99459 - START
			log.verbose("Checking strInvokePaymentStub value");
			
			if(!YFCObject.isVoid(strInvokePaymentStub) && AcademyConstants.STR_YES.equalsIgnoreCase(strInvokePaymentStub)){
				//Invoke payment stub
				log.verbose("strInvokePaymentStub: "+strInvokePaymentStub);
				docRespPayZ = AcademyPaymentStub.invokePayeezyPaymentStub(strPayZInp);
			}else{
				log.verbose("Stub is OFF and Invoking Payeez");
				docRespPayZ = AcademyPayeezyRestUtil.invokePayeezyRestService(strPayZInp, strEndpointURL.trim(), restParameters);
			}
			//docRespPayZ = AcademyPayeezyRestUtil.invokePayeezyRestService(strPayZInp, strEndpointURL.trim(), restParameters);	
			//OMNI-99459 - END
			if(!YFCObject.isVoid(docRespPayZ)) {
				docRespPayZ.getDocumentElement().setAttribute(AcademyConstants.JSON_ATTR_TRANSACTION_TYPE, strTransactionType);
			}
		}
		else if(!YFCObject.isVoid(strTransactionType) && strTransactionType.equalsIgnoreCase(AcademyConstants.TRANSACTION_TYPE_REFUND)){
			log.verbose(" doing refund transactions API call." + strTransactionType);

			docRespPayZ = processTaggedRefunds(env, docPaymentIn,strEndpointURL, restParameters);
			if(!YFCObject.isVoid(docRespPayZ)) {
				docRespPayZ.getDocumentElement().setAttribute(AcademyConstants.JSON_ATTR_TRANSACTION_TYPE, strTransactionType);
			}
			//Start : KER-15797 : Ewallet: Refund timeout retry limit is happening at infinite times.
			else {
				log.verbose(" No Valid CHARGE available for Refund");
				docRespPayZ = XMLUtil.createDocument(AcademyConstants.ELE_ERROR); 
				docRespPayZ.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, 
						AcademyConstants.STATUS_CODE_ERROR);
				docRespPayZ.getDocumentElement().setAttribute(AcademyConstants.JSON_ATTR_TRANSACTION_TYPE, 
						AcademyConstants.TRANSACTION_TYPE_REFUND);
				docRespPayZ.getDocumentElement().setAttribute(AcademyConstants.JSON_ATTR_BANK_MESSAGE,
				"REFUND_FAILED");

			}
			//End : KER-15797 : Ewallet: Refund timeout retry limit is happening at infinite times.
		}
		else if(bIsPaypalReAuthCall){
			log.verbose(" doing paypal auth transactions API call." + strTransactionType);
			//KER-[paypal JIRA]	
			//This is to check if we have to make only PayZ call. i.e. payPal call aready happen before

			//Start : KER-15779 : Prod Issue PayPal (Payment): Multiple ReAuth and Voids calls triggered from OMS						
			//String strPaymentReference7 = docPaymentIn.getDocumentElement().getAttribute(AcademyConstants.ATTR_PAYMENT_REF_7);

			//Retrieving the PaypalAuth Details from chargeTransactionErrorList.
			String strPayPalAuth = retrievePaypalAuthDetails(env, docPaymentIn);

			if(!YFCObject.isVoid(strPayPalAuth) && 
					strPayPalAuth.startsWith(AcademyConstants.STR_PAYZ_AUTH_FAIL)){
				bIsPaypalAuthSuccess = true;
				bPayZAuthRetry = true;
				log.verbose(":: strPayPalAuth ::  "+strPayPalAuth);
				String strPaymentReference[] = strPayPalAuth.split(AcademyConstants.STR_AT);

				String strId =  strPaymentReference[1];
				String strPayPalDebugId = strPaymentReference[2];
				String strPayPalAuthTime = strPaymentReference[3];

				//Retrieving Reversal ID to be re-used in timeout reversals.
				String strReversalId = AcademyPayeezyRestUtil.getAutoGeneratedUUID();
				restParameters.put(AcademyConstants.WEBSERVICE_REVERSAL_ID, strReversalId);

				strPayZInp = AcademyPayeezyRestUtil.createPAYZRestInputForPaypal(docPaymentIn, strId, 
						strPayPalDebugId, strPayPalAuthTime, strTransactionType,restParameters);

				//Call the Payeezy gateway service
				//This methd also set variable if PayZ call failed
				docRespPayZ = invokePayeezyForPayPal(strPayZInp, strEndpointURL, restParameters, docPaymentIn, 
						strPayPalAuthTime, strOrderHeaderKey, strChargeTransactionKey);

			}else{
				//Invoke PayPal system to get authorization
				docRespPaypal = AcademyPayPalRestUtil.getPayPalAuthorization(env, docPaymentIn,restParameters);
				String strPayPalState = docRespPaypal.getDocumentElement().getAttribute(AcademyConstants.ATTR_PAYPAL_RESPONSE_STATE);

				if(AcademyConstants.ELE_ERROR.equals(docRespPaypal.getNodeName())){
					log.verbose("docRespPaypal.getNodeName())-->  "+docRespPaypal.getNodeName());
					//in case of any connection issue, we will received error document. So setting  docRespPayZ as null to retry paypal auth call
					docRespPayZ = null;
					bRetry = true;
				}else if(AcademyConstants.STR_PAYPAL_RESPONSE_AUTHOSIZED.equals(strPayPalState)){				

					bIsPaypalAuthSuccess = true;
					log.verbose("bIsPaypalAuthSuccess: " +bIsPaypalAuthSuccess);
					//docRespPayZ = docRespPaypal;//Need to prepare the PayZ response

					String strPayPalAuthTime = docRespPaypal.getDocumentElement().getAttribute(AcademyConstants.ATTR_PAYPAL_RESPONSE_CREATE_TIME);
					String strId =  docRespPaypal.getDocumentElement().getAttribute(AcademyConstants.PAYPAL_RESPONSE_ID);
					String strPayPalDebugId = docRespPaypal.getDocumentElement().getAttribute(AcademyConstants.ATTR_PAYPAL_DEBUG_ID);
					
					//Retrieving Reversal ID to be re-used in timeout reversals.
					String strReversalId = AcademyPayeezyRestUtil.getAutoGeneratedUUID();
					restParameters.put(AcademyConstants.WEBSERVICE_REVERSAL_ID, strReversalId);

					strPayZInp = AcademyPayeezyRestUtil.createPAYZRestInputForPaypal(docPaymentIn,strId, strPayPalDebugId, 
							strPayPalAuthTime, strTransactionType, restParameters);

					//Call the Payeezy gateway service
					docRespPayZ = invokePayeezyForPayPal(strPayZInp, strEndpointURL, restParameters, docPaymentIn, 
							strPayPalAuthTime, strOrderHeaderKey, strChargeTransactionKey);
					
					//Update PayPal AUTH ID in the response
					docRespPayZ.getDocumentElement().setAttribute(AcademyConstants.PAYPAL_RESPONSE_ID, strId);
				}else{
					log.verbose("PayPal auth failed");
					docRespPayZ = docRespPaypal;
					//docRespPayZ.getDocumentElement().setAttribute("TransactionType", strTransactionType);
					//Handled paypal auth error scenario inside translatePayPalResp()
				}
			}
			//End : KER-15779 : Prod Issue PayPal (Payment): Multiple ReAuth and Voids calls triggered from OMS
		}
		else if(!YFCObject.isVoid(strTransactionType) && !bIsPaypalSettlementRetry){
			//Retrieving Reversal ID to be re-used in timeout reversals.
			String strReversalId = AcademyPayeezyRestUtil.getAutoGeneratedUUID();
			restParameters.put(AcademyConstants.WEBSERVICE_REVERSAL_ID, strReversalId);
			//Get the PayZ REST resquest in JSON format 
			strPayZInp = AcademyPayeezyRestUtil.createPAYZRestInputForCCAuth(docPaymentIn, strTransactionType, strSplitShipment, restParameters);
			log.verbose("AcademyInvokePayeezyRestWebservice.invokePAYZHTTPCCService() JSON Input" + strPayZInp);

			//Check if timeout reversal is required for the transaction
			validateTimeoutReversal(env, docPaymentIn, AcademyConstants.STR_SERVICE_UNAVAILABLE, strEndpointURL, restParameters);

			//Call the Payeezy gateway service
			//OMNI-99459 - START
			if(AcademyConstants.STR_YES.equalsIgnoreCase(strInvokePaymentStub)){
				//Invoke payment stub method
				docRespPayZ = AcademyPaymentStub.invokePayeezyPaymentStub(strPayZInp);
			}else{
				docRespPayZ = AcademyPayeezyRestUtil.invokePayeezyRestService(strPayZInp, strEndpointURL, restParameters);
			}
			//docRespPayZ = AcademyPayeezyRestUtil.invokePayeezyRestService(strPayZInp, strEndpointURL, restParameters);	
			//OMNI-99459 - END
			if(!YFCObject.isVoid(docRespPayZ))
				docRespPayZ.getDocumentElement().setAttribute(AcademyConstants.JSON_ATTR_TRANSACTION_TYPE, strTransactionType);
		}
		else {
			log.verbose(" Exception scenario where webservice call is not eligible");
			docRespPayZ = XMLUtil.createDocument(AcademyConstants.ELE_ERROR); 
			docRespPayZ.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, AcademyConstants.STATUS_CODE_ERROR);
			if(!YFCObject.isVoid(docRespPayZ))
				docRespPayZ.getDocumentElement().setAttribute(AcademyConstants.JSON_ATTR_TRANSACTION_TYPE, AcademyConstants.STATUS_CODE_ERROR);
		}

		if(!bIgnoreAuth && YFCObject.isVoid(docRespPayZ)) {
			this.bRetry = true;
		}

		if(this.bRetry) {
			log.verbose("bRetry: " +bRetry);
			this.bRetry = false;
			docPaymentOut = XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT);
			//Retry transaction
			retryTransaction(docPaymentOut,props.getProperty(AcademyConstants.STR_NEXT_TRIGGER_IN_MIN));
			Element elePayTimedOut = docPaymentOut.getDocumentElement();
			if(bIsPaypalOrder){
				//Start : KER-15779 : Prod Issue PayPal (Payment): Multiple ReAuth and Voids calls triggered from OMS
				//Set PaymentReference7 to handle PayPal reAuth retry scenario
				elePayTimedOut = setPayPalAuthRetryAttributes(docPaymentIn, docRespPaypal, elePayTimedOut);
				//End : KER-15779 : Prod Issue PayPal (Payment): Multiple ReAuth and Voids calls triggered from OMS
			}			
		} else {
			//Parse the PayZ response to form Payment XML
			if (AcademyConstants.STR_CHRG_TYPE_AUTH.equalsIgnoreCase(chargeType)) {								

				if(!bIgnoreAuth) {
					//KER-[paypal JIRA]: payPal failure scenario add translatePayPalResp(New method) and skip below translatePayZResp
					if(bIsPaypalReAuthCall && (!bIsPaypalAuthSuccess)){
						docPaymentOut = translatePayPalResp(docPaymentIn,docRespPaypal,strTransactionType);
					}
					else{
						docPaymentOut = translatePayZResp(docPaymentIn,docRespPayZ);
					}
				} else {
					docPaymentOut = XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT);
					Element elePaymentOut = docPaymentOut.getDocumentElement();
					elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, strAuthId);
					elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_CODE, strAuthId);				
					elePaymentOut.setAttribute(AcademyConstants.ATTR_REQUEST_ID, strAuthId);				
					elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_AMT, strReqAmt);
					elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_AMOUNT, strReqAmt);

					if(strPaymentType.equals(AcademyConstants.STR_PAYMENT_TYPE_CREDIT_CARD_NEW) && !(YFCObject.isVoid(dateCurrAuthExpiryDate) 
							|| dateCurrAuthExpiryDate.before(Calendar.getInstance().getTime()))) {
						//Not an Expired Auth. so restricting the call.
						prepareDummyAuthResponse(elePaymentOut, AcademyConstants.STR_APPROVED, AcademyConstants.STR_RESTRICTED_AUTH, 
								AcademyConstants.STR_RESTRICTED_AUTH, AcademyConstants.STR_RESTRICTED_AUTH, AcademyConstants.STR_RESTRICTED_AUTH);
					}
					else {
						prepareDummyAuthResponse(elePaymentOut, AcademyConstants.STR_APPROVED, AcademyConstants.STR_DUMMY_AUTH, 
								AcademyConstants.STR_DUMMY_AUTH, AcademyConstants.STR_DUMMY_AUTH, AcademyConstants.STR_DUMMY_AUTH);
					}			
					Calendar cal = Calendar.getInstance();				
					SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN_NEW);
					elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_TIME, sdf.format(cal.getTime()));
				}

				//PaymentReference9 contains split shipment count and PaymentReference8 contains the Settled amount on the AUTH
				//If Complete AUTH has been voided then reset the PaymentRef9 and PaymentRef8
				if(bIsCompleteAuthVoided) {
					docPaymentOut.getDocumentElement().setAttribute(AcademyConstants.ATTR_PAYMENT_REF_9, "");
					docPaymentOut.getDocumentElement().setAttribute(AcademyConstants.ATTR_PAYMENT_REF_8, "");
					
					//Start : KER-15764: Void call not sent after cancelling an entire order through multiple cancellations.
					//Updating the Actual request Amount as the value in UE Output
					log.verbose(" Updating the Output with same amount for eWallets");
					if(bIsEWalletOrders){
						
						//Begin : OMNI-32161, 32162, 32163, 32164, 32165, 32166, 32167, 32169
						docPaymentOut.getDocumentElement().setAttribute(AcademyConstants.ATTR_AUTH_AMOUNT, 
								docPaymentIn.getDocumentElement().getAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT));
						docPaymentOut.getDocumentElement().setAttribute(AcademyConstants.ATTR_TRAN_AMT, 
								docPaymentIn.getDocumentElement().getAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT));
						//End : OMNI-32161, 32162, 32163, 32164, 32165, 32166, 32167, 32169
						
					}
					//End : KER-15764: Void call not sent after cancelling an entire order through multiple cancellations.
				}

				bIgnoreAuth = false;//reset the flag

				if(AcademyConstants.STR_YES.equalsIgnoreCase(strIsDBLoggingEnableForAuth) &&
						!docPaymentOut.getDocumentElement().getAttribute(AcademyConstants.ATTR_RESPONSE_CODE)
						.equalsIgnoreCase(AcademyConstants.STR_APPROVED)) {
					log.verbose("Looging request and response in DB");
					if(!YFSObject.isVoid(strPayZInp))
					{
						InputStream in = IOUtils.toInputStream(strPayZInp);
						String xml = XML.toXml(in);

						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						factory.setNamespaceAware(true);
						DocumentBuilder builder = factory.newDocumentBuilder();
						Document docReqPayZ =  builder.parse(new InputSource(new StringReader(xml)));
						AcademyPaymentProcessingUtil.logPayZReqAndRespToDB(env, docPaymentIn, docReqPayZ, 
								docRespPayZ, AcademyConstants.STR_DB_SERVICE_FOR_PAYZ);
					}else if(!YFSObject.isVoid(docRespPaypal)){
						AcademyPaymentProcessingUtil.logPayZReqAndRespToDB(env, docPaymentIn, null, 
								docRespPaypal, AcademyConstants.STR_DB_SERVICE_FOR_PAYZ);
					}
				}
				log.verbose("Payment authorization xml::"+XMLUtil.getXMLString(docPaymentOut));
			} else if (AcademyConstants.STR_CHRG_TYPE_CHARGE.equalsIgnoreCase(chargeType)) {
				docPaymentOut = translatePayZResp(docPaymentIn,docRespPayZ);

				//Handling timeout reversals in case of Refunds
				if(!YFCObject.isVoid(strTransactionType) 
						&& AcademyConstants.TRANSACTION_TYPE_REFUND.equals(strTransactionType)) {

					//Suspend Payment in the webservice output is declined for refunds.
					if(docPaymentOut.getDocumentElement().getAttribute(AcademyConstants.ATTR_RESPONSE_CODE)
							.equalsIgnoreCase(AcademyConstants.STR_HARD_DECLINED)) {
						docPaymentOut.getDocumentElement().setAttribute(AcademyConstants.ATTR_SUSPEND_PAYMENT, 
								AcademyConstants.STR_YES);
					}
					
					if(hmFailedReversalId.size()>0) {
						log.verbose(" Failed Reversals available for Refund");
						docPaymentOut = updateFailedReversalsForRefund(docPaymentIn, docPaymentOut);
					}
				}

				String strIsDummySettlement = docPaymentOut.getDocumentElement().getAttribute(AcademyConstants.ATTR_IS_DUMMY_SETTLEMENT);
				log.verbose("Payment settlement/refund xml::"+XMLUtil.getXMLString(docPaymentOut));
				if(AcademyConstants.STR_YES.equalsIgnoreCase(strIsDBLoggingEnableForCharge) && (
						!docPaymentOut.getDocumentElement().getAttribute(AcademyConstants.ATTR_RESPONSE_CODE)
						.equalsIgnoreCase(AcademyConstants.STR_APPROVED)) || AcademyConstants.STR_YES.equalsIgnoreCase(strIsDummySettlement)) {
					//Start : KER-15797 : Ewallet: Refund timeout retry limit is happening at infinite times.
					if(!YFSObject.isVoid(strPayZInp)) {
						InputStream in = IOUtils.toInputStream(strPayZInp);
						String xml = XML.toXml(in);

						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						factory.setNamespaceAware(true);
						DocumentBuilder builder = factory.newDocumentBuilder();
						Document docReqPayZ =  builder.parse(new InputSource(new StringReader(xml)));

						AcademyPaymentProcessingUtil.logPayZReqAndRespToDB(env, docPaymentIn, docReqPayZ, 
								docRespPayZ, AcademyConstants.STR_DB_SERVICE_FOR_PAYZ);
					}else if(!YFSObject.isVoid(docRespPaypal)){
						AcademyPaymentProcessingUtil.logPayZReqAndRespToDB(env, docPaymentIn, 
								null, docRespPayZ, AcademyConstants.STR_DB_SERVICE_FOR_PAYZ);
					}
					//End : KER-15797 : Ewallet: Refund timeout retry limit is happening at infinite times.
				}

				if(docPaymentOut.getDocumentElement().getAttribute(AcademyConstants.ATTR_RESPONSE_CODE)
						.equalsIgnoreCase(AcademyConstants.STR_APPROVED)){
					//Update SplitShipment count in PaymentReference9
					docPaymentOut.getDocumentElement().setAttribute(AcademyConstants.ATTR_PAYMENT_REF_9, strSplitShipment);

					//Update Settled amount in PaymentReference8
					double dSettledAmount = 0.0;
					double dReqAmount = 0.0;
					DecimalFormat df = new DecimalFormat("0.00"); 
					String strSettledAmount = XPathUtil.getString(docPaymentIn, 
							AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_PAYMENT_REF_8);
					if(!YFCObject.isVoid(strSettledAmount)){
						dSettledAmount = Double.parseDouble(strSettledAmount);
					}
					if(!YFCObject.isVoid(strReqAmt)){
						dReqAmount = Double.parseDouble(strReqAmt);
					}
					docPaymentOut.getDocumentElement().setAttribute(AcademyConstants.ATTR_PAYMENT_REF_8, 
							df.format(dSettledAmount + dReqAmount));				
				}
				else {
					//Update the original value itself in the output
					docPaymentOut.getDocumentElement().setAttribute(AcademyConstants.ATTR_PAYMENT_REF_9, 
							XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_PAYMENT_REF_9));
					docPaymentOut.getDocumentElement().setAttribute(AcademyConstants.ATTR_PAYMENT_REF_8, 
							XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_PAYMENT_REF_8));
				}
			} 
		}

		//Updating the TranType for each transaction
		docPaymentOut.getDocumentElement().setAttribute(AcademyConstants.ATTR_TRAN_TYPE, chargeType);
		//Updating Original TransactionID as in output for reference
		docPaymentOut.getDocumentElement().setAttribute("InternalReturnMessage", strTransactionId);;

		log.verbose("AcademyInvokePayeezyRestWebservice.invokePAYZHTTPCCService() Payment Output XML:: "+XMLUtil.getXMLString(docPaymentOut));
		log.verbose("End of AcademyInvokePayeezyRestWebservice.invokePAYZHTTPCCService() method");

		return docPaymentOut;
	}



	private Element setPayPalAuthRetryAttributes(Document docPaymentIn, Document docRespPaypal, Element elePayTimedOut) 
	throws Exception {
		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.setPayPalAuthRetryAttributes() method::");
		if(bIsPaypalReAuthCall && !bSendPayPalAuthToPayZSuccess && bIsPaypalAuthSuccess){
			//Start : KER-15779 : Prod Issue PayPal (Payment): Multiple ReAuth and Voids calls triggered from OMS						
			//String strPaymentReference7 = docPaymentIn.getDocumentElement().getAttribute(AcademyConstants.ATTR_PAYMENT_REF_7);
			//Retrieving the PaypalAuth Details from chargeTransactionErrorList.
			String strPayPalAuth = retrievePaypalAuthDetails(env, docPaymentIn);

			if(YFSObject.isVoid(strPayPalAuth)){
				String strPayPalAuthTime = docRespPaypal.getDocumentElement().getAttribute("create_time");
				String strPayPalDebugId = docRespPaypal.getDocumentElement().getAttribute("Paypal-Debug-Id");
				String strId = docRespPaypal.getDocumentElement().getAttribute("id");
				String strPaymentReferenceValue = AcademyConstants.STR_PAYZ_AUTH_FAIL+AcademyConstants.STR_AT+strId+AcademyConstants.STR_AT+
				strPayPalDebugId+AcademyConstants.STR_AT+strPayPalAuthTime;
				//elePayTimedOut.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_7,strPaymentReferenceValue);
				elePayTimedOut = updateFailedPayZForPayPal(elePayTimedOut, strPaymentReferenceValue);
				elePayTimedOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, AcademyConstants.STR_FORCED_RETRY);
			}
		}
		if(bIsPaypalSettlementRetry){
			//This is to make sure closing the exising paypal open auth. 
			//String strPaymentReference7 = docPaymentIn.getDocumentElement().getAttribute(AcademyConstants.ATTR_PAYMENT_REF_7);
			String strPayPalAuth = retrievePaypalAuthDetails(env, docPaymentIn);

			//elePayTimedOut.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_7,strPaymentReference7+AcademyConstants.STR_AT+AcademyConstants.STR_SUSPEND_AUTH);	
			elePayTimedOut = updateFailedPayZForPayPal(elePayTimedOut, strPayPalAuth+AcademyConstants.STR_AT+AcademyConstants.STR_SUSPEND_AUTH);
			elePayTimedOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, AcademyConstants.STR_FORCED_RETRY);
		}
		//End : KER-15779 : Prod Issue PayPal (Payment): Multiple ReAuth and Voids calls triggered from OMS
		log.verbose("End of AcademyInvokePayeezyRestWebservice.setPayPalAuthRetryAttributes() method::");
		return elePayTimedOut;
	}



	private Element retryTransaction(Document docPaymentOut, String strNextTriggerIntervalInMin) {
		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.retryTransaction() method::");
		log.verbose("strNextTriggerIntervalInMin: " +strNextTriggerIntervalInMin);
		Element elePayTimedOut = docPaymentOut.getDocumentElement();
		elePayTimedOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
		elePayTimedOut.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_YES);			
		elePayTimedOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, AcademyConstants.STR_SOCKET_READ_TIMEOUT);
		elePayTimedOut.setAttribute(AcademyConstants.ATTR_COLLECTION_DATE, nextCollectionDate(strNextTriggerIntervalInMin));
		log.verbose("End of AcademyInvokePayeezyRestWebservice.retryTransaction() method::");

		return elePayTimedOut;
	}
	
	/**This method invokes PayZ based on the PayPal response 
	 * @param docPayIn
	 * @param docPayZAuthOut
	 * @return
	 * @throws Exception
	 */
	private Document invokePayeezyForPayPal(String strPayZInp,String strEndpointURL, Map<String, String> restParameters, 
			Document docPaymentIn, String strPayPalAuthTime, String strOrderHeaderKey, String strChargeTransactionKey) throws Exception {
		Document docRespPayZ;
		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.createAuthInPayZ() method::");

		//Check if timeout reversal is required for the transaction
		validateTimeoutReversal(env, docPaymentIn, AcademyConstants.STR_SERVICE_UNAVAILABLE, strEndpointURL, restParameters);

		//docPaymentIn will use to raise alert if needed in future
		docRespPayZ = AcademyPayeezyRestUtil.invokePayeezyRestService(strPayZInp, strEndpointURL.trim(), restParameters);		
		if("approved".equals(docRespPayZ.getDocumentElement().getAttribute("transaction_status"))){						
			bSendPayPalAuthToPayZSuccess = true;
			log.verbose("bSendPayPalAuthToPayZSuccess:  " +bSendPayPalAuthToPayZSuccess);
			docRespPayZ.getDocumentElement().setAttribute("PayPalAuthTime", strPayPalAuthTime);					
		}else{
			/*In case PayZ auth call fails, sterling will retry the auth continiously. 
			If there is any settlement request before this auth success, sterling will append SUSPEND_AUTH in paymentReferrence7 
			at the time of settlement request process. In this case we should hard declined this auth request.
			 */
			//Start : KER-15780 : Void call has not triggered before Reauth for PayPal
			//String strPaymentReference7 = docPaymentIn.getDocumentElement().getAttribute(AcademyConstants.ATTR_PAYMENT_REF_7);
			String strPayPalAuthDetails = retrievePaypalAuthDetails(env, docPaymentIn);
			
			if(YFCObject.isVoid(strPayPalAuthDetails) && 
					!(strPayPalAuthDetails.startsWith(AcademyConstants.STR_PAYZ_AUTH_FAIL) 
					&& strPayPalAuthDetails.contains(AcademyConstants.STR_SUSPEND_AUTH))){
				this.bRetry = true;					
			}
			//End : KER-15780 : Void call has not triggered before Reauth for PayPal
			bSendPayPalAuthToPayZSuccess= false;
			log.verbose("bSendPayPalAuthToPayZSuccess:  " +bSendPayPalAuthToPayZSuccess);
		}
		log.verbose("End of AcademyInvokePayeezyRestWebservice.createAuthInPayZ() method::");
		return docRespPayZ;
	}



	/**This method parses the PayZ AUTH response to Payment output XML 
	 * @param docPayIn
	 * @param docPayZAuthOut
	 * @return
	 * @throws Exception
	 */
	private Document translatePayZResp(Document docPayIn,Document docPayZAuthOut) throws Exception {

		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.translatePayZResp() method::\n" +XMLUtil.getXMLString(docPayZAuthOut));
		Document docPayAuthXML = XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT);
		Element elePaymentOut = docPayAuthXML.getDocumentElement();

		Element elePayZresp = docPayZAuthOut.getDocumentElement();

		String strHttpStatusCode = elePayZresp.getAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE);
		String strCorelationId = elePayZresp.getAttribute(AcademyConstants.JSON_ATTR_CORRELATION_ID);
		String strTransactionStatus = elePayZresp.getAttribute(AcademyConstants.JSON_ATTR_TRANSACTION_STATUS);
		String strTransactionType = elePayZresp.getAttribute(AcademyConstants.JSON_ATTR_TRANSACTION_TYPE);
		String strValidationStatus = elePayZresp.getAttribute(AcademyConstants.JSON_ATTR_VALIDATION_STATUS);
		String strTransactionId = elePayZresp.getAttribute(AcademyConstants.JSON_ATTR_TRANSACTION_ID);
		String strTransactionTag = elePayZresp.getAttribute(AcademyConstants.JSON_ATTR_TRANSACTION_TAG);
		String strBankRespCode = elePayZresp.getAttribute(AcademyConstants.JSON_ATTR_BANK_RESP_CODE);
		String strBankMessage = elePayZresp.getAttribute(AcademyConstants.JSON_ATTR_BANK_MESSAGE);
		String strGatewayRespCode = elePayZresp.getAttribute(AcademyConstants.JSON_ATTR_GATEWAY_RESP_CODE);
		String strGatewayMessage = elePayZresp.getAttribute(AcademyConstants.JSON_ATTR_GATEWAY_MESSAGE);
		String strAmount =   elePayZresp.getAttribute(AcademyConstants.JSON_ATTR_AMOUNT);
		String strReversalId =   elePayZresp.getAttribute(AcademyConstants.JSON_ATTR_REVERSAL_ID);
		String strId =   elePayZresp.getAttribute(AcademyConstants.PAYPAL_RESPONSE_ID);
		
		String strOrderHeaderKey =  XPathUtil.getString(docPayIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_HEADER_KEY);
		String strChargeTransactionKey =  XPathUtil.getString(docPayIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY);
		
		//Begin : OMNI-32161, 32162, 32163, 32164, 32165, 32166, 32167, 32169
		
		/*
		 * Retrieve Payment Type to check if it is Apple_Pay or Google_Pay,
		 * in order to save cardbrand original transaction id under payment reference 6
		 */
		
		String strPaymentType =  XPathUtil.getString(docPayIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_PAYMENT_TYPE);
		//End : OMNI-32161, 32162, 32163, 32164, 32165, 32166, 32167, 32169
		
		if(YFSObject.isVoid(strBankRespCode)) {
			strBankRespCode = XPathUtil.getString(elePayZresp, "/jsonObject/Error/messages/@code");
		}
		if(YFSObject.isVoid(strBankMessage)) {
			strBankMessage = XPathUtil.getString(elePayZresp, "/jsonObject/Error/messages/@description");
		}

		if(!YFSObject.isVoid(strTransactionType) && 
				(strTransactionType.equalsIgnoreCase(AcademyConstants.TRANSACTION_TYPE_VOID)
						|| strTransactionType.equalsIgnoreCase(AcademyConstants.TRANSACTION_TYPE_REFUND))){
			strAmount = AcademyConstants.STR_HYPHEN + strAmount;
		}

		if(strHttpStatusCode.equalsIgnoreCase(AcademyConstants.STATUS_CODE_200) 
				|| strHttpStatusCode.equalsIgnoreCase(AcademyConstants.STATUS_CODE_201)
				|| strHttpStatusCode.equalsIgnoreCase(AcademyConstants.STATUS_CODE_202)) {
			log.verbose(" Valid HTTP Status response. "+strHttpStatusCode);
			log.verbose("For OrderNo :"+XPathUtil.getString(docPayIn, AcademyConstants.STR_PAYMENT_PATH + "@OrderNo")
					+", Amount : "+strAmount+" , strBankRespCode of auth call :"+strBankRespCode 
					+" , strGatewayRespCode of auth call :"+strGatewayRespCode);
			if(strBankRespCode == null){
				elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
				elePaymentOut.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_YES);
				elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE_DESC);
				return docPayAuthXML;
			}

			//Check for Gateway Success Resposne Codes and then Success Bank Response codes
			if(!YFSObject.isVoid(strGatewayRespCode) && listPayZGatewaySuccessResponseCode.contains(strGatewayRespCode) && 
					!YFSObject.isVoid(strBankRespCode) && listPayZBankSuccessResponseCode.contains(strBankRespCode)) {

				log.verbose(" Successful Gateway :"+strGatewayRespCode+" and Bank : "+strBankRespCode+" response codes" );
				elePaymentOut = prepareApprovedResp(elePaymentOut, strBankRespCode, strBankMessage, 
						strHttpStatusCode, strGatewayMessage, strCorelationId);

				elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, strTransactionTag);
				elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_AMOUNT, strAmount);
				elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_AMT, strAmount);

				//Setting the AuthId details in AUth Return Code.
				elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, strTransactionTag);

				//Contains the Transaction ID for the request. To be used in Settlement
				elePaymentOut.setAttribute(AcademyConstants.ATTR_REQUEST_ID, strTransactionId);
				//KER-14869 stamp TransactionId as AuthCode
				elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_CODE, strTransactionId);

				//Update PayPal AuthId if available
				if(!YFSObject.isVoid(strId)) {
					elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_CODE, strId);
				}				

				if(bSendPayPalAuthToPayZSuccess){
					String strPayPalAuthTime = elePayZresp.getAttribute("PayPalAuthTime");
					elePaymentOut.setAttribute("PayPalAuthTime", strPayPalAuthTime);
				}
				
				//Begin : OMNI-32161, 32162, 32163, 32164, 32165, 32166, 32167, 32169
				/*
				 * Updating Payment Reference 6 with the Original Transaction Id for EWallet 
				 */

				if(AcademyConstants.STR_APPLE_PAY_PAYMENT.equalsIgnoreCase(strPaymentType) 
						|| AcademyConstants.STR_GOOGLE_PAY_PAYMENT.equalsIgnoreCase(strPaymentType)) {
					
					log.verbose(" :: Updating the Payment Refernce 6 logic for eWallets ");
					String strPaymentRef6 = XPathUtil.getString(elePayZresp, "/jsonObject/stored_credentials/@cardbrand_original_transaction_id");				
					
					if(!YFCObject.isNull(strPaymentRef6)) {
					
						log.verbose(" :: strPaymentRef6 :: " + strPaymentRef6);
						
						elePaymentOut.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_6, strPaymentRef6);
					}
						
					
				}
				//End : OMNI-32161, 32162, 32163, 32164, 32165, 32166, 32167, 32169

			} 
			//Check if atleast Gateway Succesful response recieved
			else if(!YFSObject.isVoid(strGatewayRespCode) && listPayZGatewaySuccessResponseCode.contains(strGatewayRespCode)) {
				log.verbose(" Only  Gateway :"+strGatewayRespCode+ "response recieved. Bank Error. ");

				if(!YFSObject.isVoid(strBankRespCode) && listPayZBankRetryResponseCode.contains(strBankRespCode)) {
					log.verbose(" Bank Retry Response code  :"+strBankRespCode+ ". Retry after some time.");
					int iErrorCount = getCountOfPaymentTransactionError(env, strOrderHeaderKey, strChargeTransactionKey);
					elePaymentOut = prepareServiceUnavailableResp(docPayIn, elePaymentOut, strTransactionType, strBankRespCode, strBankMessage, 
							strHttpStatusCode, strGatewayMessage, strCorelationId, strReversalId, iErrorCount);
				}
				else {
					elePaymentOut = prepareHardDeclinedResp(docPayIn, elePaymentOut, strTransactionType, strBankRespCode, strBankMessage, 
							strGatewayRespCode, strGatewayMessage, strCorelationId);
				}
			}
			else {
				//Not a valid bank response code or Gateway response. 
				if(YFSObject.isVoid(strGatewayRespCode)) {
					strGatewayRespCode = strHttpStatusCode;
				}
				if(YFSObject.isVoid(strGatewayMessage)) {
					strGatewayMessage = "Payeezy Error";
				}
				elePaymentOut = prepareHardDeclinedResp(docPayIn, elePaymentOut, strTransactionType, strBankRespCode, strBankMessage, 
						strHttpStatusCode, strGatewayMessage, strCorelationId);
			}
		}
		else if(strHttpStatusCode.equalsIgnoreCase(AcademyConstants.STATUS_CODE_400) 
				|| strHttpStatusCode.equalsIgnoreCase(AcademyConstants.STATUS_CODE_401)
				|| strHttpStatusCode.equalsIgnoreCase(AcademyConstants.STATUS_CODE_404)){
			log.verbose(" Invalid HTTP Status response."+strHttpStatusCode+" Updating Payment as Declined");

			if(YFSObject.isVoid(strGatewayRespCode)) {
				strGatewayRespCode = strHttpStatusCode;
			}
			if(YFSObject.isVoid(strGatewayMessage)) {
				strGatewayMessage = "Webservice Error";
			}
			elePaymentOut = prepareHardDeclinedResp(docPayIn, elePaymentOut, strTransactionType,  strBankRespCode, strBankMessage, 
					strHttpStatusCode, strGatewayMessage, strCorelationId);
		}
		else if (strHttpStatusCode.equalsIgnoreCase(AcademyConstants.STATUS_CODE_ERROR)) {
			
			if(YFSObject.isVoid(strBankMessage)) {
				strBankMessage = "PAYMENT NOT ELIGIBLE";
			}
			
			elePaymentOut = prepareHardDeclinedResp(docPayIn, elePaymentOut, strTransactionType,  "", strBankMessage, 
					strHttpStatusCode, strBankMessage, "");
		}
		else {
			if(YFSObject.isVoid(strGatewayRespCode)) {
				strGatewayRespCode = strHttpStatusCode;
			}
			if(YFSObject.isVoid(strGatewayMessage)) {
				strGatewayMessage = "Webservice Not available";
			}
			int iErrorCount = getCountOfPaymentTransactionError(env, strOrderHeaderKey, strChargeTransactionKey);
			elePaymentOut = prepareServiceUnavailableResp(docPayIn, elePaymentOut, strTransactionType, strBankRespCode, strBankMessage, 
					strHttpStatusCode, strGatewayMessage, strCorelationId, strReversalId, iErrorCount);
		}
		log.verbose("End of AcademyInvokePayeezyRestWebservice.translatePayZResp() method");
		return docPayAuthXML;		

	}


	/**This method provides the next collection date for Payment Agent to process
	 * @param strNxtTrigger
	 * @return
	 */
	private String nextCollectionDate(String strNxtTrigger) {
		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.nextCollectionDate() method ::");		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN_1);		
		int iNextTriggerIntervalInMin = Integer.parseInt(strNxtTrigger);
		cal.add(Calendar.MINUTE, iNextTriggerIntervalInMin);
		String strCollectionDate = sdf.format(cal.getTime());
		log.verbose("End of AcademyInvokePayeezyRestWebservice.nextCollectionDate() method ::");		
		return strCollectionDate;
	}


	/**
	 * This method invokes getOrderList API and checks if any setellement is already done for a given Authorization ID
	 * 
	 */
	private boolean validateSettledLinesOnOrder(YFSEnvironment env, Document docPaymentIn) throws Exception {

		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.validateSettledLinesOnOrder() method");
		boolean bIsOrderPartiallySettled = false;

		String strSettledAmount = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_PAYMENT_REF_8);
		if(!YFCObject.isVoid(strSettledAmount)){
			log.verbose(" Amount settled on Payment ::"+strSettledAmount);
			bIsOrderPartiallySettled = true;
		}
		else {
			bIsOrderPartiallySettled = validateSettlementCTRs(docPaymentIn);
		}

		log.verbose("bIsOrderPartiallySettled :: "+bIsOrderPartiallySettled);
		log.verbose("End of AcademyInvokePayeezyRestWebservice.validateSettledLinesOnOrder() method");
		return bIsOrderPartiallySettled;
	}


	/**
	 * This method checks if complete AUTH is being voided or only Partial
	 * 
	 */
	private boolean validateCompleteOrderCancel(YFSEnvironment env, Document docPaymentIn) throws Exception {

		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.validateCompleteOrderCancel() method");
		boolean bIsOrderPartiallySettled = false;

		String strCurrentAuthAmount = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CURRENT_AUTHORIZATION_AMT);
		DecimalFormat df = new DecimalFormat("0.00"); 
		strCurrentAuthAmount = df.format(Double.parseDouble(strCurrentAuthAmount));

		if(!YFCObject.isVoid(strCurrentAuthAmount)){
			log.verbose(" strCurrentAuthAmount ::"+strCurrentAuthAmount);
			//Start : KER-15764 : Void call not sent after cancelling an entire order through multiple cancellations
			double dCurrentAuthAmt = Double.parseDouble(strCurrentAuthAmount);
			
			if(dCurrentAuthAmt != 0.00){
				bIsOrderPartiallySettled = true;	
			}
			//End : KER-15764 : Void call not sent after cancelling an entire order through multiple cancellations
		}

		log.verbose("bIsOrderPartiallySettled :: "+bIsOrderPartiallySettled);
		log.verbose("End of AcademyInvokePayeezyRestWebservice.validateCompleteOrderCancel() method");
		return bIsOrderPartiallySettled;
	}

	/**
	 * This method retrievies the split shipment count based on the payment Reference 9 field.
	 * 
	 */
	private String retrieveSplitShipmentCount(YFSEnvironment env, Document docPaymentInp, String strAuthorizationId) throws Exception {

		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.retrieveSplitShipmentCount() method");
		String strSplitShipment = null;
		int iNoOfShipments = 1;

		strSplitShipment = XPathUtil.getString(docPaymentInp, 
				AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_PAYMENT_REF_9);
		String strCurrentAuthAmount = XPathUtil.getString(docPaymentInp, 
				AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CURRENT_AUTHORIZATION_AMT);
		String strRequestAmount = XPathUtil.getString(docPaymentInp, 
				AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);
		String strAuthAmount = XPathUtil.getString(docPaymentInp, 
		"/Payment/CreditCardTransactions/CreditCardTransaction/@AuthAmount");

		DecimalFormat df = new DecimalFormat("0.00"); 
		strCurrentAuthAmount = df.format(Double.parseDouble(strCurrentAuthAmount));
		strAuthAmount = df.format(Double.parseDouble(strAuthAmount));

		if(!YFCObject.isVoid(strSplitShipment)) {
			iNoOfShipments = Integer.parseInt(strSplitShipment.split("/")[0]) + 1;
		}
		else {
			log.verbose(" First shipment on line.");
		}
		
		//Check if the Shipment is last shipment on Order
		//Begin : KER-15336 : Split shipments is not valid for orders with single Shipments
		if(Double.parseDouble(strRequestAmount) == Double.parseDouble(strCurrentAuthAmount)) {
			log.verbose(" First and Last shipment on line.");
			if(iNoOfShipments != 1) {
				strSplitShipment = Integer.toString(iNoOfShipments) + "/" + Integer.toString(iNoOfShipments);	
			}
		}
		else if((!YFCObject.isVoid(strCurrentAuthAmount) && Double.parseDouble(strCurrentAuthAmount)==0)
				|| (!YFCObject.isVoid(strAuthAmount) && Double.parseDouble(strAuthAmount)==0)) {
			//last shipment on the Auth.
			log.verbose(" Last shipment on Auth.");
			if(iNoOfShipments != 1) {
				strSplitShipment = Integer.toString(iNoOfShipments) + "/" + Integer.toString(iNoOfShipments);
			}
		}
		else {
			log.verbose(" Order yet to be shipped completely. Partially shipped");
			strSplitShipment = Integer.toString(iNoOfShipments) + "/99";
		}
		//End : KER-15336 : Split shipments is not valid for orders with single Shipments
		log.verbose("End of AcademyInvokePayeezyRestWebservice.retrieveSplitShipmentCount() method");
		return strSplitShipment;
	}

	/**
	 * This method invokes getOrderList API and checks if any setellement is already done for a given Authorization ID
	 * 
	 */
	private static Document getOrderList(YFSEnvironment env, String strOrderHeaderKey) throws Exception {

		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.getOrderList() method");
		Document docGetOrderListInp = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		docGetOrderListInp.getDocumentElement().setAttribute(AcademyConstants.STR_ORDR_HDR_KEY, strOrderHeaderKey);

		//env.setApiTemplate("getOrderList", "global/template/api/getOrderList.ChargeDetails.xml");
		//Invoke API getOrderList
		//Document docGetOrderListOut = AcademyUtil.invokeAPI(env, "getOrderList", docGetOrderListInp);
		Document docGetOrderListOut = AcademyUtil.invokeService(env, 
				AcademyConstants.SERV_ACAD_GET_ORDER_LIST_FOR_PAYMENT_SERVICE, docGetOrderListInp);
		//Clear the template
		//env.clearApiTemplate("getOrderList");
		log.verbose("End of AcademyInvokePayeezyRestWebservice.getOrderList() method");
		return docGetOrderListOut;
	}

	/**
	 * This method prepares the UE output document in case of APPROVED scenarios
	 * 
	 */
	private Element prepareApprovedResp(Element elePaymentOut, String strBankRespCode, String strBankMessage,
			String strGatewayRespCode, String strGatewayMessage, String strCorelationId) throws Exception {

		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.prepareApprovedResp() method");
		elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_APPROVED);
		//elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_CODE, strBankRespCode);//KER-14869 stamp TransactionId as AuthCode

		//Contains Bank Response Code and Message
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, strBankRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, strBankMessage);

		//Contains Gateway Response Code and Message
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE, strGatewayRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_MESSAGE, strGatewayMessage);

		//Contains the ID for reference to reach out contact Payeezy for data against this request
		elePaymentOut.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_CODE, strCorelationId);

		int iExpirationDays = 0;

		log.verbose("bIsPaypalReAuthCall: " +bIsPaypalReAuthCall);
		log.verbose("bPayZAuthRetry: " +bPayZAuthRetry);
		if(bIsPaypalReAuthCall){
			iExpirationDays = Integer.parseInt(strPayZPPAuthExpiryDays);


			if(bPayZAuthRetry){
				//Start : KER-15779 : Prod Issue PayPal (Payment): Multiple ReAuth and Voids calls triggered from OMS
				//elePaymentOut.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_7,"PayZ Auth Retry Success");
				updateFailedPayZForPayPal(elePaymentOut, "PayZ Auth Retry Success");
				//End : KER-15779 : Prod Issue PayPal (Payment): Multiple ReAuth and Voids calls triggered from OMS
			}
		}else{
			iExpirationDays = Integer.parseInt(strPayZCCAuthExpiryDays);
		}

		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN_NEW);
		String strExpirationDate = "";
		String strAuthTime = elePaymentOut.getAttribute("PayPalAuthTime");

		if(!YFSObject.isVoid(strAuthTime)){
			log.verbose("setting actual Auth time");
			SimpleDateFormat sdfSource = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			Date date = sdfSource.parse(strAuthTime);
			strAuthTime = sdf.format(date);

			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.DATE, iExpirationDays);
			strExpirationDate = sdf.format(cal.getTime());
		}
		else{			
			log.verbose("Setting current time");
			Calendar cal = Calendar.getInstance();
			strAuthTime = sdf.format(cal.getTime());	

			cal.add(Calendar.DATE, iExpirationDays);
			strExpirationDate = sdf.format(cal.getTime());
		}
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_TIME, strAuthTime);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_EXP_DATE,strExpirationDate);

		log.verbose("End of AcademyInvokePayeezyRestWebservice.prepareApprovedResp() method");
		return elePaymentOut;
	}

	/**
	 * This method prepares the UE output document in case of HARD_DECLINED scenarios
	 * 
	 */
	private Element prepareHardDeclinedResp(Document docPaymentIn, Element elePaymentOut, String strTransactionType, String strBankRespCode, 
			String strBankMessage, String strGatewayRespCode, String strGatewayMessage, 
			String strCorelationId) throws Exception {
		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.prepareHardDeclinedResp() method");
		log.verbose("bIsPaypalOrder: " +bIsPaypalOrder);
		log.verbose("strTransactionType: " +strTransactionType);
		
		String strChargeType =  XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH + AcademyConstants.ATTR_CHARGE_TYPE_AT);
		
		if(!YFSObject.isVoid(strTransactionType) && 
				strTransactionType.equalsIgnoreCase(AcademyConstants.TRANSACTION_TYPE_PURCHASE)){
			//Incase a purchase trnasaction falis, do a dummy settlement
			elePaymentOut = prepareDummyPurchaseResponse(docPaymentIn,elePaymentOut, strBankRespCode, 
					strBankMessage, strGatewayRespCode, strGatewayMessage, strCorelationId);
		}else if(!YFSObject.isVoid(strTransactionType) 
				&& (strTransactionType.equalsIgnoreCase(AcademyConstants.TRANSACTION_TYPE_CAPTURE)
						|| strTransactionType.equalsIgnoreCase(AcademyConstants.TRANSACTION_TYPE_SPLIT))){
			elePaymentOut = prepareDummyCaptureResponse(docPaymentIn,elePaymentOut, strBankRespCode, 
					strBankMessage, strGatewayRespCode, strGatewayMessage, strCorelationId);
		}
		//Start : Fix to dummy the AUTH in case of void decline
		else if(!YFSObject.isVoid(strTransactionType) 
				&& strTransactionType.equalsIgnoreCase(AcademyConstants.TRANSACTION_TYPE_VOID)) {
			elePaymentOut = prepareDummyVoidResponse(docPaymentIn,elePaymentOut, strBankRespCode, 
					strBankMessage, strGatewayRespCode, strGatewayMessage, strCorelationId);
		}
		//End : Fix to dummy the AUTH in case of void decline
		//Start : KER-15797 : Ewallet: Refund timeout retry limit is happening at infinite times.
		else if(YFSObject.isVoid(strTransactionType) || 
				AcademyConstants.STR_CHRG_TYPE_CHARGE.equals(strChargeType)) {
			elePaymentOut = prepareDummyCaptureResponse(docPaymentIn,elePaymentOut, strBankRespCode, 
					strBankMessage, strGatewayRespCode, strGatewayMessage, strCorelationId);
		}
		//End : KER-15797 : Ewallet: Refund timeout retry limit is happening at infinite times.
		else {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_HARD_DECLINED);
			elePaymentOut.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_NO);
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE,strBankRespCode);
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, strBankMessage);	
			//Contains Gateway Response Code and Message
			elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE, strGatewayRespCode);
			elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_MESSAGE, strGatewayMessage);
			//Contains the ID for reference to reach out contact Payeezy for data against this request
			elePaymentOut.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_CODE, strCorelationId);
		}

		//Prepare taskQ record for hard declined cancellations
		//Start : PLCC-510 : Ignore Order Cancel in case of a void decline case
		log.verbose(" strCancelDeclinedAuth :: " + strCancelDeclinedAuth + " strCancelDeclinedCharge :: " + strCancelDeclinedCharge);
		if((!YFCObject.isVoid(strCancelDeclinedAuth) && strCancelDeclinedAuth.equals(AcademyConstants.STR_YES) 
				&& strChargeType.equals(AcademyConstants.STR_CHRG_TYPE_AUTH)
				&& !strTransactionType.equalsIgnoreCase(AcademyConstants.TRANSACTION_TYPE_VOID))
				|| (!YFCObject.isVoid(strCancelDeclinedCharge) && strCancelDeclinedCharge.equals(AcademyConstants.STR_YES) 
						&& strChargeType.equals(AcademyConstants.STR_CHRG_TYPE_CHARGE))){
			createManageTaskQueue(env, docPaymentIn);
		}
		//End : PLCC-510 : Ignore Order Cancel in case of a void decline case
		
		log.verbose("End of AcademyInvokePayeezyRestWebservice.prepareHardDeclinedResp() method");
		return elePaymentOut;
	}

	/**
	 * This method prepares the UE output document in case of SERVICE_UNAVAILABLE scenarios
	 * 
	 */
	private Element prepareServiceUnavailableResp(Document docPaymentIn, Element elePaymentOut, String strTransactionType, 
			String strBankRespCode, String strBankMessage,String strGatewayRespCode, String strGatewayMessage, 
			String strCorelationId, String strReversalId, int iErrorCount) throws Exception {

		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.prepareServiceUnavailableResp() method");

		if(iErrorCount >= iMaxRetryCount) {
			log.verbose(" Max Retry Limit Reached :: "+iMaxRetryCount+" <= "+iErrorCount);
			if(YFCObject.isVoid(strGatewayMessage)) {
				strGatewayMessage = AcademyConstants.STR_MAX_RETRY_LIMIT_REACHED;
			}
			prepareHardDeclinedResp(docPaymentIn, elePaymentOut, 
					strTransactionType, strBankRespCode, strBankMessage, strGatewayRespCode, strGatewayMessage, strCorelationId);
		} else {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
			elePaymentOut.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_YES);
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, strBankRespCode);
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, strBankMessage);
			elePaymentOut.setAttribute(AcademyConstants.ATTR_COLLECTION_DATE, 
					nextCollectionDate(props.getProperty(AcademyConstants.STR_NEXT_TRIGGER_IN_MIN)));

			//Contains Gateway Response Code and Message
			elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE, strGatewayRespCode);
			elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_MESSAGE, strGatewayMessage);
			//Contains the ID for reference to reach out contact Payeezy for data against this request
			elePaymentOut.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_CODE, strCorelationId);

			//Updating error message in the Payment Error List
			Element elePaymentErrorList = elePaymentOut.getOwnerDocument().createElement(AcademyConstants.ELE_PAYMENT_TRAN_ERROR_LIST);
			elePaymentOut.appendChild(elePaymentErrorList);

			Element elePaymentError = elePaymentOut.getOwnerDocument().createElement(AcademyConstants.ELE_PAYMENT_TRAN_ERROR);
			elePaymentErrorList.appendChild(elePaymentError);

			elePaymentError.setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
			elePaymentError.setAttribute(AcademyConstants.ATTR_MESSAGE, strReversalId);	
		}

		log.verbose("End of AcademyInvokePayeezyRestWebservice.prepareServiceUnavailableResp() method");
		return elePaymentOut;
	}

	/**
	 * This method prepares the UE output document in case of ignore Auth scenarios
	 * 
	 */
	private Element prepareDummyAuthResponse(Element elePaymentOut, String strBankRespCode, String strBankMessage,
			String strGatewayRespCode, String strGatewayMessage, String strCorelationId) throws Exception {

		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.prepareDummyAuthResponse() method");
		elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, strBankRespCode);
		//elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_CODE, strBankRespCode);//KER-14869 stamp TransactionId as AuthCode

		//Contains Bank Response Code and Message
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, strBankRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, strBankMessage);

		//Contains Gateway Response Code and Message
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE, strGatewayRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_MESSAGE, strGatewayMessage);

		//Contains the ID for reference to reach out contact Payeezy for data against this request
		elePaymentOut.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_CODE, strCorelationId);

		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN_NEW);
		Calendar cal = Calendar.getInstance();
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_TIME, sdf.format(cal.getTime()));

		log.verbose("End of AcademyInvokePayeezyRestWebservice.prepareDummyAuthResponse() method");
		return elePaymentOut;
	}

	/**
	 * This method prepares the UE output document wuth a dummy settlement incase of a purchase failure scenarios
	 * 
	 */
	private Element prepareDummyPurchaseResponse(Document docPaymentIn, Element elePaymentOut, String strBankRespCode, String strBankMessage,
			String strGatewayRespCode, String strGatewayMessage, String strCorelationId) throws Exception {

		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.prepareDummyPurchaseResponse() method");
		String strReqAmt = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);

		//Dummy settlement since Purchse had been declined
		elePaymentOut.setAttribute(AcademyConstants.ATTR_IS_DUMMY_SETTLEMENT, AcademyConstants.STR_YES);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_STATUS_CODE, AcademyConstants.STR_NO);//To raise alert using existing service
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, AcademyConstants.STR_DUMMY_SETTLEMENT);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_CODE, AcademyConstants.STR_DUMMY_SETTLEMENT);				
		elePaymentOut.setAttribute(AcademyConstants.ATTR_REQUEST_ID, AcademyConstants.STR_DUMMY_SETTLEMENT);				
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_AMT, strReqAmt);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_AMOUNT, strReqAmt);

		//elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, strBankRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_APPROVED);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, strBankRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, strBankMessage);

		//Contains Gateway Response Code and Message
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE, strGatewayRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_MESSAGE, strGatewayMessage);
		//Contains the ID for reference to reach out contact Payeezy for data against this request
		elePaymentOut.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_CODE, strCorelationId);

		Calendar cal = Calendar.getInstance();				
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN_NEW);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_TIME, sdf.format(cal.getTime()));

		//Reset PaymentReference9 is already set
		elePaymentOut.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_9, "");

		//Setting order details to raise alert
		elePaymentOut.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY, XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_HEADER_KEY));
		elePaymentOut.setAttribute(AcademyConstants.ATTR_ORDER_NO, XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_NO));
		elePaymentOut.setAttribute(AcademyConstants.ATTR_PAYMENT_TYPE, XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH + AcademyConstants.PAYMENT_TYPE_XPATH));
		//Below details will be shown in Alert console
		elePaymentOut.setAttribute(AcademyConstants.ATTR_REASON_CODE,strBankRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_REASON_DESC,strBankMessage);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_ERROR_NO, strGatewayRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_ERROR_DESC_SHORT, strGatewayMessage);
		log.verbose("End of AcademyInvokePayeezyRestWebservice.prepareDummyPurchaseResponse() method");
		return elePaymentOut;
	}


	/**
	 * This method prepares and updates TASK_Q table with orders which need to be cancelled
	 * 
	 */
	private void createManageTaskQueue(YFSEnvironment env, Document docPaymentInp) throws Exception {

		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.createManageTaskQueue() method");
		Document docTaskQueue = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
		Element eleTaskQueue = docTaskQueue.getDocumentElement();

		eleTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_KEY, 
				docPaymentInp.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));

		eleTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_TYPE, AcademyConstants.ATTR_ORDER_HEADER_KEY);
		eleTaskQueue.setAttribute(AcademyConstants.ATTR_TRANSID, AcademyConstants.TRANSACTION_DECLINED_ORDER_AGENT);

		Calendar cal = Calendar.getInstance();				
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
		eleTaskQueue.setAttribute(AcademyConstants.ATTR_AVAIL_DATE, sdf.format(cal.getTime()));
		//eleTaskQueue.setAttribute(AcademyConstants.ATTR_ACTION, "Create");

		log.verbose("Input to manageTaskQueue :: \n"+XMLUtil.getXMLString(docTaskQueue));
		AcademyUtil.invokeAPI(env,"manageTaskQueue", docTaskQueue);		
		log.verbose("End of AcademyInvokePayeezyRestWebservice.createManageTaskQueue() method");
	}



	/**
	 * This method invokes getOrderList and calculates valid cature for tagged Refunds
	 * 
	 */
	private Document processTaggedRefunds(YFSEnvironment env, Document docPaymentIn, String strEndpointURL, 
			Map<String, String> restParameters) throws Exception {
		Document docRespPayZ = null;
		Document docFailedResponse = null;

		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.processTaggedRefunds() method");
		Document docInput = XMLUtil.cloneDocument(docPaymentIn);
		HashMap<String, String> hmRefundedAmount = new HashMap<String, String>();
		HashMap<String, String> hmSettledAmount = new HashMap<String, String>();
		DecimalFormat df = new DecimalFormat("0.00"); 

		String strOrderHeaderKey = XPathUtil.getString(docPaymentIn, 
				AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.STR_ORDR_HDR_KEY);
		String strPaymentKey = XPathUtil.getString(docPaymentIn, 
				AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_PAYMENT_KEY);

		if(YFCObject.isVoid(docOrderList)) {
			docOrderList = getOrderList(env, strOrderHeaderKey);
		}

		hmRefundedAmount = getRefundedAmount(docOrderList);
		hmSettledAmount = getSettledAmounts(docOrderList, strPaymentKey);

		double dRefundAmount = 0.0;
		HashMap<String, String> hmRefundTransactionsDetails = retrieveValidSettledTransactions(docPaymentIn, hmSettledAmount, hmRefundedAmount);
		if(hmRefundTransactionsDetails.size() > 0) {
			Iterator<Map.Entry<String,String>> iter = hmRefundTransactionsDetails.entrySet().iterator();
			while (iter.hasNext()) {	
				Map.Entry<String,String> entry = iter.next();
				String strTransactionKey = entry.getKey();
				String strAmount = entry.getValue();

				String strRequestId = strTransactionKey.split(AcademyConstants.STR_UNDERSCORE )[0];
				String strAuthorizationId = strTransactionKey.split(AcademyConstants.STR_UNDERSCORE )[1];

				docInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT, strAmount);
				docInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, strAuthorizationId);

				//Retrieving Reversal ID to be re-used in timeout reversals.
				String strReversalId = AcademyPayeezyRestUtil.getAutoGeneratedUUID();
				restParameters.put(AcademyConstants.WEBSERVICE_REVERSAL_ID, strReversalId);

				//Get the PayZ REST resquest in JSON format 
				String strPayZInp =  AcademyPayeezyRestUtil.createPAYZRestInputForCCAuth(docInput, 
						AcademyConstants.TRANSACTION_TYPE_REFUND, null, restParameters);
				log.verbose("AcademyInvokePayeezyRestWebservice.invokePAYZHTTPCCService() JSON Input" + strPayZInp);

				String strNewEndpintURL = strEndpointURL + "/" + strRequestId;

				//Check if timeout reversal is required for the transaction
				validateTimeoutReversal(env, docPaymentIn, "TIMEOUT" +
						AcademyConstants.STR_HYPHEN + strRequestId, strEndpointURL, restParameters);


				//Call the Payeezy gateway service
				Document docPayZRefundResp = null;
				//OMNI-99459 - START
				if(AcademyConstants.STR_YES.equalsIgnoreCase(strInvokePaymentStub)){
					//Invoke payment stub method for refund
					docPayZRefundResp = AcademyPaymentStub.invokePayeezyPaymentStub(strPayZInp);
				}else{
					docPayZRefundResp = AcademyPayeezyRestUtil.invokePayeezyRestService(strPayZInp, strNewEndpintURL.trim(), restParameters);
				}
				//Document docPayZRefundResp = AcademyPayeezyRestUtil.invokePayeezyRestService(strPayZInp, strNewEndpintURL.trim(), restParameters);
				//OMNI-99459 - END
				if(!YFCObject.isVoid(docPayZRefundResp)) {
					docPayZRefundResp.getDocumentElement().setAttribute(AcademyConstants.JSON_ATTR_TRANSACTION_TYPE, 
							AcademyConstants.TRANSACTION_TYPE_REFUND);
				}
				String strHttpStatusCode = null;
				if(!YFCObject.isVoid(docPayZRefundResp)){
					strHttpStatusCode = docPayZRefundResp.getDocumentElement().getAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE);
					log.verbose(" strHttpStatusCode ::" + strHttpStatusCode);
				}

				if(!YFCObject.isVoid(strHttpStatusCode) && ("200".equals(strHttpStatusCode) 
						|| "201".equals(strHttpStatusCode) || "202".equals(strHttpStatusCode))) {
					docRespPayZ = XMLUtil.cloneDocument(docPayZRefundResp);
					log.verbose(" Valid response from Payeezy" + XMLUtil.getXMLString(docRespPayZ));
					dRefundAmount = dRefundAmount + Double.parseDouble(strAmount);
					docRespPayZ.getDocumentElement().setAttribute(AcademyConstants.JSON_ATTR_AMOUNT, df.format(dRefundAmount));
				}
				else {
					if(!YFCObject.isVoid(docPayZRefundResp)) {
						docFailedResponse = XMLUtil.cloneDocument(docPayZRefundResp);
						log.verbose(" Webservice call in Refund Failed " + XMLUtil.getXMLString(docPayZRefundResp));
					}
					else {
						log.verbose(" Null response recieved from webservice ");
					}
					//Updated the failed Reversal IDs in the Output doc.
					hmFailedReversalId.put(strTransactionKey + AcademyConstants.STR_UNDERSCORE + strAmount, docPayZRefundResp);
					iter.remove();
				}
			}
		}
		else {
			log.verbose(" No Valid Settlements available on Order. ");
		}
		//Update changeOrder API to update the webservice responses
		hmRefundTransactionsDetails = updateRefundedAmount(hmRefundedAmount, hmRefundTransactionsDetails);

		if(YFCObject.isVoid(docRespPayZ) && !YFCObject.isVoid(docFailedResponse)) {
			log.verbose(" complete transaction has failed. ");
			docRespPayZ = XMLUtil.cloneDocument(docFailedResponse);
		}

		prepareAndInvokeChangeOrder(hmRefundTransactionsDetails, strOrderHeaderKey);
		log.verbose("End of AcademyInvokePayeezyRestWebservice.processTaggedRefunds() method");
		return docRespPayZ;
	}

	/**
	 * This method invokes getOrderList and calculates valid cature for tagged Refunds
	 * 
	 */
	private HashMap<String, String> retrieveValidSettledTransactions(Document docPaymentIn, HashMap<String, String> hmSettledAmount, 
			HashMap<String, String> hmRefundedAmount) throws Exception {

		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.retrieveValidSettledTransactions() method");
		HashMap<String, String> hmValidTaggedRefunds = new HashMap<String, String>();
		DecimalFormat df = new DecimalFormat("0.00"); 

		String strRefundReqAmt = XPathUtil.getString(docPaymentIn, 
				AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);
		if(strRefundReqAmt.startsWith(AcademyConstants.STR_HYPHEN))
			strRefundReqAmt = strRefundReqAmt.split(AcademyConstants.STR_HYPHEN)[1];

		hmSettledAmount = getAvailableAmountsForRefund(hmRefundedAmount, hmSettledAmount);

		//Check if the Refund request amount has a best match in the settled amounts.
		if(hmSettledAmount.containsValue(strRefundReqAmt)){
			//Best match exisits. Use the same and refund.
			log.verbose(" strRefundReqAmt ::"+strRefundReqAmt);
			for (Map.Entry<String, String> entry : hmSettledAmount.entrySet()) {
				String strTransactionKey = entry.getKey();
				String strAmount = entry.getValue();
				if(strAmount.equals(strRefundReqAmt)) {
					hmValidTaggedRefunds.put(strTransactionKey, strAmount);
					break;
				}
			}
		}
		//No exact request. Check for best match
		else {
			hmSettledAmount = sortByDescending(hmSettledAmount);
			if(hmSettledAmount.size() >0) {
				double dRefundReqAmt = Double.parseDouble(strRefundReqAmt);
				Iterator<Map.Entry<String,String>> iter = hmSettledAmount.entrySet().iterator();

				while (iter.hasNext()) {	
					Map.Entry<String,String> entry = iter.next();
					String strTransactionKey = entry.getKey();
					String strAmount = entry.getValue();
					//Check if the amount on this entry is suffuicent for refund
					double dAmount = Double.parseDouble(strAmount);
					log.verbose(" dAmount ::"+dAmount+" :: dRefundReqAmt ::"+dRefundReqAmt);
					if(dRefundReqAmt > 0) {
						if(dRefundReqAmt > dAmount ) {
							log.verbose(" Request amount is partailly settled."+strTransactionKey);
							hmValidTaggedRefunds.put(strTransactionKey, strAmount);
						}
						else {
							log.verbose(" Request amount is settled."+strTransactionKey);
							hmValidTaggedRefunds.put(strTransactionKey, df.format(dRefundReqAmt));
						}
						dRefundReqAmt = dRefundReqAmt - dAmount;
					}
					else {
						break;
					}
				}
			}
		}

		log.verbose(" hmValidTaggedRefunds ::"+hmValidTaggedRefunds.size() +" \n "+hmValidTaggedRefunds.toString());
		log.verbose("End of AcademyInvokePayeezyRestWebservice.retrieveValidSettledTransactions() method");
		return hmValidTaggedRefunds;
	}

	/**
	 * This method gets the amounts refunded on the order
	 * 
	 */
	private HashMap<String, String> getRefundedAmount(Document docGetOrderListOut) throws Exception {

		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.getRefundedAmount() method");
		HashMap<String, String> hmRefundedAmount = new HashMap<String, String>();
		DecimalFormat df = new DecimalFormat("0.00"); 

		NodeList nlReferences = XPathUtil.getNodeList(docGetOrderListOut.getDocumentElement(), 
		"/OrderList/Order/References/Reference");


		for(int iRef=0; iRef < nlReferences.getLength(); iRef++) {
			Element eleReference = (Element) nlReferences.item(iRef);
			String strName = eleReference.getAttribute("Name");
			if(strName.startsWith(AcademyConstants.TRANSACTION_TYPE_REFUND)){
				String strValue = eleReference.getAttribute("Value");
				String strReqId = strName.split(AcademyConstants.STR_UNDERSCORE )[1];
				String strAuthID = strName.split(AcademyConstants.STR_UNDERSCORE )[2];
				String strAmount = strValue;
				hmRefundedAmount.put(strReqId + AcademyConstants.STR_UNDERSCORE  + strAuthID, df.format(Double.parseDouble(strAmount)));	
			}
		}
		log.verbose("End of AcademyInvokePayeezyRestWebservice.getRefundedAmount() method");
		return hmRefundedAmount;
	}


	/**
	 * This method gets the settlement details 
	 * 
	 */
	private HashMap<String, String> getSettledAmounts(Document docGetOrderListOut, String strPaymentKey) throws Exception {

		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.getSettledAmounts() method");
		HashMap<String, String> hmSettledAmount = new HashMap<String, String>();
		DecimalFormat df = new DecimalFormat("0.00"); 

		NodeList nlSettledCharges = XPathUtil.getNodeList(docGetOrderListOut.getDocumentElement(), 
				"/OrderList/Order/ChargeTransactionDetails/ChargeTransactionDetail[@PaymentKey='"+ 
				strPaymentKey + "' and @ChargeType='CHARGE']");

		for(int iStlCrgs=0; iStlCrgs < nlSettledCharges.getLength(); iStlCrgs++) {
			Element eleChargeTranDetail = (Element) nlSettledCharges.item(iStlCrgs);
			String strRequestAmount = eleChargeTranDetail.getAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT);
			String strAuthId = null;
			String strRequestId = null;

			Element eleCreditCardTransactions = (Element) eleChargeTranDetail.getElementsByTagName("CreditCardTransactions").item(0);

			NodeList nlCreditCardTransaction = eleCreditCardTransactions.getElementsByTagName("CreditCardTransaction");

			for(int iCCtrn=0; iCCtrn < nlCreditCardTransaction.getLength(); iCCtrn++) {
				Element eleCreditCardTrans = (Element) nlCreditCardTransaction.item(iCCtrn);
				if(!YFCObject.isVoid(eleCreditCardTrans.getAttribute("RequestId")) 
						&& !YFCObject.isVoid(eleCreditCardTrans.getAttribute("AuthReturnCode"))){
					strRequestId = eleCreditCardTrans.getAttribute("RequestId");
					strAuthId = eleCreditCardTrans.getAttribute("AuthReturnCode");
					break;
				}

			}

			if(!YFCObject.isVoid(strRequestId) && !YFCObject.isVoid(strAuthId) 
					&& !strRequestAmount.contains(AcademyConstants.STR_HYPHEN)){
				hmSettledAmount.put(strRequestId + AcademyConstants.STR_UNDERSCORE  + strAuthId, df.format(Double.parseDouble(strRequestAmount)));
			} else {
				log.verbose(" Not valid Charge Settlement details. ::"+strRequestId+"::"+strAuthId+"::"+strRequestAmount);
			}
		}

		log.verbose(" hmSettledAmount ::"+hmSettledAmount.size() +" \n "+hmSettledAmount.toString());
		log.verbose("End of AcademyInvokePayeezyRestWebservice.getSettledAmounts() method");
		return hmSettledAmount;
	}
	/**
	 * This method gets the amounts refunded on the order
	 * 
	 */
	private HashMap<String, String> getAvailableAmountsForRefund(HashMap<String, String> hmRefundedAMount, 
			HashMap<String, String> hmSettledAmount) throws Exception {
		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.getAvailableAmountsForRefund() method");
		DecimalFormat df = new DecimalFormat("0.00"); 

		if(hmRefundedAMount.size() >0) {
			Iterator<Map.Entry<String,String>> iter = hmSettledAmount.entrySet().iterator();
			while (iter.hasNext()) {	
				Map.Entry<String,String> entry = iter.next();
				String strTransactionKey = entry.getKey();
				String strAmount = entry.getValue();
				if(hmRefundedAMount.containsKey(strTransactionKey)){
					String strRefundAmount = hmRefundedAMount.get(strTransactionKey);
					String strNewTotal = df.format(Double.parseDouble(strAmount) - Double.parseDouble(strRefundAmount));
					if(Double.parseDouble(strNewTotal) > 0) {
						hmSettledAmount.put(strTransactionKey, strNewTotal);
					}
					else {
						iter.remove();								
					}
				}
			}
		}
		log.verbose(" hmSettledAmount ::"+hmSettledAmount.size() +" \n "+hmSettledAmount.toString());
		log.verbose("End of AcademyInvokePayeezyRestWebservice.getAvailableAmountsForRefund() method");
		return hmSettledAmount;
	}

	private static HashMap<String, String> sortByDescending(HashMap<String, String> unsortMap) {
		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.sortByDescending() method");
		log.verbose(" unsortMap ::"+unsortMap.size() +" \n "+unsortMap.toString());

		List<Entry<String, String>> list = new LinkedList<Entry<String, String>>(unsortMap.entrySet());

		// Sorting the list based on values
		Collections.sort(list, new Comparator<Entry<String, String>>() {
			public int compare(Entry<String, String> o1,
					Entry<String, String> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});

		// Maintaining insertion order with the help of LinkedList
		HashMap<String, String> sortedMap = new LinkedHashMap<String, String>();
		for (Entry<String, String> entry : list)
		{
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		log.verbose(" sortedMap ::"+sortedMap.size() +" \n "+sortedMap.toString());
		log.verbose("End of AcademyInvokePayeezyRestWebservice.sortByDescending() method");
		return sortedMap;
	}


	/**
	 * This method gets the amounts refunded on the order
	 * 
	 */
	private HashMap<String, String> updateRefundedAmount(HashMap<String, String> hmRefundedAmount, 
			HashMap<String, String> hmRefundTransactionsDetails) throws Exception {
		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.updateRefundedAmount() method");
		DecimalFormat df = new DecimalFormat("0.00"); 

		if(hmRefundTransactionsDetails.size() >0) {
			Iterator<Map.Entry<String,String>> iter = hmRefundTransactionsDetails.entrySet().iterator();
			while (iter.hasNext()) {	
				Map.Entry<String,String> entry = iter.next();
				String strTransactionKey = entry.getKey();
				String strAmount = entry.getValue();
				if(hmRefundedAmount.containsKey(strTransactionKey)){
					String strRefundAmount = hmRefundedAmount.get(strTransactionKey);
					String strNewTotal = df.format(Double.parseDouble(strAmount) + Double.parseDouble(strRefundAmount));
					hmRefundTransactionsDetails.put(strTransactionKey, strNewTotal);					
				}

			}
		}
		log.verbose(" hmRefundTransactionsDetails ::"+hmRefundTransactionsDetails.size() +" \n "+hmRefundTransactionsDetails.toString());
		log.verbose("End of AcademyInvokePayeezyRestWebservice.updateRefundedAmount() method");
		return hmRefundTransactionsDetails;
	}

	/**
	 * This method prepares and invokes changeOrder to update refunded details on order ereferences
	 * 
	 */
	private void prepareAndInvokeChangeOrder(HashMap<String, String> hmRefundTransactionsDetails, String strOrderHeaderKey) throws Exception {
		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.prepareAndInvokeChangeOrder() method");

		Document docChangeOrder = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		Element eleOrder = docChangeOrder.getDocumentElement();
		eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
		eleOrder.setAttribute(AcademyConstants.ATTR_OVERRIDE, AcademyConstants.STR_YES);
		Element eleReferences = docChangeOrder.createElement("References");
		eleOrder.appendChild(eleReferences);

		if(hmRefundTransactionsDetails.size() >0) {
			Iterator<Map.Entry<String,String>> iter = hmRefundTransactionsDetails.entrySet().iterator();
			while (iter.hasNext()) {	
				Map.Entry<String,String> entry = iter.next();
				String strTransactionKey = entry.getKey();
				String strAmount = entry.getValue();

				Element eleReference = docChangeOrder.createElement("Reference");
				eleReference.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.TRANSACTION_TYPE_REFUND 
						+ AcademyConstants.STR_UNDERSCORE  + strTransactionKey);
				eleReference.setAttribute(AcademyConstants.ATTR_VALUE, strAmount);
				eleReferences.appendChild(eleReference);
			}
			log.verbose("Input to changeOrder :: "+XMLUtil.getXMLString(docChangeOrder));
			AcademyUtil.invokeAPI(env,AcademyConstants.API_CHANGE_ORDER, docChangeOrder);
		}
		else {
			log.verbose(" No references to be updated. So skipping the changeOrder API call ");
		}


		log.verbose("End of AcademyInvokePayeezyRestWebservice.prepareAndInvokeChangeOrder() method");
	}


	/**This method parses the PayZ AUTH response to Payment output XML 
	 * @param docPayIn
	 * @param docPayZAuthOut
	 * @return
	 * @throws Exception
	 */
	private Document translatePayPalResp(Document docPayIn,Document docPayPalAuthOut, String strTransactionType) throws Exception {
		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.translatePayPalResp() method::");

		Document docPaymentOut = XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT);
		Element elePaymentOut = docPaymentOut.getDocumentElement();
		Element elePayPalResp = docPayPalAuthOut.getDocumentElement();

		//String strId = elePayPalResp.getAttribute(AcademyConstants.PAYPAL_RESPONSE_ID);
		String strHttpStatusCode = elePayPalResp.getAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE);
		String strPaypalDebugId = elePayPalResp.getAttribute(AcademyConstants.ATTR_PAYPAL_DEBUG_ID);		
		String strMessage = elePayPalResp.getAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_MESSAGE);
		String strName = elePayPalResp.getAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_NAME);
		log.verbose("strName:  " + strName);						

		if(listPayPalRetryErrorName.contains(strName)){
			log.verbose("Retrying Autorization due to PayPal response:  " + strName);
			log.verbose("AcademyConstants.STR_NEXT_TRIGGER_IN_MIN_AUTH_ERROR:  " + AcademyConstants.STR_NEXT_TRIGGER_IN_MIN_AUTH_ERROR);
			log.verbose("value for AcademyConstants.STR_NEXT_TRIGGER_IN_MIN_AUTH_ERROR:  " + props.getProperty(AcademyConstants.STR_NEXT_TRIGGER_IN_MIN_AUTH_ERROR));

			//Invoke a void call since no amount is available for AUTH.
			Document docPaypalVoid = invokePayPalVoidTransaction(docPayIn, strEndpointURL, restParameters);
			//This error will come when PayPal has active auth and we are trying to get a new auth.
			elePaymentOut = retryTransaction(docPaymentOut, props.getProperty(AcademyConstants.STR_NEXT_TRIGGER_IN_MIN_AUTH_ERROR));
			
		}else{
			elePaymentOut = prepareHardDeclinedResp(docPayIn, elePaymentOut, strTransactionType, strHttpStatusCode, strMessage, strHttpStatusCode, strMessage, strPaypalDebugId);
		}
		log.verbose("docPaymentOut: " + XMLUtil.getXMLString(docPaymentOut));
		log.verbose("End of AcademyInvokePayeezyRestWebservice.translatePayPalResp() method");
		return docPaymentOut;
	}

	private Document getLatestExistingAuth(YFSEnvironment env, Document docPaymentIn) {
		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.getLatestExistingAuth() method");
		log.verbose("docPaymentIn:\n" + XMLUtil.getXMLString(docPaymentIn));
		Element eleChargeTransactionDetail= null;
		String strRequestId = "";
		String strAuthorizationID = "";
		String strPaypalAuthId = "";
		
		String strOrderHeaderKey = docPaymentIn.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);

		try {			
			if(YFCObject.isVoid(docOrderList)) {
				docOrderList = getOrderList(env, strOrderHeaderKey);
			}
			if(!YFSObject.isVoid(docOrderList)){	

				log.verbose("getOrderList output:: \n" + XMLUtil.getXMLString(docOrderList));
				NodeList nCreatedRetOrderLines = XPathUtil.getNodeList(docOrderList, 
				"OrderList/Order/ChargeTransactionDetails/ChargeTransactionDetail[@ChargeType='AUTHORIZATION' and @RequestAmount>0 and @AuthorizationID!='']");
				int iChargeTransactionDetailCount = nCreatedRetOrderLines.getLength();

				log.verbose("iChargeTransactionDetailCount:  " +iChargeTransactionDetailCount);
				for(int iCount = 0; iCount<iChargeTransactionDetailCount;iCount++ ){					
					eleChargeTransactionDetail= (Element) nCreatedRetOrderLines.item(iCount);
					strAuthorizationID = eleChargeTransactionDetail.getAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID_CAP);
					strRequestId = XPathUtil.getString(eleChargeTransactionDetail, AcademyConstants.STR_PAYMENT_PATH_REQUEST_ID);
					strPaypalAuthId = XPathUtil.getString(eleChargeTransactionDetail, AcademyConstants.PAYMENT_PATH_FOR_AUTH_CODE);
				}

				log.verbose("strAuthorizationID:  " +strAuthorizationID);
				log.verbose("strRequestId:  " +strRequestId);
				log.verbose("strPaypalAuthId:  " +strPaypalAuthId);
				
				docPaymentIn.getDocumentElement().setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, strAuthorizationID);
				if(!YFSObject.isVoid(strRequestId))
					docPaymentIn.getDocumentElement().setAttribute(AcademyConstants.ATTR_REQUEST_ID_SMALL, strRequestId);
				
				if(!YFSObject.isVoid(strPaypalAuthId))
					docPaymentIn.getDocumentElement().setAttribute(AcademyConstants.PAYPAL_RESPONSE_ID, strPaypalAuthId);
			}
		} catch (Exception e) {
			log.error("Exception while making getOrderList call: " );
			e.printStackTrace();
		}
		log.verbose("End of AcademyInvokePayeezyRestWebservice.getLatestExistingAuth() method");
		return docPaymentIn;
	}

	/**
	 * This method prepares the UE output document wuth a dummy settlement incase of a purchase failure scenarios
	 * 
	 */
	private Element prepareDummyCaptureResponse(Document docPaymentIn, Element elePaymentOut, String strBankRespCode, String strBankMessage,
			String strGatewayRespCode, String strGatewayMessage, String strCorelationId) throws Exception {
		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.prepareDummyCaptureResponse() method");
		String strReqAmt = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);
		String strTranType = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CHARGE_TYPE);
		//Dummy settlement since capture had been declined
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_TYPE, strTranType);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_IS_DUMMY_SETTLEMENT, AcademyConstants.STR_YES);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_STATUS_CODE, AcademyConstants.STR_NO);//To raise alert using existing service
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, AcademyConstants.STR_DUMMY_SETTLEMENT);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_CODE, AcademyConstants.STR_DUMMY_SETTLEMENT);				
		elePaymentOut.setAttribute(AcademyConstants.ATTR_REQUEST_ID, AcademyConstants.STR_DUMMY_SETTLEMENT);				
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_AMT, strReqAmt);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_AMOUNT, strReqAmt);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_APPROVED);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, strBankRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, strBankMessage);
		//Contains Gateway Response Code and Message
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE, strGatewayRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_MESSAGE, strGatewayMessage);
		//Contains the ID for reference to reach out contact Payeezy for data against this request
		elePaymentOut.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_CODE, strCorelationId);
		Calendar cal = Calendar.getInstance();				
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN_NEW);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_TIME, sdf.format(cal.getTime()));
		//Setting order details to raise alert
		elePaymentOut.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY, XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_HEADER_KEY));
		elePaymentOut.setAttribute(AcademyConstants.ATTR_ORDER_NO, XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_NO));
		elePaymentOut.setAttribute(AcademyConstants.ATTR_PAYMENT_TYPE, XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH + AcademyConstants.PAYMENT_TYPE_XPATH));
		elePaymentOut.setAttribute(AcademyConstants.ATTR_REASON_CODE,strBankRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_REASON_DESC,strBankMessage);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_ERROR_NO, strGatewayRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_ERROR_DESC_SHORT, strGatewayMessage);
		log.verbose("End of AcademyInvokePayeezyRestWebservice.prepareDummyCaptureResponse() method");
		return elePaymentOut;
	}

	/**
	 * This method is used to get the List of Error Transaction for the Payment Type of the Order.
	 * 
	 * @param env
	 * @param strInternalMessage
	 * @return iErrorCount 
	 */
	private int getCountOfPaymentTransactionError(YFSEnvironment env, String strOrderHeaderKey, String strChargeTransactionKey) {
		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.getCountOfPaymentTransactionError() method");
		int iErrorCount = 0;

		try {
			if(YFCObject.isVoid(docOrderList)) {
				docOrderList = getOrderList(env, strOrderHeaderKey);
			}

			List lstPaymentTransactionError = XMLUtil.getElementListByXpath(docOrderList,
					"OrderList/Order/ChargeTransactionDetails/ChargeTransactionDetail[@ChargeTransactionKey='"
					+ strChargeTransactionKey + "']/PaymentTransactionErrorList/PaymentTransactionError");


			iErrorCount = lstPaymentTransactionError.size();
		} catch (Exception e) {
			log.debug("Exception while retrieving count of Error Transactions" + e.getMessage());
			return iErrorCount;
		} 
		log.debug("iErrorCount:: "+iErrorCount);
		log.verbose("End of AcademyInvokePayeezyRestWebservice.getCountOfPaymentTransactionError() method");
		return iErrorCount;
	}


	/**
	 * This method is to validate timeout Reversal .
	 * 
	 * @param env
	 * @param strInternalMessage
	 * @return iErrorCount 
	 */
	private void validateTimeoutReversal(YFSEnvironment env, Document docPaymentIn, String strMessageType, 
			String strEndpointURL, Map<String, String> restParameters) throws Exception {
		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.validateTimeoutReversal() method");
		String strOrderHeaderKey =  XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_HEADER_KEY);
		String strChargeTransactionKey =  XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY);

		String strReversalId = getReversalIdOfTimeOutIfExists(env, strOrderHeaderKey, strChargeTransactionKey, strMessageType);

		if(!YFCObject.isVoid(strReversalId)) {
			log.verbose(" Auth Reversal Required ");
			String strAmount = null;

			if(strReversalId.contains(AcademyConstants.STR_UNDERSCORE)){
				strAmount = strReversalId.split(AcademyConstants.STR_UNDERSCORE)[1];
			}

			String strPayZtimeoutReversalInp = AcademyPayeezyRestUtil
			.createPAYZRestInputForTimeoutReversals(docPaymentIn, strAmount, strReversalId);

			log.verbose("AcademyInvokePayeezyRestWebservice.strPayZtimeoutReversalInp() JSON Input" + strPayZtimeoutReversalInp);

			//Call the Payeezy gateway service
			Document docRespPayZ = AcademyPayeezyRestUtil.invokePayeezyRestService(strPayZtimeoutReversalInp, strEndpointURL, restParameters);			
			log.verbose("Reversal Response " + XMLUtil.getXMLString(docRespPayZ));
		}
		else {
			log.verbose(" Auth Reversal Not Required ");
		}
		log.verbose("End of AcademyInvokePayeezyRestWebservice.validateTimeoutReversal() method");
	}


	/**
	 * This method is used to get the ReversalID of the last timed out error .
	 * 
	 * @param env
	 * @param strInternalMessage
	 * @return iErrorCount 
	 */
	private String getReversalIdOfTimeOutIfExists(YFSEnvironment env, String strOrderHeaderKey, 
			String strChargeTransactionKey, String strMessageType) {
		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.getReversalIdOfTimeOutIfExists() method");
		String strReversalId = null;
		List lstPaymentTransactionError = null;
		try {
			if(YFCObject.isVoid(docOrderList)) {
				docOrderList = getOrderList(env, strOrderHeaderKey);
			}
			if(!YFCObject.isVoid(strChargeTransactionKey)) {
				lstPaymentTransactionError = XMLUtil.getElementListByXpath(docOrderList,
						"OrderList/Order/ChargeTransactionDetails/ChargeTransactionDetail[@ChargeTransactionKey='"
						+ strChargeTransactionKey + "']/PaymentTransactionErrorList/PaymentTransactionError[" +
						"@MessageType='" +strMessageType+ "']");
			}
			else {
				lstPaymentTransactionError = XMLUtil.getElementListByXpath(docOrderList,
						"OrderList/Order/ChargeTransactionDetails/ChargeTransactionDetail/PaymentTransactionErrorList" +
						"/PaymentTransactionError[@MessageType='" +strMessageType+ "']");
			}

			if(!YFCObject.isVoid(lstPaymentTransactionError) && lstPaymentTransactionError.size() > 0) {
				for(int iPErr=0; iPErr < lstPaymentTransactionError.size(); iPErr++){
					Element elePaymentTransactionError = (Element) lstPaymentTransactionError.get(iPErr);
					strReversalId = elePaymentTransactionError.getAttribute(AcademyConstants.ATTR_MESSAGE);
				}
			}

		} catch (Exception e) {
			log.debug("Exception while retrieving count of Error Transactions" + e.getMessage());
		} 
		log.debug("strReversalId:: "+strReversalId);
		log.verbose("End of AcademyInvokePayeezyRestWebservice.getReversalIdOfTimeOutIfExists() method");
		return strReversalId;
	}

	/**
	 * This method is used to updated the web service errors incase of Tagged refunds .
	 * 
	 * @param docPaymentIn
	 * @param docPaymentOut
	 * @return docPaymentOut 
	 */
	private Document updateFailedReversalsForRefund(Document docPaymentIn, Document docPaymentOut) throws Exception {
		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.updateFailedReversalsForRefund() method");
		Element elePaymentOut = docPaymentOut.getDocumentElement();
		Element elePaymentErrorList = (Element) elePaymentOut.getElementsByTagName(AcademyConstants.ELE_PAYMENT_TRAN_ERROR_LIST).item(0);

		//Remove any error handling done
		if(!YFCObject.isVoid(elePaymentErrorList)) {
			elePaymentOut.removeChild(elePaymentErrorList);
			elePaymentErrorList = docPaymentOut.createElement(AcademyConstants.ELE_PAYMENT_TRAN_ERROR_LIST);
			elePaymentOut.appendChild(elePaymentErrorList);
		}
		else {
			elePaymentErrorList = docPaymentOut.createElement(AcademyConstants.ELE_PAYMENT_TRAN_ERROR_LIST);
			elePaymentOut.appendChild(elePaymentErrorList);
		}

		Iterator<Map.Entry<String,Document>> iter = hmFailedReversalId.entrySet().iterator();
		while (iter.hasNext()) {	
			Map.Entry<String,Document> entry = iter.next();
			String strReqID_AuthId_Amount = entry.getKey();
			Document docPayZOutput = entry.getValue();
			if(!YFCObject.isNull(docPayZOutput)) {
				String strReversalId = docPayZOutput.getDocumentElement().getAttribute(AcademyConstants.JSON_ATTR_REVERSAL_ID);
				Document docFailedPaymentOut = translatePayZResp(docPaymentIn,docPayZOutput);

				String strResponse = docFailedPaymentOut.getDocumentElement().getAttribute(AcademyConstants.ATTR_RESPONSE_CODE);
				String strAuthorizationId = docFailedPaymentOut.getDocumentElement().getAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID);

				if(!AcademyConstants.STR_APPROVED.equals(strResponse) ||
						AcademyConstants.STR_DUMMY_SETTLEMENT.equals(strAuthorizationId) ) {
					log.verbose(" Error Update final Payment Output doc ");

					String strRequestId = strReqID_AuthId_Amount.split(AcademyConstants.STR_UNDERSCORE)[0];
					String strAuthId = strReqID_AuthId_Amount.split(AcademyConstants.STR_UNDERSCORE)[1];
					String strAmount = strReqID_AuthId_Amount.split(AcademyConstants.STR_UNDERSCORE)[2];

					Element elePaymentError = elePaymentOut.getOwnerDocument().createElement(AcademyConstants.ELE_PAYMENT_TRAN_ERROR);
					elePaymentErrorList.appendChild(elePaymentError);
					
					if(AcademyConstants.STR_SERVICE_UNAVAILABLE.equalsIgnoreCase(strResponse)) {
						elePaymentError.setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, "TIMEOUT" + AcademyConstants.STR_HYPHEN + strRequestId);
					}
					else if(AcademyConstants.STR_HARD_DECLINED.equalsIgnoreCase(strResponse)){
						elePaymentError.setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, "DECLINED" + AcademyConstants.STR_HYPHEN + strRequestId);
					}
					else {
						elePaymentError.setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, "ERROR" + AcademyConstants.STR_HYPHEN + strRequestId);
					}
						
					elePaymentError.setAttribute(AcademyConstants.ATTR_MESSAGE, strReversalId + AcademyConstants.STR_UNDERSCORE + strAmount);	
				}
				else {
					log.verbose(" Showing as approved which is not a valid scenario "+XMLUtil.getXMLString(docPayZOutput));
				}
			}
			else {
				log.verbose(" Null document output. Not a valid scenario");
			}
		}
		log.verbose("End of AcademyInvokePayeezyRestWebservice.updateFailedReversalsForRefund() method");
		return docPaymentOut;
	}



	/**
	 * This method is used to invoe paypal void in case a re-auth fails in paypal
	 * 
	 * @param docPaymentIn
	 * @param strEndpointURL
	 * @param restParameters
	 * @return docPaymentOut 
	 */
	private Document invokePayPalVoidTransaction(Document docPaymentIn, String strEndpointURL, Map<String, String> restParameters) {
		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.invokePayPalVoidTransaction() method");

		Document docPayalVoidRes = null;
		try {
			//RequestId is not available as part of input xml
			docPaymentIn = getLatestExistingAuth(env,docPaymentIn);
			//Updated the Paypal VOID URL.
			restParameters.put(AcademyConstants.WEBSERVICE_PAYPAL_URL_AUTH, strPayPalUrlVoid);
			
			//Invoke PayPal system to do a auth void call
			docPayalVoidRes = AcademyPayPalRestUtil.postPaypalVoid(env, docPaymentIn,restParameters);
			String strPayPalState = docPayalVoidRes.getDocumentElement().getAttribute(AcademyConstants.ATTR_PAYPAL_RESPONSE_STATE);

		} catch (Exception e) {
			log.verbose(" Exception while invoking the void web service call for Paypal");
		}

		log.verbose("End of AcademyInvokePayeezyRestWebservice.invokePayPalVoidTransaction() method");
		return docPayalVoidRes;
	}
	
	
	/**
	 * This method is used check if the AuthId has any valid Settlement against it.
	 * 
	 * @param docPaymentIn
	 * @return hasValidChargeAgainstAuthID 
	 */
	private boolean validateSettlementCTRs(Document docPaymentIn) {
		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.validateSettlementCTRs() method");

		boolean hasValidChargeAgainstAuthID =false;
		try {
			String strAuthId = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_AUTHORIZATION_ID);
			String strOrderHeaderKey =  XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_HEADER_KEY);
			
			if(!YFCObject.isVoid(strAuthId) && !YFCObject.isVoid(strOrderHeaderKey)){
				log.verbose(" Validate the CHARGE based on AuthID :: "+strAuthId);
				if(YFCObject.isVoid(docOrderList)) {
					docOrderList = getOrderList(env, strOrderHeaderKey);
				}
				
				NodeList nlValidChargeCTRs = XPathUtil.getNodeList(docOrderList.getDocumentElement(), 
				"/OrderList/Order/ChargeTransactionDetails/ChargeTransactionDetail[@AuthorizationID='"+ 
				strAuthId + "' and @ChargeType='CHARGE']");
				
				log.verbose(" nlValidChargeCTRs :: "+nlValidChargeCTRs.getLength());
				
				if(nlValidChargeCTRs.getLength() > 0) {
					return true;
				}
			}
		} catch (Exception e) {
			log.verbose(" Exception while validating the Valid Settlement on auth");
		}

		log.verbose("End of AcademyInvokePayeezyRestWebservice.validateSettlementCTRs() method");
		return hasValidChargeAgainstAuthID;
	}

	
	/**
	 * This method is added as part of KER-15764: 
	 * Void call not sent after cancelling an entire order through multiple cancellations
	 * 
	 * This method retrievies Original AUTH Amount for eWallets.
	 * 
	 * @param docPaymentIn
	 * @return hasValidChargeAgainstAuthID 
	 */
	private String retrieveOriginalAuthAmount(Document docPaymentIn) {
		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.validateSettlementCTRs() method");

		String strOrigAuthAmount = null;

		try {
			String strAuthId = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_AUTHORIZATION_ID);
			String strOrderHeaderKey =  XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_HEADER_KEY);
			
			if(!YFCObject.isVoid(strAuthId) && !YFCObject.isVoid(strOrderHeaderKey)){
				log.verbose(" Retrieve oruginal Auth amount for AUTHID :: "+strAuthId);
				if(YFCObject.isVoid(docOrderList)) {
					docOrderList = getOrderList(env, strOrderHeaderKey);
				}
				
				Element eleOriginalAuth = (Element) XPathUtil.getNodeList(docOrderList.getDocumentElement(), 
				"/OrderList/Order/ChargeTransactionDetails/ChargeTransactionDetail[@AuthorizationID='"+ 
				strAuthId + "' and @ChargeType='AUTHORIZATION' and @RequestAmount > 0]").item(0);
				
				if(!YFCObject.isVoid(eleOriginalAuth)) {
					strOrigAuthAmount = eleOriginalAuth.getAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT);
				}
			}
		} catch (Exception e) {
			log.verbose(" Exception while validating the Valid AUTH for Order");
		}

		log.verbose("End of AcademyInvokePayeezyRestWebservice.validateSettlementCTRs() method");
		return strOrigAuthAmount;
	}


	/**
	 * This method prepares the UE output document wuth a dummy Auth incase of a purchase failure scenarios
	 * 
	 */
	private Element prepareDummyVoidResponse(Document docPaymentIn, Element elePaymentOut, String strBankRespCode, String strBankMessage,
			String strGatewayRespCode, String strGatewayMessage, String strCorelationId) throws Exception {
		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.prepareDummyVoidResponse() method");
		String strReqAmt = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);
		String strTranType = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CHARGE_TYPE);
		//Dummy Auth since void had been declined
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_TYPE, strTranType);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_IS_DUMMY_SETTLEMENT, AcademyConstants.STR_YES);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_STATUS_CODE, AcademyConstants.STR_NO);//To raise alert using existing service
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, AcademyConstants.STR_DUMMY_AUTH);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_CODE, AcademyConstants.STR_DUMMY_AUTH);				
		elePaymentOut.setAttribute(AcademyConstants.ATTR_REQUEST_ID, AcademyConstants.STR_DUMMY_AUTH);				
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_AMT, strReqAmt);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_AMOUNT, strReqAmt);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_APPROVED);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, strBankRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, strBankMessage);
		//Contains Gateway Response Code and Message
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE, strGatewayRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_MESSAGE, strGatewayMessage);
		//Contains the ID for reference to reach out contact Payeezy for data against this request
		elePaymentOut.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_CODE, strCorelationId);
		Calendar cal = Calendar.getInstance();				
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN_NEW);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_TIME, sdf.format(cal.getTime()));
		//Setting order details to raise alert
		elePaymentOut.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY, XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_HEADER_KEY));
		elePaymentOut.setAttribute(AcademyConstants.ATTR_ORDER_NO, XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_NO));
		elePaymentOut.setAttribute(AcademyConstants.ATTR_PAYMENT_TYPE, XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH + AcademyConstants.PAYMENT_TYPE_XPATH));
		elePaymentOut.setAttribute(AcademyConstants.ATTR_REASON_CODE,strBankRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_REASON_DESC,strBankMessage);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_ERROR_NO, strGatewayRespCode);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_ERROR_DESC_SHORT, strGatewayMessage);
		log.verbose("End of AcademyInvokePayeezyRestWebservice.prepareDummyVoidResponse() method");
		return elePaymentOut;
	}


	/**
	 * This method is added as part of KER-15779 : 
	 * Prod Issue PayPal (Payment): Multiple ReAuth and Voids calls triggered from OMS
	 * This method retrievies the PayPal AUTH details if availble
	 * 
	 * @param env
	 * @param strOrderHeaderKey
	 * @param strChargeTransactionKey
	 * 
	 * @return strPayPalAuth 
	 */
	private String retrievePaypalAuthDetails(YFSEnvironment env, Document docPaymentInp) throws Exception {
		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.retrievePaypalAuthDetails() method");

		String strPayPalAuth = null;
		List lstPaymentTransactionError = null;
		String strOrderHeaderKey = docPaymentInp.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
		String strChargeTransactionKey = docPaymentInp.getDocumentElement().getAttribute(AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY);

		try {
			if(YFCObject.isVoid(docOrderList)) {
				docOrderList = getOrderList(env, strOrderHeaderKey);
			}
			lstPaymentTransactionError = XMLUtil.getElementListByXpath(docOrderList,
					"OrderList/Order/ChargeTransactionDetails/ChargeTransactionDetail[@ChargeTransactionKey='"
					+ strChargeTransactionKey + "']/PaymentTransactionErrorList/PaymentTransactionError[" +
					"@MessageType='" + AcademyConstants.STR_PAYPAL_AUTH + "']");


			if(lstPaymentTransactionError.size() > 0) {
				Element elePaymentTransactionError = (Element) lstPaymentTransactionError.get(0);
				strPayPalAuth = elePaymentTransactionError.getAttribute(AcademyConstants.ATTR_MESSAGE);
			}

		} catch (Exception e) {
			log.debug("Exception while retrieving count of Error Transactions" + e.getMessage());
		} 

		log.verbose(" :: strPayPalAuth :: " + strPayPalAuth);
		log.verbose("End of AcademyInvokePayeezyRestWebservice.retrievePaypalAuthDetails() method");
		return strPayPalAuth;
	}

	/**
	 * This method is added as part of KER-15779 : 
	 * Prod Issue PayPal (Payment): Multiple ReAuth and Voids calls triggered from OMS
	 * This method is used to update PayZ failed transactin for PayPal .
	 * 
	 * @param elePaymentOut
	 * @return elePaymentOut 
	 */
	private Element updateFailedPayZForPayPal(Element elePaymentOut, String strPayPalAuth) throws Exception {
		log.verbose("Begin of AcademyInvokePayeezyRestWebservice.updateFailedPayZForPayPal() method");

		Element elePaymentErrorList = (Element) elePaymentOut.getElementsByTagName(
				AcademyConstants.ELE_PAYMENT_TRAN_ERROR_LIST).item(0);

		//Remove any error handling done
		if(!YFCObject.isVoid(elePaymentErrorList)) {
			elePaymentOut.removeChild(elePaymentErrorList);
			elePaymentErrorList = elePaymentOut.getOwnerDocument().createElement(
					AcademyConstants.ELE_PAYMENT_TRAN_ERROR_LIST);
			elePaymentOut.appendChild(elePaymentErrorList);
		}
		else {
			elePaymentErrorList = elePaymentOut.getOwnerDocument().createElement(
					AcademyConstants.ELE_PAYMENT_TRAN_ERROR_LIST);
			elePaymentOut.appendChild(elePaymentErrorList);
		}


		Element elePaymentError = elePaymentOut.getOwnerDocument().createElement(AcademyConstants.ELE_PAYMENT_TRAN_ERROR);
		elePaymentErrorList.appendChild(elePaymentError);


		elePaymentError.setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, AcademyConstants.STR_PAYPAL_AUTH);
		elePaymentError.setAttribute(AcademyConstants.ATTR_MESSAGE, strPayPalAuth);	

		log.verbose("End of AcademyInvokePayeezyRestWebservice.updateFailedPayZForPayPal() method");
		return elePaymentError;
	}

}// End of AcademyInvokePayeezyRestWebservice class
