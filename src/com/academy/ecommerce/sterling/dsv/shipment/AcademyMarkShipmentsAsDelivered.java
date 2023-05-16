package com.academy.ecommerce.sterling.dsv.shipment;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.util.YCPBaseTaskAgent;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * This class will be invoked by the agent configured on the custom transaction 'PO Shipment Delivered'
 * 
 * The logic will fetch the records from yfs_task_q table
 * for the shipment which are Shipped and Invoiced having 
 * 
 * available date=current date and
 * Transaction key = transaction key of "PO Shipment Delivered".
 * 
 * And the corresponding shipment will be moved to 'Shipment Delivered' status.
 * 
 * @author Manjusha V (215812)
 *
 */
public class AcademyMarkShipmentsAsDelivered extends YCPBaseTaskAgent {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyMarkShipmentsAsDelivered.class.getName());
	
	@Override
	
	/**This method will be invoked by the agent server to mark the shipments as Shipment Delivered.
	 * 
	 * The custom logic will fetch all the record from yfs_task_q table, having 
	 * available date = current date and
	 * Transaction key = transaction key of "PO Shipment Delivered".
	 * 
	 * And will invoke changeShipmentStatus API to move the shipment to Shipment Delivered status and then will
	 * invoke registerProcessCompletion API to remove the processed record from yfs_task_q table.
	 * 
	 */
	public Document executeTask(YFSEnvironment env, Document executeTaskInDoc)
			throws Exception {
		
		String strShipmentKey = "";
		String strTaskQKey = "";
			
		Document changeShipmentStatusOutDoc =null;
		Document taskQInputDoc =null;
		Document taskQOutputDoc =null;
				
		YFCDocument  changeShipmentStatusInDoc =null;
		YFCDocument taskQOutputYFCDoc =null;
		
		Element taskQRootEle =null;
		YFCElement getShipmentListInEle =null;
		
		log.verbose("AcademyMarkShipmentsAsDelivered.executeTask () starts" );
		log.verbose("Input document: "+XMLUtil.getXMLString(executeTaskInDoc)+"<----------------");
		 
		/**
		  * Prepare input and call getTaskQueueDataList API.
		  * 
		  * This API will fetch all the record from yfs_task_q table, having 
		  * available_date =  current date and 
		  * Transaction key =  transaction  key of 'PO Shipment Delivered'
		  */
		
		taskQInputDoc = XMLUtil.createDocument("GetTaskQueueDataInput");
		taskQRootEle = taskQInputDoc.getDocumentElement();
		taskQRootEle.setAttribute("TransactionId", AcademyConstants.DSV_DELIVER_SHIPMENT_TRAN_ID);
		
		log.verbose("getTaskQueueDataList API Input Doc: "+XMLUtil.getXMLString(taskQInputDoc)+"<---------");
		taskQOutputDoc = AcademyUtil.invokeAPI(env, AcademyConstants.DSV_GET_TASKQ_DATALIST_API, taskQInputDoc);
		log.verbose("Output Doc of getTaskQueueDataList API: "+XMLUtil.getXMLString(taskQOutputDoc)+"<---------------");
		
		/**
		 * Read 'DataKey' from the out doc, which is the shipment key.
		 */
		if(taskQOutputDoc!=null){
			log.verbose("The taskQ Output Document is not null");
			taskQOutputYFCDoc = YFCDocument.getDocumentFor(taskQOutputDoc);
			YFCNodeList<YFCElement>  taskQueueList = taskQOutputYFCDoc.getElementsByTagName("TaskQueue");
		
			for (YFCElement taskQueueListEle : taskQueueList) {
				strShipmentKey = taskQueueListEle.getAttribute("DataKey");
				strTaskQKey =  taskQueueListEle.getAttribute("TaskQKey");
				
				log.verbose("Shipment Key (yfs_task_q.data_key) from yfs_task_q table --> "+strShipmentKey);
				log.verbose("Task Q Key: "+strTaskQKey);
			
		
				/**
				 * Prepare input document and invoke changeShipmentStatus API to move the 
				 * shipment status to Shipment Delivered.
				 */
		
				changeShipmentStatusInDoc =YFCDocument.createDocument("Shipment");
				getShipmentListInEle=changeShipmentStatusInDoc.getDocumentElement();
				getShipmentListInEle.setAttribute("BaseDropStatus", AcademyConstants.DSV_DELIVER_SHIPMENT_DROP_STATUS);
				getShipmentListInEle.setAttribute("ShipmentKey", strShipmentKey);
				getShipmentListInEle.setAttribute("TransactionId", AcademyConstants.DSV_DELIVER_SHIPMENT_TRAN_ID);
		 
				log.verbose("changeShipmentStatus API Input Doc: --> "+changeShipmentStatusInDoc.getString() +"<---");
				changeShipmentStatusOutDoc=AcademyUtil.invokeAPI(env, AcademyConstants.DSV_CHANGE_SHIPMENT_STATUS_API, changeShipmentStatusInDoc.getString());
				log.verbose("Output Document of changeShipmentStatus API: "+XMLUtil.getXMLString(changeShipmentStatusOutDoc)+"<-----");
		
				
				/**
				 * After the record from the yfs_task_q table is processed (i.e. after moving the shipment to Shipment Delivered status),
				 * invoke registerProcessCompletion API with the TaskQKey of the processed record to delete the
				 * corresponding record from yfs_task_q table.
				 */
				
				YFCDocument  registerProcessCompleteInDoc =YFCDocument.createDocument("RegisterProcessCompletionInput");
				YFCElement registerProcessCompleteInEle=registerProcessCompleteInDoc.getDocumentElement();
				registerProcessCompleteInEle.setAttribute("KeepTaskOpen", AcademyConstants.STR_NO);
				YFCElement currentTaskRootEle= registerProcessCompleteInDoc.createElement("CurrentTask");
				YFCElement currentTaskEle= (YFCElement) registerProcessCompleteInEle.appendChild(currentTaskRootEle);
				currentTaskEle.setAttribute("TaskQKey", strTaskQKey);
				
				log.verbose("Input document to registerProcessCompletion API : --> "+registerProcessCompleteInDoc.getString()+"<---");
				try {
					AcademyUtil.invokeAPI(env, AcademyConstants.DSV_REGISTER_PROCESS_COMPLETION_API, registerProcessCompleteInDoc.getString());
				} catch (YFCException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}// for (YFCElement taskQueueListEle : taskQueueList) {
		}// if(taskQOutputDoc!=null){
		else {
			log.verbose("The taskQ Output Document is empty/ null.");
		}
		log.verbose("AcademyMarkShipmentsAsDelivered.executeTask () ends" );
		
		return changeShipmentStatusOutDoc;
	}
}

