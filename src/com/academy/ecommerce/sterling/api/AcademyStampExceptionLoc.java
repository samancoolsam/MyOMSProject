package com.academy.ecommerce.sterling.api;

import java.util.Properties;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * This API is used to stamp the exception zone in case "SHORT" exception code 
 * is entered on partial deposit screen. This will get invoked as part of UE and when user clicks on done.
 * 
 */

public class AcademyStampExceptionLoc implements YIFCustomApi{
	/**
     * Instance to store the properties configured for the condition in Configurator.
     */
    private Properties props;
    
    /*
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademyStampExceptionLoc.class);
    

    public void setProperties(Properties props) {
        this.props = props;
    }
    
    public Document stampExceptionLocation(YFSEnvironment env, Document inDoc) throws Exception {
    	
    	Element inElem = inDoc.getDocumentElement();
    	Document outDoc = null;
    	log.beginTimer(" Begining of AcademyStampExceptionLoc -> stampExceptionLocation()Api");
    	String sStorageType = inElem.getAttribute("ItemClassification1");
    	String sItemType = inElem.getAttribute("ItemClassification2");

    	Element LocationPrefs = XMLUtil.getFirstElementByName(inElem, AcademyConstants.ELE_LOCATION_PREFERENCES);
    	log.verbose("Input to method is"+ XMLUtil.getXMLString(inDoc));
    	if (!YFCObject.isVoid(LocationPrefs)){
    		
    		//Prepare output for User exit. This will have Exception Zone to be considered as zone to do putaway.
    		outDoc = XMLUtil.createDocument(AcademyConstants.ELE_LOCATION_PREFERENCES);   		
    		Element eoutElem = outDoc.getDocumentElement();
    		
    		String sShipmentType = "";
    		
    		if(sStorageType.startsWith("CON")){								// for conveyable items
    			sShipmentType = "CON";
    		}else if(sItemType.equals("NSANHV")){							// for items which are non conveyable and non ship alone
        		sShipmentType = "CON";
    		}
    		else{															
    			sShipmentType = "BULK";
    		}
    		
    		//different exception zones configured for CON and BULK items in the Extended API component as an argument.
    		String sExceptionZones= props.getProperty(sShipmentType);   		
    		StringTokenizer st = new StringTokenizer(sExceptionZones,",");
    		
    		while (st.hasMoreTokens()){
    			Element eoutLocationPref = XMLUtil.createElement(outDoc, AcademyConstants.ELE_LOCATION_PREFERENCE, null);
        		eoutElem.appendChild(eoutLocationPref);
        		
        		Element eOutZoneElem =  XMLUtil.createElement(outDoc, AcademyConstants.ELE_ZONE, null);       		
        		eoutLocationPref.appendChild(eOutZoneElem);       		
        		eOutZoneElem.setAttribute(AcademyConstants.ATTR_ZONE_ID, st.nextToken());
    		}   		
    		eoutElem.setAttribute(AcademyConstants.ELE_NODE, inElem.getAttribute(AcademyConstants.ELE_NODE));
    		
    		log.verbose("Output from API is"+XMLUtil.getXMLString(outDoc));
    		log.endTimer(" End of AcademyStampExceptionLoc -> stampExceptionLocation()Api");
    	}    	
		return outDoc;   	
    }
}
