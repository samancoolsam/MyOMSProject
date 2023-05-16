//package declaration
package com.academy.ecommerce.sterling.userexits;

//import statements
//java import statements
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyPaymentProcessingUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSExtnPaymentCollectionInputStruct;
import com.yantra.yfs.japi.YFSExtnPaymentCollectionOutputStruct;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSCollectionOthersUE;

/**
 * 
 * class to perform the payment operations for PayPal payment type.
 *
 */

public class AcademyPayPalCollectionUEImpl implements YFSCollectionOthersUE {

	/*
	 * Start : OMNI-647 :: Discontinue Cardinal Commenting below lines of code as
	 * Below lines of code have been added to provide dummy response and raise alert
	 */

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPayPalCollectionUEImpl.class);

	public YFSExtnPaymentCollectionOutputStruct collectionOthers(YFSEnvironment env,
			YFSExtnPaymentCollectionInputStruct paymentInputStruct) throws YFSUserExitException {

		log.beginTimer("Start-AcademyPayPalCollectionUEImpl ::");
		YFSExtnPaymentCollectionOutputStruct paymentOutputStruct = null;
		try {
			if (paymentInputStruct.paymentType.equals(AcademyConstants.PAYPAL)) {

				log.debug("collectionOthers :: Payment Type is PayPal");

				paymentOutputStruct = new YFSExtnPaymentCollectionOutputStruct();

				completeSettlementAndRaiseAlert(env, paymentInputStruct, paymentOutputStruct);

			}
		} catch (Exception e) {
			log.error("Exception AcademyPayPalCollectionUEImpl.collectionOthers() :: " + e);
		}

		log.endTimer("End-AcademyPayPalCollectionUEImpl ::");
		return paymentOutputStruct;
	}

	private void completeSettlementAndRaiseAlert(YFSEnvironment env,
			YFSExtnPaymentCollectionInputStruct paymentInputStruct,
			YFSExtnPaymentCollectionOutputStruct paymentOutputStruct) throws Exception {

		log.verbose("Inside completeSettlement::Perform Settlement");

		paymentOutputStruct.authorizationId = paymentInputStruct.authorizationId;
		paymentOutputStruct.requestID = AcademyConstants.STR_DUMMY_SETTLEMENT;
		paymentOutputStruct.tranAmount = paymentInputStruct.requestAmount;
		paymentOutputStruct.authorizationAmount = paymentInputStruct.requestAmount;
		paymentOutputStruct.tranType = paymentInputStruct.chargeType;
		paymentOutputStruct.tranReturnCode = AcademyConstants.ATTR_HUNDRED;
		paymentOutputStruct.PaymentReference4 = AcademyConstants.STR_CARDINAL;
		paymentOutputStruct.authCode = AcademyConstants.STR_DUMMY_SETTLEMENT;

		if (!YFCObject.isVoid(paymentInputStruct.currentAuthorizationCreditCardTransactions)) {
			Document doc = paymentInputStruct.currentAuthorizationCreditCardTransactions;
			log.verbose("currentAuthorizationCreditCardTransactions element is::" + XMLUtil.getXMLString(doc));
			paymentOutputStruct.authCode = XPathUtil.getString(doc, AcademyConstants.XPATH_AUTH_CODE);
		}

		// Raise Alert
		Document docPayment = XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT);
		Element elePayment = docPayment.getDocumentElement();
		elePayment.setAttribute(AcademyConstants.ATTR_ORDER_NO, paymentInputStruct.orderNo);
		elePayment.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, paymentInputStruct.orderHeaderKey);
		elePayment.setAttribute(AcademyConstants.ATTR_PAYMENT_TYPE, paymentInputStruct.paymentType);
		elePayment.setAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT,
				Double.toString(paymentInputStruct.requestAmount));
		elePayment.setAttribute(AcademyConstants.ATTR_AUTH_AMOUNT, Double.toString(paymentInputStruct.requestAmount));
		elePayment.setAttribute(AcademyConstants.ATTR_TRAN_AMT, Double.toString(paymentInputStruct.requestAmount));
		elePayment.setAttribute(AcademyConstants.ATTR_PAYMENT_TYPE, paymentInputStruct.paymentType);
		elePayment.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_APPROVED);
		elePayment.setAttribute(AcademyConstants.ATTR_STATUS_CODE, AcademyConstants.STR_CARDINAL_STATUS_Y);
		elePayment.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE, AcademyConstants.ATTR_HUNDRED);
		elePayment.setAttribute(AcademyConstants.ATTR_TRAN_TYPE, paymentInputStruct.chargeType);
		elePayment.setAttribute(AcademyConstants.ATTR_TRANSID, AcademyConstants.STR_DUMMY_SETTLEMENT);
		elePayment.setAttribute(AcademyConstants.ATTR_RAISE_PAYMENT_ALERT, AcademyConstants.STR_YES);

		elePayment.setAttribute(AcademyConstants.ATTR_REASON_CODE, AcademyConstants.STR_CARDINAL);
		elePayment.setAttribute(AcademyConstants.ATTR_REASON_DESC,
				paymentInputStruct.paymentType + " :: " + paymentInputStruct.chargeType);

		AcademyUtil.invokeService(env, AcademyConstants.SERV_SETTLEMENT_FAILURE_ALERT, docPayment);

	}

	/*
	 * End : OMNI-647
	 */

	/*
	 * Start : OMNI-647 :: Discontinue Cardinal Commenting below lines of code as
	 * ASO no more using Cardinal
	 */

	/*
	 * //private Properties props; String strNextTriggerIntervalInMin = null; String
	 * strNextTriggerIntervalInMinAuthError = null; String strIsDBLoggingEnable =
	 * null; String strDBServiceName = null; String strDBLoggingStatus = null;
	 * String strAuthRetryErrorCodes = null;
	 * 
	 * private YIFApi newApi = null;
	 * 
	 * //private final static String AUTH_RETRY_FOR_STATUS_CODE =
	 * "AuthRetryForStatusCode"; //set the logger private static YFCLogCategory log
	 * = YFCLogCategory.instance(AcademyPayPalCollectionUEImpl.class);
	 * 
	 * public void setProperties(Properties props) { this.props = props; }
	 * 
	 * 
	 * private static YIFApi api = null;
	 * 
	 * static { try {
	 * 
	 * Document envDoc = null; api = YIFClientFactory.getInstance().getApi();
	 * log.verbose("invoking static block"); } catch (YIFClientCreationException e)
	 * { e.printStackTrace(); }
	 * 
	 * }
	 * 
	 * 
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yantra.yfs.japi.ue.YFSCollectionOthersUE#collectionOthers(com.yantra.yfs.
	 * japi.YFSEnvironment, com.yantra.yfs.japi.YFSExtnPaymentCollectionInputStruct)
	 * 
	 * Checking the ChargeType from InputStruct and processing according to that.
	 * 
	 * public YFSExtnPaymentCollectionOutputStruct collectionOthers(YFSEnvironment
	 * env,YFSExtnPaymentCollectionInputStruct paymentInputStruct) throws
	 * YFSUserExitException {
	 * 
	 * log.beginTimer("Start-AcademyPayPalCollectionUEImpl::");
	 * YFSExtnPaymentCollectionOutputStruct paymentOutputStruct = null; Document
	 * docCardinalRequestResponse = null; Element eleCentinelResponse = null;
	 * Document centinelReqInDoc = null; YFSEnvironment newEnv = null;
	 * 
	 * //if Payment type is PayPal if
	 * (paymentInputStruct.paymentType.equals(AcademyConstants.PAYPAL)) {
	 * 
	 * log.debug("collectionOthers :: Payment Type is PayPal"); paymentOutputStruct
	 * = new YFSExtnPaymentCollectionOutputStruct();
	 * 
	 * //get chargeType from Input struct String chargeType =
	 * paymentInputStruct.chargeType;
	 * 
	 * Start changes for STL-1445:commenting the below code //if ChargeType is
	 * CHARGE if (chargeType.equals("CHARGE")) {
	 * log.debug("collectionOthers :: Charge Type is CHARGE"); paymentOutputStruct =
	 * constructBasicCollectionPayPalOutputForCharge(paymentInputStruct); } //if
	 * ChargeType is AUTHORIZATION else if (chargeType.equals("AUTHORIZATION")) {
	 * log.debug("collectionOthers :: Charge Type is AUTHORIZATION"); try {
	 * paymentOutputStruct = invokeCardinalReAuthService(env, paymentInputStruct); }
	 * catch (Exception e) { e.printStackTrace(); }
	 * 
	 * } CentinelRequest centinelRequest = null; CentinelResponse centinelResponse =
	 * new CentinelResponse();
	 * 
	 * try { //Moving the below logic to prepare the centinel request to a common
	 * service(AcademyInvokeCardinalHTTPService) to remove code redundency.
	 * centinelRequest = getHTTPDocForAuthRequest(paymentInputStruct); //if
	 * CentinelRequest is not blank if (!YFCObject.isVoid(centinelRequest)) {
	 * 
	 * System.setProperty("javax.net.ssl.trustStore", YFSSystem
	 * .getProperty(AcademyConstants.KEY_STORE_PAYPAL));
	 * System.setProperty("javax.net.ssl.trustStorePassword", YFSSystem
	 * .getProperty(AcademyConstants.KEY_PASSWORD_PAYPAL));
	 * 
	 * log.debug("invokeCardinalReAuthService :: Cardinal Request --> " +
	 * centinelRequest.getFormattedRequest());
	 * 
	 * //making HTTP call to get the Centinel Response centinelResponse =
	 * centinelRequest.sendHTTP(YFSSystem
	 * .getProperty(AcademyConstants.CARDIANL_PAYPAL_ENDPOINT)); }
	 * 
	 * //formatting the cetinel response String response =
	 * centinelResponse.getFormattedResponse();
	 * 
	 * log.debug("invokeCardinalReAuthService :: Cardinal Response --> " +
	 * centinelResponse.getFormattedResponse());
	 * 
	 * response = response.replaceAll("&", "&amp;");
	 * 
	 * Document centinelResponseDoc = XMLUtil.getDocument(response);
	 * 
	 * log .debug("invokeCardinalReAuthService :: Response Doc is" +
	 * XMLUtil.getXMLString(centinelResponseDoc));
	 * 
	 * //Code changes to bypass webservices based on java version //String
	 * strRetryEnabled =
	 * YFSSystem.getProperty("academy.payz.cardinal.retry.enabled"); String
	 * strRetryUsingAppEnabled =
	 * YFSSystem.getProperty(AcademyConstants.PROP_CARDNL_RETRY_ENABLED_USING_APP);
	 * //log.verbose("strRetryEnabled :: "+strRetryEnabled);
	 * log.verbose("strRetryUsingAppEnabled :: "+strRetryUsingAppEnabled);
	 * //if(!YFCObject.isNull(strRetryEnabled) && strRetryEnabled.equals("Y")) {
	 * if(!YFCObject.isNull(strRetryUsingAppEnabled)&&
	 * strRetryUsingAppEnabled.equals(AcademyConstants.STR_YES)) {
	 * log.verbose(" Creating a new environment "); newEnv =
	 * createEnvironment(env.getProgId()); } else { String strJavaVersion =
	 * System.getProperty("java.version").trim(); String strDelayInterval =
	 * YFSSystem.getProperty("academy.payz.cardinal.delay.seconds"); if
	 * (!YFSObject.isVoid(strJavaVersion) && strJavaVersion.startsWith("1.6")) {
	 * log.verbose(" PayPal Orders being fulfilled from java 1.6 "); } else {
	 * log.verbose(" Skip calls from this JVM. "); paymentOutputStruct.retryFlag =
	 * "Y";
	 * 
	 * Calendar cal = Calendar.getInstance(); SimpleDateFormat sdf = new
	 * SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN_1);
	 * cal.add(Calendar.SECOND, Integer.parseInt(strDelayInterval)); String
	 * strCollectionDate = sdf.format(cal.getTime());
	 * 
	 * paymentOutputStruct.collectionDate = sdf.parse(strCollectionDate); return
	 * paymentOutputStruct; } } } //Code changes to bypass webservices based on java
	 * version
	 * 
	 * //call this method to get all the required attributes to prepare Centinel
	 * Request centinelReqInDoc = prepareCentinelRequestDoc(env,
	 * paymentInputStruct); //call this service to get the Centinel Request and
	 * Response if(!YFCObject.isNull(newEnv)) {
	 * log.verbose("Invoking the service with the new environment object");
	 * docCardinalRequestResponse = newApi.executeFlow(newEnv,
	 * AcademyConstants.SERV_ACAD_INVOKE_CARDINAL, centinelReqInDoc); } else {
	 * docCardinalRequestResponse = AcademyUtil.invokeService(env,
	 * AcademyConstants.SERV_ACAD_INVOKE_CARDINAL, centinelReqInDoc); }
	 * 
	 * String request = centinelRequest.getFormattedRequest();
	 * log.verbose("Cardinal Settlement request *** "+request); request =
	 * request.replaceAll("&", "&amp;");
	 * 
	 * Document centinelRequestDoc = XMLUtil.getDocument(request);
	 * //centinelRequestDoc = (Document)
	 * XPathUtil.getNode(docCardinalRequestResponse.getDocumentElement(),
	 * "/CardinalRequestResponse/CardinalRequest/CardinalMPI");
	 * //log.verbose("centinelRequestDoc *** "
	 * +XMLUtil.getXMLString(centinelRequestDoc));
	 * 
	 * eleCentinelResponse = (Element)
	 * XPathUtil.getNode(docCardinalRequestResponse.getDocumentElement(),
	 * AcademyConstants.XPATH_CARDINAL_RESP);
	 * 
	 * 
	 * if (AcademyConstants.STR_CHRG_TYPE_AUTH.equalsIgnoreCase(chargeType)) {
	 * log.verbose("chargeType is AUTHORIZATION *** "); //
	 * log.verbose("Cardinal AuthRequest Output " + response); paymentOutputStruct =
	 * translateCentinelResponse(env, paymentInputStruct, eleCentinelResponse); }
	 * else if (AcademyConstants.STR_CHRG_TYPE_CHARGE.equalsIgnoreCase(chargeType))
	 * { log.verbose("chargeType is CHARGE *** "); //below method is called to get
	 * the attributes needed for response validation setValidationParameters(env);
	 * //log.verbose("Cardinal Settlement Output " + response); //
	 * paymentOutputStruct =
	 * constructBasicCollectionPayPalOutputForCharge(paymentInputStruct);
	 * paymentOutputStruct = translateCentinelResponseForSettle(env,
	 * paymentInputStruct,centinelReqInDoc, eleCentinelResponse); } } catch
	 * (Exception e) { log.verbose("Error while process this request");
	 * e.printStackTrace(); } //End changes for STL-1445:modifying the logic to call
	 * cardinal for Settlement also. }
	 * log.endTimer("End-AcademyPayPalCollectionUEImpl"); return
	 * paymentOutputStruct;
	 * 
	 * }
	 *//**
		 * This method will fetch the code values set in the common code
		 * 
		 * @param env
		 * @throws Exception
		 */
	/*
	 * private void setValidationParameters(YFSEnvironment env) throws Exception {
	 * log.verbose("Inside setValidationParameters()"); Document docCardinalValLst =
	 * null; Document docCommonCodeInput = null; NodeList commonCodeNL = null; int
	 * iCommonCodeNLLen = 0; String strCodeValue = null; String strCodeShortDesc =
	 * null; Element eleCommonCode = null; String STR_GET_COMMON_CODE_LIST_OUT_TEMP
	 * =
	 * "<CommonCodeList> <CommonCode CodeValue='' CodeShortDescription='' /> </CommonCodeList>"
	 * ;
	 * 
	 * docCommonCodeInput =
	 * XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
	 * docCommonCodeInput.getDocumentElement().setAttribute(AcademyConstants.
	 * ATTR_CODE_TYPE, AcademyConstants.STR_PP_VALIDATION);
	 * docCommonCodeInput.getDocumentElement().setAttribute(AcademyConstants.
	 * ORGANIZATION_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
	 * log.verbose("Input to getCommonCodeList API is : "+XMLUtil.getXMLString(
	 * docCommonCodeInput)); Document outputTemplate =
	 * YFCDocument.getDocumentFor(STR_GET_COMMON_CODE_LIST_OUT_TEMP).getDocument();
	 * env.setApiTemplate(AcademyConstants.API_GET_COMMON_CODELIST, outputTemplate);
	 * docCardinalValLst =
	 * AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_COMMON_CODELIST,
	 * docCommonCodeInput);
	 * env.clearApiTemplate(AcademyConstants.API_GET_COMMON_CODELIST);
	 * log.verbose("Output from getCommonCodeList API is : "+XMLUtil.getXMLString(
	 * docCardinalValLst)); commonCodeNL =
	 * docCardinalValLst.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);
	 * iCommonCodeNLLen = commonCodeNL.getLength(); for(int i =
	 * 0;i<iCommonCodeNLLen;i++) { eleCommonCode = (Element)commonCodeNL.item(i);
	 * strCodeValue =
	 * eleCommonCode.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE);
	 * strCodeShortDesc =
	 * eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
	 * 
	 * if(AcademyConstants.STR_NEXT_TRIGGER_IN_MIN.equals(strCodeValue)) {
	 * strNextTriggerIntervalInMin= strCodeShortDesc; }
	 * if(AcademyConstants.STR_NEXT_TRIGGER_IN_MIN_AUTH_ERROR.equals(strCodeValue))
	 * { strNextTriggerIntervalInMinAuthError= strCodeShortDesc; }
	 * if(AcademyConstants.STR_IS_DB_LOG_ENABALE.equals(strCodeValue)) {
	 * strIsDBLoggingEnable = strCodeShortDesc; }
	 * if(AcademyConstants.STR_DB_SERVICE_NAME.equals(strCodeValue)) {
	 * strDBServiceName = strCodeShortDesc; }
	 * if(AcademyConstants.STR_DB_LOG_STATUS.equals(strCodeValue)) {
	 * strDBLoggingStatus = strCodeShortDesc; }
	 * if(AcademyConstants.STR_AUTH_RETRY_ERROR_CODES.equals(strCodeValue)) {
	 * strAuthRetryErrorCodes = strCodeShortDesc; } } }
	 *//**
		 * Modifying the cardinal response for dummy settlement if credit card
		 * settlement fails through cardinal
		 * 
		 * @param env
		 * @param inDoc
		 * @param responseDoc
		 * @param paymentPath
		 * @return
		 * @throws Exception
		 */
	/*
	 * //START: STL-1445 - In case the settlement call to Cardinal fails, Sterling
	 * should mark the charge request as paid and generate T-log. private
	 * YFSExtnPaymentCollectionOutputStruct
	 * translateCentinelResponseForSettle(YFSEnvironment env,
	 * YFSExtnPaymentCollectionInputStruct paymentInputStruct,Document
	 * centinelRequestDoc, Element eleCentinelResponse) throws Exception {
	 * 
	 * log.
	 * verbose("&&&&&&&&&&&&&&&& translateCentinelResponseForSettle &&&&&&&&&&&&&&&&&&&"
	 * ); YFSExtnPaymentCollectionOutputStruct paymentOutputStruct = new
	 * YFSExtnPaymentCollectionOutputStruct(); Document docResponse =
	 * XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT); Element eleResponse =
	 * docResponse.getDocumentElement(); Document centinelResponseDoc =
	 * XMLUtil.getDocumentForElement(eleCentinelResponse); String strStatusCode=
	 * null; //Date collectionDate = null; String strErrorNo = null; Element
	 * eleStatusCode = null; Element eleErrorNo = null;
	 * 
	 * //Start Changes:STL-1448:This attribute is used to set the attributes needed
	 * for alert in case of PayPal
	 * eleResponse.setAttribute(AcademyConstants.ATTR_PAYMENT_TYPE,
	 * paymentInputStruct.paymentType); //End Changes:STL-1448:This attribute is
	 * used to set the attributes needed for alert in case of PayPal //In case we
	 * are not able to reach cardinal we are getting error no: 7040. //In this case
	 * StatusCode element is missing in cardinal response and to handle null pointer
	 * exception the following snippet is used. eleStatusCode =
	 * (Element)eleCentinelResponse.getElementsByTagName(AcademyConstants.
	 * ATTR_STATUS_CODE).item(0) ; eleErrorNo =
	 * (Element)eleCentinelResponse.getElementsByTagName(AcademyConstants.
	 * ATTR_ERROR_NO).item(0) ; strErrorNo = eleErrorNo.getTextContent();
	 * if(YFSObject.isVoid(eleStatusCode)){
	 * eleResponse.setAttribute(AcademyConstants.ATTR_STATUS_CODE,AcademyConstants.
	 * STR_CARDINAL_STATUS_U); } else{
	 * eleResponse.setAttribute(AcademyConstants.ATTR_STATUS_CODE,
	 * eleStatusCode.getTextContent()); }
	 * 
	 * strStatusCode=eleResponse.getAttribute(AcademyConstants.ATTR_STATUS_CODE);
	 * log.verbose("StatusCode is::"+strStatusCode); //Start Changes:STL-1448:This
	 * attribute is used to set the attributes needed for alert in case of PayPal
	 * if(AcademyConstants.STR_CARDINAL_STATUS_U.equals(strStatusCode)){ //Setting
	 * collection date and retryFlag to retry again.
	 * paymentOutputStruct.collectionDate =
	 * AcademyPaymentProcessingUtil.getCollectionDate(strNextTriggerIntervalInMin);
	 * paymentOutputStruct.retryFlag = AcademyConstants.STR_YES; } else
	 * if(AcademyConstants.STR_CARDINAL_STATUS_E.equals(strStatusCode) &&
	 * strSettlementRetryErrorCodes.contains(strErrorNo)){ //Setting collection date
	 * and retryFlag to retry again. collectionDate =
	 * AcademyPaymentProcessingUtil.setCollectionDateToFuture(
	 * strNextTriggerIntervalInMin); paymentOutputStruct.collectionDate =
	 * collectionDate; paymentOutputStruct.retryFlag = AcademyConstants.STR_YES; }
	 * else { //This method will make the settlement success in Sterling in case of
	 * success and error responses from Cardinal
	 * completeSettlementAndRaiseAlertConditionally(env,paymentInputStruct,
	 * paymentOutputStruct,docResponse,eleCentinelResponse);
	 * 
	 * }
	 * 
	 * //below code is to log cardinal request and response for all settlement
	 * failures i.e P and E status which is configurable through common code
	 * if(strDBLoggingStatus.contains(strStatusCode)){ strDBServiceName =
	 * strDBServiceName.concat("_"+strStatusCode);
	 * AcademyPaymentProcessingUtil.logCardinalRequestAndResponseToDB(env,
	 * centinelRequestDoc,
	 * centinelResponseDoc,strIsDBLoggingEnable,strDBServiceName); }
	 * 
	 * return paymentOutputStruct; }
	 *//**
		 * This method will do the dummy settlement and raises alert only in case of
		 * failure scenarios
		 * 
		 * @param paymentInputStruct
		 * @param paymentOutputStruct
		 */
	/*
	 * private void completeSettlementAndRaiseAlertConditionally(YFSEnvironment env,
	 * YFSExtnPaymentCollectionInputStruct paymentInputStruct,
	 * YFSExtnPaymentCollectionOutputStruct paymentOutputStruct,Document
	 * docResponse, Element eleCentinelResponse) throws Exception{
	 * 
	 * log.
	 * verbose("Inside completeSettlementAndRaiseAlertConditionally()::Perform Settlement"
	 * );
	 * 
	 * paymentOutputStruct.authorizationId = paymentInputStruct.authorizationId;
	 * paymentOutputStruct.requestID = paymentInputStruct.authorizationId;
	 * paymentOutputStruct.tranAmount = paymentInputStruct.requestAmount;
	 * paymentOutputStruct.authorizationAmount = paymentInputStruct.requestAmount;
	 * paymentOutputStruct.tranType = AcademyConstants.STR_CHRG_TYPE_CHARGE;
	 * paymentOutputStruct.tranReturnCode = AcademyConstants.ATTR_HUNDRED;
	 * 
	 * if (!YFCObject.isVoid(paymentInputStruct.
	 * currentAuthorizationCreditCardTransactions)) { Document doc =
	 * paymentInputStruct.currentAuthorizationCreditCardTransactions;
	 * log.verbose("currentAuthorizationCreditCardTransactions element is::"+XMLUtil
	 * .getXMLString(doc)); paymentOutputStruct.authCode = XPathUtil.getString(doc,
	 * AcademyConstants.XPATH_AUTH_CODE); } Element eleResponse =
	 * docResponse.getDocumentElement(); String strStatusCode =
	 * eleResponse.getAttribute(AcademyConstants.ATTR_STATUS_CODE);
	 * if(!AcademyConstants.STR_CARDINAL_STATUS_Y.equals(strStatusCode)){
	 * 
	 * eleResponse.setAttribute(AcademyConstants.ATTR_TRAN_AMT,
	 * String.valueOf(paymentInputStruct.requestAmount));
	 * eleResponse.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,
	 * paymentInputStruct.orderHeaderKey);
	 * eleResponse.setAttribute(AcademyConstants.ATTR_ORDER_NO,
	 * paymentInputStruct.orderNo);
	 * 
	 * //START SOF: WN-2088 :Add a SOF Identifier to the Settlement Failure alert
	 * eleResponse.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_4,
	 * paymentInputStruct.paymentReference4); //END SOF: WN-2088 :Add a SOF
	 * Identifier to the Settlement Failure alert
	 * 
	 * raiseAlertForSettlementFailure(env,eleCentinelResponse,docResponse); } }
	 * 
	 * //END- STL-1121 - In case the settlement call to Cardinal fails, Sterling
	 * should mark the charge request as paid and generate T-log.
	 * 
	 *//**
		 * @param paymentInputStruct
		 * @return paymentOutputStruct
		 * 
		 *         Constructing the paymentOutputStruct, for ChargeType "CHARGE"
		 */
	/*
	 * private YFSExtnPaymentCollectionOutputStruct
	 * constructBasicCollectionPayPalOutputForCharge(
	 * YFSExtnPaymentCollectionInputStruct paymentInputStruct) {
	 * 
	 * log .beginTimer("constructBasicCollectionPayPalOutputForCharge");
	 * YFSExtnPaymentCollectionOutputStruct paymentOutputStruct = new
	 * YFSExtnPaymentCollectionOutputStruct();
	 * 
	 * paymentOutputStruct.authorizationId = paymentInputStruct.authorizationId;
	 * paymentOutputStruct.requestID = paymentInputStruct.authorizationId;
	 * paymentOutputStruct.tranAmount = paymentInputStruct.requestAmount;
	 * paymentOutputStruct.authorizationAmount = paymentInputStruct.requestAmount;
	 * paymentOutputStruct.tranType = "CHARGE"; paymentOutputStruct.tranReturnCode =
	 * "100";
	 * 
	 * log .endTimer("constructBasicCollectionPayPalOutputForCharge"); return
	 * paymentOutputStruct; }
	 * 
	 *//**
		 * @param env
		 * @param paymentInputStruct
		 * @return paymentOutputStruct
		 * 
		 *         Forming the Request Message for centinel API called
		 *         "cmpi_reauthorize" and getting the response. BAsed on the response,
		 *         Doing the re-auth and forming the Output Struct.
		 * @throws ParserConfigurationException
		 */
	/*
	 * private YFSExtnPaymentCollectionOutputStruct invokeCardinalReAuthService(
	 * YFSEnvironment env, YFSExtnPaymentCollectionInputStruct paymentInputStruct)
	 * throws Exception {
	 * 
	 * log .beginTimer("invokeCardinalReAuthService");
	 * 
	 * CentinelRequest centinelRequest = null; CentinelResponse centinelResponse =
	 * new CentinelResponse();
	 * 
	 * centinelRequest = getHTTPDocForAuthRequest(paymentInputStruct);
	 * 
	 * //if CentinelRequest is not blank if (!YFCObject.isVoid(centinelRequest)) {
	 * 
	 * System.setProperty("javax.net.ssl.trustStore", YFSSystem
	 * .getProperty(AcademyConstants.KEY_STORE_PAYPAL));
	 * System.setProperty("javax.net.ssl.trustStorePassword", YFSSystem
	 * .getProperty(AcademyConstants.KEY_PASSWORD_PAYPAL));
	 * 
	 * log.debug("invokeCardinalReAuthService :: Cardinal Request --> " +
	 * centinelRequest.getFormattedRequest());
	 * 
	 * //making HTTP call to get the Centinel Response centinelResponse =
	 * centinelRequest.sendHTTP(YFSSystem
	 * .getProperty(AcademyConstants.CARDIANL_PAYPAL_ENDPOINT)); }
	 * 
	 * //formatting the cetinel response String response =
	 * centinelResponse.getFormattedResponse();
	 * 
	 * log.debug("invokeCardinalReAuthService :: Cardinal Response --> " +
	 * centinelResponse.getFormattedResponse());
	 * 
	 * response = response.replaceAll("&", "&amp;");
	 * 
	 * Document centinelResponseDoc = XMLUtil.getDocument(response);
	 * 
	 * log .debug("invokeCardinalReAuthService :: Response Doc is" +
	 * XMLUtil.getXMLString(centinelResponseDoc));
	 * 
	 * YFSExtnPaymentCollectionOutputStruct paymentOutputStruct =
	 * translateCentinelResponse( env, paymentInputStruct, centinelResponseDoc);
	 * 
	 * return paymentOutputStruct; }
	 * 
	 * 
	 * This method will set the required attributes to form the Centinel Request for
	 * PayPal
	 * 
	 * //WN-697 Added env private Document prepareCentinelRequestDoc(YFSEnvironment
	 * env, YFSExtnPaymentCollectionInputStruct paymentInputStruct) throws Exception
	 * { log.verbose("Start prepareCentinelRequestDoc()"); Document centinelReqInDoc
	 * = XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT); Element
	 * eleCentinelReq = centinelReqInDoc.getDocumentElement();
	 * eleCentinelReq.setAttribute(AcademyConstants.ATTR_CHARGE_TYPE,
	 * paymentInputStruct.chargeType);
	 * eleCentinelReq.setAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT,
	 * String.valueOf(paymentInputStruct.requestAmount));
	 * eleCentinelReq.setAttribute(AcademyConstants.CREDIT_CARD_NO,
	 * paymentInputStruct.creditCardNo);
	 * eleCentinelReq.setAttribute(AcademyConstants.ATTR_PAYMENT_TYPE,
	 * paymentInputStruct.paymentType);
	 * eleCentinelReq.setAttribute(AcademyConstants.ATTR_BILL_TO_EMAILID,
	 * paymentInputStruct.billToEmailId);
	 * eleCentinelReq.setAttribute(AcademyConstants.ATTR_BILL_TO_FIRST_NAME,
	 * paymentInputStruct.billToFirstName);
	 * eleCentinelReq.setAttribute(AcademyConstants.ATTR_BILL_TO_LAST_NAME,
	 * paymentInputStruct.billToLastName);
	 * eleCentinelReq.setAttribute(AcademyConstants.ATTR_BILL_TO_ADDRLINE1,
	 * paymentInputStruct.billToAddressLine1);
	 * eleCentinelReq.setAttribute(AcademyConstants.ATTR_BILL_TO_CITY,
	 * paymentInputStruct.billToCity);
	 * eleCentinelReq.setAttribute(AcademyConstants.ATTR_BILL_TO_STATE,
	 * paymentInputStruct.billToState);
	 * eleCentinelReq.setAttribute(AcademyConstants.ATTR_BILL_TO_ZIPCODE,
	 * paymentInputStruct.billToZipCode);
	 * eleCentinelReq.setAttribute(AcademyConstants.ATTR_BILL_TO_COUNTRY,
	 * paymentInputStruct.billToCountry);
	 * 
	 * //Start WN-697 : Sterling to consume special characters and include them in
	 * customer-facing emails, but to remove them before settlement.
	 * AcademyUtil.convertUnicodeToSpecialChar(env, eleCentinelReq, null, true);
	 * //End WN-697 : Sterling to consume special characters and include them in
	 * customer-facing emails, but to remove them before settlement.
	 * 
	 * log.verbose("End prepareCentinelRequestDoc()::Output::"+XMLUtil.getXMLString(
	 * centinelReqInDoc)); return centinelReqInDoc;
	 * 
	 * }
	 *//**
		 * @param paymentInputStruct
		 * @return centinelRequest
		 * @throws Exception
		 * 
		 *                   Forming the request message for Centinel API
		 *                   "cmpi_reauthorize".
		 */
	/*
	 * private CentinelRequest
	 * getHTTPDocForAuthRequest(YFSExtnPaymentCollectionInputStruct
	 * paymentInputStruct)throws Exception {
	 * log.beginTimer("getHTTPDocForAuthRequest"); CentinelRequest centinelRequest =
	 * new CentinelRequest(); //moving the below method call after getting the
	 * reqAmt //setCommonAttributesForRequest(centinelRequest); //moving to
	 * setCommonAttributesForRequest method double reqAmt =
	 * paymentInputStruct.requestAmount; String reqAmount = String.valueOf(reqAmt);
	 * reqAmount = reqAmount.replace(".", "");
	 * 
	 * setCommonAttributesForRequest(centinelRequest,paymentInputStruct);
	 * 
	 * //set Cardinal OrderID for centinel request
	 * centinelRequest.add(AcademyConstants.CARDINAL_REQ_PAYPAL_ORDER_ID,
	 * paymentInputStruct.creditCardNo);
	 * 
	 * //set PayPal amount to Centinel request
	 * centinelRequest.add(AcademyConstants.CARDINAL_REQ_PAYPAL_AMOUNT, reqAmount);
	 * 
	 * //START - adding mandatory fields to centinel PayPal Request
	 * centinelRequest.add(AcademyConstants.CARDINAL_PAYPAL_REQ_EMAIL_ID,
	 * paymentInputStruct.billToEmailId);
	 * centinelRequest.add(AcademyConstants.CARDINAL_PAYPAL_REQ_BILLING_FIRST_NAME,
	 * paymentInputStruct.billToFirstName);
	 * centinelRequest.add(AcademyConstants.CARDINAL_PAYPAL_REQ_BILLING_LAST_NAME,
	 * paymentInputStruct.billToLastName);
	 * centinelRequest.add(AcademyConstants.CARDINAL_PAYPAL_REQ_BILLING_ADDRESS_1,
	 * paymentInputStruct.billToAddressLine1);
	 * centinelRequest.add(AcademyConstants.CARDINAL_PAYPAL_REQ_BILLING_CITY,
	 * paymentInputStruct.billToCity);
	 * centinelRequest.add(AcademyConstants.CARDINAL_PAYPAL_REQ_BILLING_STATE,
	 * paymentInputStruct.billToState);
	 * centinelRequest.add(AcademyConstants.CARDINAL_PAYPAL_REQ_BILLING_POSTAL_CODE,
	 * paymentInputStruct.billToZipCode); centinelRequest.add(AcademyConstants.
	 * CARDINAL_PAYPAL_REQ_BILLING_COUNTRY_CODE, paymentInputStruct.billToCountry);
	 * //END - adding mandatory fields to centinel PayPal Request
	 * 
	 * log.endTimer("getHTTPDocForAuthRequest"); return centinelRequest; }
	 * 
	 *//**
		 * @param centinelRequest
		 * @param inDoc
		 * @throws Exception
		 * 
		 *                   setting the common attributes for Request message for
		 *                   centinel API.
		 */
	/*
	 * private void setCommonAttributesForRequest(CentinelRequest
	 * centinelRequest,YFSExtnPaymentCollectionInputStruct paymenInputStruct) throws
	 * Exception {
	 * 
	 * log .beginTimer("setCommonAttributesForRequest"); String chargeType =
	 * paymenInputStruct.chargeType; double reqAmt =
	 * paymenInputStruct.requestAmount; String reqAmount = String.valueOf(reqAmt);
	 * reqAmount = reqAmount.replace(".", ""); //set PayPal amount to Centinel
	 * request centinelRequest.add(AcademyConstants.CARDINAL_REQ_PAYPAL_AMOUNT,
	 * reqAmount);
	 * 
	 * if("AUTHORIZATION".equals(chargeType)){
	 * centinelRequest.add(AcademyConstants.CARDINAL_REQ_MESSAGE_TYPE,
	 * AcademyConstants.CARDINAL_REQ_MESSAGE_TYPE_VALUE); }
	 * 
	 * else if ("CHARGE".equals(chargeType)){ if(reqAmount.startsWith("-")){
	 * centinelRequest.add(AcademyConstants.CARDINAL_REQ_MESSAGE_TYPE,
	 * AcademyConstants.CARDINAL_REQ_MESSAGE_TYPE_VALUE_REFUND); }else{
	 * centinelRequest.add(AcademyConstants.CARDINAL_REQ_MESSAGE_TYPE,
	 * AcademyConstants.CARDINAL_REQ_MESSAGE_TYPE_VALUE_SETTLEMENT); } } //set
	 * attribute Message Type to Centinel Request
	 * centinelRequest.add(AcademyConstants.CARDINAL_REQ_PAYPAL_MESSAGE_TYPE,
	 * AcademyConstants.CARDINAL_PAYPAL_MESSAGE_TYPE_VALUE);
	 * 
	 * //set attribute Version to Centinel Request centinelRequest .add(
	 * AcademyConstants.CARDINAL_REQ_PAYPAL_VERSION, YFSSystem
	 * .getProperty(AcademyConstants.CARDINAL_PAYPAL_VERSION_VALUE));
	 * 
	 * //set attribute Processor ID to Centinel Request centinelRequest .add(
	 * AcademyConstants.CARDINAL_REQ_PAYPAL_PROCESSORID, YFSSystem
	 * .getProperty(AcademyConstants.CARDINAL_PAYPAL_PROCESSORID_VALUE));
	 * 
	 * //set attribute Merchant ID to Centinel Request centinelRequest .add(
	 * AcademyConstants.CARDINAL_REQ_PAYPAL_MERCHANTID, YFSSystem
	 * .getProperty(AcademyConstants.CARDINAL_PAYPAL_MERCHANTID_VALUE));
	 * 
	 * //set attribute Transaction Password to Centinel Request centinelRequest
	 * .add( AcademyConstants.CARDINAL_REQ_PAYPAL_TRANSCATION_PWD, YFSSystem
	 * .getProperty(AcademyConstants.CARDINAL_PAYPAL_TRANSCATION_PWD_VALUE));
	 * 
	 * //set attribute Transaction Type to Centinel Request centinelRequest .add(
	 * AcademyConstants.CARDINAL_REQ_PAYPAL_TRANSCATION_TYPE, YFSSystem
	 * .getProperty(AcademyConstants.CARDINAL_PAYPAL_TRANSCATION_TYPE_VALUE));
	 * 
	 * //set attribute Currency Code to Centinel Request
	 * centinelRequest.add(AcademyConstants.CARDINAL_REQ_PAYPAL_CURRENCY_CODE,
	 * AcademyConstants.CARDINAL_PAYPAL_CURRENCY_CODE_VALUE);
	 * 
	 * log .endTimer("setCommonAttributesForRequest"); }
	 * 
	 *//**
		 * @param env
		 * @param paymentInputStruct
		 * @param response
		 * @return paymentOutputStruct
		 * @throws Exception
		 * 
		 *                   translating the Centinel response based on StatusCode.and
		 *                   forming the OutStruct according to that. Possible
		 *                   StatusCodes are "Y", "U", "P", "E".
		 */
	/*
	 * public YFSExtnPaymentCollectionOutputStruct
	 * translateCentinelResponse(YFSEnvironment
	 * env,YFSExtnPaymentCollectionInputStruct paymentInputStruct,Element response)
	 * throws Exception {
	 * 
	 * log.beginTimer("translateCentinelResponse"); String strReasonCode = null;
	 * YFSExtnPaymentCollectionOutputStruct paymentOutputStruct = new
	 * YFSExtnPaymentCollectionOutputStruct();
	 * 
	 * //Fetch NodeList of Node PaymentMethod from the input xml NodeList
	 * statusCodeEle = (NodeList) response.getElementsByTagName("StatusCode"); //If
	 * <StatusCode/> is there if(statusCodeEle.getLength()!= 0) { //if Status code
	 * is not "Y" if
	 * (!"Y".equals(response.getElementsByTagName("StatusCode").item(0).
	 * getTextContent())) {
	 * 
	 * log.debug("translateCentinelResponse :: Status Code is not Y");
	 * 
	 * 
	 * //If statusCode is "E"
	 * if("E".equals(response.getElementsByTagName("StatusCode").item(0).
	 * getTextContent())) {
	 * 
	 * log.
	 * debug("translateCentinelResponse :: Status Code is E and suspend payemtn is Y"
	 * ); //START : STL-1243 Paypal reauths failing even on Day 8 at 1:49 AM CST
	 * strReasonCode =
	 * response.getElementsByTagName(AcademyConstants.ATTR_REASON_CODE_PP).item(0).
	 * getTextContent(); //below method is called to get the attributes needed for
	 * response validation setValidationParameters(env);
	 * log.verbose("strReasonCode: "+strReasonCode);
	 * log.verbose("strAuthRetryErrorCodes: "+strAuthRetryErrorCodes);
	 * if(strAuthRetryErrorCodes.contains(strReasonCode)){
	 * retryAuthorization(response, paymentOutputStruct,
	 * strNextTriggerIntervalInMinAuthError); } else{
	 * paymentOutputStruct.authorizationAmount = 0.0D; paymentOutputStruct.retryFlag
	 * = "N"; paymentOutputStruct.authReturnMessage =
	 * response.getElementsByTagName("ErrorDesc").item(0).getTextContent();
	 * paymentOutputStruct.suspendPayment = "Y"; } //END : STL-1243 Paypal reauths
	 * failing even on Day 8 at 1:49 AM CST } else{ //STL-1243 Paypal reauths
	 * failing even on Day 8 at 1:49 AM CST. //Moved the retry logic to method level
	 * retryAuthorization(response, paymentOutputStruct,null); }
	 * 
	 * processCollectionFailure(env, paymentInputStruct,paymentOutputStruct,
	 * response); } //If status code is "Y" else {
	 * 
	 * log.debug("translateCentinelResponse :: Status Code is Y");
	 * paymentOutputStruct =
	 * translateCentinelResponseForAuth(env,paymentInputStruct, response); } } //if
	 * Centinel Response is Error Response throwing the Exception else {
	 * 
	 * Document doc = XMLUtil.createDocument("Error");
	 * paymentOutputStruct.authReturnMessage =
	 * response.getElementsByTagName("ErrorDesc").item(0).getTextContent(); Element
	 * eleRoot = doc.getDocumentElement(); eleRoot.setAttribute("ErrorCode",
	 * response.getElementsByTagName("ErrorNo").item(0).getTextContent());
	 * eleRoot.setAttribute("ErrorDescription",
	 * response.getElementsByTagName("ErrorDesc").item(0).getTextContent());
	 * log.verbose("Error Doc ::" + XMLUtil.getXMLString(doc)); throw
	 * AcademyPaymentProcessingUtil.getYFSExceptionWithErrorCode(doc); }
	 * log.endTimer("translateCentinelResponse"); return paymentOutputStruct; }
	 * //STL-1243 Paypal reauths failing even on Day 8 at 1:49 AM CST
	 *//**
		 * Set retry flag and collection date
		 * 
		 * @param response
		 * @param paymentOutputStruct
		 */
	/*
	 * private void retryAuthorization(Element response,
	 * YFSExtnPaymentCollectionOutputStruct paymentOutputStruct, String
	 * strNextTriggerIntervalInMin) throws Exception{
	 * paymentOutputStruct.authorizationAmount = 0.0D; paymentOutputStruct.retryFlag
	 * = "Y"; paymentOutputStruct.authReturnMessage =
	 * response.getElementsByTagName("ErrorDesc").item(0).getTextContent();
	 * 
	 * if(!YFSObject.isVoid(strNextTriggerIntervalInMin)){
	 * paymentOutputStruct.collectionDate =
	 * AcademyPaymentProcessingUtil.getCollectionDate(strNextTriggerIntervalInMin);
	 * }
	 * 
	 * }
	 * 
	 *//**
		 * @param env
		 * @param paymentInputStruct
		 * @param response
		 * @return paymentOutputStruct
		 * @throws Exception
		 * 
		 *                   In the centinel Response, StatusCode is "Y" then processing
		 *                   for re-auth. Extending the AuthorizationExpirationDate to
		 *                   "Calenderdate+3days"
		 */
	/*
	 * private YFSExtnPaymentCollectionOutputStruct
	 * translateCentinelResponseForAuth(YFSEnvironment
	 * env,YFSExtnPaymentCollectionInputStruct paymentInputStruct,Element response)
	 * throws Exception {
	 * 
	 * log.beginTimer("translateCentinelResponseForAuth");
	 * 
	 * YFSExtnPaymentCollectionOutputStruct paymentOutputStruct = new
	 * YFSExtnPaymentCollectionOutputStruct();
	 * 
	 * //If status Code is "Y" if
	 * ("Y".equals(response.getElementsByTagName("StatusCode").item(0)
	 * .getTextContent())) {
	 * 
	 * //get attribute Authorization Code String authId =
	 * response.getElementsByTagName("AuthorizationCode").item(0).getTextContent();
	 * 
	 * //set the attributes paymentOutputStruct.authorizationId = authId;
	 * paymentOutputStruct.authorizationAmount = paymentInputStruct.requestAmount;
	 * 
	 * //create calendat instance to get the Current Date Calendar cal =
	 * Calendar.getInstance();
	 * 
	 * SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	 * 
	 * paymentOutputStruct.authTime = sdf.format(cal.getTime());
	 * paymentOutputStruct.requestID = authId; paymentOutputStruct.tranAmount =
	 * paymentInputStruct.requestAmount;
	 * 
	 * //START : STL-1163 Commented out as part of STL-1163 Calendar cal1 =
	 * Calendar.getInstance(); SimpleDateFormat sdf1 = new
	 * SimpleDateFormat("yyyyMMddHHmmss");
	 * 
	 * int authExpdays = callGetAuthExpDays(env); log
	 * .debug("translateCentinelResponseForAuth :: Auth Exp days from Common Code is ---> "
	 * + authExpdays);
	 * 
	 * //adding the authExpdays(7 days ) for current date cal1.add(Calendar.DATE,
	 * authExpdays); String strExpirationDate = sdf1.format(cal1.getTime());
	 * 
	 * Calendar cal1 = Calendar.getInstance(); SimpleDateFormat sdf1 = new
	 * SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); String strExpirationDate =
	 * AcademyPaymentProcessingUtil.getAuthorizationExpirationDateForPayPall(env,
	 * sdf1.format(cal1.getTime())); strExpirationDate =
	 * strExpirationDate.replace("-", "").replace("T", "").replace(":", ""); //END :
	 * START : STL-1163 paymentOutputStruct.authorizationExpirationDate =
	 * strExpirationDate;
	 * 
	 * //START STL-1337 : store AuthCode received from cardinal
	 * paymentOutputStruct.authCode = authId; //END STL-1337 : store AuthCode
	 * received from cardinal
	 * 
	 * }
	 * 
	 * log .endTimer("translateCentinelResponseForAuth"); return
	 * paymentOutputStruct; }
	 * 
	 *//**
		 * @param env
		 * @return
		 * @throws Exception
		 * 
		 *                   getting the Auth Expiration Days, by calling the get
		 *                   CommonCodeList.
		 */
	/*
	 * private int callGetAuthExpDays(YFSEnvironment env) throws Exception {
	 * 
	 * //create Document with CommonCode Document getCommonCodeListinDoc =
	 * XMLUtil.createDocument("CommonCode");
	 * 
	 * //set attribute CodeType
	 * getCommonCodeListinDoc.getDocumentElement().setAttribute("CodeType",
	 * "PP_AUTHEXP_DAYS");
	 * 
	 * //set attribute OrganizationCode
	 * getCommonCodeListinDoc.getDocumentElement().setAttribute("OrganizationCode",
	 * "DEFAULT");
	 * 
	 * //calling getCommonCodeList API with input Document getCommonCodeListoutDoc =
	 * api.getCommonCodeList(env,getCommonCodeListinDoc);
	 * 
	 * //get Code Value from output xml String strAuthExpDays = ((Element)
	 * getCommonCodeListoutDoc.getElementsByTagName("CommonCode").item(0)).
	 * getAttribute("CodeValue");
	 * 
	 * int authExpDays = Integer.parseInt(strAuthExpDays);
	 * 
	 * log.debug("callGetAuthExpDays :: AuthExpDays are --> " + authExpDays);
	 * 
	 * return authExpDays; }
	 * 
	 *//**
		 * @param env
		 * @param paymentInputStruct
		 * @param paymentOutputStruct
		 * @param response
		 * @throws Exception
		 * 
		 *                   Raising the alerts, by calling the OOB Services for Failure
		 *                   Scenarios.
		 */
	/*
	 * private void processCollectionFailure(YFSEnvironment
	 * env,YFSExtnPaymentCollectionInputStruct
	 * paymentInputStruct,YFSExtnPaymentCollectionOutputStruct
	 * paymentOutputStruct,Element response) throws Exception {
	 * 
	 * log.beginTimer("processCollectionFailure");
	 * 
	 * String orderHeaderKey = paymentInputStruct.orderHeaderKey;
	 * 
	 * //create Document Order Document getOrderListinDoc =
	 * XMLUtil.createDocument("Order");
	 * 
	 * //set attribute OrderHeaderKey
	 * getOrderListinDoc.getDocumentElement().setAttribute("OrderHeaderKey",
	 * orderHeaderKey);
	 * 
	 * log.
	 * debug("processCollectionFailure :: Input XML for AcademyPaypalCollectionFailure --> "
	 * + XMLUtil.getXMLString(getOrderListinDoc));
	 * 
	 * //executing service AcademyPaypalCollectionFailure Document
	 * getOrderListoutDoc = api.executeFlow(env, "AcademyPaypalCollectionFailure",
	 * getOrderListinDoc);
	 * 
	 * log.
	 * debug("processCollectionFailure :: Output XML for AcademyPaypalCollectionFailure --> "
	 * + XMLUtil.getXMLString(getOrderListoutDoc));
	 * 
	 * Element orderEle = (Element) getOrderListoutDoc.getElementsByTagName(
	 * "Order").item(0);
	 * 
	 * Element modifiedEle = (Element) getOrderListoutDoc.importNode(orderEle,
	 * true); Document modifiedoutDoc = XMLUtil.getDocumentForElement(modifiedEle);
	 * 
	 * Element root = modifiedoutDoc.getDocumentElement();
	 * 
	 * Element failureDetails = modifiedoutDoc
	 * .createElement("CollectionFailureDetails");
	 * 
	 * //if Status Code is U if
	 * ("U".equals(response.getElementsByTagName("StatusCode").item(0)
	 * .getTextContent())) { failureDetails.setAttribute("FailureReasonCode",
	 * "SERVICE_UNAVAILABLE"); } else {
	 * failureDetails.setAttribute("FailureReasonCode", "HARD_DECLINED"); }
	 * 
	 * //set attributes
	 * failureDetails.setAttribute("CreditCardExpirationDate",paymentInputStruct.
	 * creditCardExpirationDate);
	 * failureDetails.setAttribute("CreditCardName",paymentInputStruct.
	 * creditCardName);
	 * failureDetails.setAttribute("CreditCardNo",paymentInputStruct.creditCardNo);
	 * //Start :: Host Capture Changes failureDetails.setAttribute("CreditCardType",
	 * paymentInputStruct.creditCardType);
	 * failureDetails.setAttribute("CreditCardType",AcademyConstants.PAYPAL); // End
	 * :: Host Capture Changes
	 * 
	 * failureDetails.setAttribute("PaymentType",paymentInputStruct.paymentType);
	 * failureDetails.setAttribute("RequestAmount",
	 * String.valueOf(paymentInputStruct.requestAmount));
	 * failureDetails.setAttribute("SvcNo", paymentInputStruct.svcNo);
	 * 
	 * failureDetails.setAttribute("ChargeType", paymentInputStruct.chargeType);
	 * failureDetails.setAttribute("PaymentReference1",paymentInputStruct.
	 * paymentReference1);
	 * failureDetails.setAttribute("PaymentReference2",paymentInputStruct.
	 * paymentReference2);
	 * failureDetails.setAttribute("PaymentReference3",paymentInputStruct.
	 * paymentReference3);
	 * 
	 * failureDetails.setAttribute("AuthReturnCode",paymentOutputStruct.
	 * authReturnCode);
	 * failureDetails.setAttribute("AuthReturnMessage",paymentOutputStruct.
	 * authReturnMessage);
	 * failureDetails.setAttribute("AuthReturnFlag",paymentOutputStruct.
	 * authReturnFlag); root.appendChild(failureDetails);
	 * 
	 * log.
	 * debug("processCollectionFailure :: Input XML for YCD_ProcessCollectionFailure_1.0 --> "
	 * + XMLUtil.getXMLString(modifiedoutDoc));
	 * 
	 * //executing the OOB service YCD_ProcessCollectionFailure_1.0
	 * api.executeFlow(env, "YCD_ProcessCollectionFailure_1.0",modifiedoutDoc);
	 * 
	 * log.endTimer("processCollectionFailure");
	 * 
	 * }
	 *//**
		 * This method will raise alert for status code other than Y
		 *
		 */
	/*
	 * public void raiseAlertForSettlementFailure(YFSEnvironment env,Element
	 * eleCentinelResponse,Document docResponse) throws Exception {
	 * log.verbose("Rasing alert for Settlement error"); //Below logic is added to
	 * add all response attribute for settlement failure alert Element
	 * eleCardinalMPI = eleCentinelResponse; NodeList nlCardinalMPIChilds =
	 * eleCardinalMPI.getChildNodes(); Node eleChild = null; for (int iChildCount =
	 * 0; iChildCount < nlCardinalMPIChilds.getLength(); iChildCount++) { eleChild =
	 * nlCardinalMPIChilds.item(iChildCount); if((eleChild.getNodeType()==1)){
	 * docResponse.getDocumentElement().setAttribute(eleChild.getNodeName(),
	 * eleChild.getTextContent()); } }
	 * log.verbose("Input to alert service is::"+XMLUtil.getXMLString(docResponse));
	 * AcademyUtil.invokeService(env,
	 * AcademyConstants.SERV_SETTLEMENT_FAILURE_ALERT, docResponse); }
	 * 
	 *//**
		 * This method prepares a new environment object to invoke for lower java
		 * versions
		 * 
		 * @param inDoc
		 * @return
		 * @throws Exception
		 *//*
			 * private YFSEnvironment createEnvironment(String progID) throws Exception {
			 * log.verbose("Inside createEnvironment"); String connProtocol =
			 * YFSSystem.getProperty(AcademyConstants.PROP_CARDNL_RETRY_ENVIRONMENT_PROTOCOL
			 * ); //String envURL =
			 * "http://host_name:port/smcfs/interop/InteropHttpServlet"; String envURL =
			 * YFSSystem.getProperty(AcademyConstants.PROP_CARDNL_RETRY_ENVIRONMENT); String
			 * strUserId = YFSSystem.getProperty(AcademyConstants.
			 * PROP_CARDNL_RETRY_ENABLED_USING_APP_USERID); String strPassword =
			 * YFSSystem.getProperty(AcademyConstants.
			 * PROP_CARDNL_RETRY_ENABLED_USING_APP_PASSWORD);
			 * 
			 * HashMap<String, String> envProps = new HashMap<String, String>();
			 * 
			 * envProps.put("yif.httpapi.url", envURL); newApi =
			 * YIFClientFactory.getInstance().getApi(connProtocol, envProps);
			 * 
			 * Document doc = XMLUtil.createDocument("YFSEnvironment"); Element elem =
			 * doc.getDocumentElement(); elem.setAttribute(AcademyConstants.ATTR_USR_ID,
			 * strUserId); elem.setAttribute(AcademyConstants.STR_PASSWORD, strPassword);
			 * elem.setAttribute(AcademyConstants.ATTR_PROG_ID, progID); YFSEnvironment env
			 * = newApi.createEnvironment(doc);
			 * 
			 * log.verbose("Exit createEnvironment"); return env;
			 * 
			 * }
			 */
			 
	/*
	 * End : OMNI-647
	 */		 

}
