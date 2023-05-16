package com.academy.ecommerce.sterling.bopis.inventory.api;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyPublishNodeLevelFullSync {
	
	private static final YFCLogCategory	log	= YFCLogCategory.instance(AcademyPublishNodeLevelFullSync.class);
	//Define properties to fetch argument value from service configuration
		private Properties props;
		public void setProperties(Properties props) throws Exception {
			this.props = props;
		}

		public  Document publishNodeLevelFullSync (YFSEnvironment env, Document inXML) {
			
			log.beginTimer("AcademyPublishNodeLevelFullSync::publishNodeLevelFullSync");
			log.verbose("Entering the method AcademyPublishNodeLevelFullSync.publishNodeLevelFullSync ");
			log.verbose("Input XML for AcademyPublishNodeLevelFullSync : =" +XMLUtil.getXMLString(inXML));
			
			String strSuppressZeroForFullSync =props.getProperty(AcademyConstants.ATTR_SUPPRESS_ZERO_FOR_FULLSYNC);
			
			if(AcademyConstants.STR_YES.equalsIgnoreCase(strSuppressZeroForFullSync))
			{
				Element eleInventoryMonitor=(Element) inXML.getDocumentElement().getElementsByTagName
						(AcademyConstants.ELE_INVENTORY_MONITOR).item(0);
				NodeList nlAvailabilityChange=inXML.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_AVAILABILITY_CHANGE);
				
				for (int i = 0; i < nlAvailabilityChange.getLength(); i++) {
					
					Element eleAvailabilityChange = (Element) nlAvailabilityChange.item(i);
					String strOnhandAvailableQuantity=eleAvailabilityChange.getAttribute(AcademyConstants.ATTR_ONHAND_AVAILABLE_QUANTITY);
					Double dOnhandAvailableQuantity =  Double.parseDouble(strOnhandAvailableQuantity);
					if(dOnhandAvailableQuantity==0.00)
					{
						XMLUtil.removeChild(eleInventoryMonitor,eleAvailabilityChange);
						i--;
					}
			}
			
			
	

	}
			log.endTimer("AcademyPublishNodeLevelFullSync::publishNodeLevelFullSync");
			log.verbose("Output XML for AcademyPublishNodeLevelFullSync : =" +XMLUtil.getXMLString(inXML));
			return inXML;

}
}

