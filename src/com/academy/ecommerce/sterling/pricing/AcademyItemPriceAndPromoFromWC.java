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
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyItemPriceAndPromoFromWC implements YIFCustomApi {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyItemPriceAndPromoFromWC.class);
	
    public void setProperties(Properties props) {
    	
    }

	public Document getCreateWCOrderWithLinesRequest(YFSEnvironment env,
			Document inDoc) {
		try {
			log.beginTimer(" Begining of AcademyItemPriceAndPromoFromWC->getCreateWCOrderWithLinesRequest Api");
			log.verbose("Inside getCreateWCOrderWithLinesRequest with indoc " + XMLUtil.getXMLString(inDoc));
			Document itemPriceAndPromoReq = AcademyServiceUtil.getSOAPMsgTemplateForWCCreateOrderWithLines();
			itemPriceAndPromoReq = AcademyPricingAndPromotionUtil.setUserCredentialsForWCRequest(itemPriceAndPromoReq);
			Element itemEle = (Element)inDoc.getDocumentElement().getElementsByTagName("Item").item(0);
			String itemId = itemEle.getAttribute("ItemID");
			Element itemPriceAndPromoReqEle = itemPriceAndPromoReq.getDocumentElement();
			itemPriceAndPromoReqEle.getElementsByTagName("_wcf:PartNumber").item(0).setTextContent(itemId);
			
			Node orderItemShippingInfoEle = 
				itemPriceAndPromoReqEle.getElementsByTagName("_ord:OrderItemShippingInfo").item(0);
			orderItemShippingInfoEle.getParentNode().removeChild(orderItemShippingInfoEle);
			log.verbose("Inside getCreateWCOrderWithLinesRequest returning doc " + 
					XMLUtil.getXMLString(itemPriceAndPromoReq));
			log.endTimer(" End of AcademyItemPriceAndPromoFromWC->getCreateWCOrderWithLinesRequest Api");
			return itemPriceAndPromoReq;
		} catch (Exception e) {
			throw AcademyPricingAndPromotionUtil.wrapToYFSException(e);
		}
	}

	public Document getCreateGetWCOrderRequest(YFSEnvironment env,
			Document inDoc) {
		try {
			log.verbose("Inside createGetWCOrderRequest with indoc " + XMLUtil.getXMLString(inDoc));
			Element orderEle = (Element)inDoc.getDocumentElement().getElementsByTagName("_ord:OrderIdentifier").item(0);
			String orderNo = orderEle.getElementsByTagName("_wcf:UniqueID").item(0).getTextContent();
			Document outDoc = AcademyPricingAndPromotionUtil.createGetWCOrderRequest(orderNo);
			log.verbose("Inside createGetWCOrderRequest with outDoc " + XMLUtil.getXMLString(outDoc));
			return outDoc;
		} catch (Exception e) {
			throw AcademyPricingAndPromotionUtil.wrapToYFSException(e);
		}
	}
	
	
	public Document invokeCreateWCOrder(YFSEnvironment env, Document inDoc, boolean createWithLines) 
		throws Exception {
		log.verbose("Inside invokeCreateWCOrder with indoc " + XMLUtil.getXMLString(inDoc));
		Document wcCreateOrderRequestDoc = null;
		if(createWithLines) {
			wcCreateOrderRequestDoc = getCreateWCOrderWithLinesRequest(env, inDoc);
		}
		Document wcCreateOrderResponseDoc = AcademyUtil.invokeService(env, 
				"AcademyWCCreateGuestOrderService", wcCreateOrderRequestDoc);
		log.verbose("Inside invokeCreateWCOrder with response " + XMLUtil.getXMLString(wcCreateOrderResponseDoc));
		return wcCreateOrderResponseDoc;
	}
	
	public Document invokeGetWCOrder(YFSEnvironment env, Document inDoc) 
		throws Exception {
		log.verbose("Inside invokeGetWCOrder with indoc " + XMLUtil.getXMLString(inDoc));
		Document wcGetOrderRequestDoc = getCreateGetWCOrderRequest(env, inDoc);
		Document wcGetOrderResponseDoc = AcademyUtil.invokeService(env, "AcademyWCGetOrderService", wcGetOrderRequestDoc);
		log.verbose("Inside invokeGetWCOrder with response " + XMLUtil.getXMLString(wcGetOrderResponseDoc));
		return wcGetOrderResponseDoc;
	}

	
	/**
	 * Read the response from WC and construct output for YFSGetPromotionsForItemListUE
	 * <ItemList>
	 * 		<Item ItemID="" ItemKey="" OrganizationCode="" UnitOfMeasure="">
	 * 			<PromotionList>
	 * 				<Promotion LongDescription="" ShortDescription="" URL=""/>
	 * 			</PromotionList>
	 * 		</Item>
	 * </ItemList>
	 * @param env
	 * @param inDoc
	 * @return
	 */
	public Document convertWCGetOrderToPromoUEOutPut(YFSEnvironment env, Document inDoc) {
		try {
			log.verbose("Inside convertWCGetOrderToPromoUEOutPut with indoc " + XMLUtil.getXMLString(inDoc));
			YFCDocument itemPromoUEOutput = YFCDocument.createDocument("ItemList");
			YFCElement responseEle = itemPromoUEOutput.getDocumentElement();
			NodeList orderLinelist = inDoc.getDocumentElement().getElementsByTagName("_ord:OrderItem");
			log.verbose("Inside convertWCGetOrderToPromoUEOutPut No of OrderLines " + orderLinelist.getLength());
			Document completeItemListInDoc = (Document)AcademyUtil.getContextObject(env, 
					"AcademyGetCompleteItemListInDoc");
			for(int i = 0; i < orderLinelist.getLength(); i++) {
				Element itemFromWc = (Element)orderLinelist.item(i);
				YFCElement item = responseEle.createChild("Item");
				item.setAttribute("ItemID", itemFromWc.getElementsByTagName("_wcf:PartNumber").item(0).getTextContent());
				//TODO get the proper item element and then do this
				item.setAttribute("ItemKey", XPathUtil.getString(completeItemListInDoc, 
						"ItemList/Item/@ItemKey"));
				item.setAttribute("OrganizationCode", XPathUtil.getString(completeItemListInDoc, 
						"ItemList/Item/@OrganizationCode"));
				item.setAttribute("UnitOfMeasure", XPathUtil.getString(completeItemListInDoc, 
						"ItemList/Item/@UnitOfMeasure"));
				
				YFCElement itemPromoListEle = item.createChild("PromotionList");
				NodeList promoList = itemFromWc.getElementsByTagName("_wcf:UserDataField");
				log.verbose("Inside convertWCGetOrderToPromoUEOutPut No of Promo " + promoList.getLength());
				for(int j = 0; j < promoList.getLength(); j++) {
					Element itemPromo = (Element)promoList.item(j);
					if("eligible".equalsIgnoreCase(itemPromo.getAttribute("name"))) {
						YFCElement itemPromoEle = itemPromoListEle.createChild("Promotion");
						itemPromoEle.setAttribute("LongDescription", itemPromo.getTextContent());
						itemPromoEle.setAttribute("ShortDescription", "");
						itemPromoEle.setAttribute("URL", "www.academy.com");
					}
				}
			}
			log.verbose("Inside convertWCGetOrderToPromoUEOutPut with outDoc " + XMLUtil.getXMLString(itemPromoUEOutput.getDocument()));
			return itemPromoUEOutput.getDocument();
		} catch (Exception e) {
			log.verbose("Exception inside convertWCGetOrderToPromoUEOutPut " + e.getMessage());
			throw AcademyPricingAndPromotionUtil.wrapToYFSException(e);
		}
	}
	
	/**
	 * Read the response from WC and construct output for YFSGetPromotionsForItemListUE
	 * <ItemList>
	 * 		<Item ItemID="" ItemKey="" OrganizationCode="" UnitOfMeasure="" WebsiteDisplayURL="">
	 * 			<ComputedPrice ListPrice="" RetailPrice="" UnitPrice="" />
	 * 		</Item>
	 * </ItemList>
	 * @param env
	 * @param inDoc
	 * @return
	 */
	public Document convertWCGetOrderToPricingUEOutPut(YFSEnvironment env, Document inDoc) {
		try {
			log.verbose("Inside convertWCGetOrderToPricingUEOutPut with indoc " + XMLUtil.getXMLString(inDoc));
			YFCDocument itemPriceUEOutput = YFCDocument.createDocument("ItemList");
			YFCElement responseEle = itemPriceUEOutput.getDocumentElement();
			NodeList orderLinelist = inDoc.getDocumentElement().getElementsByTagName("_ord:OrderItem");
			Document completeItemListInDoc = (Document)AcademyUtil.getContextObject(env, 
				"AcademyGetCompleteItemListInDoc");			
			for(int i = 0; i < orderLinelist.getLength(); i++) {
				Element itemFromWc = (Element)orderLinelist.item(i);
				YFCElement item = responseEle.createChild("Item");
				item.setAttribute("ItemID", itemFromWc.getElementsByTagName("_wcf:PartNumber").item(0).getTextContent()); 
				item.setAttribute("ItemKey", XPathUtil.getString(completeItemListInDoc, "ItemList/Item/@ItemKey"));
				item.setAttribute("OrganizationCode", XPathUtil.getString(completeItemListInDoc, "ItemList/Item/@OrganizationCode"));
				item.setAttribute("UnitOfMeasure", XPathUtil.getString(completeItemListInDoc, "ItemList/Item/@UnitOfMeasure"));
				item.setAttribute("WebsiteDisplayURL", "www.academy.com");

				YFCElement itemPrice =  item.createChild("ComputedPrice");
				
				String listPrice = "0.00";
				NodeList userDataList = itemFromWc.getElementsByTagName("_wcf:UserDataField");
				for(int j = 0; j < userDataList.getLength(); j++) {
					if("EDLP".equalsIgnoreCase(((Element)userDataList.item(j)).getAttribute("name"))) {
						listPrice = userDataList.item(j).getTextContent();
						break;
					}
				}
				// Mapped to EDLP
				itemPrice.setAttribute("ListPrice", listPrice);

				//Mapped to Unit price 
				itemPrice.setAttribute("UnitPrice", itemFromWc.getElementsByTagName("_wcf:Price").item(0).getTextContent());
				itemPrice.setAttribute("RetailPrice", itemFromWc.getElementsByTagName("_wcf:Price").item(0).getTextContent());
			}
			log.verbose("Inside convertWCGetOrderToPricingUEOutPut with response " + 
					XMLUtil.getXMLString(itemPriceUEOutput.getDocument()));
			return itemPriceUEOutput.getDocument();
		} catch (Exception e) {
			log.verbose("Exception inside convertWCGetOrderToPricingUEOutPut " + e.getMessage());
			throw AcademyPricingAndPromotionUtil.wrapToYFSException(e);
		}
	}
	
	public void setPricingUEOutPutToEnv(YFSEnvironment env,	Document inDoc) throws Exception {
		log.verbose("Inside setPricingUEOutPutToEnv with indoc " + XMLUtil.getXMLString(inDoc));
		Document pricingUEOutPut = convertWCGetOrderToPricingUEOutPut(env, inDoc);
		Document cloneDoc = (Document)pricingUEOutPut.cloneNode(true);
		log.verbose("Setting env 'ItemPricing' with cloneDoc " + XMLUtil.getXMLString(cloneDoc));
        AcademyUtil.setContextObject(env, "ItemPricing", cloneDoc);
	}
	
	
	/**
	 * Input is from YFSGetPromotionsForItemListUE and out put should be the 
	 * YFSGetPromotionsForItemListUE output
	 * @param env
	 * @param inDoc
	 * @return
	 */
	public Document getItemPriceAndPromoFromWC(YFSEnvironment env,
			Document inDoc) throws Exception {
		try {
			log.verbose("Inside getItemPriceAndPromoFromWC with indoc " + XMLUtil.getXMLString(inDoc));
			Document cloneInDoc = (Document)inDoc.cloneNode(true);
			AcademyUtil.setContextObject(env, "AcademyGetCompleteItemListInDoc", cloneInDoc);
			Document createOrderResponse = invokeCreateWCOrder(env, inDoc, true);
			AcademyPricingAndPromotionUtil.hasError(createOrderResponse);
			Document getOrderResponse = invokeGetWCOrder(env, createOrderResponse);
			setPricingUEOutPutToEnv(env, getOrderResponse);
			Document priceAndPromoDoc = convertWCGetOrderToPromoUEOutPut(env, getOrderResponse);
			log.verbose("Inside getItemPriceAndPromoFromWC with outdoc " + XMLUtil.getXMLString(priceAndPromoDoc));
			return priceAndPromoDoc;
		} catch (YFSException e) {
			log.verbose("YFSException inside getItemPriceAndPromoFromWC " + e.getMessage());
			throw e;
		} catch (Exception e) {
			log.verbose("Exception inside getItemPriceAndPromoFromWC " + e.getMessage());
			throw AcademyPricingAndPromotionUtil.wrapToYFSException(e);
		}
	}
}
