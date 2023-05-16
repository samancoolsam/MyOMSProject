package com.academy.ecommerce.sterling.condition;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyCheckIfShipNodeIsStore implements YCPDynamicConditionEx {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyCheckIfShipNodeIsStore.class);
	private Map propMap = null;
	Document getOrderListInDoc = null;
	boolean bIsStore = false;
	private final String strGetOrganizationHierarchyTemplate = "<Organization OrganizationCode=''> <Node NodeType='' /></Organization>";

	/**
	 * OMNI-46029 This method return true if the ShipNode is of NodeType "Store"
	 */
	public boolean evaluateCondition(YFSEnvironment env, String str, Map map, Document inXml) {
		log.beginTimer("AcademyCheckIfShipNodeIsStore-> evaluateCondition - START");
		log.verbose("Input XML to AcademyCheckIfShipNodeIsStore::evaluateCondition() " + XMLUtil.getXMLString(inXml));
		try {

			Element eleInXML = inXml.getDocumentElement();
			String strShipNode = eleInXML.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
			Document docOutputTemplate = null;
			Document docGetOrgDtls = null;
			docGetOrgDtls = XMLUtil.createDocument(AcademyConstants.ORG_ELEMENT);
			docGetOrgDtls.getDocumentElement().setAttribute(AcademyConstants.ORG_CODE_ATTR, strShipNode);
			docOutputTemplate = YFCDocument.getDocumentFor(strGetOrganizationHierarchyTemplate).getDocument();
			env.setApiTemplate(AcademyConstants.GET_ORG_HIERARCHY_API, docOutputTemplate);
			Document docOutputOrgDtls = AcademyUtil.invokeAPI(env, AcademyConstants.GET_ORG_HIERARCHY_API,
					docGetOrgDtls);
			// Clear template
			env.clearApiTemplates();
			String isDCOrStore = XMLUtil.getString(docOutputOrgDtls.getDocumentElement(),
					"//Organization/Node/@NodeType");
			log.verbose("Is Store Or DC : " + isDCOrStore);
			if (AcademyConstants.STR_STORE.equals(isDCOrStore)) {
				bIsStore = true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		log.verbose("Is Store?" + bIsStore);
		return bIsStore;

	}

	public void setProperties(Map propMap) {
		this.propMap = propMap;
	}
}