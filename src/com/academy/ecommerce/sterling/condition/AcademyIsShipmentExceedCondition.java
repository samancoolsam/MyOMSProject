package com.academy.ecommerce.sterling.condition;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.constants.AcademyConstants;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.academy.util.xml.XMLUtil;

public class AcademyIsShipmentExceedCondition implements
YCPDynamicConditionEx {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyIsShipmentExceedCondition.class);
	private Map propMap = null;
	
	public boolean evaluateCondition(YFSEnvironment env, String sName,
			Map mapData, Document inDoc) {	
		log.verbose("Input to AcademyIsShipmentExceedCondition :: " + XMLUtil.getXMLString(inDoc));
		boolean isHeavierThanLimit = false;
		Element parentEle = inDoc.getDocumentElement();
		String SHIPMENT_TYPE_NOT_CONSIDER = "ShipmentTypeNotConsider";//BLP,BNLP,BULK,BULKOVNT,WG
		
		// Check - Parent Element Name 
		log.verbose("****** Parent Node Name is : "+parentEle.getNodeName()+"****** \n");
		if(parentEle.getNodeName().equals(AcademyConstants.ELE_SHIPMENT)){
			log.verbose("*********** Inside :parentEle.getNodeName().equals(AcademyConstants.ELE_SHIPMENT) *********** \n");
			/*START : STL-1336 : Change the condition as per JIRA
			 * String strTotalWeight = "0";
			strTotalWeight = parentEle.getAttribute(AcademyConstants.TOTAL_WEIGHT);
			
			log.verbose("*********** strTotalWeight : " + strTotalWeight + " *********** \n");
			if (strTotalWeight != null)
			{
				Double totalWeight = new Double(strTotalWeight);
				if(totalWeight > 750) 
				{
					isHeavierThanLimit = true;
				}
			}
			log.verbose("*********** strTotalWeight : " + strTotalWeight + " ,isHeavierThanLimit : " + isHeavierThanLimit + " *********** \n");
			*/			
			String strPropTotalWeight = (String) this.propMap.get(AcademyConstants.TOTAL_WEIGHT);
			Double totalWeight = Double.parseDouble(strPropTotalWeight);
			String strPropShipmentType = (String) this.propMap.get(SHIPMENT_TYPE_NOT_CONSIDER);
			
			log.verbose("*********** Condition Properties *********** \n Total Weight="+strPropTotalWeight+" Shipment Types="+strPropShipmentType+"************ \n");
			
			String strTotalWeight = parentEle.getAttribute(AcademyConstants.TOTAL_WEIGHT);
			Double dTotalWeight = Double.parseDouble(strTotalWeight);
			String strDocumentType = parentEle.getAttribute(AcademyConstants.ATTR_DOC_TYPE);
			String strShipNode = parentEle.getAttribute(AcademyConstants.SHIP_NODE);
			String strShipmentType = parentEle.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE);
			
			log.verbose("*********** Values from InDoc *********** \n Total Weight = " + dTotalWeight +" Document Type = "+ strDocumentType
					+ " Ship Node = " + strShipNode + " Shipment Types " + strShipmentType + "************ \n");
			
			if(AcademyConstants.WMS_NODE.equals(strShipNode) && AcademyConstants.SALES_DOCUMENT_TYPE.equals(strDocumentType) && dTotalWeight > totalWeight 
					&& (!strPropShipmentType.contains(strShipmentType)))
			{
				isHeavierThanLimit = true;
			}
			//END : STL-1336 : Change the condition as per JIRA
			log.verbose("*********** isHeavierThanLimit : " + isHeavierThanLimit + " *********** \n");
		}
		log.verbose("*********** End :AcademyIsShipmentExceedCondition.evaluateCondition() *********** \n");
		return isHeavierThanLimit;
	}

 
	public void setProperties(Map arg0) {
		propMap = arg0;
	}

}
