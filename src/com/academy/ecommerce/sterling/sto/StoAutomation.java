package com.academy.ecommerce.sterling.sto;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.interfaces.api.AcademyGetItemDetailsAndUpdateInputAPI;
import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.shared.ycp.YFSContext;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
public class StoAutomation {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyGetItemDetailsAndUpdateInputAPI.class);

	public static YIFApi api = null;

	{
		try {
			api = YIFClientFactory.getInstance().getApi();
		} catch (YIFClientCreationException e) {
			e.printStackTrace();
		}

	}

	public Document checkAvailability(YFSEnvironment env, Document inDoc) throws Exception {
		/* segmented item list document */
		StoShipment method = new StoShipment();
		Document segmentedItems = XMLUtil.createDocument("SegmentedItems");
		Element segRooot = segmentedItems.getDocumentElement();

		/* Unsegmented item list for alert */

		Document UnsegmentedItem = XMLUtil.createDocument("UnsegmentedItems");
		Element unSegRoot = UnsegmentedItem.getDocumentElement();

		log.verbose("*** getting required attribute values from inDoc *** ");

		/* Attributes from input XML */
		Element rootele = inDoc.getDocumentElement();

		NodeList getItemList = inDoc.getElementsByTagName("ShipmentLine");

		String shipNode = rootele.getAttribute("ShipNode");

		String receivingNode = rootele.getAttribute("ReceivingNode");

		String sellerOrganizationCode = rootele.getAttribute("SellerOrganizationCode");

		String enterpriseCode = rootele.getAttribute("EnterpriseCode");

		String buyerName = rootele.getAttribute("BuyerName");

		String documentType = rootele.getAttribute("DocumentType");
		// String shipmentType = rootele.getAttribute("DocumentType");
		String shipmentType = null;

		int l = getItemList.getLength();

		Document shipmentCreationStatus = XMLUtil.createDocument("ShipmentCreationStatus");
		Element eleShipmentCreationStatus = shipmentCreationStatus.getDocumentElement();
		Element LocationInventory = null;

		log.verbose("*** printing inDoc *** " + XMLUtil.getXMLString(inDoc));

		String segmentNo = callSegmentNo(env);
		double requestorQty;

		String itemID = null;

		for (int k = 0; k < l; k++) {

			Element getShipmentLineListElement = (Element) getItemList.item(k);

			itemID = getShipmentLineListElement.getAttribute("ItemID");

			String quantity = getShipmentLineListElement.getAttribute("Quantity");

			/* qty1 is the quantity requested by sto requestor */

			requestorQty = Double.parseDouble(quantity);

			/* This method will get the shipment type */

			shipmentType = method.callShipmentGroupID(env, itemID, enterpriseCode);

			/* This method will get the node inventory */

			Document getNodeInventoryOutput = callGetNodeInventory(env, shipNode, itemID);
			log.verbose(" getNodeInventoryOutput XML" + XMLUtil.getXMLString(getNodeInventoryOutput));

			/* This method will get the available quantity at the node */

			Document getAtpOutput = callATP(env, shipNode, itemID);
			log.verbose(" getAtpOutput XML" + XMLUtil.getXMLString(getAtpOutput));

			Element eleAvailable = (Element) getAtpOutput.getElementsByTagName("Available").item(0);
			String Result = "";
			if (!YFCObject.isVoid(eleAvailable)) {

				String ATPQunatity = eleAvailable.getAttribute("Quantity");

				double qtyATP = Double.parseDouble(ATPQunatity);

				/* if passed input is available at the Node */

				if (requestorQty <= qtyATP) {

					NodeList getLocationInventoryList = getNodeInventoryOutput.getElementsByTagName("LocationInventory");
					NodeList getLocationInventoryListASC = getLocationInventoryListAsc(env, getLocationInventoryList, shipNode);

					String InventoryItemKey = "";

					int i;

					for (i = 0; i < getLocationInventoryListASC.getLength(); i++)

					{
						double availableToSegment = 0;
						String segQuantity = null;
						Element getLocationInventoryListElement = ((Element) getLocationInventoryListASC.item(i));

						String locationId = getLocationInventoryListElement.getAttribute("LocationId");
						InventoryItemKey = getLocationInventoryListElement.getAttribute("InventoryItemKey");

						String locationType = callGetLocationDetails(env, locationId, shipNode);

						if (locationType.equals("REGULAR")) {

							/*
							 * This method gets the actual quantity available
							 * for segmentation at the location
							 */
							double segmentedQuantity = callAvailableToSegment(getLocationInventoryListElement);
							availableToSegment = qtyATP - segmentedQuantity;

							segQuantity = Double.toString(availableToSegment);
							log.verbose("segQuantity???" + Double.toString(availableToSegment));

							if (requestorQty <= segmentedQuantity) {

								/*
								 * This method returns the result if
								 * segmentation is done
								 */

								Result = callChangeLocationInventoryAttributes(env, shipNode, locationId, InventoryItemKey, quantity, itemID, segmentNo);

								Element SegmentedItem = segmentedItems.createElement("SegmentedItem");
								SegmentedItem.setAttribute("segmentedItem", itemID);
								SegmentedItem.setAttribute("segQuantity", quantity);
								SegmentedItem.setAttribute("LocationID", locationId);
								segRooot.appendChild(SegmentedItem);

								LocationInventory = shipmentCreationStatus.createElement("SegmentationStatus");
								break;
							} else

							{

								Element UnSegItem = UnsegmentedItem.createElement("UnSegItem");
								UnSegItem.setAttribute("ItemID", itemID);
								UnSegItem.setAttribute("Qunatity", quantity);
								UnSegItem.setAttribute("LocationID", locationId);
								unSegRoot.appendChild(UnSegItem);

								log.verbose("Item is skipped from segmentation if location type is not REGULAR");
								log.verbose("Item is skipped from segmentation if location type is not REGULAR");
								Result = "Insufficient quantity or not REGULAR location, item is skipped";
								LocationInventory = shipmentCreationStatus.createElement("SegmentationStatus");

							}
						}

						else

						{

							Element UnSegItem = UnsegmentedItem.createElement("UnSegItem");
							UnSegItem.setAttribute("ItemID", itemID);
							UnSegItem.setAttribute("Qunatity", quantity);
							UnSegItem.setAttribute("LocationID", locationId);
							unSegRoot.appendChild(UnSegItem);

							log.verbose("Item is skipped from segmentation if location type is not REGULAR");
							Result = "Insufficient quantity or not REGULAR location, item is skipped";
							LocationInventory = shipmentCreationStatus.createElement("SegmentationStatus");

						}

						eleShipmentCreationStatus.appendChild(LocationInventory);

						LocationInventory.setAttribute("AvailabilityStatus", Result);
						LocationInventory.setAttribute("LocationID", locationId);
						LocationInventory.setAttribute("AvailableQunatity", segQuantity);

						log.verbose("Shipment creation status" + XMLUtil.getXMLString(shipmentCreationStatus));

					}

				}
				/* If the required quantity is not available at the node */
				else {

					LocationInventory = shipmentCreationStatus.createElement("SegmentationStatus");
					Element UnSegItem = UnsegmentedItem.createElement("UnSegItem");
					UnSegItem.setAttribute("ItemID", itemID);
					UnSegItem.setAttribute("Qunatity", quantity);
					unSegRoot.appendChild(UnSegItem);

					Result = "Not Sufficient quantity at the node";
					LocationInventory.setAttribute("AvailabilityStatus", Result);
					eleShipmentCreationStatus.appendChild(LocationInventory);
				}
			}

			/* if eleAvailable element is null */
			else {
				LocationInventory = shipmentCreationStatus.createElement("SegmentationStatus");
				Element UnSegItem = UnsegmentedItem.createElement("UnSegItem");
				UnSegItem.setAttribute("ItemID", itemID);
				UnSegItem.setAttribute("Qunatity", quantity);
				unSegRoot.appendChild(UnSegItem);

				Result = "Not Sufficient quantity at the node";
				LocationInventory.setAttribute("AvailabilityStatus", Result);
				eleShipmentCreationStatus.appendChild(LocationInventory);
			}

		}
		log.verbose("segmented item list " + XMLUtil.getXMLString(segmentedItems));

		log.verbose("Unsegmented Item List " + XMLUtil.getXMLString(UnsegmentedItem));

		/* This method will create the shipment */

		/*
		 * Get ShipmentNo and ShipmentKey from the output XML of createShipment
		 * api
		 */

		NodeList segmentedItemList = segmentedItems.getElementsByTagName("SegmentedItem");
		int segLength = segmentedItemList.getLength();
		if (!(segLength == 0)) {
			Document createShipmentOutput = callCreateShipment(env, sellerOrganizationCode, documentType, shipNode, receivingNode, enterpriseCode, buyerName,
					shipmentType);
			Element eleCreateShipment = createShipmentOutput.getDocumentElement();
			Element shipment = shipmentCreationStatus.createElement("Shipment");
			shipment.setAttribute("Status", "ShipmentCreated");
			eleShipmentCreationStatus.appendChild(shipment);

			String ShipmentKey = eleCreateShipment.getAttribute("ShipmentKey");

			String ShipmentNo = eleCreateShipment.getAttribute("ShipmentNo");

			/*
			 * This method returns the ShipNodePersonInfo which is used as
			 * ToAddress in ChangeShipment api
			 */

			String personInfoKey = callGetOrganizationDetails(env, receivingNode);

			callChangeShipment(env, documentType, sellerOrganizationCode, shipNode, receivingNode, ShipmentKey, ShipmentNo, segmentNo, segmentedItems,
					personInfoKey);

		} else {
			Element shipment = shipmentCreationStatus.createElement("Shipment");
			shipment.setAttribute("Status", "Shipment Not Created");
			eleShipmentCreationStatus.appendChild(shipment);

		}
		return shipmentCreationStatus;

	}
	/*
	 * private String callShipmentGroupID(YFSEnvironment env, String itemID,
	 * String enterpriseCode) throws Exception {
	 * 
	 * Document getItemDetails = XMLUtil.createDocument("Item"); Element
	 * eleGetItemDetails = getItemDetails.getDocumentElement();
	 * eleGetItemDetails.setAttribute("ItemID", itemID);
	 * eleGetItemDetails.setAttribute("OrganizationCode", enterpriseCode);
	 * eleGetItemDetails.setAttribute("UnitOfMeasure", "EACH");
	 * 
	 * Document getItemDetailsTemplate = XMLUtil.getDocument("<Item >" + "<ClassificationCodes
	 * StorageType=\"\" >" + "</ClassificationCodes>" + "</Item>");
	 * env.setApiTemplate("getItemDetails", getItemDetailsTemplate); Document
	 * getItemDetailsOutput = api.getItemDetails(env, getItemDetails);
	 * env.clearApiTemplates();
	 * 
	 * Element ClassificationCodes = (Element)
	 * getItemDetailsOutput.getElementsByTagName("ClassificationCodes").item(0);
	 * 
	 * String storageType = ClassificationCodes.getAttribute("StorageType");
	 * String shipmentType = null; if ((storageType != "NCONLP") || (storageType !=
	 * "NCONNLP")) {
	 * 
	 * shipmentType = "CON"; } else { shipmentType = "BULK"; } return
	 * shipmentType; }
	 */
	private String callGetOrganizationDetails(YFSEnvironment env, String receivingNode) throws Exception {
		log.verbose("Inside callGetOrganizationDetails");

		Document getOrganizationDetails = XMLUtil.createDocument("Organization");
		Element eleGetOrganizationDetails = getOrganizationDetails.getDocumentElement();
		eleGetOrganizationDetails.setAttribute("OrganizationCode", receivingNode);

		Document getOrganizationDetailsOutput = api.getOrganizationList(env, getOrganizationDetails);

		NodeList shipNodePersonInfoList = getOrganizationDetailsOutput.getElementsByTagName("ShipNodePersonInfo");
		Element shipNodePersonInfo = (Element) shipNodePersonInfoList.item(0);

		String personInfokey = shipNodePersonInfo.getAttribute("PersonInfoKey");

		return personInfokey;
	}
	public Document callATP(YFSEnvironment env, String ShipNode, String itemID) throws Exception {
		log.verbose("Inside callATP to get ATP qunatity");
		Document getATP = XMLUtil.createDocument("GetATP");
		Element rootGetATP = getATP.getDocumentElement();
		rootGetATP.setAttribute("ItemID", itemID);
		rootGetATP.setAttribute("OrganizationCode", "Academy_Direct");
		rootGetATP.setAttribute("ShipNode", ShipNode);
		rootGetATP.setAttribute("UnitOfMeasure", "EACH");
		rootGetATP.setAttribute("ProductClass", "GOOD");

		log.verbose(" getATP input XML " + XMLUtil.getXMLString(getATP));

		Document getAtpOutput = api.getATP(env, getATP);
		return getAtpOutput;
	}

	public String callChangeLocationInventoryAttributes(YFSEnvironment env, String shipNode, String locationId, String InventoryItemKey, String quantity,
			String itemID, String segmentNo) throws Exception {

		log.verbose("Inside callChangeLocationInventoryAttributes to segment the inventory");
		Document ChangeLocationInventoryAttributes = XMLUtil.createDocument("ChangeLocationInventoryAttributes");
		Element eleroot1 = ChangeLocationInventoryAttributes.getDocumentElement();
		eleroot1.setAttribute("EnterpriseCode", "Academy_Direct");
		eleroot1.setAttribute("Node", shipNode);

		Element source = ChangeLocationInventoryAttributes.createElement("Source");
		source.setAttribute("LocationId", locationId);
		eleroot1.appendChild(source);

		Element FromInventory = ChangeLocationInventoryAttributes.createElement("FromInventory");
		FromInventory.setAttribute("InventoryItemKey", InventoryItemKey);
		FromInventory.setAttribute("Quantity", quantity);

		FromInventory.setAttribute("InventoryStatus", "GOOD");
		FromInventory.setAttribute("Segment", "");

		Element Receipt = ChangeLocationInventoryAttributes.createElement("Receipt");
		FromInventory.appendChild(Receipt);

		Element InventoryItem1 = ChangeLocationInventoryAttributes.createElement("InventoryItem");
		InventoryItem1.setAttribute("ItemID", itemID);
		InventoryItem1.setAttribute("ProductClass", "GOOD");
		InventoryItem1.setAttribute("UnitOfMeasure", "EACH");
		FromInventory.appendChild(InventoryItem1);

		source.appendChild(FromInventory);

		Element ToInventory = ChangeLocationInventoryAttributes.createElement("ToInventory");
		ToInventory.setAttribute("Segment", segmentNo);

		ToInventory.setAttribute("SegmentType", "MTO");

		Element Receipt1 = ChangeLocationInventoryAttributes.createElement("Receipt");
		ToInventory.appendChild(Receipt1);

		Element InventoryItem2 = ChangeLocationInventoryAttributes.createElement("InventoryItem");
		InventoryItem2.setAttribute("ItemID", itemID);
		InventoryItem2.setAttribute("ProductClass", "GOOD");
		InventoryItem2.setAttribute("UnitOfMeasure", "EACH");
		ToInventory.appendChild(InventoryItem2);

		source.appendChild(ToInventory);

		Element Audit = ChangeLocationInventoryAttributes.createElement("Audit");
		Audit.setAttribute("ReasonCode", "DAMAGED ITEM");

		eleroot1.appendChild(Audit);

		log.verbose("ChangeLocationInventoryAttributes input XML " + XMLUtil.getXMLString(ChangeLocationInventoryAttributes));

		api.changeLocationInventoryAttributes(env, ChangeLocationInventoryAttributes);

		String Result = " Location is regular and Segmentation is done successfully";
		return Result;
	}

	/* This method will get the location wise inventory */
	public Document callGetNodeInventory(YFSEnvironment env, String shipNode, String itemID) throws Exception {
		log.verbose("Inside callGetNodeInventory to get the Node inventory");

		Document golInDoc = XMLUtil.createDocument("NodeInventory");
		Element eleRoot = golInDoc.getDocumentElement();
		eleRoot.setAttribute("Node", shipNode);
		Element eleInventory = golInDoc.createElement("Inventory");
		eleRoot.appendChild(eleInventory);
		Element eleInventoryItem = golInDoc.createElement("InventoryItem");
		eleInventoryItem.setAttribute("ItemID", itemID);
		eleInventory.appendChild(eleInventoryItem);

		/* Template for getNodeInventory */

		Document getNodeInventoryTemplate = XMLUtil.getDocument("<NodeInventory EnterpriseCode=\"\"  Node=\"\">"
				+ "<LocationInventoryList TotalNumberOfRecords=\"\" >"
				+ "<LocationInventory InventoryItemKey=\"\"   LocationId=\"\"    Quantity=\"\" ZoneId=\"\">"
				+ "<InventoryItem ItemID=\"\" OrganizationCode=\"\" >  " + "<Item ItemID=\"\" />" + "</InventoryItem>" + "<SummaryAttributes FifoNo=\"\" />"
				+ "<ItemInventoryDetailList>" + "<ItemInventoryDetail />" + "</ItemInventoryDetailList>" + "</LocationInventory>" + "</LocationInventoryList>"
				+ "</NodeInventory>");

		env.setApiTemplate("getNodeInventory", getNodeInventoryTemplate);
		Document getNodeInventory = api.getNodeInventory(env, golInDoc);
		env.clearApiTemplates();
		return getNodeInventory;

	}

	/* This method creates the Shipement */
	public Document callCreateShipment(YFSEnvironment env, String sellerOrganizationCode, String documentType, String shipNode, String receivingNode,
			String enterpriseCode, String buyerName, String shipmentType) throws Exception {
		log.verbose(" Inside callCreateShipment, this creates the shipment");

		Document inDoc2 = XMLUtil.createDocument("Shipment");
		Element root1 = inDoc2.getDocumentElement();
		root1.setAttribute("SellerOrganizationCode", sellerOrganizationCode);
		root1.setAttribute("DocumentType", documentType);
		root1.setAttribute("ShipmentKey", "");
		root1.setAttribute("EnterpriseCode", enterpriseCode);
		root1.setAttribute("ShipNode", shipNode);
		root1.setAttribute("ReceivingNode", receivingNode);
		root1.setAttribute("ShipmentNo", "");
		root1.setAttribute("PickticketNo", buyerName);
		root1.setAttribute("ShipmentType", shipmentType);

		Document createShipmentOutput = api.createShipment(env, inDoc2);

		log.verbose("Input XML for CreateShipment " + XMLUtil.getXMLString(inDoc2));

		return createShipmentOutput;

	}
	/* This method calls the changeShipment api */

	public void callChangeShipment(YFSEnvironment env, String documentType, String sellerOrganizationCode, String shipNode, String receivingNode,
			String ShipmentKey, String ShipmentNo, String segmentNo, Document segmentedItems, String personInfoKey) throws Exception {
		log.verbose("Inside callChangeShipment ");

		Document changeShipment = XMLUtil.createDocument("Shipment");
		Element rootShipment = changeShipment.getDocumentElement();
		rootShipment.setAttribute("Action", "Modify");
		rootShipment.setAttribute("DataElementPath", "xml:/Shipment");
		rootShipment.setAttribute("DocumentType", documentType);
		rootShipment.setAttribute("EnterpriseCode", "Academy_Direct");
		rootShipment.setAttribute("IgnoreOrdering", "Y");
		rootShipment.setAttribute("OrderAvailableOnSystem", "N");
		rootShipment.setAttribute("OverrideModificationRules", "Y");
		rootShipment.setAttribute("PackAndHold", "N");
		rootShipment.setAttribute("SellerOrganizationCode", sellerOrganizationCode);
		rootShipment.setAttribute("ShipNode", shipNode);
		rootShipment.setAttribute("ReceivingNode", receivingNode);
		rootShipment.setAttribute("ShipmentKey", ShipmentKey);
		rootShipment.setAttribute("ShipmentNo", ShipmentNo);

		Element ScacAndService = changeShipment.createElement("ScacAndService");
		ScacAndService.setAttribute("ScacAndServiceKey", "20110627155257363333");
		rootShipment.appendChild(ScacAndService);

		Element ToAddress = changeShipment.createElement("ToAddress");
		ToAddress.setAttribute("PersonInfoKey", personInfoKey);
		rootShipment.appendChild(ToAddress);

		Element ShipmentLines = XMLUtil.appendChild(changeShipment, changeShipment.getDocumentElement(), "ShipmentLines", null);
		Element ShipmentLine = null;
		int j;

		/*
		 * Retrieve elements into the ShipmentLines for Changeshipment from the
		 * inDoc
		 */

		NodeList segmentedItemList = segmentedItems.getElementsByTagName("SegmentedItem");
		int seglength = segmentedItemList.getLength();

		for (j = 0; j < seglength; j++) {

			Element getShipmentLineListElement = (Element) segmentedItemList.item(j);

			// if (getShipmentLineListElement != null) {

			String itemID1 = getShipmentLineListElement.getAttribute("segmentedItem");

			String quantity = getShipmentLineListElement.getAttribute("segQuantity");

			ShipmentLine = XMLUtil.appendChild(changeShipment, ShipmentLines, "ShipmentLine", null);
			ShipmentLine.setAttribute("Action", "Create");
			ShipmentLine.setAttribute("ItemID", itemID1);
			ShipmentLine.setAttribute("ProductClass", "GOOD");
			ShipmentLine.setAttribute("Quantity", quantity);
			ShipmentLine.setAttribute("Segment", segmentNo);
			ShipmentLine.setAttribute("SegmentType", "MTO");
			ShipmentLine.setAttribute("SubLineNo", "0");
			ShipmentLine.setAttribute("UnitOfMeasure", "EACH");
			ShipmentLines.appendChild(ShipmentLine);
			// }
		}
		log.verbose(" ChangeShipment Input " + XMLUtil.getXMLString(changeShipment));

		Document ChangeShipmentOutput = api.changeShipment(env, changeShipment);

		log.verbose("ChangeShipment output XML" + XMLUtil.getXMLString(ChangeShipmentOutput));

	}

	/*
	 * This method gets the current sequence value from db which is used as
	 * segment number
	 */
	public String callSegmentNo(YFSEnvironment env) {

		YFSContext oEnv = (YFSContext) env;
		long seqSegmentNo = oEnv.getNextDBSeqNo("EXTN_SEQUENCE_VW");

		String segmentNo = String.valueOf(seqSegmentNo);

		log.verbose("SeqSegmentNo " + segmentNo);
		return segmentNo;
	}

	public String callGetLocationDetails(YFSEnvironment env, String locationId, String shipNode) throws Exception {
		log.verbose("Inside callGetLocationDetails ");
		String locationType = "";
		Document getLocationInDoc = XMLUtil.createDocument("Location");
		Element rootLocation = getLocationInDoc.getDocumentElement();
		rootLocation.setAttribute("LocationId", locationId);
		rootLocation.setAttribute("Node", shipNode);
		log.verbose("getLocationDetails Input " + XMLUtil.getXMLString(getLocationInDoc));

		Document getLocationDetailsTemplate = XMLUtil.getDocument("<Location LocationId=\"\" LocationType=\"\" ZoneId=\"\">" + "</Location>");
		env.setApiTemplate("getLocationDetails", getLocationDetailsTemplate);

		Document getLocationDetails = api.getLocationDetails(env, getLocationInDoc);

		log.verbose("getLocationDetails output " + XMLUtil.getXMLString(getLocationDetails));
		env.clearApiTemplates();

		Element eleGetLocationDetails = getLocationDetails.getDocumentElement();

		String locationType1 = eleGetLocationDetails.getAttribute("LocationType");
		String ZoneId = eleGetLocationDetails.getAttribute("ZoneId");
		try {
			// get number of business days need to deliver shipment from common
			// code list
			Document getCommonCodeListInputXML = XMLUtil.createDocument("CommonCode");
			getCommonCodeListInputXML.getDocumentElement().setAttribute("CodeType", "ZoneID");
			Document outXML = AcademyUtil.invokeAPI(env, "getCommonCodeList", getCommonCodeListInputXML);
			if (outXML != null) {
				NodeList commonCodeList = outXML.getElementsByTagName("CommonCode");
				if (commonCodeList != null && !YFCObject.isVoid(commonCodeList)) {
					log.verbose("CommonCodeList for zonID  : " + commonCodeList.toString());
					int iLength = commonCodeList.getLength();
					for (int i = 0; i < iLength; i++) {
						Element commonCode = (Element) commonCodeList.item(i);
						String codeValue = commonCode.getAttribute("CodeValue");
						log.verbose("SCACAndService : " + ZoneId);
						log.verbose("Code value : " + codeValue);
						log.verbose("zonID : " + ZoneId);
						log.verbose("Code value : " + codeValue);

						if (!ZoneId.equalsIgnoreCase(codeValue) && locationType1.equals("REGULAR")) {
							locationType = "REGULAR";
						} else {
							locationType = "NONREGULAR";
							break;
						}
					}
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return locationType;
	}

	private NodeList getLocationInventoryListAsc(YFSEnvironment env, NodeList getLocationInventoryList, String shipNode) throws Exception {

		for (int i = 0; i < getLocationInventoryList.getLength(); i++)

		{

			Element getLocationInventoryListElement = ((Element) getLocationInventoryList.item(i));

			String locationId = getLocationInventoryListElement.getAttribute("LocationId");

			String seqNo = callGetLocationDetailsforseq(env, locationId, shipNode);
			String locationType = callGetLocationDetails(env, locationId, shipNode);
			getLocationInventoryListElement.setAttribute("Seqno", seqNo);
			getLocationInventoryListElement.setAttribute("locationType", locationType);

		}
		Document outdoc = getSequence(env, getLocationInventoryList);
		NodeList locationInventoryList = outdoc.getElementsByTagName("LocationInventory");

		return locationInventoryList;
	}

	public String callGetLocationDetailsforseq(YFSEnvironment env, String locationId, String shipNode) throws Exception {
		log.verbose("Inside callGetLocationDetails ");

		Document getLocationInDoc = XMLUtil.createDocument("Location");
		Element rootLocation = getLocationInDoc.getDocumentElement();
		rootLocation.setAttribute("LocationId", locationId);
		rootLocation.setAttribute("Node", shipNode);
		log.verbose("getLocationDetails Input " + XMLUtil.getXMLString(getLocationInDoc));

		Document getLocationDetailsTemplate = XMLUtil.getDocument("<Location LocationId=\"\" MoveOutSeqNo=\"\" >" + "</Location>");
		env.setApiTemplate("getLocationDetails", getLocationDetailsTemplate);

		Document getLocationDetails = api.getLocationDetails(env, getLocationInDoc);

		log.verbose("getLocationDetails output " + XMLUtil.getXMLString(getLocationDetails));
		env.clearApiTemplates();

		Element eleGetLocationDetails = getLocationDetails.getDocumentElement();

		String moveOutSeqNo = eleGetLocationDetails.getAttribute("MoveOutSeqNo");

		return moveOutSeqNo;
	}
	public double callAvailableToSegment(Element getLocationInventoryListElement) {

		log.verbose("Inside callAvailableToSegment");
		log.verbose("Inside callAvailableToSegment");
		log.verbose("getLocationInventoryListElement>>><<<<" + XMLUtil.getElementXMLString(getLocationInventoryListElement));

		NodeList itemInventoryDetailList = getLocationInventoryListElement.getElementsByTagName("ItemInventoryDetail");
		int inventoryLentgth = itemInventoryDetailList.getLength();
		double segmentedQuantity = 0;
		double unsegmentedQuantity = 0;

		for (int m = 0; m < inventoryLentgth; m++) {
			Element eleItemInventoryDetail = (Element) itemInventoryDetailList.item(m);
			String segmentNumber = eleItemInventoryDetail.getAttribute("Segment");
			String Quantity = eleItemInventoryDetail.getAttribute("Quantity");
			String palletId = eleItemInventoryDetail.getAttribute("PalletId");
			String caseId = eleItemInventoryDetail.getAttribute("CaseId");
			double segQty = Double.parseDouble(Quantity);
			log.verbose("segm*******" + Double.toString(segQty));

			if (!(segmentNumber.equalsIgnoreCase("")) || !palletId.equalsIgnoreCase("") || !caseId.equalsIgnoreCase("")) {
				log.verbose("inside sgmented");
				segmentedQuantity = segmentedQuantity + segQty;
			} else {
				log.verbose("inside unssgmented");
				unsegmentedQuantity = unsegmentedQuantity + segQty;
			}

		}
		log.verbose("segmentedQuantity{}{}{}" + Double.toString(unsegmentedQuantity));

		return unsegmentedQuantity;
	}
	public Document getSequence(YFSEnvironment env, NodeList locationInventoryList) throws Exception {
		ArrayList<Element> al = new ArrayList<Element>();

		int l = locationInventoryList.getLength();
		for (int i = 0; i < l; i++) {
			Element locationInventoryListElement = (Element) locationInventoryList.item(i);
			String locType = locationInventoryListElement.getAttribute("locationType");

			if (locType.equalsIgnoreCase("Regular")) {
				al.add(locationInventoryListElement);
				log.verbose("elmenetlocType" + XMLUtil.getElementXMLString(locationInventoryListElement));
			}
		}
		Collections.sort(al, new SortElementByAttributeForSeq("Seqno"));

		Document newNodeInventory = YFCDocument.createDocument("NodeInventory").getDocument();
		Element rootElement = newNodeInventory.getDocumentElement();
		for (int index = 0; index < al.size(); index++) {
			Element locationInventoryElement = (Element) al.get(index);
			rootElement.appendChild(newNodeInventory.importNode(locationInventoryElement, true));
		}
		log.verbose("sorted doc\n" + YFCDocument.getDocumentFor(newNodeInventory));
		return newNodeInventory;

	}
}

class SortElementByAttributeForSeq implements Comparator<Element> {

	private String attr;

	public SortElementByAttributeForSeq(String attr) {
		this.attr = attr;
	}

	public int compare(Element ele1, Element ele2) {
		Double seqNo1 = Double.parseDouble(ele1.getAttribute(attr));
		Double seqNo2 = Double.parseDouble(ele2.getAttribute(attr));
		return seqNo1.compareTo(seqNo2);
	}

}
 