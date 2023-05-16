package com.academy.ecommerce.sterling.bopis.sfspacking.api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * Input to the class
 * 
 * <Shipment ActualFreightCharge="" ContainerizedQuantity=""
 * DisplayLocalizedFieldInLocale="" IsPackProcessComplete="Y" ShipmentKey=""/>
 * 
 * @author Neeti
 *
 */

public class AcademyChangeShipmentOnFinishPack {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyChangeShipmentOnFinishPack.class);

	public Document changeShipmentOnFinishPack(YFSEnvironment env, Document inXML) {

		log.beginTimer("AcademyChangeShipmentOnFinishPack::changeShipmentOnFinishPack ");
		log.verbose("Entering the method AcademyChangeShipmentOnFinishPack.changeShipmentOnFinishPack");

		try {
			String strShipmentKey = null;
			String strShipNode = null;
			String strScacIntegrationRequired = null;
			String strActualFreightCharge = null;
			Double dActualFreightChargeAtShipment = 0.0D;
			Double freightChargeOfAllContainers = 0.0D;
			Document OutDocgetShipmentDetails = null;
			Document OutDocgetShipmentContainerList = null;

			double dshipmentContainerizedQuantity = 0.0D;
			Document OutDocgetShipmentLineList = null;
			
			String strDocType = null;
			strShipmentKey = inXML.getDocumentElement().getAttribute(AcademyConstants.SHIPMENT_KEY);

			if (!YFCObject.isVoid(strShipmentKey)) {
				log.verbose("ShipmentKey prsent in the input= " + strShipmentKey);
				OutDocgetShipmentDetails = callgetShipmentDetailsAPI(env, strShipmentKey);
				strDocType = OutDocgetShipmentDetails.getDocumentElement()
						.getAttribute(AcademyConstants.ATTR_DOC_TYPE);
				strShipNode = OutDocgetShipmentDetails.getDocumentElement()
						.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
				strScacIntegrationRequired = OutDocgetShipmentDetails.getDocumentElement()
						.getAttribute(AcademyConstants.ATTR_SCAC_INTEGRATION_REQUIRED);
				if (!YFCObject.isVoid(strScacIntegrationRequired)
						&& strScacIntegrationRequired.equalsIgnoreCase(AcademyConstants.STR_YES)) {
					strActualFreightCharge = OutDocgetShipmentDetails.getDocumentElement()
							.getAttribute(AcademyConstants.ATTR_ACTUAL_FREIGHT_CHARGE);
					if (!YFCObject.isVoid(strActualFreightCharge)) {
						dActualFreightChargeAtShipment = SCXmlUtil.getDoubleAttribute(
								OutDocgetShipmentDetails.getDocumentElement(),
								AcademyConstants.ATTR_ACTUAL_FREIGHT_CHARGE);
						if (dActualFreightChargeAtShipment < 0.0D)
							dActualFreightChargeAtShipment = 0.0D;
						OutDocgetShipmentContainerList = callgetShipmentContainerList(env, strShipmentKey);
						NodeList nlContainer = OutDocgetShipmentContainerList
								.getElementsByTagName(AcademyConstants.ELE_CONTAINER);
						for (int i = 0; i < nlContainer.getLength(); i++) {
							Element EleContainer = (Element) nlContainer.item(i);
							double freightChargeAtContainer = SCXmlUtil.getDoubleAttribute(EleContainer,
									AcademyConstants.ATTR_ACTUAL_FREIGHT_CHARGE);
							freightChargeOfAllContainers += freightChargeAtContainer;
						}

						dActualFreightChargeAtShipment += freightChargeOfAllContainers;

						SCXmlUtil.setAttribute(inXML.getDocumentElement(), AcademyConstants.ATTR_ACTUAL_FREIGHT_CHARGE,
								Double.toString(dActualFreightChargeAtShipment));

					}

				} else {
					log.verbose("ScacIntegrationRequired  is null or N");
				}

				OutDocgetShipmentLineList = callgetShipmentLineList(env, strShipmentKey);
				NodeList nlShipmentLine = OutDocgetShipmentLineList
						.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
				for (int j = 0; j < nlShipmentLine.getLength(); j++) {
					Element eleShipmentLine = (Element) nlShipmentLine.item(j);

					double dplacedQuantity = SCXmlUtil.getDoubleAttribute(eleShipmentLine,
							AcademyConstants.ATTR_PLACED_QUANTITY);
					dshipmentContainerizedQuantity += dplacedQuantity;

				}

				SCXmlUtil.setAttribute(inXML.getDocumentElement(), AcademyConstants.ATTR_CONTAINERIZED_QUANTITY,
						Double.toString(dshipmentContainerizedQuantity));

				// Calling the method to invoke changeShipment API
				callchangeShipment(env, strShipmentKey,
						inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_ACTUAL_FREIGHT_CHARGE),
						inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_CONTAINERIZED_QUANTITY));

				// Calling the method to invoke changeShipmentStatus API
				callchangeShipmentStatus(env, strShipmentKey, strShipNode, strDocType);

			} else {
				log.verbose("ShipmentKey passed is null or empty");
			}

		} catch (YFSException e) {
			log.error(e);
			throw e;
		} catch (Exception e) {
			log.error(e);
			throw new YFSException(
					"Exception in the method AcademyChangeShipmentOnFinishPack.changeShipmentOnFinishPack"
							+ e.getMessage());
		}

		log.endTimer("AcademyChangeShipmentOnFinishPack::changeShipmentOnFinishPack-Method");
		return inXML;
	}

	/**
	 * This method prepares the input and calls getShipmentDetails API
	 * 
	 * @param strShipmentKey
	 * @return
	 * @throws Exception
	 */

	private Document callgetShipmentDetailsAPI(YFSEnvironment env, String strShipmentKey) throws Exception {
		log.beginTimer("callgetShipmentDetailsAPI Method");
		Document OutDocgetShipmentDetails = null;
		Document getShipmentDetails = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element elegetShipmentDetails = getShipmentDetails.getDocumentElement();
		elegetShipmentDetails.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
		if (log.isVerboseEnabled()) {
			log.verbose("Calling getShipmentDetails API with the input" + XMLUtil.getXMLString(getShipmentDetails));
		}

		String templateStr = "<Shipment ActualFreightCharge='' DocumentType='' ShipmentKey='' ScacIntegrationRequired='' ShipmentContainerizedFlag='' ShipNode=''/>";

		Document outputTemplate = YFCDocument.getDocumentFor(templateStr).getDocument();
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_DETAILS, outputTemplate);
		OutDocgetShipmentDetails = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_DETAILS,
				getShipmentDetails);
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_DETAILS);
		if (log.isVerboseEnabled()) {
			log.verbose("Output of the  getShipmentDetails API " + XMLUtil.getXMLString(OutDocgetShipmentDetails));
		}

		log.endTimer("callgetShipmentDetailsAPI Method");
		return OutDocgetShipmentDetails;
	}

	/**
	 * This method prepares the input and calls getShipmentContainerList API
	 * 
	 * @param strShipmentKey
	 * @return
	 * @throws Exception
	 */
	private Document callgetShipmentContainerList(YFSEnvironment env, String strShipmentKey) throws Exception {
		log.beginTimer("callgetShipmentContainerList Method");
		Document OutDocgetShipmentContainerList = null;
		Document getShipmentContainerList = XMLUtil.createDocument(AcademyConstants.ELE_CONTAINER);

		Element eleShipmentContainerList = getShipmentContainerList.getDocumentElement();
		eleShipmentContainerList.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
		eleShipmentContainerList.setAttribute(AcademyConstants.ATTR_IS_MANIFESTED, AcademyConstants.STR_NO);
		if (log.isVerboseEnabled()) {
			log.verbose("Calling getShipmentContainerLis API with the input"
					+ XMLUtil.getXMLString(getShipmentContainerList));
		}

		String templateStr = "<Containers> <Container ActualFreightCharge='' ContainerNo=''/></Containers>";

		Document outputTemplate = YFCDocument.getDocumentFor(templateStr).getDocument();
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_CONTAINER_LIST, outputTemplate);
		OutDocgetShipmentContainerList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_CONTAINER_LIST,
				getShipmentContainerList);
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_CONTAINER_LIST);
		if (log.isVerboseEnabled()) {
			log.verbose(
					"Output of the  getShipmentDetails API " + XMLUtil.getXMLString(OutDocgetShipmentContainerList));
		}

		log.endTimer("callgetShipmentContainerList Method");
		return OutDocgetShipmentContainerList;
	}

	/**
	 * This method prepares the input and calls getShipmentLineList API
	 * 
	 * @param strShipmentKey
	 * @return
	 * @throws Exception
	 */
	private Document callgetShipmentLineList(YFSEnvironment env, String strShipmentKey) throws Exception {
		log.beginTimer("callgetShipmentLineList Method");
		Document OutDocgetShipmentLineList = null;
		Document getShipmentLineList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT_LINE);

		Element elegetShipmentLineList = getShipmentLineList.getDocumentElement();
		elegetShipmentLineList.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);

		if (log.isVerboseEnabled()) {
			log.verbose(
					"Calling callgetShipmentLineList API with the input" + XMLUtil.getXMLString(getShipmentLineList));
		}

		String templateStr = "<ShipmentLines> <ShipmentLine PlacedQuantity='' ShipmentLineKey=''/> </ShipmentLines>";

		Document outputTemplate = YFCDocument.getDocumentFor(templateStr).getDocument();
		env.setApiTemplate(AcademyConstants.SER_GET_SHIPMENT_LINE_LIST, outputTemplate);
		OutDocgetShipmentLineList = AcademyUtil.invokeAPI(env, AcademyConstants.SER_GET_SHIPMENT_LINE_LIST,
				getShipmentLineList);
		env.clearApiTemplate(AcademyConstants.SER_GET_SHIPMENT_LINE_LIST);
		if (log.isVerboseEnabled()) {
			log.verbose(
					"Output of the  callgetShipmentLineList API " + XMLUtil.getXMLString(OutDocgetShipmentLineList));
		}
		log.endTimer("callgetShipmentLineList Method");
		return OutDocgetShipmentLineList;
	}

	/**
	 * This method prepares the input and calls changeShipment API
	 * 
	 * @param env
	 * @param strShipmentKey
	 * @param ActualFreightCharge
	 * @param ContainerizedQuantity
	 * @return
	 * @throws Exception
	 */
	private Void callchangeShipment(YFSEnvironment env, String strShipmentKey, String ActualFreightCharge,
			String ContainerizedQuantity) throws Exception {
		log.beginTimer("callchangeShipment Method");

		Document changeShipment = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);

		Element elechangeShipment = changeShipment.getDocumentElement();
		elechangeShipment.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
		elechangeShipment.setAttribute(AcademyConstants.ATTR_ACTUAL_FREIGHT_CHARGE, ActualFreightCharge);
		elechangeShipment.setAttribute(AcademyConstants.ATTR_CONTAINERIZED_QUANTITY, ContainerizedQuantity);
		elechangeShipment.setAttribute(AcademyConstants.ATTR_IS_PACK_PROCESS_COMPLETED, AcademyConstants.STR_YES);
		if (log.isVerboseEnabled()) {
			log.verbose("Calling changeShipmen API with the input" + XMLUtil.getXMLString(changeShipment));
		}

		AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, changeShipment);

		log.endTimer("callchangeShipment Method");
		return null;
	}

	/**
	 * This method prepares the input and calls changeShipment API
	 * 
	 * @param env
	 * @param strShipmentKey
	 * @return
	 * @throws Exception
	 */
	private Void callchangeShipmentStatus(YFSEnvironment env, String strShipmentKey, String strShipNode, String strDocType)
			throws Exception {
		log.beginTimer("callchangeShipmentStatus Method");

		Document changeShipmentStatus = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);

		Element elechangeShipmentStatus = changeShipmentStatus.getDocumentElement();
		elechangeShipmentStatus.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
		elechangeShipmentStatus.setAttribute(AcademyConstants.ATTR_BASEDROP_STATUS,
				AcademyConstants.VAL_READY_TO_SHIP_STATUS);
		/* OMNI -46029 - START */
		if(AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE.equals(strDocType)) {
			elechangeShipmentStatus.setAttribute(AcademyConstants.ATTR_TRANSID,
					AcademyConstants.TRAN_ID_READY_TO_SHIP_FROM_WEBSTORE_0006);
		} else {
			elechangeShipmentStatus.setAttribute(AcademyConstants.ATTR_TRANSID,
					AcademyConstants.TRAN_ID_READY_TO_SHIP_FROM_WEBSTORE);
		}
		/* OMNI -46029 - END */
		elechangeShipmentStatus.setAttribute(AcademyConstants.ATTR_SHIP_NODE, strShipNode);
		if (log.isVerboseEnabled()) {
			log.verbose("Calling changeShipmentStatus API with the input" + XMLUtil.getXMLString(changeShipmentStatus));
		}

		AcademyUtil.invokeAPI(env, AcademyConstants.CHANGE_SHIPMENT_STATUS, changeShipmentStatus);

		log.endTimer("callchangeShipmentStatus Method");
		return null;
	}
}
