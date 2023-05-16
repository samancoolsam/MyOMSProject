package com.academy.ecommerce.sterling.sof.shipment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @author C0028786
 * 
 * This java class is used to cancel the firearm shipments on receiving cancellation message from GSM
 */

public class AcademyProcessHardDeclineFromGSM {
	public static final YFCLogCategory log = YFCLogCategory.instance(AcademyProcessHardDeclineFromGSM.class);
	private HashMap<String, Document> hmpChangeShipment = null;
	private static List<String> listShipmentStatus = null;
	private static List<String> listOrderLineStatus = null;

	static {
		listShipmentStatus = Arrays.asList(("1300,1400,1600.002,1600.002.100,1600.002.51,1600.002.52,1600.002.99,9000")
				.split(AcademyConstants.STR_COMMA));
		listOrderLineStatus = Arrays.asList(
				("1400,2100.100,2100.200,3350,3350.300,3700,3700.01,3700.02,3700.7777,9000,3350.400,3700.100,1100,2160.00.01,2160,2060")
						.split(AcademyConstants.STR_COMMA));
	}

	public Document processHardDecline(YFSEnvironment env, Document docInput) throws Exception {
		log.beginTimer("AcademyProcessHardDeclineFromGSM.processHardDecline() :: " + XMLUtil.getXMLString(docInput));
		Element eleInput = docInput.getDocumentElement();
		String strSalesOrderNo = eleInput.getAttribute(AcademyConstants.STR_SOF_ACQDSP_CUSTOMER_ORDERNO);
		double dCurrentQuantity;
		double dNewLineQuantity;
		Document docChangeOrderOut = null;

		Document docCompleteOrderDetailsOut = null;
		docCompleteOrderDetailsOut = callGetCompleteOrderDetails(env, strSalesOrderNo);
		log.verbose("docCompleteOrderDetailsOut XML" + XMLUtil.getXMLString(docCompleteOrderDetailsOut));
		Element eleOrderOut = docCompleteOrderDetailsOut.getDocumentElement();
		
		Document docChangeOrderInp = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		Element eleChangeOrderInp = docChangeOrderInp.getDocumentElement();

		eleChangeOrderInp.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, eleOrderOut.getAttribute(AcademyConstants.STR_ORDR_HDR_KEY));
		Element eleChangeOrderLines = docChangeOrderInp.createElement(AcademyConstants.ELE_ORDER_LINES);
		eleChangeOrderInp.appendChild(eleChangeOrderLines);
		
		String strMinOrderStatus = getMinOrderStatus(eleOrderOut);

		if (Integer.parseInt(strMinOrderStatus) < 3700) {
			log.verbose("Order contains a non-shipped or cancelled line");
			NodeList nlOrderLines = XPathUtil.getNodeList(docCompleteOrderDetailsOut,
					AcademyConstants.XPATH_EXTNISPROMOITEM);
			hmpChangeShipment = new HashMap<>();

			for (int iOrderLineCount = 0; iOrderLineCount < nlOrderLines.getLength(); iOrderLineCount++) {

				Element eleOrderLine = (Element) nlOrderLines.item(iOrderLineCount);
			
				boolean bIsFireArm = false;
				bIsFireArm = isFirearmPresent(eleOrderLine, bIsFireArm);
				log.verbose("bIsFireArm::" + bIsFireArm);
				
				String strOrderLineKey = eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
				String strOrderedQuantity = eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDERED_QTY);
				dCurrentQuantity = Double.parseDouble(strOrderedQuantity);
				dNewLineQuantity = dCurrentQuantity;
				
				Element eleShipmentLines = (Element) eleOrderLine
						.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINES).item(0);

				if (!YFCObject.isVoid(eleShipmentLines)) {
					NodeList nlShipmentLines = eleShipmentLines
							.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);
					log.verbose("ShipmentLine Count :: " + nlShipmentLines.getLength());
				
					for (int iShipmenLineCount = 0; iShipmenLineCount < nlShipmentLines
							.getLength(); iShipmenLineCount++) {
						dNewLineQuantity = checkShipmentCancellationEligibility(dNewLineQuantity, bIsFireArm,
								nlShipmentLines, iShipmenLineCount);
					}
				}
				Element eleOrderStatuses = (Element) eleOrderLine.getElementsByTagName("OrderStatuses").item(0);
				if (!YFCObject.isVoid(eleOrderStatuses)) {
					dNewLineQuantity = checkOrderLineCancellationEligibility(dNewLineQuantity, eleOrderStatuses);
				}
				log.verbose(" dCurrentQuantity :: " + dCurrentQuantity + "dNewLineQuantity :: " + dNewLineQuantity);
				cancelFirearmOrderLines(dCurrentQuantity, dNewLineQuantity, docChangeOrderInp, eleChangeOrderLines,
						eleOrderLine, bIsFireArm, strOrderLineKey);
			}
			log.verbose("docChangeOrderInp XML" + XMLUtil.getXMLString(docChangeOrderInp));
			docChangeOrderOut = processCancellation(env, docChangeOrderOut, docChangeOrderInp);
		}else {
			log.verbose("Order is completely shipped or cancelled. Ignore Order.");
		}

		log.endTimer("AcademyProcessHardDeclineFromGSM.processHardDecline()");
		return docChangeOrderOut;
	}

	private void cancelFirearmOrderLines(double dCurrentQuantity, double dNewLineQuantity, Document docChangeOrderInp,
			Element eleChangeOrderLines, Element eleOrderLine, boolean bIsFireArm, String strOrderLineKey) {
		if ((dCurrentQuantity > dNewLineQuantity) && bIsFireArm) {
			log.verbose("OrderLineKey being cancelled"
					+ eleOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
			prepareChangeOrderInp(docChangeOrderInp, eleChangeOrderLines, strOrderLineKey,
					Double.toString(dNewLineQuantity));
		}
	}

	private String getMinOrderStatus(Element eleOrderOut) {
		String strMinOrderStatus = eleOrderOut.getAttribute(AcademyConstants.ATTR_MIN_ORDER_STATUS);
		if (strMinOrderStatus.contains(".")) 
			strMinOrderStatus = strMinOrderStatus.substring(0, 4);
		return strMinOrderStatus;
	}

	private Document processCancellation(YFSEnvironment env, Document docChangeOrderOut, Document docChangeOrderInp)
			throws Exception {
		NodeList nlChangeOrderLines = XPathUtil.getNodeList(docChangeOrderInp.getDocumentElement(),
				"/Order/OrderLines/OrderLine");
		if (nlChangeOrderLines.getLength() > 0) {
			invokeChangeShipment(env);
			docChangeOrderOut = cancelDeclinedOrder(env, docChangeOrderInp);
		}
		return docChangeOrderOut;
	}

	private double checkOrderLineCancellationEligibility(double dNewLineQuantity, Element eleOrderStatuses) {
		NodeList nlOrderStatus = eleOrderStatuses.getElementsByTagName(AcademyConstants.ELE_ORDER_STATUS);
		log.verbose("nlOrderStatus Count :: " + nlOrderStatus.getLength());

		for (int iOrderStatus = 0; iOrderStatus < nlOrderStatus.getLength(); iOrderStatus++) {
			Element elOrderStatus = (Element) nlOrderStatus.item(iOrderStatus);
			log.verbose("elOrderStatus XML" + XMLUtil.getElementXMLString(elOrderStatus));
			String strStatus = elOrderStatus.getAttribute(AcademyConstants.ATTR_STATUS);
			String strStatusQty = elOrderStatus.getAttribute(AcademyConstants.ATTR_STAT_QTY);

			double dStatusQty = Double.parseDouble(strStatusQty);
			log.verbose("Order line Qty is eligible for cancellation :: " + strStatus);
			log.verbose("OrderLine before :: dNewLineQuantity :: " + dNewLineQuantity
					+ " before :: dStatusQty :: " + dStatusQty);
			if (dStatusQty > 0 && !listOrderLineStatus.contains(strStatus)
					&& Integer.parseInt(strStatus.substring(0, 4)) < 3700) {
				log.verbose("Order line Qty is eligible for cancellation :: " + strStatus);
				if (!strStatus.startsWith("3350"))
					dNewLineQuantity = dNewLineQuantity - dStatusQty;
			}
		}
		return dNewLineQuantity;
	}

	private double checkShipmentCancellationEligibility(double dNewLineQuantity, boolean bIsFireArm,
			NodeList nlShipmentLines, int iShipmenLineCount) throws Exception {
		double dShipmentLineQty;
		Element eleShipmentLine = (Element) nlShipmentLines.item(iShipmenLineCount);
		Element eleShpment = (Element) eleShipmentLine
				.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);

		if ((!YFCObject.isVoid(eleShpment)) && (!(AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE
				.equals(eleShpment.getAttribute(AcademyConstants.ATTR_DOC_TYPE))
				&& AcademyConstants.STR_SHIP_TO_STORE
						.equals(eleShpment.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE))))) {
			String strShipmentLineKey = eleShipmentLine
					.getAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY);
			dShipmentLineQty = Double
					.parseDouble(eleShipmentLine.getAttribute(AcademyConstants.ATTR_QUANTITY));
			Element eleShipment = (Element) eleShipmentLine
					.getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);

			String strShipmentKey = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			String strStatus = eleShipment.getAttribute(AcademyConstants.ATTR_STATUS);
			String strManifestKey = eleShipment.getAttribute(AcademyConstants.ATTR_MANIFEST_KEY);
			String strContainerNo = SCXmlUtil.getXpathAttribute(eleShipment,
					"Containers/Container/@ContainerNo");

			if (dShipmentLineQty > 0 && !listShipmentStatus.contains(strStatus)
					&& YFCObject.isVoid(strManifestKey) && YFCCommon.isVoid(strContainerNo)
					&& (bIsFireArm) && (Integer.parseInt(strStatus.substring(0, 4)) <= 1400)) {

				log.verbose("Shipment Line is eligible for cancellation :: ");
				addToChangeShipmentMap(strShipmentKey, strShipmentLineKey);
				dNewLineQuantity = dNewLineQuantity - dShipmentLineQty;
			}
		}
		return dNewLineQuantity;
	}

	private boolean isFirearmPresent(Element eleOrderLine, boolean bIsFireArm) {
		String strFulfillmentType = eleOrderLine.getAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE);
		String strPackListType = eleOrderLine.getAttribute(AcademyConstants.STR_PACK_LIST_TYPE);

		if (!YFCObject.isVoid(strFulfillmentType) && "STS".equals(strFulfillmentType)
				&& (!YFCObject.isVoid(strPackListType) && "FA".equals(strPackListType))) {
			bIsFireArm = true;
		}
		return bIsFireArm;
	}
	
	/** This method executes changeOrder API to cancel the order line quantity
	 * Sample Input XML
	 * 	<Order DocumentType="0001" IgnoreOrdering="Y" ModificationReasonCode="Authorization Failure" OrderHeaderKey="" Override="Y" changeOrder="Y">
		    <OrderLines>
		        <OrderLine Action="" OrderLineKey="" OrderedQty="0"/>
				 <OrderLine Action="" OrderLineKey="" OrderedQty="2"/>
		    </OrderLines>
		</Order>
	 * @param env
	 * @param inDoc
	 * @return
	 */
	private Document cancelDeclinedOrder(YFSEnvironment env, Document docChangeOrderInp) {

		log.verbose("Begin of AcademyCancelHardDeclinedOrders.cancelDeclinedOrder() method");
		Document docOutChangeOrder = null;
		Element eleOrder = null;
		
		try {
			eleOrder = docChangeOrderInp.getDocumentElement();
			eleOrder.setAttribute(AcademyConstants.ATTR_IGNORE_ORDERING, AcademyConstants.ATTR_Y);
			eleOrder.setAttribute(AcademyConstants.ATTR_MOD_REASON_CODE, "Authorization Failure");
			eleOrder.setAttribute(AcademyConstants.ATTR_OVERRIDE, AcademyConstants.ATTR_Y);
			eleOrder.setAttribute(AcademyConstants.ATTR_CHANGE_ORDER, AcademyConstants.ATTR_Y);			

			log.verbose("Input to changeOrder :: "+XMLUtil.getXMLString(docChangeOrderInp));
			docOutChangeOrder = AcademyUtil.invokeAPI(env,AcademyConstants.API_CHANGE_ORDER, docChangeOrderInp);
			log.verbose("Output of changeOrder ::"+ XMLUtil.getXMLString(docOutChangeOrder));
			log.verbose("End of AcademyCancelHardDeclinedOrders.cancelDeclinedOrder() method");

		} catch (Exception e) {			
			log.info("Exception occured in AcademyCancelHardDeclinedOrders.cancelDeclinedOrder() method for Order:");
			log.verbose(e.getMessage());
			throw new YFSException(e.getMessage());
		}
		log.verbose("End of AcademyCancelHardDeclinedOrders.cancelDeclinedOrder() method");
		return docOutChangeOrder;
	}
	
	/** This prepares OrderLine element for changeOrder Input
	 * <OrderLine Action="" OrderLineKey="201702150011522424720620" OrderedQty="0"/>
	 * @param docInXML
	 * @param newChangeOrderQty
	 * @return
	 */
	private Document prepareChangeOrderInp(Document docChangeOrderInp, Element eleOrderLines, String strOrderLineKey, String strQuantiy) {
		log.verbose("Begin of AcademyCancelHardDeclinedOrders.prepareChangeOrderInp() method");

		Element eleOrderLine = docChangeOrderInp.createElement(AcademyConstants.ELEM_ORDER_LINE);
		eleOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY, strOrderLineKey);
		eleOrderLine.setAttribute(AcademyConstants.ATTR_ORDERED_QTY, strQuantiy);
		eleOrderLines.appendChild(eleOrderLine);

		log.verbose("End of AcademyCancelHardDeclinedOrders.prepareChangeOrderInp() method");
		return docChangeOrderInp;

	}

	/**
	 * Add changeShipment input to has map for each shipment to avoid multiple api
	 * call.
	 * 
	 * @param eleShipment
	 * @throws ParserConfigurationException
	 */
	private void addToChangeShipmentMap(String strShipmentKey, String strShipmentLineKey)
			throws Exception {
		log.verbose("Begin of AcademyCancelHardDeclinedOrders.addToChangeShipmentMap() method");

		Document docChangeShipmentInput = null;
		Element eleShipmentLines = null;
		if (hmpChangeShipment.containsKey(strShipmentKey)) {
			docChangeShipmentInput = hmpChangeShipment.get(strShipmentKey);
			eleShipmentLines = XMLUtil.getElementByXPath(docChangeShipmentInput, "/Shipment/ShipmentLines");
		} else {
			docChangeShipmentInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			docChangeShipmentInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,
					strShipmentKey);
			docChangeShipmentInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_OVERRIDE_MODIFICATION_RULES,
					AcademyConstants.STR_YES);
			docChangeShipmentInput.getDocumentElement().setAttribute("Action", AcademyConstants.VAL_CANCEL);
			docChangeShipmentInput.getDocumentElement().setAttribute("CancelShipmentOnZeroTotalQuantity",
					AcademyConstants.STR_YES);

			eleShipmentLines = docChangeShipmentInput.createElement(AcademyConstants.ELE_SHIPMENT_LINES);
			docChangeShipmentInput.getDocumentElement().appendChild(eleShipmentLines);

			Element eleExtn = docChangeShipmentInput.createElement(AcademyConstants.ELE_EXTN);
			eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_SHORTPICK_REASON_CODE, "Authorization Failure");
			docChangeShipmentInput.getDocumentElement().appendChild(eleExtn);
		}

		Element eleShipmentLine = docChangeShipmentInput.createElement(AcademyConstants.ELE_SHIPMENT_LINE);
		eleShipmentLine.setAttribute(AcademyConstants.ATTR_SHIPMENT_LINE_KEY, strShipmentLineKey);
		eleShipmentLine.setAttribute(AcademyConstants.ATTR_QUANTITY, "0");
		eleShipmentLines.appendChild(eleShipmentLine);

		hmpChangeShipment.put(strShipmentKey, docChangeShipmentInput);

		log.verbose("End of AcademyCancelHardDeclinedOrders.addToChangeShipmentMap() method");
	}
	
	private void invokeChangeShipment(YFSEnvironment env) {
		log.verbose("Begin of AcademyCancelHardDeclinedOrders.invokeChangeShipment() method");
		Document docInChangeShipment = null;
		String strShipmentKey = "";
		try{
			Iterator<Entry<String, Document>> itChangeShipment = hmpChangeShipment.entrySet().iterator();
			while (itChangeShipment.hasNext()) {
				Map.Entry<String, Document> meChangeShipmentStatus = itChangeShipment.next();
				docInChangeShipment = meChangeShipmentStatus.getValue();
				strShipmentKey = meChangeShipmentStatus.getKey();

				log.verbose("Input to changeShipment API for ShipmentNo:" +strShipmentKey+" :: \n"+XMLUtil.getXMLString(docInChangeShipment));
				AcademyUtil.invokeAPI(env,AcademyConstants.API_CHANGE_SHIPMENT, docInChangeShipment);

			}
			log.verbose("End of AcademyCancelHardDeclinedOrders.invokeChangeShipment() method");

		} catch (Exception e) {
			log.info("Exception occured in AcademyCancelHardDeclinedOrders.invokeChangeShipment() method for ShipmentKey :" + strShipmentKey);
			log.verbose(e.getMessage());
			throw new YFSException(e.getMessage());
		}
	}

	private Document callGetCompleteOrderDetails(YFSEnvironment env, String strSalesOrderNo) throws Exception {

		log.beginTimer("Begin of AcademyCancelHardDeclinedOrders.callGetCompleteOrderDetails() method");
		Document docGetCompleteOrderDetailsInput = null;
		Document docGetCompleteOrderDetailsOut = null;
		Element eleOrder = null;

		docGetCompleteOrderDetailsInput = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		eleOrder = docGetCompleteOrderDetailsInput.getDocumentElement();
		eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_NO, strSalesOrderNo);
		eleOrder.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.PRIMARY_ENTERPRISE);

		log.verbose("Input of getCompleteOrderDetails :: " + XMLUtil.getXMLString(docGetCompleteOrderDetailsInput));
		docGetCompleteOrderDetailsOut = AcademyUtil.invokeService(env,
				AcademyConstants.SERV_ACAD_GET_HARD_DECLINED_ORDER_DETAILS_SERVICE, docGetCompleteOrderDetailsInput);
		log.verbose("Output of getCompleteOrderDetails ::" + XMLUtil.getXMLString(docGetCompleteOrderDetailsOut));

		log.endTimer("End of AcademyCancelHardDeclinedOrders.callGetCompleteOrderDetails() method");
		return docGetCompleteOrderDetailsOut;
	}
}
