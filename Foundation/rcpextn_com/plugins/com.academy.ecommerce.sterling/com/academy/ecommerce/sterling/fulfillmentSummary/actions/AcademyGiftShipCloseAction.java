package com.academy.ecommerce.sterling.fulfillmentSummary.actions;

import org.eclipse.jface.action.IAction;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCAction;
import com.yantra.yfc.rcp.YRCDesktopUI;

//Project Imports
import com.academy.ecommerce.sterling.fulfillmentSummary.screens.AcademyGiftShipPopup;

//Misc Imports - NONE

public class AcademyGiftShipCloseAction extends YRCAction{
	public void execute(IAction action) {
		IYRCComposite comp = (IYRCComposite)YRCDesktopUI.getCurrentPage();
		if (comp instanceof AcademyGiftShipPopup)
		{
			((AcademyGiftShipPopup)comp).getBehavior().handleCloseButtonSelected();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.yantra.yfc.rcp.YRCAction#checkForModifications()
	 */
	
	protected boolean checkForErrors() {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.yantra.yfc.rcp.YRCAction#checkForModifications()
	 */
	protected boolean checkForModifications() {
		return false;
	}
}
