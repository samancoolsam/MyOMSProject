package com.academy.ecommerce.sterling.financialtran;

import java.io.File;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.shipment.AcademyChangeToVertexCallRequest;
import com.academy.ecommerce.sterling.util.AcademyReturnOrderUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.util.YFCUtils;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyRecordFinTranForInvoice implements YIFCustomApi
{

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyRecordFinTranForInvoice.class);
	public boolean isHeaderTaxConsumed = false;
	public void setProperties(Properties arg0) throws Exception
	{

	}

	/**
	 * This method is invoiced from the service AcademyRecordFinTranOnPublishInvoice and this service is invoked while publishing the
	 * invoice(Shipment invoice, Return invoice and CreditMemo)
	 * 
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public Document manageFinTranForInvoice(YFSEnvironment env, Document inDoc) throws Exception
	{
		log.beginTimer(" Begining of AcademyRecordFinTranForInvoice-> manageFinTranForInvoice Api");
		YFCDocument acadFinTranDoc = YFCDocument.createDocument("MultiApi");
		YFCElement acadFinTranEle = acadFinTranDoc.getDocumentElement();
		String invoiceType = XMLUtil.getString(inDoc, "/InvoiceDetail/InvoiceHeader/@InvoiceType");

		log.verbose("Invoice Type = " + invoiceType);
		log.verbose("Invoice Type = " + invoiceType);

		// Get Finance Codes from common code
		Document acadFinTransCommonCodeDoc = AcademyRecordFinTranUtil.getAcademyFinTranCodeList(env);

		if ("SHIPMENT".equalsIgnoreCase(invoiceType))
		{
			manageFinTranForSalesInvoice(env, inDoc, acadFinTranEle, acadFinTransCommonCodeDoc);
		}
		else if ("RETURN".equalsIgnoreCase(invoiceType))
		{
			manageFinTranForReturnInvoice(env, inDoc, acadFinTranEle, acadFinTransCommonCodeDoc);
		}
		else if ("CREDIT_MEMO".equalsIgnoreCase(invoiceType))
		{
			manageFinTranForCreditMemo(env, inDoc, acadFinTranEle, acadFinTransCommonCodeDoc);
		}
		log.endTimer(" End of AcademyRecordFinTranForInvoice-> manageFinTranForInvoice Api");
		return acadFinTranDoc.getDocument();
	}

	/**
	 * Creates extended DB API input for ACAD_FIN_TRANSACT table. Input is getOrderInvoiceDetails output of a shipment invoice
	 * 
	 * @param env
	 * @param inDoc
	 * @param acadFinTranEle
	 * @return
	 * @throws Exception
	 */
	public YFCElement manageFinTranForSalesInvoice(YFSEnvironment env, Document inDoc, YFCElement acadFinTranEle, Document acadFinTransCommonCodeDoc)
			throws Exception
	{
		log.beginTimer("manageFinTranForSalesInvoice");
		createInputForSalesInvoiceRecord(env, inDoc, acadFinTranEle, acadFinTransCommonCodeDoc);
		log.endTimer("manageFinTranForSalesInvoice");
		return acadFinTranEle;
	}

	/**
	 * Invokes a method which creates extended DB API input for ACAD_FIN_TRANSACT table. Input is getOrderInvoiceDetails output of a return
	 * invoice.
	 * 
	 * @param env
	 * @param inDoc
	 * @param acadFinTranEle
	 * @return
	 * @throws Exception
	 */
	public YFCElement manageFinTranForReturnInvoice(YFSEnvironment env, Document inDoc, YFCElement acadFinTranEle, Document acadFinTransCommonCodeDoc)
			throws Exception
	{
		createInputForReturnInvoiceRecord(env, inDoc, acadFinTranEle, acadFinTransCommonCodeDoc);
		return acadFinTranEle;
	}

	/**
	 * Invokes a method which creates extended DB API input for ACAD_FIN_TRANSACT table. Input is getOrderInvoiceDetails output of a
	 * CREDIT_MEMO. CREDIT_MEMO is created when Appeasement is given post shipment on an order which has CC as payment type.
	 * 
	 * @param env
	 * @param inDoc
	 * @param acadFinTranEle
	 * @return
	 * @throws Exception
	 */
	public YFCElement manageFinTranForCreditMemo(YFSEnvironment env, Document inDoc, YFCElement acadFinTranEle, Document acadFinTransCommonCodeDoc)
			throws Exception
	{
		createInputForCreditMemoRecord(env, inDoc, acadFinTranEle, acadFinTransCommonCodeDoc);
		return acadFinTranEle;
	}

	/**
	 * Creates extended DB API input for ACAD_FIN_TRANSACT table.
	 * 
	 * @param env
	 * @param inDoc
	 * @param apiIndoc
	 * @throws YFSException
	 */
	private void createInputForSalesInvoiceRecord(YFSEnvironment env, Document inDoc, YFCElement apiIndoc, Document acadFinTransCommonCodeDoc) throws Exception
	{
		log.beginTimer("createInputForSalesInvoiceRecord");
		log.verbose("Input to createInputForSalesInvoiceRecord - " + XMLUtil.getXMLString(inDoc));
		NodeList orderLineList = XMLUtil.getNodeList(inDoc, "/InvoiceDetail/InvoiceHeader/LineDetails/LineDetail");

		String[] shippingShortSku = AcademyChangeToVertexCallRequest.getShipmentShortSkuAndTaxCode(env, XMLUtil.getString(inDoc,
				"/InvoiceDetail/InvoiceHeader/Shipment/@ScacAndService"));
		log.verbose("Shipping Short SKU:" + shippingShortSku);

		/*
		 * There would be two records in Fin Transaction table for each order line. One record for merchandise and one record for shipment
		 */
		boolean boolIsBopis = false;
		int lineCount = 0;
		int totalNoOfOrderLines = orderLineList.getLength();

		//Code changes for PLCC
		double dRewardPoints = 0.0;

		for (int i = 0; i < totalNoOfOrderLines; i++)
		{
			Element lineDetail = (Element) orderLineList.item(i);
			AcademyFinTranDataBean acadBean = new AcademyFinTranDataBean();
			acadBean.sourceLine = String.valueOf(lineCount += 2);
			acadBean.referenceLine = String.valueOf(i + 1);
			acadBean.shippingShortSku = shippingShortSku[0];
			acadBean.itemID = XMLUtil.getString(lineDetail, "OrderLine/Item/@ItemID");
			acadBean.itemDescription = XMLUtil.getString(lineDetail, "OrderLine/Item/@ItemShortDesc");
			acadBean.qty = XMLUtil.getString(lineDetail, "@ShippedQty");
			
			/* OMNI-48389 - Shipment Type in Cognos Fin Trans Report needs to reflect correct value -START */
			//Fetching Scac and carrier service from shipment level for 'SHP' orders to get the updated values after upgrade/downgrade 
			String strDeliveryMethod = XMLUtil.getString(lineDetail, "OrderLine/@DeliveryMethod");
			if (YFCUtils.equals(strDeliveryMethod, AcademyConstants.STR_PICK_DELIVERY_METHOD)){
				log.verbose("Delivery Method is PICK :: Setting SCAC and carrier sevice");
				acadBean.scac = strDeliveryMethod;
				acadBean.carrierService = XMLUtil.getString(lineDetail, "OrderLine/@ScacAndService");
				boolIsBopis = true;
			}
			else if (YFCUtils.equals(strDeliveryMethod, AcademyConstants.STR_SHIP_DELIVERY_METHOD)){
				log.verbose("Delivery Method is SHP :: Setting SCAC and carrier sevice");
				acadBean.scac = XMLUtil.getString(inDoc.getDocumentElement(), "//InvoiceDetail/InvoiceHeader/Shipment/@SCAC");
				acadBean.carrierService = XMLUtil.getString(inDoc.getDocumentElement(), "//InvoiceDetail/InvoiceHeader/Shipment/@ScacAndService");
			}
			else {
				log.verbose("Delivery Method is not PICK or SHP :: Setting SCAC and carrier sevice");
				acadBean.scac = XMLUtil.getString(lineDetail, "OrderLine/@SCAC");
				acadBean.carrierService = XMLUtil.getString(lineDetail, "OrderLine/@ScacAndService");
			}
			/* OMNI-48389 - Shipment Type in Cognos Fin Trans Report needs to reflect correct value -END */
			checkForGiftCardItem(env, lineDetail, acadBean);

			AcademyRecordFinTranUtil.getLineDetailsForDBRrecord(lineDetail, acadBean, "0001");
			createInputForSalesLineInvoiceRecord(apiIndoc, inDoc, acadBean, acadFinTransCommonCodeDoc);

			// If order has TaxWriteOff, create a transaction record
			if (Double.parseDouble(acadBean.merchandiseTaxWriteOff) > 0)
			{
				log.verbose("Creating Transaction record for TaxWriteOff: " + acadBean.merchandiseTaxWriteOff);
				createInputForTaxWriteOff(env, inDoc, apiIndoc, acadBean, acadFinTransCommonCodeDoc);
			}

			//Code changes for PLCC
			if (Double.parseDouble(acadBean.plccRewardPoints) > 0) {
				log.verbose("Creating Transaction record for plccRewardPoints: " + acadBean.plccRewardPoints);
				dRewardPoints = dRewardPoints + Double.parseDouble(acadBean.plccRewardPoints);
				log.verbose(" Updated Reward Points are : " + dRewardPoints);
			}
		}

		/*
		 * There would be one record for each payment tender used in the order.
		 */


		//Code Changes for PLCC : update reward points
		if(dRewardPoints > 0) {
			addPLCCPaymentCollectionInfo(inDoc, dRewardPoints);
		}

		if ("APPEASEMENT".equalsIgnoreCase(XMLUtil.getString(inDoc, "/InvoiceDetail/InvoiceHeader/Order/@OrderType")))
		{
			log.verbose("order type : Appeasement");
			YFCElement inputForAppeasementEle = AcademyRecordFinTranUtil.getFinTranApiInput(apiIndoc);
			setCommonAttributes(inputForAppeasementEle, inDoc);
			setAttributesForAppeasementOrder(inputForAppeasementEle, inDoc, acadFinTransCommonCodeDoc);
		}
		else
		{
			NodeList collectionDetailList = XMLUtil.getNodeList(inDoc, "/InvoiceDetail/InvoiceHeader/CollectionDetails/CollectionDetail["
					+ "(@ChargeType='CHARGE' or @ChargeType='TRANSFER_IN') and @Status='CHECKED']");
			int collectionDetailsLength = collectionDetailList.getLength();
			for (int collectionCount = 0; collectionCount < collectionDetailsLength; collectionCount++)
			{
				Element collectionDetail = (Element) collectionDetailList.item(collectionCount);
				YFCElement inputForLineTotalEle = AcademyRecordFinTranUtil.getFinTranApiInput(apiIndoc);
				setCommonAttributes(inputForLineTotalEle, inDoc);
				if ("CREDIT_CARD".equalsIgnoreCase(XMLUtil.getString(collectionDetail, "PaymentMethod/@PaymentType"))
						|| "PayPal".equalsIgnoreCase(XMLUtil.getString(collectionDetail, "PaymentMethod/@PaymentType"))
						|| (AcademyConstants.KLARNA_PAY_TYP).equalsIgnoreCase(XMLUtil.getString(collectionDetail, "PaymentMethod/@PaymentType"))) // OMNI-98917
				{
					setAttributesForPaymentTenders(inputForLineTotalEle, collectionDetail, AcademyRecordFinTranUtil.getAcademyFinTranCodeForXPath(
							acadFinTransCommonCodeDoc, AcademyConstants.SHIPMENT_TENDER_CC));
				}
				else if ("GIFT_CARD".equalsIgnoreCase(XMLUtil.getString(collectionDetail, "PaymentMethod/@PaymentType")))
				{
					//START : GCD-157 and GCD-162
					String strPaymentReference2 = XMLUtil.getString(collectionDetail,"PaymentMethod/@PaymentReference2");
					log.verbose("strPaymentReference2 :" + strPaymentReference2);
					
					if(AcademyConstants.STR_GC_POST_CUTOVER.equals(strPaymentReference2))
						setAttributesForPaymentTenders(inputForLineTotalEle, collectionDetail, AcademyRecordFinTranUtil.getAcademyFinTranCodeForXPath(
								acadFinTransCommonCodeDoc, AcademyConstants.SHIPMENT_TENDER_GC));
					else	
						setAttributesForPaymentTenders(inputForLineTotalEle, collectionDetail, AcademyRecordFinTranUtil.getAcademyFinTranCodeForXPath(
								acadFinTransCommonCodeDoc, AcademyConstants.ORDER_ENTRY_GC_REDEMPTION));
					//END : GCD-157 and GCD-162
				}
				else if ("TRANSFER_IN".equalsIgnoreCase(XMLUtil.getString(collectionDetail, "@ChargeType")))
				{
					log.verbose("charge type transfer_in");
					String finTranCode = AcademyRecordFinTranUtil.getAcademyFinTranCodeForXPath(acadFinTransCommonCodeDoc,
							AcademyConstants.SHIPMENT_TENDER_NO_REVENUE);
					log.verbose("setting attributes : " + XMLUtil.getElementXMLString(collectionDetail));
					setAttributesForTransferInPayment(env, inDoc, inputForLineTotalEle, collectionDetail, finTranCode, acadFinTransCommonCodeDoc);
				}
				else if("PLCC".equalsIgnoreCase(XMLUtil.getString(collectionDetail, "PaymentMethod/@PaymentType"))) {
					log.verbose(" PLCC Payment Type");
					
					setAttributesForPaymentTenders(inputForLineTotalEle, collectionDetail, AcademyRecordFinTranUtil.getAcademyFinTranCodeForXPath(
							acadFinTransCommonCodeDoc, AcademyConstants.SHIPMENT_TENDER_CC));
				}
				else if(AcademyConstants.STR_PLCC_REWARDS_PAYMENT
						.equalsIgnoreCase(XMLUtil.getString(collectionDetail, "PaymentMethod/@PaymentType"))) {
					log.verbose(" PLCC Rewards Payment Type");
					
					setAttributesForPaymentTenders(inputForLineTotalEle, collectionDetail, AcademyRecordFinTranUtil.getAcademyFinTranCodeForXPath(
							acadFinTransCommonCodeDoc, AcademyConstants.SHIPMENT_TENDER_CC));
				}
				
				if (boolIsBopis){
					inputForLineTotalEle.setAttribute("AcademyFinTranShipMethod", AcademyConstants.STR_PICK_DELIVERY_METHOD);
				}
			}
		}
		log.endTimer("createInputForSalesInvoiceRecord");
	}

	/**
	 * Method to compare Refund order purpose. It could be because of Add/Modify charges, line Cancellation, Tax reduction or Return refund.
	 * 
	 * @param env
	 * @param inDoc
	 * @param collectionDetail
	 * @return
	 * @throws Exception
	 */
	private String getTranCodeForRefundReason(YFSEnvironment env, Document inDoc, Element collectionDetail) throws Exception
	{
		log.verbose("Inside getTranCodeForRefundReason with Collection Details : " + XMLUtil.getElementXMLString(collectionDetail));
		String orderHeaderKey = XMLUtil.getString(collectionDetail, "@TransferFromOhKey");
		Document orderAuditListInDoc = YFCDocument.getDocumentFor("<OrderAudit OrderHeaderKey='" + orderHeaderKey + "'/>").getDocument();
		env.setApiTemplate("getOrderAuditList", "global/template/api/getOrderAuditList.xml");
		Document orderAuditListOutDoc = AcademyUtil.invokeAPI(env, "getOrderAuditList", orderAuditListInDoc);
		env.clearApiTemplate("getOrderAuditList");
		String amountTransferIN = XMLUtil.getString(collectionDetail, "@AmountCollected");
		// Hint: In case of tax difference between PROFORMA and INVOICE, refundDueTo value is OTHER which is missing here.
		// Use this hint to fix defect in future.
		NodeList orderAuditList = XPathUtil.getNodeList(orderAuditListOutDoc,
				"/OrderAuditList/OrderAudit/OrderAuditLevels/OrderAuditLevel[@ModificationLevel='ORDER_LINE' and "
						+ "(ModificationTypes/ModificationType/@Name='CANCEL' or "
						+ "ModificationTypes/ModificationType/@Name='PRICE' or ModificationTypes/ModificationType/@Name='TAX')]");
		int orderAuditListLenght = orderAuditList.getLength();

		for (int i = 0; i < orderAuditListLenght; i++)
		{
			Element auditElement = (Element) orderAuditList.item(i);
			Element modificationType = (Element) XPathUtil.getNode(auditElement, "ModificationTypes/ModificationType");
			log.verbose("ModificationType : " + XMLUtil.getElementXMLString(modificationType));
			Element attributeEle = (Element) XPathUtil.getNode(auditElement, "OrderAuditDetails/OrderAuditDetail/Attributes/Attribute[@Name='LineTotal']");
			if (!YFCObject.isVoid(attributeEle))
			{
				log.verbose("Change : " + XMLUtil.getElementXMLString(attributeEle));
				double modificationDiffAmount = Double.parseDouble(attributeEle.getAttribute("OldValue"))
						- Double.parseDouble(attributeEle.getAttribute("NewValue"));
				if (Double.parseDouble(amountTransferIN) == modificationDiffAmount)
				{
					log.verbose("Returning Modification : " + modificationType.getAttribute("Name"));
					return modificationType.getAttribute("Name");
				}
			}
		}
		return "";
	}

	/**
	 * Check if line item is a gift card item.
	 * 
	 * @param env
	 * @param lineDetail
	 * @param acadBean
	 * @throws Exception
	 */
	private void checkForGiftCardItem(YFSEnvironment env, Element lineDetail, AcademyFinTranDataBean acadBean) throws Exception
	{
		log.beginTimer("checkForGiftCardItem");

		Element itemDetailInputEle = YFCDocument.createDocument("Item").getDocument().getDocumentElement();
		itemDetailInputEle.setAttribute("ItemID", XMLUtil.getString(lineDetail, "OrderLine/Item/@ItemID"));
		Document itemDetailTemp = YFCDocument.getDocumentFor("<ItemList> <Item ItemID=''><Extn ExtnIsGiftCard=''/></Item> </ItemList>").getDocument();
		env.setApiTemplate(AcademyConstants.API_GET_ITEM_LIST, itemDetailTemp);
		Document itemListDocument = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ITEM_LIST, itemDetailInputEle.getOwnerDocument());
		env.clearApiTemplate(AcademyConstants.API_GET_ITEM_LIST);

		Element itemListElement = itemListDocument.getDocumentElement();
		log.verbose("Item List - " + XMLUtil.getElementXMLString(itemListElement));

		Element itemExtnElement = (Element)XPathUtil.getNode(itemListElement, "/ItemList/Item/Extn");
		if(!YFCObject.isVoid(itemExtnElement))
		{
			if ("Y".equalsIgnoreCase(itemExtnElement.getAttribute("ExtnIsGiftCard")))
			{
				acadBean.isGiftCard = true;
			}
		}

		log.endTimer("checkForGiftCardItem");
	}

	/**
	 * This method creates input for multi API for each order line. It creates two records for each line
	 * 
	 * @param apiIndoc
	 * @param inDoc
	 * @param acadBean
	 * @throws Exception
	 */
	private void createInputForSalesLineInvoiceRecord(YFCElement apiIndoc, Document inDoc, AcademyFinTranDataBean acadBean, Document acadFinTransCommonCodeDoc)
			throws Exception
	{

		log.beginTimer("createInputForSalesLineInvoiceRecord");

		YFCElement inputForMerchandiseLineEle = AcademyRecordFinTranUtil.getFinTranApiInput(apiIndoc);
		setCommonAttributes(inputForMerchandiseLineEle, inDoc);
		setAttributesForSalesOrderMerchandiseLine(inputForMerchandiseLineEle, acadBean, inDoc, acadFinTransCommonCodeDoc);
		if (log.isVerboseEnabled())
		{
			log.verbose("SO Merchandise Line: " + inputForMerchandiseLineEle.getString());
		}

		if ("REFUND".equalsIgnoreCase(XMLUtil.getString(inDoc, "/InvoiceDetail/InvoiceHeader/Order/@OrderPurpose")))
		{
			// Do nothing since no shipping charged to customer.
		}
		else if ("APPEASEMENT".equalsIgnoreCase(XMLUtil.getString(inDoc, "/InvoiceDetail/InvoiceHeader/Order/@OrderType")))
		{
			// FIX for defect #3296 - For line with FIN_TRAN_CODE equal to 3:
			// a. APPEASEMENT_AMOUNT should be zero
			// b. EXTENDED_AMOUNT should be appeasement amount
			inputForMerchandiseLineEle.setAttribute("AcademyFinTranExtendedAmount", acadBean.merchandiseExtendedAmt);
			inputForMerchandiseLineEle.setAttribute("AcademyFinTranAppesementAmount", "0");
		}
		else
		{
			YFCElement inputForShippingLineEle = AcademyRecordFinTranUtil.getFinTranApiInput(apiIndoc);
			setCommonAttributes(inputForShippingLineEle, inDoc);
			setAttributesForSalesOrderShippingLine(inputForShippingLineEle, acadBean, inDoc, acadFinTransCommonCodeDoc);
			if (log.isVerboseEnabled())
			{
				log.verbose("SO Shipping Line: " + inputForShippingLineEle.getString());
			}
		}
		log.endTimer("createInputForSalesLineInvoiceRecord");
	}

	/**
	 * 
	 * @param acadFinTranEle
	 * @param acadBean
	 * @param inDoc
	 * @throws Exception
	 */
	private void setAttributesForSalesOrderMerchandiseLine(YFCElement acadFinTranEle, AcademyFinTranDataBean acadBean, Document inDoc,
			Document acadFinTransCommonCodeDoc) throws Exception
	{

		String tranCode = AcademyRecordFinTranUtil.getAcademyFinTranCodeForXPath(acadFinTransCommonCodeDoc, AcademyConstants.SO_MERCHANDISE_OR_SHIPPING);
		String sourceLineNo = String.valueOf((Integer.parseInt(acadBean.sourceLine)) - 1);
		boolean isDeliveryFeeApplicable = false;

		if (acadBean.isGiftCard)
		{
			acadFinTranEle.setAttribute("AcademyFinTranGiftCardIssuedAmount", acadBean.merchandiseExtendedAmt);
			tranCode = AcademyRecordFinTranUtil.getAcademyFinTranCodeForXPath(acadFinTransCommonCodeDoc, AcademyConstants.GC_SHIPMENT_ISSUANCE);
			isDeliveryFeeApplicable = true;
		}
		else
		{
			acadFinTranEle.setAttribute("AcademyFinTranMerchandiseAmount", acadBean.merchandiseExtendedAmt);
			isDeliveryFeeApplicable = true;
		}

		if ("REFUND".equalsIgnoreCase(XMLUtil.getString(inDoc, "/InvoiceDetail/InvoiceHeader/Order/@OrderPurpose")))
		{
			tranCode = AcademyRecordFinTranUtil.getAcademyFinTranCodeForXPath(acadFinTransCommonCodeDoc, AcademyConstants.GC_SHIPMENT_REFUND);
			sourceLineNo = "1";
			isDeliveryFeeApplicable = false;
		}
		else if ("APPEASEMENT".equalsIgnoreCase(XMLUtil.getString(inDoc, "/InvoiceDetail/InvoiceHeader/Order/@OrderType")))
		{
			tranCode = AcademyRecordFinTranUtil.getAcademyFinTranCodeForXPath(acadFinTransCommonCodeDoc, AcademyConstants.GC_SHIPMENT_FOR_APPEASEMENT);
			sourceLineNo = "1";
			isDeliveryFeeApplicable = false;
		}

		log.verbose("setAttributesForSalesOrderMerchandiseLine bean values - " + acadBean.toString());

		acadFinTranEle.setAttribute("AcademyFinTranCode", tranCode);
		acadFinTranEle.setAttribute("AcademyFinTranSourceLine", sourceLineNo);
		acadFinTranEle.setAttribute("AcademyFinTranReferenceLine", acadBean.referenceLine);

		acadFinTranEle.setAttribute("AcademyFinTranExtendedAmount", acadBean.getMerchandiseTotal());

		acadFinTranEle.setAttribute("AcademyFinTranDiscountAmount", AcademyFinTranDataBean.getNegativeValue(acadBean.merchandiseDiscount));
		acadFinTranEle.setAttribute("AcademyFinTranDiscountId", acadBean.merchandiseDiscountReason);
		acadFinTranEle.setAttribute("AcademyFinTranAppesementAmount", AcademyFinTranDataBean.getNegativeValue(acadBean.merchandiseAppeasement));
		acadFinTranEle.setAttribute("AcademyFinTranAppsementCode", acadBean.merchandiseAppeasementReason);
		acadFinTranEle.setAttribute("AcademyFinTranStoreCouponAmount", AcademyFinTranDataBean.getNegativeValue(acadBean.merchandiseStoreCoupon));
		acadFinTranEle.setAttribute("AcademyFinTranCouponCode", acadBean.merchandiseCouponCode);
		acadFinTranEle.setAttribute("AcademyFinTranMfgCouponAmount", acadBean.merchandiseMfgCoupon);
		acadFinTranEle.setAttribute("AcademyFinTranTaxAmount", acadBean.merchandiseTax);
		/* OMNI-75060, OMNI-77406 - Add Header Tax to first Taxable Shipment Line's 
		 * Tax Amount and Extended Amount if Header Tax > 0 and line is Merchandise Sales Line - Start */
		String strHeaderTax = XMLUtil.getString(inDoc, "/InvoiceDetail/InvoiceHeader/@HeaderTax");
		if (!YFCCommon.isVoid(strHeaderTax)) {
			log.verbose("strHeaderTax -- " + strHeaderTax);
			Double dHeaderTax = Double.parseDouble(strHeaderTax);
			log.verbose("HeaderTax -- " + dHeaderTax);
			log.verbose("isHeaderTaxConsumed set as -- " + isHeaderTaxConsumed);
			if (dHeaderTax > 0.00 && isDeliveryFeeApplicable && !isHeaderTaxConsumed
					&& Double.parseDouble(acadBean.merchandiseTax) > 0.00
					&& Double.parseDouble(acadBean.merchandiseAmount) > 0.00) {
				log.verbose("Before HeaderTax - - AcademyFinTranTaxAmount - - "
						+ acadFinTranEle.getAttribute("AcademyFinTranTaxAmount")
						+ " - - AcademyFinTranExtendedAmount - - " + acadBean.getMerchandiseTotal());
				
				Double totalTax = dHeaderTax + Double.parseDouble(acadBean.merchandiseTax);
				Double totalExtendedAmount = dHeaderTax + Double.parseDouble(acadBean.getMerchandiseTotal());
				acadFinTranEle.setAttribute("AcademyFinTranTaxAmount", totalTax);
				acadFinTranEle.setAttribute("AcademyFinTranExtendedAmount", totalExtendedAmount);
				isHeaderTaxConsumed = true;
				log.verbose("isHeaderTaxConsumed set as -- " + isHeaderTaxConsumed);
				log.verbose("After HeaderTax - - AcademyFinTranTaxAmount -- "
						+ acadFinTranEle.getAttribute("AcademyFinTranTaxAmount")
						+ " - - AcademyFinTranExtendedAmount - - " + totalExtendedAmount);
			}
		}
		/* OMNI-75060, OMNI-77406 - End */
		acadFinTranEle.setAttribute("AcademyFinTranUnitPrice", acadBean.merchandiseUnitPrice);
		acadFinTranEle.setAttribute("AcademyFinTranQty", acadBean.qty);
		acadFinTranEle.setAttribute("AcademyFinTranShortSKU", acadBean.itemID);
		acadFinTranEle.setAttribute("AcademyFinTranItemDesc", acadBean.itemDescription);
		acadFinTranEle.setAttribute("AcademyFinTranShipMethod", XMLUtil.getString(inDoc, "/InvoiceDetail/InvoiceHeader/Order/Shipment/@SCAC"));
		
		if ("PICK".equalsIgnoreCase(XMLUtil.getString(inDoc, "/InvoiceDetail/InvoiceHeader/Shipment/@DeliveryMethod"))){
			acadFinTranEle.setAttribute("AcademyFinTranShipMethod", AcademyConstants.STR_PICK_DELIVERY_METHOD);
			
		}
	}

	/**
	 * 
	 * @param acadFinTranEle
	 * @param acadBean
	 * @param inDoc
	 * @throws Exception
	 */
	private void setAttributesForSalesOrderShippingLine(YFCElement acadFinTranEle, AcademyFinTranDataBean acadBean, Document inDoc,
			Document acadFinTransCommonCodeDoc) throws Exception
	{

		String tranCode = AcademyRecordFinTranUtil.getAcademyFinTranCodeForXPath(acadFinTransCommonCodeDoc, AcademyConstants.SO_MERCHANDISE_OR_SHIPPING);
		if (acadBean.isGiftCard)
		{
			tranCode = AcademyRecordFinTranUtil.getAcademyFinTranCodeForXPath(acadFinTransCommonCodeDoc, AcademyConstants.GC_SHIPMENT_ISSUANCE);
		}
		acadFinTranEle.setAttribute("AcademyFinTranCode", tranCode);
		acadFinTranEle.setAttribute("AcademyFinTranSourceLine", acadBean.sourceLine);
		acadFinTranEle.setAttribute("AcademyFinTranReferenceLine", acadBean.referenceLine);
		acadFinTranEle.setAttribute("AcademyFinTranExtendedAmount", acadBean.getShipmentTotal());
		acadFinTranEle.setAttribute("AcademyFinTranShipChargeAmount", acadBean.shippingCharge);
		acadFinTranEle.setAttribute("AcademyFinTranAppsementCode", acadBean.shippingAppeasementReason);
		acadFinTranEle.setAttribute("AcademyFinTranDiscountAmount", AcademyFinTranDataBean.getNegativeValue(acadBean.getTotalShippingDiscount(true)));
		acadFinTranEle.setAttribute("AcademyFinTranDiscountId", acadBean.shippingDiscountReason);
		acadFinTranEle.setAttribute("AcademyFinTranTaxAmount", acadBean.shippingTax);
		acadFinTranEle.setAttribute("AcademyFinTranUnitPrice", acadBean.shippingCharge);
		acadFinTranEle.setAttribute("AcademyFinTranQty", "1");
		acadFinTranEle.setAttribute("AcademyFinTranShipLineFlag", "Y");
		acadFinTranEle.setAttribute("AcademyFinTranShortSKU", acadBean.shippingShortSku);
		acadFinTranEle.setAttribute("AcademyFinTranShipMethod", acadBean.scac);
		acadFinTranEle.setAttribute("AcademyFinTranShipType", acadBean.carrierService);
	}

	private void setAttributesForTransferInPayment(YFSEnvironment env, Document inDoc, YFCElement acadFinTranEle, Element collectionDetail, String finTranCode,
			Document acadFinTransCommonCodeDoc) throws Exception
	{
		String refundDueTo = getTranCodeForRefundReason(env, inDoc, collectionDetail);
		log.verbose("Refund due to : " + refundDueTo);
		log.verbose("Refund due to : " + refundDueTo);
		if ("TAX".equalsIgnoreCase(refundDueTo))
		{
			finTranCode = AcademyRecordFinTranUtil.getAcademyFinTranCodeForXPath(acadFinTransCommonCodeDoc, AcademyConstants.SHIPMENT_TENDER_GC);
			log.verbose("fin trans code : " + finTranCode);
		}
		setAttributesForPaymentTenders(acadFinTranEle, collectionDetail, finTranCode);
		// Hint: In case of tax difference between PROFORMA and INVOICE, refundDueTo value is OTHER
		// Use this hint to fix defect in future.
		if ("TAX".equalsIgnoreCase(refundDueTo) || "CANCEL".equalsIgnoreCase(refundDueTo) || "PRICE".equalsIgnoreCase(refundDueTo))
		{
			acadFinTranEle.setAttribute("AcademyFinTranDefferedCustCredit", "0.00");
			acadFinTranEle.setAttribute("AcademyFinTranDeferredRevenue", XMLUtil.getString(collectionDetail, "@AmountCollected"));
			log.verbose("deferred rev value : " + acadFinTranEle.getAttribute("AcademyFinTranDeferredRevenue"));
		}
	}

	/**
	 * 
	 * @param acadFinTranEle
	 * @param collectionDetail
	 * @param tranCode
	 * @throws Exception
	 */
	private void setAttributesForPaymentTenders(YFCElement acadFinTranEle, Element collectionDetail, String tranCode) throws Exception
	{
		log.verbose("Inside payment tender set attribute : " + XMLUtil.getElementXMLString(collectionDetail));
		log.verbose("Inside payment tender set attribute : " + XMLUtil.getElementXMLString(collectionDetail));
		acadFinTranEle.setAttribute("AcademyFinTranCode", tranCode);
		acadFinTranEle.setAttribute("AcademyFinTranSourceLine", "00");
		acadFinTranEle.setAttribute("AcademyFinTranReferenceLine", "00");
		acadFinTranEle.setAttribute("AcademyFinTranExtendedAmount", collectionDetail.getAttribute("AmountCollected"));

		if ("TRANSFER_OUT".equalsIgnoreCase(XMLUtil.getString(collectionDetail, "@ChargeType")))
		{
			log.verbose("Inside Transfer out");
			acadFinTranEle.setAttribute("AcademyFinTranTenderType", "GIFT_CARD");
			acadFinTranEle.setAttribute("AcademyFinTranDefferedCustCredit", XMLUtil.getString(collectionDetail, "@AmountCollected"));
			log.verbose("transout amount collected : " + acadFinTranEle.getAttribute("AcademyFinTranDefferedCustCredit"));
		}
		else if ("GIFT_CARD".equalsIgnoreCase(XMLUtil.getString(collectionDetail, "PaymentMethod/@PaymentType")))
		{
			log.verbose("Inside gift card");
			acadFinTranEle.setAttribute("AcademyFinTranTenderType", "GIFT_CARD");
			//START : GCD-125 commenting as the collected amount will be sent in AcademyFinTranGCRedemptionAmount going forward.
			//Reason : With new implementation Orders placed using Gift card as payment method will no longer be deferred sale.
//			acadFinTranEle.setAttribute("AcademyFinTranDeferredRevenue", collectionDetail.getAttribute("AmountCollected"));
			//START : GCD-157 and GCD-162
			if(AcademyConstants.STR_ORDER_ENTRY_GC_REDEMPTION.equals(tranCode))
				acadFinTranEle.setAttribute("AcademyFinTranDeferredRevenue", collectionDetail.getAttribute("AmountCollected"));
			else
				acadFinTranEle.setAttribute("AcademyFinTranGCRedemptionAmount", collectionDetail.getAttribute("AmountCollected"));
			//END : GCD-125
			//END : GCD-157 and GCD-162
		}
		else if ("CREDIT_CARD".equalsIgnoreCase(XMLUtil.getString(collectionDetail, "PaymentMethod/@PaymentType"))||
				"PayPal".equalsIgnoreCase(XMLUtil.getString(collectionDetail, "PaymentMethod/@PaymentType")))
		{
			log.verbose("Inside credit card");
			acadFinTranEle.setAttribute("AcademyFinTranTenderType", "CREDIT_CARD");
			acadFinTranEle.setAttribute("AcademyFinTranCCChargeAmount", XMLUtil.getString(collectionDetail, "@AmountCollected"));
			//START :: Changes done as part of Host Capture
			if (AcademyConstants.PAYPAL.equalsIgnoreCase(XMLUtil.getString(collectionDetail, "PaymentMethod/@PaymentType")))
			{
				acadFinTranEle.setAttribute("AcademyFinTranCCType", AcademyConstants.PAYPAL);
			}
			else
			{
				acadFinTranEle.setAttribute("AcademyFinTranCCType", XMLUtil.getString(collectionDetail, "PaymentMethod/@CreditCardType"));
			}
			//END :: Changes done as part of Host Capture
		}
		else if ("TRANSFER_IN".equalsIgnoreCase(XMLUtil.getString(collectionDetail, "@ChargeType")))
		{
			log.verbose("Inside Transfer In");

			// FIX FOR DEFECT #3038 - DEFERRED_REVENUE should be populated with gift card offset, not DEFERRED_CUST_CREDIT.
			// acadFinTranEle.setAttribute("AcademyFinTranDefferedCustCredit", XMLUtil.getString(collectionDetail, "@AmountCollected"));
			acadFinTranEle.setAttribute("AcademyFinTranDeferredRevenue", XMLUtil.getString(collectionDetail, "@AmountCollected"));

			acadFinTranEle.setAttribute("AcademyFinTranTenderType", "GIFT_CARD");
			log.verbose("transin amount collected : " + acadFinTranEle.getAttribute("AcademyFinTranDefferedCustCredit"));
		}
		else if("PLCC".equalsIgnoreCase(XMLUtil.getString(collectionDetail, "PaymentMethod/@PaymentType"))) {
			log.verbose("Inside PLCC Credit Cards");
			acadFinTranEle.setAttribute("AcademyFinTranTenderType", "PLCC");
			acadFinTranEle.setAttribute("AcademyFinTranCCChargeAmount", XMLUtil.getString(collectionDetail, "@AmountCollected"));
			acadFinTranEle.setAttribute("AcademyFinTranCCType", XMLUtil.getString(collectionDetail, "PaymentMethod/@CreditCardType"));

		}
		else if(AcademyConstants.STR_PLCC_REWARDS_PAYMENT
				.equalsIgnoreCase(XMLUtil.getString(collectionDetail, "PaymentMethod/@PaymentType"))) {
			log.verbose("Inside PLCCRewards ");
			acadFinTranEle.setAttribute("AcademyFinTranTenderType", AcademyConstants.STR_PLCC_REWARDS_PAYMENT);
			acadFinTranEle.setAttribute("AcademyFinTranCCChargeAmount", XMLUtil.getString(collectionDetail, "@AmountCollected"));
		}else if (AcademyConstants.KLARNA_PAY_TYP.equalsIgnoreCase(XMLUtil.getString(collectionDetail, "PaymentMethod/@PaymentType"))) { 
			//OMNI-98917 Start 'Klarna' payment type
			log.verbose("Inside Klarna Payment Type");
			acadFinTranEle.setAttribute("AcademyFinTranTenderType", AcademyConstants.KLARNA_PAY_TYP);
			acadFinTranEle.setAttribute("AcademyFinTranCCChargeAmount",
					XMLUtil.getString(collectionDetail, "@AmountCollected"));
			acadFinTranEle.setAttribute("AcademyFinTranCCType", AcademyConstants.KLARNA_PAY_TYP);
		} //OMNI-98917 End


	}

	/**
	 * 
	 * @param acadFinTranEle
	 * @param inDoc
	 */
	private void setCommonAttributes(YFCElement acadFinTranEle, Document inDoc)
	{
		log.beginTimer("setCommonAttributes");
		try
		{
			log.verbose("Input Document:" + XMLUtil.getXMLString(inDoc));
			log.verbose("Input Doc : - " + XMLUtil.getXMLString(inDoc));
			String invoiceNo = XMLUtil.getString(inDoc, "/InvoiceDetail/InvoiceHeader/@InvoiceNo");
			
			/**
			Code Change: Starts
			Project: NeXus Integration - Enhancements.
			Code change is made to set 'AcademyFinTranDateTime' and 'AcademyFinTranDate' attribute as TransactionDate..depending on the boolean value.
			*/

			String strTranDate = XMLUtil.getString(inDoc, "/InvoiceDetail/InvoiceHeader/@TransactionDate");
			log.verbose("Transaction Date:" + strTranDate);

			/**
			Code Change: Ends
			*/
			
			log.verbose("Invoice No:" + invoiceNo);

			String originalOrderNo = XMLUtil.getString(inDoc, "/InvoiceDetail/InvoiceHeader/Order/@OrderNo");
			String referenceOrderNo = "";
			String operatorNo = XMLUtil.getString(inDoc, "/InvoiceDetail/InvoiceHeader/@Operator");

			/*
			 * For Sales Order, BillToID has Customer ID and for Return Order, fetch related Sales and get Customer ID
			 * 
			 */
			String custId = XMLUtil.getString(inDoc, "/InvoiceDetail/InvoiceHeader/Order/@BillToID");

			String terminalNoStr = XMLUtil.getString(inDoc, "/InvoiceDetail/InvoiceHeader/@TerminalNo");
			String tranNoStr = XMLUtil.getString(inDoc, "/InvoiceDetail/InvoiceHeader/@TransactionNo");
			
			/**
			Code Change: Starts
			Project: NeXus Integration - Enhancements.
			Code change is made to set 'AcademyFinTranDateTime' and 'AcademyFinTranDate' attribute as TransactionDate..depending on the boolean value.
			*/
			
			acadFinTranEle.setAttribute("AcademyFinTranDateTime", AcademyRecordFinTranUtil.getDateTimeFromTransactionDate(strTranDate, true));
			acadFinTranEle.setAttribute("AcademyFinTranDate", AcademyRecordFinTranUtil.getDateTimeFromTransactionDate(strTranDate, false));
			
			/**
			Code Change: Ends
			*/
			
			acadFinTranEle.setAttribute("AcademyFinTranSourceKey", invoiceNo);

			// Stamp Order# for SO and Out bound Invoice# for RO
			String invoiceType = XMLUtil.getString(inDoc, "/InvoiceDetail/InvoiceHeader/@InvoiceType");

			log.verbose("Invoice Type : = " + invoiceType);
			log.verbose("Original order No = " + originalOrderNo);

			if ("SHIPMENT".equalsIgnoreCase(invoiceType))
			{
				if ("APPEASEMENT".equalsIgnoreCase(XMLUtil.getString(inDoc, "/InvoiceDetail/InvoiceHeader/Order/@OrderType")))
				{
					log.verbose("inside appeasement");
					Node parentNoNode = XPathUtil.getNode(inDoc, "/InvoiceDetail/InvoiceHeader/Order/References/Reference[@Name='AppeasementParentOrderNo']");
					if (!YFCObject.isVoid(parentNoNode))
					{
						referenceOrderNo = ((Element) parentNoNode).getAttribute("Value");
						log.verbose("reference Order No : APPEASEMENT - " + referenceOrderNo);
						log.verbose("reference Order No : APPEASEMENT - " + referenceOrderNo);
					}
				}
				else if ("REFUND".equalsIgnoreCase(XMLUtil.getString(inDoc, "/InvoiceDetail/InvoiceHeader/Order/@OrderPurpose")))
				{
					log.verbose("inside refund");
					Node parentNoNode = XPathUtil.getNode(inDoc, "/InvoiceDetail/InvoiceHeader/Order/References/Reference[@Name='RefundParentOrderNo']");
					if (!YFCObject.isVoid(parentNoNode))
					{
						referenceOrderNo = ((Element) parentNoNode).getAttribute("Value");
						log.verbose("reference Order No : REFUND - " + referenceOrderNo);
						log.verbose("reference Order No : REFUND - " + referenceOrderNo);
					}
				}
			}
			else if ("RETURN".equalsIgnoreCase(XMLUtil.getString(inDoc, "/InvoiceDetail/InvoiceHeader/@InvoiceType")))
			{
				/*
				 * Node relSalesInvoiceNo = XPathUtil.getNode(inDoc,
				 * "/InvoiceDetail/InvoiceHeader/Order/References/Reference[@Name='SalesOrderInvoiceNo']");
				 * if(!YFCObject.isVoid(relSalesInvoiceNo)) { referenceOrderNo = ((Element)relSalesInvoiceNo).getAttribute("Value");
				 * log.verbose("reference Order No : RETURN - " + referenceOrderNo); log.verbose("reference Order No : return - " +
				 * referenceOrderNo); }
				 */

				// FIX - As per new requirement, for all transactions, except for orders with gift card tender, the REFERENCE_KEY
				// should be populated with the value from ORIGINAL_ORDER_NO from the original purchase
				referenceOrderNo = XPathUtil.getString(inDoc, "/InvoiceDetail/InvoiceHeader/LineDetails/LineDetail/OrderLine/DerivedFromOrder/@OrderNo");
				log.verbose("reference Order No : RETURN - " + referenceOrderNo);
			}

			log.verbose("reference Order No : - " + referenceOrderNo);

			// FIX - As per new requirement, for all transactions, except for orders with gift card tender, the REFERENCE_KEY
			// should be populated with the value from ORIGINAL_ORDER_NO from the original purchase
			if (referenceOrderNo.equals(""))
			{
				referenceOrderNo = originalOrderNo;
			}

			acadFinTranEle.setAttribute("AcademyFinTranReferenceKey", referenceOrderNo);
			acadFinTranEle.setAttribute("AcademyFinTranOrderNo", originalOrderNo);
			acadFinTranEle.setAttribute("AcademyFinTranCustNo", custId);
			acadFinTranEle.setAttribute("AcademyFinTranEntityCode", "00005");
			acadFinTranEle.setAttribute("AcademyFinTranOperator", operatorNo);
			acadFinTranEle.setAttribute("AcademyFinTranTLogTerminal", terminalNoStr);
			acadFinTranEle.setAttribute("AcademyFinTranTLogTranId", tranNoStr);

			// FIX FOR DEFECT #3251 - set 2000-01-01 12:00:00 as the default DBR_PROCESSED_DATETIME
			acadFinTranEle.setAttribute("AcademyFinTranProcessTime", "2000-01-01T12:00:00");

			if (log.isVerboseEnabled())
			{
				log.verbose("%%%%%%%%%%%%%%%% setCommonAttributes - acad fin tran element - " + acadFinTranEle.toString());
			}
		}
		catch (Exception e)
		{
			log.error(e);
		}

		log.endTimer("setCommonAttributes");
	}

	/**
	 * 
	 * @param env
	 * @param inDoc
	 * @param apiIndoc
	 * @throws Exception
	 */
	private void createInputForReturnInvoiceRecord(YFSEnvironment env, Document inDoc, YFCElement apiIndoc, Document acadFinTransCommonCodeDoc)
			throws Exception
	{
		log.beginTimer("createInputForReturnInvoiceRecord");
		log.verbose("********** createInputForReturnInvoiceRecord - input doc - " + XMLUtil.getXMLString(inDoc));
		NodeList orderLineList = XMLUtil.getNodeList(inDoc, "/InvoiceDetail/InvoiceHeader/LineDetails/LineDetail");
		int lineCount = 0;
		String returnReasonCode = "";
        boolean IsBopis = false;
		String derivedOrderKey = XMLUtil.getString(inDoc, "/InvoiceDetail/InvoiceHeader/@DerivedFromOrderHeaderKey");
		//Code changes for PLCC
		double dRewardPoints = 0.0;
		
		if (!YFCObject.isVoid(derivedOrderKey))
		{
			Document getOrderInDoc = YFCDocument.getDocumentFor("<Order OrderHeaderKey='" + derivedOrderKey + "' />").getDocument();
			Document getOrderOutTemplate = YFCDocument.getDocumentFor("<OrderList> <Order BillToID='' /> </OrderList>").getDocument();
			env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, getOrderOutTemplate);
			Document orderListDocument = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, getOrderInDoc);
			env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
			Element orderListElement = orderListDocument.getDocumentElement();
			log.verbose(XMLUtil.getElementXMLString(orderListElement));
			
			Element orderElement = (Element)XPathUtil.getNode(orderListElement, "/OrderList/Order");
			String customerID = "";
			if(!YFCObject.isVoid(orderElement))
			{
				customerID = orderElement.getAttribute("BillToID");
			}
		
			if (!YFCObject.isVoid(customerID))
			{
				Element orderEle = (Element) XMLUtil.getNode(inDoc, "/InvoiceDetail/InvoiceHeader/Order");
				if (!YFCObject.isVoid(orderEle))
				{
					orderEle.setAttribute("BillToID", customerID);
				}
			}
		}

		log.verbose("******* number of orderlines - " + orderLineList.getLength());
		for (int i = 0; i < orderLineList.getLength(); i++)
		{
			Element lineDetail = (Element) orderLineList.item(i);
			log.verbose("******* orderline number - " + i);
			log.verbose("*************** Order line Element : " + XMLUtil.getElementXMLString(lineDetail));
			AcademyFinTranDataBean acadBean = new AcademyFinTranDataBean();
			acadBean.sourceLine = String.valueOf(lineCount += 2);

			String[] shippingShortSku = AcademyChangeToVertexCallRequest.getShipmentShortSkuAndTaxCode(env, XMLUtil.getString(lineDetail,
					"OrderLine/DerivedFromOrderLine/@ScacAndService"));

			acadBean.shippingShortSku = shippingShortSku[0];
			acadBean.scac = XMLUtil.getString(lineDetail, "OrderLine/DerivedFromOrderLine/@SCAC");
			acadBean.carrierService = XMLUtil.getString(lineDetail, "OrderLine/DerivedFromOrderLine/@ScacAndService");

			acadBean.itemID = XMLUtil.getString(lineDetail, "OrderLine/Item/@ItemID");
			acadBean.itemDescription = XMLUtil.getString(lineDetail, "OrderLine/Item/@ItemShortDesc");
			acadBean.qty = XMLUtil.getString(lineDetail, "@ShippedQty");
			acadBean.returnReasonCode = XMLUtil.getString(lineDetail, "OrderLine/@ReturnReason");
			returnReasonCode = acadBean.returnReasonCode;

			AcademyRecordFinTranUtil.getLineDetailsForDBRrecord(lineDetail, acadBean, "0003");
			createInputForReturnLineRecord(env, apiIndoc, inDoc, acadBean, acadFinTransCommonCodeDoc);
			String strDeliveryMethod = XMLUtil.getString(lineDetail, "OrderLine/DerivedFromOrderLine/@DeliveryMethod");
			if (YFCUtils.equals(strDeliveryMethod, AcademyConstants.STR_PICK_DELIVERY_METHOD)){
				acadBean.scac = strDeliveryMethod;
				IsBopis = true;
			}

			//Code changes for PLCC
			if (Double.parseDouble(acadBean.plccRewardPoints) != 0) {
				log.verbose("Creating Transaction record for plccRewardPoints: " + acadBean.plccRewardPoints);
				dRewardPoints = dRewardPoints + Double.parseDouble(acadBean.plccRewardPoints);
				log.verbose(" Updated Reward Points are : " + dRewardPoints);
			}
			
		}
		
		//Code Changes for PLCC : update reward points
		if(dRewardPoints != 0) {
			addPLCCPaymentCollectionInfo(inDoc, dRewardPoints);
		}

		NodeList collectionDetailList = XMLUtil.getNodeList(inDoc, "/InvoiceDetail/InvoiceHeader/CollectionDetails/CollectionDetail["
				+ "(@ChargeType='CHARGE' or @ChargeType='TRANSFER_OUT') and @Status='CHECKED']");

		log.verbose("number of collection details nodes - " + collectionDetailList.getLength());
		for (int collectionCount = 0; collectionCount < collectionDetailList.getLength(); collectionCount++)
		{
			Element collectionDetail = (Element) collectionDetailList.item(collectionCount);
			log.verbose("****** Collection Details - " + XMLUtil.getElementXMLString(collectionDetail));
			YFCElement inputForLineTotalEle = AcademyRecordFinTranUtil.getFinTranApiInput(apiIndoc);
			setCommonAttributes(inputForLineTotalEle, inDoc);
			if ("TRANSFER_OUT".equalsIgnoreCase(XMLUtil.getString(collectionDetail, "@ChargeType")))
			{
				log.verbose("charge type - TRANSFER_OUT");
				setAttributesForPaymentTenders(inputForLineTotalEle, collectionDetail, AcademyRecordFinTranUtil.getAcademyFinTranCodeForXPath(
						acadFinTransCommonCodeDoc, AcademyConstants.SHIPMENT_TENDER_GC));
				inputForLineTotalEle.setAttribute("AcademyFinTranReturnReasonCode", returnReasonCode);
			}
			else if ("CHARGE".equalsIgnoreCase(XMLUtil.getString(collectionDetail, "@ChargeType")))
			{
				log.verbose("charge type - CHARGE");
				setAttributesForPaymentTenders(inputForLineTotalEle, collectionDetail, AcademyRecordFinTranUtil.getAcademyFinTranCodeForXPath(
						acadFinTransCommonCodeDoc, AcademyConstants.SHIPMENT_TENDER_CC));
				inputForLineTotalEle.setAttribute("AcademyFinTranReturnReasonCode", returnReasonCode);
			}
			if (IsBopis){
				inputForLineTotalEle.setAttribute("AcademyFinTranShipMethod", AcademyConstants.STR_PICK_DELIVERY_METHOD);
			}
			log.verbose("input for line total - " + inputForLineTotalEle.toString());
		}
		
		log.endTimer("createInputForReturnInvoiceRecord");
	}

	/**
	 * 
	 * @param apiIndoc
	 * @param inDoc
	 * @param acadBean
	 * @throws Exception
	 */
	private void createInputForReturnLineRecord(YFSEnvironment env, YFCElement apiIndoc, Document inDoc, AcademyFinTranDataBean acadBean,
			Document acadFinTransCommonCodeDoc) throws Exception
	{

		YFCElement inputForMerchandiseLineEle = AcademyRecordFinTranUtil.getFinTranApiInput(apiIndoc);
		setCommonAttributes(inputForMerchandiseLineEle, inDoc);
		setAttributesForReturnMerchandiseLine(inputForMerchandiseLineEle, acadBean, inDoc, acadFinTransCommonCodeDoc);
		log.verbose("RO Merchandise Line: " + inputForMerchandiseLineEle.getString());

		log.verbose("Return reason code - " + acadBean.returnReasonCode);
		YFCElement inputForShippingLineEle = AcademyRecordFinTranUtil.getFinTranApiInput(apiIndoc);
		setCommonAttributes(inputForShippingLineEle, inDoc);

		// when ACADEMY_AT_FAULT
		if (AcademyReturnOrderUtil.isReturnReasonCodeExist(env, acadBean.returnReasonCode))
		{
			setAttributesForReturnShippingLine(inputForShippingLineEle, acadBean, inDoc, acadFinTransCommonCodeDoc, false);
			log.verbose("Academy at fault - " + inputForShippingLineEle.toString());
		}
		else // When CUSTOMER_AT_FAULT
		{
			// FIX for defect #3429 - need to capture the return shipping charge and tax in the FIN tran table.
			setAttributesForReturnShippingLine(inputForShippingLineEle, acadBean, inDoc, acadFinTransCommonCodeDoc, true);
			log.verbose("Customer at fault - " + inputForShippingLineEle.toString());
		}
	}

	/**
	 * 
	 * @param acadFinTranEle
	 * @param acadBean
	 * @param inDoc
	 * @throws Exception
	 */
	private void setAttributesForReturnMerchandiseLine(YFCElement acadFinTranEle, AcademyFinTranDataBean acadBean, Document inDoc,
			Document acadFinTransCommonCodeDoc) throws Exception
	{
		log.verbose("********* setAttributesForReturnMerchandiseLine bean - " + acadBean.toString());
		acadFinTranEle.setAttribute("AcademyFinTranCode", AcademyRecordFinTranUtil.getAcademyFinTranCodeForXPath(acadFinTransCommonCodeDoc,
				AcademyConstants.RO_MERCHANDISE_OR_SHIPPING));
		acadFinTranEle.setAttribute("AcademyFinTranSourceLine", String.valueOf((Integer.parseInt(acadBean.sourceLine)) - 1));
		acadFinTranEle.setAttribute("AcademyFinTranReferenceLine", String.valueOf((Integer.parseInt(acadBean.sourceLine)) - 1));
		acadFinTranEle.setAttribute("AcademyFinTranExtendedAmount", acadBean.getMerchandiseTotal());

		// Fixed Defect - Merchandise Amount should be Exact reversal on outbound i.e. Merchandise Extended Amount
		acadFinTranEle.setAttribute("AcademyFinTranMerchandiseAmount", acadBean.merchandiseExtendedAmt);

		acadFinTranEle.setAttribute("AcademyFinTranTaxAmount", acadBean.merchandiseTax);
		acadFinTranEle.setAttribute("AcademyFinTranUnitPrice", acadBean.merchandiseUnitPrice);
		acadFinTranEle.setAttribute("AcademyFinTranReturnReasonCode", acadBean.returnReasonCode);
		acadFinTranEle.setAttribute("AcademyFinTranQty", acadBean.qty);
		acadFinTranEle.setAttribute("AcademyFinTranShortSKU", acadBean.itemID);

		// FIX FOR DEFECT #3245 - Return Merchandise line should NOT show shipping method and type. Commenting out below lines
		// acadFinTranEle.setAttribute("AcademyFinTranShipMethod", acadBean.scac);
		// acadFinTranEle.setAttribute("AcademyFinTranShipType", acadBean.carrierService);

		// FIX FOR DEFECT #3246 - ITEM_DESC for return transactions was missing
		acadFinTranEle.setAttribute("AcademyFinTranItemDesc", acadBean.itemDescription);

		// Fixed Defect - columns DISCOUNT_AMOUNT, STORE_COUPON_AMOUNT and COUPON_PROMO_CODE in Acad_Fin_Transact table are not getting
		// populated
		acadFinTranEle.setAttribute("AcademyFinTranDiscountAmount", AcademyFinTranDataBean.getPositiveValue(acadBean.merchandiseDiscount));
		acadFinTranEle.setAttribute("AcademyFinTranDiscountId", acadBean.merchandiseDiscountReason);
		acadFinTranEle.setAttribute("AcademyFinTranStoreCouponAmount", AcademyFinTranDataBean.getPositiveValue(acadBean.merchandiseStoreCoupon));
		acadFinTranEle.setAttribute("AcademyFinTranCouponCode", acadBean.merchandiseCouponCode);
		acadFinTranEle.setAttribute("AcademyFinTranMfgCouponAmount", AcademyFinTranDataBean.getPositiveValue(acadBean.merchandiseMfgCoupon));
		// Fix for # 4175 - column Appeasement in Acad_Fin_Transact table is not getting if return with a previous appeasement
		if(acadBean.merchandiseAppeasement != null && !acadBean.merchandiseAppeasement.equals("0.0")){
			log.verbose("Given previous appeasement on SO : "+acadBean.merchandiseAppeasement);
			double appeaseAmt = Double.parseDouble(acadBean.merchandiseAppeasement);
			appeaseAmt = -1.0*appeaseAmt;
			acadFinTranEle.setAttribute("AcademyFinTranAppesementAmount", String.valueOf(appeaseAmt));
		}
		// End of fix #4175
		acadFinTranEle.setAttribute("AcademyFinTranShipMethod",acadBean.scac);
		if (log.isVerboseEnabled())
		{
			log.verbose("$$$$$$$$$$$$$$ setAttributesForReturnMerchandiseLine - acad fin tran element - " + acadFinTranEle.toString());
		}
	}

	/**
	 * 
	 * @param acadFinTranEle
	 * @param acadBean
	 * @param inDoc
	 * @throws Exception
	 */
	private void setAttributesForReturnShippingLine(YFCElement acadFinTranEle, AcademyFinTranDataBean acadBean, Document inDoc,
			Document acadFinTransCommonCodeDoc, boolean isCustomerAtFault) throws Exception
	{
		log.verbose("********* setAttributesForReturnShippingLine bean - " + acadBean.toString());
		acadFinTranEle.setAttribute("AcademyFinTranCode", AcademyRecordFinTranUtil.getAcademyFinTranCodeForXPath(acadFinTransCommonCodeDoc,
				AcademyConstants.RO_MERCHANDISE_OR_SHIPPING));
		acadFinTranEle.setAttribute("AcademyFinTranSourceLine", acadBean.sourceLine);

		// FIX for defect #3258 - REFERENCE_LINE error on return transactions.
		// acadFinTranEle.setAttribute("AcademyFinTranReferenceLine", acadBean.sourceLine);
		acadFinTranEle.setAttribute("AcademyFinTranReferenceLine", String.valueOf((Integer.parseInt(acadBean.sourceLine)) - 1));

		acadFinTranEle.setAttribute("AcademyFinTranShipLineFlag", "Y");
		acadFinTranEle.setAttribute("AcademyFinTranShortSKU", acadBean.shippingShortSku);
		acadFinTranEle.setAttribute("AcademyFinTranShipMethod", acadBean.scac);
		acadFinTranEle.setAttribute("AcademyFinTranShipType", acadBean.carrierService);
		acadFinTranEle.setAttribute("AcademyFinTranReturnReasonCode", acadBean.returnReasonCode);
		acadFinTranEle.setAttribute("AcademyFinTranQty", "1");

		if (isCustomerAtFault)
		{
			// Since we are deducting the amount from the total and this is a return, the values in the column ship_charge_amount and
			// tax_amount and extended amount(ship_charge_amount + tax_amount) will be positive
			double shippingChargeAmount = Double.parseDouble(AcademyFinTranDataBean.getPositiveValue(acadBean.shippingAmount));
			double shippingTax = Double.parseDouble(AcademyFinTranDataBean.getPositiveValue(acadBean.shippingTax));
			double shippingTotal = shippingChargeAmount + shippingTax;

			acadFinTranEle.setAttribute("AcademyFinTranShipChargeAmount", String.valueOf(shippingChargeAmount));
			acadFinTranEle.setAttribute("AcademyFinTranTaxAmount", String.valueOf(shippingTax));
			acadFinTranEle.setAttribute("AcademyFinTranExtendedAmount", String.valueOf(shippingTotal));
			acadFinTranEle.setAttribute("AcademyFinTranUnitPrice", String.valueOf(shippingChargeAmount));
		}
		else
		{
			// FIX FOR DEFECT #3255 - SHIP_CHARGE_AMOUNT (negative value), DISCOUNT_AMOUNT (positive value), DISCOUNT_PROMO_ID are missing
			// for return order
			acadFinTranEle.setAttribute("AcademyFinTranShipChargeAmount", acadBean.shippingCharge);
			acadFinTranEle.setAttribute("AcademyFinTranDiscountId", acadBean.shippingDiscountReason);
			acadFinTranEle.setAttribute("AcademyFinTranDiscountAmount", AcademyFinTranDataBean.getPositiveValue(acadBean.shippingDiscount));

			acadFinTranEle.setAttribute("AcademyFinTranTaxAmount", acadBean.shippingTax);
			acadFinTranEle.setAttribute("AcademyFinTranExtendedAmount", acadBean.getShipmentTotal());
			acadFinTranEle.setAttribute("AcademyFinTranUnitPrice", acadBean.shippingCharge);
		}
		// Fix for # 4175 - column Appeasement in Acad_Fin_Transact table is not getting if return with a previous appeasement
		if(acadBean.shippingAppeasement != null && !acadBean.shippingAppeasement.equals("0.0")){
			log.verbose("Given previous appeasement on SO : "+acadBean.merchandiseAppeasement);
			double appeaseAmt = Double.parseDouble(acadBean.shippingAppeasement);
			appeaseAmt = -1.0*appeaseAmt;
			acadFinTranEle.setAttribute("AcademyFinTranAppesementAmount", String.valueOf(appeaseAmt));
		}
		// End of # 4175

		if (log.isVerboseEnabled())
		{
			log.verbose("############  setAttributesForReturnShippingLine - acad fin tran element - " + acadFinTranEle.toString());
		}
	}

	/**
	 * 
	 * @param env
	 * @param inDoc
	 * @param apiIndoc
	 * @throws Exception
	 */
	private void createInputForCreditMemoRecord(YFSEnvironment env, Document inDoc, YFCElement apiIndoc, Document acadFinTransCommonCodeDoc) throws Exception
	{

		YFCElement inputForMerchandiseLineEle = AcademyRecordFinTranUtil.getFinTranApiInput(apiIndoc);
		setCommonAttributes(inputForMerchandiseLineEle, inDoc);
		setAttributesForCreditMemoLine(inputForMerchandiseLineEle, null, inDoc, acadFinTransCommonCodeDoc);
		NodeList collectionDetailList = XMLUtil.getNodeList(inDoc, "/InvoiceDetail/InvoiceHeader/CollectionDetails/CollectionDetail["
				+ "@ChargeType='CHARGE' and @Status='CHECKED']");

		for (int collectionCount = 0; collectionCount < collectionDetailList.getLength(); collectionCount++)
		{
			Element collectionDetail = (Element) collectionDetailList.item(collectionCount);
			YFCElement inputForLineTotalEle = AcademyRecordFinTranUtil.getFinTranApiInput(apiIndoc);
			setCommonAttributes(inputForLineTotalEle, inDoc);
			setAttributesForPaymentTenders(inputForLineTotalEle, collectionDetail, AcademyRecordFinTranUtil.getAcademyFinTranCodeForXPath(
					acadFinTransCommonCodeDoc, AcademyConstants.ORDER_ENTRY_CC_REFUND));
		}
	}

	/**
	 * 
	 * @param acadFinTranEle
	 * @param acadBean
	 * @param inDoc
	 * @throws Exception
	 */
	private void setAttributesForCreditMemoLine(YFCElement acadFinTranEle, AcademyFinTranDataBean acadBean, Document inDoc, Document acadFinTransCommonCodeDoc)
			throws Exception
	{
		acadFinTranEle.setAttribute("AcademyFinTranCode", AcademyRecordFinTranUtil.getAcademyFinTranCodeForXPath(acadFinTransCommonCodeDoc,
				AcademyConstants.ORDER_ENTRY_CC_APPEASEMENT));
		
		acadFinTranEle.setAttribute("AcademyFinTranExtendedAmount", XMLUtil.getString(inDoc, "/InvoiceDetail/InvoiceHeader/@TotalAmount"));
		acadFinTranEle.setAttribute("AcademyFinTranAppesementAmount", XMLUtil.getString(inDoc, "/InvoiceDetail/InvoiceHeader/@TotalAmount"));

		acadFinTranEle.setAttribute("AcademyFinTranAppsementCode", XMLUtil.getString(inDoc,
				"/InvoiceDetail/InvoiceHeader/LineDetails/LineDetail/LineCharges/LineCharge/@Reference"));
	}

	
	/**
	 * 
	 * @param acadFinTranEle
	 * @param inDoc
	 * @throws Exception
	 */
	private void setAttributesForAppeasementOrder(YFCElement acadFinTranEle, Document inDoc, Document acadFinTransCommonCodeDoc) throws Exception
	{
		log.verbose("setting attribute for appeasement order");
		acadFinTranEle.setAttribute("AcademyFinTranCode", AcademyRecordFinTranUtil.getAcademyFinTranCodeForXPath(acadFinTransCommonCodeDoc,
				AcademyConstants.SHIPMENT_TENDER_NO_REVENUE));
		acadFinTranEle.setAttribute("AcademyFinTranExtendedAmount", AcademyFinTranDataBean.getNegativeValue(XMLUtil.getString(inDoc,
				"/InvoiceDetail/InvoiceHeader/@LineSubTotal")));
		acadFinTranEle.setAttribute("AcademyFinTranAppesementAmount", AcademyFinTranDataBean.getNegativeValue(XMLUtil.getString(inDoc,
				"/InvoiceDetail/InvoiceHeader/@TotalDiscount")));

		Node parentNoNode = XPathUtil.getNode(inDoc, "/InvoiceDetail/InvoiceHeader/Order/References/Reference[@Name='AppeasementParentOrderNo']");
		if (!YFCObject.isVoid(parentNoNode))
		{
			acadFinTranEle.setAttribute("AcademyFinTranReferenceKey", ((Element) parentNoNode).getAttribute("Value"));
			log.verbose("acad fintran reference key - " + acadFinTranEle.getAttribute("AcademyFinTranReferenceKey"));
		}

		// FIX for defect #3296 - APPEASEMENT_CODE is not getting correctly populated where FIN_TRAN_CODE
		// is equal to 12. On this line,the actual appeasement code should be populated in the APPEASEMENT_CODE field
		// Node appeasementReasonNode = XPathUtil.getNode(inDoc,
		// "/InvoiceDetail/InvoiceHeader/Order/References/Reference[@Name='AppeasementParentOrderNo']");
		Node appeasementReasonNode = XPathUtil.getNode(inDoc, "/InvoiceDetail/InvoiceHeader/Order/References/Reference[@Name='AppeasementReasonCode']");

		if (!YFCObject.isVoid(appeasementReasonNode))
		{
			acadFinTranEle.setAttribute("AcademyFinTranAppsementCode", ((Element) appeasementReasonNode).getAttribute("Value"));
			log.verbose("acad fintran appease code - " + acadFinTranEle.getAttribute("AcademyFinTranAppsementCode"));
		}
	}

	private void createInputForTaxWriteOff(YFSEnvironment env, Document inDoc, YFCElement apiIndoc, AcademyFinTranDataBean acadBean,
			Document acadFinTransCommonCodeDoc) throws Exception
	{
		YFCElement inputForTaxWriteOffLineEle = AcademyRecordFinTranUtil.getFinTranApiInput(apiIndoc);
		setCommonAttributes(inputForTaxWriteOffLineEle, inDoc);
		setAttributesForTaxWriteOff(inputForTaxWriteOffLineEle, inDoc, acadBean, acadFinTransCommonCodeDoc);
	}

	private void setAttributesForTaxWriteOff(YFCElement acadFinTranEle, Document inDoc, AcademyFinTranDataBean acadBean, Document acadFinTransCommonCodeDoc)
			throws Exception
	{
		log.verbose("********** fin tran element : BEFORE - " + acadFinTranEle.toString());
		log.verbose("******* input to setAttributesForTaxWriteOff - " + XMLUtil.getXMLString(inDoc));
		acadFinTranEle.setAttribute("AcademyFinTranCode", AcademyRecordFinTranUtil.getAcademyFinTranCodeForXPath(acadFinTransCommonCodeDoc,
				AcademyConstants.SO_MERCHANDISE_OR_SHIPPING));
		acadFinTranEle.setAttribute("AcademyFinTranReferenceLine", "0");
		acadFinTranEle.setAttribute("AcademyFinTranTaxReserve", acadBean.merchandiseTaxWriteOff);

		NodeList collectionDetailList = XMLUtil.getNodeList(inDoc, "/InvoiceDetail/InvoiceHeader/CollectionDetails/CollectionDetail["
				+ "(@ChargeType='CHARGE' or @ChargeType='TRANSFER_IN') and @Status='CHECKED']");
		log.verbose("No of collections - " + collectionDetailList.getLength());
		if (collectionDetailList.getLength() > 0)
		{
			Element collectionDetail = (Element) collectionDetailList.item(0);
			log.verbose("********* Collection details - " + XMLUtil.getElementXMLString(collectionDetail));
			
			// FIX for defect #3471 - Column TENDER_TYPE and CC_TYPE should be blank in case for Tax Reserve line, 
			// commenting out below piece of code to fix this
			/*
			if ("CREDIT_CARD".equalsIgnoreCase(XMLUtil.getString(collectionDetail, "PaymentMethod/@PaymentType")))
			{
				acadFinTranEle.setAttribute("AcademyFinTranTenderType", XMLUtil.getString(collectionDetail, "PaymentMethod/@PaymentType"));
				acadFinTranEle.setAttribute("AcademyFinTranCCType", XMLUtil.getString(collectionDetail, "PaymentMethod/@CreditCardType"));
			}
			else if ("GIFT_CARD".equalsIgnoreCase(XMLUtil.getString(collectionDetail, "PaymentMethod/@PaymentType")))
			{
				acadFinTranEle.setAttribute("AcademyFinTranTenderType", XMLUtil.getString(collectionDetail, "PaymentMethod/@PaymentType"));
			}
			*/
		}
		acadFinTranEle.setAttribute("AcademyFinTranShipMethod",acadBean.scac);
		log.verbose("********** fin tran element : AFTER - " + acadFinTranEle.toString());
	}

	private Document addPLCCPaymentCollectionInfo(Document inDoc, double dRewardPoints) throws Exception
	{
		log.verbose("******* input to addPLCCPaymentCollectionInfo - ");
		
		Element eleCollectionDetail = (Element) XPathUtil.getNodeList(inDoc.getDocumentElement(), 
		"/InvoiceDetail/InvoiceHeader/CollectionDetails/CollectionDetail").item(0);
		Element eleCollectionDetails = (Element) XPathUtil.getNodeList(inDoc.getDocumentElement(), 
		"/InvoiceDetail/InvoiceHeader/CollectionDetails[CollectionDetail/PaymentMethod/@PaymentType='PLCC']").item(0);
		
		if(!YFCObject.isVoid(eleCollectionDetails)) {
			Element eleCollectionDetailNew = inDoc.createElement(AcademyConstants.ELEM_COLLECTION_DETAIL);
			XMLUtil.copyElement(inDoc, eleCollectionDetail, eleCollectionDetailNew);
			Element elePaymentMethodOld = (Element) XPathUtil.getNodeList(eleCollectionDetailNew, 
			"./PaymentMethod").item(0);
			eleCollectionDetailNew.removeChild(elePaymentMethodOld);
					
			eleCollectionDetailNew.setAttribute("AmountCollected", Double.toString(dRewardPoints));
			eleCollectionDetailNew.setAttribute("CreditAmount", Double.toString(dRewardPoints));
			eleCollectionDetailNew.setAttribute("DistributedAmount", Double.toString(dRewardPoints));
			eleCollectionDetailNew.setAttribute("RequestAmount", Double.toString(dRewardPoints));
			
			Element elePaymentMethodNew = inDoc.createElement(AcademyConstants.ELE_PAYMENT_METHOD);
			
			elePaymentMethodNew.setAttribute("ChargeSequence", "1");
			elePaymentMethodNew.setAttribute("CreditCardName", AcademyConstants.STR_PLCC_REWARDS_PAYMENT);
			elePaymentMethodNew.setAttribute("MaxChargeLimit", Double.toString(dRewardPoints));
			elePaymentMethodNew.setAttribute("PaymentType", AcademyConstants.STR_PLCC_REWARDS_PAYMENT);
			elePaymentMethodNew.setAttribute("TotalCharged", Double.toString(dRewardPoints));
			elePaymentMethodNew.setAttribute("ChargeSequence", "1");
			elePaymentMethodNew.setAttribute("ChargeSequence", "1");			
			eleCollectionDetailNew.appendChild(elePaymentMethodNew);
			eleCollectionDetails.appendChild(eleCollectionDetailNew);	
		}
		
		log.verbose("********** fin tran element : AFTER - " + XMLUtil.getXMLString(inDoc));
		return inDoc;
	}


	public static void main(String[] args)
	{
		Document doc = YFCDocument.getDocumentFor(new File("C://input.xml")).getDocument();
		Element docElement = doc.getDocumentElement();
		log.verbose(XMLUtil.getElementXMLString(docElement));
		try
		{
			Element itemExtnElement = (Element)XPathUtil.getNode(docElement, "/ItemList/Item/Extn");
			log.verbose(XMLUtil.getElementXMLString(itemExtnElement));
			String billToID = itemExtnElement.getAttribute("ExtnIsGiftCard");
			log.verbose(billToID);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		Node parentNoNode;

	}
}
