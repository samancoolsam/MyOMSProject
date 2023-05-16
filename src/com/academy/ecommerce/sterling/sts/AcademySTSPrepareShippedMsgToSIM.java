package com.academy.ecommerce.sterling.sts;

/*##################################################################################
*
* Project Name                : STS Project
* Module                      : OMS
* Author                      : CTS
* Date                        : 20-JUL-2020 
* Description				  : This class is to validate and send Fulfill message
* 								to SIM when the Sales Order is Fulfilled
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author         		Version#       Remarks/Description                      
* ---------------------------------------------------------------------------------
* 20-JUL-2020		CTS  	 			  1.0           	Initial version
* 17-SEP-2020		CTS					  2.0			STS Shipment No will be sent in OrderNo Attribute
* 														RMS transfer # will be sent in ShipmentNo Attribute 
* ##################################################################################*/

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.los.XMLUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademySTSPrepareShippedMsgToSIM {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademySTSPrepareShippedMsgToSIM.class);

	/*
	 * This method is used to prepare and send STS Fulfill Message to SIM
	 */
	public Document prepareSTSFulfillMsg(YFSEnvironment env, Document inDoc) throws Exception {

		log.verbose("Start of AcademySTSPrepareShippedMsgToSIM::prepareSTSFulfillMsg()");

		log.verbose("Entering AcademySTSPrepareShippedMsgToSIM.prepareSTSFulfillMsg() with Input :: "
				+ XMLUtil.getString(inDoc));

		Document outDoc = null;
		
		String strSOOrderHeaderKey = XPathUtil.getString(inDoc,AcademyConstants.XPATH_SHIPMENT_SHIPMENTLINE_OHK);
		
		NodeList nShpList = XPathUtil.getNodeList(inDoc, AcademyConstants.XPATH_SHIPMENT_LINE);
		int iSTSShipLines = nShpList.getLength();
		HashMap<String,String> hmFulfilledShpLines = new HashMap<String,String>();
		
		if(iSTSShipLines > 0) {
			
			for(int i=0; i < iSTSShipLines ; i++) {
			Element eleShipmentLine = (Element)nShpList.item(i); 
			String strSalesOrderLineKey = eleShipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
			String strFulfiledQty = eleShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY);
			
			hmFulfilledShpLines.put(strSalesOrderLineKey, strFulfiledQty);
			}
		}
		
		log.verbose("hmFulfilledShpLines :: " + hmFulfilledShpLines.toString());
		
		if(hmFulfilledShpLines.size() > 0) {
			if (!YFCObject.isVoid(strSOOrderHeaderKey)) {
				prepareSTSFulfillMsgToSIM(env, hmFulfilledShpLines, strSOOrderHeaderKey);
			}
		}
		log.verbose("End of AcademySTSPrepareShippedMsgToSIM::prepareSTSFulfillMsg()");
		return outDoc;
	} 
	
	/**
	 * This method invokes and returns getCompleteOrderLineList API
	 * 
	 * @param env,
	 * @param inXML
	 */
	private Document getCompleteOrderLineList(YFSEnvironment env, String strOrderHeaderKey)
	throws IllegalArgumentException, Exception {

		log.verbose("Start - AcademySTSPrepareShippedMsgToSIM.getCompleteOrderLineList()");
		log.verbose("OrderHeaderkey is " + strOrderHeaderKey);
		Document docgetComOrderLineListinput = XMLUtil
		.getDocument("<OrderLine ChainedFromOrderHeaderKey='" + strOrderHeaderKey + "' DocumentType='0006' />");

		env.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_LINE_LIST,
				AcademyConstants.STR_TEMPLATE_GET_COMP_ORDERLINE_LIST_STS_UNRSV);
		log.verbose("Input for getCompleteOrderLineList Api is " + XMLUtil.getString(docgetComOrderLineListinput));
		Document docgetOrderLineListout = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMPLETE_ORDER_LINE_LIST,
				docgetComOrderLineListinput);
		log.verbose("Output for getCompleteOrderLineList Api is " + XMLUtil.getString(docgetOrderLineListout));
		env.clearApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_LINE_LIST);

		log.verbose("End - AcademySTSPrepareShippedMsgToSIM.getCompleteOrderLineList()");
		return docgetOrderLineListout;
	}

	public void prepareSTSFulfillMsgToSIM(YFSEnvironment env, HashMap<String,String> hmFulfilledShpLines, String strSOOrderHeaderKey)
			throws Exception {
		log.verbose("Begin of AcademySTSPrepareShippedMsgToSIM.prepareSTSFulfillMsgToSIM() method"); 
		
		HashMap<String, Document> hmFulfillMsgShp = new HashMap<String, Document>(); 
		Document docgetCompleteOrderLineListout = getCompleteOrderLineList(env, strSOOrderHeaderKey);
		
		Iterator<Map.Entry<String,String>> iter = hmFulfilledShpLines.entrySet().iterator();
		
		while (iter.hasNext()) {
			Map.Entry<String,String> entry = iter.next();
			
			Element eleTOShipmentLine = (Element) XPathUtil.getNode(docgetCompleteOrderLineListout,
					"OrderLineList/OrderLine/ShipmentLines/ShipmentLine[@ChainedFromOrderLineKey='"
					+ entry.getKey() + "']");
			
			if(!YFCObject.isNull(eleTOShipmentLine) && !YFCObject.isVoid(eleTOShipmentLine)) {
				String strShipmentKey = eleTOShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
				if(hmFulfillMsgShp.containsKey(strShipmentKey)) {
					log.verbose("Exisitng ShipmentKey. Updating hte ShipmentLines ");
					Document docFulfillMsgToSIM = hmFulfillMsgShp.get(strShipmentKey);	
					Element eleFulfillShipmentLines = XmlUtils.getChildElement(docFulfillMsgToSIM.getDocumentElement(), AcademyConstants.ELE_SHIPMENT_LINES);
					Element eleTOShipment =XmlUtils.getChildElement(eleTOShipmentLine, AcademyConstants.ELE_SHIPMENT); 
					//Updated Shipment doc with another shipment line details
					Element eleFulfillShipmentLine = XmlUtils.createChild(eleFulfillShipmentLines, AcademyConstants.ELE_SHIPMENT_LINE);
					eleFulfillShipmentLine.setAttribute(AcademyConstants.ATTR_ORDER_NO, eleTOShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
					eleFulfillShipmentLine.setAttribute(AcademyConstants.ATTR_ITEM_ID, eleTOShipmentLine.getAttribute(AcademyConstants.ATTR_ITEM_ID)); 
					eleFulfillShipmentLine.setAttribute(AcademyConstants.ATTR_QUANTITY, entry.getValue());
					
					//Adding the updated doc to the hashmap
					hmFulfillMsgShp.put(strShipmentKey, docFulfillMsgToSIM);
				}else {
					log.verbose("New Shipment.");
					Element eleTOShipment =XmlUtils.getChildElement(eleTOShipmentLine, AcademyConstants.ELE_SHIPMENT); 
					//Start OMNI-9523 Stamping ExtnRMSTransferNumber in ShipmentNo and ShipmentNo in the OrderNo
					Element eleExtn = XmlUtils.getChildElement(eleTOShipment, AcademyConstants.ELE_EXTN); 
					//End OMNI-9523
					Document docFulfillMsgToSIM = XmlUtils.createDocument(AcademyConstants.ELE_SHIPMENT);
					Element eleFulFillShipment = docFulfillMsgToSIM.getDocumentElement(); 
					
					//Copy the shipment level information from TO to form a new doc
					eleFulFillShipment.setAttribute(AcademyConstants.ATTR_SHIP_NODE, eleTOShipment.getAttribute(AcademyConstants.ATTR_SHIP_NODE)); 
					//Start OMNI-9523 Stamping ExtnRMSTransferNumber in ShipmentNo and ShipmentNo in the OrderNo
					eleFulFillShipment.setAttribute(AcademyConstants.ATTR_ORDER_NO, eleTOShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
					eleFulFillShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO,eleTOShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
					//End OMNI-9523
					eleFulFillShipment.setAttribute(AcademyConstants.ATTR_RECV_NODE, eleTOShipment.getAttribute(AcademyConstants.ATTR_RECV_NODE));
					eleFulFillShipment.setAttribute(AcademyConstants.ATTR_DOC_TYPE, eleTOShipment.getAttribute(AcademyConstants.ATTR_DOC_TYPE));
					//Start OMNI-47445: STS2.0
					eleFulFillShipment.setAttribute(AcademyConstants.ATTR_ORDER_NAME, XPathUtil.getString(docgetCompleteOrderLineListout,AcademyConstants.XPATH_SO_ORDER_NO));
					//String strNodeType=XPathUtil.getString(docgetCompleteOrderLineListout, "OrderLineList/OrderLine/ShipmentLines/ShipmentLine/Shipment/ShipNode/@NodeType");
					//OMNI-88753,OMNI-89586 - START
					String strNodeType=XPathUtil.getString(docgetCompleteOrderLineListout, "OrderLineList/OrderLine/ShipmentLines/ShipmentLine[@ChainedFromOrderLineKey='"+ entry.getKey() + "']/Shipment/ShipNode/@NodeType");
					eleFulFillShipment.setAttribute(AcademyConstants.ATTR_ORDER_NAME, XPathUtil.getString(docgetCompleteOrderLineListout,AcademyConstants.XPATH_SO_ORDER_NO));
					if(strNodeType.equals("Store")) {
						eleFulFillShipment.setAttribute(AcademyConstants.A_NODE_TYPE,strNodeType);
					}
					else {
						eleFulFillShipment.setAttribute(AcademyConstants.A_NODE_TYPE,"");	
					}
					//OMNI-88753,OMNI-89586 - END
					//End OMNI-47445
					//Start OMNI-9523
					Element eleFulfillExtn = XmlUtils.createChild(eleFulFillShipment, AcademyConstants.ELE_EXTN);
					eleFulfillExtn.setAttribute(AcademyConstants.ATTR_EXTN_RMS_TRANSFER_NO, eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_RMS_TRANSFER_NO));
					//End OMNI-9523
					Element eleFulfillShipmentLines = XmlUtils.createChild(eleFulFillShipment, AcademyConstants.ELE_SHIPMENT_LINES);
					
					Element eleFulfillShipmentLine = XmlUtils.createChild(eleFulfillShipmentLines, AcademyConstants.ELE_SHIPMENT_LINE);
					eleFulfillShipmentLine.setAttribute(AcademyConstants.ATTR_ORDER_NO, eleTOShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
					eleFulfillShipmentLine.setAttribute(AcademyConstants.ATTR_ITEM_ID, eleTOShipmentLine.getAttribute(AcademyConstants.ATTR_ITEM_ID)); 
					eleFulfillShipmentLine.setAttribute(AcademyConstants.ATTR_QUANTITY, entry.getValue());
					
					//Adding the new doc to the hashmap
					hmFulfillMsgShp.put(strShipmentKey, docFulfillMsgToSIM);
				}
			}
		}
		
		log.verbose("hmFulfillMsgShp :: " + hmFulfillMsgShp.toString());
		
		if(hmFulfillMsgShp.size() > 0) {
			Iterator<Map.Entry<String, Document>> iter2 = hmFulfillMsgShp.entrySet().iterator();

			while (iter2.hasNext()) {
				Map.Entry<String, Document> entryForFulfillMsg = iter2.next();
				log.verbose("Sending message for ShipmentKey :: " + entryForFulfillMsg.getKey());
				sendFulfillMessage(env, entryForFulfillMsg.getValue());
			}
		}
		
		log.verbose("End of AcademySTSPrepareShippedMsgToSIM.prepareSTSFulfillMsgToSIM() method");
		
	}
	
	/**
	 * This method is be used to invoke AcademySTSSendFulfillMsgToSIM serivce
	 * which sends the Fulfill message
	 * 
	 * @param env,
	 * @param inXML
	 * @throws Exception 
	 */
	private void sendFulfillMessage(YFSEnvironment env, Document docFulfllMsgToSIM) throws Exception {
		// TODO Auto-generated method stub
		log.verbose("Begin of AcademySTSPrepareShippedMsgToSIM.sendFulfillMessage() method");

		Element eleFulfillShipLines = XmlUtils.getChildElement(docFulfllMsgToSIM.getDocumentElement(),
				AcademyConstants.ELE_SHIPMENT_LINES);
		if (!YFCObject.isNull(eleFulfillShipLines) && eleFulfillShipLines.hasChildNodes()) {

			log.verbose("Input to Fulfill Msg To SIM is " + XMLUtil.getString(docFulfllMsgToSIM));
			Document docFulfllMsgToSIMOutput = AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACADEMY_FULFILL_MSG_TO_SIM, docFulfllMsgToSIM);
			log.verbose("Output to Fulfill Msg To SIM is " + XMLUtil.getString(docFulfllMsgToSIMOutput));

		} else {
			log.verbose("Shipment does not contain vlaid Shipment lines. So Skip logic " + XMLUtil.getString(docFulfllMsgToSIM));			
		}

		log.verbose("End of AcademySTSPrepareShippedMsgToSIM.sendFulfillMessage() method");
	}
}