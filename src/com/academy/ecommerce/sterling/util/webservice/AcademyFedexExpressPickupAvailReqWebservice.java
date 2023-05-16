package com.academy.ecommerce.sterling.util.webservice;

import java.net.URL;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.dom.DOMSource;

import org.apache.axis.message.MessageElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.util.AcademyServiceUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.ibm.icu.impl.PropsVectors;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyFedexExpressPickupAvailReqWebservice implements YIFCustomApi {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyFedexExpressPickupAvailReqWebservice.class);
	private Properties props;

	@Override
	public void setProperties(Properties props) throws Exception {
		this.props = props;

	}

	public Document invokeFedexExpressPickupAvailReq(YFSEnvironment envObj, Document docInp) {

		Document responseDoc = null;

		try {

			log.verbose("invokeFedexExpressPickupAvailReq: START");
			log.verbose("invokeFedexExpressPickupAvailReq: Input Doc" + XMLUtil.getXMLString(docInp));

			SOAPConnection connection = AcademyServiceUtil.getSOAPConnection();

			String strEndpointURL = YFSSystem.getProperty("academy.fedex.shipment.pickup.request.endpoint");

			log.verbose("invokeFedexExpressPickupAvailReq: strEndpointURL" + strEndpointURL);

			URL endpoint = new URL(strEndpointURL);

			Document document = getSOAPMessageForFedexPickupAvailReq(envObj, docInp);

			MessageFactory messageFactory = MessageFactory.newInstance();
			SOAPMessage request = messageFactory.createMessage();
			DOMSource domSource = new DOMSource(document);
			SOAPPart soapPart = request.getSOAPPart();
			soapPart.setContent(domSource);

			SOAPMessage response = connection.call(request, endpoint);

			log.verbose(
					"invokeFedexExpressPickupAvailReq: SOAP response" + XMLUtil.getXMLString(response.getSOAPPart()));

			SOAPBody soapBody = response.getSOAPBody();
			Iterator<MessageElement> itr = soapBody.getChildElements();
			MessageElement ME = itr.next();
			responseDoc = ME.getAsDocument();

			if (AcademyConstants.STR_YES
					.equalsIgnoreCase(props.getProperty(AcademyConstants.STR_ENABLE_EXPORT_REQ_AND_RES))) {

				Document mergedDoc = mergeRequestAndResponse(document, responseDoc);

				// Add the merged Request and Response to YFS_EXPORT table for reference
				addtoYFSExportTable(envObj, mergedDoc);
			}

			if (AcademyConstants.STR_YES
					.equalsIgnoreCase(props.getProperty(AcademyConstants.STR_ENABLE_INFO_LOG_REQ_AND_RES))) {

				log.info("FedEx Pickup Availability Request and Response :: Date - "
						+ docInp.getDocumentElement().getAttribute(AcademyConstants.ATTR_CURRENT_DATE)
						+ " : ShipNode - " + docInp.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE)
						+ " : ShipmentNo - "
						+ docInp.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO) + " : Request - "
						+ XMLUtil.getXMLString(document) + " : Response - " + XMLUtil.getXMLString(responseDoc));
			}

			log.verbose("invokeFedexExpressPickupAvailReq: SOAP response" + responseDoc);

		} catch (Exception e) {
			log.error("Exception - invokeFedexExpressPickupAvailReq() :: " + e);
		}

		return responseDoc;

	}

	private void addtoYFSExportTable(YFSEnvironment env, Document mergedDoc) throws Exception {

		Document inputDoc = YFCDocument.createDocument("CreateExportDataEx").getDocument();
		Element eleInputDoc = inputDoc.getDocumentElement();
		eleInputDoc.setAttribute("EnterpriseCode", AcademyConstants.PRIMARY_ENTERPRISE);
		eleInputDoc.setAttribute("FlowName", "AcademyFedexExpressPickupAvailReq");
		eleInputDoc.setAttribute("SubFlowName", "AcademyFedexExpressPickupAvailReq");

		Element eleExportData = inputDoc.createElement("XmlExportData");
		eleExportData.setTextContent(XMLUtil.getXMLString(mergedDoc));
		eleInputDoc.appendChild(eleExportData);

		if (log.isVerboseEnabled()) {
			log.verbose("addtoYFSExportTable: Input" + XMLUtil.getXMLString(inputDoc));
		}

		// Calling the API to put the data in the yfs_export table
		AcademyUtil.invokeAPI(env, "createExportDataEx", inputDoc);

	}

	private Document mergeRequestAndResponse(Document document, Document responseDoc) throws Exception {

		YFCDocument yfcInDoc = YFCDocument.getDocumentFor(document);
		YFCDocument mergeDoc = YFCDocument.createDocument(AcademyConstants.TAG_MERGE_ROOT_DOC);
		YFCElement rootElement = mergeDoc.getDocumentElement();
		YFCElement wrapperElement = rootElement.createChild("RequestDocument");
		// Insert input document as first child.
		// Now, to import, we have to remove the element from the original
		// Document
		// and then insert into new parent. Since we dont want to disturb the
		// original
		// document, lets operate on its clone.
		YFCDocument cloneInDoc = XMLUtil.cloneDocument(yfcInDoc);
		YFCElement yfce = cloneInDoc.getDocumentElement();
		cloneInDoc.removeChild(yfce);
		wrapperElement.importNode(yfce);

		YFCDocument envDoc = YFCDocument.getDocumentFor(responseDoc);
		YFCElement wrapperElementRes = rootElement.createChild("ResponseDocument");
		if (envDoc != null) {
			// Lets operate on env's clone.
			YFCDocument cloneInDocRes = XMLUtil.cloneDocument(envDoc);
			YFCElement yfceRes = cloneInDocRes.getDocumentElement();
			cloneInDocRes.removeChild(yfceRes);
			wrapperElementRes.importNode(yfceRes);
		}

		if (log.isVerboseEnabled()) {
			log.verbose("invokeFedexExpressPickupAvailReq: mergeRequestAndResponse Output"
					+ XMLUtil.getXMLString(mergeDoc.getDocument()));
		}

		return mergeDoc.getDocument();

	}

	private Document getSOAPMessageForFedexPickupAvailReq(YFSEnvironment env, Document inDoc) throws Exception {

		Document document = AcademyServiceUtil.getSOAPMessageTemplateForFedexExpressPickupAvailRequest();

		Element eleRoot = inDoc.getDocumentElement();

		Element eleShipNodePersonInfo = (Element) XPathUtil.getNode(eleRoot, "ShipNode/ShipNodePersonInfo");

		// End changes as part of STL-1567
		document.getElementsByTagName("q0:Key").item(0)
				.setTextContent(YFSSystem.getProperty("academy.fedex.shipment.pickup.request.key"));
		document.getElementsByTagName("q0:Password").item(0)
				.setTextContent(YFSSystem.getProperty("academy.fedex.shipment.pickup.request.password"));
		document.getElementsByTagName("q0:AccountNumber").item(0)
				.setTextContent(YFSSystem.getProperty("academy.fedex.shipment.pickup.request.accountNumber"));
		document.getElementsByTagName("q0:MeterNumber").item(0)
				.setTextContent(YFSSystem.getProperty("academy.fedex.shipment.pickup.request.meterNumber"));
		document.getElementsByTagName("q0:CustomerTransactionId").item(0)
				.setTextContent("Shipment No::" + eleRoot.getAttribute("ShipmentNo"));

		document.getElementsByTagName("q0:StreetLines").item(0)
				.setTextContent(eleShipNodePersonInfo.getAttribute("AddressLine1"));
		document.getElementsByTagName("q0:City").item(0).setTextContent(eleShipNodePersonInfo.getAttribute("City"));
		document.getElementsByTagName("q0:StateOrProvinceCode").item(0)
				.setTextContent(eleShipNodePersonInfo.getAttribute("State"));
		document.getElementsByTagName("q0:PostalCode").item(0)
				.setTextContent(eleShipNodePersonInfo.getAttribute("ZipCode"));
		document.getElementsByTagName("q0:CountryCode").item(0)
				.setTextContent(eleShipNodePersonInfo.getAttribute("Country"));

		document.getElementsByTagName("q0:PickupRequestType").item(0)
				.setTextContent(AcademyConstants.FDX_PICK_REQ_TYPE);

		document.getElementsByTagName("q0:DispatchDate").item(0)
				.setTextContent(eleRoot.getAttribute(AcademyConstants.FDX_PICK_REQ_DISPATCH_DATE));

		document.getElementsByTagName("q0:Carriers").item(0)
				.setTextContent(AcademyConstants.FDX_PICK_REQ_EXPRESS_CARRIER);

		document.getElementsByTagName("q0:PickupType").item(0).setTextContent(AcademyConstants.STR_ON_CALL);

		log.verbose("getSOAPMessageForFedexPickupAvailReq: SOAP Request" + XMLUtil.getXMLString(document));

		return document;

	}
	//
}
