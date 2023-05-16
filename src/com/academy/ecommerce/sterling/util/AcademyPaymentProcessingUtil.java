package com.academy.ecommerce.sterling.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPFault;
import org.apache.axis.message.MessageElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.ecommerce.sterling.util.webservice.InvokeCyberSourceWebservice;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfs.japi.YFSExtnPaymentCollectionInputStruct;
import com.yantra.yfs.japi.YFSUserExitException;


public class AcademyPaymentProcessingUtil implements YIFCustomApi {
	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPaymentProcessingUtil.class);
	private Properties props;
	
	public void setProperties(Properties props) {
		this.props = props;
	}
	
	public static void  raiseAlert(YFSEnvironment env, Document inDoc) throws Exception {
		
			log.verbose("############### Inside raiseAlert ######");
			log.verbose("Input doc " + XMLUtil.getXMLString(inDoc));
			
			YFCElement eleResponse = YFCDocument.getDocumentFor(inDoc).getDocumentElement();
			
			Document docExceptionInput = XMLUtil.createDocument(AcademyConstants.ELE_INBOX);
			Element eleExceptionInput = docExceptionInput.getDocumentElement();
			eleExceptionInput.setAttribute(AcademyConstants.ATTR_ACTIVE_FLAG, AcademyConstants.STR_YES);
			eleExceptionInput.setAttribute(AcademyConstants.ATTR_CONSOLIDATE, AcademyConstants.STR_YES);
			eleExceptionInput.setAttribute(AcademyConstants.ATTR_EXCPTN_TYPE, "ACADEMY_WALLET_SERVICE_ALERTS");
			eleExceptionInput.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY, eleResponse.getAttribute("OrderHeaderKey"));
			eleExceptionInput.setAttribute(AcademyConstants.ATTR_ORDER_NO, eleResponse.getAttribute("OrderNo"));

			Element eleConsolidationTemplt = docExceptionInput.createElement(
					AcademyConstants.ELE_CONSOLIDATE_TEMPLT);
			XMLUtil.appendChild(docExceptionInput.getDocumentElement(), eleConsolidationTemplt);
			
			Element eleInboxConsolidationTemplt=docExceptionInput.createElement(AcademyConstants.ELE_INBOX);
			eleInboxConsolidationTemplt.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY, "");
			eleInboxConsolidationTemplt.setAttribute(AcademyConstants.ATTR_ORDER_NO, "");
			eleInboxConsolidationTemplt.setAttribute(AcademyConstants.ATTR_EXCPTN_TYPE, "");
			XMLUtil.appendChild(eleConsolidationTemplt, eleInboxConsolidationTemplt);
			
			Element eleInboxRefList = docExceptionInput.createElement(AcademyConstants.ELE_INBOX_REF_LIST);
			XMLUtil.appendChild(docExceptionInput.getDocumentElement(),	eleInboxRefList);
			
			Element eleInboxRef = docExceptionInput.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE, "TEXT");
			eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME, "Reason Code");
			eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE, eleResponse.getAttribute("ErrorCode"));
			XMLUtil.appendChild(eleInboxRefList, eleInboxRef);
			
			eleInboxRef = docExceptionInput.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE, "TEXT");
			eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME, "Reason Description");
			eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE, eleResponse.getAttribute("ErrorDescription"));
			XMLUtil.appendChild(eleInboxRefList, eleInboxRef);
			
			eleInboxRef = docExceptionInput.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE, "TEXT");
			eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME, "CreditCardNo");
			eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE, eleResponse.getAttribute("CreditCardNo"));
			XMLUtil.appendChild(eleInboxRefList, eleInboxRef);
			
			log.verbose("Input to createException API " + XMLUtil.getXMLString(docExceptionInput));
			
			AcademyUtil.invokeAPI(env, AcademyConstants.API_CREATE_EXCEPTION, docExceptionInput);
	}
	
	// This is invoked from InvokeCyberSourceWebservice class
	public static void raiseNoValidTenderAlert(YFSEnvironment env, Document inDoc) throws Exception {
		
			log.verbose("############### Inside raiseNoValidTenderAlert ######");
			YFCDocument inDocForAlert = YFCDocument.createDocument("Order");
			YFCElement eleInDocForAlert = inDocForAlert.getDocumentElement();
			eleInDocForAlert.setAttribute("OrderHeaderKey", 
				XPathUtil.getString(inDoc, InvokeCyberSourceWebservice.PAYMENT_PATH_FOR_REFUND_OR_REAUTH + "@OrderHeaderKey"));
			eleInDocForAlert.setAttribute("OrderNo", 
				XPathUtil.getString(inDoc, InvokeCyberSourceWebservice.PAYMENT_PATH_FOR_REFUND_OR_REAUTH + "@OrderNo"));
			eleInDocForAlert.setAttribute("PaymentType", 
				XPathUtil.getString(inDoc, InvokeCyberSourceWebservice.PAYMENT_PATH_FOR_REFUND_OR_REAUTH + "@PaymentType"));
			eleInDocForAlert.setAttribute("CreditCardNo", 
				XPathUtil.getString(inDoc, InvokeCyberSourceWebservice.PAYMENT_PATH_FOR_REFUND_OR_REAUTH + "@CreditCardNo"));
			eleInDocForAlert.setAttribute("SvcNo", " ");
			eleInDocForAlert.setAttribute("RequestAmount", 
				XPathUtil.getString(inDoc, InvokeCyberSourceWebservice.PAYMENT_PATH_FOR_REFUND_OR_REAUTH + "@RequestAmount"));
			eleInDocForAlert.setAttribute("ErrorCode", "Refund Failed");
			eleInDocForAlert.setAttribute("ErrorDescription", "Refund Failed");
			invokeCreateExcepiontForNoValidTender(env, inDocForAlert.getDocument());
			log.verbose("############### raiseNoValidTenderAlert completed ######");
	
	}
	
	public static Document invokeCreateExcepiontForNoValidTender(YFSEnvironment env, Document inDoc) 
		throws Exception {
		log.verbose("############### Inside invokeCreateExcepiontForNoValidTender ######");
		log.verbose("With input " + XMLUtil.getXMLString(inDoc));
		Element eleInDoc = inDoc.getDocumentElement();

		Document docExceptionInput = XMLUtil.createDocument(AcademyConstants.ELE_INBOX);
		Element eleExceptionInput = docExceptionInput.getDocumentElement();
		eleExceptionInput.setAttribute(AcademyConstants.ATTR_ACTIVE_FLAG, AcademyConstants.STR_YES);
		eleExceptionInput.setAttribute(AcademyConstants.ATTR_CONSOLIDATE, AcademyConstants.STR_YES);
		eleExceptionInput.setAttribute(AcademyConstants.ATTR_EXCPTN_TYPE, "ACADEMY_NO_VALID_TENDER");
		eleExceptionInput.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY, eleInDoc.getAttribute("OrderHeaderKey"));
		eleExceptionInput.setAttribute(AcademyConstants.ATTR_ORDER_NO, eleInDoc.getAttribute("OrderNo"));
		
		Element eleConsolidationTemplt = docExceptionInput.createElement(AcademyConstants.ELE_CONSOLIDATE_TEMPLT);
		XMLUtil.appendChild(docExceptionInput.getDocumentElement(), eleConsolidationTemplt);
		
		Element eleInboxConsolidationTemplt=docExceptionInput.createElement(AcademyConstants.ELE_INBOX);
		eleInboxConsolidationTemplt.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY, "");
		eleInboxConsolidationTemplt.setAttribute(AcademyConstants.ATTR_ORDER_NO, "");
		eleInboxConsolidationTemplt.setAttribute(AcademyConstants.ATTR_EXCPTN_TYPE, "");
		XMLUtil.appendChild(eleConsolidationTemplt, eleInboxConsolidationTemplt);
		
		Element eleInboxRefList = docExceptionInput.createElement(AcademyConstants.ELE_INBOX_REF_LIST);
		XMLUtil.appendChild(docExceptionInput.getDocumentElement(),	eleInboxRefList);
		
		Element eleInboxRef = docExceptionInput.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
		eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE, "TEXT");
		eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME, "Payment Type");
		eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE, eleInDoc.getAttribute("PaymentType"));
		XMLUtil.appendChild(eleInboxRefList, eleInboxRef);

		// If PaymentType is CC add CC# else if PaymentType is GC add SVC#
		if("CREDIT_CARD".equalsIgnoreCase(eleInDoc.getAttribute("PaymentType"))) {
			eleInboxRef = docExceptionInput.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE, "TEXT");
			eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME, "CreditCard#");
			eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE, eleInDoc.getAttribute("CreditCardNo"));
			XMLUtil.appendChild(eleInboxRefList, eleInboxRef);
		}
		else if("GIFT_CARD".equalsIgnoreCase(eleInDoc.getAttribute("PaymentType"))) {
			eleInboxRef = docExceptionInput.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
			eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE, "TEXT");
			eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME, "GiftCard#");
			eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE, eleInDoc.getAttribute("SvcNo"));
			XMLUtil.appendChild(eleInboxRefList, eleInboxRef);
		}
		
		eleInboxRef = docExceptionInput.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
		eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE, "TEXT");
		eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME, "RequestAmount");
		eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE, eleInDoc.getAttribute("RequestAmount"));
		XMLUtil.appendChild(eleInboxRefList, eleInboxRef);

		eleInboxRef = docExceptionInput.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
		eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE, "TEXT");
		eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME, "ErrorCode");
		eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE,eleInDoc.getAttribute("ErrorCode"));
		XMLUtil.appendChild(eleInboxRefList, eleInboxRef);

		eleInboxRef = docExceptionInput.createElement(AcademyConstants.ELE_INBOX_REFERENCES);
		eleInboxRef.setAttribute(AcademyConstants.ATTR_REF_TYPE, "TEXT");
		eleInboxRef.setAttribute(AcademyConstants.ATTR_NAME, "ErrorDescription");
		eleInboxRef.setAttribute(AcademyConstants.ATTR_VALUE, eleInDoc.getAttribute("ErrorDescription"));
		XMLUtil.appendChild(eleInboxRefList, eleInboxRef);

		log.verbose("Input to createException API " + XMLUtil.getXMLString(docExceptionInput));
		return AcademyUtil.invokeAPI(env, AcademyConstants.API_CREATE_EXCEPTION, docExceptionInput);
	}
	
	
	// This is invoked from the external Service
	public Document noValidTenderAlert(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("############### Inside noValidTenderAlert ######");
		YFCDocument inDocForAlert = YFCDocument.createDocument("Order");
		YFCElement eleInDocForAlert = inDocForAlert.getDocumentElement();
		eleInDocForAlert.setAttribute("OrderHeaderKey", 
				XPathUtil.getString(inDoc, "/Order/@OrderHeaderKey"));
		eleInDocForAlert.setAttribute("OrderNo", 
				XPathUtil.getString(inDoc, "/Order/@OrderNo"));
		eleInDocForAlert.setAttribute("PaymentType", 
			XPathUtil.getString(inDoc, "/Order/PaymentMethods/PaymentMethod/@PaymentType"));
		eleInDocForAlert.setAttribute("CreditCardNo", 
			XPathUtil.getString(inDoc, "/Order/PaymentMethods/PaymentMethod/@CreditCardNo"));
		eleInDocForAlert.setAttribute("SvcNo", 
			XPathUtil.getString(inDoc, "/Order/PaymentMethods/PaymentMethod/@SvcNo"));
		eleInDocForAlert.setAttribute("RequestAmount", 
			XPathUtil.getString(inDoc, "/Order/@RemainingAmount"));
		eleInDocForAlert.setAttribute("ErrorCode", "Payment suspended");
		eleInDocForAlert.setAttribute("ErrorDescription", "Payment suspended for Charge/Refund");
		return invokeCreateExcepiontForNoValidTender(env, inDocForAlert.getDocument());
	}
	
	/**
	 * This method returns the AuthorizationID used in most recent settlement transaction on a 
	 * given payment method so that same AuthorizationID can be used at the time of Refund transaction.
	 * @param nodeList
	 * @return AuthorizationID or "" string
	 * 
	 */
	public static String getValidAuth(NodeList nodeList) {
		try {
			if(nodeList == null || nodeList.getLength() == 0) {
				log.verbose("############### Node list is null or empty ######");
				return "";
			}
			List<Element> nodes = new ArrayList<Element>();
			if(nodeList.getLength() > 1) {
				Comparator<Element> comp = new Comparator<Element>() {
					public int compare(Element chargeTran1, Element chargeTran2) {
						DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
						try {
							Date d0 = formatter.parse(chargeTran1.getAttribute("CollectionDate"));
							Date d1 = formatter.parse(chargeTran2.getAttribute("CollectionDate"));
							return (d0.compareTo(d1));
						} catch (ParseException e) {
							log.verbose("############### Date Comparison failed ######");
							
						}
						return -1;
					}
				};
				for(int i = 0; i < nodeList.getLength(); i++) {
					nodes.add((Element)nodeList.item(i));
				}
				/* 
				 * Sort the collection in descending order so that most recent charge
				 * transaction will be the first.
				 */
				Collections.sort(nodes, Collections.reverseOrder(comp));
			} else {
				nodes.add((Element)nodeList.item(0));
			}
			return nodes.get(0).getAttribute("AuthorizationID");
		} catch (Exception e) {
			log.verbose("############### Date Comparison failed ######");
			
			return "";
		}
	}

	/**
	 * Method wraps Exception object to YFSUserExitException object
	 * @param e
	 * @return YFSUserExitException 
	 */
	public static YFSException getYFSExceptionWithTrace(final Exception exception) {
		final YFSException yfsException = new YFSException();
		yfsException.setStackTrace(exception.getStackTrace());
		return yfsException;
	}
	
	/**
	 * Method creates YFSUserExitException object with error code and error description
	 * from the document 
	 * @param outDoc
	 * @return YFSUserExitException
	 */
	public static YFSUserExitException getUEExceptionFromSoapFault(final SOAPFault soapFault) {
		final YFSUserExitException yfsUEException = new YFSUserExitException();
		yfsUEException.setErrorCode(soapFault.getFaultCode());
		yfsUEException.setErrorDescription(soapFault.getFaultString());
		return yfsUEException;
	}
	
	
	/**
	 * Start-Added below method as part of STL-1378
	 * Method creates YFSException object with error code and error description
	 * from the document 
	 * @param outDoc
	 * @return YFSException
	 */
	public static YFSException getYFSExceptionWithErrorCode(final Document outDoc) {
		final YFSException yfsException = new YFSException();
		yfsException.setErrorCode(outDoc.getDocumentElement().getAttribute("ErrorCode"));
		yfsException.setErrorDescription(outDoc.getDocumentElement().getAttribute("ErrorDescription"));
		return yfsException;
	}
	
	/**
	 * Method creates YFSException object with error code and error description
	 * from the document 
	 * @param strErrorCode
	 * @param strErrorDesc
	 * @return YFSException
	 */
	public static YFSException getYFSExceptionWithErrorCode(String strErrorCode,String strErrorDesc) {
		final YFSException yfsException = new YFSException();
		yfsException.setErrorCode(strErrorCode);
		yfsException.setErrorDescription(strErrorDesc);
		return yfsException;
	}
	
	/**
	 * Method creates YFSException object with error code and error description
	 * from the document 
	 * @param outDoc
	 * @return YFSException
	 */
	public static YFSException getExceptionFromSoapFault(final Document responseDoc) {
		final YFSException yfsException = new YFSException();
		YFCElement inEle = YFCDocument.getDocumentFor(responseDoc).getDocumentElement();
		if(!YFCObject.isVoid(inEle.getChildElement("faultcode"))){
			yfsException.setErrorCode(inEle.getChildElement("faultcode").getNodeValue());	
		}
		if(!YFCObject.isVoid(inEle.getChildElement("faultstring"))){
			yfsException.setErrorDescription(inEle.getChildElement("faultstring").getNodeValue());
		}
		return yfsException;
	}

	/**
	 * Method creates YFSUserExitException object with error code and error description
	 * from the document 
	 * @param outDoc
	 * @return YFSUserExitException
	 */
	public static YFSUserExitException getUEExceptionFromAuthResponse(final SOAPBody response) {
		final YFSUserExitException yfsUEException = new YFSUserExitException();
		yfsUEException.setErrorCode("Authorization failed");
		yfsUEException.setErrorDescription("Authorization failed with reason code : " + 
				((MessageElement)response.getElementsByTagName("reasonCode").item(0)).getValue());
		return yfsUEException;
	}

	public static String getAuthExpirationDate(YFSEnvironment env, String tenderType) {
		Document docCommonCodeListOutput = null;
		Document docAuthExpDaysInput = null;
		long currentTime = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");		
		try {
			log.verbose("************ Inside getAuthExpirationDate ***********");
			log.verbose("tenderType = "+tenderType);
			docAuthExpDaysInput = XMLUtil
					.createDocument(AcademyConstants.ELE_COMMON_CODE);
			docAuthExpDaysInput.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_CODE_TYPE, "AUTH_EXP_DAYS");
			docAuthExpDaysInput.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_COMMON_CODE_VALUE, tenderType);
			docAuthExpDaysInput.getDocumentElement().setAttribute(
					"OrganizationCode", AcademyConstants.PRIMARY_ENTERPRISE);
			docCommonCodeListOutput = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_COMMON_CODELIST,
					docAuthExpDaysInput);
			log.verbose("outXML of CommonCodeList="+XMLUtil.getXMLString(docCommonCodeListOutput));
			if(!YFCObject.isVoid(docCommonCodeListOutput)) {				
				String expirationDays = XPathUtil.getString(docCommonCodeListOutput, 
				"CommonCodeList/CommonCode/@CodeShortDescription");
				log.verbose("expirationDays = " +XMLUtil.getXMLString(docCommonCodeListOutput));
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, Integer.parseInt(expirationDays));
				return sdf.format(cal.getTime());
			}
		} catch (Exception e) {
			return sdf.format(currentTime);
		}
		return sdf.format(currentTime);
	}
	
	/**
	 * Input date format from CyberSource is yyyy-MM-dd'T'HH:mm:ssZ e.g. '2009-09-30T06:48:42Z'
	 * To be converted to XML date format yyyyMMddHHmmss e.g. '20090930064842'
	 * @param time
	 * @return
	 */
	
	public static String getDateInXMLFormat(String time) {
		try {
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			Date date = (Date)formatter.parse(time);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			return sdf.format(date);
		} catch (ParseException e) {
			return "";
		}
	}
	
	/*
	 * If input comes from COM, CreditCardExpirationDate will be in "MM/yyyy" format. If
	 * input comes from Agent, its format will be as "MMyyyy".
	 */

	public static String[] getCCExpirationMonthAndYear(String ccExpirationDate) {
		String[] ccExpMonthAndYear = new String[2];
		if(ccExpirationDate.indexOf("/") != -1) {
			ccExpMonthAndYear = ccExpirationDate.split("/");	
		} else {
			/*Start Fix for Bug 4127 - Zubair*/
			//ccExpMonthAndYear[0] = ccExpirationDate.substring(0, 2);
			/*The first string contains the month which will be the first 2 characters of the ccExpiration date.
			 * Expect the format to be in MMyyyy
			 * If the value of the month is only 1 e.g.4,5 etc then we will prefix the month with a 0 to show
			 * as 04,05 required by cybersource.*/
			ccExpMonthAndYear[0] = ccExpirationDate.substring(0,ccExpirationDate.length()-2);
			int i= ccExpMonthAndYear[0].length();
			if(i==1){
				ccExpMonthAndYear[0]="0".concat(ccExpMonthAndYear[0]);
			}else if(i>2){
				/*if the format of the date is MMyyyy*/
				ccExpMonthAndYear[0] = ccExpirationDate.substring(0,2);
			}
			//ccExpMonthAndYear[1] = ccExpirationDate.substring(2, ccExpirationDate.length());
			/*The second string contains the year which will be the last 2 digits of the ccExpiration date.
			 * Expect the format to be in MMyyyy*/
			ccExpMonthAndYear[1] = ccExpirationDate.substring(ccExpirationDate.length()-2, ccExpirationDate.length());
			/*End Fix for Bug 4127 - Zubair*/
		}
		return ccExpMonthAndYear;
	}
	
	//START : STL-1163
	/** This method will return AuthorizationExpirationDate(yyyy-MM-ddTHH:mm:ss format) for paypall order.
	 * @param env
	 * @param strAuthDate in yyyy-MM-ddTHH:mm:ss format
	 * @return strExpirationDate in yyyy-MM-ddTHH:mm:ss format
	 * @throws Exception
	 */
	
	/*
	Begin: OMNI-1755 :Cardinal Technical Cleanup
 
	public static String getAuthorizationExpirationDateForPayPall(YFSEnvironment env, String strAuthDate)
    throws Exception
    {
		Document docCommonCodeListoutOutput = getAuthExpDetailFromCommonCode(env);
		Element eleCommonCode = (Element)docCommonCodeListoutOutput.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE).item(0);

		String strExpirationDays = eleCommonCode.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE);//"8";
		String strCutofTimeHH = eleCommonCode.getAttribute(AcademyConstants.CODE_LONG_DESC);//"01";
		String strCutofTimeMM = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);//"49";

		int iExpirationDays = Integer.valueOf(strExpirationDays).intValue();
		int iCutofTimeInMin = Integer.valueOf(strCutofTimeHH).intValue() * 60 + Integer.valueOf(strCutofTimeMM).intValue();

		String strAuthTimeHH = strAuthDate.substring(11, 13);
		String strAuthTimeMM = strAuthDate.substring(14, 16);
		int iAuthTimeInMin = Integer.valueOf(strAuthTimeHH).intValue() * 60 + Integer.valueOf(strAuthTimeMM).intValue();

		log.verbose("Auth Exp days(CodeValue) from Common Code is:\t" + iExpirationDays);
		log.verbose("Cut of Time Hrs(CodeLongDescription) from common code is:\t" + strCutofTimeHH);
		log.verbose("Cut of Time Mins(CodeShortDescription) from common code is:\t" + strCutofTimeMM);
		log.verbose("strAuthDate:\t" + strAuthDate);
		log.verbose("iCutofTimeInMin:" + iCutofTimeInMin);
		log.verbose("iAuthTimeInMin:\t" + iAuthTimeInMin);

		if (iAuthTimeInMin < iCutofTimeInMin) {
			log.verbose("AuthTime is lesser than CutofTime. So ExpirationDays = ExpirationDays - 1 day = " + (Integer.valueOf(strExpirationDays).intValue() - 1));
			iExpirationDays--;
		}

		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date dAuthDate = formatter.parse(strAuthDate);
		Calendar cal = Calendar.getInstance();
		cal.setTime(dAuthDate);
		cal.add(Calendar.DATE, iExpirationDays);
		String strExpirationDate = formatter.format(cal.getTime());

		strExpirationDate = strExpirationDate.concat("T").concat(strCutofTimeHH).concat(":").concat(strCutofTimeMM).concat(":").concat("00");
		log.verbose("strExpirationDate:" + strExpirationDate);
		return strExpirationDate;
    }
	
	End: OMNI-1755 : Cardinal Technical Cleanup
	*/

  private static Document getAuthExpDetailFromCommonCode(YFSEnvironment env)  throws Exception
  {
    Document docCommonCodeListinInput = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
    docCommonCodeListinInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, "PP_AUTHEXP_DAYS");
    docCommonCodeListinInput.getDocumentElement().setAttribute(AcademyConstants.ORG_CODE_ATTR, "DEFAULT");
    Document docCommonCodeListoutOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMONCODE_LIST, docCommonCodeListinInput);

    return docCommonCodeListoutOutput;
  }
  
	/**
	 * @param env
	 * @param centinelRequestDoc
	 * @throws ParserConfigurationException
	 * @throws DOMException
	 * @throws Exception
	 */
	 
	/*
	Begin: OMNI-1755 :Cardinal Technical Cleanup
 
	public static void logCardinalRequestAndResponseToDB(YFSEnvironment env, Document centinelRequestDoc, Document centinelResponseDoc,String strIsDBLoggingEnable,String strDBServiceName) throws Exception {
		if("Y".equals(strIsDBLoggingEnable) && (!YFSObject.isVoid(strDBServiceName))){	    	  
	    	  Document docCardinalRequestResponse = XMLUtil.createDocument(AcademyConstants.ELE_CARDINAL_REQ_RESP); 
	    	  Element eleCardinalRequestResponse = docCardinalRequestResponse.getDocumentElement();
	    	  Element eleCardinalRequest = docCardinalRequestResponse.createElement(AcademyConstants.ELE_CARDINAL_REQ);
	    	  Element elecentinelRequest = centinelRequestDoc.getDocumentElement();
	    	  eleCardinalRequestResponse.appendChild(eleCardinalRequest);
	    	  XMLUtil.importElement(eleCardinalRequest, elecentinelRequest);

	    	  Element eleCardinalResponse = docCardinalRequestResponse.createElement(AcademyConstants.ELE_CARDINAL_RESP);
	    	  Element eleCentinelResponse = centinelResponseDoc.getDocumentElement();
	    	  eleCardinalRequestResponse.appendChild(eleCardinalResponse);
	    	  XMLUtil.importElement(eleCardinalResponse, eleCentinelResponse);

	    	  AcademyUtil.invokeService(env, strDBServiceName, docCardinalRequestResponse);
	      }
	}
	End: OMNI-1755 : Cardinal Technical Cleanup
	*/
	
  //END : STL-1163
  /**Added as part of STL-1448.Sets the Collection date to 15 mins(which is configurable) ahead from the current timestamp
	 * @return
	 * @throws ParseException
	 */
	public static Date getCollectionDate(String strNextTriggerIntervalInMin) throws ParseException {
		Date collectionDate = null;
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN_1);			
		int iNextTriggerIntervalInMin = Integer.parseInt(strNextTriggerIntervalInMin);			
		cal.add(Calendar.MINUTE, iNextTriggerIntervalInMin);			
		String strCollectionDate = sdf.format(cal.getTime());
		collectionDate = sdf.parse(strCollectionDate);
		log.verbose("strCollectionDate:\t"+collectionDate);
		return collectionDate;
	}
	//END : STL-1163
//	RTS PaymentProcessingUtil changes : Begin
	/**
	 * This method is to create Payment XML from YFSExtnPaymentCollectionInputStruct object 
	 * @param YFSExtnPaymentCollectionInputStruct
	 * @return Document
	 * @throws Exception
	 */
	public static Document convertPaymentInputStructToXML(YFSExtnPaymentCollectionInputStruct paymentInputStruct)
    throws Exception {
  
		log.verbose("Enter AcademyPaymentProcessingUtil.convertPaymentInputStructToXML() method :");	
	    Document paymentInputDoc = XMLUtil.newDocument();
	    Element paymentRoot = paymentInputDoc.createElement(AcademyConstants.ELE_PAYMENT);
	    
	    paymentRoot.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID, paymentInputStruct.authorizationId);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_CUSTOMER_ACCOUNT_NO, paymentInputStruct.customerAccountNo);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_CUSTOMER_PO_NO, paymentInputStruct.customerPONo);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_DOC_TYPE, paymentInputStruct.documentType);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, paymentInputStruct.enterpriseCode);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_FNAME, paymentInputStruct.firstName);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_LNAME, paymentInputStruct.lastName);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_MIDDLE_NAME, paymentInputStruct.middleName);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_MERCHANT_ID, paymentInputStruct.merchantId);
	    paymentRoot.setAttribute(AcademyConstants.STR_ORDR_HDR_KEY, paymentInputStruct.orderHeaderKey);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_ORDER_NO, paymentInputStruct.orderNo);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_PAYMENT_TYPE, paymentInputStruct.paymentType);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT, Double.toString(paymentInputStruct.requestAmount));
	    paymentRoot.setAttribute(AcademyConstants.ATTR_SECURE_AUTHENTICATION_CODE, paymentInputStruct.secureAuthenticationCode);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_SVCNO, paymentInputStruct.svcNo);
	    
	    //The attribute bPreviouslyInvoked starts with small as mentioned in documentation 
	    paymentRoot.setAttribute(AcademyConstants.ATTR_PREVIOUSLY_INVOKED, String.valueOf(paymentInputStruct.bPreviouslyInvoked).toString());
	    paymentRoot.setAttribute(AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY, paymentInputStruct.chargeTransactionKey);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_CHARGE_TYPE, paymentInputStruct.chargeType);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_CC_EXPIRATION_DATE, paymentInputStruct.creditCardExpirationDate);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_CC_NAME, paymentInputStruct.creditCardName);
	    paymentRoot.setAttribute(AcademyConstants.CREDIT_CARD_NO, paymentInputStruct.creditCardNo);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_CREDIT_CARD_TYPE, paymentInputStruct.creditCardType);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_CURRENCY, paymentInputStruct.currency);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_CURRENT_AUTHORIZATION_AMT, Double.toString(paymentInputStruct.currentAuthorizationAmount));
	    
	    paymentRoot.setAttribute(AcademyConstants.ATTR_BILL_TO_ADDRLINE1, paymentInputStruct.billToAddressLine1);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_BILL_TO_CITY, paymentInputStruct.billToCity);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_BILL_TO_COUNTRY, paymentInputStruct.billToCountry);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_BILL_TO_DAY_PHONE, paymentInputStruct.billToDayPhone);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_BILL_TO_EMAILID, paymentInputStruct.billToEmailId);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_BILL_TO_FIRST_NAME, paymentInputStruct.billToFirstName);
	    //paymentRoot.setAttribute(AcademyConstants.ATTR_BILL_TO_ID, paymentInputStruct.billToId);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_BILL_TOID, paymentInputStruct.billToId);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_BILL_TO_KEY, paymentInputStruct.billTokey);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_BILL_TO_LAST_NAME, paymentInputStruct.billToLastName);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_BILL_TO_STATE, paymentInputStruct.billToState);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_BILL_TO_ZIPCODE, paymentInputStruct.billToZipCode);
	    
	    paymentRoot.setAttribute(AcademyConstants.ATTR_SHIP_TO_ADDR_LINE1, paymentInputStruct.shipToAddressLine1);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_SHIP_TO_CITY, paymentInputStruct.shipToCity);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_SHIP_TO_COUNTRY, paymentInputStruct.shipToCountry);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_SHIP_TO_DAY_PHONE, paymentInputStruct.shipToDayPhone);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_SHIP_TO_EMAIL_ID, paymentInputStruct.shipToEmailId);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_SHIP_TO_FIRST_NAME, paymentInputStruct.shipToFirstName);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_SHIP_TO_LAST_NAME, paymentInputStruct.shipToLastName);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_SHIP_TO_ID, paymentInputStruct.shipToId);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_SHIP_TO_KEY, paymentInputStruct.shipTokey);    
	    paymentRoot.setAttribute(AcademyConstants.ATTR_SHIP_TO_STATE, paymentInputStruct.shipToState);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_SHIP_TO_ZIP_CODE, paymentInputStruct.shipToZipCode);
	    
	    paymentRoot.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_1, paymentInputStruct.paymentReference1);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_2, paymentInputStruct.paymentReference2);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_3, paymentInputStruct.paymentReference3);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_4, paymentInputStruct.paymentReference4);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_5, paymentInputStruct.paymentReference5);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_6, paymentInputStruct.paymentReference6);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_7, paymentInputStruct.paymentReference7);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_8, paymentInputStruct.paymentReference8);
	    paymentRoot.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_9, paymentInputStruct.paymentReference9);
	    
	    //currentAuthorizationCreditCardTransactions : Data of the original authorization from the YFS_CREDIT_CARD_TRANSACTION table in XML format.
	    /*
	     * <Payment AuthorizationId="" BillToAddressLine1="" BillToCity="" BillToCountry="" BillToDayPhone="" BillToEmailId=""
	     * ....
	     * <CreditCardTransactions>
			<CreditCardTransaction AuthAmount="" AuthAvs="" AuthCode="" AuthReturnCode="" AuthReturnFlag="" AuthReturnMessage=""
			AuthTime="" CVVAuthCode="" ChargeTransactionKey=""CreditCardTransactionKey="" InternalReturnCode=""
			InternalReturnFlag="" InternalReturnMessage="" ParentKey="" Reference1="" Reference2="" RequestId=""
			TranAmount="" TranRequestTime=""TranReturnCode="" TranReturnFlag="" TranReturnMessage="" TranType=""/>
			</CreditCardTransactions>
			</Payment>
	     */
	    if (!YFCObject.isVoid(paymentInputStruct.currentAuthorizationCreditCardTransactions)) {
	      Document doc = YFCDocument.getDocumentFor(paymentInputStruct.currentAuthorizationCreditCardTransactions).getDocument();
	      XMLUtil.importElement(paymentRoot,doc.getDocumentElement());
	    }	
	    if (!YFCObject.isVoid(paymentInputStruct.currentAuthorizationExpirationDate)) {
	      YFCDate currentExpDate = new YFCDate(paymentInputStruct.currentAuthorizationExpirationDate);
	      paymentRoot.setAttribute(AcademyConstants.ATTR_CURRENT_AUTHORIZATION_EXPIRATION_DATE, currentExpDate.getString());
	    }
	    
	    paymentInputDoc.appendChild(paymentRoot);
	    
	    log.verbose("After PaymentInputStructToXML conversion :" +XMLUtil.getXMLString(paymentInputDoc));
	    log.verbose("Exit AcademyPaymentProcessingUtil.convertPaymentInputStructToXML() method :");
	
	    return paymentInputDoc;
	}
	//RTS PaymentProcessingUtil changes : End

	//Start PayZ : MPMT-111,112
	/** This method logs the PayZ request and response to database based on the PayZ response code
	 * @param env
	 * @param docPayZReq , docPayZResp
	 * @throws ParserConfigurationException
	 * @throws DOMException
	 * @throws Exception
	 */
	public static void logPayZReqAndRespToDB(YFSEnvironment env, Document docPayInput, Document docPayZReq, Document docPayZResp,String strDBServiceName) throws Exception {
		log.verbose("Start AcademyPaymentProcessingUtil.logPayZReqAndRespToDB() method ::");
		Document docPayZReqResp = XMLUtil.createDocument(AcademyConstants.ELE_PAYZ_REQ_RESP); 
		Element elePayZReqResp = docPayZReqResp.getDocumentElement();

		if(!YFSObject.isVoid(docPayZReq)){
			Element elePayZReq = docPayZReqResp.createElement(AcademyConstants.ELE_PAYZ_REQ);
			Element elePayZReqTemp = docPayZReq.getDocumentElement();
			elePayZReqResp.appendChild(elePayZReq);
			XMLUtil.importElement(elePayZReq, elePayZReqTemp);
		}
		
		if(!YFSObject.isVoid(docPayZResp)){
			Element elePayZResp = docPayZReqResp.createElement(AcademyConstants.ELE_PAYZ_RESP);
			Element elePayZRespTemp = docPayZResp.getDocumentElement();
			elePayZReqResp.appendChild(elePayZResp);
			XMLUtil.importElement(elePayZResp, elePayZRespTemp);
		}
		Element elePayInput = docPayZReqResp.createElement(AcademyConstants.ELE_PAYMENT_INPUT);
		Element elePayInputTemp = docPayInput.getDocumentElement();
		elePayZReqResp.appendChild(elePayInput);
		XMLUtil.importElement(elePayInput, elePayInputTemp);

		AcademyUtil.invokeService(env, strDBServiceName, docPayZReqResp);		
		log.verbose("End AcademyPaymentProcessingUtil.logPayZReqAndRespToDB() method ::");
	}
	//End PayZ : MPMT-111,112
}