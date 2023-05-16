package com.academy.ecommerce.sterling.bopis.shipment;

import java.util.Map;
import org.w3c.dom.Document;
import com.academy.util.constants.AcademyConstants;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyCheckShipStatusBeforeCancellation implements YCPDynamicConditionEx{
private static YFCLogCategory log = YFCLogCategory.instance(AcademyCheckShipStatusBeforeCancellation.class);
	
	/**
	 * This method will check what was the shipment status before it is cancelled. If shipment is cancelled after Ready
	 * For Backroom Pick status (1100.70.06.10) status it will return true else false.
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception 
	 */
	
	@Override
	public boolean evaluateCondition(YFSEnvironment arg0, String arg1,
			Map arg2, Document arg3) {
       boolean boolStatusGreater = true;
		YFCDocument outDocCancelShipEvent = YFCDocument.getDocumentFor(arg3);
		YFCElement eleOutDocShp = outDocCancelShipEvent.getDocumentElement();
		String strStatus = eleOutDocShp.getAttribute(AcademyConstants.STATUS);
		if (YFCCommon.equalsIgnoreCase(strStatus, AcademyConstants.VAL_CANCELLED_STATUS)){
			YFCNodeList<YFCElement> nlShipmentAudit =  eleOutDocShp.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_STATUS_AUDIT);
		     for (YFCElement eleShipmentAudit: nlShipmentAudit){
		    	 String strNewStatus = eleShipmentAudit.getAttribute(AcademyConstants.ATTR_NEW_STATUS);
		    	 if (YFCCommon.equalsIgnoreCase(strNewStatus,AcademyConstants.VAL_CANCELLED_STATUS)){
		    		 String strOldStatus = eleShipmentAudit.getAttribute(AcademyConstants.ATTR_OLD_STATUS);
		    		 if (YFCCommon.equalsIgnoreCase(strOldStatus,AcademyConstants.STR_SHIPMENT_CREATED_STATUS) || YFCCommon.equalsIgnoreCase(strOldStatus,AcademyConstants.VAL_SHIP_STATUS)){
		    			 boolStatusGreater = false;
		    		 }
		    	 }
		    	 
		     }
		}
		return boolStatusGreater;
	}

	@Override
	public void setProperties(Map arg0) {
		// TODO Auto-generated method stub
		
	}
}
