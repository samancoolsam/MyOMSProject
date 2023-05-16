package com.academy.ecommerce.sterling.order.api;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyPricingAndPromotionUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSBeforeChangeOrderUE;

public class AcademyBeforeChangeOrderUEImpl implements YFSBeforeChangeOrderUE {

	private static final YFCLogCategory log = YFCLogCategory
			.instance(AcademyBeforeChangeOrderUEImpl.class);

	public Document beforeChangeOrder(YFSEnvironment env, Document inDoc)
			throws YFSUserExitException {
		log
				.beginTimer(" Begining of AcademyBeforeChangeOrderUEImpl->beforeChangeOrder Api");
		log.verbose("inXML to AcademyBeforeChangeOrderUE "
				+ XMLUtil.getXMLString(inDoc));
		Document outDoc = null;
		try {
			int noOfCancellableOL = XMLUtil.getNodeList(inDoc.getDocumentElement(), "OrderLines/OrderLine[@Action='CANCEL']").getLength();
			if(noOfCancellableOL > 0)
				outDoc = inDoc;
			else{
				outDoc = AcademyUtil.invokeService(env,
						AcademyConstants.YCD_BEFORE_CHANGE_ORDER_SERVICE, inDoc);
				log.verbose("inXML after YCD_SetOrderDefaultsOnChange_2 "
						+ XMLUtil.getXMLString(outDoc));
			}
			
			NodeList nOrderLine = XMLUtil.getNodeList(outDoc.getDocumentElement(), "OrderLines/OrderLine");
			int iOrderLine = nOrderLine.getLength();
			
			if (iOrderLine > 0) {
				env
						.setApiTemplate("getShipmentListForOrder",
								"global/template/api/getShipmentListForOrder.BatchPrint.xml");
				Document docShipmentListForOrder = AcademyUtil.invokeAPI(env,
						"getShipmentListForOrder", outDoc);
				env.clearApiTemplates();
				NodeList nShipment = docShipmentListForOrder
						.getDocumentElement().getElementsByTagName("Shipment");
				int iShipment = nShipment.getLength();
				if (iShipment > 0) {
					log.verbose("**** Order has active shipment against it*****");
					ArrayList lShipments = new ArrayList();
					String strWaveStatus;

					/*
					 * Retrieve the shipment line to which the orderline being
					 * cancelled belongs to. It is assumed that the orderline
					 * will not belong to multiple shipments. The below logic
					 * holds good only if an orderline belongs to single
					 * shipment
					 */
					for (int i = 0; i < iOrderLine; i++) {

						Element eleOrderLine = (Element) nOrderLine.item(i);
						String strOrderLineKey = eleOrderLine
								.getAttribute("OrderLineKey");
						for (int j = 0; j < iShipment; j++) {
							Element eleShipment = (Element) nShipment.item(j);
							Element eleShipmentLines = (Element) eleShipment
									.getElementsByTagName("ShipmentLines")
									.item(0);
							NodeList nShipmentLine = eleShipmentLines
									.getElementsByTagName("ShipmentLine");
							int iShipmentLine = nShipmentLine.getLength();
							for (int k = 0; k < iShipmentLine; k++) {
								Element eleShipmentLine = (Element) nShipmentLine
										.item(k);
								String sOrderLineKey = eleShipmentLine
										.getAttribute("OrderLineKey");
								if (sOrderLineKey.equals(strOrderLineKey)) {
									Element eleStatus = (Element) eleShipment
											.getElementsByTagName("Status")
											.item(0);
									String strShipmentStatus = eleStatus
											.getAttribute("Status");
									strShipmentStatus = strShipmentStatus
											.substring(0, 4);
									int iShipmentStatus = Integer
											.parseInt(strShipmentStatus);
									String strShipmentGroupId = eleShipment
											.getAttribute("ShipmentGroupId");
									/*
									 * Check if the shipment is already
									 * cancelled. If it is, we dont have to
									 * update the flag on Shipment since pack
									 * slips will be of no relevance in this
									 * scenario
									 */
									if ((iShipmentStatus < 9000)
											&& (strShipmentGroupId
													.equals("CON-OVNT")
													|| strShipmentGroupId
															.equals("CON-GC-SG") || strShipmentGroupId
													.equals("CON-ML-SG") || strShipmentGroupId
													.equals("GC-SG"))) {
										log
												.verbose("***Shipment is active and Batch print shipment****");
										String strShipmentKey = eleShipmentLine
												.getAttribute("ShipmentKey");
										String strWaveNo = eleShipmentLine
												.getAttribute("WaveNo");
										String strShipNode = eleShipment
												.getAttribute("ShipNode");
										if (!"".equals(strWaveNo)) {
											strWaveStatus = getWaveStatus(env,
													strShipNode, strWaveNo);
											Double dWaveStatus = Double
													.parseDouble(strWaveStatus);
											/**
											 * Check if there is a wave against
											 * the shipment and if the status of
											 * the shipment is greator than
											 * (Printed)1300.30, store the
											 * shipmentkey for shipment update *
											 */
											if (dWaveStatus > 1300.20) {
												if (!lShipments
														.contains(strShipmentKey)) {
													lShipments
															.add(strShipmentKey);
												}
											}
										}
									}
								}
							}
						}

					}

					int iShipments = lShipments.size();
					if (iShipments > 0) {
						log
								.verbose("*** Shipments need to be updated with ExtnLinesCancelled value****");
						env.setTxnObject("ShipmentKey", lShipments);
					}
				}

			}

			// commenting this code out, pricing is not in scope
			/*
			 * if("Y".equalsIgnoreCase(inDoc.getDocumentElement().getAttribute("ExtnIsPageDirty"))) {
			 * outDoc = AcademyUtil.invokeService(env,
			 * "AcademyBeforeChangeOrderService", inDoc); }
			 */
		} catch (Exception e) {
			log.verbose("Exception in AcademyBeforeChangeOrderUEImpl");
			// throw AcademyPricingAndPromotionUtil.wrapToYFSException(e);
			log.verbose(AcademyPricingAndPromotionUtil.wrapToYFSException(e));
		}
		if (outDoc == null) {
			outDoc = inDoc;
		}
		log.verbose("Output from AcademyBeforeChangeOrderUE "
				+ XMLUtil.getXMLString(outDoc));
		log
				.endTimer(" End of AcademyBeforeChangeOrderUEImpl->beforeChangeOrder Api");
		return outDoc;
	}

	private String getWaveStatus(YFSEnvironment env, String strShipNode,
			String strWaveNo) {
		log
				.beginTimer(" Begining of AcademyBeforeChangeOrderUEImpl -> getWaveStatus Api");
		String strStatus = "";
		try {
			Document docWave = XMLUtil.createDocument("Wave");
			docWave.getDocumentElement().setAttribute("WaveNo", strWaveNo);
			docWave.getDocumentElement().setAttribute("Node", strShipNode);
			Document outWaveList = AcademyUtil.invokeAPI(env, "getWaveList",
					docWave);
			Element eleWaveList = outWaveList.getDocumentElement();
			Element eleStatus = (Element) eleWaveList.getElementsByTagName(
					"Status").item(0);
			strStatus = eleStatus.getAttribute("Status");
			log.verbose("*** Status of the wave is ******" + strStatus);
			log
					.endTimer(" End of AcademyBeforeChangeOrderUEImpl  -> getWaveStatus Api");
			return strStatus;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strStatus;

	}
	public static void main(String[] args0)throws Exception {
		Document d = YFCDocument.getDocumentFor(new File("C://test.xml"))
				.getDocument();
		AcademyBeforeChangeOrderUEImpl ap = new AcademyBeforeChangeOrderUEImpl();
		ap.beforeChangeOrder(null, d);

	}
}
