package com.academy.ecommerce.yantriks.inventory;

import java.util.HashMap;
import java.util.Iterator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.Map;

import org.apache.commons.json.JSONArray;
import org.apache.commons.json.JSONException;
import org.apache.commons.json.JSONObject;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantriks.yih.adapter.util.YantriksCommonUtil;
import com.yantriks.yih.adapter.util.YantriksConstants;

public class AcademyGetYantriksShipNodesSupply {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyGetYantriksShipNodesSupply.class);

	/*
	 * Input XML <getShipNodeInventory ConsiderAllNodes="" ConsiderAllSegments=""
	 * Description="" DistributionRuleId="" IgnoreOrdering="Y" ItemID="114587727"
	 * Node="" OrganizationCode="DEFAULT" ProductClass="GOOD" Segment=""
	 * SegmentType="" ShipDate="" UnitOfMeasure="EACH"/>
	 */
	
	/**
	 * This method Prepares the JSON input and invokes the web service.
	 * 
	 * @param env
	 * @param inDoc
	 * @return Document
	 * @throws Exception
	 */

	public Document getShipNodesSupply(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer("AcademyGetYantriksShipNodesSupply::getShipNodesSupply");
		log.debug("AcademyGetYantriksShipNodesSupply.getShipNodesSupply() Input XML ::"
				+ XMLUtil.getXMLString(inDoc));
		Element elegetShipNodeInventory = null;
		Document outDoc = null;
		elegetShipNodeInventory = inDoc.getDocumentElement();
		String strItemID = elegetShipNodeInventory.getAttribute(YantriksConstants.ATTR_ITEM_ID);
		String strUnitOfMeasure = elegetShipNodeInventory.getAttribute(YantriksConstants.UOM);
		String strOrganizationCode = elegetShipNodeInventory.getAttribute(YantriksConstants.ORGANIZATION_CODE);
		String strProductClass = elegetShipNodeInventory.getAttribute(YantriksConstants.A_PRODUCT_CLASS);
		String strDescription = elegetShipNodeInventory.getAttribute(YantriksConstants.A_DESCRIPTION);
		// invokeYantriksAPI WS Call
		outDoc = invokeYantriksAPI(strItemID, strUnitOfMeasure, inDoc, env);
		log.debug("outDoc is :" + outDoc);
		Element eleShipNodeInventory = outDoc.getDocumentElement();
		Element eleItem = (Element) eleShipNodeInventory.getElementsByTagName(YantriksConstants.ITEM).item(0);
		eleItem.setAttribute(YantriksConstants.ORGANIZATION_CODE, strOrganizationCode);
		eleItem.setAttribute(YantriksConstants.ATTR_ITEM_ID, strItemID);
		eleItem.setAttribute(YantriksConstants.A_PRODUCT_CLASS, strProductClass);
		eleItem.setAttribute(YantriksConstants.UOM, strUnitOfMeasure);
		eleItem.setAttribute(YantriksConstants.A_DESCRIPTION, strDescription);
		log.endTimer("AcademyGetYantriksShipNodesSupply::getShipNodesSupply");
		return outDoc;
	}

	/**
	 * This method Invokes the Yantriks webservice to get supply for an Item.
	 * 
	 * @param strItemID
	 * @param strUnitOfMeasure
	 * @param inDoc
	 * @param env
	 * @return Document
	 */

	private Document invokeYantriksAPI(String strItemID, String strUnitOfMeasure, Document inDoc, YFSEnvironment env) {
		log.beginTimer("AcademyGetYantriksShipNodesSupply::invokeYantriksAPI");
		Document outDoc = null;
		String jsonStr = null;
		String httpBody = "";
		String output = null;
		String apiUrl ="";
		String WebServiceUrl="";
		HashMap<String, String> locationToSupplyMap = new HashMap<String, String>();

		try {
			//String apiUrl = "https://academy-dev-pfpi.yantriks.in/inventory-services/search/ACADEMY/" + strItemID + "/"+ strUnitOfMeasure + "/" + "ONHAND";
			WebServiceUrl=YFSSystem.getProperty(AcademyConstants.YANTRIKS_SHIPNODES_SUPPLY_URL);
			if (!YFCObject.isVoid(WebServiceUrl)) 
			{
				apiUrl = YFSSystem.getProperty(AcademyConstants.YANTRIKS_SHIPNODES_SUPPLY_URL).replace(AcademyConstants.ATTR_$ITEM_ID,strItemID );
				apiUrl = apiUrl.replace(AcademyConstants.ATTR_$UNIT_OF_MEASURE, strUnitOfMeasure);
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
				outDoc = populateOutput(locationToSupplyMap, "Error");
			}
		} catch (Exception e2) {
			log.error("Exception Caught while calling availability API with input : " + e2);
			outDoc = populateOutput(locationToSupplyMap, "Error");
		}
		log.endTimer("AcademyGetYantriksShipNodesSupply::invokeYantriksAPI");

		return outDoc;
	}

	/**
	 * This method reads the supply and populates the map.
	 * 
	 * @param locationToSupplyMap
	 * @param output
	 * @return HashMap
	 */
	public HashMap<String, String> PopulateSupplyMap(HashMap<String, String> locationToSupplyMap, String output) {

		log.beginTimer("AcademyGetYantriksShipNodesSupply::PopulateSupplyMap");

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
		log.endTimer("AcademyGetYantriksShipNodesSupply::PopulateSupplyMap");
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
		log.beginTimer("AcademyGetYantriksShipNodesSupply::populateOutput");
		log.debug("Response is :" + strResponse);
		YFCDocument ShipNodeInventoryDoc = YFCDocument.createDocument(YantriksConstants.SHIP_NODE_INVENTORY);
		YFCElement ShipNodeInventory = ShipNodeInventoryDoc.getDocumentElement();
		YFCElement eleItem = ShipNodeInventory.createChild(YantriksConstants.ITEM);
		YFCElement eleShipNodes = eleItem.createChild(YantriksConstants.A_SHIP_NODES);
		Iterator it = locationToSupplyMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			log.debug(pair.getKey() + " = " + pair.getValue());
			YFCElement eleShipNode = eleShipNodes.createChild(YantriksConstants.ELE_SHIP_NODE);
			eleShipNode.setAttribute(YantriksConstants.A_SHIP_NODE, pair.getKey().toString());
			eleShipNode.setAttribute(YantriksConstants.TOTAL_SUPPLY, pair.getValue().toString());
		}
		eleItem.setAttribute(YantriksConstants.RESPONSE, strResponse);
		log.verbose("output is =" + XMLUtil.getXMLString(ShipNodeInventoryDoc.getDocument()));
		log.endTimer("AcademyGetYantriksShipNodesSupply::populateOutput");
		return ShipNodeInventoryDoc.getDocument();
	}
}
