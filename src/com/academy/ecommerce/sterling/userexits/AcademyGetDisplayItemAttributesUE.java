package com.academy.ecommerce.sterling.userexits;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.StringUtil;
import com.yantra.ycm.japi.ue.YCMGetDisplayItemAttributesUE;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;

public class AcademyGetDisplayItemAttributesUE implements YCMGetDisplayItemAttributesUE {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyGetDisplayItemAttributesUE.class);

    public Document getDisplayItemAttributes(YFSEnvironment arg0, Document inDoc) throws YFSUserExitException {
    	
    	log.beginTimer(" Begining of AcademyGetDisplayItemAttributesUE ->  getDisplayItemAttributes () Api");
        Element inputDocEle = inDoc.getDocumentElement();
        
        Element primaryInfoEle = (Element) inputDocEle.getElementsByTagName("PrimaryInformation").item(0);
        Element extnElement = (Element) inputDocEle.getElementsByTagName("Extn").item(0);
        
        String extnSizeCodeDescription = extnElement.getAttribute("ExtnSizeCodeDescription");
        String extnIPDescription = extnElement.getAttribute("ExtnIPItemDescription");
        String extnColorCodeDescription = extnElement.getAttribute("ExtnColorCodeDescription");
        
        /*
         * CR# 33
         * Logic for computing DisplayItemId and DisplayItemDescriptoin
         * 
         * DisplayItemId: (First four characters from ‘ExtnSizeCodeDescription’)-
         * 			(15 characters from ‘ExtnIPItemDescription’) = 20 characters 
		 * DisplayItemDescription: (16th to 25th Character from ‘ExtnIPItemDescription’)-
		 *  		(First nine characters from ‘ExtnColorDescription’) = 20 characters.
		 * 
		 * If any of ExtnSizeCodeDescription, ExtnIPItemDescription and ExtnColorCodeDescription 
		 * is empty or length less than the required, still it is considered with the available length.  
		 * After computing these fields, if any of these has empty value, still same is 
		 * considered for display.
         */
        
        StringBuffer academyItemID = new StringBuffer();
        if (!StringUtil.isEmpty(extnSizeCodeDescription)) {
        	if(extnSizeCodeDescription.length() > 4) {
        		academyItemID.append(extnSizeCodeDescription.substring(0, 4));
        	} else {
        		academyItemID.append(extnSizeCodeDescription);
        	}
        }
        academyItemID.append("-");
        if (!StringUtil.isEmpty(extnIPDescription)) {
       		if(extnIPDescription.length() > 15) {
       			academyItemID.append(extnIPDescription.substring(0, 15));
       		} else {
       			academyItemID.append(extnIPDescription);
       		}
		}
        if(academyItemID.equals("-")) {
        	academyItemID.deleteCharAt(0);
        }
        StringBuffer academyItemDesc = new StringBuffer();
        if (!StringUtil.isEmpty(extnIPDescription)) {
        	if(extnIPDescription.length() > 25) {
        		academyItemDesc.append(extnIPDescription.substring(15, 25));
        	} else if (extnIPDescription.length() > 15){
        		academyItemDesc.append(extnIPDescription.substring(15));
        	}
        }
        academyItemDesc.append("-");
        if (!StringUtil.isEmpty(extnColorCodeDescription)) {
       		if(extnColorCodeDescription.length() > 9) {
       			academyItemDesc.append(extnColorCodeDescription.substring(0, 9));
       		} else {
       			academyItemDesc.append(extnColorCodeDescription);
       		}
		}
        if(academyItemDesc.equals("-")) {
        	academyItemDesc.deleteCharAt(0);
        }
		inputDocEle.setAttribute("DisplayItemId", academyItemID.toString());
		primaryInfoEle.setAttribute("DisplayItemDescription", academyItemDesc.toString());
		log.endTimer(" End of AcademyGetDisplayItemAttributesUE ->  getDisplayItemAttributes () Api");
        return inDoc;
	}
 }
