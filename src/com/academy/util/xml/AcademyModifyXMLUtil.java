package com.academy.util.xml;

import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.logger.Logger;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyModifyXMLUtil implements YIFCustomApi {

	private static Logger log = Logger.getLogger(AcademyModifyXMLUtil.class.getName());
	private Properties props;

	public void setProperties(Properties props) throws Exception {

		this.props = props;
	}

	public Document addAttributeOnXPAth(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("Entering AcademyModifyXMLUtil.addAttributeOnXPAth() :: " + XMLUtil.getXMLString(inDoc));
		for (Object xPath : props.keySet()) {
			String strAttributeName = ((String) xPath).substring(((String) xPath).lastIndexOf("@") + 1);
			int iend = ((String) xPath).lastIndexOf("/");
			String strXPath = ((String) xPath).substring(0, iend);
			Element ele = XMLUtil.getElementByXPath(inDoc, strXPath);
			String xPathValue = (String) props.get(xPath);
			if (xPathValue.startsWith("$")) {
				ele.setAttribute(strAttributeName, YFSSystem.getProperty(xPathValue.substring(1)));
			} else {
				ele.setAttribute(strAttributeName, xPathValue);
			}

		}
		if (inDoc.getDocumentElement().hasAttribute("URL_STORELOCATOR")
				|| inDoc.getDocumentElement().hasAttribute("URL_ViewOrderDetails")) {
			updateTwoURLs(inDoc);
		}

		log.verbose("Exiting AcademyModifyXMLUtil.addAttributeOnXPAth() ::" + XMLUtil.getXMLString(inDoc));
		return inDoc;
	}

	private void updateTwoURLs(Document inDoc) throws Exception {
		String strShipNode = "";
		String strZipCode = "";
		String strState = "";
		String strCity = "";
		Element eleOrder = inDoc.getDocumentElement();
		Element eleShip = (Element) XPathUtil.getNode(eleOrder, "/Order/Shipments/Shipment[@ShipmentKey='"
				+ eleOrder.getAttribute(AcademyConstants.STR_CURRENT_SHIPMENTKEY) + "']");
		if (!YFCCommon.isVoid(eleShip)) {
			strShipNode = eleShip.getAttribute(AcademyConstants.ATTR_SHIP_NODE);
		}

		String strOrderNo = eleOrder.getAttribute(AcademyConstants.ATTR_ORDER_NO);
		Element eleBillTo = SCXmlUtil.getChildElement(eleOrder, AcademyConstants.ELE_PERSON_INFO_BILL_TO);
		Element eleShipTo = (Element) XMLUtil.getElementByXPath(inDoc, "/Order/OrderLines/OrderLine/PersonInfoShipTo");

		if (!YFCCommon.isVoid(eleShipTo)) {
			strState = eleShipTo.getAttribute(AcademyConstants.ATTR_STATE);
			strCity = eleShipTo.getAttribute(AcademyConstants.ATTR_CITY);
		}

		// BOPIS-1638 Get ZipCode from BillTo instead of ShipTo - Start
		if (!YFCCommon.isVoid(eleBillTo)) {
			strZipCode = eleBillTo.getAttribute(AcademyConstants.ZIP_CODE);
		}
		// BOPIS-1638 Get ZipCode from BillTo instead of ShipTo - End

		String strURL_ViewOrderDetails = eleOrder.getAttribute("URL_ViewOrderDetails");
		if (!YFCCommon.isVoid(strURL_ViewOrderDetails)) {
			strURL_ViewOrderDetails = strURL_ViewOrderDetails.replace("@@@@", strOrderNo);
			strURL_ViewOrderDetails = strURL_ViewOrderDetails.replace("$$$$", strZipCode);
			eleOrder.setAttribute("URL_ViewOrderDetails", strURL_ViewOrderDetails);

		}
		String strURL_STORELOCATOR = eleOrder.getAttribute("URL_STORELOCATOR");
		if (!YFCCommon.isVoid(strURL_STORELOCATOR)) {

			strURL_STORELOCATOR = strURL_STORELOCATOR.replace("@@@@", strCity);
			strURL_STORELOCATOR = strURL_STORELOCATOR.replace("####", strState);
			strURL_STORELOCATOR = strURL_STORELOCATOR.replace("$$$$", strShipNode);
			eleOrder.setAttribute("URL_STORELOCATOR", strURL_STORELOCATOR);

		}
	}
}