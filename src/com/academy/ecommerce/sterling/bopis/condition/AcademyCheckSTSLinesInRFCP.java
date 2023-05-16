package com.academy.ecommerce.sterling.bopis.condition;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyCheckSTSLinesInRFCP implements YCPDynamicConditionEx {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyCheckSTSLinesInRFCP.class);
	private Map propMap = null;
	Document getOrderListInDoc = null;
	boolean saveTheSaleLineExist = false;
	private final String GET_SHIPMENT_LIST_OUT_TEMP = "<Shipments>"
			+ "<Shipment ShipmentKey='' ShipmentNo='' ShipmentType='' Status='' >" + "<ShipmentLines>"
			+ "<ShipmentLine ActualQuantity='' OverShipQuantity='' Quantity='' OrderNo=''/>" + "</ShipmentLines>"
			+ "</Shipment>" + "</Shipments>";

	/**
	 * OMNI-30146 This method return true if Save The Sale lines exits in BOPIS
	 * order and if they are not yet cancelled.
	 */
	public boolean evaluateCondition(YFSEnvironment env, String str, Map map, Document inXml) {
		log.beginTimer("AcademyCheckSTSLinesInBOPIS-> evaluateCondition - START");
		// boolean isSaveTheSaleLineExist = false;

		try {
			if (!YFCObject.isVoid(inXml)) {
				Element shipmentElement = inXml.getDocumentElement();
				String orderNo = shipmentElement.getAttribute(AcademyConstants.ATTR_ORDER_NO);
				if (YFCObject.isVoid(orderNo)) {
					orderNo = SCXmlUtil.getXpathAttribute(shipmentElement, "ShipmentLines/ShipmentLine/@OrderNo");
					String strShipmentKey = shipmentElement.getAttribute("ShipmentKey");
					log.debug("OrderNo" + orderNo);
					if (YFCObject.isVoid(orderNo)) {
						if (!YFCObject.isVoid(strShipmentKey)) {
							Document docGetShipmentListInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
							Element eleRoot = docGetShipmentListInput.getDocumentElement();
							eleRoot.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);
							log.debug("getShpmentList Template"
									+ SCXmlUtil.getString(XMLUtil.getDocument(GET_SHIPMENT_LIST_OUT_TEMP)));
							log.debug("getShpmentList Input" + SCXmlUtil.getString(docGetShipmentListInput));
							env.setApiTemplate("getShipmentList", XMLUtil.getDocument(GET_SHIPMENT_LIST_OUT_TEMP));
							log.debug("Template XML is" + GET_SHIPMENT_LIST_OUT_TEMP);

							log.debug("Input xml for getShipmentList api:"
									+ com.academy.util.xml.XMLUtil.getXMLString(docGetShipmentListInput));
							Document docgetShipmentListOutput = AcademyUtil.invokeAPI(env,
									AcademyConstants.API_GET_SHIPMENT_LIST, docGetShipmentListInput);
							orderNo = SCXmlUtil.getXpathAttribute(docgetShipmentListOutput.getDocumentElement(),
									"Shipment/ShipmentLines/ShipmentLine/@OrderNo");
							log.debug("Output xml for getShipmentList api:"
									+ com.academy.util.xml.XMLUtil.getXMLString(docgetShipmentListOutput));
							env.clearApiTemplate("getShipmentList");
						} else {

							return false;
						}
					}
				}
				getOrderListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
				getOrderListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_NO, orderNo);
				getOrderListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_DOC_TYPE,
						AcademyConstants.SALES_DOCUMENT_TYPE);
				getOrderListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE,
						AcademyConstants.PRIMARY_ENTERPRISE);

				Document outputTemplate = YFCDocument.getDocumentFor(
						"<OrderList> <Order> <OrderLines> <OrderLine MaxLineStatus='' MinLineStatus=''> <Extn ExtnOriginalFulfillmentType=''/> </OrderLine> </OrderLines> </Order> </OrderList>")
						.getDocument();
				log.debug(SCXmlUtil.getString(outputTemplate));
				env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, outputTemplate);
				Document getOrderListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST,
						getOrderListInDoc);
				env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
				log.debug("getOrderList API output is :: " + XMLUtil.getXMLString(getOrderListOutDoc));
				if (!YFCObject.isVoid(getOrderListOutDoc)) {
					NodeList nlSaveTheSaleLines = XPathUtil.getNodeList(getOrderListOutDoc.getDocumentElement(),
							"/OrderList/Order/OrderLines/OrderLine[@MaxLineStatus!='9000']");
					int noOfSaveTheSaleLines = nlSaveTheSaleLines.getLength();
					for (int i = 0; i < noOfSaveTheSaleLines; i++) {
						Element eleOrderLine = (Element) nlSaveTheSaleLines.item(i);
						log.debug("MinLineStatus" + eleOrderLine.getAttribute("MinLineStatus"));
						String strExtnOriginalFulfillmentType = SCXmlUtil.getXpathAttribute(eleOrderLine,
								"Extn/@ExtnOriginalFulfillmentType");
						log.debug("strExtnOriginalFulfillmentType" + strExtnOriginalFulfillmentType);
						String strMinLineStatus = eleOrderLine.getAttribute("MinLineStatus").replace(".",",");
						log.debug("Number of STS eligible lines which are not in cancelled status :::"
								+ noOfSaveTheSaleLines);
						log.debug("MinLineStatus of Current OrderLine :::" + strMinLineStatus);
						String [] aaMinLineStatus=strMinLineStatus.split(",");
						Integer intMinLineStatus=Integer.parseInt(aaMinLineStatus[0]);
						if ("BOPIS".equals(strExtnOriginalFulfillmentType) && intMinLineStatus< 3350) {
							saveTheSaleLineExist = true;
							break;
						}
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.debug("saveTheSaleLineExist" + saveTheSaleLineExist);
		return saveTheSaleLineExist;

	}

	public void setProperties(Map propMap) {
		this.propMap = propMap;
	}
}