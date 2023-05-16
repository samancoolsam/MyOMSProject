package com.academy.ecommerce.sterling.los;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.japi.YFSEnvironment;

	
	/**This method helps pick up the list of shipments that are eligible for routing(upgrade/downgrade)
	The input to getShipmentList formed from this method will be like following:
	<Shipment Status="1100" ShipNode="005" ShipmentKey="" ShipmentKeyQryType="GT" >
	</Shipment>
	*/
public class AcademyPickShipmentsForRouting
{
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPickShipmentsForRouting.class);
	public Document pickShipmentsForRouting(YFSEnvironment env, Document inXML) throws Exception
	{
		log.verbose("Inside method");
		log.verbose("*** PickShipmentsForRouting input doc *** "	+ XMLUtil.getXMLString(inXML));

		YFCDate currentDate = new YFCDate(new java.util.Date());
		NodeList nl = inXML.getElementsByTagName("LastMessage");
		String shipmentKey = "";
		if (nl.getLength() == 1)
		{
			shipmentKey = ((Element) (((Element) (inXML.getElementsByTagName("LastMessage").item(0))).getElementsByTagName("Shipment").item(0)))
					.getAttribute("ShipmentKey");
		}

		Document shipmentList = XMLUtil.createDocument("Shipment");
		Element rootShipment = shipmentList.getDocumentElement();
		rootShipment.setAttribute("StatusQryType", "EQ");
		rootShipment.setAttribute("Status", "1100");
		rootShipment.setAttribute("ShipNode", "005");
		rootShipment.setAttribute("ShipmentKey", shipmentKey);
		rootShipment.setAttribute("ShipmentKeyQryType", "GT");

		log.verbose("getShipmentList input XML " + XMLUtil.getXMLString(shipmentList));

		Document getShipmentListTemplate = XMLUtil.getDocument("<Shipments TotalNumberOfRecords=\"\" >" + "<Shipment>" + "<AdditionalDates >"
				+ "<AdditionalDate />" + "</AdditionalDates >" + "<FromAddress>" + "<Extn />" + "</FromAddress>" + "<ToAddress>" + "<Extn />" + 
				"</ToAddress>" + "<Extn />" + "</Shipment>" + "</Shipments>");
		log.verbose("getShipmentListTemplate  " + XMLUtil.getXMLString(getShipmentListTemplate));

		env.setApiTemplate("getShipmentList", getShipmentListTemplate);
		Document outShipmentListDoc = AcademyUtil.invokeAPI(env, "getShipmentList", shipmentList);
		env.clearApiTemplate("getShipmentList");
		log.verbose(" shipmentList Output " + XMLUtil.getXMLString(outShipmentListDoc));

		return outShipmentListDoc;
	}
}
