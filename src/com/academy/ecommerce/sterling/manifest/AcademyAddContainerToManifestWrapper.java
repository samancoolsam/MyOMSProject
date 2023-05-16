package com.academy.ecommerce.sterling.manifest;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.xml.XMLUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
/**
 * This class is responsible to add container to manifest at the time of clicking Finish pack from store UI.
 * @author Sanchit
 * 
 * BOPIS-123 : SFS Shipment confirmation and Manifest in webStore
 *
 */
public class AcademyAddContainerToManifestWrapper {
	
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyAddContainerToManifestWrapper.class);

	public Document addContainerToManifest(YFSEnvironment env,Document inDoc) throws Exception {
		
		log.beginTimer("AcademyAddContainerToManifestWrapper::addContainerToManifest");
		log.verbose("Entering the method AcademyAddContainerToManifestWrapper.addContainerToManifest");
		//Prod ISSUE fix for Reprint Shipping Label : Begin
		env.setTxnObject(AcademyConstants.TXN_OBJ_FINISH_PACK,"Y");
		//Prod ISSUE fix for Reprint Shipping Label : End
		Element eleConatiner = inDoc.getDocumentElement();
		String strShipmentKey=eleConatiner.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		Document addContainerToManifestOP=null;
		Document getShipmentContainerListOP = AcademyUtil.invokeService
				(env, AcademyConstants.SER_GET_SHIPMENT_CONTAINER_LIST , inDoc);
		
		Element eleConatiners = getShipmentContainerListOP.getDocumentElement();
		NodeList nlcontainer = eleConatiners.getElementsByTagName(AcademyConstants.ELE_CONTAINER);
				
		for (int i = 0; i < nlcontainer.getLength(); i++)
		{
		Element elecontainer = (Element) nlcontainer.item(i);	
		Document doccontainer=XMLUtil.getDocumentForElement(elecontainer);
		addContainerToManifestOP=AcademyUtil.invokeService
		(env, AcademyConstants.SER_ACADEMY_ADD_CONTAINER_TO_MANIFEST , doccontainer);
		
		}
		
		log.endTimer("AcademyAddContainerToManifestWrapper::addContainerToManifest");
		log.verbose("Check the output doc returned: =" +XMLUtil.getXMLString(addContainerToManifestOP));
		return addContainerToManifestOP;
	}
	
	

}
