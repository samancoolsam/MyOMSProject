package com.academy.ecommerce.sterling.dsv.invoice;

import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyDSVCreateShipmentInvoice implements YIFCustomApi{

	private static Logger log=Logger.getLogger(AcademyDSVCreateShipmentInvoice.class.getName());
	
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}
	public Document createShipmentInvoice(YFSEnvironment env, Document inDoc)
	throws Exception {
		
	    Document inDocToCreateShipmentInvoiceAPI=null;
	    Document inDocToChangeShipmentAPI = null;
		Document outXML1=null;
		//SOF :: WN-2035 Start - Vendor Invoicing
		Document docOutGetShipmentList = null;
		String strPOLineType = null;
		//SOF :: WN-2035 End - Vendor Invoicing
		
		if(log.isVerboseEnabled()){
			log.verbose("Entering class : AcademyDSVCreateShipmentInvoice  methodname : createShipmentInvoice "+XMLUtil.getXMLString(inDoc));
		}
		
		String strShipmentConfirmationFlag = "Y";
		if (env.getTxnObject("ShipmentConfirmationFlag") == null){
		    log.verbose("Inside if check for ShipmentConfirmationFlag is not null:::::");
		    env.setTxnObject("ShipmentConfirmationFlag", strShipmentConfirmationFlag);
		}
		
		if (env.getTxnObject("VendorNetShipConfirmationMessage") == null) {
            log.verbose("Inside if check for VendorNetShipConfirmationMessage:::::");
            String strIPPODate = inDoc.getDocumentElement().getAttribute("IPPurchaseOrderDate");
            env.setTxnObject("VendorNetShipConfirmationMessage", strIPPODate);
            log.verbose("VendorNetShipConfirmationMessage Txn Obj is set");
        }
		
		Element shipmentEle = inDoc.getDocumentElement();
		
		//SOF :: WN-2035 Start - Vendor Invoicing
		//For SOF Shipments, we will be Skipping createShipmentInvoice during IP_PO process.SO Shipment Invoicing will be taken care after SO is Shipped
		//Hence making getShipmentList call, to check if its SOF
		docOutGetShipmentList = getShipmentList(env, shipmentEle);
		
		strPOLineType = XPathUtil.getString(docOutGetShipmentList, AcademyConstants.XPATH_SHIPMENT_ORDERLINE_LINETYPE);
		
		if(!AcademyConstants.STR_SPECIAL_ORDER_FIREARMS.equals(strPOLineType)){
			log.verbose("IP_PO flow :: For DSV Shipments calling createShipmentInvoice");
		//SOF :: WN-2035 End - Vendor Invoicing
			
		inDocToCreateShipmentInvoiceAPI = prepareInputDocForCreateShipmentInvoice(shipmentEle);
		outXML1=AcademyUtil.invokeAPI(env, "createShipmentInvoice", inDocToCreateShipmentInvoiceAPI);
		log.verbose("v API called ::::::::");
		
		if(log.isVerboseEnabled()){
			log.verbose("Exiting class : AcademyDSVCreateShipmentInvoice  methodname : createShipmentInvoice "+XMLUtil.getXMLString(outXML1));
		}
		}
		log.verbose("Exiting AcademyDSVCreateShipmentInvoice.createShipmentInvoice() ");	
	return outXML1;	
	}
	
	//SOF :: WN-2035 Start - Vendor Invoicing
	/**
	 * Make getShipmentList API call, to check if PO lineType is SOF
	 * @param env
	 * @param shipmentEle
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws Exception
	 */
	private Document getShipmentList(YFSEnvironment env, Element shipmentEle)
			throws ParserConfigurationException, SAXException, IOException,
			Exception {
		Document docInGetShipmentList = null;
		Document docGetShipmentListTemplate = null;
		Document docOutGetShipmentList = null;
		
		docInGetShipmentList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT); 
		docInGetShipmentList.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, 
					shipmentEle.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
		log.verbose("getShipmentList API Input :: "+XMLUtil.getXMLString(docInGetShipmentList));
		
		docGetShipmentListTemplate = XMLUtil.getDocument("<Shipments> <Shipment> <ShipmentLines> <ShipmentLine>"
				+"<OrderLine LineType='' />"
				+"</ShipmentLine> </ShipmentLines> </Shipment> </Shipments>");
		
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST,docGetShipmentListTemplate);
		docOutGetShipmentList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST, docInGetShipmentList);
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
		
		log.verbose("getShipmentList API Output :: "+XMLUtil.getXMLString(docOutGetShipmentList));		
		return docOutGetShipmentList;
	}
	//SOF :: WN-2035 End - Vendor Invoicing

    private Document prepareInputDocForCreateShipmentInvoice(Element shipmentEle) throws Exception {
        // TODO Auto-generated method stub
        Document inDocToAPI = null;
	    String transactionId = "CREATE_SHMNT_INVOICE.0005";
	    String shipmentKey = shipmentEle.getAttribute("ShipmentKey");
	    inDocToAPI = XMLUtil.createDocument("Shipment");
	    inDocToAPI.getDocumentElement().setAttribute("ShipmentKey", shipmentKey);
	    inDocToAPI.getDocumentElement().setAttribute("TransactionId", transactionId);
        return inDocToAPI;
    }
	
   
}
