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
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademySFSFedexExpressCreatePickupWebservice {

	private Properties props;

	/**
	 * Instance of logger
	 */
	private static YFCLogCategory log = YFCLogCategory
	.instance(AcademySFSFedexExpressCreatePickupWebservice.class);

	public void setProperties(Properties props) {
		this.props = props;
	}

	public Document invokeFedexExpCreatePickUpReqWebservice(YFSEnvironment env,
			Document inDoc) throws Exception {

		if (log.isVerboseEnabled()) {
			log.verbose("invokeFedexExpCreatePickUpReqWebservice: START");
			log.verbose("invokeFedexExpCreatePickUpReqWebservice: Input Doc"
					+ XMLUtil.getXMLString(inDoc));
		}

		SOAPConnection connection = AcademyServiceUtil.getSOAPConnection();

		String strEndpointURL = YFSSystem
		.getProperty("academy.fedex.shipment.pickup.request.endpoint");
		if (log.isVerboseEnabled()) {
			log
			.verbose("invokeFedexExpCreatePickUpReqWebservice: strEndpointURL"
					+ strEndpointURL);
		}
		URL endpoint = new URL(strEndpointURL);

		Document document = getSOAPMessageForFedexCreatePickupReq(env, inDoc);

		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage request = messageFactory.createMessage();
		DOMSource domSource = new DOMSource(document);
		SOAPPart soapPart = request.getSOAPPart();
		soapPart.setContent(domSource);

		SOAPMessage response = connection.call(request, endpoint);

		log.verbose("invokeFedexExpCreatePickUpReqWebservice: SOAP response"
				+ XMLUtil.getXMLString(response.getSOAPPart()));

		SOAPBody soapBody = response.getSOAPBody();
		Iterator<MessageElement> itr = soapBody.getChildElements();
		MessageElement ME = itr.next();
		Document responseDoc = ME.getAsDocument();

		if (AcademyConstants.STR_YES
				.equalsIgnoreCase(props.getProperty(AcademyConstants.STR_ENABLE_EXPORT_REQ_AND_RES))) {

			Document mergedDoc = mergeRequestAndResponse(document, responseDoc);

			// Add the merged Request and Response to YFS_EXPORT table for reference
			addtoYFSExportTable(env, mergedDoc);
		}

		if (log.isVerboseEnabled()) {
			log.verbose("invokeFedexExpCreatePickUpReqWebservice: SOAP response" + responseDoc);
		}

		if (AcademyConstants.STR_YES
				.equalsIgnoreCase(props.getProperty(AcademyConstants.STR_ENABLE_INFO_LOG_REQ_AND_RES))) {

			log.info("FedEx Create Pickup Request and Response :: Date - "
					+ inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_CURRENT_DATE) + " : ShipNode - "
					+ inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIP_NODE) + " : ShipmentNo - "
					+ inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO) + " : Request - "
					+ XMLUtil.getXMLString(document) + " : Response - " + XMLUtil.getXMLString(responseDoc));
		}

		return responseDoc;
	}

	private void addtoYFSExportTable(YFSEnvironment env, Document mergedDoc)
	throws Exception {

		Document inputDoc = YFCDocument.createDocument("CreateExportDataEx")
		.getDocument();
		Element eleInputDoc = inputDoc.getDocumentElement();
		eleInputDoc.setAttribute("EnterpriseCode",
				AcademyConstants.PRIMARY_ENTERPRISE);
		eleInputDoc.setAttribute("FlowName",
		"AcademySFSInvokeFedexExpressCreatePickupReqWebservice");
		eleInputDoc.setAttribute("SubFlowName",
		"AcademySFSInvokeFedexExpressCreatePickupReqWebservice");

		Element eleExportData = inputDoc.createElement("XmlExportData");
		eleExportData.setTextContent(XMLUtil.getXMLString(mergedDoc));
		eleInputDoc.appendChild(eleExportData);

		if (log.isVerboseEnabled()) {
			log.verbose("addtoYFSExportTable: Input"
					+ XMLUtil.getXMLString(inputDoc));
		}

		// Calling the API to put the data in the yfs_export table
		AcademyUtil.invokeAPI(env, "createExportDataEx", inputDoc);

	}

	private Document mergeRequestAndResponse(Document document,
			Document responseDoc) throws Exception {

		YFCDocument yfcInDoc = YFCDocument.getDocumentFor(document);
		YFCDocument mergeDoc = YFCDocument
		.createDocument(AcademyConstants.TAG_MERGE_ROOT_DOC);
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
		YFCElement wrapperElementRes = rootElement
		.createChild("ResponseDocument");
		if (envDoc != null) {
			// Lets operate on env's clone.
			YFCDocument cloneInDocRes = XMLUtil.cloneDocument(envDoc);
			YFCElement yfceRes = cloneInDocRes.getDocumentElement();
			cloneInDocRes.removeChild(yfceRes);
			wrapperElementRes.importNode(yfceRes);
		}

		if (log.isVerboseEnabled()) {
			log
			.verbose("invokeFedexExpCreatePickUpReqWebservice: mergeRequestAndResponse Output"
					+ XMLUtil.getXMLString(mergeDoc.getDocument()));
		}

		return mergeDoc.getDocument();

	}

	private Document getSOAPMessageForFedexCreatePickupReq(YFSEnvironment env,
			Document inDoc) throws Exception {

		Document document = AcademyServiceUtil
		.getSOAPMessageTemplateForFedexCreatePickupReq();

		Element eleRoot = inDoc.getDocumentElement();
		//Start changes as part of STL-1567
		/*Element eleShipNodePersonInfo = (Element) XPathUtil.getNode(eleRoot,
				"Shipment/ShipNode/ShipNodePersonInfo");*/
		Element eleShipNodePersonInfo = (Element) XPathUtil.getNode(eleRoot,"ShipNode/ShipNodePersonInfo");
		String strContainerWeightUnit = props.getProperty(AcademyConstants.ATTR_CONT_WEIGHT_UNIT);
		String strContainerWeight = props.getProperty(AcademyConstants.ATTR_CONT_WEIGHT);
		String strFedxPickReqCompanyCloseTime = props.getProperty(AcademyConstants.ATTR_FEDX_PICK_REQ_COMP_CLOSE_TIME);
		//End changes as part of STL-1567
		document.getElementsByTagName("v5:Key").item(0).setTextContent(YFSSystem.getProperty("academy.fedex.shipment.pickup.request.key"));
		document.getElementsByTagName("v5:Password").item(0).setTextContent(YFSSystem.getProperty("academy.fedex.shipment.pickup.request.password"));
		document.getElementsByTagName("v5:AccountNumber").item(0).setTextContent(YFSSystem.getProperty("academy.fedex.shipment.pickup.request.accountNumber"));
		document.getElementsByTagName("v5:MeterNumber").item(0).setTextContent(YFSSystem.getProperty("academy.fedex.shipment.pickup.request.meterNumber"));
		//Start changes as part of STL-1567
		/*document.getElementsByTagName("v5:CustomerTransactionId").item(0)
				.setTextContent(
						"Container No::" + eleRoot.getAttribute("ContainerNo"));*/
		document.getElementsByTagName("v5:CustomerTransactionId").item(0).setTextContent("Shipment No::" + eleRoot.getAttribute("ShipmentNo"));
		//End changes as part of STL-1567

		document.getElementsByTagName("v5:CompanyName").item(0).setTextContent("Academy " + eleShipNodePersonInfo.getAttribute("FirstName"));
		document.getElementsByTagName("v5:StreetLines").item(0).setTextContent(eleShipNodePersonInfo.getAttribute("AddressLine1"));
		document.getElementsByTagName("v5:City").item(0).setTextContent(eleShipNodePersonInfo.getAttribute("City"));
		document.getElementsByTagName("v5:StateOrProvinceCode").item(0).setTextContent(eleShipNodePersonInfo.getAttribute("State"));
		document.getElementsByTagName("v5:PostalCode").item(0).setTextContent(eleShipNodePersonInfo.getAttribute("ZipCode"));
		document.getElementsByTagName("v5:CountryCode").item(0).setTextContent(eleShipNodePersonInfo.getAttribute("Country"));

		document.getElementsByTagName("v5:ReadyTimestamp").item(0).setTextContent(eleRoot.getAttribute(AcademyConstants.FDX_CREATE_PICK_REQ_READYTIMESTAMP));
		document.getElementsByTagName("v5:CompanyCloseTime").item(0).setTextContent(strFedxPickReqCompanyCloseTime);

		document.getElementsByTagName("v5:PackageCount").item(0).setTextContent("1");
		//Start changes as part of STL-1567
		/*String strUOM = eleRoot.getAttribute("ContainerGrossWeightUOM");
		if ("LBS".equalsIgnoreCase(strUOM)) {
			strUOM = "LB";
		}
		document.getElementsByTagName("v5:Units").item(0).setTextContent(strUOM);
		document.getElementsByTagName("v5:Value").item(0).setTextContent(eleRoot.getAttribute("ContainerGrossWeight"));
		 */
		document.getElementsByTagName("v5:Units").item(0).setTextContent(strContainerWeightUnit);
		document.getElementsByTagName("v5:Value").item(0).setTextContent(strContainerWeight);
		//End changes as part of STL-1567		
		document.getElementsByTagName("v5:CarrierCode").item(0).setTextContent(AcademyConstants.FDX_PICK_REQ_EXPRESS_CARRIER);

		if (log.isVerboseEnabled()) {
			log.verbose("invokeFedexExpCreatePickUpReqWebservice: SOAP Request"
					+ XMLUtil.getXMLString(document));
		}

		return document;

	}
}
