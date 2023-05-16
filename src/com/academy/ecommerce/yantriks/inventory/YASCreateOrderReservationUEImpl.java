package com.academy.ecommerce.yantriks.inventory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.json.JSONArray;
import org.apache.commons.json.JSONException;
import org.apache.commons.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.academy.util.common.AcademyExceptionAlert;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantriks.yih.adapter.util.YantriksCommonUtil;
import com.yantriks.yih.adapter.util.YantriksConstants;


public class YASCreateOrderReservationUEImpl {

	private static YFCLogCategory log = YFCLogCategory.instance(YASCreateOrderReservationUEImpl.class);
	private static Properties props;

	/**
	 * 
	 * 1.This method will receive order xml as i/p and map it to required json with updated demand type and fulfillment type.
	 * 2.Yantrik's api will be called to extend TTL and order reservation.
	 * 
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception 
	 */

	public Document createReservationPayloadFromGetOrderList(YFSEnvironment env, Document inDoc)
			throws Exception {
		String methodName = "createReservationPayloadFromGetOrderList";
		log.beginTimer(methodName);
		if (log.isDebugEnabled())
			log.debug("Input createReservationPayloadFromGetOrderList : " + methodName + " : "
					+ SCXmlUtil.getString(inDoc));
		Element eleRoot = inDoc.getDocumentElement(); 
		String isAllocated = eleRoot.getAttribute(AcademyConstants.STR_ALLOCATED); 
		log.debug("isAllocated:"+isAllocated);
		Document getOrderListIP = SCXmlUtil.createDocument(AcademyConstants.ELE_ORDER);
		getOrderListIP.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,
				eleRoot.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));
		

		if (log.isDebugEnabled())
			log.debug("Input to getOrderList :: " + SCXmlUtil.getString(getOrderListIP));
		Document outGetOrderList = YantriksCommonUtil.invokeAPI(env, AcademyConstants.TEMP_RECONCILE_GET_ORDER_LIST,
				AcademyConstants.API_GET_ORDER_LIST, getOrderListIP);
		
		if (log.isDebugEnabled())
			log.debug("Output of getOrderList :: " + SCXmlUtil.getString(outGetOrderList));
		
		//OMNI - 52851 Start changes - Demand update is going as SCHEDULE_TO for all lines
		Document outGetShipmentListForOrder=getShipmentListForOrder(env, eleRoot.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));
		//OMNI - 52851 End  changes - Demand update is going as SCHEDULE_TO for all lines

		Map<String, Element> primeLineToOrderLineMap = buildPrimeLineToOrderLineMap(outGetOrderList);

		JSONObject requestJson = new JSONObject();
		JSONArray lineReservationDetailsArray = new JSONArray();
		Element eleOrder = SCXmlUtil.getChildElement(outGetOrderList.getDocumentElement(), AcademyConstants.ELE_ORDER);
		String orderHeaderKey = eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
		log.debug("orderHeaderKey :: " + orderHeaderKey);
		// OMNI-48035 BEGIN
		String strModifyTsOfOrder= eleOrder.getAttribute(AcademyConstants.ATTR_MODIFY_TS);
		// OMNI-48035 END
		String orgId = AcademyConstants.ORG_ID_ACADEMY;
		String reservationId = YantriksCommonUtil.getReservationID(eleOrder);
		Element eleOrderLines = SCXmlUtil.getChildElement(eleOrder, AcademyConstants.ELE_ORDER_LINES);
		NodeList nlOrderLines = eleOrderLines.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
		populateRootLevelJSONAttributes(requestJson, orgId, AcademyConstants.V_OMS, reservationId, 0,
				AcademyConstants.YIH_TIME_UNIT_MINUTES,env,strModifyTsOfOrder,isAllocated);

		int orderLineLength = nlOrderLines.getLength();
		for (int i = 0; i < orderLineLength; i++) {
			Element currInOrderLine = (Element) nlOrderLines.item(i);
			Element orderLineEle = primeLineToOrderLineMap
					.get(currInOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
			String strDeliveryMethod = orderLineEle.getAttribute(AcademyConstants.ATTR_DELIVERY_METHOD);
			String fulfillmentType = orderLineEle.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
			String primeLineNo = orderLineEle.getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO);
			Element eleItem = SCXmlUtil.getChildElement(orderLineEle, AcademyConstants.ITEM);

			log.debug("DeliveryMethod :: " + strDeliveryMethod);
			log.debug("fulfillmentType :: " + fulfillmentType);

			Element eleSchedules = SCXmlUtil.getChildElement(orderLineEle, AcademyConstants.ELE_SCHEDULES);
			NodeList nlSchedules = eleSchedules.getElementsByTagName(AcademyConstants.ELE_SCHEDULE);
			int scheduleLength = nlSchedules.getLength();
			Element eleOrderStatuses = SCXmlUtil.getChildElement(orderLineEle, AcademyConstants.ELE_ORDER_STATUSES);
			NodeList nlOrderStatuses = eleOrderStatuses.getElementsByTagName(AcademyConstants.ELE_ORDER_STATUS);
			String lineStatus = ((Element) nlOrderStatuses.item(0)).getAttribute(AcademyConstants.ATTR_STATUS);
			List<String> lineStatues = new ArrayList<String>();

			int orderStatusesLength = nlOrderStatuses.getLength();
			Map<String, Map<String, Map<String, Integer>>> mapShipNodeDateToDemandQty = new HashMap<>();
			Map<String, Element> mapScheduleKeytoSchedules = new HashMap<>();
			for (int j = 0; j < scheduleLength; j++) {
				Element currSchedule = (Element) nlSchedules.item(j);
				String orderLineScheduleKey = currSchedule.getAttribute(AcademyConstants.ATTR_ORDER_LINE_SCHEDULE_KEY);
				mapScheduleKeytoSchedules.put(orderLineScheduleKey, currSchedule);
			}
			String procureFromNode = "";
			//OMNI - 52851 Start changes - Demand update is going as SCHEDULE_TO for all lines
			String sOrderLineKey=currInOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
			String sLineShipmentStatus = XMLUtil.getString(outGetShipmentListForOrder,"/ShipmentList/Shipment[./ShipmentLines/ShipmentLine[@ChainedFromOrderLineKey='" + sOrderLineKey + "']]/@Status");
			//OMNI - 52851 End changes - Demand update is going as SCHEDULE_TO for all lines
			for (int j = 0; j < orderStatusesLength; j++) {
				Element currOrderStatus = (Element) nlOrderStatuses.item(j);
				if (log.isDebugEnabled())
					log.debug("Current Order Status Element : " + SCXmlUtil.getString(currOrderStatus));
				String orderLineScheduleKey = currOrderStatus.getAttribute(AcademyConstants.ATTR_ORDER_LINE_SCHEDULE_KEY);
				if (null == mapScheduleKeytoSchedules.get(orderLineScheduleKey)) {
					continue;
				}
				Element eleRespectiveSchedule = mapScheduleKeytoSchedules.get(orderLineScheduleKey);
				if (log.isDebugEnabled())
					log.debug("Fetched Order Schedule Element : " + SCXmlUtil.getString(eleRespectiveSchedule));
				String shipNode = eleRespectiveSchedule.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
				procureFromNode = eleRespectiveSchedule.getAttribute(AcademyConstants.ATTR_PROCURE_FROM_NODE);
				String statusQty = eleRespectiveSchedule.getAttribute(AcademyConstants.ATTR_QUANTITY);
				int intStatusQty = (int) Double.parseDouble(statusQty);
				String status = currOrderStatus.getAttribute(AcademyConstants.ATTR_STATUS);
				lineStatues.add(status);
				String demandType = YantriksCommonUtil.deriveDemandTypeFromStatus(env,status,shipNode,procureFromNode,fulfillmentType,sLineShipmentStatus);// Modified the method arguments to include ShipNode, procureFromNode and fulfillmentType as part of OMNI-40029
				/*OMNI-52209: Start Change - Read the service argument to determine if demand update to be published during backorder or not*/
				String enableDmndUpdtOnBackOrder=props.getProperty(YantriksConstants.ENABLE_DMD_UPDT_ON_BACKORDER);
				String shipNodeForDemand = YantriksCommonUtil.deriveShipNodeBasedOnDemandStatus(shipNode,
						procureFromNode, status,enableDmndUpdtOnBackOrder);
				/*OMNI-52209: End Change */
				if ("".equals(shipNodeForDemand)) {
					shipNodeForDemand = AcademyConstants.V_NETWORK;
				}
				String expectedShipDate = eleRespectiveSchedule.getAttribute(AcademyConstants.ATTR_EXPECTED_SHIP_DATE);
				String reservationDateToPut = YantriksCommonUtil.convertShipDateToUTC(expectedShipDate);
				SimpleDateFormat formatter = new SimpleDateFormat(AcademyConstants.YAS_TIME_STAMP_FORMAT, Locale.US);
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

			JSONObject lineReservationDetailsObj = new JSONObject();
			lineReservationDetailsObj.put(AcademyConstants.YIH_LINE_ID, primeLineNo);
			lineReservationDetailsObj.put(AcademyConstants.JSON_ATTR_PRODUCT_ID,
					eleItem.getAttribute(AcademyConstants.ATTR_ITEM_ID));
			lineReservationDetailsObj.put(AcademyConstants.JSON_ATTR_UOM, eleItem.getAttribute(AcademyConstants.ATTR_UOM));
			JSONArray locationArray = new JSONArray();


			/**
			 * OMNI-16736, Start change
			 * If condition added to set fulfillment type and location id, loc type if order
			 * line status is 1100
			 */
			// OMNI-50402 - Start - STS 2.0 Demand update during scheduling for updated locationOMNI-50402 - Start - STS 2.0 Demand update during scheduling for updated location
			String strProcureFromNode = orderLineEle.getAttribute(AcademyConstants.ATTR_PROCURE_FROM_NODE);
			//OMNI-50402 - End
			
			if (lineStatus.equals(AcademyConstants.V_STATUS_1100)&& !fulfillmentType.equals(AcademyConstants.FULFILLMENT_TYPE_EGC)) {
				String statusQty = ((Element) nlOrderStatuses.item(0)).getAttribute(AcademyConstants.A_STATUS_QTY);
				int intStatusQty = 0;
				if (!YFCCommon.isVoid(statusQty)) {
					intStatusQty = (int) Double.parseDouble(statusQty);
				}
				String orderLineFulfillmentType = "";
				String locationId = "";
				String locationType = "";
				String procureFromShipNode="";
				String demandType = YantriksCommonUtil.deriveDemandTypeFromStatus(env,lineStatus,locationId,procureFromShipNode,orderLineFulfillmentType,null);// Modified the method arguments to include ShipNode, procureFromNode and orderLineFulfillmentType as part of OMNI-40029


				if (!YFCCommon.isVoid(strDeliveryMethod)) {
					if (strDeliveryMethod.equals(AcademyConstants.STR_SHP)) {
						orderLineFulfillmentType = AcademyConstants.V_FULFILLMENT_TYPE_SHIP;
						locationId = AcademyConstants.V_NETWORK;
						locationType = AcademyConstants.V_NETWORK;

					} else if (strDeliveryMethod.equals(AcademyConstants.STR_PICK)
							&& fulfillmentType.equals(AcademyConstants.V_FULFILLMENT_TYPE_BOPIS)) {

						orderLineFulfillmentType = AcademyConstants.STR_PICK;
						locationId = orderLineEle.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
						locationType = YantriksCommonUtil.getLocationType(env, locationId);

					} 
					/*
					 * Code Changes for OMNI-45550 
					 * Code Change for STS-2.0 to identify STSFS line
					 * PROCURE_FROM_NODE should be blank and FULFILLMENT_TYPE should be STS
					 * 
					 * STS-1.0 to identify STS line PROCURE_FROM_NODE should NOT be blank and
					 * FULFILLMENT_TYPE should be STS
					 */
					else if (fulfillmentType.equals(AcademyConstants.V_FULFILLMENT_TYPE_STS)) {
						if (YFCCommon.isVoid(strProcureFromNode)) {
							orderLineFulfillmentType = AcademyConstants.V_FULFILLMENT_TYPE_STSFS;
							locationId = AcademyConstants.V_NETWORK;
							locationType = AcademyConstants.V_NETWORK;
						} else {
							orderLineFulfillmentType = AcademyConstants.V_FULFILLMENT_TYPE_STS;
							locationId = orderLineEle.getAttribute(AcademyConstants.ATTR_PROCURE_FROM_NODE);
							locationType = YantriksCommonUtil.getLocationType(env, locationId);
						}
					}
					//OMNI-45550 - End
				} else {
					// throw exception if Delivery method is null
					throwException();
				}
				lineReservationDetailsObj.put(AcademyConstants.JSON_ATTR_FULFILLMENT_TYPE, orderLineFulfillmentType);
				locationArray = prepareLocObj(locationId, locationType, demandType, intStatusQty);

				
				// OMNI- 16736, End change
			} else if (!lineStatus.equals(AcademyConstants.V_STATUS_1100) && !fulfillmentType.equals(AcademyConstants.FULFILLMENT_TYPE_EGC)) {
				// OMNI - 25631, start change
				if (!YFCCommon.isVoid(strDeliveryMethod)) {
					if (strDeliveryMethod.equals(AcademyConstants.STR_SHP)) {
						lineReservationDetailsObj.put(AcademyConstants.JSON_ATTR_FULFILLMENT_TYPE,
								AcademyConstants.V_FULFILLMENT_TYPE_SHIP);

					} else if (strDeliveryMethod.equals(AcademyConstants.STR_PICK)
							&& fulfillmentType.equals(AcademyConstants.V_FULFILLMENT_TYPE_BOPIS)) {

						lineReservationDetailsObj.put(AcademyConstants.JSON_ATTR_FULFILLMENT_TYPE,
								AcademyConstants.V_FULFILLMENT_TYPE_PICK);

					} 
					

					/*
					 * OMNI-50402 - Start - STS 2.0 Demand update during scheduling for updated
					 * location 
					 * 
					 * STS will have PICK Delivery Method
					 * 
					 * STS-2.0 to identify STSFS line PROCURE_FROM_NODE should be blank and
					 * FULFILLMENT_TYPE should be STS
					 * 
					 * STS-1.0 to identify STS line PROCURE_FROM_NODE should NOT be blank and
					 * FULFILLMENT_TYPE should be STS
					 * 
					 * 2160.01 - Shipped To Store, 2160.01.100 - Arrived at Store, 3350.400 - Ready
					 * for Customer Pick up, 2160.70.06.10 - Received at Store, 3200 - Released,
					 * 3350 - Included In Shipment, 3700.100 - Picked up by Customer(Added as part of OMNI-59114) - for these SO statuses Fulfillment Type is PICK
					 * same as STS 1.0
					 * 
					 * sts2.0 - If shipment status is Ready to ship to store, set fulfillment type as pick
					 * 
					 */
					else if (fulfillmentType.equals(AcademyConstants.V_FULFILLMENT_TYPE_STS)) {
						if ((!YFCCommon.isVoid(sLineShipmentStatus)&& sLineShipmentStatus.equalsIgnoreCase("1100.70.06.30")) 
								|| lineStatues.contains(AcademyConstants.V_STATUS_2160_01) || lineStatues.contains(AcademyConstants.V_STATUS_2160_01_100) 
								|| lineStatues.contains(AcademyConstants.V_STATUS_3350_400) || lineStatues.contains(AcademyConstants.V_STATUS_2160_70_06_10) 
								|| lineStatues.contains(AcademyConstants.V_STATUS_3200) ||  lineStatues.contains(AcademyConstants.V_STATUS_3350)
								|| lineStatues.contains(YantriksConstants.V_STATUS_3700_100)) {
							//2160.70.06.10 (Received In Store), SO Released, SO Included In Shipment fulfillment type to be sent is PICK 
							lineReservationDetailsObj.put(AcademyConstants.JSON_ATTR_FULFILLMENT_TYPE, AcademyConstants.V_FULFILLMENT_TYPE_PICK);
							requestJson.put(AcademyConstants.JSON_ATTR_SELLING_CHANNEL, AcademyConstants.SELLING_CHANNEL_GLOBAL);
						} else if (YFCCommon.isVoid(strProcureFromNode)) {
							lineReservationDetailsObj.put(AcademyConstants.JSON_ATTR_FULFILLMENT_TYPE,
									AcademyConstants.V_FULFILLMENT_TYPE_STSFS);
						} else {
							lineReservationDetailsObj.put(AcademyConstants.JSON_ATTR_FULFILLMENT_TYPE, AcademyConstants.V_FULFILLMENT_TYPE_STS);
						}
					}
					// OMNI-50402 - End
					
				} else {
					// throw exception if Delivery method is null
					throwException();
				}
				for (Map.Entry<String, Map<String, Map<String, Integer>>> shipNodeDemandQtyEntry : mapShipNodeDateToDemandQty
						.entrySet()) {
					String shipNode = shipNodeDemandQtyEntry.getKey();
					Map<String, Map<String, Integer>> reservationToDemandQtyEntry = shipNodeDemandQtyEntry.getValue();
					JSONObject locationObj = new JSONObject();
					String locationType = "";
					if(!shipNode.equals(AcademyConstants.V_NETWORK)) {
						 locationType = YantriksCommonUtil.getLocationType(env, shipNode);
					}else {
						locationType = AcademyConstants.V_NETWORK;
					}
					String segment = AcademyConstants.CONST_DEFAULT;
					
					JSONArray demandsArray = new JSONArray();
					String demand_Type = "";
					for (Map.Entry<String, Map<String, Integer>> dateToDemandEntry : reservationToDemandQtyEntry
							.entrySet()) {
						String reservationDate = dateToDemandEntry.getKey();
						Map<String, Integer> demandAndQtyEntry = dateToDemandEntry.getValue();
						for (Map.Entry<String, Integer> entry : demandAndQtyEntry.entrySet()) {
							String demandType = entry.getKey();
							demand_Type = demandType;
							int quantity = entry.getValue();
							if (!AcademyConstants.DT_DEFAULT_IGNORE.equals(demandType)) {
								log.verbose("Demand Type for the line "+primeLineNo+ " is "+demand_Type);
								JSONObject newDemandObj = new JSONObject();
								newDemandObj.put(AcademyConstants.JSON_ATTR_DEMAND_TYPE, demandType);
								newDemandObj.put(AcademyConstants.YIH_A_QUANTITY, quantity);
								newDemandObj.put(AcademyConstants.JSON_ATTR_SEGMENT, segment);
								log.verbose("COMMENTING RESERVATION DATE");
								//YantriksCommonUtil.putFutureOnlyReservationDate(newDemandObj, reservationDate);
								demandsArray.add(newDemandObj);
							}
						}
					}
					log.verbose("Checking BO now ");
					// OMNI - 29541, Change start
					if(!demand_Type.equals(YantriksConstants.DT_BACKORDERED)) {
						log.verbose("Demand Type NOT BACKORDERED.. Stamping LocationID and LocationType ");
						//OMNI_51881 - start changes of locationid setup for sts2.0 line
						String sCurrentLineShipNode=currInOrderLine.getAttribute("ShipNode");
						log.verbose("sCurrentLineShipNode:::"+sCurrentLineShipNode);
						String nodeType="";
						if (!YFCObject.isVoid(shipNode)) 
						nodeType=YantriksCommonUtil.getShipNodeType(env,shipNode);
						log.verbose("nodeType:"+nodeType+"procureFromNode:"+procureFromNode);
						log.verbose("sLineShipmentStatus::"+sLineShipmentStatus);
						/*if(!YFCCommon.isVoid(sLineShipmentStatus)&& sLineShipmentStatus.equalsIgnoreCase("1100.70.06.30")
								&& nodeType.equals(YantriksConstants.AT_LT_STORE) && !YFCObject.isVoid(procureFromNode) && lineStatues.contains(YantriksConstants.V_STATUS_2160_00_01)) {*/
						//Changes for OMNI-63470
						if(!YFCCommon.isVoid(sLineShipmentStatus)&& sLineShipmentStatus.equalsIgnoreCase("1100.70.06.30")
								&& nodeType.equals(YantriksConstants.AT_LT_STORE) && !YFCObject.isVoid(procureFromNode) && (lineStatues.contains(YantriksConstants.V_STATUS_2160_00_01)|| lineStatues.contains(YantriksConstants.V_STATUS_2160_00_01_200) || lineStatues.contains(YantriksConstants.V_STATUS_2160_00_01_300))) {
							log.verbose("SO shipnode for STS2.0");
							locationObj.put(AcademyConstants.YIH_LOCATION_ID, sCurrentLineShipNode); 
						}else {
					  //OMNI_51881 - End changes of locationid setup for sts2.0 line
						locationObj.put(AcademyConstants.YIH_LOCATION_ID, shipNode);
						}
						locationObj.put(AcademyConstants.JSON_ATTR_LOCATION_TYPE, locationType);
					} else {
						log.verbose("Demand Type BACKORDERED.. Stamping NETWORK as LocationID and LocationType "); 
						locationObj.put(AcademyConstants.YIH_LOCATION_ID, AcademyConstants.V_NETWORK);
						locationObj.put(AcademyConstants.JSON_ATTR_LOCATION_TYPE, AcademyConstants.V_NETWORK);
					}
					// OMNI - 29541, Change end
					locationObj.put(AcademyConstants.JSON_ATTR_DEMANDS, demandsArray);
					if (!demandsArray.isEmpty()) {
						log.verbose("Adding location obj now "+locationObj);
						locationArray.add(locationObj);
					}
				}
				/**
				 * OMNI -29603 , Start change
				 * Set Demand type as scheduled and fulfillment Type as SHIP, if fulfillment type is "EGC" and status
				 * is not cancelled & Shipped
				 */
			} else if (fulfillmentType.equals(AcademyConstants.FULFILLMENT_TYPE_EGC)){
				lineReservationDetailsObj.put(AcademyConstants.JSON_ATTR_FULFILLMENT_TYPE,
						AcademyConstants.V_FULFILLMENT_TYPE_SHIP);
				int intStatusQty = 0;
				
				if(!lineStatus.equals(YantriksConstants.V_STATUS_3700)
					&& !lineStatus.equals(AcademyConstants.VAL_CANCELLED_STATUS)) {
					String statusQty = ((Element) nlOrderStatuses.item(0)).getAttribute(AcademyConstants.A_STATUS_QTY);
					if (!YFCCommon.isVoid(statusQty)) {
						intStatusQty = (int) Double.parseDouble(statusQty);
					}
					String locationType = YantriksCommonUtil.getLocationType(env, AcademyConstants.EGC_SHIP_NODE);
					locationArray = prepareLocObj(AcademyConstants.EGC_SHIP_NODE, locationType, AcademyConstants.DT_SCHEDULED, intStatusQty);
				}
			}
			// OMNI -29603 , End change
			lineReservationDetailsObj.put(AcademyConstants.JSON_ATTR_LOCATION_RES_DETAILS, locationArray);
			lineReservationDetailsArray.add(lineReservationDetailsObj);
			// OMNI -25631 , End change

		}

		log.debug("Input formed for line reservation modification : " + requestJson.toString());
		if (!lineReservationDetailsArray.isEmpty())

		{
			log.debug("Reconciliation needed for Reservation Id : " + reservationId);
			requestJson.put(AcademyConstants.JSON_ATTR_LINE_RES_DETAILS, lineReservationDetailsArray);
		}

		log.debug("Input JSON for Resevation update for Order :: " + reservationId +" :: " + requestJson);

		invokeYIHApiAndFormReturnDoc(orgId, requestJson, 0, env, orderHeaderKey, reservationId);

		return inDoc;
	}
	
	private JSONArray prepareLocObj(String locationId, String locType, 
			String demandType, int intStatusQty ) throws JSONException {
		JSONArray locationArray = new JSONArray();
		
		JSONArray demandsArray = new JSONArray();
		JSONObject locationObj = new JSONObject();

		locationObj.put(AcademyConstants.YIH_LOCATION_ID, locationId);
		locationObj.put(AcademyConstants.JSON_ATTR_LOCATION_TYPE, locType);

		JSONObject newDemandObj = new JSONObject();
		newDemandObj.put(AcademyConstants.JSON_ATTR_DEMAND_TYPE, demandType);
		newDemandObj.put(AcademyConstants.YIH_A_QUANTITY, intStatusQty);
		newDemandObj.put(AcademyConstants.JSON_ATTR_SEGMENT, AcademyConstants.CONST_DEFAULT);
		demandsArray.add(newDemandObj);
		locationObj.put(AcademyConstants.JSON_ATTR_DEMANDS, demandsArray);
		if (!demandsArray.isEmpty()) {
			locationArray.add(locationObj);
		}
		return locationArray;
	}
	
	private void throwException() {
		YFSException yfsException = new YFSException();
		// Todo : Need to confirm exceptionId value
		String exceptionId  = AcademyConstants.DELIVERY_METHOD_NULL_EXP;
		
		yfsException.setErrorDescription(exceptionId);
		throw yfsException;

	}

	/**
	 * This method is to invoke yantrik's api with input Json to extend order TTL, if any exception occurs retry for X no. of times.
	 * @param retryCounter
	 * @param inputJson
	 * @param orderHeaderKey 
	 * @return
	 */
	private String invokeYIHApiAndFormReturnDoc(String orgCode, JSONObject inputJson,
			int retryCounter, YFSEnvironment env, String orderHeaderKey, String orderId) {
		
		String jsonStr = null;
		String httpBody = "";
		log.beginTimer("invokeReservationApi");
		
		// OMNI-44317 Change BEGIN 
//		try {
//			jsonStr = inputJson.toString();
//
//		} catch (Exception ex) {
//			throw new YFSException(ex.getMessage());
//		}
		// OMNI-44317 Change END 
		String output = null;
		int retryAttempts = getRetryAttempts();
		if (!YFCCommon.isVoid(orgCode)) {
			try {
				
				// OMNI-44317 Change BEGIN 
				//when same order line has more than one unit in different status we send duplicate demand type which fails in Yantriks
				log.debug("JSON before merging \n"+inputJson );
				inputJson=mergeDuplicates(inputJson);
				jsonStr = inputJson.toString();
				log.debug("JSON After merging \n"+jsonStr );
				// OMNI-44317 Change END 
				
				String apiUrl = formAPIUrl();
				httpBody = httpBody.concat("\n   " + jsonStr);

				if (log.isDebugEnabled())
					log.debug("httpBody is : " + httpBody);
				String content = httpBody.substring(1, httpBody.length() - 1);
				if (content != null && !content.isEmpty()) {
					output = YantriksCommonUtil.callYantriksAPI(apiUrl, "POST", httpBody, YantriksCommonUtil.getAvailabilityProduct(),env);// Modified the method argument to pass environment object as part of OMNI-16848
					if (AcademyConstants.V_NO_CONTENT_FOUND.equals(output)) {
						log.debug(
								"No Content Found as we received 204 from reservation call, will set supply to zero");
					}/*OMNI-16848: Start Change- Added this logic to raise alert once the retry attempts limit is reached for reprocessible error codes*/ 
					else if (AcademyConstants.CREATEORDER_RETRY_ALERT.equals(output)) {
						String queqeId = "DEFAULT";
						String excepType = "ReservationFailed";
						AcademyExceptionAlert.raiseAlert(env, excepType, orderHeaderKey,queqeId, AcademyConstants.CREATEORDER_RESERVATION_FAILURE);
					}/*OMNI-16848: End Change*/ 
					else {
						log.debug("Resevation Update Output for Order : " +orderId +" :: "+ output);
						/*
						 * YFSException yfsException = new YFSException();
						 * yfsException.setErrorDescription(
						 * "Yantriks Servers were up but received the system failure hence throwing exception, Response Code can be identified in the traces"
						 * ); throw yfsException;
						 */
					}
				} else {
					log.error("Not Calling YIH API, As the Http Body is empty....");
				}
			} catch (Exception e2) {
				/* OMNI-43045: Start Change :  Fetch the transaction object set in BeforeCreateOrderUEImpl to determine if invoked on create order*/
				log.info("Exception Caught while calling reservation API with input : " + httpBody);
				boolean isInvokedOnCreateOrder = false;
				if (!YFCCommon.isVoid(env.getTxnObject("IsInvokedOnCreateOrder"))) {
					isInvokedOnCreateOrder = (boolean) env.getTxnObject("IsInvokedOnCreateOrder");
				}
				log.verbose("isInvokedOnCreateOrder:"+isInvokedOnCreateOrder);
				/*Throw exception only when not invoked on create order*/
				if (!isInvokedOnCreateOrder) {
					throw new YFSException(e2.getMessage());
				}
				/* OMNI-43045: End Change*/
				
				/*  OMNI -16736 : Start change */
				/*
				 * YFSException yfsException = new YFSException(); yfsException.
				 * setErrorDescription("Exception Caught while calling availability API" +
				 * e2.getMessage()); throw yfsException;
				 
				while (retryCounter < retryAttempts) {
					log.error("Exception occur while calling reservation api : "+e2);
					retryCounter++;
					log.info("Retry Attempt is "+retryCounter);
					invokeYIHApiAndFormReturnDoc(orgCode, inputJson, retryCounter, env, orderHeaderKey,orderId);
				}
				/*
				 * Raise an alert if reservation fails for retryAttempts times
				 
				if(retryCounter == retryAttempts) {
					try {
						//Todo :Need to check value for quequ Id and exception type
						String queqeId = "DEFAULT";
						String excepType = "ReservationFailed";
						AcademyExceptionAlert.raiseAlert(env, excepType, orderHeaderKey,queqeId, e2.getMessage());
					} catch (Exception e) {
						log.error("Exception occur while raising the alert for reservation retry : "+e);
					}
				}
				/* OMNI - 16736, End change */
			}

			log.endTimer("invokeReservationApi");
		}

		return output;
	}

	private Map<String, Element> buildPrimeLineToOrderLineMap(Document outGetOrderList) {
		Map<String, Element> mapToReturn = new HashMap<>();

		Element eleRootOrderList = outGetOrderList.getDocumentElement();
		Element eleOrder = SCXmlUtil.getChildElement(eleRootOrderList, AcademyConstants.ELE_ORDER);
		Element eleOrderLines = SCXmlUtil.getChildElement(eleOrder, AcademyConstants.ELE_ORDER_LINES);

		NodeList nlOrderLines = eleOrderLines.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
		int orderLineLength = nlOrderLines.getLength();
		for (int i = 0; i < orderLineLength; i++) {
			Element currOrderLine = (Element) nlOrderLines.item(i);
			String orderLineKey = currOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
			mapToReturn.put(orderLineKey, currOrderLine);
		}
		return mapToReturn;
	}

	/**
	 * This method is to get yantrik api url
	 * 
	 * @return String
	 */
	private String formAPIUrl() {

		// String inventoryAggURL = AcademyConstants.API_URL_GET_RESERVE_API_URI +
		// "/ACADEMY.COM/OMS";
		String apiUrl = YFSSystem.getProperty("yantriks.reservation.url");
		/*
		 * inventoryAggURL + "?considerCapacity=" + false + "&operation=manage" +
		 * "&ignoreAvailabilityCheck" + "=" + true;
		 */
		return apiUrl;
	}

	public static JSONObject populateRootLevelJSONAttributes(JSONObject requestJson, String invOrgCode,
			String updateUser, String orderId, int expirationTime, String expirationTimeUnit,YFSEnvironment env,String strModifyTsOfOrder,String isAllocated) throws JSONException, ParseException {
		//OMNI-48035 BEGIN 
		String strEnableModifytsFromDBForYantriks=props.getProperty(YantriksConstants.STR_ENABLE_MODIFYTS_UPDATE);
		String strEnableModifytsFromDBForYantriksAsync=props.getProperty(YantriksConstants.STR_ENABLE_MODIFYTS_UPDATE_ASYNC);//Flag for async demand update
		//OMNI-48035 END
		
		requestJson.put(AcademyConstants.JSON_ATTR_ORG_ID, invOrgCode); // INVorgCode
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
		/*OMNI-45548: Start Change - Defect related to scheduleOrder with updateTime < CreateOrder demand updateTime */
		boolean isInvokedOnCreateOrder = false;/* This txn object is set in BeforeCreateOrderUE to 1.Avoid invocation of getavailability call on create order
		2.In CommonUtil code for exception handling to not rollback createOrder in case of exception.
		3.This same txn object has been reused only for 45548 */
		if (!YFCCommon.isVoid(env.getTxnObject(AcademyConstants.IS_INVOKED_ON_CREATE_ORDER))) {
			 isInvokedOnCreateOrder = true;
		}
		log.verbose("isInvokedOnCreateOrder = " + isInvokedOnCreateOrder);
		if(isInvokedOnCreateOrder) { 
			String currentDate=YantriksCommonUtil.getCurrentDateOrTimeStamp(formatter);
			log.verbose("Current Time from Java Code = " + currentDate);
			
			//OMNI-48035 BEGIN
			if(!YFCObject.isVoid(strEnableModifytsFromDBForYantriks) && AcademyConstants.STR_YES.equals(strEnableModifytsFromDBForYantriks)){
				log.verbose("Fetching modify time stamp from order. "+strModifyTsOfOrder);
				currentDate=getModifyDateInCorrectFormat(strModifyTsOfOrder,false);
				log.verbose("Final timestamp for JSON after getting and offsetting modifyTS =  "+currentDate);
			}   
			//OMNI-48035 END
			
			// NEGATIVE OFFSET BEGIN
			//Adding negative offset to JVM time / modifyts from order header for timing issue between create order and schedule order
			//demand update
			String offsetInSecs=props.getProperty(AcademyConstants.CREATE_ORDER_DEMAND_UPDATE_OFFSET);
			if(YFCObject.isVoid(offsetInSecs)) {
				log.verbose("Offset is null , hence defaulting to zero secs");
				offsetInSecs="0";
			}
			log.verbose("Offset In Secs= " + offsetInSecs);
			String demandUpdateTime=AcademyConstants.STR_HYPHEN.concat(offsetInSecs);
			Date date = formatter.parse(currentDate);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int createOrderUpdateTime=Integer.parseInt(demandUpdateTime);
			cal.add(Calendar.SECOND, createOrderUpdateTime);
			Date updatedDate=cal.getTime();
			log.verbose("Updated Time = " + formatter.format(updatedDate));
			//NEGATIVE OFFSET END
			
			requestJson.put(AcademyConstants.JSON_ATTR_UPDATE_TIME,formatter.format(updatedDate));
		}
		/*OMNI-45548: End Change*/
		
		//Else block is invoked when the demand update  is  sent for anything other than create order transaction.
		else {
			String currentDate=YantriksCommonUtil.getCurrentDateOrTimeStamp(formatter); 
			log.verbose("Current Time from Java Code = "+currentDate);
			// OMNI-48035 BEGIN
			String dmdUpdateOnAsync=(String) env.getTxnObject(AcademyConstants.DMD_UPDT_ON_ASYNC);//Fetch the txn object stored in AcademyKafkaDeltaUpdate.java class
			log.verbose("dmdUpdateOnAsync: "+dmdUpdateOnAsync);
			if (!YFCObject.isVoid(dmdUpdateOnAsync) && AcademyConstants.STR_YES.equals(dmdUpdateOnAsync)) {
				log.verbose("Demand Update invoked on Async call");
				if(!YFCObject.isVoid(strEnableModifytsFromDBForYantriksAsync) && AcademyConstants.STR_YES.equals(strEnableModifytsFromDBForYantriksAsync)  && YFCObject.isVoid(isAllocated)) {
					log.verbose("Fetching modify time stamp from order on async call: "+strModifyTsOfOrder);
					currentDate=getModifyDateInCorrectFormat(strModifyTsOfOrder,true);
					log.verbose("Final timestamp for JSON on async call after getting and offsetting modifyTS =  "+currentDate);
				}
			}
			else if(!YFCObject.isVoid(strEnableModifytsFromDBForYantriks) && AcademyConstants.STR_YES.equals(strEnableModifytsFromDBForYantriks)  && YFCObject.isVoid(isAllocated)){
				log.verbose("Fetching modify time stamp from order."+strModifyTsOfOrder);
				currentDate=getModifyDateInCorrectFormat(strModifyTsOfOrder,false);
				log.verbose("Final timestamp for JSON after getting and offsetting modifyTS =  "+currentDate);
			}
			// OMNI-48035 END
			requestJson.put(AcademyConstants.JSON_ATTR_UPDATE_TIME,	currentDate); 
		}
		requestJson.put(AcademyConstants.JSON_ATTR_UPDATE_USER, updateUser); // HardCoded Value
		requestJson.put(AcademyConstants.JSON_ATTR_ORDER_ID, orderId);
		requestJson.put(AcademyConstants.JSON_ATTR_EXP_TIME, expirationTime);
		requestJson.put(AcademyConstants.JSON_ATTR_EXP_TIME_UNIT, expirationTimeUnit); // Customer Overrides Based
		return requestJson;
	}

	

	public static int getRetryAttempts() {
		String retryAttempts = YFSSystem.getProperty(AcademyConstants.PROP_RETRY_ATTEMPTS);
		if (YFCObject.isVoid(retryAttempts)) {
			return 3;
		}
		return Integer.parseInt(retryAttempts);
	}
	
	// OMNI-44317 Change BEGIN
	//when same orderline has more than one unit in differnt status we send duplicate demand type which fails in Yantriks
	public static JSONObject mergeDuplicates(JSONObject myJsonObject) throws Exception {

		JSONArray reservation = myJsonObject.getJSONArray(YantriksConstants.JSON_ATTR_LINE_RES_DETAILS);
		int length = reservation.length();
		log.debug("First Length ="+length);
		for (int i = 0; i < length; i++) {
			log.debug("Inside For Loop");
			JSONObject currentLineReservation = reservation.getJSONObject(i);
			JSONArray arrayLocationReservation = currentLineReservation.getJSONArray(YantriksConstants.JSON_ATTR_LOCATION_RES_DETAILS);
			int locationLength = arrayLocationReservation.length();
			log.debug("Second Length ="+locationLength);
			for (int j = 0; j < locationLength; j++) {
				log.debug("Inside second Loop");
				JSONObject currentLocationReservation = arrayLocationReservation.getJSONObject(j);
				JSONArray arrayDemands = currentLocationReservation.getJSONArray(YantriksConstants.JSON_ATTR_DEMANDS);
				int demandsLength = arrayDemands.length();
				log.debug("Third Length ="+demandsLength);
				if (demandsLength > 1) {
					log.debug("If satisifed");
					Map<String, Integer> map = new HashMap<String, Integer>();
					for (int k = 0; k < demandsLength; k++) {
						JSONObject currentDemand = arrayDemands.getJSONObject(k);
						String strDemandType = currentDemand.getString(YantriksConstants.JSON_ATTR_DEMAND_TYPE);
						int intQuantity = currentDemand.getInt(YantriksConstants.JSON_ATTR_QUANTITY);

						if (map.containsKey(strDemandType)) {
							log.debug("Map already has entry for "+strDemandType);
							int value = map.get(strDemandType);
							value = value + intQuantity;
							map.put(strDemandType, value);
							currentDemand.put(YantriksConstants.JSON_ATTR_QUANTITY, value);
						} else {
							log.debug("Inserting new record in map for "+strDemandType);
							map.put(strDemandType, intQuantity);
						}
					}

					for (int l = 0; l < demandsLength; l++) {
						JSONObject currentDemand = arrayDemands.getJSONObject(l);

						String strDemandType = currentDemand.getString(YantriksConstants.JSON_ATTR_DEMAND_TYPE);
						int intQuantity = currentDemand.getInt(YantriksConstants.JSON_ATTR_QUANTITY);
						if (map.containsKey(strDemandType)) {
							int mapValue = map.get(strDemandType);
							if (mapValue != intQuantity) {
								log.info("Removing demand for  "+strDemandType +" with quantity "+intQuantity);
								arrayDemands.remove(l);
								demandsLength--;
								l--;
							}
						}
					}
				}
				log.debug("If condition not satisfied");
			}
		}
		return myJsonObject;
	}
	// OMNI-44317 Change END 
	/*OMNI-45548: Start Change - Setter method for properties*/
	public void setProperties(Properties props) {
		YASCreateOrderReservationUEImpl.props = props;
	}
	/*OMNI-45548: End Change*/
	
	
	/**
	 * This method is added as part of fix to send the updated date to Yantriks to avoid the
	 * overlap issue when multiple quick updates for a specific order are sent to Yantriks.
	 * 
	 * This method uses the Last modified time stamp of the order
	 */
	public static String getModifyDateInCorrectFormat(String strModifyTS,boolean isAsyncUpdate) throws ParseException {
		DateFormat originalFormat = new SimpleDateFormat(YantriksConstants.DATE_FORMAT_WITH_TIME_ZONE);
		// TimeZone timeZone3 = TimeZone.getTimeZone("CST");
		// originalFormat.setTimeZone(timeZone3);
		DateFormat updateFormat = new SimpleDateFormat(YantriksConstants.YAS_TIME_STAMP_FORMAT, Locale.US);
		updateFormat.setTimeZone(TimeZone.getTimeZone(YantriksConstants.STR_UTC));
		Date date = originalFormat.parse(strModifyTS);

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		
		//Adding (+) offset value to the Modifyts
		/*If Async update , read the async update offset value else read the non async update offset value*/
		String strUpdateTime="";
		if(isAsyncUpdate) {
			strUpdateTime = props.getProperty(YantriksConstants.STR_MODIFYTS_OFF_SET_ASYNC);
		}
		else {
		 strUpdateTime = props.getProperty(YantriksConstants.STR_MODIFYTS_OFF_SET);
		}
		log.verbose("Demand Update Offset in Secs: "+strUpdateTime);
		
		if (YFCObject.isVoid(strUpdateTime)) {
			strUpdateTime = YantriksConstants.STR_DEFAULT_ONE;
		}
		int iUpdatedModifyTS = Integer.parseInt(strUpdateTime);
		cal.add(Calendar.SECOND, iUpdatedModifyTS);
		
		Date updatedDate = cal.getTime();
		String formattedDate = updateFormat.format(updatedDate);
		return formattedDate;
	}
	
	
	/**
	 * OMNI- 51881 Changes
	 * This method is prepares input and makes a getShipmentList API call
	 * @throws Exception 
	 */
	public static Document getShipmentListForOrder(YFSEnvironment env,String sOrderHeaderKey) throws Exception {
		Document getShipmentListForOrderIndoc=XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		getShipmentListForOrderIndoc.getDocumentElement().setAttribute(AcademyConstants.STR_ORDR_HDR_KEY ,sOrderHeaderKey);
		Document templateDocGetShipmentListForOrder = XMLUtil.getDocument("<ShipmentList>\r\n"
				+ "    <Shipment DeliveryMethod=\"\" DocumentType=\"\" ShipmentNo=\"\" Status=\"\">\r\n"
				+ "        <ShipmentLines>\r\n"
				+ "            <ShipmentLine ShipmentLineKey=\"\" ChainedFromOrderLineKey=\"\"/>\r\n"
				+ "        </ShipmentLines>\r\n"
				+ "    </Shipment>\r\n"
				+ "</ShipmentList>");
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENTLIST_FORORDER, templateDocGetShipmentListForOrder);	
		log.verbose("Input - getShipmentListForOrder - " + XMLUtil.getXMLString(getShipmentListForOrderIndoc));
		Document outXML = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENTLIST_FORORDER, getShipmentListForOrderIndoc);
		log.verbose("Output - getShipmentListForOrder - " + XMLUtil.getXMLString(outXML));
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENTLIST_FORORDER);
		return outXML;
	}
	
}
