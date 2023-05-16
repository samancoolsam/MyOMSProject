package com.academy.ecommerce.sterling.bopis.inventory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
/**
 * 
 * @author Abhishek Aggarwal
 * @Date 03/07/2018
 */
public class AcademyCycleCountMsgToSIM 
{
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyCycleCountMsgToSIM.class);
	boolean boolIsDirtyRequired = false;
	/**
	 * This method is responsible to send cycle count message to SIM.
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public Document cycleCountMsg(YFSEnvironment env,Document inDoc) throws Exception
	{
		log.beginTimer("AcademyCycleCountMsgToSIM.java-cycleCountMsg() : Start");
		boolean boolSendCycleCount = false;
		//This Transaction Object is set in BeforeChangeShipmentUE to get only those shipmentLine keys for which the 
		//Shortage Qty is coming in input i.e. from Web store UI.
		ArrayList<String> arrShpLneListUE = (ArrayList<String>) env.getTxnObject("arrShipmentLineListOfUE");
		//BOPIS-1374 : BEGIN
		YFCDocument yfsInDoc = YFCDocument.getDocumentFor(inDoc);
		log.debug("AcademyCycleCountMsgToSIM.java : cycleCountMsg() :inDoc"+yfsInDoc.toString());
		YFCElement eleInDocShp = yfsInDoc.getDocumentElement();
		//BOPIS-1553 : Begin
		if(!YFCCommon.isVoid(arrShpLneListUE) || YFCCommon.equalsIgnoreCase("StoreBatch",eleInDocShp.getTagName()))
		//BOPIS-1553 : END	
		{

			//call getcommonCode List to fetch the cycle count common codes 
			YFCDocument inDocCommonCode = YFCDocument.createDocument(AcademyConstants.ELE_COMMON_CODE);
			YFCElement eleInDocCommnCde = inDocCommonCode.getDocumentElement();
			eleInDocCommnCde.setAttribute(AcademyConstants.ATTR_CALL_ORG_CODE,AcademyConstants.HUB_CODE);
			eleInDocCommnCde.setAttribute(AcademyConstants.ATTR_CODE_TYPE,AcademyConstants.ATTR_ACAD_CYCLE_COUNT);
			HashMap<String,YFCElement> hmpCycleCntShpLne = new HashMap<String, YFCElement>();
			ArrayList<String> arrLstCycleCntRsnCode = populateArrLstWithCycleCntRsnCode(env,inDocCommonCode);

			
			if(YFCCommon.equalsIgnoreCase("StoreBatch",eleInDocShp.getTagName()))
			{
				invokeCycleCountForBatchShortPick(env,yfsInDoc,arrLstCycleCntRsnCode);
				if(boolIsDirtyRequired)
				{
					eleInDocShp.setAttribute("MarkDirty", "Y");
				}
				log.endTimer("AcademyCycleCountMsgToSIM.java-cycleCountMsg() : End");
				return yfsInDoc.getDocument();
			}
			YFCNodeList<YFCElement> nlInDocShpLne = eleInDocShp.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
			for(YFCElement eleInDocShpLne : nlInDocShpLne)
			{
				YFCElement eleInDocExtn = eleInDocShpLne.getChildElement(AcademyConstants.ELE_EXTN);
				String strExtnReasonCode=eleInDocExtn.getAttribute(AcademyConstants.ATTR_EXTN_REASON_CODE);
				String strShipmentLineKey=eleInDocShpLne.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);

				//Check if cycle count message is required.
				if(arrLstCycleCntRsnCode.contains(strExtnReasonCode))
				{
					boolSendCycleCount=true;
					if(!hmpCycleCntShpLne.containsKey(strShipmentLineKey) && arrShpLneListUE.contains(strShipmentLineKey))
					{
						hmpCycleCntShpLne.put(strShipmentLineKey, eleInDocShpLne);
					}
				}
			}

			String strInDocShipNode = eleInDocShp.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
			//BOPIS: BOPIS-1374 : Begin
			//Call Cycle Count only if the transaction object contains any Shipment Lines.
			if(!hmpCycleCntShpLne.isEmpty())
			{
				//call inventory node control and set the boolean to false if the map becomes empty
				hmpCycleCntShpLne = callInventoryNodeControl(env,hmpCycleCntShpLne,strInDocShipNode);
			}
			//BOPIS: BOPIS-1374 : End
			if(boolSendCycleCount && !hmpCycleCntShpLne.isEmpty())
			{
				YFCDocument outDocCycleCount = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENT);
				YFCElement eleOutDocCCShp = outDocCycleCount.getDocumentElement();
				eleOutDocCCShp.setAttribute(AcademyConstants.ATTR_SHIP_NODE, strInDocShipNode);
				YFCElement eleOutDocCCShpLns =eleOutDocCCShp.createChild(AcademyConstants.ELE_SHIPMENT_LINES);
				for(Map.Entry<String, YFCElement> entry : hmpCycleCntShpLne.entrySet())
				{
					YFCElement eleOutDocCCShpLne = eleOutDocCCShpLns.createChild(AcademyConstants.ELE_SHIPMENT_LINE);
					YFCElement elehmpShpLne = entry.getValue();
					String strOutDocCCItemID = elehmpShpLne.getAttribute(AcademyConstants.ATTR_ITEM_ID);
					String strOutDocCCQuantity = elehmpShpLne.getAttribute(AcademyConstants.ATTR_SHORTAGE_QTY);
					eleOutDocCCShpLne.setAttribute(AcademyConstants.ATTR_ITEM_ID,strOutDocCCItemID);
					eleOutDocCCShpLne.setAttribute(AcademyConstants.ATTR_QUANTITY,strOutDocCCQuantity);
				}
				//call service to publish message to SIm for Cycle Count.
				log.debug("AcademyCycleCountMsgToSIM.java : Output message to SIM"+outDocCycleCount.toString());
				AcademyUtil.invokeService(env,"AcademyBOPISPublishCycleCountToSIM",outDocCycleCount.getDocument());

			}
			log.endTimer("AcademyCycleCountMsgToSIM.java-cycleCountMsg() : End");
		}
		//BOPIS-1374 : End
		return inDoc;
	}
	
	/**
	 * 
	 * @param env
	 * @param yfsInDoc
	 * @throws Exception 
	 */
	public void invokeCycleCountForBatchShortPick(YFSEnvironment env,YFCDocument yfsInDoc,ArrayList<String> arrLstCycleCntRsnCode) throws Exception
	{
		YFCElement eleInDocShp = yfsInDoc.getDocumentElement();
		String strInDocShipNode = eleInDocShp.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
		YFCElement eleItem = eleInDocShp.getChildElement(AcademyConstants.ITEM);
		String strItemID = eleItem.getAttribute(AcademyConstants.ATTR_ITEM_ID);
		String strShortageQty = eleItem.getAttribute(AcademyConstants.ATTR_SHORTAGE_QTY);
		String strReasonCode = eleItem.getAttribute(AcademyConstants.ATTR_SHORTAGE_REASON);
		Document outDocInvNodeCntrl = getInventoryNodeContrl(env,strInDocShipNode,strItemID);
		ArrayList<String> arrItemListInvNdeCntrl = createItemNodeCntrlList(outDocInvNodeCntrl);
		
		//Start SFS-33 SFS Batch short pick issue-Node inventory expiry date not updating (resulting in dirty node not being created)
		boolean bInvPicNotExpired = false;		
		String strInvPicIncorrectTill = XPathUtil.getString(outDocInvNodeCntrl.getDocumentElement(),
				"//InventoryNodeControlList/InventoryNodeControl[@ItemID='"+strItemID+"']/@InvPictureIncorrectTillDate");
		log.debug("strInvPicIncorrectTill : "+strInvPicIncorrectTill);
		if(!YFCCommon.isVoid(strInvPicIncorrectTill)){
			SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
			Date dtInvPicIncorrectTill = sdf.parse(strInvPicIncorrectTill);			
			Date dtSystemDate =  sdf.parse(sdf.format(Calendar.getInstance().getTime()));
			if(dtInvPicIncorrectTill.after(dtSystemDate))
				bInvPicNotExpired = true;
			
			log.debug("bInvPicNotExpired : "+bInvPicNotExpired);		
		}		
		//End SFS-33 SFS Batch short pick issue-Node inventory expiry date not updating (resulting in dirty node not being created)
		
		if(!(arrItemListInvNdeCntrl.contains(strItemID) && bInvPicNotExpired) && arrLstCycleCntRsnCode.contains(strReasonCode))
		{
			YFCDocument outDocCycleCount = YFCDocument.createDocument(AcademyConstants.ELE_SHIPMENT);
			YFCElement eleOutDocCCShp = outDocCycleCount.getDocumentElement();
			eleOutDocCCShp.setAttribute(AcademyConstants.ATTR_SHIP_NODE, strInDocShipNode);
			YFCElement eleOutDocCCShpLns =eleOutDocCCShp.createChild(AcademyConstants.ELE_SHIPMENT_LINES);

			YFCElement eleOutDocCCShpLne = eleOutDocCCShpLns.createChild(AcademyConstants.ELE_SHIPMENT_LINE);
			eleOutDocCCShpLne.setAttribute(AcademyConstants.ATTR_ITEM_ID,strItemID);
			eleOutDocCCShpLne.setAttribute(AcademyConstants.ATTR_QUANTITY,strShortageQty);

			//call service to publish message to SIm for Cycle Count.
			log.debug("Invoking AcademyBOPISPublishCycleCountToSIM - to publish message to SIM for Cycle Count.");	
			AcademyUtil.invokeService(env,"AcademyBOPISPublishCycleCountToSIM",outDocCycleCount.getDocument());
			boolIsDirtyRequired = true;
		}
		
		
	}
	
	/**
	 * This method remove the lines for which the item node combination is marked dirty.
	 * @param env
	 * @param hmpCycleCntShpLne
	 * @param strInDocShipNode
	 * @return
	 * @throws Exception
	 */
	public HashMap<String, YFCElement> callInventoryNodeControl(YFSEnvironment env,HashMap<String, YFCElement> hmpCycleCntShpLne,String strInDocShipNode) throws Exception
	{
		ArrayList<String> arrShipmentLinekey = new ArrayList<String>();
	
		for(Map.Entry<String, YFCElement> entry : hmpCycleCntShpLne.entrySet())
		{
			YFCElement elehmpShpLne = entry.getValue();
			String strElehmpShpLneKey = entry.getKey();
			String strOutDocCCItemID = elehmpShpLne.getAttribute(AcademyConstants.ATTR_ITEM_ID);
			//BOPIS: BOPIS-1374 : Begin
			//Changing the Inventory Node Control call for specific Item Node Combination.
			Document outDocInvNodeCntrl = getInventoryNodeContrl(env,strInDocShipNode,strOutDocCCItemID);
			//Checking if the Item Node Combination is marked dirty.
			ArrayList<String> arrItemListInvNdeCntrl = createItemNodeCntrlList(outDocInvNodeCntrl);
			//BOPIS: BOPIS-1374 : End
			
			//Start SFS-36 SFS :: Cycle count message is not sent during individual shipment short when an expired dirty node entry is present for an item
			boolean bInvPicNotExpired = false;		
			String strInvPicIncorrectTill = XPathUtil.getString(outDocInvNodeCntrl.getDocumentElement(),
					"//InventoryNodeControlList/InventoryNodeControl[@ItemID='"+strOutDocCCItemID+"']/@InvPictureIncorrectTillDate");
			log.debug("strInvPicIncorrectTill : "+strInvPicIncorrectTill);
			if(!YFCCommon.isVoid(strInvPicIncorrectTill)){
				SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
				Date dtInvPicIncorrectTill = sdf.parse(strInvPicIncorrectTill);			
				Date dtSystemDate =  sdf.parse(sdf.format(Calendar.getInstance().getTime()));
				if(dtInvPicIncorrectTill.after(dtSystemDate))
					bInvPicNotExpired = true;
				
				log.debug("bInvPicNotExpired : "+bInvPicNotExpired);		
			}		
			//End SFS-36 SFS :: Cycle count message is not sent during individual shipment short when an expired dirty node entry is present for an item			
			
			if(arrItemListInvNdeCntrl.contains(strOutDocCCItemID) && bInvPicNotExpired)
			{
				if(!arrShipmentLinekey.contains(strElehmpShpLneKey))
				{
					arrShipmentLinekey.add(strElehmpShpLneKey);
				}
			}
			
		}
		for(String strShpLneKey: arrShipmentLinekey)
		{
			if(hmpCycleCntShpLne.containsKey(strShpLneKey))
			{
				hmpCycleCntShpLne.remove(strShpLneKey);
			}
		}
		
		return hmpCycleCntShpLne;
	}
	/**
	 * This method assigns all the common code for shortage cycle count message to arrayList.
	 * @param env
	 * @param inDocCommonCode
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> populateArrLstWithCycleCntRsnCode(YFSEnvironment env,YFCDocument inDocCommonCode) throws Exception
	{
		ArrayList<String> arrLstCycleCntRsnCode = new ArrayList<String>();
		
		Document outDocCommonCodeList = AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_COMMONCODE_LIST, inDocCommonCode.getDocument());
		YFCDocument outDocCommnCdeLst = YFCDocument.getDocumentFor(outDocCommonCodeList);
		YFCElement eleOutDocCommnCodeLst = outDocCommnCdeLst.getDocumentElement();
		YFCNodeList<YFCElement> nlCommonCode = eleOutDocCommnCodeLst.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);
		for(YFCElement eleCommonCode : nlCommonCode)
		{
			String strCodeValue = eleCommonCode.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE);
			if(!arrLstCycleCntRsnCode.contains(strCodeValue))
			{
				arrLstCycleCntRsnCode.add(strCodeValue);
			}
		}
		
		return arrLstCycleCntRsnCode;
	}
	
	/**
	 * 
	 * @param env
	 * @param strShipNode
	 * @return
	 * @throws Exception
	 */
	public Document getInventoryNodeContrl(YFSEnvironment env,String strShipNode,String strItemID) throws Exception
	{
		YFCDocument inDocGetInvNodeCntrl = YFCDocument.createDocument("InventoryNodeControl");
		YFCElement eleInDocGetInvNodeCntrl = inDocGetInvNodeCntrl.getDocumentElement();
		eleInDocGetInvNodeCntrl.setAttribute(AcademyConstants.ATTR_NODE, strShipNode);
		eleInDocGetInvNodeCntrl.setAttribute(AcademyConstants.ATTR_ITEM_ID, strItemID);
		eleInDocGetInvNodeCntrl.setAttribute(AcademyConstants.ORGANIZATION_CODE,AcademyConstants.DSV_ENTERPRISE_CODE);
		Document outDocInvNodeCntrl = AcademyUtil.invokeAPI(env,"getInventoryNodeControlList", inDocGetInvNodeCntrl.getDocument());
		
		return outDocInvNodeCntrl;
	}
	
	/**
	 * 
	 * @param outDocInvNodeCntrl
	 * @return
	 */
	public ArrayList<String> createItemNodeCntrlList(Document outDocInvNodeCntrl)
	{
		YFCDocument yfcOutDocInvNdeCntrl = YFCDocument.getDocumentFor(outDocInvNodeCntrl);
		YFCElement eleInvNdeCntrlList = yfcOutDocInvNdeCntrl.getDocumentElement();
		YFCNodeList<YFCElement> nlInvNodeCntrl = eleInvNdeCntrlList.getElementsByTagName("InventoryNodeControl");
		
		ArrayList<String> arrItemListInvNdeCntrl = new ArrayList<String>();
		for(YFCElement eleInvNodeCntrl: nlInvNodeCntrl)
		{
			String strItemID =  eleInvNodeCntrl.getAttribute(AcademyConstants.ATTR_ITEM_ID);
			if(!arrItemListInvNdeCntrl.contains(strItemID))
			{
				arrItemListInvNdeCntrl.add(strItemID);
			}
		}
		return arrItemListInvNdeCntrl;
	}
}
