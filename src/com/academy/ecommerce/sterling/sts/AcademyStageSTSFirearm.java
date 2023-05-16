package com.academy.ecommerce.sterling.sts;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @Author Everest
 * @JIRA# OMNI-90027 - STS Firearm - Acquisition Staging
 * 
 * @Purpose
 * As part of STS 1.0 FireArm orders, when the order moved to Included in Shipment the staging process is done by the following class. 
 * On the on success event of consolidate to shipment transaction, a service AcademyStageSTSFirearm will be invoked which calls this 
 * class logic. This will update the Staging location to container and shipment line along with BackroomPickComplete and BackroomPickQty 
 * values via changeShipment API. Finally moves the status to RFCP via changeShipmentStatus API.
 **/

public class AcademyStageSTSFirearm {
	public static final YFCLogCategory log = YFCLogCategory.instance(AcademyStageSTSFirearm.class);

	private Properties props;
	public void setProperties(Properties props) {
		this.props = props;
	}
	
	public void stageSTSFirearm(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer(this.getClass() + ".stageSTSFirearm");
		log.verbose("Entering AcademyStageSTSFirearm.stageSTSFirearm() :: " + XMLUtil.getXMLString(inDoc));
		Element eleInput = null;
		String strStagingLocation = props.getProperty(AcademyConstants.ATTR_STAGING_LOCATION);
		eleInput = inDoc.getDocumentElement();

		log.verbose("Invoking APIs for updating staging location");
		updateShipmentLineInfo(env, eleInput,strStagingLocation);
		//OMNI-93544 - Start
		if (!AcademyConstants.STR_SPECIAL_ORDER_FIREARMS.equalsIgnoreCase(
				eleInput.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE))) {
			updateStagingLocForContainer(env, eleInput,strStagingLocation);
		}
		invokeChangeShipmentStatus(env, inDoc);
		//OMNI-93544 - End

		log.endTimer(this.getClass() + ".stageSTSFirearm");
	}

	/**
	 * Prepares and invoke changeShipment API to update ExtnStagingLocation and BackroomPick attributes against shipmentLines
	 * @param env
	 * @param eleInput
	 * @param strStagingLocation 
	 * @return void
	 * @throws Exception
	 */
	private void updateShipmentLineInfo(YFSEnvironment env, Element eleInput, String strStagingLocation) throws Exception {
		log.beginTimer(this.getClass() + ".updateShipmentLineInfo");
		
		String shipLineQty = "";
		Document docChangeShipmentIn = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleChangeShipment = docChangeShipmentIn.getDocumentElement();
		eleChangeShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,
				eleInput.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
		Element eleShipmentLines = docChangeShipmentIn.createElement(AcademyConstants.ELE_SHIPMENT_LINES);
		XMLUtil.appendChild(docChangeShipmentIn.getDocumentElement(), eleShipmentLines);
		NodeList nlShipmentLine = eleInput.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		int iShipLine = 0;
		iShipLine = nlShipmentLine.getLength();

		for (int i = 0; i < iShipLine; i++) {
			Element eleShipmentLineIn = (Element) nlShipmentLine.item(i);
			log.verbose("Each ShipmentLine::" + XMLUtil.getElementXMLString(eleShipmentLineIn));
			Element eleShipmentLine = docChangeShipmentIn.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
			eleShipmentLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY,
					eleShipmentLineIn.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY));
			eleShipmentLine.setAttribute(AcademyConstants.ATTR_BACKROOM_PICK_COMPLETE, AcademyConstants.ATTR_Y);
			eleShipmentLine.setAttribute(AcademyConstants.ATTR_BACKROOM_PICKED_QTY,
					eleShipmentLineIn.getAttribute(AcademyConstants.ATTR_QUANTITY));
			eleShipmentLine.setAttribute(AcademyConstants.ATTR_QUANTITY,
					eleShipmentLineIn.getAttribute(AcademyConstants.ATTR_QUANTITY));
			XMLUtil.appendChild(eleShipmentLines, eleShipmentLine);
			Element eleExtnShipmentLine = docChangeShipmentIn.createElement(AcademyConstants.ELE_EXTN);
			//OMNI-93544 - Start
			if (AcademyConstants.STR_SPECIAL_ORDER_FIREARMS.equalsIgnoreCase(
					eleInput.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE))) {
				shipLineQty = eleShipmentLineIn.getAttribute(AcademyConstants.ATTR_QUANTITY);
				eleExtnShipmentLine.setAttribute(AcademyConstants.ATTR_EXTN_SOF_RDYFORCUSTPICK_QTY, 
						String.valueOf(Double.parseDouble(shipLineQty)));
			} 
			//OMNI-95365 - Updating Staging location for DSV SOF along with STS 1.0
			eleExtnShipmentLine.setAttribute(AcademyConstants.ATTR_EXTN_STAGING_LOCATION, strStagingLocation);
			//OMNI-93544 - End
			XMLUtil.appendChild(eleShipmentLine, eleExtnShipmentLine);
		}
		log.verbose("Input to changeShipment :: updateShipmentLineInfo"
				+ XMLUtil.getXMLString(docChangeShipmentIn));
		Document docChangeShipForContainerOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT,
				docChangeShipmentIn);
		log.verbose("changeShipment output doc" + XMLUtil.getXMLString(docChangeShipForContainerOut));
		log.endTimer(this.getClass() + ".updateShipmentLineInfo");
	}

	/**
	 * Prepares and invoke changeShipment API to update StagingLocation value in Zone attribute against Containers
	 * @param env
	 * @param eleInput
	 * @param strStagingLocation2 
	 * @return docChangeShipForContainerOut
	 * @throws Exception
	 */
	private Document updateStagingLocForContainer(YFSEnvironment env, Element eleInput, String strStagingLoc) throws Exception {
		log.beginTimer(this.getClass() + ".updateStagingLocForContainer");
		Document docGetShipmentListOut = invokeGetShipmentListForTO(env, eleInput);
		String strShipmentContainerKey = "";
		Element eleShipment = (Element) docGetShipmentListOut.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);		
		NodeList nlContainers = eleShipment.getElementsByTagName(AcademyConstants.ELE_CONTAINER);
		Document docChangeShipForContStagingLoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleChangeShipForContainer = docChangeShipForContStagingLoc.getDocumentElement();
		eleChangeShipForContainer.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,
				eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));

		if (eleShipment.getAttribute(AcademyConstants.STR_DOCUMENT_TYPE).equalsIgnoreCase(AcademyConstants.DOCUMENT_TYPE_PO)) {
			eleChangeShipForContainer.setAttribute(AcademyConstants.ATTR_OVERRIDE_MODIFICATION_RULES, AcademyConstants.STR_YES);
		}
		Element eleContainers = docChangeShipForContStagingLoc.createElement(AcademyConstants.ELE_CONTAINERS);
		XMLUtil.appendChild(docChangeShipForContStagingLoc.getDocumentElement(), eleContainers);

		for (int i = 0; i < nlContainers.getLength(); i++) {
			Element eleContainerFromList = (Element) nlContainers.item(i);
			strShipmentContainerKey = eleContainerFromList.getAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY);
			Element eleContainer = docChangeShipForContStagingLoc.createElement(AcademyConstants.ELE_CONTAINER);
			eleContainer.setAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY,strShipmentContainerKey);
			eleContainer.setAttribute(AcademyConstants.ATTR_IS_RECEIVED, AcademyConstants.ATTR_Y);
			eleContainer.setAttribute(AcademyConstants.ELE_ZONE,strStagingLoc);
			XMLUtil.appendChild(eleContainers, eleContainer);
		}
		
		log.verbose("Input to changeShipment :: updateStagingLocForContainer "
				+ XMLUtil.getXMLString(docChangeShipForContStagingLoc));
		Document docChangeShipForContainerOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT,
				docChangeShipForContStagingLoc);
		log.verbose("changeShipment is complete" + XMLUtil.getXMLString(docChangeShipForContainerOut));
		log.endTimer(this.getClass() + ".updateStagingLocForContainer");

		return docChangeShipForContainerOut;
	}

	/**
	 * Prepares and invoke getShipmentList API to fetch ShipmentContainerKey of associated Transfer Order with 
	 * Sales Order details.
	 * @param env
	 * @param eleInput
	 * @return docGetShipmentListOut
	 * @throws Exception
	 */
	private Document invokeGetShipmentListForTO(YFSEnvironment env, Element eleInput) throws Exception {
		Document docGetShipmentListOut = null;
		String strOrderHeaderKey = eleInput.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
		String strShipmentType = eleInput.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE);
		Document docGetShipmentListIn = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleShipment = docGetShipmentListIn.getDocumentElement();
		//OMNI-93544 - Start
		if (AcademyConstants.STR_SHIP_TO_STORE.equals(strShipmentType)) {
			eleShipment.setAttribute(AcademyConstants.STR_DOCUMENT_TYPE, AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE);
			eleShipment.setAttribute(AcademyConstants.ATTR_PACKLIST_TYPE, AcademyConstants.STS_FA);
		} else if (AcademyConstants.STR_SPECIAL_ORDER_FIREARMS.equals(strShipmentType)) {
			eleShipment.setAttribute(AcademyConstants.STR_DOCUMENT_TYPE, AcademyConstants.DOCUMENT_TYPE_PO);
		}
		//OMNI-93544 - End
		Element eleShipmentLines = docGetShipmentListIn.createElement(AcademyConstants.ELE_SHIPMENT_LINES);
		XMLUtil.appendChild(docGetShipmentListIn.getDocumentElement(), eleShipmentLines);
		
		Element eleShipmentLine = docGetShipmentListIn.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
		XMLUtil.appendChild(eleShipmentLines, eleShipmentLine);
		Element eleOrderLine = docGetShipmentListIn.createElement(AcademyConstants.ELE_ORDER_LINE);
		eleOrderLine.setAttribute(AcademyConstants.ATTR_CHAINED_FROM_ORDER_HEADER_KEY, strOrderHeaderKey);
		XMLUtil.appendChild(eleShipmentLine, eleOrderLine);
		Document docGetShipmentListOutputTemplate = XMLUtil.getDocument("<Shipments>\r\n"
				+ "	<Shipment DocumentType='' ShipmentKey=''>\r\n"
				+ "	<Containers>\r\n"
				+ "        <Container ContainerNo='' ShipmentContainerKey='' IsReceived=''/>\r\n"
				+ "    </Containers>\r\n"
				+ "	</Shipment>\r\n"
				+ "</Shipments>");
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, docGetShipmentListOutputTemplate);
		log.verbose("Input to getShipmentList :: invokeGetShipmentListForTO" + XMLUtil.getXMLString(docGetShipmentListIn));
		docGetShipmentListOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST, docGetShipmentListIn);
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
		log.verbose("getShipmentList output doc" + XMLUtil.getXMLString(docGetShipmentListOut));
		
		return docGetShipmentListOut;
	}

	/**
	 * Prepares and invoke changeShipmentStatus API to move the order to RFCP status.
	 * @param env
	 * @param eleInput
	 * @return docChangeShipStatusOut
	 * @throws Exception
	 */
	public Document invokeChangeShipmentStatus(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer(this.getClass() + ".invokeChangeShipmentStatus");

		Document docChangeShipStatus = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleChangeShipStatus = docChangeShipStatus.getDocumentElement();
		eleChangeShipStatus.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,
				inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
		eleChangeShipStatus.setAttribute(AcademyConstants.ATTR_BASEDROP_STATUS,
				AcademyConstants.VAL_READY_TO_SHIP_STATUS);
		eleChangeShipStatus.setAttribute(AcademyConstants.ATTR_TRANS_ID, AcademyConstants.TRAN_YCD_BACKROOM_PICK);
		
		log.verbose("Input to changeShipmentStatus : " + XMLUtil.getXMLString(docChangeShipStatus));
		Document docChangeShipStatusOut = AcademyUtil.invokeAPI(env, AcademyConstants.CHANGE_SHIPMENT_STATUS_API,
				docChangeShipStatus);
		log.verbose("The changeShipmentStatus is complete" + XMLUtil.getXMLString(docChangeShipStatusOut));
		log.endTimer(this.getClass() + ".invokeChangeShipmentStatus");

		return docChangeShipStatusOut;
	}

}