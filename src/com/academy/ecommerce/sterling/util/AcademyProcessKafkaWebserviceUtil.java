package com.academy.ecommerce.sterling.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.HttpsURLConnection;

import org.w3c.dom.Document;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyProcessKafkaWebserviceUtil {

	public static ConcurrentHashMap<String, String> oAuthKafkaToken = new ConcurrentHashMap<>();

	//Retrieving the Success/Retry/Error response codes for COP file.
	private static List<String> listKafkaSuccessResponseCodes = Arrays.asList(
			YFSSystem.getProperty("academy.kafka.success.name").split(AcademyConstants.STR_COMMA));
	private static List<String> listKafkaRetryResponseCodes = Arrays.asList(
			YFSSystem.getProperty("academy.kafka.retry.name").split(AcademyConstants.STR_COMMA));
	private static List<String> listKafkaErrorResponseCodes = Arrays.asList(
			YFSSystem.getProperty("academy.kafka.error.name").split(AcademyConstants.STR_COMMA));

	//Creating a generic hashmap for all response codes for each category
	private static HashMap<String, HashMap<String, String>> hmResponseCode = new  HashMap<String, HashMap<String, String>>();

	//Logger defination
	private static YFCLogCategory logger = YFCLogCategory.instance(AcademyProcessKafkaWebserviceUtil.class);

	//Static block to set all the properties to be used by utils
	static {
		logger.verbose("Inside Static Block");
		addToTaskMap();
	}

	/**
	 * This method invokes the Kafka webservice 
	 * 
	 * @param env
	 * @param strKafkaWebserviceType
	 * @param strInput
	 * @return docOutput
	 */
	public Document invokeKafkaWebserviceApi(YFSEnvironment env, String strKafkaWebserviceType, String strInput, String strInvocationType) throws Exception{
		logger.beginTimer("AcademyProcessKafkaWebserviceUtil.invokeKafkaWebserviceApi method ");
		Document docOutput = null;

		try {

			//Invoke the Kafka webservice call
			docOutput = invokeKafkaRestWebservice(strKafkaWebserviceType, strInput, strInvocationType);			
		}
		catch (YFSException yfsEx) {
			throw yfsEx;
		}
		catch (Exception exp) {
			//Creating info logger for Splunk alerts 
			logger.info("Kafka Error :: Exception while invoking Kafka ::" + 
					strKafkaWebserviceType	+ " . Error Trace :: "+exp.getStackTrace());

			YFSException yfsExecPOCreate = new YFSException();
			yfsExecPOCreate.setErrorCode("KAFKA_101");
			yfsExecPOCreate.setErrorDescription("Error while trying Invoking Kafka :: " + exp.getMessage());
			throw yfsExecPOCreate;
		}

		logger.endTimer("AcademyProcessKafkaWebserviceUtil.invokeKafkaWebserviceApi method ");
		return docOutput;
	}



	/**
	 * This method invokes the Kafka REST API .
	 * 
	 * @param env
	 * @param strKafkaWebserviceType
	 * @return strToken
	 * @throws Exception
	 */
	public Document invokeKafkaRestWebservice(String strKafkaWebserviceType, String strInput, String strInvocationType) throws Exception {
		logger.beginTimer("AcademyProcessKafkaWebserviceUtil.invokeKafkaRestWebservice method ");
		logger.verbose(":: invokeKafkaRestWebservice() :: strKafkaWebserviceType :: " + strKafkaWebserviceType);
		logger.verbose(":: invokeKafkaRestWebservice() :: strInput :: " + strInput);

		Document docResponse = XMLUtil.createDocument(AcademyConstants.ELE_RESPONSE);

		try {

			URL url = new URL(YFSSystem.getProperty(strKafkaWebserviceType + ".url"));

			//HttpsURLConnection httpConn = (HttpsURLConnection) url.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			
			httpConn.setRequestMethod(AcademyConstants.WEBSERVICE_POST);
			httpConn.setRequestProperty(AcademyConstants.WEBSERVICE_CONTENT_TYPE, "application/vnd.kafka.json.v2+json");
			httpConn.setRequestProperty(AcademyConstants.WEBSERVICE_ACCEPT, AcademyConstants.ACCEPT_APPLICATION_JSON);
			httpConn.setDoOutput(true);


			//Setting Timeout
			//String strKafkaTimeout = "30000";
			String strKafkaTimeout = YFSSystem.getProperty(strKafkaWebserviceType + ".timeout");
			if (!YFSObject.isVoid(strKafkaTimeout)) {
				httpConn.setConnectTimeout(Integer.parseInt(strKafkaTimeout));
				httpConn.setReadTimeout(Integer.parseInt(strKafkaTimeout));
			}

			httpConn.setDoInput(true);
			httpConn.setDoOutput(true);
			httpConn.setUseCaches(false);
			httpConn.setAllowUserInteraction(false);
			httpConn.setRequestProperty("charset", "UTF-8");
			OutputStream outStream = httpConn.getOutputStream();
			OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream, "UTF-8");
			outStreamWriter.write(strInput);
			outStreamWriter.flush();
			outStreamWriter.close();
			outStream.close();

			String strResponseCode = Integer.toString(httpConn.getResponseCode());
			
			//This is used to read only for https calls response
			//String strResponseMessage = httpConn.getResponseMessage();

			
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getResponseCode() / 100 == 2 ? httpConn.getInputStream() : httpConn.getErrorStream()));

			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = reader.readLine()) != null) {
				response.append(inputLine);
			}
			reader.close();

			//print result
			logger.verbose(response.toString());
			String strResponseMessage = response.toString();
						
			logger.verbose("Response Message--->" + strResponseMessage + "--Response Code-->" + strResponseCode);

			docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, strResponseCode);
			docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_RESPONSE_MESSAGE, strResponseMessage);

			String strExceptionType = validateKafkaResponse(strResponseCode, strResponseMessage, strInvocationType);

			docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_STATUS, strExceptionType);

		} 
		catch (YFSException yfsEx) {
			throw yfsEx;
		}
		catch (Exception e) {
			//Creating info logger for Splunk alerts 
			logger.info("Kafka Invocation Error ::  Error while invoking Kafka : " 
					+ strKafkaWebserviceType + " . Error Trace :: "+e.getStackTrace());

			YFSException yfsExcep = new YFSException(e.getMessage());
			yfsExcep.setErrorCode("KAFKA_101");
			yfsExcep.setErrorDescription("Error While trying to Invoke Kafka " + strKafkaWebserviceType);
			throw yfsExcep;
		}

		logger.endTimer("AcademyProcessKafkaWebserviceUtil.invokeKafkaRestWebservice method ");
		return docResponse;

	}




	/**
	 * This method will create task map which will define which responses are valid or 
	 * error or retry for Kafka
	 * 
	 * @param listProp
	 * @param strPropName
	 */
	static void addToTaskMap() {

		logger.debug("Begin of AcademyProcessKafkaWebserviceUtil.addToTaskMap() method .");
		logger.verbose("Kafka:: Success.size():" + listKafkaSuccessResponseCodes.size());
		logger.verbose("Kafka:: Retry.size():" + listKafkaRetryResponseCodes.size());
		logger.verbose("Kafka:: Error.size():" + listKafkaErrorResponseCodes.size());
		//The Map is of the format <POCreate, <400_Bad Request, RETRY>>;

		updateDetailsToMap("SUCCESS", listKafkaSuccessResponseCodes);
		updateDetailsToMap("RETRY", listKafkaRetryResponseCodes);
		updateDetailsToMap("ERROR", listKafkaErrorResponseCodes);

		logger.verbose("Kafka final map::" + hmResponseCode);
		logger.debug("End of AcademyProcessKafkaWebserviceUtil.addToTaskMap()");
	}

	/**
	 * This method will sort out the different errors for each type of Kafka
	 * 
	 * @param strResponseType
	 * @param ListResponseCodes
	 */
	private static void updateDetailsToMap(String strResponseType, List<String> listResponseCodes) {

		logger.beginTimer("AcademyProcessKafkaWebserviceUtil.updateDetailsToMap() method");
		logger.verbose("Kafka:: listResponseCodes.size():" + listResponseCodes.size() + " strResponseType:" + strResponseType);
		Iterator<String> itr = listResponseCodes.iterator(); 

		//Iterating through each error code to sort based on HttpCode and response error
		while (itr.hasNext()) {
			String strValue = itr.next();
			logger.verbose("Kafka :: strValue:" + strValue);
			String[] arrOfStr = strValue.split("/");

			String strResponseCode = null;
			String strChubModule = null;

			//If the property is defined with http code and response message
			if(arrOfStr.length==3) {
				logger.verbose("Kafka :: arrOfStr[0]:" + arrOfStr[0]);
				logger.verbose("Kafka :: arrOfStr[1]:" + arrOfStr[1]);
				logger.verbose("Kafka :: arrOfStr[2]:" + arrOfStr[2]);
				strResponseCode = arrOfStr[0] + "_" + arrOfStr[1] ;
				strChubModule = arrOfStr[2] ;

			}
			//If property is only defined based on the http code
			else if(arrOfStr.length==2) {
				logger.verbose("Kafka :: arrOfStr[0]:" + arrOfStr[0]);
				logger.verbose("Kafka :: arrOfStr[1]:" + arrOfStr[1]);
				strResponseCode = arrOfStr[0] ;
				strChubModule = arrOfStr[1] ;
			}

			//The Map is of the format <DSVSupplierCode, <400_Bad Request, RETRY>>;
			if(hmResponseCode.containsKey(strChubModule)) {
				HashMap<String, String> hmResponseType = hmResponseCode.get(strChubModule);
				hmResponseType.put(strResponseCode, strResponseType);
				hmResponseCode.put(strChubModule, hmResponseType);
			}
			else {
				HashMap<String, String> hmResponseType = new HashMap<String,String>();
				hmResponseType.put(strResponseCode, strResponseType);
				hmResponseCode.put(strChubModule, hmResponseType);
			}
		}
		logger.debug(":: hmResponseCode :: "+hmResponseCode.toString());
		logger.endTimer("AcademyProcessKafkaWebserviceUtil.updateDetailsToMap() method");
	}


	/**
	 * This method provide a response if the http status code or message is to 
	 * be success or retry or error based on the type of invocation.
	 * 
	 * @param strResponseCode
	 * @param strResponseMessage
	 * @param strInvocationType
	 * @return String
	 */
	@SuppressWarnings("unchecked")
	public String validateKafkaResponse(String strResponseCode, String strResponseMessage, String strInvocationType) {
		logger.beginTimer("AcademyProcessKafkaWebserviceUtil.validateKafkaResponse() method");
		logger.verbose("Response Message--->" + strResponseMessage + "--Response Code-->" + strResponseCode);

		HashMap<String,String> hmKafkaExceptionTypes = hmResponseCode.get(strInvocationType);

		if(!YFCObject.isVoid(strResponseMessage) && 
				hmKafkaExceptionTypes.containsKey(strResponseCode + "_" + strResponseMessage)) {
			logger.verbose("Updated Response Code-->" + strResponseCode);
			return hmKafkaExceptionTypes.get(strResponseCode + "_" + strResponseMessage);

		}

		if(hmKafkaExceptionTypes.containsKey(strResponseCode)) {
			return hmKafkaExceptionTypes.get(strResponseCode);
		}

		logger.verbose(" Direct Response Code is not matching. So checking only for the 2XX, 4XX, 5XX sceanrios ");

		Iterator<?> iter = hmKafkaExceptionTypes.entrySet().iterator();
		logger.verbose(":: hmKafkaExceptionTypes:: -->" + hmKafkaExceptionTypes.toString());

		while (iter.hasNext()) {
			Map.Entry<String, String> testMap = (Map.Entry<String, String>)iter.next();
			String strKey = testMap.getKey();

			if(strKey.contains("XX")) {
				logger.verbose(" Validating :: " + strKey.split("X")[0]);
				//Check if the response code start with the value
				if(strResponseCode.startsWith(strKey.split("X")[0])) {
					return testMap.getValue();
				}

			}

		}
		logger.endTimer("AcademyProcessKafkaWebserviceUtil.validateKafkaResponse() method");
		return "NA";
	}

}
