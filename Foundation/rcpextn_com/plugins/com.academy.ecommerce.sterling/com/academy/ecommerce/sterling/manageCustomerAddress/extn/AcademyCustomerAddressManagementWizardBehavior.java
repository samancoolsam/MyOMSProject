	
package com.academy.ecommerce.sterling.manageCustomerAddress.extn;

/**
 * Created on Jun 05,2009
 *
 */
 
import java.util.ArrayList;

import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.yantra.yfc.rcp.YRCWizardExtensionBehavior;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
/**
 * @author sahmed
 * Copyright © 2005-2008 Sterling Commerce, Inc. All Rights Reserved.
 */
 public class AcademyCustomerAddressManagementWizardBehavior extends YRCWizardExtensionBehavior {
	   String strOrginalIsSignature;
	   String strOriginalIsAPOFPO;
	   String strOriginalIsPOBOX;
	/**
	 * This method initializes the behavior class.
	 */
	public void init() {
		   strOrginalIsSignature= getFieldValue(AcademyPCAConstants.ATTR_IS_SIGNATURE_REQUIRED);
		   strOriginalIsAPOFPO=getFieldValue(AcademyPCAConstants.ATTR_IS_APO_FPO);
		   strOriginalIsPOBOX=getFieldValue(AcademyPCAConstants.ATTR_IS_POBOX_ADDRESS);
	}
 
 	
    public String getExtnNextPage(String currentPageId) {
		//TODO
		return null;
    }
    
    public IYRCComposite createPage(String pageIdToBeShown) {
		//TODO
		return null;
	}
    
    public void pageBeingDisposed(String pageToBeDisposed) {
		//TODO
    }

    /**
     * Called when a wizard page is about to be shown for the first time.
     *
     */
    public void initPage(String pageBeingShown) {
		//TODO
    }
 	
 	
	/**
	 * Method for validating the text box.
     */
    public YRCValidationResponse validateTextField(String fieldName, String fieldValue) {
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
		
			// Control name: btnClose
		
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
	 * Create and return the binding data for advanced table columns added to the tables.
	 */
	 public YRCExtendedTableBindingData getExtendedTableBindingData(String tableName, ArrayList tableColumnNames) {
	 	// Create and return the binding data definition for the table.
		
	 	// The defualt super implementation does nothing.
	 	return super.getExtendedTableBindingData(tableName, tableColumnNames);
	 }


	@Override
	public void postSetModel(String namespace) {
		super.postSetModel(namespace);
	}
	 
	 
}
//TODO Validation required for a Button control: btnClose