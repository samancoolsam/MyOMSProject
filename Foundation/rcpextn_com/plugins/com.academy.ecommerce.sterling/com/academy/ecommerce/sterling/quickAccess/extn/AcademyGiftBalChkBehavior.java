/**
 * 
 */
package com.academy.ecommerce.sterling.quickAccess.extn;

import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.academy.ecommerce.sterling.util.XMLUtil;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.internal.YRCApiCaller;

/**
 * @author sahmed
 * 
 */
public class AcademyGiftBalChkBehavior extends YRCBehavior {
	private Element inElm = null;

	private AcademyGiftBalChk page = null;

	private YRCApiContext context;

	public AcademyGiftBalChkBehavior(Composite ownerComposite, String form_id,
			Element inElm) {
		super(ownerComposite, form_id, inElm);
		this.inElm = inElm;
		this.page = (AcademyGiftBalChk) ownerComposite;
	}

	/*
	 * Below method will be invoked when the Check Balance of Gift Card Button
	 * is selected
	 */
	/*
	 * Response XML from AcademyGiftCardLookupService is: <PaymentMethod
	 * GiftCardNo="" FundsAvailable="" ValidationResult="" ErrorCode=""
	 * ErrorDescription=""/>
	 */
	public void checkBalanceButtonSelected() {
		YRCPlatformUI
				.trace("###### Inside checkBalanceButtonSelected Method############");
		Element eleGiftCardRequest = getPaymentMethodModel();
		String strGiftCardNumber = eleGiftCardRequest.getAttribute("SvcNo");
		String strPINNumber = eleGiftCardRequest
				.getAttribute("PaymentReference1");
		if ((!YRCPlatformUI.isVoid(eleGiftCardRequest))
				&& (!YRCPlatformUI.isVoid(strGiftCardNumber))) {
			// Start fix for #3735 - as part of R25			
			if(strGiftCardNumber.length() != 13){
				YRCPlatformUI.showError(AcademyPCAConstants.ATTR_ERROR, "The GC# is invalid.");
				eleGiftCardRequest.setAttribute("SvcNo", "");
				eleGiftCardRequest.setAttribute("PaymentReference1", "");
				setModel("PaymentMethod", eleGiftCardRequest);
				return;
			}else if(strGiftCardNumber.length() == 13){
				try{
					Long.parseLong(strGiftCardNumber);
				}catch(NumberFormatException nfe){
					YRCPlatformUI.showError(AcademyPCAConstants.ATTR_ERROR, "The GC# is invalid.");
					eleGiftCardRequest.setAttribute("SvcNo", "");
					eleGiftCardRequest.setAttribute("PaymentReference1", "");
					setModel("PaymentMethod", eleGiftCardRequest);
					return;
				}				
			}
			// End #3735
			Document docGiftCardBalanceLookUp = callGiftCardLookUpService(eleGiftCardRequest
					.getOwnerDocument());
			YRCPlatformUI
					.trace("Response From ISD for Gift Card Balance Look Up is:"
							+ XMLUtil.getXMLString(docGiftCardBalanceLookUp));
			if (!YRCPlatformUI.isVoid(docGiftCardBalanceLookUp)) {
				Element eleGiftCardBalanceLookUpResponse = docGiftCardBalanceLookUp
						.getDocumentElement();
				String strError =eleGiftCardBalanceLookUpResponse.getAttribute("ErrorCode");
				String strErrorDesc=eleGiftCardBalanceLookUpResponse.getAttribute("ErrorDescription");
				if ("".equals(strError)) {
					String strFundsAvailable = eleGiftCardBalanceLookUpResponse
							.getAttribute("FundsAvailable");
					String strValidationResult = eleGiftCardBalanceLookUpResponse
							.getAttribute("ValidationResult");
					StringBuffer strBalance = new StringBuffer();
					strBalance.append("Gift Card No: ");
					strBalance.append(strGiftCardNumber);
					strBalance.append("  has a Balance of:$ ");
					strBalance.append(strFundsAvailable);
					if ("Y".equals(strValidationResult)) {
						eleGiftCardRequest.setAttribute("SvcNo", "");
						eleGiftCardRequest
								.setAttribute("PaymentReference1", "");
						eleGiftCardRequest.setAttribute("Balance", strBalance
								.toString());
						setModel("PaymentMethod", eleGiftCardRequest);
					} else {

						YRCPlatformUI.showError(AcademyPCAConstants.ATTR_ERROR,
								strErrorDesc);
						eleGiftCardRequest.setAttribute("SvcNo", "");
						eleGiftCardRequest
								.setAttribute("PaymentReference1", "");
						setModel("PaymentMethod", eleGiftCardRequest);
					}
				} else {
					YRCPlatformUI.showError(AcademyPCAConstants.ATTR_ERROR,
							strErrorDesc);
					eleGiftCardRequest.setAttribute("SvcNo", "");
					eleGiftCardRequest.setAttribute("PaymentReference1", "");
					setModel("PaymentMethod", eleGiftCardRequest);
				}
			}
		} else {
			YRCPlatformUI.showError(AcademyPCAConstants.ATTR_ERROR, "GC# must be entered");
			eleGiftCardRequest.setAttribute("SvcNo", "");
			eleGiftCardRequest.setAttribute("PaymentReference1", "");
			setModel("PaymentMethod", eleGiftCardRequest);
		}

	}

	private Document callGiftCardLookUpService(Document docGiftCardRequest) {
		YRCApiContext context = new YRCApiContext();
		YRCApiCaller syncapiCaller = new YRCApiCaller(context, true);
		context.setApiName("AcademyGiftCardLookupService");
		context
				.setFormId("com.academy.ecommerce.sterling.quickAccess.extn.AcademyGiftBalChk");
		context.setInputXml(docGiftCardRequest);
		syncapiCaller.invokeApi();
		Document docOutputGiftCardBalanceLookUp = context.getOutputXml();
		return docOutputGiftCardBalanceLookUp;
	}

	private Element getPaymentMethodModel() {
		Element eleGiftCardBalanceRequest = getTargetModel("PaymentMethod");
		YRCPlatformUI.trace("Model Retrieved is:"
				+ XMLUtil.getElementXMLString(eleGiftCardBalanceRequest));
		return eleGiftCardBalanceRequest;
	}
}