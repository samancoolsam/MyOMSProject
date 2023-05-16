package com.academy.ecommerce.sterling.paymentInquiry.extn;

/**
 * Created on May 10,2009
 *
 */
//Java Imports
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Element;

//Sterling Imports
import com.yantra.yfc.rcp.YRCDialog;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCWizardExtensionBehavior;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCXmlUtils;

//Project Imports
import com.academy.ecommerce.sterling.paymentInquiry.screens.PaymentResourcePopUp;
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.academy.ecommerce.sterling.util.ResourceUtil;

//Misc Imports -NONE
/**
 * @author sahmed Copyright © 2005-2008 Sterling Commerce, Inc. All Rights
 *         Reserved.
 */
public class AcademyPaymentInquiryExtnWizardBehavior extends
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
		String strURL=null;
		if (fieldName.equals(AcademyPCAConstants.ATTR_PAYMENT_DETAILS)) {
			// Retrieve the Model
			Element eleOrderDetails = getModel(AcademyPCAConstants.ATTR_ORDER_COLLECTION_DETAILS);
			YRCPlatformUI.trace("#########Checking Model Data############"
					+ YRCXmlUtils.getString(eleOrderDetails));
			// Get the orderheaderkey from the model above
			String sorderHeaderKey = eleOrderDetails
					.getAttribute(AcademyPCAConstants.ATTR_ORDER_HEADER_KEY);
			YRCPlatformUI.trace("###### Get the Order Header Key#############"
					+ YRCXmlUtils
							.getAttribute(eleOrderDetails, sorderHeaderKey));
			strURL= ResourceUtil.get("CONSOLE_PAYMENT_DETAILS_AUTHORIZATION_URL1")+ sorderHeaderKey + ResourceUtil.get("CONSOLE_PAYMENT_DETAILS_AUTHORIZATION_URL2");
			Display display = Display.getDefault();
			// open sterling console for payment details
			PaymentResourcePopUp PaymentResourcePopUp = new PaymentResourcePopUp(
					display.getActiveShell(), SWT.NONE, strURL, true);
			YRCDialog dialog = new YRCDialog(PaymentResourcePopUp, 1200, 700,
					AcademyPCAConstants.ATTR_PAYMENT_COLLECTION_DETAILS,
					AcademyPCAConstants.ATTR_PAYMENT__DETAILS, "", true);
			dialog.open();
		}
		if (fieldName.equals("extn_LnkCreateMemo")) {
			// Retrieve the Model
			Element eleOrderDetails = getModel(AcademyPCAConstants.ATTR_ORDER_COLLECTION_DETAILS);
			YRCPlatformUI.trace("#########Checking Model Data############"
					+ YRCXmlUtils.getString(eleOrderDetails));
			// Get the orderheaderkey from the model above
			String sorderHeaderKey = eleOrderDetails
					.getAttribute(AcademyPCAConstants.ATTR_ORDER_HEADER_KEY);
			YRCPlatformUI.trace("###### Get the Order Header Key#############"
					+ YRCXmlUtils
							.getAttribute(eleOrderDetails, sorderHeaderKey));
			strURL= ResourceUtil.get("CONSOLE_PAYMENT_DETAILS_CREDIT_MEMO_URL1")+ sorderHeaderKey + ResourceUtil.get("CONSOLE_PAYMENT_DETAILS_CREDIT_MEMO_URL2");
			Display display = Display.getDefault();
			// open sterling console for payment details
			PaymentResourcePopUp PaymentResourcePopUp = new PaymentResourcePopUp(
					display.getActiveShell(), SWT.NONE, strURL, true);
			YRCDialog dialog = new YRCDialog(PaymentResourcePopUp, 1200, 700,
					AcademyPCAConstants.ATTR_PAYMENT_COLLECTION_DETAILS,
					AcademyPCAConstants.ATTR_PAYMENT__DETAILS, "", true);
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
//TODO Validation required for a Link control: extn_LnkViewAuthorizationandChargeDetails