//Package Declaration
package com.academy.ecommerce.sterling.condition;

//Import Statements Declaration
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;

import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * Service :: AcademySFSTrickleInventoryFeedService
 * 
 * Input :: 
 * 
 * <Items>
 * <Item UnitOfMeasure="EACH" SupplyType="ONHAND" ShipNode="Required" Quantity="Required" ProductClass="GOOD"
 * OrganizationCode="DEFAULT" ItemID="Required" AvailabilityType="TRACK" AdjustmentType="ADJUSTMENT">
 * </Item>
 * </Items> 
 * 
 * Note :: Quantity to be adjusted. Negative value to decrease inventory and positive value to increase inventory.
 * Input coming to this condition is from IP (External System), 
 * 		as part of trickle feed inventory updates.
 * From the Input message if the ShipNode is "001" and SFDC Flag is "N" then returning TRUE
 * in other cases returning FALSE.
 */
public class AcademyIsSFDCFlagIsN  implements YCPDynamicConditionEx {

	//Set the logger configuration
	private static YFCLogCategory	log	= YFCLogCategory.instance(AcademyIsSFDCFlagIsN.class);


	public boolean evaluateCondition(YFSEnvironment env, String arg1, Map arg2, Document inDoc)  {
		log.verbose("Inxml is :: "+ XMLUtil.getXMLString(inDoc));
		
		//START: SHIN-4
		
			
		try {
			HashMap<String, String> shipNodeSFDCMap = AcademyCommonCode
					.getCommonCodeListAsHashMap(env, AcademyConstants.CC_SFDCShipNode,
							AcademyConstants.HUB_CODE);
			
		
		//END: SHIN-4

		NodeList nlItem = inDoc.getElementsByTagName("Item");

		for(int i=0;i<nlItem.getLength();i++)
		{
			Element eleItem = (Element) nlItem.item(i);
			String strShipNode = eleItem.getAttribute("ShipNode");
			String strItemID = eleItem.getAttribute("ItemID");

			log.verbose("Ship Node is :: "+ strShipNode);
			log.verbose("Item ID is :: "+ strItemID);
			
			//START: SHIN-4
			//Commented as part of SHIN-4
			//String strShipNodeFromCC;
			
			//Commented try block alone as part of SHIN-4 
			//try {
				
				//Commented as part of SHIN-4
				//strShipNodeFromCC = invokeGetCommonCodeList(env);
				//Modified the below condition as part of SHIN-4
				//if(strShipNode.equals(strShipNodeFromCC))
				//END: SHIN-4
				
				if(shipNodeSFDCMap.containsKey(strShipNode))
				{
					log.verbose("ShipNode is SFDCShipNode:" +strShipNode);
					String strIsSFDCFlag = invokeGetItemDetails(env, strItemID);

					if(strIsSFDCFlag.equalsIgnoreCase(AcademyConstants.STR_NO)){
						return true;
					}

				}
				else
				{
					return false;
				}
			
			//START: SHIN-4	
			
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// END: SHIN-4
		
			
		return false;
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

		String strShipNodeCode = ((Element) getCommonCodeListOutDoc.getElementsByTagName("CommonCode").item(0)).getAttribute("CodeShortDescription");

		return strShipNodeCode;

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

		Document getItemDetailsOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ITEM_DETAILS, getItemDetailsInDoc);
		env.clearApiTemplate(AcademyConstants.API_GET_ITEM_DETAILS);
		log.verbose("getItemDetails output - " + XMLUtil.getXMLString(getItemDetailsOutDoc));

		String strIsSFDCFlag = ((Element) getItemDetailsOutDoc.getElementsByTagName("Extn").item(0)).getAttribute("ExtnShpFromDC");

		return strIsSFDCFlag;

	}

	public void setProperties(Map arg0) {
		// TODO Auto-generated method stub

	}


}
