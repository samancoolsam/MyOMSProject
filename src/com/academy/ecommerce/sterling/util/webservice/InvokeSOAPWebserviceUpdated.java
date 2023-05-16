//package Declaration
package com.academy.ecommerce.sterling.util.webservice;

//import statements

//java util import statements
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Properties;

//import javax.net.ssl.HttpsURLConnection;

//w3c import statements
import org.w3c.dom.Document;

//academy import statements
import com.academy.util.xml.XMLUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.ecommerce.sterling.shipment.AcademyChangeToVertexCallRequest;

//yantra import statements
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * 
 * Class to invoke Vertex-Lite using HTTP Calls with time-out and retry logic
 * OMNI-29859 : STL - Time Out Logic
 * OMNI-22670 : STL - Retry Logic
 * OMNI-28018 : Enable/Disable Proxy for Vertex Calls
 * OMNI-24737 : Retry to the Vertex-OnDemand URL after failure for Vertex-Lite URL
 *
 */

public class InvokeSOAPWebserviceUpdated implements YIFCustomApi {
	private Properties props;

	/**
	 * 
	 * Instance of log
	 * 
	 */
	private static YFCLogCategory log = YFCLogCategory
	.instance(InvokeSOAPWebserviceUpdated.class);

	public void setProperties(Properties props) {
		this.props = props;
	}


	public Document invokeSoapWebservice(YFSEnvironment env, Document docInput) 
	throws Exception{

		log.beginTimer(this.getClass() + ".invokeSoapWebservice");

		Document docOutput = null;
		log.verbose("Input Doc of invokeSoapWebservice : " +XMLUtil.getXMLString(docInput));
		
		String methodName = props.getProperty("MethodName");
		log.verbose("*** methodName *** " + methodName);
		
		String strEndPointUrl = props.getProperty("URL");

		log.verbose("Vertex EndPointUrl : " +strEndPointUrl);

		String strWebserviceRequest = XMLUtil.getXMLString(docInput);		

		log.verbose("Vertex strWebserviceRequest : " +strWebserviceRequest);
		
		docOutput = postHttp(env, strWebserviceRequest, strEndPointUrl);
				
		log.verbose("Output Doc of invokeSoapWebservice : " +XMLUtil.getXMLString(docOutput));
		
		return docOutput;

	}	

	
	/**
	 * This method is used to make a connection to HTTP webservices and 
	 * to post the request to these services. It sends the response received  
	 * from webservices to the calling method.
	 * 
	 * @param env
	 * @param strRequest
	 * @param strUrl
	 * @return docYfcOutput
	 * @throws Exception
	 */
	public Document postHttp(YFSEnvironment env, String strRequest, 
			String strUrl) throws Exception {

		log.beginTimer(this.getClass() + ".postHttp");		
		HttpURLConnection httpConn = null;
		PrintWriter prWriter = null;
		BufferedReader buffReader = null;
		String strOutput = null;
		Document docOutput = null;

		URL url = new URL(strUrl);	

		log.verbose("Vertex Webservice Url : "+strUrl);
		
		//Fetching the host-names of the web-service 
		String strHost;
		if (!YFCObject.isVoid(YFSSystem.getProperty(AcademyConstants.IS_VERTEX_LITE_ENABLED)) && 
				AcademyConstants.STR_YES.equalsIgnoreCase(YFSSystem.getProperty(AcademyConstants.IS_VERTEX_LITE_ENABLED))) {
			strHost = YFSSystem.getProperty(AcademyConstants.VERTEX_LITE_HOST_NAME);   //"slapvlt01e.academy.com";
		} else {
			strHost = YFSSystem.getProperty(AcademyConstants.VERTEX_7_HOST_NAME);   //"utaxws7.academy.com";
		}
		
		log.verbose("Vertex Webservice Host  : "+strHost);
		
		int iMaxRetryCount = 4;
		// Get the MaxRetries property, if it exists. If there is a problem, use 4 for 3 retries.
		if (!YFCObject.isVoid(YFSSystem.getProperty(AcademyConstants.VERTEX_MAX_RETRY_COUNT))) {
					try {
						iMaxRetryCount = Integer.parseInt(YFSSystem.getProperty(AcademyConstants.VERTEX_MAX_RETRY_COUNT));
					} catch (Exception e) {
						log.verbose("Error getting iMaxRetryCount: " + e);
						log.verbose(e);
						iMaxRetryCount = 4;
					}
				}
		
		boolean callOnDemandURLOnFailure = false;
		int iRetryCount = 0;
		
		while (iRetryCount < iMaxRetryCount) {
			log.verbose(":: iRetryCount  : "+iRetryCount+" :: iMaxRetryCount :: "+ iMaxRetryCount+" :: Attempt Count :: "+ (iRetryCount+1));

						
		try {

			//Start : OMNI-28018 : Changes to Enable/Disable Proxy for Vertex Call
			Proxy proxy;
			log.verbose("Is PROXY disabled for Vertex:: "+YFSSystem.getProperty(AcademyConstants.PROP_VERTEX_DISABLE_PROXY));
			
			if(!YFCObject.isVoid(YFSSystem.getProperty(AcademyConstants.PROP_VERTEX_DISABLE_PROXY)) 
					&& "Y".equals(YFSSystem.getProperty(AcademyConstants.PROP_VERTEX_DISABLE_PROXY))) {
				log.verbose("Connecting Vertex directly without Proxy :: strEndpointURL: "+strUrl);
				httpConn = (HttpURLConnection) url.openConnection();
			}
			else {
				String strProxyHost = YFSSystem.getProperty("academy.http.proxyHost");
				String strProxyPort = YFSSystem.getProperty("academy.http.proxyPort");
				log.verbose("Connecting Vertex through proxy:: strProxyHost: "+strProxyHost+" :: strProxyPort:"+strProxyPort + " :: strEndpointURL: "+strUrl);
				proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(strProxyHost, Integer.parseInt(strProxyPort))); 
				httpConn = (HttpURLConnection) url.openConnection(proxy);
			}
			//End : OMNI-28018 : Changes to Enable/Disable Proxy for Vertex Call
			


			httpConn.addRequestProperty("Host", strHost);
			
			int connectionTO= Integer.parseInt(YFSSystem.getProperty(AcademyConstants.VERTEX_CONNECTION_TIMEOUT));
			System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
			 
			log.verbose(" Timeout defined in MilliSeconds in Customer_Overrides File is " + connectionTO);
			httpConn.setConnectTimeout(connectionTO);
			httpConn.setReadTimeout(connectionTO);
						
			httpConn.addRequestProperty("Content-Type", "text/xml;charset=UTF-8");
			httpConn.addRequestProperty("SOAPAction", "");
			httpConn.addRequestProperty("Content-Length", String.valueOf(strRequest.length()));
						
			httpConn.setDoOutput(true);					
			httpConn.setDoInput(true);
			
			httpConn.connect();
						
			prWriter = new PrintWriter(httpConn.getOutputStream());
			prWriter.println(strRequest);
			prWriter.flush();

			// Get the response code & response message
			int iResponseCode = httpConn.getResponseCode();
			log.verbose(":: iResponseCode  : "+iResponseCode);
			String strResponseMessage = httpConn.getResponseMessage();

			log.verbose(":: strResponseMessage  : "+strResponseMessage);
			// Case of HTTP Success Response
			if (iResponseCode < 400){
				//Create a input stream reader and get the string output
				//of the webservice.	
				buffReader = new BufferedReader(new InputStreamReader(httpConn.
						getInputStream()));
				strOutput = getResponseData(buffReader);
				//log.verbose("Vertex :: strOutput :: "+strOutput);
				
				//Convert the string output to YFC doc.
				docOutput = XMLUtil.getDocument(strOutput);
				log.verbose("Vertex Webservice Response (HTTP Success) : "+XMLUtil.getXMLString(docOutput));
				callOnDemandURLOnFailure = false;
				break;
			}

			// Case of HTTP Error Response
			else if (iResponseCode >= 400){
				//Create a input stream reader and get the string output 
				//of the webservice.
				buffReader = new BufferedReader(new InputStreamReader(httpConn.
						getErrorStream()));				
				strOutput = getResponseData(buffReader);
				log.error("Vertex Webservice Response (HTTP Error) :: strOutput :: "+strOutput);
				iRetryCount++ ;
				callOnDemandURLOnFailure = true;
				//Convert the string output to YFC doc.
				//docYfcOutput = YFCDocument.getDocumentFor(strOutput);
				//log.verbose("Webservice Response (HTTP Error) : "+XMLUtil.getXMLString(docYfcOutput.getDocument()));
				
			}		

		} catch(SocketTimeoutException  e){
			log.error("InvokeSOAPWebserviceUpdated.postHttp SocketTimeoutException Occured" +e.getMessage());
			iRetryCount++ ;			
			callOnDemandURLOnFailure = true;
		} catch (Exception e) {
			log.error("InvokeSOAPWebserviceUpdated.postHttp Exception Occured " +e.getMessage());
			//iRetryCount++ ;
			callOnDemandURLOnFailure = false;
			break;			
		} finally {
			if (httpConn != null) {
				httpConn.disconnect();
				if (prWriter != null)
					prWriter.close();
				if (buffReader != null)
					buffReader.close();
			}
		}

		}
		
		//Start : OMNI-24737 : Retry to the Vertex-OnDemand URL after failure for Vertex-Lite URL		
		if(callOnDemandURLOnFailure) {
			if(!YFCObject.isVoid(YFSSystem.getProperty(AcademyConstants.IS_VERTEX_ONDEMAND_RETRY_ENABLED)) 
					&& "Y".equals(YFSSystem.getProperty(AcademyConstants.IS_VERTEX_ONDEMAND_RETRY_ENABLED))) {
				
				//Retrieving VertexRequestWithoutSoapEnvelope Transaction Object from AcademyChangeToVertexCallRequest.java
				Document VertexRequestWithoutSoapEnvelope = (Document) env.getTxnObject("VertexRequestWithoutSoapEnvelope");
				Document VertexRequestWithSoapEnvelope = AcademyChangeToVertexCallRequest.wrapSoapEnvelopeForVertexOnDemand(VertexRequestWithoutSoapEnvelope);
				String strOnDemandRequest = XMLUtil.getXMLString(VertexRequestWithSoapEnvelope);		
				log.verbose("strOnDemandWebserviceRequest : " +strOnDemandRequest);
				log.verbose("Retrying to Vertex OnDemand");
				docOutput = callOnDemandURLOnFailure(env, strOnDemandRequest);
			}
		}
		//End : OMNI-24737 : Retry to the Vertex-OnDemand URL after failure for Vertex-Lite URL
		
		
		if(YFCObject.isVoid(docOutput)) {
			docOutput = XMLUtil.createDocument("Error");
			docOutput.getDocumentElement().setAttribute("ErrorCode", "404");
			docOutput.getDocumentElement().setAttribute("ErrorDescription", "Tax Call is timed Out");
		}
		log.endTimer(this.getClass() + ".postHttp");
		return docOutput;
	}	

	/**
	 * This method is used to get the string response out of BufferedReader.
	 * 
	 * @param reader
	 * @return string
	 */
	public String getResponseData(BufferedReader reader) throws IOException{

		log.beginTimer(this.getClass() + ".getResponseData");
		String strline;
		StringBuffer outputBuffer = new StringBuffer("");
		while ((strline = reader.readLine()) != null) {
			outputBuffer.append(strline);
		}

		log.endTimer(this.getClass() + ".getResponseData");
		return outputBuffer.toString();
	}
	
	/**
	 * This method is used to make a connection to onDemandURL on failure and 
	 * to post the request to these services. It sends the response received  
	 * from HTTP webservices to the calling method.
	 * OMNI-24737 : Retry to the Vertex-OnDemand URL after failure for Vertex-Lite URL
	 * 
	 * @param env
	 * @param strRequest
	 * @return docYfcOutput
	 * @throws Exception
	 */
	private Document callOnDemandURLOnFailure(YFSEnvironment env, String strRequest)
		throws Exception {

			log.beginTimer(this.getClass() + ".callOnDemandURLOnFailure");		
			HttpURLConnection httpConn = null;
			PrintWriter prWriter = null;
			BufferedReader buffReader = null;
			String strOutput = null;
			Document docOutput = null;
			String strUrl = null;
			if (!YFCObject.isVoid(YFSSystem.getProperty(AcademyConstants.VERTEX_ONDEMAND_URL)) ) {
				strUrl = YFSSystem.getProperty(AcademyConstants.VERTEX_ONDEMAND_URL);   
			} 
			URL url = new URL(strUrl);	
			log.verbose("OnDemand Webservice Url : "+strUrl);
			
			//Fetching the host-names of the web-service
			String strHost = null;
			if (!YFCObject.isVoid(YFSSystem.getProperty(AcademyConstants.VERTEX_ONDEMAND_HOST_NAME)) ) {
				strHost = YFSSystem.getProperty(AcademyConstants.VERTEX_ONDEMAND_HOST_NAME);   //"academy.ondemand.vertexinc.com";
			} 
			log.verbose("OnDemand Webservice Host  : "+strHost);
			
			int iOnDemandMaxRetryCount = 1;
			// Get the MaxRetries property, if it exists. If there is a problem, use 1 for no retry.
			if (!YFCObject.isVoid(YFSSystem.getProperty(AcademyConstants.VERTEX_ONDEMAND_MAX_RETRY_COUNT))) {
						try {
							iOnDemandMaxRetryCount = Integer.parseInt(YFSSystem.getProperty(AcademyConstants.VERTEX_ONDEMAND_MAX_RETRY_COUNT));
						} catch (Exception e) {
							log.verbose("Error getting iMaxRetryCount: " + e);
							log.verbose(e);
							iOnDemandMaxRetryCount = 1;
						}
					}

			int iOnDemandRetryCount = 0;
			
			while (iOnDemandRetryCount < iOnDemandMaxRetryCount) {
				log.verbose(":: iOnDemandRetryCount  : "+iOnDemandRetryCount+" :: iOnDemandMaxRetryCount :: "+ iOnDemandMaxRetryCount+" :: Attempt Count :: "+ (iOnDemandRetryCount+1));

			try {
				
				//Start : OMNI-28018 : Changes to Enable/Disable Proxy for Vertex Call
				Proxy proxy;
				log.verbose("Is PROXY disabled for Vertex:: "+YFSSystem.getProperty(AcademyConstants.PROP_VERTEX_DISABLE_PROXY));
				
				if(!YFCObject.isVoid(YFSSystem.getProperty(AcademyConstants.PROP_VERTEX_DISABLE_PROXY)) 
						&& "Y".equals(YFSSystem.getProperty(AcademyConstants.PROP_VERTEX_DISABLE_PROXY))) {
					log.verbose("Connecting Vertex directly without Proxy :: strEndpointURL: "+strUrl);
					httpConn = (HttpURLConnection) url.openConnection();
				}
				else {
					String strProxyHost = YFSSystem.getProperty("academy.http.proxyHost");
					String strProxyPort = YFSSystem.getProperty("academy.http.proxyPort");
					log.verbose("Connecting Vertex through proxy:: strProxyHost: "+strProxyHost+" :: strProxyPort: "+strProxyPort + " :: strEndpointURL: "+strUrl);
					proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(strProxyHost, Integer.parseInt(strProxyPort))); 
					httpConn = (HttpURLConnection) url.openConnection(proxy);
				}
				//End : OMNI-28018 : Changes to Enable/Disable Proxy for Vertex Call
				
				httpConn.addRequestProperty("Host", strHost);
				
				int connectionTO= Integer.parseInt(YFSSystem.getProperty(AcademyConstants.VERTEX_ONDEMAND_CONNECTION_TIMEOUT));
				System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
				 
				log.verbose(" Timeout defined in MilliSeconds in Customer_Overrides File is " + connectionTO);
				httpConn.setConnectTimeout(connectionTO);
				httpConn.setReadTimeout(connectionTO);
							
				httpConn.addRequestProperty("Content-Type", "text/xml;charset=UTF-8");
				httpConn.addRequestProperty("SOAPAction", "");
				httpConn.addRequestProperty("Content-Length", String.valueOf(strRequest.length()));
							
				httpConn.setDoOutput(true);					
				httpConn.setDoInput(true);
				
				httpConn.connect();
							
				prWriter = new PrintWriter(httpConn.getOutputStream());
				prWriter.println(strRequest);
				prWriter.flush();

				// Get the response code & response message
				int iOnDemandResponseCode = httpConn.getResponseCode();
				log.verbose(":: iOnDemandResponseCode  : "+iOnDemandResponseCode);
				String strOnDemandResponseMessage = httpConn.getResponseMessage();

				log.verbose(":: strResponseMessage  : "+strOnDemandResponseMessage);
				// Case of HTTP Success Response
				if (iOnDemandResponseCode < 400){
					//Create a input stream reader and get the string output
					//of the webservice.	
					buffReader = new BufferedReader(new InputStreamReader(httpConn.
							getInputStream()));
					strOutput = getResponseData(buffReader);
					//log.verbose("Vertex OnDemand :: strOutput :: "+strOutput);
					
					//Convert the string output to YFC doc.
					docOutput = XMLUtil.getDocument(strOutput);
					log.verbose("Vertex OnDemand Webservice Response (HTTP Success) : "+XMLUtil.getXMLString(docOutput));
					break;
				}

				// Case of HTTP Error Response
				else if (iOnDemandResponseCode >= 400){
					//Create a input stream reader and get the string output 
					//of the webservice.
					buffReader = new BufferedReader(new InputStreamReader(httpConn.
							getErrorStream()));				
					strOutput = getResponseData(buffReader);
					log.error("Vertex OnDemand Webservice Response (HTTP Error) :: strOutput :: "+strOutput);
					iOnDemandRetryCount++ ;
					//Convert the string output to YFC doc.
					//docYfcOutput = YFCDocument.getDocumentFor(strOutput);
					//log.verbose("Webservice Response (HTTP Error) : "+XMLUtil.getXMLString(docYfcOutput.getDocument()));
					
				}		

			} catch(SocketTimeoutException  e){
				log.error("InvokeSOAPWebserviceUpdated.callOnDemandURL SocketTimeoutException Occured" +e.getMessage());
				iOnDemandRetryCount++ ;			
			} catch (Exception e) {
				log.error("InvokeSOAPWebserviceUpdated.callOnDemandURL Exception Occured " +e.getMessage());
				//iRetryCount++ ;
				break;			
			} finally {
				if (httpConn != null) {
					httpConn.disconnect();
					if (prWriter != null)
						prWriter.close();
					if (buffReader != null)
						buffReader.close();
				}
			}

			}

			if(YFCObject.isVoid(docOutput)) {
				docOutput = XMLUtil.createDocument("Error");
				docOutput.getDocumentElement().setAttribute("ErrorCode", "404");
				docOutput.getDocumentElement().setAttribute("ErrorDescription", "Tax Call is timed Out");
			}
					
			log.endTimer(this.getClass() + ".callOnDemandURLOnFailure");
			return docOutput;
	}
	
}
