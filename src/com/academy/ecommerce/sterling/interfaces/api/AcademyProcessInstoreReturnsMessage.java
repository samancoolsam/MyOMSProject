package com.academy.ecommerce.sterling.interfaces.api;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyReturnOrderUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/*##################################################################################
 *
 * Project Name                : Kount Integration Release 2
 * Module                      : OMS
 * Author                      : CTS
 * Date                        : 24-MAY-2019
 * Description                 : This class does following
 *                               1.Processes in-store return updates from POS via ESB.
 *                               2.Creates return order with 'Return Invoiced' status.
 *                               3.Moves Sales to 'Return Created' and then to 'Return Received' status.
 *                               4.If there is a mismatch in Item/ReturnedQty with invoice,
 *                                 then the program just creates the Return Order. Sales order status
 *                                 remains as it is.
 *                               
 *
 *
 * Change Revision
 * ---------------------------------------------------------------------------------
 * Date            Author                  Version#       Remarks/Description                     
 * ---------------------------------------------------------------------------------
 * 24-MAY-2019     CTS                      1.0            Initial version
 * ##################################################################################*/

public class AcademyProcessInstoreReturnsMessage implements YIFCustomApi {

	private static YFCLogCategory logger = YFCLogCategory
			.instance(AcademyProcessInstoreReturnsMessage.class);

	public Document processInstoreReturnMessages(YFSEnvironment yfsEnvironment,
			Document docADCMessage) throws YFSException, Exception {

		// LocalVariables
		String strSalesOrdInvoiceNo = null;
		String strReturnOrdNo = null;

		// LocalObjects
		Document docImportReturnOrderInp = null;
		Document docImportReturnOrderOut = null;
		Document docChgOrdStsRetCreatedInp = null;
		Document docChgOrdStsRetreceivedInp = null;
		Document docCommonCodeOut = null;
		Element eleADCMessage = null;
		Element eleSalesOrderInvoiceDetails = null;
		Element eleRetOrderDetails = null;
		NodeList nlADCOrdLines = null;

		HashMap<String, Double> hmADCItemList = new HashMap<String, Double>();
		HashMap<String, Double> hmInvoiceNoItemList = new HashMap<String, Double>();
		HashMap<String, Double> hmItemMismatchList = new HashMap<String, Double>();
		HashMap<String, Double> hmItemQtyMismatchList = new HashMap<String, Double>();
		HashMap<String, Double> hmEligibleItemsList = new HashMap<String, Double>();

		try {

			logger
					.verbose("Input - AcademyProcessReturnOrderMessageFromADC::\n"
							+ XMLUtil.getXMLString(docADCMessage));

			eleADCMessage = docADCMessage.getDocumentElement();
			strSalesOrdInvoiceNo = eleADCMessage
					.getAttribute(AcademyConstants.ATTR_INVOICE_NO);
			strReturnOrdNo = eleADCMessage
					.getAttribute(AcademyConstants.ATTR_ADC_RMANO);
			nlADCOrdLines = XMLUtil.getNodeList(eleADCMessage,
					AcademyConstants.XPATH_ORDERLINE);

			if (!StringUtil.isEmpty(strSalesOrdInvoiceNo)
					&& !StringUtil.isEmpty(strReturnOrdNo)
					&& nlADCOrdLines.getLength() > 0) {

				logger.verbose("InvoiceNo:" + strSalesOrdInvoiceNo + "\n"
						+ "RMANo::" + strReturnOrdNo);

				eleSalesOrderInvoiceDetails = AcademyReturnOrderUtil
						.getOrderInvoiceDetails(yfsEnvironment,
								strSalesOrdInvoiceNo);

				logger
						.verbose("Output of OrderInvoiceList::\n"
								+ XMLUtil
										.getElementXMLString(eleSalesOrderInvoiceDetails));

				prepareItemListHashMapForRequestedInvoice(
						eleSalesOrderInvoiceDetails, hmInvoiceNoItemList);

				docCommonCodeOut = getCommonCodeList(yfsEnvironment);

				logger.verbose("CommonCode Output::"
						+ XMLUtil.getXMLString(docCommonCodeOut));

				Element eleOrder = (Element) eleSalesOrderInvoiceDetails
						.getElementsByTagName(AcademyConstants.ELE_ORDER).item(
								0);
				eleRetOrderDetails = prepareAndInvokeGetOrderList(
						yfsEnvironment, eleOrder);

				logger.verbose("Output of getOrderList Return::\n"
						+ XMLUtil.getElementXMLString(eleRetOrderDetails));

				for (int i = 0; i < nlADCOrdLines.getLength(); i++) {

					Element eleADCOrdLine = (Element) nlADCOrdLines.item(i);

					logger.verbose("ADC Orderline:: "
							+ XMLUtil.getElementXMLString(eleADCOrdLine));

					String strItemId = eleADCOrdLine
							.getAttribute(AcademyConstants.ITEM_ID);
					String strADCQty = eleADCOrdLine
							.getAttribute(AcademyConstants.ATTR_RETURN_QTY);

					Double dRetQty = Double.parseDouble(strADCQty);

					validateItemInInvoice(eleADCOrdLine, strItemId, dRetQty,
							hmADCItemList, hmInvoiceNoItemList,
							hmItemMismatchList, hmItemQtyMismatchList);

					// Comparing eligibility - Begin

					getEligibilityForReturnCreation(eleADCOrdLine,
							eleSalesOrderInvoiceDetails, eleRetOrderDetails,
							hmInvoiceNoItemList, hmEligibleItemsList,
							hmItemQtyMismatchList, hmItemMismatchList);

					// Comparing eligibility - End

					// Map Details
					logger
							.verbose("hmADCItemList::"
									+ hmADCItemList.toString());
					logger.verbose("hmItemMismatchList::"
							+ hmItemMismatchList.toString());
					logger.verbose("hmItemQtyMismatchList::"
							+ hmItemQtyMismatchList.toString());
					logger.verbose("hmEligibleItemsList::"
							+ hmEligibleItemsList.toString());
					logger.verbose("hmInvoiceNoItemList"
							+ hmInvoiceNoItemList.toString());
					// Map Details

					docImportReturnOrderInp = prepareAndInvokeImportOrder(
							yfsEnvironment, docImportReturnOrderInp,
							docCommonCodeOut, eleSalesOrderInvoiceDetails,
							eleADCMessage, eleADCOrdLine, hmADCItemList,
							hmItemMismatchList, hmItemQtyMismatchList,
							hmEligibleItemsList, i);

					if (!hmEligibleItemsList.isEmpty()) {

						docChgOrdStsRetCreatedInp = prepareChargeOrdStatusInp(
								yfsEnvironment, docChgOrdStsRetCreatedInp,
								eleADCMessage, eleADCOrdLine,
								eleSalesOrderInvoiceDetails, hmADCItemList,
								hmEligibleItemsList,
								AcademyConstants.STR_TXN_INCLUDE_IN_RETURN,
								AcademyConstants.STR_STATUS_RETURN_CREATED);

						docChgOrdStsRetreceivedInp = prepareChargeOrdStatusInp(
								yfsEnvironment, docChgOrdStsRetreceivedInp,
								eleADCMessage, eleADCOrdLine,
								eleSalesOrderInvoiceDetails, hmADCItemList,
								hmEligibleItemsList,
								AcademyConstants.STR_TXN_RECEIPT_LISTENER,
								AcademyConstants.STR_STATUS_RETURN_RECEIVED);
					}

				}
				if (null != docImportReturnOrderInp) {

					logger.verbose("Input - ImportOrderAPI::"
							+ XMLUtil.getXMLString(docImportReturnOrderInp));

					yfsEnvironment.setApiTemplate(
							AcademyConstants.API_IMPORT_ORDER,
							AcademyConstants.STR_TEMPLATEFILE_IMPORT_ORDER);

					docImportReturnOrderOut = AcademyUtil.invokeAPI(
							yfsEnvironment, AcademyConstants.API_IMPORT_ORDER,
							docImportReturnOrderInp);

					yfsEnvironment
							.clearApiTemplate(AcademyConstants.API_IMPORT_ORDER);

					sendReturnStatusToKount(yfsEnvironment,
							docImportReturnOrderOut);
				}

				if (!hmItemMismatchList.isEmpty()
						|| !hmItemQtyMismatchList.isEmpty()) {

					raiseAlertItemOrQuantityMismatch(yfsEnvironment,
							docImportReturnOrderOut, hmItemMismatchList,
							hmItemQtyMismatchList);
				}

				try {
					if (!hmEligibleItemsList.isEmpty()) {

						if (null != docChgOrdStsRetCreatedInp) {
							logger
									.verbose("ChangeOrderStatus input - Return Order Created Status::\n"
											+ XMLUtil
													.getXMLString(docChgOrdStsRetCreatedInp));
							AcademyUtil.invokeAPI(yfsEnvironment,
									AcademyConstants.API_CHG_ORD_STATUS,
									docChgOrdStsRetCreatedInp);
						}
						if (null != docChgOrdStsRetreceivedInp) {

							logger
									.verbose("ChangeOrderStatus input - Return Received Status::\n"
											+ XMLUtil
													.getXMLString(docChgOrdStsRetreceivedInp));
							AcademyUtil.invokeAPI(yfsEnvironment,
									AcademyConstants.API_CHG_ORD_STATUS,
									docChgOrdStsRetreceivedInp);
						}

					}
				} catch (Exception e) {
					logger
							.error("Exception - changeOrderStatus API call::"
									+ e);
				}

			} else {
				logger
						.verbose("InvoiceNo or RMANo or Orderlines are missing in the input::");
				YFSException e = new YFSException();
				e
						.setErrorCode(AcademyConstants.STR_MANDATORY_PARMS_MISSING_ERROR_CODE);
				e
						.setErrorDescription(AcademyConstants.STR_MANDATORY_PARMS_MISSING_ERROR_DESC);
				throw e;
			}

		} catch (YFSException yfse) {
			if (yfse.getErrorCode().equalsIgnoreCase(
					AcademyConstants.STR_INVALID_INVOICE_ERROR_CODE)
					|| yfse
							.getErrorCode()
							.equalsIgnoreCase(
									AcademyConstants.STR_MANDATORY_PARMS_MISSING_ERROR_CODE)) {
				throw yfse;
			} else {
				logger.error("Exception - processACDReturnMessages()::" + yfse);
				prepareAndInvokeImportOrderOnException(yfsEnvironment,
						eleADCMessage, eleSalesOrderInvoiceDetails,
						hmADCItemList, hmItemMismatchList,
						hmItemQtyMismatchList);
			}

		} catch (Exception e) {
			logger.error("Exception - processACDReturnMessages()::" + e);
			prepareAndInvokeImportOrderOnException(yfsEnvironment,
					eleADCMessage, eleSalesOrderInvoiceDetails, hmADCItemList,
					hmItemMismatchList, hmItemQtyMismatchList);
		}

		// Calling updateTaskQueueForFreeItem() as part of OMNI-98744
		updateTaskQueueForFreeItem(yfsEnvironment, docImportReturnOrderInp);
		// OMNI-103428 Starts
		postMessageForReturnStatusUpdate(yfsEnvironment,docImportReturnOrderOut);
		// OMNI-103428 Ends
		return docImportReturnOrderOut;
	}

	public void setProperties(Properties arg0) throws Exception {

	}

	private void prepareADCItemListHasMap(Element eleADCOrdLine,
			String strItemId, Double dRetQty,
			HashMap<String, Double> hmADCItemList) throws Exception {

		if (hmADCItemList.containsKey(strItemId)) {
			Double dTempQty = hmADCItemList.get(strItemId);
			Double dNewQty = dTempQty + dRetQty;
			hmADCItemList.put(strItemId, dNewQty);
			eleADCOrdLine.setAttribute(AcademyConstants.ATTR_DUPLICATE_QTY,
					String.valueOf(dTempQty));
		} else {
			hmADCItemList.put(strItemId, dRetQty);
		}
	}

	private void validateItemInInvoice(Element eleADCOrdLine, String strItemId,
			Double dRetQty, HashMap<String, Double> hmADCItemList,
			HashMap<String, Double> hmInvoiceNoItemList,
			HashMap<String, Double> hmItemMismatchList,
			HashMap<String, Double> hmItemQtyMismatchList) throws Exception {

		logger
				.verbose("AcademyProcessReturnMessageAPI_validateItemInInvoice():");

		prepareADCItemListHasMap(eleADCOrdLine, strItemId, dRetQty,
				hmADCItemList);

		if (!hmInvoiceNoItemList.isEmpty()) {

			isQtyItemMismatchForRequestedInvoice(strItemId, hmADCItemList,
					hmInvoiceNoItemList, hmItemMismatchList,
					hmItemQtyMismatchList);
		}
	}

	private void isQtyItemMismatchForRequestedInvoice(String strItemId,
			HashMap<String, Double> hmADCItemList,
			HashMap<String, Double> hmInvoiceNoItemList,
			HashMap<String, Double> hmItemMismatchList,
			HashMap<String, Double> hmItemQtyMismatchList) throws Exception {

		if (hmADCItemList.containsKey(strItemId)) {

			Double dbReqQtyVal = hmADCItemList.get(strItemId);

			if (hmInvoiceNoItemList != null) {
				if (hmInvoiceNoItemList.containsKey(strItemId)) {
					Double dbInvoicedQtyValue = hmInvoiceNoItemList
							.get(strItemId);
					if (dbReqQtyVal > dbInvoicedQtyValue) {
						hmItemQtyMismatchList.put(strItemId, dbReqQtyVal);
					}
				} else {
					hmItemMismatchList.put(strItemId, dbReqQtyVal);
				}
			}
		}
	}

	private void getEligibilityForReturnCreation(Element eleADCOrderLine,
			Element eleSalesOrderInvoiceDetails, Element eleRetOrderDetails,
			HashMap<String, Double> hmInvoiceNoItemList,
			HashMap<String, Double> hmEligibleItemsList,
			HashMap<String, Double> hmItemQtyHashMap,
			HashMap<String, Double> hmItemMismatchList) {
		boolean isEligibleForReturn = false;
		try {

			logger.verbose("Begin - getEligibilityForReturnCreation()::");

			logger.verbose("Element - ADC Orderline::"
					+ XMLUtil.getElementXMLString(eleADCOrderLine));

			if (!hmItemMismatchList.containsKey(eleADCOrderLine
					.getAttribute(AcademyConstants.ATTR_ITEM_ID))) {
				// invoke method to get eligibility - returns boolean
				isEligibleForReturn = calculateAvailQtyForReturn(
						eleADCOrderLine, eleSalesOrderInvoiceDetails,
						eleRetOrderDetails);

				logger.verbose("Boolean - isEligibleForReturn::"
						+ isEligibleForReturn);

				String strItemId = eleADCOrderLine
						.getAttribute(AcademyConstants.ATTR_ITEM_ID);
				String strRetQty = eleADCOrderLine
						.getAttribute(AcademyConstants.ATTR_RETURN_QTY);

				if (isEligibleForReturn) {

					if (hmEligibleItemsList.containsKey(strItemId)) {
						Double dTempQty = hmEligibleItemsList.get(strItemId);
						hmEligibleItemsList.put(strItemId, (Double
								.parseDouble(strRetQty) + dTempQty));
					} else {
						hmEligibleItemsList.put(strItemId, Double
								.parseDouble(strRetQty));
					}

				} else {

					if (hmEligibleItemsList.containsKey(strItemId)) {
						hmEligibleItemsList.remove(strItemId);
					}
					hmItemQtyHashMap.put(strItemId, Double
							.parseDouble(strRetQty));
				}
			}

		} catch (Exception e) {
			logger.error("Exception - getEligibilityForReturnCreation()::\n"
					+ e);
		}
	}

	private boolean calculateAvailQtyForReturn(Element eleOrderLine,
			Element eleSalesOrderInvoiceDetails, Element eleRetOrderDetails) {

		String strItemId = null;
		String strDupItemQty = null;

		double dQtyReturned = 0;
		double dQtyAvailReturn = 0;
		double dQtyProcessed = 0;
		double dQtyPrevDupItem = 0;

		boolean isQtyAvailForReturn = false;

		NodeList nlReturnLines = null;

		try {

			logger.verbose("Begin - calculateAvailQtyForReturn()::\n");

			strItemId = eleOrderLine
					.getAttribute(AcademyConstants.ATTR_ITEM_ID);

			strDupItemQty = eleOrderLine
					.getAttribute(AcademyConstants.ATTR_DUPLICATE_QTY);
			if (!StringUtil.isEmpty(strDupItemQty)) {
				dQtyPrevDupItem = Double.parseDouble(strDupItemQty);
			}

			dQtyAvailReturn = Double.parseDouble(eleOrderLine
					.getAttribute(AcademyConstants.ATTR_RETURN_QTY));

			NodeList nlReturnOrders = eleRetOrderDetails
					.getElementsByTagName(AcademyConstants.ELE_ORDER);

			if (null != nlReturnOrders) {

				for (int j = 0; j < nlReturnOrders.getLength(); j++) {

					Element eleReturnOrder = (Element) nlReturnOrders.item(j);

					nlReturnLines = XMLUtil.getNodeList(eleReturnOrder,
							"OrderLines/OrderLine[Item/@ItemID='" + strItemId
									+ "']");

					if (null != nlReturnLines) {
						logger.verbose("Checking - return lines::\n");
						for (int retLines = 0; retLines < nlReturnLines
								.getLength(); retLines++) {
							Element eleReturnOrderLine = (Element) nlReturnLines
									.item(retLines);
							logger
									.verbose("ele ret::"
											+ XMLUtil
													.getElementXMLString(eleReturnOrderLine));
							String strReturnStatusQty = eleReturnOrderLine
									.getAttribute(AcademyConstants.ATTR_STATUS_QTY);

							dQtyReturned = dQtyReturned
									+ Double.parseDouble(strReturnStatusQty);

						}
					}
				}
			}

			Element eleLineDetail = XMLUtil.getElementByXPath(
					eleSalesOrderInvoiceDetails.getOwnerDocument(),
					"OrderInvoiceList/OrderInvoice/LineDetails/LineDetail[@ItemID='"
							+ strItemId + "']");

			String strShippedQty = eleLineDetail
					.getAttribute(AcademyConstants.ATTR_RTN_SHIPPED_QTY);

			dQtyProcessed = Double.parseDouble(strShippedQty);

			logger.verbose("Value:: "
					+ (dQtyProcessed - dQtyReturned - dQtyPrevDupItem));

			if (dQtyAvailReturn <= (dQtyProcessed - dQtyReturned - dQtyPrevDupItem)) {
				isQtyAvailForReturn = true;
			}

			return isQtyAvailForReturn;

		} catch (Exception e) {
			logger.error("Exception - calculateAvailQtyForReturn()::" + e);
		}

		return false;
	}

	private Document prepareAndInvokeImportOrderOnException(YFSEnvironment env,
			Element eleADCInput, Element eleSalesOrderInvoice,
			HashMap<String, Double> hmADCItemList,
			HashMap<String, Double> hmItemMismatchList,
			HashMap<String, Double> hmItemQtyMismatchList) throws Exception {

		Document docImportOrder = null;
		Document docImportOrderOut = null;
		Element eleOrder = null;
		Document docCommonCodeOut = null;

		try {

			logger
					.verbose("Start - prepareAndInvokeImportOrderOnException()::");
			if (!YFSObject.isNull(eleSalesOrderInvoice)) {

				docCommonCodeOut = getCommonCodeList(env);

				logger.verbose("CommonCode Output::"
						+ XMLUtil.getXMLString(docCommonCodeOut));

				Element eleInvoiceOrder = (Element) eleSalesOrderInvoice
						.getElementsByTagName(AcademyConstants.ELE_ORDER).item(
								0);

				String strOrderHeaderKey = eleInvoiceOrder
						.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);

				String strOrderLineKey = null;

				docImportOrder = XMLUtil
						.createDocument(AcademyConstants.ELE_ORDER);
				eleOrder = docImportOrder.getDocumentElement();

				eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_TYPE,
						AcademyConstants.STR_INSTORE_RETURN);
				eleOrder
						.setAttribute(
								AcademyConstants.ATTR_ORDER_DATE,
								eleADCInput
										.getAttribute(AcademyConstants.ATTR_RETURN_DATE));
				eleOrder.setAttribute(AcademyConstants.ATTR_FULFILLMENT_TYPE,
						AcademyConstants.STR_RETURN_FULFILLMENT);
				eleOrder.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE,
						AcademyConstants.PRIMARY_ENTERPRISE);
				eleOrder.setAttribute(AcademyConstants.ATTR_DOC_TYPE,
						AcademyConstants.STR_RETURN_DOCTYPE);

				String strShipNode = eleADCInput
						.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
				eleOrder.setAttribute(AcademyConstants.ATTR_RECV_NODE,
						strShipNode);
				eleOrder.setAttribute(
						AcademyConstants.ATTR_RETURN_GIFT_RECIPIENT,
						AcademyConstants.STR_NO);
				eleOrder.setAttribute(AcademyConstants.ATTR_HAS_DERIVED_PARENT,
						AcademyConstants.STR_YES);
				eleOrder.setAttribute(AcademyConstants.ATTR_SELL_ORG_CODE,
						AcademyConstants.PRIMARY_ENTERPRISE);
				eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_NO,
						eleADCInput
								.getAttribute(AcademyConstants.ATTR_ADC_RMANO));
				eleOrder.setAttribute(AcademyConstants.ATTR_APPLY_DEFLT_TMPLT,
						AcademyConstants.STR_YES);
				eleOrder.setAttribute(AcademyConstants.ATTR_CREATED_AT_NODE,
						AcademyConstants.STR_YES);

				Element elePersonInfoBillTo = XMLUtil
						.getElementByXPath(
								eleSalesOrderInvoice.getOwnerDocument(),
								AcademyConstants.XPATH_ORDER_INVOICE_PERSON_INFO_BILL_TO);

				Element eleTempPersonInfoBill = (Element) docImportOrder
						.importNode(elePersonInfoBillTo, true);
				eleOrder.appendChild(eleTempPersonInfoBill);

				eleOrder.setAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID,
						elePersonInfoBillTo
								.getAttribute(AcademyConstants.ATTR_EMAILID));

				Document docOrgList = getOrganizationList(env, strShipNode);
				Element eleOrgContactInfo = (Element) docOrgList
						.getDocumentElement().getElementsByTagName(
								AcademyConstants.ELE_CORPORATE_PERSONAL_INFO)
						.item(0);
				Element elePersonInfoShipTo = docImportOrder
						.createElement(AcademyConstants.ELEM_PERSON_INFO_SHIP_TO);
				eleOrder.appendChild(elePersonInfoShipTo);

				Element eleTempPersonInfoShip = (Element) docImportOrder
						.importNode(eleOrgContactInfo, true);
				XMLUtil.copyElement(docImportOrder, eleTempPersonInfoShip,
						elePersonInfoShipTo);

				Element eleOrderLines = docImportOrder
						.createElement(AcademyConstants.ELE_ORDER_LINES);
				eleOrder.appendChild(eleOrderLines);

				NodeList nlADCOrdLines = XPathUtil.getNodeList(eleADCInput,
						AcademyConstants.XPATH_ORDERLINE);

				for (int i = 0; i < nlADCOrdLines.getLength(); i++) {

					Element eleADCOrdLine = (Element) nlADCOrdLines.item(i);

					String strItemId = eleADCOrdLine
							.getAttribute(AcademyConstants.ATTR_ITEM_ID);
					String strRetQty = eleADCOrdLine
							.getAttribute(AcademyConstants.ATTR_RETURN_QTY);
					String strRetReason = eleADCOrdLine
							.getAttribute(AcademyConstants.ATTR_RETURN_REASON);
					Element eleCommonCode = (Element) XPathUtil.getNode(
							docCommonCodeOut.getDocumentElement(),
							"CommonCode[@CodeValue='" + strRetReason + "']");

					if (!YFCObject.isNull(eleCommonCode)) {
						strRetReason = eleCommonCode
								.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
					}

					Element eleOrderLine = docImportOrder
							.createElement(AcademyConstants.ELE_ORDER_LINE);
					eleOrderLines.appendChild(eleOrderLine);

					eleOrderLine.setAttribute(
							AcademyConstants.ATTR_PRIME_LINE_NO, Integer
									.toString(i + 1));
					eleOrderLine.setAttribute(AcademyConstants.SUB_LINE_NO,
							Integer.toString(1));
					eleOrderLine.setAttribute(
							AcademyConstants.ATTR_ORDERED_QTY, strRetQty);
					eleOrderLine.setAttribute(
							AcademyConstants.ATTR_RETURN_REASON, strRetReason);
					eleOrderLine.setAttribute(AcademyConstants.ATTR_ACTION,
							AcademyConstants.STR_ACTION_CREATE);
					eleOrderLine.setAttribute(AcademyConstants.ATTR_SHIP_NODE,
							strShipNode);
					eleOrderLine.setAttribute(
							AcademyConstants.ATTR_PIPELINE_ID,
							AcademyConstants.IN_STORE_PIPELINE_ID);

					eleOrderLine.setAttribute(
							AcademyConstants.ATTR_PIPELINE_OWNER_KEY,
							AcademyConstants.HUB_CODE);

					Element eleItem = docImportOrder
							.createElement(AcademyConstants.ITEM);
					eleOrderLine.appendChild(eleItem);

					eleItem.setAttribute(AcademyConstants.ATTR_ITEM_ID,
							strItemId);
					eleItem.setAttribute(AcademyConstants.ATTR_UOM,
							AcademyConstants.UOM_EACH_VAL);

					Element eleDerivedFrom = docImportOrder
							.createElement(AcademyConstants.ATTR_DERIVED_FROM);
					eleOrderLine.appendChild(eleDerivedFrom);

					eleDerivedFrom.setAttribute(
							AcademyConstants.ATTR_ORDER_HEADER_KEY,
							strOrderHeaderKey);

					if (hmItemMismatchList.containsKey(strItemId)
							|| hmItemQtyMismatchList.containsKey(strItemId)) {
						SimpleDateFormat sdf = new SimpleDateFormat(
								"yyyySSSSSSSSSSMMddHHmmss");
						strOrderLineKey = sdf.format(new Date());
					} else {

						Element eleSalesOrderLine = XMLUtil.getElementByXPath(
								eleSalesOrderInvoice.getOwnerDocument(),
								"OrderInvoiceList/OrderInvoice/LineDetails/LineDetail[@ItemID='"
										+ strItemId + "']");

						strOrderLineKey = eleSalesOrderLine
								.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);

					}

					eleDerivedFrom.setAttribute(
							AcademyConstants.ATTR_ORDER_LINE_KEY,
							strOrderLineKey);

					Element eleOrderStatuses = docImportOrder
							.createElement(AcademyConstants.ELE_ORDER_STATUSES);
					eleOrderLine.appendChild(eleOrderStatuses);

					Element eleOrderStatus = docImportOrder
							.createElement(AcademyConstants.ELE_ORDER_STATUS);
					eleOrderStatuses.appendChild(eleOrderStatus);

					eleOrderStatus.setAttribute(AcademyConstants.ATTR_STATUS,
							AcademyConstants.STATUS_RETURN_INVOICED);
					eleOrderStatus.setAttribute(AcademyConstants.ATTR_STAT_QTY,
							strRetQty);

					Element eleSchedule = docImportOrder
							.createElement(AcademyConstants.ELE_SCHEDULE);
					eleOrderStatus.appendChild(eleSchedule);
				}

				if (null != docImportOrder) {

					logger.verbose("Input - ImportOrderAPI::"
							+ XMLUtil.getXMLString(docImportOrder));

					env.setApiTemplate(AcademyConstants.API_IMPORT_ORDER,
							AcademyConstants.STR_TEMPLATEFILE_IMPORT_ORDER);

					docImportOrderOut = AcademyUtil.invokeAPI(env,
							AcademyConstants.API_IMPORT_ORDER, docImportOrder);

					env.clearApiTemplate(AcademyConstants.API_IMPORT_ORDER);

					sendReturnStatusToKount(env, docImportOrderOut);
				}
				return docImportOrderOut;
			}

		} catch (Exception e) {
			logger.error("Exception::" + e);
			throw new Exception(e);
		}
		return null;

	}

	private void sendReturnStatusToKount(YFSEnvironment yfsEnvironment,
			Document docImportReturnOrder) throws Exception {

		try {			
			AcademyUtil.invokeService(yfsEnvironment,
					AcademyConstants.SERVICE_ACADEMY_KOUNT_RETURN_STATUS_SERVICE,
					docImportReturnOrder);
			
		} catch (Exception e) {
			logger.error("Exception::" + e);
		}	

	}

	private Document getCommonCodeList(YFSEnvironment env) {

		Document getCommonCodeListInDoc = null;
		Document getCommonCodeListOutDoc = null;

		try {
			getCommonCodeListInDoc = XMLUtil
					.createDocument(AcademyConstants.ELE_COMMON_CODE);
			getCommonCodeListInDoc.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_CODE_TYPE,
					AcademyConstants.RETURN_REASON);
			getCommonCodeListInDoc.getDocumentElement().setAttribute(
					AcademyConstants.ORGANIZATION_CODE,
					AcademyConstants.PRIMARY_ENTERPRISE);

			logger.verbose("getCommonCodeList input Doc: "
					+ XMLUtil.getXMLString(getCommonCodeListInDoc));

			env.setApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST,
					AcademyConstants.STR_TEMPLATEFILE_GET_COMMON_CODE_LIST);

			getCommonCodeListOutDoc = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_COMMONCODE_LIST,
					getCommonCodeListInDoc);

			env.clearApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST);

			logger.verbose("getCommonCodeList API Output Doc: "
					+ XMLUtil.getXMLString(getCommonCodeListOutDoc));

		} catch (Exception e) {
			logger.error("Exception - getCommonCodeList()::" + e);
		}

		return getCommonCodeListOutDoc;
	}

	private Document getOrganizationList(YFSEnvironment env, String strNode) {

		Document docInpOrgList = null;
		Document docOutOrgList = null;

		try {

			logger.verbose("Start - getOrganizationList()::");
			docInpOrgList = XMLUtil.createDocument(AcademyConstants.ELE_ORG);
			Element eleInpOrgList = docInpOrgList.getDocumentElement();
			eleInpOrgList.setAttribute(AcademyConstants.ORGANIZATION_CODE,
					strNode);

			env.setApiTemplate(AcademyConstants.API_GET_ORGANIZATION_LIST,
					AcademyConstants.STR_TEMPLATEFILE_GET_ORG_LIST);

			docOutOrgList = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_ORGANIZATION_LIST, docInpOrgList);

			env.clearApiTemplate(AcademyConstants.API_GET_ORGANIZATION_LIST);

			if (null != docOutOrgList) {
				logger.verbose("Output of Organization Details::"
						+ XMLUtil.getXMLString(docOutOrgList));
				return docOutOrgList;
			}
		} catch (Exception e) {
			logger.error("Exception - getOrganizationList()::" + e);
		}
		return null;
	}

	private void prepareItemListHashMapForRequestedInvoice(
			Element eleGetOrderInvoiceDetails,
			HashMap<String, Double> hmInvoiceNoItemList) throws YFSException {

		logger
				.verbose("Start - prepareItemListHashMapForRequestedInvoice():: ");

		NodeList nlInvLineDetails = eleGetOrderInvoiceDetails
				.getElementsByTagName(AcademyConstants.ELEM_LINE_DETAIL);

		if (null != nlInvLineDetails && nlInvLineDetails.getLength() > 0) {

			for (int k = 0; k < nlInvLineDetails.getLength(); k++) {
				Element eleLineDetail = (Element) nlInvLineDetails.item(k);

				String strItemID = eleLineDetail
						.getAttribute(AcademyConstants.ITEM_ID);
				Double dbQty = Double.parseDouble(eleLineDetail
						.getAttribute(AcademyConstants.ATTR_RTN_SHIPPED_QTY));
				if (!StringUtil.isEmpty(strItemID)) {
					hmInvoiceNoItemList.put(strItemID, dbQty);
				}
			}
		} else {
			logger
					.verbose("Order Invoice details not available in Sterling System.");
			YFSException e = new YFSException();
			e.setErrorCode(AcademyConstants.STR_INVALID_INVOICE_ERROR_CODE);
			e
					.setErrorDescription(AcademyConstants.STR_INVALID_INVOICE_ERROR_DESC);
			throw e;
		}
	}

	private Document prepareAndInvokeImportOrder(YFSEnvironment env,
			Document docImportOrderInp, Document docCommonCodeOut,
			Element eleSalesOrderInvoiceDetails, Element eleADCInput,
			Element eleADCOrdLine, HashMap<String, Double> hmADCItemList,
			HashMap<String, Double> hmItemMismatchList,
			HashMap<String, Double> hmItemQtyMismatchList,
			HashMap<String, Double> hmEligibleItemList, int i) throws Exception {

		Element eleOrder = null;

		String strOrderHeaderKey = null;
		String strOrderLineKey = null;
		String strShipNode = null;
		try {

			logger.verbose("Start - prepareAndInvokeImportOrder()::");
			if (!YFCObject.isNull(eleSalesOrderInvoiceDetails)) {

				Element eleInvoiceOrder = (Element) eleSalesOrderInvoiceDetails
						.getElementsByTagName(AcademyConstants.ELE_ORDER).item(
								0);

				strOrderHeaderKey = eleInvoiceOrder
						.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);

				strShipNode = eleADCInput
						.getAttribute(AcademyConstants.ATTR_SHIP_NODE);

				if (YFCObject.isNull(docImportOrderInp)) {

					docImportOrderInp = XMLUtil
							.createDocument(AcademyConstants.ELE_ORDER);
					eleOrder = docImportOrderInp.getDocumentElement();

					eleOrder.setAttribute(AcademyConstants.ATTR_ORDER_TYPE,
							AcademyConstants.STR_INSTORE_RETURN);
					eleOrder
							.setAttribute(
									AcademyConstants.ATTR_ORDER_DATE,
									eleADCInput
											.getAttribute(AcademyConstants.ATTR_RETURN_DATE));
					eleOrder.setAttribute(
							AcademyConstants.ATTR_FULFILLMENT_TYPE,
							AcademyConstants.STR_RETURN_FULFILLMENT);
					eleOrder.setAttribute(
							AcademyConstants.ATTR_ENTERPRISE_CODE,
							AcademyConstants.PRIMARY_ENTERPRISE);
					eleOrder.setAttribute(AcademyConstants.ATTR_DOC_TYPE,
							AcademyConstants.STR_RETURN_DOCTYPE);

					eleOrder.setAttribute(AcademyConstants.ATTR_RECV_NODE,
							strShipNode);
					eleOrder.setAttribute(
							AcademyConstants.ATTR_RETURN_GIFT_RECIPIENT,
							AcademyConstants.STR_NO);
					eleOrder.setAttribute(AcademyConstants.ATTR_SELL_ORG_CODE,
							AcademyConstants.PRIMARY_ENTERPRISE);
					eleOrder
							.setAttribute(
									AcademyConstants.ATTR_ORDER_NO,
									eleADCInput
											.getAttribute(AcademyConstants.ATTR_ADC_RMANO));
					eleOrder.setAttribute(
							AcademyConstants.ATTR_APPLY_DEFLT_TMPLT,
							AcademyConstants.STR_YES);
					eleOrder.setAttribute(
							AcademyConstants.ATTR_CREATED_AT_NODE,
							AcademyConstants.STR_YES);
					eleOrder.setAttribute(
							AcademyConstants.ATTR_HAS_DERIVED_PARENT,
							AcademyConstants.STR_YES);

					Element elePersonInfoBillTo = XMLUtil
							.getElementByXPath(
									eleInvoiceOrder.getOwnerDocument(),
									AcademyConstants.XPATH_ORDER_INVOICE_PERSON_INFO_BILL_TO);

					Element eleTempPersonInfoBill = (Element) docImportOrderInp
							.importNode(elePersonInfoBillTo, true);
					eleOrder.appendChild(eleTempPersonInfoBill);

					eleOrder
							.setAttribute(
									AcademyConstants.ATTR_CUST_EMAIL_ID,
									elePersonInfoBillTo
											.getAttribute(AcademyConstants.ATTR_EMAILID));

					Document docOrgList = getOrganizationList(env, strShipNode);
					Element eleOrgContactInfo = (Element) docOrgList
							.getDocumentElement()
							.getElementsByTagName(
									AcademyConstants.ELE_CORPORATE_PERSONAL_INFO)
							.item(0);
					Element elePersonInfoShipTo = docImportOrderInp
							.createElement(AcademyConstants.ELEM_PERSON_INFO_SHIP_TO);
					eleOrder.appendChild(elePersonInfoShipTo);

					Element eleTempPersonInfoShip = (Element) docImportOrderInp
							.importNode(eleOrgContactInfo, true);
					XMLUtil.copyElement(docImportOrderInp,
							eleTempPersonInfoShip, elePersonInfoShipTo);

					Element eleOrderLines = docImportOrderInp
							.createElement(AcademyConstants.ELE_ORDER_LINES);
					eleOrder.appendChild(eleOrderLines);

				}
				// Orderline Details
				String strItemId = eleADCOrdLine
						.getAttribute(AcademyConstants.ATTR_ITEM_ID);
				String strRetQty = eleADCOrdLine
						.getAttribute(AcademyConstants.ATTR_RETURN_QTY);
				String strRetReason = eleADCOrdLine
						.getAttribute(AcademyConstants.ATTR_RETURN_REASON);
				Element eleCommonCode = (Element) XPathUtil.getNode(
						docCommonCodeOut.getDocumentElement(),
						"CommonCode[@CodeValue='" + strRetReason + "']");

				if (!YFCObject.isNull(eleCommonCode)) {
					strRetReason = eleCommonCode
							.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
				}

				Element eleOrderLine = docImportOrderInp
						.createElement(AcademyConstants.ELE_ORDER_LINE);

				eleOrderLine.setAttribute(AcademyConstants.ATTR_PRIME_LINE_NO,
						Integer.toString(i + 1));
				eleOrderLine.setAttribute(AcademyConstants.SUB_LINE_NO, Integer
						.toString(1));
				eleOrderLine.setAttribute(AcademyConstants.ATTR_ORDERED_QTY,
						strRetQty);
				eleOrderLine.setAttribute(AcademyConstants.ATTR_RETURN_REASON,
						strRetReason);
				eleOrderLine.setAttribute(AcademyConstants.ATTR_ACTION,
						AcademyConstants.STR_ACTION_CREATE);
				eleOrderLine.setAttribute(AcademyConstants.ATTR_SHIP_NODE,
						strShipNode);
				eleOrderLine.setAttribute(AcademyConstants.ATTR_PIPELINE_ID,
						AcademyConstants.IN_STORE_PIPELINE_ID);

				eleOrderLine.setAttribute(
						AcademyConstants.ATTR_PIPELINE_OWNER_KEY,
						AcademyConstants.HUB_CODE);

				Element eleItem = docImportOrderInp
						.createElement(AcademyConstants.ITEM);
				eleOrderLine.appendChild(eleItem);

				eleItem.setAttribute(AcademyConstants.ATTR_ITEM_ID, strItemId);
				eleItem.setAttribute(AcademyConstants.ATTR_UOM,
						AcademyConstants.UOM_EACH_VAL);

				Element eleDerivedFrom = docImportOrderInp
						.createElement(AcademyConstants.ATTR_DERIVED_FROM);
				eleOrderLine.appendChild(eleDerivedFrom);

				eleDerivedFrom.setAttribute(
						AcademyConstants.ATTR_ORDER_HEADER_KEY,
						strOrderHeaderKey);

				if (hmItemMismatchList.containsKey(strItemId)
						|| hmItemQtyMismatchList.containsKey(strItemId)) {
					SimpleDateFormat sdf = new SimpleDateFormat(
							AcademyConstants.STR_TIME_FORMAT_ORDERLINE_KEY);
					strOrderLineKey = sdf.format(new Date());
				} else {

					Element eleSalesOrderLine = XMLUtil.getElementByXPath(
							eleSalesOrderInvoiceDetails.getOwnerDocument(),
							"OrderInvoiceList/OrderInvoice/LineDetails/LineDetail[@ItemID='"
									+ strItemId + "']");

					strOrderLineKey = eleSalesOrderLine
							.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);

				}

				eleDerivedFrom.setAttribute(
						AcademyConstants.ATTR_ORDER_LINE_KEY, strOrderLineKey);

				Element eleOrderStatuses = docImportOrderInp
						.createElement(AcademyConstants.ELE_ORDER_STATUSES);
				eleOrderLine.appendChild(eleOrderStatuses);

				Element eleOrderStatus = docImportOrderInp
						.createElement(AcademyConstants.ELE_ORDER_STATUS);
				eleOrderStatuses.appendChild(eleOrderStatus);

				eleOrderStatus.setAttribute(AcademyConstants.ATTR_STATUS,
						AcademyConstants.STATUS_RETURN_INVOICED);
				eleOrderStatus.setAttribute(AcademyConstants.ATTR_STAT_QTY,
						strRetQty);

				Element eleSchedule = docImportOrderInp
						.createElement(AcademyConstants.ELE_SCHEDULE);
				eleOrderStatus.appendChild(eleSchedule);
				// Orderline Details

				// Import orderline element
				Element eleOrderLines = XMLUtil.getElementByXPath(
						docImportOrderInp,
						AcademyConstants.XPATH_ORDER_ORDERLINES);
				XMLUtil.importElement(eleOrderLines, eleOrderLine);
				// Import orderline element
			}

		} catch (Exception e) {
			logger.error("Exception - prepareAndInvokeImportOrder()::+e");
		}
		return docImportOrderInp;
	}

	private Document prepareChargeOrdStatusInp(YFSEnvironment env,
			Document docChangeOrderStatusInp, Element eleADCInput,
			Element eleADCOrderLine, Element eleSalesOrderInvoiceDetails,
			HashMap<String, Double> hmADCItemList,
			HashMap<String, Double> hmEligibleItemsList,
			String strTransactionId, String strBaseDropStatus) throws Exception {

		try {

			if (!hmEligibleItemsList.isEmpty()) {
				if (YFCObject.isNull(docChangeOrderStatusInp)) {

					Element eleInvoiceOrder = (Element) eleSalesOrderInvoiceDetails
							.getElementsByTagName(AcademyConstants.ELE_ORDER)
							.item(0);
					docChangeOrderStatusInp = XMLUtil
							.createDocument(AcademyConstants.ELE_ORD_STATUS_CHG);
					Element eleChangeOrderStatusInp = docChangeOrderStatusInp
							.getDocumentElement();

					eleChangeOrderStatusInp
							.setAttribute(
									AcademyConstants.ATTR_ORDER_HEADER_KEY,
									eleInvoiceOrder
											.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));
					eleChangeOrderStatusInp.setAttribute(
							AcademyConstants.ATTR_SELECT_METHOD, "WAIT");
					eleChangeOrderStatusInp.setAttribute(
							AcademyConstants.ATTR_TRANS_ID, strTransactionId);

					Element eleOrderLines = docChangeOrderStatusInp
							.createElement(AcademyConstants.ELE_ORDER_LINES);
					eleChangeOrderStatusInp.appendChild(eleOrderLines);
				}

				String strItemId = eleADCOrderLine
						.getAttribute(AcademyConstants.ATTR_ITEM_ID);

				if (hmEligibleItemsList.containsKey(strItemId)) {

					Element eleOrderLine = docChangeOrderStatusInp
							.createElement(AcademyConstants.ELE_ORDER_LINE);

					String strRetQty = eleADCOrderLine
							.getAttribute(AcademyConstants.ATTR_RETURN_QTY);

					Element eleSalesOrderLine = XMLUtil.getElementByXPath(
							eleSalesOrderInvoiceDetails.getOwnerDocument(),
							"OrderInvoiceList/OrderInvoice/LineDetails/LineDetail[@ItemID='"
									+ strItemId + "']");

					if (null != eleSalesOrderLine) {
						String strOrderLineKey = eleSalesOrderLine
								.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);

						eleOrderLine.setAttribute(
								AcademyConstants.ATTR_ORDER_LINE_KEY,
								strOrderLineKey);

						Element eleOrderStatus = XMLUtil
								.getElementByXPath(
										eleSalesOrderInvoiceDetails
												.getOwnerDocument(),
										"OrderInvoiceList/OrderInvoice/Order/OrderLines/OrderLine"
												+ "[@OrderLineKey='"
												+ strOrderLineKey
												+ "']/OrderStatuses/OrderStatus[@Status='3700'"
												+ "or @Status='3700.7777' or @Status='3700.100']");

						String strOrderReleasekey = eleOrderStatus
								.getAttribute(AcademyConstants.ATTR_RELEASE_KEY);

						eleOrderLine.setAttribute(
								AcademyConstants.ATTR_RELEASE_KEY,
								strOrderReleasekey);

						eleOrderLine.setAttribute(
								AcademyConstants.ATTR_BASEDROP_STATUS,
								strBaseDropStatus);

						Element eleOrdLineTranQty = docChangeOrderStatusInp
								.createElement(AcademyConstants.ELE_ORDER_LINE_TRANSQTY);
						eleOrderLine.appendChild(eleOrdLineTranQty);

						if (hmADCItemList.get(strItemId) > Double
								.parseDouble(strRetQty)) {

							Element eleDupOrderLine = XMLUtil
									.getElementByXPath(docChangeOrderStatusInp,
											"OrderStatusChange/OrderLines/OrderLine[@OrderLineKey='"
													+ strOrderLineKey + "']");
							if (null != eleDupOrderLine) {
								Element eleOrderLines = XMLUtil
										.getElementByXPath(
												docChangeOrderStatusInp,
												AcademyConstants.XPATH_CHANGE_ORDER_STATUS_ORDERLINES);
								XMLUtil.removeChild(eleOrderLines,
										eleDupOrderLine);
							}

							strRetQty = String.valueOf(hmEligibleItemsList
									.get(strItemId));

							eleOrderLine.setAttribute(
									AcademyConstants.ATTR_QUANTITY, strRetQty);

							eleOrdLineTranQty.setAttribute(
									AcademyConstants.ATTR_QUANTITY, strRetQty);

						} else {
							eleOrderLine.setAttribute(
									AcademyConstants.ATTR_QUANTITY, strRetQty);

							eleOrdLineTranQty.setAttribute(
									AcademyConstants.ATTR_QUANTITY, strRetQty);
						}

					}

					Element eleOrderLines = XMLUtil
							.getElementByXPath(
									docChangeOrderStatusInp,
									AcademyConstants.XPATH_CHANGE_ORDER_STATUS_ORDERLINES);
					XMLUtil.importElement(eleOrderLines, eleOrderLine);

				}

			}

		} catch (Exception e) {
			logger.error("Exception - prepareChargeOrdStatusInp():: " + e);
		}
		return docChangeOrderStatusInp;
	}

	private Element prepareAndInvokeGetOrderList(YFSEnvironment env,
			Element eleOrder) throws Exception {

		Document docGetOrderListInp = null;
		Document docGetOrderListOut = null;
		Element eleGetOrderListOut = null;

		try {
			docGetOrderListInp = XMLUtil
					.createDocument(AcademyConstants.ELE_ORDER);
			Element eleOrderInp = docGetOrderListInp.getDocumentElement();
			eleOrderInp.setAttribute(AcademyConstants.ATTR_DOC_TYPE,
					AcademyConstants.STR_RETURN_DOCTYPE);

			Element eleOrderLine = docGetOrderListInp
					.createElement(AcademyConstants.ELE_ORDER_LINE);
			eleOrderInp.appendChild(eleOrderLine);

			Element eleDerivedFrom = docGetOrderListInp
					.createElement(AcademyConstants.ATTR_DERIVED_FROM);
			eleOrderLine.appendChild(eleDerivedFrom);

			eleDerivedFrom
					.setAttribute(
							AcademyConstants.ATTR_ORDER_HEADER_KEY,
							eleOrder
									.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));

			eleDerivedFrom.setAttribute(AcademyConstants.ATTR_ORDER_NO,
					eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_NO));

			eleDerivedFrom.setAttribute(AcademyConstants.ATTR_DOC_TYPE,
					AcademyConstants.SALES_DOCUMENT_TYPE);

			logger.verbose("getOrderList input::"
					+ XMLUtil.getXMLString(docGetOrderListInp));

			env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST,
					AcademyConstants.STR_TEMPLATEFILE_GET_ORDER_LIST_INSTORE);

			docGetOrderListOut = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_ORDER_LIST, docGetOrderListInp);

			env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);

			if (null != docGetOrderListOut) {
				logger.verbose("getOrderList output::"
						+ XMLUtil.getXMLString(docGetOrderListOut));

				eleGetOrderListOut = docGetOrderListOut.getDocumentElement();
			}

		} catch (Exception e) {
			logger.error("Exception - prepareAndInvokeGetOrderList()::" + e);
		}
		return eleGetOrderListOut;
	}

	private void raiseAlertItemOrQuantityMismatch(
			YFSEnvironment yfsEnvironment, Document docImpOrdOut,
			HashMap<String, Double> hmItemMismatch,
			HashMap<String, Double> hmItemQtyMismatch) {

		try {

			Document docMultiApiInp = XMLUtil
					.createDocument(AcademyConstants.ELE_MULTIAPI);
			Element eleMultiApiInp = docMultiApiInp.getDocumentElement();

			Element eleImpOrder = docImpOrdOut.getDocumentElement();
			NodeList nlOrderLines = XMLUtil.getNodeList(eleImpOrder,
					AcademyConstants.XPATH_ORDERLINE);

			for (int i = 0; i < nlOrderLines.getLength(); i++) {
				Element eleOrderLine = (Element) nlOrderLines.item(i);

				Element eleItem = (Element) eleOrderLine.getElementsByTagName(
						AcademyConstants.ELE_IMPORT_ORDER_ITEM).item(0);

				String strItemId = eleItem
						.getAttribute(AcademyConstants.ATTR_ITEM_ID);

				if (hmItemMismatch.containsKey(strItemId)
						|| hmItemQtyMismatch.containsKey(strItemId)) {

					Element eleApi = docMultiApiInp
							.createElement(AcademyConstants.ELE_API);
					eleApi.setAttribute(AcademyConstants.ATTR_NAME,
							AcademyConstants.API_CREATE_EXCEPTION);
					eleMultiApiInp.appendChild(eleApi);

					Element eleInput = docMultiApiInp
							.createElement(AcademyConstants.ELE_INPUT);
					eleApi.appendChild(eleInput);

					Element eleInbox = docMultiApiInp
							.createElement(AcademyConstants.ELE_INBOX);
					eleInput.appendChild(eleInbox);
					eleInbox.setAttribute(AcademyConstants.ATTR_ACTIVE_FLAG,
							AcademyConstants.STR_YES);
					eleInbox
							.setAttribute(
									AcademyConstants.ATTR_ORDER_HEADER_KEY,
									eleImpOrder
											.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));
					eleInbox
							.setAttribute(
									AcademyConstants.ATTR_ORDER_LINE_KEY,
									eleOrderLine
											.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
					eleInbox
							.setAttribute(
									AcademyConstants.ATTR_ORDER_NO,
									eleImpOrder
											.getAttribute(AcademyConstants.ATTR_ORDER_NO));
					eleInbox.setAttribute(AcademyConstants.ATTR_MULTI_API_ITEM_ID,
							strItemId);
					eleInbox
							.setAttribute(
									AcademyConstants.ATTR_SHIP_NODE_KEY,
									eleOrderLine
											.getAttribute(AcademyConstants.ATTR_SHIP_NODE));
					eleInbox.setAttribute(AcademyConstants.ATTR_API_NAME,
							AcademyConstants.STR_INSTORE_RETURN_API_NAME);
					eleInbox.setAttribute(AcademyConstants.ATTR_SUB_FLOW_NAME,
							AcademyConstants.STR_INSTORE_RETURN_API_NAME);
					eleInbox.setAttribute(AcademyConstants.ATTR_DESCRIPTION,
							AcademyConstants.STR_INSTORE_RETURN_EXCEPTION_TYPE);
					eleInbox.setAttribute(AcademyConstants.ATTR_EXCEPTION_TYPE,
							AcademyConstants.STR_INSTORE_RETURN_EXCEPTION_TYPE);
					eleInbox.setAttribute(AcademyConstants.ATTR_FLOW_NAME,
							AcademyConstants.STR_INSTORE_RETURN_FLOW_NAME);
					eleInbox.setAttribute(AcademyConstants.ATTR_QUEUE_ID,
							AcademyConstants.STR_INSTORE_RETURN_QUEUE_ID);

					if (hmItemMismatch.containsKey(strItemId)
							&& hmItemQtyMismatch.containsKey(strItemId)) {
						eleInbox.setAttribute(
								AcademyConstants.ATTR_DETAIL_DESCRIPTION,
								AcademyConstants.STR_ITEM_AND_QTY_MISMATCH);
						eleInbox.setAttribute(
								AcademyConstants.ATTR_ERROR_REASON,
								AcademyConstants.STR_ITEM_AND_QTY_MISMATCH);
					} else if (hmItemMismatch.containsKey(strItemId)) {
						eleInbox.setAttribute(
								AcademyConstants.ATTR_DETAIL_DESCRIPTION,
								AcademyConstants.STR_ITEM_MISMATCH);
						eleInbox.setAttribute(
								AcademyConstants.ATTR_ERROR_REASON,
								AcademyConstants.STR_ITEM_MISMATCH);
					} else {
						eleInbox.setAttribute(
								AcademyConstants.ATTR_DETAIL_DESCRIPTION,
								AcademyConstants.STR_QUANTITY_MISMATCH);
						eleInbox.setAttribute(
								AcademyConstants.ATTR_ERROR_REASON,
								AcademyConstants.STR_QUANTITY_MISMATCH);
					}
				}

			}

			logger.verbose("MultiAPI input ::\n"
					+ XMLUtil.getXMLString(docMultiApiInp));
			AcademyUtil.invokeAPI(yfsEnvironment,
					AcademyConstants.API_MULTI_API, docMultiApiInp);

		} catch (Exception e) {
			logger.error("Exception - raiseAlertItemQuantityMismatch()::" + e);
		}

	}
	// OMNI-98744 Starts
	private void updateTaskQueueForFreeItem(YFSEnvironment yfsEnvironment, Document docImportOrder) throws Exception {
		logger.beginTimer("AcademyProcessInstoreReturnMessage::updateTaskQueueForFreeItem");
		try {
			logger.verbose("Input updateTaskQueueForFreeItem: " + XMLUtil.getXMLString(docImportOrder));
			AcademyUtil.invokeService(yfsEnvironment, AcademyConstants.SERVICE_ACADEMY_CC_RETURN_MANAGE_TASK_Q,
					docImportOrder);
		} catch (Exception e) {
			logger.error("Exception::" + e);
		}
		logger.endTimer("AcademyProcessInstoreReturnMessage::updateTaskQueueForFreeItem");
	}
	//OMNI-98744 ends
	//OMNI-103428 Starts
	private void postMessageForReturnStatusUpdate(YFSEnvironment yfsEnvironment,Document docImportReturnOrderOut) {
		logger.beginTimer("AcademyProcessInstoreReturnMessage::postMessageForReturnStatusUpdate");
		try {
		Element Order=docImportReturnOrderOut.getDocumentElement();
		Order.setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE,AcademyConstants.VAL_RETURN_REFUNDED);
		AcademyUtil.invokeService(yfsEnvironment, AcademyConstants.SERVICE_ACADEMY_RETURN_STATUS_UPDATES_IN_Q , docImportReturnOrderOut);
		}
		catch (Exception e) {
			logger.error("Exception::" + e);
		}
		logger.endTimer("AcademyProcessInstoreReturnMessage::postMessageForReturnStatusUpdate");
	}
	//OMNI-103428 ends
}