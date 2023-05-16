package com.academy.ecommerce.sterling.util.webservice;
 
import java.text.DecimalFormat;
import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.academy.ecommerce.sterling.util.AcademyRTSUtil;
import com.academy.ecommerce.sterling.util.stub.AcademyPaymentStub;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
//START : GCD-97
//HTTPS connection related imports 
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
//END : GCD-97
/**
 * Making HTTP call to RTS system
 * @author C0007277
 *         C0008432
 * 
 */

public class AcademyInvokeRTSHTTPGCService {
	
	private Properties props;	
	String chargeType = null;
	String ramount = null;
	String authrId = null;
	String transactionType = null;
	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyInvokeRTSHTTPGCService.class);
	//START : GCD-97
	public void setProperties(Properties props)
	{
		this.props = props;
	}
	//END : GCD-97
	
	//Start OMNI-99826 - Change for payment stub
	private static String strInvokePaymentStub = YFSSystem.getProperty(AcademyConstants.INVOKE_PAYMENT_STUB); //OMNI-99459
	//End OMNI-99826
	
	/**
	 * Method invokes AcademyRTSUtil to form the input XML based on payment tender, merges reqResp doc.
	 * @param inDoc
	 * @return docRTSRequestResponse - RTS ReqResp Merged Document
	 * @throws Exception
	 */
	public Document invokeRTSHTTPService(YFSEnvironment env, Document inDoc)throws Exception
	{
		log.verbose("Input to invokeRTSHTTPService():: "+XMLUtil.getXMLString(inDoc));	
		Document rtsRequestDoc = null;
		Document rtsResponseDoc = null;
		Document docRTSRequestResponse = null;
		String strRequestAmount = "0";		
		if(!YFCObject.isVoid(inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_PAYMENT_TYPE))){
			
			if(AcademyConstants.STR_GIFT_CARD.equals(inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_PAYMENT_TYPE))){
				chargeType = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_CHARGE_TYPE);				
				log.verbose("chargeType : "+chargeType);
				
				if(!YFCObject.isVoid(chargeType)){
					if(AcademyConstants.STR_CHRG_TYPE_AUTH.equals(chargeType)){
						transactionType = AcademyConstants.STR_RTS_BALANCE_INQUIRY_TRAN_TYPE;
						strRequestAmount = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT);
						log.verbose("strRequestAmount : "+strRequestAmount);
						//START:GCD-115
						if(Float.parseFloat(strRequestAmount) < 0){
							//For Auth Reversal : Make a 'SALE' call with amount as $0
							//This will charge $0 on the card and release rest of the amount -> As good as releasing the auth
							inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_REQUEST_AMOUNT, "0.00");
							transactionType = AcademyConstants.RTS_REQ_TRAN_TYPE_SALE;
							log.verbose("invokeRTSHTTPService()::setAttribute.RequestAmount=0.00 "+XMLUtil.getXMLString(inDoc));
						}
						
					}else if(AcademyConstants.STR_CHRG_TYPE_CHARGE.equals(chargeType))
						transactionType = AcademyConstants.RTS_REQ_TRAN_TYPE_SALE;
						//END : GCD-115				
				}				
				log.verbose("transactionType : "+transactionType);				
				rtsRequestDoc = AcademyRTSUtil.getRTSHTTPRequestDoc(env,inDoc,transactionType);
			}
				
			}else{
			//Activation - Default chargeType, before calling this class
			chargeType = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_CHARGE_TYPE);
			log.verbose("transactionType : "+chargeType);
			rtsRequestDoc = AcademyRTSUtil.getRTSHTTPRequestDoc(env,inDoc,chargeType);
			//Start : GCD-175
			String strOrderNo = rtsRequestDoc.getElementsByTagName(AcademyConstants.ATTR_RTS_INVOICE).item(0).getTextContent();
			//Meaning Order is APPEASEMENT or RETURN order
			if(strOrderNo.startsWith("Y")){
				log.verbose("GC Activation : APPEASEMENT/RETURN order - Truncate Y from Order# ");
				rtsRequestDoc.getElementsByTagName(AcademyConstants.ATTR_RTS_INVOICE).item(0).setTextContent(strOrderNo.replaceFirst("^Y", ""));				
			}
			//End : GCD-175
		}
		
      
		if (!YFCObject.isVoid(rtsRequestDoc)){	
			log.verbose("****Request to RTS :: "+XMLUtil.getXMLString(rtsRequestDoc));
			//OMNI-99826 PaymentStub : Start
			if(!YFCObject.isVoid(strInvokePaymentStub) && AcademyConstants.STR_YES.equalsIgnoreCase(strInvokePaymentStub)){
				//Invoke payment stub
				log.verbose("strInvokePaymentStub: "+strInvokePaymentStub);
				rtsResponseDoc = AcademyPaymentStub.invokeRTSPaymentStubForGC(env,rtsRequestDoc,inDoc);
			}
			else{				
			//START : GCD-97		
			//Read from argument tab.
        	System.setProperty(AcademyConstants.STR_KEY_SSL_TRUST_STORE_GC, props.getProperty(AcademyConstants.STR_KEY_STORE_GC));
			System.setProperty(AcademyConstants.STR_KEY_SSL_TRUST_STORE_PASSWORD_GC, props.getProperty(AcademyConstants.STR_KEY_PASSWORD_GC));
			rtsResponseDoc = sendHTTP(env,rtsRequestDoc);
			}
			//OMNI-99826 PaymentStub : END
			//END : GCD-97
			//Start : OMNI-106156 : Handling Timeout of Timeout Reversal
			//log.verbose("****Response from RTS :: " +XMLUtil.getXMLString(rtsResponseDoc));
			//Only Changing in RTSresp, not in RTSreq
			//if 0.00 is sent to RTS, assuming RTS will respond with 0.00
			//String strRTSrespAmount = rtsResponseDoc.getElementsByTagName("IxAmount").item(0).getTextContent();
			if(!YFCCommon.isVoid(rtsResponseDoc)){
			if(rtsResponseDoc.getElementsByTagName(AcademyConstants.ATTR_RTS_AMOUNT).item(0).getTextContent().equals("0.0")){
				Double reqAmt = new Double(strRequestAmount); 
				DecimalFormat df = new DecimalFormat("##.00");				
				rtsResponseDoc.getElementsByTagName(AcademyConstants.ATTR_RTS_AMOUNT).item(0).setTextContent(df.format(reqAmt.doubleValue()).replace(".", ""));
				log.verbose("Auth Release : Negative Amt :: ****Response from RTS " +XMLUtil.getXMLString(rtsResponseDoc));
			}
			}
			else {
				rtsRequestDoc = XMLUtil.createDocument(AcademyConstants.ELE_ERROR);
				rtsRequestDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_ERROR_DESC, AcademyConstants.SOCKET_TIME_OUT_EXCEPTION);
			}
			//End : OMNI-106156 : Handling Timeout of Timeout Reversal
		}
		
		docRTSRequestResponse = AcademyRTSUtil.getRTSHTTPReqRespDoc(rtsRequestDoc, rtsResponseDoc);
		log.verbose("RTS ReqResp Merged Doc :: " +XMLUtil.getXMLString(docRTSRequestResponse));		
		return docRTSRequestResponse;
	}
	//START :  GCD-97	
	/**
	 * Method makes a HTTPS call to RTS system
	 * @param property
	 * @return 
	 * @throws Exception 
	 */
		// changed signature as the argument tab values were not read from this method.
	private Document sendHTTP(YFSEnvironment env,Document requestXMLDoc) throws Exception {
		log.verbose("Inside sendHTTP()");
		URL url;
		//String rtsURL = YFSSystem.getProperty(AcademyConstants.STR_RTS_END_POINT);
		//log.verbose("RTS URL::"+rtsURL);
		HttpsURLConnection connection = null;
		StringBuffer strRTSResponse = new StringBuffer();
		BufferedReader rd;
		
		try{			
			String rtsURL = YFSSystem.getProperty(AcademyConstants.STR_RTS_END_POINT).trim();
			log.verbose("RTS URL::"+rtsURL);
			
			log.verbose("Java version::"+System.getProperty("java.version"));
			log.verbose("jdk.http.auth.tunneling.disabledSchemes:: before :"+System.getProperty("jdk.http.auth.tunneling.disabledSchemes"));
			System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
			log.verbose("jdk.http.auth.tunneling.disabledSchemes::After:"+System.getProperty("jdk.http.auth.tunneling.disabledSchemes"));
			
			url = new URL(rtsURL);
			log.verbose(":::Connection is about to be established:::" + url);	            
			connection = (HttpsURLConnection) url.openConnection();	    
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type", "text/xml");
			//Changes for Payment Migration
			//connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Cache-Control", "no-cache");
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			//connection.setUseCaches(false);
			//Changes for Payment Migration
			
			connection.setConnectTimeout(Integer.parseInt(props.getProperty(AcademyConstants.STR_RTS_GIFT_CARD_TIME_OUT)));
			connection.setReadTimeout(Integer.parseInt(props.getProperty(AcademyConstants.STR_RTS_GC_READ_TIME_OUT)));
			
			//To set protocal
			//Changes for Payment Migration
			String strSSLContext = props.getProperty("SSLContext");
			log.verbose("::strSSLContext ::" + strSSLContext);
			if(!YFCObject.isNull(strSSLContext)) {
				//SSLContext sc = SSLContext.getInstance("TLSv1"); //$NON-NLS-1$
				SSLContext sc = SSLContext.getInstance(strSSLContext); //$NON-NLS-1$
				sc.init(null, null, new java.security.SecureRandom());
				connection.setSSLSocketFactory(sc.getSocketFactory());
			}
			//Changes for Payment Migration

			log.verbose("::Connection object created ::" + ((HttpsURLConnection) connection).getSSLSocketFactory());
		    
		     // Send request
		    DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
		    log.verbose("created outputStreamObject!!"+ wr);
		    log.verbose("sending request ::"+ XMLUtil.getXMLString(requestXMLDoc));
		    wr.writeBytes(XMLUtil.getXMLString(requestXMLDoc));
		    wr.flush();
		    wr.close();
	
		    log.verbose(":::Message posted in RTS Stream:::");
		    
	        //successful connection established with server
	        InputStream is = connection.getInputStream();
	        rd = new BufferedReader(new InputStreamReader(is));
	        String inputLine;
	        while ((inputLine = rd.readLine()) != null){ 
	            strRTSResponse.append(inputLine);                 
	        }
	        //write data to tem file  
	        rd.close();
	       log.verbose("Xml Response from RTS:::" + strRTSResponse);

		}catch(SocketTimeoutException e){
	        log.verbose("RTS call :: SocketTimeoutException"+e.toString());
	        e.printStackTrace();
	        
	        //START : GCD-134
	        //If Activation times out, then reverse the activation and then activate the card.
	        //Element IxOptions is present only for Activation call and not for 'Activation Reversal' call.
	        if(AcademyConstants.STR_RTS_ACTIVATION_TRAN_TYPE.equals(requestXMLDoc.getElementsByTagName(AcademyConstants.ATTR_RTS_TRAN_TYPE).item(0).getTextContent()) &&
	        		YFCCommon.isVoid((Element) requestXMLDoc.getElementsByTagName(AcademyConstants.ATTR_RTS_OPTIONS).item(0))){
	        	log.error("Gift Card :: Activation call timed-out for Order "+requestXMLDoc.getElementsByTagName(AcademyConstants.ATTR_RTS_INVOICE).item(0).getTextContent());
	        	Document rtsRespDoc = sendActivationTimeOut(env,requestXMLDoc);
	        	log.verbose("ActivatePhysicalCard : sendActivationTimeOut response "+ XMLUtil.getXMLString(rtsRespDoc));
	        	return rtsRespDoc;
	        }
			//Start : OMNI-106156 : Handling Timeout of Timeout Reversal
			else if(AcademyConstants.STR_RTS_CHARGE_TRAN_TYPE.equals(requestXMLDoc.getElementsByTagName(AcademyConstants.ATTR_RTS_TRAN_TYPE).item(0).getTextContent()) &&
	        		(!YFCCommon.isVoid((Element) requestXMLDoc.getElementsByTagName(AcademyConstants.ATTR_RTS_OPTIONS).item(0))
	        				&& !requestXMLDoc.getElementsByTagName(AcademyConstants.ATTR_RTS_OPTIONS).item(0).getTextContent().contains("reversal"))){
            //End : OMNI-106156 : Handling Timeout of Timeout Reversal
 				log.debug("Charge : TimeOutReversal -> Reverse - Charge/Retry");
                //GCD-145 : Technical alert for Settlement timeout
                log.error("Gift Card :: "+chargeType+" call timed-out for Order "); 
                
                try {

					// When the Setllement call timesout, make a 'Sale reversal'
					// call.
					Boolean bTimeOutReversalSuccess = sendTimeOutReversalRequest(
							env, requestXMLDoc);

					if (bTimeOutReversalSuccess) {
						log.error("Gift Card :: For Order# "
								+ requestXMLDoc.getElementsByTagName(
										AcademyConstants.ATTR_RTS_INVOICE)
										.item(0).getTextContent()
								+ " call timed-out Reversal Successful ");
					} else {
						log.error("Gift Card ::For Order# "
								+ requestXMLDoc.getElementsByTagName(
										AcademyConstants.ATTR_RTS_INVOICE)
										.item(0).getTextContent()
								+ " call timed-out Reversal was not Successful");
					}
				} catch(Exception e2){
                    //Start : OMNI-104596 : Handling Exception cases for Gift Cards
                	log.error("Gift Card Error :: For Order# "
							+ requestXMLDoc.getElementsByTagName(
									AcademyConstants.ATTR_RTS_INVOICE)
									.item(0).getTextContent()
							+ " Charge SSLHandshakeException: Exception in collectionSVC( )!!!"+e2);
                    e2.printStackTrace();
                    YFSException yfsExcep = new YFSException(e.getMessage());
        			yfsExcep.setErrorCode("EXTN_RTS_200");
        			yfsExcep.setErrorDescription("Error While trying to Invoke RTS Timeout Reversal ");
        			throw yfsExcep;
        			//End : OMNI-104596 : Handling Exception cases for Gift Cards
                }
	        }
	        else{
		    	//Start : OMNI-104596 : Handling Exception cases for Gift Cards
	        	log.error("Gift Card Error :: For Order# "
						+ requestXMLDoc.getElementsByTagName(
								AcademyConstants.ATTR_RTS_INVOICE)
								.item(0).getTextContent()
						+ " For RTS calls(Authorization/Settlement/Settlement Reversal/Activation Reversal),throw out SocketTimeoutException");
	        	e.printStackTrace();
	        	YFSException yfsExcep = new YFSException(e.getMessage());
    			yfsExcep.setErrorCode("EXTN_RTS_100");
    			yfsExcep.setErrorDescription("Error While trying to Invoke RTS Socket Timeout");
    			throw yfsExcep;
		    	//End : OMNI-104596 : Handling Exception cases for Gift Cards
	        }
	        	
	        //END : GCD-134
		}catch(SSLHandshakeException e){
	        log.verbose("RTS call--> :: SSLHandshakeException"+e.toString());
	        e.printStackTrace();	        
	        if(AcademyConstants.STR_RTS_ACTIVATION_TRAN_TYPE.equals(requestXMLDoc.getElementsByTagName(AcademyConstants.ATTR_RTS_TRAN_TYPE).item(0).getTextContent()) &&
	        		YFCCommon.isVoid((Element) requestXMLDoc.getElementsByTagName(AcademyConstants.ATTR_RTS_OPTIONS).item(0))){
	        	log.error("Gift Card :: SSLHandshakeException occured for Activation call for Order "+requestXMLDoc.getElementsByTagName(AcademyConstants.ATTR_RTS_INVOICE).item(0).getTextContent());
	        	Document rtsRespDoc = sendActivationTimeOut(env,requestXMLDoc);
	        	log.verbose("ActivatePhysicalCard : sendActivationTimeOut response "+ XMLUtil.getXMLString(rtsRespDoc));
	        	return rtsRespDoc;
	        }
	        //Start : OMNI-104596 : Handling Exception cases for Gift Cards
	        else {
	        	log.error("Gift Card Error :: For Order# "
						+ requestXMLDoc.getElementsByTagName(
								AcademyConstants.ATTR_RTS_INVOICE)
								.item(0).getTextContent()
						+ " Throw SSLHandshakeException in sendHTTP");
	        	YFSException yfsExcep = new YFSException(e.getMessage());
    			yfsExcep.setErrorCode("EXTN_RTS_101");
    			yfsExcep.setErrorDescription("Error While trying to Invoke RTS SSL Handshake Exception");
    			throw yfsExcep;
	        }
			//End : OMNI-104596 : Handling Exception cases for Gift Cards
	        
		}catch(Exception e){
	        //Start : OMNI-104596 : Handling Exception cases for Gift Cards
			log.error("Gift Card Error :: For Order# "
					+ requestXMLDoc.getElementsByTagName(
							AcademyConstants.ATTR_RTS_INVOICE)
							.item(0).getTextContent()
					+ " RTS call Generic Exception---> :: Exception"+e.toString());
	        e.printStackTrace();
	        YFSException yfsExcep = new YFSException(e.getMessage());
			yfsExcep.setErrorCode("EXTN_RTS_200");
			yfsExcep.setErrorDescription("Error While trying to Invoke RTS Generic Exception");
			throw yfsExcep;
	        //End : OMNI-104596 : Handling Exception cases for Gift Cards
		}
		
		finally{			
				
			if (connection != null) {
				connection.disconnect();
			}			
		}
	    return XMLUtil.getDocument(strRTSResponse.toString());

	}
	
	/**
	 * Added to invoke RTS service from AcademyGCActivationReprocessingAgent agent in service AcademyInvokeRTSReprocessGCActivation.
	 * @param env
	 * @param rtsReqDoc
	 * @return
	 * @throws Exception
	 */
	//changed method signature to pass argument tab aparameters to sendHTTP method.
	public Document sendHTTPReprocessGCActivation(YFSEnvironment env,Document rtsReqDoc) throws Exception {
		log.verbose("Invoking RTS without getRTSHTTPRequestDoc, for ReprocessGCActivation :: " + XMLUtil.getXMLString(rtsReqDoc));
		chargeType="Activate";
		//Read from argument tab.   	
		System.setProperty(AcademyConstants.STR_KEY_SSL_TRUST_STORE_GC, props.getProperty(AcademyConstants.STR_KEY_STORE_GC));
		System.setProperty(AcademyConstants.STR_KEY_SSL_TRUST_STORE_PASSWORD_GC, props.getProperty(AcademyConstants.STR_KEY_PASSWORD_GC));
		log.verbose("System properties set!!");
		Document docRTSresp = sendHTTP(env,rtsReqDoc);
		return docRTSresp;
	}
	
	//START : GCD-134
	/**
	 * Method invokes RTS to reverse the Activation and then makes another Activation call.
	 * @param reqDoc - RTS Activation request XML
	 * @return docRTSResp
	 * @throws Exception
	 */
	private Document sendActivationTimeOut(YFSEnvironment env, Document reqDoc) throws Exception{
		
		log.verbose("Entering sendActivationTimeOutReversalRequest ::"+ XMLUtil.getXMLString(reqDoc));
		Document docRTSResp = null;		
		Element eleIxOptions = null;
		String strActionCode = null;
		
		//For 'Activation Reversal', append <IxOptions>*reversal</IxOptions> to the Activation request XML
		Element elereqDoc = reqDoc.getDocumentElement();
		eleIxOptions = reqDoc.createElement(AcademyConstants.ATTR_RTS_OPTIONS);
		eleIxOptions.appendChild(reqDoc.createTextNode("*reversal"));
		elereqDoc.appendChild(eleIxOptions);
		
		try{
			log.verbose("Activation Reversal Input ::"+ XMLUtil.getXMLString(reqDoc));		
			Document rtsActvnRevrsRespDoc = sendHTTP(env,reqDoc);
			log.verbose("Activation Reversal Output ::"+ XMLUtil.getXMLString(rtsActvnRevrsRespDoc));			
			
			if(!YFCCommon.isVoid(rtsActvnRevrsRespDoc.getDocumentElement())){
				Element eleIxActionCode =  (Element) rtsActvnRevrsRespDoc.getElementsByTagName(AcademyConstants.ATTR_RTS_ACTION_CODE).item(0);
				if(!YFCCommon.isVoid(eleIxActionCode)){
					strActionCode = rtsActvnRevrsRespDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_ACTION_CODE).item(0).getTextContent();
				    log.verbose("isVoid(strActionCode) : "+YFCCommon.isVoid(strActionCode));
				
			    //Means Activation reversal was successfull
			    if(!YFCCommon.isVoid(strActionCode)){
			    	//After 'Activation Reversal' call,to make retry Activation call remove <IxOptions>*reversal</IxOptions>
			    	reqDoc.getDocumentElement().removeChild(reqDoc.getElementsByTagName(AcademyConstants.ATTR_RTS_OPTIONS).item(0));
					log.verbose("Activation after reversal, Input ::"+ XMLUtil.getXMLString(reqDoc));		
					docRTSResp = sendHTTP(env,reqDoc);
					log.verbose("Activation after reversal, Output ::"+ XMLUtil.getXMLString(docRTSResp));
			    }
				}else{//This will make sure transaction is rolled back and 'AcademyGCActivationProcess' TaskQ entry is retained to retry later.
					throw new YFSException("Activation Reversal Unsuccessful");
				}
			}			
		}catch(Exception e){
			e.printStackTrace();
			log.verbose("Exception!!!! : Activation Reversal/Re-Activation RTS call :: "+e.toString());
			throw e;
		}
		
		return docRTSResp;
	}
	//END : GCD-134
	
	//Time Out Reversal  
	private boolean sendTimeOutReversalRequest(YFSEnvironment env, Document reqDoc) throws Exception{
		Document docRTSResp = null;
		log.verbose("Entering sendTimeOutReversalRequest()");
		String strActionCode = null;
		try{
			reqDoc.getElementsByTagName(AcademyConstants.ATTR_RTS_OPTIONS).item(0).setTextContent("multilock *reversal");
		    log.verbose("Sale Reversal transaction:: Input :"+XMLUtil.getXMLString(reqDoc));    		
		    
		    docRTSResp = sendHTTP(env,reqDoc);
		    log.verbose("Sale Reversal transaction:: Output :"+XMLUtil.getXMLString(docRTSResp));
		    
		    if(!YFCCommon.isVoid(docRTSResp.getDocumentElement()))
		    	strActionCode = docRTSResp.getDocumentElement().getElementsByTagName(AcademyConstants.ATTR_RTS_ACTION_CODE).item(0).getTextContent();
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
	
	
//START:GCD-97 Commenetd the stubbing code 
//	/**
//	 * Stubbing the response until RTS system is up
//	 */
//	private Document sendHTTPTest(Document rtsReqDoc) throws ParserConfigurationException, SAXException, IOException {
//		
//		String strAmount = null;
//	    File file = null;
//	    if("Redeem".equalsIgnoreCase(transactionType)){
//	    	file = new File("/apps/SterlingOMS/Foundation/bin/authRevrsalRTSresponse.xml");
//	    }else if ("AUTHORIZATION".equals(this.chargeType)){
//	      file = new File("/apps/SterlingOMS/Foundation/bin/authRTSresponse.xml");
//	    }else if ("CHARGE".equals(this.chargeType)){
//	      file = new File("/apps/SterlingOMS/Foundation/bin/chargeRTSresponse.xml");
//	    }else if ("Activate".equals(this.chargeType)) {
//	      file = new File("/apps/SterlingOMS/Foundation/bin/activation.xml");
//	    }
//	    
//	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//	    DocumentBuilder db = dbf.newDocumentBuilder();
//	    dbf.setValidating(true);
//	    Document doc = XMLUtil.getDocument("<XMLAJBFipayResponse version='1.0'><IxCmd>0101</IxCmd><IxActionCode>0</IxActionCode><IxIsoResp>00</IxIsoResp><IxTimeOut>7</IxTimeOut><IxDebitCredit>GiftCard</IxDebitCredit><IxStoreNumber>005</IxStoreNumber><IxTerminalNumber>0005</IxTerminalNumber><IxTranType>Activate</IxTranType><IxAccount>8555011234561107</IxAccount><IxInvoice>OrderNo</IxInvoice><IxPostingDate>RTS STAN</IxPostingDate><IxDate>MMDDYYYY</IxDate><IxTime>HHMMSS</IxTime><IxDepositData>10000</IxDepositData><IxResponseTimeMilliSeconds>HHMMSSmm</IxResponseTimeMilliSeconds><IxProductInfo></IxProductInfo><IxMailOrderAVSData>*ECI</IxMailOrderAVSData></XMLAJBFipayResponse>");
//	    if (file != null)
//	      doc = db.parse(file);
//	    log.verbose("Stubbing RTS response -After doc read :: " + XMLUtil.getXMLString(doc));
//	    
//	    doc.getDocumentElement().getElementsByTagName("IxAccount").item(0).setTextContent(
//	    		rtsReqDoc.getDocumentElement().getElementsByTagName("IxAccount").item(0).getTextContent());
//	    doc.getDocumentElement().getElementsByTagName("IxInvoice").item(0).setTextContent(
//	    		rtsReqDoc.getDocumentElement().getElementsByTagName("IxInvoice").item(0).getTextContent());
//	    
//	    Calendar cal = Calendar.getInstance();		
//	    doc.getDocumentElement().getElementsByTagName("IxDate").item(0).setTextContent(
//	    		new SimpleDateFormat("MMddyyyy").format(cal.getTime()));
//	    doc.getDocumentElement().getElementsByTagName("IxTime").item(0).setTextContent(
//	    		new SimpleDateFormat("HHmmss").format(cal.getTime()));
//	    
//	    log.verbose("doc-> IxAccount :: " + XMLUtil.getXMLString(doc));
//	    
//	    log.verbose("chargeType " + chargeType);
//	    log.verbose("chargeType boolean " + !"Activate".equals(this.chargeType));
//	    
//	    if(!"Activate".equals(this.chargeType)){
//	    	log.verbose("ACTIVATE " + chargeType);
//	    	strAmount = String.valueOf(Double.parseDouble(rtsReqDoc.getDocumentElement().getElementsByTagName("IxAmount").item(0).getTextContent()));
//		    log.verbose("doc-> strAmount :: " + strAmount);
//		    doc.getDocumentElement().getElementsByTagName("IxAmount").item(0).setTextContent(strAmount);		    
//		    doc.getDocumentElement().getElementsByTagName("IxDepositData").item(0).setTextContent(strAmount);
//		    String authCode = randomstring(6, 6);
//            log.verbose((new StringBuilder("AuthCode is::")).append(authCode).toString());
//            doc.getDocumentElement().getElementsByTagName("IxAuthCode").item(0).setTextContent(authCode);
////		    doc.getDocumentElement().getElementsByTagName("IxAuthCode").item(0).setTextContent("410119");//Hardcoding for testing 
//	    }
//	    
//	    log.verbose("Stubbing RTS response -After dynamic data change :: " + XMLUtil.getXMLString(doc));
//	    
//	    return doc;
//	}
//	 public static int rand(int lo, int hi)
//	    {
//	        int n = (hi - lo) + 1;
//	        int i = rn.nextInt() % n;
//	        if(i < 0)
//	        {
//	            i = -i;
//	        }
//	        return lo + i;
//	    }
//
//	    public static String randomstring(int lo, int hi)
//	    {
//	        int n = rand(lo, hi);
//	        byte b[] = new byte[n];
//	        for(int i = 0; i < n; i++)
//	        {
//	            b[i] = (byte)rand(49, 56);
//	        }
//
//	        return new String(b);
//	    }
//END: GCD-97
}
