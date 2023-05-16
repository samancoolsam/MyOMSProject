package com.academy.ecommerce.sterling.alertConsole.extn;

import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.academy.ecommerce.sterling.util.XMLUtil;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCWizardExtensionBehavior;

public class AcademyCloseAlertExtnBehavior extends YRCWizardExtensionBehavior {

	private String status = "";

	//@Override
	public YRCValidationResponse validateButtonClick(String fieldName) {

		if ("btnAssign".equalsIgnoreCase(fieldName)) {
			String strNotes = getFieldValue("tANotes");
			if (YRCPlatformUI.isVoid(strNotes)) {
				YRCPlatformUI.showError(AcademyPCAConstants.ATTR_ERROR,
						"Please complete the notes field");
				return new YRCValidationResponse(
						YRCValidationResponse.YRC_VALIDATION_ERROR,
						"Please complete the notes field");
			}
		}
		return super.validateButtonClick(fieldName);
	}

	@Override
	public boolean preCommand(YRCApiContext apiContext) {

		return super.preCommand(apiContext);
	}

	@Override
	public void postSetModel(String namespace) {
		YRCPlatformUI.trace(namespace);
		super.postSetModel(namespace);
	}

	@Override
	public Object getInputObject() {
		// TODO Auto-generated method stub
		return super.getInputObject();
	}

	@Override
	public IYRCComposite createPage(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initPage(String pageBeingShown) {
		super.initPage(pageBeingShown);
		Object obj = getInputObject();
		if (isInitPageInProgress) {
			if (obj != null && obj instanceof Element) {
				YRCPlatformUI.trace(XMLUtil.getElementXMLString((Element) obj));
				Element inbox = (Element) obj;
				if (inbox != null
						&& "OPEN"
								.equalsIgnoreCase(inbox.getAttribute("Status"))) {
					status = inbox.getAttribute("Status");
				}
			}
		}

	}

	

	@Override
	public void pageBeingDisposed(String arg0) {
		// TODO Auto-generated method stub

	}

}
