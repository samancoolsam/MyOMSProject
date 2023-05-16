package com.academy.som.searchshipment.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.academy.som.searchshipment.screens.AcademySearchShipmentExtnBehavior;
import com.academy.som.util.logging.AcademySIMTraceUtil;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCAction;
import com.yantra.yfc.rcp.YRCBaseBehavior;
import com.yantra.yfc.rcp.YRCBehavior;
import com.yantra.yfc.rcp.YRCDesktopUI;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCWizard;
import com.yantra.yfc.rcp.YRCWizardExtensionBehavior;

/**
 * Custom Action class done for Reset Hot Key in FormID :
 * "com.yantra.pca.ycd.rcp.tasks.common.advancedShipmentSearch.screens.YCDAdvancedShipmentSearchCriteriaPanel"
 * 
 * @author <a href="mailto:Kruthi.KM@cognizant.com">Kruthi KM</a> Copyright ©
 *         2006-2009 Sterling Commerce, Inc. All Rights Reserved.
 */
public class AcademyShipmentSearchResetAction extends YRCAction implements
		IWorkbenchWindowActionDelegate {
	AcademySearchShipmentExtnBehavior obj = new AcademySearchShipmentExtnBehavior();
	private static String CLASSNAME = "AcademyShipmentSearchResetAction";

	/**
	 * Superclass method called internally when the Hot Key F9 is pressed on
	 * Advance Shipment Search screen which resets the fields
	 */
	public void execute(IAction arg0) {
		final String methodName = "execute(action)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		AcademySIMTraceUtil.logMessage("F9 Pressed");

		// gets the current wizard i.e YCDAdvancedShipmentSearchWizard
		Composite comp = YRCDesktopUI.getCurrentPage();
		if (comp instanceof YRCWizard) {
			YRCWizard wizard = (YRCWizard) YRCDesktopUI.getCurrentPage();
			// gets the behaviour class i.e
			// YCDAdvancedShipmentSearchCriteriaPanelBehavior 
			YRCBehavior beh = wizard
					.getChildBehavior("com.yantra.pca.ycd.rcp.tasks.common.advancedShipmentSearch.screens.YCDAdvancedShipmentSearchCriteriaPanel");
			YRCExtentionBehavior extnbeh = beh.getExtensionBehavior();
			((AcademySearchShipmentExtnBehavior) extnbeh).resetFields();

		}
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}

	protected boolean checkForErrors() {
		return false;
	}

	protected boolean checkForModifications() {
		return false;
	}

}
