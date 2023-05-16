package com.academy.ecommerce.yantriks.inventory;

import java.util.HashMap;
import java.util.Iterator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantriks.yih.adapter.util.YantriksCommonUtil;
import com.yantriks.yih.adapter.util.YantriksConstants;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.yantra.yfs.core.YFSSystem;

public class AcademyGetYantriksNodeSupply {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyGetYantriksNodeSupply.class);

	/*
	 * Input XML <InventorySupply IgnoreOrdering="Y" ItemID="241089654"
	 * OrganizationCode="" ProductClass="GOOD" Segment="" SegmentType=""
	 * ShipByDate="" ShipNode="9999" SupplyType="" UnitOfMeasure="EACH"
	 * ValidateItem="Y" ValidateShipNode="Y" ValidateShipNodeOwner="N"/>
	 */
	
	/**
	 * This method Prepares the JSON input and invokes the web service.
	 * 
	 * @param env
	 * @param inDoc
	 * @return Document
	 * @throws Exception
	 */

	public Document getYantriksNodeSupply(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer("AcademyGetYantriksNodeSupply::getYantriksNodeSupply");
		log.debug("AcademyGetYantriksNodeSupply.getYantriksNodeSupply() Input XML ::"
				+ XMLUtil.getXMLString(inDoc));
		String strNodeType = null;
		Element elegetYantriksInventory = inDoc.getDocumentElement();
		try {
			String strShipNode = elegetYantriksInventory.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
			if (!YFCObject.isVoid(strShipNode)) {
				strNodeType = getNodeType(env, strShipNode);
			} else {
				YFSException yfsException = new YFSException();
				yfsException.setErrorDescription("YFS:No Ship node provided");
				yfsException.setErrorCode("YFS10046");
				throw yfsException;
			}
		} catch (Exception yfsException) {
			throw yfsException;
		}

		Document outDoc = invokeYantriksAPI(strNodeType, inDoc, env);
		log.endTimer("AcademyGetYantriksNodeSupply::getYantriksNodeSupply");
		return outDoc;
	}

	// To be added to util

	/**
	 * This method gets the NodeType for the ShipNode.
	 * 
	 * @param env
	 * @param strShipNode
	 * @return String
	 */

	public String getNodeType(YFSEnvironment env, String strShipNode) throws Exception {
		log.beginTimer("AcademyGetYantriksNodeSupply::getNodeType");
		log.debug(" ShipNode is : "+strShipNode);
		String strNodeType = null;
		YFCDocument ShipNodeDoc = YFCDocument.createDocument(AcademyConstants.ATTR_SHIP_NODE);
		YFCElement eleShipNode = ShipNodeDoc.getDocumentElement();
		eleShipNode.setAttribute(AcademyConstants.ATTR_SHIP_NODE, strShipNode);
		Document docGetOrderListOut;
		try {
			docGetOrderListOut = AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACADEMY_GET_SHIPNODE_LIST,
					ShipNodeDoc.getDocument());
			Element eleShipNodeList = docGetOrderListOut.getDocumentElement();
			Element eleShipNodeOut = (Element) eleShipNodeList.getElementsByTagName(AcademyConstants.ATTR_SHIP_NODE)
					.item(0);
			if (!YFCCommon.isVoid(eleShipNodeOut)) {
				String strOutNodeType = eleShipNodeOut.getAttribute(YantriksConstants.NODE_TYPE);
				log.debug(" NodeType  is : "+strOutNodeType);
				if (!YFCObject.isVoid(strOutNodeType)) {
					if (strOutNodeType.equalsIgnoreCase(YantriksConstants.LT_STORE)) {
						strNodeType = YantriksConstants.YANTRIKS_VAL_STORE;
					} else if (strOutNodeType.contains(YantriksConstants.LT_DC)) {
						strNodeType = YantriksConstants.YANTRIKS_VAL_DC;
					} else if (strOutNodeType.equalsIgnoreCase(YantriksConstants.DROP_SHIP_NODE_TYPE)) {
						strNodeType = YantriksConstants.LT_DSV;
					}

				}
			} else {
				YFSException yfsException = new YFSException();
				yfsException.setErrorDescription("YFS: Invalid Ship Node");
				yfsException.setErrorCode("YFS10048");
				throw yfsException;
			}
		} catch (Exception yfsException) {
			log.debug("Exception Caught while getting NodeType for the shipNode :  " + yfsException);
			throw yfsException;
		}
		log.endTimer("AcademyGetYantriksNodeSupply::getNodeType");
		return strNodeType;
	}

	/**
	 * This method Invokes the Yantriks webservice to get supply for the Item
	 * 
	 * @param strNodeType
	 * @param inDoc
	 * @param env
	 * @return Document
	 */
	
	public Document invokeYantriksAPI(String strNodeType, Document inDoc, YFSEnvironment env) throws Exception {
		log.beginTimer("AcademyGetYantriksNodeSupply::invokeYantriksAPI");
		String jsonStr = null;
		String httpBody = "";
		String output = null;
		HashMap<String, String> locationToSupplyMap = new HashMap<String, String>();
		Document outDoc = null;
		String apiUrl ="";
		String WebServiceUrl="";

		try {
			Element elegetYantriksInventory = inDoc.getDocumentElement();
			String strItemID = elegetYantriksInventory.getAttribute(YantriksConstants.ATTR_ITEM_ID);
			String strShipNode = elegetYantriksInventory.getAttribute(YantriksConstants.SHIP_NODE);
			String strUnitOfMeasure = elegetYantriksInventory.getAttribute(YantriksConstants.UOM);
			//String apiUrl = "https://academy-dev-pfpi.yantriks.in/inventory-services/search-detail/ACADEMY/" + strItemID+ "/" + strUnitOfMeasure + "/" + strNodeType + "/" + strShipNode + "/" + "ONHAND";
			WebServiceUrl=YFSSystem.getProperty(AcademyConstants.YANTRIKS_NODE_SUPPLY_URL);
			if (!YFCObject.isVoid(WebServiceUrl)) 
			{
				apiUrl = YFSSystem.getProperty(AcademyConstants.YANTRIKS_NODE_SUPPLY_URL).replace(AcademyConstants.ATTR_$ITEM_ID,strItemID );
				apiUrl = apiUrl.replace(AcademyConstants.ATTR_$UNIT_OF_MEASURE, strUnitOfMeasure);
				apiUrl = apiUrl.replace(AcademyConstants.ATTR_$NODE_TYPE, strNodeType);
				apiUrl = apiUrl.replace(AcademyConstants.ATTR_$SHIP_NODE, strShipNode);
			}
			log.debug(" Final WebServiceURL : " + apiUrl);
			
			httpBody = httpBody.concat("\n   " + jsonStr);
			if (log.isDebugEnabled())
				log.debug("httpBody is : " + httpBody);

			log.debug("API input:" + httpBody);

			output = YantriksCommonUtil.callYantriksAPI(apiUrl, YantriksConstants.HTTP_METHOD_GET, httpBody,
					YantriksCommonUtil.getAvailabilityProduct(), env);

			log.debug("API response is:" + output);

			if (YantriksConstants.V_NO_CONTENT_FOUND.equals(output)) {
				log.debug("No Content Found as we received 204 from availability call, will set supply to zero");
				outDoc = populateOutput(locationToSupplyMap, "204");
			} else if (!output.equals(YantriksConstants.V_FAILURE)
					&& !output.equals(YantriksConstants.V_NON_RETRY_EXCEPTION)) {
				log.debug("Success response from Yantriks call" + output);
				PopulateSupplyMap(locationToSupplyMap, output);
				outDoc = populateOutput(locationToSupplyMap, "200");
			} else {
				log.error("Received the System failure " + output);
			}
		} catch (Exception Exception) {
			log.error("Exception Caught while calling availability API with input : " + Exception);
			YFSException yfsException = new YFSException();
			yfsException.setErrorDescription("YFS:Inventory Hub Connectivity Failed");
			yfsException.setErrorCode("YFS10045");
			throw yfsException;
		}
		log.endTimer("AcademyGetYantriksNodeSupply::invokeYantriksAPI");
		return outDoc;
	}

	/**
	 * This method populates the map.
	 * 
	 * @param locationToSupplyMap
	 * @param output
	 * @return HashMap
	 */
	
	public HashMap<String, String> PopulateSupplyMap(HashMap<String, String> locationToSupplyMap, String output) {

		log.beginTimer("AcademyGetYantriksNodeSupply::PopulateSupplyMap");

		try {
			JSONObject jResponseObj = new JSONObject(output);
			JSONArray physicalInventory = jResponseObj.getJSONArray(YantriksConstants.JSON_PHYSICAL_INVENTORY);
			String productId = jResponseObj.getString(YantriksConstants.JSON_ATTR_PRODUCT_ID);
			log.debug("ProductId :: " + productId);
			log.debug("physicalInventory :: " + physicalInventory.toString());

			for (int k = 0; k < physicalInventory.length(); k++) {
				JSONObject currentLocationDetail = physicalInventory.getJSONObject(k);
				log.debug("currentLocationDetail :: " + currentLocationDetail);
				JSONArray locations = currentLocationDetail.getJSONArray(YantriksConstants.JSON_ATTR_LOCATIONS);
				for (int i = 0; i < locations.length(); i++) {
					JSONObject locationList = locations.getJSONObject(i);
					String locationID = locationList.getString(YantriksConstants.JSON_ATTR_LOCATION_ID);
					log.debug("locationID :: " + locationID);
					JSONArray supplyTypes = locationList.getJSONArray(YantriksConstants.JSON_SUPPLY_TYPES);
					for (int j = 0; j < supplyTypes.length(); j++) {
						JSONObject supplyTypesList = supplyTypes.getJSONObject(j);
						String quantity = supplyTypesList.getString(YantriksConstants.JSON_ATTR_QUANTITY);
						log.debug("quantity :: " + quantity);
						locationToSupplyMap.put(locationID, quantity);
					}
				}
			}
		} catch (NumberFormatException | JSONException e) {
			throw new YFSException("Exception Found :: " + e.getMessage());
		}
		log.endTimer("AcademyGetYantriksNodeSupply::PopulateSupplyMap");
		return locationToSupplyMap;
	}

	/**
	 * This method prepares the output in the required format.
	 * 
	 * @param locationToSupplyMap
	 * @param strResponse
	 * @return Document
	 */
	
	public Document populateOutput(HashMap<String, String> locationToSupplyMap, String strResponse) {
		log.beginTimer("AcademyGetYantriksNodeSupply::populateOutput");
		log.debug("Response is :" + strResponse);
		YFCDocument ItemDoc = YFCDocument.createDocument(YantriksConstants.E_ITEM);
		YFCElement eleItem = ItemDoc.getDocumentElement();
		YFCElement eleSupplies = eleItem.createChild(YantriksConstants.E_SUPPLIES);
		Iterator it = locationToSupplyMap.entrySet().iterator();
		if (it.hasNext()) {
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				log.debug(pair.getKey() + " = " + pair.getValue());
				YFCElement eleInventorySupply = eleSupplies.createChild(YantriksConstants.ATTR_INVENTORY_SUPPLY);
				eleInventorySupply.setAttribute(YantriksConstants.A_SHIP_NODE, pair.getKey().toString());
				eleInventorySupply.setAttribute(YantriksConstants.A_QUANTITY, pair.getValue().toString());
			}
		} else {
			YFCElement eleInventorySupply = eleSupplies.createChild(YantriksConstants.ATTR_INVENTORY_SUPPLY);
			eleInventorySupply.setAttribute(YantriksConstants.A_QUANTITY, "0.00");
		}
		eleItem.setAttribute(YantriksConstants.RESPONSE, strResponse);
		log.verbose("output is =" + XMLUtil.getXMLString(ItemDoc.getDocument()));
		log.endTimer("AcademyGetYantriksNodeSupply::populateOutput");
		return ItemDoc.getDocument();
	}
}
