package com.academy.ecommerce.sterling.api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.util.YCPBaseTaskAgent;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademySendMailOnBulkGiftCardOrders extends YCPBaseTaskAgent {
		
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademySendMailOnBulkGiftCardOrders.class);
	public Document executeTask(YFSEnvironment env, Document agentXML)
			throws Exception {
		
		String taskQKey, gcActivationCode;
		Document outShipDoc, outDoc, inXML, inShipDoc;
		Element ordEle, extnEle, rootElem, currTaskElem;
		try {
			log.beginTimer(" Begining of AcademySendMailOnBulkGiftCardOrders executeTask() Api");
			taskQKey = agentXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_TASK_Q_KEY);
			inShipDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			inShipDoc.getDocumentElement().setAttribute(AcademyConstants.SHIPMENT_KEY,
					agentXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_DATA_KEY));

			env
					.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_DETAILS,
							"global/template/api/getShipmentDetails.BulkGCActivation.xml");
			outShipDoc = AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_SHIPMENT_DETAILS,
					inShipDoc);
			ordEle = (Element) outShipDoc.getElementsByTagName(AcademyConstants.ELE_ORDER).item(0);

			outDoc = AcademyUtil.invokeService(env,
					AcademyConstants.ACADEMY_GC_ACTIVATION_SERVICE, inShipDoc);
			outDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_NO,
					ordEle.getAttribute(AcademyConstants.ATTR_ORDER_NO));
			outDoc.getDocumentElement().setAttribute("OriginalTotalAmount",
					ordEle.getAttribute("OriginalTotalAmount"));
			outDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID,
					ordEle.getAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID));

			gcActivationCode = outDoc.getDocumentElement().getAttribute(
					AcademyConstants.STR_GC_ACTIVATION_CODE);
			
			//Start WN-697 : Sterling to consume special characters and include them in customer-facing emails, but to remove them before settlement.				
			AcademyUtil.convertUnicodeToSpecialChar(env, outDoc.getDocumentElement(), null, false);
			//End WN-697 : Sterling to consume special characters and include them in customer-facing emails, but to remove them before settlement.
			
			// invoke service to send an Mail to Customer
			AcademyUtil.invokeService(env,
					AcademyConstants.ACADEMY_GC_ACTIVATION_MAIL_SERVICE,outDoc);

			// calling changeShipment to store Activation code
			extnEle = inShipDoc.createElement(AcademyConstants.ELE_EXTN);
			extnEle.setAttribute(AcademyConstants.ATTR_EXTN_GCACT_CODE, gcActivationCode);
			inShipDoc.getDocumentElement().appendChild((Node) extnEle);
			AcademyUtil.invokeAPI(env,AcademyConstants.API_CHANGE_SHIPMENT, inShipDoc);

			// Completing Register process by calling RegisterProcess Api()
			inXML = XMLUtil
					.createDocument(AcademyConstants.ELE_REG_PROCESS_COMP_INPUT);

			rootElem = inXML.getDocumentElement();

			rootElem.setAttribute(AcademyConstants.ATTR_KEEP_TASK_OPEN, AcademyConstants.STR_NO);

			currTaskElem = inXML.createElement(AcademyConstants.ELE_CURR_TASK);

			currTaskElem.setAttribute(AcademyConstants.ATTR_TASK_Q_KEY,
					taskQKey);

			rootElem.appendChild(currTaskElem);

			AcademyUtil.invokeAPI(env,
					AcademyConstants.API_REGISTER_PROCESS_COMPLETION, inXML);
			log.endTimer(" End of AcademySendMailOnBulkGiftCardOrders executeTask() Api");
		} catch (Exception e) {
			
			throw new YFSException(e.getMessage());
		}
		
		return agentXML;
	}

}
