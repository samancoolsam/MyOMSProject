package com.academy.ecommerce.sterling.manageCustomerAddress.extn;

/**
 * Created on May 10,2009
 *
 */
//Java Imports
import java.util.ArrayList;
import org.w3c.dom.Element;

//Sterling Imports
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCDesktopUI;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCWizard;
import com.yantra.yfc.rcp.YRCWizardExtensionBehavior;


//Project Imports


import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.academy.ecommerce.sterling.util.XMLUtil;

//Misc Imports - NONE
/**
 * @author sahmed Copyright © 2005-2008 Sterling Commerce, Inc. All Rights
 *         Reserved.
 */
public class AcademyAddressPanelExtnBehavior extends YRCExtentionBehavior {

	private String strIsSignatureRequired=null;
	private String strIsPOBOXADDRESS=null;
	private String strIsAPOFPO=null;

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

	/**
	 * post set model to check the model on load of the screen and set the
	 * extended attributes values, which are read only. The updated changes from
	 * the edit address pop up screen are validated and changes to the extended
	 * attributes on this screen are synchronized accordingly
	 */

	public void postSetModel(String namespace) {
		YRCWizard currentPage = (YRCWizard) YRCDesktopUI.getCurrentPage();
		YRCPlatformUI.trace(currentPage.getFormId());
		if(currentPage.getFormId().equalsIgnoreCase("com.yantra.pca.ycd.rcp.tasks.orderEntry.wizards.YCDOrderEntryWizard")) {
			/*YRCExtentionBehavior parentBehavior = getParentExtnBehavior();
			if(parentBehavior != null) {*/
				setControlVisible("extn_ChkIsSignatureRequired", false);
				setControlVisible("extn_ChkIsPOAddress", false);
				setControlVisible("extn_ChkIsAPOFPO", false);
			
		}
		if (namespace.equals(AcademyPCAConstants.ATTR_ADDRESS)) {
			// the extended fields on the manage customer address screen are
			// READ-ONLY
			disableField(AcademyPCAConstants.ATTR_IS_SIGNATURE_REQUIRED);
			disableField(AcademyPCAConstants.ATTR_IS_POBOX_ADDRESS);
			disableField(AcademyPCAConstants.ATTR_IS_APO_FPO);
			// the root element of the customerAddress model is retrieved
			Element personInfoEle=getModel("address");
			if (!YRCPlatformUI.isVoid(personInfoEle)) {
			YRCPlatformUI.trace("Address Panel model in post command: :"+XMLUtil.getElementXMLString(personInfoEle));
    		Element eleExtn=(Element) personInfoEle.getElementsByTagName(AcademyPCAConstants.ATTR_EXTN).item(0);
    		strIsSignatureRequired=getFieldValue(AcademyPCAConstants.ATTR_IS_SIGNATURE_REQUIRED);
    		strIsPOBOXADDRESS=getFieldValue(AcademyPCAConstants.ATTR_IS_POBOX_ADDRESS);
    		strIsAPOFPO=getFieldValue(AcademyPCAConstants.ATTR_IS_APO_FPO);
			}
    		
			
			Element rootEle = getModel("customerAddress");
			// the Extn element is retrieved
			if (!YRCPlatformUI.isVoid(rootEle)) {
				Element extnElem = (Element) rootEle.getElementsByTagName(
						"Extn").item(0);
				YRCPlatformUI.trace("########## Inside postSetModel##########"
						+ XMLUtil.getElementXMLString(rootEle));
				// the model values are synchronized with the address pop up
				// model values.
				try {
					extnElem.setAttribute("ExtnIsSignatureRequired",strIsSignatureRequired);
					extnElem.setAttribute("ExtnIsPOBOXADDRESS",strIsPOBOXADDRESS);
					extnElem.setAttribute("ExtnIsAPOFPO",strIsAPOFPO);
					// the updated model is populated.
					//repopulateModel(AcademyPCAConstants.ATTR_CUST_ADDRESS);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					if (YRCPlatformUI.isTraceEnabled()) {
						YRCPlatformUI.trace(e);
					}
				}

			}
			
			super.postSetModel(namespace);

		}
	}

}
//TODO Validation required for a Check control: extn_ChkIsAPOFPO
//TODO Validation required for a Link control: lnkEditAddress
//TODO Validation required for a Check control: extn_ChkIsSignatureRequired
//TODO Validation required for a Check control: extn_ChkIsPOAddress