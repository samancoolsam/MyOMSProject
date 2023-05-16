package com.academy.ecommerce.sterling.util.stub;

/**#########################################################################################
 *
 * Project Name                : Fulfillment POD
 * Author                      : Everest
 * Author Group				   : Payment
 * Date                        : 01-FEB-2023 
 * Description				   : This is a util class which handles the Stubs for Payment
 * 								 
 * ---------------------------------------------------------------------------------
 * Date            	Author         			Version#       		Remarks/Description                      
 * ---------------------------------------------------------------------------------
 * 01-FEB-2023		Everest  	 			  1.0           	Initial version
 * 08-MAR-2023		Everest  	 			  2.0           	Revised version
 *
 * #########################################################################################*/

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.json.utils.XML;
import org.apache.commons.lang3.time.DateUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.academy.ecommerce.sterling.util.AcademyRTSUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyPaymentStub {
	
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPaymentStub.class);
	YFSEnvironment env = null;
	
	/**
	 * This method contains the logic to have a payment stub logic 
	 * for the Payeezy payments 
	 * 
	 * @param strPayReq
	 * @return docOutput
	 */
	public static Document invokePayeezyPaymentStub (String strPayReq){
		
		log.beginTimer("AcademyPaymentStub: invokePayeezyPaymentStub() method");	
		log.verbose("invokePayeezyPaymentStub method input: "+strPayReq);
		String strTransactionType = null;
		Document docPayStubResponse = null;
		
		
		try {
			docPayStubResponse = XMLUtil.createDocument(AcademyConstants.STR_JSON_OBJECT);
			Document docPaymentReq = getPaymentReqXmlFromString(strPayReq);
			if(!YFCObject.isVoid(docPaymentReq)){
				Element eleRootPayReqDoc = docPaymentReq.getDocumentElement();
				strTransactionType = eleRootPayReqDoc.getAttribute(AcademyConstants.JSON_ATTR_TRANSACTION_TYPE);
				if(!YFCObject.isVoid(strTransactionType)){
					
					log.verbose("TransactionType in input: "+strTransactionType);
										
					if(AcademyConstants.TRANSACTION_TYPE_CAPTURE.equalsIgnoreCase(strTransactionType)){
						//Capture response
						getPayeezyPaymentStubResponse(docPayStubResponse, eleRootPayReqDoc, strTransactionType);
					}else if(AcademyConstants.TRANSACTION_TYPE_VOID.equalsIgnoreCase(strTransactionType)){
						//Void response
						getPayeezyPaymentStubResponse(docPayStubResponse, eleRootPayReqDoc, strTransactionType);
					}else if(AcademyConstants.TRANSACTION_TYPE_SPLIT.equalsIgnoreCase(strTransactionType)){
						//Split response
						getPayeezyPaymentStubResponse(docPayStubResponse, eleRootPayReqDoc, strTransactionType);
					}else if(AcademyConstants.TRANSACTION_TYPE_REFUND.equalsIgnoreCase(strTransactionType)){
						//refund response
						getPayeezyPaymentStubResponse(docPayStubResponse, eleRootPayReqDoc, strTransactionType);
					}else if(AcademyConstants.TRANSACTION_TYPE_AUTHORIZE.equalsIgnoreCase(strTransactionType) || 
							AcademyConstants.TRANSACTION_TYPE_PURCHASE.equalsIgnoreCase(strTransactionType)){
						//auth response
						getPayeezyPaymentStubResponseForAuthorize(docPayStubResponse, eleRootPayReqDoc, strTransactionType);
					}
				}
			}
			
			//Setting sleep time for Payeezy
			String strPayeezyTimeout = YFSSystem.getProperty("academy.payeezy.stub.timeout");
			int iTimeout = 500;
			if(!YFCObject.isVoid(strPayeezyTimeout)) {
				iTimeout = Integer.parseInt(strPayeezyTimeout);
			}
			//Setting timeout as present in properties
			setSleepTime(iTimeout);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		log.verbose("docPayStubResponse " + XMLUtil.getXMLString(docPayStubResponse));
		log.endTimer("AcademyPaymentStub: invokePayeezyPaymentStub() method");
		return docPayStubResponse;
	}
	 
	/**
	 * OMNI-99826 : START
	 * This method contains the logic to have a payment stub logic 
	 * for the RTS GC payments 
	 * 
	 * @param rtsRequestDoc
	 * @param Env
	 * @return docOutput
	 * 
	 * Input Request and response Authorization:
	 * SampleInput:
	 * <XMLAJBFipayRequest Version="1.0">
     *<IxMailOrderAVSData>*ECI</IxMailOrderAVSData>
     *<IxCmd>100</IxCmd>
     *<IxDebitCredit>GiftCard</IxDebitCredit>
     *<IxStoreNumber>005</IxStoreNumber>
     *<IxTerminalNumber>100</IxTerminalNumber>
     *<IxTranType>BalanceInquiry</IxTranType>
     *<IxDate>03072023</IxDate>
     *<IxTime>035012</IxTime>
     *<IxAccount>7777343333894695</IxAccount>
     *<IxAmount>500</IxAmount>
     *<IxInvoice>202302015</IxInvoice>
     *<IxOptions>multilock</IxOptions>
     *</XMLAJBFipayRequest>
     *Sample OutPut XML:
     *<XMLAJBFipayResponse>
     *<IxCmd>101</IxCmd>
     *<IxActionCode>0</IxActionCode>
     *<IxDebitCredit>GiftCard</IxDebitCredit>
     *<IxStoreNumber>005</IxStoreNumber>
     *<IxTerminalNumber>100</IxTerminalNumber>
     *<IxTranType>BalanceInquiry</IxTranType>
     *<IxAccount>7777343333894695</IxAccount>
     *<IxAmount>500.0</IxAmount>
     *<IxInvoice>202302015</IxInvoice>
     *<IxOptions>multilock TranLock=500.0</IxOptions>
     *<IxMailOrderAVSData>*ECI</IxMailOrderAVSData>
     *<IxReceiptDisplay>Approved </IxReceiptDisplay>
     *<IxSeqNumber>251474</IxSeqNumber>
     *<IxPostingDate>251474</IxPostingDate>
     *<IxDate>03072023</IxDate>
     *<IxTime>035012</IxTime>
     *<IxIsoResp>00</IxIsoResp>
     *<IxBankNodeID>SVDOTG1</IxBankNodeID>
     *<IxAuthResponseTime>0</IxAuthResponseTime>
     *<IxDebitComLink>00500503</IxDebitComLink>
     *<IxResponseDateTimeMilli>20230307035012</IxResponseDateTimeMilli>
     *<IxIsVoid>20230307035012</IxIsVoid>
     *<IxResponseTimeMilliSeconds>106</IxResponseTimeMilliSeconds>
     *<IxPS2000>099533</IxPS2000>
     *<IxDepositData>500.0</IxDepositData>
     *<IxIssueNumber>13</IxIssueNumber>
     *<IxAuthCode>312954</IxAuthCode>
     *</XMLAJBFipayResponse>
     *
     ** Input Request and response Charge:
	 * SampleInput:
	 * <XMLAJBFipayRequest Version="1.0">
     *<IxMailOrderAVSData>*ECI</IxMailOrderAVSData>
     *<IxCmd>100</IxCmd>
     *<IxDebitCredit>GiftCard</IxDebitCredit>
     *<IxStoreNumber>005</IxStoreNumber>
     *<IxTerminalNumber>100</IxTerminalNumber>
     *<IxTranType>Sale</IxTranType>
     *<IxDate>03072023</IxDate>
     *<IxTime>042816</IxTime>
     *<IxAccount>7777343333894695</IxAccount>
     *<IxAmount>500</IxAmount>
     *<IxInvoice>202302015</IxInvoice>
     *<IxIssueNumber>13</IxIssueNumber>
     *<IxOptions>multilock</IxOptions>
     *</XMLAJBFipayRequest>
     *
     *Sample Output:
     *<XMLAJBFipayResponse>
     * <IxCmd>101</IxCmd>
     *<IxActionCode>0</IxActionCode>
     *<IxDebitCredit>GiftCard</IxDebitCredit>
     *<IxStoreNumber>005</IxStoreNumber>
     *<IxTerminalNumber>100</IxTerminalNumber>
     *<IxTranType>Sale</IxTranType>
     *<IxAccount>7777343333894695</IxAccount>
     *<IxAmount>500.0</IxAmount>
     *<IxInvoice>202302015</IxInvoice>
     *<IxOptions>multilock TranLock=500.0</IxOptions>
     *<IxMailOrderAVSData>*ECI</IxMailOrderAVSData>
     *<IxReceiptDisplay>Approved </IxReceiptDisplay>
     *<IxSeqNumber>251474</IxSeqNumber>
     *<IxPostingDate>251474</IxPostingDate>
     *<IxDate>03072023</IxDate>
     *<IxTime>042816</IxTime>
     *<IxIsoResp>00</IxIsoResp>
     *<IxBankNodeID>SVDOTG1</IxBankNodeID>
     *<IxAuthResponseTime>0</IxAuthResponseTime>
     *<IxDebitComLink>00500503</IxDebitComLink>
     *<IxResponseDateTimeMilli>20230307042816</IxResponseDateTimeMilli>
     *<IxIsVoid>20230307042816</IxIsVoid>
     *<IxResponseTimeMilliSeconds>106</IxResponseTimeMilliSeconds>
     *<IxPS2000>099533</IxPS2000>
     *<IxDepositData>500.0</IxDepositData>
     *<IxIssueNumber>13</IxIssueNumber>
     *<IxAuthCode>366515</IxAuthCode>
     *</XMLAJBFipayResponse>
     *
     *Sample input output for Reversal
     *RequestXML
     * <XMLAJBFipayRequest Version="1.0">
     *<IxMailOrderAVSData>*ECI</IxMailOrderAVSData>
     *<IxCmd>100</IxCmd>
     *<IxDebitCredit>GiftCard</IxDebitCredit>
     *<IxStoreNumber>005</IxStoreNumber>
     *<IxTerminalNumber>100</IxTerminalNumber>
     *<IxTranType>BalanceInquiry</IxTranType>
     *<IxDate>03082023</IxDate>
     *<IxTime>014542</IxTime>
     *<IxAccount>7777343333894695</IxAccount>
     *<IxAmount>500</IxAmount>
     *<IxInvoice>202302018</IxInvoice>
     *<IxOptions>multilock</IxOptions>
     *</XMLAJBFipayRequest>
     *
     *Response XML:
     *<XMLAJBFipayResponse>
     *<IxCmd>101</IxCmd>
     *<IxActionCode>0</IxActionCode>
     *<IxDebitCredit>GiftCard</IxDebitCredit>
     *<IxStoreNumber>005</IxStoreNumber>
     *<IxTerminalNumber>100</IxTerminalNumber>
     *<IxTranType>BalanceInquiry</IxTranType>
     *<IxAccount>7777343333894695</IxAccount>
     *<IxAmount>500.0</IxAmount>
     *<IxInvoice>202302018</IxInvoice>
     *<IxOptions>multilock TranLock=500.0</IxOptions>
     *<IxMailOrderAVSData>*ECI</IxMailOrderAVSData>
     *<IxReceiptDisplay>Approved </IxReceiptDisplay>
     *<IxSeqNumber>251474</IxSeqNumber>
     *<IxPostingDate>251474</IxPostingDate>
     *<IxDate>03082023</IxDate>
     *<IxTime>014542</IxTime>
     *<IxIsoResp>00</IxIsoResp>
     *<IxBankNodeID>SVDOTG1</IxBankNodeID>
     *<IxAuthResponseTime>0</IxAuthResponseTime>
     *<IxDebitComLink>00500503</IxDebitComLink>
     *<IxResponseDateTimeMilli>20230308014542</IxResponseDateTimeMilli>
     *<IxIsVoid>20230308014542</IxIsVoid>
     *<IxResponseTimeMilliSeconds>106</IxResponseTimeMilliSeconds>
     *<IxPS2000>099533</IxPS2000>
     *<IxDepositData>500.0</IxDepositData>
     *<IxIssueNumber>13</IxIssueNumber>
     *<IxAuthCode>869375</IxAuthCode>
     *</XMLAJBFipayResponse>
     * 
	 */
	public static Document invokeRTSPaymentStubForGC(YFSEnvironment env,Document rtsRequestDoc, Document GCInputDoc){
		
		log.beginTimer("AcademyPaymentStub: invokeRTSPaymentStubForGC() method");	
		log.verbose("invokeRTSPaymentStubForGC method GCInputDoc: "+XMLUtil.getXMLString(GCInputDoc));
		log.verbose("invokeRTSPaymentStubForGC method inputRequestDoc: "+XMLUtil.getXMLString(rtsRequestDoc));		
		String chargeType = null;
		Document Responsedoc = null;
		try {
			if(!YFCObject.isVoid(rtsRequestDoc) && !YFCObject.isVoid(GCInputDoc) )
			{
				chargeType = GCInputDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_CHARGE_TYPE);				
				log.verbose("chargeType : "+chargeType);
				String strTransactionType = rtsRequestDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_TRAN_TYPE).item(0).getTextContent();
				log.verbose("TransactionType : "+strTransactionType);
				Responsedoc = XMLUtil.getDocument("<XMLAJBFipayResponse>\r\n"
						+ "	<IxCmd>101</IxCmd>\r\n"
						+ "	<IxActionCode>0</IxActionCode>   \r\n"
						+ "	<IxDebitCredit>GiftCard</IxDebitCredit>\r\n"
						+ "	<IxStoreNumber></IxStoreNumber>\r\n"
						+ "	<IxTerminalNumber>100</IxTerminalNumber>\r\n"
						+ "	<IxTranType>BalanceInquiry</IxTranType>\r\n"
						+ "	<IxAccount>7777343333894695</IxAccount>\r\n"
						+ "	<IxAmount>4163</IxAmount>\r\n"
						+ "	<IxInvoice>GC2023021503</IxInvoice>\r\n"
						+ "	<IxOptions></IxOptions>\r\n"
						+ "	<IxMailOrderAVSData>*ECI</IxMailOrderAVSData>\r\n"
						+ "	<IxReceiptDisplay>Approved </IxReceiptDisplay>\r\n"
						+ "	<IxSeqNumber>251474</IxSeqNumber>\r\n" 
						+ " <IxPostingDate>251474</IxPostingDate>\r\n" 
						+ "	<IxDate>02152023</IxDate>\r\n"
						+ "	<IxTime>030924</IxTime>\r\n"
						+ "	<IxIsoResp>00</IxIsoResp>\r\n"
						+ "	<IxBankNodeID>SVDOTG1</IxBankNodeID>\r\n" 
						+ "	<IxAuthResponseTime>0</IxAuthResponseTime>\r\n"
						+ "	<IxDebitComLink>00500503</IxDebitComLink>	\r\n" 
						+ "	<IxResponseDateTimeMilli>031038660</IxResponseDateTimeMilli>\r\n"
						+ "	<IxIsVoid>12304603103803750831</IxIsVoid>\r\n"
						+ "	<IxResponseTimeMilliSeconds>106</IxResponseTimeMilliSeconds>	\r\n"
						+ "	<IxPS2000>099533</IxPS2000>\r\n"
						+ "	<IxDepositData>150000</IxDepositData>\r\n"
						+ "	<IxIssueNumber>13</IxIssueNumber>\r\n"
						+ "	<IxAuthCode>519582</IxAuthCode>\r\n"
						+ "</XMLAJBFipayResponse>");	
				if(!YFCObject.isVoid(strTransactionType) && !YFCObject.isVoid(chargeType) ){
					Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_STORE_NUM).item(0).setTextContent(
							rtsRequestDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_STORE_NUM).item(0).getTextContent());
					Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_TRAN_TYPE).item(0).setTextContent(
							rtsRequestDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_TRAN_TYPE).item(0).getTextContent());
					Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_ACCOUNT).item(0).setTextContent(
							rtsRequestDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_ACCOUNT).item(0).getTextContent());
					Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_INVOICE).item(0).setTextContent(
							rtsRequestDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_INVOICE).item(0).getTextContent());
				
					Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_MAIL_ORDER_AVS_DATA).item(0).setTextContent(
							rtsRequestDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_MAIL_ORDER_AVS_DATA).item(0).getTextContent());
					
					String strAmount = String.valueOf(Double.parseDouble(rtsRequestDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_AMOUNT).item(0).getTextContent()));
					log.verbose("doc-> strAmount :: " + strAmount);
					Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_AMOUNT).item(0).setTextContent(strAmount);		    
					Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_DEPOSIT_DATA).item(0).setTextContent(strAmount);
					//Assuming Success Auth wont get response with TranLock=0, in that case we will be wrongly reading that scenario as partial auth
					String strIxOptions=rtsRequestDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_OPTIONS).item(0).getTextContent()
							+" "+ "TranLock=" +strAmount; 
						Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_OPTIONS).item(0).setTextContent(strIxOptions);
					
					/*NodeList nlCreditCardTransaction = XPathUtil.getNodeList(GCInputDoc.getDocumentElement(), 
							"CreditCardTransactions/CreditCardTransaction");
					 Element eleCreditCardTransaction = (Element) nlCreditCardTransaction.item(0);*/
					
					 String authCode = AcademyRTSUtil.randomstring(6, 6);
					 log.verbose("AuthCode is::"+authCode);
					 Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_AUTH_CODE).item(0).setTextContent(authCode);
					 
					
					Calendar cal = Calendar.getInstance();		
					Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_DATE).item(0).setTextContent(new SimpleDateFormat("MMddyyyy").format(cal.getTime()));
					Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_TIME).item(0).setTextContent(new SimpleDateFormat("HHmmss").format(cal.getTime()));
					Responsedoc.getDocumentElement().getElementsByTagName("IxResponseDateTimeMilli").item(0).setTextContent(getCurrentDateAndTime());
					Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_IS_VOID).item(0).setTextContent(getCurrentDateAndTime());
					
					if(AcademyConstants.STR_CHRG_TYPE_CHARGE.equals(chargeType) && strTransactionType.equals(AcademyConstants.RTS_REQ_TRAN_TYPE_SALE))
					{
						log.verbose("Inside GC Settlement Code");
						Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_ISSUE_NUM).item(0).setTextContent(
								rtsRequestDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_ISSUE_NUM).item(0).getTextContent());
						log.verbose("invokeRTSPaymentStubForGC method ResponseForSettlment: "+XMLUtil.getXMLString(Responsedoc));
						
					}else if(AcademyConstants.STR_CHRG_TYPE_AUTH.equals(chargeType) && strTransactionType.equals(AcademyConstants.RTS_REQ_TRAN_TYPE_SALE))
					{
						log.verbose("Inside GC Auth Reversal Code");
						Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_ISSUE_NUM).item(0).setTextContent(
								rtsRequestDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_ISSUE_NUM).item(0).getTextContent());
						log.verbose("invokeRTSPaymentStubForGC method ResponseForAuthReversal: "+XMLUtil.getXMLString(Responsedoc));
					}else if(AcademyConstants.STR_CHRG_TYPE_AUTH.equals(chargeType) && strTransactionType.equals(AcademyConstants.STR_RTS_BALANCE_INQUIRY_TRAN_TYPE))
					{
						log.verbose("Inside GC Reauthorization Code");
						Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_ISSUE_NUM).item(0).setTextContent("01");
						log.verbose("invokeRTSPaymentStubForGC method ResponseForReauthorization: "+XMLUtil.getXMLString(Responsedoc));						
					}		
				}		
			}	
			//Setting sleep time for Payeezy
			String strRTSTimeout = YFSSystem.getProperty("academy.rts.stub.timeout");
			int iTimeout = 500;
			if(!YFCObject.isVoid(strRTSTimeout)) {
				iTimeout = Integer.parseInt(strRTSTimeout);
			}
			//Setting timeout as present in properties
			setSleepTime(iTimeout);
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		log.verbose("docPayStubResponse " + XMLUtil.getXMLString(Responsedoc));
		log.endTimer("AcademyPaymentStub: invokePayeezyPaymentStub() method");
		return Responsedoc;
	}
	//OMNI-99826 END
	/**
	 * This method converts the input document from string to xml format.
	 * Sample input:
	 * {"amount":"5411","transaction_tag":"10048226378","merchant_ref":"CC786696641","transaction_type":"capture",
	 * "reversal_id":"b03907cf-1ec5-4f53-8bbf-6762ce629608","currency_code":"USD"}
	 * 
	 * Sample output:
	 * <jsonObject amount="5411" currency_code="USD" merchant_ref="CC786696641" reversal_id="b03907cf-1ec5-4f53-8bbf-6762ce629608" 
	 * transaction_tag="10048226378" transaction_type="capture"/>
	 * 
	 * @param strPayReq
	 * @return
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private static Document getPaymentReqXmlFromString (String strPayReq) throws IOException, ParserConfigurationException, SAXException{
		
		log.beginTimer("AcademyPaymentStub: getPaymentReqXmlFromString() method" );
		InputStream in = IOUtils.toInputStream(strPayReq);
		String xml = XML.toXml(in);	
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		String FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
		dbf.setFeature(FEATURE, true);
		dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		DocumentBuilder builder = dbf.newDocumentBuilder();
		Document docPayReq = builder.parse(new InputSource(new StringReader(xml)));
		
		log.verbose("docPayReq: "+XMLUtil.getXMLString(docPayReq));
		log.endTimer("AcademyPaymentStub:getPaymentReqXmlFromString() method" );
		return docPayReq;
	}
	
	/**
	 * This method returns the stub response for different Transaction Type
	 * Sample input Capture:
	 * <jsonObject amount="5411" currency_code="USD" merchant_ref="CC786696641" reversal_id="b03907cf-1ec5-4f53-8bbf-6762ce629608" 
	 * transaction_tag="10048226378" transaction_type="capture"/>
	 * 
	 * Sample output Capture:
	 * <jsonObject HttpStatusCode="201" amount="54.11" bank_message="Approved"
	    bank_resp_code="100" correlation_id="2023020711514700"
	    currency="USD" gateway_message="Transaction Normal"
	    gateway_resp_code="00"
	    reversal_id="b03907cf-1ec5-4f53-8bbf-6762ce629608"
	    transaction_id="2023020711514701" transaction_status="approved"
	    transaction_tag="10048226378" transaction_type="capture" validation_status="success"/>
	    
	 * Sample input Void:
	 * <jsonObject amount="5411" currency_code="USD" merchant_ref="CC786696634" transaction_tag="10048180775" transaction_type="void"/>
	 * 
	 * Sample output Void:
	 * <jsonObject HttpStatusCode="201" amount="54.11" bank_message="Approved"
	    bank_resp_code="100" correlation_id="2023020711500200"
	    currency="USD" gateway_message="Transaction Normal"
	    gateway_resp_code="00" reversal_id=""
	    transaction_id="2023020711500201" transaction_status="approved"
	    transaction_tag="10048180775" transaction_type="void" validation_status="success"/>
	    
	 * Sample input Split Shipment:
	 * 1st charge:-
	 * <jsonObject amount="3246" currency_code="USD" merchant_ref="CC0786696646" reversal_id="1df3a836-a18f-48d7-960a-cd57411b0009" split_shipment="1/99" 
	 * transaction_tag="10048228459" transaction_type="split"/>
	 * 
	 * Last charge:-
	 * <jsonObject amount="2922" currency_code="USD" merchant_ref="CC1786696642" reversal_id="df629a61-8dcb-4083-820f-1905db691cee" split_shipment="2/2" 
	 * transaction_tag="10048183925" transaction_type="split"/>
	 * 
	 * Sample output Split Shipment:
	 * <jsonObject HttpStatusCode="201" amount="32.46" bank_message="Approved"
	    bank_resp_code="100" correlation_id="2023020711543700"
	    currency="USD" gateway_message="Transaction Normal"
	    gateway_resp_code="00" transaction_id="2023020711543701"
	    transaction_status="approved" transaction_tag="10048228459"
	    transaction_type="split" validation_status="success"/>
	    
	    
	 * Sample input Refund:
	 * <jsonObject amount="5411" currency_code="USD" merchant_ref="CC7866976290" reversal_id="0d7059ee-89a6-431c-87cd-76bb351cf170" 
	 * transaction_tag="10048210969" transaction_type="refund"/>
	 * 
	 * Sample output Refund:
	 * <jsonObject HttpStatusCode="201" amount="54.11" bank_message="Approved"
	    bank_resp_code="100" correlation_id="2023020711584200"
	    currency="USD" gateway_message="Transaction Normal"
	    gateway_resp_code="00"
	    reversal_id="0d7059ee-89a6-431c-87cd-76bb351cf170"
	    transaction_id="RETURN" transaction_status="approved"
	    transaction_tag="10048210969" transaction_type="refund" validation_status="success"/>
	    
	 * @param docPayStubResponse
	 * @param eleRootPayReqDoc
	 * @param strTransactionType
	 * @return
	 */
	private static Document getPayeezyPaymentStubResponse(Document docPayStubResponse, Element eleRootPayReqDoc, String strTransactionType){
		
		log.beginTimer("AcademyPaymentStub: getPayeezyPaymentStubResponse() method");
		
		Element eleRootPayStubResponse = docPayStubResponse.getDocumentElement();		
		eleRootPayStubResponse.setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, AcademyConstants.STATUS_CODE_201);
		updateAmountInDecimal(eleRootPayStubResponse, eleRootPayReqDoc);
		eleRootPayStubResponse.setAttribute(AcademyConstants.JSON_ATTR_BANK_MESSAGE, AcademyConstants.BANK_MSG_APPROVED);
		eleRootPayStubResponse.setAttribute(AcademyConstants.JSON_ATTR_BANK_RESP_CODE, AcademyConstants.ATTR_HUNDRED);
		eleRootPayStubResponse.setAttribute(AcademyConstants.JSON_ATTR_CORRELATION_ID, getCurrentDateAndTime() + "00");//Validate
		eleRootPayStubResponse.setAttribute(AcademyConstants.CURRENCY, eleRootPayReqDoc.getAttribute(AcademyConstants.JSON_ATTR_CURRENCY_CODE));
		eleRootPayStubResponse.setAttribute(AcademyConstants.JSON_ATTR_GATEWAY_MESSAGE, AcademyConstants.ATTR_TRANS_NORMAL);
		eleRootPayStubResponse.setAttribute(AcademyConstants.JSON_ATTR_GATEWAY_RESP_CODE, AcademyConstants.STR_PAYZ_APPROVED);
		eleRootPayStubResponse.setAttribute(AcademyConstants.JSON_ATTR_REVERSAL_ID, eleRootPayReqDoc.getAttribute(AcademyConstants.JSON_ATTR_REVERSAL_ID));
		eleRootPayStubResponse.setAttribute(AcademyConstants.JSON_ATTR_TRANSACTION_ID, getCurrentDateAndTime() + "01");//Validate
		eleRootPayStubResponse.setAttribute(AcademyConstants.JSON_ATTR_TRANSACTION_STATUS, AcademyConstants.TRAN_STATUS_APPROVED);
		eleRootPayStubResponse.setAttribute(AcademyConstants.JSON_ATTR_TRANSACTION_TAG, eleRootPayReqDoc.getAttribute(AcademyConstants.JSON_ATTR_TRANSACTION_TAG));
		eleRootPayStubResponse.setAttribute(AcademyConstants.JSON_ATTR_TRANSACTION_TYPE, strTransactionType);
		eleRootPayStubResponse.setAttribute(AcademyConstants.JSON_ATTR_VALIDATION_STATUS, AcademyConstants.ATTR_SUCCESS);
		
		
		log.endTimer("AcademyPaymentStub: getPayeezyPaymentStubResponse() method");		
		return docPayStubResponse;
	}
	

	/**
	 * This method returns the stub response for the transaction type: auth	 * 
	 * Sample input:
	 * <jsonObject amount="5411" currency_code="USD" merchant_ref="CC786696641" method="token" reversal_id="669d2c9e-1590-43bb-ba88-a44547e4439e" 
	 * transaction_type="authorize">
	 * <token token_type="FDToken">
	 * <token_data cardholder_name="Nandhini R" exp_date="0330" type="Visa" value="8805774950981111"/>
	 * </token>
	 * </jsonObject>
	 * 
	 * Sample output:
	 * <jsonObject HttpStatusCode="201" amount="54.11" bank_message="Approved"
		    bank_resp_code="100" correlation_id="2023020711460700"
		    currency="USD" cvv2="I" gateway_message="Transaction Normal"
		    gateway_resp_code="00" method="token"
		    transaction_id="2023020711460701" transaction_status="approved"
		    transaction_tag="2023020711460702" transaction_type="authorize" validation_status="success">
		    <token token_type="FDToken">
		        <token_data cardholder_name="Nandhini R" exp_date="0330"
		            type="Visa" value="8805774950981111"/>
		    </token>
		</jsonObject>
	 * 
	 * @param docPayStubResponse
	 * @param eleRootPayReqDoc
	 * @param strTransactionType
	 * @return
	 */
	private static Document getPayeezyPaymentStubResponseForAuthorize(Document docPayStubResponse, Element eleRootPayReqDoc, String strTransactionType){
		
		log.beginTimer("AcademyPaymentStub: getPayeezyPaymentStubResponseForAuthorize or Purchase() method");
		
		Element eleRootPayStubResponse = docPayStubResponse.getDocumentElement();		
		eleRootPayStubResponse.setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, AcademyConstants.STATUS_CODE_201);
		updateAmountInDecimal(eleRootPayStubResponse, eleRootPayReqDoc);
		eleRootPayStubResponse.setAttribute(AcademyConstants.JSON_ATTR_BANK_MESSAGE, AcademyConstants.BANK_MSG_APPROVED);
		eleRootPayStubResponse.setAttribute(AcademyConstants.JSON_ATTR_BANK_RESP_CODE, AcademyConstants.ATTR_HUNDRED);
		eleRootPayStubResponse.setAttribute(AcademyConstants.JSON_ATTR_CORRELATION_ID, getCurrentDateAndTime() + "00");//Validate
		eleRootPayStubResponse.setAttribute(AcademyConstants.CURRENCY, eleRootPayReqDoc.getAttribute(AcademyConstants.JSON_ATTR_CURRENCY_CODE));
		eleRootPayStubResponse.setAttribute(AcademyConstants.CVV2, AcademyConstants.STR_I);
		eleRootPayStubResponse.setAttribute(AcademyConstants.JSON_ATTR_GATEWAY_MESSAGE, AcademyConstants.ATTR_TRANS_NORMAL);
		eleRootPayStubResponse.setAttribute(AcademyConstants.JSON_ATTR_GATEWAY_RESP_CODE, AcademyConstants.STR_PAYZ_APPROVED);
		eleRootPayStubResponse.setAttribute(AcademyConstants.JSON_ATTR_METHOD, eleRootPayReqDoc.getAttribute(AcademyConstants.JSON_ATTR_METHOD));		
		eleRootPayStubResponse.setAttribute(AcademyConstants.JSON_ATTR_TRANSACTION_ID, getCurrentDateAndTime() + "01");//Validate
		eleRootPayStubResponse.setAttribute(AcademyConstants.JSON_ATTR_TRANSACTION_STATUS, AcademyConstants.TRAN_STATUS_APPROVED);
		eleRootPayStubResponse.setAttribute(AcademyConstants.JSON_ATTR_TRANSACTION_TAG, getCurrentDateAndTime() + "02");
		eleRootPayStubResponse.setAttribute(AcademyConstants.JSON_ATTR_TRANSACTION_TYPE, strTransactionType);
		eleRootPayStubResponse.setAttribute(AcademyConstants.JSON_ATTR_VALIDATION_STATUS, AcademyConstants.ATTR_SUCCESS);
					
		Element eletokenIn = (Element) eleRootPayReqDoc.getElementsByTagName(AcademyConstants.STR_TOKEN).item(0);
		Element eletoken = (Element) docPayStubResponse.importNode(eletokenIn, true);
		eleRootPayStubResponse.appendChild(eletoken);
		
		log.endTimer("AcademyPaymentStub: getPayeezyPaymentStubResponseForAuthorize or Purchase() method");		
		return docPayStubResponse;
	}
	
	/**This method updates the amount in decimal format.
	 * 
	 * @param eleRootPayStubResponse
	 * @param eleRootPayReqDoc
	 */
	private static void updateAmountInDecimal(Element eleRootPayStubResponse, Element eleRootPayReqDoc){
		
		log.beginTimer("AcademyPaymentStub: getAmountInDecimal() method");		
		
		String strAmount = eleRootPayReqDoc.getAttribute(AcademyConstants.JSON_ATTR_AMOUNT);
		strAmount = Double.toString((Double.parseDouble(strAmount)/100));
		log.verbose("Amount in decimal: " + strAmount);
		eleRootPayStubResponse.setAttribute(AcademyConstants.JSON_ATTR_AMOUNT, strAmount);
		
		log.endTimer("AcademyPaymentStub: getAmountInDecimal() method");
		
	}
	
	/**This method gets the current Date and time
	 * 
	 * @return
	 */
	private static String getCurrentDateAndTime(){
		
		log.beginTimer("AcademyPaymentStub: getCurrentDateAndTime() method");
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");  
		LocalDateTime now = LocalDateTime.now();  
		String strCurrentDateTimeVal = dtf.format(now);
		log.verbose("strCurrentDateTimeVal: " + strCurrentDateTimeVal);
		
		log.endTimer("AcademyPaymentStub: getCurrentDateAndTime() method");
		return strCurrentDateTimeVal;
	}
	
	
	/**Adds sleep time
	 * 
	 * @return
	 * @throws Exception 
	 */
	private static void setSleepTime(int iMilliSeconds) throws Exception{
		log.beginTimer("AcademyPaymentStub: setSleepTime() method");
		TimeUnit.MILLISECONDS.sleep(iMilliSeconds);
		log.endTimer("AcademyPaymentStub: setSleepTime() method");
	}
	
	


	/**
	 * OMNI-99827 : START
	 * This method contains the logic to have a payment stub logic 
	 * for the RTS PLCC payments 
	 * 
	 * @param rtsRequestDoc
	 * @param Env
	 * @return docOutput
	 * Different transaction types:
	 * PreAuth [Auth with reqAmt +ve] - ReAuth
	   VoidPreAuth [Chartype is Auth and RequestAmt is -ve][Void]
	   Refund [Refund]
       PreAuthComp [Settlement-Capture call]
       Sale [No Valid auth available]
     **Input Request and response Charge:
	 * SampleInput:
	<XMLAJBFipayRequest>
    <IxCmd>100</IxCmd>
    <IxDebitCredit>CREDIT</IxDebitCredit>
    <IxStoreNumber>005</IxStoreNumber>
    <IxTerminalNumber>300</IxTerminalNumber>
    <IxTranType>VoidPreAuth</IxTranType>
    <IxDate>03072023</IxDate>
    <IxTime>011354</IxTime>
    <IxAccount>************2829</IxAccount>
    <IxPosEchoField>2669832804232829</IxPosEchoField>
    <IxAmount>4949</IxAmount>
    <IxInvoice>PLCC003</IxInvoice>
    <IxOptions>Privatelabel *StoreStan 120209 *Tokenization</IxOptions>
	<IxNeedsReversal>1200&amp;amp;amp;#x1C;011006&amp;amp;amp;#x1C;230206015357&amp;amp;amp;#x1C;000000215952&amp;amp;amp;#x1C;006647</IxNeedsReversal>
    <IxMailOrderAVSData>*ECI</IxMailOrderAVSData>
	</XMLAJBFipayRequest>
     *
     *Sample Output:
    <XMLAJBFipayResponse>
    <IxCmd>101</IxCmd>
    <IxActionCode>0</IxActionCode>
    <IxDebitCredit>CREDIT</IxDebitCredit>
    <IxStoreNumber>005</IxStoreNumber>
    <IxTerminalNumber>100</IxTerminalNumber>
    <IxTranType>VoidPreAuth</IxTranType>
    <IxAccount>************2829</IxAccount>
    <IxAmount>4949.0</IxAmount>
    <IxInvoice>PLCC003</IxInvoice>
    <IxOptions>Privatelabel *StoreStan 120209 *Tokenization TranLock=4949.0</IxOptions>
    <IxMailOrderAVSData>*ECI</IxMailOrderAVSData>
    <IxReceiptDisplay>Approved </IxReceiptDisplay>
    <IxSeqNumber>251474</IxSeqNumber>
    <IxPostingDate>251474</IxPostingDate>
    <IxDate>03072023</IxDate>
    <IxTime>011354</IxTime>
    <IxIsoResp>00</IxIsoResp>
    <IxBankNodeID>SVDOTG1</IxBankNodeID>
    <IxAuthResponseTime>0</IxAuthResponseTime>
    <IxDebitComLink>00500503</IxDebitComLink>
    <IxResponseDateTimeMilli>20230307011354</IxResponseDateTimeMilli>
    <IxIsVoid>20230307011354</IxIsVoid>
    <IxResponseTimeMilliSeconds>106</IxResponseTimeMilliSeconds>
    <IxPS2000>099533</IxPS2000>
    <IxDepositData>4949.0</IxDepositData>
    <IxIssueNumber>13</IxIssueNumber>
    <IxAuthCode>927844</IxAuthCode>
	</XMLAJBFipayResponse>
     */
	public static Document invokeRTSPaymentStubForPLCC(YFSEnvironment env,Document docRTSRequest, Document docPaymentIn){
		
		log.beginTimer("AcademyPaymentStub: invokeRTSPaymentStubForPLCC() method");	
		log.verbose("invokeRTSPaymentStubForPLCC method PLCCInputDoc: "+XMLUtil.getXMLString(docPaymentIn));
		log.verbose("invokeRTSPaymentStubForPLCC method inputRequestDoc: "+XMLUtil.getXMLString(docRTSRequest));		
		String chargeType = null;
		Document Responsedoc = null;
		try {
			if(!YFCObject.isVoid(docRTSRequest) && !YFCObject.isVoid(docPaymentIn) )
			{
				chargeType = docPaymentIn.getDocumentElement().getAttribute(AcademyConstants.ATTR_CHARGE_TYPE);				
				log.verbose("chargeType : "+chargeType);
				String strTransactionType = docRTSRequest.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_TRAN_TYPE).item(0).getTextContent();
				log.verbose("TransactionType : "+strTransactionType);
				Responsedoc = XMLUtil.getDocument("<XMLAJBFipayResponse>\r\n"
						+ "	<IxCmd>101</IxCmd>\r\n"
						+ "	<IxActionCode>0</IxActionCode>   \r\n"
						+ "	<IxDebitCredit>CREDIT</IxDebitCredit>\r\n"
						+ "	<IxStoreNumber>005</IxStoreNumber>\r\n"
						+ "	<IxTerminalNumber>100</IxTerminalNumber>\r\n"
						+ "	<IxTranType>BalanceInquiry</IxTranType>\r\n"
						+ "	<IxAccount>7777343333894695</IxAccount>\r\n"
						+ "	<IxAmount>4163</IxAmount>\r\n"
						+ "	<IxInvoice>GC2023021503</IxInvoice>\r\n"
						+ "	<IxOptions></IxOptions>\r\n"
						+ "	<IxMailOrderAVSData>*ECI</IxMailOrderAVSData>\r\n"
						+ "	<IxReceiptDisplay>Approved </IxReceiptDisplay>\r\n"
						+ "	<IxSeqNumber>251474</IxSeqNumber>\r\n" 
						+ " <IxPostingDate>251474</IxPostingDate>\r\n" 
						+ "	<IxDate>02152023</IxDate>\r\n"
						+ "	<IxTime>030924</IxTime>\r\n"
						+ "	<IxIsoResp>00</IxIsoResp>\r\n"
						+ "	<IxBankNodeID>SVDOTG1</IxBankNodeID>\r\n" 
						+ "	<IxAuthResponseTime>0</IxAuthResponseTime>\r\n"
						+ "	<IxDebitComLink>00500503</IxDebitComLink>	\r\n" 
						+ "	<IxResponseDateTimeMilli>031038660</IxResponseDateTimeMilli>\r\n"
						+ "	<IxIsVoid>12304603103803750831</IxIsVoid>\r\n"
						+ "	<IxResponseTimeMilliSeconds>106</IxResponseTimeMilliSeconds>	\r\n"
						+ "	<IxPS2000>099533</IxPS2000>\r\n"
						+ "	<IxDepositData>150000</IxDepositData>\r\n"
						+ "	<IxIssueNumber>13</IxIssueNumber>\r\n"
						+ "	<IxAuthCode>519582</IxAuthCode>\r\n"
						+ "</XMLAJBFipayResponse>");	
				if(!YFCObject.isVoid(strTransactionType) && !YFCObject.isVoid(chargeType) ){
					Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_STORE_NUM).item(0).setTextContent(
							docRTSRequest.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_STORE_NUM).item(0).getTextContent());
					Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_TRAN_TYPE).item(0).setTextContent(
							docRTSRequest.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_TRAN_TYPE).item(0).getTextContent());
					Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_ACCOUNT).item(0).setTextContent(
							docRTSRequest.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_ACCOUNT).item(0).getTextContent());
					Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_INVOICE).item(0).setTextContent(
							docRTSRequest.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_INVOICE).item(0).getTextContent());
				
					Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_MAIL_ORDER_AVS_DATA).item(0).setTextContent(
							docRTSRequest.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_MAIL_ORDER_AVS_DATA).item(0).getTextContent());
					
					String strAmount = String.valueOf(Double.parseDouble(docRTSRequest.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_AMOUNT).item(0).getTextContent()));
					log.verbose("doc-> strAmount :: " + strAmount);
					Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_AMOUNT).item(0).setTextContent(strAmount);		    
					Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_DEPOSIT_DATA).item(0).setTextContent(strAmount);
					//Assuming Success Auth wont get response with TranLock=0, in that case we will be wrongly reading that scenario as partial auth
					String strIxOptions=docRTSRequest.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_OPTIONS).item(0).getTextContent()
							+" "+ "TranLock=" +strAmount; 
						Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_OPTIONS).item(0).setTextContent(strIxOptions);
					
					/*NodeList nlCreditCardTransaction = XPathUtil.getNodeList(GCInputDoc.getDocumentElement(), 
							"CreditCardTransactions/CreditCardTransaction");
					 Element eleCreditCardTransaction = (Element) nlCreditCardTransaction.item(0);*/
					
					 String authCode = AcademyRTSUtil.randomstring(6, 6);
					 log.verbose("AuthCode is::"+authCode);
					 Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_AUTH_CODE).item(0).setTextContent(authCode);
					 
					
					Calendar cal = Calendar.getInstance();		
					Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_DATE).item(0).setTextContent(new SimpleDateFormat("MMddyyyy").format(cal.getTime()));
					Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_TIME).item(0).setTextContent(new SimpleDateFormat("HHmmss").format(cal.getTime()));
					Responsedoc.getDocumentElement().getElementsByTagName("IxResponseDateTimeMilli").item(0).setTextContent(getCurrentDateAndTime());
					Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_IS_VOID).item(0).setTextContent(getCurrentDateAndTime());
					
				
					if(AcademyConstants.STR_CHRG_TYPE_CHARGE.equals(chargeType) && AcademyConstants.ATTR_TRANSACTIONTYPE.equals(AcademyConstants.RTS_REQ_TRAN_TYPE_PRE_AUTH_COMP))
					{
						log.verbose("Inside PLCC Settlement Code");
						Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_ISSUE_NUM).item(0).setTextContent(
								docRTSRequest.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_ISSUE_NUM).item(0).getTextContent());
						log.verbose("invokeRTSPaymentStubForPLCC method ResponseForSettlement: "+XMLUtil.getXMLString(Responsedoc));
						
					}else if(AcademyConstants.STR_CHRG_TYPE_AUTH.equals(chargeType) && AcademyConstants.ATTR_TRANSACTIONTYPE.equals(AcademyConstants.RTS_REQ_TRAN_TYPE_VOID))
					{
						log.verbose("Inside PLCC Auth Reversal Code");
						Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_ISSUE_NUM).item(0).setTextContent(
								docRTSRequest.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_ISSUE_NUM).item(0).getTextContent());
						log.verbose("invokeRTSPaymentStubForPLCC method ResponseForAuthReversal: "+XMLUtil.getXMLString(Responsedoc));
					}else if(AcademyConstants.STR_CHRG_TYPE_AUTH.equals(chargeType) && AcademyConstants.ATTR_TRANSACTIONTYPE.equals(AcademyConstants.RTS_REQ_TRAN_TYPE_PRE_AUTH))
					{
						log.verbose("Inside PLCC Reauthorization Code");
						Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_ISSUE_NUM).item(0).setTextContent("01");
						log.verbose("invokeRTSPaymentStubForPLCC method ResponseForReauthorization: "+XMLUtil.getXMLString(Responsedoc));						
					}else if(AcademyConstants.STR_CHRG_TYPE_CHARGE.equals(chargeType) && AcademyConstants.ATTR_TRANSACTIONTYPE.equals(AcademyConstants.RTS_REQ_TRAN_TYPE_REFUND))
					{
						log.verbose("Inside PLCC Refund Code");
						Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_ISSUE_NUM).item(0).setTextContent(
								docRTSRequest.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_ISSUE_NUM).item(0).getTextContent());
						log.verbose("invokeRTSPaymentStubForPLCC method ResponseForRefund: "+XMLUtil.getXMLString(Responsedoc));
						
					}else if(AcademyConstants.STR_CHRG_TYPE_CHARGE.equals(chargeType) && AcademyConstants.ATTR_TRANSACTIONTYPE.equals(AcademyConstants.RTS_REQ_TRAN_TYPE_SALE))
					{
						log.verbose("Inside PLCC Sale-purchase Code");
						Responsedoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_ISSUE_NUM).item(0).setTextContent(
								docRTSRequest.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_ISSUE_NUM).item(0).getTextContent());
						log.verbose("invokeRTSPaymentStubForPLCC method ResponseForSale: "+XMLUtil.getXMLString(Responsedoc));
						
					}	
					
				}
			}
			//Setting sleep time for Payeezy
			String strRTSTimeout = YFSSystem.getProperty("academy.rts.stub.timeout");
			int iTimeout = 500;
			if(!YFCObject.isVoid(strRTSTimeout)) {
				iTimeout = Integer.parseInt(strRTSTimeout);
			}
			//Setting timeout as present in properties
			setSleepTime(iTimeout);
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		log.verbose("docPayStubResponse " + XMLUtil.getXMLString(Responsedoc));
		log.endTimer("AcademyPaymentStub: invokePLCCPaymentStub() method");
		return Responsedoc;
	}
	//OMNI-99827 END
	
	//OMNI-106361 - Stub for Direct Paypal - Start
	public static Document invokePaymentStubForDirectPaypal(Document docPaymentIn, 
			String strTransactionType) {
		log.beginTimer("AcademyPaymentStub.invokePaymentStubForDirectPaypal() method");
		log.verbose("AcademyPaymentStub: Paypal Direct::  TransactionType: " + strTransactionType);
		
		Document responseDoc = null;
		Element eleParentJson = null;
		Element eleAmount = null;
		Element eleLinks = null;
		Element eleLinks1 = null;
		Element eleJsonObj = null;
		Element elePaymentIn = null;
		Element eleTotRefAmount = null;
		Element eleRefReceivedAmt = null;
		Element eleTnxFee = null;
		StringBuilder parentPayment = new StringBuilder();
		String authId = "";
		String ccNo = "";
		
		try {
			responseDoc = XMLUtil.createDocument(AcademyConstants.STR_JSON_OBJECT);
			eleParentJson = responseDoc.getDocumentElement();
			
			elePaymentIn = docPaymentIn.getDocumentElement();
			eleAmount = responseDoc.createElement(AcademyConstants.JSON_ATTR_AMOUNT);
			eleAmount.setAttribute(AcademyConstants.CURRENCY, elePaymentIn.getAttribute(AcademyConstants.ATTR_CURRENCY));
			eleParentJson.appendChild(eleAmount);
			String totalAmount = elePaymentIn.getAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT);
			if (totalAmount.contains(AcademyConstants.STR_HYPHEN)) {
				totalAmount = totalAmount.substring(1);
			}
			eleAmount.setAttribute(AcademyConstants.ATTR_PAYPAL_TOTAL, totalAmount);
			
			authId = elePaymentIn.getAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID);
			ccNo = elePaymentIn.getAttribute(AcademyConstants.CREDIT_CARD_NO);
			
			eleParentJson.setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, AcademyConstants.STATUS_CODE_200);
			eleParentJson.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STATUS_CODE_200);
			
			String debugId = randomSmallString(13);
			String parentPayId = randomCapsString(24);
			eleParentJson.setAttribute(AcademyConstants.JSON_ATTR_CORRELATION_ID, debugId);
			eleParentJson.setAttribute(AcademyConstants.ATTR_PAYPAL_DEBUG_ID, debugId);
			
			parentPayment.append(AcademyConstants.STR_PAYID);
			parentPayment.append(parentPayId);
			eleParentJson.setAttribute(AcademyConstants.ATTR_PAYPAL_PARENT_PAYMENT, parentPayment.toString());
			
			eleParentJson.setAttribute(AcademyConstants.ATTR_PAYPAL_RESPONSE_CREATE_TIME, getCurrentTime());
			eleParentJson.setAttribute(AcademyConstants.ATTR_PAYPAL_UPDATE_TIME, getCurrentTime());		
			
			eleLinks = responseDoc.createElement(AcademyConstants.ATTR_PAYPAL_LINKS);
			
			eleLinks1 = responseDoc.createElement(AcademyConstants.ATTR_PAYPAL_LINKS);
			eleJsonObj = responseDoc.createElement(AcademyConstants.STR_JSON_OBJECT);
			eleJsonObj.setAttribute(AcademyConstants.ATTR_PAYPAL_HREF, 
					AcademyConstants.ATTR_PAYPAL_HREF_PAYMENT_LINK + parentPayment.toString());
			eleJsonObj.setAttribute(AcademyConstants.JSON_ATTR_METHOD, AcademyConstants.STR_HTTP_GET);
			eleJsonObj.setAttribute(AcademyConstants.STR_REL, AcademyConstants.ATTR_PAYPAL_PARENT_PAYMENT);
			eleLinks1.appendChild(eleJsonObj);
			eleParentJson.appendChild(eleLinks1);
			
			if (AcademyConstants.TRANSACTION_TYPE_VOID.equalsIgnoreCase(strTransactionType)) {
				
				eleParentJson.setAttribute(AcademyConstants.PAYPAL_RESPONSE_ID, authId);
				eleParentJson.setAttribute(AcademyConstants.ATTR_PAYPAL_RESPONSE_STATE, 
						AcademyConstants.STR_PAYPAL_RESPONSE_VOIDED);
				eleParentJson.setAttribute(AcademyConstants.ATTR_PAYPAL_PROTECTION_ELIGIBILITY, 
						AcademyConstants.STR_ELIGIBLE);
				eleParentJson.setAttribute(AcademyConstants.ATTR_PAYPAL_PROTECTION_ELIGIBILITY_TYPE, 
						AcademyConstants.STR_PROTECTION_ELIGIBILITY);
				eleParentJson.setAttribute(AcademyConstants.JSON_ATTR_VALID_TILL, addDateForPayment(7));								
				
				eleJsonObj = responseDoc.createElement(AcademyConstants.STR_JSON_OBJECT);
				eleJsonObj.setAttribute(AcademyConstants.ATTR_PAYPAL_HREF, 
						AcademyConstants.ATTR_PAYPAL_HREF_AUTH_LINK + authId);
				eleJsonObj.setAttribute(AcademyConstants.JSON_ATTR_METHOD, AcademyConstants.STR_HTTP_GET);
				eleJsonObj.setAttribute(AcademyConstants.STR_REL, AcademyConstants.STR_SELF);
				eleLinks.appendChild(eleJsonObj);
				eleParentJson.appendChild(eleLinks);
				
			} else if (AcademyConstants.TRANSACTION_TYPE_AUTHORIZE.equalsIgnoreCase(strTransactionType)) {				
				
				if (YFCCommon.isVoid(authId)) {
					authId = randomCapsString(17);
				}
				eleParentJson.setAttribute(AcademyConstants.PAYPAL_RESPONSE_ID, authId);
				eleParentJson.setAttribute(AcademyConstants.ATTR_PAYPAL_RESPONSE_STATE, 
						AcademyConstants.STR_PAYPAL_RESPONSE_AUTHORIZED);
				eleParentJson.setAttribute(AcademyConstants.ATTR_PAYPAL_PROTECTION_ELIGIBILITY, 
						AcademyConstants.STR_ELIGIBLE);
				eleParentJson.setAttribute(AcademyConstants.ATTR_PAYPAL_PROTECTION_ELIGIBILITY_TYPE, 
						AcademyConstants.STR_PROTECTION_ELIGIBILITY);
				eleParentJson.setAttribute(AcademyConstants.JSON_ATTR_VALID_TILL, addDateForPayment(30));
				eleParentJson.setAttribute(AcademyConstants.ATTR_PAYPAL_PAYMENT_MODE, 
						AcademyConstants.STR_INSTANT_TRANSFER);
				eleParentJson.setAttribute(AcademyConstants.JSON_ATTR_STATE_REASON, 
						AcademyConstants.STR_CHRG_TYPE_AUTH);
				
				eleJsonObj = responseDoc.createElement(AcademyConstants.STR_JSON_OBJECT);
				eleJsonObj.setAttribute(AcademyConstants.ATTR_PAYPAL_HREF, 
						AcademyConstants.ATTR_PAYPAL_HREF_ORDER_LINK + ccNo);
				eleJsonObj.setAttribute(AcademyConstants.JSON_ATTR_METHOD, AcademyConstants.STR_HTTP_GET);
				eleJsonObj.setAttribute(AcademyConstants.STR_REL, AcademyConstants.STR_ORDER);
				eleLinks.appendChild(eleJsonObj);
				eleParentJson.appendChild(eleLinks);
				
				eleLinks = responseDoc.createElement(AcademyConstants.ATTR_PAYPAL_LINKS);
				eleJsonObj = responseDoc.createElement(AcademyConstants.STR_JSON_OBJECT);
				eleJsonObj.setAttribute(AcademyConstants.ATTR_PAYPAL_HREF, 
						AcademyConstants.ATTR_PAYPAL_HREF_AUTH_LINK + authId);
				eleJsonObj.setAttribute(AcademyConstants.JSON_ATTR_METHOD, AcademyConstants.STR_HTTP_GET);
				eleJsonObj.setAttribute(AcademyConstants.STR_REL, AcademyConstants.STR_SELF);
				eleLinks.appendChild(eleJsonObj);
				eleParentJson.appendChild(eleLinks);
				
				eleLinks = responseDoc.createElement(AcademyConstants.ATTR_PAYPAL_LINKS);
				eleJsonObj = responseDoc.createElement(AcademyConstants.STR_JSON_OBJECT);
				eleJsonObj.setAttribute(AcademyConstants.ATTR_PAYPAL_HREF, 
						AcademyConstants.ATTR_PAYPAL_HREF_AUTH_LINK + authId + 
						AcademyConstants.STR_SLASH + AcademyConstants.TRANSACTION_TYPE_CAPTURE);
				eleJsonObj.setAttribute(AcademyConstants.JSON_ATTR_METHOD, AcademyConstants.STR_POST);
				eleJsonObj.setAttribute(AcademyConstants.STR_REL, AcademyConstants.TRANSACTION_TYPE_CAPTURE);
				eleLinks.appendChild(eleJsonObj);
				eleParentJson.appendChild(eleLinks);
				
				eleLinks = responseDoc.createElement(AcademyConstants.ATTR_PAYPAL_LINKS);
				eleJsonObj = responseDoc.createElement(AcademyConstants.STR_JSON_OBJECT);
				eleJsonObj.setAttribute(AcademyConstants.ATTR_PAYPAL_HREF, 
						AcademyConstants.ATTR_PAYPAL_HREF_AUTH_LINK + authId + 
						AcademyConstants.STR_SLASH + AcademyConstants.TRANSACTION_TYPE_VOID);
				eleJsonObj.setAttribute(AcademyConstants.JSON_ATTR_METHOD, AcademyConstants.STR_POST);
				eleJsonObj.setAttribute(AcademyConstants.STR_REL, AcademyConstants.TRANSACTION_TYPE_VOID);
				eleLinks.appendChild(eleJsonObj);
				eleParentJson.appendChild(eleLinks);
				
				
			} else if (AcademyConstants.TRANSACTION_TYPE_CAPTURE.equalsIgnoreCase(strTransactionType)) {
				
				eleParentJson.setAttribute(AcademyConstants.ATTR_PAYPAL_RESPONSE_STATE, 
						AcademyConstants.STR_PAYPAL_RESPONSE_COMPLETED);
				eleParentJson.setAttribute(AcademyConstants.JSON_ATTR_STATE_REASON, 
						AcademyConstants.STR_REASON_CODE_NONE);
				eleParentJson.setAttribute(AcademyConstants.ATTR_PAYPAL_INVOICE_NO, 
						elePaymentIn.getAttribute(AcademyConstants.ATTR_ORDER_NO));
				eleParentJson.setAttribute(AcademyConstants.ATTR_PAYPAL_IS_FINAL_CAPTURE, AcademyConstants.STR_TRUE);
				eleParentJson.setAttribute(AcademyConstants.ATTR_PAYPAL_CUSTOM, "");
				
				eleJsonObj = responseDoc.createElement(AcademyConstants.STR_JSON_OBJECT);
				eleJsonObj.setAttribute(AcademyConstants.ATTR_PAYPAL_HREF, 
						AcademyConstants.ATTR_PAYPAL_HREF_AUTH_LINK + authId);
				eleJsonObj.setAttribute(AcademyConstants.JSON_ATTR_METHOD, AcademyConstants.STR_HTTP_GET);
				eleJsonObj.setAttribute(AcademyConstants.STR_REL, AcademyConstants.STR_PAYPAL_AUTHORIZATION);
				eleLinks.appendChild(eleJsonObj);
				eleParentJson.appendChild(eleLinks);
				
				authId = randomCapsString(17);
				eleParentJson.setAttribute(AcademyConstants.PAYPAL_RESPONSE_ID, authId);
				
				eleLinks = responseDoc.createElement(AcademyConstants.ATTR_PAYPAL_LINKS);
				eleJsonObj = responseDoc.createElement(AcademyConstants.STR_JSON_OBJECT);
				eleJsonObj.setAttribute(AcademyConstants.ATTR_PAYPAL_HREF, 
						AcademyConstants.ATTR_PAYPAL_HREF_CAPTURE_LINK + authId);
				eleJsonObj.setAttribute(AcademyConstants.JSON_ATTR_METHOD, AcademyConstants.STR_HTTP_GET);
				eleJsonObj.setAttribute(AcademyConstants.STR_REL, AcademyConstants.STR_SELF);
				eleLinks.appendChild(eleJsonObj);
				eleParentJson.appendChild(eleLinks);
				
				eleLinks = responseDoc.createElement(AcademyConstants.ATTR_PAYPAL_LINKS);
				eleJsonObj = responseDoc.createElement(AcademyConstants.STR_JSON_OBJECT);
				eleJsonObj.setAttribute(AcademyConstants.ATTR_PAYPAL_HREF, 
						AcademyConstants.ATTR_PAYPAL_HREF_CAPTURE_LINK + authId + 
						AcademyConstants.STR_SLASH + AcademyConstants.TRANSACTION_TYPE_REFUND);
				eleJsonObj.setAttribute(AcademyConstants.JSON_ATTR_METHOD, AcademyConstants.STR_POST);
				eleJsonObj.setAttribute(AcademyConstants.STR_REL, AcademyConstants.TRANSACTION_TYPE_REFUND);
				eleLinks.appendChild(eleJsonObj);
				eleParentJson.appendChild(eleLinks);				

				eleTnxFee = responseDoc.createElement(AcademyConstants.PAYPAL_TRANSACTION_FEE);
				eleTnxFee.setAttribute(AcademyConstants.CURRENCY, elePaymentIn.getAttribute(AcademyConstants.ATTR_CURRENCY));
				eleTnxFee.setAttribute(AcademyConstants.JSON_ATTR_VALUE, AcademyConstants.STR_DECIMAL_ONE);
				eleParentJson.appendChild(eleTnxFee);
				
			} else if (AcademyConstants.TRANSACTION_TYPE_REFUND.equalsIgnoreCase(strTransactionType)) {
				
				eleParentJson.setAttribute(AcademyConstants.ATTR_PAYPAL_RESPONSE_STATE, 
						AcademyConstants.STR_PAYPAL_RESPONSE_COMPLETED);
				
				String captureId = randomCapsString(17);
				eleParentJson.setAttribute(AcademyConstants.STR_PAYPAL_CAPTURE_ID, captureId);
				
				eleLinks = responseDoc.createElement(AcademyConstants.ATTR_PAYPAL_LINKS);
				eleJsonObj = responseDoc.createElement(AcademyConstants.STR_JSON_OBJECT);
				eleJsonObj.setAttribute(AcademyConstants.ATTR_PAYPAL_HREF, 
						AcademyConstants.ATTR_PAYPAL_HREF_CAPTURE_LINK + captureId);
				eleJsonObj.setAttribute(AcademyConstants.JSON_ATTR_METHOD, AcademyConstants.STR_HTTP_GET);
				eleJsonObj.setAttribute(AcademyConstants.STR_REL, AcademyConstants.TRANSACTION_TYPE_CAPTURE);
				eleLinks.appendChild(eleJsonObj);
				eleParentJson.appendChild(eleLinks);
				
				authId = randomCapsString(17);				
				eleParentJson.setAttribute(AcademyConstants.PAYPAL_RESPONSE_ID, authId);
				
				eleLinks = responseDoc.createElement(AcademyConstants.ATTR_PAYPAL_LINKS);
				eleJsonObj = responseDoc.createElement(AcademyConstants.STR_JSON_OBJECT);
				eleJsonObj.setAttribute(AcademyConstants.ATTR_PAYPAL_HREF, 
						AcademyConstants.ATTR_PAYPAL_HREF_REFUND_LINK + authId);
				eleJsonObj.setAttribute(AcademyConstants.JSON_ATTR_METHOD, AcademyConstants.STR_HTTP_GET);
				eleJsonObj.setAttribute(AcademyConstants.STR_REL, AcademyConstants.STR_SELF);
				eleLinks.appendChild(eleJsonObj);
				eleParentJson.appendChild(eleLinks);
				
				eleTotRefAmount = responseDoc.createElement(AcademyConstants.TOTAL_REFUNDED_AMOUNT);
				eleTotRefAmount.setAttribute(AcademyConstants.CURRENCY, elePaymentIn.getAttribute(AcademyConstants.ATTR_CURRENCY));
				eleTotRefAmount.setAttribute(AcademyConstants.JSON_ATTR_VALUE, totalAmount);
				eleParentJson.appendChild(eleTotRefAmount);
				
				eleRefReceivedAmt = responseDoc.createElement(AcademyConstants.REFUND_FROM_RECEIVED_AMOUNT);
				eleRefReceivedAmt.setAttribute(AcademyConstants.CURRENCY, elePaymentIn.getAttribute(AcademyConstants.ATTR_CURRENCY));
				String refReceivedAmount = String.valueOf(Double.parseDouble(totalAmount) - 1);				
				eleRefReceivedAmt.setAttribute(AcademyConstants.JSON_ATTR_VALUE, refReceivedAmount);
				eleParentJson.appendChild(eleRefReceivedAmt);
				
				eleTnxFee = responseDoc.createElement(AcademyConstants.REFUND_FROM_TRANSACTION_FEE);
				eleTnxFee.setAttribute(AcademyConstants.CURRENCY, elePaymentIn.getAttribute(AcademyConstants.ATTR_CURRENCY));
				eleTnxFee.setAttribute(AcademyConstants.JSON_ATTR_VALUE, AcademyConstants.STR_DECIMAL_ONE);
				eleParentJson.appendChild(eleTnxFee);
				
			}			
			
		} catch (Exception e) {
			e.printStackTrace(); 
			log.verbose("Exception at AcademyPaymentStub.invokePaymentStubForDirectPaypal");
		}
		log.verbose("Returning Stub Response :: " + XMLUtil.getXMLString(responseDoc));
		log.endTimer("AcademyPaymentStub.invokePaymentStubForDirectPaypal() method");
		return responseDoc;
	}
	
	/**Adding n number of days to current day in yyyy-MM-dd'T'HH:mm:ss format
	 * @param n
	 * @return futureDate
	 */
	public static String addDateForPayment(int n) {
		String strFutureDay = "";
		Date futureday = DateUtils.addDays(new Date(), n);
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);
		strFutureDay = sdf.format(futureday);
		log.verbose("**Future Date to consider :: " + strFutureDay);
		return strFutureDay;
	}
	
	/**Getting current date time in yyyy-MM-dd'T'HH:mm:ss format
	 * @return currentDateTime
	 */
	private static String getCurrentTime() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(AcademyConstants.STR_DATE_TIME_PATTERN);  
		LocalDateTime now = LocalDateTime.now();  
		String strCurrentDateTimeVal = dtf.format(now);
		log.verbose("strCurrentDateTimeVal: " + strCurrentDateTimeVal);
		return strCurrentDateTimeVal;
	}
	
	/**Generating random alphanumeric ID in Small letters with Specified length 
	 * @param len
	 * @return random id
	 */
	public static String randomSmallString(int len) {
		String alphaNumeric = "0123456789abcdefghijklmnopqrstuvxyz";
		StringBuilder random = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			int index = (int)(alphaNumeric.length() * Math.random());
			random.append(alphaNumeric.charAt(index));
		}
		log.verbose("random alphanumeric ID in Small letters ::" + random.toString());
		return random.toString();
	}
	
	/**Generating random alphanumeric ID in Capital letters with Specified length 
	 * @param len
	 * @return random id
	 */
	public static String randomCapsString(int len) {
		String alphaNumeric = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		StringBuilder random = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			int index = (int)(alphaNumeric.length() * Math.random());
			random.append(alphaNumeric.charAt(index));
		}
		log.verbose("random alphanumeric ID in Capital letters ::" + random.toString());
		return random.toString();
	}
	
	//OMNI-106361 - Stub for Direct Paypal - End

}
