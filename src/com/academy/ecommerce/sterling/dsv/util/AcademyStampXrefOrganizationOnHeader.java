package com.academy.ecommerce.sterling.dsv.util;

import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**#########################################################################################
*
* Project Name                : DSV
* Author                      : C0028916
* Author Group				  : DSV
* Date                        : 09-AUG-2022 
* Description				  : This class stamp the VendorID in the header level.
* 								 
* ---------------------------------------------------------------------------------
* Date            Author         		Version#       		Remarks/Description                      
* ---------------------------------------------------------------------------------
* 27-Mar-2020		C0028916  	 			  1.0           	Updated version
*
* #########################################################################################*/

public class AcademyStampXrefOrganizationOnHeader implements YIFCustomApi {
	
	private static YFCLogCategory logger = YFCLogCategory.instance(AcademyStampXrefOrganizationOnHeader.class);
	
	public Document invokeChangeOrder(YFSEnvironment env, Document inDoc) throws Exception {
		
		logger.verbose("Input to AcademyStampXrefOrganizationOnHeader" + XMLUtil.getXMLString(inDoc));
		Element eleDocEle = inDoc.getDocumentElement();
		Element eleShipnode = SCXmlUtil.getChildElement(eleDocEle, AcademyConstants.SHIP_NODE1);
		Element eleOrganization = SCXmlUtil.getChildElement(eleShipnode,AcademyConstants.ORG_ELEMENT);
		String strXrefOrganization = eleOrganization.getAttribute(AcademyConstants.ATTR_XREF_ORGANIZATION_CODE);
		logger.verbose("strXrefOrganization" + strXrefOrganization);
		Element eleOrder = SCXmlUtil.getChildElement(eleDocEle, AcademyConstants.ELE_ORDER);
		String strOrderNo = eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_NO);
		Document changeOrderInputDoc = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		Element eleChangeOrder =  changeOrderInputDoc.getDocumentElement();
		eleChangeOrder.setAttribute(AcademyConstants.ATTR_ACTION,AcademyConstants.STR_ACTION_MODIFY_UPPR);
		eleChangeOrder.setAttribute(AcademyConstants.STR_DOCUMENT_TYPE,AcademyConstants.DOCUMENT_TYPE_PO);
		eleChangeOrder.setAttribute(AcademyConstants.ATTR_ORDER_NO,strOrderNo);
		eleChangeOrder.setAttribute(AcademyConstants.VENDOR_ID,strXrefOrganization);
		eleChangeOrder.setAttribute(AcademyConstants.ATTR_SELECT_METHOD,AcademyConstants.STR_WAIT);
		eleChangeOrder.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE,AcademyConstants.A_ACADEMY_DIRECT);
		eleChangeOrder.setAttribute(AcademyConstants.ATTR_OVERRIDE,AcademyConstants.STR_YES);
		logger.verbose("Input to changeOrder API" + XMLUtil.getXMLString(changeOrderInputDoc));
		AcademyUtil.invokeAPI(env,AcademyConstants.API_CHANGE_ORDER,changeOrderInputDoc);    
		return inDoc;
	}
	@Override
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
	}

}
