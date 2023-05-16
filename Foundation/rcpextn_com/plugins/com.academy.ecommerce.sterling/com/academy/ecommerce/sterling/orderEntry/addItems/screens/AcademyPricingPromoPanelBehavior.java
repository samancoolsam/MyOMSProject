package com.academy.ecommerce.sterling.orderEntry.addItems.screens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.yantra.yfc.rcp.YRCBehavior;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.academy.ecommerce.sterling.changeFulfillmentOptions.extn.AcademyChangeFulfillmentOptionsExtnBehavior;
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.academy.ecommerce.sterling.util.XPathUtil;

public class AcademyPricingPromoPanelBehavior extends YRCBehavior {

	private AcademyPricingPromoPanel view = null;
	private Element eleUpdateOrderDetails = null;
	private YRCExtentionBehavior behavior;

	public AcademyPricingPromoPanelBehavior(Composite ownerComposite,
			Composite parent, String formId, YRCExtentionBehavior behavior) {
		super(ownerComposite, formId);
		this.view = (AcademyPricingPromoPanel) ownerComposite;
		this.behavior=behavior;
	}

	public Element getModel() {
		return eleUpdateOrderDetails;

	}

	/*
	 * This method is called from add item screen after obtaining the instance
	 * of the promotion panel
	 */

	public void callExternalPanelBehaviorFromAddItemScreen(
			Element eleOrderDetails) {
		YRCPlatformUI
				.trace(
						"#########Order Details on instantiating from Add Item Screen###########",
						eleOrderDetails);
		setModel(AcademyPCAConstants.ATTR_EXTN_ORDER_DETAILS, eleOrderDetails);
		createLabels(eleOrderDetails);
	}

	public void callExternalPanelFromCFOScreen(Element eleOrderDetails) {
		YRCPlatformUI
				.trace(
						"#########Order Details on instantiating from Change Fulfillment Options Screen###########",
						eleOrderDetails);
		setModel(AcademyPCAConstants.ATTR_EXTN_ORDER_DETAILS_FOR_CFO,
				eleOrderDetails);
		createLabels(eleOrderDetails);
	}

	private void createLabels(Element eleOrderDetails) {
		YRCPlatformUI
		.trace(
				"#####Inside the create label method of promotions panel for Change Fulfillment Options Screen#########",
				eleOrderDetails);
		try {
			Element eleOrderLines = (Element) XPathUtil.getNode(eleOrderDetails,"OrderLines");
			if(!YRCPlatformUI.isVoid(eleOrderLines)){
				NodeList nOrderLine =eleOrderLines.getElementsByTagName("OrderLine");
				Map<String, List<String>> promoMap = new HashMap<String, List<String>>();
				for(int i=0;i<nOrderLine.getLength();i++){
					Element eleOrderLine = (Element)nOrderLine.item(i);
					Element eleItem= (Element)XPathUtil.getNode(eleOrderLine, "Item");
					String strItemID = eleItem.getAttribute("ItemID");
					List itemPromoList = promoMap.get(strItemID);
					if(YRCPlatformUI.isVoid(itemPromoList)) {
						itemPromoList = new ArrayList<String>();
					}
					Element eleAwardsElem = (Element) XPathUtil.getNode(eleOrderLine, "Awards");
					if (!YRCPlatformUI.isVoid(eleAwardsElem)) {
						NodeList nAward = eleAwardsElem
						.getElementsByTagName(AcademyPCAConstants.ATTR_AWARD);
						for (int j = 0; j < nAward.getLength(); j++) {
							Element eleAward =(Element)nAward.item(j);
							String strAwardApplied = eleAward.getAttribute("AwardApplied");
							if((behavior instanceof AcademyChangeFulfillmentOptionsExtnBehavior) && ("Y".equals(strAwardApplied))){
								String strDescription = eleAward.getAttribute(AcademyPCAConstants.ATTR_DESC);
								if(!itemPromoList.contains(strDescription)) {
									itemPromoList.add(strDescription);
								}
							}else{
								String strDescription = eleAward.getAttribute(AcademyPCAConstants.ATTR_DESC);
								if(!itemPromoList.contains(strDescription)) {
									itemPromoList.add(strDescription);
								}
							}
						}
						promoMap.put(strItemID, itemPromoList);
					}
				} 
				Iterator<String> itemIdItr = promoMap.keySet().iterator();
				while(itemIdItr.hasNext()) {
					String itemID = itemIdItr.next();
					List<String> itemPromoList = promoMap.get(itemID);
					for(int i = 0; i < itemPromoList.size(); i++) {
						view.createLabelForPromotions(1, itemPromoList.get(i), itemID);
					}
				}
			}
		}
			catch (Exception e) {
			e.printStackTrace();
		}
	}

}
