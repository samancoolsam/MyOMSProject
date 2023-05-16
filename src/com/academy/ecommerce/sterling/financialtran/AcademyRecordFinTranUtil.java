package com.academy.ecommerce.sterling.financialtran;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyServiceUtil;
import com.academy.util.xml.XMLUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @author psomashekar-tw
 *
 */
public class AcademyRecordFinTranUtil
{

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyRecordFinTranUtil.class);

	/**
	 * Document has list of Charges, Discounts and Tax list for different document types
	 */
	private static final Document CHARGE_AND_TAX_DOC = getChargeAndTaxDoc();

	private static Document getChargeAndTaxDoc()
	{
		try
		{
			return AcademyServiceUtil.getSOAPMessageTemplate("/global/template/utils/AcademyChargesAndDiscountsList.xml");
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Creates input for extendeddb api for ACAD_FIN_TRANSACT table
	 * @param apiIndoc
	 * @return
	 */
	public static YFCElement getFinTranApiInput(YFCElement apiIndoc)
	{
		YFCElement apiEle = apiIndoc.createChild("API");
		apiEle.setAttribute("IsExtendedDbApi", "Y");
		apiEle.setAttribute("Name", "createACADFinTrans");
		YFCElement apiInputEle = apiEle.createChild("Input");
		return apiInputEle.createChild("ACADFinTrans");
	}

	/**
	 * Calculates merchandise tax for given document type. Sum up all taxes defined as merchandise tax
	 * and subtract all taxes defined as merchandise tax write off. 
	 * (Refer AcademyChargesAndDiscountsList.xml)
	 * This also sets corresponding fields in finTranDataObj if passed as parameter.
	 * @param lineDetailEle
	 * @param finTranDataObj
	 * @param documentType
	 * @return
	 */
	public static double getMerchardizeTax(Element lineDetailEle, Object finTranDataObj, String documentType)
	{
		double merchandizeTax = 0.0;
		try
		{
			// Get list of merchandise tax from common code
			NodeList mTaxListFromCommonCode = getMerchandiseTaxList(documentType);

			/*
			 * Sum up all taxes under ChargeCategory='TAXES' and subtract all taxes under
			 * ChargeCategory='TAX_WRITEOFF'
			 */
			for (int taxCount = 0; taxCount < mTaxListFromCommonCode.getLength(); taxCount++)
			{
				Element taxFromCommonCode = (Element) mTaxListFromCommonCode.item(taxCount);
				log.verbose("tax from common code - " + XMLUtil.getElementXMLString(taxFromCommonCode));
				NodeList merchandizeTaxList = XMLUtil.getNodeList(lineDetailEle, "LineTaxes/LineTax[@ChargeCategory='"
						+ taxFromCommonCode.getAttribute("ChargeCategory") + "']");
				for (int i = 0; i < merchandizeTaxList.getLength(); i++)
				{
					Element taxElement = (Element) merchandizeTaxList.item(i);
					log.verbose("TAX ELEMENT - " + XMLUtil.getElementXMLString(taxElement));
					log.verbose("charge category - " + taxElement.getAttribute("ChargeCategory"));
					if ("TAX_WRITEOFF".equalsIgnoreCase(taxElement.getAttribute("ChargeCategory")))
					{
						if (finTranDataObj instanceof AcademyFinTranDataBean)
						{
							((AcademyFinTranDataBean) finTranDataObj).merchandiseTaxWriteOff = taxElement.getAttribute("Tax");
						}
						merchandizeTax -= Double.parseDouble(taxElement.getAttribute("Tax"));
					}
					else
					{
						merchandizeTax += Double.parseDouble(taxElement.getAttribute("Tax"));
					}
				}
			}
			if (finTranDataObj instanceof AcademyFinTranDataBean)
			{
				((AcademyFinTranDataBean) finTranDataObj).merchandiseTax = String.valueOf(merchandizeTax);
				log.verbose("getMerchardizeTax - bean : " + ((AcademyFinTranDataBean) finTranDataObj).toString());
			}
		}
		catch (Exception e)
		{
			log.error(e);
		}
		return merchandizeTax;
	}

	/**
	 * Calculates shipping tax for given document type. Sum up all taxes defined as shipping tax
	 * and subtract all taxes defined as shipping tax write off. 
	 * (Refer AcademyChargesAndDiscountsList.xml)
	 * This also sets corresponding fields in finTranDataObj if passed as parameter.
	 * @param lineDetailEle
	 * @param finTranDataObj
	 * @param doucmentType
	 * @return
	 */
	public static double getShipingTax(Element lineDetailEle, Object finTranDataObj, String doucmentType)
	{
		double shippingTax = 0.0;
		try
		{

			// Get list of merchandise tax from common code
			NodeList sTaxListFromCommonCode = getShippingTaxList(doucmentType);

			/*
			 * Sum up all tax under ChargeCategory='Shipping'
			 */
			for (int taxCount = 0; taxCount < sTaxListFromCommonCode.getLength(); taxCount++)
			{
				Element taxFromCommonCode = (Element) sTaxListFromCommonCode.item(taxCount);
				log.verbose("tax from common code - " + XMLUtil.getElementXMLString(taxFromCommonCode));
				NodeList shippingTaxList = XMLUtil.getNodeList(lineDetailEle, "LineTaxes/LineTax[@ChargeCategory='"
						+ taxFromCommonCode.getAttribute("ChargeCategory") + "']");
				for (int i = 0; i < shippingTaxList.getLength(); i++)
				{
					Element taxElement = (Element) shippingTaxList.item(i);
					log.verbose("tax element - " + XMLUtil.getElementXMLString(taxElement));
					if ("TAX_WRITEOFF".equalsIgnoreCase(taxElement.getAttribute("ChargeCategory")))
					{
						if (finTranDataObj instanceof AcademyFinTranDataBean)
						{
							((AcademyFinTranDataBean) finTranDataObj).shippingTaxWriteOff = taxElement.getAttribute("Tax");
						}
						shippingTax -= Double.parseDouble(taxElement.getAttribute("Tax"));
					}
					else
					{
						shippingTax += Double.parseDouble(taxElement.getAttribute("Tax"));
					}
				}
			}
			if (finTranDataObj instanceof AcademyFinTranDataBean)
			{
				((AcademyFinTranDataBean) finTranDataObj).shippingTax = String.valueOf(shippingTax);
				log.verbose("getShipingTax - " + ((AcademyFinTranDataBean) finTranDataObj).toString());
			}
		}
		catch (Exception e)
		{
			log.error(e);
		}
		return shippingTax;
	}

	/**
	 * Calculates merchandise amount, merchandise tax, shipping amount and shipping tax from a 
	 * Order line and sets values into finTranDataObj
	 * @param lineDetailEle
	 * @param finTranDataObj
	 * @param doucmentType
	 */
	public static void getLineDetailsForDBRrecord(Element lineDetailEle, Object finTranDataObj, String doucmentType)
	{
		//Code Changes for PLCC Payment
		getPLCCRewardPoints(lineDetailEle, finTranDataObj, doucmentType);
		
		getMerchandiseAmount(lineDetailEle, finTranDataObj, doucmentType);
		getMerchardizeTax(lineDetailEle, finTranDataObj, doucmentType);
		getShipingAmount(lineDetailEle, finTranDataObj, doucmentType);
		getShipingTax(lineDetailEle, finTranDataObj, doucmentType);
		
	}

	/**
	 * Calculates merchandise amount for an order line. 
	 * This is (ExtendedPrince + Merchandise Charges - Merchandise Discounts)
	 * Also sets corresponding fields in finTranDataObj.
	 * @param lineDetailEle
	 * @param finTranDataObj
	 * @param doucmentType
	 * @return
	 */
	public static double getMerchandiseAmount(Element lineDetailEle, Object finTranDataObj, String doucmentType)
	{
		double merchandiseAmt = 0.0;
		try
		{
			String extendedPrice = XMLUtil.getString(lineDetailEle, "@ExtendedPrice");
			log.verbose("extended price - " + extendedPrice);
			Element lineCharges = (Element) XMLUtil.getNode(lineDetailEle, "LineCharges");
			double merchandiseChargeAndDiscount = getMerchandiseChargeAndDiscount(lineCharges, finTranDataObj, doucmentType);
			log.verbose("returned merchandise charge and discount - " + merchandiseChargeAndDiscount);
			merchandiseAmt = Double.parseDouble(extendedPrice) + merchandiseChargeAndDiscount;
			log.verbose("merchandise amount - " + merchandiseAmt);
			if (finTranDataObj instanceof AcademyFinTranDataBean)
			{
				String unitPrice = XMLUtil.getString(lineDetailEle, "@UnitPrice");
				((AcademyFinTranDataBean) finTranDataObj).merchandiseExtendedAmt = extendedPrice;
				((AcademyFinTranDataBean) finTranDataObj).merchandiseUnitPrice = unitPrice;
				((AcademyFinTranDataBean) finTranDataObj).merchandiseAmount = String.valueOf(merchandiseAmt);
			}
		}
		catch (Exception e)
		{
			log.error(e);
		}

		if(log.isVerboseEnabled() && null != finTranDataObj) {
			log.verbose("getMerchandiseAmount : bean value - " + ((AcademyFinTranDataBean) finTranDataObj).toString());
		}
		

		return merchandiseAmt;
	}

	/**
	 * Calculates shipping amount for an order line. 
	 * This is (Shipping Charges - Shipping Discounts)
	 * Also sets corresponding fields in finTranDataObj.
	 * @param lineDetailEle
	 * @param finTranDataObj
	 * @param doucmentType
	 * @return
	 */
	public static double getShipingAmount(Element lineDetailEle, Object finTranDataObj, String doucmentType)
	{
		double shippingAmt = 0.0;
		try
		{
			Element lineCharges = (Element) XMLUtil.getNode(lineDetailEle, "LineCharges");
			shippingAmt = getShipingChargeAndDiscount(lineCharges, finTranDataObj, doucmentType);
			log.verbose("getShipingAmount - shipping amout : " + shippingAmt);
			if (finTranDataObj instanceof AcademyFinTranDataBean)
			{
				((AcademyFinTranDataBean) finTranDataObj).shippingAmount = String.valueOf(shippingAmt);
				log.verbose("getShipingAmount bean - " + ((AcademyFinTranDataBean) finTranDataObj).toString());
			}
		}
		catch (Exception e)
		{
			log.error(e);
		}
		return shippingAmt;
	}

	/**
	 * Calculates shipping Charges for OrderLine
	 * Also sets corresponding fields in finTranDataObj.
	 * @param lineDetailEle
	 * @param finTranDataObj
	 * @param doucmentType
	 * @return
	 */
	public static double getShipingCharges(Element lineDetailEle, Object finTranDataObj, String doucmentType)
	{
		double shippingCharge = 0.0;
		try
		{
			Element lineCharges = (Element) XMLUtil.getNode(lineDetailEle, "LineCharges");
			try
			{
				NodeList sChargeListFromCommonCode = getShippingCAndDList(doucmentType);
				shippingCharge = getChargesWithOutDiscountForLine(lineCharges, sChargeListFromCommonCode, false, finTranDataObj);
			}
			catch (Exception e)
			{
				log.error(e);
			}
			if (finTranDataObj instanceof AcademyFinTranDataBean)
			{
				((AcademyFinTranDataBean) finTranDataObj).shippingAmount = String.valueOf(shippingCharge);
			}
		}
		catch (Exception e)
		{
			log.error(e);
		}
		return shippingCharge;
	}

	/**
	 * Calculates merchandise charge total (merchandise charge - merchandise discount)
	 * @param lineCharges
	 * @param finTranDataObj
	 * @param doucmentType
	 * @return
	 */
	public static double getMerchandiseChargeAndDiscount(Element lineCharges, Object finTranDataObj, String doucmentType)
	{
		double merchandiseChargeAndDiscount = 0.0;
		try
		{
			NodeList mChargeListFromCommonCode = getMerchandiseCAndDList(doucmentType);
			merchandiseChargeAndDiscount = getChagerAndDiscountForLine(lineCharges, mChargeListFromCommonCode, true, finTranDataObj);
		}
		catch (Exception e)
		{
			log.error(e);
		}
		return merchandiseChargeAndDiscount;
	}

	/**
	 * Calculates shipping charge total (shipping charge - shipping discount)
	 * @param lineCharges
	 * @param finTranDataObj
	 * @param doucmentType
	 * @return
	 */
	public static double getShipingChargeAndDiscount(Element lineCharges, Object finTranDataObj, String doucmentType)
	{
		double shippingChargeAndDiscount = 0.0;
		try
		{
			NodeList sChargeListFromCommonCode = getShippingCAndDList(doucmentType);
			shippingChargeAndDiscount = getChagerAndDiscountForLine(lineCharges, sChargeListFromCommonCode, false, finTranDataObj);
		}
		catch (Exception e)
		{
			log.error(e);
		}
		return shippingChargeAndDiscount;
	}

	/**
	 * This method actually calculates charges for line. If isMerchantiseLine=true, calculates 
	 * charges for merchandise else for shipping. 
	 * @param lineCharges
	 * @param chargeListFromCommonCode
	 * @param isMerchantiseLine
	 * @param finTranDataObj
	 * @return
	 */
	private static double getChagerAndDiscountForLine(Element lineCharges, NodeList chargeListFromCommonCode, boolean isMerchantiseLine, Object finTranDataObj)
	{
		double chargeAmount = 0.0;
		log.verbose("getChagerAndDiscountForLine - " + XMLUtil.getElementXMLString(lineCharges));
		try
		{
			for (int i = 0; i < chargeListFromCommonCode.getLength(); i++)
			{
				Element chargeFromCommonCode = (Element) chargeListFromCommonCode.item(i);
				
				log.verbose("getChagerAndDiscountForLine - Element numuber " + i + " : " + XMLUtil.getElementXMLString(chargeFromCommonCode));
				Element lineCharge = (Element) XMLUtil.getNode(lineCharges, "LineCharge[@ChargeCategory='"
						+ chargeFromCommonCode.getAttribute("ChargeCategory") + "' and @ChargeName='" + chargeFromCommonCode.getAttribute("ChargeName") + "']");
				if(log.isVerboseEnabled() && !YFCObject.isNull(lineCharge)) {
					log.verbose("Line charge : " + XMLUtil.getElementXMLString(lineCharge));
				}
				
				if (!YFCObject.isNull(lineCharge) && "Y".equalsIgnoreCase(lineCharge.getAttribute("IsBillable")))
				{
					double chargeForCategory = Double.parseDouble(lineCharge.getAttribute("ChargeAmount"));
					log.verbose("Charge for category " + chargeForCategory);
					if ("Y".equalsIgnoreCase(lineCharge.getAttribute("IsDiscount")))
					{
						log.verbose("IsDiscount - Y");
						if (finTranDataObj instanceof AcademyFinTranDataBean)
						{
							log.verbose("Is merchandise line : " + isMerchantiseLine);
							String chargeType = chargeFromCommonCode.getAttribute("FinTranChargeType");
							String strChargeName = chargeFromCommonCode.getAttribute(AcademyConstants.ATTR_CHARGE_NAME);
							log.verbose("charge type : " + chargeType + " :: strChargeName : " + strChargeName);
							log.verbose("##### Acad Fin Trans Bean - " + ((AcademyFinTranDataBean) finTranDataObj).toString() );
							if (isMerchantiseLine)
							{
								if ("OFFER".equalsIgnoreCase(chargeType))
								{
									//Start : Defect-128 : consider PLCC_EveryDay5 discount only for tax calculation
									double dPLCCRewards = Double.parseDouble(((AcademyFinTranDataBean) finTranDataObj).plccRewardPoints);
									log.verbose(" PLCC Rewards : " + dPLCCRewards);
									if(!("PLCC_EveryDay5".equals(strChargeName) && dPLCCRewards != 0 )) {
										
										double merchandiseDiscount = Double.parseDouble(((AcademyFinTranDataBean) finTranDataObj).merchandiseDiscount)
											+ chargeForCategory;
										((AcademyFinTranDataBean) finTranDataObj).merchandiseDiscount = String.valueOf(merchandiseDiscount);
										((AcademyFinTranDataBean) finTranDataObj).merchandiseDiscountReason = lineCharge.getAttribute("Reference");
									}
									//End : Defect-128 : consider PLCC_EveryDay5 discount only for tax calculation
									
								}
								else if ("STORE_COUPON".equalsIgnoreCase(chargeType))
								{
									double merchandiseStoreCoupon = Double.parseDouble(((AcademyFinTranDataBean) finTranDataObj).merchandiseStoreCoupon)
											+ chargeForCategory;
									((AcademyFinTranDataBean) finTranDataObj).merchandiseStoreCoupon = String.valueOf(merchandiseStoreCoupon);
									((AcademyFinTranDataBean) finTranDataObj).merchandiseCouponCode = lineCharge.getAttribute("Reference");
								}
								else if ("MFG_COUPON".equalsIgnoreCase(chargeType))
								{
									double merchandiseMfgCoupon = Double.parseDouble(((AcademyFinTranDataBean) finTranDataObj).merchandiseMfgCoupon)
											+ chargeForCategory;
									((AcademyFinTranDataBean) finTranDataObj).merchandiseMfgCoupon = String.valueOf(merchandiseMfgCoupon);
									((AcademyFinTranDataBean) finTranDataObj).merchandiseCouponCode = lineCharge.getAttribute("Reference");
								}
								else if ("APPEASEMENT".equalsIgnoreCase(chargeType))
								{
									double merchandiseAppeasement = Double.parseDouble(((AcademyFinTranDataBean) finTranDataObj).merchandiseAppeasement)
											+ chargeForCategory;
									((AcademyFinTranDataBean) finTranDataObj).merchandiseAppeasement = String.valueOf(merchandiseAppeasement);
									((AcademyFinTranDataBean) finTranDataObj).merchandiseAppeasementReason = lineCharge.getAttribute("Reference");
								}
								else
								{
									double merchandiseOtherDiscount = Double.parseDouble(((AcademyFinTranDataBean) finTranDataObj).merchandiseOtherDiscount)
											+ chargeForCategory;
									((AcademyFinTranDataBean) finTranDataObj).merchandiseOtherDiscount = String.valueOf(merchandiseOtherDiscount);
									((AcademyFinTranDataBean) finTranDataObj).merchandiseDiscountReason = lineCharge.getAttribute("Reference");
								}
							}
							else
							{
								if ("OFFER".equalsIgnoreCase(chargeType))
								{
									double shippingDiscount = Double.parseDouble(((AcademyFinTranDataBean) finTranDataObj).shippingDiscount)
											+ chargeForCategory;
									((AcademyFinTranDataBean) finTranDataObj).shippingDiscount = String.valueOf(shippingDiscount);
									((AcademyFinTranDataBean) finTranDataObj).shippingDiscountReason = lineCharge.getAttribute("Reference");
								}
								else if ("STORE_COUPON".equalsIgnoreCase(chargeType))
								{
									double shippingStoreCoupon = Double.parseDouble(((AcademyFinTranDataBean) finTranDataObj).shippingStoreCoupon)
											+ chargeForCategory;
									((AcademyFinTranDataBean) finTranDataObj).shippingStoreCoupon = String.valueOf(shippingStoreCoupon);
									((AcademyFinTranDataBean) finTranDataObj).shippingCouponCode = lineCharge.getAttribute("Reference");
								}
								else if ("CARRIER_COUPON".equalsIgnoreCase(chargeType))
								{
									double shippingCarrierCoupon = Double.parseDouble(((AcademyFinTranDataBean) finTranDataObj).shippingCarrierCoupon)
											+ chargeForCategory;
									((AcademyFinTranDataBean) finTranDataObj).shippingCarrierCoupon = String.valueOf(shippingCarrierCoupon);
									((AcademyFinTranDataBean) finTranDataObj).shippingCouponCode = lineCharge.getAttribute("Reference");
								}
								else if ("APPEASEMENT".equalsIgnoreCase(chargeType))
								{
									double shippingAppeasement = Double.parseDouble(((AcademyFinTranDataBean) finTranDataObj).shippingAppeasement)
											+ chargeForCategory;
									((AcademyFinTranDataBean) finTranDataObj).shippingAppeasement = String.valueOf(shippingAppeasement);
									((AcademyFinTranDataBean) finTranDataObj).shippingAppeasementReason = lineCharge.getAttribute("Reference");
								}
								else
								{
									double shippingOtherDiscount = Double.parseDouble(((AcademyFinTranDataBean) finTranDataObj).shippingOtherDiscount)
											+ chargeForCategory;
									((AcademyFinTranDataBean) finTranDataObj).shippingOtherDiscount = String.valueOf(shippingOtherDiscount);
									((AcademyFinTranDataBean) finTranDataObj).merchandiseDiscountReason = lineCharge.getAttribute("Reference");
								}
							}

							log.verbose("##### Fin Trans Bean - " + ((AcademyFinTranDataBean) finTranDataObj).toString());
						}
						chargeForCategory = (-1) * chargeForCategory;
					}
					else
					{
						if (finTranDataObj instanceof AcademyFinTranDataBean)
						{
							log.verbose("IsDiscount - N $$$$ Fin Trans Bean BEFORE - " + ((AcademyFinTranDataBean) finTranDataObj).toString());
							if (isMerchantiseLine)
							{
								double merchantiseCharge = Double.parseDouble(((AcademyFinTranDataBean) finTranDataObj).merchandiseCharge) + chargeForCategory;
								((AcademyFinTranDataBean) finTranDataObj).merchandiseCharge = String.valueOf(merchantiseCharge);
							}
							else
							{
								double shippingCharge = Double.parseDouble(((AcademyFinTranDataBean) finTranDataObj).shippingCharge) + chargeForCategory;
								((AcademyFinTranDataBean) finTranDataObj).shippingCharge = String.valueOf(shippingCharge);
							}
							log.verbose("$$$$ Fin Trans  AFTER - " + ((AcademyFinTranDataBean) finTranDataObj).toString());
						}
					}
					log.verbose("charge amount BEFORE - " + chargeAmount);
					chargeAmount += chargeForCategory;
					log.verbose("charge amount AFTER - " + chargeAmount);
				}
			}
		}
		catch (Exception e)
		{
			log.error(e);
		}
		return chargeAmount;
	}

	/**
	 * This method actually calculates charges for line. If isMerchantiseLine=true, calculates 
	 * charges for merchandise else for shipping. 
	 * @param lineCharges
	 * @param chargeListFromCommonCode
	 * @param isMerchantiseLine
	 * @param finTranDataObj
	 * @return
	 */
	private static double getChargesWithOutDiscountForLine(Element lineCharges, NodeList chargeListFromCommonCode, boolean isMerchantiseLine,
			Object finTranDataObj)
	{
		double chargeAmount = 0.0;
		try
		{
			for (int i = 0; i < chargeListFromCommonCode.getLength(); i++)
			{
				Element chargeFromCommonCode = (Element) chargeListFromCommonCode.item(i);
				Element lineCharge = (Element) XMLUtil.getNode(lineCharges, "LineCharge[@ChargeCategory='"
						+ chargeFromCommonCode.getAttribute("ChargeCategory") + "' and @ChargeName='" + chargeFromCommonCode.getAttribute("ChargeName")
						+ "' and @IsDiscount='N']");
				if (!YFCObject.isNull(lineCharge) && "Y".equalsIgnoreCase(lineCharge.getAttribute("IsBillable")))
				{
					double chargeForCategory = Double.parseDouble(lineCharge.getAttribute("ChargeAmount"));
					if ("Y".equalsIgnoreCase(lineCharge.getAttribute("IsDiscount")))
					{
						if (finTranDataObj instanceof AcademyFinTranDataBean)
						{
							if (isMerchantiseLine)
							{
								if ("OFFER".equalsIgnoreCase(chargeFromCommonCode.getAttribute("FinTranChargeType")))
								{
									double merchandiseDiscount = Double.parseDouble(((AcademyFinTranDataBean) finTranDataObj).merchandiseDiscount)
											+ chargeForCategory;
									((AcademyFinTranDataBean) finTranDataObj).merchandiseDiscount = String.valueOf(merchandiseDiscount);
									((AcademyFinTranDataBean) finTranDataObj).merchandiseDiscountReason = lineCharge.getAttribute("Reference");
								}
								else if ("STORE_COUPON".equalsIgnoreCase(chargeFromCommonCode.getAttribute("FinTranChargeType")))
								{
									double merchandiseStoreCoupon = Double.parseDouble(((AcademyFinTranDataBean) finTranDataObj).merchandiseStoreCoupon)
											+ chargeForCategory;
									((AcademyFinTranDataBean) finTranDataObj).merchandiseStoreCoupon = String.valueOf(merchandiseStoreCoupon);
									((AcademyFinTranDataBean) finTranDataObj).merchandiseCouponCode = lineCharge.getAttribute("Reference");
								}
								else if ("MFG_COUPON".equalsIgnoreCase(chargeFromCommonCode.getAttribute("FinTranChargeType")))
								{
									double merchandiseMfgCoupon = Double.parseDouble(((AcademyFinTranDataBean) finTranDataObj).merchandiseMfgCoupon)
											+ chargeForCategory;
									((AcademyFinTranDataBean) finTranDataObj).merchandiseMfgCoupon = String.valueOf(merchandiseMfgCoupon);
									((AcademyFinTranDataBean) finTranDataObj).merchandiseCouponCode = lineCharge.getAttribute("Reference");
								}
								else if ("APPEASEMENT".equalsIgnoreCase(chargeFromCommonCode.getAttribute("FinTranChargeType")))
								{
									double merchandiseAppeasement = Double.parseDouble(((AcademyFinTranDataBean) finTranDataObj).merchandiseAppeasement)
											+ chargeForCategory;
									((AcademyFinTranDataBean) finTranDataObj).merchandiseAppeasement = String.valueOf(merchandiseAppeasement);
									((AcademyFinTranDataBean) finTranDataObj).merchandiseAppeasementReason = lineCharge.getAttribute("Reference");
								}
								else
								{
									double merchandiseOtherDiscount = Double.parseDouble(((AcademyFinTranDataBean) finTranDataObj).merchandiseOtherDiscount)
											+ chargeForCategory;
									((AcademyFinTranDataBean) finTranDataObj).merchandiseOtherDiscount = String.valueOf(merchandiseOtherDiscount);
									((AcademyFinTranDataBean) finTranDataObj).merchandiseDiscountReason = lineCharge.getAttribute("Reference");
								}
							}
							else
							{
								if ("OFFER".equalsIgnoreCase(chargeFromCommonCode.getAttribute("FinTranChargeType")))
								{
									double shippingDiscount = Double.parseDouble(((AcademyFinTranDataBean) finTranDataObj).shippingDiscount)
											+ chargeForCategory;
									((AcademyFinTranDataBean) finTranDataObj).shippingDiscount = String.valueOf(shippingDiscount);
									((AcademyFinTranDataBean) finTranDataObj).shippingDiscountReason = lineCharge.getAttribute("Reference");
								}
								else if ("STORE_COUPON".equalsIgnoreCase(chargeFromCommonCode.getAttribute("FinTranChargeType")))
								{
									double shippingStoreCoupon = Double.parseDouble(((AcademyFinTranDataBean) finTranDataObj).shippingStoreCoupon)
											+ chargeForCategory;
									((AcademyFinTranDataBean) finTranDataObj).shippingStoreCoupon = String.valueOf(shippingStoreCoupon);
									((AcademyFinTranDataBean) finTranDataObj).shippingCouponCode = lineCharge.getAttribute("Reference");
								}
								else if ("CARRIER_COUPON".equalsIgnoreCase(chargeFromCommonCode.getAttribute("FinTranChargeType")))
								{
									double shippingCarrierCoupon = Double.parseDouble(((AcademyFinTranDataBean) finTranDataObj).shippingCarrierCoupon)
											+ chargeForCategory;
									((AcademyFinTranDataBean) finTranDataObj).shippingCarrierCoupon = String.valueOf(shippingCarrierCoupon);
									((AcademyFinTranDataBean) finTranDataObj).shippingCouponCode = lineCharge.getAttribute("Reference");
								}
								else if ("APPEASEMENT".equalsIgnoreCase(chargeFromCommonCode.getAttribute("FinTranChargeType")))
								{
									double shippingAppeasement = Double.parseDouble(((AcademyFinTranDataBean) finTranDataObj).shippingAppeasement)
											+ chargeForCategory;
									((AcademyFinTranDataBean) finTranDataObj).shippingAppeasement = String.valueOf(shippingAppeasement);
									((AcademyFinTranDataBean) finTranDataObj).shippingAppeasementReason = lineCharge.getAttribute("Reference");
								}
								else
								{
									double shippingOtherDiscount = Double.parseDouble(((AcademyFinTranDataBean) finTranDataObj).shippingOtherDiscount)
											+ chargeForCategory;
									((AcademyFinTranDataBean) finTranDataObj).shippingOtherDiscount = String.valueOf(shippingOtherDiscount);
									((AcademyFinTranDataBean) finTranDataObj).merchandiseDiscountReason = lineCharge.getAttribute("Reference");
								}
							}
						}
						chargeForCategory = (-1) * chargeForCategory;
					}
					else
					{
						if (finTranDataObj instanceof AcademyFinTranDataBean)
						{
							if (isMerchantiseLine)
							{
								double merchantiseCharge = Double.parseDouble(((AcademyFinTranDataBean) finTranDataObj).merchandiseCharge) + chargeForCategory;
								((AcademyFinTranDataBean) finTranDataObj).merchandiseCharge = String.valueOf(merchantiseCharge);
							}
							else
							{
								double shippingCharge = Double.parseDouble(((AcademyFinTranDataBean) finTranDataObj).shippingCharge) + chargeForCategory;
								((AcademyFinTranDataBean) finTranDataObj).shippingCharge = String.valueOf(shippingCharge);
								if ("RETURN_CHARGE".equalsIgnoreCase(chargeFromCommonCode.getAttribute("FinTranChargeType")))
								{
									((AcademyFinTranDataBean) finTranDataObj).returnReasonCode = lineCharge.getAttribute("Reference");
								}

							}
						}
					}
					chargeAmount += chargeForCategory;
				}
			}
		}
		catch (Exception e)
		{
			log.error(e);
		}
		return chargeAmount;
	}

	/**
	 * Reads list of merchandise charges and discounts from common code and returns
	 * element node list
	 * @param documentType
	 * @return
	 */
	private static NodeList getMerchandiseCAndDList(String documentType)
	{
		try
		{
			NodeList mChargeListFromCommonCode = XMLUtil.getNodeList(CHARGE_AND_TAX_DOC, "/AcademyChargesAndTaxs/Order[@DocumentType='" + documentType
					+ "']/OrderLine/Merchandise/LineChareges/LineCharge");
			return mChargeListFromCommonCode;
		}
		catch (Exception e)
		{
			log.error(e);
		}
		return null;
	}

	/**
	 * Reads list of merchandise tax from common code and returns
	 * element node list
	 * @param documentType
	 * @return
	 */
	private static NodeList getMerchandiseTaxList(String documentType)
	{
		try
		{
			NodeList mTaxistFromCommonCode = XMLUtil.getNodeList(CHARGE_AND_TAX_DOC, "/AcademyChargesAndTaxs/Order[@DocumentType='" + documentType
					+ "']/OrderLine/Merchandise/LineTaxs/LineTax");
			return mTaxistFromCommonCode;
		}
		catch (Exception e)
		{
			log.error(e);
		}
		return null;
	}

	/**
	 * Reads list of shipping charges and discounts from common code and returns
	 * element node list
	 * @param documentType
	 * @return
	 */
	private static NodeList getShippingCAndDList(String documentType)
	{
		try
		{
			NodeList sChargeListFromCommonCode = XMLUtil.getNodeList(CHARGE_AND_TAX_DOC, "/AcademyChargesAndTaxs/Order[@DocumentType='" + documentType
					+ "']/OrderLine/Shipping/LineChareges/LineCharge");
			return sChargeListFromCommonCode;
		}
		catch (Exception e)
		{
			log.error(e);
		}
		return null;
	}

	/**
	 * Reads list of shipping tax from common code and returns
	 * element node list
	 * @param documentType
	 * @return
	 */
	private static NodeList getShippingTaxList(String documentType)
	{
		try
		{
			NodeList sTaxistFromCommonCode = XMLUtil.getNodeList(CHARGE_AND_TAX_DOC, "/AcademyChargesAndTaxs/Order[@DocumentType='" + documentType
					+ "']/OrderLine/Shipping/LineTaxs/LineTax");
			return sTaxistFromCommonCode;
		}
		catch (Exception e)
		{
			log.error(e);
		}
		return null;
	}

	/**
	 * Returns Financial transaction date excluding time stamp. 
	 * @param value
	 * @return
	 */
	public static String getTranDate(String value)
	{
		//2010-06-08T00:00:00-00:00
		DateFormat dbDateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		try
		{
			return formatter.format(dbDateTimeFormatter.parse(value));
		}
		catch (ParseException e)
		{
			log.error(e);
		}
		return formatter.format(new Date());
	}

	/**
	 *Code Change for Project: NeXus Integration - Enhancements
	 *Fix provided for the new req Backdated Transaction 06-05-2013
	 * Returns Financial transaction date and time extracted from Transaction Date. 
	 * Transaction Date format 2013-05-02T04:15:48-04:00
	 *
	 * @param value
	 * @return
	 */
	public static String getDateTimeFromTransactionDate(String tranDate, boolean isDateTimeRequired) throws ParseException
	{
		log.beginTimer("getDateTimeFromTransactionDate");

		log.verbose("Transaction Date: " + tranDate);
		log.verbose("isDateTimeRequired: " + isDateTimeRequired);

		DateFormat dbDateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		DateFormat dbDateFormatter = new SimpleDateFormat("yyyyMMdd");
				
		if (!YFCObject.isVoid(tranDate))
		{
       		if (isDateTimeRequired)
				{
				    //Retain only till seconds value in the incoming Transaction Date.
					String dateTimeFromTranDate = tranDate.substring(0, 19);
				    log.verbose("Date Time substring: " +dateTimeFromTranDate);
				    return dateTimeFromTranDate;
								
				}
				else
				{
					log.verbose("Date :" +dbDateFormatter.format(dbDateTimeFormatter.parse(tranDate)));
				    return dbDateFormatter.format(dbDateTimeFormatter.parse(tranDate));
				}
			}
		
		//If Transaction Date is null or empty, then get the system Time.
		if (isDateTimeRequired)
		{
			log.verbose("Date format inside if: " + dbDateTimeFormatter.format(new Date()));
			return dbDateTimeFormatter.format(new Date());
		}
		else
		{
			log.verbose("Date format inside else: " + dbDateFormatter.format(new Date()));
			return dbDateFormatter.format(new Date());
		}
	}

	/**
	 *  Get Financial Transaction code for given transaction type
	 * @param env
	 * @param finTransType
	 * @throws YFSException
	 */
	public static String getAcademyFinTranCode(YFSEnvironment env, String finTransType) throws YFSException
	{
		YFCDocument commonCodeInDoc = YFCDocument.createDocument("CommonCode");
		commonCodeInDoc.getDocumentElement().setAttribute("CodeType", AcademyConstants.FIN_TRAN_TYPE);
		commonCodeInDoc.getDocumentElement().setAttribute("CodeValue", finTransType);
		Document commonCodeOutDoc = null;
		String finTranCode = null;
		try
		{
			commonCodeOutDoc = AcademyUtil.invokeAPI(env, "getCommonCodeList", commonCodeInDoc.getDocument());
			//commonCodeOutDoc = YFCDocument.getDocumentFor(new File("D://Document2.xml")).getDocument();
		}
		catch (Exception e)
		{
			throw new YFSException("Error Invoking getCommonCodeList API");
		}

		NodeList nList = commonCodeOutDoc.getElementsByTagName("CommonCode");
		if (nList.getLength() > 0)
		{
			Element commonCodeEl = (Element) nList.item(0);
			finTranCode = commonCodeEl.getAttribute("CodeShortDescription");
		}

		return finTranCode;
	}

	/**
	 *  Get Financial Transaction Code List
	 * @param env
	 * @param finTransType
	 * @throws YFSException
	 */
	public static Document getAcademyFinTranCodeList(YFSEnvironment env) throws YFSException
	{
		YFCDocument commonCodeInDoc = YFCDocument.createDocument("CommonCode");
		commonCodeInDoc.getDocumentElement().setAttribute("CodeType", AcademyConstants.FIN_TRAN_TYPE);
		Document commonCodeOutDoc = null;
		try
		{
			commonCodeOutDoc = AcademyUtil.invokeAPI(env, "getCommonCodeList", commonCodeInDoc.getDocument());
			//commonCodeOutDoc = YFCDocument.getDocumentFor(new File("D://Document1.xml")).getDocument();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new YFSException("Error Invoking getCommonCodeList API");
		}

		return commonCodeOutDoc;
	}

	/**
	 *  Get Financial Transaction Code for XPath
	 * @param env
	 * @param finTransType
	 * @throws YFSException
	 */
	public static String getAcademyFinTranCodeForXPath(Document financeCodeDoc, String xpath) throws YFSException
	{
		Element financeCodeEl = null;
		String financeCode = null;
		try
		{
			if (financeCodeDoc != null)
			{
				financeCodeEl = (Element) XMLUtil.getNode(financeCodeDoc, "/CommonCodeList/CommonCode[@CodeValue='" + xpath + "']");
				if (financeCodeEl != null)
				{
					financeCode = financeCodeEl.getAttribute("CodeShortDescription");
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new YFSException("Exception occurred while retriving finance code from common code");
		}

		return financeCode;
	}
	
	
	/**
	 * This method is added as part of PLCC Project.
	 * Calculates PLCC Reward Points based on PLCC Payment 
	 * and PLCC_EveryDay5 Charge name or line discount.
	 * 
	 * @param lineDetailEle
	 * @param finTranDataObj
	 * @param doucmentType
	 * @return
	 */
	public static double getPLCCRewardPoints(Element lineDetailEle, Object finTranDataObj, String doucmentType)
	{
		log.verbose("getPLCCRewardPoints : begin");
		double merchandiseAmt = 0.0;
		try {
			
			Element lineCharges = (Element) XMLUtil.getNode(lineDetailEle, "LineCharges");
			
			Element lineCharge = (Element) XMLUtil.getNode(lineCharges, "LineCharge[@ChargeCategory='Promotions' and " +
					"@ChargeName='PLCC_EveryDay5']");
			if(log.isVerboseEnabled() && !YFCObject.isNull(lineCharge)) {
				log.verbose("Line charge : " + XMLUtil.getElementXMLString(lineCharge));
			}
			
			if (!YFCObject.isNull(lineCharge) && "Y".equalsIgnoreCase(lineCharge.getAttribute("IsBillable"))) {
				
				double chargeForCategory = Double.parseDouble(lineCharge.getAttribute("ChargeAmount"));
				log.verbose("Charge for category " + chargeForCategory);
				if ("Y".equalsIgnoreCase(lineCharge.getAttribute("IsDiscount"))) {
					
					log.verbose("IsDiscount - Y");
					if (finTranDataObj instanceof AcademyFinTranDataBean) {
						log.verbose("Updating reward points as  "+ chargeForCategory);
						((AcademyFinTranDataBean) finTranDataObj).plccRewardPoints = String.valueOf(chargeForCategory);
					}
				}
			}
		}
		catch (Exception e) {
			log.error(e);
		}

		if(log.isVerboseEnabled() && null != finTranDataObj) {
			log.verbose("getPLCCRewardPoints : bean value - " + ((AcademyFinTranDataBean) finTranDataObj).toString());
		}
		
		log.verbose("getPLCCRewardPoints : end");
		return merchandiseAmt;
	}

}
