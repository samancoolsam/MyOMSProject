//package declaration
package com.academy.ecommerce.sterling.userexits;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.util.AcademyDirectPaypalOtherCollectionUEImplUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.pca.ycd.ue.utils.YCDPaymentUEXMLManager;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSExtnPaymentCollectionInputStruct;
import com.yantra.yfs.japi.YFSExtnPaymentCollectionOutputStruct;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSCollectionOthersUE;

/**
 * 
 * class to perform the payment operations for PayPal,paypal payment type.
 *
 */

public class AcademyOtherCollectionUEImpl implements YFSCollectionOthersUE {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyOtherCollectionUEImpl.class);


	/*
	 * Start : OMNI-647 :: Discontinue Cardinal Commenting below lines of code as
	 * Below lines of code have been added to provide dummy response and raise alert
	 * for PayPal
	 */

	public YFSExtnPaymentCollectionOutputStruct collectionOthers(YFSEnvironment env,
			YFSExtnPaymentCollectionInputStruct paymentInputStruct) throws YFSUserExitException {

		log.beginTimer("Start-AcademyOtherCollectionUEImpl ::");
		String sChargeType = paymentInputStruct.chargeType;
		String sPaymentType = paymentInputStruct.paymentType;
		double dRequestAmt = paymentInputStruct.requestAmount;
		log.debug("PaymentType :: ChargeType :: RequestAmt " + sPaymentType + "::" + sChargeType + "::" + dRequestAmt);

		YFSExtnPaymentCollectionOutputStruct paymentOutputStruct = new YFSExtnPaymentCollectionOutputStruct();
		try {
			if (AcademyConstants.PAYPAL.equals(sPaymentType)) {
				completeSettlementAndRaiseAlert(env, paymentInputStruct, paymentOutputStruct);

			}
			//Start OMNI-57405 - Payment Migration(PayPal Decoupling)
			else if (AcademyConstants.PAYPAL_DECOUPLED.equals(sPaymentType)) { 
				log.verbose("Payment Type - " + sPaymentType);
				Document docPayment = YCDPaymentUEXMLManager.convertPaymentInputStructToXML(paymentInputStruct).getDocument();
				Document docPaymentSrvOut = AcademyUtil.invokeService(env, AcademyConstants.SERV_DIRECT_PAYPAL, docPayment);
				paymentOutputStruct=new AcademyDirectPaypalOtherCollectionUEImplUtil().processDirectPaypalCollectionResponse(env,paymentInputStruct,docPaymentSrvOut);
				return paymentOutputStruct;

			}
			//End OMNI-57405 - Payment Migration(PayPal Decoupling)
		} catch (YFCException exp) {
			throw exp;
		} catch (Exception exp) {
			YFSUserExitException ex = new YFSUserExitException(exp.getMessage());
			throw ex;
		} finally {
			log.debug("paymentOutputStruct:" + paymentOutputStruct);
			log.endTimer("AcademyOtherCollectionUEImpl");
			log.debug("Exiting AcademyOtherCollectionUEImpl");
		}
		return paymentOutputStruct;
	}

	private void completeSettlementAndRaiseAlert(YFSEnvironment env,
			YFSExtnPaymentCollectionInputStruct paymentInputStruct,
			YFSExtnPaymentCollectionOutputStruct paymentOutputStruct) throws Exception {
		log.debug("Begin of AcademyOtherCollectionUEImpl.completeSettlementAndRaiseAlert() method");
		log.verbose("Inside completeSettlement::Perform Settlement");

		paymentOutputStruct.authorizationId = paymentInputStruct.authorizationId;
		paymentOutputStruct.requestID = AcademyConstants.STR_DUMMY_SETTLEMENT;
		paymentOutputStruct.tranAmount = paymentInputStruct.requestAmount;
		paymentOutputStruct.authorizationAmount = paymentInputStruct.requestAmount;
		paymentOutputStruct.tranType = paymentInputStruct.chargeType;
		paymentOutputStruct.tranReturnCode = AcademyConstants.ATTR_HUNDRED;
		paymentOutputStruct.PaymentReference4 = AcademyConstants.STR_CARDINAL;
		paymentOutputStruct.authCode = AcademyConstants.STR_DUMMY_SETTLEMENT;

		if (!YFCObject.isVoid(paymentInputStruct.currentAuthorizationCreditCardTransactions)) {
			Document doc = paymentInputStruct.currentAuthorizationCreditCardTransactions;
			log.verbose("currentAuthorizationCreditCardTransactions element is::" + XMLUtil.getXMLString(doc));
			paymentOutputStruct.authCode = XPathUtil.getString(doc, AcademyConstants.XPATH_AUTH_CODE);
		}

		// Raise Alert
		Document docPayment = XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT);
		Element elePayment = docPayment.getDocumentElement();
		elePayment.setAttribute(AcademyConstants.ATTR_ORDER_NO, paymentInputStruct.orderNo);
		elePayment.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, paymentInputStruct.orderHeaderKey);
		elePayment.setAttribute(AcademyConstants.ATTR_PAYMENT_TYPE, paymentInputStruct.paymentType);
		elePayment.setAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT,
				Double.toString(paymentInputStruct.requestAmount));
		elePayment.setAttribute(AcademyConstants.ATTR_AUTH_AMOUNT, Double.toString(paymentInputStruct.requestAmount));
		elePayment.setAttribute(AcademyConstants.ATTR_TRAN_AMT, Double.toString(paymentInputStruct.requestAmount));
		elePayment.setAttribute(AcademyConstants.ATTR_PAYMENT_TYPE, paymentInputStruct.paymentType);
		elePayment.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_APPROVED);
		elePayment.setAttribute(AcademyConstants.ATTR_STATUS_CODE, AcademyConstants.STR_CARDINAL_STATUS_Y);
		elePayment.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE, AcademyConstants.ATTR_HUNDRED);
		elePayment.setAttribute(AcademyConstants.ATTR_TRAN_TYPE, paymentInputStruct.chargeType);
		elePayment.setAttribute(AcademyConstants.ATTR_TRANSID, AcademyConstants.STR_DUMMY_SETTLEMENT);
		elePayment.setAttribute(AcademyConstants.ATTR_RAISE_PAYMENT_ALERT, AcademyConstants.STR_YES);

		elePayment.setAttribute(AcademyConstants.ATTR_REASON_CODE, AcademyConstants.STR_CARDINAL);
		elePayment.setAttribute(AcademyConstants.ATTR_REASON_DESC,
				paymentInputStruct.paymentType + " :: " + paymentInputStruct.chargeType);

		AcademyUtil.invokeService(env, AcademyConstants.SERV_SETTLEMENT_FAILURE_ALERT, docPayment);
		log.debug("End of AcademyOtherCollectionUEImpl.completeSettlementAndRaiseAlert() method");

	}
}
