package com.academy.ecommerce.sterling.bopis.sfspacking.api;

import org.w3c.dom.Document;

import com.academy.util.common.AcademyUtil;

import com.academy.util.constants.AcademyConstants;

import com.academy.util.xml.XMLUtil;

import com.sterlingcommerce.baseutil.SCXmlUtil;

import com.yantra.yfc.dom.YFCDocument;

import com.yantra.yfc.log.YFCLogCategory;

import com.yantra.yfs.japi.YFSEnvironment;

/**
 * 
 * 
 * Input to the class
 * 
 * 
 * 
 * 
 * 
 * <ItemList> <Item IsShippingCntr="" ItemID="" ItemKey="">
 * 
 * 
 * <PrimaryInformation Description="" ItemType=""/> </Item> </ItemList>
 * 
 * 
 * 
 * 
 * 
 * @author Everest
 *
 * 
 * 
 * 
 * 
 */

public class AcademyGetToggleForContDropDown {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyGetToggleForContDropDown.class);

	public Document getToggleFromCommonCode(YFSEnvironment env, Document inXML) throws Exception {

		log.beginTimer("AcademyGetToggleForContDropDown::getToggleFromCommonCode ");

		log.verbose("Entering the method AcademyGetToggleForContDropDown.getToggleFromCommonCode");

		String strGetCommonCodeListInput = "<CommonCode CodeValue='" + AcademyConstants.V_CONTAINER_DROP_DOWN_WEBSOM

				+ "' CodeType='" + AcademyConstants.V_TGL_RCP_WEB_SOM_UI + "'/>";

		Document docGetCommonCodeListInput = XMLUtil.getDocument(strGetCommonCodeListInput);

		log.verbose("docGetCommonCodeListInput \n" + XMLUtil.getXMLString(docGetCommonCodeListInput));

		String templateStr = "<CommonCodeList><CommonCode CodeLongDescription='' CodeShortDescription='' CodeType='' CodeValue=''/></CommonCodeList>";

		Document outputTemplate = YFCDocument.getDocumentFor(templateStr).getDocument();

		env.setApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST, outputTemplate);

		Document OutDocgetCommonCodeList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMONCODE_LIST,

				docGetCommonCodeListInput);

		env.clearApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST);

		log.verbose("docGetCommonCodeListoutput \n" + XMLUtil.getXMLString(docGetCommonCodeListInput));

		inXML.getDocumentElement().setAttribute("EnableContainerDropDown", SCXmlUtil

				.getXpathAttribute(OutDocgetCommonCodeList.getDocumentElement(), "CommonCode/@CodeShortDescription"));

		log.verbose("Output of the  AcademyGetToggleForContDropDown " + XMLUtil.getXMLString(inXML));

		return inXML;

	}

}