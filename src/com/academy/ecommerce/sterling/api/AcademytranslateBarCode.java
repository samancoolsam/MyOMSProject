package com.academy.ecommerce.sterling.api;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
/**
 * 
 * @author vjawaria
 *Input XMl to following class is <?xml version="1.0" encoding="UTF-8"?>
<BarCode BarCodeData="1" BarCodeType="ShippingOrInventoryContainer" IgnoreOrdering="Y">
    <ContextualInfo EnterpriseCode="" OrganizationCode="005"/>
</BarCode>
 */
public class AcademytranslateBarCode implements YIFCustomApi { 
	public void setProperties(Properties props) {} 
	
	
	  /**
   * Instance of logger
   */
  private static YFCLogCategory log = YFCLogCategory.instance(AcademytranslateBarCode.class);
  
  /**
   * This method will retrieve ShipmentKey from input XMl and call method callGetDiscrepancyList 
   */
  
  public Document translateBarCode(YFSEnvironment env, Document inDoc) throws Exception {
	  log.beginTimer(" Begining of AcademytranslateBarCode-> translateBarCode Api");
	  // Get BarCodeData from Input XML which is customerPONo for shipment. Call getshipmentlist API with customerPO No.
	  Document outDoc = AcademyUtil.invokeAPI(env, "translateBarCode", inDoc);
	  if(!YFCObject.isVoid(outDoc)){
		  String sCustomerPONo = inDoc.getDocumentElement().getAttribute("BarCodeData");
		  
		  /*Prepare input as follows:
		  <Shipment>
		  <ShipmentLines>
		  <ShipmentLine CustomerPoNo="Final2"/>
		  </ShipmentLines>
		  </Shipment>
*/
		  Document inShipDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		  
		  Element inShipElem = inShipDoc.getDocumentElement();
		  
		  Element eShipmentLinesElem = XMLUtil.createElement(inShipDoc, AcademyConstants.ELE_SHIPMENT_LINES, null);
		  
		  Element eShipmentLineElem =  XMLUtil.createElement(inShipDoc, AcademyConstants.ELE_SHIPMENT_LINE, null);
		  
		  eShipmentLineElem.setAttribute("CustomerPoNo", sCustomerPONo);
		  
		  XMLUtil.appendChild(eShipmentLinesElem, eShipmentLineElem);
		  
		  XMLUtil.appendChild(inShipElem, eShipmentLinesElem);
		  
		  //Call AcademygetShipmentList service to get list of shipment with this customer Po no
		  
		  Document outShipDoc = AcademyUtil.invokeService(env, "AcademygetShipmentList", inShipDoc);
		  
		  if(!YFCObject.isVoid(outShipDoc)){
			  
			  Element elemShipments = XMLUtil.getFirstElementByName(outShipDoc.getDocumentElement(), "Shipments");
			  Element elemShipment  = XMLUtil.getFirstElementByName(elemShipments, AcademyConstants.ELE_SHIPMENT);
			  Element elemShipmentLines  = XMLUtil.getFirstElementByName(elemShipment, AcademyConstants.ELE_SHIPMENT_LINES);
			  
				if(!YFCObject.isVoid(elemShipmentLines)){
	    			Element eShipmentLine = XMLUtil.getFirstElementByName(elemShipmentLines,AcademyConstants.ELE_SHIPMENT_LINE);
	    			 
	    			 	String sParentShipmentKey = eShipmentLine.getAttribute(AcademyConstants.ATTR_PARENT_LINE_KEY);
	    			 	 log.verbose(" Parent ShipmentKey is " + sParentShipmentKey );	
	    			 	
	    	    		String sKitCode = eShipmentLine.getAttribute(AcademyConstants.KIT_CODE);
	    	    		 log.verbose(" KitCode for ShipmentLine is " + sKitCode );
	    	    		 
	    	    		 if(!sParentShipmentKey.equals("") || sKitCode.equals(AcademyConstants.BUNDLE)){
	    	    			 outDoc.getDocumentElement().setAttribute("IsKitShipment", AcademyConstants.STR_YES);
	    	    			 
	    	    			 String sTotalKitContainers = elemShipmentLines.getAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS);
	    	    			 
	    	    			 int dCompQty = (Integer.valueOf(sTotalKitContainers)-1);
	    	    			 
	    	    			 outDoc.getDocumentElement().setAttribute("NoOfComponent", String.valueOf(dCompQty));
	    	    			 
	    	    			 log.verbose("Shipment is a Kit Shipment" );
	    	    		 }
	    		}
			  
		  }
	  }
	  log.endTimer(" End of AcademytranslateBarCode-> translateBarCode Api");
 return inDoc;
  }
}
