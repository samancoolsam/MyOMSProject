package com.academy.ecommerce.sterling.addressCapture.extn;

/**
 * Created on May 10,2009
 *
 */
//Java Imports
import java.util.ArrayList;

import org.w3c.dom.Document;

import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCWizardExtensionBehavior;
import com.yantra.yfc.rcp.YRCXmlUtils;

//Sterling Imports
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;

//Misc Imports

/**
 * @author sahmed Copyright © 2005-2008 Sterling Commerce, Inc. All Rights
 *         Reserved.
 */
public class AcademyAddressCaptureExtnWizardBehavior extends
		YRCWizardExtensionBehavior {
	public static final String FORM_ID = AcademyPCAConstants.FORM_ID_ADDRESS_CAPTURE;
	String strTitle = null;

	/**
	 * This method initializes the behavior class.
	 */

	@Override
	public void init() {
		setFocus(AcademyPCAConstants.ATTR_PNL_CONTACT);
		Document docItemListModel = YRCXmlUtils
				.createDocument(AcademyPCAConstants.ATTR_EXTN);
		setExtentionModel(AcademyPCAConstants.ATTR_EXTN_EXTENDED_ATTRIBUTES,
				docItemListModel.getDocumentElement());

		YRCApiContext ctx = new YRCApiContext();
		ctx.setApiName(AcademyPCAConstants.API_GET_COMMONCODELIST);
		ctx.setFormId(FORM_ID);
		Document getCommonCodeListInput = YRCXmlUtils
				.createDocument(AcademyPCAConstants.ELE_COMMON_CODE);
		getCommonCodeListInput.getDocumentElement().setAttribute(
				AcademyPCAConstants.ATTR_CODE_TYPE,
				AcademyPCAConstants.ATTR_CODE_TYPE_VALUE);
		if (YRCPlatformUI.isTraceEnabled()) {
			YRCPlatformUI.trace(AcademyPCAConstants.ATTR_GET_COMMON_CODE_LIST
					+ YRCXmlUtils.getString(getCommonCodeListInput));
		}
		ctx.setInputXml(getCommonCodeListInput);

		callApi(ctx);
	}

	@Override
	public void handleApiCompletion(YRCApiContext ctx) {
		if (AcademyPCAConstants.API_GET_COMMONCODELIST.equals(ctx.getApiName())) {
			Document commonCodeListDoc = ctx.getOutputXml();
			if (YRCPlatformUI.isTraceEnabled()) {
				YRCPlatformUI.trace("####OUTPUT FOR GET COMMONE CODE LIST ####"
						+ YRCXmlUtils.getString(commonCodeListDoc));
			}
			setExtentionModel(AcademyPCAConstants.ATTR_EXTN_COMMON_CODE_LIST,
					commonCodeListDoc.getDocumentElement());
			if (strTitle != null) {
				setFieldValue("extn_CmbTitle", strTitle);
			}

		}
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

	@Override
	public YRCValidationResponse validateButtonClick(String fieldName) {
		return super.validateButtonClick(fieldName);
	}

	public void setBindingforComponents() {

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

	@Override
	public void postSetModel(String namespace) {
		if (namespace.equals("PersonInfo")) {
			strTitle = getModel("PersonInfo").getAttribute("Title");
		}
		super.postSetModel(namespace);
	}

}
// TODO Validation required for a Check control: extn_ChkIsAPOFPO
// TODO Validation required for a Button control: btnConfirm
// TODO Validation required for a Check control: extn_ChkIsSignatureRequired
// TODO Validation required for a Check control: extn_ChkIsPOAddress
// TODO Validation required for a Combo control: extn_CmbTitle
