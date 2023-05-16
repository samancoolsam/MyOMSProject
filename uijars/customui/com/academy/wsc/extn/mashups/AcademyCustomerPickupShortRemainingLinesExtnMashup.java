package com.academy.wsc.extn.mashups;

import com.ibm.wsc.common.mashups.WSCBaseMashup;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.ui.web.framework.context.SCUIContext;
import com.sterlingcommerce.ui.web.framework.helpers.SCUIMashupHelper;
import com.sterlingcommerce.ui.web.framework.mashup.SCUIMashupMetaData;
import com.sterlingcommerce.ui.web.framework.utils.SCUIUtils;
import com.yantra.yfc.core.YFCObject;

import java.util.ArrayList;
import java.util.Iterator;
import org.w3c.dom.Element;






public class AcademyCustomerPickupShortRemainingLinesExtnMashup extends WSCBaseMashup
{
  public AcademyCustomerPickupShortRemainingLinesExtnMashup() {}
  
  public Element massageInput(Element inputEl, SCUIMashupMetaData mashupMetaData, SCUIContext uiContext)
  {
    Element eInput = super.massageInput(inputEl, mashupMetaData, uiContext);
    

    Element getShipmentLineListInputEl = SCXmlUtil.createDocument("ShipmentLine").getDocumentElement();
    getShipmentLineListInputEl.setAttribute("ShipmentKey", eInput.getAttribute("ShipmentKey"));
    Element shipmentLineListElement = (Element)SCUIMashupHelper.invokeMashup("customerPickup_getShipmentLineListForShortAll", getShipmentLineListInputEl, uiContext);
    
    Element shipmentLine = SCXmlUtil.getFirstChildElement(SCXmlUtil.getChildElement(eInput, "ShipmentLines"));
    String shortageResolutionReason = shipmentLine.getAttribute("ShortageResolutionReason");
    String cancelReason = shipmentLine.getAttribute("CancelReason");
    
    eInput = getChangeShipmentInputForPickAll(eInput, (Element)shipmentLineListElement.cloneNode(true), shortageResolutionReason, cancelReason);
    return eInput;
  }
  




  private Element getChangeShipmentInputForPickAll(Element mashupInput, Element shipmentLineListEle, String shortageResolutionReason, String cancelReason)
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
        
        shipmentLine.setAttribute("ShortageResolutionReason", shortageResolutionReason);
        shipmentLine.setAttribute("CancelReason", cancelReason);
        
        String custQty = shipmentLine.getAttribute("CustomerPickedQuantity");
        if(!YFCObject.isVoid(custQty)) {
        	Double customerPickedQty = Double.parseDouble(custQty);
        	Double originalQty = Double.parseDouble(shipmentLine.getAttribute("Quantity"));
        	Double qtyD = originalQty - customerPickedQty;
        	// commenting below code since Extn attributes are not required on click of shortage on customer pick screen (cycle count changes)
//	        Element eleExtn = SCXmlUtil.createChild(shipmentLine, "Extn");
//	        eleExtn.setAttribute("ExtnMsgToSIM", "Y");
//	        eleExtn.setAttribute("ExtnReasonCode", cancelReason);
			
	        String qty = String.valueOf(qtyD);
	        shipmentLine.setAttribute("ShortageQty", qty);
        } else {
        	Double originalQty = Double.parseDouble(shipmentLine.getAttribute("Quantity"));
        	// commenting below code since Extn attributes are not required on click of shortage on customer pick screen
//        	Element eleExtn = SCXmlUtil.createChild(shipmentLine, "Extn");
//	        eleExtn.setAttribute("ExtnMsgToSIM", "N");
//			eleExtn.setAttribute("ExtnReasonCode", cancelReason);
	        String qty = String.valueOf(originalQty);
	        shipmentLine.setAttribute("ShortageQty", qty);
        }
        
        SCXmlUtil.importElement(shipmentLinesChildElement, shipmentLine);
      }
    }
    

    return mashupInput;
  }
}