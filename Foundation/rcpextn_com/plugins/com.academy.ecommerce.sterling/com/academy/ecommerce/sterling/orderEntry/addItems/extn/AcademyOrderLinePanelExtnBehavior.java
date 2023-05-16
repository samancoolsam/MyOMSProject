package com.academy.ecommerce.sterling.orderEntry.addItems.extn;

/**
 * Created on May 14,2009
 *
 */

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyConstants;
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.academy.ecommerce.sterling.util.XMLUtil;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCTextBindingData;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCXPathUtils;
import com.yantra.yfc.rcp.YRCXmlUtils;

/**
 * @author sahmed Copyright © 2005-2008 Sterling Commerce, Inc. All Rights
 *         Reserved.
 */
public class AcademyOrderLinePanelExtnBehavior extends YRCExtentionBehavior {

	/**
	 * Variable to store Product Available Date passed in input
	 */

	public Element eleFulfillmentOptions = null;

	/**
	 * This method initializes the behavior class.
	 */
	public void init() {
		Document docItemListModel = YRCXmlUtils.createDocument("ItemList");
		setExtentionModel("ExtnItemList", docItemListModel.getDocumentElement());
		setControlVisible("extn_lbl_OverridePrice",false );
		setControlVisible("extn_txt_OverridePrice",false );
	}

	/**
	 * Method for validating the text box.
	 */
	public YRCValidationResponse validateTextField(String fieldName,
			String fieldValue) {
		// TODO Validation required for the following controls.

		// TODO Create and return a response.
		return super.validateTextField(fieldName, fieldValue);
	}

	/**
	 * Method for validating the combo box entry.
	 */
	public void validateComboField(String fieldName, String fieldValue) {
		// TODO Validation required for the following controls.

		// TODO Create and return a response.
		super.validateComboField(fieldName, fieldValue);
	}

	/**
	 * Method called when a button is clicked.
	 */
	public YRCValidationResponse validateButtonClick(String fieldName) {
		// TODO Validation required for the following controls.
		// TODO Create and return a response.
		return super.validateButtonClick(fieldName);
	}

	/**
	 * Method called when a link is clicked.
	 */
	public YRCValidationResponse validateLinkClick(String fieldName) {
		// TODO Validation required for the following controls.

		// TODO Create and return a response.
		return super.validateLinkClick(fieldName);
	}

	/**
	 * Create and return the binding data for advanced table columns added to
	 * the tables.
	 */
	public YRCExtendedTableBindingData getExtendedTableBindingData(
			String tableName, ArrayList tableColumnNames) {
		// Create and return the binding data definition for the table.

		// The defualt super implementation does nothing.
		return super.getExtendedTableBindingData(tableName, tableColumnNames);
	}

	public void postSetModel(String strModel) {

		if (strModel.equals(AcademyPCAConstants.ATTR_ITEM_DETAILS)) {
			{
				registerStaticTargetBinding(
						AcademyPCAConstants.XPATH_ORDERLINE_EXTN_TARGET_SHIP_ALONE,
						AcademyPCAConstants.XPATH_ITEM_EXTN_SOURCE_SHIP_ALONE);
			}
			
			/**
			 * Added as part of DD140
			 */
			Element itemDetails=getModel("ItemDetails");
			hideLabelAndTextBoxForNonGCItems(itemDetails);

		}
		super.postSetModel(strModel);
	}

	public void postCommand(YRCApiContext ctx) {
		String[] strApiNames = ctx.getApiNames();
		boolean bUseStreetDate = false;
		for (int i = 0; i < strApiNames.length; i++) {
			YRCPlatformUI.trace("##### Inside Post Command #####"
					+ strApiNames[i]);
			

			if ((strApiNames[i]
					.equals(AcademyPCAConstants.API_GET_ITEMLIST_WITHOUT_PRICE))
					|| (strApiNames[i]
							.equals(AcademyPCAConstants.API_GET_ITEMLIST_WITH_PRICE))) {
				Element eleExtnItemList = getExtentionModel("ExtnItemList");
				if(!YRCPlatformUI.isVoid(eleExtnItemList)){
				if (!YRCPlatformUI.isVoid(strApiNames[i])) {
					Element eleItemList = ctx.getOutputXmls()[i]
							.getDocumentElement();
					if (!YRCPlatformUI.isVoid(eleItemList)){
					Element eleItem = YRCXmlUtils.getChildElement(eleItemList,
							AcademyPCAConstants.ATTR_ITEM);
					if (!YRCPlatformUI.isVoid(eleItem)){
					Element eleExtn = YRCXmlUtils.getChildElement(eleItem,
							AcademyPCAConstants.ATTR_EXTN);
					String strExtnStreetDate = eleExtn
							.getAttribute(AcademyPCAConstants.ATTR_EXTN_STREET_DATE);
					Element eleItemExtn = YRCXmlUtils.createChild(
							eleExtnItemList, AcademyPCAConstants.ATTR_ITEM);
					eleItemExtn.setAttribute(
							AcademyPCAConstants.ATTR_EXTN_STREET_DATE,
							strExtnStreetDate);
					eleItemExtn
							.setAttribute(
									AcademyPCAConstants.ATTR_ITEM_ID,
									eleItem
											.getAttribute(AcademyPCAConstants.ATTR_ITEM_ID));
					YRCPlatformUI.trace("##### Inside Post Command #####");
					setExtentionModel(AcademyPCAConstants.ATTR_EXTN_ITEM_LIST,
							eleExtnItemList);
					}
					}
				}
			}
			}

			if (strApiNames[i]
					.equals(AcademyPCAConstants.API_GET_FULFILLMENT_OPTIONS_FOR_LINES)) {
				YRCPlatformUI.trace("##### Inside Post Command #####"
						+ strApiNames[i]);
				Element eleExtnFulfillment = ctx.getOutputXmls()[i]
						.getDocumentElement();
				YRCPlatformUI.trace(
						"#######Fulfillment option extension model ####",
						YRCXmlUtils.getString(eleExtnFulfillment));
				NodeList nlAssignment = eleExtnFulfillment
						.getElementsByTagName(AcademyPCAConstants.ATTR_ASSIGNMENT);
				for (int j = 0; j < nlAssignment.getLength(); j++) {
					Element eleAssignment = (Element) nlAssignment.item(j);
					String strShipDate = eleAssignment
							.getAttribute(AcademyPCAConstants.ATTR_SHIP_DATE);
					Element elePromiseLine = (Element) eleAssignment
							.getParentNode().getParentNode().getParentNode()
							.getParentNode();
					String strItemID = elePromiseLine
							.getAttribute(AcademyPCAConstants.ATTR_ITEM_ID);
					Element eleExtnItemList = getExtentionModel(AcademyPCAConstants.ATTR_EXTN_ITEM_LIST);
					Iterator itrItem = YRCXmlUtils.getChildren(eleExtnItemList);
					while (itrItem.hasNext()) {
						Element eleItemTemp = (Element) itrItem.next();
						String strItemIDTemp = eleItemTemp
								.getAttribute(AcademyPCAConstants.ATTR_ITEM_ID);
						if (strItemIDTemp.equals(strItemID)) {
							String strExtnStreetDate = eleItemTemp
									.getAttribute(AcademyPCAConstants.ATTR_EXTN_STREET_DATE);
							bUseStreetDate = checkForActualProductAvailableDate(
									strShipDate, strExtnStreetDate);
							if (bUseStreetDate) {
								eleAssignment.setAttribute(
										AcademyPCAConstants.ATTR_SHIP_DATE,
										strExtnStreetDate);
								eleAssignment.setAttribute(AcademyPCAConstants.ATTR_PROD_AVAIL_DATE,strExtnStreetDate);
								Element eleOption = (Element) eleAssignment.getParentNode().getParentNode();
								eleOption.setAttribute(AcademyPCAConstants.ATTR_PROD_AVAILABLE_DATE,strExtnStreetDate);
								eleOption.setAttribute(AcademyPCAConstants.ATTR_DELIVERY_DATE,strExtnStreetDate);
								YRCPlatformUI.trace(
										"#######Fulfillment option extension model ####",
										YRCXmlUtils.getString(eleExtnFulfillment));
								this
										.setExtentionModel(
												AcademyPCAConstants.ATTR_EXT_FINAL_SHIP_DATE,
												eleAssignment);
								registerStaticTargetBinding(
										AcademyPCAConstants.XPATH_ORDERLINE_REQ_SHIP_DATE,
										AcademyPCAConstants.XPATH_EXTN_SHIP_DATE);
								registerStaticTargetBinding(AcademyPCAConstants.XPATH_ORDERLINE_REQ_DEL_DATE,AcademyPCAConstants.XPATH_EXTN_SHIP_DATE);
							} else {
								eleAssignment.setAttribute(
										AcademyPCAConstants.ATTR_SHIP_DATE,
										strShipDate);
								this
										.setExtentionModel(
												AcademyPCAConstants.ATTR_EXT_FINAL_SHIP_DATE,
												eleAssignment);
								registerStaticTargetBinding(
										AcademyPCAConstants.XPATH_ORDERLINE_REQ_SHIP_DATE,
										AcademyPCAConstants.XPATH_EXTN_SHIP_DATE);
							}
						}
					}
				}
			}
		}
		super.postCommand(ctx);
	}
	private boolean checkForActualProductAvailableDate(String strShipDate,
			String strExtnStreetDate) {

		try {
			DateFormat df = new SimpleDateFormat(
					AcademyConstants.STR_DATE_TIME_PATTERN);
			strExtnStreetDate+="T00:00:00-04:00";
			Date dtExtnStreetDate = df.parse(strExtnStreetDate);
			Date dtProductAvaiableDate = df.parse(strShipDate);
			if (dtExtnStreetDate.after(dtProductAvaiableDate)) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return isInitPageInProgress;
	}
	/**
	 * Written as part of CR for Call Center UI
	 * @param ele
	 * ExtnIsGiftCard="Y" 
	 */
	
	private void  hideLabelAndTextBoxForNonGCItems( Element ele){
		Element Itemextn=YRCXmlUtils.getChildElement(ele, "Extn");
		String giftCardFlag=YRCXmlUtils.getAttribute(Itemextn, "ExtnIsGiftCard");
		/**
		 * BUG Fix :- 9206 
		 * <OrderLine>
    			<Item ItemID="GC-ITEM-005">
        			<Extn ExtnShipAlone="N"/>
    			</Item>
    		<OrderLineTranQuantity OrderedQty="1.0" TransactionalUOM="EACH"/>
    			<LinePriceInfo UnitPrice="OVERRIDEN PRICE" IsPriceLocked="Y"/>
		 	</OrderLine>
		 */
		if(AcademyPCAConstants.ATTR_Y.equals(giftCardFlag)){
			setControlVisible("extn_lbl_OverridePrice",true );
			setControlVisible("extn_txt_OverridePrice",true);
			Element orderLine=getTargetModel("OrderLine");
			/**
			 * Making Is Price Locked as Y at the LinePriceInfo level.
			 * New Price will be stamped as UnitPrice="" at this very level.
			 */
			Element linePriceInfo=YRCXmlUtils.getChildElement(orderLine, "LinePriceInfo");
			linePriceInfo.setAttribute("IsPriceLocked", "Y");
				
			
		}
				
	}

}