package com.academy.ecommerce.sterling.itemDetails.extn;

/**
 * Created on May 10,2009
 *
 */
//Java Imports
import java.util.ArrayList;

import org.eclipse.swt.widgets.Display;
//Sterling Imports
import com.yantra.yfc.rcp.YRCDesktopUI;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCTextBindingData;
import com.yantra.yfc.rcp.YRCValidationResponse;

//Package Imports

import com.academy.ecommerce.sterling.util.AcademyPCAConstants;

//Misc Imports -NONE
/**
 * @author sahmed
 * Copyright © 2005-2008 Sterling Commerce, Inc. All Rights Reserved.
 */
 public class AcademySytleSizeExtnBehavior extends YRCExtentionBehavior{

	 private static final String XPATH_CURRENCY = "getItemListForOrdering:ItemList/@Currency";
/**
 * This method initializes the behavior class.
 */
		 //public static Boolean onLoadFlag=true;
		public void init() {
			getBindingData(AcademyPCAConstants.ATTR_TXT_LIST_PRICE);
		}
		
		@Override
		public Object getBindingData(String fieldName) {
			if (fieldName.equals(AcademyPCAConstants.ATTR_TXT_LIST_PRICE)) {
	            YRCTextBindingData txtBindingData = new YRCTextBindingData();
	            txtBindingData.setSourceBinding("SelectedItem:Item/ComputedPrice/@ListPrice");
	            txtBindingData.setCurrencyXPath("getItemListForOrdering:ItemList/@Currency");
	            txtBindingData.setDynamic(true);
	            return txtBindingData;
	        }
			return super.getBindingData(fieldName);
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
	}
//TODO Validation required for a Text control: extn_TListPrice
//TODO Validation required for a Button control: btnClose
//TODO Validation required for a Text control: extn_TEndDate