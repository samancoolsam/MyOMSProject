//package Declaration
package com.academy.ecommerce.sterling.condition;

//import statements
//java util import statements
import java.util.Map;

//w3c import statements
import org.w3c.dom.Document;

//academy import statements
import com.academy.util.constants.AcademyConstants;

//yantra import statements
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * 
 * Class to check if Vertex-Lite is enabled
 * To Check Vertex-5 or Vertex-9 URL is enabled
 * OMNI-26977 : Vertex-Lite Enable Flag
 *
 */

public class AcademyIsPropertyEnabled implements YCPDynamicConditionEx{

	//Set the logger
	private static YFCLogCategory log = YFCLogCategory
		.instance(AcademyIsPropertyEnabled.class);
	private Map<String, String> conditionProp = null;
	@Override
	public boolean evaluateCondition(YFSEnvironment arg0, String arg1, Map arg2, Document arg3) {
		log.beginTimer(" Begining of AcademyIsPropertyEnabled-> evaluateCondition Api");
		// Displaying the Map 
        log.debug("Initial Property Mappings are: " + conditionProp);
		if (conditionProp != null) {
	        String propertyValue = conditionProp.get("Property");
		
		log.verbose("Is Property Enabled :: " + YFSSystem.getProperty(propertyValue));
		if (!YFCObject.isVoid(YFSSystem.getProperty(propertyValue)) && 
				AcademyConstants.STR_YES.equalsIgnoreCase(YFSSystem.getProperty(propertyValue))) {
			log.debug("Returning TRUE");
			log.endTimer("End of AcademyIsPropertyEnabled-> evaluateCondition Api");
			return true;
		}
		else{
			log.debug("Returning FALSE");
			log.endTimer("End of AcademyIsPropertyEnabled-> evaluateCondition Api");
			return false;
		}
		}
		return false;
	}

	@Override
	public void setProperties(Map arg0) {
		// TODO Auto-generated method stub
		conditionProp = arg0;
	}

}
