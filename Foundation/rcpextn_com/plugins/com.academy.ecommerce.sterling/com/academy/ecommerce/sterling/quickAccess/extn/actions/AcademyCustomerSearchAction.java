package com.academy.ecommerce.sterling.quickAccess.extn.actions;

import org.eclipse.jface.action.IAction;

import com.academy.ecommerce.sterling.quickAccess.extn.AcademyQuickAccessExtnBehivor;
import com.yantra.yfc.rcp.YRCAction;
import com.yantra.yfc.rcp.YRCDesktopUI;
import com.yantra.yfc.rcp.YRCWizard;

public class AcademyCustomerSearchAction extends YRCAction {

	@Override
	public void execute(IAction arg0) {
		YRCWizard currentPage = (YRCWizard)YRCDesktopUI.getCurrentPage();
		AcademyQuickAccessExtnBehivor extnBehavior = (AcademyQuickAccessExtnBehivor)currentPage.getExtensionBehavior();
		extnBehavior.copyFromClipBoardAndFireSearch();

	}
	protected boolean checkForErrors() {
		return false;
	}

	protected boolean checkForModifications() {
		return false;
	}

}
