package com.academy.ecommerce.sterling.util.webservice;

import java.net.URL;
import java.util.Properties;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.dom.DOMSource;

import org.apache.axis.message.MessageElement;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyPaymentProcessingUtil;
import com.academy.ecommerce.sterling.util.AcademyServiceUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;


public class InvokeCyberSourceWebservice implements YIFCustomApi {
	
	private Properties props;
	
	public final static String PAYMENT_PATH_FOR_AUTH = "/Payment/";
	public final static String PAYMENT_PATH_FOR_REFUND_OR_REAUTH = "/AcademyMergedDocument/EnvironmentDocument/Payment/"; 
	public final static String CC_DECRYPTED_INFO_PATH = "/AcademyMergedDocument/InputDocument/PaymentMethod/";

	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory.instance(InvokeCyberSourceWebservice.class);

	public void setProperties(Properties props) {
		this.props = props;
	}

	public Document invokeSoapWebservice(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("*** invoking invokeSoapWebservice with input doc *** " + XMLUtil.getXMLString(inDoc));
		SOAPConnection connection =	AcademyServiceUtil.getSOAPConnection();
		String strEndpointURL = YFSSystem.getProperty(props.getProperty("academy.cybersource.endpoint"));
		log.verbose("*** strEndpointURL *** " + strEndpointURL);
		URL endpoint = new URL(strEndpointURL);
		String paymentPath = PAYMENT_PATH_FOR_REFUND_OR_REAUTH;
		if("Payment".equalsIgnoreCase(inDoc.getDocumentElement().getNodeName())) {
			paymentPath = PAYMENT_PATH_FOR_AUTH;
		} else if ("Y".equals(XPathUtil.getString(inDoc, CC_DECRYPTED_INFO_PATH + "@HasError"))) {
			// Raise Wallet unavailable Alert and return
			
			YFCDocument inDocToWalletAllert = YFCDocument.createDocument("Order");
			inDocToWalletAllert.getDocumentElement().setAttribute("OrderHeaderKey", 
					XPathUtil.getString(inDoc, PAYMENT_PATH_FOR_REFUND_OR_REAUTH + "@OrderHeaderKey"));
			inDocToWalletAllert.getDocumentElement().setAttribute("OrderNo", 
					XPathUtil.getString(inDoc, PAYMENT_PATH_FOR_REFUND_OR_REAUTH + "@OrderNo"));
			inDocToWalletAllert.getDocumentElement().setAttribute("CreditCardNo", 
					XPathUtil.getString(inDoc, PAYMENT_PATH_FOR_REFUND_OR_REAUTH + "@CreditCardNo"));
			inDocToWalletAllert.getDocumentElement().setAttribute("ErrorCode", 
					XPathUtil.getString(inDoc, CC_DECRYPTED_INFO_PATH + "@ErrorCode"));
			inDocToWalletAllert.getDocumentElement().setAttribute("ErrorDescription", 
					XPathUtil.getString(inDoc, CC_DECRYPTED_INFO_PATH + "@ErrorDescription"));
			
			AcademyPaymentProcessingUtil.raiseAlert(env, inDocToWalletAllert.getDocument());

			YFCDocument docRespose = YFCDocument.createDocument("Payment");
			YFCElement eleResponse = docRespose.getDocumentElement();
			log.verbose("CyberSource webservice not invoked since Wallet Service unavailable");
			eleResponse.setAttribute("ResponseCode", "SERVICE_UNAVAILABLE");
			eleResponse.setAttribute("RetryFlag", "Y");
			eleResponse.setAttribute("TranReturnCode", 
					XPathUtil.getString(inDoc, CC_DECRYPTED_INFO_PATH + "@ErrorCode"));
			eleResponse.setAttribute("TranReturnMessage", 
					XPathUtil.getString(inDoc, CC_DECRYPTED_INFO_PATH + "@ErrorDescription"));
			return docRespose.getDocument();
		}
		
		SOAPMessage request = getSOAPMessageForCyberSource(env, inDoc, paymentPath);
		SOAPMessage response = connection.call(request, endpoint);
		log.verbose("*** SOAPMessage response *** " + XMLUtil.getXMLString(response.getSOAPPart()));
		Document outDoc = translateSOAPResponse(env, inDoc, response, paymentPath);
		log.verbose("*** return from invokeSoapWebservice with out doc *** " + XMLUtil.getXMLString(outDoc));
		return outDoc;
	}

	private SOAPMessage getSOAPMessageForCyberSource(YFSEnvironment env, Document inDoc, String paymentPath) 
		throws Exception {
		try {
			Document document = null;
			String chargeType = XPathUtil.getString(inDoc, paymentPath + "@ChargeType");
			if("AUTHORIZATION".equalsIgnoreCase(chargeType)) {
				log.verbose("*** getSOAPMessageForCyberSource for AUTHORIZATION  *** ");
				document = getSOAPDocForAuthRequest(inDoc, paymentPath);
			} else if("CHARGE".equalsIgnoreCase(chargeType)) {
				log.verbose("*** getSOAPMessageForCyberSource for CHARGE  *** ");
				document = getSOAPDocForRefundRequest(env, inDoc, paymentPath);
			}
			log.verbose("*** returning getSOAPMessageForCyberSource *** " + XMLUtil.getXMLString(document));
			MessageFactory messageFactory = MessageFactory.newInstance();
			SOAPMessage soapMessage = messageFactory.createMessage();
			DOMSource domSource = new DOMSource(document);
			SOAPPart soapPart = soapMessage.getSOAPPart();
			soapPart.setContent(domSource);
			return soapMessage;
		} catch (SOAPException e) {
			throw e;
		}
	}
	
	private Document getSOAPDocForRefundRequest(YFSEnvironment env, Document inDoc, String paymentPath) throws Exception {
		log.verbose(" ############inside getSOAPDocForRefundRequest() ");
		Document document = AcademyServiceUtil.getSOAPMessageTemplateForCCRefund();
		setCommonAttributesForRequest(document, inDoc, paymentPath);
		log.verbose("After setting common Attributes " + XMLUtil.getXMLString(document));
		String authorizationID = getValidAuthID(env, inDoc, paymentPath);
		if(YFCObject.isVoid(authorizationID)) {
			YFSException yfsException = new YFSException();
			yfsException.setErrorCode("No Valid AuthorizationID found for Refund");
			yfsException.setErrorDescription("No Valid AuthorizationID found for Refund");
			throw yfsException;
		}
		document.getElementsByTagName(
				AcademyConstants.CYBERSOURCE_URN_PREFIX + "captureRequestID").item(0).setTextContent(
						authorizationID);
		double refundAmount = Math.abs(Double.parseDouble(XPathUtil.getString(inDoc, 
				PAYMENT_PATH_FOR_REFUND_OR_REAUTH + "@RequestAmount")));
		document.getElementsByTagName(
				AcademyConstants.CYBERSOURCE_URN_PREFIX + "grandTotalAmount").item(0).setTextContent(
				String.valueOf(refundAmount));
		log.verbose("Refund input request " + XMLUtil.getXMLString(document));
		return document;
	}
	
	private Document getSOAPDocForAuthRequest(Document inDoc, String paymentPath) throws Exception {
		log.verbose(" ############inside getSOAPDocForAuthRequest() ");
		Document document = AcademyServiceUtil.getSOAPMessageTemplateForCCAuth();
		setCommonAttributesForRequest(document, inDoc, paymentPath);
		log.verbose("After setting common Attributes " + XMLUtil.getXMLString(document));
		
		document.getElementsByTagName(AcademyConstants.CYBERSOURCE_URN_PREFIX + "firstName").item(0).setTextContent(
				XPathUtil.getString(inDoc, paymentPath + "@BillToFirstName"));
		document.getElementsByTagName(AcademyConstants.CYBERSOURCE_URN_PREFIX + "lastName").item(0).setTextContent(
				XPathUtil.getString(inDoc, paymentPath + "@BillToLastName"));
		document.getElementsByTagName(AcademyConstants.CYBERSOURCE_URN_PREFIX + "street1").item(0).setTextContent(
				XPathUtil.getString(inDoc, paymentPath + "@BillToAddressLine1"));
		document.getElementsByTagName(AcademyConstants.CYBERSOURCE_URN_PREFIX + "city").item(0).setTextContent(
				XPathUtil.getString(inDoc, paymentPath + "@BillToCity"));
		document.getElementsByTagName(AcademyConstants.CYBERSOURCE_URN_PREFIX + "state").item(0).setTextContent(
				XPathUtil.getString(inDoc, paymentPath + "@BillToState"));
		document.getElementsByTagName(AcademyConstants.CYBERSOURCE_URN_PREFIX + "postalCode").item(0).setTextContent(
				XPathUtil.getString(inDoc, paymentPath + "@BillToZipCode"));
		document.getElementsByTagName(AcademyConstants.CYBERSOURCE_URN_PREFIX + "country").item(0).setTextContent(
				XPathUtil.getString(inDoc, paymentPath + "@BillToCountry"));
		if(YFCObject.isVoid(XPathUtil.getString(inDoc, paymentPath + "@BillToEmailId"))) {
 			document.getElementsByTagName(AcademyConstants.CYBERSOURCE_URN_PREFIX + "email").item(0).setTextContent(
					"admin@academy.com");
		} else {
 			document.getElementsByTagName(AcademyConstants.CYBERSOURCE_URN_PREFIX + "email").item(0).setTextContent(
					XPathUtil.getString(inDoc, paymentPath + "@BillToEmailId"));
		}
		log.verbose("AuthRequest Input " + XMLUtil.getXMLString(document));
		return document;
	}
	
	private void setCommonAttributesForRequest(Document document, Document inDoc, String paymentPath) throws Exception {
		log.verbose("&&&&&&&&&&&&&&&&&&& setCommonAttributesForRequest &&&&&&&&&&&&&&&&&&&");
		document.getElementsByTagName(AcademyConstants.CYBERSOURCE_WSSE_PREFIX + "Username").item(0).setTextContent(
				YFSSystem.getProperty(props.getProperty("academy.cybersource.username")));
		document.getElementsByTagName(AcademyConstants.CYBERSOURCE_WSSE_PREFIX + "Password").item(0).setTextContent(
				YFSSystem.getProperty(props.getProperty("academy.cybersource.password")));
		document.getElementsByTagName(AcademyConstants.CYBERSOURCE_URN_PREFIX + "merchantID").item(0).setTextContent(
				YFSSystem.getProperty(props.getProperty("academy.cybersource.merchantid")));
		document.getElementsByTagName(AcademyConstants.CYBERSOURCE_URN_PREFIX + "merchantReferenceCode").item(0).setTextContent(
				XPathUtil.getString(inDoc, paymentPath + "@OrderNo"));
		
		document.getElementsByTagName(AcademyConstants.CYBERSOURCE_URN_PREFIX + "currency").item(0).setTextContent(
				XPathUtil.getString(inDoc, paymentPath + "@Currency"));
		document.getElementsByTagName(AcademyConstants.CYBERSOURCE_URN_PREFIX + "grandTotalAmount").item(0).setTextContent(
				XPathUtil.getString(inDoc, paymentPath + "@RequestAmount"));
		
		/* 
		 * For of ReAuth or Refund, get Credit card number and Credit card expiration date 
		 * from decrypt info. Else fetch from Sterling input.
		 * 
		 */ 
		if(!YFCObject.isVoid(XPathUtil.getString(inDoc, CC_DECRYPTED_INFO_PATH + "@CreditCardNo"))) {
			document.getElementsByTagName(AcademyConstants.CYBERSOURCE_URN_PREFIX + "accountNumber").item(0).setTextContent(
				XPathUtil.getString(inDoc, CC_DECRYPTED_INFO_PATH + "@CreditCardNo"));
		} else {
			document.getElementsByTagName(AcademyConstants.CYBERSOURCE_URN_PREFIX + "accountNumber").item(0).setTextContent(
				XPathUtil.getString(inDoc, paymentPath + "@CreditCardNo"));
		}
		/*Start Fix For PROD Bug 4127 - Zubair*/
		String ccExpirationDate = "";
		String ccExpirationMonth="";
		String ccExpirationYear="";
		if((!YFCObject.isVoid(XPathUtil.getString(inDoc, CC_DECRYPTED_INFO_PATH + "@CreditCardExpMonth")))&&(!YFCObject.isVoid(XPathUtil.getString(inDoc, CC_DECRYPTED_INFO_PATH + "@CreditCardExpYear")))) {
			ccExpirationMonth = XPathUtil.getString(inDoc, CC_DECRYPTED_INFO_PATH + "@CreditCardExpMonth");
			String ccTempExpirationYear = XPathUtil.getString(inDoc, CC_DECRYPTED_INFO_PATH + "@CreditCardExpYear");
			ccExpirationYear = ccTempExpirationYear.substring(ccTempExpirationYear.length()-2,ccTempExpirationYear.length());
			document.getElementsByTagName(AcademyConstants.CYBERSOURCE_URN_PREFIX + "expirationMonth").item(0).setTextContent(
					ccExpirationMonth);
			document.getElementsByTagName(AcademyConstants.CYBERSOURCE_URN_PREFIX + "expirationYear").item(0).setTextContent(
					ccExpirationYear);
		} else {
			ccExpirationDate = XPathUtil.getString(inDoc, paymentPath + "@CreditCardExpirationDate");
			String[] ccExpMonthAndYear = AcademyPaymentProcessingUtil.getCCExpirationMonthAndYear(ccExpirationDate);
			document.getElementsByTagName(AcademyConstants.CYBERSOURCE_URN_PREFIX + "expirationMonth").item(0).setTextContent(
					ccExpMonthAndYear[0]);
			document.getElementsByTagName(AcademyConstants.CYBERSOURCE_URN_PREFIX + "expirationYear").item(0).setTextContent(
					ccExpMonthAndYear[1]);
		}
		/*End Fix For PROD Bug 4127 - Zubair*/
	}
	
	public Document translateSOAPResponse(YFSEnvironment env, Document inDoc, SOAPMessage response, 
			String paymentPath) throws Exception {
		log.verbose("&&&&&&&&&&&&&&&&&&& translateSOAPResponse &&&&&&&&&&&&&&&&&&&");
		YFCDocument docRespose = YFCDocument.createDocument("Payment");
		YFCElement eleResponse = docRespose.getDocumentElement();
		
		if(response.getSOAPBody().hasFault()) {
			SOAPFault soapFault = response.getSOAPBody().getFault();

			/*
			 * If the the request is for Auth and from COM PCA, throw UE Exception with error code and 
			 * error description. Else return ResponseCode as "SERVICE_UNAVAILABLE"
			 */
			if(PAYMENT_PATH_FOR_AUTH.equals(paymentPath)) {
				throw AcademyPaymentProcessingUtil.getUEExceptionFromSoapFault(soapFault);
			} else {
				log.verbose("&&&&&&&&&&&&&&&&&&& Respose has SOAPFault &&&&&&&&&&&&&&&&&&&");
				log.verbose("AuthRequest Input " + XMLUtil.getElementXMLString(soapFault));
				eleResponse.setAttribute("ResponseCode", "SERVICE_UNAVAILABLE");
				eleResponse.setAttribute("RetryFlag", "Y");
				eleResponse.setAttribute("TranReturnCode", soapFault.getFaultCode());
				eleResponse.setAttribute("TranReturnMessage", soapFault.getFaultString());
			}
		} else {
			if("AUTHORIZATION".equalsIgnoreCase(XPathUtil.getString(inDoc, 
					paymentPath + "@ChargeType"))) {
				translateSOAPResponseForAuth(env, inDoc, response, eleResponse, paymentPath);
			}
			else if("CHARGE".equalsIgnoreCase(XPathUtil.getString(inDoc, 
					paymentPath + "@ChargeType"))) {
				translateSOAPResponseForRefund(env, inDoc, response, eleResponse, paymentPath);
			}
		}
		return docRespose.getDocument();
	}
	
	
	private void translateSOAPResponseForAuth(YFSEnvironment env, Document inDoc, 
			SOAPMessage response, YFCElement eleResponse, String paymentPath) throws Exception {
		log.verbose("translateSOAPResponseForAuth &&&&&&&&&&&&&&&&&&&");
		log.verbose("inDoc ="+XMLUtil.getXMLString(inDoc));
		SOAPBody soapBody = response.getSOAPBody();
		String returnStatus = ((MessageElement)soapBody.getElementsByTagName("decision").item(0)).getValue();
		if("ACCEPT".equalsIgnoreCase(returnStatus)) {
			log.verbose("translateSOAPResponseForAuth ACCEPT &&&&&&&&&&&&&&&&&&&");
			eleResponse.setAttribute("ResponseCode", "APPROVED");
			eleResponse.setAttribute("AuthorizationId", 
					((MessageElement)soapBody.getElementsByTagName("requestID").item(0)).getValue());
			eleResponse.setAttribute("AuthorizationAmount", 
					XPathUtil.getString(inDoc, paymentPath + "@RequestAmount"));
			log.verbose("RequestAmount = "+XPathUtil.getString(inDoc, paymentPath + "@RequestAmount"));
			eleResponse.setAttribute("AuthTime", AcademyPaymentProcessingUtil.getDateInXMLFormat( 
					((MessageElement)soapBody.getElementsByTagName("authorizedDateTime").item(0)).getValue()));
			eleResponse.setAttribute("RequestID", 
					((MessageElement)soapBody.getElementsByTagName("requestID").item(0)).getValue());
			eleResponse.setAttribute("TranAmount", 
					((MessageElement)soapBody.getElementsByTagName("amount").item(0)).getValue());			
			String tenderType = XPathUtil.getString(inDoc, paymentPath + "@CreditCardType");
			log.verbose("tenderType = "+tenderType);
			eleResponse.setAttribute("AuthorizationExpirationDate", 
					AcademyPaymentProcessingUtil.getAuthExpirationDate(env, tenderType));
		} else {
			if(PAYMENT_PATH_FOR_AUTH.equals(paymentPath)) {
				throw AcademyPaymentProcessingUtil.getUEExceptionFromAuthResponse(soapBody);
			} else {
				handleErrorResponse(returnStatus, soapBody, eleResponse);	
			}
		}
	}
	
	private void translateSOAPResponseForRefund(YFSEnvironment env, Document inDoc, 
			SOAPMessage response, YFCElement eleResponse, String paymentPath) throws Exception {
		log.verbose("translateSOAPResponseForRefund  &&&&&&&&&&&&&&&&&&&");
		SOAPBody soapBody = response.getSOAPBody();
		String returnStatus = ((MessageElement)soapBody.getElementsByTagName("decision").item(0)).getValue();
		
		if("ACCEPT".equalsIgnoreCase(returnStatus)) {
			log.verbose("translateSOAPResponseForRefund ACCEPT &&&&&&&&&&&&&&&&&&&");
			eleResponse.setAttribute("ResponseCode", "APPROVED");
			eleResponse.setAttribute("AuthorizationId", 
					((MessageElement)soapBody.getElementsByTagName("requestID").item(0)).getValue());
			eleResponse.setAttribute("AuthReturnCode", 
					((MessageElement)soapBody.getElementsByTagName("reasonCode").item(0)).getValue());
			eleResponse.setAttribute("AuthorizationAmount", 
					XPathUtil.getString(inDoc, paymentPath + "@RequestAmount"));
			eleResponse.setAttribute("AuthTime", AcademyPaymentProcessingUtil.getDateInXMLFormat(
					((MessageElement)soapBody.getElementsByTagName("requestDateTime").item(0)).getValue()));
			eleResponse.setAttribute("RequestID", 
					((MessageElement)soapBody.getElementsByTagName("requestID").item(0)).getValue());
			eleResponse.setAttribute("TranAmount", 
					((MessageElement)soapBody.getElementsByTagName("amount").item(0)).getValue());
		} else {
			handleErrorResponseForRefund(env, inDoc, returnStatus, soapBody, eleResponse);		
		}
	}
	
	private void handleErrorResponseForRefund(YFSEnvironment env, Document inDoc, 
			String returnStatus, SOAPBody soapBody,	YFCElement eleResponse) throws Exception{
		
		if ("REJECT".equalsIgnoreCase(returnStatus) || "ERROR".equalsIgnoreCase(returnStatus)) {
			AcademyPaymentProcessingUtil.raiseNoValidTenderAlert(env, inDoc);
			
			log.verbose("translateSOAPResponseForAuth ERROR &&&&&&&&&&&&&&&&&&&");
			eleResponse.setAttribute("ResponseCode", "HARD_DECLINED");
			eleResponse.setAttribute("RetryFlag", "Y");
			eleResponse.setAttribute("AuthReturnCode", 
					((MessageElement)soapBody.getElementsByTagName("reasonCode").item(0)).getValue());
		} else {
			eleResponse.setAttribute("ResponseCode", "SERVICE_UNAVAILABLE");
			eleResponse.setAttribute("RetryFlag", "Y");
			eleResponse.setAttribute("AuthReturnCode", "Service Unavailable");
		}
	}
	
	private void handleErrorResponse(String returnStatus, SOAPBody soapBody, YFCElement eleResponse) 
		throws Exception {
		
		if ("REJECT".equalsIgnoreCase(returnStatus) || "ERROR".equalsIgnoreCase(returnStatus)) {
			log.verbose("translateSOAPResponseForAuth ERROR &&&&&&&&&&&&&&&&&&&");
			eleResponse.setAttribute("ResponseCode", "HARD_DECLINED");
			eleResponse.setAttribute("RetryFlag", "Y");
			eleResponse.setAttribute("AuthReturnCode", 
					((MessageElement)soapBody.getElementsByTagName("reasonCode").item(0)).getValue());
		} else {
			eleResponse.setAttribute("ResponseCode", "SERVICE_UNAVAILABLE");
			eleResponse.setAttribute("RetryFlag", "Y");
			eleResponse.setAttribute("AuthReturnCode", "Service Unavailable");
		}
	}

	/**
	 * Call getOrderDetails API to get the CyberSource authorization/requestID using which
	 * refund can be performed.
	 * @return
	 */
	
	private Document callGetOrderDetailsApi(YFSEnvironment env, Document inDoc, String paymentPath) throws Exception {
		log.verbose("callGetOrderDetailsApi &&&&&&&&&&&&&&&&&&&");

		StringBuffer orderDetailsOutPutTemplateBuff = new StringBuffer();
		orderDetailsOutPutTemplateBuff.append("<Order OrderHeaderKey=''>");
		orderDetailsOutPutTemplateBuff.append("<PaymentMethods>");
		orderDetailsOutPutTemplateBuff.append("<PaymentMethod PaymentKey='' PaymentType='' CreditCardNo=''/>");
		orderDetailsOutPutTemplateBuff.append("</PaymentMethods>");
		orderDetailsOutPutTemplateBuff.append("<ChargeTransactionDetails>");
		orderDetailsOutPutTemplateBuff.append("<ChargeTransactionDetail ChargeType='' AuthorizationID='' ");
		orderDetailsOutPutTemplateBuff.append("PaymentKey='' AuthorizationExpirationDate='' RequestAmount='' ");
		orderDetailsOutPutTemplateBuff.append("CreditAmount='' CollectionDate=''>");
		orderDetailsOutPutTemplateBuff.append("<CreditCardTransactions>");
		orderDetailsOutPutTemplateBuff.append("<CreditCardTransaction RequestId='' />");
		orderDetailsOutPutTemplateBuff.append("</CreditCardTransactions>");
		orderDetailsOutPutTemplateBuff.append("</ChargeTransactionDetail>");
		orderDetailsOutPutTemplateBuff.append("</ChargeTransactionDetails>");
		orderDetailsOutPutTemplateBuff.append("</Order>");

		YFCDocument getOrderDetailInDoc = YFCDocument.createDocument("Order");
		getOrderDetailInDoc.getDocumentElement().setAttribute("OrderHeaderKey", 
				XPathUtil.getString(inDoc, paymentPath + "@OrderHeaderKey"));

		YFCDocument getOrderDetailOutTemplate = YFCDocument.getDocumentFor(
				orderDetailsOutPutTemplateBuff.toString());
		env.setApiTemplate("getOrderDetails", getOrderDetailOutTemplate.getDocument());
		log.verbose("getOrder detail input " + XMLUtil.getXMLString(getOrderDetailInDoc.getDocument()));
		Document  getOrderDetailOutDoc = AcademyUtil.invokeAPI(env, 
				"getOrderDetails", getOrderDetailInDoc.getDocument());
		log.verbose("getOrder detail output " + XMLUtil.getXMLString(getOrderDetailOutDoc));
		env.clearApiTemplate("getOrderDetails");
		return getOrderDetailOutDoc;
	}
	
	private String getValidAuthID(YFSEnvironment env, Document inDoc, String paymentPath) throws Exception {
		Document getOrderDetailOutDoc = callGetOrderDetailsApi(env, inDoc, paymentPath);
		String ccNumber = XPathUtil.getString(inDoc, paymentPath + "@CreditCardNo");
		//KER-12036 : Payment Migration Changes to support new Payment Type
		String paymentKey = XPathUtil.getString(getOrderDetailOutDoc, 
				"/Order/PaymentMethods/PaymentMethod[(@PaymentType='CREDIT_CARD' or @PaymentType='Credit_Card') and @CreditCardNo='" + ccNumber + "']/@PaymentKey");
		log.verbose("Refunding on PaymentKey:" + paymentKey + " CreditCard:" + ccNumber);
		NodeList nodeList = XPathUtil.getNodeList(getOrderDetailOutDoc, 
				"/Order/ChargeTransactionDetails/ChargeTransactionDetail[@PaymentKey='" + paymentKey + "' and @ChargeType='CHARGE' and @CreditAmount > 0]");
		return AcademyPaymentProcessingUtil.getValidAuth(nodeList);	
	}
}