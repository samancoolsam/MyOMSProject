/**************************************************************************
  * Description	    : This class is invoked by by changeReleaseStatus event of changeOrder transaction for 0001 document type
 * 
 *  * Sample Input to class
 * <?xml version="1.0" encoding="UTF-8"?>
 *	<Order DocumentType="0001" EnterpriseCode="Academy_Direct" OrderHeaderKey="202103300601383652470257" OrderNo="SO_STS_Z3003_04">
 *	<OrderLines>
 *		<OrderLine DeliveryMethod="PICK" OrderLineKey="202103300601383652470258" OrderedQty="1.00" PrimeLineNo="1" ShipNode="134" SubLineNo="1">
 *			<Item ItemID="122912171"/>
 *			<FromOrderReleaseStatuses>
 *				<FromOrderReleaseStatus DatesChanged="Y" MovedQty="1.00" Status="1100">
 *					<ToOrderReleaseStatuses>
 *						<ToOrderReleaseStatus Status="2060"/>
 *					</ToOrderReleaseStatuses>
 *				</FromOrderReleaseStatus>
 *			</FromOrderReleaseStatuses>
 *		</OrderLine>
 *	</OrderLines>
 *</Order>
 * --------------------------------
 * 	Date             Author               
 * --------------------------------
 *  6-April-2021      Cognizant			 	 
 * 
 * -------------------------------------------------------------------------
 **************************************************************************/


package com.academy.ecommerce.yantriks.inventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static com.academy.util.constants.AcademyConstants.*;
import com.academy.util.common.StringUtil;
import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantriks.yih.adapter.util.YantriksCommonUtil;


public class AcademyPostKafkaUpdateToQueue {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPostKafkaUpdateToQueue.class);
	//code changes for OMNI-92694
	private String strItemID="";
	private String strQuantity="";

	public Document prepareSupplyDemandUpdates(YFSEnvironment env, Document docInXml) throws Exception {
		log.verbose("Reading Transaction objects");
		String strStatus = (String) env.getTxnObject(ATTR_STATUS);
		String strShipmentKey = (String) env.getTxnObject(ATTR_SHIPMENT_KEY);
		String strContainerNo = (String) env.getTxnObject(ATTR_CONTAINER_NO);
		String isSTSShipment= (String) env.getTxnObject(IS_STS_SHIPMENT);// Reading the txn object which is set on STS create shipment on success as part of OMNI-40029
		String isOnlyDemandUpdateNeeded= (String) env.getTxnObject(DEMAND_UPDATE_NEEDED);
		strItemID = (String) env.getTxnObject(ITEM_ID);
		strQuantity = (String) env.getTxnObject(ATTR_QUANTITY);
		//code changes for OMNI-92694--Start
		log.verbose("Status is " + strStatus);
		log.verbose("strShipmentKey is " + strShipmentKey);
		log.verbose("strContainerNo is " + strContainerNo);
		log.verbose("isSTSShipment is " + isSTSShipment);
		log.verbose("isOnlyDemandUpdateNeeded is " + isOnlyDemandUpdateNeeded);
		log.verbose("AcqItemID is " + strItemID);
		log.verbose("AcqQty is " + strQuantity);
		
		if (!YFCObject.isVoid(strStatus)) {
			log.verbose("Status is " + strStatus);
			// OMNI-53346, Change start
			String strSTS2Received = (String) env.getTxnObject("STS2.0_RECEIVED");
			if(strStatus.equals(AcademyConstants.STR_RECEIVED) && !YFCObject.isVoid(strSTS2Received) && strSTS2Received.equals(STR_YES))
			{
				log.verbose("STS2.0 line, publish only demand update");
				publishDemandToQ(env, docInXml);
			}else {
				publishSupplyToQ(env, strShipmentKey, strStatus,strContainerNo);
				publishDemandToQ(env, docInXml);
			}
			//publishSupplyToQ(env, strShipmentKey, strStatus,strContainerNo);
			//publishDemandToQ(env, docInXml);
			// OMNI-53346, Change end
		}
		/*OMNI-40029: Start Change - Publish demand to queue if Txn object is Y and if the SO status is 2160.00.01  */
		if (!YFCObject.isVoid(isSTSShipment) && STR_YES.equals(isSTSShipment)) {
			log.verbose("Is STS Shipment:"+isSTSShipment);
			String status = XPathUtil.getString(docInXml,XPATH_ORDERRELEASE_TO_STATUS);
			/*if (V_STATUS_2160_00_01.equals(status)) {*/
			//Changes for OMNI-63470
			if(!StringUtil.isEmpty(strStatus) && status.startsWith(V_STATUS_2160_00_01)){
				log.verbose("Status is :"+status);
				publishDemandToQ(env, docInXml);
			}	
		}
		/*OMNI-40029: End Change*/
		
		//OMNI-51891 BEGIN
		//OMS to publish demand details to Yantriks during Vendor Inventory Shortage (ReasonCode 902)
		if (!YFCObject.isVoid(isOnlyDemandUpdateNeeded) && STR_YES.equals(isOnlyDemandUpdateNeeded)) {
			log.verbose("Only Demand Update is Needed");
			publishDemandToQ(env, docInXml);
			log.verbose("Only Demand Update Published");
			
		}
		//OMNI-51891 END
		
		
		log.verbose("Return from the class after checking publish messages to queue");
		return docInXml;
	}

	public void publishDemandToQ(YFSEnvironment env, Document docInXml) throws Exception {
		/**
		 * Sample demand XML to publish to queue
		 * <Demand DocumentType="0001" EnterpriseCode="Academy_Direct" OrderHeaderKey="202103300601383652470257" OrderNo="SO_STS_Z3003_04"/>
		 */
		log.verbose("Publish Demand Details");
		Element eleInputRoot = docInXml.getDocumentElement();
		Document docDemand = SCXmlUtil.createDocument(ELE_DEMAND);
		Element eleDemandRoot = docDemand.getDocumentElement();
		/*OMNI-40029: Start Change - Form the demand input to publish to the queue*/
		YFCDocument inDoc = YFCDocument.getDocumentFor(docInXml);
		eleDemandRoot.setAttribute(ATTR_DOC_TYPE, eleInputRoot.getAttribute(ATTR_DOC_TYPE));
		eleDemandRoot.setAttribute(ATTR_ENTERPRISE_CODE, eleInputRoot.getAttribute(ATTR_ENTERPRISE_CODE));
		if (!YFCObject.isVoid(eleInputRoot.getAttribute(STR_ORDR_HDR_KEY))) {
		eleDemandRoot.setAttribute(STR_ORDR_HDR_KEY, eleInputRoot.getAttribute(STR_ORDR_HDR_KEY));
		eleDemandRoot.setAttribute(ATTR_ORDER_NO, eleInputRoot.getAttribute(ATTR_ORDER_NO));
		}
		else {
			YFCElement eleShipmentLines = inDoc.getDocumentElement().getChildElement(ELE_SHIPMENT_LINES);
			YFCElement eleShipmentLine=eleShipmentLines.getChildElement(ELE_SHIPMENT_LINE);
			eleDemandRoot.setAttribute(STR_ORDR_HDR_KEY, eleShipmentLine.getAttribute(STR_ORDR_HDR_KEY));
			eleDemandRoot.setAttribute(ATTR_ORDER_NO, eleShipmentLine.getAttribute(ATTR_ORDER_NO));
			}
		/*OMNI-40029: Start Change*/
		//SCXmlUtil.setAttributes(eleInputRoot, eleDemandRoot);
		
		/*OMNI-48549 :Start Change - Reading transaction objects for STS and SFDC for ALLOCATED update which is set on transactions CONSOLIDATE_TO_SHIPMENT for SFDC and CREATE_SHIPMENT.0006 for STS */
		String isAllocatedCreated=(String) env.getTxnObject(IS_ALLOCATED_CREATED);
		String isSTSShipment= (String) env.getTxnObject(IS_STS_SHIPMENT);
		log.verbose("isAllocatedCreated: "+isAllocatedCreated);
		log.verbose("isSTSShipment: "+isSTSShipment);
		/*Setting Allocated="Y" in the Demand XML 
		<Demand DocumentType="0001" EnterpriseCode="Academy_Direct" OrderHeaderKey="202103300601383652470257" OrderNo="SO_STS_Z3003_04" Allocated="Y"/>
		*/
		if ((!YFCObject.isVoid(isAllocatedCreated) && STR_YES.equals(isAllocatedCreated)) || (!YFCObject.isVoid(isSTSShipment) && STR_YES.equals(isSTSShipment))) {
			log.verbose("Setting Allocated attribute in the Demand XML published to Queue");
			eleDemandRoot.setAttribute(STR_ALLOCATED, STR_YES);
		}
		
		/*OMNI-48549 - End Change*/
		log.verbose("Demand document to be published \n" + SCXmlUtil.getString(docDemand));
		// new YASCreateOrderReservationUEImpl().createReservationPayloadFromGetOrderList(env, docDemand);
		publishMessageToQ(env, docDemand);

	}

	private void publishSupplyToQ(YFSEnvironment env, String strShipmentKey, String strStatus, String strContainerNo) throws Exception {
		/**
		 * Sample supply details to publish to queue
		 * <Supply ItemID="112140737" NodeType="DC" Quantity="-1" ShipNode="711" SupplyType="SOH" UnitOfMeasure="EACH"/>
		 */
		
		log.verbose("Publish Supply Details");
		// ShipmentKey is set as transaction object either in AcademySTSUpdateTrailerDetails or AcademySTSProcessTOSIMReceipt class
		if (!YFCObject.isVoid(strShipmentKey)) {
			log.verbose("Shipment Key is present");
			Document docInToGetShipmentDetails = SCXmlUtil.createDocument(ELE_SHIPMENT);
			Element eleRootOfDocInToGetShipment = docInToGetShipmentDetails.getDocumentElement();
			eleRootOfDocInToGetShipment.setAttribute(ATTR_SHIPMENT_KEY, strShipmentKey);

			log.verbose("Input to getShipmentList API: " + SCXmlUtil.getString(docInToGetShipmentDetails));

			env.setApiTemplate(GET_SHIPMENT_LIST_API, TEMPLATE_GET_SHIPMENT_LIST_KAFKA_UPDATE);
			Document docOutShipmentDetails = AcademyUtil.invokeAPI(env, GET_SHIPMENT_LIST_API, docInToGetShipmentDetails);
			// Document docOutShipmentDetails = SCXmlUtil.createFromFileOrUrl("C:\\Users\\DELL\\Desktop\\CTS Docs\\getShipmentListOutput.xml");
			env.clearApiTemplate(GET_SHIPMENT_LIST_API);
			log.verbose("Shipment details output is "+SCXmlUtil.getString(docOutShipmentDetails));
			Element eleShipments = docOutShipmentDetails.getDocumentElement();
			Element eleShipment = SCXmlUtil.getChildElement(eleShipments, ELE_SHIPMENT);
			if (!YFCObject.isVoid(eleShipment)) {
				String strShipmentType = eleShipment.getAttribute(ATTR_SHIPMENT_TYPE);
				if (!YFCObject.isVoid(strShipmentType) && STR_SHIP_TO_STORE.equals(strShipmentType)) {
					if (STR_RECEIVED.equals(strStatus)) {
						processContainerBeingReceived(env, eleShipment, strContainerNo);// Handling the logic to modify the shipment element so that supply updates are sent only for the shipment lines which are present in the container which is received in SOM 
					}
					log.verbose("Shipment type is STS");
					Element eleShipmentLines = SCXmlUtil.getChildElement(eleShipment, ELE_SHIPMENT_LINES);
					List<Element> listOfShipmentLines = SCXmlUtil.getChildren(eleShipmentLines, ELE_SHIPMENT_LINE);
					for (Element eleCurrentShipmentLine : listOfShipmentLines) {
						log.verbose("CurrentShipmentLine iterator");
						for (int i = 1; i <= 2; i++) {
							Document docSupplyUpdate = SCXmlUtil.createDocument(E_SUPPLY);
							Element eleRootOfSupplyUpdateDoc = docSupplyUpdate.getDocumentElement();
							eleRootOfSupplyUpdateDoc.setAttribute(ATTR_ITEM_ID, eleCurrentShipmentLine.getAttribute(ATTR_ITEM_ID));
							eleRootOfSupplyUpdateDoc.setAttribute(ATTR_UOM, UOM_EACH_VAL);
							if (i == 1) {
								log.verbose("First Iteration");
								String strNode = null;
								if (STR_RECEIVED.equals(strStatus)) {
									log.verbose("Status is Received");
									strNode = eleShipment.getAttribute(ATTR_RECV_NODE);
									eleRootOfSupplyUpdateDoc.setAttribute(ATTR_QUANTITY, eleCurrentShipmentLine.getAttribute(ATTR_QUANTITY));
								} else {
									log.verbose("Status is Shipped");
									strNode = eleShipment.getAttribute(ATTR_SHIP_NODE);
									eleRootOfSupplyUpdateDoc.setAttribute(ATTR_QUANTITY, STR_HYPHEN + eleCurrentShipmentLine.getAttribute(ATTR_QUANTITY));
								}

								eleRootOfSupplyUpdateDoc.setAttribute(ATTR_SUPPLY_TYPE, V_SOH);
								eleRootOfSupplyUpdateDoc.setAttribute(ATTR_SHIP_NODE, strNode);
								log.verbose("Find Node Type Here");
								String strNodeType = YantriksCommonUtil.getLocationType(env, strNode);
								log.verbose("NodeType is " + strNodeType);
								eleRootOfSupplyUpdateDoc.setAttribute(A_NODE_TYPE, strNodeType);
							} else {
								log.verbose("Second Iteration");
								String strNode = eleShipment.getAttribute(ATTR_RECV_NODE);
								if (STR_RECEIVED.equals(strStatus)) {
									log.verbose("Status is Received");
									eleRootOfSupplyUpdateDoc.setAttribute(ATTR_QUANTITY, STR_HYPHEN + eleCurrentShipmentLine.getAttribute(ATTR_QUANTITY));
								} else {
									log.verbose("Status is Shipped");
									eleRootOfSupplyUpdateDoc.setAttribute(ATTR_QUANTITY, eleCurrentShipmentLine.getAttribute(ATTR_QUANTITY));
								}
								eleRootOfSupplyUpdateDoc.setAttribute(ATTR_SUPPLY_TYPE, V_IN_TRANSIT);
								eleRootOfSupplyUpdateDoc.setAttribute(ATTR_SHIP_NODE, strNode);
								log.verbose("Find Node Type Here");
								String strNodeType = YantriksCommonUtil.getLocationType(env, strNode);
								eleRootOfSupplyUpdateDoc.setAttribute(A_NODE_TYPE, strNodeType);
							}
							log.verbose("\nSupply Details To Publish");
							log.verbose(SCXmlUtil.getString(docSupplyUpdate));
							// new IntegServerWillInvokeThisClass().kafkaInvDeltaUpdate(null, docSupplyUpdate);
							publishMessageToQ(env, docSupplyUpdate);
						}
					}
				}
			}
		} else {
			log.verbose("Shipment Key Not Present");
			// YFSException yfsException = new YFSException();
			// String exceptionId = AcademyConstants.EXP_SHIPMENT_NOT_FOUND + strShipmentKey;
			// yfsException.setErrorDescription(exceptionId);
			// throw yfsException;

		}
	} 

	private void publishMessageToQ(YFSEnvironment env, Document docInput) throws Exception {
		// Invoke Service to insert record in Q
		AcademyUtil.invokeService(env, SERVICE_YANTRIKS_KAFKA_DELTA_UPDATE, docInput);
	}
	
	/*This method is added as a fix so that existing logic is not modified. This method will modify the existing Shipment element to retain only the shipment lines
	 *  present within the received container and remove the other shipment lines which are not present */
	
	private void processContainerBeingReceived(YFSEnvironment env, Element eleShipment, String strContainerNo) throws Exception {
		Element eleContainerFromShipment = (Element) XPathUtil.getNode(eleShipment, "//Shipments/Shipment/Containers/Container[@ContainerNo='" + strContainerNo + "']");
		Map<String, String> mapOfSLKeyAndQuantity = new HashMap<String, String>();
		if (!YFCObject.isVoid(eleContainerFromShipment)) {
			NodeList listOfShipmentKeysInThisContainer = XPathUtil.getNodeList(eleContainerFromShipment, "ContainerDetails/ContainerDetail");
			for (int i = 0; i < listOfShipmentKeysInThisContainer.getLength(); i++) {
				Element eleCurrentLine = (Element) listOfShipmentKeysInThisContainer.item(i);
				String strLineKey = eleCurrentLine.getAttribute(ATTR_SHIPMENT_LINE_KEY);
				String strQuantity = eleCurrentLine.getAttribute(ATTR_QUANTITY);
				mapOfSLKeyAndQuantity.put(strLineKey, strQuantity);
				log.verbose("Adding this line to set with container No " + strContainerNo + " and Line Key as " + eleCurrentLine.getAttribute(ATTR_SHIPMENT_LINE_KEY));
			}
		}
		else
		{
			// code changes for OMNI-96161
			String strLineKey = (String) XPathUtil.getString(eleShipment, "//Shipments/Shipment/ShipmentLines/ShipmentLine[@ItemID='" + strItemID + "']/@ShipmentLineKey");
			if(!YFCObject.isVoid(strLineKey))
			{
				mapOfSLKeyAndQuantity.put(strLineKey, strQuantity);
			}
		}
		
		if (mapOfSLKeyAndQuantity.size() > 0) {
			Element eleShipmentLines = SCXmlUtil.getChildElement(eleShipment, ELE_SHIPMENT_LINES);
			List<Element> listOfShipmentLines = SCXmlUtil.getChildren(eleShipmentLines, ELE_SHIPMENT_LINE);
			for (Element eleCurrentShipmentLine : listOfShipmentLines) {
				String strShimentLineKey = eleCurrentShipmentLine.getAttribute(ATTR_SHIPMENT_LINE_KEY);
				if (!mapOfSLKeyAndQuantity.keySet().contains(strShimentLineKey)) {
					log.verbose("Removing this line " + strShimentLineKey);
					eleShipmentLines.removeChild(eleCurrentShipmentLine);
				} else {
					// Temporarily setting the quantity as per Container
					// Quantity so that exact quantity will be communicated to
					// Yantriks in supply update
					eleCurrentShipmentLine.setAttribute(ATTR_QUANTITY, mapOfSLKeyAndQuantity.get(strShimentLineKey));
				}
			}
		}
	}

	// public static void main(String[] args) throws Exception {
	// Document doc = SCXmlUtil.createFromFileOrUrl("C:\\Users\\DELL\\Desktop\\CTS Docs\\changeOrderOnChangeReleaseStatus.xml");
	// ClassToInvokeOnReleaseStatusChangeEvent obj = new ClassToInvokeOnReleaseStatusChangeEvent();
	// obj.prepareSupplyDemandUpdates(null, doc);
	// }

}
