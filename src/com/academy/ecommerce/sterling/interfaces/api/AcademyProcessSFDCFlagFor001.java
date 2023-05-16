//Package Declaration
package com.academy.ecommerce.sterling.interfaces.api;

//Import Statements Declaration
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.util.xml.XMLUtil;
import com.academy.util.common.AcademyUtil;	
import com.academy.util.constants.AcademyConstants;

import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * Service :: AcademySFSFullInventoryLoadService
 * 
 * Input :: 
 * 
 * <Items YantraMessageGroupID="Required" ShipNode="Required" CompleteInventoryFlag="N" ApplyDifferences="Y">
 * <Item UnitOfMeasure="EACH" ProductClass="GOOD" ItemID="Required" InventoryOrganizationCode="DEFAULT">
 * <Supplies>   
 * <Supply SupplyType="ONHAND" Quantity="Required" AvailabilityType="TRACK">
 * </Supply> 
 * </Supplies> 
 * </Item> 
 * </Items>
 * 
 * Input coming to this condition is from IP (External System),
 * 	as part of Full Sync Inventory Updates.
 * From the input if the Ship Node is "001" then for Item SFDC Flag is "N" then
 * making the inventory as "0" for that Item and ShipNode Combination.
 */
public class AcademyProcessSFDCFlagFor001 {

	// Set the logger configuration
	private static YFCLogCategory	log	= YFCLogCategory.instance(AcademyProcessSFDCFlagFor001.class);

	public Document processSFDCFlag(YFSEnvironment env, Document inDoc) throws Exception
	{
		//START: SHIN-4
		HashMap<String, String> shipNodeSFDCMap = AcademyCommonCode.getCommonCodeListAsHashMap(env, AcademyConstants.CC_SFDCShipNode, AcademyConstants.HUB_CODE);
		//END: SHIN-4
		
		log.verbose("Inxml is :: "+ XMLUtil.getXMLString(inDoc));

		String strShipNode = inDoc.getDocumentElement().getAttribute("ShipNode");
		log.verbose("Ship Node is :: "+ strShipNode);

		//commented as part of SHIN-4
		//String strShipNodeFromCC = invokeGetCommonCodeList(env);
		
		//START: SHIN-4
		//Modified the condition as part of SHIN-4
		//if(strShipNode.equals(strShipNodeFromCC))
		
		
		if (shipNodeSFDCMap.containsKey(strShipNode))
		{
		//END: SHIN-4
		
			log.verbose("ShipNode is SFDCShipNode:" +strShipNode);
			NodeList nlItem = inDoc.getElementsByTagName("Item");

			log.verbose("Item NL Length :: "+ nlItem.getLength());

			for(int i=0;i<nlItem.getLength();i++)
			{
				Element eleItem = (Element) nlItem.item(i);
				String strItemID = eleItem.getAttribute("ItemID");
				log.verbose("Item ID :: "+ strItemID);

				String strIsSFDCFlag = invokeGetItemDetails(env,strItemID);

				if(strIsSFDCFlag.equalsIgnoreCase("N"))
				{
					log.verbose("SFDC Flag N");
					Element eleSupply = (Element) eleItem.getElementsByTagName("Supply").item(0);
					eleSupply.setAttribute("Quantity", "0");

				}

			}
		}
		log.verbose("Final OutPut XML ::"+ XMLUtil.getXMLString(inDoc));
		return inDoc;

	}

	/**
	 * @param env
	 * @return Invoking the getCommonCodeList API 
	 * 		   and getting CodeShortDescription (ShipNode 001) configured under the Common Code Type "SFDCShipNode"
	 * 		   returning the same.
	 * @throws Exception
	 */
	//START: SHIN-4
	//Commented as part of SHIN-4
	/*public String invokeGetCommonCodeList(YFSEnvironment env) throws Exception
	{
		Document getCommonCodeListInDoc = XMLUtil.createDocument("CommonCode");
		
		getCommonCodeListInDoc.getDocumentElement().setAttribute("CodeType", "SFDCShipNode");
		
		Document getCommonCodeListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMONCODE_LIST, getCommonCodeListInDoc);

		log.verbose("getCommonCodeList API Output:"+ XMLUtil.getXMLString(getCommonCodeListOutDoc));

		String strShipNodeFromCC = ((Element) getCommonCodeListOutDoc.getElementsByTagName("CommonCode").item(0)).getAttribute("CodeShortDescription");

		return strShipNodeFromCC;

	}*/
	//END: SHIN-4

	/**
	 * @param env
	 * @param strItemID
	 * @return invoking the getItemDetails API, and
	 * 		    getting the ExtnShpFromDC Attribute from Output and returning the same.
	 * @throws Exception
	 */
	public String invokeGetItemDetails(YFSEnvironment env, String strItemID) throws Exception
	{
		Document getItemDetailsInDoc = XMLUtil.createDocument("Item");

		getItemDetailsInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_ITEM_ID, strItemID);
		getItemDetailsInDoc.getDocumentElement().setAttribute(AcademyConstants.ORG_CODE_ATTR, AcademyConstants.CATALOG_ORG_CODE);
		getItemDetailsInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_UOM, AcademyConstants.UOM_EACH_VAL);

		log.verbose("getItemDetails inDoc:: "+ XMLUtil.getXMLString(getItemDetailsInDoc));

		//Document getItemDetailsTemplate = XMLUtil.createDocument("<Item ItemID=''> <Extn ExtnShpFromDC='' /></Item>");
		Document getItemDetailsTemplate = YFCDocument.getDocumentFor("<Item ItemID=''> <Extn ExtnShpFromDC='' /></Item>").getDocument();

		env.setApiTemplate(AcademyConstants.API_GET_ITEM_DETAILS, getItemDetailsTemplate);

		//invoking getItemDetails API to get ExtnShpFromDC flag for the Item
		//Start-Change for STL-740//
		Document getItemDetailsOutDoc = null;
	
		String strIsSFDCFlag = "";
				try {
			log.verbose("getItemDetails Input:" + XMLUtil.getXMLString(getItemDetailsInDoc));
			getItemDetailsOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ITEM_DETAILS, getItemDetailsInDoc);			
		} catch (YFSException yfsEx) {
			// Exception Occurred.
			log.verbose("Unable to fetch SFDC flag value for Item:" + strItemID);
			env.clearApiTemplate(AcademyConstants.API_GET_ITEM_DETAILS);
			return strIsSFDCFlag;
		}
		//End-Change for STL-740//
		env.clearApiTemplate(AcademyConstants.API_GET_ITEM_DETAILS);
		log.verbose("getItemDetails output - " + XMLUtil.getXMLString(getItemDetailsOutDoc));
		strIsSFDCFlag = ((Element) getItemDetailsOutDoc.getElementsByTagName("Extn").item(0)).getAttribute("ExtnShpFromDC");
		
		return strIsSFDCFlag;
	}

}
