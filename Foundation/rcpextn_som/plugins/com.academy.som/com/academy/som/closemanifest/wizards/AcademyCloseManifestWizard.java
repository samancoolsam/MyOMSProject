package com.academy.som.closemanifest.wizards;

import org.eclipse.swt.widgets.Composite;

import com.yantra.yfc.rcp.IYRCPanelHolder;
import com.yantra.yfc.rcp.YRCWizard;
import com.yantra.yfc.rcp.YRCWizardBehavior;

public class AcademyCloseManifestWizard 
extends YRCWizard
{
  private AcademyCloseManifestWizardBehavior myBehavior;
  public static final String WIZARD_ID = "com.academy.som.closemanifest.wizards.AcademyCloseManifestWizard";
  
  public AcademyCloseManifestWizard(String wizardId, Composite parent, int style, Object inputObject)
  {
    super(wizardId, parent, inputObject, style);
    initializeWizard();
    start();
  }
  
  public String getFormId()
  {
    return "com.academy.som.closemanifest.wizards.AcademyCloseManifestWizard";
  }
  
  protected YRCWizardBehavior createBehavior()
  {
    this.myBehavior = new AcademyCloseManifestWizardBehavior(this, "com.academy.som.closemanifest.wizards.AcademyCloseManifestWizard", this.wizardInput);
    return this.myBehavior;
  }
  
  public IYRCPanelHolder getPanelHolder()
  {
    return null;
  }
  
  public AcademyCloseManifestWizardBehavior getMyBehavior()
  {
    return this.myBehavior;
  }
  
  public void setMyBehavior(AcademyCloseManifestWizardBehavior myBehavior)
  {
    this.myBehavior = myBehavior;
  }
  
  public String getHelpId()
  {
    return null;
  }
}
