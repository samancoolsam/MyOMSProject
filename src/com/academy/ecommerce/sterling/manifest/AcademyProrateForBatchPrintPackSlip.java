package com.academy.ecommerce.sterling.manifest;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyProrateForBatchPrintPackSlip implements YIFCustomApi {

	private Properties props;
	private Element eleContainer = null;
	private Element eleShipments = null;
	private String strInvoiceNo = null;
	private Element eleInvoiceDetail = null;
	private Element eleShipment = null;
	private String strProNumber = null;

	public void setProperties(Properties props) {
		this.props = props;
	}

	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyProrateForBatchPrintPackSlip.class);

	public Document prorateForBatchPrintPackSlip(YFSEnvironment env,
			Document inDoc) throws YFCException {
		log
				.beginTimer(" Begining of AcademyProrateForBatchPrintPackSlip-> prorateForBatchPrintPackSlip Api");
		
		Document docShipments = null;
		try {
			log
					.verbose("******************* inDoc for prorateForBatchPrintPackSlip ***************** "
							+ XMLUtil.getXMLString(inDoc));
			// Fetch the matching Shipment Details and create in the below format
			/**
			 * <Shipments>
			 * 	<Shipment>
			 * 		<ShipmentLines>
			 * 			<ShipmentLine/>
			 * 		</ShipmentLines>
			 * 		<Instructions/>
			 * 		<OrderInvoiceList/>
			 * 	</Shipment>
			 * </Shipments>
			 */
			String curShipmentKey = XPathUtil.getString(inDoc.getDocumentElement(), "InputDocument/Shipment/@ShipmentKey");
			Element eleMatchedShipment = (Element)XPathUtil.getNode(inDoc.getDocumentElement(), "EnvironmentDocument/BatchList/ShipmentList/Shipment[@ShipmentKey='"+curShipmentKey+"']");
			
			//Fix For STL-398//
			
			Element eleOverallTotals = (Element)XPathUtil.getNode(inDoc.getDocumentElement(), "InputDocument/Shipment/ShipmentLines/ShipmentLine/OrderLine/Order/OverallTotals");
			
			//Fix For STL-398//
			
			docShipments = XMLUtil.createDocument("Shipments");
			Element eleShipment = docShipments.createElement("Shipment");
			XMLUtil.copyElement(docShipments, eleMatchedShipment, eleShipment);
			docShipments.getDocumentElement().appendChild(eleShipment);
			// Continue the Pro-rate Logic
			eleShipments = docShipments.getDocumentElement();
			eleShipment = (Element) eleShipments.getElementsByTagName(
					"Shipment").item(0);
			String strExtnInvoiceNo = null;
			if (!YFCObject.isVoid(eleShipment)) {
				Element eleOrderInvoiceList = (Element) eleShipment
						.getElementsByTagName("OrderInvoiceList").item(0);
				eleInvoiceDetail = (Element) eleOrderInvoiceList
						.getElementsByTagName("OrderInvoice").item(0);
				log.verbose("******Invoice Detail Element is*****"
						+ XMLUtil.getElementXMLString(eleInvoiceDetail));
				// By default stamp ACAD_PACKSLIP_CONV if gift instruction is
				// there
				// change it to ACAD_PACKSLIP_CONV_GIFT
				if (!YFCObject.isVoid(eleShipment)) {
					eleShipment.setAttribute("PrintDocumentId",
							"ACAD_BATCH_PRINT_PACKSLIP_CONV");
					String strPrinterId = props.getProperty("PRINTER_ID");
					eleShipment.setAttribute("PrinterId", strPrinterId);
				}
				// Call stamp marketing lines method to get marketing lines from
				// common code.
				AcademyStampFileLocation ac = new AcademyStampFileLocation();
				ac.stampMarketingLines(env, eleShipment);

				// Check For Gift Instruction
				checkGiftInstruction(env, eleShipment);

				// Extract the invoice number from Order Invoice table
				strExtnInvoiceNo = XMLUtil.getString(eleInvoiceDetail,
						"Extn/@ExtnInvoiceNo");

				log
						.verbose("********* Invoice number @ OrderInvoice is **********"
								+ strExtnInvoiceNo);
				eleShipment.setAttribute("ExtnInvoiceNo", strExtnInvoiceNo);
				String strOrderInvoiceKey = eleInvoiceDetail.getAttribute("OrderInvoiceKey");
				log
				.verbose("********* Pro forma order Invoice Key is **********"
						+ strOrderInvoiceKey);
				eleShipment.setAttribute("OrderInvoiceKey", strOrderInvoiceKey);
				// this method checks if the releaseWave/print is done on same
				// day or not.
				// For same date, use the same invoice number.For different
				// date, generate new invoice number
				// generateInvoiceNo = createInvoiceNumber(strExtnInvoiceNo);

				/***************************************************************
				 * the below logic will check if there is an invoice number
				 * existing. if yes, it will check if the invoice is created on
				 * the same day. if not then a new invoice # will be generated.
				 * Else if there is no invoice number, it will create a new
				 * invoice number.
				 **************************************************************/
				// Change for CR - Reprint PackSlip Flow
				// If the API is invoking for reprint then don't call
				// GetInvoiceNoUE
				String isReprintFlow = "N";
				if (eleShipment.hasAttribute("ReprintPackSlip"))
					isReprintFlow = eleShipment.getAttribute("ReprintPackSlip");
				log.verbose("isReprintFlow : " + isReprintFlow
						+ " actual InvoiceNo is : " + strExtnInvoiceNo);
				if (isReprintFlow.equals("Y")) {
					strInvoiceNo = strExtnInvoiceNo;
					if (eleShipment.hasAttribute("ReprintPrinterId")) {
						String strPrinterId = eleShipment.getAttribute("ReprintPrinterId");
						eleShipment.setAttribute("PrinterId", strPrinterId);
					}
				} else {
					/**
					 * Academy Bug # 4775: As per discussion & mail thread from suresh,
					 * Should not generate Invoice # on Wave Print and 
					 * not print Return Label until Pack completed.
					 * 
					 *  Therefore, removing the logic to generate Invoice # so that Academy won't lose transaction #  
					 *   
					 */
					/*if (YFCObject.isNull(strExtnInvoiceNo)) {
						log.verbose("**** Invoice Number is not existing on the PRO_FORMA, hence call Invoice UE to generate InvoiceNo UE****");
						callGetInvoiceNoUE(env, eleShipment);
					} else {
						log.verbose("****Invoice No exists on the PRO_FORMA, so evaluate if invoice # needs to be regenarated");
						Document docShipment = XMLUtil
								.createDocument("Shipment");
						XMLUtil.copyElement(docShipment, eleShipment,
								docShipment.getDocumentElement());
						log.verbose("****Shipment document is *****"
								+ XMLUtil.getXMLString(docShipment));
						Document docInvoiceNo = AcademyUtil.invokeService(env,
								"AcademyValidateInvoiceNumberService",
								docShipment);
						if (!YFCObject.isVoid(docInvoiceNo)) {
							strInvoiceNo = docInvoiceNo.getDocumentElement()
									.getAttribute("InvoiceNo");
							if ("Y".equals(docInvoiceNo.getDocumentElement().getAttribute(
							"RegeneratedInvoiceNumber"))) {
						log
								.verbose("**** Invoice Number is regenerated. New Invoice Number is******"
										+ strInvoiceNo);
							} else {
						log
								.verbose("**** Invoice Number is not regenerated. Using the same Invoice Number is******"
										+ strInvoiceNo);
					}
						}
					}*/ 
						 
				}
				calculateDataForPackSlip(env, eleShipment, eleOverallTotals);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			log.verbose(ex);
			throw new YFCException(ex);
		}
		log
				.endTimer(" End of AcademyProrateForBatchPrintPackSlip-> prorateForBatchPrintPackSlip Api");

		return docShipments;
	}


	private void callGetInvoiceNoUE(YFSEnvironment env, Element eleShipment)
			throws ParserConfigurationException, Exception {
		log
				.beginTimer(" Begining of AcademyProrateForPackSlip-> callGetInvoiceNoUE Api");

		Document docInvoiceDetail = XMLUtil.createDocument("InvoiceDetail");
		Element eleInvoiceHeader = XMLUtil.createElement(docInvoiceDetail,
				"InvoiceHeader", null);
		docInvoiceDetail.getDocumentElement().appendChild(eleInvoiceHeader);

		Element eleShipment1 = XMLUtil.createElement(docInvoiceDetail,
				"Shipment", null);
		eleInvoiceHeader.appendChild(eleShipment1);
		eleShipment1.setAttribute("ShipmentKey", eleShipment
				.getAttribute("ShipmentKey"));
		eleShipment1.setAttribute("ShipDate", eleShipment
				.getAttribute("ShipDate"));
		eleShipment1.setAttribute("ShipNode", eleShipment
				.getAttribute("ShipNode"));
		eleShipment1.setAttribute("SCAC", eleShipment.getAttribute("SCAC"));
		eleShipment1.setAttribute("CallInvoiceUEFromPrintFLow", "Y");

		Document outDoc = AcademyUtil.invokeService(env,
				"AcademyInvokeGetInvoiceNo", docInvoiceDetail);
		log.verbose("********** output doc of AcademyInvokeGetInvoiceNo : "
				+ XMLUtil.getXMLString(outDoc));

		strInvoiceNo = outDoc.getDocumentElement().getAttribute("InvoiceNo");
		log.verbose("********** invoice number : " + strInvoiceNo);
		changeOrderInvoiceToUpdateExtnInvoiceNo(env);
		log
				.endTimer(" End of AcademyProrateForPackSlip-> callGetInvoiceNoUE Api");
	}

	private void changeOrderInvoiceToUpdateExtnInvoiceNo(YFSEnvironment env)
			throws ParserConfigurationException, Exception {
		log.verbose("***** Invoice Details Element for PRO_FORMA is*****"
				+ XMLUtil.getElementXMLString(eleInvoiceDetail));
		String strGetProformaOrderInvoiceKey = eleInvoiceDetail
				.getAttribute("OrderInvoiceKey");
		Document docInput = XMLUtil.createDocument("OrderInvoice");
		docInput.getDocumentElement().setAttribute("OrderInvoiceKey",
				strGetProformaOrderInvoiceKey);
		Element eleExtn = XMLUtil.createElement(docInput, "Extn", null);
		docInput.getDocumentElement().appendChild(eleExtn);
		eleExtn.setAttribute("ExtnInvoiceNo", strInvoiceNo);
		env.setApiTemplate("changeOrderInvoice", "global/template/api/changeOrderInvoice.PrintFlow.xml");
		Document outdoc = AcademyUtil.invokeAPI(env, "changeOrderInvoice",
				docInput);
		env.clearApiTemplate("changeOrderInvoice");
		log.verbose("*********** output of change order Invoice api : "
				+ XMLUtil.getXMLString(outdoc));
	}

	public void calculateDataForPackSlip(YFSEnvironment env, Element eleShipment, Element eleOverallTotals)
			throws Exception {
		log
				.beginTimer(" Begining of AcademyProrateForBatchPrintPackSlip-> calculateDataForPackSlip Api");
		Element eleShipmentLine = null;
		Element eleOrderLine = null;
		Element LineCharges = null;
		Element LineTaxList = null;
		Element LineCharge = null;
		double totalShippingCharge = 0.00;
		double totalTax = 0.00;
		String strUnitPrice = "0.00";
		String strExtendedPrice = "0.00";
		String strOrderedPricingQty = "0.00";
		double ShippingChargeForLine = 0.00;
		double tax = 0.00;
		String strShipmentLineQty = "0.00";
		double totalDiscount = 0.00;
		double extendedPriceTotal = 0.00;
		double containerExtendedPrice = 0.00;

		Element eleInvoiceheader = (Element) XMLUtil.getNode(eleShipment,
				"OrderInvoiceList/OrderInvoice");
		/*
		 * Element eleContainerDetails =
		 * XMLUtil.getFirstElementByName(inDocElem,
		 * "EnvironmentDocument/Container/ContainerDetails");
		 */
		Element eleInvoiceLineDetails = XMLUtil.getFirstElementByName(
				eleInvoiceheader, "LineDetails");

		DecimalFormat decimalCorrection = new DecimalFormat("0.00");
		setDataInPackSlipFormat(env, eleShipment);
		Element eleShipmentLines = (Element) eleShipment.getElementsByTagName(
				"ShipmentLines").item(0);
		if (!YFCObject.isVoid(eleShipmentLines)) {
			NodeList nShipmentLine = XMLUtil.getNodeList(eleShipmentLines,
					"ShipmentLine");
			if (!YFCObject.isVoid(nShipmentLine)) {
				int iLength = nShipmentLine.getLength();
				for (int i = 0; i < iLength; i++) {
					eleShipmentLine = (Element) nShipmentLine.item(i);
					if (!YFCObject.isVoid(eleShipmentLine)) {
						strShipmentLineQty = eleShipmentLine
								.getAttribute("Quantity");
						double dShipmentLineQty = Double
								.parseDouble(strShipmentLineQty);
						int iShipmentLineQty = (int) dShipmentLineQty;
						eleShipmentLine.setAttribute("Quantity", Integer
								.toString(iShipmentLineQty));
						double shippingChargeAtLineLevel = 0.00;
						double discountForLine = 0.00;
						setStaticFieldsOnConvPackSlip(env, eleShipmentLine);
						if (!YFCObject.isVoid(eleShipmentLine)) {
							eleOrderLine = XMLUtil.getFirstElementByName(
									eleShipmentLine, "OrderLine");
							if (!YFCObject.isVoid(eleOrderLine)) {
								eleOrderLine.setAttribute("USD", "$");
								String strOrderLineKey = eleOrderLine
										.getAttribute("OrderLineKey");
								Element eleOrder = XMLUtil
										.getFirstElementByName(eleOrderLine,
												"Order");
								Element elePaymentMethods = XMLUtil
										.getFirstElementByName(eleOrder,
												"PaymentMethods");
									NodeList nPaymentMethod = XMLUtil.getNodeList(
										elePaymentMethods, "PaymentMethod");
								if (!YFCObject.isVoid(nPaymentMethod)) {
									for (int j = 0; j < nPaymentMethod
											.getLength(); j++) {
										Element elePaymentMethod = (Element) nPaymentMethod
												.item(j);
										String strDisplayCreditCardNo = elePaymentMethod
												.getAttribute("DisplayCreditCardNo");
										String strCreditCardType = elePaymentMethod
												.getAttribute("CreditCardType");
										String strSvcNo = elePaymentMethod
												.getAttribute("DisplaySvcNo");
										elePaymentMethod.setAttribute(
												"PaymentMethodCurrency", "$");
										String strPaymentType = elePaymentMethod
												.getAttribute("PaymentType");

										if (!YFCObject.isNull(strPaymentType)) {
											//KER-12036 : Payment Migration Changes to support new Payment Type
											//OMNI-31625: Tender type for PLCC, ApplePay, GooglePay - START
											if (AcademyConstants.PAYPAL.equalsIgnoreCase(strPaymentType)) {
												strCreditCardType=strPaymentType;
											}
											log.verbose("AcademyProrateForBatchPrintPackSlip-calculateDataForPackSlip - strPaymentType :: "+strPaymentType);
											log.verbose("AcademyProrateForBatchPrintPackSlip-calculateDataForPackSlip - strCreditCardType :: "+strCreditCardType);
											String strCommonCodeCCType = getCommonCodeForCrediCardType(env, strCreditCardType);
											log.verbose("getCommonCodeForCrediCardType-calculateDataForPackSlip - strCommonCodeCCType :: "+strCommonCodeCCType);
											/* if ("CREDIT_CARD".equalsIgnoreCase(strPaymentType)){ */											
											if(AcademyConstants.CREDIT_CARD.equalsIgnoreCase(strPaymentType)
												|| AcademyConstants.STR_APPLE_PAY_PAYMENT.equalsIgnoreCase(strPaymentType) 
												|| AcademyConstants.STR_GOOGLE_PAY_PAYMENT.equalsIgnoreCase(strPaymentType)
												|| AcademyConstants.STR_PLCC_PAYMENT.equalsIgnoreCase(strPaymentType)													
												) {																								
												//OMNI-31625: Tender type for PLCC, ApplePay, GooglePay - END
												elePaymentMethod.setAttribute("DisplayCreditCardNo", "("+ strDisplayCreditCardNo+ ")");
												//elePaymentMethod.setAttribute("CreditCardType", strCreditCardType);
												elePaymentMethod.setAttribute("CreditCardType", strCommonCodeCCType);
												elePaymentMethod.setAttribute("AmountCharged", eleOverallTotals.getAttribute("GrandTotal"));
											} 
											// Start :: PayPal Integration
											else if (AcademyConstants.PAYPAL.equalsIgnoreCase(strPaymentType)) {//KER-11461 change equals to equalsIgnoreCase.  to support new Paypal payment type
												log.verbose("Inside PayPal");
												elePaymentMethod.setAttribute("DisplayCreditCardNo", "");
												//Hardcoding the creditCardType to PayPal
												/*elePaymentMethod.setAttribute("CreditCardType", strCreditCardType);*/
												//OMNI-31625: Start
												//elePaymentMethod.setAttribute("CreditCardType", AcademyConstants.PAYPAL);
												//elePaymentMethod.setAttribute("CreditCardType", AcademyConstants.PACK_SLIP_PAYPAL);
												elePaymentMethod.setAttribute("CreditCardType", strCommonCodeCCType);
												//OMNI-31625: Start
												elePaymentMethod.setAttribute("AmountCharged", eleOverallTotals.getAttribute("GrandTotal"));
												log.verbose("End PayPal");
											}
											// End :: PayPal Integration
											else {
												elePaymentMethod.setAttribute("DisplayCreditCardNo", "(" + strSvcNo + ")");
												//OMNI-31625: Start
												//elePaymentMethod.setAttribute("CreditCardType", "GC");
												elePaymentMethod.setAttribute("CreditCardType", strCommonCodeCCType);
												//OMNI-31625: Start
												elePaymentMethod.setAttribute("AmountCharged", eleOverallTotals.getAttribute("GrandTotal"));
											}
										}
									}
								}

								NodeList iLineDetail = XMLUtil.getNodeList(
										eleInvoiceLineDetails, "LineDetail");
								if (!YFCObject.isVoid(iLineDetail)) {
									for (int j = 0; j < iLineDetail.getLength(); j++) {

										Element iLineDetailEle = (Element) iLineDetail
												.item(j);
										LineCharges = XMLUtil
												.getFirstElementByName(
														iLineDetailEle,
														"OrderLine/LineCharges");
										LineTaxList = XMLUtil
												.getFirstElementByName(
														iLineDetailEle,
														"LineTaxList");
										String strOrderlineKey = iLineDetailEle
												.getAttribute("OrderLineKey");
										if (strOrderLineKey
												.equals(strOrderlineKey)) {
											double shippingTaxAtLineLevel = 0.00;
											double itemTaxAtLineLevel = 0.00;
											String itemDiscType = "";

											strUnitPrice = ((Element) iLineDetail
													.item(j))
													.getAttribute("UnitPrice");
											strExtendedPrice = ((Element) iLineDetail
													.item(j))
													.getAttribute("ExtendedPrice");
											containerExtendedPrice = Double
													.valueOf(strUnitPrice)
													* Double
															.valueOf(strShipmentLineQty);
											extendedPriceTotal += containerExtendedPrice;
											strOrderedPricingQty = ((Element) iLineDetail
													.item(j))
													.getAttribute("ShippedQty");
											eleOrderLine.setAttribute(
													"UnitPrice", "$"
															+ strUnitPrice);
											eleOrderLine
													.setAttribute(
															"ExtendedPrice",
															"$"
																	+ decimalCorrection
																			.format(containerExtendedPrice));
											if (!YFCObject.isVoid(LineCharges)) {
												NodeList iLineCharge = XMLUtil
														.getNodeList(
																LineCharges,
																"LineCharge");
												for (int k = 0; k < iLineCharge
														.getLength(); k++) {
													LineCharge = (Element) iLineCharge
															.item(k);
													String isDiscount = LineCharge
															.getAttribute("IsDiscount");
													String chargeAmount = LineCharge
															.getAttribute("ChargeAmount");
													String chargeName = LineCharge
															.getAttribute("ChargeName");

													if (LineCharge
															.getAttribute(
																	"ChargeName")
															.contains(
																	"Shipping")) {
														String strShippingChargeAtLineLevel = LineCharge
																.getAttribute("ChargeAmount");
														if ("Y"
																.equals(LineCharge
																		.getAttribute("IsDiscount"))) {
															shippingChargeAtLineLevel = shippingChargeAtLineLevel
																	- Double
																			.parseDouble(strShippingChargeAtLineLevel);
														} else {
															shippingChargeAtLineLevel = shippingChargeAtLineLevel
																	+ Double
																			.parseDouble(strShippingChargeAtLineLevel);
														}
													}
													if ("BOGO"
															.equals(chargeName)
															|| ("DiscountCoupon"
																	.equals(chargeName))) {
														if ("Y"
																.equals(isDiscount)) {
															discountForLine = discountForLine
																	+ Double
																			.parseDouble(chargeAmount);
															itemDiscType = getItemDiscTypeNames(
																	chargeName,
																	itemDiscType,
																	iLineCharge,
																	k);
														}
													}
												}

												discountForLine = (discountForLine / Double
														.parseDouble(strOrderedPricingQty))
														* Double
																.parseDouble(strShipmentLineQty);
												shippingChargeAtLineLevel = (shippingChargeAtLineLevel / Double
														.parseDouble(strOrderedPricingQty))
														* Double
																.parseDouble(strShipmentLineQty);
												totalDiscount += discountForLine;

												String disForLine = decimalCorrection
														.format(discountForLine);
												eleOrderLine
														.setAttribute(
																"ShippingChargeAtLineLevel",
																"$"
																		+ decimalCorrection
																				.format(shippingChargeAtLineLevel));
												eleOrderLine.setAttribute(
														"DiscountForLine", "($"
																+ disForLine
																+ ")");// changed

												if (itemDiscType.endsWith("/")) {
													itemDiscType = itemDiscType
															.substring(
																	0,
																	(itemDiscType
																			.length() - 1));
												}

												eleOrderLine.setAttribute(
														"ItemDiscDesc",
														itemDiscType);

												if (!YFCObject
														.isVoid(LineTaxList)) {
													NodeList nLineTax = XMLUtil
															.getNodeList(
																	LineTaxList,
																	"LineTax");

													for (int k = 0; k < nLineTax
															.getLength(); k++) {
														Element eleLineTax = (Element) nLineTax
																.item(k);
														String chargeCategory = eleLineTax
																.getAttribute("ChargeCategory");
														if ("Shipping"
																.equals(chargeCategory)) {
															String strShippingTaxAtLineLevel = eleLineTax
																	.getAttribute("Tax");
															shippingTaxAtLineLevel += Double
																	.parseDouble(strShippingTaxAtLineLevel);
														}
														if ("TAXES"
																.equals(chargeCategory)) {
															String strItemTaxAtLineLevel = eleLineTax
																	.getAttribute("Tax");
															itemTaxAtLineLevel += Double
																	.parseDouble(strItemTaxAtLineLevel);
														}
													}
												}

											}
											itemTaxAtLineLevel = (itemTaxAtLineLevel / Double
													.parseDouble(strOrderedPricingQty))
													* Double
															.parseDouble(strShipmentLineQty);
											shippingTaxAtLineLevel = (shippingTaxAtLineLevel / Double
													.parseDouble(strOrderedPricingQty))
													* Double
															.parseDouble(strShipmentLineQty);
											// Fix for Issue #3678, corrected
											// the proration logic
											tax = tax + shippingTaxAtLineLevel
													+ itemTaxAtLineLevel;

											eleOrderLine
													.setAttribute(
															"ShippingTaxLineLevel",
															"$"
																	+ decimalCorrection
																			.format(shippingTaxAtLineLevel));
											eleOrderLine
													.setAttribute(
															"ItemTaxLineLevel",
															"$"
																	+ decimalCorrection
																			.format(itemTaxAtLineLevel));
										}
									}
								}
							}
						}
						totalTax = +tax;
						log.verbose("Total Tax is " + totalTax);
						totalShippingCharge = totalShippingCharge
								+ shippingChargeAtLineLevel;
					}
				}
			}
			eleShipment.setAttribute("TotalExtendedPrice", "$"
					+ decimalCorrection.format(extendedPriceTotal));
			eleShipment.setAttribute("TotalTax", "$"
					+ decimalCorrection.format(tax));
			eleShipment.setAttribute("ShippingCharges", "$"
					+ decimalCorrection.format(totalShippingCharge));
			eleShipment.setAttribute("TotalDiscount", "($"
					+ decimalCorrection.format(totalDiscount) + ")");
			double cartonTotal = extendedPriceTotal + tax + totalShippingCharge
					- totalDiscount;
			eleShipment.setAttribute("CartonTotal", "$"
					+ decimalCorrection.format(cartonTotal));

			//eleShipment.setAttribute("InvoiceNo", strInvoiceNo);
		}
		movePaymentMethodsNodeOutside(eleShipment);

		log
				.endTimer(" End of AcademyProrateForPackSlip-> calculateDataForPackSlip Api");
	}
	
	
	
	
	//OMNI-31625: Tender type for PLCC, ApplePay, GooglePay - START
	/**
	 * This method return the common code short description value.
	 * @param env
	 * @param strCreditCardType
	 * @return
	 */
	/**inDoc:
	 * <CommonCode CodeType="PACK_SLIP_PAY_TYPE" CodeValue="GIFT_CARD"/>
	 * 
		outDoc:
		<CommonCodeList>
			<CommonCode CodeLongDescription="GC" CodeShortDescription="GC" CodeType="PACK_SLIP_PAY_TYPE" CodeValue="GIFT_CARD"/>
		</CommonCodeList>
	 *
	 */
	private String getCommonCodeForCrediCardType(YFSEnvironment env, String strCreditCardType) {
		// TODO Auto-generated method stub
		log.verbose( "AcademyProrateForBatchPrintPackSlip-getCommonCodeForCrediCardType :: "+  strCreditCardType);
		Document inDocGetCommonCodeList = null;
		Document outDocGetCommonCodeList = null;
		String strCommonCodeCCType = "";		
		try {			
			inDocGetCommonCodeList = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
			inDocGetCommonCodeList.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, AcademyConstants.STR_PACK_SLIP_PAY_TYPE);
			//inDocGetCommonCodeList.getDocumentElement().setAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE, strCreditCardType.replaceAll(" ", "").toUpperCase());
			inDocGetCommonCodeList.getDocumentElement().setAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE, strCreditCardType);
			//Invoking getCommonCodeList API to fetch the CommonCodeCreditCardType for PACK_SLIP_PAY_TYPE codeType.
			outDocGetCommonCodeList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMON_CODELIST, inDocGetCommonCodeList);
			log.verbose( "AcademyProrateForBatchPrintPackSlip-getCommonCodeForCrediCardType - outDocGetCommonCodeList :: "+  XMLUtil.getXMLString(outDocGetCommonCodeList));
			if(!YFCObject.isVoid(outDocGetCommonCodeList) && 
					outDocGetCommonCodeList.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_COMMON_CODE).getLength() > 0)
			{			
				Element eleCommonCode = XMLUtil.getElementByXPath(outDocGetCommonCodeList, AcademyConstants.XPATH_COMMON_CODE_ELE);
				strCommonCodeCCType = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
				log.verbose( "AcademyProrateForBatchPrintPackSlip-getCommonCodeForCrediCardType - strCommonCodeCCType :: "+  strCommonCodeCCType);
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return strCommonCodeCCType;
	}
	//OMNI-31625: Tender type for PLCC, ApplePay, GooglePay - End
		
		
		
	/**
	 * MOVE PaymentMethods node FROM
	 * Container/ContainerDetails/Container/ShipmentLine/OrderLine/Order/PaymentMethods
	 * TO Container/Shipment/PaymentMethods.
	 * 
	 * @param inDocElem
	 */
	private void movePaymentMethodsNodeOutside(Element eleShipment) {
		log
				.beginTimer(" End of AcademyProrateForPackSlip-> movePaymentMethodsNodeOutside Api");
		log
				.verbose("********* input to movePaymentMethodsNodeOutside *************");

		/*
		 * Element shipmentElement = XMLUtil.getFirstElementByName(inDocElem,
		 * "EnvironmentDocument/Container/Shipment");
		 */
		if (!YFCObject.isVoid(eleShipment)) {
			Element eleShipmentLines = XMLUtil.getFirstElementByName(
					eleShipment, "ShipmentLines");
			if (!YFCObject.isVoid(eleShipmentLines)) {
				try {
					NodeList nShipmentLine = XMLUtil.getNodeList(
							eleShipmentLines, "ShipmentLine");

					// COPY PaymentMethods node
					// FROM
					// Container/ContainerDetails/Container/ShipmentLine/OrderLine/Order/PaymentMethods
					// TO Container/Shipment/PaymentMethods.
					NodeList paymentMethodsNodeList = XMLUtil.getNodeList(
							nShipmentLine.item(0),
							"OrderLine/Order/PaymentMethods");
					if (paymentMethodsNodeList.getLength() > 0) {
						Node paymentMethods = paymentMethodsNodeList.item(0);
						log
								.verbose("********* payment method element - "
										+ XMLUtil
												.getElementXMLString((Element) paymentMethods));
						// added to Container/Shipment
						eleShipment.appendChild(paymentMethods);
					}
					int numberOfContainers = nShipmentLine.getLength();
					log.verbose("********* number of containers - "
							+ numberOfContainers);
					for (int i = 0; i < numberOfContainers; i++) {
						NodeList orderNodeList = XMLUtil.getNodeList(
								nShipmentLine.item(i), "OrderLine/Order");
						if (orderNodeList.getLength() > 0) {
							Element orderElement = (Element) orderNodeList
									.item(0);
							log
									.verbose("********* Order element - "
											+ XMLUtil
													.getElementXMLString(orderElement));
							Node paymentMethodsNode = orderElement
									.getElementsByTagName("PaymentMethods")
									.item(0);
							if (!YFCObject.isVoid(paymentMethodsNode)) {
								orderElement.removeChild(paymentMethodsNode);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		log.verbose("********* output xml - "
				+ XMLUtil.getElementXMLString(eleShipment));
		log
				.endTimer(" End of AcademyProrateForPackSlip-> movePaymentMethodsNodeOutside Api");
	}

	private String getShipDateFromInvoiceNo(YFSEnvironment env,
			String strInvoiceNo) {

		String strShipDate = "";
		if (!YFCObject.isNull(strInvoiceNo)) {
			if (strInvoiceNo.length() > 9) {
				String year = strInvoiceNo.substring(0, 4);
				String month = strInvoiceNo.substring(4, 6);
				String day = strInvoiceNo.substring(6, 8);

				strShipDate = month + "/" + day + "/" + year;
			}
		}
		return strShipDate;
	}

	private void setDataInPackSlipFormat(YFSEnvironment env, Element eleShipment) {
		// setting the shipment Quantity to integer value
		/*
		 * Element shipLineEle = (Element) eleContainerDetails
		 * .getElementsByTagName("ShipmentLine").item(0);
		 */

		// setting the SCAC and carrier code in PACK-Slip format
		log.verbose("******setDataInPackSlipFormat method -> Begin********");
		String SCACandCarrier = "";
		String scacVal = eleShipment.getAttribute("SCAC");
		String scacValue = getScacValueFromMapping(env, scacVal);
		String carrierVal = eleShipment.getAttribute("CarrierServiceCode");
		// STL-28 code fix//
		if (AcademyConstants.STR_PRIORITY_MAIL.equalsIgnoreCase(carrierVal)){
			 SCACandCarrier = "USPS";
		}else if (AcademyConstants.STR_FIRST_CLASS_MAIL.equalsIgnoreCase(carrierVal)){
			 SCACandCarrier = "USPS";
		}
		else{
			SCACandCarrier = scacValue + " - " + carrierVal;
		}
		//End of code fix for STL-28//		
		//String SCACandCarrier = scacValue + " - " + carrierVal;
		eleShipment.setAttribute("SCACAndCarrier", SCACandCarrier);
		Element eleShipmentLines = (Element) eleShipment.getElementsByTagName(
				"ShipmentLines").item(0);
		Element eleShipmentLine = (Element) eleShipmentLines
				.getElementsByTagName("ShipmentLine").item(0);
		Element eleOrderLine = (Element) eleShipmentLine.getElementsByTagName(
				"OrderLine").item(0);
		if (!YFCObject.isNull(eleOrderLine)) {
			Element eleOrder = (Element) eleOrderLine.getElementsByTagName(
					"Order").item(0);
			if (!YFCObject.isNull(eleOrder)) {
				eleShipment.setAttribute("OrderDate",
						getDateInPackSlipFormat(eleOrder
								.getAttribute("OrderDate")));
			}
		}
		String strShipmentKey = eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		log.verbose("Calling getShipCreateDateFromShipmentKey with shipment key : " + strShipmentKey);
		String shipCreateDate = getShipCreateDateFromShipmentKey(strShipmentKey);
		eleShipment.setAttribute("ShipDate", shipCreateDate);
		
		/*String shipDate = getShipDateFromInvoiceNo(env, strInvoiceNo);
		eleShipment.setAttribute("ShipDate", shipDate);*/

	}

	private String getShipCreateDateFromShipmentKey(String strShipmentKey){

		log.verbose("Inside getShipCreateDateFromShipmentKey with shipment key : " + strShipmentKey);
		String strShipDate = "";
		if (!YFCObject.isNull(strShipmentKey)) {
				String year = strShipmentKey.substring(0, 4);
				log.verbose(year);
				String month = strShipmentKey.substring(4, 6);
				log.verbose(month);
				String day = strShipmentKey.substring(6, 8);
				log.verbose(day);

				strShipDate = month + "/" + day + "/" + year;
				log.verbose("Calculated Ship Create Date is : " +strShipDate);
		}
		return strShipDate;
	}
	
	public String getScacValueFromMapping(YFSEnvironment env, String scacVal) {

		String displayScacVal = "";

		if ("UPSN".equals(scacVal)) {
			displayScacVal = "UPS";
		}
		if (scacVal.startsWith("USPS")) {
			displayScacVal = "USPS";
		}
		if (scacVal.startsWith("FEDX")) {
			displayScacVal = "FEDEX";
		}
		else {
			displayScacVal = scacVal;
		}
		return displayScacVal;
	}

	public static String getDateInPackSlipFormat(String value) {
		// 2010-06-08T00:00:00-00:00
		DateFormat dbDateTimeFormatter = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss");
		DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		try {
			return formatter.format(dbDateTimeFormatter.parse(value));
		} catch (ParseException e) {

		}
		return formatter.format(new Date());
	}

	private String getItemDiscTypeNames(String ItemDiscChargeName,
			String itemDiscType, NodeList iLineCharge, int k) {

		String itemDiscPrintName = "";
		if ("BOGO".equals(ItemDiscChargeName)) {
			itemDiscPrintName = "Buy More, Get More";
		}
		if ("DiscountCoupon".equals(ItemDiscChargeName)) {
			itemDiscPrintName = "Coupon";
		}
		if ("CUSTOMER_APPEASEMENT".equals(ItemDiscChargeName)) {
			itemDiscPrintName = "CS Adj.";
		}

		if (k < (iLineCharge.getLength() - 1)) {
			itemDiscType = itemDiscType + itemDiscPrintName + " /";
		} else {
			itemDiscType += itemDiscPrintName;
		}

		// if (itemDiscType.endsWith("/")) {
		// itemDiscType =
		// itemDiscType.substring(itemDiscType.length()-(itemDiscType.length()-1));
		// }
		return itemDiscType;
	}

	private void checkGiftInstruction(YFSEnvironment env, Element eleShipment)
			throws Exception {
		if (!YFCObject.isVoid(eleShipment)) {
			Element Instructions = XMLUtil.getFirstElementByName(eleShipment,
					"Instructions");
			if (!YFCObject.isVoid(Instructions)) {
				NodeList InstructionList = XMLUtil.getNodeList(Instructions,
						"Instruction");
				if (!YFCObject.isVoid(InstructionList)) {
					int iLen = InstructionList.getLength();
					for (int k = 0; k < iLen; k++) {
						Element Instruction = (Element) InstructionList.item(k);
						String InstuctionType = Instruction
								.getAttribute("InstructionType");
						if ("GIFT".equals(InstuctionType)) {
							eleShipment.setAttribute("IsGiftMessageShipment",
									"Y");
							eleShipment.setAttribute("PrintDocumentId",
									"ACAD_BATCH_PRINT_PACKSLIP_CONV_GIFT");
						}
					}
				}
			}
		}

	}

	public void checkForLastContainer(Element inDocElem, double shippingCharge,
			double discount, double tax, String orderedPricingQty,
			String quantity) {
		// For bulk
		// call getshipmentcontainerlist to get all container scm..the one with
		// highest container scm is last..if any of the container does not have
		// double tax1 = Double.parseDouble(tax);
		double orderQty = Double.parseDouble(orderedPricingQty);
		double ShipQty = Double.parseDouble(quantity);

		// For Conveyable
		Element sShipmentElem = XMLUtil.getFirstElementByName(inDocElem,
				"Shipment");
		if (!YFCObject.isVoid(sShipmentElem)) {
			String isPackProcessComplete = sShipmentElem
					.getAttribute("IsPackProcessComplete");
			if ("Y".equals(isPackProcessComplete)) {
				shippingCharge = shippingCharge
						- ((Math.round(((shippingCharge / orderQty) * 100))) / 100.00)
						* (orderQty - ShipQty);
				discount = discount
						- ((Math.round(((discount / orderQty)
								* (orderQty - ShipQty) * 100))) / 100.00)
						* (orderQty - ShipQty);
				tax = tax
						- ((Math
								.round(((tax / orderQty) * (orderQty - ShipQty) * 100))) / 100.00)
						* (orderQty - ShipQty);
			} else {
				shippingCharge = (Math.round(((shippingCharge / orderQty)
						* ShipQty * 100))) / 100.00;
				discount = (Math.round(((discount / orderQty) * ShipQty * 100))) / 100.00;
				tax = (Math.round(((tax / orderQty) * ShipQty * 100))) / 100.00;
			}
		}
		// call getShipmentdetails

	}

	public void generatePRONumberForShipment() {

	}

	/*
	 * This method adds attributes to the pack slip These data have to be shown
	 * only when the an item exist.
	 * 
	 */
	public Element setStaticFieldsOnConvPackSlip(YFSEnvironment env,
			Element eleShipmentLine) {

		eleShipmentLine.setAttribute("ItemDiscText", "ITM DISC");
		eleShipmentLine.setAttribute("ItemTaxText", "ITM TAX");
		eleShipmentLine.setAttribute("ShippingAdjText", "SHIPPING");
		eleShipmentLine.setAttribute("ShippingTaxText", "TAX");
		eleShipmentLine.setAttribute("ItemTaxDesc",
				"Tax calculated after discount");
		eleShipmentLine.setAttribute("ShippingAdjDesc",
				"Shipping cost for this item");
		eleShipmentLine
				.setAttribute("ShippingTaxDesc", "Sales Tax on shipping");

		return eleShipmentLine;

	}
}
