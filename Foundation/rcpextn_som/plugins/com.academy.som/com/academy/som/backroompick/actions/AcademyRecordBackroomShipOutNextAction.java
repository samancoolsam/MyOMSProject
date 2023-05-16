package com.academy.som.backroompick.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.academy.som.backroompick.screens.AcademyRecordBackroomShipOutExtnWizardBehavior;
import com.academy.som.util.AcademyPCAConstants;
import com.academy.som.util.logging.AcademySIMTraceUtil;
import com.yantra.yfc.rcp.YRCAction;
import com.yantra.yfc.rcp.YRCDesktopUI;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCWizard;

/**
 * Custom HotKey Action class done for "NEXT" button
 * in wizard PageIDs : "com.yantra.pca.sop.rcp.tasks.outboundexecution.pickup.recordbackroompick.wizardpages.SOPOBERecordBackroomPickShipOut"
 *					 : "com.yantra.pca.sop.rcp.tasks.outboundexecution.pickup.recordbackroompick.wizardpages.SOPRecordBackroomPickScanMode" 
 *
 * @author <a href="mailto:KaushikN.Sanji@cognizant.com">Kaushik N Sanji</a>
 * Copyright © 2006-2009 Sterling Commerce, Inc. All Rights Reserved.
 */
public class AcademyRecordBackroomShipOutNextAction extends YRCAction implements
		IWorkbenchWindowActionDelegate {

	private static String CLASSNAME="AcademyRecordBackroomShipOutNextAction";
	
	/**
	 * Superclass method called internally when a HotKey bound to 
	 * a button is pressed. In this case, the HotKey is F11
	 * for button "Next"
	 */
	public void execute(IAction action) {
		final String methodName="execute(action)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		AcademySIMTraceUtil.logMessage("F11 Pressed");
		
		YRCWizard currentWizard = (YRCWizard) YRCDesktopUI.getCurrentPage(); //fetching the current page's wizard
		YRCExtentionBehavior exbhvr = currentWizard.getExtensionBehavior();	//fetching the wizard's extension behavior
		if (exbhvr instanceof AcademyRecordBackroomShipOutExtnWizardBehavior) {			
			AcademyRecordBackroomShipOutExtnWizardBehavior shipOutExtnBehavior = (AcademyRecordBackroomShipOutExtnWizardBehavior) exbhvr;
			if(shipOutExtnBehavior.doProceedAction()){ //calling the doProceedAction() method of wizard extension behavior
				if(!shipOutExtnBehavior.completePickDone){
					AcademySIMTraceUtil.logMessage("invoking OOB Next BUtton");
					//call the OOB NEXT action to proceed to the Next page when complete pick is not done
					AcademySIMTraceUtil.logMessage("calling OOB Next Action : "+AcademyPCAConstants.BTN_NEXT_OOB_ACTION_ID+" :: START");
					YRCPlatformUI.fireAction(AcademyPCAConstants.BTN_NEXT_OOB_ACTION_ID);
					AcademySIMTraceUtil.logMessage("calling OOB Next Action : "+AcademyPCAConstants.BTN_NEXT_OOB_ACTION_ID+" :: END");
				}
			}
		}
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}

}
