package com.academy.ecommerce.sterling.orderEntry.createCustomer.extn;

/**
 * Created on May 10,2009
 *
 */

//Java Imports
import java.util.ArrayList;

//Sterling Imports
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCWizardExtensionBehavior;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;

//Project Imports -NONE

//Misc Imports - NONE
/**
 * @author sahmed Copyright © 2005-2008 Sterling Commerce, Inc. All Rights
 *         Reserved.
 */
public class AcademyCreateCustomerContactWizardBehavior extends
		YRCWizardExtensionBehavior {

	private String sBusinessCustomerName = null;

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


		if (fieldName.equals(AcademyPCAConstants.ATTR_CORPORATE_CUSTOMER)) {
			if (AcademyPCAConstants.ATTR_Y
					.equals(getFieldValue(AcademyPCAConstants.ATTR_CORPORATE_CUSTOMER))) {
				YRCPlatformUI.trace("Check for Button Click: " + fieldName);
				enableField(AcademyPCAConstants.ATTR_COMPANY);
				disableField(AcademyPCAConstants.ATTR_REQURIES_WEBPROFILEID);
			} else {
				sBusinessCustomerName = getFieldValue(AcademyPCAConstants.ATTR_COMPANY);
				enableField(AcademyPCAConstants.ATTR_REQURIES_WEBPROFILEID);
			}
		} else {
			if (AcademyPCAConstants.ATTR_Y
					.equals(getFieldValue(AcademyPCAConstants.ATTR_REQURIES_WEBPROFILEID))) {
				sBusinessCustomerName = getFieldValue(AcademyPCAConstants.ATTR_COMPANY);
				disableField(AcademyPCAConstants.ATTR_CORPORATE_CUSTOMER);
				disableField(AcademyPCAConstants.ATTR_COMPANY);
				// setFieldValue("extn_chkCorporateCustomer", "");
				setFieldValue(AcademyPCAConstants.ATTR_COMPANY, "");
			} else {
				enableField(AcademyPCAConstants.ATTR_CORPORATE_CUSTOMER);
				enableField(AcademyPCAConstants.ATTR_COMPANY);
				setFieldValue(AcademyPCAConstants.ATTR_CORPORATE_CUSTOMER,
						sBusinessCustomerName);
			}
		}
		return super.validateButtonClick(fieldName);

	}

	/**
	 * Method called when a link is clicked.
	 */
	public YRCValidationResponse validateLinkClick(String fieldName) {
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
//TODO Validation required for a Check control: extn_ChkRequiresWebprofileID
//TODO Validation required for a Check control: extn_ChkOptIn
//TODO Validation required for a Text control: extn_TCompany
//TODO Validation required for a Check control: extn_ChkCorporateCustomer
//TODO Validation required for a Combo control: extn_CmbTitle