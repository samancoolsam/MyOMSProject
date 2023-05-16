//package declaration
package com.academy.ecommerce.sterling.item.api;
//java util import statements
import java.util.Properties;
import java.util.HashMap;
//w3c import statements
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
//academy import statements
import com.academy.ecommerce.sterling.shipment.AcademySFSPrintPendingShipments;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
//yantra import statements
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;


/**STL-1440. created by Netai Dey
 * Description: Class AcademySFSProcessPrintItemPickTicketsForMO is copied from AcademySFSProcessPrintItemPickTickets gets the list of all
 * items against the requested shipnode.
 *
 * @throws Exception
 */
public class AcademySFSProcessPrintItemPickTicketsForMO implements YIFCustomApi
{
	//log set up 
	private static final YFCLogCategory	log	= YFCLogCategory.instance(AcademySFSProcessPrintItemPickTicketsForMO.class);
	// Instance to store the properties configured for the condition in
	// Configurator
	private Properties	prop;
	// Stores property configured in configurator
	public void setProperties(Properties prop) throws Exception
	{
		this.prop = prop;
	}
	

	/**
	 * Retrieves item list based on ShipNode.
	 *
	 * @param env
	 *            Yantra Environment Context.
	 * @param inXML
	 *            Input Document Value.
	 * @return void
	 *
	 * Sample Input : <Print DisplayLocalizedFieldInLocale="en_US_EST" MaximumRecords=""
    	PickticketNo="admin20160224_1640" ShipNode="116">
    		<Login LoginID="admin" Password="password"/>
		</Print>
	 */
	public Document getItemPickLists(YFSEnvironment env, Document inXML) throws Exception
	{
		log.verbose("getItemPickLists inXML \n" + XMLUtil.getXMLString(inXML));
		// Declare String variable and set the value
		Element elePrint = inXML.getDocumentElement();
				
		log.verbose("AcademySFSGetItemStoreQtyService input  \n" + XMLUtil.getXMLString(inXML));
		Document docStoreItemPickQuantityList = AcademyUtil.invokeService(env, AcademyConstants.CUSTOM_ITEM_PICK_QTY_SERV,inXML);
		log.verbose("AcademySFSGetItemStoreQtyService OutPut \n" + XMLUtil.getXMLString(docStoreItemPickQuantityList));
		
		docStoreItemPickQuantityList.getDocumentElement().setAttribute(AcademyConstants.SHIP_NODE, elePrint.getAttribute(AcademyConstants.SHIP_NODE));
		
		NodeList storeItemPickQuantitys = docStoreItemPickQuantityList.getElementsByTagName(AcademyConstants.ELE_STORE_ITEM_PICK_QTY);
		//Fetch the count of the NodeList
		int itemLength = storeItemPickQuantitys.getLength();

		// Loop through each Shipment element
		for (int j = 0; j < itemLength; j++)
		{
			//Fetch the element StoreItemPickQuantity
			Element item = (Element) storeItemPickQuantitys.item(j);			
			String strItemID = item.getAttribute(AcademyConstants.ATTR_ITEM_ID);
			String strItemAliasValue = AcademyUtil.getItemAliasValueForItem(strItemID, env);
			if(!YFCObject.isVoid(strItemAliasValue))
			{
				log.verbose("************ALIAS VALUE IS NOT NULL***********************");
				item.setAttribute(AcademyConstants.ATTR_UPC_CODE,strItemAliasValue );
			}
			else
			{
				log.verbose("************ALIAS VALUE IS NULL***********************");
				item.setAttribute(AcademyConstants.ATTR_UPC_CODE, AcademySFSPrintPendingShipments.generateBarcode(strItemID));	
			}		
		}	
	
		log.verbose("ItemPickTicketDoc \n" + XMLUtil.getXMLString(docStoreItemPickQuantityList));
		return docStoreItemPickQuantityList;
	}
}