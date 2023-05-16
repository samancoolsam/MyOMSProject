package com.academy.ecommerce.sterling.invoice.api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Properties;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

import com.academy.ecommerce.sterling.util.AcademyPaymentProcessingUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;

public class AcademyUpdateInvoiceWithCreditCardInfAPI implements YIFCustomApi {

	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyUpdateInvoiceWithCreditCardInfAPI.class);
	
	public void setProperties(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}
	
	/*	
	public static void main(String args[]) {
		File f = new File("D:/123.xml");
		Document docInput = XMLUtil.getDocumentFromFile(f);
		System.out.println("#### Input Document is "
				+ XMLUtil.getXMLString(docInput));
		AcademyUpdateInvoiceWithCreditCardInfAPI instance = new AcademyUpdateInvoiceWithCreditCardInfAPI();
		Document docTest = instance.retrieveCreditCardInfoAndUpdateMsg(null, docInput);
		System.out.println("#### Output of retrieveCreditCardInfoAndUpdateMsg is "
				+ XMLUtil.getXMLString(docTest));

	}*/

	public Document retrieveCreditCardInfoAndUpdateMsg(YFSEnvironment env,
			Document inDoc) {
		log.verbose("******* invoking retrieveCreditCardInfoAndUpdateMsg method ********");
		log.verbose("******* input doc ********" + XMLUtil.getXMLString(inDoc));
		Document docOutput = null;
		Document docWebServiceResponse=null;
		Document docNewPaymentMethod=null;
		Element elePaymentMethod = null;
		Element eleCurrCollection=null;
		Element eleNewPaymentMethod=null;
		NodeList nCollections=null;
		int iCollections=0;
		String strWalletId=null;
		String strCurrPaymentType="GIFT_CARD";

		try {
			log.beginTimer(" Begin of AcademyUpdateInvoiceWithCreditCardInfAPI ->retrieveCreditCardInfoAndUpdateMsg Api");
				docOutput=XMLUtil.cloneDocument(inDoc);
				nCollections=XPathUtil.getNodeList(docOutput.getDocumentElement(),AcademyConstants.XPATH_INVOICE_COLLECTION_DETAIL);
				iCollections=nCollections.getLength();
				for(int i=0;i<iCollections;i++)
				{
				eleCurrCollection = (Element) nCollections.item(0);
				elePaymentMethod = (Element) XPathUtil.getNode(
						eleCurrCollection, AcademyConstants.ELE_PAYMENT_METHOD);
				if(elePaymentMethod!=null)
				strCurrPaymentType = elePaymentMethod
						.getAttribute(AcademyConstants.ATTR_PAYMENT_TYPE);
				
				if (AcademyConstants.STR_CREDIT_PAYMENT_TYPE
						.equals(strCurrPaymentType)) {
					strWalletId = elePaymentMethod
							.getAttribute(AcademyConstants.ATTR_CREDIT_CARD_NO);
					docNewPaymentMethod = XMLUtil
							.createDocument(AcademyConstants.ELE_PAYMENT_METHOD);
					eleNewPaymentMethod = docNewPaymentMethod
							.getDocumentElement();
					eleNewPaymentMethod.setAttribute(
							AcademyConstants.CREDIT_CARD_NO, strWalletId);
					docWebServiceResponse = AcademyUtil
							.invokeService(
									env,
									AcademyConstants.SERVICE_GET_DECRYPTED_CREDITCARD_INFO,
									docNewPaymentMethod);
					if (!YFCObject.isVoid(docWebServiceResponse) && 
							"N".equals(docWebServiceResponse.getDocumentElement().getAttribute("HasError"))) {
						elePaymentMethod
								.setAttribute(
										AcademyConstants.ATTR_CREDIT_CARD_EXP_DATE,
										docWebServiceResponse
												.getDocumentElement()
												.getAttribute(
														AcademyConstants.ATTR_CREDIT_CARD_EXP_DATE));
						elePaymentMethod
								.setAttribute(
										AcademyConstants.ATTR_CREDIT_CARD_NO,
										docWebServiceResponse
												.getDocumentElement()
												.getAttribute(
														AcademyConstants.ATTR_CREDIT_CARD_NO));
						elePaymentMethod
								.setAttribute(
										AcademyConstants.ATTR_CREDIT_CARD_TYPE,
										docWebServiceResponse
												.getDocumentElement()
												.getAttribute(
														AcademyConstants.ATTR_CREDIT_CARD_TYPE));
						elePaymentMethod
								.setAttribute(
										AcademyConstants.ATTR_DISPLAY_CREDIT_CARD_NO,
										docWebServiceResponse
												.getDocumentElement()
												.getAttribute(
														AcademyConstants.ATTR_DISPLAY_CREDIT_CARD_NO));
						elePaymentMethod
								.setAttribute(
										AcademyConstants.ATTR_CREDIT_CARD_EXP_DATE,
										docWebServiceResponse
												.getDocumentElement()
												.getAttribute(
														AcademyConstants.ATTR_CREDIT_CARD_EXP_DATE));
					} else {
						// Raise Wallet Unavailable Alert also throw exception
						log.verbose("******* Wallet Decrypt Service Unavailable Alert raised ********");
						YFCDocument inDocToWalletAllert = YFCDocument.createDocument("Order");
						inDocToWalletAllert.getDocumentElement().setAttribute("OrderHeaderKey", 
								XPathUtil.getString(inDoc, "InvoiceDetail/InvoiceHeader/Order/@OrderHeaderKey"));
						inDocToWalletAllert.getDocumentElement().setAttribute("OrderNo", 
								XPathUtil.getString(inDoc, "InvoiceDetail/InvoiceHeader/Order/@OrderNo"));
						inDocToWalletAllert.getDocumentElement().setAttribute("CreditCardNo", 
								elePaymentMethod.getAttribute(AcademyConstants.ATTR_CREDIT_CARD_NO));
						inDocToWalletAllert.getDocumentElement().setAttribute("ErrorCode", 
								docWebServiceResponse.getDocumentElement().getAttribute("ErrorCode"));
						inDocToWalletAllert.getDocumentElement().setAttribute("ErrorDescription", 
								docWebServiceResponse.getDocumentElement().getAttribute("ErrorDescription"));
						AcademyPaymentProcessingUtil.raiseAlert(env, inDocToWalletAllert.getDocument());
					}
				}

			}
				log.endTimer(" End of AcademyUpdateInvoiceWithCreditCardInfAPI ->retrieveCreditCardInfoAndUpdateMsg Api");
		} catch (Exception e) {
						throw new YFSException(e.getMessage());
		}
		return docOutput;

	}

}
