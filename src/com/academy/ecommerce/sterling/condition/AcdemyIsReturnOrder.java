package com.academy.ecommerce.sterling.condition;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcdemyIsReturnOrder implements YCPDynamicConditionEx {
	private static YFCLogCategory log = YFCLogCategory.instance(AcdemyIsReturnOrder.class);
	private Map propMap = null;
	
	/**
	 *  This method evaluate the Order # starts with Y to skip the status updates to WCS. 
	 */
	public boolean evaluateCondition(YFSEnvironment arg0, String arg1, Map arg2, Document inputDoc) {
		log.beginTimer("Begining of AcdemyIsReturnOrder-> evaluateCondition Api");
		boolean isReturnOrder = false;
		Element receiptLineElement = (Element) inputDoc.getDocumentElement().getElementsByTagName("ReceiptLine").item(0);
		String orderNo = receiptLineElement.getAttribute("OrderNo");
		if(orderNo != null && orderNo.startsWith("Y")) {
			return true;
		}
		log.verbose("Is OrderNo starts with Y : " + isReturnOrder);
		return false;
	}
	
	public void setProperties(Map propMap) {
		this.propMap = propMap;
	}
}
