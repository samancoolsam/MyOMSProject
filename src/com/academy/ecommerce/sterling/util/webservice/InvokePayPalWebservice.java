package com.academy.ecommerce.sterling.util.webservice;

import java.net.URL;
import java.util.Properties;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.dom.DOMSource;

import org.apache.axis.message.MessageElement;
import org.w3c.dom.Document;

import com.academy.ecommerce.sterling.util.AcademyServiceUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;


public class InvokePayPalWebservice implements YIFCustomApi {
	
	private Properties props;

	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory.instance(InvokePayPalWebservice.class);

	public void setProperties(Properties props) {
		this.props = props;
	}

	public Document invokeSoapWebservice(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("*** invoking invokeSoapWebservice with input doc *** " + XMLUtil.getXMLString(inDoc));
		SOAPConnection connection =	AcademyServiceUtil.getSOAPConnection();
		log.verbose("*** Getting connection *** ");
		String strEndpointURL = YFSSystem.getProperty(props.getProperty("academy.paypal.refund.endpoint"));
		log.verbose("*** strEndpointURL *** " + strEndpointURL);
		URL endpoint = new URL(strEndpointURL);
		SOAPMessage request = getSOAPMessageForPayPalRefund(inDoc);
		SOAPMessage response = connection.call(request, endpoint);
		Document outDoc = translateSOAPResponse(inDoc, response);
		return outDoc;
	}

	private SOAPMessage getSOAPMessageForPayPalRefund(Document inDoc) throws Exception {
		try {
			Document document = AcademyServiceUtil.getSOAPMessageTemplateForPayPalRefund();
			document.getElementsByTagName(AcademyConstants.PAYPAL_URN1_PREFIX + "Username").item(0).setTextContent(
					YFSSystem.getProperty(props.getProperty("academy.paypal.refund.username")));
			
			document.getElementsByTagName(AcademyConstants.PAYPAL_URN1_PREFIX + "Password").item(0).setTextContent(
					YFSSystem.getProperty(props.getProperty("academy.paypal.refund.password")));

			document.getElementsByTagName(AcademyConstants.PAYPAL_URN1_PREFIX + "Signature").item(0).setTextContent(
					YFSSystem.getProperty(props.getProperty("academy.paypal.refund.signature")));

			document.getElementsByTagName(AcademyConstants.PAYPAL_URN_PREFIX + "TransactionID").item(0).setTextContent(
					inDoc.getDocumentElement().getAttribute("AuthorizationId"));

			document.getElementsByTagName(AcademyConstants.PAYPAL_URN_PREFIX + "RefundType").item(0).setTextContent(
					YFSSystem.getProperty(props.getProperty("academy.paypal.refund.type")));

			double refundAmount = Math.abs(Double.parseDouble(inDoc.getDocumentElement().getAttribute("RequestAmount")));
			
			document.getElementsByTagName(AcademyConstants.PAYPAL_URN_PREFIX + "Amount").item(0).setTextContent(
					String.valueOf(refundAmount));
			
			log.verbose("*** After setting getSOAPMessageForPayPalRefund *** " + XMLUtil.getXMLString(document));
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


	
	public static Document translateSOAPResponse(Document inDoc, SOAPMessage response) throws Exception {
		log.verbose("translateSOAPResponse &&&&&&&&&&&&&&&&&&&");
		String outDocString = "<Payment ResponseCode='' AsynchRequestProcess='' AuthAVS='' AuthCode='' " +
		"AuthorizationAmount='' AuthorizationExpirationDate='' AuthorizationId='' " +
		"AuthReturnCode='' AuthReturnFlag='' AuthReturnMessage='' AuthTime='' " +
		"BPreviousInvocationSuccessful='' CollectionDate='' DisplayPaymentReference1='' " +
		"HoldOrderAndRaiseEvent='' HoldReason='' InternalReturnCode='' " +
		"InternalReturnFlag='' InternalReturnMessage='' PaymentReference1='' " +
		"PaymentReference2='' PaymentReference3='' RequestID='' RetryFlag='' " +
		"SCVVAuthCode='' SuspendPayment='' TranAmount='' TranRequestTime='' " +
		"TranReturnCode='' TranReturnFlag='' TranReturnMessage='' TranType=''/>";
		YFCDocument docRespose = YFCDocument.getDocumentFor(outDocString);
		YFCElement eleResponse = docRespose.getDocumentElement();
		SOAPBody soapBody = response.getSOAPBody();
		if(response.getSOAPBody().hasFault()) {
			log.verbose("Respose has SOAPFault &&&&&&&&&&&&&&&&&&&");
			SOAPFault soapFault = response.getSOAPBody().getFault();
			log.verbose("Refund response " + XMLUtil.getElementXMLString(soapFault));
		} else {
			
			String returnStatus = ((MessageElement)soapBody.getElementsByTagName("Ack").item(0)).getValue();
			if("Success".equalsIgnoreCase(returnStatus)) {
				log.verbose("Respose has Success &&&&&&&&&&&&&&&&&&&");
				eleResponse.setAttribute("ResponseCode", "APPROVED");
				eleResponse.setAttribute("AuthorizationId", 
					((MessageElement)soapBody.getElementsByTagName("RefundTransactionID").item(0)).getValue());
				eleResponse.setAttribute("AuthorizationAmount", 
					inDoc.getDocumentElement().getAttribute("requestAmount"));
//			eleResponse.setAttribute("AuthTime", 
//					((MessageElement)soapBody.getElementsByTagName("Timestamp").item(0)).getValue());
				eleResponse.setAttribute("RequestID", 
					((MessageElement)soapBody.getElementsByTagName("RefundTransactionID").item(0)).getValue());
				eleResponse.setAttribute("TranAmount", 
					((MessageElement)soapBody.getElementsByTagName("GrossRefundAmount").item(0)).getValue());
			} else if ("Failure".equalsIgnoreCase(returnStatus)) {
				log.verbose("Respose has Failure &&&&&&&&&&&&&&&&&&&");
				eleResponse.setAttribute("ResponseCode", "SOFT_DECLINE");
				eleResponse.setAttribute("AuthReturnCode", 
					((MessageElement)soapBody.getElementsByTagName("ErrorCode").item(0)).getValue());
				eleResponse.setAttribute("AuthReturnMessage", 
					((MessageElement)soapBody.getElementsByTagName("LongMessage").item(0)).getValue());
//				eleResponse.setAttribute("AuthTime", 
//					((MessageElement)soapBody.getElementsByTagName("Timestamp").item(0)).getValue());
			}
		}
		return docRespose.getDocument();
	}
}
