package com.academy.ecommerce.server;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.academy.ecommerce.sterling.los.XMLUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.ycp.japi.util.YCPBaseTaskAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcadProcessPackedOrdersToShipped extends YCPBaseTaskAgent {

	private static YFCLogCategory logger = YFCLogCategory.instance(AcadProcessPackedOrdersToShipped.class);

	/**
	 * This method will pick the orders from TaskQ table and call confirmShipment
	 * api based on shipment status to move the order status to shipped.
	 */

	public Document executeTask(YFSEnvironment env, Document inXMLDoc) throws Exception {

		logger.beginTimer(" Begining of AcadProcessPackedOrdersToShipped-> executeTask");

		Element eleInDoc = inXMLDoc.getDocumentElement();
		logger.verbose("AcadProcessPackedOrdersToShipped : executeTask::" + XmlUtils.getString(eleInDoc));
		String strShipmentKey = eleInDoc.getAttribute(AcademyConstants.ATTR_DATA_KEY);
		String sTaskqKey = eleInDoc.getAttribute(AcademyConstants.ATTR_TASK_Q_KEY);
		// prepare input and call getShipmentList API
		Document docgetShipmentListOutput = prepareInputAndCallGetShipmentList(env, strShipmentKey);
		Element eleShipment = (Element) docgetShipmentListOutput.getDocumentElement()
				.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);
		if (!YFCObject.isVoid(eleShipment)) {
			String strStatus = eleShipment.getAttribute(AcademyConstants.STATUS);
			logger.verbose("Status" + strStatus);
			/**
			 * prepare input and invoke ConfirmShipment api if the Shipmentlevel Status is
			 * ReadyToShip else remove the entry from TaskQ table if present
			 */

			if (!YFCObject.isVoid(strStatus) && strStatus.equals(AcademyConstants.VAL_READY_TO_SHIP_TO_STORE_STATUS)) {

				Document docInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
				Element shipmentEle = docInput.getDocumentElement();
				shipmentEle.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
				shipmentEle.setAttribute(AcademyConstants.ATTR_MANIFEST_BEING_CLOSED, AcademyConstants.STR_YES);
				shipmentEle.setAttribute(AcademyConstants.ATTR_SHIP_COMPLETE, AcademyConstants.STR_YES);
				logger.verbose("The input to the confirmShipment api" + XmlUtils.getString(docInput));

				Document outDoc = AcademyUtil.invokeAPI(env, AcademyConstants.CONFIRM_SHIPMENT_API, docInput);
				logger.verbose("The output from the confirmShipment api" + XmlUtils.getString(outDoc));

			}
		}
		/** Calling this method to register completion of a task with the task queue */
		registerProcessCompletion(env, sTaskqKey);

		logger.endTimer(" End of AcadProcessPackedOrdersToShipped-> executeTask");
		return inXMLDoc;
	}

	/**
	 * This method will remove the records from TaskQ table by invoking
	 * registerProcessCompletion API
	 */
	private void registerProcessCompletion(YFSEnvironment yfsEnv, String sTaskqKey) throws Exception {
		logger.beginTimer(" Begining of AcadProcessPackedOrdersToShipped-> registerProcessCompletion");

		/**
		 * Creating input XML document for 'registerProcessCompletion' API to remove the
		 * record from TaskQ table
		 */
		Document docInput = XMLUtil.createDocument(AcademyConstants.ELE_REG_PROCESS_COMP_INPUT);
		docInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_KEEP_TASK_OPEN, AcademyConstants.STR_NO);
		Element eleCurrentTask = docInput.createElement(AcademyConstants.ELE_CURR_TASK);
		eleCurrentTask.setAttribute(AcademyConstants.ATTR_TASK_Q_KEY, sTaskqKey);
		docInput.getDocumentElement().appendChild(eleCurrentTask);

		logger.verbose("Input xml for registerProcessCompletion API " + XmlUtils.getString(docInput));
		AcademyUtil.invokeAPI(yfsEnv, AcademyConstants.API_REGISTER_PROCESS_COMPLETION, docInput);

		logger.endTimer(" End of AcadProcessPackedOrdersToShipped-> registerProcessCompletion");

	}

	/**
	 * This method will invoke getShipmentList API to check the shipment status
	 */
	private Document prepareInputAndCallGetShipmentList(YFSEnvironment env, String ShipmentKey) throws Exception {
		logger.beginTimer(" Begining of AcadProcessPackedOrdersToShipped-> prepareInputAndCallGetShipmentList");
		logger.verbose("ShipmentKey ::" + ShipmentKey);
		Document docgetShipmentListOutput = null;
		Document docGetShipmentListInput = null;

		/**
		 * Creating input XML document for 'getShipmentList' API to get the status of
		 * shipment
		 */

		docGetShipmentListInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element eleShipment = docGetShipmentListInput.getDocumentElement();
		eleShipment.setAttribute(AcademyConstants.SHIPMENT_KEY, ShipmentKey);
		logger.verbose("Input xml for getShipmentList api:" + XmlUtils.getString(docGetShipmentListInput));
		Document outputTemplate = YFCDocument
				.getDocumentFor("<Shipments> <Shipment ShipmentKey='' OrderNo='' Status=''/> </Shipments>")
				.getDocument();
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, outputTemplate);
		docgetShipmentListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST,
				docGetShipmentListInput);
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);

		logger.verbose("Output xml for getShipmentList api:" + XmlUtils.getString(docgetShipmentListOutput));

		logger.endTimer(" End of AcadProcessPackedOrdersToShipped-> prepareInputAndCallGetShipmentList");
		return docgetShipmentListOutput;
	}

}
