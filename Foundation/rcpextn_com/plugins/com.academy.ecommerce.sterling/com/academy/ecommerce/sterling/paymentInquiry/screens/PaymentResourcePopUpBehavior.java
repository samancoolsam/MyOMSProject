package com.academy.ecommerce.sterling.paymentInquiry.screens;

//Java Imports -NONE

//Sterling Imports
import org.eclipse.swt.widgets.Composite;
import com.yantra.yfc.rcp.YRCBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;

//Project Imports
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
//Misc - NONE


public class PaymentResourcePopUpBehavior extends YRCBehavior 
{
	
	PaymentResourcePopUp view = null;
    
	public PaymentResourcePopUpBehavior(Composite ownerComposite, String formId) {
        super(ownerComposite, formId);
        
        this.view = (PaymentResourcePopUp) ownerComposite;
    }
	
	public void  close()
	{
		view.getShell().close();
		YRCPlatformUI.fireAction(AcademyPCAConstants.ATTR_REFRESH_ACTION);
	}	

}
