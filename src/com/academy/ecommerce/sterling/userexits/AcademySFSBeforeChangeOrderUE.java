//package declaration
package com.academy.ecommerce.sterling.userexits;

//import statements

//java util import statements
import java.util.HashMap;

//w3c import statements
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//academy import statements
import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.common.AcademyCustomException;
import com.academy.util.xml.XMLUtil;

//yantra import statements
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSBeforeChangeOrderUE;

/** Description: Class AcademySFSBeforeChangeOrderUE stamps the Fulfillment Type
 *              for each new orderline in the order depending on whether the item 
 *              is a bulk or non bulk item
 * @throws YFSUserExitException
 */

public class AcademySFSBeforeChangeOrderUE implements YFSBeforeChangeOrderUE 
{

	//declaring the variables
	private static YIFApi api = null;

	static 
	{
		try {
			api = YIFClientFactory.getInstance().getApi();
		} catch (YIFClientCreationException e) {
			e.printStackTrace();
		}

	}

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySFSBeforeChangeOrderUE.class);

	/**
	 * Checks if there are any new orderlines and processes only the new orderlines 
	 * @param env Yantra Environment Context.
	 * @param inDoc Input Document.
	 * @return inDoc
	 */ 
	public Document beforeChangeOrder(YFSEnvironment env, Document inDoc)throws YFSUserExitException 
	{
		try 
		{
			//getting the list of item elements
			NodeList nlItem = XMLUtil.getNodeList(inDoc.getDocumentElement(),"OrderLines/OrderLine/Item");

			//Process the input only if there any new orderlines
			if(nlItem.getLength()>0){

				//printing input of UE
				if(log.isVerboseEnabled())
				{
					log.verbose("input to UE:" + XMLUtil.getXMLString(inDoc));
				}

				//declaring the variables
				String strOrganizationCode = AcademyConstants.PRIMARY_ENTERPRISE;
				String strCodeType = AcademyConstants.ITEM_FULFILLMENT_TYPE;

				//declaring hashmaps
				HashMap<String, String> hmCommonCodeList = null;
				HashMap<String, String> hmItemFT = null;

				//calling method getCommonCodeList to get the hashmap[Item, FT]
				hmCommonCodeList = AcademyCommonCode.getCommonCodeListAsHashMap(env, strCodeType, strOrganizationCode);

				//calling method GetItemList to form the complex query input to getItemList API and get the output of the API
				Document getItemListOutputDoc = getItemList(env,nlItem);

				//calling method compareHashMap to compare the hashmaps
				hmItemFT = compareHashMap(env, getItemListOutputDoc,hmCommonCodeList);

				//calling method stampFulfillmentType to stamp the fulfillment type
				stampFulfillmentType(hmItemFT,nlItem);

				//printing output of UE
				if(log.isVerboseEnabled())
				{
					log.verbose("output of UE:" + XMLUtil.getXMLString(inDoc));
				}
			}
			else
			{
				if(log.isVerboseEnabled())
				{
					log.verbose("No new orderlines in the order");
				}
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

		return inDoc;
	}

	/**
	 * Prepares input to getItemList API by extracting all the items in the input.
	 * Forms a complex query to getItemList API 
	 * @param env Yantra Environment Context.
	 * @param nlItem NodeList containing alll the item elements.
	 * @return getItemListOutputDoc output of getItemList API
	 */
	public static Document getItemList(YFSEnvironment env,NodeList nlItem) throws Exception
	{

		int itemCount = 0;

		//creating the input(complex query) for getItemList API
		Document getItemListInputDoc = YFCDocument.createDocument("Item").getDocument();
		Element eleRootElement = getItemListInputDoc.getDocumentElement();

		//setting the values
		eleRootElement.setAttribute("OrganizationCode", AcademyConstants.HUB_CODE);
		eleRootElement.setAttribute("UnitOfMeasure",AcademyConstants.UNIT_OF_MEASURE);

		Element eleComplexQuery = getItemListInputDoc.createElement("ComplexQuery");
		eleComplexQuery.setAttribute("Operator", "OR");
		Element eleAnd  =  getItemListInputDoc.createElement("And");
		eleComplexQuery.appendChild(eleAnd);
		Element eleOr  =  getItemListInputDoc.createElement("Or");
		eleAnd.appendChild(eleOr);

		for(int i = 0 ; i < nlItem.getLength() ; i++){
			Element eleItem =(Element)nlItem.item(i);
			String strItemID = eleItem.getAttribute("ItemID");

			if(log.isVerboseEnabled())
			{
				log.verbose("ItemID:"+strItemID);
			}

			itemCount++;
			if(itemCount == 1)
			{
				eleRootElement.setAttribute("ItemID", strItemID);					
			}
			else
			{
				Element eleExp = getItemListInputDoc.createElement("Exp");
				eleExp.setAttribute("Name", "ItemID");
				eleExp.setAttribute("Value", strItemID);
				eleOr.appendChild(eleExp);
			}
		}

		if(itemCount > 1)
		{
			eleRootElement.appendChild(eleComplexQuery);
		}

		if(log.isVerboseEnabled())
		{
			log.verbose("getItemlist input :" + XMLUtil.getXMLString(getItemListInputDoc));
		}

		//creating the output template for getItemList API 
		Document templateDoc = YFCDocument.getDocumentFor("<ItemList><Item ItemID=\"\" UnitOfMeasure=\"\" ><ClassificationCodes StorageType=\"\" HazmatClass=\"\" /></Item></ItemList>").getDocument();

		//setting the output template
		env.setApiTemplate("getItemList", templateDoc);

		//calling the API getItemList
		Document getItemListOutputDoc = api.getItemList(env, getItemListInputDoc);

		if(log.isVerboseEnabled())
		{
			log.verbose("o/p of getItemList->" + XMLUtil.getXMLString(getItemListOutputDoc));
		}

		env.clearApiTemplate("getItemList");

		return getItemListOutputDoc;

	}

	/**
	 * Compares the storage type of each item from the output
	 * of getItemList API and gets the Fulfillment Type from the 
	 * common code for the storage type.
	 * @param getItemListOutputDoc Document containg output of getItemList API
	 * @param hmCommonCodeList HashMap containing [StorageType, FulfillmentType]
	 * @return hmItemFT HashMap containing [ItemID, FulfillmentType]
	 * @throws Exception 
	 */
	public static HashMap<String, String> compareHashMap(YFSEnvironment env,Document getItemListOutputDoc,HashMap<String,String> hmCommonCodeList) throws Exception 
	{
		NodeList nlItemList = getItemListOutputDoc.getElementsByTagName("Item");
		HashMap<String,String> hmItemFT = new HashMap<String, String>();
		
		//Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
		HashMap hmHazmatCommonCodeList = new HashMap();
		String strOrganizationCode = AcademyConstants.PRIMARY_ENTERPRISE;
	    //End WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
	    
		//comparing the StorageType of the Item with the Common code value
		for (int i = 0; i < nlItemList.getLength(); i++) 
		{
			Element eleItem = (Element) nlItemList.item(i);
			String strItemID = eleItem.getAttribute("ItemID");
			Element eleClassificationCodes = (Element) eleItem.getElementsByTagName("ClassificationCodes").item(0);
			String strStorageType = eleClassificationCodes.getAttribute("StorageType");
			String hazmatClass = eleClassificationCodes.getAttribute(AcademyConstants.ATTR_HAZMAT_CLASS);
			
			//start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
			/*if (!YFCObject.isVoid(hazmatClass) &&  hazmatClass.equalsIgnoreCase(AcademyConstants.AMMO_HAZMAT_CLASS)) {
				// Forming map to Stamp fulfillment type as AMMO when hazmat class is C1.
				hmItemFT.put(strItemID,AcademyConstants.AMMO_FULFILLMENT_TYPE);
			}*/
			 if (!YFCObject.isVoid(hazmatClass))
			 {
				 hmHazmatCommonCodeList = AcademyCommonCode.getCommonCodeListAsHashMap(env, AcademyConstants.HAZMAT_CLASS_COMMON_CODE, strOrganizationCode);
				 if (hmHazmatCommonCodeList.containsKey(hazmatClass)) {
					 hmItemFT.put(strItemID, AcademyConstants.HAZMAT_FULFILLMENT_TYPE);
				 }
				 else {
					 hmItemFT.put(strItemID, (String)hmCommonCodeList.get(strStorageType));
				 }
			 }	     
			 //End WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
			else if(hmCommonCodeList.containsKey(strStorageType)){
				hmItemFT.put(strItemID,hmCommonCodeList.get(strStorageType));
			}
			else
			{
				//custom exception
				AcademyCustomException customExcep = new AcademyCustomException
				("UNIDENTIFIED STORAGE TYPE","AcademySFSException","Storage Type for the item not configured in Common Cde");
				throw customExcep;
			}
		}
		return hmItemFT;
	}

	/**
	 * From the input, gets the item in each orderline and 
	 * stamps the fulfillment type at the orderline level
	 * @param hmItemFT HashMap containing [ItemID, FulfillmentType]
	 * @param nlItem NodeList containing alll the item elements.
	 * @return void
	 */
	public static void stampFulfillmentType(HashMap<String,String> hmItemFT,NodeList nlItem) 
	{
		for(int k = 0 ; k < nlItem.getLength() ; k++)
		{
			Element eleItem = (Element)nlItem.item(k);
			String strItemID = eleItem.getAttribute("ItemID");
			log.verbose("item:"+strItemID);
			Element eleOrderLine = (Element) eleItem.getParentNode();
			
			//SOF :: Start WN-2032 : Create Order call: WCS must send ShipNode and FulfillmentType
			//eleOrderLine.setAttribute("FulfillmentType", hmItemFT.get(strItemID));
			String strFulfillmentType = eleOrderLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
			
			//For SOF orderLines, WCS will only send FulfillmentType="SOF" in CreateOrder XML. Hence we just have to retain it.
			//Start - BOPIS-11 : ASO_Order Capture And Processing 
			//For BOPIS orderLines, WCS will only send FulfillmentType="BOPIS". Hence we just have to retain it.
			boolean bStampFulfillmentType = true;
			//Start : OMNI-6620 : Skipping logic for STS Orders
			if(AcademyConstants.STR_SPECIAL_ORDER_FIREARMS.equals(strFulfillmentType) 
					|| AcademyConstants.STR_BUY_ONLINE_PICKUP_FROM_STORE.equals(strFulfillmentType)
					|| AcademyConstants.STR_SHIP_TO_STORE.equals(strFulfillmentType) 
					// Start: OMNI-5001 - Skipping logic for E-Gift Cards 
					|| AcademyConstants.STR_E_GIFT_CARD.equals(strFulfillmentType)){
					// End: OMNI-5001 - Skipping logic for E-Gift Cards
				bStampFulfillmentType = false;
			}
			//End : OMNI-6620 : Skipping logic for STS Orders
			//End - BOPIS-11 : ASO_Order Capture And Processing
			
			if(YFCObject.isVoid(strFulfillmentType) || bStampFulfillmentType){
				eleOrderLine.setAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE, hmItemFT.get(strItemID));
			}
			//SOF :: End WN-2032 : Create Order call: WCS must send ShipNode and FulfillmentType				
			
		}
	}
}