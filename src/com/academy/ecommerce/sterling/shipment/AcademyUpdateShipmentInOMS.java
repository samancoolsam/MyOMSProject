package com.academy.ecommerce.sterling.shipment;

import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.academy.util.constants.AcademyConstants;
import org.w3c.dom.Node;

public class AcademyUpdateShipmentInOMS implements YIFCustomApi {
	private static final YFCLogCategory log = YFCLogCategory
			.instance(AcademyUpdateShipmentInOMS.class);

	public static String strPrimeLineNo = "";
	
	public static String InvoiceNo = "";
	public static String AmountCollected = "";
	public static String Currency = "";
	public static String DateInvoiced = "";
	public static String InvoiceType = "";
	public static String LineSubTotal = "";
	public static String MasterInvoiceNo = "";
	public static String OrderInvoiceKey = "";
	public static String OrderNo = "";
	public static String TotalAmount = "";
	public static String TotalTax = "";

	public static String strSubLineNo = "";

	public void setProperties(Properties arg0) throws Exception {
	}

	public Document updateShipment(YFSEnvironment env, Document inXML)
			throws Exception {
		log.verbose("Beginning of AcademyUpdateShipmentInOMS-->updateShipment");
		log.verbose("changeShipment Request XML from WMS::"
				+ XMLUtil.getXMLString(inXML));
		Document responseXMLToWMS = null;

		try {
			String strShipmentNo = "";
			String strEnterpriseCode = "";
			String strIsPrintBatchPackSlipRequired = "";
			String strDocumentType = "";
			Document docOutputChangeShipment = null;
			Document docInputChangeShipment = null;
			Document docInputGetShipmentList = null;
			Document docOutputGetShipmentList = null;

			Element eleSoapShipment = inXML.getDocumentElement();

			eleSoapShipment.removeAttribute("ShipmentKey");

			docInputChangeShipment = prepareInputToChangeShipment(inXML);
			log.verbose("input to changeShipment api"
					+ XMLUtil.getXMLString(docInputChangeShipment));

			docOutputChangeShipment = AcademyUtil.invokeAPI(env,
					"changeShipment", docInputChangeShipment);

			log.verbose("output of changeShipment api"
					+ XMLUtil.getXMLString(docOutputChangeShipment));

			strShipmentNo = eleSoapShipment.getAttribute("ShipmentNo");
			strEnterpriseCode = eleSoapShipment.getAttribute("EnterpriseCode");
			strDocumentType = eleSoapShipment.getAttribute("DocumentType");
			docInputGetShipmentList = XMLUtil.createDocument("Shipment");
			docInputGetShipmentList.getDocumentElement().setAttribute(
					"ShipmentNo", strShipmentNo);
			docInputGetShipmentList.getDocumentElement().setAttribute(
					"EnterpriseCode", strEnterpriseCode);
			docInputGetShipmentList.getDocumentElement().setAttribute(
					"DocumentType", strDocumentType);
			env
					.setApiTemplate("getShipmentList",
							"global/template/api/getShipmentList.CreateShipmentToWMS.xml");
			log.verbose("getShipmentList api input"
					+ XMLUtil.getXMLString(docInputGetShipmentList));
			docOutputGetShipmentList = AcademyUtil.invokeAPI(env,
					"getShipmentList", docInputGetShipmentList);
			log.verbose("getShipmentList api output"
					+ XMLUtil.getXMLString(docOutputGetShipmentList));

			responseXMLToWMS = calculateShippingTaxAndItemLineTax(
					docOutputGetShipmentList, inXML);
			strIsPrintBatchPackSlipRequired = (String) env
					.getTxnObject("IsPrintBatchPackSlipRequired");

			responseXMLToWMS.getDocumentElement().setAttribute(
					"IsPrintBatchPackSlipRequired",
					strIsPrintBatchPackSlipRequired);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		log.verbose("ResponseXML To WMS-->updateShipment"
				+ XMLUtil.getXMLString(responseXMLToWMS));
		log.verbose("End AcademyUpdateShipmentInOMS-->updateShipment");
		return responseXMLToWMS;
	}

	public Document prepareInputToChangeShipment(Document inXML)
			throws ParserConfigurationException {
		log.verbose("prepareInputToChangeShipment-->input::"
				+ XMLUtil.getXMLString(inXML));
		Document changeShipmentInDoc = XMLUtil.createDocument("Shipment");
		Element inEle = inXML.getDocumentElement();
		changeShipmentInDoc.getDocumentElement().setAttribute("Action",
				inEle.getAttribute("Action"));
		changeShipmentInDoc.getDocumentElement().setAttribute("DocumentType",
				inEle.getAttribute("DocumentType"));
		changeShipmentInDoc.getDocumentElement().setAttribute("EnterpriseCode",
				inEle.getAttribute("EnterpriseCode"));
		changeShipmentInDoc.getDocumentElement().setAttribute("ShipmentNo",
				inEle.getAttribute("ShipmentNo"));
		changeShipmentInDoc.getDocumentElement().setAttribute("ShipNode",
				inEle.getAttribute("ShipNode"));
		changeShipmentInDoc.getDocumentElement().setAttribute(
				"SellerOrganizationCode",
				inEle.getAttribute("SellerOrganizationCode"));
		changeShipmentInDoc.getDocumentElement().setAttribute(
				"OverrideModificationRules",
				inEle.getAttribute("OverrideModificationRules"));
		Element eleShipmentLines = (Element) inEle.getElementsByTagName(
				"ShipmentLines").item(0);

		NodeList nlShipmentLine = eleShipmentLines
				.getElementsByTagName("ShipmentLine");

		for (int i = 0; i < nlShipmentLine.getLength(); i++) {
			Element eleShipmentLine = (Element) nlShipmentLine.item(i);
			eleShipmentLine.removeAttribute("OrderNo");
			eleShipmentLine.removeAttribute("ReleaseNo");
			eleShipmentLine.removeAttribute("RequestedTagNo");
			eleShipmentLine.removeAttribute("YFC_NODE_NUMBER");

			String strAction = eleShipmentLine.getAttribute("Action");

			if (strAction.equalsIgnoreCase("Delete")) {
				changeShipmentInDoc.getDocumentElement().setAttribute(
						"SellerOrganizationCode",
						inEle.getAttribute("EnterpriseCode"));
				changeShipmentInDoc.getDocumentElement().setAttribute(
						"OverrideModificationRules", "Y");
			}
		}

		Element eleScacAndService = (Element) inEle.getElementsByTagName(
				"ScacAndService").item(0);
		if ((!YFCObject.isVoid(eleShipmentLines))
				&& (!YFCObject.isNull(eleShipmentLines))) {
			changeShipmentInDoc.getDocumentElement().appendChild(
					changeShipmentInDoc.importNode(eleShipmentLines, true));
		
			 //STL-1589 starts Adding backorderRemovedQuantity
			 changeShipmentInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_BACK_ORD_REM_QTY,
					inEle.getAttribute(AcademyConstants.ATTR_BACK_ORD_REM_QTY));
		}
		if ((!YFCObject.isVoid(eleScacAndService))
				&& (!YFCObject.isNull(eleScacAndService))) {
			changeShipmentInDoc.getDocumentElement().appendChild(
					changeShipmentInDoc.importNode(eleScacAndService, true));
		}
		log.verbose("prepareInputToChangeShipment-->output"
				+ XMLUtil.getXMLString(changeShipmentInDoc));
		return changeShipmentInDoc;
	}

	public Document calculateShippingTaxAndItemLineTax(Document OutDoc,
			Document inDoc) throws Exception {
				log.verbose("Output Of getShipmentList API to  calculateShippingTaxAndItemLineTax Method " + XMLUtil.getXMLString(OutDoc));

		log.verbose("Input Document Which should be converted as Response " + XMLUtil.getXMLString(inDoc));

		//START STL-1654
		NodeList nLineTax = null;
		Element eleLineTaxList = null;
		Element eleLineChargeList = null;
		Element eleLineTax = null;
		String chargeCategory = null;
		String strShippingTaxAtLineLevel = null;
		String strItemTaxAtLineLevel = null;
		
		NodeList nLineCharge = null;
		Element eleLineCharge = null;
		String isDiscount = null;
		String chargeAmount = null;
		String chargeName = null;
		
		//START STL-1654
		double shippingChargeAtLineLevel = 0.0D;
	    double discountForLine = 0.0D;
	    String itemDiscType = "";
	    double shippingTaxAtLineLevel = 0.0D;
		double itemTaxAtLineLevel = 0.0D;
		//END STL-1654
		
		//STL 1228 : Added LineDetail and LineCharges to the Response which will go to WMS : START
		Element eleShipments = OutDoc.getDocumentElement();
		NodeList nlShipment = eleShipments.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
		Element eleShipment = (Element) nlShipment.item(0);
		Element eleOrderInvList = (Element) eleShipment.getElementsByTagName(AcademyConstants.ELE_ORDER_INVOICE_LIST).item(0);

		if (!YFCObject.isVoid(eleOrderInvList))
		{
			Node importEle = inDoc.importNode(eleOrderInvList, true);
			Element rootEle = (Element) inDoc.getDocumentElement();
			rootEle.appendChild(importEle);
			Element eleOrderInv = (Element) eleOrderInvList.getElementsByTagName(AcademyConstants.ELE_ORDER_INVOICE).item(0);

			if (!YFCObject.isVoid(eleOrderInv))
			{
				Element eleOrderInvoiceExtn = inDoc.createElement(AcademyConstants.ELE_EXTN);
				inDoc.getElementsByTagName(AcademyConstants.ELE_ORDER_INVOICE).item(0).appendChild(eleOrderInvoiceExtn);
				eleOrderInvoiceExtn.setAttribute(AcademyConstants.ATTR_EXTN_ORDER_INVOICE_KEY, eleOrderInv.getAttribute(AcademyConstants.ATTR_ORDER_INVOICE_KEY));

				((Element) inDoc.getElementsByTagName(AcademyConstants.ELE_ORDER_INVOICE_LIST).item(0)).removeAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS);
				((Element) inDoc.getElementsByTagName(AcademyConstants.ELEM_LINE_DETAILS).item(0)).removeAttribute(AcademyConstants.ATTR_TOT_NO_RECORDS);

				/*START STL-1654 comment out below code
				double shippingTaxAtLineLevel = 0.0D;
				double itemTaxAtLineLevel = 0.0D;*/
			    
				Element eleLineDtls = (Element) eleShipment.getElementsByTagName(AcademyConstants.ELEM_LINE_DETAILS).item(0);
				/*if (!YFCObject.isVoid(eleLineDtls))
				{*/
					NodeList lineDtlNL = eleLineDtls.getElementsByTagName(AcademyConstants.ELEM_LINE_DETAIL);
					for (int lineDtlloop = 0; lineDtlloop < lineDtlNL.getLength(); lineDtlloop++) {
						
						Element eleLineDtl = (Element) lineDtlNL.item(lineDtlloop);						
						String strPrimeLineNo = eleLineDtl.getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO);
						/*START STL-1654 comment out below code
						
						//log.verbose("primelineno is :::::::::" + strPrimeLineNo);
						Element eleShipmentLines = (Element) eleShipments.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINES).item(0);
						NodeList nShipmentLine = XMLUtil.getNodeList(eleShipmentLines,AcademyConstants.ELE_SHIPMENT_LINE);
						log.verbose("shipmentline nl len "+ nShipmentLine.getLength());
						if (!YFCObject.isVoid(nShipmentLine)) {

							for(int j=0;j<nShipmentLine.getLength();j++)
							{
								Element eleShipmentLine = (Element) nShipmentLine.item(j);
								String strSLPrimeLineNo = eleShipmentLine.getAttribute(AcademyConstants.ATTR_PRIME_LINE_NO);
								log.verbose("primelineno is :::::::::"+ strPrimeLineNo);

								if(strPrimeLineNo.equals(strSLPrimeLineNo))
								{
									Element eleOrderLine = (Element) eleShipmentLine.getElementsByTagName(AcademyConstants.ELEM_ORDER_LINE).item(0);

									if (!YFCObject.isVoid(eleOrderLine)) {
										Element eleLineTaxes = (Element) eleOrderLine.getElementsByTagName(AcademyConstants.ELE_LINE_TAXES).item(0);

										if (!YFCObject.isVoid(eleLineTaxes)) {

											NodeList nLineTax = eleLineTaxes.getElementsByTagName(AcademyConstants.ELE_LINE_TAX);

											for (int k = 0; k < nLineTax.getLength(); k++)
											{
												Element eleLineTax = (Element) nLineTax.item(k);
												log.verbose("elelinetax***" +XMLUtil.getElementXMLString(eleLineTax));
												String chargeCategory = eleLineTax.getAttribute(AcademyConstants.ATTR_CHARGE_CATEGORY);

												if (AcademyConstants.STR_SHIPPING.equals(chargeCategory))
												{
													String strShippingTaxAtLineLevel = eleLineTax.getAttribute(AcademyConstants.ATTR_TAX);

													shippingTaxAtLineLevel = shippingTaxAtLineLevel+ Double.parseDouble(strShippingTaxAtLineLevel);

													log.verbose("shippingTaxAtLineLevel"+Double.parseDouble(strShippingTaxAtLineLevel));
												}
												if (AcademyConstants.STR_TAXES.equals(chargeCategory))
												{
													String strItemTaxAtLineLevel = eleLineTax.getAttribute(AcademyConstants.ATTR_TAX);

													itemTaxAtLineLevel = itemTaxAtLineLevel + Double.parseDouble(strItemTaxAtLineLevel);
													log.verbose("itemTaxAtLineLevel" + Double.parseDouble(strItemTaxAtLineLevel));
												}
											}
										}
									}
								}
							}
						}

						END STL-1654 comment out below code
					*/
						//START STL-1654 Adding new logic to stamp EXTN attributes
						shippingTaxAtLineLevel = 0.00;
						itemTaxAtLineLevel = 0.00;
						shippingChargeAtLineLevel = 0.00;
						discountForLine = 0.00;
						itemDiscType = "";
						
		                //Element eleLineDetail = (Element)lineDtlNL.item(lineDtlloop);

		                if (!YFCObject.isVoid(eleLineDtl)) {
		                	eleLineTaxList = (Element)eleLineDtl.getElementsByTagName(AcademyConstants.ELEMENT_LINE_TAX_LIST).item(0);		                	
		                	if (!YFCObject.isVoid(eleLineTaxList))
		                	{
		                		nLineTax = eleLineTaxList.getElementsByTagName(AcademyConstants.ELE_LINE_TAX);

		                		for (int iLineTaxCunt = 0; iLineTaxCunt < nLineTax.getLength(); iLineTaxCunt++)
		                		{
		                			eleLineTax = (Element) nLineTax.item(iLineTaxCunt);
		                			log.verbose("elelinetax***" +XMLUtil.getElementXMLString(eleLineTax));
		                			chargeCategory = eleLineTax.getAttribute(AcademyConstants.ATTR_CHARGE_CATEGORY);

		                			if (AcademyConstants.STR_SHIPPING.equals(chargeCategory))
		                			{
		                				strShippingTaxAtLineLevel = eleLineTax.getAttribute(AcademyConstants.ATTR_TAX);
		                				shippingTaxAtLineLevel = shippingTaxAtLineLevel+ Double.parseDouble(strShippingTaxAtLineLevel);
		                				log.verbose("shippingTaxAtLineLevel"+Double.parseDouble(strShippingTaxAtLineLevel));
		                			}
		                			if (AcademyConstants.STR_TAXES.equals(chargeCategory)) 
		                			{
		                				strItemTaxAtLineLevel = eleLineTax.getAttribute(AcademyConstants.ATTR_TAX);
		                				itemTaxAtLineLevel = itemTaxAtLineLevel + Double.parseDouble(strItemTaxAtLineLevel);
		                				log.verbose("itemTaxAtLineLevel" + Double.parseDouble(strItemTaxAtLineLevel));
		                			}
		                		}
		                	}
		                	
		                	eleLineChargeList = (Element)eleLineDtl.getElementsByTagName(AcademyConstants.ELEMENT_LINE_CHARGE_LIST).item(0);
		                	if (!YFCObject.isVoid(eleLineChargeList))
		                	{
		                		nLineCharge = XMLUtil.getNodeList(eleLineChargeList, AcademyConstants.ELE_LINE_CHARGE);
		                		log.verbose("linetax nllen " + nLineCharge.getLength());
		                		for (int iLineChargeCunt = 0; iLineChargeCunt < nLineCharge.getLength(); iLineChargeCunt++) {
		                			eleLineCharge = (Element)nLineCharge.item(iLineChargeCunt);
		                			isDiscount = eleLineCharge.getAttribute(AcademyConstants.ATTR_IS_DISCOUNT);
		                			chargeAmount = eleLineCharge.getAttribute(AcademyConstants.ATTR_CHARGE_AMT);
		                			chargeName = eleLineCharge.getAttribute(AcademyConstants.ATTR_CHARGE_NAME);
		                			if (chargeName.contains(AcademyConstants.STR_SHIPPING))
		                			{
		                				if (AcademyConstants.STR_YES.equalsIgnoreCase(isDiscount)){
		                					shippingChargeAtLineLevel -= Double.parseDouble(chargeAmount);
		                				}
		                				else {
		                					shippingChargeAtLineLevel += Double.parseDouble(chargeAmount);
		                					}
		                			}
		                			if ((AcademyConstants.STR_BOGO.equals(chargeName)) || (AcademyConstants.STR_DISCOUNT_COUPON.equals(chargeName)))
		                			{
		                				if (AcademyConstants.STR_YES.equalsIgnoreCase(isDiscount)) {
		                					discountForLine += Double.parseDouble(chargeAmount);
		                					itemDiscType = AcademyUtil.getItemDiscTypeNames(chargeName, itemDiscType, nLineCharge, iLineChargeCunt);
		                				}
		                			}
		                		}
		                	}
		               // }
					//}
					//END STL-1654 Adding new logic to stamp EXTN attributes
						Element eleLineDetailExtn = inDoc.createElement(AcademyConstants.ELE_EXTN);
						eleLineDetailExtn.setAttribute(AcademyConstants.ATTR_EXTN_SHIPPING_TAX, String.valueOf(shippingTaxAtLineLevel));
						eleLineDetailExtn.setAttribute(AcademyConstants.ATTR_EXTN_ITEM_TAX, String.valueOf(itemTaxAtLineLevel));
						Element eleOutLineDetail = XMLUtil.getElementByXPath(inDoc, AcademyConstants.XPATH_LINE_DETAIL + "[@PrimeLineNo='"+strPrimeLineNo+"']");
						eleOutLineDetail.appendChild(eleLineDetailExtn);
						//START STL-1654 Adding missing EXTN attributes
						/*shippingTaxAtLineLevel = 0.0D;
						itemTaxAtLineLevel = 0.0D;
						*/				
						
						eleLineDetailExtn.setAttribute(AcademyConstants.ELE_EXTN_SHIP_CHARGE_AT_LINE, String.valueOf(shippingChargeAtLineLevel));
			            log.verbose("ExtnShipChargeAtLine***" + String.valueOf(shippingChargeAtLineLevel));
			            eleLineDetailExtn.setAttribute(AcademyConstants.ELE_EXTN_DISCOUNT_FOR_LINE, String.valueOf(discountForLine));
			            log.verbose("ExtnDiscountForLine***" + String.valueOf(discountForLine));
			            eleLineDetailExtn.setAttribute(AcademyConstants.ELE_EXTN_ITME_DISC_TYPE, itemDiscType);
			            log.verbose("ExtnItemDiscType***" + itemDiscType);
			            
			          //END STL-1654 Adding missing EXTN attributes
					}
				}
			}
		}
		log.verbose("OMS Final Response To WMS :: "+ XMLUtil.getXMLString(inDoc));

		//STL 1228 : Added LineDetail and LineCharges to the Response which will go to WMS : END

		return inDoc;
	}

}
