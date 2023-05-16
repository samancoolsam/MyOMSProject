package com.academy.ecommerce.sterling.paymentConfirm.extn;

/**
 * Created on May 10,2009
 *
 */
//Java Imports
import java.util.ArrayList;

import org.w3c.dom.Element;

//Sterling Imports
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCXmlUtils;
import com.yantra.yfc.rcp.internal.YRCApiCaller;

//Package Imports
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;

//Misc Imports - NONE
/**
 * @author sahmed Copyright © 2005-2008 Sterling Commerce, Inc. All Rights
 *         Reserved.
 */
public class AcademyPaymentConfirmationExtnBehavior extends
		YRCExtentionBehavior {
	 boolean bRefresh = false;
	/**
	 * This method initializes the behavior class.
	 */
	public void init() {
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
		if (fieldName.equals(AcademyPCAConstants.ATTR_BTN_TAX_EXEMPTID))
			try {
				Element eleOrderDetails = getModel(AcademyPCAConstants.ATTR_ORDER);
				// Retrieve the details of the model and check for null.
				String orderHeaderKey = null;
				if (!YRCPlatformUI.isVoid(eleOrderDetails))
					// Fetch the Order Header Key from the model
					orderHeaderKey = eleOrderDetails
							.getAttribute(AcademyPCAConstants.ATTR_ORDER_HEADER_KEY);
				if (YRCPlatformUI.isTraceEnabled()) {
					YRCPlatformUI.trace("##### Verify Order Details Element ########"
							+ YRCXmlUtils.getString(eleOrderDetails));
				}
				// create input for changeOrder API call
				org.w3c.dom.Document doc = (org.w3c.dom.Document) YRCXmlUtils
						.createDocument(AcademyPCAConstants.ATTR_ORDER);
				Element eleOrder = doc.getDocumentElement();
				eleOrder.setAttribute(
						AcademyPCAConstants.ATTR_ORDER_HEADER_KEY,
						orderHeaderKey);
				String strTaxExemptID = this
						.getFieldValue(AcademyPCAConstants.ATTR_TXT_TAX_EXEMPTID);
				if (!YRCPlatformUI.isVoid(strTaxExemptID)) {
					eleOrder.setAttribute(
							AcademyPCAConstants.ATTR_TAX_EXEMPTION_CERTIFICATE,
							strTaxExemptID);
					bRefresh=true;
				} else {
					/*
					 * In case submit is clicked without Tax ID error needs to
					 * be displayed
					 */
					YRCPlatformUI.showError(AcademyPCAConstants.ATTR_ERROR, "Enter a Valid Tax ID");
				 bRefresh=true;
				}
				YRCPlatformUI.trace("#########Doc for Added TaxExemptID ######"
						+ YRCXmlUtils.getString(doc));
				YRCApiContext context = new YRCApiContext();
				YRCApiCaller syncapiCaller = new YRCApiCaller(context,
						true);
				context.setApiName(AcademyPCAConstants.ATTR_CHANGE_ORDER);
				context.setFormId(getFormId());
				context.setInputXml(doc);
				syncapiCaller.invokeApi();
				if(bRefresh)
                    YRCPlatformUI.fireAction(AcademyPCAConstants.ATTR_REFRESH_ACTION);
			} catch (Exception e) {
				e.printStackTrace();
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
		// Create and return the binding data definition for the table.

		// The defualt super implementation does nothing.
		return super.getExtendedTableBindingData(tableName, tableColumnNames);
	}
}
//TODO Validation required for a Text control: extn_TTaxExemptID
//TODO Validation required for a Button control: extn_BtnTaxExemptID