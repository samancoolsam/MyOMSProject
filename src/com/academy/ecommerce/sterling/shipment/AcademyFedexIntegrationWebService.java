//Start OMNI-66078 CLONE - Add FedEx tracking status to STS queries
package com.academy.ecommerce.sterling.shipment;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;
import org.w3c.dom.Document;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSSystem;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.yantra.yfc.core.YFCObject;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.json.utils.XML;
import org.xml.sax.SAXException;

public class AcademyFedexIntegrationWebService {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyFedexIntegrationWebService.class);
	private static Properties props;

	public void setProperties(Properties props) {
		this.props = props;
	}

	public  Document getACADFedexUpdates(Document inDoc) throws Exception {
		log.beginTimer("AcademyACADFedexIntegrationService.getACADFedexUpdates()");
		log.verbose("AcademyACADFedexIntegrationService.getACADFedexUpdates()  -- Start");
		Document outDocstrACADFedexResponse = null;
		String strACADFedexWSJSONInput = null;
		String strACADFedexURL = null;
		String strIsACADFedexRestAPIEnabled = props.getProperty(AcademyConstants.IS_ACADFedex_REST_API_ENABLED);
		log.verbose("strIsACADFedexRestAPIEnabled ::" + strIsACADFedexRestAPIEnabled);
		strACADFedexURL = YFSSystem.getProperty(AcademyConstants.STR_ACADFedex_INTERFACE_URL);
		log.verbose("strACADFedexURL :: " + strACADFedexURL);

		if (!YFCObject.isVoid(strIsACADFedexRestAPIEnabled) && strIsACADFedexRestAPIEnabled.equalsIgnoreCase("Y")) {

			// Preparing JSON input for ACADFedex RestAPI call
			strACADFedexWSJSONInput = prepareJSONACADFedexInput(inDoc);
			log.verbose("strJSonACADFedexInput :: " + strACADFedexWSJSONInput);

			// Invoking ACADFedex RestAPI
			if (!YFCObject.isVoid(strACADFedexWSJSONInput) && !YFCObject.isVoid(strACADFedexURL)) {
				outDocstrACADFedexResponse = invokeACADFedexRestAPIWebService(strACADFedexWSJSONInput, strACADFedexURL);
				log.verbose("outDocstrACADFedexResponse ::\n " + XMLUtil.getXMLString(outDocstrACADFedexResponse));
			}

		} else {
			Element elerRootelement = inDoc.getDocumentElement();
			elerRootelement.setAttribute(AcademyConstants.IS_ACADFedex_REST_API_ENABLED, strIsACADFedexRestAPIEnabled);
			outDocstrACADFedexResponse = inDoc;
			log.verbose("outDocstrACADFedexResponse :: \n" + XMLUtil.getXMLString(outDocstrACADFedexResponse));
		}

		log.verbose("Final output :: \n" + XMLUtil.getXMLString(outDocstrACADFedexResponse));
		log.verbose("AcademyACADFedexIntegrationRestAPIService.getACADFedexUpdates()  -- End");
		log.endTimer("AcademyACADFedexIntegrationRestAPIService.getACADFedexUpdates()");

		return outDocstrACADFedexResponse;
	}

	/**
	 * This method invoke the ACADFedex RestAPI and provide response back.
	 */
	private  Document invokeACADFedexRestAPIWebService(String strInput, String strURL) throws ParserConfigurationException {
		log.verbose("AcademyACADFedexIntegrationRestAPIService - invokeACADFedexRestAPIWebService - Start \n");
		Document docRestServiceResponse = null;
		String strResponseMessage = "";
		String readLine = null;
		String strACADFedexResponse = null;
		int inRresponseCode = 0;
		log.verbose("invokeACADFedexRestAPIWebService - strInput : " + strInput);
		log.verbose("invokeACADFedexRestAPIWebService - strURL : " + strURL);

		String strJSONInput = strInput;
		int iConnectTimeOut = Integer.parseInt(YFSSystem.getProperty(AcademyConstants.STR_ACADFedex_CONNECT_TIMEOUT));
		int iReadTimeOut = Integer.parseInt(YFSSystem.getProperty(AcademyConstants.STR_ACADFedex_READ_TIMEOUT));

		try {
			URL url = new URL(strURL);
			HttpsURLConnection httpCon = (HttpsURLConnection) url.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("POST");
			httpCon.setConnectTimeout(iConnectTimeOut);
			httpCon.setReadTimeout(iReadTimeOut);
			httpCon.setRequestProperty(AcademyConstants.WEBSERVICE_CONTENT_TYPE,
					AcademyConstants.ACCEPT_APPLICATION_JSON);

			log.verbose("Content type ---> " + AcademyConstants.ACCEPT_APPLICATION_JSON);
			log.verbose("URL being used --->" + httpCon.getURL().toString());
			log.verbose("Input used --->" + strJSONInput);
			log.verbose("ConnectTimeout ---> " + iConnectTimeOut);
			log.verbose("ReadTimeout ---> " + iReadTimeOut);

			DataOutputStream outDataStream = new DataOutputStream(httpCon.getOutputStream());
			outDataStream.writeBytes(strJSONInput);
			outDataStream.flush();
			outDataStream.close();

			inRresponseCode = httpCon.getResponseCode();
			strResponseMessage = httpCon.getResponseMessage();

			log.verbose("ResponseCode : " + inRresponseCode);
			log.verbose("ResponseMessage : " + strResponseMessage);

			BufferedReader in = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
			StringBuilder response = new StringBuilder();
			while ((readLine = in.readLine()) != null) {
				response.append(readLine);
			}
			in.close();
			log.verbose("JSON String Result : " + response.toString());
			strACADFedexResponse = response.toString();
			log.verbose("ACADFedex Response strJSONOutput  : \n" + strACADFedexResponse);
			if (inRresponseCode == Integer.parseInt(AcademyConstants.SUCCESS_CODE)) {
				log.verbose("AcademyACADFedexIntegrationRestAPIWebService.invokeACADFedexRestAPIWebService(): WS calling SUCCESS");
			} else {
				log.verbose(
						"AcademyACADFedexIntegrationRestAPIWebService.invokeACADFedexRestAPIWebService(): WS calling failure ---> Response code("
								+ httpCon.getResponseCode() + ")");
				log.verbose("AcademyACADFedexIntegrationRestAPIWebService.invokeACADFedexRestAPIWebService(): getResponseMessage : "
						+ httpCon.getResponseMessage());
			}

			// Formating JSON response to XML
			docRestServiceResponse = formatstrACADFedexResponse(strACADFedexResponse);

			log.verbose("ACADFedex Response JSON to XML  : \n" + XMLUtil.getXMLString(docRestServiceResponse));

		}
			catch (SocketTimeoutException e) {

				log.info("Exception occurred in AcademyACADFedexIntegrationRestAPIService :: SocketTimeoutException :: " + e.toString()
				+ "\n Exception Stack Trace ::\n" + Arrays.toString(e.getStackTrace())
				+ "\n Input to ACADFedex Webservice call:\n" + strInput);

				docRestServiceResponse = XMLUtil.createDocument(AcademyConstants.ELE_ERROR);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, AcademyConstants.STATUS_CODE_504);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_EXCEPTION_TYPE, AcademyConstants.SOCKET_TIME_OUT_EXCEPTION);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_ERROR_MESSAGE, e.getMessage());

			} catch (SSLHandshakeException e) {

				log.info("Exception occurred in AcademyACADFedexIntegrationRestAPIService:: SSLHandshakeException :: " + e.toString()
				+ " \n Exception Stack Trace :: \n" + Arrays.toString(e.getStackTrace())
				+ " \nInput to ACADFedex Webservice call: \n" + strInput);

				docRestServiceResponse = XMLUtil.createDocument(AcademyConstants.ELE_ERROR);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, AcademyConstants.STATUS_CODE_504);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_EXCEPTION_TYPE, AcademyConstants.SSL_HANDSHAKE_EXCEPTION);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_ERROR_MESSAGE, e.getMessage());

			} catch (Exception e) {
				log.info("Exception occurred in AcademyACADFedexIntegrationRestAPIService :: Generic Exception :: " + e.toString()
				+ "\nException Stack Trace :: \n" + Arrays.toString(e.getStackTrace())
				+ "\nInput to ACADFedex Webservice call: \n" + strInput);

				docRestServiceResponse = XMLUtil.createDocument(AcademyConstants.ELE_ERROR);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, AcademyConstants.STATUS_CODE_504);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_EXCEPTION_TYPE, AcademyConstants.STR_GENERIC_EXCEPTION);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_ERROR_MESSAGE, e.getMessage());
			}



			log.verbose("invokeACADFedexRestAPIWebService Response XML ::" + XMLUtil.getXMLString(docRestServiceResponse));
			log.verbose("AcademyACADFedexIntegrationRestAPIService - invokeACADFedexRestAPIWebService - End");

			return docRestServiceResponse;
		}

		/**
		 * preparing input to ACADFedex in JSON format
		 */

		private  String prepareJSONACADFedexInput(Document inDoc) throws Exception {
			log.verbose("AcademyACADFedexIntegrationRestAPIService.prepareJSONACADFedexInput() - Start");
			log.verbose("prepareJSONACADFedexInput - inDoc :: \n" + XMLUtil.getXMLString(inDoc));
			int iNoOfTrackingNos = 0;
			String strACADFedexJSONInput = null;
			StringBuilder strJsonInput = new StringBuilder(AcademyConstants.STR_TRACKING_NO_WITH_QUOTES);


			Element eleTrackingNoInOutput = inDoc.getDocumentElement();
			NodeList nListTrackingNos = XMLUtil.getNodeList(eleTrackingNoInOutput, AcademyConstants.ELEM_TRACKING_NO);

			if (!YFCObject.isVoid(nListTrackingNos)) {
				iNoOfTrackingNos = nListTrackingNos.getLength();
				log.verbose("iNoOfTrackingNos::" + iNoOfTrackingNos);

				for (int i = 0; i < iNoOfTrackingNos; i++) {
					Element eleCurrentTrackingNo = (Element)nListTrackingNos.item(i);
					if (!YFCObject.isVoid(eleCurrentTrackingNo)) {
						String trackingNo = XMLUtil.getAttributeFromXPath(inDoc, AcademyConstants.ATTR_TRACKING_NO);
						log.verbose("trackingNo :: \n" + trackingNo);
						String statusCode = XMLUtil.getAttributeFromXPath(inDoc, AcademyConstants.ATTR_STATUS_CODE);
						log.verbose("STATUS :: \n" + statusCode);				
						strJsonInput.append(trackingNo);
						strJsonInput.append(AcademyConstants.STR_STATUS_CODE_WITH_QUOTES);
						if (i < iNoOfTrackingNos - 1) {
							strJsonInput.append(AcademyConstants.STR_COMMA);
						}
					}
				}
			}
			strJsonInput.append(AcademyConstants.STR_CLOSE_BRACES);
			strACADFedexJSONInput = strJsonInput.toString();

			log.verbose("ACADFedex JSON Input :: \n" + strACADFedexJSONInput);
			log.verbose("AcademyACADFedexIntegrationRestAPIService.prepareJSONACADFedexInput() - End");

			return strACADFedexJSONInput;
		}

		//This method converts the ACADFedex response JSON to XML

		private  Document formatstrACADFedexResponse(String strJsonstrACADFedexResponse) throws IOException, ParserConfigurationException, SAXException {

			log.verbose("AcademyACADFedexIntegrationRestAPIService.formatstrACADFedexResponse() - Start");
			log.verbose("strJsonstrACADFedexResponse :: " + strJsonstrACADFedexResponse);
			Document outDocResponse = null;
			String strJSONOutput = AcademyConstants.STR_OPEN_BRACE_TRACKING_NO_WITH_QUOTES + strJsonstrACADFedexResponse
					+ AcademyConstants.STR_OPEN_BRACE;
			log.verbose("After appending root element - Response :: \n" + strJSONOutput);
			InputStream in = IOUtils.toInputStream(strJSONOutput);
			String xml = XML.toXml(in);
			log.verbose("JSON To XML xml :: \n" + xml);
			XMLUtil.getDocument(xml);
            log.verbose("formatstrACADFedexResponse outDocResponse :: \n" + XMLUtil.getXMLString(outDocResponse));
			log.verbose("AcademyACADFedexIntegrationRestAPIService.formatstrACADFedexResponse() - End");

			return outDocResponse;
		}	

	}
	//End OMNI-66078 CLONE - Add FedEx tracking status to STS queries
