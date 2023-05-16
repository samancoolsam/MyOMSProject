package com.academy.som.printpickticket.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.academy.som.printpickticket.screens.AcademyPrintPickTicketForItemExtnWizardBehavior;
import com.academy.som.util.logging.AcademySIMTraceUtil;
import com.yantra.yfc.rcp.YRCAction;
import com.yantra.yfc.rcp.YRCDesktopUI;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCWizard;

/**
 * Custom HotKey Action class done for "Print" button that prints all pending Pick Tickets
 * in wizard PageID : "com.yantra.pca.sop.rcp.tasks.outboundexecution.pickup.printpickticket.wizardpages.SOPOutboundPrintPickTicket"
 * 
 * @author <a href="mailto:KaushikN.Sanji@cognizant.com">Kaushik N Sanji</a>
 * Copyright © 2006-2009 Sterling Commerce, Inc. All Rights Reserved.
 */
public class AcademyPrintShipmentPickTicketsAction extends YRCAction implements
		IWorkbenchWindowActionDelegate {

	private static String CLASSNAME="AcademyPrintShipmentPickTicketsAction";
	
	/**
	 * Superclass method called internally when a HotKey bound to 
	 * a button is pressed. In this case, the HotKey is F8
	 * for button "Print"
	 */
	public void execute(IAction action) {
		final String methodName="execute(action)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		AcademySIMTraceUtil.logMessage("F8 Pressed");
		
		YRCWizard currentWizard = (YRCWizard) YRCDesktopUI.getCurrentPage(); //fetching the current page's wizard
		YRCExtentionBehavior exbhvr = currentWizard.getExtensionBehavior(); //fetching the wizard's extension behavior
		if (exbhvr instanceof AcademyPrintPickTicketForItemExtnWizardBehavior) {			
			AcademyPrintPickTicketForItemExtnWizardBehavior pptExtnBehavior = (AcademyPrintPickTicketForItemExtnWizardBehavior) exbhvr;
			pptExtnBehavior.callServiceForShipmentPickTickets(); //calling service for printing shipment and item pick tickets
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
