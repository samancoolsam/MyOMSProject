package com.academy.ecommerce.sterling.bopis.condition;

import java.io.IOException;
import java.io.StringReader;
import java.text.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.academy.ecommerce.sterling.bopis.api.AcademyAddRemoveNodeFromDG;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.*;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/*##################################################################################
*
* Project Name                : R3 Release
* Module                      : OMS
* Author                      : CTS-POD
* Date                        : 08-AUG-2019
* Description				  : changes done in this class does following 
* 								 1.It is updated as per OMNI 449,489.
*                                2.Logic updated to remove the upper limit check for 
*                                  Audit Key  	
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author         		Version#       Remarks/Description                      
* ---------------------------------------------------------------------------------
*08-AUG-2019	  CTS-POD  	 			  2.0           changed version
* ##################################################################################*/

public class AcademyCheckLastOrgUpdate implements YCPDynamicConditionEx {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyAddRemoveNodeFromDG.class);

	@SuppressWarnings("rawtypes")
	Map _properties = new HashMap();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void setProperties(Map map) {
		System.out.println("CustomDynamicConditionEx:setProperties:" + map);
		if (map != null && !map.isEmpty()) {
			_properties.putAll(map);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean evaluateCondition(YFSEnvironment env, String name, Map mapData, Document inXML) {

		log.beginTimer("AcademyCheckLastOrgUpdate::evaluateCondition");

		boolean boolConditionPassed = false;
		mapData = _properties;
		String strCheckAttr = (String) mapData.get("CheckAttribute");
		String strCheckTime = (String) mapData.get("CheckTime");

		log.verbose("CheckAttribute : " + strCheckAttr);
		log.verbose("CheckTime : " + strCheckTime);

		YFCDocument inDoc = YFCDocument.getDocumentFor(inXML);
		YFCElement inEle = inDoc.getDocumentElement();

		String strNode = inEle.getAttribute(AcademyConstants.ORGANIZATION_CODE);
		log.verbose("ShipNode : " + strNode);

		try {
			Document opDoc = getAuditList(env, strNode, strCheckTime);

			YFCDocument DocGetAuditListOp = YFCDocument.getDocumentFor(opDoc);
			YFCElement eleGetAuditListOp = DocGetAuditListOp.getDocumentElement();

			// Fetch the Latest Audit from the List - Already Sorted in API Output
			String strAuditXml = eleGetAuditListOp.getElementsByTagName("Audit").item(0).getAttribute("AuditXml");

			log.verbose("AuditXML Attribute : " + strAuditXml);

			Document docAuditXML = getDocumentFromString(strAuditXml);

			Element eleItemShipNodeDGList = XMLUtil.getElementByXPath(docAuditXML,
					"/AuditDetail/Attributes/Attribute[@Name='" + strCheckAttr + "']");

			if (!YFCObject.isVoid(eleItemShipNodeDGList)) {

				boolConditionPassed = true;
			}

			log.verbose("boolConditionPassed : " + boolConditionPassed);

		} catch (Exception e) {
			log.verbose(e.getStackTrace().toString());
			e.printStackTrace();
		}

		log.endTimer("AcademyCheckLastOrgUpdate::evaluateCondition");

		return boolConditionPassed;
	}

	public Document getAuditList(YFSEnvironment env, String Node, String strCheckTime) throws Exception {
		log.verbose("In method to invoke getAuditList");

		SimpleDateFormat DateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

		Date dFromDate = new Date();
		Calendar fromCal = Calendar.getInstance();
		fromCal.setTime(dFromDate);
		fromCal.add(Calendar.SECOND, -Integer.parseInt(strCheckTime));
		String strFromDate = DateFormat.format(fromCal.getTime());

		log.verbose("FromAuditKey : " + strFromDate);
		
		/*Start of OMNI 449,489 */		  
		/*Date dToDate = new Date();
		Calendar toCal = Calendar.getInstance();
		toCal.setTime(dToDate);
		toCal.add(Calendar.SECOND, Integer.parseInt(strToCheckTime));
		String strToDate = DateFormat.format(toCal.getTime());

		log.verbose("ToAuditKey : " + strToDate); */
		
		YFCDocument docGetAuditListIp = YFCDocument.createDocument("Audit");
		YFCElement eleGetAuditListIp = docGetAuditListIp.getDocumentElement();
		eleGetAuditListIp.setAttribute("AuditKeyQryType", "GT");
		eleGetAuditListIp.setAttribute("TableKey", Node);
		eleGetAuditListIp.setAttribute("TableName", "YFS_ORGANIZATION");
		eleGetAuditListIp.setAttribute("AuditKey", strFromDate); 
		//eleGetAuditListIp.setAttribute("ToAuditKey", strToDate); 
		/*End of OMNI 449,489*/
		

		YFCElement eleOrderBy = eleGetAuditListIp.createChild(AcademyConstants.ELE_ORDERBY);
		YFCElement eleAttribute = eleOrderBy.createChild(AcademyConstants.ELE_ATTRIBUTE);
		eleAttribute.setAttribute(AcademyConstants.ATTR_DESC_SHORT, AcademyConstants.STR_YES);
		eleAttribute.setAttribute(AcademyConstants.ATTR_NAME, "AuditKey");

		log.verbose("Input to getAuditList API : " + XMLUtil.getXMLString(docGetAuditListIp.getDocument()));

		String strApiTemplate = "<AuditList>\r\n"
				+ "	<Audit AuditKey=\"\" AuditXml=\"\" TableKey=\"\" TableName=\"\"/>\r\n" + "</AuditList>";

		log.verbose("getAuditList Api Template : " + strApiTemplate);

		env.setApiTemplate("getAuditList", XMLUtil.getDocument(strApiTemplate));
		Document docGetAuditListOp = AcademyUtil.invokeAPI(env, "getAuditList", docGetAuditListIp.getDocument());
		env.clearApiTemplate("getAuditList");

		log.verbose("Output of getAuditList API : " + XMLUtil.getXMLString(docGetAuditListOp));

		return docGetAuditListOp;
	}

	public Document getDocumentFromString(String stringXML)
			throws ParserConfigurationException, SAXException, IOException {
		Document docOutput = null;
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(stringXML));

		docOutput = db.parse(is);
		return docOutput;
	}
}