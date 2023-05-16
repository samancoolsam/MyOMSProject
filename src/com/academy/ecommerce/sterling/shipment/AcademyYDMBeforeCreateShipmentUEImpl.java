/**
 * 
 */
package com.academy.ecommerce.sterling.shipment;

import java.text.DecimalFormat;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;

/**
 * @author dsharma
 * 
 */
public class AcademyYDMBeforeCreateShipmentUEImpl implements YIFCustomApi {

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.yantra.ydm.japi.ue.YDMBeforeCreateShipment#beforeCreateShipment(com
     * .yantra.yfs.japi.YFSEnvironment, org.w3c.dom.Document)
     */
    private static YFCLogCategory log = YFCLogCategory
    .instance(AcademyYDMBeforeCreateShipmentUEImpl.class);

    public Document beforeCreateShipment(YFSEnvironment env, Document inXML)
            throws Exception {
        
        String sOrdRelKey =null;
        Element eleShipLine=null;
        //Added by Anushri
        String sOrdHeaderKey=null;
        log.beginTimer(" Begin of AcademyYDMBeforeCreateShipmentUEImpl->beforeCreateShipment ()");
    


    //DSVCHANGE
            if (inXML.getDocumentElement().getAttribute("DocumentType").equals(
                    "0001") || inXML.getDocumentElement().getAttribute("DocumentType").equals(
                    "0005")) {
                    
                    Document docGetReleaseDet = XMLUtil
                        .createDocument("OrderReleaseDetail");
    //DSVCHANGE
            log.verbose("Inside outermost if condition");
    //DSVCHANGE
            if (env.getTxnObject("VendorNetShipConfirmationMessage") == null) {
                    
                    log.verbose("inside if check for VendorNetShipConfirmationMessage:::::");
                    env.setTxnObject("VendorNetShipConfirmationMessage", inXML);
                    log.verbose("VendorNetShipConfirmationMessage Txn Obj is set");
                }
    //DSVCHANGE
            //OMNI-93544 - Added ISSOFEnabled for multi releases
            log.verbose("ISSOFEnabled ::: "  + env.getTxnObject("ISSOFEnabled"));
            if (env.getTxnObject("ShipmentQuoteCall") == null || 
            		!YFCObject.isVoid(env.getTxnObject("ISSOFEnabled"))) {
                // call getOrderReleaseDetails
                Element eleOrderRelease = (Element) inXML
                        .getElementsByTagName("OrderRelease").item(0);
                if(!YFCObject.isVoid(eleOrderRelease)){
                sOrdRelKey = eleOrderRelease
                        .getAttribute("OrderReleaseKey");
                sOrdHeaderKey= eleOrderRelease.getAttribute("OrderHeaderKey");
                }
                /* Fix for NullPointer Issue in ConsolidateToShipment agent */
                if(YFCObject.isVoid(sOrdRelKey))
                {
                    eleShipLine=(Element) inXML
                    .getElementsByTagName("ShipmentLine").item(0);
                    if(!YFCObject.isVoid(eleShipLine)){
                    sOrdRelKey=eleShipLine.getAttribute("OrderReleaseKey");
                    //Added by Anushri
                    //DSVCHANGE : Added by Shamrose
                    if(inXML.getDocumentElement().getAttribute("DocumentType").equals(
                    "0005")){
                        sOrdHeaderKey=inXML.getDocumentElement().getAttribute("OrderHeaderKey");
                        log.verbose("OrderHeaderKey fetched from root ele is : " + sOrdHeaderKey);
                    }
                    else
                    {
                        sOrdHeaderKey=eleShipLine.getAttribute("OrderHeaderKey");
                    }
                    //END DSVCHANGE : Added by Shamrose
                    }
                }
                
                /* End Fix */
                
                if(YFCObject.isVoid(sOrdRelKey))
                {
                    throw new YFSUserExitException(" Order release key in beforeCreate Shipment UE implementation is null...............");
                }
                
                docGetReleaseDet.getDocumentElement().setAttribute(
                        "OrderReleaseKey", sOrdRelKey);
                
                env.setApiTemplate("getOrderReleaseDetails", "global/template/api/getOrderReleaseDetails.CallToVertex.xml");
                docGetReleaseDet = AcademyUtil.invokeAPI(env,
                        "getOrderReleaseDetails", docGetReleaseDet);
                env.clearApiTemplate("getOrderReleaseDetails");
                
                
                // Start Fix as part of STL-245
                                
                Document docGetOrderListIP = XMLUtil.createDocument("Order");
                docGetOrderListIP.getDocumentElement().setAttribute("OrderHeaderKey", sOrdHeaderKey);
                /* Start - Changes made for PERF-202 to fix YFS10137 - Order Modification Rule */
                env.setApiTemplate("getOrderList", "global/template/api/getOrderList.BeforeCreateShipmentUE.xml");
                boolean changeOrderRequired=false;
                /* End - Changes made for PERF-202 to fix YFS10137 - Order Modification Rule */
                Document docGetOrderListOP = AcademyUtil.invokeAPI(env,
                        "getOrderList", docGetOrderListIP);
                env.clearApiTemplate("getOrderList");           
                log.verbose("GetOrderList OP Doc"+XMLUtil.getXMLString(docGetOrderListOP));
                Document docChangeOrderIP = XMLUtil.createDocument("Order");
                Element changeOrderElm = docChangeOrderIP.getDocumentElement();
                changeOrderElm.setAttribute("Action", "MODIFY");
                Element OrderLinesElm = docChangeOrderIP.createElement("OrderLines");
                NodeList nlOrderLines = XMLUtil.getNodeList(docGetOrderListOP.getDocumentElement(),"/OrderList/Order/OrderLines/OrderLine");
                DecimalFormat twoDForm = new DecimalFormat("#.000"); 
                for(int i = 0; i < nlOrderLines.getLength(); i++)
                {
                    Element currentOrderLineElm = (Element) nlOrderLines.item(i);
					if(inXML.getDocumentElement().getAttribute("DocumentType").equals(
                    "0005")){
                        sOrdHeaderKey = currentOrderLineElm.getAttribute("ChainedFromOrderHeaderKey"); 
                        log.verbose("ChainedFromOrderHeaderKey retrieved : " + sOrdHeaderKey);
                    }
					String qty= currentOrderLineElm.getAttribute(AcademyConstants.ATTR_ORDERED_QTY);
                    String currentOrderLineKey=currentOrderLineElm.getAttribute("OrderLineKey");
					if(inXML.getDocumentElement().getAttribute("DocumentType").equals(
                    "0005")){
                        currentOrderLineKey = currentOrderLineElm.getAttribute("ChainedFromOrderLineKey");
                        log.verbose("ChainedFromOrderLineKey retrieved : " + currentOrderLineKey);
                    }
					Element OrderLineElm = docChangeOrderIP.createElement("OrderLine");
                    OrderLineElm.setAttribute("OrderLineKey", currentOrderLineKey);
                    OrderLineElm.setAttribute("Action", "MODIFY");
                    Element lineChargesElm = docChangeOrderIP.createElement("LineCharges");
                    NodeList nlLineCharge= currentOrderLineElm.getElementsByTagName(AcademyConstants.ELE_LINE_CHARGE);
                    for(int j = 0; j < nlLineCharge.getLength(); j++)
                    {
                        Element lineChargeElm = docChangeOrderIP.createElement("LineCharge");
                        Element currentLineChargeElm = (Element) nlLineCharge.item(j);
                        String currentRef = currentLineChargeElm.getAttribute(AcademyConstants.ATTR_REFERENCE);
                        if(!currentRef.equalsIgnoreCase("Y"))
                        {
                        //Start fix STL-397 : negative chargeperline issue
                        Double newCPU=Math.floor((Double.valueOf(currentLineChargeElm.getAttribute(AcademyConstants.ATTR_CHARGES_PER_LINE)))/Double.valueOf(qty)*100)/100 ;
                        //End fix  STL-397: negative chargeperline issue            
                        
                        Double newMod=(Double.valueOf(currentLineChargeElm.getAttribute(AcademyConstants.ATTR_CHARGES_PER_LINE)) )-(newCPU * Double.valueOf(qty));
                        lineChargeElm.setAttribute(AcademyConstants.ATTR_CHARGES_PER_LINE, String.valueOf(newMod));
                        lineChargeElm.setAttribute(AcademyConstants.ATTR_CHARGES_PER_UNIT, String.valueOf(newCPU));
                        lineChargeElm.setAttribute(AcademyConstants.ATTR_REFERENCE, AcademyConstants.STR_YES);
                        lineChargeElm.setAttribute(AcademyConstants.ATTR_CHARGE_NAME, currentLineChargeElm.getAttribute(AcademyConstants.ATTR_CHARGE_NAME));
                        lineChargeElm.setAttribute(AcademyConstants.ATTR_CHARGE_CATEGORY, currentLineChargeElm.getAttribute(AcademyConstants.ATTR_CHARGE_CATEGORY));
                        lineChargesElm.appendChild(lineChargeElm);
                        OrderLineElm.appendChild(lineChargesElm);
                        OrderLinesElm.appendChild(OrderLineElm);
                        changeOrderElm.appendChild(OrderLinesElm);
                        changeOrderRequired=true;
                        }
                    }
                }
                
                if (changeOrderRequired) {
                	changeOrderElm.setAttribute("OrderHeaderKey", sOrdHeaderKey); 
                    
                    log.verbose("Change Order Doc"+XMLUtil.getXMLString(docChangeOrderIP));
                    AcademyUtil.invokeAPI(env,
                            "changeOrder", docChangeOrderIP);
                }
                
                //End of Fix as part of STL - 245 
                
                // check for matching lines
                // Start - Fix for # 4131 - as part of R026H 
                
                //DSVCHANGE
            NodeList lstShipmentLines = null;
                    //To add ShipmentLine Elements from path Shipment/ShipmentLines only
                    if(inXML.getDocumentElement().getAttribute("DocumentType").equals(
                            "0005")){
                        lstShipmentLines = XPathUtil.getNodeList(inXML.getDocumentElement(), "ShipmentLines/ShipmentLine");
                    }
                    else{
                        lstShipmentLines = inXML.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
                    }
                //DSVCHANGE
                    
                NodeList lstReleaseLines = docGetReleaseDet.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
                
                            
                log.verbose("No of new Shipment Lines are: "+lstShipmentLines.getLength()+" No of Order Lines in Release are: "+lstReleaseLines.getLength());
                if(lstShipmentLines.getLength() != lstReleaseLines.getLength()){
                    // Not match with Released Lines
                    for(int i=0; i<lstReleaseLines.getLength(); i++){
                        Element eleOrderLine = (Element)lstReleaseLines.item(i);
                        String orderLineKey = eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
                        Node eleRelOrderLine = XPathUtil.getNode(inXML.getDocumentElement(), "ShipmentLines/ShipmentLine[@OrderLineKey='"+orderLineKey+"']");
                        if(eleRelOrderLine == null){
                            log.verbose("The order line : "+orderLineKey +" is not in the Shipment. Therefore, remove from vertex process ..");
                            eleOrderLine.getParentNode().removeChild(eleOrderLine);
                            i--;
                        }                               
                    }
                }
                // End # 4131
                // convert the input to Vertex Quote Call Request xml.
                docGetReleaseDet.getDocumentElement().setAttribute("CallType", "QuoteCall");
                //CR - Vertex Changes; Set Sterling Function name
                docGetReleaseDet.getDocumentElement().setAttribute("TranType", "CreateShipment");
                Document vertexQuoteCallReq = AcademyUtil.invokeService(
                        env, "AcademyChangeOrderRelToQuoteCallRequest",
                        docGetReleaseDet);
                
                log.verbose("After calling AcademyChangeOrderRelToQuoteCallRequest, vertexQuoteCallReq Doc is \n: " +  XMLUtil.getXMLString(vertexQuoteCallReq) );
                Document vertexQuoteCallResp = AcademyUtil.invokeService(
                        env, "AcademyVertexQuoteCallRequest",
                        vertexQuoteCallReq);
                log.verbose("After calling AcademyVertexQuoteCallRequest, vertexQuoteCallResp Doc is \n: " +  XMLUtil.getXMLString(vertexQuoteCallResp) );
                env.setTxnObject("ShipmentQuoteCall", vertexQuoteCallResp);
                
                //BR2-R008-Gift Message CR - Begin
                
                
                Element eleOrderLine = (Element) lstReleaseLines.item(0);
                //NodeList lstinst = nodeOrderLine.getElementsByTagName("Instruction");
                try{
                NodeList giftInst = XPathUtil.getNodeList(eleOrderLine, "Instructions/Instruction[@InstructionType='GIFT']");
                
                if(giftInst != null && giftInst.getLength()>0){         
                    Element instructions = inXML.createElement("Instructions");
                    inXML.getDocumentElement().appendChild(instructions);
                    for(int indx=0;indx<giftInst.getLength(); indx++){
                        Element eleSrcInst = (Element)giftInst.item(indx);
                        Element instruction =inXML.createElement("Instruction");
                        instructions.appendChild(instruction);              
                        XMLUtil.copyElement(inXML, eleSrcInst, instruction);
                        instruction.setAttribute("SequenceNo", String.valueOf(indx+1));
                    }
                }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // upgrade - downgrade code begin
            log.verbose("docGetReleaseDet - " +XMLUtil.getXMLString(docGetReleaseDet));
            Element eleOrderLine = (Element) docGetReleaseDet.getElementsByTagName("OrderLine").item(0);
            log.verbose("eleOrderLine - " +XMLUtil.getElementXMLString(eleOrderLine));
            
            
            String dtExpectedShipmentDate = inXML.getDocumentElement().getAttribute("ExpectedShipmentDate");
            Element eleAdditionalDates = inXML.createElement("AdditionalDates");
            inXML.getDocumentElement().appendChild(eleAdditionalDates);
            Element eleAdditionalDate1 = inXML.createElement("AdditionalDate");
            eleAdditionalDates.appendChild(eleAdditionalDate1);
            eleAdditionalDate1.setAttribute("ExpectedDate", dtExpectedShipmentDate);
            eleAdditionalDate1.setAttribute("DateTypeId", "ACADEMY_SHIPMENT_DATE");
            
            NodeList orderDate = eleOrderLine.getElementsByTagName("OrderDate");
            for(int i=0; i<orderDate.getLength(); i++)
            {
                Element eleOrderDate = (Element)orderDate.item(i);
                log.verbose("eleOrderDate - " +XMLUtil.getElementXMLString(eleOrderDate));
                if("ACADEMY_DELIVERY_DATE".equals(eleOrderDate.getAttribute("DateTypeId")))
                {
                    log.verbose("***inside if***");
                    String strCommittedDate = eleOrderDate.getAttribute("CommittedDate");                   
                    Element eleAdditionalDate = inXML.createElement("AdditionalDate");
                    eleAdditionalDates.appendChild(eleAdditionalDate);
                    eleAdditionalDate.setAttribute("RequestedDate", strCommittedDate);
                    eleAdditionalDate.setAttribute("DateTypeId", "ACADEMY_DELIVERY_DATE");
                    log.verbose("***inXML***" +XMLUtil.getXMLString(inXML));
                }
            }
            /*Element eleRoot = inXML.getDocumentElement();
            Element eleExtn = inXML.createElement("Extn");
            eleExtn.setAttribute("ExtnOriginalShipmentScac", eleRoot.getAttribute("SCAC"));
            eleExtn.setAttribute("ExtnOriginalShipmentLos", eleRoot.getAttribute("CarrierServiceCode"));
            eleRoot.appendChild(eleExtn);*/
            

            

            Element elePersonInfoShipTo = (Element) docGetReleaseDet.getElementsByTagName("PersonInfoShipTo").item(0);
            log.verbose("elePersonInfoShipTo - " +XMLUtil.getElementXMLString(elePersonInfoShipTo));
            Element eleExtn1 = (Element) elePersonInfoShipTo.getElementsByTagName("Extn").item(0);
            log.verbose("eleExtn1 - " +XMLUtil.getElementXMLString(eleExtn1));

            Boolean str = false;
            if(!YFCObject.isVoid(env.getTxnObject("RemoveScac")))
            {
            	str = (Boolean)env.getTxnObject("RemoveScac");
            }

            if(str)
            {
            	log.verbose("***inside APO FPO check***");
            	if(!"Y".equals(eleExtn1.getAttribute("ExtnIsAPOFPO")) && !"Y".equals(eleExtn1.getAttribute("ExtnIsPOBOXADDRESS")))
            	{
            		env.setTxnObject("RemoveScac", true);
            		log.verbose("***inside APO FPO check***");
            		//inXML.getDocumentElement().setAttribute("SCAC", "");
            		//inXML.getDocumentElement().setAttribute("CarrierServiceCode", "");
            		log.verbose("***inXML***" +XMLUtil.getXMLString(inXML));
            	}
            }
            // upgrade - downgrade code end   
                
            //Start : OMNI-6369 : SO Shipment Type Changes
            String strFulfillmentType = eleOrderLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
            log.verbose(":: strFulfillmentType :: " + strFulfillmentType);
            if(!YFCObject.isVoid(strFulfillmentType) && strFulfillmentType.equals(AcademyConstants.STR_SHIP_TO_STORE)) {
            	inXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE, AcademyConstants.STR_SHIP_TO_STORE);
            }
            //End : OMNI-6369 : SO Shipment Type Changes

        }
		// Start : OMNI-6369 : STS Transfer Order Shipment Changes
		if (inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_DOC_TYPE).equals("0006")) {
			// Stamping the SCAC, Carrier and ShipmentType for STS DC and Store Orders

			String strShipNode = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE);
			
			Boolean nodeTypeIsStore = AcademyUtil.checkNodeTypeIsStore(env, strShipNode);
                    log.verbose("Is Store  : " +nodeTypeIsStore);       
                    //proceeding if ship node is DC	
                    if(nodeTypeIsStore == false ) {
                    	log.verbose("Stamping SCAC, Carrier and ShipmentType for STS DC Shipment");
				inXML.getDocumentElement().setAttribute(AcademyConstants.CARRIER_SERVICE_CODE,
						AcademyConstants.STR_SHIP_TO_STORE);
				inXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_SCAC, AcademyConstants.STR_LOCAL);
				inXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE,
						AcademyConstants.STR_SHIP_TO_STORE);
			} else {
				/* OMNI-46028 STS 2.0 Stamp Carrier attributes for TO shipment : START */
				log.verbose("Stamping SCAC as FEDX and CarrierServiceCode for STS Store Shipment");
				inXML.getDocumentElement().setAttribute(AcademyConstants.CARRIER_SERVICE_CODE,
						AcademyConstants.FEDX_GROUND);
				inXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_SCAC, AcademyConstants.FEDX_ORG_CODE);
				inXML.getDocumentElement().setAttribute(AcademyConstants.REQUESTED_CARRIER_SERVICE_CODE,
						AcademyConstants.FEDX_GROUND);
				inXML.getDocumentElement().setAttribute(AcademyConstants.SCAC_INTEGRATION_REQUIRED,
						AcademyConstants.STR_YES);
				/* OMNI-46028 STS 2.0 Stamp Carrier attributes for TO shipment : END */
				 //Start OMNI 45551 STS 2.0 Rate carrier look up, Stamping ACADEMY_DELIVERY_DATE to Additional Dates of shipment
				Document docGetReleaseDet = XMLUtil.createDocument("OrderReleaseDetail");
				eleShipLine=(Element) inXML
						.getElementsByTagName("ShipmentLine").item(0);
				
				if(!YFCObject.isVoid(eleShipLine)){
					sOrdRelKey=eleShipLine.getAttribute("OrderReleaseKey");
				}
				
				if(YFCObject.isVoid(sOrdRelKey))
				{
					throw new YFSUserExitException(" Order release key in beforeCreate Shipment UE implementation is null...............");
				}

				docGetReleaseDet.getDocumentElement().setAttribute(
						"OrderReleaseKey", sOrdRelKey);

				env.setApiTemplate("getOrderReleaseDetails", "global/template/api/getOrderReleaseDetails.CallToVertex.xml");
				docGetReleaseDet = AcademyUtil.invokeAPI(env,
						"getOrderReleaseDetails", docGetReleaseDet);
				env.clearApiTemplate("getOrderReleaseDetails");
				
				log.verbose("docGetReleaseDet - " +XMLUtil.getXMLString(docGetReleaseDet));
				Element eleOrderLine = (Element) docGetReleaseDet.getElementsByTagName("OrderLine").item(0);
				log.verbose("eleOrderLine - " +XMLUtil.getElementXMLString(eleOrderLine));

				//String dtExpectedShipmentDate = inXML.getDocumentElement().getAttribute("ExpectedShipmentDate");
				Element eleAdditionalDates = inXML.createElement("AdditionalDates");
				inXML.getDocumentElement().appendChild(eleAdditionalDates);

				NodeList orderDate = eleOrderLine.getElementsByTagName("OrderDate");
				for(int i=0; i<orderDate.getLength(); i++)
				{
					Element eleOrderDate = (Element)orderDate.item(i);
					log.verbose("eleOrderDate - " +XMLUtil.getElementXMLString(eleOrderDate));
					if("ACADEMY_DELIVERY_DATE".equals(eleOrderDate.getAttribute("DateTypeId")))
					{
						log.verbose("***inside if***");
						String strCommittedDate = eleOrderDate.getAttribute("CommittedDate");                   
						Element eleAdditionalDate = inXML.createElement("AdditionalDate");
						eleAdditionalDates.appendChild(eleAdditionalDate);
						eleAdditionalDate.setAttribute("RequestedDate", strCommittedDate);
						eleAdditionalDate.setAttribute("DateTypeId", "ACADEMY_DELIVERY_DATE");
						log.verbose("***inXML***" +XMLUtil.getXMLString(inXML));
					}
				}
				
			}
			//End OMNI 45551 STS 2.0 Rate carrier look up, Stamping ACADEMY_DELIVERY_DATE to Additional Dates of TO shipment

			// Stamping TO OrderNo on Shipment Level.
			String strOrderNo = XPathUtil.getString(inXML, "/Shipment/ShipmentLines/ShipmentLine/@OrderNo");
			String strOrderHeaderKey = XPathUtil.getString(inXML,
					"/Shipment/ShipmentLines/ShipmentLine/@OrderHeaderKey");

			log.verbose("***TO Details ***" + strOrderNo + " :::: " + strOrderHeaderKey);

			inXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
			inXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);

		}
		// End : OMNI-6369 : STS Transfer Order Shipment Changes

        log.endTimer(" End of AcademyYDMBeforeCreateShipmentUEImpl->beforeCreateShipment ()");      
        return inXML;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.yantra.interop.japi.YIFCustomApi#setProperties(java.util.Properties)
     */
    public void setProperties(Properties arg0) throws Exception {
        // TODO Auto-generated method stub

    }

}
