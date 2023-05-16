//package declaration
package com.academy.ecommerce.sterling.item.api;
//java util import statements
import java.util.Properties;
import java.util.HashMap;
//w3c import statements
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
//academy import statements
import com.academy.ecommerce.sterling.shipment.AcademySFSPrintPendingShipments;
import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
//yantra import statements
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;


/**
 * Description: Class AcademySFSProcessPrintItemPickTickets gets the list of all
 * items against the requested shipnode.
 *
 * @throws Exception
 */
public class AcademyCheckForHazmatItem implements YIFCustomApi
{
	//log set up 
	private static final YFCLogCategory	log	= YFCLogCategory.instance(AcademyCheckForHazmatItem.class);
	// Instance to store the properties configured for the condition in
	// Configurator
	private Properties	prop;
	// Stores property configured in configurator
	public void setProperties(Properties prop) throws Exception
	{
		this.prop = prop;
	}
	/**
	 * Class to check if it is an HAZMAT item.
	 * This is developed as part of JIRAs WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
	 * @param env
	 *            Yantra Environment Context.
	 * @param inXML
	 *            Input Document Value.
	 * @return Document inXML
	 * @throws Exception 
	 * @author C0007098
	 * 	 */
	public static Document checkForHazmatItem(YFSEnvironment env, Document inXML) throws Exception
	{
		log.verbose("Entering the method AcademyCheckForHazmatItem.checkForHazmatItem ");
		
		Element eleItems = null;
		Element eleItem = null;
		String strHazmatClass = null;
		HashMap hmHazmatCommonCodeList = new HashMap();
	    String strCodeType = AcademyConstants.HAZMAT_CLASS_COMMON_CODE;
	    String strOrganizationCode = AcademyConstants.PRIMARY_ENTERPRISE;
	    
		eleItems = inXML.getDocumentElement();
		eleItem = (Element) eleItems.getElementsByTagName(AcademyConstants.ELEM_ITEM).item(0);
		strHazmatClass = eleItem.getAttribute(AcademyConstants.ATTR_HAZMAT_CLASS);
		if (!YFCObject.isVoid(strHazmatClass) && !strHazmatClass.equals(AcademyConstants.AMMO_HAZMAT_CLASS))
		{
			log.verbose("Its an Hazmat Item");
			 hmHazmatCommonCodeList = AcademyCommonCode.getCommonCodeListAsHashMap(env, strCodeType, strOrganizationCode);
	            if (hmHazmatCommonCodeList.containsKey(strHazmatClass)) {
	            	eleItem.setAttribute(AcademyConstants.ATTR_EXTN_IS_HAZMAT, AcademyConstants.STR_YES);
	            }	            
		}	
		log.verbose("Output of the Method AcademyCheckForHazmatItem ::" + XMLUtil.getXMLString(inXML));
		return inXML;
	}


}