package com.academy.ecommerce.sterling.orderSummary.extn;

import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.yantra.yfc.rcp.YRCBehavior;

public class AcademyGCLinesCompositeBehavior extends YRCBehavior {

	private AcademyGCLinesComposite page = null;

	int a = 0;

	public AcademyGCLinesCompositeBehavior(
			AcademyGCLinesComposite ownerComposite, String form_id,
			Element inElm, Element eleShipmentLine) {
		super(ownerComposite, form_id);
		this.page = (AcademyGCLinesComposite) ownerComposite;
		setModel("ShipmentLine", eleShipmentLine);
	}

	public Element getOrderLineModel() {
		return getModel(AcademyPCAConstants.ATTR_ORDER_LINE_DETAILS);
	}

	public Element getGCModel() {
		return getModel("ShipmentLine");

	}
}
