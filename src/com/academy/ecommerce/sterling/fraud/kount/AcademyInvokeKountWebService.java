package com.academy.ecommerce.sterling.fraud.kount;

/*##################################################################################
*
* Project Name                : Kount Integration
* Module                      : OMS
* Author                      : CTS
* Date                        : 10-MAY-2019 
* Description				  : This util class is common class to invoke kount web-service based on URL type
* Change Revision
* ---------------------------------------------------------------------------------
* Date            Author         		Version#       Remarks/Description                      
* ---------------------------------------------------------------------------------
* 10-MAY-2019		CTS  	 			  1.0           	Initial version
* 16-MAY-2019		CTS  	 			  1.1           	Updated code to handle multiple error codes
* 22-MAY-2019		CTS  	 			  1.2           	Updated code to update return order refund status to Kount(FPT-22,28,54)
* ##################################################################################*/

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.json.utils.XML;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSException;
import com.yantra.yfs.core.YFSSystem;

public class AcademyInvokeKountWebService {
	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyInvokeKountWebService.class);

	/** This method invoke kount webservice method based on url type
	 * 
	 * @param String
	 * @param String
	 * @return
	 * @throws Exception
	 */

	/***** 
	 * ********** Expected WS success response ***********
	 ** <?xml version="1.0" encoding="UTF-8"?>
	 **	<jsonObject status="ok">
		    <count failure="0" success="1"/>
	 *	</jsonObject>
	 * 
	 ****/
	/**
	 * ********** Expected WS failure response ***********
	 * <jsonObject status="failure">
		    <errors>
		        <jsonObject code="1731" msg="Transaction not found [123ABCZ]"
		            scope="123ABCZ" type="404"/>
		    </errors>
		    <count failure="1" success="0"/>
		</jsonObject>
	 ***/
	public static Document invokeWebservice(String strKountInput, String strURLType) throws Exception {
		log.verbose("Begin of AcademyInvokeKountWebService.invokeWebservice() ");
		Document docWSResponce = null;

		String strKountURL = YFSSystem.getProperty(AcademyConstants.PROP_KOUNT_ENDPOINT_URL+"."+strURLType);
		log.verbose("AcademyInvokeKountWebService: Kount Webservice URL :: " + strKountURL);

		if(strURLType.equals(AcademyConstants.STR_KOUNT_URL_STATUS)) {
			docWSResponce = invokeKountWebService(strKountInput, strKountURL, AcademyConstants.WEBSERVICE_POST);
			log.verbose("AcademyInvokeKountWebService:invokeWebservice: Status: docWSResponce \n" + XMLUtil.getXMLString(docWSResponce));
		}
		else if(strURLType.equals(AcademyConstants.STR_KOUNT_URL_DETAIL)) {
			docWSResponce = invokeKountWebService(strKountInput, strKountURL + "?" + strKountInput, AcademyConstants.STR_HTTP_GET);
			log.verbose("AcademyInvokeKountWebService:invokeWebservice: detail: docWSResponce \n" + XMLUtil.getXMLString(docWSResponce));								
		}
		//Start : FPT-22,28,54
		else if(strURLType.equals(AcademyConstants.STR_KOUNT_URL_RFCB)) {
			docWSResponce = invokeKountWebService(strKountInput, strKountURL, AcademyConstants.WEBSERVICE_POST);
			log.verbose("AcademyInvokeKountWebService:invokeWebservice: rfcb: docWSResponce \n" + XMLUtil.getXMLString(docWSResponce));
		}
		//End : FPT-22,28,54

		String strStatus = docWSResponce.getDocumentElement().getAttribute(AcademyConstants.STR_KOUNT_URL_STATUS);
		log.verbose("AcademyInvokeKountWebService:invokeWebservice:strStatus " + strStatus);

		if (!YFSObject.isVoid(strStatus) && strStatus.equalsIgnoreCase(AcademyConstants.STR_WS_STATUS_OK)) {

			String strSuccessValue = XMLUtil.getAttributeFromXPath(docWSResponce, AcademyConstants.XPATH_WS_SUCCESSVALUE);
			if(!YFSObject.isVoid(strSuccessValue) && strSuccessValue.equals(AcademyConstants.STR_ZERO)) {
				throwCustomException(docWSResponce);
			}else {
				log.verbose("Status Webservice Processed Successfully: WS Status is " + strStatus);
			}
		}else {
			throwCustomException(docWSResponce);
		}

		log.verbose("End of AcademyInvokeKountWebService.invokeWebservice() ");
		return docWSResponce;
	}

	/**This method throws custom exception with error message
	 * @param inDoc
	 * @return
	 * @throws Exception
	 **/

	/**
	 * <jsonObject status="failure">
		    <errors>
		        <jsonObject code="1731/1725" msg="Transaction not found [123ABCZ]/Invalid transaction id [70NX02VRJMZW1]"
		            scope="123ABCZ/70NX02VRJMZW1" type="404/400"/>
		    </errors>
		    <count failure="1" success="0"/>
	   </jsonObject>
	 * 
	 **/
	/**
	 * <Error ExceptionType="GenericException" HttpErrorMessage="cannot write to a URLConnection if doOutput=false - call setDoOutput(true)" 
	 * HttpStatusCode="504"/>
	 **/
	private static void throwCustomException(Document docWSResponce) throws Exception {

		log.verbose("Begin of AcademyInvokeKountWebService.throwCustomException() ");

		String strErrorMessage = XMLUtil.getAttributeFromXPath(docWSResponce, AcademyConstants.XPATH_WS_MSG);
		String strErrorCode = XMLUtil.getAttributeFromXPath(docWSResponce, AcademyConstants.XPATH_WS_ERR_CODE);

		if(YFCObject.isVoid(strErrorMessage)) {
			strErrorMessage = docWSResponce.getDocumentElement().getAttribute(AcademyConstants.ATTR_EXCPTN_TYPE) + AcademyConstants.STR_HYPHEN +
			docWSResponce.getDocumentElement().getAttribute(AcademyConstants.ATTR_HTTP_ERROR_MESSAGE);
		}

		if(YFCObject.isVoid(strErrorCode)) {
			strErrorCode = docWSResponce.getDocumentElement().getAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE);
		}

		YFSException yfse = new YFSException();
		yfse.setErrorCode(strErrorCode);
		yfse.setErrorDescription(strErrorMessage);
		yfse.setAttribute("ErrorResponse", XMLUtil.getXMLString(docWSResponce) );

		log.verbose("End of AcademyInvokeKountWebService.throwCustomException() ");
		throw yfse;
	}

	/** This method connect Kount system to update order fraud status 
	 * 
	 * @param strInput
	 * @param strURL
	 * @param strWebserviceType
	 * @return
	 * @throws Exception
	 */
	private static Document invokeKountWebService(String strInput, String strURL, String strWebserviceType) throws Exception {
		log.verbose("Begin of AcademyInvokeKountWebService.invokeKountWebService() ");
		log.verbose("AcademyInvokeKountWebService.invokeKountWebService() JSON Input ::  "+ strInput);

		Document docResponse = null;
		String strProxyHost = null;
		String strProxyPort = null;

		try {	
			URL url = new URL(strURL);			 
			HttpsURLConnection conn;
			strProxyHost = YFSSystem.getProperty(AcademyConstants.PROP_KOUNT_HTTP_PROXYHOST);
			strProxyPort = YFSSystem.getProperty(AcademyConstants.PROP_KOUNT_HTTP_PROXYPORT);

			if(!YFCObject.isVoid(strProxyHost)){
				Proxy proxy;

				log.verbose("\n Connecting Kount through proxy:: strProxyHost : " +strProxyHost+ " ********* strProxyPort : "+strProxyPort );

				if(!YFSObject.isVoid(strProxyHost)) {
					proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(strProxyHost, Integer.parseInt(strProxyPort))); 
				}
				else {
					proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(strProxyHost,Integer.parseInt(AcademyConstants.STR_WS_PORT))); 
				}
				
				conn = (HttpsURLConnection) url.openConnection(proxy);
			}
			else {
				
				conn = (HttpsURLConnection) url.openConnection();
			}

			conn.setUseCaches(false);
			conn.setDoOutput(true);

			conn.setConnectTimeout(Integer.valueOf(YFSSystem.getProperty(AcademyConstants.PROP_KOUNT_CONNECT_TIMEOUT)));
			conn.setReadTimeout(Integer.valueOf(YFSSystem.getProperty(AcademyConstants.PROP_KOUNT_READ_TIMEOUT)));

			conn.setRequestMethod(strWebserviceType);
			conn.setRequestProperty(AcademyConstants.WEBSERVICE_CONTENT_TYPE, YFSSystem.getProperty(AcademyConstants.PROP_KOUNT_CONTENT_TYPE_VALUE));
			conn.setRequestProperty(YFSSystem.getProperty(AcademyConstants.PROP_KOUNT_API_KEY), YFSSystem.getProperty(AcademyConstants.PROP_KOUNT_API_KEY_VALUE));

			try {
				log.verbose("AcademyInvokeKountWebService.invokeKountWebService(): URL being used :: " + conn.getURL().toString());
				log.verbose("AcademyInvokeKountWebService.invokeKountWebService(): Input used :: " + strInput);

				if(strWebserviceType.equals(AcademyConstants.WEBSERVICE_POST)) {
					DataOutputStream outDataStream = new DataOutputStream(conn.getOutputStream());
					outDataStream.writeBytes(strInput);
					outDataStream.flush();
					outDataStream.close();
				}

				int iResponseCode = conn.getResponseCode();
				log.verbose("AcademyInvokeKountWebService.invokeKountWebService(): iResponseCode ::  " + iResponseCode);

				if (iResponseCode == Integer.parseInt(AcademyConstants.STATUS_CODE_200) || iResponseCode == Integer.parseInt(AcademyConstants.STATUS_CODE_201) || iResponseCode == Integer.parseInt(AcademyConstants.STATUS_CODE_202)) {
					log.verbose("AcademyInvokeKountWebService.invokeKountWebService(): WS calling SUCCESS");
				} else {
					log.verbose("AcademyInvokeKountWebService.invokeKountWebService(): WS calling failure ---> Response code(" + conn.getResponseCode() + ")");
					log.verbose("AcademyInvokeKountWebService.invokeKountWebService(): getResponseMessage : "+ conn.getResponseMessage());
				}

				docResponse = formatKountResponse(conn);

			} catch (SocketTimeoutException e) {

				log.info("AcademyInvokeKountWebService.invokeKountWebService(): WebService Exception:: SocketTimeoutException :: " + e.toString() + " Exception Stack Trace" + e.getStackTrace().toString());
				docResponse = XMLUtil.createDocument(AcademyConstants.ELE_ERROR);
				docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, AcademyConstants.STATUS_CODE_504);
				docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_EXCEPTION_TYPE, AcademyConstants.SOCKET_TIME_OUT_EXCEPTION);
				docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_ERROR_MESSAGE, e.getMessage());
			} catch (SSLHandshakeException e) {

				log.info("AcademyInvokeKountWebService.invokeKountWebService(): WebService Exception:: SSLHandshakeException :: " + e.toString() + " Exception Stack Trace" + e.getStackTrace().toString());
				docResponse = XMLUtil.createDocument(AcademyConstants.ELE_ERROR);
				docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, AcademyConstants.STATUS_CODE_504);
				docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_EXCEPTION_TYPE, AcademyConstants.SSL_HANDSHAKE_EXCEPTION);
				docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_ERROR_MESSAGE, e.getMessage());
			} catch (Exception e) {
				log.info("AcademyInvokeKountWebService.invokeKountWebService(): WebService Exception:: Generic Exception :: " + e.toString() + " Exception Stack Trace" + e.getStackTrace().toString());
				docResponse = XMLUtil.createDocument(AcademyConstants.ELE_ERROR);
				docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, AcademyConstants.STATUS_CODE_504);
				docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_EXCEPTION_TYPE, AcademyConstants.STR_GENERIC_EXCEPTION);
				docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_ERROR_MESSAGE, e.getMessage());
			}

		} catch (Exception e) {
			log.verbose("AcademyInvokeKountWebService.invokeKountWebService(): Exception before invoking web service call" + e.getMessage() + " \n " + e.toString());
			docResponse = XMLUtil.createDocument(AcademyConstants.ELE_ERROR);
			docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_STATUS_CODE, AcademyConstants.STATUS_CODE_504);
			docResponse.getDocumentElement().setAttribute(AcademyConstants.ATTR_HTTP_ERROR_MESSAGE, e.getMessage());
		}

		log.verbose("End of AcademyInvokeKountWebService.invokeKountWebService()");
		return docResponse;
	}

	/** This method convert the webservice output from json to xml format 
	 * 
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	private static Document formatKountResponse(HttpsURLConnection conn)
	throws Exception {

		log.verbose("Begin of AcademyInvokeKountWebService.formatKountResponse() method");
		Document docResponse = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					conn.getResponseCode() / 100 == 2 ? conn.getInputStream()
							: conn.getErrorStream()));

			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = reader.readLine()) != null) {
				response.append(inputLine);
			}
			reader.close();
			String strResponse = response.toString();

			log.verbose("AcademyInvokeKountWebService.formatKountResponse(): Webservice response :: \n" + strResponse);

			InputStream in = IOUtils.toInputStream(strResponse);
			String xml = XML.toXml(in);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			docResponse = builder.parse(new InputSource(new StringReader(xml)));

			log.verbose("AcademyInvokeKountWebService.formatKountResponse(): docResponse :: \n"+ XMLUtil.getXMLString(docResponse));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (docResponse != null)
			log.verbose("AcademyInvokeKountWebService.formatKountResponse() Output XML ::" + XMLUtil.getXMLString(docResponse));
		log.verbose("End of AcademyInvokeKountWebService.formatKountResponse() method");

		return docResponse;
	}
}
