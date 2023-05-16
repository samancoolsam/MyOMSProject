	
package com.academy.ecommerce.sterling.fulfillmentSummary.extn;

/**
 * Created on May 11,2009
 *
 */
 
/*import DebsCCProhibitionCodeScreenCellModifier;
import DebsCCProhibitionCodeScreenImageProvider;*/
//Java imports
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Element;

//Sterling Imports
import com.yantra.yfc.rcp.YRCExtendedCellModifier;
import com.yantra.yfc.rcp.YRCExtendedTableImageProvider;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCTblClmBindingData;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;

//project Imports
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
/**
 * @author sahmed
 * Copyright © 2005-2008 Sterling Commerce, Inc. All Rights Reserved.
 */
 public class AcademyFulfillmentSummaryExtnBehavior extends YRCExtentionBehavior{

	 
	/**
	 * This method initializes the behavior class.
	 */
	public void init() {
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
        // Create and return the binding data definition for the table.
        YRCPlatformUI.trace("#####  Inside getExtendedTableBindingData ########");
        YRCExtendedCellModifier myCellModifier=new YRCExtendedCellModifier(){
			@Override
			public boolean allowModify(String property, String value, Element element) {
				if(property.equals(AcademyPCAConstants.ATTR_CHECKED)){
				if(((Element) element.getElementsByTagName(AcademyPCAConstants.ATTR_EXTN).item(0)).getAttribute(AcademyPCAConstants.ATTR_DROP_SHIP_FLAG).equals("Y")){
					return false;
				}else return true;
				}
				return true;
			}

			@Override
			public String getModifiedValue(String arg0, String arg1,
					Element arg2) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public YRCValidationResponse validateModifiedValue(String property,
					String value, Element element) {
				
				return null;
			}
        	
        };
        YRCExtendedTableBindingData tblBindingData = new YRCExtendedTableBindingData();
        createTblColBindindData(tblBindingData);
        tblBindingData.setCellModifier(myCellModifier);
        tblBindingData.setImageProvider(new YRCExtendedTableImageProvider(){

			@Override
			public String getImageThemeForColumn(Object arg0, String arg1) {
				return null;
			}
        	
        });
       
        return tblBindingData;
    }
	/**
     * This method is used to set column binding data for each of the advance
     * custom columns . This is achieved by creating a hash map with multiple
     * instances of YRCTblClmBindingData for each of the columns and finally
     * setting the hash map against the instance of
     * 'YRCExtendedTableBindingData'
     *
     * @param  tblBindingData  Table Binding Data object that stores data for
     *                         custom columns
     */
    private void createTblColBindindData(YRCExtendedTableBindingData tblBindingData) {
		// TODO Auto-generated method stub
    	HashMap<String, YRCTblClmBindingData> clmBindDataMap = new HashMap();
		YRCTblClmBindingData clmBindData = new YRCTblClmBindingData();
		clmBindData.setAttributeBinding(AcademyPCAConstants.ATTR_CHECKED);
		clmBindData.setName(AcademyPCAConstants.ATTR_ITEM_CHECK);
		clmBindDataMap.put(AcademyPCAConstants.ATTR_EXTN_ITEM_CHECK, clmBindData);
		tblBindingData.setTableColumnBindingsMap(clmBindDataMap);
   	}

		public AcademyFulfillmentSummaryExtnBehavior getBehavior() {
			return null;
		}
 }