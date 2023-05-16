package com.academy.som.searchshipment.screens;

import java.util.ArrayList;
import java.util.HashMap;

import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCTblClmBindingData;
	//fix for STL - 1039

public class AcademySearchShipmentWizardExtnBehaviour extends YRCExtentionBehavior {	

	
	public YRCExtendedTableBindingData getExtendedTableBindingData(
			String tableName, ArrayList tableColumnNames) {
	    YRCExtendedTableBindingData extnTblBindingData = null;
	    
	    if ("tblSearchResults".equals(tableName)) {
	    	
	      for (int i = 0; i < tableColumnNames.size(); i++) {
	        String controlName = (String)tableColumnNames.get(i);
	        
	        if ("extn_clmReqShipDate"
	          .equals(controlName)) {
	        	
	          extnTblBindingData = new YRCExtendedTableBindingData(
	            tableName);
	          HashMap bindingDataMap = new HashMap();

	          YRCTblClmBindingData advClmBindingData = new YRCTblClmBindingData();

	          advClmBindingData
	            .setAttributeBinding("@RequestedShipmentDate");
	          advClmBindingData.setDbLocaliseReqd(false);
	          advClmBindingData.setSortReqd(true);

	          advClmBindingData
	            .setFilterBinding("@RequestedShipmentDate");
	          advClmBindingData.setColumnBinding(null);
	          advClmBindingData.setDataType("LastOccurredOn");
	          bindingDataMap.put(controlName, advClmBindingData);
	          extnTblBindingData
	            .setTableColumnBindingsMap(bindingDataMap);
	        }
	      }
	    }

	    if (extnTblBindingData != null) {
	      return extnTblBindingData;
	    }

	    return super.getExtendedTableBindingData(tableName, tableColumnNames);
	  }
	//fix for STL - 1039
}
