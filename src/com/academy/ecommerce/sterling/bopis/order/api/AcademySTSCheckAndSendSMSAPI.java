package com.academy.ecommerce.sterling.bopis.order.api;

/**#########################################################################################
*
* Project Name					: OMNI-40283,OMNI-40285
* Module						: OMNI-38560
* Author						: C0027905
* Author Group					: EVEREST
* Date							: 29-May-2021 
* Description	: This class publish SMS to ESB queue when BOPIS order is moved
* to Ready For Customer Pick Status and Order Has Delayed Lines.
* 
* ---------------------------------------------------------------------------------
* Date			Author			Version#		Remarks/Description
* ---------------------------------------------------------------------------------
* 29-May-2021 Everest	1.0			Updated version
* #########################################################################################*/

import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademySTSCheckAndSendSMSAPI implements YIFCustomApi {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademySTSCheckAndSendSMSAPI.class);
	private Properties props;

	public Document checkAndSendSMS(YFSEnvironment env, Document inDoc) throws Exception {

		log.verbose("Start of method checkAndSendSMS : input is :: " + XMLUtil.getXMLString(inDoc));

		Document docSMSMessage =null;
		String smsFOR = "SAVE_THE_SALE_SMS";
		String messageType = "SaveTheSale";
		String primaryPhoneNo = "";
		String alternatePhoneNo = "";
		String strMessage = "";

		Element eleIndoc = inDoc.getDocumentElement();
		NodeList currentShipmentLines = getCurrentShipmentLines(eleIndoc);
		for (int i = 0; i < currentShipmentLines.getLength(); i++) {
			Element shipmentLine = (Element) currentShipmentLines.item(i);
			Element eleOrderLine = getOrderLine(eleIndoc, shipmentLine.getAttribute("OrderLineKey"));
			boolean bHasCancelledLines = checkCancelledItem(eleOrderLine);
			if (bHasCancelledLines) {
				smsFOR = "SAVE_THE_SALE_CN_SMS";
				messageType = "SaveTheSaleCancel";
				log.verbose("bHasCancelledLines : " + bHasCancelledLines);
				break;

			}
		}

		Element elePersonInfoBillToOut = SCXmlUtil.getChildElement(eleIndoc, AcademyConstants.ELE_PERSON_INFO_BILL_TO);
		Element elePersonInfoMarkForOut = SCXmlUtil.getChildElement(eleIndoc,
				AcademyConstants.ELE_PERSON_INFO_MARK_FOR);

		if (!YFCCommon.isVoid(elePersonInfoBillToOut)) {
			primaryPhoneNo = elePersonInfoBillToOut.getAttribute(AcademyConstants.ATTR_DAY_PHONE);
		}
		if (!YFCCommon.isVoid(elePersonInfoMarkForOut)) {
			alternatePhoneNo = elePersonInfoMarkForOut.getAttribute(AcademyConstants.ATTR_DAY_PHONE);
		}

		if (!YFCCommon.isVoid(primaryPhoneNo) || !YFCCommon.isVoid(alternatePhoneNo)) {

			Document outDocGetCommonCodeList = getCommonCodeList(env, smsFOR);
			String strSMSMessage = getSMSMessage(outDocGetCommonCodeList, smsFOR);

			docSMSMessage = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, messageType);
			String orderNo = eleIndoc.getAttribute(AcademyConstants.ATTR_ORDER_NO);
			strMessage = strSMSMessage.replace(AcademyConstants.ATT_$ORDER_NO, orderNo);
			strMessage = strMessage.replace("#", " ");
			docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_MESSAGE, strMessage);
// End OMNI-5398,5399
			if (!YFCCommon.isVoid(alternatePhoneNo)) {
				docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_DAY_PHONE, alternatePhoneNo);
				log.verbose("alternate person SMS message : " + XMLUtil.getXMLString(docSMSMessage));
				AcademyUtil.invokeService(env, AcademyConstants.ACD_BOPIS_POST_MES_Q_SERVICE, docSMSMessage);
			}
			if (!YFCCommon.isVoid(primaryPhoneNo)) {
				docSMSMessage.getDocumentElement().setAttribute(AcademyConstants.ATTR_DAY_PHONE, primaryPhoneNo);
				log.verbose("primary customer SMS message : " + XMLUtil.getXMLString(docSMSMessage));
				AcademyUtil.invokeService(env, AcademyConstants.ACD_BOPIS_POST_MES_Q_SERVICE, docSMSMessage);
			}
		}

		return docSMSMessage;
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

	private boolean checkCancelledItem(Element eleOrderLine) throws Exception {
		double orderQty = Double.parseDouble(eleOrderLine.getAttribute("OriginalOrderedQty"))
				- Double.parseDouble(eleOrderLine.getAttribute("OrderedQty"));
		if (orderQty > 0) {
			return true;
		}
		return false;
	}

	private String getSMSMessage(Document outDocGetCommonCodeList, String smsFOR) throws Exception {
		log.verbose("Start AcademySTSCheckAndSendSMSAPI.getSMSMessage()  strSMSType ::" + smsFOR);

		String strSMSMessage = "";
		int iMsgCount = 1;
		String strMessage = "";

		do {
			strMessage = XPathUtil.getString(outDocGetCommonCodeList, "/CommonCodeList/CommonCode[@CodeValue='" + smsFOR
					+ "_Mes" + iMsgCount + "']/@CodeLongDescription");
			if (YFCObject.isVoid(strMessage)) {
				iMsgCount = 0;
			} else {
				strSMSMessage = strSMSMessage + strMessage;
				iMsgCount++;
			}
		} while (iMsgCount != 0);

		log.verbose("End AcademySTSCheckAndSendSMSAPI.getSMSMessage() Output message " + strSMSMessage);
		return strSMSMessage;
	}

	private Document getCommonCodeList(YFSEnvironment env, String strCodeType) throws Exception {

		log.verbose("Start AcademySTSCheckAndSendSMSAPI.getCommonCodeList() strCodeType :: " + strCodeType);
// TODO Auto-generated method stub
		Document inDocGetCommonCodeList = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
		inDocGetCommonCodeList.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, strCodeType);
		log.verbose("input to getCommonCodeList " + XMLUtil.getXMLString(inDocGetCommonCodeList));
		Document outDocGetCommonCodeList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMON_CODELIST,
				inDocGetCommonCodeList);
		log.verbose("End AcademySTSCheckAndSendSMSAPI.getCommonCodeList() output XML: "
				+ XMLUtil.getXMLString(outDocGetCommonCodeList));
		return outDocGetCommonCodeList;
	}

	public void setProperties(Properties props) throws Exception {
		this.props = props;

	}
}