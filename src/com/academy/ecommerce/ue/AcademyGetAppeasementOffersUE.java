package com.academy.ecommerce.ue;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.common.ResourceUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.pca.ycd.japi.ue.YCDGetAppeasementOffersUE;
import com.yantra.util.YFCUtils;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;

public class AcademyGetAppeasementOffersUE implements YCDGetAppeasementOffersUE {

	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyGetAppeasementOffersUE.class);

	private String sAppeasementCategory = "";

	private Document discountMerchandiseXML = null;

	private Document discountShippingXML = null;

	private Document existingAppeasementOnOrderXML = null;

	private YFSEnvironment env;

	private double calculatedLineTotal;

	// the below variable is added for the CR 2683, which prevents immedeate
	// appeasement for GC tender
	private String preventImmedeateAppeasement;

	public Document getAppeasementOffers(YFSEnvironment env, Document inDoc)
			throws YFSUserExitException {
		log
				.beginTimer(" Begining of AcademyGetAppeasementOffersUE-> getAppeasementOffers Api");
		this.env = env;
		YFCDocument dIn = YFCDocument.getDocumentFor(inDoc);
		if (dIn == null) {
			return null;
		}
		YFCDocument dOut = YFCDocument
				.createDocument(AcademyConstants.ELE_APPEASEMENT_OFFERS);
		YFCElement eOut = dOut.getDocumentElement();
		YFCElement eIn = dIn.getDocumentElement();
		log.verbose("input to the appeasement user exit" + eIn.getString());

		YFCElement order = eIn.getElementsByTagName("Order").item(0);
		preventImmedeateAppeasement = order
				.getAttribute("PreventImmedeateAppeasement");
		// Upgrade changes for to prevent Immediate Appeasement when order has giftcard - start
		if(YFCObject.isVoid(preventImmedeateAppeasement) && (!YFCObject.isVoid(order)))
		{
			YFCElement eleAppeasementReason = order.getChildElement(AcademyConstants.ELE_APPEASEMENT_REASON);
			preventImmedeateAppeasement = eleAppeasementReason
			.getAttribute("PreventImmedeateAppeasementFromWC");
		}
		
      // Upgrade changes for to prevent Immediate Appeasement when order has giftcard - end
		log.verbose("Should immedeate appeasement be stopped? "
				+ preventImmedeateAppeasement);
		eOut = setAppeasementOffersForOrder(eOut, eIn, env);
		setPrefferedAttribute(eOut);
		log
				.endTimer(" End of AcademyGetAppeasementOffersUE-> getAppeasementOffers Api");
		return dOut.getDocument();
	}

	private void setPrefferedAttribute(YFCElement eOut) {
		Iterator iteratorAppeasementOffers = eOut.getChildren();

		if (iteratorAppeasementOffers != null) {
			while (iteratorAppeasementOffers.hasNext()) {
				YFCElement eAppeasementOffer = (YFCElement) iteratorAppeasementOffers
						.next();
				eAppeasementOffer.setAttribute(AcademyConstants.ATTR_PREFERRED,
						"Y");
				break;
			}
		}

	}

	private YFCElement setAppeasementOffersForOrder(YFCElement eOut,
			YFCElement eIn, YFSEnvironment env) {
		log
				.beginTimer(" Begining of AcademyGetAppeasementOffersUE-> setAppeasementOffersForOrder Api");
		YFCElement eOrder = eIn.getChildElement(AcademyConstants.ELE_ORDER);
		YFCElement eSelectedReason = eOrder
				.getChildElement(AcademyConstants.ELE_APPEASEMENT_REASON);

		String sAppeasementReasonCode = eSelectedReason
				.getAttribute(AcademyConstants.ATTR_REASON_CODE);
		sAppeasementCategory = eSelectedReason
				.getAttribute("CodeLongDescription");

		YFCElement eOrderIn = eIn.getChildElement(AcademyConstants.ELE_ORDER);
		String sOrderHeaderKey = eOrderIn
				.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY);

		setChargeCataegoryAndNameForAppeasementOffers(eOut,
				sAppeasementCategory);

		eOut.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,
				sOrderHeaderKey);
		eOut.setAttribute(AcademyConstants.ATTR_REASON_CODE,
				sAppeasementReasonCode);
		log.verbose("before calling getAppeasementOffersList");
		YFCElement eAppeasementOffers = getAppeasementOffersList(eIn,
				sAppeasementCategory, env);
		log.verbose("eAppeasementOffers" + eAppeasementOffers.getString());
		if (eAppeasementOffers != null) {
			Iterator iteratorAppeasementOffers = eAppeasementOffers
					.getChildren();
			if (iteratorAppeasementOffers != null) {
				while (iteratorAppeasementOffers.hasNext()) {
					YFCElement eAppeasementOffer = (YFCElement) iteratorAppeasementOffers
							.next();
					eOut = setAppeasementOffer(eOrderIn, eOut,
							eAppeasementOffer, sAppeasementCategory);
					log.verbose("eAppeasementOfferseOut" + eOut.getString());
				}
			}
		}
		eOut = setPreferredOffer(eOut);
		log
				.endTimer(" End of AcademyGetAppeasementOffersUE-> setAppeasementOffersForOrder Api");
		return eOut;
	}

	private YFCElement setAppeasementOffer(YFCElement eOrderIn,
			YFCElement eOut, YFCElement eAppeasementOffer,
			String appeasementCategory) {
		String offerType = eAppeasementOffer
				.getAttribute(AcademyConstants.ATTR_OFFER_TYPE);
		log
				.beginTimer(" Begining of AcademyGetAppeasementOffersUE-> setAppeasementOffer Api");
		if (offerType.equalsIgnoreCase("PERCENT_FUTURE_ORDER")) {
			eOut.importNode(eAppeasementOffer);
			return eOut;
		}
		YFCElement eOrderAppeasementOffer = eAppeasementOffer
				.createChild(AcademyConstants.ELE_ORDER);
		YFCElement eOrderLinesAppeasementOffer = eAppeasementOffer
				.createChild(AcademyConstants.ELE_ORDER_LINES);
		eOrderAppeasementOffer.setAttribute(
				AcademyConstants.ATTR_ORDER_HEADER_OFFER_AMOUNT, "");
		setChargeCataegoryAndNameForAppeasementOffers(eOrderAppeasementOffer,
				appeasementCategory);
		double discountPercent = 0;
		double offerAmount = 0;
		String strDiscountPercent = eAppeasementOffer
				.getAttribute(AcademyConstants.ATTR_DISCOUNT_PERCENT);
		String strOfferAmount = eAppeasementOffer
				.getAttribute(AcademyConstants.ATTR_OFFER_AMOUNT);
		if (strDiscountPercent.trim() != null
				&& !strDiscountPercent.equalsIgnoreCase(""))
			discountPercent = Double.parseDouble(strDiscountPercent);
		if (strOfferAmount.trim() != null
				&& !strOfferAmount.equalsIgnoreCase(""))
			offerAmount = truncateDecimal(Double.parseDouble(strOfferAmount),
					AcademyConstants.INT_NUMBER_OF_DECIMALS);
		log.verbose("Discount percent : " + discountPercent);
		log.verbose("Offer Amount : " + offerAmount);
		double totalLineOfferAmounts = 0.0;
		YFCElement eOrderLines = eOrderIn
				.getChildElement(AcademyConstants.ELE_ORDER_LINES);
		if (eOrderLines != null
				&& !offerType.equalsIgnoreCase("FLAT_AMOUNT_ORDER")
				&& !offerType.equalsIgnoreCase("VARIABLE_AMOUNT_ORDER")) {
			// if(eOrderLines!=null &&
			// !offerType.equalsIgnoreCase("FLAT_AMOUNT_ORDER") ){
			Iterator iteratorOrderLines = eOrderLines.getChildren();
			if (iteratorOrderLines != null) {
				while (iteratorOrderLines.hasNext()) {
					YFCElement eOrderLine = (YFCElement) iteratorOrderLines
							.next();
					String strOrderLineKey = eOrderLine
							.getAttribute("OrderLineKey");
					YFCElement eOrderLineAppeasementOffer = eOrderLinesAppeasementOffer
							.createChild(AcademyConstants.ELE_ORDER_LINE);
					String sOrderLineKey = eOrderLine
							.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
					eOrderLineAppeasementOffer
							.setAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY,
									sOrderLineKey);
					String sOrderedQty = eOrderLine.getAttribute("OrderedQty");
					eOrderLineAppeasementOffer.setAttribute("OrderedQty",
							sOrderedQty);
					if (discountPercent != 0) {
						if (appeasementCategory.equals("Merchandise")) {
							// Added by Chetan
							double dMerchandiseTax = getTax(eOrderLine
									.getChildElement("LineTaxes"),
									"Merchandise");
							// End Chetan
							YFCElement eLineOverallTotals = eOrderLine
									.getChildElement(AcademyConstants.ELE_LINE_OVERALL_TOTALS);
							/*
							 * double lineGrandTotal = Double
							 * .parseDouble(eLineOverallTotals
							 * .getAttribute(AcademyConstants.ATTR_EXTENDED_PRICE));
							 */
							// Added By Chetan
							double lineGrandTotal = (BigDecimal
									.valueOf(Double
											.parseDouble(eLineOverallTotals
													.getAttribute(AcademyConstants.ATTR_EXTENDED_PRICE)))
									.add(BigDecimal.valueOf(dMerchandiseTax)))
									.doubleValue();
							log.verbose("##### lineGrandTotal ##### "
									+ lineGrandTotal);
							// End Chetan
							double calculatedLineTotal = this
									.calculateLineTotal(eOrderLine,
											lineGrandTotal, appeasementCategory);
							log.verbose("##### calculatedLineTotal ##### "
									+ calculatedLineTotal);

							log
									.verbose("********** Caluculate line total after future appeasement *********");
							calculatedLineTotal = calculateLineTotalAfterFutureAppeasement(
									eOrderLine, calculatedLineTotal,
									sAppeasementCategory, strOrderLineKey,
									eOrderIn.getAttribute("OrderNo"));
							double lineOfferAmount = (calculatedLineTotal * 0.01)
									* discountPercent;

							log
									.verbose("Line Offer Amount: "
											+ lineOfferAmount);
							eOrderLineAppeasementOffer
									.setAttribute(
											AcademyConstants.ATTR_ORDER_LINE_OFFER_AMOUNT,
											truncateDecimal(
													lineOfferAmount,
													AcademyConstants.INT_NUMBER_OF_DECIMALS));

							totalLineOfferAmounts = totalLineOfferAmounts
									+ lineOfferAmount;
							log.verbose("Total LIne offer amount: "
									+ totalLineOfferAmounts);
						} else {
							// Added by Chetan
							double dShippingTax = getTax(eOrderLine
									.getChildElement("LineTaxes"), "Shipping");
							// End Chetan
							YFCElement elineCharges = eOrderLine
									.getChildElement("LineCharges");
							Iterator iteratorLineCharges = elineCharges
									.getChildren();
							if (iteratorLineCharges != null) {
								while (iteratorLineCharges.hasNext()) {
									YFCElement eShipCharge = (YFCElement) iteratorLineCharges
											.next();
									if (eShipCharge != null) {
										if (eShipCharge.getAttribute(
												"ChargeCategory").equals(
												"Shipping")) {
											/*
											 * double lineGrandTotal = Double
											 * .parseDouble(eShipCharge
											 * .getAttribute("ChargeAmount"));
											 */
											// Added By Chetan
											double lineGrandTotal = (BigDecimal
													.valueOf(Double
															.parseDouble(eShipCharge
																	.getAttribute("ChargeAmount")))
													.add(BigDecimal
															.valueOf(dShippingTax)))
													.doubleValue();
											log
													.verbose("##### Shipping lineGrandTotal ##### "
															+ lineGrandTotal);
											// End By Chetan
											double calculatedLineTotal = this
													.calculateLineTotal(
															eOrderLine,
															lineGrandTotal,
															appeasementCategory);
											log
													.verbose("##### calculatedLineTotal ##### "
															+ calculatedLineTotal);
											double lineOfferAmount = (calculatedLineTotal * 0.01)
													* discountPercent;
											log
													.verbose("*****  LineOfferAmount  : "
															+ lineOfferAmount);
											eOrderLineAppeasementOffer
													.setAttribute(
															AcademyConstants.ATTR_ORDER_LINE_OFFER_AMOUNT,
															truncateDecimal(
																	lineOfferAmount,
																	AcademyConstants.INT_NUMBER_OF_DECIMALS));
											totalLineOfferAmounts = totalLineOfferAmounts
													+ lineOfferAmount;
											log
													.verbose("*******  TotalLineofferamount  : "
															+ totalLineOfferAmounts);
										}
									}
								}
							}
						}
					}
					setChargeCataegoryAndNameForAppeasementOffers(
							eOrderLineAppeasementOffer, appeasementCategory);
				}
			}
		}
		eOrderAppeasementOffer.setAttribute(
				AcademyConstants.ATTR_ORDER_HEADER_OFFER_AMOUNT,
				truncateDecimal(offerAmount - totalLineOfferAmounts,
						AcademyConstants.INT_NUMBER_OF_DECIMALS));
		eOut.importNode(eAppeasementOffer);
		log
				.endTimer(" End of AcademyGetAppeasementOffersUE-> setAppeasementOffer Api");
		return eOut;
	}

	private YFCElement setPreferredOffer(YFCElement eOut) {
		// TODO Auto-generated method stub
		return eOut;
	}

	private YFCElement getAppeasementOffersList(YFCElement eIn,
			String appeasementCategory, YFSEnvironment env) {
		log.verbose("appeasementCategory" + appeasementCategory);
		log
				.beginTimer(" Begining of AcademyGetAppeasementOffersUE-> getAppeasementOffersList Api");
		YFCElement eAppeasementOffers = YFCDocument.createDocument(
				AcademyConstants.ELE_APPEASEMENT_OFFERS).getDocumentElement();
		YFCElement eOrderIn = eIn.getChildElement(AcademyConstants.ELE_ORDER);
		String strOrderNo = eOrderIn.getAttribute("OrderNo");
		YFCElement eOverallTotalIn = eOrderIn
				.getChildElement(AcademyConstants.ELE_OVERALL_TOTALS);
		// double orderGrandTotal =
		// Double.parseDouble(eOverallTotalIn.getAttribute(AcademyConstants.ATTR_GRAND_TOTAL));
		double orderGrandTotal = 0;
		double shippingGrandTotal = 0;
		String isWhiteGlove = "N";
		YFCElement eOrderLines = eOrderIn
				.getChildElement(AcademyConstants.ELE_ORDER_LINES);
		Iterator iteratorOrderLines = eOrderLines.getChildren();
		if (iteratorOrderLines != null) {
			while (iteratorOrderLines.hasNext()) {
				YFCElement eOrderLine = (YFCElement) iteratorOrderLines.next();
				String strOrderLineKey = eOrderLine
						.getAttribute("OrderLineKey");
				YFCElement eItemDetails = eOrderLine
						.getChildElement(AcademyConstants.ATTR_ITEM_DETAILS);
				if (eItemDetails != null && eItemDetails.hasChildNodes()) {
					YFCElement eExtnElem = eItemDetails
							.getChildElement(AcademyConstants.ELE_EXTN);
					log.verbose("order line : "
							+ YFCUtils.getStringValue(eOrderLine));
					log
							.verbose("value of attribute ATTR_ITEM_IS_WHITE_GLOVE : "
									+ eExtnElem
											.getAttribute(AcademyConstants.ATTR_ITEM_IS_WHITE_GLOVE));
					if ((AcademyConstants.ATTR_Y)
							.equals(eExtnElem
									.getAttribute(AcademyConstants.ATTR_ITEM_IS_WHITE_GLOVE))) {
						isWhiteGlove = "Y";
					}
				}

				// Added by Chetan
				double dMerchandiseTax = getTax(eOrderLine
						.getChildElement("LineTaxes"), "Merchandise");
				// End Chetan

				YFCElement eLineOverallTotals = eOrderLine
						.getChildElement(AcademyConstants.ELE_LINE_OVERALL_TOTALS);
				/*
				 * double lineGrandTotal = Double.parseDouble(eLineOverallTotals
				 * .getAttribute(AcademyConstants.ATTR_EXTENDED_PRICE)) +
				 * Double.parseDouble(eLineOverallTotals .getAttribute("Tax"));
				 */
				// Added by Chetan
				double lineGrandTotal = (BigDecimal
						.valueOf(Double
								.parseDouble(eLineOverallTotals
										.getAttribute(AcademyConstants.ATTR_EXTENDED_PRICE)))
						.add(BigDecimal.valueOf(dMerchandiseTax)))
						.doubleValue();
				// End by Chetan
				log.verbose("lineGrandTotal" + lineGrandTotal
						+ appeasementCategory + this.sAppeasementCategory);
				// orderGrandTotal=orderGrandTotal+lineGrandTotal;

				/**
				 * ****************************** START
				 * ******************************
				 */
				calculatedLineTotal = this.calculateLineTotal(eOrderLine,
						lineGrandTotal, "Merchandise");
				calculatedLineTotal = calculateLineTotalAfterFutureAppeasement(
						eOrderLine, calculatedLineTotal, appeasementCategory,
						strOrderLineKey, strOrderNo);

				// subtracting total discout from line grand total
				orderGrandTotal = orderGrandTotal + calculatedLineTotal;

				log.verbose("GrandTotal : " + orderGrandTotal);

				/** ******************************** END *********************** */

				try {
					// Added by Chetan
					double dShippingTax = getTax(eOrderLine
							.getChildElement("LineTaxes"), "Shipping");
					// End Chetan
					YFCElement elineCharges = eOrderLine
							.getChildElement("LineCharges");
					Iterator iteratorLineCharges = elineCharges.getChildren();
					if (iteratorLineCharges != null) {
						while (iteratorLineCharges.hasNext()) {
							YFCElement eShipCharge = (YFCElement) iteratorLineCharges
									.next();
							if (eShipCharge != null) {
								if (eShipCharge.getAttribute("ChargeCategory")
										.equals("Shipping")) {
									double shipCharge = Double
											.parseDouble(eShipCharge
													.getAttribute("ChargeAmount"));
									/*
									 * shippingGrandTotal = shippingGrandTotal +
									 * shipCharge;
									 */
									// Added by Chetan
									shippingGrandTotal = (BigDecimal.valueOf(
											shippingGrandTotal).add(
											BigDecimal.valueOf(shipCharge))
											.add(BigDecimal
													.valueOf(dShippingTax)))
											.doubleValue();
											
									shippingGrandTotal = this.calculateLineTotal(eOrderLine,
											shippingGrandTotal, "Shipping");
											
									log.verbose("#### shippingGrandTotal : "
											+ orderGrandTotal);
									// End by Chetan
								}
							}
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		Map userGroupMap = createUserGroupMap(env);
		String userGroup = getUserGroup(env, userGroupMap);

		log.verbose("Appeasement Category : " + appeasementCategory);
		if (appeasementCategory.equals("Merchandise")) {
			log.verbose("userGroup : " + userGroup);
			createMerchandiseAppeasementOffers(userGroup, "APPEASE_MRCH_GC",
					eAppeasementOffers, orderGrandTotal);
			createMerchandiseAppeasementOffers(userGroup, "APPEASE_MERCH",
					eAppeasementOffers, orderGrandTotal);
		}
		if (appeasementCategory.equals("Shipping")) {
			log.verbose("Is item white glove : " + isWhiteGlove);
			if (AcademyConstants.ATTR_Y.equals(isWhiteGlove)) {
				createShippingAppeasementOffers(userGroup, "APPEASE_SHP_WG",
						eAppeasementOffers, shippingGrandTotal);
			} else {
				// createShippingAppeasementOffers(userGroup, "APPEASE_SHP_STD",
				// eAppeasementOffers, shippingGrandTotal);
				log
						.verbose("creating standard shipping appeasement offer for userGroup : "
								+ userGroup);
				eAppeasementOffers = createAppeasementOffer(eAppeasementOffers,
						"PERCENT_FUTURE_ORDER", Integer.parseInt(ResourceUtil
								.get("SHP_USER_SITE_LEADER_PERCENT")),
						shippingGrandTotal);
				if (!AcademyConstants.STR_YES
						.equalsIgnoreCase(preventImmedeateAppeasement)) {
					eAppeasementOffers = createAppeasementOffer(
							eAppeasementOffers, "PERCENT_ORDER",
							Integer.parseInt(ResourceUtil
									.get("SHP_USER_SITE_LEADER_PERCENT")),
							shippingGrandTotal);
				}
			}
		}

		log
				.endTimer(" End of AcademyGetAppeasementOffersUE-> getAppeasementOffersList Api");
		return eAppeasementOffers;
	}

	// Added by Chetan

	private double getTax(YFCElement eleLineTaxes, String strAppeasementCategory) {

		log.verbose("Inside getTax() function");

		BigDecimal dTax = BigDecimal.valueOf(0.00);

		if (!YFCObject.isVoid(eleLineTaxes)) {
			Iterator itrLineTaxes = eleLineTaxes.getChildren();
			if (!YFCObject.isVoid(itrLineTaxes)) {
				while (itrLineTaxes.hasNext()) {
					YFCElement eleLineTax = (YFCElement) itrLineTaxes.next();
					if (!YFCObject.isVoid(eleLineTax)) {
						String strChargeCategory = eleLineTax
								.getAttribute(AcademyConstants.ATTR_CHARGE_CATEGORY);
						if (strAppeasementCategory
								.equalsIgnoreCase("Merchandise")) {
							if (strChargeCategory.equalsIgnoreCase("TAXES")) {
								log
										.verbose("Merchandise Tax --> "
												+ eleLineTax
														.getAttribute(AcademyConstants.ATTR_TAX_NAME)
												+ " -- "
												+ eleLineTax
														.getAttribute(AcademyConstants.ATTR_TAX));
								dTax = dTax
										.add(BigDecimal
												.valueOf(Double
														.parseDouble(eleLineTax
																.getAttribute(AcademyConstants.ATTR_TAX))));
							}
							if (strChargeCategory
									.equalsIgnoreCase("TAX_WRITEOFF")) {
								log
										.verbose("Merchandise Tax --> "
												+ eleLineTax
														.getAttribute(AcademyConstants.ATTR_TAX_NAME)
												+ " -- "
												+ eleLineTax
														.getAttribute(AcademyConstants.ATTR_TAX));
								dTax = dTax
										.subtract(BigDecimal
												.valueOf(Double
														.parseDouble(eleLineTax
																.getAttribute(AcademyConstants.ATTR_TAX))));
							}
						}
						if (strAppeasementCategory.equalsIgnoreCase("Shipping")) {
							if (strChargeCategory.equalsIgnoreCase("Shipping")) {
								log
										.verbose("Shipping Tax --> "
												+ eleLineTax
														.getAttribute(AcademyConstants.ATTR_TAX_NAME)
												+ " -- "
												+ eleLineTax
														.getAttribute(AcademyConstants.ATTR_TAX));
								dTax = dTax
										.add(BigDecimal
												.valueOf(Double
														.parseDouble(eleLineTax
																.getAttribute(AcademyConstants.ATTR_TAX))));
							}
						}
					}
				}
			}
		}
		log.verbose("Total Tax for " + strAppeasementCategory + " = "
				+ dTax.doubleValue());
		return dTax.doubleValue();
	}

	// End Chetan

	private void createMerchandiseAppeasementOffers(String userGroup,
			String appeasementCommonCodeType, YFCElement eAppeasementOffers,
			double orderGrandTotal) {
		try {
			log
					.beginTimer(" Begining of AcademyGetAppeasementOffersUE-> createMerchandiseAppeasementOffers Api");
			Document getCommonCodeListInputXML = XMLUtil
					.createDocument("CommonCode");
			getCommonCodeListInputXML.getDocumentElement().setAttribute(
					"CodeType", appeasementCommonCodeType);
			Document outXML = AcademyUtil.invokeAPI(env, "getCommonCodeList",
					getCommonCodeListInputXML);
			if (outXML != null) {
				NodeList commonCodeList = outXML
						.getElementsByTagName("CommonCode");
				if (commonCodeList != null && !YFCObject.isVoid(commonCodeList)) {
					log.verbose("common Code List for "
							+ appeasementCommonCodeType);
					log
							.verbose("CommonCodeList : "
									+ commonCodeList.toString());
					int iLength = commonCodeList.getLength();
					for (int i = 0; i < iLength; i++) {
						Element commonCode = (Element) commonCodeList.item(i);
						String codeValue = commonCode.getAttribute("CodeValue");
						log.verbose("Code value : " + codeValue);
						if (userGroup.equals(codeValue)) {
							if (appeasementCommonCodeType
									.equals("APPEASE_MERCH")) {
								log
										.verbose("creating merchandise appeasement offer for userGroup : "
												+ userGroup);
								createMultipleAppeasementOffers(
										eAppeasementOffers,
										orderGrandTotal,
										Integer
												.parseInt(commonCode
														.getAttribute("CodeShortDescription")));
							} else if (appeasementCommonCodeType
									.equals("APPEASE_MRCH_GC")) {
								log
										.verbose("creating GC merchandise appeasement offer for userGroup : "
												+ userGroup);
								createMultipleAppeasementOfferForGC(
										eAppeasementOffers,
										orderGrandTotal,
										Integer
												.parseInt(commonCode
														.getAttribute("CodeShortDescription")));
							}
						}
					}
				}
			}
			log
					.endTimer(" End of AcademyGetAppeasementOffersUE-> createMerchandiseAppeasementOffers Api");
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createShippingAppeasementOffers(String userGroup,
			String appeasementCommonCode, YFCElement eAppeasementOffers,
			double shippingGrandTotal) {
		try {
			log
					.beginTimer(" Begining of AcademyGetAppeasementOffersUE-> createShippingAppeasementOffers Api");
			Document getCommonCodeListInputXML = XMLUtil
					.createDocument("CommonCode");
			getCommonCodeListInputXML.getDocumentElement().setAttribute(
					"CodeType", appeasementCommonCode);
			Document outXML = AcademyUtil.invokeAPI(env, "getCommonCodeList",
					getCommonCodeListInputXML);
			if (outXML != null) {
				NodeList commonCodeList = outXML
						.getElementsByTagName("CommonCode");
				if (commonCodeList != null && !YFCObject.isVoid(commonCodeList)) {
					log
							.verbose("common Code List for "
									+ appeasementCommonCode);
					log
							.verbose("CommonCodeList : "
									+ commonCodeList.toString());
					int iLength = commonCodeList.getLength();
					for (int i = 0; i < iLength; i++) {
						Element commonCode = (Element) commonCodeList.item(i);
						String codeValue = commonCode.getAttribute("CodeValue");
						log.verbose("Code value : " + codeValue);
						if (userGroup.equals(codeValue)) {
							log
									.verbose("creating shipping appeasement offer for userGroup : "
											+ userGroup);
							createMultipleAppeasementOfferForGC(
									eAppeasementOffers,
									shippingGrandTotal,
									Integer
											.parseInt(commonCode
													.getAttribute("CodeShortDescription")));
							createMultipleAppeasementOffers(
									eAppeasementOffers,
									shippingGrandTotal,
									Integer
											.parseInt(commonCode
													.getAttribute("CodeShortDescription")));
						}
					}
				}
			}

			log
					.endTimer(" End of AcademyGetAppeasementOffersUE-> createShippingAppeasementOffers Api");
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private double calculateLineTotal(YFCElement eOrderLine, double lineTotal,
			String sOfferType) {

		// Subtract already existing discounts (BOGO, DiscountCoupon etc) from
		// line grand total
		// make an entry "DISCOUNT" in commonCodeList with values BOGO,
		// DiscountCoupon etc.

		log.verbose("Calculating the line total after discount");

		double totalDiscount = 0;
		try {
			log
					.beginTimer(" Begining of AcademyGetAppeasementOffersUE-> calculateLineTotal Api");
			Document getCommonCodeListInputXML = XMLUtil
					.createDocument("CommonCode");

			// make an entry "DISCOUNT" in commonCodeList with values BOGO,
			// CUSTOMER_APPEASEMENT, DiscountCoupon etc.

			Element outCommElem = null;
			log.verbose("Before the check sOfferType" + sOfferType);
			if (sOfferType.equals("Merchandise")) {
				log.verbose("inside the check");
				if (discountMerchandiseXML == null) {
					log.verbose("inside Second check");

					getCommonCodeListInputXML.getDocumentElement()
							.setAttribute("CodeType", "DISCOUNT_MERCH");
					log.verbose("calling the APi");
					discountMerchandiseXML = AcademyUtil.invokeAPI(env,
							"getCommonCodeList", getCommonCodeListInputXML);
				}
				if (!YFCObject.isVoid(discountMerchandiseXML)) {
					outCommElem = discountMerchandiseXML.getDocumentElement();

					log.verbose("output of discountMerchandise common code"
							+ XMLUtil.getElementXMLString(outCommElem));
				}
			} else {
				if (discountShippingXML == null) {
					getCommonCodeListInputXML.getDocumentElement()
							.setAttribute("CodeType", "Discount_Shp");
					discountShippingXML = AcademyUtil.invokeAPI(env,
							"getCommonCodeList", getCommonCodeListInputXML);

				}

				if (!YFCObject.isVoid(discountShippingXML)) {
					outCommElem = discountShippingXML.getDocumentElement();
					log.verbose("output of discountshipping common code"
							+ XMLUtil.getElementXMLString(outCommElem));
				}
			}

			NodeList commonCodeList = null;

			commonCodeList = XMLUtil.getNodeList(outCommElem, "CommonCode");

			// make an entry "DISCOUNT" in commonCodeList with values BOGO,
			// CUSTOMER_APPEASEMENT, DiscountCoupon etc.
			if (commonCodeList != null && !YFCObject.isVoid(commonCodeList)) {
				int iLength = commonCodeList.getLength();
				YFCElement elineCharges = eOrderLine
						.getChildElement("LineCharges");
				Iterator iteratorLineCharges = elineCharges.getChildren();

				// iterating to check every line
				if (iteratorLineCharges != null) {
					while (iteratorLineCharges.hasNext()) {
						YFCElement lineCharge = (YFCElement) iteratorLineCharges
								.next();
						if (lineCharge != null) {
							// loop to check all the discounts
							for (int k = 0; k < iLength; k++) {

								log.verbose("output of line charges"
										+ lineCharge.getString());
								Element CommonCode = (Element) commonCodeList
										.item(k);
								String codeValue = CommonCode
										.getAttribute("CodeValue");
								log
										.verbose("output of line charges"
												+ lineCharge
														.getAttribute("ChargeName"));

								if (codeValue.equals(lineCharge
										.getAttribute("ChargeName"))) {
									log
											.verbose("Discount found : "
													+ codeValue);
									double discountAmount = Double
											.parseDouble(lineCharge
													.getAttribute("ChargeAmount"));
									totalDiscount = totalDiscount
											+ discountAmount;
								}
							}
						}
					}
				}
			}
			log
					.endTimer(" End of AcademyGetAppeasementOffersUE-> calculateLineTotal Api");
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		log.verbose("returning line total after discount"
				+ (lineTotal - totalDiscount));
		return lineTotal - totalDiscount;
	}

	private void createMultipleAppeasementOffers(YFCElement eAppeasementOffers,
			double orderGrandTotal, int appeasementMaxValue) {
		int count = 5;
		if (!AcademyConstants.STR_YES
				.equalsIgnoreCase(preventImmedeateAppeasement)) {
			for (int i = 0; i < appeasementMaxValue; i += 5) {
				eAppeasementOffers = createAppeasementOffer(eAppeasementOffers,
						"PERCENT_ORDER", count, orderGrandTotal);
				count = count + 5;
			}
		}

	}

	private void createMultipleAppeasementOfferForGC(
			YFCElement eAppeasementOffers, double orderGrandTotal,
			int appeasementMaxValue) {
		int count = 5;
		for (int i = 0; i < appeasementMaxValue; i += 5) {
			eAppeasementOffers = createAppeasementOffer(eAppeasementOffers,
					"PERCENT_FUTURE_ORDER", count, orderGrandTotal);
			count = count + 5;
		}
	}

	private Map createUserGroupMap(YFSEnvironment env) {

		Map mp = new HashMap<String, Integer>();
		try {
			Document getCommonCodeListInputXML = XMLUtil
					.createDocument("CommonCode");

			getCommonCodeListInputXML.getDocumentElement().setAttribute(
					"CodeType", AcademyConstants.APP_USR_GRP_LST);
			Document outXML = AcademyUtil.invokeAPI(env, "getCommonCodeList",
					getCommonCodeListInputXML);
			if (outXML != null) {
				NodeList listOfCommCodes = outXML
						.getElementsByTagName("CommonCode");
				int len = listOfCommCodes.getLength();
				if (len > 0) {
					for (int i = 0; i < len; i++) {
						Element commCodeElem = (Element) listOfCommCodes
								.item(i);
						mp.put(commCodeElem.getAttribute("CodeValue"), Integer
								.parseInt(commCodeElem
										.getAttribute("CodeShortDescription")));
					}
				}
			}

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mp;

	}

	private String getUserGroup(YFSEnvironment env, Map userGroupMap) {

		String userId = env.getUserId();
		log.verbose("Logged in user ID : " + userId);
		String currUserGroupName = "";
		try {
			log
					.beginTimer(" Begining of AcademyGetAppeasementOffersUE-> getUserGroup Api");
			Document getUserListInputXML = XMLUtil.createDocument("User");
			getUserListInputXML.getDocumentElement().setAttribute("Loginid",
					userId);
			Document getUserListOutputXML = AcademyUtil.invokeAPI(env,
					"getUserList", getUserListInputXML);

			NodeList userGroupList = getUserListOutputXML
					.getElementsByTagName("UserGroupList");
			int length = userGroupList.getLength();
			if (length > 0) {

				for (int i = 0; i < length; i++) {
					Element currUserGroupListElem = (Element) userGroupList
							.item(i);
					String usergroupkey = currUserGroupListElem
							.getAttribute("UsergroupKey");
					String userGroupName = getUserGroupName(env, usergroupkey);
					log.verbose("user group key : " + usergroupkey);
					log.verbose("user group name : " + userGroupName);
					if (currUserGroupName.equals("")
							&& userGroupMap.get(userGroupName) != null) {
						currUserGroupName = userGroupName;
					} else {
						if (userGroupMap.get(userGroupName) != null
								&& userGroupMap.get(currUserGroupName) != null) {
							if ((Integer) (userGroupMap.get(userGroupName)) > (Integer) (userGroupMap
									.get(currUserGroupName))) {
								currUserGroupName = userGroupName;
							}
						}
					}
				}
			}
			log
					.endTimer(" End of AcademyGetAppeasementOffersUE-> getUserGroup Api");
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		log.verbose("Logged in user : " + userId + " belongs to user group : "
				+ currUserGroupName);
		return currUserGroupName;
	}

	private String getUserGroupName(YFSEnvironment env, String usergroupkey) {
		String usergroupName = "";
		try {
			Document getUserGroupListInputXML = XMLUtil
					.createDocument("UserGroup");
			getUserGroupListInputXML.getDocumentElement().setAttribute(
					"UsergroupKey", usergroupkey);
			Document getUserGroupListOutputXML = AcademyUtil.invokeAPI(env,
					"getUserGroupList", getUserGroupListInputXML);
			log.verbose("User group list : "
					+ XMLUtil.getXMLString(getUserGroupListOutputXML));
			usergroupName = ((Element) getUserGroupListOutputXML
					.getElementsByTagName("UserGroup").item(0))
					.getAttribute("UsergroupId");
			log.verbose("user group name : " + usergroupName);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return usergroupName;
	}

	private YFCElement createAppeasementOffer(YFCElement eAppeasementOffers,
			String sOfferType, double iDiscountPercent, double orderGrandTotal) {
		YFCElement eAppeasementOffer = YFCDocument.createDocument(
				AcademyConstants.ELE_APPEASEMENT_OFFER).getDocumentElement();
		eAppeasementOffer.setAttribute(AcademyConstants.ATTR_OFFER_TYPE,
				sOfferType);
		log
				.beginTimer(" Begining of AcademyGetAppeasementOffersUE-> createAppeasementOffer Api");
		if (iDiscountPercent != 0) {
			eAppeasementOffer.setAttribute(
					AcademyConstants.ATTR_DISCOUNT_PERCENT, iDiscountPercent);
			double offerAmount = 0.0;
			// if(!sOfferType.equalsIgnoreCase("PERCENT_FUTURE_ORDER"))
			offerAmount = (iDiscountPercent * 0.01) * (orderGrandTotal);
			eAppeasementOffer.setAttribute(AcademyConstants.ATTR_OFFER_AMOUNT,
					truncateDecimal(offerAmount,
							AcademyConstants.INT_NUMBER_OF_DECIMALS));
		} else {
			eAppeasementOffer.setAttribute(
					AcademyConstants.ATTR_DISCOUNT_PERCENT, "");
			if (sOfferType.equalsIgnoreCase("FLAT_AMOUNT_ORDER")) {
				eAppeasementOffer
						.setAttribute(
								AcademyConstants.ATTR_OFFER_AMOUNT,
								truncateDecimal(10.00,
										AcademyConstants.INT_NUMBER_OF_DECIMALS));
			} else {
				eAppeasementOffer.setAttribute(
						AcademyConstants.ATTR_OFFER_AMOUNT, "");
			}
		}
		/*
		 * if(sOfferType.equalsIgnoreCase("PERCENT_FUTURE_ORDER"))
		 * eAppeasementOffer.setAttribute(AcademyConstants.ATTR_PREFERRED,"Y");
		 * else
		 * eAppeasementOffer.setAttribute(AcademyConstants.ATTR_PREFERRED,"N");
		 */
		eAppeasementOffer.setAttribute(AcademyConstants.ATTR_PREFERRED, "N");
		eAppeasementOffers.importNode(eAppeasementOffer);
		log
				.endTimer(" End of AcademyGetAppeasementOffersUE-> createAppeasementOffer Api");
		return eAppeasementOffers;
	}

	private void setChargeCataegoryAndNameForAppeasementOffers(YFCElement eOut,
			String appeasementCategory) {
		eOut.setAttribute(AcademyConstants.ATTR_CHARGE_CATEGORY,
				"CUSTOMER_APPEASEMENT");
		eOut.setAttribute(AcademyConstants.ATTR_CHARGE_NAME,
				appeasementCategory);

	}

	private double calculateLineTotalAfterFutureAppeasement(
			YFCElement eOrderLine, double lineTotal, String sOfferType,
			String strOrderLineKey, String strOrderNumber) {

		log
				.beginTimer(" Begining of AcademyGetAppeasementOffersUE-> calculateLineTotalAfterFutureAppeasement Api");
		log.verbose("Calculating the line total after appeasement");

		double totalDiscount = 0;
		try {
			Document aCADOrderAppeasementInputXML = XMLUtil
					.createDocument("ACADOrderAppeasement");
			Element aCADOrderAppeasementInputXMLEle = aCADOrderAppeasementInputXML
					.getDocumentElement();

			aCADOrderAppeasementInputXMLEle.setAttribute("OrderNumber",
					strOrderNumber);
			aCADOrderAppeasementInputXMLEle.setAttribute("AppeasementReason",
					this.sAppeasementCategory);
			// aCADOrderAppeasementInputXMLEle.setAttribute("AcademyOrderLineKey",
			// strOrderLineKey);
			aCADOrderAppeasementInputXMLEle.setAttribute("OfferType",
					"PERCENT_FUTURE_ORDER");

			if (existingAppeasementOnOrderXML == null) {

				existingAppeasementOnOrderXML = AcademyUtil.invokeService(env,
						"AcademyGetAppeasementListForOrderService",
						aCADOrderAppeasementInputXML);
			}
			NodeList existingAppeasementOnOrderList = existingAppeasementOnOrderXML
					.getElementsByTagName("ACADOrderAppeasement");

			if (existingAppeasementOnOrderList != null
					&& !YFCObject.isVoid(existingAppeasementOnOrderList)) {
				int iLength = existingAppeasementOnOrderList.getLength();

				// loop to check all the discounts
				for (int k = 0; k < iLength; k++) {
					Element existingAppeasementOnOrderEle = (Element) existingAppeasementOnOrderList
							.item(k);
					String orderLineKeyValue = existingAppeasementOnOrderEle
							.getAttribute("AcademyOrderLineKey");
					if (orderLineKeyValue.equals(strOrderLineKey)) {
						log.verbose("Appeasement found : ");
						double discountAmount = Double
								.parseDouble(existingAppeasementOnOrderEle
										.getAttribute("AppeasementAmount"));
						log.verbose("Appeasement found :discount Amount "
								+ discountAmount);
						totalDiscount = totalDiscount + discountAmount;
					}
				}
			}
			log
					.endTimer(" End of AcademyGetAppeasementOffersUE-> calculateLineTotalAfterFutureAppeasement Api");
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		log.verbose("returning line total after discount"
				+ (lineTotal - totalDiscount));
		return lineTotal - totalDiscount;
	}

	// Added below method as part of Penny issue - truncate after specified
	// decimals
	private double truncateDecimal(double offerVal, int noOfDecimal) {
		String newVal = null;
		String value = String.valueOf(offerVal);
		int decimalPos = value.indexOf(".");
		newVal = value.substring(0, decimalPos);
		String tempStr = value.substring(decimalPos + 1);
		if (tempStr.length() > noOfDecimal) {
			newVal = newVal + "." + tempStr.substring(0, noOfDecimal);
		} else
			newVal = newVal + "." + tempStr;
		return Double.parseDouble(newVal);
	}
}
