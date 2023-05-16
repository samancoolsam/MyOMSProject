package com.academy.som.printpickticket.actions;

import org.eclipse.jface.action.IAction;

import com.academy.som.printpickticket.screens.AcademyPrintPickTicketForItemExtnWizardBehavior;
import com.academy.som.util.AcademyPCAConstants;
import com.academy.som.util.AcademyPCAErrorMessagesInterface;
import com.academy.som.util.logging.AcademySIMTraceUtil;
import com.yantra.yfc.rcp.YRCAction;
import com.yantra.yfc.rcp.YRCDesktopUI;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCWizard;

/**
 * Custom HotKey Action class done for "Print Item Pick Ticket" button
 * in wizard PageID : "com.yantra.pca.sop.rcp.tasks.outboundexecution.pickup.printpickticket.wizardpages.SOPOutboundPrintPickTicket"
 * 
 * @author <a href="mailto:KaushikN.Sanji@cognizant.com">Kaushik N Sanji</a>
 * Copyright © 2006-2009 Sterling Commerce, Inc. All Rights Reserved.
 */
public class AcademyPrintPickTicketForItemAction extends YRCAction {
	
	private static String CLASSNAME="AcademyPrintPickTicketForItemAction";

	/**
	 * Superclass method called internally when a HotKey bound to 
	 * a button is pressed. In this case, the HotKey is F4
	 * for button "Print Item Pick Ticket"
	 */
	public void execute(IAction action) {
		final String methodName="execute(action)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		AcademySIMTraceUtil.logMessage("F4 Pressed");
		
		YRCWizard currentWizard = (YRCWizard) YRCDesktopUI.getCurrentPage(); //fetching the current page's wizard
		YRCExtentionBehavior exbhvr = currentWizard.getExtensionBehavior(); //fetching the wizard's extension behavior
		if (exbhvr instanceof AcademyPrintPickTicketForItemExtnWizardBehavior) {			
			AcademyPrintPickTicketForItemExtnWizardBehavior pptExtnBehavior = (AcademyPrintPickTicketForItemExtnWizardBehavior) exbhvr;
			//fetching the Batch Number selected for reprint
			String batchNoSelected = pptExtnBehavior.getFieldValue(AcademyPCAConstants.CUSTOM_CMB_BATCH_GRP);
			if(batchNoSelected!=null && batchNoSelected.trim().length()!=0){
				//if Pick ticket Batch Number is selected for reprint then call service for reprinting item pick tickets
				AcademySIMTraceUtil.logMessage(methodName+" :: Batch Number selected: "+batchNoSelected);
				pptExtnBehavior.callServiceForReprintItemPickTickets();
			}
			else{
				//if Pick ticket Batch Number is not selected for reprint then throw an error message
				AcademySIMTraceUtil.logMessage(methodName+" :: Batch Number selected: NULL");
				YRCPlatformUI.showError(AcademyPCAConstants.STRING_ERROR,
						YRCPlatformUI.getString(AcademyPCAErrorMessagesInterface.BATCH_NO_VAL_ERR_MSG_KEY));
			}
		}
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}

	/**
	 * This method is used to check if the editor has to be checked for errors.
	 * 
	 * @return boolean
	 * 			<br/> - returns <b>False</b> to skip the check
	 */
	protected boolean checkForErrors() {
		return false;
	}

	/**
	 * This method is used to check if the editor has to be checked for
	 * modifications.
	 * 
	 * @return boolean
	 * 			<br/> - returns <b>False</b> to skip the check
	 */
	protected boolean checkForModifications() {
		return false;
	}

}
