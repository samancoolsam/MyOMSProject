package com.academy.ecommerce.sterling.bopis.order.condition;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyBOPISUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @Purpose : 
 * As part of BOPIS-Phase 1 requirement, following 3 holds must not be applied if order has BOPIS line.
 *  - ACADEMY_AWAIT_FRAUD
 *	- MAX_QTY_HOLD (This hold bypassing logic is taken care in AcademyBeforeCreateOrderUEImpl.stampPackListTypeForOrderLines() )
 *
 *
 *  If Order has BOPIS line & if the Hold type is configured in 'BOPIS_BYPAS_HOLDTYPS' commoncode, then bypass placing hold on that Order.
 *  
 *
 *	This DynamicCondition class is invoked from below 2 places 
 *  - During Automatic hold creation - ‘Max Qty Hold’
 *  
 **/

public class AcademyBypassHoldCreationForBOPISMixedCartOrders implements YCPDynamicConditionEx {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyBypassHoldCreationForBOPISMixedCartOrders.class);	
	private Map argMap;
	public void setProperties(Map map) {
		argMap = map;
	}
	
	public boolean evaluateCondition(YFSEnvironment env, String sName, Map mapData, Document inDoc) {
		log.beginTimer("Entering AcademyBypassHoldCreationForBOPISMixedCartOrders.evaluateCondition() ::"+XMLUtil.getXMLString(inDoc));
		
		boolean bAllowHoldCreationOnOrder = false;
		boolean bOrderEligibleForHold = false;
		String strHoldType = (String)argMap.get(AcademyConstants.STR_HOLD_TYPE);
		log.verbose("HOLD_TYPE :: "+strHoldType);
		Element eleCommonCode = null;
		Document docOutGetCommonCodeList = null;		
		
		try{
			//If any of the orderline is BOPIS, bypass hold creation
			boolean bOrderHasBOPISline = AcademyBOPISUtil.checkIfOrderHasBOPISline(inDoc);			
			log.verbose("Order has BOPIS orderline : "+bOrderHasBOPISline);
					
			if(bOrderHasBOPISline){
				docOutGetCommonCodeList = AcademyBOPISUtil.getCommonCodeList(env, AcademyConstants.STR_BYPASS_HOLDS_COMMONCODE_TYPE, AcademyConstants.PRIMARY_ENTERPRISE);			
				
				eleCommonCode = (Element) XPathUtil.getNode(docOutGetCommonCodeList, "/CommonCodeList/CommonCode[@CodeValue='" + strHoldType + "']");
				
				//If HoldType is configured in commoncode, then bypass hold creation
				if(YFCCommon.isVoid(eleCommonCode)){
					log.verbose("Order has BOPIS line. But holdType is not listed in commoncode, hence order is eligible for Hold creation");
					bOrderEligibleForHold = true;
				}
			}else{
				//Non-BOPIS order
				log.verbose("Non-BOPIS order -> Hence follow as is hold creation process");
				bOrderEligibleForHold = true;		
			}
			
			//This is a Common class for evaluating 1 type of hold creation - ACADEMY_AWAIT_FRAUD
			if(bOrderEligibleForHold){
				//If (/Order/@ExtnFraudCheck='CHALLENGE') , then apply ACADEMY_AWAIT_FRAUD Hold on the otrder
				if(AcademyConstants.STR_FRAUD_HOLD_TYPE.equals(strHoldType)){
					String strExtnFraudCheck = XMLUtil.getAttributeFromXPath(inDoc, AcademyConstants.XPATH_ORDER_EXTN_FRAUDCHECK);
					if(AcademyConstants.STR_FRAUDHOLD_CHALLENGE.equals(strExtnFraudCheck)){
						bAllowHoldCreationOnOrder = true;
						log.verbose("Apply Hold - 'ACADEMY_AWAIT_FRAUD'");
					}				
				}
				//KER-12036 : Payment Migration Changes with new Fraud system 
				//If (/Order/@ExtnFraudCheck='review') , then apply ACADEMY_FRAUD_REVIEW Hold on the otrder
				if(AcademyConstants.STR_FRAUD_REVIEW_HOLD.equals(strHoldType)){
					String strExtnFraudCheck = XMLUtil.getAttributeFromXPath(inDoc, AcademyConstants.XPATH_ORDER_EXTN_FRAUDCHECK);
					if(AcademyConstants.STR_FRAUDHOLD_REVIEW.equalsIgnoreCase(strExtnFraudCheck)){
						bAllowHoldCreationOnOrder = true;
						log.verbose("Apply Hold - 'ACADEMY_FRAUD_REVIEW'");
					}		
				}				
			}
			
		}catch (Exception e) {
			log.error(e);
			throw new YFSException("Exception in the method AcademyBypassHoldCreationForBOPISMixedCartOrders.evaluateCondition() : " +e.getMessage());
		}	
		log.endTimer("Exiting AcademyBypassHoldCreationForBOPISMixedCartOrders.evaluateCondition() ::"+bAllowHoldCreationOnOrder);		
		
		return bAllowHoldCreationOnOrder;
	}

}
