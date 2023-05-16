package com.academy.ecommerce.item.api;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;


public class AcademyBarCodeTranslator implements YIFCustomApi {
	/**
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademyBarCodeTranslator.class);
	
	YFSEnvironment env=null;
	Document docInput=null;
	Document docOutput=null;
	Document docItemDetails=null;
	


	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
	}
	public Document translateItemAlias(YFSEnvironment env, Document inDoc) throws Exception
	{
		String strItemAliasValue=null;
		this.docInput=inDoc;
		this.env=env;
		log.verbose("##### Document sent as input ######"+XMLUtil.getXMLString(inDoc));
		if(!YFCObject.isVoid(docInput))
		strItemAliasValue=docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_BAR_CODE_DATA);
		if(YFCObject.isVoid(strItemAliasValue))
		{
			YFSException exception=new YFSException("Item ID Cannot be blank");
			exception.setErrorCode("EXTN_ACADEMY_002");
			throw exception;
		}
		docItemDetails=getDetailsOfItem(strItemAliasValue);
		docOutput=prepareOutputDocument();
		return docOutput;
		
	}
	
	private Document getDetailsOfItem(String strItemAliasValue)
	{
		try
		{
			Document docInputGetItemDetails=XMLUtil.createDocument(AcademyConstants.ITEM);
			Element eleItemAliasList=docInputGetItemDetails.createElement(AcademyConstants.ELE_ITEM_ALIAS_LIST);
			docInputGetItemDetails.getDocumentElement().appendChild(eleItemAliasList);
			Element eleItemAlias=docInputGetItemDetails.createElement(AcademyConstants.ELE_ITEM_ALIAS);
			eleItemAliasList.appendChild(eleItemAlias);
			eleItemAlias.setAttribute(AcademyConstants.ATTR_ALIAS_VAL, strItemAliasValue);
			Document docItemListOutput=AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_ITEM_LIST,docInputGetItemDetails);
			return docItemListOutput;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
		
		private Document prepareOutputDocument()
		{
			try
			{
				docOutput=XMLUtil.createDocument(AcademyConstants.ELE_BAR_CODE);
				docOutput.getDocumentElement().setAttribute(AcademyConstants.ATTR_BAR_CODE_DATA,docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_BAR_CODE_DATA));
				docOutput.getDocumentElement().setAttribute(AcademyConstants.ATTR_BAR_CODE_TYPE,docInput.getDocumentElement().getAttribute(AcademyConstants.ATTR_BAR_CODE_TYPE));
				Element eleTranslations=docOutput.createElement(AcademyConstants.ELE_TRANSLATIONS);
				docOutput.getDocumentElement().appendChild(eleTranslations);
				eleTranslations.setAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS,docItemDetails.getDocumentElement().getAttribute(AcademyConstants.ATTR_TOT_ITEM_LIST));
				Element eleTranslation=docOutput.createElement(AcademyConstants.ELE_TRANSLATION);
				eleTranslations.appendChild(eleTranslation);
				Element eleItemContextualInfo=docOutput.createElement(AcademyConstants.ELE_CONTEXT_INFO);
				eleTranslation.appendChild(eleItemContextualInfo);
				eleItemContextualInfo.setAttribute(AcademyConstants.ATTR_INV_UOM,AcademyConstants.UNIT_OF_MEASURE);
				eleItemContextualInfo.setAttribute(AcademyConstants.ITEM_ID,XPathUtil.getString(docItemDetails.getDocumentElement(),AcademyConstants.XPATH_ITEM_LIST_ITEMID));
				eleItemContextualInfo.setAttribute(AcademyConstants.ATTR_PROD_CLASS,AcademyConstants.PRODUCT_CLASS);
				return docOutput;
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
		
			
	

}
