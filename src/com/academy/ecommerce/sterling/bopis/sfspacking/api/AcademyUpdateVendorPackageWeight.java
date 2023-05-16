package com.academy.ecommerce.sterling.bopis.sfspacking.api;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.cts.sterling.custom.accelerators.util.XMLUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfs.japi.YFSEnvironment;


public class AcademyUpdateVendorPackageWeight {
	
	public  Document updateVendorPackageWeight(YFSEnvironment env, Document inXML) throws Exception{
		
		Document changeShipmentdoc=null;
		
		Element eleShipment=inXML.getDocumentElement();
		String strShipmentKey=eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		String strShipmentContainerKey=eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY);
		
		NodeList itemList = inXML.getElementsByTagName("Item");
		if (itemList != null && itemList.getLength() > 0) {
			Element containerVolumeItem = (Element) itemList.item(0);
			
			NodeList primaryInfoList = containerVolumeItem.getElementsByTagName("PrimaryInformation");
			if (primaryInfoList != null && primaryInfoList.getLength() > 0) {
				Element containerVolumePrimaryInformation = (Element) primaryInfoList.item(0);
			String strunitWeight = containerVolumePrimaryInformation.getAttribute("UnitWeight");
			
			if(!YFCObject.isVoid(strunitWeight)){
			Document changeShipmentDoc=XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
			Element elechangeShipmentDoc=changeShipmentDoc.getDocumentElement();
			elechangeShipmentDoc.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
			Element eleContainers =  SCXmlUtil.createChild(elechangeShipmentDoc,AcademyConstants.ELE_CONTAINERS);
			Element eleContainer =  SCXmlUtil.createChild(eleContainers,AcademyConstants.ELE_CONTAINER);
			eleContainer.setAttribute(AcademyConstants.ATTR_ACTUAL_WEIGHT, strunitWeight);
			eleContainer.setAttribute(AcademyConstants.ATTR_CONTAINER_GROSS_WEIGHT, strunitWeight);
			eleContainer.setAttribute(AcademyConstants.ATTR_CONTAINER_NET_WEIGHT, strunitWeight);
			eleContainer.setAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY, strShipmentContainerKey);
			
			changeShipmentdoc=AcademyUtil.invokeService(env, AcademyConstants.ACADEMY_CHANGE_SHIPMENT_FOR_UPDATE_WEIGHT, changeShipmentDoc);
		
			}
		
	}
			
		}
		
		return changeShipmentdoc;
	}
}

