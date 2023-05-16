package com.academy.ecommerce.sterling.bopis.api;

import org.w3c.dom.*;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.common.*;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.dom.*;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfc.core.YFCObject;

/**		   This class will be invoked from ON_SUCCESS of
 *         manageOrganizationHierarchy to add or remove(Active=Y/N)
 *         corresponding ShipNodes from the DGs configured in the COMMON_CODE
 *         ADD_REM_PROCURE_NODE based on the ExtnIsSFSEnabled
 *         */


public class AcademyUpdateProcurementDGForSFSFlag {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyUpdateProcurementDGForSFSFlag.class);

	public Document addOrRemoveNodeFromProcurementDG(YFSEnvironment env, Document inDoc) throws Exception {

		log.beginTimer("AcademyUpdateProcurementDGForSFSFlag::addOrRemoveNodeFromProcurementDG");
		log.verbose("Entering AcademyUpdateProcurementDGForSFSFlag.addOrRemoveNodeFromProcurementDG() with Input:: " + XMLUtil.getXMLString(inDoc));

		YFCDocument ipDoc = null;
		YFCElement eleOrg = null;
		YFCElement eleExtn = null;
		String strSFSFlag = "";
		NodeList nl =null;
		
		ipDoc = YFCDocument.getDocumentFor(inDoc);
		eleOrg = ipDoc.getDocumentElement();

		if (eleOrg.getElementsByTagName(AcademyConstants.ELE_EXTN).getLength() > 0) {
			eleExtn = eleOrg.getChildElement(AcademyConstants.ELE_EXTN);
		}

		if (eleExtn != null && eleExtn.hasAttribute(AcademyConstants.ATTR_EXTN_SFS_ENABLED)) {
			strSFSFlag = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_SFS_ENABLED);
		}

		String strNode = eleOrg.getAttribute(AcademyConstants.ORGANIZATION_CODE);

		log.verbose("ShipNode : " + strNode + " | SFSFlag : " + strSFSFlag);
		
		Document docGetCommonCodeListOP = AcademyBOPISUtil.getCommonCodeList(env, AcademyConstants.COMMONCODETYPE_ADD_REM_PROCURE_NODE,
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

			log.verbose("getDistributionRuleList Input  " + XMLUtil.getXMLString(getDistributionRuleListIP.getDocument()));

			Document DocGetDGListOp = AcademyUtil.invokeService(env,
					AcademyConstants.ACADEMY_GET_DISTRIBUTION_RULE_LIST_SERVICE,
					getDistributionRuleListIP.getDocument());

			log.verbose("getDistributionRuleList Output :: " + XMLUtil.getXMLString(DocGetDGListOp));
			
			/* XML getting stored in DocGetDGListOp is 
			 * <DistributionRuleList>
    			<DistributionRule DefaultFlag="N" Description="DG_STS_STORE" DistributionRuleId="DG_STS_STORE" DistributionRuleKey="20210721030158225317343"
        			OwnerKey="Academy_Direct" Purpose="PROCUREMENT">
        			<ItemShipNodes>
            			<ItemShipNode ActiveFlag="Y" ItemshipnodeKey="20210721030205225317346" ShipnodeKey="102"/>
            		</ItemShipNodes></DistributionRule></DistributionRuleList>
			 */
			
			YFCDocument deleteDistributionIp=null;
			YFCDocument createDistributionIp=null;
			String strItemShipNodeKey=null;
			String elePurpose=null;
			
			Element eleDistributionRule = XMLUtil.getElementByXPath(DocGetDGListOp, AcademyConstants.XPATH_DISTRIBUTIONRULE);
			elePurpose=eleDistributionRule.getAttribute(AcademyConstants.ATTR_PURPOSE);
			log.verbose("elePurpose : "+elePurpose);
			if(elePurpose.equals(AcademyConstants.PROCUREMENT))
			{
				log.verbose("strDGName : "+strDGName);
				nl = XPathUtil.getNodeList(DocGetDGListOp, AcademyConstants.XPATH_SHIPNODEKEY + strNode + AcademyConstants.CLOSING_BACKET);
				log.verbose("nodelist nl length : "+nl.getLength());
				// adding shipnode into the DG, if the flag value is Y
				if(nl.getLength()==0 && (!YFCObject.isVoid(strSFSFlag) && strSFSFlag.equals(AcademyConstants.STR_YES)))
				{
					createDistributionIp = YFCDocument.createDocument(AcademyConstants.ELE_ITEM_SHIP_NODE);
					YFCElement elecreateDistribution = createDistributionIp.getDocumentElement();
					elecreateDistribution.setAttribute(AcademyConstants.ATTR_DISTRIBUTION_RULE_ID,strDGName);
					elecreateDistribution.setAttribute(AcademyConstants.ITEM_Id,AcademyConstants.STR_ALL);
					elecreateDistribution.setAttribute(AcademyConstants.ATTR_OWENER_KEY,AcademyConstants.A_ACADEMY_DIRECT);
					elecreateDistribution.setAttribute(AcademyConstants.ATTR_SHIP_NODE_KEY,strNode);
					elecreateDistribution.setAttribute(AcademyConstants.ATTR_PURPOSE,elePurpose);
					log.verbose("createDistribution I/P : " + XMLUtil.getXMLString(createDistributionIp.getDocument()));
					AcademyUtil.invokeAPI(env, AcademyConstants.API_CREATE_DISTRIBUTION,createDistributionIp.getDocument());
				}
				// removing shipnode from the DG, if the flag value is N
				else if(nl.getLength()==1 && (!YFCObject.isVoid(strSFSFlag) && strSFSFlag.equals(AcademyConstants.STR_NO)))
				{
					Element elesShipNode =(Element) nl.item(0);
					strItemShipNodeKey=elesShipNode.getAttribute(AcademyConstants.ATTR_ITEM_SHIPNODE_KEY);
					deleteDistributionIp = YFCDocument.createDocument(AcademyConstants.ELE_ITEM_SHIP_NODE);
					YFCElement eledeleteDistribution = deleteDistributionIp.getDocumentElement();
					eledeleteDistribution.setAttribute(AcademyConstants.ATTR_DISTRIBUTION_RULE_ID,strDGName);
					eledeleteDistribution.setAttribute(AcademyConstants.ITEM_Id,AcademyConstants.STR_ALL);
					eledeleteDistribution.setAttribute(AcademyConstants.ATTR_ITEM_SHIPNODE_KEY,strItemShipNodeKey);
					eledeleteDistribution.setAttribute(AcademyConstants.ATTR_OWENER_KEY,AcademyConstants.A_ACADEMY_DIRECT);
					eledeleteDistribution.setAttribute(AcademyConstants.ATTR_SHIP_NODE_KEY,strNode);
					eledeleteDistribution.setAttribute(AcademyConstants.ATTR_PURPOSE,elePurpose);
					log.verbose("deleteDistribution I/P : " + XMLUtil.getXMLString(deleteDistributionIp.getDocument()));
					AcademyUtil.invokeAPI(env, AcademyConstants.API_DELETE_DISTRIBUTION,deleteDistributionIp.getDocument());
				}
			}
		}
		log.endTimer("AcademyUpdateProcurementDGForSFSFlag::addOrRemoveNodeFromProcurementDG");
		return inDoc;
	}
}