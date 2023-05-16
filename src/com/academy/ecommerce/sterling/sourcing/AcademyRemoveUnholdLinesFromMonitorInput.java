package com.academy.ecommerce.sterling.sourcing;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyRemoveUnholdLinesFromMonitorInput {
	
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyRemoveUnholdLinesFromMonitorInput.class);
	
	public  Document removeUnholdLinesFromMonitorInput(YFSEnvironment env, Document inDoc) throws Exception {
		
		Element eleOrderLines=null;
		Element eleOrderLine=null;
		NodeList nlOrdLine=null;
		
		log.verbose("Input XML:"+XMLUtil.getXMLString(inDoc));
		eleOrderLines=(Element) inDoc.getDocumentElement().getElementsByTagName("OrderLines").item(0);
		nlOrdLine = inDoc.getDocumentElement().getElementsByTagName("OrderLine");
		
		for(int i=0;i<nlOrdLine.getLength();i++) {	
			eleOrderLine=(Element) nlOrdLine.item(i);
			if(!YFCObject.isNull(eleOrderLine)) {
				String strHoldFlag=eleOrderLine.getAttribute("HoldFlag");
				if(strHoldFlag.equalsIgnoreCase("N")) {
					XMLUtil.removeChild(eleOrderLines, eleOrderLine);
					i--;	
				}
				
			}
		}
		log.verbose("Changed Input XML:"+XMLUtil.getXMLString(inDoc));
		
		return inDoc;
		
	}

}
