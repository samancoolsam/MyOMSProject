/**
 * 
 */
package com.academy.ecommerce.sterling.alertConsole.extn;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.util.AcademyConstants;
import com.academy.ecommerce.sterling.util.XMLUtil;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCXmlUtils;

/**
 * @author stummala
 *
 */
public class AcademyAlertTreeExtnBehavior extends YRCExtentionBehavior {

	/* (non-Javadoc)
	 * @see com.yantra.yfc.rcp.YRCExtentionBehavior#preCommand(com.yantra.yfc.rcp.YRCApiContext)
	 */
	@Override
	public boolean preCommand(YRCApiContext apiContext) {
		String[] apiNames = apiContext.getApiNames();
		for(int i=0; i<apiNames.length; i++){
			YRCPlatformUI.trace(" API name is : "+apiNames[i]);
			if(apiNames[i].equalsIgnoreCase("getAlertStatisticsForUser")){
				Document inputDoc = apiContext.getInputXmls()[i];
				Element eleInBox = (Element)inputDoc.getDocumentElement().getElementsByTagName("Inbox").item(0);
				Element eleExtn = inputDoc.createElement(AcademyConstants.ELE_EXTN);
				eleExtn.setAttribute("ExtnNonCCExceptionType", "N");
				eleInBox.appendChild(eleExtn);
				YRCPlatformUI.trace(" Input xml is : "+XMLUtil.getXMLString(apiContext.getInputXmls()[i]));
			}
		}
		return super.preCommand(apiContext);
	}	
}
