package com.academy.ecommerce.sterling.dsv.util;

/**#########################################################################################
 *
 * Project Name                : CHub Migration for DSV
 * Author                      : Everest
 * Author Group				  : DSV
 * Date                        : 09-AUG-2022 
 * Description				  : This is a util class which contails all the methods required 
 * 								creating a PO and all utils for DSV-Chub project
 * 								 
 * ---------------------------------------------------------------------------------
 * Date            	Author         			Version#       		Remarks/Description                      
 * ---------------------------------------------------------------------------------
 * 09-Aug-2022		Everest  	 			  1.0           	Initial version
 *
 * #########################################################################################*/

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.ibm.sterling.afc.jsonutil.PLTJSONUtils;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyComHubRestApiUtil implements YIFCustomApi {

	private Properties props;
	public static ConcurrentHashMap<String, String> oAuthToken = new ConcurrentHashMap<>();

	//Instance of logger for the java code
	private static YFCLogCategory logger = YFCLogCategory.instance(AcademyComHubRestApiUtil.class);

	//Retrieving the Success/Retry/Error response codes for COP file.
	private static List<String> listCHubSuccessResponseCodes = Arrays.asList(
			YFSSystem.getProperty("academy.chub.success.name").split(AcademyConstants.STR_COMMA));
	private static List<String> listCHubRetryResponseCodes = Arrays.asList(
			YFSSystem.getProperty("academy.chub.retry.name").split(AcademyConstants.STR_COMMA));
	private static List<String> listCHubErrorResponseCodes = Arrays.asList(
			YFSSystem.getProperty("academy.chub.error.name").split(AcademyConstants.STR_COMMA));

	//Creating a generic hashmap for all response codes for each category
	private static HashMap<String, HashMap<String, String>> hmResponseCode = new  HashMap<String, HashMap<String, String>>();


	//Static block to set all the properties to be used by utils
	static {
		logger.verbose("Inside Static Block");
		System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
		addToTaskMap();
	}


	String strMessage = null;

	String strCOMHUBCreatePOURL = YFSSystem.getProperty(AcademyConstants.ACADEMY_COMHUB_REST_CREATEPO_URL);
	String strCreatePOTimeout = YFSSystem.getProperty(AcademyConstants.ACADEMY_COMHUB_REST_CREATEPO_TIMEOUT);
	String strGrantType = YFSSystem.getProperty(AcademyConstants.ACADEMY_COMHUB_REST_TOKEN_GRANTTYPE);
	String strClientId = YFSSystem.getProperty(AcademyConstants.ACADEMY_COMHUB_REST_TOKEN_CLIENTID);
	String strClientSecret = YFSSystem.getProperty(AcademyConstants.ACADEMY_COMHUB_REST_TOKEN_CLIENTSECRET);
	String strTokenUrl = YFSSystem.getProperty(AcademyConstants.ACADEMY_COMHUB_REST_TOKEN_URL);



	/**
	 * This method will create task map which will define which responses are valid or 
	 * error or retry for CHUB
	 * 
	 * @param listProp
	 * @param strPropName
	 */
	static void addToTaskMap() {

		logger.debug("Begin of AcademyComHubRestApiUtil.addToTaskMap() method .");
		logger.verbose("CHub:: Success.size():" + listCHubSuccessResponseCodes.size());
		logger.verbose("CHub:: Retry.size():" + listCHubRetryResponseCodes.size());
		logger.verbose("CHub:: Error.size():" + listCHubErrorResponseCodes.size());
		//The Map is of the format <POCreate, <400_Bad Request, RETRY>>;

		updateDetailsToMap("SUCCESS", listCHubSuccessResponseCodes);
		updateDetailsToMap("RETRY", listCHubRetryResponseCodes);
		updateDetailsToMap("ERROR", listCHubErrorResponseCodes);

		logger.verbose("CHub final map::" + hmResponseCode);
		logger.debug("End of AcademyComHubRestApiUtil.addToTaskMap()");
	}



	/**
	 * This method will sort out the different errors for each type of Chub
	 * 
	 * @param listProp
	 * @param strPropName
	 */
	private static void updateDetailsToMap(String strResponseType, List<String> listResponseCodes) {

		logger.beginTimer("AcademyComHubRestApiUtil.updateDetailsToMap() method");
		logger.verbose("CHub:: listResponseCodes.size():" + listResponseCodes.size() + " strResponseType:" + strResponseType);
		Iterator<String> itr = listResponseCodes.iterator(); 

		//Iterating through each error code to sort based on HttpCode and response error
		while (itr.hasNext()) {
			String strValue = itr.next();
			logger.verbose("CHub :: strValue:" + strValue);
			String[] arrOfStr = strValue.split("/");

			String strResponseCode = null;
			String strChubModule = null;

			//If the property is defined with http code and response message
			if(arrOfStr.length==3) {
				logger.verbose("CHub :: arrOfStr[0]:" + arrOfStr[0]);
				logger.verbose("CHub :: arrOfStr[1]:" + arrOfStr[1]);
				logger.verbose("CHub :: arrOfStr[2]:" + arrOfStr[2]);
				strResponseCode = arrOfStr[0] + "_" + arrOfStr[1] ;
				strChubModule = arrOfStr[2] ;

			}
			//If property is only defined based on the http code
			else if(arrOfStr.length==2) {
				logger.verbose("CHub :: arrOfStr[0]:" + arrOfStr[0]);
				logger.verbose("CHub :: arrOfStr[1]:" + arrOfStr[1]);
				strResponseCode = arrOfStr[0] ;
				strChubModule = arrOfStr[1] ;
			}


			//The Map is of the format <POCreate, <400_Bad Request, RETRY>>;
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
		logger.endTimer("AcademyComHubRestApiUtil.updateDetailsToMap() method");
	}


	/**
	 * This method provide a response if the http status code or message is to 
	 * be success or retry or error based on the type of invocation.
	 * 
	 * @param listProp
	 * @param strPropName
	 */
	public String validateChubResponse(String strResponseCode, String strResponseMessage, String strInvocationType) {
		logger.beginTimer("AcademyComHubRestApiUtil.validateChubResponse() method");
		logger.verbose("Response Message--->" + strResponseMessage + "--Response Code-->" + strResponseCode);

		HashMap<String,String> hmChubExceptionTypes = hmResponseCode.get(strInvocationType);

		if(!YFCObject.isVoid(strResponseMessage) && 
				hmChubExceptionTypes.containsKey(strResponseCode + "_" + strResponseMessage)) {
			logger.verbose("Updated Response Code-->" + strResponseCode);
			return hmChubExceptionTypes.get(strResponseCode + "_" + strResponseMessage);

		}
		
		if(hmChubExceptionTypes.containsKey(strResponseCode)) {
			return hmChubExceptionTypes.get(strResponseCode);
		}

		logger.verbose(" Direct Response Code is not matching. So checking only for the 2XX, 4XX, 5XX sceanrios ");

		Iterator iter = hmChubExceptionTypes.entrySet().iterator();
		logger.verbose(":: hmChubExceptionTypes:: -->" + hmChubExceptionTypes.toString());
		
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
		logger.endTimer("AcademyComHubRestApiUtil.validateChubResponse() method");
		return "NA";
	}




	public void setProperties(Properties props) throws Exception {
		this.props = props;

	}

	/**
	 * This method converts the JSON to xml format
	 * 
	 * @param env
	 * @return iExpiryBufferSecs
	 * @throws Exception
	 */
	public Document getXMLFromJSON(String jsonInput, String strRootName) throws Exception {
		logger.beginTimer("getXMLFromJSON Started");
		Document docOutput = null;

		if (!YFCCommon.isVoid(jsonInput)) {
			docOutput = PLTJSONUtils.getXmlFromJSON(jsonInput, strRootName);
		}
		logger.endTimer("getXMLFromJSON End");
		return docOutput;
	}

	/**
	 * This method retrieve the Access Token for CHub using the below logic
	 *  Get Token first from Static Map. 
	 *  If token is not present in static map, then look in DB. 
	 *  If not Present in both DB and Map then make a call to CHub Token
	 *  	 OAuth Token URL and get an new token. 
	 *  Update new token in both DB and Static Map. 
	 *  In few edge cases there can be more than one Token in DB. 
	 *  In that case we will always get the latest record from the DB and update it.
	 * 
	 * @param env
	 * @return iExpiryBufferSecs
	 * @throws Exception
	 */
	public String getAccessToken(YFSEnvironment env, int iExpiryBufferSecs) throws Exception {
		logger.beginTimer("AcademyComHubRestApiUtil.getAccessToken method ");
		logger.verbose(":: getAccessToken() :: iExpiryBufferSecs :: " + iExpiryBufferSecs);
		logger.verbose(":: getAccessToken env() :: getProgId :: " + env.getProgId());

		Date dtTokenExpiryTimeStamp = null;
		String strAccessToken = null;
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
		Date dtCurrentDate = new Date();

		try {
			if (!oAuthToken.isEmpty()) {
				logger.verbose("oAuthToken is NOT Empty");

				dtTokenExpiryTimeStamp = sdf.parse(oAuthToken.get(AcademyConstants.STR_TOKEN_EXPIRY_TIMESTAMP));
				strAccessToken = oAuthToken.get(AcademyConstants.ATTR_ACCESS_TOKEN);

				logger.verbose(":: From JVM expiresTimeStamp: " + dtTokenExpiryTimeStamp);
				logger.verbose(":: From JVM strAccessToken: " + strAccessToken);
			}

			if (oAuthToken.isEmpty() || (dtTokenExpiryTimeStamp != null && dtCurrentDate.after(dtTokenExpiryTimeStamp))) {
				// JVM variable checking
				logger.verbose("Toke Expired or JVM Token not available ");
				String strDBTokenInp = "<AcadReuseAccessToken IgnoreOrdering='N' MaximumRecords='1' TargetSystem='CHUB'>"
						+ "<OrderBy><Attribute Name='Modifyts' Desc='Y'/></OrderBy></AcadReuseAccessToken>";
				Document docDBTokenInp = XMLUtil.getDocument(strDBTokenInp);

				//Retrieving the token details from DB
				Document docDBTokenOutput = AcademyUtil.invokeService(env,
						AcademyConstants.ACADEMY_GET_REUSE_ACCESS_TOKEN_LIST, docDBTokenInp);
				List lstDBTokenDeails = XMLUtil.getElementListByXpath(docDBTokenOutput,
						"AcadReuseAccessTokenList/AcadReuseAccessToken");

				if (lstDBTokenDeails.size() > 0) {
					logger.verbose("lstDBTokenDeails is NOT Empty-->" + lstDBTokenDeails);
					Element eleDBTokenDeails = (Element) (lstDBTokenDeails.get(0));
					String strDBTokenKey = eleDBTokenDeails.getAttribute(AcademyConstants.REUSE_ACCESS_TOKEN_KEY);
					String strDBAccessToken = eleDBTokenDeails.getAttribute(AcademyConstants.ACCESS_TOKEN);
					//String strDBExpDateTimeInJVMTimeZone = getDateInJVMDateTimeStamp(strDBExpiresTimeStamp, sdf);
					String strDBExpDateTimeInJVMTimeZone = eleDBTokenDeails.getAttribute(AcademyConstants.EXPIRES_ON);

					Calendar cal = Calendar.getInstance();
					cal.setTime(sdf.parse(strDBExpDateTimeInJVMTimeZone));

					Date dbExpiresTimeStamp = cal.getTime();
					logger.verbose("AcademyCOMHUBRestApiUtil.getAccessToken() dbExpiresTimeStamp: " + dbExpiresTimeStamp);

					if (dbExpiresTimeStamp.before(dtCurrentDate)) {
						//Retrieving the token from CHUB
						Document docToken = getAccessTokenFromCHub();

						String strToken = docToken.getDocumentElement()
								.getAttribute(AcademyConstants.ATTR_ACCESS_TOKEN);
						String strTokenExpiryTS = docToken.getDocumentElement()
								.getAttribute(AcademyConstants.STR_TOKEN_EXPIRY_IN);

						Calendar calTokenExpiry = Calendar.getInstance();
						calTokenExpiry.setTime(dtCurrentDate);
						calTokenExpiry.add(Calendar.SECOND, Integer.parseInt(strTokenExpiryTS) - iExpiryBufferSecs);
						Date dtTokeExpiryTimeStamp = calTokenExpiry.getTime();

						logger.verbose(
								"AcademyCOMHUBRestApiUtil.getAccessToken() method after adding secs expiresTimeStamp:"
										+ dtTokeExpiryTimeStamp);
						oAuthToken.put(AcademyConstants.STR_TOKEN_EXPIRY_TIMESTAMP, sdf.format(dtTokeExpiryTimeStamp));
						oAuthToken.put(AcademyConstants.ATTR_ACCESS_TOKEN, strToken);
						strDBTokenInp = "<AcadReuseAccessToken ReuseAccessTokenKey='" + strDBTokenKey
								+ "' AccessToken='" + strToken + "' ExpiresOn='" + sdf.format(dtTokeExpiryTimeStamp) + "'/>";
						docDBTokenInp = XMLUtil.getDocument(strDBTokenInp);
						docDBTokenOutput = AcademyUtil.invokeService(env,
								AcademyConstants.ACADEMY_UPDATE_REUSE_ACCESS_TOKEN, docDBTokenInp);
						strAccessToken = oAuthToken.get(AcademyConstants.ATTR_ACCESS_TOKEN);
						logger.verbose(
								"AcademyCOMHUBRestApiUtil.getAccessToken() strAccessToken if dbExpiresTimeStamp expires: "
										+ strAccessToken);

					}
					// Keep latest DB Token details in JVM variables
					else {
						oAuthToken.put(AcademyConstants.STR_TOKEN_EXPIRY_TIMESTAMP, strDBExpDateTimeInJVMTimeZone);
						oAuthToken.put(AcademyConstants.ATTR_ACCESS_TOKEN, strDBAccessToken);
						strAccessToken = oAuthToken.get(AcademyConstants.ATTR_ACCESS_TOKEN);
						logger.verbose(
								"AcademyCOMHUBRestApiUtil.getAccessToken() strAccessToken Keep latest DBTOken to map: "
										+ strAccessToken);
						logger.verbose("Keep latest DB Token details in JVM variables" + oAuthToken);
					}
				} else {
					// starting Call
					logger.verbose("ACADEMY_CREATE_REUSE_ACCESS_TOKEN Called");

					//Retrieving the token from CHUB
					Document docToken = getAccessTokenFromCHub();

					String strToken = docToken.getDocumentElement()
							.getAttribute(AcademyConstants.ATTR_ACCESS_TOKEN);
					String strTokenExpiryTS = docToken.getDocumentElement()
							.getAttribute(AcademyConstants.STR_TOKEN_EXPIRY_IN);

					Calendar calTokenExpiry = Calendar.getInstance();
					calTokenExpiry.setTime(dtCurrentDate);
					calTokenExpiry.add(Calendar.SECOND, Integer.parseInt(strTokenExpiryTS) - iExpiryBufferSecs);
					Date dtTokeExpiryTimeStamp = calTokenExpiry.getTime();

					logger.verbose(
							"AcademyCOMHUBRestApiUtil.getAccessToken() method after adding secs expiresTimeStamp:"
									+ dtTokeExpiryTimeStamp);
					oAuthToken.put(AcademyConstants.STR_TOKEN_EXPIRY_TIMESTAMP, sdf.format(dtTokeExpiryTimeStamp));
					oAuthToken.put(AcademyConstants.ATTR_ACCESS_TOKEN, strToken);
					strDBTokenInp = "<AcadReuseAccessToken AccessToken='" + strToken + "' ExpiresOn='"
							+ sdf.format(dtTokeExpiryTimeStamp) + "' TargetSystem='CHUB'/>";
					docDBTokenInp = XMLUtil.getDocument(strDBTokenInp);
					docDBTokenOutput = AcademyUtil.invokeService(env,
							AcademyConstants.ACADEMY_CREATE_REUSE_ACCESS_TOKEN, docDBTokenInp);
					strAccessToken = oAuthToken.get(AcademyConstants.ATTR_ACCESS_TOKEN);
					logger.verbose(
							"AcademyCOMHUBRestApiUtil.getAccessToken() method AcademyCreateReuseAccessToken Output: "
									+ XMLUtil.getXMLString(docDBTokenOutput));
					strAccessToken = oAuthToken.get(AcademyConstants.ATTR_ACCESS_TOKEN);
					logger.verbose("ACADEMY_CREATE_REUSE_ACCESS_TOKEN--->" + strAccessToken);
				}

			}
		} catch (YFSException yfsEx) {
			throw yfsEx;

		} catch (Exception exe) {
			//Creating info logger for Splunk alerts 
			logger.info("DSV CHUB Token Error ::  Error while tring to retrieve CHub Token:" 
					+ " . Error Trace :: "+exe.getStackTrace());

			YFSException yfsExcep = new YFSException(exe.getMessage());
			yfsExcep.setErrorCode("CHUB_100");
			yfsExcep.setErrorDescription("Error While trying to Retrieve Chub Token ");
			throw yfsExcep;
		}

		logger.endTimer("AcademyComHubRestApiUtil.getAccessToken method ");
		return strAccessToken;

	}

	/**
	 * This method retrieves the dates as per the SDF format
	 * 
	 * @param env
	 * @return iExpiryBufferSecs
	 * @throws Exception
	 */
	public static String getDateInJVMDateTimeStamp(String strDateTimeOffSet, SimpleDateFormat sdf) {
		logger.beginTimer("AcademyComHubRestApiUtil.getDateInJVMDateTimeStamp method ");
		ZonedDateTime serverZoneDateTime = ZonedDateTime.parse(strDateTimeOffSet);
		logger.verbose("JVM TimeZone: " + Calendar.getInstance().getTimeZone().getID());

		ZoneId clientTimeZone = ZoneId.of(Calendar.getInstance().getTimeZone().getID());
		// Convert serverZoneDateTime to JVM DateTime Zone
		ZonedDateTime clientZoneDateTime = serverZoneDateTime.withZoneSameInstant(clientTimeZone);
		String strJVMZoneDateTime = sdf.format(clientZoneDateTime);

		logger.verbose("serverZoneDateTime: " + sdf.format(serverZoneDateTime) + "JVMZoneDateTime: " + strJVMZoneDateTime);
		logger.endTimer("AcademyComHubRestApiUtil.getDateInJVMDateTimeStamp method ");
		return strJVMZoneDateTime;

	}


	/**
	 * This method retrieves the Access Token from COmmerce Hub
	 * 
	 * @param env
	 * @param strGrantType
	 * @param strClientId
	 * @param strClientSecret
	 * @param strUrl
	 * @param strTimeOut
	 * @return Document
	 * @throws Exception
	 */
	private Document getAccessTokenFromCHub() throws Exception {
		logger.beginTimer("AcademyComHubRestApiUtil.getAccessTokenFromCHub method ");
		logger.verbose("getAccessTokenDoc Started");

		HttpsURLConnection httpConn = null;
		BufferedReader bReaderResponse = null;
		Document docOAauthOut = null;
		int iResponseCode = -1;

		try {

			StringBuilder sbAccessTokenInp = new StringBuilder();
			sbAccessTokenInp.append(AcademyConstants.GRANT_TYPE + strGrantType);
			sbAccessTokenInp.append(AcademyConstants.CLIENT_ID + strClientId);
			sbAccessTokenInp.append(AcademyConstants.CLIENT_SECRET + strClientSecret);

			logger.verbose("Http Start");
			byte[] bAccessTokenInput = sbAccessTokenInp.toString().getBytes("UTF-8");
			URL url = new URL(strTokenUrl);

			httpConn = (HttpsURLConnection) url.openConnection();

			//SEtting Timeout
			if (!YFSObject.isVoid(strCreatePOTimeout)) {
				httpConn.setConnectTimeout(Integer.parseInt(strCreatePOTimeout));
				httpConn.setReadTimeout(Integer.parseInt(strCreatePOTimeout));
			}

			httpConn.setRequestMethod(AcademyConstants.WEBSERVICE_POST);
			httpConn.setRequestProperty(AcademyConstants.WEBSERVICE_CONTENT_TYPE, "application/x-www-form-urlencoded");
			httpConn.setRequestProperty(AcademyConstants.STR_CONTENT_LENGTH, "" + bAccessTokenInput.length);
			httpConn.setRequestProperty(AcademyConstants.WEBSERVICE_ACCEPT, AcademyConstants.ACCEPT_APPLICATION_JSON);
			httpConn.setDoOutput(true);

			logger.verbose("Getting OutputStream");
			OutputStream postStream = httpConn.getOutputStream();
			postStream.write(bAccessTokenInput, 0, bAccessTokenInput.length);
			postStream.close();

			iResponseCode = httpConn.getResponseCode();

			String strResponseCode = Integer.toString(httpConn.getResponseCode());
			String strResponseMessage = httpConn.getResponseMessage();
			logger.verbose("Response Message--->" + strResponseMessage + "--Response Code-->" + strResponseCode);


			String strExceptionType = validateChubResponse(strResponseCode, strResponseMessage, "Token");

			//Success Response for Token 
			if (!YFCObject.isVoid(strExceptionType) && "SUCCESS".equals(strExceptionType)) {
				InputStreamReader reader = new InputStreamReader(httpConn.getInputStream());
				bReaderResponse = new BufferedReader(reader);

				StringBuilder sbResponse = new StringBuilder();
				String strResponse = null;
				while ((strResponse = bReaderResponse.readLine()) != null) {
					sbResponse.append(strResponse.trim());
				}

				String json = sbResponse.toString();
				logger.verbose("Json String = " + json);
				json = json.replace("null", "\"\"");
				logger.verbose("JSON from OAuth Call--->" + json);
				docOAauthOut = getXMLFromJSON(json, AcademyConstants.OAUTH_RESPONSE);
				logger.verbose(XMLUtil.getXMLString(docOAauthOut));
			}
			else if(!YFCObject.isVoid(strExceptionType) && ("RETRY".equals(strExceptionType)
					|| "ERROR".equals(strExceptionType))) {
				//Creating info logger for Splunk alerts 
				logger.info("CHub Token Creation :: Error While invoking CHub for Token :: ResponseCode :: " 
						+ strResponseCode + " :: and ResponseMessage :: " + strResponseMessage
						+ " :: and OMS Configured Exception handling is :: " + strExceptionType);

				YFSException yfsTokenExec = new YFSException();
				yfsTokenExec.setErrorCode("CHUB_100");
				yfsTokenExec.setErrorDescription(strExceptionType + " in CHub Token call. Response : " + iResponseCode);
				throw yfsTokenExec;
			}
			else {
				//Creating info logger for Splunk alerts 
				logger.info("CHub Token Creation :: Error While invoking CHub for Token :: ResponseCode :: " 
						+ strResponseCode + " :: and ResponseMessage :: " + strResponseMessage
						+ " :: and Exception Type not in COP :: " + strExceptionType);

				YFSException yfsTokenExec = new YFSException();
				yfsTokenExec.setErrorCode("CHUB_100");
				yfsTokenExec.setErrorDescription("ERROR in CHub Token call. Response : " + iResponseCode);
				throw yfsTokenExec;
			}

		} catch (java.io.IOException e) {
			e.printStackTrace();
			iResponseCode = iResponseCode == -1 ? 500 : iResponseCode;
			//Creating info logger for Splunk alerts 
			logger.info("CHub Token Creation :: IOException Error While invoking CHub for Token :: ResponseCode :: " 
					+ iResponseCode + " :: and ResponseMessage :: " + e.getMessage()
					+ " and Error STack Trace as below \n " + e.getStackTrace());

			YFSException yfsTokenExec = new YFSException();
			yfsTokenExec.setErrorCode("CHUB_100");
			yfsTokenExec.setErrorDescription("IOException Error in CHub Token call. Response : " + iResponseCode);
			throw yfsTokenExec;

		}
		catch (YFSException yfsEx) {
			throw yfsEx;
		}
		catch (Exception e) {
			e.printStackTrace();
			iResponseCode = iResponseCode == -1 ? 500 : iResponseCode;
			//Creating info logger for Splunk alerts 
			logger.info("CHub Token Creation :: Generic Exception Error While invoking CHub for Token :: ResponseCode :: " 
					+ iResponseCode + " :: and ResponseMessage :: " + e.getMessage()
					+ " and Error STack Trace as below \n " + e.getStackTrace());

			YFSException yfsTokenExec = new YFSException();
			yfsTokenExec.setErrorCode("CHUB_100");
			yfsTokenExec.setErrorDescription("Generic Error in CHub Token call. Response : " + iResponseCode);
			throw yfsTokenExec;

		} finally {

			if (bReaderResponse != null)
				bReaderResponse.close();
			if (httpConn != null)
				httpConn.disconnect();
		}
		logger.endTimer("AcademyComHubRestApiUtil.getAccessTokenFromCHub method ");
		return docOAauthOut;
	}

	
	
	/**
	 * This method retrieves the Requests for multiple POs and makes parallel API Calls
	 * 
	 * @param env
	 * @param lRequest
	 * @return Document
	 * @throws Exception
	 */
	public HashMap<String, Document> makeParallelCall(YFSEnvironment env, List<Document> lRequest) throws Exception {
		{
			logger.beginTimer("AcademyComHubRestApiUtil::makeParallelCall");
			logger.verbose("Entering the method AcademyComHubRestApiUtil.makeParallelCall ");

			HashMap<String, Document> hmDocumentOut = new HashMap<String, Document>();
			Document docOutDoc = null;
			Collection<Callable<Document>> tasks = new ArrayList<>();
			String strToken = getAccessToken(env, 3600);

			for (int i=0; i<lRequest.size(); i++) {
				logger.verbose("Inside makeParallelCall LOOP");
				tasks.add(new AcademyPOCancelCallableRequest(env, strToken, lRequest.get(i)));
			}
			
		
			int numThreads = lRequest.size();
			logger.verbose("Begin ExecutorService to run thread pools");
			ExecutorService executor = Executors.newFixedThreadPool(numThreads);
			logger.verbose("ExecutorService finished");
			List<Future<Document>> results=null;
			try {
				logger.verbose("Begin invoking all the tasks");
				results = executor.invokeAll(tasks);
				logger.verbose("End of invoke all");
			} catch (InterruptedException ie) {
				ie.printStackTrace();
				throw ie;
			}
			catch (Exception exp) {
				throw exp;
			}
			
			for(Future<Document> response : results){
				logger.verbose("Inside Future loop");
				try {
					docOutDoc = response.get();
					logger.verbose("Response output document :"+ XMLUtil.getXMLString(docOutDoc));
					String strOrderNo = docOutDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO);
					hmDocumentOut.put(strOrderNo, docOutDoc);

				} catch (InterruptedException | ExecutionException e) {
					logger.verbose("In Exception case of parallel API Call");
					Thread.currentThread().interrupt();
					e.printStackTrace();
				}
			}
			executor.shutdown();
			return hmDocumentOut;
		}
	}
	
	/**
	 * This method is a sample code to validate DSV parallel calls
	 * 
	 * @param env
	 * @param docInput
	 * @return Document
	 * @throws Exception
	 */
	public Document invokeMultiplePOCancel(YFSEnvironment env, Document docInput) throws Exception {	
		logger.verbose("Begin of AcademyComHubRestApiUtil.invokeMultiplePOCancel() method");
		List<Document> lOrders = new ArrayList<Document>();
		HashMap<String, Document> hmOrdersResponse = new HashMap<String, Document>();
		
		logger.verbose("Input to invokeMultiplePOCancel()"+SCXmlUtil.getString(docInput)); 
		logger.verbose("::invokeMultiplePOCancel env() :: getProgId :: " + env.getProgId());

		NodeList nlOrderList = docInput.getElementsByTagName(AcademyConstants.ELE_ORDER);
		int iEleOrderCount = nlOrderList.getLength();
		logger.verbose("NodeList nlORderList Length is : "+iEleOrderCount);
		
		if(iEleOrderCount > 0) {
			for (int iOrderCount = 0; iOrderCount < iEleOrderCount; iOrderCount++) {
				Element eleOrder = (Element) nlOrderList.item(iOrderCount);	
				
				lOrders.add(XMLUtil.getDocumentForElement(eleOrder));
			}
		}	
		
		hmOrdersResponse = makeParallelCall(env, lOrders);
		docInput= formatResponseForMultipleAPI(docInput, hmOrdersResponse);
		
		logger.verbose("End of AcademyComHubRestApiUtil.invokeMultiplePOCancel() method");
		
		return docInput;
	}
	
	
	
	/**
	 * This method is a format the response and update which response are successful
	 * 
	 * @param env
	 * @param docInput
	 * @return Document
	 * @throws Exception
	 */
	public Document formatResponseForMultipleAPI(Document docInput, HashMap<String, Document> hmOrdersResponse) throws Exception {	
		logger.verbose("Begin of AcademyComHubRestApiUtil.formatResponseForMultipleAPI() method");		
		logger.verbose("Input to formatResponseForMultipleAPI()"+SCXmlUtil.getString(docInput)); 
		logger.verbose("Input to lOrdersRespons"+hmOrdersResponse.toString()); 

		NodeList nlOrderList = docInput.getElementsByTagName(AcademyConstants.ELE_ORDER);
		int iEleOrderCount = nlOrderList.getLength();
		logger.verbose("NodeList nlOrderList Length is : "+iEleOrderCount);
		
		if(iEleOrderCount > 0) {
			for (int iOrderCount = 0; iOrderCount < iEleOrderCount; iOrderCount++) {
				Element eleOrder = (Element) nlOrderList.item(iOrderCount);	
				String strOrderNo = eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_NO);
				logger.verbose(" Order contains in HashMap "+SCXmlUtil.getString(hmOrdersResponse.get(strOrderNo)));
				
				eleOrder.setAttribute("Response", ((Document)hmOrdersResponse.get(strOrderNo)).getDocumentElement().getAttribute("Response"));
				eleOrder.setAttribute("ResponseMessage", ((Document)hmOrdersResponse.get(strOrderNo)).getDocumentElement().getAttribute("ResponseMessage"));
			}
		}
		logger.verbose("End of AcademyComHubRestApiUtil.formatResponseForMultipleAPI() method");
		return docInput;
	}
	


}
