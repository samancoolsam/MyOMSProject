package com.academy.ecommerce.sterling.sts;


import java.util.Arrays;

/*##################################################################################
 *
 * Project Name                : STS Project
 * Module                      : OMS
 * Author                      : CTS
 * Date                        : 07-JUL-2020 
 * Description				  : This class is to validate and send un-reserve message
 * 								to SIM when the Sales Order is cancelled
 * Change Revision
 * ---------------------------------------------------------------------------------
 * Date            Author         		Version#       Remarks/Description                      
 * ---------------------------------------------------------------------------------
 * 07-JUL-2020		CTS  	 			  1.0           	Initial version
 * 17-SEP-2020		CTS					  2.0			STS Shipment No will be sent in OrderNo Attribute
 * 														RMS transfer # will be sent in ShipmentNo Attribute
 * 
 * ##################################################################################*/

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XPathUtil;
import com.cts.sterling.custom.accelerators.util.XMLUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyPrepareSTSUnReserveMessageToSIM {

	//Class variables for logger and properties
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyPrepareSTSUnReserveMessageToSIM.class);
	private Properties props;

	public Document prepareSTSUnReserveMsg(YFSEnvironment env, Document inDoc) throws IllegalArgumentException, Exception{
		log.beginTimer("AcademyPrepareSTSUnReserveMessageToSIM.prepareSTSUnReserveMsg()");
		log.verbose("Input - AcademyPrepareSTSUnReserveMessageToSIM.prepareSTSUnReserveMsg() input"+XMLUtil.getString(inDoc));

		//Fetching list of status from Service arguments which are eligible for sending un-reserve message
		List<String> listShipmentStatus = null;
		String strSalesStatus = props.getProperty(AcademyConstants.STR_STS_SALES_STATUS);
		listShipmentStatus = Arrays.asList(props.getProperty(AcademyConstants.STR_STS_CANCEL_SALES_STATUS).split(AcademyConstants.STR_COMMA));
		log.verbose("Sales Status Check is" + strSalesStatus);

		//Fetch list of cnacelled STS Lines
		NodeList nlOrderLineList = XPathUtil.getNodeList(inDoc, AcademyConstants.XPATH_STS_ORDERLINE_LIST);
		int iSTSOrderLines = nlOrderLineList.getLength();

		//Maps Cancelled Sales OrderLine and Cancelled Qty
		HashMap<String, String> hmCancelledOrderLine = new HashMap<String, String>(); 
		if(iSTSOrderLines > 0) {
			log.verbose(" Order Contains STS Liens which are cancelled");

			for(int iOl =0; iOl< iSTSOrderLines ; iOl++) {
				Element eleOrderLine = (Element)nlOrderLineList.item(iOl);
				String strOrderLineKey = eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
				//String strOrderLineStatus = XPathUtil.getString(eleOrderLine, AcademyConstants.XPATH_TO_CANCELLED_FROM_STATUS);
				String strAuditReference1 = XPathUtil.getString(inDoc, AcademyConstants.XPATH_AUDIT_REFERENCE1);
				
				//Start : OMNI-8763 : Logic to handle multiple order line status
				NodeList nlCancelledFrom = XPathUtil.getNodeList(eleOrderLine, "StatusBreakupForCanceledQty/CanceledFrom");
				log.verbose("nlCancelledFrom :: " + nlCancelledFrom.getLength());

				for(int iCF =0; iCF< nlCancelledFrom.getLength() ; iCF++) {
					Element eleCancelledFrom = (Element)nlCancelledFrom.item(iCF);
					String strStatus = eleCancelledFrom.getAttribute(AcademyConstants.ATTR_STATUS);
					
					boolean bOrderStatus = checkOrderStatus(strStatus,strSalesStatus);
					log.verbose("Cancelled Status is " + strStatus + " bOrderStatus is greater " + bOrderStatus );
					if((!YFCObject.isNull(strAuditReference1) && !YFCObject.isVoid(strAuditReference1) && strAuditReference1.equals(AcademyConstants.STR_TO_INITIATED_CANCEL)) ||
							bOrderStatus || listShipmentStatus.contains(strStatus)) {

						String strCancelledQty = eleCancelledFrom.getAttribute(AcademyConstants.ATTR_QUANTITY);
						if(hmCancelledOrderLine.containsKey(strOrderLineKey)) {
							hmCancelledOrderLine.put(strOrderLineKey, 
								Double.toString(Double.parseDouble(hmCancelledOrderLine.get(strOrderLineKey)) + Double.parseDouble(strCancelledQty))) ;
						}
						else {
							hmCancelledOrderLine.put(strOrderLineKey, strCancelledQty) ;							
						}
					}
				}
				//End : OMNI-8763 : Logic to handle multiple order line status
			} 
		}
		log.verbose("hmCancelledOrderLine :: " + hmCancelledOrderLine.toString());

		//Lines ELigible for Sending messgae.
		if(hmCancelledOrderLine.size() > 0) {
			String strOrderHeaderKey = XPathUtil.getString(inDoc, AcademyConstants.XPATH_ORDERHEADER_KEY); 
			prepareAndSendUnReserveMessage(env, hmCancelledOrderLine, strOrderHeaderKey);

		}

		log.verbose("End - Inside AcademyPrepareSTSUnReserveMessageToSIM.prepareSTSUnReserveMsg()");
		log.endTimer("AcademyPrepareSTSUnReserveMessageToSIM.prepareSTSUnReserveMsg()");
		return inDoc;
	}

	/**
	 * This method invokes and returns getCompleteOrderLineList API
	 * 
	 * @param env,
	 * @param inXML
	 */
	private Document callGetCompOrderLineListForSTS(YFSEnvironment env, String strOrderHeaderKey)
	throws IllegalArgumentException, Exception {

		log.verbose("Start - AcademyPrepareSTSUnReserveMessageToSIM.callGetCompOrderLineListForSTS()");
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

		log.verbose("End - AcademyPrepareSTSUnReserveMessageToSIM.callGetCompOrderLineListForSTS()");
		return docgetOrderLineListout;
	}

	/**
	 * This method prepares the final message to be sent for each shipment
	 * 
	 * @param env,
	 * @param inXML
	 */
	private void prepareAndSendUnReserveMessage(YFSEnvironment env, HashMap<String, String> hmCancelledOrderLine, String strOrderHeaderKey) throws Exception {
		log.verbose("Begin of AcademyPrepareSTSUnReserveMessageToSIM.prepareAndSendUnReserveMessage() method");
		
		HashMap<String, Document> hmUnreserveMessageShipment = new HashMap<String, Document>(); 

		Document docgetCompleteOrderLineListout = callGetCompOrderLineListForSTS(env,strOrderHeaderKey);
		String strTOOrderNo = XPathUtil.getString(docgetCompleteOrderLineListout,AcademyConstants.XPATH_TO_ORDER_NO);

		Iterator<Map.Entry<String,String>> iter = hmCancelledOrderLine.entrySet().iterator();
		
		/* Start OMNI-53518: STS2.0
		 * Do not send SIM Cancel Reserve Update before Shipment moves to Ready to Ship status
		 */
		
		String strTOShipmentStatus = XPathUtil.getString(docgetCompleteOrderLineListout, "OrderLineList/OrderLine/ShipmentLines/ShipmentLine/Shipment/@Status");
		log.verbose("STS 2.0 Shipment Status" + strTOShipmentStatus);
		
		//End OMNI-53518: STS2.0
		
		while (iter.hasNext()) {
			Map.Entry<String,String> entry = iter.next();

			Element eleTOShipmentLine = (Element) XPathUtil.getNode(docgetCompleteOrderLineListout,
					"OrderLineList/OrderLine/ShipmentLines/ShipmentLine[@ChainedFromOrderLineKey='"
					+ entry.getKey() + "']");
			if(!YFCObject.isNull(eleTOShipmentLine) && !YFCObject.isVoid(eleTOShipmentLine)) {
				String strShipmentKey = eleTOShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
				String strNodeType=XPathUtil.getString(docgetCompleteOrderLineListout, "OrderLineList/OrderLine/ShipmentLines/ShipmentLine[@ChainedFromOrderLineKey='"+ entry.getKey() + "']/Shipment/ShipNode/@NodeType");	
				if(hmUnreserveMessageShipment.containsKey(strShipmentKey)) {					
					log.verbose("Exisitng ShipmentKey. Updating hte ShipmentLines ");
					Document docUnRsvMsgToSIM = hmUnreserveMessageShipment.get(strShipmentKey);	
					Element eleUnRsvShipmentLines = XmlUtils.getChildElement(docUnRsvMsgToSIM.getDocumentElement(), AcademyConstants.ELE_SHIPMENT_LINES);
	
					//Updated Shipment doc with another shipment line details
					Element eleUnRsvShipmentLine = XmlUtils.createChild(eleUnRsvShipmentLines, AcademyConstants.ELE_SHIPMENT_LINE);
					eleUnRsvShipmentLine.setAttribute(AcademyConstants.ATTR_ORDER_NO, eleTOShipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_NO));
					eleUnRsvShipmentLine.setAttribute(AcademyConstants.ATTR_ITEM_ID, eleTOShipmentLine.getAttribute(AcademyConstants.ATTR_ITEM_ID)); 
					eleUnRsvShipmentLine.setAttribute(AcademyConstants.ATTR_SHORTAGE_QTY, entry.getValue());
					
					//Updated Extension flag to indicate message sent to SIM
					Element eleUnRsvExtn = XmlUtils.createChild(eleUnRsvShipmentLine, AcademyConstants.ELE_EXTN); 
					eleUnRsvExtn.setAttribute(AcademyConstants.ATTR_EXTN_MSG_TO_SIM, AcademyConstants.ATTR_Y);
					
					/* Start OMNI-53518: STS2.0
					 * Do not send SIM Cancel Reserve Update before Shipment moves to Ready to Ship to Store status
					 */
					if(strNodeType.equals("Store")) {
						if (compareStatus(strTOShipmentStatus, AcademyConstants.VAL_READY_TO_SHIP_STATUS) < 0) {
							log.verbose("STS 2.0 Shipment Status Skipped" + strTOShipmentStatus);
							continue;
						}	
					}
					// End OMNI-53518: STS2.0

					//Adding the updated doc to the hashmap
					hmUnreserveMessageShipment.put(strShipmentKey, docUnRsvMsgToSIM);
				}
				else {
					Element eleTOShipment =XmlUtils.getChildElement(eleTOShipmentLine, AcademyConstants.ELE_SHIPMENT); 
					//Start OMNI-9523 Stamping ExtnRMSTransferNumber in ShipmentNo and ShipmentNo in the OrderNo
					Element eleExtn = XmlUtils.getChildElement(eleTOShipment, AcademyConstants.ELE_EXTN); 
					//End OMNI-9523
					Document docUnRsvMsgToSIM = XmlUtils.createDocument(AcademyConstants.ELE_SHIPMENT);
					Element eleUnRsvShipment = docUnRsvMsgToSIM.getDocumentElement(); 
					
					//Copy the shipment level information from TO to form a new doc
					eleUnRsvShipment.setAttribute(AcademyConstants.ATTR_SHIP_NODE, eleTOShipment.getAttribute(AcademyConstants.ATTR_SHIP_NODE)); 
					//Start OMNI-9523 Stamping ExtnRMSTransferNumber in ShipmentNo and ShipmentNo in the OrderNo
					eleUnRsvShipment.setAttribute(AcademyConstants.ATTR_ORDER_NO, eleTOShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
					eleUnRsvShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, eleTOShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
					//End OMNI-9523
					eleUnRsvShipment.setAttribute(AcademyConstants.ATTR_RECV_NODE, eleTOShipment.getAttribute(AcademyConstants.ATTR_RECV_NODE));
					eleUnRsvShipment.setAttribute(AcademyConstants.ATTR_DOC_TYPE, eleTOShipment.getAttribute(AcademyConstants.ATTR_DOC_TYPE));
					//Start OMNI-47445: STS2.0
					eleUnRsvShipment.setAttribute(AcademyConstants.ATTR_ORDER_NAME, XPathUtil.getString(docgetCompleteOrderLineListout,AcademyConstants.XPATH_SO_ORDER_NO));
					if(strNodeType.equals("Store")) {
						eleUnRsvShipment.setAttribute(AcademyConstants.A_NODE_TYPE,strNodeType);
						/* Start OMNI-53518: STS2.0
						 * Do not send SIM Cancel Reserve Update before Shipment moves to Ready to Ship  to Store status
						 */
						if (compareStatus(strTOShipmentStatus,  AcademyConstants.VAL_READY_TO_SHIP_STATUS) < 0) {
							log.verbose("STS 2.0 Shipment Status Skipped" + strTOShipmentStatus);
							continue;
						}	
						// End OMNI-53518
					}
					else {
						eleUnRsvShipment.setAttribute(AcademyConstants.A_NODE_TYPE,"");	
					}
					//End OMNI-47445
					
					//Start OMNI-9523
					Element eleUnRsvShpExtn = XmlUtils.createChild(eleUnRsvShipment, AcademyConstants.ELE_EXTN);
					eleUnRsvShpExtn.setAttribute(AcademyConstants.ATTR_EXTN_RMS_TRANSFER_NO, eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_RMS_TRANSFER_NO));
					//End OMNI-9523
					Element eleUnRsvShipmentLines = XmlUtils.createChild(eleUnRsvShipment, AcademyConstants.ELE_SHIPMENT_LINES);
					
					Element eleUnRsvShipmentLine = XmlUtils.createChild(eleUnRsvShipmentLines, AcademyConstants.ELE_SHIPMENT_LINE);
					eleUnRsvShipmentLine.setAttribute(AcademyConstants.ATTR_ORDER_NO, eleTOShipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_NO));
					eleUnRsvShipmentLine.setAttribute(AcademyConstants.ATTR_ITEM_ID, eleTOShipmentLine.getAttribute(AcademyConstants.ATTR_ITEM_ID)); 
					eleUnRsvShipmentLine.setAttribute(AcademyConstants.ATTR_SHORTAGE_QTY, entry.getValue());
	
					//Updated Extension flag to indicate message sent to SIM
					Element eleUnRsvExtn = XmlUtils.createChild(eleUnRsvShipmentLine, AcademyConstants.ELE_EXTN); 
					eleUnRsvExtn.setAttribute(AcademyConstants.ATTR_EXTN_MSG_TO_SIM, AcademyConstants.ATTR_Y);	
					
					//Adding the new doc to the hashmap
					hmUnreserveMessageShipment.put(strShipmentKey, docUnRsvMsgToSIM);
				}
			}
		}

		log.verbose("hmUnreserveMessageShipment :: " + hmUnreserveMessageShipment.toString());

		if(hmUnreserveMessageShipment.size() > 0)
		{
			Iterator<Map.Entry<String, Document>> iter2 = hmUnreserveMessageShipment.entrySet().iterator();

			while (iter2.hasNext()) {
				Map.Entry<String, Document> entryForUnRsv = iter2.next();
				log.verbose("Sending message for ShipmentKey :: " + entryForUnRsv.getKey());
				sendUnreserveMessage(env, entryForUnRsv.getValue());
			}
		}
		
		log.verbose("End of AcademyPrepareSTSUnReserveMessageToSIM.prepareAndSendUnReserveMessage() method");
	}
	
	
	/**
	 * This method is be used to invoke AcademySTSSendUnRsvMsgToSIM serivce
	 * which sends the un-reserve message
	 * 
	 * @param env,
	 * @param inXML
	 */
	public void sendUnreserveMessage(YFSEnvironment env, Document docUnReserveShipment) throws Exception {
		log.verbose("Begin of AcademyPrepareSTSUnReserveMessageToSIM.sendUnreserveMessage() method");

		Element eleUnRsvShipLines = XmlUtils.getChildElement(docUnReserveShipment.getDocumentElement(),
				AcademyConstants.ELE_SHIPMENT_LINES);
		if (!YFCObject.isNull(eleUnRsvShipLines) && eleUnRsvShipLines.hasChildNodes()) {

			log.verbose("Input to UnReserve Msg To SIM is " + XMLUtil.getString(docUnReserveShipment));
			AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACADEMY_SEND_UNRSV_MSG_TO_SIM, docUnReserveShipment);
			log.verbose("Output to UnReserve Msg To SIM is " + XMLUtil.getString(docUnReserveShipment));

		} else {
			log.verbose("Shipment does not contain vlaid Shipment lines. So Skip logic " + XMLUtil.getString(docUnReserveShipment));			
		}

		log.verbose("End of AcademyPrepareSTSUnReserveMessageToSIM.sendUnreserveMessage() method");
	}

	/**
	 * This method compares two statuses.
	 * 
	 * @param StrOrderStatus
	 * @param strStatusVal
	 * @return
	 */
	public boolean checkOrderStatus(String StrShipmentStatus, String strStatusVal) {
		log.beginTimer("AcademyDynamicCondnForMsgToSIM.java-checkShipmentStatus() : Start");
		Integer anInt = compareStatus(StrShipmentStatus, strStatusVal);
		if (anInt == 0) {
			return false;
		} else if (anInt > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * THis method compares statuses without using the == operator.
	 * 
	 * @param status1 yfs_status code
	 * @param status2 yfs_status code
	 * @return if status1 == status2, it returns 0 else if status1 > status2, it
	 *         returns positive integer else if status1 < status2, it returns
	 *         negative integer
	 */
	public static Integer compareStatus(String status1, String status2) {
		int comparisonNumber = YFCCommon.compareStrings(status1, status2);
		if (comparisonNumber == 0) {
			return 0;
		} else if (!YFCCommon.isVoid(status1) && YFCCommon.isVoid(status2)) {
			return 1;
		} else if (YFCCommon.isVoid(status1) && !YFCCommon.isVoid(status2)) {
			return -1;
		}

		String[] splittedStatus1 = { status1 };
		if (status1.indexOf('.') > 0) {
			splittedStatus1 = status1.split("\\.");
		}

		String[] splittedStatus2 = { status2 };
		if (status2.indexOf('.') > 0) {
			splittedStatus2 = status2.split("\\.");
		}

		int minSubStatusNo = Math.min(splittedStatus1.length, splittedStatus2.length);
		int i = 0;
		for (; i < minSubStatusNo; i++) {
			comparisonNumber = Integer.parseInt(splittedStatus1[i]) - Integer.parseInt(splittedStatus2[i]);
			if (comparisonNumber != 0) {
				return comparisonNumber;
			}
		}

		if (splittedStatus1.length == splittedStatus2.length) {
			return 0;
		} else if (splittedStatus1.length > splittedStatus2.length) {
			return 1;
		} else if (splittedStatus1.length < splittedStatus2.length) {
			return -1;
		}

		return null;
	}

	public void setProperties(Properties props) throws Exception {

		this.props = props;
	}

}
