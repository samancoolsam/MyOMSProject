package com.academy.ecommerce.interfaces.api;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.academy.util.xml.XPathWrapper;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyGetItemDetailsAndUpdateInputAPI implements YIFCustomApi {
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyGetItemDetailsAndUpdateInputAPI.class);

	/**
	 * Variable to store input document that's passed to the API
	 */
	Document docInput = null;

	/**
	 * Variable to store Item ID passed in input
	 */
	String strItemIDInInput = null;

	/**
	 * Variable to store action on Item
	 */
	String strAction = null;

	/**
	 * Variable to store & return output
	 */
	Document docOutput = null;

	/**
	 * Variable to item association list in the final output document
	 */
	Element eleAssociationListInOutput = null;

	public AcademyGetItemDetailsAndUpdateInputAPI() {
		// TODO Auto-generated constructor stub
	}

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
	}

	/**
	 * This method would get the input document and will call the necessary APIs
	 * to manipulate the final output XML based on existing associatons for
	 * items which would be processed by other APIs in the service
	 * 
	 * @param env -
	 *            Environment variable
	 * @param inDoc -
	 *            Document that's passed as input
	 * @return - Final manipulated output document
	 * @throws Exception -
	 *             Generic exception
	 */
	public Document getItemDetailsAndPerformUpdates(YFSEnvironment env,
			Document inDoc) throws Exception {
		// Call getItemDetails API
		// this.env = env;
		this.docInput = inDoc;

		String strItemID = null;

		Document docItemDetails = null;
		boolean isLatestItemRecord;
		Element eleOutputDocumentElement = null;
		Document docItemAssociations = null;

		// Element eleItemList=null;
		if (!YFCObject.isVoid(inDoc)) {
			prepareOutputDocument(inDoc);
			strItemID = inDoc.getDocumentElement().getAttribute(
					AcademyConstants.ATTR_ITEM_ID);

			this.strItemIDInInput = strItemID;
			this.strAction = inDoc.getDocumentElement().getAttribute("Action");
			log.verbose("######### Action Passed is ####" + strAction);
		}
		
		log.verbose("*************** Inside getItemDetailsAndPerformUpdates ************ ");
		
		if (!YFCObject.isVoid(strItemID)) 
		{
			try 
			{
				isLatestItemRecord = checkIfLatestItemRecord(strItemID, env);
				log.verbose("****** is item record latest - " + isLatestItemRecord);

				if (isLatestItemRecord) 
				{
					log.verbose("********* Item record in input is latest");
					updateItemAliases(docOutput);
					docItemAssociations = getAssociationDetailsForItem(env);
					updateItemAssociationInOutput(docItemAssociations);
					clearTemplatesForAPIs(env);
				} 
				else 
				{
					log.verbose("********* Item record in input is old");
					if (!YFCObject.isVoid(docOutput)) 
					{
						eleOutputDocumentElement = docOutput.getDocumentElement();
						log.verbose("********* Setting attribute to quit item add process");
						eleOutputDocumentElement.setAttribute(AcademyConstants.ATTR_QUIT_ITEM_ADD, AcademyConstants.STR_YES);
					}

					return docOutput;
				}
			} 
			catch (YFSException e) {
				if ("YCM0033".equals(e.getErrorCode())) {

					updateItemAliases(docOutput);
					docItemAssociations = getAssociationDetailsForItem(env);
					updateItemAssociationInOutput(docItemAssociations);
					clearTemplatesForAPIs(env);
				} else {
					throw new YFSException(e.getMessage());
				}
			} catch (Exception e) {
				throw new YFSException(e.getMessage());
			}

			if (!YFCObject.isVoid(docItemDetails)) {
			}
		} else {
			YFSException excItemID = new YFSException(
					AcademyConstants.ERR_ITEM_NOT_FOUND);
			excItemID.setErrorCode(AcademyConstants.ERR_CODE_01);
			throw excItemID;
		}

		return docOutput;
	}

	/**
	 * Method to form the output document from the input document
	 * 
	 * @param inDoc
	 *            Input document passed to the API
	 */
	private void prepareOutputDocument(Document inDoc) {
		try {

			log.verbose("***** Preparing output document");
			docOutput = XMLUtil.createDocument(AcademyConstants.ELE_ITEM_LIST);

			Element eleCloneInput = XMLUtil.cloneDocument(inDoc)
					.getDocumentElement();
			Node nodeAsssociations = XMLUtil.getNode(eleCloneInput,
					AcademyConstants.ELE_ASSOCIATION_LIST);
			if (!YFCObject.isVoid(nodeAsssociations)) {
				XMLUtil.removeChild(eleCloneInput, (Element) nodeAsssociations);
			}
			XMLUtil
					.importElement(docOutput.getDocumentElement(),
							eleCloneInput);
			log.verbose("***** Output document ::"
					+ XMLUtil.getXMLString(docOutput));
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
	}

	/**
	 * Method will update item aliases structure in the output document. Custom
	 * UPC code value will be formed for each of the aliases by appending
	 * numerics in a sequence
	 * 
	 * @param docOutput
	 *            Output document containing the updated alias details for the
	 *            item
	 */
	private void updateItemAliases(Document docOutput) {
		Element eleItem = docOutput.getDocumentElement();
		log.verbose("***** Inside method to update aliases in input");
		try {
			XPathWrapper xpathWrapper = new XPathWrapper(eleItem);
			NodeList nodeListItemAliases = xpathWrapper
					.getNodeList(AcademyConstants.XPATH_ITEM_ALIAS);
			int iAliasList = nodeListItemAliases.getLength();
			Element eleItemAlias = null;
			String strItemAliasCode = null;

			for (int i = 0; i < iAliasList; i++) {
				eleItemAlias = (Element) nodeListItemAliases.item(i);
				strItemAliasCode = eleItemAlias
						.getAttribute(AcademyConstants.ATTR_ALIAS_NAME);
				eleItemAlias.setAttribute(AcademyConstants.ATTR_ALIAS_NAME,
						strItemAliasCode + String.valueOf(i));
			}
			log.verbose("***** Output document after updating aliases::"
					+ XMLUtil.getXMLString(docOutput));
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
	}

	/**
	 * Method that will compare and verify if the item is
	 * older than the already existing record for the item in DB
	 * 
	 * @param strItemID - Item ID
	 * @param env - Environment
	 * 
	 * @return boolean Flag saying if item record is latest
	 */
	private boolean checkIfLatestItemRecord(String strItemID, YFSEnvironment env)
	{
		log.verbose("********* Inside method to check if item record in input is latest");
		try
		{
			Document docInputGetItemList = XMLUtil.createDocument(AcademyConstants.ITEM);
			docInputGetItemList.getDocumentElement().setAttribute(AcademyConstants.ATTR_ITEM_ID, strItemID);
			Document outputTemplate = YFCDocument.getDocumentFor("<ItemList> <Item SyncTS=''/> </ItemList>").getDocument();
			env.setApiTemplate(AcademyConstants.API_GET_ITEM_LIST, outputTemplate);
			Document itemListOutputDocument = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ITEM_LIST, docInputGetItemList);
			env.clearApiTemplate(AcademyConstants.API_GET_ITEM_LIST);
			log.verbose("Item list output - " + XMLUtil.getXMLString(itemListOutputDocument));
			
			if(!YFCObject.isVoid(itemListOutputDocument) && itemListOutputDocument.getDocumentElement().getElementsByTagName(AcademyConstants.ITEM).getLength()>0)
			{
				Element itemElement = (Element) itemListOutputDocument.getDocumentElement().getElementsByTagName(AcademyConstants.ITEM).item(0);
				String strItemLastModifiedDate = itemElement.getAttribute(AcademyConstants.ATTR_SYNC_TIME);

				Element eleItemInput = docInput.getDocumentElement();
				String strItemSyncDate = eleItemInput.getAttribute(AcademyConstants.ATTR_SYNC_TIME);
			
				DateFormat df = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
				Date dtItemLastModDate = df.parse(strItemLastModifiedDate);
				Date dtSyncDate = df.parse(strItemSyncDate);
				if (dtSyncDate.after(dtItemLastModDate)) 
				{
					return true;
				} 
				else 
				{
					return false;
				}
			}
		}
		catch(ParserConfigurationException e1)
		{
			e1.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * Method to make 'getItemAssociations' API call and get association details
	 * for the item passed in input
	 * 
	 * @param env
	 *            Environment variable
	 * @return docOutputItemAssociations Document containing association details
	 *         for the item
	 */
	private Document getAssociationDetailsForItem(YFSEnvironment env) {
		try {
			log
					.verbose("********* Inside method to get association details for item");
			Document docInputGetAssociations = XMLUtil
					.createDocument(AcademyConstants.ELE_ASSOCIATION_LIST);
			docInputGetAssociations.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_ITEM_ID, strItemIDInInput);
			docInputGetAssociations.getDocumentElement()
					.setAttribute(AcademyConstants.ATTR_UOM,
							AcademyConstants.UNIT_OF_MEASURE);
			docInputGetAssociations.getDocumentElement().setAttribute(
					AcademyConstants.ORGANIZATION_CODE,
					AcademyConstants.HUB_CODE);

			// Default template for getItemAssociations API will be used
			Document docOutputItemAssociations = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_ITEM_ASSOCIATIONS,
					docInputGetAssociations);
			log.verbose("********* Association for Item::"
					+ XMLUtil.getXMLString(docOutputItemAssociations));
			return docOutputItemAssociations;
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
	}

	/**
	 * Method to update item associations in the output document in order to be
	 * procesed by 'modifyItemAssociations' API in the service
	 * 
	 * @param docItemAssoc
	 *            Document containing existing association details for Item
	 */
	private void updateItemAssociationInOutput(Document docItemAssoc) {
		boolean bAssociationInDBExists = true;
		boolean bAssociationInInputExists = true;
		int iAssocList = 0;
		int iAssocListInInput = 0;
		XPathWrapper xpathWrapperAssocDoc = null;
		NodeList nodeListItemAssoc = null;
		XPathWrapper xpathWrapperAssocInputDoc = null;
		NodeList nodeListInputItemAssoc = null;
		log
				.verbose("********* Inside method to get update item associations in output");
		try {
			if (!YFCObject.isVoid(docItemAssoc)) {
				xpathWrapperAssocDoc = new XPathWrapper(docItemAssoc
						.getDocumentElement());
				nodeListItemAssoc = xpathWrapperAssocDoc
						.getNodeList(AcademyConstants.XPATH_ASSOCIATIONS);
				iAssocList = nodeListItemAssoc.getLength();
			}

			xpathWrapperAssocInputDoc = new XPathWrapper(docInput
					.getDocumentElement());
			nodeListInputItemAssoc = xpathWrapperAssocInputDoc
					.getNodeList(AcademyConstants.XPATH_ASSOCIATIONS_INPUT);

			if (!YFCObject.isVoid(nodeListInputItemAssoc)) {
				iAssocListInInput = nodeListInputItemAssoc.getLength();
			}

			if (iAssocList <= 0) {
				bAssociationInDBExists = false;
			}

			if (iAssocListInInput <= 0) {
				bAssociationInInputExists = false;
			}

			if (("Delete").equals(strAction)) {
				docOutput.getDocumentElement().setAttribute(
						AcademyConstants.ATTR_CALL_MODIFY_ITEM_ASSOC, "N");

				return;
			}

			if (!bAssociationInDBExists && !bAssociationInInputExists) {
				docOutput.getDocumentElement().setAttribute(
						AcademyConstants.ATTR_CALL_MODIFY_ITEM_ASSOC, "N");

				return;
			}

			eleAssociationListInOutput = (Element) XPathUtil.getNode(docOutput
					.getDocumentElement(),
					AcademyConstants.XPATH_ASSOCIATION_LIST);

			for (int i = 0; i < iAssocListInInput; i++) {
				Element eleCurrentAsociationInInput = (Element) nodeListInputItemAssoc
						.item(i);
				Element eleAssocInAssocDoc = (Element) XPathUtil
						.getNode(
								docItemAssoc.getDocumentElement(),
								"/AssociationList/Association/Item[@ItemID='"
										+ eleCurrentAsociationInInput
												.getAttribute("AssociateItemID")
										+ "']");

				if (YFCObject.isVoid(eleAssocInAssocDoc)) {
					appendCurrentAssociationToList(docItemAssoc,
							eleCurrentAsociationInInput,
							AcademyConstants.STR_ACTION_CREATE);
				} else {
					appendCurrentAssociationToList(docItemAssoc,
							eleCurrentAsociationInInput,
							AcademyConstants.STR_ACTION_MODIFY);
				}
			}

			formInputForRemovingAssociations(docItemAssoc);
			log
					.verbose("********* Output document after updatng associations :::"
							+ XMLUtil.getXMLString(docOutput));
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
	}

	/**
	 * This method will append association element to the final output document
	 * based on if the association is new or already existing
	 * 
	 * @param Document
	 *            Document containing existing association details for Item
	 * @param eleCurrentAsociationInInput
	 *            Element that contains the current association that's being
	 *            evaluated in the calling method
	 * @param strAction
	 *            Action that needs to be performed based on if the association
	 *            is new or existing
	 */
	private void appendCurrentAssociationToList(Document docItemAssoc,
			Element eleCurrentAsociationInInput, String strAction) {
		Element eleCurrentItemAssociation = null;
		Element eleCurrentAssociatedItem = null;
		Element eleItem = null;
		Element eleAssocInAssocDoc = null;
		Element eleAssociation = null;
		Date dateCurrent = null;
		DateFormat dateFormat = null;
		log
				.verbose("********* Inside method to append association to output document");
		try {
			eleAssocInAssocDoc = (Element) XPathUtil
					.getNode(
							docItemAssoc.getDocumentElement(),
							"/AssociationList/Association/Item[@ItemID='"
									+ eleCurrentAsociationInInput
											.getAttribute(AcademyConstants.ATTR_ASSOC_ITEM_ID)
									+ "']");

			if (!YFCObject.isVoid(eleAssocInAssocDoc)) {
				eleAssociation = (Element) eleAssocInAssocDoc.getParentNode();

			}

			if (YFCObject.isVoid(eleAssociationListInOutput)) {
				eleItem = (Element) XMLUtil.getNode(docOutput
						.getDocumentElement(), AcademyConstants.ITEM);
				eleAssociationListInOutput = docOutput
						.createElement(AcademyConstants.ELE_ASSOCIATION_LIST);
				XMLUtil.appendChild(eleItem, eleAssociationListInOutput);
				eleAssociationListInOutput.setAttribute(
						AcademyConstants.ATTR_ITEM_ID, strItemIDInInput);
				eleAssociationListInOutput.setAttribute(
						AcademyConstants.ORGANIZATION_CODE,
						AcademyConstants.HUB_CODE);
				eleAssociationListInOutput.setAttribute(
						AcademyConstants.ATTR_UOM,
						AcademyConstants.UNIT_OF_MEASURE);
			}

			eleCurrentItemAssociation = XMLUtil.importElement(
					eleAssociationListInOutput, docOutput
							.createElement(AcademyConstants.ELE_ASSOCIATION));
			eleCurrentItemAssociation.setAttribute(
					AcademyConstants.ATTR_ACTION, strAction);
			eleCurrentItemAssociation.setAttribute(
					AcademyConstants.ATTR_ASSOC_TYPE,
					eleCurrentAsociationInInput
							.getAttribute(AcademyConstants.ATTR_ASSOC_TYPE));

			if (AcademyConstants.STR_ACTION_CREATE.equals(strAction)) {
				dateCurrent = new Date();
				dateFormat = new SimpleDateFormat(
						AcademyConstants.STR_SIMPLE_DATE_PATTERN);

				String currentDate = dateFormat.format(dateCurrent);
				eleCurrentItemAssociation.setAttribute(
						AcademyConstants.STR_EFFECTIVE_FROM, currentDate);
				eleCurrentItemAssociation.setAttribute(
						AcademyConstants.STR_EFFECTIVE_TO,
						AcademyConstants.STR_MAX_YEAR);
			} else if (AcademyConstants.STR_ACTION_MODIFY.equals(strAction)) {
				eleCurrentItemAssociation
						.setAttribute(
								AcademyConstants.STR_EFFECTIVE_FROM,
								eleAssociation
										.getAttribute(AcademyConstants.STR_EFFECTIVE_FROM));
				eleCurrentItemAssociation
						.setAttribute(
								AcademyConstants.STR_EFFECTIVE_TO,
								eleAssociation
										.getAttribute(AcademyConstants.STR_EFFECTIVE_TO));
			}

			eleCurrentAssociatedItem = XMLUtil.importElement(
					eleCurrentItemAssociation, docOutput
							.createElement(AcademyConstants.ITEM));
			eleCurrentAssociatedItem.setAttribute(
					AcademyConstants.ATTR_ITEM_ID, eleCurrentAsociationInInput
							.getAttribute(AcademyConstants.ATTR_ASSOC_ITEM_ID));
			eleCurrentAssociatedItem.setAttribute(
					AcademyConstants.ORGANIZATION_CODE,
					AcademyConstants.HUB_CODE);
			eleCurrentAssociatedItem.setAttribute(AcademyConstants.ATTR_UOM,
					AcademyConstants.UNIT_OF_MEASURE);

			if (AcademyConstants.STR_ACTION_MODIFY.equals(strAction)) {
				XMLUtil.removeChild(docItemAssoc.getDocumentElement(),
						(Element) eleAssocInAssocDoc.getParentNode());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
	}

	/**
	 * This method is called from the method to update item associations. This
	 * method will append the necessary xml strcuture to the output document to
	 * remove associations
	 * 
	 * @param docItemAssoc -
	 *            Document containing existing association details for Item
	 */

	private void formInputForRemovingAssociations(Document docItemAssoc) {
		Element eleItem = null;
		Element eleRemove = null;
		XPathWrapper xpathWrapperAssocDocument = null;
		NodeList nodeListItemAssociton = null;
		int iAssocList = 0;
		log
				.verbose("********* Inside method to add remove elements for association");
		try {
			if (!YFCObject.isVoid(docItemAssoc)) {
				xpathWrapperAssocDocument = new XPathWrapper(docItemAssoc
						.getDocumentElement());
				nodeListItemAssociton = xpathWrapperAssocDocument
						.getNodeList(AcademyConstants.XPATH_ASSOCIATIONS);
				iAssocList = nodeListItemAssociton.getLength();
			}

			for (int i = 0; i < iAssocList; i++) {
				Element eleCurrentAsociation = (Element) nodeListItemAssociton
						.item(i);
				if (YFCObject.isVoid(eleAssociationListInOutput)) {
					eleItem = (Element) XMLUtil.getNode(docOutput
							.getDocumentElement(), AcademyConstants.ITEM);
					eleAssociationListInOutput = docOutput
							.createElement(AcademyConstants.ELE_ASSOCIATION_LIST);
					XMLUtil.appendChild(eleItem, eleAssociationListInOutput);
					eleAssociationListInOutput.setAttribute(
							AcademyConstants.ATTR_ITEM_ID, strItemIDInInput);
					eleAssociationListInOutput.setAttribute(
							AcademyConstants.ORGANIZATION_CODE,
							AcademyConstants.HUB_CODE);
					eleAssociationListInOutput.setAttribute(
							AcademyConstants.ATTR_UOM,
							AcademyConstants.UNIT_OF_MEASURE);
				}

				eleRemove = XMLUtil.createDocument(AcademyConstants.ELE_REMOVE)
						.getDocumentElement();

				if (!YFCObject.isVoid(eleCurrentAsociation)) {
					XMLUtil.importElement(eleRemove, eleCurrentAsociation);

				}

				Element eleChild = (Element) docOutput.importNode(eleRemove,
						true);
				eleAssociationListInOutput.appendChild(eleChild);
				log
						.verbose("********* Output document after adding remove elements for associations"
								+ XMLUtil.getXMLString(docOutput));

			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
	}

	/**
	 * This method clears all the templates set for API calls in the environment
	 * variable
	 * 
	 * @param env -
	 *            Environment variable
	 */
	private void clearTemplatesForAPIs(YFSEnvironment env) {
		log.verbose("********** Clearing templates set for API calls");
		env.clearApiTemplates();
	}
	
	public static void main(String[] args)
	{
		Document doc = YFCDocument.getDocumentFor(new File("C://input.xml")).getDocument();
		Element itemElement = (Element) doc.getDocumentElement().getElementsByTagName(AcademyConstants.ITEM).item(0);
		log.verbose(XMLUtil.getXMLString(doc));
		String strItemLastModifiedDate = itemElement.getAttribute(AcademyConstants.ATTR_SYNC_TIME);
		log.verbose(strItemLastModifiedDate);
	}
}
