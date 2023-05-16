package com.academy.ecommerce.sterling.shipment;

import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.json.utils.XML;
import org.elasticsearch.common.mvel2.ast.Strsim;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.shared.ycp.YFSContext;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyTOProcessBackroomPick {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyTOProcessBackroomPick.class);
	private static YIFApi api = null;

	static {
		try {
			api = YIFClientFactory.getInstance().getApi();
		} catch (YIFClientCreationException e) {
			e.printStackTrace();
		}
	}

	public void performBackroomPick(YFSEnvironment env, Document inXML) throws Exception {
		if (log.isVerboseEnabled()) {
			log.verbose("AcademyTOProcessBackroomPick: performBackroomPick: inXML: " + XMLUtil.getXMLString(inXML));

		}

		Element eleInXML = inXML.getDocumentElement();
		boolean isPickCompleted = false;
		boolean isShorted = false;
		boolean isPickLaterOptionSelected = false;
		boolean isPartaillyShorted = false;
		HashMap<String, Double> itemQuantityMap = new HashMap<String, Double>();
		Document changeShipmentInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleChangeShipmentInput = changeShipmentInput.getDocumentElement();
		Element changeShipmentLines = changeShipmentInput.createElement(AcademyConstants.ELE_SHIPMENT_LINES);
		eleChangeShipmentInput.appendChild(changeShipmentLines);
		String strShipmentKey = eleInXML.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		String strShipmentNo = eleInXML.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
		String strDocumentType = eleInXML.getAttribute(AcademyConstants.ATTR_DOC_TYPE);
		// OMNI-6615
		String strShipNode = eleInXML.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
		String strReceivingNode = null;
		String strContainerNo = "";
		// OMNI-6615

		String containerScm = "";

		// OMNI-6450
		Document docChangeShipmentOut = null;
		// OMNI-6450

		// OMNI-7980
		String strPrintPackId = null;
		// OMNI-7980

		// Check for Picking option
		if (eleInXML.getAttribute(AcademyConstants.STR_IS_COMPLETE_PICK).equals(AcademyConstants.STR_YES)) {
			isPickCompleted = true;
		}

		if (eleInXML.getAttribute(AcademyConstants.STR_RADIO_SELECTION)
				.equals(AcademyConstants.STR_BP_INVENTORY_SHORTAGE)) {
			isShorted = true;
			
			//OMNI-7244
			eleInXML.setAttribute(AcademyConstants.ATTR_SOURCE, AcademyConstants.STR_SOURCE_RCP);
			AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACAD_CREATE_INV_NODE_CONTROL, inXML);
			//OMNI-7244
		}
		if (eleInXML.getAttribute(AcademyConstants.STR_RADIO_SELECTION).equals(AcademyConstants.STR_BP_PICK_LATER)) {
			isPickLaterOptionSelected = true;
		}

		NodeList nlShipmentLine = eleInXML.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);

		boolean isAnyItemPicked = false;

		Element eleContainerDetails = changeShipmentInput.createElement(AcademyConstants.ELE_CONTAINER_DTLS);

		for (int index = 0; index < nlShipmentLine.getLength(); index++) {
			Double dShortageQty = 0.0;
			Double dBackroomPickedQty = 0.0;
			Double dPickedQty = 0.0;
			Element eleShipmentLine = (Element) nlShipmentLine.item(index);

			// OMNI-6615
			if (StringUtil.isEmpty(strReceivingNode)) {
				Element eleOrderLine = (Element) eleShipmentLine.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE)
						.item(0);
				strReceivingNode = eleOrderLine.getAttribute(AcademyConstants.ATTR_RECV_NODE);
			}
			// OMNI-6615

			if (!eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHORTAGE_QTY).trim().equals("")) {
				dShortageQty = Double.parseDouble(eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHORTAGE_QTY));
			}
			if (!eleShipmentLine.getAttribute(AcademyConstants.ATTR_BACKROOM_PICKED_QTY).trim().equals("")) {
				dBackroomPickedQty = Double
						.parseDouble(eleShipmentLine.getAttribute(AcademyConstants.ATTR_BACKROOM_PICKED_QTY));
			}
			if (!eleShipmentLine.getAttribute(AcademyConstants.ELE_PICK_QTY).trim().equals("")) {
				dPickedQty = Double.parseDouble(eleShipmentLine.getAttribute(AcademyConstants.ELE_PICK_QTY));
			}
			if (dPickedQty == 0) {
				if (!eleShipmentLine.getAttribute(AcademyConstants.ATTR_PICK_QTY_1).trim().equals("")) {
					dPickedQty = Double.parseDouble(eleShipmentLine.getAttribute(AcademyConstants.ATTR_PICK_QTY_1));
				}
			}

			double dNewBackroomPickedQuantity = dBackroomPickedQty + dPickedQty;
			Element changeShipmentLine = changeShipmentInput.createElement(AcademyConstants.ELE_SHIPMENT_LINE);

			log.verbose("changeShipmentLines: " + XMLUtil.getElementXMLString(changeShipmentLines));
			changeShipmentLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY,
					eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY));
			if (dPickedQty > 0) {
				isAnyItemPicked = true;
				changeShipmentLine.setAttribute(AcademyConstants.ATTR_BACKROOM_PICKED_QTY,
						"" + dNewBackroomPickedQuantity);
				Element containerDetail = changeShipmentInput.createElement(AcademyConstants.CONTAINER_DETL_ELEMENT);
				eleContainerDetails.appendChild(containerDetail);
				containerDetail.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY,
						eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY));
				containerDetail.setAttribute(AcademyConstants.ATTR_QUANTITY, "" + dPickedQty);

			}
			if (dBackroomPickedQty > 0) {
				isPartaillyShorted = true;
				isPickCompleted = true;
			}
			if (dShortageQty > 0 && isShorted) {

				changeShipmentLine.setAttribute(AcademyConstants.ATTR_BACKROOM_PICKED_QTY,
						"" + dNewBackroomPickedQuantity);
				changeShipmentLine.setAttribute(AcademyConstants.ATTR_QUANTITY, "" + dNewBackroomPickedQuantity);
			}
			if (dPickedQty > 0 || (dShortageQty > 0 && isShorted)) {
				changeShipmentLines.appendChild(changeShipmentLine);
				eleChangeShipmentInput.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.STR_ACTION_MODIFY);
			}

			if (!isPickLaterOptionSelected) {
				isPickCompleted = true;
			}

		}

		if (isAnyItemPicked) {
			log.verbose("Inside itemPicked condition");
			/*
			 * if(isAmmoShipment) { Element eleShipmentListOut = (Element)
			 * docShipmentListOutput.getElementsByTagName("Shipment").item(0);
			 * validateLOS(env, eleShipmentListOut , strLineType); }
			 */

			containerScm = getContainerScm(env);

			// OMNI-6615
			strContainerNo = getTOContainerNo(env, strShipNode, strReceivingNode);
			// OMNI-6615

			Element eleContainers = changeShipmentInput.createElement(AcademyConstants.ELE_CONTAINERS);
			Element eleContainer = changeShipmentInput.createElement(AcademyConstants.ELE_CONTAINER);
			eleContainer.appendChild(eleContainerDetails);

			String strContainerType = eleInXML.getAttribute(AcademyConstants.ATTR_CONATINER_TYPE).trim();
			log.verbose("ContainerType --------> " + strContainerType);
			if (AcademyConstants.STR_VENDOR_PACKAGE.equalsIgnoreCase(strContainerType)) {
				eleContainer = setContainerVolumeDetailsVndrPkg(env, eleInXML, eleContainer);
				
				//Start : OMNI-8665 : ContainerNo not being set for Vendor Package
				eleContainer.setAttribute(AcademyConstants.ATTR_CONTAINER_NO, strContainerNo);
				//End : OMNI-8665 : ContainerNo not being set for Vendor Package

				// OMNI-9066
				eleContainer.setAttribute(AcademyConstants.A_CONATINER_SCM, containerScm);
				// OMNI-9066
				
				eleContainers.appendChild(eleContainer);
				eleChangeShipmentInput.appendChild(eleContainers);
				if (log.isVerboseEnabled()) {
					log.verbose("ProcessBackroomPick: performBackroomPick: changeShipment: "
							+ XMLUtil.getXMLString(changeShipmentInput));
				}
			} else {
				// Invoke method to fetch the volume details of the container
				Element containerVolumePrimaryInformation = setContainerVolumeDetails(env, eleInXML, eleContainer);
				String containerGrossWeightString = eleInXML.getAttribute(AcademyConstants.ATTR_CONTAINER_GROSS_WEIGHT);
				double containerGrossWeight = (containerGrossWeightString.isEmpty()
						|| containerGrossWeightString == null) ? 0 : Double.parseDouble(containerGrossWeightString);
				double containerNetWeight = containerGrossWeight;
				if (containerGrossWeight == 0) {
					// Invoke method to fetch the container Net Weight
					log.verbose("Calling containerNetWeight method from conditio :NOT isMultiBox");
					containerNetWeight = getNetWeightOfItems(env, itemQuantityMap);
					containerGrossWeight = containerNetWeight + Double.parseDouble(
							containerVolumePrimaryInformation.getAttribute(AcademyConstants.ELE_UNIT_WEIGHT));
				}
				eleContainer.setAttribute(AcademyConstants.ATTR_CONTAINER_GROSS_WEIGHT, containerGrossWeight + "");
				eleContainer.setAttribute(AcademyConstants.ATTR_CONTAINER_NET_WEIGHT, containerNetWeight + "");
				eleContainer.setAttribute(AcademyConstants.ATTR_SCAC,
						eleInXML.getAttribute(AcademyConstants.ATTR_SCAC));
				eleContainer.setAttribute(AcademyConstants.CARRIER_SERVICE_CODE,
						eleInXML.getAttribute(AcademyConstants.CARRIER_SERVICE_CODE));

				eleContainer.setAttribute(AcademyConstants.ATTR_CONATINER_TYPE, AcademyConstants.STR_CASE);
				eleContainer.setAttribute(AcademyConstants.ATTR_IS_PACK_PROCESS_COMPLETED, AcademyConstants.ATTR_Y);

				eleContainer.setAttribute(AcademyConstants.A_CONATINER_SCM, containerScm);

				// OMNI-6615
				eleContainer.setAttribute(AcademyConstants.ATTR_CONTAINER_NO, strContainerNo);
				// OMNI-6615

				// Append child element Containers
				eleContainers.appendChild(eleContainer);
				eleChangeShipmentInput.appendChild(eleContainers);
				if (log.isVerboseEnabled()) {
					log.verbose("ProcessBackroomPick: performBackroomPick: changeShipment: "
							+ XMLUtil.getXMLString(changeShipmentInput));
				}

			}
		}

		else if (isShorted && !isPartaillyShorted) {
			log.verbose("Full Shortage");
			/*
			 * <Shipment Action="Cancel" CancelRemovedQuantity="Y" DocumentType="0006"
			 * ShipmentKey="202005100453131982021471" ShipmentNo="100233534"> <Extn
			 * ExtnShortpickReasonCode="Freshness Standards"
			 * ExtnSopModifyts="2020-05-09T07:06:39"/> </Shipment>
			 */
			isPickCompleted = false;
			cancelShipmentOnFullShortage(env, eleInXML);

		}

		NodeList nlShipmentLineListIn = changeShipmentLines.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		log.verbose("ShipmentLineList: " + nlShipmentLineListIn.getLength());

		if (isAnyItemPicked || isPartaillyShorted) {
			log.verbose("invoke changeShipment call");
			eleChangeShipmentInput.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
			eleChangeShipmentInput.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
			eleChangeShipmentInput.setAttribute(AcademyConstants.ATTR_DOC_TYPE, strDocumentType);
			// changeShipmentInput.appendChild(changeShipmentLines);

			Element eleShipmentExtn = changeShipmentInput.createElement(AcademyConstants.ELE_EXTN);
			eleChangeShipmentInput.appendChild(eleShipmentExtn);
			eleShipmentExtn.setAttribute(AcademyConstants.ATTR_EXTN_SOP_MODIFYTS, AcademyUtil.getDate());

			if (isShorted) {
				eleChangeShipmentInput.setAttribute(AcademyConstants.ATTR_CANCEL_REMOVED_QTY, AcademyConstants.ATTR_Y);
				String strReasonCode = eleInXML.getAttribute(AcademyConstants.ATTR_EXTN_SHORTPICK_REASON_CODE);
				eleShipmentExtn.setAttribute(AcademyConstants.ATTR_EXTN_SHORTPICK_REASON_CODE, strReasonCode);
			}

			docChangeShipmentOut = AcademyUtil.invokeService(env,
					AcademyConstants.SERVICE_ACADEMY_STS_INVOKE_CHANGE_SHIPMENT_API, changeShipmentInput);
		}

		Document changeShipmentStatusOutput = null;
		if (isPickCompleted && !isPickLaterOptionSelected) {
			log.verbose("invoke changeShipment status call");
			changeShipmentStatusOutput = prepareAndInvokeChangeShipmentStatus(env, strShipmentKey);
		}

		if (!YFCObject.isVoid(changeShipmentStatusOutput)) {
			Element eleChangeShipmentStatusOutput = changeShipmentStatusOutput.getDocumentElement();
			String strStatus = eleChangeShipmentStatusOutput.getAttribute(AcademyConstants.ATTR_STATUS);
			log.verbose("Status: " + strStatus);

			if (strStatus.equals(AcademyConstants.VAL_READY_TO_SHIP_STATUS)) {
				Document docShipAckToExeterInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
				Element eleShipAckToExeterInput = docShipAckToExeterInput.getDocumentElement();
				eleShipAckToExeterInput.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
				log.verbose("setting the Transaction obj POST_ACK_MSG to blank");
				env.setTxnObject(AcademyConstants.STR_POST_ACK_MSG, AcademyConstants.STR_EMPTY_STRING);
				AcademyUtil.invokeService(env, AcademyConstants.SERVICE_ACADEMY_PREPARE_SHIP_ACK_MSG_TO_EXETER_FOR_SI,
						docShipAckToExeterInput);
				//OMNI-89521---START
				Element eleChangeShipmentOut = docChangeShipmentOut.getDocumentElement();
				String strPackListType = eleChangeShipmentOut.getAttribute(AcademyConstants.ATTR_PACKLIST_TYPE);
				if (!YFCObject.isVoid(strPackListType) && (strPackListType.equals(AcademyConstants.STS_FA))) {
					AcademyUtil.invokeService(env, "AcademyPublishCustomerInfoToGSM",docChangeShipmentOut);
				}
				//OMNI-89521---END
			  }

		}

		// OMNI-6450
		if (null != docChangeShipmentOut
				&& !StringUtil.isEmpty(containerScm)) {

			strPrintPackId = eleInXML.getAttribute(AcademyConstants.ATTR_PRINT_PACK_ID);
			
			Document docCommonCodeOut = getCommonCodeList(env, strPrintPackId);
			
			if(null != docCommonCodeOut) {
				
				NodeList nlCommonCode = docCommonCodeOut.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);
				
				for(int iCC=0; iCC < nlCommonCode.getLength(); iCC++) {
					
					Element eleCommonCode = (Element) nlCommonCode.item(iCC);
					
					String strCodeValue = eleCommonCode.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE);
					String strCodeShortDesc = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
					
					if(!StringUtil.isEmpty(strCodeValue)) {
						
						log.verbose("Code Value :: " + strCodeValue);
						
						if(strCodeValue.contains(AcademyConstants.STR_STS_SHIPPING_LABEL_PRINTER_NAME)) {
							env.setTxnObject(AcademyConstants.STR_STS_SHIPPING_LABEL_PRINTER_ID, strCodeShortDesc);
						}
						else if(strCodeValue.contains(AcademyConstants.STR_ORMD_LABEL_PRINTER_NAME)) {
							env.setTxnObject(AcademyConstants.STR_STS_ORMD_LABEL_PRINTER_ID, strCodeValue);
						}
					}
				}
			}
			
			log.verbose("Output - ChangeShipment API :: " + XMLUtil.getXMLString(docChangeShipmentOut));
			
			Element eleContainer = null;
			
			if(!StringUtil.isEmpty(containerScm)) {
				log.verbose("Container SCM :: " + containerScm);
				
				eleContainer = (Element) XPathUtil.getNode(docChangeShipmentOut.getDocumentElement(),
						"/Shipment/Containers/Container[@ContainerScm='" + containerScm + "']");

			}

			if (null != eleContainer) {

				log.verbose("Element - Container :: " + XMLUtil.getElementXMLString(eleContainer));

				String strShipmentContainerKey = eleContainer.getAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY);
				if (!StringUtil.isEmpty(strShipmentContainerKey)) {
					processContainerDetails(env, strShipmentContainerKey);
				}

			}

		}
		// OMNI-6450

	}

	// OMNI-6450

	/**
	 * This method will process the container details
	 * 
	 * @param Yantra Environment
	 * @param String ShipmentContainerKey
	 * @param String ShipNode
	 */
	public void processContainerDetails(YFSEnvironment env, String shipmentContainerKey) throws Exception {
		// Invoke the method to fetch the container details
		Document docGetShipmentContainerList = getContainerDetails(env, shipmentContainerKey);

		log.verbose("Container Details ::  " + XMLUtil.getXMLString(docGetShipmentContainerList));

		Document raiseEventInputDoc = prepareRaiseEventApiInp(docGetShipmentContainerList);

		// Invoke the raiseEvent API
		AcademyUtil.invokeAPI(env, AcademyConstants.API_ACADEMY_RAISE_EVENT, raiseEventInputDoc);
	}

	private Document prepareRaiseEventApiInp(Document docContainerDetails) throws Exception {

		Document docRaiseEvent = XMLUtil.createDocument(AcademyConstants.ELE_RAISE_EVENT);
		Element eleRaiseEvent = docRaiseEvent.getDocumentElement();
		eleRaiseEvent.setAttribute(AcademyConstants.ATTR_TRANS_ID, AcademyConstants.STR_TRAN_ID_ADD_TO_CONTAINER);
		eleRaiseEvent.setAttribute(AcademyConstants.ATTR_EVENT_ID, AcademyConstants.STR_EVENT_ID_CONTAINER_PACK);

		Element eleDataType = docRaiseEvent.createElement(AcademyConstants.ELE_DATA_TYPE);
		eleRaiseEvent.appendChild(eleDataType);
		eleDataType.setTextContent(AcademyConstants.STR_ONE);

		Element eleXmlData = docRaiseEvent.createElement(AcademyConstants.ELE_XML_DATA);
		eleRaiseEvent.appendChild(eleXmlData);
		eleXmlData.setTextContent(XMLUtil.getXMLString(docContainerDetails));

		log.verbose("Input - raiseEvent API :: " + XMLUtil.getXMLString(docRaiseEvent));

		return docRaiseEvent;
	}

	/**
	 * This method will invoke getShipmentContainerList
	 * 
	 * @param Yantra               Environment
	 * @param ShipmentContainerKey
	 * @return output xml of getShipmentContainerDetails API
	 */

	public Document getContainerDetails(YFSEnvironment env, String shipmentContainerKey) throws Exception {

		// Set the template for getShipmentContainerDetails API
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_CONTAINER_LIST,
				AcademyConstants.STR_TEMPLATEFILE_GET_SHIPMENT_CONTAINER_LIST);

		// Create the root element Container
		Document docGetShipmentContaierListInp = XMLUtil.createDocument(AcademyConstants.ELE_CONTAINER);
		Element eleGetShipmentContainerListInp = docGetShipmentContaierListInp.getDocumentElement();

		// Set the attribute ShipmentContainerKey
		eleGetShipmentContainerListInp.setAttribute(AcademyConstants.ATTR_SHIP_CONT_KEY, shipmentContainerKey);

		log.verbose("Input - getShipmentContainerList API :: " + XMLUtil.getXMLString(docGetShipmentContaierListInp));

		// Invoke getShipmentContainerDetails API and return the output document
		Document docGetShipmentContainerListOut = AcademyUtil.invokeAPI(env,
				AcademyConstants.API_GET_SHIPMENT_CONTAINER_LIST, docGetShipmentContaierListInp);

		Element eleContainer = (Element) docGetShipmentContainerListOut
				.getElementsByTagName(AcademyConstants.ELE_CONTAINER).item(0);

		return XMLUtil.getDocumentForElement(eleContainer);
	}
	// OMNI-6450

	/**
	 * This method will invoke generateSCM API
	 * 
	 * @param env,
	 * @return String SCM
	 */
	public String getContainerScm(YFSEnvironment env) throws Exception {
		// Create element generateSCM
		Document generateSCMInput = XMLUtil.createDocument("generateSCM");
		Element generateSCMInputRoot = generateSCMInput.getDocumentElement();
		// Set attributes
		generateSCMInputRoot.setAttribute(AcademyConstants.ATTR_CONATINER_TYPE, AcademyConstants.STR_CASE);
		generateSCMInputRoot.setAttribute(AcademyConstants.ATTR_NUM_SCMS_REQUESTED, "1");
		// Invoke generateSCM API
		Document generateSCMOutput = api.generateSCM(env, generateSCMInput);
		// Return attribute SCM
		return ((Element) generateSCMOutput.getElementsByTagName(AcademyConstants.STR_SCM).item(0))
				.getAttribute(AcademyConstants.STR_SCM);
	}

	/**
	 * This method will set the container volume details
	 * 
	 * @param env, inXML, element container
	 * @return element PrimaryInformation
	 */
	public Element setContainerVolumeDetails(YFSEnvironment env, Element inXMLRoot, Element container)
			throws Exception {
		// Fetch the attribute value of ContainerType
		if (log.isVerboseEnabled()) {
			log.verbose(
					"Inside setContainerVolumeDetails with Container Element" + XMLUtil.getElementXMLString(container));
		}
		String containerItemID = inXMLRoot.getAttribute(AcademyConstants.ATTR_CONATINER_TYPE).trim();
		// Fetch the attribute value of ContainerTypeKey
		String containerItemKey = inXMLRoot.getAttribute(AcademyConstants.ATTR_CONTAINER_TYPE_KEY).trim();
		// Creat root element Item
		Document docItemListInput = XMLUtil.createDocument(AcademyConstants.ITEM);
		Element docItemListInputRoot = docItemListInput.getDocumentElement();
		// Set the MaximumRecords
		docItemListInputRoot.setAttribute(AcademyConstants.ATTR_MAX_RECORD, "1");
		// check for empty value
		if (!containerItemKey.equals("")) {
			// set attribute ItemKey
			docItemListInputRoot.setAttribute(AcademyConstants.ATTR_ITEM_KEY, containerItemKey);
		} else {
			// Set the ItemID
			docItemListInputRoot.setAttribute(AcademyConstants.ITEM_ID, containerItemID);
		}
		// Invoke service
		Document docItemListOutput = AcademyUtil.invokeService(env,
				AcademyConstants.SERVICE_ACADEMY_SFS_GET_CONTAINER_VOLUME_DETAILS, docItemListInput);
		if (log.isVerboseEnabled()) {
			log.verbose("Output of AcademySFSGetContainerVolumeDetails" + XMLUtil.getXMLString(docItemListOutput));
		}
		// Fetch the element Item
		Element containerVolumeItem = (Element) docItemListOutput.getElementsByTagName(AcademyConstants.ITEM).item(0);
		// Fetch the element PrimaryInformation
		Element containerVolumePrimaryInformation = (Element) docItemListOutput
				.getElementsByTagName(AcademyConstants.ELE_PRIMARY_INFO).item(0);
		// Set attribute values
		container.setAttribute(AcademyConstants.ATTR_CORRUGATION_ITEM_KEY,
				containerVolumeItem.getAttribute(AcademyConstants.ATTR_ITEM_KEY));
		container.setAttribute(AcademyConstants.ATTR_CONTAINER_HEIGHT,
				containerVolumePrimaryInformation.getAttribute(AcademyConstants.ATTR_UNIT_HEIGHT));
		container.setAttribute(AcademyConstants.ATTR_CONTAINER_HEIGHT_UOM,
				containerVolumePrimaryInformation.getAttribute(AcademyConstants.ATTR_UNIT_HEIGHT_UOM));
		container.setAttribute(AcademyConstants.ATTR_CONTAINER_LENGTH,
				containerVolumePrimaryInformation.getAttribute(AcademyConstants.ATTR_UNIT_LENGTH));
		container.setAttribute(AcademyConstants.ATTR_CONTAINER_LENGTH_UOM,
				containerVolumePrimaryInformation.getAttribute(AcademyConstants.ATTR_UNIT_LENGTH_UOM));
		container.setAttribute(AcademyConstants.ATTR_CONTAINER_WIDTH,
				containerVolumePrimaryInformation.getAttribute(AcademyConstants.ATTR_UNIT_WIDTH));
		container.setAttribute(AcademyConstants.ATTR_CONTAINER_WIDTH_UOM,
				containerVolumePrimaryInformation.getAttribute(AcademyConstants.ATTR_UNIT_WIDTH_UOM));
		container.setAttribute(AcademyConstants.ATTR_CONTAINER_GROSS_WEIGHT_UOM,
				containerVolumePrimaryInformation.getAttribute(AcademyConstants.ATTR_UNIT_WEIGHT_UOM));
		container.setAttribute(AcademyConstants.ATTR_CONTAINER_NET_WEIGHT_UOM,
				containerVolumePrimaryInformation.getAttribute(AcademyConstants.ATTR_UNIT_WEIGHT_UOM));
		container.setAttribute(AcademyConstants.ATTR_ACTUAL_WEIGHT,
				containerVolumePrimaryInformation.getAttribute(AcademyConstants.ATTR_ACTUAL_WEIGHT));
		container.setAttribute(AcademyConstants.ATTR_ACTUAL_WEIGHT_UOM,
				containerVolumePrimaryInformation.getAttribute(AcademyConstants.ATTR_ACTUAL_WEIGHT_UOM));

		// Return element PrimaryInformation
		return containerVolumePrimaryInformation;
	}

	/**
	 * Fetch the NetWeigt of the item
	 * 
	 * @param env
	 * @hashmap of itemQuanty
	 * @return netweight
	 */
	public double getNetWeightOfItems(YFSEnvironment env, HashMap<String, Double> itemQuantityMap) throws Exception {
		log.verbose("****The input itemList to getNetWeightOfItems****");
		int size = itemQuantityMap.size();
		log.verbose("Size of the itemQuantityMap is " + size);
		double netWeight = 0;
		Document getItemListOutputDoc = null;
		int itemCount = 1;
		String itemId = "";
		// Iterate through the hashmap entry
		Iterator iterator = itemQuantityMap.keySet().iterator();
		while (iterator.hasNext()) {
			log.verbose("Iterating through items in iemQuantityMap");
			itemId = (String) iterator.next();
			System.out.println("Item id in the map is" + itemId);
			break;
		}
		// creating the input(complex query) for getItemList API
		// Create root node Item
		Document getItemListInputDoc = YFCDocument.createDocument(AcademyConstants.ITEM).getDocument();
		Element eleRootElement = getItemListInputDoc.getDocumentElement();
		// setting the values
		eleRootElement.setAttribute(AcademyConstants.ORGANIZATION_CODE, AcademyConstants.HUB_CODE);
		eleRootElement.setAttribute(AcademyConstants.ATTR_UOM, AcademyConstants.UNIT_OF_MEASURE);
		eleRootElement.setAttribute(AcademyConstants.ATTR_ITEM_ID, itemId);
		// Create element ComplexQuery
		Element eleComplexQuery = getItemListInputDoc.createElement(AcademyConstants.COMPLEX_QRY_ELEMENT);
		// setting the value
		eleComplexQuery.setAttribute(AcademyConstants.COMPLEX_OPERATOR_ATTR, AcademyConstants.ELE_OR);
		Element eleAnd = getItemListInputDoc.createElement(AcademyConstants.COMPLEX_AND_ELEMENT);
		eleComplexQuery.appendChild(eleAnd);
		Element eleOr = getItemListInputDoc.createElement(AcademyConstants.COMPLEX_OR_ELEMENT);
		eleAnd.appendChild(eleOr);
		// Loop till the record is present in the hashmap
		while (iterator.hasNext()) {
			// Increment the ItemCount
			itemCount++;
			// Fetch the itemId
			itemId = (String) iterator.next();
			// Create element Exp
			Element eleExp = getItemListInputDoc.createElement(AcademyConstants.COMPLEX_EXP_ELEMENT);
			// Setting the attribute
			eleExp.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.ITEM_Id);
			eleExp.setAttribute(AcademyConstants.ATTR_VALUE, itemId);
			eleOr.appendChild(eleExp);
		}
		if (itemCount == 1) {
			log.verbose("Dint go inside while loop");
		}

		// If Item count is greater than 1
		if (itemCount > 1) {
			System.out.println("**ItemCount greater than 1:Item count is" + itemCount);
			// Append the element ComplexQuery to root node
			eleRootElement.appendChild(eleComplexQuery);
		}
		// Print the verbose log
		if (log.isVerboseEnabled()) {
			log.verbose("getItemlist input :" + XMLUtil.getXMLString(getItemListInputDoc));
		}
		// Invoke the service
		Document docItemListOutput = AcademyUtil.invokeService(env,
				AcademyConstants.SERVICE_ACADEMY_SFS_GET_CONTAINER_VOLUME_DETAILS, getItemListInputDoc);
		// Fetch the NodeList of Node PrimaryInformation
		NodeList containerVolumePrimaryInformationList = docItemListOutput
				.getElementsByTagName(AcademyConstants.ELE_PRIMARY_INFO);
		// Loop through the NodeList record
		for (int i = 0; i < containerVolumePrimaryInformationList.getLength(); i++) {
			// Fetch the element
			Element containerVolumePrimaryInformation = (Element) containerVolumePrimaryInformationList.item(i);
			Element containerVolumeItem = (Element) containerVolumePrimaryInformation.getParentNode();
			// Fetch the element ItemId
			itemId = containerVolumeItem.getAttribute(AcademyConstants.ATTR_ITEM_ID);
			// Fetch the attribute UnitWeight
			String itemWeight = containerVolumePrimaryInformation.getAttribute(AcademyConstants.ELE_UNIT_WEIGHT).trim();
			// Check if the va;ue is not empty
			if (!itemWeight.equals("") && itemQuantityMap.containsKey(itemId)) {
				// Calculate the netWeight
				netWeight += itemQuantityMap.get(itemId) * Double.parseDouble(itemWeight);
			}
		}
		// return thr netWeight value
		log.verbose("net weight from  the method is " + netWeight);
		return netWeight;
	}

	/**
	 * This method will set the container volume details to the item details in case
	 * when the conatiner type = "VendorPackage"
	 * 
	 * @param env, inXML, element container
	 * @return element PrimaryInformation
	 */
	public Element setContainerVolumeDetailsVndrPkg(YFSEnvironment env, Element inXMLRoot, Element container)
			throws Exception {

		// Start - Changes made for PERF-395 to handle null pointer exception in a one
		// of scenario.
		if (log.isVerboseEnabled()) {
			log.verbose("setContainerVolumeDetailsVndrPkg: inXMLRoot Elem: " + XMLUtil.getElementXMLString(inXMLRoot));
		}

		// Fetch the element Item
		NodeList itemList = inXMLRoot.getElementsByTagName(AcademyConstants.ELEM_ITEM);
		if (itemList != null && itemList.getLength() > 0) {
			Element containerVolumeItem = (Element) itemList.item(0);
			// Set attribute values
			container.setAttribute(AcademyConstants.ATTR_CORRUGATION_ITEM_KEY,
					containerVolumeItem.getAttribute(AcademyConstants.ATTR_ITEM_TYPE));

			// Fetch the element PrimaryInformation
			NodeList primaryInfoList = containerVolumeItem.getElementsByTagName(AcademyConstants.ELE_PRIMARY_INFO);
			if (primaryInfoList != null && primaryInfoList.getLength() > 0) {
				Element containerVolumePrimaryInformation = (Element) primaryInfoList.item(0);

				String unitHeight = containerVolumePrimaryInformation.getAttribute(AcademyConstants.ATTR_UNIT_HEIGHT);
				if (!YFCObject.isVoid(unitHeight)) {
					container.setAttribute(AcademyConstants.ATTR_CONTAINER_HEIGHT, unitHeight);
				}

				String unitHeightUOM = containerVolumePrimaryInformation
						.getAttribute(AcademyConstants.ATTR_UNIT_HEIGHT_UOM);
				if (!YFCObject.isVoid(unitHeightUOM)) {
					container.setAttribute(AcademyConstants.ATTR_CONTAINER_HEIGHT_UOM, unitHeightUOM);
				}

				String unitLength = containerVolumePrimaryInformation.getAttribute(AcademyConstants.ATTR_UNIT_LENGTH);
				if (!YFCObject.isVoid(unitLength)) {
					container.setAttribute(AcademyConstants.ATTR_CONTAINER_LENGTH, unitLength);
				}

				String unitLengthUOM = containerVolumePrimaryInformation
						.getAttribute(AcademyConstants.ATTR_UNIT_LENGTH_UOM);
				if (!YFCObject.isVoid(unitLengthUOM)) {
					container.setAttribute(AcademyConstants.ATTR_CONTAINER_LENGTH_UOM, unitLengthUOM);
				}

				String unitWidth = containerVolumePrimaryInformation.getAttribute(AcademyConstants.ATTR_UNIT_WIDTH);
				if (!YFCObject.isVoid(unitWidth)) {
					container.setAttribute(AcademyConstants.ATTR_CONTAINER_WIDTH, unitWidth);
				}

				String unitWidthUOM = containerVolumePrimaryInformation
						.getAttribute(AcademyConstants.ATTR_UNIT_WIDTH_UOM);
				if (!YFCObject.isVoid(unitWidthUOM)) {
					container.setAttribute(AcademyConstants.ATTR_CONTAINER_WIDTH_UOM, unitWidthUOM);
				}

				String unitWeight = containerVolumePrimaryInformation.getAttribute(AcademyConstants.ATTR_UINT_WEIGHT);
				String actualWeight = containerVolumePrimaryInformation.getAttribute(AcademyConstants.ATTR_UINT_WEIGHT);

				if (!YFCObject.isVoid(unitWeight)) {
					container.setAttribute(AcademyConstants.ATTR_CONTAINER_GROSS_WEIGHT, unitWeight);
					container.setAttribute(AcademyConstants.ATTR_CONTAINER_NET_WEIGHT, unitWeight);

				}

				String unitWeightUOM = containerVolumePrimaryInformation
						.getAttribute(AcademyConstants.ATTR_UNIT_WEIGHT_UOM);
				if (!YFCObject.isVoid(unitWeightUOM)) {
					container.setAttribute(AcademyConstants.ATTR_CONTAINER_GROSS_WEIGHT_UOM, unitWeightUOM);
					container.setAttribute(AcademyConstants.ATTR_CONTAINER_NET_WEIGHT_UOM, unitWeightUOM);
					container.setAttribute(AcademyConstants.ATTR_ACTUAL_WEIGHT_UOM, unitWeightUOM);

				}
			} else {
				// Throw custom exception as primary information of an item element cannot be
				// missing.
				YFSException e = new YFSException();
				e.setErrorCode("EXTN_ACAD004");
				e.setErrorDescription("System Error. Unable to fetch primary information for the item.");
				throw e;
			}
		}
		if (log.isVerboseEnabled()) {
			log.verbose("setContainerVolumeDetailsVndrPkg: container Elem: " + XMLUtil.getElementXMLString(container));
		}

		return container;

		// End - Changes made for PERF-395 to handle null pointer exception in a one of
		// scenario.
	}

	public Document prepareAndInvokeChangeShipmentStatus(YFSEnvironment env, String strShipmentKey) {
		Document docChangeShipmentStatusOut = null;
		try {
			Document docChangeShipmentStatusInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			Element eleChangeShipmentStatusInput = docChangeShipmentStatusInput.getDocumentElement();
			eleChangeShipmentStatusInput.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
			eleChangeShipmentStatusInput.setAttribute(AcademyConstants.ATTR_BASEDROP_STATUS,
					AcademyConstants.VAL_READY_TO_SHIP_STATUS);
			eleChangeShipmentStatusInput.setAttribute(AcademyConstants.ATTR_TRANSID,
					AcademyConstants.STR_SOP_CHECK_BACKROOM_PICK_0006_TRAN);

			docChangeShipmentStatusOut = AcademyUtil.invokeService(env,
					AcademyConstants.SERVICE_ACADEMY_STS_INVOKE_CHANGE_SHIPMENT_STATUS_API,
					docChangeShipmentStatusInput);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return docChangeShipmentStatusOut;
	}

	public void cancelShipmentOnFullShortage(YFSEnvironment env, Element eleShipment) {

		try {
			Document docChangeShipmentInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			Element eleChangeShipmentInput = docChangeShipmentInput.getDocumentElement();
			eleChangeShipmentInput.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.VAL_CANCEL);
			eleChangeShipmentInput.setAttribute(AcademyConstants.ATTR_CANCEL_REMOVED_QTY, AcademyConstants.ATTR_Y);
			String strDocumentType = eleShipment.getAttribute(AcademyConstants.ATTR_DOC_TYPE);
			String strShipmentNo = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
			String strShipmentKey = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			String strReasonCode = eleShipment.getAttribute(AcademyConstants.ATTR_EXTN_SHORTPICK_REASON_CODE);
			Element eleExtn = docChangeShipmentInput.createElement(AcademyConstants.ELE_EXTN);
			eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_SHORTPICK_REASON_CODE, strReasonCode);
			eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_SOP_MODIFYTS, AcademyUtil.getDate());
			eleChangeShipmentInput.appendChild(eleExtn);
			eleChangeShipmentInput.setAttribute(AcademyConstants.ATTR_DOC_TYPE, strDocumentType);
			eleChangeShipmentInput.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
			eleChangeShipmentInput.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);

			log.verbose("Invoke changeShipment call on Full Shortage");
			Document ChangeShipmentOut = AcademyUtil.invokeService(env,
					AcademyConstants.SERVICE_ACADEMY_STS_INVOKE_CHANGE_SHIPMENT_API, docChangeShipmentInput);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// OMNI-6615 : Begin
	/**
	 * This method will be used to fetch sequence and form container number
	 * 
	 * @param env,
	 * @param strShipNode
	 * @return String ContainerNO
	 */
	public String getTOContainerNo(YFSEnvironment env, String strShipNode, String strReceivingNode) throws Exception {
		String strContainerNo = "";
		String strContainerSeq = "";
		YFSContext objContext;

		try {
			log.verbose(
					"Start getTOContainerNo() :: ShipNode-" + strShipNode + " :: ReceivingNode-" + strReceivingNode);

			objContext = (YFSContext) env;
			long lSeqSTSContainerNo = objContext
					.getNextDBSeqNo(AcademyConstants.STR_ACAD_STS_CONTAINER_NO + strShipNode);
			strContainerSeq = String.valueOf(lSeqSTSContainerNo);
			log.verbose("STS Container Seq from DB :: " + strContainerSeq);
			strContainerNo = strReceivingNode.concat(strContainerSeq);
			log.verbose("Container No - " + strContainerNo + " for ShipNode - " + strShipNode);

		} catch (Exception e) {

			log.info("Exception: getTOContainerNo() :: " + e.getMessage());
			YFSException yfse = new YFSException();
			yfse.setErrorCode(AcademyConstants.ERR_CODE_STS_SEQ_GEN);
			yfse.setErrorDescription(e.getMessage());
			throw yfse;
		}
		return strContainerNo;
	}
	// OMNI-6615 : End

	// OMNI-7980
	private Document getCommonCodeList(YFSEnvironment env, String strCodeType) {

		Document getCommonCodeListInDoc = null;
		Document getCommonCodeListOutDoc = null;

		try {
			getCommonCodeListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
			getCommonCodeListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE,
					strCodeType);
			getCommonCodeListInDoc.getDocumentElement().setAttribute(AcademyConstants.ORGANIZATION_CODE,
					AcademyConstants.HUB_CODE);

			log.verbose("getCommonCodeList input Doc: " + XMLUtil.getXMLString(getCommonCodeListInDoc));

			env.setApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST,
					AcademyConstants.STR_TEMPLATEFILE_GET_COMMON_CODE_LIST_PACK_STATION_DEVICE);

			getCommonCodeListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMONCODE_LIST,
					getCommonCodeListInDoc);

			env.clearApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST);

			log.verbose("getCommonCodeList API Output Doc: " + XMLUtil.getXMLString(getCommonCodeListOutDoc));

		} catch (Exception e) {
			log.error("Exception - getCommonCodeList()::" + e);
		}

		return getCommonCodeListOutDoc;
	}
	// OMNI-7980

}
