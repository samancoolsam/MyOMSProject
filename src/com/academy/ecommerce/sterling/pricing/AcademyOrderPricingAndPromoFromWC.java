package com.academy.ecommerce.sterling.pricing;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyPricingAndPromotionUtil;
import com.academy.ecommerce.sterling.util.AcademyServiceUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyOrderPricingAndPromoFromWC implements YIFCustomApi {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyOrderPricingAndPromoFromWC.class);
    public void setProperties(Properties props) {

    }

    /**
     * inDoc is input to beforChangeOrderUE
     * @param env
     * @param inDoc
     * @return
     */
 	public Document invokeUpdateWCOrder(YFSEnvironment env,	Document inDoc, 
 			Document getOrderDetailDoc) throws YFSException {
		try {
			log.beginTimer(" Begining of AcademyOrderPricingAndPromoFromWC->invokeUpdateWCOrder Api");
			log.verbose("Inside invokeUpdateWCOrder with indoc " + XMLUtil.getXMLString(inDoc));
			Document wcUpdateOrderRequestDoc = getUpdateWCOrderRequest(env, inDoc, getOrderDetailDoc);
			Document wcUpdateOrderResponseDoc = AcademyUtil.invokeService(env, 
					"AcademyWCUpdateGuestOrderService", wcUpdateOrderRequestDoc);
			log.verbose("Inside invokeUpdateWCOrder with outdoc " + XMLUtil.getXMLString(wcUpdateOrderResponseDoc));
			AcademyPricingAndPromotionUtil.hasError(wcUpdateOrderResponseDoc);
			log.endTimer(" Ending of AcademyOrderPricingAndPromoFromWC->invokeUpdateWCOrder Api");
			return wcUpdateOrderResponseDoc;
		} catch (YFSException e) {
			log.verbose("YFSException inside invokeUpdateWCOrder " + e.getMessage());
			throw e;
		} catch (Exception e) {
			log.verbose("Exception inside invokeUpdateWCOrder " + e.getMessage());
			throw AcademyPricingAndPromotionUtil.wrapToYFSException(e);
		}
	}
    

 	/**
 	 * Method invokes WCS Checkcout web service.
 	 * @param env
 	 * @param inDoc - Input from beforeChangeOrderUE
 	 * @return
 	 */
 	public Document invokeCheckOutWCOrder(YFSEnvironment env, Document inDoc, Document getOrderDetailDoc) throws YFSException {
		try {	
			log.verbose("Inside getCreateWCOrderWithLinesRequest with indoc " + XMLUtil.getXMLString(inDoc));
			String invokedFrom = inDoc.getDocumentElement().getAttribute("AcademyInvokedFrom");
			Document checkOutWCOrderRequest = null;
			if("PAYMENT".equals(invokedFrom)) {
				checkOutWCOrderRequest = getCheckoutRequestForPayment(env, inDoc, getOrderDetailDoc);
			} else {
				String orderNo = getOrderDetailDoc.getDocumentElement().getAttribute("OrderNo");
				checkOutWCOrderRequest = AcademyPricingAndPromotionUtil.createCheckOutWCOrderRequest(orderNo);
			}
			AcademyPricingAndPromotionUtil.setUserCredentialsForWCRequest(checkOutWCOrderRequest);
			log.verbose("Inside getCreateWCOrderWithLinesRequest with request " + 
					XMLUtil.getXMLString(checkOutWCOrderRequest));
			Document wcCheckOutOrderResponseDoc = AcademyUtil.invokeService(env, 
					"AcademyWCProcessOrderService", checkOutWCOrderRequest);
			log.verbose("Inside getCreateWCOrderWithLinesRequest with response " + 
					XMLUtil.getXMLString(wcCheckOutOrderResponseDoc));
			AcademyPricingAndPromotionUtil.hasError(wcCheckOutOrderResponseDoc);
			return wcCheckOutOrderResponseDoc;
		} catch (YFSException e) {
			log.verbose("YFSException inside invokeUpdateWCOrder " + e.getMessage());
			throw e;
		} catch (Exception e) {
			log.verbose("Exception inside invokeUpdateWCOrder " + e.getMessage());
			throw AcademyPricingAndPromotionUtil.wrapToYFSException(e);
		}
	}
    
 	/**
 	 * Method invokes WCS getOrder web service.
 	 * @param env
 	 * @param inDoc - Input from beforeChangeOrderUE
 	 * @return
 	 */
	public Document invokeGetWCOrder(YFSEnvironment env, Document inDoc) 
		throws Exception {
		log.verbose("Inside getCreateWCOrderWithLinesRequest with indoc " + XMLUtil.getXMLString(inDoc));
		String orderNo = inDoc.getDocumentElement().getAttribute("OrderNo");
		Document wcGetOrderRequestDoc = AcademyPricingAndPromotionUtil.createGetWCOrderRequest(orderNo);
		log.verbose("Inside invokeGetWCOrder with request " + 
				XMLUtil.getXMLString(wcGetOrderRequestDoc));
		Document wcGetOrderResponseDoc = AcademyUtil.invokeService(env, 
				"AcademyWCGetOrderService", wcGetOrderRequestDoc);
		log.verbose("Inside invokeGetWCOrder with response " + 
				XMLUtil.getXMLString(wcGetOrderResponseDoc));
		return wcGetOrderResponseDoc;
	}
 	
	/**
	 * This fetches order Merchandise price, discount and promotions 
	 * @param env
	 * @param inDoc
	 * @return
	 */
 	public Document updateAndGetOrderPriceAndPromoFromWC(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("Inside getCreateWCOrderWithLinesRequest with indoc " + XMLUtil.getXMLString(inDoc));
		Document getOrderDetailDoc = getOrderDetails(env, inDoc);
		invokeUpdateWCOrder(env, inDoc, getOrderDetailDoc);
		Document wcGetOrderResponseDoc = invokeGetWCOrder(env, getOrderDetailDoc);
		return mapGetWCOrderResponseToUEOutput(env, inDoc, wcGetOrderResponseDoc, false, getOrderDetailDoc); 
	}

 	/**
 	 * This fetches Order Merchandise price, discount, promotions, Shipping charges and Tax details 
 	 * @param env
 	 * @param inDoc
 	 * @return
 	 */
 	public Document checkOutAndGetOrderPriceAndPromoFromWC(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("Inside getCreateWCOrderWithLinesRequest with indoc " + XMLUtil.getXMLString(inDoc));
		Document getOrderDetailDoc = getOrderDetails(env, inDoc);
		invokeCheckOutWCOrder(env, inDoc, getOrderDetailDoc);
		Document wcGetOrderResponseDoc = invokeGetWCOrder(env, getOrderDetailDoc);
		return mapGetWCOrderResponseToUEOutput(env, inDoc, wcGetOrderResponseDoc, true, getOrderDetailDoc);
	}

 	/**
 	 * Method does update, checkout and getOrder on WCS 
 	 * @param env
 	 * @param inDoc
 	 * @return
 	 * @throws Exception
 	 */
 	public Document updateCheckOutAndGetOrderPriceAndPromoFromWC(YFSEnvironment env, Document inDoc) throws Exception {
		try {
			Document getOrderDetailDoc = getOrderDetails(env, inDoc);
			invokeUpdateWCOrder(env, inDoc, getOrderDetailDoc);
			invokeCheckOutWCOrder(env, inDoc, getOrderDetailDoc);
			Document wcGetOrderResponseDoc = invokeGetWCOrder(env, getOrderDetailDoc);
			return mapGetWCOrderResponseToUEOutput(env, inDoc, wcGetOrderResponseDoc, true, getOrderDetailDoc);
		} catch (YFSException e) {
			log.verbose("YFSException inside getItemPriceAndPromoFromWC " + e.getMessage());
			throw e;
		} catch (Exception e) {
			log.verbose("Exception inside getItemPriceAndPromoFromWC " + e.getMessage());
			throw AcademyPricingAndPromotionUtil.wrapToYFSException(e);
		}
	}
 	
 	/**
 	 * This method is invoked from the SDF.
 	 * @param env
 	 * @param inDoc
 	 * @return
 	 * @throws Exception
 	 */
 	public Document handleBeforeChangeOrder(YFSEnvironment env, Document inDoc) throws Exception {
		try {
			/*
			 * If Re-pricing is required, invoke IBM WC Update/Checkout/Get call based on the 
			 * scenario.  
			 */
			String invokedFrom = inDoc.getDocumentElement().getAttribute("AcademyInvokedFrom");
			if("ADD_LINE_ITEMS".equals(invokedFrom)) {
				return updateAndGetOrderPriceAndPromoFromWC(env, inDoc);
			} else if("FULFILLMENT_OPTIONS".equals(invokedFrom) || "FULFILLMENT_SUMMARY".equals(invokedFrom)) {
				return updateCheckOutAndGetOrderPriceAndPromoFromWC(env, inDoc);
			} else if ("PAYMENT".equals(invokedFrom)) {
				return checkOutAndGetOrderPriceAndPromoFromWC(env, inDoc);
			} 
			return inDoc;
		} catch (YFSException e) {
			log.verbose("YFSException inside getItemPriceAndPromoFromWC " + e.getMessage());
			throw e;
		} catch (Exception e) {
			log.verbose("Exception inside getItemPriceAndPromoFromWC " + e.getMessage());
			throw AcademyPricingAndPromotionUtil.wrapToYFSException(e);
		}
	}
 	
 	
 	/**
 	 * Updates BeforeChangeOrderUE input with the WCS response details like ExtnWCOrderItemIdentifier, Quantity
 	 * Price, promotions, Awards, Shipping Charge, Merchandise/Shipping tax, purchase with gift order line and 
 	 * returns as BeforeChangeOrderUE output.
 	 * @param env
 	 * @param inDoc - Input doc from beforeChangeOrderUE
 	 * @param wcOrderResponse - Document from GetOrder from WCS
 	 * @param isCheckoutCall
 	 * @return
 	 * @throws Exception
 	 */
 	public Document mapGetWCOrderResponseToUEOutput(YFSEnvironment env, Document inDoc, 
 			Document wcOrderResponse, boolean isCheckoutCall, Document getOrderDetailDoc) throws Exception {
 		
 		/*
 		 * Existing line could have modified with Price, Adjustments, Quantity, Charges, Taxes
 		 * ShipModeIdentifier,
 		 */
 		// Get the beforeChangeOrder input, update the same with WCS response and return.
 		YFCDocument beforChangeOrderInDoc = YFCDocument.getDocumentFor(inDoc);
 		Document getOrderResponseDoc = AcademyUtil.invokeService(env, "RemoveNameSpaceService", wcOrderResponse);
 		if("PAYMENT".equals(beforChangeOrderInDoc.getDocumentElement().getAttribute("AcademyInvokedFrom"))) {
 			Element orderLines = (Element)getOrderDetailDoc.getElementsByTagName("OrderLines").item(0);
 			XMLUtil.importElement(beforChangeOrderInDoc.getDocument().getDocumentElement(), orderLines);
 		}
 		YFCNodeList<YFCElement> orderLineList = beforChangeOrderInDoc.getElementsByTagName("OrderLine");
 		Element wcOrderElement = (Element)getOrderResponseDoc.getElementsByTagName("Order").item(0);

 		// For each order line form the input
 		for(int i = (orderLineList.getLength() - 1); i >= 0; i--) {

 			YFCElement orderLine = orderLineList.item(i);
 			Element orderLineFromWC = null;
 			Element getOrderDetailOrderLine = null;

 			/*
 			 * If the LineAction is Remove, then modify the beforeChangeOrderUE output for the line
 			 * so as OderLine has  only two attributes <OrderLine Action="REMOVE" OrderLineKey="" />
 			 */
 			if("REMOVE".equals(orderLine.getAttribute("Action"))) {
 				YFCElement modifiedOrderLine = orderLine.getParentElement().createChild("OrderLine");
				modifiedOrderLine.setAttribute("OrderLineKey", orderLine.getAttribute("OrderLineKey"));
				modifiedOrderLine.setAttribute("Action", "REMOVE");
				orderLine.getParentNode().removeChild(orderLine);
				continue;
 			}

 			/*
 			 * For each line from the input, get the corresponding WCS line. This is based on 
 			 * Sterling ExtnWCOrderItemIdentifier = WCS OrderItemIdentifier.
 			 * If the input order line is new, then compare ItemID of Sterling order line with the
 			 * Part number   
 			 */
 			
 			if(!YFCObject.isVoid(orderLine.getAttribute("OrderLineKey"))) {
 				getOrderDetailOrderLine = (Element)XPathUtil.getNode(getOrderDetailDoc, 
 	 					"Order/OrderLines/OrderLine[@OrderLineKey='" + orderLine.getAttribute("OrderLineKey") + "']");
 				
 				/* 
 				 * If the line is a GWP, WCS will keep change OrderItemIdentifier, so
 				 * we need to map Sterling order line with WCS only by ItemID. There may be a
 				 * scenario where GWP line might have removed in WCS order, in such case,
 				 * remove the line from Sterling Order. 
 				 */
 				if("Y".equals(getOrderDetailOrderLine.getAttribute("GiftFlag"))) {
 					String itemID = XPathUtil.getString(getOrderDetailOrderLine, "Item/@ItemID");	
 	 	 			orderLineFromWC = (Element)XPathUtil.getNode(wcOrderElement, 
 	 	 					"OrderItem/CatalogEntryIdentifier/ExternalIdentifier[PartNumber='" + itemID + "']");
 	 	 			if(!YFCObject.isVoid(orderLineFromWC)) {
 	 	 				orderLineFromWC = (Element)orderLineFromWC.getParentNode().getParentNode(); // This will return OrderItem element
 	 	 				//Remove this element from the WCOrder, so that it is not handled as new GiftLine from WCS 
 	 	 				orderLineFromWC.getParentNode().removeChild(orderLineFromWC);
 	 	 			} else {
 	 	 				// If GWP line no more valid in WCS, remove from Sterling Order
 	 	 				if("Y".equals(getOrderDetailDoc.getDocumentElement().getAttribute("DraftOrderFlag"))) {
 	 	 					YFCElement modifiedOrderLine = orderLine.getParentElement().createChild("OrderLine");
 	 	 					modifiedOrderLine.setAttribute("OrderLineKey", orderLine.getAttribute("OrderLineKey"));
 	 	 					modifiedOrderLine.setAttribute("Action", "REMOVE");
 	 	 					orderLine.getParentNode().removeChild(orderLine);
 	 	 				} else {
 	 	 					orderLine.setAttribute("Action", "CANCEL");
 	 	 				}
 	 	 				continue;
 	 	 			}
 				} else {
 					// else get the corresponding WCS order based on ExtnWCOrderItemIdentifier.
 	 				orderLineFromWC = (Element)XPathUtil.getNode(wcOrderElement, 
 	 	 					"OrderItem/OrderItemIdentifier[UniqueID='" + XPathUtil.getString(getOrderDetailOrderLine, 
 	 	 							"Extn/@ExtnWCOrderItemIdentifier")	+ "']");
 	 				// This is to ensure any order line not exists in WCS should be remove from Sterling.
 	 				if(!YFCObject.isVoid(orderLineFromWC)) {
 	 					orderLineFromWC = (Element)orderLineFromWC.getParentNode(); // This will return OrderItem element
 	 				} else {
 	 					//This may be the scenario when order line is cancelled from Sterling. 
 	 	 				continue;
 	 				}
 				}
 				
 			} else {
 				// Get the corresponding WCS order based on ItemID.
 				String itemID = XPathUtil.getString((Element)orderLine.getDOMNode(), "Item/@ItemID");	
 	 			orderLineFromWC = (Element)XPathUtil.getNode(wcOrderElement, 
 	 					"OrderItem/CatalogEntryIdentifier/ExternalIdentifier[PartNumber='" + itemID + "']");
 	 			orderLineFromWC = (Element)orderLineFromWC.getParentNode().getParentNode(); // This will return OrderItem element
 			}
 			
 			setOrderLineValues(env, orderLine, orderLineFromWC, beforChangeOrderInDoc, getOrderDetailOrderLine, wcOrderElement);
 			
 	 		if(isCheckoutCall) {
 	 			YFCNodeList<YFCElement> lineTaxesFromOrderLine = orderLine.getElementsByTagName("LineTaxes");
 	 			YFCElement lineTaxes = null;
 	 			if(lineTaxesFromOrderLine.getLength() > 0) {
 	 				lineTaxes = lineTaxesFromOrderLine.item(0);
 	 			} else {
 	 				lineTaxes = orderLine.createChild("LineTaxes");	
 	 			}
 	 			
 	 			// Map sales tax
 	 			Element salesTax = (Element)XPathUtil.getNode(lineTaxes.getDOMNode(), 
 	 				"LineTax[@ChargeCategory='TAXES' and @TaxName='Merchandise']");
 	 			if(YFCObject.isVoid(salesTax)) {
 	 				salesTax = (Element)lineTaxes.createChild("LineTax").getDOMNode();
 	 			}
 	 			salesTax.setAttribute("ChargeCategory", "TAXES");
 	 			salesTax.setAttribute("ChargeName", "Taxes");
 	 			salesTax.setAttribute("TaxName", "Merchandise");
 	 			salesTax.setAttribute("Tax", 
 	 					AcademyPricingAndPromotionUtil.getAbsValue(XPathUtil.getString(orderLineFromWC, 
 	 							"OrderItemAmount/SalesTax")));
 	 			
 	 			// Map shipping tax
 	 			Element shippingTax = (Element)XPathUtil.getNode(lineTaxes.getDOMNode(), 
	 				"LineTax[@ChargeCategory='Shipping' and @TaxName='Shipping']");
	 			if(YFCObject.isVoid(shippingTax)) {
	 				shippingTax = (Element)lineTaxes.createChild("LineTax").getDOMNode();
	 			}
 	 			shippingTax.setAttribute("ChargeCategory", "Shipping");
 	 			shippingTax.setAttribute("ChargeName", "ShippingCharge");
 	 			shippingTax.setAttribute("TaxName", "Shipping");
 	 			shippingTax.setAttribute("Tax", AcademyPricingAndPromotionUtil.getAbsValue(
 	 					XPathUtil.getString(orderLineFromWC, "OrderItemAmount/ShippingTax")));
 	 		}
 		}
 		// Add all the gift item lines to beforeChangeOrderUE output and persist in the system.
 		YFCElement beforeChangeOrderOrderLines = (YFCElement)beforChangeOrderInDoc.getElementsByTagName("OrderLines").item(0);
 		NodeList giftWithPurchaseLineList = XPathUtil.getNodeList(wcOrderElement, 
 				"OrderItem/OrderItemAmount[@freeGift='true']");
 		for(int freeGiftLine = 0; freeGiftLine < giftWithPurchaseLineList.getLength(); freeGiftLine++) {
 			YFCElement newGiftLine = beforeChangeOrderOrderLines.createChild("OrderLine");
 			// Mark this lien as gift Item
 			newGiftLine.setAttribute("GiftFlag", "Y");
 			Element orderLineFromWC = (Element)giftWithPurchaseLineList.item(freeGiftLine).getParentNode();
 			YFCElement item = newGiftLine.createChild("Item");
 			
 			Document itemDetail = AcademyPricingAndPromotionUtil.getItemDetailFromSterling(env, 
 					XPathUtil.getString(orderLineFromWC, "CatalogEntryIdentifier/ExternalIdentifier/PartNumber"));
 			
 			item.setAttribute("ItemID", XPathUtil.getString(itemDetail, "ItemList/Item/@ItemID"));
 			item.setAttribute("ProductClass",  XPathUtil.getString(itemDetail, "ItemList/Item/PrimaryInformation/@DefaultProductClass"));
 			item.setAttribute("UnitOfMeasure", XPathUtil.getString(itemDetail, "ItemList/Item/@UnitOfMeasure"));
 			setOrderLineValues(env, newGiftLine, orderLineFromWC, beforChangeOrderInDoc, null, wcOrderElement);
 		}
 		return beforChangeOrderInDoc.getDocument();
 	}

 	/**
 	 * Sets Order line values like ExtnWCOrderItemIdentifier, Quantity, Discounts, Promotions, Awards
 	 * SCAC value.
 	 * @param env
 	 * @param orderLine
 	 * @param orderLineFromWC
 	 * @param changeOrderDoc
 	 * @throws Exception
 	 */
	private void setOrderLineValues(YFSEnvironment env, YFCElement orderLine, Element orderLineFromWC, 
			YFCDocument changeOrderDoc, Element getOrderDetailOrderLine, Element wcOrderElement) throws Exception {
		
		/*
		 * Set GiftFlag, ExtnWCOrderItemIdentifier, Quantity, Promotions and Awards
		 */
		
		// If getOrderDetailOrderLine == null, orderLineFromWC represents new line containing GWP 
		if(YFCObject.isNull(getOrderDetailOrderLine)) {
			YFCElement extnEle = orderLine.createChild("Extn");
			extnEle.setAttribute("ExtnWCOrderItemIdentifier", 
					XPathUtil.getString(orderLineFromWC, "OrderItemIdentifier/UniqueID"));
		}
		orderLine.setAttribute("OrderedQty", XPathUtil.getString(orderLineFromWC, "Quantity"));
		
		// If orderLine has LinePriceInfo element, then set Unit price and List price 
		YFCElement linePriceInfo = orderLine.getElementsByTagName("LinePriceInfo").item(0);
		if(YFCObject.isVoid(linePriceInfo)) {
			linePriceInfo = orderLine.createChild("LinePriceInfo");
		}
		
		linePriceInfo.setAttribute("UnitPrice", XPathUtil.getString(orderLineFromWC, "OrderItemAmount/UnitPrice/Price"));
		String listPrice = "0.00";
		NodeList userDataList = orderLineFromWC.getElementsByTagName("UserDataField");
		for(int j = 0; j < userDataList.getLength(); j++) {
			if("EDLP".equalsIgnoreCase(((Element)userDataList.item(j)).getAttribute("name"))) {
				listPrice = userDataList.item(j).getTextContent();
				break;
			}
		}
		linePriceInfo.setAttribute("ListPrice", XPathUtil.getString(orderLineFromWC, listPrice));
		
		/*
		 * Reset the LineCharges. If there is no line adjustments, it means the adjustments are no 
		 * longer valid and need to be removed from Sterling. For e.g. if the promotion code removed
		 * from WCS, then their is no adjustment returned and previously saved promotion discount need
		 * to be cleared from Sterling.
		 */
		YFCElement lineCharges = orderLine.getChildElement("LineCharges");
		if(YFCObject.isVoid(lineCharges)) {
			lineCharges = orderLine.createChild("LineCharges");
		}
		lineCharges.setAttribute("Reset", "Y");
		
		// Set Shipping Charge here.
		Element shippingCharge = (Element)XPathUtil.getNode(lineCharges.getDOMNode(), 
			"LineCharge[@ChargeCategory='Shipping' and @ChargeName='ShippingCharge']");

		if(YFCObject.isVoid(shippingCharge)) {
			shippingCharge = (Element)lineCharges.createChild("LineCharge").getDOMNode();
			shippingCharge.setAttribute("ChargeCategory", "Shipping");
			shippingCharge.setAttribute("ChargeName",  "ShippingCharge");
		}
		if(!YFCObject.isVoid(shippingCharge)) {
			shippingCharge.setAttribute("ChargePerLine", XPathUtil.getString(orderLineFromWC, 
					"OrderItemAmount/ShippingCharge"));
			shippingCharge.setAttribute("Reference", "Shipping Charge");
		}

		
		/*
		 * Logic to persist Promotion and Awards 
		 * Check from the order detail, if the promotion is already persisted. If not, persist else update
		 * the existing. Similarly Awards, first check if the Award persisted already, if not persist 
		 * else update the existing one.
		 *  
		 */
		
		// Persist adjustments 
		NodeList adjustmentList = orderLineFromWC.getElementsByTagName("Adjustment");
		if(adjustmentList.getLength() > 0) {

			// Get/Create Promotions element from Order level
			YFCElement promotions = changeOrderDoc.getDocumentElement().getChildElement("Promotions"); 
			if(YFCObject.isVoid(promotions)) {
				promotions = changeOrderDoc.getDocumentElement().createChild("Promotions");
			}
			
			// Get/Create Awards element from Order line 
			YFCElement awards = orderLine.getChildElement("Awards");
			if(YFCObject.isVoid(awards)) {
				awards = orderLine.createChild("Awards");
			}

			/*
			 * All promotions comes as Adjustments
			 */
			
			//For each Adjustment 
			for(int adj = 0; adj < adjustmentList.getLength(); adj ++) {
				Element adjustment = (Element)adjustmentList.item(adj);
				setPromotionsAsDicscountCharge(lineCharges, adjustment);
				// Add promotion detail at Order level and Award detail at line level
				
				Element orderPromo = null;

				String adjustmentCode = XPathUtil.getString(adjustment, "Code");
				if("PromocodePromotion".equalsIgnoreCase(adjustmentCode)){
					Element promotionCodeEle = (Element)XPathUtil.getNode(wcOrderElement, "PromotionCode/Code");
					if(!YFCObject.isVoid(promotionCodeEle)) {
						String promoCode = promotionCodeEle.getTextContent();
						orderPromo = (Element)XPathUtil.getNode(changeOrderDoc.getDOMNode(), 
							"Order/Promotions/Promotion[@PromotionId='" + promoCode + "']");
						adjustmentCode = promoCode;
					}
				} else {
					orderPromo = (Element)XPathUtil.getNode(changeOrderDoc.getDOMNode(), 
						"Order/Promotions/Promotion[@PromotionType='" + XPathUtil.getString(adjustment, "Usage") + "' and " +
							"@PromotionId='" + adjustmentCode + "']");
				}
				

				// If promotion not present in order detail, add the promotion detail.
				if(YFCObject.isVoid(orderPromo)) {
					orderPromo = (Element) promotions.createChild("Promotion").getDOMNode();
					orderPromo.setAttribute("PromotionId", adjustmentCode);
					orderPromo.setAttribute("PromotionType", XPathUtil.getString(adjustment, "Usage"));
					orderPromo.setAttribute("PromotionApplied", "Y");
				}
				// Add awards at line level
				Element award = (Element)XPathUtil.getNode(awards.getDOMNode(), 
						"Award[@AwardId='" + XPathUtil.getString(adjustment, "Code") + "']");
				if(YFCObject.isVoid(award)) {
					award = (Element)awards.createChild("Award").getDOMNode();
				}
				String awardAmount = AcademyPricingAndPromotionUtil.getAbsValue(XPathUtil.getString(adjustment, "Amount")); 
				award.setAttribute("AwardAmount", String.valueOf(Double.parseDouble(awardAmount) + 
						Double.parseDouble(YFCObject.isVoid(award.getAttribute("AwardAmount")) ? 
								"0.00" : award.getAttribute("AwardAmount"))));
				award.setAttribute("AwardId", XPathUtil.getString(adjustment, "Code"));
				award.setAttribute("AwardType", XPathUtil.getString(adjustment, "Usage"));
				award.setAttribute("Description", XPathUtil.getString(adjustment, "Description"));
				award.setAttribute("PromotionId", orderPromo.getAttribute("PromotionId"));
				award.setAttribute("AwardApplied", "Y");
			}
		}
			// Map shipping carrier service code
		Node shipModeId = XPathUtil.getNode(orderLineFromWC, 
				"OrderItemShippingInfo/ShippingMode/ShippingModeIdentifier/UniqueID");
		if(!YFCObject.isVoid(shipModeId)) {
			String[] scacFromWC = AcademyPricingAndPromotionUtil.getScacCodeForShipModeId(env, 
					shipModeId.getTextContent());
			orderLine.setAttribute("ScacAndService", scacFromWC[1]);
		}
	}
	
	/**
	 * Gets OrderDetails from Sterling System
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws YFSException
	 */
	private static Document getOrderDetails(YFSEnvironment env, Document inDoc) throws YFSException {
		try {
			env.setApiTemplate("getOrderDetails", "global/template/api/pricing/order/getOrderDetailsForPricing.xml");
			Document getOrderOutDoc = AcademyUtil.invokeAPI(env, "getOrderDetails", inDoc);
			env.clearApiTemplate("getOrderDetails");
			return getOrderOutDoc;
		} catch (Exception e) {
			log.error(e);
			AcademyPricingAndPromotionUtil.wrapToYFSException(e);
		}
		return inDoc;
	}

	/**
	 * This method prepares request for WCS order. 
	 * @param env
	 * @param inDoc
	 * @param getOrderDetailDoc
	 * @return
	 * @throws YFSException
	 */
	private static Document getUpdateWCOrderRequest(YFSEnvironment env, Document inDoc, 
			Document getOrderDetailDoc) throws YFSException {
		
		try {
			Document updateWCOrderRequest = AcademyServiceUtil.getSOAPMsgTemplateForWCUpdateOrder();
			AcademyPricingAndPromotionUtil.setUserCredentialsForWCRequest(updateWCOrderRequest);
			Element inDocEle = inDoc.getDocumentElement();
			
			//Set OrderIdentifier
			Node orderIdentifier = updateWCOrderRequest.getElementsByTagName("_ord:OrderIdentifier").item(0);
			((Element)orderIdentifier).getElementsByTagName("_wcf:UniqueID").item(0).setTextContent(
					getOrderDetailDoc.getDocumentElement().getAttribute("OrderNo"));
			
			Element wcOrderEle = (Element)updateWCOrderRequest.getElementsByTagName("_ord:Order").item(0);
			Element wcOrderItemTemplate = (Element)wcOrderEle.getElementsByTagName("_ord:OrderItem").item(0);
			wcOrderItemTemplate.getParentNode().removeChild(wcOrderItemTemplate);
			
			NodeList orderLineList = inDocEle.getElementsByTagName("OrderLine");

			String orderPurpose = getOrderDetailDoc.getDocumentElement().getAttribute("OrderPurpose");
			Document returnOrderDetails = null;
			if("EXCHANGE".equals(orderPurpose)) {
				YFCDocument returnOrderInDoc = YFCDocument.createDocument("Order");
				returnOrderInDoc.getDocumentElement().setAttribute("OrderHeaderKey", 
						getOrderDetailDoc.getDocumentElement().getAttribute("ReturnOrderHeaderKeyForExchange"));
				returnOrderDetails = getOrderDetails(env, returnOrderInDoc.getDocument());
			}
			
			/* 
			 * Iterate through each item line and construct request and the possible scenario
			 * 1) New item added (Order line without OrderLineKey),
			 * 2) Existing line modified (Order line with OrderLineKey)
			 * 3) GWP line which should not be included in the WCS update request. OrderLine GiftFlag="Y"
			 * 4) Remove existing line.
			 */
			
			for(int i = 0; i < orderLineList.getLength(); i++) {
				Element orderLineFromInput = (Element)orderLineList.item(i);
				
				String orderLineKey = orderLineFromInput.getAttribute("OrderLineKey");

				Element wcOrderItemEle = (Element)wcOrderItemTemplate.cloneNode(true);
				Element orderItemIdEle = (Element)wcOrderItemEle.getElementsByTagName("_ord:OrderItemIdentifier").item(0);
				
				// This can also be checked with Action="CREATE"/"MODIFY"/"REMOVE"
				if(!YFCObject.isVoid(orderLineKey)) {
					// Existing line getting modified.
					Element orderLineFromGetOrder = (Element)XPathUtil.getNode(getOrderDetailDoc, 
							"Order/OrderLines/OrderLine[@OrderLineKey='" + 
							orderLineFromInput.getAttribute("OrderLineKey") + "']");
					
					// If the line is GWP, skip the iteration.
					if("Y".equals(orderLineFromGetOrder.getAttribute("GiftFlag"))) {
						continue;
					}
					
					/* 
					 * Else, construct OrderItem element, populate values like, OrderItemIdentifier, 
					 * ItemID, Quantity, Shipping address, ShippingModeID, PriceOverride, UnitPrice
					 */      
					
					// Set OrderItemIdentifier which is ExtnWCOrderItemIdentifier
					orderItemIdEle.getElementsByTagName("_wcf:UniqueID").item(0).setTextContent(
							XPathUtil.getString(orderLineFromGetOrder, "Extn/@ExtnWCOrderItemIdentifier"));
					
					// If the Action="REMOVE", set quantity as zero, and send.
					
					if("REMOVE".equals(orderLineFromInput.getAttribute("Action")) || 
							"CANCEL" .equals(orderLineFromInput.getAttribute("Action"))) {
						wcOrderItemEle.getElementsByTagName("_ord:Quantity").item(0).setTextContent("0.00");
						/* 
						 * Remove CatalogEntryIdentifier, OrderItemAmount and OrderItemShippingInfo
						 * elements form the request. Otherwise WCS throws exception
						 */
						
						Element catalogEntryIdentifier = (Element) 
							wcOrderItemEle.getElementsByTagName("_ord:CatalogEntryIdentifier").item(0);
						catalogEntryIdentifier.getParentNode().removeChild(catalogEntryIdentifier);
						Element orderItemAmount = (Element) 
							wcOrderItemEle.getElementsByTagName("_ord:OrderItemAmount").item(0);
						orderItemAmount.getParentNode().removeChild(orderItemAmount);
						Element orderItemShippingInfo = (Element) 
							wcOrderItemEle.getElementsByTagName("_ord:OrderItemShippingInfo").item(0);
						orderItemShippingInfo.getParentNode().removeChild(orderItemShippingInfo);
						wcOrderEle.appendChild(wcOrderItemEle);
						continue;
					}
					
					/* 
					 * Setting Item ID, since input doc for the existing line does not have ItemID, 
					 * get it from the SalesOrderDetail document
					 */
					wcOrderItemEle.getElementsByTagName("_wcf:PartNumber").item(0).setTextContent(
							XPathUtil.getString(orderLineFromGetOrder, "Item/@ItemID"));

					/* 
					 * Set the quantity.
					 */
					String lineQty = "0.00";
					if(!YFCObject.isVoid(XPathUtil.getString(orderLineFromInput, "OrderLineTranQuantity/@OrderedQty"))) {
						lineQty = XPathUtil.getString(orderLineFromInput, "OrderLineTranQuantity/@OrderedQty");
					} else if(!YFCObject.isVoid(XPathUtil.getString(orderLineFromInput, "@OrderedQty"))) {
						lineQty = XPathUtil.getString(orderLineFromInput, "@OrderedQty");
					} else if(!YFCObject.isVoid(XPathUtil.getString(orderLineFromGetOrder, "@OrderedQty"))) {
						lineQty = XPathUtil.getString(orderLineFromGetOrder, "@OrderedQty");
					}
					wcOrderItemEle.getElementsByTagName("_ord:Quantity").item(0).setTextContent(lineQty);

					// Set Price
					Element orderItemAmount = (Element)wcOrderItemEle.getElementsByTagName("_ord:OrderItemAmount").item(0);
					orderItemAmount.getElementsByTagName("_wcf:Price").item(0).setTextContent(
							XPathUtil.getString(orderLineFromGetOrder, "LinePriceInfo/@UnitPrice"));
					
					/* 
					 * Set price override info here
					 * If the exchange order has same item as of return order or if the line item is a gift card, 
					 * set priceOverride="true"
					 */
					boolean isPriceOverriden = false;
					if("Y".equals(XPathUtil.getString(orderLineFromGetOrder, "LinePriceInfo/@IsPriceLocked"))) {
						isPriceOverriden = true;
					}
					
					if(!YFCObject.isNull(returnOrderDetails)) {
						if(!YFCObject.isVoid(XPathUtil.getNode(returnOrderDetails, 
								"Order/OrderLines/OrderLine/Item[@ItemID='" + 
								XPathUtil.getString(orderLineFromGetOrder, "Item/@ItemID") + "']"))) {
							isPriceOverriden = true;
						}
					}
					
					if(isPriceOverriden) {
						orderItemAmount.setAttribute("priceOverride", "true");
					}
				} else {
					/* 
					 * New Line.
					 * Remove OrderItemIdentifier element from the request since we don't have OrderItemIdentifier 
					 */
					orderItemIdEle.getParentNode().removeChild(orderItemIdEle);
					
					// Set ItemID
					wcOrderItemEle.getElementsByTagName("_wcf:PartNumber").item(0).setTextContent(
							XPathUtil.getString(orderLineFromInput, "Item/@ItemID"));
					
					// Set Quantity
					wcOrderItemEle.getElementsByTagName("_ord:Quantity").item(0).setTextContent(
							XPathUtil.getString(orderLineFromInput, "OrderLineTranQuantity/@OrderedQty"));

					// Set Price
					Element orderItemAmount = (Element)wcOrderItemEle.getElementsByTagName("_ord:OrderItemAmount").item(0);
					if(YFCObject.isVoid(XPathUtil.getString(orderLineFromInput, "LinePriceInfo/@UnitPrice"))) {
						orderItemAmount.getElementsByTagName("_wcf:Price").item(0).setTextContent("0.00");
					} else {
						orderItemAmount.getElementsByTagName("_wcf:Price").item(0).setTextContent(
								XPathUtil.getString(orderLineFromInput, "LinePriceInfo/@UnitPrice"));
					}

					// In case of exchange order or if Item is a gift card, set price override info here
					boolean isPriceOverriden = false;
					if("Y".equals(XPathUtil.getString(orderLineFromInput, "LinePriceInfo/@IsPriceLocked"))) {
						isPriceOverriden = true;
					}
					
					if(!YFCObject.isNull(returnOrderDetails)) {
						if(!YFCObject.isVoid(XPathUtil.getNode(returnOrderDetails, 
								"Order/OrderLines/OrderLine/Item[@ItemID='" + 
								XPathUtil.getString(orderLineFromInput, "Item/@ItemID") + "']"))) {
							isPriceOverriden = true;
						}
					}
					if(isPriceOverriden) {
						orderItemAmount.setAttribute("priceOverride", "true");
					}
				}
				
				/* 
				 * Set Line Shipping info. If called from ADD_LINE_ITEMS/FULFILLMENT_SUMMERY (i.e. PersonInfoShipTo 
				 * is missing in OrderLine) screen, remove OrderItemShippingInfo element from the request. 
				 * Else set the the   
				 */
				
				Node lineShipTo = XPathUtil.getNode(orderLineFromInput, "PersonInfoShipTo");
				if(YFCObject.isVoid(lineShipTo)) {
					Element orderItemShippingInfo = (Element)wcOrderItemEle.getElementsByTagName("_ord:OrderItemShippingInfo").item(0);
					orderItemShippingInfo.getParentNode().removeChild(orderItemShippingInfo);
				} else {
					wcOrderItemEle.getElementsByTagName("_wcf:AddressLine").item(0).setTextContent(
							XPathUtil.getString(lineShipTo, "@AddressLine1"));
					wcOrderItemEle.getElementsByTagName("_wcf:City").item(0).setTextContent(
									XPathUtil.getString(lineShipTo, "@City"));
					wcOrderItemEle.getElementsByTagName("_wcf:StateOrProvinceName").item(0).setTextContent(
									XPathUtil.getString(lineShipTo, "@State"));
					wcOrderItemEle.getElementsByTagName("_wcf:Country").item(0).setTextContent(
									XPathUtil.getString(lineShipTo, "@Country"));
					wcOrderItemEle.getElementsByTagName("_wcf:PostalCode").item(0).setTextContent(
									XPathUtil.getString(lineShipTo, "@ZipCode"));
					
					// Set shipping mode identifier to CarrierServiceCode(LOS) value
					String carrierServiceCode = XPathUtil.getString(orderLineFromInput, "@CarrierServiceCode");
					Element orderItemShippingInfo = (Element)wcOrderItemEle.getElementsByTagName("_ord:OrderItemShippingInfo").item(0);
					if(!YFCObject.isVoid(carrierServiceCode)) {
						//If CarrierServiceCode(LOS) is EXPEDITED, then set expedite='true'
						if("EXPEDITED".equals(carrierServiceCode)) {
							orderItemShippingInfo.setAttribute("expedite", "true");
						} else {
							Element shippingModeId = (Element)wcOrderItemEle.getElementsByTagName("_ord:ShippingModeIdentifier").item(0);
							shippingModeId.getElementsByTagName("_ord:UniqueID").item(0).setTextContent(carrierServiceCode);
						}
					} else {
						// If no CarrierServiceCode(LOS) info, remove OrderItemShippingInfo element from the request
						orderItemShippingInfo.getParentNode().removeChild(orderItemShippingInfo);
					}
				}
				wcOrderEle.appendChild(wcOrderItemEle);
			}
			return updateWCOrderRequest;
		} catch (Exception e) {
			throw AcademyPricingAndPromotionUtil.wrapToYFSException(e);
		}
	}
	
	/**
	 * This method prepares request for Adding and Removing coupons to WCS order.
	 * @param inDoc
	 * @param getOrderDetailDoc
	 * @return
	 * @throws YFSException
	 */
	private static Document getCheckoutRequestForPayment(YFSEnvironment env, Document inDoc, 
			Document getOrderDetailDoc) throws YFSException {
		try {
			String orderNo = getOrderDetailDoc.getDocumentElement().getAttribute("OrderNo");
			Document checkOutWCOrderRequest = AcademyServiceUtil.getSOAPMsgTemplateForProcessOrderForPayments();
			Element docEle = checkOutWCOrderRequest.getDocumentElement();
			docEle.getElementsByTagName("_wcf:UniqueID").item(0).setTextContent(orderNo);
			Element wcOrderEle = (Element)checkOutWCOrderRequest.getElementsByTagName("_ord:Order").item(0);
			// Set coupon code. Assumption only one coupon would be added to an order
			NodeList promotionList = inDoc.getElementsByTagName("Promotion");
			if (promotionList.getLength() > 0) {
				Element promotionFromInput = (Element)promotionList.item(0);
				if("CREATE".equals(promotionFromInput.getAttribute("Action"))) {
					Element promotionCode = (Element)wcOrderEle.getElementsByTagName("_ord:PromotionCode").item(0);
					promotionCode.getElementsByTagName("_ord:Code").item(0).setTextContent(
							promotionFromInput.getAttribute("PromotionId"));
					wcOrderEle.appendChild(promotionCode);
				}
				/*
				 * if the Action="REMOVE", pass blank Promotion Code.
				 * With this WCS would remove the coupon from the order and re price. 
				 */
			}
			return checkOutWCOrderRequest;
		} catch (Exception e) {
			throw AcademyPricingAndPromotionUtil.wrapToYFSException(e);
		}
	}
	
	/**
	 * 
	 * @param lineCharges
	 * @param adjustment
	 * @throws YFSException
	 */
	private static void setPromotionsAsDicscountCharge(YFCElement lineCharges, Element adjustment) throws YFSException {
		try {
			String adjustmentCode = XPathUtil.getString(adjustment, "Code");
			Element lineCharge = null;
			if(!YFCObject.isVoid(adjustmentCode) && adjustmentCode.toUpperCase().contains("SHIPPINGPROMO")) {
				lineCharge = (Element)XPathUtil.getNode(lineCharges.getDOMNode(), 
					"LineCharge[@ChargeCategory='Promotions' and @ChargeName='ShippingPromotion']");
				if(YFCObject.isVoid(lineCharge)) {
					lineCharge = (Element)lineCharges.createChild("LineCharge").getDOMNode();
					lineCharge.setAttribute("ChargeCategory", "Promotions");
					lineCharge.setAttribute("ChargeName",  "ShippingPromotion");
				}
			} else if(!YFCObject.isVoid(adjustmentCode) && adjustmentCode.toUpperCase().contains("BUY2GET")) {
				lineCharge = (Element)XPathUtil.getNode(lineCharges.getDOMNode(), 
					"LineCharge[@ChargeCategory='Promotions' and @ChargeName='BOGO']");
				if(YFCObject.isVoid(lineCharge)) {
					lineCharge = (Element)lineCharges.createChild("LineCharge").getDOMNode();
					lineCharge.setAttribute("ChargeCategory", "Promotions");
					lineCharge.setAttribute("ChargeName",  "BOGO");
				}
			} else if(!YFCObject.isVoid(adjustmentCode) && adjustmentCode.toUpperCase().contains("GIFTWITHPURCHASE")) {
				lineCharge = (Element)XPathUtil.getNode(lineCharges.getDOMNode(), 
					"LineCharge[@ChargeCategory='Promotions' and @ChargeName='GWP']");
				if(YFCObject.isVoid(lineCharge)) {
					lineCharge = (Element)lineCharges.createChild("LineCharge").getDOMNode();
					lineCharge.setAttribute("ChargeCategory", "Promotions");
					lineCharge.setAttribute("ChargeName",  "GWP");
				}
			} else if(!YFCObject.isVoid(adjustmentCode) && adjustmentCode.toUpperCase().contains("PROMOCODE")) {
				lineCharge = (Element)XPathUtil.getNode(lineCharges.getDOMNode(), 
					"LineCharge[@ChargeCategory='DiscountCoupon' and @ChargeName='DiscountCoupon']");
				if(YFCObject.isVoid(lineCharge)) {
					lineCharge = (Element)lineCharges.createChild("LineCharge").getDOMNode();
					lineCharge.setAttribute("ChargeCategory", "DiscountCoupon");
					lineCharge.setAttribute("ChargeName",  "DiscountCoupon");
				}
			} else {
				lineCharge = (Element)XPathUtil.getNode(lineCharges.getDOMNode(), 
					"LineCharge[@ChargeCategory='Promotions' and @ChargeName='Other']");
				if(YFCObject.isVoid(lineCharge)) {
					lineCharge = (Element)lineCharges.createChild("LineCharge").getDOMNode();
					lineCharge.setAttribute("ChargeCategory", "Promotions");
					lineCharge.setAttribute("ChargeName",  "Other");
				}
			}
			if(!YFCObject.isVoid(lineCharge)) {
				lineCharge.setAttribute("ChargePerLine", 
						AcademyPricingAndPromotionUtil.getAbsValue(XPathUtil.getString(adjustment, "Amount")));
				lineCharge.setAttribute("Reference", XPathUtil.getString(adjustment, "Description"));
			}
		} catch (Exception e) {
			throw AcademyPricingAndPromotionUtil.wrapToYFSException(e);
		}
	}
}