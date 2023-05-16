package com.academy.ecommerce.sterling.bopis.condition;

import java.util.Map;

import org.w3c.dom.Document;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyDynamicCondnForMsgToSIM implements YCPDynamicConditionEx
{
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyDynamicCondnForMsgToSIM.class);
	Map ConditionProp;
	boolean condition = false;
	@Override
	public boolean evaluateCondition(YFSEnvironment env, String arg1,
			Map arg2, Document arg3) {
		log.beginTimer("AcademyDynamicCondnForMsgToSIM.java-evaluateCondition() : Start");
		log.debug("AcademyDynamicCondnForMsgToSIM.java-evaluateCondition() : InDoc"+arg3);
		try
		{
			YFCDocument yfcInDoc = YFCDocument.getDocumentFor(arg3);
			YFCElement eleShipment = yfcInDoc.getDocumentElement();
			String strShipNode = eleShipment.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
			String strDeliveryMethod = eleShipment.getAttribute(AcademyConstants.ATTR_DELIVERY_METHOD);
			String strStatus = eleShipment.getAttribute(AcademyConstants.ATTR_STATUS);
			
			//Fetch the Node Type of the Ship node using the getOrganizationHirarchy API.
			//Do Not call getOrganizationHirarchy API if the ShipNode is Blank.
			boolean isNodeTypeStore = false;
			if(!YFCCommon.isVoid(strShipNode))
			{
				log.debug("The ShipNode is the inDoc is not Blank.Calling getOrgHirarchy");
				//Do not call getOrgHrchy if ShipNode is blank.
				isNodeTypeStore = checkNodeType(env,strShipNode);
			}
			String strDeliveryMethodCheck = (String) ConditionProp.get(AcademyConstants.ATTR_DELIVERY_METHOD);
			String strStatusValue = (String) ConditionProp.get(AcademyConstants.PROP_STATUS_VALUE); 
			//Check if the Status in the inDoc is greater than or equal to the status value in the condition property.
			boolean statusCheck = checkShipmentStatus(strStatus,strStatusValue);
			if(isNodeTypeStore && statusCheck)
			{
				condition = true;
			}
			if(YFCCommon.equalsIgnoreCase("Y", strDeliveryMethodCheck) && 
					!YFCCommon.equalsIgnoreCase(AcademyConstants.STR_PICK,strDeliveryMethod) && condition)
			{
				condition = false;
			}
		}
		catch(Exception ex)
		{
			String strErrorMessage = ex.getMessage();
			YFCDocument inEx = YFCDocument.getDocumentFor(strErrorMessage);
			String strErrorCode = inEx.getDocumentElement().getChildElement("Error").getAttribute("ErrorCode");
			if(YFCCommon.equalsIgnoreCase("YFS10001", strErrorCode))
			{
				YFSException e = new YFSException();
				e.setErrorCode(strErrorCode);
				e.setErrorDescription("ShipNode is not present in the System.");
				throw e;
			}
			else
			{
				YFSException e = new YFSException();
				e.setErrorDescription("Custom Dynamic Condition Returned an error during Message to SIM.");
				e.printStackTrace();
				throw e;
			}
			
		}
		log.endTimer("AcademyDynamicCondnForMsgToSIM.java-evaluateCondition() : End");
		return condition;

	}

	@Override
	public void setProperties(Map arg0) {
		//fetch if the Delivery method check is required.
		ConditionProp = arg0;
		
	}
	
	/**
	 * 
	 * @param env
	 * @param strShipNode
	 * @return
	 * @throws Exception
	 */
	public boolean checkNodeType(YFSEnvironment env,String strShipNode) throws Exception
	{
		log.beginTimer("AcademyDynamicCondnForMsgToSIM.java-checkNodeType() : Start");
		log.debug("AcademyDynamicCondnForMsgToSIM.java-checkNodeType() : ShipNode "+strShipNode);
		//Call getOrganizationHierarchy to fetch the Node Type 
		YFCDocument yfcInDocGetOrgHrchy = YFCDocument.createDocument(AcademyConstants.ELE_ORG);
		YFCElement eleOrgInput = yfcInDocGetOrgHrchy.getDocumentElement();
		eleOrgInput.setAttribute(AcademyConstants.ORGANIZATION_CODE,AcademyConstants.DSV_ENTERPRISE_CODE);
		eleOrgInput.setAttribute("OrganizationKey",strShipNode);
		log.debug("AcademyDynamicCondnForMsgToSIM.java-checkNodeType() : InDoc getOrganizationHirarchy "+yfcInDocGetOrgHrchy.toString());
		Document outDocGetorgHrchy = AcademyUtil.invokeService(env,"AcademyBOPISGetOrgHierarchy", yfcInDocGetOrgHrchy.getDocument());
		log.debug("AcademyDynamicCondnForMsgToSIM.java-checkNodeType() : OutDoc getOrganizationHirarchy "+outDocGetorgHrchy);
		YFCDocument yfsOutDocGetorgHrchy = YFCDocument.getDocumentFor(outDocGetorgHrchy);
		YFCElement eleOrgOutput = yfsOutDocGetorgHrchy.getDocumentElement();
		YFCElement eleNode = eleOrgOutput.getChildElement(AcademyConstants.ELE_NODE);
		String strNodeType = eleNode.getAttribute(AcademyConstants.ATTR_NODE_TYPE);
		log.debug("AcademyDynamicCondnForMsgToSIM.java-checkNodeType() :NodeType "+strNodeType);
		if(!YFCCommon.isVoid(strNodeType) && YFCCommon.equalsIgnoreCase(AcademyConstants.STR_STORE, strNodeType))
		{
			//if NodeType is not null and Equal to "Store" send true.
			log.endTimer("AcademyDynamicCondnForMsgToSIM.java-checkNodeType() : End");
			return true;
		}
		log.endTimer("AcademyDynamicCondnForMsgToSIM.java-checkNodeType() : End");
		return false;
	}
	
	/**
	 * This method compares two statuses.
	 * @param StrShipmentStatus
	 * @param strStatusVal
	 * @return
	 */
	public boolean checkShipmentStatus(String StrShipmentStatus,String strStatusVal)
	{
		log.beginTimer("AcademyDynamicCondnForMsgToSIM.java-checkShipmentStatus() : Start");
		Integer anInt = compareStatus(StrShipmentStatus, strStatusVal);
		if(anInt == 0)
		{
			return true;
		}
		else if(anInt > 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * THis method compares statuses without using the == operator.
	 * @param status1 yfs_status code
	 * @param status2 yfs_status code
	 * @return
	 * if status1 == status2, it returns 0
	 * else if status1 > status2, it returns positive integer
	 * else if status1 < status2, it returns negative integer
	 */
	public static Integer compareStatus(String status1, String status2) {
		int comparisonNumber = YFCCommon.compareStrings(status1, status2);
		if(comparisonNumber == 0){
			return 0;
		}else if(!YFCCommon.isVoid(status1) && YFCCommon.isVoid(status2)){
			return 1;
		}else if(YFCCommon.isVoid(status1) && !YFCCommon.isVoid(status2)){
			return -1;
		}
		
		String[] splittedStatus1 = {status1};
		if(status1.indexOf('.') > 0){
			splittedStatus1 = status1.split("\\.");
		}
		
		String[] splittedStatus2 = {status2};
		if(status2.indexOf('.') > 0){
			splittedStatus2 = status2.split("\\.");
		}
		
		int minSubStatusNo = Math.min(splittedStatus1.length, splittedStatus2.length);
		int i = 0;
		for(;i < minSubStatusNo; i++){
			comparisonNumber = Integer.parseInt(splittedStatus1[i]) - Integer.parseInt(splittedStatus2[i]);
			if(comparisonNumber != 0){
				return comparisonNumber;
			}
		}
		
		if(splittedStatus1.length == splittedStatus2.length){
			return 0;
		}else if(splittedStatus1.length > splittedStatus2.length){
			return 1;
		}else if(splittedStatus1.length < splittedStatus2.length){
			return -1;
		}
			
		return null;
	}
	
}
