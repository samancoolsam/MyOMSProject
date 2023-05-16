package com.academy.ecommerce.sterling.util.webservice;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * This class acts as a proxy for Cardinal and Payeezy service based on PayRef3 attribute 
 * @author C0008437
 * created on 2017-02-27
 */
public class AcademyCreditCardProxy {
	
	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyCreditCardProxy.class);
	
	//Stores the PAYZ_ENABLED
	private Properties props;
	private YIFApi api = null;
	
	/**
	 * Set the PAYZ_ENABLED argument value
	 */
	public void setProperties(Properties props){
		this.props = props;
	}
	
	/**
	 * This is the proxy method for calling PayZ or Cardinal Service based on PAYZ_ENABLED argument and PayRef3 attribute  
	 */
	public Document invokeCreditCardProxy(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("Begin of AcademyCreditCardProxy.invokeCreditCardProxy() input doc ::"+XMLUtil.getXMLString(inDoc));
		
		String strPayZEnabled = props.getProperty(AcademyConstants.STR_PAYZ_ENABLED);
		log.verbose("PayZ Enabled : "+strPayZEnabled);
		
		//This <Payment> response document is returned to credit card UE
		Document docCCProxyResp = null;
		
		String strPayRef3 = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_PAYMENT_REF_3);
		//start ACC-II Tokenization
		String strPayType = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_PAYMENT_TYPE);
		//End ACC-II Tokenization
		//Code changes to bypass webservices based on java version
		YFSEnvironment newEnv = null;
		//String strRetryEnabled = YFSSystem.getProperty(AcademyConstants.PROP_CARDNL_RETRY_ENABLED);
		String strRetryUsingAppEnabled = YFSSystem.getProperty(AcademyConstants.PROP_CARDNL_RETRY_ENABLED_USING_APP);
		//log.verbose("strRetryEnabled :: "+strRetryEnabled);
		log.verbose("strRetryUsingAppEnabled :: "+strRetryUsingAppEnabled);
		//KER-15323:: Payment Migration: Cardinal Credit Card Settlement Exception
		try {
		//if(!YFCObject.isNull(strRetryEnabled) && strRetryEnabled.equals(AcademyConstants.STR_YES)) {
			if(!YFCObject.isNull(strRetryUsingAppEnabled)&& strRetryUsingAppEnabled.equals(AcademyConstants.STR_YES)) {
				if ((YFSObject.isVoid(strPayRef3) || AcademyConstants.STR_PAY_TYPE_PAYEEZY.equalsIgnoreCase(strPayRef3))) {
					
					newEnv = createEnvironment(env.getProgId());
					log.verbose(" Cadrinal/More option orders call being done from java 1.6 using new environment");
				}
			}
			/*else {
				String strDelayInterval = YFSSystem.getProperty("academy.payz.cardinal.delay.seconds");
				String strJavaVersion = System.getProperty("java.version").trim();
				
				log.verbose(" CC Code changes to bypass webservices based on java version ");
				if(!YFSObject.isVoid(strJavaVersion) &&  strJavaVersion.startsWith("1.8")
						&& (!YFSObject.isVoid(strPayRef3) && AcademyConstants.STR_FIRST_DATA.equalsIgnoreCase(strPayRef3))) {
					log.verbose(" FirstData call being done from java 1.8 ");
				}
				else if (!YFSObject.isVoid(strJavaVersion) &&  strJavaVersion.startsWith("1.6") 
						&& (YFSObject.isVoid(strPayRef3) || AcademyConstants.STR_PAY_TYPE_PAYEEZY.equalsIgnoreCase(strPayRef3))) {
					log.verbose(" Cadrinal/More option orders call being done from java 1.6 ");
				}
				else {
					log.verbose(" Skip calls from this JVM. ");
					docCCProxyResp = XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT);
					Element elePaymentOut = docCCProxyResp.getDocumentElement();
					elePaymentOut.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_YES);	

					Calendar cal = Calendar.getInstance();
					SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN_1);		
					cal.add(Calendar.SECOND, Integer.parseInt(strDelayInterval));
					String strCollectionDate = sdf.format(cal.getTime());
					elePaymentOut.setAttribute(AcademyConstants.ATTR_COLLECTION_DATE, strCollectionDate);
					elePaymentOut.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
					return docCCProxyResp;
				}
			} }*/			
		
		//Code changes to bypass webservices based on java version

		//KER-10384:: Payment Migration: Added condition to check of FirstData for Payment Migration
			if(!YFSObject.isVoid(strPayRef3) && AcademyConstants.STR_FIRST_DATA.equalsIgnoreCase(strPayRef3)) {
				log.verbose("calling AcademyInvokePAYZHTTPCCService service with input as : "+XMLUtil.getXMLString(inDoc));
				docCCProxyResp = AcademyUtil.invokeService(env, AcademyConstants.SERV_ACAD_INVOKE_PAYZ_REST_SERVICE, inDoc);
			}
			//Added for PLCC Payment Project.
			//start ACC-II Tokenization
			else if(!YFSObject.isVoid(strPayType) && AcademyConstants.STR_PLCC_PAYMENT.equalsIgnoreCase(strPayType)) {
				log.verbose("calling AcademyInvokeRTSWebservice service with input as : "+XMLUtil.getXMLString(inDoc));
				docCCProxyResp = AcademyUtil.invokeService(env, "AcademyInvokeRTSWebservice", inDoc);
			}
			//End ACC-II Tokenization
			else if( strPayZEnabled != null && AcademyConstants.STR_YES.equalsIgnoreCase(strPayZEnabled) && 
				!YFSObject.isVoid(strPayRef3) && AcademyConstants.STR_PAY_TYPE_PAYEEZY.equalsIgnoreCase(strPayRef3)) {
				
				log.verbose("calling AcademyInvokePAYZHTTPCCService service with input as : "+XMLUtil.getXMLString(inDoc));
				//Code changes to bypass webservices based on java version
				if(!YFCObject.isNull(newEnv)) {
	        		log.verbose("Invoking the service with the new environment object");
	        		docCCProxyResp = api.executeFlow(newEnv, AcademyConstants.SERV_ACAD_INVOKE_PAYZ_PAYMENT_SYSTEM, inDoc);
	        	}
	        	else {
	        		docCCProxyResp = AcademyUtil.invokeService(env, AcademyConstants.SERV_ACAD_INVOKE_PAYZ_PAYMENT_SYSTEM, inDoc);
	        	}
				//KER-15323:: Payment Migration: Cardinal Credit Card Settlement Exception
				raisePaymentAlert(env, docCCProxyResp);
			} else {
					
				/*
				 * Start : OMNI-647 - Discontinue Cardinal
				 * Commenting below lines of code as ASO no more using Cardinal  
				 */
				 
				/* 
				log.verbose("calling AcademyInvokeCardinalPaymentSystemService service with input as : "+XMLUtil.getXMLString(inDoc));
				//Code changes to bypass webservices based on java version
				if(!YFCObject.isNull(newEnv)) {
	        		log.verbose("Invoking the service with the new environment object");
	        		docCCProxyResp = api.executeFlow(newEnv, AcademyConstants.SERV_ACAD_INVOKE_CARDINAL_PAYMENT_SYSTEM, inDoc);
	        	}
	        	else {
	        		docCCProxyResp = AcademyUtil.invokeService(env, AcademyConstants.SERV_ACAD_INVOKE_CARDINAL_PAYMENT_SYSTEM, inDoc);
	        	}
				//KER-15323:: Payment Migration: Cardinal Credit Card Settlement Exception
				*/
				
				// Below lines of code has been added to generate dummy response
				docCCProxyResp = createDummyCentinelResponseForSettle(inDoc);
				
				/*
				 * End : OMNI-647 - Discontinue Cardinal 
				 */
				 
				raisePaymentAlert(env, docCCProxyResp); 
			}
		}
		catch (Exception e){
			log.debug("Exception while making Credit Card Web service calls");
			e.printStackTrace();
			docCCProxyResp = XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT);
			Element eleResponse = docCCProxyResp.getDocumentElement();
			eleResponse.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE, AcademyConstants.STR_SERVICE_UNAVAILABLE);
			eleResponse.setAttribute(AcademyConstants.ATTR_RETRY_FLAG, AcademyConstants.STR_YES);
			eleResponse.setAttribute(AcademyConstants.ATTR_AUTH_RETURN_CODE, "Credit Card Service Unavailable");
		}
				
		log.verbose("End of AcademyCreditCardProxy.invokeCreditCardProxy() output doc ::"+XMLUtil.getXMLString(docCCProxyResp));
		
		return docCCProxyResp;
	}

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
	
	
	/**This method is added as part of KER-15323 - Cardinal Settlement Exception issue
	 * This method invokes a PaymentAlert to raise an alert in case of failure.
	 * @param inDoc
	 * @return
	 * @throws Exception
	 */
	private void raisePaymentAlert(YFSEnvironment env, Document docInput) throws Exception {
		log.verbose("Inside raisePaymentAlert");
		
		String strRaisePaymentAlert =  docInput.getDocumentElement().getAttribute("RaisePaymentAlert");
		if(!YFCObject.isNull(strRaisePaymentAlert) && AcademyConstants.STR_YES.equals(strRaisePaymentAlert)) {
			log.verbose(" Invoking a PayemntFailureAlert service to create an exception");
			AcademyUtil.invokeService(env, AcademyConstants.SERV_SETTLEMENT_FAILURE_ALERT, docInput);
		}
		log.verbose("Exit raisePaymentAlert");
	}
	
	//Start : OMNI-647 - Discontinue Cardinal
	//Below method generates dummy response
	private Document createDummyCentinelResponseForSettle (Document inDoc) {
		
		Document docDummySettResp = null;
		
		try {
		
			Element eleInDoc = inDoc.getDocumentElement();
		    docDummySettResp = XMLUtil.createDocument(AcademyConstants.ELE_PAYMENT);
			Element eleSetResp = docDummySettResp.getDocumentElement();
			
			eleSetResp.setAttribute(AcademyConstants.ATTR_AUTH_AMOUNT,
					eleInDoc.getAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT));
			eleSetResp.setAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT,
					eleInDoc.getAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT));
			eleSetResp.setAttribute(AcademyConstants.ATTR_TRAN_AMT,
					eleInDoc.getAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT));
			eleSetResp.setAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY,
					eleInDoc.getAttribute(AcademyConstants.ATTR_ORDER_HEADER_KEY));
			eleSetResp.setAttribute(AcademyConstants.ATTR_ORDER_ID,
					eleInDoc.getAttribute(AcademyConstants.CREDIT_CARD_NO));
			eleSetResp.setAttribute(AcademyConstants.ATTR_ORDER_NO,
					eleInDoc.getAttribute(AcademyConstants.ATTR_ORDER_NO));
			eleSetResp.setAttribute(AcademyConstants.ATTR_PAYMENT_TYPE,
					eleInDoc.getAttribute(AcademyConstants.ATTR_PAYMENT_TYPE));
			eleSetResp.setAttribute(AcademyConstants.ATTR_RESPONSE_CODE,
					AcademyConstants.STR_APPROVED);
			eleSetResp.setAttribute(AcademyConstants.ATTR_STATUS_CODE,
					AcademyConstants.STR_CARDINAL_STATUS_Y);			
			eleSetResp.setAttribute(AcademyConstants.ATTR_TRAN_RETURN_CODE,
					AcademyConstants.ATTR_HUNDRED);
			eleSetResp.setAttribute(AcademyConstants.ATTR_TRAN_TYPE,
					eleInDoc.getAttribute(AcademyConstants.ATTR_CHARGE_TYPE));
			eleSetResp.setAttribute(AcademyConstants.ATTR_TRANSID,
					AcademyConstants.STR_DUMMY_SETTLEMENT);
			eleSetResp.setAttribute(AcademyConstants.ATTR_RAISE_PAYMENT_ALERT,
					AcademyConstants.STR_YES);
			eleSetResp.setAttribute(AcademyConstants.ATTR_PAYMENT_REF_4,
					AcademyConstants.STR_CARDINAL);
			eleSetResp.setAttribute(AcademyConstants.ATTR_REASON_CODE,
					AcademyConstants.STR_CARDINAL);
			eleSetResp.setAttribute(AcademyConstants.ATTR_REASON_DESC,
					eleInDoc.getAttribute(AcademyConstants.ATTR_PAYMENT_TYPE)+" :: "
						+eleInDoc.getAttribute(AcademyConstants.ATTR_CHARGE_TYPE));
			eleSetResp.setAttribute(AcademyConstants.ATTR_REQUEST_ID,
					AcademyConstants.STR_DUMMY_SETTLEMENT);
			eleSetResp.setAttribute(AcademyConstants.ATTR_AUTH_CODE,
					AcademyConstants.STR_DUMMY_SETTLEMENT);
			eleSetResp.setAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID,
					eleInDoc.getAttribute(AcademyConstants.ATTR_AUTHORIZATION_ID));
						
		}catch(Exception e) {
			log.error("Exception:: "+e);
		}
		
		return docDummySettResp;
	}
	//End : OMNI-647 - Discontinue Cardinal

}//End of AcademyCreditCardProxy class