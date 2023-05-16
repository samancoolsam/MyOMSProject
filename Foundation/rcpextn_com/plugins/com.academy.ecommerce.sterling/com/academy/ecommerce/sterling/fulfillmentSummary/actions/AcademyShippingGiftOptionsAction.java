package com.academy.ecommerce.sterling.fulfillmentSummary.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Composite;

import com.academy.ecommerce.sterling.fulfillmentSummary.extn.AcademyFulfillmentSummaryExtnBehavior;
import com.yantra.yfc.rcp.YRCAction;
import com.yantra.yfc.rcp.YRCDesktopUI;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCWizard;

public class AcademyShippingGiftOptionsAction extends YRCAction{
	
	public void execute(IAction arg0) {
		Composite wizard = YRCDesktopUI.getCurrentPage();
		if(wizard instanceof YRCWizard){
			Composite wizardPage = ((YRCWizard)wizard).getCurrentPage(); 
			AcademyFulfillmentSummaryExtnBehavior fulfillmentScreen = null;
			if(!YRCPlatformUI.isVoid(fulfillmentScreen)){
				AcademyFulfillmentSummaryExtnBehavior fulfillmentScreenBehavior = (AcademyFulfillmentSummaryExtnBehavior) fulfillmentScreen.getExtensionBehavior();
			//	fulfillmentScreenBehavior.openShipmentGiftOptionsPopup();
			}
	
		// TODO Auto-generated method stub
		}
	}
}
