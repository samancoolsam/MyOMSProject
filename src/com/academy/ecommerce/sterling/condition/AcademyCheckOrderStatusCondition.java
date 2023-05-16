package com.academy.ecommerce.sterling.condition;

import java.util.Iterator;
import java.util.Map;

import com.yantra.ycp.japi.YCPDynamicCondition;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyCheckOrderStatusCondition implements YCPDynamicCondition {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyCheckOrderStatusCondition.class);
	public boolean evaluateCondition(YFSEnvironment env, String sName,
			Map mapData, String sXMLData) {
		log.beginTimer(" Begin of AcademyCheckOrderStatusCondition-> evaluateCondition Api");
		YFCDocument inXML=YFCDocument.getDocumentFor(sXMLData);
		YFCElement eIn=inXML.getDocumentElement();
		YFCElement eOrderLines=eIn.getChildElement("OrderLines");
		if(eOrderLines!=null){
			Iterator iteratorOrderLines = eOrderLines.getChildren();
    		if(iteratorOrderLines!=null){
    			while(iteratorOrderLines.hasNext()){
    				YFCElement eOrderLine = (YFCElement) iteratorOrderLines.next();
    				if(Double.parseDouble(eOrderLine.getAttribute("MaxLineStatus"))>=3700){
    					return true;
    				}
    			}
    		}
		}
		log.endTimer(" Ending of AcademyCheckOrderStatusCondition-> evaluateCondition Api");
		return false;
	}

}
