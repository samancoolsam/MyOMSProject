package com.academy.ecommerce.sterling.inventory.condition;

import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author Cognizant
 * This class is invoked as part of dynamic condition using sterling framework to
 * verify if the item getting created is already existing in OMS db or not.
 * if it is present this class returns TRUE else FALSE
 *
 */
public class AcademyCheckIfItemIsAlreadyCreated implements YCPDynamicConditionEx {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyCheckIfItemIsAlreadyCreated.class);

	@SuppressWarnings("rawtypes")
	@Override
	public boolean evaluateCondition(YFSEnvironment env, String arg1, Map arg2, Document inDoc) {

		log.beginTimer("AcademyCheckIfItemIsAlreadyCreated::evaluateCondition");
		log.verbose("Entering the method AcademyCheckIfItemIsAlreadyCreated.evaluateCondition ");
		log.verbose("Input XML for evaluateCondition() ==> " + XMLUtil.getXMLString(inDoc));
		String strGetItemListTemplate = "<ItemList TotalNumberOfRecords=\"\" >\r\n" + "<Item />\r\n" + "</ItemList>";
		Element eleItemListFromInDoc = inDoc.getDocumentElement();
		Element eleItemFromInDoc = (Element) eleItemListFromInDoc
				.getElementsByTagName(AcademyConstants.ITEM).item(0);
		String strItemID=eleItemFromInDoc.getAttribute(AcademyConstants.ATTR_ITEM_ID);
		String strOrgCode=eleItemFromInDoc.getAttribute(AcademyConstants.ORGANIZATION_CODE);
		String strUOM=eleItemFromInDoc.getAttribute(AcademyConstants.ATTR_UOM);
		Document docgetItemListInput = null;
		try {
			docgetItemListInput = XMLUtil.createDocument(AcademyConstants.ITEM);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		Element eleItem = (Element) docgetItemListInput.getDocumentElement();
		eleItem.setAttribute(AcademyConstants.ATTR_ITEM_ID, strItemID);
		eleItem.setAttribute(AcademyConstants.ORGANIZATION_CODE, strOrgCode);
		eleItem.setAttribute(AcademyConstants.ATTR_UOM, strUOM);
		env.setApiTemplate(AcademyConstants.API_GET_ITEM_LIST, strGetItemListTemplate);
		Document getItemListOutput = null;
		try {
			log.verbose("Calling the getOrderList API...");
			getItemListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ITEM_LIST,
					docgetItemListInput);
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.verbose("Output XML for getItemList() ==> " + XMLUtil.getXMLString(getItemListOutput));
		Element elegetItemListOP = getItemListOutput.getDocumentElement();
		int itotalnumberofRecords = Integer.parseInt(elegetItemListOP.getAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS));
		if(itotalnumberofRecords==0){
			log.verbose("Condition Returns False...");
			return false;
		}
		log.verbose("Condition Returns True...");
		return true;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void setProperties(Map arg0) {
	}
}
