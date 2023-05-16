package com.academy.ecommerce.sterling.dsv.invoice;

import java.util.Properties;


import org.w3c.dom.Document;
import org.w3c.dom.Element;


import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyUpdateIPPOLookupTable implements YIFCustomApi{
    
    private static YFCLogCategory log = YFCLogCategory
    .instance(AcademyUpdateIPPOLookupTable.class);

    public void setProperties(Properties arg0) throws Exception {
        // TODO Auto-generated method stub
        
    }
    public Document updateIPPOLookup(YFSEnvironment env,
            Document inDoc) throws Exception {
        Document inDocToAPI = null;
        log.verbose("Entry class : AcademyUpdateIPPOLookupTable :: method : updateIPPOLookup with input \n " + XMLUtil.getXMLString(inDoc));
        Element ShipmentElement = inDoc.getDocumentElement();
        inDocToAPI = prepareInputDocToCreateIPPO(ShipmentElement);
        log.verbose("before IPPOLookup insert call inDocTOAPI is \n" + XMLUtil.getXMLString(inDocToAPI));
        AcademyUtil.invokeService(env, "AcademyLoadIPPOTable", inDocToAPI);
        log.verbose("after IPPOLookup insert call outDoc is \n" + XMLUtil.getXMLString(inDocToAPI));
        Document outDoc = prepareInputDocForCreateShipmentInvoice(inDoc);
        log.verbose("input to createShipmentInvoice is : \n " + XMLUtil.getXMLString(outDoc));
        return outDoc;
        
    }

    private Document prepareInputDocForCreateShipmentInvoice(Document inDoc) {
        // TODO Auto-generated method stub
        inDoc.getDocumentElement().removeAttribute("IPPurchaseOrderNo");
        inDoc.getDocumentElement().setAttribute("TransactionId", "CREATE_SHMNT_INVOICE.0005");
        return inDoc;
    }

    private Document prepareInputDocToCreateIPPO(Element shipmentElement) throws Exception {
        // TODO Auto-generated method stub
        Document inDocToAPI = XMLUtil.createDocument("ExtnIPPOLookup");
        inDocToAPI.getDocumentElement().setAttribute("ShipmentKey", shipmentElement.getAttribute("ShipmentKey"));
        inDocToAPI.getDocumentElement().setAttribute("PoNo", shipmentElement.getAttribute("IPPurchaseOrderNo"));
        inDocToAPI.getDocumentElement().setAttribute("PoDate", shipmentElement.getAttribute("IPPurchaseOrderDate"));
        return inDocToAPI;
        
    }
        

}
