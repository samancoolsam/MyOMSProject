//package declaration
package com.academy.ecommerce.sterling.condition;

//import statements

//java util import statements
import java.util.Map;
import java.util.Properties;

//w3c import statements
import org.w3c.dom.Document;

//yantra import statements
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

//academy import statements
import com.academy.ecommerce.sterling.shipment.AcademySFSCheckForStoreShipment;
import com.academy.util.constants.AcademyConstants;


/** Description: Class AcademySFSCheckIfOnboardedStore gets the
 * ShipNode path configured as properties of the dynamic condition
  */
public class AcademySFSCheckIfOnboardedStore implements YCPDynamicConditionEx{

	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory.instance(AcademySFSCheckIfOnboardedStore.class);
	
	//Declaring variables
	private boolean result=false;
	private Map argMap;

	//Stores property:ShipNodePath configured in configurator
	public void setProperties(Map map) {
		argMap = map;
	}

	/**
	 * gets the Xpath of shipnode and stamps it in the properties
	 * of  AcademySFSCheckModelStore.
	 *
	 * @param env
	 *            Yantra Environment Context.
	 * @param Map
	 *            map
	 * @return result
	 * 			Boolean variable
	 */
	public boolean evaluateCondition(YFSEnvironment env, String str, Map map, Document inXml) {

		try
		{
			if (!YFCObject.isVoid(inXml))
			{
				//Fetching the Xpath
				String path =(String)argMap.get("ShipNodePath");

				//Setting the properties
				Properties prop = new Properties();
				prop.setProperty(AcademyConstants.KEY_ATTR_XPATH, path);
				AcademySFSCheckForStoreShipment academySFSCheckForStoreShipment = new AcademySFSCheckForStoreShipment();
				academySFSCheckForStoreShipment.setProperties(prop);

				//calling method isMOdelStore of the class AcademySFSCheckModelStore
				Document outDoc=academySFSCheckForStoreShipment.isStoreShipment(env, inXml);
				//fetching the value of attribute IsOnboardedStore
				String strIsOnboardedStore=outDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ONBOARD_STORE);

				//If teh value is Y, then return result as true
				if(strIsOnboardedStore.equals(AcademyConstants.ATTR_Y))
				{
					log.verbose("**********Condition returns TRUE*****************");
					result=true;
					return result;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
}



