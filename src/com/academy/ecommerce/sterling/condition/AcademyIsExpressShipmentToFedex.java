//package declaration
package com.academy.ecommerce.sterling.condition;

//import statements declaration
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;


/**
 * @author kparvath
 * 
 * Evaluates the condition whether the shipment is Express Shipment or not.
 * 
 * This Dynamic condition has been used in Service "AcademySFSCreateFedexExpressPickupReqService".
 *
 */
public class AcademyIsExpressShipmentToFedex  implements YCPDynamicConditionEx{

//	Set the logger
	private static YFCLogCategory log = YFCLogCategory
	.instance(AcademyIsExpressShipmentToFedex.class);

	public boolean evaluateCondition(YFSEnvironment env, String arg1, Map arg2, Document inXML) {

		log.debug("Input MXL coming to evaluateCondition AcademyIsExpressShipmentToFedex :: " + XMLUtil.getXMLString(inXML));

		Element containerEle = inXML.getDocumentElement();

		String strSCAC = containerEle.getAttribute("SCAC");
		String strCarrierServiceCode = containerEle.getAttribute("CarrierServiceCode");

		if(strSCAC.equalsIgnoreCase("FEDX") && (strCarrierServiceCode.equals("2 Day") || strCarrierServiceCode.equals("Standard Overnight")))
		{
			log.verbose("Express Shipment");
			return true;

		}
		else{
			log.verbose("not Express Shipment");
			return false;
		}

	}

	public void setProperties(Map arg0) {
		// TODO Auto-generated method stub

	}
}
