package com.academy.ecommerce.sterling.interfaces.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyReturnOrderUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/*##################################################################################
*
* Project Name                : POD July Release
* Module                      : OMS
* Author                      : CTS
* Date                        : 01-JULY-2021
* Description                 : This class does following
*                               1.Process the entry of Acad_Tracking_Updates, that having
*                                 RETURN_RECEIVE as status based on common code flag.
*                               2.Receive the return order that tracking no associated with. 
*                               3.Moves Return order 'Receipt Closed' status.
*                               
*                               
*
*
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author                  Version#       Remarks/Description                     
* ---------------------------------------------------------------------------------
* 01-JULY-2021     CTS                      1.0            Initial version
* ##################################################################################*/

public class AcademyProcessReturnOrderTrackingUpdates implements YIFCustomApi {

	private static YFCLogCategory logger = YFCLogCategory.instance(AcademyProcessReturnOrderTrackingUpdates.class);

	private Properties props;

	@Override
	public void setProperties(Properties props) throws Exception {
		this.props = props;

	}

	/*
	 * Sample Input ::
	 * 
	 * <AcadTrackingUpdates CarrierUpdateTrackerKey="20210701071515224622922"
	 * Createprogid="AcademyUploadQVDServer" Createts="2021-07-01T07:15:15-05:00"
	 * Createuserid="AcademyUploadQVDServer" InvoiceNo="" IsProcessed="N" Lockid="0"
	 * Modifyprogid="AcademyUploadQVDServer" Modifyts="2021-07-01T07:15:15-05:00"
	 * Modifyuserid="AcademyUploadQVDServer" ProNo="" ShipmentNo=""
	 * Status="RETURN_RECEIVE" StatusAddress="" StatusCity=""
	 * StatusDate="2021-06-30" StatusDescription="PU" StatusLocation=""
	 * StatusZipCode="" TrackingNo="183062939287"/>
	 */
	public Document processReturnOrderTrackingUpdates(YFSEnvironment env, Document docInp) throws Exception {

		String strReturnOrderUpdateFlag = null;
		String strEligibleReturnOrderStatuses = null;

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.processReturnOrderTrackingUpdates() :: Begin");

		logger.verbose("Input to processReturnOrderTrackingUpdates() :: " + XMLUtil.getXMLString(docInp));

		Document docReturnCarrierUpdate = returnCarrierUpdates(env, AcademyConstants.STR_RET_CARRIER_UPDATES,
				AcademyConstants.PRIMARY_ENTERPRISE);

		Node nReturnCarrierUpdatesCC = XPathUtil.getNode(docReturnCarrierUpdate, AcademyConstants.ELE_COMMON_CODE_LIST);

		if (null != nReturnCarrierUpdatesCC) {

			strReturnOrderUpdateFlag = XPathUtil.getString(nReturnCarrierUpdatesCC, AcademyConstants.XPATH_RETURN_FLAG);

			strEligibleReturnOrderStatuses = XPathUtil.getString(nReturnCarrierUpdatesCC,
					AcademyConstants.XPATH_RETURN_ORDER_STATUS);
		}

		if (AcademyConstants.STR_YES.equalsIgnoreCase(strReturnOrderUpdateFlag)) {

			logger.verbose("The flag to process return order tracking updates is ON :: ");

			Element eleAcadTrackingUpdates = docInp.getDocumentElement();

			String strTrackingNo = eleAcadTrackingUpdates.getAttribute(AcademyConstants.ATTR_TRACKING_NO);

			Document docGetOrderListOut = getReturnOrderDetailsUsingTrackingNo(env, strTrackingNo);

			if (null != docGetOrderListOut) {

				Element eleRetOrder = (Element) docGetOrderListOut.getElementsByTagName(AcademyConstants.ELE_ORDER)
						.item(0);

				if (null != eleRetOrder) {

					logger.verbose("Return Order details :: " + XMLUtil.getElementXMLString(eleRetOrder));

					String strMaxOrderStatus = eleRetOrder.getAttribute(AcademyConstants.ATTR_MAX_ORDER_STATUS);

					if (strEligibleReturnOrderStatuses.contains(strMaxOrderStatus)) {

						logger.verbose("Return order is eligible to receive :: ");

						updateAndReceiveReturnOrder(env, docGetOrderListOut);

						modifyCarrierStatusTracker(env, docInp, AcademyConstants.STR_YES);

					} else {

						logger.verbose("Return order is ineligible to receive, ignoring the message :: ");

						modifyCarrierStatusTracker(env, docInp, AcademyConstants.STR_IGNORED);

					}

				}
			} else {

				logger.verbose("Return order details not found :: ");

				modifyCarrierStatusTracker(env, docInp, AcademyConstants.STR_IGNORED);
			}
		} else {

			logger.verbose("The flag to process return order tracking updates is OFF :: ");

			modifyCarrierStatusTracker(env, docInp, AcademyConstants.STR_IGNORED);
		}

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.processReturnOrderTrackingUpdates() :: End");

		return docInp;
	}

	private Document returnCarrierUpdates(YFSEnvironment env, String strCodeType, String strOrganizationCode)
			throws Exception {

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.returnCarrierUpdates() :: Begin");

		Document docCommonCodeListIn = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
		docCommonCodeListIn.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, strCodeType);
		docCommonCodeListIn.getDocumentElement().setAttribute(AcademyConstants.ORGANIZATION_CODE, strOrganizationCode);

		logger.verbose("Input - getCommonCOdeList API :: " + XMLUtil.getXMLString(docCommonCodeListIn));

		Document getCommonCodeListOPTempl = XMLUtil
				.getDocument("<CommonCode CodeValue='' CodeShortDescription='' CodeLongDescription='' />");

		env.setApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST, getCommonCodeListOPTempl);

		Document docCommonCodeListOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMONCODE_LIST,
				docCommonCodeListIn);
		env.clearApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST);

		if (null != docCommonCodeListOut) {

			logger.verbose("Output - getCommonCodeList API :: " + XMLUtil.getXMLString(docCommonCodeListOut));
		}

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.returnCarrierUpdates() :: End");

		return docCommonCodeListOut;

	}

	private Document getReturnOrderDetailsUsingTrackingNo(YFSEnvironment env, String strTrackingNo) throws Exception {

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.getReturnOrderDetailsUsingTrackingNo() :: Begin");

		Document docGetOrderListOut = null;
		Document docGetOrderListInp = prepareGetOrderListInp(strTrackingNo);

		if (!YFCObject.isNull(docGetOrderListInp)) {

			env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST,
					AcademyConstants.STR_TEMPLATEFILE_GET_ORDER_LIST_RETURN_UPDATES);

			docGetOrderListOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, docGetOrderListInp);

			env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);

			logger.verbose("getOrderList output :: " + XMLUtil.getXMLString(docGetOrderListOut));
		}

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.getReturnOrderDetailsUsingTrackingNo() :: End");

		return docGetOrderListOut;
	}

	private Document prepareGetOrderListInp(String strTrackingNo) throws Exception {

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.prepareGetOrderListInp() :: Begin");

		Document docGetOrderListInp = null;

		if (!StringUtil.isEmpty(strTrackingNo)) {

			docGetOrderListInp = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			Element eleOrder = docGetOrderListInp.getDocumentElement();
			eleOrder.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.STR_RETURN_DOCTYPE);
			Element eleExtn = docGetOrderListInp.createElement(AcademyConstants.ELE_EXTN);
			eleExtn.setAttribute(AcademyConstants.ATTR_EXTN_TRACKING_NO, strTrackingNo);
			eleOrder.appendChild(eleExtn);

			logger.verbose("getOrderList input :: " + XMLUtil.getXMLString(docGetOrderListInp));
		}

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.prepareGetOrderListInp() :: End");

		return docGetOrderListInp;
	}

	private void updateAndReceiveReturnOrder(YFSEnvironment env, Document docReturnOrderDetails) throws Exception {

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.updateAndReceiveReturnOrder() :: Begin");

		Element eleOrder = (Element) docReturnOrderDetails.getElementsByTagName(AcademyConstants.ELE_ORDER).item(0);
		Element eleOrdExtn = XmlUtils.getChildElement(eleOrder, AcademyConstants.ELE_EXTN);

		NodeList nlRetOrdLines = XMLUtil.getNodeListByXPath(docReturnOrderDetails,
				"/OrderList/Order/OrderLines/OrderLine");

		if (nlRetOrdLines.getLength() > 0) {

			logger.verbose("Return order lines exist :: ");

			boolean bRemoveReturnShippingCharge = false;

			HashMap<String, Double> hmReturnLineItems = new HashMap<String, Double>();

			Element eleOrderLine = (Element) eleOrder.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE).item(0);

			logger.verbose("Return order line :: " + XMLUtil.getElementXMLString(eleOrderLine));

			String strSalesOrderHeaderKey = eleOrderLine
					.getAttribute(AcademyConstants.ATTR_DERIVED_FROM_ORDER_HEADER_KEY);

			Document docCommonCodeList = AcademyReturnOrderUtil.getRefundableReasonCodeValuesList(env);

			logger.verbose("CommonCodeList output :: " + XMLUtil.getXMLString(docCommonCodeList));

			Document docChangeOrderInp = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			Element eleChangeOrderInp = docChangeOrderInp.getDocumentElement();

			eleChangeOrderInp.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,
					eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));
			eleChangeOrderInp.setAttribute(AcademyConstants.ATTR_OVERRIDE, AcademyConstants.STR_YES);

			Element eleChgOrdOrderLines = docChangeOrderInp.createElement(AcademyConstants.ELE_ORDER_LINES);
			eleChangeOrderInp.appendChild(eleChgOrdOrderLines);

			Document docReceiveOrderInp = XMLUtil.createDocument(AcademyConstants.ELE_RETURN_ORDERS);
			Element eleReceiveOrderInp = docReceiveOrderInp.getDocumentElement();

			Element eleReceiveOrder = docReceiveOrderInp.createElement(AcademyConstants.ELE_ORDER);
			eleReceiveOrderInp.appendChild(eleReceiveOrder);
			eleReceiveOrder.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,
					eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));

			Element eleReceiveOrderLines = docReceiveOrderInp.createElement(AcademyConstants.ELE_ORDER_LINES);
			eleReceiveOrder.appendChild(eleReceiveOrderLines);

			if (!AcademyReturnOrderUtil
					.isAcademyTrackingNo(eleOrdExtn.getAttribute(AcademyConstants.ATTR_EXTN_TRACKING_NO))) {

				bRemoveReturnShippingCharge = true;
			}

			for (int i = 0; i < nlRetOrdLines.getLength(); i++) {

				boolean bRemoveShippingCharge = false;

				Element eleRetOrderLine = (Element) nlRetOrdLines.item(i);

				logger.verbose("For loop - Return order line :: " + XMLUtil.getElementXMLString(eleRetOrderLine));

				Element eleRetItem = XmlUtils.getChildElement(eleRetOrderLine, AcademyConstants.ITEM);

				hmReturnLineItems.put(eleRetItem.getAttribute(AcademyConstants.ATTR_ITEM_ID),
						Double.parseDouble(eleRetOrderLine.getAttribute(AcademyConstants.ATTR_ORDERED_QTY)));

				// Return receive
				Element eleReceiveOrderLine = docReceiveOrderInp.createElement(AcademyConstants.ELE_ORDER_LINE);
				eleReceiveOrderLines.appendChild(eleReceiveOrderLine);
				eleReceiveOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY,
						eleRetOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
				eleReceiveOrderLine.setAttribute(AcademyConstants.ATTR_ITEM_ID,
						eleRetItem.getAttribute(AcademyConstants.ATTR_ITEM_ID));
				eleReceiveOrderLine.setAttribute(AcademyConstants.ATTR_UOM,
						eleRetItem.getAttribute(AcademyConstants.ATTR_UOM));
				eleReceiveOrderLine.setAttribute(AcademyConstants.ATTR_QUANTITY,
						eleRetOrderLine.getAttribute(AcademyConstants.ATTR_ORDERED_QTY));

				eleReceiveOrder.setAttribute(AcademyConstants.ATTR_RECV_NODE,
						eleRetOrderLine.getAttribute(AcademyConstants.ATTR_SHIP_NODE));

				if (!isReturnReasonCodeExist(docCommonCodeList,
						eleRetOrderLine.getAttribute(AcademyConstants.ATTR_RETURN_REASON))) {

					bRemoveShippingCharge = true;
				}

				// Change order
				Element eleChgOrdOrderLine = docChangeOrderInp.createElement(AcademyConstants.ELE_ORDER_LINE);
				eleChgOrdOrderLines.appendChild(eleChgOrdOrderLine);
				eleChgOrdOrderLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY,
						eleRetOrderLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
				eleChgOrdOrderLine.setAttribute(AcademyConstants.ATTR_SHIP_NODE,
						eleRetOrderLine.getAttribute(AcademyConstants.ATTR_SHIP_NODE));
				eleChgOrdOrderLine.setAttribute(AcademyConstants.ATTR_RETURN_REASON,
						eleRetOrderLine.getAttribute(AcademyConstants.ATTR_RETURN_REASON));

				logger.verbose("boolean - bRemoveShippingCharge :: " + bRemoveShippingCharge);
				logger.verbose("boolean - bRemoveReturnShippingCharge :: " + bRemoveReturnShippingCharge);

				// Change order - line charges
				Element eleRetOrderLineCharges = (Element) eleRetOrderLine
						.getElementsByTagName(AcademyConstants.ELE_LINE_CHARGES).item(0);
				eleRetOrderLineCharges.setAttribute(AcademyConstants.ATTR_RESET, AcademyConstants.STR_YES);

				removeReturnShippingCharge(eleRetOrderLineCharges, bRemoveReturnShippingCharge, bRemoveShippingCharge);
				XMLUtil.importElement(eleChgOrdOrderLine, eleRetOrderLineCharges);

				// Change order - line taxes
				Element eleRetOrderLineTaxes = (Element) eleRetOrderLine
						.getElementsByTagName(AcademyConstants.ELE_LINE_TAXES).item(0);
				eleRetOrderLineTaxes.setAttribute(AcademyConstants.ATTR_RESET, AcademyConstants.STR_YES);

				removeReturnShippingTaxes(eleRetOrderLineTaxes, bRemoveReturnShippingCharge, bRemoveShippingCharge);
				XMLUtil.importElement(eleChgOrdOrderLine, eleRetOrderLineTaxes);

			}

			// Get sales order invoice details
			String strSalesOrderInvoiceNo = fetchSalesOrderInvoiceNo(env, hmReturnLineItems, strSalesOrderHeaderKey);

			if (!StringUtil.isEmpty(strSalesOrderInvoiceNo)) {
				Element eleReferences = docChangeOrderInp.createElement(AcademyConstants.ELE_REFERENCES);
				eleChangeOrderInp.appendChild(eleReferences);

				Element eleReference = docChangeOrderInp.createElement(AcademyConstants.ELE_REFERENCE);
				eleReference.setAttribute(AcademyConstants.ATTR_NAME, AcademyConstants.STR_SALES_ORDER_INVOICE_NO);
				eleReference.setAttribute(AcademyConstants.ATTR_VALUE, strSalesOrderInvoiceNo);
				eleReferences.appendChild(eleReference);
			}

			// ChangeOrder API
			invokeChangeOrder(env, docChangeOrderInp);

			// ReturnReceive Method
			receiveReturnOrder(env, docReceiveOrderInp);

			logger.verbose("AcademyProcessReturnOrderTrackingUpdates.updateAndReceiveReturnOrder() :: End");
		}

	}

	private void modifyCarrierStatusTracker(YFSEnvironment env, Document inDoc, String strIsProcessed)
			throws Exception {
		logger.debug("Start - modifyCarrierStatusTracker() ##");

		inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_UPDATE_PROCESSED, strIsProcessed);

		logger.debug("Input to API::" + XMLUtil.getXMLString(inDoc));
		AcademyUtil.invokeService(env, "AcademyModifyTrackingUpdates", inDoc);

		logger.debug("End - modifyCarrierStatusTracker() ##");

	}

	private String fetchSalesOrderInvoiceNo(YFSEnvironment env, HashMap<String, Double> hmReturnOrderLineDtls,
			String strSalesOrderHeaderKey) throws Exception {

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.fetchSalesOrderInvoiceNo() :: Begin");

		String strInvoiceNo = null;

		HashMap<Integer, String> hmSalesInvoiceWithWeightage = new HashMap<Integer, String>();

		Document docSalesOrderInvoiceList = fetchSalesOrderInvoiceDetails(env, strSalesOrderHeaderKey);

		NodeList nlOrderInvoices = docSalesOrderInvoiceList.getElementsByTagName(AcademyConstants.ELE_ORDER_INVOICE);

		if (nlOrderInvoices.getLength() > 0) {

			for (int i = 0; i < nlOrderInvoices.getLength(); i++) {

				logger.verbose("FOR Loop - Order Invoices :: ");

				HashMap<String, Double> hmSalesOrdInvoiceDtls = new HashMap<String, Double>();

				Element eleOrderInvoice = (Element) nlOrderInvoices.item(i);

				logger.verbose("Element Order Invoice ::  " + XMLUtil.getElementXMLString(eleOrderInvoice));

				NodeList nlLineDetails = eleOrderInvoice.getElementsByTagName(AcademyConstants.ELE_LINE_DETAIL);

				strInvoiceNo = eleOrderInvoice.getAttribute(AcademyConstants.ATTR_INVOICE_NO);

				if (nlLineDetails.getLength() > 0) {

					for (int j = 0; j < nlLineDetails.getLength(); j++) {

						logger.verbose("FOR Loop - Order Invoiced Lines :: ");

						Element eleLineDetail = (Element) nlLineDetails.item(j);

						String strItemId = eleLineDetail.getAttribute(AcademyConstants.ATTR_ITEM_ID);
						String strQuantity = eleLineDetail.getAttribute(AcademyConstants.ATTR_RTN_SHIPPED_QTY);

						hmSalesOrdInvoiceDtls.put(strItemId, Double.parseDouble(strQuantity));
					}
				}

				if (hmSalesOrdInvoiceDtls.equals(hmReturnOrderLineDtls)) {

					logger.verbose("Exact Sales Order Invoice is identified - Items,Qty match :: ");

					hmSalesInvoiceWithWeightage.put(5, strInvoiceNo);

				} else if (hmReturnOrderLineDtls.keySet().equals(hmSalesOrdInvoiceDtls.keySet())) {

					logger.verbose("Sales Order Invoice is identified - Items match :: ");

					hmSalesInvoiceWithWeightage.put(3, strInvoiceNo);

				} else if (isRetLineMapSubsetOfSalesInvoice(hmSalesOrdInvoiceDtls, hmReturnOrderLineDtls)) {

					logger.verbose("Sales Order Invoice is identified - Items match (Sub Set) :: ");

					hmSalesInvoiceWithWeightage.put(1, strInvoiceNo);

				} else {

					logger.verbose("Sales Order Invoice is not found :: ");

					hmSalesInvoiceWithWeightage.put(0, AcademyConstants.STR_BLANK);

				}

			}

			strInvoiceNo = hmSalesInvoiceWithWeightage.get(Collections.max(hmSalesInvoiceWithWeightage.keySet()));

		}

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.fetchSalesOrderInvoiceNo() :: End");

		return strInvoiceNo;
	}

	private boolean isRetLineMapSubsetOfSalesInvoice(HashMap<String, Double> hmSalesOrdInvoiceLines,
			HashMap<String, Double> hmRetOrdLines) throws Exception {

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.isRetLineMapSubsetOfSalesInvoice() :: Begin");

		boolean isSubset = false;

		for (Map.Entry<String, Double> retLine : hmRetOrdLines.entrySet()) {

			logger.verbose("Comparing Return & Sales Order Invoice lines :: ");

			if (hmSalesOrdInvoiceLines.containsKey(retLine.getKey())) {
				isSubset = true;
			} else {
				isSubset = false;
				break;
			}

		}

		logger.verbose("Is Subset :: " + isSubset);

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.isRetLineMapSubsetOfSalesInvoice() :: End");

		return isSubset;
	}

	private Document fetchSalesOrderInvoiceDetails(YFSEnvironment env, String strSalesOrderHeaderKey) throws Exception {

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.fetchSalesOrderInvoiceDetails() :: Begin");

		Document docOrderInvoice = XMLUtil.createDocument(AcademyConstants.ELE_ORDER_INVOICE);
		Element eleOrderInvoice = docOrderInvoice.getDocumentElement();
		eleOrderInvoice.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strSalesOrderHeaderKey);
		eleOrderInvoice.setAttribute(AcademyConstants.ATTR_INVOICE_TYPE, AcademyConstants.STR_ORDER_INVOICE_TYPE);

		logger.verbose("Input - getOrderInvoiceList API :: " + XMLUtil.getXMLString(docOrderInvoice));

		env.setApiTemplate(AcademyConstants.API_GET_ORDER_INVOICE_LIST,
				AcademyConstants.STR_TEMPLATEFILE_GETORDER_INVOICEDETAILS);

		Document docOrderInvoiceList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_INVOICE_LIST,
				docOrderInvoice);

		env.clearApiTemplate(AcademyConstants.API_GET_ORDER_INVOICE_LIST);

		logger.verbose("Output - getOrderInvoiceList API :: " + XMLUtil.getXMLString(docOrderInvoiceList));

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.fetchSalesOrderInvoiceDetails() :: End");

		return docOrderInvoiceList;
	}

	private Element removeReturnShippingCharge(Element eleReturnLineCharges, boolean bRemoveReturnShippingCharge,
			boolean bRemoveShippingCharge) throws Exception {

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.removeReturnShippingCharge() :: Begin");

		logger.verbose("BEFORE - Element LineCharges :: " + XMLUtil.getElementXMLString(eleReturnLineCharges));

		if (bRemoveReturnShippingCharge) {

			NodeList nlReturnShippingChargeList = XPathUtil.getNodeList(eleReturnLineCharges,
					"LineCharge[@ChargeCategory='ReturnShippingCharge']");

			if (nlReturnShippingChargeList.getLength() == 1) {

				logger.verbose("bRemoveReturnShippingCharge - node length == 1");

				Element eleLineCharge = (Element) nlReturnShippingChargeList.item(0);
				eleReturnLineCharges.removeChild(eleLineCharge);

			} else if (nlReturnShippingChargeList.getLength() > 1) {

				logger.verbose("bRemoveReturnShippingCharge - node length > 1");

				for (int i = 0; i < nlReturnShippingChargeList.getLength(); i++) {

					Element eleLineCharge = (Element) nlReturnShippingChargeList.item(i);
					eleReturnLineCharges.removeChild(eleLineCharge);

				}
			}
		}

		if (bRemoveShippingCharge) {

			NodeList nlReturnLineShippingChargeList = XPathUtil.getNodeList(eleReturnLineCharges,
					"LineCharge[@ChargeName='ShippingCharge' or @ChargeName='ShippingPromotion']");

			if (nlReturnLineShippingChargeList.getLength() == 1) {

				logger.verbose("bRemoveShippingCharge - node length == 1");

				Element eleLineCharge = (Element) nlReturnLineShippingChargeList.item(0);
				eleReturnLineCharges.removeChild(eleLineCharge);

			} else if (nlReturnLineShippingChargeList.getLength() > 1) {

				logger.verbose("bRemoveShippingCharge - node length > 1");

				for (int i = 0; i < nlReturnLineShippingChargeList.getLength(); i++) {

					Element eleLineCharge = (Element) nlReturnLineShippingChargeList.item(i);
					eleReturnLineCharges.removeChild(eleLineCharge);

				}
			}
		}

		logger.verbose("AFTER - Element LineCharges :: " + XMLUtil.getElementXMLString(eleReturnLineCharges));

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.removeReturnShippingCharge() :: End");

		return eleReturnLineCharges;
	}

	private Element removeReturnShippingTaxes(Element eleReturnLineTaxes, boolean bRemoveReturnShippingCharge,
			boolean bRemoveShippingCharge) throws Exception {

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.removeReturnShippingTaxes() :: Begin");

		logger.verbose("BEFORE - Element LineTaxes :: " + XMLUtil.getElementXMLString(eleReturnLineTaxes));

		if (bRemoveReturnShippingCharge) {

			NodeList nlReturnShippingTaxList = XPathUtil.getNodeList(eleReturnLineTaxes,
					"LineTax[@TaxName='ReturnShipTax']");

			if (nlReturnShippingTaxList.getLength() == 1) {

				logger.verbose("bRemoveReturnShippingCharge - node length == 1");

				Element eleLineTaxe = (Element) nlReturnShippingTaxList.item(0);
				eleReturnLineTaxes.removeChild(eleLineTaxe);

			} else if (nlReturnShippingTaxList.getLength() > 1) {

				logger.verbose("bRemoveReturnShippingCharge - node length > 1");

				for (int i = 0; i < nlReturnShippingTaxList.getLength(); i++) {

					Element eleLineTaxe = (Element) nlReturnShippingTaxList.item(i);
					eleReturnLineTaxes.removeChild(eleLineTaxe);
				}

			}
		}

		if (bRemoveShippingCharge) {

			NodeList nlreturnLineShippingTaxList = XPathUtil.getNodeList(eleReturnLineTaxes,
					"LineTax[@ChargeCategory='Shipping']");

			if (nlreturnLineShippingTaxList.getLength() == 1) {

				logger.verbose("bRemoveShippingCharge - node length == 1");

				Element eleLineTaxes = (Element) nlreturnLineShippingTaxList.item(0);
				eleReturnLineTaxes.removeChild(eleLineTaxes);

			} else if (nlreturnLineShippingTaxList.getLength() > 1) {

				logger.verbose("bRemoveShippingCharge - node length > 1");

				for (int i = 0; i < nlreturnLineShippingTaxList.getLength(); i++) {

					Element eleLineTaxe = (Element) nlreturnLineShippingTaxList.item(i);
					eleReturnLineTaxes.removeChild(eleLineTaxe);
				}

			}
		}

		logger.verbose("AFTER - Element LineTaxes :: " + XMLUtil.getElementXMLString(eleReturnLineTaxes));

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.removeReturnShippingTaxes() :: End");

		return eleReturnLineTaxes;
	}

	/*
	 * Below following methods are used to process moving the Return Order
	 * Authorized status to return received, ReceiptClosed statuses.
	 */

	private void receiveReturnOrder(YFSEnvironment env, Document docReturnLinesReceiveDoc) {
		Element eleReturnOrders = null;
		Element eleCurrentReturnOrder = null;
		Document docStartReceipt = null;
		Document docReturnReceiveOutput = null;
		Document docCloseReceiptOutput = null;
		NodeList nListReturnOrders = null;
		int iNumberOfReturns = 0;

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.receiveReturnOrder() :: Begin");

		try {
			if (!YFCObject.isVoid(docReturnLinesReceiveDoc)) {

				logger.verbose("input xml :: " + XMLUtil.getXMLString(docReturnLinesReceiveDoc));

				eleReturnOrders = docReturnLinesReceiveDoc.getDocumentElement();
				nListReturnOrders = XMLUtil.getNodeList(eleReturnOrders, AcademyConstants.ELE_ORDER);
				iNumberOfReturns = nListReturnOrders.getLength();

				for (int i = 0; i < iNumberOfReturns; i++) {

					eleCurrentReturnOrder = (Element) nListReturnOrders.item(i);
					docStartReceipt = startReceiptForReturnOrder(eleCurrentReturnOrder, env);

					if (!YFCObject.isVoid(docStartReceipt)) {
						logger.verbose("docStartReceipt::::" + XMLUtil.getXMLString(docStartReceipt));
					}

					docReturnReceiveOutput = receiveOrderForReturn(docStartReceipt, eleCurrentReturnOrder, env);

					if (!YFCObject.isVoid(docReturnReceiveOutput)) {
						logger.verbose("docReturnReceiveOutput::::" + XMLUtil.getXMLString(docReturnReceiveOutput));
					}

					docCloseReceiptOutput = closeReturnReceipt(docStartReceipt, env);

					if (!YFCObject.isVoid(docCloseReceiptOutput)) {
						logger.verbose("docCloseReceiptOutput::::" + XMLUtil.getXMLString(docCloseReceiptOutput));

					}
				}
			} else
				logger.verbose("Inside receiveReturnOrder docReturnLinesReceiveDoc is NULL :: ");

		} catch (Exception e) {
			logger.error("Exception :: " + e.getMessage());
			throw new YFSException(e.getMessage());
		}

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.receiveReturnOrder() :: End");

	}

	private Document receiveOrderForReturn(Document docStartReceipt, Element eleCurrentReturnOrder,
			YFSEnvironment env) {
		Document receiveReturnOrderOutput = null;
		Document docReceiveReturn = null;
		Element eleReceiptLines = null;
		Element eleReceiptLine = null;

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.receiveOrderForReturnn() :: Begin");

		try {

			docReceiveReturn = XMLUtil.createDocument(AcademyConstants.ELE_RECPT);
			docReceiveReturn.getDocumentElement().setAttribute(AcademyConstants.ATTR_DOC_TYPE,
					AcademyConstants.STR_RETURN_DOCTYPE);

			docReceiveReturn.getDocumentElement().setAttribute(AcademyConstants.ATTR_RECPT_NO,
					docStartReceipt.getDocumentElement().getAttribute(AcademyConstants.ATTR_RECPT_NO));
			docReceiveReturn.getDocumentElement().setAttribute(AcademyConstants.ATTR_RECPT_HEADERKEY,
					docStartReceipt.getDocumentElement().getAttribute(AcademyConstants.ATTR_RECPT_HEADERKEY));
			docReceiveReturn.getDocumentElement().setAttribute(AcademyConstants.ATTR_RECV_NODE,
					docStartReceipt.getDocumentElement().getAttribute(AcademyConstants.ATTR_RECV_NODE));
			docReceiveReturn.getDocumentElement().setAttribute(AcademyConstants.STR_ORDR_HDR_KEY,
					eleCurrentReturnOrder.getAttribute(AcademyConstants.STR_ORDR_HDR_KEY));
			eleReceiptLines = docReceiveReturn.createElement(AcademyConstants.RECEIPT_LINES);

			NodeList nReturnLines = XPathUtil.getNodeList(eleCurrentReturnOrder, AcademyConstants.XPATH_ORDERLINE);
			int iNoOfReturnLines = nReturnLines.getLength();

			for (int j = 0; j < iNoOfReturnLines; j++) {

				Element eleCurrentLine = (Element) nReturnLines.item(j);
				eleReceiptLine = docReceiveReturn.createElement(AcademyConstants.RECEIPT_LINE);
				eleReceiptLine.setAttribute(AcademyConstants.ATTR_UOM,
						eleCurrentLine.getAttribute(AcademyConstants.ATTR_UOM));
				eleReceiptLine.setAttribute(AcademyConstants.ATTR_ITEM_ID,
						eleCurrentLine.getAttribute(AcademyConstants.ATTR_ITEM_ID));
				// eleReceiptLine.setAttribute(AcademyConstants.ATTR_PROD_CLASS,
				// AcademyConstants.PRODUCT_CLASS);
				eleReceiptLine.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY,
						eleCurrentLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
				eleReceiptLine.setAttribute(AcademyConstants.ATTR_QUANTITY,
						eleCurrentLine.getAttribute(AcademyConstants.ATTR_QUANTITY));

				XMLUtil.importElement(eleReceiptLines, eleReceiptLine);
			}

			XMLUtil.importElement(docReceiveReturn.getDocumentElement(), eleReceiptLines);

			if (!YFCObject.isVoid(docReceiveReturn)) {

				logger.verbose("Input to receiveOrder is :: " + XMLUtil.getXMLString(docReceiveReturn));

				receiveReturnOrderOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_RECEIVE_ORDER,
						docReceiveReturn);
			}
		} catch (Exception e) {
			logger.error("Exception :: " + e.getMessage());
			throw new YFSException(e.getMessage());
		}

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.receiveOrderForReturnn() :: End");

		return receiveReturnOrderOutput;
	}

	private Document closeReturnReceipt(Document docStartReceipt, YFSEnvironment env) {
		Document docCloseReceiptInput = null;
		Document docCloseReceiptOutput = null;
		Element eleReceipt = null;

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.closeReturnReceipt() :: Begin");

		try {

			docCloseReceiptInput = XMLUtil.createDocument(AcademyConstants.ELE_RECPT);
			eleReceipt = docCloseReceiptInput.getDocumentElement();
			eleReceipt.setAttribute(AcademyConstants.ATTR_RECPT_HEADERKEY,
					docStartReceipt.getDocumentElement().getAttribute(AcademyConstants.ATTR_RECPT_HEADERKEY));
			eleReceipt.setAttribute(AcademyConstants.ATTR_RECPT_NO,
					docStartReceipt.getDocumentElement().getAttribute(AcademyConstants.ATTR_RECPT_NO));
			eleReceipt.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.STR_RETURN_DOCTYPE);
			eleReceipt.setAttribute(AcademyConstants.ATTR_RECV_NODE,
					docStartReceipt.getDocumentElement().getAttribute(AcademyConstants.ATTR_RECV_NODE));

			if (!YFCObject.isVoid(docCloseReceiptInput)) {

				logger.verbose("Input to closeReceipt is" + XMLUtil.getXMLString(docCloseReceiptInput));

				docCloseReceiptOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_CLOSE_RCPT,
						docCloseReceiptInput);
			}

		} catch (Exception e) {
			logger.error("Exception :: " + e.getMessage());
			throw new YFSException(e.getMessage());
		}

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.closeReturnReceipt() :: End");

		return docCloseReceiptOutput;
	}

	private Document startReceiptForReturnOrder(Element currOrder, YFSEnvironment env) {
		Element eleReceipt = null;
		Element eleShipment = null;
		Document docStartRecptInput = null;
		Document docStartRecptOutput = null;

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.startReceiptForReturnOrder() :: Begin");

		try {

			docStartRecptInput = XMLUtil.createDocument(AcademyConstants.ELE_RECPT);
			eleReceipt = docStartRecptInput.getDocumentElement();
			eleReceipt.setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.STR_RETURN_DOCTYPE);
			eleReceipt.setAttribute(AcademyConstants.ATTR_RECV_NODE,
					currOrder.getAttribute(AcademyConstants.ATTR_RECV_NODE));
			eleShipment = docStartRecptInput.createElement(AcademyConstants.ELE_SHIPMENT);
			eleShipment.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY,
					currOrder.getAttribute(AcademyConstants.STR_ORDR_HDR_KEY));
			eleShipment.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.PRIMARY_ENTERPRISE);

			XMLUtil.importElement(eleReceipt, eleShipment);

			if (!YFCObject.isVoid(docStartRecptInput)) {

				logger.verbose("Input to startReceipt is" + XMLUtil.getXMLString(docStartRecptInput));

				docStartRecptOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_START_RCPT, docStartRecptInput);
			}
		} catch (Exception e) {
			logger.error("Exception :: " + e.getMessage());
			throw new YFSException(e.getMessage());
		}

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.startReceiptForReturnOrder() :: End");

		return docStartRecptOutput;
	}

	private void invokeChangeOrder(YFSEnvironment env, Document docChangeOrderInp) throws Exception {

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.invokeChangeOrder() :: Begin");

		logger.verbose("changeOrder input :: " + XMLUtil.getXMLString(docChangeOrderInp));

		env.setTxnObject("RepricingFlag", AcademyConstants.STR_NO);
		AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_ORDER, docChangeOrderInp);

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.invokeChangeOrder() :: End");
	}

	public boolean isReturnReasonCodeExist(Document docCommonCodeList, String returnReasonCode) throws Exception {

		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.isReturnReasonCodeExist() :: Begin");

		boolean bIsReasonCodePresent = false;

		Element eleExistedReturnReasonCode = (Element) XPathUtil.getNode(docCommonCodeList.getDocumentElement(),
				"CommonCode[@CodeValue='" + returnReasonCode + "']");

		if (null != eleExistedReturnReasonCode) {
			bIsReasonCodePresent = true;
		}

		logger.verbose("bIsReasonCodePresent - " + bIsReasonCodePresent);
		logger.verbose("AcademyProcessReturnOrderTrackingUpdates.isReturnReasonCodeExist() :: End");

		return bIsReasonCodePresent;

	}

}
