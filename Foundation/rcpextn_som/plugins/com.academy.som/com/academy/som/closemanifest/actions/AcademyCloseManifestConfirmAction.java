package com.academy.som.closemanifest.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.academy.som.closemanifest.wizardpages.AcademyCloseManifest;
import com.academy.som.closemanifest.wizardpages.AcademyCloseManifestBehavior;
import com.academy.som.closemanifest.wizards.AcademyCloseManifestWizard;
import com.yantra.yfc.rcp.YRCDesktopUI;

public class AcademyCloseManifestConfirmAction 
implements IWorkbenchWindowActionDelegate {

	  public static final String ACTION_ID = "Close_Manifest_Confirm_Action";
	  
	  public void dispose() {}
	  
	  public void init(IWorkbenchWindow window) {}
	  
	  public void run(IAction action)
	  {
		  AcademyCloseManifestWizard wizard = (AcademyCloseManifestWizard)YRCDesktopUI.getCurrentPage();
	    Composite currentPage = wizard.getCurrentPage();
	    if ((currentPage instanceof AcademyCloseManifest))
	    {
	      AcademyCloseManifest page = (AcademyCloseManifest)currentPage;
	      AcademyCloseManifestBehavior behavior = page.getMyBehavior();
	      behavior.showNextPage();
	    }
	  }
	  
	  public void selectionChanged(IAction action, ISelection selection) {}
}
