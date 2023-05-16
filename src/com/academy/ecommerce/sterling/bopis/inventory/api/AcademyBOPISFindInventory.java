package com.academy.ecommerce.sterling.bopis.inventory.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @Author : Chiranthan(SapientRazorfish_)
 * @JIRA# : BOPIS-263 – WCS-OMS Find Inventory (Real time Call)
 * @Date : Created on 07-07-2018
 * 
 * @Purpose : 
 * This interface(AcademyBOPISFindInventoryService) is exposed as a REST Webservice, for real time findInventory request/response from WCS.
 * WCS make an real time product availability request from Product Detail Page (PDP) and cart page.
 *
 * This class does preprocessing, before findInventory API is invoked.
 * For each BOPIS promiseline, ACAD_BOPIS_ITEM_EXCLUSION table is checked to see if the item-node is excluded from BOPIS consideration.
 * If excluded, shipNode is removed from <ShipNode> & the filtered ship nodes are added to the <ExcludeShipNode> tag at each PromiseLine.
 * Once the ship nodes are added in ExcludeShipNode tag those nodes will not be considered in findInventory call for Inventory lookup. 
 *
 *  Assumptions: 
 *      - findInventory call from WCS, will only happen for BOPIS lines.
 *      - ASO Support will manually populate ACAD_BOPIS_ITEM_EXCLUSION, based on request.			
 *   	  
 **/

public class AcademyBOPISFindInventory {
	
	private static Logger log = Logger.getLogger(AcademyBOPISFindInventory.class.getName());
	private Properties props;
	public void setProperties(Properties props) throws Exception {		
		this.props = props;
	}
	
	/** This method loops through each Promiseline for Item-Node exclusion check.
	 * @param env
	 * @param inDoc
	 */
	public Document preprocessFindInventory(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("Entering AcademyBOPISFindInventory.preprocessFindInventory() :: "+XMLUtil.getXMLString(inDoc));		
		Element elePromiseLine = null;
		NodeList nlPromiseLine = null;
		
		try{
			nlPromiseLine = inDoc.getElementsByTagName(AcademyConstants.ELE_PROMISE_LINE);
			
			for(int i=0; i < nlPromiseLine.getLength(); i++){
				elePromiseLine = (Element) nlPromiseLine.item(i);
				
				//Check if Item-Node combination is present in item hangoff table - ACAD_BOPIS_ITEM_EXCLUSION.
				checkBopisItemExclusionList(env, elePromiseLine);
				
			}			
		}catch (Exception e){
			e.printStackTrace();			
			throw new YFSException(e.getMessage(),"INV0001", "Exception Ocuured in AcademyBOPISFindInventory");
		}		
		
		log.verbose("Input to findInventory API :: "+XMLUtil.getXMLString(inDoc));
		log.verbose("Exiting AcademyBOPISFindInventory.preprocessFindInventory()");
		return inDoc;
	}
	
	
	/** This method checks ACAD_BOPIS_ITEM_EXCLUSION(YFS_ITEM Hangoff table) table to see if the item-node is excluded from consideration.
	 * @param env
	 * @param elePromiseLine
	 */
	private void checkBopisItemExclusionList(YFSEnvironment env, Element elePromiseLine) throws Exception {
		log.verbose("Entering AcademyBOPISFindInventory.checkBopisItemExclusionList() :: "+XMLUtil.getElementXMLString(elePromiseLine));		
		Element eleInGetItemList = null;
		Element eleGetItemListTemplate = null;
		Element eleItem = null;
		Element eleExtn = null;
		Element eleBopisItemExclusionList = null;
		Element eleBopisItemExclusion = null;
		Document docInGetItemList = null;
		Document docOutGetItemList = null;
		Document docGetItemListTemplate = null;		
		
		//Form input document for getItemList API
		docInGetItemList = XMLUtil.createDocument(AcademyConstants.ELEM_ITEM);
		eleInGetItemList = docInGetItemList.getDocumentElement();
		eleInGetItemList.setAttribute(AcademyConstants.ATTR_ITEM_ID, elePromiseLine.getAttribute(AcademyConstants.ATTR_ITEM_ID));
		eleInGetItemList.setAttribute(AcademyConstants.ATTR_PROD_CLASS, elePromiseLine.getAttribute(AcademyConstants.ATTR_PROD_CLASS));
		eleInGetItemList.setAttribute(AcademyConstants.ATTR_UOM, elePromiseLine.getAttribute(AcademyConstants.ATTR_UOM));
		
		//Form getItemList API template, to fetch records from ACAD_BOPIS_ITEM_EXCLUSION Hangoff table.
		docGetItemListTemplate = XMLUtil.createDocument(AcademyConstants.ELE_ITEM_LIST);
		eleGetItemListTemplate = docGetItemListTemplate.getDocumentElement();		
		eleItem = docGetItemListTemplate.createElement(AcademyConstants.ELEM_ITEM);		
		eleItem.setAttribute(AcademyConstants.ATTR_ITEM_ID, AcademyConstants.STR_EMPTY_STRING);
		XMLUtil.appendChild(eleGetItemListTemplate, eleItem);		
		eleExtn = docGetItemListTemplate.createElement(AcademyConstants.ELE_EXTN);		
		eleExtn.setAttribute(AcademyConstants.ELE_EXTN, AcademyConstants.STR_EMPTY_STRING);
		XMLUtil.appendChild(eleItem, eleExtn);		
		eleBopisItemExclusionList = docGetItemListTemplate.createElement(AcademyConstants.ELE_ITEM_EXTN_BOPIS_ITEM_EXCLUSION_LIST);
		XMLUtil.appendChild(eleExtn, eleBopisItemExclusionList);		
		eleBopisItemExclusion = docGetItemListTemplate.createElement(AcademyConstants.ELE_ITEM_EXTN_BOPIS_ITEM_EXCLUSION);
		eleBopisItemExclusion.setAttribute(AcademyConstants.ATTR_STORE_NO, AcademyConstants.STR_EMPTY_STRING);
		eleBopisItemExclusion.setAttribute(AcademyConstants.ATTR_DELIVERY_METHOD, AcademyConstants.STR_EMPTY_STRING);
		XMLUtil.appendChild(eleBopisItemExclusionList, eleBopisItemExclusion);
		
		//Invoke getItemList to fetch details from ACAD_BOPIS_ITEM_EXCLUSION HangOff table 
		log.verbose("Input to getItemList : "+XMLUtil.getXMLString(docInGetItemList));
		log.verbose("getItemList API template : "+XMLUtil.getXMLString(docGetItemListTemplate));
		env.setApiTemplate(AcademyConstants.API_GET_ITEM_LIST, docGetItemListTemplate);
		docOutGetItemList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ITEM_LIST, docInGetItemList);
		env.clearApiTemplate(AcademyConstants.API_GET_ITEM_LIST);
		log.verbose("Output from getItemList : "+XMLUtil.getXMLString(docOutGetItemList));
		
		//Add all the excluded ShipNodes for Promiseline.ItemID, to an NodeList
		NodeList nlBopisItemExclusionStoreList = docOutGetItemList.getElementsByTagName(AcademyConstants.ELE_ITEM_EXTN_BOPIS_ITEM_EXCLUSION);
		
		if(nlBopisItemExclusionStoreList.getLength() > 0){
			log.verbose("ACAD_BOPIS_ITEM_EXCLUSION has some shipnodes excluded for Promiseline.Item : "+nlBopisItemExclusionStoreList.getLength());
			evalShipNodeExclusion(elePromiseLine, nlBopisItemExclusionStoreList);
		}
		
		log.verbose("Exiting AcademyBOPISFindInventory.checkBopisItemExclusionList()");
	}
	

	/** This method checks,
	 * 	If item-node combination is excluded, then shipNode is removed from <ShipNode> & the filtered ship nodes are added to the <ExcludeShipNode> tag at each PromiseLine.
	 * @param elePromiseLine
	 * @param nlBopisItemExclusionStoreList
	 */
	private void evalShipNodeExclusion(Element elePromiseLine, NodeList nlBopisItemExclusionStoreList) throws Exception {
		log.verbose("Entering AcademyBOPISFindInventory.evalShipNodeExclusion()");
		String strShipNode = null;		
		Element eleBopisItemExclusion = null;
		Element elePromiseLineShipNode = null;
		Element eleExcludedShipNode = null;
		List<String> storeExclusionList = new ArrayList<String>();
		Element eleExcludedShipNodes = elePromiseLine.getOwnerDocument().createElement(AcademyConstants.ELE_EXCLUDED_SHIP_NODES);	
		
		for(int i=0; i < nlBopisItemExclusionStoreList.getLength(); i++){
			eleBopisItemExclusion = (Element) nlBopisItemExclusionStoreList.item(i);
			storeExclusionList.add(eleBopisItemExclusion.getAttribute(AcademyConstants.ATTR_STORE_NO));
		}
		
		NodeList nlPromiseLineShipNode = elePromiseLine.getElementsByTagName(AcademyConstants.ATTR_SHIP_NODE);
		
		int iNlPromiseLineShipNodeLength = nlPromiseLineShipNode.getLength();
		for(int j=0; j < iNlPromiseLineShipNodeLength; j++){
			elePromiseLineShipNode = (Element) nlPromiseLineShipNode.item(j);
			strShipNode = elePromiseLineShipNode.getAttribute(AcademyConstants.ATTR_NODE);
			
			if(storeExclusionList.contains(strShipNode)){
				log.verbose("Excluding ShipNode due to ACAD_BOPIS_ITEM_EXCLUSION - "+strShipNode);
				eleExcludedShipNode = eleExcludedShipNodes.getOwnerDocument().createElement(AcademyConstants.ELE_EXCLUDED_SHIP_NODE);		
				eleExcludedShipNode.setAttribute(AcademyConstants.ATTR_NODE, strShipNode);
				//OMNI-4252 Node capacity Removed, Hence updated SuppressNodeCapacity to Y
				eleExcludedShipNode.setAttribute(AcademyConstants.ATTR_SUPPRESS_NODE_CAPACITY, AcademyConstants.STR_YES);
				//Note - Product Java docs - has a spelling mistake(Suppress vs Supress) 
				//<ExcludedShipNode Node="" SuppressNodeCapacity="N" SupressProcurement="Y" SupressSourcing="Y"/>
				eleExcludedShipNode.setAttribute(AcademyConstants.ATTR_SUPRESS_PROCUREMENT, AcademyConstants.STR_YES);
				eleExcludedShipNode.setAttribute(AcademyConstants.ATTR_SUPRESS_SOURCING, AcademyConstants.STR_YES);
				XMLUtil.appendChild(eleExcludedShipNodes, eleExcludedShipNode);
			}
		}
		
		if(eleExcludedShipNodes.getChildNodes().getLength() > 0){
			XMLUtil.appendChild(elePromiseLine, eleExcludedShipNodes);
		}	
		
		log.verbose("Exiting AcademyBOPISFindInventory.evalShipNodeExclusion() :: "+XMLUtil.getElementXMLString(elePromiseLine));
	}
}
