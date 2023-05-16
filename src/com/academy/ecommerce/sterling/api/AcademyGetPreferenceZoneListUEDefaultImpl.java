package com.academy.ecommerce.sterling.api;

import java.util.Properties;

import org.w3c.dom.Document;

import com.academy.util.constants.AcademyConstants;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyGetPreferenceZoneListUEDefaultImpl implements YIFCustomApi{

    /**
     * Instance to store the properties configured for the condition in Configurator.
     */
    private Properties props;
    
    /*
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademyGetPreferenceZoneListUEDefaultImpl.class);
    
    
    /**
     * Creates a new instance of AcademyGetPreferenceZoneListUEDefaultImpl 
     */
    public void setProperties(Properties props) {
        this.props = props;
    }
    public Document invokeDefaultLogic(YFSEnvironment env, Document inDoc) throws Exception {
    	log.beginTimer(" Begining of AcademyGetPreferenceZoneListUEDefaultImpl-> invokeDefaultLogic Api");
    	YFCElement element = (YFCDocument.getDocumentFor(inDoc)).getDocumentElement();
         YFCElement prefElems = element.getChildElement(AcademyConstants.ELE_LOCATION_PREFERENCES);
         if (prefElems != null){
             prefElems.setAttribute(AcademyConstants.ELE_NODE, element.getAttribute(AcademyConstants.ELE_NODE));
             YFCDocument outDoc = YFCDocument.createDocument();
             YFCElement elem = (YFCElement)outDoc.importNode(prefElems, true);
             outDoc.appendChild(elem);
             log.endTimer(" End of AcademyGetPreferenceZoneListUEDefaultImpl-> invokeDefaultLogic Api");
             return outDoc.getDocument();
         }
		return null;
    }
    
}
