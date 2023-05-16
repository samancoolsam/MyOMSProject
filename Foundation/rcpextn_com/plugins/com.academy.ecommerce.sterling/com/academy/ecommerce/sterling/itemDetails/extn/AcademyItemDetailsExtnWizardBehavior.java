package com.academy.ecommerce.sterling.itemDetails.extn;

/**
 * Created on May 10,2009
 *
 */
//Java Imports
import java.util.ArrayList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//Sterling Imports
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCDialog;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCWizardExtensionBehavior;
import com.yantra.yfc.rcp.YRCXmlUtils;

//Project Imports
import com.academy.ecommerce.sterling.itemDetails.screens.AcademyItemPopUp;
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.academy.ecommerce.sterling.util.XPathUtil;

//Misc Imports -NONE
/**
 * @author sahmed Copyright © 2005-2008 Sterling Commerce, Inc. All Rights
 *         Reserved.
 */
public class AcademyItemDetailsExtnWizardBehavior extends
		YRCWizardExtensionBehavior {

	/**
	 * This method initializes the behavior class.
	 */
	public void init() {
          
		super.init();

		// TODO: Write behavior init here.
	}

	public String getExtnNextPage(String currentPageId) {
		// TODO
		return null;
	}

	public IYRCComposite createPage(String pageIdToBeShown) {
		// TODO
		return null;
	}

	public void pageBeingDisposed(String pageToBeDisposed) {
		// TODO
	}

	/**
	 * Called when a wizard page is about to be shown for the first time.
	 * 
	 */
	public void initPage(String pageBeingShown) {
		// TODO
	}

	/**
	 * Method for validating the text box.
	 */
	public YRCValidationResponse validateTextField(String fieldName,
			String fieldValue) {
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
		if ((fieldName.equals("buttonBackToSearch"))
				|| (fieldName.equals("buttonMoreDetails"))
				|| (fieldName.equals("buttonCreateOrder"))
				|| (fieldName.equals("buttonAddToOrder")))
			;
		// onLoadFlag=true;
		// TODO Create and return a response.
		return super.validateButtonClick(fieldName);
	}

	/**
	 * This method is called when the extn_LnkWebItemDetails link is clicked.
	 * Academy would display the website item details for the item selected. For
	 * now the www.Academy.com is accessed on click of this link.
	 */
	public YRCValidationResponse validateLinkClick(String fieldName) {
		// TODO Validation required for the following controls.
		if (fieldName.equals(AcademyPCAConstants.ATTR_WEB_ITEM_DETAILS)) {
			Element eleItemDetails = getModel("Results");
			try {
				String strWebsireURL = XPathUtil.getString(eleItemDetails, "ComputedPrice/QuantityRangePriceList/QuantityRangePrice/Region/@RegionDescription");
				Display display = Display.getDefault();
				AcademyItemPopUp AcademyItemPopUp = new AcademyItemPopUp(display
						.getActiveShell(), SWT.NONE,strWebsireURL, true);
				YRCDialog dialog = new YRCDialog(AcademyItemPopUp, 638, 400,
						AcademyPCAConstants.ATTR_ACADEMY, AcademyPCAConstants.ATTR_WEBSITE_ACCESS);
				dialog.open();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		// Control name: extn_lnkWebItemDetails

		// TODO Create and return a response.
		return super.validateLinkClick(fieldName);
	}

	/*
	 * the postSetModel method is implemented to check if the item is a store
	 * only item. If true, it would display "this is a store only item" label on
	 * the screen. It also checks if an item has a sale price(unit price)is
	 * zero/blank, if true it would hide the Every Day Low Price(List Price) and
	 * End Date for the item
	 */

	@Override
	public void postSetModel(String namespace) {

		if (namespace.equals(AcademyPCAConstants.ATTR_RESULTS))
			try {
				Element ItemEle = getModel("Results");
				YRCPlatformUI.trace("Checking Model Data"
						+ YRCXmlUtils.getString(ItemEle));
				if ("".equals(getFieldValue(AcademyPCAConstants.ATTR_LBL_UNIT_PRICE))) {
					setControlVisible(AcademyPCAConstants.ATTR_LBL_LIST_PRICE,
							false);
					setControlVisible(AcademyPCAConstants.ATTR_LABEL_LIST_PRICE,
							false);
					setControlVisible(AcademyPCAConstants.ATTR_LBL_END_DATE, false);
					setControlVisible(AcademyPCAConstants.ATTR_END_DATE, false);
				} else {
					setControlVisible(AcademyPCAConstants.ATTR_LBL_LIST_PRICE, true);
					setControlVisible(AcademyPCAConstants.ATTR_LABEL_LIST_PRICE,
							true);
					setControlVisible(AcademyPCAConstants.ATTR_LBL_END_DATE, true);
					setControlVisible(AcademyPCAConstants.ATTR_END_DATE, true);
				}
						
				NodeList nodeList = XPathUtil.getNodeList(ItemEle,
						AcademyPCAConstants.ATTR_EXTN);
				for (int i = 0; i < nodeList.getLength(); i++) {
					// navigating through each element in the item details
					Element rItemDetailsNode = (Element) nodeList.item(i);
					String extnECommerce = YRCXmlUtils.getAttribute(rItemDetailsNode,AcademyPCAConstants.ATTR_ECOMMERCE);
					if (AcademyPCAConstants.ATTR_EXTN_ECOMMERCE_VALUE
							.equals(extnECommerce)) 
					
					{
						setControlVisible(
								AcademyPCAConstants.ATTR_STORE_ONLY_ITEM, true);
					} else {
						setControlVisible(
								AcademyPCAConstants.ATTR_STORE_ONLY_ITEM, false);
					}
				}
			} catch (Exception e) {
				if (YRCPlatformUI.isTraceEnabled()) {
					YRCPlatformUI.trace(e);
				}
			}

		super.postSetModel(namespace);
	}

	/**
	 * Create and return the binding data for advanced table columns added to
	 * the tables.
	 */
	public YRCExtendedTableBindingData getExtendedTableBindingData(
			String tableName, ArrayList tableColumnNames) {
		return super.getExtendedTableBindingData(tableName, tableColumnNames);
	}
}
//TODO Validation required for a Link control: extn_LnkWebItemDetails
//TODO Validation required for a Text control: extn_TEndDate
//TODO Validation required for a Button control: buttonMoreDetails
//TODO Validation required for a Text control: extn_TShippingInstruction
//TODO Validation required for a Button control: buttonAddToOrder
//TODO Validation required for a Text control: extn_TCountryOfOrigin
//TODO Validation required for a Text control: extn_TQuantityRestrictions
//TODO Validation required for a Link control: lnkStyleSize
//TODO Validation required for a Button control: buttonBackToSearch
//TODO Validation required for a Text control: extn_TColorName
//TODO Validation required for a Text control: extn_LabelItemavailabletobeshippedfrom
//TODO Validation required for a Button control: buttonCreateOrder
//TODO Validation required for a Text control: extn_THazmat