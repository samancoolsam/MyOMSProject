package com.academy.ecommerce.sterling.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.shared.ycp.YFSContext;

/**
 * Added to get RTS request XMLs from various transactions.
 * @author C0007319
 * 		   C0008473
 *
 */

public class AcademyRTSUtil {
	
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyRTSUtil.class);
	static String  strRTSGCTimeOut = null;
	static String  strRTSCCTimeOut = null;
	private static Random rn = new Random();
	/**
	 * Based on charge type form RTS request XMLs.
	 * @param inDoc
	 * @param chargeType
	 * @return reqDoc- RTS request document
	 * @throws Exception 
	 */ 

	public static Document getRTSHTTPRequestDoc(YFSEnvironment env, Document inDoc, String chargeType) throws Exception {
		
		log.verbose("Inside getRTSHTTPRequestDoc() with input :: charge type::"+chargeType+"And inDoc ::"+XMLUtil.getXMLString(inDoc));
		Element inDocEle = inDoc.getDocumentElement();		
		String strPaymentType = inDocEle.getAttribute(AcademyConstants.ATTR_PAYMENT_TYPE);
		
		Document reqDoc = setRTSCommonRequestAttributes(env,inDocEle,strPaymentType,chargeType);
		
		Element elereqDoc = reqDoc.getDocumentElement();		
		log.verbose(" getRTSHTTPRequestDoc()strPaymentType "+strPaymentType);		
		if(!YFCObject.isVoid(strPaymentType)){
			log.verbose("payment type is not void");
		if (AcademyConstants.STR_GIFT_CARD.equals(strPaymentType)) {
				log.verbose("For Payment method::"+strPaymentType);	
				log.verbose("Inside ChargeType check for charge_type:: "+chargeType);		
				
				if(chargeType.equals(AcademyConstants.STR_RTS_CHARGE_TRAN_TYPE)){
					Element eleIxIssueNumber = null;
					String multiLockId = null;
					log.verbose("Inside charge_type ::"+AcademyConstants.STR_RTS_CHARGE_TRAN_TYPE);					
					//May change and need to add constant
					multiLockId = XMLUtil.getAttributeFromXPath(inDoc, "Payment/CreditCardTransactions/CreditCardTransaction/@AuthCode");
					
					if(YFCCommon.isVoid(multiLockId)){
						//Partial Auth Reversal: <CreditCardTransactions> missing in paymentInputStruct, hence make a getChargeTransactionList API call to fetch authCode
						multiLockId = getAuthCode(env, inDoc);
					}				
					
					//Mandatory to populate IxIssueNumber
					multiLockId = (!YFCCommon.isVoid(multiLockId)) ? multiLockId : " ";
					eleIxIssueNumber = reqDoc.createElement(AcademyConstants.RTS_RESP_LOCK_ID);
					eleIxIssueNumber.appendChild(reqDoc.createTextNode(multiLockId));
					elereqDoc.appendChild(eleIxIssueNumber);
					log.verbose("multiLockId:: "+multiLockId);
				}
				
				Element eleIxOptions = reqDoc.createElement(AcademyConstants.ATTR_RTS_OPTIONS);
				eleIxOptions.appendChild(reqDoc.createTextNode(AcademyConstants.STR_RTS_VALUE_MULTI_LOCK)); 
				elereqDoc.appendChild(eleIxOptions);
		}
		}
		log.verbose("Exiting getRTSHTTPRequestDoc() with request doc ::"+XMLUtil.getXMLString(reqDoc));
		return reqDoc;
	}
	
	/**
	 * Method merges RTS reqResp doc
	 * @param requestDoc,responseDoc
	 * @return docRTSRequestResponse - RTS ReqResp Merged Document
	 * @throws ParserConfigurationException 
	 */
	public static Document getRTSHTTPReqRespDoc(Document requestDoc, Document responseDoc) throws ParserConfigurationException {
		
		log.verbose("Begin AcademyRTSUtil.getRTSHTTPReqRespDoc()");
		Document docRTSRequestResponse = XMLUtil.createDocument("RTSRequestResponse"); 
		Element eleRTSRequestResponse = docRTSRequestResponse.getDocumentElement();	 
		
		Element eleRTSReq = docRTSRequestResponse.createElement("RTSrequest");
		Element eleRTSRequest = requestDoc.getDocumentElement();
		eleRTSRequestResponse.appendChild(eleRTSReq);
		XMLUtil.importElement(eleRTSReq, eleRTSRequest);

		Element eleRTSResp = docRTSRequestResponse.createElement("RTSresponse");
		Element eleRTSResponse = responseDoc.getDocumentElement();
		eleRTSRequestResponse.appendChild(eleRTSResp);
		XMLUtil.importElement(eleRTSResp, eleRTSResponse);
		log.verbose("AcademyRTSUtil docRTSRequestResponse Merged doc : "+XMLUtil.getXMLString(docRTSRequestResponse));
		
		return docRTSRequestResponse;		
	}
	
	/**
	 * Method forms RTS input XML common for all request types
	 * @param inDocEle,paymentType-GIFT_CARD, tranType-BalanceInquiry/Redeem/Activate
	 * @return doc - RTS input with common attributes
	 */
public static Document setRTSCommonRequestAttributes(YFSEnvironment env,Element inDocEle,String paymentType,String tranType) {
		
		log.verbose("Inside setRTSCommonRequestAttributes( )");
		log.verbose("paymentType::"+paymentType);
		log.verbose("tranType::"+tranType);
		log.verbose("InDoc::"+XMLUtil.getXMLString(inDocEle.getOwnerDocument()));		

		Document doc = YFCDocument.createDocument("XMLAJBFipayRequest").getDocument();
		Element rootEle = doc.getDocumentElement();
		rootEle.setAttribute("Version", "1.0");
		
		Element eleIxCmd = doc.createElement(AcademyConstants.ATTR_RTS_CMD);
		Element eleIxDebitCredit = doc.createElement(AcademyConstants.ATTR_RTS_DEBIT_CREDIT);
		Element eleIxStoreNumber = doc.createElement(AcademyConstants.ATTR_RTS_STORE_NUM);
		Element eleIxTerminalNumber = doc.createElement(AcademyConstants.ATTR_RTS_TERMINAL_NUM);			
		Element eleIxTranType = doc.createElement(AcademyConstants.ATTR_RTS_TRAN_TYPE);
		Element eleIxDate = doc.createElement(AcademyConstants.ATTR_RTS_DATE);
		Element eleIxTime = doc.createElement(AcademyConstants.ATTR_RTS_TIME);
		Element eleIxAccount = doc.createElement(AcademyConstants.ATTR_RTS_ACCOUNT);
		Element eleIxExpDate = doc.createElement(AcademyConstants.ATTR_RTS_EXPIRY_DATE);
		Element eleIxOptions = doc.createElement(AcademyConstants.ATTR_RTS_OPTIONS);
		Element eleIxAmount = doc.createElement(AcademyConstants.ATTR_RTS_AMOUNT);
		Element eleIxInvoice = doc.createElement(AcademyConstants.ATTR_RTS_INVOICE);
		//Start : MPMT-84
		Element eleMailOrderAVS = doc.createElement(AcademyConstants.ATTR_RTS_MAIL_ORDER_AVS_DATA);
				
		SimpleDateFormat sdfDate = new SimpleDateFormat(AcademyConstants.STR_RTS_DATE_FORMAT);
		SimpleDateFormat sdfTime = new SimpleDateFormat(AcademyConstants.STR_RTS_DATE_TIME);
		Calendar cal = Calendar.getInstance();
		eleIxDate.appendChild(doc.createTextNode(sdfDate.format(cal.getTime())));
		eleIxTime.appendChild(doc.createTextNode(sdfTime.format(cal.getTime())));
		
		Double requestAmt = new Double(inDocEle.getAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT)); 
		DecimalFormat df = new DecimalFormat("##.00");
		
		eleIxInvoice.appendChild(doc.createTextNode(inDocEle.getAttribute(AcademyConstants.ATTR_ORDER_NO)));
		
		if (AcademyConstants.STR_GIFT_CARD.equals(paymentType) || AcademyConstants.STR_RTS_ACTIVATION_TRAN_TYPE.equals(tranType)){
						
			eleIxCmd.appendChild(doc.createTextNode(AcademyConstants.STR_RTS_VALUE_CMD));
			eleIxDebitCredit.appendChild(doc.createTextNode(AcademyConstants.STR_RTS_VALUE_GIFT_CARD));
			eleIxStoreNumber.appendChild(doc.createTextNode(YFSSystem.getProperty(AcademyConstants.STR_RTS_VALUE_STORE_NUM)));
			if(AcademyConstants.STR_RTS.equalsIgnoreCase(inDocEle.getAttribute("PaymentReference3")))
				eleIxTerminalNumber.appendChild(doc.createTextNode(YFSSystem.getProperty(AcademyConstants.STR_RTS_VALUE_MERCH_TERM_NUM)));
			else
				eleIxTerminalNumber.appendChild(doc.createTextNode(YFSSystem.getProperty(AcademyConstants.STR_RTS_VALUE_TERM_NUM)));
			eleIxTranType.appendChild(doc.createTextNode(tranType));	
			eleIxAccount.appendChild(doc.createTextNode(inDocEle.getAttribute("SvcNo")));
			eleIxAmount.appendChild(doc.createTextNode(df.format(requestAmt.doubleValue()).replace(".", "")));
	        //Start GCD-219 To recorgnize the Ecom Transaction
			if(!AcademyConstants.STR_RTS_ACTIVATION_TRAN_TYPE.equals(tranType))
			{
			eleMailOrderAVS.appendChild(doc.createTextNode(AcademyConstants.STR_RTS_VALUE_ECI));
			rootEle.appendChild(eleMailOrderAVS); 
			}
	        //End GCD-219
			rootEle.appendChild(eleIxCmd);
			rootEle.appendChild(eleIxDebitCredit);
			rootEle.appendChild(eleIxStoreNumber);
			rootEle.appendChild(eleIxTerminalNumber);
			rootEle.appendChild(eleIxTranType);
			rootEle.appendChild(eleIxDate);
			rootEle.appendChild(eleIxTime);
			rootEle.appendChild(eleIxAccount);
			rootEle.appendChild(eleIxAmount);
			rootEle.appendChild(eleIxInvoice);
			
		}
		
		//Start : MPMT-19,23,24,28 Request document for RTS
		else if(paymentType.equalsIgnoreCase(AcademyConstants.STR_CREDIT_PAYMENT_TYPE)){
			
			String  reqAmount = inDocEle.getAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT);
			log.verbose("Amount::"+reqAmount);
			String  strAuthorizationID = inDocEle.getAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID);
			
			eleIxCmd.appendChild(doc.createTextNode(YFSSystem.getProperty(AcademyConstants.RTS_REQ_CMD)));
           		eleIxStoreNumber.appendChild(doc.createTextNode(YFSSystem.getProperty(AcademyConstants.RTS_REQ_STORE_NUM)));
			eleIxTerminalNumber.appendChild(doc.createTextNode(YFSSystem.getProperty(AcademyConstants.RTS_REQ_TERMINAL_NUM)));
			eleIxDebitCredit.appendChild(doc.createTextNode(AcademyConstants.RTS_REQ_CREDIT));
			eleIxExpDate.appendChild(doc.createTextNode(inDocEle.getAttribute(AcademyConstants.ATTR_CC_EXPIRATION_DATE)));
			//eleIxAccount.appendChild(doc.createTextNode(inDocEle.getAttribute(AcademyConstants.CREDIT_CARD_NO)));
			eleIxAccount.appendChild(doc.createTextNode(inDocEle.getAttribute(AcademyConstants.ATTR_PAYMENT_REF_4)));
			eleIxAmount.appendChild(doc.createTextNode(df.format(requestAmt.doubleValue()).replace(".", "").replace("-", "")));
			
			//Start : MPMT-84
			//The data in IxMailOrderAVSData element set as *AVS_Zip_Address i.e *AVS_ShipToZipCode_ShipToAddressLine1
			String mailOrderAVSData = AcademyConstants.RTS_MAIL_ORDER_AVS  
										+ inDocEle.getAttribute(AcademyConstants.ATTR_SHIP_TO_ZIP_CODE).replace("-", "") 
										+ AcademyConstants.STR_UNDERSCORE
										+ inDocEle.getAttribute(AcademyConstants.ATTR_SHIP_TO_ADDR_LINE1);
			
			eleMailOrderAVS.appendChild(doc.createTextNode(mailOrderAVSData));
			
			YFSContext oEnv = (YFSContext) env;
			long rtsTranSeqNo = oEnv.getNextDBSeqNo(YFSSystem.getProperty(AcademyConstants.RTS_TRAN_SEQ_NUM));
			
			eleIxOptions.appendChild(doc.createTextNode(AcademyConstants.RTS_OPTIONS_SALE + rtsTranSeqNo));
			log.verbose("RTS Seq No:: "+ AcademyConstants.RTS_OPTIONS_SALE + rtsTranSeqNo);

			//End : MPMT-84

			if(tranType.equalsIgnoreCase(AcademyConstants.STR_CHRG_TYPE_AUTH)) {
				eleIxTranType.appendChild(doc.createTextNode(AcademyConstants.RTS_REQ_TRAN_TYPE_PRE_AUTH));
				//eleIxOptions.appendChild(doc.createTextNode(AcademyConstants.RTS_OPTIONS_SALE));
			}
			if(tranType.equalsIgnoreCase(AcademyConstants.STR_CHRG_TYPE_CHARGE)) {
				if(reqAmount.startsWith("-")){
					eleIxTranType.appendChild(doc.createTextNode(AcademyConstants.RTS_REQ_TRAN_TYPE_REFUND));
					//eleIxOptions.appendChild(doc.createTextNode(AcademyConstants.RTS_OPTIONS_REVERSAL));
				} else {
					if(YFSObject.isVoid(strAuthorizationID) || AcademyConstants.STR_DUMMY_AUTH_CODE.equals(strAuthorizationID)){
						eleIxTranType.appendChild(doc.createTextNode(AcademyConstants.RTS_REQ_TRAN_TYPE_SALE));
					}else{
						eleIxTranType.appendChild(doc.createTextNode(AcademyConstants.RTS_REQ_TRAN_TYPE_PRE_AUTH_COMP));
					}
					//eleIxOptions.appendChild(doc.createTextNode(AcademyConstants.RTS_OPTIONS_SALE));
				}
			}
			
			rootEle.appendChild(eleIxCmd);
			rootEle.appendChild(eleIxDebitCredit);
			rootEle.appendChild(eleIxStoreNumber);
			rootEle.appendChild(eleIxTerminalNumber);
			rootEle.appendChild(eleIxTranType);
			rootEle.appendChild(eleIxDate);
			rootEle.appendChild(eleIxTime);
			rootEle.appendChild(eleIxAccount);
			rootEle.appendChild(eleIxExpDate);
			rootEle.appendChild(eleIxOptions);
			rootEle.appendChild(eleIxAmount);
			rootEle.appendChild(eleIxInvoice);
			rootEle.appendChild(eleMailOrderAVS);
		}
		//End : MPMT-19,23,24,28 Request document for RTS 
		
		log.verbose("Exiting setRTSCommonRequestAttributes() with o/p XML::"+XMLUtil.getXMLString(doc));
		return doc;
	}

/**
 * In case of reversing Partial Authorization, we will have to populate authCode from its corresponding authorization request
 * @param inXML
 * @return Auth Code
 * @throws Exception 
 */
private static String getAuthCode(YFSEnvironment env, Document inXML) throws Exception{
	
	log.verbose("Entering getAuthCode");		
	String strOpenAuthAmount = String.valueOf(Math.abs(Double.parseDouble(inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_CURRENT_AUTHORIZATION_AMT))));
	log.verbose("strOpenAuthAmount :: "+strOpenAuthAmount);
	
	Document getChargeTranListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_CHARGE_TRANS);
	getChargeTranListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, 
			inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID));
	getChargeTranListInDoc.getDocumentElement().setAttribute(AcademyConstants.STR_ORDR_HDR_KEY, 
			inXML.getDocumentElement().getAttribute(AcademyConstants.STR_ORDR_HDR_KEY));		
	getChargeTranListInDoc.getDocumentElement().setAttribute("OpenAuthorizedAmount", 
			strOpenAuthAmount);
	log.verbose("Input to getChargeTransactionList :: "+XMLUtil.getXMLString(getChargeTranListInDoc));		
	String strGetChargeTransactionListTemp = "<ChargeTransactionDetails><ChargeTransactionDetail AuthorizationID=''><CreditCardTransactions>"
		+"<CreditCardTransaction AuthCode=''/></CreditCardTransactions></ChargeTransactionDetail></ChargeTransactionDetails>";
	Document docGetChargeTransactionListTemp = YFCDocument.getDocumentFor(strGetChargeTransactionListTemp).getDocument();
	
	env.setApiTemplate(AcademyConstants.API_GET_CHARGE_TRANSACTION_LIST, docGetChargeTransactionListTemp);
	Document getChargeTranListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_CHARGE_TRANSACTION_LIST, getChargeTranListInDoc);
	env.clearApiTemplate(AcademyConstants.API_GET_CHARGE_TRANSACTION_LIST);		
	log.verbose("Output from getChargeTransactionList :: "+XMLUtil.getXMLString(getChargeTranListOutDoc));
	
	NodeList nlCreditCardTransaction = getChargeTranListOutDoc.getElementsByTagName(AcademyConstants.ELE_CREDIT_TRANS);		
	for(int i=0; i < nlCreditCardTransaction.getLength(); i++){
		Element eleCreditCardTransaction = (Element) nlCreditCardTransaction.item(i);	
		log.verbose("eleCreditCardTransaction :: "+eleCreditCardTransaction);
		String strAuthCode = eleCreditCardTransaction.getAttribute(AcademyConstants.ATTR_AUTH_CODE);
		log.verbose("strAuthCode :: "+strAuthCode);
		if(!YFCCommon.isVoid(strAuthCode))
			return strAuthCode;
	}
	return null;
}

	public static int rand(int lo, int hi){  
	        int n = hi - lo + 1;
	        int i = rn.nextInt() % n;
	        if (i < 0)
	                i = -i;
	   
	        return lo + i;
	}
	public static String randomstring(int lo, int hi){  
	        int n = rand(lo, hi);
	        byte b[] = new byte[n];
	        for (int i = 0; i < n; i++)
	                b[i] = (byte)rand('1', '9');
	   
	        return new String(b);
	}
}