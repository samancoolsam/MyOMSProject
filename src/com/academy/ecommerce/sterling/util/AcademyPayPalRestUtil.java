package com.academy.ecommerce.sterling.util;

//package com.academy.commerce.rest.payeezyclient.domain.v2;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.json.utils.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.academy.ecommerce.sterling.util.stub.AcademyPaymentStub;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfc.util.YFCDateUtils;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyPayPalRestUtil {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPayPalRestUtil.class);

	private static String strInvokePaymentStub = YFSSystem.getProperty(AcademyConstants.INVOKE_PAYMENT_STUB); //OMNI-106361
	//private static String strPayPalUser = "AZIoy7wkXiDML9-c9Vz712AghG_XejVMyT0HHrUgxfGIt5Ch-V7i2SuUPiR4Xry-ds9kfsKZPdxDnyFL";
	//private static String strPayPalPass = "EPHFGXDN1_GrYqczB5AvaEL5YkZkBSZg85XLu0oSkTUN77dFUeXray2Q1ptM3vfYplVHynKmEW3IashF";
	//private static String strEncodedCredential = null;
	//private static String strProxyHost = "52.22.64.70";
	//private static String strProxyPort = "8443"; 

	//private static String PAYPAL_URL_TOKEN = "https://api.sandbox.paypal.com/v1/oauth2/token";
	//private static String PAYPAL_URL_AUTH = "https://sandbox.paypal.com/v1/payments/orders/{OrderId}/authorize";
	//private static YFCDate expiresTimeStamp = new YFCDate();
	//Hash map to save token related details
	private static ConcurrentHashMap<String,String> chmapToken =  new ConcurrentHashMap<>();

	public static Document createDocumentForJson(String strJsonInput) throws ParserConfigurationException, SAXException, IOException
	{
		log.verbose("strJsonInput::  "+strJsonInput);
		String strXMLFormat = XML.toXml(IOUtils.toInputStream(strJsonInput));
		log.verbose("strXMLFormat::  "+strXMLFormat);	

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document docResponse =  builder.parse(new InputSource(new StringReader(strXMLFormat)));
		return docResponse;
	}

	/*public Document createAccessToken(YFSEnvironment env,Document inDoc) {
		createAccessToken(Map<String, String> restParameters);
		return inDoc;
	}*/


	/**This methid will create PayPal token and then get the Authorization
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public static Document getPayPalAuthorization(YFSEnvironment env, Document inDoc, Map<String, String> restParameters) throws Exception {
		log.verbose("Begin of AcademyPayPalRestUtil.getPayPalAuthorization() input doc ::"+XMLUtil.getXMLString(inDoc));

		Document docResponse = null;
		String strAccessToken = "";

		docResponse = createAccessToken(restParameters);
		strAccessToken = docResponse.getDocumentElement().getAttribute(AcademyConstants.ATTR_ACCESS_TOKEN);
		log.verbose("strAccessToken:  " +strAccessToken);

		if(!YFSObject.isVoid(strAccessToken)){
			log.verbose("Received AccessToken");
			docResponse = invokePayPalRestService(inDoc,strAccessToken,restParameters);	
		}
		log.verbose("End of AcademyPayPalRestUtil.getPayPalAuthorization() method");
		return docResponse;
	}

	/**This method invoke PayPal REST API for authorization.
	 * @param inDoc
	 * @param strAccessToken
	 * @return
	 * @throws Exception
	 */
	public static Document invokePayPalRestService(Document inDoc,String strAccessToken, Map<String, String> restParameters) throws Exception {
		log.verbose("Begin of AcademyPayPalRestUtil.invokePayPalRestService() input doc ::"+XMLUtil.getXMLString(inDoc));
		HttpsURLConnection con;
		Proxy proxy;
		String strJsonInput = "";
		Document docResponse = null;

		//String strOrderId = inDoc.getDocumentElement().getAttribute(AcademyConstants.PAYPAL_REQ_ORDER_ID);	
		//String strAmount = inDoc.getDocumentElement().getAttribute(AcademyConstants.PAYPAL_REQ_AMOUNT);
		String strCreditCardNo = XPathUtil.getString(inDoc, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.CREDIT_CARD_NO);
		String strRequestAmount = XPathUtil.getString(inDoc, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);
		String atrPaypalAuthId = inDoc.getDocumentElement().getAttribute(AcademyConstants.PAYPAL_RESPONSE_ID);
		
		DecimalFormat decFormat = new DecimalFormat("0.00");
		strRequestAmount = decFormat.format(Double.valueOf(strRequestAmount));

		strJsonInput = "{\"amount\": {\"currency\": \"USD\",\"total\": \""+strRequestAmount+"\"}}";

		log.verbose("invokePayPalInterface() ::Payment Input XML :: \n "+strJsonInput);

		byte[] buffer = new byte[strJsonInput.length()];
		buffer = strJsonInput.getBytes();
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		bout.write(buffer);

		String strProxyHost = restParameters.get(AcademyConstants.WEBSERVICE_PROXY_HOST);
		String strProxyPort = restParameters.get(AcademyConstants.WEBSERVICE_PROXY_PORT);
		String strURL = restParameters.get(AcademyConstants.WEBSERVICE_PAYPAL_URL_AUTH);

		if(strURL.contains("{OrderId}")){
			strURL = strURL.replace("{OrderId}", strCreditCardNo);
		}
		if(strURL.contains("{AuthorizationId}")){
			strURL = strURL.replace("{AuthorizationId}", atrPaypalAuthId);
		}

		URL url = new URL(strURL);		

		//System.setProperty(AcademyConstants.WEBSERVICE_HTTPS_PROXY_HOST, strProxyHost);
		//System.setProperty(AcademyConstants.WEBSERVICE_HTTPS_PROXY_PORT, strProxyPort);
		
		//Start : OMNI-1435 : Changes for PayPal Proxy enabling/disabling
		String strIsProxyEnabled = restParameters.get(AcademyConstants.PROP_ENABLE_PROXY);
		log.verbose("\n strIsProxyEnabled : " +strIsProxyEnabled);

		if(!YFCObject.isVoid(strIsProxyEnabled) && strIsProxyEnabled.equals("Y")){
			log.verbose("Connecting PayPal through proxy:: strProxyHost:"+strProxyHost+" :: strProxyPort:"+strProxyPort + " :: strEndpointURL:"+strURL);
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(strProxyHost, Integer.parseInt(strProxyPort))); 
			con = (HttpsURLConnection) url.openConnection(proxy);
		}
		else {
			log.verbose("Connecting PayPal directly without Proxy :: strEndpointURL:"+strURL);
			con = (HttpsURLConnection) url.openConnection();
		}
		//End : OMNI-1435 : Changes for PayPal Proxy enabling/disabling
		
		//Start : OMNI-1435 : Changes for PayPal Proxy enabling/disabling
		//proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(strProxyHost, Integer.parseInt(strProxyPort)));        
		//con = (HttpsURLConnection) url.openConnection(proxy); 
		//conn = (HttpsURLConnection) url.openConnection();//Without proxy
		//End : OMNI-1435 : Changes for PayPal Proxy enabling/disabling
		
		log.verbose("Connected..... ");

		con.setDoOutput(true);
		con.setRequestMethod(AcademyConstants.WEBSERVICE_POST); 
		con.setRequestProperty(AcademyConstants.WEBSERVICE_CONTENT_TYPE,AcademyConstants.ACCEPT_APPLICATION_JSON);
		con.setRequestProperty(AcademyConstants.WEBSERVICE_AUTHORIZATION, AcademyConstants.STR_BEARER_SPACE+strAccessToken);

		String strConnectionTimeout = restParameters.get(AcademyConstants.ATTR_CONNECTION_TIMEOUT);
		String strReadTimeout = restParameters.get(AcademyConstants.ATTR_RESPONSE_TIMEOUT);	

		if(!YFSObject.isVoid(strConnectionTimeout)){
			int iConnectionTimeout = Integer.parseInt(strConnectionTimeout);
			con.setConnectTimeout(iConnectionTimeout);
		}
		if(!YFSObject.isVoid(strReadTimeout)){
			int iReadTimeout = Integer.parseInt(strReadTimeout);
			con.setReadTimeout(iReadTimeout);
		}

		try {
			DataOutputStream outDataStream = new DataOutputStream(con.getOutputStream());
			outDataStream.writeBytes(strJsonInput);
			outDataStream.flush();
			outDataStream.close();

			int iResponseCode = con.getResponseCode();
			log.verbose("iResponseCode... --->  "+iResponseCode);

			if(iResponseCode>200 & iResponseCode<300 ){
				log.verbose("calling... ---> SUCCESS");
			}else{
				log.verbose("calling... failure ---> Response code("+con.getResponseCode()+")");
				log.verbose(con.getResponseMessage());
			}

			docResponse = formatPayPalResponse(con);
		}
		//Updated the catch block to create Splunk Alerts
		catch (SSLHandshakeException e) {
			log.info("PayZ with PayPal Exception:: SSLHandshakeException :: "+e.toString()+" Exception Stack Trace"+e.getStackTrace().toString());
			docResponse = XMLUtil.createDocument("json_object");
			docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, AcademyConstants.STATUS_CODE_504);
			docResponse.getDocumentElement().setAttribute(AcademyConstants.JSON_ATTR_REVERSAL_ID, 
					restParameters.get(AcademyConstants.WEBSERVICE_REVERSAL_ID));
		} catch(Exception e) {
			log.info("PayZ with PayPal Generic Exception:: "+e.toString()+" Exception Stack Trace"+e.getStackTrace().toString());
			log.verbose("Generate Authorization again");
			docResponse = XMLUtil.getDocumentFromString("<Error HttpStatusCode='ERROR' API='token' /> ");
		}finally{
			if(con!=null){
				log.verbose("Disconnecting -->con.disconnect()");
				con.disconnect();
			}
		}

		log.verbose("End AcademyPayPalRestUtil.invokePayPalRestService() ");
		return docResponse;
	}

	/**This method prepares the PayPal response document for PayPal
	 * @param con
	 * @return
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private static Document formatPayPalResponse(HttpsURLConnection con) throws IOException, ParserConfigurationException, SAXException {
		log.verbose("Begin of AcademyPayPalRestUtil.formatPayPalResponse() method");

		Document docResponse = null;
		Element eleResponse = null;
		int iResponseCode = con.getResponseCode();
		BufferedReader reader = new BufferedReader(new InputStreamReader(iResponseCode / 100 == 2 ? con.getInputStream() : con.getErrorStream()));

		String inputLine;
		StringBuffer strResponse = new StringBuffer();

		while ((inputLine = reader.readLine()) != null) {
			strResponse.append(inputLine);
		}
		reader.close();

		if(!YFSObject.isVoid(strResponse)){
			docResponse =  createDocumentForJson(strResponse.toString());
			eleResponse = docResponse.getDocumentElement();
			log.verbose("Adding response headers");
			eleResponse.setAttribute(AcademyConstants.ATTR_PAYPAL_DEBUG_ID, con.getHeaderField(AcademyConstants.ATTR_PAYPAL_DEBUG_ID));	
			eleResponse.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, Integer.toString(iResponseCode));	
			docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, Integer.toString(iResponseCode));
			docResponse.getDocumentElement().setAttribute(AcademyConstants.JSON_ATTR_CORRELATION_ID, con.getHeaderField(AcademyConstants.ATTR_PAYPAL_DEBUG_ID));
		}

		if(log.isVerboseEnabled())
		{
			log.verbose("printing response headers");
			Map<String, List<String>> map = con.getHeaderFields();
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				log.verbose("Key : " + entry.getKey() + " ,Value : " + entry.getValue());					
			}
		}
		if(docResponse != null)
			log.verbose("AcademyPayPalRestUtil.formatPayPalResponse() Output XML ::"+XMLUtil.getXMLString(docResponse));

		log.verbose("End of AcademyPayPalRestUtil.formatPayPalResponse() method");

		return docResponse;
	}



	private static Document createAccessToken(Map<String, String> restParameters) {
		log.verbose("Begin of AcademyPayPalRestUtil.createAccessToken() method");

		URL url;
		HttpsURLConnection con ;
		Proxy proxy;	
		Document docResponse = null;

		try {
			String strProxyHost = restParameters.get(AcademyConstants.WEBSERVICE_PROXY_HOST);
			String strProxyPort = restParameters.get(AcademyConstants.WEBSERVICE_PROXY_PORT);
			String strPayPalUser = restParameters.get(AcademyConstants.WEBSERVICE_PAYPAL_USER_ID);
			String strPayPalPass = restParameters.get(AcademyConstants.WEBSERVICE_PAYPAL_PASSWORD);
			String strPayPalTokenUrl = restParameters.get(AcademyConstants.WEBSERVICE_PAYPAL_URL_TOKEN);
			String strEncodedCredential = DatatypeConverter.printBase64Binary((strPayPalUser+":"+strPayPalPass).getBytes(AcademyConstants.STR_UTF8)); 

			url = new URL(strPayPalTokenUrl);
			//StringBuffer strResponse = new StringBuffer();
			//HttpsURLConnection con = (HttpsURLConnection) url.openConnection();	
			//System.setProperty(AcademyConstants.WEBSERVICE_HTTPS_PROXY_HOST, strProxyHost);
			//System.setProperty(AcademyConstants.WEBSERVICE_HTTPS_PROXY_PORT, strProxyPort);
			
			//Start : OMNI-69010 - Enabling/Disabling proxy based on the service argument named PaypalProxyEnable
			//from the services AcademyDirectPaypalUEService,AcademyInvokePayeezyRestWebservice
			String strPaypalProxyEnable = restParameters.get(AcademyConstants.ATTR_PAYPAL_PROXY_ENABLE);
			log.verbose("\n PaypalProxyEnable (Service Argument of 1.AcademyDirectPaypalUEService / 2.AcademyInvokePayeezyRestWebservice) :: " +strPaypalProxyEnable);

			if(!YFCObject.isVoid(strPaypalProxyEnable) && strPaypalProxyEnable.equals("Y")){
				log.verbose("Connecting PayPal through proxy:: strProxyHost:"+strProxyHost+" :: strProxyPort:"+strProxyPort + " :: strEndpointURL:"+strPayPalTokenUrl);
				proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(strProxyHost, Integer.parseInt(strProxyPort))); 
				con = (HttpsURLConnection) url.openConnection(proxy);
			}
			else {
				log.verbose("Connecting PayPal directly without Proxy :: strEndpointURL:"+strPayPalTokenUrl);
				con = (HttpsURLConnection) url.openConnection();
			}
			//con = (HttpsURLConnection) url.openConnection(); 
			//End : OMNI-69010
			con.setRequestMethod(AcademyConstants.WEBSERVICE_POST);
			log.verbose("strEncodedCredential:  "  +strEncodedCredential);
			con.setRequestProperty(AcademyConstants.WEBSERVICE_AUTHORIZATION, AcademyConstants.STR_BASIC_SPACE+strEncodedCredential);
			con.setRequestProperty(AcademyConstants.WEBSERVICE_CONTENT_TYPE,AcademyConstants.STR_X_WWW_FORM_URLENCODED);
			con.setDoOutput(true);

			String strConnectionTimeout = restParameters.get(AcademyConstants.ATTR_CONNECTION_TIMEOUT);
			String strReadTimeout = restParameters.get(AcademyConstants.ATTR_RESPONSE_TIMEOUT);

			if(!YFSObject.isVoid(strConnectionTimeout)){
				int iConnectionTimeout = Integer.parseInt(strConnectionTimeout);
				con.setConnectTimeout(iConnectionTimeout);
			}
			if(!YFSObject.isVoid(strReadTimeout)){
				int iReadTimeout = Integer.parseInt(strReadTimeout);
				con.setReadTimeout(iReadTimeout);
			}

			String strPayload = AcademyConstants.STR_GRANT_TYPE_CLIENT_CREDENTIALS;  

			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(strPayload);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			log.verbose("responseCode:: " +responseCode);
			docResponse = formatPayPalResponse(con);

			return docResponse;				

		} /*catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			log.verbose("Exception "+e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.verbose("Exception "+e.getMessage());
			e.printStackTrace();
		}*/
		catch (Exception e){
			log.info("PayZ with PayPal DataToken Create Error :: "+e.toString()+" Exception Stack Trace"+e.getStackTrace().toString());
			log.verbose("Please generate AccessToken");
			docResponse = XMLUtil.getDocumentFromString("<Error HttpStatusCode='ERROR' API='token/> ");			
		}
		log.verbose("End of AcademyPayPalRestUtil.createAccessToken() method");
		return docResponse;	
	}



	/**This method will create PayPal token and then do the Paypal void
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public static Document postPaypalVoid(YFSEnvironment env, Document inDoc, Map<String, String> restParameters) throws Exception {
		log.verbose("Begin of AcademyPayPalRestUtil.postPaypalVoid() input doc ::"+XMLUtil.getXMLString(inDoc));

		Document docResponse = null;
		String strAccessToken = "";

		docResponse = createAccessToken(restParameters);
		strAccessToken = docResponse.getDocumentElement().getAttribute(AcademyConstants.ATTR_ACCESS_TOKEN);
		log.verbose("strAccessToken:  " +strAccessToken);

		if(!YFSObject.isVoid(strAccessToken)){
			log.verbose("Received AccessToken");
			docResponse = invokePayPalRestService(inDoc,strAccessToken,restParameters);	
		}
		log.verbose("End of AcademyPayPalRestUtil.postPaypalVoid() method");
		return docResponse;
	}
	
	/**
	 * This method invoke Paypal REST API for all transaction types for paypal decoupled scenarios
	 * @param strAccessToken
	 * @param restParameters
	 * @param strJsonInput
	 * @return
	 * @throws Exception
	 */
	private static Document invokeDirectPaypalRestService(String strAccessToken, Map<String, String> restParameters,String strJsonInput,String strCTK,String strSplunkDtls) throws Exception {
		log.debug("Begin of AcademyPayPalRestUtil.invokeDirectPaypalRestService() after direct paypal.");
		log.verbose("Paypal Direct:: strJsonInput::" +strJsonInput);
		HttpsURLConnection con;
		Proxy proxy;
		
		Document docResponse = null;
		log.verbose("invokeDirectPaypalRestService() ::strJsonInput :: \n "+strJsonInput);
		byte[] buffer = new byte[strJsonInput.length()];
		buffer = strJsonInput.getBytes();
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		bout.write(buffer);	
		
		String strProxyHost = restParameters.get(AcademyConstants.WEBSERVICE_PROXY_HOST);
		String strProxyPort = restParameters.get(AcademyConstants.WEBSERVICE_PROXY_PORT);
		String strURL = restParameters.get(AcademyConstants.WEBSERVICE_PAYPAL_URL);
		String  strPaypalURLId=   restParameters.get(AcademyConstants.ATTR_AUTHORIZATION_ID);
		String strPaypalReqId=restParameters.get(AcademyConstants.STR_PAYPAL_REQUEST_ID);
		if(strURL.contains("{OrderId}")){//Reauth
			strURL = strURL.replace("{OrderId}", strPaypalURLId);
		}else if(strURL.contains("{AuthorizationId}")){ //Capture&Void
			strURL = strURL.replace("{AuthorizationId}", strPaypalURLId);
		}else if(strURL.contains("{CaptureId}")){ //Refund
			strURL = strURL.replace("{CaptureId}", strPaypalURLId);
		}
		log.verbose("Connecting PayPal strEndpointURL:"+strURL);
		URL url = new URL(strURL);
		log.verbose("Paypal Direct:: AcademyDirectPaypalUEService rest call details:: ChargeTransactionKey: " + strCTK + 
				" :JsonInput ::" + strJsonInput +" EndpointURL: "+ strURL);
		//Start : OMNI-69010 - Enabling/Disabling proxy based on the service argument named PaypalProxyEnable
		//from the services AcademyDirectPaypalUEService,AcademyInvokePayeezyRestWebservice
		String strPaypalProxyEnable = restParameters.get(AcademyConstants.ATTR_PAYPAL_PROXY_ENABLE);
		log.verbose("\n PaypalProxyEnable (Service Argument of 1.AcademyDirectPaypalUEService / 2.AcademyInvokePayeezyRestWebservice): " +strPaypalProxyEnable);
		if(!YFCObject.isVoid(strPaypalProxyEnable) && strPaypalProxyEnable.equals("Y")){
			log.verbose("Connecting PayPal through proxy:: strProxyHost:"+strProxyHost+" :: strProxyPort:"+strProxyPort + " :: strEndpointURL:"+strURL);
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(strProxyHost, Integer.parseInt(strProxyPort))); 
			con = (HttpsURLConnection) url.openConnection(proxy);
		}
		else {
			log.verbose("Connecting PayPal directly without Proxy :: strEndpointURL:"+strURL);
			con = (HttpsURLConnection) url.openConnection();
		}
		//con = (HttpsURLConnection) url.openConnection(); 
		//End : OMNI-69010
		
		log.verbose("Connected..... ");

		con.setDoOutput(true);
		con.setRequestMethod(AcademyConstants.WEBSERVICE_POST); 
		con.setRequestProperty(AcademyConstants.WEBSERVICE_CONTENT_TYPE,AcademyConstants.ACCEPT_APPLICATION_JSON);
		con.setRequestProperty(AcademyConstants.WEBSERVICE_AUTHORIZATION, AcademyConstants.STR_BEARER_SPACE+strAccessToken);
		if(!YFCObject.isVoid(strPaypalReqId))
		con.setRequestProperty(AcademyConstants.STR_PAYPAL_REQUEST_ID,strPaypalReqId);
		log.verbose("con.etRequestMethod(AcademyConstants.WEBSERVICE_POST)..... " + con.getRequestMethod());
		log.verbose("con.getRequestProperty(AcademyConstants.WEBSERVICE_CONTENT_TYPE)..... " + con.getRequestProperty(AcademyConstants.WEBSERVICE_CONTENT_TYPE));
		log.verbose("con.getRequestProperty(AcademyConstants.WEBSERVICE_AUTHORIZATION)..... " + con.getRequestProperty(AcademyConstants.WEBSERVICE_AUTHORIZATION));
		log.verbose("con.getRequestProperty(AcademyConstants.STR_PAYPAL_REQUEST_ID)..... " + con.getRequestProperty(AcademyConstants.STR_PAYPAL_REQUEST_ID));
		String strConnectionTimeout = restParameters.get(AcademyConstants.ATTR_CONNECTION_TIMEOUT);
		String strReadTimeout = restParameters.get(AcademyConstants.ATTR_RESPONSE_TIMEOUT);	

		if(!YFSObject.isVoid(strConnectionTimeout)){
			int iConnectionTimeout = Integer.parseInt(strConnectionTimeout);
			con.setConnectTimeout(iConnectionTimeout);
		}
		if(!YFSObject.isVoid(strReadTimeout)){
			int iReadTimeout = Integer.parseInt(strReadTimeout);
			con.setReadTimeout(iReadTimeout);
		}

		try {
			DataOutputStream outDataStream = new DataOutputStream(con.getOutputStream());
			outDataStream.writeBytes(strJsonInput);
			outDataStream.flush();
			outDataStream.close();

			int iResponseCode = con.getResponseCode();
			log.verbose("iResponseCode... --->  "+iResponseCode);

			if(iResponseCode>=200 & iResponseCode<300 ){
				log.verbose("calling... ---> SUCCESS");
			}else{
				log.verbose("calling... failure ---> Response code("+con.getResponseCode()+")");
				log.verbose(con.getResponseMessage());
			}

			if((!String.valueOf(iResponseCode).startsWith(AcademyConstants.STATUS_CODE_2XX))&&
				(!String.valueOf(iResponseCode).startsWith(AcademyConstants.STATUS_CODE_4XX)) && 
				(!String.valueOf(iResponseCode).startsWith(AcademyConstants.STATUS_CODE_5XX)))
			{
				docResponse = XMLUtil.createDocument("json_object");
				docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE,String.valueOf(iResponseCode));
				docResponse.getDocumentElement().setAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_NAME,
						AcademyConstants.STR_UNHANDLED_ERROR);
				
			}else {
				docResponse = formatPayPalResponse(con);
			}	
		}
		//Updated the catch block to create Splunk Alerts
		catch (SSLHandshakeException sslExp) {
			strSplunkDtls = strSplunkDtls.replace("{ResponseCode}","RestCallFailed");
			log.info("Paypal Direct:: SSLHandshakeException :: "+sslExp.toString()+ strSplunkDtls);
			docResponse = XMLUtil.createDocument("json_object");
			docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, AcademyConstants.STATUS_CODE_504);
			docResponse.getDocumentElement().setAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_NAME, AcademyConstants.ATTR_SSL_HANDSHK_EXCEPTION);
			docResponse.getDocumentElement().setAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_MESSAGE, sslExp.getMessage());
			log.verbose(sslExp);
		}catch(SocketTimeoutException sockExp)//java.net.SocketTimeoutException
		{	
			strSplunkDtls = strSplunkDtls.replace("{ResponseCode}","RestCallFailed");
			log.info("Paypal Direct:: SocketTimeoutException :: " +sockExp.toString() + strSplunkDtls);
			docResponse = XMLUtil.createDocument("json_object");
			docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, AcademyConstants.STATUS_CODE_504);
			docResponse.getDocumentElement().setAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_NAME, AcademyConstants.STR_SOCKET_READ_TIMEOUT);
			docResponse.getDocumentElement().setAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_MESSAGE, sockExp.getMessage());
			log.verbose(sockExp);
		}catch(Exception exp) {
			strSplunkDtls = strSplunkDtls.replace("{ResponseCode}","RestCallFailed");
			log.info("Paypal Direct:: Exception :: " +exp.toString()+ strSplunkDtls);
			log.verbose(exp);
			docResponse = XMLUtil.getDocument("<Error HttpStatusCode='ERROR'  name='GENERAL_ERROR'/> ");
		}finally{
			if(con!=null){
				log.verbose("Paypal Direct:: Disconnecting -->con.disconnect()");
				con.disconnect();
			}
			if(!YFCObject.isVoid(docResponse))
			{
				log.verbose("Paypal Direct:: rest call output details:: ChargeTransactionKey:: "+
						strCTK+ " docResponse:"  + XMLUtil.getXMLString(docResponse));
			}
		}

		log.debug("End AcademyPayPalRestUtil.invokeDirectPaypalRestService() after direct paypal.");
		return docResponse;
	}
		
	//OMNI-71639 - START
	/**
	 * 
	 * @param env
	 * @param restParameters
	 * @return
	 * @throws Exception
	 */
	private static String  getAccessToken(YFSEnvironment env,Map<String, String> restParameters)throws Exception{
		log.debug("Begin of AcademyPayPalRestUtil.getAccessToken() after direct paypal.");
		log.verbose("Paypal Direct::  AcademyConstants.STR_PAYPAL_REUSE_ACCESS_TOKEN_DIRECT_CALL:"+restParameters.get(AcademyConstants.STR_PAYPAL_REUSE_ACCESS_TOKEN_DIRECT_CALL));		
		
		String strAccessToken="";
		Document docResponse = null;
		if("Y".equals(restParameters.get(AcademyConstants.STR_PAYPAL_REUSE_ACCESS_TOKEN_DIRECT_CALL)))
			{
				updateReuseAccessTokenMap(env,restParameters);
				strAccessToken = chmapToken.get(AcademyConstants.ATTR_ACCESS_TOKEN);
		}else {
				docResponse = createAccessToken(restParameters);
				strAccessToken = docResponse.getDocumentElement().getAttribute(AcademyConstants.ATTR_ACCESS_TOKEN);
		}
		
		log.debug("End of AcademyPayPalRestUtil.getAccessToken() after direct paypal.");
		return strAccessToken;
		
	}
	
	/**
	 * 
	 * @param env
	 * @param restParameters
	 * @throws Exception
	 */
	private static void updateReuseAccessTokenMap(YFSEnvironment env,Map<String, String> restParameters) throws Exception 
	{
		log.debug("Begin of Paypal Direct::AcademyPayPalRestUtil.updateReuseAccessTokenMap() method after direct paypal.");		
		YFCDate expiresTimeStamp=null;
		if(!chmapToken.isEmpty())			
		{
			expiresTimeStamp=YFCDate.getYFCDate(chmapToken.get(AcademyConstants.STR_PAYPAL_TOKEN_EXPIRY_TIMESTAMP));
		}
		log.verbose("Paypal Direct:: expiresTimeStamp: " + expiresTimeStamp );
		//Either first call or token value expired, make access token rest call get the token id and set exipired datetime which is expires-a configurable buffer 
		//in secs and keep in hashmap
		if(chmapToken.isEmpty() || YFCDateUtils.LTE(expiresTimeStamp, new YFCDate(), true)) //JVM variable checking
		{
				//String strDBTokenInp = "<AcadReuseAccessToken IgnoreOrdering='N' MaximumRecords='1' TargetSystem='PAYPAL'><OrderBy><Attribute Name='Modifyts' Desc='Y'/></OrderBy></AcadReuseAccessToken>";
				String strDBTokenInp = AcademyConstants.STR_GET_REUSE_TKN;
				Document docDBTokenInp=XMLUtil.getDocument(strDBTokenInp);
				Document docDBTokenOutput = AcademyUtil.invokeService(env, AcademyConstants.STR_GET_REUSE_TKN_LIST_SRV, docDBTokenInp);
				List<Node> lstDBTokenDeails = XMLUtil.getElementListByXpath(docDBTokenOutput,AcademyConstants.STR_GET_REUSE_TKN_LIST_XPATH);
				if(lstDBTokenDeails.size()>0)
				{
					Element eleDBTokenDeails= (Element)(lstDBTokenDeails.get(0));
					String strDBTokenKey= eleDBTokenDeails.getAttribute(AcademyConstants.STR_REUSE_TKN_KEY);
					String strDBExpiresTimeStamp=eleDBTokenDeails.getAttribute(AcademyConstants.STR_EXPIRES_ON);
					String strDBAccessToke=eleDBTokenDeails.getAttribute(AcademyConstants.STR_REUSE_TOKEN);
					String strDBExpDateTimeInJVMTimeZone=AcademyPaypalPaymentProcessingUtil.getDateInJVMDateTimeStamp(strDBExpiresTimeStamp,DateTimeFormatter.ISO_LOCAL_DATE_TIME); 
					YFCDate dbExpiresTimeStamp=YFCDate.getYFCDate(strDBExpDateTimeInJVMTimeZone);
					if(YFCDateUtils.LTE(dbExpiresTimeStamp, new YFCDate(), true)) //DB expiry checking  JVM1 -JVM1+1sec -1 sec -1sec 
					{
						log.info("Paypal Direct:: createAccessToken Call.");
						Document docToken = createAccessToken(restParameters);
						log.verbose("Paypal Direct:: docToken: " + XMLUtil.getXMLString(docToken));
						String strToken =  docToken.getDocumentElement().getAttribute(AcademyConstants.ATTR_ACCESS_TOKEN);
						String strSecs = docToken.getDocumentElement().getAttribute(AcademyConstants.STR_PAYPAL_TOKEN_EXPIRY_IN);
						log.verbose("Paypal Direct:: strSecs: " + strSecs );
						log.verbose("Paypal Direct:: strToken: " + strToken );
						log.verbose("Paypal Direct:: checking AcademyConstants.STR_PAYPAL_TOKEN_EXPIRY_BUFFER_IN_SECS: " + restParameters.get(AcademyConstants.STR_PAYPAL_TOKEN_EXPIRY_BUFFER_IN_SECS));
						//Get buffer in secs and deduct	 from expires on
						int iExpiryBuffer=Integer.parseInt(restParameters.get(AcademyConstants.STR_PAYPAL_TOKEN_EXPIRY_BUFFER_IN_SECS));
						expiresTimeStamp=new YFCDate();
						YFCDateUtils.addSeconds(expiresTimeStamp,(Integer.parseInt(strSecs)-iExpiryBuffer));
						log.verbose("Paypal Direct::after subtracting buffer secs from expiresTimeStamp:" + expiresTimeStamp);
						//Keep token and token expiry datetimestamp in the hashmap to check if expired token in future
						chmapToken.put(AcademyConstants.STR_PAYPAL_TOKEN_EXPIRY_TIMESTAMP, expiresTimeStamp.getString());
						chmapToken.put(AcademyConstants.ATTR_ACCESS_TOKEN,strToken);
						StringBuilder strBldUpdInp=new StringBuilder(AcademyConstants.STR_UPDT_REUSE_TKN1).append(strDBTokenKey).append(AcademyConstants.STR_UPDT_REUSE_TKN2).
								append(strToken).append(AcademyConstants.STR_UPDT_REUSE_TKN3).append(expiresTimeStamp.getString()).append(AcademyConstants.STR_UPDT_REUSE_TKN4);
						//strDBTokenInp = "<AcadReuseAccessToken ReuseAccessTokenKey='"+strDBTokenKey+"' AccessToken='"+strToken +"' ExpiresOn='" + expiresTimeStamp.getString() +"'/>";
						docDBTokenInp=XMLUtil.getDocument(strBldUpdInp.toString());
						log.verbose("Paypal Direct:: Updating AccessToken in DB");
						docDBTokenOutput = AcademyUtil.invokeService(env, AcademyConstants.STR_UPDATED_REUSE_TKN_SRV, docDBTokenInp);
						log.verbose("Paypal Direct:: AcademyUpdateReuseAccessToken Output: " + XMLUtil.getXMLString(docDBTokenOutput) );
					}else {//Keep latest DB Token details in JVM variables
						chmapToken.put(AcademyConstants.STR_PAYPAL_TOKEN_EXPIRY_TIMESTAMP, strDBExpDateTimeInJVMTimeZone);
						chmapToken.put(AcademyConstants.ATTR_ACCESS_TOKEN,strDBAccessToke);						
					}
				}else{//starting Call
					log.verbose("Paypal Direct:: Creating new record in DB");
					log.info("Paypal Direct:: createAccessToken Call.");
					Document docToken = createAccessToken(restParameters);
					log.verbose("Paypal Direct::  docToken: " + XMLUtil.getXMLString(docToken));
					String strToken =  docToken.getDocumentElement().getAttribute(AcademyConstants.ATTR_ACCESS_TOKEN);
					String strSecs = docToken.getDocumentElement().getAttribute(AcademyConstants.STR_PAYPAL_TOKEN_EXPIRY_IN);
					log.verbose("Paypal Direct:: strSecs: " + strSecs);
					log.verbose("Paypal Direct:: strToken: " + strToken );
					log.verbose("Paypal Direct:: AcademyConstants.STR_PAYPAL_TOKEN_EXPIRY_BUFFER_IN_SECS: " + restParameters.get(AcademyConstants.STR_PAYPAL_TOKEN_EXPIRY_BUFFER_IN_SECS));
					//Get buffer in secs and deduct	 from expires on
					int iExpiryBuffer=Integer.parseInt(restParameters.get(AcademyConstants.STR_PAYPAL_TOKEN_EXPIRY_BUFFER_IN_SECS));
					expiresTimeStamp=new YFCDate();
					YFCDateUtils.addSeconds(expiresTimeStamp,(Integer.parseInt(strSecs)-iExpiryBuffer));
					log.verbose("Paypal Direct:: AcademyPayPalRestUtil.updateReuseAccessTokenMap() method after subtracting buffer secs from expiresTimeStamp:" + expiresTimeStamp );
					//Keep token and token expiry datetimestamp in the hashmap to check if expired token in future
					chmapToken.put(AcademyConstants.STR_PAYPAL_TOKEN_EXPIRY_TIMESTAMP, expiresTimeStamp.getString());
					chmapToken.put(AcademyConstants.ATTR_ACCESS_TOKEN,strToken);
					StringBuilder strBldUpdInp=new StringBuilder(AcademyConstants.STR_CREATE_REUSE_TKN1).append(strToken).append(AcademyConstants.STR_CREATE_REUSE_TKN2).append(expiresTimeStamp.getString()).
							append(AcademyConstants.STR_CREATE_REUSE_TKN3);
					//strDBTokenInp = "<AcadReuseAccessToken  AccessToken='"+strToken +"' ExpiresOn='" + expiresTimeStamp.getString() +"'  TargetSystem='OMS'/>";
					docDBTokenInp=XMLUtil.getDocument(strBldUpdInp.toString());
					docDBTokenOutput = AcademyUtil.invokeService(env, AcademyConstants.STR_CREATE_REUSE_TKN_SRV, docDBTokenInp);
					log.verbose("Paypal Direct:: AcademyCreateReuseAccessToken Output: " + XMLUtil.getXMLString(docDBTokenOutput) );
				}
				
		}
		
		log.verbose("Paypal Direct:: AcademyPayPalRestUtil.updateReuseAccessTokenMap() method. chmapToken.toString()::" + chmapToken.toString());
		log.debug("End of AcademyPayPalRestUtil.updateReuseAccessTokenMap() method  after direct paypal.");
	}
	
	/**
	 * 
	 * @param inDoc
	 * @param docResponse
	 * @param strAPIName
	 * @param strInfoResp
	 * @param strHttpStatus
	 * @param strErrorName
	 * @param strErrodMessage
	 * @throws Exception
	 */
	private static Document printAndReturnRespErrorInfoDoc(String strInfoStatic,Document inDoc,String strAPIName,String strInfoResp,String strHttpStatus,
			String strErrorName,String strErrodMessage )throws Exception {		
		log.debug("Begin of AcademyPayPalRestUtil.printAndReturnRespErrorInfoDoc() method after direct paypal.");		
		String strSplunkDtls = prepareDetlsInfo(inDoc,strAPIName,strInfoResp);
		log.info(strInfoStatic +strSplunkDtls);
		Document docResponse = XMLUtil.createDocument("json_object");
		docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, strHttpStatus);
		docResponse.getDocumentElement().setAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_NAME, strErrorName);
		docResponse.getDocumentElement().setAttribute(AcademyConstants.PAYPAL_ERROR_RESPONSE_MESSAGE, strErrodMessage);
		log.verbose("Paypal Direct:: docResponse: " + XMLUtil.getXMLString(docResponse));
		log.debug("End of AcademyPayPalRestUtil.printAndReturnRespErrorInfoDoc() method after direct paypal.");
		return docResponse;
	}
	/**
	 * 
	 * @param inDoc
	 * @param APIName
	 * @param strInfoResp
	 * @throws Exception
	 */
	private static String prepareDetlsInfo(Document inDoc,String APIName,String strInfoResp)throws Exception {
		log.debug("Begin of AcademyPayPalRestUtil.prepareErrorRespInfo() method after direct paypal.");
		String strSplunkDtls =AcademyConstants.PAYPAL_TRACE_SPLUNK.replace("{APIName}",APIName);
		strSplunkDtls = strSplunkDtls.replace("{ResponseCode}",strInfoResp);
		String strCTK=XPathUtil.getString(inDoc,AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY);
		strSplunkDtls = strSplunkDtls.replace("{CTK}",strCTK);
		strSplunkDtls = strSplunkDtls.replace("{OrderNo}",
				XPathUtil.getString(inDoc,AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_NO));
		log.debug("End of AcademyPayPalRestUtil.prepareErrorRespInfo() method after direct paypal.");
		return strSplunkDtls;
	}
	//OMNI-71639 - END
	
	/**This method will get valid Paypal token and will make paypal rest call for all transaction-types
	 * and get approves response id to settle a transaction
	 * 
	 * @param env
	 * @param inDoc
	 * @param restParameters
	 * @param strJsonInput
	 * @return
	 * @throws Exception
	 */
	public static Document callDirectPaypalRestService(YFSEnvironment env,Document inDoc,Map<String, String> restParameters,String strJsonInput,String strTransactionType) throws Exception {
		log.debug("Begin of AcademyPayPalRestUtil.callDirectPaypalRestService() after direct paypal.");
		log.verbose("Paypal Direct::  inDoc:"+XMLUtil.getXMLString(inDoc));	
		log.verbose("Paypal Direct::  restParameters:"+restParameters);
		log.verbose("Paypal Direct::  strJsonInput:"+strJsonInput);	
		log.verbose("Paypal Direct::  strTransactionType:"+strTransactionType);
		Document docResponse = null;
		String strAccessToken = null;
		if(AcademyConstants.TRANSACTION_TYPE_AUTHORIZE.equals(strTransactionType))
		{
			String strCreditCardNo = XPathUtil.getString(inDoc, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.CREDIT_CARD_NO);
			restParameters.put(AcademyConstants.ATTR_AUTHORIZATION_ID, strCreditCardNo);
		}
		else if(AcademyConstants.TRANSACTION_TYPE_CAPTURE.equals(strTransactionType)||AcademyConstants.TRANSACTION_TYPE_VOID.equals(strTransactionType))
		{
			String strPaypalAuthId =  XPathUtil.getString(inDoc, AcademyConstants.STR_PAYMENT_PATH_AT +AcademyConstants.ATTR_AUTHORIZATION_ID);
			restParameters.put(AcademyConstants.ATTR_AUTHORIZATION_ID, strPaypalAuthId);
			
		}
		log.verbose("Paypal Direct::  After adding values restParameters:"+restParameters);		
		try {
			strAccessToken=getAccessToken(env,restParameters);
			log.verbose("Paypal Direct:: strAccessToken: " +strAccessToken);
			if(!YFSObject.isVoid(strAccessToken)){
				log.verbose("Paypal Direct:: Received AccessToken");
				String strSplunkDtls =AcademyConstants.PAYPAL_TRACE_SPLUNK.replace("{APIName}",strTransactionType);
				String strCTK=XPathUtil.getString(inDoc,AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY);
				strSplunkDtls = strSplunkDtls.replace("{CTK}",strCTK);
				strSplunkDtls = strSplunkDtls.replace("{OrderNo}",
						XPathUtil.getString(inDoc,AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_ORDER_NO));
				//OMNI-106361 - Stub for Direct Paypal - Start
				log.verbose("strInvokePaymentStub: " + strInvokePaymentStub);
				if (AcademyConstants.STR_YES.equalsIgnoreCase(strInvokePaymentStub)) {					
					docResponse = AcademyPaymentStub.invokePaymentStubForDirectPaypal(inDoc, strTransactionType);
				} else {
					//OMNI-106361 - Stub for Direct Paypal - End
					docResponse = invokeDirectPaypalRestService(strAccessToken,restParameters,strJsonInput,strCTK,strSplunkDtls);
				}
			}else {
				throw new Exception("Access Token is Null/Blank.");
				
			}
		}catch(SQLException sqlExp)
		{
			docResponse=printAndReturnRespErrorInfoDoc("Paypal Direct:: SQLException :: "+sqlExp.toString(),inDoc,strTransactionType,
					"SQLException","SQLException",
					sqlExp.getSQLState(),sqlExp.getMessage());
			log.verbose(sqlExp);
		}catch(Exception exp)
		{
			docResponse=printAndReturnRespErrorInfoDoc("Paypal Direct:: AccessTokenError :: "+exp.toString(),inDoc,strTransactionType,
					"TokenRestCallFailed",AcademyConstants.STATUS_CODE_504,
					AcademyConstants.STR_ACCESS_TOKEN_FAILED,"Access Token Call Failed");
			log.verbose(exp);
		}	

		log.verbose("Paypal Direct:: docResponse::" + XMLUtil.getXMLString(docResponse));
		log.debug("End of AcademyPayPalRestUtil.callDirectPaypalRestService() method after direct paypal.");
		return docResponse;	
		
	}
	
}

