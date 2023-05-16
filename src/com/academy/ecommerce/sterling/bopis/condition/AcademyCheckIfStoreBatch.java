package com.academy.ecommerce.sterling.bopis.condition;

import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;

import com.yantra.ycp.japi.YCPDynamicConditionEx;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * @author Mohamed Shaikna
 *
 */
public class AcademyCheckIfStoreBatch implements YCPDynamicConditionEx {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyCheckIfStoreBatch.class);

	@SuppressWarnings("rawtypes")
	@Override
	public boolean evaluateCondition(YFSEnvironment env, String arg1, Map arg2, Document inDoc) {

		log.beginTimer("AcademyCheckIfStoreBatch::evaluateCondition");
		log.verbose("Entering the method AcademyCheckIfStoreBatch.evaluateCondition ");
		log.verbose("Input XML for evaluateCondition() ==> " + XMLUtil.getXMLString(inDoc));

		String strGetShipmentListTemp = "<Shipments>\r\n" + "<Shipment ShipmentKey=\"\" >\r\n" + "<ShipmentLines>\r\n"
				+ "<ShipmentLine StoreBatchKey=\"\" />\r\n" + "</ShipmentLines>\r\n" + "</Shipment>\r\n"
				+ "</Shipments>";

		Element eleShipmentFromInDoc = inDoc.getDocumentElement();
		Element eleShipmentLineFromInDoc = (Element) eleShipmentFromInDoc
				.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE).item(0);

		if (eleShipmentLineFromInDoc.hasAttribute(AcademyConstants.ATTR_STORE_BATCH_KEY)
				&& !(YFCCommon.isVoid(eleShipmentLineFromInDoc.getAttribute(AcademyConstants.ATTR_STORE_BATCH_KEY)))) {
			log.verbose("Input to Condition is true");
			return true;
		}

		String strShipmentKey = eleShipmentFromInDoc.getAttribute(AcademyConstants.SHIPMENT_KEY);

		Document docgetShipmentListInput = null;
		try {
			docgetShipmentListInput = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		Element elemShipment = (Element) docgetShipmentListInput.getDocumentElement();

		elemShipment.setAttribute(AcademyConstants.SHIPMENT_KEY, strShipmentKey);

		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, strGetShipmentListTemp);
		Document getShipmentListOutput = null;

		try {
			getShipmentListOutput = AcademyUtil.invokeService(env, "AcademyGetShipmentListForRemoveUser",
					docgetShipmentListInput);
		} catch (Exception e) {
			e.printStackTrace();
		}

		log.verbose("Output XML for getShipmentList() ==> " + XMLUtil.getXMLString(getShipmentListOutput));

		Element eleShipmentOP = getShipmentListOutput.getDocumentElement();

		Element eleShipmentLine = (Element) eleShipmentOP.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE)
				.item(0);

		if (eleShipmentLine.hasAttribute(AcademyConstants.ATTR_STORE_BATCH_KEY)
				&& !(YFCCommon.isVoid(eleShipmentLine.getAttribute(AcademyConstants.ATTR_STORE_BATCH_KEY)))) {
			log.verbose("Output of getShipmentList is true");
			log.verbose(
					"StoreBatchKey is " + eleShipmentLine.getAttribute(AcademyConstants.ATTR_STORE_BATCH_KEY).trim());
			return true;
		}
		log.verbose("Condition Returns False...");
		return false;

	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setProperties(Map arg0) {
	}
}