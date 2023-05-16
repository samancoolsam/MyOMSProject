package com.academy.ecommerce.sterling.orderSummary.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Display;
import com.yantra.yfc.rcp.YRCAction;

/**
* @author sahmed
 *
*/
public class TaxIdPopupCancelAction extends YRCAction{

		public void execute(IAction action) {
			// Do nothing on cancel. Just close the active shell.
			Display.getCurrent().getActiveShell().close();
		}

	}

