package com.academy.ecommerce.sterling.condition;

/*##################################################################################
* Project Name                : CLS Returns
* Module                      : OMS
* Author                      : CTS
* Date                        : 15-JULY-2022 
* Description                 : Dynamic Condition to validate CLS Return Node
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author                Version#       Remarks/Description                      
* ---------------------------------------------------------------------------------
* 15-JULY-2022      CTS                   1.0               Initial version
* ##################################################################################*/
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyIsCLSShipNode implements YCPDynamicConditionEx {
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyIsCLSShipNode.class);
	String strCLSShipNode = null;
	String strShipNode = null;
	String strDraftOrderFlag = null;

	@Override
	public boolean evaluateCondition(YFSEnvironment env, String arg1, Map map, Document inDoc) {
		boolean flag = false;
		try {
			strCLSShipNode = AcademyCommonCode.getCodeValue(env, AcademyConstants.STR_CLS_RETURN_NODE_VALUE,
					AcademyConstants.STR_ACTIVE, AcademyConstants.HUB_CODE);
			Element eleOrder = inDoc.getDocumentElement();
			strDraftOrderFlag = eleOrder.getAttribute(AcademyConstants.DRAFT_ORDER_FLAG);
			Element eleOrderLine = (Element) eleOrder.getElementsByTagName("OrderLine").item(0);

			strShipNode = eleOrderLine.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
			log.verbose("Order Draft :" + strDraftOrderFlag + "Order Line Shipnode :" + strShipNode
					+ "CLSShipNodeCommoncode  :: " + strCLSShipNode);

			if ((YFCCommon.equals(strCLSShipNode, strShipNode))
					|| (AcademyConstants.ATTR_Y.equals(strDraftOrderFlag))) {
				log.verbose("AcademyIsCLSShipNode.evaluateCondition() :: true");
				return flag = true;
			}
		} catch (Exception e) {
			log.error("Exception caught is::" + e);
		}
		return flag;
	}

	@Override
	public void setProperties(Map arg0) {
		// TODO Auto-generated method stub

	}

}
