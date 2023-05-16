//package declaration
package com.academy.ecommerce.sterling.userexits;

//import statements

//java util import statements
import java.util.HashMap;
import java.util.Map.Entry;

//w3c import statements
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//academy import statements
import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.util.common.AcademyCustomException;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;

//yantra import statements
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.ue.YFSGetItemNodeCostForSourcingUE;
import com.yantra.yfs.japi.YFSUserExitException;

/*Input to this service is 
<ItemCostList EnterpriseCode="" OrganizationCode="">
    <ItemCost ItemID="" ProductClass="" UnitOfMeasure=""> 
       <Nodes>            
            <Node Qty="" ShipNode=""/>        
         </Nodes>   
      </ItemCost>
</ItemCostList> */

/** Description: Class AcademySFSGetItemNodeCostForSourcingUE stamps the 
 *              UnitCost for item present at each node
* @throws YFSUserExitException
*/

public class AcademySFSGetItemNodeCostForSourcingUE implements YFSGetItemNodeCostForSourcingUE
{
      //declaring the variables
      private static YIFApi api = null;

      static
      {
            try
            {
                  api = YIFClientFactory.getInstance().getApi();
            }
            catch (YIFClientCreationException e)
            {
                  e.printStackTrace();
            }
      }

      private static final YFCLogCategory log = YFCLogCategory.instance(AcademySFSGetItemNodeCostForSourcingUE.class);

      /**
      * Process the inout and stamps the unit cost for each shipnode for each distribution group 
       * @param env Yantra Environment Context
      * @param inDoc Input Document
      * @return inDoc
      */
      public Document getItemNodeCostForSourcing(YFSEnvironment env, Document inDoc) throws YFSUserExitException 
      {
            if(log.isVerboseEnabled())
            {
                  log.verbose("input to ue:"+XMLUtil.getXMLString(inDoc));
            }

            //Declare String varibale
            String strCodeType1 = AcademyConstants.DISTRIBUTION_GROUP_COST;
            String strOrganizationCode  = AcademyConstants.PRIMARY_ENTERPRISE;
            String strCodeType2 = AcademyConstants.ITEM_FULFILLMENT_TYPE;

            //Define hashmap and set in env object
            HashMap<String,String> hmDgUnitCost = (HashMap<String, String>) env.getTxnObject("AcademySFSGetItemNodeCostForSourcingUE.hmDgUnitCost");
            HashMap<String,String> hmDgFulfillType = (HashMap<String, String>) env.getTxnObject("AcademySFSGetItemNodeCostForSourcingUE.hmDgFulfillType");
            HashMap<String,String> hmShipNodeUnitCost = (HashMap<String, String>) env.getTxnObject("AcademySFSGetItemNodeCostForSourcingUE.hmShipNodeUnitCost");
            HashMap<String,String> hmStorageTypeFT = (HashMap<String, String>) env.getTxnObject("AcademySFSGetItemNodeCostForSourcingUE.hmStorageTypeFT");
            HashMap<String, String> hmItemFT = (HashMap<String, String>) env.getTxnObject("AcademySFSGetItemNodeCostForSourcingUE.hmItemFT");

            try
            {

                  //Check if hmDgUnitCost is null
                  if(hmDgUnitCost == null)
                  {
                        hmDgUnitCost = new HashMap<String,String>();
                        hmDgFulfillType = new HashMap<String,String>();

                        //calling method getCommonCodeList to get the hashmap(DG, Fulfillment type) and hashmap(DG,UnitCost)
                        Document DGFTUnitCostDoc = AcademyCommonCode.getCommonCodeList(env, strCodeType1, strOrganizationCode);

                        //Fetch the Nodelist CommonCode
                        NodeList nlGetCodeList1 = DGFTUnitCostDoc.getElementsByTagName("CommonCode");

                        //Loop through the nodelist CommonCode to populate the hashmap hmDgUnitCost and hmDgFulfillType
                        for(int i = 0 ; i < nlGetCodeList1.getLength() ; i++)
                        {
                              //Fetch the element CommonCode
                              Element eleCode = (Element)nlGetCodeList1.item(i);
                              //Fetch the attributes CodeValue,CodeShortDescription and CodeLongDescription
                              String strCodeValue = eleCode.getAttribute("CodeValue");
                              String strCodeShortDesc = eleCode.getAttribute("CodeShortDescription");
                              String strCodeLongDesc = eleCode.getAttribute("CodeLongDescription");
                              if(log.isVerboseEnabled())
                              {
                                    log.verbose("code:"+strCodeValue+" short:"+strCodeShortDesc+" strCodeLongDesc"+strCodeLongDesc);
                              }
                              //Populate the hashmap hmDgUnitCost with CodeValue and CodeShortDescription
                              hmDgUnitCost.put(strCodeValue, strCodeShortDesc);
                              //Populate the hashmap hmDgFulfillType with CodeValue and CodeLongDescription
                              hmDgFulfillType.put(strCodeValue, strCodeLongDesc);
                        }

                        //Set in env object
                        env.setTxnObject("AcademySFSGetItemNodeCostForSourcingUE.hmDgUnitCost", hmDgUnitCost);
                        env.setTxnObject("AcademySFSGetItemNodeCostForSourcingUE.hmDgFulfillType", hmDgFulfillType);
                  }

                  //Printing the hashmap hmDgUnitCost
                  if(log.isVerboseEnabled())
                  {
                        log.verbose("hmDgUnitCost-->");
                        for (Entry<String, String> entry : hmDgUnitCost.entrySet()) {
                              log.verbose(entry.getKey() + "    " + entry.getValue());
                        }
                  }

                  //Printing the hashmap hmDgFulfillType
                  if(log.isVerboseEnabled())
                  {
                        log.verbose("hmDgFulfillType-->");
                        for (Entry<String, String> entry : hmDgFulfillType.entrySet()) {
                              log.verbose(entry.getKey() + "    " + entry.getValue());
                        }
                  }

                  
                  //Check if hmStorageType is null
                  if(hmStorageTypeFT == null)
                  {
                        //calling method getCommonCodeList to get hashmap(StorageType,FT)
                        hmStorageTypeFT = AcademyCommonCode.getCommonCodeListAsHashMap(env, strCodeType2, strOrganizationCode);
                        env.setTxnObject("AcademySFSGetItemNodeCostForSourcingUE.hmStorageTypeFT", hmStorageTypeFT);
                  }

                  //Printing the hashmap hmStorageTypeFT
                  if(log.isVerboseEnabled())
                  {
                        log.verbose("hmStorageTypeFT-->");
                        for (Entry<String, String> entry : hmStorageTypeFT.entrySet()) {
                              log.verbose(entry.getKey() + "    " + entry.getValue());
                        }
                  }

                  
                  //Check if hmShipNodeUnitCost is null
                  if(hmShipNodeUnitCost == null)
                  {
                        //calling method getUnitCostForShipNode to get the unit cost for each shipnode
                        hmShipNodeUnitCost = getUnitCostForShipNode(env, hmDgUnitCost,hmDgFulfillType);
                        env.setTxnObject("AcademySFSGetItemNodeCostForSourcingUE.hmShipNodeUnitCost", hmShipNodeUnitCost);
                  }

                  //Printing the hashmap hmShipNodeUnitCost
                  if(log.isVerboseEnabled())
                  {
                        log.verbose("hmShipNodeUnitCost-->");
                        for (Entry<String, String> entry : hmShipNodeUnitCost.entrySet()) {
                              log.verbose(entry.getKey() + "    " + entry.getValue());
                        }
                  }

                  //Fetch the nodelist ItemCost
                  NodeList nlItemCostList = inDoc.getElementsByTagName("ItemCost");

                  //Check if hmItemFT is null
                  if(hmItemFT == null)
                  {
                        hmItemFT = new HashMap<String,String>();
                        env.setTxnObject("AcademySFSGetItemNodeCostForSourcingUE.hmItemFT", hmItemFT);
                  }

                  //calling method getItemList to form the complex query input to getItemList API and then get the output of the API
                  getItemList(env,nlItemCostList,hmStorageTypeFT, hmItemFT);              

                  //calling method stampUnitCost to stamp the unit cost for each node for non bulk items
                  stampUnitCost(env, hmItemFT,nlItemCostList,hmShipNodeUnitCost);

                  //printing output of UE
                  if(log.isVerboseEnabled())
                  {
                        log.verbose("output of ue:" + XMLUtil.getXMLString(inDoc));
                  }

            }
            catch (Exception e) 
            {
                  if(e instanceof YFSUserExitException) 
                  {
                        throw (YFSUserExitException)e;                        
                  }

                  throw( new YFSUserExitException(e.getMessage()));
            }
            //Return output xml
            return inDoc;
      }

      /**
      * Gets the unit cost for each shipnode for each distribution group 
       * @param env Yantra Environment Context.
      * @param hmDgUnitCost HashMap containing [DistributionGroup, Unit Cost]
      * @param hmDgFulfillType HashMap containing [DistributionGroup, FulfillmentType]
      * @return hmShipNodeUnitCost HashMap containing [ShipNode, UnitCost]
      */
      private HashMap<String,String> getUnitCostForShipNode(YFSEnvironment env, HashMap<String,String> hmDgUnitCost, HashMap<String,String> hmDgFulfillType) throws Exception
      {
            
            //Declare Document variable
            Document getDistributionListOutputDoc = null;
            
            //Define hasmap to store the Shipnode and unit cost values
            HashMap<String,String> hmShipNodeUnitCost = new HashMap<String,String>();

            //forming input for getDistributionList API
            Document getDistributionListInputDoc = XMLUtil.createDocument("ItemShipNode");
            //Creating root node
            Element eleRooElement = getDistributionListInputDoc.getDocumentElement();
            //Set the attributes ItemId and OwnerKey
            eleRooElement.setAttribute("ItemId","ALL");
            eleRooElement.setAttribute("OwnerKey",AcademyConstants.PRIMARY_ENTERPRISE);

            if(log.isVerboseEnabled())
            {
                  log.verbose("input to api is:" + XMLUtil.getXMLString(getDistributionListInputDoc));
            }

            //creating the output template for getDistributionList API
            Document templateDoc = YFCDocument.getDocumentFor("<ItemShipNodeList><ItemShipNode DistributionRuleId=\"\" ShipnodeKey=\"\" /></ItemShipNodeList>").getDocument();
            //Set the template for getDistributionList API
            env.setApiTemplate("getDistributionList", templateDoc);
            //Invoke getDistributionList API
            getDistributionListOutputDoc = api.getDistributionList(env,getDistributionListInputDoc);
            //Clear the template
            env.clearApiTemplate("getDistributionList");

            if(log.isVerboseEnabled())
            {
                  log.verbose("output of api is:" + XMLUtil.getXMLString(getDistributionListOutputDoc));
            }

            //Fetch the nodelist ItemShipNode
            NodeList nlItemShipNode = getDistributionListOutputDoc.getElementsByTagName("ItemShipNode");
            //Loop through the NodeList ItemShipNode
            for(int listIndex = 0  ;listIndex < nlItemShipNode.getLength() ; listIndex++)
            {
                  //Fetch the element ItemShipNode
                  Element eleItemShipNode = (Element) nlItemShipNode.item(listIndex);
                  //Fetch the attributes DistributionRuleId and ShipnodeKey
                  String strDistributionRuleId = eleItemShipNode.getAttribute("DistributionRuleId");
                  String strShipNode = eleItemShipNode.getAttribute("ShipnodeKey");

                  //Check if hashmap hmDgUnitCost contains the DistributionRuleId
                  if(hmDgUnitCost.containsKey(strDistributionRuleId))
                  {
                        //Fetch UnitCost form the hashmap
                        String strUnitCost = hmDgUnitCost.get(strDistributionRuleId);
                        //Fetch FulfillmentType from the hashmap
                        String strFT = hmDgFulfillType.get(strDistributionRuleId);

                        if(log.isVerboseEnabled())
                        {
                              log.verbose("unit cost:"+strUnitCost+"FT:"+strFT);
                        }

                        //Concatenate the shipnode with fulfillment type
                        strShipNode = strFT.concat("_").concat(strShipNode);

                        if(log.isVerboseEnabled())
                        {
                              log.verbose("ship node:"+strShipNode);
                        }

                        //Store the shipnode and unit cost in the hashmap
                        hmShipNodeUnitCost.put(strShipNode,strUnitCost);

                  }
            }
            //Return the hashmap
            return hmShipNodeUnitCost;
      }

      /**
      * Prepares input to getItemList API by extracting all the items in the input.
      * Forms a complex query to getItemList API 
       * @param env Yantra Environment Context.
      * @param nlItemCostList NodeList containing all the item elements.
      * @param hmStorageTypeFT HashMap containing [StorageType,FulfillmentType]
      * @param hmItemFT HashMap containing [ItemID,FulfillmentType]
      * @return void
      */
      private void getItemList(YFSEnvironment env,NodeList nlItemCostList,HashMap<String,String> hmStorageTypeFT,HashMap<String,String> hmItemFT) throws Exception
      {
            //Declare the document variable
            Document getItemListOutputDoc = null;
            //Declare the integer variable
            int itemCount = 0;

            //creating the input(complex query) for getItemList API
            Document getItemListInputDoc = YFCDocument.createDocument("Item").getDocument();
            //Creating root element 
            Element eleRootElement = getItemListInputDoc.getDocumentElement();

            //setting the values OrganizationCode and UnitOfMeasure
            eleRootElement.setAttribute("OrganizationCode", AcademyConstants.HUB_CODE);
            eleRootElement.setAttribute("UnitOfMeasure",AcademyConstants.UNIT_OF_MEASURE);

            //Create the element ComplexQuery
            Element eleComplexQuery = getItemListInputDoc.createElement("ComplexQuery");
            //Set the attribute Operator
            eleComplexQuery.setAttribute("Operator", "OR");
            //Create the element And
            Element eleAnd = getItemListInputDoc.createElement("And");
            //Append the element And to ComplexQuery
            eleComplexQuery.appendChild(eleAnd);
            //Create the element Or
            Element eleOr = getItemListInputDoc.createElement("Or");
            //Append the element Or to And
            eleAnd.appendChild(eleOr);

            //Loop through the nodelist ItemCostList
            for (int i = 0; i < nlItemCostList.getLength() ; i++)
            {
                  //Fetch the element ItemCost
                  Element eleItemCost = (Element) nlItemCostList.item(i);
                  //Fetch the attribute ItemID
                  String strItemID = eleItemCost.getAttribute("ItemID");

                  if(log.isVerboseEnabled())
                  {
                        log.verbose("item id:"+strItemID);
                  }

                  //Check if hashmap hmItemFT contains the ItemID
                  if(!hmItemFT.containsKey(strItemID))
                  {
                        //Increment the itemcount
                        itemCount++;
                        //Check if itemcount is 1
                        if(itemCount == 1)
                        {
                              //If yes, set ItemID
                              eleRootElement.setAttribute("ItemID", strItemID);                             
                        }
                        else
                        {
                              //Else, create the complex query and set all the ItemIDs
                              Element eleExp = getItemListInputDoc.createElement("Exp");
                              eleExp.setAttribute("Name", "ItemID");
                              eleExp.setAttribute("Value", strItemID);
                              eleOr.appendChild(eleExp);
                        }
                  }
            }

            //Check if itemcount=0
            if(itemCount == 0)
            {
                  return;
            }

            //Check if itemcount is greater than 0
            if(itemCount > 1)
            {
                  //If yes, append the complex query
                  eleRootElement.appendChild(eleComplexQuery);
            }

            if(log.isVerboseEnabled())
            {
                  log.verbose("getItemlist input :" + XMLUtil.getXMLString(getItemListInputDoc));
            }

            //    creating the output template for getItemList API
            Document templateDoc = YFCDocument.getDocumentFor("<ItemList><Item ItemID=\"\" UnitOfMeasure=\"\" ><ClassificationCodes StorageType=\"\" /></Item></ItemList>").getDocument();

            //setting the output template
            env.setApiTemplate("getItemList", templateDoc);

            //calling the API getItemList
            getItemListOutputDoc = api.getItemList(env, getItemListInputDoc);

            if(log.isVerboseEnabled())
            {
                  log.verbose("o/p of getItemList->" + XMLUtil.getXMLString(getItemListOutputDoc));
            }

            //Clear the template
            env.clearApiTemplate("getItemList");
            //Fetch the nodelist nlItemList
            NodeList nlItemList = getItemListOutputDoc.getElementsByTagName("Item");
            //Loop through the nodelist nlItemList
            for (int i = 0; i < nlItemList.getLength(); i++) 
            {
                  //Fetch the element Item
                  Element eleItem = (Element) nlItemList.item(i);
                  //Fetch the the value of attribute ItemID
                  String strItemID = eleItem.getAttribute("ItemID");
                  //Fetch the element ClassificationCodes
                  Element eleClassificationCodes = (Element) eleItem.getElementsByTagName("ClassificationCodes").item(0);
                  //Fetch the the value of attribute StorageType
                  String strStorageType = eleClassificationCodes.getAttribute("StorageType");

                  //Check if hmStorageTypeFT contains StorageType
                  if(hmStorageTypeFT.containsKey(strStorageType))
                  {
                        //If yes, then store it in the hashmap
                        hmItemFT.put(strItemID,hmStorageTypeFT.get(strStorageType));
                  }
                  else
                  {
                        //custom exception
                        AcademyCustomException customExcep = new AcademyCustomException
                        ("UNIDENTIFIED STORAGE TYPE","AcademySFSException","Storage Type for the item not configured in Common Code");
                        throw customExcep;
                  }
            }
      }

      /**
      * Stamps the unit cost for all the nodes of an item
      * @param env Yantra Environment Context.
      * @param nlItemCostList NodeList containing all the item elements.
      * @param hmShipNodeUnitCost HashMap containing [ShipNode,UnitCost]
      * @param hmItemFT HashMap containing [ItemID,FulfillmentType]
      * @return void
      */
      private void stampUnitCost(YFSEnvironment env, HashMap<String,String> hmItemFT,NodeList nlItemCostList, HashMap<String,String> hmShipNodeUnitCost)
      {
            //comparing the StorageType of the Item with the Common code value
            //Loop through the nodelist nlItemCostList
            for(int i = 0 ; i < nlItemCostList.getLength() ; i++)
            {
                  //Fetch the element ItemCost
                  Element eleItemCost = (Element)nlItemCostList.item(i);
                  //Fetch the value of attribute ItemID
                  String strItemID = eleItemCost.getAttribute("ItemID");
                  //Check if hashmap contains ItemID
                  if(hmItemFT.containsKey(strItemID))
                  {
                        //fetch the value of fulfillment type from hashmap
                        String strFT = hmItemFT.get(strItemID);
                        //fetch the nodelist Node
                        NodeList nlNodes = eleItemCost.getElementsByTagName("Node");
                        //Loop through the nodelist Node
                        for(int j = 0 ; j < nlNodes.getLength() ; j++)
                        {
                              //Fetch the element Node
                              Element eleNode = (Element)nlNodes.item(j);
                              //Fetch the attribute ShipNode
                              String strShipNode = eleNode.getAttribute("ShipNode");
                              //Concatenate shipnode with fulfillment type
                              strShipNode = strFT.concat("_").concat(strShipNode);
                              //Check if hashmap hmShipNodeUnitCost contains shipnode
                              if(hmShipNodeUnitCost.containsKey(strShipNode))
                              {
                                    //if yes, stamp the uit cost
                                    eleNode.setAttribute("UnitCost",hmShipNodeUnitCost.get(strShipNode));
                              }
                        }
                  }
            }
      }
}