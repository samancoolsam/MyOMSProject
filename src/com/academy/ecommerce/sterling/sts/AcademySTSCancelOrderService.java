package com.academy.ecommerce.sterling.sts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/*##################################################################################
 *
 * Project Name                : STS Project
 * Module                      : OMS
 * Author                      : CTS
 * Date                        : 01-JUL-2020 
 * Description				  : This class is to validate and cancel TO and SO lines
 * Change Revision
 * ---------------------------------------------------------------------------------
 * Date            Author         		Version#       Remarks/Description                      
 * ---------------------------------------------------------------------------------
 * 01-JUL-2020		CTS  	 			  1.0           	Initial version
 * 
 * ##################################################################################*/

public class AcademySTSCancelOrderService {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySTSCancelOrderService.class);


	public Document cancelOrder(YFSEnvironment env, Document inXML) throws Exception {
		log.verbose("Begin of AcademySTSCancelOrderService.cancelOrder() method");

		String strModificationReference1 = inXML.getDocumentElement()
				.getAttribute(AcademyConstants.ATTR_MODIFICATION_REFERENCE_1);

		if (YFCObject.isVoid(strModificationReference1)) {
			strModificationReference1 = XPathUtil.getString(inXML, "/Order/OrderAudit/@Reference1");
		}
		
		log.verbose(" strModificationReference1 :: " + strModificationReference1);

		if (!YFCObject.isVoid(strModificationReference1)
				&& (strModificationReference1.equals(AcademyConstants.STR_TO_INITIATED_CANCEL)
						|| strModificationReference1.equals(AcademyConstants.STR_CONTAINER_LOST))) {
			log.verbose("Modification Reference is not Null. Skipping logic :: " + strModificationReference1);
			return inXML;
		}

		log.verbose("XML is a Sales Order. Validate Status");
		// Cancellation triggered from WCS/ Call Center or Sterling Console.
		log.verbose(":: inXML.getNodeName() :: " + inXML.getDocumentElement().getNodeName());

		if (inXML.getDocumentElement().getNodeName().equals(AcademyConstants.ELE_ORDER)) {
			log.verbose(":: SO Cancellation Validate and cancel TO Shipment or Update ExtnIsSOCancelled=Y.");
			getAndValidateTransferOrder(env, inXML);

		} else if (inXML.getDocumentElement().getNodeName().equals(AcademyConstants.ELE_CONTAINER)) {
			log.verbose(":: Cancellation for Lost Scenarios. Cancel SO and Update ExtnIsSOCancelled=Y.");

			processLostContainerAndCanelSO(env, inXML);
		}
		log.verbose("End of AcademySTSCancelOrderService.cancelOrder() method");
		return inXML;
	}

	/**
	 * This method is used to validate the TO Lines pending Cancellation by invoking
	 * a changeOrder API
	 * 
	 * @param env,
	 * @param inXML
	 */
	private void getAndValidateTransferOrder(YFSEnvironment env, Document docSalesOrder) throws Exception {

		log.verbose("Begin of AcademySTSCancelOrderService.getAndValidateTransferOrder() method");
		Document docinputFromEnv = null;		
		Element eleSalesOrder = docSalesOrder.getDocumentElement();
		NodeList nlSTSSalesOrderLines = XPathUtil.getNodeList(eleSalesOrder,
				"/Order/OrderLines/OrderLine[@FulfillmentType='STS']");
	
		if (nlSTSSalesOrderLines.getLength() > 0) {
			log.verbose("STS lines present :: ");
			Document docTransferOrderLineList = null;
			String strSalesOHK = eleSalesOrder.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
			/**The below logic is written for STS Line level Cancellation. We get the transaction Obj
			 * from the AcademyOrderCancellationFromWCS, docInputFromEnv is Line level Change Order
			 * Input to the changeOrder in AcademyOrderCancellationFromWCS.
			 * getTransferLineDetailsForWCSCancellation is used to call getCompleteOrderLineList with
			 * OrderLineKey to get only one line details
			 */
			// OMNI-30152 Start
			docinputFromEnv = (Document)env.getTxnObject("IsLineLevelCancellationFromWCS");
			if(!YFCObject.isNull(docinputFromEnv)) {
			
				docTransferOrderLineList = getTransferLineDetailsForWCSCancellation(env, docinputFromEnv);
			// OMNI-30152 End
			}else {
				docTransferOrderLineList = getTransferOrderDetails(env, strSalesOHK);
			}
						
			String strCancelReason = XPathUtil.getString(docSalesOrder, "/Order/OrderAudit/@ReasonCode");
			log.verbose("strCancelReason :: " + strCancelReason);
			
			if (!YFCObject.isVoid(docTransferOrderLineList)
					&& docTransferOrderLineList.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE).getLength() > 0) {
				for (int index = 0; index < nlSTSSalesOrderLines.getLength(); index++) {
					Element eleSalesOrderLine = (Element) nlSTSSalesOrderLines.item(index);
					String strChangeInQuantity = eleSalesOrderLine
							.getAttribute(AcademyConstants.ATTR_CHANGE_IN_ORDERED_QTY);
					if (strChangeInQuantity.startsWith("-")) {
						strChangeInQuantity = strChangeInQuantity.replace("-", "");
					}
					log.verbose("strChangeInQuantity: " + strChangeInQuantity);
					if (!YFCObject.isVoid(strChangeInQuantity)) {
						String strSOOrderLineKey = eleSalesOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);

						Element eleTOOrderLine = (Element) XPathUtil.getNode(docTransferOrderLineList,
								"/OrderLineList/OrderLine[@ChainedFromOrderLineKey='" + strSOOrderLineKey + "']");
						if (!YFCObject.isVoid(eleTOOrderLine)) {
							log.verbose(" Updated TO OrderLine with Quantity");
							eleTOOrderLine.setAttribute(AcademyConstants.ATTR_CHANGE_IN_ORDERED_QTY,
									strChangeInQuantity);
						}

					}

				}

				env.setTxnObject(AcademyConstants.STR_TO_LINE_LIST, docTransferOrderLineList);

				NodeList nlTransferOrderLine = XPathUtil.getNodeList(docTransferOrderLineList,
						"/OrderLineList/OrderLine[@ChangeInOrderedQty > 0 ]");
				NodeList nlTransferShipmentLine = XPathUtil.getNodeList(docTransferOrderLineList,
						"/OrderLineList/OrderLine[@ChangeInOrderedQty > 0 ]/ShipmentLines/ShipmentLine");

				if (nlTransferOrderLine.getLength() > 0 || nlTransferShipmentLine.getLength() > 0) {
					validateAndCancelTransferOrder(env, nlTransferOrderLine, nlTransferShipmentLine, strCancelReason, true, false);
				} else {
					log.verbose("No Valid Transfer Orders Lines or Shipment Lines available for cancel :: ");
				}
			} else {
				log.verbose("No Valid Transfer Orders Available :: ");
			}
		} else {
			log.verbose("No STS Lines :: ");
		}
		log.verbose("End of AcademySTSCancelOrderService.getAndValidateTransferOrder() method");
	}

	/**
	 * This method is used to validate the SO Lines pending Cancellation by invoking
	 * a changeOrder API
	 * 
	 * @param env,
	 * @param inXML
	 */
	public Document processTOInitiatedCancellations(YFSEnvironment env, Document inXML) throws Exception {
		log.verbose("Begin of AcademySTSCancelOrderService.processTOInitiatedCancellations() method");

		
		NodeList nlShipmentLine = inXML.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		Document docTOOrderLines = XMLUtil.createDocument(AcademyConstants.ELE_ORDER_LINES);

		String strExtnShortpickReasonCode = XPathUtil.getString(inXML, "/Shipment/Extn/@ExtnShortpickReasonCode");
		if(YFCObject.isVoid(strExtnShortpickReasonCode)) {
			strExtnShortpickReasonCode = AcademyConstants.STR_INVENTORY_SHORTAGE;			
		}
		
		for (int index = 0; index < nlShipmentLine.getLength(); index++) {
			Element eleShipmentLine = (Element) nlShipmentLine.item(index);
			String strQuantityReduced = eleShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY_REDUCED);
			if (!YFCObject.isVoid(strQuantityReduced)) {

				Element eleOrderLine = XMLUtil.getFirstElementByName(eleShipmentLine, AcademyConstants.ELE_ORDER_LINE);
				eleOrderLine.setAttribute(AcademyConstants.ATTR_CHANGE_IN_ORDERED_QTY, strQuantityReduced);
				eleOrderLine.setAttribute("ShipmentActualQty", eleShipmentLine.getAttribute("ActualQuantity"));

				Element eleTOCancelORderLine = docTOOrderLines.createElement(AcademyConstants.ELE_ORDER_LINE);
				XMLUtil.copyElement(docTOOrderLines, eleOrderLine, eleTOCancelORderLine);
				docTOOrderLines.getDocumentElement().appendChild(eleTOCancelORderLine);
			}
		}
		NodeList nlTOOrderLineToCancel = XPathUtil.getNodeList(docTOOrderLines, AcademyConstants.XPATH_ORDERLINE);
		log.verbose(" lTOOrderLine size :: " + nlTOOrderLineToCancel.getLength());
		if (nlTOOrderLineToCancel.getLength() > 0) {
			validateAndCancelTransferOrder(env, nlTOOrderLineToCancel, null, strExtnShortpickReasonCode, false, false);
		}

		log.verbose("End of AcademySTSCancelOrderService.processTOShipmentCancellation() method");
		return inXML;
	}

	/**
	 * This method is used to update Containers as lost and cancel SO Lines
	 * 
	 * @param env,
	 * @param inXML
	 */
	private void processLostContainerAndCanelSO(YFSEnvironment env, Document inXML) throws Exception {

		log.verbose("Begin of AcademySTSCancelOrderService.processLostContainerAndCanelSO() method");

		HashMap<String, String> hmSOCancellableLines = new HashMap<String, String>();

		NodeList nlSalesOrderLine = XPathUtil.getNodeList(inXML, "/Container/SalesOrder/OrderLines/OrderLine");
		log.verbose(" nlSalesOrderLine size :: " + nlSalesOrderLine.getLength());

		for (int index = 0; index < nlSalesOrderLine.getLength(); index++) {
			Element eleSalesOrderLine = (Element) nlSalesOrderLine.item(index);
			hmSOCancellableLines.put(eleSalesOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY),
					eleSalesOrderLine.getAttribute(AcademyConstants.ATTR_ORDERED_QTY));
		}
		String strOrderHeaderKey = XPathUtil.getString(inXML, "/Container/SalesOrder/@OrderHeaderKey");
		hmSOCancellableLines.put(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);

		log.verbose(" hmSOCancellableLines size :: " + hmSOCancellableLines.size());

		updateShipmentContainersforLostScenario(env, inXML);

		cancelSalesOrder(env, hmSOCancellableLines);

		log.verbose("End of AcademySTSCancelOrderService.processLostContainerAndCanelSO() method");

	}

	/**
	 * This method is used to validate the SO Lines pending Cancellation by invoking
	 * a changeOrder API
	 * 
	 * @param env,
	 * @param inXML
	 */
	private void validateAndCancelTransferOrder(YFSEnvironment env, NodeList nlTOOrderLine, NodeList nlShipmentLine,
			String strCancelReasonCode, boolean isSOAlreadyCancelled, boolean isTOAlreadyCancelled) throws Exception {
		log.verbose("Begin of AcademySTSCancelOrderService.validateAndCancelTransferOrder() method");

		HashMap<String, String> hmTOCancellableLines = new HashMap<String, String>();
		HashMap<String, HashMap<String, String>> hmTOCancellableShipments = new HashMap<String, HashMap<String, String>>();
		HashMap<String, String> hmCancellableContianers = new HashMap<String, String>();
		HashMap<String, String> hmSOCancellableLines = new HashMap<String, String>();
		HashMap<String, Element> hmInventoryAdjustmentItems = new HashMap<String, Element>();
	
		for (int index = 0; index < nlTOOrderLine.getLength(); index++) {
			Element eleTransferOrderLine = (Element) nlTOOrderLine.item(index);

			String strChangeInOrderedQty = eleTransferOrderLine
					.getAttribute(AcademyConstants.ATTR_CHANGE_IN_ORDERED_QTY);
			if (strChangeInOrderedQty.startsWith(AcademyConstants.STR_HYPHEN)) {
				strChangeInOrderedQty = strChangeInOrderedQty.replace(AcademyConstants.STR_HYPHEN,
						AcademyConstants.STR_EMPTY_STRING);
			}
			String strMinLineStatus = eleTransferOrderLine.getAttribute(AcademyConstants.ATTR_MIN_LINE_STATUS);

			log.verbose(" strMinLineStatus : " + strMinLineStatus);

			// IF TO is not yet cancelled i.e TO Line contains some backrodered/unscheduled Qty
			if (!YFCObject.isVoid(strChangeInOrderedQty) && !YFCObject.isVoid(strMinLineStatus)
					&& Integer.parseInt(strMinLineStatus.substring(0, 4)) < 3350) {
				log.verbose(" Order contains TO lines and no TO Shipment available to cancel");

				Element eleChainedFromOrderLine = (Element) XPathUtil.getNode(eleTransferOrderLine,
						AcademyConstants.ELE_CHAINED_FROM_ORDER_LINE);

				//Fix for Partial Quantity Cancellation for Exeter
				String strOrderedQty = eleChainedFromOrderLine.getAttribute(AcademyConstants.ATTR_ORDERED_QTY);
				double dOrderedQty = Double.parseDouble(strOrderedQty);
				double dChangeInOrderQty = Double.parseDouble(strChangeInOrderedQty);
				String strFinalQty = Double.toString(dOrderedQty - dChangeInOrderQty);
				
				String strShipmentActualQty = eleTransferOrderLine.getAttribute("ShipmentActualQty");
				double dShipmentActualQty = Double.parseDouble(strShipmentActualQty);
				
				log.verbose("Shipment Actual Qty - " + dShipmentActualQty + " QuantityReduced - " + dChangeInOrderQty);
				if (dShipmentActualQty > 0 && dChangeInOrderQty > 0) {
					log.verbose("Partial Shortage - Shipment Actual Qty - " + dShipmentActualQty + " QuantityReduced - " + dChangeInOrderQty);
					eleChainedFromOrderLine.setAttribute("IsPartialCancellation", AcademyConstants.STR_YES);
				}
				
				if (!isTOAlreadyCancelled) {
					// Logic to cancel the TO Lines first and Then SO
					hmTOCancellableLines.put(AcademyConstants.ATTR_ORDER_HEADER_KEY,
							eleTransferOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));
					hmTOCancellableLines.put(eleTransferOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY),
							strFinalQty);
					//hmTOCancellableLines.put(eleTransferOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY),	strChangeInOrderedQty);
				}
			//Fix for Partial Quantity Cancellation for Exeter
				
				// Logic to Cancel SO
				if (!isSOAlreadyCancelled) {
					hmSOCancellableLines = validateSalesOrderLineEligibilityForCancel(env, hmSOCancellableLines,
							eleChainedFromOrderLine, strChangeInOrderedQty);
				}

			} else if (!YFCObject.isVoid(strChangeInOrderedQty) && !YFCObject.isVoid(strMinLineStatus)
					&& Integer.parseInt(strMinLineStatus.substring(0, 4)) > 3350
					&& Integer.parseInt(strMinLineStatus.substring(0, 4)) < 9000) {
				log.verbose(
						" Order contains TO lines which have been Shipped. Cannot Cancel Them. Update ExtnIsSOCancelled=Y");

				hmCancellableContianers = getCancelledContainerInfo(env, nlShipmentLine, hmCancellableContianers,
						eleTransferOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));

			} else if (!YFCObject.isVoid(strChangeInOrderedQty) && !YFCObject.isVoid(strMinLineStatus)
					&& Integer.parseInt(strMinLineStatus.substring(0, 4)) == 3350) {
				log.verbose(" Order contains TO lines which Released or Shipment Created");

				// Validate Shipment Status and cancel Containers if present.
				String strShipmentStatus = XPathUtil.getString(eleTransferOrderLine,
						"./ShipmentLines/ShipmentLine/Shipment/@Status");
				String strShipNode	= XPathUtil.getString(eleTransferOrderLine,
						"./ShipmentLines/ShipmentLine/Shipment/@ShipNode");
				
				log.verbose("Shipment Status :: " + strShipmentStatus);
				
				/* OMNI-52010 Start - Added conditions for STS2.0 Shipment statuses */

				if (!YFCObject.isVoid(strShipmentStatus)
						&& (strShipmentStatus.equals(AcademyConstants.STATUS_READY_FOR_BACKROOM_PICK_VAL)
								|| strShipmentStatus.equals(AcademyConstants.STATUS_BACKROOM_PICK_IN_PROGRESS_VAL)
								|| strShipmentStatus.equals(AcademyConstants.STATUS_READY_FOR_PACK_VAL)
								|| strShipmentStatus.equals(AcademyConstants.STATUS_SHIPMENT_BEING_PACKED))) {
					log.verbose("Shipment is in " + strShipmentStatus + " flow. Can cancel Shipment");

					hmTOCancellableShipments = getShipmentLinestoBeCancelled(env, nlShipmentLine,
							hmTOCancellableShipments, hmInventoryAdjustmentItems, 
							eleTransferOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY), strChangeInOrderedQty, strShipNode);

				}				
				/* OMNI-52010 End - Added conditions for STS2.0 Shipment statuses */
				else {
					log.verbose("Shipment is already packed. So not cancelling Shipment. ");
					hmCancellableContianers = getCancelledContainerInfo(env, nlShipmentLine, hmCancellableContianers,
							eleTransferOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
				}
				// Logic to Cancel SO
				if (!isSOAlreadyCancelled) {
					Element eleChainedFromOrderLine = (Element) XPathUtil.getNode(eleTransferOrderLine,
							AcademyConstants.ELE_CHAINED_FROM_ORDER_LINE);
					hmSOCancellableLines = validateSalesOrderLineEligibilityForCancel(env, hmSOCancellableLines,
							eleChainedFromOrderLine, strChangeInOrderedQty);
				}

			} else if (!YFCObject.isVoid(strChangeInOrderedQty) && !YFCObject.isVoid(strMinLineStatus)
					&& Integer.parseInt(strMinLineStatus.substring(0, 4)) >= 9000) {
				Element eleChainedFromOrderLine = (Element) XPathUtil.getNode(eleTransferOrderLine,
						AcademyConstants.ELE_CHAINED_FROM_ORDER_LINE);

				String strSOStatus = eleChainedFromOrderLine.getAttribute(AcademyConstants.ATTR_MIN_LINE_STATUS);

				log.verbose(" strSOStatus : " + strSOStatus);

				// Logic to Cancel SO
				if (!YFCObject.isVoid(strSOStatus) && Integer.parseInt(strSOStatus.substring(0, 4)) < 3700
						&& !isSOAlreadyCancelled) {
					hmSOCancellableLines = validateSalesOrderLineEligibilityForCancel(env, hmSOCancellableLines,
							eleChainedFromOrderLine, strChangeInOrderedQty);

				}

			} else {
				log.verbose(" TO Line " + eleTransferOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY)
						+ " is not in a valid status :: " + strMinLineStatus + "to cancel");
			}

		}
		log.verbose(" hmTOCancellableLines size :: " + hmTOCancellableLines.size());
		log.verbose(" hmSOCancellableLines size :: " + hmSOCancellableLines.size());
		log.verbose(" hmTONonCancellableLines size :: " + hmTOCancellableShipments.size());
		log.verbose(" hmTOContainersCancelled size :: " + hmCancellableContianers.size());

		if (hmCancellableContianers.size() > 0) {
			log.verbose("TOShipment Container Pending Update " + hmCancellableContianers.toString());
			updateShipmentContainersAsCancelled(env, hmCancellableContianers);
		}

		if (hmTOCancellableShipments.size() > 0) {
			log.verbose("TOShipment pending cancellation " + hmTOCancellableShipments.toString());
			cancelShipments(env, hmTOCancellableShipments, strCancelReasonCode);

			log.verbose(" hmInventoryAdjustmentItems size :: " + hmInventoryAdjustmentItems.size() );
			if (hmInventoryAdjustmentItems.size() > 0) {
				log.verbose("TOShipment Cancelled and Inventory Adjustment " + hmInventoryAdjustmentItems.toString());
				adjustInventoryForCancelledShipment(env, hmInventoryAdjustmentItems);
			}
		}
		
		if (hmTOCancellableLines.size() > 0) {
			log.verbose("TO Lines pending cancellation " + hmTOCancellableLines.toString());
			cancelOrder(env, hmTOCancellableLines, strCancelReasonCode, strCancelReasonCode);
		}
		//OMNI-90242  - START
		String strIsWCSInitiatedCancellation = null;
		if(!YFCObject.isVoid(env.getTxnObject(AcademyConstants.STR_IS_WCS_CANCELLATION)))
		{
			strIsWCSInitiatedCancellation = env.getTxnObject(AcademyConstants.STR_IS_WCS_CANCELLATION).toString();
		}

		if (hmSOCancellableLines.size() > 0 && !AcademyConstants.STR_YES.equals(strIsWCSInitiatedCancellation)) {
			log.verbose("SO Lines pending cancellation " + hmSOCancellableLines.toString());
			cancelOrder(env, hmSOCancellableLines, strCancelReasonCode, AcademyConstants.STR_TO_INITIATED_CANCEL);
		}
		//OMNI-90242  - END
		log.verbose("End of AcademySTSCancelOrderService.validateAndCancelTransferOrder() method");

	}

	/**
	 * This method is be used to validate is Sales ORder Line is eligible for Cancel
	 * 
	 * @param env,
	 * @param inXML
	 */
	private HashMap<String, String> validateSalesOrderLineEligibilityForCancel(YFSEnvironment env,
			HashMap<String, String> hmSOCancellableLines, Element eleSalesOrderLine, String strChangeInOrderedQty)
			throws Exception {
		log.verbose("Begin of AcademySTSCancelOrderService.validateSalesOrderLineEligibilityForCancel() method");
		
		//OMNI-70080 - STS Resourcing Start
		String isSTS2Short = (String) env.getTxnObject(AcademyConstants.IS_STS2_SHIPMENT);
		String isPartialSOCancel = eleSalesOrderLine.getAttribute("IsPartialCancellation");
		log.verbose(
				"isPartialSOCancel in AcademySTSCancelOrderService.validateSalesOrderLineEligibilityForCancel() method - "
						+ isPartialSOCancel);
		log.verbose(
				"IsSTS2ShipmentShort in AcademySTSCancelOrderService.validateSalesOrderLineEligibilityForCancel() method - "
						+ isSTS2Short);
		if ((!YFCObject.isVoid(isSTS2Short) && AcademyConstants.STR_YES.equals(isSTS2Short))
				&& (YFCObject.isVoid(isPartialSOCancel) || !AcademyConstants.STR_YES.equals(isPartialSOCancel))) {
			return hmSOCancellableLines;
		}
		//OMNI-70080 - STS Resourcing End
		
		String strOrderedQty = eleSalesOrderLine.getAttribute(AcademyConstants.ATTR_ORDERED_QTY);
		String strFinalQty = Double
				.toString(Double.parseDouble(strOrderedQty) - Double.parseDouble(strChangeInOrderedQty));

		if (!strFinalQty.startsWith("-")) {
			hmSOCancellableLines.put(AcademyConstants.ATTR_ORDER_HEADER_KEY,
					eleSalesOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));
			hmSOCancellableLines.put(eleSalesOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY), strFinalQty);
		} else {
			log.verbose(" Over cancellation of SO Is happenning. To be handled");
		}

		log.verbose("End of AcademySTSCancelOrderService.validateSalesOrderLineEligibilityForCancel() method");
		return hmSOCancellableLines;
	}

	/**
	 * This method is be used to cancel OrderLines by invoking a changeOrder API
	 * 
	 * @param env,
	 * @param inXML
	 */
	private void cancelOrder(YFSEnvironment env, HashMap<String, String> hmTOCancellableLines,
			String strCancelReasonCode, String strReferenceCode) throws Exception {
		log.verbose("Begin of AcademySTSCancelOrderService.cancelOrder() method");

		Document docChangeOrder = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		docChangeOrder.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,
				hmTOCancellableLines.get(AcademyConstants.ATTR_ORDER_HEADER_KEY));
		// docChangeOrder.getDocumentElement().setAttribute(AcademyConstants.ATTR_ACTION,
		// AcademyConstants.STR_CANCEL);
		docChangeOrder.getDocumentElement().setAttribute(AcademyConstants.ATTR_OVERRIDE, AcademyConstants.STR_YES);

		docChangeOrder.getDocumentElement().setAttribute(AcademyConstants.ATTR_MOD_REASON_CODE, strCancelReasonCode);
		docChangeOrder.getDocumentElement().setAttribute(AcademyConstants.ATTR_MODIFICATION_REFERENCE_1,
				strReferenceCode);

		Element eleOrderLines = docChangeOrder.createElement(AcademyConstants.ELE_ORDER_LINES);

		Iterator<Map.Entry<String, String>> iter = hmTOCancellableLines.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, String> entry = iter.next();

			if (!entry.getKey().equals(AcademyConstants.ATTR_ORDER_HEADER_KEY)) {
				Element eleOrderLine = docChangeOrder.createElement(AcademyConstants.ELE_ORDER_LINE);
				eleOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, entry.getKey());
				eleOrderLine.setAttribute(AcademyConstants.ATTR_ORDERED_QTY, entry.getValue());

				eleOrderLines.appendChild(eleOrderLine);
			}

		}

		docChangeOrder.getDocumentElement().appendChild(eleOrderLines);

		log.verbose("Final changeOrder input :: " + XMLUtil.getElementXMLString(docChangeOrder.getDocumentElement()));

		log.beginTimer("Cancel Order");
		AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER, docChangeOrder);
		log.endTimer("Cancel Order");

		log.verbose("End of AcademySTSCancelOrderService.cancelOrder() method");

	}

	/**
	 * This method is be used to update cancelled Containers with flag as Y
	 * 
	 * @param env,
	 * @param inXML
	 */
	private void updateShipmentContainersAsCancelled(YFSEnvironment env, HashMap<String, String> hmTOContainersCancelled)
	throws Exception {
		log.verbose("Begin of AcademySTSCancelOrderService.updateShipmentContainersAsCancelled() method");

		Document docMultiAPI = XMLUtil.createDocument(AcademyConstants.ELE_MULTIAPI);
		ArrayList<String> lShipmentList = new ArrayList<String>();

		Iterator<Map.Entry<String, String>> iter = hmTOContainersCancelled.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, String> entry = iter.next();

			if (lShipmentList.contains(entry.getValue())) {
				log.verbose(" Exisitng Shipment Key. Adding Containers ");
				Element eleContainers = (Element) XPathUtil.getNode(docMultiAPI,
						"/MultiApi/API/Input/Shipment[@ShipmentKey='" + entry.getValue() + "']/Containers");
				Element eleContainer = docMultiAPI.createElement(AcademyConstants.ELE_CONTAINER);
				eleContainer.setAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY, entry.getKey());

				String strIsReceived = retrieveIsReceivedFlag(env, entry.getValue(), entry.getKey());
				if (AcademyConstants.STR_YES.equals(strIsReceived)) {
					eleContainer.setAttribute(AcademyConstants.ATTR_IS_RECEIVED, AcademyConstants.STR_YES);
				}

				Element eleExtn = docMultiAPI.createElement(AcademyConstants.ELE_EXTN);
				eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_IS_SO_CANCELLED, AcademyConstants.STR_YES);

				eleContainer.appendChild(eleExtn);
				eleContainers.appendChild(eleContainer);
			} else {
				log.verbose(" New Shipment Key. Adding Shipment and Containers ");

				lShipmentList.add(entry.getValue());
				Element eleAPI = docMultiAPI.createElement(AcademyConstants.ELE_API);
				eleAPI.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.API_CHANGE_SHIPMENT);

				Element eleInput = docMultiAPI.createElement(AcademyConstants.ELE_INPUT);
				Element eleShipment = docMultiAPI.createElement(AcademyConstants.ELE_SHIPMENT);
				eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, entry.getValue());
				eleShipment.setAttribute(AcademyConstants.ATTR_OVERRIDE_MODIFICATION_RULES, AcademyConstants.STR_YES);

				Element eleContainers = docMultiAPI.createElement(AcademyConstants.ELE_CONTAINERS);

				Element eleContainer = docMultiAPI.createElement(AcademyConstants.ELE_CONTAINER);
				eleContainer.setAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY, entry.getKey());

				String strIsReceived = retrieveIsReceivedFlag(env, entry.getValue(), entry.getKey());
				if (AcademyConstants.STR_YES.equals(strIsReceived)) {
					eleContainer.setAttribute(AcademyConstants.ATTR_IS_RECEIVED, AcademyConstants.STR_YES);
				}

				Element eleExtn = docMultiAPI.createElement(AcademyConstants.ELE_EXTN);
				eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_IS_SO_CANCELLED, AcademyConstants.STR_YES);

				eleContainer.appendChild(eleExtn);
				eleContainers.appendChild(eleContainer);
				eleShipment.appendChild(eleContainers);
				eleInput.appendChild(eleShipment);
				eleAPI.appendChild(eleInput);
				docMultiAPI.getDocumentElement().appendChild(eleAPI);
			}

		}

		log.verbose("Final multiAPI input :: " + XMLUtil.getElementXMLString(docMultiAPI.getDocumentElement()));

		log.beginTimer("changeShipmentContainer for Cancelled Containers");
		AcademyUtil.invokeAPI(env, AcademyConstants.API_MULTI_API, docMultiAPI);
		log.endTimer("changeShipmentContainer for Cancelled Containers");

		log.verbose("End of AcademySTSCancelOrderService.updateShipmentContainersAsCancelled() method");

	}

	/**
	 * This method is be used to check if any of cancellable containers associated
	 * with active shipment line.
	 * 
	 * 
	 * @param env,
	 * @param inXML
	 */
	private HashMap<String, String> excludeActiveShipmentLineContainerInfo(YFSEnvironment env,
			HashMap<String, String> hmCancellableContianers, String strTOLineKey) throws Exception {

		Document docTOLineList = (Document) env.getTxnObject(AcademyConstants.STR_TO_LINE_LIST);

		if (!YFCObject.isVoid(docTOLineList)) {

			NodeList nlTransferShipmentLine = XPathUtil.getNodeList(docTOLineList,
					"/OrderLineList/OrderLine[not(@ChangeInOrderedQty)]/ShipmentLines/ShipmentLine"
							+ "[OrderLine/ChainedFromOrderLine/@MinLineStatus!='9000']");

			if (nlTransferShipmentLine.getLength() > 0) {
				log.verbose("Active ShipmentLines are present :: ");

				for (int iSL = 0; iSL < nlTransferShipmentLine.getLength(); iSL++) {

					Element eleShipmentLine = (Element) nlTransferShipmentLine.item(iSL);

					if (!eleShipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY).equals(strTOLineKey)) {
						log.verbose(" Removing added Contianers ::"
								+ eleShipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));

						NodeList nlContainerDetails = XPathUtil.getNodeList(eleShipmentLine,
								"./ContainerDetails/ContainerDetail");
						log.verbose(" :: nlContainerDetails.getLength :: " + nlContainerDetails.getLength());

						for (int iCD = 0; iCD < nlContainerDetails.getLength(); iCD++) {
							Element eleContainerDetails = (Element) nlContainerDetails.item(iCD);
							String strShipmentContainerKey = eleContainerDetails
									.getAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY);
							if (hmCancellableContianers.containsKey(strShipmentContainerKey)) {
								hmCancellableContianers.remove(strShipmentContainerKey);
							}

						}
					}
				}
			}
		}

		return hmCancellableContianers;
	}

	/**
	 * This method is be used to check IsReceived flag at container level and
	 * if the value is 'Y' then return the same so that it can be retained during 
	 * update on container.
	 * 
	 * 
	 * @param env,
	 * @param inXML
	 */
	private String retrieveIsReceivedFlag(YFSEnvironment env, String strShipmentKey, String strShipmentContainerKey)
	throws Exception {

		Document docTOLineList = (Document) env.getTxnObject(AcademyConstants.STR_TO_LINE_LIST);
		String strIsReceived = "";

		if (!YFCObject.isVoid(docTOLineList)) {

			Element eleContainer = (Element) XPathUtil.getNode(docTOLineList,
					"/OrderLineList/OrderLine/ShipmentLines/ShipmentLine/Shipment" + 
							"[@ShipmentKey='" + strShipmentKey + "']/Containers" + 
							"/Container[@ShipmentContainerKey='" + strShipmentContainerKey + "' and @IsReceived='Y']");
			if (!YFCObject.isVoid(eleContainer)) {
				strIsReceived = AcademyConstants.STR_YES;
			}

		}

		return strIsReceived;
	}

	/**
	 * This method is be used to check if the corresponding TO has any containers or
	 * not.
	 * 
	 * @param env,
	 * @param inXML
	 */
	private HashMap<String, String> getCancelledContainerInfo(YFSEnvironment env, NodeList nlShipmentLine,
			HashMap<String, String> hmCancellableContianers, String strTOLineKey) throws Exception {
		log.verbose("Begin of AcademySTSCancelOrderService.getCancelledContainerInfo() method");

		if (nlShipmentLine != null && nlShipmentLine.getLength() > 0) {

			for (int iSL = 0; iSL < nlShipmentLine.getLength(); iSL++) {
				Element eleShipmentLine = (Element) nlShipmentLine.item(iSL);

				if (eleShipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY).equals(strTOLineKey)) {
					log.verbose(" Adding Contianers for OrderLineKey ::"
							+ eleShipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));

					NodeList nlContainerDetails = XPathUtil.getNodeList(eleShipmentLine,
							"./ContainerDetails/ContainerDetail");
					log.verbose(" :: nlContainerDetails.getLength :: " + nlContainerDetails.getLength());

					for (int iCD = 0; iCD < nlContainerDetails.getLength(); iCD++) {
						Element eleContainerDetails = (Element) nlContainerDetails.item(iCD);
						hmCancellableContianers.put(
								eleContainerDetails.getAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY),
								eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));

					}
				}
			}
		}

		else {
			log.verbose(" Exception case which is to be handled ");
		}

		excludeActiveShipmentLineContainerInfo(env, hmCancellableContianers, strTOLineKey);

		log.verbose("End of AcademySTSCancelOrderService.getCancelledContainerInfo() method");
		return hmCancellableContianers;
	}

	/**
	 * This method is be used to fetch the TO Shipment lines eligible for Cancel.
	 * 
	 * @param env,
	 * @param inXML
	 */
	private HashMap<String, HashMap<String, String>> getShipmentLinestoBeCancelled(YFSEnvironment env,
			NodeList nlShipmentLine, HashMap<String, HashMap<String, String>> hmTOCancellableShipments,
			HashMap<String, Element>hmInventoryAdjustmentItems, String strTOLineKey, String strChangeInOrderedQty,
			String strShipNode) throws Exception {
		log.verbose("Begin of AcademySTSCancelOrderService.getShipmentLinestoBeCancelled() method");

		String strShipmentKey = "";

		if (nlShipmentLine != null && nlShipmentLine.getLength() > 0) {

			for (int iSL = 0; iSL < nlShipmentLine.getLength(); iSL++) {
				Element eleShipmentLine = (Element) nlShipmentLine.item(iSL);
				strShipmentKey = eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
				String strShipmentLineKey = eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);

				if (eleShipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY).equals(strTOLineKey)) {
					log.verbose(" Adding Contianers for OrderLineKey ::"
							+ eleShipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));

					String strShipmentLineActualQty = eleShipmentLine
							.getAttribute(AcademyConstants.ATTR_ACTUAL_QUANTITY);
					String strFinalQty = Double.toString(
							Double.parseDouble(strShipmentLineActualQty) - Double.parseDouble(strChangeInOrderedQty));

					hmInventoryAdjustmentItems.put(eleShipmentLine.getAttribute(AcademyConstants.ATTR_ITEM_ID), 
							prepareAdjustInventoryInput(eleShipmentLine, strChangeInOrderedQty, strShipNode));
					
					if (hmTOCancellableShipments.containsKey(strShipmentKey)) {
						HashMap hmShipmentLines = hmTOCancellableShipments.get(strShipmentKey);
						hmShipmentLines.put(strShipmentLineKey, strFinalQty);
						hmTOCancellableShipments.put(strShipmentKey, hmShipmentLines);
					} else {
						HashMap hmShipmentLines = new HashMap<String, String>();
						hmShipmentLines.put(strShipmentLineKey, strFinalQty);
						hmTOCancellableShipments.put(strShipmentKey, hmShipmentLines);
					}

				}
			}
		}

		else {
			log.verbose(" Exception case which is to be handled ");
		}

		if (!StringUtil.isEmpty(strShipmentKey)) {

			log.verbose("ShipmentKey :: " + strShipmentKey);

			hmTOCancellableShipments = getShipmentLinestoBeReset(env, hmTOCancellableShipments, strTOLineKey,
					strShipmentKey);

		}

		log.verbose("End of AcademySTSCancelOrderService.getShipmentLinestoBeCancelled() method");
		return hmTOCancellableShipments;
	}

	/**
	 * This method is be used to fetch the TO Shipment lines eligible for Reset.
	 * 
	 * @param env,
	 * @param inXML
	 */
	private HashMap<String, HashMap<String, String>> getShipmentLinestoBeReset(YFSEnvironment env,
			HashMap<String, HashMap<String, String>> hmTOCancellableShipments, String strTOLineKey,
			String strShipmentKey) throws Exception {
		log.verbose("Begin of AcademySTSCancelOrderService.getShipmentLinestoBeReset() method");

		log.verbose("ShipmentKey :: " + strShipmentKey);

		Document docTOLineList = (Document) env.getTxnObject(AcademyConstants.STR_TO_LINE_LIST);

		if (!YFCObject.isVoid(docTOLineList)) {

			NodeList nlTOShipmentLine = XMLUtil.getNodeList(docTOLineList,
					"/OrderLineList/OrderLine/ShipmentLines/ShipmentLine[Shipment/@ShipmentKey='" + strShipmentKey
							+ "']");

			for (int iSL = 0; iSL < nlTOShipmentLine.getLength(); iSL++) {

				Element eleShipmentLine = (Element) nlTOShipmentLine.item(iSL);
				String strShipmentLineKey = eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);

				if (!eleShipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY).equals(strTOLineKey)) {

					if (hmTOCancellableShipments.containsKey(strShipmentKey)) {
						HashMap hmShipmentLines = hmTOCancellableShipments.get(strShipmentKey);
						if(!hmShipmentLines.containsKey(strShipmentLineKey)) {
							hmShipmentLines.put(strShipmentLineKey, AcademyConstants.STR_ACTION_RESET);
							hmTOCancellableShipments.put(strShipmentKey, hmShipmentLines);
						}
						
					}
				}

			}
		}

		log.verbose("End of AcademySTSCancelOrderService.getShipmentLinestoBeReset() method");
		return hmTOCancellableShipments;
	}

	/**
	 * This method is be used to update cancelled Containers with flag as Y
	 * 
	 * @param env,
	 * @param inXML
	 */
	private void cancelShipments(YFSEnvironment env, HashMap<String, HashMap<String, String>> hmTOCancellableShipments, 
			String strReasonCode) throws Exception {
		log.verbose("Begin of AcademySTSCancelOrderService.cancelShipments() method");

		Document docMultiAPI = XMLUtil.createDocument(AcademyConstants.ELE_MULTIAPI);

		Iterator<Map.Entry<String, HashMap<String, String>>> iterShipment = hmTOCancellableShipments.entrySet()
				.iterator();

		while (iterShipment.hasNext()) {
			Map.Entry<String, HashMap<String, String>> entryShipment = iterShipment.next();

			Element eleAPI = docMultiAPI.createElement(AcademyConstants.ELE_API);
			eleAPI.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.API_CHANGE_SHIPMENT);

			Element eleInput = docMultiAPI.createElement(AcademyConstants.ELE_INPUT);
			Element eleShipment = docMultiAPI.createElement(AcademyConstants.ELE_SHIPMENT);
			eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, entryShipment.getKey());
			eleShipment.setAttribute(AcademyConstants.ATTR_CANCEL_REMOVED_QTY, AcademyConstants.STR_YES);
			eleShipment.setAttribute(AcademyConstants.ATTR_CANCEL_SHIPMENT_ON_ZERO_QTY, AcademyConstants.STR_YES);
			eleShipment.setAttribute(AcademyConstants.ATTR_OVERRIDE_MODIFICATION_RULES, AcademyConstants.STR_YES);

			Element eleExtn = docMultiAPI.createElement(AcademyConstants.ELE_EXTN);
			eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_SHORTPICK_REASON_CODE, strReasonCode);
			eleShipment.appendChild(eleExtn);

			Element eleContainers = docMultiAPI.createElement(AcademyConstants.ELE_CONTAINERS);
			eleContainers.setAttribute(AcademyConstants.ATTR_REPLACE, AcademyConstants.STR_YES);

			Element eleShipmentLines = docMultiAPI.createElement(AcademyConstants.ELE_SHIPMENT_LINES);

			HashMap<String, String> hmShipmentLines = entryShipment.getValue();
			Iterator<Map.Entry<String, String>> iterShipmentLines = hmShipmentLines.entrySet().iterator();

			while (iterShipmentLines.hasNext()) {
				Map.Entry<String, String> entryShipmentLine = iterShipmentLines.next();

				Element eleShipmentLine = docMultiAPI.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
				eleShipmentLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, entryShipmentLine.getKey());
				eleShipmentLine.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.STR_ACTION_MODIFY);
				eleShipmentLine.setAttribute(AcademyConstants.ATTR_BACKROOM_PICKED_QTY,
						AcademyConstants.STR_ZERO_IN_DECIMAL);

				if (!AcademyConstants.STR_ACTION_RESET.equals(entryShipmentLine.getValue())) {
					eleShipmentLine.setAttribute(AcademyConstants.ATTR_QUANTITY, entryShipmentLine.getValue());
				}

				eleShipmentLines.appendChild(eleShipmentLine);
			}

			eleShipment.appendChild(eleShipmentLines);
			eleShipment.appendChild(eleContainers);
			eleInput.appendChild(eleShipment);
			eleAPI.appendChild(eleInput);
			docMultiAPI.getDocumentElement().appendChild(eleAPI);
		}

		log.verbose("Final multiAPI input :: " + XMLUtil.getElementXMLString(docMultiAPI.getDocumentElement()));

		log.beginTimer("changeShipment for Cancelled Shipment");
		AcademyUtil.invokeAPI(env, AcademyConstants.API_MULTI_API, docMultiAPI);
		log.endTimer("changeShipment for Cancelled Shipment");

		log.verbose("End of AcademySTSCancelOrderService.cancelShipments() method");

	}


	/**
	 * This method is be used to fetch Transfer Order details
	 * 
	 * @param env,
	 * @param inXML
	 */
	private Document getTransferOrderDetails(YFSEnvironment envObj, String strSalesOHK) throws Exception {

		log.verbose("Begin of AcademySTSCancelOrderService.getTransferOrderDetails() method");

		Document docTransferOrderLineListInp = XMLUtil.createDocument(AcademyConstants.ELE_ORDER_LINE);
		Element eleTransferOrderLineListInp = docTransferOrderLineListInp.getDocumentElement();

		eleTransferOrderLineListInp.setAttribute(AcademyConstants.ATTR_CHAINED_FROM_ORDER_HEADER_KEY, strSalesOHK);

		log.verbose("Input to API - getCompleteOrderLineList :: " + XMLUtil.getXMLString(docTransferOrderLineListInp));
		envObj.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_LINE_LIST,
				AcademyConstants.STR_TEMPLATE_FILE_GET_COMPLETE_ORDER_LINE_LIST_STS_CANCEL);
		Document docTransferOrderLineListOut = AcademyUtil.invokeAPI(envObj,
				AcademyConstants.API_GET_COMPLETE_ORDER_LINE_LIST, docTransferOrderLineListInp);
		envObj.clearApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_LINE_LIST);
		log.verbose("Output of API - getCompleteOrderLineList :: " + XMLUtil.getXMLString(docTransferOrderLineListOut));

		log.verbose("End of AcademySTSCancelOrderService.getTransferOrderDetails() method");
		return docTransferOrderLineListOut;
	}

	
	
	/**
	 * This method is be used to update cancelled Containers with flag as Y and 
	 * also update the IsReceived=Y where applicable
	 * 
	 * @param env,
	 * @param inXML
	 */
	private void updateShipmentContainersforLostScenario(YFSEnvironment env, Document inXML) throws Exception {
		log.verbose("Begin of AcademySTSCancelOrderService.updateShipmentContainersforLostScenario() method");

		Element eleContainerInp = inXML.getDocumentElement();

		Document docMultiAPI = XMLUtil.createDocument(AcademyConstants.ELE_MULTIAPI);
		Element eleAPI = docMultiAPI.createElement(AcademyConstants.ELE_API);
		eleAPI.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.API_CHANGE_SHIPMENT);

		Element eleInput = docMultiAPI.createElement(AcademyConstants.ELE_INPUT);
		Element eleShipment = docMultiAPI.createElement(AcademyConstants.ELE_SHIPMENT);
		eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, eleContainerInp.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
		eleShipment.setAttribute(AcademyConstants.ATTR_OVERRIDE_MODIFICATION_RULES, AcademyConstants.STR_YES);

		Element eleContainers = docMultiAPI.createElement(AcademyConstants.ELE_CONTAINERS);

		Element eleContainer = docMultiAPI.createElement(AcademyConstants.ELE_CONTAINER);
		eleContainer.setAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY, eleContainerInp.getAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY));

		if(eleContainerInp.hasAttribute(AcademyConstants.ATTR_IS_RECEIVED)) {
			eleContainer.setAttribute(AcademyConstants.ATTR_IS_RECEIVED, eleContainerInp.getAttribute(AcademyConstants.ATTR_IS_RECEIVED));					
		}
		Element eleExtn = docMultiAPI.createElement(AcademyConstants.ELE_EXTN);
		eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_IS_SO_CANCELLED, AcademyConstants.STR_YES);

		eleContainer.appendChild(eleExtn);
		eleContainers.appendChild(eleContainer);
		eleShipment.appendChild(eleContainers);
		eleInput.appendChild(eleShipment);
		eleAPI.appendChild(eleInput);
		docMultiAPI.getDocumentElement().appendChild(eleAPI);


		log.verbose("Final multiAPI input :: " + XMLUtil.getElementXMLString(docMultiAPI.getDocumentElement()));

		log.beginTimer("changeShipmentContainer for Cancelled Containers");
		AcademyUtil.invokeAPI(env, AcademyConstants.API_MULTI_API, docMultiAPI);
		log.endTimer("changeShipmentContainer for Cancelled Containers");

		log.verbose("End of AcademySTSCancelOrderService.updateShipmentContainersforLostScenario() method");

	}


	/**
	 * This method is be used to update cancelled Containers with flag as Y and 
	 * also update the IsReceived=Y where applicable
	 * 
	 * @param env,
	 * @param inXML
	 */
	private void cancelSalesOrder(YFSEnvironment env, HashMap<String, String> hmSOCancellableLines) throws Exception {
		log.verbose("Begin of AcademySTSCancelOrderService.cancelSalesOrder() method");

		//Fetch Sales Order Details to see if Shipments are avaialble on SO or not.
		Document docGetShipmentListOut = getShipmentList(env, hmSOCancellableLines.get(AcademyConstants.ATTR_ORDER_HEADER_KEY));
		NodeList nlShipment = XPathUtil.getNodeList(docGetShipmentListOut, "/Shipments/Shipment");

		log.verbose(" nlShipment :: " + nlShipment.getLength());
		if(nlShipment.getLength() == 1) {
			log.verbose("Order has STS Shipment. So cancel Shipment");
			validateAndCancelShipments(env, hmSOCancellableLines, docGetShipmentListOut);
			
			if (hmSOCancellableLines.size() > 0) {
				log.verbose("SO Lines pending cancellation " + hmSOCancellableLines.toString());
				cancelOrder(env, hmSOCancellableLines, AcademyConstants.STR_CONTAINER_LOST,
						AcademyConstants.STR_TO_INITIATED_CANCEL);
			}
			
		}
		else if (nlShipment.getLength() == 0) {
			log.verbose("Order does not contain any STS Shipment. So cancel Order");

			if (hmSOCancellableLines.size() > 0) {
				log.verbose("SO Lines pending cancellation " + hmSOCancellableLines.toString());
				cancelOrder(env, hmSOCancellableLines, AcademyConstants.STR_CONTAINER_LOST,
						AcademyConstants.STR_TO_INITIATED_CANCEL);
			}
		}
		else {
			log.verbose("Invalid Scenario where order has more than 1 STS Shipment");
			//OMNI-30150
			log.verbose("SO Lines pending cancellation " + hmSOCancellableLines.toString());
			log.verbose("Order has STS Shipment. So cancel Shipment");
			validateAndCancelShipments(env, hmSOCancellableLines, docGetShipmentListOut);
			cancelOrder(env, hmSOCancellableLines, AcademyConstants.STR_CONTAINER_LOST,
					AcademyConstants.STR_TO_INITIATED_CANCEL);
			//OMNI-30150
		}

		log.verbose("End of AcademySTSCancelOrderService.updateShipmentContainersforLostScenario() method");

	}

	/**
	 * This method is be used to get Sales Order Shipment Details
	 * 
	 * @param env,
	 * @param inXML
	 */
	private Document getShipmentList(YFSEnvironment env, String strOrderHeaderKey) throws Exception {
		log.verbose("Begin of AcademySTSCancelOrderService.getShipmentList() method");

		Document docShipmentList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleShipment = docShipmentList.getDocumentElement();

		eleShipment.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
		eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, AcademyConstants.STR_SHIP_TO_STORE);

		log.verbose("Input to API - getShipmentList :: " + XMLUtil.getXMLString(docShipmentList));
		log.beginTimer("getShipmentList for Lost Containers");
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST,
				XMLUtil.getDocument("<Shipments> <Shipment> <ShipmentLines> <ShipmentLine /> </ShipmentLines></Shipment></Shipments>"));
		Document docGetShipmentListOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST, docShipmentList);
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
		log.endTimer("getShipmentList for Lost Containers");

		log.verbose("End of AcademySTSCancelOrderService.getShipmentList() method");

		return docGetShipmentListOut;
	}



	/**
	 * This method is be used to cancel Shipment and corresponding SO Lines
	 * 
	 * @param env,
	 * @param inXML
	 */
	private void validateAndCancelShipments(YFSEnvironment env, HashMap<String, String> hmSOCancellableLines, 
			Document docGetShipmentListOut) throws Exception {
		log.verbose("Begin of AcademySTSCancelOrderService.validateAndCancelShipments() method");
		Element eleShipmentOut = null;
		
		Iterator<Map.Entry<String, String>> iterCancelLines1 = hmSOCancellableLines.entrySet().iterator();
		while (iterCancelLines1.hasNext()) {
			Map.Entry<String, String> entryCancelLine1 = iterCancelLines1.next(); 
			log.verbose("Entrycancel line is " +  entryCancelLine1.getKey() );
			
			if (!entryCancelLine1.getKey().equals(AcademyConstants.ATTR_ORDER_HEADER_KEY)) {
				
				eleShipmentOut = (Element) XPathUtil.getNode(docGetShipmentListOut, 
					"//Shipments/Shipment[ShipmentLines/ShipmentLine[@OrderLineKey='" + entryCancelLine1.getKey() + "']]");
				
			}
		}

		String strShipmentStatus = XPathUtil.getString(eleShipmentOut, "@Status");
		log.verbose(" strShipmentStatus :: " + strShipmentStatus);
		if(!YFCObject.isVoid(strShipmentStatus) && Integer.parseInt(strShipmentStatus.substring(0, 4)) < 1400) {
			log.verbose(" Shipment is not yet Picked Up. Can be cancellable.");

			String strShipmentKey = XPathUtil.getString(eleShipmentOut, "@ShipmentKey");

			Document docChangeShipment = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			Element eleShipment = docChangeShipment.getDocumentElement();
			eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
			eleShipment.setAttribute(AcademyConstants.ATTR_BACK_ORD_REM_QTY, AcademyConstants.STR_YES);
			eleShipment.setAttribute(AcademyConstants.ATTR_CANCEL_SHIPMENT_ON_ZERO_QTY, AcademyConstants.STR_YES);
			eleShipment.setAttribute(AcademyConstants.ATTR_OVERRIDE_MODIFICATION_RULES, AcademyConstants.STR_YES);

			Element eleExtn = XmlUtils.createChild(eleShipment, AcademyConstants.ELE_EXTN);
			eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_SHORTPICK_REASON_CODE, AcademyConstants.STR_CONTAINER_LOST);

			Element eleShipmentLines = XmlUtils.createChild(eleShipment, AcademyConstants.ELE_SHIPMENT_LINES);

			Iterator<Map.Entry<String, String>> iterCancelLines = hmSOCancellableLines.entrySet().iterator();

			while (iterCancelLines.hasNext()) {
				Map.Entry<String, String> entryCancelLine = iterCancelLines.next();

				if (!entryCancelLine.getKey().equals(AcademyConstants.ATTR_ORDER_HEADER_KEY)) {
					String strShipmentLineKey = XPathUtil.getString(docGetShipmentListOut, 
							"/Shipments/Shipment/ShipmentLines/ShipmentLine[@OrderLineKey='" + entryCancelLine.getKey() + "']/@ShipmentLineKey");

					Element eleShipmentLine = XmlUtils.createChild(eleShipmentLines, AcademyConstants.ELE_SHIPMENT_LINE);
					eleShipmentLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, strShipmentLineKey);
					eleShipmentLine.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.STR_ACTION_MODIFY);
					eleShipmentLine.setAttribute(AcademyConstants.ATTR_BACKROOM_PICKED_QTY,	AcademyConstants.STR_ZERO_IN_DECIMAL);

					eleShipmentLine.setAttribute(AcademyConstants.ATTR_QUANTITY, entryCancelLine.getValue());
				}
			}
			
			log.verbose("Final changeShipment input :: " + XMLUtil.getElementXMLString(docChangeShipment.getDocumentElement()));

			log.beginTimer("changeShipment for SO Shipment");
			AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, docChangeShipment);
			log.endTimer("changeShipment for SO Shipment");
			
		} else {
			log.verbose(" Shipment is processed or already cancelled.");
		}
		log.verbose("End of AcademySTSCancelOrderService.validateAndCancelShipments() method");

	}

	/*
	 * This method is be used to prepare input for Adjust Inventory
	 * 
	 * @param env,
	 * @param inXML
	 */
	private Element prepareAdjustInventoryInput(Element eleShipmentLine, String strChangeInOrderedQty, String strShipNode) throws Exception {
		log.verbose("Begin of AcademySTSCancelOrderService.prepareAdjustInventoryInput() method");

		Element eleItem = XmlUtils.createDocument(AcademyConstants.ITEM).getDocumentElement();
		eleItem.setAttribute(AcademyConstants.ATTR_ADJUST_TYPE, AcademyConstants.STR_ADJUSTMENT);
		eleItem.setAttribute(AcademyConstants.ATTR_ITEM_ID, eleShipmentLine.getAttribute(AcademyConstants.ATTR_ITEM_ID));
		eleItem.setAttribute(AcademyConstants.ATTR_UOM, eleShipmentLine.getAttribute(AcademyConstants.ATTR_UOM));
		eleItem.setAttribute(AcademyConstants.ATTR_PROD_CLASS, eleShipmentLine.getAttribute(AcademyConstants.ATTR_PROD_CLASS));
		eleItem.setAttribute(AcademyConstants.ORGANIZATION_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
		eleItem.setAttribute(AcademyConstants.ATTR_QUANTITY, AcademyConstants.STR_HYPHEN + strChangeInOrderedQty);
		eleItem.setAttribute(AcademyConstants.ATTR_SHIP_NODE, strShipNode);
		eleItem.setAttribute(AcademyConstants.ATTR_SUPPLY_TYPE, AcademyConstants.STR_SUPP_TYPE_VAL);

		log.verbose("End of AcademySTSCancelOrderService.prepareAdjustInventoryInput() method");
		return eleItem;
	}
	
	
	/*
	 * This method is be used to remove Inventory for Cancelled Shipments
	 * 
	 * @param env,
	 * @param inXML
	 */
	private void adjustInventoryForCancelledShipment(YFSEnvironment env, HashMap<String, Element> hmInventoryAdjustmentItems ) throws Exception {
		log.verbose("Begin of AcademySTSCancelOrderService.adjustInventoryForCancelledShipment() method");

		Document docItems = XMLUtil.createDocument(AcademyConstants.ELE_ITEMS);
		Iterator<Map.Entry<String, Element>> iterItems = hmInventoryAdjustmentItems.entrySet().iterator();

		while (iterItems.hasNext()) {
			Map.Entry<String, Element> entryItems = iterItems.next();
			Element eleItem = XmlUtils.createChild(docItems.getDocumentElement(), AcademyConstants.ITEM);
			XMLUtil.copyElement(docItems, entryItems.getValue(), eleItem);

		}

		log.verbose("Final adjustInventory input :: " + XMLUtil.getElementXMLString(docItems.getDocumentElement()));

		log.beginTimer("adjustInventory for Cancelled Shipment");
		AcademyUtil.invokeAPI(env, "adjustInventory", docItems);
		log.endTimer("adjustInventory for Cancelled Shipment");

		log.verbose("End of AcademySTSCancelOrderService.adjustInventoryForCancelledShipment() method");

	}
	/**
	 * Start OMNI-30152 - STS Line Level Cancellation
	 * @param envObj
	 * @param docInputFromEnv
	 * @return
	 * @throws Exception
	 */
	private Document getTransferLineDetailsForWCSCancellation(YFSEnvironment envObj,
			Document docInputFromEnv) throws Exception {
		// TODO Auto-generated method stub
		
		log.verbose("Begin of AcademySTSCancelOrderService.getTransferLineDetailsForWCSCancellation() method");

		Document docTransferOrderLineListInp = XMLUtil.createDocument(AcademyConstants.ELE_ORDER_LINE);
		Element eleTransferOrderLineListInp = docTransferOrderLineListInp.getDocumentElement();
		
		String strOrderLineKey = XPathUtil.getString(docInputFromEnv, "/Order/OrderLines/OrderLine/@OrderLineKey");
		log.verbose("OrderLineKey is "+ strOrderLineKey);
		eleTransferOrderLineListInp.setAttribute(AcademyConstants.ATTR_CHAINED_FROM_ORDER_LINE_KEY, strOrderLineKey);

		log.verbose("Input to API - getCompleteOrderLineList :: " + XMLUtil.getXMLString(docTransferOrderLineListInp));
		envObj.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_LINE_LIST,
				AcademyConstants.STR_TEMPLATE_FILE_GET_COMPLETE_ORDER_LINE_LIST_STS_CANCEL);
		Document docTransferOrderLineListOut = AcademyUtil.invokeAPI(envObj,
				AcademyConstants.API_GET_COMPLETE_ORDER_LINE_LIST, docTransferOrderLineListInp);
		envObj.clearApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_LINE_LIST);
		log.verbose("Output of API - getCompleteOrderLineList :: " + XMLUtil.getXMLString(docTransferOrderLineListOut));

		log.verbose("End of AcademySTSCancelOrderService.getTransferLineDetailsForWCSCancellation() method");
		return docTransferOrderLineListOut;
	}
	/**End OMNI-30152 */
}
