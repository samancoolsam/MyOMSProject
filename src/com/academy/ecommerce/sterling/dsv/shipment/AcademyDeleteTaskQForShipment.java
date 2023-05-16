package com.academy.ecommerce.sterling.dsv.shipment;

import org.w3c.dom.Document;

import com.academy.ecommerce.sterling.dsv.util.AcademyDsvUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * This class will be invoked on create and confirm shipment on_success event for PO and
 * the unwanted yfs_task_q entries for the shipment will be removed.
 * 
 * @author Manjusha V (215812)
 *
 */
public class AcademyDeleteTaskQForShipment {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyDeleteTaskQForShipment.class.getName());
	
	/**
	 * On successful completion of create and confirm shipment, a record will be inserted in yfs_task_q table as the next transaction in the
	 * pipeline is a task based transaction (create shipment invoice). As the shipment invoice creation is not using the yfs_task_q records
	 * which are inserted on success of the create and confirm shipment, we have to delete the entries from the table.
	 * 
	 *  This code deletes the unwanted yfs_task_q entry for that shipment.
	 * @param env
	 * @param deleteTaskQInDoc
	 * @return
	 */
	public Document deleteTaskQForShipment (YFSEnvironment env, Document deleteTaskQInDoc){
		
		log.verbose("AcademyDeleteTaskQForShipment.deleteTaskQForShipment () starts");
		log.verbose("Create And Confirm Shipment ON_SUCCESS event XML -->"+XMLUtil.getXMLString(deleteTaskQInDoc)+"<----");
		
		String strTranKey="";
		String strShipmentKey="";		
		Document deleteTaskQOutDoc = null;
		
		/**
		 * read the ShipmentKey from the input XML. 
		 * The record which needs to be deleted from yfs_task_q table will be present with the Data_Key =  ShipmentKey 
		 */
		try {
			strShipmentKey = XMLUtil.getString(deleteTaskQInDoc, "/Shipment/@ShipmentKey");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		log.verbose("Shipment Key : " +strShipmentKey);
		
		/**
		 * create document to invoke 'manageTaskQueue' API.
		 */
		YFCDocument  manageTaskQueueInDoc =YFCDocument.createDocument("TaskQueue");
		YFCElement manageTaskQueueInEle=manageTaskQueueInDoc.getDocumentElement();
		
		/**
		 * The attribute, 'Operation' needs to be provided with value 'Delete' in order to delete the record from yfs_task_q table.
		 */
		manageTaskQueueInEle.setAttribute("Operation", AcademyConstants.DSV_MANAGE_TASK_Q_OPERATION);
		
		/**
		 * AcademyDsvUtil.getTransactionDetails method will return the transaction_key for a given transaction id.
		 */
		strTranKey = AcademyDsvUtil.getTransactionDetails(env, AcademyConstants.DSV_CREATE_SHIPMENT_INV_TRAN_ID);
		log.verbose("Transaction Key: "+strTranKey);
		
		manageTaskQueueInEle.setAttribute("TransactionKey", strTranKey);
		manageTaskQueueInEle.setAttribute("DataKey", strShipmentKey);
		manageTaskQueueInEle.setAttribute("DataType", AcademyConstants.DSV_TASK_Q_DATA_TYPE);
		
		log.verbose("Input XML to manageTaskQueue API: --> "+manageTaskQueueInDoc.getString() +"<---");
		
		try {
			deleteTaskQOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.DSV_MANAGE_TASK_QUEUE_API, manageTaskQueueInDoc.getString());
		} catch (YFCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 log.verbose("manageTaskQueue API Output XML after deleting the record for the shipment:  --> "+XMLUtil.getXMLString(deleteTaskQOutDoc)+"<-------------!");
		 
		 log.verbose("AcademyDeleteTaskQForShipment.deleteTaskQForShipment () ends");
		return deleteTaskQOutDoc;
	}
}
