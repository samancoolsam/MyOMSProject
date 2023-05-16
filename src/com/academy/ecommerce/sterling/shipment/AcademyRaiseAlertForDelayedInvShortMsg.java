package com.academy.ecommerce.sterling.shipment;

import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;

/**
 * @author <a href="mailto:Kruthi.KM@cognizant.com">Kruthi KM</a>, Created on
 *         01/08/2014. This Class will raise an alert to the Business User in
 *         case Sterling receives delayed No Inventory message
 */
public class AcademyRaiseAlertForDelayedInvShortMsg implements YIFCustomApi {
	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyRaiseAlertForDelayedInvShortMsg.class);

	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	/*
	 * This method prepares the input to createException API which raises an
	 * alert to the business user
	 */
	public void raiseAlertForDelayedInvShortMsg(YFSEnvironment env,
			Document inDoc) {
		log.verbose("Input to raiseAlertForDelayedInvShortMsg():: "
				+ XMLUtil.getXMLString(inDoc));
		Document createExpInDoc = null;
		;
		try {
			String strShipmentLineNo = XPathUtil.getString(inDoc,
					"/Shipment/ShipmentLines/ShipmentLine/@ShipmentLineNo");
			String strQty = XPathUtil.getString(inDoc,
					"/Shipment/ShipmentLines/ShipmentLine/@Quantity");
			createExpInDoc = XMLUtil.createDocument(AcademyConstants.ELE_INBOX);
			Element eleAlertShipment = createExpInDoc.getDocumentElement();
			eleAlertShipment.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO,
					inDoc.getDocumentElement().getAttribute(
							AcademyConstants.ATTR_SHIPMENT_NO));
			eleAlertShipment.setAttribute(AcademyConstants.ATTR_EXCPTN_TYPE,
					AcademyConstants.ATTR_EXCPTN_TYPE_VAL);
			eleAlertShipment.setAttribute(AcademyConstants.ATTR_QUEUE_ID,
					AcademyConstants.ATTR_QUEUE_ID_VAL);
			eleAlertShipment.setAttribute(AcademyConstants.ATTR_DESC,
					"Delayed No-inventory message recieved for line#"
							+ strShipmentLineNo
							+ " of Shipment#"
							+ inDoc.getDocumentElement().getAttribute(
									"ShipmentNo") + " for Qty" + strQty + "");
			AcademyUtil.invokeAPI(env, AcademyConstants.API_CREATE_EXCEPTION,
					createExpInDoc);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}