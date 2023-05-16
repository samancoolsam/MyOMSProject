/**
 * 
 */
package com.academy.ecommerce.sterling.quickAccess.extn.actions;

import org.eclipse.jface.action.IAction;

import com.academy.ecommerce.sterling.quickAccess.extn.AcademyGiftBalChk;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCAction;
import com.yantra.yfc.rcp.YRCDesktopUI;

/**
 * @author sahmed
 * 
 */
public class AcademyCheckBalanceAction extends YRCAction {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yantra.yfc.rcp.YRCAction#execute(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void execute(IAction arg0) {
		IYRCComposite comp = (IYRCComposite) YRCDesktopUI.getCurrentPage();
		if (comp instanceof AcademyGiftBalChk) {
			try {
				((AcademyGiftBalChk) comp).getBehavior()
						.checkBalanceButtonSelected();
			} catch (Exception e) {
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
