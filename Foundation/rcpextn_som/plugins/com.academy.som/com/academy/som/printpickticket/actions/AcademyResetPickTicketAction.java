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
 * Custom HotKey Action class done for "Reset Pick Ticket" button
 * Class created as part of STL-1493 on 03/25/2016
 * @author <a href="mailto:Shruthi.KenkareNarendrababu@cognizant.com">Shruthi K N</a>
 *
 */
public class AcademyResetPickTicketAction extends YRCAction {
	
	private static String CLASSNAME="AcademyResetPickTicketAction";

	public void execute(IAction action) {
		final String methodName="execute(action)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		//AcademySIMTraceUtil.logMessage("F4 Pressed");
		
		YRCWizard currentWizard = (YRCWizard) YRCDesktopUI.getCurrentPage(); //fetching the current page's wizard
		YRCExtentionBehavior exbhvr = currentWizard.getExtensionBehavior(); //fetching the wizard's extension behavior
		if (exbhvr instanceof AcademyPrintPickTicketForItemExtnWizardBehavior) {			
			AcademyPrintPickTicketForItemExtnWizardBehavior pptExtnBehavior = (AcademyPrintPickTicketForItemExtnWizardBehavior) exbhvr;
			// If Reset Pick Ticket button is clicked invoke the below service
			AcademySIMTraceUtil.logMessage(methodName+" :: Invoke service for  Reset Pick Tickets: ");
			pptExtnBehavior.callServiceForResetPickTickets();			
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
