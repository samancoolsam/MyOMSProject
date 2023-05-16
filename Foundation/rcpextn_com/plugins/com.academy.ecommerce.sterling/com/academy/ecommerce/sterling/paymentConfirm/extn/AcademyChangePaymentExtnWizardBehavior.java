package com.academy.ecommerce.sterling.paymentConfirm.extn;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

//Sterling Imports
import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCXPathUtils;
import com.yantra.yfc.rcp.internal.YRCApiCaller;

public class AcademyChangePaymentExtnWizardBehavior extends
		YRCExtentionBehavior {
	
	@Override
	public void postSetModel(String namespace) {
		if("Order".equals(namespace)) {
			Element orderDetails = getModel(namespace);
			Element paymentMethod = (Element)YRCXPathUtils.evaluate(orderDetails, 
					"/Order/PaymentMethods/PaymentMethod[@NewPaymentMethod='Y' and @PaymentType='GIFT_CARD']", 
					XPathConstants.NODE);
			if(!YRCPlatformUI.isVoid(paymentMethod)) {
				paymentMethod.setAttribute("MaxChargeLimit", paymentMethod.getAttribute("FundsAvailable"));
			}
		}
		repopulateModel(namespace);
		super.postSetModel(namespace);
	}
	
	
	@Override
	public boolean preCommand(YRCApiContext apiContext) {
		if("changeOrder".equals(apiContext.getApiName())) {

			/*
			 * apiContext.getInputXml() return input of changeOrder.
			 */
			Document inputDocForEncriptService = apiContext.getInputXml();
			/*
			 * Test input Doc, if has Credit_CARD as payment Type, then only invoke
			 * AcademyEncriptCreditCardInformation Service.
			 * 
			 */ 
			Node creditCardPaymentType = (Node) YRCXPathUtils.evaluate(inputDocForEncriptService.getDocumentElement(), 
					"/Order/PaymentMethods/PaymentMethod[@NewPaymentMethod='Y' and @PaymentType='CREDIT_CARD']", 
					XPathConstants.NODE);
			if(!YRCPlatformUI.isVoid(creditCardPaymentType)) {
				YRCApiContext context = new YRCApiContext();
				YRCApiCaller syncApiCaller = new YRCApiCaller(context, true);
				context.setApiName(AcademyPCAConstants.ACADEMY_ENCRIPT_CC_COMMAND);
				context.setFormId(AcademyPCAConstants.ACADEMY_ENCRIPT_CC_COMMAND_FORMID);
				context.setInputXml(inputDocForEncriptService);
				syncApiCaller.invokeApi();
				inputDocForEncriptService = context.getOutputXml();
				apiContext.setInputXml(inputDocForEncriptService);	
			}
		}
		return super.preCommand(apiContext);
	}
	
}
