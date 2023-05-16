package com.academy.ecommerce.sterling.orderSummary.extn;

import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.academy.ecommerce.sterling.util.XMLUtil;
import com.academy.ecommerce.sterling.util.XPathUtil;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.internal.YRCApiCaller;

public class AcademyGiftCardChildCompositeBehavior extends YRCBehavior {

	private AcademyGiftCardChildComposite page = null;

	private Element inElm = null;

	private AcademyGCLinesComposite gcComp;

	private boolean bGiftItem;

	public AcademyGiftCardChildCompositeBehavior(
			AcademyGiftCardChildComposite ownercomposite, String form_id,
			Element inElm) {
		super(ownercomposite, form_id, inElm);
		page = (AcademyGiftCardChildComposite) ownercomposite;
		this.inElm = inElm;
		setModel(AcademyPCAConstants.ATTR_SHIPMENT, inElm);
	}

	public String getTxtActivate() {
		return page.txtActivate.getText();
	}

	public String getTxtActivatedQty() {
		return page.txtActivatedQty.getText();
	}

	/*
	 * This method will be invoked to form the xml which forms the input to the
	 * non-task queue based transaction
	 */
	public void formMessageForBulkGCShipment() {
		String strEnteredActivationCode = getTxtActivate();
		String strActualActivationCode = getTxtActivatedQty();
		String strShipmentKey = inElm
				.getAttribute(AcademyPCAConstants.ATTR_SHIPMENT_KEY);
		YRCPlatformUI.trace("###Activation Code is######"
				+ strEnteredActivationCode);
		if ("".equals(strEnteredActivationCode)) {
			YRCPlatformUI.showError("Error",
					"Activation Code needs to be entered");
		} else {
			if (strEnteredActivationCode.equals(strActualActivationCode)) {
				/* Logic to get the shipment lines and form xml */
				try {
					ArrayList gcLinesComp = page.objGCCollection;
					Document doc = XMLUtil.createDocument("Shipment");
					doc.getDocumentElement().setAttribute("Source", ".YRCApp");
					doc.getDocumentElement().setAttribute("ActivationCode",
							strEnteredActivationCode);
					for (int j = 0; j < gcLinesComp.size(); j++) {
						gcComp = (AcademyGCLinesComposite) gcLinesComp.get(j);
						Element gcData = gcComp.getBehavior().getGCModel();
						YRCPlatformUI
								.trace("#######Shipment Line Model in the page is############"
										+ XMLUtil.getElementXMLString(gcData));

						try {
							String strShipmentLineKey = XPathUtil.getString(
									gcData, "@ShipmentLineKey");
							String strUnitQty = XPathUtil.getString(gcData,
									"OrderLine/LinePriceInfo/@UnitPrice");
							Element eleShipmentLine = doc
									.createElement(AcademyPCAConstants.ATTR_SHIPMENT_LINE);
							doc.getDocumentElement().appendChild(
									eleShipmentLine);
							eleShipmentLine.setAttribute("ShipmentLineKey",
									strShipmentLineKey);
							eleShipmentLine.setAttribute("UnitPrice",
									strUnitQty);
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
					YRCPlatformUI
							.trace("###XML prepared for pushing into the queue is#####"
									+ XMLUtil.getXMLString(doc));
					invokeSyncAPI(doc, "AcademyBulkGCLinesMessageForActivation");
					callChangeShipmentToUpdateBulkGCCompletion(strShipmentKey);
					page.activateButton.setEnabled(false);
					page.txtActivate.setEnabled(false);
					page.lblBulkGCCompletion.setVisible(true);
					page.lblBulkGCCompletion.setEnabled(true);
					page.lblBulkGCCompletion
							.setText("Activation Process has started");
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				}
			} else {
				YRCPlatformUI
						.showError("Error",
								"ActivationCode on Shipment doesn't match the Entered Activation Code");
			}
		}

	}

	private Document invokeSyncAPI(Document docCustomer, String strAPIName) {
		YRCApiContext context = new YRCApiContext();
		YRCApiCaller syncapiCaller = new YRCApiCaller(context, true);
		context.setApiName(strAPIName);
		context
				.setFormId("com.academy.ecommerce.sterling.orderSummary.extn.AcademyGiftCardChildComposite");
		context.setInputXml(docCustomer);
		syncapiCaller.invokeApi();
		Document outDoc = context.getOutputXml();
		return outDoc;
	}

	public boolean getbGiftItem() {
		return bGiftItem;
	}

	public void callChangeShipmentToUpdateBulkGCCompletion(String strShipmentKey) {
		try {
			Document docShipment = XMLUtil
					.createDocument(AcademyPCAConstants.ATTR_SHIPMENT);
			docShipment.getDocumentElement().setAttribute(
					AcademyPCAConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
			Element eleExtn = docShipment
					.createElement(AcademyPCAConstants.ATTR_EXTN);
			docShipment.getDocumentElement().appendChild(eleExtn);
			eleExtn.setAttribute("ExtnGCBulkActivationFlag",
					AcademyPCAConstants.ATTR_Y);
			invokeSyncAPI(docShipment, "changeShipment");
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

	}
}
