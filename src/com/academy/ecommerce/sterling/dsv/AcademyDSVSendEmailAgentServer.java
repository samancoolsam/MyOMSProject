package com.academy.ecommerce.sterling.dsv;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.ycp.japi.util.YCPBaseTaskAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyDSVSendEmailAgentServer extends YCPBaseTaskAgent {

	private static Logger log = Logger
			.getLogger(AcademyDSVSendEmailAgentServer.class.getName());

	@Override
	public Document executeTask(YFSEnvironment env, Document executeTaskInDoc)
			throws Exception {
		Element currElem = null;
		Document getShipmentListCallOutDoc = null;
		Document manageTaskQOutDoc = null;
		String shipmentKey = "";
		String strTaskQKey="";

		log.verbose("AcademyDSVSendEmailAgentServer.executeTask () starts");
		log.verbose("Input document -->"
				+ XMLUtil.getXMLString(executeTaskInDoc) + "<---");

		String transactionkey = executeTaskInDoc.getDocumentElement()
				.getAttribute(AcademyConstants.ATTR_TRANS_KEY);
		Document transactionListXML = callgetTransactionList(env,
				transactionkey);

		if (transactionListXML != null) {
			NodeList tranList = transactionListXML
					.getElementsByTagName(AcademyConstants.ELE_TRANSACTION);
			currElem = (Element) tranList.item(0);
		} else {
			log.verbose("transactionListXML is null");
		}
		
		
		//OMNI-40848 Start partial fulfillment email changes
				String strDataType=executeTaskInDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_DATA_TYPE);
				if(currElem.getAttribute(AcademyConstants.ATTR_TRAN_ID).equals("SEND_EMAIL_ON_DSV_INV.0005.ex")){
				if(!YFCCommon.isVoid(strDataType) && strDataType.equals(AcademyConstants.STR_ORDR_HDR_KEY)) {
					strTaskQKey =  executeTaskInDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_TASK_Q_KEY);
					log.verbose("Partial fulfillment email changes");
					String strOrderHeaderKey= executeTaskInDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_DATA_KEY);
					Document outXML1 = callgetCompleteOrderDetails(env, strOrderHeaderKey);
					AcademyUtil.invokeService(env, "AcademyPostPartialFulfillmentEmail", outXML1);
					StringBuffer notetextPF=new StringBuffer();
					Element orderElem=outXML1.getDocumentElement();
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date date = new Date();
					notetextPF.append("Sent Partial Fulfilment Email  on "+dateFormat.format(date)+" to "+orderElem.getAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID));
					callChangeOrder(env,strOrderHeaderKey,notetextPF);
					callregisterProcessCompletionAPI(env,strTaskQKey); 
					return executeTaskInDoc;
				}
				}
		//OMNI-40848 End partial fulfillment email changes
				
		if (currElem.getAttribute("Tranid").equals(
				"SEND_EMAIL_ON_DSV_SHIP.0005.ex")) {
			shipmentKey = executeTaskInDoc.getDocumentElement().getAttribute(
					AcademyConstants.ATTR_DATA_KEY);
			getShipmentListCallOutDoc = getShipmentListCall(env, shipmentKey);
		}
		if (currElem.getAttribute("Tranid").equals(
				"SEND_EMAIL_ON_DSV_INV.0005.ex")) {
			shipmentKey = executeTaskInDoc.getDocumentElement().getAttribute(
					AcademyConstants.ATTR_DATA_KEY);
			getShipmentListCallOutDoc = getShipmentListCall(env, shipmentKey);
			//start OMNI-38141  push notification changes
			AcademyUtil.invokeService(env, "AcademyPushNotificationOnDSVShipConfirm", getShipmentListCallOutDoc);
			//End OMNI-38141  push notification changes
		}

		String strTranId = currElem.getAttribute(AcademyConstants.ATTR_TRAN_ID);
		
		//Start : OMNI-2401 : Turn Off - Narvar -STL Shipment Confirmation Off
		String strEnableNarvarEmails = System.getProperty(AcademyConstants.PROP_ENABLE_NARVAR_EMAILS);
		String strSCAC = XPathUtil.getString(getShipmentListCallOutDoc, AcademyConstants.XPATH_SHIPMENT_SCAC);
		log.verbose(" Narvar Enabled :: " + strEnableNarvarEmails + " :: :: SCAC :: " + strSCAC);
		
		boolean bSentMail = false;
		
		if( (!YFCObject.isVoid(strTranId) && strTranId.equals("SEND_EMAIL_ON_DSV_INV.0005.ex")) &&
				(YFCObject.isVoid(strEnableNarvarEmails) || strEnableNarvarEmails.equals(AcademyConstants.STR_YES)) && 
				(!YFCCommon.isVoid(strSCAC) && !strSCAC.equals(AcademyConstants.STR_LOCAL))){
			log.verbose("Skipping DSV (for FEDX and USPS) Emails as Narvar emails are enabled. Delete Task Q");
			
			YFCDocument  manageTaskQueueInDoc =YFCDocument.createDocument(AcademyConstants.ELE_TASK_QUEUE);
			YFCElement manageTaskQueueInEle=manageTaskQueueInDoc.getDocumentElement();
			manageTaskQueueInEle.setAttribute(AcademyConstants.ATTR_OPERATION, AcademyConstants.STR_OPERATION_VAL_DELETE);
			strTaskQKey =  executeTaskInDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_TASK_Q_KEY);
			manageTaskQueueInEle.setAttribute(AcademyConstants.ATTR_TASK_Q_KEY, strTaskQKey);
			
			log.verbose("Input XML to manageTaskQueue API: --> "+manageTaskQueueInDoc.getString() +"<---");
			
			try {
				manageTaskQOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.DSV_MANAGE_TASK_QUEUE_API, manageTaskQueueInDoc.getString());
			} catch (Exception e) {
				log.info(" Task_Q deletion failed. TaskQKey="+strTaskQKey);
				e.printStackTrace();
			}
			
			return executeTaskInDoc;
		}
		else {
			bSentMail = callSendEmailService(env, strTranId, getShipmentListCallOutDoc);
		}
		//End : OMNI-2401 : Turn Off - Narvar -STL Shipment Confirmation Off
		
		/**
		 * delete the task_q record from yfs_task_q table, once the email to the business and customer has been sent successfully.
		 * 
		 * begins
		 */
		
		strTaskQKey =  executeTaskInDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_TASK_Q_KEY);
		log.verbose("Task Q Key: "+strTaskQKey);
		
		if (bSentMail){
			log.verbose("Email sent, so delete the task_q enrty");
			
			YFCDocument  manageTaskQueueInDoc =YFCDocument.createDocument("TaskQueue");
			YFCElement manageTaskQueueInEle=manageTaskQueueInDoc.getDocumentElement();
			manageTaskQueueInEle.setAttribute("Operation", AcademyConstants.DSV_MANAGE_TASK_Q_OPERATION);
			manageTaskQueueInEle.setAttribute("TaskQKey", strTaskQKey);
			
			log.verbose("Input XML to manageTaskQueue API: --> "+manageTaskQueueInDoc.getString() +"<---");
			
			try {
				manageTaskQOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.DSV_MANAGE_TASK_QUEUE_API, manageTaskQueueInDoc.getString());
			} catch (YFCException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
		}else{
			log.verbose("Email sending failed, so not deleting the task_q entry.");
		}
		
		/**
		 * ends
		 */
		return executeTaskInDoc;
	}

	private Document getShipmentListCall(YFSEnvironment env, String shipmentKey) {
		log.verbose("inside getShipmentListCall");
		Document getShipmentListInDoc = null;
		Document getShipmentListOutDoc = null;

		try {
			getShipmentListInDoc = XMLUtil.createDocument("Shipment");
			getShipmentListInDoc.getDocumentElement().setAttribute(
					"ShipmentKey", shipmentKey);
			Document outputTemplate = XMLUtil
					.getDocument("<Shipments>"
							+ "<Shipment OrderHeaderKey=\" \" ShipmentType=\" \" ShipmentKey=\"\" OrderNo=\"\" SCAC=\"\" >"
							+ "<ShipmentLines>"
							+ "<ShipmentLine OrderHeaderKey=\" \" ShipmentKey=\"\" ShipmentLineKey=\"\"/>"
							+ "</ShipmentLines>"
							+ "<OrderInvoiceList>"
							+ "<OrderInvoice DateInvoiced=\"\" InvoiceNo=\"\" InvoiceType=\"\" OrderNo=\"\"/>"
							+ "</OrderInvoiceList>" 
							+ "</Shipment>" + "</Shipments>");

			env.setApiTemplate("getShipmentList", outputTemplate);

			getShipmentListOutDoc = AcademyUtil.invokeAPI(env,
					"getShipmentList", getShipmentListInDoc);
			env.clearApiTemplate("getShipmentList");

			log.verbose("getShipmentListOutDoc: "
					+ XMLUtil.getXMLString(getShipmentListOutDoc)
					+ "<-----------");

		} catch (ParserConfigurationException e) {

			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getShipmentListOutDoc;
	}

	private boolean callSendEmailService(YFSEnvironment env, String tranId,
			final Document getShipmentListCallOutDoc) {
		boolean sendMailSuccess = true;

		log.verbose("inside callSendEmailService");
		try {
			if (tranId.equals("SEND_EMAIL_ON_DSV_SHIP.0005.ex")) {
				AcademyUtil.invokeService(env,
						"AcademySendShipConfirmationEmailToBusinessGrp",
						getShipmentListCallOutDoc);
			}
			if (tranId.equals("SEND_EMAIL_ON_DSV_INV.0005.ex")) {
				AcademyUtil
						.invokeService(
								env,
								"AcademySendShipConfirmationEmailToCustomer",
								getShipmentListCallOutDoc);
			}
		} catch (Exception e) {
			sendMailSuccess = false;
			e.printStackTrace();
		}
		return sendMailSuccess;
	}

	private Document callgetTransactionList(YFSEnvironment env,
			String transactionkey) {
		log.verbose("inside callgetTransactionList");
		Document inXML, outXML = null;

		Document outputTemplate = null;
		try {
			outputTemplate = XMLUtil.createDocument("TransactionList");
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
		Element transactionEle = outputTemplate.createElement("Transaction");
		transactionEle.setAttribute("Tranid", "");
		transactionEle.setAttribute("TransactionKey", "");
		transactionEle.setAttribute("Tranname", "");
		outputTemplate.getDocumentElement().appendChild(transactionEle);

		try {
			inXML = XMLUtil.createDocument(AcademyConstants.ELE_TRANSACTION);
			inXML.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_TRANS_KEY, transactionkey);
			env.setApiTemplate("getTransactionList", outputTemplate);
			outXML = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_TRANSACTION_LIST, inXML);

			log.verbose("getTransactionList out Doc --> "
					+ XMLUtil.getXMLString(outXML) + "<--");
			env.clearApiTemplate("getTransactionList");
		} catch (ParserConfigurationException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}
		return outXML;
	}
	
	private Document callgetCompleteOrderDetails(YFSEnvironment env, String orderHeaderKey) {
		Document inputXML=null;
		Document outXML=null;
		try {
			inputXML = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			inputXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, orderHeaderKey);
			inputXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.SALES_DOCUMENT_TYPE);
			env.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS, "global/template/api/getCompleteOrderDetails.ToSendEmail.xml");	
			outXML = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS, inputXML);
			log.verbose(XMLUtil.getXMLString(outXML));
			env.clearApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS);
		} catch (ParserConfigurationException e) {
			
			e.printStackTrace();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return outXML;
	}
	
	private void callChangeOrder(YFSEnvironment env, String orderHeaderKey, StringBuffer notetext) {
		try {
			Document changeOrderInputXML=XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			changeOrderInputXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, orderHeaderKey);
			Element notesElem=changeOrderInputXML.createElement(AcademyConstants.ELE_NOTES);
			changeOrderInputXML.getDocumentElement().appendChild(notesElem);
			Element noteElem=changeOrderInputXML.createElement(AcademyConstants.ELE_NOTE);
			noteElem.setAttribute(AcademyConstants.ATTR_NOTE_TEXT, notetext.toString());
			notesElem.appendChild(noteElem);
			AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER, changeOrderInputXML);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void callregisterProcessCompletionAPI(YFSEnvironment env, String taskQKey) {
		try {
			
			log.verbose("********callregisterProcessCompletionAPI***** ");
			Document inXML=XMLUtil.createDocument(AcademyConstants.ELE_REG_PROCESS_COMP_INPUT);
			Element rootElem=inXML.getDocumentElement();
			rootElem.setAttribute(AcademyConstants.ATTR_KEEP_TASK_OPEN, "N");
			Element currTaskElem=inXML.createElement(AcademyConstants.ELE_CURR_TASK);
			currTaskElem.setAttribute(AcademyConstants.ATTR_TASK_Q_KEY, taskQKey);
			rootElem.appendChild(currTaskElem);
			AcademyUtil.invokeAPI(env, AcademyConstants.API_REGISTER_PROCESS_COMPLETION, inXML);
			log.verbose("********callregisterProcessCompletionAPI END***** ");
		} catch (ParserConfigurationException e) {
			
			e.printStackTrace();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
	}
}
