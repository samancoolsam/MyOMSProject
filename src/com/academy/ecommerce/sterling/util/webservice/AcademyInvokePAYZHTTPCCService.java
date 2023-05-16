package com.academy.ecommerce.sterling.util.webservice;

import com.academy.ecommerce.sterling.util.AcademyPaymentProcessingUtil;
import com.academy.ecommerce.sterling.util.AcademyServiceUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
//import org.xml.sax.SAXException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
//import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * This service makes socket call to Payeezy payment gateway system for payment type of CREDIT_CARD
 * @author C0008437
 * created on 2017-02-27
 */
public class AcademyInvokePAYZHTTPCCService {

	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyInvokePAYZHTTPCCService.class);

	/**It holds the properties configured in AcademyInvokePAYZHTTPCCService service
	 * 
	 */
	private Properties props;

	/**It sets the properties
	 * @param props
	 */
	public void setProperties(Properties props) {
		this.props = props;
	}

	//This holds the EXact_Resp_Code of PayZ under SERVICE_UNAVAILABLE category
	private static List<String> listPayZServUnavailbleRespCode = null;

	//This holds the EXact_Resp_Code of PayZ under HARD_DECLINED category
	private static List<String> listPayZHardDeclinedRespCode = null;

	//This holds the EXact_Resp_Code of PayZ which needs retry
	//private static List<String> listPayZRetryRespCode = null;

	//Below are variables which holds the PayZ values from customer_overrides.properties file 
	private static String strEndpointURL = null;
	private static String strHost = null;
	private static String strPort = null;
	private static String strWebServicePath = null;
	private static String strSoapAction = null;
	private static String strMethodName = null;
	private static String strXmlNamespace = null;

	private static String strExactID = null;
	private static String strPayZPwd = null;
	private static String strPayZTimeout = null;
	private static String strPayZCCAuthExpiryDays = null;

	//private static String strPayZMsgLength = null;
	private static String strPayZAuthHeaderId = null;
	private static String strPayZKey = null;
	private static String strPayZSecretKeyAlgorithm = null;
	private static String strPayZMsgDigestAlgorithm = null;
	
	private static String strProxyHost = null;
	private static String strProxyPort = null;

	/* This static block initializes the variables from customer_overrides.properties file
	 * 
	 */
	static {
		log.verbose("Begin of AcademyInvokePAYZHTTPCCService static block ...");

		listPayZServUnavailbleRespCode = Arrays.asList(YFSSystem.getProperty(AcademyConstants.STR_SERV_UNAVAILABLE_EXACT_RESP_CODE).split(AcademyConstants.STR_COMMA));
		listPayZHardDeclinedRespCode = Arrays.asList(YFSSystem.getProperty(AcademyConstants.STR_HARD_DECLINED_EXACT_RESP_CODE).split(AcademyConstants.STR_COMMA));		
		//listPayZRetryRespCode = Arrays.asList(YFSSystem.getProperty(AcademyConstants.STR_RETRY_EXACT_RESP_CODE).split(AcademyConstants.STR_COMMA));

		strEndpointURL = YFSSystem.getProperty(AcademyConstants.STR_PAYZ_ENDPOINT_URL);
		strHost = YFSSystem.getProperty(AcademyConstants.STR_PAYZ_HOST);
		strPort = YFSSystem.getProperty(AcademyConstants.STR_PAYZ_PORT);
		strWebServicePath = YFSSystem.getProperty(AcademyConstants.STR_PAYZ_WEBSERVICE_PATH);
		strSoapAction = YFSSystem.getProperty(AcademyConstants.STR_PAYZ_SOAPACTION);
		strMethodName = YFSSystem.getProperty(AcademyConstants.STR_PAYZ_METHOD_NAME);
		strXmlNamespace = YFSSystem.getProperty(AcademyConstants.STR_PAYZ_XMLNAMESPACE);

		strExactID = YFSSystem.getProperty(AcademyConstants.STR_PAYZ_EXACT_ID);
		strPayZPwd = YFSSystem.getProperty(AcademyConstants.STR_PAYZ_PASSWORD);
		strPayZTimeout = YFSSystem.getProperty(AcademyConstants.STR_PAYZ_TIMEOUT);
		strPayZCCAuthExpiryDays = YFSSystem.getProperty(AcademyConstants.STR_CC_AUTH_EXPIRY_DAYS);

		strPayZAuthHeaderId = YFSSystem.getProperty(AcademyConstants.STR_PAYZ_AUTH_HEADER_ID);
		strPayZKey = YFSSystem.getProperty(AcademyConstants.STR_PAYZ_KEY);		
		strPayZSecretKeyAlgorithm = YFSSystem.getProperty(AcademyConstants.STR_PAYZ_SECRETKEY_ALGORITHM);
		strPayZMsgDigestAlgorithm = YFSSystem.getProperty(AcademyConstants.STR_PAYZ_MESSAGE_DIGEST_ALGORITHM);
		strProxyHost = YFSSystem.getProperty(AcademyConstants.STR_PAYZ_PROXY_HOST);
		strProxyPort = YFSSystem.getProperty(AcademyConstants.STR_PAYZ_PROXY_PORT);
		
		
		/*listPayZServUnavailbleRespCode = Arrays.asList("07,12,16,19,21,23,26,40,42,61,62".split(","));
	    listPayZHardDeclinedRespCode = Arrays.asList("11,14,15,28,32,52,53,57,64,89,93,69".split(","));

	    strEndpointURL = "https://api.demo.globalgatewaye4.firstdata.com:443/transaction/v22";
	    strHost = "api.demo.globalgatewaye4.firstdata.com";
	    strPort = "443";
	    strWebServicePath = "/transaction/v22";

	    strSoapAction = "http://secure2.e-xact.com/vplug-in/transaction/rpc-enc/SendAndCommit";
	    strMethodName = "SendAndCommit";
	    strXmlNamespace = "http://secure2.e-xact.com/vplug-in/transaction/rpc-enc/Request";

	    strExactID = "FF9734-53";
	    strPayZPwd = "ePsmERRuvM0uUUZJquae4MvavUwRZdj3";
	    strPayZTimeout = "30000";
	    strPayZCCAuthExpiryDays = "7";

	    strPayZAuthHeaderId = "439985";
	    strPayZKey = "5NSyYV8qqKx~ZJWASVaPhzKn_2ycFCgB";
	    strPayZSecretKeyAlgorithm = "HmacSHA1";
	    strPayZMsgDigestAlgorithm = "SHA1";

	    strProxyHost = "52.22.64.70";
	    strProxyPort = "8080";*/
		
		log.info("AcademyInvokePAYZHTTPCCService static variable initialization successfull ...");

		log.verbose("End of AcademyInvokePAYZHTTPCCService static block ...");
	}

	//This flag used as SocketTimeoutException indicator
	private boolean bRetry = false;

	/**
	 * This method creates and sends the payment request to PayZ gateway.
	 * @param docPaymentIn - It's a Payment XML 
	 * @return docPaymentOut - Payment XML as response document
	 * @throws Exception
	 */
	public Document invokePAYZHTTPCCService(YFSEnvironment env, Document docPaymentIn) throws Exception {

		log.verbose("Begin of AcademyInvokePAYZHTTPCCService.invokePAYZHTTPCCService() method");
		log.verbose("AcademyInvokePAYZHTTPCCService.invokePAYZHTTPCCService() Payment Input XML ::"+XMLUtil.getXMLString(docPaymentIn));

		Document docReqPayZ = null;
		Document docRespPayZ = null;		
		Document docPaymentOut = null;

		String chargeType =  XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH + "@ChargeType");
		String strIsDBLoggingEnableForAuth = props.getProperty(AcademyConstants.STR_DB_LOGGING_FOR_AUTH);
		String strIsDBLoggingEnableForCharge = props.getProperty(AcademyConstants.STR_DB_LOGGING_FOR_CHARGE);
		
		//START: GCD-197
		boolean bIgnoreAuth = false;
		Date dateCurrAuthExpiryDate = null;
		String strCurrAuthExpiry = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CURR_AUTH_EXP_DATE);
		String strReqAmt = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);
		String strAuthId = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_AUTHORIZATION_ID);
		
		//For cancel : true, For re-auth on auth expiry its false
		if(!YFCObject.isVoid(strCurrAuthExpiry)){
			dateCurrAuthExpiryDate = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN).parse(strCurrAuthExpiry);
		}
		/*1)For auth expiry product will make one entry for auth with -ve amount and one for new auth with +ve amount.
		* As the auth is already expired so we cannot void it and this auth with -ve amount will be processed as dummy i.e APPROVED response.
		* This call doesn't have CurrentAuthorizationExpirationDate attribute in Payment input xml.
		* 
		* 2)For partial/full cancllation the Payment input xml has CurrentAuthorizationExpirationDate attribute and the date is in future.
		* So the auth with -ve amount for cancellation would need VOID call. Here bIgnoreAuth value will remain false.
		*/
		//For cancel : false, For re-auth on auth expiry its true
		if(AcademyConstants.STR_CHRG_TYPE_AUTH.equalsIgnoreCase(chargeType) && 
				strReqAmt.startsWith(AcademyConstants.STR_HYPHEN) &&
				( YFCObject.isVoid(dateCurrAuthExpiryDate) || dateCurrAuthExpiryDate.before(Calendar.getInstance().getTime()) ) ) {
			bIgnoreAuth = true;
			log.info("DUMMY_AUTH for CTK::"+XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY));
		}
		//END: GCD-197
		if (!bIgnoreAuth) { //GCD-197
			//Get the PayZ SOAP resquest in XML document format 
			docReqPayZ = getSOAPMessageForPayZSendAndCommitReq(docPaymentIn);
			log.verbose("AcademyInvokePAYZHTTPCCService.invokePAYZHTTPCCService() XML document of PayZ Req ::"+XMLUtil.getXMLString(docReqPayZ));

			//Call the Payeezy gateway service
			docRespPayZ = callPayZService(docReqPayZ);
			//log.verbose("AcademyInvokePAYZHTTPCCService.invokePAYZHTTPCCService() SOAP response :: "+XMLUtil.getXMLString(docRespPayZ));
		}//GCD-197
		
		if(this.bRetry) {

			this.bRetry = false;
			docPaymentOut = XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT);
			Element elePayTimedOut = docPaymentOut.getDocumentElement();
			elePayTimedOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
			elePayTimedOut.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_YES);			
			elePayTimedOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG,AcademyConstants.STR_SOCKET_READ_TIMEOUT);
			elePayTimedOut.setAttribute(AcademyConstants.ATTR_COLLECTION_DATE,nextCollectionDate(props.getProperty(AcademyConstants.STR_NEXT_TRIGGER_IN_MIN)));

		} else {
			//Parse the PayZ response to form Payment XML
			if (AcademyConstants.STR_CHRG_TYPE_AUTH.equalsIgnoreCase(chargeType)) {								
				//START: GCD-197
				if(!bIgnoreAuth) {
					docPaymentOut = translatePayZAuthResp(docPaymentIn,docRespPayZ);
				} else {
					YFCDocument docDummyAuthResp = YFCDocument.createDocument(AcademyConstants.ELE_PAYMENT);
					YFCElement eleResp = docDummyAuthResp.getDocumentElement();

					eleResp.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_APPROVED);				
					eleResp.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, AcademyConstants.STR_DUMMY_AUTH);
					eleResp.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, strAuthId);
					eleResp.setAttribute(AcademyConstants.ATTR_AUTH_CODE, strAuthId);				
					eleResp.setAttribute(AcademyConstants.ATTR_REQUEST_ID, strAuthId);				
					eleResp.setAttribute(AcademyConstants.ATTR_TRAN_AMT, strReqAmt);
					eleResp.setAttribute(AcademyConstants.ATTR_AUTH_AMOUNT, strReqAmt);

					Calendar cal = Calendar.getInstance();				
					SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN_NEW);
					eleResp.setAttribute(AcademyConstants.ATTR_AUTH_TIME, sdf.format(cal.getTime()));

					docPaymentOut = docDummyAuthResp.getDocument();
				}
				bIgnoreAuth = false;//reset the flag
				//END: GCD-197
				if(AcademyConstants.STR_YES.equalsIgnoreCase(strIsDBLoggingEnableForAuth) &&
						!docPaymentOut.getDocumentElement().getAttribute(AcademyConstants.ATTR_RESPONSE_CODE).equalsIgnoreCase(AcademyConstants.STR_APPROVED)) {
					AcademyPaymentProcessingUtil.logPayZReqAndRespToDB(env, docPaymentIn, docReqPayZ, docRespPayZ, AcademyConstants.STR_DB_SERVICE_FOR_PAYZ);
				}
				log.verbose("Payment authorization xml::"+XMLUtil.getXMLString(docPaymentOut));
			} else if (AcademyConstants.STR_CHRG_TYPE_CHARGE.equalsIgnoreCase(chargeType)) {
				docPaymentOut = translatePayZChargeResp(docPaymentIn,docRespPayZ);
				log.verbose("Payment settlement/refund xml::"+XMLUtil.getXMLString(docPaymentOut));
				if(AcademyConstants.STR_YES.equalsIgnoreCase(strIsDBLoggingEnableForCharge) &&
						!docPaymentOut.getDocumentElement().getAttribute(AcademyConstants.ATTR_RESPONSE_CODE).equalsIgnoreCase(AcademyConstants.STR_APPROVED)) {
					AcademyPaymentProcessingUtil.logPayZReqAndRespToDB(env, docPaymentIn, docReqPayZ, docRespPayZ, AcademyConstants.STR_DB_SERVICE_FOR_PAYZ);
				}
			} 
		}
		log.verbose("AcademyInvokePAYZHTTPCCService.invokePAYZHTTPCCService() Payment Output XML:: "+XMLUtil.getXMLString(docPaymentOut));

		log.verbose("End of AcademyInvokePAYZHTTPCCService.invokePAYZHTTPCCService() method");

		return docPaymentOut;
	}

	/**This method prepares the PayZ request in SOAP format
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	private Document getSOAPMessageForPayZSendAndCommitReq(Document inDoc) throws Exception {

		log.verbose("Begin of AcademyInvokePAYZHTTPCCService.getSOAPMessageForPayZSendAndCommitReq() method");

		Document docPayZSoap = AcademyServiceUtil.getSOAPMessageTemplateForPayZSendAndCommitReq();
		//Document docPayZSoap = createDocument1("sendAndCommit.xml");
		Element elePaymentInput = inDoc.getDocumentElement();

		String chargeType = elePaymentInput.getAttribute(AcademyConstants.ATTR_CHARGE_TYPE);
		String reqAmount = elePaymentInput.getAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT);
		String strAuthorizationID = XPathUtil.getString(inDoc, AcademyConstants.STR_PAYMENT_PATH + AcademyConstants.XPATH_AUTHORIZATION_ID);		

		docPayZSoap.getElementsByTagName(AcademyConstants.ATTR_EXACT_ID).item(0).setTextContent(strExactID);
		docPayZSoap.getElementsByTagName(AcademyConstants.ATTR_PASSWORD).item(0).setTextContent(strPayZPwd);	
		docPayZSoap.getElementsByTagName(AcademyConstants.ATTR_DOLLOAR_AMOUNT).item(0).setTextContent(reqAmount);
		docPayZSoap.getElementsByTagName(AcademyConstants.ATTR_CARD_HOLDERS_NAME).item(0).setTextContent(elePaymentInput.getAttribute(AcademyConstants.ATTR_BILL_TO_FIRST_NAME)+" "+elePaymentInput.getAttribute(AcademyConstants.ATTR_BILL_TO_LAST_NAME));
		docPayZSoap.getElementsByTagName(AcademyConstants.ATTR_REF_NO).item(0).setTextContent(elePaymentInput.getAttribute(AcademyConstants.ATTR_ORDER_NO));
		//docPayZSoap.getElementsByTagName(AcademyConstants.ATTR_AUTHORIZATION_NUM).item(0).setTextContent(elePaymentInput.getAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID));		
		docPayZSoap.getElementsByTagName(AcademyConstants.ATTR_TRANSARMOR_TOKEN).item(0).setTextContent(elePaymentInput.getAttribute(AcademyConstants.ATTR_CREDIT_CARD_NO));
		//docPayZSoap.getElementsByTagName(AcademyConstants.ATTR_TRANSACTION_TAG).item(0).setTextContent(elePaymentInput.getAttribute(AcademyConstants.ATTR_PAYMENT_REF_2));
		docPayZSoap.getElementsByTagName(AcademyConstants.ATTR_CARD_TYPE).item(0).setTextContent(elePaymentInput.getAttribute(AcademyConstants.ATTR_CREDIT_CARD_TYPE));
		docPayZSoap.getElementsByTagName(AcademyConstants.ATTR_EXPIRY_DATE).item(0).setTextContent(elePaymentInput.getAttribute(AcademyConstants.ATTR_CC_EXPIRATION_DATE));

		/*	Transaction_Type for PayZ calls
		    Tagged Pre-Authorization / Authorization : 01
			Tagged Pre-AuthCompletion / settlement : 32 
			Purchase/Sale : 00
			Tagged Refund : 34
			Refund : 04
			Tagged Void : 33
		 */
		if (AcademyConstants.STR_CHRG_TYPE_AUTH.equals(chargeType)) {
			if (reqAmount.startsWith(AcademyConstants.STR_HYPHEN)) { 
				//START GCD-197		
				docPayZSoap.getElementsByTagName(AcademyConstants.ATTR_DOLLOAR_AMOUNT).item(0).setTextContent(reqAmount.replace(AcademyConstants.STR_HYPHEN, AcademyConstants.STR_EMPTY_STRING));
				docPayZSoap.getElementsByTagName(AcademyConstants.ATTR_TRANSACTION_TYPE).item(0).setTextContent(AcademyConstants.STR_PAYZ_VOID);//void
				docPayZSoap.getElementsByTagName(AcademyConstants.ATTR_AUTHORIZATION_NUM).item(0).setTextContent(elePaymentInput.getAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID));
				//END GCD-197
			} else {
				docPayZSoap.getElementsByTagName(AcademyConstants.ATTR_TRANSACTION_TYPE).item(0).setTextContent(AcademyConstants.STR_PAYZ_AUTH);//auth
			}
		} else if (AcademyConstants.STR_CHRG_TYPE_CHARGE.equals(chargeType)){
			//In Payment output XML we should send RequestID and in Payment input XML it comes as "RequestId" . Its a product bug.
			docPayZSoap.getElementsByTagName(AcademyConstants.ATTR_TRANSACTION_TAG).item(0).setTextContent(XPathUtil.getString(inDoc, AcademyConstants.STR_PAYMENT_PATH + AcademyConstants.STR_PAYMENT_PATH_REQUEST_ID));
			docPayZSoap.getElementsByTagName(AcademyConstants.ATTR_AUTHORIZATION_NUM).item(0).setTextContent(XPathUtil.getString(inDoc, AcademyConstants.STR_PAYMENT_PATH + AcademyConstants.PAYMENT_PATH_FOR_AUTH_CODE));

			if (reqAmount.startsWith(AcademyConstants.STR_HYPHEN)) {
				//For refund call the input xml doesn't contain CreditCardTransaction element and AuthorizationId is coming as blank 
				docPayZSoap.getElementsByTagName(AcademyConstants.ATTR_TRANSACTION_TYPE).item(0).setTextContent(AcademyConstants.STR_PAYZ_REFUND);//refund
				docPayZSoap.getElementsByTagName(AcademyConstants.ATTR_DOLLOAR_AMOUNT).item(0).setTextContent(reqAmount.replace(AcademyConstants.STR_HYPHEN, AcademyConstants.STR_EMPTY_STRING));

			} else if (YFSObject.isVoid(strAuthorizationID) || AcademyConstants.STR_DUMMY_AUTH_CODE.equals(strAuthorizationID)){
				docPayZSoap.getElementsByTagName(AcademyConstants.ATTR_AUTHORIZATION_NUM).item(0).setTextContent(AcademyConstants.STR_EMPTY_STRING);
				docPayZSoap.getElementsByTagName(AcademyConstants.ATTR_TRANSACTION_TYPE).item(0).setTextContent(AcademyConstants.STR_PAYZ_SALE);//sale

			} else {
				docPayZSoap.getElementsByTagName(AcademyConstants.ATTR_TRANSACTION_TYPE).item(0).setTextContent(AcademyConstants.STR_PAYZ_SETTLEMENT);//settlement
			}

		} /* else if (AcademyConstants.STR_VOID.equals(chargeType)){
			docPayZSoap.getElementsByTagName(AcademyConstants.ATTR_TRANSACTION_TYPE).item(0).setTextContent("33");//void
		}
		 */

		log.verbose("AcademyInvokePAYZHTTPCCService.getSOAPMessageForPayZSendAndCommitReq: SOAP Request" + XMLUtil.getXMLString(docPayZSoap));
		log.verbose("End of AcademyInvokePAYZHTTPCCService.getSOAPMessageForPayZSendAndCommitReq() method");

		return docPayZSoap;
	}

	/**This method parses the PayZ AUTH response to Payment output XML 
	 * @param docPayIn
	 * @param docPayZAuthOut
	 * @return
	 * @throws Exception
	 */
	private Document translatePayZAuthResp(Document docPayIn,Document docPayZAuthOut) throws Exception {

		log.verbose("Begin of AcademyInvokePAYZHTTPCCService.translatePayZAuthResp() method::");
		//These variables are used to hold the PayZ response values 
		String strExactRespCode = null;
		String strExactMsg = null;
		String strBankRespCode = null;
		String strBankMsg = null;
		String strBankRespCode2 = null;
		String strSeqNo = null;
		String strReqAmt = null;
		String strDollarAmount = null;
		String strAuthNum = null;
		String strTranTag = null;
		String strTranApp = null;
		String strTranErr = null;

		Document docPayAuthXML = XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT);
		Element elePayAuthResp = docPayAuthXML.getDocumentElement();

		//Element eleCCTranRoot = docPayAuthXML.createElement("CreditCardTransactions");
		//Element eleCCTranResp = docPayAuthXML.createElement("CreditCardTransaction");

		//eleCCTranRoot.appendChild(eleCCTranResp);
		//elePayAuthResp.appendChild(eleCCTranRoot);

		strReqAmt = XPathUtil.getString(docPayIn, AcademyConstants.STR_PAYMENT_PATH + AcademyConstants.STR_REQ_AMOUNT);
		strAuthNum = docPayZAuthOut.getElementsByTagName(AcademyConstants.ATTR_AUTHORIZATION_NUM).item(0).getTextContent();
		strTranTag = docPayZAuthOut.getElementsByTagName(AcademyConstants.ATTR_TRANSACTION_TAG).item(0).getTextContent();
		strDollarAmount = docPayZAuthOut.getElementsByTagName(AcademyConstants.ATTR_DOLLOAR_AMOUNT).item(0).getTextContent();
		strExactRespCode = docPayZAuthOut.getElementsByTagName(AcademyConstants.ATTR_EXACT_RESP_CODE).item(0).getTextContent();
		strExactMsg = docPayZAuthOut.getElementsByTagName(AcademyConstants.ATTR_EXACT_MESSAGE).item(0).getTextContent();
		strBankRespCode = docPayZAuthOut.getElementsByTagName(AcademyConstants.ATTR_BANK_RESP_CODE).item(0).getTextContent();
		strBankMsg = docPayZAuthOut.getElementsByTagName(AcademyConstants.ATTR_BANK_MESSAGE).item(0).getTextContent();
		strBankRespCode2 = docPayZAuthOut.getElementsByTagName(AcademyConstants.ATTR_BANK_RESP_CODE_2).item(0).getTextContent();
		strSeqNo = docPayZAuthOut.getElementsByTagName(AcademyConstants.ATTR_SEQUENCE_NO).item(0).getTextContent();
		strTranApp = docPayZAuthOut.getElementsByTagName(AcademyConstants.ATTR_TRANSACTION_APPROVED).item(0).getTextContent();
		strTranErr = docPayZAuthOut.getElementsByTagName(AcademyConstants.ATTR_TRANSACTION_ERROR).item(0).getTextContent();

		log.verbose("For OrderNo :"+XPathUtil.getString(docPayIn, AcademyConstants.STR_PAYMENT_PATH + "@OrderNo")+", Amount : "+strDollarAmount+" , ExactRespCode of auth call :"+strExactRespCode);

		if(strExactRespCode == null){

			elePayAuthResp.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
			elePayAuthResp.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_YES);
			elePayAuthResp.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE_DESC);
			return docPayAuthXML;
		}

		if(!YFSObject.isVoid(strExactRespCode) && AcademyConstants.STR_PAYZ_APPROVED.equals(strExactRespCode) && 
				!YFSObject.isVoid(strTranApp) && AcademyConstants.STR_TRUE.equalsIgnoreCase(strTranApp)) {

			elePayAuthResp.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_APPROVED);
			elePayAuthResp.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, strAuthNum);
			elePayAuthResp.setAttribute(AcademyConstants.ATTR_AUTH_AMOUNT, strReqAmt);
			elePayAuthResp.setAttribute(AcademyConstants.ATTR_TRAN_AMT, strReqAmt);
			//In Payment output XML we should send RequestID and in Payment input XML it comes as "RequestId" . It's a product bug.
			elePayAuthResp.setAttribute(AcademyConstants.ATTR_REQUEST_ID, strTranTag);
			elePayAuthResp.setAttribute(AcademyConstants.ATTR_AUTH_CODE, strAuthNum);//updating
			//elePayAuthResp.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_4, strTranTag);
			//elePayAuthResp.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_2, strTranTag);
			//elePayAuthResp.setAttribute("SCVVAuthCode", strTranTag);//updating
			//elePayAuthResp.setAttribute("Reference2", strTranTag+"A");//not working
			SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN_NEW);
			Calendar cal = Calendar.getInstance();
			elePayAuthResp.setAttribute(AcademyConstants.ATTR_AUTH_TIME, sdf.format(cal.getTime()));

			int iExpirationDays = Integer.parseInt(strPayZCCAuthExpiryDays);
			Calendar cal1 = Calendar.getInstance();
			cal1.add(Calendar.DATE, iExpirationDays);

			elePayAuthResp.setAttribute(AcademyConstants.ATTR_AUTH_EXP_DATE,sdf.format(cal1.getTime()));
			//eleCCTranResp.setAttribute("Reference2", strTranTag+"B");//not working
			//eleCCTranResp.setAttribute("Reference_2", strTranTag+"C");//not working
			//elePayAuthResp.setAttribute(ATTR_AUTH_RETURN_CODE, strTranTag+"D");//updating

		} else {
			/* PayZ Response
			Status     Transaction_Approved    Transaction_Error 
			--------   --------------------    -----------------
			Approved   True (1)                False (0) 
			Decline    False (0)               False (0) 
			Error      False (0)               True (1) 
			 */
			if (listPayZServUnavailbleRespCode.contains(strExactRespCode) && !YFSObject.isVoid(strTranErr) && AcademyConstants.STR_TRUE.equalsIgnoreCase(strTranErr) ) {
				elePayAuthResp.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
				elePayAuthResp.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_YES);
				elePayAuthResp.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, strExactRespCode);
				elePayAuthResp.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, strExactRespCode +" - "+ strExactMsg+" , "+ strBankMsg);
				elePayAuthResp.setAttribute(AcademyConstants.ATTR_COLLECTION_DATE,nextCollectionDate(props.getProperty(AcademyConstants.STR_NEXT_TRIGGER_IN_MIN)));

			} else if (listPayZHardDeclinedRespCode.contains(strExactRespCode) && !YFSObject.isVoid(strTranErr) && AcademyConstants.STR_FALSE.equalsIgnoreCase(strTranErr)) {
				elePayAuthResp.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_HARD_DECLINED);
				elePayAuthResp.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_YES);
				elePayAuthResp.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE,strExactRespCode);
				elePayAuthResp.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG,strExactRespCode +" - "+ strExactMsg+" , "+ strBankMsg);	

			} else {
				elePayAuthResp.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_HARD_DECLINED);
				elePayAuthResp.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_YES);
				elePayAuthResp.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, strExactRespCode);
				//elePayAuthResp.setAttribute(AcademyConstants.ATTR_SUSPEND_PAYMENT, AcademyConstants.STR_NO);
				//elePayAuthResp.setAttribute(AcademyConstants.ATTR_COLLECTION_DATE, nextCollectionDate(props.getProperty(AcademyConstants.STR_NEXT_TRIGGER_IN_MIN)));
				elePayAuthResp.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG,strExactRespCode +" - "+ strExactMsg+" , "+ strBankMsg);
			}
		}

		log.verbose("End of AcademyInvokePAYZHTTPCCService.translatePayZAuthResp() method");
		return docPayAuthXML;		

	}


	/**This method parses the PayZ CHARGE response to Payment output XML
	 * @param docPayIn
	 * @param docPayZChargeOut
	 * @return
	 * @throws Exception
	 */
	private Document translatePayZChargeResp(Document docPayIn, Document docPayZChargeOut) throws Exception {

		log.verbose("Begin of AcademyInvokePAYZHTTPCCService.translatePayZChargeResp() method::");
//		These variables are used to hold the PayZ response values 
		String strExactRespCode = null;
		String strExactMsg = null;
		String strBankRespCode = null;
		String strBankMsg = null;
		String strBankRespCode2 = null;
		String strSeqNo = null;
		String strReqAmt = null;
		String strDollarAmount = null;
		String strAuthNum = null;
		String strTranTag = null;
		String strTranApp = null;
		String strTranErr = null;
		
		Element elePayZChargeOut = null;
		NodeList nlPayZChargeOutChilds = null;
		Node eleChild = null;

		strReqAmt = XPathUtil.getString(docPayIn, AcademyConstants.STR_PAYMENT_PATH + AcademyConstants.STR_REQ_AMOUNT);
		strAuthNum = docPayZChargeOut.getElementsByTagName(AcademyConstants.ATTR_AUTHORIZATION_NUM).item(0).getTextContent();
		strTranTag = docPayZChargeOut.getElementsByTagName(AcademyConstants.ATTR_TRANSACTION_TAG).item(0).getTextContent();
		strDollarAmount = docPayZChargeOut.getElementsByTagName(AcademyConstants.ATTR_DOLLOAR_AMOUNT).item(0).getTextContent();
		strExactRespCode = docPayZChargeOut.getElementsByTagName(AcademyConstants.ATTR_EXACT_RESP_CODE).item(0).getTextContent();
		strExactMsg = docPayZChargeOut.getElementsByTagName(AcademyConstants.ATTR_EXACT_MESSAGE).item(0).getTextContent();
		strBankRespCode = docPayZChargeOut.getElementsByTagName(AcademyConstants.ATTR_BANK_RESP_CODE).item(0).getTextContent();
		strBankMsg = docPayZChargeOut.getElementsByTagName(AcademyConstants.ATTR_BANK_MESSAGE).item(0).getTextContent();
		strBankRespCode2 = docPayZChargeOut.getElementsByTagName(AcademyConstants.ATTR_BANK_RESP_CODE_2).item(0).getTextContent();
		strSeqNo = docPayZChargeOut.getElementsByTagName(AcademyConstants.ATTR_SEQUENCE_NO).item(0).getTextContent();
		strTranApp = docPayZChargeOut.getElementsByTagName(AcademyConstants.ATTR_TRANSACTION_APPROVED).item(0).getTextContent();
		strTranErr = docPayZChargeOut.getElementsByTagName(AcademyConstants.ATTR_TRANSACTION_ERROR).item(0).getTextContent();

		Document docPayChargeXML = XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT);
		Element elePayChargeResp = docPayChargeXML.getDocumentElement();

		log.verbose("For OrderNo :"+XPathUtil.getString(docPayIn, AcademyConstants.STR_PAYMENT_PATH + "@OrderNo")+", Amount : "+strDollarAmount+" , ExactRespCode of charge call :"+strExactRespCode);

		//START WN-1471-Dummy settlement for failure transaction
		//Comment out existing code
		/*if(!YFSObject.isVoid(strExactRespCode) && AcademyConstants.STR_PAYZ_APPROVED.equals(strExactRespCode) && !YFSObject.isVoid(strTranApp) && AcademyConstants.STR_TRUE.equalsIgnoreCase(strTranApp)) {
			elePayChargeResp.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_APPROVED);
			elePayChargeResp.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, strAuthNum);
			//In Payment output XML we should send RequestID and in Payment input XML it comes as "RequestId" . Its a product bug.
			elePayChargeResp.setAttribute(AcademyConstants.ATTR_REQUEST_ID, strTranTag);
			//elePayChargeResp.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_4, strTranTag);
			//elePayChargeResp.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_2, strTranTag);
			elePayChargeResp.setAttribute(AcademyConstants.ATTR_PAYMENT_TYPE, XPathUtil.getString(docPayIn, AcademyConstants.STR_PAYZ_PAYMENT_PATH + AcademyConstants.PAYMENT_TYPE_XPATH));
			elePayChargeResp.setAttribute(AcademyConstants.ATTR_AUTH_AMOUNT, XPathUtil.getString(docPayIn, AcademyConstants.STR_PAYZ_PAYMENT_PATH + AcademyConstants.STR_REQ_AMOUNT));						
			elePayChargeResp.setAttribute(AcademyConstants.ATTR_TRAN_TYPE, AcademyConstants.STR_CHRG_TYPE_CHARGE);
			elePayChargeResp.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE, AcademyConstants.ATTR_HUNDRED);
			elePayChargeResp.setAttribute(AcademyConstants.ATTR_TRAN_AMT, XPathUtil.getString(docPayIn, AcademyConstants.STR_PAYZ_PAYMENT_PATH + AcademyConstants.STR_REQ_AMOUNT));
			elePayChargeResp.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY, XPathUtil.getString(docPayIn, AcademyConstants.STR_PAYZ_PAYMENT_PATH + "@OrderHeaderKey"));
			elePayChargeResp.setAttribute(AcademyConstants.ATTR_ORDER_NO, XPathUtil.getString(docPayIn, AcademyConstants.STR_PAYZ_PAYMENT_PATH + "@OrderNo"));
			elePayChargeResp.setAttribute(AcademyConstants.ATTR_AUTH_CODE, strAuthNum);
			elePayChargeResp.setAttribute(AcademyConstants.ATTR_STATUS_CODE,AcademyConstants.STR_YES);//The StatusCode attribute is used for raising PaymentFailure alert
		} else {
			 PayZ Response
			Status     Transaction_Approved    Transaction_Error 
			--------   --------------------    -----------------
			Approved   True (1)                False (0) 
			Decline    False (0)               False (0) 
			Error      False (0)               True (1) 
			 
			elePayChargeResp.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, strAuthNum);
			elePayChargeResp.setAttribute(AcademyConstants.ATTR_REQUEST_ID, strTranTag);						
			elePayChargeResp.setAttribute(AcademyConstants.ATTR_STATUS_CODE,AcademyConstants.STR_NO);

			if (listPayZServUnavailbleRespCode.contains(strExactRespCode) && !YFSObject.isVoid(strTranErr) && AcademyConstants.STR_TRUE.equalsIgnoreCase(strTranErr)) {
				elePayChargeResp.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
				elePayChargeResp.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_YES);
				elePayChargeResp.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, strExactRespCode);
				elePayChargeResp.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, strExactRespCode +" - "+ strExactMsg+" , "+ strBankMsg);
				elePayChargeResp.setAttribute(AcademyConstants.ATTR_COLLECTION_DATE,nextCollectionDate(props.getProperty(AcademyConstants.STR_NEXT_TRIGGER_IN_MIN)));

			} else if (listPayZHardDeclinedRespCode.contains(strExactRespCode) && !YFSObject.isVoid(strTranErr) && AcademyConstants.STR_FALSE.equalsIgnoreCase(strTranErr)) {
				elePayChargeResp.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_HARD_DECLINED);
				elePayChargeResp.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_YES);
				elePayChargeResp.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, strExactRespCode);
				elePayChargeResp.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, strExactRespCode +" - "+ strExactMsg+" , "+ strBankMsg);

			} else {				
				elePayChargeResp.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_HARD_DECLINED);
				elePayChargeResp.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_YES);
				elePayChargeResp.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, strExactRespCode);
				elePayChargeResp.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, strExactRespCode +" - "+ strExactMsg+" , "+ strBankMsg);
			}

		}*/
		
		//Retry when system is unavailable. All error codes mention in the property consider for retry
		if (listPayZServUnavailbleRespCode.contains(strExactRespCode) && !YFSObject.isVoid(strTranErr) && AcademyConstants.STR_TRUE.equalsIgnoreCase(strTranErr)) {
			log.verbose("Retry scenario ::");
			elePayChargeResp.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
			elePayChargeResp.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_YES);
			elePayChargeResp.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, strExactRespCode);
			elePayChargeResp.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, strExactRespCode +" - "+ strExactMsg+" , "+ strBankMsg);
			elePayChargeResp.setAttribute(AcademyConstants.ATTR_COLLECTION_DATE,nextCollectionDate(props.getProperty(AcademyConstants.STR_NEXT_TRIGGER_IN_MIN)));
			return docPayChargeXML;
		}
		log.verbose("Settlement Process");
		elePayChargeResp.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_APPROVED);
		elePayChargeResp.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, strAuthNum);
		//In Payment output XML we should send RequestID and in Payment input XML it comes as "RequestId" . Its a product bug.
		elePayChargeResp.setAttribute(AcademyConstants.ATTR_REQUEST_ID, strTranTag);
		//elePayChargeResp.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_4, strTranTag);
		//elePayChargeResp.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_2, strTranTag);
		elePayChargeResp.setAttribute(AcademyConstants.ATTR_PAYMENT_TYPE, XPathUtil.getString(docPayIn, AcademyConstants.STR_PAYMENT_PATH + AcademyConstants.PAYMENT_TYPE_XPATH));
		elePayChargeResp.setAttribute(AcademyConstants.ATTR_AUTH_AMOUNT, XPathUtil.getString(docPayIn, AcademyConstants.STR_PAYMENT_PATH + AcademyConstants.STR_REQ_AMOUNT));						
		elePayChargeResp.setAttribute(AcademyConstants.ATTR_TRAN_TYPE, AcademyConstants.STR_CHRG_TYPE_CHARGE);
		elePayChargeResp.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE, AcademyConstants.ATTR_HUNDRED);
		elePayChargeResp.setAttribute(AcademyConstants.ATTR_TRAN_AMT, XPathUtil.getString(docPayIn, AcademyConstants.STR_PAYMENT_PATH + AcademyConstants.STR_REQ_AMOUNT));
		elePayChargeResp.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY, XPathUtil.getString(docPayIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_HEADER_KEY));
		elePayChargeResp.setAttribute(AcademyConstants.ATTR_ORDER_NO, XPathUtil.getString(docPayIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_NO));
		elePayChargeResp.setAttribute(AcademyConstants.ATTR_AUTH_CODE, strAuthNum);
		elePayChargeResp.setAttribute(AcademyConstants.ATTR_STATUS_CODE,AcademyConstants.STR_YES);//The StatusCode attribute is used for raising PaymentFailure alert
		elePayChargeResp.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_3,XPathUtil.getString(docPayIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_PAYMENT_REF_3));
		
		//START SOF: 2088 :Add a SOF Identifier to the Settlement Failure alert
		elePayChargeResp.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_4,XPathUtil.getString(docPayIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_PAYMENT_REF_4));
		//END SOF: 2088 :Add a SOF Identifier to the Settlement Failure alert
		
		//Setting payZ response for alert.
		if(YFSObject.isVoid(strTranApp) || AcademyConstants.STR_FALSE.equalsIgnoreCase(strTranApp)) {
			log.verbose(" Dummy Settlement ");
			elePayChargeResp.setAttribute(AcademyConstants.ATTR_STATUS_CODE,AcademyConstants.STR_NO);
			
			elePayZChargeOut = (Element)docPayZChargeOut.getElementsByTagName(AcademyConstants.ELE_PAYZ_TRANSACTION_RESULT).item(0);
			if(!YFSObject.isVoid(elePayZChargeOut)) {
				nlPayZChargeOutChilds = elePayZChargeOut.getChildNodes();			

				for (int iChildCount = 0; iChildCount < nlPayZChargeOutChilds.getLength(); iChildCount++) {
					eleChild = nlPayZChargeOutChilds.item(iChildCount);
					if((eleChild.getNodeType() == 1)){
						//convert all text node to attribute
						//if("Transaction_Approved".equals(eleChild.getNodeName())){
							elePayChargeResp.setAttribute(eleChild.getNodeName(), eleChild.getTextContent());
						//}
						
					}
				}
			}
		}
		//END WN-1471-Dummy settlement for failure transaction
		
		log.verbose("End of AcademyInvokePAYZHTTPCCService.translatePayZChargeResp() method::");
		return docPayChargeXML;
	}

	/**This method creates a Socket call to Payeezy payment gateway
	 * @param docPayZIn
	 * @return docPayZOut
	 */
	private Document callPayZService(Document docPayZIn) throws Exception{

		log.verbose("Begin of AcademyInvokePAYZHTTPCCService.callPayZService() method::");

		Document docPayZOut = null;
		String strEncryptedXML = null;
		String hashTime = null;
		String strAuthHeader = null;
		//String inputLine = null;
		int length = 2330;
		
		//START WN-1420 Http call with proxy
		//SSLSocket socket = null; 
		URL url = null;
		Proxy proxy = null;        
		HttpsURLConnection con = null;

		BufferedReader buffRead;
		String strInputLine = null;
		String strPayZIn = null;
		//END WN-1420 Http call with proxy

		StringBuffer strBuffPayZResp = new StringBuffer();		

		try {
			
			//START WN-1420 Http call with proxy. Removed socket connection related code
			url = new URL(strEndpointURL);

			strPayZIn = XMLUtil.getXMLString(docPayZIn);
			
			strEncryptedXML = getContentSha1(strPayZIn);
			hashTime = getHashTime();
			strAuthHeader = getAuthHeader(hashTime, strEncryptedXML);
			length += (strMethodName.length() * 2) + strXmlNamespace.length() + strPayZIn.length();

			/*log.verbose("Connecting PayZ through proxy:: strProxyHost:"+strProxyHost+" :: strProxyPort:"+strProxyPort + "strEndpointURL: "+strEndpointURL);
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(strProxyHost, Integer.parseInt(strProxyPort)));        
			con = (HttpsURLConnection) url.openConnection(proxy);
			//con = (HttpsURLConnection) url.openConnection();//Without proxy
			log.verbose("opening Connection for PayZ through proxy:: strProxyHost:"+strProxyHost+" :: strProxyPort:"+strProxyPort);*/

			//Adding ProxyEnable parameter temporary only for UAT testing without proxy
			String strProxyEnable = props.getProperty("ProxyEnable");
			log.verbose("ProxyEnable:: " +strProxyEnable);
			if(!YFSObject.isVoid(strProxyEnable) && "Y".equals(strProxyEnable)){
				log.verbose("Inside If:: " +strProxyEnable);
				log.verbose("Connecting PayZ through proxy:: strProxyHost:"+strProxyHost+" :: strProxyPort:"+strProxyPort + "strEndpointURL: "+strEndpointURL);
				proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(strProxyHost, Integer.parseInt(strProxyPort)));        
				con = (HttpsURLConnection) url.openConnection(proxy);
			}
			else {
				log.verbose("Inside Else:: " +strProxyEnable);
				log.verbose("Connecting PayZ without proxy:: " + "strEndpointURL: "+strEndpointURL);
				con = (HttpsURLConnection) url.openConnection();//Without proxy
			}
			log.verbose("opening Connection for PayZ ");
			
			con.setRequestMethod(AcademyConstants.STR_POST);

			con.setRequestProperty(AcademyConstants.STR_HOST, strHost);
			con.setRequestProperty(AcademyConstants.STR_CONTENT_TYPE, AcademyConstants.STR_TEXT_XML);
			con.setRequestProperty(AcademyConstants.STR_CONTENT_LENGTH, String.valueOf(length));		 
			con.setRequestProperty(AcademyConstants.STR_PAYZ_AUTHORIZATION, strAuthHeader);
			con.setRequestProperty(AcademyConstants.STR_GGE4_DATE, hashTime);
			con.setRequestProperty(AcademyConstants.STR_GGE4_CONTENT_SHA1, strEncryptedXML);
			con.setRequestProperty(AcademyConstants.STR_SOAPACTION,strSoapAction );		   

			log.verbose(AcademyConstants.STR_HOST + AcademyConstants.STR_COLON + strHost);
			log.verbose(AcademyConstants.STR_CONTENT_TYPE + AcademyConstants.STR_COLON + AcademyConstants.STR_TEXT_XML);
			log.verbose(AcademyConstants.STR_CONTENT_LENGTH + AcademyConstants.STR_COLON + String.valueOf(length));		 
			log.verbose(AcademyConstants.STR_PAYZ_AUTHORIZATION + AcademyConstants.STR_COLON + strAuthHeader);
			log.verbose(AcademyConstants.STR_GGE4_DATE + AcademyConstants.STR_COLON + hashTime);
			log.verbose(AcademyConstants.STR_GGE4_CONTENT_SHA1 + AcademyConstants.STR_COLON + strEncryptedXML);
			log.verbose(AcademyConstants.STR_SOAPACTION + AcademyConstants.STR_COLON + strSoapAction );	
			
			con.setDoInput(true);
			con.setDoOutput(true);
			//con.setUseCaches(false);

			con.setConnectTimeout(Integer.parseInt(strPayZTimeout));//connection time out set in millisec
			con.setReadTimeout(Integer.parseInt(strPayZTimeout));//Read time out set in millisec
			/*
				//To set protocal
				SSLContext sc = SSLContext.getInstance("TLSv1.2"); //$NON-NLS-1$
				sc.init(null, null, new java.security.SecureRandom());
				con.setSSLSocketFactory(sc.getSocketFactory());
			 */

			log.verbose("Send request \n");

			long startTime = System.currentTimeMillis();
			// Send request
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			log.verbose("sending request ::"+ strPayZIn);

			wr.writeBytes(strPayZIn);
			wr.flush();
			wr.close();

			log.verbose("successful connection established with server");
			
			//InputStream inputStream = con.getInputStream();
			strBuffPayZResp = new StringBuffer();

			buffRead = new BufferedReader(new InputStreamReader(con.getInputStream()));
			while ((strInputLine = buffRead.readLine()) != null){ 
				strBuffPayZResp.append(strInputLine);                 
			}
			//write data to temp file  
			buffRead.close();		
			
			long elapsedTime = System.currentTimeMillis() - startTime;

			log.info("AcademyInvokePAYZHTTPCCService.callPayZService() Response time of PayZ call:: "+ elapsedTime +" milisecs");
			log.verbose("AcademyInvokePAYZHTTPCCService.callPayZService() PayZ Response in string format ::"+strBuffPayZResp);

			if(strBuffPayZResp.toString().toLowerCase().indexOf(AcademyConstants.STR_SOAP_ENVELOPE) != -1){
				docPayZOut = XMLUtil.getDocument(strBuffPayZResp.substring(strBuffPayZResp.toString().toLowerCase().indexOf(AcademyConstants.STR_SOAP_ENVELOPE)));
				log.verbose("AcademyInvokePAYZHTTPCCService.callPayZService() PayZ Response in xml format ::"+ XMLUtil.getXMLString(docPayZOut));
			} else {
				log.info("AcademyInvokePAYZHTTPCCService.callPayZService() PayZ Error ::"+ strBuffPayZResp);
				//log.verbose("AcademyInvokePAYZHTTPCCService.callPayZService() PayZ Error Response ::"+ strBuffPayZResp);
				docPayZOut = XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT);
				Element elePayHardDeclined = docPayZOut.getDocumentElement();

				elePayHardDeclined.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_HARD_DECLINED);
				elePayHardDeclined.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_YES);			
				elePayHardDeclined.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG,AcademyConstants.STR_PAYZ_ERROR_IN_RESPONSE);
			}
		} 
		catch (Exception ex) {
			this.bRetry = true;			
			ex.printStackTrace();
			log.info("AcademyInvokePAYZHTTPCCService.callPayZService() inside Exception ::"+ex.getMessage());			
		}
		finally {
			if (con != null) {
				con.disconnect();
				con=null;
				log.verbose("Inside finally :: setting con=null");
			}		    
		}
		log.verbose("End of AcademyInvokePAYZHTTPCCService.callPayZService() method ::");

		return docPayZOut;
	}	


	/**This method provides the next collection date for Payment Agent to process
	 * @param strNxtTrigger
	 * @return
	 */
	private String nextCollectionDate(String strNxtTrigger) {
		log.verbose("Begin of AcademyInvokePAYZHTTPCCService.nextCollectionDate() method ::");		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN_1);		
		int iNextTriggerIntervalInMin = Integer.parseInt(strNxtTrigger);
		cal.add(Calendar.MINUTE, iNextTriggerIntervalInMin);
		String strCollectionDate = sdf.format(cal.getTime());
		log.verbose("End of AcademyInvokePAYZHTTPCCService.nextCollectionDate() method ::");		
		return strCollectionDate;
	}
	/**
	 * @param hashTime
	 * @param encryptedXML
	 * @return
	 */
	private String getAuthHeader(String hashTime, String encryptedXML) {
		String authorizationHeader = null;		
		try {
			String hmacString = AcademyConstants.STR_POST + AcademyConstants.STR_NEW_LINE + AcademyConstants.STR_TEXT_XML + AcademyConstants.STR_NEW_LINE + 
			encryptedXML + AcademyConstants.STR_NEW_LINE + hashTime + AcademyConstants.STR_NEW_LINE +
			strWebServicePath;

			authorizationHeader = AcademyConstants.STR_GGE4_API + AcademyConstants.STR_BLANKSPACE + strPayZAuthHeaderId + 
			AcademyConstants.STR_COLON + sha1(hmacString, strPayZKey);

			return authorizationHeader; 
		} catch (Exception e) {
			throw new RuntimeException("Failed Runtime: "+e);
		}
	}

	/**
	 * @param s
	 * @param keyString
	 * @return
	 */
	private String sha1(String s, String keyString){
		Base64 base64 = new Base64();
		try {
			SecretKeySpec key = new SecretKeySpec(keyString.getBytes(AcademyConstants.STR_UTF8), strPayZSecretKeyAlgorithm);
			Mac mac = Mac.getInstance(strPayZSecretKeyAlgorithm);
			mac.init(key);
			byte[] bytes = mac.doFinal(s.getBytes(AcademyConstants.STR_UTF8));

			return new String(base64.encode(bytes)); 
		} catch (Exception e) {
			throw new RuntimeException("Failed Runtime: "+e);
		}
	}

	/**
	 * @return
	 */
	private String getHashTime() {
		String time = getUTCFormattedDate(AcademyConstants.STR_UTC_FORMAT_DATE);
		return time;
	}

	/**
	 * @param format
	 * @return
	 */
	private String getUTCFormattedDate(String format) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		dateFormat.setTimeZone(TimeZone.getTimeZone(AcademyConstants.STR_UTC));
		return dateFormat.format(new Date());
	}

	/**
	 * @param content
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	private String getContentSha1(String content) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest mDigest = MessageDigest.getInstance(strPayZMsgDigestAlgorithm);
		byte[] result = mDigest.digest(content.getBytes());

		mDigest.update(content.getBytes(AcademyConstants.STR_UTF8), 0, content.length());
		result = mDigest.digest();

		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < result.length; i++) {
			int halfbyte = result[i] >>> 4 & 0xF;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) (48 + halfbyte));
				else
					buf.append((char) (97 + (halfbyte - 10)));
				halfbyte = result[i] & 0xF;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	/*public static void main(String args[]) throws Exception
	{
		Document inDoc = createDocument1("PayZRequest.xml");
		AcademyInvokePAYZHTTPCCService obj = new AcademyInvokePAYZHTTPCCService();	
		
		Properties props1 = new Properties();
		props1.setProperty(AcademyConstants.STR_DB_LOGGING_FOR_AUTH,"N");
		props1.setProperty(AcademyConstants.STR_DB_LOGGING_FOR_CHARGE,"N");
		obj.setProperties(props1);
		//obj.invokePAYZHTTPCCService(env, inDoc);
		obj.invokePAYZHTTPCCService(null, inDoc);
	}
	public static Document createDocument1(String filePath) throws Exception
    {
                    File file = new File(filePath);
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    dbf.setValidating(true);
                    Document doc = db.parse(file);
                    //log.verbose(XMLUtil.getXMLString(doc));
                    return doc;
    }*/
}// End of AcademyInvokePAYZHTTPCCService class