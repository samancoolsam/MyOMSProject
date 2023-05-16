package com.academy.ecommerce.sterling.bopis.order.api;

import java.util.HashMap;

/**#########################################################################################
*
* Project Name					: OMNI-40283,OMNI-40285
* Module						: OMNI-41026
* Author						: C0027905
* Author Group					: EVEREST
* Date							: 29-May-2021 
* Description	: This class send Push Notifications for Save The Sales scenario
* to Ready For Customer Pick Status and Order Has Delayed Lines.
* ---------------------------------------------------------------------------------
* Date			Author			Version#		Remarks/Description
* ---------------------------------------------------------------------------------
* 29-May-2021 Everest	1.0			Updated version
* #########################################################################################*/

import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.email.util.AcademyEmailUtil;
import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantriks.yih.adapter.util.YantriksConstants;

public class AcademySTSSendPushNotificaiton implements YIFCustomApi {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademySTSSendPushNotificaiton.class);
	private Properties props;

	public Document sendPushNotification(YFSEnvironment env, Document inDoc) throws Exception {

		log.verbose("Start of method sendPushNotification : input is :: " + XMLUtil.getXMLString(inDoc));
		HashMap<String, String> hmUnicodeMap = AcademyCommonCode.getCommonCodeListAsHashMap(env,
				AcademyConstants.LISTRAK_EMAIL_CODES, AcademyConstants.PRIMARY_ENTERPRISE);
		AcademyEmailUtil.removeOrderlines(inDoc, env, inDoc.getDocumentElement());

		String strMessageType;
		boolean isAllLineDelayed = true;
		Element eleIndoc = inDoc.getDocumentElement();
		NodeList currentShipmentLines = getCurrentShipmentLines(eleIndoc);
		for (int i = 0; i < currentShipmentLines.getLength(); i++) {
			Element shipmentLine = (Element) currentShipmentLines.item(i);
			Element eleOrderLine = getOrderLine(eleIndoc, shipmentLine.getAttribute("OrderLineKey"));
			log.verbose("Is Delayed Order Line ::: " + checkDelayedLine(eleOrderLine));
			if (!checkDelayedLine(eleOrderLine)) {
				isAllLineDelayed = false;
			}

		}
		if (isAllLineDelayed) {
			strMessageType = hmUnicodeMap.get("STS_ALL_DELAYED_MSG_TYPE");
		} else {
			strMessageType = hmUnicodeMap.get("STS_MSG_TYPE");
		}
		log.verbose(strMessageType);
		inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, strMessageType);
		log.verbose(SCXmlUtil.getString(inDoc));
		AcademyUtil.invokeService(env, "AcademyPostPushNotificationMsgToQueue", inDoc);

		return inDoc;
	}

	private boolean checkDelayedLine(Element eleOrderLine) throws Exception {
		// bopis DELAYED -sts -DeliveryMethod="PICK" AND FulfillmentType="STS" /
		// MaxLineStatus="2160.00.01" / ExtnOriginalFulfillmentType="BOPIS"
		// - bopis DELAYED -sts and ="" OrderedQty="8.00" OriginalOrderedQty="8.00"
		String strMaxLineStatus =eleOrderLine.getAttribute("MaxLineStatus");
		return !YFCObject.isVoid(eleOrderLine) && "PICK".equals(eleOrderLine.getAttribute("DeliveryMethod"))
				&& "STS".equals(eleOrderLine.getAttribute("FulfillmentType"))
				//&& "2160.00.01".equals(eleOrderLine.getAttribute("MaxLineStatus"))
				//Changes for OMNI-63470
				&&  !StringUtil.isEmpty(strMaxLineStatus) && strMaxLineStatus.startsWith(YantriksConstants.V_STATUS_2160_00_01)
				&& (!YFCObject.isVoid(getExtnOriginalFulfillmentType(eleOrderLine)))
				&& "BOPIS".equals(getExtnOriginalFulfillmentType(eleOrderLine))
				&& (!YFCObject.isVoid(eleOrderLine.getAttribute("OrderedQty"))
						&& !YFCObject.isVoid(eleOrderLine.getAttribute("OriginalOrderedQty"))
						&& Double.parseDouble(eleOrderLine.getAttribute("OrderedQty")) == Double
								.parseDouble(eleOrderLine.getAttribute("OriginalOrderedQty"))) ? true : false;

	}

	private String getExtnOriginalFulfillmentType(Element eleOrderLine) throws Exception {
		String olkey = eleOrderLine.getAttribute("OrderLineKey");
		return XMLUtil.getString(eleOrderLine,
				"/Order/OrderLines/OrderLine[@OrderLineKey='" + olkey + "']/Extn/@ExtnOriginalFulfillmentType");
	}

	private Element getOrderLine(Element eleIndoc, String orderlineKey) throws Exception {
		return (Element) XPathUtil.getNode(eleIndoc,
				"/Order/OrderLines/OrderLine[@OrderLineKey='" + orderlineKey + "']");
	}

	private NodeList getCurrentShipmentLines(Element eleIndoc) throws Exception {
		String currentShipmentKey = XPathUtil.getString(eleIndoc, "/Order/@CurrentShipmentKey");
		return XPathUtil.getNodeList(eleIndoc,
				"/Order/Shipments/Shipment[@ShipmentKey='" + currentShipmentKey + "']/ShipmentLines/ShipmentLine");
	}

	public void setProperties(Properties props) throws Exception {
		this.props = props;

	}
}