package com.academy.ecommerce.sterling.util.webservice;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.academy.ecommerce.sterling.util.AcademyServiceUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyAgentAddressSOAPCall implements YIFCustomApi {
	private Properties props;

	private Document document = null;

	/**
	 * 
	 * Instance of logger
	 * 
	 */
	private static YFCLogCategory log = YFCLogCategory
			.instance(AcademyAgentAddressSOAPCall.class);

	public void setProperties(Properties props) {
		this.props = props;

	}

	public Document agentAddressSOAPCall(YFSEnvironment env, Document inDoc)
			throws Exception {
		log.verbose("*** invoking agentAddressSOAPCall with input doc *** "
				+ XMLUtil.getXMLString(inDoc));

		/*
		 * Invoke the agile service by setting the Txn Object from the component
		 * invoking this utility.The below method is called on shipment confirm
		 * to get the agent address location
		 */
		if ((env.getTxnObject("AgileMethodName").toString())
				.equals("InvokeAgileWebserviceForShipment")) {

			document = AcademyServiceUtil
					.getSOAPMessageTemplateForAgentAddressLocation();
			Element eleRoot = document.getDocumentElement();
			Element elePierbridgeRateRequestdocument = (Element) XMLUtil
					.getElementsByTagName(eleRoot, "PierbridgeRateRequest")
					.get(0);
			if (!YFCObject.isVoid(elePierbridgeRateRequestdocument)) {

				setDefaultProperties(inDoc, elePierbridgeRateRequestdocument);

				/* Prepare the Sender input */
				Element eleSender = (Element) XMLUtil.getElementsByTagName(
						elePierbridgeRateRequestdocument, "Sender").get(0);
				if (!YFCObject.isVoid(eleSender)) {
					prepareSenderInformationForShipment(inDoc, eleSender);
				}
				/* Prepare the Receiver input */
				Element eleReceiver = (Element) XMLUtil.getElementsByTagName(
						elePierbridgeRateRequestdocument, "Receiver").get(0);
				if (!YFCObject.isVoid(eleReceiver)) {

					prepareReceiverInformationForShipment(inDoc, eleReceiver);
				}

			}
		}

		/*
		 * Invoke the agile service by setting the Txn Object from the component
		 * invoking this utility.The below method is called on shipment confirm
		 * to get the agent address location
		 */
		else {
			log.verbose("Inside the ELSE BLOCK - white Glove Returns  BOL ");
			document = AcademyServiceUtil
					.getSOAPMessageTemplateForAgentAddressLocation();
			Element eleRoot = document.getDocumentElement();
			Element elePierbridgeRateRequestdocument = (Element) XMLUtil
					.getElementsByTagName(eleRoot, "PierbridgeRateRequest")
					.get(0);
			if (!YFCObject.isVoid(elePierbridgeRateRequestdocument)) {
				setDefaultProperties(inDoc, elePierbridgeRateRequestdocument);

				/* Prepare the Sender input */
				Element eleSender = (Element) XMLUtil.getElementsByTagName(
						elePierbridgeRateRequestdocument, "Sender").get(0);
				if (!YFCObject.isVoid(eleSender)) {
					// Modified the code for bug # 4299
					prepareSenderInformationForWGReturn(inDoc, eleSender,env);
				}
				/* Prepare the Receiver input */
				Element eleReceiver = (Element) XMLUtil.getElementsByTagName(
						elePierbridgeRateRequestdocument, "Receiver").get(0);
				if (!YFCObject.isVoid(eleReceiver)) {
					// Modified the code for bug # 4299
					prepareReceiverInformationForWGReturn(inDoc, eleReceiver);
				}
			}

		}
		return document;
	}

	private void prepareReceiverInformationForReturn(Document inDoc,
			Element eleReceiver,YFSEnvironment env) throws Exception {
		Element personInfoEle=(Element)inDoc.getElementsByTagName("PersonInfo").item(0);
		if (personInfoEle!=null)
		{
			Element eleCompany = (Element) XMLUtil.getElementsByTagName(
					eleReceiver, "CompanyName").get(0);
			eleCompany.setTextContent(personInfoEle.getAttribute("Company"));

			Element eleStreet = (Element) XMLUtil.getElementsByTagName(eleReceiver,
					"Street").get(0);
			eleStreet.setTextContent(personInfoEle.getAttribute("AddressLine1"));

			Element eleCity = (Element) XMLUtil.getElementsByTagName(eleReceiver,
					"City").get(0);
			eleCity.setTextContent(personInfoEle.getAttribute("City"));

			Element eleRegion = (Element) XMLUtil.getElementsByTagName(eleReceiver,
					"Region").get(0);
			eleRegion.setTextContent(personInfoEle.getAttribute("State"));

			Element elePostalCode = (Element) XMLUtil.getElementsByTagName(
					eleReceiver, "PostalCode").get(0);
			elePostalCode.setTextContent(personInfoEle.getAttribute("ZipCode"));

			Element eleCountry = (Element) XMLUtil.getElementsByTagName(
					eleReceiver, "Country").get(0);
			eleCountry.setTextContent(personInfoEle.getAttribute("Country"));
		}
		else
		{
			//call getOrgHeirarchy to get the Return ship Node address details.
			log.verbose("Calling out getOrganizationHierarchy - in case of it doesn't get additional cls address");
			// Api call to get the CLS address details:
			Document getCLSNodeDoc = XMLUtil.createDocument("Organization");
			getCLSNodeDoc.getDocumentElement().setAttribute(AcademyConstants.ORGANIZATION_CODE,AcademyConstants.STR_CLS_RECVING_NODE);
			
			env.setApiTemplate("getOrganizationHierarchy", "global/template/api/getOrganizationHierarchy.createReturnOrderUEImpl.xml");
						Document clsNodeDocDetails = AcademyUtil.invokeAPI(env,"getOrganizationHierarchy", getCLSNodeDoc);
			Element eleToContactAddress = (Element) clsNodeDocDetails.getDocumentElement().getElementsByTagName("ContactPersonInfo").item(0);
				
			Element eleCompany = (Element) XMLUtil.getElementsByTagName(
					eleReceiver, "CompanyName").get(0);
			eleCompany.setTextContent(eleToContactAddress.getAttribute("Company"));

			Element eleStreet = (Element) XMLUtil.getElementsByTagName(eleReceiver,
					"Street").get(0);
			eleStreet.setTextContent(eleToContactAddress.getAttribute("AddressLine1"));

			Element eleCity = (Element) XMLUtil.getElementsByTagName(eleReceiver,
					"City").get(0);
			eleCity.setTextContent(eleToContactAddress.getAttribute("City"));

			Element eleRegion = (Element) XMLUtil.getElementsByTagName(eleReceiver,
					"Region").get(0);
			eleRegion.setTextContent(eleToContactAddress.getAttribute("State"));

			Element elePostalCode = (Element) XMLUtil.getElementsByTagName(
					eleReceiver, "PostalCode").get(0);
			elePostalCode.setTextContent(eleToContactAddress.getAttribute("ZipCode"));

			Element eleCountry = (Element) XMLUtil.getElementsByTagName(
					eleReceiver, "Country").get(0);
			eleCountry.setTextContent(eleToContactAddress.getAttribute("Country"));
			
		}
			
		/*Element eleCompany = (Element) XMLUtil.getElementsByTagName(
				eleReceiver, "CompanyName").get(0);
		eleCompany.setTextContent(XMLUtil.getString(inDoc,
				"Order/PersonInfoShipTo/@Company"));

		Element eleStreet = (Element) XMLUtil.getElementsByTagName(eleReceiver,
				"Street").get(0);
		eleStreet.setTextContent(XMLUtil.getString(inDoc,
				"Order/PersonInfoShipTo/@AddressLine1"));

		Element eleCity = (Element) XMLUtil.getElementsByTagName(eleReceiver,
				"City").get(0);
		eleCity.setTextContent(XMLUtil.getString(inDoc,
				"Order/PersonInfoShipTo/@City"));

		Element eleRegion = (Element) XMLUtil.getElementsByTagName(eleReceiver,
				"Region").get(0);
		eleRegion.setTextContent(XMLUtil.getString(inDoc,
				"Order/PersonInfoShipTo/@State"));

		Element elePostalCode = (Element) XMLUtil.getElementsByTagName(
				eleReceiver, "PostalCode").get(0);
		elePostalCode.setTextContent(XMLUtil.getString(inDoc,
				"Order/PersonInfoShipTo/@ZipCode"));

		Element eleCountry = (Element) XMLUtil.getElementsByTagName(
				eleReceiver, "Country").get(0);
		eleCountry.setTextContent(XMLUtil.getString(inDoc,
				"Order/PersonInfoShipTo/@Country"));*/

	}

	private void prepareSenderInformationForReturn(Document inDoc,
			Element eleSender) throws Exception {
		Element eleSentBy = (Element) XMLUtil.getElementsByTagName(eleSender,
				"SentBy").get(0);
		Element eleCity = (Element) XMLUtil.getElementsByTagName(eleSender,
				"City").get(0);
		Element eleRegion = (Element) XMLUtil.getElementsByTagName(eleSender,
				"Region").get(0);
		Element elePostalCode = (Element) XMLUtil.getElementsByTagName(
				eleSender, "PostalCode").get(0);
		Element eleCountry = (Element) XMLUtil.getElementsByTagName(eleSender,
				"Country").get(0);

		eleSentBy.setTextContent(XMLUtil.getString(inDoc,
				"Order/PersonInfoBillTo/@AddressLine1"));
		eleCity.setTextContent(XMLUtil.getString(inDoc,
				"Order/PersonInfoBillTo/@City"));
		eleRegion.setTextContent(XMLUtil.getString(inDoc,
				"Order/PersonInfoBillTo/@State"));
		elePostalCode.setTextContent(XMLUtil.getString(inDoc,
				"Order/PersonInfoBillTo/@ZipCode"));
		eleCountry.setTextContent(XMLUtil.getString(inDoc,
				"Order/PersonInfoBillTo/@Country"));

	}

	private void prepareReceiverInformationForShipment(Document inDoc,
			Element eleReceiver) throws Exception {
		Element eleCompany = (Element) XMLUtil.getElementsByTagName(
				eleReceiver, "CompanyName").get(0);
		eleCompany.setTextContent(XMLUtil.getString(inDoc,
				"Shipment/ToAddress/@Company"));

		Element eleStreet = (Element) XMLUtil.getElementsByTagName(eleReceiver,
				"Street").get(0);
		eleStreet.setTextContent(XMLUtil.getString(inDoc,
				"Shipment/ToAddress/@AddressLine1"));

		Element eleCity = (Element) XMLUtil.getElementsByTagName(eleReceiver,
				"City").get(0);
		eleCity.setTextContent(XMLUtil.getString(inDoc,
				"Shipment/ToAddress/@City"));

		Element eleRegion = (Element) XMLUtil.getElementsByTagName(eleReceiver,
				"Region").get(0);
		eleRegion.setTextContent(XMLUtil.getString(inDoc,
				"Shipment/ToAddress/@State"));

		Element elePostalCode = (Element) XMLUtil.getElementsByTagName(
				eleReceiver, "PostalCode").get(0);
		elePostalCode.setTextContent(XMLUtil.getString(inDoc,
				"Shipment/ToAddress/@ZipCode"));

		Element eleCountry = (Element) XMLUtil.getElementsByTagName(
				eleReceiver, "Country").get(0);
		eleCountry.setTextContent(XMLUtil.getString(inDoc,
				"Shipment/ToAddress/@Country"));
	}

	private void prepareSenderInformationForShipment(Document inDoc,
			Element eleSender) throws Exception {
		Element eleSentBy = (Element) XMLUtil.getElementsByTagName(eleSender,
				"SentBy").get(0);
		Element eleCity = (Element) XMLUtil.getElementsByTagName(eleSender,
				"City").get(0);
		Element eleRegion = (Element) XMLUtil.getElementsByTagName(eleSender,
				"Region").get(0);
		Element elePostalCode = (Element) XMLUtil.getElementsByTagName(
				eleSender, "PostalCode").get(0);
		Element eleCountry = (Element) XMLUtil.getElementsByTagName(eleSender,
				"Country").get(0);

		eleSentBy.setTextContent(XMLUtil.getString(inDoc,
				"Shipment/FromAddress/@AddressLine1"));
		eleCity.setTextContent(XMLUtil.getString(inDoc,
				"Shipment/FromAddress/@City"));
		eleRegion.setTextContent(XMLUtil.getString(inDoc,
				"Shipment/FromAddress/@State"));
		elePostalCode.setTextContent(XMLUtil.getString(inDoc,
				"Shipment/FromAddress/@ZipCode"));
		eleCountry.setTextContent(XMLUtil.getString(inDoc,
				"Shipment/FromAddress/@Country"));
	}

	private void setDefaultProperties(Document inDoc,
			Element elePierbridgeRateRequestdocument) {
		String strCarrier = inDoc.getDocumentElement().getAttribute("SCAC");
		String strCarrierCode = getCarrierCode(inDoc, strCarrier);
		Element eleCarrier = (Element) XMLUtil.getElementsByTagName(
				elePierbridgeRateRequestdocument, "Carrier").get(0);
		eleCarrier.setTextContent(strCarrierCode);

		Element eleUserName = (Element) XMLUtil.getElementsByTagName(
				elePierbridgeRateRequestdocument, "UserName").get(0);
		eleUserName.setTextContent(props.getProperty("UserName"));

		Element eleServiceType = (Element) XMLUtil.getElementsByTagName(
				elePierbridgeRateRequestdocument, "ServiceType").get(0);
		eleServiceType.setTextContent(props.getProperty("ServiceType"));
		Element elePackagesTypes = (Element) XMLUtil.getElementsByTagName(
				elePierbridgeRateRequestdocument, "Packages").get(0);
		Element elePackage = (Element) XMLUtil.getElementsByTagName(
				elePackagesTypes, "Package").get(0);
		Element elePackagesType = (Element) XMLUtil.getElementsByTagName(
				elePackage, "PackageType").get(0);
		Element eleWeight = (Element) XMLUtil.getElementsByTagName(elePackage,
				"Weight").get(0);
		Element eleFreightClass = (Element) XMLUtil.getElementsByTagName(elePackage,
				"FreightClass").get(0);
		elePackagesType.setTextContent(props.getProperty("PackageType"));
		eleWeight.setTextContent(props.getProperty("Weight"));
		eleFreightClass.setTextContent(props.getProperty("FreightClass"));
		
	}

	/*
	 * This method gets the carrier code for Agile by checking the SCAC on
	 * shipment created at Sterling
	 */
	private String getCarrierCode(Document inDoc, String strCarrier) {
		String strCarrierCode = null;
		Map<String, String> hm = new HashMap<String, String>();
		hm.put("USPS", "5");
		hm.put("UPS", "12");
		hm.put("EXEL", "1000");
		strCarrierCode = hm.get(strCarrier);
		return strCarrierCode;
	}
	// START : Modified the code for bug # 4299
	private void prepareSenderInformationForWGReturn(Document inDoc, Element eleSender,YFSEnvironment env) throws Exception{
		Element eleSentBy = (Element) XMLUtil.getElementsByTagName(eleSender,"SentBy").get(0);
		Element eleCity = (Element) XMLUtil.getElementsByTagName(eleSender,	"City").get(0);
		Element eleRegion = (Element) XMLUtil.getElementsByTagName(eleSender,	"Region").get(0);
		Element elePostalCode = (Element) XMLUtil.getElementsByTagName(eleSender, "PostalCode").get(0);
		Element eleCountry = (Element) XMLUtil.getElementsByTagName(eleSender,"Country").get(0);
		Element personInfoEle=(Element)inDoc.getElementsByTagName("PersonInfo").item(0);
		if (personInfoEle!=null)
		{
			eleSentBy.setTextContent(personInfoEle.getAttribute("AddressLine1"));
			eleCity.setTextContent(personInfoEle.getAttribute("City"));
			eleRegion.setTextContent(personInfoEle.getAttribute("State"));
			elePostalCode.setTextContent(personInfoEle.getAttribute("ZipCode"));
			eleCountry.setTextContent(personInfoEle.getAttribute("Country"));		
		}else{
			//call getOrgHeirarchy to get the Return ship Node address details.
			log.verbose("Calling out getOrganizationHierarchy - in case of it doesn't get additional cls address");
			// Api call to get the CLS address details:
			Document getCLSNodeDoc = XMLUtil.createDocument("Organization");
			getCLSNodeDoc.getDocumentElement().setAttribute(AcademyConstants.ORGANIZATION_CODE,AcademyConstants.STR_CLS_RECVING_NODE);
			
			env.setApiTemplate("getOrganizationHierarchy", "global/template/api/getOrganizationHierarchy.createReturnOrderUEImpl.xml");
						Document clsNodeDocDetails = AcademyUtil.invokeAPI(env,"getOrganizationHierarchy", getCLSNodeDoc);
			Element eleToContactAddress = (Element) clsNodeDocDetails.getDocumentElement().getElementsByTagName("ContactPersonInfo").item(0);
			eleSentBy.setTextContent(eleToContactAddress.getAttribute("AddressLine1"));
			eleCity.setTextContent(eleToContactAddress.getAttribute("City"));
			eleRegion.setTextContent(eleToContactAddress.getAttribute("State"));
			elePostalCode.setTextContent(eleToContactAddress.getAttribute("ZipCode"));
			eleCountry.setTextContent(eleToContactAddress.getAttribute("Country"));
		}
	}
	
	private void prepareReceiverInformationForWGReturn(Document inDoc, Element eleReceiver) throws Exception{
		Element eleCompany = (Element) XMLUtil.getElementsByTagName(eleReceiver, "CompanyName").get(0);
		eleCompany.setTextContent(XMLUtil.getString(inDoc,"Order/PersonInfoBillTo/@Company"));
		
		Element eleStreet = (Element) XMLUtil.getElementsByTagName(eleReceiver,"Street").get(0);
		eleStreet.setTextContent(XMLUtil.getString(inDoc,"Order/PersonInfoBillTo/@AddressLine1"));
		
		Element eleCity = (Element) XMLUtil.getElementsByTagName(eleReceiver,"City").get(0);
		eleCity.setTextContent(XMLUtil.getString(inDoc,"Order/PersonInfoBillTo/@City"));
		
		Element eleRegion = (Element) XMLUtil.getElementsByTagName(eleReceiver,"Region").get(0);
		eleRegion.setTextContent(XMLUtil.getString(inDoc,"Order/PersonInfoBillTo/@State"));
		
		Element elePostalCode = (Element) XMLUtil.getElementsByTagName(eleReceiver, "PostalCode").get(0);
		elePostalCode.setTextContent(XMLUtil.getString(inDoc,"Order/PersonInfoBillTo/@ZipCode"));
		
		Element eleCountry = (Element) XMLUtil.getElementsByTagName(eleReceiver, "Country").get(0);
		eleCountry.setTextContent(XMLUtil.getString(inDoc,"Order/PersonInfoBillTo/@Country"));
	}
	
	// End # 4299
}
