package com.academy.ecommerce.sterling.util;

// Java imports
import java.io.IOException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;

//Other imports
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

//Sterling imports
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSSystem;

/**
 * Utility class for creating WebService connection and template load.
 * @author psomashekar-tw
 *
 */

public class AcademyServiceUtil {

	/**
     * Log variable.
     */
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyServiceUtil.class);
	
	public static SOAPConnection getSOAPConnection() throws UnsupportedOperationException, SOAPException {
		log.verbose("*************** getSOAPConnection() *********");
		String httpProxyHost = YFSSystem.getProperty("academy.http.proxyHost");
		String httpProxyPort = YFSSystem.getProperty("academy.http.proxyPort");	
		String httpNonProxyHosts = YFSSystem.getProperty("academy.http.nonProxyHosts");	
		if(!YFCObject.isVoid(httpProxyHost) && !YFCObject.isVoid(httpProxyPort) 
				&& !YFCObject.isVoid(httpNonProxyHosts)) {
			log.verbose("***************Setting Proxy *********");
			System.setProperty("http.proxyHost", httpProxyHost);
			System.setProperty("http.proxyPort", httpProxyPort);
			System.setProperty("http.nonProxyHosts", httpNonProxyHosts);
		}
		
		System.setProperty("javax.xml.soap.SOAPConnectionFactory", 
				"org.apache.axis.soap.SOAPConnectionFactoryImpl");
		System.setProperty("javax.xml.soap.MessageFactory", "org.apache.axis.soap.MessageFactoryImpl");
		System.setProperty("javax.xml.soap.SOAPFactory", "org.apache.axis.soap.SOAPFactoryImpl");
		//setCertificates();
		SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
		return soapConnectionFactory.createConnection();
	}
	
	public static void setCertificates() {
		log.verbose("***************Setting Certificates *********");
		TrustManager[] trustAllCerts = new TrustManager[] {
			new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}
				public void checkClientTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
				}
				public void checkServerTrusted(
						java.security.cert.X509Certificate[] certs, String authType) {
				}
			}
		};
		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			log.error(e);
		}
	}

	public static Document getSOAPMessageTemplate(String filePath) throws SOAPException,
		ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(true);
		DocumentBuilder builder = dbFactory.newDocumentBuilder();
		log.verbose("*** builder.parse() filePath *** " + filePath);
		log.verbose("*** InputStream is *** " + AcademyServiceUtil.class.getResourceAsStream(filePath));
		Document document = builder.parse(AcademyServiceUtil.class.getResourceAsStream(filePath));
		log.verbose("*** builder.parse() *** " + XMLUtil.getXMLString(document));
		return document;
	}
	
	public static Document getSOAPMessageTemplateForPayPalRefund() throws SOAPException,
		ParserConfigurationException, SAXException, IOException {
		return getSOAPMessageTemplate("/global/template/webservice/paypal/RefundRequest.xml");
	}
	
	public static Document getSOAPMessageTemplateForCCAuth() throws SOAPException,
		ParserConfigurationException, SAXException, IOException {
		return getSOAPMessageTemplate("/global/template/webservice/cybersource/AuthRequest.xml");
	}
	
	public static Document getSOAPMessageTemplateForCCRefund() throws SOAPException,
		ParserConfigurationException, SAXException, IOException {
		return getSOAPMessageTemplate("/global/template/webservice/cybersource/RefundRequest.xml");
	}
	
	public static Document getSOAPMsgTemplateForWCCreateOrderWithoutLines() throws SOAPException,
		ParserConfigurationException, SAXException, IOException {
		return getSOAPMessageTemplate("/global/template/webservice/ibmwc/changeOrder/CreateOrderWithoutLines.xml");
	}

	public static Document getSOAPMsgTemplateForWCCreateOrderWithLines() throws SOAPException,
		ParserConfigurationException, SAXException, IOException {
		return getSOAPMessageTemplate("/global/template/webservice/ibmwc/changeOrder/CreateOrderWithLines.xml");
	}

	public static Document getSOAPMsgTemplateForWCUpdateOrder() throws SOAPException,
		ParserConfigurationException, SAXException, IOException {
		return getSOAPMessageTemplate("/global/template/webservice/ibmwc/changeOrder/UpdateOrder.xml");
	}
	public static Document getSOAPMsgTemplateForWCGetOrder() throws SOAPException,
		ParserConfigurationException, SAXException, IOException {
		return getSOAPMessageTemplate("/global/template/webservice/ibmwc/getOrder/GetOrder.xml");
	}

	public static Document getSOAPMsgTemplateForWCProcessOrder() throws SOAPException,
		ParserConfigurationException, SAXException, IOException {
		return getSOAPMessageTemplate("/global/template/webservice/ibmwc/processOrder/ProcessOrder.xml");
	}

	public static Document getSOAPMessageTemplateForAgentAddressLocation() throws SOAPException,
		ParserConfigurationException, SAXException, IOException {
		return getSOAPMessageTemplate("/global/template/webservice/agile/PierbrigeRateRequest.xml");
	}
	
	public static Document getSOAPMessageTemplateForRateShop() throws SOAPException,
		ParserConfigurationException, SAXException, IOException {
	return getSOAPMessageTemplate("/global/template/webservice/agile/PierbrigeRateShopRequest.xml");
	}

	public static Document getSOAPMsgTemplateForWCProcessOrderWithShippingInfo() throws SOAPException,
		ParserConfigurationException, SAXException, IOException {
		return getSOAPMessageTemplate("/global/template/webservice/ibmwc/processOrder/ProcessOrderForShipmentInfo.xml");
	}

	public static Document getSOAPMsgTemplateForProcessOrderForPayments() throws SOAPException,
		ParserConfigurationException, SAXException, IOException {
		return getSOAPMessageTemplate("/global/template/webservice/ibmwc/processOrder/ProcessOrderForPayments.xml");
	}
	
	// Fedex Pickup Enhancements
	public static Document getSOAPMessageTemplateForFedexPickupAvalReq()
			throws SOAPException, ParserConfigurationException, SAXException,
			IOException {
		return getSOAPMessageTemplate("/global/template/webservice/fedex/getPickupAvailability.xml");
	}

	public static Document getSOAPMessageTemplateForFedexCreatePickupReq()
			throws SOAPException, ParserConfigurationException, SAXException,
			IOException {
		return getSOAPMessageTemplate("/global/template/webservice/fedex/createPickupRequest.xml");
	}
	
	//START : PAYZ
	public static Document getSOAPMessageTemplateForPayZSendAndCommitReq() 
	throws SOAPException, ParserConfigurationException, SAXException,IOException {
		return getSOAPMessageTemplate("/global/template/webservice/payz/sendAndCommit.xml");		
	}
	//END : PAYZ
	
	//OMNI-5703 : Begin	
		public static Document getSOAPMessageTemplateForFedexExpressPickupAvailRequest()
				throws SOAPException, ParserConfigurationException, SAXException,
				IOException {
			return getSOAPMessageTemplate("/global/template/webservice/fedex/getPickupAvailabilityRequest.xml");
		}
	//OMNI-5703 : End
}
