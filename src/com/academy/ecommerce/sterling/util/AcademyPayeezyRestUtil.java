package com.academy.ecommerce.sterling.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.json.JSONObject;
import org.apache.commons.json.utils.XML;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSObject;


public class AcademyPayeezyRestUtil {

	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPayeezyRestUtil.class);

	public static Document invokePayeezyRestService(String strJsonInput, String strURL, Map<String, String> restParameters) throws ParserConfigurationException{
		log.verbose("Begin AcademyPayeezyRestUtil.invokePayeezyRestService() ");
		log.verbose("AcademyPayeezyRestUtil.invokePayeezyRestService() JSON Input :: \n "+strJsonInput);

		Document docResponse = null;

		try {
			byte[] buffer = new byte[strJsonInput.length()];
			buffer = strJsonInput.getBytes();
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			bout.write(buffer);

			restParameters = AcademyHmacGenerationForPayeezy.getSecurityKeys(restParameters,strJsonInput);

			URL url = new URL(strURL);
			HttpsURLConnection conn;
			String strProxyHost = restParameters.get(AcademyConstants.WEBSERVICE_PROXY_HOST);
			String strProxyPort = restParameters.get(AcademyConstants.WEBSERVICE_PROXY_PORT);

			Proxy proxy;
			
			//Start : OMNI-1435 : Changes for Payeezy Proxy enabling/disabling
			String strIsProxyEnabled = restParameters.get(AcademyConstants.PROP_ENABLE_PROXY);
			log.verbose("\n strIsProxyEnabled : " +strIsProxyEnabled);

			if(!YFCObject.isVoid(strIsProxyEnabled) && strIsProxyEnabled.equals("Y")){
				log.verbose("Connecting PayZ through proxy:: strProxyHost:"+strProxyHost+" :: strProxyPort:"+strProxyPort + " :: strEndpointURL:"+strURL);
				proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(strProxyHost, Integer.parseInt(strProxyPort))); 
				conn = (HttpsURLConnection) url.openConnection(proxy);
			}
			else {
				log.verbose("Connecting PayZ directly without Proxy :: strEndpointURL:"+strURL);
				conn = (HttpsURLConnection) url.openConnection();
			}
			//End : OMNI-1435 : Changes for Payeezy Proxy enabling/disabling
			

			//System.setProperty(AcademyConstants.WEBSERVICE_HTTPS_PROXY_HOST, strProxyHost);
			//System.setProperty(AcademyConstants.WEBSERVICE_HTTPS_PROXY_PORT, strProxyPort);
			
			//Start : OMNI-1435 : Changes for Payeezy Proxy enabling/disabling
			//proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(strProxyHost, Integer.parseInt(strProxyPort)));        
			//conn = (HttpsURLConnection) url.openConnection(proxy);
			//End : OMNI-1435 : Changes for Payeezy Proxy enabling/disabling
			
			conn.setDoOutput(true);
			conn.setRequestMethod(AcademyConstants.WEBSERVICE_POST); 
			conn.setRequestProperty(AcademyConstants.WEBSERVICE_CONTENT_TYPE, AcademyConstants.ACCEPT_APPLICATION_JSON);
			conn.setRequestProperty(AcademyConstants.WEBSERVICE_APIKEY, restParameters.get(AcademyConstants.WEBSERVICE_APIKEY));
			conn.setRequestProperty(AcademyConstants.WEBSERVICE_AUTHORIZATION, restParameters.get(AcademyConstants.WEBSERVICE_AUTHORIZATION));
			conn.setRequestProperty(AcademyConstants.WEBSERVICE_TOKEN, restParameters.get(AcademyConstants.WEBSERVICE_TOKEN));

			conn.setRequestProperty(AcademyConstants.WEBSERVICE_NONCE, restParameters.get(AcademyConstants.WEBSERVICE_NONCE));
			conn.setRequestProperty(AcademyConstants.WEBSERVICE_TIMESTAMP, restParameters.get(AcademyConstants.WEBSERVICE_TIMESTAMP));
			
			String strConnectionTimeout = restParameters.get(AcademyConstants.ATTR_CONNECTION_TIMEOUT);
			String strReadTimeout = restParameters.get(AcademyConstants.ATTR_RESPONSE_TIMEOUT);
			
			if(!YFSObject.isVoid(strConnectionTimeout)){
				int iConnectionTimeout = Integer.parseInt(strConnectionTimeout);
				conn.setConnectTimeout(iConnectionTimeout);
			}
			if(!YFSObject.isVoid(strReadTimeout)){
				int iReadTimeout = Integer.parseInt(strReadTimeout);
				conn.setReadTimeout(iReadTimeout);
			}
			try {
				log.verbose("URL being used... --->"+conn.getURL().toString());
				log.verbose("Input used... --->"+strJsonInput);
				DataOutputStream outDataStream = new DataOutputStream(conn.getOutputStream());
				outDataStream.writeBytes(strJsonInput);
				outDataStream.flush();
				outDataStream.close();
				
				int iResponseCode = conn.getResponseCode();
				log.verbose("iResponseCode... --->  "+iResponseCode);
				log.verbose("output... --->  "+outDataStream.toString());

				if(iResponseCode==200 || iResponseCode==201 || iResponseCode==202){
					log.verbose("calling... ---> SUCCESS");
				}else{
					log.verbose("calling... failure ---> Response code("+conn.getResponseCode()+")");
					log.verbose(conn.getResponseMessage());
				}
				docResponse = formatPayeezyResponse(conn);
				docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, Integer.toString(iResponseCode));
				docResponse.getDocumentElement().setAttribute(AcademyConstants.JSON_ATTR_REVERSAL_ID, 
						restParameters.get(AcademyConstants.WEBSERVICE_REVERSAL_ID));

			}
			//Updated the catch block to create Splunk Alerts
			catch (SocketTimeoutException e) {
				log.info("Payeezy Exception:: SocketTimeoutException :: "+e.toString()+" Exception Stack Trace"+e.getStackTrace().toString());
				docResponse = XMLUtil.createDocument("json_object");
				docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, AcademyConstants.STATUS_CODE_504);
				docResponse.getDocumentElement().setAttribute(AcademyConstants.JSON_ATTR_REVERSAL_ID, 
						restParameters.get(AcademyConstants.WEBSERVICE_REVERSAL_ID));
			} catch (SSLHandshakeException e) {
				log.info("Payeezy Exception:: SSLHandshakeException :: "+e.toString()+" Exception Stack Trace"+e.getStackTrace().toString());
				docResponse = XMLUtil.createDocument("json_object");
				docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, AcademyConstants.STATUS_CODE_504);
				docResponse.getDocumentElement().setAttribute(AcademyConstants.JSON_ATTR_REVERSAL_ID, 
						restParameters.get(AcademyConstants.WEBSERVICE_REVERSAL_ID));
			} catch (Exception e) {
				log.info("Payeezy Exception:: Generic Exception :: "+e.toString()+" Exception Stack Trace"+e.getStackTrace().toString());
				docResponse = XMLUtil.createDocument("json_object");
				docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, AcademyConstants.STATUS_CODE_504);
				docResponse.getDocumentElement().setAttribute(AcademyConstants.JSON_ATTR_REVERSAL_ID, 
						restParameters.get(AcademyConstants.WEBSERVICE_REVERSAL_ID));
			}
		}
		catch (Exception e) {
			log.info("Payeezy Exception:: Before Webservice Invocation :: "+e.toString()+" Exception Stack Trace"+e.getStackTrace().toString());
			docResponse = XMLUtil.createDocument("json_object");
			docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, AcademyConstants.STATUS_CODE_504);
			docResponse.getDocumentElement().setAttribute(AcademyConstants.JSON_ATTR_REVERSAL_ID, 
					restParameters.get(AcademyConstants.WEBSERVICE_REVERSAL_ID));
		}
		
		log.verbose("End AcademyPayeezyRestUtil.invokePayeezyRestService() ");
		return docResponse;

	}


	/**This method prepares the PayZ request in REST format for Credit Cards
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public static String createPAYZRestInputForCCAuth(Document inDoc, String strTransactionType, String strSplitShipment, 
			Map<String, String> restParameters) throws Exception {

		log.verbose("Begin of AcademyPayeezyRestUtil.createPAYZRestInputForCCAuth() method");
		String strJSONInput = null;

		String strCurrency = XPathUtil.getString(inDoc, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CURRENCY);
		String strRequestAmount = XPathUtil.getString(inDoc, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);

		//Updating request amount in format that can be read by PAyeezy 
		DecimalFormat df = new DecimalFormat("#"); 
		strRequestAmount = df.format((Double.parseDouble(strRequestAmount)*100));

		String strCreditCardType = XPathUtil.getString(inDoc, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CREDIT_CARD_TYPE);
		String strCreditCardNo = XPathUtil.getString(inDoc, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.CREDIT_CARD_NO);
		String strFirstName = XPathUtil.getString(inDoc, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_FNAME);
		String strLastName = XPathUtil.getString(inDoc, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_LNAME);
		String strCCExpiryDate = XPathUtil.getString(inDoc, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CC_EXPIRATION_DATE);
		String strOrderNo = XPathUtil.getString(inDoc, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_NO);
		
		//Begin : OMNI-32161, 32162, 32163, 32164, 32165, 32166, 32167, 32169
		String strPaymentReference6 = XPathUtil.getString(inDoc, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_PAYMENT_REF_6);
		//End : OMNI-32161, 32162, 32163, 32164, 32165, 32166, 32167, 32169
		
		//This is only for testing
		if(strCCExpiryDate == null || strCCExpiryDate.isEmpty()) {
			log.verbose(" Credit Card Expiry date is empty.");
			//strCCExpiryDate="1230";
		}

		JSONObject json = new JSONObject();
		//Added Mer_ref as OrderNo as a New request to validate the AUTH calls for each order
		//json.put(AcademyConstants.JSON_ATTR_MERCHANT_REF, AcademyConstants.STR_ACADEMY_SPORTS);
		json.put(AcademyConstants.JSON_ATTR_MERCHANT_REF, strOrderNo);
		
		json.put(AcademyConstants.JSON_ATTR_TRANSACTION_TYPE, strTransactionType);
		if(strRequestAmount.startsWith(AcademyConstants.STR_HYPHEN)) {
			json.put(AcademyConstants.JSON_ATTR_AMOUNT, strRequestAmount.split(AcademyConstants.STR_HYPHEN)[1]);
		} else {
			json.put(AcademyConstants.JSON_ATTR_AMOUNT, strRequestAmount);
		}
		
		json.put(AcademyConstants.JSON_ATTR_CURRENCY_CODE, strCurrency.toUpperCase());

		//Begin : OMNI-32161, 32162, 32163, 32164, 32165, 32166, 32167, 32169
		if (restParameters.containsKey(AcademyConstants.STR_IS_EWALLET_ORDER) 
				&& restParameters.get(AcademyConstants.STR_IS_EWALLET_ORDER).equalsIgnoreCase(AcademyConstants.STR_YES)) {
			
			log.verbose(" Is EWallet Order. Updating Input for CardType = " + strCreditCardType);
			
			/*
			 * Updating card type into FirstData acceptable case
			 */
			strCreditCardType = setCreditCardTypeText(strCreditCardType);
			
			log.verbose("Updated CreditCardType = " + strCreditCardType);
			
			
			if(AcademyConstants.TRANSACTION_TYPE_AUTHORIZE.equals(strTransactionType) 
					|| AcademyConstants.TRANSACTION_TYPE_PURCHASE.equals(strTransactionType)) {
				
				
				if(strCreditCardType.equalsIgnoreCase(AcademyConstants.STR_CREDITCARD_TYPE_AMERICAN_EXP)) {
					
					/*
					 * Below json attribute and value will be added for re-auth/purchase request 
					 * for e-wallet amex
					 *  
					 * "stored_credentials_amex": "X"
					 */
					
					json.put(AcademyConstants.JSON_ATTR_STORED_CREDENTIALS_AMEX, AcademyConstants.STR_X);
					
					/*
					 * Sample Request for American Express
					 * 
					 * {
 							"amount": "8660",
 							"stored_credentials_amex": "X",   <--- Attribute being set
 							"method": "token",
 							"merchant_ref": "731004063",
 							"transaction_type": "authorize",
 							"reversal_id": "6220b7ba-b51f-48b2-b913-a34d7b184968",
 							"currency_code": "USD",
 							"token": {
  								"token_data": {
   									"exp_date": "1226",
   									"type": "American Express",
   									"cardholder_name": "Siva T",
   									"value": "7159092179031111"
  								},
  								"token_type": "FDToken"
 							}
						}
					 */
					
				}else {
					
					JSONObject storedCredentials = new JSONObject();
					
					storedCredentials.put(AcademyConstants.JSON_ATTR_INITIATOR, AcademyConstants.STR_MERCHANT);
					storedCredentials.put(AcademyConstants.JSON_ATTR_IS_SCHEDULED, AcademyConstants.STR_TRUE);
					
										
					
					if(YFCObject.isVoid(strPaymentReference6) && !YFCObject.isVoid(strCreditCardType) && (
							strCreditCardType.equalsIgnoreCase(AcademyConstants.STR_CREDITCARD_TYPE_VISA) 
							|| strCreditCardType.equalsIgnoreCase(AcademyConstants.STR_CREDITCARD_TYPE_DISCOVER))) {
						
						/*
						 * OMS saves cardbrand original transaction id under payment reference6 to send it in subsequent 
						 * requests for Visa and Discover e-wallets. In order to determine the value of attribute 'SEQUENCE', oms check if payment reference6
						 * is null. if null, then value 'FIRST' will be set for SEQUENCE.
						 */
						storedCredentials.put(AcademyConstants.JSON_ATTR_SEQUENCE, AcademyConstants.STR_FIRST);
						
						/*
						 * Sample Request
						 * 
						 * Visa/Discover - First Re-authorization or purchase
						 * 
						 * {
 								"amount": "3246",
 								"method": "token",
 								"merchant_ref": "731012106",
 								"stored_credentials": {
  									"sequence": "FIRST",     <------
  									"initiator": "MERCHANT", <------
  									"is_scheduled": "true"   <------
 								},
 								"transaction_type": "authorize",
 								"reversal_id": "a241be1a-0805-4f99-9f64-4dcb2322aa89",
 								"currency_code": "USD",
 								"token": {
  									"token_data": {
   										"exp_date": "1223",
   										"type": "Visa",     <------ Visa/Discover : based on card type
   										"cardholder_name": "Tim Harris",
   										"value": "8209049190852518"
  									},
  								"token_type": "FDToken"
 								}
						  }
						 * 
						 */
						
					}
					else {
						
						/*
						 * If payment reference6 is not null, then SEQUENCE will get set as SUBSEQUENCE
						 * 
						 */
						
						storedCredentials.put(AcademyConstants.JSON_ATTR_SEQUENCE, AcademyConstants.STR_SUBSEQUENT);
						
						
						if(!YFCObject.isVoid(strPaymentReference6) 
								&& (strCreditCardType.equalsIgnoreCase(AcademyConstants.STR_CREDITCARD_TYPE_VISA) 
										|| strCreditCardType.equalsIgnoreCase(AcademyConstants.STR_CREDITCARD_TYPE_DISCOVER))) {
							
							log.verbose(" :: strPaymentReference6 ::  " + strPaymentReference6);
							
							/*
							 * when payment reference6 is not null, the value is fetched and send it as the value of
							 * cardbrand_oroginal_transaction_id. It would have received as part of first re-authorization call
							 */
							storedCredentials.put(AcademyConstants.JSON_ATTR_CARD_BRAND_ORIG_TXN_ID, strPaymentReference6);
							
							
							if(strCreditCardType.equalsIgnoreCase(AcademyConstants.STR_CREDITCARD_TYPE_DISCOVER)) {
								
								DecimalFormat dfCardBrandOrigAmt = new DecimalFormat(AcademyConstants.STR_ZERO_WITH_DECIMAL); 
								String strCardBrandOrigAmt = dfCardBrandOrigAmt.format(Double.parseDouble(strRequestAmount)/100);
								
								storedCredentials.put(AcademyConstants.JSON_ATTR_CARD_BRAND_ORIG_AMOUNT, strCardBrandOrigAmt);
							}
						}
						
						/*
						 * Sample Request 
						 * 
						 * Visa/Discover - Subsequent requests
						 * 
						 * {
   								"amount": "2164",
   								"method": "token",
   								"merchant_ref": "731018019",
   								"stored_credentials": {
      								"sequence": "SUBSEQUENT",
      								"cardbrand_original_transaction_id": "286864265013460",  <---- saved under payment reference 6 from first re-auth response
      								"initiator": "MERCHANT",
      								"is_scheduled": "true",
      								"cardbrand_original_amount": "21.64"  <----- send only for Discover
   								},
   								"transaction_type": "authorize",
   								"reversal_id": "cbdd5a36-4d0d-44c9-ba4e-38f9897af6c6",
   								"currency_code": "USD",
   								"token": {
      								"token_data": {
         							"exp_date": "1226",
         							"type": "Discover",
         							"cardholder_name": "drew m",
         							"value": "9838494038751111"
      							},
      							"token_type": "FDToken"
   							}

						 * 
						 */
						
						/*
						 * Sample Request
						 * 
						 * Mastercard
						 * 
						 * {
 								"amount": "8660",
 								"method": "token",
 								"merchant_ref": "731003083",
 								"stored_credentials": {   <---- Always send below attributes for re-authorization/purchase calls
  									"sequence": "SUBSEQUENT",
  									"initiator": "MERCHANT",
  									"is_scheduled": "true"
 								},
 								"transaction_type": "authorize",
 								"reversal_id": "2729dd9a-da20-4d80-ab1c-43bdb6c67545",
 								"currency_code": "USD",
 								"token": {
  									"token_data": {
   										"exp_date": "0922",
   										"type": "Mastercard",
   										"cardholder_name": "Bindu Eg",
   										"value": "7267913795206937"
  									},
  									"token_type": "FDToken"
 								}
							}
						 * 
						 */
						
					}

					json.put(AcademyConstants.JSON_ATTR_STORED_CREDENTIALS, storedCredentials);
					
				}
				
				
			}			
			
			

		}		
		//End : OMNI-32161, 32162, 32163, 32164, 32165, 32166, 32167, 32169
		
		if(AcademyConstants.TRANSACTION_TYPE_AUTHORIZE.equals(strTransactionType) 
				|| AcademyConstants.TRANSACTION_TYPE_PURCHASE.equals(strTransactionType)){
			
			JSONObject token = new JSONObject();
			JSONObject tokenData = new JSONObject();

			json.put(AcademyConstants.JSON_ATTR_METHOD, AcademyConstants.STR_TOKEN);
			token.put(AcademyConstants.JSON_ATTR_TOKEN_TYPE, "FDToken");

			tokenData.put(AcademyConstants.JSON_ATTR_TYPE, strCreditCardType);
			tokenData.put(AcademyConstants.JSON_ATTR_VALUE, strCreditCardNo);
			tokenData.put(AcademyConstants.JSON_ATTR_CARD_HOLDER_NAME, strFirstName + " " + strLastName);
			tokenData.put(AcademyConstants.JSON_ATTR_EXPIRY_DATE, strCCExpiryDate);

			token.put(AcademyConstants.JSON_ATTR_TOKEN_DATA, tokenData);
			json.put(AcademyConstants.STR_TOKEN, token);
		}
		else {
			String strAuthorizationID = XPathUtil.getString(inDoc, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_AUTHORIZATION_ID);
			json.put(AcademyConstants.JSON_ATTR_TRANSACTION_TAG, strAuthorizationID);

			if(strTransactionType == AcademyConstants.TRANSACTION_TYPE_SPLIT ) {
				json.put(AcademyConstants.JSON_ATTR_SPLIT_SHIPMENT, strSplitShipment);
			}
		}

		//Adding an auto generated Reversal Id to be used in case of timeout Reversals.
		if(!YFCObject.isVoid(restParameters.get(AcademyConstants.WEBSERVICE_REVERSAL_ID)))
			json.put(AcademyConstants.JSON_ATTR_REVERSAL_ID, restParameters.get(AcademyConstants.WEBSERVICE_REVERSAL_ID));
		
		strJSONInput = json.toString();
		log.verbose("AcademyPayeezyRestUtil.createPAYZRestInputForCCAuth: REST Request" + strJSONInput);
		log.verbose("End of AcademyPayeezyRestUtil.createPAYZRestInputForCCAuth() method");

		return strJSONInput;
	}



	/**This method prepares the PayZ request in REST format for Credit Cards
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public static Document formatPayeezyResponse(HttpsURLConnection conn) throws Exception {

		log.verbose("Begin of AcademyPayeezyRestUtil.formatPayeezyResponse() method");
		Document docResponse = null;
		try {

			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getResponseCode() / 100 == 2 ? conn.getInputStream() : conn.getErrorStream()));

			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = reader.readLine()) != null) {
				response.append(inputLine);
			}
			reader.close();

			//print result
			log.verbose(response.toString());
			String resp = response.toString();

			log.verbose(" Output \n" +resp);

			InputStream in = IOUtils.toInputStream(resp);
			String xml = XML.toXml(in);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			docResponse =  builder.parse(new InputSource(new StringReader(xml)));

			//Update the response to convert back the amount into decimals
			String strAmount = docResponse.getDocumentElement().getAttribute(AcademyConstants.JSON_ATTR_AMOUNT);
			strAmount = Double.toString((Double.parseDouble(strAmount)/100));
			docResponse.getDocumentElement().setAttribute(AcademyConstants.JSON_ATTR_AMOUNT, strAmount);
			log.verbose(XMLUtil.getXMLString(docResponse));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(docResponse != null)
			log.verbose("AcademyPayeezyRestUtil.formatPayeezyResponse() Output XML ::"+XMLUtil.getXMLString(docResponse));
		log.verbose("End of AcademyPayeezyRestUtil.formatPayeezyResponse() method");

		return docResponse;
	}
	/**	 
	 * @param inDoc
	 * @return
	 * @throws Exception
	 {
			  "amount": "7",
			  "transaction_type": "authorize",
			  "merchant_ref": "paypal1234",
			  "method": "paypal",
			  "currency_code": "USD",
			  "paypal_transaction_details": {
			    "timestamp": "2014/11/05 16:05:52",
			    "authorization": "O-1XW865551D308884E",
			    "success": true,
			    "message": "Success",
			    "correlation_id": "c9e47ba37b784",
			    "payer_id": "SA4SLFX48Y6DL",
			    "gross_amount_currency_id": "USD",
			    "cardholder_name": "John Smith"
			  }
			}
	 */
	public static String createPAYZRestInputForPaypal(Document docPaymentIn, String strId, String strPayPalDebugId, 
			String strPayPalAuthTime, String strTransactionType, Map<String, String> restParameters) throws Exception {
		
		log.verbose("Begin of AcademyPayeezyRestUtil.createPAYZRestInputForPaypalAuth() method");
		
		log.verbose("strPayPalAuthTime:  " + strPayPalAuthTime);
		log.verbose("strPayPalDebugId:  " + strPayPalDebugId);
		log.verbose("strId:  " + strId);
		
		String strJSONInput = null;
		//String strAmount = XPathUtil.getString(docRespPaypal.getDocumentElement(), "/jsonObject/amount/@total");
		String strRequestAmount = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);
		String strFirstName = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_FNAME);
		String strLastName = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_LNAME);
		String strPaymentReference6 = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_PAYMENT_REF_6);
		String strOrderNo = XPathUtil.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_NO);
		
		/*String strPayPalAuthTime = docRespPaypal.getDocumentElement().getAttribute("create_time");
		String strId =  docRespPaypal.getDocumentElement().getAttribute("id");
		*/
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
		Date date = sdf.parse(strPayPalAuthTime);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
		strPayPalAuthTime = formatter.format(date);
	
		//Updating request amount in format that can be read by PAyeezy 
		DecimalFormat df = new DecimalFormat("#"); 
		strRequestAmount = df.format((Double.parseDouble(strRequestAmount)*100));

		JSONObject json = new JSONObject();
		json.put(AcademyConstants.JSON_ATTR_AMOUNT, strRequestAmount);
		//Added Mer_ref as OrderNo as a New request to validate the AUTH calls for each order
		//json.put(AcademyConstants.JSON_ATTR_MERCHANT_REF, AcademyConstants.STR_ACADEMY_SPORTS);
		json.put(AcademyConstants.JSON_ATTR_MERCHANT_REF, strOrderNo);
		json.put(AcademyConstants.JSON_ATTR_METHOD, "paypal");
		json.put(AcademyConstants.JSON_ATTR_CURRENCY_CODE, "USD");
		json.put(AcademyConstants.JSON_ATTR_TRANSACTION_TYPE, strTransactionType);
		
		JSONObject jsonPaypalTransactionDetails = new JSONObject();
		jsonPaypalTransactionDetails.put(AcademyConstants.WEBSERVICE_TIMESTAMP, strPayPalAuthTime);
		//jsonPaypalTransactionDetails.put(AcademyConstants.STR_PAYZ_AUTHORIZATION, strCreditCardNo);
		jsonPaypalTransactionDetails.put(AcademyConstants.STR_PAYZ_AUTHORIZATION, strId);//Added as per Zuheb.(New request)
		jsonPaypalTransactionDetails.put("success", "true");
		jsonPaypalTransactionDetails.put("message", "Success");
		jsonPaypalTransactionDetails.put(AcademyConstants.JSON_ATTR_CORRELATION_ID, strPayPalDebugId);		
		//jsonPaypalTransactionDetails.put("payer_id", strId);
		jsonPaypalTransactionDetails.put(AcademyConstants.ATTR_PAYPAL_PAYER_ID, strPaymentReference6);//Added as per Zuheb.(New request)
		jsonPaypalTransactionDetails.put("gross_amount_currency_id", "USD");		
		jsonPaypalTransactionDetails.put(AcademyConstants.JSON_ATTR_CARD_HOLDER_NAME, strFirstName + " " + strLastName);
		json.put("paypal_transaction_details", jsonPaypalTransactionDetails);
		
		//Adding an auto generated Reversal Id to be used in case of timeout Reversals.
		json.put(AcademyConstants.JSON_ATTR_REVERSAL_ID, restParameters.get(AcademyConstants.WEBSERVICE_REVERSAL_ID));
		
		strJSONInput = json.toString();
		log.verbose("AcademyPayeezyRestUtil.createPAYZRestInputForPaypalAuth: REST Request" + strJSONInput);
		log.verbose("End of AcademyPayeezyRestUtil.createPAYZRestInputForPaypalAuth() method");

		return strJSONInput;
	}
	
	
	/**This method generates a 36 digit token using JAVA UUID
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public static String getAutoGeneratedUUID() throws Exception {
		log.verbose("Begin of AcademyPayeezyRestUtil.getUUID() method");
		
		UUID uuid = UUID.randomUUID();
	    String randomUUIDString = uuid.toString();
	    log.verbose("randomUUIDString ::" + randomUUIDString);
	    log.verbose("End of AcademyPayeezyRestUtil.getUUID() method");
		return randomUUIDString;
	}
	
	
	/**This method prepares the PayZ request in REST format for timeout Reversals
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public static String createPAYZRestInputForTimeoutReversals(Document inDoc, String strRequestAmount, String strReversalId) throws Exception {

		log.verbose("Begin of AcademyPayeezyRestUtil.createPAYZRestInputForTimeoutReversals() method");
		String strJSONInput = null;
		String strCurrency = XPathUtil.getString(inDoc, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CURRENCY);
		String strOrderNo = XPathUtil.getString(inDoc, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_NO);
		
		if(YFCObject.isVoid(strRequestAmount))
			strRequestAmount = XPathUtil.getString(inDoc, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);

		//Updating request amount in format that can be read by PAyeezy 
		DecimalFormat df = new DecimalFormat("#"); 
		strRequestAmount = df.format((Double.parseDouble(strRequestAmount)*100));

		JSONObject json = new JSONObject();
		//Added Mer_ref as OrderNo as a New request to validate the AUTH calls for each order
		//json.put(AcademyConstants.JSON_ATTR_MERCHANT_REF, AcademyConstants.STR_ACADEMY_SPORTS);
		json.put(AcademyConstants.JSON_ATTR_MERCHANT_REF, strOrderNo);
		json.put(AcademyConstants.JSON_ATTR_TRANSACTION_TYPE, AcademyConstants.TRANSACTION_TYPE_VOID);
		json.put(AcademyConstants.JSON_ATTR_METHOD, AcademyConstants.CREDIT_CARD.toLowerCase());
		json.put(AcademyConstants.JSON_ATTR_CURRENCY_CODE, strCurrency.toUpperCase());
		
		if(strRequestAmount.startsWith(AcademyConstants.STR_HYPHEN)) {
			json.put(AcademyConstants.JSON_ATTR_AMOUNT, strRequestAmount.split(AcademyConstants.STR_HYPHEN)[1]);
		} else {
			json.put(AcademyConstants.JSON_ATTR_AMOUNT, strRequestAmount);
		}

		json.put(AcademyConstants.JSON_ATTR_REVERSAL_ID, strReversalId);
		
		strJSONInput = json.toString();
		log.verbose("AcademyPayeezyRestUtil.createPAYZRestInputForTimeoutReversals: REST Request" + strJSONInput);
		log.verbose("End of AcademyPayeezyRestUtil.createPAYZRestInputForTimeoutReversals() method");

		return strJSONInput;
	}
	
	//Begin : OMNI-32161, 32162, 32163, 32164, 32165, 32166, 32167, 32169
	private static String setCreditCardTypeText(String strCreditCardTypeOMS) throws Exception{
		
		String strCreditCardTypePayZ = null;
		
		/* Changing card type to firstdata acceptable case
		 * 
		 * AMEX / AMERICAN EXPRESS / American Express ---> American Express
		 * DISCOVER / Discover ----> Discover
		 * Visa / VISA ---------------> Visa
		 * MASTER CARD / MASTERCARD / MasterCard / Master Card -----> Mastercard
		 */
		
		
		if(AcademyConstants.STR_CREDITCARD_TYPE_AMEX.equalsIgnoreCase(strCreditCardTypeOMS) 
				|| AcademyConstants.STR_CREDITCARD_TYPE_AMERICAN_EXP.equalsIgnoreCase(strCreditCardTypeOMS)) {
			
			strCreditCardTypePayZ = AcademyConstants.STR_CREDITCARD_TYPE_AMERICAN_EXP;
			
		} else if(AcademyConstants.STR_CREDITCARD_TYPE_DISCOVER.equalsIgnoreCase(strCreditCardTypeOMS)) {
			
			strCreditCardTypePayZ = AcademyConstants.STR_CREDITCARD_TYPE_DISCOVER;
			
		}else if (AcademyConstants.STR_CREDITCARD_TYPE_VISA.equalsIgnoreCase(strCreditCardTypeOMS)) {
			
			strCreditCardTypePayZ = AcademyConstants.STR_CREDITCARD_TYPE_VISA;
			
		}else if(AcademyConstants.STR_CREDITCARD_TYPE_MASTERCARD.equalsIgnoreCase(strCreditCardTypeOMS)
				|| AcademyConstants.STR_CREDITCARD_TYPE_MASTER_CARD.equalsIgnoreCase(strCreditCardTypeOMS)) {
		
			strCreditCardTypePayZ = AcademyConstants.STR_CREDITCARD_TYPE_MASTERCARD;
			
		}		
		
		
		return strCreditCardTypePayZ;
	}
	//End : OMNI-32161, 32162, 32163, 32164, 32165, 32166, 32167, 32169
	
	
}
