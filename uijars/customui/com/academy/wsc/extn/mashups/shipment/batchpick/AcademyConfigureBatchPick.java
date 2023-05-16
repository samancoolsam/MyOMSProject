package com.academy.wsc.extn.mashups.shipment.batchpick;

import com.ibm.wsc.common.mashups.WSCBaseMashup;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.ui.web.framework.context.SCUIContext;
import com.sterlingcommerce.ui.web.framework.mashup.SCUIMashupMetaData;
import com.sterlingcommerce.ui.web.framework.utils.SCUIUtils;
import java.util.Iterator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AcademyConfigureBatchPick extends WSCBaseMashup{
	
	public Element massageOutput(Element outEl, SCUIMashupMetaData mashupMetaData, SCUIContext uiContext)
	  {
	    Element userPrefsElem = SCXmlUtil.createDocument("UserPref").getDocumentElement();
	    Iterator it = SCXmlUtil.getChildren(outEl);
	    while (it.hasNext())
	    {
	      Element userPrefElem = (Element)it.next();
	      String componentName = userPrefElem.getAttribute("ComponentName");
	      if (SCUIUtils.equals(componentName, "BATCH_MAX_SHIPMENTS")) {
	        userPrefsElem.setAttribute("BatchMaxShipments", userPrefElem.getAttribute("Definition"));
	      }
	      if (SCUIUtils.equals(componentName, "BATCH_SORT_METHOD")) {
	        userPrefsElem.setAttribute("BatchSortMethod", userPrefElem.getAttribute("Definition"));
	      }
	    }
	    if (!userPrefsElem.hasAttribute("BatchMaxShipments")) {
	      userPrefsElem.setAttribute("BatchMaxShipments", "10");
	    }
	    if (!userPrefsElem.hasAttribute("BatchSortMethod")) {
	      userPrefsElem.setAttribute("BatchSortMethod", "SORT_AFTER_PICK");
	      
	    }
	    return userPrefsElem;
	  }

}
