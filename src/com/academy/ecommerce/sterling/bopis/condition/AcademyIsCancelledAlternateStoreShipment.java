package com.academy.ecommerce.sterling.bopis.condition;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyIsCancelledAlternateStoreShipment implements YCPDynamicConditionEx {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyIsCancelledAlternateStoreShipment.class);
	private Map propMap = null;
	
	/**
	 * This Method returns <strong>TRUE</strong> when AlternateStore Shipment is
	 * full cancelled.
	 */
	@Override
	public boolean evaluateCondition(YFSEnvironment env, String str, Map map, Document inDoc) {
		boolean evaluateCondition = false;
		log.beginTimer("evaluateCondition");
		log.verbose("InDocument :: " + inDoc);
		try {
			Document inDocGetShipmentList = getShipmentList(env, inDoc);
			if (isAlternateStoreShipmentCancelled(inDocGetShipmentList)) {
					evaluateCondition = true;
			}
		} catch (Exception e) {
			log.error(e.getStackTrace());
			throw new YFSException(e.getMessage());
		}

		log.verbose("AcademyIsCancelledAlternateStoreShipment.evaluateCondition return :: " + evaluateCondition);
		log.endTimer("evaluateCondition");
		return evaluateCondition;
	}

	/**
	 * This method return <strong> TRUE </strong> when full shipment is <strong>shortpicked</strong> but converted to <strong>only AS lines.</strong>
	 * </br> Default return <strong> FALSE </strong>
	 * @param docGetShipmentList
	 * @return
	 * @throws Exception
	 */
	private boolean hasOnlyPendingASlines(Document docGetShipmentList) throws Exception {
		if (isAlternateStoreShipmentCancelled(docGetShipmentList)) {
			NodeList ndOrderLines = XMLUtil.getNodeListByXPath(docGetShipmentList,
					".//ShipmentLines/ShipmentLine/OrderLine[@MaxLineStatus='9000']");
			int iOrderLines = ndOrderLines.getLength();
			NodeList ndASOrderLines = XMLUtil.getNodeListByXPath(docGetShipmentList,
					".//ShipmentLines/ShipmentLine/OrderLine[@MaxLineStatus='9000']/Extn[@ExtnIsASP='Y']");
			int iASOrderLines = ndASOrderLines.getLength();
			if (iOrderLines == iASOrderLines) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * This Method return <strong> TRUE </strong> when full Shipment is <strong>Cancelled</strong> 
	 * @param docGetShipmentList
	 * @return
	 * @throws Exception
	 */
	private boolean isAlternateStoreShipmentCancelled(Document docGetShipmentList) throws Exception {
		boolean isAlternateStoreShipmentCancelled = false;
		boolean isShipmentCancelled = XMLUtil.getString(docGetShipmentList, "/Shipments/Shipment/@Status").equalsIgnoreCase("9000") ? true : false;
		if (isShipmentCancelled) {
			String strShipmentShipNode = XMLUtil.getString(docGetShipmentList, "/Shipments/Shipment/@ShipNode");
			String strExtnASShipNode = XMLUtil.getString(docGetShipmentList,
					"/Shipments/Shipment/ShipmentLines/ShipmentLine/Order/Extn/@ExtnASShipNode");
			if (!YFCCommon.isVoid(strExtnASShipNode) && strExtnASShipNode.equalsIgnoreCase(strShipmentShipNode)) {
				isAlternateStoreShipmentCancelled = true;
			}
		}
		return isAlternateStoreShipmentCancelled;
	}
	
	/**
	 * Invoke getShipmentList
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	private Document getShipmentList(YFSEnvironment env, Document inDoc) throws Exception {
		String strShipmentNo = XMLUtil.getString(inDoc,"/Shipment/@ShipmentNo");
		Document inDocShip = XMLUtil.createDocument("Shipment");
		inDocShip.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
		inDocShip.getDocumentElement().setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.SALES_DOCUMENT_TYPE);
		inDocShip.getDocumentElement().setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, "global/template/api/getShipmentList.academySendEmailAgent.xml");
		Document outDocShip=AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST, inDocShip);
		log.verbose("GetShipmentList Output  doc->"+XMLUtil.getXMLString(outDocShip));
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
		return outDocShip;
	}

	@Override
	public void setProperties(Map arg0) {
	}

}
