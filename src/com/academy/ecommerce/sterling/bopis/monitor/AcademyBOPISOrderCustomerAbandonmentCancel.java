package com.academy.ecommerce.sterling.bopis.monitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**#########################################################################################
*
* Project Name                : POD - Fulfillment
* Author                      : Everest
* Author Group				  : Fulfillment
* Date                        : 17-AUG-2022 
* Description				  : This class cancels all the shipments which have been in RFCP 
* 								for a order once the max customer pick date has crossed 
* 								 
* ---------------------------------------------------------------------------------
* Date            	Author         			Version#       		Remarks/Description                      
* ---------------------------------------------------------------------------------
* 17-Aug-2022		Everest  	 			  1.0           	Initial version
*
* #########################################################################################*/


public class AcademyBOPISOrderCustomerAbandonmentCancel implements YIFCustomApi {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyBOPISOrderCustomerAbandonmentCancel.class);

	
	HashMap<String, HashMap<String, String>> hmShipmentToBeCancelled = new HashMap<>();
	 HashMap<String, String> hmOrderLineToCancel = new  HashMap<>();

	public void setProperties(Properties props) throws Exception {
		log.verbose("setProperties :: ");
	}
	
	
	
	/**
	 * This method is invoked from the Service to validate and process Shipment cancellation
	 * and along with that the order cancellation
	 * 
	 * @param env
	 * @return shipmentConfDoc
	 * @throws Exception
	 */
	
	public Document processOrderCancellation (YFSEnvironment env, Document inDoc) throws Exception {		
		log.beginTimer("AcademyBOPISOrderCustomerAbandonmentCancel.processOrderCancellation() method:: ");
		log.verbose("Entering AcademyBOPISOrderCustomerAbandonmentCancel.processOrderCancellation() :: "+XMLUtil.getXMLString(inDoc));
		
		//Prepare a changeShipment API input to cancel the shipment
		Document docChangeShipmentMultiAPIInp = prepareInputForCancelShipment(inDoc);
		
		if(!YFCObject.isVoid(docChangeShipmentMultiAPIInp)) {
			log.beginTimer("changeShipment for cancelling pending Shipments");
			AcademyUtil.invokeAPI(env, AcademyConstants.API_MULTI_API, docChangeShipmentMultiAPIInp);
			log.endTimer("changeShipment for cancelling pending Shipments");
			
			//Once Shipment is cancelled, invoke API to cancel Order
			Element eleShipment = (Element) inDoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE).item(0);
			String strOrderHeaderKey = eleShipment.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
			
			log.verbose(":: strOrderHeaderKey :: "+strOrderHeaderKey);
			prepareAndInvokeForCancelOrder(env, strOrderHeaderKey);
		}
		
		log.endTimer("AcademyBOPISOrderCustomerAbandonmentCancel.processOrderCancellation() method:: ");
		return inDoc;		
	}
	
	
	/**
	 * This method is invoked to prepare the API input to cancel Shipment
	 * 
	 * @param env
	 * @return shipmentConfDoc
	 * @throws Exception
	 */

	private Document prepareInputForCancelShipment(Document inDoc) throws Exception {
		log.beginTimer("AcademyBOPISOrderCustomerAbandonmentCancel.prepareInputForCancelShipment() method:: ");
		
		Document docChangeShipmentMultiAPIInp = null;
		
		//Loop through the doc for each shipment line, and prepare API inp to cancel Shipment
		NodeList nlShipmentLine = inDoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		for (int i=0;i < nlShipmentLine.getLength(); i++)
		{
			Element eleShipmentLine = (Element) nlShipmentLine.item(i);
			String strShipmentKey = eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			String strShipmentLineKey = eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);
			String strQuantity = eleShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY);
			String strOrderLineKey = XPathUtil.getString(eleShipmentLine, "./OrderLine/@OrderLineKey");
			String strOrderLineQuantity = XPathUtil.getString(eleShipmentLine, "./OrderLine/@OrderedQty");
			
			Double dQuantity =  Double.parseDouble(strQuantity);
			Double dOLQty = Double.parseDouble(strOrderLineQuantity);
			Double dCalculateQty = dOLQty - dQuantity;
			if (dQuantity>0.00){
				log.verbose("Shipment line is eligible for cancel. :: "+ strShipmentKey + " :: "+strShipmentLineKey + " :: "+strQuantity);
				
				//Update the details in a hashmap
				if(hmShipmentToBeCancelled.containsKey(strShipmentKey)) {
					HashMap<String, String> hmShipmentLinesToCancel = hmShipmentToBeCancelled.get(strShipmentKey);
					hmShipmentLinesToCancel.put(strShipmentLineKey, strQuantity);
					hmShipmentToBeCancelled.put(strShipmentKey, hmShipmentLinesToCancel);
					hmOrderLineToCancel.put(strOrderLineKey, Double.toString(dCalculateQty));
				}
				else {
					HashMap<String, String> hmShipmentLinesToCancel = new HashMap<>();
					hmShipmentLinesToCancel.put(strShipmentLineKey, strQuantity);
					hmShipmentToBeCancelled.put(strShipmentKey, hmShipmentLinesToCancel);	
					hmOrderLineToCancel.put(strOrderLineKey, Double.toString(dCalculateQty));
				}
			}
		}
		
		//Check if shipments are eligible to cancel and prepare API input
		log.verbose("hmShipmentToBeCancelled. :: "+ hmShipmentToBeCancelled.size() + " :: "+hmShipmentToBeCancelled.toString() );
		if (hmShipmentToBeCancelled.size() > 0) {
			docChangeShipmentMultiAPIInp = addShipmentToMultiApiInput(hmShipmentToBeCancelled);
		}
		
		
		log.endTimer("AcademyBOPISOrderCustomerAbandonmentCancel.prepareInputForCancelShipment() method:: ");
		return docChangeShipmentMultiAPIInp;
	}
	
	
	/**
	 * This method is invoked to prepare a multi API input with changeShipment API
	 * 
	 * @param hmShipmentToBeCancelled -
	 * @return docOutput - Final Output XML as response document
	 * @throws Exception
	 */
	private Document addShipmentToMultiApiInput(HashMap<String, HashMap<String, String>> hmShipmentToBeCancelled) {
		log.beginTimer("AcademyBOPISOrderCustomerAbandonmentCancel.addShipmentToMultiApiInput() method:: ");
		log.verbose("Begin of addShipmentToMultiApiInput() method");
		Document docMultiAPI = null;
		
		try {
		docMultiAPI = XMLUtil.createDocument(AcademyConstants.ELE_MULTIAPI);

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

				eleShipmentLine.setAttribute(AcademyConstants.ATTR_QUANTITY, AcademyConstants.STR_ZERO);
				eleShipmentLine.setAttribute(AcademyConstants.ATTR_SHORTAGE_QTY,entryShipmentLine.getValue());
				
				Element eleExtn= docMultiAPI.createElement(AcademyConstants.ELE_EXTN);
				eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_MSG_TO_SIM, AcademyConstants.STR_YES);
				eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_REASON_CODE,AcademyConstants.STR_CUSTOMER_ABANDONED);

				eleShipmentLine.appendChild(eleExtn);
				eleShipmentLines.appendChild(eleShipmentLine);

			}
			// Append all nodes to the multi api input
			eleShipment.appendChild(eleShipmentLines);
			eleInput.appendChild(eleShipment);
			eleAPI.appendChild(eleInput);
			docMultiAPI.getDocumentElement().appendChild(eleAPI);

		}
		} catch (Exception e) {
			e.printStackTrace();
			log.verbose("Exception in AcademyBOPISOrderCustomerAbandonmentCancel.addShipmentToMultiApiInput ");
		}

		log.verbose("Final multiAPI input :: " + XMLUtil.getXMLString(docMultiAPI));
		log.endTimer("AcademyBOPISOrderCustomerAbandonmentCancel.addShipmentToMultiApiInput() method:: ");
		return docMultiAPI;
	}

	
	/**
	 * This method is invoked to prepare and invoke the API input to cancel Order
	 * 
	 * @param env
	 * @return shipmentConfDoc
	 * @throws Exception
	 */

	private void prepareAndInvokeForCancelOrder(YFSEnvironment env, String strOrderHeaderKey) throws Exception {
		log.beginTimer("AcademyBOPISOrderCustomerAbandonmentCancel.prepareInputForCancelOrder() method:: ");
		
		Document docChangeOrder = null;
		
		if(hmOrderLineToCancel.size() > 0) {
			//PReparing ChangeOrder Input
			docChangeOrder = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			Element eleOrder = docChangeOrder.getDocumentElement();
			eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
			eleOrder.setAttribute(AcademyConstants.ATTR_MOD_REASON_CODE, AcademyConstants.STR_CUSTOMER_ABANDONED);
			eleOrder.setAttribute(AcademyConstants.ATTR_OVERRIDE,AcademyConstants.STR_YES);

			Element eleOrderLines = docChangeOrder.createElement(AcademyConstants.ELE_ORDER_LINES);
			
			//Adding the lines to be cancelled
			Iterator<Entry<String, String>> iterOrderLine = hmOrderLineToCancel.entrySet().iterator();
			while (iterOrderLine.hasNext()) {
				
				Entry<String, String> entryOrderLine = iterOrderLine.next();
		
				Element eleOrderLine = docChangeOrder.createElement(AcademyConstants.ELE_ORDER_LINE);
				log.verbose("Short Order Line Key - " + entryOrderLine.getKey());
				
				eleOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, entryOrderLine.getKey());
				eleOrderLine.setAttribute(AcademyConstants.ATTR_ORDERED_QTY, entryOrderLine.getValue());
				
				//Adding Orderline notes
				Element eleNotes = docChangeOrder.createElement(AcademyConstants.ELE_NOTES);
				Element eleNote = docChangeOrder.createElement(AcademyConstants.ELE_NOTE);
				eleNote.setAttribute(AcademyConstants.ATTR_OPERATION, AcademyConstants.STR_OPERATION_VAL_CREATE);
				eleNote.setAttribute(AcademyConstants.ATTR_NOTE_TEXT, AcademyConstants.STR_CUSTOMER_ABANDONED);
				eleNote.setAttribute(AcademyConstants.ATTR_REASON_CODE, AcademyConstants.STR_CUSTOMER_ABANDONED);

				eleNotes.appendChild(eleNote);
				eleOrderLine.appendChild(eleNotes);
				eleOrderLines.appendChild(eleOrderLine);
			}
			eleOrder.appendChild(eleOrderLines);
		}
		
		//Check if Order can be cancelled 
		if (!YFCObject.isVoid(docChangeOrder)) {
			log.verbose("Final changeOrder input :: " + XMLUtil.getXMLString(docChangeOrder));
			log.beginTimer("changeOrder for cancelling pending OrderLines");
			AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER, docChangeOrder);
			log.endTimer("changeOrder for cancelling pending OrderLines");
		}
		
		
		log.endTimer("AcademyBOPISOrderCustomerAbandonmentCancel.prepareInputForCancelOrder() method:: ");
	}
}