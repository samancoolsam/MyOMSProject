package com.academy.ecommerce.yantriks.inventory;

import com.academy.util.constants.AcademyConstants;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;

import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfs.japi.ue.YFSGetExternalInventoryForItemListUE;
//import com.yantriks.yih.adapter.util.YIHXMLUtil;
import com.yantriks.yih.adapter.util.YantriksCommonUtil;
import org.apache.commons.json.JSONArray;
import org.apache.commons.json.JSONException;
import org.apache.commons.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

public class YASGetExternalInventoryForItemListUEImpl implements YFSGetExternalInventoryForItemListUE {

	private static final YFCLogCategory log = YFCLogCategory.instance(YASGetExternalInventoryForItemListUEImpl.class);

	/* This method is invoked on the YFSGetExternalInventoryForItemListUE */
	/*
	 * Sample UE Input : <ItemList MaximumRecords="5000"> <Item
	 * AggregateSupplyOfNonRequestedTag="Y" ApplyFutureSafetyFactor="Y"
	 * ApplyOnhandSafetyFactor="Y" DemandType="SCHEDULED"
	 * EnterpriseCode="Academy_Direct" FirstShipDate="2021-03-15" IgnorePromised="N"
	 * IgnoreUnpromised="Y" ItemID="102540489" LastShipDate="2021-03-15"
	 * OrderReference="202103150609053638430090" OrganizationCode="Academy_Direct"
	 * OwnerOrganizationCode="DEFAULT" ProductClass="GOOD" TotalRequiredQty="1.00"
	 * TransactionId="SCHEDULE.0001" UnitOfMeasure="EACH"> <ShipNodes><ShipNode
	 * ShipNode="711"/></ShipNodes> <Tags><Tag BatchNo="" LotAttribute1=""
	 * LotAttribute2="" LotAttribute3="" LotKeyReference="" LotNumber=""
	 * RevisionNo="" TagNumber=""/></Tags> <Segments><Segment Segment=""
	 * SegmentType=""/></Segments> </Item></ItemList>
	 */
	public Document getExternalInventoryForItemList(YFSEnvironment env, Document inpDoc) {
		log.beginTimer("getExternalInventoryForItemList");
		if (log.isDebugEnabled())
			log.debug("YASGetExternalInventoryForItemListUEImpl :: getExternalInventoryForItemList() Input Document :: "
					+ SCXmlUtil.getString(inpDoc));

		/*
		 * OMNI-25629: Start Change - Fetching the transaction object which is set in
		 * BeforeCreateOrderUE
		 */
		boolean isInvokedOnCreateOrder = false;
		if (!YFCCommon.isVoid(env.getTxnObject("IsInvokedOnCreateOrder"))) {
			isInvokedOnCreateOrder = (boolean) env.getTxnObject("IsInvokedOnCreateOrder");
		}
		log.debug("IsInvokedOnCreateOrder:" + isInvokedOnCreateOrder);
		/* OMNI-25629: End Change */
		/*
		 * Added the if condition to prevent the invocation of the UE on create order as
		 * part of OMNI-30343
		 */

		if (!isInvokedOnCreateOrder) {
			YFCDocument inDoc = YFCDocument.getDocumentFor(inpDoc);
			YFCElement itemGet = inDoc.getDocumentElement().getChildElement(AcademyConstants.ITEM);
			YFCDocument yfcInpDoc = YFCDocument.getDocumentFor(inpDoc);
			YFCElement rootEle = yfcInpDoc.getDocumentElement();
			YFCNodeList<YFCElement> nlItems = rootEle.getElementsByTagName(AcademyConstants.ITEM);
			log.debug("nlItems.length is "+nlItems.getLength());
			/* OMNI-25629: Start Change */
			// GetOrderList API call for fetching the Fulfillment type of the OrderLine
			YFCElement itemEleFromInput = (YFCElement) nlItems.item(0);
			String OrderHeaderKey = itemEleFromInput.getAttribute(AcademyConstants.ATTR_ORDER_REF);

			Document inDocGetOrderList = null;
			try {
				inDocGetOrderList = XmlUtils.createDocument(AcademyConstants.ELE_ORDER);
			} catch (ParserConfigurationException | FactoryConfigurationError e1) { // exception can't be thrown on the
																					// method as the implemented
																					// interface does not accept
				log.error(e1);
			}
			Element eleGetOrderList = inDocGetOrderList.getDocumentElement();

			if (YFCCommon.isVoid(OrderHeaderKey)) {
				OrderHeaderKey = (String) env.getTxnObject(AcademyConstants.ATTR_ORDER_HEADER_KEY);// Fetching env
																									// object set in the
																									// sourcing
																									// optimizer for
																									// OMNI-25630
				log.debug("Get Transaction Object - OrderHeaderKey :" + OrderHeaderKey);
			}

			String orderNo = "";
			Document outGetOrderList = null;
			List<String> backorderItemIdList = new ArrayList<String>();//Initialize the map for storing the list of itemids which are backrdered as part of OMNI-53842
			if (!YFCCommon.isVoid(OrderHeaderKey)) {
				eleGetOrderList.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, OrderHeaderKey);
				/* Invoking getOrderList API */
				outGetOrderList = YantriksCommonUtil.invokeAPI(env, AcademyConstants.TEMP_RECONCILE_GET_ORDER_LIST,
						AcademyConstants.API_GET_ORDER_LIST, inDocGetOrderList);
				YFCDocument outGetOrderListDoc = YFCDocument.getDocumentFor(outGetOrderList);
				YFCElement rootEleGetOrderList = outGetOrderListDoc.getDocumentElement();
				if (!YFCCommon.isVoid(rootEleGetOrderList)) {

					YFCNodeList<YFCElement> norderelement = rootEleGetOrderList
							.getElementsByTagName(AcademyConstants.ELE_ORDER);
					if (!YFCCommon.isVoid(norderelement)) {
						YFCElement orderelement = (YFCElement) norderelement.item(0);
						if (!YFCCommon.isVoid(orderelement)) {
							orderNo = orderelement.getAttribute(AcademyConstants.ATTR_ORDER_NO);
							log.debug("orderNo:" + orderNo);
						}
					}
					
					/*OMNI-53842 : Start Change - Loop through the order status element of each orderline and if status is 1300 and statusQty > 0 
					 * then store the item ids of those orderlines in a list*/
					Element eleOrder = SCXmlUtil.getChildElement(outGetOrderList.getDocumentElement(), AcademyConstants.ELE_ORDER);
					Element eleOrderLines = SCXmlUtil.getChildElement(eleOrder, AcademyConstants.ELE_ORDER_LINES);
					NodeList nlOrderLine = eleOrderLines.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
					for (int i = 0; i < nlOrderLine.getLength(); i++) {
						Element currInOrderLine = (Element) nlOrderLine.item(i);
						Element eleItem=SCXmlUtil.getChildElement(currInOrderLine, AcademyConstants.ITEM);
						String itemId=eleItem.getAttribute(AcademyConstants.ITEM_ID);
						log.debug("itemId:"+itemId);
						Element eleOrderStatuses = SCXmlUtil.getChildElement(currInOrderLine, AcademyConstants.ELE_ORDER_STATUSES);
						List<Element> listOfOrderStatusElement = SCXmlUtil.getChildren(eleOrderStatuses,AcademyConstants.ELE_ORDER_STATUS);
						for (Element currOrderStatusElement : listOfOrderStatusElement) {
							String orderlineStatus=currOrderStatusElement.getAttribute(AcademyConstants.ATTR_STATUS);
							String orderlineStatusQty=currOrderStatusElement.getAttribute(AcademyConstants.ATTR_STAT_QTY);
							double orderlineStatusQtyVal = 0.0D;
							if (!YFCObject.isVoid(orderlineStatusQty)) {
								orderlineStatusQtyVal = Double.parseDouble(orderlineStatusQty);
							}
							log.debug("orderlineStatus:"+orderlineStatus+"   orderlineStatusQty:"+orderlineStatusQty+"  orderlineStatusQtyVal:"+orderlineStatusQtyVal);
							/*OMNI-54374: Start Change*/
							String strDemandType = itemEleFromInput.getAttribute(AcademyConstants.ATTR_DEMAND_TYPE);
							// Read the value of blank yantriks order id enabled flag from customer overrides property
							String blankYantriksOrderIdEnabled = YFSSystem.getProperty(AcademyConstants.PROP_BLANK_YANTRIKS_ORDER_ID_ENABLED);
							log.debug("strDemandType:"+strDemandType+"    blankYantriksOrderIdEnabled:"+blankYantriksOrderIdEnabled);
							/*Update the condition check to map the blank order id for backorder scenario only for scheduled demand type 
							and when the flag for updating blank order id is Y*/
							if(!YFCObject.isVoid(orderlineStatus) && AcademyConstants.STR_BACKORDER_STATUS.equals(orderlineStatus) && orderlineStatusQtyVal > 0 
									&& !YFCObject.isVoid(strDemandType) && AcademyConstants.STR_SCHEDULED.equals(strDemandType) 
									&& !YFCObject.isVoid(blankYantriksOrderIdEnabled) && AcademyConstants.STR_YES.equals(blankYantriksOrderIdEnabled)) 
							/*OMNI-54374: End Change*/ {
								log.debug("OrderLineStatus is 1300 and Status Qty is greater than zero , hence adding item to the backorderItemIdList");
								backorderItemIdList.add(itemId);
							}
						}
						
					}
					log.debug("backorderItemIdList:"+backorderItemIdList);
					/*OMNI-53842 : End Change*/
				}
			}

			// Invoking getShipNodeList API
			ArrayList<String> alShipNodes = new ArrayList<>();
			YFCNodeList<YFCElement> nlShipNode = rootEle.getElementsByTagName(AcademyConstants.SHIP_NODE);

			for (YFCElement eleShipNode : nlShipNode) {
				String strShipNode = eleShipNode.getAttribute(AcademyConstants.SHIP_NODE);
				if (!alShipNodes.contains(strShipNode)) {
					log.debug("ShipNode adding "+strShipNode);
					alShipNodes.add(strShipNode);
				}
			}

			log.debug("alShipNodes:" + alShipNodes);

			YFCNodeList<YFCElement> nlInpItems = rootEle.getElementsByTagName(AcademyConstants.ITEM);
			Document docShipNodeList = SCXmlUtil.createDocument(AcademyConstants.SHIP_NODE);
			YFCDocument docInpShipNodeList = YFCDocument.getDocumentFor(docShipNodeList);
			YFCElement eleShipNode = docInpShipNodeList.getDocumentElement();
			YFCElement eleComplexQuery = eleShipNode.createChild(AcademyConstants.COMPLEX_QRY_ELEMENT);
			YFCElement eleOr = eleComplexQuery.createChild(AcademyConstants.COMPLEX_OR_ELEMENT);
			for (String strShipNode : alShipNodes) {
				YFCElement eleExp = eleOr.createChild(AcademyConstants.COMPLEX_EXP_ELEMENT);
				eleExp.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.SHIP_NODE);
				eleExp.setAttribute(AcademyConstants.ATTR_QRY_TYPE, AcademyConstants.COMPLEX_QRY_TYPE_EQ);
				eleExp.setAttribute(AcademyConstants.ATTR_VALUE, strShipNode);
			}
			log.debug("docShipNodeList:" + SCXmlUtil.getString(docShipNodeList));

			// Invoke getShipNodeList API
			Document docGetShipNodeListOut = YantriksCommonUtil.invokeAPI(env,
					AcademyConstants.TEMP_RECONCILE_GET_SHIPNODE_LIST, AcademyConstants.API_GET_SHIP_NODE_LIST,
					docShipNodeList);

			Map<String, String> mapShipNodeType = new HashMap<>();
			Map<String, String> mapParentOrg = new HashMap<>();

			YFCDocument docOutShipNodeList = YFCDocument.getDocumentFor(docGetShipNodeListOut);
			YFCNodeList<YFCElement> nlOutShipNode = docOutShipNodeList.getElementsByTagName(AcademyConstants.SHIP_NODE);

			for (YFCElement eleOutShipNode : nlOutShipNode) {

				YFCNodeList<YFCElement> nlOrganizationCode = eleOutShipNode
						.getElementsByTagName(AcademyConstants.ORG_ELEMENT);
				YFCElement organizationEle = (YFCElement) nlOrganizationCode.item(0);
				mapShipNodeType.put(eleOutShipNode.getAttribute(AcademyConstants.SHIP_NODE),
						eleOutShipNode.getAttribute(AcademyConstants.ATTR_NODE_TYPE));
				mapParentOrg.put(eleOutShipNode.getAttribute(AcademyConstants.SHIP_NODE),
						organizationEle.getAttribute(AcademyConstants.PARENT_ORGANIZATION_CODE));

			}
			log.debug("mapShipNodeType:" + mapShipNodeType);
			log.debug("mapParentOrg:" + mapParentOrg);

			/* OMNI-25629: End Change */

			String orgCode = null;
			String sellingChannel = AcademyConstants.SELLING_CHANNEL_GLOBAL;

			// Item to Supply Map which will be used to update the UE output so that find
			// inventory will have options
			HashMap<String, Map<String, List<String>>> itemToSupplyMap = new HashMap<>();

			JSONObject inputJson = new JSONObject();
			JSONArray productArray = new JSONArray();
			// Extra Map added to keep track of itemID with total required qty based on it
			Map<String, Integer> itemToRequiredQtyMap = new HashMap<>();
			log.debug("Looping to form Promise Document with length "+nlItems.getLength());
			for (int i = 0; i < nlItems.getLength(); i++) {
				// Looping through the lines for preparing the input
				log.debug("Count "+i);
				JSONObject productObject = new JSONObject();
				YFCElement itemEleFromInp = (YFCElement) nlItems.item(i);
				itemToRequiredQtyMap.put(itemEleFromInp.getAttribute(AcademyConstants.ITEM_Id),
						(int) Double.parseDouble(itemEleFromInp.getAttribute(AcademyConstants.ATTR_TOTAL_REQ_QTY)));
				orgCode = AcademyConstants.ORG_ID;
				if (YFCObject.isVoid(orgCode)) {
					orgCode = itemGet.getAttribute(AcademyConstants.ORGANIZATION_CODE);
				}

				try {
					if (!YFCCommon.isVoid(orderNo)) {
						/*OMNI-53842 : Start Change - For the item ids present in the backorder items list, map the orderid as blank value else map the orderNo */
						String productId=itemEleFromInp.getAttribute(AcademyConstants.ITEM_ID);
						log.debug("productId: "+productId);
						 if(!YFCObject.isVoid(backorderItemIdList) && backorderItemIdList.contains(productId))  {
							log.debug("Mapping orderid as blank value since order has line/qty which is backordered");
							 productObject.put(AcademyConstants.JSON_ATTR_ORDER_ID, "");
						 }
						 else {
							 log.debug("Mapping orderid with orderNo");
							 productObject.put(AcademyConstants.JSON_ATTR_ORDER_ID, orderNo);
						 }
						 /*OMNI-53842 : End Change*/
					}
					productObject.put(AcademyConstants.JSON_ATTR_PRODUCT_ID,
							itemEleFromInp.getAttribute(AcademyConstants.ITEM_ID));
					productObject.put(AcademyConstants.JSON_ATTR_UOM,
							itemEleFromInp.getAttribute(AcademyConstants.ATTR_UOM));
					productArray.add(productObject);

				} catch (JSONException e) {

					log.error("JSON Exception :: " + e);
					YFSException yfsException = new YFSException();
					yfsException
							.setErrorDescription("Exception Caught while calling availability API" + e.getMessage());
					String strErrorCode = AcademyConstants.GET_AVAILABILITY_EXP;
					yfsException.setErrorCode(strErrorCode);
					throw yfsException;
				}

			}
			log.debug("Map size is "+itemToRequiredQtyMap.size());
			try {
				log.debug("Adding Header Attributes");
				inputJson.put(AcademyConstants.JSON_ATTR_PRODUCTS, productArray);
				inputJson.put(AcademyConstants.JSON_ATTR_SELLING_CHANNEL, sellingChannel);
				inputJson.put(AcademyConstants.JSON_INP_TRANSACTION_TYPE, AcademyConstants.TRANS_TYPE);

				inputJson.put(AcademyConstants.JSON_ATTR_ORG_ID, orgCode);
			} catch (JSONException e) {
				log.error("JSON Exception :: " + e);
				YFSException yfsException = new YFSException();
				yfsException.setErrorDescription("Exception Caught while calling availability API" + e.getMessage());
				String strErrorCode = AcademyConstants.GET_AVAILABILITY_EXP;
				yfsException.setErrorCode(strErrorCode);
				throw yfsException;
			}
			/* OMNI-25629: Start Change */
			// check the transaction object to avoid multiple API calls

			if (env.getTxnObject("YntrxGetAvailibiltyResp") != null) {
				log.debug("Get avail is not null");
				itemToSupplyMap = (HashMap<String, Map<String, List<String>>>) env
						.getTxnObject("YntrxGetAvailibiltyResp");
				log.debug("Get Transaction object - itemToSupplyMap :" + itemToSupplyMap);
				rootEle = modifyUEOutput(itemToSupplyMap, rootEle, mapShipNodeType, mapParentOrg);
				log.debug("rootEle:" + rootEle);
				/* OMNI-25629: End Change */
			} else {
				
				log.debug("JSON Before Removing Duplicates");
				log.debug("Input JSON for YAS Call :: " + inputJson);
				//OMNI-51287 BEGIN 
				try {
					inputJson=removeDuplicates(inputJson);
				} catch (JSONException e) {
					log.error("JSON Exception :: " + e);
					YFSException yfsException = new YFSException();
					yfsException.setErrorDescription("Exception Caught while calling availability API" + e.getMessage());
					String strErrorCode = AcademyConstants.GET_AVAILABILITY_EXP;
					yfsException.setErrorCode(strErrorCode);
					throw yfsException;
				}
				log.debug("JSON After Removing Duplicates "+inputJson);
				//OMNI-51287 END
				inpDoc = invokeYIHApiAndFormReturnDoc(env, orgCode, inputJson, inpDoc, itemToRequiredQtyMap,
						itemToSupplyMap, rootEle, outGetOrderList, mapShipNodeType, mapParentOrg);// Modified the method
																									// argument as part
																									// of OMNI-25629
				log.debug("Returning UE Output after calling YIH API" + inDoc.getDocument());
				log.endTimer("getExternalInventoryForItemList");
				return inpDoc;

			}
		}
		/*
		 * Handling the else for the if condition added to avoid UE invocation on create
		 * order as part of OMNI-30343
		 */
		else {
			log.debug("Entering the else condition since getting invoked on create order ");
			setSupplyAsZero(inpDoc, null, null);
			log.endTimer("getExternalInventoryForItemList");
		}

		return inpDoc;

	}

	/*
	 * This method is used for invoking the yantriks getavailablity API - Existing
	 * method with modified arguments added as part of OMNI-25629
	 */
	private Document invokeYIHApiAndFormReturnDoc(YFSEnvironment env, String orgCode, JSONObject inputJson,
			Document inpDoc, Map<String, Integer> itemToRequiredQtyMap,
			HashMap<String, Map<String, List<String>>> itemToSupplyMap, YFCElement rootEle, Document outGetOrderList,
			Map<String, String> mapShipNodeType, Map<String, String> mapParentOrg) {
		String jsonStr = null;
		String httpBody = "";
		log.beginTimer("invokeYIHApi");
		log.debug("itemToRequiredMap inside invokeYIHApi :: " + itemToRequiredQtyMap);

		try {
			jsonStr = inputJson.toString();
			log.debug("JSON String is "+jsonStr);
		} catch (Exception ex) {
			log.error("Exception :: " + ex);
			YFSException yfsException = new YFSException();
			yfsException.setErrorDescription("Exception Caught while calling availability API" + ex.getMessage());
			String strErrorCode = AcademyConstants.GET_AVAILABILITY_EXP;
			yfsException.setErrorCode(strErrorCode);
			throw yfsException;
		}
		String output = null;

		if (!YFCCommon.isVoid(orgCode)) {
			try {

				/*
				 * OMNI-25629 : Begin Change - Fetch the Yantriks getAvailability call URL from
				 * customer overrides properties
				 */
				String apiUrl = YFSSystem.getProperty(AcademyConstants.API_URL_INV_AVL);
				/* OMNI-25629 : End Change */
				httpBody = httpBody.concat("\n   " + jsonStr);

				if (log.isDebugEnabled())
					log.debug("httpBody is : " + httpBody);
				/*
				 * Sample input JSON to Yantriks API: { "transactionType": "OMS",
				 * "sellingChannel": "ACADEMY.COM", "orgId": "ACADEMY", "products": [ { "uom":
				 * "EACH", "productId": "102540489", "orderId": "2021031516" } ] }
				 */
				log.debug("API input:" + httpBody);
				String content = httpBody.substring(1, httpBody.length() - 1);
				if (content != null && !content.isEmpty()) {
					output = YantriksCommonUtil.callYantriksAPI(apiUrl, AcademyConstants.YIH_HTTP_METHOD_POST, httpBody,
							YantriksCommonUtil.getAvailabilityProduct(),env);// Modified the method argument to pass environment object as part of OMNI-16848

					log.debug("API response is:" + output);
					/*
					 * Sample Success response from Yantriks: { "orgId": "ACADEMY",
					 * "sellingChannel": "ACADEMY.COM", "transactionType": "OMS",
					 * "availabilityByProducts": [ { "productId": "102540489", "uom": "EACH",
					 * "gtin": null, "launchDate": null, "availabilityByFulfillmentTypes": [ {
					 * "fulfillmentType": "STS", "availabilityDetails": [ { "segment": "DEFAULT",
					 * "atp": 31.0, "supply": 31.0, "demand": 0.0, "safetyStock": 0.0, "atpStatus":
					 * null, "availabilityByLocations": [ { "locationId": "001", "locationType":
					 * "DC", "atp": 31.0, "supply": 31.0, "demand": 0.0, "safetyStock": 0.0,
					 * "atpStatus": null, "futureQtyByDates": [] } ] } ] }, { "fulfillmentType":
					 * "SHIP", "availabilityDetails": [ { "segment": "DEFAULT", "atp": 10027.0,
					 * "supply": 10032.0, "demand": 1.0, "safetyStock": 4.0, "atpStatus": null,
					 * "availabilityByLocations": [ { "locationId": "502", "locationType": "DSV",
					 * "atp": 10027.0, "supply": 10030.0, "demand": 1.0, "safetyStock": 2.0,
					 * "atpStatus": null, "futureQtyByDates": [] } ] } ] }, { "fulfillmentType":
					 * "PICK", "availabilityDetails": [ { "segment": "DEFAULT", "atp": 50.0,
					 * "supply": 50.0, "demand": 0.0, "safetyStock": 0.0, "atpStatus": null,
					 * "availabilityByLocations": [ { "locationId": "101", "locationType": "STORE",
					 * "atp": 50.0, "supply": 50.0, "demand": 0.0, "safetyStock": 0.0, "atpStatus":
					 * null, "futureQtyByDates": [] } ] } ] } ] } ] }
					 * 
					 */
					if (AcademyConstants.V_NO_CONTENT_FOUND.equals(output)) {
						log.debug(
								"No Content Found as we received 204 from availability call, will set supply to zero");
						setSupplyAsZero(inpDoc, mapShipNodeType, mapParentOrg);
						return inpDoc;
					} else if (!output.equals(AcademyConstants.V_FAILURE)
							&& !output.equals(AcademyConstants.V_NON_RETRY_EXCEPTION)) {
						// If we have not got the failure then we are good to go to update the supply
						// map with item as key and location and date as values
						log.debug("Success response from Yantriks call");
						// OMNI-29101 FindInventory Start Change
						String strFulfillmentType = (String) env.getTxnObject(AcademyConstants.ATTR_FULFILLMENT_TYPE);
						// OMNI-29101 FindInventory End Change
						// OMNI-29102 Reservation Start Change
						Document docReservationInput = (Document) env.getTxnObject("ReservationInput");
						// OMNI-29102 Reservation End Change
						itemToSupplyMap = formItemSupplyCol(env, output, itemToRequiredQtyMap, outGetOrderList,
								strFulfillmentType, docReservationInput, inpDoc);

						/* OMNI-25629: Start Change */
						// set transaction object with the productToItemSupplyMap
						env.setTxnObject("YntrxGetAvailibiltyResp", itemToSupplyMap);
						log.debug("Set Transaction Object - itemToSupplyMap: " + itemToSupplyMap);
						if (log.isDebugEnabled())
							log.debug("Output from formItemSupplyCol() Method :: " + itemToSupplyMap);
						/* OMNI-25629: End Change */
					} else {
						log.info("Exception Caught while calling availability API with input :" + output);
						YFSException yfsException = new YFSException();
						yfsException.setErrorDescription(
								"Yantriks Servers were up but received the system failure hence throwing exception, Response Code can be identified in the traces");
						throw yfsException;
					}
				} else {
					log.error("Not Calling YIH API, As the Http Body is empty....");
					return inpDoc;
				}
			} catch (Exception e2) {
				/* OMNI-43045: Start Change - Catch the exception thrown from common util code and then throw back the caught exception*/
				log.info("Exception Caught while calling availability API with input : " + httpBody);
				throw new YFSException(e2.getMessage());
				/* OMNI-43045: End Change*/
			}
			if (itemToSupplyMap != null && itemToSupplyMap.size() > 0) {
				rootEle = modifyUEOutput(itemToSupplyMap, rootEle, mapShipNodeType, mapParentOrg);// Modified the method
																									// arguments as part
																									// of OMNI-25629
				/*
				 * Sample modifyUE Output : <ItemList MaximumRecords="5000"> <Item
				 * AggregateSupplyOfNonRequestedTag="Y" ApplyFutureSafetyFactor="Y"
				 * ApplyOnhandSafetyFactor="Y" DemandType="SCHEDULED"
				 * EnterpriseCode="Academy_Direct" FirstShipDate="2021-03-15" IgnorePromised="N"
				 * IgnoreUnpromised="Y" ItemID="102540489" LastShipDate="2021-03-15"
				 * OrderReference="202103150609053638430090" OrganizationCode="Academy_Direct"
				 * OwnerOrganizationCode="DEFAULT" ProductClass="GOOD" TotalRequiredQty="1.00"
				 * TransactionId="SCHEDULE.0001" UnitOfMeasure="EACH"> <ShipNodes><ShipNode
				 * ShipNode="711"/></ShipNodes> <Tags><Tag BatchNo="" LotAttribute1=""
				 * LotAttribute2="" LotAttribute3="" LotKeyReference="" LotNumber=""
				 * RevisionNo="" TagNumber=""/></Tags> <Segments><Segment Segment=""
				 * SegmentType=""/></Segments> <Supplies><Supply FirstShipDate="2021-03-18"
				 * LastShipDate="2021-03-18" Quantity="10" ShipNode="711"/></Supplies>
				 * </Item></ItemList>
				 */
				log.debug("Output from modifyUEOutput() method:: " + rootEle);
				if (log.isDebugEnabled())
					log.debug("Output from modifyUEOutput() method : " + rootEle);

			} else {
				setSupplyAsZero(inpDoc, mapShipNodeType, mapParentOrg);// Modified the method arguments as part of
																		// OMNI-25629
				/*
				 * Sample UE Output for setSupplyAsZero: <ItemList MaximumRecords="5000"> <Item
				 * AggregateSupplyOfNonRequestedTag="Y" ApplyFutureSafetyFactor="Y"
				 * ApplyOnhandSafetyFactor="Y" DemandType="SCHEDULED"
				 * EnterpriseCode="Academy_Direct" FirstShipDate="2021-03-15" IgnorePromised="N"
				 * IgnoreUnpromised="Y" ItemID="102540489" LastShipDate="2021-03-15"
				 * OrderReference="202103150609053638430090" OrganizationCode="Academy_Direct"
				 * OwnerOrganizationCode="DEFAULT" ProductClass="GOOD" TotalRequiredQty="1.00"
				 * TransactionId="SCHEDULE.0001" UnitOfMeasure="EACH"> <ShipNodes><ShipNode
				 * ShipNode="711"/></ShipNodes> <Tags><Tag BatchNo="" LotAttribute1=""
				 * LotAttribute2="" LotAttribute3="" LotKeyReference="" LotNumber=""
				 * RevisionNo="" TagNumber=""/></Tags> <Segments><Segment Segment=""
				 * SegmentType=""/></Segments> <Supplies><Supply FirstShipDate="2021-03-18"
				 * LastShipDate="2021-03-18" Quantity="0" ShipNode="711"/></Supplies>
				 * </Item></ItemList>
				 */
				return inpDoc;
			}
			log.endTimer("invokeYIHApi");
		}

		return inpDoc;

	}

	/*
	 * setting the supply as zero when we receive NO_CONTENT _FOUND - Existing
	 * method with modified arguments added as part of OMNI-25629
	 */
	private static void setSupplyAsZero(Document inpDoc, Map<String, String> mapShipNodeType,
			Map<String, String> mapParentOrg) {
		log.beginTimer("setSupplyAsZero");
		YFCDocument yfcInpDoc = YFCDocument.getDocumentFor(inpDoc);
		if (log.isDebugEnabled())
			log.debug("Input Document in setSupplyAsZero :: " + yfcInpDoc);

		YFCElement rootEle = yfcInpDoc.getDocumentElement();
		YFCNodeList<YFCElement> nlInpItems = rootEle.getElementsByTagName(AcademyConstants.ITEM);
		for (int j = 0; j < nlInpItems.getLength(); j++) {

			YFCElement itemEleFromInp = (YFCElement) nlInpItems.item(j);
			if (log.isDebugEnabled())
				log.debug("itemEleFromInp :: " + itemEleFromInp);

			String orderQty = itemEleFromInp.getAttribute(AcademyConstants.ATTR_TOTAL_REQ_QTY);
			int totalQty = (int) Float.parseFloat(orderQty);

			YFCElement Supplies = itemEleFromInp.createChild(AcademyConstants.ELE_SUPPLIES);
			if (log.isDebugEnabled())
				log.debug("orderLineQty :: " + totalQty);

			String inpItemID = itemEleFromInp.getAttribute(AcademyConstants.ITEM_ID);
			String orgCode = itemEleFromInp.getAttribute(AcademyConstants.ORGANIZATION_CODE);
			/*
			 * OMNI- 25629 : Start Change - Fetching the first & last ship date from the
			 * item element
			 */
			String firstShipDate = itemEleFromInp.getAttribute(AcademyConstants.A_FIRST_SHIP_DATE);
			String lastShipDate = itemEleFromInp.getAttribute(AcademyConstants.A_LAST_SHIP_DATE);
			/* OMNI-25629 : End Change */
			if (log.isDebugEnabled())
				log.debug("inpItemID :: " + inpItemID);
			log.debug("orgCode :: " + orgCode);

			YFCNodeList<YFCElement> inpShipNode = itemEleFromInp.getElementsByTagName(AcademyConstants.SHIP_NODE);
			for (int cnt = 0; cnt < inpShipNode.getLength(); cnt++) {
				YFCElement eleSupply = Supplies.createChild(AcademyConstants.E_SUPPLY);
				YFCElement inpSupply = (YFCElement) inpShipNode.item(cnt);
				String shipNode = inpSupply.getAttribute(AcademyConstants.SHIP_NODE);

				if (log.isDebugEnabled())
					log.debug("shipNode : " + shipNode);

				eleSupply.setAttribute(AcademyConstants.SHIP_NODE, shipNode);
				eleSupply.setAttribute(AcademyConstants.ATTR_QUANTITY, AcademyConstants.ATTR_ZERO);
				/*
				 * OMNI- 25629 : Start Change - Setting the first & last ship date fetched from
				 * the item element
				 */
				eleSupply.setAttribute(AcademyConstants.A_FIRST_SHIP_DATE, firstShipDate);
				eleSupply.setAttribute(AcademyConstants.A_LAST_SHIP_DATE, lastShipDate);
				// Method invoked for setting the organization code for DSV node
				if (!YFCCommon.isVoid(mapShipNodeType)) {
					if (mapShipNodeType.get(shipNode).equals(AcademyConstants.DROP_SHIP_NODE_TYPE)) {
						setOrgCodeForDSVNode(itemEleFromInp, mapParentOrg, shipNode);
					}
				}
				/* OMNI-25629 : End Change */
				if (log.isDebugEnabled())
					log.debug("Supply Element :: " + eleSupply.toString());

			}
		}
		if (log.isDebugEnabled()) {
			// log.debug
			log.debug("Output Document from setSupplyAsZero() :: " + yfcInpDoc.toString());

		}
		log.debug("Output Document from setSupplyAsZero() :: " + yfcInpDoc.toString());
		log.endTimer("setSupplyAsZero");
	}

	/*
	 * Method used for modifying the UE output with the supply details - Existing
	 * method with modified arguments added as part ofOMNI-25629
	 */
	private static YFCElement modifyUEOutput(HashMap<String, Map<String, List<String>>> itemToSupplyMap,
			YFCElement rootEle, Map<String, String> mapShipNodeType, Map<String, String> mapParentOrg) {
		log.beginTimer("modifyUEOutput");
		log.debug("inside modifyUEOutput");
		if (log.isDebugEnabled())
			log.debug("Input Element in modifyUEOutput() Method : " + rootEle);

		YFCNodeList<YFCElement> nlInpItems = rootEle.getElementsByTagName(AcademyConstants.ITEM);

		for (int j = 0; j < nlInpItems.getLength(); j++) {

			YFCElement itemEleFromInp = (YFCElement) nlInpItems.item(j);
			String inpItemID = itemEleFromInp.getAttribute(AcademyConstants.ITEM_ID);
			/*
			 * OMNI- 25629 : Start Change - Fetching the first & last ship date from the
			 * item element
			 */
			String firstShipDate = itemEleFromInp.getAttribute(AcademyConstants.A_FIRST_SHIP_DATE);
			String lastShipDate = itemEleFromInp.getAttribute(AcademyConstants.A_LAST_SHIP_DATE);
			/* OMNI- 25629 : End Change */
			if (log.isDebugEnabled())
				log.debug("itemEleFromInp :: " + itemEleFromInp);

			String orderQty = itemEleFromInp.getAttribute(AcademyConstants.ATTR_TOTAL_REQ_QTY);
			int totalQty = (int) Float.parseFloat(orderQty);

			YFCElement Supplies = itemEleFromInp.createChild(AcademyConstants.ELE_SUPPLIES);

			if (log.isDebugEnabled())
				log.debug("orderLineQty :: " + totalQty);

			String orgCode = itemEleFromInp.getAttribute(AcademyConstants.ORGANIZATION_CODE);

			if (log.isDebugEnabled())
				log.debug("inpItemID :: " + inpItemID);
			log.debug("orgCode :: " + orgCode);

			YFCNodeList<YFCElement> inpShipNode = itemEleFromInp.getElementsByTagName(AcademyConstants.SHIP_NODE);

			Map<String, List<String>> yihStoresSupplyCol = itemToSupplyMap.get(inpItemID);
			log.debug("yihStoresSupplyCol:" + yihStoresSupplyCol);

			if (log.isDebugEnabled())
				log.debug("yihStoresSupplyCol :: " + yihStoresSupplyCol);

			if (yihStoresSupplyCol != null && yihStoresSupplyCol.size() > 0) {

				for (int cnt = 0; cnt < inpShipNode.getLength(); cnt++) {
					YFCElement inpSupply = (YFCElement) inpShipNode.item(cnt);
					log.debug("inpSupply:" + inpSupply);
					String shipNode = inpSupply.getAttribute(AcademyConstants.SHIP_NODE);

					if (log.isDebugEnabled())
						log.debug("shipNode : " + shipNode);

					List<String> yihStoreATPAndDate = yihStoresSupplyCol.get(shipNode);
					if (log.isDebugEnabled())
						log.debug("yihStoreATPAndDate : " + yihStoreATPAndDate);
					log.debug("yihStoreATPAndDate:" + yihStoreATPAndDate);

					if (yihStoreATPAndDate != null && !yihStoreATPAndDate.isEmpty()) {

						for (int i = 0; i < yihStoreATPAndDate.size(); i = i + 2) {
							YFCElement eleSupply = Supplies.createChild(AcademyConstants.E_SUPPLY);
							eleSupply.setAttribute(AcademyConstants.SHIP_NODE, shipNode);
							eleSupply.setAttribute(AcademyConstants.ATTR_QUANTITY, yihStoreATPAndDate.get(i));
							/*
							 * OMNI- 25629 : Start Change - Setting the first & last ship date fetched from
							 * the item element
							 */

							eleSupply.setAttribute(AcademyConstants.A_FIRST_SHIP_DATE, firstShipDate);
							eleSupply.setAttribute(AcademyConstants.A_LAST_SHIP_DATE, lastShipDate);
							// Method invoked for setting the organization code for DSV node
							if (mapShipNodeType.get(shipNode).equals(AcademyConstants.DROP_SHIP_NODE_TYPE)) {
								setOrgCodeForDSVNode(itemEleFromInp, mapParentOrg, shipNode);
							}
							/* OMNI-25629 : End Change */

						}
					} else {
						SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.US);
						String currentDate = YantriksCommonUtil.getCurrentDateOrTimeStamp(formatter);
						YFCElement eleSupply = Supplies.createChild(AcademyConstants.E_SUPPLY);
						eleSupply.setAttribute(AcademyConstants.SHIP_NODE, shipNode);
						eleSupply.setAttribute(AcademyConstants.ATTR_QUANTITY, AcademyConstants.ATTR_ZERO);
						/*
						 * OMNI- 25629 : Start Change - Setting the first & last ship date fetched from
						 * the item element
						 */
						eleSupply.setAttribute(AcademyConstants.A_FIRST_SHIP_DATE, firstShipDate);
						eleSupply.setAttribute(AcademyConstants.A_LAST_SHIP_DATE, lastShipDate);
						// Method invoked for setting the organization code for DSV node
						if (mapShipNodeType.get(shipNode).equals(AcademyConstants.DROP_SHIP_NODE_TYPE)) {
							setOrgCodeForDSVNode(itemEleFromInp, mapParentOrg, shipNode);
						}
						/* OMNI- 25629 :End Change */
					}
				}

			}

		}

		if (log.isDebugEnabled()) {
			log.debug("Updated output :: " + rootEle);
		}

		log.endTimer("modifyUEOutput");

		return rootEle;
	}

	/* OMNI- 25629 : Start Change */
	// Method used for stamping OrgCode and OwnerOrgCode appropriately for DSV nodes
	// */
	private static void setOrgCodeForDSVNode(YFCElement itemEleFromInp, Map<String, String> mapParentOrg,
			String shipNode) {
		log.beginTimer("setOrgCodeForDSVNode");
		itemEleFromInp.setAttribute(AcademyConstants.ORGANIZATION_CODE, mapParentOrg.get(shipNode));
		itemEleFromInp.setAttribute(AcademyConstants.OWNER_ORGANIZATION_CODE, "");
		log.endTimer("setOrgCodeForDSVNode");
	}

	/* OMNI- 25629 : End Change */

	/*
	 * Method used for forming an product to ATP map from the getavailability API
	 * response. This map is then used in the modifyUEOutput method for setting the
	 * supplies accordingly in the UE output - Existing method with modified
	 * arguments added as part of OMNI-25629
	 */
	private static HashMap<String, Map<String, List<String>>> formItemSupplyCol(YFSEnvironment env,String output,
			Map<String, Integer> itemToRequiredQtyMap, Document outGetOrderList, String strFulfillmentType,
			Document docReservationInput, Document inpDoc)
			throws ParserConfigurationException, FactoryConfigurationError {
		log.beginTimer("formItemSupplyCol");

		log.debug("inside formITemSupplyCol");
		log.debug("Inside formItemSupplyCol");
		log.debug("Env obj::  " + strFulfillmentType);
		// Product supply map with date and quantity
		HashMap<String, Map<String, List<String>>> productItemToSupplyMap = new HashMap<>();

		if (log.isDebugEnabled())
			log.debug("YIH API output JSON : " + output.toString());
		Object ObjSaveYntrxResponse=  env.getTxnObject("SaveYntrxResponse");
		if(!YFCCommon.isVoid(ObjSaveYntrxResponse)) {
			boolean bSaveYantriksREsponse = (boolean)ObjSaveYntrxResponse;
			if(bSaveYantriksREsponse) {
				env.setTxnObject("YntrxResponse", output);
			}
		}
		// map for storing item id and FT
		Map<String, String> itemToFulfillmentTypeMap = new HashMap<>();
		/* OMNI- 25629 : Start Change */
		boolean bIsFulfillmentTypeEmpty = false;
		// Code to fetch FT of the orderline from getOrderList output
		if (!YFCCommon.isVoid(outGetOrderList)) {
			YFCDocument outGetOrderListDoc = YFCDocument.getDocumentFor(outGetOrderList);
			YFCElement rootEleGetOrderList = outGetOrderListDoc.getDocumentElement();
			log.debug("rootEleGetOrderList: " + rootEleGetOrderList);
			YFCNodeList<YFCElement> norderline = rootEleGetOrderList
					.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
			// Storing the map with fulfillmentType and the item id based on the delivery
			// method and fulfillment type of the orderline
			HashMap<String, String> mapOdrLineKeyFulFillmentType = (HashMap<String, String>)env.getTxnObject("OrderLineKey_FulfillmentType");
			HashMap<String, String> mapOdrLineKeyDeliveryMethod = (HashMap<String, String>)env.getTxnObject("OrderLineKey_DeliveryMethod");
			HashMap<String, String> mapOdrLineKeyProcureFromNode = (HashMap<String, String>)env.getTxnObject("OrderLineKey_ProcureFromNode");
			for (int i = 0; i < norderline.getLength(); i++) {
				String fulfillmentType = "";
				YFCElement currentOrderLine = (YFCElement) norderline.item(i);
				String strOrderLineKey = currentOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
				String strDeliveryMethod = null;
				String strPrcoureFromNode = null;
				String orderLineFulfillmentType = null;
				if(!YFCCommon.isVoid(mapOdrLineKeyFulFillmentType) && mapOdrLineKeyFulFillmentType.containsKey(strOrderLineKey)) {
					orderLineFulfillmentType =  mapOdrLineKeyFulFillmentType.get(strOrderLineKey);
					log.verbose("FulfillmentType :: "+ orderLineFulfillmentType);
				}else {
					orderLineFulfillmentType = currentOrderLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
				}
				if(!YFCCommon.isVoid(mapOdrLineKeyDeliveryMethod) && mapOdrLineKeyDeliveryMethod.containsKey(strOrderLineKey)) {
					strDeliveryMethod =  mapOdrLineKeyDeliveryMethod.get(strOrderLineKey);
					log.verbose("DeliveryMethod :: "+ strDeliveryMethod);
				}else {
					strDeliveryMethod = currentOrderLine.getAttribute(AcademyConstants.ATTR_DEL_METHOD);
				}
				if(!YFCCommon.isVoid(mapOdrLineKeyProcureFromNode) && mapOdrLineKeyProcureFromNode.containsKey(strOrderLineKey)) {
					strPrcoureFromNode =  mapOdrLineKeyProcureFromNode.get(strOrderLineKey);
					log.verbose("ProcureFromNode :: "+ strPrcoureFromNode);
				}else {
					strPrcoureFromNode = currentOrderLine.getAttribute(AcademyConstants.ATTR_PROCURE_FROM_NODE);
				}
				if (!YFCCommon.isVoid(strDeliveryMethod)) {
					if (strDeliveryMethod.equals(AcademyConstants.STR_SHP)) {
						fulfillmentType = AcademyConstants.V_FULFILLMENT_TYPE_SHIP;
					} else if (strDeliveryMethod.equals(AcademyConstants.STR_PICK)
							&& orderLineFulfillmentType.equals(AcademyConstants.V_FULFILLMENT_TYPE_BOPIS)) {
						fulfillmentType = AcademyConstants.STR_PICK;
					} else if (orderLineFulfillmentType.equals(AcademyConstants.V_FULFILLMENT_TYPE_STS) 
							&& !YFCCommon.isVoid(strPrcoureFromNode)) {
						fulfillmentType = AcademyConstants.V_FULFILLMENT_TYPE_STS;
					} 
					/* STS 2.0 - Start - Check against fulfillmentType SHIP from Yantriks as new FT is not yet created at Yantriks end for STS 2.0 */
					else if (orderLineFulfillmentType.equals(AcademyConstants.V_FULFILLMENT_TYPE_STS) 
							&& YFCCommon.isVoid(strPrcoureFromNode)) {
						fulfillmentType = AcademyConstants.V_FULFILLMENT_TYPE_STSFS;
					}
					/* STS 2.0 - End */
					
				} else {
					// throw exception if Delivery method is null
					throwDeliveryMethodException();
				}

				YFCNodeList<YFCElement> eleItem = currentOrderLine.getElementsByTagName(AcademyConstants.ITEM);
				YFCElement Item = (YFCElement) eleItem.item(0);
				String itemId = Item.getAttribute(AcademyConstants.ATTR_ITEM_ID);
				itemToFulfillmentTypeMap.put(itemId, fulfillmentType);
			}
		}
		/* OMNI- 25629 : End Change */
		// OMNI-29102 Reservation Start Change
		if (!YFCCommon.isVoid(docReservationInput)) {
			Element eleReservationInput = docReservationInput.getDocumentElement();
			NodeList nlPromiseLine = eleReservationInput.getElementsByTagName(AcademyConstants.ELE_PROMISE_LINE);
			for (int i = 0; i < nlPromiseLine.getLength(); i++) {
				Element elePromiseLine = (Element) nlPromiseLine.item(i);
				String strItemId = elePromiseLine.getAttribute(AcademyConstants.ATTR_ITEM_ID);
				String strPromiseLineFulfillmentType = elePromiseLine
						.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
				if ((AcademyConstants.STR_BUY_ONLINE_PICKUP_FROM_STORE)
						.equalsIgnoreCase(strPromiseLineFulfillmentType)) {
					if (!YFCCommon.isVoid(strItemId)) {
						itemToFulfillmentTypeMap.put(strItemId, AcademyConstants.STR_PICK);
					}
				} else if ((AcademyConstants.STR_SHIP_TO_STORE).equalsIgnoreCase(strPromiseLineFulfillmentType)) {
					if (!YFCCommon.isVoid(strItemId)) {
						itemToFulfillmentTypeMap.put(strItemId, AcademyConstants.V_FULFILLMENT_TYPE_STS);
					}
				} else {
					if (!YFCCommon.isVoid(strItemId)) {
						itemToFulfillmentTypeMap.put(strItemId, AcademyConstants.V_FULFILLMENT_TYPE_SHIP);
					}
				}
			}
			log.debug("Reservation Input Map item deatils ::" + itemToFulfillmentTypeMap);
		}
		// OMNI-29102 Reservation End Change

		try {
			// Parsing the output of get Availability
			JSONObject jResponseObj = new JSONObject(output);
			if (log.isDebugEnabled())
				log.debug("jResponseObj :: " + jResponseObj);

			// store the product array
			JSONArray availByProductsArray = jResponseObj
					.getJSONArray(AcademyConstants.JSON_ATTR_AVAILABILITY_BY_PRODUCTS); // itemAvailabilityDetails
			if (log.isDebugEnabled())
				log.debug("availByProductsArray :: " + availByProductsArray.toString());

			for (int k = 0; k < availByProductsArray.length(); k++) {
				JSONObject currentProductAvail = availByProductsArray.getJSONObject(k);
				String productId = currentProductAvail.getString(AcademyConstants.JSON_ATTR_PRODUCT_ID);
				if (log.isDebugEnabled())
					log.debug("ProductId :: " + productId);

				if (log.isDebugEnabled())
					log.debug("currentProductAvail :: " + currentProductAvail);

				Map<String, Map<String, List<String>>> ftAndItsSupplies = new HashMap<>();
				Map<String, List<String>> yihSupplies = new HashMap<>();

				JSONArray availByFulfillType = currentProductAvail
						.getJSONArray(AcademyConstants.JSON_ATTR_AVAIL_BY_FF_TYPE);

				for (int i = 0; i < availByFulfillType.length(); i++) {

					JSONObject currAvailByFulfillType = new JSONObject();
					for (int j = 0; j < availByFulfillType.length(); j++) {
						/* OMNI- 25629 : Start Change */
						// Fetch the availableByFulfillmentType JSON from the response applicable only
						// for the fulfillment type of the item id
						log.debug("inside for loop");
						log.debug("Map values of itemToFulfillmentTypeMap ::: " + itemToFulfillmentTypeMap);
						String productFulfilmenttype = itemToFulfillmentTypeMap.get(productId);
						// OMNI-29101 FindInventory Start Change
						if (YFCCommon.isVoid(productFulfilmenttype) && !YFCCommon.isVoid(strFulfillmentType)) {
							if ((AcademyConstants.STR_BUY_ONLINE_PICKUP_FROM_STORE)
									.equalsIgnoreCase(strFulfillmentType)) {
								productFulfilmenttype = AcademyConstants.V_FULFILLMENT_TYPE_PICK;
							} else if ((AcademyConstants.STR_SHIP_TO_STORE).equalsIgnoreCase(strFulfillmentType)) {
								productFulfilmenttype = AcademyConstants.V_FULFILLMENT_TYPE_STS;
							}
						}
						// OMNI-29101 FindInventory End Change
						JSONObject getFulfillTypeList = availByFulfillType.getJSONObject(j);

						String fulfillmentType = getFulfillTypeList
								.getString(AcademyConstants.JSON_ATTR_FULFILLMENT_TYPE);
						log.debug("fulfillmentType ::" + fulfillmentType);
						log.debug("productFulfilmenttype ::" + productFulfilmenttype);
						if (fulfillmentType.equalsIgnoreCase(productFulfilmenttype)
								|| fulfillmentType.equalsIgnoreCase(strFulfillmentType)) {
							currAvailByFulfillType = availByFulfillType.getJSONObject(j);
						}
					}
					/* OMNI- 25629 : End Change */

					if (!currAvailByFulfillType.isEmpty()) {// Added this if condition for handling where the
															// fulfillment type is not available in the response as part
															// of OMNI- 25629
						JSONArray availByDetails = currAvailByFulfillType
								.getJSONArray(AcademyConstants.JSON_ATTR_AVAIL_BY_DETAILS);
						JSONObject availByDetailsFirstObj = availByDetails.getJSONObject(0);
						JSONArray availabilityByLocations = availByDetailsFirstObj
								.getJSONArray(AcademyConstants.JSON_ATTR_AVAIL_BY_LOCATIONS);

						for (int j = 0; j < availabilityByLocations.length(); j++) {
							List<String> atpAndDate = new ArrayList<>();
							JSONObject currAvailByLocations = availabilityByLocations.getJSONObject(j);
							String location = currAvailByLocations.getString(AcademyConstants.YIH_LOCATION_ID);

							int atp = currAvailByLocations.getInt(AcademyConstants.JSON_ATTR_ATP);

							SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.US);
							String currentDate = YantriksCommonUtil.getCurrentDateOrTimeStamp(formatter);
							if (log.isDebugEnabled())
								log.debug("Current Date :: " + currentDate);
							if (atp != 0) {
								atpAndDate.add(String.valueOf(atp));
								atpAndDate.add(currentDate);

							}
							JSONArray futureDateArray = currAvailByLocations
									.getJSONArray(AcademyConstants.JSON_ATTR_FUTURE_QTY_DATES);

							if (futureDateArray.length() != 0) {
								for (int l = 0; l < futureDateArray.size(); l++) {
									JSONObject currentFutureDateObj = futureDateArray.getJSONObject(l);

									int fQty = currentFutureDateObj.getInt(AcademyConstants.JSON_ATTR_FUTURE_QTY);

									String futureDate = currentFutureDateObj
											.getString(AcademyConstants.JSON_ATTR_FUTURE_QTY_DATE);
									if (fQty != 0) {
										atpAndDate.add(String.valueOf(fQty));
										atpAndDate.add(futureDate);

									}
								}
							}
							yihSupplies.put(location, atpAndDate);

							if (log.isDebugEnabled())
								log.debug("Current yihSupplies :: " + yihSupplies.toString());
						}
					} else {
						bIsFulfillmentTypeEmpty = true;
					}
				}

				productItemToSupplyMap.put(productId, yihSupplies);
			}
			if (log.isDebugEnabled())
				log.debug("yihItemToSupplyMap :: " + productItemToSupplyMap);

			log.debug("itemToFulfillmetTypeMap:" + itemToFulfillmentTypeMap);
			log.debug("productItemToSupplyMap :: " + productItemToSupplyMap);

			if (bIsFulfillmentTypeEmpty) {
				log.debug("Setting supply as zero since Fulfillment type is not present");
				setSupplyAsZero(inpDoc, null, null);
			}

		} catch (NumberFormatException | JSONException e) {
			log.error("Exception :: " + e);
			YFSException yfsException = new YFSException();
			yfsException.setErrorDescription("Exception Caught while calling availability API" + e.getMessage());
			String strErrorCode = AcademyConstants.GET_AVAILABILITY_EXP;
			yfsException.setErrorCode(strErrorCode);
			throw yfsException;
		}
		log.endTimer("formItemSupplyCol");

		return productItemToSupplyMap;

	}

	private static void throwDeliveryMethodException() {
		YFSException yfsException = new YFSException();
		String exceptionId = AcademyConstants.DELIVERY_METHOD_NULL_EXP;
		yfsException.setErrorDescription(exceptionId);
		throw yfsException;

	}

	//
	/*
	 * private void setSupplyAsOrderedQtyForBOPUS(Document inpDoc, Set<String>
	 * bopusItems) { log.beginTimer("setSupplyAsOrderedQtyForBOPUS"); YFCDocument
	 * yfcInpDoc = YFCDocument.getDocumentFor(inpDoc); if (log.isDebugEnabled())
	 * log.debug("Input Document in setSupplyAsOrderedQtyForBOPUS :: " + yfcInpDoc);
	 * YFCElement rootEle = yfcInpDoc.getDocumentElement();
	 * 
	 * YFCNodeList nlInpItems = rootEle.getElementsByTagName(AcademyConstants.ITEM);
	 * 
	 * for (int j = 0; j < nlInpItems.getLength(); j++) {
	 * 
	 * YFCElement itemEleFromInp = (YFCElement) nlInpItems.item(j); String inpItemID
	 * = itemEleFromInp.getAttribute(AcademyConstants.A_ITEM_ID); if
	 * (bopusItems.contains(inpItemID)) { if (log.isDebugEnabled())
	 * log.debug("itemEleFromInp :: " + itemEleFromInp);
	 * 
	 * String orderQty =
	 * itemEleFromInp.getAttribute(AcademyConstants.ATTR_TOTAL_REQ_QTY); int
	 * totalQty = (int) Float.parseFloat(orderQty);
	 * 
	 * YFCElement Supplies =
	 * itemEleFromInp.createChild(AcademyConstants.E_SUPPLIES); if
	 * (log.isDebugEnabled()) log.debug("orderLineQty :: " + totalQty);
	 * 
	 * String orgCode =
	 * itemEleFromInp.getAttribute(AcademyConstants.A_ORGANIZATION_CODE); if
	 * (log.isDebugEnabled()) log.debug("inpItemID :: " + inpItemID);
	 * log.debug("orgCode :: " + orgCode);
	 * 
	 * YFCNodeList inpShipNode =
	 * itemEleFromInp.getElementsByTagName(AcademyConstants.SHIP_NODE);
	 * 
	 * for (int cnt = 0; cnt < inpShipNode.getLength(); cnt++) { YFCElement
	 * eleSupply = Supplies.createChild(AcademyConstants.E_SUPPLY); YFCElement
	 * inpSupply = (YFCElement) inpShipNode.item(cnt);
	 * 
	 * String shipNode = inpSupply.getAttribute(AcademyConstants.SHIP_NODE);
	 * 
	 * if (log.isDebugEnabled()) log.debug("shipNode : " + shipNode);
	 * 
	 * eleSupply.setAttribute(AcademyConstants.SHIP_NODE, shipNode);
	 * eleSupply.setAttribute(AcademyConstants.A_FIRST_SHIP_DATE, "19420101");
	 * eleSupply.setAttribute(AcademyConstants.A_LAST_SHIP_DATE, "25000101");
	 * eleSupply.setAttribute(AcademyConstants.ATTR_QUANTITY,
	 * itemEleFromInp.getAttribute(AcademyConstants.ATTR_TOTAL_REQ_QTY)); //
	 * eleSupply.setAttribute(PTConstants.A_SOFT_ASSIGN_QTY, yihStoreAtp);
	 * 
	 * if (log.isDebugEnabled()) log.debug("Supply Element :: " +
	 * eleSupply.toString());
	 * 
	 * } } } if (log.isDebugEnabled()) {
	 * log.debug("Output Document from setSupplyAsOrderedQtyForBOPUS() :: " +
	 * yfcInpDoc.toString()); } log.endTimer("setSupplyAsOrderedQtyForBOPUS"); }
	 */
	
	
	/** OMNI-51287 BEGIN
	 * 
	 * getAvailability issue-duplicate lines in request to Yantriks
	 */
	public static JSONObject removeDuplicates(JSONObject myJsonObject) throws JSONException {
		log.debug("Removing Duplicates from JSON");

		JSONArray products = myJsonObject.getJSONArray(AcademyConstants.JSON_ATTR_PRODUCTS);
		int length =products.length();
		log.debug("Length is " + length);
		Set<String> setOfItemIDs = new HashSet<String>();
		for (int i = 0; i < length; i++) {
			JSONObject currentProduct = products.getJSONObject(i);
			String strItemId = currentProduct.getString(AcademyConstants.JSON_ATTR_PRODUCT_ID);
			if (setOfItemIDs.contains(strItemId)) {
				products.remove(i);
				length--;
				i--;
			} else {
				setOfItemIDs.add(strItemId);
			}
		}
		return myJsonObject;
	}
	// OMNI-51287 END
}
