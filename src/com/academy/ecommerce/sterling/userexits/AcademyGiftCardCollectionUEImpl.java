package com.academy.ecommerce.sterling.userexits;
 
import java.util.Date;
import java.util.HashMap;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.academy.ecommerce.sterling.util.AcademyPaymentProcessingUtil;
import com.academy.ecommerce.sterling.util.AcademyRTSUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.common.AcademyUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfs.japi.YFSExtnPaymentCollectionInputStruct;
import com.yantra.yfs.japi.YFSExtnPaymentCollectionOutputStruct;
import com.yantra.yfs.japi.YFSUserExitException;
import com.yantra.yfs.japi.ue.YFSCollectionStoredValueCardUE;

/**
 * SVC UserExit is implemented to have custom logic to ReAuth, reverse auth and Charge Orders with tender type as 'GIFT_CARD'
 * @author C0007277
 *
 */

public class AcademyGiftCardCollectionUEImpl implements YFSCollectionStoredValueCardUE {
	
	String strNextTriggerIntervalInMin = null;
  	String strNextTriggerIntervalInMinAuthError = null;
  	String strIsDBLoggingEnable = null;
  	String strDBServiceName = null;
  	String strAuthNoRetryErrorCodes = null;
  	String strSettleNoRetryErrorCodes = null;
  	//START : GCD-59 to hold the error codes for which technical error has to be raised.
  	String strAuthTechErrorCodes = null;
  	String strSettleTechErrorCodes = null;
  	//END : GCD-59
  	int iGCAuthStrikesLimit = 0;  	
  	boolean bAuthStrikeLimitReached = false;
  	
  	private YIFApi api = null;
  	
	public static YFCLogCategory log = YFCLogCategory.instance(AcademyGiftCardCollectionUEImpl.class);
	/**
	 * Implements UE method and does follwoing
	 * 1.Check if the request is for expired auth then do dummy auth reversal else invokes OOB service which inturn call RTS service.
	 * 2.After the response is received from RTS based on the charge type YFSExtnPaymentCollectionOutputStruct object is set and returned from UE.
	 */
	public YFSExtnPaymentCollectionOutputStruct collectionStoredValueCard(YFSEnvironment oEnv, YFSExtnPaymentCollectionInputStruct paymentInputStruct) throws YFSUserExitException {
		log.verbose("Inside collectionStoredValueCardUE");
        YFSExtnPaymentCollectionOutputStruct paymentOutputStruct;
        log.beginTimer("YCDCollectionSVCImpl");
        log.debug("Begin AcademyGiftCardCollectionUEImpl");
        Element eleRTSRequest=null;
        Element eleRTSResponse=null;
        paymentOutputStruct = new YFSExtnPaymentCollectionOutputStruct();
        String chargeType = paymentInputStruct.chargeType;           
        
        YFSEnvironment newEnv = null;
        //Start : OMNI-104596 : Handling Exception cases for Gift Cards
        Document docRTSException = null;
        //End : OMNI-104596 : Handling Exception cases for Gift Cards

        try
        {	    
        	 //Start : OMNI-104596 : Handling Exception cases for Gift Cards
            docRTSException = XMLUtil.createDocument("RTSException"); 
    		Element eleRTSException = docRTSException.getDocumentElement();
            //End : OMNI-104596 : Handling Exception cases for Gift Cards
    		
        	//Code changes to bypass webservices based on java version
        	//String strRetryEnabled = YFSSystem.getProperty("academy.payz.cardinal.retry.enabled");
    		String strRetryUsingAppEnabled = YFSSystem.getProperty(AcademyConstants.PROP_CARDNL_RETRY_ENABLED_USING_APP);
    		//log.verbose("strRetryEnabled :: "+strRetryEnabled);
    		log.verbose("strRetryUsingAppEnabled :: "+strRetryUsingAppEnabled);
    		//if(!YFCObject.isNull(strRetryEnabled) && strRetryEnabled.equals("Y")) {
    			if(!YFCObject.isNull(strRetryUsingAppEnabled)&& strRetryUsingAppEnabled.equals(AcademyConstants.STR_YES)) {
    				log.verbose(" Creating a new environment ");
    				newEnv = createEnvironment(oEnv.getProgId());
    			}
    			/*else {
    				String strDelayInterval = YFSSystem.getProperty("academy.payz.cardinal.delay.seconds");
    				String strJavaVersion = System.getProperty("java.version").trim();
    	    		log.verbose(" GC Code changes to bypass webservices based on java version ");
    	    		if (!YFSObject.isVoid(strJavaVersion) &&  strJavaVersion.startsWith("1.6")) {
    	    			log.verbose(" GC Orders being fulfilled from java 1.6 ");
    	    		}
    	    		else {
    	    			log.verbose(" Skip calls from this JVM. ");
    	    			paymentOutputStruct.retryFlag = "Y"; 
    	    			
    	    			Calendar cal = Calendar.getInstance();
    	    			SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN_1);		
    	    			cal.add(Calendar.SECOND, Integer.parseInt(strDelayInterval));
    	    			String strCollectionDate = sdf.format(cal.getTime());
    	    			
    	    			paymentOutputStruct.collectionDate = sdf.parse(strCollectionDate);
    	    			return paymentOutputStruct;
    	    		}
    			}			
    		}*/
    		//Code changes to bypass webservices based on java version
    		
        	//Using custom method to convert paymentInputStruct to XML
        	Document paymentInputDoc = AcademyPaymentProcessingUtil.convertPaymentInputStructToXML(paymentInputStruct);
            
        	//Start : OMNI-104596 : Handling Exception cases for Gift Cards
        	Element elePayInput = docRTSException.createElement(AcademyConstants.ELE_PAYMENT_INPUT);
    		Element elePayInputTemp = paymentInputDoc.getDocumentElement();
    		eleRTSException.appendChild(elePayInput);
    		XMLUtil.importElement(elePayInput, elePayInputTemp);
            //End : OMNI-104596 : Handling Exception cases for Gift Cards

            //PMR 72976,004,000 :: Since we are not able to access Auth Expiry date from paymentInputStruct, calling getChargeTransactionList API
            Date authExpiryDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(getAuthorizationExpiryDate(oEnv,paymentInputStruct.chargeTransactionKey));
            paymentInputStruct.currentAuthorizationExpirationDate =  authExpiryDate;
                                    
            log.verbose("ChargeType::"+paymentInputStruct.chargeType+"\n"+"AuthExpiryDate::"+paymentInputStruct.currentAuthorizationExpirationDate);
            //Do dummy auth for expired auth reversals : currentAuthorizationExpirationDate < sysdate and requestAmount is negative as the authorization is already released.
            if(AcademyConstants.STR_CHRG_TYPE_AUTH.equalsIgnoreCase(chargeType) 
            	&& paymentInputStruct.currentAuthorizationExpirationDate.before(Calendar.getInstance().getTime())
            	&& paymentInputStruct.requestAmount < 0){
            	log.verbose("Begin AuthorizationDateCheck");
            	//dummy auth code 
                paymentOutputStruct.authCode = AcademyRTSUtil.randomstring(1, 1);
                paymentOutputStruct.authorizationAmount = paymentInputStruct.requestAmount;
                //dummy authorization id 
                paymentOutputStruct.authorizationId = AcademyRTSUtil.randomstring(6, 6);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                Calendar cal = Calendar.getInstance();
                paymentOutputStruct.authTime = sdf.format(cal.getTime());
                paymentOutputStruct.PaymentReference1 = paymentInputStruct.paymentReference1;
                paymentOutputStruct.requestID = paymentInputStruct.orderNo;
                paymentOutputStruct.SvcNo = paymentInputStruct.svcNo;
                
                Double processedAmount = Double.valueOf(paymentInputStruct.requestAmount);
                paymentOutputStruct.tranAmount = processedAmount.doubleValue();
                paymentOutputStruct.tranType = paymentInputStruct.chargeType;
                paymentOutputStruct.tranReturnCode = "101";
                paymentOutputStruct.authorizationExpirationDate = getAuthExpiryDateForGC(oEnv);
                log.verbose((new StringBuilder("output for Auth expiry")).append(paymentOutputStruct).toString());
                log.endTimer("YCDCollectionSVCImpl : Dummy Auth");
                
                return paymentOutputStruct;
            }
            else{
            	  //AcademyInvokeRTSHTTPGCService will be passed as argument in Service.
            	//Code changes to bypass webservices based on java version
            	Document docRTSReqRes;
            	if(!YFCObject.isNull(newEnv)) {
            		log.verbose("Invoking the service with the new environment object");
            		docRTSReqRes = api.executeFlow(newEnv, "YCD_ExecuteCollectionSVC_Proxy_1.0", paymentInputDoc);
            	}
            	else {
            		docRTSReqRes = AcademyUtil.invokeService(oEnv,"YCD_ExecuteCollectionSVC_Proxy_1.0", paymentInputDoc);
            	}
            	//Code changes to bypass webservices based on java version
            	
                log.verbose("YCD_ExecuteCollectionSVC_Proxy_1.0 : Collection Output XML is :: "+XMLUtil.getXMLString(docRTSReqRes));
                //Service returns a document which will have RTS request and response XMls.
                eleRTSRequest = XMLUtil.getElementByXPath(docRTSReqRes, AcademyConstants.XPATH_RTS_REQ);
                eleRTSResponse = XMLUtil.getElementByXPath(docRTSReqRes, AcademyConstants.XPATH_RTS_RESP);
                log.verbose("RTSRequestXML::"+XMLUtil.getElementXMLString(eleRTSRequest));
                log.verbose("RTSResponseXML::"+XMLUtil.getElementXMLString(eleRTSResponse));
                       
                if (AcademyConstants.STR_CHRG_TYPE_AUTH.equalsIgnoreCase(chargeType))
    			{
    				log.verbose("chargeType is AUTHORIZATION *** ");			     
    				paymentOutputStruct = translateRTSResponse(oEnv, paymentInputStruct, eleRTSResponse, eleRTSRequest, docRTSReqRes);
    			}		
    			else if (AcademyConstants.STR_CHRG_TYPE_CHARGE.equalsIgnoreCase(chargeType))
    			{
    				log.verbose("chargeType is CHARGE *** ");
    				paymentOutputStruct = translateRTSResponseForSettle(oEnv, paymentInputStruct, eleRTSRequest, eleRTSResponse, eleRTSRequest, docRTSReqRes);
    			}
           }
        }
        
        //START : GCD-131,GCD-132
        catch(YFSException e){   
        	String strErrorCode = e.getErrorCode();
        	log.verbose("Inside YFSException Catch block :: ErrorCode : "+strErrorCode+", chargeType : "+chargeType);	
        	//Start : OMNI-104596 : Handling Exception cases for Gift Cards
        	log.error("Gift Card Error :: For Order# " + paymentInputStruct.orderNo
        			+ " Webservice Invocation error :: " + e.getErrorDescription()); 
        	log.verbose((new StringBuilder("RTS call : Exception")).append(e).toString());
        	//SocketTimeoutException exception is getting wrapped into YFSException, hence checking for ErrorCode
        	docRTSException = prepareExceptionResponse(docRTSException, paymentInputStruct.orderNo, chargeType, strErrorCode, e.getErrorDescription());
        	
			try {
				getGCcommonCodeValues(oEnv);
				if("Y".equals(strIsDBLoggingEnable) && (!YFCObject.isVoid(strDBServiceName))){					
					AcademyUtil.invokeService(oEnv, strDBServiceName, docRTSException);				
				}
			}catch (Exception e1) {
				log.verbose("Error while updating the log in the DB");
				e1.printStackTrace();
			}
			handleExceptions(oEnv, paymentInputStruct, paymentOutputStruct, chargeType);
        	//End : OMNI-104596 : Handling Exception cases for Gift Cards
        
        }//END : GCD-131,GCD-132
        catch(Exception e){
        	log.verbose("Exception occurred!!");
        	//Start : OMNI-104596 : Handling Exception cases for Gift Cards
        	log.error("Gift Card Error :: For Order# " + paymentInputStruct.orderNo
        			+ " Webservice Invocation error :: " + e.getMessage()); 
        	log.verbose((new StringBuilder("RTS call : Exception")).append(e).toString());
        	docRTSException = prepareExceptionResponse(docRTSException, paymentInputStruct.orderNo, chargeType, "Generic Error", e.getMessage());
			if("Y".equals(strIsDBLoggingEnable) && (!YFCObject.isVoid(strDBServiceName))){
				try {
                    getGCcommonCodeValues(oEnv);
					AcademyUtil.invokeService(oEnv, strDBServiceName, docRTSException);
				} catch (Exception e1) {
		    		log.verbose("Error while updating the log in the DB");
		    		e1.printStackTrace();
				}
			}
        	//End : OMNI-104596 : Handling Exception cases for Gift Cards
			
			handleExceptions(oEnv, paymentInputStruct, paymentOutputStruct, chargeType);   
        }
        finally{
            log.endTimer("Exiting YCDCollectionSVCImpl");
        }
        log.debug("YCDCollectionSVCImpl END-> paymentOutputStruct"+paymentOutputStruct);
        return paymentOutputStruct;
    
	}
	
	/**
	 * Method calls getChargeTransactionList API to fetch AuthorizationExpirationDate
	 * With Authorization Reversal Strategy : "Reverse Excess", we will have 2 entires in YCT - AuthReversal and New Auth
	   We need currentAuthorizationExpirationDate to differentiate between expired authorization vs actual Auth reversal(non expired Auth)
	 * @param chargeTransactionKey
	 * @return AuthorizationExpirationDate
	 */
	public String getAuthorizationExpiryDate(YFSEnvironment env, String chargeTransactionKey) throws Exception{
		log.verbose("Entering getAuthorizationExpiryDate()");
		
		Document getChargeTransactionDetailsInDoc = XMLUtil.createDocument(AcademyConstants.ELE_CHARGE_TRANS);
		getChargeTransactionDetailsInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY,chargeTransactionKey);		
		log.verbose("Input to getChargeTransactionList API :: "+XMLUtil.getXMLString(getChargeTransactionDetailsInDoc));		
		String strGetChargeTransactionListTemp = "<ChargeTransactionDetails><ChargeTransactionDetail AuthorizationExpirationDate=' '/></ChargeTransactionDetails>";
		Document outputTemplate = YFCDocument.getDocumentFor(strGetChargeTransactionListTemp).getDocument();
		
		env.setApiTemplate(AcademyConstants.API_GET_CHARGE_TRANSACTION_LIST,outputTemplate);
		Document getChargeTransactionDetailsOutDoc = AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_CHARGE_TRANSACTION_LIST,getChargeTransactionDetailsInDoc);
		env.clearApiTemplate(AcademyConstants.API_GET_CHARGE_TRANSACTION_LIST);
		
		log.verbose("Output from getChargeTransactionList API :: "+XMLUtil.getXMLString(getChargeTransactionDetailsOutDoc));
		Element eleChargeTranDetails = (Element)getChargeTransactionDetailsOutDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_CHARGE_TRANS).item(0);
		log.verbose("getChargeTransactionList.AuthorizationExpirationDate :: "+eleChargeTranDetails.getAttribute(AcademyConstants.ATTR_AUTH_EXP_DATE));
		
		String strAuthExpDate = eleChargeTranDetails.getAttribute("AuthorizationExpirationDate");
		//In case Auth Exp Date is blank, return previous date so that ReAuth happens
		if(YFCCommon.isVoid(strAuthExpDate)){
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");			
			cal.add(Calendar.DATE, -1);
			strAuthExpDate = sdf.format(cal.getTime());
		}
		log.verbose("Exiting getAuthorizationExpiryDate()");
		return strAuthExpDate;
	}
	/**
	 * Sets attribute related to retry.
	 * @param response
	 * @param paymentOutputStruct
	 * @throws Exception
	 */
	private void retryAuthorization(Element response, YFSExtnPaymentCollectionOutputStruct paymentOutputStruct) throws Exception{
		log.verbose("Inside retryAuthorization ");
		paymentOutputStruct.retryFlag = "Y";
		paymentOutputStruct.authReturnMessage = response.getElementsByTagName(AcademyConstants.RTS_RESP_ERROR_DESC).item(0).getTextContent();		
		if(!YFSObject.isVoid(strNextTriggerIntervalInMin)){
			paymentOutputStruct.collectionDate = getCollectionDate();
		}	
		log.verbose("Setting next available date for retry to ::"+paymentOutputStruct.collectionDate+"\n Exiting retryAuthorization()");
	}
	/**
	 * Method returns the next time interval for retry.
	 * @return
	 * @throws ParseException
	 */
	public Date getCollectionDate() throws ParseException{
		log.verbose("Inside getCollectionDate()");
		Date collectionDate = null;
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");			
		int iNextTriggerIntervalInMin = Integer.parseInt(strNextTriggerIntervalInMin);			
		cal.add(Calendar.MINUTE, iNextTriggerIntervalInMin);			
		String strCollectionDate = sdf.format(cal.getTime());
		collectionDate =  sdf.parse(strCollectionDate);
		log.verbose("Exiting getCollectionDate()");
		return collectionDate;
		
	}
	/**
	 * Get authorization expiry date for new authorization.
	 * @param env
	 * @return
	 * @throws Exception
	 */
	public String getAuthExpiryDateForGC(YFSEnvironment env) throws Exception{
		log.verbose("Entering getAuthExpiryDateForGC()");
		Document getCommonCodeListinDoc = XMLUtil.createDocument("CommonCode");
		getCommonCodeListinDoc.getDocumentElement().setAttribute("CodeType",AcademyConstants.GC_AUTHEXP_DAYS);
		getCommonCodeListinDoc.getDocumentElement().setAttribute("OrganizationCode", AcademyConstants.HUB_CODE);
		
		Document getCommonCodeListoutDoc = AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_COMMON_CODELIST,getCommonCodeListinDoc);
		
		String strAuthExpDays = ((Element) getCommonCodeListoutDoc.getElementsByTagName("CommonCode").item(0)).getAttribute("CodeValue");
		int authExpDays = Integer.parseInt(strAuthExpDays);
		log.debug("getAuthExpiryDateForGC :: AuthExpDays CommonCode --> " + authExpDays);
				
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, authExpDays); // Adding configured # of days
		String strAuthExpDate = sdf.format(c.getTime());		
		String strAuthExpirationDate = strAuthExpDate.replace("-", "").replace("T", "").replace(":", "");		
		log.debug("getAuthExpiryDateForGC :: strExpirationDate  --> " + strAuthExpirationDate);
		log.verbose("Exiting getAuthExpiryDateForGC()");
		return strAuthExpirationDate;
	}
	
	/**
	 * Method checks if retry limit has reached.
	 * iGCAuthStrikesLimit is a configurable paramater.
	 * It is added as commonCode under codeType GC_VVALIDATION
	 * @param paymentInputStruct
	 * @param paymentOutputStruct
	 * @return
	 */
	public boolean processAuthStrikes(YFSEnvironment env,YFSExtnPaymentCollectionInputStruct paymentInputStruct,YFSExtnPaymentCollectionOutputStruct paymentOutputStruct) throws Exception{
		log.verbose("Entering processAuthStrikes()");		
		//START::retry authorization and settlement failures.
		boolean bAuthStrikeLimitReached = false;
		int iNumberOfStrikes;		
		Document docGetAcadGCPayRetryList = null,docGetAcadGCPayRetryListOutput = null;
		
		//Custome table ACAD_GC_PAY_RETRY, will maintain no of Strikes Counter. Fetch the counter value to see if it has reached StrikeLimit and raise alert 
		docGetAcadGCPayRetryList = XMLUtil.createDocument(AcademyConstants.ELE_ACAD_GC_PAY_RETRY);
		Element eleAcadGetGCPayRetryList = docGetAcadGCPayRetryList.getDocumentElement();
		eleAcadGetGCPayRetryList.setAttribute(AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY, paymentInputStruct.chargeTransactionKey);
		log.verbose("AcademyGetAcadGcPayRetryList input :: " +XMLUtil.getXMLString(docGetAcadGCPayRetryList));
		
		docGetAcadGCPayRetryListOutput = AcademyUtil.invokeService(env,"AcademyGetAcadGcPayRetryList", docGetAcadGCPayRetryList);
		
		log.verbose("AcademyGetAcadGcPayRetryList output :: " +XMLUtil.getXMLString(docGetAcadGCPayRetryListOutput));
		NodeList nlgcPayRetry = docGetAcadGCPayRetryListOutput.getElementsByTagName("ACADGCPayRetry");
		log.verbose("The nodeList length is:" +nlgcPayRetry.getLength());
		
		if (nlgcPayRetry.getLength() == 0){
			iNumberOfStrikes = 0;
			//If the Auth/Charge Strike has failed for the first time. Create an entry in ACAD_GC_PAY_RETRY for retrying and set Counter value to Zero.
			invokeAcademyCreateAcadGcPayRetry(env, paymentInputStruct);
		}else{
			//If the Auth/Charge Retry Strike has failed. Increment the Counter value
			iNumberOfStrikes = invokeAcademyChangeAcadGcPayRetry(env, paymentInputStruct, nlgcPayRetry);
		}
		//END:retry authorization and settlement failures.
		
	    if (iNumberOfStrikes >= iGCAuthStrikesLimit) {
	      bAuthStrikeLimitReached = true;	     
	   }
		
	    log.verbose("Exiting processAuthStrikes()");
	    return bAuthStrikeLimitReached;
	    
	}
	
	/**
	 * Method Creates an entry in ACAD_GC_PAY_RETRY for retrying and set NumberOfRetry Counter value to Zero
	 * @param env
	 * @param paymentInputStruct
	 */
	private void invokeAcademyCreateAcadGcPayRetry(YFSEnvironment env, YFSExtnPaymentCollectionInputStruct paymentInputStruct) throws Exception{
		log.verbose("Entering invokeAcademyCreateAcadGcPayRetry ::");
		Document docCreateAcadGCPayRetry = null ,docCreateAcadGCPayRetryOutput = null ;
		docCreateAcadGCPayRetry = XMLUtil.createDocument(AcademyConstants.ELE_ACAD_GC_PAY_RETRY);
		Element eleAcadGCPayRetry = docCreateAcadGCPayRetry.getDocumentElement();
		eleAcadGCPayRetry.setAttribute(AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY, paymentInputStruct.chargeTransactionKey);
		eleAcadGCPayRetry.setAttribute(AcademyConstants.ATTR_CHARGE_TYPE,paymentInputStruct.chargeType);
		eleAcadGCPayRetry.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,paymentInputStruct.orderHeaderKey);		
		eleAcadGCPayRetry.setAttribute(AcademyConstants.ATTR_NUMBER_OF_RETRY,AcademyConstants.STR_ZERO);
		log.verbose("AcademyCreateAcadGcPayRetry input :: " +XMLUtil.getXMLString(docCreateAcadGCPayRetry));
		
		//insert record into ACAD_GC_PAY_RETRY 
		docCreateAcadGCPayRetryOutput = AcademyUtil.invokeService(env,"AcademyCreateAcadGcPayRetry", docCreateAcadGCPayRetry);
		
		log.verbose("Exiting invokeAcademyCreateAcadGcPayRetry :: " +XMLUtil.getXMLString(docCreateAcadGCPayRetryOutput));
	}
	
	/**
	 * Method increments NumberOfRetry Counter value in ACAD_GC_PAY_RETRY for the corresponding chargeTransaction entry.
	 * @param env
	 * @param paymentInputStruct
	 * @param nlgcPayRetry
	 * @return iNumberOfStrikes
	 */
	private int invokeAcademyChangeAcadGcPayRetry(YFSEnvironment env, YFSExtnPaymentCollectionInputStruct paymentInputStruct, NodeList nlgcPayRetry) throws Exception{
		log.verbose("Entering invokeAcademyChangeAcadGcPayRetry ::");
		Element eleACADGCPayRetry = (Element) nlgcPayRetry.item(0);			
		int iNumberOfStrikes = Integer.parseInt(eleACADGCPayRetry.getAttribute(AcademyConstants.ATTR_NUMBER_OF_RETRY));
		log.verbose("iNumberOfStrikes :: "+iNumberOfStrikes);
		Document docChangeAcadGCPayRetry = null , docChangeAcadGCPayRetryOutput = null;			
		docChangeAcadGCPayRetry = XMLUtil.createDocument(AcademyConstants.ELE_ACAD_GC_PAY_RETRY);
		Element eleAcadChangeGCPayRetry = docChangeAcadGCPayRetry.getDocumentElement();
		eleAcadChangeGCPayRetry.setAttribute(AcademyConstants.ATTR_CHARGE_TRANSACTION_KEY, paymentInputStruct.chargeTransactionKey);
		eleAcadChangeGCPayRetry.setAttribute(AcademyConstants.ATTR_NUMBER_OF_RETRY, Integer.toString(++iNumberOfStrikes));
		eleAcadChangeGCPayRetry.setAttribute(AcademyConstants.ATTR_GCPAY_RETRY_KEY,eleACADGCPayRetry.getAttribute(AcademyConstants.ATTR_GCPAY_RETRY_KEY));
		log.verbose("AcademyChangeAcadGcPayRetry input :: " +XMLUtil.getXMLString(docChangeAcadGCPayRetry));
		
		docChangeAcadGCPayRetryOutput = AcademyUtil.invokeService(env,"AcademyChangeAcadGcPayRetry", docChangeAcadGCPayRetry);
		
		log.verbose("Exiting invokeAcademyChangeAcadGcPayRetry :: " +XMLUtil.getXMLString(docChangeAcadGCPayRetryOutput));
		return iNumberOfStrikes;
	}
	
	/**
	 * Method fetches CommodeCode values for GC validation
	 * @param env
	 * @throws Exception
	 */
	private void getGCcommonCodeValues(YFSEnvironment env) throws Exception
	{
		log.verbose("Inside getGCcommonCodeValues()");
		Document docCommonCodeOutput =  null;
		Document docCommonCodeInput = null;
		NodeList commonCodeNL = null;
		int iCommonCodeNLLen = 0;
		String strCodeValue = null;
		String strCodeShortDesc = null;
		Element eleCommonCode = null;
		String STR_GET_COMMON_CODE_LIST_OUT_TEMP = "<CommonCodeList> <CommonCode CodeValue='' CodeShortDescription='' /> </CommonCodeList>";

		docCommonCodeInput = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
		docCommonCodeInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE, AcademyConstants.STR_GC_VALIDATION);
		docCommonCodeInput.getDocumentElement().setAttribute(AcademyConstants.ORGANIZATION_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
		log.verbose("Input to getCommonCodeList API is : "+XMLUtil.getXMLString(docCommonCodeInput));
		Document outputTemplate = YFCDocument.getDocumentFor(STR_GET_COMMON_CODE_LIST_OUT_TEMP).getDocument();
		
		env.setApiTemplate(AcademyConstants.API_GET_COMMON_CODELIST, outputTemplate);
		docCommonCodeOutput = AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_COMMON_CODELIST,docCommonCodeInput);
		env.clearApiTemplate(AcademyConstants.API_GET_COMMON_CODELIST);
		
		log.verbose("Output from getCommonCodeList API is : "+XMLUtil.getXMLString(docCommonCodeOutput));
		commonCodeNL = docCommonCodeOutput.getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);
		iCommonCodeNLLen = commonCodeNL.getLength();
		for(int i = 0;i<iCommonCodeNLLen;i++)
		{
			eleCommonCode = (Element)commonCodeNL.item(i);
			strCodeValue = eleCommonCode.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE);
			strCodeShortDesc = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
			
			if(AcademyConstants.STR_NEXT_TRIGGER_IN_MIN.equals(strCodeValue))
			{
				strNextTriggerIntervalInMin= strCodeShortDesc;
				log.verbose("strNextTriggerIntervalInMin::"+strNextTriggerIntervalInMin);
			}
			if(AcademyConstants.STR_NEXT_TRIGGER_IN_MIN_AUTH_ERROR.equals(strCodeValue))
			{
				strNextTriggerIntervalInMinAuthError= strCodeShortDesc;
				log.verbose("strNextTriggerIntervalInMinAuthError::"+strNextTriggerIntervalInMinAuthError);
			}
			if(AcademyConstants.STR_IS_DB_LOG_ENABALE.equals(strCodeValue))
			{
				strIsDBLoggingEnable = strCodeShortDesc;
				log.verbose("strIsDBLoggingEnable::"+strIsDBLoggingEnable);
			}
			if(AcademyConstants.STR_DB_SERVICE_NAME.equals(strCodeValue))
			{
				strDBServiceName = strCodeShortDesc;
				log.verbose("strDBServiceName::"+strDBServiceName);
			}
			if(AcademyConstants.STR_AUTH_NO_RETRY_ERROR_CODES.equals(strCodeValue))
			{
				strAuthNoRetryErrorCodes = strCodeShortDesc;
				log.verbose("strAuthRetryErrorCodes::"+strAuthNoRetryErrorCodes);
			}
			if(AcademyConstants.STR_CHARGE_NO_RETRY_ERROR_CODES.equals(strCodeValue))
			{
				strSettleNoRetryErrorCodes = strCodeShortDesc;
				log.verbose("strSettleNoRetryErrorCodes::"+strSettleNoRetryErrorCodes);
			}		
			if(AcademyConstants.STR_AUTH_TECH_ERROR_CODES.equals(strCodeValue))
			{
				strAuthTechErrorCodes = strCodeShortDesc;
				log.verbose("strAuthTechErrorCodes::"+strAuthTechErrorCodes);
			}
			if(AcademyConstants.STR_CHARGE_TECH_ERROR_CODES.equals(strCodeValue))
			{
				strSettleTechErrorCodes = strCodeShortDesc;
				log.verbose("strSettleTechErrorCodes::"+strSettleTechErrorCodes);
			}			
			if("AuthStrikeLimit".equals(strCodeValue))
			{
				iGCAuthStrikesLimit = Integer.parseInt(strCodeShortDesc);
				log.verbose("iGCAuthStrikesLimit::"+iGCAuthStrikesLimit);
			}
		}
	}
	
	/**
	 * Method handles "Auth" response from RTS. All scenarios Success/Decline/Unavailable
	 * @param paymentInputStruct
	 * @param response
	 * @return paymentOutputStruct
	 * @throws Exception
	 */
	public YFSExtnPaymentCollectionOutputStruct translateRTSResponse(YFSEnvironment env,YFSExtnPaymentCollectionInputStruct paymentInputStruct,Element response,Element request,Document RTSReqRes) throws Exception{
		log.verbose("Inside translateRTSResponse()");
		log.beginTimer("Auth:translateRTSResponse");
		YFSExtnPaymentCollectionOutputStruct paymentOutputStruct = new YFSExtnPaymentCollectionOutputStruct();
		boolean bPartialAuth = false;
		//RTS atrribute that will have the status of the call.
		//If value is returned as zero then it is successfull call.

		if(!YFCCommon.isVoid(response)){
		String strActionCode = response.getElementsByTagName(AcademyConstants.RTS_RESP_ACTION_CODE).item(0).getTextContent();
		if (AcademyConstants.STR_RTS_SUCCESS_RESP.equals(strActionCode)) {
			log.verbose("Authorization was successful");
			
			//Start : GCD-136
			String strIxOptions = response.getElementsByTagName(AcademyConstants.ATTR_RTS_OPTIONS).item(0).getTextContent();
			String strTranLock = strIxOptions.substring(strIxOptions.indexOf("=") + 1, strIxOptions.length());
			String strIxAmount = response.getElementsByTagName(AcademyConstants.ATTR_RTS_AMOUNT).item(0).getTextContent();
			log.verbose("strIxOptions is ::"+strIxOptions+" strTranLock is ::"+strTranLock+" strIxAmount is ::"+strIxAmount);
			log.verbose("TranLock : "+(Double.parseDouble(strTranLock))/100 +" IxAmount : "+(Double.parseDouble(strIxAmount))/100);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			Calendar cal = Calendar.getInstance();
			
			//Assuming Success Auth wont get response with TranLock=0, in that case we will be wrongly reading that scenario as partial auth
			if((Double.parseDouble(strTranLock))/100 < (Double.parseDouble(strIxAmount))/100)
				bPartialAuth = true;
			
			if(bPartialAuth)
				paymentOutputStruct.authorizationAmount = Double.parseDouble(strTranLock)/100;
			else
				paymentOutputStruct.authorizationAmount = paymentInputStruct.requestAmount;	
			//End : GCD-136
			paymentOutputStruct.authCode = response.getElementsByTagName(AcademyConstants.RTS_RESP_LOCK_ID).item(0).getTextContent();//LockID is stored in YCCT.Auth_code			
			paymentOutputStruct.tranAmount = paymentInputStruct.requestAmount;;
			paymentOutputStruct.authorizationId = response.getElementsByTagName(AcademyConstants.ATTR_RTS_AUTH_CODE).item(0).getTextContent();			
			paymentOutputStruct.authTime = sdf.format(cal.getTime());
			paymentOutputStruct.requestID = response.getElementsByTagName(AcademyConstants.ATTR_RTS_INVOICE).item(0).getTextContent();
			paymentOutputStruct.SvcNo = paymentInputStruct.svcNo;
			paymentOutputStruct.tranType = paymentInputStruct.chargeType;
			paymentOutputStruct.tranReturnCode = response.getElementsByTagName(AcademyConstants.ATTR_RTS_CMD).item(0).getTextContent();
			paymentOutputStruct.authorizationExpirationDate = getAuthExpiryDateForGC(env);
			//tranRequestTime not used for paypal. If not passed OOB only will set YCCT.tran_request_time to currentTime
			//paymentOutputStruct.tranRequestTime = (response.getElementsByTagName("IxDate").item(0).getTextContent()).concat(response.getElementsByTagName("IxTime").item(0).getTextContent());
			
		}
		//START: GCD-59 Error handling for authorization failure scenarios
		//Action code for rejected request can be 1 or 2
		else if(!YFCCommon.isVoid(strActionCode)){
			log.verbose("Authorization failed!!");
			log.verbose("ActionCode is ::"+strActionCode);
			
			//Common code will have the parameters related to error handling.
			getGCcommonCodeValues(env);
				
			//logs the RTS request and Response XMLS in DB when ActionCode is not Zero.
			if("Y".equals(strIsDBLoggingEnable) && (!YFSObject.isVoid(strDBServiceName))){
				AcademyUtil.invokeService(env, strDBServiceName, RTSReqRes);
			}

			String rtsErrorCode = response.getElementsByTagName(AcademyConstants.RTS_RESP_ERROR_CODE).item(0).getTextContent();
			String rtsErrorDescription = response.getElementsByTagName(AcademyConstants.RTS_RESP_ERROR_DESC).item(0).getTextContent();
			log.verbose("ErrorCode::"+rtsErrorCode+"\n"+"ErrorDescription::"+rtsErrorDescription);
			//Based on Finance team input we will not retry for few error codes.
			//strAuthNoRetryErrorCodes holds the error codes for which retry is not required.
			if(!strAuthNoRetryErrorCodes.contains(rtsErrorCode)){			
				bAuthStrikeLimitReached = processAuthStrikes(env,paymentInputStruct, paymentOutputStruct);	
				log.verbose("Boolean bAuthStrikeLimitReached::"+bAuthStrikeLimitReached);
				//If maximum retry is reached then suspend the payment type and raise alert.
				if (bAuthStrikeLimitReached) {
					paymentOutputStruct.retryFlag = "N";
					paymentOutputStruct.authReturnMessage=rtsErrorDescription;
					paymentOutputStruct.authReturnCode=rtsErrorCode;
					paymentOutputStruct.suspendPayment = "Y";
					
					raiseHardDeclineAlert(env, paymentInputStruct, response);
					
		         }else{
		        	 log.verbose("Set Auth Retry parameters");
		        	 retryAuthorization(response, paymentOutputStruct);
		         }
				
			}else{ //Technical alert :: No retry raise Business and / or log error to splunk
				log.verbose("Do not retry and raise alert");
				paymentOutputStruct.retryFlag = "N";
				paymentOutputStruct.authReturnMessage=rtsErrorDescription;
				paymentOutputStruct.authReturnCode=rtsErrorCode;
				paymentOutputStruct.suspendPayment = "Y";
				
				raiseHardDeclineAlert(env, paymentInputStruct, response);
				
			}
		}
		}
		log.verbose("Exiting translateRTSResponse()");
		//END GCD-59
		return paymentOutputStruct;		
	}
	

	/**
	 * Method handles "Charge" response from RTS. All scenarios Success/Decline/Unavailable
	 * @param paymentInputStruct
	 * @param requestRTS
	 * @param responseRTS
	 * @return paymentOutputStruct
	 * @throws Exception
	 */
	private YFSExtnPaymentCollectionOutputStruct translateRTSResponseForSettle(YFSEnvironment env, 
			YFSExtnPaymentCollectionInputStruct paymentInputStruct,Element requestRTS, Element responseRTS,Element eleRTSRequest, Document RTSReqRes) throws Exception {
		log.verbose("Inside translateRTSResponseForSettle()");
		YFSExtnPaymentCollectionOutputStruct paymentOutputStruct = new YFSExtnPaymentCollectionOutputStruct();
		//START GCD:59 Error handling

		if(!YFCCommon.isVoid(responseRTS)){
		String strActioncode = responseRTS.getElementsByTagName(AcademyConstants.RTS_RESP_ACTION_CODE).item(0).getTextContent();
		if (AcademyConstants.STR_RTS_SUCCESS_RESP.equals(strActioncode)) {		
			//START: GCD-95 added to include authCode and other fields set for Authorization 
			
			 	paymentOutputStruct.authorizationId = paymentInputStruct.authorizationId;
			 	
	            paymentOutputStruct.requestID = responseRTS.getElementsByTagName(AcademyConstants.ATTR_RTS_INVOICE).item(0).getTextContent();
	            paymentOutputStruct.tranAmount = paymentInputStruct.requestAmount;
	            paymentOutputStruct.authorizationAmount = paymentInputStruct.requestAmount;
	            paymentOutputStruct.tranType = paymentInputStruct.chargeType;
	            paymentOutputStruct.tranReturnCode = responseRTS.getElementsByTagName(AcademyConstants.ATTR_RTS_CMD).item(0).getTextContent();
	            paymentOutputStruct.authorizationExpirationDate = paymentInputStruct.currentAuthorizationExpirationDate.toString();
	            paymentOutputStruct.authTime = (new SimpleDateFormat("yyyyMMddHHmmss")).format(Calendar.getInstance().getTime());
	            paymentOutputStruct.retryFlag = "N";
	            paymentOutputStruct.suspendPayment = "N";
	            paymentOutputStruct.SvcNo = paymentInputStruct.svcNo;
	            //Start GCD-240 AuthCode during Settlment
	            String authCode = responseRTS.getElementsByTagName(AcademyConstants.ATTR_RTS_AUTH_CODE).item(0).getTextContent();
	            //End GCD-240 AuthCode during Settlment
	            paymentOutputStruct.authCode = authCode;
	            paymentOutputStruct.sCVVAuthCode = authCode;
	            
	            log.verbose("translateRTSResponseForSettle Success::"+paymentOutputStruct);
			//END : GCD-95
		}/*else if(YFCCommon.isVoid(strActioncode)){
			
			//send timeout reversal request
			//invoke RTS service to settle the amount.			
			
		}*/
		else{
			log.verbose("Settlment failed!!");
			
			//Common code will have the parameters related to error handling.
			getGCcommonCodeValues(env);
			
			//logs the RTS request and Response XMLS in DB when ActionCode is not Zero.
			if("Y".equals(strIsDBLoggingEnable) && (!YFSObject.isVoid(strDBServiceName))){
				log.verbose("Settlment failed -> Logging RTS req resp to DB");
				AcademyUtil.invokeService(env, strDBServiceName, RTSReqRes);
			}
			
			String rtsErrorCode = responseRTS.getElementsByTagName(AcademyConstants.RTS_RESP_ERROR_CODE).item(0).getTextContent();
			String rtsErrorDescription = responseRTS.getElementsByTagName(AcademyConstants.RTS_RESP_ERROR_DESC).item(0).getTextContent();
			log.verbose("ErrorCode::"+rtsErrorCode+"\n"+"ErrorDescription::"+rtsErrorDescription);
			//Based on Finance team input we will not retry for few error codes.
			//strSettleNoRetryErrorCodes holds the error codes for which retry is not required.
			if(!strSettleNoRetryErrorCodes.contains(rtsErrorCode)){		
				////we are re using the method written for Authorization as the logic is same.
				bAuthStrikeLimitReached = processAuthStrikes(env, paymentInputStruct, paymentOutputStruct);	
				log.verbose("Boolean bAuthStrikeLimitReached::"+bAuthStrikeLimitReached);
				//If maximum retry is reached then do dummy settlement and raise alert.
				if (bAuthStrikeLimitReached) {
					//GCD-144
					paymentOutputStruct = dummySettlementAndRaiseAlert(env, paymentInputStruct, paymentOutputStruct, responseRTS, rtsErrorCode);
					
		         }else{
		        	 log.verbose("Set Auth Retry parameters");
		        	 //we are re using the method written for Authorization as the logic is same.
		        	 retryAuthorization(responseRTS, paymentOutputStruct);
		         }
				
			}else{ //Technical alert :: No retry raise Business and / or log error to splunk
				//GCD-144
				paymentOutputStruct = dummySettlementAndRaiseAlert(env, paymentInputStruct, paymentOutputStruct, responseRTS, rtsErrorCode);
				
			}
		}
		}
		return paymentOutputStruct;
	}
	
	
	 //START : GCD-144
    /**Method does dummy Settlement on failure scenario and raises a SettlementFailure alert
	 * @param env
	 * @param paymentInputStruct
	 * @param paymentOutputStruct
	 * @param responseRTS
	 * @param rtsErrorCode
	 * @return paymentOutputStruct
	 * @throws Exception
	 * 
	 * Raising the alert, by calling the service PaymentFailureAlert
	 */
	private YFSExtnPaymentCollectionOutputStruct dummySettlementAndRaiseAlert(YFSEnvironment env,YFSExtnPaymentCollectionInputStruct paymentInputStruct,
    		YFSExtnPaymentCollectionOutputStruct paymentOutputStruct,Element responseRTS,String rtsErrorCode) throws Exception{
    	
    	log.debug("Entering dummySettlementAndRaiseAlert ");
    	Element eleAuthCode = (Element) responseRTS.getElementsByTagName(AcademyConstants.RTS_RESP_LOCK_ID).item(0);    	
    	paymentOutputStruct.authCode = (!YFCCommon.isVoid(eleAuthCode)) ? eleAuthCode.getTextContent() : " ";
    	log.debug("dummySettlementAndRaiseAlert : paymentOutputStruct.authCode"+paymentOutputStruct.authCode);
		paymentOutputStruct.authorizationId = paymentInputStruct.authorizationId;
		paymentOutputStruct.requestID = responseRTS.getElementsByTagName(AcademyConstants.ATTR_RTS_INVOICE).item(0).getTextContent();
		paymentOutputStruct.tranAmount = paymentInputStruct.requestAmount;
		paymentOutputStruct.authorizationAmount = paymentInputStruct.requestAmount;
		paymentOutputStruct.tranType = paymentInputStruct.chargeType;
		paymentOutputStruct.tranReturnCode = "Dummy Settlement";	
		
		log.debug("dummySettlementAndRaiseAlert paymentOutputStruct"+paymentOutputStruct);
		
		Document docExceptionInput = XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT);
		docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_PAYMENT_TYPE, paymentInputStruct.paymentType);
		docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_NO, paymentInputStruct.orderNo);
		docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY, paymentInputStruct.orderHeaderKey);
		docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_SVCNO, paymentInputStruct.svcNo);
		docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_TRAN_AMT, String.valueOf(paymentInputStruct.requestAmount));
		docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT, String.valueOf(paymentInputStruct.requestAmount));
		docExceptionInput.getDocumentElement().setAttribute("ReasonCode_GC", responseRTS.getElementsByTagName(AcademyConstants.ATTR_RTS_RECEIPT_DISP).item(0).getTextContent());
		docExceptionInput.getDocumentElement().setAttribute("ErrorNo_GC", responseRTS.getElementsByTagName(AcademyConstants.ATTR_RTS_ISO_RESP).item(0).getTextContent());
		docExceptionInput.getDocumentElement().setAttribute("ErrorDesc", responseRTS.getElementsByTagName(AcademyConstants.ATTR_RTS_RECEIPT_DISP).item(0).getTextContent());
		docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_ERROR_NO, responseRTS.getElementsByTagName(AcademyConstants.ATTR_RTS_ISO_RESP).item(0).getTextContent());
		docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_STATUS_CODE, responseRTS.getElementsByTagName(AcademyConstants.ATTR_RTS_ACTION_CODE).item(0).getTextContent());
		//SOF :: Start WN-2088 Add a SOF Identifier to the Settlement Failure alert for GC
		docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_PAYMENT_REF_4, paymentInputStruct.paymentReference4);
		//SOF :: End WN-2088 Add a SOF Identifier to the Settlement Failure alert for GC
		
		log.debug("dummySettlementAndRaiseAlert :: Input to PaymentFailureAlert --> "+ XMLUtil.getXMLString(docExceptionInput));
		AcademyUtil.invokeService(env, AcademyConstants.SERV_SETTLEMENT_FAILURE_ALERT, docExceptionInput);
		log.debug("dummySettlementAndRaiseAlert :: SettlementFailure alert raised!!! ");
		
		if(strAuthTechErrorCodes.contains(rtsErrorCode)|| strSettleTechErrorCodes.contains(rtsErrorCode)){
			//TO DO:Need to check with Splunk admin team for the key words in the log.
			log.error("Gift Card :: "+paymentInputStruct.chargeType + " failed due to "
					+responseRTS.getElementsByTagName(AcademyConstants.ATTR_RTS_RECEIPT_DISP).item(0).getTextContent()+ " for Order "+paymentInputStruct.orderNo);
		}		
		return paymentOutputStruct;
    }
	//END : GCD-144
	
	//START : GCD-161
	/*** Method raises HARD_DECLINED alert by calling createException API.
	 * @param env
	 * @param paymentInputStruct
	 * @param response
	 * @throws Exception
	 * 
	 */
	private void raiseHardDeclineAlert(YFSEnvironment env,YFSExtnPaymentCollectionInputStruct paymentInputStruct, Element response) throws Exception {
		
		log.verbose("Entering raiseHardDeclineAlert");
		Document docExceptionInput = null;
		String strOrderNo = paymentInputStruct.orderNo;
		String strErrorCode = response.getElementsByTagName(AcademyConstants.ATTR_RTS_ISO_RESP).item(0).getTextContent();
		String strErrorMessage = response.getElementsByTagName(AcademyConstants.ATTR_RTS_RECEIPT_DISP).item(0).getTextContent();
		
		try {
			//Forming input to createException
			docExceptionInput = XMLUtil.createDocument(AcademyConstants.ELE_INBOX);
			docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_ACTIVE_FLAG,AcademyConstants.STR_YES);			
			docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_EXCPTN_TYPE, AcademyConstants.PAYMENT_HARD_DECLINED_GC);
			docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_NO,strOrderNo);
			docExceptionInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,paymentInputStruct.orderHeaderKey);			
			docExceptionInput.getDocumentElement().setAttribute("DetailDescription", "Authorization failed for Order# : " + strOrderNo
					+ " ::GiftCard Value : " + paymentInputStruct.requestAmount + " :: Reason : " + strErrorMessage);			
			log.verbose("Input to createException API :: "+XMLUtil.getXMLString(docExceptionInput));
			
			AcademyUtil.invokeAPI(env, AcademyConstants.API_CREATE_EXCEPTION,docExceptionInput);
			log.debug("Gift Card :: Hard Decline alert raised!!!");
			
		} catch (Exception e) {
			throw new YFSException(e.getMessage());
		}
		
		//Logging technical alert
		if(strAuthTechErrorCodes.contains(strErrorCode)){
			log.error("Gift Card :: AUTHORIZATION failed due to " + strErrorMessage + " for Order " + strOrderNo);
		}
		
		log.error("Exiting raiseHardDeclineAlert");
	}
	//END : GCD-161	
	
//	START : GCD-132
	/**Method makes a 'Sale Reversal' transaction call to RTS.
	 * @param env
	 * @param inDoc
	 * @throws Exception
	 * 
	 */
	private boolean sendTimeOutReversalRequest(YFSEnvironment env, Document inDoc) throws Exception{
		
		log.verbose("Entering sendTimeOutReversalRequest()");
		String strActionCode = null;
		try{
		    Document rtsTimeOutRevInDoc = AcademyRTSUtil.getRTSHTTPRequestDoc(env, inDoc, AcademyConstants.RTS_REQ_TRAN_TYPE_SALE);
		    rtsTimeOutRevInDoc.getElementsByTagName(AcademyConstants.ATTR_RTS_OPTIONS).item(0).setTextContent("multilock *reversal");
		    log.verbose("Sale Reversal transaction:: Input :"+XMLUtil.getXMLString(rtsTimeOutRevInDoc));    		
		    
		    Document rtsResponseDoc = AcademyUtil.invokeService(env, "AcademyInvokeRTSReprocessGCActivation", rtsTimeOutRevInDoc);
		    log.verbose("Sale Reversal transaction:: Output :"+XMLUtil.getXMLString(rtsResponseDoc));
		    
		    if(!YFCCommon.isVoid(rtsResponseDoc.getDocumentElement()))
		    	strActionCode = rtsResponseDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_ACTION_CODE).item(0).getTextContent();
		    log.verbose("isVoid(strActionCode) : "+YFCCommon.isVoid(strActionCode));
	      
		}catch(YFSException e){
			e.printStackTrace();			
		}
		log.verbose("Exiting sendTimeOutReversalRequest()");
	    
		//If we recieve ActionCode(RTS Response), then 'Sale Reversal transaction' was done
	    if(!YFCCommon.isVoid(strActionCode))
	    	return true;
	    else
	    	return false;	
	}
	//END : GCD-132

	
	/**This method prepares a new environment object to invoke for lower java versions
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	private YFSEnvironment createEnvironment(String progID) throws Exception {
		log.verbose("Inside createEnvironment");
		String connProtocol = YFSSystem.getProperty(AcademyConstants.PROP_CARDNL_RETRY_ENVIRONMENT_PROTOCOL);
		//String envURL = "http://host_name:port/smcfs/interop/InteropHttpServlet";
		String envURL = YFSSystem.getProperty(AcademyConstants.PROP_CARDNL_RETRY_ENVIRONMENT);
		String strUserId = YFSSystem.getProperty(AcademyConstants.PROP_CARDNL_RETRY_ENABLED_USING_APP_USERID);
		String strPassword = YFSSystem.getProperty(AcademyConstants.PROP_CARDNL_RETRY_ENABLED_USING_APP_PASSWORD);
		
		HashMap<String, String> envProps = new HashMap<String, String>();

		envProps.put("yif.httpapi.url", envURL);
		api = YIFClientFactory.getInstance().getApi(connProtocol, envProps);

		Document doc = XMLUtil.createDocument("YFSEnvironment");
		Element elem = doc.getDocumentElement();
		elem.setAttribute(AcademyConstants.ATTR_USR_ID, strUserId);
		elem.setAttribute(AcademyConstants.STR_PASSWORD, strPassword);
		elem.setAttribute(AcademyConstants.ATTR_PROG_ID, progID);
		YFSEnvironment env = api.createEnvironment(doc);

		log.verbose("Exit createEnvironment");
		return env;

	}

	/** This method is added as part of OMNI-104596. This method prepares the exception and logs the same to DB
	 * @param env
	 * @param docPayZReq , docPayZResp
	 * @throws ParserConfigurationException
	 * @throws DOMException
	 * @throws Exception
	 */
	private Document prepareExceptionResponse(Document docRTSException, String strOrderNo, String strChargeType, 
			String strErrorCode, String strExceptionType) {
		log.verbose("Start AcademyGiftCardCollectionUEImpl.prepareExceptionResponse() method ::");

		Element eleRTSException = docRTSException.getDocumentElement();
		
		Document docRTSResponse;
		try {
			docRTSResponse = XMLUtil.createDocument("Exception");
			Element eleRTSResp = docRTSResponse.getDocumentElement();
			eleRTSResp.setAttribute(AcademyConstants.ATTR_ORDER_NO, strOrderNo);
			eleRTSResp.setAttribute(AcademyConstants.ATTR_CHARGE_TYPE, strChargeType);
			eleRTSResp.setAttribute(AcademyConstants.ATTR_EXCEPTION_TYPE, strExceptionType);
			eleRTSResp.setAttribute(AcademyConstants.ATTR_ERROR_CODE, strErrorCode);
			
			Element eleRTSExcep = docRTSException.createElement("Exception");
			eleRTSException.appendChild(eleRTSExcep);
			XMLUtil.importElement(eleRTSExcep, eleRTSResp);

		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		log.verbose("End AcademyGiftCardCollectionUEImpl.prepareExceptionResponse() method ::");
		return docRTSException;
	}
	//End PayZ : MPMT-111,112
	
	/**This method does the follwoing:
	 * If charge type is AUTHORIZATION: Updates the collectionDate to sysdate+nextTriggerInterval for retry.
	 * If charge type is CHARGE: Does Sales reversal call. If successful, does the settlement call. Else, 
	 *                           updates the collectionDate to sysdate+nextTriggerInterval for retry.
	 * 
	 * @param oEnv
	 * @param paymentInputStruct
	 * @param paymentOutputStruct
	 * @param chargeType
	 */
	private void handleExceptions(YFSEnvironment oEnv, YFSExtnPaymentCollectionInputStruct paymentInputStruct, 
			YFSExtnPaymentCollectionOutputStruct paymentOutputStruct, String chargeType) {
		
		log.beginTimer("handleExceptions");
		if(AcademyConstants.STR_CHRG_TYPE_AUTH.equalsIgnoreCase(chargeType)){
            log.debug("Authorization : SSLHandshakeException -> Retry");
            //GCD-145 : Technical alert for Authorization timeout
            log.error("Gift Card :: "+chargeType+" SSLHandshakeException for Order "+paymentInputStruct.orderNo); 
            
            paymentOutputStruct.retryFlag = "Y"; 
            try {				
				paymentOutputStruct.collectionDate = getCollectionDate();
			} catch (Exception e1) {
				log.verbose("SSLHandshakeException: Exception in collectionSVC( )!!!"+e1);
				e1.printStackTrace();
			}                                              
        }else if(AcademyConstants.STR_CHRG_TYPE_CHARGE.equalsIgnoreCase(chargeType)){
            log.debug("Charge : SSLHandshakeException -> Reverse - Charge/Retry");
            //GCD-145 : Technical alert for Settlement timeout
            log.error("Gift Card :: "+chargeType+" call timed-out for Order "+paymentInputStruct.orderNo); 
            
            try{
                Document paymentInputDoc = AcademyPaymentProcessingUtil.convertPaymentInputStructToXML(paymentInputStruct);
                //Start : OMNI-104596 : Handling Exception cases for Gift Cards
                //When the Setllement call timesout, make a 'Sale reversal' call.
                //Boolean bTimeOutReversalSuccess = sendTimeOutReversalRequest(oEnv, paymentInputDoc);
                //As the timeout is already being considered as part of the API call. Skip extra timeout reversal
                Boolean bTimeOutReversalSuccess = false;                
            	//End : OMNI-104596 : Handling Exception cases for Gift Cards
                
                if(bTimeOutReversalSuccess){
                	//If 'Sale Reversal' call was successfull , it means RTS is up and we can make the settlement call
                	collectionStoredValueCard(oEnv, paymentInputStruct);                            	
                }else{
                	//If 'Sale Reversal' call failed. Then retry settlement call after sometime by setting collectionDate
                	paymentOutputStruct.retryFlag = "Y";
					paymentOutputStruct.collectionDate = getCollectionDate();
                }
            } catch(Exception e2){
            	log.verbose("Charge SSLHandshakeException: Exception in collectionSVC( )!!!"+e2);
                e2.printStackTrace();
            }
        }
		
		log.endTimer("handleExceptions");
	}
	
}
