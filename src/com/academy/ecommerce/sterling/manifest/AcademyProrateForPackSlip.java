package com.academy.ecommerce.sterling.manifest;

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
import com.academy.ecommerce.sterling.util.AcademyServiceUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.common.StringUtil;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyProrateForPackSlip implements YIFCustomApi {

	private Properties props;
	private Element eleContainer = null;
	private Element inDocElem = null;
	private String strInvoiceNo = null;
	private Element eleInvoiceDetail = null;
	private Element eleShipment = null;
	private String strProNumber = null;
	private String strPrintBatchPackSlip="N";
	//Begin : OMNI-3056
	private Document docChargeAndDiscountList = null;
	//End : OMNI-3056
	public void setProperties(Properties props) {
		this.props = props;
	}

	/**
	 * Returns Date in pack slip format.
	 * @param value
	 * @return
	 */
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyProrateForPackSlip.class);

	public Document prorateForPackSlip(YFSEnvironment env, Document inDoc) throws YFCException{
		log.beginTimer(" Begining of AcademyProrateForPackSlip-> prorateForPackSlip Api");
		//Start WN-1000 : [STRETCH] Remove special characters from the Packing Slip
		Element eleToAddress = null;
		Element eleBillToAddress = null;
		//End WN-1000 : [STRETCH] Remove special characters from the Packing Slip
		try{
			inDocElem = inDoc.getDocumentElement();
			log.verbose("&&&&&&&&&&&&&inDoc for prorateForPackSlip &&&&&&&&&&&&&&& " + XMLUtil.getXMLString(inDoc));
			//Begin : OMNI-3056
			setDocumentChargeAndDiscountList();
			//End : OMNI-3056
			if (!YFCObject.isVoid(inDocElem)) {
				eleInvoiceDetail = (Element) XMLUtil.getNode(inDocElem,
				"InputDocument/InvoiceDetail");
				eleContainer = (Element) XMLUtil.getNode(inDocElem,
				"EnvironmentDocument/Container");
				// By default stamp ACAD_PACKSLIP_CONV if gift instruction is there
				// change it to ACAD_PACKSLIP_CONV_GIFT
				if (!YFCObject.isVoid(eleContainer)) {
					strPrintBatchPackSlip = eleContainer.getAttribute("PrintBatchPackSlip");
					if(strPrintBatchPackSlip.equals("Y")){
						eleContainer.setAttribute("PrintDocumentId",
						"ACAD_CONTAINER_PACKSLIP_CONV");
						/*Also check if this is the reprint flow and set the printer id from the environment.
						 * Else stamp Xerox BackUp printer for the invoice*/
						String isReprintFlow = "N";
						if (env.getTxnObject("ReprintPackSlip") != null)
							isReprintFlow = (String) env.getTxnObject("ReprintPackSlip");
						if (isReprintFlow.equals("Y")) {
							log.verbose("*** This is reprint flow so retrive the printer id from environment object*****");
							if (env.getTxnObject("ReprintPrinterId") != null) {
								String strPrinterId = (String) env
								.getTxnObject("ReprintPrinterId");
								eleContainer.setAttribute("PrinterId", strPrinterId);
							}
						}else{
							String strInvoicePrinterId = props.getProperty("PRINTER_ID");
							eleContainer.setAttribute("PrinterId", strInvoicePrinterId);
						}
					}else{
						eleContainer.setAttribute("PrintDocumentId",
						"ACAD_PACKSLIP_CONV");
						String sImageLocation = props.getProperty("Image_Location");
						String sContainerNo = eleContainer.getAttribute("ContainerNo");
						eleContainer.setAttribute("CarrierLabel", sImageLocation + sContainerNo);
					}
				}

				// Call stamp marketing lines method to get marketing lines from
				// common code.
				AcademyStampFileLocation ac = new AcademyStampFileLocation();
				ac.stampMarketingLines(env, eleContainer);

				// Check For Gift Instruction
				checkGiftInstruction(env, eleContainer);

				// Call Invoice No UE to create Shipment Invoice
				eleShipment = (Element) XMLUtil.getNode(inDocElem, "EnvironmentDocument/Container/Shipment");

				/*
				 * Fix for 12716. For reprint/repack scenario, check if Invoice# already exists and if true,
				 * do not call callGetInvoiceNoUE
				 *
				 */

				//Extract the invoice number from Order Invoice table

				/*Changes by Zubair*/
				strInvoiceNo = XMLUtil.getString(eleInvoiceDetail,
				"InvoiceHeader/Extn/@ExtnInvoiceNo");

				log.verbose("****Invoice number retrieved to be stamped on packslip*****" + strInvoiceNo);

				/*Changes by Zubair*/

				//this method checks if the re-packing is done on same day or not.
				//for same date, use the same invoice number.
				//for different date, generate new invoice number
				/*Changes by Zubair*/
				//boolean generateInvoiceNo = createInvoiceNumber(strExtnInvoiceNo);
				/*Changes by Zubair*/

				/* Logic for generating Invoice No
				 * Generate the invoice number only for the first container of the shipment.
				 * Use the same invoice number for all the containers.
				 * If the shipment is unpacked and re-packed then
				 * 		-If repacking is done on the same day, use the existing invoice number
				 * 		-If repacking is done on different day, create new invoice number.
				 */

				//if (!YFCObject.isVoid(eleShipment) && YFCObject.isVoid(strExtnInvoiceNo)) {


				/*Changes by Zubair*/
				/*				if (!YFCObject.isVoid(eleShipment)) {
					log.verbose("Invoice number does exist in Indoc InvoiceDetails");
					//Change for CR - Reprint PackSlip Flow
					//If the API is invoking for reprint then don't call GetInvoiceNoUE
					String isReprintFlow = "N";
					if(env.getTxnObject("ReprintPackSlip")!=null)
						isReprintFlow = (String)env.getTxnObject("ReprintPackSlip");
					log.verbose("isReprintFlow : "+isReprintFlow+" actual InvoiceNo is : "+strExtnInvoiceNo);
					if(isReprintFlow.equals("Y")){
						log.verbose("*** This is reprint flow*****");
						if(env.getTxnObject("ReprintPrinterId")!=null){
							String strPrinterId = (String)env.getTxnObject("ReprintPrinterId");
							eleContainer.setAttribute("PrinterId", strPrinterId);
							}
						strInvoiceNo = strExtnInvoiceNo;
					}else{	
						if (YFCObject.isNull(strExtnInvoiceNo)) {
							callGetInvoiceNoUE(env, inDoc, eleShipment);
						} else if (generateInvoiceNo) {
							callGetInvoiceNoUE(env, inDoc, eleShipment);
						} else {
							strInvoiceNo = strExtnInvoiceNo;
						}
					}
				}*/
				/*Changes by Zubair*/
				// Get List of each container line. It will have each item as one container detail element

				calculateDataForPackSlip(env, inDocElem);
				
				//Start WN-1000 : [STRETCH] Remove special characters from the Packing Slip		
				eleToAddress = (Element) XMLUtil.getNode(inDoc,AcademyConstants.XPATH_CONTAINER_SHIPMENT_TOADDRESS);
				eleBillToAddress = (Element) XMLUtil.getNode(inDoc,AcademyConstants.XPATH_CONTAINER_SHIPMENT_BILLTOADDRESS);		
				AcademyUtil.convertUnicodeToSpecialChar(env, eleToAddress, eleBillToAddress, false);
				//End WN-1000 : [STRETCH] Remove special characters from the Packing Slip

			}
		}catch(Exception ex){
			ex.printStackTrace();
			log.verbose(ex);
			throw new YFCException(ex);
		}
		log.endTimer(" End of AcademyProrateForPackSlip-> prorateForPackSlip Api");

		return inDoc;
	}


	public void calculateDataForPackSlip(YFSEnvironment env, Element inDocElem)	throws Exception {
		log.beginTimer(" Begining of AcademyProrateForPackSlip-> calculateDataForPackSlip Api");
		//BOPIS-1584 : Begin
		String strWebStoreFlow = (String) env.getTxnObject(AcademyConstants.A_IS_WEB_STORE_FLOW);
		//BOPIS-1584 : End
		Element eContainerDetail = null;
		Element eShipmentLine = null;
		Element eleOrderLine = null;
		Element LineCharges = null;
		Element LineTaxes = null;
		Element LineCharge = null;
		double totalShippingCharge = 0.00;
		double totalTax = 0.00;
		String strUnitPrice = "0.00";
		String strExtendedPrice = "0.00";
		String strOrderedPricingQty = "0.00";
		double ShippingChargeForLine = 0.00;		
		double tax = 0.00;
		String sContainerQty = "0.00";
		double totalDiscount = 0.00;
		double extendedPriceTotal = 0.00;
		double containerExtendedPrice = 0.00;
		//START STL-1711 Moved outside of the loop
		double itemTaxAtLineLevel = 0.00;
		double shippingTaxAtLineLevel = 0.00;
		double discountForLine = 0.00;
		double shippingChargeAtLineLevel=0.00;
		String itemDiscType = "";
		//END STL-1711 Moved outside of the loop
		final String XPATH_OVER_ALL_TOTALS = "InputDocument/InvoiceDetail/InvoiceHeader/Order/OverallTotals";//added for STL-1279.

		Element eleInvoiceheader = (Element) XMLUtil.getNode(inDocElem,	"InputDocument/InvoiceDetail/InvoiceHeader");
		Element eleContainerDetails = XMLUtil.getFirstElementByName(inDocElem, "EnvironmentDocument/Container/ContainerDetails");
		Element eleInvoiceLineDetails = XMLUtil.getFirstElementByName(eleInvoiceheader, "LineDetails");
		Element eleContainer = XMLUtil.getFirstElementByName(inDocElem, "EnvironmentDocument/Container");


		//START STL-1279. Adding $ for pack slip.
		Element eleOverallTotals = (Element) XMLUtil.getNode(inDocElem,	XPATH_OVER_ALL_TOTALS);		
		if(!YFSObject.isVoid(eleOverallTotals)) 
		{
			String strGrandTotal =eleOverallTotals.getAttribute(AcademyConstants.ATTR_GRAND_TOTAL);
			if(!YFSObject.isVoid(strGrandTotal) && !strGrandTotal.contains(AcademyConstants.STR_DOLLAR)){
				eleOverallTotals.setAttribute(AcademyConstants.ATTR_GRAND_TOTAL, AcademyConstants.STR_DOLLAR + strGrandTotal);
			}
		}
		//END STL-1279. Adding $ for pack slip.

		DecimalFormat decimalCorrection = new DecimalFormat("0.00");
		setDataInPackSlipFormat(env, eleContainerDetails, eleContainer);


		if (!YFCObject.isVoid(eleContainerDetails)) {
			NodeList nListContainer = XMLUtil.getNodeList(eleContainerDetails, "ContainerDetail");
			if (!YFCObject.isVoid(nListContainer)) {
				int iLength = nListContainer.getLength();
				for (int i = 0; i < iLength; i++) {
					eContainerDetail = (Element) nListContainer.item(i);
					if (!YFCObject.isVoid(eContainerDetail)) {

						sContainerQty = eContainerDetail.getAttribute("Quantity");
						double dContQty = Double.parseDouble(sContainerQty);
						int iContQty = (int) dContQty;
						eContainerDetail.setAttribute("Quantity", Integer.toString(iContQty));

						Element shipLineEle = (Element) eContainerDetail.getElementsByTagName("ShipmentLine").item(0);
						String shipQty = shipLineEle.getAttribute("Quantity");
						double dShipQty = Double.parseDouble(shipQty);
						int iShipQty = (int) dShipQty;
						shipLineEle.setAttribute("Quantity", Integer.toString(iShipQty));

						eShipmentLine = XMLUtil.getFirstElementByName(eContainerDetail, "ShipmentLine");
						//double shippingChargeAtLineLevel = 0.00;
						//double discountForLine = 0.00;
						//CR#38 setting the static fields for display on the pack slip
						setStaticFieldsOnConvPackSlip(env, eContainerDetail);
						//end changes for CR#38
						if (!YFCObject.isVoid(eShipmentLine)) {
							eleOrderLine = XMLUtil.getFirstElementByName(eShipmentLine, "OrderLine");
							if (!YFCObject.isVoid(eleOrderLine)) {
								eleOrderLine.setAttribute("USD","$");
								String strOrderLineKey = eleOrderLine.getAttribute("OrderLineKey");
								/** ********Start Bug Fix 9607 ******************* */
								Element eleOrder = XMLUtil.getFirstElementByName(eleOrderLine,"Order");
								Element elePaymentMethods = XMLUtil.getFirstElementByName(eleOrder,"PaymentMethods");
								NodeList nPaymentMethod = XMLUtil.getNodeList(elePaymentMethods, "PaymentMethod");
								if (!YFCObject.isVoid(nPaymentMethod)) {
									for (int j = 0; j < nPaymentMethod.getLength(); j++) {
										Element elePaymentMethod=(Element) nPaymentMethod.item(j);
										String strDisplayCreditCardNo = elePaymentMethod.getAttribute("DisplayCreditCardNo");
										String strCreditCardType= elePaymentMethod.getAttribute("CreditCardType");
										String strSvcNo=elePaymentMethod.getAttribute("DisplaySvcNo");
										elePaymentMethod.setAttribute("PaymentMethodCurrency", "$");
										String strPaymentType = elePaymentMethod.getAttribute("PaymentType");

										if (!YFCObject.isNull(strPaymentType)) {
											//KER-12036 : Payment Migration Changes to support new Payment Type
											//OMNI-31625: Tender type for PLCC, ApplePay, GooglePay - START
											if (AcademyConstants.PAYPAL.equalsIgnoreCase(strPaymentType)) {
												strCreditCardType=strPaymentType;
											}
											log.verbose("AcademyProrateForPackSlip-calculateDataForPackSlip - strPaymentType :: "+strPaymentType);
											log.verbose("AcademyProrateForPackSlip-calculateDataForPackSlip - strCreditCardType :: "+strCreditCardType);
											String strCommonCodeCCType = getCommonCodeForCrediCardType(env, strCreditCardType);
											log.verbose("AcademyProrateForPackSlip-calculateDataForPackSlip - strCommonCodeCCType :: "+strCommonCodeCCType);
											/* if ("CREDIT_CARD".equalsIgnoreCase(strPaymentType)){ */											
											if(AcademyConstants.CREDIT_CARD.equalsIgnoreCase(strPaymentType)
												|| AcademyConstants.STR_APPLE_PAY_PAYMENT.equalsIgnoreCase(strPaymentType) 
												|| AcademyConstants.STR_GOOGLE_PAY_PAYMENT.equalsIgnoreCase(strPaymentType)
												|| AcademyConstants.STR_PLCC_PAYMENT.equalsIgnoreCase(strPaymentType)													
												) {
												//OMNI-31625: Tender type for PLCC, ApplePay, GooglePay - END
												//START STL-1279. Adding bracket conditionally to avoid multiple bracket for Bulk pack slip.
												//elePaymentMethod.setAttribute("DisplayCreditCardNo", "(" + strDisplayCreditCardNo  + ")");
												if(!YFSObject.isVoid(strDisplayCreditCardNo) && !strDisplayCreditCardNo.contains(AcademyConstants.STR_OPEN_BRACKET) ){
													elePaymentMethod.setAttribute(AcademyConstants.ATTR_DISPLAY_CREDIT_CARD_NO, AcademyConstants.STR_OPEN_BRACKET + strDisplayCreditCardNo  + AcademyConstants.STR_CLOSE_BRACKET);
												}else{
													elePaymentMethod.setAttribute(AcademyConstants.ATTR_DISPLAY_CREDIT_CARD_NO, strDisplayCreditCardNo );
												}
												//END STL-1279. Adding bracket conditionally to avoid multiple bracket for Bulk pack slip.
												//elePaymentMethod.setAttribute("CreditCardType",	strCreditCardType);
												elePaymentMethod.setAttribute("CreditCardType",	strCommonCodeCCType);											
												elePaymentMethod.setAttribute("AmountCharged", elePaymentMethod.getAttribute("TotalAuthorized"));
											} 
											// Start :: PayPal Integration
											else if (AcademyConstants.PAYPAL.equalsIgnoreCase(strPaymentType)) {//KER-11461 change equals to equalsIgnoreCase.  to support new Paypal payment type
												elePaymentMethod.setAttribute("DisplayCreditCardNo","");
												//Hard coding the creditCardType to PayPal
												//elePaymentMethod.setAttribute("CreditCardType",strCreditCardType);
												//OMNI-31625: Start
												//elePaymentMethod.setAttribute("CreditCardType", AcademyConstants.PAYPAL);
												//elePaymentMethod.setAttribute("CreditCardType", AcademyConstants.PACK_SLIP_PAYPAL);
												elePaymentMethod.setAttribute("CreditCardType", strCommonCodeCCType);
												//OMNI-31625: End
												elePaymentMethod.setAttribute("AmountCharged",elePaymentMethod.getAttribute("TotalAuthorized"));
											}
											// End :: PayPal Integration
											else {
												elePaymentMethod.setAttribute("DisplayCreditCardNo", "(" + strSvcNo + ")");
												//OMNI-31625: Start
												//elePaymentMethod.setAttribute("CreditCardType", "GC");
												elePaymentMethod.setAttribute("CreditCardType", strCommonCodeCCType);
												//OMNI-31625: End
												elePaymentMethod.setAttribute("AmountCharged", elePaymentMethod.getAttribute("TotalCharged"));
											}
										}
									}
								}
								//START STL-1711 Tax details are not correct in Packing slip 
								NodeList iLineDetail = XMLUtil.getNodeList(eleInvoiceLineDetails, AcademyConstants.ELEM_LINE_DETAIL); 
								if (!YFCObject.isVoid(iLineDetail)) {
									for (int j = 0; j < iLineDetail.getLength(); j++) {
										Element iLineDetailEle = (Element) iLineDetail.item(j);
										// Fix For STL-240- Starts//										
										//LineCharges = XMLUtil.getFirstElementByName(iLineDetailEle, "LineCharges");
										// Fix For STL-240- Ends//
										// Fix For Defect#71- Starts//											
										/*LineCharges = XMLUtil.getFirstElementByName(iLineDetailEle, "OrderLine/LineCharges");										 
										LineTaxes = XMLUtil.getFirstElementByName(iLineDetailEle, "OrderLine/LineTaxes");*/
										// Fix For Defect#71- Ends//
										//LineTaxes = XMLUtil.getFirstElementByName(iLineDetailEle, "LineTaxes");
										//END STL-1711 Tax details are not correct in Packing slip 
										String strOrderlineKey = iLineDetailEle.getAttribute("OrderLineKey");
										if (strOrderLineKey.equals(strOrderlineKey)) {
											shippingTaxAtLineLevel = 0.00;
											itemTaxAtLineLevel = 0.00;
											itemDiscType = "";
											//START STL-1711 Tax details are not correct in Packing slip 
											shippingChargeAtLineLevel=0.00;
											discountForLine = 0.00;
											
											strUnitPrice = ((Element) iLineDetail.item(j)).getAttribute(AcademyConstants.ATTR_UNIT_PRICE);
											strExtendedPrice = ((Element) iLineDetail.item(j)).getAttribute(AcademyConstants.ATTR_EXTENDED_PRICE);
											containerExtendedPrice = Double.valueOf(strUnitPrice) * Double.valueOf(sContainerQty);
											extendedPriceTotal += containerExtendedPrice;
											strOrderedPricingQty = ((Element) iLineDetail.item(j)).getAttribute(AcademyConstants.ATTR_RTN_SHIPPED_QTY);
											eleOrderLine.setAttribute(AcademyConstants.ATTR_UNIT_PRICE, AcademyConstants.STR_DOLLAR + strUnitPrice);
											eleOrderLine.setAttribute(AcademyConstants.ATTR_EXTENDED_PRICE, AcademyConstants.STR_DOLLAR + decimalCorrection.format(containerExtendedPrice));
											// Start OMNI-1774 Incorrect Shipping Charges on PackingSlips
											/*Element eleLineChargeList = (Element) iLineDetailEle.getElementsByTagName(AcademyConstants.ELEMENT_LINE_CHARGE_LIST).item(0);
											
											//BOPIS-1584 : Begin
											*//**
											 * If the pack slip is printed from web store, use LineCharges element instead of LineChargeList.
											 * This is due to the fact that the IsDiscount Attribute is available at LineCharges level for 9.5
											 *//*
											if(YFCCommon.equalsIgnoreCase("Y",strWebStoreFlow))
											{
												log.debug("Chnaging to LineCharges for Web Store" + strWebStoreFlow);
												eleLineChargeList = (Element) XMLUtil.getNode(iLineDetailEle,
														"LineCharges");
											}*/
											//BOPIS-1584 : End
											// Start OMNI-1774 Incorrect Shipping Charges on PackingSlips
											Element eleLineChargeList = (Element) XMLUtil.getNode(iLineDetailEle,
													"LineCharges");
											// End OMNI-1774 Incorrect Shipping Charges on PackingSlips
											if (!YFCObject.isVoid(eleLineChargeList))
						                	{
												
											NodeList nLineCharge = XMLUtil.getNodeList(eleLineChargeList, AcademyConstants.ELE_LINE_CHARGE);
											for (int iLineChargeCunt = 0; iLineChargeCunt < nLineCharge.getLength(); iLineChargeCunt++) {
											//END STL-1711 Tax details are not correct in Packing slip 
												Element eleLineCharge = (Element)nLineCharge.item(iLineChargeCunt);
												String isDiscount = eleLineCharge.getAttribute(AcademyConstants.ATTR_IS_DISCOUNT);
												String chargeAmount = eleLineCharge.getAttribute(AcademyConstants.ATTR_CHARGE_AMT);
												String chargeName = eleLineCharge.getAttribute(AcademyConstants.ATTR_CHARGE_NAME);
												
												//START STL-1711 Tax details are not correct in Packing slip 
												/*strUnitPrice = ((Element) iLineDetail.item(j)).getAttribute("UnitPrice");
												strExtendedPrice = ((Element) iLineDetail.item(j)).getAttribute("ExtendedPrice");
												containerExtendedPrice = Double.valueOf(strUnitPrice) * Double.valueOf(sContainerQty);
												extendedPriceTotal += containerExtendedPrice;
												strOrderedPricingQty = ((Element) iLineDetail.item(j)).getAttribute("ShippedQty");
												eleOrderLine.setAttribute("UnitPrice", "$" + strUnitPrice);
												eleOrderLine.setAttribute("ExtendedPrice", "$" + decimalCorrection.format(containerExtendedPrice));*/
												/*if (!YFCObject.isVoid(LineCharges)) {
												NodeList iLineCharge = XMLUtil.getNodeList(LineCharges, "LineCharge");
												for (int k = 0; k < iLineCharge.getLength(); k++) {
													LineCharge = (Element) iLineCharge.item(k);
													String isDiscount = LineCharge.getAttribute("IsDiscount");
													String chargeAmount = LineCharge.getAttribute("ChargeAmount");
													String chargeName = LineCharge.getAttribute("ChargeName");*/
												//if (eleLineCharge.getAttribute("ChargeName").contains("Shipping"))
												if (chargeName.contains(AcademyConstants.STR_SHIPPING)){
													//String strShippingChargeAtLineLevel = eleLineCharge.getAttribute("ChargeAmount");
												//END STL-1711 Tax details are not correct in Packing slip 
													String strShippingChargeAtLineLevel = chargeAmount;
													if (AcademyConstants.STR_YES.equals(eleLineCharge.getAttribute(AcademyConstants.ATTR_IS_DISCOUNT))) {
														shippingChargeAtLineLevel = shippingChargeAtLineLevel -
														Double.parseDouble(strShippingChargeAtLineLevel);
													} else {
														shippingChargeAtLineLevel = shippingChargeAtLineLevel +
														Double.parseDouble(strShippingChargeAtLineLevel);
													}
												}
												
												// Begin : OMNI-3056
												Element eleChargesAndTaxes = getMerchandiseCAndDList(
															AcademyConstants.SALES_DOCUMENT_TYPE);
												log.verbose("ChargesAndTeaxesDoc:: "
															+ XMLUtil.getElementXMLString(eleChargesAndTaxes));													
												Document docTemp = XMLUtil.createDocument("Order");
												Element eleTemp = docTemp.getDocumentElement();
												
												Element eleTempChargesAndTaxes = (Element) docTemp
														.importNode(eleChargesAndTaxes, true);
												eleTemp.appendChild(eleTempChargesAndTaxes);
												Element elemLineCharge = (Element)XPathUtil.getNode(eleTempChargesAndTaxes,
														"//LineChareges/LineCharge[@ChargeName='"+ chargeName + "']");

												//if (AcademyConstants.STR_BOGO.equals(chargeName) || (AcademyConstants.STR_DISCOUNT_COUPON.equals(chargeName)) || chargeName.startsWith("PLCC_")) {
												 if (!YFCObject.isNull(elemLineCharge) && !YFCObject.isVoid(elemLineCharge)) {
														log.verbose("eleLineCharge :: "+
																XMLUtil.getElementXMLString(elemLineCharge));
												// End : OMNI-3056
													 if (AcademyConstants.STR_YES.equals(isDiscount)) {
													discountForLine = discountForLine + Double.parseDouble(chargeAmount);
													//itemDiscType = getItemDiscTypeNames(chargeName, itemDiscType, iLineCharge, k);
													itemDiscType = getItemDiscTypeNames(chargeName, itemDiscType, nLineCharge, iLineChargeCunt);//As a part of STL-1711
													}
												}
											}
										}

											discountForLine = (discountForLine / Double.parseDouble(strOrderedPricingQty))* Double.parseDouble(sContainerQty);
											shippingChargeAtLineLevel = (shippingChargeAtLineLevel / Double.parseDouble(strOrderedPricingQty))* Double.parseDouble(sContainerQty);
										
											totalDiscount += discountForLine;
											totalShippingCharge = totalShippingCharge + shippingChargeAtLineLevel; //As a part of STL-1711
											String disForLine = decimalCorrection.format(discountForLine);
											eleOrderLine.setAttribute("ShippingChargeAtLineLevel", "$" + decimalCorrection.format(shippingChargeAtLineLevel));
											eleOrderLine.setAttribute("DiscountForLine", "($" + disForLine + ")");//changed for CR#38

											if (itemDiscType.endsWith("/")) {
												itemDiscType = itemDiscType.substring(0, (itemDiscType.length()-1));
											}

											eleOrderLine.setAttribute("ItemDiscDesc", itemDiscType);
											//START STL-1711 Tax details are not correct in Packing slip 
											
											Element eleLineTaxList = (Element) iLineDetailEle.getElementsByTagName(AcademyConstants.ELEMENT_LINE_TAX_LIST).item(0);
											//START BOPIS-1277: Use LineTaxes if LineTaxList Is not present
											if(YFCObject.isVoid(eleLineTaxList))
											{
											 eleLineTaxList = (Element) iLineDetailEle.getElementsByTagName("LineTaxes").item(0);
											}
											//END BOPIS-1277: Use LineTaxes if LineTaxList Is not present
																			
											if (!YFCObject.isVoid(eleLineTaxList))
						                	{
											NodeList nLineTax = XMLUtil.getNodeList(eleLineTaxList, AcademyConstants.ELE_LINE_TAX);
											for (int iLineTaxCunt = 0; iLineTaxCunt < nLineTax.getLength(); iLineTaxCunt++) {
												Element eleLineTax = (Element)nLineTax.item(iLineTaxCunt);
												String chargeCategory = eleLineTax.getAttribute(AcademyConstants.ATTR_CHARGE_CATEGORY);						                															
												/*if (!YFCObject.isVoid(LineTaxes)) {
													NodeList nLineTax = XMLUtil.getNodeList(LineTaxes,"LineTax");

														for (int k = 0; k < nLineTax.getLength(); k++) {
															Element eleLineTax = (Element) nLineTax.item(k);
															String chargeCategory =  eleLineTax.getAttribute("ChargeCategory");*/
												//END STL-1711 Tax details are not correct in Packing slip 
												if (AcademyConstants.STR_SHIPPING.equals(chargeCategory)) {
													String strShippingTaxAtLineLevel = eleLineTax.getAttribute(AcademyConstants.ATTR_TAX);
													shippingTaxAtLineLevel += Double.parseDouble(strShippingTaxAtLineLevel);
													log.verbose("****** inside iff shippingTaxAtLineLevel ::" + shippingTaxAtLineLevel);
													// Fix for #3678 - Add shipping taxes outside of loop
													//tax += shippingTaxAtLineLevel;
												}
												if (AcademyConstants.STR_TAXES.equals(chargeCategory)) {
													String strItemTaxAtLineLevel = eleLineTax.getAttribute(AcademyConstants.ATTR_TAX);
													itemTaxAtLineLevel += Double.parseDouble(strItemTaxAtLineLevel);
													// Fix for #3678 - Add taxes outside of loop
													//tax += itemTaxAtLineLevel;
												}
											}
						                	}
											// Fix for #3678 - Add shipping taxes and taxes
											//tax = tax + shippingTaxAtLineLevel + itemTaxAtLineLevel;
											//tax = (((tax) / Double.parseDouble(strOrderedPricingQty))* Double.parseDouble(sContainerQty));
										}

									}
									itemTaxAtLineLevel = (itemTaxAtLineLevel / Double.parseDouble(strOrderedPricingQty))* Double.parseDouble(sContainerQty);
									shippingTaxAtLineLevel = (shippingTaxAtLineLevel / Double.parseDouble(strOrderedPricingQty))* Double.parseDouble(sContainerQty);
									log.verbose("****** shippingTaxAtLineLevel ::" + shippingTaxAtLineLevel);
									//Fix for Issue #3678, corrected the proration logic
									tax = tax + shippingTaxAtLineLevel + itemTaxAtLineLevel;
									log.verbose("****** tax ::" + tax);
									eleOrderLine.setAttribute("ShippingTaxLineLevel", "$" + decimalCorrection.format(shippingTaxAtLineLevel));
									eleOrderLine.setAttribute("ItemTaxLineLevel", "$" + decimalCorrection.format(itemTaxAtLineLevel));
								} 
							}
						}
					}
				}
				//checkForLastContainer(eleContainer, discountForLine, shippingChargeAtLineLevel, tax, strOrderedPricingQty, sContainerQty);
				totalTax = totalTax + tax;
				log.verbose("****** totalTax ::" + totalTax);
				//totalShippingCharge = totalShippingCharge + shippingChargeAtLineLevel; //As a part of STL-1711
			}
		}

		eleContainerDetails.setAttribute("TotalExtendedPrice", "$" + decimalCorrection.format(extendedPriceTotal));
		eleContainerDetails.setAttribute("TotalTax", "$" + decimalCorrection.format(tax));
		eleContainerDetails.setAttribute("ShippingCharges", "$" + decimalCorrection.format(totalShippingCharge));
		eleContainerDetails.setAttribute("TotalDiscount", "($" + decimalCorrection.format(totalDiscount) + ")");
		double cartonTotal = extendedPriceTotal + tax + totalShippingCharge - totalDiscount;
		eleContainerDetails.setAttribute("CartonTotal", "$" + decimalCorrection.format(cartonTotal));

		eleContainerDetails.setAttribute("InvoiceNo", strInvoiceNo);
		eleContainerDetails.setAttribute("ProNumber", strProNumber);

		// FIX for defect #3003 - Conveyable Packing List printed a second blank page
		movePaymentMethodsNodeOutside(inDocElem);

		log.endTimer(" End of AcademyProrateForPackSlip-> calculateDataForPackSlip Api");
	}
	/*// FIX for defect #3003 - Conveyable Packing List printed a second blank page
		movePaymentMethodsNodeOutside(inDocElem);

		log.endTimer(" End of AcademyProrateForPackSlip-> calculateDataForPackSlip Api");
	}*/

	
	
	//OMNI-31625: Tender type for PLCC, ApplePay, GooglePay - START
	/**
	 * This method return the common code short description value.
	 * @param env
	 * @param strCreditCardType
	 * @return
	 */
	/**inDoc:
	 * <CommonCode CodeType="PACK_SLIP_PAY_TYPE" CodeValue="MasterCard"/>
	 * 
		outDoc:
		<CommonCodeList>
			<CommonCode CodeLongDescription="MASTER" CodeShortDescription="MASTER" CodeType="PACK_SLIP_PAY_TYPE" CodeValue="MasterCard"/>
		</CommonCodeList>
	 *
	 */
	private String getCommonCodeForCrediCardType(YFSEnvironment env, String strCreditCardType) {
		// TODO Auto-generated method stub
		log.verbose( "AcademyProrateForPackSlip-getCommonCodeForCrediCardType :: "+  strCreditCardType);
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
			log.verbose( "AcademyProrateForPackSlip-getCommonCodeForCrediCardType - outDocGetCommonCodeList :: "+  XMLUtil.getXMLString(outDocGetCommonCodeList));
			if(!YFCObject.isVoid(outDocGetCommonCodeList) && 
					outDocGetCommonCodeList.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_COMMON_CODE).getLength() > 0)
			{			
				Element eleCommonCode = XMLUtil.getElementByXPath(outDocGetCommonCodeList, AcademyConstants.XPATH_COMMON_CODE_ELE);
				strCommonCodeCCType = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
				log.verbose( "AcademyProrateForPackSlip-getCommonCodeForCrediCardType - strCommonCodeCCType :: "+  strCommonCodeCCType);
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
	 * MOVE PaymentMethods node
	 * FROM Container/ContainerDetails/Container/ShipmentLine/OrderLine/Order/PaymentMethods
	 * TO Container/Shipment/PaymentMethods.
	 *
	 * @param inDocElem
	 */
	private void movePaymentMethodsNodeOutside(Element inDocElem)
	{
		log.beginTimer(" End of AcademyProrateForPackSlip-> movePaymentMethodsNodeOutside Api");
		log.verbose("********* input to movePaymentMethodsNodeOutside - " + XMLUtil.getElementXMLString(inDocElem));

		Element shipmentElement = XMLUtil.getFirstElementByName(inDocElem, "EnvironmentDocument/Container/Shipment");
		if (!YFCObject.isVoid(shipmentElement))
		{
			Element eleContainerDetails = XMLUtil.getFirstElementByName(inDocElem, "EnvironmentDocument/Container/ContainerDetails");
			if (!YFCObject.isVoid(eleContainerDetails))
			{
				try
				{
					log.verbose("********* Container details - " + XMLUtil.getElementXMLString(eleContainerDetails));
					NodeList nListContainer = XMLUtil.getNodeList(eleContainerDetails, "ContainerDetail");

					// COPY PaymentMethods node
					// FROM Container/ContainerDetails/Container/ShipmentLine/OrderLine/Order/PaymentMethods
					// TO Container/Shipment/PaymentMethods.
					NodeList paymentMethodsNodeList = XMLUtil.getNodeList(nListContainer.item(0), "ShipmentLine/OrderLine/Order/PaymentMethods");
					if(paymentMethodsNodeList.getLength() > 0)
					{
						Node paymentMethods = paymentMethodsNodeList.item(0);
						log.verbose("********* payment method element - " + XMLUtil.getElementXMLString((Element)paymentMethods));
						// added to Container/Shipment
						shipmentElement.appendChild(paymentMethods);
					}

					// REMOVE PaymentMethods node FROM ShipmentLine/OrderLine/Order/PaymentMethods
					int numberOfContainers = nListContainer.getLength();
					log.verbose("********* number of containers - " + numberOfContainers);
					for(int i=0;i< numberOfContainers; i++)
					{
						NodeList orderNodeList = XMLUtil.getNodeList(nListContainer.item(i), "ShipmentLine/OrderLine/Order");
						if(orderNodeList.getLength() > 0)
						{
							Element orderElement = (Element) orderNodeList.item(0);
							log.verbose("********* Order element - " + XMLUtil.getElementXMLString(orderElement));
							Node paymentMethodsNode = orderElement.getElementsByTagName("PaymentMethods").item(0);
							if(!YFCObject.isVoid(paymentMethodsNode))
							{
								orderElement.removeChild(paymentMethodsNode);
							}
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		log.verbose("********* output xml - " + XMLUtil.getElementXMLString(inDocElem));
		log.endTimer(" End of AcademyProrateForPackSlip-> movePaymentMethodsNodeOutside Api");
	}

	private String getShipDateFromInvoiceNo(YFSEnvironment env, String strInvoiceNo){

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

	private void setDataInPackSlipFormat(YFSEnvironment env, Element eleContainerDetails, Element eleContainer) {
		//setting the shipment Quantity to integer value
		Element shipLineEle = (Element) eleContainerDetails.getElementsByTagName("ShipmentLine").item(0);

		//setting the SCAC and carrier code in PACK-Slip format
		String SCACandCarrier = "";
		String scacVal = eleContainer.getAttribute("SCAC");
		String scacValue = getScacValueFromMapping(env, scacVal);
		String carrierVal = eleContainer.getAttribute("CarrierServiceCode");
		// STL-28 code fix//
		if (AcademyConstants.STR_PRIORITY_MAIL.equalsIgnoreCase(carrierVal)){
			SCACandCarrier = "USPS";
		}else if (AcademyConstants.STR_FIRST_CLASS_MAIL.equalsIgnoreCase(carrierVal)){
			SCACandCarrier = "USPS";
		}else{
			SCACandCarrier = scacValue + " - " + carrierVal;
		}
		//End of code fix for STL-28//
		//String SCACandCarrier = scacValue + " - " + carrierVal;
		eleContainer.setAttribute("SCACAndCarrier", SCACandCarrier);
		// Start : BOPIS-1579
		 if ( !YFCObject.isNull(shipLineEle) && shipLineEle.getElementsByTagName("OrderLine").getLength()>0)
		 {log.verbose("Shipment line is not null Ordrline is present inside Shipment line");
			 Element orderLineEle = (Element) shipLineEle.getElementsByTagName("OrderLine").item(0);
				if (!YFCObject.isNull(orderLineEle)) {
					Element orderEle = (Element) orderLineEle.getElementsByTagName("Order").item(0);
					// String ordDate = orderEle.getAttribute("OrderDate");
					if (!YFCObject.isNull(orderEle)) {
						orderEle.setAttribute("OrderDate", getDateInPackSlipFormat(orderEle.getAttribute("OrderDate")));
					}
				} 
		 }
		// End : BOPIS-1579

		Element shipEle = (Element) eleContainer.getElementsByTagName("Shipment").item(0);
		//Removing the shipdate code as now the ship date is being extracted from Invoice no
		/*if (!YFCObject.isNull(shipEle)) {
			//String shipDate = shipEle.getAttribute("ShipDate");
			//System.out.println("shipdate" + shipDate);
			shipEle.setAttribute("ShipDate", getDateInPackSlipFormat(shipEle.getAttribute("ShipDate")));
		}*/

		String shipDate = getShipDateFromInvoiceNo(env, strInvoiceNo);
		shipEle.setAttribute("ShipDate", shipDate);

	}


	public String getScacValueFromMapping(YFSEnvironment env, String scacVal){

		String displayScacVal = "";

		if ("UPSN".equals(scacVal)) {
			displayScacVal = "UPS";
		}
		if (scacVal.startsWith("USPS")){
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


	public static String getDateInPackSlipFormat (String value) {
		//2010-06-08T00:00:00-00:00
		DateFormat dbDateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		try {
			return formatter.format(dbDateTimeFormatter.parse(value));
		} catch (ParseException e) {

		}
		return formatter.format(new Date());
	}


	//private String getItemDiscTypeNames(String ItemDiscChargeName,  String itemDiscType, NodeList iLineCharge, int k) #As a part of STL-1711
	private String getItemDiscTypeNames(String ItemDiscChargeName,	String itemDiscType, NodeList nLineCharge, int iLineChargeCunt) {

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

		//Begin : OMNI-3056
		if("PLCC_Acquisition_Offer".equals(ItemDiscChargeName)) {
			itemDiscPrintName = "PLCC Acquisition Offer";
		}
		
		if("PLCC_Welcome_Offer".equals(ItemDiscChargeName)) {
			itemDiscPrintName = "PLCC Welcome Offer";
		}
		
		if("PLCC_Anniversary".equals(ItemDiscChargeName)) {
			itemDiscPrintName = "PLCC Anniversary";
		}
		
		if("PLCC_EveryDay5".equals(ItemDiscChargeName)) {
			itemDiscPrintName = "PLCC EveryDay5";
		}
		//End : OMNI-3056

		
		//if (k < (iLineCharge.getLength()-1)) #As a part of STL-1711
		if (iLineChargeCunt < (nLineCharge.getLength()-1)) {
			itemDiscType =  itemDiscType + itemDiscPrintName + " /";
		} else  {
			itemDiscType += itemDiscPrintName;
		}

//		if (itemDiscType.endsWith("/")) {
//		itemDiscType = itemDiscType.substring(itemDiscType.length()-(itemDiscType.length()-1));
//		}
		return itemDiscType;
	}


	private void checkGiftInstruction(YFSEnvironment env, Element inDocElem)
	throws Exception {
		if (!YFCObject.isVoid(inDocElem)) {
			Element Shipment = XMLUtil.getFirstElementByName(inDocElem,
			"Shipment");
			strPrintBatchPackSlip = eleContainer.getAttribute("PrintBatchPackSlip");
			if (!YFCObject.isVoid(Shipment)) {
				Element Instructions = XMLUtil.getFirstElementByName(Shipment,
				"Instructions");
				if (!YFCObject.isVoid(Instructions)) {
					NodeList InstructionList = XMLUtil.getNodeList(
							Instructions, "Instruction");
					if (!YFCObject.isVoid(InstructionList)) {
						int iLen = InstructionList.getLength();
						for (int k = 0; k < iLen; k++) {
							Element Instruction = (Element) InstructionList
							.item(k);
							String InstuctionType = Instruction
							.getAttribute("InstructionType");
							if ("GIFT".equals(InstuctionType)) {
								inDocElem.setAttribute("IsGiftMessageShipment",
								"Y");
								inDocElem.setAttribute("PrintDocumentId",
								"ACAD_CONTAINER_PACKSLIP_CONV_GIFT");
							}
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
		//double tax1 = Double.parseDouble(tax);
		double orderQty = Double.parseDouble(orderedPricingQty);
		double ShipQty = Double.parseDouble(quantity);

		// For Conveyable
		Element sShipmentElem = XMLUtil.getFirstElementByName(inDocElem, "Shipment");
		if (!YFCObject.isVoid(sShipmentElem)) {
			String isPackProcessComplete = sShipmentElem.getAttribute("IsPackProcessComplete");
			if ("Y".equals(isPackProcessComplete)) {
				shippingCharge = shippingCharge	- ((Math.round(((shippingCharge / orderQty) * 100))) / 100.00) * (orderQty - ShipQty);
				discount = discount	- ((Math.round(((discount / orderQty) * (orderQty - ShipQty) * 100))) / 100.00)	* (orderQty - ShipQty);
				tax = tax	- ((Math.round(((tax / orderQty) * (orderQty - ShipQty) * 100))) / 100.00) * (orderQty - ShipQty);
			} else {
				shippingCharge = (Math.round(((shippingCharge / orderQty) * ShipQty * 100))) / 100.00;
				discount = (Math.round(((discount / orderQty) * ShipQty * 100))) / 100.00;
				tax = (Math.round(((tax / orderQty) * ShipQty * 100))) / 100.00;
			}
		}
		// call getShipmentdetails

	}

	public void generatePRONumberForShipment() {

	}

	/*This method adds attributes to the pack slip
	 * These data have to be shown only when the an item exist.
	 *
	 * */
	public Element setStaticFieldsOnConvPackSlip(YFSEnvironment env, Element eContainerDetail){

		eContainerDetail.setAttribute("ItemDiscText", "ITM DISC");
		eContainerDetail.setAttribute("ItemTaxText", "ITM TAX");
		eContainerDetail.setAttribute("ShippingAdjText", "SHIPPING");
		eContainerDetail.setAttribute("ShippingTaxText", "TAX");
		eContainerDetail.setAttribute("ItemTaxDesc", "Tax calculated after discount");
		eContainerDetail.setAttribute("ShippingAdjDesc", "Shipping cost for this item");
		eContainerDetail.setAttribute("ShippingTaxDesc", "Sales Tax on shipping");

		return eContainerDetail;

	}
	
	// Begin : OMNI-3056
	private void setDocumentChargeAndDiscountList() {

		try {
			docChargeAndDiscountList = AcademyServiceUtil
					.getSOAPMessageTemplate("/global/template/utils/AcademyChargesAndDiscountsList.xml");
		} catch (Exception e) {
			log.error("Exception 'setDocumentChargeAndDiscountList' method:: " + e);
		}
	}

	public Element getMerchandiseCAndDList(String documentType) {
		Element eleTypeChargeAndDicountList = null;
		try {

			log.verbose("docChargeAndDiscountList ::" + XMLUtil.getXMLString(docChargeAndDiscountList));

			eleTypeChargeAndDicountList = XMLUtil.getElementByXPath(docChargeAndDiscountList,
					"/AcademyChargesAndTaxs/Order[@DocumentType='" + documentType
							+ "']/OrderLine/Merchandise/LineChareges");

			log.verbose("eleTypeChargeAndDicountList ::" + XMLUtil.getElementXMLString(eleTypeChargeAndDicountList));
		} catch (Exception e) {
			log.error("Exception caught in 'getMerchandiseCAndDList' method:: " + e);
		}
		return eleTypeChargeAndDicountList;
	}
	// End : OMNI-3056

}
