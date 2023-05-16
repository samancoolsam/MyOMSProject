package com.academy.ecommerce.sterling.orderSummary.extn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.academy.ecommerce.sterling.util.XMLUtil;
import com.academy.ecommerce.sterling.util.XPathUtil;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCXmlUtils;
import com.yantra.yfc.rcp.internal.YRCApiCaller;
import com.yantra.yfc.rcp.internal.YRCRefreshScreenAction;

public class AcademyGiftCardLoadBehavior extends YRCBehavior {

	private AcademyGiftCardLoad page = null;

	private Element rootElem = null;

	private Element gcChargeRequest = null;

	private Element eleCurrentOrderLine = null;

	private AcademyGiftCardChildComposite childComp = null;

	public static String nNoOfOrderLines = null;

	int iTextFieldValue = 0;

	String ele = null;

	private AcademyGCLinesComposite gcComp = null;

	private int iOrderLineQty = 0;

	private String strShipmentKey;

	private String strShipmentLineKey;

	private Element inElm = null;

	public AcademyGiftCardLoadBehavior(Composite ownerComposite,
			String form_id, Element inElm) {
		super(ownerComposite, form_id, inElm);
		this.inElm = inElm;
		this.page = (AcademyGiftCardLoad) ownerComposite;
		setModel("Order", inElm);
		Document outDoc = callGetShipmentListForOrder(inElm);
		initializeModel(outDoc);
	}

	@Override
	protected void init() {
		// TODO Auto-generated method stub
		super.init();
	}

	private void initializeModel(Document outDoc) {
		Element eleShipment = outDoc.getDocumentElement();
		setModel("Shipment", eleShipment);
	}

	private Document callGetShipmentListForOrder(Element inputElement) {
		Document docInput = YRCXmlUtils.createFromString("<Order/>");
		Element eleInput = docInput.getDocumentElement();
		eleInput
				.setAttribute(
						AcademyPCAConstants.ATTR_ORDER_HEADER_KEY,
						inputElement
								.getAttribute(AcademyPCAConstants.ATTR_ORDER_HEADER_KEY));
		Document docOutput = invokeSyncAPI(docInput, "getShipmentListForOrder");
		return docOutput;
	}

	public void handleCancelButtonSelected() {
		YRCPlatformUI
				.closeEditor(
						"com.academy.ecommerce.sterling.orderSearch.editor.AcademyGiftCardLoadEditor",
						true);
	}

	public Element getShipmentModel() {
		Element eleShipment = getModel("Shipment");
		return eleShipment;
	}


	/**
	 * @param docShipmentLine
	 * @return
	 */
	private Document invokeSyncAPI(Document docShipmentLine, String strAPIName) {
		YRCApiContext context = new YRCApiContext();
		YRCApiCaller syncapiCaller = new YRCApiCaller(context, true);
		context.setApiName(strAPIName);
		context.setFormId(AcademyPCAConstants.FORM_ID_LOAD_GIFT_CARD_PARENT);
		context.setInputXml(docShipmentLine);
		syncapiCaller.invokeApi();
		Document outputXMLOfShipmentLineList = context.getOutputXml();
		return outputXMLOfShipmentLineList;
	}

}
