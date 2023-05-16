package com.academy.ecommerce.sterling.condition;

import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import com.yantra.ycp.japi.YCPDynamicCondition;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;

public class AcademyValidateProfilePublishCondition implements
		YCPDynamicCondition {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyValidateProfilePublishCondition.class);

	public boolean evaluateCondition(YFSEnvironment env, String name,
			Map mapData, String xmlData) {
		boolean strCheckCondition = false;
		try {
			log.beginTimer(" Begining of AcademyValidateProfilePublishCondition-> evaluateCondition Api");
			YFCDocument xml = YFCDocument.getDocumentFor(xmlData);
			YFCElement eleManageCustomerOutput = xml.getDocumentElement();
			String attrVal = xml.getDocumentElement().getAttribute(
					"ExternalCustomerID");
			YFCElement eleExtn =  eleManageCustomerOutput
					.getElementsByTagName(AcademyConstants.ELE_CUSTOMER_CONTACT_LIST).item(0);
			YFCElement eleExtn1 = eleExtn.getElementsByTagName(
					AcademyConstants.ELE_CUSTOMER_CONTACT).item(0);
			YFCElement eleExtn2 =  eleExtn1.getElementsByTagName(AcademyConstants.ELE_EXTN)
					.item(0);
			String attrVal1 = eleExtn2.getAttribute("ExtnRequiresWebprofileID");
			if (YFCObject.isVoid(attrVal) && (attrVal1.equals(AcademyConstants.STR_YES))) {
				strCheckCondition = true;
			} else {
				strCheckCondition = false;
			} 
			log.endTimer(" End of AcademyValidateProfilePublishCondition-> evaluateCondition Api");
		} catch (Exception ex) {
			
			throw new YFSException("Failed Condition" +ex.getMessage());

		}
		return strCheckCondition;
	}
}
