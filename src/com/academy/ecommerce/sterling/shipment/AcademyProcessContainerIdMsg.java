package com.academy.ecommerce.sterling.shipment;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author <a href="mailto:Kruthi.KM@cognizant.com">Kruthi KM</a>, Created on
 *         01/08/2014. This Class will get invoked when Sterling receives
 *         ContainerId message from Exeter-WMS via ESB.
 */
public class AcademyProcessContainerIdMsg {
	/**
	 * Instance of logger
	 */
	private static final YFCLogCategory log = YFCLogCategory
			.instance(AcademyProcessContainerIdMsg.class);

	/*
	 * This method will prepare the input to changeShipment API to store the
	 * ContainerId coming from Exeter in Sterling in yfs_shipment table under
	 * EXTN_EXETER_CONTAINER_ID column.
	 * Input XML format :
	 * Updated as part of OMNI-6610
	 * 	<Shipment ShipmentNo="" > 
			<Extn Firearm="False" Lane="S021" ContainerId="887781535"/>
		</Shipment>
	 */
	public void processContainerIdMsg(YFSEnvironment env, Document inDoc) {
		log.verbose("Input to processContainerIdMsg()::"
				+ XMLUtil.getXMLString(inDoc));
		try {
			String strExeterContainerId = XPathUtil.getString(inDoc, "/Shipment/Extn/@ContainerId");
			Document changeShipmentInDoc = XMLUtil
					.createDocument(AcademyConstants.ELE_SHIPMENT);
			changeShipmentInDoc.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_SELL_ORG_CODE,
					AcademyConstants.PRIMARY_ENTERPRISE);
			changeShipmentInDoc.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_ENTERPRISE_CODE,
					AcademyConstants.PRIMARY_ENTERPRISE);
			changeShipmentInDoc.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_DOC_TYPE,
					AcademyConstants.SALES_DOCUMENT_TYPE);
			/*START SHIN-17 removed hard coding of ShipNode="001" and fetch it from the 
			 * input message coming from ESB
			 * 
			 * changeShipmentInDoc.getDocumentElement().setAttribute(
			 *		AcademyConstants.SHIP_NODE, AcademyConstants.STR_001_NODE);
			 */
			changeShipmentInDoc.getDocumentElement().setAttribute(
					AcademyConstants.SHIP_NODE,
					inDoc.getDocumentElement().getAttribute(
							AcademyConstants.SHIP_NODE));
			// END SHIN-17 
			changeShipmentInDoc.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_SHIPMENT_NO,
					inDoc.getDocumentElement().getAttribute(
							AcademyConstants.ATTR_SHIPMENT_NO));
			Element eleExtn = changeShipmentInDoc
					.createElement(AcademyConstants.ELE_EXTN);
			eleExtn.setAttribute(AcademyConstants.ATTR_EXETER_CONTAINER_ID,
					strExeterContainerId);
			//Start : OMNI-6610 : Lane & Firearm details to be sent from Exeter to OMS for all STS Shipments
			String strExtnLane = XPathUtil.getString(inDoc, "/Shipment/Extn/@Lane");
			String strExtnFirearm = XPathUtil.getString(inDoc, "/Shipment/Extn/@Firearm");
			
			if(!YFCObject.isVoid(strExtnFirearm) || !YFCObject.isVoid(strExtnLane)) {
				//changeShipmentInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_SELL_ORG_CODE,
				//		AcademyConstants.HUB_CODE);
				
				eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_LANE, strExtnLane);
				if(!YFCObject.isVoid(strExtnFirearm) && strExtnFirearm.equalsIgnoreCase(AcademyConstants.STR_TRUE)) {
					eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_IS_FIREARM, AcademyConstants.STR_YES);
				}
				else if(!YFCObject.isVoid(strExtnFirearm) && strExtnFirearm.equalsIgnoreCase(AcademyConstants.STR_FALSE)) {
					eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_IS_FIREARM, AcademyConstants.STR_NO);
				}
				else {
					log.info("Invalid Firearm Info :: " + strExtnFirearm + " :: recieved for Shipment :: "
							+ inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
				}	
			}			
			//End : OMNI-6610 : Lane & Firearm details to be sent from Exeter to OMS for all STS Shipments
			
			changeShipmentInDoc.getDocumentElement().appendChild(eleExtn);

			log.verbose("Change Shipment Input XML::"
					+ XMLUtil.getXMLString(changeShipmentInDoc));
			Document outDocChangeShipment = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_CHANGE_SHIPMENT, changeShipmentInDoc);

			log.verbose("Change Shipment Output XML::"
					+ XMLUtil.getXMLString(outDocChangeShipment));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
