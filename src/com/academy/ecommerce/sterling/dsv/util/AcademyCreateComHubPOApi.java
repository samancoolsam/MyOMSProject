package com.academy.ecommerce.sterling.dsv.util;


/**#########################################################################################
 *
 * Project Name                : CHub Migration for DSV
 * Author                      : Everest
 * Author Group				  : DSV
 * Date                        : 09-AUG-2022 
 * Description				  : This class retrieves the Order details and invokes a webservice
 * 								call to CHUB to create a PO 
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
import java.io.OutputStreamWriter;
import java.net.URL;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.TreeSet;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.json.JSONArray;
import org.apache.commons.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;


public class AcademyCreateComHubPOApi implements YIFCustomApi {

	private static YFCLogCategory logger = YFCLogCategory.instance(AcademyCreateComHubPOApi.class);
	private Properties props;

	//CHub PO Create URL
	String strCOMHUBCreatePOURL = YFSSystem.getProperty(AcademyConstants.ACADEMY_COMHUB_REST_CREATEPO_URL);

	AcademyComHubRestApiUtil acadComHubUtil = new AcademyComHubRestApiUtil();
	private static HashMap<String, HashMap<String, String>> hmResponseCode = new  HashMap<String, HashMap<String, String>>();


	/**
	 * This method is invoked as part of order Release and creates a PO in CHub
	 * 
	 * @param env
	 * @return inDoc
	 * @throws Exception
	 */

	public Document invokeCOMHUBCreatePO(YFSEnvironment env, Document inDoc) throws Exception {
		logger.beginTimer("AcademyCreateComHubPOApi.invokeCOMHUBCreatePO method ");
		logger.verbose("Input to invokeCOMHUBCreatePO--***->" + XMLUtil.getXMLString(inDoc));

		String strResponse = null;
		String strMessage = null;

		int iExpiryBufferSecs = Integer.parseInt(props.getProperty(AcademyConstants.STR_TOKEN_EXPIRY_BUFFER_IN_SECS));

		try {

			JSONObject jsonInput = prepareCreatePOJson(env, inDoc);
			
			//Retieve Access Token
			String strToken = acadComHubUtil.getAccessToken(env, iExpiryBufferSecs);
			logger.verbose("strToken1--->" + strToken);

			strMessage = jsonInput.toString(1);
			strResponse = invokeCHubPOCreateRestAPI(env, strMessage, strCOMHUBCreatePOURL, strToken);
		
		}
		catch (YFSException yfsEx) {
			throw yfsEx;
		}
		catch (Exception exe) {
			//Creating info logger for Splunk alerts 
			logger.info("DSV PO Create Error ::  Internal OMS Error for OrderNo::" 
					+ inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO) 
					+ " . Error Trace :: "+exe.getStackTrace());

			YFSException yfsExcep = new YFSException(exe.getMessage());
			yfsExcep.setErrorCode("CHUB_INTERNAL_ERROR");
			yfsExcep.setErrorDescription("Error While trying to Create PO ");
			throw yfsExcep;
		}

		logger.verbose("Response from CreatePO call--->" + strResponse);
		logger.endTimer("AcademyCreateComHubPOApi.invokeCOMHUBCreatePO method ");

		return inDoc;
	}


	/**
	 * This method is creates a webservice for the PO Create REST API call
	 * 
	 * @param env
	 * @return shipmentConfDoc
	 * @throws Exception
	 */
	private JSONObject prepareCreatePOJson(YFSEnvironment env, Document inDoc) throws Exception {
		logger.beginTimer("AcademyCreateComHubPOApi.prepareCreatePOJson method ");
		logger.verbose("Inside prepareCreatePOJson :: " + XMLUtil.getXMLString(inDoc));
		JSONObject jsonRoot = new JSONObject();

		try {

			Element eleDocEle = inDoc.getDocumentElement();

			Element eleOrder = SCXmlUtil.getChildElement(eleDocEle, AcademyConstants.ELE_ORDER);
			NodeList nlOrderLines = eleDocEle.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
			Element elePersonInfoShipTo = SCXmlUtil.getChildElement(eleDocEle, AcademyConstants.ELEM_PERSON_INFO_SHIP_TO);
			Element elePersonInfoBillTo = SCXmlUtil.getChildElement(eleOrder, AcademyConstants.ELE_PERSON_INFO_BILL_TO);
			Element elePackListPriceInfo = SCXmlUtil.getChildElement(eleDocEle, AcademyConstants.PACK_LIST_PRICE_INFO);
			Element eleShipnode = SCXmlUtil.getChildElement(eleDocEle, AcademyConstants.SHIP_NODE1);

			//SEtting header level info in the PO Create Request message
			jsonRoot.put(AcademyConstants.PO_NUMBER, eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_NO));
			jsonRoot.put(AcademyConstants.CONSUMER_ORDER_NUMBER, eleDocEle.getAttribute(AcademyConstants.ATTR_ORDER_NAME));
			jsonRoot.put(AcademyConstants.ORDER_TYPE, AcademyConstants.DROP_SHIP);
			jsonRoot.put(AcademyConstants.DSCO_TRADING_PARTNER_ID, eleDocEle.getAttribute(AcademyConstants.SHIP_NODE));
			jsonRoot.put(AcademyConstants.DSCO_TRADING_PARTNER_NAME,
					eleShipnode.getAttribute(AcademyConstants.ATTR_OWENER_KEY));
			jsonRoot.put(AcademyConstants.NUMBER_OF_LINE_ITEMS, nlOrderLines.getLength());

			//Validating if the SCAC & CarrierServiceCode is valid
			String strSCACSterling = eleDocEle.getAttribute(AcademyConstants.ATTR_SCAC);
			if (StringUtil.isEmpty(strSCACSterling)) {
				//Creating info logger for Splunk alerts 
				logger.info("DSV PO Create Error :: SCAC/CarrierService Validation failed for OrderNo::" 
						+ inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO)
						+ " and SCAC present on the order is :: " + strSCACSterling);
				
				YFSException yfsExeSCACAndService = new YFSException("SCAC/CarrierService Validation failed");
				yfsExeSCACAndService.setErrorCode("CHUB_102");
				yfsExeSCACAndService.setErrorDescription("SCAC/CarrierService Validation failed");
				throw yfsExeSCACAndService;
				
			}
			
			//Validating if the SCAC & CarrierServiceCode is valid
			String strCarrierServiceCodeSterling = eleDocEle.getAttribute(AcademyConstants.CARRIER_SERVICE_CODE);
			
			if (StringUtil.isEmpty(strCarrierServiceCodeSterling)) {
				//Creating info logger for Splunk alerts 
				logger.info("DSV PO Create Error :: SCAC/CarrierService Validation failed for OrderNo::" 
						+ inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO)
						+ " and CarrierServiceCode present on the order is :: " + strCarrierServiceCodeSterling);
				
				YFSException yfsExeSCACAndService = new YFSException("SCAC/CarrierService Validation failed");
				yfsExeSCACAndService.setErrorCode("CHUB_102");
				yfsExeSCACAndService.setErrorDescription("SCAC/CarrierService Validation failed");
				throw yfsExeSCACAndService;
			}
			
			
			//Retrieve the OMS to CHUB Carrier Service Code mapping
			String strSCACChub = getSCACMapping(env, strSCACSterling + " " + strCarrierServiceCodeSterling,inDoc);

			jsonRoot.put(AcademyConstants.SHIPPING_SERVICE_LEVEL_CODE, strSCACChub);

			jsonRoot.put(AcademyConstants.RELEASE_NUMBER, eleDocEle.getAttribute(AcademyConstants.RELEASE_NO));
			jsonRoot.put(AcademyConstants.ORDER_TOTAL_AMOUNT,
					elePackListPriceInfo.getAttribute(AcademyConstants.TOTAL_AMOUNT));
			jsonRoot.put(AcademyConstants.RETAILER_CREATE_DATE, eleDocEle.getAttribute(AcademyConstants.ELE_ORDER_DATE));
			jsonRoot.put(AcademyConstants.SHIP_BY_DATE, eleDocEle.getAttribute(AcademyConstants.REQ_SHIP_DATE));
			
			//Retrieve Initial Promise date present on the Sales ORder
			TreeSet<Date> setOfDates = getExtnInitialPromiseDateSet(env, inDoc);
			
			DateFormat df = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			
			//Setting Dates in the request
			if (!setOfDates.isEmpty()) {
				jsonRoot.put(AcademyConstants.EXPECTED_DELIVERY_DATE, df.format(setOfDates.first()));
				jsonRoot.put(AcademyConstants.REQUIRED_DELIVERY_DATE, df.format(setOfDates.last()));
			}

			//Preparing input for each orderline
			JSONArray lineItems = new JSONArray();
			for (int i = 0; i < nlOrderLines.getLength(); i++) {
				JSONObject lineItem = new JSONObject();
				Element eleOrderLine = (Element) nlOrderLines.item(i);
				lineItem.put(AcademyConstants.JSON_ATTR_QUANTITY,
						eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDERED_QTY));
						
			//Start : OMNI-101708 : Updating Unit Cost from Firestore DB
				String strVendorUnitPrice = XPathUtil.getString(eleOrderLine, 
						"./References/Reference[@Name='VendorUnitCost']/@Value");
				//End : OMNI-101708 : Updating Unit Cost from Firestore DB

				//Retrieving Item and PRice Info
				Element eleItem = SCXmlUtil.getChildElement(eleOrderLine, AcademyConstants.ELEM_ITEM);
				String strItemCost = eleItem.getAttribute(AcademyConstants.ATTR_UNIT_COST);
				
				//Start : OMNI-101708 : Updating Unit Cost from Firestore DB
				if(!YFCObject.isVoid(strVendorUnitPrice))  {
					logger.verbose("Considering the Price from References. " + strVendorUnitPrice);
					lineItem.put(AcademyConstants.EXPECTED_COST, Float.valueOf(strVendorUnitPrice));

				} else {
					lineItem.put(AcademyConstants.EXPECTED_COST, Float.valueOf(strItemCost));
				}
				//End : OMNI-101708 : Updating Unit Cost from Firestore DB
				
				lineItem.put(AcademyConstants.SKU, eleItem.getAttribute(AcademyConstants.ATTR_ITEM_ID));
				lineItem.put(AcademyConstants.PARTNER_SKU, eleItem.getAttribute(AcademyConstants.ATTR_ITEM_ID));
				lineItem.put(AcademyConstants.TITLE, eleItem.getAttribute(AcademyConstants.MANUFACTURER_ITEM));
				Element eleLinePriceInfo = SCXmlUtil.getChildElement(eleOrderLine, AcademyConstants.ELE_LINEPRICE_INFO);
				String strConsumerPrice = eleLinePriceInfo.getAttribute(AcademyConstants.ATTR_UNIT_PRICE);
				lineItem.put(AcademyConstants.CONSUMER_PRICE, Float.valueOf(strConsumerPrice));

				lineItem.put(AcademyConstants.LINE_NUMBER, eleOrderLine.getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO));

				lineItem.put(AcademyConstants.UNITE_OF_MEASURE, AcademyConstants.EA);
				lineItem.put(AcademyConstants.DSCO_TRADING_PARTNER_ID, eleDocEle.getAttribute(AcademyConstants.SHIP_NODE));
				// lineItem.put(AcademyConstants.DSCO_TRADING_PARTNER_NAME,


				JSONArray retailerItemIds = new JSONArray();
				retailerItemIds.add(eleDocEle.getAttribute(AcademyConstants.ATTR_RELEASE_KEY));
				lineItem.put(AcademyConstants.RETAILER_ITEM_IDS, retailerItemIds);

				lineItems.add(lineItem);
			}
			jsonRoot.put(AcademyConstants.LINE_ITEMS, lineItems);

			//Updating the Shipping and Billing Info
			JSONObject shipping = new JSONObject();

			shipping.put(AcademyConstants.FIRST_NAME, elePersonInfoShipTo.getAttribute(AcademyConstants.ATTR_FNAME));
			shipping.put(AcademyConstants.LAST_NAME2, elePersonInfoShipTo.getAttribute(AcademyConstants.ATTR_LNAME));
			shipping.put(AcademyConstants.NAME, elePersonInfoShipTo.getAttribute(AcademyConstants.ATTR_FNAME) + " "
					+ elePersonInfoShipTo.getAttribute(AcademyConstants.ATTR_LNAME));
			shipping.put(AcademyConstants.EMAIL, elePersonInfoShipTo.getAttribute(AcademyConstants.ATTR_EMAILID));
			shipping.put(AcademyConstants.COMPANY, elePersonInfoShipTo.getAttribute(AcademyConstants.ATTR_COMPANY));
			shipping.put(AcademyConstants.ADDRESS1, elePersonInfoShipTo.getAttribute(AcademyConstants.ATTR_ADDRESS_LINE_1));
			shipping.put(AcademyConstants.ADDRESS2, elePersonInfoShipTo.getAttribute(AcademyConstants.ATTR_ADDRESS_LINE_2));
			shipping.put(AcademyConstants.JSON_CITY, elePersonInfoShipTo.getAttribute(AcademyConstants.ATTR_CITY));
			shipping.put(AcademyConstants.REGION, elePersonInfoShipTo.getAttribute(AcademyConstants.ATTR_STATE));
			// shipping.put(AcademyConstants.JSON_STATE,

			shipping.put(AcademyConstants.POSTAL, elePersonInfoShipTo.getAttribute(AcademyConstants.ZIP_CODE));
			shipping.put(AcademyConstants.PHONE, elePersonInfoShipTo.getAttribute(AcademyConstants.ATTR_DAY_PHONE));
			shipping.put(AcademyConstants.JSON_COUNTRY, elePersonInfoShipTo.getAttribute(AcademyConstants.COUNTRY));
			shipping.put(AcademyConstants.ADDRESS_TYPE, AcademyConstants.RESIDENTIAL);

			JSONObject billTo = new JSONObject();

			billTo.put(AcademyConstants.FIRST_NAME, elePersonInfoBillTo.getAttribute(AcademyConstants.ATTR_FNAME));
			billTo.put(AcademyConstants.LAST_NAME2, elePersonInfoBillTo.getAttribute(AcademyConstants.ATTR_LNAME));
			//Start : OMNI-105511 : Lispey SOF Orders missing Last Name
			billTo.put(AcademyConstants.NAME, elePersonInfoBillTo.getAttribute(AcademyConstants.ATTR_FNAME) + " "
					+ elePersonInfoBillTo.getAttribute(AcademyConstants.ATTR_LNAME));
			//End : OMNI-105511 : Lispey SOF Orders missing Last Name
			billTo.put(AcademyConstants.EMAIL, elePersonInfoBillTo.getAttribute(AcademyConstants.ATTR_EMAILID));
			billTo.put(AcademyConstants.COMPANY, elePersonInfoBillTo.getAttribute(AcademyConstants.ATTR_COMPANY));
			billTo.put(AcademyConstants.ADDRESS1, elePersonInfoBillTo.getAttribute(AcademyConstants.ATTR_ADDRESS_LINE_1));
			billTo.put(AcademyConstants.ADDRESS2, elePersonInfoBillTo.getAttribute(AcademyConstants.ATTR_ADDRESS_LINE_2));
			billTo.put(AcademyConstants.JSON_CITY, elePersonInfoBillTo.getAttribute(AcademyConstants.ATTR_CITY));
			billTo.put(AcademyConstants.REGION, elePersonInfoBillTo.getAttribute(AcademyConstants.ATTR_STATE));

			billTo.put(AcademyConstants.POSTAL, elePersonInfoBillTo.getAttribute(AcademyConstants.ZIP_CODE));
			billTo.put(AcademyConstants.PHONE, elePersonInfoBillTo.getAttribute(AcademyConstants.ATTR_DAY_PHONE));
			billTo.put(AcademyConstants.JSON_COUNTRY, elePersonInfoBillTo.getAttribute(AcademyConstants.COUNTRY));
			billTo.put(AcademyConstants.ADDRESS_TYPE, AcademyConstants.RESIDENTIAL);

			JSONArray address = new JSONArray();
			address.add(elePersonInfoShipTo.getAttribute(AcademyConstants.ATTR_ADDRESS_LINE_1));

			shipping.put(AcademyConstants.JSON_ATTR_ADDRESS, address);

			JSONArray addressBill = new JSONArray();
			addressBill.add(elePersonInfoBillTo.getAttribute(AcademyConstants.ATTR_ADDRESS_LINE_1));

			billTo.put(AcademyConstants.JSON_ATTR_ADDRESS, addressBill);

			jsonRoot.put(AcademyConstants.SHIPPING, shipping);
			jsonRoot.put(AcademyConstants.BILLTO, billTo);

			logger.verbose("Final JSON to CHUB--->" + jsonRoot.toString());			
		}
		catch (YFSException yfsEx) {
			throw yfsEx;
		}
		catch (Exception exp) {
			//Creating info logger for Splunk alerts 
			logger.info("DSV PO Create Error :: Exception while preparing the input ::" 
					+ inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO)
					+ " . Error Trace :: "+exp.getStackTrace());

			YFSException yfsExecPOCreate = new YFSException();
			yfsExecPOCreate.setErrorCode("CHUB_INTERNAL_ERROR");
			yfsExecPOCreate.setErrorDescription("Error while trying to create the PO :: " + exp.getMessage());
			throw yfsExecPOCreate;

		}
		logger.endTimer("AcademyCreateComHubPOApi.prepareCreatePOJson method ");
		return jsonRoot;
		
	}

	/**
	 * This method is validates the SCAC MApping for OMS vs CHub
	 * 
	 * @param env
	 * @return shipmentConfDoc
	 * @throws Exception
	 */
	private String getSCACMapping(YFSEnvironment env, String strSCAC, Document inDoc) throws Exception {
		logger.beginTimer("AcademyCreateComHubPOApi.getSCACMapping() method");
		logger.verbose(" :: strSCAC--->" + strSCAC);

		String strChubSCAC = null;
		
		//Prepare CommonCode Input
		Document docGetCommonCodeListInput = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
		docGetCommonCodeListInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE,
				AcademyConstants.STR_TO_CHUB_SCAC_MAP);
		docGetCommonCodeListInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE, strSCAC);
		
		//Invoking the CommonCode API
		Document outXML1 = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMONCODE_LIST,
				docGetCommonCodeListInput);
		
		//Validating the common code response
		Element eleDocEle = outXML1.getDocumentElement();
		NodeList nlCommonCode = eleDocEle.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);
		Element eleComonCode = (Element) nlCommonCode.item(0);
		
		if (!YFCObject.isVoid(eleComonCode)) {
			strChubSCAC = eleComonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
			
			}
		if (StringUtil.isEmpty(strChubSCAC)) {
			//Creating info logger for Splunk alerts 
			logger.info("DSV PO Create Error :: SCAC/CarrierService Validation failed for OrderNo::" 
			+ inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO));
			
			YFSException yfsExeSCACAndService = new YFSException("SCAC/CarrierService Validation failed");
			yfsExeSCACAndService.setErrorCode("CHUB_102");
			yfsExeSCACAndService.setErrorDescription("SCAC/CarrierService Validation failed");
			throw yfsExeSCACAndService;
			
		}
		
		logger.verbose("strChubSCAC--->" + strChubSCAC);
		logger.endTimer("AcademyCreateComHubPOApi.getSCACMapping() method");
		return strChubSCAC;
	}
	
	/**
	 * This method retrieves the Initial promise date from corresponding Sales Order
	 * 
	 * @param env
	 * @return inDoc
	 * @throws Exception
	 */
	private TreeSet<Date> getExtnInitialPromiseDateSet(YFSEnvironment env, Document inDoc) throws Exception {

		logger.beginTimer("AcademyCreateComHubPOApi.getExtnInitialPromiseDateSet() method");

		TreeSet<Date> hsDateSet = new TreeSet<Date>();
		Element eleOrderLine = (Element) inDoc.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE).item(0);
		String strChainedFromOrderHeaderKey = eleOrderLine
				.getAttribute(AcademyConstants.ATTR_CHAINED_FROM_ORDER_HEADER_KEY);
		
		Document docGetOrderListOut = invokeGetOrderList(env, strChainedFromOrderHeaderKey);

		Document docOrder = XMLUtil.getDocumentForElement(
				SCXmlUtil.getChildElement(docGetOrderListOut.getDocumentElement(), AcademyConstants.ELE_ORDER));
		NodeList nlOrderLine = docOrder.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
		int iLen = nlOrderLine.getLength();
		
		//Navigate through each order line and fetch the initial promise date
		for (int i = 0; i < iLen; i++) {
			Element eleOrderLine1 = (Element) nlOrderLine.item(i);
			
			/* The code below will consider only dropship lines for promise date calculation */
			// Start - OMNI-101851
			String strFulfillmentType = eleOrderLine1.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
			if (!StringUtil.isEmpty(strFulfillmentType)
					&& strFulfillmentType.equalsIgnoreCase(AcademyConstants.STR_DROP_SHIP)) {
			Element eleExtn = SCXmlUtil.getChildElement(eleOrderLine1, AcademyConstants.ELE_EXTN);
			String strInitialPromiseDate = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_INITIAL_PROMISE_DATE);
			if (!StringUtil.isEmpty(strInitialPromiseDate)) {
				SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);

				hsDateSet.add(sdf.parse(strInitialPromiseDate));
			}
		  }
		  // End - OMNI-101851
		}
		logger.verbose("hsDateSet" + hsDateSet.toString());
		logger.endTimer("AcademyCreateComHubPOApi.getExtnInitialPromiseDateSet() method");
		return hsDateSet;
	}

	/**
	 * This method invokes getOrderList API
	 * 
	 * @param env
	 * @return inDoc
	 * @throws Exception
	 */
	private Document invokeGetOrderList(YFSEnvironment env, String strChainedFromOrderHeaderKey) throws Exception {

		logger.beginTimer("AcademyCreateComHubPOApi.invokeGetOrderList() method");
		logger.verbose(" invokeGetOrderList() :: " + strChainedFromOrderHeaderKey);

		Document docGetOrdListOut = null;
		Document docGetOrderListTemplate = null;

		//Preparing the input for getOrderList
		Document docGetOrdListInp = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		docGetOrdListInp.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,
				strChainedFromOrderHeaderKey);
		logger.verbose("input - getOrderList :: " + XMLUtil.getXMLString(docGetOrdListInp));

		docGetOrderListTemplate = XMLUtil.getDocument(
				"<OrderList>\r\n<Order OrderNo=\"\" >\r\n<OrderLines>\r\n\r\n<OrderLine PrimeLineNo=\"\" FulfillmentType=\"\" >\r\n<Extn ExtnInitialPromiseDate=\"\"/>\r"
				+ "\n</OrderLine >\r\n\r\n</OrderLines>\r\n\r\n</Order>\r\n</OrderList>");

		//Invoke getOrderList API
		env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, docGetOrderListTemplate);
		docGetOrdListOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, docGetOrdListInp);
		env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);

		logger.verbose("output - getOrderList :: " + XMLUtil.getXMLString(docGetOrdListOut));
		logger.endTimer("AcademyCreateComHubPOApi.invokeGetOrderList() method");

		return docGetOrdListOut;
	}
	
	
	/**
	 * This method makes a webserivce call to CHub for PO Create
	 * 
	 * @param listProp
	 * @param strPropName
	 */
	private String invokeCHubPOCreateRestAPI(YFSEnvironment env, String strRequest, String strPOCreateUrl, String strAuthToken)
			throws Exception {
		logger.beginTimer("AcademyCreateComHubPOApi.invokeCHubPOCreateRestAPI method ");
		logger.verbose("invokeCOMHUBCreatePORestService Started");
		int iResponseCode = -1;
		String strResponse = null;
		BufferedReader bReaderResponse = null;

		try {

			URL url = new URL(strPOCreateUrl);
			HttpsURLConnection httpConn = (HttpsURLConnection) url.openConnection();
			httpConn.setRequestMethod(AcademyConstants.WEBSERVICE_POST);
			httpConn.setRequestProperty(AcademyConstants.WEBSERVICE_CONTENT_TYPE, AcademyConstants.ACCEPT_APPLICATION_JSON);
			httpConn.setRequestProperty(AcademyConstants.WEBSERVICE_ACCEPT, AcademyConstants.ACCEPT_APPLICATION_JSON);
			httpConn.setDoOutput(true);

			httpConn.setRequestProperty(AcademyConstants.WEBSERVICE_AUTHORIZATION, AcademyConstants.STR_BEARER_SPACE + strAuthToken);
			
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

			Document docExportDoc = acadComHubUtil.getXMLFromJSON(strRequest, "ChubJson");
			AcademyUtil.invokeService(env, "ExportChubJSONInDB", docExportDoc);

			String strResponseCode = Integer.toString(httpConn.getResponseCode());
			String strResponseMessage = httpConn.getResponseMessage();
			logger.verbose("Response Message--->" + strResponseMessage + "--Response Code-->" + strResponseCode);

			String strExceptionType = acadComHubUtil.validateChubResponse(strResponseCode, strResponseMessage, AcademyConstants.PO_CREATE);

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
				
			}
			else if(!YFCObject.isVoid(strExceptionType) && ("RETRY".equals(strExceptionType)
					|| "ERROR".equals(strExceptionType))) {
				//Creating info logger for Splunk alerts 
				logger.info("CHub PO Creation Error :: Error While trying to create PO :: ResponseCode :: " 
						+ strResponseCode + " :: and ResponseMessage :: " + strResponseMessage
						+ " :: and OMS Configured Exception handling is :: " + strExceptionType);
								
				BufferedReader br = new BufferedReader(new InputStreamReader(httpConn.getErrorStream(), "utf-8"));
				StringBuilder responseOut = new StringBuilder();
				String responseLine = null;
				
				while ((responseLine = br.readLine()) != null) {
					responseOut.append(responseLine.trim());
				}

				strResponse = responseOut.toString();
				logger.info(":: strRequest  = " + strRequest);
				logger.info(":: strResponse Error String = " + strResponse);
				
				YFSException yfsTokenExec = new YFSException();
				yfsTokenExec.setErrorCode("CHUB_101");
				yfsTokenExec.setErrorDescription(strExceptionType + " in CHub PO Create call. Response : " + strResponseCode + " : Message :" + strResponseMessage);
				throw yfsTokenExec;
			}
			else {
				//Creating info logger for Splunk alerts 
				logger.info("CHub PO Creation Error :: Error While trying to create PO :: ResponseCode :: " 
						+ strResponseCode + " :: and ResponseMessage :: " + strResponseMessage
						+ " :: and Exception Type not in COP :: " + strExceptionType);

				YFSException yfsTokenExec = new YFSException();
				yfsTokenExec.setErrorCode("CHUB_101");
				yfsTokenExec.setErrorDescription("ERROR in CHub PO Create call. Response : " + iResponseCode);
				throw yfsTokenExec;
			}
		

		} catch (java.io.IOException e) {
			e.printStackTrace();
			iResponseCode = iResponseCode == -1 ? 500 : iResponseCode;
			//Creating info logger for Splunk alerts 
			logger.info("CHub PO Creation Error :: IOException Error While trying to create PO :: ResponseCode :: " 
					+ iResponseCode + " :: and ResponseMessage :: " + e.getMessage()
					+ " and Error STack Trace as below \n " + e.getStackTrace().toString());
			logger.verbose(":: Error  " + e.getLocalizedMessage());
			logger.verbose(":: Error  " + e.getMessage());
			e.printStackTrace();
			
			YFSException yfsTokenExec = new YFSException();
			yfsTokenExec.setErrorCode("CHUB_101");
			yfsTokenExec.setErrorDescription("IOException Error in CHub PO Create call. Response : " + iResponseCode);
			throw yfsTokenExec;

		}
		catch (YFSException yfsEx) {
			throw yfsEx;
		}
		catch (Exception e) {
			e.printStackTrace();
			iResponseCode = iResponseCode == -1 ? 500 : iResponseCode;
			//Creating info logger for Splunk alerts 
			logger.info("CHub PO Creation Error :: Generic Error While trying to create PO :: ResponseCode :: " 
					+ iResponseCode + " :: and ResponseMessage :: " + e.getMessage()
					+ " and Error STack Trace as below \n " + e.getStackTrace());

			YFSException yfsTokenExec = new YFSException();
			yfsTokenExec.setErrorCode("CHUB_101");
			yfsTokenExec.setErrorDescription("Generic Error in CHub PO Create call. Response : " + iResponseCode);
			throw yfsTokenExec;

		} 

		logger.verbose("Output from invokeCOMHUBCreatePORestService-->" + strResponse);
		logger.endTimer("AcademyCreateComHubPOApi.invokeCHubPOCreateRestAPI method ");
		return strResponse;
	}

	@Override
	public void setProperties(Properties props) throws Exception {
		this.props = props;

	}

}
