package com.academy.som.backroompick.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.academy.som.backroompick.screens.AcademyRecordBackroomShipOutExtnWizardBehavior;
import com.academy.som.util.logging.AcademySIMTraceUtil;
import com.yantra.yfc.rcp.YRCAction;
import com.yantra.yfc.rcp.YRCDesktopUI;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCWizard;

/**
 * Custom HotKey Action class done for "CONFIRM" button
 * in wizard PageID : "com.yantra.pca.sop.rcp.tasks.outboundexecution.pickup.recordbackroompick.wizardpages.SOPRecordBackroomShortageResolution"
 * 
 * @author <a href="mailto:KaushikN.Sanji@cognizant.com">Kaushik N Sanji</a>
 * Copyright © 2006-2009 Sterling Commerce, Inc. All Rights Reserved.
 */
public class AcademyRecordBackroomShipOutConfirmAction extends YRCAction
		implements IWorkbenchWindowActionDelegate {
	
	private static String CLASSNAME="AcademyRecordBackroomShipOutConfirmAction";
	
	/**
	 * Superclass method called internally when a HotKey bound to 
	 * a button is pressed. In this case, the HotKey is F8
	 * for button "Confirm"
	 */
	public void execute(IAction action) {
		final String methodName="execute(action)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		AcademySIMTraceUtil.logMessage("F8 Pressed");
		
		YRCWizard currentWizard = (YRCWizard) YRCDesktopUI.getCurrentPage(); //fetching the current page's wizard
		YRCExtentionBehavior exbhvr = currentWizard.getExtensionBehavior(); //fetching the wizard's extension behavior
		if (exbhvr instanceof AcademyRecordBackroomShipOutExtnWizardBehavior) {			
			AcademyRecordBackroomShipOutExtnWizardBehavior shipOutExtnBehavior = (AcademyRecordBackroomShipOutExtnWizardBehavior) exbhvr;
			shipOutExtnBehavior.doConfirmAction(); //calling the doConfirmAction() method of wizard extension behavior 
		}
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}
}
