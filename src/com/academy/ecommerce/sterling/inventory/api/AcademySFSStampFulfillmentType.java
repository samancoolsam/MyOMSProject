//package declaration

package com.academy.ecommerce.sterling.inventory.api;

//import statements

//java util import statements
import java.util.HashMap;

//w3c import statements
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//academy import statements
import com.academy.ecommerce.sterling.userexits.AcademySFSBeforeChangeOrderUE;
import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;

//yantra import statements
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * Description: Class AcademySFSStampFulfillmentType stamps the Fulfillment Type
 * for each promiseline depending on whether the item is a bulk or non bulk item
 * 
 * @throws Exception
 */
public class AcademySFSStampFulfillmentType
{

	private static final YFCLogCategory	log	= YFCLogCategory.instance(AcademySFSStampFulfillmentType.class);

	/**
	 * Process the input and stamps the fulfillment type for each promiseline
	 * 
	 * @param env
	 *            Yantra Environment Context.
	 * @param inDoc
	 *            Input Document.
	 * @return inDoc
	 */
	public Document stampFulfillmentTypeForPromiseLine(YFSEnvironment env, Document inDoc) throws Exception
	{

		// Declaring Variables
		String strOrganizationCode = AcademyConstants.PRIMARY_ENTERPRISE;
		String strCodeType = AcademyConstants.ITEM_FULFILLMENT_TYPE;

		// declaring hashmap
		HashMap<String, String> hmStorageTypeFT = new HashMap<String, String>();
		HashMap<String, String> hmItemFT = new HashMap<String, String>();

		// calling method getCommonCodeList to get hashmap[Item, FT]
		hmStorageTypeFT = AcademyCommonCode.getCommonCodeListAsHashMap(env, strCodeType, strOrganizationCode);

		// Getting all promiseline nodes
		NodeList nlPromiseLine = inDoc.getElementsByTagName("PromiseLine");

		// calling method GetItemList to form the complex query input to
		// getItemList API and then get the output of the API
		Document getItemListOutputDoc = AcademySFSBeforeChangeOrderUE.getItemList(env, nlPromiseLine);

		// calling method compareHashMap to compare the hashmaps		
		//start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
		//hmItemFT = AcademySFSBeforeChangeOrderUE.compareHashMap(getItemListOutputDoc, hmStorageTypeFT);
		hmItemFT = AcademySFSBeforeChangeOrderUE.compareHashMap(env, getItemListOutputDoc, hmStorageTypeFT);
		//End WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
		// calling method setFulfillmentType to stamp the fulfillment types
		
		setFulfillmentType(hmItemFT, nlPromiseLine);

		// printing output of UE
		if (log.isVerboseEnabled())
		{
			log.verbose("final output is " + XMLUtil.getXMLString(inDoc));
		}
		return inDoc;
	}

	/**
	 * From the input, gets the item in each promiseline and stamps the
	 * fulfillment type at the promiseline level
	 * 
	 * @param hmItemFT
	 *            HashMap containing [ItemID, FulfillmentType]
	 * @param nlItem
	 *            nlPromiseLine containing all the item elements.
	 * @return void
	 */
	private void setFulfillmentType(HashMap<String, String> hmItemFT, NodeList nlPromiseLine)
	{
		//Loop through each PromiseLine NodeList
		for (int i = 0; i < nlPromiseLine.getLength(); i++)
		{
			//Fetch the element PromiseLine
			Element elePromiseLine = (Element) nlPromiseLine.item(i);
			//Fetch the attribute value of ItemID
			String strItemID = elePromiseLine.getAttribute("ItemID");
			//Print the verbose log
			if (log.isVerboseEnabled())
			{
				log.verbose("Item ID:" + strItemID);
			}
			
			//SOF :: Start WN-2029 : WCS reservation call: WCS must send ShipNode and FulfillmentType
			// Check if the ItemID value exists in the haspmap entry
			//if (hmItemFT.containsKey(strItemID) 
			//For SOF promiseLines, WCS will only send FulfillmentType="SOF". Hence we just have to retain it.
			//BOPIS-264 – WCS-OMS Inventory Reservation (Real time Call)
			//For BOPIS promiseLines, WCS will only send FulfillmentType="BOPIS". Hence we just have to retain it & not override.
			//OMNI-6448 : STS Order creation similar to BOPIS
			//OMNI-14564 : eGift Card: eGC reservation call from WCS
			//For EGC promiseLines, WCS will only send FulfillmentType="EGC". Hence, we just have to retain it and not override.
			if (hmItemFT.containsKey(strItemID) && 
					!AcademyConstants.STR_SPECIAL_ORDER_FIREARMS.equals(elePromiseLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE))
					&& !AcademyConstants.STR_BUY_ONLINE_PICKUP_FROM_STORE.equals(elePromiseLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE))
					&& !AcademyConstants.STR_SHIP_TO_STORE.equals(elePromiseLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE))
					&& !AcademyConstants.STR_EGC.equals(elePromiseLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE)))
			//SOF :: End WN-2029 : WCS reservation call: WCS must send ShipNode and FulfillmentType
			{
				//if true, Set the attribute FulfillmentType with value from hashmap
				elePromiseLine.setAttribute("FulfillmentType", hmItemFT.get(strItemID));
			}
		}
	}
}
