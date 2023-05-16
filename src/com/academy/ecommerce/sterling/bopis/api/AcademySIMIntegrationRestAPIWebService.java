package com.academy.ecommerce.sterling.bopis.api;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Properties;
import org.w3c.dom.Document;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.yantra.yfc.core.YFCObject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.json.utils.XML;
import org.xml.sax.InputSource;

public class AcademySIMIntegrationRestAPIWebService {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademySIMIntegrationRestAPIWebService.class);
	private static Properties props;

	// Fetching Service level properties
	public void setProperties(Properties props) {
		this.props = props;
	}

	public static Document getSIMUpdates(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer("AcademySIMIntegrationRestAPIService.getSIMUpdates()");
		log.verbose("AcademySIMIntegrationRestAPIService.getSIMUpdates()  -- Start");

		Document outDocSIMResponse = null;
		String strSIMWSJSONInput = null;
		String strSIMURL = null;

		// Fetch the service level argument
		String strIsSIMRestAPIEnabled = props.getProperty(AcademyConstants.IS_SIM_REST_API_ENABLED);
		log.verbose("strIsSIMRestAPIEnabled ::" + strIsSIMRestAPIEnabled);

		// Fetching SIM interface URL from Properties(CustomerOverrieds)
		strSIMURL = YFSSystem.getProperty(AcademyConstants.STR_SIM_INTERFACE_URL);
		log.verbose("strSIMURL :: " + strSIMURL);

		if (!YFCObject.isVoid(strIsSIMRestAPIEnabled) && strIsSIMRestAPIEnabled.equalsIgnoreCase("Y")) {

			// Preparing JSON input for SIM RestAPI call
			strSIMWSJSONInput = prepareJSONSIMInput(env, inDoc);
			log.verbose("strJSonSIMInput :: " + strSIMWSJSONInput);

			// Invoking SIM RestAPI
			if (!YFCObject.isVoid(strSIMWSJSONInput) && !YFCObject.isVoid(strSIMURL)) {
				outDocSIMResponse = invokeSIMRestAPIWebService(strSIMWSJSONInput, strSIMURL);
				log.verbose("outDocSIMResponse ::\n " + XMLUtil.getXMLString(outDocSIMResponse));
			}
			// Element eleSIM = outDocSIMResponse.getDocumentElement();
			// Merging the response from SIM to input doc
			// XMLUtil.importElement(inDoc.getDocumentElement(), eleSIM);
			// log.verbose("Merging SIM response Document :: \n" +
			// XMLUtil.getXMLString(outDocSIMResponse));

		} else {
			// Handling when the SIM calls are prevented
			Element elerRootelement = inDoc.getDocumentElement();
			elerRootelement.setAttribute(AcademyConstants.IS_SIM_REST_API_ENABLED, strIsSIMRestAPIEnabled);
			outDocSIMResponse = inDoc;
			log.verbose("outDocSIMResponse :: \n" + XMLUtil.getXMLString(outDocSIMResponse));
		}

		log.verbose("Final output to Planogram Service :: \n" + XMLUtil.getXMLString(outDocSIMResponse));
		log.verbose("AcademySIMIntegrationRestAPIService.getSIMUpdates()  -- End");
		log.endTimer("AcademySIMIntegrationRestAPIService.getSIMUpdates()");

		return outDocSIMResponse;
	}

	/**
	 * This method invoke the SIM RestAPI and provide response back.
	 * 
	 * @param strInput
	 * @param strURL
	 * @return
	 * @throws Exception
	 */
	private static Document invokeSIMRestAPIWebService(String strInput, String strURL) throws Exception {
		log.verbose("AcademySIMIntegrationRestAPIService - invokeSIMRestAPIWebService - Start \n");
		Document docRestServiceResponse = null;
		String strResponseMessage = "";
		String readLine = null;
		String SIMResponse = null;
		int inRresponseCode = 0;
		// String strJSONOutput = null;
		// String strSIMRestServiceResponseCode = "";
		log.verbose("invokeSIMRestAPIWebService - strInput : " + strInput);
		log.verbose("invokeSIMRestAPIWebService - strURL : " + strURL);

		String strJSONInput = strInput.toString();

		int iConnectTimeOut = Integer.parseInt(YFSSystem.getProperty(AcademyConstants.STR_SIM_CONNECT_TIMEOUT));
		int iReadTimeOut = Integer.parseInt(YFSSystem.getProperty(AcademyConstants.STR_SIM_READ_TIMEOUT));

		try {
			URL url = new URL(strURL);
			HttpsURLConnection httpCon = (HttpsURLConnection) url.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("POST");
			httpCon.setConnectTimeout(iConnectTimeOut);
			httpCon.setReadTimeout(iReadTimeOut);
			httpCon.setRequestProperty(AcademyConstants.WEBSERVICE_CONTENT_TYPE,
					AcademyConstants.ACCEPT_APPLICATION_JSON);

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

				inRresponseCode = httpCon.getResponseCode();
				strResponseMessage = httpCon.getResponseMessage();

				log.verbose("ResponseCode : " + inRresponseCode);
				log.verbose("ResponseMessage : " + strResponseMessage);

				BufferedReader in = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
				StringBuffer response = new StringBuffer();
				while ((readLine = in.readLine()) != null) {
					response.append(readLine);
				}
				in.close();
				log.verbose("JSON String Result : " + response.toString());
				SIMResponse = response.toString();
				log.verbose("SIM Response strJSONOutput  : \n" + SIMResponse);
				if (inRresponseCode == Integer.parseInt(AcademyConstants.SUCCESS_CODE)) {
					log.verbose("AcademySIMIntegrationRestAPIWebService.invokeSIMRestAPIWebService(): WS calling SUCCESS");
				} else {
					log.verbose(
							"AcademySIMIntegrationRestAPIWebService.invokeSIMRestAPIWebService(): WS calling failure ---> Response code("
									+ httpCon.getResponseCode() + ")");
					log.verbose("AcademySIMIntegrationRestAPIWebService.invokeSIMRestAPIWebService(): getResponseMessage : "
							+ httpCon.getResponseMessage());
				}

				// Formating JSON response to XML
				docRestServiceResponse = formatSIMResponse(SIMResponse);

				log.verbose("SIM Response JSON to XML  : \n" + XMLUtil.getXMLString(docRestServiceResponse));
			}

			catch (SocketTimeoutException e) {

				log.info("Exception occurred in AcademySIMIntegrationRestAPIService :: SocketTimeoutException :: " + e.toString()
						+ "\nException Stack Trace :: \n" + e.getStackTrace().toString()
						+ "\nInput to SIM Webservice call: \n" + strInput);
				
				docRestServiceResponse = XMLUtil.createDocument(AcademyConstants.ELE_ERROR);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, AcademyConstants.STATUS_CODE_504);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_EXCEPTION_TYPE, AcademyConstants.SOCKET_TIME_OUT_EXCEPTION);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_ERROR_MESSAGE, e.getMessage());

			} catch (SSLHandshakeException e) {

				log.info("Exception occurred in AcademySIMIntegrationRestAPIService:: SSLHandshakeException :: " + e.toString()
						+ "\nException Stack Trace :: \n" + e.getStackTrace().toString()
						+ "\nInput to SIM Webservice call: \n" + strInput);

				docRestServiceResponse = XMLUtil.createDocument(AcademyConstants.ELE_ERROR);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, AcademyConstants.STATUS_CODE_504);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_EXCEPTION_TYPE, AcademyConstants.SSL_HANDSHAKE_EXCEPTION);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_ERROR_MESSAGE, e.getMessage());

			} catch (Exception e) {
				log.info("Exception occurred in AcademySIMIntegrationRestAPIService :: Generic Exception :: " + e.toString()
						+ "\nException Stack Trace :: \n" + e.getStackTrace().toString()
						+ "\nInput to SIM Webservice call: \n" + strInput);

				docRestServiceResponse = XMLUtil.createDocument(AcademyConstants.ELE_ERROR);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, AcademyConstants.STATUS_CODE_504);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_EXCEPTION_TYPE, AcademyConstants.STR_GENERIC_EXCEPTION);
				docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_ERROR_MESSAGE, e.getMessage());
			}
		} catch (Exception e) {
			log.info("Exception occurred in AcademySIMIntegrationRestAPIService before invoking web service call :: Exception::" + e.getMessage()
							+ " \n " + e.toString() + "\nException Stack Trace :: \n" + e.getStackTrace().toString()
							+ "\nInput to SIM Webservice call: \n" + strInput);
			docRestServiceResponse = XMLUtil.createDocument(AcademyConstants.ELE_ERROR);
			docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, AcademyConstants.STATUS_CODE_504);
			docRestServiceResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_ERROR_MESSAGE, e.getMessage());
		}

		log.verbose("invokeSIMRestAPIWebService Response XML ::" + XMLUtil.getXMLString(docRestServiceResponse));
		log.verbose("AcademySIMIntegrationRestAPIService - invokeSIMRestAPIWebService - End");

		return docRestServiceResponse;
	}

	/**
	 * preparing input to SIM in JSON format
	 * 
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	// {"storeId": "134", "itemIds": ["119380522"]}
	private static String prepareJSONSIMInput(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("AcademySIMIntegrationRestAPIService.prepareJSONSIMInput() - Start");
		log.verbose("prepareJSONSIMInput - inDoc :: \n" + XMLUtil.getXMLString(inDoc));
		int iNoOfOItemLines = 0;
		String strItemID = null;
		String strSIMJSONInput = null;
		StringBuffer strJsonInput = null;
		// String strItemList = null;

		String StoreId = XMLUtil.getAttributeFromXPath(inDoc, AcademyConstants.XPATH_STORE_ID);
		log.verbose("StoreId :: \n" + StoreId);
		Element eleItemsInOutput = inDoc.getDocumentElement();
		NodeList nListItems = XMLUtil.getNodeList(eleItemsInOutput, AcademyConstants.ELEM_ITEM);

		if (!YFCObject.isVoid(nListItems)) {
			iNoOfOItemLines = nListItems.getLength();
			System.out.println("iNoOfOItemLines::" + iNoOfOItemLines);
		}

		strJsonInput = new StringBuffer(AcademyConstants.STR_STORE_ID_WITH_QUOTES);
		strJsonInput.append(StoreId);
		strJsonInput.append(AcademyConstants.STR_ITEM_IDS_WITH_QUOTES);

		String[] strItemList = new String[iNoOfOItemLines];
		for (int i = 0; i < iNoOfOItemLines; i++) {
			Element eleCurrentItem = (Element) nListItems.item(i);
			if (!YFCObject.isVoid(eleCurrentItem)) {
				strItemID = XMLUtil.getString(eleCurrentItem, AcademyConstants.XPATH_ITEM);
				log.verbose("strItemID :: \n" + strItemID);
				strItemList[i] = strItemID;
				strJsonInput.append(AcademyConstants.STR_ESC_DOUBLE_QUOTE_CHAR);
				strJsonInput.append(strItemID);
				strJsonInput.append(AcademyConstants.STR_ESC_DOUBLE_QUOTE_CHAR);
				if (i < iNoOfOItemLines - 1) {
					strJsonInput.append(AcademyConstants.STR_COMMA);
				}
			}
		}
		strJsonInput.append(AcademyConstants.STR_CLOSE_BRACES);

		strSIMJSONInput = strJsonInput.toString();

		log.verbose("SIM JSON Input :: \n" + strSIMJSONInput);
		log.verbose("AcademySIMIntegrationRestAPIService.prepareJSONSIMInput() - End");

		return strSIMJSONInput;
	}

	/**
	 * This method converts the SIM response JSON to XML.
	 * 
	 * @param strJsonSIMResponse
	 * @return
	 * @throws Exception
	 */
	private static Document formatSIMResponse(String strJsonSIMResponse) throws Exception {

		log.verbose("AcademySIMIntegrationRestAPIService.formatSIMResponse() - Start");
		log.verbose("strJsonSIMResponse :: " + strJsonSIMResponse);

		Document outDocResponse = null;

		// String strResponse = strJsonSIMResponse.toString();
		String strJSONOutput = AcademyConstants.STR_OPEN_BRACE_ITEM_ID_WITH_QUOTES + strJsonSIMResponse
				+ AcademyConstants.STR_OPEN_BRACE;
		log.verbose("After appending root element - Response :: \n" + strJSONOutput);

		InputStream in = IOUtils.toInputStream(strJSONOutput);
		String xml = XML.toXml(in);
		log.verbose("JSON To XML xml :: \n" + xml);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();

		outDocResponse = builder.parse(new InputSource(new StringReader(xml)));

		log.verbose("formatSIMResponse outDocResponse :: \n" + XMLUtil.getXMLString(outDocResponse));
		log.verbose("AcademySIMIntegrationRestAPIService.formatSIMResponse() - End");

		return outDocResponse;
	}

	/**
	 * inDoc:: <Items StoreID="033"> <Item ItemID="010035624"/>
	 * <Item ItemID="010035625"/> </Items>
	 * 
	 * outDoc:: <jsonObject> <Item>
	 * <jsonObject availableStockOnHand="504" itemId="010035624" retailPrice="64.99"
	 * lastReceivedDate="07/20/2021" storeId="033"/> </Item> <Item>
	 * <jsonObject availableStockOnHand="504" itemId="010035625" retailPrice="64.99"
	 * lastReceivedDate="07/20/2021" storeId="033"/> </Item> </jsonObject>
	 **/
	/*
	 * public Document getSIMUpdatesTestData(YFSEnvironment env, Document indoc)
	 * throws Exception {
	 * 
	 * log.verbose("getSIMUpdatesTestData - indoc :: \n" +
	 * XMLUtil.getXMLString(indoc)); Document outputDoc = null;
	 * 
	 * outputDoc = XMLUtil.createDocument("jsonObject"); Element elejsonObject =
	 * outputDoc.getDocumentElement();
	 * 
	 * String strStoreID = XMLUtil.getAttributeFromXPath(indoc, "Items/@StoreID");
	 * log.verbose("strStoreID : " + strStoreID); NodeList nlItemList =
	 * indoc.getElementsByTagName("Item"); int iEleItemList =
	 * nlItemList.getLength(); log.verbose("NodeList iEleItemList Length is : " +
	 * iEleItemList);
	 * 
	 * if (iEleItemList > 0) { for (int i = 0; i < iEleItemList; i++) { Element
	 * eleItem = (Element) nlItemList.item(i); String strItemID =
	 * eleItem.getAttribute("ItemID"); log.verbose("ItemID : " + strItemID); Element
	 * itemElement = XMLUtil.createElement(outputDoc, "Item", true); Element
	 * itemjsonObject = XMLUtil.createElement(outputDoc, "jsonObject", true);
	 * 
	 * itemjsonObject.setAttribute("storeId", strStoreID);
	 * itemjsonObject.setAttribute("itemId", strItemID);
	 * itemjsonObject.setAttribute("availableStockOnHand", "504"); //
	 * itemjsonObject.setAttribute("lastReceivedDate", "07/20/2021");
	 * itemjsonObject.setAttribute("lastReceivedDate", "");
	 * itemjsonObject.setAttribute("retailPrice", "$64.99");
	 * 
	 * itemElement.appendChild(itemjsonObject);
	 * elejsonObject.appendChild(itemElement); } }
	 * log.verbose("getSIMUpdatesTestData - outputDoc :: \n" +
	 * XMLUtil.getXMLString(outputDoc));
	 * 
	 * return outputDoc; }
	 */

}