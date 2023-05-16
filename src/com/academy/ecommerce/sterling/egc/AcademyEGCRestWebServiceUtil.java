package com.academy.ecommerce.sterling.egc;

/**#########################################################################################
 *
 * Project Name                : EGC
 * Module                      : OMNI-13739
 * Author                      : C0015606
 * Author Group				   : CTS - POD
 * Date                        : 10-DEC -2020 
 * Description				   : This class is common to invoke the WCS REST-WebService for Appeasement/Refund and Fraud interfaces
 * 								 Also converts the JSON response to XML format and returns the document to the agent code for further process											  								 
 * ------------------------------------------------------------------------------------------------------------------------
 * Date            	Author 		        			Version#       	Remarks/Description                      
 * ------------------------------------------------------------------------------------------------------------------------
 * 10-DEC-2020		CTS(C0015606)	 	 			 1.0           		Initial version
 * 04-JAN-2020		CTS(C0023461)					 2.0				Incorporate Security features from OMS side for the
 * 																	    Fraud and Cart Call Webservice calls (OMNI-17588)
 * #########################################################################################################################*/

import java.io.BufferedReader;
import java.util.zip.GZIPInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.json.JSONObject;
import org.w3c.dom.Document;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.ibm.sterling.afc.jsonutil.PLTJSONUtils;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSSystem;

public class AcademyEGCRestWebServiceUtil {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyEGCRestWebServiceUtil.class);

	public static Document invokeEGCWCSRestService(JSONObject JsonInput, String strWebServiceURL, String reqMethod)
			throws Exception {
		log.beginTimer("AcademyEGCRestWebServiceUtil.invokeEGCWCSRestService()");
		log.debug("JSON Input to AcademyEGCRestWebServiceUtil.invokeEGCWCSRestService() ::" + JsonInput.toString());

		Document docRestServiceResponse = null;
		String strWCSRestServiceResponseBody = "";
		String strWCSRestServiceResponseCode = "";
		String strWCSRestServiceApiKey = "";
		String strWCSRestServiceApiSecret = "";
		String strWCSRestServiceHMac = "";

		// OMNI-17588
		String strJSONInput = JsonInput.toString();
		Map<String, String> restParameters = new HashMap<String, String>();

		int iConnectTimeOut = Integer.parseInt(YFSSystem.getProperty(AcademyConstants.STR_EGC_CONNECT_TIMEOUT));
		int iReadTimeOut = Integer.parseInt(YFSSystem.getProperty(AcademyConstants.STR_EGC_READ_TIMEOUT));

		strWCSRestServiceApiKey = YFSSystem.getProperty(AcademyConstants.STR_EGC_WCS_API_KEY);
		strWCSRestServiceApiSecret = YFSSystem.getProperty(AcademyConstants.STR_EGC_WCS_API_SECRET);
		strWCSRestServiceHMac = YFSSystem.getProperty(AcademyConstants.STR_EGC_WCS_HMAC);

		// api security considerations
		log.verbose("APIKey: " + strWCSRestServiceApiKey + " ApiSecret: " + strWCSRestServiceApiSecret + " Hmac: "
				+ strWCSRestServiceHMac);
		restParameters.put(AcademyConstants.WEBSERVICE_APIKEY, strWCSRestServiceApiKey);
		restParameters.put(AcademyConstants.WEBSERVICE_API_SECRET, strWCSRestServiceApiSecret);
		restParameters.put(AcademyConstants.WEBSERVICE_HMAC, strWCSRestServiceHMac);
		restParameters.put(AcademyConstants.WEBSERVICE_PAYLOAD, strJSONInput);

		String strAuthorization = getMacValue(restParameters);

		log.verbose("Authorization: " + strAuthorization);

		restParameters.put(AcademyConstants.WEBSERVICE_AUTHORIZATION, strAuthorization);
		try {

			URL url = new URL(strWebServiceURL);
			HttpsURLConnection httpCon = (HttpsURLConnection) url.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod(reqMethod);
			httpCon.setConnectTimeout(iConnectTimeOut);
			httpCon.setReadTimeout(iReadTimeOut);
			//httpCon.setRequestProperty(AcademyConstants.STR_ACCEPT_ENCODING, AcademyConstants.STR_GZIP);
			httpCon.setRequestProperty(AcademyConstants.WEBSERVICE_CONTENT_TYPE,
					AcademyConstants.ACCEPT_APPLICATION_JSON);
			httpCon.setRequestProperty(AcademyConstants.WEBSERVICE_APIKEY, strWCSRestServiceApiKey);
			httpCon.setRequestProperty(AcademyConstants.WEBSERVICE_AUTHORIZATION,
					restParameters.get(AcademyConstants.WEBSERVICE_AUTHORIZATION));
			try {
				log.verbose("Content type ---> " + AcademyConstants.ACCEPT_APPLICATION_JSON);
				log.verbose("URL being used --->" + httpCon.getURL().toString());
				log.verbose("Input used --->" + strJSONInput);
				log.verbose("ConnectTimeout ---> " + iConnectTimeOut);
				log.verbose("ReadTimeout ---> " + iReadTimeOut);

				DataOutputStream outDataStream = new DataOutputStream(httpCon.getOutputStream());
				outDataStream.writeBytes(strJSONInput);
				outDataStream.flush();
				outDataStream.close();

				log.verbose("ResponseCode : " + httpCon.getResponseCode());
				log.verbose("ResponseMessage : " + httpCon.getResponseMessage());

				strWCSRestServiceResponseBody = httpCon.getResponseMessage();
				docRestServiceResponse = formatEGCResponse(httpCon);
			}

			catch (SocketTimeoutException e) {
				
				log.info(
						"Exception occurred in AcademyEGCRestWebServiceUtil.invokeEGCWCSRestService(): "
						+ "\nInput to WCS Webservice call: \n" + JsonInput  
						+ "\nWebService Exception:: SocketTimeoutException :: "
								+ e.toString() + "\nException Stack Trace :: \n" + e.getStackTrace().toString());
				docRestServiceResponse = XMLUtil.createDocument(AcademyConstants.ELE_ERROR);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE,
						AcademyConstants.STATUS_CODE_504);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_EXCEPTION_TYPE,
						AcademyConstants.SOCKET_TIME_OUT_EXCEPTION);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_ERROR_MESSAGE,
						e.getMessage());
				log.info("Exception occurred in AcademyEGCRestWebServiceUtil.invokeEGCWCSRestService() while connecting WCS for processing the EGC:\n"
						+ XMLUtil.getXMLString(docRestServiceResponse));
			} catch (SSLHandshakeException e) {

				log.info(
						"Exception occurred in AcademyEGCRestWebServiceUtil.invokeEGCWCSRestService():"
								+ "\nInput to WCS Webservice call: \n" + JsonInput  
								+ "\nWebService Exception:: SSLHandshakeException :: "
								+ e.toString() + "\nException Stack Trace :: \n" + e.getStackTrace().toString());
				docRestServiceResponse = XMLUtil.createDocument(AcademyConstants.ELE_ERROR);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE,
						AcademyConstants.STATUS_CODE_504);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_EXCEPTION_TYPE,
						AcademyConstants.SSL_HANDSHAKE_EXCEPTION);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_ERROR_MESSAGE,
						e.getMessage());
				log.info("Exception occurred in AcademyEGCRestWebServiceUtil.invokeEGCWCSRestService() while connecting WCS for processing the EGC:\n"
						+ XMLUtil.getXMLString(docRestServiceResponse));
			} catch (Exception e) {
				log.info(
						"Exception occurred in AcademyEGCRestWebServiceUtil.invokeEGCWCSRestService(): "
						+ "\nInput to WCS Webservice call: \n" + JsonInput 
						+ "\nWebService Exception:: Generic Exception :: "
								+ e.toString() + "\nException Stack Trace :: \n" + e.getStackTrace().toString());
				docRestServiceResponse = XMLUtil.createDocument(AcademyConstants.ELE_ERROR);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE,
						AcademyConstants.STATUS_CODE_504);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_EXCEPTION_TYPE,
						AcademyConstants.STR_GENERIC_EXCEPTION);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_ERROR_MESSAGE,
						e.getMessage());
				log.info("Exception occurred in AcademyEGCRestWebServiceUtil.invokeEGCWCSRestService() while connecting WCS for processing the EGC:\n"
						+ XMLUtil.getXMLString(docRestServiceResponse));
			}
		} catch (Exception e) {
			log.info(
					"Exception occurred in AcademyEGCRestWebServiceUtil.invokeEGCWCSRestService() before invoking web service call :: "
							+ "\nInput to WCS Webservice call: \n" + JsonInput 
							+ "\nException::" + e.getMessage() + " \n " + e.toString()
			 				+ "\nException Stack Trace :: \n" + e.getStackTrace().toString());
			docRestServiceResponse = XMLUtil.createDocument(AcademyConstants.ELE_ERROR);
			docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE,
					AcademyConstants.STATUS_CODE_504);
			docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_ERROR_MESSAGE,
					e.getMessage());
			log.info("Exception occurred in AcademyEGCRestWebServiceUtil.invokeEGCWCSRestService() while connecting WCS for processing the EGC:\n"
					+ XMLUtil.getXMLString(docRestServiceResponse));
		}

		log.verbose("Webservice Response XML ::" + XMLUtil.getXMLString(docRestServiceResponse));
		log.endTimer("AcademyEGCRestWebServiceUtil.invokeEGCWCSRestService()");

		return docRestServiceResponse;
	}
	private static Document formatEGCResponse(HttpsURLConnection httpCon) throws Exception {
		log.beginTimer("AcademyEGCRestWebServiceUtil.formatEGCResponse()");
		Document docResponse = null;
		try {

			log.verbose("Encoding --->" + httpCon.getContentEncoding());
			log.verbose("Content Type ---> " + httpCon.getContentType());
			log.verbose("Length ---> " + httpCon.getContentLength());
			log.verbose("Output ---> " + httpCon.getDoOutput());
			BufferedReader br;
			InputStream inStream = httpCon.getResponseCode() / 100 == 2 ? httpCon.getInputStream()
					: httpCon.getErrorStream();
			if (AcademyConstants.STR_GZIP.equalsIgnoreCase(httpCon.getContentEncoding())) {
				br = new BufferedReader(new InputStreamReader(new GZIPInputStream(inStream)));
			} else {
				br = new BufferedReader(new InputStreamReader((inStream)));
			}

			StringBuilder sb = new StringBuilder();
			String strOutput;
			while ((strOutput = br.readLine()) != null) {
				sb.append(strOutput);
			}
			br.close();
			JSONObject json = new JSONObject(sb.toString());

			docResponse = PLTJSONUtils.getXmlFromJSON(json.toString(), AcademyConstants.ATTR_RESPONSE);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.info("Exception occurred in AcademyEGCRestWebServiceUtil.formatEGCResponse() " + e);
			e.printStackTrace();
		}

		Integer iResponseCode = httpCon.getResponseCode();
		String strResponseCode = iResponseCode.toString();
		String strResponseMessage = httpCon.getResponseMessage();
		docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, strResponseCode);
		docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_RESPONSE_MESSAGE, strResponseMessage);

		log.endTimer("AcademyEGCRestWebServiceUtil.formatEGCResponse()");
		return docResponse;

	}

	public static String getMacValue(Map<String, String> data) throws Exception {

		log.beginTimer("AcademyEGCRestWebServiceUtil.getMacValue()");

		Mac mac = Mac.getInstance(data.get(AcademyConstants.WEBSERVICE_HMAC));
		String strApiKey = data.get(AcademyConstants.WEBSERVICE_APIKEY);
		String strApiSecret = data.get(AcademyConstants.WEBSERVICE_API_SECRET);
		String strHMac = data.get(AcademyConstants.WEBSERVICE_HMAC);

		log.verbose("ApiSecret: " + strApiSecret);
		log.verbose("HMac: " + data.get(AcademyConstants.WEBSERVICE_HMAC));

		SecretKeySpec secret_key = new SecretKeySpec(strApiSecret.getBytes(), strHMac);
		mac.init(secret_key);
		StringBuilder stringBulder = new StringBuilder();
		stringBulder.append(strApiKey);

		if (data.get(AcademyConstants.WEBSERVICE_PAYLOAD) != null) {
			log.verbose("Appending payload::::: " + data.get(AcademyConstants.WEBSERVICE_PAYLOAD));
			stringBulder.append(data.get(AcademyConstants.WEBSERVICE_PAYLOAD));
		}

		log.verbose("Buffer Output:: " + stringBulder);
		byte[] macHash = mac.doFinal(stringBulder.toString().getBytes(AcademyConstants.STR_UTF8));

		// We will use DatatypeConveter as Base64 (commons.codec jar) is being
		// referenced in many other
		// jar files and which causes No Such method error due to referencing old jar
		// file
		String strAuthorization = DatatypeConverter.printBase64Binary(toHex(macHash));
		log.verbose("HMAC Output(DataTyPeConverter):::" + strAuthorization);

		log.endTimer("AcademyEGCRestWebServiceUtil.getMacValue()");

		return strAuthorization;
	}

	public static byte[] toHex(byte[] arr) {
		log.beginTimer("AcademyEGCRestWebServiceUtil.toHex()");
		String hex = DatatypeConverter.printHexBinary(arr);
		log.verbose("Value printed by DatatypeConverter.printHexBinary --->" + hex);
		// We will use DatatypeConveter as Hex (commons.codec jar) is being referenced
		// in many other
		// jar files and which causes No Such method error due to referencing old jar
		// file
		log.endTimer("AcademyEGCRestWebServiceUtil.toHex()");
		return hex.toLowerCase().getBytes();
	}
}
