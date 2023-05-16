package com.academy.som.closemanifest.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.yantra.yfc.rcp.YRCEditorInput;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCXmlUtils;

public class AcademyCloseManifestAction
implements IWorkbenchWindowActionDelegate
{
  public static final String ACTION_ID = "com.academy.som.closemanifest.actions.AcademyCloseManifestAction";
  
  public void dispose() {}
  
  public void init(IWorkbenchWindow window) {}
  
  public void run(IAction action)
  {
    YRCPlatformUI.openEditor("com.academy.som.closemanifest.editors.AcademyCloseManifestEditor", new YRCEditorInput(YRCXmlUtils.createFromString("<CloseManifest />").getDocumentElement(), new String[] { "" }, "Close_Manifest_Task"));
  }
  
  public void selectionChanged(IAction action, ISelection selection) {}
}
