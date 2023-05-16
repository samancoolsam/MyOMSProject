package com.academy.ecommerce.sterling.userexits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfs.japi.YFSExtnLineTaxCalculationInputStruct;
import com.yantra.yfs.japi.YFSExtnTaxBreakup;
import com.yantra.yfs.japi.YFSExtnTaxCalculationOutStruct;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSRecalculateLineTaxUE;

/**
 * 
 */

/**
 * @author dsharma
 * 
 */
public class AcademySalesOrderRecalculateLineTaxUEImpl implements
		YFSRecalculateLineTaxUE {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademySalesOrderRecalculateLineTaxUEImpl.class);
	
	static Hashtable<String, Integer> ht = new Hashtable<String, Integer>();
	// static double remainingTax = 0.0;
	static final String[] taxNames = { "STATE", "COUNTY", "DISTRICT" };
	static {
		for (int i = 0; i < taxNames.length; i++)
			ht.put(taxNames[i], Integer.valueOf(i));

	}

	private static int get(String s) {
		return ht.get(s).intValue();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yantra.yfs.japi.ue.YFSRecalculateLineTaxUE#recalculateLineTax(com
	 * .yantra.yfs.japi.YFSEnvironment,
	 * com.yantra.yfs.japi.YFSExtnLineTaxCalculationInputStruct)
	 */
	public YFSExtnTaxCalculationOutStruct recalculateLineTax(
			YFSEnvironment env,
			YFSExtnLineTaxCalculationInputStruct taxInputStruct)
			throws YFSUserExitException {
		try {
			log.beginTimer(" Begining of AcademySalesOrderRecalculateLineTaxUEImpl-> recalculateLineTax()");
			YFSExtnTaxCalculationOutStruct outputStruct = new YFSExtnTaxCalculationOutStruct();
			List<YFSExtnTaxBreakup> outputList = new ArrayList<YFSExtnTaxBreakup>();
			// TODO Auto-generated method stub
        			//start fix STL-397: tax proration
        			List<YFSExtnTaxBreakup> listColTax = taxInputStruct.colTax;
        			log.verbose("*************Total tax**********"+taxInputStruct.tax);
        			Double openTaxTotal = taxInputStruct.tax;
                    int intTotalTaxes = listColTax.size();
                    
                    // refund the taxes in case of cancellations.
                    Double invoicedTax= 0.0;
                    for (int j = 0; j < intTotalTaxes; j++) {
                        YFSExtnTaxBreakup taxBreakupSample = listColTax.get(j);
                        log.verbose("*************Invoiced tax breakup**********"+taxBreakupSample.invoicedTax);
                        log.verbose("*************individual tax**********"+taxBreakupSample.tax);
                        log.verbose("*************chargeCategory**********"+taxBreakupSample.chargeCategory);
                        log.verbose("*************chargeName**********"+taxBreakupSample. chargeName);
                        log.verbose("*************totalInvoicedCharge**********"+taxBreakupSample.totalInvoicedCharge);
                        
						//start fix STL-503_1 :: tax writeoff issue during tax free holiday
						if(taxBreakupSample.chargeCategory.equals("TAX_WRITEOFF"))
						{
							log.verbose("*************Charge Catogory is TAX_WRITEOFF**********");
							invoicedTax = invoicedTax - taxBreakupSample.invoicedTax;
						}
						else
						{
							log.verbose("*************Charge Catogory is not TAX_WRITEOFF**********");
							invoicedTax = invoicedTax + taxBreakupSample.invoicedTax;
						}
						//end fix STL-503_1						
                     }   
                    log.verbose("*************INVOICED TAX**********"+invoicedTax);
                   
				   
				   
        			//end fix STL-397: tax proration
			/*
			 * bForInvoice will be 'true' at the time of invoice creation. Will be also 'true'
			 * when a CREDIT_MEMO generated because of Appeasement on an order post invoice.
			 */
			
			if (taxInputStruct.bForInvoice) {
				log.verbose("Calculate tax for : \n  bForInvoice : "+taxInputStruct.bForInvoice);
				String sOrdHdrKey = taxInputStruct.orderHeaderKey;
				String sOrdLineKey = taxInputStruct.orderLineKey;
				String sInvoiceNo = taxInputStruct.invoiceKey;
				// get the Proforma Invoice # from Env.
								if (env.getTxnObject("InvoiceNo") != null) {
					sInvoiceNo = env.getTxnObject("InvoiceNo").toString();

				}
				
				
				// get the new tax from vertex
				// get the Vertex document from the environment
				Document docVertexInvoiceResponse = null;
				HashMap<String, Double> lvertexTax = new HashMap<String, Double>();
				double vertexMerchandiseTax = 0.0;
				double vertexShippingTax = 0.0;
				double vertexTax = 0.0;
				
				
				/* 
				 * invoiceMode will be PRICE_CHANGE when this UE is called by CREDIT_MEMO i.e. Post Invoice Appeasement 
				 */ 
				
				log.verbose("****************  invoice mode : " + taxInputStruct.invoiceMode);
				if("PRICE_CHANGE".equals(taxInputStruct.invoiceMode)) {
					log.verbose("Calculate tax for : \n  bForInvoice : "+taxInputStruct.bForInvoice);
					/* commenting out this code as no need to make a vertex call in case of appeasement

					// Make a vertex Call here.
					Document salesOrderDetail = getCompleteSalesOrderDetails(env, sOrdHdrKey);
					
					// convert the input to Vertex Quote Call Request xml.
					salesOrderDetail.getDocumentElement().setAttribute("CallType", "QuoteCall");
					Document vertexRequest = AcademyUtil.invokeService(env, "AcademyCreditMemoToInvoiceCallRequest", salesOrderDetail);
					docVertexInvoiceResponse = AcademyUtil.invokeService(env, "AcademyVertexInvoiceCallRequest", vertexRequest);
					System.out.println("docmnet from vertex" + XMLUtil.getXMLString(docVertexInvoiceResponse));
					lvertexTax = this.getVertexTaxAmt(docVertexInvoiceResponse,	sOrdLineKey);
					
					*/
					
					log.verbose("************ APPEASEMENT - NO Tax recalculation, NO vertex call ****************");

					outputList = this.updateTax(taxInputStruct, lvertexTax, true, 0, "Appeasement", outputList);
					outputStruct.colTax = outputList;

					log.verbose("***********  colTax - " + outputStruct.colTax.toString());
					log.verbose("********** tax - " + outputStruct.tax);
					log.verbose("*********** tax percentage - " + outputStruct.taxPercentage);

					return outputStruct;
				}
				
				if (env.getTxnObject("ShipmentInvoiceCall") != null) 
				{
					docVertexInvoiceResponse = (Document) env.getTxnObject("ShipmentInvoiceCall");
					log.verbose("AcademySalesOrderRecalculateLineTaxUEImpl --> Vertex Invoice Response : " + XMLUtil.getXMLString(docVertexInvoiceResponse));
					
					lvertexTax = this.getVertexTaxAmt(docVertexInvoiceResponse, sOrdLineKey);
					log.verbose("***********  Vertex tax amount : " + lvertexTax);
					
					vertexMerchandiseTax = lvertexTax.get("VertexMerchandiseTax").doubleValue();
					log.verbose("***********  Vertex merchandise tax : " + vertexMerchandiseTax);
					
					vertexShippingTax = lvertexTax.get("VertexShippingTax").doubleValue();
					log.verbose("***********  Vertex Shipping Tax : " + vertexShippingTax);
					
					vertexTax = vertexShippingTax + vertexMerchandiseTax;
					log.verbose("***********  Vertex Tax : " + vertexTax);
				}
				// get the proforma invoice tax of shipment
				HashMap<String, Double> lProformaTax = new HashMap<String, Double>();
				double proformaShippingTax = 0.0;
				double proformaMerchandiseTax = 0.0;
				double proformaTax = 0.0;
				if (env.getTxnObject("ShipmentKey") != null) 
				{
					String sShipmentKey = (String) env.getTxnObject("ShipmentKey");
					
					lProformaTax = this.getProformaTax(env, sShipmentKey, sOrdLineKey);
					proformaShippingTax = lProformaTax.get("ProformaShippingTax").doubleValue();
					proformaMerchandiseTax = lProformaTax.get("ProformaMerchandiseTax").doubleValue();
					proformaTax = proformaShippingTax + proformaMerchandiseTax;

				}
				//start fix : STL-397 : tax proration
				//if (vertexTax > opproformaTax) 
				log.verbose("***before condition*** vertex tax ::: " + vertexTax + ", openTaxTotal ::: " + openTaxTotal + ",invoicedTax ::: " + invoicedTax + ", openTaxTotal - invoicedTax == " + (openTaxTotal - invoicedTax));
				if (vertexTax > (openTaxTotal - invoicedTax)) 
				//end fix : STL-397 : tax proration
				{
					log.verbose("************* vertex tax > proforma tax");
					outputList = this.updateTax(taxInputStruct, lvertexTax, true, 0, "Shipping", outputList);
					outputList = this.updateTax(taxInputStruct, lvertexTax, true, 0, "Merchandise", outputList);
					// construct changeOrder Input
					//start fix : STL-397 : tax proration
					log.verbose("difference to be included as TAX_WRITEOFF == " + (vertexTax - (openTaxTotal - invoicedTax)));
					outputList = this.setChangeOrderInEnv(env, outputList, vertexTax - (openTaxTotal - invoicedTax));
					//end fix : STL-397 : tax proration
				} 
				else 
				{
					log.verbose("************* vertex tax < proforma tax");
					outputList = this.updateTax(taxInputStruct, lvertexTax, true, 0, "Shipping", outputList);
					outputList = this.updateTax(taxInputStruct, lvertexTax, true, 0, "Merchandise", outputList);
				}
				outputStruct.colTax = outputList;
				this.printOutStructTax(outputStruct);
			}
			/*
			 * Check if UE is invoked for Proforma Invoice at the time of
			 * shipment creation
			 */
			else if (taxInputStruct.bForPacklistPrice) {
				log.verbose("Calculate tax for : \n  bForPacklistPrice : "+taxInputStruct.bForPacklistPrice);
				// get the orderLineKey from input
				String sOrdHdrKey = taxInputStruct.orderHeaderKey;
				String sOrdLineKey = taxInputStruct.orderLineKey;
				// get the Proforma Invoice # from Env.
				String sInvoiceNo = taxInputStruct.invoiceKey;
				if (env.getTxnObject("InvoiceNo") != null) 
				{
					sInvoiceNo = env.getTxnObject("InvoiceNo").toString();

				}
				log.verbose("OrderHeaderKey is "+sOrdHdrKey+"OrderLineKey is "+sOrdLineKey+"Invoice No is : "+sInvoiceNo);
				// get all the Proforma Invoices and their tax amount for the
				// order line key

				/**
				 * START - Fix for #4131
				 *  - Remove a line from Shipment
				 *  - cancel paritial qty of Shipment line
				 *  Always compares the open tax of order line with new vertex tax while creating new proforma in case above scenarios. 
				 *  Therefore, commenting the below code to get previous proforma tax. 
				 * 
				 */
				/*HashMap<String, Double> invoiceTaxList = this.getOrderLineOpenTaxAmt(env, sOrdHdrKey, sOrdLineKey, sInvoiceNo);

				double invoiceShippingTax = invoiceTaxList.get("ProformaShippingTax").doubleValue();
				double invoiceMerchandiseTax = invoiceTaxList.get("ProformaMerchandiseTax").doubleValue();
				log.verbose("Got the tax details from previous invoice \n ProformaShippingTax is "+invoiceShippingTax+" ProformaMerchandiseTax is "+invoiceMerchandiseTax);
*/
				// get the order line tax
				HashMap<String, Double> openTax = this.getInputTax(taxInputStruct);
				double openShippingTax = openTax.get("OpenShippingTax").doubleValue();
				double openMerchandiseTax = openTax.get("OpenMerchandiseTax").doubleValue();

				log.verbose("Processing Order Line tax details \n openShippingTax is "+openShippingTax+" openMerchandiseTax is "+openMerchandiseTax);
				/*openShippingTax = openShippingTax - invoiceShippingTax;
				openMerchandiseTax = openMerchandiseTax - invoiceMerchandiseTax;*/
				// get the Vertex document from the environment
				Document docVertexQuoteResponse = null;
				HashMap<String, Double> lvertexTax = new HashMap<String, Double>();
				double vertexShippingTax = 0.0;
				double vertexMerchandiseTax = 0.0;

				if (env.getTxnObject("ShipmentQuoteCall") != null) {
					log.verbose("Shipment Quote Call : Tax calculation ");

					docVertexQuoteResponse = (Document) env.getTxnObject("ShipmentQuoteCall");
					log.verbose("*********** shipment quote call : vertex response : " + XMLUtil.getXMLString(docVertexQuoteResponse));
					
					lvertexTax = this.getVertexTaxAmt(docVertexQuoteResponse, sOrdLineKey);
					log.verbose("********  ShipmentQuoteCall --> vertex tax : " + lvertexTax);
					
					vertexShippingTax = lvertexTax.get("VertexShippingTax") .doubleValue();
					log.verbose("************ ShipmentQuoteCall --> vertex shipping tax : " + vertexShippingTax);
					
					vertexMerchandiseTax = lvertexTax.get( "VertexMerchandiseTax").doubleValue();
					log.verbose("************* ShipmentQuoteCall --> merchandise tax : " + vertexMerchandiseTax);	
					
					// Prorate the Open tax if partial shipment the line
					// As part of #4131 fix - R026H
					double dOpenQty = taxInputStruct.currentQty;
					double dLineQty = taxInputStruct.lineQty; 
					log.verbose("Check for Partial Shipment : Open Qty is : "+dOpenQty+" and Order line Qty is: "+dLineQty);
					if(dOpenQty > 0 && dOpenQty != dLineQty){
						openShippingTax = openShippingTax * (dOpenQty/dLineQty);
						openMerchandiseTax =  openMerchandiseTax * (dOpenQty/dLineQty);
					}
					// End # 4131
					log.verbose("check for vertexShippingTax > openShippingTax is : ");
					if (vertexShippingTax > openShippingTax) 
					{
						// return the open tax
						// update the outputStruct
						// remainingTax = openShippingTax;
						log.verbose("True");
						outputList = this.updateTax(taxInputStruct, lvertexTax, false, openShippingTax, "Shipping", outputList);

					} 
					else 
					{
						// return the new tax
						log.verbose("False");
						outputList = this.updateTax(taxInputStruct, lvertexTax, true, 0, "Shipping", outputList);

					}
					log.verbose("check for vertexMerchandiseTax > openMerchandiseTax is : ");

					if (vertexMerchandiseTax > openMerchandiseTax) {
						// return the open tax
						// update the outputStruct
						// remainingTax = openMerchandiseTax;
						log.verbose("True");
						outputList = this.updateTax(taxInputStruct, lvertexTax, false, openMerchandiseTax, "Merchandise", outputList);

					} 
					else 
					{
						// return the new tax
						log.verbose("False");
						outputList = this.updateTax(taxInputStruct, lvertexTax, true, 0, "Merchandise", outputList);
						
					}

				}
				outputStruct.colTax = outputList;
				this.printOutStructTax(outputStruct);
			} else {
				log.verbose("Calculate tax for : none ");
				// call getOrderLineTaxes and return the same
				// get the orderLineKey from input
				String sOrdHdrKey = taxInputStruct.orderHeaderKey;
				String sOrdLineKey = taxInputStruct.orderLineKey;
				// get the Proforma Invoice # from Env.
				String sInvoiceNo = taxInputStruct.invoiceKey;
				if (env.getTxnObject("InvoiceNo") != null) 
				{
					sInvoiceNo = env.getTxnObject("InvoiceNo").toString();

				}
				log.verbose("OrderHeaderKey is "+sOrdHdrKey+"OrderLineKey is "+sOrdLineKey+"Invoice No is : "+sInvoiceNo);
				
				HashMap<String, Double> invoiceTaxList = this.getOrderLineOpenTaxAmt(env, sOrdHdrKey, sOrdLineKey, sInvoiceNo);

				log.verbose("**************  returning the same tax : "+invoiceTaxList.size());
				// START - Fix for #4403
				if(invoiceTaxList.size() > 0){
					outputList = this.updateTax(taxInputStruct, invoiceTaxList, true, 0, "Shipping", outputList);
					outputList = this.updateTax(taxInputStruct, invoiceTaxList, true, 0, "Merchandise", outputList);
					outputStruct.colTax = outputList;					
				}else{
					/**
					 * This block executes when
					 *  - Refund ( Payment Collection Agent)
					 *  - Cancel , Change Price and Ship To (changeOrder API and Address Verification agent)
					 */
					log.verbose("colTax in inputTaxStruct is : "+taxInputStruct.colTax.toString());
					List taxBrkIn = taxInputStruct.colTax;
					Iterator it01 = taxBrkIn.iterator();
					while(it01.hasNext()){
						YFSExtnTaxBreakup inBrkup = (YFSExtnTaxBreakup)it01.next();
						log.verbose(" Tax Name is : "+inBrkup.taxName+" value is : "+inBrkup.tax);					
					}
					log.verbose("tax : " + taxInputStruct.tax);
					log.verbose("tax percenteage : " + taxInputStruct.taxPercentage);
					outputStruct.colTax = taxInputStruct.colTax;
				}
				//End of #4403
				//outputStruct.tax = taxInputStruct.tax;
				//outputStruct.taxPercentage = taxInputStruct.taxPercentage;
			}
			log.endTimer(" End of AcademySalesOrderRecalculateLineTaxUEImpl-> recalculateLineTax()");
			return outputStruct;
		} catch (Exception e) {
			throw getYFSUserExceptionWithTrace(e);
		}
	}

	/**
	 * Method wraps Exception object to YFSUserExitException object
	 * 
	 * @param e
	 * @return YFSUserExitException
	 */
	private static YFSUserExitException getYFSUserExceptionWithTrace(Exception e) {
		YFSUserExitException yfsUEException = new YFSUserExitException();
		yfsUEException.setStackTrace(e.getStackTrace());
		return yfsUEException;
	}

	private HashMap<String, Double> getOrderLineOpenTaxAmt(YFSEnvironment env, String sOrdHrdKey, String sOrdLineKey, String sInvoiceNo) throws Exception 
	{
		HashMap<String, Double> aMap = new HashMap<String, Double>();
		double totalShippingTax = 0.0;
		double totalMerchandiseTax = 0.0;
		try {
			log.beginTimer(" Begining of AcademySalesOrderRecalculateLineTaxUEImpl (getYFSUserExceptionWithTrace)-> getOrderLineOpenTaxAmt()");
			// call getOrderInvoiceList to get all the proforma Invoices
			Document inDocProformaList = XMLUtil.createDocument("OrderInvoice");
			Element eleInvoice = inDocProformaList.getDocumentElement();
			eleInvoice.setAttribute("InvoiceType", "PRO_FORMA");
			eleInvoice.setAttribute("OrderHeaderKey", sOrdHrdKey);
			env.setApiTemplate("getOrderInvoiceList", "global/template/api/getOrderInvoiceList.LineChargesForShipment.xml");
			Document outDocProformaList = AcademyUtil.invokeAPI(env, "getOrderInvoiceList", inDocProformaList);
			env.clearApiTemplate("getOrderInvoiceList");
			// get the list of invoices for order..
			NodeList nlInvoiceList = outDocProformaList.getElementsByTagName("OrderInvoice");
			int iNoOfInvoice = nlInvoiceList.getLength();
			log.verbose("Total Invoice for Order is : "+iNoOfInvoice);

			for (int i = 0; i < iNoOfInvoice; i++) 
			{
				Element eleOrderInvoice = (Element) nlInvoiceList.item(i);
				String sInvoice = eleOrderInvoice.getAttribute("InvoiceNo");
				log.verbose("Active Invoice No is : "+sInvoice+" Processing Invoice No is : "+sInvoiceNo);
				if (!sInvoice.equals(sInvoiceNo)) 
				{
					// call getOrderInvoiceDetails
					/*Document inDocProformaDetail = XMLUtil.createDocument("GetOrderInvoiceDetails");
					Element eleInvoiceDet = inDocProformaDetail.getDocumentElement();
					eleInvoiceDet.setAttribute("InvoiceType", "PRO_FORMA");
					eleInvoiceDet.setAttribute("OrderHeaderKey", sOrdHrdKey);
					eleInvoiceDet.setAttribute("InvoiceNo", sInvoice);

					Document outDocProformaDetail = AcademyUtil.invokeAPI(env, "getOrderInvoiceDetails", inDocProformaDetail);*/
					NodeList nlLineList = eleOrderInvoice.getElementsByTagName("LineDetail");
					int iNoOfInvoiceLines = nlLineList.getLength();
					log.verbose("********** number of invoice lines : " + iNoOfInvoiceLines);
					for (int j = 0; j < iNoOfInvoiceLines; j++) 
					{
						Element eleInvoiceLine = (Element) nlLineList.item(j);
						log.verbose("processing OrderLineKey is : "+sOrdLineKey+" OrderLineKey of Active invoice is : "+eleInvoiceLine.getAttribute("OrderLineKey"));
						if (sOrdLineKey.equals(eleInvoiceLine.getAttribute("OrderLineKey"))) 
						{
							log.verbose("processing the same order line");
							// get the Taxes
							NodeList nlTaxes = eleInvoiceLine.getElementsByTagName("LineTax");
							int iNoOftaxes = nlTaxes.getLength();
							log.verbose("********** Number of taxes : " + iNoOftaxes);
							
							for (int k = 0; k < iNoOftaxes; k++) 
							{
								Element eleTaxDet = (Element) nlTaxes.item(k);
								String sChargeCategory = eleTaxDet.getAttribute("ChargeCategory");
								String sTaxType = eleTaxDet.getAttribute("ChargeCategory").concat(eleTaxDet.getAttribute("TaxName"));
								log.verbose("********* Tax Type : " + sTaxType);
								
								aMap.put(sTaxType, Double.valueOf(eleTaxDet.getAttribute("Tax")));
								if (sChargeCategory.indexOf("Shipping") > -1) 
								{
									totalShippingTax = totalShippingTax + Double.parseDouble(eleTaxDet.getAttribute("Tax"));
									log.verbose("***** TOTAL Shipping tax : " + totalShippingTax);
								} 
								else if (sChargeCategory.indexOf("TAXES") > -1) 
								{
									totalMerchandiseTax = totalMerchandiseTax + Double.parseDouble(eleTaxDet.getAttribute("Tax"));
									log.verbose("***** TOTAL Merchandise tax : " + totalMerchandiseTax);
								}

							}
						}
					}
				}
			}
			log.endTimer(" End of AcademySalesOrderRecalculateLineTaxUEImpl (getYFSUserExceptionWithTrace)-> getOrderLineOpenTaxAmt()");
		} catch (Exception ex) {
			AcademySalesOrderRecalculateLineTaxUEImpl
					.getYFSUserExceptionWithTrace(ex);
		}
		
		//aMap.put("ProformaShippingTax", Double.valueOf(totalShippingTax));
		//aMap.put("ProformaMerchandiseTax", Double.valueOf(totalMerchandiseTax));
		return aMap;
	}

	private HashMap<String, Double> getVertexTaxAmt(Document docVertexResponse,
			String sOrdLineKey) {
		log.beginTimer(" Begining of AcademySalesOrderRecalculateLineTaxUEImpl (getYFSUserExceptionWithTrace)-> getVertexTaxAmt()");
		HashMap<String, Double> aMap = new HashMap<String, Double>();
		double vertexShippingTax = 0.0;
		double vertexMerchandiseTax = 0.0;
		Boolean isFlatTax = false;
		try {
			log.verbose("Looking new Tax in vertex response for Order Line : "+sOrdLineKey);
			NodeList nlLineList = docVertexResponse
					.getElementsByTagName("LineItem");
			int iNoOfLines = nlLineList.getLength();
			log.verbose("No of LineItem : "+iNoOfLines);
			for (int i = 0; i < iNoOfLines; i++) {
				Element eleLineItem = (Element) nlLineList.item(i);
				
				NodeList nlCodeFields = eleLineItem
						.getElementsByTagName("FlexibleCodeField");
				int iNoOfCodeFields = nlCodeFields.getLength();
				String sFieldValue = "";
				for (int j = 0; j < iNoOfCodeFields; j++) {
					Element eleCodeField = (Element) nlCodeFields.item(j);
					if (eleCodeField.getAttribute("fieldId").equals("3")) {
						sFieldValue = eleCodeField.getTextContent();
					}
				}
				log.verbose("Value for FlexibleCodeField 3 is : "+sFieldValue);
				if (sFieldValue.indexOf(sOrdLineKey) > -1) {
					log.verbose("Found the matching Order line item");					
					NodeList nlTaxes = eleLineItem
							.getElementsByTagName("Taxes");
					int iNoOfTaxes = nlTaxes.getLength();
					log.verbose("No of Taxes are : "+iNoOfTaxes);
					for (int k = 0; k < iNoOfTaxes; k++) {
						Element eleTax = (Element) nlTaxes.item(k);
						String sTaxType = ((Element) eleTax
								.getElementsByTagName("Jurisdiction").item(0))
								.getAttribute("jurisdictionLevel") + "-" + ((Element) eleTax
								.getElementsByTagName("Jurisdiction").item(0)).getAttribute("jurisdictionId");
						String taxAmt = ((Element) eleTax.getElementsByTagName(
								"CalculatedTax").item(0)).getTextContent();
						log.verbose("Tax Type is : "+sTaxType+" tax amount is : "+taxAmt);
						if (sFieldValue.length() != sOrdLineKey.length()) {
							log.verbose("Shipping Tax");
							//OMNI-75300 -START
							if (!eleTax.getAttribute(AcademyConstants.ATTR_TAX_STRUCTURE).equals(AcademyConstants.STR_FLAT_TAX)) {
							sTaxType = "Shipping".concat(sTaxType);
							vertexShippingTax = vertexShippingTax
									+ Double.parseDouble(taxAmt);
								isFlatTax =false;
							} else
							{ 
								isFlatTax =true;
							}
							log.verbose("Flat tax :: " +isFlatTax);
							//OMNI-75300 -END
											} else {
												log.verbose("Merchandise Tax");
							sTaxType = "TAXES".concat(sTaxType);
							vertexMerchandiseTax = vertexMerchandiseTax
									+ Double.parseDouble(taxAmt);
											}
						//NegativeTax Fix start : STL-503
	                       if(aMap.get(sTaxType) != null){
	                           taxAmt = String.valueOf(Double.parseDouble(taxAmt) + aMap.get(sTaxType)); 
	                           log.verbose("MAP is not null");
	                       }
	                     //NegativeTax Fix end : STL-503
						 //OMNI-75300 - Added If-condition
						if(!isFlatTax) {
						aMap.put(sTaxType, Double.valueOf(taxAmt));
						}
						log.verbose("vertex tax results MAP:::" + aMap);            
					}
				}
			}

		} catch (Exception ex) {
			AcademySalesOrderRecalculateLineTaxUEImpl
					.getYFSUserExceptionWithTrace(ex);
		}
				aMap.put("VertexShippingTax", Double.valueOf(vertexShippingTax));
		aMap.put("VertexMerchandiseTax", Double.valueOf(vertexMerchandiseTax));
		log.verbose("Total of VertexShippingTax is : "+vertexShippingTax+" and VertexMerchandiseTax is : "+vertexMerchandiseTax);
		log.endTimer(" End of AcademySalesOrderRecalculateLineTaxUEImpl (getYFSUserExceptionWithTrace)-> getVertexTaxAmt()");
		return aMap;
	}

	private String getTaxType(String sVertexTax) {
		final int DISTRICT = 2;
		final int COUNTY = 1;
		final int STATE = 0;
		String sTaxType = "DISTRICT_TAX";
			switch (get(sVertexTax)) {
		case STATE: {
			
			sTaxType = "STATE_TAX";
			
			return "STATE_TAX";
		}
		case COUNTY: {
						sTaxType = "COUNTY_TAX";
			
			return "COUNTY_TAX";
		}
		case DISTRICT: {
					sTaxType = "DISTRICT_TAX";
			return "DISTRICT_TAX";
		}
		}

		return sTaxType;
	}

	private HashMap<String, Double> getInputTax(
			YFSExtnLineTaxCalculationInputStruct taxInputStruct) {
		log.verbose("Begining of AcademySalesOrderRecalculateLineTaxUEImpl -> getInputTax() ");
		List<YFSExtnTaxBreakup> lTaxList = taxInputStruct.colTax;
		int iNoOfTaxes = lTaxList.size();
		log.verbose("No of tax in YFSExtnLineTaxCalculationInputStruct are : "+iNoOfTaxes);
		double openMerchandiseTax = 0.0;
		double openShippingTax = 0.0;
		HashMap<String, Double> inputTax = new HashMap<String, Double>();
		for (int i = 0; i < iNoOfTaxes; i++) {
			YFSExtnTaxBreakup taxBreak = lTaxList.get(i);
			String sChargeCategory = taxBreak.chargeCategory;
			log.verbose("Tax break up details \n ChargeCategory : "+sChargeCategory);
			log.verbose("Tax is : "+taxBreak.tax);
			if (sChargeCategory.indexOf("Shipping") > -1) {
				openShippingTax = openShippingTax + taxBreak.tax;				
			} else if (sChargeCategory.indexOf("TAXES") > -1) {
				openMerchandiseTax = openMerchandiseTax + taxBreak.tax;
				
			}
		}
		log.verbose("Total OpenShippingTax is : "+openShippingTax+" OpenMerchandiseTax is : "+openMerchandiseTax);
		inputTax.put("OpenShippingTax", openShippingTax);
		inputTax.put("OpenMerchandiseTax", openMerchandiseTax);
		log.verbose("Ending of AcademySalesOrderRecalculateLineTaxUEImpl -> getInputTax() ");
		return inputTax;
	}

	private List<YFSExtnTaxBreakup> updateTax(YFSExtnLineTaxCalculationInputStruct taxInputStruct, HashMap<String, Double> NewTax, 
			boolean override,double invoiceTax, String isShipTax, List<YFSExtnTaxBreakup> outTaxList)
	{
		log.beginTimer(" Begining of AcademySalesOrderRecalculateLineTaxUEImpl (getYFSUserExceptionWithTrace)-> updateTax()");
		Set<String> taxes = NewTax.keySet();
		log.verbose("number of taxes : " + taxes.size());

		Iterator<String> iter = taxes.iterator();
		YFSExtnTaxBreakup outputTax = null;
		while (iter.hasNext()) {
			String sTaxType = iter.next();
			log.verbose("Tax Type : "+sTaxType);
			double tax = NewTax.get(sTaxType).doubleValue();
			log.verbose("Tax Amount is : "+tax);
			if (sTaxType.indexOf("Vertex") < 0) {
				log.verbose("Vertex Tax ");
				if (isShipTax.indexOf("Shipping") > -1) {
					log.verbose("Tax for Shipping.....");
					if (sTaxType.indexOf("Shipping") > -1) {
						outputTax = new YFSExtnTaxBreakup();
						outputTax.chargeCategory = "Shipping";
						outputTax.chargeName = "ShippingCharge";
						outputTax.taxName = sTaxType.substring(8, sTaxType
								.length());
						log.verbose("sTaxType is : "+sTaxType+"\n Override previous tax is : "+override);
						if (!override) {
							log.verbose(" Check for tax > invoiceTax :  tax is "+tax+" and invoiceTax is : "+invoiceTax);
							if (tax > invoiceTax) {
								outputTax.tax = invoiceTax;
								log.verbose("set tax with invoicetax");
								outTaxList.add(outputTax);
								break;
							} else {
								log.verbose("set new tax");
								outputTax.tax = tax;
								invoiceTax = invoiceTax - tax;
								log.verbose("invoiceTax-tax : "+invoiceTax);
							}
						} else {
							log.verbose("reset the tax "+ tax);
							outputTax.tax = tax;	
							
						}
						outTaxList.add(outputTax);
					}
				} else if (isShipTax.indexOf("Merchandise") > -1) {
					log.verbose("Tax for Merchandise.....");
					if (sTaxType.indexOf("TAXES") > -1) {
						outputTax = new YFSExtnTaxBreakup();
						outputTax.chargeCategory = "TAXES";
						outputTax.chargeName = "Taxes";
						outputTax.taxName = sTaxType.substring(5, sTaxType
								.length());
						log.verbose("sTaxType is : "+sTaxType+"\n Override previous tax is : "+override);
						if (!override) {
							log.verbose(" Check for tax > invoiceTax :  tax is "+tax+" and invoiceTax is : "+invoiceTax);
							if (tax > invoiceTax) {
								outputTax.tax = invoiceTax;
								log.verbose("set tax with invoicetax");
								outTaxList.add(outputTax);
								break;
							} else {
								log.verbose("set new tax");
								outputTax.tax = tax;
								invoiceTax = invoiceTax - tax;
								log.verbose("invoiceTax-tax : "+invoiceTax);
								
							}
						} else {
							log.verbose("reset the tax "+ tax);
							outputTax.tax = tax;
						}
						outTaxList.add(outputTax);
					}
				} else if (isShipTax.indexOf("Appeasement") > -1) {
						outputTax = new YFSExtnTaxBreakup();
						if (sTaxType.indexOf("TAXES") > -1) {
						outputTax.chargeCategory = "TAXES";
						outputTax.chargeName = "Taxes";
						outputTax.taxName = sTaxType.substring(5, sTaxType.length());
						outputTax.tax = -(tax);
						log.verbose("Appeasement tax name : " + outputTax.taxName);
						log.verbose("appeasement tax : " + outputTax.tax);
						outTaxList.add(outputTax);
					}
				}

			}
		}
		log.endTimer(" End of AcademySalesOrderRecalculateLineTaxUEImpl (getYFSUserExceptionWithTrace)-> updateTax()");
		return outTaxList;
	}

	private HashMap<String, Double> getProformaTax(YFSEnvironment env,
			String sShipmentKey, String sOrdLineKey) throws Exception {

		HashMap<String, Double> aMap = new HashMap<String, Double>();
		double totalShippingTax = 0.0;
		double totalMerchandiseTax = 0.0;

		try {
			log.beginTimer(" Begining of AcademySalesOrderRecalculateLineTaxUEImpl (getYFSUserExceptionWithTrace)-> getProformaTax()");
			// call getOrderInvoiceList to get all the proforma Invoices
			Document inDocProformaList = XMLUtil.createDocument("OrderInvoice");
			Element eleInvoice = inDocProformaList.getDocumentElement();
			eleInvoice.setAttribute("InvoiceType", "PRO_FORMA");
			eleInvoice.setAttribute("ShipmentKey", sShipmentKey);
			env.setApiTemplate("getOrderInvoiceList", "global/template/api/getOrderInvoiceList.LineChargesForShipment.xml");
			Document outDocProformaList = AcademyUtil.invokeAPI(env,
					"getOrderInvoiceList", inDocProformaList);
			env.clearApiTemplate("getOrderInvoiceList");
			// get the list of invoices for order..
		
			NodeList nlInvoiceList = outDocProformaList
					.getElementsByTagName("OrderInvoice");
			int iNoOfInvoice = nlInvoiceList.getLength();

			for (int i = 0; i < iNoOfInvoice; i++) {
				Element eleOrderInvoice = (Element) nlInvoiceList.item(i);
				String sInvoice = eleOrderInvoice.getAttribute("InvoiceNo");
				// Comment the getOrderInvoiceDetails API call as part of code clean up
				// call getOrderInvoiceDetails
				/*Document inDocProformaDetail = XMLUtil
						.createDocument("GetOrderInvoiceDetails");
				Element eleInvoiceDet = inDocProformaDetail
						.getDocumentElement();
				eleInvoiceDet.setAttribute("InvoiceType", "PRO_FORMA");
				eleInvoiceDet.setAttribute("InvoiceNo", sInvoice);

				Document outDocProformaDetail = AcademyUtil.invokeAPI(env,
						"getOrderInvoiceDetails", inDocProformaDetail);*/
				NodeList nlLineList = eleOrderInvoice.getElementsByTagName("LineDetail");
				int iNoOfInvoiceLines = nlLineList.getLength();
				for (int j = 0; j < iNoOfInvoiceLines; j++) {
					Element eleInvoiceLine = (Element) nlLineList.item(j);
					if (sOrdLineKey.equals(eleInvoiceLine
							.getAttribute("OrderLineKey"))) {
						// get the Taxes
						NodeList nlTaxes = eleInvoiceLine
								.getElementsByTagName("LineTax");
						int iNoOftaxes = nlTaxes.getLength();
						for (int k = 0; k < iNoOftaxes; k++) {
							Element eleTaxDet = (Element) nlTaxes.item(k);
							String sChargeCategory = eleTaxDet
									.getAttribute("ChargeCategory");
							String sTaxType = sChargeCategory.concat(eleTaxDet
									.getAttribute("TaxName"));
							aMap.put(sTaxType, Double.valueOf(eleTaxDet
									.getAttribute("Tax")));

							if (sChargeCategory.indexOf("Shipping") > -1) {
								totalShippingTax = totalShippingTax
										+ Double.parseDouble(eleTaxDet
												.getAttribute("Tax"));
								
							} else if (sChargeCategory.indexOf("TAXES") > -1) {
								totalMerchandiseTax = totalMerchandiseTax
										+ Double.parseDouble(eleTaxDet
												.getAttribute("Tax"));
								
							}

						}
					}
				}
			}
			log.endTimer(" End of AcademySalesOrderRecalculateLineTaxUEImpl (getYFSUserExceptionWithTrace)-> getProformaTax()");
		} catch (Exception ex) {
			AcademySalesOrderRecalculateLineTaxUEImpl
					.getYFSUserExceptionWithTrace(ex);
		}
	aMap.put("ProformaShippingTax", Double.valueOf(totalShippingTax));
		aMap.put("ProformaMerchandiseTax", Double.valueOf(totalMerchandiseTax));

		return aMap;
	}

	private List<YFSExtnTaxBreakup> setChangeOrderInEnv(YFSEnvironment env,
			List<YFSExtnTaxBreakup> outputList, double discount) {

		YFSExtnTaxBreakup outputTax = new YFSExtnTaxBreakup();
		
		/** Start Fix - 9764 **/
		outputTax.chargeCategory = "TAX_WRITEOFF";
		outputTax.chargeName = "TAX_WRITEOFF";
		outputTax.tax = discount;
		outputTax.taxName = "INTERNAL_TAX_WRITEOFF";
		/** End Fix - 9764 **/
		
		
		/*
		 * try { Document docchangeOrderIn = null; if
		 * (env.getTxnObject("ChangeOrderOnInvoiceCreation") == null) {
		 * docchangeOrderIn = XMLUtil.createDocument("Order"); Element
		 * elechangeOrder = docchangeOrderIn.getDocumentElement();
		 * elechangeOrder.setAttribute("Action", "MODIFY");
		 * elechangeOrder.setAttribute("OrderHeaderKey", sOrdHdrKey); Element
		 * elePayMethds = docchangeOrderIn .createElement("PaymentMethods");
		 * elechangeOrder.appendChild(elePayMethds); Element elePayMethod =
		 * docchangeOrderIn .createElement("PaymentMethod");
		 * elePayMethds.appendChild(elePayMethod);
		 * elePayMethod.setAttribute("PaymentType", "INTERN_TAX_WRITEOFF");
		 * elePayMethod.setAttribute("MaxChargeLimit", "9999");
		 * elePayMethod.setAttribute("SuspendAnyMoreCharges", "N");
		 * elePayMethod.setAttribute("UnlimitedCharges", "N");
		 * elePayMethod.setAttribute("SaveToCustomer", "N");
		 * elePayMethod.setAttribute("PaymentReference1", sOrdHdrKey);
		 * 
		 * Element elePayDetail = docchangeOrderIn
		 * .createElement("PaymentDetails");
		 * elePayMethod.appendChild(elePayDetail);
		 * elePayDetail.setAttribute("AuthorizationID", sOrdHdrKey);
		 * elePayDetail.setAttribute("RequestAmount", Double
		 * .toString(discount)); elePayDetail.setAttribute("ChargeType",
		 * "CHARGE"); elePayDetail.setAttribute("ProcessedAmount", Double
		 * .toString(discount));
		 * 
		 * } else { docchangeOrderIn = (Document) env
		 * .getTxnObject("ChangeOrderOnInvoiceCreation"); NodeList nlPaymentList
		 * = docchangeOrderIn .getElementsByTagName("PaymentMethod"); int
		 * iNoOfPayMethod = nlPaymentList.getLength(); for (int i = 0; i <
		 * iNoOfPayMethod; i++) { Element elePayMethod = (Element)
		 * nlPaymentList.item(i); if
		 * (elePayMethod.getAttribute("PaymentType").equals(
		 * "INTERN_TAX_WRITEOFF")) {
		 * 
		 * Element elePayDetail = (Element) elePayMethod
		 * .getElementsByTagName("PaymentDetails").item(0); discount =
		 * Double.parseDouble(elePayMethod .getAttribute("ProcessedAmount")) +
		 * discount; elePayDetail.setAttribute("ProcessedAmount", Double
		 * .toString(discount)); elePayDetail.setAttribute("RequestProcessed",
		 * Double .toString(discount));
		 * elePayDetail.setAttribute("RequestAmount", Double
		 * .toString(discount)); } } } System.out.println("change Order input "
		 * + XMLUtil.getXMLString(docchangeOrderIn));
		 * env.setTxnObject("ChangeOrderOnInvoiceCreation", docchangeOrderIn);
		 * 
		 * } catch (Exception ex) { AcademySalesOrderRecalculateLineTaxUEImpl
		 * .getYFSUserExceptionWithTrace(ex); }
		 */
		outputList.add(outputTax);
		return outputList;
	}

	private Document getCompleteSalesOrderDetails(YFSEnvironment env, String orderHeaderKey) {

		Document docCompleteOrderDetails = null;
		Document docApiInput = null;
		Element eleOrder = null;
		try {
			env.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS,
					AcademyConstants.STR_TEMPLATEFILE_ORDERDETAILS_UEIMPL);
			docApiInput = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			eleOrder = docApiInput.getDocumentElement();
			eleOrder.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY,
					orderHeaderKey);
			docCompleteOrderDetails = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS,
					docApiInput);

		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException(e.getMessage());
		}

		return docCompleteOrderDetails;
	}
	
	private void printOutStructTax(YFSExtnTaxCalculationOutStruct outputStruct) {
		List<YFSExtnTaxBreakup> lTaxList = outputStruct.colTax;
		int iNoOfTaxes = lTaxList.size();
		for (int i = 0; i < iNoOfTaxes; i++) {
			YFSExtnTaxBreakup taxBreak = lTaxList.get(i);
			
		}
	}
}
