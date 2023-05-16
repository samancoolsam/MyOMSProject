package com.academy.wsc.extn.mashups;

import com.ibm.wsc.common.mashups.WSCBaseMashup;
import com.ibm.wsc.mashups.utils.WSCMashupUtils;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.ui.web.framework.context.SCUIContext;
import com.sterlingcommerce.ui.web.framework.helpers.SCUIAuthorizationHelper;
import com.sterlingcommerce.ui.web.framework.helpers.SCUIMashupHelper;
import com.sterlingcommerce.ui.web.framework.mashup.SCUIMashupMetaData;
import com.sterlingcommerce.ui.web.framework.utils.SCUIUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AcademyShipmentListByShipmentNoOrderNoExtnMashup extends WSCBaseMashup {
	
	
	public Element massageInput(Element inputEl, SCUIMashupMetaData mashupMetaData, SCUIContext uiContext)
	  {
	    Element eInput = super.massageInput(inputEl, mashupMetaData, uiContext);
	    
	    String action = SCXmlUtil.getAttribute(inputEl, "Action");
	    WSCMashupUtils.addAttributeToUIContext(uiContext, "Action", this, action);
	    WSCMashupUtils.addAttributeToUIContext(uiContext, "mashupInput", this, eInput);
	    eInput.removeAttribute("Action");
	    
	    return eInput;
	  }
	  
	  public Element massageOutput(Element outEl, SCUIMashupMetaData mashupMetaData, SCUIContext uiContext)
	  {
	    Element eOutput = super.massageOutput(outEl, mashupMetaData, uiContext);
	    
	    return processShipmentListOutput(eOutput, mashupMetaData, uiContext, true);
	  }
	  
	  private Element getShipmentListByOrderAPIInput(Element mashupInput)
	  {
	    Element apiInput = SCXmlUtil.createDocument("Order").getDocumentElement();
	    
	    apiInput.setAttribute("OrderNo", mashupInput.getAttribute("ShipmentNo"));
	    
	    return apiInput;
	  }
	  
	  private Element processShipmentListOutput(Element shipmentListOutputElement, SCUIMashupMetaData mashupMetaData, SCUIContext uiContext, boolean invokeOrderListAPI)
	  {
	    int shipmentCount = 0;
	    String actionInContext = (String)WSCMashupUtils.getAttributeFromUIContext(uiContext, "Action", this);
	    try
	    {
	      shipmentCount = Integer.parseInt(SCXmlUtil.getAttribute(shipmentListOutputElement, "TotalNumberOfRecords"));
	    }
	    catch (NumberFormatException e) {}
	    if (shipmentCount == 1)
	    {
	      Element shipmentElement = SCXmlUtil.getChildElement(shipmentListOutputElement, "Shipment");
	      String deliveryMethod = shipmentElement.getAttribute("DeliveryMethod");
	      String status = shipmentElement.getAttribute("Status");
	      if ((SCUIUtils.equals("BackroomPick", actionInContext)) && (!SCUIUtils.isVoid(status)) && ((status.contains("1100.70.06.10")) || (status.contains("1100.70.06.20"))))
	      {
	        if (((SCUIUtils.equals("PICK", deliveryMethod)) && (SCUIAuthorizationHelper.hasPermission(uiContext, "WSC000006"))) || ((SCUIUtils.equals("SHP", deliveryMethod)) && (SCUIAuthorizationHelper.hasPermission(uiContext, "WSC000017"))))
	        {
	        	if(!SCUIUtils.equals("1100.70.06.30", status)){
	        		shipmentListOutputElement = WSCMashupUtils.validateShipment(SCXmlUtil.getChildElement(shipmentListOutputElement, "Shipment"), mashupMetaData, uiContext, actionInContext);
	        	}
	        	/*else if(SCUIUtils.equals("1100.70.06.30", status)){
	        		Element errorElem = SCXmlUtil.createDocument("Error").getDocumentElement();
		      	      SCXmlUtil.setAttribute(errorElem, "Action", actionInContext);
		      	      SCXmlUtil.setAttribute(errorElem, "ErrorDescription", "This shipment is already Packed. Invalid Operation!");
		      	      SCXmlUtil.importElement(shipmentListOutputElement, errorElem);
	        	}*/
	        }
	        else
	        {
	          shipmentElement.setAttribute("ErrorDescription", "NoPermission");
	          
	          shipmentListOutputElement = shipmentElement;
	        }
	      }
	      else {
	    	  if(!SCUIUtils.equals("1100.70.06.30", status)){
	    		  shipmentListOutputElement = WSCMashupUtils.validateShipment(SCXmlUtil.getChildElement(shipmentListOutputElement, "Shipment"), mashupMetaData, uiContext, actionInContext);
	    	  }
	    	  else if(SCUIUtils.equals("1100.70.06.30", status)){
        		Element errorElem = SCXmlUtil.createDocument("Error").getDocumentElement();
	      	      SCXmlUtil.setAttribute(errorElem, "Action", actionInContext);
	      	      SCXmlUtil.setAttribute(errorElem, "ErrorDescription", "This shipment is already Packed. Invalid Operation!");
	      	      SCXmlUtil.importElement(shipmentListOutputElement, errorElem);
	    	  }
	      }
	    }
	    else if (shipmentCount > 1)
	    {
	      Element errorElem = SCXmlUtil.createDocument("Error").getDocumentElement();
	      
	      SCXmlUtil.setAttribute(errorElem, "Action", actionInContext);
	      SCXmlUtil.setAttribute(errorElem, "ErrorDescription", "MultipleShipmentsFound");
	      SCXmlUtil.importElement(shipmentListOutputElement, errorElem);
	    }
	    else if ((invokeOrderListAPI) && (shipmentCount == 0))
	    {
	      Element apiInputElement = getShipmentListByOrderAPIInput((Element)WSCMashupUtils.getAttributeFromUIContext(uiContext, "mashupInput", this));
	      Element apiOutputElement = (Element)SCUIMashupHelper.invokeMashup("portlet_getShipmentListForOrder", apiInputElement, uiContext);
	      
	      shipmentListOutputElement = processShipmentListOutput(apiOutputElement, mashupMetaData, uiContext, false);
	    }
	    return shipmentListOutputElement;
	  }
	

}
