package com.academy.ecommerce.item.api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyStampItemExtnToBOL {
	
	public Document stampItemExtnToBOL(YFSEnvironment env, Document inDoc){
		
		Element shipmentEle = inDoc.getDocumentElement();
		NodeList shipmentLineNL = shipmentEle.getElementsByTagName("ShipmentLine");
		
		try {
			
		for (int shipmentLineNLCount = 0; shipmentLineNLCount < shipmentLineNL.getLength(); shipmentLineNLCount++) {
			
			Element shipmentLineEle = (Element) shipmentLineNL.item(shipmentLineNLCount);
			Element itemEle = (Element) shipmentLineEle.getElementsByTagName("Item").item(0);
			String itemID = itemEle.getAttribute("ItemID");
			//String NMFCDesc = itemEle.getAttribute("NMFCDescription");
			
			Document inputGetItemList = XMLUtil.createDocument("Item");
			inputGetItemList.getDocumentElement().setAttribute("ItemID", itemID);
			Document outGetItemList = AcademyUtil.invokeService(env, "AcademyGetItemExtnForBOL", inputGetItemList);
			Element outputItemEle = outGetItemList.getDocumentElement();
			Element extnItemEle = (Element)outputItemEle.getElementsByTagName("Extn").item(0);
			String NMFCDesc = extnItemEle.getAttribute("ExtnNMFCDescription");
			itemEle.setAttribute("NMFCDescription", NMFCDesc);
			
			Node dup = inDoc.importNode(extnItemEle, false);
			itemEle.appendChild(dup);
			
		
		}

		} catch (Exception e) {
			// TODO: handle exception
		}

		
		return inDoc;
	}

}
