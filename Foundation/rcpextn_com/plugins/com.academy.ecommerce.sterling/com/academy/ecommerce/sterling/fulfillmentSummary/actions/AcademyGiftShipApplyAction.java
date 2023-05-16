package com.academy.ecommerce.sterling.fulfillmentSummary.actions;

import org.eclipse.jface.action.IAction;

import com.academy.ecommerce.sterling.fulfillmentSummary.screens.AcademyGiftShipPopup;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCAction;
import com.yantra.yfc.rcp.YRCDesktopUI;

public class AcademyGiftShipApplyAction extends YRCAction{

	public void execute(IAction action) {
		IYRCComposite comp = (IYRCComposite)YRCDesktopUI.getCurrentPage();
		 if (comp instanceof AcademyGiftShipPopup){
			((AcademyGiftShipPopup)comp).getBehavior().handleApplyButtonSelected();
		}
	}
	
	protected boolean checkForErrors() {
		return false;
	}
	
	protected boolean checkForModifications() {
		return false;
	}
}
