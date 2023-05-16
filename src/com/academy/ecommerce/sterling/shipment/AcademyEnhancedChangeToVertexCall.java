package com.academy.ecommerce.sterling.shipment;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.financialtran.AcademyRecordFinTranUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @author dsharma
 * 
 */
public class AcademyEnhancedChangeToVertexCall implements YIFCustomApi {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yantra.interop.japi.YIFCustomApi#setProperties(java.util.Properties)
	 */
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyChangeToVertexCallRequest.class);

	/**
	 *  This method implements to publish Distribute Tax to Vertex while Return order process
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public Document convertOrderToVertexRequest(YFSEnvironment env,
			Document inDoc) throws Exception {
		Document outDoc = null;
		try {
			log
					.beginTimer(" Begining of AcademyEnhancedChangeToVertexCall->convertOrderToVertexRequest() Api");
			Element eleOrder = inDoc.getDocumentElement();
			Element eleQuoteReq = null;
			SimpleDateFormat sDate = new SimpleDateFormat();
			Calendar calendar = Calendar.getInstance();
			sDate.applyPattern("yyyy-MM-dd");

			// Create Root element here
			String tranType=" ";
			String docNumber = "";
			String strOrderNo7=" ";
			String envInvoiceNo = (String) env.getTxnObject("InvoiceNo");
			log.verbose("Invoice No at env Object :: " + envInvoiceNo);
			if (eleOrder.getAttribute("CallType").equals("QuoteCall")) {
				tranType="VertexQuoteCall";
				outDoc = XMLUtil.createDocument("QuotationRequest");
				eleQuoteReq = outDoc.getDocumentElement();
				String orderDate = eleOrder.getAttribute("OrderDate");
				eleQuoteReq.setAttribute("documentDate", YFCObject
						.isVoid(orderDate) ? sDate.format(calendar.getTime())
						: orderDate);
				docNumber = eleOrder.getAttribute("OrderNo");
				strOrderNo7=eleOrder.getAttribute("OrderNo");
			} else if (eleOrder.getAttribute("CallType").equals(
					"DistributeCall")) {
				tranType="VertexDistributeCall";
				outDoc = XMLUtil.createDocument("DistributeTaxRequest");
				eleQuoteReq = outDoc.getDocumentElement();
				eleQuoteReq.setAttribute("documentDate", sDate.format(calendar
						.getTime()));
				/*
				 * if (envInvoiceNo != null && !envInvoiceNo.equals(""))
				 * docNumber = envInvoiceNo; else
				 */
				docNumber = eleOrder.getAttribute("OrderNo");
				strOrderNo7 = getOrderNoForSalesOrderFromReturnOrderInputXML(
						env, inDoc);
			}

			eleQuoteReq.setAttribute("documentNumber", docNumber);

			Element eleToAddress = (Element) eleOrder.getElementsByTagName(
					"PersonInfoShipTo").item(0);
			//Element eleToDestinationAddress = (Element) eleOrder
					//.getElementsByTagName("ShipNodePersonInfo").item(0);
			// Element eleFromAddress = (Element) eleOrdRelease
			// .getElementsByTagName("ShipNodePersonInfo").item(0);
			eleQuoteReq.setAttribute("returnAssistedParametersIndicator",
					"true");
			eleQuoteReq.setAttribute("transactionType", "SALE");

			Element eleSeller = outDoc.createElement("Seller");
			eleQuoteReq.appendChild(eleSeller);
			Element eleCompany = outDoc.createElement("Company");
			eleSeller.appendChild(eleCompany);
			eleCompany.setTextContent(YFSSystem
					.getProperty("oms.vertex.quotation.request.company"));
			Element eleDivision = outDoc.createElement("Division");
			eleSeller.appendChild(eleDivision);
			eleDivision.setTextContent(YFSSystem
					.getProperty("oms.vertex.quotation.request.division"));

			if ("0003".equals(eleOrder.getAttribute("DocumentType"))) {
				// with the new req - changing this to Customer Person info Ship
				// To address as Destination address
				/*
				 * Element eleCustomer = this
				 * .setCustomerAddress(eleToDestinationAddress);
				 * eleQuoteReq.appendChild(outDoc.importNode(eleCustomer,
				 * true));
				 */
				Element eleCustomer = this.setCustomerAddress(eleToAddress);
				eleQuoteReq.appendChild(outDoc.importNode(eleCustomer, true));
				
			} else {
				Element eleCustomer = this.setCustomerAddress(eleToAddress);
				eleQuoteReq.appendChild(outDoc.importNode(eleCustomer, true));
			}
			//String strOrderNo7=eleOrder.getAttribute("OrderNo");
			Element elePay = (Element) eleOrder.getElementsByTagName(
					"PaymentMethod").item(0);
			String sField4 = "";
			if (!YFCObject.isVoid(elePay)) {
				sField4 = elePay.getAttribute("PaymentType");
			} else if ("0003".equals(eleOrder.getAttribute("DocumentType"))) {
				sField4 = getPaymentTypeForSalesOrderFromReturnOrderInputXML(
						env, inDoc);
			}

			DecimalFormat df = new DecimalFormat("#.00");
			NodeList nlOrdlines = eleOrder.getElementsByTagName("OrderLine");
			int iNoOfOrdlines = nlOrdlines.getLength();
			int lineItem = 0;
			for (int i = 0; i < iNoOfOrdlines; i++) {
				Element eleOrdLine = (Element) nlOrdlines.item(i);
				// Start Fix for # 4041 - as Part of release R027
				// Prevent Canceled Order Line to calculate Tax
				if(eleOrdLine.getAttribute(AcademyConstants.ATTR_STATUS).equals("Cancelled"))
					continue;
				// End fix for # 4041
				
				/*Start Fix for #4077
				 * Checks the order line status, and only those order lines which are in Receipt Closed (3950) status, would be included for invoicing.
				*/
				if (eleOrder.getAttribute("CallType").equals(
				"DistributeCall"))
				{
					log.verbose("Inside the block to check if the status of line is 3950 for returns");
					Element orderStatus = (Element) XPathUtil.getNode(
							eleOrdLine,
							"./OrderStatuses/OrderStatus");
					log.verbose("OrderStatusLine is "+XMLUtil.getXMLString(orderStatus.getOwnerDocument()));
					
					if(null!=orderStatus && !"3950".equals(orderStatus.getAttribute("Status")))
					{
						log.verbose("OrderStatus is "+orderStatus.getAttribute("Status"));
							continue;
					}
					
				}
				//End fix for #4077
				
				String sOrdLineKey = eleOrdLine.getAttribute("OrderLineKey");
				String returnReason = eleOrdLine.getAttribute("ReturnReason");
				boolean isAcademyAtFault = getReturnReasonCode(env,
						returnReason);

				double returnQty = Double.parseDouble(eleOrdLine
						.getAttribute("OrderedQty"));
				Element eleLinePrice = (Element) eleOrdLine
						.getElementsByTagName("LinePriceInfo").item(0);
				// String numField2 = eleLinePrice.getAttribute("Tax");
				String sUnitPrice = eleLinePrice.getAttribute("UnitPrice");
				String sField5 = "Return";
				double extendedPrice = 0.0;
				String sDateField1 = "";
				if (eleOrder.getAttribute("CallType").equals("QuoteCall")) {
					sDateField1 = eleOrder.getAttribute("OrderDate");
				} else if (eleOrder.getAttribute("CallType").equals(
						"DistributeCall")) {
					extendedPrice = Double.parseDouble(sUnitPrice) * returnQty;
					Element sReceiptClosed = (Element) XPathUtil.getNode(
							eleOrdLine,
							"./OrderStatuses/OrderStatus[@Status=\""
									+ "3950\"]");
					sDateField1 = sReceiptClosed.getAttribute("StatusDate");
				}
				eleOrdLine.setAttribute("ExtendedPrice", Double
						.toString(extendedPrice));
				eleOrdLine.setAttribute("UnitPrice", sUnitPrice);

				// Merchandise Charge - Discount
				double merchandiseAmount = AcademyRecordFinTranUtil
						.getMerchandiseAmount(eleOrdLine, null, "0003");
				log.verbose("merchandiseAmount : " + merchandiseAmount);
				merchandiseAmount = merchandiseAmount * -1;
				log.verbose("merchandiseAmount * -1 : " + merchandiseAmount);

				// Shipping Charge - Discount
				double shippingAmount = AcademyRecordFinTranUtil
						.getShipingAmount(eleOrdLine, null, "0003");
				log.verbose("shippingAmount : " + shippingAmount);
				shippingAmount = shippingAmount * -1;
				log.verbose("shippingAmount * -1 : " + shippingAmount);

				// Merchandise Tax
				double merchandiseTax = AcademyRecordFinTranUtil
						.getMerchardizeTax(eleOrdLine, null, "0003");

				// Shipping Tax
				double shippingTax = AcademyRecordFinTranUtil.getShipingTax(
						eleOrdLine, null, "0003");

				// Shipping Charges without discount
				double shippingCharges = AcademyRecordFinTranUtil
						.getShipingCharges(eleOrdLine, null, "0003");

				// Form merchandise line - start
				Element eleMerchandiseLineItem = outDoc
						.createElement("LineItem");
				eleQuoteReq.appendChild(eleMerchandiseLineItem);
				eleMerchandiseLineItem.setAttribute("lineItemNumber", Integer
						.toString(lineItem + 1));
				String sShipNode = eleOrdLine.getAttribute("ShipNode");
				Element newLineItem = this.setGeneralAttribute(env, eleOrdLine,
						eleToAddress, sShipNode, eleOrder
								.getAttribute("DocumentType"));
				XMLUtil
						.copyElement(outDoc, newLineItem,
								eleMerchandiseLineItem);

				Element merchandiseLineEleQty = outDoc
						.createElement("Quantity");
				eleMerchandiseLineItem.appendChild(merchandiseLineEleQty);

				merchandiseLineEleQty
						.setTextContent(Double.toString(returnQty));
				merchandiseLineEleQty.setAttribute("unitOfMeasure", "EA");

				Element merchandiseLineEleExtnPrice = outDoc
						.createElement("ExtendedPrice");
				eleMerchandiseLineItem.appendChild(merchandiseLineEleExtnPrice);
				log.verbose("extendedPrice : " + extendedPrice);
				extendedPrice = extendedPrice * -1;
				log.verbose("extendedPrice * -1 :" + extendedPrice);
				merchandiseLineEleExtnPrice.setTextContent(df
						.format(merchandiseAmount));

				Element merchandiseLineEleInputTotalTax = outDoc
						.createElement("InputTotalTax");
				eleMerchandiseLineItem
						.appendChild(merchandiseLineEleInputTotalTax);
				log.verbose("merchandiseTax : " + merchandiseTax);
				merchandiseTax = merchandiseTax * -1.0;
				log.verbose("merchandiseTax * -1.0 : "
						+ df.format(merchandiseTax));
				merchandiseLineEleInputTotalTax.setTextContent(df
						.format(merchandiseTax));

				Element merchandiseLineEleFlexiField = this.setFlexibleFields(
						sOrdLineKey, "0.0", sField4, df.format(extendedPrice),
						sDateField1, sField5,tranType,strOrderNo7);
				eleMerchandiseLineItem.appendChild(outDoc.importNode(
						merchandiseLineEleFlexiField, true));

				// Form merchandise line - end

				// Return Shipping charges
				NodeList nlShipCharge = XPathUtil.getNodeList(eleOrdLine,
						"./LineCharges/LineCharge[@ChargeCategory=\""
								+ "ReturnShippingCharge\"]");
				int inoOfShipCharge = nlShipCharge.getLength();
				double returnShippingCharges = 0.0;
				for (int k = 0; k < inoOfShipCharge; k++) {
					Element eleShipCharge = (Element) nlShipCharge.item(k);
					String sChargeAmt = eleShipCharge
							.getAttribute("ChargeAmount");
					returnShippingCharges = returnShippingCharges
							+ Double.parseDouble(sChargeAmt);
				}

				// Return Shipping taxes
				NodeList nlShipTax = XPathUtil.getNodeList(eleOrdLine,
						"./LineTaxes/LineTax[@ChargeCategory=\""
								+ "ReturnShippingCharge\"]");
				int inoOfShipTax = nlShipTax.getLength();
				double returnShippingTaxes = 0.0;
				for (int k = 0; k < inoOfShipTax; k++) {
					Element eleShipTax = (Element) nlShipTax.item(k);
					String sReturnTax = eleShipTax.getAttribute("Tax");
					returnShippingTaxes = returnShippingTaxes
							+ Double.parseDouble(sReturnTax);
				}

				// Form Shipping Line - start
				Element eleShipLineItem = outDoc.createElement("LineItem");
				eleShipLineItem.setAttribute("lineItemNumber", Integer
						.toString(lineItem + 2));
				eleQuoteReq.appendChild(eleShipLineItem);
				
				String scacAndServiceCode = eleOrdLine.getAttribute("ScacAndService");
				/*
				 * Shipping Short SKU and Tax Code is set here. Tax Code is
				 * referred as productClass. Short SKU depends on SCACAndService
				 * value. This would be fetched from Common code.
				 * 
				 */
				String[] shortSkqAndTaxCode = getShipmentShortSkuAndTaxCode(
						env, scacAndServiceCode);
				Element eleProduct = (Element) newLineItem
						.getElementsByTagName("Product").item(0);
				eleProduct.setTextContent(shortSkqAndTaxCode[0]); // Short SKU
				eleProduct.setAttribute("productClass", shortSkqAndTaxCode[1]); // 127
				
		
				XMLUtil.copyElement(outDoc, newLineItem, eleShipLineItem);
				Element eleShipLineQty = outDoc.createElement("Quantity");
				eleShipLineItem.appendChild(eleShipLineQty);
				eleShipLineQty.setTextContent("1.0");
				eleShipLineQty.setAttribute("unitOfMeasure", "EA");
				Element eleShipLineExtnPrice = outDoc
						.createElement("ExtendedPrice");
				eleShipLineItem.appendChild(eleShipLineExtnPrice);

				if (isAcademyAtFault) {
					eleShipLineExtnPrice.setTextContent(df
							.format(shippingAmount));
				} else {
					eleShipLineExtnPrice.setTextContent(df
							.format(returnShippingCharges));
					shippingCharges = returnShippingCharges * -1;
				}

				Element eleShipLineInputTotalTax = outDoc
						.createElement("InputTotalTax");
				eleShipLineItem.appendChild(eleShipLineInputTotalTax);

				if (isAcademyAtFault) {
					log.verbose("shippingTax : " + shippingTax);
					eleShipLineInputTotalTax.setTextContent(df
							.format(shippingTax * -1));
				} else {
					eleShipLineInputTotalTax.setTextContent(df
							.format(returnShippingTaxes));
				}

				String sField3 = sOrdLineKey.concat("-").concat("SHIPPING");

				Element eleShipLineFlexiField = this.setFlexibleFields(sField3,
						"0.0", sField4, Double.toString(shippingCharges * -1),
						sDateField1, sField5,tranType,strOrderNo7);
				eleShipLineItem.appendChild(outDoc.importNode(
						eleShipLineFlexiField, true));

				// Form Shipping Line - end
			}
			log
					.endTimer(" End of AcademyEnhancedChangeToVertexCall->convertOrderToVertexRequest() Api");
		} catch (YFSException ex) {
			throw ex;
		}
		outDoc = wrapSoapEnvelope(outDoc);
		return outDoc;
	}

	/*
	 * private String getReturnInvoiceNo(YFSEnvironment env, Document inDoc) {
	 * log.verbose("Get Return Invoice No :: getReturnInvoiceNo - start");
	 * String returnInvoiceNo = ""; try { Element order =
	 * inDoc.getDocumentElement(); if (order.getAttribute("OrderHeaderKey") !=
	 * "") { Document inputDoc = XMLUtil.createDocument("OrderInvoice");
	 * inputDoc.getDocumentElement().setAttribute("OrderHeaderKey",
	 * order.getAttribute("OrderHeaderKey"));
	 * inputDoc.getDocumentElement().setAttribute("InvoiceType", "RETURN"); log
	 * .verbose("***** invoking api getOrderInvoiceList from
	 * getReturnInvoiceNo-- input doc : " + XMLUtil.getXMLString(inputDoc));
	 * Document outDoc = AcademyUtil.invokeAPI(env, "getOrderInvoiceList",
	 * inputDoc); Element docElement = outDoc.getDocumentElement(); if
	 * (docElement.getElementsByTagName("OrderInvoice").getLength() > 0) {
	 * Element orderInvoice = (Element) docElement
	 * .getElementsByTagName("OrderInvoice").item(0); returnInvoiceNo =
	 * orderInvoice.getAttribute("InvoiceNo"); } } } catch (Exception ex) {
	 * ex.printStackTrace(); } log.verbose("Return Invoice No is :: " +
	 * returnInvoiceNo + "\n getReturnInvoiceNo - End"); return returnInvoiceNo; }
	 */

	private boolean getReturnReasonCode(YFSEnvironment env, String sReturnReason) {
		int returnReason = Integer.parseInt(sReturnReason);
		if (returnReason <= 3) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * This method constructs request message for Vertex Quote Call
	 * 
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */

	public Document convertOrdRelToVertexRequest(YFSEnvironment env,
			Document inDoc) throws Exception {
		String tranType=" ";
		
		log
				.beginTimer(" Begining of AcademyEnhancedChangeToVertexCall->convertOrdRelToVertexRequest() Api");
		Document outDoc = null;
		log.verbose("Inside convertOrdRelToVertexRequest with input "
				+ XMLUtil.getXMLString(inDoc));
		if (inDoc.getDocumentElement().getAttribute("CallType").equals(
				"QuoteCall")) {
			tranType="vertexQuoteCall";
			outDoc = XMLUtil.createDocument("QuotationRequest");
		} else if (inDoc.getDocumentElement().getAttribute("CallType").equals(
				"InvoiceCall")) {
			tranType="VertexInvoiceCall";
			outDoc = XMLUtil.createDocument("InvoiceRequest");
		} else if (inDoc.getDocumentElement().getAttribute("CallType").equals(
				"DistributeCall")) {
			tranType="vertexDistributeCall";
			outDoc = XMLUtil.createDocument("DistributionTaxRequest");
		}
		Element eleQuoteReq = outDoc.getDocumentElement();
		Element eleOrdRelease = inDoc.getDocumentElement();
		Element eleToAddress = (Element) eleOrdRelease.getElementsByTagName(
				"PersonInfoShipTo").item(0);
		Element eleToDestinationAddress = (Element) eleOrdRelease
				.getElementsByTagName("ShipNodePersonInfo").item(0);

		eleQuoteReq.setAttribute("documentDate", eleOrdRelease
				.getAttribute("OrderDate"));
		eleQuoteReq.setAttribute("documentNumber", XPathUtil.getString(
				eleOrdRelease, "Order/@OrderNo"));

		eleQuoteReq.setAttribute("returnAssistedParametersIndicator", "true");
		eleQuoteReq.setAttribute("transactionType", "SALE");

		Element eleSeller = outDoc.createElement("Seller");
		eleQuoteReq.appendChild(eleSeller);
		Element eleCompany = outDoc.createElement("Company");
		eleSeller.appendChild(eleCompany);
		eleCompany.setTextContent(YFSSystem
				.getProperty("oms.vertex.quotation.request.company"));
		Element eleDivision = outDoc.createElement("Division");
		eleSeller.appendChild(eleDivision);
		eleDivision.setTextContent(YFSSystem
				.getProperty("oms.vertex.quotation.request.division"));

		Element eleOrder = (Element) eleOrdRelease
				.getElementsByTagName("Order").item(0);
		if ("0003".equals(eleOrder.getAttribute("DocumentType"))) {
			// Destination is Node
			Element eleCustomer = this
					.setCustomerAddress(eleToDestinationAddress);
			eleQuoteReq.appendChild(outDoc.importNode(eleCustomer, true));

		} else {
			Element eleCustomer = this.setCustomerAddress(eleToAddress);
			eleQuoteReq.appendChild(outDoc.importNode(eleCustomer, true));
		}
		String strOrderNo7=eleOrder.getAttribute("OrderNo");
		Element elePay = (Element) eleOrder.getElementsByTagName(
				"PaymentMethod").item(0);
		String sPaymentTypeField4 = "";
		if (!YFCObject.isVoid(elePay)) {
			sPaymentTypeField4 = elePay.getAttribute("PaymentType");
		} else if ("0003".equals(eleOrder.getAttribute("DocumentType"))) {
			sPaymentTypeField4 = getPaymentTypeFromSalesOrder(env, inDoc);
		}
		String sField5 = "Regular";
		NodeList nlOrdlines = eleOrdRelease.getElementsByTagName("OrderLine");
		int iNoOfOrdlines = nlOrdlines.getLength();
		int lineItem = 0;
		/*
		 * For each release line, construct two line in vertex request. One for
		 * merchandize price and another for shipping charge.
		 */
		for (int i = 0; i < iNoOfOrdlines; i++) {

			Element eleOrdLine = (Element) nlOrdlines.item(i);
			log.verbose("Order line : "
					+ XMLUtil.getElementXMLString(eleOrdLine));
			// Start Fix for # 4041 - as Part of release R027
			// Prevent Canceled Order Line to calculate Tax
			if(eleOrdLine.getAttribute(AcademyConstants.ATTR_STATUS).equals("Cancelled"))
				continue;
			// End fix for # 4041
			String sOrdLineKey = eleOrdLine.getAttribute("OrderLineKey");
			String sDateField1 = eleOrdLine.getAttribute("ReqShipDate");
			Element eleLinePrice = (Element) eleOrdLine.getElementsByTagName(
					"LinePriceInfo").item(0);
			//String sLineTotal = eleLinePrice.getAttribute("LineTotal");
			String unitPrice = eleLinePrice.getAttribute("UnitPrice");

			//String numTaxField2 = eleLinePrice.getAttribute("Tax");
			double relqty = Double.parseDouble(((Element) eleOrdLine
					.getElementsByTagName("OrderLineTranQuantity").item(0))
					.getAttribute("StatusQuantity"));
			double lineQty = Double.parseDouble(eleOrdLine
					.getAttribute("OrderedQty"));

			double extendedPrice = Double.parseDouble(unitPrice) * lineQty;

			log.verbose("unitprice : " + unitPrice);
			log.verbose("line qty : " + lineQty);
			log.verbose("extended price : " + extendedPrice);

			eleOrdLine.setAttribute("ExtendedPrice", Double
					.toString(extendedPrice));
			eleOrdLine.setAttribute("UnitPrice", unitPrice);

			// Get Merchandise Charge - Discount
			double merchandiseAmount = AcademyRecordFinTranUtil
					.getMerchandiseAmount(eleOrdLine, null, "0001");
			double proRatedMerchandiseAmt = merchandiseAmount
					* (relqty / lineQty);

			// Get Shipping Charge - Discount
			double shippingAmount = AcademyRecordFinTranUtil.getShipingAmount(
					eleOrdLine, null, "0001");
			double proRatedShipingAmt = shippingAmount * (relqty / lineQty);

			// get Merchandise Tax
			double merchandiseTax = AcademyRecordFinTranUtil.getMerchardizeTax(
					eleOrdLine, null, "0001");
			double proratedMerchandiseTax = merchandiseTax * (relqty / lineQty);

			// get Shipping Tax
			double shippingTax = AcademyRecordFinTranUtil.getShipingTax(
					eleOrdLine, null, "0001");
			double proratedShippingTax = shippingTax * (relqty / lineQty);
			// This field is Merchandise Amount without discounts and taxes
			double proratedExtendedPrice = extendedPrice * (relqty / lineQty);
			// get Shipping Charges
			double shippingCharges = AcademyRecordFinTranUtil
					.getShipingCharges(eleOrdLine, null, "0001");
			double proRatedShipingCharges = shippingCharges
					* (relqty / lineQty);

			log.verbose("proRatedMerchandiseAmt : " + proRatedMerchandiseAmt);
			log.verbose("proRatedShipingAmt : " + proRatedShipingAmt);
			log.verbose("proratedMerchandiseTax : " + proratedMerchandiseTax);
			log.verbose("proratedShippingTax : " + proratedShippingTax);
			log.verbose("proratedExtendedPrice : " + proratedExtendedPrice);
			log.verbose("proRatedShipingCharges : " + proRatedShipingCharges);

			/*
			 * Element eleCouponCharge = (Element) XPathUtil.getNode(
			 * eleOrdLine, "./LineCharges/LineCharge[@ChargeCategory=\"" +
			 * "DiscountCoupon\"]"); double couponAmt = 0.0; if (eleCouponCharge !=
			 * null) { couponAmt = Double.parseDouble(eleCouponCharge
			 * .getAttribute("ChargeAmount")); } String numField1 =
			 * Double.toString((Double .parseDouble(sLineTotal) - couponAmt));
			 */
			Element eleLineItem = outDoc.createElement("LineItem");
			eleQuoteReq.appendChild(eleLineItem);
			eleLineItem.setAttribute("lineItemNumber", Integer
					.toString(lineItem + 1));
			String sShipNode = eleOrdRelease.getAttribute("ShipNode");
			Element newLineItem = this.setGeneralAttribute(env, eleOrdLine,
					eleToAddress, sShipNode, eleOrder
							.getAttribute("DocumentType"));
			XMLUtil.copyElement(outDoc, newLineItem, eleLineItem);

			/*
			 * Get all charges/discounts associated with Shipping to calculate
			 * final shipping charge.
			 */
			NodeList nlShipCharge = XPathUtil
					.getNodeList(
							eleOrdLine,
							"./LineCharges/LineCharge[@ChargeCategory=\""
									+ "Shipping\" or (@ChargeCategory='Promotions' and "
									+ "@ChargeName='ShippingPromotion')]");
			int inoOfShipCharge = nlShipCharge.getLength();
			/*
			 * double sShipAmt = 0.0; for (int k = 0; k < inoOfShipCharge; k++) {
			 * Element eleShipCharge = (Element) nlShipCharge.item(k); String
			 * sChargeAmt = eleShipCharge .getAttribute("ChargeAmount");
			 * if("Y".equalsIgnoreCase(eleShipCharge.getAttribute("IsDiscount"))) {
			 * sShipAmt = sShipAmt + (-1 * Double.parseDouble(sChargeAmt)); }
			 * else { sShipAmt = sShipAmt + Double.parseDouble(sChargeAmt); } }
			 */
			if (inoOfShipCharge > 0) {
				Element eleShipLineItem = outDoc.createElement("LineItem");
				eleShipLineItem.setAttribute("lineItemNumber", Integer
						.toString(lineItem + 2));
				eleQuoteReq.appendChild(eleShipLineItem);

				String scacAndServiceCode = eleOrdLine
						.getAttribute("ScacAndService");
				/*
				 * Shipping Short SKU and Tax Code is set here. Tax Code is
				 * referred as productClass. Short SKU depends on SCACAndService
				 * value. This would be fetched from Common code.
				 * 
				 */
				String[] shortSkqAndTaxCode = getShipmentShortSkuAndTaxCode(
						env, scacAndServiceCode);
				Element eleProduct = (Element) newLineItem
						.getElementsByTagName("Product").item(0);
				eleProduct.setTextContent(shortSkqAndTaxCode[0]); // Short SKU
				eleProduct.setAttribute("productClass", shortSkqAndTaxCode[1]); // 127
				// or
				// 157

				XMLUtil.copyElement(outDoc, newLineItem, eleShipLineItem);
				Element eleQty = outDoc.createElement("Quantity");
				eleShipLineItem.appendChild(eleQty);
				// For Shipping SKU's quanity should always be set to 1.0
				eleQty.setTextContent("1.0");
				eleQty.setAttribute("unitOfMeasure", "EA");
				Element eleExtnPrice = outDoc.createElement("ExtendedPrice");
				eleShipLineItem.appendChild(eleExtnPrice);
				// double proRatedShipAmt = sShipAmt * (relqty / lineQty);
				eleExtnPrice
						.setTextContent(Double.toString(proRatedShipingAmt));

				String sField3 = sOrdLineKey.concat("-").concat("SHIPPING");
				Element eleFlexiField = this.setFlexibleFields(sField3, Double
						.toString(proratedShippingTax), sPaymentTypeField4,
						Double.toString(proRatedShipingCharges), sDateField1,
						sField5,tranType,strOrderNo7);
				eleShipLineItem.appendChild(outDoc.importNode(eleFlexiField,
						true));
			}

			Element eleQty = outDoc.createElement("Quantity");
			eleLineItem.appendChild(eleQty);

			eleQty.setTextContent(Double.toString(relqty));

			eleQty.setAttribute("unitOfMeasure", "EA");

			// double extnPrice = ((Double.parseDouble(sLineTotal) - sShipAmt) *
			// (relqty / lineQty));
			Element eleExtnPrice = outDoc.createElement("ExtendedPrice");
			eleLineItem.appendChild(eleExtnPrice);
			//String numField1 = eleOrdLine.getAttribute("UnitPrice");
			eleExtnPrice
					.setTextContent(Double.toString(proRatedMerchandiseAmt));
			Element eleFlexiField = this.setFlexibleFields(sOrdLineKey, Double
					.toString(proratedMerchandiseTax), sPaymentTypeField4,
					Double.toString(proratedExtendedPrice), sDateField1,
					sField5,tranType,strOrderNo7);
			eleLineItem.appendChild(outDoc.importNode(eleFlexiField, true));
			lineItem = lineItem + 2;
		}
		log.verbose("Inside convertOrdRelToVertexRequest with output "
				+ XMLUtil.getXMLString(outDoc));
		outDoc = wrapSoapEnvelope(outDoc);
		log
				.endTimer(" End of AcademyEnhancedChangeToVertexCall->convertOrdRelToVertexRequest() Api");
		return outDoc;
	}

	/**
	 *  This method implements to calculate shipping tax for SalesOrder.
	 *  
	 *  a. Vertex InvoiceCall
	 *  b. Vertex QuoteCall.
	 *  
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public Document convertShipmentToVertexRequest(YFSEnvironment env,
			Document inDoc) throws Exception {
		log
				.beginTimer(" Begining of AcademyEnhancedChangeToVertexCall->convertShipmentToVertexRequest() Api");
		log.verbose("Inside convertShipmentToVertexRequest with input "
				+ XMLUtil.getXMLString(inDoc));
		Document outDoc = null;
		Element eleQuoteReq = null;
		Element eleShipment = inDoc.getDocumentElement();
		String tranType=" ";

		if (inDoc.getDocumentElement().getAttribute("CallType").equals(
				"QuoteCall")) {
			tranType="VertexQuoteCall";
			outDoc = XMLUtil.createDocument("QuotationRequest");
			eleQuoteReq = outDoc.getDocumentElement();

			eleQuoteReq.setAttribute("documentDate", eleShipment
					.getAttribute("ExpectedShipmentDate"));

		} else if (inDoc.getDocumentElement().getAttribute("CallType").equals(
				"InvoiceCall")) {
			tranType="vertexInvoiceCall";
			outDoc = XMLUtil.createDocument("InvoiceRequest");
			eleQuoteReq = outDoc.getDocumentElement();
			eleQuoteReq.setAttribute("documentDate", eleShipment
					.getAttribute("ActualShipmentDate"));
		}

		// CR 2953 - as per new requirement, documentNumber should have shipment
		// invoice number value (NOT Shipment no)
		String documentNumber = "";
		Document orderInvoiceListDoc = getOrderInvoiceList(env, inDoc);
		if (!YFCObject.isVoid(orderInvoiceListDoc)) {
			Element eleOrderInvoiceList = orderInvoiceListDoc
					.getDocumentElement();
			log.verbose("********* ordre invoice list ******* "
					+ XMLUtil.getElementXMLString(eleOrderInvoiceList));
			Element eleExtn = (Element) XPathUtil
					.getNode(eleOrderInvoiceList,
							"/OrderInvoiceList/OrderInvoice[@InvoiceType='PRO_FORMA']/Extn");
			log.verbose("********** Extn element ********** "
					+ XMLUtil.getElementXMLString(eleExtn));
			if (!YFCObject.isVoid(eleExtn)) {
				documentNumber = eleExtn.getAttribute("ExtnInvoiceNo");
			}
		}

		log.verbose("********** document number ********** " + documentNumber);
		eleQuoteReq.setAttribute("documentNumber", documentNumber);

		Element eleToAddress = (Element) eleShipment.getElementsByTagName(
				"ToAddress").item(0);
		Element eleFromAddress = (Element) eleShipment.getElementsByTagName(
				"FromAddress").item(0);

		eleQuoteReq.setAttribute("returnAssistedParametersIndicator", "true");
		eleQuoteReq.setAttribute("transactionType", "SALE");

		Element eleSeller = outDoc.createElement("Seller");
		eleQuoteReq.appendChild(eleSeller);
		Element eleCompany = outDoc.createElement("Company");
		eleSeller.appendChild(eleCompany);
		eleCompany.setTextContent(YFSSystem
				.getProperty("oms.vertex.quotation.request.company"));
		Element eleDivision = outDoc.createElement("Division");
		eleSeller.appendChild(eleDivision);
		eleDivision.setTextContent(YFSSystem
				.getProperty("oms.vertex.quotation.request.division"));

		Element eleCustomer = this.setCustomerAddress(eleToAddress);
		eleQuoteReq.appendChild(outDoc.importNode(eleCustomer, true));
		String sDateField1 = eleShipment.getAttribute("ExpectedShipmentDate");
		String sField5 = "Regular";
		NodeList nlShipLines = eleShipment.getElementsByTagName("ShipmentLine");
		int iNoOfOrdlines = nlShipLines.getLength();
		int lineItem = 0;
		for (int i = 0; i < iNoOfOrdlines; i++) {
			Element eleShipLine = (Element) nlShipLines.item(i);
			String strOrderNo7=eleShipLine.getAttribute("OrderNo");
			String sOrdLineKey = eleShipLine.getAttribute("OrderLineKey");
			double sShipQty = Double.parseDouble(eleShipLine
					.getAttribute("Quantity"));
			Element eleOrdLine = (Element) eleShipLine.getElementsByTagName(
					"OrderLine").item(0);
			// Start Fix for # 4041 - as Part of release R027
			// Prevent Canceled Order Line to calculate Tax
			if(eleOrdLine.getAttribute(AcademyConstants.ATTR_STATUS).equals("Cancelled"))
				continue;
			// End fix for # 4041
			log.verbose("Order Line : "
					+ XMLUtil.getElementXMLString(eleOrdLine));

			double sLineQty = Double.parseDouble(eleOrdLine
					.getAttribute("OrderedQty"));

			Element eleLinePrice = (Element) eleOrdLine.getElementsByTagName(
					"LinePriceInfo").item(0);
		//	String sLineTotal = eleLinePrice.getAttribute("LineTotal");
			String unitPrice = eleLinePrice.getAttribute("UnitPrice");
			// String numField2 = eleLinePrice.getAttribute("Tax");

			Element eleOrder = (Element) eleShipLine.getElementsByTagName(
					"Order").item(0);
			Element elePay = (Element) eleOrder.getElementsByTagName(
					"PaymentMethod").item(0);
			String sField4 = "";
			if (!YFCObject.isVoid(elePay)) {
				sField4 = elePay.getAttribute("PaymentType");
			} else if ("0003".equals(eleOrder.getAttribute("DocumentType"))) {
				sField4 = getPaymentTypeFromSalesOrder(env, inDoc);
			}

			double extendedPrice = Double.parseDouble(unitPrice) * sLineQty;

			eleOrdLine.setAttribute("ExtendedPrice", Double
					.toString(extendedPrice));
			eleOrdLine.setAttribute("UnitPrice", unitPrice);

			// Get Merchandise Charge & Discount
			double merchandiseAmount = AcademyRecordFinTranUtil
					.getMerchandiseAmount(eleOrdLine, null, "0001");
			double proRatedMerchandiseAmt = merchandiseAmount
					* (sShipQty / sLineQty);

			// Get Shipping Charge & Discount
			double shippingAmount = AcademyRecordFinTranUtil.getShipingAmount(
					eleOrdLine, null, "0001");
			double proRatedShipingAmt = shippingAmount * (sShipQty / sLineQty);

			// get Merchandise Tax
			double merchandiseTax = 0.0;
			merchandiseTax = AcademyRecordFinTranUtil.getMerchardizeTax(
					eleOrdLine, null, "0001");

			// get Shipping Tax
			double shippingTax = 0.0;
			shippingTax = AcademyRecordFinTranUtil.getShipingTax(eleOrdLine,
					null, "0001");

			// For InvoiceCall - get the proforma invoice tax of shipment
			if (inDoc.getDocumentElement().getAttribute("CallType").equals(
					"InvoiceCall")) {
				HashMap<String, Double> lProformaTax = new HashMap<String, Double>();
				double proformaShippingTax = 0.0;
				double proformaMerchandiseTax = 0.0;

				if (eleShipment.getAttribute("ShipmentKey") != null) {
					String sShipmentKey = eleShipment
							.getAttribute("ShipmentKey");
					log.verbose("Shipment Key..." + sShipmentKey);
					log.verbose("Order line key : " + sOrdLineKey);
					lProformaTax = this.getProformaTax(env, sShipmentKey,
							sOrdLineKey);
					proformaShippingTax = lProformaTax.get(
							"ProformaShippingTax").doubleValue();
					proformaMerchandiseTax = lProformaTax.get(
							"ProformaMerchandiseTax").doubleValue();

					log.verbose("Proforma shipping tax : "
							+ proformaShippingTax);
					log.verbose("proforma merchandise tax : "
							+ proformaMerchandiseTax);
				}
				merchandiseTax = proformaMerchandiseTax;
				shippingTax = proformaShippingTax;
			}

			DecimalFormat decimalCorrection = new DecimalFormat("0.00");
			double proratedMerchandiseTax = Double
					.parseDouble(decimalCorrection.format(merchandiseTax));
			double proratedShippingTax = Double.parseDouble(decimalCorrection
					.format(shippingTax));

			if (inDoc.getDocumentElement().getAttribute("CallType").equals(
					"QuoteCall")) {
				proratedMerchandiseTax = Double.parseDouble(decimalCorrection
						.format(merchandiseTax * (sShipQty / sLineQty)));
				proratedShippingTax = Double.parseDouble(decimalCorrection
						.format(shippingTax * (sShipQty / sLineQty)));
			}

			// This field is Merchandise Amount without discounts and taxes
			double proratedExtendedPrice = extendedPrice
					* (sShipQty / sLineQty);
			// get Shipping Charges
			double shippingCharges = AcademyRecordFinTranUtil
					.getShipingCharges(eleOrdLine, null, "0001");
			double proRatedShipingCharges = shippingCharges
					* (sShipQty / sLineQty);

			log.verbose("After tax calculation :-");
			log.verbose("proratedMerchandiseTax : " + proratedMerchandiseTax);
			log.verbose("proratedShippingTax : " + proratedShippingTax);
			log.verbose("proratedExtendedPrice : " + proratedExtendedPrice);
			log.verbose("shippingCharges : " + shippingCharges);
			log.verbose("proRatedShipingCharges : " + proRatedShipingCharges);
			
			

			/*
			 * Get all charges/discounts associated with Shipping to calculate
			 * final shipping charge.
			 */
			NodeList nlShipCharge = XPathUtil
					.getNodeList(
							eleOrdLine,
							"./LineCharges/LineCharge[@ChargeCategory=\""
									+ "Shipping\" or (@ChargeCategory='Promotions' and "
									+ "@ChargeName='ShippingPromotion')]");

			int inoOfShipCharge = nlShipCharge.getLength();
			/*
			 * double sShipAmt = 0.0; for (int k = 0; k < inoOfShipCharge; k++) {
			 * Element eleShipCharge = (Element) nlShipCharge.item(k); String
			 * sChargeAmt = eleShipCharge .getAttribute("ChargeAmount");
			 * if("Y".equalsIgnoreCase(eleShipCharge.getAttribute("IsDiscount"))) {
			 * sShipAmt = sShipAmt + (-1 * Double.parseDouble(sChargeAmt)); }
			 * else { sShipAmt = sShipAmt + Double.parseDouble(sChargeAmt); } }
			 */
			if (inoOfShipCharge > 0) {
				Element eleShipLineItem = outDoc.createElement("LineItem");
				eleShipLineItem.setAttribute("lineItemNumber", Integer
						.toString(lineItem + 2));
				eleQuoteReq.appendChild(eleShipLineItem);
				Element FromAddress = this.setSellerAddress(eleFromAddress);
				Element ToAddress= this.setCustomerAddress(eleToAddress);
				eleShipLineItem.appendChild(outDoc
						.importNode(FromAddress, true));
				eleShipLineItem.appendChild(outDoc.importNode(ToAddress, true));

				Element eleProduct = outDoc.createElement("Product");
				eleShipLineItem.appendChild(eleProduct);

				/*
				 * Shipping Short SKU and Tax Code is set here. Tax Code is
				 * referred as productClass. Short SKU depends on SCACAndService
				 * value. This would be fetched from Common code.
				 */
				String[] shortSkqAndTaxCode = getShipmentShortSkuAndTaxCode(
						env, eleShipment.getAttribute("ScacAndService"));
				eleProduct.setTextContent(shortSkqAndTaxCode[0]); // Short SKU
				eleProduct.setAttribute("productClass", shortSkqAndTaxCode[1]); // 127
				// or
				// 157

				Element eleQty = outDoc.createElement("Quantity");
				eleShipLineItem.appendChild(eleQty);
				// eleQty.setTextContent(Double.toString(sShipQty));
				// For Shipping SKU's quanity should always be set to 1.0
				eleQty.setTextContent("1.0");
				eleQty.setAttribute("unitOfMeasure", "EA");
				Element eleExtnPrice = outDoc.createElement("ExtendedPrice");
				eleShipLineItem.appendChild(eleExtnPrice);

				// double proRatedShipAmt = sShipAmt * (sShipQty / sLineQty);
				eleExtnPrice
						.setTextContent(Double.toString(proRatedShipingAmt));
				String sField3 = sOrdLineKey.concat("-").concat("SHIPPING");

				Element eleFlexiFields = this
						.setFlexibleFields(sField3, Double
								.toString(proratedShippingTax), sField4, Double
								.toString(proRatedShipingCharges), sDateField1,
								sField5,tranType,strOrderNo7);
				eleShipLineItem.appendChild(outDoc.importNode(eleFlexiFields,
						true));

			}
			
			// product
			
			Element eleLineItem = outDoc.createElement("LineItem");
			eleQuoteReq.appendChild(eleLineItem);
			eleLineItem.setAttribute("lineItemNumber", Integer
					.toString(lineItem + 1));
			Element FromAddress = this.setSellerAddress(eleFromAddress);
			Element ToAddress = this.setCustomerAddress(eleToAddress);
			eleLineItem.appendChild(outDoc.importNode(FromAddress, true));
			eleLineItem.appendChild(outDoc.importNode(ToAddress, true));
			
			Element eleProduct = outDoc.createElement("Product");
			eleLineItem.appendChild(eleProduct);
			eleProduct.setTextContent(eleShipLine.getAttribute("ItemID"));
			String sItemId = eleShipLine.getAttribute("ItemID");
			String sUOM = eleShipLine.getAttribute("UnitOfMeasure");

			eleProduct.setAttribute("productClass", this.getItemProductCode(
					env, sItemId, sUOM, AcademyConstants.CATALOG_ORG_CODE));
			// Item Id is set as value of Product element
			eleProduct.setTextContent(sItemId);
			
			Element eleQty = outDoc.createElement("Quantity");
			eleLineItem.appendChild(eleQty);
			eleQty.setTextContent(Double.toString(sShipQty));
			eleQty.setAttribute("unitOfMeasure", "EA");

			Element eleExtnPrice = outDoc.createElement("ExtendedPrice");
			// double extnPrice = (Double.parseDouble(sLineTotal) - sShipAmt)
			// * (sShipQty / sLineQty);
			eleExtnPrice
					.setTextContent(Double.toString(proRatedMerchandiseAmt));
			eleLineItem.appendChild(eleExtnPrice);
			// String numField1 = eleLinePrice.getAttribute("UnitPrice");
			Element eleFlexiFields = this.setFlexibleFields(sOrdLineKey, Double
					.toString(proratedMerchandiseTax), sField4, Double
					.toString(proratedExtendedPrice), sDateField1, sField5,tranType,strOrderNo7);
			eleLineItem.appendChild(outDoc.importNode(eleFlexiFields, true));
			lineItem = lineItem + 2;
		}
		log.verbose("Inside convertShipmentToVertexRequest with output "
				+ XMLUtil.getXMLString(outDoc));

		outDoc = wrapSoapEnvelope(outDoc);
		log
				.endTimer(" End of AcademyEnhancedChangeToVertexCall->convertShipmentToVertexRequest() Api");
		return outDoc;
	}

	private Document getOrderInvoiceList(YFSEnvironment env, Document inDoc) {
		log.verbose("***** Inside getOrderInvoiceList  *****");
		Document outDoc = null;
		try {
			Element eleShipment = inDoc.getDocumentElement();
			String shipmentKey = eleShipment.getAttribute("ShipmentKey");
			log.verbose("***** Shipment key  ***** " + shipmentKey);
			if (shipmentKey != null || !shipmentKey.equals("")) {
				Document inputDoc = XMLUtil.createDocument("OrderInvoice");
				inputDoc.getDocumentElement().setAttribute("ShipmentKey",
						shipmentKey);
				log
						.verbose("***** invoking api getOrderInvoiceList -- input doc : "
								+ XMLUtil.getXMLString(inputDoc));
				outDoc = AcademyUtil.invokeAPI(env, "getOrderInvoiceList",
						inputDoc);
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		log.verbose("******* output of getOrderInvoiceList ********* "
				+ XMLUtil.getXMLString(outDoc));
		return outDoc;
	}

	private Element getFromAddress(YFSEnvironment env, String sNode) {
		Element contactAdd = null;
		try {
			log
					.beginTimer(" Begining of AcademyEnhancedChangeToVertexCall->getFromAddress() Api");
			Document getOrgDet = XMLUtil.createDocument("Organization");
			getOrgDet.getDocumentElement().setAttribute("OrganizationCode",
					sNode);
			Document outPutTemplate = YFCDocument
					.getDocumentFor(
							"<Organization><ContactPersonInfo City='' State='' ZipCode='' Country=''/></Organization>")
					.getDocument();

			env.setApiTemplate("getOrganizationHierarchy", outPutTemplate);
			Document orgDetail = AcademyUtil.invokeAPI(env,
					"getOrganizationHierarchy", getOrgDet);
			// cument orgDetail = YFCDocument.getDocumentFor(new
			// File("D://getOrganizationHierarchy.xml")).getDocument();
			contactAdd = (Element) orgDetail.getDocumentElement()
					.getElementsByTagName("ContactPersonInfo").item(0);
			log
					.endTimer(" end of AcademyChangeToVertexCallRequest->getFromAddress() Api");
		} catch (Exception ex) {
			YFSException yfsException = new YFSException();
			yfsException.setStackTrace(ex.getStackTrace());
			throw yfsException;
		}
		return contactAdd;
	}

	/**
	 * Gets the tax product code from Item detail
	 */
	private String getItemProductCode(YFSEnvironment env, String sItemId, String sUOM, String sOrgCode) 
	{
		String productCode = "";
		try 
		{
			log.beginTimer(" Begining of AcademyEnhancedChangeToVertexCall->getItemProductCode() Api");
			Document getItemList = XMLUtil.createDocument("Item");
			getItemList.getDocumentElement().setAttribute("ItemID", sItemId);
			Document outPutTemplate = YFCDocument.getDocumentFor("<ItemList> <Item ItemKey=''><ClassificationCodes/></Item> </ItemList>").getDocument();
			env.setApiTemplate(AcademyConstants.API_GET_ITEM_LIST, outPutTemplate);
			log.verbose("Inside getItemProductCode invoking getItemList with input " + XMLUtil.getXMLString(getItemList));
			Document itemListDocument = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ITEM_LIST, getItemList);
			env.clearApiTemplate(AcademyConstants.API_GET_ITEM_LIST);
			log.verbose("getItemList output " + XMLUtil.getXMLString(itemListDocument));

			if(!YFCObject.isVoid(itemListDocument))
			{
				Element classificationCodes = (Element) itemListDocument.getDocumentElement().getElementsByTagName("ClassificationCodes").item(0);
				productCode = classificationCodes.getAttribute("TaxProductCode");
			}

			log.verbose("productCode " + productCode);
			log.endTimer(" End ofAcademyEnhancedChangeToVertexCall->getItemProductCode() Api");
		} 
		catch (Exception ex) 
		{
			YFSException yfsException = new YFSException();
			yfsException.setStackTrace(ex.getStackTrace());
			throw yfsException;
		}
		
		return productCode;
	}

	private Element setSellerAddress(Element eleFromAddress) {
		Element eleSel = null;
		try {
			log
					.beginTimer(" Begining of AcademyEnhancedChangeToVertexCall->setSellerAddress() Api");
			Document outDoc = XMLUtil.createDocument("Seller");
			eleSel = outDoc.getDocumentElement();
			Element eleOrigin = outDoc.createElement("PhysicalOrigin");
			eleSel.appendChild(eleOrigin);
			Element eleCountry = outDoc.createElement("Country");
			eleOrigin.appendChild(eleCountry);
			eleCountry.setTextContent(YFSSystem
					.getProperty("oms.vertex.quotation.request.country"));
			String strZipCode=eleFromAddress.getAttribute("ZipCode");
			if(YFCObject.isNull(strZipCode))
					{
			Element eleCity = outDoc.createElement("City");
			eleOrigin.appendChild(eleCity);
			eleCity.setTextContent(eleFromAddress.getAttribute("City"));
			Element eleState = outDoc.createElement("MainDivision");
			eleOrigin.appendChild(eleState);
			eleState.setTextContent(eleFromAddress.getAttribute("State"));
			Element eleZipCode = outDoc.createElement("PostalCode");
			eleOrigin.appendChild(eleZipCode);
			eleZipCode.setTextContent(eleFromAddress.getAttribute("ZipCode"));
					}
			else
			{
				Element eleZipCode = outDoc.createElement("PostalCode");
				eleOrigin.appendChild(eleZipCode);
				eleZipCode.setTextContent(eleFromAddress.getAttribute("ZipCode"));
				
			}
			log
					.endTimer(" End of AcademyEnhancedChangeToVertexCall->setSellerAddress() Api");
		} catch (Exception ex) {
			YFSException yfsException = new YFSException();
			yfsException.setStackTrace(ex.getStackTrace());
			throw yfsException;
		}
		return eleSel;
	}

	private Element setCustomerAddress(Element eleToAddress) {
		Element eleCustomer = null;
		String whitegloveReturns = "";
		try {

			log
					.beginTimer(" Begining of AcademyEnhancedChangeToVertexCall->setCustomerAddress() Api");
			Document outDoc = XMLUtil.createDocument("Customer");
			eleCustomer = outDoc.getDocumentElement();
			Element eleDestination = outDoc.createElement("Destination");
			eleCustomer.appendChild(eleDestination);
			Element eleCountry = outDoc.createElement("Country");
			eleDestination.appendChild(eleCountry);
			eleCountry.setTextContent(YFSSystem
					.getProperty("oms.vertex.quotation.request.country"));
			String strZipCode=eleToAddress.getAttribute("ZipCode");
			if(YFCObject.isNull(strZipCode))
			{
			Element eleCity = outDoc.createElement("City");
			eleDestination.appendChild(eleCity);
			eleCity.setTextContent(eleToAddress.getAttribute("City"));
			Element eleState = outDoc.createElement("MainDivision");
			eleDestination.appendChild(eleState);
			eleState.setTextContent(eleToAddress.getAttribute("State"));
			Element eleZipCode = outDoc.createElement("PostalCode");
			eleDestination.appendChild(eleZipCode);
			eleZipCode.setTextContent(eleToAddress.getAttribute("ZipCode"));
			}
			else
			{
				Element eleZipCode = outDoc.createElement("PostalCode");
				eleDestination.appendChild(eleZipCode);
				eleZipCode.setTextContent(eleToAddress.getAttribute("ZipCode"));
			}
			
			log
					.endTimer(" End of AcademyEnhancedChangeToVertexCall->setCustomerAddress() Api");

		} catch (Exception ex) {
			YFSException yfsException = new YFSException();
			yfsException.setStackTrace(ex.getStackTrace());
			throw yfsException;
		}
		return eleCustomer;
	}

	private Element setGeneralAttribute(YFSEnvironment env, Element eleOrdLine,
			Element eleToAddress, String sShipNode, String docType) {
		Document outDoc = null;
		Element eleLineItem = null;
		try {
			log
					.beginTimer(" Begining of AcademyEnhancedChangeToVertexCall->setGeneralAttribute() Api and docType is="
							+ docType);
			outDoc = XMLUtil.createDocument("LineItem");
			eleLineItem = outDoc.getDocumentElement();
			if (sShipNode != null && !sShipNode.equals("")) {
				// Setting phy address as 005 address detials in case of Return
				// Distribute call - as a new change
				if ("0003".equals(docType))
					sShipNode = "005";
				Element eleFromAddress = this.getFromAddress(env, sShipNode);
				if (eleFromAddress != null) {
					if ("0003".equals(docType)) {
						// Return Order; Ship Node is receiver and Customer is
						// sender

						// For Physical Origin; From Customer - 005 address
						// details
						log
								.verbose("Inside the setGeneralAttribute - in case of Return Distribute Call setSellerAddress() & setCustomerAddress()");
						Element eleSeller = this
								.setSellerAddress(eleFromAddress);
						eleLineItem.appendChild(outDoc.importNode(eleSeller,
								true));
						// For Destination; To Ship Node
						Element eleCustomer = this
								.setCustomerAddress(eleToAddress);
						eleLineItem.appendChild(outDoc.importNode(eleCustomer,
								true));

					} else {
						// From Store
						Element eleSeller = this
								.setSellerAddress(eleFromAddress);
						eleLineItem.appendChild(outDoc.importNode(eleSeller,
								true));
						// To Customer Shipping Location
						Element eleCustomer = this
								.setCustomerAddress(eleToAddress);

						eleLineItem.appendChild(outDoc.importNode(eleCustomer,
								true));
					}
				}
			}
			// product
			Element eleProduct = outDoc.createElement("Product");
			Element eleItem = (Element) eleOrdLine.getElementsByTagName("Item")
					.item(0);
			eleProduct.setTextContent(eleItem.getAttribute("ItemID"));
			String sItemId = eleItem.getAttribute("ItemID");
			String sUOM = eleItem.getAttribute("UnitOfMeasure");

			eleProduct.setAttribute("productClass", this.getItemProductCode(
					env, sItemId, sUOM, AcademyConstants.CATALOG_ORG_CODE));

			eleLineItem.appendChild(eleProduct);
			log
					.endTimer(" End of AcademyEnhancedChangeToVertexCall->setGeneralAttribute() Api");
		} catch (Exception ex) {
			YFSException yfsException = new YFSException();
			yfsException.setStackTrace(ex.getStackTrace());
			throw yfsException;
		}
		return eleLineItem;

	}

	private Element setFlexibleFields(String sField3, String numField2,
			String Field4, String numField1, String dateField1, String Field5,String sField6,String sField7) {
		Element eleFlexFields = null;
		try {
			log
					.beginTimer(" Begining of AcademyEnhancedChangeToVertexCall->setFlexibleFields() Api");
			log.verbose("Field 1 : " + numField1);
			log.verbose("Field 2 : " + numField2);
			log.verbose("Field 3 : " + sField3);
			log.verbose("Field 4 : " + Field4);
			log.verbose("Field 5 : " + Field5);
			log.verbose("dateField 1 : " + dateField1);
			log.verbose("Field 7 : " + sField7);
			log.verbose("Field 6: " + sField6);

			Document outDoc = XMLUtil.createDocument("FlexibleFields");

			eleFlexFields = outDoc.getDocumentElement();
			
			Element eleFlexCodeField7 = outDoc
			.createElement("FlexibleCodeField");
			eleFlexFields.appendChild(eleFlexCodeField7);
			eleFlexCodeField7.setTextContent(sField7);
			eleFlexCodeField7.setAttribute("fieldId", "7");
			
			Element eleFlexCodeField6 = outDoc
			.createElement("FlexibleCodeField");
			eleFlexFields.appendChild(eleFlexCodeField6);
			eleFlexCodeField6.setTextContent(sField6);
			eleFlexCodeField6.setAttribute("fieldId", "6");

			Element eleFlexCodeField5 = outDoc
					.createElement("FlexibleCodeField");
			eleFlexFields.appendChild(eleFlexCodeField5);
			eleFlexCodeField5.setTextContent(Field5);
			eleFlexCodeField5.setAttribute("fieldId", "5");

			Element eleFlexCodeField4 = outDoc
					.createElement("FlexibleCodeField");
			eleFlexFields.appendChild(eleFlexCodeField4);
			eleFlexCodeField4.setTextContent(Field4);
			eleFlexCodeField4.setAttribute("fieldId", "4");

			Element eleFlexCodeField3 = outDoc
					.createElement("FlexibleCodeField");
			eleFlexFields.appendChild(eleFlexCodeField3);
			eleFlexCodeField3.setTextContent(sField3);
			eleFlexCodeField3.setAttribute("fieldId", "3");

			Element eleFlexCodeField2 = outDoc
					.createElement("FlexibleCodeField");
			eleFlexFields.appendChild(eleFlexCodeField2);
			eleFlexCodeField2.setTextContent("0005");
			eleFlexCodeField2.setAttribute("fieldId", "2");

			Element eleFlexCodeField1 = outDoc
					.createElement("FlexibleCodeField");
			eleFlexFields.appendChild(eleFlexCodeField1);
			eleFlexCodeField1.setTextContent("0005");
			eleFlexCodeField1.setAttribute("fieldId", "1");

			Element eleFlexNumericField2 = outDoc
					.createElement("FlexibleNumericField");
			eleFlexFields.appendChild(eleFlexNumericField2);
			// get the Extn Promised Tax

			eleFlexNumericField2.setTextContent(numField2);

			eleFlexNumericField2.setAttribute("fieldId", "2");

			Element eleFlexNumericField1 = outDoc
					.createElement("FlexibleNumericField");
			eleFlexFields.appendChild(eleFlexNumericField1);
			// get the Extn Promised Tax
			eleFlexNumericField1.setTextContent(numField1);
			eleFlexNumericField1.setAttribute("fieldId", "1");

			Element eleFlexDateField1 = outDoc
					.createElement("FlexibleDateField");
			eleFlexFields.appendChild(eleFlexDateField1);
			// get the Extn Promised Tax
			eleFlexDateField1.setTextContent(dateField1);
			eleFlexDateField1.setAttribute("fieldId", "1");
			log
					.endTimer(" End of AcademyEnhancedChangeToVertexCall->setFlexibleFields() Api");
		} catch (Exception ex) {
			YFSException yfsException = new YFSException();
			yfsException.setStackTrace(ex.getStackTrace());
			throw yfsException;
		}
		return eleFlexFields;
	}

	/*
	 * private Element getSalesOrderLineDetails(YFSEnvironment env, String
	 * sOrderLineKey) { Element eleOrdLine = null; try { Document getLineDet =
	 * XMLUtil.createDocument("OrderLineDetail");
	 * getLineDet.getDocumentElement().setAttribute("OrderLineKey",
	 * sOrderLineKey); env.setApiTemplate("getOrderLineDetails",
	 * "global/template/api/getOrderLineDetails.CallToVertex.xml");
	 * 
	 * Document LineDetail = AcademyUtil.invokeAPI(env, "getOrderLineDetails",
	 * getLineDet); env.clearApiTemplate("getOrderLineDetails"); eleOrdLine =
	 * LineDetail.getDocumentElement(); } catch (Exception ex) { YFSException
	 * yfsException = new YFSException();
	 * yfsException.setStackTrace(ex.getStackTrace()); throw yfsException; }
	 * return eleOrdLine; }
	 */
	/**
	 * 
	 * @param env
	 * @param eleShipment
	 * @return String[] String[0] - Short SKU String[1] - Tax Code
	 */
	public static String[] getShipmentShortSkuAndTaxCode(YFSEnvironment env,
			String scacAndServiceCode) {
		log.verbose("getShipmentShortSkuAndTaxCode and scacAndServiceCode is "
				+ scacAndServiceCode);
		log
				.beginTimer(" Begin of AcademyEnhancedChangeToVertexCall->getShipmentShortSkuAndTaxCode() Api");
		Document docCommonCodeListOutput = null;
		Document docShipmentChargeSKU = null;
		String[] shortSquAndTaxCode = new String[] { "", "" };
		if (!YFCObject.isVoid(scacAndServiceCode)) {
			try {
				docShipmentChargeSKU = XMLUtil
						.createDocument(AcademyConstants.ELE_COMMON_CODE);
				docShipmentChargeSKU.getDocumentElement().setAttribute(
						AcademyConstants.ATTR_CODE_TYPE, "SHP_CHARGE_SKU");
				docShipmentChargeSKU.getDocumentElement().setAttribute(
						AcademyConstants.ATTR_COMMON_CODE_VALUE,
						scacAndServiceCode);
				docShipmentChargeSKU.getDocumentElement()
						.setAttribute("OrganizationCode",
								AcademyConstants.PRIMARY_ENTERPRISE);
				docCommonCodeListOutput = AcademyUtil.invokeAPI(env,
						AcademyConstants.API_GET_COMMON_CODELIST,
						docShipmentChargeSKU);
				// cCommonCodeListOutput = YFCDocument.getDocumentFor(new
				// File("D://commonCodeShpChargeSKU.xml")).getDocument();
				log
						.verbose("getShipmentShortSkuAndTaxCode and docCommonCodeListOutput is "
								+ XMLUtil.getXMLString(docCommonCodeListOutput));

				if (!YFCObject.isVoid(docCommonCodeListOutput)) {
					shortSquAndTaxCode[0] = XPathUtil.getString(
							docCommonCodeListOutput,
							"CommonCodeList/CommonCode/@CodeShortDescription");
					shortSquAndTaxCode[1] = XPathUtil.getString(
							docCommonCodeListOutput,
							"CommonCodeList/CommonCode/@CodeLongDescription");
				}
			} catch (DOMException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		log.verbose("getShipmentShortSkuAndTaxCode returning [0] : "
				+ shortSquAndTaxCode[0]);
		log.verbose("getShipmentShortSkuAndTaxCode returning [1] : "
				+ shortSquAndTaxCode[1]);
		log
				.endTimer(" End of AcademyEnhancedChangeToVertexCall->getShipmentShortSkuAndTaxCode() Api");
		return shortSquAndTaxCode;
	}

	/**
	 * Method to get vertex request for post invoice Appeasement
	 * 
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */

	public Document convertCreditMemoToVertexRequest(YFSEnvironment env,
			Document inDoc) throws Exception {
		Document outDoc = null;
		String tranType=" ";
		try {
			log
					.beginTimer(" Begin of AcademyEnhancedChangeToVertexCall->convertCreditMemoToVertexRequest() Api");
			if (inDoc.getDocumentElement().getAttribute("CallType").equals(
					"QuoteCall")) {
				tranType="vertexQuoteCall";
				outDoc = XMLUtil.createDocument("QuotationRequest");

			} else if (inDoc.getDocumentElement().getAttribute("CallType")
					.equals("InvoiceCall")) {
				tranType="vertexInvoiceCall";
				outDoc = XMLUtil.createDocument("InvoiceRequest");
			} else if (inDoc.getDocumentElement().getAttribute("CallType")
					.equals("DistributeCall")) {
				tranType="vertexDistributeCall";
				outDoc = XMLUtil.createDocument("DistributionTaxRequest");
			}

			Element eleQuoteReq = outDoc.getDocumentElement();
			Element eleOrdRelease = inDoc.getDocumentElement();
			Element eleToAddress = (Element) eleOrdRelease
					.getElementsByTagName("PersonInfoShipTo").item(0);

			eleQuoteReq.setAttribute("documentDate", eleOrdRelease
					.getAttribute("OrderDate"));
			eleQuoteReq.setAttribute("returnAssistedParametersIndicator",
					"true");
			eleQuoteReq.setAttribute("transactionType", "SALE");

			Element eleSeller = outDoc.createElement("Seller");
			eleQuoteReq.appendChild(eleSeller);
			Element eleCompany = outDoc.createElement("Company");
			eleSeller.appendChild(eleCompany);
			eleCompany.setTextContent(YFSSystem
					.getProperty("oms.vertex.quotation.request.company"));
			Element eleDivision = outDoc.createElement("Division");
			eleSeller.appendChild(eleDivision);
			eleDivision.setTextContent(YFSSystem
					.getProperty("oms.vertex.quotation.request.division"));

			Element eleCustomer = this.setCustomerAddress(eleToAddress);
			eleQuoteReq.appendChild(outDoc.importNode(eleCustomer, true));

			Element eleOrder = (Element) eleOrdRelease.getElementsByTagName(
					"Order").item(0);
			String strOrderNo7=eleOrder.getAttribute("OrderNo");
			/*
			 * Fix for NullPointer Issue While due to
			 * AcademyChangeToVertexCallRequest for customer Appeasements
			 */
			Element elePay = (Element) eleOrdRelease.getElementsByTagName(
					"PaymentMethod").item(0);
			/* End of Fix */
			String sField5 = "Regular";
			String sField4 = "";
			if (!YFCObject.isVoid(elePay)) {
				sField4 = elePay.getAttribute("PaymentType");
			} else if ("0003".equals(eleOrder.getAttribute("DocumentType"))) {
				sField4 = getPaymentTypeFromSalesOrder(env, inDoc);
			}
			/*
			 * Fix for NullPointer Issue While due to
			 * AcademyChangeToVertexCallRequest for customer Appeasements
			 */
			NodeList nlOrdlines = XPathUtil.getNodeList(eleOrdRelease,
					"/Order/OrderLines/OrderLine");
			/* End of Fix */

			int iNoOfOrdlines = nlOrdlines.getLength();

			int lineItem = 0;
			for (int i = 0; i < iNoOfOrdlines; i++) {
				Element eleOrdLine = (Element) nlOrdlines.item(i);
				// Start Fix for # 4041 - as Part of release R027
				// Prevent Canceled Order Line to calculate Tax
				if(eleOrdLine.getAttribute(AcademyConstants.ATTR_STATUS).equals("Cancelled"))
					continue;
				// End fix for # 4041
				String sOrdLineKey = eleOrdLine.getAttribute("OrderLineKey");
				String sDateField1 = eleOrdLine.getAttribute("ReqShipDate");

				Element eleLinePrice = (Element) eleOrdLine
						.getElementsByTagName("LinePriceInfo").item(0);

				String sLineTotal = eleLinePrice.getAttribute("LineTotal");

				String numField2 = eleLinePrice.getAttribute("Tax");
				double relqty = Double.parseDouble(((Element) eleOrdLine
						.getElementsByTagName("OrderLineTranQuantity").item(0))
						.getAttribute("StatusQuantity"));
				double lineQty = Double.parseDouble(eleOrdLine
						.getAttribute("OrderedQty"));
				/*
				 * Element eleCouponCharge = (Element) XPathUtil.getNode(
				 * eleOrdLine, "./LineCharges/LineCharge[@ChargeCategory=\"" +
				 * "DiscountCoupon\"]"); double couponAmt = 0.0; if
				 * (eleCouponCharge != null) { couponAmt =
				 * Double.parseDouble(eleCouponCharge
				 * .getAttribute("ChargeAmount")); } String numField1 =
				 * Double.toString((Double .parseDouble(sLineTotal) -
				 * couponAmt));
				 */
				Element eleLineItem = outDoc.createElement("LineItem");
				eleQuoteReq.appendChild(eleLineItem);
				eleLineItem.setAttribute("lineItemNumber", Integer
						.toString(lineItem + 1));
				String sShipNode = eleOrdRelease.getAttribute("ShipNode");
				Element newLineItem = this.setGeneralAttribute(env, eleOrdLine,
						eleToAddress, sShipNode, eleOrder
								.getAttribute("DocumentType"));
				XMLUtil.copyElement(outDoc, newLineItem, eleLineItem);
				NodeList nlShipCharge = XPathUtil.getNodeList(eleOrdLine,
						"./LineCharges/LineCharge[@ChargeCategory=\""
								+ "Shipping\"]");
				int inoOfShipCharge = nlShipCharge.getLength();
				double sShipAmt = 0.0;
				for (int k = 0; k < inoOfShipCharge; k++) {
					Element eleShipCharge = (Element) nlShipCharge.item(k);
					String sChargeAmt = eleShipCharge
							.getAttribute("ChargeAmount");
					sShipAmt = sShipAmt + Double.parseDouble(sChargeAmt);//8 for shipping
				}

				if (inoOfShipCharge > 0) {
					Element eleShipLineItem = outDoc.createElement("LineItem");
					eleShipLineItem.setAttribute("lineItemNumber", Integer
							.toString(lineItem + 2));
					eleQuoteReq.appendChild(eleShipLineItem);
					XMLUtil.copyElement(outDoc, newLineItem, eleShipLineItem);
					Element eleQty = outDoc.createElement("Quantity");
					eleShipLineItem.appendChild(eleQty);
					eleQty.setTextContent(Double.toString(relqty));
					eleQty.setAttribute("unitOfMeasure", "EA");
					Element eleExtnPrice = outDoc
							.createElement("ExtendedPrice");
					eleShipLineItem.appendChild(eleExtnPrice);
					sShipAmt = sShipAmt * (relqty / lineQty);
					eleExtnPrice.setTextContent(Double.toString(sShipAmt));

					String sField3 = sOrdLineKey.concat("-").concat("SHIPPING");
					Element eleFlexiField = this.setFlexibleFields(sField3,
							numField2, sField4, Double.toString(sShipAmt),
							sDateField1, sField5,tranType,strOrderNo7);
					eleShipLineItem.appendChild(outDoc.importNode(
							eleFlexiField, true));
				}

				Element eleQty = outDoc.createElement("Quantity");
				eleLineItem.appendChild(eleQty);

				eleQty.setTextContent(Double.toString(relqty));

				eleQty.setAttribute("unitOfMeasure", "EA");

				double extnPrice = (Double.parseDouble(sLineTotal) - sShipAmt)
						* (relqty / lineQty);
				Element eleExtnPrice = outDoc.createElement("ExtendedPrice");
				eleLineItem.appendChild(eleExtnPrice);
				String numField1 = eleOrdLine.getAttribute("UnitPrice");
				eleExtnPrice.setTextContent(Double.toString(extnPrice));
				Element eleFlexiField = this.setFlexibleFields(sOrdLineKey,
						numField2, sField4, numField1, sDateField1, sField5,tranType,strOrderNo7);
				eleLineItem.appendChild(outDoc.importNode(eleFlexiField, true));
				lineItem = lineItem + 1;
			}
			log
					.endTimer(" End of AcademyEnhancedChangeToVertexCall->convertCreditMemoToVertexRequest() Api");
		} catch (YFSException ex) {
			throw ex;
		}
		outDoc = wrapSoapEnvelope(outDoc);
		return outDoc;
	}

	private static Document wrapSoapEnvelope(Document inDoc) {
		log
				.beginTimer(" Begin of AcademyEnhancedChangeToVertexCall->wrapSoapEnvelope() Api");
		YFCDocument inputDocument = YFCDocument
				.createDocument("soapenv:Envelope");
		YFCElement inputElement = inputDocument.getDocumentElement();
		inputElement.setAttribute("xmlns:soapenv",
				"http://schemas.xmlsoap.org/soap/envelope/");
		inputElement.setAttribute("xmlns:xs",
				"http://www.w3.org/2001/XMLSchema");
		inputElement.createChild("soapenv:Header");
		YFCElement soapBody = inputElement.createChild("soapenv:Body");
		YFCElement vertexEnv = soapBody.createChild("VertexEnvelope");
		vertexEnv.setAttribute("xmlns", "urn:vertexinc:o-series:tps:5:0");
		vertexEnv.setAttribute("xmlns:xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
		vertexEnv.setAttribute("xsi:schemaLocation",
				"urn:vertexinc:o-series:tps:5:0");
		vertexEnv.setAttribute("xsi:schemaLocation",
				"urn:vertexinc:o-series:tps:5:0 VertexInc_Envelope.xsd");
		YFCElement login = vertexEnv.createChild("Login");
		YFCElement trustedId = login.createChild("TrustedId");
		trustedId.setNodeValue(YFSSystem
				.getProperty("oms.vertex.quotation.request.trustedid"));
		XMLUtil.importElement((Element) vertexEnv.getDOMNode(), inDoc
				.getDocumentElement());
		log
				.endTimer(" End of AcademyEnhancedChangeToVertexCall->wrapSoapEnvelope() Api");
		return inputDocument.getDocument();
	}
	
	private static String getOrderNoForSalesOrderFromReturnOrderInputXML(YFSEnvironment env, Document returnOrderDoc) throws YFSException 
	{
		String OrderNo="";
		try 
		{
			log.beginTimer(" Begin of AcademyEnhancedChangeToVertexCall->getOrderNoForSalesOrderFromReturnOrderInputXML Api");
			log.verbose("inside getOrderNoForSalesOrderFromReturnOrderInputXML with input " + XMLUtil.getXMLString(returnOrderDoc));
			
			String soHeaderKey = XPathUtil.getString(returnOrderDoc,"Order/OrderLines/OrderLine/@DerivedFromOrderHeaderKey");
			log.verbose("Sales Order Header Key = " + soHeaderKey);
			
			Document inXML = YFCDocument.getDocumentFor("<Order OrderHeaderKey ='" + soHeaderKey + "'/>").getDocument();
			Document outPutTemplate = YFCDocument.getDocumentFor("<OrderList> <Order OrderNo=''/> </OrderList>").getDocument();
			env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, outPutTemplate);
			Document salesOrderListDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, inXML);
			env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
			log.verbose("order list output " + XMLUtil.getXMLString(salesOrderListDoc));
			
			if(!YFCObject.isVoid(salesOrderListDoc))
			{
				Element orderElement = (Element)salesOrderListDoc.getElementsByTagName("Order").item(0);
				OrderNo = orderElement.getAttribute("OrderNo");
			}
			
			log.verbose("returning PaymentType " + OrderNo);
			log.endTimer(" End of AcademyEnhancedChangeToVertexCall->getOrderNoForSalesOrderFromReturnOrderInputXML Api");

			return OrderNo;
		}
		catch (Exception e) {
			YFSException yfsException = new YFSException();
			yfsException.setErrorCode(e.getMessage());
			yfsException.setStackTrace(e.getStackTrace());
			throw yfsException;
		}
	}
	

	/**
	 * 
	 * @param env
	 * @param returnOrderDoc
	 * @return
	 * @throws YFSException
	 */
	private static String getPaymentTypeForSalesOrderFromReturnOrderInputXML(YFSEnvironment env, Document returnOrderDoc) throws YFSException 
	{
		String paymentType = "";
		try 
		{
			log.beginTimer(" Begin of AcademyEnhancedChangeToVertexCall->getPaymentTypeForSalesOrderFromReturnOrderInputXML() Api");
			log.verbose("inside getPaymentTypeFromSalesOrder with input "+ XMLUtil.getXMLString(returnOrderDoc));
			
			String soHeaderKey = XPathUtil.getString(returnOrderDoc, "Order/OrderLines/OrderLine/@DerivedFromOrderHeaderKey");
			log.verbose("Sales Order Header Key = " + soHeaderKey);
			
			Document getOrderDetailInDoc = YFCDocument.getDocumentFor("<Order OrderHeaderKey ='" + soHeaderKey + "'/>").getDocument();
			String templateStr = "<OrderList> <Order OrderHeaderKey='' OrderNo=''><PaymentMethods><PaymentMethod ChargeSequence='' PaymentType='' SuspendAnyMoreCharges=''/></PaymentMethods></Order> </OrderList>";
			Document getOrderDetailOutputTemplate = YFCDocument.getDocumentFor(templateStr).getDocument();
			env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, getOrderDetailOutputTemplate);
			Document salesOrderListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, getOrderDetailInDoc);
			env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
			log.verbose("getSODetailOutDoc " + XMLUtil.getXMLString(salesOrderListOutDoc));
			
			if(!YFCObject.isVoid(salesOrderListOutDoc))
			{
				//KER-12036 : Payment Migration Changes to support new Payment Type
				paymentType = XPathUtil.getString(salesOrderListOutDoc, 
						"OrderList/Order/PaymentMethods/PaymentMethod[@SuspendAnyMoreCharges='N' and (@PaymentType='CREDIT_CARD' or @PaymentType='Credit_Card')]/@PaymentType");

				if (YFCObject.isVoid(paymentType)) 
				{
					paymentType = XPathUtil.getString(salesOrderListOutDoc, 
							"OrderList/Order/PaymentMethods/PaymentMethod[@SuspendAnyMoreCharges='N' and @PaymentType='GIFT_CARD']/@PaymentType");
				}
			}

			log.verbose("returning PaymentType " + paymentType);
			log.endTimer(" End of AcademyEnhancedChangeToVertexCall->getPaymentTypeForSalesOrderFromReturnOrderInputXML() Api");
			
			return paymentType;
		} 
		catch (Exception e) 
		{
			YFSException yfsException = new YFSException();
			yfsException.setErrorCode(e.getMessage());
			yfsException.setStackTrace(e.getStackTrace());
			throw yfsException;
		}
	}

	/**
	 * 
	 * @param env
	 * @param returnOrderDoc
	 * @return
	 * @throws YFSException
	 */
	private static String getPaymentTypeFromSalesOrder(YFSEnvironment env, Document returnOrderDoc) throws YFSException 
	{
		String paymentType = "";
		try 
		{
			log.beginTimer(" begin of AcademyEnhancedChangeToVertexCall->getPaymentTypeFromSalesOrder() Api");
			log.verbose("inside getPaymentTypeFromSalesOrder with input " + XMLUtil.getXMLString(returnOrderDoc));

			String returnOrderHeaderKey = returnOrderDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);
			log.verbose("return order header key " + returnOrderHeaderKey);

			Document getOrderListInDoc = YFCDocument.createDocument("Order").getDocument();
			getOrderListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, returnOrderHeaderKey);
			String templateStr = "<OrderList> <Order OrderHeaderKey=''><OrderLines><OrderLine DerivedFromOrderHeaderKey=''/></OrderLines></Order> </OrderList>";
			Document getROListOutputTemplate = YFCDocument.getDocumentFor(templateStr).getDocument();
			env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, getROListOutputTemplate);
			Document getROListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, getOrderListInDoc);
			env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
			log.verbose("getROListOutDoc " + XMLUtil.getXMLString(getROListOutDoc));

			if(!YFCObject.isVoid(getROListOutDoc))
			{
				String soHeaderKey = XPathUtil.getString(getROListOutDoc, "OrderList/Order/OrderLines/OrderLine/@DerivedFromOrderHeaderKey");
				log.verbose("associated sales order header key " + soHeaderKey);

				getOrderListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, soHeaderKey);
				templateStr = "<OrderList> <Order OrderHeaderKey=''><PaymentMethods><PaymentMethod ChargeSequence='' PaymentType='' SuspendAnyMoreCharges=''/></PaymentMethods></Order> </OrderList>";
				getROListOutputTemplate = YFCDocument.getDocumentFor(templateStr).getDocument();
				env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, getROListOutputTemplate);
				Document getSOListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, getOrderListInDoc);
				env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
				log.verbose("getSOListOutDoc " + XMLUtil.getXMLString(getSOListOutDoc));
				
				if(!YFCObject.isVoid(getSOListOutDoc))
				{
					//KER-12036 : Payment Migration Changes to support new Payment Type
					paymentType = XPathUtil.getString(getSOListOutDoc, 
							"OrderList/Order/PaymentMethods/PaymentMethod[@SuspendAnyMoreCharges='N' and (@PaymentType='CREDIT_CARD' or @PaymentType='Credit_Card')]/@PaymentType");
					
					if (YFCObject.isVoid(paymentType)) 
					{
						paymentType = XPathUtil.getString(getSOListOutDoc, 
								"OrderList/Order/PaymentMethods/PaymentMethod[@SuspendAnyMoreCharges='N' and @PaymentType='GIFT_CARD']/@PaymentType");
					}
				}
			}

			log.verbose("returning PaymentType " + paymentType);
			log.endTimer(" End of AcademyChangeToVertexCallRequest->getPaymentTypeFromSalesOrder() Api");
		
			return paymentType;
		}
		catch (Exception e) 
		{
			YFSException yfsException = new YFSException();
			yfsException.setErrorCode(e.getMessage());
			yfsException.setStackTrace(e.getStackTrace());
			throw yfsException;
		}
	}

	private HashMap<String, Double> getProformaTax(YFSEnvironment env,
			String sShipmentKey, String sOrdLineKey) throws Exception {

		HashMap<String, Double> aMap = new HashMap<String, Double>();
		double totalShippingTax = 0.0;
		double totalMerchandiseTax = 0.0;

		try {
			log
					.beginTimer(" Begin of AcademyEnhancedChangeToVertexCall->getProformaTax() Api");
			// call getOrderInvoiceList to get all the proforma Invoices
			Document inDocProformaList = XMLUtil.createDocument("OrderInvoice");
			Element eleInvoice = inDocProformaList.getDocumentElement();
			eleInvoice.setAttribute("InvoiceType", "PRO_FORMA");
			eleInvoice.setAttribute("ShipmentKey", sShipmentKey);
			Document outDocProformaList = AcademyUtil.invokeAPI(env,
					"getOrderInvoiceList", inDocProformaList);
			// get the list of invoices for order..

			NodeList nlInvoiceList = outDocProformaList
					.getElementsByTagName("OrderInvoice");
			int iNoOfInvoice = nlInvoiceList.getLength();

			for (int i = 0; i < iNoOfInvoice; i++) {
				Element eleOrderInvoice = (Element) nlInvoiceList.item(i);
				String sInvoice = eleOrderInvoice.getAttribute("InvoiceNo");
				// call getOrderInvoiceDetails
				Document inDocProformaDetail = XMLUtil
						.createDocument("GetOrderInvoiceDetails");
				Element eleInvoiceDet = inDocProformaDetail
						.getDocumentElement();
				eleInvoiceDet.setAttribute("InvoiceType", "PRO_FORMA");
				eleInvoiceDet.setAttribute("InvoiceNo", sInvoice);

				Document outDocProformaDetail = AcademyUtil.invokeAPI(env,
						"getOrderInvoiceDetails", inDocProformaDetail);
				NodeList nlLineList = outDocProformaDetail
						.getElementsByTagName("LineDetail");
				int iNoOfInvoiceLines = nlLineList.getLength();
				for (int j = 0; j < iNoOfInvoiceLines; j++) {
					Element eleInvoiceLine = (Element) nlLineList.item(j);
					if (sOrdLineKey.equals(eleInvoiceLine
							.getAttribute("OrderLineKey"))) {
						// get the Taxes
						NodeList nlTaxes = eleInvoiceLine
								.getElementsByTagName("LineTax");
						int iNoOftaxes = nlTaxes.getLength();
						for (int k = 0; k < iNoOftaxes; k++) {
							Element eleTaxDet = (Element) nlTaxes.item(k);
							String sChargeCategory = eleTaxDet
									.getAttribute("ChargeCategory");
							String sTaxType = sChargeCategory.concat(eleTaxDet
									.getAttribute("TaxName"));
							aMap.put(sTaxType, Double.valueOf(eleTaxDet
									.getAttribute("Tax")));

							if (sChargeCategory.indexOf("Shipping") > -1) {
								totalShippingTax = totalShippingTax
										+ Double.parseDouble(eleTaxDet
												.getAttribute("Tax"));

							} else if (sChargeCategory.indexOf("TAXES") > -1) {
								totalMerchandiseTax = totalMerchandiseTax
										+ Double.parseDouble(eleTaxDet
												.getAttribute("Tax"));

							}

						}
					}
				}
			}
			log
					.endTimer(" End of AcademyEnhancedChangeToVertexCall->getProformaTax() Api");
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new YFSException(ex.getMessage());
		}
		aMap.put("ProformaShippingTax", Double.valueOf(totalShippingTax));
		aMap.put("ProformaMerchandiseTax", Double.valueOf(totalMerchandiseTax));

		return aMap;
	}

	/**
	 * below method implemented to caluculate return shipping tax for white
	 * glove Returns processing
	 * 
	 * Vertex Return Quote Call
	 */
	public Document convertReturnOrderToVertexRequest(YFSEnvironment env,
			Document inDoc) throws Exception {
		Document outDoc = null;
		String whitegloveReturns = "Y";
		String tranType="vertexReturnOrderInvoiceCall";
		try {
			log
					.verbose("Begining of AcademyEnhancedChangeToVertexCall->convertReturnOrderToVertexRequest() Api and inDoc is "
							+ XMLUtil.getXMLString(inDoc));
			log
					.beginTimer(" Begining of AcademyEnhancedChangeToVertexCall->convertReturnOrderToVertexRequest() Api");
			Element eleOrder = inDoc.getDocumentElement();
			Element eleQuoteReq = null;
			SimpleDateFormat sDate = new SimpleDateFormat();
			Calendar calendar = Calendar.getInstance();
			sDate.applyPattern("yyyy-MM-dd");
			// Create Root element here

			outDoc = XMLUtil.createDocument("QuotationRequest");
			eleQuoteReq = outDoc.getDocumentElement();
			String orderDate = eleOrder.getAttribute("OrderDate");
			eleQuoteReq.setAttribute("documentDate", YFCObject
					.isVoid(orderDate) ? sDate.format(calendar.getTime())
					: orderDate);

			// TODO ??
			eleQuoteReq.setAttribute("documentNumber", eleOrder
					.getAttribute("OrderHeaderKey"));

			Element eleToAddress = (Element) eleOrder.getElementsByTagName(
					"PersonInfoShipTo").item(0);

			Element eleToContactAddress = (Element) eleOrder
					.getElementsByTagName("PersonInfo").item(0);

			// Element eleFromAddress = (Element) eleOrdRelease
			// .getElementsByTagName("ShipNodePersonInfo").item(0);
			eleQuoteReq.setAttribute("returnAssistedParametersIndicator",
					"true");
			eleQuoteReq.setAttribute("transactionType", "SALE");

			Element eleSeller = outDoc.createElement("Seller");
			Element eleCompany = outDoc.createElement("Company");
			eleSeller.appendChild(eleCompany);
			eleCompany.setTextContent(YFSSystem
					.getProperty("oms.vertex.quotation.request.company"));
			Element eleDivision = outDoc.createElement("Division");
			eleSeller.appendChild(eleDivision);
			eleDivision.setTextContent(YFSSystem
					.getProperty("oms.vertex.quotation.request.division"));

			eleQuoteReq.appendChild(eleSeller);

			log.verbose("before calling setAddress() eleToAddress Ele is "
					+ XMLUtil.getElementXMLString(eleToContactAddress));
			Element eleCustomer = null;
			if (eleToContactAddress != null) {
				eleCustomer = this.setCustomerAddress(eleToContactAddress);
				eleQuoteReq.appendChild(outDoc.importNode(eleCustomer, true));
			} else {
				// call getOrgHeirarchy to get the Return ship Node address
				// details.
				log
						.verbose("Calling out getOrganizationHierarchy - in case of it doesn't get additional cls address");
				// Api call to get the CLS address details:
				Document getCLSNodeDoc = XMLUtil.createDocument("Organization");
				getCLSNodeDoc.getDocumentElement().setAttribute(
						AcademyConstants.ORGANIZATION_CODE,
						AcademyConstants.STR_CLS_RECVING_NODE);

				env
						.setApiTemplate("getOrganizationHierarchy",
								"global/template/api/getOrganizationHierarchy.createReturnOrderUEImpl.xml");
				Document clsNodeDocDetails = AcademyUtil.invokeAPI(env,
						"getOrganizationHierarchy", getCLSNodeDoc);
				eleToContactAddress = (Element) clsNodeDocDetails
						.getDocumentElement().getElementsByTagName(
								"ContactPersonInfo").item(0);

				eleCustomer = this.setCustomerAddress(eleToContactAddress);
				eleQuoteReq.appendChild(outDoc.importNode(eleCustomer, true));
			}

			log.verbose("after calling setAddress() customer Ele is "
					+ XMLUtil.getElementXMLString(eleCustomer));

			Element elePay = (Element) eleOrder.getElementsByTagName(
					"PaymentMethod").item(0);
			String strOrderNo7=getOrderNoForSalesOrderFromReturnOrderInputXML(env,inDoc);
			String sField4 = "";

			sField4 = getPaymentTypeForSalesOrderFromReturnOrderInputXML(env,
					inDoc);
			log.verbose("sField4---" + sField4);
			NodeList nlOrdlines = eleOrder.getElementsByTagName("OrderLine");
			int iNoOfOrdlines = nlOrdlines.getLength();
			int lineItem = 0;
			for (int i = 0; i < iNoOfOrdlines; i++) {
				Element eleOrdLine = (Element) nlOrdlines.item(i);
				// Start Fix for # 4041 - as Part of release R027
				// Prevent Canceled Order Line to calculate Tax
				if(eleOrdLine.getAttribute(AcademyConstants.ATTR_STATUS).equals("Cancelled"))
					continue;
				// End fix for # 4041
				String returnOrdLineKey = eleOrdLine
						.getAttribute("OrderLineKey");

				// String numField2 = eleLinePrice.getAttribute("Tax");
				String sField5 = "Return";
				String extendedPrice = "0";
				String sDateField1 = sDate.format(calendar.getTime());

				NodeList nlShipCharge = XPathUtil.getNodeList(eleOrdLine,
						"./LineCharges/LineCharge[@ChargeCategory=\""
								+ "ReturnShippingCharge\"]");

				if (nlShipCharge.getLength() > 0) {
					extendedPrice = ((Element) nlShipCharge.item(0))
							.getAttribute("ChargePerLine");
				}
				log.verbose("Return Shiping Charge El Details are :"
						+ extendedPrice + " and XML is "
						+ XMLUtil.getXMLString(outDoc));

				// Form merchandise line - start
				Element eleReturnShippingChargeLineItem = outDoc
						.createElement("LineItem");
				eleQuoteReq.appendChild(eleReturnShippingChargeLineItem);
				lineItem = lineItem + 1;
				eleReturnShippingChargeLineItem.setAttribute("lineItemNumber",
						Integer.toString(lineItem));

				Element eleExtendedPrice = outDoc
						.createElement("ExtendedPrice");
				eleExtendedPrice.setTextContent(extendedPrice);

				/*
				 * Shipping Short SKU and Tax Code is set here. Tax Code is
				 * referred as productClass. Short SKU depends on SCACAndService
				 * value. This would be fetched from Common code.
				 * 
				 */
				Element eleProduct = outDoc.createElement("Product");
				String scacAndServiceCode = eleOrdLine
						.getAttribute("ScacAndService");
				log.verbose("scacAndServiceCode=" + scacAndServiceCode);
				String[] shortSkqAndTaxCode = getShipmentShortSkuAndTaxCode(
						env, scacAndServiceCode);
				eleProduct.setTextContent(shortSkqAndTaxCode[0]); // Short SKU
				eleProduct.setAttribute("productClass", shortSkqAndTaxCode[1]); // 127
				// or
				// 157
				eleReturnShippingChargeLineItem.appendChild(eleProduct);

				eleExtendedPrice.setTextContent(extendedPrice);
				eleReturnShippingChargeLineItem.appendChild(eleExtendedPrice);
				// adding Physical Origin to Quote Request
				// Element eleLineSeller = outDoc.createElement("Seller");
				Element eleOrigin = outDoc.createElement("PhysicalOrigin");

				Element eleCountry = outDoc.createElement("Country");
				eleOrigin.appendChild(eleCountry);
				eleCountry.setTextContent(eleToAddress.getAttribute("Country"));
				Element eleCity = outDoc.createElement("City");
				eleOrigin.appendChild(eleCity);
				eleCity.setTextContent(eleToAddress.getAttribute("City"));
				Element eleState = outDoc.createElement("MainDivision");
				eleOrigin.appendChild(eleState);
				eleState.setTextContent(eleToAddress.getAttribute("State"));
				Element eleZipCode = outDoc.createElement("PostalCode");
				eleOrigin.appendChild(eleZipCode);
				eleZipCode.setTextContent(eleToAddress.getAttribute("ZipCode"));

				// eleSeller.appendChild(eleOrigin);

				eleReturnShippingChargeLineItem.appendChild((eleOrigin));

				eleReturnShippingChargeLineItem.appendChild(outDoc.importNode(
						eleCustomer, true));

				Element returnShipLineEleQty = outDoc.createElement("Quantity");
				returnShipLineEleQty.setTextContent(eleOrdLine
						.getAttribute("OrderedQty"));
				returnShipLineEleQty.setAttribute("unitOfMeasure", "EA");
				eleReturnShippingChargeLineItem
						.appendChild(returnShipLineEleQty);

				Element returnLineEleFlexiField = this.setFlexibleFields(
						returnOrdLineKey, "0.0", sField4, extendedPrice,
						sDateField1, sField5,tranType,strOrderNo7);
				log
						.verbose("final outDoc- Vertex Request XML is returnLineEleFlexiField "
								+ XMLUtil
										.getElementXMLString(returnLineEleFlexiField));
				eleReturnShippingChargeLineItem.appendChild(outDoc.importNode(
						returnLineEleFlexiField, true));
				log
						.verbose("final outDoc- Vertex Request XML is eleReturnShippingChargeLineItem- "
								+ XMLUtil
										.getElementXMLString(eleReturnShippingChargeLineItem));
			}
			log
					.endTimer(" End of AcademyEnhancedChangeToVertexCall->convertReturnOrderToVertexRequest() Api");
			log.verbose("final outDoc- Vertex Request XML is "
					+ XMLUtil.getXMLString(outDoc));
		} catch (YFSException ex) {
			throw ex;
		}
		outDoc = wrapSoapEnvelope(outDoc);
		return outDoc;
	}

	public static void main(String[] args) throws Exception 
	{
		Document doc = YFCDocument.getDocumentFor(new File("C://input.xml")).getDocument();
		log.verbose(XMLUtil.getXMLString(doc));
		Element orderElement = (Element)doc.getElementsByTagName("Order").item(0);
		String OrderNo = orderElement.getAttribute("OrderNo");
		log.verbose("returning PaymentType " + OrderNo);
		log.endTimer(" End of AcademyEnhancedChangeToVertexCall->getOrderNoForSalesOrderFromReturnOrderInputXML Api");
		log.verbose(OrderNo);
	}
}
