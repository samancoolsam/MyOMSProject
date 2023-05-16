package com.academy.ecommerce.sterling.manifest;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyCheckForLastContainer {

	public Document checkForLastContainer(YFSEnvironment env, Document inDoc) throws Exception {
		
		boolean printBOL = true;
		Element containerEle = inDoc.getDocumentElement();
		String shipmentKey = containerEle.getAttribute("ShipmentKey");
		
		Document inGetShipList = XMLUtil.createDocument("Shipment");
		inGetShipList.getDocumentElement().setAttribute("ShipmentKey", shipmentKey);
		
		Document outGetShipList = AcademyUtil.invokeService(env, "AcademyGetShipmentListForBOL", inGetShipList);
		
		Element outShipEle = outGetShipList.getDocumentElement();
		NodeList outContNL = outShipEle.getElementsByTagName("Container");
		
		for (int i = 0; i < outContNL.getLength(); i++) {
			Element outContEle = (Element) outContNL.item(i);
			String manifestNo = outContEle.getAttribute("ManifestNo");
			
			if (YFCObject.isNull(manifestNo)) {
				printBOL = false;
			}
		}
		
		containerEle.setAttribute("PrintBOL", "" + printBOL);
		return inDoc;
		
	}
}
