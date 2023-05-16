//package declaration
package com.academy.ecommerce.sterling.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.pca.ycd.ue.utils.YCDPaymentUEXMLManager;
import com.yantra.shared.dbclasses.YFS_Charge_TransactionDBHome;
import com.yantra.shared.dbi.YFS_Charge_Transaction;
import com.yantra.shared.dbi.YFS_Payment_Type;
import com.yantra.shared.ycp.YFSContext;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSExtnPaymentCollectionInputStruct;
import com.yantra.yfs.japi.YFSExtnPaymentCollectionOutputStruct;
import com.yantra.yfs.japi.YFSUserExitException;

/**
 * 
 * class to perform the payment operations for paypal processing output.
 *
 */

public class AcademyDirectPaypalOtherCollectionUEImplUtil {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyDirectPaypalOtherCollectionUEImplUtil.class);


	public YFSExtnPaymentCollectionOutputStruct processDirectPaypalCollectionResponse(YFSEnvironment env,
			YFSExtnPaymentCollectionInputStruct paymentInputStruct, Document docPaymentSrvOut) throws YFSUserExitException {
		log.beginTimer("AcademyDirectPaypalOtherCollectionUEImplUtil.processDirectPaypalCollectionResponse()");
		log.debug("Begin of AcademyDirectPaypalOtherCollectionUEImplUtil.processDirectPaypalCollectionResponse() method after direct paypal.");
		YFSContext oCtx = (YFSContext) env;
		YFSExtnPaymentCollectionOutputStruct paymentOutputStruct = new YFSExtnPaymentCollectionOutputStruct();
		try {

			YFCDocument paymentInputDoc = YCDPaymentUEXMLManager.convertPaymentInputStructToXML(paymentInputStruct);
			YFCDocument collectionOutputDoc = YFCDocument.getDocumentFor(docPaymentSrvOut);
			YFCElement root = collectionOutputDoc.getDocumentElement();
			String sOrderHeaderKey = paymentInputDoc.getDocumentElement().getAttribute("OrderHeaderKey");
			String sResponseCode = root.getAttribute(AcademyConstants.ATTR_RESPONSE_CODE);
			if (!YFCObject.equals(sResponseCode, AcademyConstants.STR_APPROVED) && !YFCObject.equals(sResponseCode, AcademyConstants.STR_HARD_DECLINED)
					&& !YFCObject.equals(sResponseCode, "SOFT_DECLINED") && !YFCObject.equals(sResponseCode, "BANK_HOLD")
					&& !YFCObject.equals(sResponseCode, AcademyConstants.STR_SERVICE_UNAVAILABLE)) {
				YFCException ex = new YFCException("YCD00001");
				ex.setAttribute("ResponseCode", sResponseCode);
				throw ex;
			}
			YCDPaymentUEXMLManager.populatePaymentOutputStructFromXML(paymentOutputStruct, YFCDocument.getDocumentFor(docPaymentSrvOut));
			if (YFCObject.equals(sResponseCode, AcademyConstants.STR_HARD_DECLINED)) {
				paymentOutputStruct.authorizationAmount = 0.0D;
				if (!YFCObject.isVoid(collectionOutputDoc.getDocumentElement().getAttribute("RetryFlag"))) {
					paymentOutputStruct.retryFlag = collectionOutputDoc.getDocumentElement().getAttribute("RetryFlag");
				} else {
					paymentOutputStruct.retryFlag = "N";
				}

				if (!YFCObject.isVoid(collectionOutputDoc.getDocumentElement().getAttribute("SuspendPayment"))) {
					paymentOutputStruct.suspendPayment = collectionOutputDoc.getDocumentElement().getAttribute("SuspendPayment");
				} else {
					paymentOutputStruct.suspendPayment = "Y";
				}
				Document docOrderList = AcademyPaypalPaymentProcessingUtil.getOrderList(oCtx, AcademyConstants.STR_DPP_COLLECTION_FAIL_SRV,
						sOrderHeaderKey);
				YFCDocument eventDoc = prepareEventDoc(oCtx,  paymentInputStruct,
						 paymentOutputStruct,  sResponseCode, docOrderList);
				String strErrorMessage = paymentOutputStruct.tranReturnMessage;
				if (AcademyConstants.STR_MAX_RETRY_LIMIT_REACHED.equals(strErrorMessage)) {
					YFCElement eventRoot = eventDoc.getDocumentElement();
					YFCElement failureElem = eventRoot.getChildElement("CollectionFailureDetails");
					failureElem.setAttribute("FailureReasonCode", "YCD_AUTH_RETRY_LIMIT");
				}
				this.raiseEvent( env, eventDoc);
			} else {
				YFCDocument eventDoc;
				if (!YFCObject.equals(sResponseCode, "SOFT_DECLINED") && !YFCObject.equals(sResponseCode, "BANK_HOLD")) {
					if (YFCObject.equals(sResponseCode, AcademyConstants.STR_SERVICE_UNAVAILABLE)) {
						paymentOutputStruct.authorizationAmount = 0.0D;
						if (!YFCObject.isVoid(collectionOutputDoc.getDocumentElement().getAttribute("RetryFlag"))) {
							paymentOutputStruct.retryFlag = collectionOutputDoc.getDocumentElement().getAttribute("RetryFlag");
						} else {
							paymentOutputStruct.retryFlag = "Y";
						}

						if (!YFCObject.isVoid(collectionOutputDoc.getDocumentElement().getAttribute("SuspendPayment"))) {
							paymentOutputStruct.suspendPayment = collectionOutputDoc.getDocumentElement().getAttribute("SuspendPayment");
						} else {
							paymentOutputStruct.suspendPayment = "N";
						}
						String sInternalRetMsg = paymentOutputStruct.internalReturnMessage;
						if(!YFCObject.equals(sInternalRetMsg, AcademyConstants.STR_UNHANDLED_ERROR) &&
							!YFCObject.equals(sInternalRetMsg, AcademyConstants.STR_ACCESS_TOKEN_FAILED))
						{	
							Document docOrderList = AcademyPaypalPaymentProcessingUtil.getOrderList(oCtx, AcademyConstants.STR_DPP_COLLECTION_FAIL_SRV,
									sOrderHeaderKey);
							eventDoc = prepareEventDoc(oCtx, paymentInputStruct,
									paymentOutputStruct, sResponseCode, docOrderList);
									this.raiseEvent( env, eventDoc);
						}		
					} else if (YFCObject.equals(sResponseCode, AcademyConstants.STR_APPROVED)) {
						paymentOutputStruct.authorizationAmount = collectionOutputDoc.getDocumentElement().getDoubleAttribute("AuthorizationAmount");
						paymentOutputStruct.authorizationExpirationDate = collectionOutputDoc.getDocumentElement().getAttribute("AuthorizationExpirationDate");
						paymentOutputStruct.authTime = collectionOutputDoc.getDocumentElement().getAttribute("AuthTime");
						if (!YFCObject.isVoid(collectionOutputDoc.getDocumentElement().getAttribute("RetryFlag"))) {
							paymentOutputStruct.retryFlag = collectionOutputDoc.getDocumentElement().getAttribute("RetryFlag");
						} else {
							paymentOutputStruct.retryFlag = "N";
						}

						if (!YFCObject.isVoid(collectionOutputDoc.getDocumentElement().getAttribute("SuspendPayment"))) {
							paymentOutputStruct.suspendPayment = collectionOutputDoc.getDocumentElement().getAttribute("SuspendPayment");
						} else {
							paymentOutputStruct.suspendPayment = "N";
						}

						paymentOutputStruct.tranAmount = Double.parseDouble(collectionOutputDoc.getDocumentElement().getAttribute("TranAmount"));
						paymentOutputStruct.sCVVAuthCode = collectionOutputDoc.getDocumentElement().getAttribute("SCVVAuthCode");
					}
				} else {
					paymentOutputStruct.authorizationAmount = 0.0D;
					if (!YFCObject.isVoid(collectionOutputDoc.getDocumentElement().getAttribute("RetryFlag"))) {
						paymentOutputStruct.retryFlag = collectionOutputDoc.getDocumentElement().getAttribute("RetryFlag");
					} else {
						paymentOutputStruct.retryFlag = "N";
					}

					if (!YFCObject.isVoid(collectionOutputDoc.getDocumentElement().getAttribute("SuspendPayment"))) {
						paymentOutputStruct.suspendPayment = collectionOutputDoc.getDocumentElement().getAttribute("SuspendPayment");
					} else {
						paymentOutputStruct.suspendPayment = "Y";
					}
					Document docOrderList = AcademyPaypalPaymentProcessingUtil.getOrderList(oCtx, AcademyConstants.STR_DPP_COLLECTION_FAIL_SRV,
							sOrderHeaderKey);
					eventDoc = prepareEventDoc(oCtx, paymentInputStruct,paymentOutputStruct,  sResponseCode, docOrderList);
					this.raiseEvent( env, eventDoc);
					
				}
			}
		} catch (YFCException exp) {
			log.info("Paypal Direct:: Exception :: " +exp.toString());
			log.verbose(exp);
			throw exp;
		} catch (Exception exp) {
			log.info("Paypal Direct:: Exception :: "  +exp.toString());
			log.error("Exception in the collectionOthers method of AcademyDirectPaypalOtherCollectionUEImplUtil ", exp);
			YFSUserExitException ex = new YFSUserExitException(exp.getMessage());
			throw ex;
		} finally {
			log.endTimer("AcademyDirectPaypalOtherCollectionUEImplUtil.processDirectPaypalCollectionResponse()");
			log.debug("Exiting AcademyDirectPaypalOtherCollectionUEImplUtil.processDirectPaypalCollectionResponse() after direct paypal.");
		}
		return paymentOutputStruct;
	}

	/**This method prepares raiseEvent XML doc
	 * 
	 * @param oCtx
	 * @param paymentInputStruct
	 * @param paymentOutputStruct
	 * @param sResponseCode
	 * @param docOrderList
	 * @return
	 * @throws Exception
	 */
	private YFCDocument prepareEventDoc(YFSContext oCtx, YFSExtnPaymentCollectionInputStruct paymentInputStruct,
			YFSExtnPaymentCollectionOutputStruct paymentOutputStruct, String sResponseCode,Document docOrderList)throws Exception {	
		log.debug("Begin of AcademyDirectPaypalOtherCollectionUEImplUtil.prepareEventDoc() after direct paypal.");
		Element ordrDetail =  XMLUtil.getElementByXPath(docOrderList, AcademyConstants.STR_DPP_ORD_XPATH);
		Document docOrderDetails= XMLUtil.getDocumentForElement(ordrDetail);
		YFCDocument eventDoc = YFCDocument.getDocumentFor(docOrderDetails);	
		YFCElement root = eventDoc.getDocumentElement();
		if(root != null) {
			YFCElement failureDetails = root.createChild("CollectionFailureDetails");
			failureDetails.setAttribute("FailureReasonCode", sResponseCode);
			failureDetails.setAttribute("AuthReturnCode", paymentOutputStruct.authReturnCode);
			failureDetails.setAttribute("AuthReturnFlag", paymentOutputStruct.authReturnFlag);
			failureDetails.setAttribute("AuthReturnMessage", paymentOutputStruct.authReturnMessage);
			failureDetails.setAttribute("ChargeType", paymentOutputStruct.tranType);
			failureDetails.setAttribute("CreditCardExpirationDate", paymentInputStruct.creditCardExpirationDate);
			failureDetails.setAttribute("CreditCardName", paymentInputStruct.creditCardName);
			failureDetails.setAttribute("CreditCardNo", paymentInputStruct.creditCardNo);
			failureDetails.setAttribute("CreditCardType", paymentInputStruct.creditCardType);
			failureDetails.setAttribute("DisplaySvcNo", paymentOutputStruct.DisplaySvcNo);
			failureDetails.setAttribute("HoldOrderAndRaiseEvent", paymentOutputStruct.holdOrderAndRaiseEvent);
			failureDetails.setAttribute("HoldReason", paymentOutputStruct.holdReason);
			failureDetails.setAttribute("InternalReturnCode", paymentOutputStruct.internalReturnCode);
			failureDetails.setAttribute("InternalReturnFlag", paymentOutputStruct.internalReturnFlag);
			failureDetails.setAttribute("InternalReturnMessage", paymentOutputStruct.internalReturnMessage);
			failureDetails.setAttribute("PaymentReference1", paymentOutputStruct.PaymentReference1);
			failureDetails.setAttribute("PaymentReference2", paymentOutputStruct.PaymentReference2);
			failureDetails.setAttribute("PaymentReference3", paymentOutputStruct.PaymentReference3);
			failureDetails.setAttribute("PaymentReference4", paymentOutputStruct.PaymentReference4);
			failureDetails.setAttribute("PaymentReference5", paymentOutputStruct.PaymentReference5);
			failureDetails.setAttribute("PaymentReference6", paymentOutputStruct.PaymentReference6);
			failureDetails.setAttribute("PaymentReference7", paymentOutputStruct.PaymentReference7);
			failureDetails.setAttribute("PaymentReference8", paymentOutputStruct.PaymentReference8);
			failureDetails.setAttribute("PaymentReference9", paymentOutputStruct.PaymentReference9);
			failureDetails.setAttribute("PaymentType", paymentInputStruct.paymentType);
			failureDetails.setAttribute("RequestAmount", paymentInputStruct.requestAmount);
			failureDetails.setAttribute("SvcNo", paymentInputStruct.svcNo);
			failureDetails.setAttribute("TranAmount", paymentOutputStruct.tranAmount);
			failureDetails.setAttribute("TranRequestTime", paymentOutputStruct.tranRequestTime);
			failureDetails.setAttribute("TranReturnCode", paymentOutputStruct.tranReturnCode);
			failureDetails.setAttribute("TranReturnFlag", paymentOutputStruct.tranReturnFlag);
			failureDetails.setAttribute("TranReturnMessage", paymentOutputStruct.tranReturnMessage);
			failureDetails.setAttribute("TranType", paymentOutputStruct.tranType);
			failureDetails.setAttribute("AuthTime", paymentOutputStruct.authTime);
			failureDetails.setAttribute("AuthAVS", paymentOutputStruct.authAVS);
			failureDetails.setAttribute("CVVAuthCode", paymentOutputStruct.sCVVAuthCode);
			
			YFS_Charge_Transaction oChgTran = YFS_Charge_TransactionDBHome.getInstance().selectWithPK(oCtx, paymentInputStruct.chargeTransactionKey);
			if (oChgTran != null) {
				YFS_Payment_Type oType = oChgTran.getPayment().getPaymentType();
				if (oType != null) {
					failureDetails.setAttribute("PaymentTypeGroup", oType.getPayment_Type_Group());
				}

				failureDetails.setAttribute("DisplayCreditCardNo", "************" + oChgTran.getPayment().getDisplay_Credit_Card_No());
			}
		}
		log.debug("event doc created : " + XMLUtil.getXMLString(eventDoc.getDocument()));
		log.debug("End of AcademyDirectPaypalOtherCollectionUEImplUtil.prepareEventDoc() after direct paypal.");
		return eventDoc;
	}

	/**This method will call raiseEvent api of COLLECTION_FAILED even of PAYMENT_EXECUTION transaction
	 * 
	 * @param env
	 * @param eventDoc
	 * @throws Exception
	 */
	private void raiseEvent(YFSEnvironment env, YFCDocument eventDoc) throws Exception{
		log.debug("Begin of AcademyDirectPaypalOtherCollectionUEImplUtil.raiseEvent() method after direct paypal.");
		String raiseEventPart1 =AcademyConstants.STR_DPP_RAISE_EVENT1 ;
		String raiseEventPart2 = AcademyConstants.STR_DPP_RAISE_EVENT2 ;
		Document docRaiseEventInput= eventDoc.getDocument();
    	log.verbose("eleRaiseEvent"+XMLUtil.getXMLString(docRaiseEventInput));
    	Document raiseEventInputDoc = XMLUtil.getDocument(raiseEventPart1 + XMLUtil.getXMLString(docRaiseEventInput) + raiseEventPart2);    	
    	log.verbose("Calling raiseEvent API  with input : =" +XMLUtil.getXMLString(raiseEventInputDoc));
	   	AcademyUtil.invokeAPI(env,AcademyConstants.API_ACADEMY_RAISE_EVENT,raiseEventInputDoc);
		log.debug("End of AcademyDirectPaypalOtherCollectionUEImplUtil.raiseEvent() method after direct paypal.");

	}
	

}
