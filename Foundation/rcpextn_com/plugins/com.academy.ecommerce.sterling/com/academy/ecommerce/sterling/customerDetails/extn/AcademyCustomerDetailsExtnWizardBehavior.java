	
package com.academy.ecommerce.sterling.customerDetails.extn;

/**
 * Created on May 10,2009
 *
 */
 //Java Imports
import java.util.ArrayList;
//Sterling Imports
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCWizardExtensionBehavior;

//Package Imports
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;

//Misc Imports -NONE
/**
 * @author sahmed
 * Copyright © 2005-2008 Sterling Commerce, Inc. All Rights Reserved.
 */
 public class AcademyCustomerDetailsExtnWizardBehavior extends YRCWizardExtensionBehavior {

	 /**The Is RedLined field and Opt In field are read only on the customer details page. */
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
	    	setControlEditable("extn_textCustomerIDValue", false);
	    }
	 	
	 	
		/**
		 * Method for validating the text box.
	     */
	    public YRCValidationResponse validateTextField(String fieldName, String fieldValue) {
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
			return super.validateLinkClick(fieldName);
		}
		
		/**
		 * Create and return the binding data for advanced table columns added to the tables.
		 */
		 public YRCExtendedTableBindingData getExtendedTableBindingData(String tableName, ArrayList tableColumnNames) {
		 	return super.getExtendedTableBindingData(tableName, tableColumnNames);
		 }
		 
		 /** On load of the screen, the value of ExtnIsRedLined is retrieved from postSetModel and 
		 binded to extn_ChkIsRedLined. This is a read-only field */
		 
		 public void postSetModel(String namespace) {
			
				if(namespace.equals(AcademyPCAConstants.API_GET_CUSTOMERDETAILS_OUTPUT)){
					disableField(AcademyPCAConstants.ATTR_IS_RED_LINED);
					disableField(AcademyPCAConstants.ATTR_OPT_IN);
				   }
				super.postSetModel(namespace);
			}


	}
//TODO Validation required for a Check control: extn_OptIn
//TODO Validation required for a Check control: extn_ChkIsRedLined
//TODO Validation required for a Text control: extn_TCompanyName