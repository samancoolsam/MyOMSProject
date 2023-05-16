package com.academy.ecommerce.sterling.util;

/**#########################################################################################
*
* Project Name                : Klarna
* Module                      : OMNI-92705
* Author                      : C0023461
* Author Group				  : CTS - POD
* Date                        : 08-DEC-2022 
* Description				  : This class is common to invoke the Klarna REST-WebService for below interfaces
* 									1. extend-authorization-time
* 									2. captures
* 									3. cancel
* 									4. release-remaining-authorization
* 									5. refunds
 * 								 Also converts the JSON response to XML format and returns the document to the
 * 								 calling UE code for further process								 
* ---------------------------------------------------------------------------------
* Date            	Author         		Version#       		Remarks/Description                      
* ---------------------------------------------------------------------------------
* 12-DEC-2022		CTS  	 			 1.0           		Initial version
*
* #########################################################################################*/


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

import javax.net.ssl.SSLHandshakeException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.json.JSONObject;
import org.apache.commons.json.utils.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyKlarnaInterfaceUtil {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyKlarnaInterfaceUtil.class);
	public static  Map<Integer,String> MAP_KLARNA_TRAN_TYPE=new HashMap<Integer,String>();
	static
	{
		{{ MAP_KLARNA_TRAN_TYPE.put(1,"extend-authorization-time");   
		MAP_KLARNA_TRAN_TYPE.put(2,"captures");    
		MAP_KLARNA_TRAN_TYPE.put(3,"cancel");  
		MAP_KLARNA_TRAN_TYPE.put(4,"release-remaining-authorization"); 
		MAP_KLARNA_TRAN_TYPE.put(5,"refunds"); 
		}};
	}
	
	/* This method is used to invoke Klarna APIs from API Tester only */
		
	public Document invokeKlarna(YFSEnvironment env, Document inDoc) throws Exception {
		
		log.beginTimer("AcademyKlarnaInterfaceUtil.invokeKlarna(");
		log.verbose("AcademyKlarnaInterfaceUtil.invokeKlarna() :: Input document : " + XMLUtil.getXMLString(inDoc));
		Element eleInDoc = inDoc.getDocumentElement();
		String strKlarnOrderID = eleInDoc.getAttribute("KlarnaOrderID");
		String strTransactionType = eleInDoc.getAttribute("TransactionType");
		int iTransactionType = Integer.parseInt(strTransactionType);
		String strRequestAmount = eleInDoc.getAttribute("RequestAmount");
		String strIdempotencyKey = eleInDoc.getAttribute("IdempotencyKey");

		Document docKlarnaResponse = invokeKlarnaService(env,strKlarnOrderID, iTransactionType, strRequestAmount, strIdempotencyKey);
		Element eleKlarnaResponse = docKlarnaResponse.getDocumentElement();
		String strHttpStatusCode = eleKlarnaResponse.getAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE);
		boolean isisConnectionSuccessfull =	isConnectionSuccessfull(docKlarnaResponse);
		if(!YFCCommon.isVoid(docKlarnaResponse)) {
			log.verbose("AcademyKlarnaInterfaceUtil.invokeKlarna() :: Output document : " + XMLUtil.getXMLString(docKlarnaResponse));
		}
		
		if(!isisConnectionSuccessfull || strHttpStatusCode.startsWith("4") || strHttpStatusCode.startsWith("5") ) {
			YFSException yfsExp =  new YFSException();
			yfsExp.setErrorCode(AcademyConstants.STR_EXTN_ACADEMY_08);
			yfsExp.setErrorDescription(getKlarnaErrorMessage(docKlarnaResponse));
			throw yfsExp;
		}

		log.endTimer("AcademyKlarnaInterfaceUtil.invokeKlarna(");
		return docKlarnaResponse;		
	}
	
	/* This method is used to invoke Klarna APIs */

	public static Document invokeKlarnaService(YFSEnvironment env,String klarnaOrderId, int iTransactionType,String strRequestAmount,
			String idempotencyKey) throws ParserConfigurationException {
		
		log.beginTimer("AcademyKlarnaInterfaceUtil.invokeKlarnaService()");
		log.verbose("AcademyKlarnaInterfaceUtil.invokeKlarnaService() - START");

		Document docServiceOut = null;
		HttpURLConnection conn = null;
		Document docKlarnarequest = null;		
		String strJSONInput = null;
		String authToken = null;
		String strReference = null;
		String strKlarnaURL = YFSSystem.getProperty(AcademyConstants.KLARNA_URL);
		String strUserName = YFSSystem.getProperty(AcademyConstants.KLARNA_USERNAME);
		String strPassword = YFSSystem.getProperty(AcademyConstants.KLARNA_PASSWORD);
	
		/*
		 String strKlarnaURL = "https://api-na.playground.klarna.com/ordermanagement/v1/orders/{OrderId}/{TransactionType}";
		 String strUserName ="UG100344_896cb912f8b1";
		 String strPassword ="iVy6lAkGszYWsjxg";
		 */
		
		int iResponseCode = 0;
		// Append Klarna Order Id to URL
		if (strKlarnaURL.contains("{OrderId}")) {
			strKlarnaURL = strKlarnaURL.replace("{OrderId}", klarnaOrderId);
		}
		if (strKlarnaURL.contains("{TransactionType}")) {
			strKlarnaURL = strKlarnaURL.replace("{TransactionType}", MAP_KLARNA_TRAN_TYPE.get(iTransactionType));
		}
		try {
			 authToken = Base64.getEncoder().encodeToString((strUserName + ":" + strPassword).getBytes("UTF-8"));

			URL url = new URL(strKlarnaURL);
			log.verbose("The URL is : : : " + url);

			conn = (HttpURLConnection) url.openConnection();
			//TODO strKlarnaTimeout=20
			String strKlarnaTimeout =  YFSSystem.getProperty(AcademyConstants.KLARNA_TIMEOUT);
			String strKlarnaReadTimeout = YFSSystem.getProperty(AcademyConstants.KLARNA_READ_TIMEOUT);
			
			if (!YFSObject.isVoid(strKlarnaTimeout)) {
				log.verbose("Klarna Connection Timeout - " + strKlarnaTimeout);
				int iConnectionTimeout = Integer.parseInt(strKlarnaTimeout);
				conn.setConnectTimeout(iConnectionTimeout);
			}
			if (!YFSObject.isVoid(strKlarnaReadTimeout)) {
				log.verbose("Klarna Connection Readout - " + strKlarnaReadTimeout);
				int iReadTimeout = Integer.parseInt(strKlarnaReadTimeout);
				conn.setReadTimeout(iReadTimeout);
			}

			conn.setRequestMethod(AcademyConstants.STR_POST);
			conn.setRequestProperty(AcademyConstants.WEBSERVICE_AUTHORIZATION,
					AcademyConstants.STR_BASIC_SPACE + authToken);
			conn.setRequestProperty(AcademyConstants.WEBSERVICE_CONTENT_TYPE, AcademyConstants.ACCEPT_APPLICATION_JSON);
			
			if (!YFCObject.isVoid(idempotencyKey)) {
                strReference = idempotencyKey.substring(idempotencyKey.length() - 11);
				env.setTxnObject(AcademyConstants.STR_KLARNA_REFERENCE, strReference);
				StringJoiner strJoiner = new StringJoiner("-");
				
				strJoiner.add(idempotencyKey.substring(0, 4)).add(idempotencyKey.substring(4, 8))
                .add(idempotencyKey.substring(8, 12)).add(idempotencyKey.substring(idempotencyKey.length()-16, idempotencyKey.length()-12))
                .add(idempotencyKey.substring(idempotencyKey.length()-4, idempotencyKey.length()));

				UUID idempotencyKeyUUID = UUID.fromString(strJoiner.toString());
				idempotencyKey = idempotencyKeyUUID.toString();

			} else {
				
				idempotencyKey = AcademyPayeezyRestUtil.getAutoGeneratedUUID();
				
			}
			log.verbose("idempotencyKey :: " + idempotencyKey);
			conn.setRequestProperty(AcademyConstants.KLARNA_IDEMPOTENCY_KEY, idempotencyKey);
			conn.setDoOutput(true);

			// Input JSON is needed for Capture, Refund only
			if (AcademyConstants.INT_KLARNA_CAPTUPE_TRANSACTION==iTransactionType ||
					AcademyConstants.INT_KLARNA_REFUND_TRANSACTION==iTransactionType) {

				JSONObject json = new JSONObject();
				Double dRequestAmount = Double.parseDouble(strRequestAmount);
				//Updating request amount in format that can be read by Klarna 
				DecimalFormat df = new DecimalFormat("#"); 
				strRequestAmount = df.format(dRequestAmount*100);
								
				if (strRequestAmount.startsWith(AcademyConstants.STR_HYPHEN)) {
					strRequestAmount = strRequestAmount.split(AcademyConstants.STR_HYPHEN)[1];
				} 
				
				int iRequestAmount = Integer.parseInt(strRequestAmount);

				//Setting Captured Amount or REfunded Amount based on Transaction type
				if (AcademyConstants.INT_KLARNA_CAPTUPE_TRANSACTION==iTransactionType) {
					json.put(AcademyConstants.KL_ATTR_CAPTURED_AMT, iRequestAmount);

				} else if (AcademyConstants.INT_KLARNA_REFUND_TRANSACTION==iTransactionType) {
					json.put(AcademyConstants.KL_ATTR_REFUNDED_AMT, iRequestAmount);

				}
                 if (!YFCObject.isVoid(strReference)) {
					json.put(AcademyConstants.STR_KLARNA_REFERENCE, strReference);
				}
				 strJSONInput = json.toString();
				log.verbose("JSON Input is :: " + strJSONInput);

				byte[] postDataBytes = strJSONInput.getBytes("UTF-8");
				conn.getOutputStream().write(postDataBytes);
			}
			log.beginTimer("AcademyKlarnaInterfaceUtil.invokeKlarnaWebService");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					conn.getResponseCode() / 100 == 2 ? conn.getInputStream() : conn.getErrorStream()));
			StringBuilder out = new StringBuilder();
			String output;

			while ((output = br.readLine()) != null) {
				out.append(output);
			}
			log.endTimer("AcademyKlarnaInterfaceUtil.invokeKlarnaWebService");
			iResponseCode = conn.getResponseCode();
			log.verbose("Klarna ResponseCode... --->  " + iResponseCode);
			
			if (conn.getResponseCode() / 100 == 2) {
				log.verbose("Klarna ---> SUCCESS");
			} else {
				log.verbose("Klarna --> FAILURE ---> Response code : " + conn.getResponseCode());
				log.verbose("Response Message : " + conn.getResponseMessage());
			}

			String strServiceOutput = out.toString();
			log.verbose("The Klarna JSON Response is : " + strServiceOutput);
			if (!YFCCommon.isVoid(strServiceOutput)) {
				InputStream in = IOUtils.toInputStream(strServiceOutput);
				String xml = XML.toXml(in);

				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				docServiceOut = builder.parse(new InputSource(new StringReader(xml)));
			} else {
				docServiceOut = XMLUtil.createDocument("KlarnaResponse");
			}
			// Update the response to convert back the amount into decimals
			Element eleResponse = docServiceOut.getDocumentElement();
			log.verbose("The Klarna XML Response is : " + XMLUtil.getXMLString(docServiceOut));

			Map<String, List<String>> hmRespHeaders = conn.getHeaderFields();
			log.verbose("The Klarna XML Response with Headers ::");
			
			for (Map.Entry<String, List<String>> entry : hmRespHeaders.entrySet()) {
				
				List<String> liRespHeaderValue = entry.getValue();
				String respHeaderKey = entry.getKey();
				String respHeaderValue = "";
				
				if (YFCCommon.isVoid(respHeaderKey)) {
					respHeaderKey = AcademyConstants.ATTR_RESPONSE_CODE;
				}
				
				log.verbose("Key : " + respHeaderKey + " ,Value : " + liRespHeaderValue);
				
				if (!liRespHeaderValue.isEmpty()) {
					respHeaderValue = liRespHeaderValue.get(0);
				}
				eleResponse.setAttribute(respHeaderKey, respHeaderValue);
			}
			docServiceOut.getDocumentElement().setAttribute(AcademyConstants.ATTR_KLARNA_OPERATION, MAP_KLARNA_TRAN_TYPE.get(iTransactionType));
			docServiceOut.getDocumentElement().setAttribute(AcademyConstants.KLARNA_SERVICE_INVOCATION_STATUS, AcademyConstants.STR_SUCCESS);
			docServiceOut.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE,Integer.toString(iResponseCode));
			log.verbose("The Klarna XML Response after adding Response Headers is : " + XMLUtil.getXMLString(docServiceOut));
			
		} catch(Exception e) {
			
			if(e instanceof SocketTimeoutException) {

				log.info("Klarna Exception:: SocketTimeoutException :: " + e.toString() + " Exception Stack Trace" + Arrays.toString(e.getStackTrace()));
				docServiceOut = prepareErrorResponse(idempotencyKey, Integer.parseInt(AcademyConstants.STATUS_CODE_501), e.getClass().getCanonicalName());

			} else if(e instanceof SSLHandshakeException) {

				log.info("Klarna Exception:: SSLHandshakeException :: " + e.toString() + " Exception Stack Trace" + Arrays.toString(e.getStackTrace()));
				docServiceOut = prepareErrorResponse(idempotencyKey, Integer.parseInt(AcademyConstants.STATUS_CODE_501), e.getClass().getCanonicalName());
			} else {

				log.info("Klarna Exception:: Generic Webservice Invocation :: " + e.toString() + " Exception Stack Trace"+ Arrays.toString(e.getStackTrace()));
				docServiceOut = prepareErrorResponse(idempotencyKey, iResponseCode, e.getClass().getCanonicalName());

			}
		} 
		finally {
            if (!YFSObject.isVoid(docServiceOut)) {
			Element eleKlarnaResponse = docServiceOut.getDocumentElement();
			String strHttpStatusCode = eleKlarnaResponse.getAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE);

			if (strHttpStatusCode.startsWith("4") || strHttpStatusCode.startsWith("5")) {
				docKlarnarequest = prepareKlarnaRequest(strKlarnaURL, iTransactionType, strJSONInput, idempotencyKey,
						authToken);
				env.setTxnObject(AcademyConstants.STR_KLARNA_REQUEST, docKlarnarequest);
			}
         }
		}

		
		log.verbose("Class: AcademyKlarnaInterfaceUtil.invokeKlarnaService() - END" + XMLUtil.getXMLString(docServiceOut));
		log.endTimer("AcademyKlarnaInterfaceUtil.invokeKlarnaService");

		return docServiceOut;
	}

	/* This method is used to prepeare Error Responses from Klarna */
	
	public static Document prepareErrorResponse(String idempotencyKey, int iResponseCode, String exceptionServer) throws ParserConfigurationException {
		log.beginTimer("AcademyKlarnaInterfaceUtil.prepareErrorResponse()");
		Document docServiceOut;
		docServiceOut = XMLUtil.createDocument("KlarnaResponse");
		Element eleResponse = docServiceOut.getDocumentElement();
		if(iResponseCode!=0) {
			eleResponse.setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE,
					Integer.toString(iResponseCode));
		}
		else {
			eleResponse.setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE,
					AcademyConstants.STATUS_CODE_504);
		}
		eleResponse.setAttribute(AcademyConstants.KLARNA_SERVICE_INVOCATION_STATUS, AcademyConstants.STR_FAIL);
		eleResponse.setAttribute(AcademyConstants.KLARNA_IDEMPOTENCY_KEY, idempotencyKey);
		eleResponse.setAttribute("error-code", AcademyConstants.STR_SERVICE_UNAVAILABLE);
		eleResponse.setAttribute("error-message", exceptionServer);
		log.verbose("AcademyKlarnaInterfaceUtil.prepareErrorResponse() :: Output :: " + XMLUtil.getXMLString(docServiceOut));
		log.endTimer("AcademyKlarnaInterfaceUtil.prepareErrorResponse()");
		return docServiceOut;
	}

	
	/**
	 * This Method evaluates Klarna Connecction Response
	 * @param docKlarnaResponse
	 * @return <p><strong>true</strong> when Klarna successful connecction  </p>
	 *         <p><strong>false</strong> when Klarna un-successful connecction  </p>
	 */
	public static boolean isConnectionSuccessfull(Document docKlarnaResponse) {
		log.beginTimer("START ::  isConnectionSuccessfull :: START");
		log.verbose("START ::  isConnectionSuccessfull :: START");
		boolean isConnectionSuccessfull = true;
		Element eleKlarnaResponse = docKlarnaResponse.getDocumentElement();
		String strInternalError = eleKlarnaResponse.getAttribute(AcademyConstants.KLARNA_SERVICE_INVOCATION_STATUS);
		if (!YFCCommon.isVoid(strInternalError) && strInternalError.equalsIgnoreCase(AcademyConstants.STR_FAIL)) {
			isConnectionSuccessfull = false;
		}
		log.verbose("END :: isConnectionSuccessfull :: END  " + isConnectionSuccessfull);
		log.endTimer("END :: isConnectionSuccessfull :: END");
		return isConnectionSuccessfull;
	}
	/* This method is used for prepare Klarna Request that needs to storing in yfs_export on failure */
	public static Document prepareKlarnaRequest(String strKlarnaURL, int iTransactionType, String strJSONInput,
			String idempotencyKey, String authToken) throws ParserConfigurationException {
		log.beginTimer("AcademyKlarnaInterfaceUtil.prepareKlarnaRequest()");
		Document docKlarnaRequest;
		docKlarnaRequest = XMLUtil.createDocument(AcademyConstants.STR_KLARNA_REQUEST);
		Element eleKlarnaReq = docKlarnaRequest.getDocumentElement();

		Element eleKlarnaRequest = docKlarnaRequest.createElement(AcademyConstants.STR_KLARNA_REQUEST_URL);
		eleKlarnaRequest.appendChild(docKlarnaRequest.createTextNode(strKlarnaURL));
		eleKlarnaReq.appendChild(eleKlarnaRequest);

		Element eleidempotencyKey = docKlarnaRequest.createElement(AcademyConstants.KLARNA_IDEMPOTENCY_KEY);
		eleidempotencyKey.appendChild(docKlarnaRequest.createTextNode(idempotencyKey));
		eleKlarnaReq.appendChild(eleidempotencyKey);

		Element eleRequestType = docKlarnaRequest.createElement(AcademyConstants.STR_KLARNA_REQUEST_TYPE);
		eleRequestType.appendChild(docKlarnaRequest.createTextNode(AcademyConstants.STR_POST));
		eleKlarnaReq.appendChild(eleRequestType);
		Element eleContentType = docKlarnaRequest.createElement(AcademyConstants.WEBSERVICE_CONTENT_TYPE);
		eleContentType.appendChild(docKlarnaRequest.createTextNode(AcademyConstants.ACCEPT_APPLICATION_JSON));
		eleKlarnaReq.appendChild(eleContentType);

		if (AcademyConstants.INT_KLARNA_CAPTUPE_TRANSACTION == iTransactionType
				|| AcademyConstants.INT_KLARNA_REFUND_TRANSACTION == iTransactionType) {
			if (!YFSObject.isVoid(strJSONInput)) {
				Element eleRequestBody = docKlarnaRequest.createElement(AcademyConstants.STR_KLARNA_REQUEST_BODY);
				eleRequestBody.appendChild(docKlarnaRequest.createTextNode(strJSONInput));
				eleKlarnaReq.appendChild(eleRequestBody);
			}
		}

		log.verbose("AcademyKlarnaInterfaceUtil.prepareKlarnaRequest() :: Output :: "
				+ XMLUtil.getXMLString(docKlarnaRequest));
		log.endTimer("AcademyKlarnaInterfaceUtil.prepareKlarnaRequest()");
		return docKlarnaRequest;
	}
	
	/**
	 * This Method returns error_messages from Klarna <p> trims message if length > 100 </P> 
	 * @param inDocUE
	 * @param docKlarnaResponse
	 * @return ErrorMessage
	 * @throws Exception
	 */
	public static String getKlarnaErrorMessage(Document docKlarnaResponse) throws Exception {
		String strErrorMsg = null;
		strErrorMsg = XPathUtil.getString(docKlarnaResponse,AcademyConstants.XPATH_ERROR_MESSAGES);
		strErrorMsg = YFCCommon.isVoid(strErrorMsg) ? XPathUtil.getString(docKlarnaResponse,AcademyConstants.XPATH_ERRORMESSAGE) : strErrorMsg ;
		if(strErrorMsg.length() > 100 ) {
			strErrorMsg = strErrorMsg.replace(" ","");
			if(strErrorMsg.length() > 100 ) {
				strErrorMsg = strErrorMsg.substring(0, 100);
			}
		}
		return strErrorMsg;
	}
}
