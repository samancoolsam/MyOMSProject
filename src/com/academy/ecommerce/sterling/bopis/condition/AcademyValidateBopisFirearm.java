package com.academy.ecommerce.sterling.bopis.condition;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @Author Syed Saman Murtuza (C0029527)
 * @JIRA# OMNI-94773 - BOPIS Firearms - Online Cancellation (OMS to GSM)
 * @Date Created Jan 3rd 2023
 * 
 * @Purpose
 * This Dynamic condition is used to check whether it is BOPIS order and in 'Paper Work Initiated' or 'RFCP' status
 **/

public class AcademyValidateBopisFirearm implements YCPDynamicConditionEx {
	
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyValidateBopisFirearm.class);

	boolean bBopisFA = false;
	boolean bEligibleStatus = false;
	int iLen = 0;
	
	/**
	 * Dynamic condition to check BOPIS line and previous status as 'RFCP' or 'Paper Work Initiated'
	 * @param env
	 * @param inDoc
	 * @return bStatusRFCPOrPaperworkInit
	 * @throws Exception
	 */	
	@Override
	public boolean evaluateCondition(YFSEnvironment env, String str, Map map, Document inDoc) {
		
		log.beginTimer("AcademyValidateBopisFirearm-evaluateCondition() : Start");
		log.debug("AcademyValidateBopisFirearm-evaluateCondition() : InDoc"+inDoc);
		try
		{ 
			
		    Element eleShipment = inDoc.getDocumentElement();
		    Element eleShipmentLines = SCXmlUtil.getChildElement(eleShipment, AcademyConstants.ELE_SHIPMENT_LINES);
			String strPackListType = eleShipment.getAttribute(AcademyConstants.STR_PACK_LIST_TYPE);
			String strStatus = eleShipment.getAttribute(AcademyConstants.STATUS);
			
			NodeList nlShipmentLine = eleShipmentLines.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
			
			
			//ndListRemove contains the shipment lines that are already cancelled or not eligible for cancellation. This is to avoid sending 
			//duplicate messages to GSM
			NodeList ndListRemove = XPathUtil.getNodeList(eleShipment, AcademyConstants.XPATH_SHIPMENTLINE_TO_BE_REMOVED);
			for (int j=0;j<ndListRemove.getLength();j++) {
				Element eleShipmentLinesToRemove= (Element) ndListRemove.item(j);
				String strActualQty =  eleShipmentLinesToRemove.getAttribute(AcademyConstants.ATTR_ACTUAL_QUANTITY);
				if(!"0.00".equalsIgnoreCase(strActualQty)) {
					log.verbose("Shipment Line element to be removed :: " + XMLUtil.getElementXMLString(eleShipmentLinesToRemove));
					eleShipmentLinesToRemove.getParentNode().removeChild(eleShipmentLinesToRemove);
				}
				else  {
					NodeList ndListTag = eleShipmentLinesToRemove.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_TAG_SERIAL);
					int lenList = ndListTag.getLength();
					if (lenList == 0) {
						log.verbose("Shipment Line element to be removed :: " + XMLUtil.getElementXMLString(eleShipmentLinesToRemove));
						eleShipmentLinesToRemove.getParentNode().removeChild(eleShipmentLinesToRemove);
					}
					
				}
				
			}
			
			for (int i = 0; i < nlShipmentLine.getLength(); i++) {
				Element eleShipmentLine = (Element) nlShipmentLine.item(i);
				Element eleOrderLine = SCXmlUtil.getChildElement(eleShipmentLine, AcademyConstants.ELE_ORDER_LINE);
				String strFulfillmentType = eleOrderLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
				
				if(!YFCCommon.isVoid(strFulfillmentType) && AcademyConstants.STR_BUY_ONLINE_PICKUP_FROM_STORE.equals(strFulfillmentType) 
						&& !YFCCommon.isVoid(strPackListType) && AcademyConstants.STS_FA.equals(strPackListType)){
					bBopisFA = true;
				}
			}
			//OMNI-95645 Changes
			if(bBopisFA && AcademyConstants.STATUS_SHIPMENT_CANCELLED.equalsIgnoreCase(strStatus)){
				NodeList nlShipmentStatusAudit = XPathUtil.getNodeList(inDoc, "Shipment/ShipmentStatusAudits/ShipmentStatusAudit[@NewStatus='1100.70.06.30.5' or @NewStatus='1100.70.06.30.7']");
				iLen = nlShipmentStatusAudit.getLength(); 
				if(iLen > 0) {
					bEligibleStatus = true;
				}
			}
			else if (bBopisFA && (AcademyConstants.STR_READY_FOR_CUSTOMER_PICK_STATUS.equalsIgnoreCase(strStatus) || AcademyConstants.STR_PAPER_WORK_INITIATED_STATUS.equalsIgnoreCase(strStatus) )) {
				NodeList ndList = XPathUtil.getNodeList(eleShipment, "ShipmentLines/ShipmentLine/ShipmentTagSerials/ShipmentTagSerial");
				iLen = ndList.getLength(); 
				if(iLen > 0) {
					bEligibleStatus = true;
				}				
			}
			
			
		}
		catch(Exception ex)
		{
				YFSException e = new YFSException();
				e.setErrorDescription("Custom Dynamic Condition Returned an error");
				e.printStackTrace();
				throw e;
			
		}
		log.endTimer("AcademyValidateBopisFirearm-evaluateCondition() : End");
		return (bEligibleStatus) ;
	}


	@Override
	public void setProperties(Map arg0) {
		 //set properties will be unused here
	}

}
