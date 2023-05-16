package com.academy.ecommerce.sterling.financialtran;

public class AcademyFinTranDataBean {
	
	public String merchandiseAmount = "0.0"; // extendedAmt + Charges - Discount - Store coupon - Appeasement
	public String merchandiseUnitPrice = "0.0"; // Unit price
	public String merchandiseExtendedAmt = "0.0"; // Unit price * quantity
	public String merchandiseDiscount = "0.0"; // Only discounts
	public String merchandiseCharge = "0.0"; // All positive charges - discounts
	public String merchandiseTax = "0.0"; // All positive taxes - tax write off
	public String merchandiseTaxWriteOff = "0.0"; // Only write off amount
	public String merchandiseStoreCoupon = "0.0"; // Store coupon
	public String merchandiseMfgCoupon = "0.0"; // Manufacturer coupon
	public String merchandiseAppeasement = "0.0"; // Appeasement
	public String merchandiseOtherDiscount = "0.0"; // Appeasement
	public String merchandiseCouponCode = "";
	public String merchandiseAppeasementReason = "";
	public String merchandiseDiscountReason = "";
	
	public String shippingAmount = "0.0";
	public String shippingCharge = "0.0";
	public String shippingDiscount = "0.0";
	public String shippingTax = "0.0";
	public String shippingTaxWriteOff = "0.0";
	public String shippingShortSku = "";
	public String shippingStoreCoupon = "0.0";
	public String shippingCarrierCoupon = "0.0";
	public String shippingAppeasement = "0.0";
	public String shippingOtherDiscount = "0.0";
	public String shippingCouponCode = "";
	public String shippingAppeasementReason = "";
	public String shippingDiscountReason = "";
	
	public String qty = "0.0";
	public String sourceLine = "0";
	public String referenceLine = "0";
	public String itemID = "";
	public String itemDescription = "";
	public String scac = "";
	public String carrierService = "";
	public boolean isGiftCard = false;
	public String returnReasonCode = "";
	
	public String plccRewardPoints = "0.0";
	
	/**
	 * Returns line total which is merchandise total + shipping total
	 * @return
	 */
	public String getLineTotal() {
		return String.valueOf(Double.parseDouble(getMerchandiseTotal()) 
				+ Double.parseDouble(getShipmentTotal()));
	}
	
	/**
	 * Returns merchandise total which is merchandise amount (Extended price + chargers - discounts)
	 * + merchandise tax - merchandise tax write off
	 * @return
	 */
	public String getMerchandiseTotal() {
		return String.valueOf(Double.parseDouble(merchandiseAmount) 
				+ Double.parseDouble(merchandiseTax));
	}
	
	/**
	 * Returns shipping total which is shipping amount + shipping tax - shipping tax write off
	 * @return
	 */
	public String getShipmentTotal() {
		return String.valueOf(Double.parseDouble(shippingAmount) 
				+ Double.parseDouble(shippingTax));
	}

	public String getTotalMerchandiseDiscount(boolean includeTaxWriteOff) {
		double discountTotal = Double.parseDouble(merchandiseDiscount) 
			+ Double.parseDouble(merchandiseStoreCoupon)
			+ Double.parseDouble(merchandiseMfgCoupon)
			+ Double.parseDouble(merchandiseAppeasement)
			+ Double.parseDouble(merchandiseOtherDiscount);
		if(includeTaxWriteOff) {
			discountTotal += Double.parseDouble(merchandiseTaxWriteOff);
		}
		return String.valueOf(discountTotal);
	}
	
	public String getTotalShippingDiscount(boolean includeTaxWriteOff) {
		double discountTotal = Double.parseDouble(shippingDiscount) 
			+ Double.parseDouble(shippingStoreCoupon)
			+ Double.parseDouble(shippingCarrierCoupon)
			+ Double.parseDouble(shippingAppeasement)
			+ Double.parseDouble(shippingOtherDiscount);
		if(includeTaxWriteOff) {
			discountTotal += Double.parseDouble(shippingTaxWriteOff);
		}
		return String.valueOf(discountTotal);
	}
	
	public static String getNegativeValue(String value) {
		return ("-" + value);
	}

	public static String getPositiveValue(String value) 
	{
		if(value == null || value.equals(""))
		{
			return value;
		}
		
		double doubleValue = Double.parseDouble(value);
		return String.valueOf(Math.abs(doubleValue));
	}

	@Override
	public String toString()
	{
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("merchandiseAmount = " + merchandiseAmount);
		strBuffer.append(", ");
		strBuffer.append("merchandiseUnitPrice = " + merchandiseUnitPrice);
		strBuffer.append(", ");
		strBuffer.append("merchandiseExtendedAmt = " + merchandiseExtendedAmt );
		strBuffer.append(", ");
		strBuffer.append("merchandiseDiscount = " + merchandiseDiscount);
		strBuffer.append(", ");
		strBuffer.append("merchandiseCharge = " + merchandiseCharge);
		strBuffer.append(", ");
		strBuffer.append("merchandiseTax = " + merchandiseTax);
		strBuffer.append(", ");
		strBuffer.append("merchandiseTaxWriteOff = " + merchandiseTaxWriteOff);
		strBuffer.append(", ");
		strBuffer.append("merchandiseStoreCoupon = " + merchandiseStoreCoupon);
		strBuffer.append(", ");
		strBuffer.append("merchandiseMfgCoupon = " + merchandiseMfgCoupon);
		strBuffer.append(", ");
		strBuffer.append("merchandiseAppeasement = " + merchandiseAppeasement);
		strBuffer.append(", ");
		strBuffer.append("merchandiseOtherDiscount = " + merchandiseOtherDiscount);
		strBuffer.append(", ");
		strBuffer.append("merchandiseCouponCode = " + merchandiseCouponCode);
		strBuffer.append(", ");
		strBuffer.append("merchandiseAppeasementReason = " + merchandiseAppeasementReason);
		strBuffer.append(", ");
		strBuffer.append("merchandiseDiscountReason = " + merchandiseDiscountReason);
		strBuffer.append(", ");
		strBuffer.append("shippingAmount = " + shippingAmount);
		strBuffer.append(", ");
		strBuffer.append("shippingCharge = " + shippingCharge);
		strBuffer.append(", ");
		strBuffer.append("shippingDiscount = " + shippingDiscount);
		strBuffer.append(", ");
		strBuffer.append("shippingTax = " + shippingTax);
		strBuffer.append(", ");
		strBuffer.append("shippingTaxWriteOff = " + shippingTaxWriteOff);
		strBuffer.append(", ");
		strBuffer.append("shippingShortSku = " + shippingShortSku);
		strBuffer.append(", ");
		strBuffer.append("shippingStoreCoupon = " + shippingStoreCoupon);
		strBuffer.append(", ");
		strBuffer.append("shippingCarrierCoupon = " + shippingCarrierCoupon);
		strBuffer.append(", ");
		strBuffer.append("shippingAppeasement = " + shippingAppeasement);
		strBuffer.append(", ");
		strBuffer.append("shippingOtherDiscount = " + shippingOtherDiscount);
		strBuffer.append(", ");
		strBuffer.append("shippingCouponCode = " + shippingCouponCode);
		strBuffer.append(", ");
		strBuffer.append("shippingAppeasementReason = " + shippingAppeasementReason);
		strBuffer.append(", ");
		strBuffer.append("shippingDiscountReason = " + shippingDiscountReason);
		strBuffer.append(", ");
		strBuffer.append("qty = " + qty);
		strBuffer.append(", ");
		strBuffer.append("sourceLine = " + sourceLine);
		strBuffer.append(", ");
		strBuffer.append("referenceLine = " + referenceLine);
		strBuffer.append(", ");
		strBuffer.append("itemID = " + itemID);
		strBuffer.append(", ");
		strBuffer.append("itemDescription = " + itemDescription);
		strBuffer.append(", ");
		strBuffer.append("scac = " + scac);
		strBuffer.append(", ");
		strBuffer.append("carrierService = " + carrierService);
		strBuffer.append(", ");
		strBuffer.append("isGiftCard = " + isGiftCard);
		strBuffer.append(", ");
		strBuffer.append("returnReasonCode = " + returnReasonCode);
		strBuffer.append(", ");
		strBuffer.append("plccRewardPoints = " + plccRewardPoints);
		return strBuffer.toString();
	}
	
	
}
