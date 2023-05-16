package com.academy.ecommerce.sterling.bopis.api;

import org.w3c.dom.*;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.common.*;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.*;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author Mohamed Shaikna
 * 
 *         BOPIS-2025 - This class will be invoked from ON_SUCCESS of
 *         manageOrganizationHierarchy to add or remove (Active=Y/N)
 *         corresponding ShipNodes from the DGs configured in the COMMON_CODE
 *         ADD_REM_NODE_DGs
 */

public class AcademyUpdateDGForSFSFlag {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyUpdateDGForSFSFlag.class);

	public Document addOrRemoveNodeFromDG(YFSEnvironment env, Document inDoc) throws Exception {

		log.beginTimer("AcademyUpdateDGForSFSFlag::addOrRemoveNodeFromDG");
		log.verbose("Entering AcademyUpdateDGForSFSFlag.addOrRemoveNodeFromDG() with Input:: "
				+ XMLUtil.getXMLString(inDoc));

		YFCDocument ipDoc = null;
		YFCElement eleOrg = null;
		YFCElement eleExtn = null;
		YFCDocument manageDistributionRuleIP = null;
		boolean boolManageDGCallReq = false;
		String strSFSFlag = "";

		ipDoc = YFCDocument.getDocumentFor(inDoc);
		eleOrg = ipDoc.getDocumentElement();

		if (eleOrg.getElementsByTagName("Extn").getLength() > 0) {
			eleExtn = eleOrg.getChildElement(AcademyConstants.ELE_EXTN);
		}

		if (eleExtn != null && eleExtn.hasAttribute("ExtnIsSFSEnabled")) {
			strSFSFlag = eleExtn.getAttribute("ExtnIsSFSEnabled");
		}

		String strNode = eleOrg.getAttribute(AcademyConstants.ORGANIZATION_CODE);

		log.verbose("ShipNode : " + strNode + " | SFSFlag : " + strSFSFlag);

		Document docGetCommonCodeListOP = AcademyBOPISUtil.getCommonCodeList(env, "ADD_REM_NODE_DG_SFS",
				AcademyConstants.PRIMARY_ENTERPRISE);

		log.verbose("CommonCode OP : " + XMLUtil.getXMLString(docGetCommonCodeListOP));

		YFCDocument yfsGetCommonCodeListOP = YFCDocument.getDocumentFor(docGetCommonCodeListOP);
		YFCElement eleGetCommonCodeList = yfsGetCommonCodeListOP.getDocumentElement();
		YFCNodeList<YFCElement> eleListCommonCode = eleGetCommonCodeList
				.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);

		for (YFCElement eleCommonCodeOp : eleListCommonCode) {

			log.verbose("Looping through Common Code to iterate all DGs");

			YFCDocument getDistributionRuleListIP = null;
			String strDGName = eleCommonCodeOp.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE);

			log.verbose("DGName :: " + strDGName);

			getDistributionRuleListIP = YFCDocument.createDocument(AcademyConstants.ELE_DISTRIBUTION_RULE);
			YFCElement eleDistributionRuleList = getDistributionRuleListIP.getDocumentElement();
			eleDistributionRuleList.setAttribute(AcademyConstants.ATTR_DISTRIBUTION_RULE_ID, strDGName);

			log.verbose(
					"getDistributionRuleList Input  " + XMLUtil.getXMLString(getDistributionRuleListIP.getDocument()));

			Document DocGetDGListOp = AcademyUtil.invokeService(env,
					AcademyConstants.ACADEMY_GET_DISTRIBUTION_RULE_LIST_SERVICE,
					getDistributionRuleListIP.getDocument());

			log.verbose("getDistributionRuleList Output :: " + XMLUtil.getXMLString(DocGetDGListOp));

			Element eleItemShipNodeDGList = XMLUtil.getElementByXPath(DocGetDGListOp,
					"/DistributionRuleList/DistributionRule/ItemShipNodes/ItemShipNode[@ShipnodeKey='" + strNode
							+ "']");

			if (!YFCObject.isVoid(eleItemShipNodeDGList)) {
				log.verbose("ShipNode is already part of DG irrespective of its ActiveFlag. "
						+ "Adding ShipNode to DG is not part of Design");

				String strActiveFlag = eleItemShipNodeDGList.getAttribute(AcademyConstants.ATTR_ACTIVE_FLAG);

				manageDistributionRuleIP = null;
				manageDistributionRuleIP = YFCDocument.createDocument(AcademyConstants.ELE_DISTRIBUTION_RULE);
				YFCElement elemanageDistributionRule = manageDistributionRuleIP.getDocumentElement();
				elemanageDistributionRule.setAttribute(AcademyConstants.ATTR_OWENER_KEY,
						AcademyConstants.PRIMARY_ENTERPRISE);
				elemanageDistributionRule.setAttribute(AcademyConstants.ATTR_DISTRIBUTION_RULE_ID, strDGName);
				elemanageDistributionRule.setAttribute(AcademyConstants.ATTR_PURPOSE, AcademyConstants.STR_SOURCING);
				elemanageDistributionRule.setAttribute(AcademyConstants.ATTR_ITEM_GROUP_CODE,
						AcademyConstants.STR_PROD);
				YFCElement eleItemShipNodes = elemanageDistributionRule
						.createChild(AcademyConstants.ELE_ITEM_SHIP_NODES);
				eleItemShipNodes.setAttribute(AcademyConstants.ATTR_RESET, AcademyConstants.STR_NO);
				YFCElement eleItemShipNode = eleItemShipNodes.createChild(AcademyConstants.ELE_ITEM_SHIP_NODE);
				eleItemShipNode.setAttribute(AcademyConstants.ATTR_SHIP_NODE_KEY, strNode);
				eleItemShipNode.setAttribute(AcademyConstants.ATTR_ITEM_TYPE, AcademyConstants.STR_ALL);
				eleItemShipNode.setAttribute(AcademyConstants.ITEM_Id, AcademyConstants.STR_ALL);

				if (strSFSFlag.equalsIgnoreCase(AcademyConstants.STR_YES)
						&& (strActiveFlag.equalsIgnoreCase(AcademyConstants.STR_NO))) {
					boolManageDGCallReq = true;
					eleItemShipNode.setAttribute(AcademyConstants.ATTR_ACTIVE_FLAG, AcademyConstants.STR_YES);
				}

				if (strSFSFlag.equalsIgnoreCase(AcademyConstants.STR_NO)
						&& (strActiveFlag.equalsIgnoreCase(AcademyConstants.STR_YES))) {
					boolManageDGCallReq = true;
					eleItemShipNode.setAttribute(AcademyConstants.ATTR_ACTIVE_FLAG, AcademyConstants.STR_NO);
				}
			}

			if (boolManageDGCallReq) {
				log.verbose("Condition Matched to invoke manageDistributionRule API");

				log.verbose(
						"manageDistributionRule I/P : " + XMLUtil.getXMLString(manageDistributionRuleIP.getDocument()));

				AcademyUtil.invokeAPI(env, AcademyConstants.API_MANAGE_DISTRIBUTION_RULE,
						manageDistributionRuleIP.getDocument());
			}
		}
		
		/*
		 * OMNI-34707  : BEGIN
		 * 
		 * The below code change is added to publish the update to Yantriks whenever there is a flag update for SFS fulfillment type
		 * for a particular location. 
		 */
		log.verbose("Sending  Yantriks Fulfillment Update for SFS to Queue");
		inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_FLAG_UPDATE, AcademyConstants.V_FULFILLMENT_TYPE_SFS);
		AcademyUtil.invokeService(env, AcademyConstants.SERVICE_YANTRIKS_LOCATION_FULFILLMENT_UPDATE, inDoc);
		inDoc.getDocumentElement().removeAttribute(AcademyConstants.ATTR_FLAG_UPDATE);
		log.verbose("SFS Message Published to Queue");
		//OMNI-34707  : BEGIN
		
		log.endTimer("AcademyUpdateDGForSFSFlag::addOrRemoveNodeFromDG");
		return inDoc;
	}
}