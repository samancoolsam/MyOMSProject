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

public class AcademyBarCodeTranslatorAPIForAdhocMove implements YIFCustomApi {

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
	String strOutputItemFIFO = "";

	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyBarCodeTranslatorAPIForAdhocMove.class);

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
		log.beginTimer(" Begin of AcademyBarCodeTranslatorAPIForAdhocMove ->translateItemAlias Api");
		String strItemAliasValue = null;
		this.docInput = inDoc;
		// this.env = env;
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
		
		String itemId = XPathUtil.getString(docItemDetails.getDocumentElement(),AcademyConstants.XPATH_ITEM_LIST_ITEMID);
		
		log.verbose("ItemID is :" + itemId);
		
		if(null != itemId && !"".equals(itemId)) {
			strOutputItemFIFO = getItemFIFO(docInput, docItemDetails, env);
		}		
		
		log.verbose("FIFO # :" + strOutputItemFIFO);
		
		docOutput = prepareOutputDocument();
		log.verbose("****** In translateItemAlias - Document to return :::"
				+ XMLUtil.getXMLString(docOutput));
		log.endTimer(" End of AcademyBarCodeTranslatorAPIForAdhocMove ->translateItemAlias Api");
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
			log.beginTimer(" Begin of AcademyBarCodeTranslatorAPIForAdhocMove ->getDetailsOfItem Api");
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
			log.endTimer(" End of AcademyBarCodeTranslatorAPIForAdhocMove ->getDetailsOfItem Api");
			return docItemListOutput;
		} catch (Exception e) {
					throw new YFSException(e.getMessage());

		}
	}

	/**
	 * Method retrieves the details of the Location Inventory Item by making 'getNodeInventoy' API call
	 * FIFO number of the item is obtained.
	 */
	private String getItemFIFO(Document docInput, Document docItemDtls, YFSEnvironment env) {
		try{
			String strItemFIFO = "";
			log.beginTimer(" Begin of AcademyBarCodeTranslatorAPIForAdhocMove ->getItemFIFO Api");
			Document docInputNodeInventory = XMLUtil.createDocument("NodeInventory");
			Element eleInventory = docInputNodeInventory.createElement("Inventory");
			Element eleInventoryItem = docInputNodeInventory.createElement("InventoryItem");
			
			Element eleContextInfo = (Element) docInput.getDocumentElement().getElementsByTagName("ContextualInfo").item(0);
			String strOrgCode = eleContextInfo.getAttribute("OrganizationCode");
			Element eleLocContextInfo = (Element) docInput.getDocumentElement().getElementsByTagName("LocationContextualInfo").item(0);
			if (!YFCObject.isVoid(eleLocContextInfo)) {
				String strLocId = eleLocContextInfo.getAttribute("LocationId");
				docInputNodeInventory.getDocumentElement().setAttribute("LocationId", strLocId);
			}
			Element eleContainerContextInfo = (Element) docInput.getDocumentElement().getElementsByTagName("ContainerContextualInfo").item(0);
			if (!YFCObject.isVoid(eleContainerContextInfo)) {
				String strCaseId = eleContainerContextInfo.getAttribute("CaseId");
				eleInventory.setAttribute("CaseId", strCaseId);
			}
			
			docInputNodeInventory.getDocumentElement().setAttribute("Node", strOrgCode);
			eleInventoryItem.setAttribute(AcademyConstants.ATTR_INV_UOM,AcademyConstants.UNIT_OF_MEASURE);
			eleInventoryItem.setAttribute(AcademyConstants.ITEM_ID,XPathUtil.getString(docItemDtls.getDocumentElement(),AcademyConstants.XPATH_ITEM_LIST_ITEMID));
			eleInventoryItem.setAttribute(AcademyConstants.ATTR_PROD_CLASS,AcademyConstants.PRODUCT_CLASS);
			
			eleInventory.appendChild(eleInventoryItem);
			docInputNodeInventory.getDocumentElement().appendChild(eleInventory);
			
			Document tempOutputNodeInventory = XMLUtil.getDocument("<NodeInventory EnterpriseCode=\"\" InventoryOrganizationCode=\"\" Node=\"\">" +
					"<LocationInventoryList TotalNumberOfRecords=\"\"><LocationInventory InventoryItemKey=\"\" LocationId=\"\" >" +
					"<SummaryAttributes CaseId=\"\" CountryOfOrigin=\"\" FifoNo=\"\" InventoryItemKey=\"\" InventoryStatus=\"\" PalletId=\"\" ShipByDate=\"\">" +
					"</SummaryAttributes></LocationInventory></LocationInventoryList></NodeInventory>");
	
			env.setApiTemplate("getNodeInventory", tempOutputNodeInventory);
			Document docOutputNodeInventory = AcademyUtil.invokeAPI(env, "getNodeInventory", docInputNodeInventory);
			env.clearApiTemplates();
			
			Element eleSummaryAttr = (Element) docOutputNodeInventory.getDocumentElement().getElementsByTagName("SummaryAttributes").item(0);
			if (!YFCObject.isVoid(eleSummaryAttr)) {
				strItemFIFO = eleSummaryAttr.getAttribute("FifoNo");
			}
			log.endTimer(" End of AcademyBarCodeTranslatorAPIForAdhocMove ->getItemFIFO Api");
			return strItemFIFO;
		}catch(Exception e) {
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
			log.beginTimer(" Begin of AcademyBarCodeTranslatorAPIForAdhocMove ->prepareOutputDocument Api");
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

				/**
				 * *****Fix for adhoc move failure. Reported on 11-05-2010
				 */
				Element eleContextualInfo = docOutput
						.createElement("ContextualInfo");
				eleTranslation.appendChild(eleContextualInfo);
				eleContextualInfo.setAttribute(
						AcademyConstants.ATTR_ENTERPRISE_CODE,
						AcademyConstants.PRIMARY_ENTERPRISE);
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
				
				Element eleInventoryContextualInfo = docOutput
						.createElement("Inventory");
				eleItemContextualInfo.appendChild(eleInventoryContextualInfo);
				eleInventoryContextualInfo.setAttribute("FifoNo", strOutputItemFIFO);
				
				String strCaseID = "";
				String strPalletID = "";
				if (docInput.getDocumentElement().getElementsByTagName("ContainerContextualInfo").getLength() > 0){
					Element eleContainerInfo = (Element) docInput.getDocumentElement().getElementsByTagName("ContainerContextualInfo").item(0);

					strCaseID = eleContainerInfo.getAttribute("CaseId");
					strPalletID = eleContainerInfo.getAttribute("PalletId");
					Element eleContainerContextualInfo = docOutput.createElement("ContainerContextualInfo");
					eleTranslation.appendChild(eleContainerContextualInfo);
					eleContainerContextualInfo.setAttribute("CaseId", strCaseID);
					eleContainerContextualInfo.setAttribute("PalletId", strPalletID);
				}
			}
			log.verbose("****** prepareOutputDocument : prepare document is :::"
							+ XMLUtil.getXMLString(docOutput));
			log.endTimer(" End of AcademyBarCodeTranslatorAPIForAdhocMove ->prepareOutputDocument Api");
			return docOutput;
		} catch (Exception e) {
					throw new YFSException(e.getMessage());
		}
	}
}
