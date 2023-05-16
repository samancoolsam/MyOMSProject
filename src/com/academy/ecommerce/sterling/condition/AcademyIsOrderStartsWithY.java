package com.academy.ecommerce.sterling.condition;

import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyIsOrderStartsWithY implements YCPDynamicConditionEx {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyIsOrderStartsWithY.class);
	final String docType = "<!DOCTYPE Update_Academy_OrderStatus SYSTEM 'Update_Academy_OrderStatus_10.dtd'>";
	private Map propMap = null;
	public boolean evaluateCondition(YFSEnvironment arg0, String arg1, Map arg2, Document inDoc) {
		// TODO Auto-generated method stub
		log.debug("Begining of AcademyIsOrderStartsWithY -> evaluateCondition Api");
		String inDocString = XMLUtil.getXMLString(inDoc);
		
		inDocString = inDocString.replaceAll(docType, "");
		log.debug("After removing DOCTYPE : " + inDocString);
		try {
			Document doc = XMLUtil.getDocument(inDocString);
			Element orderNumberElem = (Element)doc.getDocumentElement().getElementsByTagName("OrderNumber").item(0);
			String orderNo = orderNumberElem.getTextContent();
			log.debug("OrderNo : " + orderNo);
			
			if(orderNo != null && orderNo.startsWith("Y")) {
				return true;
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	public void setProperties(Map arg0) {
		// TODO Auto-generated method stub
		
	}

}
