package com.academy.ecommerce.sterling.api;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
/**
 * 
 * @author muchil
 */
public class AcademyBreakLPNForAdhocMove implements YIFCustomApi { 
	public void setProperties(Properties props) {} 
	
	
	  /**
   * Instance of logger
   */
  private static YFCLogCategory log = YFCLogCategory.instance(AcademyBreakLPNForAdhocMove.class);
  
  /**
   * This method will check if task_type is ADHOC and call method moveLocationInventoryAPI to break LPN at
   * deposit location 
   */
  
  	public void breakLPNForAdhocMove(YFSEnvironment env, Document inDoc) throws Exception {
	  log.beginTimer(" Begining of AcademyBreakLPNForAdhocMove-> breakLPNForAdhocMove Api");
	  log.verbose("Input document to AcademyBreakLPNForAdhocMove: "+XMLUtil.getXMLString(inDoc));
	  if(!YFCObject.isVoid(inDoc)){
		  Element taskEle = inDoc.getDocumentElement();
		  String strTaskType = taskEle.getAttribute("TaskType");
		  String strNode = taskEle.getAttribute("Node");
		  String strEnterpriseKey = taskEle.getAttribute("EnterpriseKey");
		  String strTargetLocationId=taskEle.getAttribute("TargetLocationId");
		  
		  if(strTaskType.equals("ADHOC")){
			  log.verbose("Task Type is ADHOC");
			  Element inventoryEle = (Element) taskEle.getElementsByTagName("Inventory").item(0);
			  if(!YFCObject.isVoid(inventoryEle)){
				  String strPalletId = inventoryEle.getAttribute("SourcePalletId");
				  String strItemId = inventoryEle.getAttribute("ItemId");
				  
				  if(!YFCObject.isVoid(strPalletId) && YFCObject.isVoid(strItemId)){
					  log.verbose("Before calling moveLocationInventoryAPI");
					  moveLocationInventoryAPI(env, strNode,
							  strEnterpriseKey, strTargetLocationId, strPalletId);
					  log.verbose("After calling moveLocationInventoryAPI");
				  }				 
				  }
			  }
		  }
	  log.endTimer(" End of AcademyBreakLPNForAdhocMove-> breakLPNForAdhocMove Api");
	  }
	  

	  public void moveLocationInventoryAPI(YFSEnvironment env,String strNode,
			  String strEnterpriseKey,String strTargetLocationId,String strPalletId) throws Exception{
		  try{
			  
	    Document inXMLGetLPNDtls = XMLUtil.createDocument("GetLPNDetails");
        Element eleGetLPNDtls = inXMLGetLPNDtls.getDocumentElement();
        eleGetLPNDtls.setAttribute("Node", strNode);
        eleGetLPNDtls.setAttribute("EnterpriseCode", strEnterpriseKey);
        Element lpnElement = inXMLGetLPNDtls.createElement("LPN");
        lpnElement.setAttribute("PalletId", strPalletId);
        eleGetLPNDtls.appendChild(lpnElement);
        log.verbose("Input to GetLPNDetails API: "+XMLUtil.getXMLString(inXMLGetLPNDtls));
        
		Document tempGetLPNDtls = YFCDocument.parse("<GetLPNDetails EnterpriseCode=\"\" InventoryOrganizationCode=\"\" Node=\"\"> " +
				"<LPN CaseId=\"\" CaseQuantity=\"\" PalletId=\"\" > " +
				"<ItemInventoryDetailList> " +
				"<ItemInventoryDetail CaseId=\"\" CountryOfOrigin=\"\" FifoNo=\"\" InventoryStatus=\"\" PalletId=\"\" PendOutQty=\"\" Quantity=\"\" " +
				"Segment=\"\" SegmentType=\"\" ShipByDate=\"\"> " +
				"<InventoryItem ItemID=\"\" ProductClass=\"\" UnitOfMeasure=\"\"> " +
				"<Item ItemID=\"\" ItemKey=\"\" UnitOfMeasure=\"\"> " +
				"</Item> " +
				"</InventoryItem>" +
				"</ItemInventoryDetail> " +
				"</ItemInventoryDetailList> " +
				"</LPN> " +
				"</GetLPNDetails>").getDocument();
		env.setApiTemplate("getLPNDetails", tempGetLPNDtls);
		
		Document outXMLGetLPNDtls = AcademyUtil.invokeAPI(env,"getLPNDetails",inXMLGetLPNDtls);
		env.clearApiTemplates();
		log.verbose("Output to getLPNDetails API: "+XMLUtil.getXMLString(outXMLGetLPNDtls));
		
		Element outEle = outXMLGetLPNDtls.getDocumentElement();
		Element ItemDtlList = (Element) outEle.getElementsByTagName("ItemInventoryDetailList").item(0);
		Element ItemInvDtlEle = (Element) ItemDtlList.getElementsByTagName("ItemInventoryDetail").item(0);
		String sQty = ItemInvDtlEle.getAttribute("Quantity");
		
		Element InvItemEle = (Element) ItemInvDtlEle.getElementsByTagName("InventoryItem").item(0);
		String sItemId = InvItemEle.getAttribute("ItemID");
		String sProductClass = InvItemEle.getAttribute("ProductClass");
		String sUOM = InvItemEle.getAttribute("UnitOfMeasure");
		
          Document moveLocationDoc=XMLUtil.createDocument("MoveLocationInventory");
		  Element moveLocationEle= moveLocationDoc.getDocumentElement();
		  moveLocationEle.setAttribute("EnterpriseCode", strEnterpriseKey);
		  moveLocationEle.setAttribute("Node", strNode);
		  
		  Element sourceEle=moveLocationDoc.createElement("Source");
		  moveLocationEle.appendChild(sourceEle);
		  sourceEle.setAttribute("LocationId", strTargetLocationId);
		  sourceEle.setAttribute("PalletId", strPalletId);
		  
		  Element invEle=moveLocationDoc.createElement("Inventory");
		  sourceEle.appendChild(invEle);
		  
		  invEle.setAttribute("Quantity", sQty);
		  
		  Element invItemEle=moveLocationDoc.createElement("InventoryItem");
		  invEle.appendChild(invItemEle);
		  
		  invItemEle.setAttribute("ItemID", sItemId);
		  invItemEle.setAttribute("ProductClass", sProductClass);
		  invItemEle.setAttribute("UnitOfMeasure", sUOM);
		  
		  Element destEle=moveLocationDoc.createElement("Destination");
		  moveLocationEle.appendChild(destEle);
		  
		  destEle.setAttribute("LocationId", strTargetLocationId);
		  destEle.setAttribute("PalletId","");

		  log.verbose("Input XML to moveLocInventory method: "+XMLUtil.getXMLString(moveLocationDoc));
		  AcademyUtil.invokeAPI(env,"moveLocationInventory",moveLocationDoc);

		  
		  }catch(Exception e)
		  {	  
			  log.verbose("Error captured in moveLocationInventory method");
		  	  throw e;
		  }
	  }
  }

