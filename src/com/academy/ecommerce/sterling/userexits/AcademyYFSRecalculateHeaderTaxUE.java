package com.academy.ecommerce.sterling.userexits;

/*##################################################################################
* Project Name                : CO Delivery Fee
* Module                      : OMS
* Author                      : CTS
* Date                        : 01-JULY-2022 
* Description				  : Custom Class to apply Header Tax on Shipment with Taxable item
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author         		Version#       Remarks/Description                      
* ---------------------------------------------------------------------------------
* 01-JULY-2022		CTS  	 			  1.0           	Initial version
* ##################################################################################*/

import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfs.japi.YFSExtnTaxBreakup;
import com.yantra.yfs.japi.YFSUserExitException;
import com.academy.util.constants.AcademyConstants;
import com.yantra.yfs.japi.ue.YFSRecalculateHeaderTaxUE;
import com.yantra.yfs.japi.YFSExtnTaxCalculationOutStruct;
import com.yantra.yfs.japi.YFSExtnHeaderTaxCalculationInputStruct;


public class AcademyYFSRecalculateHeaderTaxUE implements YFSRecalculateHeaderTaxUE {

	private static YFCLogCategory logger = YFCLogCategory.instance(AcademyYFSRecalculateHeaderTaxUE.class.getName());

	private String orderHeaderKey;
	private boolean bForInvoice;
	private boolean bForPacklistPrice;
	private boolean bLastInvoice;
	private boolean hasPendingChanges;
	private String invoiceKey;
	private String sShipNode;
	private String shipToId;
	private String shipToCity;
	private String shipToState;
	private String shipToZipCode;
	private String shipToCountry;
	private String purpose;
	private String enterpriseCode;
	private String documentType;
	private String taxpayerId;
	private String taxJurisdiction;
	private String taxExemptionCertificate;
	private String taxExemptFlag;
	private String invoiceMode;
	private List<YFSExtnTaxBreakup> colCharge;
	private List<YFSExtnTaxBreakup> colTax;
	private double headerShippingCharges;
	private double headerHandlingCharges;
	private double headerPersonalizeCharges;
	private double discountAmount;
	private double tax;
	private double taxPercentage;
	private double totalOriginalChargeAmount;
	private double totalCurrentChargeAmount;
	private Document eMemo;
	private boolean isShipmentInvoice = false;
	private boolean isReturnInvoice = false;
	private boolean isFirstShipment = false;
	private boolean hasHeaderTax = false;
	private boolean hasTaxableItem = false;

	/**
	 * 
	 */
	public AcademyYFSRecalculateHeaderTaxUE() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public YFSExtnTaxCalculationOutStruct recalculateHeaderTax(YFSEnvironment env,
			YFSExtnHeaderTaxCalculationInputStruct headerTaxCalculationInputStruct) throws YFSUserExitException {
		logger.beginTimer("START AcademyYFSRecalculateHeaderTaxUE.recalculateHeaderTax() START");
		YFSExtnTaxCalculationOutStruct outStruct = new YFSExtnTaxCalculationOutStruct();
		headerTaxCalculationInputStruct(headerTaxCalculationInputStruct);
		try {
			Object obj_shipmentForInvoice = env.getTxnObject("ShipmentForInvoice");
			if (YFCCommon.isVoid(obj_shipmentForInvoice)) {
				colTax.stream().forEach(YFSExtnTaxBreakup -> YFSExtnTaxBreakup.tax = 0.00);
				outStruct.colTax = this.colTax;
				outStruct.tax = this.tax;
				outStruct.taxPercentage = this.taxPercentage;
				logger.verbose("No Header TAX ");
				return outStruct;
			} else  {
				if (this.bForInvoice && isShipmentInvoice && hasHeaderTax && isFirstShipment) {
					logger.verbose("** bForInvoice : " + this.bForInvoice +" ** isShipmentInvoice : " + isShipmentInvoice);
					logger.verbose("** hasHeaderTax : " + hasHeaderTax +" ** isFirstShipment : " + isFirstShipment);
					String sDeliveryMethod = null;
					String sShipmentType = null;
					Document doc_shipmentForInvoice = (Document) obj_shipmentForInvoice;
					Element eleShipment = doc_shipmentForInvoice.getDocumentElement();
					sShipmentType = eleShipment.getAttribute(AcademyConstants.Att_ShipmentType);
					sDeliveryMethod = eleShipment.getAttribute(AcademyConstants.Att_DeliveryMethod);
					if (!YFCCommon.isVoid(sShipmentType) && !YFCCommon.isVoid(sDeliveryMethod)
							&& !(sShipmentType).equalsIgnoreCase(AcademyConstants.Str_ShipmentType_STS)
							&& (sDeliveryMethod).equalsIgnoreCase(AcademyConstants.Str_DeliveryMethod_SHP)) {
						hasTaxableItem = shipmentHasTaxableItem(doc_shipmentForInvoice);
						logger.verbose("** hasTaxableItem : " + hasTaxableItem);
						logger.verbose("*** All True *** ");
					}
				}
			}
			
			if (hasTaxableItem) {
				outStruct.colTax = this.colTax;
				outStruct.tax = this.tax;
				outStruct.taxPercentage = this.taxPercentage;
				logger.verbose("Applied Header TAX on the Shipment invoice");
			} else {
				colTax.stream().forEach(YFSExtnTaxBreakup -> YFSExtnTaxBreakup.tax = 0.00);
				outStruct.colTax = this.colTax;
				outStruct.tax = this.tax;
				outStruct.taxPercentage = this.taxPercentage;
				logger.verbose("No Header TAX ");
			}
		} catch (Exception e) {
			throw new YFSException("Exception in applying HeaderTax in AcademyYFSRecalculateHeaderTaxUE :: " + e.getMessage());
		}
		logger.endTimer("END AcademyYFSRecalculateHeaderTaxUE.recalculateHeaderTax() END");
		return outStruct;
	}

	private boolean shipmentHasTaxableItem(Document doc_shipmentForInvoice)	throws Exception {
		boolean shipmentHasTaxableItem = false;
		NodeList OrderLineExthL = XPathUtil.getNodeList(doc_shipmentForInvoice,AcademyConstants.Xpath_ExtnIsDeliveryFeeApplicable);
		if(OrderLineExthL.getLength() > 0) shipmentHasTaxableItem = true;
		return shipmentHasTaxableItem;
	}

	private void headerTaxCalculationInputStruct(
			YFSExtnHeaderTaxCalculationInputStruct headerTaxCalculationInputStruct) {
		this.orderHeaderKey = headerTaxCalculationInputStruct.orderHeaderKey;
		this.bForInvoice = headerTaxCalculationInputStruct.bForInvoice;
		this.bForPacklistPrice = headerTaxCalculationInputStruct.bForPacklistPrice;
		this.bLastInvoice = headerTaxCalculationInputStruct.bLastInvoice;
		this.invoiceKey = headerTaxCalculationInputStruct.invoiceKey;
		this.sShipNode = headerTaxCalculationInputStruct.sShipNode;
		this.shipToId = headerTaxCalculationInputStruct.shipToId;
		this.shipToCity = headerTaxCalculationInputStruct.shipToCity;
		this.shipToState = headerTaxCalculationInputStruct.shipToState;
		this.shipToZipCode = headerTaxCalculationInputStruct.shipToZipCode;
		this.shipToCountry = headerTaxCalculationInputStruct.shipToCountry;
		this.purpose = headerTaxCalculationInputStruct.purpose;
		this.enterpriseCode = headerTaxCalculationInputStruct.enterpriseCode;
		this.documentType = headerTaxCalculationInputStruct.documentType;
		this.taxpayerId = headerTaxCalculationInputStruct.taxpayerId;
		this.taxJurisdiction = headerTaxCalculationInputStruct.taxJurisdiction;
		this.taxExemptionCertificate = headerTaxCalculationInputStruct.taxExemptionCertificate;
		this.taxExemptFlag = headerTaxCalculationInputStruct.taxExemptFlag;
		this.colCharge = headerTaxCalculationInputStruct.colCharge;
		this.colTax = headerTaxCalculationInputStruct.colTax;
		this.headerShippingCharges = headerTaxCalculationInputStruct.headerShippingCharges;
		this.headerHandlingCharges = headerTaxCalculationInputStruct.headerHandlingCharges;
		this.headerPersonalizeCharges = headerTaxCalculationInputStruct.headerPersonalizeCharges;
		this.discountAmount = headerTaxCalculationInputStruct.discountAmount;
		this.hasPendingChanges = headerTaxCalculationInputStruct.hasPendingChanges;
		this.tax = headerTaxCalculationInputStruct.tax;
		this.taxPercentage = headerTaxCalculationInputStruct.taxPercentage;
		this.invoiceMode = headerTaxCalculationInputStruct.invoiceMode;
		this.eMemo = headerTaxCalculationInputStruct.eMemo;
		this.totalOriginalChargeAmount = headerTaxCalculationInputStruct.totalOriginalChargeAmount;
		this.totalCurrentChargeAmount = headerTaxCalculationInputStruct.totalCurrentChargeAmount;
		isShipmentInvoice = !YFCCommon.isVoid(this.invoiceMode)
				&& YFCCommon.equalsIgnoreCase("SHIPMENT", this.invoiceMode);
		isReturnInvoice = !YFCCommon.isVoid(this.invoiceMode) && YFCCommon.equalsIgnoreCase("RETURN", this.invoiceMode);
		hasHeaderTax = this.colTax.size() > 0;
		if (hasHeaderTax) {
			List<YFSExtnTaxBreakup> colTinList = this.colTax;
			isFirstShipment = colTinList.stream()
					.anyMatch(YFSExtnTaxBreakup -> YFSExtnTaxBreakup.tax != YFSExtnTaxBreakup.invoicedTax
							&& YFSExtnTaxBreakup.invoicedTax == 0.00);
		}
		getInputStructValues();
	}

	private void getInputStructValues() {
		logger.verbose("AcademyYFSRecalculateHeaderTaxUE [ isShipmentInvoice : " + isShipmentInvoice
				+ " orderHeaderKey=" + orderHeaderKey + ", bForInvoice=" + bForInvoice + ", bForPacklistPrice="
				+ bForPacklistPrice + ", bLastInvoice=" + bLastInvoice + ", invoiceKey=" + invoiceKey + ", sShipNode="
				+ sShipNode + ", shipToId=" + shipToId + ", shipToCity=" + shipToCity + ", shipToState=" + shipToState
				+ ", shipToZipCode=" + shipToZipCode + ", shipToCountry=" + shipToCountry + ", purpose=" + purpose
				+ ", enterpriseCode=" + enterpriseCode + ", documentType=" + documentType + ", taxpayerId=" + taxpayerId
				+ ", taxJurisdiction=" + taxJurisdiction + ", taxExemptionCertificate=" + taxExemptionCertificate
				+ ", taxExemptFlag=" + taxExemptFlag + ", headerShippingCharges=" + headerShippingCharges
				+ ", headerHandlingCharges=" + headerHandlingCharges + ", headerPersonalizeCharges="
				+ headerPersonalizeCharges + ", discountAmount=" + discountAmount + ", hasPendingChanges="
				+ hasPendingChanges + ", tax=" + tax + ", taxPercentage=" + taxPercentage + ", invoiceMode="
				+ invoiceMode + ", totalOriginalChargeAmount=" + totalOriginalChargeAmount
				+ ", totalCurrentChargeAmount=" + totalCurrentChargeAmount + "]");
	}

}