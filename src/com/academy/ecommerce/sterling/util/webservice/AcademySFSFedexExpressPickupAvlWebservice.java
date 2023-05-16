package com.academy.ecommerce.sterling.util.webservice;

import java.net.URL;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.dom.DOMSource;

import org.apache.axis.message.MessageElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.util.AcademyServiceUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademySFSFedexExpressPickupAvlWebservice {

	private Properties props;

	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademySFSFedexExpressPickupAvlWebservice.class);

	public void setProperties(Properties props) {
		this.props = props;
	}

	public Document invokeFedexExpPickUpAvlWebservice(YFSEnvironment env,
			Document inDoc) throws Exception {

		if (log.isVerboseEnabled()) {
			log.verbose("invokeFedexExpPickUpAvlWebservice: START");
			log.verbose("invokeFedexExpPickUpAvlWebservice: Input Doc"
					+ XMLUtil.getXMLString(inDoc));
		}

		SOAPConnection connection = AcademyServiceUtil.getSOAPConnection();

		String strEndpointURL = YFSSystem
				.getProperty("academy.fedex.shipment.pickup.request.endpoint");
		if (log.isVerboseEnabled()) {
			log.verbose("invokeFedexExpPickUpAvlWebservice: strEndpointURL"
					+ strEndpointURL);
		}
		URL endpoint = new URL(strEndpointURL);

		SOAPMessage request = getSOAPMessageForFedexPickupAvalReq(inDoc);

		SOAPMessage response = connection.call(request, endpoint);

		log.verbose("invokeFedexExpPickUpAvlWebservice: SOAP response"
				+ XMLUtil.getXMLString(response.getSOAPPart()));
		SOAPBody soapBody = response.getSOAPBody();
		Iterator<MessageElement> itr = soapBody.getChildElements();
		MessageElement ME = itr.next();
		Document responseDoc = ME.getAsDocument();

		if (log.isVerboseEnabled()) {
			log.verbose("invokeFedexExpPickUpAvlWebservice: SOAP response"
					+ responseDoc);
		}

		return responseDoc;
	}

	private SOAPMessage getSOAPMessageForFedexPickupAvalReq(Document inDoc)
			throws Exception {

		try {
			Document document = AcademyServiceUtil
					.getSOAPMessageTemplateForFedexPickupAvalReq();

			document
					.getElementsByTagName("v5:Key")
					.item(0)
					.setTextContent(
							YFSSystem
									.getProperty("academy.fedex.shipment.pickup.request.key"));
			document
					.getElementsByTagName("v5:Password")
					.item(0)
					.setTextContent(
							YFSSystem
									.getProperty("academy.fedex.shipment.pickup.request.password"));
			document
					.getElementsByTagName("v5:AccountNumber")
					.item(0)
					.setTextContent(
							YFSSystem
									.getProperty("academy.fedex.shipment.pickup.request.accountNumber"));
			document
					.getElementsByTagName("v5:MeterNumber")
					.item(0)
					.setTextContent(
							YFSSystem
									.getProperty("academy.fedex.shipment.pickup.request.meterNumber"));

			Element eleShipNodePersonInfo = (Element) XPathUtil.getNode(inDoc
					.getDocumentElement(),
					"Shipment/ShipNode/ShipNodePersonInfo");

			document.getElementsByTagName("v5:StreetLines").item(0)
					.setTextContent(
							eleShipNodePersonInfo.getAttribute("AddressLine1"));
			document.getElementsByTagName("v5:City").item(0).setTextContent(
					eleShipNodePersonInfo.getAttribute("City"));
			document
					.getElementsByTagName("v5:StateOrProvinceCode")
					.item(0)
					.setTextContent(eleShipNodePersonInfo.getAttribute("State"));
			document.getElementsByTagName("v5:PostalCode").item(0)
					.setTextContent(
							eleShipNodePersonInfo.getAttribute("ZipCode"));
			document.getElementsByTagName("v5:CountryCode").item(0)
					.setTextContent(
							eleShipNodePersonInfo.getAttribute("Country"));

			document.getElementsByTagName("v5:PickupRequestType").item(0)
					.setTextContent(AcademyConstants.FDX_PICK_REQ_TYPE);
			document
					.getElementsByTagName("v5:DispatchDate")
					.item(0)
					.setTextContent(
							inDoc
									.getDocumentElement()
									.getAttribute(
											AcademyConstants.FDX_PICK_REQ_DISPATCH_DATE));
			document.getElementsByTagName("v5:NumberOfBusinessDays").item(0)
					.setTextContent(
							AcademyConstants.FDX_PICK_REQ_NO_OF_BUSINESS_DAYS);
			document.getElementsByTagName("v5:Carriers").item(0)
					.setTextContent(
							AcademyConstants.FDX_PICK_REQ_EXPRESS_CARRIER);

			if (log.isVerboseEnabled()) {
				log.verbose("invokeFedexExpPickUpAvlWebservice: SOAP Request"
						+ XMLUtil.getXMLString(document));
			}

			MessageFactory messageFactory = MessageFactory.newInstance();
			SOAPMessage soapMessage = messageFactory.createMessage();
			DOMSource domSource = new DOMSource(document);
			SOAPPart soapPart = soapMessage.getSOAPPart();
			soapPart.setContent(domSource);
			return soapMessage;
		} catch (SOAPException e) {
			throw e;
		}
	}
}
