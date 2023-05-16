/**
 * 
 */
package com.academy.ecommerce.sterling.fulfillmentSummary.screens;

import java.util.Iterator;
import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// Sterling Imports
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCBehavior;
import com.yantra.yfc.rcp.YRCXmlUtils;

// Project Imports
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;

// Misc Imports - NONE

/**
 * @author sahmed
 * 
 */
public class AcademyGiftShipPopupBehavior extends YRCBehavior {

	private AcademyGiftShipPopup ownerComposite;
	private Element targetModel;
	private boolean isVoidGiftMessage = true;
	private Object input;
	private boolean giftWrapImplemented = false;
	// private String enterpriseCode;
	private NodeList orderLineList;

	public AcademyGiftShipPopupBehavior(Composite ownerComposite,
			String formId, boolean giftFlag, Object input) {
		super(ownerComposite, formId);
		this.ownerComposite = (AcademyGiftShipPopup) ownerComposite;
		// initialize();
	}

	
	private void initialize() {
		Element elem = YRCXmlUtils.createDocument("OrderLine")
				.getDocumentElement();
		ownerComposite.getGiftRadioButton().setSelection(true);
		elem.setAttribute(AcademyPCAConstants.IS_GIFT_FLAG,
				AcademyPCAConstants.ATTR_Y);
		// setGiftMessageAndCheckForGiftWrap((Element)input,elem);
		setModel("ModelForInitialize", elem);
		ownerComposite.setGiftItemsModel(elem);
	}

	private Object getRule(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public void handleApplyButtonSelected() {
		targetModel = getTargetModel("targetModel");
		// YCDGiftUtils.handleInstructions(orderLineList,targetModel);
		ownerComposite.setGiftItemsModel(targetModel);
		ownerComposite.getShell().close();
	}

	/*
	 * private Element getTargetModel(String string) { // TODO Auto-generated
	 * method stub return null; }
	 */

	public void handleCloseButtonSelected() {
		ownerComposite.setGiftItemsModel(targetModel);
		ownerComposite.getShell().close();
	}

	public void callCommonCodes() {
		// TODO Auto-generated method stub
		Document ExtnCommonCodeInputXML = YRCXmlUtils
				.createDocument(AcademyPCAConstants.ATTR_COMMON_CODE);
		ExtnCommonCodeInputXML.getDocumentElement().setAttribute(
				AcademyPCAConstants.ATTR_CODE_TYPE,
				AcademyPCAConstants.ATTR_GIFT_MESSAGE);
		YRCApiContext ctx = new YRCApiContext();
		ctx.setApiName(AcademyPCAConstants.API_GET_COMMONCODELIST_FOR_GIFT);
		ctx.setFormId(getFormId());
		ctx.setInputXml(ExtnCommonCodeInputXML);
		callApi(ctx);
	}

	@Override
	public void handleApiCompletion(YRCApiContext ctx) {
		if (ctx.getApiName().equals(AcademyPCAConstants.API_GET_COMMONCODELIST_FOR_GIFT)) {
			setModel(AcademyPCAConstants.ATTR_EXTN_COMMON_CODE, ctx.getOutputXml().getDocumentElement());
		}
		// TODO Auto-generated method stub
		super.handleApiCompletion(ctx);
	}
}
