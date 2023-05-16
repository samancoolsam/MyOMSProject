//package declaration

package com.academy.ecommerce.sterling.shipment;

//java util import statements
import java.util.Properties;
//java w3c statements
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
//academy import statements
import com.academy.ecommerce.sterling.shipment.AcademySFSPrintPendingShipments;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
//yantra import statements
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * Description: Class AcademySFSPublishTransferRequestMsg prepares xml structure
 * required for Store Transfer Request message
 * 
 * @throws Exception
 * @return outXML (Transfer Request message)
 */

public class AcademySFSPublishTransferRequestMsg implements YIFCustomApi
{
	// Instance to store the properties configured for the condition in
	// Configurator
	private Properties	props;

	// Stores property:TO_SHIP_NODE configured in configurator
	public void setProperties(Properties props) throws Exception
	{
		this.props = props;
	}

	public Document formatTransferMessage(YFSEnvironment env, Document inXML) throws Exception
	{
		// Declare Document Variable
		Document outXML = null;
		// Declare Element Variable
		Element eleShipment = null;
		Element eleShipmentLines = null;
		Element eleShipmentLine = null;
		Element eleItem = null;
		Element eleLinePriceInfo = null;
		Element elenewShipmentLine = null;
		Element eleShipmentLineItem = null;
		// Declare NodeList Variable
		NodeList nlShipmentList = null;

		// Create output XML in the format
		// <Shipment ShipmentNo="" FromShipNode="" ToShipNode="">
		// <ShipmentLines>
		// <ShipmentLine ItemID="" Quantity="" ItemUPCCode="" ProductClass=""
		// UnitOfMeasure="" UnitPrice=""/>
		// </ShipmentLines>
		// </Shipment>

		// create element Shipment
		outXML = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		eleShipment = outXML.getDocumentElement();
		// Set attribute ShipmentNo
		eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));

		String fromNode = (String) props.get(AcademyConstants.KEY_FROM_SHIP_NODE);
		if (!YFCObject.isNull(fromNode))
		{
			// Set attribute FromShipNode
			eleShipment.setAttribute(AcademyConstants.ATTR_FROM_NODE, fromNode);
		} else
		{
			eleShipment.setAttribute(AcademyConstants.ATTR_FROM_NODE, inXML.getDocumentElement().getAttribute(AcademyConstants.SHIP_NODE));
		}
		// Set attribute ToShipNode
		eleShipment.setAttribute(AcademyConstants.ATTR_TO_NODE, props.getProperty(AcademyConstants.KEY_TO_SHIP_NODE));
		// create element ShipmentLines
		eleShipmentLines = outXML.createElement(AcademyConstants.ELE_SHIPMENT_LINES);
		// Append to Shipment
		eleShipment.appendChild(eleShipmentLines);
		// Fetch the list of element ShipmentLine
		nlShipmentList = inXML.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		// Loop through each ShipmentLine
		for (int i = 0; i < nlShipmentList.getLength(); i++)
		{
			eleShipmentLine = (Element) nlShipmentList.item(i);
			// Fetch the element Item
			eleItem = (Element) XPathUtil.getNode(eleShipmentLine, AcademyConstants.XPATH_SHIP_ITEM);
			// Fetch the element LinePriceInfo
			eleLinePriceInfo = (Element) XPathUtil.getNode(eleShipmentLine, AcademyConstants.XPATH_LINE_PRICE_INFO);
			// Fetch the element ShipmentLine/Item
			eleShipmentLineItem = (Element) XPathUtil.getNode(eleShipmentLine, AcademyConstants.ITEM);
			// create element ShipmentLine
			elenewShipmentLine = outXML.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
			// Set attribute ItemID
			elenewShipmentLine.setAttribute(AcademyConstants.ATTR_ITEM_ID, eleShipmentLine.getAttribute(AcademyConstants.ATTR_ITEM_ID));
			// Set attribute Quantity
			elenewShipmentLine.setAttribute(AcademyConstants.ATTR_QUANTITY, eleShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY));
			// Set attribute UnitOfMeasure
			elenewShipmentLine.setAttribute(AcademyConstants.ATTR_UOM, eleShipmentLine.getAttribute(AcademyConstants.ATTR_UOM));
			// Set attribute ProductClass
			elenewShipmentLine.setAttribute(AcademyConstants.ATTR_PROD_CLASS, eleShipmentLine.getAttribute(AcademyConstants.ATTR_PROD_CLASS));
			// Set attribute UnitPrice
			elenewShipmentLine.setAttribute(AcademyConstants.ATTR_UNIT_PRICE, eleLinePriceInfo.getAttribute(AcademyConstants.ATTR_UNIT_PRICE));
			// Set attribute ItemUPCCode
			/*
			 * IP requires Vendor Item ID. Mapping the Item UPC to Global Item
			 * ID before passing to IP
			 * elenewShipmentLine.setAttribute(AcademyConstants.ATTR_ITEM_UPC,AcademySFSPrintPendingShipments.generateBarcode(eleShipmentLine.getAttribute(AcademyConstants.ATTR_ITEM_ID)) );
			 */
			elenewShipmentLine.setAttribute(AcademyConstants.ATTR_ITEM_UPC, eleShipmentLineItem.getAttribute(AcademyConstants.ATTR_GLOB_ITEM_ID));
			// Append element ShipmentLine to element ShipmentLines
			eleShipmentLines.appendChild(elenewShipmentLine);
		}
		// return the final output xml
		return outXML;
	}
}