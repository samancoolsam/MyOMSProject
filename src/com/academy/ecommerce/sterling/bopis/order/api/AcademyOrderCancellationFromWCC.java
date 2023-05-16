package com.academy.ecommerce.sterling.bopis.order.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/*##################################################################################
*
* Description : This code was added as part of OMNI-63312 to handle cancellation 
* of Orders from WCC
* 
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author         		Version#       Remarks/Description                      
* ---------------------------------------------------------------------------------
* 20-FEB-2022		Everest 			  1.0           	Initial version
* 
* ##################################################################################*/

/*
 * Sample Order level cancellation Input to the class
 * <Order DisplayLocalizedFieldInLocale="en_US_EST" IgnoreOrdering="Y"
    ModificationReasonCode="Changed my mind" OrderHeaderKey="202202141153355569793047">
    <OrderLines>
        <OrderLine OrderLineKey="202202141153355569793048"
            PrimeLineNo="1" QuantityToCancel="2"/>
    </OrderLines>
    <Notes>
        <Note NoteText="User requested to cancel because of 3 - Changed my mind"/>
    </Notes>
   </Order>
 * 
 * Sample Line Level Cancellation Input to the class
 * 
<Order DisplayLocalizedFieldInLocale="en_US_EST" IgnoreOrdering="Y" ModificationReasonCode="Customer Requested" OrderHeaderKey="202202220440385571249590">
   <OrderLines>
      <OrderLine OrderLineKey="202202220440385571249591" PrimeLineNo="1" QuantityToCancel="2" />
      <OrderLine OrderLineKey="202202220440385571249592" PrimeLineNo="2" QuantityToCancel="2" />
   </OrderLines>
   <Notes>
      <Note NoteText="USer requested to cancel because of 25 - Customer Requested Other Reason" />
   </Notes>
</Order>
 *
 * 
 */

public class AcademyOrderCancellationFromWCC {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyOrderCancellationFromWCC.class);
	private Properties props;

	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}

	/**
	 * This method is invoked to process all WCC cancellations.
	 * 
	 * @param inXML - It's a input XML
	 * @return docOutput - Final Output XML as response document
	 * @throws Exception
	 */
	public Document processCompleteOrderCancellation(YFSEnvironment env, Document inXML) throws Exception {
		log.beginTimer("AcademyOrderCancellationFromWCC::processCompleteOrderCancellation");
		log.verbose("Entering the method AcademyOrderCancellationFromWCC.processCompleteOrderCancellation ");

		log.verbose("Input to the class AcademyOrderCancellationFromWCC " + XMLUtil.getXMLString(inXML));
		Document docOutput = null;
		Document docChangeOrderIn = null;

		// Validate the input xmls
		if (!YFCObject.isVoid(inXML)) {
			String strOrderHeaderKey = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
			String strAction = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_ACTION);
			String strModReasonCode = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_MOD_REASON_CODE);

			// Retrieve shipments eligible for Cancellation
			Document docShipmentList = getShipmentListForOrder(env, strOrderHeaderKey);

			log.verbose("Does output have Shipments ::" + docShipmentList.hasChildNodes());

			if (!YFCObject.isVoid(docShipmentList) && docShipmentList.hasChildNodes()) {
				log.verbose(" :: Order has some shipments that can be cancelked :: ");
				validateAndCancelEligibleShipments(env, inXML, docShipmentList);

			}
			// Check if Order or Orderline level cancel
			if (strAction.equalsIgnoreCase(AcademyConstants.STR_CANCEL)) {

				docOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER, inXML);
				log.endTimer("changeOrder for cancelling Complete Order");

			} else {
				// Prepare changeOrder API Input
				log.verbose("Invoke changeOrder API to cancel Orderlines::");
				log.beginTimer("changeOrder for cancelling orderlines");
				docChangeOrderIn = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
				Element eleOrder = docChangeOrderIn.getDocumentElement();
				eleOrder.setAttribute(AcademyConstants.ATTR_OVERRIDE, AcademyConstants.STR_YES);
				eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
				eleOrder.setAttribute(AcademyConstants.ATTR_MOD_REASON_CODE, strModReasonCode);

				Element eleChangeOrderOrderLines = docChangeOrderIn.createElement(AcademyConstants.ELE_ORDER_LINES);

				NodeList nlOrderLine = XMLUtil.getNodeListByXPath(inXML, "Order/OrderLines/OrderLine");

				Element eleOrderLineIn = null;
				// Prepare Orderline input for changeOrder
				for (int iOL = 0; iOL < nlOrderLine.getLength(); iOL++) {
					eleOrderLineIn = (Element) nlOrderLine.item(iOL);
					Element eleOrderLineOut = docChangeOrderIn.createElement("OrderLine");
					eleOrderLineOut.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY,
							eleOrderLineIn.getAttribute("OrderLineKey"));
					eleOrderLineOut.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.STR_CANCEL);

					// Append OrderLine to changeOrder Input
					eleChangeOrderOrderLines.appendChild(eleOrderLineOut);
				}

				eleOrder.appendChild(eleChangeOrderOrderLines);

				// Append Order Notes
				NodeList nlNotes = XMLUtil.getNodeListByXPath(inXML, "Order/Notes/Note");
				Element eleChangeOrderNotes = docChangeOrderIn.createElement(AcademyConstants.ELE_NOTES);
				Element eleNoteIn = null;
				for (int iNotes = 0; iNotes < nlNotes.getLength(); iNotes++) {
					eleNoteIn = (Element) nlNotes.item(iNotes);
					Element eleChangeOrderNote = docChangeOrderIn.createElement(AcademyConstants.ELE_NOTE);
					eleChangeOrderNote.setAttribute(AcademyConstants.ATTR_NOTE_TEXT,
							eleNoteIn.getAttribute(AcademyConstants.ATTR_NOTE_TEXT));
					eleChangeOrderNotes.appendChild(eleChangeOrderNote);
					eleOrder.appendChild(eleChangeOrderNotes);
				}

				log.verbose("changeOrder for cancelling Orderlines");
				// Invoke changeOrder API
				// Setting the docOutput: OMNI-65893 - Start
				docOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER, docChangeOrderIn);
				// OMNI-65893- End
			}
		}
		log.endTimer("AcademyOrderCancellationFromWCC::processCompleteOrderCancellation");
		return docOutput;
	}

	/**
	 * This method is invoked to retrieve all the Shipments present for given Order
	 * 
	 * @param inXML - It's a input XML
	 * @return docOutput - Final Output XML as response document
	 * @throws Exception
	 */
	private Document getShipmentListForOrder(YFSEnvironment env, String strOrderHeaderKey) throws Exception {
		log.beginTimer("AcademyOrderCancellationFromWCC::getShipmentListForOrder");
		log.verbose(" Invoking getShipmentListForORder API");
		Document docGetOrderListOut = null;

		Document docGetShipmentListForOrderInp = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		docGetShipmentListForOrderInp.getDocumentElement().setAttribute(AcademyConstants.STR_ORDR_HDR_KEY,
				strOrderHeaderKey);
		docGetOrderListOut = AcademyUtil.invokeAPI(env, "getShipmentListForOrder", docGetShipmentListForOrderInp);
		log.verbose("End of getOrderList() method");

		log.endTimer("AcademyOrderCancellationFromWCC::getShipmentListForOrder");
		return docGetOrderListOut;
	}

	/**
	 * This method is invoked to check and provide list of Shipment and Shipment
	 * lines which need to be cancelled and invoke a changeShipment to cancel the
	 * same
	 * 
	 * @param inXML - It's a input XML
	 * @return docOutput - Final Output XML as response document
	 * @throws Exception
	 */
	private void validateAndCancelEligibleShipments(YFSEnvironment env, Document inXML, Document docShipmentList)
			throws Exception {
		log.beginTimer("AcademyOrderCancellationFromWCC::validateAndCancelEligibleShipments");
		log.verbose(" Invoking getShipmentListForORder API");

		boolean bIsOrderCancel = false;
		String strOrderAction = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_ACTION);
		List<String> lOrderLinesToBeCancelled = new ArrayList<String>();
		HashMap<String, HashMap<String, String>> hmShipmentToBeCancelled = new HashMap<String, HashMap<String, String>>();

		// Check if the complete order is being cancelled.
		if (!YFCObject.isNull(strOrderAction) && strOrderAction.equalsIgnoreCase(AcademyConstants.STR_CANCEL)) {
			bIsOrderCancel = true;
		}

		log.verbose(" :: bIsOrderCancel :: " + bIsOrderCancel);

		if (!bIsOrderCancel) {
			NodeList nlOrderLine = inXML.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
			log.verbose("::OrderLines to be cancelled :: " + nlOrderLine.getLength());
			for (int iOL = 0; iOL < nlOrderLine.getLength(); iOL++) {

				Element eleOrderLine = (Element) nlOrderLine.item(iOL);
				lOrderLinesToBeCancelled.add(eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
			}

			log.verbose("::OrderLines to be cancelled :: " + lOrderLinesToBeCancelled.size());

		}

		NodeList nlShipmentLines = docShipmentList.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		log.verbose(":: nlShipmentLines :: " + nlShipmentLines.getLength());

		// Loop through each of the Shipment
		for (int iShipmentLine = 0; iShipmentLine < nlShipmentLines.getLength(); iShipmentLine++) {

			Element eleShipmentLine = (Element) nlShipmentLines.item(iShipmentLine);

			String strOrderLineKey = eleShipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
			if (bIsOrderCancel
					|| (!YFCObject.isVoid(strOrderLineKey) && lOrderLinesToBeCancelled.contains(strOrderLineKey))) {
				log.verbose(":: Shipment Line is eligible to be cancelled :: ");

				String strQuantity = eleShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY);
				if (!YFCObject.isVoid(strQuantity) && Double.parseDouble(strQuantity) > 0) {
					log.verbose(":: Shipment Line has quantity to be canclled :: ");

					// TODO : Added holder to handle quantity level cancellations later

					// Update the shipment details in the hashmap to be eligible for cancel
					String strShipmentKey = eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
					if (hmShipmentToBeCancelled.containsKey(strShipmentKey)) {
						HashMap<String, String> hmShipmentLinesToCancel = hmShipmentToBeCancelled.get(strShipmentKey);
						hmShipmentLinesToCancel
								.put(eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY), "0");
						hmShipmentToBeCancelled.put(strShipmentKey, hmShipmentLinesToCancel);
					} else {
						HashMap<String, String> hmShipmentLinesToCancel = new HashMap<String, String>();
						;
						hmShipmentLinesToCancel
								.put(eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY), "0");
						hmShipmentToBeCancelled.put(strShipmentKey, hmShipmentLinesToCancel);
					}

				}
			}

		}

		log.verbose(":: Shipment eligible to be cancelled :: " + hmShipmentToBeCancelled.size());

		if (hmShipmentToBeCancelled.size() > 0) {
			invokeShipmentCancellation(env, hmShipmentToBeCancelled);
		}

	}

	/**
	 * This method is invoked to cancel shipments eligible to cancel
	 * 
	 * @param inXML - It's a input XML
	 * @return docOutput - Final Output XML as response document
	 * @throws Exception
	 */
	private void invokeShipmentCancellation(YFSEnvironment env,
			HashMap<String, HashMap<String, String>> hmShipmentToBeCancelled) throws Exception {
		log.beginTimer("AcademyOrderCancellationFromWCC::invokeShipmentCancellation");
		log.verbose("Begin of invokeShipmentCancellation() method");

		Document docMultiAPI = XMLUtil.createDocument(AcademyConstants.ELE_MULTIAPI);

		Iterator<Entry<String, HashMap<String, String>>> iterShipment = hmShipmentToBeCancelled.entrySet().iterator();
		while (iterShipment.hasNext()) {
			Entry<String, HashMap<String, String>> entryShipment = iterShipment.next();

			Element eleAPI = docMultiAPI.createElement(AcademyConstants.ELE_API);
			eleAPI.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.API_CHANGE_SHIPMENT);

			// Prepare input for MultiPAI for multiple shipments
			Element eleInput = docMultiAPI.createElement(AcademyConstants.ELE_INPUT);
			Element eleShipment = docMultiAPI.createElement(AcademyConstants.ELE_SHIPMENT);
			eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, entryShipment.getKey());
			eleShipment.setAttribute(AcademyConstants.ATTR_OVERRIDE_MODIFICATION_RULES, AcademyConstants.STR_YES);
			eleShipment.setAttribute(AcademyConstants.ATTR_BACK_ORD_REM_QTY, AcademyConstants.STR_YES);
			eleShipment.setAttribute(AcademyConstants.ATTR_CANCEL_SHIPMENT_ON_ZERO_QTY, AcademyConstants.STR_YES);

			Element eleShipmentLines = docMultiAPI.createElement(AcademyConstants.ELE_SHIPMENT_LINES);
			HashMap<String, String> hmShipmentLineToCancel = hmShipmentToBeCancelled.get(entryShipment.getKey());

			// Iterate through shipment lines and add them to the input
			Iterator<Entry<String, String>> iterShipmentLine = hmShipmentLineToCancel.entrySet().iterator();
			while (iterShipmentLine.hasNext()) {
				Entry<String, String> entryShipmentLine = iterShipmentLine.next();

				Element eleShipmentLine = docMultiAPI.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
				eleShipmentLine.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.VAL_CANCEL);
				eleShipmentLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, entryShipmentLine.getKey());

				eleShipmentLines.appendChild(eleShipmentLine);

			}
			// Append all nodes to the multi api input
			eleShipment.appendChild(eleShipmentLines);
			eleInput.appendChild(eleShipment);
			eleAPI.appendChild(eleInput);
			docMultiAPI.getDocumentElement().appendChild(eleAPI);

		}

		log.verbose("Final multiAPI input :: " + XMLUtil.getElementXMLString(docMultiAPI.getDocumentElement()));

		log.beginTimer("changeShipment for cancelling pending Shipments");
		AcademyUtil.invokeAPI(env, AcademyConstants.API_MULTI_API, docMultiAPI);
		log.endTimer("changeShipment for cancelling pending Shipments");

		log.verbose("End of invokeShipmentCancellation() method");
		log.endTimer("AcademyOrderCancellationFromWCC::invokeShipmentCancellation");
	}

}
