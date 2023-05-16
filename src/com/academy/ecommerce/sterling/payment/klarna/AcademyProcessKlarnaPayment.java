package com.academy.ecommerce.sterling.payment.klarna;

/**#########################################################################################
 *
 * Project Name                : Klarna
 * Module                      : OMNI-92705
 * Author                      : C0023461
 * Author Group				  : CTS - POD
 * Date                        : 08-DEC-2022
 * Description				  : This class is used for Processing Klarna Payments
 * ---------------------------------------------------------------------------------
 * Date            	Author         		Version#       		Remarks/Description
 * ---------------------------------------------------------------------------------
 * 12-DEC-2022		CTS  	 			 1.0           		Initial version
 *
 * #########################################################################################*/

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import org.w3c.dom.Document;
import com.academy.ecommerce.sterling.util.AcademyKlarnaInterfaceUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfc.core.YFCObject;
public class AcademyProcessKlarnaPayment implements YIFCustomApi {

	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyProcessKlarnaPayment.class.getName());

	private Properties props;
	/**
	 * <p><strong>0</strong> void Original Authorization</p>
	 * <p><strong>1</strong> extend-authorization-time</p>
	 * <p><strong>2</strong> captures</p>
	 * <p><strong>3</strong> cancel</p>
	 * <p><strong>4</strong> release-remaining-authorization</p>
	 * <p><strong>5</strong> refunds</p>
	 */
	private Integer iTransactionType = null;
	/**
	 * <p>default value is <strong>false</strong> </p>
	 * <p>when value is <strong>true</strong> -> create payment hard decline agent</p>
	 */
	private Document docGetOrderListOut;


	/**
	 * @param env
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	public Document processKlarnaPayment(YFSEnvironment env, Document inDocUE) throws Exception {
		log.setLogAll(true);
		log.beginTimer("START :: AcademyProcessKlarnaPayment.processKlarnaPayment :: START");
		log.verbose("AcademyProcessKlarnaPayment.processKlarnaPayment.inDoc" + XMLUtil.getXMLString(inDocUE));
		Document outDocUE = null;
		try {
			getOrderList(env, inDocUE);
			String strChargeType = XPathUtil.getString(inDocUE,AcademyConstants.XPATH_PAYMENT_CHARGETYPE);
			log.verbose("ChargeType :: " + strChargeType);
			if (YFCCommon.equalsIgnoreCase(AcademyConstants.STR_CHRG_TYPE_CHARGE, strChargeType)) {
				outDocUE = processTransactionCharge(env,inDocUE);
				releaseRemainingAuth(env,inDocUE,outDocUE);
			} else if (YFCCommon.equalsIgnoreCase(AcademyConstants.STR_CHRG_TYPE_AUTH, strChargeType)) {
				outDocUE = processTransactionAuthorization(env,inDocUE);
			}
		} catch (Exception exp) {
			outDocUE = handleExceptions(exp);
		}
		log.verbose("AcademyProcessKlarnaPayment.processKlarnaPayment.outDoc" + XMLUtil.getXMLString(outDocUE));
		log.endTimer("END :: AcademyProcessKlarnaPayment.processKlarnaPayment :: END ");
		return outDocUE;
	}

	private void raiseSettlementFailure(YFSEnvironment env,Document inDocUE, Document outDocUE,String strHttpStatusCode) throws Exception {
		log.beginTimer("START :: raiseSettlementFailure :: START");
		if (iTransactionType.equals(AcademyConstants.INT_KLARNA_CAPTUPE_TRANSACTION) || iTransactionType.equals(AcademyConstants.INT_KLARNA_REFUND_TRANSACTION)) {
			log.verbose("bSettlementFailure :: " + true);
			Document docExceptionInput = XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT);
			docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_PAYMENT_TYPE, inDocUE.getDocumentElement().getAttribute(AcademyConstants.ATTR_PAYMENT_TYPE));
			docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_NO, inDocUE.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO));
			docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, inDocUE.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));
			docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_TRAN_AMT, getDoubleFormateString(inDocUE.getDocumentElement().getAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT)));
			docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT, getDoubleFormateString(inDocUE.getDocumentElement().getAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT)));
			docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_ERROR_NO, strHttpStatusCode);
			docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_TRANS_ID, outDocUE.getDocumentElement().getAttribute(AcademyConstants.ATTR_TRAN_RETURN_MESSAGE));
			String strResonDesc =  XPathUtil.getString(outDocUE,AcademyConstants.XPATH_PAYMENT_TRANS_ERROR_NESSAGE);
			docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_ERROR_DESC_SHORT, strResonDesc);
			docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_REASON_DESC, "Klarna Operation :: "+ outDocUE.getDocumentElement().getAttribute(AcademyConstants.ATTR_KLARNA_OPERATION));
			log.verbose("raiseSettlementFailure.inDoc :: " + XMLUtil.getXMLString(docExceptionInput));
			AcademyUtil.invokeService(env, AcademyConstants.SERV_SETTLEMENT_FAILURE_ALERT, docExceptionInput);		
		}
		log.endTimer("END :: raiseSettlementFailure :: END ");
	}

	/**
	 * @param env 
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	private Document processTransactionCharge(YFSEnvironment env, Document inDocUE) throws Exception {
		log.beginTimer("START :: processTransactionCharge :: START");
		log.verbose("START :: processTransactionCharge :: START");
		Document docKlarnaResponse = null;
		Document outDocUE = null ;
		String strKlarnOrderID = XPathUtil.getString(inDocUE,AcademyConstants.XPATH_PAYMENT_CREDITCARDNO);
		String strRequestAmount = XPathUtil.getString(inDocUE,AcademyConstants.XPATH_PAYMENT_REQUESTAMOUNT);
		String strIdempotencyKey = XPathUtil.getString(inDocUE, AcademyConstants.XPATH_PAYMENT_CHARGETRANSACTIONKEY);
		iTransactionType = getChargeTransactionType(strRequestAmount);
		docKlarnaResponse = AcademyKlarnaInterfaceUtil.invokeKlarnaService(env,strKlarnOrderID, iTransactionType, strRequestAmount,strIdempotencyKey);
		outDocUE = prepareOutDocument(env,inDocUE,docKlarnaResponse, false);
		log.verbose("END :: processTransactionCharge :: END ");
		log.endTimer("END :: processTransactionCharge :: END ");
		return outDocUE;
	}


	/**
	 *
	 * @param env 
	 * @param env
	 * @param inDocUE
	 * @return
	 * @throws Exception
	 */
	private Document processTransactionAuthorization(YFSEnvironment env, Document inDocUE) throws Exception {
		log.beginTimer("START :: processTransactionAuthorization :: START");
		log.verbose("START :: processTransactionAuthorization :: START");
		Document outDocUE = null;
		String strCurrentAuthAmount = XPathUtil.getString(inDocUE,AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CURRENT_AUTHORIZATION_AMT);
		String strRequestAmount = XPathUtil.getString(inDocUE,AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);
		String strIdempotencyKey = XPathUtil.getString(inDocUE, AcademyConstants.XPATH_PAYMENT_CHARGETRANSACTIONKEY);
		String strKlarnOrderID = XPathUtil.getString(inDocUE, AcademyConstants.XPATH_PAYMENT_CREDITCARDNO);

		Document docKlarnaResponse = null;
		boolean isCloseRecordDummy = false;

		if(isNegative(strRequestAmount) ){
			log.verbose("RequestAmount is Negative");
			if(isNegative(strCurrentAuthAmount) && strCurrentAuthAmount.equalsIgnoreCase(strRequestAmount)) {
				log.verbose("CurrentAuthorizationAmount is Negative");
				log.verbose("isCloseRecordDummy :: true");
				iTransactionType=0;
				isCloseRecordDummy = true;
			}else{

				String strTotalOpenBookings = XPathUtil.getString(docGetOrderListOut, AcademyConstants.XPATH_TOTAL_OPEN_BOOKINGS);
				String strTotalCredits = XPathUtil.getString(docGetOrderListOut, AcademyConstants.XPATH_CTD_TOTALCREDITS);
				String strTotalDebits = XPathUtil.getString(docGetOrderListOut,AcademyConstants.XPATH_CTD_TOTALDEBITS);
				String strMinOrderStatus = XPathUtil.getString(docGetOrderListOut, AcademyConstants.XPATH_MIN_ORDER_STATUS);
				String[] components = strMinOrderStatus.split("\\.");
				strMinOrderStatus = (components.length > 2) ? components[0] + "." + components[1] : strMinOrderStatus;
				String strChargedAmount = XPathUtil.getString(inDocUE, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_PAYMENT_REF_8);

				if (AcademyConstants.VAL_CANCELLED_STATUS.equals(strMinOrderStatus)) {
					log.verbose("Full Order Cancelation :: true");
					iTransactionType = AcademyConstants.INT_KLARNA_FULL_CANCEL_TRANSACTION;
				} else {
					// totalBookingconditon may be excluded with introduction on credit==debit, needs to be confimed
					boolean bLastActiveLineCanceltion = strTotalCredits.equalsIgnoreCase(strTotalDebits) && !isZero(strTotalCredits);
					log.verbose("bLastActiveLineCanceltion :: " + bLastActiveLineCanceltion);
					iTransactionType = AcademyConstants.INT_KLARNA_REMAINING_CANCEL_TRANSACTION;
					if (isZero(strTotalOpenBookings) && !isZero(strChargedAmount) && bLastActiveLineCanceltion) {
						log.verbose("Order Has Active Lines :: false");
					} else {
						log.verbose("Order Has Active Lines :: true");
						log.verbose("isCloseRecordDummy :: true");
						isCloseRecordDummy = true;
					}
				}
			}
		}
		else if((!isNegative(strRequestAmount)) && isZero(strCurrentAuthAmount)){
			log.verbose("RequestAmount is not Negative");
			log.verbose("CurrentAuthorizationAmount is Zero");
			iTransactionType = 1;
		}
		if(!isCloseRecordDummy) {
			docKlarnaResponse = AcademyKlarnaInterfaceUtil.invokeKlarnaService(env,strKlarnOrderID, iTransactionType, strRequestAmount, strIdempotencyKey);
		}
		outDocUE = prepareOutDocument(env,inDocUE, docKlarnaResponse, isCloseRecordDummy);
		
		log.verbose("END :: processTransactionAuthorization :: END ");
		log.endTimer("END :: processTransactionAuthorization :: END ");
		return outDocUE;
	}

	/**
	 *
	 * @param env
	 * @param inDocUE
	 * @param outDocUE
	 * @throws Exception
	 */
	private void releaseRemainingAuth(YFSEnvironment env, Document inDocUE, Document outDocUE) throws Exception {
		log.beginTimer("START :: releaseRemainingAuth :: START");
		log.verbose("START :: releaseRemainingAuth :: START");
		String strKlarnOrderID = XPathUtil.getString(inDocUE, AcademyConstants.XPATH_PAYMENT_CREDITCARDNO);
		String strIdempotencyKey = XPathUtil.getString(inDocUE, AcademyConstants.XPATH_PAYMENT_CHARGETRANSACTIONKEY);
		String strPaymentReference7 = XPathUtil.getString(inDocUE,AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_PAYMENT_REF_7);
		String strResponseCode = XPathUtil.getString(outDocUE,AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_RESPONSE_CODE);
		String strSOrderNo = XPathUtil.getString(inDocUE,AcademyConstants.STR_PAYMENT_PATH_AT +AcademyConstants.ATTR_ORDER_NO);
		if (isNegative(strPaymentReference7) && iTransactionType == 2 && strResponseCode.equalsIgnoreCase(AcademyConstants.STR_APPROVED)) {
			String strTotalOpenAuthorizations = XPathUtil.getString(docGetOrderListOut,AcademyConstants.XPATH_TOTAL_OPENAUTHORIZATIONS);
			if (Double.valueOf(strTotalOpenAuthorizations) == 0.00) {
			log.verbose("PaymentReference7 is not Zero && TotalOpenAuthorizations is Zero && Capture Call ResponseCode is APPROVED :: Eligible for Release Remaining Authorization Amoutn");	
				Document docReleaseAuth = XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT);
				Element eleReleaseAuth = docReleaseAuth.getDocumentElement();
				eleReleaseAuth.setAttribute(AcademyConstants.ATTR_ORDER_NO, strSOrderNo);
				eleReleaseAuth.setAttribute(AcademyConstants.ATTR_KL_ORDERID, strKlarnOrderID);
				eleReleaseAuth.setAttribute(AcademyConstants.ATTR_KL_TRANSACTIONTYPE, "4");
				eleReleaseAuth.setAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT, strPaymentReference7);
				eleReleaseAuth.setAttribute(AcademyConstants.ATTR_IDEMPOTENCY_KEY, strIdempotencyKey);
				log.verbose("releaseRemainingAuth.AcademyPostKlarnaReleaseRemainingAuth.inDoc" + XMLUtil.getXMLString(docReleaseAuth));
				AcademyUtil.invokeService(env, AcademyConstants.SERV_ACADEMY_KLARNA_POST_RELEASE_REMAINING_AUTH, docReleaseAuth);
			}
		}
	 log.verbose("END :: releaseRemainingAuth :: END");
	 log.endTimer("END :: releaseRemainingAuth :: END");	
	}

	private Document prepareOutDocument(YFSEnvironment env, Document inDocUE,Document docKlarnaResponse, boolean isCloseRecordDummy) throws Exception{
		log.beginTimer("START ::  prepareOutDocument :: START");
		log.verbose("START ::  prepareOutDocument :: START");
		boolean isConnectionSuccessfull = false;
		Document outDocUE = null;
		if (docKlarnaResponse != null && !isCloseRecordDummy) {
			isConnectionSuccessfull = AcademyKlarnaInterfaceUtil.isConnectionSuccessfull(docKlarnaResponse);
			log.verbose("Klarna Connection Successfull :: " + isConnectionSuccessfull);
			if (isConnectionSuccessfull) {
				outDocUE = prepareOutDocUE(env,inDocUE, docKlarnaResponse);
			} else {
				outDocUE = prepareServiceUnavailableResp(docKlarnaResponse);
			}
		} else {
			log.verbose("Prepare Dummy UE OutDocument :: TRUE");
			outDocUE =  prepareDummyOutDocUE(env,inDocUE);
		}
		
		log.verbose("END :: prepareOutDocument :: END");
		log.endTimer("END :: prepareOutDocument :: END");
		return outDocUE ;
	}

	private Document prepareDummyOutDocUE(YFSEnvironment env,Document inDocUE) throws Exception {
		log.beginTimer("START ::  prepareDummyOutDocUE :: START");
		log.verbose("START ::  prepareDummyOutDocUE :: START");
		Document outDocUE = populatePaymentDefaultOutputXML(env,inDocUE);
		Element outEleUE = outDocUE.getDocumentElement();
		String strRequestAmount = XPathUtil.getString(inDocUE, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);
		generateDummyResponse(outDocUE);
		updatePaymentReference(inDocUE, outEleUE, strRequestAmount);
		log.verbose("END :: prepareDummyOutDocUE :: END" + XMLUtil.getXMLString(outDocUE));
		log.endTimer("END :: prepareDummyOutDocUE :: END");
		return outDocUE;
	}

	/**
	 *
	 * @param env
	 * @param inDocUE
	 * @param outDocUE
	 * @param docKlarnaResponse
	 * @param bIsDummySettle
	 * @return
	 * @throws Exception
	 */
	private Document prepareOutDocUE(YFSEnvironment env, Document inDocUE,Document docKlarnaResponse) throws Exception {
		log.beginTimer("START ::  prepareOutDocUE :: START");
		log.verbose("START ::  prepareOutDocUE :: START");
		boolean isEligibleForRetry = false;
		Document outDocUE = populatePaymentDefaultOutputXML(env,inDocUE);
		Element outEleUE = outDocUE.getDocumentElement();
		String strRequestAmount = XPathUtil.getString(inDocUE, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);
		String strIdempotencyKey = XPathUtil.getString(inDocUE,AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY);
		Element eleKlarnaResponse = docKlarnaResponse.getDocumentElement();
		String strHttpStatusCode = eleKlarnaResponse.getAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE);
		outEleUE.setAttribute(AcademyConstants.ATTR_KLARNA_OPERATION,eleKlarnaResponse.getAttribute(AcademyConstants.ATTR_KLARNA_OPERATION));
		String strSOrderNo = XPathUtil.getString(inDocUE,AcademyConstants.STR_PAYMENT_PATH_AT +AcademyConstants.ATTR_ORDER_NO);
			if (strHttpStatusCode.startsWith("2")) {
				log.verbose("2XX Klarna Response");
				if (iTransactionType == AcademyConstants.INT_KLARNA_EXTEND_AUTH_TRANSACTION) {
					String strNumDaysToExtend = props.getProperty(AcademyConstants.KL_REAUTH_EXPIRY_DAYS);
					Integer iExpirationDays = Integer.parseInt(strNumDaysToExtend);
					outEleUE.setAttribute(AcademyConstants.ATTR_AUTH_EXP_DATE, getDate(Calendar.DATE,iExpirationDays,AcademyConstants.STR_DATE_TIME_PATTERN_NEW));
				}else if (iTransactionType == AcademyConstants.INT_KLARNA_CAPTUPE_TRANSACTION) {
					outEleUE.setAttribute(AcademyConstants.ATTR_REQUEST_ID,eleKlarnaResponse.getAttribute(AcademyConstants.ATTR_CAPTURE_ID));
				}else if (iTransactionType == AcademyConstants.INT_KLARNA_REFUND_TRANSACTION) {
					outEleUE.setAttribute(AcademyConstants.ATTR_REQUEST_ID,eleKlarnaResponse.getAttribute(AcademyConstants.ATTR_REFUND_ID));
				}
				updatePaymentReference(inDocUE,outEleUE,strRequestAmount);
				
			} 
			else if (strHttpStatusCode.startsWith("4")) {
				log.info("Klarna Http Error Response :: "+ strHttpStatusCode +" | OrderNumber :: "+ strSOrderNo +" | ChargeTransactionKey :: "+ strIdempotencyKey );
				isEligibleForRetry = isEligibleForRetry(inDocUE,outDocUE, docKlarnaResponse,AcademyConstants.STR_KL_4XX_ERROR,AcademyConstants.STR_KL_4XX_RETRYINTERVAL,AcademyConstants.STR_KL_4XX_RETRYLIMIT);
				log.verbose("4XX Klarna Response");
				log.verbose("isEligibleForRetry :: " + isEligibleForRetry);
				
				if (isEligibleForRetry) {
					updateOutDocCodes(outEleUE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
					outEleUE.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
					outEleUE.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_YES);
					outEleUE.setAttribute(AcademyConstants.ATTR_SUSPENDPAYMENT, AcademyConstants.STR_NO);
				} else {
					
					if (iTransactionType == AcademyConstants.INT_KLARNA_EXTEND_AUTH_TRANSACTION) {
						updateOutDocCodes(outEleUE, AcademyConstants.STR_HARD_DECLINED);
						outEleUE.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_HARD_DECLINED);
						createDeclineOrderAgentRecord(env, inDocUE);
					} else {
						generateDummyResponse(outDocUE);
						updatePaymentReference(inDocUE, outEleUE, strRequestAmount);
						String strTotalOpenAuthorizations = XPathUtil.getString(docGetOrderListOut, AcademyConstants.XPATH_TOTAL_OPENAUTHORIZATIONS);
						boolean isPartialCapture = iTransactionType.equals(AcademyConstants.INT_KLARNA_CAPTUPE_TRANSACTION)
								&& !isZero(strTotalOpenAuthorizations) && strHttpStatusCode.equalsIgnoreCase("403");
						boolean isRefundHardDecline = iTransactionType.equals(AcademyConstants.INT_KLARNA_REFUND_TRANSACTION)
								&& !strHttpStatusCode.equalsIgnoreCase("403");
						if (isPartialCapture || isRefundHardDecline) {
							createDeclineOrderAgentRecord(env, inDocUE);
						}
						raiseSettlementFailure(env, inDocUE, outDocUE,strHttpStatusCode);
					}
					outEleUE.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_NO);
					outEleUE.setAttribute(AcademyConstants.ATTR_SUSPENDPAYMENT, AcademyConstants.STR_YES);
					logKlarnaReqAndRespToDB(env, inDocUE, docKlarnaResponse, "DB_FAIL_KLARNA");
				}
			}
			if (strHttpStatusCode.startsWith("5")) {
				log.info("Klarna Http Error Response :: "+ strHttpStatusCode +" | OrderNumber :: "+ strSOrderNo +" | ChargeTransactionKey :: "+ strIdempotencyKey );
				isEligibleForRetry = isEligibleForRetry(inDocUE,outDocUE, docKlarnaResponse,AcademyConstants.STR_KL_5XX_ERROR,AcademyConstants.STR_KL_5XX_RETRYINTERVAL,AcademyConstants.STR_KL_5XX_RETRYLIMIT);
			    log.verbose("5XX Klarna Response");
			    log.verbose("isEligibleForRetry :: " + isEligibleForRetry);
				if (isEligibleForRetry) {
					updateOutDocCodes(outEleUE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
					outEleUE.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
					outEleUE.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_YES);
					outEleUE.setAttribute(AcademyConstants.ATTR_SUSPENDPAYMENT, AcademyConstants.STR_NO);
				} else {
					
					if (iTransactionType.equals(AcademyConstants.INT_KLARNA_EXTEND_AUTH_TRANSACTION)) {
						updateOutDocCodes(outEleUE, AcademyConstants.STR_HARD_DECLINED);
						outEleUE.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_HARD_DECLINED);
						outEleUE.setAttribute(AcademyConstants.ATTR_AUTH_CODE,AcademyConstants.STR_HARD_DECLINED);
					}else {
						generateDummyResponse(outDocUE);
						updatePaymentReference(inDocUE, outEleUE, strRequestAmount);
					}
					outEleUE.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_NO);
					outEleUE.setAttribute(AcademyConstants.ATTR_SUSPENDPAYMENT, AcademyConstants.STR_YES);
				    raiseSettlementFailure(env,inDocUE,outDocUE,strHttpStatusCode);
					logKlarnaReqAndRespToDB(env, inDocUE, docKlarnaResponse, "DB_FAIL_KLARNA");
				}
				
			} 
			outEleUE.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_CODE, strHttpStatusCode);
			outEleUE.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE,strHttpStatusCode);
			outEleUE.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE,strHttpStatusCode);
			log.verbose("END :: prepareOutDocUE :: END");
			log.endTimer("END :: prepareOutDocUE :: END");
		return outDocUE;
	}
    
	
	private void updateOutDocCodes(Element outEleUE, String strCode) {
		log.verbose("START ::  updateOutDocCodes :: START");
		outEleUE.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, strCode);
		outEleUE.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE, strCode);
		outEleUE.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_CODE,strCode);
		outEleUE.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_MESSAGE,strCode);
		outEleUE.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_MESSAGE,strCode);
		outEleUE.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG,strCode);
		log.verbose("END :: updateOutDocCodes :: END");
	}

	/**
	 * 
	 * @param inDocUE
	 * @param outEleUE
	 * @param strRequestAmount
	 * @throws Exception
	 * <p>p
	 */
	private void updatePaymentReference(Document inDocUE, Element outEleUE, String strRequestAmount) throws Exception {
		log.verbose("START ::  updatePaymentReference :: START");
		DecimalFormat decfor = new DecimalFormat("0.00");
		if(iTransactionType == 2) {
			String strPaymentReference8 = XPathUtil.getString(inDocUE,AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_PAYMENT_REF_8);
			double dPaymentReference8 = YFCCommon.isStringVoid(strPaymentReference8) ? 00.00 :  Double.valueOf(strPaymentReference8);
			dPaymentReference8 += Double.valueOf(strRequestAmount);
			strPaymentReference8 = decfor.format(dPaymentReference8);
			outEleUE.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_8, strPaymentReference8);
		}else if (iTransactionType == 4) {
			String strPaymentReference7 = XPathUtil.getString(inDocUE,AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_PAYMENT_REF_7);
			double dPaymentReference7 = YFCCommon.isStringVoid(strPaymentReference7) ? 00.00 :  Double.valueOf(strPaymentReference7);
			dPaymentReference7 += Double.valueOf(strRequestAmount);
			strPaymentReference7 = decfor.format(dPaymentReference7);
			outEleUE.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_7, strPaymentReference7);
		}
		log.verbose("END :: updatePaymentReference :: END");
	}

	/**
	 *
	 * @param inDocUE
	 * @param outDocUE
	 * @param strAuthId
	 * @param strAuthCode
	 * @param strRequestAmount
	 * @return
	 * @throws Exception
	 */
	private Element generateDummyResponse(Document outDocUE) throws Exception {
		log.beginTimer("START ::  generateDummyResponse :: START");
		log.verbose("START ::  generateDummyResponse :: START");
		Element outEleUE = outDocUE.getDocumentElement();
		if (iTransactionType.equals(AcademyConstants.INT_KLARNA_CAPTUPE_TRANSACTION) ||iTransactionType.equals(AcademyConstants.INT_KLARNA_REFUND_TRANSACTION)) {
			updateOutDocCodes(outEleUE,AcademyConstants.STR_DUMMY_SETTLEMENT);
			outEleUE.setAttribute(AcademyConstants.ATTR_AUTH_CODE,AcademyConstants.STR_DUMMY_SETTLEMENT);
		}else {
			updateOutDocCodes(outEleUE,AcademyConstants.STR_DUMMY_AUTH);
			outEleUE.setAttribute(AcademyConstants.ATTR_AUTH_CODE,AcademyConstants.STR_DUMMY_AUTH);
		}
		outEleUE.setAttribute(AcademyConstants.ATTR_AUTH_TIME, getDate(Calendar.DATE,0,AcademyConstants.STR_DATE_TIME_PATTERN_NEW));
		log.verbose("END :: generateDummyResponse :: END" + XMLUtil.getXMLString(outDocUE));
		log.endTimer("END :: generateDummyResponse :: END");
		return outEleUE;
	}




	/**
	 * This Method evaluates Klarna Connecction Response
	 * @param docKlarnaResponse
	 * @return <p><strong>true</strong> when Klarna successful connecction  </p>
	 *         <p><strong>false</strong> when Klarna un-successful connecction  </p>
	 */
	private boolean isConnectionSuccessfull(Document docKlarnaResponse) {
		log.beginTimer("START ::  isConnectionSuccessfull :: START");
		log.verbose("START ::  isConnectionSuccessfull :: START");
		boolean isConnectionSuccessfull = true;
		Element eleKlarnaResponse = docKlarnaResponse.getDocumentElement();
		String strInternalError = eleKlarnaResponse.getAttribute(AcademyConstants.KLARNA_SERVICE_INVOCATION_STATUS);
		if (!YFCCommon.isVoid(strInternalError) && strInternalError.equalsIgnoreCase(AcademyConstants.STR_FAIL)) {
			isConnectionSuccessfull = false;
		}
		log.verbose("END :: isConnectionSuccessfull :: END" + isConnectionSuccessfull);
		log.endTimer("END :: isConnectionSuccessfull :: END");
		return isConnectionSuccessfull;
	}


	/**
	 *
	 * @param strRequestAmount
	 * @return
	 */
	public int getChargeTransactionType(String strRequestAmount) {
		iTransactionType = isNegative(strRequestAmount) ?  AcademyConstants.INT_KLARNA_REFUND_TRANSACTION : AcademyConstants.INT_KLARNA_CAPTUPE_TRANSACTION;
		return iTransactionType;
	}

	/**
	 *
	 * @param amount
	 * @return
	 */
	public static boolean isNegative(Object amount) {
		Double dRequestAmount=0.00;
		if (!YFCCommon.isVoid(amount)) {
			if (amount instanceof Double) {
				dRequestAmount = (Double) amount;
			} else if (amount instanceof String) {
				dRequestAmount = Double.parseDouble((String) amount);
			}
			if (Double.compare(dRequestAmount, 0.0) < 0) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	/**
	 *
	 * @param amount
	 * @return
	 */
	public static boolean isZero(Object amount) {
		Double dRequestAmount=0.00;
		if (!YFCCommon.isVoid(amount)) {
			if (amount instanceof Double) {
				dRequestAmount = (Double) amount;
			} else if (amount instanceof String) {
				dRequestAmount = Double.parseDouble((String) amount);
			}
			if (Double.compare(dRequestAmount, 0.0)==0) {
				return true;
			}
			return false;
		}
		return false;
	}

	/**
	 *
	 * @param outDocUE
	 * @param docKlarnaResponse
	 * @param strChargeTransactionKey
	 * @param strHttpStatusCode
	 * @param strMessageType 
	 * @param strErrorReTryInterval 
	 * @return
	 * @throws Exception
	 */
	private boolean isEligibleForRetry(Document inDocUE, Document outDocUE, Document docKlarnaResponse, String strMessageType,
			String strReTryInterval, String StrReTryLimit) throws Exception {
		boolean bEligibleForRetry = false;
		int iReTryLimit =0;
		int iNextInterval = 0;
		String strChargeTransactionKey = XPathUtil.getString(inDocUE,AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY);
		
		String strErrorMsg = AcademyKlarnaInterfaceUtil.getKlarnaErrorMessage(docKlarnaResponse);
		Element outEleUE = outDocUE.getDocumentElement();
		Element elePaymentTransactionErrorList = outDocUE.createElement(AcademyConstants.ELE_PAYMENT_TRAN_ERROR_LIST);
		Element elePaymentTransactionError = outDocUE.createElement(AcademyConstants.ELE_PAYMENT_TRAN_ERROR);
		XMLUtil.appendChild(outEleUE, elePaymentTransactionErrorList);
		XMLUtil.appendChild(elePaymentTransactionErrorList, elePaymentTransactionError);
		elePaymentTransactionError.setAttribute(AcademyConstants.ATTR_MESSAGE, strErrorMsg);
		elePaymentTransactionError.setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, strMessageType);
		
		String strPaymentTransactionError = AcademyConstants.XPATH_PAYMENT_TRANSACTION_ERROR;
		strPaymentTransactionError = strPaymentTransactionError.replace("{ChargeTransactionKey}", strChargeTransactionKey);
		strPaymentTransactionError = strPaymentTransactionError.replace("{MessageType}", strMessageType);

		NodeList ndPayTransErr = XPathUtil.getNodeList(docGetOrderListOut, strPaymentTransactionError);
		int iNoOfErrorOcc = ndPayTransErr.getLength();
		
		iReTryLimit = Integer.parseInt( props.getProperty(StrReTryLimit));
		log.verbose("ReTry Limit :: "+ iReTryLimit);
		log.verbose("ReTry Count :: "+ iNoOfErrorOcc);
		if (iNoOfErrorOcc < iReTryLimit) {
			
			iNextInterval = Integer.parseInt(props.getProperty(strReTryInterval));
			bEligibleForRetry = true;
		}	
		
		outEleUE.setAttribute(AcademyConstants.ATTR_COLLECTION_DATE,getDate(Calendar.MINUTE,iNextInterval, AcademyConstants.STR_DATE_TIME_PATTERN_1));
		return bEligibleForRetry;
	}
	
	
	
	
	
	/**
	 * This Method returns error_messages from Klarna <p> trims message if length > 100 </P> 
	 * @param inDocUE
	 * @param docKlarnaResponse
	 * @return ErrorMessage
	 * @throws Exception
	 */
	public static String getKlarnaErrorMessage(Document docKlarnaResponse) throws Exception {
		String strErrorMsg = null;
		strErrorMsg = XPathUtil.getString(docKlarnaResponse, AcademyConstants.XPATH_ERROR_MESSAGES);
		strErrorMsg = YFCCommon.isVoid(strErrorMsg) ? XPathUtil.getString(docKlarnaResponse, AcademyConstants.XPATH_ERRORMESSAGE) : strErrorMsg ;
		if(strErrorMsg.length() > 100 ) {
			strErrorMsg = strErrorMsg.replace(AcademyConstants.STR_BLANK,AcademyConstants.STR_EMPTY_STRING);
			if(strErrorMsg.length() > 100 ) {
				strErrorMsg = strErrorMsg.substring(0, 100);
			}
		}
		return strErrorMsg;
	}

	/**
	 *
	 * @param env
	 * @param inDocUE
	 * @throws Exception
	 */
	private void createDeclineOrderAgentRecord(YFSEnvironment env, Document inDocUE) throws Exception  {

		log.beginTimer("createDeclineOrderAgentRecord");
		log.verbose("Begin of createDeclineOrderAgentRecord() method");
		String strOrderHeaderKey = XPathUtil.getString(inDocUE, AcademyConstants.STR_PAYMENT_PATH_AT +AcademyConstants.ATTR_ORDER_HEADER_KEY);
		Document docTaskQueue = XMLUtil.createDocument(AcademyConstants.ELE_TASK_QUEUE);
		Element eleTaskQueue = docTaskQueue.getDocumentElement();
		eleTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_KEY, strOrderHeaderKey);
		eleTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_TYPE, AcademyConstants.ATTR_ORDER_HEADER_KEY);
		eleTaskQueue.setAttribute(AcademyConstants.ATTR_TRANSID, AcademyConstants.TRANSACTION_DECLINED_ORDER_AGENT);
		eleTaskQueue.setAttribute(AcademyConstants.ATTR_AVAIL_DATE, getDate(Calendar.DATE,0,AcademyConstants.STR_DATE_TIME_PATTERN));

		log.verbose("inDoc manageTaskQueue :: \n" + XMLUtil.getXMLString(docTaskQueue));
		AcademyUtil.invokeAPI(env, AcademyConstants.API_MANAGE_TASK_QUEUE, docTaskQueue);
		log.verbose("End of createDeclineOrderAgentRecord() method");
		log.endTimer("createDeclineOrderAgentRecord");
	}

	/**
	 *
	 * @param outDocUE
	 * @param docKlarnaResponse
	 * @return
	 * @throws Exception
	 */
	private Document prepareServiceUnavailableResp(Document docKlarnaResponse) throws Exception {
		log.verbose("Begin of prepareServiceUnavailableResp() method");
		Integer strRetryInterval = Integer.parseInt(props.getProperty(AcademyConstants.ATTR_KL_DEFAULT_RETRYINTERVAL));
		Document outDocUE = XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT);
		Element outEleUE = outDocUE.getDocumentElement();
		Element elePaymentTransactionErrorList = outDocUE.createElement(AcademyConstants.ELE_PAYMENT_TRAN_ERROR_LIST);
		Element elePaymentTransactionError = outDocUE.createElement(AcademyConstants.ELE_PAYMENT_TRAN_ERROR);
		XMLUtil.appendChild(outEleUE, elePaymentTransactionErrorList);
		XMLUtil.appendChild(elePaymentTransactionErrorList, elePaymentTransactionError);
		String strErrorMsg = getKlarnaErrorMessage(docKlarnaResponse);
		elePaymentTransactionError.setAttribute(AcademyConstants.ATTR_MESSAGE, strErrorMsg);
		elePaymentTransactionError.setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE, AcademyConstants.STR_KL_NOT_REACHABLE);

		outEleUE.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
		outEleUE.setAttribute(AcademyConstants.ATTR_AUTH_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
		outEleUE.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_YES);
		outEleUE.setAttribute(AcademyConstants.ATTR_SUSPEND_PAYMENT, AcademyConstants.STR_NO);
		outEleUE.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
		outEleUE.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, AcademyConstants.STR_SERVICE_UNAVAILABLE);
		outEleUE.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
		outEleUE.setAttribute(AcademyConstants.ATTR_COLLECTION_DATE, getDate(Calendar.MINUTE,strRetryInterval,AcademyConstants.STR_DATE_TIME_PATTERN_1));
		log.verbose("End of prepareServiceUnavailableResp() method");
		return outDocUE;
	}


	/**
	 * This Method invoke getOrderList and update {@link docGetOrderListOut}
	 * @param env
	 * @param inDocUE
	 * @throws Exception
	 */
	private void getOrderList(YFSEnvironment env, Document inDocUE) throws Exception {
		log.verbose("Begin of getOrderList() method");
		String strOrderHeaderKey = XPathUtil.getString(inDocUE,AcademyConstants.STR_PAYMENT_PATH_AT +AcademyConstants.ATTR_ORDER_HEADER_KEY);
		Document docGetOrderListTemplateIn = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
		Element eleGetOrderList = docGetOrderListTemplateIn.getDocumentElement();
		eleGetOrderList.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, strOrderHeaderKey);
		env.setApiTemplate(AcademyConstants.API_GET_ORDER_LIST, AcademyConstants.TEMPLATE_GETORDERLIST_KLARNA_PAYMENTUE);
		docGetOrderListOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ORDER_LIST, docGetOrderListTemplateIn);
		env.clearApiTemplate(AcademyConstants.API_GET_ORDER_LIST);
		log.verbose("getOrderList outDoc :: \n" + XMLUtil.getXMLString(docGetOrderListOut));
		log.verbose("End of getOrderList() method");
	}

    public Document populatePaymentDefaultOutputXML(YFSEnvironment env,Document inDocUE) throws Exception  {
            log.debug("Entering populatePaymentDefaultOutputXML");
            Document outDocUE = XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT);
    		Element outEleUE = outDocUE.getDocumentElement();
    		String strIdempotencyKey = XPathUtil.getString(inDocUE,AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY);
    		String strRequestAmount = XPathUtil.getString(inDocUE, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_REQUEST_AMOUNT);
    		strRequestAmount = getDoubleFormateString(Double.valueOf(strRequestAmount));
    		String strDate = getDate(Calendar.MINUTE,0,AcademyConstants.STR_DATE_TIME_PATTERN_NEW);
    		getAuthCode(env, outEleUE);
    		getAuthId(outEleUE,strIdempotencyKey);
            outEleUE.setAttribute(AcademyConstants.ATTR_AUTH_AMOUNT,strRequestAmount);
            outEleUE.setAttribute(AcademyConstants.ATTR_AUTH_EXP_DATE,strDate);
            outEleUE.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE,AcademyConstants.STR_APPROVED);
            outEleUE.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG,AcademyConstants.ATTR_TRANS_NORMAL);
            outEleUE.setAttribute(AcademyConstants.ATTR_AUTH_TIME,strDate);
            outEleUE.setAttribute(AcademyConstants.ATTR_COLLECTION_DATE,strDate);
            outEleUE.setAttribute("ExecutionDate",strDate);
            outEleUE.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_CODE,AcademyConstants.STR_APPROVED);
            outEleUE.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_MESSAGE,AcademyConstants.ATTR_TRANS_NORMAL);
            outEleUE.setAttribute(AcademyConstants.ATTR_RETRY_FLAG,AcademyConstants.STR_NO);
            outEleUE.setAttribute(AcademyConstants.ATTR_SUSPENDPAYMENT,AcademyConstants.STR_NO);
            outEleUE.setAttribute(AcademyConstants.ATTR_TRAN_AMT,strRequestAmount);
            outEleUE.setAttribute("TranRequestTime",strDate);
            outEleUE.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE,AcademyConstants.STR_APPROVED);
            outEleUE.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_MESSAGE,AcademyConstants.ATTR_TRANS_NORMAL);
            outEleUE.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_APPROVED);
            outEleUE.setAttribute(AcademyConstants.ATTR_TRAN_TYPE,XPathUtil.getString(inDocUE, AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_CHARGE_TYPE));
            log.debug("Exiting populatePaymentDefaultOutputXML");
            return outDocUE;
        }
    
	    private void getAuthId(Element outEleUE, String strIdempotencyKey) {
    	 outEleUE.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID,strIdempotencyKey.substring(strIdempotencyKey.length() - 11));
		
	}
	
	private void getAuthCode(YFSEnvironment env, Element outEleUE) {
		String strKlarnaReference = (String) env.getTxnObject(AcademyConstants.STR_KLARNA_REFERENCE);
		if (!YFCObject.isVoid(strKlarnaReference)) {
			outEleUE.setAttribute(AcademyConstants.ATTR_AUTH_CODE, strKlarnaReference);
		}
	}

	private String getDoubleFormateString(Double dValue) {
    	DecimalFormat decfor = new DecimalFormat("0.00");
    	return decfor.format(dValue);
    }
	
	private String getDoubleFormateString(String strValue) {
		Double dValue = Double.valueOf(strValue);
    	return getDoubleFormateString(dValue);
    }
    //This Method Prepare XML that Publish to Export-Table on Klarna_Failure
	public static void logKlarnaReqAndRespToDB(YFSEnvironment env, Document docPayInput, Document docKlarnaResponse,
			String strDBServiceName) throws Exception {
		log.verbose("Start AcademyPaymentProcessingUtil.logPayZReqAndRespToDB() method ::");
		Document docKlarnaReqResp = XMLUtil.createDocument(AcademyConstants.STR_KLARNA_REQUEST_RESPONSE);
		Element eleKlarnaReqRespOut = docKlarnaReqResp.getDocumentElement();
		Element eleKlarnaReqResp = docKlarnaResponse.getDocumentElement();

		Document docKlarnaRequest = (Document) env.getTxnObject(AcademyConstants.STR_KLARNA_REQUEST);
		log.verbose("InDoc KlarnaRequest :: " + XMLUtil.getXMLString(docKlarnaRequest));
		if (!YFSObject.isVoid(docKlarnaRequest)) {
			Element eleKlarnaRequest = docKlarnaRequest.getDocumentElement();
			XMLUtil.importElement(eleKlarnaReqRespOut, eleKlarnaRequest);
		}

		if (!YFSObject.isVoid(docKlarnaResponse)) {
			Element eleKlarnaRes = docKlarnaReqResp.createElement(AcademyConstants.STR_KLARNA_RESPONSE);
			eleKlarnaReqRespOut.appendChild(eleKlarnaRes);
			XMLUtil.importElement(eleKlarnaRes, eleKlarnaReqResp);
		}

		Element elePayInput = docKlarnaReqResp.createElement(AcademyConstants.ELE_PAYMENT_INPUT);
		eleKlarnaReqRespOut.appendChild(elePayInput);
		Element elePayInputTemp = docPayInput.getDocumentElement();
		XMLUtil.importElement(elePayInput, elePayInputTemp);
		AcademyUtil.invokeService(env, strDBServiceName, docKlarnaReqResp);
		log.verbose("End AcademyPaymentProcessingUtil.logPayZReqAndRespToDB() method ::");
	}
	/**
	 * This Method return date in String formate
	 * @param iDaysToAdd : DAYS/HOURS to ADD
	 * @param strRetryInterval : no of hr/days to add
	 * @param strFormate : Date in String Formate
	 * @return
	 */
	private String getDate(Integer iDaysToAdd, Integer strRetryInterval, String strFormate){
		log.verbose("Begin of getDate() method");
		SimpleDateFormat sdf = new SimpleDateFormat(strFormate);
		Calendar cal = Calendar.getInstance();
		cal.add(iDaysToAdd, strRetryInterval);
		String StrDate = sdf.format(cal.getTime());
		log.verbose("End of getDate() method");
		return StrDate;
	}
	
	private Document handleExceptions(Exception exp) throws Exception {
		log.verbose("Begin of handleExceptions() method");
		Integer strRetryInterval = Integer.parseInt(props.getProperty(AcademyConstants.ATTR_KL_DEFAULT_RETRYINTERVAL));
		Document outDocUE = XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT);
		Element outEleUE = outDocUE.getDocumentElement();
		Element elePaymentTransactionErrorList = outDocUE.createElement(AcademyConstants.ELE_PAYMENT_TRAN_ERROR_LIST);
		Element elePaymentTransactionError = outDocUE.createElement(AcademyConstants.ELE_PAYMENT_TRAN_ERROR);
		XMLUtil.appendChild(outEleUE, elePaymentTransactionErrorList);
		XMLUtil.appendChild(elePaymentTransactionErrorList, elePaymentTransactionError);
		String strMessage = exp.getMessage();
		strMessage = strMessage.substring(0, 100);
		elePaymentTransactionError.setAttribute(AcademyConstants.ATTR_MESSAGE, strMessage);
		elePaymentTransactionError.setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE,AcademyConstants.STR_EXCEPTION);
		outEleUE.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
		outEleUE.setAttribute(AcademyConstants.ATTR_AUTH_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
		outEleUE.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_YES);
		outEleUE.setAttribute(AcademyConstants.ATTR_SUSPEND_PAYMENT, AcademyConstants.STR_NO);
		outEleUE.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
		outEleUE.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, AcademyConstants.STR_SERVICE_UNAVAILABLE);
		outEleUE.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
		outEleUE.setAttribute(AcademyConstants.ATTR_COLLECTION_DATE, getDate(Calendar.MINUTE,strRetryInterval,AcademyConstants.STR_DATE_TIME_PATTERN_1));
		log.verbose("End of handleExceptions() method");
		return outDocUE;
	}

	@Override
	public void setProperties(Properties props) throws Exception {
		this.props = props;
	}
}
