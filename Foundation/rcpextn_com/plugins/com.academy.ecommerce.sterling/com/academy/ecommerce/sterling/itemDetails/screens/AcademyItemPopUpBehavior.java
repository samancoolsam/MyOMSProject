package com.academy.ecommerce.sterling.itemDetails.screens;

import org.eclipse.swt.widgets.Composite;
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.yantra.yfc.rcp.YRCBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;

public class AcademyItemPopUpBehavior extends YRCBehavior 
{
	
	AcademyItemPopUp view = null;
    
	public AcademyItemPopUpBehavior(Composite ownerComposite, String formId) {
        super(ownerComposite, formId);
        
        this.view = (AcademyItemPopUp) ownerComposite;
    }
	
	public void  close()
	{
		view.getShell().close();
		YRCPlatformUI.fireAction(AcademyPCAConstants.ATTR_REFRESH_ACTION);
	}	

}
