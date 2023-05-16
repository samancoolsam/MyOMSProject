package com.academy.ecommerce.sterling.bopis.api;

import java.util.HashMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantriks.yih.adapter.util.YantriksConstants;

/**
 * This class is responsible to fetch Item and planogram details to display them on Store UI.
 * @author Sanchit/Radhakrishna Mediboina
 *
 */
public class AcademyGetItemAndPlanogramInfo {
	
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyGetItemAndPlanogramInfo.class);
		
	boolean boolPlanogramCallRequired = false;
	
	HashMap<String, YFCElement> hashMapPlanogramDetails = new HashMap<String, YFCElement>(); 
	
	
	/**
	 * This method is responsible to fetch Item and planogram details required to be displayed on batch pick and backroompick page on Web SOM.
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public Document getItemAndPlanogramInfo(YFSEnvironment env,Document inDoc) throws Exception
	{
		log.beginTimer("AcademyGetItemAndPlanogramInfo::getItemAndPlanogramInfo  :: "+SCXmlUtil.getString(inDoc));
		log.verbose("Entering the method AcademyGetItemAndPlanogramInfo.getItemAndPlanogramInfo");
		
		Document outDoc;
		
		YFCDocument yfcDocInput = YFCDocument.getDocumentFor(inDoc);
		YFCElement eleRootInput = yfcDocInput.getDocumentElement();
		String strIsBatchPlanogram=eleRootInput.getAttribute(AcademyConstants.ATTR_IS_BATCHPLANOGRAM);
		
		if (!(YFCObject.isVoid(strIsBatchPlanogram)) && AcademyConstants.STR_YES.equalsIgnoreCase(strIsBatchPlanogram)){
			 outDoc = getItemAndPlanogramDetailOnBatchScreen(env,inDoc);
		}else {
			 outDoc = getItemAndPlanogramDetailOnBackroomScreen(env,inDoc);
		}
		
		log.endTimer("AcademyGetItemAndPlanogramInfo::getItemAndPlanogramInfo");
		
		return outDoc;
	}
	
	
	/**
	 * This method is responsible to fetch Item and planogram details required to be displayed on batch pick Store UI Screen.
	 * @param env
	 * @param inDoc
	 * @throws Exception
	 */

	private Document getItemAndPlanogramDetailOnBatchScreen(YFSEnvironment env, Document inDoc) throws Exception {
		
		log.beginTimer("AcademyGetItemAndPlanogramInfo::getItemAndPlanogramDetailOnBatchScreen" );
		log.verbose("AcademyGetItemAndPlanogramInfo.getItemAndPlanogramDetailOnBatchScreen - inDoc:: \n" +SCXmlUtil.getString(inDoc));
		
		Document outDocSIMUpdate = null;
		YFCElement eleExtnShipmentLines=null;
		String strPlanogramStatus=null;
		String strResErrorCode = null;
		//String strStoreId = null;		
		
		//OMNI-31380 Start
		String strYantriksEnabled = getCommonCode(env);
		//OMNI-31380 End
		
		//OMNI-45645: SIM Integration -- Start
		outDocSIMUpdate = getSIMUpdatesForBatchLines(env, inDoc);
		//OMNI-45645: SIM Integration -- End
		
		YFCDocument yfcDocInput = YFCDocument.getDocumentFor(inDoc);
		YFCElement eleRootInput = yfcDocInput.getDocumentElement();		
		YFCElement eleOutput = eleRootInput.getChildElement(AcademyConstants.ELE_OUTPUT);
		YFCElement elestoreBatch=eleOutput.getChildElement(AcademyConstants.ELE_STORE_BATCH);
		YFCElement eleBatchLines = elestoreBatch.getChildElement(AcademyConstants.ELE_STORE_BATCH_LINES);
		
		YFCNodeList<YFCElement> eleGetBatchLineNodeList= eleBatchLines.getElementsByTagName(AcademyConstants.ELE_STORE_BATCH_LINE);
		
		YFCDocument getItemSupply = YFCDocument.createDocument(AcademyConstants.ELE_INVENTORY_SUPPLY);
		
		for (YFCElement eleBatchLine : eleGetBatchLineNodeList){		
			//OMNI-31380 Start
			if (!YFCObject.isVoid(strYantriksEnabled) && AcademyConstants.STR_NO.equalsIgnoreCase(strYantriksEnabled)){
				getItemSupplyInfo(env, elestoreBatch, getItemSupply, eleBatchLine);
				eleBatchLine.setAttribute(YantriksConstants.YANTRIKS_ENABLED, AcademyConstants.STR_NO);
			} else {
				eleBatchLine.setAttribute(YantriksConstants.YANTRIKS_ENABLED, AcademyConstants.STR_YES);
			}
			//OMNI-31380 End
			
			//OMNI-45645: SIM Integration -- Start		
			String strBLItemID = eleBatchLine.getAttribute(AcademyConstants.ITEM_ID);
			log.verbose("strBLItemID :: " +strBLItemID);
			
			if(!YFCObject.isVoid(outDocSIMUpdate)) {
				
				log.verbose("SIM RestAPI invocation for SFS Bacthpick Screen");
				YFCDocument yfcDocSIMUpdate = YFCDocument.getDocumentFor(outDocSIMUpdate);
				YFCElement eleRootJsonObject = yfcDocSIMUpdate.getDocumentElement();
				log.verbose("eleRootJsonObject.hasChildNodes() :: " +eleRootJsonObject.hasChildNodes());
				
				strResErrorCode = eleRootJsonObject.getAttribute(AcademyConstants.STRI_CODE);
				log.verbose("SIM Response Error Code :: " +strResErrorCode);
				
				if(!YFCObject.isVoid(eleRootJsonObject) && eleRootJsonObject.getNodeName().equals(AcademyConstants.STR_JSON_OBJECT)
						&& eleRootJsonObject.hasChildNodes() &&  YFCObject.isVoid(strResErrorCode)) {
					
					eleBatchLine.setAttribute(AcademyConstants.STR_SIM_REST_API, AcademyConstants.STRI_SUCCESS);
					log.verbose("SIM invocation is enabled and RestAPI is Success for BatchPick Screen");
					YFCNodeList<YFCElement> eleItemNodeList = eleRootJsonObject.getElementsByTagName(AcademyConstants.ITEM);
					
					for (YFCElement eleItem : eleItemNodeList){
						YFCElement eleChieldJsonObject = eleItem.getChildElement(AcademyConstants.STR_JSON_OBJECT);
						String strResItemID = eleChieldJsonObject.getAttribute(AcademyConstants.STR_ITEM_ID);
						log.verbose("SIM Response ItemId :: " +strResItemID);
						
						if((!YFCObject.isVoid(strBLItemID) && !YFCObject.isVoid(strResItemID)) && strBLItemID.equals(strResItemID)) {
							
							eleBatchLine.setAttribute(AcademyConstants.STR_AVL_STOCK_ON_HAND, eleChieldJsonObject.getAttribute(AcademyConstants.STRI_AVL_STOCK_ON_HAND));
							log.verbose("SIM Response AvailableStockOnHand :: " +eleChieldJsonObject.getAttribute(AcademyConstants.STRI_AVL_STOCK_ON_HAND));
							
							eleBatchLine.setAttribute(AcademyConstants.STR_LAST_REC_DATE, eleChieldJsonObject.getAttribute(AcademyConstants.STRI_LAST_REC_DATE));
							log.verbose("SIM Response LastReceivedDate :: " +eleChieldJsonObject.getAttribute(AcademyConstants.STRI_LAST_REC_DATE));
							
							eleBatchLine.setAttribute(AcademyConstants.STR_ITEM_PRICE, eleChieldJsonObject.getAttribute(AcademyConstants.STR_RETAIL_PRICE));
							log.verbose("SIM Response ItemPrice/retailPrice :: " +eleChieldJsonObject.getAttribute(AcademyConstants.STR_RETAIL_PRICE));
						}
					}
				} else {
					//log.verbose("SIM invocation disabled at service level, IsSIMRestAPIEnabled shd be Y or RestAPI invocation is failed");
					//String strResErrormessage = eleRootJsonObject.getAttribute("message");
					log.verbose("Invalid SIM Response1 - outDocSIMUpdate :: \n" +SCXmlUtil.getString(outDocSIMUpdate));
					eleBatchLine.setAttribute(AcademyConstants.STR_SIM_REST_API, AcademyConstants.STRI_FAILURE);
				}				
			}else {
				//log.verbose("SIM RestAPI Disabled or RestAPI invocation is failed");
				log.verbose("Invalid SIM Response2 - outDocSIMUpdate :: \n" +SCXmlUtil.getString(outDocSIMUpdate));
				eleBatchLine.setAttribute(AcademyConstants.STR_SIM_REST_API, AcademyConstants.STRI_FAILURE);
			}		
			//OMNI-45645: SIM Integration -- End
			
			YFCElement eleItemDetails = eleBatchLine.getChildElement(AcademyConstants.ELE_ITEM_DETAILS);		
			YFCElement eleExtnItemDetails = eleItemDetails.getChildElement(AcademyConstants.ELE_ITEM_DETAILS_EXTN);
			
			YFCElement eleShipmentLines = eleBatchLine.getChildElement(AcademyConstants.ELE_SHIPMENT_LINES);
			YFCNodeList<YFCElement> eleShipmentLineNodeList= eleShipmentLines.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);			
			for(YFCElement eleShipmentLine : eleShipmentLineNodeList){
				YFCElement eleExtn = eleShipmentLine.getChildElement(AcademyConstants.ELE_EXTN);
				strPlanogramStatus = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_PLANOGRAM_STATUS);
			    if (YFCCommon.equalsIgnoreCase(AcademyConstants.VAL_LOCATION_AVAILABLE,strPlanogramStatus))
			    {
			    	 String strItemId = eleShipmentLine.getAttribute(AcademyConstants.ATTR_ITEM_ID);  
			    	 eleExtnShipmentLines = eleShipmentLine.getChildElement(AcademyConstants.ELE_EXTN);
				     break;
				 }				
			}
			
			if(YFCCommon.equalsIgnoreCase(AcademyConstants.VAL_LOCATION_AVAILABLE,strPlanogramStatus))
			{
				eleExtnItemDetails.setAttribute(AcademyConstants.ATTR_EXTNPOGID,eleExtnShipmentLines.getAttribute(AcademyConstants.ATTR_EXTNPOGID));
				eleExtnItemDetails.setAttribute(AcademyConstants.ATTR_EXTNDEPARTMENT_PLANO,eleExtnShipmentLines.getAttribute(AcademyConstants.ATTR_EXTNDEPARTMENT));
				eleExtnItemDetails.setAttribute(AcademyConstants.ATTR_EXTNSECTION,eleExtnShipmentLines.getAttribute(AcademyConstants.ATTR_EXTNSECTION));
				eleExtnItemDetails.setAttribute(AcademyConstants.ATTR_EXTNPOGNUMBER,eleExtnShipmentLines.getAttribute(AcademyConstants.ATTR_EXTNPOGNUMBER));
				eleExtnItemDetails.setAttribute(AcademyConstants.ATTR_EXTNLIVEDATE,eleExtnShipmentLines.getAttribute(AcademyConstants.ATTR_EXTNLIVEDATE));			
			}		
		}
		log.endTimer("AcademyGetItemAndPlanogramInfo::getItemAndPlanogramDetailOnBatchScreen");
		log.verbose("Check if Planogram Details are present: \n" +XMLUtil.getXMLString(yfcDocInput.getDocument()));
		
		return yfcDocInput.getDocument();	
	}
	
	
	/**
	 * This method is invoked to fetch Item and planogram details on Backroom Pick Store UI page.
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	private Document getItemAndPlanogramDetailOnBackroomScreen(YFSEnvironment env, Document inDoc) throws Exception {
		
		log.beginTimer("AcademyGetItemAndPlanogramInfo::getItemAndPlanogramDetailOnBackroomScreen");
		log.verbose("Entering the method AcademyGetItemAndPlanogramInfo.getItemAndPlanogramDetailOnBackroomScreen :: \n"+SCXmlUtil.getString(inDoc));
		Document outDocSIMUpdate = null;
		
		YFCDocument yfcPage = YFCDocument.getDocumentFor(inDoc);
		YFCElement elePage = yfcPage.getDocumentElement();
		YFCElement eleOutput = elePage.getChildElement(AcademyConstants.ELE_OUTPUT);
		YFCElement eleGetShipmentLineList = eleOutput.getChildElement(AcademyConstants.ELE_SHIPMENT_LINES);
		
		if(!YFCCommon.isVoid(eleGetShipmentLineList)) {
			YFCNodeList<YFCElement> eleGetShipmentLineNodeList= eleGetShipmentLineList.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
			YFCDocument getItemSupply = YFCDocument.createDocument(AcademyConstants.ELE_INVENTORY_SUPPLY);
			YFCDocument changeShipmentIp = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENT);
			YFCElement elechangeShipmentIp = changeShipmentIp.getDocumentElement();
			YFCElement eleShipmentLinesChangeShp =elechangeShipmentIp.createChild(AcademyConstants.ELE_SHIPMENT_LINES);
			YFCElement eleShipmentLineChangeShp=null;
			
			//OMNI-31379 Start
			String strYantriksEnabled=getCommonCode(env);
			//OMNI-31379 End
			
			//OMNI-45645: SIM Integration -- Start
			outDocSIMUpdate = getSIMUpdatesForBackroom(env, inDoc);
			//OMNI-45645: SIM Integration -- End
	     
			for (YFCElement eleShipmentLine : eleGetShipmentLineNodeList){
				boolPlanogramCallRequired = false;
				
				String strItemID=eleShipmentLine.getAttribute(AcademyConstants.ATTR_ITEM_ID);
				String strShipmentKey=eleShipmentLine.getAttribute(AcademyConstants.SHIPMENT_KEY);
				YFCElement eleOrderLine=eleShipmentLine.getChildElement(AcademyConstants.ELEM_ORDER_LINE); 
				YFCElement eleItemDetails=eleOrderLine.getChildElement(AcademyConstants.ELE_ITEM_DETAILS);
	
				YFCDocument getShipmentLinePlanogramDetailsIp = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENT);
				YFCElement eleShipmentPlanogramIp = getShipmentLinePlanogramDetailsIp.getDocumentElement();
				YFCElement eleShipment = eleShipmentLine.getChildElement(AcademyConstants.ELE_SHIPMENT);
				
				//OMNI-31379 Start
				if (!YFCObject.isVoid(strYantriksEnabled) && AcademyConstants.STR_NO.equalsIgnoreCase(strYantriksEnabled)){
					stampItemSuppyforBackroomPick(env, getItemSupply, eleShipmentLine, eleShipment, eleItemDetails);
					eleShipmentLine.setAttribute(YantriksConstants.YANTRIKS_ENABLED, AcademyConstants.STR_NO);
				} else {
					eleShipmentLine.setAttribute(YantriksConstants.YANTRIKS_ENABLED, AcademyConstants.STR_YES);
				}
				//OMNI-31379 End
				
				//OMNI-45645: SIM Integration -- Start							
				String strSLItemID = eleShipmentLine.getAttribute(AcademyConstants.ITEM_ID);
				log.verbose("strSLItemID :: " +strSLItemID);
				
				if(!YFCObject.isVoid(outDocSIMUpdate)) {
					
					log.verbose("SIM RestAPI invocation for SFS & BOPIS Backroompick screen");
					YFCDocument yfcDocSIMUpdate = YFCDocument.getDocumentFor(outDocSIMUpdate);
					YFCElement eleRootJsonObject = yfcDocSIMUpdate.getDocumentElement();
					log.verbose("eleRootJsonObject.hasChildNodes() :: " +eleRootJsonObject.hasChildNodes());
					
					String strResErrorCode = eleRootJsonObject.getAttribute(AcademyConstants.STRI_CODE);
					log.verbose("SIM Response Error Code :: " +strResErrorCode);
					
					if(!YFCObject.isVoid(eleRootJsonObject) && eleRootJsonObject.getNodeName().equals(AcademyConstants.STR_JSON_OBJECT) 
							&& eleRootJsonObject.hasChildNodes() && YFCObject.isVoid(strResErrorCode)) {
						eleShipmentLine.setAttribute(AcademyConstants.STR_SIM_REST_API, AcademyConstants.STRI_SUCCESS);
						log.verbose("SIM RestAPI Enabled and API invocation is success for backroompick screen");
						YFCNodeList<YFCElement> eleItemNodeList= eleRootJsonObject.getElementsByTagName(AcademyConstants.ITEM);
						
						for (YFCElement eleItem : eleItemNodeList){
							YFCElement eleChieldJsonObject = eleItem.getChildElement(AcademyConstants.STR_JSON_OBJECT);
							String strResItemId = eleChieldJsonObject.getAttribute(AcademyConstants.STR_ITEM_ID);
							log.verbose("SIM Response ItemId :: " +strResItemId);
							
							if((!YFCObject.isVoid(strSLItemID) && !YFCObject.isVoid(strResItemId)) && strSLItemID.equals(strResItemId)) {
								
								eleShipmentLine.setAttribute(AcademyConstants.STR_AVL_STOCK_ON_HAND, eleChieldJsonObject.getAttribute(AcademyConstants.STRI_AVL_STOCK_ON_HAND));
								log.verbose("SIM Response AvailableStockOnHand :: " + eleChieldJsonObject.getAttribute(AcademyConstants.STRI_AVL_STOCK_ON_HAND));
								
								eleShipmentLine.setAttribute(AcademyConstants.STR_LAST_REC_DATE, eleChieldJsonObject.getAttribute(AcademyConstants.STRI_LAST_REC_DATE));
								log.verbose("SIM Response LastReceivedDate :: " + eleChieldJsonObject.getAttribute(AcademyConstants.STRI_LAST_REC_DATE));
								
								eleShipmentLine.setAttribute(AcademyConstants.STR_ITEM_PRICE, eleChieldJsonObject.getAttribute(AcademyConstants.STR_RETAIL_PRICE));
								log.verbose("SIM Response ItemPrice/retailPrice :: " + eleChieldJsonObject.getAttribute(AcademyConstants.STR_RETAIL_PRICE));
							}
						}
					} else {
						//log.verbose("SIM invocation is disabled at service level, IsSIMRestAPIEnabled shd be Y or RestAPI invocation is failed");
						//String strResErrormessage = eleRootJsonObject.getAttribute("message");
						log.verbose("Invalid SIM Response1 - outDocSIMUpdate :: \n" +SCXmlUtil.getString(outDocSIMUpdate));
						eleShipmentLine.setAttribute(AcademyConstants.STR_SIM_REST_API, AcademyConstants.STRI_FAILURE);
					}					
				}else {
					//log.verbose("SIM RestAPI Disabled or RestAPI invocation is failed");
					log.verbose("Invalid SIM Response2 - outDocSIMUpdate :: \n" +SCXmlUtil.getString(outDocSIMUpdate));
					eleShipmentLine.setAttribute(AcademyConstants.STR_SIM_REST_API, AcademyConstants.STRI_FAILURE);
				}		
				//OMNI-45645: SIM Integration -- End
				
				YFCElement eleExtn = eleShipmentLine.getChildElement(AcademyConstants.ELE_EXTN);
				String strPlanogramStatus = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_PLANOGRAM_STATUS);
								
			    if (YFCCommon.equalsIgnoreCase(AcademyConstants.VAL_NOT_INITIATED, strPlanogramStatus))
			    {
			    	 boolPlanogramCallRequired = true;
			    	 eleShipmentLine.setAttribute(AcademyConstants.IS_CHNAGESHIPMENT_CALL_REQ, AcademyConstants.STR_YES);
			    	 eleShipmentPlanogramIp.setAttribute(AcademyConstants.SHIP_NODE,eleShipment.getAttribute(AcademyConstants.ATTR_SHIP_NODE));
			 	     eleShipmentPlanogramIp.setAttribute(AcademyConstants.SHIPMENT_KEY,eleShipmentLine.getAttribute(AcademyConstants.SHIPMENT_KEY));
			         YFCElement eleShipmentLnsPlanogramIp = eleShipmentPlanogramIp.createChild(AcademyConstants.ELE_SHIPMENT_LINES);
			    	 YFCElement eleShipmentLnPlanogramIp = eleShipmentLnsPlanogramIp.createChild(AcademyConstants.ELE_SHIPMENT_LINE);
					 eleShipmentLnPlanogramIp.setAttribute(AcademyConstants.ATTR_SHIP_LINE_NO, eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIP_LINE_NO));
				     eleShipmentLnPlanogramIp.setAttribute(AcademyConstants.ATTR_ITEM_ID,strItemID);
			    }
			    
			    if (boolPlanogramCallRequired){
					Document getShipmentLinePlanogramDetailsOp = AcademyUtil.invokeService(env, AcademyConstants.SER_ACAD_CALL_PLANOGRAM_DETAILS , getShipmentLinePlanogramDetailsIp.getDocument());
					
					YFCDocument yfcDocgetShipmentLinePlanogramDetailsOp = YFCDocument.getDocumentFor(getShipmentLinePlanogramDetailsOp);
					YFCElement eleRootGetShipmentPlanogramDtls = yfcDocgetShipmentLinePlanogramDetailsOp.getDocumentElement();
					YFCElement eleShipmentLinesPlanogramOp = eleRootGetShipmentPlanogramDtls.getChildElement(AcademyConstants.ELE_SHIPMENT_LINES);
					YFCNodeList<YFCElement> eleGetShipmentLinePlanogramOpNodeList= eleShipmentLinesPlanogramOp.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
				    
					for(YFCElement eleGetShipmentLinePlanogramOp : eleGetShipmentLinePlanogramOpNodeList){
				    	String strPlamogramOPItemId = eleGetShipmentLinePlanogramOp.getAttribute(AcademyConstants.ATTR_ITEM_ID);
				    	YFCElement eleExtnPlamogramDetails = eleGetShipmentLinePlanogramOp.getChildElement(AcademyConstants.ELE_EXTN);
				    	hashMapPlanogramDetails.put(strPlamogramOPItemId, eleExtnPlamogramDetails);
				    }
			    
					if (hashMapPlanogramDetails.containsKey(strItemID)){
						eleExtn = eleShipmentLine.getChildElement(AcademyConstants.ELE_EXTN);
						YFCElement eleExtnInMap=hashMapPlanogramDetails.get(strItemID);
						eleExtn.setAttributes(eleExtnInMap.getAttributes());	
					}
					
					if(!YFCCommon.isVoid(changeShipmentIp)&&(eleShipmentLine.getAttribute(AcademyConstants.IS_CHNAGESHIPMENT_CALL_REQ).equalsIgnoreCase(AcademyConstants.STR_YES)))
					{
						elechangeShipmentIp.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
						elechangeShipmentIp.setAttribute(AcademyConstants.IS_CHNAGESHIPMENT_CALL_REQ, AcademyConstants.STR_YES);
						eleShipmentLineChangeShp =eleShipmentLinesChangeShp.createChild(AcademyConstants.ELE_SHIPMENT_LINE);
						eleShipmentLineChangeShp.setAttribute(AcademyConstants.ATTR_SHIP_LINE_NO, eleShipmentLine.getAttribute(AcademyConstants.ATTR_SHIP_LINE_NO));
						YFCElement eleExtnChangeShipment =eleShipmentLineChangeShp.createChild(AcademyConstants.ELE_EXTN);
						eleExtnChangeShipment.setAttribute(AcademyConstants.ATTR_EXTNPOGID,eleExtn.getAttribute(AcademyConstants.ATTR_EXTNPOGID));
						eleExtnChangeShipment.setAttribute(AcademyConstants.ATTR_EXTNDEPARTMENT,eleExtn.getAttribute(AcademyConstants.ATTR_EXTNDEPARTMENT));
						eleExtnChangeShipment.setAttribute(AcademyConstants.ATTR_EXTNSECTION,eleExtn.getAttribute(AcademyConstants.ATTR_EXTNSECTION));
						eleExtnChangeShipment.setAttribute(AcademyConstants.ATTR_EXTNPOGNUMBER,eleExtn.getAttribute(AcademyConstants.ATTR_EXTNPOGNUMBER));
						eleExtnChangeShipment.setAttribute(AcademyConstants.ATTR_EXTNLIVEDATE,eleExtn.getAttribute(AcademyConstants.ATTR_EXTNLIVEDATE));
						eleExtnChangeShipment.setAttribute(AcademyConstants.ATTR_EXTN_PLANOGRAM_STATUS,eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_PLANOGRAM_STATUS));
					}
					
					log.verbose("Planogram Webservice Output: =" +XMLUtil.getXMLString(getShipmentLinePlanogramDetailsOp));
				}
			}
			log.endTimer("AcademyGetItemAndPlanogramInfo::getItemAndPlanogramDetailOnBackroomScreen");
			
			if(!YFCCommon.isVoid(eleShipmentLineChangeShp))
			{
				AcademyUtil.invokeService(env, AcademyConstants.SER_POSTMESSAGE_FOR_CHANGE_SHIPMENT, changeShipmentIp.getDocument());
			}
		}
		log.verbose("Check if Planogram Details are present - getItemAndPlanogramDetailOnBackroomScreen: \n" +XMLUtil.getXMLString(yfcPage.getDocument()));
		
		return yfcPage.getDocument();
	}

	
	/**
	 * This method is invoked to stamp supply Quantity of the item in ItemDetails Tag for Backroom Pick Screen.
	 * @param env
	 * @param getItemSupply
	 * @param eleShipmentLine
	 * @param eleShipmentLine
	 * @return eleItemDetails
	 * @throws Exception
	 */
	private void stampItemSuppyforBackroomPick(YFSEnvironment env, YFCDocument getItemSupply,
			YFCElement eleShipmentLine, YFCElement eleShipment, YFCElement eleItemDetails) throws Exception {
				
		log.beginTimer("AcademyGetItemAndPlanogramInfo::stampItemSuppyforBackroomPick");
		log.verbose("Entering the method AcademyGetItemAndPlanogramInfo.stampItemSuppyforBackroomPick");
		
		YFCElement eleRootGetInventorySupply = getItemSupply.getDocumentElement();
		eleRootGetInventorySupply.setAttribute(AcademyConstants.ATTR_ITEM_ID, eleShipmentLine.getAttribute(AcademyConstants.ATTR_ITEM_ID));
		eleRootGetInventorySupply.setAttribute(AcademyConstants.ATTR_UOM, eleShipmentLine.getAttribute(AcademyConstants.ATTR_UOM));
		eleRootGetInventorySupply.setAttribute(AcademyConstants.ATTR_PROD_CLASS, eleShipmentLine.getAttribute(AcademyConstants.ATTR_PROD_CLASS));
		eleRootGetInventorySupply.setAttribute(AcademyConstants.ATTR_SUPPLY_TYPE, AcademyConstants.STR_SUPP_TYPE_VAL);
		eleRootGetInventorySupply.setAttribute(AcademyConstants.ATTR_SHIP_NODE, eleShipment.getAttribute(AcademyConstants.ATTR_SHIP_NODE));
		
		//Invoke getInventorySupply api
		Document getInventorySupplyOpDoc = AcademyUtil.invokeService(env, AcademyConstants.SER_GET_INVENTORY_SUPPLY, getItemSupply.getDocument());
		YFCDocument yfcDocGetInventorySupplyOp = YFCDocument.getDocumentFor(getInventorySupplyOpDoc);
		YFCElement eleInventorySupplyOp = yfcDocGetInventorySupplyOp.getDocumentElement();
		YFCElement eleSupplies= eleInventorySupplyOp.getChildElement(AcademyConstants.ELE_SUPPLIES);
		YFCElement eleInventorySupply = eleSupplies.getChildElement(AcademyConstants.ELE_INVENTORY_SUPPLY);
		if (!YFCCommon.isVoid(eleInventorySupply)){
			eleItemDetails.setAttribute(AcademyConstants.STR_ONHAND_INVENTORY, eleInventorySupply.getAttribute(AcademyConstants.ATTR_QUANTITY));
		}
		log.verbose("Get Inventory Supply Output for Backroom Pick Screen: =" +XMLUtil.getXMLString(getInventorySupplyOpDoc));
		log.endTimer("AcademyGetItemAndPlanogramInfo::stampItemSuppyforBackroomPick");
	}


	
	
	/**
	 * This method is invoked to stamp supply Quantity of the item in ItemDetails Tag for Batch Pick Store UI.
	 * @param eleRootGetStoreBatchDetails
	 * @param getItemSupply
	 * @param eleBatchLine
	 * @return
	 * @throws Exception
	 */	
	private void getItemSupplyInfo(YFSEnvironment env, YFCElement elestoreBatch, YFCDocument getItemSupply, YFCElement eleBatchLine) throws Exception {
		
		log.beginTimer("AcademyGetItemAndPlanogramInfo::getItemSupplyInfo");
		log.verbose("Entering the method AcademyGetItemAndPlanogramInfo.getItemSupplyInfo");
		
		YFCElement eleItemDetails = eleBatchLine.getChildElement(AcademyConstants.ELE_ITEM_DETAILS);
		YFCElement elePrimaryInformation = eleItemDetails.getChildElement(AcademyConstants.PRIM_INFO);
		
		//prepare Input for GetInventorySupply
		YFCElement eleRootGetInventorySupply = getItemSupply.getDocumentElement();
		eleRootGetInventorySupply.setAttribute(AcademyConstants.ATTR_ITEM_ID, eleItemDetails.getAttribute(AcademyConstants.ATTR_ITEM_ID));
		eleRootGetInventorySupply.setAttribute(AcademyConstants.ATTR_UOM, eleItemDetails.getAttribute(AcademyConstants.ATTR_UOM));
		eleRootGetInventorySupply.setAttribute(AcademyConstants.ATTR_PROD_CLASS, elePrimaryInformation.getAttribute(AcademyConstants.ATTR_DEFAULT_PROD_CLASS));
		eleRootGetInventorySupply.setAttribute(AcademyConstants.ATTR_SUPPLY_TYPE, AcademyConstants.STR_SUPP_TYPE_VAL);
		eleRootGetInventorySupply.setAttribute(AcademyConstants.ATTR_SHIP_NODE,elestoreBatch.getAttribute(AcademyConstants.ORGANIZATION_CODE));
		eleRootGetInventorySupply.setAttribute(AcademyConstants.ORGANIZATION_CODE,eleItemDetails.getAttribute(AcademyConstants.ORGANIZATION_CODE));
		
		//Invoke getInventorySupply api
		Document getInventorySupplyOpDoc = AcademyUtil.invokeService(env, AcademyConstants.SER_GET_INVENTORY_SUPPLY, getItemSupply.getDocument());
		YFCDocument yfcDocGetInventorySupplyOp = YFCDocument.getDocumentFor(getInventorySupplyOpDoc);
		YFCElement eleInventorySupplyOp = yfcDocGetInventorySupplyOp.getDocumentElement();
		YFCElement eleSupplies= eleInventorySupplyOp.getChildElement(AcademyConstants.ELE_SUPPLIES);
		YFCElement eleInventorySupply = eleSupplies.getChildElement(AcademyConstants.ELE_INVENTORY_SUPPLY);
		
		if (!YFCCommon.isVoid(eleInventorySupply)){
			eleItemDetails.setAttribute(AcademyConstants.STR_ONHAND_INVENTORY, eleInventorySupply.getAttribute(AcademyConstants.ATTR_QUANTITY));
		}
		
		log.verbose("Get Inventory Supply Output for Batch Pick Screen: =" +XMLUtil.getXMLString(getInventorySupplyOpDoc));
		log.endTimer("AcademyGetItemAndPlanogramInfo::getItemSupplyInfo");
	}
	
	private String  getCommonCode(YFSEnvironment env) throws Exception
	{
		String strYantriksEnabled="N";
		
		Document docGetReasonCodesInput = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
		docGetReasonCodesInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE,"WEBSOM_YAN_ENABLED");
		docGetReasonCodesInput.getDocumentElement().setAttribute(AcademyConstants.ORGANIZATION_CODE,AcademyConstants.HUB_CODE);
		Document docCommonCodeListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMON_CODELIST,docGetReasonCodesInput);
	
		YFCDocument yfcDocCommonCodeListOutput = YFCDocument.getDocumentFor(docCommonCodeListOutput);
		YFCElement eleCommonCodeList = yfcDocCommonCodeListOutput.getDocumentElement();
		YFCElement eleCommonCode=eleCommonCodeList.getElementsByTagName(YantriksConstants.E_COMMON_CODE).item(0);
		strYantriksEnabled=eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
		
		return strYantriksEnabled;
	}
	
	
	
	//OMNI-45645: SIM Integration -- Start
		
	/**
	 * inDoc to Service:
	 * <Items StoreID="033"> 
	 * 		<Item ItemID="010035624"/> 
	 * 		<Item ItemID="010035625"/>
	 * </Items>
	 */
	/**
	 * WebService outDoc's:
	 * Error:
	 * <jsonObject code="500" message="There was an error processing your request. It has been logged (ID cba1c68dbb569515)."/>
	 * 
	 * Success:
	 * <jsonObject> 
	 * 		<Item> 
	 * 			<jsonObject availableStockOnHand="23" clearance="false" itemId="010035624" lastReceivedDate="07/22/2021" retailPrice="$6.99" storeId="33"/>
	 * 		</Item>
	 *  	<Item> 
	 *  		<jsonObject availableStockOnHand="22" clearance="false" itemId="010035625" lastReceivedDate="07/22/2021" retailPrice="$6.99" storeId="33"/>
	 * 		</Item> 
	 * </jsonObject>
	 */
	
	/**
	 * This method invoke AcademySIMIntegrationRestAPI for SIM updates for BatchPick lines.
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	private Document getSIMUpdatesForBatchLines(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("getSIMUpdatesForBatchLines - Start" + XMLUtil.getXMLString(inDoc));
		
		Document outDocService = null; 
		Document indocService= null;
		
		indocService = XMLUtil.createDocument(AcademyConstants.ELE_ITEMS);
		Element eleItems = indocService.getDocumentElement();
		
		YFCDocument yfcDocInput = YFCDocument.getDocumentFor(inDoc);
		YFCElement eleRootInput = yfcDocInput.getDocumentElement();		
		YFCElement eleOutput = eleRootInput.getChildElement(AcademyConstants.ELE_OUTPUT);
		YFCElement elestoreBatch = eleOutput.getChildElement(AcademyConstants.ELE_STORE_BATCH);
		
		String strStoreID = elestoreBatch.getAttribute(AcademyConstants.ORGANIZATION_CODE);
		log.verbose("getSIMUpdatesForBatchLines - strStoreID :: " + strStoreID);
		
		if (!YFCCommon.isVoid(strStoreID)){
			eleItems.setAttribute(AcademyConstants.STR_STORE_ID, strStoreID);
						
			YFCElement eleBatchLines = elestoreBatch.getChildElement(AcademyConstants.ELE_STORE_BATCH_LINES);		
			YFCNodeList<YFCElement> eleGetBatchLineNodeList= eleBatchLines.getElementsByTagName(AcademyConstants.ELE_STORE_BATCH_LINE);
					
			for (YFCElement eleBatchLine : eleGetBatchLineNodeList){			
				String strItemID = eleBatchLine.getAttribute(AcademyConstants.ITEM_ID);
				log.verbose("getSIMUpdatesForBatchLines - strItemID :: " + strItemID);
				if (!YFCCommon.isVoid(strItemID)){
					Element itemElement = XMLUtil.createElement(indocService, AcademyConstants.ITEM, true);
					itemElement.setAttribute(AcademyConstants.ITEM_ID, strItemID);
					eleItems.appendChild(itemElement);
				}
			}	
			log.verbose("Start: Invoking the AcademySIMIntegrationRestAPIWebService, inXML :\n" + XMLUtil.getXMLString(indocService));
			//outDocService = AcademyUtil.invokeService(env, "AcademySIMIntegrationRestAPIService", indocService);
			outDocService = AcademyUtil.invokeService(env, "AcademySIMIntegrationRestAPIWebService", indocService);
			log.verbose("End: Invoking the AcademySIMIntegrationRestAPIWebService, outDoc:: \n" + XMLUtil.getXMLString(outDocService));
		} else {
			log.verbose("getSIMUpdatesForBatchLines - strStoreID should not be null to invoke SIM Rest API");
		}
		log.verbose("getSIMUpdatesForBatchLines - End");
		
		return outDocService;
	}
	
	
	/**
	 * This method invoke AcademySIMIntegrationRestAPI for SIM updates for Backroom Pick lines.
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	/**
	 * inDoc to Service:
	 * <Items StoreID="033"> 
	 * 		<Item ItemID="010035624"/> 
	 * 		<Item ItemID="010035625"/>
	 * </Items>
	 */
	private Document getSIMUpdatesForBackroom(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("getSIMUpdatesForBackroom - Start" + XMLUtil.getXMLString(inDoc));
		
		Document outDocService = null; 
		Document indocService= null;
		String strStoreID = null;
		
		indocService = XMLUtil.createDocument(AcademyConstants.ELE_ITEMS);
		Element eleItems = indocService.getDocumentElement();
		
		YFCDocument yfcPage = YFCDocument.getDocumentFor(inDoc);
		YFCElement elePage = yfcPage.getDocumentElement();
		YFCElement eleOutput = elePage.getChildElement(AcademyConstants.ELE_OUTPUT);
		YFCElement eleGetShipmentLineList = eleOutput.getChildElement(AcademyConstants.ELE_SHIPMENT_LINES);
		
		YFCNodeList<YFCElement> eleGetShipmentLineNodeList = eleGetShipmentLineList.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
				
		for (YFCElement eleShipmentLine : eleGetShipmentLineNodeList){					
			String strItemID = eleShipmentLine.getAttribute(AcademyConstants.ITEM_ID);
			log.verbose("getSIMUpdatesForBackroom - strItemID :: " + strItemID);
			if (!YFCCommon.isVoid(strItemID)){
				Element itemElement = XMLUtil.createElement(indocService, AcademyConstants.ITEM, true);
				itemElement.setAttribute(AcademyConstants.ITEM_ID, strItemID);
				eleItems.appendChild(itemElement);
				
				YFCElement eleShipment = eleShipmentLine.getChildElement(AcademyConstants.ELE_SHIPMENT);
				strStoreID = eleShipment.getAttribute(AcademyConstants.STR_SHIPNODE);
			}
		}
		log.verbose("getSIMUpdatesForBackroom - strStoreID :: " + strStoreID);
		
		if (!YFCCommon.isVoid(strStoreID)){
			eleItems.setAttribute(AcademyConstants.STR_STORE_ID, strStoreID);
						
			log.verbose("Start: Invoking the AcademySIMIntegrationRestAPIWebService, inXML: \n" + XMLUtil.getXMLString(indocService));
			//outDocService = AcademyUtil.invokeService(env, "AcademySIMIntegrationRestAPIService", indocService);
			outDocService = AcademyUtil.invokeService(env, "AcademySIMIntegrationRestAPIWebService", indocService);
			log.verbose("End: Invoking the AcademySIMIntegrationRestAPIWebService, outDoc: \n" + XMLUtil.getXMLString(outDocService));
		} else {
			log.verbose("getSIMUpdatesForBatchLines - strStoreID should not be null to invoke SIM Rest API");
		}
		log.verbose("getSIMUpdatesForBackroom - End");
		
		return outDocService;
	}
	//OMNI-45645: SIM Integration -- End
	
}
