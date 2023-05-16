package com.academy.som.closemanifest.wizards;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.academy.som.closemanifest.wizardpages.AcademyCloseManifest;
import com.academy.som.util.logging.AcademySIMTraceUtil;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCContainerTitleManager;
import com.yantra.yfc.rcp.YRCApplication;
import com.yantra.yfc.rcp.YRCContainerTitleManager;
import com.yantra.yfc.rcp.YRCDesktopUI;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCWizard;
import com.yantra.yfc.rcp.YRCWizardBehavior;



public class AcademyCloseManifestWizardBehavior
extends YRCWizardBehavior
{
private String statusMessage = "DUMMY";
public static final String GetManifestListCommand = "GetManifestList";
public static final String CloseManifestCommand = "CloseManifest";
public static final String ChangeShipmentCommand = "ChangeShipment";
private Object inputObject;

public AcademyCloseManifestWizardBehavior(Composite ownerComposite, String formId, Object inputObject)
{
  super(ownerComposite, formId, inputObject);
  this.inputObject = inputObject;
  init();
}

public void init() {}

public IYRCComposite createPage(String pageIdToBeShown, Composite pnlRoot)
{
  IYRCComposite page = null;
  if (pageIdToBeShown.equalsIgnoreCase("com.academy.som.closemanifest.wizardpages.AcademyCloseManifest"))
  {
	AcademyCloseManifest page1 = new AcademyCloseManifest(pnlRoot, 0);
    page1.setWizBehavior(this);
    page = page1;
    YRCContainerTitleManager.getTitleProvider().setTitle("Close Manifest");
    YRCPlatformUI.setMessage("Instruction_Close_Manifest");
  }
  return page;
}

public String getCustomerMessage()
{
  //SOPWorkbenchAdvisor.getSopHdr().setTitle("Title_Close_Manifest");
  YRCContainerTitleManager.getTitleProvider().setTitle("Title_Close_Manifest");
//	YRCDesktopWorkbenchWindowAdvisor.
  if (getCurrentPageID().equals("com.academy.som.closemanifest.wizardpages.AcademyCloseManifest")) {
    return "Instruction_Close_Manifest";
  }
  return null;
}

public String getStatusMessage()
{
  return this.statusMessage;
}

public void setStatusMessage(String statusMessage)
{
  this.statusMessage = statusMessage;
  pageChanged();
}

public void pageBeingDisposed(String pageToBeDisposed) {}

public void setStatusMessageKey(String str) {}

/**
 * Superclass method to set the screen models for the custom API Calls
 * and to handle failure of API calls
 * @param context
 * 			<br/> - the context in which the API is called
 */
public void handleApiCompletion(YRCApiContext context) {
	final String methodName="handleApiCompletion(context)";
//	AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
	String strApiName = context.getApiName();
	if(context.getInvokeAPIStatus()<0){}
}
}
