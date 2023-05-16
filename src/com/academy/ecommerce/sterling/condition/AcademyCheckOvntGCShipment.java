package com.academy.ecommerce.sterling.condition;

import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.YCPDynamicCondition;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyCheckOvntGCShipment implements YCPDynamicCondition{

	private Properties props;	
	   public void setProperties(Properties props) {
	        this.props = props;
	   }
	
	public boolean evaluateCondition(YFSEnvironment arg0, String arg1,
									 Map arg2, String sXMLData) {
		
		
		Document inXml = XMLUtil.getDocumentFromString((sXMLData));
		
		//System.out.println("this is the input to the condition" + XMLUtil.getXMLString(inXml));
		
		Element shipmentElement = (Element) (inXml.getDocumentElement()).getElementsByTagName("Shipment").item(0);
		
		String shipmentType = shipmentElement.getAttribute("ShipmentType");
		
		String carrierServiceCode = shipmentElement.getAttribute("CarrierServiceCode");
		
		
		if ("CONOVNT".equals(shipmentType)) {
			return true;
		} else {
			if ("GC".equals(shipmentType) || shipmentType.equalsIgnoreCase(AcademyConstants.STR_GC_ONLY_SHIP_TYPE)) {
				if (("2nd Day Air").equals(carrierServiceCode) || ("Next Day Air").equals(carrierServiceCode)) {
					return true;
				}
			}
		}
		return false;
	}

	public void setProperties(Map arg0) {
		// TODO Auto-generated method stub
		
	}
}
