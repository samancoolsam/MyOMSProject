package com.academy.ecommerce.sterling.dsv.shipment;

import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * This class will be invoked on successful create and confirm inbound DSV
 * shipment
 * 
 * @author Christopher White (09082016)
 * 
 */

public class AcademyDSVShipConfirmToRMS implements YIFCustomApi {
	// tatic Document getCompleteOrderDetailsOutDoc = null;

	private static Logger log = Logger
			.getLogger(AcademyDSVShipConfirmToRMS.class.getName());

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
	}

	/**
	 * On successful completion of create and confirm shipment, the out going
	 * XML will be passed a XSL translator then passed to this API. This API
	 * will obtain OrderNo from incoming XML, the API getCompleteOrderDetails
	 * will be invoked using OrderNo. CustomerOrderNo (OrderName) and
	 * RMSVenderNo (SupplierName) will be used from API outdoc. CustomerOrderNo
	 * and and RMSVendorNo will be appended to original incoming XML and
	 * returned.
	 * 
	 * This code takes in xml, invokes getCompleteOrderDetails API for OrderName
	 * and supplierName, appends to oringal xml to return.
	 * 
	 * @param env
	 * @param inDoc
	 * @return inDoc with new attributes/vaules (RMSVendorNo, CustomerOrderNo)
	 * 
	 * @throws Exeception
	 *             (General)
	 */
	public Document dsvShipConfirmMessage(YFSEnvironment env, Document inDoc)
			throws Exception {

		Document docInGetCompleteOrderDetails = null;
		Document docOutGetCompleteOrderDetails = null;
		NodeList orderReleaseNodeList = null;

		try {
			// Get element from incoming XML
			Element eleShipment = inDoc.getDocumentElement();

			// Get OrderNo to use in API call
			String strPOOrderNo = eleShipment
					.getAttribute(AcademyConstants.ATTR_ORDER_NO);
			String strShipKey = eleShipment
					.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);

			String strCustOrderNo = "";
			String strRmsVenNo = "";

			log.verbose("Input document -->" + XMLUtil.getXMLString(inDoc)
					+ "<---");

			// Created API doc and set attributes for API call
			docInGetCompleteOrderDetails = XMLUtil
					.createDocument(AcademyConstants.ELE_ORDER);
			docInGetCompleteOrderDetails.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_ORDER_NO, strPOOrderNo);
			docInGetCompleteOrderDetails.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_DOC_TYPE,
					AcademyConstants.DOCUMENT_TYPE_SHIPMENT);
			docInGetCompleteOrderDetails.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_ENTERPRISE_CODE,
					AcademyConstants.DSV_ENTERPRISE_CODE);
			// Create API template
			Document outputTemplate = XMLUtil
					.getDocument("<Order OrderNo=\"\" OrderName=\"\" >"
							+ "<Shipments><Shipment ShipmentNo=\"\" >"
							+ "<ShipmentLines><ShipmentLine ShipmentKey=\"\" ItemId=\"\" >"
							+ "<OrderRelease SupplierName=\"\" />"
							+ "</ShipmentLine>" + "</ShipmentLines>"
							+ "</Shipment>" + "</Shipments>" + "</Order>");

			log.verbose("Input document to getCompleteOrderDetails API -->"
					+ XMLUtil.getElementXMLString(docInGetCompleteOrderDetails
							.getDocumentElement()));
			// Set API template
			env.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS,
					outputTemplate);
			// invoke getCompleteOrderDetails API
			docOutGetCompleteOrderDetails = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS,
					docInGetCompleteOrderDetails);
			// Clear API template
			env
					.clearApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS);
			log.verbose("getCompleteOrderDetails API Output document: --> "
					+ XMLUtil.getXMLString(docOutGetCompleteOrderDetails)
					+ "<--");

			// Get CustomerOrderNo from invoked API outDoc
			Element eleCompleteOrderDeatials = docOutGetCompleteOrderDetails
					.getDocumentElement();
			strCustOrderNo = eleCompleteOrderDeatials
					.getAttribute(AcademyConstants.ATTR_ORDER_NAME);

			// Get RMSVendorNo from invoked API outDoc
			orderReleaseNodeList = XMLUtil.getNodeList(
					eleCompleteOrderDeatials,
					"/Order/Shipments/Shipment/ShipmentLines/ShipmentLine[@ShipmentKey='"
							+ strShipKey + "']/OrderRelease");

			if (orderReleaseNodeList.getLength() > 0) {
				Element eleOrderRelease = (Element) orderReleaseNodeList
						.item(0);
				strRmsVenNo = XPathUtil.getString(eleOrderRelease,
						"@SupplierName");
			}

			// Append CustomerOrderNO and RMSVendorNo to original incoming doc
			eleShipment.setAttribute("CustomerOrderNo", strCustOrderNo);
			eleShipment.setAttribute("RMSVendorNo", strRmsVenNo);

		} catch (RuntimeException e) {
			log
					.verbose("RuntimeExeption thrown in method: dsvShipConfirmMessage");
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		} catch (ParserConfigurationException e) {
			log
					.verbose("ParserConfiguationExeption thrown in method: dsvShipConfirmMessage");
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}

		return inDoc;
	}
}