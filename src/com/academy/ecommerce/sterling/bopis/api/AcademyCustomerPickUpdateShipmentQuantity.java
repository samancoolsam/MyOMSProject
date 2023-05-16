package com.academy.ecommerce.sterling.bopis.api;
/**#########################################################################################
 *
 * Project Name                : Fulfillment POD
 * Author                      : Everest
 * Author Group				  : Fulfillment-POD
 * Date                        : 14-Sep-2022
 * Description				  : This class retrieves multiple ShipmentLines and updated 
 * 								CustomerPickedQty and ShortageQty on Screen load.
 * 								 
 * ---------------------------------------------------------------------------------
 * Date            Author         		Version#       		Remarks/Description                      
 * ---------------------------------------------------------------------------------
 * 14-Sep-2022		Everest	 			  1.0           	Updated version
 *
 * #########################################################################################*/

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyCustomerPickUpdateShipmentQuantity {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyCustomerPickUpdateShipmentQuantity.class);

	/** This method updates Customer picked qty for multiple lines.
	 * @param env
	 * @param inDoc
	 * @return
	 */
	public Document updateCustomerPickedQuantity(YFSEnvironment env, Document inDoc) {

		log.beginTimer("AcademyCustomerPickUpdateShipmentQuantity::updateCustomerPickedQuantity");
		log.verbose("Entering AcademyCustomerPickUpdateShipmentQuantity.updateCustomerPickedQuantity() with Input::\n "
				+ XMLUtil.getXMLString(inDoc));

		HashMap<String, Document> hmShipmentToUpdate = new HashMap <>();

		try {
			NodeList nlShipmentLine = inDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
			log.verbose(" :: ShipmentLinesOnOrder :: "+nlShipmentLine.getLength());

			//looping throught the shipment lines to get all the Shipment lines
			for (int iSl=0; iSl < nlShipmentLine.getLength() ;iSl++) {
				Element eleShipmentLine = (Element) nlShipmentLine.item(iSl);
				hmShipmentToUpdate = prepareChangeShipmentInput(hmShipmentToUpdate, eleShipmentLine);
			}
			
			//prepare multiAPI and invoke changeShipment
			prepareAndInvokeChangeShipment(env, hmShipmentToUpdate);

		} catch (Exception e) {
			log.verbose("Exception at AcademyCustomerPickUpdateShipmentQuantity.updateCustomerPickedQuantity");

		}
		return inDoc;

	}

	/**
	 * This method validates the ShipmentsLine and prepares the changeShipment Input accordingly.
	 *
	 * @param hmShipmentToUpdate
	 * @param eleShipmentLine
	 * @return hmShipmentToUpdate
	 */
	private HashMap <String,Document> prepareChangeShipmentInput(HashMap <String,Document> hmShipmentToUpdate, Element eleShipmentLine) {
		log.beginTimer("AcademyCustomerPickUpdateShipmentQuantity::prepareChangeShipmentInput");
		Document docShipment = null;
		Element eleShipmentLines = null;

		try {
			
			//Checking if a shipmentKey is already present. If true add to exising Shipment key in map
			String strShipmentKey = eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			if(hmShipmentToUpdate.containsKey(strShipmentKey)) {
				log.verbose(" Document present. Updating the Shipment doc");
				docShipment = hmShipmentToUpdate.get(strShipmentKey);
			}
			else {
				docShipment = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
				docShipment.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
			}

			//Preparing input for Shipmentlines
			NodeList nlShipmentLines = docShipment.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINES);
			if(nlShipmentLines.getLength() == 0 ) {
				eleShipmentLines = SCXmlUtil.createChild(docShipment.getDocumentElement(), AcademyConstants.ELE_SHIPMENT_LINES);
				docShipment.getDocumentElement().appendChild(eleShipmentLines);
			}
			else {
				eleShipmentLines = (Element) nlShipmentLines.item(0);
			}

			Element eleAppendedShipmentLine = SCXmlUtil.createChild(docShipment.getDocumentElement(), AcademyConstants.ELE_SHIPMENT_LINE);
			XMLUtil.copyElement(docShipment, eleShipmentLine, eleAppendedShipmentLine);
			eleShipmentLines.appendChild(eleAppendedShipmentLine);

			hmShipmentToUpdate.put(strShipmentKey, docShipment);
			log.verbose(":: Updated Doc :: " + XMLUtil.getXMLString(docShipment));

		} catch (Exception e) {
			e.printStackTrace(); 
			log.verbose("Exception at AcademyCustomerPickUpdateShipmentQuantity.validateShipmentEligibilityForUpdate");
		}		
		log.endTimer("AcademyCustomerPickUpdateShipmentQuantity::prepareChangeShipmentInput");
		return hmShipmentToUpdate;
	}
	
	
	
	/**
	 * This method invokes a multiAPI to update the shipment details.
	 *
	 * @param hmShipmentToUpdate
	 * @param eleShipmentLine
	 * @return 
	 * @return hmShipmentToUpdate
	 */
	private void prepareAndInvokeChangeShipment(YFSEnvironment env, HashMap <String,Document> hmShipmentToUpdate) {
		log.beginTimer("AcademyCustomerPickUpdateShipmentQuantity::prepareAndInvokeChangeShipment");
		try {

			Document docChangeShipmentMultiAPI = XMLUtil.createDocument(AcademyConstants.ELE_MULTIAPI);

			Iterator<Entry<String, Document>> itChangeShipment = hmShipmentToUpdate.entrySet().iterator();
			
			while (itChangeShipment.hasNext()) {
				Map.Entry<String, Document> meChangeShipmentStatus = (Map.Entry<String, Document>)itChangeShipment.next();
				Document docShipmentInput = meChangeShipmentStatus.getValue();
				
				Element eleAPI = docChangeShipmentMultiAPI.createElement(AcademyConstants.ELE_API);
				eleAPI.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.API_CHANGE_SHIPMENT);
				
				// Prepare input for MultiAPI for multiple shipments
				Element eleInput = docChangeShipmentMultiAPI.createElement(AcademyConstants.ELE_INPUT);
				Element eleShipmentInp = docChangeShipmentMultiAPI.createElement(AcademyConstants.ELE_SHIPMENT);
				
				XMLUtil.copyElement(docChangeShipmentMultiAPI, docShipmentInput.getDocumentElement(), eleShipmentInp);
				
				eleInput.appendChild(eleShipmentInp);
				eleAPI.appendChild(eleInput);
				docChangeShipmentMultiAPI.getDocumentElement().appendChild(eleAPI);
			}
			
			log.verbose(":: Updated Doc :: " + XMLUtil.getXMLString(docChangeShipmentMultiAPI));
			AcademyUtil.invokeAPI(env,AcademyConstants.API_MULTI_API, docChangeShipmentMultiAPI);


		} catch (Exception e) {
			e.printStackTrace(); 
			log.verbose("Exception at AcademyCustomerPickUpdateShipmentQuantity.prepareAndInvokeChangeShipment");
		}		
		log.endTimer("AcademyCustomerPickUpdateShipmentQuantity::prepareAndInvokeChangeShipment");
	}


}
