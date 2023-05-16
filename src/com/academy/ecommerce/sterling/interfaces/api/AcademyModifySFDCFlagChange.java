/**
 * Class Name - AcademyModifySFDCFlagChange
 * Author - Mounica V
 * Creation date - 07-10-2013
 * Last Modified date - 11-10-2013
 * Purpose : This class gets executed on invoking the service AcademyModifySFDCFlag.The main 
 * function of this class is to put a hold and release hold for items with Node-001 
 * combination and inserts record into YFS_INVENTORY_ACTIVITY table.When flag changes from Y 
 * to N it places hold and for N to Y it releases hold.     
 * 
 */
/*Input XML to handle AcademyModifySFDCFlag Service
 <ItemList>
	<Item ItemID="" UnitOfMeasure="" ProductClass=""
		OrganizationCode="">
		<Extn ExtnShpFromDC="" />
	</Item>
 </ItemList>
 */
package com.academy.ecommerce.sterling.interfaces.api;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyModifySFDCFlagChange implements YIFCustomApi {

	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyModifySFDCFlagChange.class);

	public void setProperties(Properties arg0) throws Exception {
	}

	/**
	 * This method would get the input document and will call the necessary APIs
	 * to manipulate the final output XML based on existing flag value for items
	 * which would be processed by other APIs in the service
	 * 
	 * @param env
	 *            - Environment variable
	 * @param docInput
	 *            - Input Document passed to the API
	 * @return - Final manipulated output document
	 * @throws Exception
	 *             - Generic exception
	 */

	public Document manageItemInventoryUpdate(YFSEnvironment env,Document docInput)throws Exception {
	
		log.beginTimer(" Begining of AcademyModifySFDCFlagChange->**** manageItemInventoryUpdate start*****");
	
	    Document docAPITemplate = YFCDocument.getDocumentFor(
			"<ItemList><Item ItemID='' >"
					+ "<Extn ExtnShpFromDC='' ExtnShipFromStoreItem=''/></Item></ItemList>").getDocument();	
	
	    Element eleItem = null;
	    Element eleItemList = null;
	    NodeList nItemlist = null;
	    Document docIpGetItemList = null;
	    Document docOpGetItemList = null;
	   	Element eleIpGetItemList = null; 
	   	Element eleIpGetItemListExtn = null;
	   	String strItemID = null;
	   	String strProductClass = null;
	   	String strDCInputFlag = null;
	 	   		   	
	    try{
	    log.verbose("********* AcademyModifySFDCFlagChange--> manageItemInventoryUpdate **** "
					+ XMLUtil.getXMLString(docInput));
	    
		eleItemList = docInput.getDocumentElement();
		nItemlist = (NodeList) eleItemList.getElementsByTagName(AcademyConstants.ITEM);
		    //Retrieved NodeList of Item from Input 
		int iNoOfItems = nItemlist.getLength();
		
		for(int i = 0; i < iNoOfItems ; i++)
	    {	        
			eleItem = (Element) nItemlist.item(i);
			 
			 strItemID = eleItem.getAttribute(AcademyConstants.ATTR_ITEM_ID);
			 strProductClass = eleItem.getAttribute(AcademyConstants.ATTR_PROD_CLASS);
			 //Check for value of ItemID   
			 if(!YFCObject.isVoid(strItemID)) {
				 
				 docIpGetItemList = XMLUtil.getDocumentForElement(eleItem); 
				 eleIpGetItemList = docIpGetItemList.getDocumentElement();
				 eleIpGetItemListExtn = (Element) eleIpGetItemList.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
				 strDCInputFlag = eleIpGetItemListExtn.getAttribute(AcademyConstants.ATTR_EXTN_SHP_FROM_DC);
				 eleIpGetItemList.removeChild(eleIpGetItemListExtn);
				 
				 log.verbose("********* AcademyModifySFDCFlagChange--> callgetItemListApi Input **** "
							+ XMLUtil.getXMLString(docIpGetItemList));
				 env.setApiTemplate(AcademyConstants.API_GET_ITEM_LIST,
						 docAPITemplate); 
				 docOpGetItemList = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_ITEM_LIST, docIpGetItemList);
				 env.clearApiTemplate(AcademyConstants.API_GET_ITEM_LIST);
				 log.verbose("********* AcademyModifySFDCFlagChange--> callgetItemListApi Output ****"
						+ XMLUtil.getXMLString(docOpGetItemList));
			 
			 try{
					// Check for Change in SFDC Flag
				 	if(isDCExtnAttributeChanged(env,eleItem,docOpGetItemList)){
						 if (strDCInputFlag.equals(AcademyConstants.STR_YES)){
							 	changeOfDCFlagFromNtoY(env,strItemID, strProductClass);	
							}else if (strDCInputFlag.equals(AcademyConstants.STR_NO)) {
								changeOfDCFlagFromYtoN(env,strItemID, strProductClass);
							}
					 }
					 
				 	//Check for Change in SFS Flag
					if (isSFSExtnAttributeChanged(env,eleItem,docOpGetItemList)){
										    	 
							changeOfSFSFlag(env,strItemID, strProductClass);
					    }
					 }catch(Exception e){
					throw new YFSException(e.getMessage());
			} // end of catch block
		} // end of if loop strItemID null check  		
	} // end of for loop
	   
	callManageItemApi(env, docInput);
	
	log.endTimer(" End of AcademyModifySFDCFlagChange-> manageItemInventoryUpdate end");
	
	}catch (Exception e) {
		e.printStackTrace();
	}
	return docInput;
}

	/**
	 * Method will compare flag values of ExtnDC attribute of API input and
	 * getItemList input.
	 * 
	 * @param eleInputItem
	 *            - Input Item element passed to the API
	 * @param docGetItemList
	 *            - Document passed to getItemList API
	 * @return boolean Flag saying if Extn DC attribute is changed
	 */
	private boolean isDCExtnAttributeChanged(YFSEnvironment env,
			Element eleInputItem, Document docGetItemList) {

		Element eleRetrievedItemExtn = null;
		Element eleInputItemExtn = null;
		String  strDCInputFlag = null;
		String  strItemListFlag = null;

		boolean bReturnFlag = false;

		log.verbose("*************** Inside AcademyModifySFDCFlagChange::isDCExtnAttributeChanged - start ************ ");

		// Getting required attributes details from input feed XML.
		eleInputItemExtn = (Element) eleInputItem.getElementsByTagName(
				AcademyConstants.ELE_EXTN).item(0);
		strDCInputFlag = eleInputItemExtn.getAttribute(AcademyConstants.ATTR_EXTN_SHP_FROM_DC);

		// Getting required attributes details from getItemList.
		eleRetrievedItemExtn = (Element) docGetItemList.getElementsByTagName(
				AcademyConstants.ELE_EXTN).item(0);
		strItemListFlag = eleRetrievedItemExtn.getAttribute(AcademyConstants.ATTR_EXTN_SHP_FROM_DC);

		if (!strDCInputFlag.equals(strItemListFlag)) {
			bReturnFlag = true;
		}
		log.verbose("*************** Inside AcademyModifySFDCFlagChange::isDCExtnAttributeChanged - end ************ ");
		return bReturnFlag;
	}
	
	
	private boolean isSFSExtnAttributeChanged(YFSEnvironment env,
			Element eleInputItem, Document docGetItemList) {

		Element eleRetrievedItemExtn = null;
		Element eleInputItemExtn = null;
		String  strSFSInputFlag = null;
		String  strItemListFlag = null;

		boolean bReturnFlag = false;

		log.verbose("*************** Inside AcademyModifySFDCFlagChange::isSFSExtnAttributeChanged - start ************ ");

		// Getting required attributes details from input feed XML.
		eleInputItemExtn = (Element) eleInputItem.getElementsByTagName(
				AcademyConstants.ELE_EXTN).item(0);
		strSFSInputFlag = eleInputItemExtn.getAttribute(AcademyConstants.ATTR_EXTN_SHIP_FROM_STORE_ITEM);

		// Getting required attributes details from getItemList.
		eleRetrievedItemExtn = (Element) docGetItemList.getElementsByTagName(
				AcademyConstants.ELE_EXTN).item(0);
		strItemListFlag = eleRetrievedItemExtn.getAttribute(AcademyConstants.ATTR_EXTN_SHIP_FROM_STORE_ITEM);

		if (!strSFSInputFlag.equals(strItemListFlag)) {
			bReturnFlag = true;
		}
		log.verbose("*************** Inside AcademyModifySFDCFlagChange::isSFSExtnAttributeChanged - end ************ ");
		return bReturnFlag;
	}
	

	/**
	 * Method will invoke CreateInventoryActivity based on ExtnDC value
	 * 
	 * @param strItemID
	 *            - ItemID from Input passed to API
	 * @param strProductClass
	 *            - ProductClass from Input passed to API
	 * @param env
	 *            - YFSEnvironment
	 * 
	 **/
	private void changeOfDCFlagFromYtoN(YFSEnvironment env, String strItemID,
			String strProductClass) {

		Document docIpManageInventoryNodeControl = null;
		Element eleIpManageInventoryNodeControl = null;

		log.verbose("********* AcademyModifySFDCFlagChange --> callchangeOfFlagFromYtoN Start **** ");

		docIpManageInventoryNodeControl = YFCDocument
				.getDocumentFor(
						"<InventoryNodeControl InvPictureIncorrectTillDate='' "
								+ "ItemID='' Node='' OrganizationCode='' ProductClass='' NodeControlType='' "
								+ "UnitOfMeasure='' />").getDocument();
		try {

			eleIpManageInventoryNodeControl = docIpManageInventoryNodeControl
					.getDocumentElement();
			eleIpManageInventoryNodeControl.setAttribute(
					AcademyConstants.ATTR_ITEM_ID, strItemID);
			eleIpManageInventoryNodeControl.setAttribute(
					AcademyConstants.ATTR_NODE, "001");
			eleIpManageInventoryNodeControl.setAttribute(
					AcademyConstants.ORGANIZATION_CODE,
					AcademyConstants.PRIMARY_ENTERPRISE);
			eleIpManageInventoryNodeControl.setAttribute(
					AcademyConstants.ATTR_PROD_CLASS, strProductClass);
			eleIpManageInventoryNodeControl
					.setAttribute(AcademyConstants.ATTR_UOM,
							AcademyConstants.UNIT_OF_MEASURE);
			eleIpManageInventoryNodeControl.setAttribute(
					AcademyConstants.ATTR_NODE_CONTROL_TYPE, "ON_HOLD");
			eleIpManageInventoryNodeControl.setAttribute(
					AcademyConstants.ATTR_INV_PIC_INCORRECT_TILL_DATE,
					"2500-01-01");

			log.verbose("*********AcademyModifySFDCFlagChange --> callmanageInventoryNodeControl Input**** "
					+ XMLUtil.getXMLString(docIpManageInventoryNodeControl));
			AcademyUtil.invokeAPI(env,
					AcademyConstants.API_MANAGE_INVENTORY_NODE_CONTROL,
					docIpManageInventoryNodeControl);

			log.verbose("*********AcademyModifySFDCFlagChange --> callmanageInventoryNodeControl Call Success**** ");

			callCreateInventoryActivity(env, strItemID, strProductClass);
			
			log.verbose("********* AcademyModifySFDCFlagChange --> callchangeOfFlagFromYtoN End **** ");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method will invoke CreateInventoryActivity based on flag value
	 * 
	 * @param strItemID
	 *            - ItemID from Input passed to API
	 * @param strProductClass
	 *            - ProductClass from Input passed to API
	 * @param env
	 *            - YFSEnvironment
	 * 
	 **/

	private void changeOfDCFlagFromNtoY(YFSEnvironment env, String strItemID,
			String strProductClass) {

		Document docIpManageInventoryNodeControl = null;
		Element eleIpManageInventoryNodeControl = null;

		log.verbose("********* AcademyModifySFDCFlagChange --> callchangeOfFlagFromNtoY Start **** ");
		docIpManageInventoryNodeControl = YFCDocument
				.getDocumentFor(
						"<InventoryNodeControl InventoryPictureCorrect='' "
								+ "ItemID='' Node='' OrganizationCode='' ProductClass='' NodeControlType='' "
								+ "UnitOfMeasure='' />").getDocument();
		try {

			eleIpManageInventoryNodeControl = docIpManageInventoryNodeControl
					.getDocumentElement();
			eleIpManageInventoryNodeControl.setAttribute(
					AcademyConstants.ATTR_ITEM_ID, strItemID);
			eleIpManageInventoryNodeControl.setAttribute(
					AcademyConstants.ATTR_NODE, "001");
			eleIpManageInventoryNodeControl.setAttribute(
					AcademyConstants.ORGANIZATION_CODE,
					AcademyConstants.PRIMARY_ENTERPRISE);
			eleIpManageInventoryNodeControl.setAttribute(
					AcademyConstants.ATTR_PROD_CLASS, strProductClass);
			eleIpManageInventoryNodeControl
					.setAttribute(AcademyConstants.ATTR_UOM,
							AcademyConstants.UNIT_OF_MEASURE);
			eleIpManageInventoryNodeControl.setAttribute(
					AcademyConstants.ATTR_INV_PIC_CORRECT,
					AcademyConstants.STR_YES);

			log.verbose("*********AcademyModifySFDCFlagChange --> callmanageInventoryNodeControl Input**** "
					+ XMLUtil.getXMLString(docIpManageInventoryNodeControl));
			AcademyUtil.invokeAPI(env,
					AcademyConstants.API_MANAGE_INVENTORY_NODE_CONTROL,
					docIpManageInventoryNodeControl);

			log.verbose("*********AcademyModifySFDCFlagChange --> callmanageInventoryNodeControl Call Success**** ");

			callCreateInventoryActivity(env, strItemID, strProductClass);
			
			log.verbose("********* AcademyModifySFDCFlagChange --> callchangeOfFlagFromNtoY End **** ");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method will invoke CreateInventoryActivity If SFS flag value changes
	 * 
	 * @param strItemID
	 *            - ItemID from Input passed to API
	 * @param strProductClass
	 *            - ProductClass from Input passed to API
	 * @param env
	 *            - YFSEnvironment
	 * 
	 **/
	
	private void changeOfSFSFlag(YFSEnvironment env, String strItemID,
			String strProductClass){
		
		   log.verbose("********* AcademyModifySFDCFlagChange --> callchangeOfSFSFlag Start **** ");

		try {
 			callCreateInventoryActivity(env, strItemID, strProductClass);
 			
     		log.verbose("********* AcademyModifySFDCFlagChange --> callchangeOfSFSFlag End **** ");

		} catch (Exception e) {
			e.printStackTrace();
		}	
		
	}
	
	/**
	 * Method to call CreateInventoryActivity
	 * 
	 * @param strItemID
	 *            - ItemID from Input passed to API
	 * @param strProductClass
	 *            - ProductClass from Input passed to API
	 * @param env
	 *            - YFSEnvironment
	 * 
	 **/
	private void callCreateInventoryActivity(YFSEnvironment env,
			String strItemID, String strProductClass) {

		Document docIpForInvActivity = null;
		Document docOpForInvActivity = null;

		log.verbose("********* AcademyModifySFDCFlagChange--> callCreateInventoryActivity start **** ");
		docIpForInvActivity = YFCDocument
				.getDocumentFor(
						"<InventoryActivity ItemID='' "
								+ " Node='' OrganizationCode='' ProcessedFlag='' ProductClass='' "
								+ "UnitOfMeasure='' />").getDocument();
		try {
			Element eleInvenActivity = docIpForInvActivity.getDocumentElement();
			eleInvenActivity.setAttribute(AcademyConstants.ATTR_ITEM_ID,
					strItemID);
			eleInvenActivity.setAttribute(AcademyConstants.ORGANIZATION_CODE,
					AcademyConstants.PRIMARY_ENTERPRISE);
			eleInvenActivity.setAttribute(AcademyConstants.ATTR_UOM,
					AcademyConstants.UNIT_OF_MEASURE);
			eleInvenActivity.setAttribute(AcademyConstants.ATTR_PROD_CLASS,
					strProductClass);
			eleInvenActivity.setAttribute(AcademyConstants.ATTR_PROCESSED_FLAG,
					"F");
			eleInvenActivity.setAttribute(AcademyConstants.ATTR_NODE, "001");

			log.verbose("********* AcademyModifySFDCFlagChange--> callCreateInventoryActivity Input **** "
					+ XMLUtil.getXMLString(docIpForInvActivity));
			docOpForInvActivity = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_CREATE_INVENTORY_ACTIVITY,
					docIpForInvActivity);
			log.verbose("********* AcademyModifySFDCFlagChange--> callCreateInventoryActivity Output**** "
					+ XMLUtil.getXMLString(docOpForInvActivity));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to call ManageItem API
	 * 
	 * @param docInput
	 *            - Input Document passed to the API
	 * @param env
	 *            - YFSEnvironment
	 * 
	 **/
	private void callManageItemApi(YFSEnvironment env, Document docInput)
			throws Exception {

		Document docIpManageItem = docInput;
		Document docOpManageItem = null;

		Document docmanageAPITemplate = YFCDocument.getDocumentFor(
				"<ItemList><Item ItemID='' UnitOfMeasure='' >"
						+ "OrganizationCode = '' >"
						+ "<Extn ExtnShpFromDC=''/></Item></ItemList>")
				.getDocument();

		try {

			Element eleManageItem = docmanageAPITemplate.getDocumentElement();
			eleManageItem.setAttribute(AcademyConstants.ORGANIZATION_CODE,
					AcademyConstants.CATALOG_ORG_CODE);

			log.verbose("********* AcademyModifySFDCFlagChange--> callmanageItemApi Input **** "
					+ XMLUtil.getXMLString(docIpManageItem));
			env.setApiTemplate(AcademyConstants.API_MANAGE_ITEM,
					docmanageAPITemplate);
			docOpManageItem = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_MANAGE_ITEM, docIpManageItem);
			env.clearApiTemplate(AcademyConstants.API_MANAGE_ITEM);
			log.verbose("********* AcademyModifySFDCFlagChange--> callmanageItemApi Output **** "
					+ XMLUtil.getXMLString(docOpManageItem));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
