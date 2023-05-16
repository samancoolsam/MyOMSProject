/**
 *
 */
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
import org.w3c.dom.NodeList;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author sahmed
 *
 */
public class AcademyGetPRONoAndInvoiceNo implements YIFCustomApi {

	private Properties props = null;
	Document docInvoiceDetail = null;
	private String strInvoiceNo = null;
	private String strProNumber = null;
	private Element inDocElem = null;
	private Element eleShipment = null;
	private Element eleContainer = null;
	private Element eleInvoiceDetail = null;

	/*
	 * (non-Javadoc)
	 *
	 * @see com.yantra.interop.japi.YIFCustomApi#setProperties(java.util.Properties)
	 */
	public void setProperties(Properties props) {
		this.props = props;
	}

	private static YFCLogCategory log = YFCLogCategory
	.instance(AcademyGetPRONoAndInvoiceNo.class);

	public Document getPRONumberAndInvoiceNumber(YFSEnvironment env,
			Document inDoc) throws Exception {
		log.beginTimer(" Begining of AcademyGetPRONoAndInvoiceNo-> getPRONumberAndInvoiceNumber Api");
		//Start WN-1000 : [STRETCH] Remove special characters from the Packing Slip
		Element eleToAddress = null;
		Element eleBillToAddress = null;
		//End WN-1000 : [STRETCH] Remove special characters from the Packing Slip
		final String XPATH_OVER_ALL_TOTALS = "InputDocument/InvoiceDetail/InvoiceHeader/Order/OverallTotals";//added for STL-1279.
		
		inDocElem = inDoc.getDocumentElement();
		if (!YFCObject.isVoid(inDocElem)) {
			eleContainer = (Element) XMLUtil.getNode(inDocElem,	"EnvironmentDocument/Container");
			eleShipment = (Element) XMLUtil.getNode(inDocElem, "EnvironmentDocument/Container/Shipment");
			eleInvoiceDetail = (Element) XMLUtil.getNode(inDocElem,	"InputDocument/InvoiceDetail");
			Document docInputToGeneratePRONumber = XMLUtil.createDocument("Input");

			Element eleContainerDetails = XMLUtil.getFirstElementByName(inDocElem,	"EnvironmentDocument/Container/ContainerDetails");

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
			
			// FIX for defect 3048 - ProNo should be generated only for BULK White Glove Item
			String shipmentType = eleShipment.getAttribute("ShipmentType");
			log.verbose("Shipment type : " + shipmentType);
			if("WG".equals(shipmentType))
			{
				// Check if the shipment has a pro number, if yes then don't generate a new PRO number
				String proNum = eleShipment.getAttribute("ProNo");
				if((proNum).trim().equals(""))
				{
					//EFP-17 - Start
					String strScac = eleShipment.getAttribute("SCAC");
					docInputToGeneratePRONumber.getDocumentElement().setAttribute("SCAC", strScac);
					//EFP-17 - End
					generatePRONumber(env, docInputToGeneratePRONumber, eleShipment);
					eleContainerDetails.setAttribute("ProNumber", strProNumber);
				}
				else
				{
					eleContainerDetails.setAttribute("ProNumber", (eleShipment.getAttribute("ProNo")));
				}
			}
			else
			{
				eleContainerDetails.setAttribute("ProNumber", "");
			}

//			Call Invoice No UE to create Shipment Invoice
			eleShipment = (Element) XMLUtil.getNode(inDocElem, "EnvironmentDocument/Container/Shipment");

			/* 
			 * Fix for 12716. For reprint/repack scenario, check if Invoice# already exists and if true, 
			 * do not call callGetInvoiceNoUE
			 * 
			 */
			
			//Extract the invoice number from Order Invoice table
			String strExtnInvoiceNo = XMLUtil.getString(eleInvoiceDetail,
			"InvoiceHeader/Extn/@ExtnInvoiceNo");
			
			log.verbose("Invoice number in Indoc InvoiceDetails : " + strExtnInvoiceNo);
			
			//this method checks if the re-packing is done on same day or not. 
			//for same date, use the same invoice number.
			//for different date, generate new invoice number
			//boolean generateInvoiceNo = createInvoiceNumber(strExtnInvoiceNo);

			
			/* Logic for generating Invoice No
			 * Generate the invoice number only for the first container of the shipment.
			 * Use the same invoice number for all the containers.
			 * If the shipment is unpacked and re-packed then
			 * 		-If repacking is done on the same day, use the existing invoice number
			 * 		-If repacking is done on different day, create new invoice number.
			 */ 

			//if (!YFCObject.isVoid(eleShipment) && YFCObject.isVoid(strExtnInvoiceNo)) {
			if (!YFCObject.isVoid(eleShipment)) {
				log.verbose("Invoice number does exist in Indoc InvoiceDetails");
				//Change for CR - Reprint PackSlip Flow
				//If the API is invoking for reprint then don't call GetInvoiceNoUE
				String isReprintFlow = "N";
				if(env.getTxnObject("ReprintPackSlip")!=null)
					isReprintFlow = (String)env.getTxnObject("ReprintPackSlip");
				log.verbose("isReprintFlow : "+isReprintFlow+" actual InvoiceNo is : "+strExtnInvoiceNo);
				if(isReprintFlow.equals("Y")){
					log.verbose("**** This reprint flow *****");
					if(env.getTxnObject("ReprintPrinterId")!=null){
						String strPrinterId = (String)env.getTxnObject("ReprintPrinterId");
						eleContainer.setAttribute("PrinterId", strPrinterId);
					}
					strInvoiceNo = strExtnInvoiceNo;
				}else{
					if (YFCObject.isNull(strExtnInvoiceNo)) {
						log.verbose("****Invoice Number doesn't exist so needs to be generated");
						callGetInvoiceNoUE(env, inDocElem);
					} else /*if (generateInvoiceNo)*/ {
						//callGetInvoiceNoUE(env, inDocElem);
						eleShipment.setAttribute("ExtnInvoiceNo", strExtnInvoiceNo);
						String strOrderInvoiceKey = XMLUtil.getString(eleInvoiceDetail,"InvoiceHeader/@OrderInvoiceKey");
						eleShipment.setAttribute("OrderInvoiceKey", strOrderInvoiceKey);
						Document docShipment = XMLUtil.createDocument("Shipment");
						XMLUtil.copyElement(docShipment, eleShipment, docShipment
								.getDocumentElement());
						log.verbose("****Shipment document is *****"
								+ XMLUtil.getXMLString(docShipment));
						Document docInvoiceNo = AcademyUtil.invokeService(env,
								"AcademyValidateInvoiceNumberService", docShipment);
						if (!YFCObject.isVoid(docInvoiceNo)) {
							strInvoiceNo = docInvoiceNo.getDocumentElement().getAttribute(
							"InvoiceNo");
							log
							.verbose("**** Invoice Header Document is returned from AcademyValidateInvoiceNumberService****");
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
					} /*else {
						strInvoiceNo = strExtnInvoiceNo;
					}*/
				}
			}




			calculateDataForPackSlip(env, inDocElem);
			
			//Begin change for #3845 by mithila
			Element eleContainerDetail=(Element)eleContainerDetails.getElementsByTagName("ContainerDetail").item(0);
			if(!YFCObject.isVoid(eleContainerDetail))
			{   log.verbose("Inside Container Detail element");
			Element eleShipmentLine = (Element)eleContainerDetail.getElementsByTagName("ShipmentLine").item(0);
				
			if(!YFCObject.isVoid(eleShipmentLine))
			{	log.verbose("Inside Shipment Line element");
			String strShipLineKey=eleShipmentLine.getAttribute("ShipmentLineKey");
			log.verbose("Shipment Line Key is "+strShipLineKey);
			Document docInputToGetTaskList=XMLUtil.createDocument("Task");
			Element eleTask=docInputToGetTaskList.getDocumentElement();
			eleTask.setAttribute("TaskStatus", "1100"); 
			Element eleTaskReferences=docInputToGetTaskList.createElement("TaskReferences");
			eleTask.appendChild(eleTaskReferences);

			eleTaskReferences.setAttribute("ShipmentLineKey", strShipLineKey);
			log.verbose("Input document to getTaskList: "+XMLUtil.getXMLString(docInputToGetTaskList));

			Document docTempForGetTaskList=YFCDocument.parse("<TaskList><Task SourceLocationId=\"\">" +
			"<Inventory ItemId=\"\" Quantity=\"\"/></Task></TaskList>").getDocument();
			env.setApiTemplate("getTaskList", docTempForGetTaskList);
			log.verbose("Output template for getTaskList: "+XMLUtil.getXMLString(docTempForGetTaskList));

			Document docOutputOfGetTaskList=AcademyUtil.invokeAPI(env, "getTaskList", docInputToGetTaskList);
			log.verbose("Output document for getTaskList: "+XMLUtil.getXMLString(docOutputOfGetTaskList));

			//Begin Transforming output of getTaskList to merge into output doc of this class
			if(!YFCObject.isVoid(docOutputOfGetTaskList))
			{
				Element eleTaskList=docOutputOfGetTaskList.getDocumentElement();
				if(!YFCObject.isVoid(eleTaskList))
				{
					Element eleTaskOut = (Element)eleTaskList.getElementsByTagName("Task").item(0);
					if(!YFCObject.isVoid(eleTaskOut))
					{
						String strLocId= eleTaskOut.getAttribute("SourceLocationId");

						Element eleInventory=(Element)eleTaskOut.getElementsByTagName("Inventory").item(0);
						String strItemId= eleInventory.getAttribute("ItemId");
						String strQuantity=eleInventory.getAttribute("Quantity");

						Element eleContainerTask=inDoc.createElement("Task");				
						eleContainerDetail.appendChild(eleContainerTask);
						eleContainerTask.setAttribute("SourceLocationId", strLocId);
						eleContainerTask.setAttribute("ItemId", strItemId);
						eleContainerTask.setAttribute("Quantity", strQuantity);
					}
					else
					{
						Element eleContainerTask=inDoc.createElement("Task");				
						eleContainerDetail.appendChild(eleContainerTask);
						eleContainerTask.setAttribute("SourceLocationId", "");
					}
				}
			}
			//End Transforming output of getTaskList to merge into output doc of this class
			//eleContainerDetail.appendChild(docOutputOfGetTaskList);	
			}
			}
			//End change for #3845 by mithila
		}
		
		//Start WN-1000 : [STRETCH] Remove special characters from the Packing Slip		
		eleToAddress = (Element) XMLUtil.getNode(inDoc,AcademyConstants.XPATH_CONTAINER_SHIPMENT_TOADDRESS);
		eleBillToAddress = (Element) XMLUtil.getNode(inDoc,AcademyConstants.XPATH_CONTAINER_SHIPMENT_BILLTOADDRESS);		
		AcademyUtil.convertUnicodeToSpecialChar(env, eleToAddress, eleBillToAddress, false);
		//End WN-1000 : [STRETCH] Remove special characters from the Packing Slip
		
		log.beginTimer(" End of AcademyGetPRONoAndInvoiceNo-> getPRONumberAndInvoiceNumber Api");
		//System.out.println("output of this class" + XMLUtil.getXMLString(inDoc));
		log.verbose("Final Output document : "+XMLUtil.getXMLString(inDoc));
		return inDoc;
	}

	/**
	 * @param strExtnInvoiceNo
	 * @return
	 */
	private boolean createInvoiceNumber(String strExtnInvoiceNo) {
		Date todayDate = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		String invoiceDate = "";
		boolean generateInvoiceNo = false;
		if(!YFCObject.isNull(strExtnInvoiceNo)){
			invoiceDate = strExtnInvoiceNo.substring(0, 8);

			String sysdate = dateFormat.format(todayDate);

			if (!sysdate.equals(invoiceDate)) {
				generateInvoiceNo = true;
			}
		}
		return generateInvoiceNo;
	}	


	private Document generatePRONumber(YFSEnvironment env,	Document docInputToGeneratePRONumber, Element eleShipment) throws Exception {

		Document docGeneratePRONumber = AcademyUtil.invokeService(env,	"AcademyGeneratePRONumber", docInputToGeneratePRONumber);
		strProNumber = docGeneratePRONumber.getDocumentElement().getAttribute("ProNumber");
		persistProNoOnShipment(env, eleShipment, strProNumber);
		return docGeneratePRONumber;
	}

	/* Call this method to persist the ProNo on Shipment */
	private void persistProNoOnShipment(YFSEnvironment env,	Element eleShipment, String strProNumber) {

		try {
			Document docShipment = XMLUtil.createDocument("Shipment");
			docShipment.getDocumentElement().setAttribute("ShipmentKey", eleShipment.getAttribute("ShipmentKey"));
			docShipment.getDocumentElement().setAttribute("ProNo", strProNumber);
			Document docChangeShipment = AcademyUtil.invokeAPI(env,	"changeShipment", docShipment);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private Document callGetInvoiceNoUE(YFSEnvironment env, Element inDocElem)	throws Exception, ParserConfigurationException {
		log.verbose("********** AcademyGetPRONoAndInvoiceNo:callGetInvoiceNoUE() --> Beings");
		log.verbose("************* input doc element : " + XMLUtil.getElementXMLString(inDocElem));
		Element eleInvoiceDetail = (Element) XMLUtil.getNode(inDocElem,	"InputDocument/InvoiceDetail");
		Document docInvoiceDetail = XMLUtil.createDocument("InvoiceDetail");
		Element eleInvoiceHeader = XMLUtil.createElement(docInvoiceDetail, "InvoiceHeader", null);
		docInvoiceDetail.getDocumentElement().appendChild(eleInvoiceHeader);
		Element eleShipment1 = XMLUtil.createElement(docInvoiceDetail, "Shipment", null);
		eleInvoiceHeader.appendChild(eleShipment1);
		eleShipment1.setAttribute("ShipmentKey", eleShipment.getAttribute("ShipmentKey"));
		eleShipment1.setAttribute("ShipDate", eleShipment.getAttribute("ShipDate"));
		eleShipment1.setAttribute("ShipNode", eleShipment.getAttribute("ShipNode"));
		eleShipment1.setAttribute("CallInvoiceUEFromPrintFLow", "Y");
		log.verbose("********** CallInvoiceUEFromPrintFLow set to Y");

		/* Call GetInvoiceNo UE Service to retrieve the Invoice No */
		Document outDoc = AcademyUtil.invokeService(env, "AcademyInvokeGetInvoiceNo", docInvoiceDetail);
		strInvoiceNo = outDoc.getDocumentElement().getAttribute("InvoiceNo");
		if (!YFCObject.isVoid(eleInvoiceDetail)) {
			String strGetExistingInvoiceNo = XMLUtil.getString(inDocElem, "InputDocument/InvoiceDetail/InvoiceHeader/Extn/@ExtnInvoiceNo");
			//********Fix for bug# 2648**************
			//	if ("".equals(strGetExistingInvoiceNo)) {
			String strGetProformaOrderInvoiceKey = XMLUtil.getString(inDocElem, "InputDocument/InvoiceDetail/InvoiceHeader/@OrderInvoiceKey");
			Document docInput = XMLUtil.createDocument("OrderInvoice");
			docInput.getDocumentElement().setAttribute("OrderInvoiceKey",strGetProformaOrderInvoiceKey);
			Element eleExtn = XMLUtil.createElement(docInput, "Extn", null);
			docInput.getDocumentElement().appendChild(eleExtn);
			eleExtn.setAttribute("ExtnInvoiceNo", strInvoiceNo);
			Document outdoc = AcademyUtil.invokeAPI(env, "changeOrderInvoice", docInput);
			//	}
		}
		log.verbose("********** Invoice Detail doc : " + docInvoiceDetail);
		return docInvoiceDetail;
	}

	public void calculateDataForPackSlip(YFSEnvironment env, Element inDocElem)
	throws Exception {
		log.beginTimer(" Begining of AcademyGetPRONoAndInvoiceNo-> calculateDataForPackSlip Api");
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

		Element eleInvoiceheader = (Element) XMLUtil.getNode(inDocElem,	"InputDocument/InvoiceDetail/InvoiceHeader");
		Element eleContainerDetails = XMLUtil.getFirstElementByName(inDocElem, "EnvironmentDocument/Container/ContainerDetails");
		Element eleInvoiceLineDetails = XMLUtil.getFirstElementByName(eleInvoiceheader, "LineDetails");
		Element eleContainer = XMLUtil.getFirstElementByName(inDocElem, "EnvironmentDocument/Container");

		DecimalFormat decimalCorrection = new DecimalFormat("0.00");
		setDataInPackSlipFormat(env, eleContainerDetails, eleContainer);
		
		boolean isMultibox =   false;
		String  multiBox  = (String) env.getTxnObject("isMultibox");
		log.verbose("Fetching the transaction object isMultibox.Value is  ::" +multiBox );
		if(multiBox != null && !multiBox.isEmpty() && multiBox.equalsIgnoreCase("Y")){
			isMultibox = true;
		}
		
		
		if (!YFCObject.isVoid(eleContainerDetails)) {
			NodeList nListContainer = XMLUtil.getNodeList(eleContainerDetails, "ContainerDetail");
			if (!YFCObject.isVoid(nListContainer)) {
				int iLength = nListContainer.getLength();
				for (int i = 0; i < iLength; i++) {
					eContainerDetail = (Element) nListContainer.item(i);
					if (!YFCObject.isVoid(eContainerDetail)) {

						sContainerQty = eContainerDetail.getAttribute("Quantity");
						double dContQty = Double.parseDouble(sContainerQty);
						int iContQty = isMultibox ? 1 :(int) dContQty;
						log.verbose("iContQty is set to : :" + iContQty);
						eContainerDetail.setAttribute("Quantity", Integer.toString(iContQty));

						Element shipLineEle = (Element) eContainerDetail.getElementsByTagName("ShipmentLine").item(0);
						String shipQty = shipLineEle.getAttribute("Quantity");
						double dShipQty = Double.parseDouble(shipQty);
						int iShipQty = (int) dShipQty;
						shipLineEle.setAttribute("Quantity", Integer.toString(iShipQty));

						eShipmentLine = XMLUtil.getFirstElementByName(eContainerDetail, "ShipmentLine");
						//As a part of STL-1711 Tax details are not correct in Packing slip 
						//double shippingChargeAtLineLevel = 0.00;
						//double discountForLine = 0.00;

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
											log.verbose("AcademyGetPRONoAndInvoiceNo-calculateDataForPackSlip - strPaymentType :: "+strPaymentType);
											log.verbose("AcademyGetPRONoAndInvoiceNo-calculateDataForPackSlip - strCreditCardType :: "+strCreditCardType);
											String strCommonCodeCCType = getCommonCodeForCrediCardType(env, strCreditCardType);
											log.verbose("AcademyGetPRONoAndInvoiceNo-calculateDataForPackSlip - strCommonCodeCCType :: "+strCommonCodeCCType);
											/* if ("CREDIT_CARD".equalsIgnoreCase(strPaymentType)){	 */											
											if(AcademyConstants.CREDIT_CARD.equalsIgnoreCase(strPaymentType)
												|| AcademyConstants.STR_APPLE_PAY_PAYMENT.equalsIgnoreCase(strPaymentType) 
												|| AcademyConstants.STR_GOOGLE_PAY_PAYMENT.equalsIgnoreCase(strPaymentType)
												|| AcademyConstants.STR_PLCC_PAYMENT.equalsIgnoreCase(strPaymentType)													
												) {												
												//OMNI-31625: Tender type for PLCC, ApplePay, GooglePay - END
												elePaymentMethod.setAttribute("DisplayCreditCardNo", "(" + strDisplayCreditCardNo  + ")");
												//elePaymentMethod.setAttribute("CreditCardType",	strCreditCardType);
												elePaymentMethod.setAttribute("CreditCardType",	strCommonCodeCCType);
												elePaymentMethod.setAttribute("AmountCharged", elePaymentMethod.getAttribute("TotalAuthorized"));

											} 
											// Start :: PayPal Integration
											else if (AcademyConstants.PAYPAL.equalsIgnoreCase(strPaymentType)) {//KER-11461 change equals to equalsIgnoreCase.  to support new Paypal payment type
												elePaymentMethod.setAttribute("DisplayCreditCardNo","");												
												// Start :: Host Capture Changes
												//elePaymentMethod.setAttribute("CreditCardType",strCreditCardType);
												//OMNI-31625: Start
												//elePaymentMethod.setAttribute("CreditCardType", AcademyConstants.PAYPAL);
												//elePaymentMethod.setAttribute("CreditCardType", AcademyConstants.PACK_SLIP_PAYPAL);
												elePaymentMethod.setAttribute("CreditCardType", strCommonCodeCCType);
												//OMNI-31625: End
												// End :: Host Capture Changes												
												elePaymentMethod.setAttribute("AmountCharged",elePaymentMethod.getAttribute("TotalAuthorized"));
											}
											// End :: PayPal Integration
											else{
												elePaymentMethod.setAttribute("DisplayCreditCardNo", "(" + strSvcNo + ")" );
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
										/*if (!YFCObject.isVoid(iLineDetail)) {
										for (int j = 0; j < iLineDetail.getLength(); j++) {

										Element iLineDetailEle = (Element) iLineDetail.item(j);
										LineCharges = XMLUtil.getFirstElementByName(iLineDetailEle, "OrderLine/LineCharges");
										//LineTaxes = XMLUtil.getFirstElementByName(iLineDetailEle, "LineTaxes");
										//Start-Fix Defect#71
										LineTaxes = XMLUtil.getFirstElementByName(iLineDetailEle, "OrderLine/LineTaxes");
										//End-Fix Defect#71*/
										//END STL-1711 Tax details are not correct in Packing slip 
										String strOrderlineKey = iLineDetailEle.getAttribute("OrderLineKey");
										if (strOrderLineKey.equals(strOrderlineKey)) {
											shippingTaxAtLineLevel = 0.00;
											itemTaxAtLineLevel = 0.00;
											itemDiscType = "";
											//START STL-1711 Tax details are not correct in Packing slip 
											shippingChargeAtLineLevel=0.00;
											discountForLine = 0.00;
											//END STL-1711 Tax details are not correct in Packing slip 

											strUnitPrice = ((Element) iLineDetail.item(j)).getAttribute("UnitPrice");
											strExtendedPrice = ((Element) iLineDetail.item(j)).getAttribute("ExtendedPrice");
											containerExtendedPrice = Double.valueOf(strUnitPrice) * Double.valueOf(sContainerQty);
											extendedPriceTotal += containerExtendedPrice;
											strOrderedPricingQty = ((Element) iLineDetail.item(j)).getAttribute("ShippedQty");
											eleOrderLine.setAttribute("UnitPrice", "$" + strUnitPrice);
											eleOrderLine.setAttribute("ExtendedPrice", "$" + decimalCorrection.format(containerExtendedPrice));

											//START STL-1711 Tax details are not correct in Packing slip 
											/*if (!YFCObject.isVoid(LineCharges)) {
												NodeList iLineCharge = XMLUtil.getNodeList(LineCharges, "LineCharge");
												for (int k = 0; k < iLineCharge.getLength(); k++) {
													LineCharge = (Element) iLineCharge.item(k);
													String isDiscount = LineCharge.getAttribute("IsDiscount");
													String chargeAmount = LineCharge.getAttribute("ChargeAmount");
													String chargeName = LineCharge.getAttribute("ChargeName");*/
											Element eleLineChargeList = (Element) iLineDetailEle.getElementsByTagName(AcademyConstants.ELEMENT_LINE_CHARGE_LIST).item(0);
											if (!YFCObject.isVoid(eleLineChargeList))
						                	{
											NodeList nLineCharge = XMLUtil.getNodeList(eleLineChargeList, AcademyConstants.ELE_LINE_CHARGE);
											for (int iLineChargeCunt = 0; iLineChargeCunt < nLineCharge.getLength(); iLineChargeCunt++) {
												Element eleLineCharge = (Element)nLineCharge.item(iLineChargeCunt);
												String isDiscount = eleLineCharge.getAttribute(AcademyConstants.ATTR_IS_DISCOUNT);
												String chargeAmount = eleLineCharge.getAttribute(AcademyConstants.ATTR_CHARGE_AMT);
												String chargeName = eleLineCharge.getAttribute(AcademyConstants.ATTR_CHARGE_NAME);
												//if (eleLineCharge.getAttribute("ChargeName").contains("Shipping"))
												//END STL-1711 Tax details are not correct in Packing slip 
												if (chargeName.contains(AcademyConstants.STR_SHIPPING)) {
													//String strShippingChargeAtLineLevel = LineCharge.getAttribute("ChargeAmount"); #As a part of STL-1711
													String strShippingChargeAtLineLevel = chargeAmount;
													if (AcademyConstants.STR_YES.equals(isDiscount)) {
														shippingChargeAtLineLevel = shippingChargeAtLineLevel -
														Double.parseDouble(strShippingChargeAtLineLevel);
													} else {
														shippingChargeAtLineLevel = shippingChargeAtLineLevel +
														Double.parseDouble(strShippingChargeAtLineLevel);
													}
												}
												if (AcademyConstants.STR_BOGO.equals(chargeName) || (AcademyConstants.STR_DISCOUNT_COUPON.equals(chargeName))) {
													if (AcademyConstants.STR_YES.equals(isDiscount)) {
														discountForLine = discountForLine + Double.parseDouble(chargeAmount);
														//itemDiscType = getItemDiscTypeNames(chargeName, itemDiscType, iLineCharge, k); #As a part of STL-1711
														itemDiscType = getItemDiscTypeNames(chargeName, itemDiscType, nLineCharge, iLineChargeCunt);
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
													// Fix for #3678 - Add shipping taxes outside of loop
													//tax += shippingTaxAtLineLevel;
												}

												if (AcademyConstants.STR_TAXES.equals(chargeCategory)) {
													String strItemTaxAtLineLevel = eleLineTax.getAttribute(AcademyConstants.ATTR_TAX);
													itemTaxAtLineLevel += Double.parseDouble(strItemTaxAtLineLevel);
													// Fix for #3678 - Add shipping taxes outside of loop
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
									//Fix for Issue #3678, corrected the proration logic
									tax = tax + shippingTaxAtLineLevel + itemTaxAtLineLevel;

									eleOrderLine.setAttribute("ShippingTaxLineLevel", "$" + decimalCorrection.format(shippingTaxAtLineLevel));
									eleOrderLine.setAttribute("ItemTaxLineLevel", "$" + decimalCorrection.format(itemTaxAtLineLevel));
								}
							}
						}
					}
				}
				totalTax = totalTax + tax;
				//totalShippingCharge = totalShippingCharge + shippingChargeAtLineLevel; //As a part of STL-1711
			}
		}
		eleContainerDetails.setAttribute("TotalExtendedPrice", "$" + decimalCorrection.format(extendedPriceTotal));
		eleContainerDetails.setAttribute("TotalTax", "$" + decimalCorrection.format(totalTax));
		eleContainerDetails.setAttribute("ShippingCharges", "$" + decimalCorrection.format(totalShippingCharge));
		eleContainerDetails.setAttribute("TotalDiscount", "($" + decimalCorrection.format(totalDiscount) + ")");
		double cartonTotal = extendedPriceTotal + totalTax + totalShippingCharge - totalDiscount;
		eleContainerDetails.setAttribute("CartonTotal", "$" + decimalCorrection.format(cartonTotal));
		eleContainerDetails.setAttribute("InvoiceNo", strInvoiceNo);
		eleContainerDetails.setAttribute("ProNumber", strProNumber);

		log.endTimer(" End of AcademyGetPRONoAndInvoiceNo-> calculateDataForPackSlip Api");
	}
	
	
	//OMNI-31625: Tender type for PLCC, ApplePay, GooglePay - START
	/**
	 * This method return the common code short description value.
	 * @param env
	 * @param strCreditCardType
	 * @return
	 */
	/**inDoc:
	 * <CommonCode CodeType="PACK_SLIP_PAY_TYPE" CodeValue="AMERICAN EXPRESS"/>
	 * 
		outDoc:
		<CommonCodeList>
			<CommonCode CodeLongDescription="AMEX" CodeShortDescription="AMEX" CodeType="PACK_SLIP_PAY_TYPE" CodeValue="AMERICAN EXPRESS"/>
		</CommonCodeList>
	 *
	 */
	private String getCommonCodeForCrediCardType(YFSEnvironment env, String strCreditCardType) {
		// TODO Auto-generated method stub
		log.verbose( "AcademyGetPRONoAndInvoiceNo-getCommonCodeForCrediCardType :: "+  strCreditCardType);
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
			log.verbose( "AcademyGetPRONoAndInvoiceNo-getCommonCodeForCrediCardType - outDocGetCommonCodeList :: "+  XMLUtil.getXMLString(outDocGetCommonCodeList));
			if(!YFCObject.isVoid(outDocGetCommonCodeList) && 
					outDocGetCommonCodeList.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_COMMON_CODE).getLength() > 0)
			{			
				Element eleCommonCode = XMLUtil.getElementByXPath(outDocGetCommonCodeList, AcademyConstants.XPATH_COMMON_CODE_ELE);
				strCommonCodeCCType = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
				log.verbose( "AcademyGetPRONoAndInvoiceNo-getCommonCodeForCrediCardType - strCommonCodeCCType :: "+  strCommonCodeCCType);
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

		Element orderLineEle = (Element) shipLineEle.getElementsByTagName("OrderLine").item(0);
		if (!YFCObject.isNull(orderLineEle)) {
			Element orderEle = (Element) orderLineEle.getElementsByTagName("Order").item(0);
			//String ordDate = orderEle.getAttribute("OrderDate");
			if (!YFCObject.isNull(orderEle)) {
				orderEle.setAttribute("OrderDate", getDateInPackSlipFormat(orderEle.getAttribute("OrderDate")));
			}
		}

		Element shipEle = (Element) eleContainer.getElementsByTagName("Shipment").item(0);
		log.verbose("Shipment Element without ShipDate is : "+ XMLUtil.getElementXMLString(shipEle));
		//Fix for bug #4461 begins
		String strShipmentKey = shipEle.getAttribute("ShipmentKey");
		/*if (!YFCObject.isNull(shipEle)) {
			//String shipDate = shipEle.getAttribute("RequestedShipmentDate");
			//System.out.println("shipdate" + shipDate);
			shipEle.setAttribute("ShipDate", getDateInPackSlipFormat(shipEle.getAttribute("ShipDate")));
		}*/
		log.verbose("Calling getShipCreateDateFromShipmentKey with shipment key : " + strShipmentKey);
		String shipCreateDate = getShipCreateDateFromShipmentKey(env, strShipmentKey);
		shipEle.setAttribute("ShipDate", shipCreateDate);
		log.verbose("Shipment Element is : "+ XMLUtil.getElementXMLString(shipEle));
	}


	private String getShipCreateDateFromShipmentKey(YFSEnvironment env, String strShipmentKey){

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
	//Fix for bug #4461 ends


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

		//if (k < (iLineCharge.getLength()-1)) //STL-1711 modified the condition
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



	/*	This method adds attributes to the pack slip
	 * These data have to be shown only when the an item exist.
	 *
	 *
		public Element setStaticFieldsOnBulkPackSlip(YFSEnvironment env, Element eContainerDetail){

			//eContainerDetail.setAttribute("ItemDiscText", props.getProperty("ItemDiscText"));
			eContainerDetail.setAttribute("ItemDiscText", "ITM DISC");
			eContainerDetail.setAttribute("ItemTaxText", "ITM TAX");
			eContainerDetail.setAttribute("ShippingAdjText", "SHIPPING-ADJ");
			eContainerDetail.setAttribute("ShippingTaxText", "SHIPPING TAX");
			eContainerDetail.setAttribute("ItemDiscDesc", "Bogo promo / Coupon / CS Adj");
			eContainerDetail.setAttribute("ItemTaxDesc", "Tax calculated after discount(8.25%)");
			eContainerDetail.setAttribute("ShippingAdjDesc", "Shipping cost for this item w/Adj.");
			eContainerDetail.setAttribute("ShippingTaxDesc", "Taxes for Shipping(8.25%)");

			return eContainerDetail;

		}
	 */

}
