package com.academy.ecommerce.sterling.userexits;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.ecommerce.sterling.util.AcademyPricingAndPromotionUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSExtnInputLineChargesShipment;
import com.yantra.yfs.japi.YFSExtnLineChargeStruct;
import com.yantra.yfs.japi.YFSExtnOutputLineChargesShipment;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSGetLineChargesForShipmentUE;

public class AcademyReturnOrderLineChargesForShipmentUEImpl implements
		YFSGetLineChargesForShipmentUE {

	/*
	 * This UE Implemented for overriding tax values & to handle Proration issue
	 * corrections as part of CR#18.
	 */
	Document outDoc = null;

	YFSExtnOutputLineChargesShipment outStruct = new YFSExtnOutputLineChargesShipment();

	// creating the instance of Logger
	YFCLogCategory m_Logger = YFCLogCategory
			.instance(AcademyReturnOrderLineChargesForShipmentUEImpl.class
					.getName());

	ArrayList<YFSExtnLineChargeStruct> newLineCharges = new ArrayList<YFSExtnLineChargeStruct>();

	YFSExtnLineChargeStruct chargesStrct = new YFSExtnLineChargeStruct();

	YFSExtnLineChargeStruct returnShippingChargesStrct = new YFSExtnLineChargeStruct();

	YFSExtnLineChargeStruct couponChargesStrct = new YFSExtnLineChargeStruct();

	YFSExtnLineChargeStruct promotionStrct = new YFSExtnLineChargeStruct();

	YFSExtnLineChargeStruct appeaseStrct = new YFSExtnLineChargeStruct();

	DecimalFormat twoDForm = new DecimalFormat("#.##");

	// CR - Vertex Changes for Original SalesOrder No and Sterling Function Name
	String derivedOrderNo = "";

	String derivedPaymentTender = "";

	boolean isReqWGQuotationCall = false;

	public YFSExtnOutputLineChargesShipment getLineChargesForShipment(
			YFSEnvironment env, YFSExtnInputLineChargesShipment inputLineCharge)
			throws YFSUserExitException {

		// TODO Auto-generated method stub
		// Get the Shipment Key
		// Get shipment details and then make the Quote call to Vertex
		YFSExtnOutputLineChargesShipment outStruct = new YFSExtnOutputLineChargesShipment();
		try {
			m_Logger
					.beginTimer(" Begining of AcademyReturnOrderLineChargesForShipmentUEImpl -> getLineChargesForShipmentApi");
			String totalShippingInvoicedCharge = "0", totalreturnShippingCharge = "0", totalappeasementChrge = "0", totalcouponDiscCharge = "0", totalpromotionChrge = "0", sOrderHdrKey = inputLineCharge.orderHeaderKey, shippingCharge = "0", returnShippingCharge = "0", couponDiscCharge = "0", appeasementChrge = "0", promotionChrge = "0";
			Document inXML = null;
			if (sOrderHdrKey != null && !sOrderHdrKey.equals("")) {
				/**
				 * Start - CR : New WG carrier and Pro-rate White Glove Shipping
				 * charges and Tax
				 */

				// As part of clean up, replace the getOrderLineDetails API with
				// getOrderLineList API
				/*
				 * Document docOrderDet = XMLUtil .createDocument("OrderLine");
				 * docOrderDet.getDocumentElement().setAttribute(
				 * AcademyConstants.ATTR_ORDER_HEADER_KEY, sOrderHdrKey);
				 * docOrderDet.getDocumentElement().setAttribute(
				 * AcademyConstants.ATTR_ORDER_LINE_KEY,
				 * inputLineCharge.orderLineKey);
				 * docOrderDet.getDocumentElement().setAttribute(
				 * AcademyConstants.ATTR_DOC_TYPE,
				 * AcademyConstants.STR_RETURN_DOCTYPE); env
				 * .setApiTemplate("getOrderLineList",
				 * "global/template/api/getOrderLineDetails.LineChargesForReturnInvoice.xml");
				 * 
				 * Document inXML = AcademyUtil.invokeAPI(env,
				 * "getOrderLineList", docOrderDet);
				 * 
				 * env.clearApiTemplate("getOrderLineList");
				 */

				// Check env for available data
				boolean needListApiCall = false;
				if (env.getTxnObject("RtnWGReceiptDoc") == null) {
					needListApiCall = true;
					m_Logger
							.verbose("The object 'RtnWGReceiptDoc' in env is not available ");
				} else {
					m_Logger
							.verbose("The object 'RtnWGReceiptDoc' in env is available ");
					inXML = (Document) env.getTxnObject("RtnWGReceiptDoc");
					m_Logger.verbose("Order on env is "
							+ inXML.getDocumentElement().getAttribute(
									AcademyConstants.ATTR_ORDER_LINE_KEY)
							+ " and Order in input is : " + sOrderHdrKey);
					if (!inXML.getDocumentElement().getAttribute(
							AcademyConstants.ATTR_ORDER_LINE_KEY).equals(
							sOrderHdrKey))
						needListApiCall = true;
				}
				m_Logger.verbose("Required getOrderList API call : "
						+ needListApiCall);
				if (needListApiCall) {
					// Get Order Details by invoking API getOrderList
					Document docOrderDet = XMLUtil.createDocument("Order");
					docOrderDet.getDocumentElement().setAttribute(
							AcademyConstants.ATTR_ORDER_HEADER_KEY,
							sOrderHdrKey);
					docOrderDet.getDocumentElement().setAttribute(
							AcademyConstants.ATTR_DOC_TYPE,
							AcademyConstants.STR_RETURN_DOCTYPE);
					env
							.setApiTemplate(
									AcademyConstants.API_GET_ORDER_LIST,
									"global/template/api/getOrderList.LineChargesForReturnInvoice.xml");
					inXML = AcademyUtil.invokeAPI(env,
							AcademyConstants.API_GET_ORDER_LIST, docOrderDet);
					env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);

					inXML = XMLUtil.getDocumentForElement((Element) XMLUtil
							.getFirstElementByName(inXML.getDocumentElement(),
									AcademyConstants.ELE_ORDER));

					// CR - Vertex Request changes : Original Order No
					if (env.getTxnObject("ReturnInvoiceCall") == null) {
						Element derivedOrderDetail = (Element) XPathUtil
								.getNode(
										inXML,
										"Order/OrderLines/OrderLine/DerivedFromOrder[@DocumentType='"
												+ AcademyConstants.SALES_DOCUMENT_TYPE
												+ "']");
						if (!YFSObject.isNull(derivedOrderDetail)
								|| !YFSObject.isVoid(derivedOrderDetail)) {
							derivedOrderNo = derivedOrderDetail
									.getAttribute(AcademyConstants.ATTR_ORDER_NO);
							// Find the payment tender for return
							//KER-12036 : Payment Migration Changes to support new Payment Type
							derivedPaymentTender = XPathUtil
									.getString(
											derivedOrderDetail,
											"PaymentMethods/PaymentMethod[@SuspendAnyMoreCharges='N' and (@PaymentType='CREDIT_CARD' or @PaymentType='Credit_Card')]/@PaymentType");

							if (YFCObject.isVoid(derivedPaymentTender)) {
								derivedPaymentTender = XPathUtil
										.getString(
												derivedOrderDetail,
												"PaymentMethods/PaymentMethod[@SuspendAnyMoreCharges='N' and @PaymentType='GIFT_CARD']/@PaymentType");
							}
							m_Logger.verbose("returning PaymentType "
									+ derivedPaymentTender);
						}
						inXML.getDocumentElement().setAttribute(
								"DerivedOrderNo", derivedOrderNo);
						inXML.getDocumentElement().setAttribute(
								"DerivedPaymentTender", derivedPaymentTender);
					}
					// End
					// Manipulate the getOrderList API output template by
					// pro-rate if required and remove <OrderLine> element which
					// is not in receipt closed status
					prepareDataWithTransactionData(inXML);

					// CR - New White Glove Carrier
					String createProgId = inXML.getDocumentElement()
							.getAttribute("Createprogid");
					if (createProgId != null
							&& createProgId
									.equals(AcademyConstants.STR_CC_PROG_ID)) {
						NodeList lstCustAtFault = XPathUtil
								.getNodeList(
										inXML,
										"Order/OrderLines/OrderLine[@ReturnReasonLongDesc='"
												+ AcademyConstants.STR_RTN_CUSTOMER_FAULT
												+ "']");
						if (lstCustAtFault.getLength() > 0
								&& isReqWGQuotationCall)
							invokeQuotationReqCall(env, inXML);
					}
					// Set the final document in the YFSEnvironment object.
					// Therefore, it can use with in the same transaction
					// boundary
					env.setTxnObject("RtnWGReceiptDoc", inXML);
				}

				/* Fix for bug#4967 */
				NodeList lstOrderLineList = XPathUtil.getNodeList(inXML,
						"Order/OrderLines/OrderLine[@OrderLineKey='"
								+ inputLineCharge.orderLineKey + "']");
				if (lstOrderLineList.getLength() > 0) {
					Element eleOrderLine = (Element) lstOrderLineList.item(0);

					// NodeList lineChrgesList = inXML
					// .getElementsByTagName(AcademyConstants.ELE_LINE_CHARGE);
					NodeList lineChrgesList = eleOrderLine
							.getElementsByTagName(AcademyConstants.ELE_LINE_CHARGE);
					m_Logger.verbose("The length of line charges is : "
							+ lineChrgesList.getLength());

					// ArrayList<YFSExtnLineChargeStruct> newLineCharges = new
					// ArrayList<YFSExtnLineChargeStruct>();
					YFSExtnLineChargeStruct newLineChargeTypes[] = new YFSExtnLineChargeStruct[lineChrgesList
							.getLength()];
					List inputChargesList = inputLineCharge.orderLineCharges;
					for (int i = 0; i < lineChrgesList.getLength(); i++) {
						Element lineChrgEle = (Element) lineChrgesList.item(i);
						m_Logger.trace("*********** Element Line Charge -- "
								+ XMLUtil.getElementXMLString(lineChrgEle));

						Iterator chargesItr = inputChargesList.iterator();

						while (chargesItr.hasNext()) {
							chargesStrct = (YFSExtnLineChargeStruct) chargesItr
									.next();
							m_Logger.verbose("111111"
									+ ((Object) chargesStrct).toString());
							m_Logger.verbose("The charge Strct is : "
									+ chargesStrct.chargeCategory + " : "
									+ chargesStrct.chargeName + " : "
									+ chargesStrct.chargeAmount + " : "
									+ chargesStrct.chargePerLine);
							if (chargesStrct.chargeName
									.equalsIgnoreCase(lineChrgEle
											.getAttribute(AcademyConstants.ATTR_CHARGE_NAME))) {
								newLineChargeTypes[i] = new YFSExtnLineChargeStruct();
								newLineChargeTypes[i].chargeCategory = lineChrgEle
										.getAttribute(AcademyConstants.ATTR_CHARGE_CATEGORY);
								newLineChargeTypes[i].chargeName = lineChrgEle
										.getAttribute(AcademyConstants.ATTR_CHARGE_NAME);

								// Fix for #3365 - Reference attribute need to
								// be
								// stamped
								newLineChargeTypes[i].reference = lineChrgEle
										.getAttribute(AcademyConstants.ATTR_REFERENCE);
								// Get already Computed values
								newLineChargeTypes[i].chargePerLine = Double
										.parseDouble(lineChrgEle
												.getAttribute(AcademyConstants.ATTR_CHARGES_PER_LINE));

								/*
								 * shippingCharge =
								 * lineChrgEle.getAttribute(AcademyConstants.ATTR_CHARGES_PER_LINE);
								 * totalShippingInvoicedCharge =
								 * lineChrgEle.getAttribute("InvoicedChargePerLine");
								 * m_Logger.verbose("shippingCharge :
								 * "+shippingCharge+"
								 * totalShippingInvoicedCharge :
								 * "+totalShippingInvoicedCharge);
								 * 
								 * if(inputLineCharge.bLastInvoiceForOrderLine) {
								 * double remainChargAmt =
								 * (Double.parseDouble(shippingCharge)) -
								 * (Double.parseDouble(totalShippingInvoicedCharge));
								 * m_Logger.verbose("remainChargAmt :
								 * "+remainChargAmt);
								 * newLineChargeTypes[i].chargePerLine =
								 * (Double.valueOf(twoDForm.format(remainChargAmt))); }
								 * else { newLineChargeTypes[i].chargePerLine =
								 * Double.parseDouble(shippingCharge); String
								 * overAllQty =
								 * inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDERED_QTY);
								 * m_Logger.verbose("overAllQty : "+overAllQty);
								 * if(foundChargeInEnvInstance &&
								 * chargesStrct.chargeName.equals(AcademyConstants.STR_RETURNSHIPPING_CHARGE)){
								 * newLineChargeTypes[i].chargePerLine =
								 * Double.parseDouble(shippingCharge); }else{
								 * newLineChargeTypes[i].chargePerLine =
								 * (inputLineCharge.shipmentQty)
								 * (Double.valueOf(twoDForm.format((Double.parseDouble(shippingCharge)) /
								 * Double.parseDouble(overAllQty)))); } }
								 */

								newLineCharges.add(newLineChargeTypes[i]);
								m_Logger.verbose("newLineChargeTypes"+newLineChargeTypes[i]);
							}
						}
					}
				}

				outStruct.newLineCharges = newLineCharges;
			}
			invokeVertexCall(env, inXML);
			derivedOrderNo = "";
			derivedPaymentTender = "";
			m_Logger
					.endTimer(" End of AcademyReturnOrderLineChargesForShipmentUEImpl -> getLineChargesForShipmentApi");
		} catch (Exception e) {
			// throw new YFSUserExitException();
			throw AcademyPricingAndPromotionUtil.wrapToYFSException(e);
		} finally {
			derivedOrderNo = "";
			derivedPaymentTender = "";
		}

		m_Logger.verbose(outStruct.toString());

		return outStruct;

	}

	private void prepareDataWithTransactionData(Document inXML)
			throws Exception {

		// Step 1: Remove the element <OrderLine> if it is not in Receipt Closed
		// status at now.
		// Step 2: Calculate shipping charge and Tax in case of Partial Receipt
		// Closed.
		NodeList lstOrderline = inXML.getDocumentElement()
				.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
		for (int i = 0; i < lstOrderline.getLength(); i++) {
			Element eleOrderLine = (Element) lstOrderline.item(i);
			m_Logger
					.verbose(" OrderLineKey is : "
							+ eleOrderLine
									.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
			NodeList lstOLStatus = XPathUtil.getNodeList(eleOrderLine,
					"OrderStatuses/OrderStatus[@Status='"
							+ AcademyConstants.STR_RTN_RECEIPT_CLOSED
							+ "' or @Status='"
							+ AcademyConstants.STR_RTN_READY_TO_INVOICE + "']");
			if (lstOLStatus.getLength() > 0) {
				// TODO: pro-rate logic
				double dTotalOLQty = Double.parseDouble(eleOrderLine
						.getAttribute(AcademyConstants.ATTR_ORDERED_QTY));
				double dStatusQty = 0.0;
				for (int j = 0; j < lstOLStatus.getLength(); j++) {
					Element eleStatus = (Element) lstOLStatus.item(j);
					dStatusQty += Double.parseDouble(XPathUtil.getString(
							eleStatus, "OrderStatusTranQuantity/@StatusQty"));
				}
				if (dTotalOLQty > dStatusQty) {
					m_Logger
							.verbose(" Partial receipt closed : dTotalOLQty '"
									+ dTotalOLQty + "' dStatusQty '"
									+ dStatusQty + "'");
					// Check the current receipt qty is the last qty or not.
					NodeList lstOpenOLStatus = XPathUtil.getNodeList(
							eleOrderLine, "OrderStatuses/OrderStatus[@Status='"
									+ AcademyConstants.STR_RTN_CREATED
									+ "' or @Status='"
									+ AcademyConstants.STR_RTN_AUTHORIZED
									+ "' or @Status='"
									+ AcademyConstants.STR_RTN_DRAFT_CREATED
									+ "']");
					boolean isLastQtyOfLine = true;
					if (lstOpenOLStatus.getLength() > 0) {
						// means this is not the last qty to be received
						isLastQtyOfLine = false;
						isReqWGQuotationCall = true;
					}
					m_Logger.verbose(" isLastQtyOfLine : " + isLastQtyOfLine);
					NodeList lstLineCharges = XPathUtil.getNodeList(
							eleOrderLine,
							"LineCharges/LineCharge[@ChargeAmount!='0.00']");
					for (int lc = 0; lc < lstLineCharges.getLength(); lc++) {
						Element eleLineCharge = (Element) lstLineCharges
								.item(lc);
						double chargeAmount = Double
								.parseDouble(eleLineCharge
										.getAttribute(AcademyConstants.ATTR_CHARGE_AMT));
						m_Logger
								.verbose("Charge catagory :"
										+ eleLineCharge
												.getAttribute(AcademyConstants.ATTR_CHARGE_CATEGORY)
										+ " name : "
										+ eleLineCharge
												.getAttribute(AcademyConstants.ATTR_CHARGE_NAME)
										+ " amount : " + chargeAmount);
						if (isLastQtyOfLine) {
							// TODO: get the delta by minus the Invoiced amount
							double totalRtnInvoicedCharge = Double
									.parseDouble(eleLineCharge
											.getAttribute(AcademyConstants.ATTR_CHARGES_INVOICED_CHARGE_PER_LINE));
							double dProrateVal = chargeAmount
									- totalRtnInvoicedCharge;
							m_Logger.verbose(" The charges for last qty is : "
									+ dProrateVal);
							eleLineCharge
									.setAttribute(
											AcademyConstants.ATTR_CHARGE_AMT,
											truncateDecimal(
													String.valueOf(dProrateVal),
													AcademyConstants.INT_NUMBER_OF_DECIMALS));
							eleLineCharge
									.setAttribute(
											AcademyConstants.ATTR_CHARGES_PER_LINE,
											truncateDecimal(
													String.valueOf(dProrateVal),
													AcademyConstants.INT_NUMBER_OF_DECIMALS));
						} else {
							double dProrateVal = chargeAmount
									* (dStatusQty / dTotalOLQty);
							m_Logger.verbose(" The prorated value is : "
									+ dProrateVal);
							eleLineCharge
									.setAttribute(
											AcademyConstants.ATTR_CHARGE_AMT,
											truncateDecimal(
													String.valueOf(dProrateVal),
													AcademyConstants.INT_NUMBER_OF_DECIMALS));
							eleLineCharge
									.setAttribute(
											AcademyConstants.ATTR_CHARGES_PER_LINE,
											truncateDecimal(
													String.valueOf(dProrateVal),
													AcademyConstants.INT_NUMBER_OF_DECIMALS));
						}
					}
					NodeList lstLineTax = XPathUtil.getNodeList(eleOrderLine,
							"LineTaxes/LineTax[@Tax!='0.00']");
					for (int lt = 0; lt < lstLineTax.getLength(); lt++) {
						Element eleLineTax = (Element) lstLineTax.item(lt);
						double tax = Double.parseDouble(eleLineTax
								.getAttribute(AcademyConstants.ATTR_TAX));
						m_Logger
								.verbose("Tax catagory : "
										+ eleLineTax
												.getAttribute(AcademyConstants.ATTR_CHARGE_CATEGORY)
										+ " name : "
										+ eleLineTax
												.getAttribute(AcademyConstants.ATTR_CHARGE_NAME)
										+ " tax : " + tax);
						if (isLastQtyOfLine) {
							// TODO: get the delta by minus the Invoiced tax
							double totalRtnInvoicedTax = Double
									.parseDouble(eleLineTax
											.getAttribute(AcademyConstants.ATTR_INVOICED_TAX));
							double prorateTax = tax - totalRtnInvoicedTax;
							m_Logger.verbose(" The tax for last qty is : "
									+ prorateTax);
							eleLineTax
									.setAttribute(
											AcademyConstants.ATTR_TAX,
											truncateDecimal(
													String.valueOf(prorateTax),
													AcademyConstants.INT_NUMBER_OF_DECIMALS));
						} else {
							double prorateTax = tax
									* (dStatusQty / dTotalOLQty);
							m_Logger.verbose(" The prorated tax is : "
									+ prorateTax);
							eleLineTax
									.setAttribute(
											AcademyConstants.ATTR_TAX,
											truncateDecimal(
													String.valueOf(prorateTax),
													AcademyConstants.INT_NUMBER_OF_DECIMALS));
						}
					}
				}
			} else {
				eleOrderLine.getParentNode().removeChild(eleOrderLine);
				i--;
			}
		}
		m_Logger.verbose("Final template is : " + XMLUtil.getXMLString(inXML));
	}

	private void invokeQuotationReqCall(YFSEnvironment env,
			Document returnOrderDoc) throws Exception {
		NodeList lstOL = returnOrderDoc
				.getElementsByTagName(AcademyConstants.ELE_ORDER_LINE);
		Document dRtnScacCode = getReturnWGCarrier(env);
		// Check for partial Return Received
		boolean bReqQuotationCall = false;
		if (lstOL != null && lstOL.getLength() > 0) {
			for (int i = 0; i < lstOL.getLength(); i++) {
				Element eleOrderLine = (Element) lstOL.item(i);
				Node scacNode = XPathUtil
						.getNode(
								dRtnScacCode,
								"CommonCodeList/CommonCode[@CodeValue='"
										+ eleOrderLine
												.getAttribute(AcademyConstants.ATTR_SCAC)
										+ "']");
				if (scacNode != null) {
					m_Logger.verbose(" Customer At Falut and WG Carrier.");
					bReqQuotationCall = true;
				}
			}
		}
		m_Logger.verbose(" is it Partial Recipt and Invoice : "
				+ isReqWGQuotationCall + " for WG Return Order : "
				+ bReqQuotationCall);
		// Invoke Vertex Quotation Call
		if (bReqQuotationCall) {
			returnOrderDoc.getDocumentElement().setAttribute("CallType",
					"QuoteCall");
			returnOrderDoc.getDocumentElement().setAttribute("TranType",
					"CreateReturnInvoice");

			Document vertexQuotationCallReq = AcademyUtil.invokeService(env,
					"AcademyChangeReturnOrderToQuoteCallRequest",
					returnOrderDoc);
			m_Logger.verbose("Request XML for Vertex call is "
					+ XMLUtil.getXMLString(vertexQuotationCallReq));

			Document vertexQuotationCallRes = null;
			if (vertexQuotationCallReq.getElementsByTagName("LineItem")
					.getLength() > 0) {
				vertexQuotationCallRes = AcademyUtil
						.invokeService(env, "AcademyVertexQuoteCallRequest",
								vertexQuotationCallReq);

				// Update Response
				for (int indx = 0; indx < lstOL.getLength(); indx++) {
					Element eleOrderLine = (Element) lstOL.item(indx);
					m_Logger
							.verbose("The OL Key of received line is : "
									+ eleOrderLine
											.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY));
					Element eleOrdLineTax = (Element) XPathUtil
							.getNode(
									eleOrderLine,
									"LineTaxes/LineTax[@ChargeCategory='"
											+ AcademyConstants.STR_RETURNSHIPPING_CHARGE
											+ "']");
					if (eleOrdLineTax == null)
						continue;

					NodeList lstVertexLI = vertexQuotationCallRes
							.getElementsByTagName("LineItem");
					String vertexReturnShippingTax = null;
					String extendedPrice = null;
					for (int inx = 0; inx < lstVertexLI.getLength(); inx++) {
						Element eleLineItem = (Element) lstVertexLI.item(inx);
						NodeList lstCodeField = eleLineItem
								.getElementsByTagName("FlexibleCodeField");
						for (int cf = 0; cf < lstCodeField.getLength(); cf++) {
							if (((Element) lstCodeField.item(cf)).getAttribute(
									"fieldId").equals("3")) {
								m_Logger.verbose("Found FieldId = 3");
								if (((Element) lstCodeField.item(cf))
										.getTextContent()
										.equals(
												eleOrderLine
														.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY))) {
									m_Logger
											.verbose("Found matching Order Line Item : "
													+ ((Element) lstCodeField
															.item(cf))
															.getTextContent());
									vertexReturnShippingTax = ((Element) eleLineItem
											.getElementsByTagName("TotalTax")
											.item(0)).getTextContent();
									extendedPrice = ((Element) eleLineItem
											.getElementsByTagName(
													"ExtendedPrice").item(0))
											.getTextContent();
								}
							}
						}
					}
					m_Logger.verbose("vertexReturnShippingTax : "
							+ vertexReturnShippingTax + " \t extendedPrice = "
							+ extendedPrice);
					// Set it on OrderLine Tax
					if (vertexReturnShippingTax != null)
						eleOrdLineTax.setAttribute(AcademyConstants.ATTR_TAX,
								vertexReturnShippingTax);
				}
			}
		}
	}

	private Document getReturnWGCarrier(YFSEnvironment env) throws Exception {
		Document docWGSCACLst = null;
		try {
			Document docScacCodeInput = XMLUtil
					.createDocument(AcademyConstants.ELE_COMMON_CODE);
			docScacCodeInput.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_CODE_TYPE,
					AcademyConstants.STR_RTN_RCPT_WG_SCAC);
			docScacCodeInput.getDocumentElement().setAttribute(
					AcademyConstants.ORGANIZATION_CODE,
					AcademyConstants.PRIMARY_ENTERPRISE);
			env.setApiTemplate(AcademyConstants.API_GET_COMMON_CODELIST,
					"global/template/api/getCommonCodeList.IsWhiteGloveSCAC.xml");
			docWGSCACLst = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_COMMON_CODELIST, docScacCodeInput);
			env.clearApiTemplate(AcademyConstants.API_GET_COMMON_CODELIST);
		} catch (Exception e) {
			e.printStackTrace();
			m_Logger.verbose("Failed to invoke getCommonCodeList API : " + e);
			throw e;
		}
		return docWGSCACLst;
	}

	/**
	 * Method wraps Exception object to YFSUserExitException object
	 * 
	 * @param e
	 * @return YFSUserExitException
	 */
	private static YFSUserExitException getYFSUserExceptionWithTrace(Exception e) {
		YFSUserExitException yfsUEException = new YFSUserExitException();
		yfsUEException.setStackTrace(e.getStackTrace());
		return yfsUEException;
	}

	// sending the invoice details to Vertex.
	private void invokeVertexCall(YFSEnvironment env, Document inXML)
			throws Exception {
		try {
			m_Logger
					.beginTimer(" Begining of AcademyReturnOrderLineChargesForShipmentUEImpl -> invokeVertexCall");

			if (env.getTxnObject("ReturnInvoiceCall") == null) {
				// get the Shipment Key
				/*
				 * String sOrderHdrKey = inputLineCharge.orderHeaderKey; if
				 * (sOrderHdrKey != null && !sOrderHdrKey.equals("")) { Document
				 * docOrderDet = XMLUtil
				 * .createDocument(AcademyConstants.ELE_ORDER);
				 * docOrderDet.getDocumentElement().setAttribute(
				 * AcademyConstants.ATTR_ORDER_HEADER_KEY, sOrderHdrKey); env
				 * .setApiTemplate(AcademyConstants.API_GET_ORDER_LIST,
				 * "global/template/api/getOrderList.CallToVertex.xml");
				 * 
				 * Document inXML = AcademyUtil.invokeAPI(env,
				 * AcademyConstants.API_GET_ORDER_LIST, docOrderDet);
				 * 
				 * env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
				 * 
				 * inXML =
				 * XMLUtil.getDocumentForElement((Element)XMLUtil.getFirstElementByName(inXML.getDocumentElement(),
				 * AcademyConstants.ELE_ORDER));
				 */
				// Check weather In-Store return order or not.
				Element eleForShipNode = null;
				if (inXML.getDocumentElement()
						.getElementsByTagName("OrderLine").getLength() > 0) {
					/*
					 * eleForShipNode = (Element) inXML.getDocumentElement()
					 * .getElementsByTagName("OrderLine").item(0);
					 */
					eleForShipNode = (Element) XPathUtil.getNode(inXML
							.getDocumentElement(),
							"OrderLines/OrderLine[@ShipNode!='']");
				}

				if (eleForShipNode == null)
					m_Logger
							.verbose("AcademyReturnOrderLineChargesForShipmentUEImpl :: order line is null ");

				if (eleForShipNode != null && AcademyCommonCode.getCodeValueList(env,AcademyConstants.STR_CLS_RETURN_NODE_VALUE, AcademyConstants.HUB_CODE).contains(eleForShipNode.getAttribute("ShipNode"))) {
					m_Logger
							.verbose("AcademyReturnOrderLineChargesForShipmentUEImpl :: shipnode is "
									+ eleForShipNode.getAttribute("ShipNode"));
					// do the distribute call
					env.setTxnObject("ReturnDistributeCall",
							AcademyConstants.STR_YES);
					// convert the input to Vertex Quote Call Request xml.
					inXML.getDocumentElement().setAttribute("CallType",
							"DistributeCall");
					// CR - Vertex changes; set Sterling Function name
					inXML.getDocumentElement().setAttribute("TranType",
							"CreateReturnInvoice");
					// CR - Vertex Changes; set DerivedOrderNo and
					// DerivedPaymentTender in input to VertexRequest
					if (derivedOrderNo.trim().length() > 0)
						inXML.getDocumentElement().setAttribute(
								"DerivedOrderNo", derivedOrderNo);

					if (derivedPaymentTender.trim().length() > 0)
						inXML.getDocumentElement().setAttribute(
								"DerivedPaymentTender", derivedPaymentTender);

					Document vertexDistributeCallReq = AcademyUtil
							.invokeService(
									env,
									"AcademyChangeOrderToDistributeCallRequest",
									inXML);

					Document vertexDistributeCallResp = AcademyUtil
							.invokeService(env,
									"AcademyVertexDistributeCallRequest",
									vertexDistributeCallReq);
					// Start Fix for #3855 - as part of R26 // This UE invokes
					// for each line. Therefore,vertex distribute call shall
					// invoke only one time.
					env.setTxnObject("ReturnInvoiceCall",
							vertexDistributeCallResp);

					// End for #3855
				} else if (eleForShipNode != null
						&& eleForShipNode.getAttribute("ShipNode").equals(
								AcademyConstants.ACADEMY_SHIP_NODE)) {
					// logic for #4243 - Only IN-STORE Return Order where
					// ShipNode is '701'
					m_Logger.verbose("ShipNode is: "
							+ eleForShipNode.getAttribute("ShipNode")
							+ " and OrderName is : "
							+ inXML.getDocumentElement().getAttribute(
									"OrderName"));
					env.setTxnObject("ReturnOrderName", inXML
							.getDocumentElement().getAttribute(
									AcademyConstants.ATTR_ORDER_NAME));
				}
				// }
			}
			m_Logger
					.endTimer(" End of AcademyReturnOrderLineChargesForShipmentUEImpl -> invokeVertexCall");
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception();
		}
	}

	// Added below method as part of Penny issue - truncate after specified
	// decimals
	private String truncateDecimal(String value, int noOfDecimal) {
		String newVal = null;
		int decimalPos = value.indexOf(".");
		newVal = value.substring(0, decimalPos);
		String tempStr = value.substring(decimalPos + 1);
		if (tempStr.length() > noOfDecimal) {
			newVal = newVal + "." + tempStr.substring(0, noOfDecimal);
		} else
			newVal = newVal + "." + tempStr;
		return newVal;
	}
}