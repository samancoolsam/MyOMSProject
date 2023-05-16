package com.academy.ecommerce.sterling.item.api;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;

import com.yantra.interop.japi.YIFCustomApi;

import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;

import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Properties;

public class AcademyBarCodeTranslatorAPI implements YIFCustomApi {

	/**
	 * Variable to hold the input that's passed
	 */
	Document docInput = null;

	/**
	 * Variable to hold the output that will be returned
	 */
	Document docOutput = null;

	/**
	 * Document variable that will hold the details of item
	 */
	Document docItemDetails = null;

	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyBarCodeTranslatorAPI.class);

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
	}

	/**
	 * This method will retrieve the item id based on the alias value passed in
	 * the input
	 * 
	 * @param env
	 *            Environment variable
	 * @param inDoc
	 *            Input document
	 * @return docOutput Document in translateBarCode format with details of
	 *         Item retrieved for the Item Alias value
	 * @throws Exception
	 *             Generic exception
	 */
	public Document translateItemAlias(YFSEnvironment env, Document inDoc)
			throws Exception {
		String strItemAliasValue = null;
		this.docInput = inDoc;
		// this.env = env;
		log.beginTimer(" Begin of AcademyBarCodeTranslatorAPI ->translateItemAlias Api");
		log.verbose("****** In translateItemAlias method");
		if (!YFCObject.isVoid(inDoc))
			log.verbose("***** In translateItemAlias: input document is:::"
					+ XMLUtil.getXMLString(inDoc));
		if (!YFCObject.isVoid(docInput)) {
			strItemAliasValue = docInput.getDocumentElement().getAttribute(
					AcademyConstants.ATTR_BAR_CODE_DATA);
		}

		if (YFCObject.isVoid(strItemAliasValue)) {
			YFSException exception = new YFSException();
			exception.setErrorCode(AcademyConstants.ERR_CODE_02);
			throw exception;
		}

		docItemDetails = getDetailsOfItem(strItemAliasValue, env);
		docOutput = prepareOutputDocument();
		log.verbose("****** In translateItemAlias - Document to return :::"
				+ XMLUtil.getXMLString(docOutput));
		log.endTimer(" End of AcademyBarCodeTranslatorAPI ->translateItemAlias Api");
		return docOutput;
	}

	/**
	 * Method retrieves the details of the item by making 'getItemList' API call
	 * using the item alias value
	 * 
	 * @param strItemAliasValue
	 *            Item alias passed in input
	 * @param env
	 *            Environment variab;e
	 * @return docItemListOutput Document containing details of item fetched
	 *         through API call
	 */
	private Document getDetailsOfItem(String strItemAliasValue,
			YFSEnvironment env) {
		try {
			log.beginTimer(" Begin of AcademyBarCodeTranslatorAPI ->getDetailsOfItem Api");
			log
					.verbose("****** getDetailsOfItem : inside method to get item details");
			Document docInputGetItemDetails = XMLUtil
					.createDocument(AcademyConstants.ITEM);
			docInputGetItemDetails.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_READ_UNCOMMITED,
					AcademyConstants.STR_YES);
			Element eleItemAliasList = docInputGetItemDetails
					.createElement(AcademyConstants.ELE_ITEM_ALIAS_LIST);
			docInputGetItemDetails.getDocumentElement().appendChild(
					eleItemAliasList);

			Element eleItemAlias = docInputGetItemDetails
					.createElement(AcademyConstants.ELE_ITEM_ALIAS);
			eleItemAliasList.appendChild(eleItemAlias);
			eleItemAlias.setAttribute(AcademyConstants.ATTR_ALIAS_VAL,
					strItemAliasValue);

			Document docItemListOutput = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_ITEM_LIST, docInputGetItemDetails);
			log.verbose("****** Exiting getDetailsOfItem document is ::"
					+ XMLUtil.getXMLString(docItemListOutput));
			log.endTimer(" End of AcademyBarCodeTranslatorAPI ->getDetailsOfItem Api");
			return docItemListOutput;
		} catch (Exception e) {
						throw new YFSException(e.getMessage());

		}
	}

	/**
	 * Method to form the output document from the input document
	 * 
	 * 
	 */
	private Document prepareOutputDocument() {
		try {
			log.beginTimer(" Begin of AcademyBarCodeTranslatorAPI ->prepareOutputDocument Api");
			String strTotItemRecords = null;
			log
					.verbose("****** prepareOutputDocument : inside method to prepare output document");
			docOutput = XMLUtil.createDocument(AcademyConstants.ELE_BAR_CODE);
			docOutput.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_BAR_CODE_DATA,
					docInput.getDocumentElement().getAttribute(
							AcademyConstants.ATTR_BAR_CODE_DATA));
			docOutput.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_BAR_CODE_TYPE,
					docInput.getDocumentElement().getAttribute(
							AcademyConstants.ATTR_BAR_CODE_TYPE));

			Element eleTranslations = docOutput
					.createElement(AcademyConstants.ELE_TRANSLATIONS);
			docOutput.getDocumentElement().appendChild(eleTranslations);
			strTotItemRecords = docItemDetails.getDocumentElement()
					.getAttribute(AcademyConstants.ATTR_TOT_ITEM_LIST);
			if (YFCObject.isVoid(strTotItemRecords))
				strTotItemRecords = "0";
			eleTranslations.setAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS,
					strTotItemRecords);
			if (!YFCObject.isVoid(strTotItemRecords)) {
				Element eleTranslation = docOutput
						.createElement(AcademyConstants.ELE_TRANSLATION);
				eleTranslations.appendChild(eleTranslation);
				Element eleItemContextualInfo = docOutput
						.createElement(AcademyConstants.ELE_CONTEXT_INFO);
				eleTranslation.appendChild(eleItemContextualInfo);
				eleItemContextualInfo.setAttribute(
						AcademyConstants.ATTR_INV_UOM,
						AcademyConstants.UNIT_OF_MEASURE);
				eleItemContextualInfo.setAttribute(AcademyConstants.ITEM_ID,
						XPathUtil.getString(
								docItemDetails.getDocumentElement(),
								AcademyConstants.XPATH_ITEM_LIST_ITEMID));
				eleItemContextualInfo.setAttribute(
						AcademyConstants.ATTR_PROD_CLASS,
						AcademyConstants.PRODUCT_CLASS);
			}
			log
					.verbose("****** prepareOutputDocument : prepare document is :::"
							+ XMLUtil.getXMLString(docOutput));
			log.endTimer(" End of AcademyBarCodeTranslatorAPI ->prepareOutputDocument Api");
			return docOutput;
		} catch (Exception e) {
			
			throw new YFSException(e.getMessage());
		}
	}
}
