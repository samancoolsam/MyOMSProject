package com.academy.ecommerce.sterling.dsv.shipment;

import org.w3c.dom.Document;
import com.academy.util.common.AcademyUtil;
import com.academy.ecommerce.sterling.dsv.util.AcademyDsvUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;


/**
 * This class will be invoked on 'CREATE_SHIPMENT_INVOICE.ON_INVOICE_CREATION' event to add
 * an entry in yfs_task_q table for the shipment, with available_date = the dateInvoiced + Lead Time.
 *  
 * @author Manjusha V (215812)
 *
 */

public class AcademyAddTaskQForShipment {
	
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyAddTaskQForShipment.class.getName());
	
	/**
	 * 
	 * @param env
	 * @param createShipmentInvoiceInDoc
	 * @throws Exception
	 */
	public void addTaskQForShipment(YFSEnvironment env, Document createShipmentInvoiceInDoc) throws Exception
	{
		log.verbose("AcademyAddTaskQForShipment.addTaskQForShipment() starts");
		log.verbose("input document: --> "+XMLUtil.getXMLString(createShipmentInvoiceInDoc)+"<--------");
		YFCDocument createShipmentInvoiceInYFCDoc =  YFCDocument.getDocumentFor(createShipmentInvoiceInDoc);
	
		String strInvoicedDate="";
		String strInvoiceType="";
		String strParsedInvoiedDateAfterUpdation ="";
		String strShipmentType="";		
		String strShipmentKey="";
		
		int iLeadDays = 0;
		
		/**
		 * Read the DateInvoiced, ShipmentKey &  ShipmentType if  InvoiceType = SHIPMENT.
		 */
		
		 YFCNodeList<YFCElement>  orderInvoiceList = createShipmentInvoiceInYFCDoc.getElementsByTagName("OrderInvoice");
		 YFCElement orderInvoiceListEle= (YFCElement)orderInvoiceList.item(0);
		 
		 strInvoiceType = orderInvoiceListEle.getAttribute("InvoiceType");
		
		 //fetch for the shipment invoice only
		 if (AcademyConstants.DSV_ORDER_INVOICE_TYPE.equalsIgnoreCase(strInvoiceType))
		 {
			 log.verbose("Invoice Type is " +strInvoiceType);
			 strShipmentKey = orderInvoiceListEle.getAttribute("ShipmentKey");
			 strShipmentType = XMLUtil.getString(createShipmentInvoiceInDoc, "/OrderInvoice/Shipment/@ShipmentType");
			 strInvoicedDate = orderInvoiceListEle.getAttribute("DateInvoiced"); //yfs_order_invoice.createts
			 
			 log.verbose("Invoiced Date: "+strInvoicedDate);
			 log.verbose("Shipment Key: "+strShipmentKey);
			 log.verbose("Shipment Type: "+strShipmentType);
			 
			 /**
			  * invoke AcademyDsvUtil.getCommonCodeValueForCodeType method to get the Lead Time configured 
			  * in common codes for 'SHP_DELVRY_DAYS' 
			  */
			 iLeadDays = AcademyDsvUtil.getCommonCodeValueForCodeType(env, strShipmentType);
			 
			 /**
			  * invoke AcademyDsvUtil.getParsedInvoiedDateAfterUpdation method to get the estimated delivery date
			  * by adding the lead time + date invoiced.
			  */
			 strParsedInvoiedDateAfterUpdation = AcademyDsvUtil.getParsedInvoiedDateAfterUpdation(strInvoicedDate, iLeadDays);
			 
			 /**
			  * invoke insertTaskQEntryShipment method to invoke manageTaskQueue API to insert a record for the shipment with 
			  * available_date = strParsedInvoiedDateAfterUpdation for that shipment.
			  */
			 			 	
			 Document manageTaskQueueToInsertOutDoc = insertTaskQEntryShipment (env, strShipmentKey, strParsedInvoiedDateAfterUpdation);
			 log.verbose("manageTaskQueue API output document --> "+XMLUtil.getXMLString(manageTaskQueueToInsertOutDoc)+"<-------------!");
			 
		 }// if invoice type = SHIPMENT
		 else{		 
			 log.verbose("invoice_type is not equal to 'SHIPMENT'!");
		 }
		 log.verbose("AcademyAddTaskQForShipment.addTaskQForShipment() ends");
	}
	
	
	
	 /**
	  * invoke manageTaskQueue API to insert entry for a shipment key in yfs_task_q table.
	  * This method will be invoked to insert an entry with modified available_date for 'PO Shipment Delivered' transaction
	  */
	
	public Document insertTaskQEntryShipment (YFSEnvironment env, String strShipmentKey, String strParsedInvoiedDateAfterUpdation){
		
		log.verbose("AcademyAddTaskQForShipment.insertTaskQEntryShipment () starts. ");
		
		String strTranKey = "";
		Document manageTaskQueueOutDoc =null;
		
		/**
		 * invoke AcademyDsvUtil.getTransactionDetails method to get the transaction_key for the transaction id 'DSV_DELIVER_SHIPMENT_TRAN_ID'
		 */
		strTranKey = AcademyDsvUtil.getTransactionDetails(env, AcademyConstants.DSV_DELIVER_SHIPMENT_TRAN_ID);
		log.verbose("Transaction Key: "+strTranKey);
		
		/**
		 * create input xml to invoke manageTaskQueue API.
		 */
		YFCDocument  manageTaskQueueInDoc =YFCDocument.createDocument("TaskQueue");
		YFCElement manageTaskQueueInEle=manageTaskQueueInDoc.getDocumentElement();
		manageTaskQueueInEle.setAttribute("AvailableDate", strParsedInvoiedDateAfterUpdation);
		manageTaskQueueInEle.setAttribute("TransactionKey", strTranKey);
		manageTaskQueueInEle.setAttribute("DataKey", strShipmentKey);
		manageTaskQueueInEle.setAttribute("DataType", AcademyConstants.DSV_TASK_Q_DATA_TYPE);
		
		log.verbose("Input XML to manageTaskQueue API: --> "+manageTaskQueueInDoc.getString() +"<---");
		
		try {
			manageTaskQueueOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.DSV_MANAGE_TASK_QUEUE_API, manageTaskQueueInDoc.getString());
		} catch (YFCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.verbose("manageTaskQueue API output xml:  --> "+XMLUtil.getXMLString(manageTaskQueueOutDoc)+"<-------------!");
		log.verbose("AcademyAddTaskQForShipment.insertTaskQEntryShipment () ends. ");
		 
		 return manageTaskQueueOutDoc;
		 
	}
	
	
}

