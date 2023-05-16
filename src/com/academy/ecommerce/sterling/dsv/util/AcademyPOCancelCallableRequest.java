package com.academy.ecommerce.sterling.dsv.util;

/**#########################################################################################
 *
 * Project Name                : DSV Phase 2 - OMS to CHUB Cancellations
 * Author                      : Everest
 * Author Group				   : DSV
 * Date                        : 06-FEB-2023 
 * Description				   : This is a util class handles PO cancellation to CHUB
 * 								 
 * ---------------------------------------------------------------------------------
 * Date            	Author         			Version#       		Remarks/Description                      
 * ---------------------------------------------------------------------------------
 * 06-FEB-2023		Everest  	 			  1.0           	Initial version
 * 19-SPR-2023		Everest  	 			  1.1           	Updated the new PO Cancel URL
 *
 * #########################################################################################*/


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.concurrent.Callable;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.json.JSONArray;
import org.apache.commons.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyPOCancelCallableRequest implements Callable<Document> {
	
	private static YFCLogCategory logger = YFCLogCategory.instance(AcademyPOCancelCallableRequest.class);

	private Document inpDocument;
	private YFSEnvironment yfsEnv;
	private String strAuthToken;
	//CHub PO Create URL
    String strCOMHUBCancelPOURL = YFSSystem.getProperty(AcademyConstants.ACADEMY_COMHUB_REST_CANCEL_PO_URL);
    //Start : OMNI-108360 : Changes to PO Cancel API
    String strCancelRequestType = YFSSystem.getProperty(AcademyConstants.ACADEMY_COMHUB_REST_CANCEL_PO_REQUEST_TYPE);
    //End : OMNI-108360 : Changes to PO Cancel API

	AcademyComHubRestApiUtil acadComHubUtil = new AcademyComHubRestApiUtil();

	/**
	 * This method is a contructor called for each PO in the parallel API call
	 * for the CHUB related cancellations
	 * 
	 * @param env
	 * @param strToken
	 * @param inDoc
	 */
	public AcademyPOCancelCallableRequest(YFSEnvironment env, String strToken, Document inDoc) {
		this.inpDocument = inDoc;
		this.yfsEnv = env;
		this.strAuthToken = strToken;
	}
	
	/**
	 * This method is the main method which will make the request and provide the response
	 * for the CHUB related cancellations
	 * 
	 * @param env
	 */
	public Document call() throws Exception {
		logger.beginTimer("AcademyPOCancelCallableRequest.call method ");
		logger.verbose("preparePOCancelAPIRequest Inside Call");
		Document docOutput = XMLUtil.createDocument(AcademyConstants.ELE_RESPONSE);
		String strOrderNo = inpDocument.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO);
		Element eleResponse = docOutput.getDocumentElement();
		eleResponse.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
		
		try {
			String strRequest = preparePOCancelAPIRequest(inpDocument, strCOMHUBCancelPOURL).toString();
			logger.verbose("invokePOCancelRestAPI Inside Call");
			//Start : OMNI-108360 : Changes to PO Cancel API
			docOutput = invokePOCancelRestAPI(yfsEnv, strOrderNo, strRequest, strCOMHUBCancelPOURL, strCancelRequestType, strAuthToken);
			//End : OMNI-108360 : Changes to PO Cancel API

		}
		catch (YFSException yfsx) {
			throw yfsx;
		}
		catch (Exception exp) {
			eleResponse.setAttribute(AcademyConstants.ATTR_RESPONSE, AcademyConstants.STATUS_CODE_ERROR);
			eleResponse.setAttribute(AcademyConstants.ATTR_RESPONSE_MESSAGE, "Error in the Invocation during call()");
		}

		logger.verbose("Call Method Ended");
		logger.endTimer("AcademyPOCancelCallableRequest.call method ");
		return docOutput;
	}	


	/* This method is creates a webservice for the PO Cancel REST API call
	 * For PO Item Cancel Request is : {"id": "Y102325478","type": "PO_NUMBER",
	 * "lineItems": [{"partnerSku":"0112255878","cancelledQuantity": 0,"cancelledReason": "Customer Cancel",
 	 * "cancelCode": "903","lineNumber":1}]}
 	 * 
 	 * For PO Order Cancel Request is : {"poNumber": "Y102325478","cancelCode": "903","reason": "Customer Cancel",
 	 * "lineItems": [{"partnerSku":"0112255878","cancelledQuantity": 0,"cancelledReason": "Customer Cancel",
 	 * "cancelCode": "903","lineNumber":1}]}
	 * 
	 * @param env
	 * @return shipmentConfDoc
	 * @throws Exception
	 */
	private JSONObject preparePOCancelAPIRequest(Document inDoc, String strCOMHUBCancelPOURL) throws Exception {
		logger.beginTimer("AcademyPOCancelCallableRequest.preparePOCancelAPIRequest method ");
		logger.info("Inside preparePOCancelAPIRequest :: " + XMLUtil.getXMLString(inDoc));
		JSONObject jsonRoot = new JSONObject();

		try {
			Element eleOrder = inDoc.getDocumentElement();
			//Start : OMNI-108360 : Changes to PO Cancel API
			boolean bIsOrderRequestCancel = true;
			if(strCOMHUBCancelPOURL.contains("item")) {
				bIsOrderRequestCancel = false;
			}
			//Setting header level verbose in the PO Create Request message
			if(bIsOrderRequestCancel) {
				jsonRoot.put(AcademyConstants.PO_NUMBER, eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_NO));
				jsonRoot.put(AcademyConstants.CANCELLED_CODE, AcademyConstants.CANCEL_REASON_CODE);		
				jsonRoot.put(AcademyConstants.CANCELLED_REASON, AcademyConstants.CANCEL_REASON_TEXT);				
			}
			else {
				jsonRoot.put("id", eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_NO));
				jsonRoot.put("type", AcademyConstants.TYPE_PO_NUMBER);		
		
			}
			//Start : OMNI-108360  : Changes to PO Cancel API
			NodeList nlOrderLines = eleOrder.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);

			//Preparing input for each orderline
			JSONArray lineItems = new JSONArray();

			for (int i = 0; i < nlOrderLines.getLength(); i++) {
				JSONObject lineItem = new JSONObject();
				Element eleOrderLine = (Element) nlOrderLines.item(i);
				lineItem.put(AcademyConstants.PARTNER_SKU, eleOrderLine.getAttribute(AcademyConstants.ATTR_ITEM_ID));
				lineItem.put(AcademyConstants.CANCELLED_QUANTITY,
						(int)(Double.parseDouble(eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDERED_QTY))));
				lineItem.put(AcademyConstants.LINE_NUMBER, eleOrderLine.getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO));
				lineItem.put(AcademyConstants.CANCELLED_REASON,AcademyConstants.CANCEL_REASON_TEXT);
				lineItem.put(AcademyConstants.CANCELLED_CODE,AcademyConstants.CANCEL_REASON_CODE);
				lineItems.add(lineItem);
			}
			jsonRoot.put(AcademyConstants.LINE_ITEMS, lineItems);
			//End : OMNI-108360  : Changes to PO Cancel API

			logger.info("Final JSON to CHUB--->" + jsonRoot.toString());			
		}
		catch (YFSException yfsEx) {
			throw yfsEx;
		}
		catch (Exception exp) {
			//Creating verbose logger for Splunk alerts 
			logger.info("DSV PO Cancel Error :: Exception while preparing the input ::" 
					+ inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO)
					+ " . Error Trace :: "+exp.getStackTrace());

			YFSException yfsExecPOCreate = new YFSException();
			yfsExecPOCreate.setErrorCode("CHUB_INTERNAL_ERROR");
			yfsExecPOCreate.setErrorDescription("Error while trying to Cancel the PO :: " + exp.getMessage());
			exp.printStackTrace();
			throw exp;

		}
		logger.endTimer("AcademyPOCancelCallableRequest.prepareCreatePOJson method ");
		return jsonRoot;
		
	}
	 
	 
	/**
	 * This method makes a webserivce call to CHub for PO Cancel and prepares the webserivce response
	 * to contain the retry or exception errors based on the values in common code
	 * 
	 * @param listProp
	 * @param strPropName
	 */
	private Document invokePOCancelRestAPI(YFSEnvironment env,String strOrderNo, String strRequest, String strURL, String strCancelRequestType, String strToken)
			throws Exception {
		logger.beginTimer("AcademyPOCancelCallableRequest.invokePOCancelRestAPI method ");
		logger.verbose("invokePOCancelRestAPI Started");
		int iResponseCode = -1;
		String strResponse = null;
		BufferedReader bReaderResponse = null;
		Document docResponse = XMLUtil.createDocument(AcademyConstants.ELE_RESPONSE);
		Element eleResponse = docResponse.getDocumentElement();
		eleResponse.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);

		try {

			URL url = new URL(strURL);
			HttpsURLConnection httpConn = (HttpsURLConnection) url.openConnection();
			
			//Start : OMNI-108360 : Changes to PO Cancel API
			//httpConn.setRequestMethod(AcademyConstants.WEBSERVICE_POST);
			httpConn.setRequestMethod(strCancelRequestType);
			//End : OMNI-108360 : Changes to PO Cancel API

			httpConn.setRequestProperty(AcademyConstants.WEBSERVICE_CONTENT_TYPE, AcademyConstants.ACCEPT_APPLICATION_JSON);
			httpConn.setRequestProperty(AcademyConstants.WEBSERVICE_ACCEPT, AcademyConstants.ACCEPT_APPLICATION_JSON);
			httpConn.setDoOutput(true);

			httpConn.setRequestProperty(AcademyConstants.WEBSERVICE_AUTHORIZATION, AcademyConstants.STR_BEARER_SPACE + strToken);
			
			//SEtting Timeout
			String strCreatePOTimeout = YFSSystem.getProperty(AcademyConstants.ACADEMY_COMHUB_REST_CREATEPO_TIMEOUT);
			if (!YFSObject.isVoid(strCreatePOTimeout)) {
				httpConn.setConnectTimeout(Integer.parseInt(strCreatePOTimeout));
				httpConn.setReadTimeout(Integer.parseInt(strCreatePOTimeout));
			}
			
			httpConn.setDoInput(true);
			httpConn.setDoOutput(true);
			httpConn.setUseCaches(false);
			httpConn.setAllowUserInteraction(false);
			httpConn.setRequestProperty("charset", "UTF-8");
			OutputStream outStream = httpConn.getOutputStream();
			OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream, "UTF-8");
			outStreamWriter.write(strRequest);
			outStreamWriter.flush();
			outStreamWriter.close();
			outStream.close();

			String strResponseCode = Integer.toString(httpConn.getResponseCode());
			String strResponseMessage = httpConn.getResponseMessage();
			logger.verbose("Response Message--->" + strResponseMessage + "--Response Code-->" + strResponseCode);

			String strExceptionType = acadComHubUtil.validateChubResponse(strResponseCode, strResponseMessage, "POCancel");

			//Success REsponse for Token 
			if (!YFCObject.isVoid(strExceptionType) && "SUCCESS".equals(strExceptionType)) {
				InputStreamReader reader = new InputStreamReader(httpConn.getInputStream());
				bReaderResponse = new BufferedReader(reader);

				StringBuilder sbResponse = new StringBuilder();
				while ((strResponse = bReaderResponse.readLine()) != null) {
					sbResponse.append(strResponse.trim());
				}

				strResponse = sbResponse.toString();
				logger.verbose("strResponse String = " + strResponse);
				eleResponse.setAttribute(AcademyConstants.ATTR_RESPONSE, strExceptionType);
				eleResponse.setAttribute(AcademyConstants.ATTR_RESPONSE_MESSAGE, strResponse);
				
			}
			else if(!YFCObject.isVoid(strExceptionType) && ("RETRY".equals(strExceptionType)
					|| AcademyConstants.STATUS_CODE_ERROR.equals(strExceptionType))) {
				//Creating verbose logger for Splunk alerts 
				logger.info("CHub PO Cancel Error :: Error While trying to Cancel PO RETRY/EXCEPTION :: ResponseCode :: " 
						+ strResponseCode + AcademyConstants.CHUB_RESPONSE_MSG + strResponseMessage
						+ " :: and OMS Configured Exception handling is :: " + strExceptionType);
				
				//Start : OMNI-108362 : Handling 201 response for new Cancel API
				BufferedReader br;
				
				if(httpConn.getErrorStream() != null) {
					br = new BufferedReader(new InputStreamReader(httpConn.getErrorStream(), "utf-8"));
				}
				else {
					br = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "utf-8"));
				}
				//End : OMNI-108362 : Handling 201 response for new Cancel API

				StringBuilder responseOut = new StringBuilder();
				String responseLine = null;
				
				while ((responseLine = br.readLine()) != null) {
					responseOut.append(responseLine.trim());
				}

				strResponse = responseOut.toString();
				logger.info(":: strRequest  = " + strRequest + ":: strResponse Error String = " + strResponse);
				
				strResponse = strResponse + strExceptionType + " in CHub PO Cancel call. Response : " + strResponseCode + " : Message :" + strResponseMessage;
				eleResponse.setAttribute(AcademyConstants.ATTR_RESPONSE, strExceptionType);
				eleResponse.setAttribute(AcademyConstants.ATTR_RESPONSE_MESSAGE, strResponse);
			}
			else {
				//Creating verbose logger for Splunk alerts 
				logger.info("CHub PO Cancel Error :: Error While trying to Cancel PO :: ResponseCode :: " 
						+ strResponseCode + AcademyConstants.CHUB_RESPONSE_MSG  + strResponseMessage
						+ " :: and Exception Type not in COP :: " + strExceptionType);
				logger.info(":: strRequest  = " + strRequest + ":: strResponse Error String = " + strResponse);

				strResponse = strResponse + "ERROR in CHub PO Cancel call. Response : " + iResponseCode;
				eleResponse.setAttribute(AcademyConstants.ATTR_RESPONSE, strExceptionType);
				eleResponse.setAttribute(AcademyConstants.ATTR_RESPONSE_MESSAGE, strResponse);
			}

		} catch (java.io.IOException e) {
			e.printStackTrace();
			iResponseCode = iResponseCode == -1 ? 500 : iResponseCode;
			//Creating verbose logger for Splunk alerts 
			logger.info("CHub PO Cancel Error :: IOException Error While trying to cancel PO :: ResponseCode :: " 
					+ iResponseCode + " :: and ResponseMessage :: " + e.getMessage()
					+ " and Error STack Trace as below \n " + e.getStackTrace());
			logger.info(":: strRequest  = " + strRequest + ":: strResponse Error String = " + strResponse);
			
			strResponse = strResponse + "IOException in CHub PO Cancel call. Response : " + iResponseCode;
			eleResponse.setAttribute(AcademyConstants.ATTR_RESPONSE, AcademyConstants.STATUS_CODE_ERROR);
			eleResponse.setAttribute(AcademyConstants.ATTR_RESPONSE_MESSAGE, strResponse);

		}
		catch (YFSException yfsEx) {
			throw yfsEx;
		}
		catch (Exception e) {
			e.printStackTrace();
			iResponseCode = iResponseCode == -1 ? 500 : iResponseCode;
			//Creating verbose logger for Splunk alerts 
			logger.info("CHub PO Cancel Error :: Generic Error While trying to cancel PO :: ResponseCode :: " 
					+ iResponseCode + " :: and ResponseMessage :: " + e.getMessage()
					+ " and Error STack Trace as below \n " + e.getStackTrace());
			logger.info(":: strRequest  = " + strRequest + ":: strResponse Error String = " + strResponse);

			strResponse = strResponse + "Generic in CHub PO Cancel call. Response : " + iResponseCode;
			eleResponse.setAttribute(AcademyConstants.ATTR_RESPONSE, AcademyConstants.STATUS_CODE_ERROR);
			eleResponse.setAttribute(AcademyConstants.ATTR_RESPONSE_MESSAGE, strResponse);
		} 

		logger.verbose("Output from invokePOCancelRestAPI-->" + strResponse);
		logger.endTimer("AcademyPOCancelCallableRequest.invokePOCancelRestAPI method ");
		return docResponse;
	}
	
	
}
