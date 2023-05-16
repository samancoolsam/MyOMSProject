package com.academy.wsc.extn.mashups;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Element;

import com.ibm.wsc.common.mashups.WSCBaseMashup;
import com.ibm.wsc.mashups.utils.WSCMashupUtils;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.framework.utils.SCXmlUtils;
import com.sterlingcommerce.ui.web.framework.context.SCUIContext;
import com.sterlingcommerce.ui.web.framework.helpers.SCUILocalizationHelper;
import com.sterlingcommerce.ui.web.framework.helpers.SCUIMashupHelper;
import com.sterlingcommerce.ui.web.framework.mashup.SCUIMashupMetaData;
import com.sterlingcommerce.ui.web.framework.utils.SCUIUtils;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.ui.backend.util.APIManager;


public class AcademyUpdateShipmentPickQuantityExtnMashup extends WSCBaseMashup{

	private static final YFCLogCategory	log	= YFCLogCategory.instance(AcademyUpdateShipmentPickQuantityExtnMashup.class);
	
	private static final String ZERO_QUANTITY = "0";

  
  
  public Element massageInput(Element inputEl, SCUIMashupMetaData mashupMetaData, SCUIContext uiContext)
  {
    Element eInput = super.massageInput(inputEl, mashupMetaData, uiContext);
    String action = SCXmlUtil.getAttribute(eInput, "Action");
    eInput.removeAttribute("Action");
    
    if ((!SCUIUtils.isVoid(action)) && (!SCUIUtils.equals("Continue", action)))
    {
      Element getShipmentLineListInputEl = SCXmlUtil.createDocument("ShipmentLine").getDocumentElement();
      
      if ((SCUIUtils.equals("PickAllLine", action)) || (SCUIUtils.equals("PickLine", action)) || (SCUIUtils.equals("MarkLineAsShortage", action))) {
        getShipmentLineListInputEl.setAttribute("ShipmentLineKey", getShipmentLineKeyFromInput(eInput));
      } else if ((SCUIUtils.equals("StartOver", action)) || (SCUIUtils.equals("PickAll", action)) || (SCUIUtils.equals("MarkAllLinesShortage", action)) || (SCUIUtils.equals("StartOverFromBatchPick", action))) {
        getShipmentLineListInputEl.setAttribute("ShipmentKey", eInput.getAttribute("ShipmentKey"));
      }
      
      WSCMashupUtils.addAttributeToUIContext(uiContext, "shipmentKey", this, eInput.getAttribute("ShipmentKey"));
      
      Element shipmentLineListElement = (Element)SCUIMashupHelper.invokeMashup("backroomPick_getShipmentLineListToUpdatePickQty", getShipmentLineListInputEl, uiContext);
      
      WSCMashupUtils.addAttributeToUIContext(uiContext, "shipmentLineListOutput", this, shipmentLineListElement);
      WSCMashupUtils.addAttributeToUIContext(uiContext, "shipmentLineListInput", this, getShipmentLineListInputEl);
      WSCMashupUtils.addAttributeToUIContext(uiContext, "action", this, action);
      
      if ((!SCUIUtils.equals("PickLine", action)) && (!SCUIUtils.equals("MarkLineAsShortage", action))) {
        eInput = getChangeShipmentInputByAction(eInput, (Element)shipmentLineListElement.cloneNode(true), action);
      }
      
      if (SCUIUtils.equals("StartOverFromBatchPick", action)) {
        eInput.setAttribute("HoldLocation", "");
        eInput.setAttribute("IncludedInBatch", "N");
		//changes for BOPIS-1621:Begin
		eInput.setAttribute("PickticketNo", "");
		//BOPIS-1621: end
        Element storeBatchList = SCXmlUtil.getChildElement(eInput, "StoreBatchList");
        String sStoreBatchKey = SCXmlUtil.getXpathAttribute(storeBatchList,"StoreBatch/@StoreBatchKey");
        WSCMashupUtils.addAttributeToUIContext(uiContext, "storeBatchList", this, storeBatchList);
        WSCMashupUtils.addAttributeToUIContext(uiContext, "storeBatchkey", this, sStoreBatchKey);
        eInput.removeChild(storeBatchList);
      }
      
      if ((SCUIUtils.equals("MarkLineAsShortage", action)) || (SCUIUtils.equals("MarkAllLinesShortage", action)))
      {
        String shortageReasonCode = SCXmlUtil.getAttribute(eInput, "ShortageReasonCode");
        eInput.removeAttribute("ShortageReasonCode");
        
        //if (SCUIUtils.equals("AllInventoryShortage", shortageReasonCode)) {
          eInput.setAttribute("BackOrderRemovedQuantity", "Y");
       // }
        
        Element shipmentLinesChildElement = SCXmlUtil.getChildElement(eInput, "ShipmentLines");
        
        ArrayList<Element> shipmentLineList = SCXmlUtil.getChildren(shipmentLinesChildElement, "ShipmentLine");
        
        if (shipmentLineList.size() == 1) {
          Element shipmentLineElement = (Element)shipmentLineList.get(0);
          String shortageQty = SCXmlUtil.getAttribute(shipmentLineElement, "ShortageQty");
          
          if (!SCUIUtils.equals("AllInventoryShortage", shortageReasonCode)) {
            shipmentLineElement.removeAttribute("ShortageQty");
          }
          shipmentLineElement.setAttribute("ShortageQty", shortageQty); 
          
        }
        

        WSCMashupUtils.addAttributeToUIContext(uiContext, "ShortageReasonCode", this, shortageReasonCode);
      }
    }
    
	

    return eInput;
  }
  
//  public Element massageInput(Element inputEl, SCUIMashupMetaData mashupMetaData, SCUIContext uiContext)
//	  {
//		String shortageQty = null;
//		Element shipmentLinesChildElement = SCXmlUtil.getChildElement(inputEl, "ShipmentLines");
//        
//        ArrayList<Element> shipmentLineList = SCXmlUtil.getChildren(shipmentLinesChildElement, "ShipmentLine");
//        if (shipmentLineList.size() == 1)
//        {
//          Element shipmentLineElement = (Element)shipmentLineList.get(0);
//          shortageQty = SCXmlUtil.getAttribute(shipmentLineElement, "ShortageQty");
//        }
//        
//        Element eInputNew = super.massageInput(inputEl, mashupMetaData, uiContext);
//        /*Removed the OOB condition where the below attribute is being set only if ShortageReason is "AllInventoryShortage"  */
//        eInputNew.setAttribute("BackOrderRemovedQuantity", "Y");        
//        Element shipmentLinesChildElementNew = SCXmlUtil.getChildElement(eInputNew, "ShipmentLines");        
//        ArrayList<Element> shipmentLineListNew = SCXmlUtil.getChildren(shipmentLinesChildElementNew, "ShipmentLine");
//        if (shipmentLineList.size() == 1)
//        {
//        	Element shipmentLineElementNew = (Element)shipmentLineListNew.get(0);
//        	shipmentLineElementNew.setAttribute("ShortageQty", shortageQty); 
//        }
//        return eInputNew;
//	  }








  public Element massageOutput(Element outEl, SCUIMashupMetaData mashupMetaData, SCUIContext uiContext)
  {
    Element eOutput = super.massageOutput(outEl, mashupMetaData, uiContext);
    
    Element shipmentLineListElement = (Element)WSCMashupUtils.getAttributeFromUIContext(uiContext, "shipmentLineListOutput", this, true);
    String action = (String)WSCMashupUtils.getAttributeFromUIContext(uiContext, "action", this, true);
    String status = SCXmlUtil.getAttribute(outEl, "Status");
    if ((!SCUIUtils.isVoid(action)) && (!SCUIUtils.equals("Continue", action)))
    {
      Element invokeUEInputElement = getUpdateInventoryUEInput(shipmentLineListElement, action);
      
      SCUIMashupHelper.invokeMashup("backroomPick_updateLocationInventoryUE", invokeUEInputElement, uiContext);
    }
    

    if (SCUIUtils.equals("StartOverFromBatchPick", action)) {
      handleStartOverBatchPick(outEl, uiContext);
      // BOPIS-1589 BEGIN
      Element eInputForManageBatch = SCXmlUtil.createDocument("Shipment").getDocumentElement();
      eInputForManageBatch.setAttribute("ShipmentKey", (String)WSCMashupUtils.getAttributeFromUIContext(uiContext, "shipmentKey", this, true));
      eInputForManageBatch.setAttribute("StoreBatchKey", (String)WSCMashupUtils.getAttributeFromUIContext(uiContext, "storeBatchkey", this, true));
     /* try
	    {
	      SCUIMashupHelper.invokeMashup("startOverFromBatchPick_manageBatch", eInputForManageBatch, uiContext);
	    } catch (APIManager.XMLExceptionWrapper e) {
	      throw e;
	    }*/
   // BOPIS-1589 END
    }
    
    if ((SCUIUtils.equals("MarkLineAsShortage", action)) || (SCUIUtils.equals("MarkAllLinesShortage", action))) {
    	// Commenting below code which is invoking changeOrder Api, Since it is handled in the 'AcademyStoreRecordShortageService' service
    	//      addOrderNotesForShortageLines(shipmentLineListElement, uiContext);
      if (isLastShortageLineForCancellingShipment(uiContext)) {
        String shipmentKey = (String)WSCMashupUtils.getAttributeFromUIContext(uiContext, "shipmentKey", this, true);
        Element cancelShipmentInput = SCXmlUtil.createDocument("Shipment").getDocumentElement();
        cancelShipmentInput.setAttribute("ShipmentKey", shipmentKey);
        SCUIMashupHelper.invokeMashup("backroomPickUp_cancelShipment", cancelShipmentInput, uiContext);
        eOutput.setAttribute("Action", "ShowCancelPopup");
        return eOutput;
      }
    }
    
    if ((SCUIUtils.equals("PickAllLine", action)) || (SCUIUtils.equals("PickLine", action)) || (SCUIUtils.equals("MarkLineAsShortage", action)))
    {
      Element getShipmentLineListInputEl = (Element)WSCMashupUtils.getAttributeFromUIContext(uiContext, "shipmentLineListInput", this, true);
      
      Element shipmentLineListOutput = (Element)SCUIMashupHelper.invokeMashup("backroomPick_getShipmentLineDetails", getShipmentLineListInputEl, uiContext);
      
      if ((!SCUIUtils.isVoid(shipmentLineListOutput)) && (SCXmlUtil.getChildren(shipmentLineListOutput, "ShipmentLine").size() > 0)) {
        return (Element)SCXmlUtil.getChildren(shipmentLineListOutput, "ShipmentLine").get(0);
      }
    }
    

    if (SCUIUtils.equals("MarkAllLinesShortage", action)) {
      eOutput.setAttribute("Action", "MarkAllLinesShortage");
    }
    
    return eOutput;
  }
  
  private void handleStartOverBatchPick(Element outEl, SCUIContext uiContext)
  {
    String status = SCXmlUtil.getAttribute(outEl, "Status");
    if (!status.contains("1100.70.06.20"))
    {
		log.verbose("Here to see status" + outEl);
      Element changeShipmentStatusInput = SCXmlUtil.createDocument("Shipment").getDocumentElement();
      changeShipmentStatusInput.setAttribute("BaseDropStatus", "1100.70.06.20");
      changeShipmentStatusInput.setAttribute("ShipmentKey", outEl.getAttribute("ShipmentKey"));
      changeShipmentStatusInput.setAttribute("TransactionId", "YCD_BACKROOM_PICK_IN_PROGRESS");
      SCUIMashupHelper.invokeMashup("common_changeShipmentStatus", changeShipmentStatusInput, uiContext);
    }
    
    Element getStoreBatchLocationListInput = SCXmlUtil.createDocument("StoreBatchLocation").getDocumentElement();
    String shipmentKey = (String)WSCMashupUtils.getAttributeFromUIContext(uiContext, "shipmentKey", this, false);
    getStoreBatchLocationListInput.setAttribute("ShipmentKey", shipmentKey);
    Element getStoreBatchLocationListOutput = (Element)SCUIMashupHelper.invokeMashup("common_getStoreBatchLocationList", getStoreBatchLocationListInput, uiContext);
    ArrayList<Element> storeBatchLocList = SCXmlUtil.getChildren(getStoreBatchLocationListOutput, "StoreBatchLocation");
    if ((!SCUIUtils.isVoid(storeBatchLocList)) && (storeBatchLocList.size() > 0)) {
      Iterator<Element> storeBatchLocItr = storeBatchLocList.iterator();
      while (storeBatchLocItr.hasNext()) {
        Element storeBatchLocation = (Element)storeBatchLocItr.next();
        Element manageStoreBatch = SCXmlUtil.createDocument("StoreBatchLocation").getDocumentElement();
        manageStoreBatch.setAttribute("StoreBatchLocationKey", storeBatchLocation.getAttribute("StoreBatchLocationKey"));
        SCUIMashupHelper.invokeMashup("common_manageStoreBatchLocationList", manageStoreBatch, uiContext);
      }
    }
    
    Element storeBatchList = (Element)WSCMashupUtils.getAttributeFromUIContext(uiContext, "storeBatchList", this, true);
    ArrayList<Element> orginalStoreBatchList = SCXmlUtil.getChildren(storeBatchList, "StoreBatch");
    if ((!SCUIUtils.isVoid(orginalStoreBatchList)) && (orginalStoreBatchList.size() > 0)) {
      Iterator<Element> orgStoreBatchItr = orginalStoreBatchList.iterator();
      while (orgStoreBatchItr.hasNext()) {
        Element storeBatch = (Element)orgStoreBatchItr.next();
        Element storeBatchInput = SCXmlUtil.createDocument("StoreBatch").getDocumentElement();
        storeBatchInput.setAttribute("StoreBatchKey", storeBatch.getAttribute("StoreBatchKey"));
        Element orgStoreBatchOutput = (Element)SCUIMashupHelper.invokeMashup("common_getStoreBatchDetailsForStatus", storeBatchInput, uiContext);
        Element storeBatchOut = SCXmlUtil.getChildElement(orgStoreBatchOutput, "StoreBatch");
        int numOfRecs = Integer.valueOf(storeBatchOut.getAttribute("TotalNumberOfShipments")).intValue();
        String statusOutput = storeBatchOut.getAttribute("Status");
        boolean isCancelInvokedForBatch = false;
        if ((SCUIUtils.equals(Integer.valueOf(numOfRecs), Integer.valueOf(0))) && (!SCUIUtils.equals(statusOutput, "9000")))
        {
          storeBatchInput.setAttribute("Status", "9000");
          SCUIMashupHelper.invokeMashup("common_manageStoreBatchForStatus", storeBatchInput, uiContext);
          isCancelInvokedForBatch = true;
        }
        if ((SCUIUtils.equals(statusOutput, "1100")) && (SCUIUtils.equals(Boolean.valueOf(isCancelInvokedForBatch), Boolean.valueOf(false))))
        {
          Element getShipmentLinesToBePickedInput = SCXmlUtil.createDocument("ShipmentLine").getDocumentElement();
          getShipmentLinesToBePickedInput.setAttribute("StoreBatchKey", storeBatch.getAttribute("StoreBatchKey"));
          Element toBePickedLinesOutput = (Element)SCUIMashupHelper.invokeMashup("common_getShipmentLinesToBePicked", getShipmentLinesToBePickedInput, uiContext);
          int numOfShipLines = Integer.valueOf(toBePickedLinesOutput.getAttribute("TotalNumberOfRecords")).intValue();
          if (SCUIUtils.equals(Integer.valueOf(numOfShipLines), Integer.valueOf(0)))
          {

            storeBatchInput.setAttribute("Status", "2000");
            SCUIMashupHelper.invokeMashup("common_manageStoreBatchForStatus", storeBatchInput, uiContext);
            
            Element getShipmentLinesDetailsInput = SCXmlUtil.createDocument("ShipmentLine").getDocumentElement();
            getShipmentLinesDetailsInput.setAttribute("StoreBatchKey", storeBatch.getAttribute("StoreBatchKey"));
            Element getShipmentLinesDetailsOutput = (Element)SCUIMashupHelper.invokeMashup("common_getShipmentLinesInBatch", getShipmentLinesDetailsInput, uiContext);
            
            ArrayList<Element> shipmentLineListForBatch = SCXmlUtil.getChildren(getShipmentLinesDetailsOutput, "ShipmentLine");
            Map<String, Element> shipmentKeyShipmentDetailsMap = new HashMap();
            if ((!SCUIUtils.isVoid(shipmentLineListForBatch)) && (shipmentLineListForBatch.size() > 0)) {
              Iterator<Element> shipmentLineItr = shipmentLineListForBatch.iterator();
              while (shipmentLineItr.hasNext()) {
                Element shipmentLine = (Element)shipmentLineItr.next();
                Element shipmentObject = SCXmlUtil.getChildElement(shipmentLine, "Shipment");
                String shipmentKeyOut = shipmentObject.getAttribute("ShipmentKey");
                if ((!SCUIUtils.isVoid(shipmentKeyOut)) && 
                  (!shipmentKeyShipmentDetailsMap.containsKey(shipmentKey)) && (!shipmentKeyOut.equals(shipmentKey))) {
                  shipmentKeyShipmentDetailsMap.put(shipmentKeyOut, shipmentObject);
                }
              }
            }
            

            if (!shipmentKeyShipmentDetailsMap.isEmpty()) {
              Iterator<String> shipmentKeysItr = shipmentKeyShipmentDetailsMap.keySet().iterator();
              while (shipmentKeysItr.hasNext()) {
                String sShipmentKey = (String)shipmentKeysItr.next();
                Element getIndShipmentLinesToBePickedInput = SCXmlUtil.createDocument("ShipmentLine").getDocumentElement();
                getIndShipmentLinesToBePickedInput.setAttribute("ShipmentKey", sShipmentKey);
                Element getIndShipmentLinesToBePickedOutput = (Element)SCUIMashupHelper.invokeMashup("common_getShipmentLinesToBePicked", getIndShipmentLinesToBePickedInput, uiContext);
                int numOfIndShipLines = Integer.valueOf(getIndShipmentLinesToBePickedOutput.getAttribute("TotalNumberOfRecords")).intValue();
                if (SCUIUtils.equals(Integer.valueOf(numOfIndShipLines), Integer.valueOf(0)))
                {

                  Element updateShipmentObject = (Element)shipmentKeyShipmentDetailsMap.get(sShipmentKey);
                  if (updateShipmentObject.getAttribute("Status").contains("1100.70.06.20")) {
                    String dMethod = updateShipmentObject.getAttribute("DeliveryMethod");
                    if (SCUIUtils.equals(dMethod, "PICK")) {
                      Element changeShipmentStatusInput = SCXmlUtil.createDocument("Shipment").getDocumentElement();
                      changeShipmentStatusInput.setAttribute("BaseDropStatus", "1100.70.06.30");
                      changeShipmentStatusInput.setAttribute("ShipmentKey", sShipmentKey);
                      changeShipmentStatusInput.setAttribute("TransactionId", "YCD_BACKROOM_PICK");
                      SCUIMashupHelper.invokeMashup("common_changeShipmentStatus", changeShipmentStatusInput, uiContext);
                    } else if (SCUIUtils.equals(dMethod, "SHP")) {
                      Element changeShipmentStatusInput = SCXmlUtil.createDocument("Shipment").getDocumentElement();
                      changeShipmentStatusInput.setAttribute("BaseDropStatus", "1100.70.06.50");
                      changeShipmentStatusInput.setAttribute("ShipmentKey", sShipmentKey);
                      changeShipmentStatusInput.setAttribute("TransactionId", "YCD_BACKROOM_PICK");
                      SCUIMashupHelper.invokeMashup("common_changeShipmentStatus", changeShipmentStatusInput, uiContext);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
  







  private String getShipmentLineKeyFromInput(Element mashupInputEl)
  {
    String shipmentLineKey = "";
    
    Element shipmentLineListElement = SCXmlUtil.getChildElement(mashupInputEl, "ShipmentLines");
    
    if (!SCUIUtils.isVoid(shipmentLineListElement)) {
      ArrayList<Element> shipmentLineList = SCXmlUtil.getChildren(shipmentLineListElement, "ShipmentLine");
      
      if ((!SCUIUtils.isVoid(shipmentLineList)) && (shipmentLineList.size() > 0))
      {
        Iterator<Element> shipmentLineItr = shipmentLineList.iterator();
        if (shipmentLineItr.hasNext())
        {
          Element shipmentLine = (Element)shipmentLineItr.next();
          shipmentLineKey = shipmentLine.getAttribute("ShipmentLineKey");
        }
      }
    }
    


    return shipmentLineKey;
  }
  









  private Element getUpdateInventoryUEInput(Element shipmentLineListElement, String action)
  {
    Element invokeUEInputElement = SCXmlUtil.createDocument("InvokeUE").getDocumentElement();
    Element inventoryListElement = SCXmlUtil.createChild(SCXmlUtil.createChild(SCXmlUtil.createChild(invokeUEInputElement, "XMLData"), "UpdateLocationInventory"), "InventoryList");
    
    ArrayList<Element> shipmentLineList = SCXmlUtil.getChildren(shipmentLineListElement, "ShipmentLine");
    
    if ((!SCUIUtils.isVoid(shipmentLineList)) && (shipmentLineList.size() > 0))
    {
      Iterator<Element> shipmentLineItr = shipmentLineList.iterator();
      while (shipmentLineItr.hasNext())
      {
        Element shipmentLine = (Element)shipmentLineItr.next();
        Element inventoryElement = SCXmlUtil.createDocument("Inventory").getDocumentElement();
        
        if (SCUIUtils.equals("StartOver", action)) {
          inventoryElement.setAttribute("Quantity", "0");
        } else if (((!SCUIUtils.isVoid(action)) && (SCUIUtils.equals("PickAll", action))) || (SCUIUtils.equals("PickAllLine", action))) {
          inventoryElement.setAttribute("Quantity", shipmentLine.getAttribute("Quantity"));
        }
        
        Element invItemElement = SCXmlUtil.createChild(inventoryElement, "InventoryItem");
        invItemElement.setAttribute("ItemID", shipmentLine.getAttribute("ItemID"));
        invItemElement.setAttribute("ProductClass", shipmentLine.getAttribute("ProductClass"));
        invItemElement.setAttribute("UnitOfMeasure", shipmentLine.getAttribute("UnitOfMeasure"));
        
        SCXmlUtil.importElement(inventoryListElement, inventoryElement);
      }
    }
    


    return invokeUEInputElement;
  }
  













  private Element getChangeShipmentInputByAction(Element mashupInput, Element shipmentLineListEle, String action)
  {
    Element shipmentLinesChildElement = SCXmlUtil.getChildElement(mashupInput, "ShipmentLines");
    
    if (SCUIUtils.isVoid(shipmentLinesChildElement)) {
      shipmentLinesChildElement = SCXmlUtil.createChild(mashupInput, "ShipmentLines");
    }
    
    ArrayList<Element> shipmentLineList = SCXmlUtil.getChildren(shipmentLineListEle, "ShipmentLine");
    
    if ((!SCUIUtils.isVoid(shipmentLineList)) && (shipmentLineList.size() > 0))
    {
      Iterator<Element> shipmentLineItr = shipmentLineList.iterator();
      while (shipmentLineItr.hasNext())
      {
        Element shipmentLine = (Element)shipmentLineItr.next();
        
        if (SCUIUtils.equals("StartOver", action))
        {
          int shipmentLineShortQty = 0;
          try
          {
            if (!SCUIUtils.isVoid(shipmentLine.getAttribute("ShortageQty"))) {
              shipmentLineShortQty = Integer.valueOf(shipmentLine.getAttribute("ShortageQty")).intValue();
            }
          }
          catch (NumberFormatException e) {}
          





          shipmentLine.setAttribute("BackroomPickedQuantity", "0");

        }
        else if (SCUIUtils.equals("PickAll", action)) {
          shipmentLine.setAttribute("BackroomPickedQuantity", shipmentLine.getAttribute("Quantity"));
        } else { if (SCUIUtils.equals("PickAllLine", action)) {
            ArrayList<Element> inputShipLineList = SCXmlUtil.getChildren(shipmentLinesChildElement, "ShipmentLine");
            ((Element)inputShipLineList.get(0)).setAttribute("BackroomPickedQuantity", shipmentLine.getAttribute("Quantity"));
            break; }
          if (SCUIUtils.equals("StartOverFromBatchPick", action)) {
            int shipmentLineShortQty = 0;
            try
            {
              if (!SCUIUtils.isVoid(shipmentLine.getAttribute("ShortageQty"))) {
                shipmentLineShortQty = Integer.valueOf(shipmentLine.getAttribute("ShortageQty")).intValue();
              }
            }
            catch (NumberFormatException e) {}
            





            shipmentLine.setAttribute("BackroomPickedQuantity", "0");
            
            shipmentLine.setAttribute("StoreBatchKey", "");
            shipmentLine.setAttribute("StagedQuantity", "0");
            shipmentLine.setAttribute("BatchPickPriority", "");



          }
          else if (SCUIUtils.equals("MarkAllLinesShortage", action)) {
            String shortageReasonCode = SCXmlUtil.getAttribute(mashupInput, "ShortageReasonCode");
            
            double backroomPickedQty = 0.0D;
            
            if (!SCUIUtils.isVoid(shipmentLine.getAttribute("BackroomPickedQuantity"))) {
              backroomPickedQty = Double.parseDouble(shipmentLine.getAttribute("BackroomPickedQuantity"));
            }
            
            if (SCUIUtils.equals("AllInventoryShortage", shortageReasonCode)) {
              shipmentLine.setAttribute("Quantity", String.valueOf(backroomPickedQty));
              shipmentLine.setAttribute("ShortageQty", getShortageQtyForShipmentLine(shipmentLine));
            }
          }
        }
        


        shipmentLine.removeAttribute("ItemID");
        shipmentLine.removeAttribute("ProductClass");
        shipmentLine.removeAttribute("UnitOfMeasure");
        shipmentLine.removeAttribute("OriginalQuantity");
        shipmentLine.removeAttribute("OrderHeaderKey");
        shipmentLine.removeAttribute("OrderLineKey");
        SCXmlUtil.importElement(shipmentLinesChildElement, shipmentLine);
      }
    }
    


    return mashupInput;
  }
  

//Commenting below code which is invoking changeOrder Api, Since it is handled in the 'AcademyStoreRecordShortageService' service
/*
  private void addOrderNotesForShortageLines(Element shipmentLineListEle, SCUIContext uiContext)
  {
    ArrayList<Element> shipmentLineList = SCXmlUtil.getChildren(shipmentLineListEle, "ShipmentLine");
    
    int numOfShipLines = shipmentLineList.size();
    Element addNotesInputEle = null;
    String mashupName = "backroomPickUp_addShortageReasonNoteForShipmentLine";
    
    if ((!SCUIUtils.isVoid(shipmentLineList)) && (numOfShipLines > 0))
    {
      if (numOfShipLines == 1)
      {
        Element shipmentLine = (Element)shipmentLineList.get(0);
        
        Element orderEle = SCXmlUtil.createDocument("Order").getDocumentElement();
        orderEle.setAttribute("OrderHeaderKey", shipmentLine.getAttribute("OrderHeaderKey"));
        
        addNotesInputEle = appendNoteTextToOrder(orderEle, shipmentLine, uiContext);

      }
      else
      {
        mashupName = "backroomPickUp_addShortageReasonNoteForShipmentLineMultiApi";
        Element orderEle = SCXmlUtil.createDocument("Order").getDocumentElement();
        
        Iterator<Element> itr = shipmentLineList.iterator();
        while (itr.hasNext())
        {
          Element shipmentLine = (Element)itr.next();
          boolean orderHeaderKeySet = false;
          double backroomPickedQty = 0.0D;double originalQty = 0.0D;
          
          if (!SCUIUtils.isVoid(shipmentLine.getAttribute("BackroomPickedQuantity"))) {
            backroomPickedQty = Double.parseDouble(shipmentLine.getAttribute("BackroomPickedQuantity"));
          }
          if (!SCUIUtils.isVoid(shipmentLine.getAttribute("OriginalQuantity"))) {
            originalQty = Double.parseDouble(shipmentLine.getAttribute("OriginalQuantity"));
          }
          
          if (backroomPickedQty < originalQty)
          {
            if (!orderHeaderKeySet) {
              orderEle.setAttribute("OrderHeaderKey", shipmentLine.getAttribute("OrderHeaderKey"));
              orderHeaderKeySet = true;
            }
            
            orderEle = appendNoteTextToOrder(orderEle, shipmentLine, uiContext);
          }
        }
        

        Element multiAPIEle = SCXmlUtil.createDocument("MultiApi").getDocumentElement();
        Element inputEle = SCXmlUtil.createChild(SCXmlUtil.createChild(multiAPIEle, "API"), "Input");
        SCXmlUtil.importElement(inputEle, orderEle);
        
        addNotesInputEle = multiAPIEle;
      }
    }
    




    SCUIMashupHelper.invokeMashup(mashupName, addNotesInputEle, uiContext);
  }
  */
  









  private Element appendNoteTextToOrder(Element orderEle, Element shipmentLine, SCUIContext uiContext)
  {
    String shortageReasonCode = (String)WSCMashupUtils.getAttributeFromUIContext(uiContext, "ShortageReasonCode", this, false);
    
    Element orderLinesEle = SCXmlUtil.getChildElement(orderEle, "OrderLines");
    
    if (SCUIUtils.isVoid(orderLinesEle)) {
      orderLinesEle = SCXmlUtil.createChild(orderEle, "OrderLines");
    }
    
    Element orderLineEle = SCXmlUtil.createChild(orderLinesEle, "OrderLine");
    
    orderLineEle.setAttribute("OrderLineKey", shipmentLine.getAttribute("OrderLineKey"));
    
    Element noteEle = SCXmlUtil.createChild(SCXmlUtil.createChild(orderLineEle, "Notes"), "Note");
    
    if (SCUIUtils.equals("AllInventoryShortage", shortageReasonCode)) {
      noteEle.setAttribute("NoteText", SCUILocalizationHelper.getString(uiContext, "Inventory_Shortage_OrderLine_Note"));
    } else {
      noteEle.setAttribute("NoteText", getNoteTextForCustomShortageReasonCode(uiContext, shortageReasonCode));
    }
    
    return orderEle;
  }
  









  private String getNoteTextForCustomShortageReasonCode(SCUIContext uiContext, String shortageReasonCode)
  {
    String noteText = "";String codeShortDescription = "";
    
    Element commonCodeElement = SCXmlUtil.createDocument("CommonCode").getDocumentElement();
    commonCodeElement.setAttribute("CodeType", "YCD_PICK_SHORT_RESOL");
    
    Element commonCodeListElement = (Element)SCUIMashupHelper.invokeMashup("common_getReasonCodeList", commonCodeElement, uiContext);
    
    ArrayList<Element> commonCodeList = SCXmlUtil.getChildren(commonCodeListElement, "CommonCode");
    Iterator<Element> itr = commonCodeList.iterator();
    while (itr.hasNext()) {
      Element commonCode = (Element)itr.next();
      if (SCUIUtils.equals(shortageReasonCode, commonCode.getAttribute("CodeValue"))) {
        codeShortDescription = commonCode.getAttribute("CodeShortDescription");
        break;
      }
    }
    
    noteText = SCUILocalizationHelper.getString(uiContext, "Custom_Shortage_OrderLine_Note");
    
    Object[] bundleArgs = new Object[2];
    bundleArgs[0] = new Object();
    bundleArgs[0] = codeShortDescription;
    
    noteText = MessageFormat.format(noteText, bundleArgs);
    
    return noteText;
  }
  









  private String getShortageQtyForShipmentLine(Element shipmentLine)
  {
    double shortageQty = 0.0D;double backroomPickedQty = 0.0D;double originalQty = 0.0D;
    
    if (!SCUIUtils.isVoid(shipmentLine.getAttribute("BackroomPickedQuantity"))) {
      backroomPickedQty = Double.parseDouble(shipmentLine.getAttribute("BackroomPickedQuantity"));
    }
    if (!SCUIUtils.isVoid(shipmentLine.getAttribute("OriginalQuantity"))) {
      originalQty = Double.parseDouble(shipmentLine.getAttribute("OriginalQuantity"));
    }
    
    shortageQty = originalQty - backroomPickedQty;
    
    return String.valueOf(shortageQty);
  }
  








  private boolean isLastShortageLineForCancellingShipment(SCUIContext uiContext)
  {
    boolean cancelShipment = false;
    
    String shipmentKey = (String)WSCMashupUtils.getAttributeFromUIContext(uiContext, "shipmentKey", this, false);
    
    Element shipmentLineListInput = SCXmlUtil.createDocument("ShipmentLine").getDocumentElement();
    shipmentLineListInput.setAttribute("ShipmentKey", shipmentKey);
    
    Element shortedShipmentLineListElement = (Element)SCUIMashupHelper.invokeMashup("backroomPickUp_getCompletelyShortedShipmentLineListCount", shipmentLineListInput, uiContext);
    
    if (SCUIUtils.equals("0", shortedShipmentLineListElement.getAttribute("TotalNumberOfRecords"))) {
      cancelShipment = true;
    }
    
    return cancelShipment;
  }

}
