package com.academy.ecommerce.sterling.shipment;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;

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
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @author dsharma
 * 
 */
public class AcademyChangeToVertexCallRequest implements YIFCustomApi {

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
					.beginTimer(" Begining of AcademyChangeToVertexCallRequest->convertOrderToVertexRequest() Api");
			Element eleOrder = inDoc.getDocumentElement();
			Element eleQuoteReq = null;
			SimpleDateFormat sDate = new SimpleDateFormat();
			Calendar calendar = Calendar.getInstance();
			sDate.applyPattern("yyyy-MM-dd");

			// Create Root element here
			String docNumber = "";
			String envInvoiceNo = (String) env.getTxnObject("InvoiceNo");
			log.verbose("Invoice No at env Object :: " + envInvoiceNo);
			if (eleOrder.getAttribute("CallType").equals("QuoteCall")) {
				outDoc = XMLUtil.createDocument("QuotationRequest");
				eleQuoteReq = outDoc.getDocumentElement();
				String orderDate = eleOrder.getAttribute("OrderDate");
				eleQuoteReq.setAttribute("documentDate", YFCObject
						.isVoid(orderDate) ? sDate.format(calendar.getTime())
						: orderDate);
				docNumber = eleOrder.getAttribute("OrderNo");
			} else if (eleOrder.getAttribute("CallType").equals(
					"DistributeCall")) {
				outDoc = XMLUtil.createDocument("DistributeTaxRequest");
				eleQuoteReq = outDoc.getDocumentElement();
				eleQuoteReq.setAttribute("documentDate", sDate.format(calendar
						.getTime()));
				/*
				 * if (envInvoiceNo != null && !envInvoiceNo.equals(""))
				 * docNumber = envInvoiceNo; else
				 */
				docNumber = eleOrder.getAttribute("OrderNo");
			}

			eleQuoteReq.setAttribute("documentNumber", docNumber);
			// CR - Vertex Request changes; Set Sterling Function/Transaction name as FlexibleField 6
			String strTransType = eleOrder.getAttribute("TranType");
			// CR - Vertex Request changes; Set SalesOrder No as FlexibleField 7
			String derivedSalesOrderNo = eleOrder.getAttribute("DerivedOrderNo");
			
			/*START WN-199 Returns: Academy is unable to receive a tax credit after a return is initiated (BE Dev)
			Changing the Zipcode from CLS Address to Customer origin Address */
			/*Element eleToAddress = (Element) eleOrder.getElementsByTagName(
					"PersonInfoShipTo").item(0);*/
			Element eleToAddress = XMLUtil.getElementByXPath(inDoc, AcademyConstants.XPATH_PERSON_INFO_SHIP_TO);
			//END WN-199 Returns: Academy is unable to receive a tax credit after a return is initiated (BE Dev)
			
			Element eleToDestinationAddress = (Element) eleOrder
					.getElementsByTagName("ShipNodePersonInfo").item(0);
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

			Element elePay = (Element) eleOrder.getElementsByTagName(
					"PaymentMethod").item(0);
			String sField4 = "";
			if ("0003".equals(eleOrder.getAttribute("DocumentType"))) {
				// prevent detail API unnecessarily
				sField4 = eleOrder.getAttribute("DerivedPaymentTender");
				if(YFSObject.isNull(sField4) || YFSObject.isVoid(sField4)){
					sField4 = getPaymentTypeForSalesOrderFromReturnOrderInputXML(env, inDoc);
				}
			}else if (!YFCObject.isVoid(elePay)) {
				sField4 = elePay.getAttribute("PaymentType");
			}  

			DecimalFormat df = new DecimalFormat("#0.00");
			//CR- Vertex Changes; Apply Sorting on OrderLine/@PrimeLineNo
			YFCDocument yfcDoc = YFCDocument.getDocumentFor(inDoc);
			YFCElement yfcOrderLines = yfcDoc.getDocumentElement().getChildElement(AcademyConstants.ELE_ORDER_LINES);
			yfcOrderLines.sortNumericChildren(new String[]{AcademyConstants.ATTR_PRIME_LINE_NO});
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
				if (eleOrder.getAttribute("CallType").equals("DistributeCall"))
				{
					log.verbose("Inside the block to check if the status of line is 3950 for returns");
					//NodeList lstOrderStatus =  XPathUtil.getNodeList(eleOrdLine,"./OrderStatuses/OrderStatus[@Status='3950']");
					NodeList lstOrderStatus =  XPathUtil.getNodeListWS(eleOrdLine,"./OrderStatuses/OrderStatus[@Status='3950']",XPathConstants.NODESET);
					log.verbose("OrderStatusLine is "+lstOrderStatus.getLength());
					
					if(lstOrderStatus.getLength()<=0)
						continue;					
					
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
				double retReceivedQty = 0.0;
				if (eleOrder.getAttribute("CallType").equals("QuoteCall")) {
					sDateField1 = eleOrder.getAttribute("OrderDate");
				} else if (eleOrder.getAttribute("CallType").equals(
						"DistributeCall")) {
					/*Element sReceiptClosed = (Element) XPathUtil.getNode(
							eleOrdLine,
							"./OrderStatuses/OrderStatus[@Status=\""
									+ "3950\"]");*/
					Element sReceiptClosed = (Element) XPathUtil.getNodeWS(
							eleOrdLine,
							"./OrderStatuses/OrderStatus[@Status=\""
									+ "3950\"]",XPathConstants.NODE);
					sDateField1 = sReceiptClosed.getAttribute("StatusDate");
					//NodeList lstReceiptClosed = XPathUtil.getNodeList(eleOrdLine, "OrderStatuses/OrderStatus[@Status='3950']/OrderStatusTranQuantity");
					NodeList lstReceiptClosed = XPathUtil.getNodeListWS(eleOrdLine, "OrderStatuses/OrderStatus[@Status='3950']/OrderStatusTranQuantity",XPathConstants.NODESET);
					// Continue the next order line if the current Order Line is not received return fully/partially
					if(lstReceiptClosed.getLength()<=0)
						continue;
					
					for(int a=0;a<lstReceiptClosed.getLength(); a++){
						String statusQty = ((Element)lstReceiptClosed.item(a)).getAttribute("StatusQty");
						if(statusQty != null){
							retReceivedQty += Double.parseDouble(statusQty);
						}
					}
					if(retReceivedQty == 0.0)
						retReceivedQty = returnQty;
					extendedPrice = Double.parseDouble(sUnitPrice) * retReceivedQty;			
				}
				eleOrdLine.setAttribute("ExtendedPrice", Double
						.toString(extendedPrice));
				eleOrdLine.setAttribute("UnitPrice", sUnitPrice);

				// Merchandise Charge - Discount
				double merchandiseAmount = AcademyRecordFinTranUtil
						.getMerchandiseAmount(eleOrdLine, null, "0003");
				log.verbose("merchandiseAmount : " + merchandiseAmount);
				/*double proRatedMerchandiseAmt = merchandiseAmount* (retReceivedQty / returnQty);
				log.verbose("proRatedMerchandiseAmt : "+proRatedMerchandiseAmt); */
				merchandiseAmount = merchandiseAmount * -1;
				log.verbose("merchandiseAmount * -1 : " + merchandiseAmount);

				// Shipping Charge - Discount
				double shippingAmount = AcademyRecordFinTranUtil
						.getShipingAmount(eleOrdLine, null, "0003");
				log.verbose("shippingAmount : " + shippingAmount);
				/*if(shippingAmount != 0){
					double proRatedShippingChargeAmt = shippingAmount* (retReceivedQty / returnQty);
					log.verbose("proRatedShippingChargeAmt : "+proRatedShippingChargeAmt);
				} */
				shippingAmount = shippingAmount * -1;
				log.verbose("shippingAmount * -1 : " + shippingAmount);

				// Merchandise Tax
				double merchandiseTax = AcademyRecordFinTranUtil
						.getMerchardizeTax(eleOrdLine, null, "0003");
				log.verbose("merchandiseTax : "+merchandiseTax);
				/*if(merchandiseTax != 0){
					merchandiseTax = merchandiseTax*(retReceivedQty/returnQty);
					log.verbose("Prorated merchandiseTax : "+merchandiseTax);
				}*/

				// Shipping Tax
				double shippingTax = AcademyRecordFinTranUtil.getShipingTax(
						eleOrdLine, null, "0003");
				log.verbose("shippingTax : "+shippingTax);
				/*if(shippingTax != 0){
					shippingTax = shippingTax*(retReceivedQty/returnQty);
					log.verbose("Prorated shippingTax : "+shippingTax);
				}*/

				// Shipping Charges without discount
				double shippingCharges = AcademyRecordFinTranUtil
						.getShipingCharges(eleOrdLine, null, "0003");
				log.verbose("shippingCharges : "+shippingCharges);
				/*if(shippingCharges != 0){
					shippingCharges = shippingCharges*(retReceivedQty/returnQty);
					log.verbose("Prorated shippingCharges : "+shippingCharges);
				}*/

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
						.setTextContent(Double.toString(retReceivedQty));
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
				if(merchandiseTax != 0.0){
					merchandiseTax = merchandiseTax * -1.0;
					log.verbose("merchandiseTax * -1.0 : "+ df.format(merchandiseTax));
					merchandiseLineEleInputTotalTax.setTextContent(df.format(merchandiseTax));
				}else{
					merchandiseTax = merchandiseTax * -1.0;
					merchandiseLineEleInputTotalTax.setTextContent(String.valueOf(merchandiseTax));
				}
				
				Element merchandiseLineEleFlexiField = this.setFlexibleFields(
						sOrdLineKey, "0.0", sField4, df.format(extendedPrice),
						sDateField1, sField5, strTransType, derivedSalesOrderNo);
				eleMerchandiseLineItem.appendChild(outDoc.importNode(
						merchandiseLineEleFlexiField, true));

				// Form merchandise line - end

				// Return Shipping charges
				/*NodeList nlShipCharge = XPathUtil.getNodeList(eleOrdLine,
						"./LineCharges/LineCharge[@ChargeCategory=\""
								+ "ReturnShippingCharge\"]");*/
				NodeList nlShipCharge = XPathUtil.getNodeListWS(eleOrdLine,
						"./LineCharges/LineCharge[@ChargeCategory=\""
								+ "ReturnShippingCharge\"]",XPathConstants.NODESET);
				int inoOfShipCharge = nlShipCharge.getLength();
				double returnShippingCharges = 0.0;
				for (int k = 0; k < inoOfShipCharge; k++) {
					Element eleShipCharge = (Element) nlShipCharge.item(k);
					String sChargeAmt = eleShipCharge
							.getAttribute("ChargeAmount");
					returnShippingCharges = returnShippingCharges
							+ Double.parseDouble(sChargeAmt);
				}
				log.verbose("returnShippingCharges : "+returnShippingCharges);
				/*if(returnShippingCharges != 0){
					returnShippingCharges = returnShippingCharges*(retReceivedQty/returnQty);
					log.verbose("Prorated returnShippingCharges : "+returnShippingCharges);
				} */

				// Return Shipping taxes
				/*NodeList nlShipTax = XPathUtil.getNodeList(eleOrdLine,
						"./LineTaxes/LineTax[@ChargeCategory=\""
								+ "ReturnShippingCharge\"]");*/
				NodeList nlShipTax = XPathUtil.getNodeListWS(eleOrdLine,
						"./LineTaxes/LineTax[@ChargeCategory=\""
								+ "ReturnShippingCharge\"]",XPathConstants.NODESET);
				int inoOfShipTax = nlShipTax.getLength();
				double returnShippingTaxes = 0.0;
				for (int k = 0; k < inoOfShipTax; k++) {
					Element eleShipTax = (Element) nlShipTax.item(k);
					String sReturnTax = eleShipTax.getAttribute("Tax");
					returnShippingTaxes = returnShippingTaxes
							+ Double.parseDouble(sReturnTax);
				}
				log.verbose("returnShippingTaxes : "+returnShippingTaxes);
				/*if(returnShippingTaxes != 0){
					returnShippingTaxes = returnShippingTaxes*(retReceivedQty/returnQty);
					log.verbose("Prorated returnShippingTaxes : "+returnShippingTaxes);
				} */

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
					if(shippingTax != 0.0)
						eleShipLineInputTotalTax.setTextContent(df.format(shippingTax * -1));
					else
						eleShipLineInputTotalTax.setTextContent(String.valueOf(shippingTax * -1));
				} else {
					eleShipLineInputTotalTax.setTextContent(df
							.format(returnShippingTaxes));
				}

				String sField3 = sOrdLineKey.concat("-").concat("SHIPPING");

				Element eleShipLineFlexiField = this.setFlexibleFields(sField3,
						"0.0", sField4, Double.toString(shippingCharges * -1),
						sDateField1, sField5,strTransType, derivedSalesOrderNo);
				eleShipLineItem.appendChild(outDoc.importNode(
						eleShipLineFlexiField, true));

				// Form Shipping Line - end
				lineItem = lineItem+2;
			}
			log
					.endTimer(" End of AcademyChangeToVertexCallRequest->convertOrderToVertexRequest() Api");
		} catch (YFSException ex) {
			throw ex;
		}
		//Start : OMNI-24737 : Vertex Call Failure Scenario
		env.setTxnObject("VertexRequestWithoutSoapEnvelope", outDoc);
		//End : OMNI-24737
		//outDoc = wrapSoapEnvelope(outDoc);
		//Start : OMNI-26977 : Vertex-Lite Enable Flag
		if (!YFCObject.isVoid(YFSSystem.getProperty(AcademyConstants.IS_VERTEX_LITE_ENABLED)) && 
				AcademyConstants.STR_YES.equalsIgnoreCase(YFSSystem.getProperty(AcademyConstants.IS_VERTEX_LITE_ENABLED))) {
			outDoc = wrapSoapEnvelopeForVertex9(outDoc);
		} else {
			outDoc = wrapSoapEnvelopeForVertex7(outDoc);
		}
		//End : OMNI-26977		
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
		log
				.beginTimer(" Begining of AcademyChangeToVertexCallRequest->convertOrdRelToVertexRequest() Api");
		Document outDoc = null;
		log.verbose("Inside convertOrdRelToVertexRequest with input "
				+ XMLUtil.getXMLString(inDoc));
		if (inDoc.getDocumentElement().getAttribute("CallType").equals(
				"QuoteCall")) {
			outDoc = XMLUtil.createDocument("QuotationRequest");
		} else if (inDoc.getDocumentElement().getAttribute("CallType").equals(
				"InvoiceCall")) {
			outDoc = XMLUtil.createDocument("InvoiceRequest");
		} else if (inDoc.getDocumentElement().getAttribute("CallType").equals(
				"DistributeCall")) {
			outDoc = XMLUtil.createDocument("DistributionTaxRequest");
		}
		
		Element eleQuoteReq = outDoc.getDocumentElement();
		Element eleOrdRelease = inDoc.getDocumentElement();
		// CR - Vertex Request changes; Set Sterling Function/Transaction name as FlexibleField 6
		String strTransType = eleOrdRelease.getAttribute("TranType");
		Element eleToAddress = (Element) eleOrdRelease.getElementsByTagName(
				"PersonInfoShipTo").item(0);
		Element eleToDestinationAddress = (Element) eleOrdRelease
				.getElementsByTagName("ShipNodePersonInfo").item(0);

		eleQuoteReq.setAttribute("documentDate", eleOrdRelease
				.getAttribute("OrderDate"));
		//String strOrderNo = XPathUtil.getString(eleOrdRelease, "Order/@OrderNo");
		String strOrderNo = XPathUtil.getStringWS(eleOrdRelease, "Order/@OrderNo",XPathConstants.STRING);
		eleQuoteReq.setAttribute("documentNumber", strOrderNo);

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

		Element elePay = (Element) eleOrder.getElementsByTagName(
				"PaymentMethod").item(0);
		String sPaymentTypeField4 = "";
		if (!YFCObject.isVoid(elePay)) {
			sPaymentTypeField4 = elePay.getAttribute("PaymentType");
		} else if ("0003".equals(eleOrder.getAttribute("DocumentType"))) {
			sPaymentTypeField4 = getPaymentTypeFromSalesOrder(env, inDoc);
		}
		String sField5 = "Regular";
		//CR- Vertex Changes; Apply Sorting on OrderLine/@PrimeLineNo
		YFCDocument yfcDoc = YFCDocument.getDocumentFor(inDoc);
		YFCElement yfcOrderLines = yfcDoc.getDocumentElement().getChildElement(AcademyConstants.ELE_ORDER_LINES);
		yfcOrderLines.sortNumericChildren(new String[]{AcademyConstants.ATTR_PRIME_LINE_NO});
		
		NodeList nlOrdlines = eleOrdRelease.getElementsByTagName("OrderLine");
		int iNoOfOrdlines = nlOrdlines.getLength();
		int lineItem = 0;
		DecimalFormat df = new DecimalFormat("#0.00");
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
			
			//DSVCHANGE Replace OrderLineKey with ChainedFromOrderLineKey
			
            if(eleOrder.getAttribute("DocumentType").equals("0005")){
                sOrdLineKey = eleOrdLine.getAttribute("ChainedFromOrderLineKey");
                log.verbose("OrderLineKey set as ChainedFromOrderLineKey which is ::: " + sOrdLineKey);
            }
            //DSVCHANGE
			String sDateField1 = eleOrdLine.getAttribute("ReqShipDate");
			Element eleLinePrice = (Element) eleOrdLine.getElementsByTagName(
					"LinePriceInfo").item(0);
			String sLineTotal = eleLinePrice.getAttribute("LineTotal");
			String unitPrice = eleLinePrice.getAttribute("UnitPrice");

			String numTaxField2 = eleLinePrice.getAttribute("Tax");
			double relqty = Double.parseDouble(((Element) eleOrdLine
					.getElementsByTagName("OrderLineTranQuantity").item(0))
					.getAttribute("StatusQuantity"));
			// Get the Unallocated Qty to do proration
			// Start fix for # 4131 as part of R026H
			if("0001".equals(eleOrder.getAttribute("DocumentType"))){
				relqty = Double.parseDouble(((Element) eleOrdLine
						.getElementsByTagName("OrderLineTranQuantity").item(0))
						.getAttribute("OpenQty"));
			}
			log.verbose("********* \t RELEASED QTY IS : "+relqty+" ******************** ");
			// End # 4131
			
			double lineQty = Double.parseDouble(eleOrdLine
					.getAttribute("OrderedQty"));

			double extendedPrice = Double.parseDouble(unitPrice) * lineQty;

			log.verbose("unitprice : " + unitPrice);
			log.verbose("line qty : " + lineQty);
			log.verbose("extended price : " + extendedPrice);

			eleOrdLine.setAttribute("ExtendedPrice", df.format(extendedPrice));
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
			/*NodeList nlShipCharge = XPathUtil
					.getNodeList(
							eleOrdLine,
							"./LineCharges/LineCharge[@ChargeCategory=\""
									+ "Shipping\" or (@ChargeCategory='Promotions' and "
									+ "@ChargeName='ShippingPromotion')]");*/
			NodeList nlShipCharge = XPathUtil
			.getNodeListWS(
					eleOrdLine,
					"./LineCharges/LineCharge[@ChargeCategory=\""
							+ "Shipping\" or (@ChargeCategory='Promotions' and "
							+ "@ChargeName='ShippingPromotion')]",XPathConstants.NODESET);
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
						.setTextContent(df.format(proRatedShipingAmt));

				String sField3 = sOrdLineKey.concat("-").concat("SHIPPING");
				Element eleFlexiField = this.setFlexibleFields(sField3, df.format(proratedShippingTax), sPaymentTypeField4,
						df.format(proRatedShipingCharges), sDateField1,
						sField5, strTransType, strOrderNo);
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
			String numField1 = eleOrdLine.getAttribute("UnitPrice");
			eleExtnPrice
					.setTextContent(df.format(proRatedMerchandiseAmt));
			Element eleFlexiField = this.setFlexibleFields(sOrdLineKey, df.format(proratedMerchandiseTax), sPaymentTypeField4,
					df.format(proratedExtendedPrice), sDateField1,
					sField5,strTransType,strOrderNo);
			eleLineItem.appendChild(outDoc.importNode(eleFlexiField, true));
			lineItem = lineItem + 2;
		}
		log.verbose("Inside convertOrdRelToVertexRequest with output "
				+ XMLUtil.getXMLString(outDoc));
		//Start : OMNI-24737 : Vertex Call Failure Scenario
		env.setTxnObject("VertexRequestWithoutSoapEnvelope", outDoc);
		//End : OMNI-24737
		//outDoc = wrapSoapEnvelope(outDoc);
		//Start : OMNI-26977 : Vertex-Lite Enable Flag
		if (!YFCObject.isVoid(YFSSystem.getProperty(AcademyConstants.IS_VERTEX_LITE_ENABLED)) && 
				AcademyConstants.STR_YES.equalsIgnoreCase(YFSSystem.getProperty(AcademyConstants.IS_VERTEX_LITE_ENABLED))) {
			outDoc = wrapSoapEnvelopeForVertex9(outDoc);
		} else {
			outDoc = wrapSoapEnvelopeForVertex7(outDoc);
		}
		//End : OMNI-26977
		log
				.endTimer(" End of AcademyChangeToVertexCallRequest->convertOrdRelToVertexRequest() Api");
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
				.beginTimer(" Begining of AcademyChangeToVertexCallRequest->convertShipmentToVertexRequest() Api");
		log.verbose("Inside convertShipmentToVertexRequest with input "
				+ XMLUtil.getXMLString(inDoc));
		Document outDoc = null;
		Document docWithVendorZipCode = null;
		Element eleQuoteReq = null;
		Element eleShipment = inDoc.getDocumentElement();
		env.setTxnObject("ShipmentForInvoice",inDoc);
		
		if (inDoc.getDocumentElement().getAttribute("CallType").equals(
				"QuoteCall")) {
			outDoc = XMLUtil.createDocument("QuotationRequest");
			eleQuoteReq = outDoc.getDocumentElement();

			eleQuoteReq.setAttribute("documentDate", eleShipment
					.getAttribute("ExpectedShipmentDate"));

		} else if (inDoc.getDocumentElement().getAttribute("CallType").equals(
				"InvoiceCall")) {
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
			/*Element eleExtn = (Element) XPathUtil
					.getNode(eleOrderInvoiceList,
							"/OrderInvoiceList/OrderInvoice[@InvoiceType='PRO_FORMA']/Extn");*/
			Element eleExtn = (Element) XPathUtil
			.getNodeWS(eleOrderInvoiceList,
					"/OrderInvoiceList/OrderInvoice[@InvoiceType='PRO_FORMA']/Extn",XPathConstants.NODE);
			log.verbose("********** Extn element ********** "
					+ XMLUtil.getElementXMLString(eleExtn));
			if (!YFCObject.isVoid(eleExtn)) {
				documentNumber = eleExtn.getAttribute("ExtnInvoiceNo");
			}
		}
		// CR - Vertex Request changes; Set SalesOrder No as FlexibleField 7
		String strOrderNo = ((Element)inDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE).item(0)).getAttribute(AcademyConstants.ATTR_ORDER_NO);
		// Start - Fix for # 4131 as part of R026H
		if(documentNumber.trim().length() == 0 && inDoc.getDocumentElement().getAttribute("CallType").equals("QuoteCall")){
			documentNumber = strOrderNo;	
		}
		// End #4131

		log.verbose("********** document number ********** " + documentNumber);
		eleQuoteReq.setAttribute("documentNumber", documentNumber);
		// CR - Vertex Request changes; Set Sterling Function/Transaction name as FlexibleField 6
		String strTransType= eleShipment.getAttribute("TranType");

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
		
		//CR- Vertex Changes; Apply Sorting on OrderLine/@PrimeLineNo
		YFCDocument yfcDoc = YFCDocument.getDocumentFor(inDoc);
		YFCElement yfcOrderLines = yfcDoc.getDocumentElement().getChildElement(AcademyConstants.ELE_SHIPMENT_LINES);
		yfcOrderLines.sortNumericChildren(new String[]{AcademyConstants.ATTR_PRIME_LINE_NO});
		
		NodeList nlShipLines = eleShipment.getElementsByTagName("ShipmentLine");
		int iNoOfOrdlines = nlShipLines.getLength();
		int lineItem = 0;
		for (int i = 0; i < iNoOfOrdlines; i++) {
			Element eleShipLine = (Element) nlShipLines.item(i);
			String sOrdLineKey = eleShipLine.getAttribute("OrderLineKey");
			double sShipQty = Double.parseDouble(eleShipLine
					.getAttribute("Quantity"));
			Element eleOrdLine = (Element) eleShipLine.getElementsByTagName(
					"OrderLine").item(0);
			
			//DSVCHANGE
            String documentType = eleShipment.getAttribute("DocumentType"); 
            if(null != documentType && "0005".equals(documentType)){
                
                String chainedFromOrderLineKey = eleOrdLine.getAttribute("ChainedFromOrderLineKey");
                log.verbose("ChainedFromOrderLineKey is ::::: " + chainedFromOrderLineKey);
                sOrdLineKey = chainedFromOrderLineKey;
                
            }
            //DSVCHANGE
			
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
			String sLineTotal = eleLinePrice.getAttribute("LineTotal");
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

			DecimalFormat decimalCorrection = new DecimalFormat("#0.00");
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

			Element eleLineItem = outDoc.createElement("LineItem");
			eleQuoteReq.appendChild(eleLineItem);
			eleLineItem.setAttribute("lineItemNumber", Integer
					.toString(lineItem + 1));
			
			//DSVCHANGE
                
                Element FromAddress = this.setSellerAddress(eleFromAddress);
                eleLineItem.appendChild(outDoc.importNode(FromAddress, true));
            //DSVCHANGE

			
			Element ToAddress = this.setCustomerAddress(eleToAddress);
			
			eleLineItem.appendChild(outDoc.importNode(ToAddress, true));
			// product

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
					.setTextContent(decimalCorrection.format(proRatedMerchandiseAmt));
			eleLineItem.appendChild(eleExtnPrice);
			// String numField1 = eleLinePrice.getAttribute("UnitPrice");
			Element eleFlexiFields = this.setFlexibleFields(sOrdLineKey, 
					decimalCorrection.format(proratedMerchandiseTax), sField4,
					decimalCorrection.format(proratedExtendedPrice), sDateField1, sField5, strTransType,strOrderNo);
			eleLineItem.appendChild(outDoc.importNode(eleFlexiFields, true));
			/*
			 * Get all charges/discounts associated with Shipping to calculate
			 * final shipping charge.
			 */
			/*NodeList nlShipCharge = XPathUtil
					.getNodeList(
							eleOrdLine,
							"./LineCharges/LineCharge[@ChargeCategory=\""
									+ "Shipping\" or (@ChargeCategory='Promotions' and "
									+ "@ChargeName='ShippingPromotion')]");*/
			NodeList nlShipCharge = XPathUtil
			.getNodeListWS(
					eleOrdLine,
					"./LineCharges/LineCharge[@ChargeCategory=\""
							+ "Shipping\" or (@ChargeCategory='Promotions' and "
							+ "@ChargeName='ShippingPromotion')]",XPathConstants.NODESET);

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
				Element fromAddress = this.setSellerAddress(eleFromAddress);
				Element toAddress = this.setCustomerAddress(eleToAddress);
				eleShipLineItem.appendChild(outDoc
						.importNode(fromAddress, true));
				eleShipLineItem.appendChild(outDoc.importNode(toAddress, true));

				Element eleShippingProduct = outDoc.createElement("Product");
				eleShipLineItem.appendChild(eleShippingProduct);

				/*
				 * Shipping Short SKU and Tax Code is set here. Tax Code is
				 * referred as productClass. Short SKU depends on SCACAndService
				 * value. This would be fetched from Common code.
				 */
				String[] shortSkqAndTaxCode = getShipmentShortSkuAndTaxCode(
						env, eleShipment.getAttribute("ScacAndService"));
				eleShippingProduct.setTextContent(shortSkqAndTaxCode[0]); // Short SKU
				eleShippingProduct.setAttribute("productClass", shortSkqAndTaxCode[1]); // 127
				// or
				// 157

				Element eleShippingQty = outDoc.createElement("Quantity");
				eleShipLineItem.appendChild(eleShippingQty);
				// eleQty.setTextContent(Double.toString(sShipQty));
				// For Shipping SKU's quanity should always be set to 1.0
				eleShippingQty.setTextContent("1.0");
				eleShippingQty.setAttribute("unitOfMeasure", "EA");
				Element eleShippingExtnPrice = outDoc.createElement("ExtendedPrice");
				eleShipLineItem.appendChild(eleShippingExtnPrice);

				// double proRatedShipAmt = sShipAmt * (sShipQty / sLineQty);
				eleShippingExtnPrice
						.setTextContent(decimalCorrection.format(proRatedShipingAmt));
				String sField3 = sOrdLineKey.concat("-").concat("SHIPPING");

				Element eleShippingFlexiFields = this
						.setFlexibleFields(sField3, decimalCorrection.format(proratedShippingTax), sField4, 
								decimalCorrection.format(proRatedShipingCharges), sDateField1,
								sField5,strTransType,strOrderNo);
				eleShipLineItem.appendChild(outDoc.importNode(eleShippingFlexiFields,
						true));

			}			
			lineItem = lineItem + 2;
		}
		log.verbose("Inside convertShipmentToVertexRequest with output "
				+ XMLUtil.getXMLString(outDoc));
		//Start : OMNI-24737 : Vertex Call Failure Scenario
		env.setTxnObject("VertexRequestWithoutSoapEnvelope", outDoc);
		//End : OMNI-24737
		//outDoc = wrapSoapEnvelope(outDoc);
		//Start : OMNI-26977 : Vertex-Lite Enable Flag
		if (!YFCObject.isVoid(YFSSystem.getProperty(AcademyConstants.IS_VERTEX_LITE_ENABLED)) && 
				AcademyConstants.STR_YES.equalsIgnoreCase(YFSSystem.getProperty(AcademyConstants.IS_VERTEX_LITE_ENABLED))) {
			outDoc = wrapSoapEnvelopeForVertex9(outDoc);
		} else {
			outDoc = wrapSoapEnvelopeForVertex7(outDoc);
		}
		//End : OMNI-26977
		log
				.endTimer(" End of AcademyChangeToVertexCallRequest->convertShipmentToVertexRequest() Api");
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
				env.clearApiTemplate("getOrderInvoiceList");
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
					.beginTimer(" Begining of AcademyChangeToVertexCallRequest->getFromAddress() Api");
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
	 * @throws Exception TODO
	 */
	private String getItemProductCode(YFSEnvironment env, String sItemId, String sUOM, String sOrgCode) throws Exception 
	{
		String productCode = "";
		log.beginTimer(" Begining of AcademyEnhancedChangeToVertexCall->getItemProductCode() Api");
		Document getItemList = XMLUtil.createDocument("Item");
		getItemList.getDocumentElement().setAttribute("ItemID", sItemId);
		Document outPutTemplate = YFCDocument.getDocumentFor("<ItemList> <Item ItemKey=''><ClassificationCodes/></Item> </ItemList>").getDocument();
		env.setApiTemplate(AcademyConstants.API_GET_ITEM_LIST, outPutTemplate);
		log.verbose("Inside getItemProductCode invoking getItemList with input " + XMLUtil.getXMLString(getItemList));
		Document itemListDocument = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ITEM_LIST, getItemList);
		env.clearApiTemplate(AcademyConstants.API_GET_ITEM_LIST);
		log.verbose("getItemList output " + XMLUtil.getXMLString(itemListDocument));

		if(!YFCObject.isVoid(itemListDocument) && itemListDocument.getDocumentElement().getElementsByTagName(AcademyConstants.ITEM).getLength()>0)
		{
			Element classificationCodes = (Element) itemListDocument.getDocumentElement().getElementsByTagName("ClassificationCodes").item(0);
			productCode = classificationCodes.getAttribute("TaxProductCode");
		}else{
			log.verbose("Item "+sItemId+" is not found. It may held status or not existed. Therefore, throw an exception");
			YFCException itemExce = new YFCException();
			itemExce.setAttribute(YFCException.ERROR_CODE,"EXTN_ACADEMY_12");
			itemExce.setAttribute(YFCException.ERROR_DESCRIPTION, "Invalid Item "+sItemId+". It may be in held status or not existed.");
			itemExce.setErrorDescription("Invalid Item "+sItemId+". It may be in held status or not existed.");
			throw itemExce;
		}

		log.verbose("productCode " + productCode);
		log.endTimer(" End ofAcademyEnhancedChangeToVertexCall->getItemProductCode() Api");
		 
		return productCode;
	}

	private Element setSellerAddress(Element eleFromAddress) {
		Element eleSel = null;
		try {
			log
					.beginTimer(" Begining of AcademyChangeToVertexCallRequest->setSellerAddress() Api");
			Document outDoc = XMLUtil.createDocument("Seller");
			eleSel = outDoc.getDocumentElement();
			Element eleOrigin = outDoc.createElement("PhysicalOrigin");
			eleSel.appendChild(eleOrigin);
			Element eleCountry = outDoc.createElement("Country");
			eleOrigin.appendChild(eleCountry);
			eleCountry.setTextContent(YFSSystem
					.getProperty("oms.vertex.quotation.request.country"));
			//CR - Vertex Changes; don't send City and State if zip code is available
			String zipCode = eleFromAddress.getAttribute("ZipCode");
			log.verbose("Just before setting ZipCode as PostalCode under SellerAddress ::: " + zipCode);
			if(YFSObject.isNull(zipCode) || YFSObject.isVoid(zipCode)){
				Element eleCity = outDoc.createElement("City");
				eleOrigin.appendChild(eleCity);
				eleCity.setTextContent(eleFromAddress.getAttribute("City"));
				Element eleState = outDoc.createElement("MainDivision");
				eleOrigin.appendChild(eleState);
				eleState.setTextContent(eleFromAddress.getAttribute("State"));
			}
			Element eleZipCode = outDoc.createElement("PostalCode");
			eleOrigin.appendChild(eleZipCode);
			eleZipCode.setTextContent(eleFromAddress.getAttribute("ZipCode"));
			log
					.endTimer(" End of AcademyChangeToVertexCallRequest->setSellerAddress() Api");
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
					.beginTimer(" Begining of AcademyChangeToVertexCallRequest->setCustomerAddress() Api");
			Document outDoc = XMLUtil.createDocument("Customer");
			eleCustomer = outDoc.getDocumentElement();
			Element eleDestination = outDoc.createElement("Destination");
			eleCustomer.appendChild(eleDestination);
			Element eleCountry = outDoc.createElement("Country");
			eleDestination.appendChild(eleCountry);
			eleCountry.setTextContent(YFSSystem
					.getProperty("oms.vertex.quotation.request.country"));
			//CR - Vertex Changes; don't send City and State if zip code is available
			String zipCode = eleToAddress.getAttribute("ZipCode");
			if(YFSObject.isNull(zipCode) || YFSObject.isVoid(zipCode)){
				Element eleCity = outDoc.createElement("City");
				eleDestination.appendChild(eleCity);
				eleCity.setTextContent(eleToAddress.getAttribute("City"));
				Element eleState = outDoc.createElement("MainDivision");
				eleDestination.appendChild(eleState);
				eleState.setTextContent(eleToAddress.getAttribute("State"));
			}
			Element eleZipCode = outDoc.createElement("PostalCode");
			eleDestination.appendChild(eleZipCode);
			eleZipCode.setTextContent(eleToAddress.getAttribute("ZipCode"));
			log
					.endTimer(" End of AcademyChangeToVertexCallRequest->setCustomerAddress() Api");

		} catch (Exception ex) {
			YFSException yfsException = new YFSException();
			yfsException.setStackTrace(ex.getStackTrace());
			throw yfsException;
		}
		return eleCustomer;
	}

	private Element setGeneralAttribute(YFSEnvironment env, Element eleOrdLine,
			Element eleToAddress, String sShipNode, String docType) {
	    
	    
	    Document docWithVendorZipCode = null;
		Document outDoc = null;
		Element eleLineItem = null;
		String sZipCodeFromVendor = null;
		try {
			log
					.beginTimer(" Begining of AcademyChangeToVertexCallRequest->setGeneralAttribute() Api and docType is="
							+ docType);
			outDoc = XMLUtil.createDocument("LineItem");
			eleLineItem = outDoc.getDocumentElement();
			if (sShipNode != null && !sShipNode.equals("")) {
			    log.verbose("Inside sShipNode check:::");
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
					  //DSVCHANGE to stamp actual ZipCode from Vendornet on ToAddressEle 
					    log.verbose("Before VendorNetShipConfirmationMessage not null check");
                        if("0005".equals(docType) && env.getTxnObject("VendorNetShipConfirmationMessage") != null){
                               log.verbose("Inside If of VendorNetShipConfirmationMessage not null check");
                               //Purchase order release/shipment
                               //Check whether DSV shipment/release confirm?
                               //ZipCode sent by the Vendor set
                               docWithVendorZipCode = (Document) env.getTxnObject("VendorNetShipConfirmationMessage");
                               //sZipCodeFromVendor = XPathUtil.getString(docWithVendorZipCode, "Shipment/FromAddress/@ZipCode");
                               sZipCodeFromVendor = XPathUtil.getStringWS(docWithVendorZipCode, "Shipment/FromAddress/@ZipCode",XPathConstants.STRING);
                               if (sZipCodeFromVendor != null || !sZipCodeFromVendor.equals("")) {
                                   log.verbose("sZipCodeFromVendor for ProformaInvoice= " + sZipCodeFromVendor);
                                   
                                   eleFromAddress.setAttribute("ZipCode", sZipCodeFromVendor);
                                   log.verbose("After stamping ToAddress with ZipCode the ToAddressEle is: \n" + XMLUtil.getElementXMLString(eleFromAddress) );
                               }
                               
                          

                           }
                      //DSVCHANGE
                          
                            log.verbose("outside if of VendorNetShipConfirmationMessage not null check..!!");
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
					.endTimer(" End of AcademyChangeToVertexCallRequest->setGeneralAttribute() Api");
		} catch (Exception ex) {
			YFSException yfsException = new YFSException();
			yfsException.setStackTrace(ex.getStackTrace());
			throw yfsException;
		}
		return eleLineItem;

	}

	// Modified the method signature as part of CR - Vertex changes; pass Sterling Function name and Original SalesOrderNo as Flexible field 6 and 7
	
	private Element setFlexibleFields(String sField3, String numField2,
			String Field4, String numField1, String dateField1, String Field5, String field6, String field7) {
		Element eleFlexFields = null;
		try {
			log
					.beginTimer(" Begining of AcademyChangeToVertexCallRequest->setFlexibleFields() Api");
			log.verbose("Field 1 : " + numField1);
			log.verbose("Field 2 : " + numField2);
			log.verbose("Field 3 : " + sField3);
			log.verbose("Field 4 : " + Field4);
			log.verbose("Field 5 : " + Field5);
			log.verbose("Field 6 : " + field6);
			log.verbose("Field 7 : " + field7);
			log.verbose("dateField 1 : " + dateField1);

			Document outDoc = XMLUtil.createDocument("FlexibleFields");

			eleFlexFields = outDoc.getDocumentElement();
			// CR - Vertex Changes; Flexible field 7 Original Sales Order No
			Element eleFlexCodeField7 = outDoc.createElement("FlexibleCodeField");
			eleFlexCodeField7.setTextContent(field7);
			eleFlexCodeField7.setAttribute("fieldId", "7");
			eleFlexFields.appendChild(eleFlexCodeField7);
			// CR - Vertex Changes; Flexible field 6 Sterling Function/Transaction name
			Element eleFlexCodeField6 = outDoc.createElement("FlexibleCodeField");
			eleFlexCodeField6.setTextContent(field6);
			eleFlexCodeField6.setAttribute("fieldId", "6");
			eleFlexFields.appendChild(eleFlexCodeField6);			

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
					.endTimer(" End of AcademyChangeToVertexCallRequest->setFlexibleFields() Api");
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
				.beginTimer(" Begin of AcademyChangeToVertexCallRequest->getShipmentShortSkuAndTaxCode() Api");
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
					/*shortSquAndTaxCode[0] = XPathUtil.getString(
							docCommonCodeListOutput,
							"CommonCodeList/CommonCode/@CodeShortDescription");
					shortSquAndTaxCode[1] = XPathUtil.getString(
							docCommonCodeListOutput,
							"CommonCodeList/CommonCode/@CodeLongDescription");*/
					shortSquAndTaxCode[0] = XPathUtil.getStringWS(
							docCommonCodeListOutput.getDocumentElement(),
							"CommonCodeList/CommonCode/@CodeShortDescription",XPathConstants.STRING);
					shortSquAndTaxCode[1] = XPathUtil.getStringWS(
							docCommonCodeListOutput.getDocumentElement(),
							"CommonCodeList/CommonCode/@CodeLongDescription",XPathConstants.STRING);
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
				.endTimer(" End of AcademyChangeToVertexCallRequest->getShipmentShortSkuAndTaxCode() Api");
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
		try {
			log
					.beginTimer(" Begin of AcademyChangeToVertexCallRequest->convertCreditMemoToVertexRequest() Api");
			if (inDoc.getDocumentElement().getAttribute("CallType").equals(
					"QuoteCall")) {
				outDoc = XMLUtil.createDocument("QuotationRequest");

			} else if (inDoc.getDocumentElement().getAttribute("CallType")
					.equals("InvoiceCall")) {
				outDoc = XMLUtil.createDocument("InvoiceRequest");
			} else if (inDoc.getDocumentElement().getAttribute("CallType")
					.equals("DistributeCall")) {
				outDoc = XMLUtil.createDocument("DistributionTaxRequest");
			}

			Element eleQuoteReq = outDoc.getDocumentElement();
			Element eleOrdRelease = inDoc.getDocumentElement();
			String strTransType = eleOrdRelease.getAttribute("TranType");
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
			/*NodeList nlOrdlines = XPathUtil.getNodeList(eleOrdRelease,
					"/Order/OrderLines/OrderLine");*/
			NodeList nlOrdlines = XPathUtil.getNodeListWS(eleOrdRelease,
					"/Order/OrderLines/OrderLine",XPathConstants.NODESET);
			/* End of Fix */
			DecimalFormat df = new DecimalFormat("#0.00");

			int iNoOfOrdlines = nlOrdlines.getLength();

			int lineItem = 0;
			for (int i = 0; i < iNoOfOrdlines; i++) {
				Element eleOrdLine = (Element) nlOrdlines.item(i);
				// Start Fix for # 4041 - as Part of release R026G
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
				/*NodeList nlShipCharge = XPathUtil.getNodeList(eleOrdLine,
						"./LineCharges/LineCharge[@ChargeCategory=\""
								+ "Shipping\"]");*/
				NodeList nlShipCharge = XPathUtil.getNodeListWS(eleOrdLine,
						"./LineCharges/LineCharge[@ChargeCategory=\""
								+ "Shipping\"]",XPathConstants.NODESET);
				int inoOfShipCharge = nlShipCharge.getLength();
				double sShipAmt = 0.0;
				for (int k = 0; k < inoOfShipCharge; k++) {
					Element eleShipCharge = (Element) nlShipCharge.item(k);
					String sChargeAmt = eleShipCharge
							.getAttribute("ChargeAmount");
					sShipAmt = sShipAmt + Double.parseDouble(sChargeAmt);
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
					eleExtnPrice.setTextContent(df.format(sShipAmt));

					String sField3 = sOrdLineKey.concat("-").concat("SHIPPING");
					Element eleFlexiField = this.setFlexibleFields(sField3,
							numField2, sField4, df.format(sShipAmt),
							sDateField1, sField5,strTransType, eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_NO));
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
				eleExtnPrice.setTextContent(df.format(extnPrice));
				Element eleFlexiField = this.setFlexibleFields(sOrdLineKey,
						numField2, sField4, numField1, sDateField1, sField5,strTransType,eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_NO));
				eleLineItem.appendChild(outDoc.importNode(eleFlexiField, true));
				lineItem = lineItem + 1;
			}
			log
					.endTimer(" End of AcademyChangeToVertexCallRequest->convertCreditMemoToVertexRequest() Api");
		} catch (YFSException ex) {
			throw ex;
		}
		//outDoc = wrapSoapEnvelope(outDoc);
		//Start : OMNI-26977 : Vertex-Lite Enable Flag
		if (!YFCObject.isVoid(YFSSystem.getProperty(AcademyConstants.IS_VERTEX_LITE_ENABLED)) && 
				AcademyConstants.STR_YES.equalsIgnoreCase(YFSSystem.getProperty(AcademyConstants.IS_VERTEX_LITE_ENABLED))) {
			outDoc = wrapSoapEnvelopeForVertex9(outDoc);
		} else {
			outDoc = wrapSoapEnvelopeForVertex7(outDoc);
		}
		//End : OMNI-26977
		return outDoc;
	}

	//OMNI-26977 : Renamed wrapSoapEnvelope() method to wrapSoapEnvelopeForVertex9() method
	private static Document wrapSoapEnvelopeForVertex9(Document inDoc) {
		log
				.beginTimer(" Begin of AcademyChangeToVertexCallRequest->wrapSoapEnvelopeForVertex9() Api");
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
		vertexEnv.setAttribute("xmlns", "urn:vertexinc:o-series:tps:9:0");
		vertexEnv.setAttribute("xmlns:xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
		vertexEnv.setAttribute("xsi:schemaLocation",
				"urn:vertexinc:o-series:tps:9:0");
		vertexEnv.setAttribute("xsi:schemaLocation",
				"urn:vertexinc:o-series:tps:9:0 VertexInc_Envelope.xsd");
		YFCElement login = vertexEnv.createChild("Login");
		YFCElement trustedId = login.createChild("TrustedId");
		trustedId.setNodeValue(YFSSystem
				.getProperty("oms.vertex.quotation.request.trustedid"));
		XMLUtil.importElement((Element) vertexEnv.getDOMNode(), inDoc
				.getDocumentElement());
		log
				.endTimer(" End of AcademyChangeToVertexCallRequest->wrapSoapEnvelopeForVertex9() Api");
		return inputDocument.getDocument();
	}

	//Start : OMNI-26977 : Vertex-Lite Enable Flag
	private static Document wrapSoapEnvelopeForVertex7(Document inDoc) {
		log
				.beginTimer(" Begin of AcademyChangeToVertexCallRequest->wrapSoapEnvelopeForVertex7() Api");
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
				.getProperty("oms.vertex_7.quotation.request.trustedid"));
		XMLUtil.importElement((Element) vertexEnv.getDOMNode(), inDoc
				.getDocumentElement());
		log
				.endTimer(" End of AcademyChangeToVertexCallRequest->wrapSoapEnvelopeForVertex7() Api");
		return inputDocument.getDocument();
	}
	//End : OMNI-26977

	//Start : OMNI-24737 : Vertex Call Failure Scenario
	public static Document wrapSoapEnvelopeForVertexOnDemand(Document inDoc) {
		log
				.beginTimer(" Begin of AcademyChangeToVertexCallRequest->wrapSoapEnvelopeForVertexOnDemand() Api");
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
		vertexEnv.setAttribute("xmlns", "urn:vertexinc:o-series:tps:9:0");
		vertexEnv.setAttribute("xmlns:xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
		vertexEnv.setAttribute("xsi:schemaLocation",
				"urn:vertexinc:o-series:tps:9:0");
		vertexEnv.setAttribute("xsi:schemaLocation",
				"urn:vertexinc:o-series:tps:9:0 VertexInc_Envelope.xsd");
		YFCElement login = vertexEnv.createChild("Login");
		YFCElement trustedId = login.createChild("TrustedId");
		trustedId.setNodeValue(YFSSystem
				.getProperty("oms.vertex.ondemand.quotation.request.trustedid"));
		XMLUtil.importElement((Element) vertexEnv.getDOMNode(), inDoc
				.getDocumentElement());
		log
				.endTimer(" End of AcademyChangeToVertexCallRequest->wrapSoapEnvelopeForVertexOnDemand() Api");
		return inputDocument.getDocument();
	}
	//End : OMNI-24737


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
			
			//String soHeaderKey = XPathUtil.getString(returnOrderDoc, "Order/OrderLines/OrderLine/@DerivedFromOrderHeaderKey");
			String soHeaderKey = XPathUtil.getStringWS(returnOrderDoc.getDocumentElement(), "Order/OrderLines/OrderLine/@DerivedFromOrderHeaderKey",XPathConstants.STRING);
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
				/*paymentType = XPathUtil.getString(salesOrderListOutDoc, 
						"OrderList/Order/PaymentMethods/PaymentMethod[@SuspendAnyMoreCharges='N' and @PaymentType='CREDIT_CARD']/@PaymentType");*/
				paymentType = XPathUtil.getStringWS(salesOrderListOutDoc.getDocumentElement(), 
						"OrderList/Order/PaymentMethods/PaymentMethod[@SuspendAnyMoreCharges='N' and (@PaymentType='CREDIT_CARD' or @PaymentType='Credit_Card')]/@PaymentType",XPathConstants.STRING);

				if (YFCObject.isVoid(paymentType)) 
				{
					/*paymentType = XPathUtil.getString(salesOrderListOutDoc, 
							"OrderList/Order/PaymentMethods/PaymentMethod[@SuspendAnyMoreCharges='N' and @PaymentType='GIFT_CARD']/@PaymentType");*/
					paymentType = XPathUtil.getStringWS(salesOrderListOutDoc.getDocumentElement(), 
							"OrderList/Order/PaymentMethods/PaymentMethod[@SuspendAnyMoreCharges='N' and @PaymentType='GIFT_CARD']/@PaymentType",XPathConstants.STRING);
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
				//String soHeaderKey = XPathUtil.getString(getROListOutDoc, "OrderList/Order/OrderLines/OrderLine/@DerivedFromOrderHeaderKey");
				String soHeaderKey = XPathUtil.getStringWS(getROListOutDoc.getDocumentElement(), "OrderList/Order/OrderLines/OrderLine/@DerivedFromOrderHeaderKey",XPathConstants.STRING);
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
					/*paymentType = XPathUtil.getString(getSOListOutDoc, 
							"OrderList/Order/PaymentMethods/PaymentMethod[@SuspendAnyMoreCharges='N' and @PaymentType='CREDIT_CARD']/@PaymentType");*/
					paymentType = XPathUtil.getStringWS(getSOListOutDoc.getDocumentElement(), 
							"OrderList/Order/PaymentMethods/PaymentMethod[@SuspendAnyMoreCharges='N' and (@PaymentType='CREDIT_CARD' or @PaymentType='Credit_Card')]/@PaymentType",XPathConstants.STRING);
					
					if (YFCObject.isVoid(paymentType)) 
					{
						/*paymentType = XPathUtil.getString(getSOListOutDoc, 
								"OrderList/Order/PaymentMethods/PaymentMethod[@SuspendAnyMoreCharges='N' and @PaymentType='GIFT_CARD']/@PaymentType");*/
						paymentType = XPathUtil.getStringWS(getSOListOutDoc.getDocumentElement(), 
								"OrderList/Order/PaymentMethods/PaymentMethod[@SuspendAnyMoreCharges='N' and @PaymentType='GIFT_CARD']/@PaymentType",XPathConstants.STRING);
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
					.beginTimer(" Begin of AcademyChangeToVertexCallRequest->getProformaTax() Api");
			// call getOrderInvoiceList to get all the proforma Invoices
			Document inDocProformaList = XMLUtil.createDocument("OrderInvoice");
			Element eleInvoice = inDocProformaList.getDocumentElement();
			eleInvoice.setAttribute("InvoiceType", "PRO_FORMA");
			eleInvoice.setAttribute("ShipmentKey", sShipmentKey);
			env.setApiTemplate("getOrderInvoiceList", "global/template/api/getOrderInvoiceList.LineChargesForShipment.xml");
			Document outDocProformaList = AcademyUtil.invokeAPI(env,
					"getOrderInvoiceList", inDocProformaList);
			env.clearApiTemplate("getOrderInvoiceList");
			// get the list of invoices for order..

			NodeList nlInvoiceList = outDocProformaList
					.getElementsByTagName("OrderInvoice");
			int iNoOfInvoice = nlInvoiceList.getLength();

			for (int i = 0; i < iNoOfInvoice; i++) {
				Element eleOrderInvoice = (Element) nlInvoiceList.item(i);
				String sInvoice = eleOrderInvoice.getAttribute("InvoiceNo");
				// Comment the getOrderInvoiceDetails API as part of code clean up
				// call getOrderInvoiceDetails
				/*Document inDocProformaDetail = XMLUtil
						.createDocument("GetOrderInvoiceDetails");
				Element eleInvoiceDet = inDocProformaDetail
						.getDocumentElement();
				eleInvoiceDet.setAttribute("InvoiceType", "PRO_FORMA");
				eleInvoiceDet.setAttribute("InvoiceNo", sInvoice);

				Document outDocProformaDetail = AcademyUtil.invokeAPI(env,
						"getOrderInvoiceDetails", inDocProformaDetail);*/
				NodeList nlLineList = eleOrderInvoice.getElementsByTagName("LineDetail");
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
					.endTimer(" End of AcademyChangeToVertexCallRequest->getProformaTax() Api");
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
		try {
			log
					.verbose("Begining of AcademyChangeToVertexCallRequest->convertReturnOrderToVertexRequest() Api and inDoc is "
							+ XMLUtil.getXMLString(inDoc));
			log
					.beginTimer(" Begining of AcademyChangeToVertexCallRequest->convertReturnOrderToVertexRequest() Api");
			Element eleOrder = inDoc.getDocumentElement();
			// CR - Vertex Request changes; Set Sterling Function/Transaction name as FlexibleField 6
			String strTransType = eleOrder.getAttribute("TranType");
			// CR - Vertex Request changes; Set Original SalesOrder No as FlexibleField 7
			String derivedOrderNo = eleOrder.getAttribute("DerivedOrderNo");
			Element eleQuoteReq = null;
			SimpleDateFormat sDate = new SimpleDateFormat();
			Calendar calendar = Calendar.getInstance();
			sDate.applyPattern("yyyy-MM-dd");
			DecimalFormat df = new DecimalFormat("#0.00");
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
			String sField4 = "";
			// Prevent to call detail API unnecessarily
			sField4 = eleOrder.getAttribute("DerivedPaymentTender");
			if(YFSObject.isNull(sField4) || YFSObject.isVoid(sField4)){
				sField4 = getPaymentTypeForSalesOrderFromReturnOrderInputXML(env,inDoc);
			}
			log.verbose("sField4---" + sField4);
			//CR- Vertex Changes; Apply Sorting on OrderLine/@PrimeLineNo
			YFCDocument yfcDoc = YFCDocument.getDocumentFor(inDoc);
			YFCElement yfcOrderLines = yfcDoc.getDocumentElement().getChildElement(AcademyConstants.ELE_ORDER_LINES);
			yfcOrderLines.sortNumericChildren(new String[]{AcademyConstants.ATTR_PRIME_LINE_NO});
			
			NodeList nlOrdlines = eleOrder.getElementsByTagName("OrderLine");
			int iNoOfOrdlines = nlOrdlines.getLength();
			int lineItem = 0;
			for (int i = 0; i < iNoOfOrdlines; i++) {
				Element eleOrdLine = (Element) nlOrdlines.item(i);
				// Start Fix for # 4041 - as Part of release R026G
				// Prevent Canceled Order Line to calculate Tax
				if(eleOrdLine.getAttribute(AcademyConstants.ATTR_STATUS).equals("Cancelled"))
					continue;
				// End fix for # 4041
				String returnOrdLineKey = eleOrdLine
						.getAttribute("OrderLineKey");

				// String numField2 = eleLinePrice.getAttribute("Tax");
				String sField5 = "Return";
				String extendedPrice = "0.00";
				String sDateField1 = sDate.format(calendar.getTime());

				/*NodeList nlShipCharge = XPathUtil.getNodeList(eleOrdLine,
						"./LineCharges/LineCharge[@ChargeCategory=\""
								+ "ReturnShippingCharge\"]");*/
				NodeList nlShipCharge = XPathUtil.getNodeListWS(eleOrdLine,
						"./LineCharges/LineCharge[@ChargeCategory=\""
								+ "ReturnShippingCharge\"]",XPathConstants.NODESET);

				if (nlShipCharge.getLength() > 0) {
					extendedPrice = ((Element) nlShipCharge.item(0))
							.getAttribute("ChargePerLine");					
				}
				log.verbose("extendedPrice is : " +extendedPrice);
				if(Double.parseDouble(extendedPrice) == 0.00)
					continue;
				
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
				//Prorate the Shipping charges
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
				//CR - Vertex Changes; don't send City and State if zip code is available
				String zipCode = eleToAddress.getAttribute("ZipCode");
				if(YFSObject.isNull(zipCode) || YFSObject.isVoid(zipCode)){
					Element eleCity = outDoc.createElement("City");
					eleOrigin.appendChild(eleCity);
					eleCity.setTextContent(eleToAddress.getAttribute("City"));
					Element eleState = outDoc.createElement("MainDivision");
					eleOrigin.appendChild(eleState);
					eleState.setTextContent(eleToAddress.getAttribute("State"));
				}
				Element eleZipCode = outDoc.createElement("PostalCode");
				eleOrigin.appendChild(eleZipCode);
				eleZipCode.setTextContent(eleToAddress.getAttribute("ZipCode"));

				// eleSeller.appendChild(eleOrigin);

				eleReturnShippingChargeLineItem.appendChild((eleOrigin));

				eleReturnShippingChargeLineItem.appendChild(outDoc.importNode(
						eleCustomer, true));

				Element returnShipLineEleQty = outDoc.createElement("Quantity");
				/*returnShipLineEleQty.setTextContent(eleOrdLine
						.getAttribute("OrderedQty"));*/
				//Shipping Line should have qty always 1.0
				returnShipLineEleQty.setTextContent("1.0");
				returnShipLineEleQty.setAttribute("unitOfMeasure", "EA");
				eleReturnShippingChargeLineItem
						.appendChild(returnShipLineEleQty);

				Element returnLineEleFlexiField = this.setFlexibleFields(
						returnOrdLineKey, "0.0", sField4, extendedPrice,
						sDateField1, sField5, strTransType, derivedOrderNo);
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
					.endTimer(" End of AcademyChangeToVertexCallRequest->convertReturnOrderToVertexRequest() Api");
			log.verbose("final outDoc- Vertex Request XML is "
					+ XMLUtil.getXMLString(outDoc));
		} catch (YFSException ex) {
			throw ex;
		}
		//outDoc = wrapSoapEnvelope(outDoc);
		//Start : OMNI-26977 : Vertex-Lite Enable Flag
		if (!YFCObject.isVoid(YFSSystem.getProperty(AcademyConstants.IS_VERTEX_LITE_ENABLED)) && 
				AcademyConstants.STR_YES.equalsIgnoreCase(YFSSystem.getProperty(AcademyConstants.IS_VERTEX_LITE_ENABLED))) {
			outDoc = wrapSoapEnvelopeForVertex9(outDoc);
		} else {
			outDoc = wrapSoapEnvelopeForVertex7(outDoc);
		}
		//End : OMNI-26977
		return outDoc;
	}	
	
	public static void main(String[] args) throws Exception {
		Document doc = YFCDocument.getDocumentFor(new File("C://2.xml"))
				.getDocument();
		Element docElement = doc.getDocumentElement();
		log.verbose(XMLUtil.getElementXMLString(docElement));
		/*Element eleExtn = (Element) XPathUtil
				.getNode(docElement,
						"/OrderInvoiceList/OrderInvoice[@InvoiceType='PRO_FORMA']/Extn");*/
		Element eleExtn = (Element) XPathUtil
		.getNodeWS(docElement,
				"/OrderInvoiceList/OrderInvoice[@InvoiceType='PRO_FORMA']/Extn",XPathConstants.NODE);
		log.verbose(XMLUtil.getElementXMLString(eleExtn));
		String documentNumber = eleExtn.getAttribute("ExtnInvoiceNo");

	}
}
