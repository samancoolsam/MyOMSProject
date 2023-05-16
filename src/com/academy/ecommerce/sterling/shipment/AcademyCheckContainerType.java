package com.academy.ecommerce.sterling.shipment;

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;


/*
 * Dynamic Condition 
 * Called on ADD_TO_CONTAINER.ON_CONTAINER_PACK_PROCESS_COMPLETE
 * to check if container type is PALLET
 * author @muchil
 */



public class AcademyCheckContainerType implements YIFCustomApi { 
	
	public void setProperties(Properties props) {} 
    /*
     * Instance of logger
     */
    private static YFCLogCategory log = YFCLogCategory.instance(AcademyCheckContainerType.class);

    
    /* to check if container type is PALLET, if yes then 
     * throw exception on the pack station
     * */
	public void checkContainerType(YFSEnvironment env, Document inDoc) throws Exception {
		log.beginTimer(" Begining of AcademyCheckContainerType-> checkContainerType Api");
		log.verbose("Input Xml to checkContainerType API is" + XMLUtil.getXMLString(inDoc));

  	if (!YFCObject.isVoid(inDoc))
		{
			Element eleContainer = inDoc.getDocumentElement();
			String strContainerType=eleContainer.getAttribute("ContainerType");
			if ("Pallet".equals(strContainerType) )
			{
				log.verbose("Container Type 'Pallet' cannot be used for Packing");
				YFCException err = new YFCException();
				err.setAttribute(YFCException.ERROR_CODE, "EXTN_ACADEMY_09");
				err.setAttribute(YFCException.ERROR_DESCRIPTION, "Container Type 'Pallet' cannot be used for Packing");
				throw err;
			}
			log.verbose("Container type is "+strContainerType);
			}
		 	log.endTimer(" End of AcademyCheckContainerType-> checkContainerType Api");
		}
		
	   
}
