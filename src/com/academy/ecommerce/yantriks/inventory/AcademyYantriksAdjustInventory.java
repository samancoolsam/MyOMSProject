package com.academy.ecommerce.yantriks.inventory;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.json.JSONArray;
import org.apache.commons.json.JSONException;
import org.apache.commons.json.JSONObject;


import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantriks.yih.adapter.util.YantriksCommonUtil;
import com.yantriks.yih.adapter.util.YantriksConstants;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;


public class AcademyYantriksAdjustInventory {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyYantriksAdjustInventory.class);

	/*
	<Items IgnoreOrdering="Y">
	    <Item AdjustmentType="ADJUSTMENT" ETA="" ItemID="000007583"
	      OrganizationCode="DEFAULT" ProductClass="GOOD" ReasonCode=""
	      ReasonText="" ShipNode="033" UnitOfMeasure="EACH"/>
	</Items> 
	*/
		
	/**
	 * This method Prepares the JSON input and invokes the web service.
	 * 
	 * @param env
	 * @param inDoc
	 * @return Document
	 * @throws Exception
	 */
	
	public Document adjustInventory(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer("AcademyYantriksAdjustInventory::adjustInventory");
		log.debug("AcademyYantriksAdjustInventory.adjustInventory() Input XML ::"+ XMLUtil.getXMLString(inDoc));
		String strLocationType=null;
		Element eleItems = inDoc.getDocumentElement();
		Element eleItem=(Element)eleItems.getElementsByTagName(YantriksConstants.E_ITEM).item(0);
		String strShipNode=eleItem.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
		String strProductId=eleItem.getAttribute(AcademyConstants.ITEM_ID);
		try {
			if (!YFCObject.isVoid(strShipNode)) {
				strLocationType=getNodeType(env, strShipNode);
			}
			else
			{
				YFSException yfsException = new YFSException();
				yfsException.setErrorDescription("YFS:Invalid Node");
				yfsException.setErrorCode("YFS10048");
				throw yfsException;
			}
		}
		catch(Exception yfsException)
		{
			throw yfsException;
		}

		String strUOM=eleItem.getAttribute(AcademyConstants.ATTR_UOM);
		String strReasonCode=eleItem.getAttribute(AcademyConstants.ATTR_REASON_CODE);
		String strReasonText=eleItem.getAttribute(AcademyConstants.ATTR_REASON_TEXT);
		String strQuantity=eleItem.getAttribute(AcademyConstants.ATTR_QUANTITY);
		String strUser=eleItem.getAttribute(AcademyConstants.ATTR_USER);
		HashMap<String, Map<String, List<String>>> itemToSupplyMap = new HashMap<>();
		JSONObject adjustInventory = new JSONObject();
		try {
				JSONObject key=new JSONObject();
				key.put(AcademyConstants.JSON_ATTR_ORG_ID, AcademyConstants.ORG_ID_ACADEMY);
				key.put(AcademyConstants.JSON_ATTR_PRODUCT_ID,strProductId);
				key.put(AcademyConstants.JSON_ATTR_UOM, strUOM);
				key.put(AcademyConstants.YIH_LOCATION_ID,strShipNode);
				key.put(AcademyConstants.JSON_ATTR_LOCATION_TYPE,strLocationType);
				JSONObject value=new JSONObject();
				value.put(AcademyConstants.JSON_ATTR_EVENT_TYPE, AcademyConstants.V_SUPPLY_UPDATE);
				value.put(AcademyConstants.JSON_ATTR_FEED_TYPE, AcademyConstants.V_DELTA);
				value.put(AcademyConstants.YIH_LOCATION_ID,strShipNode);
				value.put(AcademyConstants.JSON_ATTR_LOCATION_TYPE, strLocationType);
				value.put(AcademyConstants.JSON_ATTR_ORG_ID, AcademyConstants.ORG_ID_ACADEMY);
				value.put(AcademyConstants.JSON_ATTR_PRODUCT_ID, strProductId);
				value.put(AcademyConstants.JSON_ATTR_UOM, strUOM);
				SimpleDateFormat formatter = new SimpleDateFormat(AcademyConstants.JSON_YAS_TIME_STAMP_FORMAT, Locale.US);
				String strcurrentDate = YantriksCommonUtil.getCurrentDateOrTimeStamp(formatter);
				value.put(AcademyConstants.JSON_ATTR_UPDATE_TIME_STAMP, strcurrentDate);	
				JSONObject to=new JSONObject();
				JSONArray supplyTypesArray= new JSONArray();
				JSONObject supplyType=new JSONObject();
				supplyType.put(AcademyConstants.JSON_ATTR_SUPPLY_TYPE, AcademyConstants.V_SOH);
				supplyType.put(AcademyConstants.JSON_ATTR_QUANTITY, strQuantity);
				supplyTypesArray.put(supplyType);
				to.put(AcademyConstants.JSON_ATTR_SUPPLY_TYPES, supplyTypesArray);
				value.put(AcademyConstants.JSON_ATTR_TO,to);
				JSONObject audit=new JSONObject();
				audit.put(AcademyConstants.JSON_ATTR_TXN_ID, strReasonCode);
				audit.put(AcademyConstants.JSON_ATTR_TXN_REASON,strReasonText);
				audit.put(AcademyConstants.JSON_ATTR_TXN_SYSTEM, AcademyConstants.V_INV_SERVICE_CONSUMER);
				audit.put(AcademyConstants.JSON_INP_TRANSACTION_TYPE, AcademyConstants.V_SUPPLY_FEED);
				audit.put(AcademyConstants.JSON_ATTR_TXN_USER, strUser);
				value.put(AcademyConstants.JSON_ATTR_AUDIT,audit);
				adjustInventory.put(AcademyConstants.A_IS_FULLYQUALIFIED_TOPIC_NAME, AcademyConstants.STR_FALSE);
				adjustInventory.put(AcademyConstants.JSON_ATTR_KEY,key);
				adjustInventory.put(AcademyConstants.A_OPERATION, AcademyConstants.STR_CREATE);
				adjustInventory.put(AcademyConstants.A_TOPIC, AcademyConstants.V_CUSTOM_INV_FEED_UPDATE);
				adjustInventory.put(AcademyConstants.JSON_ATTR_VALUE, value);
				
				log.debug("JSON Request is  ::" + adjustInventory);
		} 
		catch (JSONException e) 
		{
			log.debug("JSON Exception in AcademyYantriksAdjustInventory::adjustInventory :: " + e.getMessage());
		}
		invokeYantriksAPI(adjustInventory, inDoc, itemToSupplyMap,env);
		log.endTimer("AcademyYantriksAdjustInventory::adjustInventory");
		return inDoc;
	}
	
	// To be added to utill
	
	/**
	 * This method gets the NodeType for the ShipNode.
	 * 
	 * @param env
	 * @param strShipNode
	 * @return String
	 */
	
	public String getNodeType(YFSEnvironment env,String  strShipNode) throws Exception
	{
		log.beginTimer("AcademyGetYantriksNodeSupply::getNodeType");
		log.debug(" ShipNode is : "+strShipNode);
		String strNodeType=null;
		YFCDocument ShipNodeDoc = YFCDocument.createDocument(AcademyConstants.ATTR_SHIP_NODE);
		YFCElement eleShipNode  = ShipNodeDoc.getDocumentElement();
		eleShipNode.setAttribute(AcademyConstants.ATTR_SHIP_NODE, strShipNode);
		Document docGetOrderListOut;
		try {
			docGetOrderListOut = AcademyUtil.invokeService(env,AcademyConstants.SERVICE_ACADEMY_GET_SHIPNODE_LIST,ShipNodeDoc.getDocument());
			Element eleShipNodeList=docGetOrderListOut.getDocumentElement();
			Element eleShipNodeOut=(Element)eleShipNodeList.getElementsByTagName(AcademyConstants.ATTR_SHIP_NODE).item(0);
			if (!YFCCommon.isVoid(eleShipNodeOut)) {
			String strOutNodeType=eleShipNodeOut.getAttribute(YantriksConstants.NODE_TYPE);
			log.debug(" NodeType  is : "+strOutNodeType);
			if (!YFCObject.isVoid(strOutNodeType))
			{
				if(strOutNodeType.equalsIgnoreCase(YantriksConstants.LT_STORE))
				{
					strNodeType=YantriksConstants.YANTRIKS_VAL_STORE;
				}
				else if(strOutNodeType.contains(YantriksConstants.LT_DC))
				{
					strNodeType=YantriksConstants.YANTRIKS_VAL_DC;
				}
				else if(strOutNodeType.equalsIgnoreCase(YantriksConstants.DROP_SHIP_NODE_TYPE))
				{
					strNodeType=YantriksConstants.LT_DSV;
					YFSException yfsException = new YFSException();
					yfsException.setErrorDescription("Inventory Adjustment to Drop Ship Location not allowed");
					yfsException.setErrorCode("Drop Ship Error");
					log.info("Throwing Exception");
					throw yfsException;
				}
			}
			}
			else
			{
				YFSException yfsException = new YFSException();
				yfsException.setErrorDescription("YFS: Invalid Ship Node");
				yfsException.setErrorCode("YFS10048");
				throw yfsException;
			}
		} 
		catch (Exception yfsException) 
		{
			log.debug("Catching Exception");
			throw yfsException;
		}	
		log.endTimer("AcademyGetYantriksNodeSupply::getNodeType");
		return strNodeType;
	}

	/**
	 * This method Invokes the Yantriks webservice to get supply for the Item
	 * 
	 * @param inputJson
	 * @param inDoc
	 * @param itemToSupplyMap
	 * @return Document
	 */
	public Document invokeYantriksAPI(JSONObject inputJson, Document inDoc,
			HashMap<String, Map<String, List<String>>> itemToSupplyMap,YFSEnvironment env) 
	{
		log.beginTimer("AcademyYantriksAdjustInventory::invokeYantriksAPI");
		log.debug("AcademyYantriksAdjustInventory.invokeYantriksAPI() Input XML ::"+ XMLUtil.getXMLString(inDoc));
		log.debug("Input JSON in invokeYantriksAPI ::" + inputJson);
		String jsonStr = null;
		String httpBody = "";
		try 
		{
			jsonStr = inputJson.toString();
		} 
		catch (Exception ex) 
		{
			log.error("Exception Caught while calling availability API with input : " + ex);
		}
		String output = null;
		try {
			//String apiUrl = "https://academy-dev-pfpcs.yantriks.in/kafka-rest-services";
			String apiUrl = YFSSystem.getProperty(AcademyConstants.URL_YANTRIK_KAFKA_UPDATE);
			httpBody = httpBody.concat("\n   " + jsonStr);
			if (log.isDebugEnabled())
				log.debug("httpBody is : " + httpBody);

			log.debug("API input:" + httpBody);
			String content = httpBody.substring(1, httpBody.length() - 1);
			if (content != null && !content.isEmpty()) 
			{
				output = YantriksCommonUtil.callYantriksAPI(apiUrl, YantriksConstants.YIH_REQ_METHOD_POST, httpBody,
						YantriksCommonUtil.getAvailabilityProduct(),env);

				log.debug("API response is:" + output);

				if (YantriksConstants.V_NO_CONTENT_FOUND.equals(output)) 
				{
					log.verbose("No Content Found as we received 204 from availability call, will set supply to zero");
				} 
				else if (!output.equals(YantriksConstants.V_FAILURE) && !output.equals(YantriksConstants.V_NON_RETRY_EXCEPTION)) 
				{
					log.debug("Success response from Yantriks call" + output);
					Thread.sleep(1000);
				} 
				else 
				{
					log.error("Received the System failure " + output);
				}
			} 
			else {
				log.error("Not Calling YIH API, As the Http Body is empty....");
				return inDoc;
			}
		}
		catch (Exception e2) {
			log.error("Exception Caught while calling availability API with input : " + e2);
		}
		log.endTimer("AcademyYantriksAdjustInventory::invokeYantriksAPI");
		return inDoc;

	}
}
