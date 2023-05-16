package com.academy.ecommerce.order.api;

import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyCustomerAppeasementUtilAPI implements YIFCustomApi {
	/** log variable for logging messages */
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyCustomerAppeasementUtilAPI.class);
	
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}
	
	public Document stampAttributesOnOrder(YFSEnvironment env,
			Document inDoc){
		Element rootElem=inDoc.getDocumentElement();
		stampUserId(env,rootElem);
		stampGCItemID(env,rootElem);
		
		return inDoc;
		
	}

	private void stampGCItemID(YFSEnvironment env, Element rootElem) {
	/*
	 * call getCommonCodeList API
	 * <CommonCode CodeType="APPEASE_GC"/>
	 */	
		try {
			Document getCommonCodeListInputXML=XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
			// OMNI-5008 EGC Appeasement - START
			getCommonCodeListInputXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, AcademyConstants.APPEASE_GC);
			Document outXML=AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMON_CODELIST, getCommonCodeListInputXML);
			if(outXML!=null){
				//Element commoncode=(Element) outXML.getElementsByTagName("CommonCode").item(0);
				NodeList nlCommonCode = outXML.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);
				Boolean bEGCEnabled = AcademyUtil.isEGCEnabled(env);
				for(int i=0; i<nlCommonCode.getLength(); i++)
				{
					Element commoncode=(Element)nlCommonCode.item(i);
					String sLongDesc = commoncode.getAttribute(AcademyConstants.CODE_LONG_DESC);
					log.verbose(sLongDesc);
					if(bEGCEnabled && sLongDesc.contains(AcademyConstants.STR_E_GIFT_CARD))
					{
						log.verbose(commoncode.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE));
						rootElem.setAttribute(AcademyConstants.GC_ITEM_ID, commoncode.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE));
						rootElem.setAttribute(AcademyConstants.IS_GC_ENABLED, AcademyConstants.STR_YES);
					}
					
					else {
						if(sLongDesc.contains(AcademyConstants.STR_P_GIFT_CARD))
						rootElem.setAttribute(AcademyConstants.GC_ITEM_ID, commoncode.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE));
					}
				}
			// OMNI-5008 EGC Appeasement - START
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void stampUserId(YFSEnvironment env, Element rootElem) {
		rootElem.setAttribute("UserId", env.getUserId());
		
	}

}
