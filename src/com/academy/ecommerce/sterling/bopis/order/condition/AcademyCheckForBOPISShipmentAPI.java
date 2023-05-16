package com.academy.ecommerce.sterling.bopis.order.condition;

import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
/**
 * @Purpose : This class used to check whether current shipment is BOPIS shipment or not.
 * 
 * getCompleteOrderDetais Output will be the input for this class.
 * 
 * First it takes the ShipmentKey from env object, which has set in AcademySendEmailAgentServer class.
 * It identifies the current shipment based on shipment key.
 * Then it checks whether shipment is a BOPIS or not based on DeliveryMethod, if it is PICK then it returns true else false.
 **/

public class AcademyCheckForBOPISShipmentAPI implements YCPDynamicConditionEx {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyCheckForBOPISShipmentAPI.class);

	@Override
	public boolean evaluateCondition(YFSEnvironment env, String strMessage, Map mapData, Document inDoc) {
		log.beginTimer("Start of AcademyCheckForBOPISShipmentAPI.evaluateCondition  :");
		log.verbose("Input to AcademyCheckForBOPISShipmentAPI "+XMLUtil.getXMLString(inDoc));
		String strSHPKey = (String) env.getTxnObject("SHP_Key");
		if(!YFCCommon.isVoid(strSHPKey)) {
			try {
				Element eleShip = (Element) XPathUtil.getNode(inDoc.getDocumentElement(), "/Order/Shipments/Shipment[@ShipmentKey='" + strSHPKey + "']");
				if((!YFCCommon.isVoid(eleShip)) && (eleShip.getAttribute(AcademyConstants.ATTR_DEL_METHOD).equals(AcademyConstants.STR_PICK))) 
					return true;
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return false;
	}

	@Override
	public void setProperties(Map arg0) {
		// TODO Auto-generated method stub
		
	}

}
