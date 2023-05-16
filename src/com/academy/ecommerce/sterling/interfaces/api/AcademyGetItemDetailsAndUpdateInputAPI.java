package com.academy.ecommerce.sterling.interfaces.api;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.academy.util.xml.XPathWrapper;
import com.comergent.api.xml.XMLUtils;
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
	
	//START: SHIN-5
	boolean isAttributeChanged;
	boolean isNewItem=false;
	String strExtnSFDCPIM="";
	String strExtnSFDCSterling="";
	String strExtnSFDQPIM="";
	String strExtnSFDQSterling="";
	String strExtnSFDCPIMValueIsEmpty="";
	boolean isSFDCAttributeChanged;
	boolean isSFDQAttributeChanged;
	Document itemListOutputDocument=null;
	Document docOutputGetInventoryItem=null;
	//END: SHIN-5
	//Start : BOPIS-PIM Item interface feed(IsPickupAllowed)
	boolean isIsPickupAllowedAttributeChanged = false;
	//End : BOPIS-PIM Item interface feed(IsPickupAllowed)
	// Start - OMNI 1734
	boolean isBOPISSafetyQuantityChanged = false;
	// ENd - OMNI 1734
	
	//Begin: OMNI-10273
	boolean isSTSUpdateRequired = false;
	//End: OMNI-10273
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
		log.beginTimer(" Begining of AcademyGetItemDetailsAndUpdateInputAPI-> getItemDetailsAndPerformUpdates Api");
		String strItemID = null;
		Document docItemDetails = null;
		boolean isLatestItemRecord=false;
		Element eleOutputDocumentElement = null;
		Document docItemAssociations = null;
		//SHIN-5 Setting new variable strIsModelItem
		String strIsModelItem = null;

		// Element eleItemList=null;
		if (!YFCObject.isVoid(inDoc)) {
			prepareOutputDocument(inDoc);
			strItemID = inDoc.getDocumentElement().getAttribute(
					AcademyConstants.ATTR_ITEM_ID);
			this.strItemIDInInput = strItemID;
			this.strAction = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ACTION);
			//	START: SHIN-5
			Element elePrimaryInfoElement = (Element) inDoc.getElementsByTagName(AcademyConstants.PRIM_INFO).item(0);
			strIsModelItem = elePrimaryInfoElement.getAttribute(AcademyConstants.ATTR_IS_MODEL_ITEM);
			//  END : SHIN-5
		}

		log.verbose("*************** Inside getItemDetailsAndPerformUpdates ************ ");
		
		if (!YFCObject.isVoid(strItemID)) 
		{
			try 
			{
				isLatestItemRecord = checkIfLatestItemRecord(strItemID, env,
                        inDoc);
				log.verbose("****** is item record latest - " + isLatestItemRecord);

				if (isLatestItemRecord) 
				{
					log.verbose("********* Item record in input is latest");
					// Start : OMNI-1734
					if(isNewItem || isIsPickupAllowedAttributeChanged){
					updateSafetyFactorDefn(docOutput);
					}
					// End : OMNI-1734
					
					//Begin: OMNI-10273
					if(isNewItem && !AcademyConstants.ATTR_Y.equalsIgnoreCase(strIsModelItem)) {
						log.verbose("Triggering Inv Activity for New Item :: ");
						
						setSTSFlagOnItemCreation(inDoc);
						
						String strUOM = inDoc.getDocumentElement().getAttribute(
								AcademyConstants.ATTR_UOM);
						
						callCreateInventoryActivity(strItemID, strUOM, env);
					}
					//End: OMNI-10273
					updateItemAliases(docOutput);
					docItemAssociations = getAssociationDetailsForItem(env);
					updateItemAssociationInOutput(docItemAssociations);
					//START: SHIN-5 
					// The service processFlagAttributeChange is executed only for childSKU
					
					//OMNI-10273 : Added flag isNewItem
					if (!AcademyConstants.ATTR_Y.equalsIgnoreCase(strIsModelItem) && !isNewItem)
					{
						processFlagAttributeChange(strItemID,env, inDoc);
					}
					//END: SHIN-5
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
					
					// Start : OMNI-1734
					if(isNewItem || isIsPickupAllowedAttributeChanged){
					updateSafetyFactorDefn(docOutput);
					}
					// End : OMNI-1734
					
					//Begin: OMNI-10273
					if(isNewItem) {
						
						log.verbose("Triggering Inv Activity for New Item :: ");
						
						String strUOM = inDoc.getDocumentElement().getAttribute(
								AcademyConstants.ATTR_UOM);
						isSTSUpdateRequired = true;
						callCreateInventoryActivity(strItemID, strUOM, env);
					}
					//End: OMNI-10273
					
					updateItemAliases(docOutput);
					docItemAssociations = getAssociationDetailsForItem(env);
					updateItemAssociationInOutput(docItemAssociations);
					//START: SHIN-5
					// The service processFlagAttributeChange is executed only for childSKU
					if (!AcademyConstants.ATTR_Y.equalsIgnoreCase(strIsModelItem))
					{
						processFlagAttributeChange(strItemID,env, inDoc);
					}
					//END: SHIN-5
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
		log.endTimer(" End of AcademyGetItemDetailsAndUpdateInputAPI-> getItemDetailsAndPerformUpdates Api");
		log.verbose("Final output - "
				+ XMLUtil.getXMLString(docOutput));
		return docOutput;
	}

	/**
	 * Method for the fix : STL-501
	 * 
	 * @param strItemID
	 *            - ItemID from PIM feed
	 * @param orgCode
	 *            - OrganizationCode from PIM feed
	 * @param strUOM
	 *            - UnitOfMeasure from PIM feed
	 * @param env
	 *            - YFSEnvironment
	 * 
	 * 
	 **/
	private void callCreateInventoryActivity(String strItemID, String strUOM,
			YFSEnvironment env) {
		// TODO Auto-generated method stub
		log.verbose("********* AcademyGetItemDetailsAndUpdateInputAPI--> callCreateInventoryActivity start **** ");
		//Start : OMNI-54138 : Removing logic to create Inventory Activity 
		/*Document inputDoc = YFCDocument
				.getDocumentFor(
						"<InventoryActivity DistributionRuleId='' "
								+ "ItemID='' Node='' OrganizationCode='' ProcessedFlag='' ProductClass='' UnitOfMeasure='' />")
				.getDocument();
		try {
			Element invenActivityElement = inputDoc.getDocumentElement();
			invenActivityElement.setAttribute(AcademyConstants.ATTR_ITEM_ID,
					strItemID);
			invenActivityElement.setAttribute(
					AcademyConstants.ORGANIZATION_CODE,
					AcademyConstants.PRIMARY_ENTERPRISE);
			invenActivityElement
					.setAttribute(AcademyConstants.ATTR_UOM, strUOM);
			invenActivityElement.setAttribute(AcademyConstants.ATTR_PROD_CLASS,
					AcademyConstants.PRODUCT_CLASS);
			invenActivityElement.setAttribute(
					AcademyConstants.ATTR_PROCESSED_FLAG, "F");
			//Start : BOPIS-PIM Item interface feed(IsPickupAllowed)
			if(isIsPickupAllowedAttributeChanged || isBOPISSafetyQuantityChanged){
				invenActivityElement.setAttribute(AcademyConstants.ATTR_DISTRIBUTION_RULE_ID, AcademyConstants.DISTRIBUTION_GROUP_BOPIS);
			}			
			//End : BOPIS-PIM Item interface feed(IsPickupAllowed)
			
			//Begin: OMNI-10273
			else if(isSTSUpdateRequired) {
				invenActivityElement.setAttribute(AcademyConstants.ATTR_DISTRIBUTION_RULE_ID, AcademyConstants.DISTRIBUTION_GROUP_STS);				
			}
			//End: OMNI-10273	
			
			log.verbose("********* AcademyGetItemDetailsAndUpdateInputAPI--> callCreateInventoryActivity **** "
					+ XMLUtil.getXMLString(inputDoc));
			Document outInvActivityDoc = AcademyUtil.invokeAPI(env,
					AcademyConstants.CREATE_INVENTORY_ACTIVITY_API, inputDoc);
			log.verbose("********* AcademyGetItemDetailsAndUpdateInputAPI--> callCreateInventoryActivity **** "
					+ XMLUtil.getXMLString(outInvActivityDoc));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		//OMNI-54138 -END
		log.verbose("********* AcademyGetItemDetailsAndUpdateInputAPI--> callCreateInventoryActivity end **** ");
	}

	/**
	 * Method to form the output document from the input document
	 * 
	 * 
	 * @param inDoc
	 *            Input document passed to the API
	 */
	private void prepareOutputDocument(Document inDoc) {
		try {
			log.beginTimer(" begin of AcademyGetItemDetailsAndUpdateInputAPI-> prepareOutputDocument Api");
			log.verbose("***** Preparing output document");
			docOutput = XMLUtil.createDocument(AcademyConstants.ELE_ITEM_LIST);

			Element eleCloneInput = XMLUtil.cloneDocument(inDoc)
					.getDocumentElement();
			Node nodeAsssociations = XMLUtil.getNode(eleCloneInput,
					AcademyConstants.ELE_ASSOCIATION_LIST);
			if(!YFCObject.isVoid(nodeAsssociations))
			{	
				XMLUtil.removeChild(eleCloneInput, (Element) nodeAsssociations);
			}
			XMLUtil
					.importElement(docOutput.getDocumentElement(),
							eleCloneInput);
			log.verbose("***** Output document ::"
					+ XMLUtil.getXMLString(docOutput));
			log.endTimer(" End of AcademyGetItemDetailsAndUpdateInputAPI-> prepareOutputDocument Api");
		} catch (Exception e) {
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
			log.beginTimer(" Begin of AcademyGetItemDetailsAndUpdateInputAPI-> updateItemAliases Api");
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
			log.endTimer(" End of AcademyGetItemDetailsAndUpdateInputAPI-> updateItemAliases Api");
		} catch (Exception e) {
			
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
	private boolean checkIfLatestItemRecord(String strItemID,
			YFSEnvironment env, Document inDoc) {
		log.verbose("********* Inside method to check if item record in input is latest");
		
		String orgCode = null;
		String strUOM = null;
		try {
			Document docInputGetItemList = XMLUtil
					.createDocument(AcademyConstants.ITEM);
			docInputGetItemList.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_ITEM_ID, strItemID);
			//STL-729,730: ExtnEcommerceCode added in the o/p template in order to compare ecom value from PIM and ecom value of item in the database 
			
			//START: SHIN-5
			//Updated the template to retrieve additoinal Extn Attributes
			//BOPIS-PIM Item interface feed(IsPickupAllowed)
			//OMNI-10273 - Corrected template to fetch safety factor quantity and percentage
			Document outputTemplate = YFCDocument
					.getDocumentFor(
							"<ItemList><Item ItemID='' SyncTS='' >"
							+ "<InventoryParameters OnhandSafetyFactorPercentage='' OnhandSafetyFactorQuantity='' />"
							+ "<PrimaryInformation IsPickupAllowed='' />"
							+ "<Extn ExtnShipFromStoreItem='' ExtnShpFromDC='' ExtnShipFromDQ='' ExtnEcommerceCode=''/>" 
							+ "<SafetyFactorDefinitions><SafetyFactorDefinition DeliveryMethod='' " 
							+ "OnhandSafetyFactorPercentage='' OnhandSafetyFactorQuantity=''/>"
							+ "</SafetyFactorDefinitions></Item></ItemList>")
					.getDocument();
			//END: SHIN-5
			
			env.setApiTemplate(AcademyConstants.API_GET_ITEM_LIST,
					outputTemplate);
			itemListOutputDocument = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_ITEM_LIST, docInputGetItemList);
			env.clearApiTemplate(AcademyConstants.API_GET_ITEM_LIST);
			log.verbose("Item list output - "
					+ XMLUtil.getXMLString(itemListOutputDocument));
			
			//START: SHIN-5 - If/Else Block has been added as part of this JIRA.
			if (!YFCObject.isVoid(itemListOutputDocument) && itemListOutputDocument.getDocumentElement().hasChildNodes()) {
					log.verbose("Item Exist!");
					isAttributeChanged = isAttributeChanged(inDoc,
							itemListOutputDocument);
					strItemID = inDoc.getDocumentElement().getAttribute(
							AcademyConstants.ATTR_ITEM_ID);
					orgCode = inDoc.getDocumentElement().getAttribute(
							AcademyConstants.ORG_CODE_ATTR);
					strUOM = inDoc.getDocumentElement().getAttribute(
							AcademyConstants.ATTR_UOM);
					if (isAttributeChanged) { // check for modification
						callCreateInventoryActivity(strItemID, strUOM, env);
					}
					Element itemElement = (Element) itemListOutputDocument
							.getDocumentElement()
							.getElementsByTagName(AcademyConstants.ITEM).item(0);
					String strItemLastModifiedDate = itemElement
							.getAttribute(AcademyConstants.ATTR_SYNC_TIME);
	
					Element eleItemInput = docInput.getDocumentElement();
					String strItemSyncDate = eleItemInput.getAttribute(AcademyConstants.ATTR_SYNC_TIME);
				
					DateFormat df = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
					Date dtItemLastModDate = df.parse(strItemLastModifiedDate);
					Date dtSyncDate = df.parse(strItemSyncDate);
					if (dtSyncDate.after(dtItemLastModDate)) 
					{
						log.verbose("checkIfLatestItemRecord = true");
						return true;
					} 
					else 
					{
						log.verbose("checkIfLatestItemRecord = false");
						return false;
					}		
			} else {
				// It's a new item
				log.verbose("isNewItem = true");
				isNewItem = true;
			}
			//END: SHIN-5
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
	 * Method that will compare and verify if the item attributes is modified
	 * from PIM.(For Fix : STL-501)
	 * 
	 * @param inDoc
	 *            - Document
	 * @param itemListOutputDocument
	 *            - Document
	 * @return boolean Flag saying if item record is latest
	 */
	private boolean isAttributeChanged(Document inDoc,
			Document itemListOutputDocument) {
		try{
		// TODO Auto-generated method stub
		boolean isFlag = false;
		log.verbose("*************** Inside isAttributeChanged - start ************ ");
		log.verbose("***inDoc Contents***");
		log.verbose(XMLUtil.getXMLString(inDoc));
		log.verbose("***itemListOutputDocument Contents***");
		log.verbose(XMLUtil.getXMLString(itemListOutputDocument));
		// Getting required attributes details from PIM feed XML.
		Element extnPIM = (Element) inDoc.getElementsByTagName(
				AcademyConstants.ELE_EXTN).item(0);
		Element extnInvParamsElement = (Element) inDoc.getElementsByTagName(
				"InventoryParameters").item(0);
		String extnSFSPIM = extnPIM.getAttribute("ExtnShipFromStoreItem");
		String onhandSafetyQuantityPIM = extnInvParamsElement
				.getAttribute("OnhandSafetyFactorQuantity");
		String onhandSafetyPercentPIM = extnInvParamsElement
				.getAttribute("OnhandSafetyFactorPercentage");
		log.verbose("***onhandSafetyPercentPIM***" + onhandSafetyPercentPIM);
		//Added as a part of STL-729,730
		//Fetching ExtnEcommerceCode value from xml sent by PIM
		String extnEcomCodePIM = extnPIM
				.getAttribute("ExtnEcommerceCode");
		log.verbose("***extnEcomCodePIM::" + extnEcomCodePIM+"***");
		
		// Getting required attributes details from Sterling.		
		Element extnSterling = (Element) itemListOutputDocument
				.getElementsByTagName("Extn").item(0);
		Element itemSterling = (Element) itemListOutputDocument
				.getElementsByTagName("Item").item(0);
		String extnSFSSterling = extnSterling
				.getAttribute("ExtnShipFromStoreItem");
		//Begin: OMNI-10273
		Element eleInvParamsSterling = (Element) itemSterling.getElementsByTagName(AcademyConstants.ELE_INVENTORY_PARAMETERS).item(0);
		
		String onhandSafetyQuantitySterling = eleInvParamsSterling
				.getAttribute("OnhandSafetyFactorQuantity");
		String onhandSafetyPercentSterling = eleInvParamsSterling
				.getAttribute("OnhandSafetyFactorPercentage");
		//End: OMNI-10273
		log.verbose("***onhandSafetyPercentSterling***" + onhandSafetyPercentSterling);
		//Added as a part of STL-729,730
		//Fetching ExtnEcommerceCode value of item from database
		String extnEcomCodeSterling = extnSterling
				.getAttribute("ExtnEcommerceCode");
		log.verbose("***extnEcomCodeSterling***" + extnEcomCodeSterling);
		
		//Start : BOPIS-PIM Item interface feed(IsPickupAllowed)
		Element eleItemPrimaryInfoPIM = (Element) inDoc.getElementsByTagName(AcademyConstants.ELE_PRIMARY_INFO).item(0);
		log.verbose("eleItemPrimaryInfoPIM :: "+XMLUtil.getElementXMLString(eleItemPrimaryInfoPIM));
		Element eleItemPrimaryInfoSterling = (Element) itemListOutputDocument.getElementsByTagName(AcademyConstants.ELE_PRIMARY_INFO).item(0);
		log.verbose("eleItemPrimaryInfoSterling :: "+XMLUtil.getElementXMLString(eleItemPrimaryInfoSterling));
		String strIsPickupAllowedPIM = eleItemPrimaryInfoPIM.getAttribute(AcademyConstants.ATTR_IS_PICKUP_ALLOWED);
		String strIsPickupAllowedSterling = eleItemPrimaryInfoSterling.getAttribute(AcademyConstants.ATTR_IS_PICKUP_ALLOWED);			
		//End : BOPIS-PIM Item interface feed(IsPickupAllowed)			
		
		//Start : OMNI-1734 
		String strOnhandSafetyQtyBOPISPIM = null;
		String strOnhandSafetyPctBOPISPIM = null;
		String strOnhandSafetyQtyBOPISSterling = null;
		String strOnhandSafetyPctBOPISSterling = null;
		
		
		
				
		if((!YFCObject.isVoid(strIsPickupAllowedPIM) && strIsPickupAllowedPIM.equals(AcademyConstants.STR_YES)) || 
				(!YFCObject.isVoid(strIsPickupAllowedSterling)&& strIsPickupAllowedSterling.equals(AcademyConstants.STR_YES))) {
			Element eleSafetyStockDefinitionsPIM = (Element) inDoc.getElementsByTagName(AcademyConstants.ATTR_SAFETY_FACTOR_DEFINITIONS).item(0);
			Element eleSafetyStockDefinitionsSterling = (Element) itemListOutputDocument.getElementsByTagName(AcademyConstants.ATTR_SAFETY_FACTOR_DEFINITIONS).item(0);
			
			// checking SKUSafetyfactor details In PIM Feed
			if (!YFCObject.isVoid(eleSafetyStockDefinitionsPIM)
					&& eleSafetyStockDefinitionsPIM.hasChildNodes()) {
				Element eleSafetyStockDefinitionPIM = (Element) XMLUtil.getElementByXPath(inDoc,AcademyConstants.XPATH_PIM_SAFETY_FACTOR_DEFINITION);	
				strOnhandSafetyQtyBOPISPIM = eleSafetyStockDefinitionPIM.getAttribute(AcademyConstants.ATTR_ONHAND_SAFETY_FACTOR_QUANTITY);
				strOnhandSafetyPctBOPISPIM = eleSafetyStockDefinitionPIM.getAttribute(AcademyConstants.ATTR_ONHAND_SAFETY_FACTOR_PERCENTAGE);
			}
			
			log.verbose(" ***strOnhandSafetyQtyBOPISPIM*** " + strOnhandSafetyQtyBOPISPIM +
					" ***strOnhandSafetyPctBOPISPIM*** "+ strOnhandSafetyPctBOPISPIM);

			// checking SKUSafetyfactor details In getItemList 
			if (!YFCObject.isVoid(eleSafetyStockDefinitionsSterling)
					&& eleSafetyStockDefinitionsSterling.hasChildNodes()) {

				Element eleSafetyStockDefinitionSterling = (Element) XMLUtil.getElementByXPath(itemListOutputDocument,AcademyConstants.XPATH_STERLING_SAFETY_FACTOR_DEFINITION);
				strOnhandSafetyQtyBOPISSterling = eleSafetyStockDefinitionSterling.getAttribute(AcademyConstants.ATTR_ONHAND_SAFETY_FACTOR_QUANTITY);
				strOnhandSafetyPctBOPISSterling = eleSafetyStockDefinitionSterling.getAttribute(AcademyConstants.ATTR_ONHAND_SAFETY_FACTOR_PERCENTAGE);
			}
			log.verbose(" ***strOnhandSafetyQtyBOPISSterling*** "+ strOnhandSafetyQtyBOPISSterling
					+ " ***strOnhandSafetyPctBOPISSterling*** "+ strOnhandSafetyPctBOPISSterling);

			
		}
		//End : OMNI-1734 
		

				
		//Start : BOPIS-PIM Item interface feed(IsPickupAllowed)
		if (!strIsPickupAllowedPIM.equals(null) && !strIsPickupAllowedPIM.equals(strIsPickupAllowedSterling)) {
			log.verbose("***IsPickupAllowedPIM != IsPickupAllowedSterling***");
			isIsPickupAllowedAttributeChanged = true;
			isFlag = true;
		}
		
		//End : BOPIS-PIM Item interface feed(IsPickupAllowed)
		
		//Begin: OMNI-10273
		else if (AcademyConstants.STR_TEMP
				.equals(extnSFSSterling) && !extnSFSSterling.equals(extnSFSPIM)) {
			log.verbose("***extnSFSSterling = T");
			isSTSUpdateRequired = true;
			isFlag = true;
		}
		//End: OMNI-10273
		
		
		else if (!extnSFSPIM.equals(extnSFSSterling)) {
			log.verbose("***extnSFSPIM!=extnSFSSterling");
			isFlag = true;
		}
		//STL-729,730: If PIM sends ecom code as "01" or "04" and database has ecom code other than the passed value then WCS createInventoryActivity API should be invoked
		//Start WN-2092 Sterling to treat 07 like 01 from PIM
		else if ((("01".equals(extnEcomCodePIM)) || ("04".equals(extnEcomCodePIM)) || (AcademyConstants.STR_07.equals(extnEcomCodePIM))) && (!extnEcomCodeSterling.equals(extnEcomCodePIM))) {
			log.verbose("extnEcomCodeSterling=" + extnEcomCodeSterling);
			log.verbose("***Ecommerce code changed to " + extnEcomCodePIM + "***");
			isFlag = true;
		//End WN-2092 Sterling to treat 07 like 01 from PIM
		}
		
		//End of SL-729,730
		
		/*
		else if (!onhandSafetyQuantityPIM.equals(null)&& !onhandSafetyQuantityPIM.equals(onhandSafetyQuantitySterling)) {
			log.verbose("***onhandSafetyQuantityPIM!=onhandSafetyQuantitySterling***");
			isFlag = true;
		}
		else if (!onhandSafetyPercentPIM.equals(null)&& !onhandSafetyPercentPIM.equals(onhandSafetyPercentSterling)) {
			log.verbose("***onhandSafetyPercentPIM!=onhandSafetyPercentSterling***");
			isFlag = true;
		//Start : OMNI-1734 
		}else if (!YFCObject.isNull(strOnhandSafetyQtyBOPISPIM)&& !strOnhandSafetyQtyBOPISPIM.equals(strOnhandSafetyQtyBOPISSterling)) {
			log.verbose("***IsBOPISonHandSafetyQtyPIM != IsBOPISonHandSafetyQtySterling***");
			isBOPISSafetyQuantityChanged = true;
			isFlag = true;
		} else if (!YFCObject.isNull(strOnhandSafetyPctBOPISPIM) && !strOnhandSafetyPctBOPISPIM.equals(strOnhandSafetyPctBOPISSterling)) {
			log.verbose("***IsBOPISonHandSafetyQtyPIM != IsBOPISonHandSafetyQtySterling***");
			isBOPISSafetyQuantityChanged = true;
			isFlag = true;
		}		
		//End : OMNI-1734 	
		 */
		
		else if (convertAndCompare(onhandSafetyQuantityPIM,onhandSafetyQuantitySterling)) {
			log.verbose("***onhandSafetyQuantityPIM!=onhandSafetyQuantitySterling***");
			isFlag = true;
		}
		else if (convertAndCompare(onhandSafetyPercentPIM,onhandSafetyPercentSterling)) {
			log.verbose("***onhandSafetyPercentPIM!=onhandSafetyPercentSterling***");
			isFlag = true;
		}
		else if (convertAndCompare(strOnhandSafetyQtyBOPISPIM,strOnhandSafetyQtyBOPISSterling)) {
			log.verbose("***IsBOPISonHandSafetyQtyPIM != IsBOPISonHandSafetyQtySterling***");
			isBOPISSafetyQuantityChanged = true;
			isFlag = true;
		}
		
		else if (convertAndCompare(strOnhandSafetyPctBOPISPIM,strOnhandSafetyPctBOPISSterling)) {
			log.verbose("***IsBOPISonHandSafetyQtyPIM != IsBOPISonHandSafetyQtySterling***");
			isBOPISSafetyQuantityChanged = true;
			isFlag = true;
		}
		
		log.verbose("isFlag = " + isFlag);
		return isFlag;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
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
			log.beginTimer(" Begin of AcademyGetItemDetailsAndUpdateInputAPI-> getAssociationDetailsForItem Api");
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
			log.endTimer(" Ending of AcademyGetItemDetailsAndUpdateInputAPI-> getAssociationDetailsForItem Api");
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
		log.beginTimer(" Begin of AcademyGetItemDetailsAndUpdateInputAPI-> updateItemAssociationInOutput Api");
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
			log.endTimer(" Ending of AcademyGetItemDetailsAndUpdateInputAPI-> updateItemAssociationInOutput Api");
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
		log.beginTimer(" Begin of AcademyGetItemDetailsAndUpdateInputAPI-> appendCurrentAssociationToList Api");
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
			log.endTimer(" Ending of AcademyGetItemDetailsAndUpdateInputAPI-> appendCurrentAssociationToList Api");
		} catch (Exception e) {
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
		log.beginTimer(" Begin of AcademyGetItemDetailsAndUpdateInputAPI-> formInputForRemovingAssociations Api");
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
			log.endTimer(" Ending of AcademyGetItemDetailsAndUpdateInputAPI-> formInputForRemovingAssociations Api");
		} catch (Exception e) {
			
			throw new YFSException(e.getMessage());
		}
	}
	
//START: SHIN-5
	
	/**
	 * @param inDoc
	 * @param itemListOutputDocument
	 * @return
	 * 
	 * This method isSFDCAttributeChanged() contains the logic to get the SFDC value from PIM 
	 * and Sterling and check whether it equals or not.
	 * 
	 * The below logic is handled for the PIM input where existing item with SFDC attribute passed.
	 * Sterling will always have a value in DB for an existing item and it set the flag as True
	 * 
	 * The below logic is handled for Sterling will have SFDC value as empty  for a new item and 
	 * it set the flag as True
	 * 
	 * The below logic is handled for PIM input where new item without SFDC value passed.
	 * and it set the flag as True
	 * 
	 */
	private boolean isSFDCAttributeChanged(Document inDoc,
			Document itemListOutputDocument) {
		
		boolean bReturnFlag = false;
		log.verbose("*************** Inside isSFDCAttributeChanged - start ************ ");
		log.verbose("***inDoc Contents***");
		log.verbose(XMLUtil.getXMLString(inDoc));
		log.verbose("***itemListOutputDocument Contents***");
		log.verbose(XMLUtil.getXMLString(itemListOutputDocument));
		
		Element itemElement = (Element) itemListOutputDocument
		.getDocumentElement()
		.getElementsByTagName(AcademyConstants.ITEM).item(0);
		
		// Getting required attributes details from PIM feed XML.
		Element eleExtnPIM = (Element) inDoc.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
		strExtnSFDCPIM = eleExtnPIM.getAttribute(AcademyConstants.ATTR_EXTN_SHIP_DC);
		
		if (YFCObject.isVoid(itemElement)) {
			//No Record of Item in the System. Handling as New Item
			log.verbose("***ItemList blank. No Record of Item in the System. Handling as New Item***");
			bReturnFlag = true;
		} else {
					
				// Getting required attributes details from Sterling.		
				Element eleExtnSterling = (Element) itemListOutputDocument
					.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
				strExtnSFDCSterling = eleExtnSterling.getAttribute(AcademyConstants.ATTR_EXTN_SHIP_DC);
			
			 	//SFDC value passed in the PIM input xml
				 if(!YFCObject.isVoid(strExtnSFDCPIM))
				{
					//checking whether SFDC value from PIM equals the sterling SFDC value
					//This handles the case of New Item scenario as well 
					if (!strExtnSFDCPIM.equals(strExtnSFDCSterling)) {
						log.verbose("***strExtnSFDCPIM!=strExtnSFDCSterling***");
						bReturnFlag = true;
					}
	
					//SFDC value from PIM is void	
				} else {
					log.verbose("***strExtnSFDCPIM is Void. Flag is not passed***");
				}
		}
		
		log.verbose("*************** Inside isSFDCAttributeChanged - End ************"); 
			 
		return bReturnFlag;
	}
	//END: SHIN-5
	
	//START: SHIN-5
	
	/**
	 * @param inDoc
	 * @param itemListOutputDocument
	 * @return
	 * 
	 * This method isSFDQAttributeChanged() contains the logic to get the SFDQ value from PIM 
	 * and Sterling and check whether it equals or not.
	 * 
	 * The below logic is handled for the PIM input where existing item with SFDQ attribute passed.
	 * Sterling will always have a value in DB for an existing item and it set the flag as True
	 * 
	 * The below logic is handled for Sterling will have SFDQ value as empty  for an new item and 
	 * it set the flag as True
	 * 
	 * The below logic is handled for PIM input where new item without SFDQ value passed.
	 * and it set the flag as True
	 * 
	 */
	private boolean isSFDQAttributeChanged(Document inDoc,
			Document itemListOutputDocument) {
		
		boolean bReturnFlag = false;
		log.verbose("*************** Inside isSFDQAttributeChanged - start ************ ");
		log.verbose("***inDoc Contents***");
		log.verbose(XMLUtil.getXMLString(inDoc));
		log.verbose("***itemListOutputDocument Contents***");
		log.verbose(XMLUtil.getXMLString(itemListOutputDocument));
		
		Element itemElement = (Element) itemListOutputDocument
		.getDocumentElement()
		.getElementsByTagName(AcademyConstants.ITEM).item(0);
		
		// Getting required attributes details from PIM feed XML.
		 Element eleExtnPIM = (Element) inDoc.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
		 strExtnSFDQPIM = eleExtnPIM.getAttribute(AcademyConstants.ATTR_EXTN_SHP_FROM_DQ);
		
		if (YFCObject.isVoid(itemElement)) {
			//No Record of Item in the System. Handling as New Item
			log.verbose("***ItemList blank. No Record of Item in the System. Handling as New Item***");
			bReturnFlag = true;
		} else {
			 
			// Getting required attributes details from Sterling.		
			Element eleExtnSterling = (Element) itemListOutputDocument
						.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
			strExtnSFDQSterling = eleExtnSterling.getAttribute(AcademyConstants.ATTR_EXTN_SHP_FROM_DQ);
			
			
			 //SFDQ value passed in the PIM input xml
			 if(!YFCObject.isVoid(strExtnSFDQPIM))
			 {
				 //checking whether SFDQ value from PIM equals the sterling SFDQ value
				 //This handles the case of New Item scenario as well 
				 if (!strExtnSFDQPIM.equals(strExtnSFDQSterling)) {
					 log.verbose("***strExtnSFDQPIM!=strExtnSFDQSterling");
					 bReturnFlag = true;
				 }
				 //SFDQ value from PIM is void
			 } else {
					log.verbose("***strExtnSFDQPIM is Void. New Item & Flag is not passed***");
			 }
		}
		 log.verbose("*************** Inside isSFDQAttributeChanged - End ************");
		 
		 return bReturnFlag;
	}
	//END: SHIN-5
	
	//START: SHIN-5
	/*
	 * Created a Hash map for the common code SFDCShipNode.
	 * Looping through the hash map and get the key.
	 * 
	 * checking the not null condition for strSFDCMapKey and invoking the method createIpforMgeInvNodeContValIsY
	 * once the input is prepared for manageinventorynodecontrol for value Y  and it invoke the API.
	 *
	 */
	
	private Document changeOfDCFlagFromNtoY(YFSEnvironment env, String strItemID) throws Exception
	{
		String strMethodName="changeOfDCFlagFromNtoY";
		String strSFDCMapKey="";
		Document docOutMgeInvNodeConValY= null;
		
		//multiapi document is created
		Document docMultiApi = XMLUtil.createDocument(AcademyConstants.ELE_MULTIAPI);
		
		
		try {
			//Hashmap is created for the common code type SFDCShipNode for the flag Y
			HashMap<String, String> hmSFDCMap = AcademyCommonCode
					.getCommonCodeListAsHashMap(env, AcademyConstants.CC_SFDCShipNode,
							AcademyConstants.HUB_CODE);
			
			for(Entry<String, String> entry : hmSFDCMap.entrySet()){
				//getting the key
				strSFDCMapKey = entry.getKey();
				
				if (!YFCObject.isVoid(strSFDCMapKey))
				{
					//invoking the method to prepare the input for manageInventoryNodeControl API for flag Y
					docMultiApi = createIpforMgeInvNodeContValIsY(docMultiApi,strItemID,strSFDCMapKey);
					
					log.verbose(strMethodName+"::multiapi Input inside for loop of Common Code::"
							+ XMLUtil.getXMLString(docMultiApi));
				}
		   }
			
			log.verbose(strMethodName+"::multiapi Input final::"
					+ XMLUtil.getXMLString(docMultiApi));
			
			//invoking the multiApi API for manageInventoryNodeControl API input for flag Y
			docOutMgeInvNodeConValY = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_MULTI_API,
					docMultiApi);
			
			log.verbose(strMethodName+"::multiapi  Output for SFDC Flag N to Y::"
					+ XMLUtil.getXMLString(docOutMgeInvNodeConValY));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
		return docOutMgeInvNodeConValY;
	}
	//END: SHIN-5
	
	
	//START: SHIN-5
	/*
	 * Multiapi input is prepared for manageInventoryNodeControl API for each common code key.
	 * The input which is prepared for Value Y will delete the record in db.
	 */
	private Document createIpforMgeInvNodeContValIsY(Document docMultiApi,String strItemID,String strNodeKeyY)
	throws Exception
	{

		log.verbose("********* Create Input for manageInventoryNodeControl API for flag Y Start **** ");
		
		try {
			
			//InventoryNodeControl document is prepared
			Document docIpMgeInvNodeCon = XMLUtil.createDocument(AcademyConstants.INVENTORY_NODE_CONTROL);

			Element eleIpMgeInvNodeCon = docIpMgeInvNodeCon
					.getDocumentElement();
			
			//the required attributes are set here
			eleIpMgeInvNodeCon.setAttribute(
					AcademyConstants.ATTR_ITEM_ID, strItemID);
			eleIpMgeInvNodeCon.setAttribute(
					AcademyConstants.ATTR_NODE, strNodeKeyY);
			eleIpMgeInvNodeCon.setAttribute(
					AcademyConstants.ORGANIZATION_CODE,
					AcademyConstants.PRIMARY_ENTERPRISE);
			eleIpMgeInvNodeCon.setAttribute(
					AcademyConstants.ATTR_PROD_CLASS, AcademyConstants.PRODUCT_CLASS);
			eleIpMgeInvNodeCon
					.setAttribute(AcademyConstants.ATTR_UOM,
							AcademyConstants.UNIT_OF_MEASURE);
			eleIpMgeInvNodeCon.setAttribute(
					AcademyConstants.ATTR_INV_PIC_CORRECT,
					AcademyConstants.STR_YES);

			log.verbose("********* Create Input for manageInventoryNodeControl API for flag Y and proceed with the multi api invocation method End ******** "
					+ XMLUtil.getXMLString(docIpMgeInvNodeCon));
			
			//invoking the method includeInMultiApi to prepare the multi api structure 
			includeInMultiApi(docMultiApi, docIpMgeInvNodeCon.getDocumentElement(),AcademyConstants.API_MANAGE_INVENTORY_NODE_CONTROL);
				
			log.verbose("****************** Create Input for manageInventoryNodeControl API for flag Y  End **** ");

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return docMultiApi;
		
	}
	
	//END: SHIN-5
	
	//START: SHIN-5
	/*
	 * Created a Hash map for the common code SFDCShipNode.
	 * Looping through the hash map and get the key.
	 * 
	 * checking the not null condition for strSFDCMapKeyN and invoking the method createIpforMgeInvNodeContValIsN
	 * once the input is prepared for manageinventorynodecontrol for value N  and it invoke the API.
	 *
	 */
	
	private Document changeOfDCFlagFromYtoN(YFSEnvironment env, String strItemID) throws Exception
	{
		String strMethod = "changeOfDCFlagFromYtoN";
		String strSFDCMapKeyN="";
		Document docOutMgeInvNodeConN= null;
		
		//multiapi document is created
		Document docMultiApi = XMLUtil.createDocument(AcademyConstants.ELE_MULTIAPI);
		
		try {
			
			//Hashmap is created for the common code type SFDCShipNode for the flag N
			HashMap<String, String> hmSFDCMapN = AcademyCommonCode
					.getCommonCodeListAsHashMap(env, AcademyConstants.CC_SFDCShipNode,
							AcademyConstants.HUB_CODE);
			
			for(Entry<String, String> entryN : hmSFDCMapN.entrySet()){
				//getting the key
				strSFDCMapKeyN = entryN.getKey();
				
				if (!YFCObject.isVoid(strSFDCMapKeyN)){
					
					//invoking the method to prepare the input for manageInventoryNodeControl API for flag N
					docMultiApi = createIpforMgeInvNodeContValIsN(docMultiApi,strItemID,strSFDCMapKeyN);
					
					log.verbose(strMethod+"::multiapi Input inside for loop of Common Code::"
							+ XMLUtil.getXMLString(docMultiApi));
				}
		   }
			
			log.verbose(strMethod+"::multiapi Input final::"
					+ XMLUtil.getXMLString(docMultiApi));
			
			//invoking the multiApi API for manageInventoryNodeControl API input for flag N
			docOutMgeInvNodeConN = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_MULTI_API,
					docMultiApi);
			
			log.verbose(strMethod+"::multi api Output for SFDC flag Y TO N::"
					+ XMLUtil.getXMLString(docOutMgeInvNodeConN));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
		return docOutMgeInvNodeConN;
	}
	//END: SHIN-5
	
	//START: SHIN-5
	/*
	 * Multiapi input is prepared for manageInventoryNodeControl API for each common code key.
	 * The input which is prepared for Value N will insert the record in db.
	 */
	private Document createIpforMgeInvNodeContValIsN(Document docMultiApi,String strItemID,String strNodeKeyN) throws Exception
	{
		
		log.verbose("****************** Create Input for manageInventoryNodeControl API for flag N  Start **** ");
		/*docIpMgeInvNodeCon = YFCDocument
				.getDocumentFor(
						"<InventoryNodeControl InventoryPictureCorrect='' "
								+ "ItemID='' Node='' OrganizationCode='' ProductClass='' NodeControlType='' "
								+ "UnitOfMeasure='' />").getDocument();*/

		try {
			
			//InventoryNodeControl document is prepared
			Document docIpMgeInvNodeCon = XMLUtil.createDocument(AcademyConstants.INVENTORY_NODE_CONTROL);

			Element eleIpMgeInvNodeCon = docIpMgeInvNodeCon
					.getDocumentElement();
			
			//the required attributes are set here
			eleIpMgeInvNodeCon.setAttribute(
					AcademyConstants.ATTR_ITEM_ID, strItemID);
			eleIpMgeInvNodeCon.setAttribute(
					AcademyConstants.ATTR_NODE, strNodeKeyN);
			eleIpMgeInvNodeCon.setAttribute(
					AcademyConstants.ORGANIZATION_CODE,
					AcademyConstants.PRIMARY_ENTERPRISE);
			eleIpMgeInvNodeCon.setAttribute(
					AcademyConstants.ATTR_PROD_CLASS,AcademyConstants.PRODUCT_CLASS);
			eleIpMgeInvNodeCon
					.setAttribute(AcademyConstants.ATTR_UOM,
							AcademyConstants.UNIT_OF_MEASURE);
			eleIpMgeInvNodeCon.setAttribute(
					AcademyConstants.ATTR_NODE_CONTROL_TYPE, AcademyConstants.ATTR_VAL_ON_HOLD);
			eleIpMgeInvNodeCon.setAttribute(
					AcademyConstants.ATTR_INV_PIC_INCORRECT_TILL_DATE,
					AcademyConstants.ATTR_VAL_INV_PIC_INCORRECT_TILL_DATE);

			log.verbose("*********Create Input for manageInventoryNodeControl API for flag N and proceed with the multi api invocation method End **** "
					+ XMLUtil.getXMLString(docIpMgeInvNodeCon));
			
			//invoking the method includeInMultiApi to prepare the multi api structure 
			includeInMultiApi(docMultiApi, docIpMgeInvNodeCon.getDocumentElement(),AcademyConstants.API_MANAGE_INVENTORY_NODE_CONTROL);
				
			log.verbose("********* Create Input for manageInventoryNodeControl API for flag N  End  **** ");

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return docMultiApi;
		
	}
	
	//END: SHIN-5
	
	//START: SHIN-5
	
	/*
	 * Created a Hash map for the common code SFDQShipNode.
	 * Looping through the hash map and get the key.
	 * 
	 * checking the not null condition for strSFDQMapKey and invoking the method createIpforMgeInvNodeContValIsY
	 * once the input is prepared for manageinventorynodecontrol for value Y  and it invoke the API.
	 *
	 */
	
	private Document changeOfDQFlagFromNtoY(YFSEnvironment env, String strItemID) throws Exception
	{
		String strMethodName="changeOfDQFlagFromNtoY";
		String strSFDQMapKey="";
		Document docOutMgeInvNodeConValYForDQ= null;
		
		//multiapi document is created
		Document docMultiApi = XMLUtil.createDocument(AcademyConstants.ELE_MULTIAPI);
		
		try {
			
			//Hashmap is created for the common code type SFDQShipNode for the flag Y
			HashMap<String, String> hmSFDQMap = AcademyCommonCode
					.getCommonCodeListAsHashMap(env, AcademyConstants.CC_SFDQShipNode,
							AcademyConstants.HUB_CODE);
			
			for(Entry<String, String> entry : hmSFDQMap.entrySet()){
				//getting the key
				strSFDQMapKey = entry.getKey();
				
				if (!YFCObject.isVoid(strSFDQMapKey))
				{
					//invoking the method to prepare the input for manageInventoryNodeControl API for flag Y
					docMultiApi = createIpforMgeInvNodeContValIsY(docMultiApi,strItemID,strSFDQMapKey);
								
					log.verbose(strMethodName+"::multiapi Input inside for loop of Common Code::"
							+ XMLUtil.getXMLString(docMultiApi));
				}
		   }
			
			log.verbose(strMethodName+"::multiapi Input final:::"
					+ XMLUtil.getXMLString(docMultiApi));
			
			//invoking the multiApi API for manageInventoryNodeControl API input for flag Y
			docOutMgeInvNodeConValYForDQ = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_MULTI_API,
					docMultiApi);
			
			log.verbose(strMethodName+"::multi api Output for SFDQ flag N to Y::"
					+ XMLUtil.getXMLString(docOutMgeInvNodeConValYForDQ));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
		return docOutMgeInvNodeConValYForDQ;
	}
	//END: SHIN-5
	
	//START: SHIN-5
	/*
	 * Created a Hash map for the common code SFDQShipNode.
	 * Looping through the hash map and get the key.
	 * 
	 * checking the not null condition for strSFDQMapKeyN and invoking the method createIpforMgeInvNodeContValIsN
	 * once the input is prepared for manageinventorynodecontrol for value N  and it invoke the API.
	 *
	 */
	
	private Document changeOfDQFlagFromYtoN(YFSEnvironment env, String strItemID) throws Exception
	{
		String strMethodName="changeOfDQFlagFromYtoN";
		String strSFDQMapKeyN="";
		Document docOutMgeInvNodeConValNForDQ= null;
		
		//multiapi document is created
		Document docMultiApi = XMLUtil.createDocument(AcademyConstants.ELE_MULTIAPI);
		
		try {
			
			//Hashmap is created for the common code type SFDQShipNode for the flag N
			HashMap<String, String> hmSFDQMapN = AcademyCommonCode
					.getCommonCodeListAsHashMap(env, AcademyConstants.CC_SFDQShipNode,
							AcademyConstants.HUB_CODE);
			
			for(Entry<String, String> entryN : hmSFDQMapN.entrySet()){
				//getting the key
				strSFDQMapKeyN = entryN.getKey();
				
				if (!YFCObject.isVoid(strSFDQMapKeyN))
				{
					
					//invoking the method to prepare the input for manageInventoryNodeControl API for flag N
					docMultiApi = createIpforMgeInvNodeContValIsN(docMultiApi,strItemID,strSFDQMapKeyN);
					
					log.verbose(strMethodName+"::multiapi Input inside for loop of Common Code::"
							+ XMLUtil.getXMLString(docMultiApi));
				}
		   }
			
			log.verbose(strMethodName+"::multiapi Input final::"
					+ XMLUtil.getXMLString(docMultiApi));
			
			//invoking the multiApi API for manageInventoryNodeControl API input for flag N
			docOutMgeInvNodeConValNForDQ = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_MULTI_API,
					docMultiApi);
			
			log.verbose(strMethodName+"::multi api Output for SFDQ Y TO N::"
					+ XMLUtil.getXMLString(docOutMgeInvNodeConValNForDQ));
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
		return docOutMgeInvNodeConValNForDQ;
	}
	//END: SHIN-5
	
	//START: SHIN-5
	
	/**
	 * This method will set APIName, input for multiAPI
	 * 
	 * @param input
	 *            Document of multiApi
	 * @param element
	 *            to append under Input element
	 * @param ApiName
	 */
	private static void includeInMultiApi(Document docMultiApi, Element apiInput, String apiName)
	{
		// Fetch the root element
		Element multiApiDocRoot = docMultiApi.getDocumentElement();
		// Create element API
		Element api = docMultiApi.createElement(AcademyConstants.ELE_API);
		multiApiDocRoot.appendChild(api);
		// set attribute Name
		api.setAttribute(AcademyConstants.ATTR_NAME, apiName);
		// Create element Input
		Element input = docMultiApi.createElement(AcademyConstants.ELE_INPUT);
		api.appendChild(input);
	
		Node importEle = docMultiApi.importNode(apiInput, true);
		input.appendChild(importEle);
		log.verbose("MultiApi Doc prepared ::"
				+ XMLUtil.getXMLString(docMultiApi));

	}
	
	
	//END: SHIN-5
	
	//START:SHIN-5
	/*
	 * The method processFlagAttributeChange contains logic to invoke a method to check whether 
	 * attribute changed or not for both DC and DQ
	 * 
	 * if the SFDC attribute changed is true then it check whether the sfdc  flag value from PIM is Y 
	 * then invoking the logic of changeOfDCFlagFromNtoY and if the sfdc flag value from PIM is N then
	 *  invoking the logic of changeOfDCFlagFromYtoN. Additionally logic defined for sfdc flag value from 
	 *  PIM is void then also invoking the method changeOfDCFlagFromYtoN.
	 * 
	 * if the SFDQ attribute changed is true then it check whether the sfdc  flag value from PIM is Y 
	 * then invoking the logic of changeOfDCFlagFromNtoY
	 * 
	 * The above logic applies for SFDQ flag also.
	 * 
	 */
	private void processFlagAttributeChange(String strItemID,YFSEnvironment env, Document inDoc)throws Exception
	{
		String strMethodName="processFlagAttributeChange";
		String strUOM = inDoc.getDocumentElement().getAttribute(
				AcademyConstants.ATTR_UOM);
		

		log.verbose(strMethodName+":: start ::");

		//The Method isSFDCAttributeChanged() invocation is done to check the SFDC value from PIM and Sterling equals or not
		isSFDCAttributeChanged = isSFDCAttributeChanged(inDoc,
				itemListOutputDocument);

		//The Method isSFDQAttributeChanged() invocation is done to check the SFDQ value from PIM and Sterling equals or not
		isSFDQAttributeChanged = isSFDQAttributeChanged(inDoc,
				itemListOutputDocument);
		
		
		if (!isAttributeChanged && (isSFDCAttributeChanged || isSFDQAttributeChanged)) {
			
			//invoking the getInventoryItemList() to invoke the getInventoryItemList API to know whether the inventory item exists for the item or no
			getInventoryItemList( strItemID,env,inDoc);
			
			if(!YFCObject.isVoid(docOutputGetInventoryItem)&&docOutputGetInventoryItem.getDocumentElement().hasChildNodes()){
				
				log.verbose("********* Get Inventory Item List::"
					+ XMLUtil.getXMLString(docOutputGetInventoryItem));
			
				// check for modification
				callCreateInventoryActivity(strItemID, strUOM, env);
			}
		}
		

		/* if SFDC attribute changed condition satisfies then checking the value of strExtnSFDCPIM equals Yes then
		 * invoking the method changeOfDCFlagFromNtoY
		 * 
		 * if the value of strExtnSFDCPIM equals No then invoking the method changeOfDCFlagFromYtoN
		 * 
		 * if the value of strExtnSFDCPIM is void then invoking the method changeOfDCFlagFromYtoN
		 */
		
		/* OMNI-6365 : Begin
		 * if (isSFDCAttributeChanged) { if (YFCObject.isVoid(strExtnSFDCPIM)) {
		 * changeOfDCFlagFromYtoN(env,strItemID); } else if
		 * (strExtnSFDCPIM.equals(AcademyConstants.STR_YES)){
		 * changeOfDCFlagFromNtoY(env,strItemID); } else if
		 * (strExtnSFDCPIM.equals(AcademyConstants.STR_NO)){
		 * changeOfDCFlagFromYtoN(env,strItemID); } }
		 * OMNI-6365 : End
		 */

		/* if SFDQ attribute changed condition satisfies then checking the value of strExtnSFDQPIM equals Yes then
		 * invoking the method changeOfDQFlagFromNtoY
		 * 
		 * if the value of strExtnSFDQPIM equals No then invoking the method changeOfDQFlagFromYtoN
		 * 
		 * if the value of strExtnSFDQPIM is void then invoking the method changeOfDQFlagFromYtoN
		 */

		/* OMNI-6365 : Begin
		 * if (isSFDQAttributeChanged) { if (YFCObject.isVoid(strExtnSFDQPIM)) {
		 * changeOfDQFlagFromYtoN(env,strItemID); } else if
		 * (strExtnSFDQPIM.equals(AcademyConstants.STR_YES)){
		 * changeOfDQFlagFromNtoY(env,strItemID); } else if
		 * (strExtnSFDQPIM.equals(AcademyConstants.STR_NO)){
		 * changeOfDQFlagFromYtoN(env,strItemID); } }
		 * OMNI-6365 : End
		 */
		
		//OMNI-6363 : Begin
		updateDGOverrideAttribute();
		//OMNI-6363 : End
		
		log.verbose(strMethodName+":: end ::");
	}
	
	//END: SHIN-5
	
	
	//OMNI-6363 : Begin
	private void updateDGOverrideAttribute() throws Exception{
		
		String strExtnShipFromDC = null;
		String strExtnShipFromDQ = null;
				
		Element eleExtn = (Element) docOutput
				.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_EXTN)
				.item(0);
		
		if(null != eleExtn) {
			strExtnShipFromDC = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_SHP_FROM_DC);
			strExtnShipFromDQ = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_SHP_FROM_DQ);
		}
		
		if(StringUtil.isEmpty(strExtnShipFromDC)) {
			
			log.verbose("strExtnShipFromDC is empty");
			
			strExtnShipFromDC = strExtnSFDCSterling;
			
			log.verbose("Value from DB :: "+strExtnShipFromDC);
		}
		
		if(StringUtil.isEmpty(strExtnShipFromDQ)) {
			log.verbose("strExtnShipFromDQ is empty");
			strExtnShipFromDQ = strExtnSFDQSterling;
			log.verbose("Value from DB :: "+strExtnShipFromDQ);
		}
				
		if (AcademyConstants.STR_YES.equals(strExtnShipFromDC) 
				&& AcademyConstants.STR_YES.equals(strExtnShipFromDQ)) {
			
			setDGOverrideAttribute(docOutput, AcademyConstants.STR_EMPTY_STRING);
		}else if((AcademyConstants.STR_NO.equals(strExtnShipFromDC) 
				|| AcademyConstants.STR_NO.equals(strExtnShipFromDQ))) {
			
			setDGOverrideAttribute(docOutput, AcademyConstants.STR_YES);
		}
	}
	
	private void setDGOverrideAttribute(Document docInput, String strEnableOverride) throws Exception {			
		
		Element eleInvParams = null;
		
		log.debug("Input Document :: "+XMLUtil.getXMLString(docInput));
		log.debug("Enable Override :: "+strEnableOverride);
		
		Element eleItem = (Element)docInput.getDocumentElement().getElementsByTagName(AcademyConstants.ITEM).item(0);
		
		eleInvParams = (Element) docInput.getDocumentElement()
				.getElementsByTagName(AcademyConstants.ELE_INVENTORY_PARAMETERS)
				.item(0);
		if (YFCObject.isNull(eleInvParams)) {
			
			eleInvParams = docInput.createElement(AcademyConstants.ELE_INVENTORY_PARAMETERS);
		}
		
		eleInvParams.setAttribute(AcademyConstants.ATTR_REQUIRES_DG_OVERRIDE, strEnableOverride);
		eleItem.appendChild(eleInvParams);
		log.debug("Input Document post modification :: "+XMLUtil.getXMLString(docInput));
	}
	//OMNI-6363 : End
	
	//START: SHIN-5
	private void getInventoryItemList(String strItemID,
			YFSEnvironment env, Document inDoc) throws Exception
	{
		String strMethodName = "getInventoryItemList";
		String strUOM = inDoc.getDocumentElement().getAttribute(
				AcademyConstants.ATTR_UOM);
		try {
			log.beginTimer(strMethodName+"::***Begin of AcademyGetItemDetailsAndUpdateInputAPI-> getInventoryItemList Api*** START::");
			log
					.verbose("********* Inside method to get Inventory Item List details for item");
			Document docInputGetInventoryItem = XMLUtil
					.createDocument(AcademyConstants.ELE_INVENTORY_ITEM);
			docInputGetInventoryItem.getDocumentElement().setAttribute(
					AcademyConstants.INVENTORY_ORGANIZATION_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
			docInputGetInventoryItem.getDocumentElement()
					.setAttribute(AcademyConstants.ITEM_ID,strItemID);
			docInputGetInventoryItem.getDocumentElement().setAttribute(
					AcademyConstants.ORGANIZATION_CODE,
					AcademyConstants.HUB_CODE);
			docInputGetInventoryItem.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_PROD_CLASS,AcademyConstants.PRODUCT_CLASS);
			docInputGetInventoryItem.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_UOM,strUOM);
			
			log.verbose("********* Get Inventory Item List Input XML::"
					+ XMLUtil.getXMLString(docInputGetInventoryItem));
			
			docOutputGetInventoryItem = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_INVENTORY_ITEM_LIST,
					docInputGetInventoryItem);
			
			log.verbose("********* Get Inventory Item List Output XML::"
					+ XMLUtil.getXMLString(docOutputGetInventoryItem));
			log.endTimer(strMethodName+"::***End of AcademyGetItemDetailsAndUpdateInputAPI-> getInventoryItemList Api*** END");
	} catch(Exception e){
		e.printStackTrace();
		throw e;
	}
	}
	//END: SHIN-5

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
	
	//Start : OMNI-1734
	private void updateSafetyFactorDefn(Document docPIMInput) throws Exception {
		
		// TODO Auto-generated method stub
		log.verbose("****** Enter into updateSafetyFactorDefn method ");
		log.verbose("Cloned PIM Input is " + XMLUtil.getXMLString(docPIMInput));
		log.verbose("itemListOutputDocument is " + XMLUtil.getXMLString(itemListOutputDocument));
		
		Element eleInp  = docPIMInput.getDocumentElement();
		Element elePIMItem = (Element) eleInp.getElementsByTagName("Item").item(0);
		Element elePriminfo = (Element) elePIMItem.getElementsByTagName(AcademyConstants.ELE_PRIMARY_INFO).item(0);
		Element eleItemListOutput = itemListOutputDocument.getDocumentElement();
		Element eleSterlingItem = (Element) eleItemListOutput.getElementsByTagName("Item").item(0);
		Element eleSterPriminfo = null; 
		String strPickUpAllowedSter = null;
		String strPickUpAllowedPIM = elePriminfo.getAttribute(AcademyConstants.ATTR_IS_PICKUP_ALLOWED);
		
		if(!YFCObject.isVoid(eleSterlingItem)){
			
			eleSterPriminfo = (Element) eleSterlingItem.getElementsByTagName(AcademyConstants.ELE_PRIMARY_INFO).item(0);
			strPickUpAllowedSter = eleSterPriminfo.getAttribute(AcademyConstants.ATTR_IS_PICKUP_ALLOWED);
		}
		
		log.verbose("****** PIM IsPickUpAllowed is" + strPickUpAllowedPIM);
		log.verbose("****** Sterling IsPickUpAllowed is" + strPickUpAllowedSter);
		Element eleSafetyFactorDefinitions = null;
		Element eleSafetyFactorDefinition = null;
		
		if((!YFCObject.isVoid(strPickUpAllowedPIM) && strPickUpAllowedPIM.equals(AcademyConstants.STR_YES) && isNewItem) || 
				(isIsPickupAllowedAttributeChanged && !YFCObject.isVoid(strPickUpAllowedPIM) 
						&& !YFCObject.isVoid(strPickUpAllowedSter)	&& strPickUpAllowedPIM.equals(AcademyConstants.STR_YES) 
						&&  strPickUpAllowedSter.equals(AcademyConstants.STR_NO)) ){

			eleSafetyFactorDefinitions = (Element) elePIMItem.getElementsByTagName(AcademyConstants.ATTR_SAFETY_FACTOR_DEFINITIONS).item(0);

			if (!YFCObject.isVoid(eleSafetyFactorDefinitions) && eleSafetyFactorDefinitions.hasChildNodes())
			{
				eleSafetyFactorDefinition = (Element)eleSafetyFactorDefinitions.getElementsByTagName(AcademyConstants.ATTR_SAFETY_FACTOR_DEFINITION).item(0);

			}
			else{
				if (YFCObject.isNull(eleSafetyFactorDefinitions)) {
					eleSafetyFactorDefinitions = docPIMInput.createElement(AcademyConstants.ATTR_SAFETY_FACTOR_DEFINITIONS);
				}
				eleSafetyFactorDefinition =	docPIMInput.createElement(AcademyConstants.ATTR_SAFETY_FACTOR_DEFINITION);	
				eleSafetyFactorDefinition.setAttribute(AcademyConstants.ATTR_DEL_METHOD,AcademyConstants.STR_PICK);
				eleSafetyFactorDefinition.setAttribute(AcademyConstants.ATTR_ONHAND_SAFETY_FACTOR_QUANTITY, "0.0");
				
				XMLUtil.importElement(eleSafetyFactorDefinitions, eleSafetyFactorDefinition);
			}
		}
		if (YFCObject.isNull(eleSafetyFactorDefinitions)) {
			XMLUtil.importElement(elePIMItem, eleSafetyFactorDefinitions);
		}
		log.verbose("****** Exit updateSafetyFactorDefn method " + XMLUtil.getXMLString(docPIMInput));
		
	}
	//End : OMNI-1734
	
	//Begin: OMNI-10273
	private void setSTSFlagOnItemCreation(Document docInpPIM) throws Exception{
		
		Element eleItemPrimaryInfoPIM = (Element) docInpPIM.getElementsByTagName(AcademyConstants.ELE_PRIMARY_INFO).item(0);
		
		log.verbose("eleItemPrimaryInfoPIM :: "+XMLUtil.getElementXMLString(eleItemPrimaryInfoPIM));
		
		String strIsPickupAllowedPIM = eleItemPrimaryInfoPIM.getAttribute(AcademyConstants.ATTR_IS_PICKUP_ALLOWED);		
		
		if(AcademyConstants.STR_YES.equals(strIsPickupAllowedPIM)) {
			isIsPickupAllowedAttributeChanged = true;
		}else {
			isSTSUpdateRequired = true;
		}
		
		
	}
	
	public boolean convertAndCompare(String strSafetyFactorPIM, String strSafetyFactorSterling) {
		log.verbose("****** Begin of convertAndCompare method ****** ");
        boolean bComparisonResult = false;
       
        double dSafetyFactorPIM;
        double dSafetyFactorSterling;
               
        if(!StringUtil.isEmpty(strSafetyFactorPIM)  &&  !StringUtil.isEmpty(strSafetyFactorSterling)){
           
            dSafetyFactorPIM = Double.parseDouble(strSafetyFactorPIM);
            dSafetyFactorSterling = Double.parseDouble(strSafetyFactorSterling);
           
            log.verbose("D Sterling: "+Double.toString(dSafetyFactorSterling));
           
            log.verbose("D PIM: "+Double.toString(dSafetyFactorPIM));
           
            if(dSafetyFactorPIM != dSafetyFactorSterling){
                bComparisonResult = true;
            }
        }
        
        else if(!YFCObject.isNull(strSafetyFactorPIM) && !strSafetyFactorPIM.equals(strSafetyFactorSterling)) {
        	
        	log.verbose("String comparison as first condition fails ::: ");
        	
        	bComparisonResult = true;
        }
       
        log.verbose("Result :: "+bComparisonResult);
        log.verbose("****** End of convertAndCompare method ******");
        return bComparisonResult;
    }
	//End: OMNI-10273
}
