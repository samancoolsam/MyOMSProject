package com.academy.ecommerce.sterling.util.webservice;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.util.AcademyPaymentProcessingUtil;
import com.academy.ecommerce.sterling.util.stub.AcademyPaymentStub;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.shared.ycp.YFSContext;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.academy.ecommerce.sterling.util.webservice.AcademyRTSPLCCUtil;
/**
 * This service makes REST call to RTS system for payment type PLCC
 * 
 * @author C0014737
 * @created on 2018-08-20
 */
public class AcademyRTSWebserviceForPLCC {

	// Instance of logger
	private static YFCLogCategory log = YFCLogCategory
	.instance(AcademyRTSWebserviceForPLCC.class);

	// It holds the properties configured in AcaddemyRTSWebserviceForPLCC
	// service
	private static Properties props;

	public void setProperties(Properties props) {
		this.props = props;
	}
	private static String strInvokePaymentStub = YFSSystem.getProperty(AcademyConstants.INVOKE_PAYMENT_STUB); //OMNI-99827
	
	/**
	 * This method creates and sends the payment request to RTS. Based on the
	 * webservice response, the UE output is approved or declined or set as
	 * retry
	 * 
	 * @param docPaymentIn
	 * @return docPaymentOut
	 * @throws Exception
	 */
	public Document invokeRTSPLCCService(YFSEnvironment env,
			Document docPaymentIn) throws Exception {

		log
		.verbose("Begin of AcaddemyRTSWebserviceForPLCC.invokeRTSPLCCService() method");
		log
		.verbose("AcaddemyRTSWebserviceForPLCC.invokeRTSPLCCService() Payment Input XML ::"
				+ XMLUtil.getXMLString(docPaymentIn));

		Document docPaymentOut = null;
		Document docRTSResponseDoc = null;
		String strTransactionType = determineTransactionType(docPaymentIn);

		Document docGetOrderList = getOrderList(env, 
				docPaymentIn.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));

		validateTimeoutReversal(env, docPaymentIn, docGetOrderList, strTransactionType);
		Document docRTSRequest = getRTSHTTPRequestDoc(env, docPaymentIn, docGetOrderList, strTransactionType, null);

		log.verbose("****Request to RTS  from  :: "+ XMLUtil.getXMLString(docRTSRequest));
		//OMNI-99827 - Start
		if(!YFCObject.isVoid(strInvokePaymentStub) && AcademyConstants.STR_YES.equalsIgnoreCase(strInvokePaymentStub)){
			//Invoke payment stub
			log.verbose("strInvokePaymentStub: "+strInvokePaymentStub);
			docRTSResponseDoc = AcademyPaymentStub.invokeRTSPaymentStubForPLCC(env,docRTSRequest,docPaymentIn);
		}		
		//OMNI-99827 - End
		else{
		docRTSResponseDoc = sendHTTP(docRTSRequest);
		}
		log.verbose("****Response from RTS :: "+ XMLUtil.getXMLString(docRTSResponseDoc));
		docPaymentOut = translateRTSResponse(env, docPaymentIn,docRTSResponseDoc, docRTSRequest, docGetOrderList, strTransactionType);
		log.verbose("AcaddemyRTSWebserviceForPLCC.invokeRTSPLCCService() Payment Output XML:: "+ XMLUtil.getXMLString(docPaymentOut));
		log.verbose("End of AcaddemyRTSWebserviceForPLCC.invokeRTSPLCCService() method");

		return docPaymentOut;
	}

	/**
	 * This method creates a webservice input
	 * 
	 * @param inDoc
	 * @param strTransactionType
	 * @param strChargeType
	 * @param lRTSTranSeqNo
	 * @return Document
	 * @throws Exception
	 */

	private Document getRTSHTTPRequestDoc(YFSEnvironment env,
			Document docPaymentIn, Document docGetOrderList, 
			String strTransactionType, String strReversalOptions) 
	throws Exception {

		log
		.verbose("Inside getRTSHTTPRequestDoc() with input :: Transaction type::"
				+ strTransactionType
				+ "And inDoc ::"
				+ XMLUtil.getXMLString(docPaymentIn));
		Element elePaymentInp = docPaymentIn.getDocumentElement();
		String strPaymentType = elePaymentInp
		.getAttribute(AcademyConstants.ATTR_PAYMENT_TYPE);

		log.verbose("paymentType::" + strPaymentType);
		log.verbose("tranType::" + strTransactionType);

		Document docRTSRequest = YFCDocument.createDocument(
				AcademyConstants.ATTR_RTS_XMLAJBF_IPAY_REQUEST).getDocument();
		Element eleRTSRequest = docRTSRequest.getDocumentElement();

		// Update the mandatory paratmeters in webservice input
		Element eleIxCmd = docRTSRequest
		.createElement(AcademyConstants.ATTR_RTS_CMD);
		Element eleIxDebitCredit = docRTSRequest
		.createElement(AcademyConstants.ATTR_RTS_DEBIT_CREDIT);
		Element eleIxStoreNumber = docRTSRequest
		.createElement(AcademyConstants.ATTR_RTS_STORE_NUM);
		Element eleIxTerminalNumber = docRTSRequest
		.createElement(AcademyConstants.ATTR_RTS_TERMINAL_NUM);

		eleIxCmd.appendChild(docRTSRequest.createTextNode(YFSSystem
				.getProperty(AcademyConstants.RTS_REQ_CMD)));
		eleIxDebitCredit.appendChild(docRTSRequest
				.createTextNode(AcademyConstants.RTS_REQ_CREDIT));
		eleIxStoreNumber.appendChild(docRTSRequest.createTextNode(YFSSystem
				.getProperty(AcademyConstants.RTS_REQ_STORE_NUM)));
		eleIxTerminalNumber.appendChild(docRTSRequest.createTextNode(YFSSystem
				.getProperty(AcademyConstants.RTS_REQ_TERMINAL_NUM)));

		eleRTSRequest.appendChild(eleIxCmd);
		eleRTSRequest.appendChild(eleIxDebitCredit);
		eleRTSRequest.appendChild(eleIxStoreNumber);
		eleRTSRequest.appendChild(eleIxTerminalNumber);

		// Update the transaction type in webservice input
		Element eleIxTranType = docRTSRequest
		.createElement(AcademyConstants.ATTR_RTS_TRAN_TYPE);
		eleIxTranType.appendChild(docRTSRequest
				.createTextNode(strTransactionType));
		eleRTSRequest.appendChild(eleIxTranType);

		// Update System Date and Time in webservice input
		Element eleIxDate = docRTSRequest
		.createElement(AcademyConstants.ATTR_RTS_DATE);
		Element eleIxTime = docRTSRequest
		.createElement(AcademyConstants.ATTR_RTS_TIME);

		SimpleDateFormat sdfDate = new SimpleDateFormat(
				AcademyConstants.STR_RTS_DATE_FORMAT);
		SimpleDateFormat sdfTime = new SimpleDateFormat(
				AcademyConstants.STR_RTS_DATE_TIME);
		Calendar cal = Calendar.getInstance();
		eleIxDate.appendChild(docRTSRequest.createTextNode(sdfDate.format(cal
				.getTime())));
		eleIxTime.appendChild(docRTSRequest.createTextNode(sdfTime.format(cal
				.getTime())));

		eleRTSRequest.appendChild(eleIxDate);
		eleRTSRequest.appendChild(eleIxTime);

		// Update Card Details in Webservice Input
		String strCreditCardNo = elePaymentInp
		.getAttribute(AcademyConstants.CREDIT_CARD_NO);
		String strCreditCardExpiry = elePaymentInp
		.getAttribute(AcademyConstants.ATTR_CC_EXPIRATION_DATE);
		//start ACC-II Tokenization
		String strPaymentKey = docPaymentIn.getDocumentElement().getAttribute(AcademyConstants.ATTR_PAYMENT_KEY);
		log.verbose("strPaymentKey:: " + strPaymentKey);
		Element elePaymentMethod = XMLUtil.getElementByXPath(docGetOrderList,
				"OrderList/Order/ChargeTransactionDetails/ChargeTransactionDetail[@PaymentKey='"+strPaymentKey+"']/PaymentMethod");
		log.verbose("elePaymentMethod::" + XMLUtil.getElementXMLString(elePaymentMethod));
		String strPayRef3 = docPaymentIn.getDocumentElement().getAttribute(AcademyConstants.ATTR_PAYMENT_REF_3);
		if (!YFCObject.isVoid(strCreditCardNo)) {
			// Update the credit Card Number
			
			if(!YFCObject.isVoid(strPayRef3) && AcademyConstants.STR_ACC_PAYMENT.equalsIgnoreCase(strPayRef3)) {

			Element elePosEchoField = docRTSRequest
			.createElement(AcademyConstants.ATTR_RTS_POS_ECHO_FIELD);
			Element eleIxAccount = docRTSRequest
			.createElement(AcademyConstants.ATTR_RTS_ACCOUNT);

			log.verbose("docRTSRequest::" + XMLUtil.getXMLString(docRTSRequest));
			//Contains the Tokenized Value of the PLCC Card No
			elePosEchoField.appendChild(docRTSRequest.createTextNode(strCreditCardNo));
			String strDisplayCreditCardNo = elePaymentMethod.getAttribute(AcademyConstants.ATTR_DISPLAY_CREDIT_CARD_NO);
			log.verbose("strDisplayCreditCardNo:: " + strDisplayCreditCardNo);
			
			// Contains Masked Account No
			eleIxAccount.appendChild(docRTSRequest.createTextNode(AcademyConstants.Mask_CC +strDisplayCreditCardNo));
			
			eleRTSRequest.appendChild(eleIxAccount);
			eleRTSRequest.appendChild(elePosEchoField);
		
			}
			else {
				Element eleIxAccount = docRTSRequest
						.createElement(AcademyConstants.ATTR_RTS_ACCOUNT);
						//Contains the clear text field value of PLCC Card No
						eleIxAccount.appendChild(docRTSRequest.createTextNode(strCreditCardNo));

						eleRTSRequest.appendChild(eleIxAccount);
			}
			
		}
		//End ACC-II Tokenization
		if (!YFCObject.isVoid(strCreditCardExpiry)) {
			// Update the credit Card Expiry
			Element eleIxExpDate = docRTSRequest
			.createElement(AcademyConstants.ATTR_RTS_EXPIRY_DATE);
			eleIxExpDate.appendChild(docRTSRequest
					.createTextNode(strCreditCardExpiry));
			eleRTSRequest.appendChild(eleIxExpDate);
		}

		// Update OrderNo and Request Amount
		Element eleIxAmount = docRTSRequest
		.createElement(AcademyConstants.ATTR_RTS_AMOUNT);
		Element eleIxInvoice = docRTSRequest
		.createElement(AcademyConstants.ATTR_RTS_INVOICE);

		String reqAmount = elePaymentInp
		.getAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT);
		log.verbose("Amount::" + reqAmount);
		
		Double dRequestAmt = new Double(reqAmount);
		DecimalFormat df = new DecimalFormat("0.00");
		
		eleIxAmount.appendChild(docRTSRequest.createTextNode(df.format(
				dRequestAmt.doubleValue()).replace(".", "").replace("-", "")));
		eleIxInvoice.appendChild(docRTSRequest.createTextNode(elePaymentInp
				.getAttribute(AcademyConstants.ATTR_ORDER_NO)));

		eleRTSRequest.appendChild(eleIxAmount);
		eleRTSRequest.appendChild(eleIxInvoice);
		
		// Start OMNI-23546  Enable/Disable DI functionality in OMS
		String strDIEnabled = YFSSystem.getProperty(AcademyConstants.IS_DI_Enabled);
		log.verbose("IsDeferredInterestEnabled :: " + strDIEnabled);
		String strPayRef5 ="";
		if (!YFCObject.isVoid(strDIEnabled) && AcademyConstants.STR_YES.equalsIgnoreCase(strDIEnabled)) {
		 strPayRef5 = docPaymentIn.getDocumentElement().getAttribute(AcademyConstants.ATTR_PAYMENT_REF_5);
		 log.verbose("Payment Reference5 is :: " + strPayRef5);
		}
		// End OMNI-23546  Enable/Disable DI functionality in OMS
		
		// Update Option i.e Actual/timeout reversal call details
		Element eleIxOptions = docRTSRequest
		.createElement(AcademyConstants.ATTR_RTS_OPTIONS);

		if (!YFCObject.isVoid(strReversalOptions)) {
			log.verbose("Preparing input for reversal ::" + strReversalOptions);
			// Invoking a webservice reversal
			if (!YFCObject.isVoid(strPayRef3) && AcademyConstants.STR_ACC_PAYMENT.equalsIgnoreCase(strPayRef3)) {
				if (!strReversalOptions.contains(AcademyConstants.STR_TOKENIZATION)) {
					eleIxOptions.appendChild(
							docRTSRequest.createTextNode(strReversalOptions + AcademyConstants.STR_TOKENIZATION));
				} else {
					eleIxOptions.appendChild(docRTSRequest.createTextNode(strReversalOptions));
				}
			} else {

				eleIxOptions.appendChild(docRTSRequest.createTextNode(strReversalOptions));
			}
			
		} else {
			// Invoking an actual webservice call
			YFSContext oEnv = (YFSContext) env;
			long lRTSTranSeqNo = oEnv.getNextDBSeqNo(YFSSystem.getProperty(AcademyConstants.RTS_TRAN_SEQ_NUM));
			log.verbose("RTS Seq No:: " + AcademyConstants.RTS_OPTIONS_SALE + lRTSTranSeqNo);
			// Start OMNI-27989 & OMNI-26204 Adding Sales Order No in IXOption for refund
			if (AcademyConstants.RTS_REQ_TRAN_TYPE_REFUND.equals(strTransactionType) && (!YFCObject.isVoid(strPayRef5))
					&& !YFCObject.isVoid(strDIEnabled) && AcademyConstants.STR_YES.equalsIgnoreCase(strDIEnabled)) {
				String strSalesOrderNo = docPaymentIn.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO);
				if (!YFCObject.isVoid(strPayRef3) && AcademyConstants.STR_ACC_PAYMENT.equalsIgnoreCase(strPayRef3)) {
					eleIxOptions.appendChild(docRTSRequest.createTextNode(AcademyConstants.STR_PRIVATE_LABEL + " "
							+ AcademyConstants.RTS_OPTIONS_SALE + lRTSTranSeqNo + AcademyConstants.STR_TOKENIZATION
							+ " " + AcademyConstants.STR_ORG_INVOICE + strSalesOrderNo));
				} else {
					eleIxOptions.appendChild(docRTSRequest
							.createTextNode(AcademyConstants.STR_PRIVATE_LABEL + " " + AcademyConstants.RTS_OPTIONS_SALE
									+ lRTSTranSeqNo + " " + AcademyConstants.STR_ORG_INVOICE + strSalesOrderNo));
					// added _ to orginvoice string in the academy constants
					// Added the condition to check if the promotion is PLCC_DEF_INT_6M or
					// PLCC_DEF_INT_12M for regular , there is no requirement to pass
				}
			}
			// End OMNI-27989  & OMNI-26204 Adding Sales Order No in IXOption for refund
			else {
				if (!YFCObject.isVoid(strPayRef3) && AcademyConstants.STR_ACC_PAYMENT.equalsIgnoreCase(strPayRef3)) {
					eleIxOptions.appendChild(docRTSRequest.createTextNode(AcademyConstants.STR_PRIVATE_LABEL + " "
							+ AcademyConstants.RTS_OPTIONS_SALE + lRTSTranSeqNo + AcademyConstants.STR_TOKENIZATION));
				} else {
					eleIxOptions.appendChild(docRTSRequest.createTextNode(AcademyConstants.STR_PRIVATE_LABEL + " "
							+ AcademyConstants.RTS_OPTIONS_SALE + lRTSTranSeqNo));
				}
			}
		}
		eleRTSRequest.appendChild(eleIxOptions);

		// IxNeedsReversal is mapped to AUTHID and is to be used in PreAuthComp
		// calls
		if ((AcademyConstants.RTS_REQ_TRAN_TYPE_PRE_AUTH_COMP.equals(strTransactionType)
				|| AcademyConstants.RTS_REQ_TRAN_TYPE_VOID.equals(strTransactionType))
				&& YFCObject.isVoid(strReversalOptions)) {

			//Retrieve IxNeedsReversal from corresponding AUTH
			String strAuthId = XPathUtil.getString(docPaymentIn,
					AcademyConstants.STR_PAYMENT_PATH_AT + AcademyConstants.ATTR_AUTHORIZATION_ID);
			String strTranReturnMsg = getTranMsgForAuth(docGetOrderList, strAuthId);
			
			Element eleIxNeedsReversal = docRTSRequest
			.createElement(AcademyConstants.ATTR_RTS_NEEDS_REVERSAL);
			eleIxNeedsReversal.appendChild(docRTSRequest
					.createTextNode(strTranReturnMsg));

			eleRTSRequest.appendChild(eleIxNeedsReversal);
		}
				
		// Start OMNI-26203 & OMNI_26204 Adding IXIssueNumber for PreAuthComp, Sale and Refund
		if (!YFCObject.isVoid(strDIEnabled) && AcademyConstants.STR_YES.equalsIgnoreCase(strDIEnabled)
				&& !YFCObject.isVoid(strPayRef5)) {
			if (AcademyConstants.RTS_REQ_TRAN_TYPE_PRE_AUTH_COMP.equals(strTransactionType)
					|| AcademyConstants.RTS_REQ_TRAN_TYPE_SALE.equals(strTransactionType)
					|| AcademyConstants.RTS_REQ_TRAN_TYPE_REFUND.equals(strTransactionType)) {
				log.verbose("Transaction Type is Refund or PreAuthComp Or Sale adding IXIssueNumber");
				Element eleIxIssueNumber = docRTSRequest.createElement(AcademyConstants.ATTR_RTS_ISSUE_NUMBER);
				eleIxIssueNumber.appendChild(docRTSRequest.createTextNode(AcademyConstants.STR_PROMO + strPayRef5));
				eleRTSRequest.appendChild(eleIxIssueNumber);
			}
		}
		// End OMNI-26203 & OMNI_26204 Adding IXIssueNumber for PreAuthComp, Sale and Refund

		//Updated the AVS data with a dummy value as its a mandatory field.
		Element eleMailOrderAVSData = docRTSRequest
		.createElement(AcademyConstants.ATTR_RTS_MAIL_ORDER_AVS_DATA);
		eleMailOrderAVSData.appendChild(docRTSRequest
				.createTextNode("*ECI"));
		eleRTSRequest.appendChild(eleMailOrderAVSData);

		log.verbose("Exiting getRTSHTTPRequestDoc() with request doc ::"
				+ XMLUtil.getXMLString(docRTSRequest));
		return docRTSRequest;
	}

	/**
	 * This method determines the transaction type
	 * 
	 * @param strRequestAmount
	 * @param strChargeType
	 * @param strAuthId
	 * @return
	 * @throws Exception
	 */
	private String determineTransactionType(Document docPaymentIn)
	throws Exception {
		String strTransactionType = null;
		String strAuthId = XPathUtil.getString(docPaymentIn,
				AcademyConstants.STR_PAYMENT_PATH_AT
				+ AcademyConstants.ATTR_AUTHORIZATION_ID);
		String strChargeType = XPathUtil.getString(docPaymentIn,
				AcademyConstants.STR_PAYMENT_PATH_AT
				+ AcademyConstants.ATTR_CHARGE_TYPE);
		String strRequestAmount = XPathUtil.getString(docPaymentIn,
				AcademyConstants.STR_PAYMENT_PATH_AT
				+ AcademyConstants.ATTR_REQUEST_AMOUNT);

		if (AcademyConstants.STR_CHRG_TYPE_AUTH.equals(strChargeType)) {
			strTransactionType = strRequestAmount
			.startsWith(AcademyConstants.STR_HYPHEN) ? AcademyConstants.RTS_REQ_TRAN_TYPE_VOID
					: AcademyConstants.RTS_REQ_TRAN_TYPE_PRE_AUTH;
			log.verbose("strRequestAmount : " + strRequestAmount);

		} else if (AcademyConstants.STR_CHRG_TYPE_CHARGE.equals(strChargeType)) {

			if (strRequestAmount.startsWith(AcademyConstants.STR_HYPHEN)) {
				// For Refunds Make a 'Refund' call
				strTransactionType = AcademyConstants.RTS_REQ_TRAN_TYPE_REFUND;
			} else {
				// This is a settlement for the PLCC card
				if (!YFCObject.isVoid(strAuthId)) {
					strTransactionType = AcademyConstants.RTS_REQ_TRAN_TYPE_PRE_AUTH_COMP;
				} else {
					// No valid authorization present on the order. Invoking a
					// Sale Transaction
					strTransactionType = AcademyConstants.RTS_REQ_TRAN_TYPE_SALE;
				}
			}

		}

		return strTransactionType;
	}

	/**
	 * Method makes a HTTPS call to RTS system
	 * 
	 * @param property
	 * @return
	 * @throws Exception
	 */
	private Document sendHTTP(Document requestXMLDoc)
	throws Exception {
		log.verbose("Begin of  AcaddemyRTSWebserviceForPLCC.sendHTTP() method");
		// URL url;
		Document docRTSResponse = null;

		HttpsURLConnection httpConnection = null;
		// StringBuffer strRTSResponse = new StringBuffer();
		BufferedReader brReader = null;
		DataOutputStream dosOutputStream = null;
		String strEndpointURL = YFSSystem
		.getProperty(AcademyConstants.PROP_PLCC_ENDPOINT_URL);
		String strSSLTrustStoreKey = props
		.getProperty(AcademyConstants.STR_KEY_STORE_GC);
		String strSSLTrustStorePassword = props
		.getProperty(AcademyConstants.STR_KEY_PASSWORD_GC);
		// To set protocol
		String strSSLContext = props
		.getProperty(AcademyConstants.STR_HTTP_SSLCONTEXT);

		try {
			log.verbose("RTS PLCC URL::" + strEndpointURL);
			System.setProperty(AcademyConstants.STR_KEY_SSL_TRUST_STORE_GC,strSSLTrustStoreKey);
			System.setProperty(AcademyConstants.STR_KEY_SSL_TRUST_STORE_PASSWORD_GC,strSSLTrustStorePassword);
			URL url = new URL(strEndpointURL.trim());
			log.verbose(":::Connection is about to be established:::" + url.toString());

			httpConnection = (HttpsURLConnection) url.openConnection();
			httpConnection.setRequestMethod(AcademyConstants.STR_HTTP_GET);
			httpConnection.setRequestProperty(AcademyConstants.STR_CONTENT_TYPE,AcademyConstants.STR_TEXT_XML);
			httpConnection.setRequestProperty(AcademyConstants.STR_HTTP_CACHE_CONTROL,AcademyConstants.STR_HTTP_NO_CACHE);
			httpConnection.setUseCaches(false);
			httpConnection.setDoInput(true);
			httpConnection.setDoOutput(true);

			String strConnectionTimeout = props.getProperty(AcademyConstants.STR_RTS_TIME_OUT);
			String strReadTimeout =    props.getProperty(AcademyConstants.STR_RTS_READ_TIME_OUT);
			httpConnection.setConnectTimeout(Integer.parseInt(strConnectionTimeout));
			httpConnection.setReadTimeout(Integer.parseInt(strReadTimeout));
			log.verbose("::strSSLContext ::" + strSSLContext);

			if (!YFCObject.isNull(strSSLContext)) {
				SSLContext sc = SSLContext.getInstance(strSSLContext); //$NON-NLS-1$
				sc.init(null, null, new java.security.SecureRandom());
				httpConnection.setSSLSocketFactory(sc.getSocketFactory());
			}
			log.verbose("::Connection object created ::"+ ((HttpsURLConnection) httpConnection).getSSLSocketFactory());

			// Send request
			dosOutputStream = new DataOutputStream(httpConnection.getOutputStream());
			log.verbose("created outputStreamObject!!" + dosOutputStream);
			log.verbose("sending request ::"
					+ XMLUtil.getXMLString(requestXMLDoc));
			dosOutputStream.writeBytes(XMLUtil.getXMLString(requestXMLDoc));
			dosOutputStream.flush();
			dosOutputStream.close();

			log.verbose(":::Message posted in RTS Stream:::");
			InputStream is = httpConnection.getInputStream();
			brReader = new BufferedReader(new InputStreamReader(is));
			String inputLine;
			StringBuffer strRTSResponse = new StringBuffer();
			while ((inputLine = brReader.readLine()) != null) {
				strRTSResponse.append(inputLine);
			}
			// Closing the Input Stream and Reader after reading the output data
			brReader.close();
			is.close();

			log.verbose("Xml Response from RTS:::" + strRTSResponse.toString());
			docRTSResponse = XMLUtil.getDocument(strRTSResponse.toString());

		} catch (SocketTimeoutException e) {

			log.info("RTS PLCC Exception:: SocketTimeoutException :: "+e.toString()+" Exception Stack Trace"+e.getStackTrace().toString());
			docRTSResponse = XMLUtil.createDocument(AcademyConstants.ELE_ERROR);
			docRTSResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE,AcademyConstants.TIME_OUT_ERROR_STATUS_CODE);
			docRTSResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_EXCEPTION_TYPE,AcademyConstants.SOCKET_TIME_OUT_EXCEPTION);
			docRTSResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_ERROR_MESSAGE, e.getMessage());
		} catch (SSLHandshakeException e) {

			log.info("RTS PLCC Exception:: SSLHandshakeException :: "+e.toString()+" Exception Stack Trace"+e.getStackTrace().toString());
			docRTSResponse = XMLUtil.createDocument(AcademyConstants.ELE_ERROR);
			docRTSResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE,AcademyConstants.TIME_OUT_ERROR_STATUS_CODE);
			docRTSResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_EXCEPTION_TYPE,AcademyConstants.SSL_HANDSHAKE_EXCEPTION);
			docRTSResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_ERROR_MESSAGE, e.getMessage());
		} catch (Exception e) {
			log.info("RTS PLCC Exception:: Generic Exception :: "+e.toString()+" Exception Stack Trace"+e.getStackTrace().toString());
			docRTSResponse = XMLUtil.createDocument(AcademyConstants.ELE_ERROR);
			docRTSResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE,AcademyConstants.STATUS_CODE_504);
			docRTSResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_ERROR_MESSAGE, e.getMessage());
		} finally {

			log.verbose("Inside the finally block of sendHTTP() method");
			try {
				if (httpConnection != null){
					httpConnection.disconnect();
				}
				if(dosOutputStream != null){
					dosOutputStream.flush();
					dosOutputStream.close();
				}
				if(brReader != null){
					brReader.close();
				}
			} catch (IOException e) {
				log.info("RTS PLCC Exception:: IOException :: "+e.toString()+" Exception Stack Trace"+e.getStackTrace().toString());
				docRTSResponse = XMLUtil.createDocument(AcademyConstants.ELE_ERROR);
				docRTSResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE,AcademyConstants.STATUS_CODE_504);
				docRTSResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_ERROR_MESSAGE, e.getMessage());
			}
		}
		log.verbose("End of  AcaddemyRTSWebserviceForPLCC.sendHTTP() method ");
		return docRTSResponse;
	}

	/**
	 * This method transforms the webservice response into Sterling readable XML
	 * format
	 * 
	 * @param docRTSResponse
	 * @return Document
	 * @throws Exception
	 */

	private Document translateRTSResponse(YFSEnvironment env, Document docPaymentIn, 
			Document docRTSResponse, Document docRTSRequest, 
			Document docGetOrderList, String strTransactionType) 
	throws Exception {

		log
		.verbose("Begin of AcaddemyRTSWebserviceForPLCC.translateRTSResponse() method");

		Document docPaymentOut         = null;
		String strActionCode           = null;
		String strPrnFile = null;
		String strChargeTransactionKey = XPathUtil.getString(docPaymentIn,AcademyConstants.STR_PAYMENT_PATH_AT+ AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY);
		String strAuthID = XPathUtil.getString(docPaymentIn,AcademyConstants.STR_PAYMENT_PATH_AT+ AcademyConstants.ATTR_AUTHORIZATION_ID);
		String strOrderNo = docPaymentIn.getDocumentElement().getAttribute(AcademyConstants.ATTR_ORDER_NO);
		log.verbose("ChargeTransactionKey ::"+strChargeTransactionKey);

		if (YFCCommon.isVoid(docRTSResponse)) {
			log.info("RTS ACC Exception:: --> No webservice response ");
			//Retrieve timeOut count for this ChargeTransaction
			int iErrorCount = getCountOfPaymentTransactionError(docGetOrderList, 
					strChargeTransactionKey, AcademyConstants.STR_SERVICE_UNAVAILABLE);
			// Updating output as service unavailable so as to retry after
			docPaymentOut = prepareServiceUnavailableResp(docPaymentIn, docRTSResponse, 
					docRTSRequest, strTransactionType, iErrorCount);
			
			updateDBWithWebserviceReponses(env, docPaymentIn, docRTSRequest,
					docRTSResponse);
			return docPaymentOut;
		}

		Element eleRespActionCode = (Element) docRTSResponse
		.getElementsByTagName(AcademyConstants.RTS_RESP_ACTION_CODE)
		.item(0);

		if (!YFCObject.isVoid(eleRespActionCode)) {
			strActionCode = eleRespActionCode.getTextContent();
		}
		log.verbose("ActionCode is ::" + strActionCode);
		Element eleRespPrnFile = (Element) docRTSResponse
		.getElementsByTagName(AcademyConstants.RTS_RESP_IX_PNR_FILE)
		.item(0);
				
		if (!YFCObject.isVoid(eleRespPrnFile)) {
			strPrnFile = eleRespPrnFile.getTextContent();
		}
		
		if (!YFCCommon.isVoid(strActionCode) &&
				AcademyConstants.STR_RTS_SUCCESS_RESP.equals(strActionCode)) {
			log.verbose("Authorization was successful");
			docPaymentOut = prepareApprovedResp(docRTSResponse);
			
		} else if (!YFCCommon.isVoid(strActionCode) &&
				AcademyConstants.STR_RTS_DECLINED_RESP.equals(strActionCode)) {
			log.verbose("Authorization failed!! Payment to be set as declined");
			log.error("RTS ACC Action Code : " + AcademyConstants.STR_RTS_DECLINED_RESP + " Error Message : "
					+ AcademyConstants.STR_RTS_DECLINED_ERROR_MSG + strPrnFile + " for OrderNo : " + strOrderNo);
			docPaymentOut = prepareDeclinedResponse(env, docPaymentIn, docRTSRequest, docRTSResponse, docGetOrderList,
					strTransactionType);

			updateDBWithWebserviceReponses(env, docPaymentIn, docRTSRequest,
					docRTSResponse);
		} 
		//start ACC-II Tokenization
		else if(!YFCCommon.isVoid(strActionCode) &&
				AcademyConstants.STR_TOKENEX_DOWN_RSP.equals(strActionCode)) {
			log.error("RTS ACC Action Code : " + AcademyConstants.STR_TOKENEX_DOWN_RSP + " Error Message : "
					+ AcademyConstants.RTS_TOKENEX_DOWN_ERROR_MSG + strPrnFile + " for OrderNo : " + strOrderNo);
			// Retrieve timeOut count for this ChargeTransaction
			int iErrorCount = getCountOfPaymentTransactionError(docGetOrderList, 
					strChargeTransactionKey, AcademyConstants.STR_SERVICE_UNAVAILABLE);
			// Updating output as Bank Down so as to retry after
			docPaymentOut = prepareServiceUnavailableResp(docPaymentIn, docRTSResponse, 
					docRTSRequest, strTransactionType, iErrorCount);
			
			updateDBWithWebserviceReponses(env, docPaymentIn, docRTSRequest,
					docRTSResponse);
			
		} /*else if(!YFCCommon.isVoid(strActionCode) &&
				AcademyConstants.STR_RTS_TIME_OUT_RSP.equals(strActionCode)) {
			log.error("Error Code : " + AcademyConstants.STR_RTS_TIME_OUT_RSP + " Error Message : " 
				+ AcademyConstants.RTS_TIME_OUT_ERROR_MSG
					+ strPrnFile);
			// Retrieve timeOut count for this ChargeTransaction
			int iErrorCount = getCountOfPaymentTransactionError(docGetOrderList, 
					strChargeTransactionKey, AcademyConstants.STR_SERVICE_UNAVAILABLE);
			// Updating output as Time Out so as to retry after
			docPaymentOut = prepareServiceUnavailableResp(docPaymentIn, docRTSResponse, 
					docRTSRequest, strTransactionType, iErrorCount);
		}*/
		else if ((!YFCCommon.isVoid(strActionCode) &&
					AcademyConstants.STR_RTS_UNAVL_RESP.equals(strActionCode)))
		{
			log.error("RTS ACC Action Code : " + AcademyConstants.STR_RTS_UNAVL_RESP + " Error Message : "
                    + AcademyConstants.RTS_UNAVAILABLE_ERROR_MSG + strPrnFile +" for OrderNo : "+strOrderNo);

			// Retrieve timeOut count for this ChargeTransaction
			int iErrorCount = getCountOfPaymentTransactionError(docGetOrderList, 
					strChargeTransactionKey, AcademyConstants.STR_SERVICE_UNAVAILABLE);
			// Updating output as service unavailable so as to retry after
			docPaymentOut = prepareServiceUnavailableResp(docPaymentIn, docRTSResponse, 
					docRTSRequest, strTransactionType, iErrorCount);
			
			updateDBWithWebserviceReponses(env, docPaymentIn, docRTSRequest,
					docRTSResponse);
		}
		//End ACC-II Tokenization
		else {
			String exceptionType = docRTSResponse.getDocumentElement().getAttribute(AcademyConstants.ATTR_EXCEPTION_TYPE);
			// Exception Code retrieved for Socket Timeout and SSL Handshake
			if (exceptionType.equals(AcademyConstants.SSL_HANDSHAKE_EXCEPTION )
					|| exceptionType.equals(AcademyConstants.SOCKET_TIME_OUT_EXCEPTION)) {
				
				 log.error("RTS ACC Error Message : "
                         + AcademyConstants.STR_EXCEPTION_IS + exceptionType +" for OrderNo : "+strOrderNo);

				// Retrieve timeOut count for this ChargeTransaction
				int iErrorCount = getCountOfPaymentTransactionError(docGetOrderList, 
						strChargeTransactionKey, AcademyConstants.STR_SERVICE_UNAVAILABLE);
				// Updating output as service unavailable so as to retry after
				docPaymentOut = prepareServiceUnavailableResp(docPaymentIn, docRTSResponse, 
						docRTSRequest, strTransactionType, iErrorCount);
			} else {
				log.error("RTS ACC Exception or Action Codes not handled hence declined the OrderNo : "+strOrderNo);
				docPaymentOut = prepareDeclinedResponse(env, docPaymentIn,
						docRTSRequest, docRTSResponse, docGetOrderList, strTransactionType);
			}

			updateDBWithWebserviceReponses(env, docPaymentIn, docRTSRequest,
					docRTSResponse);
		}

		if(!YFCObject.isVoid(strAuthID)) {
			docPaymentOut.getDocumentElement()
				.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, 
					strAuthID);
		}

		log
		.verbose("End of AcaddemyRTSWebserviceForPLCC.translateRTSResponse() method");
		return docPaymentOut;
	}

	/**
	 * This method is invoked for APPROVED scenarios and same is updated as UE
	 * output
	 * 
	 * @param docRTSResponse
	 * @return Document
	 * @throws Exception
	 */
	private Document prepareApprovedResp(Document docRTSResponse)
	throws Exception {
		log
		.verbose("Begin of AcaddemyRTSWebserviceForPLCC.prepareApprovedResp() method");

		SimpleDateFormat sdf = new SimpleDateFormat(
				AcademyConstants.STR_DATE_TIME_PATTERN_NEW);
		Calendar cal = Calendar.getInstance();
		String strDate = sdf.format(cal.getTime());
		String strPLCCAuthExpiryDays = YFSSystem
		.getProperty(AcademyConstants.PROP_PLCC_EXPIRY_DAYS);
		if (!YFCObject.isVoid(strPLCCAuthExpiryDays)) {
			cal.add(Calendar.DATE, Integer.parseInt(strPLCCAuthExpiryDays));
		}
		String strExpirationDate = sdf.format(cal.getTime());

		DecimalFormat df = new DecimalFormat("0.00");
		String strAmount = docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_AMOUNT).item(0).getTextContent();

		// Convert amount to double format
		strAmount = df.format(Double.parseDouble(strAmount) / 100);

		String strTranType = docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_TRAN_TYPE).item(0).getTextContent();

		// Update the amount value with negative if it is void or refund.
		if (!YFCObject.isVoid(strTranType)
				&& (AcademyConstants.RTS_REQ_TRAN_TYPE_VOID
						.equalsIgnoreCase(strTranType) || AcademyConstants.RTS_REQ_TRAN_TYPE_REFUND
						.equalsIgnoreCase(strTranType))) {
			strAmount = AcademyConstants.STR_HYPHEN + strAmount;
		}

		Document docPaymentOut = XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT);
		Element elePaymentOut = docPaymentOut.getDocumentElement();

		//Updated the auth time and AUTH Expiry Date
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_TIME, strDate);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_EXP_DATE,
				strExpirationDate);
		
		//Updating the AUTH Amount in UE output
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_AMOUNT, strAmount);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_AMT, strAmount);
		
		//Updating the Charge Type
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_TYPE, strTranType);
		
		
		elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE,
				AcademyConstants.STR_APPROVED);

		//Updating other webservice response attributes to be re-used later for re-consolidation
		if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_ACTION_CODE).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, docRTSResponse.getElementsByTagName(
					AcademyConstants.ATTR_RTS_ACTION_CODE).item(0)
					.getTextContent());
		}

		if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_OPTIONS).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_REQUEST_ID, docRTSResponse.getElementsByTagName(
					AcademyConstants.ATTR_RTS_OPTIONS).item(0)
					.getTextContent());
		}

		if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_AUTH_CODE).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_CODE, docRTSResponse.getElementsByTagName(
					AcademyConstants.ATTR_RTS_AUTH_CODE).item(0)
					.getTextContent());
		}
		
		//This attribute will be recieved for AUTH, SALE, Refund calls
		if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_RECEIPT_DISP).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, docRTSResponse.getElementsByTagName(
					AcademyConstants.ATTR_RTS_RECEIPT_DISP).item(0)
					.getTextContent());
		}
		
		//This attribute will be updated for Void and Capture calls
		if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_ADDITIONAL_MSG).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, docRTSResponse.getElementsByTagName(
					AcademyConstants.ATTR_RTS_ADDITIONAL_MSG).item(0)
					.getTextContent());
		}
		
		if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_IS_VOID).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_CODE,
					docRTSResponse.getElementsByTagName(
							AcademyConstants.ATTR_RTS_IS_VOID).item(0)
							.getTextContent());	
		}
		
		if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_ISO_RESP).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE, docRTSResponse.getElementsByTagName(
					AcademyConstants.ATTR_RTS_ISO_RESP).item(0)
					.getTextContent());
		}
		
		if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_DEBIT_COM_LINK).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID,
					docRTSResponse.getElementsByTagName(
							AcademyConstants.ATTR_RTS_DEBIT_COM_LINK).item(0)
							.getTextContent());	
		}

		//Updating the IxNeedsReversal attribute for TranReturnMessages
		if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_NEEDS_REVERSAL).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_MESSAGE,
					docRTSResponse.getElementsByTagName(
							AcademyConstants.ATTR_RTS_NEEDS_REVERSAL).item(0)
							.getTextContent());	
		}
		
		log
		.verbose("AcaddemyRTSWebserviceForPLCC.prepareApprovedResp() Payment Output XML:: "
				+ XMLUtil.getXMLString(docPaymentOut));
		log
		.verbose("End of AcaddemyRTSWebserviceForPLCC.prepareApprovedResp() method");

		return docPaymentOut;
	}


	/**
	 * This methods returns the number of transactions occured on an order
	 * 
	 * @param env
	 * @param strOrderHeaderKey
	 * @param strChargeTransactionKey
	 * @return
	 * @throws Exception
	 */
	private int getCountOfPaymentTransactionError(Document docGetOrderList, 
			String strChargeTransactionKey, String strMessageType) throws Exception {
		log
		.verbose("Begin of AcademyRTSWebServiceForPLCC.getCountOfPaymentTransactionError() method");
		int iErrorCount = 0;

		List lstPaymentTransactionError = XMLUtil
		.getElementListByXpath(
				docGetOrderList,
				"/OrderList/Order/ChargeTransactionDetails/ChargeTransactionDetail[@ChargeTransactionKey='"
				+ strChargeTransactionKey
				+ "']/PaymentTransactionErrorList/PaymentTransactionError["
				+ "@MessageType='" + strMessageType + "']");
		iErrorCount = lstPaymentTransactionError.size();

		log.debug("iErrorCount:: " + iErrorCount);
		log
		.verbose("End of AcademyRTSWebServiceForPLCC.getCountOfPaymentTransactionError() method");
		return iErrorCount;
	}

	/**
	 * This method validates if a transaction is eligible for Retry or not
	 * 
	 * @param strTransactionType
	 * @param iErrorCount
	 * @return
	 * @throws Exception
	 */
	private boolean validateRetryLimit(String strTransactionType, int iErrorCount)
		throws Exception {
		log.verbose("Begin of AcademyRTSWebServiceForPLCC.validateRetryLimit() method");
		log.verbose(" :: strTransactionType :: " + strTransactionType);
		String strMaxRetryLimit = getMaxRetryLimt(strTransactionType);
		log.verbose(" :: strMaxRetryLimit :: " + strMaxRetryLimit);
		
		if (YFCObject.isVoid(strMaxRetryLimit)) {
			strMaxRetryLimit = AcademyConstants.STR_ZERO;
		}

		// If Retry count is less than max limit
		if (iErrorCount < Integer.parseInt(strMaxRetryLimit)) {
			return true;
		}
		log.verbose("End of AcademyRTSWebServiceForPLCC.validateRetryLimit() method");
		return false;
	}

	
	/**
	 * This method prepares the response when web service is not available
	 * 
	 * @param env
	 * @param docPaymentIn
	 * @param docRTSResponse
	 * @param docRTSRequest
	 * @param strTransactionType
	 * @param strChargeType
	 * @param iErrorCount
	 * @return
	 * @throws Exception
	 */
	private Document prepareServiceUnavailableResp(Document docPaymentIn, 
			Document docRTSResponse, Document docRTSRequest, 
			String strTransactionType, int iErrorCount)
	throws Exception {

		log
		.verbose("Begin of AcademyRTSWebServiceForPLCC.prepareServiceUnavailableResp() method");
		Document docPaymentOut = XMLUtil
		.createDocument(AcademyConstants.ELE_PAYMENT);
		Element elePaymentOut = docPaymentOut.getDocumentElement();
		log.verbose(" :: strTransactionType :: " + strTransactionType);
		
		boolean bIsRetry = validateRetryLimit(strTransactionType, iErrorCount);
		log.verbose(" :: bIsRetry :: " + bIsRetry);
		
		if(bIsRetry) {
			String strRetryInterval = getMaxRetryInterval(strTransactionType);
			log.verbose(" :: strRetryInterval :: " + strRetryInterval);
			
			elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE,
					AcademyConstants.STR_SERVICE_UNAVAILABLE);
			elePaymentOut.setAttribute(AcademyConstants.ATTR_RETRY_FLAG,
					AcademyConstants.STR_YES);
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_CODE,
					AcademyConstants.STR_SERVICE_UNAVAILABLE);

			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE,
					docRTSResponse.getDocumentElement().getAttribute(
							AcademyConstants.ATTR_HTTP_STATUS_CODE));
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG,
					docRTSResponse.getDocumentElement().getAttribute(
							AcademyConstants.ATTR_EXCEPTION_TYPE));

			elePaymentOut.setAttribute(AcademyConstants.ATTR_COLLECTION_DATE,
					nextCollectionDate(strRetryInterval));

			// Updating error message in the Payment Error List
			Element elePaymentErrorList = docPaymentOut
			.createElement(AcademyConstants.ELE_PAYMENT_TRAN_ERROR_LIST);
			elePaymentOut.appendChild(elePaymentErrorList);

			Element elePaymentError = docPaymentOut
			.createElement(AcademyConstants.ELE_PAYMENT_TRAN_ERROR);
			elePaymentErrorList.appendChild(elePaymentError);

			if (AcademyConstants.STR_AUTH_FAILURE.equals(strTransactionType)) {
				// Transaction type is AUTH Failure. Update same Error message
				elePaymentError.setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE,
						AcademyConstants.STR_AUTH_FAILURE);
			}
			else {
				elePaymentError.setAttribute(AcademyConstants.ATTR_MESSAGE_TYPE,
						AcademyConstants.STR_SERVICE_UNAVAILABLE);
			}

			elePaymentError.setAttribute(AcademyConstants.ATTR_MESSAGE,
					docRTSRequest.getElementsByTagName(
							AcademyConstants.ATTR_RTS_OPTIONS).item(0)
							.getTextContent());
		}
		else {
			elePaymentOut = validateFailureAndPrepareResponse(docPaymentIn, docRTSResponse, 
					elePaymentOut, strTransactionType);
		}
		//Updating the Charge Type
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_TYPE, 
				docPaymentIn.getDocumentElement().getAttribute(AcademyConstants.ATTR_CHARGE_TYPE));

		log
		.verbose(" :: docPaymentOut :: " + XMLUtil.getXMLString(docPaymentOut));
		
		log
		.verbose("End of AcademyRTSWebServiceForPLCC.prepareServiceUnavailableResp() method");
		return docPaymentOut;
	}

	
	
	/**
	 * This method returns Date in which the transaction is to be processed
	 * again
	 * 
	 * @param strRetryInterval
	 * @return
	 */
	private String nextCollectionDate(String strRetryInterval) {
		log
		.verbose("Begin of AcademyRTSWebServiceForPLCC.nextCollectionDate() method ::");
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(
				AcademyConstants.STR_DATE_TIME_PATTERN_1);
		strRetryInterval = !YFCObject.isVoid(strRetryInterval) ? strRetryInterval
				: AcademyConstants.RTS_NEXT_TRIGER_TIME;
		cal.add(Calendar.MINUTE, Integer.parseInt(strRetryInterval));
		String strCollectionDate = sdf.format(cal.getTime());
		log
		.verbose("End of AcademyRTSWebServiceForPLCC.nextCollectionDate() method ::");
		return strCollectionDate;
	}

	/**
	 * This method validates the timeout reversal
	 * 
	 * @param env
	 * @param docPaymentIn
	 * @param strTransactionType
	 * @param strChargeType
	 * @throws Exception
	 */
	private void validateTimeoutReversal(YFSEnvironment env,
			Document docPaymentIn, Document docGetOrderList, String strTransactionType) throws Exception {
		log
		.verbose("Begin of AcademyRTSWebServiceForPLCC.validateTimeoutReversal() method");

		String strChargeTransactionKey = XPathUtil.getString(docPaymentIn,
				AcademyConstants.STR_PAYMENT_PATH_AT
				+ AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY);
		String strIxOptions = getReversalIdOfTimeOutIfExists(docGetOrderList, 
				strChargeTransactionKey, AcademyConstants.STR_SERVICE_UNAVAILABLE);

		if (!YFCObject.isVoid(strIxOptions)) {
			log.verbose(" Valid for Timeout reversal with ID " + strIxOptions);
			strIxOptions = strIxOptions.replace(
					AcademyConstants.RTS_OPTIONS_SALE,
					AcademyConstants.RTS_OPTIONS_REVERSAL);

			Document docReversalInp = getRTSHTTPRequestDoc(env, docPaymentIn,
					docGetOrderList, strTransactionType, strIxOptions);
			if (!YFCObject.isVoid(docReversalInp)) {
				log.verbose("****Request to RTS from validateTimeoutReversal :: "
						+ XMLUtil.getXMLString(docReversalInp));
				Document docReversalResp = sendHTTP(docReversalInp);
								log.verbose("****Response from RTS from validateTimeoutReversal :: "
										+ XMLUtil.getXMLString(docReversalResp));
			}

		}

	}

	/**
	 *  This method retrieves the timeout reversal ID for an order if it exists
	 * 
	 * @param strOrderHeaderKey
	 * @param strChargeTransactionKey
	 * @param strMessageType
	 * @return
	 * @throws Exception
	 */
	private String getReversalIdOfTimeOutIfExists(Document docGetOrderList, 
			String strChargeTransactionKey, String strMessageType) throws Exception {
		log
		.verbose("Begin of AcademyRTSWebServiceForPLCC.getReversalIdOfTimeOutIfExists() method");
		String strReversalId = null;

		List lstPaymentTransactionError = XMLUtil
		.getElementListByXpath(
				docGetOrderList,
				"/OrderList/Order/ChargeTransactionDetails/ChargeTransactionDetail[@ChargeTransactionKey='"
				+ strChargeTransactionKey
				+ "']/PaymentTransactionErrorList/PaymentTransactionError["
				+ "@MessageType='" + strMessageType + "']");
		if (!YFCObject.isVoid(lstPaymentTransactionError)
				&& lstPaymentTransactionError.size() > 0) {
			for (int iPErr = 0; iPErr < lstPaymentTransactionError.size(); iPErr++) {
				Element elePaymentTransactionError = (Element) lstPaymentTransactionError
				.get(iPErr);
				strReversalId = elePaymentTransactionError
				.getAttribute(AcademyConstants.ATTR_MESSAGE);
			}
		}

		log.debug("strReversalId:: " + strReversalId);
		log
		.verbose("End of AcademyRTSWebServiceForPLCC.getReversalIdOfTimeOutIfExists() method");
		return strReversalId;
	}

	/**
	 * @param env
	 * @param strOrderHeaderKey
	 * @return
	 * @throws Exception
	 */
	private static Document getOrderList(YFSEnvironment env,
			String strOrderHeaderKey) throws Exception { // Adddd desc

		log
		.verbose("Begin of AcademyRTSWebServiceForPLCC.getOrderList() method");

		Document docGetOrderListInp = XMLUtil
		.createDocument(AcademyConstants.ELE_ORDER);
		docGetOrderListInp.getDocumentElement().setAttribute(
				AcademyConstants.STR_ORDR_HDR_KEY, strOrderHeaderKey);

		Document docGetOrderListOut = AcademyUtil.invokeService(env,
				AcademyConstants.SERV_ACAD_GET_ORDER_LIST_FOR_PAYMENT_SERVICE,
				docGetOrderListInp);
		log.verbose("End of AcademyRTSWebServiceForPLCC.getOrderList() method");
		return docGetOrderListOut;
	}

	/**
	 * This method prepares the UE output when a transaction is declined even
	 * after requured number of retries
	 * 
	 * @param env
	 * @param docPaymentIn
	 * @param docRTSRequest
	 * @param docRTSResponse
	 * @param strTransactionType
	 * @param strChargeType
	 * @return
	 * @throws Exception
	 */
	private Document prepareDeclinedResponse(YFSEnvironment env, Document docPaymentIn, 
			Document docRTSRequest, Document docRTSResponse, Document docGetOrderList, 
			String strTransactionType) throws Exception {

		log
		.verbose("Begin of AcademyRTSWebServiceForPLCC.prepareDeclinedResponse() method");
		Document docPaymentOut = XMLUtil
		.createDocument(AcademyConstants.ELE_PAYMENT);
		Element elePaymentOut = docPaymentOut.getDocumentElement();
		String strChargeType = XPathUtil.getString(docPaymentIn,
				AcademyConstants.STR_PAYMENT_PATH_AT
				+ AcademyConstants.ATTR_CHARGE_TYPE);
		String strCancelDeclinedAuth = props
		.getProperty(AcademyConstants.STR_CANCEL_DECLINED_AUTH);
		String strCancelDeclinedCharge = props
		.getProperty(AcademyConstants.STR_CANCEL_DECLINED_CHARGE);
		
		if (!YFCObject.isVoid(strTransactionType)
				&& AcademyConstants.RTS_REQ_TRAN_TYPE_PRE_AUTH
				.equals(strTransactionType)) {
			log.verbose(" AUTH call is declined, invoke a void call ");

			//Validate if the void call was already done or not and then retry or decline
			docPaymentOut = validateAuthFailureAndRetry(env, docPaymentIn, docRTSRequest, 
					docRTSResponse, docGetOrderList, docPaymentOut);

		}
		else {
			elePaymentOut = validateFailureAndPrepareResponse(docPaymentIn, docRTSResponse, 
					elePaymentOut, strTransactionType);
		}
		
		//Updating the Charge Type
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_TYPE, 
				docPaymentIn.getDocumentElement().getAttribute(AcademyConstants.ATTR_CHARGE_TYPE));

		// Prepare taskQ record for hard declined cancellations
		log.verbose(" strCancelDeclinedAuth :: " + strCancelDeclinedAuth
				+ " strCancelDeclinedCharge :: " + strCancelDeclinedCharge);

		//Start : PLCC-510 : Ignore Order Cancel in case of a void decline case
		if ((!YFCObject.isVoid(strCancelDeclinedAuth)
				&& strCancelDeclinedAuth.equals(AcademyConstants.STR_YES)
				&& strChargeType.equals(AcademyConstants.STR_CHRG_TYPE_AUTH)
				&& strTransactionType.equals(AcademyConstants.RTS_REQ_TRAN_TYPE_PRE_AUTH))
				|| (!YFCObject.isVoid(strCancelDeclinedCharge)
								&& strCancelDeclinedCharge.equals(AcademyConstants.STR_YES)
								&& strChargeType.equals(AcademyConstants.STR_CHRG_TYPE_CHARGE))){
			log.verbose(" Creating task q record for failures ");
			try {
				createManageTaskQueue(env, docPaymentIn);
			} catch (IOException e) {
				log.info("RTS PLCC Exception:: Error Processing Task Q for Order "+
						docPaymentIn.getDocumentElement().getAttribute(
								AcademyConstants.ATTR_ORDER_HEADER_KEY));
			}
		}
		//End : PLCC-510 : Ignore Order Cancel in case of a void decline case
		
		log
		.verbose("Output of AcademyRTSWebServiceForPLCC.prepareDeclinedResponse()::"
				+ docPaymentOut);
		log
		.verbose("End of AcademyRTSWebServiceForPLCC.prepareDeclinedResponse() method");
		return docPaymentOut;

	}

	/**
	 * This method validaets if transaction is eligible for dummy 
	 * response or a declined response
	 * 
	 * @param docPaymentIn
	 * @param docRTSResponse
	 * @param elePaymentOut
	 * @param strTransactionType
	 * @return
	 * @throws Exception
	 */
	private Element validateFailureAndPrepareResponse(Document docPaymentIn,
			Document docRTSResponse, Element elePaymentOut, 
			String strTransactionType) throws Exception {

		log
		.verbose("Begin of AcademyInvokePayeezyRestWebservice.validateFailureAndPrepareResponse() method");
		//If void call is declined, Ignore and update as a dummy response
		if (!YFCObject.isVoid(strTransactionType)
				&& AcademyConstants.RTS_REQ_TRAN_TYPE_VOID
				.equals(strTransactionType)) {
			log.verbose(" Ignore and update dummy response for VOID ");
			elePaymentOut = prepareDummyResponse(docPaymentIn, docRTSResponse,
					elePaymentOut, AcademyConstants.STR_DUMMY_AUTH);
		}
		//If CAPTURE or PURCHASE or REFUND calls fail, update it as a dummy
		// settlement and raise alert
		else if (!YFCObject.isVoid(strTransactionType)
				&& (AcademyConstants.RTS_REQ_TRAN_TYPE_PRE_AUTH_COMP
						.equals(strTransactionType)
						|| AcademyConstants.RTS_REQ_TRAN_TYPE_SALE
						.equals(strTransactionType) 
						|| AcademyConstants.RTS_REQ_TRAN_TYPE_REFUND
						.equals(strTransactionType))) {
			log
			.verbose(" CAPTURE or PURCHASE or REFUND calls fail, update it as a dummy settlement and raise alert ");
			elePaymentOut = prepareDummyResponse(docPaymentIn, docRTSResponse,
					elePaymentOut, AcademyConstants.STR_DUMMY_SETTLEMENT);
		}
		else if (!YFCObject.isVoid(strTransactionType)
				&& (AcademyConstants.RTS_REQ_TRAN_TYPE_PRE_AUTH).equals(strTransactionType)) {
			log
			.verbose(" AUTH failure. Update as HARD_DECLINED and raise alert ");
			elePaymentOut = prepareHardDeclinedResponse(docRTSResponse, elePaymentOut, 
					AcademyConstants.STR_HARD_DECLINED);
		}
		else {
			log
			.verbose("Not a valid TransactionType. Update declined response");
			elePaymentOut = prepareHardDeclinedResponse(docRTSResponse, elePaymentOut, 
					"INVALID_TRANSACTION");
		}

		log
		.verbose("End of AcademyInvokePayeezyRestWebservice.validateFailureAndPrepareResponse method");

		return elePaymentOut;
	}

	
	/**
	 * This method prepare the declined response
	 * 
	 * @param docRTSResponse
	 * @param elePaymentOut
	 * @param strMessage
	 * @return
	 * @throws Exception
	 */
	private Element prepareHardDeclinedResponse(Document docRTSResponse,
			Element elePaymentOut, String strMessage) throws Exception {

		log
		.verbose("Begin of AcademyInvokePayeezyRestWebservice.prepareHardDeclinedResponse() method");
		elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, strMessage);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_NO);
		
		//Updating other webservice response attributes to be re-used later for re-consolidation
		if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_ACTION_CODE).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, docRTSResponse.getElementsByTagName(
					AcademyConstants.ATTR_RTS_ACTION_CODE).item(0).getTextContent());
		}
		else {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, 
					strMessage);
		}

		if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_OPTIONS).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_REQUEST_ID, docRTSResponse.getElementsByTagName(
					AcademyConstants.ATTR_RTS_OPTIONS).item(0).getTextContent());
		}

		if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_AUTH_CODE).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_CODE, docRTSResponse.getElementsByTagName(
					AcademyConstants.ATTR_RTS_AUTH_CODE).item(0).getTextContent());
		} else {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_CODE, 
					strMessage);
		}
		
		//This attribute will be recieved for AUTH, SALE, Refund calls
		if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_RECEIPT_DISP).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, docRTSResponse.getElementsByTagName(
					AcademyConstants.ATTR_RTS_RECEIPT_DISP).item(0).getTextContent());
		} 
		//This attribute will be updated for Void and Capture calls
		else if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_ADDITIONAL_MSG).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, docRTSResponse.getElementsByTagName(
					AcademyConstants.ATTR_RTS_ADDITIONAL_MSG).item(0).getTextContent());
		}else {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, 
					strMessage);
		}
		
		if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_IS_VOID).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_CODE, docRTSResponse.getElementsByTagName(
							AcademyConstants.ATTR_RTS_IS_VOID).item(0).getTextContent());	
		}
		
		if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_ISO_RESP).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE, docRTSResponse.getElementsByTagName(
					AcademyConstants.ATTR_RTS_ISO_RESP).item(0).getTextContent());
		}else {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE, 
					strMessage);
		}
		
		if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_DEBIT_COM_LINK).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, docRTSResponse.getElementsByTagName(
							AcademyConstants.ATTR_RTS_DEBIT_COM_LINK).item(0).getTextContent());	
		}else {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, 
					strMessage);
		}

		//Updating the IxNeedsReversal attribute for TranReturnMessages
		if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_NEEDS_REVERSAL).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_MESSAGE, docRTSResponse.getElementsByTagName(
							AcademyConstants.ATTR_RTS_NEEDS_REVERSAL).item(0).getTextContent());	
		}else {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_MESSAGE, 
					strMessage);
		}
		
		log
		.verbose("End of AcademyInvokePayeezyRestWebservice.prepareHardDeclinedResponse method");

		return elePaymentOut;
	}
	
	/**
	 * This method prepare a dummy settlement response to be used when a
	 * transaction is declined
	 * 
	 * @param docPaymentIn
	 * @param docRTSRequest
	 * @param docRTSResponse
	 * @param elePaymentOut
	 * @return
	 * @throws Exception
	 */
	private Element prepareDummyResponse(Document docPaymentIn,
			Document docRTSResponse, Element elePaymentOut, 
			String strMessage) throws Exception {

		log
		.verbose("Begin of AcademyRTSWebserviceForPLCC.prepareDummyResponse() method");
		String strReqAmt = XPathUtil.getString(docPaymentIn,
				AcademyConstants.STR_PAYMENT_PATH_AT
				+ AcademyConstants.ATTR_REQUEST_AMOUNT);
		// String strExceptionType =
		// docRTSResponse.getDocumentElement().getAttribute(AcademyConstants.ATTR_EXCEPTION_TYPE);

		// Dummy SETTLEMENT as transaction had been declined
		elePaymentOut.setAttribute(AcademyConstants.ATTR_IS_DUMMY_SETTLEMENT,
				AcademyConstants.STR_YES);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_STATUS_CODE,
				AcademyConstants.STR_NO);// To raise alert using existing
		// service
		
		if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_ACTION_CODE).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, docRTSResponse.getElementsByTagName(
					AcademyConstants.ATTR_RTS_ACTION_CODE).item(0).getTextContent());
		}
		else {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE,
					"DUMMY");
		}

		if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_OPTIONS).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_REQUEST_ID, docRTSResponse.getElementsByTagName(
					AcademyConstants.ATTR_RTS_OPTIONS).item(0).getTextContent());
		}
		else {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_REQUEST_ID, strMessage);
		}

		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_CODE, strMessage);
		/*if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_AUTH_CODE).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_CODE, docRTSResponse.getElementsByTagName(
					AcademyConstants.ATTR_RTS_AUTH_CODE).item(0).getTextContent());
		} else {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_CODE, 
					AcademyConstants.STR_HARD_DECLINED);
		}*/
		
		//This attribute will be recieved for AUTH, SALE, Refund calls
		if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_RECEIPT_DISP).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, docRTSResponse.getElementsByTagName(
					AcademyConstants.ATTR_RTS_RECEIPT_DISP).item(0).getTextContent());
		} 
		//This attribute will be updated for Void and Capture calls
		else if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_ADDITIONAL_MSG).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG, docRTSResponse.getElementsByTagName(
					AcademyConstants.ATTR_RTS_ADDITIONAL_MSG).item(0).getTextContent());
		}
		else {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_MSG,
			"DUMMY");
		}
		
		if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_IS_VOID).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_INTERNAL_RETURN_CODE, docRTSResponse.getElementsByTagName(
							AcademyConstants.ATTR_RTS_IS_VOID).item(0).getTextContent());	
		}
		else {
			elePaymentOut.setAttribute(
					AcademyConstants.ATTR_INTERNAL_RETURN_CODE, "DUMMY");
		}
		
		if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_ISO_RESP).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE, docRTSResponse.getElementsByTagName(
					AcademyConstants.ATTR_RTS_ISO_RESP).item(0).getTextContent());
		}
		else {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE,
			"DUMMY");
		}
		
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, strMessage);
		/*if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_DEBIT_COM_LINK).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, docRTSResponse.getElementsByTagName(
							AcademyConstants.ATTR_RTS_DEBIT_COM_LINK).item(0).getTextContent());	
		}else {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, 
					AcademyConstants.STR_HARD_DECLINED);
		}*/
		

		//Updating the IxNeedsReversal attribute for TranReturnMessages
		if(!YFCObject.isVoid(docRTSResponse.getElementsByTagName(
				AcademyConstants.ATTR_RTS_NEEDS_REVERSAL).item(0))) {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_MESSAGE, docRTSResponse.getElementsByTagName(
							AcademyConstants.ATTR_RTS_NEEDS_REVERSAL).item(0).getTextContent());	
		}else {
			elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, 
					AcademyConstants.STR_HARD_DECLINED);
		}
		
		elePaymentOut.setAttribute(AcademyConstants.ATTR_TRAN_AMT, strReqAmt);
		elePaymentOut
		.setAttribute(AcademyConstants.ATTR_AUTH_AMOUNT, strReqAmt);

		elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE,
				AcademyConstants.STR_APPROVED);		

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(
				AcademyConstants.STR_DATE_TIME_PATTERN_NEW);
		elePaymentOut.setAttribute(AcademyConstants.ATTR_AUTH_TIME, sdf
				.format(cal.getTime()));

		// Setting order details to raise alert
		elePaymentOut.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY, XPathUtil
				.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT
						+ AcademyConstants.ATTR_ORDER_HEADER_KEY));
		elePaymentOut.setAttribute(AcademyConstants.ATTR_ORDER_NO, XPathUtil
				.getString(docPaymentIn, AcademyConstants.STR_PAYMENT_PATH_AT
						+ AcademyConstants.ATTR_ORDER_NO));
		elePaymentOut.setAttribute(AcademyConstants.ATTR_PAYMENT_TYPE,
				XPathUtil.getString(docPaymentIn,
						AcademyConstants.STR_PAYMENT_PATH
						+ AcademyConstants.PAYMENT_TYPE_XPATH));

		// Below details will be shown in Alert console
		elePaymentOut.setAttribute(AcademyConstants.ATTR_REASON_CODE, "");
		elePaymentOut.setAttribute(AcademyConstants.ATTR_REASON_DESC, "");
		elePaymentOut.setAttribute(AcademyConstants.ATTR_ERROR_NO, "");
		elePaymentOut.setAttribute(AcademyConstants.ATTR_ERROR_DESC_SHORT, "");

		log
		.verbose("Dummy response prepared in prepareDummyResponse() method ::"
				+ XMLUtil
				.getXMLString(elePaymentOut.getOwnerDocument()));
		log
		.verbose("End of AcademyInvokePayeezyRestWebservice.prepareDummyResponse() method");

		return elePaymentOut;
	}

	/**
	 * This method retry the AUTH after making the void call
	 * 
	 * @param env
	 * @param docPaymentIn
	 * @param strTransactionType
	 * @param strChargeType
	 * @throws Exception
	 */
	private void invokeVoidCallAndRetry(YFSEnvironment env, Document docPaymentInput, 
			Document docGetOrderList, String strTransactionType) throws Exception {
		log
		.verbose("Begin of AcademyRTSWebServiceForPLCC.invokeVoidCallAndRetry() method");

		// Retrieve the previous Autorization ID
		String strAuthId = getPreviousAuthID(docGetOrderList);

		if (!YFCObject.isVoid(strAuthId)) {
			strTransactionType = AcademyConstants.RTS_REQ_TRAN_TYPE_VOID;
			
			docPaymentInput.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_AUTHORIZATION_ID, strAuthId);

			Document docVoidReq = getRTSHTTPRequestDoc(env, docPaymentInput,
					docGetOrderList, strTransactionType, null);
			if (!YFCObject.isVoid(docVoidReq)) {
				log.verbose("****Request to RTS  from invokeVoidCallAndRetry():: "
						+ XMLUtil.getXMLString(docVoidReq));

				Document docVoidResponse = sendHTTP(docVoidReq);

				log.verbose("****Response from RTS invokeVoidCallAndRetry():: "
						+ XMLUtil.getXMLString(docVoidResponse));
			}

		} else {
			log.verbose(" Invalid Scenario where void is done before AUTH");
		}

		log
		.verbose("End of AcademyRTSWebServiceForPLCC.invokeVoidCallAndRetry() method");
	}

	/**
	 * This method fetches the authorization id of previous transaction
	 * 
	 * @param env
	 * @param strOrderHeaderKey
	 * @return
	 * @throws Exception
	 */
	private String getPreviousAuthID(Document docGetOrderList) throws Exception {
		log
		.verbose("Begin of AcademyRTSWebServiceForPLCC.getPreviousAuthID() method");
		String strAuthorizationId = null;

		List lChargeTranDetails = XMLUtil.getElementListByXpath(docGetOrderList,
				"OrderList/Order/ChargeTransactionDetails/ChargeTransactionDetail" +
				"[@ChargeType='AUTHORIZATION' and @RequestAmount > 0]");

		if (!YFCObject.isVoid(lChargeTranDetails)
				&& lChargeTranDetails.size() > 0) {
			for (int iCTDls = 0; iCTDls < lChargeTranDetails.size(); iCTDls++) {

				Element eleChargeTransactionDetail = (Element) lChargeTranDetails
				.get(iCTDls);
				strAuthorizationId = eleChargeTransactionDetail
				.getAttribute(AcademyConstants.ATTR_TRAN_RETURN_MESSAGE);
			}
		}

		log.debug("strAuthorizationId:: " + strAuthorizationId);
		log
		.verbose("End of AcademyRTSWebServiceForPLCC.getPreviousAuthID() method");
		return strAuthorizationId;
	}

	/**
	 * This method prepares and updates YFS_EXPORT tablewith webservice errors
	 * 
	 * @param env
	 * @param docPaymentIn
	 * @param docRTSRequest
	 * @param docRTSResponse
	 * @param strChargeType
	 * @throws Exception
	 */

	private void updateDBWithWebserviceReponses(YFSEnvironment env,Document docPaymentIn, 
			Document docRTSRequest,Document docRTSResponse) throws Exception {

		String strChargeType = XPathUtil.getString(docPaymentIn,AcademyConstants.STR_PAYMENT_PATH_AT+ AcademyConstants.ATTR_CHARGE_TYPE);
		log.verbose("Begin of AcademyRTSWebServiceForPLCC.updateDBWithWebserviceReponses() method");
		String strIsDBLoggingEnabled = AcademyConstants.STR_NO;

		if(AcademyConstants.STR_CHRG_TYPE_AUTH.equals(strChargeType)){

			strIsDBLoggingEnabled = props.getProperty(AcademyConstants.STR_DB_LOGGING_FOR_AUTH);
		}
		if(AcademyConstants.STR_CHRG_TYPE_CHARGE.equals(strChargeType)){

			strIsDBLoggingEnabled = props.getProperty(AcademyConstants.STR_DB_LOGGING_FOR_CHARGE);

		}
		if(strIsDBLoggingEnabled.equals(AcademyConstants.STR_YES)){
			log.verbose("Logging request and response in DB");
			AcademyPaymentProcessingUtil.logPayZReqAndRespToDB(env,
					docPaymentIn, docRTSRequest, docRTSResponse,
					AcademyConstants.STR_DB_SERVICE_FOR_PAYZ);
		}

		log
		.verbose("End of AcademyRTSWebServiceForPLCC.updateDBWithWebserviceReponses() method");
	}



	/**
	 * This method prepares and updates TASK_Q table with orders which need to
	 * be cancelled
	 * 
	 * @param env
	 * @param docPaymentInp
	 * @throws Exception
	 */
	private void createManageTaskQueue(YFSEnvironment env,
			Document docPaymentInp) throws Exception {

		log
		.verbose("Begin of AcademyRTSWebServiceForPLCC.createManageTaskQueue() method");
		Document docTaskQueue = XMLUtil
		.createDocument(AcademyConstants.ELE_TASK_QUEUE);
		Element eleTaskQueue = docTaskQueue.getDocumentElement();

		eleTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_KEY, docPaymentInp
				.getDocumentElement().getAttribute(
						AcademyConstants.ATTR_ORDER_HEADER_KEY));

		eleTaskQueue.setAttribute(AcademyConstants.ATTR_DATA_TYPE,
				AcademyConstants.ATTR_ORDER_HEADER_KEY);
		eleTaskQueue.setAttribute(AcademyConstants.ATTR_TRANSID,
				AcademyConstants.TRANSACTION_DECLINED_ORDER_AGENT);

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(
				AcademyConstants.STR_DATE_TIME_PATTERN);
		eleTaskQueue.setAttribute(AcademyConstants.ATTR_AVAIL_DATE, sdf
				.format(cal.getTime()));

		log.verbose("Input to manageTaskQueue :: \n"
				+ XMLUtil.getXMLString(docTaskQueue));
		AcademyUtil.invokeAPI(env, "manageTaskQueue", docTaskQueue);
		log
		.verbose("End of AcademyRTSWebServiceForPLCC.createManageTaskQueue() method");
	}
	
	
	/**
	 * This method fetches the authorization id of previous transaction
	 * 
	 * @param env
	 * @param strOrderHeaderKey
	 * @return
	 * @throws Exception
	 */
	private String getTranMsgForAuth(Document docGetOrderList, String strAuthId) throws Exception {
		log
		.verbose("Begin of AcademyRTSWebServiceForPLCC.getTranMsgForAuth() method");
		String strAuthorizationId = null;

		List lCreditCardTransation = XMLUtil.getElementListByXpath(docGetOrderList,
				"/OrderList/Order/ChargeTransactionDetails/ChargeTransactionDetail" +
				"[@ChargeType='AUTHORIZATION' and @AuthorizationID='" +
				 strAuthId + "' and @RequestAmount > 0]/CreditCardTransactions/CreditCardTransaction");

		log.debug("lCreditCardTransation size :: " + lCreditCardTransation.size());
		
		if (!YFCObject.isVoid(lCreditCardTransation)
				&& lCreditCardTransation.size() > 0) {
			for (int iCTDls = 0; iCTDls < lCreditCardTransation.size(); iCTDls++) {

				Element eleCreditCardTransaction = (Element) lCreditCardTransation
				.get(iCTDls);
				strAuthorizationId = eleCreditCardTransaction
				.getAttribute(AcademyConstants.ATTR_TRAN_RETURN_MESSAGE);
			}
		}

		log.debug("IxNeedsReversal in TranMessage is :: " + strAuthorizationId);
		log
		.verbose("End of AcademyRTSWebServiceForPLCC.getTranMsgForAuth() method");
		return strAuthorizationId;
	}
	
	
	/**
	 * This method fetches the max retry limit based on transaction Type
	 * 
	 * @param strTransactionType
	 * @return
	 * @throws Exception
	 */
	private String getMaxRetryLimt(String strTransactionType) throws Exception {
		log.verbose("Begin of AcademyRTSWebServiceForPLCC.getMaxRetryLimt() method");
		
		String strMaxRetryLimit = null;
		if (AcademyConstants.RTS_REQ_TRAN_TYPE_PRE_AUTH_COMP
				.equals(strTransactionType)
				|| AcademyConstants.RTS_REQ_TRAN_TYPE_SALE
				.equals(strTransactionType)) {
			// Transaction type is capture/settle. Retrieve corresponding retry limit
			strMaxRetryLimit = YFSSystem
			.getProperty(AcademyConstants.PROP_PLCC_SETTLE_TIMEOUT_LIMIT);
		}
		else if (AcademyConstants.RTS_REQ_TRAN_TYPE_VOID
				.equals(strTransactionType)) {
			// Transaction type is Void. Retrieve corresponding retry limit 
			strMaxRetryLimit = YFSSystem
			.getProperty(AcademyConstants.PROP_PLCC_VOID_TIMEOUT_LIMIT);
		} 
		else if (AcademyConstants.RTS_REQ_TRAN_TYPE_PRE_AUTH
				.equals(strTransactionType)) {
			// Transaction type is RE-AUTH. Retrieve corresponding retry limit
			strMaxRetryLimit = YFSSystem
			.getProperty(AcademyConstants.PROP_PLCC_AUTH_TIMEOUT_LIMIT);
			
		} else if (AcademyConstants.RTS_REQ_TRAN_TYPE_REFUND
				.equals(strTransactionType)) {
			// Transaction type is Refund. Retrieve corresponding retry limit
			strMaxRetryLimit = YFSSystem
			.getProperty(AcademyConstants.PROP_PLCC_REFUND_TIMEOUT_LIMIT);
		} else if (AcademyConstants.STR_AUTH_FAILURE.equals(strTransactionType)) {
			// Transaction type is AUTH Failure. Retrieve corresponding retry limit
			strMaxRetryLimit = YFSSystem
			.getProperty(AcademyConstants.PROP_PLCC_AUTH_LIMIT);
		}
		log
		.verbose("End of AcademyRTSWebServiceForPLCC.getMaxRetryLimt() method");
		return strMaxRetryLimit;
	}
	
	/**
	 * This method fetches the max retry interval based on transaction Type
	 * 
	 * @param strTransactionType
	 * @return
	 * @throws Exception
	 */
	private String getMaxRetryInterval(String strTransactionType) throws Exception {
		log.verbose("Begin of AcademyRTSWebServiceForPLCC.getMaxRetryInterval() method");
		
		String strRetryInterval = null;
		if (AcademyConstants.RTS_REQ_TRAN_TYPE_PRE_AUTH_COMP
				.equals(strTransactionType)
				|| AcademyConstants.RTS_REQ_TRAN_TYPE_SALE
				.equals(strTransactionType)) {
			// Transaction type is capture/settle. Retrieve corresponding retry interval
			strRetryInterval = YFSSystem
			.getProperty(AcademyConstants.PROP_PLCC_SETTLE_TIMEOUT_INTERVAL);
		}
		else if (AcademyConstants.RTS_REQ_TRAN_TYPE_VOID
				.equals(strTransactionType)) {
			// Transaction type is Void. Retrieve corresponding retry interval
			strRetryInterval = YFSSystem
			.getProperty(AcademyConstants.PROP_PLCC_VOID_TIMEOUT_INTERVAL);

		} 
		else if (AcademyConstants.RTS_REQ_TRAN_TYPE_PRE_AUTH
				.equals(strTransactionType)) {
			// Transaction type is RE-AUTH. Retrieve corresponding retry interval
			strRetryInterval = YFSSystem
			.getProperty(AcademyConstants.PROP_PLCC_AUTH_TIMEOUT_INTERVAL);

		} else if (AcademyConstants.RTS_REQ_TRAN_TYPE_REFUND
				.equals(strTransactionType)) {
			// Transaction type is Refund. Retrieve corresponding retry interval
			strRetryInterval = YFSSystem
			.getProperty(AcademyConstants.PROP_PLCC_REFUND_TIMEOUT_INTERVAL);
		} else if (AcademyConstants.STR_AUTH_FAILURE.equals(strTransactionType)) {
			// Transaction type is AUTH Failure. Retrieve corresponding retry interval
			strRetryInterval = YFSSystem
			.getProperty(AcademyConstants.PROP_PLCC_AUTH_INTERVAL);
		}
		log
		.verbose("End of AcademyRTSWebServiceForPLCC.getMaxRetryInterval() method");
		return strRetryInterval;
	}
	
	
	/**
	 * This method fetches the max retry interval based on transaction Type
	 * 
	 * @param strTransactionType
	 * @return
	 * @throws Exception
	 */
	private Document validateAuthFailureAndRetry(YFSEnvironment env, Document docPaymentIn,
			Document docRTSRequest, Document docRTSResponse,
			Document docGetOrderList, Document docPaymentOut) throws Exception {
		log.verbose("Begin of AcademyRTSWebServiceForPLCC.validateAuthFailureAndRetry() method");
		String strChargeTransactionKey = XPathUtil.getString(docPaymentIn,
				AcademyConstants.STR_PAYMENT_PATH_AT+ AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY);

		log.verbose("ChargeTransactionKey ::"+strChargeTransactionKey);
		int iErrorCount = getCountOfPaymentTransactionError(docGetOrderList, 
				strChargeTransactionKey, AcademyConstants.STR_AUTH_FAILURE);
		
		boolean bIsRetry = validateRetryLimit(AcademyConstants.STR_AUTH_FAILURE, iErrorCount);
		log.verbose(":: validateAuthFailureAndRetry:: bIsRetry ::"+bIsRetry);
		if(bIsRetry) {
			invokeVoidCallAndRetry(env, docPaymentIn, docGetOrderList, AcademyConstants.RTS_REQ_TRAN_TYPE_VOID);
			docPaymentOut = prepareServiceUnavailableResp(docPaymentIn, docRTSResponse,
					docRTSRequest, AcademyConstants.STR_AUTH_FAILURE, iErrorCount);
		}
		else {
			Element elePaymentOut = docPaymentOut.getDocumentElement();
			elePaymentOut = prepareHardDeclinedResponse(docRTSResponse, 
					elePaymentOut, AcademyConstants.STR_HARD_DECLINED);	
		}
		
		log
		.verbose("End of AcademyRTSWebServiceForPLCC.validateAuthFailureAndRetry() method");
		return docPaymentOut;
	}
	
}
//End of AcaddemyRTSWebserviceForPLCC class
