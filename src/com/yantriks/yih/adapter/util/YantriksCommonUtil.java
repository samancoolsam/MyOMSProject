package com.yantriks.yih.adapter.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.json.JSONArray;
import org.apache.commons.json.JSONException;
import org.apache.commons.json.JSONObject;
//import org.apache.http.client.utils.DateUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.ecommerce.yantriks.inventory.AcademyJWTTokenGenerator;
import com.academy.ecommerce.yantriks.inventory.stub.AcademyYantriksStub;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class YantriksCommonUtil {

	private static final YFCLogCategory log = YFCLogCategory.instance(YantriksCommonUtil.class);
	private static Properties resources = new Properties();
	private static YIFApi api;

	public static Document invokeAPI(YFSEnvironment env, String templateName, String apiName, Document inDoc) {
		env.setApiTemplate(apiName, templateName);
		Document returnDoc = invoke(env, apiName, inDoc);
		env.clearApiTemplate(apiName);

		return returnDoc;
	}

	public static Document invoke(YFSEnvironment env, String apiName, Document inDoc) {
		Document returnDoc = null;
		try {
			api = YIFClientFactory.getInstance().getApi();
			returnDoc = api.invoke(env, apiName, inDoc);
		} catch (RemoteException e) {
			log.error(e);
			throw new YFCException(e);
		} catch (YIFClientCreationException e) {
			log.error(e);
			throw new YFCException(e);
		}
		return returnDoc;
	}

	public static Document invokeFlow(YFSEnvironment env, String serviceName, Document inDoc) {
		Document returnDoc = null;
		try {
			api = YIFClientFactory.getInstance().getApi();
			returnDoc = api.executeFlow(env, serviceName, inDoc);
		} catch (RemoteException e) {
			log.error(e);
			throw new YFCException(e);
		} catch (YIFClientCreationException e) {
			log.error(e);
			throw new YFCException(e);
		}
		return returnDoc;
	}

	public static Document invokeService(YFSEnvironment env, String serviceName, Document inDoc)
			throws YFSException, RemoteException {
		return invokeFlow(env, serviceName, inDoc);
	}

	public static Document invokeAPI(YFSEnvironment env, String apiName, Document inDoc) {
		return invoke(env, apiName, inDoc);
	}

	public static Document invokeAPI(YFSEnvironment env, String apiName, String inDocStr) {
		Document returnDoc = null;
		try {
			returnDoc = invoke(env, apiName, YFCDocument.parse(inDocStr).getDocument());
		} catch (SAXException e) {
			log.error(e);
			throw new YFCException(e);
		} catch (IOException e) {
			log.error(e);
			throw new YFCException(e);
		}
		return returnDoc;
	}

	public static boolean isNetworkLevelReservationEnabled() {
		String enabled = YFSSystem.getProperty(YantriksConstants.PROP_NETWORK_RESERVATION);
		if ("Y".equalsIgnoreCase(enabled) || "true".equalsIgnoreCase(enabled)) {
			return true;
		}
		return false;
	}

	public static String getEnterpriseDG(YFSEnvironment oEnv, String primaryEnterprise) {
		YFCDocument inDoc = YFCDocument.createDocument("CommonCode");
		inDoc.getDocumentElement().setAttribute("CallingOrganizationCode", primaryEnterprise);
		inDoc.getDocumentElement().setAttribute("CodeType", "YIH_ENTERPRISE_DG");
		oEnv.setApiTemplate("getCommonCodeList", outputTemplateCommonCodeForDG);
		YFCDocument returnDoc = YFCDocument.getDocumentFor(invoke(oEnv, "getCommonCodeList", inDoc.getDocument()));
		oEnv.clearApiTemplate("getCommonCodeList");
		return returnDoc.getDocumentElement().getChildElement("CommonCode").getAttribute("CodeValue");
	}

	public static String outputTemplateCommonCodeForDG = "<CommonCodeList>\r\n"
			+ "<CommonCode CodeLongDescription=\"\" CodeShortDescription=\"\" CodeType=\"\" CodeValue=\"\" OrganizationCode=\"\"/>\r\n"
			+ "</CommonCodeList>";

	public static String getJsonAttribute(String jsonStr, String attr) {
		if (jsonStr.indexOf(attr) == -1) {
			return null;
		}
		jsonStr = jsonStr.substring(jsonStr.indexOf(attr) + attr.length());

		jsonStr = jsonStr.indexOf(",") > -1 ? jsonStr.substring(0, jsonStr.indexOf(","))
				: jsonStr.substring(0, jsonStr.indexOf("}"));
		return jsonStr.trim();
	}

	public static String get(String name) {
		String retVal = resources.getProperty(name);
		if (retVal != null) {
			retVal = retVal.trim();
		}
		return retVal;
	}

	public static String massageItemID(String itemID) {
		if (itemID.contains("-")) {
			itemID = itemID.replaceAll("-", "");
		}
		return itemID;
	}

	public static Document invokeAPI(YFSEnvironment env, Document template, String apiName, Document inDoc) {
		env.setApiTemplate(apiName, template);
		Document returnDoc = invoke(env, apiName, inDoc);
		env.clearApiTemplate(apiName);
		return returnDoc;
	}

	public static String convertToProductDate(String strOrderDate) {
		String strDate = null;
		try {
			if (!YFCObject.isVoid(strOrderDate)) {
				SimpleDateFormat oms_format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				SimpleDateFormat yih_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS z");

				Date orderDate = oms_format.parse(strOrderDate);
				strDate = yih_format.format(orderDate);
			}
		} catch (ParseException e) {
			log.error(e);
			throw new YFCException(e);
		}
		return strDate;
	}

	public static void invokeCreateAsyncReq(YFCDocument inDoc, YFSEnvironment env, String apiUrl) {
		log.beginTimer("invokeCreateAsyncReq");
		Document inputDoc = inDoc.getDocument();
		try {
			Document docIp = YIHXMLUtil.createDocument("CreateAsyncRequest");
			Element eleIp = docIp.getDocumentElement();
			Element eleApi = docIp.createElement("API");
			eleIp.appendChild(eleApi);
			eleApi.setAttribute("IsService", "Y");
			eleApi.setAttribute("Name", "YIH_Generic_Async_Service");
			Element eleInput = docIp.createElement("Input");
			eleApi.appendChild(eleInput);
			Element eleImpIp = (Element) docIp.importNode(inputDoc.getDocumentElement(), true);
			eleImpIp.setAttribute("FromAsyncReq", "Y");
			eleImpIp.setAttribute("URL", apiUrl);
			eleInput.appendChild(eleImpIp);
			if (log.isDebugEnabled())
				log.debug("Prepared input document for createAsyncReq api is:" + YIHXMLUtil.getElementXMLString(eleIp));
			invokeAPI(env, "createAsyncRequest", docIp);

		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
		log.endTimer("invokeCreateAsyncReq");
	}

	public static void createAsyncReqEntry(YFSEnvironment env, String apiUrl, String httpBody, String productToCall) {
		log.beginTimer("createAsyncReqEntry");
		try {
			Document docIp = SCXmlUtil.createDocument(YantriksConstants.ELE_CREATE_ASYNC_REQ);
			Element eleIp = docIp.getDocumentElement();
			Element eleApi = SCXmlUtil.createChild(eleIp, YantriksConstants.E_API);
			eleApi.setAttribute(YantriksConstants.A_IS_SERVICE, YantriksConstants.YES);
			eleApi.setAttribute(YantriksConstants.ATTR_NAME, YantriksConstants.SERV_YANTRIKS_ASYNC_REQ);
			Element eleInput = SCXmlUtil.createChild(eleApi, YantriksConstants.ELE_INPUT);
			Element eleInputInsideInp = SCXmlUtil.createChild(eleInput, YantriksConstants.ELE_INPUT);
			eleInputInsideInp.setTextContent(httpBody);
			// Setting up the URL which will be fetched for Yantriks Call
			eleInputInsideInp.setAttribute(YantriksConstants.A_URL, apiUrl);
			// Setting up this attribute so that it can be identified that it is from
			// adapter async req not implemented exactly as of now
			eleInputInsideInp.setAttribute(YantriksConstants.ATTR_FROM_ASYNC_REQ, YantriksConstants.STR_YES);
			// Attribute to find out which yantriks server/product to call
			eleInputInsideInp.setAttribute(YantriksConstants.ATTR_YANT_PRODUCT_TO_CALL, productToCall);
			if (log.isDebugEnabled())
				log.debug("Prepared input document for createAsyncReq api is:" + YIHXMLUtil.getElementXMLString(eleIp));
			invokeAPI(env, YantriksConstants.API_CREATE_ASYNC_REQ, docIp);

		} catch (Exception e) {
			throw new YFSException("Error while creating Async Entry :: " + e.getMessage());
		}
		log.endTimer("invokeCreateAsyncReq");
	}

	public static void createAsyncReqEntryForKafka(YFSEnvironment env, String topicName, Document inDoc) {
		// For Kafka we have different method
		log.beginTimer("createAsyncReqEntryForKafka");
		try {
			Document docIp = SCXmlUtil.createDocument(YantriksConstants.ELE_CREATE_ASYNC_REQ);
			Element eleIp = docIp.getDocumentElement();
			Element eleApi = SCXmlUtil.createChild(eleIp, YantriksConstants.E_API);
			eleApi.setAttribute(YantriksConstants.A_IS_SERVICE, YantriksConstants.YES);
			eleApi.setAttribute(YantriksConstants.ATTR_NAME, YantriksConstants.SERV_YANTRIKS_KAFKA_ASYNC_REQ);
			Element eleInput = SCXmlUtil.createChild(eleApi, YantriksConstants.ELE_INPUT);
			Element eleInputInsideInp = SCXmlUtil.createChild(eleInput, YantriksConstants.ELE_INPUT);
			eleInputInsideInp.setTextContent(SCXmlUtil.getString(inDoc));
			// For Topic identification
			eleInputInsideInp.setAttribute(YantriksConstants.A_TOPIC, topicName);
			// For Request coming from Adapter
			eleInputInsideInp.setAttribute(YantriksConstants.ATTR_FROM_ASYNC_REQ, YantriksConstants.STR_YES);
			if (log.isDebugEnabled())
				log.debug("Prepared input document for createAsyncReq api is:" + YIHXMLUtil.getElementXMLString(eleIp));
			invokeAPI(env, YantriksConstants.API_CREATE_ASYNC_REQ, docIp);

		} catch (Exception e) {
			throw new YFSException("Error while creating Async Entry :: " + e.getMessage());
		}
		log.endTimer("createAsyncReqEntryForKafka");
	}

	public static void createAsyncReqEntryForPOkafka(YFSEnvironment env, String topicName, String jsonString) {
		// For Kafka we have different method
		log.beginTimer("createAsyncReqEntryForKafka");
		try {
			Document docIp = SCXmlUtil.createDocument(YantriksConstants.ELE_CREATE_ASYNC_REQ);
			Element eleIp = docIp.getDocumentElement();
			Element eleApi = SCXmlUtil.createChild(eleIp, YantriksConstants.E_API);
			eleApi.setAttribute(YantriksConstants.A_IS_SERVICE, YantriksConstants.YES);
			eleApi.setAttribute(YantriksConstants.ATTR_NAME, YantriksConstants.SERV_YANTRIKS_KAFKA_ASYNC_REQ);
			Element eleInput = SCXmlUtil.createChild(eleApi, YantriksConstants.ELE_INPUT);
			Element eleInputInsideInp = SCXmlUtil.createChild(eleInput, YantriksConstants.ELE_INPUT);
			eleInputInsideInp.setTextContent(jsonString);
			// For Topic identification
			eleInputInsideInp.setAttribute(YantriksConstants.A_TOPIC, topicName);
			// For Request coming from Adapter
			eleInputInsideInp.setAttribute(YantriksConstants.ATTR_FROM_ASYNC_REQ, YantriksConstants.STR_YES);
			if (log.isDebugEnabled())
				log.debug("Prepared input document for createAsyncReq api is:" + YIHXMLUtil.getElementXMLString(eleIp));
			invokeAPI(env, YantriksConstants.API_CREATE_ASYNC_REQ, docIp);

		} catch (Exception e) {
			throw new YFSException("Error while creating Async Entry :: " + e.getMessage());
		}
		log.endTimer("createAsyncReqEntryForKafka");
	}

	public static String callYantriksGetOrDeleteAPI(String apiUrl, String httpMethod, String productToCall) {
		log.beginTimer("callYantriksGetOrDeleteAPI");
		log.debug("API URL for Get or Delete :: " + apiUrl);
		log.debug("Http Method :: " + apiUrl);
		if ((YFCCommon.isVoid(httpMethod)) || (YFCCommon.isVoid(apiUrl))) {
			if (log.isDebugEnabled())
				log.debug("Mandatory parameters are missing");
			if (log.isDebugEnabled()) {
				log.debug("httpMethod:: " + httpMethod + "apiUrl:: " + apiUrl);
			}
			return "";
		}

		String outputStr = "";
		try {
			String protocol = YFSSystem
					.getProperty(YantriksConstants.YANTRIKSDOT + productToCall + YantriksConstants.DOTPROTOCOL);
			String host = YFSSystem
					.getProperty(YantriksConstants.YANTRIKSDOT + productToCall + YantriksConstants.DOTHOSTNAME);
			String port = YFSSystem
					.getProperty(YantriksConstants.YANTRIKSDOT + productToCall + YantriksConstants.DOTPORT);
			String timeout = YFSSystem
					.getProperty(YantriksConstants.YANTRIKSDOT + productToCall + YantriksConstants.DOTTIMEOUT);

			/*
			 * String protocol = "http"; String host ="localhost"; String port ="8080";
			 * String timeout ="1000";
			 */

			URL url = null;
			if (!YFCCommon.isVoid(port)) {
				url = new URL(protocol + "://" + host + ":" + port + apiUrl);
			} else {
				url = new URL(protocol + "://" + host + apiUrl);
			}

			if (log.isDebugEnabled())
				log.debug("URL is:" + url.toString());

			long startTime = System.currentTimeMillis();

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod(httpMethod);

			if (!YFCCommon.isVoid(timeout)) {
				conn.setConnectTimeout(Integer.parseInt(timeout));
			}
			long endTime = System.currentTimeMillis();
			if (log.isDebugEnabled()) {
				log.debug("Output from Server ...." + conn.toString());
			}
			log.debug("Response Code Received :: " + conn.getResponseCode());
			if (conn.getResponseCode() != 200) {
				return YantriksConstants.V_FAILURE;
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			String outputLine = null;
			while ((outputLine = br.readLine()) != null) {
				outputStr = outputStr.concat(outputLine);
			}
			if (log.isDebugEnabled()) {
				log.debug("Output from Server ....");
				log.debug(outputStr);
			}
			conn.disconnect();
		} catch (Exception e) {
			log.error("Error : " + e.getMessage() + " URL: " + apiUrl + " for Method :: " + httpMethod);
			throw new YFSException("Exception is thrown from yantriks API :: " + e.getMessage());
		} finally {
			log.endTimer("callYantriksGetOrDeleteAPI");
		}
		return outputStr;
	}

	public static String getReservationID(Element ele) {
		Element childEle = SCXmlUtil.getChildElement(ele, YantriksConstants.ELE_EXTN);
		if (!YFCObject.isVoid(childEle)) {
			if (!YFCObject.isVoid(childEle.getAttribute(YantriksConstants.EXTN_RESERVATION_ID))) {
				return childEle.getAttribute(YantriksConstants.EXTN_RESERVATION_ID);
			} else {
				return ele.getAttribute(YantriksConstants.A_ORDER_NO);
			}
		} else {
			return ele.getAttribute(YantriksConstants.A_ORDER_NO);
		}
	}

	public static boolean isStoreEnabled() {
		log.beginTimer("isStoreEnabled");
		String strIsYIHSFSStoresEnabled = YFSSystem.getProperty(YantriksConstants.PROP_SFS_STORES_ENABLED);
		if (log.isDebugEnabled())
			log.debug(" Is SFS enabled " + strIsYIHSFSStoresEnabled);
		if ("true".equalsIgnoreCase(strIsYIHSFSStoresEnabled)) {
			if (log.isDebugEnabled())
				log.debug(" Return True ");
			return true;
		}
		if (log.isDebugEnabled())
			log.debug(" Return False ");

		log.endTimer("isStoreEnabled");
		return false;
	}

	public static void modifyExistingReservationExpityTime(String orderNo, String orgId, int expirationTime,
			String expirationTimeUnit) {
		/// availability-services/reservations/v3.0/{orgId}/{orderId}/{extendedExpiryTime}/{extendedExpiryTimeUnit}
		StringBuilder updateReserveURL = new StringBuilder();
		updateReserveURL.append(YantriksConstants.API_URL_GET_RESERVE_DETAILS);
		updateReserveURL.append("/");
		updateReserveURL.append(orgId);
		updateReserveURL.append("/");
		updateReserveURL.append(orderNo);
		updateReserveURL.append("/");
		updateReserveURL.append(expirationTime);
		updateReserveURL.append("/");
		updateReserveURL.append(expirationTimeUnit);

		String updatereservationDetails = null;
		try {
			updatereservationDetails = YantriksCommonUtil.callYantriksGetOrDeleteAPI(updateReserveURL.toString(),
					YantriksConstants.HTTP_METHOD_PUT, YantriksCommonUtil.getAvailabilityProduct());
			log.debug("Output of GET/DELETE/MODIFY Yantriks api call :: " + updatereservationDetails);
		} catch (Exception e) {
			throw new YFSException("Did not update the reservation details for SHIP lines :: " + e.getMessage());
		}

	}

	public static String createOrModifyReservationBasedOnAction(YFSEnvironment env, String requestJson,
			String transactionType, boolean entryToBeCreatedOnTimeout, boolean entryToBeCreatedOnFailure, String action,
			String sellingChannel, boolean canReserveAfter, boolean considerCapacity, boolean considerGtin,
			boolean ignoreAvailabilityCheck) {
		log.debug("createOrModifyReservationBasedOnAction :: Input formed for Reservation Modification/Creation :: "
				+ requestJson);
		StringBuilder sb = new StringBuilder();
		sb.append(YantriksConstants.API_URL_RESERVATION);
		sb.append("/");
		sb.append(sellingChannel);
		sb.append("/");
		sb.append(transactionType);
		sb.append("?");
		sb.append(YantriksConstants.CAN_RESERVE_AFTER);
		sb.append("=");
		sb.append(canReserveAfter);
		sb.append("&");
		sb.append(YantriksConstants.CONSIDER_CAPACITY);
		sb.append("=");
		if (YantriksConstants.V_ACTION_MODIFY.equals(action)) {
			// If we are getting the modification request for an existing reservation than
			// we will consider capacity as false
			sb.append(false);
		} else {
			sb.append(YFSSystem.getProperty(YantriksConstants.PROP_CONSIDER_CAPACITY)); // customer overrides based
		}
		sb.append("&");
		sb.append(YantriksConstants.CONSIDER_GTIN);
		sb.append("=");
		sb.append(considerGtin);
		sb.append("&");
		sb.append(YantriksConstants.IGNORE_AVAILABILITY_CHECK);
		sb.append("=");
		sb.append(ignoreAvailabilityCheck);

		log.debug("URL formed for Reservation :: " + sb.toString());

		String httpMethod = null;
		String output = null;
		int retryAttempts = YantriksCommonUtil.getRetryAttempts();
		int retryCounter = 0;
		if (YantriksConstants.V_ACTION_CREATE.equals(action)) {
			httpMethod = YantriksConstants.YIH_HTTP_METHOD_POST;
		} else {
			httpMethod = YantriksConstants.HTTP_METHOD_PUT;
		}
		try {
			while (retryCounter < retryAttempts) {
				if (YantriksConstants.YIH_HTTP_METHOD_POST.equals(httpMethod)
						|| YantriksConstants.HTTP_METHOD_PUT.equals(httpMethod)) {
					output = YantriksCommonUtil.callYantriksAPI(sb.toString(), httpMethod, requestJson,
							YantriksCommonUtil.getAvailabilityProduct(),env);
				}
				if (output.equals(YantriksConstants.V_FAILURE)) {
					retryCounter++;
				} else {
					break;
				}
			}

			if (YantriksConstants.V_FAILURE.equals(output)) {
				log.debug("Reservation Failed due to some error code Hence Creating the Entry in Async Table");
				if (entryToBeCreatedOnFailure) {
					YantriksCommonUtil.createAsyncReqEntry(env, sb.toString(), requestJson,
							YantriksCommonUtil.getAvailabilityProduct());
				} else {
					log.error(
							"Servers are available but Reservation Failed in Yantriks hence logging the message in ELK :: "
									+ requestJson);
				}
			} else {
				log.debug("Reservation in YAS Success");
			}
		} catch (Exception e) {
			log.debug("One of the expected exception is Connection exception");
			if (entryToBeCreatedOnTimeout) {
				// If passed true will create the entry in createAsyncRequest table
				YantriksCommonUtil.createAsyncReqEntry(env, sb.toString(), requestJson,
						YantriksCommonUtil.getAvailabilityProduct());
				// Logging it as well so that it will go to ELK
				log.error("YantriksServers are down hence entry created and reservation failed :: Message "
						+ requestJson);
			} else {
				log.error("Reservation Failed due to Server Unavailability");
				throw new YFSException(
						"Reservation failed and async request not to be created on yantriks product unavailability"
								+ e.getMessage());
			}
		}
		return output;
	}

	public static void cancelExistingReservation(YFSEnvironment env, Document inDoc, String orderId, String cartId,
			boolean restoreCapacity) {
		String baseReserveUrl = YantriksConstants.API_URL_RESERVATION;
		String orgId = YantriksCommonUtil.getOrgId(); // Can be replaced with specific customer logic so inDoc has been
														// passed
		StringBuilder sb = new StringBuilder();
		sb.append(baseReserveUrl);
		sb.append("/");
		sb.append(orgId);
		sb.append("/");
		if (!YFCObject.isVoid(cartId)) {
			sb.append(cartId);
		} else {
			sb.append(orderId);
		}
		sb.append("?");
		sb.append(YantriksConstants.QUERY_PARAM_RESTORE_CAPACITY);
		sb.append("=");
		sb.append(restoreCapacity);
		log.debug("URL formed :: " + sb.toString());

		String output = null;
		// If retryAttempts are needed custom then it can be hardcoded here
		int retryAttempts = YantriksCommonUtil.getRetryAttempts();
		int retryCounter = 0;
		try {
			while (retryCounter < retryAttempts) {
				output = callYantriksGetOrDeleteAPI(sb.toString(), YantriksConstants.HTTP_METHOD_DELETE,
						YantriksCommonUtil.getAvailabilityProduct());
				if (output.equals(YantriksConstants.V_FAILURE)) {
					retryCounter++;
				} else {
					break;
				}
			}

			if (YantriksConstants.V_FAILURE.equals(output)) {
				log.error("Reservation Deletion Failed and its a delete call hence not storing it in Async");
				YantriksCommonUtil.createAsyncReqEntry(env, sb.toString(), YantriksConstants.STR_BLANK,
						YantriksCommonUtil.getAvailabilityProduct());
			} else {
				log.debug("Reservation Cancellation in YAS Success");
			}
		} catch (Exception e) {
			log.error("Exception Caught while calling YantriksGetOrDeleteAPI");
			throw new YFSException(
					"Yantriks Server are down hence reservation could not get cancelled" + e.getMessage());
		}
	}

	public static String deleteLineIdReservationOrder(YFSEnvironment env, String orderId, String orgId, String lineId,
			boolean restoreCapacity, boolean isEntryToBeCreatedOnFailure, boolean isEntryToBeCreatedOnException) {
		String baseReserveUrl = YantriksConstants.API_URL_RESERVATION;
		StringBuilder sb = new StringBuilder();
		sb.append(baseReserveUrl);
		sb.append("/");
		sb.append(YantriksConstants.QP_LINES);
		sb.append("/");
		sb.append(orgId);
		sb.append("/");
		sb.append(orderId);
		sb.append("/");
		sb.append(lineId);
		sb.append("?");
		sb.append(YantriksConstants.QUERY_PARAM_RESTORE_CAPACITY);
		sb.append("=");
		sb.append(restoreCapacity);

		log.debug("URL formed :: " + sb.toString());

		String output = null;
		// If retryAttempts are needed custom then it can be hardcoded here
		int retryAttempts = YantriksCommonUtil.getRetryAttempts();
		int retryCounter = 0;
		try {
			while (retryCounter < retryAttempts) {
				output = callYantriksGetOrDeleteAPI(sb.toString(), YantriksConstants.HTTP_METHOD_DELETE,
						YantriksCommonUtil.getAvailabilityProduct());
				if (output.equals(YantriksConstants.V_FAILURE)) {
					retryCounter++;
				} else {
					break;
				}
			}

			if (YantriksConstants.V_FAILURE.equals(output)) {
				// throw new YFSException("Deletion of line failed in Yantriks YAS");
				if (isEntryToBeCreatedOnFailure) {
					YantriksCommonUtil.createAsyncReqEntry(env, sb.toString(), YantriksConstants.STR_BLANK,
							YantriksCommonUtil.getAvailabilityProduct());
				} else {
					log.error("Reservation Deletion Failed and its a delete call hence not storing it in Async");
				}
			} else {
				log.debug("Reservation Cancellation in YAS Success");
			}
		} catch (Exception e) {
			if (isEntryToBeCreatedOnException) {
				log.error("Exception Caught while calling YantriksGetOrDeleteAPI");
				YantriksCommonUtil.createAsyncReqEntry(env, sb.toString(), YantriksConstants.STR_BLANK,
						YantriksCommonUtil.getAvailabilityProduct());
			} else {
				throw new YFSException(
						"Yantriks Server are down hence reservation could not get cancelled" + e.getMessage());
			}
		}
		return output;
	}

	public static String fetchExistingReservation(String orgId, String orderNo) {
		// Forming the URL for get Reservation Details call
		StringBuilder reserveURL = new StringBuilder();
		reserveURL.append(YantriksConstants.API_URL_GET_RESERVE_DETAILS);
		reserveURL.append("/");
		reserveURL.append(orgId);
		reserveURL.append("/");
		reserveURL.append(orderNo);

		String reservationDetails = null;
		try {
			reservationDetails = YantriksCommonUtil.callYantriksGetOrDeleteAPI(reserveURL.toString(),
					YantriksConstants.HTTP_METHOD_GET, YantriksCommonUtil.getAvailabilityProduct());

		} catch (Exception e) {
			throw new YFSException("Did not received the reservation details for SHIP lines :: " + e.getMessage());
		}
		return reservationDetails;
	}

	public static String fetchExistingLineReservation(String orgId, String reservationId, String lineId) {
		// Forming the URL for get Reservation Details at line level
		StringBuilder sb = new StringBuilder();
		sb.append(YantriksConstants.API_URL_RESERVATION);
		sb.append("/");
		sb.append(YantriksConstants.QP_LINES);
		sb.append("/");
		sb.append(orgId);
		sb.append("/");
		sb.append(reservationId);
		sb.append("/");
		sb.append(lineId);

		String reservationDetails = null;
		try {
			reservationDetails = YantriksCommonUtil.callYantriksGetOrDeleteAPI(sb.toString(),
					YantriksConstants.HTTP_METHOD_GET, YantriksCommonUtil.getAvailabilityProduct());

		} catch (Exception e) {
			log.error("Did not find reservation for reservation id :: " + reservationId);
			YFSException f = new YFSException();
			f.setErrorDescription("Servers are down");
			throw f;
		}
		return reservationDetails;
	}

	public static String modifyExistingLineReservation(YFSEnvironment env, String httpBody, String sellingChannel,
			String transactionType, boolean canReserveAfter, boolean considerCapacity, boolean considerGtin,
			boolean force, boolean ignoreAvailabilityCheck, boolean isEntryToBeCreatedOnFailure,
			boolean isEntryToBeCreatedOnException) {
		// Forming the URL for Post update at line level
		StringBuilder sb = new StringBuilder();
		sb.append(YantriksConstants.API_URL_RESERVATION);
		sb.append("/");
		sb.append(YantriksConstants.QP_LINES);
		sb.append("/");
		sb.append(sellingChannel);
		sb.append("/");
		sb.append(transactionType);
		sb.append("?");
		sb.append(YantriksConstants.CAN_RESERVE_AFTER);
		sb.append("=");
		sb.append(canReserveAfter);
		sb.append("&");
		sb.append(YantriksConstants.CONSIDER_CAPACITY);
		sb.append("=");
		sb.append(considerCapacity);
		sb.append("&");
		sb.append(YantriksConstants.CONSIDER_GTIN);
		sb.append("=");
		sb.append(considerGtin);
		sb.append("&");
		sb.append(YantriksConstants.FORCE);
		sb.append("=");
		sb.append(force);
		sb.append("&");
		sb.append(YantriksConstants.IGNORE_AVAILABILITY_CHECK);
		sb.append("=");
		sb.append(ignoreAvailabilityCheck);

		log.debug("API URL getting formed for PUT Call :: " + sb.toString());
		log.debug("Input Coming for Reservation Line Update :: " + httpBody);

		String output = null;
		// If retryAttempts are needed custom then it can be hardcoded here
		int retryAttempts = YantriksCommonUtil.getRetryAttempts();
		int retryCounter = 0;
		try {
			while (retryCounter < retryAttempts) {
				output = callYantriksAPI(sb.toString(), YantriksConstants.HTTP_METHOD_PUT, httpBody,
						YantriksCommonUtil.getAvailabilityProduct(),env);
				if (output.equals(YantriksConstants.V_FAILURE)) {
					retryCounter++;
				} else {
					break;
				}
			}

			if (YantriksConstants.V_FAILURE.equals(output)) {

				if (isEntryToBeCreatedOnFailure) {
					log.debug("Reservation Update Failed ");
					YantriksCommonUtil.createAsyncReqEntry(env, sb.toString(), httpBody,
							YantriksCommonUtil.getAvailabilityProduct());
				} else {
					log.error(
							"Servers are available but Reservation Update Failed in Yantriks hence logging the message in ELK :: "
									+ httpBody);
				}
			} else {
				log.debug("Reservation Update in YAS Success");
			}
		} catch (Exception e) {
			if (isEntryToBeCreatedOnException) {
				log.error("Reservation Update Failed due to timeout :: " + e.getMessage());
				YantriksCommonUtil.createAsyncReqEntry(env, sb.toString(), httpBody,
						YantriksCommonUtil.getAvailabilityProduct());
			} else {
				throw new YFSException("Reservation Update Failed due to timeout :: " + e.getMessage());
			}
		}
		return output;
	}

	public static String getCurrentDateOrTimeStamp(SimpleDateFormat formatter) {
		String timeZone = "UTC";
		if (timeZone.equals("")) {
			timeZone = "UTC"; // Setting UTC as default
		}
		formatter.setTimeZone(TimeZone.getTimeZone(timeZone));
		Date date = new Date();
		return formatter.format(date);
	}

	public static String getTransactionType() {
		// For now hardcoding as PDP , PLP may also be an option but not clear as of now
		// hoe to retrieve
		return YFSSystem.getProperty(YantriksConstants.PROP_TRANSACTION_TYPE);
	}

	public static String getSegment() {
		return "DEFAULT";
	}

	public static String getSellingChannel() {
		// As retrieval of it yet to be finalised hence hardcoding it for now
		return YFSSystem.getProperty(YantriksConstants.PROP_SELLING_CHANNEL);
	}

	public static int getRetryAttempts() {
		String retryAttempts = YFSSystem.getProperty(YantriksConstants.PROP_RETRY_ATTEMPTS);
		if (YFCObject.isVoid(retryAttempts)) {
			return 3;
		}
		return Integer.parseInt(retryAttempts);
	}

	public static boolean getConsiderCapParam() {
		String considerCapacity = YFSSystem.getProperty(YantriksConstants.PROP_CONSIDER_CAPACITY_ENABLED);
		if (YFCObject.isVoid(considerCapacity)) {
			return false;
		}
		return Boolean.valueOf(considerCapacity);
	}

	public static boolean getConsiderGtinParam() {
		String considerGtin = YFSSystem.getProperty(YantriksConstants.PROP_CONSIDER_GTIN_ENABLED);
		if (YFCObject.isVoid(considerGtin)) {
			return false;
		}
		return Boolean.valueOf(considerGtin);
	}

	public static String getOrgId() {
		return "ACADEMY";// URBN
	}

	public static String getFulfillmentService() {
		return "STANDARD";
	}

	public static int getReservationExpirationTime() {
		String expirationTime = YFSSystem.getProperty(YantriksConstants.PROP_EXPIRATION_TIME);
		if (YFCObject.isVoid(expirationTime)) {
			return 0;
		}
		return Integer.parseInt(expirationTime);
	}

	public static int getModifyRequestReservationExpirationTime() {
		String expirationTime = YFSSystem.getProperty(YantriksConstants.PROP_MODIFY_REQUEST_EXPIRATION_TIME);
		if (YFCObject.isVoid(expirationTime)) {
			return 0;
		}
		return Integer.parseInt(expirationTime);
	}

	public static String getModifyRequestExpirationTimeUnit() {
		String expiryTimeUnit = YFSSystem.getProperty(YantriksConstants.PROP_MODIFY_REQUEST_EXPIRY_TIME_UNIT);
		if (YFCObject.isVoid(expiryTimeUnit)) {
			return "SECONDS";
		}
		return expiryTimeUnit;
	}

	public static String getExpirationTimeUnit() {
		String expiryTimeUnit = "SECONDS";
		if (YFCObject.isVoid(expiryTimeUnit)) {
			return "SECONDS";
		}
		return expiryTimeUnit;
	}

	public static String getMasterDataProduct() {
		if (YFCObject.isVoid(YFSSystem.getProperty(YantriksConstants.PROP_MASTER_UPLOAD_PRODUCT))) {
			return YantriksConstants.PRODUCT_YCS;
		}
		return YFSSystem.getProperty(YantriksConstants.PROP_MASTER_UPLOAD_PRODUCT);
	}

	public static String getSupplyProduct() {
		if (YFCObject.isVoid(YFSSystem.getProperty(YantriksConstants.PROP_SUPPLY_DATA_PRODUCT))) {
			return YantriksConstants.PRODUCT_ILT;
		}
		return YFSSystem.getProperty(YantriksConstants.PROP_SUPPLY_DATA_PRODUCT);
	}

	public static String getAvailabilityProduct() {
		if (YFCObject.isVoid(YFSSystem.getProperty(YantriksConstants.PROP_AVAILABILITY_PRODUCT))) {
			return YantriksConstants.PRODUCT_YAS;
		}
		return YFSSystem.getProperty(YantriksConstants.PROP_AVAILABILITY_PRODUCT);
	}

	public static boolean isPublishToKafkaEnabled() {
		if (YFCObject.isVoid(YFSSystem.getProperty(YantriksConstants.PROP_ENABLE_PUBLISH_TO_KAFKA))) {
			return false;
		}
		return Boolean.valueOf(YFSSystem.getProperty(YantriksConstants.PROP_ENABLE_PUBLISH_TO_KAFKA));
	}

	public static boolean isPublishToPOKafkaEnabled() {
		if (YFCObject.isVoid(YFSSystem.getProperty(YantriksConstants.PROP_ENABLE_PO_PUBLISH_TO_KAFKA))) {
			return false;
		}
		return Boolean.valueOf(YFSSystem.getProperty(YantriksConstants.PROP_ENABLE_PO_PUBLISH_TO_KAFKA));
	}

	public static JSONArray getLocationTypeArray() {
		JSONArray locationTypeJsonArray = new JSONArray();
		String locationTypes = YFSSystem.getProperty(YantriksConstants.PROP_VALID_LOCATION_TYPES);
		// String locationTypes = "DC,AggNode,BackOffice,CC,MSN,Store,Vendor";
		String[] locationTypesArray = locationTypes.split(",");
		for (String locStrings : locationTypesArray) {
			locationTypeJsonArray.add(locStrings);
		}
		return locationTypeJsonArray;
	}

	// STER-4905 START
	public static String getEquivalentSupplyTypeForYantriks(String incomingSupplyType) {
		if (YantriksConstants.IM_LIST_ONHAND_MAPPED_SUPPLY_TYPES.contains(incomingSupplyType)) {
			log.debug("Supply Type to be mapped to ONHAND hence returning ONHAND");
			return YantriksConstants.V_C_ONHAND;
		}
		return incomingSupplyType;
	}
	// STER-4905 END

	public static boolean isNewBackOrderImplementationEnabled() {
		String newBoImplEnabled = YFSSystem.getProperty(YantriksConstants.PROP_NEWBOIMPL_ENABLE);
		log.debug("New BackOrder Impl Enable Property : " + newBoImplEnabled);
		if (YantriksConstants.CONST_FALSE.equals(newBoImplEnabled)
				|| YantriksConstants.STR_NO.equals(newBoImplEnabled)) {
			return false;
		}
		return true;
	}

	public static String deriveDemandTypeFromStatus(YFSEnvironment env,String status,String shipNode,String procureFromNode,String orderLineFulfillmentType,String sLineShipStatus) {// Modified the method arguments to include ShipNode, procureFromNode and orderLineFulfillmentType as part of OMNI-40029 
		String invokedOnChangeOrderOnCancel=(String) env.getTxnObject(YantriksConstants.INVOKED_ON_CHANGEORDER_CANCEL);
		log.verbose("Checking Demand Type");
		log.verbose("invokedOnChangeOrderOnCancel:"+invokedOnChangeOrderOnCancel);
		
		// Get the list of statuses which need to be ignored. This has been added lately without modifying the existing logic.
		Set<String> setOfIgnoredStatuses=getIgnoredStatuses(env);
		
		/*OMNI-40029: Start Change - Invoke method to fetch the node type */
		String nodeType="";
		if (!YFCObject.isVoid(shipNode)) 
		nodeType=getShipNodeType(env,shipNode);
		log.verbose("nodeType:"+nodeType+"procureFromNode:"+procureFromNode);
		/*OMNI-40029: End Change*/
		
		/*OMNI-34706 : BEGIN 
		 * We are ignoring the below status because for SFS, when order line moves to Ready To Ship status,OMS sends an update to 
		 * Yantriks as Shipped. So whenever an order line is in 3350.300 status, blank demand update will be sent to Yantriks. 
		 * */
		if (nodeType.equals(YantriksConstants.AT_LT_STORE) && status.equals(YantriksConstants.V_STATUS_3350_300)) {
			log.verbose("Ignore for SFS Ready To Ship");
			return YantriksConstants.DT_DEFAULT_IGNORE;
		}/*OMNI-34706 : END */
		else if (status.compareTo(YantriksConstants.V_STATUS_1100) >= 0 && status.compareTo(YantriksConstants.V_STATUS_1300) < 0) {
			// Changed from OPEN TO RESERVED
//          return YantriksConstants.DT_OPEN_ORDER;
			log.verbose("Return Reserver Order");
			return YantriksConstants.DT_RSRV_ORDER;

		}
		/*OMNI-40029: Start Change - Added for handling ALLOCATED demand update on SFDC shipment*/ 
		else if (nodeType.equals(YantriksConstants.AT_LT_SHARED_INVENTORY_DC) && (status.compareTo(YantriksConstants.V_STATUS_3350) >= 0 && status.compareTo(YantriksConstants.V_STATUS_3700) < 0)) {
			log.verbose("Return ALLOCATED for SFDC");
			return YantriksConstants.DT_ALLOCATED;
		}
		//OMNI-51881 - Start changes - Adding for Handling SCHEDULED_TO as demand update on sts 2.0 shipment
		/*else if(!YFCCommon.isVoid(sLineShipStatus)&& sLineShipStatus.equalsIgnoreCase("1100.70.06.30")
				&& nodeType.equals(YantriksConstants.AT_LT_STORE) && !YFCObject.isVoid(procureFromNode) && YantriksConstants.V_STATUS_2160_00_01.equals(status)) {*/
		//Changes for OMNI-63470
		else if(!YFCCommon.isVoid(sLineShipStatus)&& sLineShipStatus.equalsIgnoreCase("1100.70.06.30")
				&& nodeType.equals(YantriksConstants.AT_LT_STORE) && !YFCObject.isVoid(procureFromNode)  && !StringUtil.isEmpty(status) && status.startsWith(YantriksConstants.V_STATUS_2160_00_01)) {
			log.verbose("Return SCHEDULED_TO for STS2.0");
			return YantriksConstants.DT_SCHEDULED_TO;
		}
		//OMNI-51881 - End Changes- Adding for Handling SCHEDULED_TO as demand update on sts 2.0 shipment
		/*Added for handling ALLOCATED demand update on STS shipment*/ 
		/*else if(!YFCObject.isVoid(procureFromNode) && YantriksConstants.V_STATUS_2160_00_01.equals(status)) {*/
		//Changes for OMNI-63470
		else if(!YFCObject.isVoid(procureFromNode) && !StringUtil.isEmpty(status) && status.startsWith(YantriksConstants.V_STATUS_2160_00_01)) {
			/*
			 * OMNI-52525 - Start 
			 * STS 2.0 - SO Line Fulfillment In Progress - NodeType is Store - Set demandType as SCHEDULED
			 * STS 1.0 - SO Line Fulfillment In Progress - Set demandType as ALLOCATED
			 * 
			 * OMNI-53879 - Start
			 * STS1.0: Demand type is going as SCHEDULED instead of ALLOCATED once the TO shipment is created
			 * Changed from checking ShipNode NodeType to ProcureFromNode NodeType 
			 * */
			String procNodeType = "";
			if (!YFCObject.isVoid(procureFromNode)) {
				procNodeType = getShipNodeType(env,procureFromNode);
			}
			log.verbose("Proc nodeType:" + procNodeType + "ProcureFromNode:" + procureFromNode);
			
			if (YantriksConstants.V_STORE.equals(procNodeType)) {
				log.verbose("Return SCHEDULED for STS2.0");
				return YantriksConstants.DT_SCHEDULED;
			} else {
				log.verbose("Return ALLOCATED for STS");
				return YantriksConstants.DT_ALLOCATED;
			}
			// OMNI-52525 - End
		//Start OMNI-59114
		} else if(YantriksConstants.V_STATUS_3350_400.equals(status) && orderLineFulfillmentType.equals(AcademyConstants.V_FULFILLMENT_TYPE_BOPIS)) {
			log.verbose("Return BOPIS ready For Pickup"+status+orderLineFulfillmentType);
			return YantriksConstants.DT_ALLOCATED;
		} else if(YantriksConstants.V_STATUS_3700_100.equals(status) && orderLineFulfillmentType.equals(AcademyConstants.V_FULFILLMENT_TYPE_BOPIS)) {
			log.verbose("Return BOPIS Customer picked up"+status+orderLineFulfillmentType);
			return YantriksConstants.DT_DEFAULT_IGNORE;
		}
		/*
		 * OMNI-59114 - STS 1.0, 2.0 - OL status is in Received in Store, SO Released to
		 * SO RFCP status the Demand Type is Allocated 
		 * 3200 - Released, 3700 - Shipped, 2160.70.06.10 - Received In Store, 2160.01.100 - Arrived at Store As part of
		 * OMNI-60618 for Arrived at Store status the Demand Type is Allocated
		 */
		else if (AcademyConstants.V_FULFILLMENT_TYPE_STS.equals(orderLineFulfillmentType)
				&& (YantriksConstants.V_STATUS_2160_01_100.equals(status) || YantriksConstants.V_STATUS_2160_70_06_10.equals(status) 
						|| (status.compareTo(YantriksConstants.V_STATUS_3200) >= 0 && status.compareTo(YantriksConstants.V_STATUS_3700) < 0))) {
			log.verbose("Returning ALLOCATED Common Util Class for SO statuses from Arrived at Store, Received at Store till RFCP for STS Orders");
			return YantriksConstants.DT_ALLOCATED;
		}
		//End OMNI-59114
		/*For SOF , post Inbound shipment is shipped the below code 
		takes care of sending demand update as shipped for Released and SI Included In shipment status.
		*/
		else if (!YFCObject.isVoid(orderLineFulfillmentType) && YantriksConstants.SOF_FULFILLMENT_TYPE.contentEquals(orderLineFulfillmentType) && (status.compareTo(YantriksConstants.V_STATUS_3200) >= 0 && status.compareTo(YantriksConstants.V_STATUS_3700) < 0)) {
			log.verbose("Return IGNORE For SOF");
			return YantriksConstants.DT_DEFAULT_IGNORE;
		}
		/*OMNI-40029: End Change*/
		else if(YantriksConstants.V_STATUS_3350_400.equals(status) && !YFCObject.isVoid(invokedOnChangeOrderOnCancel) && YantriksConstants.STR_YES.equals(invokedOnChangeOrderOnCancel)){
			log.verbose("Returning Ignore From Common Util Class for Shortage scenario on Customer pick up");
			return YantriksConstants.DT_DEFAULT_IGNORE;
		}
		else if (YantriksConstants.V_STATUS_1300_100.equals(status)
				|| YantriksConstants.V_STATUS_1300.equals(status) || YantriksConstants.V_STATUS_1310.equals(status)) {
			log.verbose("Return BackOrder");
			return YantriksConstants.DT_BACKORDERED;
		}
		/*
		 * 1500 - Scheduled, 1600 - Awaiting Chained Order Creation, 2160.00.100 - , 2060 -
		 * Awaiting Procurement Transfer Order Creation, 2100.100 - PO Released, 3200 -
		 * Released, 3700 - Shipped, 2160.70.06.10 - Received In Store, 2160.01.100 -
		 * Arrived at Store
		 */
		else if (YantriksConstants.V_STATUS_1500.equals(status) || YantriksConstants.V_STATUS_2160_00_100.equals(status) 
				|| YantriksConstants.V_STATUS_1600.equals(status)|| YantriksConstants.V_STATUS_2060.equals(status) || YantriksConstants.V_STATUS_2100_100.equals(status)
				|| (status.compareTo(YantriksConstants.V_STATUS_3200) >= 0 && status.compareTo(YantriksConstants.V_STATUS_3700) < 0)
				/* Commented as part of OMNI-59114,OMNI-60618 - STS 1.0, 2.0 - OL status is in Arrived at Store, Received in Store
				|| YantriksConstants.V_STATUS_2160_70_06_10.equals(status)
				|| YantriksConstants.V_STATUS_2160_01_100.equals(status) */) {//Added Received In store status for which Demand Type needs to be sent as SCHEDULED as part of OMNI-40029
			log.verbose("Return SCHEDULED 1");
			return YantriksConstants.DT_SCHEDULED;
		} else if (YantriksConstants.V_STATUS_2160_01.equals(status)) {
			log.verbose("Return SCHEDULED_TO");
			return YantriksConstants.DT_SCHEDULED_TO;
		}
		else if(YantriksConstants.V_STATUS_3700.equals(status) || YantriksConstants.V_9000.equals(status) || 
				YantriksConstants.V_STATUS_3700_100.equals(status)	|| YantriksConstants.V_STATUS_3700_7777.equals(status)
				|| YantriksConstants.V_STATUS_1400.equals(status)) { 
			log.verbose("Returning Ignore From Common Util Class");
			return YantriksConstants.DT_DEFAULT_IGNORE;
		}
		// Below has been added lately without modifying the existing logic. Any other statuses that need to
		//be ignored can be added to the common code  YANTRIKS_DEMAND_IGN
		else if(setOfIgnoredStatuses.contains(status)){
			log.verbose("Returning Ignore Because Status is Configured in Common Codes to be Ignored");
			return YantriksConstants.DT_DEFAULT_IGNORE;
		}
//		else if (status.compareTo(YantriksConstants.V_STATUS_3200) >= 0 && status.compareTo(YantriksConstants.V_STATUS_3700) < 0) {
//			return YantriksConstants.DT_ALLOCATED;
//		}
		else {
			log.verbose("Returning SCHEDULED Common Util Class for other statuses");
			return YantriksConstants.DT_SCHEDULED;
		}
	}
	
	/*OMNI-40029: Start Change - Added method to fetch the Node Type of the ship node*/
	public static String getShipNodeType(YFSEnvironment env, String shipNode) {
		log.beginTimer("getShipNodeType");
		String strNodeType="";
		if(!YFCObject.isVoid(shipNode)) {
		Document docShipNodeInpList = SCXmlUtil.createDocument(YantriksConstants.SHIP_NODE);
		Element eleShipNodeInpList = docShipNodeInpList.getDocumentElement();
		eleShipNodeInpList.setAttribute(YantriksConstants.A_SHIP_NODE, shipNode);
		Document docShipNodeOutList = YantriksCommonUtil.invokeAPI(env,YantriksConstants.TEMP_GET_SHIP_NODE_LIST,YantriksConstants.API_GET_SHIP_NODE_LIST,
				docShipNodeInpList);
		Element eleShipNodeList = docShipNodeOutList.getDocumentElement();
		Element eleShipNode = (Element) eleShipNodeList.getElementsByTagName(YantriksConstants.SHIP_NODE).item(0);
		strNodeType = eleShipNode.getAttribute(YantriksConstants.A_NODE_TYPE);
		}
		log.verbose("strNodeType from getShipNodeType method is :"+strNodeType);
		log.endTimer("getShipNodeType");
		return strNodeType;
	}
	/*OMNI-40029: End Change*/

	public static String deriveDemandTypeFromStatusCA(String status) {
		if (status.compareTo("1100") >= 0 && status.compareTo("1300") < 0) {
//			return YantriksConstants.DT_OPEN_ORDER;
			return YantriksConstants.DT_RSRV_ORDER;

		} else if (YantriksConstants.V_STATUS_1300_100.equals(status)
				|| YantriksConstants.V_STATUS_1300.equals(status)) {
			
			return YantriksConstants.DT_BACKORDERED;
		} else if ("1500".equals(status) || "2160.00.100".equals(status)
			|| "1600".equals(status)|| "2060".equals(status)) {
			return YantriksConstants.DT_SCHEDULED;
		} else if (status.compareTo("3200") >= 0 && status.compareTo("3700") < 0) {
			return YantriksConstants.DT_ALLOCATED;
		} else if ("1310".equals(status)) {
			return "UNSCHEDULED";
		} else {
			return "IGNORE";
		}
	}

	public static void putFutureOnlyReservationDate(JSONObject demandObj, String reservationDate) throws JSONException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		String currentDate = YantriksCommonUtil.getCurrentDateOrTimeStamp(formatter);
		String shipDateToPut = null;
		if (reservationDate.length() > 10) {
			shipDateToPut = reservationDate.substring(0, 10);
		} else {
			shipDateToPut = reservationDate;
		}
		if (shipDateToPut.compareTo(currentDate) > 0) {
			demandObj.put(YantriksConstants.JSON_ATTR_RESERVATION_DATE, shipDateToPut);
		}
	}

	public static String convertShipDateToUTC(String shipDate) throws ParseException {
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		TimeZone tz1 = TimeZone.getTimeZone("UTC");
		sdf1.setTimeZone(tz1);
		Date sDate1 = sdf1.parse(shipDate);
		String sDate = sdf1.format(sDate1);
		return sDate.substring(0, 10);
	}

	public static String deriveShipNodeBasedOnDemandStatus(String shipNode, String procureFromNode, String status,String enableDmndUpdtOnBackOrder) {
		
		if (log.isDebugEnabled())
			log.debug(" Ship node and procureFromNode are :: "+shipNode +" , "+procureFromNode );
		
		if (!YFCObject.isVoid(status) && (YantriksConstants.V_STATUS_2160_01.equalsIgnoreCase(status)) || YantriksConstants.V_STATUS_2160_01_100.equalsIgnoreCase(status)) {
			return shipNode;
		}
		if(!YFCCommon.isVoid(procureFromNode) && (YantriksConstants.V_STATUS_3350.equalsIgnoreCase(status) || YantriksConstants.V_STATUS_3350_400.equalsIgnoreCase(status) || YantriksConstants.V_STATUS_2160_70_06_10.equalsIgnoreCase(status) || YantriksConstants.V_STATUS_3200.equalsIgnoreCase(status))) {
			return shipNode;
		}
		String ship_Node = "";
		if(!YFCCommon.isVoid(procureFromNode)) {
			ship_Node = procureFromNode;
		}else {
			ship_Node = shipNode;
		}
		 /*OMNI-52209: Start Change - Stamp blank ship node for shortage/backorder scenarios*/
		log.debug("Status in deriveShipNodeBasedOnDemandStatus: "+status);
		log.debug("enableDmndUpdtOnBackOrder: "+enableDmndUpdtOnBackOrder);
		if(!YFCObject.isVoid(enableDmndUpdtOnBackOrder)&& YantriksConstants.STR_YES.equals(enableDmndUpdtOnBackOrder)) {
			if(!YFCObject.isVoid(status) && YantriksConstants.V_STATUS_1300.equalsIgnoreCase(status)){
			log.debug("Status is backordered. Hence returning empty string" );
			return YantriksConstants.STR_BLANK;
			}
		}
		 /*OMNI-52209: End Change*/
		return ship_Node;
	}

	public static void removeExistingDateOnDemandOnCondition(JSONObject currDem) throws JSONException {
		// First check is to check reservation date is null or not
		if (null == currDem.get(YantriksConstants.JSON_RESERVATION_DATE)) {
			currDem.remove(YantriksConstants.JSON_ATTR_RESERVATION_DATE);
		} else {
			SimpleDateFormat formatterForDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
			String currentDate = YantriksCommonUtil.getCurrentDateOrTimeStamp(formatterForDate);
			String reservationDate = currDem.getString(YantriksConstants.JSON_ATTR_RESERVATION_DATE);
			if (reservationDate.compareTo(currentDate) <= 0) {
				// Removing Date if Existing reservation date in demand is less than or equal to
				// current date
				currDem.remove(YantriksConstants.JSON_ATTR_RESERVATION_DATE);
			}
		}
	}

	public static Map<String, Map<String, Map<String, Integer>>> prepareMapFromSchedulesToOrderStatuses(YFSEnvironment env,
			NodeList nlSchedules, NodeList nlOrderStatuses) throws ParseException {
		int scheduleLength = nlSchedules.getLength();
		int orderStatusesLength = nlOrderStatuses.getLength();
		Map<String, Map<String, Map<String, Integer>>> mapShipNodeDateToDemandQty = new HashMap<>();
		Map<String, Element> mapScheduleKeytoSchedules = new HashMap<>();
		for (int j = 0; j < scheduleLength; j++) {
			Element currSchedule = (Element) nlSchedules.item(j);
			String orderLineScheduleKey = currSchedule.getAttribute(YantriksConstants.A_ORDER_LINE_SCHEDULE_KEY);
			mapScheduleKeytoSchedules.put(orderLineScheduleKey, currSchedule);
		}

		for (int j = 0; j < orderStatusesLength; j++) {
			Element currOrderStatus = (Element) nlOrderStatuses.item(j);
			System.out.println("Fetched Order Status Element : " + SCXmlUtil.getString(currOrderStatus));
			String orderLineScheduleKey = currOrderStatus.getAttribute(YantriksConstants.A_ORDER_LINE_SCHEDULE_KEY);
			if (null == mapScheduleKeytoSchedules.get(orderLineScheduleKey)) {
				continue;
			}
			Element eleRespectiveSchedule = mapScheduleKeytoSchedules.get(orderLineScheduleKey);
			if (log.isDebugEnabled())
				log.debug("Fetched Order Status Element : " + SCXmlUtil.getString(eleRespectiveSchedule));
			String shipNode = eleRespectiveSchedule.getAttribute(YantriksConstants.A_SHIP_NODE);
			String procureFromNode = eleRespectiveSchedule.getAttribute(YantriksConstants.A_PROCURE_FROM_NODE);
			String fulfillmentType="";
			String statusQty = eleRespectiveSchedule.getAttribute(YantriksConstants.QUANTITY);
			int intStatusQty = (int) Double.parseDouble(statusQty);
			if (YFCObject.isVoid(shipNode) || 0 == intStatusQty) {
				continue;
			}
			String status = currOrderStatus.getAttribute(YantriksConstants.A_STATUS);
			String demandType = YantriksCommonUtil.deriveDemandTypeFromStatus(env,status,shipNode,procureFromNode,fulfillmentType,null);
			String shipNodeForDemand = YantriksCommonUtil.deriveShipNodeBasedOnDemandStatus(shipNode, procureFromNode,
					status,"N");// pass enableDmndUpdtOnBackOrder as N as part of OMNI-52209
			String expectedShipDate = eleRespectiveSchedule.getAttribute(YantriksConstants.A_EXPECTED_SHIP_DATE);
			String reservationDateToPut = YantriksCommonUtil.convertShipDateToUTC(expectedShipDate);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
			String currentDate = YantriksCommonUtil.getCurrentDateOrTimeStamp(formatter);
			if (reservationDateToPut.compareTo(currentDate) < 0) {
				reservationDateToPut = currentDate;
			}
			if (mapShipNodeDateToDemandQty.containsKey(shipNodeForDemand)) {
				Map<String, Map<String, Integer>> dateToDemandQtyMap = mapShipNodeDateToDemandQty
						.get(shipNodeForDemand);
				if (dateToDemandQtyMap.containsKey(reservationDateToPut)) {
					Map<String, Integer> mapDemandToQty = dateToDemandQtyMap.get(reservationDateToPut);
					if (mapDemandToQty.containsKey(demandType)) {
						int existingQty = mapDemandToQty.get(demandType);
						mapDemandToQty.put(demandType, existingQty + intStatusQty);
					} else {
						mapDemandToQty.put(demandType, intStatusQty);
					}
				} else {
					Map<String, Integer> demandToQtyMap = new HashMap<>();
					demandToQtyMap.put(demandType, intStatusQty);
					dateToDemandQtyMap.put(reservationDateToPut, demandToQtyMap);
				}
			} else {
				Map<String, Integer> newDemandQtyMap = new HashMap<>();
				newDemandQtyMap.put(demandType, intStatusQty);
				Map<String, Map<String, Integer>> dateToDemandQtyMap = new HashMap<>();
				dateToDemandQtyMap.put(reservationDateToPut, newDemandQtyMap);
				mapShipNodeDateToDemandQty.put(shipNodeForDemand, dateToDemandQtyMap);
			}
		}
		return mapShipNodeDateToDemandQty;
	}

	public static Map<String, Map<String, Map<String, Integer>>> prepareMapFromSchedulesToOrderStatuses(
			Element orderLine) throws ParseException {
		Element eleSchedules = SCXmlUtil.getChildElement(orderLine, YantriksConstants.ELE_SCHEDULES);
		Element eleOrderStatuses = SCXmlUtil.getChildElement(orderLine, YantriksConstants.ELE_ORDER_STATUSES);
		NodeList nlOrderStatuses = eleOrderStatuses.getElementsByTagName(YantriksConstants.ELE_ORDER_STATUS);
		NodeList nlSchedules = eleSchedules.getElementsByTagName(YantriksConstants.ELE_SCHEDULE);
		int scheduleLength = nlSchedules.getLength();
		int orderStatusesLength = nlOrderStatuses.getLength();
		Map<String, Map<String, Map<String, Integer>>> mapShipNodeDemandToDateQty = new HashMap<>();
		Map<String, Element> mapScheduleKeytoSchedules = new HashMap<>();
		for (int j = 0; j < scheduleLength; j++) {
			Element currSchedule = (Element) nlSchedules.item(j);
			String orderLineScheduleKey = currSchedule.getAttribute(YantriksConstants.A_ORDER_LINE_SCHEDULE_KEY);
			mapScheduleKeytoSchedules.put(orderLineScheduleKey, currSchedule);
		}

		for (int j = 0; j < orderStatusesLength; j++) {
			Element currOrderStatus = (Element) nlOrderStatuses.item(j);
			System.out.println("Fetched Order Status Element : " + SCXmlUtil.getString(currOrderStatus));
			String orderLineScheduleKey = currOrderStatus.getAttribute(YantriksConstants.A_ORDER_LINE_SCHEDULE_KEY);
			if (null == mapScheduleKeytoSchedules.get(orderLineScheduleKey)) {
				continue;
			}
			Element eleRespectiveSchedule = mapScheduleKeytoSchedules.get(orderLineScheduleKey);
			if (log.isDebugEnabled())
				log.debug("Fetched Order Status Element : " + SCXmlUtil.getString(eleRespectiveSchedule));
			String shipNode = eleRespectiveSchedule.getAttribute(YantriksConstants.A_SHIP_NODE);
			String procureFromNode = eleRespectiveSchedule.getAttribute(YantriksConstants.A_PROCURE_FROM_NODE);
			String statusQty = eleRespectiveSchedule.getAttribute(YantriksConstants.QUANTITY);
			int intStatusQty = (int) Double.parseDouble(statusQty);
			if (YFCObject.isVoid(shipNode) || 0 == intStatusQty) {
				continue;
			}
			String status = currOrderStatus.getAttribute(YantriksConstants.A_STATUS);
			String demandType = YantriksCommonUtil.deriveDemandTypeFromStatusCA(status);
			String shipNodeForDemand = YantriksCommonUtil.deriveShipNodeBasedOnDemandStatus(shipNode, procureFromNode,
					status,"N"); // pass enableDmndUpdtOnBackOrder as N as part of OMNI-52209
			String expectedShipDate = eleRespectiveSchedule.getAttribute(YantriksConstants.A_EXPECTED_SHIP_DATE);
			String reservationDateToPut = YantriksCommonUtil.convertShipDateToUTC(expectedShipDate);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
			String currentDate = YantriksCommonUtil.getCurrentDateOrTimeStamp(formatter);
			if (reservationDateToPut.compareTo(currentDate) < 0) {
				reservationDateToPut = currentDate;
			}
			if (mapShipNodeDemandToDateQty.containsKey(shipNodeForDemand)) {
				Map<String, Map<String, Integer>> demandToDateQtyMap = mapShipNodeDemandToDateQty
						.get(shipNodeForDemand);
				if (demandToDateQtyMap.containsKey(demandType)) {
					Map<String, Integer> mapDateToQty = demandToDateQtyMap.get(demandType);
					if (mapDateToQty.containsKey(reservationDateToPut)) {
						int existingQty = mapDateToQty.get(reservationDateToPut);
						mapDateToQty.put(reservationDateToPut, existingQty + intStatusQty);
					} else {
						mapDateToQty.put(reservationDateToPut, intStatusQty);
					}
				} else {
					Map<String, Integer> dateToQtyMap = new HashMap<>();
					dateToQtyMap.put(reservationDateToPut, intStatusQty);
					demandToDateQtyMap.put(demandType, dateToQtyMap);
				}
			} else {
				Map<String, Integer> newDateQtyMap = new HashMap<>();
				newDateQtyMap.put(reservationDateToPut, intStatusQty);
				Map<String, Map<String, Integer>> demandToDateQtyMap = new HashMap<>();
				demandToDateQtyMap.put(demandType, newDateQtyMap);
				mapShipNodeDemandToDateQty.put(shipNodeForDemand, demandToDateQtyMap);
			}
		}
		return mapShipNodeDemandToDateQty;
	}

	public static Map<String, Map<String, Map<String, Integer>>> prepareMapFromSchedulesToOrderStatusesWithShipDateAsSecondKey(
			Element orderLine) throws ParseException {
		Element eleSchedules = SCXmlUtil.getChildElement(orderLine, YantriksConstants.ELE_SCHEDULES);
		Element eleOrderStatuses = SCXmlUtil.getChildElement(orderLine, YantriksConstants.ELE_ORDER_STATUSES);
		NodeList nlOrderStatuses = eleOrderStatuses.getElementsByTagName(YantriksConstants.ELE_ORDER_STATUS);
		NodeList nlSchedules = eleSchedules.getElementsByTagName(YantriksConstants.ELE_SCHEDULE);
		int scheduleLength = nlSchedules.getLength();
		int orderStatusesLength = nlOrderStatuses.getLength();
		Map<String, Map<String, Map<String, Integer>>> mapShipNodeDateToDemandQty = new HashMap<>();
		Map<String, Element> mapScheduleKeytoSchedules = new HashMap<>();
		for (int j = 0; j < scheduleLength; j++) {
			Element currSchedule = (Element) nlSchedules.item(j);
			String orderLineScheduleKey = currSchedule.getAttribute(YantriksConstants.A_ORDER_LINE_SCHEDULE_KEY);
			mapScheduleKeytoSchedules.put(orderLineScheduleKey, currSchedule);
		}

		for (int j = 0; j < orderStatusesLength; j++) {
			Element currOrderStatus = (Element) nlOrderStatuses.item(j);
			System.out.println("Fetched Order Status Element : " + SCXmlUtil.getString(currOrderStatus));
			String orderLineScheduleKey = currOrderStatus.getAttribute(YantriksConstants.A_ORDER_LINE_SCHEDULE_KEY);
			if (null == mapScheduleKeytoSchedules.get(orderLineScheduleKey)) {
				continue;
			}
			Element eleRespectiveSchedule = mapScheduleKeytoSchedules.get(orderLineScheduleKey);
			if (log.isDebugEnabled())
				log.debug("Fetched Order Status Element : " + SCXmlUtil.getString(eleRespectiveSchedule));
			String shipNode = eleRespectiveSchedule.getAttribute(YantriksConstants.A_SHIP_NODE);
			String procureFromNode = eleRespectiveSchedule.getAttribute(YantriksConstants.A_PROCURE_FROM_NODE);
			String statusQty = eleRespectiveSchedule.getAttribute(YantriksConstants.QUANTITY);
			int intStatusQty = (int) Double.parseDouble(statusQty);
			if (YFCObject.isVoid(shipNode) || 0 == intStatusQty) {
				continue;
			}
			String status = currOrderStatus.getAttribute(YantriksConstants.A_STATUS);
			String demandType = YantriksCommonUtil.deriveDemandTypeFromStatusCA(status);
			String shipNodeForDemand = YantriksCommonUtil.deriveShipNodeBasedOnDemandStatus(shipNode, procureFromNode,
					status,"N");// pass enableDmndUpdtOnBackOrder as N as part of OMNI-52209
			String expectedShipDate = eleRespectiveSchedule.getAttribute(YantriksConstants.A_EXPECTED_SHIP_DATE);
			String reservationDateToPut = YantriksCommonUtil.convertShipDateToUTC(expectedShipDate);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
			String currentDate = YantriksCommonUtil.getCurrentDateOrTimeStamp(formatter);
			if (reservationDateToPut.compareTo(currentDate) < 0) {
				reservationDateToPut = currentDate;
			}
			if (mapShipNodeDateToDemandQty.containsKey(shipNodeForDemand)) {
				Map<String, Map<String, Integer>> dateToDemandQty = mapShipNodeDateToDemandQty.get(shipNodeForDemand);
				if (dateToDemandQty.containsKey(reservationDateToPut)) {
					Map<String, Integer> mapDemandToQty = dateToDemandQty.get(reservationDateToPut);
					if (mapDemandToQty.containsKey(demandType)) {
						int existingQty = mapDemandToQty.get(demandType);
						mapDemandToQty.put(demandType, existingQty + intStatusQty);
					} else {
						mapDemandToQty.put(demandType, intStatusQty);
					}
				} else {
					Map<String, Integer> demandToQtyMap = new HashMap<>();
					demandToQtyMap.put(demandType, intStatusQty);
					dateToDemandQty.put(reservationDateToPut, demandToQtyMap);
				}
			} else {
				Map<String, Integer> newDemandQtyMap = new HashMap<>();
				newDemandQtyMap.put(demandType, intStatusQty);
				Map<String, Map<String, Integer>> dateToDemandQtyMap = new HashMap<>();
				dateToDemandQtyMap.put(reservationDateToPut, newDemandQtyMap);
				mapShipNodeDateToDemandQty.put(shipNodeForDemand, dateToDemandQtyMap);
			}
		}
		return mapShipNodeDateToDemandQty;
	}

	public static boolean isNewBackOrderConfirmAssignmentImplemented(YFSEnvironment oEnv) {

		return true;
	}

	public static Map<String, Element> buildPrimeNoToOrderLineMap(Document getOrderListOP) {
		Map<String, Element> mapToReturn = new HashMap<>();
		Element eleRoot = getOrderListOP.getDocumentElement();
		Element eleOrder = SCXmlUtil.getChildElement(eleRoot, YantriksConstants.ORDER);
		Element eleOrderLines = SCXmlUtil.getChildElement(eleOrder, YantriksConstants.E_ORDER_LINES);
		NodeList nlOrderLines = eleOrderLines.getElementsByTagName(YantriksConstants.E_ORDER_LINE);
		int nlOrderLineLength = nlOrderLines.getLength();
		for (int i = 0; i < nlOrderLineLength; i++) {
			Element eleCurrOrderLine = (Element) nlOrderLines.item(i);
			String primeNo = eleCurrOrderLine.getAttribute(YantriksConstants.ATTR_PRIME_LINENO);
			mapToReturn.put(primeNo, eleCurrOrderLine);
		}
		return mapToReturn;
	}

	public static String determineFulfillmentType(String deliveryMethod, String fulfillmentType) {
		log.beginTimer("determineFulfillmentType");
		// Logic needs to be written accrodingly
		return "";
	}

	public static String modifyExistingLineReservationConsideringCapacity(YFSEnvironment env, String httpBody,
			String sellingChannel, String transactionType, boolean canReserveAfter, boolean considerCapacity,
			boolean considerGtin, boolean force, boolean ignoreAvailabilityCheck, boolean ignoreConsumptionDate,
			boolean restoreCapacity, boolean exceptionToBethrownOnYantriksErrors,
			boolean exceptionToBeThrownOnSystemErrors, boolean exceptionToBeThrownOnNotEnoughCapacity) {
		log.beginTimer("modifyExistingLineReservation");
		// Forming the URL for Post update at line level
		StringBuilder sb = new StringBuilder();
		sb.append(YantriksConstants.API_URL_RESERVATION);
		sb.append("/");
		sb.append(YantriksConstants.QP_LINES);
		sb.append("/");
		sb.append(sellingChannel);
		sb.append("/");
		sb.append(transactionType);
		sb.append("?");
		sb.append(YantriksConstants.CAN_RESERVE_AFTER);
		sb.append("=");
		sb.append(canReserveAfter);
		sb.append("&");
		sb.append(YantriksConstants.CONSIDER_CAPACITY);
		sb.append("=");
		sb.append(considerCapacity);
		sb.append("&");
		sb.append(YantriksConstants.CONSIDER_GTIN);
		sb.append("=");
		sb.append(considerGtin);
		sb.append("&");
		sb.append(YantriksConstants.FORCE);
		sb.append("=");
		sb.append(force);
		sb.append("&");
		sb.append(YantriksConstants.IGNORE_AVAILABILITY_CHECK);
		sb.append("=");
		sb.append(ignoreAvailabilityCheck);
		sb.append("&");
		sb.append(YantriksConstants.IGNORE_CONSUMPTION_DATE);
		sb.append("=");
		sb.append(ignoreConsumptionDate);
		sb.append("&");
		sb.append(YantriksConstants.QUERY_PARAM_RESTORE_CAPACITY);
		sb.append("=");
		sb.append(restoreCapacity);

		StringBuilder createReserveUrl = new StringBuilder();
		createReserveUrl.append(YantriksConstants.API_URL_RESERVATION);
		createReserveUrl.append("/");
		createReserveUrl.append(sellingChannel);
		createReserveUrl.append("/");
		createReserveUrl.append(transactionType);
		createReserveUrl.append("?");
		createReserveUrl.append(YantriksConstants.CAN_RESERVE_AFTER);
		createReserveUrl.append("=");
		createReserveUrl.append(canReserveAfter);
		createReserveUrl.append("&");
		createReserveUrl.append(YantriksConstants.CONSIDER_CAPACITY);
		createReserveUrl.append("=");
		createReserveUrl.append(considerCapacity);
		createReserveUrl.append("&");
		createReserveUrl.append(YantriksConstants.CONSIDER_GTIN);
		createReserveUrl.append("=");
		createReserveUrl.append(considerGtin);
		createReserveUrl.append("&");
		createReserveUrl.append(YantriksConstants.IGNORE_AVAILABILITY_CHECK);
		createReserveUrl.append("=");
		createReserveUrl.append(ignoreAvailabilityCheck);
		createReserveUrl.append("&");
		createReserveUrl.append(YantriksConstants.IGNORE_CONSUMPTION_DATE);
		createReserveUrl.append("=");
		createReserveUrl.append(ignoreConsumptionDate);
		createReserveUrl.append("&");
		createReserveUrl.append(YantriksConstants.QUERY_PARAM_RESTORE_CAPACITY);
		createReserveUrl.append("=");
		createReserveUrl.append(restoreCapacity);

		log.debug("API URL getting formed for PUT Call :: " + sb.toString());
		System.out.println("URL :: " + sb.toString());
		log.debug("Input Coming for Reservation Line Update :: " + httpBody);

		String jsonResponse = null;
		String output = null;
		// If retryAttempts are needed custom then it can be hardcoded here
		int retryAttempts = YantriksCommonUtil.getRetryAttempts();
		int retryCounter = 0;
		String httpMethod = YantriksConstants.HTTP_METHOD_PUT;
		String apiURL = sb.toString();
		boolean notEnoughCapacityCaught = false;
		try {
			while (retryCounter < retryAttempts) {
				log.debug("Calling API with Http Method :: " + httpMethod);
				log.debug("Calling API with URL :: " + apiURL);
				log.beginTimer("CallYantriksReservationAPI START" + retryCounter);
				log.endTimer("CallYantriksReservationAPI END" + retryCounter);
				log.debug("Output of Reservation Call :: " + output);
				if (output.contains(YantriksConstants.V_FAILURE)) {
					retryCounter++;
				} else if (output.contains(YantriksConstants.V_ENTITY_NOT_EXISTS)) {
					log.debug("Entity did not exist hence will flip the call to POST Reservation Call");

					retryCounter++;
				} else {
					break;
				}
			}

			String[] splitResponse = output.split(YantriksConstants.RES_SPLITTER);
			String responseString = splitResponse[0];
			if (splitResponse.length > 1) {
				jsonResponse = splitResponse[1];
			}

			if (YantriksConstants.V_NOT_ENOUGH_CAPACITY.equals(responseString)) {
				log.error("Yantriks API failed with " + output + " for the request " + httpBody
						+ "Hence throwing exception");
				notEnoughCapacityCaught = true;
				YFSException yfsException = new YFSException();
				yfsException.setErrorCode(YantriksConstants.ERR_CODE_YAN_NEC);
				yfsException
						.setErrorDescription("NOT_ENOUGH_CAPACITY for request " + httpBody + " API URL : " + apiURL);
				throw yfsException;
			} else if (YantriksConstants.V_NOT_ENOUGH_ATP.equals(responseString)
					|| YantriksConstants.V_VALIDATION_ERROR.equals(responseString)
					|| YantriksConstants.V_CONFLICT.equals(responseString)
					|| YantriksConstants.V_ENTITY_ALREADY_EXISTS.equals(responseString)) {
				JSONObject errObj = new JSONObject(jsonResponse);
				String status = errObj.getString(YantriksConstants.YIH_STATUS);
				String error = errObj.getString(YantriksConstants.JSON_ATTR_ERROR);
				String message = errObj.getString(YantriksConstants.JSON_ATTR_MESSAGE);
				log.error("Yantriks API Failed with status " + status + " Error " + error + " Message " + message
						+ " for Request " + httpBody);
				if (exceptionToBethrownOnYantriksErrors) {
					YFSException yfsException = new YFSException();
					yfsException.setErrorDescription("Yantriks API Failed with status " + status + " Error " + error
							+ " Message " + message + " for Request " + httpBody);
					throw yfsException;
				} else {
					log.error("Yantriks API Failed with status " + status + " Error " + error + " Message " + message
							+ " for Request " + httpBody);
				}
			} else if (YantriksConstants.IM_LIST_RESERVATION_FAILURES.contains(responseString)) {
				JSONObject errObj = new JSONObject(jsonResponse);
				String status = errObj.getString(YantriksConstants.YIH_STATUS);
				String error = errObj.getString(YantriksConstants.JSON_ATTR_ERROR);
				String message = errObj.getString(YantriksConstants.JSON_ATTR_MESSAGE);
				log.error("Yantriks API Failed with system errors, status of request " + status + " Error " + error
						+ " Message " + message + " for Request " + httpBody);
				if (exceptionToBeThrownOnSystemErrors) {
					log.error("Yantriks API Failed with system errors, status of request " + status + " Error " + error
							+ " Message " + message + " for Request " + httpBody);
					YFSException yfsException = new YFSException();
					yfsException.setErrorDescription("Yantriks API Failed with system errors, status of request "
							+ status + " Error " + error + " Message " + message + " for Request " + httpBody);
					throw yfsException;
				} else {
					log.error("Yantriks Call failed for Errors such as NOT_ENOUGH_ATP/Validation Errors for input : "
							+ httpBody);
				}
			} else {
				log.debug("Reservation Update in YAS Success");
			}
		} catch (Exception e) {
			log.error("Exception Caught while calling Reservation API Exception Message :: " + e.getMessage());
			if (exceptionToBeThrownOnNotEnoughCapacity && notEnoughCapacityCaught) {
				log.error("Not Enough Capacity while calling reservation api");
				if (e instanceof YFSException) {
					YFSException ex = (YFSException) e;
					throw ex;
				} else {
					YFSException yfsException = new YFSException();
					yfsException.setErrorCode(YantriksConstants.ERR_CODE_GENERIC);
					yfsException.setErrorDescription(
							"Exception caught while calling Reservation Yantriks API/Yantriks Server might be down"
									+ e.getMessage());
					throw yfsException;
				}
			} else if (exceptionToBeThrownOnSystemErrors) {
				if (e instanceof YFSException) {
					YFSException ex = (YFSException) e;
					throw ex;
				} else {
					YFSException yfsException = new YFSException();
					yfsException.setErrorCode(YantriksConstants.ERR_CODE_GENERIC);
					yfsException.setErrorDescription(
							"Exception caught while calling Reservation Yantriks API/Yantriks Server might be down"
									+ e.getMessage());
					throw yfsException;
				}
			} else {
				log.error("Exception Cause :: " + e.getCause());
			}
		}
		log.endTimer("modifyExistingLineReservation");
		return output;
	}

	public static boolean determineIgnoreConsumptionDateFlag(Document inputDoc) {
		String enterpriseCode = inputDoc.getDocumentElement().getAttribute(YantriksConstants.A_ENTERPRISE_CODE);
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.US);
		String currentTime = getCurrentDateOrTimeStamp(formatter);
		log.debug("determineIgnoreConsumptionDate() Current Time : " + currentTime);
		System.out.println("determineIgnoreConsumptionDate() Current Time : " + currentTime);
		System.out.println("getEST CutOff : " + getESTCutOffTime());
		System.out.println("getBST CutOff : " + getBSTCutOffTime());
		if (YantriksConstants.IM_LIST_EST_LOCALE_EC.contains(enterpriseCode)
				&& currentTime.compareTo(getESTCutOffTime()) < 0) {
			return false;
		} else if (YantriksConstants.IM_LIST_BST_LOCALE_EC.contains(enterpriseCode)
				&& currentTime.compareTo(getESTCutOffTime()) < 0) {
			return false;
		}
		return true;
	}

	public static boolean isIgnoreConsumptionDateFeatureEnabled(YFSEnvironment oEnv) {
		String isFeatureEnabled = YFSSystem.getProperty(YantriksConstants.PROP_ICD_IMPL_ENABLE);
		log.debug("isIgnoreConsumptionDateFeatureEnabled : " + isFeatureEnabled);
		if (YantriksConstants.CONST_FALSE.equals(isFeatureEnabled)
				|| YantriksConstants.STR_NO.equals(isFeatureEnabled)) {
			return false;
		}
		return true;
	}

	public static String getESTCutOffTime() {
		String estCutOffTime = YFSSystem.getProperty(YantriksConstants.PROP_EST_STORES_CUTOFFTIME_UTC);
		if (YFCObject.isVoid(estCutOffTime)) {
			return "17:30";
		} else {
			return estCutOffTime;
		}
	}

	public static String getBSTCutOffTime() {
		String bstCutOffTime = YFSSystem.getProperty(YantriksConstants.PROP_BST_STORES_CUTOFFTIME_UTC);
		if (YFCObject.isVoid(bstCutOffTime)) {
			return "12:30";
		} else {
			return bstCutOffTime;
		}
	}

	public static boolean isCutOffTimeCrossed(YFSEnvironment oEnv, Document inputDoc) throws Exception {

		return true;
	}

	

	/*OMNI -16848: Start Change - Modified the existing callYantriksAPI method to handle exception scenarios */
	public static String callYantriksAPI(String apiUrl, String httpMethod, String body, String productToCall,YFSEnvironment env) 
			 {
		HttpsURLConnection conn = null;
		String outputStr = "";
		int retryCount=0;
		int retryAttempts = YantriksCommonUtil.getRetryAttempts();
		String nonReprocessFlag="N";
		String responseBody="";
		boolean isReprocessible=false;
		boolean fetchResponseDetails=true;
		List<String> reprocessCodeList = new ArrayList<String>();
		List<String> nonreprocessCodeList = new ArrayList<String>();
		log.beginTimer("callYantriksAPI");
		String reprocessCodes=YFSSystem.getProperty(YantriksConstants.YANTRIKS_REPROCESS_CODE);
		String nonreprocessCodes=YFSSystem.getProperty(YantriksConstants.YANTRIKS_NONREPROCESS_CODE);
		if(!YFCObject.isVoid(reprocessCodes)) {
		reprocessCodeList = Arrays.asList(reprocessCodes.split(YantriksConstants.STR_COMMA));
		log.debug("reprocessCodeList:"+reprocessCodeList);
		}
		if(!YFCObject.isVoid(nonreprocessCodes)) {
		nonreprocessCodeList = Arrays.asList(nonreprocessCodes.split(YantriksConstants.STR_COMMA));
		log.debug("nonreprocessCodeList:"+nonreprocessCodeList);
		}
		try {
			//Yantriks Stub Invocation - Start
			String strInvokeYantriksStub = YFSSystem.getProperty(AcademyConstants.INVOKE_YANTRIKS_STUB);
			if(!YFCObject.isVoid(strInvokeYantriksStub) && AcademyConstants.STR_YES.equalsIgnoreCase(strInvokeYantriksStub)) {
				outputStr = AcademyYantriksStub.invokeYantriksStub(apiUrl, body);
				return outputStr;
			}
			//Yantriks Stub Invocation - End
			/* Invoke the Yantriks API and fetch the connection object */
			conn = invokeYantriksAPI(apiUrl,httpMethod,body,productToCall,env);
			boolean isInvokedOnCreateOrder = false;
			if (!YFCCommon.isVoid(env.getTxnObject("IsInvokedOnCreateOrder"))) {
				isInvokedOnCreateOrder = (boolean) env.getTxnObject("IsInvokedOnCreateOrder");
			}
			
			if (log.isDebugEnabled())
			log.debug("IsInvokedOnCreateOrder:" + isInvokedOnCreateOrder);
			/*OMNI-22625:Start Change  Handle entity does not exist response for Yantriks location onboarding update */
			boolean postCallRequired= false;
			if (!YFCCommon.isVoid(env.getTxnObject("postCallRequired"))) {
				postCallRequired = (boolean) env.getTxnObject("postCallRequired");
			}
			if (log.isDebugEnabled())
			log.debug("postCallRequired:"+postCallRequired);
			if(postCallRequired) {
				if (conn.getResponseCode() == YantriksConstants.RESP_CODE_400) {
						responseBody=getResponseDetails(conn);
						log.debug("Error Response:"+responseBody);
						fetchResponseDetails=false;
					 if (responseBody.contains(YantriksConstants.YANTRIKS_ENTITY_DOESNT_EXIST)) {
						 log.debug("Response code is 400 and Entity does not exist, hence making a POST call");
						 conn=invokeYantriksAPI(apiUrl,YantriksConstants.YIH_HTTP_METHOD_POST, body, productToCall, env);
						 fetchResponseDetails=true;
					 }
				}
			}
			/*OMNI-22625:End Change*/
			
			/* OMNI-34709 :Start */
			boolean putCallRequired= false;
			if (!YFCCommon.isVoid(env.getTxnObject("putCallRequired"))) {
				putCallRequired = (boolean) env.getTxnObject("putCallRequired");
			}
			if (log.isDebugEnabled())
				log.debug("putCallRequired:" + putCallRequired);
			if(putCallRequired) {
				if (conn.getResponseCode() == YantriksConstants.RESP_CODE_409) {
					 responseBody=getResponseDetails(conn);
					log.debug("Error Response:"+responseBody);
					log.debug("Response code is 409 for Node Control from yantriks, hence making a PUT call");
					conn=invokeYantriksAPI(apiUrl,YantriksConstants.HTTP_METHOD_PUT, body, productToCall, env);
				}
			}
			/* OMNI-34709 :End */
			
			if (log.isDebugEnabled())
			log.debug("Response Code :: "+conn.getResponseCode());
			String responseCode=String.valueOf(conn.getResponseCode());
			/* Success response code 200 or 201 from Yantriks , return the Yantriks API output*/
			if (conn.getResponseCode() == YantriksConstants.RESP_CODE_200 || conn.getResponseCode() == YantriksConstants.RESP_CODE_201) {
				
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

				String outputLine = null;
				while ((outputLine = br.readLine()) != null) {
					outputStr = outputStr.concat(outputLine);
				}
				if (log.isDebugEnabled()) {
					log.debug("Output from Server ....");
					log.debug(outputStr);
				}
				
				return outputStr;
			}
			/*Handle response codes other than 200 or 201*/
			if (conn.getResponseCode() != YantriksConstants.RESP_CODE_200 || conn.getResponseCode() != YantriksConstants.RESP_CODE_201) {
				
				
				log.info("Yantriks API Failed as it did not return 200 or 201");
				/*204 response code returned back to the invoked method for handling it */
				if (conn.getResponseCode() == YantriksConstants.RESP_CODE_204) {
					log.debug("No Content Found hence returning NO_CONTENT_FOUND");
					return YantriksConstants.V_NO_CONTENT_FOUND;
				}
				/*Handle non reprocessible error codes - 404, 401, 400 */
				if (!YFCObject.isVoid(responseCode)
						&& !reprocessCodeList.contains(responseCode)) { // OMNI-43045 : For any response code from Yantriks other than the reprocessible error codes , non reprocessible exception is thrown 
					log.info("Yantriks API call failed with response code:" + conn.getResponseCode()+": "+conn.getResponseMessage());
					log.debug(conn.getResponseMessage());
					log.debug("fetchResponseDetails:"+fetchResponseDetails);
					if(fetchResponseDetails){
					 responseBody= getResponseDetails(conn);
					log.info("Error Response:"+responseBody);
					}
					/*Throw error response if not invoked on create order*/
					if(!isInvokedOnCreateOrder) {
						throwErrorResponse(conn,apiUrl,responseBody);
					}
					
				}
				
				/*Handle reprocessible error codes - 500, 502*/
				if (!YFCObject.isVoid(responseCode) && reprocessCodeList.contains(responseCode)) {
					while (retryCount < retryAttempts) {
						if (nonreprocessCodeList.contains(responseCode)) {
							nonReprocessFlag="Y";
							break;
						} else {
							
							retryCount++;
							log.debug("Retry Attempt is " + retryCount);
							conn=invokeYantriksAPI(apiUrl, httpMethod, body, productToCall, env);
							responseCode=String.valueOf(conn.getResponseCode());
						}

					}
					
					/* log the error based on the flag stored in properties_file */
					String isYantriksLogEnabled=YFSSystem.getProperty(YantriksConstants.YANTRIKS_ISLOG_ENABLED);
					log.debug("isYantriksLogEnabled:"+isYantriksLogEnabled+"nonReprocessFlag:"+nonReprocessFlag);
					if (!isInvokedOnCreateOrder && !YFCObject.isVoid(isYantriksLogEnabled) && isYantriksLogEnabled.equalsIgnoreCase("Y") && nonReprocessFlag.equalsIgnoreCase("N")) {
						log.info("Could not succeed even after : "+retryAttempts+" retry attempts, response code "+conn.getResponseCode()+":"+conn.getResponseMessage());
						responseBody= getResponseDetails(conn);
						log.info("Error Response:"+responseBody);
						isReprocessible=true;
						throwErrorResponse(conn,apiUrl,responseBody);
					}
					
					
					/*Raising an alert on create order once retry attempts limit was reached */
					if(retryCount == retryAttempts && isInvokedOnCreateOrder) {
						log.info("Retry attempts reached the limits on createOrder,hence raising an alert");
						return YantriksConstants.CREATEORDER_RETRY_ALERT;
					}
					
					/*Throw error response if not invoked on create order and if response code is not 200 or 201 or 204*/
					if(!isInvokedOnCreateOrder && (conn.getResponseCode() != YantriksConstants.RESP_CODE_200 || conn.getResponseCode() != YantriksConstants.RESP_CODE_201)) {
					if( conn.getResponseCode() == YantriksConstants.RESP_CODE_204) {
						log.debug("No Content Found hence returning NO_CONTENT_FOUND");
						return YantriksConstants.V_NO_CONTENT_FOUND;
					}
					else if (nonReprocessFlag.equalsIgnoreCase("Y")){
						responseBody= getResponseDetails(conn);
						log.info("Error Response:"+responseBody);
						throwErrorResponse(conn,apiUrl,responseBody);
					}
					
					}

				}
				

			}
		} /*catch (IOException e) {
			throwIOException(e,apiUrl,body);
		} catch (ParserConfigurationException e) {
			throwParserConfigurationException(e,apiUrl,body);
		} */catch (Exception e) {
			throwException(e,apiUrl,body,isReprocessible);
		}
		log.endTimer("callYantriksAPI");
		conn.disconnect();
		return outputStr;
		
		
	}
	/* Handle exceptions for reprocessible and non reprocessible error codes as part of OMNI-16848*/
	private static void throwErrorResponse(HttpsURLConnection conn, String apiUrl,String responseBody)
			throws IOException {//This method is modified as part of custom error handling 
		// String responseBody= getResponseDetails(conn);
		log.info("Received failure response while invoking Yantriks API with url : " + apiUrl + " Response Code:"
				+ conn.getResponseCode() + " Response Body:" + responseBody);
		YFSException yfsException = new YFSException(
				"Received failure response while invoking Yantriks API with url : " + apiUrl,
				"Response Code: " + conn.getResponseCode(),
				"Response Message:" + conn.getResponseMessage() + ",Response Body:" + responseBody);
		throw yfsException;
		
	}

	/* Method created to fetch the error response body from Yantriks API call as part of OMNI-16848 */
	private static String getResponseDetails(HttpsURLConnection conn) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        String responseBody="";
		String outputLine = null;
			while ((outputLine = br.readLine()) != null) {
				responseBody = responseBody.concat(outputLine);
			}
		return responseBody;
	}

	/* Method created to handle IOException as part of OMNI-16848 
	private static void throwIOException(IOException e, String apiUrl, String body) {
		log.error("IOException Caught while invoking Yantriks API : " + e + "with input:"+ body);
		YFSException yfsException = new YFSException();
		yfsException.setErrorDescription("IOException Caught while invoking Yantriks API" + e.getMessage()+ "URL: "+ apiUrl);
		String strErrorCode = YantriksConstants.YANTRIKS_API_EXP;
		yfsException.setErrorCode(strErrorCode);
		throw yfsException;
	}
    
	/* Method created to handle ParserConfigurationException as part of OMNI-16848 
	private static void throwParserConfigurationException(ParserConfigurationException e, String apiUrl, String body) {
		log.error("ParserConfigurationException Caught while invoking Yantriks API : " + e + "with input:"+ body);
		YFSException yfsException = new YFSException();
		yfsException.setErrorDescription("ParserConfigurationException Caught while invoking Yantriks API" + e.getMessage()+ "URL: "+ apiUrl);
		String strErrorCode = YantriksConstants.YANTRIKS_API_EXP;
		yfsException.setErrorCode(strErrorCode);
		throw yfsException;
		
	}*/
	
	/* Method created to handle Exception as part of OMNI-16848 */
	private static void throwException(Exception e, String apiUrl, String body,boolean isReprocessible) {//This method is modified as part of custom error handling 
		log.debug("isReprocessible: " + isReprocessible);
		log.debug("e.getMessage(): " + e.getMessage());
		/*OMNI-43045 : Start Change :Throw custom error code only for reprocessible exception */
		if (isReprocessible) {
			log.info("Reprocessible Exception Caught while invoking Yantriks API : " + e + "with input:" + body);
			YFSException yfsException = new YFSException(e.getMessage(), YantriksConstants.ERR_CODE_29, e.getMessage());
			throw yfsException;
		} 
		/*OMNI-47903 : Start Change - Setting EXTN_ACADEMY_39 as error code for socket timeout exception*/
		else if (e.getMessage().contains(YantriksConstants.ERR_CONNECT_TIME_OUT) || e.getMessage().contains(YantriksConstants.ERR_SOCKET_TIME_OUT) || 
				e.getMessage().contains(YantriksConstants.ERR_CODE_39)) {
			log.info("Socket TimeOut Exception Caught while invoking Yantriks API : " + e + "with input:" + body);
			YFSException yfsException = new YFSException(e.getMessage(), YantriksConstants.ERR_CODE_39,
					e.getMessage());
			throw yfsException;
		}
		/*OMNI-47903 : End Change */
		else {
			log.info("Non Reprocessible Exception Caught while invoking Yantriks API : " + e + "with input:" + body);
			YFSException yfsException = new YFSException(e.getMessage(), YantriksConstants.YANTRIKS_API_EXP,
					e.getMessage());
			throw yfsException;
		}
		/*OMNI-43045 : End Change*/

	}

	

	/* Created this new method as part of exception handling to invoke the Yantriks API and fetch the connection object - OMNI-16848*/
	private static HttpsURLConnection invokeYantriksAPI(String apiUrl, String httpMethod, String body,
			String productToCall, YFSEnvironment env) throws ParserConfigurationException, Exception  {

		log.debug("API URL for Yantriks Call :: " + apiUrl);
		log.debug("Http Method :: " + httpMethod);
		log.debug("Request Body :: " + body);
		log.debug("Product to Call :: " + productToCall);
		log.beginTimer("invokeYantriksAPI");
		if ((YFCCommon.isVoid(httpMethod)) || (YFCCommon.isVoid(body)) || (YFCCommon.isVoid(apiUrl))) {
			if (log.isDebugEnabled())
				log.debug("Mandatory parameters are missing");
			if (log.isDebugEnabled()) {
				log.debug("httpMethod:: " + httpMethod + " body:: " + body + " apiUrl:: " + apiUrl);
			}
			// This Piece of code below handles the DELETE requests which do not have the
			// request body.
			if (!YantriksConstants.HTTP_METHOD_DELETE.equals(httpMethod) || YFCCommon.isVoid(apiUrl)) {
				log.debug("Returning Null for Non_DELETE method or EMPTY URL");
				return null;
			} else {
				log.debug("Continue processing delete request without body");
			}
		}

		String strIsSecurityEnabled = YantriksConstants.IS_SECURITY_ENABLED;
		String protocol = YantriksConstants.YIH_HTTPS_PROTOCOL;
		
		String encodedAuth = AcademyJWTTokenGenerator.getAuthToken("Academy");
		log.debug("encodedAuth:" + encodedAuth);
		String timeout = YFSSystem.getProperty("yantriks.timeout");
		if (log.isDebugEnabled()) {

			log.debug("protocol:: " + protocol +" timeout:: " + timeout);
			log.debug("protocol:: " + protocol + " timeout:: " + timeout);
		}
		URL url = new URL(apiUrl);
		HttpsURLConnection httpsconn = null;
		if (log.isDebugEnabled())
			log.debug("URL is:" + url.toString());
		if (log.isDebugEnabled()) {
			log.debug("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
			log.debug("HTTP url : " + url);
			log.debug("Body: " + body);
			log.debug("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		}
		long startTime = System.currentTimeMillis();
		if ("http".equals(protocol)) {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod(httpMethod);
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Content-Type", "application/json");
			if ("true".equalsIgnoreCase(strIsSecurityEnabled)) {
				conn.setRequestProperty("Authorization", "Bearer " + encodedAuth);
			}
			if (!YFCCommon.isVoid(timeout)) {
				conn.setConnectTimeout(Integer.parseInt(timeout));
			}
			if (httpMethod.equals(YantriksConstants.YIH_REQ_METHOD_POST)
					|| httpMethod.equals(YantriksConstants.HTTP_METHOD_PUT)) {
				OutputStream os = conn.getOutputStream();
				os.write(body.getBytes());
				os.flush();
			}
			long endTime = System.currentTimeMillis();
			if (log.isDebugEnabled()) {
				log.debug("Output from Server ...." + conn.toString());
			}
			int r = 0;
			int retryCounter = 0;
			try {
				r = conn.getResponseCode();
			}
			catch (SocketTimeoutException e) {
				
				//throw exception to the main file and invoke custom exception for integration server
				/*int retryAttempts = YantriksCommonUtil.getRetryAttempts();

				 // retry for retryAttempts times, if timeout exception occurs 
				 

				while (retryCounter < retryAttempts) {
					if (log.isDebugEnabled())
						log.debug(retryCounter + " Retry completed");

					r = conn.getResponseCode();
					if (conn.getResponseCode() == 200 || conn.getResponseCode() == 201
							|| conn.getResponseCode() == 409) {
						break;
					} else {
						retryCounter++;
					}
				}*/
				
				throwSocketTimeoutException(e,apiUrl,body);
			}
		} else if ("https".equals(protocol)) {
			log.debug("inside https method");
			httpsconn = (HttpsURLConnection) url.openConnection();
			log.debug("Con created");
			httpsconn.setDoInput(true);
			httpsconn.setDoOutput(true);
			httpsconn.setRequestMethod(httpMethod);
			httpsconn.setRequestProperty("Accept", "application/json");
			httpsconn.setRequestProperty("Content-Type", "application/json");
			if ("true".equalsIgnoreCase(strIsSecurityEnabled)) {
				httpsconn.setRequestProperty("Authorization", "Bearer " + encodedAuth);
			}
			if (!YFCCommon.isVoid(timeout)) {
				httpsconn.setConnectTimeout(Integer.parseInt(timeout));
			}
			int retryCounter = 0;
			try {
				if (httpMethod.equals(YantriksConstants.YIH_REQ_METHOD_POST)
						|| httpMethod.equals(YantriksConstants.HTTP_METHOD_PUT)) {
					OutputStream os = httpsconn.getOutputStream();
					os.write(body.getBytes());
					os.flush();
				}

				long endTime = System.currentTimeMillis();
				int r = 0;
				r = httpsconn.getResponseCode();
			} catch (SocketTimeoutException e) {

				/*int retryAttempts = YantriksCommonUtil.getRetryAttempts();

				 // retry for retryAttempts times, if timeout exception occurs 
				 

				while (retryCounter < retryAttempts) {
					if (log.isDebugEnabled())
						log.debug(retryCounter + " Retry completed");

					r = conn.getResponseCode();
					if (conn.getResponseCode() == 200 || conn.getResponseCode() == 201
							|| conn.getResponseCode() == 409) {
						break;
					} else {
						retryCounter++;
					}
				}*/
				throwSocketTimeoutException(e,apiUrl,body);
			}
		}

		log.endTimer("invokeYantriksAPI");
		log.debug("end of https method");
		
		return httpsconn;
	}
	
	/* Method created to handle SocketTimeoutException as part of OMNI-16848 */
	private static void throwSocketTimeoutException(SocketTimeoutException e, String apiUrl, String body) {
		log.info("SocketTimeoutException Caught while invoking Yantriks API : " + e + "with input:"+ body);
		YFSException yfsException = new YFSException();
		yfsException.setErrorDescription("SocketTimeoutException Caught while invoking Yantriks API" + e.getMessage()+ "URL: "+ apiUrl);
		/*OMNI-47903 : Start Change - setting a custom error code for reprocess on SocketTimeoutException*/
		String strErrorCode = YantriksConstants.ERR_CODE_39;
		/*OMNI-47903 : End Change */
		yfsException.setErrorCode(strErrorCode);
		throw yfsException;
		
	}

	/* OMNI-16848: End Change */

	public static String getLocationType(YFSEnvironment env, String shipNode) {
		// TODO Auto-generated method stub
		Document docShipNodeInpList = SCXmlUtil.createDocument(YantriksConstants.SHIP_NODE);
		Element eleShipNodeInpList = docShipNodeInpList.getDocumentElement();
		eleShipNodeInpList.setAttribute(YantriksConstants.A_SHIP_NODE, shipNode);
		Document docShipNodeOutList = YantriksCommonUtil.invokeAPI(env, YantriksConstants.API_GET_SHIP_NODE_LIST,
				docShipNodeInpList);
		Element eleShipNodeList = docShipNodeOutList.getDocumentElement();
		Element eleShipNode = (Element) eleShipNodeList.getElementsByTagName(YantriksConstants.SHIP_NODE).item(0);
		String strNodeType = eleShipNode.getAttribute(YantriksConstants.A_NODE_TYPE);
		String strLocationType = null;
		switch (strNodeType) {
		case YantriksConstants.DROP_SHIP_NODE_TYPE: {
			strLocationType = YantriksConstants.LT_DSV;
			break;
		}
		case YantriksConstants.LT_DC: {
			strLocationType = YantriksConstants.LT_DC;
			break;
		}
		case YantriksConstants.AT_LT_STORE: {
			strLocationType = YantriksConstants.LT_STORE;
			break;
		}
		case YantriksConstants.AT_LT_SHARED_INVENTORY_DC: {
			strLocationType = YantriksConstants.LT_DC;
			break;
		}
		}
		return strLocationType;
	}
	/**
	 * This method is used to fetch all the order line status which need to be ignore while preparing
	 * JSON update for Yantriks. In case of any new status is added in future that needs to be ignored,
	 * that can be added as comma separated value in the common code YANTRIKS_DEMAND_IGN.
	 */
	private static Set<String> getIgnoredStatuses(YFSEnvironment env) {
		log.verbose("Fetching the list of ignore codes");
		Document docCommonCode;
		String strCodes = "";
		try {
			docCommonCode = AcademyCommonCode.getCommonCodeList(env, YantriksConstants.YANTRIKS_DEMAND_IGNORE_STATUS, AcademyConstants.PRIMARY_ENTERPRISE);
			strCodes = XPathUtil.getString(docCommonCode.getDocumentElement(), "//CommonCodeList/CommonCode[@CodeValue='IgnoredStatuses']/@CodeLongDescription");
		} catch (Exception e) {
			log.verbose("Not able to fetch the common code");
		}

		Set<String> setOfStatuses = new HashSet<String>();
		if (!YFCObject.isVoid(strCodes)) {
			log.verbose("Statuses to ignore from common codes =" + strCodes);
			String[] arrayOfCodes = strCodes.split(YantriksConstants.STR_COMMA);
			for (int i = 0; i < arrayOfCodes.length; i++) {
				log.verbose("Adding to set " + arrayOfCodes[i]);
				setOfStatuses.add(arrayOfCodes[i]);
			}
		}
		return setOfStatuses;
	}
	
}
