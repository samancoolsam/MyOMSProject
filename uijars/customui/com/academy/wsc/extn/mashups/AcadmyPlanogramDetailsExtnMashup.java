package com.academy.wsc.extn.mashups;

import com.ibm.wsc.common.mashups.WSCBasePaginatedMashup;
import com.ibm.wsc.mashups.utils.WSCMashupUtils;
import com.ibm.wsc.shipment.ShipmentUtils;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.ui.web.framework.context.SCUIContext;
import com.sterlingcommerce.ui.web.framework.helpers.SCUIMashupHelper;
import com.sterlingcommerce.ui.web.framework.mashup.SCUIMashupMetaData;
import org.w3c.dom.Element;

public class AcadmyPlanogramDetailsExtnMashup extends WSCBasePaginatedMashup {
   
	public void massageAPIOutput(Element outEl, SCUIMashupMetaData mashupMetaData, SCUIContext uiContext)
	  {
	    super.massageAPIOutput(outEl, mashupMetaData, uiContext);
	    ShipmentUtils.handleShipmentLineForVariationItem(outEl, uiContext);

	  }
	
		public Element massageOutput(Element outEl, SCUIMashupMetaData mashupMetaData, SCUIContext uiContext) {
			
			outEl = (Element) SCUIMashupHelper.invokeMashup("extn_PlanogramDetialsForBackroomPickItemScanScreen", outEl, uiContext);
	        
			outEl = super.massageOutput(outEl, mashupMetaData, uiContext);
	
			return outEl;
		}

}
