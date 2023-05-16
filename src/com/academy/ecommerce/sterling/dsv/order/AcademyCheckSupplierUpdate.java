package com.academy.ecommerce.sterling.dsv.order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.ibm.icu.util.Calendar;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**#########################################################################################
 *
 * Project Name                : DSV
 * Author                      : Fulfillment POD
 * Author Group				  : DSV
 * Date                        : 26-FEB-2023 
 * Description				  : This class checks if the order is eligible for Supplier Update
 * 								to Firestore DB for DSV Nodes (with CHUB)
 * 								 
 * ---------------------------------------------------------------------------------
 * Date            	Author         		Version#       		Remarks/Description                      
 * ---------------------------------------------------------------------------------
 * 27-Feb-2023		Everest  	 		1.0           		Initial version
 *
 * #########################################################################################*/

public class AcademyCheckSupplierUpdate implements YCPDynamicConditionEx {

	private static YFCLogCategory logger = YFCLogCategory.instance(AcademyCheckSupplierUpdate.class);
	Map _properties = new HashMap();

	/**
	 * @param env
	 * @param name
	 * @param mapData
	 * @param inDoc
	 * @return Boolean
	 * @throws Exception
	 * 
	 * Validate the Organization details and see if it is eligible for Supplier
	 * Code Update to Firestore DB via Kafka
	 * 
	 * Input : <Organization Operation="Manage" OrganizationCode=""
	 * OrganizationName="" /> name is the name of the condition configured in the
	 * database mapData contains the name-value pair of strings. Keys are sam
	 * 
	 */ 
	public boolean evaluateCondition(YFSEnvironment env, String name, Map mapData, Document inDoc) {
		logger.beginTimer("AcademyCheckSupplierUpdate.evaluateCondition method ");

		boolean hasSupplierChanged = false;
		try {
			//mapData = _properties;
			logger.verbose("CustomDynamicConditionEx:_properties:" + _properties);
			String strOrganizationCode = inDoc.getDocumentElement().getAttribute(AcademyConstants.ORGANIZATION_CODE);
			String strXRefOrganizationCode = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_XREF_ORGANIZATION_CODE);
			String strNodeType = null;

			Element eleNode = SCXmlUtil.getChildElement(inDoc.getDocumentElement(), AcademyConstants.ELE_NODE);
			if(!YFCObject.isVoid(eleNode)) {
				strNodeType = eleNode.getAttribute(AcademyConstants.A_NODE_TYPE);
			}
			
			logger.verbose(": strNodeType : " + strNodeType + ": strXRefOrganizationCode : " + strXRefOrganizationCode);

			if(!YFCObject.isVoid(strOrganizationCode) && (YFCObject.isVoid(strNodeType) 
					|| (AcademyConstants.DSV_NODE_TYPE.equalsIgnoreCase(strNodeType) && YFCObject.isVoid(strXRefOrganizationCode)))) {
				Document docGetOrganizationList = getOrganizationList(env, strOrganizationCode);
				strXRefOrganizationCode = XPathUtil.getString(docGetOrganizationList, "/OrganizationList/Organization/@XrefOrganizationCode");
				strNodeType = XPathUtil.getString(docGetOrganizationList, "/OrganizationList/Organization/Node/@NodeType");
			}
			else {
				logger.verbose("None of the conditions are satisfied. So skipping getOrganizaitonList API");
			}

			//Check if the  Organization Code has XRefOrganizationCode = CHUB
			if (strXRefOrganizationCode != null && AcademyConstants.STR_CHUB.equalsIgnoreCase(strXRefOrganizationCode)) {
				// the keys of mapData are the same as the variables available in condition builder
				String strAttributeName = (String) _properties.get("AttributeName");
				String strTimeStamp = (String) _properties.get(AcademyConstants.VENDOR_TIMESTAMP);
				String strAuditXml = null;

				logger.verbose("Validating if the CHUB Organization has been modified");
				Document docAuditListOut = getAuditList(env, strOrganizationCode, strTimeStamp);

				Element eleAudit = (Element) docAuditListOut.getDocumentElement().getElementsByTagName("Audit").item(0);

				if(null != eleAudit) {
					strAuditXml = eleAudit.getAttribute("AuditXml");
					logger.verbose("AuditXML Attribute : " + strAuditXml);

					Document docAuditXML = SCXmlUtil.createFromString(strAuditXml);

					Element eleItemShipNodeDGList = XMLUtil.getElementByXPath(docAuditXML,
							"/AuditDetail/Attributes/Attribute[@Name='" + strAttributeName + "']");

					if (!YFCObject.isNull(eleItemShipNodeDGList)) {
						logger.verbose("Org has been modified");
						hasSupplierChanged = true;
					}
				}

			}

		} catch (Exception ex) {
			throw new RuntimeException("DynamicConditionEx failed");
		}
		logger.endTimer("AcademyCheckSupplierUpdate.evaluateCondition method ");
		return hasSupplierChanged;
	}


	/**
	 * @param env
	 * @param strOrganizationCode
	 * @param strTimeStamp
	 * @return Document
	 * @throws Exception
	 * 
	 * Validate the Organization Audits to see if the OrganizationCode has any modifications recently
	 * 
	 */ 
	private Document getAuditList(YFSEnvironment env, String strOrganizationCode, String strTimeStamp) throws Exception {
		logger.beginTimer("AcademyCheckSupplierUpdate.getAuditList method ");

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date dCurrentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dCurrentDate);
		calendar.add(Calendar.SECOND, -Integer.parseInt(strTimeStamp));
		String strFromDate = simpleDateFormat.format(calendar.getTime());

		// getAuditList API call --> fetch list of audit records satisfying below
		// criteria.

		Document docGetAuditListInp = XMLUtil.createDocument("Audit");
		Element eleAuditInp = docGetAuditListInp.getDocumentElement();
		eleAuditInp.setAttribute("AuditKey", strFromDate);
		eleAuditInp.setAttribute("AuditKeyQryType", "GT");
		eleAuditInp.setAttribute("TableKey", strOrganizationCode);
		eleAuditInp.setAttribute("TableName", "YFS_ORGANIZATION");
		Element eleOrderBy = docGetAuditListInp.createElement(AcademyConstants.ELE_ORDERBY);
		Element eleAttribute =docGetAuditListInp.createElement(AcademyConstants.ELE_ATTRIBUTE);
		eleAttribute.setAttribute(AcademyConstants.ATTR_NAME, "AuditKey");
		eleAttribute.setAttribute(AcademyConstants.ATTR_DESC_SHORT, AcademyConstants.STR_YES);

		eleOrderBy.appendChild(eleAttribute);
		eleAuditInp.appendChild(eleOrderBy);

		String strAuditListTemplate = "<AuditList><Audit AuditKey='' AuditXml='' TableKey='' TableName=''/></AuditList>";

		env.setApiTemplate("getAuditList", XMLUtil.getDocument(strAuditListTemplate));
		Document docGetAuditListOut = AcademyUtil.invokeAPI(env, "getAuditList", docGetAuditListInp);
		env.clearApiTemplate("getAuditList");

		logger.endTimer("AcademyCheckSupplierUpdate.getAuditList method ");
		return docGetAuditListOut;
	}

	/**
	 * @param env
	 * @param strOrganizationCode
	 * @return Document
	 * @throws Exception
	 * 
	 * This method Invokes the getOrganizationList to get the details of the Org
	 * 
	 */ 
	private Document getOrganizationList(YFSEnvironment env, String strOrganizationCode) throws Exception {
		logger.beginTimer("AcademyCheckSupplierUpdate.getOrganizationList method ");

		Document docGetOrgListInp = XMLUtil.createDocument(AcademyConstants.ELE_ORG);
		Element eleOrganizationInp = docGetOrgListInp.getDocumentElement();
		eleOrganizationInp.setAttribute(AcademyConstants.ORG_CODE_ATTR, strOrganizationCode);

		String strAuditListTemplate = "<OrganizationList><Organization OrganizationCode='' OrganizationName='' XrefOrganizationCode='' >"
				+ "<Node NodeType='' /></Organization></OrganizationList>";

		env.setApiTemplate(AcademyConstants.API_GET_ORGANIZATION_LIST, XMLUtil.getDocument(strAuditListTemplate));
		Document docGetOrganizationListOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORGANIZATION_LIST, docGetOrgListInp);
		env.clearApiTemplate(AcademyConstants.API_GET_ORGANIZATION_LIST);

		logger.endTimer("AcademyCheckSupplierUpdate.getOrganizationList method ");
		return docGetOrganizationListOut;
	}

	
	/**
	 * @param map
	 * @throws Exception
	 * 
	 * This method reads the data present in the properties at the condition
	 * 
	 */ 
	public void setProperties(Map map) {
		logger.verbose("CustomDynamicConditionEx:setProperties:" + map);
		if (map != null && !map.isEmpty()) {
			_properties.putAll(map);
		}
	}


}
