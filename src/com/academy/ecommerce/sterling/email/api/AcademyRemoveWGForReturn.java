package com.academy.ecommerce.sterling.email.api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;

public class AcademyRemoveWGForReturn {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyRemoveWGForReturn.class);
	
	public Document removeWhiteGloveLines(Document inDoc) {
		log.beginTimer(" Begining of AcademyRemoveWGForReturn-> removeWhiteGloveLines Api");
		log.verbose("***** removing white glove lines *******");
		try {
			Element eleOrderLines = (Element) inDoc.getElementsByTagName(
					"OrderLines").item(0);			
			NodeList nlOrderLines = XPathUtil.getNodeList(inDoc,
					AcademyConstants.XPATH_ORDER_ORDERLINE);
			if (!YFCObject.isVoid(nlOrderLines)) {
				for (int i = 0; i < nlOrderLines.getLength(); i++) {
					Element currOrderLine = (Element) nlOrderLines.item(i);				
					Element itemElem = (Element) currOrderLine
							.getElementsByTagName(AcademyConstants.ELE_ITEM_DETAILS).item(0);
					if (itemElem != null && itemElem.hasChildNodes()) {
						Element itemExtnElem = (Element) itemElem
								.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
						if (itemExtnElem != null && itemExtnElem.hasAttributes()
								&& (AcademyConstants.STR_YES).equals(itemExtnElem
										.getAttribute(AcademyConstants.ATTR_ITEM_IS_WHITE_GLOVE))) {
							XMLUtil.removeChild(eleOrderLines, currOrderLine);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.verbose("Exception in AcademyRemoveWGForReturn.removeWhiteGloveLines()");
		}		
		log.verbose("After removing WhiteGlove lines: " + XMLUtil.getXMLString(inDoc));
		log.endTimer(" End of AcademyRemoveWGForReturn-> removeWhiteGloveLines Api");
		return inDoc;

	}

}