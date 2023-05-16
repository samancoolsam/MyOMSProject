package com.academy.ecommerce.sterling.bopis.api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyBOPISUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * This class is responsible to Update ActiveFlag of a Node in DistributionGroup based on Any Address Flag at ShipNode level.
 * @author Sanchit
 *
 */

public class AcademyAddRemoveNodeFromDG {
	
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyAddRemoveNodeFromDG.class);
	
	/**
	 * This method is responsible to update ActiveFlag of a Node In DG based on the update done for Any Address flag for a shipNode.
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */

	public Document addRemoveNodeFromDG (YFSEnvironment env, Document inDoc) throws Exception{
		log.beginTimer("AcademyAddRemoveNodeFromDG::addRemoveNodeFromDG");
		log.verbose("Entering AcademyAddRemoveNodeFromDG.addRemoveNodeFromDG() :: "+XMLUtil.getXMLString(inDoc));
		
		YFCDocument yfcDocInput = YFCDocument.getDocumentFor(inDoc);
		YFCElement eleOrganization = yfcDocInput.getDocumentElement();
		YFCElement eleNode=eleOrganization.getChildElement(AcademyConstants.ELE_NODE);
		boolean manageDistributionRulecallrequired=false;
		String strCanShiptoOtherAddress="";
		YFCDocument manageDistributionRuleIP=null;
		
		YFCDocument getDistributionRuleListIP = YFCDocument.createDocument(AcademyConstants.ELE_DISTRIBUTION_RULE);
		
		
		
		
		
		
		if(!YFCObject.isVoid(eleNode)){
		strCanShiptoOtherAddress=eleNode.getAttribute(AcademyConstants.ATTR_CAN_SHIP_TO_OTHER_ADDRESS);
		}
		String strShipNode=eleOrganization.getAttribute(AcademyConstants.ATTR_ORGANIZATION_KEY);
		
		Document docOutGetCommonCodeList = AcademyBOPISUtil.getCommonCodeList
				(env, AcademyConstants.STR_ADD_REM_NODE_DGS, AcademyConstants.PRIMARY_ENTERPRISE);
		
		YFCDocument yfsOutGetCommonCodeList = YFCDocument.getDocumentFor(docOutGetCommonCodeList);
		YFCElement elegetCommoncodeList = yfsOutGetCommonCodeList.getDocumentElement();
		YFCNodeList<YFCElement> elecoommoncode= elegetCommoncodeList.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);
		
		 for(YFCElement eleCommonCodeoutput : elecoommoncode){
			 
			 String strDistributioGroup=eleCommonCodeoutput.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE);
			 log.verbose("*******Distribution Group*******" + strDistributioGroup);
			 
			 
			 YFCElement eleDistributionRuleList = getDistributionRuleListIP.getDocumentElement();
			 eleDistributionRuleList.setAttribute(AcademyConstants.ATTR_DISTRIBUTION_RULE_ID,strDistributioGroup);
			 
			 log.verbose("getDistributionRuleList Input  " + XMLUtil.getXMLString(getDistributionRuleListIP.getDocument()));
			 
			 Document getDistributionRuleListOP=AcademyUtil.invokeService
					 (env, AcademyConstants.ACADEMY_GET_DISTRIBUTION_RULE_LIST_SERVICE, getDistributionRuleListIP.getDocument());
			 
			 log.verbose("getdistributionList Output :: "+XMLUtil.getXMLString(getDistributionRuleListOP));
			 Element eleItemShipNodeDGList = XMLUtil.getElementByXPath
						(getDistributionRuleListOP, "/DistributionRuleList/DistributionRule/ItemShipNodes/ItemShipNode[@ShipnodeKey='"+strShipNode+"']");
				
			 
			 if (!YFCObject.isVoid(eleItemShipNodeDGList)){
				 
				 String strActiveFlag = eleItemShipNodeDGList.getAttribute(AcademyConstants.ATTR_ACTIVE_FLAG);
				 
				 manageDistributionRuleIP = YFCDocument.createDocument(AcademyConstants.ELE_DISTRIBUTION_RULE);
				 YFCElement elemanageDistributionRule = manageDistributionRuleIP.getDocumentElement();
				 elemanageDistributionRule.setAttribute(AcademyConstants.ATTR_OWENER_KEY, AcademyConstants.PRIMARY_ENTERPRISE);
				 elemanageDistributionRule.setAttribute(AcademyConstants.ATTR_DISTRIBUTION_RULE_ID,strDistributioGroup);
				 elemanageDistributionRule.setAttribute(AcademyConstants.ATTR_PURPOSE,AcademyConstants.STR_SOURCING);
				 elemanageDistributionRule.setAttribute(AcademyConstants.ATTR_ITEM_GROUP_CODE,AcademyConstants.STR_PROD);
				 YFCElement eleItemShipNodes =elemanageDistributionRule.createChild(AcademyConstants.ELE_ITEM_SHIP_NODES);
				 eleItemShipNodes.setAttribute(AcademyConstants.ATTR_RESET, AcademyConstants.STR_NO);
				 YFCElement eleItemShipNode =eleItemShipNodes.createChild(AcademyConstants.ELE_ITEM_SHIP_NODE);
				 eleItemShipNode.setAttribute(AcademyConstants.ATTR_SHIP_NODE_KEY,strShipNode);
				 eleItemShipNode.setAttribute(AcademyConstants.ATTR_ITEM_TYPE,AcademyConstants.STR_ALL);
				 eleItemShipNode.setAttribute(AcademyConstants.ITEM_Id,AcademyConstants.STR_ALL);
				 
				 if(!YFCObject.isVoid(strCanShiptoOtherAddress)&&strCanShiptoOtherAddress.equalsIgnoreCase(AcademyConstants.STR_YES)
						 && !(strActiveFlag.equalsIgnoreCase(AcademyConstants.STR_YES))){
					 
					manageDistributionRulecallrequired=true;
					eleItemShipNode.setAttribute(AcademyConstants.ATTR_ACTIVE_FLAG,AcademyConstants.STR_YES);
				 }
				 
                 if(!YFCObject.isVoid(strCanShiptoOtherAddress)&&strCanShiptoOtherAddress.equalsIgnoreCase(AcademyConstants.STR_NO)
                		 &&!(strActiveFlag.equalsIgnoreCase(AcademyConstants.STR_NO))){
					 
                	 manageDistributionRulecallrequired=true;
                	 eleItemShipNode.setAttribute(AcademyConstants.ATTR_ACTIVE_FLAG,AcademyConstants.STR_NO);
				}
				 
                 
			 }
			 
			 if(manageDistributionRulecallrequired){
			AcademyUtil.invokeAPI(env,AcademyConstants.API_MANAGE_DISTRIBUTION_RULE, manageDistributionRuleIP.getDocument());
			 }
			 
		
		
		 }
		 log.endTimer("AcademyAddRemoveNodeFromDG::addRemoveNodeFromDG");
		 
		 
		 Document docoutput = XMLUtil.getDocument("<ApiSuccess />");
		 return docoutput;
		

	}

}
