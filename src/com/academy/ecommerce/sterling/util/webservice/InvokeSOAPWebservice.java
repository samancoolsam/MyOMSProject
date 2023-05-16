package com.academy.ecommerce.sterling.util.webservice;


import java.net.URL;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.dom.DOMSource;

import org.apache.axis.message.MessageElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.academy.ecommerce.sterling.util.AcademyServiceUtil;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class InvokeSOAPWebservice implements YIFCustomApi {
	private Properties props;

	/**
	 * 
	 * Instance of logger
	 * 
	 */
	private static YFCLogCategory log = YFCLogCategory
			.instance(InvokeSOAPWebservice.class);

	public void setProperties(Properties props) {
		this.props = props;
	}

	public Document invokeSoapWebservice(YFSEnvironment env, Document inDoc)
			throws Exception {
		log.verbose("*** invokeSoapWebservice call with input:" + XMLUtil.getXMLString(inDoc));
		String methodName = props.getProperty("MethodName");
		log.verbose("*** methodName *** " + methodName);
		
		String endPointURL = props.getProperty("URL");
		log.verbose("*** strEndpointURL *** " + endPointURL);
		URL endpoint = new URL(endPointURL);
		
		SOAPConnection connection =	AcademyServiceUtil.getSOAPConnection();
		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage request = messageFactory.createMessage();

		String soapActionNamespace = props.getProperty("AcadSoapActionNameSpace");
		if(!YFCObject.isVoid(soapActionNamespace)) {
			MimeHeaders headers = request.getMimeHeaders();
	        headers.addHeader("SOAPAction", soapActionNamespace);
		}
        
		if(!YFCObject.isVoid(methodName)) {
			log.verbose("*** Inside stub invocation *** ");
			Document inputDocument = XMLUtil.newDocument();
			Element rootElement = inputDocument.createElement("web:" + methodName);
			rootElement.setAttribute("xmlns:web", "http://webservices.academy");
			Node ndTemp = inputDocument.importNode(inDoc.getDocumentElement(), false);
			rootElement.appendChild(ndTemp);
			inputDocument.appendChild((Node) rootElement);
			request.getSOAPBody().addDocument(inputDocument);
		} else {
			request.getSOAPPart().setContent(new DOMSource(inDoc));	
		}

		log.verbose("Request : " + request.toString());
		log.verbose("End point : " + endpoint.toString());
		SOAPMessage response = connection.call(request, endpoint);
		log.verbose("*** webservice response *** " + XMLUtil.getXMLString(response.getSOAPPart()));
		SOAPBody soapBody = response.getSOAPBody();
		Iterator<MessageElement> itr = soapBody.getChildElements();
		MessageElement ME = itr.next(); 
		Document responseDoc = ME.getAsDocument(); 
		log.verbose("*** invokeSoapWebservice returning *** " +	XMLUtil.getXMLString(responseDoc));
		return responseDoc;
	}
}
