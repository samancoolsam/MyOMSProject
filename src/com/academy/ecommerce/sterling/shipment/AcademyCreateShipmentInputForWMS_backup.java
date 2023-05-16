package com.academy.ecommerce.sterling.shipment;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyCreateShipmentInputForWMS_backup implements YIFCustomApi{
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyCreateShipmentInputForWMS_backup.class);
	public static String strPrimeLineNo="";
	public static String strSubLineNo="";
	public static String strPrimeLineNoInOrderInvoice="";
	public static String strSubLineNoInOrderInvoice="";
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	public Document createShipment(YFSEnvironment env, Document inXML)throws Exception{
		log.verbose("Beginning AcademyCreateShipmentInputForWMS_backup-->createShipment" + XMLUtil.getXMLString(inXML));
		String strShipmentKey="";
		String strShipmentNo="";
		Document docInputGetShipmentList = null;
		Document docOutputGetShipmentList = null;
		Document responseXMLToWMS = null;
		
		// get the ShipmentKey from the input xml coming from SENT_TO_NODE transaction.
		Element eleShipment = inXML.getDocumentElement();
		strShipmentKey= eleShipment.getAttribute("ShipmentKey");
		strShipmentNo= eleShipment.getAttribute("ShipmentNo");
		
		 //For the input for getShipmentList APi Call
		docInputGetShipmentList = XMLUtil.createDocument("Shipment");
		docInputGetShipmentList.getDocumentElement().setAttribute("ShipmentNo",strShipmentNo);
		docInputGetShipmentList.getDocumentElement().setAttribute("ShipmentKey",strShipmentKey);
		env.setApiTemplate("getShipmentList","global/template/api/getShipmentList.CreateShipmentToWMS.xml");
		docOutputGetShipmentList = AcademyUtil.invokeAPI(env,"getShipmentList", docInputGetShipmentList);
		log.verbose("Output of getShipmentList API---->"+XMLUtil.getXMLString(docOutputGetShipmentList));
		env.clearApiTemplate("getShipmentList");		
		// form the input message to createShipment in WMS
		responseXMLToWMS = formCreateShipmentInputForWMS(docOutputGetShipmentList,inXML);	
		log.verbose("ResponseXML from-->createShipment"+XMLUtil.getXMLString(responseXMLToWMS));
		log.verbose("End AcademyCreateShipmentInputForWMS_backup-->createShipment");
		
		return responseXMLToWMS;
		
	}

	
public static Document formCreateShipmentInputForWMS(Document inDoc, Document inXML) throws Exception {
		
		//log.verbose("Beginning AcademyCreateShipmentInputForWMS_backup-->formCreateShipmentInputForWMS" + XMLUtil.getXMLString(inDoc));
	
        Element eleShipment=null;
	    Document shipmentDoc= null;
	    Element eleShipmentExtn =null;
	    Document responseXMLToFirstMethod = null;
	    String strAmtcharged = "";
		String strTotalCharged = "";
		String strCreditCardType = "";
		String strGCCreditCardType = "";
		String strDisplayCreditCardNo = "";
		String strDisplaySvcNo = "";
		//STL - 1049
		String strOrderTotal = "";
		// To get the shipmentLineKey from the sent to node output
		
		Element eleInXML= inXML.getDocumentElement();
		Element eleShipmentLinesInXML = (Element) eleInXML.getElementsByTagName("ShipmentLines").item(0);
		NodeList nShipmentLineInXML = XMLUtil.getNodeList(eleShipmentLinesInXML, "ShipmentLine");		
		eleShipment = (Element) inDoc.getElementsByTagName("Shipment").item(0);	    
		//shipmentDoc = eleShipment.getOwnerDocument();
		Element eleOrderInvoiceList = (Element)eleShipment.getElementsByTagName("OrderInvoiceList").item(0);
		Element eleOrderInvoice = (Element)eleOrderInvoiceList.getElementsByTagName("OrderInvoice").item(0);		
		String strOrderInvoiceKey = eleOrderInvoice.getAttribute("OrderInvoiceKey");
		Element eleFirstShipmentLines = (Element) eleShipment.getElementsByTagName("ShipmentLines").item(0);
		Element eleFirstShipmentLine = (Element) eleFirstShipmentLines.getElementsByTagName("ShipmentLine").item(0);
		
		Element eleOrderInDoc = (Element) eleFirstShipmentLine.getElementsByTagName("Order").item(0);
		//Fix for STL - 1043
		Element elePaymentMethods = (Element) eleOrderInDoc.getElementsByTagName("PaymentMethods").item(0);
		//STL 1049 - START
		NodeList nPaymentMethodList = XMLUtil.getNodeList(elePaymentMethods,AcademyConstants.ELE_PAYMENT_METHOD);
		Element eleOverAllTotals = (Element) eleOrderInDoc.getElementsByTagName(AcademyConstants.ELE_OVERALL_TOTALS).item(0);
		strOrderTotal = eleOverAllTotals.getAttribute(AcademyConstants.ATTR_GRAND_TOTAL);
		//STL 1049 - END
		//Fix for STL - 1043
		
		//eleShipmentExtn = inDoc.createElement("Extn");
		eleShipmentExtn = (Element)eleShipment.getElementsByTagName("Extn").item(0);
		//STL-1049 : START
		eleShipmentExtn.setAttribute(AcademyConstants.ATTR_EXTN_ORDER_TOTAL, AcademyConstants.STR_DOLLAR + strOrderTotal);
		//STL-1049 : END
		//eleShipment.appendChild(eleShipmentExtn);
		Element eleExtnOrderInvoice = inDoc.createElement("Extn");
		eleExtnOrderInvoice.setAttribute("ExtnOrderInvoiceKey", strOrderInvoiceKey);
		eleOrderInvoice.appendChild(eleExtnOrderInvoice);
		eleOrderInvoice.appendChild(eleExtnOrderInvoice);
		eleOrderInvoice.removeAttribute("OrderInvoiceKey");
		log.verbose("***ExtnOrderInvoiceKey***"+strOrderInvoiceKey);
		//String strOrderInvoiceKey = eleOrderInvoice.getAttribute("OrderInvoiceKey");
		// logic to stamp the TotalAuthorized,TotalCharged,DisplayCreditCardNo, CreditCardType, DisplaySvcNo values based on the
		// paymentTypes
		//Start : STL-1049 Packing slip is not displaying multiple ways of payment information. 
		int noOfPaymentMethods = nPaymentMethodList.getLength();
		if (noOfPaymentMethods<=0) {
			log.verbose("The order does not have any payment methods");
		}
		if (eleShipmentExtn != null) {
			Element acadShipmentPaymentDtlsListElem = inDoc.createElement(AcademyConstants.ELEM_ACAD_SHIPMENT_PAYMENT_DTLS_LIST);			
			for (int i=0;i<noOfPaymentMethods;i++) {
				Element elePaymentMethod = (Element) elePaymentMethods.getElementsByTagName(AcademyConstants.ELE_PAYMENT_METHOD).item(i);
				
				// Fetch Payment Attributes
				String strPaymentType = elePaymentMethod.getAttribute(AcademyConstants.ATTR_PAYMENT_TYPE);			
				strCreditCardType = elePaymentMethod.getAttribute(AcademyConstants.ATTR_CREDIT_CARD_TYPE);
				strGCCreditCardType = AcademyConstants.GC_CREDIT_CARD_TYPE;
				strDisplayCreditCardNo = elePaymentMethod.getAttribute(AcademyConstants.ATTR_DISPLAY_CREDIT_CARD_NO);
				strDisplaySvcNo = elePaymentMethod.getAttribute(AcademyConstants.ATTR_DISPLAY_SVC_NO);
							
				log.verbose("PaymentType: "+ strPaymentType);
				log.verbose("CreditCardType: "+ strCreditCardType);
				log.verbose("DisplayCreditCardNo: "+ strDisplayCreditCardNo);
				log.verbose("DisplaySvcNo: "+ strDisplaySvcNo);
				
							
				Element acadShipmentPaymentDtlsElem = inDoc.createElement(AcademyConstants.ELEM_ACAD_SHIPMENT_PAYMENT_DTLS);
				if (strPaymentType.equalsIgnoreCase(AcademyConstants.STR_CREDIT_PAYMENT_TYPE) 
						|| strPaymentType.equalsIgnoreCase(AcademyConstants.PAYPAL)) {
					log.verbose("payment type is either paypal or Credit Card");
					
					// START :: changes done as part of host capture
					if (AcademyConstants.PAYPAL.equals(strPaymentType) ) {
						acadShipmentPaymentDtlsElem.setAttribute(AcademyConstants.ATTR_CREDIT_CARD_TYPE,  AcademyConstants.PAYPAL);
					} else {
						acadShipmentPaymentDtlsElem.setAttribute(AcademyConstants.ATTR_CREDIT_CARD_TYPE, strCreditCardType);
					}
					//END :: changes done as part of host capture
					acadShipmentPaymentDtlsElem.setAttribute(AcademyConstants.ATTR_DISPLAY_CREDIT_CARD_NO, strDisplayCreditCardNo);
					//acadShipmentPaymentDtlsElem.setAttribute(AcademyConstants.ATTR_AMOUNT_CHARGED, String.valueOf(dTotAmt));
				}
				if (strPaymentType.equals(AcademyConstants.STR_GIFT_CARD)) {
					log.verbose("payment type is Gift Card");
					acadShipmentPaymentDtlsElem.setAttribute(AcademyConstants.ATTR_CREDIT_CARD_TYPE, strGCCreditCardType);
					acadShipmentPaymentDtlsElem.setAttribute(AcademyConstants.ATTR_DISPLAY_CREDIT_CARD_NO, strDisplaySvcNo);
					//acadShipmentPaymentDtlsElem.setAttribute(AcademyConstants.ATTR_AMOUNT_CHARGED, strTotalCharged);
				}
				acadShipmentPaymentDtlsListElem.appendChild(acadShipmentPaymentDtlsElem);				
			}
			eleShipmentExtn.appendChild(acadShipmentPaymentDtlsListElem);
		}		
		// End : STL-1049 Packing slip is not displaying multiple ways of payment information.
		//Fix for STL - 1043
		else{
			log.verbose("The order does not have any payment methods");
		}
		//Fix for STL - 1043
		//eleShipment.appendChild(eleShipmentExtn);
		shipmentDoc = XMLUtil.getDocumentForElement(eleShipment);
		//log.verbose("response for shipment and orderinvoice extn: ******"+XMLUtil.getXMLString(shipmentDoc));
		responseXMLToFirstMethod = calculateShippingTaxAndItemLineTax(shipmentDoc, inXML);
		//log.verbose("response"+XMLUtil.getXMLString(responseXMLToFirstMethod));
		
		// remove child logic
		Element eleresponseShipment= responseXMLToFirstMethod.getDocumentElement();
		Element eleresponseShipmentLines = (Element)eleresponseShipment.getElementsByTagName("ShipmentLines").item(0);
		NodeList nResponseShipmentLine = XMLUtil.getNodeList(eleresponseShipmentLines,"ShipmentLine");
		Element eleresponseShipmentLine = null;
		Element eleresponseSOrderLine = null;
		
		Element eleresponseOrder = null;
		for(int l=0;l<nResponseShipmentLine.getLength();l++){			
			eleresponseShipmentLine = (Element)eleresponseShipmentLines.getElementsByTagName("ShipmentLine").item(l);
			eleresponseSOrderLine = (Element)eleresponseShipmentLine.getElementsByTagName("OrderLine").item(0);
			
			eleresponseOrder = (Element)eleresponseShipmentLine.getElementsByTagName("Order").item(0);
			
			eleresponseShipmentLine.removeChild(eleresponseOrder);
			eleresponseShipmentLine.removeChild(eleresponseSOrderLine);
		}
		
		// remove child logic
		log.verbose("response"+XMLUtil.getXMLString(responseXMLToFirstMethod));
		
		return responseXMLToFirstMethod;

	}
	 public static Document calculateShippingTaxAndItemLineTax(Document shipmentDoc,Document inXML) throws Exception
	 	{
	
		 Element eleOrderLineInDoc= null;
		 Element eleInDoc = shipmentDoc.getDocumentElement();
			Element eleShipmentLinesInDoc = (Element) eleInDoc.getElementsByTagName("ShipmentLines").item(0);
			NodeList nShipmentLineInDoc = XMLUtil.getNodeList(eleShipmentLinesInDoc,"ShipmentLine");
			Element eleOrderInvoiceListInDoc = (Element) eleInDoc.getElementsByTagName("OrderInvoiceList").item(0);
		 	Element eleOrderInvoiceInDoc = (Element) eleOrderInvoiceListInDoc.getElementsByTagName("OrderInvoice").item(0);
		  	Element eleLineDetailsInDoc = (Element) eleOrderInvoiceInDoc.getElementsByTagName("LineDetails").item(0);
		  	NodeList nLineDetailInDoc = XMLUtil.getNodeList(eleLineDetailsInDoc,"LineDetail");
		  	
		  	//Element eleLineDetailInDoc = (Element) eleLineDetails.getElementsByTagName("LineDetail");
	 	//eleOrderInvoiceList.appendChild(eleOrderInvoice);
	 	//Element eleLineDetails = inDoc.createElement("LineDetails");
	 	
	 	// to get shipment key from sent to node output
	 	Element eleInXML= inXML.getDocumentElement();
		Element eleShipmentLinesInXML = (Element) eleInXML.getElementsByTagName("ShipmentLines").item(0);
		NodeList nShipmentLineInXML = XMLUtil.getNodeList(eleShipmentLinesInXML, "ShipmentLine");
		for (int outLoop = 0; outLoop < nShipmentLineInXML.getLength(); outLoop++) {
			//log.verbose("shipmentline nlen in the inXML(sent to node)"+nShipmentLineInXML.getLength());
			Element eleShipmentLineInXML = (Element) nShipmentLineInXML.item(outLoop);
			// ShipmentLineKey from inDoc coming from sent to node.
			String strShipmentLineKeyInXML = eleShipmentLineInXML.getAttribute("ShipmentLineKey");
			log.verbose("ShipmentLineKey in xml"+ strShipmentLineKeyInXML);	
 			 
			
			if (!YFCObject.isVoid(nShipmentLineInDoc)) {
				NodeList nShipmentLine = XMLUtil.getNodeList(eleShipmentLinesInDoc, "ShipmentLine");
				log.verbose("shipmentline nl len"+ nShipmentLine.getLength());
			
	 		for(int i=0;i<nShipmentLineInDoc.getLength();i++)
	 		{
	 			//log.verbose("Shipmentline nl in indoc " + nShipmentLineInDoc.getLength());	 			

	 						//Element eleOrderLine= null;
							double shippingTaxAtLineLevel = 0.00;
							double itemTaxAtLineLevel = 0.00;
							double shippingChargeAtLineLevel = 0.00;
							double discountForLine = 0.00;
							String itemDiscType = "";
							Element eleShipmentLineInDoc = (Element) nShipmentLineInDoc.item(i);
							// ShipmentLineKey from inDoc coming from getShipmentList.
							String strShipmentLineKeyInDoc = eleShipmentLineInDoc.getAttribute("ShipmentLineKey");
							log.verbose("ShipmentLineKey is" + strShipmentLineKeyInDoc);
							if (strShipmentLineKeyInXML.equals(strShipmentLineKeyInDoc)) {
								strPrimeLineNo = eleShipmentLineInDoc.getAttribute("PrimeLineNo");
								log.verbose("primelineno is :::::::::"+ strPrimeLineNo);
								
								strSubLineNo = eleShipmentLineInDoc.getAttribute("SubLineNo");
								log.verbose("SubLineNo is ***********"+ strSubLineNo);
								eleOrderLineInDoc = (Element) eleShipmentLineInDoc.getElementsByTagName("OrderLine").item(0);
								if (!YFCObject.isVoid(eleOrderLineInDoc)) {
									//log.verbose("OL");
									//Fix for STL - 1021
									Element eleItemInDoc = (Element) eleOrderLineInDoc.getElementsByTagName("Item").item(0);
									String strItemShortDesc = eleItemInDoc.getAttribute("ItemShortDesc");
									String strNMFCClass = eleItemInDoc.getAttribute("NMFCClass");
									String strNMFCCode = eleItemInDoc.getAttribute("NMFCCode");
									String strNMFCDescription = eleItemInDoc.getAttribute("NMFCDescription");
									String strItemWeight = eleItemInDoc.getAttribute("ItemWeight");
									//Fix for STL - 1021
									Element eleLineTaxesInDoc = (Element) eleOrderLineInDoc.getElementsByTagName("LineTaxes").item(0);
									//log.verbose("eleLineTaxes" + XMLUtil.getElementXMLString(eleLineTaxesInDoc));
									if (!YFCObject.isVoid(eleLineTaxesInDoc)) {
									
										NodeList nLineTax = XMLUtil.getNodeList(eleLineTaxesInDoc, "LineTax");
										//log.verbose("linetax nllen " + nLineTax.getLength());
										for (int k = 0; k < nLineTax.getLength(); k++) {
											Element eleLineTaxInDoc = (Element) nLineTax.item(k);
											//log.verbose("elelinetax/////"+ XMLUtil.getElementXMLString(eleLineTaxInDoc));
											String chargeCategory = eleLineTaxInDoc.getAttribute("ChargeCategory");
											if ("Shipping".equals(chargeCategory)) {
												String strShippingTaxAtLineLevel = eleLineTaxInDoc.getAttribute("Tax");
												log.verbose("strShippingTaxAtLineLevel *************"+ strShippingTaxAtLineLevel);
												shippingTaxAtLineLevel += Double.parseDouble(strShippingTaxAtLineLevel);
												log.verbose("shippingTaxAtLineLevel"+ Double.parseDouble(strShippingTaxAtLineLevel));
											}
											if ("TAXES".equals(chargeCategory)) {
												String strItemTaxAtLineLevel = eleLineTaxInDoc.getAttribute("Tax");
												itemTaxAtLineLevel += Double.parseDouble(strItemTaxAtLineLevel);
												log.verbose("itemTaxAtLineLevel"+ Double.parseDouble(strItemTaxAtLineLevel));
											}

										}
										//Fix for STL - 1021
									Element eleExtnItem = shipmentDoc.createElement("Extn");
									eleExtnItem.setAttribute("ExtnItemShortDesc", strItemShortDesc);
									log.verbose("ExtnItemShortDesc***"+ strItemShortDesc);
									eleExtnItem.setAttribute("ExtnNMFCClass", strNMFCClass);
									log.verbose("ExtnNMFCClass***"+ strNMFCClass);
									eleExtnItem.setAttribute("ExtnNMFCCode", strNMFCCode);
									log.verbose("ExtnNMFCCode***"+ strNMFCCode);
									eleExtnItem.setAttribute("ExtnNMFCDescription", strNMFCDescription);
									log.verbose("ExtnNMFCDescription***"+ strNMFCDescription);
									eleExtnItem.setAttribute("ExtnItemWeight", strItemWeight);
									log.verbose("ExtnItemWeight***"+ strItemWeight);
									eleShipmentLineInDoc.appendChild(eleExtnItem);
									//Fix for STL - 1021
									}
										//Fix for STL 815
									
									Element eleLineChargesInDoc = (Element) eleOrderLineInDoc.getElementsByTagName("LineCharges").item(0);
									//log.verbose("eleLineTaxes" + XMLUtil.getElementXMLString(eleLineTaxesInDoc));
									if (!YFCObject.isVoid(eleLineChargesInDoc)) {
									
										NodeList nLineCharge = XMLUtil.getNodeList(eleLineChargesInDoc, "LineCharge");
										//log.verbose("linetax nllen " + nLineTax.getLength());
										for (int l = 0; l < nLineCharge.getLength(); l++) {
											Element eleLineChargeInDoc = (Element) nLineCharge.item(l);
											//log.verbose("elelinetax/////"+ XMLUtil.getElementXMLString(eleLineTaxInDoc));
											String isDiscount = eleLineChargeInDoc.getAttribute("IsDiscount");
											String chargeAmount = eleLineChargeInDoc.getAttribute("ChargeAmount");
											String chargeName = eleLineChargeInDoc.getAttribute("ChargeName");
											
											if (chargeName.contains("Shipping")) {
											/*	String strShippingChargeAtLineLevel = LineCharge.getAttribute("ChargeAmount");*/
												if (isDiscount.equalsIgnoreCase("Y")) {
													shippingChargeAtLineLevel = shippingChargeAtLineLevel - Double.parseDouble(chargeAmount);
												} else {
													shippingChargeAtLineLevel = shippingChargeAtLineLevel + Double .parseDouble(chargeAmount);
												}
											}
											if ("BOGO".equals(chargeName)|| ("DiscountCoupon".equals(chargeName))) {
												if (isDiscount.equalsIgnoreCase("Y")) {
													discountForLine = discountForLine+ Double.parseDouble(chargeAmount);
													itemDiscType = getItemDiscTypeNames(chargeName,itemDiscType,nLineCharge,l);
												}
											}

										}
								
									}
									
									// Fix for STL 815
								}
								for(int j=0;j<nLineDetailInDoc.getLength();j++)
						 		{
									log.verbose("LineDetail nl in indoc " + nLineDetailInDoc.getLength());	
									Element eleLineDetailInDoc = (Element) nLineDetailInDoc.item(j);
									if (!YFCObject.isVoid(eleLineDetailInDoc)) {
										strPrimeLineNoInOrderInvoice = eleLineDetailInDoc.getAttribute("PrimeLineNo");
										log.verbose("primelineno is :::::::::"+ strPrimeLineNoInOrderInvoice);
										strSubLineNoInOrderInvoice = eleLineDetailInDoc.getAttribute("SubLineNo");
										log.verbose("sublimeNo is :::::::::"+ strSubLineNoInOrderInvoice);
										if (strPrimeLineNoInOrderInvoice.equals(strPrimeLineNo)&& strSubLineNoInOrderInvoice.equals(strSubLineNo)) {
											eleLineDetailInDoc.setAttribute("PrimeLineNo",strPrimeLineNoInOrderInvoice);
											eleLineDetailInDoc.setAttribute("SubLineNo",strSubLineNoInOrderInvoice);
											log.verbose("Primelineno******"+ strPrimeLineNoInOrderInvoice);	
											Element eleExtn = shipmentDoc.createElement("Extn");
											eleExtn.setAttribute("ExtnShippingTax", String.valueOf(shippingTaxAtLineLevel));
											log.verbose("ExtnShippingTax***"+ String.valueOf(shippingTaxAtLineLevel));
											eleExtn.setAttribute("ExtnItemTax", String.valueOf(itemTaxAtLineLevel));
											log.verbose("ExtnItemTax***"+ String.valueOf(itemTaxAtLineLevel));
											//Fix for STL 815
											eleExtn.setAttribute("ExtnShipChargeAtLine", String.valueOf(shippingChargeAtLineLevel));
											log.verbose("ExtnShipChargeAtLine***"+ String.valueOf(shippingChargeAtLineLevel));
											eleExtn.setAttribute("ExtnDiscountForLine", String.valueOf(discountForLine));
											log.verbose("ExtnDiscountForLine***"+ String.valueOf(discountForLine));
											eleExtn.setAttribute("ExtnItemDiscType", itemDiscType);
											log.verbose("ExtnItemDiscType***"+ itemDiscType);
											//Fix for STL 815
											eleLineDetailInDoc.appendChild(eleExtn);
						 		}	
							}
										
		 				}

					}
	 			}
	 		
	 		}
	 		
		}
		/*for(int l=0;l<nShipmentLineInDoc.getLength();l++){
			
			eleShipmentLinesInDoc.removeChild(eleOrderLineInDoc);
			Element eleOrderInDoc= (Element) eleShipmentLinesInDoc.getElementsByTagName("Order").item(0);
			eleShipmentLinesInDoc.removeChild(eleOrderInDoc);
		}*/
		
		return shipmentDoc;
	}
	 
	 public static String getItemDiscTypeNames(String ItemDiscChargeName,
				String itemDiscType, NodeList nLineCharge, int l) {

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

			if (l < (nLineCharge.getLength() - 1)) {
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
}