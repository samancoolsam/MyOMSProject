package com.academy.ecommerce.sterling.orderSummary.actions;

//Java Imports


import org.eclipse.jface.action.IAction;
import org.w3c.dom.Element;

//Sterling Imports
import com.yantra.yfc.rcp.YRCAction;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCDesktopUI;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCXmlUtils; 

//Project Imports
import com.academy.ecommerce.sterling.orderSummary.screens.AcademyTaxIdPopUp;
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;


//Misc Imports -NONE

public class TaxIdPopupSubmitAction extends YRCAction {
	
	AcademyTaxIdPopUp taxPopup = (AcademyTaxIdPopUp) YRCDesktopUI
			.getCurrentPage();

	public void execute(IAction action) {

		if ("".equalsIgnoreCase(taxPopup.getTax_text_value())) {
			// Display an error-popup
			YRCPlatformUI.showError("Enter a Valid Tax ID",
					"Enter a Valid Tax ID");
		} else {
			try {
				org.w3c.dom.Document doc = (org.w3c.dom.Document) YRCXmlUtils
						.createDocument(AcademyPCAConstants.ATTR_ORDER);
				Element eleOrder = doc.getDocumentElement();
				eleOrder.setAttribute("Action", "MODIFY");
				eleOrder.setAttribute(AcademyPCAConstants.ATTR_ORDER_HEADER_KEY, taxPopup
						.getOrderHeaderKey());
				eleOrder.setAttribute(AcademyPCAConstants.ATTR_TAX_EXEMPTION_CERTIFICATE, taxPopup
						.getTax_text_value());
				YRCPlatformUI.trace("###### doc for UpdateTaxExemptID ####"
						+ YRCXmlUtils.getString(doc));
				YRCApiContext context = new YRCApiContext();
				context.setApiName("changeOrder");
				// context.setFormId("DummyFormId");
				context.setFormId(AcademyPCAConstants.FORM_ID_TAX_POP_UP);
				context.setInputXml(doc);
				taxPopup.getMyBehavior().callApi(context);
				taxPopup.getMyBehavior().close();
				YRCPlatformUI.showInformation("Message", "Tax ID has been modified");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				if (YRCPlatformUI.isTraceEnabled()) {
					YRCPlatformUI.trace(e);
				}
			}
			YRCPlatformUI.fireAction(AcademyPCAConstants.ATTR_REFRESH_ACTION);
		}
		
	}

	private void callApi(YRCApiContext context) {
		// TODO Auto-generated method stub

	}

	private String getFormId() {
		// TODO Auto-generated method stub
		return null;
	}
}