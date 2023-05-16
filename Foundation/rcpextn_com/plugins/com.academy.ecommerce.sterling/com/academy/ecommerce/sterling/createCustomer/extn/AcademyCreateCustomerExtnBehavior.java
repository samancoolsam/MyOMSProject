package com.academy.ecommerce.sterling.createCustomer.extn;

/**
 * Created on May 10,2009
 *
 */
//Java Imports
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCWizardExtensionBehavior;
import com.yantra.yfc.rcp.YRCXmlUtils;

import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.yantra.yfc.rcp.internal.YRCApiCaller;

//Misc Imports -NONE
/**
 * @author sahmed Copyright © 2005-2008 Sterling Commerce, Inc. All Rights
 *         Reserved.
 */
public class AcademyCreateCustomerExtnBehavior extends
		YRCWizardExtensionBehavior {

	private String sBusinessCustomerName = null;

	/**
	 * This method initializes the behavior class.
	 */
	@Override
	public void init() {

	}

	public String getExtnNextPage(String currentPageId) {
		return null;
	}

	@Override
	public IYRCComposite createPage(String pageIdToBeShown) {
		return null;
	}

	@Override
	public void pageBeingDisposed(String pageToBeDisposed) {
	}

	/**
	 * Called when a wizard page is about to be shown for the first time.
	 * 
	 */
	@Override
	public void initPage(String pageBeingShown) {

		Element element = YRCXmlUtils.createDocument("ExtnDummy")
				.getDocumentElement();

		element.setAttribute("ExtnRequiresWebprofileID", "Y");
		element.setAttribute("ExtnOptIn", "Y");
		setExtentionModel("ExtnDummy", element);
		// repopulateModel("ExtnDummy");

	}

	/**
	 * Method for validating the text box.
	 */
	@Override
	public YRCValidationResponse validateTextField(String fieldName,
			String fieldValue) {
		return super.validateTextField(fieldName, fieldValue);
	}

	/**
	 * Method for validating the combo box entry.
	 */
	@Override
	public void validateComboField(String fieldName, String fieldValue) {
		super.validateComboField(fieldName, fieldValue);
	}

	/**
	 * Method called when a button is clicked. The Checks are made for the
	 * following extensions:- 1.Check if the Corporate Customer Check is made,
	 * if yes enable the Business Name Field and disable the
	 * RequriesWebprofileID field 2.Check if the RequriesWebprofileID is
	 * checked, if yes disable bothe the Business Name field and the Corporate
	 * Customer field.
	 */
	@Override
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

	@Override
	public boolean preCommand(YRCApiContext arg0) {

		if (arg0.getApiName().equals("manageCustomer")) {

			Element custContactEle = (Element) (arg0.getInputXml()
					.getDocumentElement().getElementsByTagName(
							"CustomerContact").item(0));

			// Element
			// extnEle=(Element)custContactEle.getElementsByTagName("Extn").item(0);

			if (custContactEle.getAttribute("EmailID").equalsIgnoreCase(""))

			{

				YRCPlatformUI.showError("Error",
						"Email ID is Mandatory field for web profile creation");

				return false;

			}

			/*
			 * if(extnEle.getAttribute("ExtnRequiresWebprofileID").equalsIgnoreCase("Y")) { }
			 */
			// START # 2544
			YRCApiContext context = new YRCApiContext();
			YRCApiCaller syncApiCaller = new YRCApiCaller(context, true);
			context.setApiName("getCustomerList");
			context.setFormId(getFormId());
			// Input Xml to getCustomerList API
			Document inputToGetCustLit = YRCXmlUtils.createDocument("Customer");
			Element custContList = YRCXmlUtils.createChild(inputToGetCustLit
					.getDocumentElement(), "CustomerContactList");
			Element custCont = YRCXmlUtils.createChild(custContList,
					"CustomerContact");
			YRCXmlUtils.setAttribute(custCont, "EmailID", custContactEle
					.getAttribute("EmailID"));

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
			// END # 2544
		}

		// TODO Auto-generated method stub
		return super.preCommand(arg0);
	}

	/**
	 * Method called when a link is clicked.
	 */
	@Override
	public YRCValidationResponse validateLinkClick(String fieldName) {
		return super.validateLinkClick(fieldName);
	}

	/**
	 * Create and return the binding data for advanced table columns added to
	 * the tables.
	 */
	@Override
	public YRCExtendedTableBindingData getExtendedTableBindingData(
			String tableName, ArrayList tableColumnNames) {
		return super.getExtendedTableBindingData(tableName, tableColumnNames);
	}
}
// TODO Validation required for a Check control: extn_ChkRequiresWebprofileID
// TODO Validation required for a Text control: extn_TCompany
// TODO Validation required for a Check control: extn_OptIn
// TODO Validation required for a Check control: extn_ChkCorporateCustomer
// TODO Validation required for a Combo control: extn_CmbTitle
