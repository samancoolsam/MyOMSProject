package com.academy.ecommerce.sterling.sourcing;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyResolveSourcingHold {
	
	
private static YFCLogCategory log = YFCLogCategory.instance(AcademyRemoveUnholdLinesFromMonitorInput.class);
	
	public  Document resolveSourcingHold(YFSEnvironment env, Document inDoc) throws Exception {
		
		log.verbose("Input XML:"+XMLUtil.getXMLString(inDoc));
		
		Element eleOrder=(Element) inDoc.getDocumentElement().getElementsByTagName("Order").item(0);
		
		NodeList nlOrdLine = inDoc.getDocumentElement()
				.getElementsByTagName("OrderLine");
		
		Document changeOrderInDoc=XMLUtil.createDocument("Order");
		Element elechangeOrderInDoc=changeOrderInDoc.getDocumentElement();
		elechangeOrderInDoc.setAttribute("Override", "Y");
		elechangeOrderInDoc.setAttribute("OrderHeaderKey", eleOrder.getAttribute("OrderHeaderKey"));
		
		Element eleChgOrdLines=changeOrderInDoc.createElement("OrderLines");

		for (int i = 0; i < nlOrdLine.getLength(); i++) {
			Element eleOrdLine = (Element) nlOrdLine.item(i);
			if (!YFCObject.isNull(eleOrdLine)) {
				Element eleChgOrdLine=changeOrderInDoc.createElement("OrderLine");
				eleChgOrdLine.setAttribute("OrderLineKey", eleOrdLine.getAttribute("OrderLineKey"));
				eleChgOrdLine.setAttribute("Action", "MODIFY");
				Element eleOrderHoldTypes=changeOrderInDoc.createElement("OrderHoldTypes");
				Element eleOrderHoldType=changeOrderInDoc.createElement("OrderHoldType");
				eleOrderHoldType.setAttribute("ReasonText", "ShipNodeCheck");
				eleOrderHoldType.setAttribute("Status", "1300");
				eleOrderHoldType.setAttribute("HoldType", "ACADEMY_SCHREL_HOLD");
				eleOrderHoldTypes.appendChild(eleOrderHoldType);
				eleChgOrdLine.appendChild(eleOrderHoldTypes);
				eleChgOrdLines.appendChild(eleChgOrdLine);
			}
		}
		elechangeOrderInDoc.appendChild(eleChgOrdLines);
		log.verbose("ChangeOrder Input XML:"+XMLUtil.getXMLString(changeOrderInDoc));
		
		Document outDoc=AcademyUtil.invokeAPI(env, "changeOrder", changeOrderInDoc);
		
		return outDoc;
		
	}

}
