package com.academy.ecommerce.interfaces.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;


/**
 * This API will call getShipmentDiscrepancyList from ShipmentKey in env. 
 * If discrepancy is there it will add discrepancies to corresponding Receipt Line Element.
 */
public class AcademyReceiptDetail implements YIFCustomApi {
	
	public void setProperties(Properties props) {} 
	
	  /**
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademyReceiptDetail.class);
    
    /**
     * This method will retrieve ShipmentKey from input XMl and call method callGetDiscrepancyList 
     */
    
    public Document academyReceiptDetail(YFSEnvironment env, Document inDoc) throws Exception {
    	    	
    	log.verbose("Entering AcademyReceiptDetail.AcademyReceiptDetail()");
    	Element inShipmentLine = null;
    	Element inShipmentLines = null;
    	String sParentShipmentKey ="";
    	String sKitCode = "";
    	boolean bkitShipment = false;
    	
    	if(!YFCObject.isVoid(inDoc)){
    		// This logic decides whether shipment is kit shipment or not. Attributes used to verify whether is
    		//kit or not are ParentShipmentKey and KitCode
    		String sShipmentKey = inDoc.getDocumentElement().getAttribute(AcademyConstants.SHIPMENT_KEY);
    		Element elemShipment = XMLUtil.getFirstElementByName(inDoc.getDocumentElement(), AcademyConstants.ELE_SHIPMENT);
    		inShipmentLines = XMLUtil.getFirstElementByName(elemShipment,AcademyConstants.ELE_SHIPMENT_LINES);
    		if(!YFCObject.isVoid(inShipmentLines)){
    			 inShipmentLine = XMLUtil.getFirstElementByName(inShipmentLines,AcademyConstants.ELE_SHIPMENT_LINE);
    			 
    			 	sParentShipmentKey = inShipmentLine.getAttribute(AcademyConstants.ATTR_PARENT_LINE_KEY);
    			 	 log.verbose(" Parent ShipmentKey is " + sParentShipmentKey );	
    			 	
    	    		 sKitCode = inShipmentLine.getAttribute(AcademyConstants.KIT_CODE);
    	    		 log.verbose(" KitCode for ShipmentLine is " + sKitCode );
    	    		 
    	    		 if(!sParentShipmentKey.equals("") || sKitCode.equals(AcademyConstants.BUNDLE)){
    	    			 bkitShipment = true;
    	    			 
    	    			 log.verbose("Shipment is a Kit Shipment" );
    	    		 }
    			}
    		
    		//Method to invoke getDiscrpanylist API with ShipmentKey.
    		
    		if(!YFCObject.isVoid(sShipmentKey)){
    			
    			log.verbose("Entering callgetDiscrepancyList : ShipmentKey is" + sShipmentKey);
    			
    			callGetDiscrepancyList(env,sShipmentKey,inDoc,bkitShipment,inShipmentLines);    			
    			}
    		}
    	return inDoc;
    }

    
    /**
     * This method will call service AcademyGetDiscrepancyList 
     * and massage input XMl to include discrepancy with corresponding receipt lines. 
     * @param bkitShipment 
     * @param kitCode 
     * @param parentShipmentKey 
     * @param inShipmentLines 
     */
	private void callGetDiscrepancyList(YFSEnvironment env, String shipmentKey, Document inDoc, boolean bkitShipment, Element inShipmentLines) throws Exception {
		
		log.verbose("Inside method callgetDiscrepancyList : Input to method is" + XMLUtil.getXMLString(inDoc)); 		
		Document inGetDisc = XMLUtil.createDocument(AcademyConstants.SHIPMENT_DISCREPANCY);
		
		inGetDisc.getDocumentElement().setAttribute(AcademyConstants.SHIPMENT_KEY,shipmentKey );			
		log.verbose("Input to service AcademyGetDiscrepancyList is" + XMLUtil.getXMLString(inGetDisc)); 
		
		Document outDocDisc= AcademyUtil.invokeService(env, "AcademyGetDiscrepancyList",inGetDisc);
		env.clearApiTemplate("getShipmentDiscrepancyList");
		log.verbose("Inside callGetDiscrepancy method");
		makeExterOutput(env,inDoc,outDocDisc, bkitShipment,inShipmentLines, shipmentKey);
	}
	
	private String getExtnASNContainer(YFSEnvironment env, String shipmentKey) throws Exception {
		String strPalletID = "";
		log.verbose("Inside method getExtnASNContainer"); 		
		Document inXMLGetContainer = XMLUtil.createDocument("Container");
		
		inXMLGetContainer.getDocumentElement().setAttribute(AcademyConstants.SHIPMENT_KEY,shipmentKey);			
		log.verbose("Input to API getShipmentContainerList is" + XMLUtil.getXMLString(inXMLGetContainer)); 
		
		Document outXMLGetContainer= AcademyUtil.invokeAPI(env, "getShipmentContainerList",inXMLGetContainer);
		log.verbose("Output of API getShipmentContainerList is" + XMLUtil.getXMLString(outXMLGetContainer));
		Element eleContainerList = outXMLGetContainer.getDocumentElement();
		Element eleContainer = (Element)eleContainerList.getElementsByTagName("Container").item(0);
		if(!YFCObject.isVoid(eleContainer)){
			strPalletID =  eleContainer.getAttribute("ContainerNo");
		}
		return strPalletID;
	}

	
	
	private void makeExterOutput(YFSEnvironment env, Document inDoc,
			Document outDocDisc, boolean bkitShipment,Element inShipmentLines, String shipmentKey) {
		if(!YFCObject.isVoid(inDoc)){
			HashMap hsReceiptLine = new HashMap();
			Element eReceiptLines =  XMLUtil.getFirstElementByName(inDoc.getDocumentElement(),AcademyConstants.RECEIPT_LINES);
			if(!YFCObject.isVoid(eReceiptLines) && eReceiptLines.hasChildNodes()){
				log.verbose("Inside Receipt method - Receipts");
				// checkZeroQtyReceipt(eReceiptLines,inShipmentLines,bkitShipment);     /* Removed as part of fix for Issue#1963 */
				log.verbose("Before Sort"+ XMLUtil.getXMLString(eReceiptLines.getOwnerDocument()));
				YFCDocument inDocDoc = YFCDocument.getDocumentFor(inDoc);
				YFCElement eYFCReceiptLines = inDocDoc.getDocumentElement().getChildElement(AcademyConstants.RECEIPT_LINES);
				eYFCReceiptLines.sortChildren(new String[]{AcademyConstants.ATTR_QUANTITY});	
				log.verbose("after Sort"+ XMLUtil.getXMLString(eReceiptLines.getOwnerDocument()));
			
				List nl = XMLUtil.getSubNodeList(eReceiptLines, AcademyConstants.RECEIPT_LINE);
				if(!bkitShipment){
					 for(int i= 0; i < nl.size(); i++){
						Element receiptLine = (Element) nl.get(i);					
						String sItemId = receiptLine.getAttribute(AcademyConstants.ITEM_ID);
						hsReceiptLine.put(sItemId, receiptLine);								
					}	
				}
				else {
					
					List nlShipmentLine = XMLUtil.getSubNodeList(inShipmentLines, AcademyConstants.ELE_SHIPMENT_LINE);
					int n1ShipmentLinesize = nlShipmentLine.size();
					
					 for(int k= 0; k< n1ShipmentLinesize; k++){
						 Element ShipmentLine = (Element) nlShipmentLine.get(k);
						 String skitcode = ShipmentLine.getAttribute(AcademyConstants.KIT_CODE);
						 
						 if(skitcode.equals(AcademyConstants.BUNDLE)){
							 Element ReceiptLineElem = XMLUtil.getFirstElementByName(eReceiptLines, AcademyConstants.RECEIPT_LINE);
							 ReceiptLineElem.setAttribute("IsBundle", "Y");							 						 
							 
							 String sReplaceParent = ShipmentLine.getAttribute(AcademyConstants.ITEM_ID);
							 ReceiptLineElem.setAttribute(AcademyConstants.ITEM_ID, sReplaceParent);
							 hsReceiptLine.put(sReplaceParent, ReceiptLineElem);
						 }
					 }
				} 
			}
			Element elemDiscList = outDocDisc.getDocumentElement();
			if (!YFCObject.isVoid(outDocDisc) && elemDiscList.hasChildNodes()){
				log.verbose("Inside Disc method - Discrepancy");
				
				List nL2 = XMLUtil.getSubNodeList(elemDiscList, AcademyConstants.SHIPMENT_DISCREPANCY);		
				List sdiscItems = new ArrayList();
				
				String sPrimeLineNo = "";
				String sShipmentQty = "";
				String sQty = "";
				String sPalletId = "";
				
				for(int j= 0; j < nL2.size(); j++){
					Element elemDiscLine = (Element) nL2.get(j);
					String sdiscItemId = elemDiscLine.getAttribute(AcademyConstants.ITEM_Id);
					Element elemDummyReceipt = XMLUtil.createElement(inDoc, "ReceiptLine", hsReceiptLine);
					if(!sdiscItems.contains(sdiscItemId)){
						sdiscItems.add(sdiscItemId);
					}
					Element elemReceiptLine = (Element) hsReceiptLine.get(sdiscItemId);
					// start changes for sending message to Exter when there is a discrepancy but no receipt lines
					if (!YFCObject.isVoid(elemReceiptLine)) {
						XMLUtil.importElement(elemReceiptLine, elemDiscLine);
					} 
					
					else {
					log.verbose("No Receipt Line for Discrepancy Item: "+ sdiscItemId );
					List nShipmentLine = XMLUtil.getSubNodeList(inShipmentLines, AcademyConstants.ELE_SHIPMENT_LINE);
					int nShipmentLinesize = nShipmentLine.size();
					
					try{
						sPalletId = getExtnASNContainer(env, shipmentKey);
					}catch (Exception e) {}
					for(int k= 0; k< nShipmentLinesize; k++){
						 Element ShipmentLine = (Element) nShipmentLine.get(k);
						 String sShipmentItem = ShipmentLine.getAttribute(AcademyConstants.ITEM_ID);
						 if(sShipmentItem.equals(sdiscItemId)){
							 sPrimeLineNo = ShipmentLine.getAttribute("PrimeLineNo");
							 sShipmentQty = ShipmentLine.getAttribute("ActualQuantity");
							 sQty = ShipmentLine.getAttribute("ReceivedQuantity");
						 }
					}
					//creating receipt line for the discrepancy 					
					elemDummyReceipt.setAttribute("PalletId", sPalletId);
					elemDummyReceipt.setAttribute("ItemID", sdiscItemId);
					elemDummyReceipt.setAttribute("PrimeLineNo", sPrimeLineNo);
					elemDummyReceipt.setAttribute("Quantity", sQty);
					elemDummyReceipt.setAttribute("OriginalQuantity", sShipmentQty);
					
					log.verbose("ElemDummyReceipt" + XMLUtil.getElementXMLString(elemDummyReceipt));
					
					XMLUtil.importElement(elemDummyReceipt, elemDiscLine);
					XMLUtil.importElement(eReceiptLines, elemDummyReceipt);										
					//End changes for adding dummy receipt line	
					}
				}
				List mShipmentLine = XMLUtil.getSubNodeList(inShipmentLines, AcademyConstants.ELE_SHIPMENT_LINE);
				int mShipmentLinesize = mShipmentLine.size();
				for(int l= 0; l< mShipmentLinesize; l++){
					 Element eleShipmentLine = (Element) mShipmentLine.get(l);
					 String strShipmentItem = eleShipmentLine.getAttribute(AcademyConstants.ITEM_ID);
					 if(!sdiscItems.contains(strShipmentItem)){
						 Element elemReceiptLine = (Element) hsReceiptLine.get(strShipmentItem);
						 Element eleDiscLine = inDoc.createElement("DiscrepancyList");
							// start changes for sending message to Exter when there is a discrepancy but no receipt lines
							if (!YFCObject.isVoid(elemReceiptLine)) {
								XMLUtil.importElement(elemReceiptLine, eleDiscLine);
							} 
							
							else {
							 try{
								sPalletId = getExtnASNContainer(env, shipmentKey);
							 }catch (Exception e) {}
							 Element eleDummyReceipt = XMLUtil.createElement(inDoc, "ReceiptLine", hsReceiptLine);
							 sPrimeLineNo = eleShipmentLine.getAttribute("PrimeLineNo");
							 sShipmentQty = eleShipmentLine.getAttribute("ActualQuantity");
							 sQty = eleShipmentLine.getAttribute("ReceivedQuantity");
							 eleDummyReceipt.setAttribute("PalletId", sPalletId);
							 eleDummyReceipt.setAttribute("ItemID", strShipmentItem);
							 eleDummyReceipt.setAttribute("PrimeLineNo", sPrimeLineNo);
							 eleDummyReceipt.setAttribute("Quantity", sQty);
							 eleDummyReceipt.setAttribute("OriginalQuantity", sShipmentQty);
							 XMLUtil.importElement(eleDummyReceipt, eleDiscLine);
							 XMLUtil.importElement(eReceiptLines, eleDummyReceipt);
							}
					 }	 
				}
				log.verbose("**********Final XML***********: " + XMLUtil.getXMLString(inDoc));	
			}
			else if(YFCObject.isVoid(eReceiptLines) || !eReceiptLines.hasChildNodes()){
				log.verbose("Inside else if method - No Receipt Lines, No Discrepancy");
				List noShipmentLine = XMLUtil.getSubNodeList(inShipmentLines, AcademyConstants.ELE_SHIPMENT_LINE);
				int noShipmentLinesize = noShipmentLine.size();
				String sPrimeLineNo = "";
				String sShipmentQty = "";
				String sQty = "";
				String sPalletId = "";
				for(int k= 0; k< noShipmentLinesize; k++){
					 Element ShipmentLine = (Element) noShipmentLine.get(k);
					 String sShipmentItem = ShipmentLine.getAttribute(AcademyConstants.ITEM_ID);
					 
					 sPrimeLineNo = ShipmentLine.getAttribute("PrimeLineNo");
					 sQty = ShipmentLine.getAttribute("ReceivedQuantity");
					 sShipmentQty = ShipmentLine.getAttribute("ActualQuantity");
					 
					 Element eleDummyReceipt = inDoc.createElement("ReceiptLine");
					 try{
						sPalletId = getExtnASNContainer(env, shipmentKey);
					 }catch (Exception e) {}
					 eleDummyReceipt.setAttribute("PalletId", sPalletId);
					 eleDummyReceipt.setAttribute("ItemID", sShipmentItem);
					 eleDummyReceipt.setAttribute("PrimeLineNo", sPrimeLineNo);
					 eleDummyReceipt.setAttribute("Quantity", sQty);
					 eleDummyReceipt.setAttribute("OriginalQuantity", sShipmentQty);
					 Element eleDiscLine = inDoc.createElement("DiscrepancyList");
					 
					 XMLUtil.importElement(eleDummyReceipt, eleDiscLine);
					 XMLUtil.importElement(eReceiptLines, eleDummyReceipt);
					 }
				log.verbose("Final XML is : " + XMLUtil.getXMLString(inDoc));
			}
			else{
				log.verbose("Inside else method - Partial Receipt Lines, No Discrepancy");
				String sPrimeLineNo = "";
				String sShipmentQty = "";
				String sQty = "";
				String sPalletId = "";
				List mShipmentLine = XMLUtil.getSubNodeList(inShipmentLines, AcademyConstants.ELE_SHIPMENT_LINE);
				int mShipmentLinesize = mShipmentLine.size();
				for(int l= 0; l< mShipmentLinesize; l++){
					 Element eleShipmentLine = (Element) mShipmentLine.get(l);
					 String strShipmentItem = eleShipmentLine.getAttribute(AcademyConstants.ITEM_ID);

					 Element elemReceiptLine = (Element) hsReceiptLine.get(strShipmentItem);
					 Element eleDiscLine = inDoc.createElement("DiscrepancyList");
					 if (!YFCObject.isVoid(elemReceiptLine)) {
						XMLUtil.importElement(elemReceiptLine, eleDiscLine);
					 } 
					
					 else {
					 try{
						sPalletId = getExtnASNContainer(env, shipmentKey);
					 }catch (Exception e) {}
					 Element eleDummyReceipt = XMLUtil.createElement(inDoc, "ReceiptLine", hsReceiptLine);
					 sPrimeLineNo = eleShipmentLine.getAttribute("PrimeLineNo");
					 sShipmentQty = eleShipmentLine.getAttribute("ActualQuantity");
					 sQty = eleShipmentLine.getAttribute("ReceivedQuantity");
					 eleDummyReceipt.setAttribute("PalletId", sPalletId);
					 eleDummyReceipt.setAttribute("ItemID", strShipmentItem);
					 eleDummyReceipt.setAttribute("PrimeLineNo", sPrimeLineNo);
					 eleDummyReceipt.setAttribute("Quantity", sQty);
					 eleDummyReceipt.setAttribute("OriginalQuantity", sShipmentQty);
					 XMLUtil.importElement(eleDummyReceipt, eleDiscLine);
					 XMLUtil.importElement(eReceiptLines, eleDummyReceipt);
					}

				}
			}
		}
			
		}

	/* Removed as part of fix for Issue#1963 */
	/*
	private void checkZeroQtyReceipt(Element receiptLines,
			Element inShipmentLines, boolean bkitShipment) {
		List nL1 = XMLUtil.getSubNodeList(receiptLines, AcademyConstants.RECEIPT_LINE);
		List nL2 = XMLUtil.getSubNodeList(inShipmentLines, AcademyConstants.ELE_SHIPMENT_LINE);
		
		int nL1size = nL1.size();
		int nL2size = nL2.size();
		double dRecievedQty;
		String sisPickable="";
		double dQty;
		if(!bkitShipment && (nL1size!=nL2size) ){
			for(int j= 0; j < nL2size; j++){
				Element elemShipmentLine = (Element) nL2.get(j);
				dRecievedQty =  Double.valueOf(elemShipmentLine.getAttribute("ReceivedQuantity"));
				dQty =  Double.valueOf(elemShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY));
				
				sisPickable = elemShipmentLine.getAttribute("IsPickable");
				
				if ((dRecievedQty==0.00) && (dQty !=0.00) && (("Y").equals(sisPickable)) ){
					Element receiptlineElem = XMLUtil.
					createElement(receiptLines.getOwnerDocument(), AcademyConstants.RECEIPT_LINE, null);
					
					Element origreceiptlineElem = XMLUtil.
					getFirstElementByName(receiptLines, AcademyConstants.RECEIPT_LINE);
					receiptlineElem = origreceiptlineElem;
				
					receiptlineElem.setAttribute(AcademyConstants.ITEM_ID,elemShipmentLine.getAttribute(AcademyConstants.ITEM_ID) );
					receiptlineElem.setAttribute(AcademyConstants.ATTR_QUANTITY,"0.00" );
					receiptlineElem.setAttribute("OriginalQuantity",elemShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY) );
					
					XMLUtil.appendChild(receiptLines, receiptlineElem);				
					
				}
			}
			
		} 
		if (bkitShipment && ((nL1size+1)!= nL2size) ){
			for(int j= 0; j < nL2size; j++){
				Element elemShipmentLine = (Element) nL2.get(j);
				dRecievedQty =  Double.valueOf(elemShipmentLine.getAttribute("ReceivedQuantity"));
				dQty =  Double.valueOf(elemShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY));
				
				sisPickable = elemShipmentLine.getAttribute("IsPickable");
				
				if ((dRecievedQty==0.00) && (dQty !=0.00) && (("Y").equals(sisPickable)) ){
					//Element receiptlineElem = XMLUtil.createElement(receiptLines.getOwnerDocument(), AcademyConstants.RECEIPT_LINE, null);
					
					Element receiptlineElem = XMLUtil.getFirstElementByName(receiptLines, AcademyConstants.RECEIPT_LINE);
					receiptlineElem.setAttribute(AcademyConstants.ITEM_ID,elemShipmentLine.getAttribute(AcademyConstants.ITEM_ID) );
					receiptlineElem.setAttribute(AcademyConstants.ATTR_QUANTITY,"0.00" );
					receiptlineElem.setAttribute("OriginalQuantity",elemShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY) );
					
					XMLUtil.appendChild(receiptLines, receiptlineElem);				
					
				}
			}
		}
	}
	*/
}
