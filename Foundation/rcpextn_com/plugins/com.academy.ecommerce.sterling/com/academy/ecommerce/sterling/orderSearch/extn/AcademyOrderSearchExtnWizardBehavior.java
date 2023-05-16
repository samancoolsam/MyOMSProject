package com.academy.ecommerce.sterling.orderSearch.extn;

//Sterling Imports
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCWizardExtensionBehavior;
import com.yantra.yfc.rcp.YRCXmlUtils;

public class AcademyOrderSearchExtnWizardBehavior extends YRCWizardExtensionBehavior 
{
	@Override
	public boolean preCommand(YRCApiContext apiContext) {
		if("getOrderList".equals(apiContext.getApiName())) {
			Document apiInput = apiContext.getInputXml();
			Element paymentSearchInput = YRCXmlUtils.getXPathElement(apiInput.getDocumentElement(), "Order/PaymentMethod/ComplexQuery/Or");
			if(!YRCPlatformUI.isVoid(paymentSearchInput)) {
				Element expression = YRCXmlUtils.getChildElement(paymentSearchInput, "Exp");
				Element ccDisplayExp = YRCXmlUtils.getCopy(expression, true);
				ccDisplayExp.setAttribute("Name", "DisplayCreditCardNo");
				//Start: PayPal Changes
				ccDisplayExp.setAttribute("Name", "PaymentReference2");
				//End: PayPal Changes
				YRCXmlUtils.importElement(paymentSearchInput, ccDisplayExp);
			}
		}
		return super.preCommand(apiContext);
	}

	@Override
	public IYRCComposite createPage(String arg0)
	{
		return null;
	}

	@Override
	public void pageBeingDisposed(String arg0)
	{
	}
	
}