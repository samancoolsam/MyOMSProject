package com.academy.ecommerce.sterling.shipment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSExtnInputLineChargesShipment;
import com.yantra.yfs.japi.YFSExtnLineChargeStruct;
import com.yantra.yfs.japi.YFSExtnOutputLineChargesShipment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSGetLineChargesForShipmentUE;

public class AcademySalesOrderLineChargesForShipmentUEImpl implements
        YFSGetLineChargesForShipmentUE {

    private static final YFCLogCategory log = YFCLogCategory.instance(AcademySalesOrderLineChargesForShipmentUEImpl.class);
    /*
     * (non-Javadoc)
     * 
     * @seecom.yantra.yfs.japi.ue.YFSGetLineChargesForShipmentUE#
     * getLineChargesForShipment(com.yantra.yfs.japi.YFSEnvironment,
     * com.yantra.yfs.japi.YFSExtnInputLineChargesShipment)
     */
    public YFSExtnOutputLineChargesShipment getLineChargesForShipment(
            YFSEnvironment env, YFSExtnInputLineChargesShipment inputLineCharge)
            throws YFSUserExitException {
        // TODO Auto-generated method stub
        // Get the Shipment Key
        // Get shipment details and then make the Quote call to Vertex
        YFSExtnOutputLineChargesShipment outStruct = new YFSExtnOutputLineChargesShipment();
                
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        log.beginTimer(" Begining of AcademySalesOrderLineChargesForShipmentUEImpl -> getLineChargesForShipment()Api");
        log.verbose(" Begining of AcademySalesOrderLineChargesForShipmentUEImpl -> getLineChargesForShipment()Api");
        try 
        {
            String sOrderHdrKey = inputLineCharge.orderHeaderKey;
            if(sOrderHdrKey != null && !sOrderHdrKey.equals(""))
            {
            /*
                Document docOrderDet = XMLUtil.createDocument("OrderLineDetail");
                log.verbose("OrderHeaderKey" +  sOrderHdrKey);
                docOrderDet.getDocumentElement().setAttribute("OrderHeaderKey", sOrderHdrKey);
                docOrderDet.getDocumentElement().setAttribute("OrderLineKey", inputLineCharge.orderLineKey);
                log.verbose("OrderLineKey" +    inputLineCharge.orderLineKey);
                docOrderDet.getDocumentElement().setAttribute("DocumentType", "0001");
                env.setApiTemplate("getOrderLineDetails", "global/template/api/getOrderLineDetails.CallToVertex.xml"); // ??
                Document inXMLNew = AcademyUtil.invokeAPI(env, "getOrderLineDetails", docOrderDet);
                env.clearApiTemplate("getOrderLineDetails");

                NodeList lineChrgesList = inXMLNew.getElementsByTagName(AcademyConstants.ELE_LINE_CHARGE);
                String overAllQty = inXMLNew.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDERED_QTY);
            */
                
                List<YFSExtnLineChargeStruct> chargesList = inputLineCharge.orderLineCharges;
                List<YFSExtnLineChargeStruct> proratedChargesList = new ArrayList<YFSExtnLineChargeStruct>();
                Iterator<YFSExtnLineChargeStruct> lineChargeItr = chargesList.iterator(); 
                while(lineChargeItr.hasNext()) {
                    YFSExtnLineChargeStruct tempCharge = lineChargeItr.next();
                    YFSExtnLineChargeStruct proratedChargesStrct = new YFSExtnLineChargeStruct();
                    proratedChargesStrct.chargeName = tempCharge.chargeName;
                    log.verbose("chargeName" +  proratedChargesStrct.chargeName);
                    proratedChargesStrct.chargeCategory = tempCharge.chargeCategory;
                    log.verbose("shipmentQty" + inputLineCharge.shipmentQty);
                    // Start Fix as part of STL-245
                    String sShipKey = inputLineCharge.shipmentKey;
                    String currReference= tempCharge.reference;
                    proratedChargesStrct.reference = tempCharge.reference;
                    
                    log.verbose("************chargePerLine from proratedChargesStrct" +   proratedChargesStrct.chargePerLine);
                    log.verbose("************chargePerLine from tempCharge" +   proratedChargesStrct.chargePerLine);
                    log.verbose("************chargePerUnit from tempCharge" +   proratedChargesStrct.chargePerUnit);
                    log.verbose("************chargeAmount from tempCharge" +   proratedChargesStrct.chargeAmount);
                    
                    if(currReference.equalsIgnoreCase("Y")){
                        /*
                        log.verbose("************chargePerLine from proratedChargesStrct" +   proratedChargesStrct.chargePerLine);
                        Document getOrderInvoiceListInDoc = XMLUtil.createDocument("OrderInvoice");
                        getOrderInvoiceListInDoc.getDocumentElement().setAttribute("OrderHeaderKey", sOrderHdrKey);
                        //start: STL-397 fix                       
                        Document getOrderInvoiceListOPTemplate = YFCDocument.parse("<OrderInvoiceList TotalNumberOfRecords=\"\"><OrderInvoice DocumentType=\"\" EnterpriseCode=\"\" InvoiceNo=\"\" InvoiceType=\"\" OrderHeaderKey=\"\" OrderInvoiceKey=\"\" OrderNo=\"\" ShipNode=\"\"  ShipmentKey=\"\" ShipmentNo=\"\" Status=\"\" TotalAmount=\"\" TotalTax=\"\" ><LineDetails TotalNumberOfRecords=\"\"><LineDetail ItemID=\"\" PrimeLineNo=\"\" ProductClass=\"\" ShippedQty=\"\" SubLineNo=\"\" ><LineChargeList TotalNumberOfRecords=\"\"><LineCharge ChargeAmount=\"\" ChargeCategory=\"\" ChargeName=\"\" ChargePerLine=\"\" ChargePerUnit=\"\" /></LineChargeList></LineDetail></LineDetails></OrderInvoice></OrderInvoiceList>").getDocument();
                        log.verbose("AcademySalesOrderLineChargesForShipmentUEImpl: getLineChargesForShipment: getOrderInvoiceListDoc: " + XMLUtil.getXMLString(getOrderInvoiceListOPTemplate));
                        env.setApiTemplate(AcademyConstants.API_GET_ORDER_INVOICE_LIST, getOrderInvoiceListOPTemplate);
                        Document getOrderInvoiceListOutDoc =  AcademyUtil.invokeAPI(env,
                                AcademyConstants.API_GET_ORDER_INVOICE_LIST, getOrderInvoiceListInDoc);
                        log.verbose("getOrderInvoiceList outDoc is : " +XMLUtil.getXMLString(getOrderInvoiceListOutDoc));
                        env.clearApiTemplate(AcademyConstants.API_GET_ORDER_INVOICE_LIST);
                        //end: STL-397 fix                       

                        
                        NodeList tmpList = (NodeList) getOrderInvoiceListOutDoc.getDocumentElement();
                        NodeList OrderInvoiceList = getOrderInvoiceListOutDoc.getDocumentElement().getElementsByTagName("OrderInvoice");
                        //start: STL-397 fix 
                        NodeList LineChargeList = getOrderInvoiceListOutDoc.getDocumentElement().getElementsByTagName("LineCharge");
                        int OrderInvoiceListLength = OrderInvoiceList.getLength();
                        boolean isRoundOffAlreadyAdded = false;
                        boolean isShipKeyMatches = false;
                        isRoundOffAlreadyAdded = compareChargeAmtWithCPU(LineChargeList);
                        log.verbose("isRoundOffAlreadyAdded = " +isShipKeyMatches);
                        
                        NodeList LineChargeMatchedList = null;
                        //Check whether the ShipmentKey from the input structure coming to the UE matches with the 
                        // Pro_forma invoice and return true/false
                        for (int count = 0; count < OrderInvoiceListLength; count++) {
                            Element OrderInvoiceElement = (Element) OrderInvoiceList.item(count);
                            String sInvoiceType = OrderInvoiceElement.getAttribute("InvoiceType");
                            if(!YFCObject.isVoid(sShipKey) && sInvoiceType.equals("PRO_FORMA") && OrderInvoiceElement.getAttribute("ShipmentKey").equals(sShipKey)){
                                LineChargeMatchedList = OrderInvoiceElement.getElementsByTagName("LineCharge");
                                isShipKeyMatches = compareChargeAmtWithCPU(LineChargeMatchedList);
                                log.verbose("isShipKeyMatches = " +isShipKeyMatches);
                            }
                           
                        }
                        //condition for checking for 1st Proforma or ShipmentInvoice to add the ModValue of ChargePerLine
                        // to the respective Invoice Charges
                        if((tmpList.getLength() == 0) ||  ((!YFCObject.isNull(sShipKey) && !YFCObject.isVoid(sShipKey)) &&  isShipKeyMatches == true) ){
                            proratedChargesStrct.chargePerLine =tempCharge.chargePerLine;
                            log.verbose("inside if check :::: ");
                        }
                        
                        if((tmpList.getLength() == 1) && !YFCObject.isVoid(env.getTxnObject("CallChargesUEFromPrintFlow")) && env.getTxnObject("CallChargesUEFromPrintFlow").equals("Y")  ){
                            proratedChargesStrct.chargePerLine =tempCharge.chargePerLine;
                            log.verbose("inside if check again :::: ");
                        }else if(tmpList.getLength()>1 &&  isRoundOffAlreadyAdded == false){
                            proratedChargesStrct.chargePerLine =tempCharge.chargePerLine;
                            log.verbose("last inside if check again :::: ");
                        }
                        //End : STL 397 fix
*/						
                        //Start : STL 397 fix :: for line_charge_proration
                        if(inputLineCharge.bLastInvoiceForOrderLine == true){
                            proratedChargesStrct.chargePerLine =tempCharge.chargePerLine;
                            log.verbose("adding the mod value of LineCharge for the last invoice for the orderLine");
                        }
                        //End : STL 397 fix :: for line_charge_proration
                            
                            
                        proratedChargesStrct.chargePerUnit =tempCharge.chargePerUnit;
                        
                        log.verbose("chargePerLine and chargePerUnit are ::::  " + tempCharge.chargePerLine + " and " + tempCharge.chargePerUnit);
                        //End of Fix as part of STL-245
                    
                    }
                    else {
                        
//                      SFS: Start Defect STL-201 Fix (Split shipment shipping Charge)
                        log.verbose("SplitShipment ISSue"+"shipmentQty" +inputLineCharge.shipmentQty+"chargeName" + proratedChargesStrct.chargeName+"OrderLineQty"+inputLineCharge.orderLineOrderedQty);
                        proratedChargesStrct.chargePerLine = (inputLineCharge.shipmentQty) * 
                         (Double.valueOf(twoDForm.format(tempCharge.chargePerLine)) / inputLineCharge.orderLineOrderedQty);                 
                        log.verbose("chargePerLine" +   proratedChargesStrct.chargePerLine);
                        //proratedChargesStrct.chargePerLine=tempCharge.chargePerLine;
                        //SFS: End Defect STL-201 Fix
                    }
                    //End Change
                    proratedChargesList.add(proratedChargesStrct);
                }
                
                Iterator<YFSExtnLineChargeStruct> lineChargeItr2 =proratedChargesList.iterator();
                log.verbose("before the iteration of proratedChargesList");
                while(lineChargeItr2.hasNext()){
                    log.verbose("testing");
                    YFSExtnLineChargeStruct test = lineChargeItr2.next();
                    log.verbose(test.chargeName);
                    log.verbose(test.chargeCategory);
                    log.verbose("chargeAmount"+test.chargeAmount);
                    log.verbose("chargePerLine"+test.chargePerLine);
                    log.verbose("chargePerUnit"+test.chargePerUnit);
                    
                    
                }
                //log.verbose("newLineCharges" +    outStruct.newLineCharges.toString());
        
                
                //log.verbose("proratedChargesList" +   proratedChargesList.to);
                outStruct.newLineCharges = proratedChargesList;
                log.verbose("newLineCharges" +  outStruct.newLineCharges.size());
                
                Iterator<YFSExtnLineChargeStruct> lineChargeItr1 =outStruct.newLineCharges.iterator();
                log.verbose("before the iteration");
                while(lineChargeItr1.hasNext()){
                    log.verbose("testing");
                    YFSExtnLineChargeStruct test = lineChargeItr1.next();
                    log.verbose(test.chargeName);
                    log.verbose(test.chargeCategory);
                    log.verbose("chargeAmount"+test.chargeAmount);
                    log.verbose("chargePerLine"+test.chargePerLine);
                    log.verbose("chargePerUnit"+test.chargePerUnit);
                    
                    
                }
                log.verbose("newLineCharges" +  outStruct.newLineCharges.toString());
            }
            
            if (env.getTxnObject("ShipmentInvoiceCall") == null) {
                // get the Shipment Key
                String sShipmentKey = inputLineCharge.shipmentKey;
                if (sShipmentKey != null && !sShipmentKey.equals("")) {
                    
                    Document docShipDet = XMLUtil.createDocument("Shipment");
                    docShipDet.getDocumentElement().setAttribute("ShipmentKey", sShipmentKey);
                    
                    env.setApiTemplate("getShipmentList", "global/template/api/getShipmentDetails.CallToVertex.xml");                   
                    Document inXML = AcademyUtil.invokeAPI(env, "getShipmentList", docShipDet);
                    env.clearApiTemplate("getShipmentList");
                    
                    inXML = XMLUtil.getDocumentForElement((Element)XMLUtil.getFirstElementByName(inXML.getDocumentElement(), AcademyConstants.ELE_SHIPMENT));
                    // convert the input to Vertex Quote Call Request xml.
                    inXML.getDocumentElement().setAttribute("CallType", "InvoiceCall");
                    //CR - Vertex changes; Set Sterling Function name 
                    inXML.getDocumentElement().setAttribute("TranType", "CreateShipmentInvoice");
                    
                    //OMNI-20856 : Tax should not calculate on egift as a product purchase - Start
                    /*
                    Document vertexInvoiceCallReq = AcademyUtil.invokeService(env, "AcademyChangeShipmentToInvoiceCallRequest", inXML);
                    Document vertexInvoiceCallResp = AcademyUtil.invokeService(env, "AcademyVertexInvoiceCallRequest", vertexInvoiceCallReq);                    
                    env.setTxnObject("ShipmentInvoiceCall", vertexInvoiceCallResp);
                    */                    
                    String sShipmentType = inXML.getDocumentElement().getAttribute("ShipmentType");
                    log.verbose("ShipmentType :: " +  sShipmentType);
        			if(sShipmentType != null && !sShipmentType.equals("") && sShipmentType.equals("EGC")) {
        				log.verbose("Ignoring Vertex call for EGC Shipemnts....");
        			} else {
                        log.verbose("Vertex call starting....");
        				Document vertexInvoiceCallReq = AcademyUtil.invokeService(env, "AcademyChangeShipmentToInvoiceCallRequest", inXML);
                        Document vertexInvoiceCallResp = AcademyUtil.invokeService( env, "AcademyVertexInvoiceCallRequest", vertexInvoiceCallReq);                        
                        env.setTxnObject("ShipmentInvoiceCall", vertexInvoiceCallResp);
                        log.verbose("Vertex call ending....");
        			}
        			//OMNI-20856 : Tax should not calculate on egift as a product purchase - End
                    
                    if (env.getTxnObject("ShipmentKey") == null) {
                        env.setTxnObject("ShipmentKey", sShipmentKey);
                    }
                }
            }
            log.endTimer(" End of AcademySalesOrderLineChargesForShipmentUEImpl -> getLineChargesForShipment()Api");
        } catch (Exception e) {
            throw getYFSUserExceptionWithTrace(e);
        }           
        
        return outStruct;
    }
    
    //start : part of  STL397 fix
    /**
     * Method compares ChargeAmount and ChargePerUnit From LineChargeList
     * and returns true if Mod(ChargeAmount>ChargePerUnit)
     * 
     * @param lineChargeList
     * @return
     *//*
    private boolean compareChargeAmtWithCPU(NodeList lineChargeList) {
        // TODO Auto-generated method stub
        if(lineChargeList.getLength()>0){
            for (int count = 0; count < lineChargeList.getLength(); count++) {
            Element LineChargeEle = (Element) lineChargeList.item(count);
            //Element LineChargeEle= (Element) LineChargeEle.getElementsByTagName("LineCharge").item(0);
            Double strChargeAmount = Double.parseDouble(LineChargeEle.getAttribute("ChargeAmount"));
            Double strCPU = Double.parseDouble(LineChargeEle.getAttribute("ChargePerUnit"));
            if( (strChargeAmount % strCPU) > 0){
                Boolean isRoundOffAlreadyAdded = true;
                return true;
            } 
           }
        }
        return false;
    }*/
    //start : end of  STL397 fix

    /**
     * Method wraps Exception object to YFSUserExitException object
     * 
     * @param e
     * @return YFSUserExitException
     */
    private static YFSUserExitException getYFSUserExceptionWithTrace(Exception e) {
        YFSUserExitException yfsUEException = new YFSUserExitException();
        yfsUEException.setStackTrace(e.getStackTrace());
        return yfsUEException;
    }

}