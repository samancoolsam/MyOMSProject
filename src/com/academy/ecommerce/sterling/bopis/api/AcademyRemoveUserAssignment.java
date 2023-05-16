/**
 * 
 */
package com.academy.ecommerce.sterling.bopis.api;

import java.rmi.RemoteException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @author Mohamed Shaikna
 *
 */

public class AcademyRemoveUserAssignment {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyRemoveUserAssignment.class);

	private static YIFApi api = null;
	static {
		try {
			api = YIFClientFactory.getInstance().getApi();
		} catch (YIFClientCreationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method will be invoked to check the Status and remove the
	 * AssignedToUserID from the Shipment accordingly.
	 * 
	 * @param env
	 * @param inXML
	 * @return inXML
	 * @throws ParserConfigurationException
	 * @throws RemoteException
	 * @throws YFSException
	 *
	 */
	public Document removeUserAssignment(YFSEnvironment env, Document inXML)
			throws ParserConfigurationException, YFSException, RemoteException {

		log.beginTimer("AcademyRemoveUserAssignment::removeUserAssignment");
		log.verbose("Entering the method AcademyRemoveUserAssignment.removeUserAssignment ");
		log.verbose("Input XML for removeUserAssignment() ==> " + XMLUtil.getXMLString(inXML));

		String strShipmentStatus = null;
		String strShipmentKey = null;
		String strDeliveryMethod = null;
		String strNodeType = null;

		Element eleShipment = inXML.getDocumentElement();
		Element eleShipNode = (Element) eleShipment.getElementsByTagName(AcademyConstants.ATTR_SHIP_NODE).item(0);

		strShipmentStatus = eleShipment.getAttribute(AcademyConstants.ATTR_STATUS);
		strShipmentKey = eleShipment.getAttribute(AcademyConstants.SHIPMENT_KEY);
		strDeliveryMethod = eleShipment.getAttribute(AcademyConstants.ATTR_DEL_METHOD);
		strNodeType = eleShipNode.getAttribute(AcademyConstants.ATTR_NODE_TYPE);

		log.verbose("Shipment Key is : " + strShipmentKey);
		log.verbose("Shipment Status is : " + strShipmentStatus);
		log.verbose("DeliveryMethod is : " + strDeliveryMethod);
		log.verbose("NodeType is : " + strNodeType);

		// Check if the Shipment Status is either "Ready For Packing" or "Ready To
		// Ship", Then Remove the User Assignment.

		if ((strShipmentStatus.equalsIgnoreCase("1100.70.06.50")
				|| strShipmentStatus.equalsIgnoreCase(AcademyConstants.VAL_READY_TO_SHIP_STATUS)
				|| strShipmentStatus.equalsIgnoreCase("1100.70.06.20")) && strDeliveryMethod.equalsIgnoreCase("SHP")
				&& strNodeType.equalsIgnoreCase(AcademyConstants.STR_STORE)) {

			String strChangeShipmentTemp = "<Shipment ShipmentKey=\"\" Status=\"\" AssignedToUserId=\"\" />";

			// Prepare Document for changeShipment

			Document docChangeShipmentInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			Element elemShipment = (Element) docChangeShipmentInput.getDocumentElement();

			elemShipment.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
			elemShipment.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.STR_ACTION_MODIFY);
			elemShipment.setAttribute(AcademyConstants.ATTR_ASSIGNED_TO_USER_ID, "");

			env.setApiTemplate(AcademyConstants.API_CHANGE_SHIPMENT, strChangeShipmentTemp);
			Document changeShipmentOutput = api.changeShipment(env, docChangeShipmentInput);
			env.clearApiTemplate(AcademyConstants.API_CHANGE_SHIPMENT);

			log.verbose("changeShipment Output is : " + XMLUtil.getXMLString(changeShipmentOutput));

		}

		return inXML;
	}
}