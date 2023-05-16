package com.academy.ecommerce.sterling.order.api;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.util.YCPBaseAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/*
 * The custom non task queue based agent picks the Order Releases which have 
 * ExtnSharedInventory value as ‘S’ and process those releases if they are still 
 * in Released status by creating the Shipment for the same and also changing the 
 * status of the Order Release to ‘SI Included In Shipment’. After that the 
 * ExtnSharedInventory value is modified to ‘P’. If the Releases are Cancelled 
 * or Backordered then EXTN_SHARED_INVENTORY flag is modified to ‘C’ or ‘B’ and 
 * in all other cases ‘X’. 
 */
public class AcademySharedInventoryConsolidateToShipmentSTS extends YCPBaseAgent {
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademySharedInventoryConsolidateToShipmentSTS.class.getName());

	/*
	 * The getJobs method invokes the getOrderReleaseList API to fetch the Order
	 * Releases which have OrderRelease/Extn/@ExtnSharedInventory value as ‘S’ and
	 * forms the List of such OrderRelease documents and returns the same
	 * 
	 * @param env YFSEnvironment
	 * 
	 * @param inXML Document
	 * 
	 * @param lastMessage Document
	 * 
	 * @return orderReleaseList List
	 */
	public List<Document> getJobs(YFSEnvironment env, Document inXML, Document lastMessage) throws Exception {
		log.verbose("Inside AcademySharedInventoryConsolidateToShipmentSTS.getJobs method ");
		log.verbose("Input Document of getJobs method is inXML " + XMLUtil.getXMLString(inXML) + "<-----");

		List<Document> orderReleaseList = new ArrayList<Document>();
		Document getOrderReleaseListInDoc = null;
		Document getOrderReleaseListOutDoc = null;
		Element orderReleaseInEle = null;
		Element orderReleaseExtnEle = null;

		getOrderReleaseListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_ORD_RELEASE);
		orderReleaseInEle = getOrderReleaseListInDoc.getDocumentElement();

		orderReleaseInEle.setAttribute(AcademyConstants.ATTR_MAX_RECORD,
				inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_NUM_RECORDS));
		orderReleaseInEle.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE);
		orderReleaseExtnEle = getOrderReleaseListInDoc.createElement(AcademyConstants.ELE_EXTN);
		orderReleaseInEle.appendChild(orderReleaseExtnEle);
		orderReleaseExtnEle.setAttribute(AcademyConstants.ATTR_EXTN_SI, AcademyConstants.EXTN_SHARED_INVENTORY_S);
		log.verbose("Input Document of getOrderReleaseList XML is" + XMLUtil.getXMLString(getOrderReleaseListInDoc)
				+ "<-----");
		if (!YFCObject.isVoid(lastMessage)) {
			log.verbose("LastMessage is present for the OrderRelease");
			log.verbose(
					"Input Document of getJobs method is lastMessage " + XMLUtil.getXMLString(lastMessage) + "<-----");
			String lastOrderReleaseKey = lastMessage.getDocumentElement()
					.getAttribute(AcademyConstants.ATTR_RELEASE_KEY);
			orderReleaseInEle.setAttribute(AcademyConstants.ATTR_RELEASE_KEY, lastOrderReleaseKey);
			orderReleaseInEle.setAttribute(AcademyConstants.ATTR_ORD_REL_KEY_QRY_TYPE, AcademyConstants.GT_QRY_TYPE);
		}
		log.verbose(
				"getOrderReleaseList API Input Doc: --> " + XMLUtil.getXMLString(getOrderReleaseListInDoc) + "<---");

		// Set the getOrderReleaseList template
		env.setApiTemplate(AcademyConstants.API_GET_ORDER_RELEASE_LIST,
				"global/template/api/SIConsolidateToShipment_getOrderReleaseList.xml");
		getOrderReleaseListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_RELEASE_LIST,
				getOrderReleaseListInDoc);

		// Clearing the template
		env.clearApiTemplate(AcademyConstants.API_GET_ORDER_RELEASE_LIST);
		log.verbose("Output Document of getOrderReleaseList API: " + XMLUtil.getXMLString(getOrderReleaseListOutDoc)
				+ "<-----");
		NodeList orderReleaseOutList = getOrderReleaseListOutDoc.getElementsByTagName(AcademyConstants.ELE_ORD_RELEASE);

		// populating orderReleaseNewDoc in the ArrayList
		for (int i = 0; i < orderReleaseOutList.getLength(); i++) {
			Element orderReleaseOutEle = (Element) orderReleaseOutList.item(i);
			Document orderReleaseNewDoc = XMLUtil.getDocumentForElement(orderReleaseOutEle);
			orderReleaseList.add(orderReleaseNewDoc);
		}
		return orderReleaseList;

	}

	/*
	 * The executeJob method processes each OrderRelease which is returned by
	 * getJobs Method and if those releases are still in Released status creates the
	 * Shipment for the same and also the status of the OrderRelease is changed to
	 * ‘SI Included In Shipment’. At the end the EXTN_SHARED_INVENTORY flag is
	 * modified to ‘P’. If the OrderReleases are Cancelled or Backordered then
	 * EXTN_SHARED_INVENTORY flag is modified to ‘C’ or ‘B’ and in all other cases
	 * ‘X’.
	 * 
	 * @param env YFSEnvironment
	 * 
	 * @param inDoc Document
	 * 
	 * @return void
	 */
	@Override
	// Execute Job method
	public void executeJob(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("Inside AcademySharedInventoryConsolidateToShipmentSTS.executeJob method ");
		log.verbose("Input Document of executeJob method is " + XMLUtil.getXMLString(inDoc) + "<-----");

		String strOrderReleaseKey = "";
		String strOrderHeaderKey = "";
		YFCDocument changeOrderStatusInDoc = null;
		Document changeOrderStatusOutDoc = null;
		YFCElement changeOrderStatusInEle = null;
		YFCDocument consolidateToShipInDoc = null;
		YFCElement consolidateToShipInEle = null;
		Document consolidateToShipOutDoc = null;

		try {
			strOrderReleaseKey = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_RELEASE_KEY);
			String strMinReleaseStatus = inDoc.getDocumentElement()
					.getAttribute(AcademyConstants.ATTR_MIN_ORD_REL_STATUS);
			String strMaxReleaseStatus = inDoc.getDocumentElement()
					.getAttribute(AcademyConstants.ATTR_MAX_ORD_REL_STATUS);
			// If the MinOrderReleaseStatus or MaxOrderReleaseStatus is 3200
			if (AcademyConstants.VAL_RELE_STATUS.equals(strMinReleaseStatus)
					|| AcademyConstants.VAL_RELE_STATUS.equals(strMaxReleaseStatus)) {
				consolidateToShipInDoc = YFCDocument.createDocument(AcademyConstants.ELE_ORD_RELEASE);
				consolidateToShipInEle = consolidateToShipInDoc.getDocumentElement();
				consolidateToShipInEle.setAttribute(AcademyConstants.ATTR_RELEASE_KEY, strOrderReleaseKey);

				env.setApiTemplate(AcademyConstants.API_CONSOL_SHIP,
						"global/template/api/SIConsolidateToShipment_consolidateToShipment.xml");
				log.verbose("consolidateToShipment API Input Doc: --> " + consolidateToShipInDoc.getString() + "<---");
				consolidateToShipOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_CONSOL_SHIP,
						consolidateToShipInDoc.getString());
				log.verbose("Output Document of consolidateToShipment API: "
						+ XMLUtil.getXMLString(consolidateToShipOutDoc) + "<-----");
				env.clearApiTemplate(AcademyConstants.API_CONSOL_SHIP);
				/**
				 * Prepare input document and invoke changeOrderStatus API to move the order
				 * release status to SI Included In Shipment.
				 */

				/*Element shipmentEle = consolidateToShipOutDoc.getDocumentElement();
				NodeList shipmentLineNodeList = shipmentEle.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
				Element shipmentLineElement = (Element) shipmentLineNodeList.item(0);
				strOrderHeaderKey = shipmentLineElement.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);

				changeOrderStatusInDoc = YFCDocument.createDocument(AcademyConstants.ELE_ORD_STATUS_CHG);
				changeOrderStatusInEle = changeOrderStatusInDoc.getDocumentElement();
				changeOrderStatusInEle.setAttribute(AcademyConstants.ATTR_BASEDROP_STATUS,
						AcademyConstants.VAL_SI_INCLUDEDINSHIPMENT_STATUS);

				changeOrderStatusInEle.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
				changeOrderStatusInEle.setAttribute(AcademyConstants.ATTR_RELEASE_KEY, strOrderReleaseKey);
				changeOrderStatusInEle.setAttribute(AcademyConstants.ATTR_TRANSID,
						AcademyConstants.SI_CONSOL_TO_SHIPMENT_TRAN_ID_TRAN_ORDER);

				env.setApiTemplate(AcademyConstants.API_CHG_ORD_STATUS,
						"global/template/api/SIConsolidateToShipment_changeOrderStatus.xml");
				log.verbose("changeOrderStatus API Input Doc: --> " + changeOrderStatusInDoc.getString() + "<---");
				changeOrderStatusOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHG_ORD_STATUS,
						changeOrderStatusInDoc.getString());
				log.verbose("Output Document of changeOrderStatus API: " + XMLUtil.getXMLString(changeOrderStatusOutDoc)
						+ "<-----");
				env.clearApiTemplate(AcademyConstants.API_CHG_ORD_STATUS);*/
				// Call changeRelease to change the ExtnSharedInventory value to P
				changeReleaseDocument(env, strOrderReleaseKey, AcademyConstants.EXTN_SHARED_INVENTORY_P);
			} else if ((AcademyConstants.VAL_CANCELLED_STATUS).equals(strMinReleaseStatus)) {
				// Call changeRelease to change the ExtnSharedInventory value to C
				changeReleaseDocument(env, strOrderReleaseKey, AcademyConstants.EXTN_SHARED_INVENTORY_C);
			} else if ((AcademyConstants.VAL_BACKORDERED_STATUS).equals(strMinReleaseStatus)) {
				// Call changeRelease to change the ExtnSharedInventory value to B
				changeReleaseDocument(env, strOrderReleaseKey, AcademyConstants.EXTN_SHARED_INVENTORY_B);
			} else {
				// Call changeRelease to change the ExtnSharedInventory value to X
				changeReleaseDocument(env, strOrderReleaseKey, AcademyConstants.EXTN_SHARED_INVENTORY_X);
			}
		} catch (Exception ex) {
			log.verbose("Exception in AcademySharedInventoryConsolidateToShipmentSTS.executeJob() method.");
			throw ex;
		}
	}

	/*
	 * This method constucts the input xml and invokes the changeRelease API to
	 * update the OrderRelease/Extn/@ExtnSharedInventory attribute value
	 * 
	 * @param env YFSEnvironment
	 * 
	 * @param strOrderReleaseKey String
	 * 
	 * @param extnSharedInventory String
	 * 
	 * @return void
	 */
	public void changeReleaseDocument(YFSEnvironment env, String strOrderReleaseKey, String extnSharedInventory)
			throws Exception {
		log.verbose("Inside AcademySharedInventoryConsolidateToShipmentSTS.changeReleaseDocument() method.");
		YFCDocument changeReleaseInDoc = null;
		YFCElement changeReleaseInEle = null;
		YFCElement changeReleaseExtnEle = null;
		changeReleaseInDoc = YFCDocument.createDocument(AcademyConstants.ELE_ORD_RELEASE);
		changeReleaseInEle = changeReleaseInDoc.getDocumentElement();
		changeReleaseInEle.setAttribute(AcademyConstants.ATTR_RELEASE_KEY, strOrderReleaseKey);
		changeReleaseInEle.setAttribute(AcademyConstants.ATTR_OVERRIDE, AcademyConstants.STR_YES);
		changeReleaseExtnEle = changeReleaseInDoc.createElement(AcademyConstants.ELE_EXTN);
		changeReleaseInEle.appendChild(changeReleaseExtnEle);
		changeReleaseExtnEle.setAttribute(AcademyConstants.ATTR_EXTN_SI, extnSharedInventory);
		env.setApiTemplate(AcademyConstants.API_CHANGE_RELEASE,
				"global/template/api/SIConsolidateToShipment_changeRelease.xml");
		log.verbose("changeRelease API Input Doc: --> " + changeReleaseInDoc.getString() + "<---");
		Document changeReleaseOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_RELEASE,
				changeReleaseInDoc.getString());
		log.verbose("Output Document of changeRelease API: " + XMLUtil.getXMLString(changeReleaseOutDoc) + "<-----");
		env.clearApiTemplate(AcademyConstants.API_CHANGE_RELEASE);
	}
}
