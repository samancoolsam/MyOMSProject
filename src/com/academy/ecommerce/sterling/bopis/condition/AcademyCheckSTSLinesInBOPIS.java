
package com.academy.ecommerce.sterling.bopis.condition;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyCheckSTSLinesInBOPIS implements YCPDynamicConditionEx {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyCheckSTSLinesInBOPIS.class);
	private Map propMap = null;
	Document getOrderListInDoc = null;
	boolean saveTheSaleLineExist = false;

	/**
	 * OMNI-30146 This method return true if Save The Sale lines exits in
	 * BOPIS order and if they are not yet cancelled.
	 */
	public boolean evaluateCondition(YFSEnvironment env, String str, Map map, Document inXml) {
		log.beginTimer("AcademyCheckSTSLinesInBOPIS-> evaluateCondition - START");
		// boolean isSaveTheSaleLineExist = false;

		try {
			if (!YFCObject.isVoid(inXml)) {
				Element shipmentElement = inXml.getDocumentElement();
				String orderNo = shipmentElement.getAttribute(AcademyConstants.ATTR_ORDER_NO);
				getOrderListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
				getOrderListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_NO, orderNo);
				getOrderListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_DOC_TYPE,
						AcademyConstants.SALES_DOCUMENT_TYPE);
				getOrderListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE,
						AcademyConstants.PRIMARY_ENTERPRISE);

				Document outputTemplate = YFCDocument.getDocumentFor(
						"<OrderList> <Order> <OrderLines> <OrderLine FulfillmentType='' MaxLineStatus=''> <Extn ExtnOriginalFulfillmentType=''/> </OrderLine> </OrderLines> </Order> </OrderList>")
						.getDocument();
				env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, outputTemplate);
				Document getOrderListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST,
						getOrderListInDoc);
				env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
				log.verbose("getOrderList API output is :: " + XMLUtil.getXMLString(getOrderListOutDoc));
				if (!YFCObject.isVoid(getOrderListOutDoc)) {
					NodeList nlSaveTheSaleLines = XPathUtil.getNodeList(getOrderListOutDoc.getDocumentElement(),
							"/OrderList/Order/OrderLines/OrderLine[@MaxLineStatus!='9000']/Extn[@ExtnOriginalFulfillmentType='BOPIS']");
					NodeList nlASEligibleLines = XPathUtil.getNodeList(getOrderListOutDoc.getDocumentElement(),
							"/OrderList/Order/OrderLines/OrderLine[@MaxLineStatus!='9000' and @FulfillmentType='BOPIS']");
					int noOfSaveTheSaleLines = nlSaveTheSaleLines.getLength();
					int noOfASEligibleLines = nlASEligibleLines.getLength();
					log.verbose("Number of STS eligible lines which are not in cancelled status :::"
							+ noOfSaveTheSaleLines);
					log.verbose("Number of lines which are eligible for AS functionality and not in cancelled status :::"
							+ noOfASEligibleLines);
					if (noOfSaveTheSaleLines > 0) {
						saveTheSaleLineExist = true;
					}else if(noOfASEligibleLines > 0) {
						saveTheSaleLineExist = true;
					}

				}
				log.verbose(
						"The value of saveTheSaleLineExist at the end of AcademyCheckSTSLinesInBOPIS.evaluateCondition() is  ::"
								+ saveTheSaleLineExist);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return saveTheSaleLineExist;

	}

	public void setProperties(Map propMap) {
		this.propMap = propMap;
	}
}
