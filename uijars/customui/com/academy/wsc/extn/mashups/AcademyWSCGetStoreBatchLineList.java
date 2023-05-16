package com.academy.wsc.extn.mashups;

import org.w3c.dom.Element;

import com.ibm.wsc.shipment.batchpick.mashups.WSCGetStoreBatchLineList;
import com.sterlingcommerce.ui.web.framework.context.SCUIContext;
import com.sterlingcommerce.ui.web.framework.helpers.SCUIMashupHelper;
import com.sterlingcommerce.ui.web.framework.mashup.SCUIMashupMetaData;

public class AcademyWSCGetStoreBatchLineList extends WSCGetStoreBatchLineList{
	public Element massageOutput(Element outEl, SCUIMashupMetaData mashupMetaData, SCUIContext uiContext) {
        
		outEl = super.massageOutput(outEl, mashupMetaData, uiContext);
		outEl.setAttribute("isBatchPlanogram", "Y");
		outEl = (Element) SCUIMashupHelper.invokeMashup("extn_ItemPropertiesService", outEl, uiContext);

		return outEl;
	}
}
