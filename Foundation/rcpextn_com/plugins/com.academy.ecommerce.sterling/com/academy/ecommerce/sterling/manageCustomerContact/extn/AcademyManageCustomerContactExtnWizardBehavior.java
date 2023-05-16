package com.academy.ecommerce.sterling.manageCustomerContact.extn;

/**
 * Created on May 10,2009
 *
 */

//Java Imports
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

//Sterling Imports
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCWizardExtensionBehavior;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCXmlUtils;
import com.yantra.yfc.rcp.internal.YRCApiCaller;

//Project Imports -NONE
//Misc Imports -NONE
/**
 * @author sahmed Copyright © 2005-2008 Sterling Commerce, Inc. All Rights
 *         Reserved.
 */
public class AcademyManageCustomerContactExtnWizardBehavior extends
		YRCWizardExtensionBehavior {

	/**
	 * This method initializes the behavior class.
	 */
	public void init() {

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

		// TODO Validation required for the following controls.

		// Control name: extn_TBusinessName

		// TODO Create and return a response.
		return super.validateTextField(fieldName, fieldValue);
	}

	/**
	 * Method for validating the combo box entry.
	 */
	public void validateComboField(String fieldName, String fieldValue) {
		// TODO Validation required for the following controls.

		// Control name: extn_CmbTitle

		// TODO Create and return a response.
		super.validateComboField(fieldName, fieldValue);
	}

	/**
	 * Method called when a button is clicked.
	 */
	@Override
	public YRCValidationResponse validateButtonClick(String fieldName) {

		// TODO Validation required for the following controls.

		// TODO Create and return a response.
		return super.validateButtonClick(fieldName);
	}

	/**
	 * Method called when a link is clicked.
	 */
	@Override
	public YRCValidationResponse validateLinkClick(String fieldName) {
		// TODO Validation required for the following controls.

		// TODO Create and return a response.
		return super.validateLinkClick(fieldName);
	}

	@Override
	public boolean preCommand(YRCApiContext apiContext) {

		String apiname = apiContext.getApiName();
		if (apiname.equals("manageCustomer")) {
			Document inputXml = apiContext.getInputXml();
			// START # 2544
			String oldEmail = this.getModel("getCustomerContactDetails_output")
					.getAttribute("EmailID");
			String strEmail = this.getFieldValue("txtEmailID");

			if (strEmail.equalsIgnoreCase("")) {
				YRCPlatformUI.showError("Error",
						"Email ID is Mandatory field for web profile");
				return false;
			}
			if (oldEmail != null && !oldEmail.equalsIgnoreCase("")
					&& !oldEmail.equals(strEmail)) {
				YRCApiContext context = new YRCApiContext();
				YRCApiCaller syncApiCaller = new YRCApiCaller(context, true);
				context.setApiName("getCustomerList");
				context.setFormId(apiContext.getFormId());
				// Input Xml to getCustomerList API
				Document inputToGetCustLit = YRCXmlUtils
						.createDocument("Customer");
				Element custContList = YRCXmlUtils.createChild(
						inputToGetCustLit.getDocumentElement(),
						"CustomerContactList");
				Element custCont = YRCXmlUtils.createChild(custContList,
						"CustomerContact");
				YRCXmlUtils.setAttribute(custCont, "EmailID", strEmail);

				// Set input xml at API Context
				context.setInputXml(inputToGetCustLit);
				syncApiCaller.invokeApi();
				Document outputOfGetCustList = context.getOutputXml();
				if (outputOfGetCustList != null
						&& outputOfGetCustList.hasChildNodes()) {
					if (outputOfGetCustList.getElementsByTagName("Customer")
							.getLength() > 0) {
						YRCPlatformUI.showError("Error",
								"Email ID is already in use");
						return false;
					}
				}
			}
			// END # 2544
			Element customerContactList = YRCXmlUtils.getChildElement(inputXml
					.getDocumentElement(), "CustomerContactList");
			Element customerContact = YRCXmlUtils.getChildElement(
					customerContactList, "CustomerContact");
			YRCXmlUtils.setAttribute(customerContact, "UserID", strEmail);
			// YRCXmlUtils.setAttribute(customerContact,"CustomerContactID",
			// strEmail);
			apiContext.setInputXml(inputXml);
		}

		// TODO Auto-generated method stub
		return super.preCommand(apiContext);
	}

	/**
	 * Create and return the binding data for advanced table columns added to
	 * the tables.
	 */
	@Override
	public YRCExtendedTableBindingData getExtendedTableBindingData(
			String tableName, ArrayList tableColumnNames) {
		// Create and return the binding data definition for the table.

		// The defualt super implementation does nothing.
		return super.getExtendedTableBindingData(tableName, tableColumnNames);
	}

	@Override
	public void postSetModel(String arg0) {
		if (arg0.equals(AcademyPCAConstants.API_GET_CUSTOMERDETAILS_OUTPUT)) {
			boolean b = YRCPlatformUI
					.hasPermission(AcademyPCAConstants.ATTR_ACAD_RED_LINE_PERMISSION);
			if (b) {
				setControlVisible(AcademyPCAConstants.ATTR_EXTN_CHKISREDLINED,
						true);
			} else {
				setControlVisible(AcademyPCAConstants.ATTR_EXTN_CHKISREDLINED,
						false);
			}
		}
		super.postSetModel(arg0);
	}
}
// TODO Validation required for a Text control: extn_TBusinessName
// TODO Validation required for a Check control: extn_OptIn
// TODO Validation required for a Check control: extn_ChkIsRedLined
// TODO Validation required for a Combo control: extn_CmbTitle
