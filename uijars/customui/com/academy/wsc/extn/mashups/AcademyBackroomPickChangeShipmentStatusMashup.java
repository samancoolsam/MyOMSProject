package com.academy.wsc.extn.mashups;

import com.ibm.wsc.common.mashups.WSCBaseMashup;
import com.ibm.wsc.mashups.utils.WSCMashupUtils;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.sterlingcommerce.ui.web.framework.context.SCUIContext;
import com.sterlingcommerce.ui.web.framework.helpers.SCUIMashupHelper;
import com.sterlingcommerce.ui.web.framework.mashup.SCUIMashupMetaData;
import com.sterlingcommerce.ui.web.framework.utils.SCUIUtils;
import org.w3c.dom.Element;

public class AcademyBackroomPickChangeShipmentStatusMashup extends WSCBaseMashup {
	public Element massageInput(Element inputEl, SCUIMashupMetaData mashupMetaData, SCUIContext uiContext) {
		super.massageInput(inputEl, mashupMetaData, uiContext);
		String shipmentKey = SCXmlUtil.getAttribute(inputEl, "ShipmentKey");
		WSCMashupUtils.addAttributeToUIContext(uiContext, "ShipmentKey", this, shipmentKey);
		String deliveryMethod = SCXmlUtil.getAttribute(inputEl, "ShipmentDeliveryMethod");
		WSCMashupUtils.addAttributeToUIContext(uiContext, "ShipmentDeliveryMethod", this, deliveryMethod);
		inputEl.removeAttribute("ShipmentDeliveryMethod");
		return inputEl;
	}

	public Element massageOutput(Element outEl, SCUIMashupMetaData mashupMetaData, SCUIContext uiContext) {
		super.massageOutput(outEl, mashupMetaData, uiContext);
		int totalNumOfRec = 0;

		try {
			totalNumOfRec = Integer.parseInt(outEl.getAttribute("TotalNumberOfRecords"));
		} catch (NumberFormatException var12) {
			;
		}

		if (totalNumOfRec == 0) {
			String shipmentKey = (String) WSCMashupUtils.getAttributeFromUIContext(uiContext, "ShipmentKey", this,
					true);
			Element shipmentLineListInput = SCXmlUtil.createDocument("ShipmentLine").getDocumentElement();
			shipmentLineListInput.setAttribute("ShipmentKey", shipmentKey);
			Element shortedShipmentLineListElement = (Element) SCUIMashupHelper.invokeMashup(
					"backroomPickUp_getCompletelyShortedShipmentLineListCount", shipmentLineListInput, uiContext);
			if (SCUIUtils.equals("0", shortedShipmentLineListElement.getAttribute("TotalNumberOfRecords"))) {
				Element cancelShipmentInput = SCXmlUtil.createDocument("Shipment").getDocumentElement();
				cancelShipmentInput.setAttribute("ShipmentKey", shipmentKey);
				SCUIMashupHelper.invokeMashup("backroomPickUp_cancelShipment", cancelShipmentInput, uiContext);
				outEl.setAttribute("NextAction", "ShowCancelPopup");
			} else {
				String shipmentDeliveryMethod = (String) WSCMashupUtils.getAttributeFromUIContext(uiContext,
						"ShipmentDeliveryMethod", this, true);
				Element shipmentInput = SCXmlUtil.createDocument("Shipment").getDocumentElement();
				shipmentInput.setAttribute("ShipmentKey", shipmentKey);
				Element shipmentDetailsElement = (Element) SCUIMashupHelper
						.invokeMashup("backroomPick_getShipmentDetailsByShipmentKey", shipmentInput, uiContext);
				String status = shipmentDetailsElement.getAttribute("Status");
				if (!SCUIUtils.isVoid(status) && status.contains("1100.70.06.20")) {
					if (SCUIUtils.equals("SHP", shipmentDeliveryMethod)) {
						SCUIMashupHelper.invokeMashup("backroomPickUp_changeShipmentStatusToReadyForPack",
								shipmentInput, uiContext);
					} 
					else if (SCUIUtils.equals("PICK", shipmentDeliveryMethod)) {
//						Removing below changeShipmentStaus api call since it is handled in finish pick screen
						
//						SCUIMashupHelper.invokeMashup("backroomPickUp_changeShipmentStatusToReadyForCustomerPick",
//								shipmentInput, uiContext);
					}
				}

				outEl.setAttribute("NextAction", "GotoNextScreen");
			}
		} else {
			outEl.setAttribute("NextAction", "ShowLinesNotPickedPopup");
		}

		return outEl;
	}
}