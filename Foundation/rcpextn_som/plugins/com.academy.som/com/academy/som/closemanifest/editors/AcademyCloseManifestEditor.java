package com.academy.som.closemanifest.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Element;

import com.academy.som.closemanifest.wizards.AcademyCloseManifestWizard;
import com.yantra.yfc.rcp.YRCEditorInput;
import com.yantra.yfc.rcp.YRCEditorPart;
import com.yantra.yfc.rcp.YRCPlatformUI;

public class AcademyCloseManifestEditor 
extends YRCEditorPart {
	
	  public static final String ID_EDITOR = "com.yantra.pca.sop.rcp.tasks.outboundexecution.shipout.closemanifest.editors.SOPCloseManifestEditor";
	  private String titleKey = "CloseManifest_Editor";
	  private Composite pnlRoot;
	  
	  public void doSave(IProgressMonitor monitor) {}
	  
	  public void doSaveAs() {}
	  
	  public boolean isDirty()
	  {
	    return false;
	  }
	  
	  public boolean isSaveAsAllowed()
	  {
	    return false;
	  }
	  
	  public Composite createPartControl(Composite parent, String taskID)
	  {
		YRCEditorInput input = (YRCEditorInput)getEditorInput();
		input.setTaskName(taskID);
		this.pnlRoot = new AcademyCloseManifestWizard("com.academy.som.closemanifest.wizards.AcademyCloseManifestWizard", parent, 0, input);
	    this.pnlRoot.setData("yrc:ownerPart", this);
	
	    
//	    Element e = input.getXml();
//	    e.setAttribute("ChangeTaskName", "YCD_GLOBAL_RELATED");
	    setPartName(YRCPlatformUI.getString(this.titleKey));
	   // setTitle(this.titleKey);
	    return this.pnlRoot;
	  }
	  
	  public void postSetFocus()
	  {
	    this.pnlRoot.setFocus();
	  }
	  
	  public void showBusy(boolean busy)
	  {
	    if (busy)
	    {
	      setPartName(YRCPlatformUI.getString("wait_title"));
	      this.pnlRoot.setCursor(YRCPlatformUI.getCursor(1));
	    }
	    else
	    {
	      setPartName(YRCPlatformUI.getString(this.titleKey));
	      this.pnlRoot.setCursor(null);
	    }
	  }
}
