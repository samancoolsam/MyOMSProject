package com.academy.ecommerce.sterling.orderSummary.actions;

import org.eclipse.jface.action.IAction;

import com.academy.ecommerce.sterling.orderSummary.extn.AcademyGiftCardLoad;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCAction;
import com.yantra.yfc.rcp.YRCDesktopUI;

public class AcademyGiftCardLoadValidateShipAction extends YRCAction{

	public void execute(IAction action) {
		IYRCComposite comp = (IYRCComposite)YRCDesktopUI.getCurrentPage();
		 if (comp instanceof AcademyGiftCardLoad){
				try {
					//((AcademyGiftCardLoad)comp).getBehavior().handleValidateAndShipButtonSelected();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	protected boolean checkForErrors() {
		return false;
	}
	
	protected boolean checkForModifications() {
		return false;
	}
}
