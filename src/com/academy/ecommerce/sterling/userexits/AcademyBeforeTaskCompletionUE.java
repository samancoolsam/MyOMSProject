package com.academy.ecommerce.sterling.userexits;



import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.ue.YCPBeforeTaskCompletionUE;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfs.japi.YFSUserExitException;

/* Sample Input XML to this class
 * <Task AssignedToUserId="abhi" EquipmentId="" HoldReasonCode=""
 IgnoreOrdering="Y" IsConsolidatedTask="" TargetCaseId=""
 TargetLocationId="10-LOC1" TargetPalletId="" TaskKey="20100415171633722717">
 <Inventory AssignedToUserId="abhi" ItemId="17209891" Quantity="1.0"
 SourceCaseId="" SourcePalletId="1-1"/>
 </Task>
 * */

public class AcademyBeforeTaskCompletionUE implements YCPBeforeTaskCompletionUE {

	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyBeforeTaskCompletionUE.class);

	public String beforeCreateOrder(YFSEnvironment arg0, String arg1)
			throws YFSUserExitException {

		return arg1;
	}

	public void beforeRegisterTaskCompletion(YFSEnvironment env,
			Document inputXML) throws YFSUserExitException {

		YFSException e = new YFSException();
		// get data from input XML
		String taskKey = inputXML.getDocumentElement().getAttribute("TaskKey");
		log.verbose("****************** Input Document :::::"
				+ XMLUtil.getXMLString(inputXML));
		
		// condition to exclude count tasks where taskId is stamped instead of taskKey
		if (!YFCObject.isNull(taskKey)) {
			String targetLocationId = inputXML.getDocumentElement()
					.getAttribute("TargetLocationId");
			// String itemId = inventoryInfo.getAttribute("ItemId");
			// String itemQty = inventoryInfo.getAttribute("Quantity");
			String assignedToUserId = inputXML.getDocumentElement()
					.getAttribute("AssignedToUserId");
			boolean isMixSKUPassed = true;
			String errorDescription = "";
			boolean isCapacityViolated = false;
			String itemId = "";
			String itemQty = "";
			try {
				Document inputGetTaskList = XMLUtil.createDocument("Task");
				inputGetTaskList.getDocumentElement().setAttribute("TaskKey",
						taskKey);

				Document outTempGetTaskList = YFCDocument
						.parse(
								"<TaskList><Task TargetLocationId=\"\" TaskKey=\"\" TaskType=\"\" ><Inventory ItemId=\"\" Quantity=\"\" /><TaskType ActivityGroupId=\"\" /></Task></TaskList>")
						.getDocument();
				env.setApiTemplate("getTaskList", outTempGetTaskList);

				Document outGetTaskListDoc = AcademyUtil.invokeAPI(env,
						"getTaskList", inputGetTaskList);
				env.clearApiTemplates();

				log.verbose("****************** getTaskList output Document :::::: "
						+ XMLUtil.getXMLString(outGetTaskListDoc));
				String taskType = ((Element) outGetTaskListDoc
						.getDocumentElement().getElementsByTagName("Task")
						.item(0)).getAttribute("TaskType");
				String activityGroupId = ((Element) outGetTaskListDoc
						.getDocumentElement().getElementsByTagName("TaskType")
						.item(0)).getAttribute("ActivityGroupId");
				String orgTargetLoc = ((Element) outGetTaskListDoc
						.getDocumentElement().getElementsByTagName("Task")
						.item(0)).getAttribute("TargetLocationId");
				itemId = ((Element) outGetTaskListDoc.getDocumentElement()
						.getElementsByTagName("Inventory").item(0))
						.getAttribute("ItemId");
				itemQty = ((Element) outGetTaskListDoc.getDocumentElement()
						.getElementsByTagName("Inventory").item(0))
						.getAttribute("Quantity");

				// perform the mix SKU check and capacity check only for inbound tasks
			if (!YFCObject.isNull(activityGroupId)) {
				if ("PUTAWAY".equals(activityGroupId)) {
					// invoking getLocationList API to get the zone
					Document inputGetLocationDetails = XMLUtil
							.createDocument("Location");
					inputGetLocationDetails.getDocumentElement().setAttribute(
							"LocationId", targetLocationId);

					Document outTempGetLocationList = YFCDocument
							.parse(
							"<Locations>" + 
							"<Location LocationId=\"\" ZoneId=\"\"><LocationSize IsInfiniteCapacity=\"\" />" +  
							"<TransactionalLocationAttributes AvailableVolume=\"\" AvailableWeight=\"\" PendInVolume=\"\" PendInWeight=\"\" VolumeUom=\"\" WeightUom=\"\" /></Location></Locations>")
							.getDocument();
					env.setApiTemplate("getLocationList",
							outTempGetLocationList);
					Document outGetLocationListDoc = AcademyUtil.invokeAPI(env,
							"getLocationList", inputGetLocationDetails);
					env.clearApiTemplates();

					log.verbose("*****************  getLocationList output Document ::::::::"
							+ XMLUtil.getXMLString(outGetLocationListDoc));
					Element outGetLocListEle = outGetLocationListDoc
							.getDocumentElement();
					String zoneId = ((Element) outGetLocListEle
							.getElementsByTagName("Location").item(0))
							.getAttribute("ZoneId");
					String isInfiniteCapacity = ((Element) outGetLocListEle
							.getElementsByTagName("LocationSize").item(0))
							.getAttribute("IsInfiniteCapacity");

					// invoking getZoneList API to get the mix SKU flag
					Document inputGetZoneList = XMLUtil.createDocument("Zone");
					inputGetZoneList.getDocumentElement().setAttribute(
							"ZoneId", zoneId);

					Document outTempGetZoneList = YFCDocument.parse(
							"<Zones><Zone MixSKU=\"\" ZoneId=\"\" /></Zones>")
							.getDocument();
					env.setApiTemplate("getZoneList", outTempGetZoneList);
					Document outGetZoneList = AcademyUtil.invokeAPI(env,
							"getZoneList", inputGetZoneList);
					env.clearApiTemplates();

					log.verbose("****************** getZoneList output Document::::::::"
							+ XMLUtil.getXMLString(outGetZoneList));
					String isMixSKUAllowed = ((Element) outGetZoneList
							.getDocumentElement().getElementsByTagName("Zone")
							.item(0)).getAttribute("MixSKU");

					if ("N".equals(isMixSKUAllowed)) {
						/*
						 * Input to getNodeInventory * <NodeInventory Node="005"
						 * LocationId="34-Loc1"/>
						 */
						if (!targetLocationId.equals(null)) {
						
						Document inputGetNodeInventory = XMLUtil
								.createDocument("NodeInventory");
						inputGetNodeInventory.getDocumentElement()
								.setAttribute("Node", "005");
						inputGetNodeInventory.getDocumentElement()
								.setAttribute("LocationId", targetLocationId);
						log.verbose("****************** getNodeInventory input Document::::::::"
								+ XMLUtil.getXMLString(inputGetNodeInventory));

						Document outTempGetNodeInventory = YFCDocument
								.parse(
										"<NodeInventory Node=\"\"><LocationInventoryList><LocationInventory InventoryItemKey=\"\" ><InventoryItem ItemID=\"\" ProductClass=\"\" UnitOfMeasure=\"\"></InventoryItem></LocationInventory></LocationInventoryList></NodeInventory>")
								.getDocument();
						env.setApiTemplate("getNodeInventory",
								outTempGetNodeInventory);
						Document outGetNodeInventoryDoc = AcademyUtil
								.invokeAPI(env, "getNodeInventory",
										inputGetNodeInventory);
						log.verbose("****************** getNodeInventory output Document::::::::"
								+ XMLUtil.getXMLString(outGetNodeInventoryDoc));
						env.clearApiTemplates();
						/*
						 * Output template for getNodeInventory - 
						 * <NodeInventory
						 * Node="005"> <LocationInventoryList>
						 * <LocationInventory
						 * InventoryItemKey="20100304180307611121">
						 * <InventoryItem ItemID="0013208277"
						 * ProductClass="GOOD" UnitOfMeasure="EACH" />
						 * </LocationInventory> </LocationInventoryList>
						 * </NodeInventory>
						 */

						Element getNodeInventoryEle = outGetNodeInventoryDoc
								.getDocumentElement();
						NodeList inventoryItemNodeList = getNodeInventoryEle
								.getElementsByTagName("InventoryItem");

						for (int i = 0; i < inventoryItemNodeList.getLength(); i++) {

							String locationItemId = ((Element) inventoryItemNodeList
									.item(i)).getAttribute("ItemID");
							if (!locationItemId.equals(itemId)) {
								// throw exception
								errorDescription = "MIX SKU CONSTRAINT VIOLATED";
								e.setErrorCode("EXTN_ACAD001");
								e.setErrorDescription(errorDescription);
								sendAlertToAcademy(env, errorDescription,
										assignedToUserId, targetLocationId, itemId);
								isMixSKUPassed = false;
								throw e;
							}
						}
						if (isMixSKUPassed) {
							if (!targetLocationId.equals(orgTargetLoc)) {
								if (!("Y".equals(isInfiniteCapacity))) {
									isCapacityViolated = checkCapacityConstraint(
											env, itemId, assignedToUserId,
											targetLocationId, outGetLocListEle,
											itemQty);
								}
							}
						}
						} else {
							log.verbose("*** Skipping getNodeInventory API as LocationID is NULL :::");
							
							// End of Fix for STL-496
						}
					} else {
						if (!targetLocationId.equals(orgTargetLoc)) {
							if (!("Y".equals(isInfiniteCapacity))) {
								isCapacityViolated = checkCapacityConstraint(env,
										itemId, assignedToUserId, targetLocationId,
										outGetLocListEle, itemQty);
							}
						}
					}
					if (isCapacityViolated) {
						errorDescription = "CAPACITY CONSTRAINT VIOLATED";
						e.setErrorCode("EXTN_ACAD002");
						e.setErrorDescription(errorDescription);
						sendAlertToAcademy(env, errorDescription,
								assignedToUserId, targetLocationId, itemId);
						throw e;
					}
				}
			}
		} catch (Exception ye) {
			if (!StringUtil.isEmpty(e.getErrorCode())) {
				throw e;
			} else {
				YFSUserExitException ex = new YFSUserExitException();
				throw ex;
			}
		} 
		/*	Removing the code to prevent commit after every task completion while 'Deposit All' as WMS cleanup activity by muchil
		 *  finally {
				if (!StringUtil.isEmpty(e.getErrorCode())) {
					try {
						sendAlertToAcademy(env, errorDescription,
								assignedToUserId, targetLocationId, itemId);
						YCPContext oCtx = (YCPContext) env;
						oCtx.getConnection().commit();
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
			}	*/
		}
	}

	/**
	 * This method is used for checking the capacity constraint of the item and
	 * location.
	 */
	private boolean checkCapacityConstraint(YFSEnvironment env, String itemId,
			String assignedToUserId, String targetLocationId,
			Element outGetLocListEle, String itemQty)
			throws ParserConfigurationException, Exception {

		double itemQuantity = 0;
		if (!StringUtil.isEmpty(itemQty)) {
			itemQuantity = Double.parseDouble(itemQty);
		}

		try {
			// get Item Dimensions
			Document inputGetItemList = XMLUtil.createDocument("Item");
			inputGetItemList.getDocumentElement()
					.setAttribute("ItemID", itemId);

			Document outTempGetItemList = YFCDocument
					.parse(
							"<ItemList><Item ItemID=\"\" ><PrimaryInformation UnitVolume=\"\" UnitVolumeUOM=\"\" UnitWeight=\"\" UnitWeightUOM=\"\" /></Item></ItemList>")
					.getDocument();
			env.setApiTemplate("getItemList", outTempGetItemList);
			Document outGetItemListDoc = AcademyUtil.invokeAPI(env,
					"getItemList", inputGetItemList);
			log.verbose("****************** getItemList output Document::::::::"
					+ XMLUtil.getXMLString(outGetItemListDoc));
			env.clearApiTemplates();

			Element outGetItemListEle = outGetItemListDoc.getDocumentElement();
			Element itemPrimaryInfo = (Element) outGetItemListEle
					.getElementsByTagName("PrimaryInformation").item(0);
			String itemUnitVol = itemPrimaryInfo.getAttribute("UnitVolume");
			double itemUnitVolume = 0;
			if (!StringUtil.isEmpty(itemUnitVol)) {
				itemUnitVolume = Double.parseDouble(itemUnitVol);
			}
			
		/*	Ignoring checks on item weights
		 *  String itemUnitWt = itemPrimaryInfo.getAttribute("UnitWeight");
		 *  double itemUnitWeight = 0;
			if (!StringUtil.isEmpty(itemUnitWt)) {
				itemUnitWeight = Double.parseDouble(itemUnitWt);
			}
			String itemUnitVolUOM = itemPrimaryInfo.getAttribute("UnitVolumeUOM");
			String itemUnitWtUOM = itemPrimaryInfo.getAttribute("UnitWeightUOM");	*/

			// get Location Dimensions

			Element locationDimensionEle = (Element) outGetLocListEle
					.getElementsByTagName("TransactionalLocationAttributes")
					.item(0);

			String locAvailableVol = locationDimensionEle
					.getAttribute("AvailableVolume");
			String locPendInVol = locationDimensionEle
					.getAttribute("PendInVolume");
		/*	Ignoring Location weight constraints and UOM's
		 *  String locAvailableWt = locationDimensionEle
					.getAttribute("AvailableWeight");
			String locPendInWt = locationDimensionEle
					.getAttribute("PendInWeight");
			String locAvailableVolUOM = locationDimensionEle.getAttribute("VolumeUom");
			String locAvailableWtUOM = locationDimensionEle.getAttribute("WeightUom");	*/

			double locAvailableVolume = 0;
			if (!StringUtil.isEmpty(locAvailableVol)) {
				locAvailableVolume = Double.parseDouble(locAvailableVol);
			}
			double locPendInVolume = 0;
			if (!StringUtil.isEmpty(locPendInVol)) {
				locPendInVolume = Double.parseDouble(locPendInVol);
			}
		/*	Ignoring Location weight constraints
		 *  double locAvailableWeight = 0;
			if (!StringUtil.isEmpty(locAvailableWt)) {
				locAvailableWeight = Double.parseDouble(locAvailableWt);
			}
			double locPendInWeight = 0;
			if (!StringUtil.isEmpty(locPendInWt)) {
				locPendInWeight = Double.parseDouble(locPendInWt);
			}	*/

			double totalItemVolume = itemUnitVolume * itemQuantity;
			//double totalItemWeight = itemUnitWeight * itemQuantity;

			// checking only Volume constraints
			if ((totalItemVolume > (locAvailableVolume - locPendInVolume))) {
				return true;
			}
		} catch (YFSException e) {
			throw e;
		}
		return false;
	}

	/**
	 * If Validation fails raise alert to Academy team
	 * 
	 * @param string
	 * 
	 * Info to be sent to alert-stockerid, target location, constraint violated
	 * and item in conflict.
	 */
	private void sendAlertToAcademy(YFSEnvironment env,
			String errorDescription, String assignedToUserId,
			String locationId, String itemId) {
		try {
			Document alertInXML = XMLUtil.createDocument("Inbox");
			alertInXML.getDocumentElement().setAttribute("ExceptionType",
					"INBOUND PUTAWAY EXCEPTION");
			alertInXML.getDocumentElement().setAttribute("Priority", "1");
			alertInXML.getDocumentElement().setAttribute("Description",
					errorDescription);
			alertInXML.getDocumentElement().setAttribute("DetailDescription",
					errorDescription);
			alertInXML.getDocumentElement().setAttribute("QueueId", "DEFAULT");
			alertInXML.getDocumentElement().setAttribute("ActiveFlag", "Y");
			alertInXML.getDocumentElement().setAttribute("EnterpriseKey",
					"Academy_Direct");
			alertInXML.getDocumentElement().setAttribute("AssignedToUserId",
					assignedToUserId);
			alertInXML.getDocumentElement().setAttribute("ApiName",
					"RegisterTaskCompletion");
			alertInXML.getDocumentElement().setAttribute("LocationId",
					locationId);
			alertInXML.getDocumentElement().setAttribute("ItemId", itemId);
			
			log.verbose("****************** AcademyDropMessageToCreateAlert input Document::::::::"
					+ XMLUtil.getXMLString(alertInXML));
			AcademyUtil.invokeService(env, "AcademyDropMessageToCreateAlert",alertInXML);
			//Document outAlert = AcademyUtil.invokeAPI(env, "createException",alertInXML);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
