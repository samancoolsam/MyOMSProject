package com.academy.ecommerce.sterling.inventory.api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author <a href="mailto:Kavya.Parvatham@cognizant.com">Kavya Parvatham</a>,
 *         Created on 01/08/2014. This class will process the no Inventory
 *         message received from Exeter Based on the shorted quantity received
 *         from Exeter this class will perform a complete short pick/partial
 *         short pick scenarios
 * 
 */
public class AcademyProcessNoInventoryMsg {

	/**
	 * Instance of logger
	 */
	private static final YFCLogCategory log = YFCLogCategory
			.instance(AcademyProcessNoInventoryMsg.class);

	String strOriginalShipQty = "";

	double qty = 0.00;

	double dShortageQty = 0.00;

	/*
	 * This method will compare the Shortage qty received from Exeter with the
	 * total ShipmentLine qty and if it is equal ten performs complete short
	 * pick scenario else performs partial short pick scenario
	 */
	public Document processNoInventoryMsg(YFSEnvironment env, Document inDoc)
			throws Exception {		
		log
				.verbose("Input to AcademyProcessNoInventoryMsg-processNoInventoryMsg()::"
						+ XMLUtil.getXMLString(inDoc));
		
		//Start WN-643 - Initialization
		Document docChangeShipmentOutput = null;
		Document docChangeShipmentInputToCancel = null;
		Element eleRootShipment = null;
		Element eleRootChangeShipment = null;
		String strTotalQuantity = null;
		String strShipmentKey = null;
		//End WN-643 - Initialization

		String strShipmentNo = inDoc.getDocumentElement().getAttribute(
				AcademyConstants.ATTR_SHIPMENT_NO);
		
		// START: SHIN-10
		String strShipNode = inDoc.getDocumentElement()
				.getAttribute("ShipNode");
		// END: SHIN-10

		log.verbose("Shipment # from Exeter::" + strShipmentNo);

		String strShortageQty = ((Element) inDoc.getElementsByTagName(
				AcademyConstants.ELE_SHIPMENT_LINE).item(0))
				.getAttribute(AcademyConstants.ATTR_QUANTITY);
		log.verbose("Shortage Qty from Exeter::" + strShortageQty);
		dShortageQty = Double.parseDouble(strShortageQty);
		log.verbose("dShortageQty::" + dShortageQty);

		Document getShipmentListOutDoc = invokeGetShipmentList(env,
				strShipmentNo);

		NodeList nlShipListShipmentLine = getShipmentListOutDoc
				.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);

		if (nlShipListShipmentLine.getLength() > 1) {
			log.verbose("Current Shipment has more than one line");
			for (int i = 0; i < nlShipListShipmentLine.getLength(); i++) {

				Element eleShipListShipmentLine = (Element) nlShipListShipmentLine
						.item(i);
				log.verbose("eleShipListShipmentLine :::: "
						+ XMLUtil.getElementXMLString(eleShipListShipmentLine));
				String strOriginalQty = eleShipListShipmentLine
						.getAttribute(AcademyConstants.ATTR_QUANTITY);
				log.verbose("Original Shipment Quantity::" + strOriginalQty);

				qty = qty + Double.parseDouble(strOriginalQty);

				
				log
						.verbose("Original Shipment Quantity after adding all sl's::"
								+ qty);

				/*strOriginalShipQty = String.valueOf(qty);

				 log
				 .verbose("Original Shipment Quantity after adding all sl's in string format::"
				 + strOriginalShipQty);
				 */
			}

			//strOriginalShipQty = strOriginalShipQty + "0";
		} else {
			log.verbose("Current Shipment has only one line");
			strOriginalShipQty = ((Element) getShipmentListOutDoc
					.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE)
					.item(0)).getAttribute(AcademyConstants.ATTR_QUANTITY);
			qty = Double.parseDouble(strOriginalShipQty);
			log.verbose("shipment qty incase of one line::" + qty);
		}

		log.verbose("Original Shipment Quantity::" + strOriginalShipQty);

		//if (strOriginalShipQty.equals(strShortageQty)) 
		if (qty == dShortageQty) {
			log.verbose("Invoking Complete Short Pick Scenario");
			invokeMultiApiForCompleteShortPick(env, inDoc,
					getShipmentListOutDoc);
		} else {

			log.verbose("Invoking Partial Short Pick Scenario");
			String strShipmentLineNo = ((Element) inDoc.getElementsByTagName(
					AcademyConstants.ELE_SHIPMENT_LINE).item(0))
					.getAttribute("ShipmentLineNo");

			log.verbose("strShipmentLineNo:: " + strShipmentLineNo);
			log.verbose("getShipmentList Output&&&&&&&&&&::"
					+ XMLUtil.getXMLString(getShipmentListOutDoc));

			NodeList nlShipLine = XMLUtil.getNodeListByXPath(
					getShipmentListOutDoc,
					"/Shipments/Shipment/ShipmentLines/ShipmentLine[@ShipmentLineNo='"
							+ strShipmentLineNo + "']");

			log.verbose("nlShipLine length " + nlShipLine.getLength());

			Element eleShipLine = XMLUtil.getElementByXPath(
					getShipmentListOutDoc,
					"/Shipments/Shipment/ShipmentLines/ShipmentLine[@ShipmentLineNo='"
							+ strShipmentLineNo + "']");

			log.verbose("eleShipLine value::"
					+ XMLUtil.getElementXMLString(eleShipLine));

			String strShipLineQty = eleShipLine
					.getAttribute(AcademyConstants.ATTR_QUANTITY);

			double dOriginalQty = Double.parseDouble(strShipLineQty);
			//double dShortageQty = Double.parseDouble(strShortageQty);

			double dAvailQty = dOriginalQty - dShortageQty;
			String strAvailQty = String.valueOf(dAvailQty);
			log.verbose("Available Quantity to update under Shipment Line::"
					+ strAvailQty);

			// START: SHIN-10
			
			//Start WN-643 Cancel Shipment once all the units are removed by Shared Inventory "NO INVENTORY" updates.
			docChangeShipmentOutput = invokeChangeShipmentForPartialShortPick(env, inDoc, getShipmentListOutDoc, strAvailQty);
			eleRootShipment= docChangeShipmentOutput.getDocumentElement();
			strTotalQuantity = eleRootShipment.getAttribute(AcademyConstants.ATTR_TOTAL_QTY);
			strShipmentKey = eleRootShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			log.verbose("Toatl quantity of a Shipment " +strShipmentKey +" :: is " +strTotalQuantity);
			
			if(!YFCObject.isVoid(strTotalQuantity) && strTotalQuantity.contains(AcademyConstants.STR_ZERO_IN_DECIMAL))
			{
				docChangeShipmentInputToCancel = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
				eleRootChangeShipment = docChangeShipmentInputToCancel.getDocumentElement();
				eleRootChangeShipment.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.VAL_CANCEL);
				eleRootChangeShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
				log.verbose("changeShipment input to cancel a Shipment :: "+ XMLUtil.getXMLString(docChangeShipmentInputToCancel));
				AcademyUtil.invokeAPI(env,AcademyConstants.API_CHANGE_SHIPMENT, docChangeShipmentInputToCancel);
				log.verbose("changeShipment is completed");
			}		
			//End WN-643 Cancel Shipment once all the units are removed by Shared Inventory "NO INVENTORY" updates.
		}

		/*
		 * invoking multiApi demand summary as part of partial short pick and
		 * complete short pick
		 */
		Document getDemandSummaryOutDoc = invokeMultiApiDemandSummary(env,
				inDoc, strShipNode);

		/*
		 * invoking getInventoryMismatch API as part of partial short pick and
		 * complete short pick
		 */
		invokeGetInventoryMismatch(env, strShipNode, getDemandSummaryOutDoc);

		// END: SHIN-10
		//OMNI-7887 Start
		String docType = XPathUtil.getString(getShipmentListOutDoc, AcademyConstants.XPATH_SHP_DOCUMENT_TYPE);
		// Added document type here to use in inv node control
		if(!YFCObject.isVoid(docType) && docType.equals(AcademyConstants.SALES_DOCUMENT_TYPE))
			inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_DOC_TYPE, docType);
		else if(!YFCObject.isVoid(docType) && docType.equals(AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE))
			inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_DOC_TYPE, docType);
		//OMNI-78887 End
		//OMNI-7244
		inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_SOURCE, AcademyConstants.STR_SOURCE_WMS);
		AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACAD_CREATE_INV_NODE_CONTROL, inDoc);
		//OMNI-7244

		return inDoc;
	}

	/**
	 * @param env
	 * @param strShipmentNo
	 * @return
	 * @throws Exception
	 */
	private Document invokeGetShipmentList(YFSEnvironment env,
			String strShipmentNo) throws Exception {

		Document getShipmentListInDoc = XMLUtil
				.createDocument(AcademyConstants.ELE_SHIPMENT);
		getShipmentListInDoc.getDocumentElement().setAttribute(
				AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);

		log.verbose("getShipmentList Input::"
				+ XMLUtil.getXMLString(getShipmentListInDoc));

		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST,
				"global/template/api/getShipmentListForSharedInventory.xml");

		Document getShipmentListOutDoc = AcademyUtil.invokeAPI(env,
				AcademyConstants.API_GET_SHIPMENT_LIST, getShipmentListInDoc);

		log.verbose("getShipmentList Output::"
				+ XMLUtil.getXMLString(getShipmentListOutDoc));

		return getShipmentListOutDoc;
	}

	/**
	 * @throws Exception
	 * 
	 */

	// START: SHIN-10
	/*
	 * Modified below invokeMultiApiForPartialShortPick() as part of
	 * SHIN-10 for direct change Shipment API invocations instead of multi api
	 */
	

	/*private void invokeMultiApiForPartialShortPick(YFSEnvironment env,
			Document inDoc, Document getShipmentListOutDoc, String strAvailQty)
			throws Exception {

		Document multiApiDoc = XMLUtil.createDocument("MultiApi");

		Document changeShipmentInDoc = prepareInputForChangeShipmentToModQty(
				getShipmentListOutDoc, inDoc, strAvailQty);
		includeInMultiApi(multiApiDoc,
				changeShipmentInDoc.getDocumentElement(),
				AcademyConstants.API_CHANGE_SHIPMENT);

		Document getInventoryMismatchInDoc = prepareInputForGetInventoryMismatch(
				inDoc, getShipmentListOutDoc);
		includeInMultiApi(multiApiDoc, getInventoryMismatchInDoc
				.getDocumentElement(), AcademyConstants.GET_INVENTORY_MISMATCH);

		log.verbose("multiApiDoc Input::" + XMLUtil.getXMLString(multiApiDoc));

		AcademyUtil.invokeAPI(env, "multiApi", multiApiDoc);

	}*/
	
	// Created the below method to invoke change shipment API
	
	private Document invokeChangeShipmentForPartialShortPick(YFSEnvironment env,
			Document inDoc, Document getShipmentListOutDoc, String strAvailQty)
			throws Exception {

		/*
		 * Preparing the input for modified quantity for partial short pick
		 * scenario to invoke change shipment API
		 */

		Document changeShipmentInDoc = prepareInputForChangeShipmentToModQty(
				getShipmentListOutDoc, inDoc, strAvailQty);

		// Template for changeShipment API

		env.setApiTemplate(AcademyConstants.API_CHANGE_SHIPMENT,
				"global/template/api/changeShipment.xml");

		// changeShipment API invocation is done

		Document getChangeShipmentOutDoc = AcademyUtil.invokeAPI(env,
				AcademyConstants.API_CHANGE_SHIPMENT, changeShipmentInDoc);

		log.verbose("changeShipmentForPartialShortPick Output::"
				+ XMLUtil.getXMLString(getChangeShipmentOutDoc));
		return getChangeShipmentOutDoc;

	}

	// END: SHIN-10



	/**
	 * @throws Exception
	 * 
	 */
	
	// START: SHIN-10
	/*
	 * Modified below invokeMultiApiForCompleteShortPick as part of SHIN-10 for
	 * direct change Shipment API invocations instead of multi api.
	 */

	private void invokeMultiApiForCompleteShortPick(YFSEnvironment env,
			Document inDoc, Document getShipmentListOutDoc) throws Exception {

		/*
		 * Preparing the input for complete short pick scenario to invoke change
		 * shipment API
		 */

		Document changeShipmentInDoc = prepareInputForChangeShipment(
				getShipmentListOutDoc, inDoc);

		// Template for changeShipment API

		env.setApiTemplate(AcademyConstants.API_CHANGE_SHIPMENT,
				"global/template/api/changeShipment.xml");

		// changeShipment API invocation is done

		Document getChangeShipmentOutDoc = AcademyUtil.invokeAPI(env,
				AcademyConstants.API_CHANGE_SHIPMENT, changeShipmentInDoc);

		log.verbose("changeShipmentForCompleteShortPick Output::"
				+ XMLUtil.getXMLString(getChangeShipmentOutDoc));

	}

	// END: SHIN-10


	private static Document prepareInputForChangeShipment(
			Document getShipmentListOutDoc, Document inDoc) throws Exception {
		Document changeShipmentInDoc = XMLUtil
				.createDocument(AcademyConstants.ELE_SHIPMENT);

		changeShipmentInDoc.getDocumentElement().setAttribute(
				AcademyConstants.ATTR_ACTION, AcademyConstants.VAL_CANCEL);
		changeShipmentInDoc.getDocumentElement().setAttribute(
				"BackOrderRemovedQuantity", "Y");
		changeShipmentInDoc.getDocumentElement().setAttribute(
				AcademyConstants.ATTR_SHIPMENT_KEY,
				((Element) getShipmentListOutDoc.getElementsByTagName(
						"Shipment").item(0))
						.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));

		Element eleShipmentLines = changeShipmentInDoc
				.createElement(AcademyConstants.ELE_SHIPMENT_LINES);
		Element eleShipmentLine = changeShipmentInDoc
				.createElement(AcademyConstants.ELE_SHIPMENT_LINE);

		changeShipmentInDoc.getDocumentElement().appendChild(eleShipmentLines);
		eleShipmentLines.appendChild(eleShipmentLine);

		String strShipmentLineNo = ((Element) inDoc.getElementsByTagName(
				"ShipmentLine").item(0)).getAttribute("ShipmentLineNo");

		NodeList eleShipLine = XMLUtil.getNodeListByXPath(XMLUtil
				.getDocumentForElement(getShipmentListOutDoc
						.getDocumentElement()),
				"/Shipments/Shipment/ShipmentLines/ShipmentLine[@ShipmentLineNo='"
						+ strShipmentLineNo + "']");

		eleShipmentLine.setAttribute(AcademyConstants.ATTR_QUANTITY, "0.0");
		log.verbose("eleShipLine.getLength() ::" + eleShipLine.getLength());

		if (eleShipLine.getLength() > 0) {
			eleShipmentLine
					.setAttribute(
							AcademyConstants.ATTR_SHIPMENT_LINE_KEY,
							((Element) eleShipLine.item(0))
									.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY));
		}

		log.verbose("changeShipmentInDoc for Short Pick in chng ship ::"
				+ XMLUtil.getXMLString(changeShipmentInDoc));

		return changeShipmentInDoc;
	}


	// getInventoryMismatch input is prepared

	private Document prepareInputForGetInventoryMismatch(String strShipNode,
			Document getDemandSummaryOutDoc) throws Exception {
		// START: SHIN-10
		String strItemID = "";
		String strProductClass = "";
		String strUOM = "";
		// END: SHIN-10
		String strDemandAllocatedQuantity = "";
		String strDemandExtnAllocatedQuantity = "";
		String strDemandType = "";
		Document getInventoryMismatchInDoc = XMLUtil
				.createDocument(AcademyConstants.ELE_INV_SNAPSHOT);

		getInventoryMismatchInDoc.getDocumentElement().setAttribute(
				AcademyConstants.ATTR_APPLY_DIFF, "Y");

		Element eleShipNode = getInventoryMismatchInDoc
				.createElement("ShipNode");
		eleShipNode.setAttribute(AcademyConstants.ATTR_SHIP_NODE, strShipNode);
		getInventoryMismatchInDoc.getDocumentElement().appendChild(eleShipNode);

		// START: SHIN-10
		// Creating a nodelist from getDemandSummaryOutDoc

		NodeList nlDemandSummary = getDemandSummaryOutDoc
				.getElementsByTagName(AcademyConstants.ELE_DEMAND_SUMMARY);

		for (int i = 0; i < nlDemandSummary.getLength(); i++) {

			/*
			 * Created element eleInDocShipmentLine to fetch the item id for
			 * each line
			 */
			Element eleDemandSummary = (Element) nlDemandSummary.item(i);
			strItemID = eleDemandSummary.getAttribute(AcademyConstants.ITEM_ID);
			strProductClass = eleDemandSummary
					.getAttribute(AcademyConstants.ATTR_PROD_CLASS);
			strUOM = eleDemandSummary.getAttribute(AcademyConstants.ATTR_UOM);

			Element eleItem = getInventoryMismatchInDoc.createElement("Item");
			eleItem.setAttribute(AcademyConstants.ITEM_ID, strItemID);
			eleItem.setAttribute(AcademyConstants.INVENTORY_ORGANIZATION_CODE,
					AcademyConstants.PRIMARY_ENTERPRISE);
			eleItem.setAttribute(AcademyConstants.ATTR_PROD_CLASS,
					strProductClass);
			eleItem.setAttribute(AcademyConstants.ATTR_UOM, strUOM);
			eleShipNode.appendChild(eleItem);

			// creating a nodelist
			NodeList nlDemand = eleDemandSummary
					.getElementsByTagName(AcademyConstants.ELE_DEMAND);

			for (int j = 0; j < nlDemand.getLength(); j++) {
				Element eleDemand = (Element) nlDemand.item(j);
				strDemandType = eleDemand
						.getAttribute(AcademyConstants.ATTR_DEMAND_TYPE);

				Element eleSupplyDetails = getInventoryMismatchInDoc
						.createElement(AcademyConstants.SUPPLY_DETAILS);

				eleItem.appendChild(eleSupplyDetails);

				/*
				 * if the demand type is allocated then get the quantity from
				 * demand element and set it to the supply details element for
				 * the Supply type="ONHAND" if the demand type is allocated.ex
				 * then get the quantity from demand element and set it to the
				 * supply details element for the Supply type="ONHAND.ex"
				 */

				if ("ALLOCATED".equals(strDemandType)) {
					strDemandAllocatedQuantity = eleDemand
							.getAttribute(AcademyConstants.ATTR_QUANTITY);
					eleSupplyDetails.setAttribute(
							AcademyConstants.ATTR_QUANTITY,
							strDemandAllocatedQuantity);
					eleSupplyDetails.setAttribute(
							AcademyConstants.ATTR_SUPPLY_TYPE, "ONHAND");
				} else if ("ALLOCATED.ex".equals(strDemandType)) {
					strDemandExtnAllocatedQuantity = eleDemand
							.getAttribute(AcademyConstants.ATTR_QUANTITY);
					eleSupplyDetails.setAttribute(
							AcademyConstants.ATTR_QUANTITY,
							strDemandExtnAllocatedQuantity);
					eleSupplyDetails.setAttribute(
							AcademyConstants.ATTR_SUPPLY_TYPE, "ONHAND.ex");
				}

			}
		}
		// END:SHIN-10

		log.verbose("getInventoryMismatchInDoc for Short Pick::"
				+ XMLUtil.getXMLString(getInventoryMismatchInDoc));

		return getInventoryMismatchInDoc;

	}

	private Document prepareInputForChangeShipmentToModQty(
			Document getShipmentListOutDoc, Document inDoc, String strAvailQty)
			throws Exception {

		Document changeShipmentInDoc = XMLUtil
				.createDocument(AcademyConstants.ELE_SHIPMENT);

		changeShipmentInDoc.getDocumentElement().setAttribute(
				"BackOrderRemovedQuantity", "Y");
		changeShipmentInDoc.getDocumentElement().setAttribute(
				AcademyConstants.ATTR_SHIPMENT_KEY,
				((Element) getShipmentListOutDoc.getElementsByTagName(
						"Shipment").item(0))
						.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));

		Element eleShipmentLines = changeShipmentInDoc
				.createElement(AcademyConstants.ELE_SHIPMENT_LINES);
		Element eleShipmentLine = changeShipmentInDoc
				.createElement(AcademyConstants.ELE_SHIPMENT_LINE);

		changeShipmentInDoc.getDocumentElement().appendChild(eleShipmentLines);
		eleShipmentLines.appendChild(eleShipmentLine);

		String strShipmentLineNo = ((Element) inDoc.getElementsByTagName(
				"ShipmentLine").item(0)).getAttribute("ShipmentLineNo");

		NodeList eleShipLine = XMLUtil.getNodeListByXPath(XMLUtil
				.getDocumentForElement(getShipmentListOutDoc
						.getDocumentElement()),
				"/Shipments/Shipment/ShipmentLines/ShipmentLine[@ShipmentLineNo='"
						+ strShipmentLineNo + "']");

		// log.verbose("eleShipLine for matching shipment line element in mod
		// qty ::" +XMLUtil.getElementXMLString((Element)eleShipLine.item(0)));

		eleShipmentLine.setAttribute(AcademyConstants.ATTR_QUANTITY,
				strAvailQty);
		log.verbose("eleShipLine.getLength() ::" + eleShipLine.getLength());

		if (eleShipLine.getLength() > 0) {
			eleShipmentLine
					.setAttribute(
							AcademyConstants.ATTR_SHIPMENT_LINE_KEY,
							((Element) eleShipLine.item(0))
									.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY));
		}

		log.verbose("changeShipmentInDoc for Partial Short Pick::"
				+ XMLUtil.getXMLString(changeShipmentInDoc));

		return changeShipmentInDoc;

	}

	// START: SHIN-10
	/*
	 * Prepared the multiApi input to invoke getDemandSummary API for each item in InDoc
	 * XML from EXETER
	 */

	private Document prepareInputForMultiApiGetDemandSummary(Document inDoc,
			String strShipNode) throws Exception {

		String strItemID = "";

		Document multiApiDoc = XMLUtil.createDocument("MultiApi");

		/*
		 * NodeList is created to fetch the item id from total number of
		 * shipment lines
		 */

		NodeList nlInDocShipmentLine = inDoc
				.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);

		for (int i = 0; i < nlInDocShipmentLine.getLength(); i++) {

			/*
			 * Created element eleInDocShipmentLine to fetch the item id for
			 * each line
			 */

			Element eleInDocShipmentLine = (Element) nlInDocShipmentLine
					.item(i);

			// Getting the item id for corresponding shipment line
			strItemID = eleInDocShipmentLine
					.getAttribute(AcademyConstants.ATTR_ITEM_ID);

			log.verbose("ItemID :::: " + strItemID);

			// Created the input document for getDemandSummary API

			Document getDemandSummaryInDoc = XMLUtil
					.createDocument(AcademyConstants.ELE_DEMAND_SUMMARY);
			Element elegetDemandSummaryInDoc = getDemandSummaryInDoc
					.getDocumentElement();

			/*
			 * setting the item id for the corresponding item id in each
			 * shipment line
			 */

			elegetDemandSummaryInDoc.setAttribute(
					AcademyConstants.ATTR_ITEM_ID, strItemID);

			elegetDemandSummaryInDoc.setAttribute(
					AcademyConstants.ORGANIZATION_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
			elegetDemandSummaryInDoc.setAttribute(
					AcademyConstants.ATTR_SHIP_NODE, strShipNode);
			elegetDemandSummaryInDoc.setAttribute(
					AcademyConstants.ATTR_PROD_CLASS,
					AcademyConstants.PRODUCT_CLASS);
			elegetDemandSummaryInDoc.setAttribute(AcademyConstants.ATTR_UOM,
					AcademyConstants.UNIT_OF_MEASURE);

			includeInMultiApi(multiApiDoc, getDemandSummaryInDoc
					.getDocumentElement(), AcademyConstants.GET_DEMAND_SUMMARY);
		}

		log.verbose("getDemandSummaryInDoc for Short Pick::"
				+ XMLUtil.getXMLString(multiApiDoc));
		return multiApiDoc;
	}

	// END: SHIN-10
	
	// START: SHIN-10
	/*
	 * This invokeMultiApiDemandSummary() is created to
	 * invoke multiApi for getDemandSummary API.
	 */

	private Document invokeMultiApiDemandSummary(YFSEnvironment env,
			Document inDoc, String strShipNode) throws Exception {

		// Preparing the input for multiApi getDemandSummary

		Document multiApiDoc = prepareInputForMultiApiGetDemandSummary(inDoc,
				strShipNode);

		// Invoking multiApi

		Document getDemandSummaryOutDoc = AcademyUtil.invokeAPI(env,
				AcademyConstants.API_MULTI_API, multiApiDoc);

		log.verbose("multiApiOutputDoc Output::"
				+ XMLUtil.getXMLString(getDemandSummaryOutDoc));

		return getDemandSummaryOutDoc;

	}

	// END: SHIN-10

	// START: SHIN-10
	// This invokeGetInventoryMismatch()will prepare the input and invoke the
	// getInventoryMismatch API.

	private Document invokeGetInventoryMismatch(YFSEnvironment env,
			String strShipNode, Document getDemandSummaryOutDoc)
			throws Exception {

		// preparing the input for getInventoryMismatch
		Document getInventoryMismatchInDoc = prepareInputForGetInventoryMismatch(
				strShipNode, getDemandSummaryOutDoc);

		Document getInventoryMismatchOutDoc = AcademyUtil.invokeAPI(env,
				AcademyConstants.GET_INVENTORY_MISMATCH,
				getInventoryMismatchInDoc);

		log.verbose("getInvMismatch Output::"
				+ XMLUtil.getXMLString(getInventoryMismatchOutDoc));

		return getInventoryMismatchOutDoc;

	}

	// END: SHIN-10

	private void includeInMultiApi(Document multiApiDoc, Element apiInput,
			String apiName) {

		log.verbose("apiInput ************"
				+ XMLUtil.getElementXMLString(apiInput));
		log.verbose("MultiApi Doc1 ::" + XMLUtil.getXMLString(multiApiDoc));
		// Fetch the root element
		Element multiApiDocRoot = multiApiDoc.getDocumentElement();
		// Create element API
		Element api = multiApiDoc.createElement("API");
		multiApiDocRoot.appendChild(api);

		// set attribute Name
		api.setAttribute("Name", apiName);
		// Create element Input
		Element input = multiApiDoc.createElement("Input");

		api.appendChild(input);

		// Append the childelement to input element
		Node importEle = multiApiDoc.importNode(apiInput, true);

		input.appendChild(importEle);
		log.verbose("MultiApi Doc prepared ::"
				+ XMLUtil.getXMLString(multiApiDoc));
	}
}
