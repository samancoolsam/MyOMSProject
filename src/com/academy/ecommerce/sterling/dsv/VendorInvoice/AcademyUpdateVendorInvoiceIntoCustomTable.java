package com.academy.ecommerce.sterling.dsv.VendorInvoice;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientCreationException;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfc.log.YFCLogCategory;

public class AcademyUpdateVendorInvoiceIntoCustomTable
{
	private static YIFApi api = null;
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyUpdateVendorInvoiceIntoCustomTable.class);
	static 
	{
		try {
			api = YIFClientFactory.getInstance().getApi();
		} catch (YIFClientCreationException e) {
			e.printStackTrace();
		}

	}
	public Document GetShipToKey(YFSEnvironment env, Document inDoc) throws Exception
	{
		log.beginTimer("AcademyUpdateVendorInvoiceIntoCustomTable.GetShipToKey() method");
		log.debug("Input XML to AcademyUpdateVendorInvoiceIntoCustomTable API:\n" + XMLUtil.getXMLString(inDoc));
		try {
			String PO_NO = inDoc.getDocumentElement().getAttribute("PurchaseOrderNO");
			
			NodeList InvoiceList = inDoc.getElementsByTagName("ExtnVendorInvoiceDetails");
			String PrimeLineNO = ((Element) InvoiceList.item(0)).getAttribute("PrimeLineNO");
			
			Document getOrderLineListInput = XMLUtil.getDocument("<OrderLine PrimeLineNo=\" " +PrimeLineNO+ " \" SubLineNo=\" "+1+" \"> <Order OrderNo=\" "+ PO_NO +" \"> </Order> </OrderLine>");
			//System.out.println("input :: "+XMLUtil.getXMLString(getOrderLineListInput));
			Document getOrderLineListOutput =null;
			Document getOrderLineListTemplate = XMLUtil.getDocument("<OrderLineList>"
																	+"<OrderLine ShipToKey=\" \" />"
																	+"</OrderLineList>");
			env.setApiTemplate("getOrderLineList", getOrderLineListTemplate);
			getOrderLineListOutput = api.getOrderLineList(env, getOrderLineListInput);
			//System.out.println("output xml of getOrderLinelist for PO................."+ XMLUtil.getXMLString(getOrderLineListOutput));
			
			NodeList OrderLineList = getOrderLineListOutput.getElementsByTagName("OrderLine");
			String ShipToKey = ((Element) OrderLineList.item(0)).getAttribute("ShipToKey");
			
			inDoc.getDocumentElement().setAttribute("ShipToKey", ShipToKey);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Creating a info log to for Splunk alerts 
			log.info("DSV Vendor Invoice Error :: Error While Trying to Process Vendor Invoice for Purchase OrderNo:: " 
					+ inDoc.getDocumentElement().getAttribute("PurchaseOrderNO"));
			e.printStackTrace();
			//Throw custom Exception to roll back the transaction
			YFSException yfsExcep = new YFSException(e.getMessage());
			yfsExcep.setErrorCode("CHUB_INTERNAL_ERROR");
			yfsExcep.setErrorDescription("Error While trying to process Shipment in OMS ");
			throw yfsExcep;
		}
		log.endTimer("AcademyUpdateVendorInvoiceIntoCustomTable.GetShipToKey() method");
		return inDoc;
		
	}

}
