package com.academy.ecommerce.sterling.sof.shipment;

import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.sts.AcademySTSCloseTOReceipt;
import com.academy.ecommerce.sterling.sts.AcademySTSReleaseAndCreateShipmentService;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author Chiranthan Narayanappa(C0007277)
 * @JIRA# WN-2034 - PO Changes ; WN-2036 - SO modifications
 * 
 * 
 * This Class will be invoked on ON_SUCCESS Event of the PO Create and Confirm Shipment,for SOF Shipments
 * This Class does the following :
 * 1.Call 'receiveOrder' API on PO -> This increases the ONHAND inventory of the store, and hence now SO release for that store can be created.
 * 2.Call 'releaseOrder' API on SO -> We manually call releaseOrder API with checkInventory='N', since release agent works with checkInventory='Y'.
 * 3.Call 'manageTaskQueue' API -> This deletes the 'RELEASE.0001' TaskQ entry, since we manually call releaseOrder.
 * 4.Call 'closeReceipt' API -> We post the closeReceipt input to a Queue and invoke the API asynchronously.
 **/


public class AcademySOFAutoPOReceiveSORelease implements YIFCustomApi {

	public static final YFCLogCategory log = YFCLogCategory.instance(AcademySOFAutoPOReceiveSORelease.class);
	//SOF :: Start WN-2044 Purging
	private Properties props;
	AcademySTSReleaseAndCreateShipmentService obj = new AcademySTSReleaseAndCreateShipmentService();
	String isDSVFA = "";
	String acqQty = "";
	String acqItemID = "";
	boolean bCloseReceiptComplete = false;

	public void setProperties(Properties props) throws Exception {
		
		this.props = props;
	}
	//SOF :: End WN-2044 Purging
	
	/**
	 * Calling receiveOrder API, releaseOrder API, manageTaskQueue API
	 * @param env
	 * @param inDoc
	 * @return docOutReceiveOrder
	 * @throws Exception
	 */
	public Document receivePOreleaseSO(YFSEnvironment env, Document inDoc) throws Exception {
		
		log.verbose("Entering AcademySOFAutoPOReceiveSORelease.receivePOreleaseSO() : " + XMLUtil.getXMLString(inDoc));
		
		Document docOutReceiveOrder = null;
		String strSOohk = null;
		//SOF :: Start WN-2044 Purging
		String strReceiptHeaderKey = null;		
		//SOF :: End WN-2044 Purging
		
		try {
			//OMNI-93544 - Start
			acqQty = inDoc.getDocumentElement().getAttribute("AcqQuantity");
			isDSVFA = inDoc.getDocumentElement().getAttribute("IsDSVFA");
			acqItemID = inDoc.getDocumentElement().getAttribute("AcqItemID");
			//OMNI-93544 - End
			
			if (!AcademyConstants.STR_YES.equals(isDSVFA)) {
				
				log.verbose("DSV SOF is not enabled. Executing as per old flow:");
				docOutReceiveOrder = receivePO(env, inDoc);
				log.verbose("1) PO Receiving Complete!!!!");
				
				strSOohk = XMLUtil.getString(inDoc, AcademyConstants.XPATH_SHIPMENT_ORDERLINE_CHAINEDFROMOHK);			
				
				releaseSO(env, strSOohk);			
				log.verbose("2) SO Release Complete!!!!");
				
				deleteSOreleaseTaskQ(env, strSOohk);				
				log.verbose("3) Deleting RELEASE.0001 Complete!!!!");
				
				//SOF :: Start WN-2044 Purging
				strReceiptHeaderKey = XMLUtil.getString(docOutReceiveOrder, AcademyConstants.XPATH_RECEIPT_RECEIPTHEADERKEY);
				
				postPOCloseReceiptMsgToQ(env, strReceiptHeaderKey, docOutReceiveOrder);				
				log.verbose("4) Posting 'closeReceipt' input message to Queue Complete!!!!");
				//SOF :: End WN-2044 Purging
				log.verbose("End of AcademySOFAutoPOReceiveSORelease");
			}
			
			//OMNI-93544 - Start			
			if (AcademyConstants.STR_YES.equals(isDSVFA)) {
				
				log.verbose("DSV SOF is enabled. Executing as per new flow:");
				docOutReceiveOrder = receivePO(env, inDoc);
				log.verbose("1) PO Receiving Complete!!!!");
				
				strSOohk = XMLUtil.getString(inDoc, AcademyConstants.XPATH_SHIPMENT_ORDERLINE_CHAINEDFROMOHK);
				strReceiptHeaderKey = XMLUtil.getString(docOutReceiveOrder, AcademyConstants.XPATH_RECEIPT_RECEIPTHEADERKEY);
				
				postPOCloseReceiptMsgToQ(env, strReceiptHeaderKey, docOutReceiveOrder);				
				log.verbose("2) closeReceipt Complete!!!!");
				
				if (bCloseReceiptComplete) {					
					releaseSO(env, strSOohk);			
					log.verbose("2) SO Release Complete!!!!");
					
					deleteSOreleaseTaskQ(env, strSOohk);				
					log.verbose("4) Deleting RELEASE.0001 Complete!!!!");

					Document docOrderReleaseList = obj.getOrderReleaseList(env, strSOohk);
					log.verbose("5) getOrderReleaseList Complete !!!!");
					//Fetch Release Details and create Shipment for the same.
					NodeList nlSOFRelease = XPathUtil.getNodeList(docOrderReleaseList, "/OrderReleaseList/OrderRelease[@MinOrderReleaseStatus='3200' and " +
							"OrderLines/OrderLine/@FulfillmentType='SOF']");
					
					log.verbose(" No. of SOF Order Releases :: " + nlSOFRelease.getLength());			
					if(nlSOFRelease.getLength() > 0) {
						for (int i = 0; i < nlSOFRelease.getLength(); i++) {
							Element eleOrderRelease = (Element) nlSOFRelease.item(i);
							String strOrderRelKey = eleOrderRelease.getAttribute(AcademyConstants.ATTR_RELEASE_KEY);
							changeRelease(env, strOrderRelKey);
							log.verbose("6) changeRelease Complete !!!!");
							
							obj.consolidateToShipment(env, strOrderRelKey);
							env.setTxnObject("ISSOFEnabled", "TRUE");
							log.verbose("7) consolidateToShipment Complete !!!! \n Setting TnxObject if multiRelease");
						}
					}
				}
			}
		//OMNI-93544 - End
		} catch (RuntimeException e) {
			log.verbose("RuntimeExeption thrown in method: receivePOreleaseSO() : "+e.getMessage());
			e.printStackTrace();
			throw e;
		} catch (ParserConfigurationException e) {
			log.verbose("ParserConfiguationExeption thrown in method: receivePOreleaseSO() : "+e.getMessage());
			e.printStackTrace();
			throw e;
		}
		log.verbose("Exiting AcademySOFAutoPOReceiveSORelease.receivePOreleaseSO() : " + XMLUtil.getXMLString(docOutReceiveOrder));
		
		return docOutReceiveOrder;
	}
	
	
	private void changeRelease(YFSEnvironment env, String strOrderRelKey) throws Exception {
		Document docInChangeRelease = XMLUtil.createDocument(AcademyConstants.ELE_ORD_RELEASE);
		Element eleRelease = docInChangeRelease.getDocumentElement();
		eleRelease.setAttribute(AcademyConstants.ATTR_ACTION, AcademyConstants.STR_ACTION_MODIFY_UPPR);
		eleRelease.setAttribute(AcademyConstants.ATTR_RELEASE_KEY, strOrderRelKey);
		eleRelease.setAttribute(AcademyConstants.ATTR_OVERRIDE, AcademyConstants.STR_YES);
		eleRelease.setAttribute(AcademyConstants.ATTR_DELIVERY_METHOD, AcademyConstants.STR_PICK);
		log.verbose("changeRelease Input:: " + XMLUtil.getXMLString(docInChangeRelease));
		Document docChangeReleaseTempl = XMLUtil.getDocument("<OrderRelease ReleaseNo='' OrderReleaseKey='' MinOrderReleaseStatus='' MaxOrderReleaseStatus='' DeliveryMethod=''>"
				+ "<Order OrderHeaderKey='' OrderNo=''><OrderLines><OrderLine OrderLineKey='' FulfillmentType='' "
				+ "DeliveryMethod='' PrimeLineNo=''/></OrderLines></Order></OrderRelease>");
		env.setApiTemplate(AcademyConstants.API_CHANGE_RELEASE, docChangeReleaseTempl); 
		Document docOutChangeRelease = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_RELEASE, docInChangeRelease);
		env.clearApiTemplate(AcademyConstants.API_CHANGE_RELEASE);
		log.verbose("changeRelease Output :: " + XMLUtil.getXMLString(docOutChangeRelease));
		
	}
	//SOF :: Start WN-2044 Purging
	/**
	 * After PO Shipment is received, invoke 'closeReceipt' API asynchronously.
	 * @param env
	 * @param strReceiptHeaderKey
	 * @throws Exception
	 */
	private void postPOCloseReceiptMsgToQ(YFSEnvironment env, String strReceiptHeaderKey, Document docOutReceiveOrder) throws Exception {
		log.verbose("Entering AcademySOFAutoPOReceiveSORelease.postPOCloseReceiptMsgToQ() : " + strReceiptHeaderKey);
		Document docInCloseReceiptMsg = null;
		
		docInCloseReceiptMsg = XMLUtil.createDocument(AcademyConstants.ELE_RECPT);
		docInCloseReceiptMsg.getDocumentElement().setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.DOCUMENT_TYPE_PO);
		docInCloseReceiptMsg.getDocumentElement().setAttribute(AcademyConstants.ATTR_RECPT_HEADERKEY, strReceiptHeaderKey);
		//OMNI-93544 - Start
		if (AcademyConstants.STR_YES.equals(isDSVFA)) {
			String shipKey = docOutReceiveOrder.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			docInCloseReceiptMsg.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, shipKey);
			docInCloseReceiptMsg.getDocumentElement().setAttribute(AcademyConstants.ATTR_RECV_NODE, 
					docOutReceiveOrder.getDocumentElement().getAttribute(AcademyConstants.ATTR_RECV_NODE));
			
			String finalQty = AcademySTSCloseTOReceipt.calculateReceiptQty(env, docOutReceiveOrder, shipKey);
			String[] qty = finalQty.split(",");
			int totalQuantity = Integer.parseInt(qty[0]);
			int totalReceiptQty = Integer.parseInt(qty[1]);
			
			if(totalQuantity == totalReceiptQty) {
				log.verbose("*****Condition statisfied to closeReceipt for DSV SOF *****");
				log.verbose("***closeReceipt input for DSv SOF ***\n" + XMLUtil.getXMLString(docInCloseReceiptMsg));
				AcademyUtil.invokeAPI(env, AcademyConstants.API_CLOSE_RCPT, docInCloseReceiptMsg);
				bCloseReceiptComplete = true;
				
			} else {
				log.verbose("Quantity is yet to be received for DSV SOF, So not going for closeReceipt");
			}
			//OMNI-93544 - End
		} else {
			log.verbose("Input to AcademySOFPostCloseReceiptMsgToQ : " + XMLUtil.getXMLString(docInCloseReceiptMsg));			
			AcademyUtil.invokeService(env, AcademyConstants.SERVICE_SOF_POST_CLOSERECEIPT_MSG_TO_QUEUE, docInCloseReceiptMsg);
		}
		log.verbose("Exiting AcademySOFAutoPOReceiveSORelease.postPOCloseReceiptMsgToQ()");
	}
	//SOF :: End WN-2044 Purging
	
	/**
	 * After PO Shipment is confirmed, calling receiveOrder on those shipments
	 * @param env
	 * @param inDoc
	 * @return docOutReceiveOrder
	 * @throws Exception
	 */
	private Document receivePO(YFSEnvironment env, Document inDoc) throws Exception {		
		Document docInReceiveOrder = null;
		Document docOutReceiveOrder = null;
		
		docInReceiveOrder = prepareReceiveOrderInput(inDoc);
		
		log.verbose("Input document to ReceiveOrder API :: "	+ XMLUtil.getXMLString(docInReceiveOrder));
		
		env.setApiTemplate(AcademyConstants.API_RECEIVE_ORDER, "global/template/event/RECEIVE_RECEIPT.ON_SUCCESS.0005.xml");		
		docOutReceiveOrder = AcademyUtil.invokeAPI(env,	AcademyConstants.API_RECEIVE_ORDER, docInReceiveOrder);
		env.clearApiTemplate(AcademyConstants.API_RECEIVE_ORDER);
		
		log.verbose("Output document from ReceiveOrder API :: "	+ XMLUtil.getXMLString(docOutReceiveOrder));
		
		return docOutReceiveOrder;
	}
	
	/**
	 * Prepare input for receiveOrder API
	 * @param env
	 * @param inDoc
	 * @return docInReceiveOrder
	 * @throws Exception
	 */
	private Document prepareReceiveOrderInput(Document inDoc) throws Exception{		
		Document docInReceiveOrder = null;
		Element eleShipment = null;
		Element eleReceiptLines = null;
		Element eleReceiptLine = null;
		Element eleShipmentLine = null;
		String strOHK = null;
		String strOLKey = "";
		String strSLKey = "";
		String strContainerkey = "";
		//SOF :: Start WN-2044 Purging
		String strDispositionCode = props.getProperty(AcademyConstants.ATTR_DISPOSITION_CODE);
		//SOF :: End WN-2044 Purging
		NodeList nlShipmentLine = null;
		
		docInReceiveOrder = XMLUtil.createDocument(AcademyConstants.ELE_RECPT);
		docInReceiveOrder.getDocumentElement().setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.DOCUMENT_TYPE_PO);
		docInReceiveOrder.getDocumentElement().setAttribute(AcademyConstants.ATTR_RECV_NODE, 
				inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_RECV_NODE));
		
		if (AcademyConstants.STR_YES.equals(isDSVFA)) {
			//OMNI-93544 - Start
			strOLKey = XMLUtil.getAttributeFromXPath(inDoc,
					AcademyConstants.XPATH_ITEM_ID+acqItemID+"]/OrderLine/@OrderLineKey");
			strSLKey = XMLUtil.getAttributeFromXPath(inDoc,
					AcademyConstants.XPATH_ITEM_ID+acqItemID+"]/@ShipmentLineKey");
			strContainerkey = XMLUtil.getAttributeFromXPath(inDoc,
					"Shipment/Containers/Container[ContainerDetails/ContainerDetail/@ItemID="+acqItemID+"]/@ShipmentContainerKey");
			docInReceiveOrder.getDocumentElement().setAttribute(AcademyConstants.ATTR_PALLET_ID,
					inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO));			
			docInReceiveOrder.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, 
					inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
		}
		//OMNI-93544 - End
		if (!AcademyConstants.STR_YES.equals(isDSVFA)) {
			eleShipment = docInReceiveOrder.createElement(AcademyConstants.ELE_SHIPMENT);
			eleShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, 
					inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO));
			eleShipment.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.DOCUMENT_TYPE_PO);			
			strOHK = XMLUtil.getString(inDoc, AcademyConstants.XPATH_SHIPMENT_SHIPMENTLINE_OHK);
			eleShipment.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOHK);
			eleShipment.setAttribute(AcademyConstants.ATTR_RECV_NODE, 
					inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_RECV_NODE));
			eleShipment.setAttribute(AcademyConstants.ATTR_SHIP_NODE, 
					inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE));
			XMLUtil.appendChild(docInReceiveOrder.getDocumentElement(), eleShipment);
		}
		
		eleReceiptLines = docInReceiveOrder.createElement(AcademyConstants.RECEIPT_LINES);
		XMLUtil.appendChild(docInReceiveOrder.getDocumentElement(), eleReceiptLines);			
		
		if (AcademyConstants.STR_YES.equals(isDSVFA)) {
			nlShipmentLine = XPathUtil.getNodeList(inDoc,
					"Shipment/ShipmentLines/ShipmentLine[@ItemID="+acqItemID+"]");
		} else {
			nlShipmentLine = inDoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
		}
		for(int i=0; i<nlShipmentLine.getLength(); i++){
			eleShipmentLine = (Element) nlShipmentLine.item(i);
			
			eleReceiptLine = docInReceiveOrder.createElement(AcademyConstants.RECEIPT_LINE);
			eleReceiptLine.setAttribute(AcademyConstants.ATTR_ITEM_ID, 
					eleShipmentLine.getAttribute(AcademyConstants.ATTR_ITEM_ID));
			eleReceiptLine.setAttribute(AcademyConstants.ATTR_UOM, 
					eleShipmentLine.getAttribute(AcademyConstants.ATTR_UOM));
			//SOF :: Start WN-2044 Purging
			eleReceiptLine.setAttribute(AcademyConstants.ATTR_DISPOSITION_CODE, strDispositionCode);
			//SOF :: End WN-2044 Purging
			//OMNI-93544 - Start
			if (AcademyConstants.STR_YES.equals(isDSVFA)) {
				eleReceiptLine.setAttribute(AcademyConstants.ATTR_QUANTITY, acqQty);
				log.verbose("Receiving Qty :: " + acqQty);
				eleReceiptLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, strOLKey);
				eleReceiptLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, strSLKey);
				Element eleExtn = docInReceiveOrder.createElement(AcademyConstants.ELE_EXTN);
				eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_SHP_CONT_KEY, strContainerkey);
				eleReceiptLine.appendChild(eleExtn);
			} else {
				eleReceiptLine.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOHK);
				eleReceiptLine.setAttribute(AcademyConstants.ATTR_QUANTITY, 
						eleShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY));
			}
			//OMNI-93544 - End
			
			XMLUtil.appendChild(eleReceiptLines, eleReceiptLine);		
		}
		
		return docInReceiveOrder;
	}
	
	/**
	 * After PO release, calling SO release
	 * @param env
	 * @param strSOohk
	 * @return
	 * @throws Exception
	 */
	private void releaseSO(YFSEnvironment env, String strSOohk) throws Exception {
		Document docInReleaseOrder = null;
		
		docInReleaseOrder = prepareReleaseOrderInput(strSOohk);
		
		log.verbose("Input document to releaseOrder API :: "	+ XMLUtil.getXMLString(docInReleaseOrder));
		AcademyUtil.invokeAPI(env,	AcademyConstants.API_RELEASE_ORDER, docInReleaseOrder);
		log.verbose("releaseOrder API call completed!!");
	}
	
	/**
	 * Preparing input for releaseOrder API
	 * @param env
	 * @param strSOohk
	 * @return docInReleaseOrder
	 * @throws Exception
	 */
	private Document prepareReleaseOrderInput(String strSOohk) throws Exception{
		Document docInReleaseOrder = null;
		
		docInReleaseOrder = XMLUtil.createDocument(AcademyConstants.ELE_RELEASE_ORDER);
		docInReleaseOrder.getDocumentElement().setAttribute(AcademyConstants.CHECK_INVENTORY, AcademyConstants.STR_NO);	
		docInReleaseOrder.getDocumentElement().setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.SALES_DOCUMENT_TYPE);		
		docInReleaseOrder.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strSOohk);
		
		return docInReleaseOrder;		
	}
	
	/**
	 * Calling manageTaskQueue API with Operation='Delete'
	 * @param env
	 * @param strSOohk
	 * @return
	 * @throws Exception
	 */
	private void deleteSOreleaseTaskQ(YFSEnvironment env, String strSOohk) throws Exception {
		Document docInManageTaskQ = null;
		Document docOutManageTaskQ = null;
		
		docInManageTaskQ = prepareManageTaskQueueInput(strSOohk);
		
		log.verbose("Input document to ManageTaskQueue API :: "	+ XMLUtil.getXMLString(docInManageTaskQ));			
		docOutManageTaskQ = AcademyUtil.invokeAPI(env, AcademyConstants.DSV_MANAGE_TASK_QUEUE_API, docInManageTaskQ);
		log.verbose("Output document from ManageTaskQueue API :: "	+ XMLUtil.getXMLString(docOutManageTaskQ));
	
	}
	
	/**
	 * Preparing input for manageTaskQueue API
	 * @param strSOohk
	 * @return docInManageTaskQ 
	 * @throws Exception
	 */
	private Document prepareManageTaskQueueInput(String strSOohk) throws Exception{
		Document docInManageTaskQ = null;
		
		docInManageTaskQ = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
		docInManageTaskQ.getDocumentElement().setAttribute(AcademyConstants.ATTR_OPERATION, AcademyConstants.STR_OPERATION_VAL_DELETE);
		docInManageTaskQ.getDocumentElement().setAttribute(AcademyConstants.ATTR_TRANS_KEY, AcademyConstants.STR_RELEASE_TRAN_KEY);
		docInManageTaskQ.getDocumentElement().setAttribute(AcademyConstants.ATTR_DATA_KEY, strSOohk);
		docInManageTaskQ.getDocumentElement().setAttribute(AcademyConstants.ATTR_DATA_TYPE, AcademyConstants.STR_ORDR_HDR_KEY);
		
		return docInManageTaskQ;
	}

}