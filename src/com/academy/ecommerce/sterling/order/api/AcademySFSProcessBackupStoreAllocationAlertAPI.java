//package declaration
package com.academy.ecommerce.sterling.order.api;
/** this class raises the alert for backupStore allocation while scheduling
 * @author 250626
 */
//java util import statements
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
//w3c util import statements
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
//academy util import statements
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.academy.util.xml.XPathWrapper;
//yantra util import statements
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademySFSProcessBackupStoreAllocationAlertAPI
{
	/** log variable for logging messages */
	private static final YFCLogCategory	log	= YFCLogCategory.instance(AcademySFSProcessBackupStoreAllocationAlertAPI.class);
	
	/** this method gets called at ON_SUCCESS of schedule and checks for the lines which are sourced from backup Stores.
	 * and if finds any line with backup stores raises an alert with Store ID.
	 * @param env
	 * @param inDoc
	 * @return Document
	 * @throws Exception
	 * @author 250626
	 */
	public Document processBackupStoreAlert(YFSEnvironment env, Document inDoc) throws Exception
	{
		log.beginTimer(" Begin of AcademySFSProcessBackupStoreAllocationAlertAPI  processBackupStoreAlert()- Api");
		if (log.isVerboseEnabled())
		{
			log.verbose("************* Inside AcademySFSProcessBackupStoreAllocationAlertAPI ::AcademySFSProcessBackupStoreAllocationAlertAPI ***************");
			log.verbose("********* Input XML for method processBackupStoreAlert :: AcademySFSProcessBackupStoreAllocationAlertAPI" + XMLUtil.getXMLString(inDoc));
		}
		//Declare Document variable
		Document getItemListIPDoc = null;
		Document getItemListOutputDoc = null;
		//Declare hashmap variable
		HashMap<String, String> storageTypeFulfillmentTypeMap = new HashMap<String, String>();
		HashMap<String, String> itemFulfillmentTypeMap = new HashMap<String, String>();
		Set<String> eligibleCombForAlert = new HashSet<String>();
		//Fetch the root element from the input xml
		Element inDocRoot = inDoc.getDocumentElement();
		// Getting Current organizationCode, OrderNo and OrderHeaderKey
		String organizationCode = inDocRoot.getAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE);
		String currOrderNo = inDocRoot.getAttribute(AcademyConstants.ATTR_ORDER_NO);
		String currOrderHeaderkey = inDocRoot.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);		
		// Getting Eligible Lines (Lines in Status Scheduled and Release )
		NodeList eligOrderLinesNodeList = getEligOrderLines(inDoc);
		// If any eligible line are available for processing
		if (eligOrderLinesNodeList.getLength() > 0)
		{
			//Fetch the ITEM_FULFILLMENT_TYPE from CommonCode. Store it in hashmap
			storageTypeFulfillmentTypeMap = AcademyCommonCode.getCommonCodeListAsHashMap(env, AcademyConstants.ITEM_FULFILLMENT_TYPE, organizationCode);
			//invoke method to prepare input for getItemList
			getItemListIPDoc = prepareGetItemList(eligOrderLinesNodeList);
			//set the template for getItemList API
			env.setApiTemplate(AcademyConstants.API_GET_ITEM_LIST, "global/template/api/getItemList_forBackupStoreAlert_OutputTemplate.xml");
			//Invoke getItemList API
			getItemListOutputDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ITEM_LIST, getItemListIPDoc);
			//Invoke method getItemFulfillmentTypeMap to fetch the FulfillmentType
			itemFulfillmentTypeMap = getItemFulfillmentTypeMap(getItemListOutputDoc, storageTypeFulfillmentTypeMap);
			//Invoke method getEligibleNodes to fetch the FulfillmentType
			eligibleCombForAlert = getEligibleNodes(env, eligOrderLinesNodeList, organizationCode, itemFulfillmentTypeMap);
			//Raise Alert
			createAlertForExceptions(env, eligibleCombForAlert, currOrderNo, currOrderHeaderkey);
			//clear the template
			env.clearApiTemplates();
			log.endTimer(" End of AcademySFSProcessBackupStoreAllocationAlertAPI  processBackupStoreAlert()- Api");
		}
		//Return the xml
		return inDoc;
	}
	/** This method get the eligible ShipNode that are in Scheduled or Released Status.
	 * @param env
	 * @param NodeList eligOrderLinesNodeList
	 * @param string organizationCode
	 * @param hashmap itemFulfillmentTypeMap
	 * @return Document
	 * @throws Exception
	 * @author 250626
	 */
	private Set<String> getEligibleNodes(YFSEnvironment env, NodeList eligOrderLinesNodeList, String organizationCode, HashMap<String, String> itemFulfillmentTypeMap) throws Exception
	{
		log.beginTimer(" Begin of AcademySFSProcessBackupStoreAllocationAlertAPI  getEligibleNodes()- Api");
		//Fetch CC_BACKUP_DG details from common code and store it in hashmap
		HashMap<String, String> itemFulfillTypeDistGroupMapping = AcademyCommonCode.getCommonCodeListAsHashMap(env, AcademyConstants.CC_BACKUP_DG, organizationCode);
		//Declare the Set for mapping FulfillmentType and ShipNode
		Set<String> fulfillTypeShipNodeMapping = new HashSet<String>();
		//Declare the Set for Alert
		Set<String> eligibleCombForAlert = new HashSet<String>();
		//Declare the iterator and store the value of itemFulfillTypeDistGroupMapping
		Iterator iterator = itemFulfillTypeDistGroupMapping.keySet().iterator();
		//Go through while loop till there is record
		while (iterator.hasNext())
		{
			//Fetch the distribution group id value from the hashmap
			String keyDistGrpID = iterator.next().toString();
			//Start: Process for getDistributionList API
			//Create root node
			Document distributionListInputDoc = YFCDocument.createDocument("ItemShipNode").getDocument();
			Element itemShipNode = distributionListInputDoc.getDocumentElement();
			//Map the value for attribute DistributionRuleId
			itemShipNode.setAttribute("DistributionRuleId", keyDistGrpID);
			//Map the value for attribute OwnerKey
			itemShipNode.setAttribute("OwnerKey", organizationCode);
			//Set the template for getDistributionList API
			env.setApiTemplate(AcademyConstants.API_GET_DISTRIBUTION_LIST, "global/template/api/getDistributionList_forBackupStoreAlert_OutputTemplate.xml");
			//Invoke API getDistributionList
			Document distributionListOutputDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_DISTRIBUTION_LIST, distributionListInputDoc);
			//End: Process for getDistributionList API
			//Fetch the NodeList of node ItemShipNode
			NodeList itemShipNodeList = distributionListOutputDoc.getElementsByTagName(AcademyConstants.ELE_ITEM_SHIP_NODE);
			//Loop through each NodeList record
			for (int i = 0; i < itemShipNodeList.getLength(); i++)
			{
				//Fetch the root element
				Element itemShipNodeElement = (Element) itemShipNodeList.item(i);
				//Fetch the attribute value of ShipnodeKey
				String currentShipNodeKey = itemShipNodeElement.getAttribute(AcademyConstants.ATTR_SHIP_NODE_KEY);
				//Add the value of ShipnodeKey into the hashmap
				fulfillTypeShipNodeMapping.add(itemFulfillTypeDistGroupMapping.get(keyDistGrpID) + "," + currentShipNodeKey);
				//Print verbose log
				if (log.isVerboseEnabled())
				{
					log.verbose("itemFulfillTypeDistGroupMapping current value " + itemFulfillTypeDistGroupMapping.get(keyDistGrpID) + "& currentShipNodeKey" + currentShipNodeKey);
				}
			}
		}
		//Loop through the NodeList of element OrderLine
		for (int i = 0; i < eligOrderLinesNodeList.getLength(); i++)
		{
			//Fetch the element OrderLine
			Element orderLineElement = (Element) eligOrderLinesNodeList.item(i);
			//Fetch the attribute Item/@ItemID
			String currItemID = XPathUtil.getString(orderLineElement, AcademyConstants.XPATH_ITEM_ITEMID);
			//Fetch the FulfillmentType of the ItemId stored in hashmap
			String currFulfillType = (String) itemFulfillmentTypeMap.get(currItemID);
			//Create new instance of XPathWrapper
			XPathWrapper xpathWrapper = new XPathWrapper(orderLineElement);
			//Fetch the Nodelist of element OrderStatus where status is in Scheduled or Released
			NodeList orderStatusNodeList = xpathWrapper.getNodeList(AcademyConstants.XPATH_SCHEDULED_STATUSLINES);
			//Loop through each NodeList record
			for (int j = 0; j < orderStatusNodeList.getLength(); j++)
			{
				//Fetch the element OrderStatus
				Element orderStatusElement = (Element) orderStatusNodeList.item(j);
				//Fetch the attribute value of ShipNode
				String shipNodeKey = orderStatusElement.getAttribute(AcademyConstants.SHIP_NODE);
				//Store the FulfillmentType and ShipNodeKey into a variable
				String currShipNodeFulfillType = currFulfillType + "," + shipNodeKey;
				//Check if hashmap contains the value of variable currShipNodeFulfillType
				if (fulfillTypeShipNodeMapping.contains(currShipNodeFulfillType))
				{	//Add the ShipNodes into the set
					eligibleCombForAlert.add(shipNodeKey);
				}
			}
		}
		log.endTimer(" End of AcademySFSProcessBackupStoreAllocationAlertAPI  getEligibleNodes()- Api");
		//Return set values
		return eligibleCombForAlert;
	}
	/** This method fetches all the orderline that are in schedules or release statuses.
	 * @param inDoc
	 * @return NodeList
	 * @throws Exception
	 */
	private NodeList getEligOrderLines(Document inDoc) throws Exception
	{
		//Set the logger
		log.beginTimer(" Begin of AcademySFSProcessBackupStoreAllocationAlertAPI  getEligOrderLines()- Api");
		//Fetch the NodeList of element OrderLine
		NodeList orderLinesNodeList = inDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
		//Loop through each nodelist element
		for (int i = 0; i < orderLinesNodeList.getLength(); i++)
		{
			//Fetch the element OrderLine
			Element eligOrderLineElm = (Element) orderLinesNodeList.item(i);
			//Declare a new XPathWrapper for element OrderLine
			XPathWrapper xpathWrapper1 = new XPathWrapper(eligOrderLineElm);
			//Fetch the orderline that are in scheduled or released statuses
			NodeList eligStatusNodeList = xpathWrapper1.getNodeList(AcademyConstants.XPATH_SCHEDULED_STATUSLINES);
			//Check if the count of the NodeList eligStatusNodeList is zero
			if (eligStatusNodeList.getLength() == 0)
			{
				//Remove this  node from its parent node
				eligOrderLineElm.getParentNode().removeChild(eligOrderLineElm);
				i--;
			}
		}
		//Print the verbose log
		if (log.isVerboseEnabled())
		{
			log.verbose("********* Modified XML from method getEligOrderLines :: AcademySFSProcessBackupStoreAllocationAlertAPI" + XMLUtil.getXMLString(inDoc));
		}
		log.endTimer(" End of AcademySFSProcessBackupStoreAllocationAlertAPI  getEligOrderLines()- Api");
		//Return the OrderLine NodeList
		return orderLinesNodeList;

	}

	/** This method creates the input for Alert generation, call createException to Raise alert.	 * 
	 * @param env
	 * @param itemStorageList
	 * @param currOrderNo
	 * @param currOrderHeaderkey
	 * @throws Exception
	 * @author 250626
	 */
	private void createAlertForExceptions(YFSEnvironment env, Set<String> itemStorageList, String currOrderNo, String currOrderHeaderkey) throws Exception
	{
		log.beginTimer(" Begin of AcademySFSProcessBackupStoreAllocationAlertAPI  createAlertForExceptions()- Api");
		//Create element Inbox
		Document createExceptionInputDoc = YFCDocument.createDocument(AcademyConstants.ELE_INBOX).getDocument();
		for (String filteredNode : itemStorageList)
		{	
			Element inboxElm = createExceptionInputDoc.getDocumentElement();
			//Set the value for attribute ExceptionType
			inboxElm.setAttribute("ExceptionType", AcademyConstants.BACKUP_STORE_ALERT);
			//Set the value for attribute InboxType
			inboxElm.setAttribute("InboxType", AcademyConstants.SCHEDULE_ORDER_TX);
			//Set the value for attribute Description
			inboxElm.setAttribute("Description", filteredNode);
			//Set the value for attribute QueueId
			inboxElm.setAttribute("QueueId", AcademyConstants.SFS_BACKUP_ALLOCATION_Q);
			//Set the value for attribute Consolidate
			inboxElm.setAttribute("Consolidate", "Y");
			//Set the value for attribute ConsolidationWindow
			inboxElm.setAttribute("ConsolidationWindow", "DAY");
			//Create element ConsolidationTemplate
			Element consolidationTemplate = createExceptionInputDoc.createElement("ConsolidationTemplate");
			//Append child element
			inboxElm.appendChild(consolidationTemplate);
			//Create element Inbox
			Element consolidationTemplateInbox = createExceptionInputDoc.createElement("Inbox");
			//Append child element
			consolidationTemplate.appendChild(consolidationTemplateInbox);
			//Set the value for attribute ExceptionType
			consolidationTemplateInbox.setAttribute("ExceptionType", "");
			//Set the value for attribute InboxType
			consolidationTemplateInbox.setAttribute("InboxType", "");
			//Set the value for attribute Description
			consolidationTemplateInbox.setAttribute("Description", "");
			//Set the value for attribute ActiveFlag
			consolidationTemplateInbox.setAttribute("ActiveFlag", "");
			//Set the value for attribute GeneratedOn
			consolidationTemplateInbox.setAttribute("GeneratedOn", "");

			// <ConsolidationTemplate> for Order
			// <Inbox ActiveFlag="" ExceptionType="" InboxType=""
			// OrderHeaderKey=""

			// <ConsolidationTemplate> Generic
			// <Inbox ActiveFlag="" Description="" ExceptionType="" InboxType=""
			//Print the verbose log
			if (log.isVerboseEnabled())
			{
				log.verbose("********* Input XML for createException from method createAlertForExceptions :: AcademySFSProcessBackupStoreAllocationAlertAPI" + XMLUtil.getXMLString(createExceptionInputDoc));
			}
			//Invoke createException API
			AcademyUtil.invokeAPI(env, AcademyConstants.API_CREATE_EXCEPTION, createExceptionInputDoc);
		}
		log.endTimer(" End of AcademySFSProcessBackupStoreAllocationAlertAPI  createAlertForExceptions()- Api");

	}
	
	/** this method finds Fulfillment type for any item based on thr storage type.	 * 
	 * @param getItemListOutputDoc
	 * @param storageTypeFulfillmentTypeMap
	 * @author 250626
	 * @return HashMap<String, String>
	 */

	private HashMap<String, String> getItemFulfillmentTypeMap(Document getItemListOutputDoc, HashMap<String, String> storageTypeFulfillmentTypeMap)
	{
		log.beginTimer(" Begin of AcademySFSProcessBackupStoreAllocationAlertAPI  getItemFulfillmentTypeMap()- Api");
		//Declare the hashmap
		HashMap<String, String> itemFulfillmentTypeMap = new HashMap<String, String>();
		//Fetch the NodeList of element Item
		NodeList itemNodeList = getItemListOutputDoc.getElementsByTagName(AcademyConstants.ITEM);
		//Loop through the NodeList record
		for (int i = 0; i < itemNodeList.getLength(); i++)
		{
			//Fetch the element Item
			Element currentItemElement = (Element) itemNodeList.item(i);
			//Fetch the attribute value of ItemID
			String currentItemID = currentItemElement.getAttribute(AcademyConstants.ITEM_ID);
			//Fetch the elemet ClassificationCodes
			Element classificationCodesElement = (Element) currentItemElement.getElementsByTagName(AcademyConstants.ELE_CLASSIFICATION_CODES).item(0);
			//Fetch the attribute value of StorageType
			String currentStorageType = classificationCodesElement.getAttribute(AcademyConstants.ATTR_STORAGE_TYPE);
			//Print the verbose log
			if (log.isVerboseEnabled())
			{
				log.verbose("********* Current Storage ID for " + currentItemID + ": is: " + currentStorageType);
			}
			//Fetch the FulfillmentType value from the hashmap
			String currentFulfillmentType = storageTypeFulfillmentTypeMap.get(currentStorageType);
			//Check the FulfillmentType is not null
			if (currentFulfillmentType != null)
			{
				//Print the verbose log
				if (log.isVerboseEnabled())
				{
					log.verbose("********* Inside getItemFulfillmentTypeMap Check storageTypeMap from commonCode for " + currentItemID + ": is: " + storageTypeFulfillmentTypeMap.get(currentStorageType));
				}
				//Put the value of ItemID and FulfillmentType into hashmap
				itemFulfillmentTypeMap.put(currentItemID, currentFulfillmentType);
			}
		}
		log.endTimer(" End of AcademySFSProcessBackupStoreAllocationAlertAPI  getItemFulfillmentTypeMap()- Api");
		//Return the hashmap
		return itemFulfillmentTypeMap;
	}
	
	/** This method fetches prepare input xml structure for getItemList API.
	 * @param NodeList of element OrderLine
	 * @return Document with following xml structure
	 * @throws Exception
	 */
	private Document prepareGetItemList(NodeList eligOrderLinesNodeList)
	{
		//Set the logger
		log.beginTimer(" Begin of AcademySFSProcessBackupStoreAllocationAlertAPI  prepareGetItemList()- Api");
		// creating the input Document(complex query) for getItemList API
		Document getItemListInputDoc = YFCDocument.createDocument("Item").getDocument();
		//Fetch the root element
		Element itemElem = getItemListInputDoc.getDocumentElement();
		//Declare the set interface
		Set<String> eligibleItemList = new HashSet<String>();		
		//Set the attribute value of OrganizationCode
		itemElem.setAttribute("OrganizationCode", "DEFAULT");
		//Set the attribute value of UnitOfMeasure
		itemElem.setAttribute("UnitOfMeasure", "EACH");
		//Create element ComplexQuery
		Element eleComplexQuery = getItemListInputDoc.createElement("ComplexQuery");
		//Set the attribute value of Operator
		eleComplexQuery.setAttribute("Operator", "OR");
		//Create element And
		Element eleAnd = getItemListInputDoc.createElement("And");
		//append the element
		eleComplexQuery.appendChild(eleAnd);
		//Create element Or
		Element eleOr = getItemListInputDoc.createElement("Or");
		//Append the element
		eleAnd.appendChild(eleOr);
		//Loop through NodeList OrderLine
		for (int i = 0; i < eligOrderLinesNodeList.getLength(); i++)
		{
			//Fetch the element OrderLine
			Element orderLineElement = (Element) eligOrderLinesNodeList.item(i);
			//Fetch the element Item
			Element itemElement = (Element) orderLineElement.getElementsByTagName("Item").item(0);
			//Fetch the attribute value of ItemID
			String itemID = itemElement.getAttribute("ItemID");
			//check if the value of i is zero
			if (i == 0)
			{
				//Map the value of ItemID
				itemElem.setAttribute("ItemID", itemID);
				//add ItemID into setlist
				eligibleItemList.add(itemID);
			} else
			{	//Check if set eligibleItemList does not contain ItemID
				if (!eligibleItemList.contains(itemID))
				{
					//Declare the element variable
					Element expElement = null;
					//Create element Exp
					expElement = getItemListInputDoc.createElement("Exp");
					//Set the attribute value Name
					expElement.setAttribute("Name", "ItemID");
					//Set the attribute value Value
					expElement.setAttribute("Value", itemID);
					//Append the child
					eleOr.appendChild(expElement);
					//Add the ItemId into the set list
					eligibleItemList.add(itemID);
				}
			}
		}
		//Check if set eligibleItemList size is greater than 1
		if (eligibleItemList.size() > 1)
		{
			//Append element ComplexQuery
			itemElem.appendChild(eleComplexQuery);
		}
		//Print the verbose
		if (log.isVerboseEnabled())
		{
			log.verbose("********* Input XML for getItemList from method prepareGetItemList :: AcademySFSProcessBackupStoreAllocationAlertAPI" + XMLUtil.getXMLString(getItemListInputDoc));
		}

		log.endTimer(" End of AcademySFSProcessBackupStoreAllocationAlertAPI  prepareGetItemList()- Api");
		//Return the xml
		return getItemListInputDoc;
	}
}