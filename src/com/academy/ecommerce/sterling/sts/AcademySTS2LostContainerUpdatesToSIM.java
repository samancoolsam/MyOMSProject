package com.academy.ecommerce.sterling.sts;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademySTS2LostContainerUpdatesToSIM {

	// Class variables for logger and properties
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySTS2LostContainerUpdatesToSIM.class);

	public Document sts2LostContainerUpdatesToSIM(YFSEnvironment env, Document inDoc)
			throws IllegalArgumentException, Exception {
		log.beginTimer("AcademySTS2LostContainerUpdatesToSIM.sts2LostContainerUpdatesToSIM()");
		log.verbose("Input - AcademySTS2LostContainerUpdatesToSIM.sts2LostContainerUpdatesToSIM() input"
				+ XMLUtil.getXMLString(inDoc));

		Element eleContainer = inDoc.getDocumentElement();

		String strNodeType = XMLUtil.getAttributeFromXPath(inDoc, "/Container/Shipment/ShipNode/@NodeType");
		String strShipNode = XMLUtil.getAttributeFromXPath(inDoc, "/Container/Shipment/ShipNode/@ShipNode");
		String sFulfillmentType = XMLUtil.getAttributeFromXPath(inDoc,
				"/Container/ContainerDetails/ContainerDetail/ShipmentLine/OrderLine/ChainedFromOrderLine/@FulfillmentType");
		
		
		NodeList nlShipment = eleContainer.getElementsByTagName("Shipment");
		if (!YFCObject.isVoid(nlShipment) && nlShipment.getLength() > 0) {
			Element eleShipment = (Element) nlShipment.item(0);
			eleShipment.setAttribute("ShipNode", strShipNode);
		}
		
		if (!YFCObject.isVoid(strNodeType) && AcademyConstants.STR_STORE.equals(strNodeType)
				&& !YFCObject.isVoid(sFulfillmentType) && AcademyConstants.V_FULFILLMENT_TYPE_STS.equals(sFulfillmentType)) {
			eleContainer.setAttribute(AcademyConstants.STR_LOSTCONTAINER_SHRINKFLAG, AcademyConstants.STR_YES);
			AcademyUtil.invokeService(env, "AcademyPostDemandUpdateToSIMOnCloseReceipt", inDoc);
		}

		log.verbose("End - AcademySTS2LostContainerUpdatesToSIM.sts2LostContainerUpdatesToSIM()");
		log.endTimer("AcademySTS2LostContainerUpdatesToSIM.sts2LostContainerUpdatesToSIM()");

		return inDoc;
	}
}