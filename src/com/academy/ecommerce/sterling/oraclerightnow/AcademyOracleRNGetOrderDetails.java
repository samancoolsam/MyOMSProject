package com.academy.ecommerce.sterling.oraclerightnow;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * 
 * @author kgopal
 * 
 * Customer will enter the OrderNo or will click the GetOrderDetails button from
 * the Order List search screen. This will show the Order Details screen with
 * all Order Details.
 * 
 * Service Name: AcademyOracleRNGetRecentOrderDetails
 * 
 * Input XML:
 * 
 * <Input FlowName="AcademyOracleRNGetRecentOrderDetails"> <Login LoginID=""
 * Password="" /> <Order OrderNo="" /> </Input>
 * 
 * 
 */

public class AcademyOracleRNGetOrderDetails {

	private static final YFCLogCategory log = YFCLogCategory
			.instance(AcademyOracleRNGetOrderDetails.class);

	private Properties props;

	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}

	public Document oracleRNGetOrderDetails(YFSEnvironment env, Document inDoc)
			throws Exception {

		if (log.isVerboseEnabled()) {
			log.verbose("Input to method oracleRNGetOrderDetails is: "
					+ XMLUtil.getXMLString(inDoc));
		}

		Document docLoginOutput = null;
		Document docOrderDetailsInput = null;
		Document docOrderDetailsOutput = null;

		Element eleOrderLine = null;
		Element eleLineChargesFromDetail = null;
		Element eleLineTaxesFromDetail = null;
		Element LineCharge = null;
		Element LineTax = null;

		double totalShippingTax = 0.00;
		double totalMerchanTax = 0.00;
		double totalTaxWriteoffl = 0.00;
		double totalStateTax = 0.00;
		double totalCountyTax = 0.00;
		double totalCityTaxl = 0.00;

		DecimalFormat decimalCorrection = new DecimalFormat("0.00");

		Element rootElement = inDoc.getDocumentElement();

		// Getting the login element and validating the login credentials
		Element eleLogin = (Element) rootElement.getElementsByTagName("Login")
				.item(0);
		String strLoginID = eleLogin.getAttribute("LoginID");
		String strPassword = eleLogin.getAttribute("Password");
		Document docResponse = AcademyUtil.validateLoginCredentials(env,
				strLoginID, strPassword);

		if (YFCObject.isVoid(docResponse)) {
			docLoginOutput = XMLUtil.createDocument("Error");
			docLoginOutput.getDocumentElement().setAttribute("ErrorCode",
					AcademyConstants.LOGIN_ERROR_CODE);
			docLoginOutput.getDocumentElement().setAttribute(
					"ErrorDescription", AcademyConstants.LOGIN_ERROR_DESC);
			if (log.isVerboseEnabled()) {
				log.verbose("Error Output is :"
						+ XMLUtil.getXMLString(docLoginOutput));
			}
			return docLoginOutput;
		} else {
			if (log.isVerboseEnabled()) {
				log
						.verbose("User is authenticated successfully. Going to fetch Order Details through getOrderListAPI()");
			}

			// Creating getOrderList() input
			Element eleOrderIn = (Element) rootElement.getElementsByTagName(
					"Order").item(0);
			eleOrderIn.setAttribute("DocumentType",
					AcademyConstants.SALES_DOCUMENT_TYPE);
			eleOrderIn.setAttribute("EnterpriseCode",
					AcademyConstants.ENTERPRISE_CODE_SHIPMENT);

			docOrderDetailsInput = XMLUtil.getDocumentForElement(eleOrderIn);

			if (log.isVerboseEnabled()) {
				log.verbose("Order Detail input is :"
						+ XMLUtil.getXMLString(docOrderDetailsInput));
			}
			docOrderDetailsOutput = AcademyUtil.invokeService(env,
					"AcademyOracleRNGetOrderDetails", docOrderDetailsInput);

			if (log.isVerboseEnabled()) {
				log.verbose("Order Detail raw output from service is :"
						+ XMLUtil.getXMLString(docOrderDetailsOutput));
			}

			Element eleRootOrderDetailsOutput = docOrderDetailsOutput
					.getDocumentElement();

			Element eleOrder = (Element) eleRootOrderDetailsOutput
					.getElementsByTagName("Order").item(0);
			if (!YFCObject.isVoid(eleOrder)) {

				String strCreditCardType = "";
				String strDisplayCreditCardNo = "";
				// String strDisplaySvcNo = "";
				String strSvcNo = "";

				// Splitting Order Date & Time into 2 seperate attributes
				String strOrderDate = eleOrder
						.getAttribute(AcademyConstants.ATTR_ORDER_DATE);

				DateFormat df = new SimpleDateFormat(
						AcademyConstants.STR_DATE_TIME_PATTERN);
				Date dt = df.parse(strOrderDate);
				SimpleDateFormat dt1 = new SimpleDateFormat(
						"MM/dd/yyyy'T'HH:mm:ss");
				String strDateTime[] = ((dt1.format(dt)).split("T", 2));
				eleOrder.setAttribute("OrderDate", strDateTime[0]);
				eleOrder.setAttribute("OrderTime", strDateTime[1]);

				// For multiple payment Method
				Element elePaymentMethods = (Element) eleOrder
						.getElementsByTagName(
								AcademyConstants.ELE_PAYMENT_METHODS).item(0);
				NodeList nPaymentMethod = XMLUtil.getNodeList(
						elePaymentMethods, AcademyConstants.ELE_PAYMENT_METHOD);

				if (!YFCObject.isVoid(nPaymentMethod)) {
					for (int h = 0; h < nPaymentMethod.getLength(); h++) {
						double totalCharged = 0.00;
						double totalRefunded = 0.00;
						double amountCharged = 0.00;
						String strTotalCharged = "";
						String strTotalRefunded = "";
						Element elePaymentMethod = (Element) nPaymentMethod
								.item(h);
						strDisplayCreditCardNo = elePaymentMethod
								.getAttribute(AcademyConstants.ATTR_DISPLAY_CREDIT_CARD_NO);
						strCreditCardType = elePaymentMethod
								.getAttribute(AcademyConstants.ATTR_CREDIT_CARD_TYPE);
						strSvcNo = elePaymentMethod
								.getAttribute(AcademyConstants.ATTR_SVCNO);
						strTotalCharged = elePaymentMethod
								.getAttribute(AcademyConstants.TOTAL_CHARGED);
						strTotalRefunded = elePaymentMethod
								.getAttribute(AcademyConstants.TOTAL_REFUNDED);
						String strPaymentType = elePaymentMethod
								.getAttribute(AcademyConstants.ATTR_PAYMENT_TYPE);
						totalCharged = Double.valueOf(strTotalCharged);
						totalRefunded = Double.valueOf(strTotalRefunded);
						if (!YFCObject.isNull(strPaymentType)) {
							// Start :: CREDIT_CARD
							//KER-12036 : Payment Migration Changes to support new Payment Type
							if (AcademyConstants.CREDIT_CARD
									.equalsIgnoreCase(strPaymentType)) {
								elePaymentMethod.setAttribute(
										"DisplayCreditCardNo",
										strCreditCardType + "("
												+ strDisplayCreditCardNo + ")");
								amountCharged = totalCharged - totalRefunded;
								elePaymentMethod
										.setAttribute("AmountCharged",
												decimalCorrection
														.format(amountCharged));
							}
							// Start :: PayPal
							else if (AcademyConstants.PAYPAL
									.equalsIgnoreCase(strPaymentType)) {//KER-11461 change equals to equalsIgnoreCase.  to support new Paypal payment type
								elePaymentMethod.setAttribute(
										"DisplayCreditCardNo", "");
								amountCharged = totalCharged - totalRefunded;
								elePaymentMethod
										.setAttribute("AmountCharged",
												decimalCorrection
														.format(amountCharged));

							}
							// Start :: GIFT_CARD
							else if (AcademyConstants.STR_GIFT_CARD
									.equals(strPaymentType)) {
								elePaymentMethod.setAttribute(
										"DisplayCreditCardNo", elePaymentMethod.getAttribute("DisplaySvcNo"));
								elePaymentMethod.setAttribute("AmountCharged",
										elePaymentMethod
												.getAttribute("TotalCharged"));

							}
						}
					}
				}

				// Setting Shipment and Tracking Details at OrderLine level
				Element eleOrderLines = (Element) eleOrder
						.getElementsByTagName("OrderLines").item(0);
				NodeList nlOrderLine = XMLUtil.getNodeList(eleOrderLines,
						"OrderLine");

				for (int i = 0; i < nlOrderLine.getLength(); i++) {

					double shippingTaxAtLineLevel = 0.00;
					double shippingStateTaxAtLineLevel = 0.00;
					double shippingCountyTaxAtLineLevel = 0.00;
					double shippingCityTaxAtLineLevel = 0.00;
					double stateTaxAtLineLevel = 0.00;
					double countyTaxAtLineLevel = 0.00;
					double cityTaxAtLineLevel = 0.00;
					double itemStateTaxAtLineLevel = 0.00;
					double itemCountyTaxAtLineLevel = 0.00;
					double itemCityTaxAtLineLevel = 0.00;
					double merchanTaxAtLineLevel = 0.00;
					double taxWriteoffAtLineLevel = 0.00;
					double shippingChargeAtLineLevel = 0.00;
					double shippingPromotionAtLineLevel = 0.00;
					double discountForLine = 0.00;

					eleOrderLine = (Element) nlOrderLine.item(i);
					String strOrderLineKey = eleOrderLine
							.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
					String strDelMethod = eleOrderLine
							.getAttribute(AcademyConstants.ATTR_DEL_METHOD);
					if (strDelMethod.equalsIgnoreCase("SHP")) {
						String strFulfillMethod = props.getProperty("SHP");
						eleOrderLine.setAttribute("DeliveryMethod",
								strFulfillMethod);
					}
					Element eleContainers = docOrderDetailsOutput
							.createElement("Containers");
					Element eleOrderInvoiceList = (Element) (((XMLUtil
							.getNodeListByXPath(docOrderDetailsOutput,
									"/OrderList/Order/OrderInvoiceList"))
							.item(0)));

					if ((!YFCObject.isVoid(eleOrderInvoiceList))
							&& (eleOrderInvoiceList.hasChildNodes())) {

						// Fix for DSV orders with shipped status//
						/*
						 * Element eleShipment = (Element) (((XMLUtil
						 * .getNodeListByXPath( docOrderDetailsOutput,
						 * "/OrderList/Order/OrderInvoiceList/OrderInvoice/Shipment/ShipmentLines/ShipmentLine[@OrderLineKey='" +
						 * strOrderLineKey + "']"))
						 * .item(0)).getParentNode().getParentNode());
						 */
						// Fix for DSV orders with shipped status//
						NodeList nlOrderInvoice = eleOrderInvoiceList
								.getElementsByTagName("OrderInvoice");
						for (int n = 0; n < nlOrderInvoice.getLength(); n++) {
							Element eleOrderInvoice = (Element) nlOrderInvoice
									.item(n);
							String strInvoiceType = eleOrderInvoice
									.getAttribute("InvoiceType");

							if ((!YFCObject.isVoid(eleOrderInvoice))
									&& (eleOrderInvoice.hasChildNodes())
									&& (strInvoiceType.equals("PRO_FORMA"))) {

								// Element eleContainer = null;
								Element eleShipment = (Element) eleOrderInvoice
										.getElementsByTagName("Shipment").item(
												0);
								if (log.isVerboseEnabled()) {
									log
											.verbose("Shipment Element is :"
													+ XMLUtil
															.getElementXMLString(eleShipment));
								}
								String strShipmentType = eleShipment
										.getAttribute("ShipmentType");
								String strDocType = eleShipment
										.getAttribute("DocumentType");
								Element eleShipmentLines = (Element) eleShipment
										.getElementsByTagName("ShipmentLines")
										.item(0);
								NodeList nlShipmentLine = eleShipmentLines
										.getElementsByTagName("ShipmentLine");

								// Adding the TrackingNo
								// Fix for DSV orders with shipped status//
								for (int l = 0; l < nlShipmentLine.getLength(); l++) {
									Element eleShipmentLine = (Element) nlShipmentLine
											.item(l);
									if ((!strDocType.equals("0005"))) {

										String strShipmentOLK = eleShipmentLine
												.getAttribute("OrderLineKey");
										String strShipmentLineKey = eleShipmentLine.getAttribute("ShipmentLineKey");
										String strShipQty = eleShipmentLine.getAttribute("Quantity");
										double strIntQty = Double.parseDouble(strShipQty);
										if ((!strShipmentOLK.equals(null))
												&& (strShipmentOLK
														.equals(strOrderLineKey))) {
											// Start OMNI-2207  Checking the condition(Allowing) only if ShipmentLineQty > 0
											if(strIntQty > 0){
											// End OMNI-2207 
											if (log.isVerboseEnabled()) {
												log
														.verbose("Shipment is not a DSV Shipment");
											}
											// Setting the ActualShipmentDate,
											// ShipNode,
											// SCAC,
											// ScacAndService on OrderLine

											eleOrderLine
													.setAttribute(
															"ActualShipmentDate",
															eleShipment
																	.getAttribute("ActualShipmentDate"));
											eleOrderLine
													.setAttribute(
															"ScacAndService",
															eleShipment
																	.getAttribute("ScacAndService"));
											eleOrderLine
													.setAttribute(
															"ShipNode",
															eleShipment
																	.getAttribute("ShipNode"));
											eleOrderLine
													.setAttribute(
															"SCAC",
															eleShipment
																	.getAttribute("SCAC"));
											/*
											 * Node importEle =
											 * docOrderDetailsOutput.importNode(
											 * eleShipment.getElementsByTagName("Containers")
											 * .item(0), true); NodeList
											 * nlContainer = ((Element)
											 * importEle)
											 * .getElementsByTagName("Container");
											 * for (int j = 0; j <
											 * nlContainer.getLength(); j++) {
											 * eleContainer = (Element)
											 * nlContainer .item(j); String
											 * strTrackingNo = eleContainer
											 * .getAttribute("TrackingNo");
											 * String strTrackingURL =
											 * getTrackingURL( strTrackingNo,
											 * eleShipment
											 * .getAttribute("SCAC"));
											 * eleContainer.setAttribute("TrackingURL",
											 * strTrackingURL);
											 * eleContainers.appendChild(eleContainer); }
											 */
											getContainerDetails(
													docOrderDetailsOutput,
													eleShipment, eleContainers, strShipmentLineKey);
									//	Start OMNI-2207 
										}
									//	End OMNI-2207 
										}
									} else {
										if (log.isVerboseEnabled()) {
											log
													.verbose("Shipment is a DSV Shipment");
										}
										String strShpChainedOLK = eleShipmentLine
												.getAttribute("ChainedFromOrderLineKey");
										String strShipmentLineKey = eleShipmentLine.getAttribute("ShipmentLineKey");
										if ((!strShpChainedOLK.equals(null))
												&& (strShpChainedOLK
														.equals(strOrderLineKey))) {

											eleOrderLine
													.setAttribute(
															"ActualShipmentDate",
															eleShipment
																	.getAttribute("ActualShipmentDate"));
											eleOrderLine
													.setAttribute(
															"ScacAndService",
															eleShipment
																	.getAttribute("ScacAndService"));
											eleOrderLine
													.setAttribute(
															"ShipNode",
															eleShipment
																	.getAttribute("ShipNode"));
											eleOrderLine
													.setAttribute(
															"SCAC",
															eleShipment
																	.getAttribute("SCAC"));

											/*
											 * Node importEle =
											 * docOrderDetailsOutput.importNode(
											 * eleShipment.getElementsByTagName("Containers")
											 * .item(0), true); NodeList
											 * nlContainer = ((Element)
											 * importEle)
											 * .getElementsByTagName("Container");
											 * for (int m = 0; m <
											 * nlContainer.getLength(); m++) {
											 * eleContainer = (Element)
											 * nlContainer .item(m); String
											 * strTrackingNo = eleContainer
											 * .getAttribute("TrackingNo");
											 * String strTrackingURL =
											 * getTrackingURL( strTrackingNo,
											 * eleShipment
											 * .getAttribute("SCAC"));
											 * eleContainer.setAttribute("TrackingURL",
											 * strTrackingURL);
											 * eleContainers.appendChild(eleContainer); }
											 */
											getContainerDetails(
													docOrderDetailsOutput,
													eleShipment, eleContainers, strShipmentLineKey);

										}
									}
								}

							}

						}
					}
					eleOrderLine.appendChild(eleContainers);
					// Fix for DSV orders with shipped status//
					// calculating tax and charges

					eleLineChargesFromDetail = (Element) eleOrderLine
							.getElementsByTagName("LineCharges").item(0);
					eleLineTaxesFromDetail = (Element) eleOrderLine
							.getElementsByTagName("LineTaxes").item(0);

					// Calculating the total charge, promotion and discount at
					// order line level
					NodeList nLineCharge = XMLUtil.getNodeList(
							eleLineChargesFromDetail, "LineCharge");
					if (!YFCObject.isVoid(nLineCharge)) {

						for (int j = 0; j < nLineCharge.getLength(); j++) {
							LineCharge = (Element) eleLineChargesFromDetail
									.getElementsByTagName("LineCharge").item(j);
							String isDiscount = LineCharge
									.getAttribute("IsDiscount");
							String chargeAmount = LineCharge
									.getAttribute("ChargeAmount");
							String chargeName = LineCharge
									.getAttribute("ChargeName");

							if (LineCharge.getAttribute("ChargeName").contains(
									"Shipping")) {
								String strShippingChargeAtLineLevel = LineCharge
										.getAttribute("ChargeAmount");
								if (LineCharge.getAttribute("ChargeCategory")
										.contains("Promotions")) {
									if ("Y".equals(LineCharge
											.getAttribute("IsDiscount"))) {
										shippingPromotionAtLineLevel = shippingPromotionAtLineLevel
												- Double
														.parseDouble(strShippingChargeAtLineLevel);

									} else {
										shippingPromotionAtLineLevel = shippingPromotionAtLineLevel
												+ Double
														.parseDouble(strShippingChargeAtLineLevel);

									}
								} else {
									if ("Y".equals(LineCharge
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

							}
							if ("BOGO".equals(chargeName)
									|| ("DiscountCoupon".equals(chargeName))
									|| ("Merchandise".equals(chargeName))) {
								if ("Y".equals(isDiscount)) {
									discountForLine = discountForLine
											+ Double.parseDouble(chargeAmount);

								}

							}

						}

					}

					// Calculating the various taxes at order line level
					NodeList nLineTax = XMLUtil.getNodeList(
							eleLineTaxesFromDetail, "LineTax");
					if (!YFCObject.isVoid(nLineTax)) {
						for (int k = 0; k < nLineTax.getLength(); k++) {
							LineTax = (Element) eleLineTaxesFromDetail
									.getElementsByTagName("LineTax").item(k);
							String chargeCategory = LineTax
									.getAttribute("ChargeCategory");
							String taxName = LineTax.getAttribute("TaxName");

							if ("Shipping".equals(chargeCategory)) {
								if (taxName.contains("Shipping")) {
									String strShippingTaxAtLineLevel = LineTax
											.getAttribute("Tax");
									shippingTaxAtLineLevel += Double
											.parseDouble(strShippingTaxAtLineLevel);
								}
								if (taxName.contains("STATE")) {
									String strStateTaxAtLineLevel = LineTax
											.getAttribute("Tax");
									shippingStateTaxAtLineLevel += Double
											.parseDouble(strStateTaxAtLineLevel);
								}
								if ((taxName.contains("DISTRICT"))
										|| (taxName.contains("COUNTY"))) {
									String strCountyTaxAtLineLevel = LineTax
											.getAttribute("Tax");
									shippingCountyTaxAtLineLevel += Double
											.parseDouble(strCountyTaxAtLineLevel);
								}
								if (taxName.contains("CITY")) {
									String strCityTaxAtLineLevel = LineTax
											.getAttribute("Tax");
									shippingCityTaxAtLineLevel += Double
											.parseDouble(strCityTaxAtLineLevel);
								}

							}
							if ("TAXES".equals(chargeCategory)) {
								if (taxName.contains("Merchandise")) {
									String strItemTaxAtLineLevel = LineTax
											.getAttribute("Tax");
									merchanTaxAtLineLevel += Double
											.parseDouble(strItemTaxAtLineLevel);
								}
								if (taxName.contains("STATE")) {
									String strStateTaxAtLineLevel1 = LineTax
											.getAttribute("Tax");
									itemStateTaxAtLineLevel += Double
											.parseDouble(strStateTaxAtLineLevel1);
								}
								if ((taxName.contains("DISTRICT"))
										|| (taxName.contains("COUNTY"))) {
									String strCountyTaxAtLineLevel1 = LineTax
											.getAttribute("Tax");
									itemCountyTaxAtLineLevel += Double
											.parseDouble(strCountyTaxAtLineLevel1);
								}
								if (taxName.contains("CITY")) {
									String strCityTaxAtLineLevel1 = LineTax
											.getAttribute("Tax");
									itemCityTaxAtLineLevel += Double
											.parseDouble(strCityTaxAtLineLevel1);
								}

							}
							if ("TAX_WRITEOFF".equals(chargeCategory)) {
								String strTaxWriteoffAtLineLevel = LineTax
										.getAttribute("Tax");
								taxWriteoffAtLineLevel -= Double
										.parseDouble(strTaxWriteoffAtLineLevel);

							}

						}

					}

					stateTaxAtLineLevel = stateTaxAtLineLevel
							+ itemStateTaxAtLineLevel
							+ shippingStateTaxAtLineLevel;
					countyTaxAtLineLevel = countyTaxAtLineLevel
							+ itemCountyTaxAtLineLevel
							+ shippingCountyTaxAtLineLevel;
					cityTaxAtLineLevel = cityTaxAtLineLevel
							+ itemCityTaxAtLineLevel
							+ shippingCityTaxAtLineLevel;

					eleOrderLine
							.setAttribute("LineLevelShippingCharge",
									decimalCorrection
											.format(shippingChargeAtLineLevel));
					if (log.isVerboseEnabled()) {
						log.verbose("LineLevelShippingCharge is :"
								+ shippingChargeAtLineLevel);
					}

					eleOrderLine.setAttribute("LineLevelShippingPromotion",
							decimalCorrection
									.format(shippingPromotionAtLineLevel));
					if (log.isVerboseEnabled()) {
						log.verbose("LineLevelShippingPromotion is :"
								+ shippingPromotionAtLineLevel);
					}

					eleOrderLine.setAttribute("LineLevelDiscount",
							decimalCorrection.format(discountForLine));
					if (log.isVerboseEnabled()) {
						log.verbose("LineLevelDiscount is :" + discountForLine);
					}

					eleOrderLine.setAttribute("LineLevelStateTax",
							decimalCorrection.format(stateTaxAtLineLevel));
					if (log.isVerboseEnabled()) {
						log.verbose("LineLevelStateTax is :"
								+ stateTaxAtLineLevel);
					}

					eleOrderLine.setAttribute("LineLevelCountyTax",
							decimalCorrection.format(countyTaxAtLineLevel));
					if (log.isVerboseEnabled()) {
						log.verbose("LineLevelCountyTax is :"
								+ countyTaxAtLineLevel);
					}

					eleOrderLine.setAttribute("LineLevelCityTax",
							decimalCorrection.format(cityTaxAtLineLevel));
					if (log.isVerboseEnabled()) {
						log.verbose("LineLevelCityTax is :"
								+ cityTaxAtLineLevel);
					}

					eleOrderLine.setAttribute("LineLevelShippingTax",
							decimalCorrection.format(shippingTaxAtLineLevel));
					if (log.isVerboseEnabled()) {
						log
								.verbose("LineLevelShippingTax for tax name Shipping is :"
										+ shippingTaxAtLineLevel);
					}

					eleOrderLine.setAttribute("LineLevelMerchandiseTax",
							decimalCorrection.format(merchanTaxAtLineLevel));
					if (log.isVerboseEnabled()) {
						log
								.verbose("LineLevelMerchandiseTax for tax name TAXES is :"
										+ merchanTaxAtLineLevel);
					}

					eleOrderLine.setAttribute("LineLevelTaxWriteOff",
							decimalCorrection.format(taxWriteoffAtLineLevel));
					if (log.isVerboseEnabled()) {
						log
								.verbose("LineLevelTaxWriteOff for tax name TAX_WRITEOFF is :"
										+ taxWriteoffAtLineLevel);
					}

					totalShippingTax = totalShippingTax
							+ shippingTaxAtLineLevel;
					totalMerchanTax = totalMerchanTax + merchanTaxAtLineLevel;
					totalTaxWriteoffl = totalTaxWriteoffl
							+ taxWriteoffAtLineLevel;
					totalStateTax = totalStateTax + stateTaxAtLineLevel;
					totalCountyTax = totalCountyTax + countyTaxAtLineLevel;
					totalCityTaxl = totalCityTaxl + cityTaxAtLineLevel;
					// eleOrder.removeChild(eleOrderInvoiceList);
				}
				eleOrder.setAttribute("OrderLevelShippingTax",
						decimalCorrection.format(totalShippingTax));
				eleOrder.setAttribute("OrderLevelMerchandiseTax",
						decimalCorrection.format(totalMerchanTax));
				eleOrder.setAttribute("OrderLevelTaxWriteOff",
						decimalCorrection.format(totalTaxWriteoffl));
				eleOrder.setAttribute("OrderLevelStateTax", decimalCorrection
						.format(totalStateTax));
				eleOrder.setAttribute("OrderLevelCountyTax", decimalCorrection
						.format(totalCountyTax));
				eleOrder.setAttribute("OrderLevelCityTax", decimalCorrection
						.format(totalCityTaxl));

			}

			docOrderDetailsOutput = XMLUtil
					.getDocumentForElement(eleRootOrderDetailsOutput);
			if (log.isVerboseEnabled()) {
				log
						.verbose("Output from the web service call for get order details is : "
								+ XMLUtil.getXMLString(docOrderDetailsOutput));
			}

		}

		return docOrderDetailsOutput;

	}

	private void getContainerDetails(Document docOrderDetailsOutput,
			Element eleShipment, Element eleContainers, String strShipmentLineKey) {

		NodeList nlContainer = eleShipment.getElementsByTagName("Container");

		for (int i = 0; i < nlContainer.getLength(); i++) {

			Element eleContainer = (Element) nlContainer.item(i);
			// Start OMNI-2280
			Element eleContainerDetails = (Element)eleContainer.getElementsByTagName(AcademyConstants.ELE_CONTAINER_DTLS).item(0);
			NodeList nlContainerDetail = eleContainerDetails.getElementsByTagName(AcademyConstants.CONTAINER_DETAIL);
			
			for (int j = 0; j < nlContainerDetail.getLength(); j++) {
				
				Element eleContainerDetail = (Element) nlContainerDetail.item(j);
				String strContainerShipmentLineKey = eleContainerDetail.getAttribute("ShipmentLineKey");

				if(strContainerShipmentLineKey.equals(strShipmentLineKey)) {
					Element eleContainer1 = docOrderDetailsOutput.createElement("Container");

					String strTrackingNo = eleContainer.getAttribute("TrackingNo");
					String strContainerQuantity = eleContainerDetail.getAttribute(AcademyConstants.ATTR_QUANTITY);
					// End OMNI-2280
					String strTrackingURL = getTrackingURL(strTrackingNo, eleShipment.getAttribute("SCAC"));

					eleContainer1.setAttribute("TrackingNo", eleContainer
							.getAttribute("TrackingNo"));
					// Start OMNI-2280		
					eleContainer1.setAttribute("Quantity", strContainerQuantity);
					// End OMNI-2280
					eleContainer1.setAttribute("TrackingURL", strTrackingURL);
					eleContainers.appendChild(eleContainer1);
				}
			}

			if (log.isVerboseEnabled()) {
				log.verbose("Output from getContanerDetails method is : "
						+ XMLUtil.getElementXMLString(eleContainers));
			}

		}

	}

	private String getTrackingURL(String strTrackingNo, String strSCAC) {

		String strTrackingURL = "";

		if (strSCAC.equalsIgnoreCase("FEDX")) {
			String strCarrier = props.getProperty("FEDX");
			strTrackingURL = strCarrier.concat(strTrackingNo);
		} else if (strSCAC.equalsIgnoreCase("SmartPost")) {
			String strCarrier = props.getProperty("SmartPost");
			strTrackingURL = strCarrier.concat(strTrackingNo);
		//} else if (strSCAC.equalsIgnoreCase("USPS")) {
		} else if (strSCAC.contains("USPS")) {//STL-1645 Request to Update USPS link within Oracle Service cloud
			String strCarrier = props.getProperty("USPS");
			strTrackingURL = strCarrier.concat(strTrackingNo);
		} else if (strSCAC.equalsIgnoreCase("UPSN")) {
			String strCarrier = props.getProperty("UPSN");
			strTrackingURL = strCarrier.concat(strTrackingNo);
		} else if (strSCAC.equalsIgnoreCase("CEVA")) {
			String strCarrier = props.getProperty("CEVA");
			strTrackingURL = strCarrier.concat(strTrackingNo);
		}

		if (log.isVerboseEnabled()) {
			log.verbose("Tracking No URL = " + strTrackingURL);
		}

		return strTrackingURL;
	}
}