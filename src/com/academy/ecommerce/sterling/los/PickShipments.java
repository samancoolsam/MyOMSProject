package com.academy.ecommerce.sterling.los;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.academy.ecommerce.sterling.shipment.AcademyBlankOutScac;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class PickShipments
{
	private static YFCLogCategory log = YFCLogCategory.instance(PickShipments.class);
	public Document pickShipments(YFSEnvironment env, Document inXML) throws Exception
	{
		log.verbose("Inside method");
		log.verbose("*** pickShipments input doc *** "	+ XMLUtil.getXMLString(inXML));

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
		rootShipment.setAttribute("StatusQryType", "BETWEEN");
		rootShipment.setAttribute("FromStatus", "1100.50");
		rootShipment.setAttribute("ToStatus", "1200");
		rootShipment.setAttribute("ShipNode", "005");
		rootShipment.setAttribute("ShipmentKey", shipmentKey);
		rootShipment.setAttribute("ShipmentKeyQryType", "GT");

		Element additionalDates = shipmentList.createElement("AdditionalDates");
		Element additionalDate = shipmentList.createElement("AdditionalDate");
		additionalDate.setAttribute("DateTypeId", "ACADEMY_SHIPMENT_DATE");
		additionalDate.setAttribute("ExpectedDate", currentDate.getString(YFCDate.ISO_DATETIME_FORMAT));
		additionalDate.setAttribute("ExpectedDateQryType", "LE");
		additionalDates.appendChild(additionalDate);
		rootShipment.appendChild(additionalDates);

		Element toAddress = shipmentList.createElement("ToAddress");
		Element extn = shipmentList.createElement("Extn");
		extn.setAttribute("ExtnIsAPOFPO", "N");
		extn.setAttribute("ExtnIsPOBOXADDRESS", "N");
		toAddress.appendChild(extn);
		rootShipment.appendChild(toAddress);

		Element complexQuery = shipmentList.createElement("ComplexQuery");
		complexQuery.setAttribute("Operation", "AND");
		Element or = shipmentList.createElement("Or");
		Element exp1 = shipmentList.createElement("Exp");
		exp1.setAttribute("Name", "ShipmentType");
		exp1.setAttribute("QryType", "EQ");
		exp1.setAttribute("Value", "CON");
		Element exp2 = shipmentList.createElement("Exp");
		exp2.setAttribute("Name", "ShipmentType");
		exp2.setAttribute("QryType", "EQ");
		exp2.setAttribute("Value", "CONOVNT");
		Element exp3 = shipmentList.createElement("Exp");
		exp3.setAttribute("Name", "ShipmentType");
		exp3.setAttribute("QryType", "EQ");
		exp3.setAttribute("Value", "GC");
		Element exp4 = shipmentList.createElement("Exp");
		exp4.setAttribute("Name", "ShipmentType");
		exp4.setAttribute("QryType", "EQ");
		exp4.setAttribute("Value", "GCO");
		or.appendChild(exp1);
		or.appendChild(exp2);
		or.appendChild(exp3);
		or.appendChild(exp4);
		complexQuery.appendChild(or);
		rootShipment.appendChild(complexQuery);

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
