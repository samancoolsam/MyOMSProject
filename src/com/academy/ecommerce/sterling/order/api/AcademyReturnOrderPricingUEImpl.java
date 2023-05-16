package com.academy.ecommerce.sterling.order.api;

import java.util.HashMap;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyReturnOrderUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCDoubleUtils;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * this UE Implemeted to copy SalesOrder charges/taxes to ReturnOrder
 * 
 * @author vgummadidala
 * 
 */
public class AcademyReturnOrderPricingUEImpl implements YIFCustomApi {

	Document docOutput = null;

	String clsRemoveShippingCharges = "N";

	String isWhiteGloveReturns = "";

	Document docRefundableReasonCodes = null;

	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyReturnOrderPricingUEImpl.class);

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
	}

	public Document repriceOrder(YFSEnvironment env, Document inXML) {
		Document docSalesOrderDetails = null;
		String strOrderHeaderKey = null;
		String strReturnReason = null;
		String repricingFlag = "Y";

		boolean bPaymentExists = false;

		try {
			log
					.beginTimer(" Begining of AcademyReturnOrderPricingUEImpl - repriceOrder() Api");
			if ((String) env.getTxnObject("RepricingFlag") != null)
				repricingFlag = (String) env.getTxnObject("RepricingFlag");
			if ((String) env.getTxnObject("CLSRemoveShippingCharges") != null)
				clsRemoveShippingCharges = (String) env
						.getTxnObject("CLSRemoveShippingCharges");

			// For WhiteGlove Returns -- we are copying all charges/taxes except
			// Return or Shipping Charges/Taxes - isWhiteGloveReturns- Y;
			if ((String) env.getTxnObject("isWGReturns") != null)
				isWhiteGloveReturns = (String) env.getTxnObject("isWGReturns");

			// Avoiding below Re-pricing logic in case of CLS/Store return
			// Receipt process - changeOrder calls
			if (!repricingFlag.equalsIgnoreCase("N")) {
				env.setTxnObject("RepricingFlag", "");
				docOutput = XMLUtil.cloneDocument(inXML);
				strOrderHeaderKey = XPathUtil.getString(docOutput
						.getDocumentElement(),
						AcademyConstants.XPATH_DERIVEDFROM_HEADERKEY);
				docSalesOrderDetails = getCompleteSalesOrderDetails(
						strOrderHeaderKey, false, env);

				// Commeting this below block as - Raising an Alert not required
				// if no payment Tender on CLS/Return Creation Process. bug
				// #9836

				/*
				 * bPaymentExists = checkIfPaymentExistsForOrder(docOutput); if
				 * (!bPaymentExists) {
				 * 
				 * raiseAlertForCSR(inXML, env); }
				 */
				docRefundableReasonCodes = AcademyReturnOrderUtil
						.getRefundableReasonCodeValuesList(env);

				processPaymentsForOrderLines(docOutput, docSalesOrderDetails,
						env);
				// re-setting back to N
				env.setTxnObject("isWGReturns", "N");
			}

			else
				return inXML;
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
		log
				.endTimer(" End of AcademyReturnOrderPricingUEImpl - repriceOrder() Api");
		return docOutput;

	}

	private Document getCompleteSalesOrderDetails(String orderHeaderKey,
			boolean detailAPI, YFSEnvironment env) {

		Document docCompleteOrderDetails = null;
		Document docApiInput = null;
		Element eleOrder = null;
		try {
			log
					.beginTimer(" begin of AcademyReturnOrderPricingUEImpl - getCompleteSalesOrderDetails() Api");
			docApiInput = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			eleOrder = docApiInput.getDocumentElement();
			eleOrder.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY,
					orderHeaderKey);
			if (!detailAPI) {
				env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST,
						AcademyConstants.STR_TEMPLATEFILE_ORDERDETAILS_UEIMPL);
				docCompleteOrderDetails = AcademyUtil.invokeAPI(env,
						AcademyConstants.API_GET_ORDER_LIST, docApiInput);
				env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
			} else {
				env
						.setApiTemplate(
								AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS,
								"global/template/api/getOrderDetails.ToReturnRepricing.xml");
				docCompleteOrderDetails = AcademyUtil.invokeAPI(env,
						AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS,
						docApiInput);
				env
						.clearApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
		log
				.endTimer(" End of AcademyReturnOrderPricingUEImpl - getCompleteSalesOrderDetails() Api");
		return docCompleteOrderDetails;
	}

	private void processPaymentsForOrderLines(Document inDoc,
			Document docSalesOrderDetails, YFSEnvironment env) {
		String strReturnReasonCode = null;
		Element eleRefundableReasonCode = null;
		Element eleCurrentOrderLine = null;
		Element eleLinePriceInfo = null;
		Element elePromotions = null;
		Element elePromotionsInSalesOrder = null;
		Element elePromotion = null;
		String strDerFromOrderLineKey = null;
		Element eleOrigOrderLine = null;
		String strOrigUnitPrice = null;
		NodeList nOrderLines = null;
		NodeList nPromotions = null;
		int iNoOfOrderLines = 0;
		int iPromotions = 0;
		HashMap<String, Float> retnQtyInProcess = new HashMap<String, Float>();
		Document docOrderWithPrevRODetails = null;
		try {
			log
					.beginTimer(" Begin of AcademyReturnOrderPricingUEImpl - processPaymentsForOrderLines() Api");
			nOrderLines = XPathUtil.getNodeList(inDoc.getDocumentElement(),
					AcademyConstants.XPATH_ORDERLINE);
			iNoOfOrderLines = nOrderLines.getLength();

			// Commented due to product bug and also promotions will be
			// available as LineCharges in the form of Discounts

			for (int i = 0; i < iNoOfOrderLines; i++) {
				eleCurrentOrderLine = (Element) nOrderLines.item(i);
				strDerFromOrderLineKey = eleCurrentOrderLine
						.getAttribute(AcademyConstants.ATTR_DERIVEDFROM_ORDERLINE_KEY);
				eleOrigOrderLine = (Element) XPathUtil.getNode(
						docSalesOrderDetails.getDocumentElement(),
						"Order/OrderLines/OrderLine[@OrderLineKey='"
								+ strDerFromOrderLineKey + "']");

				strOrigUnitPrice = XPathUtil.getString(eleOrigOrderLine,
						AcademyConstants.XPATH_LINEPRICE);
				eleLinePriceInfo = (Element) XPathUtil.getNode(
						eleCurrentOrderLine,
						AcademyConstants.ELE_LINEPRICE_INFO);
				eleLinePriceInfo.setAttribute(AcademyConstants.ATTR_UNIT_PRICE,
						strOrigUnitPrice);
				strReturnReasonCode = eleCurrentOrderLine
						.getAttribute("ReturnReason");

				eleRefundableReasonCode = (Element) XPathUtil.getNode(
						this.docRefundableReasonCodes.getDocumentElement(),
						"CommonCode[@CodeValue='" + strReturnReasonCode + "']");
				if (!YFCObject.isVoid(eleRefundableReasonCode)) {

					processOrderLineTaxesChargesAndPromotions(
							eleCurrentOrderLine, eleOrigOrderLine, true, env,
							retnQtyInProcess, inDoc, docOrderWithPrevRODetails);
				} else {
					processOrderLineTaxesChargesAndPromotions(
							eleCurrentOrderLine, eleOrigOrderLine, false, env,
							retnQtyInProcess, inDoc, docOrderWithPrevRODetails);

				}

			}
			log
					.endTimer(" End of AcademyReturnOrderPricingUEImpl - processPaymentsForOrderLines() Api");
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}

	}

	private void processOrderLineTaxesChargesAndPromotions(
			Element currReturnOrderLine, Element origSalesOrderLine,
			boolean bIsAcademyAtFault, YFSEnvironment env,
			HashMap retnQtyInProcess, Document inDoc,
			Document docOrderWithPrevRODetails) {
		NodeList nAwardsList = null;
		NodeList nTaxesList = null;
		NodeList nChargesList = null;
		Element eleSalesOrderAwards = null;
		Element eleCurrPromotion = null;
		Element eleCurrTax = null;
		Element eleCurrCharge = null;
		Element eleSourceAwards = null;
		Element eleDestAwards = null;
		Element eleSrcLineTaxes = null;
		Element eleDestLineTaxes = null;
		Element eleSrcLineCharges = null;
		Element eleDestLineCharges = null;
		String strOrigOrderLineQty = null;
		String strReturnedQty = null;
		String strLinePromotionValue = null;
		String strLineTaxValue = null;
		String strLineChargeValue = null;
		int iNoOfPromotions = 0;
		int iNoOfCharges = 0;
		int iNoOfTaxes = 0;
		try {
			log
					.beginTimer(" Begin of AcademyReturnOrderPricingUEImpl - processOrderLineTaxesChargesAndPromotions() Api");
			strOrigOrderLineQty = origSalesOrderLine
					.getAttribute(AcademyConstants.ATTR_ORDERED_QTY);
			strReturnedQty = currReturnOrderLine
					.getAttribute(AcademyConstants.ATTR_ORDERED_QTY);
			eleSrcLineTaxes = (Element) origSalesOrderLine
					.getElementsByTagName(AcademyConstants.ELE_LINE_TAXES)
					.item(0);
			eleDestLineTaxes = (Element) currReturnOrderLine
					.getElementsByTagName(AcademyConstants.ELE_LINE_TAXES)
					.item(0);
			if (YFCObject.isVoid(eleDestLineTaxes)) {
				Element eleTempTaxes = currReturnOrderLine.getOwnerDocument()
						.createElement(AcademyConstants.ELE_LINE_TAXES);
				eleDestLineTaxes = (Element) currReturnOrderLine
						.appendChild(eleTempTaxes);

			}
			float qtyBeingRtn = new Float(strReturnedQty).floatValue();
			String parentOLK = origSalesOrderLine
					.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
			if (retnQtyInProcess.containsKey(parentOLK)) {
				Float tmpProcessedQty = (Float) retnQtyInProcess.get(parentOLK);
				tmpProcessedQty += new Float(strReturnedQty);
				retnQtyInProcess.put(parentOLK, tmpProcessedQty);
				qtyBeingRtn = tmpProcessedQty.floatValue();
			} else {
				retnQtyInProcess.put(parentOLK, new Float(strReturnedQty));
			}

			NodeList lstReturnQtyStatus = XPathUtil
					.getNodeList(origSalesOrderLine,
							"OrderStatuses/OrderStatus[@Status='3700.02' or @Status='3700.01']");
			log.verbose("Already had return order/s : "
					+ lstReturnQtyStatus.getLength());
			float prevProcessedRtnQty = 0;
			for (int indx = 0; indx < lstReturnQtyStatus.getLength(); indx++) {
				Element eleReturnQtyStatus = (Element) lstReturnQtyStatus
						.item(indx);
				prevProcessedRtnQty += Float.parseFloat(eleReturnQtyStatus
						.getAttribute("StatusQty"));
			}
			log.verbose("Already returned Qty is : " + prevProcessedRtnQty);
			float totalRtnQty = prevProcessedRtnQty + qtyBeingRtn;
			log.verbose("Total return qty including the processing qty is : "
					+ totalRtnQty);
			boolean reqRoundedVal = false;
			if (Float.parseFloat(strOrigOrderLineQty) == totalRtnQty) {
				reqRoundedVal = true;
			}
			log.verbose("Required rounded value : " + reqRoundedVal);
			if (reqRoundedVal && prevProcessedRtnQty > 0
					&& docOrderWithPrevRODetails == null) {
				log
						.verbose("Get the complete Order details for previous created/received RO ");
				docOrderWithPrevRODetails = getCompleteSalesOrderDetails(
						origSalesOrderLine
								.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY),
						true, env);
			}

			XMLUtil.copyElement(currReturnOrderLine.getOwnerDocument(),
					eleSrcLineTaxes, eleDestLineTaxes);
			nTaxesList = XPathUtil.getNodeList(currReturnOrderLine,
					AcademyConstants.XPATH_ORDERLINE_TAXES);

			if (!YFCObject.isVoid(nTaxesList)) {
				iNoOfTaxes = nTaxesList.getLength();
			}
			for (int i = 0; i < iNoOfTaxes; i++) {

				eleCurrTax = (Element) nTaxesList.item(i);

				if (!(eleCurrTax.getAttribute(AcademyConstants.ATTR_TAX_NAME)
						.equalsIgnoreCase(AcademyConstants.STR_RETURNSHIP_TAX))) {
					eleCurrTax
							.removeAttribute(AcademyConstants.ATTR_CHRG_NAME_KEY);
					eleCurrTax.removeAttribute("InvoicedTax");
					/*
					 * Start Fix 8776
					 */
					eleCurrTax.removeAttribute(AcademyConstants.ATTR_REM_TAXES);

					/*
					 * End Fix 8776
					 * 
					 */

					strLineTaxValue = eleCurrTax
							.getAttribute(AcademyConstants.STR_TAX);

					if (strLineTaxValue == null)
						strLineTaxValue = "0";
					float fNewTaxValue = ((new Float(strLineTaxValue)
							.floatValue()) / (new Float(strOrigOrderLineQty)
							.floatValue()))
							* (new Float(strReturnedQty).floatValue());
					String strNewTax = String.valueOf(fNewTaxValue);
					if (!reqRoundedVal) {
						strNewTax = truncateDecimal(strNewTax,
								AcademyConstants.INT_NUMBER_OF_DECIMALS);
						log.verbose("after truncate the prorated value is : "
								+ strNewTax);
					} else if (new Float(strLineTaxValue).floatValue() != 0) {
						float prevReturnedTax = 0;
						log.verbose("ChargeCategory : "
								+ eleCurrTax.getAttribute("ChargeCategory")
								+ " :::  TaxName: "
								+ eleCurrTax.getAttribute("TaxName"));
						if (docOrderWithPrevRODetails != null) {
							NodeList lstROLineTaxes = XPathUtil
									.getNodeList(
											docOrderWithPrevRODetails
													.getDocumentElement(),
											"ReturnOrders/ReturnOrder/OrderLines/OrderLine[@DerivedFromOrderLineKey='"
													+ parentOLK
													+ "']/LineTaxes/LineTax[@ChargeCategory='"
													+ eleCurrTax
															.getAttribute("ChargeCategory")
													+ "' and @TaxName='"
													+ eleCurrTax
															.getAttribute("TaxName")
													+ "']");
							log.verbose(" total line taxes of Past ROs : "
									+ lstROLineTaxes.getLength());
							for (int ltIndx = 0; ltIndx < lstROLineTaxes
									.getLength(); ltIndx++) {
								Element prevTax = (Element) lstROLineTaxes
										.item(ltIndx);
								prevReturnedTax += Float.parseFloat(prevTax
										.getAttribute("Tax"));
							}
						}
						NodeList lstProcRtnLineTaxes = XPathUtil
								.getNodeList(
										inDoc.getDocumentElement(),
										"OrderLines/OrderLine[@DerivedFromOrderLineKey='"
												+ parentOLK
												+ "' and @OrderLineKey!='"
												+ currReturnOrderLine
														.getAttribute("OrderLineKey")
												+ "']/LineTaxes/LineTax[@ChargeCategory='"
												+ eleCurrTax
														.getAttribute("ChargeCategory")
												+ "' and @TaxName='"
												+ eleCurrTax
														.getAttribute("TaxName")
												+ "']");
						log
								.verbose("total line taxes from current processing RO other than current line is : "
										+ lstProcRtnLineTaxes.getLength());
						for (int lt = 0; lt < lstProcRtnLineTaxes.getLength(); lt++) {
							Element procRtnTax = (Element) lstProcRtnLineTaxes
									.item(lt);
							prevReturnedTax += Float.parseFloat(procRtnTax
									.getAttribute("Tax"));
						}
						log.verbose(Float.parseFloat(strLineTaxValue) + " - "
								+ prevReturnedTax);
						float remainTax = Float.parseFloat(strLineTaxValue)
								- prevReturnedTax;
						log.verbose(" Remain Tax is :: " + remainTax);
						float delta = remainTax - fNewTaxValue;
						if (YFCDoubleUtils.roundOff(delta) > 0.09) {
							strNewTax = truncateDecimal(String
									.valueOf(fNewTaxValue),
									AcademyConstants.INT_NUMBER_OF_DECIMALS);
						} else {
							strNewTax = String.valueOf(remainTax);
						}
					}
					// copying full tax if its WG returns - shiping tax amt
					// based on CR logic
					// Fix for 4964
					// if ((isWhiteGloveReturns.equalsIgnoreCase("Y"))
					// && eleCurrTax.getAttribute(
					// AcademyConstants.ATTR_TAX_NAME)
					// .equalsIgnoreCase("ShippingTax"))
					// eleCurrTax.setAttribute(AcademyConstants.STR_TAX,
					// strLineTaxValue);
					// else
					eleCurrTax
							.setAttribute(AcademyConstants.STR_TAX, strNewTax);
					// removing the Shipping Tax Charges if its is at Customer
					// fault (return Reason Code) -- HasTaken care in Receipt
					// Process -bug#9902
					// if its white glove returns - then don't copy any
					// shipping/Return Taxes to Return Order --- isWhiteGlove -Y
					// For WhiteGlove Returns -- remove shipping charges.
					log.verbose("eleCurrTax--"
							+ XMLUtil.getElementXMLString(eleCurrTax));
					// Fix for bug#4952
					if ((this.clsRemoveShippingCharges.equalsIgnoreCase("Y"))
							|| (!bIsAcademyAtFault)) {
						// ChargeCategory='Shipping' - changed to remove all
						// shipping related taxes.
						if (eleCurrTax.getAttribute("ChargeCategory")
								.equalsIgnoreCase("Shipping")
								&& (!bIsAcademyAtFault)) {
							Node ncurrentTax = nTaxesList.item(i);
							Node parentTaxNode = ncurrentTax.getParentNode();
							parentTaxNode.removeChild(ncurrentTax);

						}
					}
					// Handling Internal Tax Write-off as part of bug Fix #11294
					// -- commenting this portion to handle tax_writeoff on
					// returns
					/*
					 * if(eleCurrTax.getAttribute(AcademyConstants.ATTR_TAX_NAME).equalsIgnoreCase("INTERNAL_TAX_WRITEOFF")) {
					 * Node ncurrentTax=nTaxesList.item(i); Node
					 * parentTaxNode=ncurrentTax.getParentNode();
					 * parentTaxNode.removeChild(ncurrentTax); }
					 */
				}
			}

			eleSrcLineCharges = (Element) origSalesOrderLine
					.getElementsByTagName(AcademyConstants.ELE_LINE_CHARGES)
					.item(0);
			eleDestLineCharges = (Element) currReturnOrderLine
					.getElementsByTagName(AcademyConstants.ELE_LINE_CHARGES)
					.item(0);
			if (YFCObject.isVoid(eleDestLineCharges)) {
				Element eleTempCharges = currReturnOrderLine.getOwnerDocument()
						.createElement(AcademyConstants.ELE_LINE_CHARGES);
				eleDestLineCharges = (Element) currReturnOrderLine
						.appendChild(eleTempCharges);
			}
			// madhura
			if ((!(isWhiteGloveReturns.equalsIgnoreCase("Y")))) {
				XMLUtil.copyElement(currReturnOrderLine.getOwnerDocument(),
						eleSrcLineCharges, eleDestLineCharges);
			}
			if (((isWhiteGloveReturns.equalsIgnoreCase("Y")))) {
				XMLUtil.copyElement(currReturnOrderLine.getOwnerDocument(),
						eleSrcLineCharges, eleDestLineCharges);

				nChargesList = XPathUtil.getNodeList(currReturnOrderLine,
						AcademyConstants.XPATH_ORDERLINE_CHARGES);
				if (!YFCObject.isVoid(nChargesList)) {
					iNoOfCharges = nChargesList.getLength();

					for (int i = 0; i < iNoOfCharges; i++) {
						eleCurrCharge = (Element) nChargesList.item(i);
						if (bIsAcademyAtFault) {
							if ((eleCurrCharge
									.getAttribute(AcademyConstants.ATTR_CHARGE_NAME)
									.equalsIgnoreCase(AcademyConstants.STR_SHIPPING_CHARGE))
									&& (eleCurrCharge.getAttribute("Reference")
											.equalsIgnoreCase(""))) {
								Node ncurrentCharge = nChargesList.item(i);
								Node parentNode = ncurrentCharge
										.getParentNode();
								parentNode.removeChild(ncurrentCharge);
							}
						}
						if (!bIsAcademyAtFault) {
							if ((eleCurrCharge
									.getAttribute(AcademyConstants.ATTR_CHARGE_NAME)
									.equalsIgnoreCase(AcademyConstants.STR_SHIPPING_CHARGE))) {
								Node ncurrentCharge = nChargesList.item(i);
								Node parentNode = ncurrentCharge
										.getParentNode();
								parentNode.removeChild(ncurrentCharge);
							}
						}
					}
				}

			}
			nChargesList = XPathUtil.getNodeList(currReturnOrderLine,
					AcademyConstants.XPATH_ORDERLINE_CHARGES);
			if (!YFCObject.isVoid(nChargesList)) {
				iNoOfCharges = nChargesList.getLength();
			}
			for (int i = 0; i < iNoOfCharges; i++) {
				eleCurrCharge = (Element) nChargesList.item(i);

				// For WhiteGlove Returns -- remove shipping charges.
				// Fix as per new requirement
				if ((isWhiteGloveReturns.equalsIgnoreCase("Y"))
						&& (!bIsAcademyAtFault)) {
					// removing the Shipping Charges if its is at Customer fault
					// (return Reason Code) -- bug#9902
					if (eleCurrCharge.getAttribute(
							AcademyConstants.ATTR_CHARGE_NAME)
							.equalsIgnoreCase(
									AcademyConstants.STR_SHIPPING_CHARGE)) {
						/*
						 * String isRemove = "no"; isRemove =
						 * eleCurrCharge.getAttribute("Reference");
						 * log.verbose("get the value of Reference is" +
						 * isRemove); if (isRemove.equalsIgnoreCase("yes")) {
						 * log .verbose("do nothing -IF blokc -- making
						 * reference back to Empty");
						 * eleCurrCharge.setAttribute("Reference", ""); // do
						 * nothing } else {
						 */
						log
								.verbose("Else block - & removing the shipping charges that copied from SO");
						Node ncurrentCharge = nChargesList.item(i);
						Node parentNode = ncurrentCharge.getParentNode();
						parentNode.removeChild(ncurrentCharge);

						// }

					}
				}
				// Fix for bug #4952
				if (((this.clsRemoveShippingCharges.equalsIgnoreCase("Y")) || (!bIsAcademyAtFault))
						&& (!(isWhiteGloveReturns.equalsIgnoreCase("Y")))) {
					// removing the Shipping Charges if its is at Customer fault
					// (return Reason Code) -- bug#9902
					if (eleCurrCharge.getAttribute(
							AcademyConstants.ATTR_CHARGE_NAME)
							.equalsIgnoreCase(
									AcademyConstants.STR_SHIPPING_CHARGE)
							&& (!bIsAcademyAtFault)) {
						Node ncurrentCharge = nChargesList.item(i);
						Node parentNode = ncurrentCharge.getParentNode();
						parentNode.removeChild(ncurrentCharge);

					}
					// removing Shipping Promotion along with shipping Charges
					// as its a Customer fault.
					/*
					 * if (eleCurrCharge.getAttribute(
					 * AcademyConstants.ATTR_CHARGE_NAME)
					 * .equalsIgnoreCase("ShippingPromotion") &&
					 * (!bIsAcademyAtFault)) { Node ncurrentCharge =
					 * nChargesList.item(i); Node parentNode =
					 * ncurrentCharge.getParentNode();
					 * parentNode.removeChild(ncurrentCharge); }
					 */

				}
				if (!(eleCurrCharge
						.getAttribute(AcademyConstants.ATTR_CHARGE_NAME)
						.equalsIgnoreCase(AcademyConstants.STR_RETURNSHIPPING_CHARGE))) {

					if (isWhiteGloveReturns.equalsIgnoreCase("Y")
							&& eleCurrCharge
									.getAttribute(
											AcademyConstants.ATTR_CHARGE_NAME)
									.equalsIgnoreCase(
											AcademyConstants.STR_SHIPPING_CHARGE)) {
						log
								.verbose("do nothing -as its WG returns shipping charges -no need to pro-rate or remove");
						// do nothing
					} else {
						eleCurrCharge
								.removeAttribute(AcademyConstants.ATTR_CHRG_NAME_KEY);
						/*
						 * Start Fix 8776
						 */
						eleCurrCharge
								.removeAttribute(AcademyConstants.ATTR_CHARGES_PER_LINE);
						eleCurrCharge
								.removeAttribute(AcademyConstants.ATTR_CHARGES_PER_UNIT);
						eleCurrCharge
								.removeAttribute(AcademyConstants.ATTR_REM_CHARGES_PER_LINE);
						eleCurrCharge
								.removeAttribute(AcademyConstants.ATTR_REM_CHARGES_PER_UNIT);
						eleCurrCharge
								.removeAttribute(AcademyConstants.ATTR_REM_CHARGE_AMOUNT);
						eleCurrCharge.removeAttribute("InvoicedChargePerUnit");
						/*
						 * End Fix 8776 Start Fix 8912
						 */
						eleCurrCharge
								.removeAttribute(AcademyConstants.ATTR_CHARGES_INVOICED_CHARGE_AMOUNT);
						eleCurrCharge
								.removeAttribute(AcademyConstants.ATTR_CHARGES_INVOICED_CHARGE_PER_LINE);
						// End Fix 8912

						// copying all the charges to return Order if its a
						// AcademyFault reason code.
						strLineChargeValue = eleCurrCharge
								.getAttribute(AcademyConstants.ATTR_CHARGE_AMT);
						if (strLineChargeValue == null)
							strLineChargeValue = "0";
						float fNewChargeValue = ((new Float(strLineChargeValue)
								.floatValue()) / (new Float(strOrigOrderLineQty)
								.floatValue()))
								* (new Float(strReturnedQty).floatValue());
						String strNewCharge = String.valueOf(fNewChargeValue);
						// 
						if (!reqRoundedVal) {
							strNewCharge = truncateDecimal(strNewCharge,
									AcademyConstants.INT_NUMBER_OF_DECIMALS);
							log
									.verbose("After rounded off the prorated shipping charge is : "
											+ strNewCharge);
						} else if (new Float(strLineChargeValue).floatValue() != 0) {
							float prevReturnedCharges = 0;
							log.verbose("ChargeCategory : "
									+ eleCurrCharge
											.getAttribute("ChargeCategory")
									+ " ::: ChargeName : "
									+ eleCurrCharge.getAttribute("ChargeName"));
							if (docOrderWithPrevRODetails != null) {
								NodeList lstROLineCharges = XPathUtil
										.getNodeList(
												docOrderWithPrevRODetails
														.getDocumentElement(),
												"ReturnOrders/ReturnOrder/OrderLines/OrderLine[@DerivedFromOrderLineKey='"
														+ parentOLK
														+ "']/LineCharges/LineCharge[@ChargeCategory='"
														+ eleCurrCharge
																.getAttribute("ChargeCategory")
														+ "' and @ChargeName='"
														+ eleCurrCharge
																.getAttribute("ChargeName")
														+ "']");
								log.verbose(" total line charges : "
										+ lstROLineCharges.getLength());
								for (int lcIndx = 0; lcIndx < lstROLineCharges
										.getLength(); lcIndx++) {
									Element prevLineCharge = (Element) lstROLineCharges
											.item(lcIndx);
									prevReturnedCharges += Float
											.parseFloat(prevLineCharge
													.getAttribute(AcademyConstants.ATTR_CHARGE_AMT));
								}
							}
							NodeList lstProcRtnLineCharges = XPathUtil
									.getNodeList(
											inDoc.getDocumentElement(),
											"OrderLines/OrderLine[@DerivedFromOrderLineKey='"
													+ parentOLK
													+ "' and @OrderLineKey!='"
													+ currReturnOrderLine
															.getAttribute("OrderLineKey")
													+ "']/LineCharges/LineCharge[@ChargeCategory='"
													+ eleCurrCharge
															.getAttribute("ChargeCategory")
													+ "' and @ChargeName='"
													+ eleCurrCharge
															.getAttribute("ChargeName")
													+ "']");
							log
									.verbose("total line charges from current processing RO other than current line "
											+ lstProcRtnLineCharges.getLength());
							for (int lc = 0; lc < lstProcRtnLineCharges
									.getLength(); lc++) {
								Element procRtnCharge = (Element) lstProcRtnLineCharges
										.item(lc);
								prevReturnedCharges += Float
										.parseFloat(procRtnCharge
												.getAttribute(AcademyConstants.ATTR_CHARGE_AMT));
							}
							log.verbose(new Float(strLineChargeValue)
									.floatValue()
									+ " - " + prevReturnedCharges);
							float remainCharges = new Float(strLineChargeValue)
									.floatValue()
									- prevReturnedCharges;
							log.verbose(" Remain Charges is :: "
									+ remainCharges);
							float delta = remainCharges - fNewChargeValue;
							if (YFCDoubleUtils.roundOff(delta) > 0.09) {
								strNewCharge = truncateDecimal(String
										.valueOf(fNewChargeValue),
										AcademyConstants.INT_NUMBER_OF_DECIMALS);
							} else {
								strNewCharge = String.valueOf(remainCharges);
							}
						}
						eleCurrCharge.setAttribute(
								AcademyConstants.ATTR_CHARGE_AMT, strNewCharge);

						eleCurrCharge.setAttribute(
								AcademyConstants.ATTR_CHARGES_PER_LINE,
								strNewCharge);
						log.verbose("eleCurrCharge -> "
								+ XMLUtil.getElementXMLString(eleCurrCharge));

					}
				}
			}
			env.setTxnObject("CLSRemoveShippingCharges", "");
			env.setTxnObject("ReturnReasonCode", "");

			log
					.endTimer(" End of AcademyReturnOrderPricingUEImpl - processOrderLineTaxesChargesAndPromotions() Api");
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}
	}

	/*
	 * private void raiseAlertForCSR(Document inDoc, YFSEnvironment env) {
	 * 
	 * Element eleInboxRefList = null; Element eleInboxRef = null; String
	 * strExceptionValue = null; Document docExceptionInput = null; Document
	 * docCreateExceptionAPIOutput = null; try {
	 * 
	 * docExceptionInput = XMLUtil .createDocument(AcademyConstants.ELE_INBOX);
	 * docExceptionInput.getDocumentElement()
	 * .setAttribute(AcademyConstants.ATTR_ACTIVE_FLAG,
	 * AcademyConstants.STR_YES);
	 * docExceptionInput.getDocumentElement().setAttribute(
	 * AcademyConstants.ATTR_EXCPTN_TYPE,
	 * AcademyConstants.STR_MISSING_REFUNDTENDER_EXCEPTION);
	 * docExceptionInput.getDocumentElement().setAttribute(
	 * AcademyConstants.STR_ORDR_HDR_KEY,
	 * inDoc.getDocumentElement().getAttribute(
	 * AcademyConstants.STR_ORDR_HDR_KEY));
	 * docExceptionInput.getDocumentElement().setAttribute(
	 * AcademyConstants.ATTR_ORDER_NO, inDoc.getDocumentElement().getAttribute(
	 * AcademyConstants.ATTR_ORDER_NO)); eleInboxRefList = docExceptionInput
	 * .createElement(AcademyConstants.ELE_INBOX_REF_LIST);
	 * XMLUtil.appendChild(docExceptionInput.getDocumentElement(),
	 * eleInboxRefList); eleInboxRef = docExceptionInput
	 * .createElement(AcademyConstants.ELE_INBOX_REFERENCES);
	 * XMLUtil.appendChild(eleInboxRefList, eleInboxRef);
	 * eleInboxRef.setAttribute(AcademyConstants.ATTR_INBOX_REFKEY, inDoc
	 * .getDocumentElement().getAttribute( AcademyConstants.STR_ORDR_HDR_KEY));
	 * eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE,
	 * AcademyConstants.STR_EXCPTN_REF_VALUE);
	 * eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME,
	 * AcademyConstants.STR_MISSING_REFUNDTENDER); //
	 * eleInboxRef.setAttribute("Value","Order Line Fulfillment");
	 * strExceptionValue = "Refund Tender Missing for Return Order No : " +
	 * inDoc.getDocumentElement().getAttribute( AcademyConstants.ATTR_ORDER_NO);
	 * eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE, strExceptionValue);
	 * 
	 * docCreateExceptionAPIOutput = AcademyUtil.invokeAPI(env,
	 * AcademyConstants.API_CREATE_EXCEPTION, docExceptionInput); } catch
	 * (Exception e) { e.printStackTrace(); throw new
	 * YFSException(e.getMessage()); } }
	 */
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