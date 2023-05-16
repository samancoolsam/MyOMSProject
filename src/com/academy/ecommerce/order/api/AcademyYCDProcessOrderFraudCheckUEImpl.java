package com.academy.ecommerce.order.api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.pca.ycd.japi.ue.YCDProcessOrderFraudCheckUE;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;

//This UE is Implemented for raising the Fraudlent Order Alert in case of Fraud Verification Failed Scenario.
public class AcademyYCDProcessOrderFraudCheckUEImpl implements
		YCDProcessOrderFraudCheckUE {
	private static YFCLogCategory log = YFCLogCategory
	.instance(AcademyYCDProcessOrderFraudCheckUEImpl.class);

	public Document processOrderFraudCheck(YFSEnvironment env, Document inXML)
			throws YFSUserExitException {
		// TODO Auto-generated method stub
		log
		.verbose("****** Entering into AcademyYCDProcessOrderFraudCheckUEImpl on Fraud Verification Failed Scenario -> processOrderFraudCheck() and input XML is:::"
				+ XMLUtil.getXMLString(inXML));
Element ordRootEle=inXML.getDocumentElement();
ordRootEle.setAttribute(AcademyConstants.ATTR_FRAUD_RESPONSE,AcademyConstants.FAILED);
		
Element fraudCheckResponseMessagesEle=inXML.createElement(AcademyConstants.ELE_FRAUDRES_MSGS);
Element fraudCheckResponseElem=inXML.createElement(AcademyConstants.ELE_FRAUDRES_MSG);
fraudCheckResponseElem.setAttribute(AcademyConstants.ATTR_TEXT,AcademyConstants.FRAUD_VERIFICATION_FAILED);
fraudCheckResponseMessagesEle.appendChild((Node)fraudCheckResponseElem);
ordRootEle.appendChild((Node)fraudCheckResponseMessagesEle);
log
.verbose("****** Exiting from AcademyYCDProcessOrderFraudCheckUEImpl -> processOrderFraudCheck() and output XML is:::"
		+ XMLUtil.getXMLString(inXML));
		return inXML;
	}

}
