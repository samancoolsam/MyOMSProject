package com.academy.ecommerce.sterling.alertConsole.extn;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCDesktopUI;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;

public class AcademyAlertNotesExtnBehavior extends YRCExtentionBehavior
{
	@Override
	public void postCommand(YRCApiContext apiContext)
	{
		Composite currentPage = YRCDesktopUI.getCurrentPage();
		if(!YRCPlatformUI.isVoid(currentPage))
		{
			Shell shell = currentPage.getShell();
			if(!YRCPlatformUI.isVoid(shell))
			{
				shell.setText("Add Notes");
			}
		}

		super.postCommand(apiContext);
	}
}
