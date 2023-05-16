package com.academy.ecommerce.sterling.paymentConfirm.extn;

import org.w3c.dom.Element;

import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCExtentionBehavior;

public class AcademyAddRemoveCouponExtnbehavior extends YRCExtentionBehavior {
	@Override
	public boolean preCommand(YRCApiContext apiContext) {
		
		String strApiName = apiContext.getApiName();
		
		if (strApiName.equals("changeOrder") && 
				"com.yantra.pca.ycd.rcp.tasks.common.screens.YCDAddCouponComposite".equals(apiContext.getFormId())) {
			Element orderDetails = apiContext.getInputXml().getDocumentElement();
			if(isDirty()) {
				orderDetails.setAttribute("ExtnIsPageDirty", "Y");
			}
			orderDetails.setAttribute("AcademyInvokedFrom", "PAYMENT");
		}
		return super.preCommand(apiContext);
	}
}
