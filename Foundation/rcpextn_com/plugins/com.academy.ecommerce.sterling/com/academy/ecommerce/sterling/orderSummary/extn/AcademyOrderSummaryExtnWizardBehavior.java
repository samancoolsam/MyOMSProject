package com.academy.ecommerce.sterling.orderSummary.extn;

/**
 * Created on May 10,2009
 *
 */
//Java Imports
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.w3c.dom.Element;

//Sterling Imports
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCDialog;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCWizardExtensionBehavior;
import com.yantra.yfc.rcp.YRCXmlUtils;

//Project Imports
import com.academy.ecommerce.sterling.orderSummary.screens.AcademyTaxIdPopUp;
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;

//Misc Imports -NONE

/**
 * @author sahmed Copyright © 2005-2008 Sterling Commerce, Inc. All Rights
 *         Reserved.
 */
public class AcademyOrderSummaryExtnWizardBehavior extends
		YRCWizardExtensionBehavior {

	/**
	 * This method initializes the behavior class.
	 */
	public void init() {
	}

	public String getExtnNextPage(String currentPageId) {
		return null;
	}

	public IYRCComposite createPage(String pageIdToBeShown) {
		return null;
	}

	public void pageBeingDisposed(String pageToBeDisposed) {
	}

	/**
	 * Called when a wizard page is about to be shown for the first time.
	 * 
	 */
	public void initPage(String pageBeingShown) {
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
		super.validateComboField(fieldName, fieldValue);
	}

	/**
	 * Method called when a button is clicked.
	 */
	public YRCValidationResponse validateButtonClick(String fieldName) {
		return super.validateButtonClick(fieldName);
	}

	/**
	 * Method called when a link is clicked.
	 */
	public YRCValidationResponse validateLinkClick(String fieldName) {
		if (fieldName.equals(AcademyPCAConstants.ATTR_TAX_EXEMPTID)) {
			Element eleOrderDetails = getModel(AcademyPCAConstants.ATTR_ORDER_DETAILS);
			YRCPlatformUI.trace("Checking Model Data"
					+ YRCXmlUtils.getString(eleOrderDetails));
			String sTaxExemptionCertificate = eleOrderDetails
					.getAttribute("TaxExemptionCertificate");
			String sOrderHeaderKey = eleOrderDetails
					.getAttribute(AcademyPCAConstants.ATTR_ORDER_HEADER_KEY);
			YRCPlatformUI.trace("###### Fetch the Tax Exempt ID ####"
					+ YRCXmlUtils.getAttribute(eleOrderDetails,
							sTaxExemptionCertificate));
			AcademyTaxIdPopUp AcademyTaxIdPopUp = new AcademyTaxIdPopUp(
					YRCPlatformUI.getShell(), SWT.NONE,
					sTaxExemptionCertificate, sOrderHeaderKey, true);
			YRCDialog dialog = new YRCDialog(AcademyTaxIdPopUp, 190, 110,
					"TAX-ID", "Tax Id");
			dialog.open();
		}
		return super.validateLinkClick(fieldName);
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
//TODO Validation required for a Link control: extn_LnkTaxExemptID