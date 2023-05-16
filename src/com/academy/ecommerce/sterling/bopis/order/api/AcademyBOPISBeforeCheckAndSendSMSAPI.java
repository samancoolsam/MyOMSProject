package com.academy.ecommerce.sterling.bopis.order.api;

/**#########################################################################################
*
* Project Name					: OMNI-40283,OMNI-40285
* Module						: OMNI-38560
* Author						: C0027905
* Author Group					: EVEREST
* Date							: 29-May-2021 
* Description	: This class publish SMS to ESB queue when BOPIS order is moved
* to Ready For Customer Pick Status and Order Has Delayed Lines.
* 
* ---------------------------------------------------------------------------------
* Date			Author			Version#		Remarks/Description
* ---------------------------------------------------------------------------------
* 29-May-2021 Everest	1.0			Updated version
* #########################################################################################*/

import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyBOPISBeforeCheckAndSendSMSAPI implements YIFCustomApi {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyBOPISBeforeCheckAndSendSMSAPI.class);
	private Properties props;
	private final String GET_SHIPMENT_LIST_OUT_TEMP = "<Shipments><Shipment ShipmentKey=\"\" ShipmentNo=\"\" Status=\"\" StatusDate=\"\" SCAC=\"\" ScacAndService=\"\" CarrierServiceCode=\"\" OrderNo=\"\" DeliveryMethod=\"\" ActualShipmentDate=\"\" ShipNode=\"\" >\r\n"
			+ "		<Extn ExtnIsCurbsidePickupOpted=\"\" />\r\n" + "		<ShipmentLines>\r\n"
			+ "			<ShipmentLine OrderHeaderKey=\"\" Quantity=\"\" OrderNo=\"\" ShipmentLineKey=\"\">\r\n"
			+ "				<OrderLine OriginalOrderedQty=\"\" OrderedQty=\"\" DeliveryMethod=\"\" FulfillmentType=\"\" >\r\n"
			+ "					<Extn ExtnWCOrderItemIdentifier=\"\" />\r\n" + "					<OrderStatuses>\r\n"
			+ "						<OrderStatus StatusDescription=\"\" />\r\n"
			+ "					</OrderStatuses>\r\n" + "				</OrderLine>\r\n"
			+ "			</ShipmentLine>\r\n" + "		</ShipmentLines>\r\n"
			+ "		<ToAddress EMailID=\"\" FirstName=\"\" LastName=\"\" MiddleName=\"\" PersonID=\"\"  AddressLine1=\"\" City=\"\" State=\"\" ZipCode=\"\"/>\r\n"
			+ "		<OrderInvoiceList>\r\n" + "			<OrderInvoice ActualFreightCharge=\"\" AmountCollected=\"\"\r\n"
			+ "				DateInvoiced=\"\" HeaderTax=\"\" InvoiceNo=\"\" InvoiceType=\"\"\r\n"
			+ "				LineSubTotal=\"\" MasterInvoiceNo=\"\" OrderHeaderKey=\"\"\r\n"
			+ "				OrderInvoiceKey=\"\" OrderNo=\"\" OtherCharges=\"\" \r\n"
			+ "				ShipmentKey=\"\" Status=\"\" TotalAmount=\"\" TotalTax=\"\" />\r\n"
			+ "		</OrderInvoiceList>\r\n" + "		<ShipmentStatusAudits>\r\n"
			+ "			<ShipmentStatusAudit ShipmentStatusAuditKey=\"\" OldStatus=\"\" NewStatus=\"\" OldStatusDate=\"\" NewStatusDate=\"\" ReasonCode=\"\" ReasonText=\"\"/>\r\n"
			+ "		</ShipmentStatusAudits>\r\n" + "		<ShipNode NodeType=\"\" Localecode=\"\">\r\n"
			+ "			<ShipNodePersonInfo AddressLine1=\"\" City=\"\" Country=\"\" FirstName=\"\" State=\"\" ZipCode=\"\" /> \r\n"
			+ "		</ShipNode>	\r\n" + "	</Shipment></Shipments>";

	public Document beforeSendSMS(YFSEnvironment env, Document inDoc) throws Exception {

		log.verbose("Start of method checkAndSendSMS : input is :: " + XMLUtil.getXMLString(inDoc));

		boolean isEligibleToSendSMS = false;

		Element eleIndoc = inDoc.getDocumentElement();
		NodeList currentShipmentLines = getCurrentShipmentLines(eleIndoc);
		for (int i = 0; i < currentShipmentLines.getLength(); i++) {
			Element shipmentLine = (Element) currentShipmentLines.item(i);
			Element eleOrderLine = getOrderLine(eleIndoc, shipmentLine.getAttribute("OrderLineKey"));
			String strMinLineStatus = eleOrderLine.getAttribute("MinLineStatus").replace(".",",");;
			String [] aaMinLineStatus=strMinLineStatus.split(",");
			Integer intMinLineStatus=Integer.parseInt(aaMinLineStatus[0]);
			String strExtnOriginalFulfillmentType = SCXmlUtil.getXpathAttribute(eleOrderLine,
					"Extn/@ExtnOriginalFulfillmentType");
			if ("BOPIS".equals(strExtnOriginalFulfillmentType) &  (intMinLineStatus < 3350 | "9000".equals(strMinLineStatus))) {
				isEligibleToSendSMS = true;
				log.verbose("isSTSCancelledLineExist :: "+isEligibleToSendSMS);
				break;
			}

		}
		log.verbose("isRFCPLineExist :: "+isRFCPLineExist(eleIndoc, currentShipmentLines));
		if (isRFCPLineExist(eleIndoc, currentShipmentLines) & isEligibleToSendSMS) {

			String currentShipmentKey = XPathUtil.getString(eleIndoc, "/Order/@CurrentShipmentKey");
			if (!YFCObject.isVoid(currentShipmentKey)) {
				Document docGetShipmentListInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
				Element eleRoot = docGetShipmentListInput.getDocumentElement();
				eleRoot.setAttribute(AcademyConstants.SHIPMENT_KEY, currentShipmentKey);
				log.verbose("getShpmentList Template"
						+ SCXmlUtil.getString(XMLUtil.getDocument(GET_SHIPMENT_LIST_OUT_TEMP)));
				log.verbose("getShpmentList Input" + SCXmlUtil.getString(XMLUtil.getDocument(GET_SHIPMENT_LIST_OUT_TEMP)));
				
				env.setApiTemplate("getShipmentList", XMLUtil.getDocument(GET_SHIPMENT_LIST_OUT_TEMP));
				log.verbose("Input xml for getShipmentList api:"
						+ com.academy.util.xml.XMLUtil.getXMLString(docGetShipmentListInput));
				Document docgetShipmentListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST,
						docGetShipmentListInput);
				log.verbose("Output xml for getShipmentList api:"
						+ com.academy.util.xml.XMLUtil.getXMLString(docgetShipmentListOutput));
				env.clearApiTemplate("getShipmentList");
				Element eleShipment = SCXmlUtil.getXpathElement(docgetShipmentListOutput.getDocumentElement(),
						"Shipment");
				if (!YFCCommon.isVoid(eleShipment)) {
					Document docShipment = SCXmlUtil.createFromString(SCXmlUtil.getString(eleShipment));
					docShipment.getDocumentElement().setAttribute("HasRFCPLines", "true");
					return docShipment;
				} else {
					inDoc.getDocumentElement().setAttribute("HasRFCPLines", "false");
					return inDoc;
				}
			} else {

				inDoc.getDocumentElement().setAttribute("HasRFCPLines", "false");
				return inDoc;
			}

		}

		inDoc.getDocumentElement().setAttribute("HasRFCPLines", "false");
		return inDoc;

	}

	private boolean isRFCPLineExist(Element eleIndoc, NodeList currentShipmentLines) throws Exception {
		for (int i = 0; i < currentShipmentLines.getLength(); i++) {
			Element shipmentLine = (Element) currentShipmentLines.item(i);
			Element eleOrderLine = getOrderLine(eleIndoc, shipmentLine.getAttribute("OrderLineKey"));
			System.out.println();
			if (checkRFCPLine(eleOrderLine) || checkPartialRFCPLine(eleOrderLine)) {
				return true;
			}
		}
		return false;

	}

	private boolean checkRFCPLine(Element eleOrderLine) throws Exception {
		return !YFCObject.isVoid(eleOrderLine) && "PICK".equals(eleOrderLine.getAttribute("DeliveryMethod"))
				&& "3350.400".equals(eleOrderLine.getAttribute("MaxLineStatus"))
				&& (YFCObject.isVoid(getExtnOriginalFulfillmentType(eleOrderLine)))
				&& (!YFCObject.isVoid(eleOrderLine.getAttribute("OrderedQty"))
						&& !YFCObject.isVoid(eleOrderLine.getAttribute("OriginalOrderedQty"))
						&& Double.parseDouble(eleOrderLine.getAttribute("OrderedQty")) == Double
								.parseDouble(eleOrderLine.getAttribute("OriginalOrderedQty"))) ? true : false;
	}

	private boolean checkPartialRFCPLine(Element eleOrderLine) throws Exception {
		double dOrderedQty = 0.00;
		if (!YFCObject.isVoid(eleOrderLine.getAttribute("OrderedQty"))) {
			dOrderedQty = Double.parseDouble(eleOrderLine.getAttribute("OrderedQty"));
		}
		double dOriginalOrderedQty = Double.parseDouble(eleOrderLine.getAttribute("OriginalOrderedQty"));
		return !YFCObject.isVoid(eleOrderLine) && "PICK".equals(eleOrderLine.getAttribute("DeliveryMethod"))
				&& "3350.400".equals(eleOrderLine.getAttribute("MaxLineStatus"))
				&& (YFCObject.isVoid(getExtnOriginalFulfillmentType(eleOrderLine)))
				&& (!YFCObject.isDoubleVoid(dOrderedQty) && !YFCObject.isVoid(dOriginalOrderedQty) 
						&&  dOrderedQty != dOriginalOrderedQty) ? true : false;
	}

	private String getExtnOriginalFulfillmentType(Element eleOrderLine) throws Exception {
		String olkey = eleOrderLine.getAttribute("OrderLineKey");
		return XMLUtil.getString(eleOrderLine,
				"/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/Extn/@ExtnOriginalFulfillmentType");
	}

	private Element getOrderLine(Element eleIndoc, String orderlineKey) throws Exception {
		return (Element) XPathUtil.getNode(eleIndoc,
				"/Order/OrderLines/OrderLine[@OrderLineKey='" + orderlineKey + "']");
	}

	private NodeList getCurrentShipmentLines(Element eleIndoc) throws Exception {
		String currentShipmentKey = XPathUtil.getString(eleIndoc, "/Order/@CurrentShipmentKey");
		return XPathUtil.getNodeList(eleIndoc,
				"/Order/Shipments/Shipment[@ShipmentKey='" + currentShipmentKey + "']/ShipmentLines/ShipmentLine");
	}

	public void setProperties(Properties props) throws Exception {
		this.props = props;

	}
}