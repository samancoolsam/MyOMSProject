package com.academy.ecommerce.sterling.los;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;

/*
 * Upgrade/Downgrade logic for 005 shipments
 * 
 */

public class AcademyUpgradeDowngradeProcess
{

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyUpgradeDowngradeProcess.class);
	private Properties props;
	public void setProperties(Properties props)
	{
		this.props = props;

	}
	String strFEDEX = "";
	String strGround ="";
	String strHomeDelivery = "";
	String strStandardOvernight = "";
	String strSmartPost = "";
	String str2Day = "";
	//START : STL-1334 Add USPS to Agile Rate Shop Call During UG/ DG
	String strUSPSAgile = null;
	String strUSPSSterling = null;
	String strPriorityMailAgile = null;
	String strPriorityMailSterling = null;
	//END : STL-1334 Add USPS to Agile Rate Shop Call During UG/ DG
	
	/*public void blankOutScac(YFSEnvironment env, Document inXML) throws Exception
	{
		log.beginTimer(" Begin of AcademyBlankOutScac->blankOutScac ()");
		log.verbose("***inXML***" +XMLUtil.getXMLString(inXML));
		log.verbose("***outside APO FPO check***" +env.getTxnObject("RemoveScac"));

		Element eleToAddress = (Element) inXML.getElementsByTagName("ToAddress").item(0);
		log.verbose("eleToAddress - " +XMLUtil.getElementXMLString(eleToAddress));
		Element eleExtn1 = (Element) eleToAddress.getElementsByTagName("Extn").item(0);
		log.verbose("eleExtn1 - " +XMLUtil.getElementXMLString(eleExtn1));

		Boolean str = false;
		if(!YFCObject.isVoid(env.getTxnObject("RemoveScac")))
		{
			str = (Boolean)env.getTxnObject("RemoveScac");
		}
		if(str)
		{
			log.verbose("***inside APO FPO check***");

			if(!"Y".equals(eleExtn1.getAttribute("ExtnIsAPOFPO")) && !"Y".equals(eleExtn1.getAttribute("ExtnIsPOBOXADDRESS")))
			{
				env.setTxnObject("RemoveScac", true);
				log.verbose("***inside APO FPO check***");
				Document docShipment1 = XMLUtil.createDocument("Shipment");
				docShipment1.getDocumentElement().setAttribute("ShipmentKey", inXML.getDocumentElement().getAttribute("ShipmentKey"));
				docShipment1.getDocumentElement().setAttribute("RoutingSource", "");
				docShipment1.getDocumentElement().setAttribute("SCAC", "");
				//docShipment.getDocumentElement().setAttribute("CarrierServiceCode", "");
				log.verbose("***docShipment***" +XMLUtil.getXMLString(docShipment1));
				Document outXML1 = AcademyUtil.invokeAPI(env, "changeShipment", docShipment1);
				log.verbose("***outXML***" +XMLUtil.getXMLString(outXML1));

			}			
		}
	}*/

	public void upgradeDowngradeProcess(YFSEnvironment env, Document inXML)throws Exception{
			
		log.beginTimer(" Begin of AcademyUpgradeDowngradeProcess->upgradeDowngradeProcess ()");
		try {
		log.verbose("***inXML***" +XMLUtil.getXMLString(inXML));

		//getting the properties from the arguments
		strFEDEX = props.getProperty("FEDEX");
		strGround = props.getProperty("Ground");
		strHomeDelivery = props.getProperty("Home Delivery");
		strStandardOvernight = props.getProperty("Standard Overnight");
		strSmartPost = props.getProperty("Smart Post");
		str2Day = props.getProperty("2Day");
		String extnOverrideUOrD = null;//STL-1710
		
		//START : STL-1334 Add USPS to Agile Rate Shop Call During UG/ DG
		strUSPSAgile = props.getProperty(AcademyConstants.STR_USPS_AGILE); 
		strUSPSSterling = props.getProperty(AcademyConstants.STR_USPS_STERLING);
		strPriorityMailAgile = props.getProperty(AcademyConstants.STR_PRIORITY_MAIL_AGILE);
		strPriorityMailSterling = props.getProperty(AcademyConstants.STR_PRIORITY_MAIL_STERLING);
		//END : STL-1334 Add USPS to Agile Rate Shop Call During UG/ DG
		
		// Start STL-1577 Shipments less than 1 lb Shall Ship USPS First Class
		String strUSPSWeightConstraint = props.getProperty(AcademyConstants.STR_USPS_WEIGHT_CONSTRAINT);
		String strUSPSScacShipmentType = props.getProperty(AcademyConstants.STR_USPS_SCAC_SHIPMENT_TYPE);
		String strLOSForUSPSFirstClass = props.getProperty(AcademyConstants.STR_LOS_FOR_USPS_FIRST_CLASS);
		// End STL-1577 Shipments less than 1 lb Shall Ship USPS First Class
		
		//Start STL-1671 Shipments less than 9 lb Shall Ship from USPS First Class
		String strUSPSWeightConstraintForPriorityMail = props.getProperty(AcademyConstants.STR_USPS_WEIGHT_CONSTRAINT_PRIORITY_MAIL);
		String strUSPSScacShipmentTypeForPriorityMail = props.getProperty(AcademyConstants.STR_USPS_SCAC_SHIPMENT_TYPE_PRIORITY_MAIL);
		String strLOSForUSPSPriorityMail = props.getProperty(AcademyConstants.STR_LOS_FOR_USPS_PRIORITY_MAIL);
		boolean isLOSModificationRequire= true;
		
		log.verbose("USPSWeightConstraintForPriorityMail:  " +strUSPSWeightConstraintForPriorityMail);
		log.verbose("USPSScacShipmentTypeForPriorityMail:  " +strUSPSScacShipmentTypeForPriorityMail);
		log.verbose("LOSForUSPSPriorityMail:  " +strLOSForUSPSPriorityMail);
		//END STL-1671 Shipments less than 9 lb Shall Ship from USPS First Class
		
		Document changeShipmentOut = null;
		Document docChangeShipment = XMLUtil.createDocument("Shipment");
		Document docChangeShipmentStatusOutput = null;
		boolean changeShipmentRequired = false;

		//getting the shipmentno from the  input
		Element rootEle =  inXML.getDocumentElement();
		//Element eleRoutable = (Element)rootEle.getElementsByTagName("Routable").item(0);
		String strShipmentNo = rootEle.getAttribute("ShipmentNo");

		//creating input for getShipmentList api call
		Document docGetShipmentList = XMLUtil.createDocument("Shipment");
		docGetShipmentList.getDocumentElement().setAttribute("ShipmentNo", strShipmentNo);

		log.verbose("***docGetShipmentList***" +XMLUtil.getXMLString(docGetShipmentList));
		//getting the template of getShipmentList api call
		env.setApiTemplate("getShipmentList", "global/template/api/getShipmentListForRateShop.xml");

		//getShipmentList call
		Document outShipmentListDoc = AcademyUtil.invokeAPI(env, "getShipmentList", docGetShipmentList);
		env.clearApiTemplate("getShipmentList");
		log.verbose("***outShipmentListDoc***" +XMLUtil.getXMLString(outShipmentListDoc));

		//getting the ToAddress
		Element eleToAddress = null;		
		if(outShipmentListDoc.getDocumentElement().getElementsByTagName("ToAddress").getLength()>0)
			eleToAddress = (Element)outShipmentListDoc.getDocumentElement().getElementsByTagName("ToAddress").item(0);

		//getting the attributes to check the address is a APO/FPO address or a PO Box Address
		Element eleExtn = (Element)eleToAddress.getElementsByTagName("Extn").item(0);
		String extnIsAPOFPO = eleExtn.getAttribute("ExtnIsAPOFPO");
		String extnIsPOBOXADDRESS = eleExtn.getAttribute("ExtnIsPOBOXADDRESS");

		Element eleShipment = (Element)outShipmentListDoc.getDocumentElement().getElementsByTagName("Shipment").item(0);
		String strOutShipmentNo = eleShipment.getAttribute("ShipmentNo");
		String strShipmentKey = eleShipment.getAttribute("ShipmentKey");
		String strSellerOrganizationCode = eleShipment.getAttribute("SellerOrganizationCode");
		//getting the shipnode
		String strShipNode = eleShipment.getAttribute("ShipNode");

		// Stamp ShipmentKey in the changeShipment Input Document.
		Element shipmentElem = docChangeShipment.getDocumentElement();
		shipmentElem.setAttribute("ShipmentKey", strShipmentKey);

		//getting the original SCAC and LOS
		Element eleExtnShipment = (Element)eleShipment.getElementsByTagName("Extn").item(0);
		String extnCarrierServiceCode = eleExtnShipment.getAttribute("ExtnOriginalShipmentLos");
		String extnSCAC = eleExtnShipment.getAttribute("ExtnOriginalShipmentScac");
		
		//START STL-1710 Enable override U/D logic 005 WMS 
		extnOverrideUOrD = eleExtnShipment.getAttribute(AcademyConstants.EXTN_ATTR_OVERRIDE_UPGD_OR_DGD);
		if (!YFCObject.isVoid(extnOverrideUOrD) && !extnOverrideUOrD.equalsIgnoreCase(AcademyConstants.STR_YES)) {
		//END STL-1710 Enable override U/D logic 005 WMS 
		log.verbose("AcademySFSLOSUpgradeDowngradeProcess_extnOverrideUOrD: " + extnOverrideUOrD);
		//getting the deliverydate
		String strShipmentType = eleShipment.getAttribute("ShipmentType");
		String strCarrierServiceCode = eleShipment.getAttribute("CarrierServiceCode");
		Element eleAdditionalDates = (Element)eleShipment.getElementsByTagName("AdditionalDates").item(0);
		NodeList additionalDateNL = eleAdditionalDates.getElementsByTagName("AdditionalDate");
		String strRequestedDate = "";
		
		// Start STL-1577 Shipments less than 1 lb Shall Ship USPS First Class
		//Checking Shipment Weight and Carrier service code
		String strTotalWeight = eleShipment.getAttribute(AcademyConstants.STR_Total_Weight);

		if (!(YFCObject.isVoid(strUSPSWeightConstraint))
				&& strUSPSScacShipmentType.contains(strShipmentType)
				&& strLOSForUSPSFirstClass.contains(strCarrierServiceCode)
				&& Double.parseDouble(strTotalWeight) <= Double.parseDouble(strUSPSWeightConstraint)) {
			log.verbose("Setting USPS First-Class Mail");
			shipmentElem.setAttribute(AcademyConstants.ATTR_SCAC, AcademyConstants.STR_USPS_SCAC);
			shipmentElem.setAttribute(AcademyConstants.CARRIER_SERVICE_CODE, AcademyConstants.STR_CARRIER_SERVICE_CODE);
			changeShipmentRequired = true;
			strCarrierServiceCode=AcademyConstants.STR_CARRIER_SERVICE_CODE;
		}
		// End STL-1577 Shipments less than 1 lb Shall Ship USPS First Class
		//Start STL-1671 Shipments less than 9 lb Shall Ship USPS First Class
		else if (!(YFCObject.isVoid(strUSPSWeightConstraintForPriorityMail))
				&& strUSPSScacShipmentTypeForPriorityMail.contains(strShipmentType)
				&& strLOSForUSPSPriorityMail.contains(strCarrierServiceCode)
				&& Double.parseDouble(strTotalWeight) <= Double.parseDouble(strUSPSWeightConstraintForPriorityMail)) {
			log.verbose("Setting USPS Priority Mail");
			shipmentElem.setAttribute(AcademyConstants.ATTR_SCAC, strUSPSSterling);
			shipmentElem.setAttribute(AcademyConstants.CARRIER_SERVICE_CODE, strPriorityMailSterling);
			changeShipmentRequired = true;
			isLOSModificationRequire = false;
		}//END STL-1671 Shipments less than 9 lb Shall Ship USPS First Class

		for(int i=0; i<additionalDateNL.getLength(); i++)
		{
			Element eleAdditionalDate = (Element)additionalDateNL.item(i);
			log.verbose("eleOrderDate - " +XMLUtil.getElementXMLString(eleAdditionalDate));
			if("ACADEMY_DELIVERY_DATE".equals(eleAdditionalDate.getAttribute("DateTypeId")))
			{
				log.verbose("***inside if***");
				strRequestedDate = eleAdditionalDate.getAttribute("RequestedDate");
			}
		}

		//Start Ammo change - check if ShipmentType="AMMO" and address is Commercial then set SCAC="FEDX" and CarrierServiceCode="Ground"
		
		String strAmmoShipmentType=rootEle.getAttribute("ShipmentType");
		log.verbose("ShipmentType: "+strAmmoShipmentType);
		String strCompany=eleToAddress.getAttribute("Company");
		log.verbose("Comapny: "+strCompany);
		
		//Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
		if((AcademyConstants.AMMO_SHIPMENT_TYPE.equals(strShipmentType) || AcademyConstants.HAZMAT_SHIPMENT_TYPE.equals(strShipmentType)) && 
				!YFCObject.isVoid(strCompany)){
			//End WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
			
			Document docShipment = XMLUtil.createDocument("Shipment");
			docShipment.getDocumentElement().setAttribute("ShipmentKey", inXML.getDocumentElement().getAttribute("ShipmentKey"));
			docShipment.getDocumentElement().setAttribute("SCAC", AcademyConstants.SCAC_FEDX);
			docShipment.getDocumentElement().setAttribute("CarrierServiceCode", AcademyConstants.LOS_GROUND);
			// Added OverrideModificationRules="Y" as a part of STL-1561
			docShipment.getDocumentElement().setAttribute(AcademyConstants.ATTR_OVERRIDE_MODIFICATION_RULES, AcademyConstants.STR_YES);
			log.verbose("***docShipment***" +XMLUtil.getXMLString(docShipment));
			Document outXML = AcademyUtil.invokeAPI(env, "changeShipment", docShipment);
			log.verbose("***outXML***" +XMLUtil.getXMLString(outXML));
		}
		//End Ammo change
		
		Element eleRoot = null;
		if (!YFCObject.isVoid(strRequestedDate)) {
			// eliminating store shipments, DSV shipments
			if ("005".equals(strShipNode)) {
				// eliminating WG and GC shipments
				//STL-735: Added a condition to avoid upgrade/downgrade logic for AMMO shipments.
				log.verbose("Shipment Type: " + strShipmentType);
				
				//Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
				if (!"WG".equals(strShipmentType) && !"GC".equals(strShipmentType) 
						&& !"GCO".equals(strShipmentType) && !AcademyConstants.AMMO_SHIPMENT_TYPE.equals(strShipmentType)
						&& !AcademyConstants.HAZMAT_SHIPMENT_TYPE.equals(strShipmentType)
						&& ! AcademyConstants.STR_CARRIER_SERVICE_CODE.equals(strCarrierServiceCode) //STL-1577 Added a condition to avoid upgrade/downgrade logic for shipments less then 1 lbs
						&& (isLOSModificationRequire) //STL-1671 
				//&& ! "Standard Overnight".equals(strCarrierServiceCode)//Commented as part of STL-1477	
						//End WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
				){
					log.verbose("inside upgrade/downgrade");
					// eliminating APO/FPO and PO Box shipments
					if (!"Y".equals(extnIsAPOFPO) && !"Y".equals(extnIsPOBOXADDRESS)) {
						Document docShipment = XMLUtil.getDocumentForElement(eleShipment);
						// calling AcademyRateShopService to get the agile
						// response
						Document docPierbridgeResposnse = AcademyUtil.invokeService(env, "AcademyRateShopService", docShipment);
						//calling the method getCheapestCarrierService which will give the upgraded/downgraded SCAC and LOS
						Document resultDoc = getCheapestCarrierService(env, extnSCAC, extnCarrierServiceCode, docShipment, docPierbridgeResposnse);
						if (!YFCObject.isNull(resultDoc))
						{
							eleRoot = resultDoc.getDocumentElement();
							// preparing the changeShipment input document
							shipmentElem.setAttribute("SCAC", eleRoot.getAttribute("SCAC"));
							shipmentElem.setAttribute("CarrierServiceCode", eleRoot.getAttribute("CarrierService"));
							Element eleoutExtn = docChangeShipment.createElement("Extn");
							eleoutExtn.setAttribute("ExtnShipUpgrdOrDowngrd", eleRoot.getAttribute("ShipUpgrdOrDowngrd"));
							shipmentElem.appendChild(eleoutExtn);

							//Checking whether the shipment is eligible for upgrade(U)/Downgrade(D) 

							//Updating as part of STL-566

							//Updating as part of STL-580 - For Overnight Shipment Downgrade should not happen

							String extnShipUpgrdOrDowngrd = eleoutExtn.getAttribute("ExtnShipUpgrdOrDowngrd");
							log.verbose("extnCarrierServiceCode :: "+ extnCarrierServiceCode);
							log.verbose("extnShipUpgrdOrDowngrd :: "+ extnShipUpgrdOrDowngrd);

							//if (!extnCarrierServiceCode.equalsIgnoreCase("Standard Overnight") || ! extnShipUpgrdOrDowngrd.equalsIgnoreCase("D"))
							//{

							log.verbose("Standard Overnight should not be Downgraded");
							/* Start - Changes made as part of STL 683 */
							changeShipmentRequired = true;
							/* End - Changes made as part of STL 683 */
							/*  OMNI-90977 - START - Remove Upgrade/downgrade alerts
							if ("U".equals(eleRoot.getAttribute("ShipUpgrdOrDowngrd")) || "D".equals(eleRoot.getAttribute("ShipUpgrdOrDowngrd"))) {
								// preparing the alert document to raise the
								// alert
								log.verbose("Raising Alert");
								Document alertDoc = XMLUtil.createDocument("Shipment");
								Element eleAlertShipment = alertDoc.getDocumentElement();
								eleAlertShipment.setAttribute("ShipmentNo", eleShipment.getAttribute("ShipmentNo"));
								eleAlertShipment.setAttribute("ShipmentKey", strShipmentKey);
								eleAlertShipment.setAttribute("SCAC", eleShipment.getAttribute("SCAC"));
								eleAlertShipment.setAttribute("CarrierServiceCode", eleShipment.getAttribute("CarrierServiceCode"));
								eleAlertShipment.setAttribute("ChangedSCAC", eleRoot.getAttribute("SCAC"));
								eleAlertShipment.setAttribute("ChangedCarrierServiceCode", eleRoot.getAttribute("CarrierService"));
								eleAlertShipment.setAttribute("UpgradedOrDowngraded", eleRoot.getAttribute("ShipUpgrdOrDowngrd"));
								log.verbose("***alertDoc***"+XMLUtil.getXMLString(alertDoc));
								//invoking the service RaiseAlertForChangeInLOS for raising an alert
								AcademyUtil.invokeService(env, "RaiseAlertForChangeInLOS", alertDoc);
							}
							OMNI-90977 - END - Remove Upgrade/downgrade alerts */
						}
					}
				}
			}
			//}
		}
		
		String strScac = "";
		String strCarrierService = "";
		//Preparing the output of RoutingShipment UE
		/*Document docRoutingResults = XMLUtil.createDocument("RoutingResults");
		Element eleRoutingResults = docRoutingResults.getDocumentElement();

		Element eleRoutingResult = docRoutingResults.createElement("RoutingResult");
		eleRoutingResults.appendChild(eleRoutingResult);

		//setting the attributes for the element Routable of the output
		Element eleOutRoutable = docRoutingResults.createElement("Routable");
		eleRoutingResult.appendChild(eleOutRoutable);
		eleOutRoutable.setAttribute("BuyerOrganizationCode", eleShipment.getAttribute("BuyerOrganizationCode"));
		eleOutRoutable.setAttribute("EnterpriseCode", eleShipment.getAttribute("EnterpriseCode"));
		eleOutRoutable.setAttribute("InputCarrierServiceCode", eleShipment.getAttribute("CarrierServiceCode"));
		eleOutRoutable.setAttribute("SellerOrganizationCode", eleShipment.getAttribute("SellerOrganizationCode"));
		eleOutRoutable.setAttribute("ShipmentNo", eleShipment.getAttribute("ShipmentNo"));
		eleOutRoutable.setAttribute("Weight", eleShipment.getAttribute("TotalWeight"));
		eleOutRoutable.setAttribute("WeightUOM", eleShipment.getAttribute("TotalWeightUOM"));
		eleOutRoutable.setAttribute("Volume", eleShipment.getAttribute("TotalVolume"));
		eleOutRoutable.setAttribute("VolumeUOM", eleShipment.getAttribute("TotalVolumeUOM"));

		//setting the attributes for the element RoutingOption of the output
		Element eleRoutingOption = docRoutingResults.createElement("RoutingOption");
		eleRoutingResult.appendChild(eleRoutingOption);
		eleRoutingOption.setAttribute("InputCarrierServiceCode", eleShipment.getAttribute("CarrierServiceCode"));
		eleRoutingOption.setAttribute("MaxWeight", eleShipment.getAttribute("TotalWeight"));
		eleRoutingOption.setAttribute("MinVolume", eleShipment.getAttribute("TotalVolume"));
		eleRoutingOption.setAttribute("WeightUOM", eleShipment.getAttribute("TotalWeightUOM"));
		eleRoutingOption.setAttribute("VolumeUOM", eleShipment.getAttribute("TotalVolumeUOM"));

		Element eleCarrierOptions = docRoutingResults.createElement("CarrierOptions");
		eleRoutingOption.appendChild(eleCarrierOptions);

		Element eleCarrierOption = docRoutingResults.createElement("CarrierOption");
		eleCarrierOptions.appendChild(eleCarrierOption); */
		
		if(eleRoot!=null)
		{
			strScac = "FedEx";
			strCarrierService = eleRoot.getAttribute("CarrierService");
		}
		
		//if the upgrade needs to be done then setting the CarrierServiceCode
		/*if("U".equals(eleRoot.getAttribute("ShipUpgrdOrDowngrd")))
			{
				log.verbose("LOS Got Upgraded");
				strScac = "FedEx";
				strCarrierService = eleRoot.getAttribute("CarrierService");
			}
			//if the downgrade needs to be done then setting the CarrierServiceCode
			else if ("D".equals(eleRoot.getAttribute("ShipUpgrdOrDowngrd")) && extnCarrierServiceCode.equalsIgnoreCase("Standard Overnight"))
			{
				log.verbose("Downgrade should not happen for Standard Overnight");
				strScac = "FedEx";
				strCarrierService = eleShipment.getAttribute("CarrierServiceCode");
			}
			else if ("D".equals(eleRoot.getAttribute("ShipUpgrdOrDowngrd")) || !extnCarrierServiceCode.equalsIgnoreCase("Standard Overnight"))
			{
				log.verbose("LOS got Downgraded");
				strScac = "FedEx";
				strCarrierService = eleRoot.getAttribute("CarrierService");
			}
			//else setting the original scac and LOS
			else
			{
				strScac = eleRoot.getAttribute("SCAC");
				strCarrierService = eleShipment.getAttribute("CarrierServiceCode");
				if("FEDX".equals(strScac))
				{
					strScac = "FedEx";
				}
				if(strFEDEX.equals(strScac))
				{
					strScac = "FedEx";
				}
				if("Next Day Air".equals(strCarrierService))
				{
					strCarrierService = "ND";
				}
				else if("2nd Day Air".equals(strCarrierService))
				{
					strCarrierService = "2nd";
				}
			}*/

		else
		{
			strScac = eleShipment.getAttribute("SCAC");
			//Start - Changes made as part of STL 683 
			// scac and service check was failing for FEDX instead of FedEx
			if (strScac != null && strScac.equalsIgnoreCase("FEDX")) {
				strScac = "FedEx";
			}
			//End - Changes made as part of STL 683
			strCarrierService = eleShipment.getAttribute("CarrierServiceCode");
		}
		//String strScacAndService = strScac + " " + strCarrierService;
		//eleCarrierOption.setAttribute("ScacAndServiceCode", strScacAndService);

		/* Start - Changes made as part of STL 683 */
		// Company field will be stamped as DOT when SCAC is FedEx and Service
		// is Ground.
		docChangeShipment = AcademyUtil.updateCompanyToDot(strScac, strCarrierService, docChangeShipment, outShipmentListDoc);
		if (docChangeShipment.getDocumentElement().hasAttribute("ChangeShipmentRequired")) {
			// ChangeShipmentRequired attribute has been set in
			// updateCompanyToDot() method to identify whether a changeShipment
			// API call is required here.
			changeShipmentRequired = true;
		}

		if (changeShipmentRequired) {
			// Added OverrideModificationRules="Y" as a part of STL-1561
			Element eleRootShipment = docChangeShipment.getDocumentElement();
			eleRootShipment.setAttribute(AcademyConstants.ATTR_OVERRIDE_MODIFICATION_RULES, AcademyConstants.STR_YES);
			log.verbose("Input to changeShipment API +::" +XMLUtil.getXMLString(docChangeShipment));
			changeShipmentOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, docChangeShipment);
			log.verbose("Output to changeShipment API +::" +XMLUtil.getXMLString(changeShipmentOut));
		}
		
		//START STL-1561 Preventing shipment from changing to Routed status
		/*Document docChangeShipmentStatus = XMLUtil.createDocument("Shipment");
		Element eleChangeShipmentStatus = docChangeShipmentStatus.getDocumentElement();
		eleChangeShipmentStatus.setAttribute("ShipmentNo", strOutShipmentNo);
		eleChangeShipmentStatus.setAttribute("ShipmentKey", strShipmentKey);
		eleChangeShipmentStatus.setAttribute("SellerOrganizationCode", strSellerOrganizationCode);
		eleChangeShipmentStatus.setAttribute("ShipNode", strShipNode);
		eleChangeShipmentStatus.setAttribute("BaseDropStatus", AcademyConstants.BASE_DROP_STATUS);
		eleChangeShipmentStatus.setAttribute("TransactionId", AcademyConstants.TRANSACTION_ID_FOR_ROUTE_SHIPMENT);
		docChangeShipmentStatusOutput = AcademyUtil.invokeAPI(env, "changeShipmentStatus", docChangeShipmentStatus);*/
		//END STL-1561
		} 		
		} //STL-1710 Enable override U/D logic 005 WMS  
		catch (ParserConfigurationException e) {
			log.verbose("Caught ParserConfigurationException" );
			e.printStackTrace();
		} catch (SAXException e) {
			log.verbose("Caught SAXException" );
			e.printStackTrace();
		} catch (IOException e) {
			log.verbose("Caught IOException" );
			e.printStackTrace();
		} catch (Exception e) {
			log.verbose("Caught Exception" );
			e.printStackTrace();
		}
		log.endTimer(" End of AcademyUpgradeDowngradeProcess->upgradeDowngradeProcess ()");
		}
	
	/* End - Changes made as part of STL 683 */

	//return docRoutingResults;


/**
 * This method transforms the agile response to the Sterling input format.
 * And also determines whether it is a upgrade/downgrade.
 * 
 * @param env
 *            Yantra Environment Context.
 * @param inSCAC
 *            Original SCAC.
 * @param inCarrierServiceCode
 *            Original CarrierServiceCode.
 * @param docShipment
 *            getShipmentList output document.
 * @param peirbridgeResponse
 *            agile Response Document.
 *            
 * @return Document
 */
public Document getCheapestCarrierService(YFSEnvironment env, String inSCAC, String inCarrierServiceCode, Document docShipment, Document peirbridgeResponse) throws Exception
{
	NodeList rateLt = peirbridgeResponse.getElementsByTagName("Rate");
	Element tempRateElement = null;
	Document outDoc = YFCDocument.createDocument("Out").getDocument();
	String scac = null;
	String strCode = null;
	
	log.verbose("inSCAC"+ inSCAC);
	log.verbose("inCarrierServiceCode"+ inCarrierServiceCode);

	//preparing the input to getCommonCodeList to check the priority of the original LOS
	Document getCommonCodeListInputXML = XMLUtil.createDocument("CommonCode");
	getCommonCodeListInputXML.getDocumentElement().setAttribute("CodeType", "PriorityCarrier");
	getCommonCodeListInputXML.getDocumentElement().setAttribute("CodeValue", inCarrierServiceCode);
	//getCommonCodeList api call
	Document outXML = AcademyUtil.invokeAPI(env, "getCommonCodeList", getCommonCodeListInputXML);
	log.verbose("***getCommonCodeList out XML 1***" +XMLUtil.getXMLString(outXML));
	NodeList commonCodeList = outXML.getElementsByTagName("CommonCode");
	String strCarrierPriority = "";

	if (commonCodeList != null && !YFCObject.isVoid(commonCodeList))
	{
		log.verbose("Common Code is not null");
		Element commonCode = (Element)commonCodeList.item(0);
		String codeValue = commonCode.getAttribute("CodeValue");
		log.verbose("codeValue"+ codeValue);
		if (inCarrierServiceCode.equals(codeValue))
		{
			strCarrierPriority = commonCode.getAttribute("CodeShortDescription");
			log.verbose("strCarrierPriority is: " + strCarrierPriority);
		}
	}

	//agile output will contain only one rate if we call with FilterMode
	//START : STL-1481 : Added the logic to handle the error response from Agile.
	log.verbose("before rateLt.getLength() == 1");
	if(rateLt.getLength() == 1)
	{
		log.verbose("Inside rateLt.getLength() == 1");
		strCode  = peirbridgeResponse.getElementsByTagName(AcademyConstants.ELE_AGILE_CODE).item(0).getTextContent();
		log.verbose("Status Code from Agile response"+strCode);

		//Agile Status Code=1=Success Code=0=Error
		if(AcademyConstants.STR_AGILE_CODE_VALUE_SUCCESS.equals(strCode))
		{
			tempRateElement = (Element)rateLt.item(0);
			scac = tempRateElement.getElementsByTagName(AcademyConstants.ATTR_SCAC).item(0).getTextContent();			
		}
	}
	
	if(!YFSObject.isVoid(scac))
	{
		/*//fetching the SCAC from agile response
		log.verbose("rateLt.getLength()==1");
		tempRateElement = (Element)rateLt.item(0);
		String scac = tempRateElement.getElementsByTagName("SCAC").item(0).getTextContent();*/
		//END : STL-1481 : Added the logic to handle the error response from Agile.
		log.verbose("SCAC is :::"+ scac);
		if(scac.contains(strFEDEX))
		{
			scac="FEDX";
		}
		if("UPS".equals(scac))
		{
			scac="UPSN";
		}
		//START : STL-1334 Add USPS to Agile Rate Shop Call During UG/ DG
		if(scac.contains(strUSPSAgile))
		{
			scac=strUSPSSterling;
		}
		//END : STL-1334 Add USPS to Agile Rate Shop Call During UG/ DG
		
		//fetching the LOS from the agile response
		Element serviceType = (Element)tempRateElement.getElementsByTagName("ServiceType").item(0);
		String carrierService = serviceType.getElementsByTagName("Description").item(0).getTextContent();
		log.verbose("carrierService given by Agile"+ carrierService);
		//all the below logic is to fetch the correct LOS according to Sterling
		if(carrierService.contains(strHomeDelivery))
		{
			carrierService = strHomeDelivery;

			/*if("".equals(XMLUtil.getString(docShipment, "Shipment/ToAddress/@Company")))
				{
					carrierService = strHomeDelivery;
				}
				else
				{
					Element eleShipment = docShipment.getDocumentElement();
					eleShipment.setAttribute("Exceptional", "Y");
					Document docPierbridgeResposnse = AcademyUtil.invokeService(env, "AcademyRateShopService", docShipment);
					NodeList rateLt1 = docPierbridgeResposnse.getElementsByTagName("Rate");
					boolean isGd = false;
					for(int i=0; i<rateLt1.getLength(); i++)
					{
						Element eleRate = (Element)rateLt1.item(i);
						Element eleServiceType = (Element)eleRate.getElementsByTagName("ServiceType").item(0);
						Element eleDescription = (Element)eleServiceType.getElementsByTagName("Description").item(0);
						String strDescription = eleDescription.getTextContent();
						if(strDescription.contains("Ground"))
						{
							carrierService = "Ground";
							isGd = true;
							break;
						}
					}
					if(!isGd)
					{
						log.verbose("***** Making the outDoc null ****** ");
						outDoc = null;
					}
				}*/
		}
		else if(carrierService.contains(strGround))
		{
			carrierService = "Ground";

			/*if("".equals(XMLUtil.getString(docShipment, "Shipment/ToAddress/@Company")))
				{					
					Element eleShipment = docShipment.getDocumentElement();
					eleShipment.setAttribute("Exceptional", "Y");
					Document docPierbridgeResposnse = AcademyUtil.invokeService(env, "AcademyRateShopService", docShipment);
					NodeList rateLt1 = docPierbridgeResposnse.getElementsByTagName("Rate");
					boolean isHD = false;
					for(int i=0; i<rateLt1.getLength(); i++)
					{
						Element eleRate = (Element)rateLt1.item(i);
						Element eleServiceType = (Element)eleRate.getElementsByTagName("ServiceType").item(0);
						Element eleDescription = (Element)eleServiceType.getElementsByTagName("Description").item(0);
						String strDescription = eleDescription.getTextContent();
						if(strDescription.contains("Home Delivery"))
						{
							carrierService = "Home Delivery";
							isHD = true;
							break;
						}
						if(strDescription.contains("Ground"))
                        {
                            carrierService = "Ground";
                            isHD = true;
                            break;
                        }

					}
					if(!isHD)
					{
						log.verbose("***** Making the outDoc null ****** ");
						outDoc = null;
					}
				}
				else
				{
					carrierService = strGround;
				}*/
		}			
		else if(carrierService.contains(strStandardOvernight))
		{
			carrierService = strStandardOvernight;
		}
		else if(carrierService.contains(str2Day))
		{
			carrierService = "2 Day";
		}

		//Start WN-1118 Split out the FedEx Smartpost SCAC
		else if(carrierService.contains(AcademyConstants.STR_PARCEL_SELECT))
		{
			carrierService = AcademyConstants.STR_PARCEL_POST;
			scac = AcademyConstants.STR_SMART_POST;
		}	
		//End WN-1118 Split out the FedEx Smartpost SCAC
		
		else if(carrierService.contains(strSmartPost))
		{
			carrierService = strSmartPost;
		}
		else if(carrierService.contains("2nd Day Air"))
		{
			carrierService = "2nd Day Air";
		}
		else if(carrierService.contains("Next Day Air"))
		{
			carrierService = "Next Day Air";
		}
		//START : STL-1334 Add USPS to Agile Rate Shop Call During UG/ DG
		else if(carrierService.contains(strPriorityMailAgile))
		{
			carrierService = strPriorityMailSterling;
		}
		//END : STL-1334 Add USPS to Agile Rate Shop Call During UG/ DG

		//fetching the priority of the LOS returned by agile
		Document getCommonCodeListInputXML1 = XMLUtil.createDocument("CommonCode");
		getCommonCodeListInputXML1.getDocumentElement().setAttribute("CodeType", "PriorityCarrier");
		getCommonCodeListInputXML1.getDocumentElement().setAttribute("CodeValue", carrierService);
		Document outXML1 = AcademyUtil.invokeAPI(env, "getCommonCodeList", getCommonCodeListInputXML1);

		log.verbose("***getCommonCodeList out XML 2***" +XMLUtil.getXMLString(outXML1));
		NodeList commonCodeList1 = outXML1.getElementsByTagName("CommonCode");
		String strCarrierPriority1 = "";

		if (commonCodeList1 != null && !YFCObject.isVoid(commonCodeList1))
		{
			log.verbose("CommonCode List2 is not null");
			Element commonCode = (Element)commonCodeList1.item(0);
			String codeValue = commonCode.getAttribute("CodeValue");
			log.verbose("codeValue2::"+ codeValue);
			if (carrierService.equals(codeValue))
			{
				log.verbose("Code Value and LOS is same");
				strCarrierPriority1 = commonCode.getAttribute("CodeShortDescription");
				log.verbose("strCarrierPriority is: " + strCarrierPriority1);
			}

			//preparing the return doc
			outDoc.getDocumentElement().setAttribute("SCAC", scac);
			outDoc.getDocumentElement().setAttribute("CarrierService", carrierService);	

			log.verbose("strCarrierPriority************"+ strCarrierPriority);
			log.verbose("strCarrierPriority1********"+ strCarrierPriority1);
			//comparing the original LOS and the agile returned LOS to determine whether it is a upgrade or downgrade
			int i = Integer.parseInt(strCarrierPriority);			
			int i1 = Integer.parseInt(strCarrierPriority1);

			log.verbose("I & I1"+ i+"*******"+ i1);
			if(i>i1)
			{
				outDoc.getDocumentElement().setAttribute("ShipUpgrdOrDowngrd","D");
			}
			else if(i<i1)
			{
				outDoc.getDocumentElement().setAttribute("ShipUpgrdOrDowngrd","U");
			}
			log.verbose("***outDoc**" +XMLUtil.getXMLString(outDoc));
		}	
		//START : STL-1334 Add USPS to Agile Rate Shop Call During UG/ DG
		//This is to handle if agile return some carrier which is not configure in Sterling common code.
		else{
			outDoc = null;
		}
		//END : STL-1334 Add USPS to Agile Rate Shop Call During UG/ DG
	}

	//if the agile returns blank response, calling the agile without filter option and getting the highest LOS
	else
	{
		log.verbose("Agile gave more responses");
		Element eleShipment = docShipment.getDocumentElement();
		eleShipment.setAttribute("Exceptional", "Y");
		//calling AcademyRateShopService to get the agile response
		Document docPierbridgeResposnse = AcademyUtil.invokeService(env, "AcademyRateShopService", docShipment);

		//getting all the priority of the LOS from the common codes
		Document getCommonCodeListXML = XMLUtil.createDocument("CommonCode");
		//Start: changes as part of STL-1477
		/*getCommonCodeListXML.getDocumentElement().setAttribute("CodeType", "PriorityCarrier");*/
		getCommonCodeListXML.getDocumentElement().setAttribute("CodeType", AcademyConstants.COMMON_CODE_PRIORITY_NO_FILTER_MODE);
		//End: changes as part of STL-1477
		Document outgetCommonCodeListXML = AcademyUtil.invokeAPI(env, "getCommonCodeList", getCommonCodeListXML);
		//calling the method getHighest to get the highest LOS from the agile response
		String strServiceLevel = getHighest(docShipment, docPierbridgeResposnse, outgetCommonCodeListXML);

		log.verbose("strServiceLevel"+ strServiceLevel);
		if(!("".equals(strServiceLevel)))
		{
			//START : STL-1334 Add USPS to Agile Rate Shop Call During UG/ DG
			//outDoc.getDocumentElement().setAttribute("SCAC", "FEDX");
			
			if(strPriorityMailSterling.equals(strServiceLevel))
			{
				scac=strUSPSSterling;								
			}
			
			//Start WN-1118 Split out the FedEx Smartpost SCAC
			else if (AcademyConstants.STR_PARCEL_POST.equals(strServiceLevel))
			{
				scac = AcademyConstants.STR_SMART_POST;
			}
			//End WN-1118 Split out the FedEx Smartpost SCAC
			
			else
			{
				scac=AcademyConstants.SCAC_FEDX;
			}
			log.verbose("SCAC is :::"+ scac);
			outDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_SCAC, scac);
			//END : STL-1334 Add USPS to Agile Rate Shop Call During UG/ DG			
			outDoc.getDocumentElement().setAttribute("CarrierService", strServiceLevel);

			//getting the priority of the highest value fetched earlier
			Document getCommonCodeListInputXML1 = XMLUtil.createDocument("CommonCode");
			getCommonCodeListInputXML1.getDocumentElement().setAttribute("CodeType", "PriorityCarrier");
			getCommonCodeListInputXML1.getDocumentElement().setAttribute("CodeValue", strServiceLevel);
			Document outXML1 = AcademyUtil.invokeAPI(env, "getCommonCodeList", getCommonCodeListInputXML1);

			log.verbose("***getCommonCode List 3 out put**" +XMLUtil.getXMLString(outXML1));
			NodeList commonCodeList1 = outXML1.getElementsByTagName("CommonCode");
			String strCarrierPriority1 = "";

			if (commonCodeList1 != null && !YFCObject.isVoid(commonCodeList1))
			{
				Element commonCode = (Element)commonCodeList1.item(0);
				String codeValue = commonCode.getAttribute("CodeValue");
				log.verbose("codeValue%%%%%%%"+ codeValue);
				if (strServiceLevel.equals(codeValue))
				{
					strCarrierPriority1 = commonCode.getAttribute("CodeShortDescription");
					log.verbose("strCarrierPriority is: " + strCarrierPriority1);
				}
				log.verbose("strCarrierPriority#############"+ strCarrierPriority);
				log.verbose("strCarrierPriority1############"+ strCarrierPriority1);

				int i = Integer.parseInt(strCarrierPriority);			
				int i1 = Integer.parseInt(strCarrierPriority1);

				log.verbose("I & I1"+ i+"############"+ i1);

				//comparing the original LOS and the LOS returned by agile
				if(i>i1) 
				{
					outDoc.getDocumentElement().setAttribute("ShipUpgrdOrDowngrd","D");
				}
				else if(i<i1)
				{
					outDoc.getDocumentElement().setAttribute("ShipUpgrdOrDowngrd","U");
				}
			}
			log.verbose("***outDoc#########" +XMLUtil.getXMLString(outDoc));
		}
		else
		{
			log.verbose("***** Making the outDoc null ****** ");
			outDoc = null;
		}
	}

	if (!YFCObject.isNull(outDoc)) 
	{
		log.verbose("***OutDoc***" + XMLUtil.getXMLString(outDoc));
	}
	return outDoc;
}

public ArrayList <String> inputServiceLevels = new ArrayList <String>();
public String finalServiceLevel = "";

/*
 * This method checks for the highest LOS from the agile response(without filter mode) and
 * returns the same
 */
public String getHighest(Document docShipment,Document docPierbridgeResposnse,Document outgetCommonCodeListXML) throws Exception 
{
	XPath xpath = XPathFactory.newInstance().newXPath();
	log.beginTimer("getHighest************");
	String strCode = null;
	
	NodeList rateLt = docPierbridgeResposnse.getElementsByTagName("Rate");
	//START : STL-1481 : Added the logic to handle the error response from Agile. while upgrading
	if(rateLt.getLength() != 0)
	{
		log.verbose("Inside rateLt.getLength() == 1");
		strCode  = docPierbridgeResposnse.getElementsByTagName(AcademyConstants.ELE_AGILE_CODE).item(0).getTextContent();
		log.verbose("Status Code from Agile response"+strCode);			
	}
	
	//Agile Status Code=1=Success Code=0=Error
	if(AcademyConstants.STR_AGILE_CODE_VALUE_SUCCESS.equals(strCode))
	{/*
	if(rateLt.getLength()!=0)
	{*/
		//END : STL-1481 : Added the logic to handle the error response from Agile. while upgrading
		log.verbose("Rate NL is > 0"+ rateLt.getLength());
		ArrayList <Integer> rateValues = new ArrayList <Integer>();
		for(int i=0; i<rateLt.getLength(); i++)
		{
			Element eleRate = (Element)rateLt.item(i);
			Element eleServiceType = (Element)eleRate.getElementsByTagName("ServiceType").item(0);
			Element eleDescription = (Element)eleServiceType.getElementsByTagName("Description").item(0);
			String strDescription = eleDescription.getTextContent();
			
			log.verbose("strDescription&&&&&"+ strDescription);
			//adding all the values to arraylist 
			if(strDescription.contains(strHomeDelivery))
			{
				strDescription = strHomeDelivery;
				inputServiceLevels.add(strDescription);
			}
			else if(strDescription.contains(strGround))
			{
				strDescription = strGround;
				inputServiceLevels.add(strDescription);
			}

			else if(strDescription.contains(strStandardOvernight))
			{
				strDescription = strStandardOvernight;
				inputServiceLevels.add(strDescription);
			}
			else if(strDescription.contains(str2Day))
			{
				strDescription = "2 Day";
				inputServiceLevels.add(strDescription);
			}
			//Start WN-1118 Split out the FedEx Smartpost SCAC
			else if(strDescription.contains(AcademyConstants.STR_PARCEL_SELECT))
			{
				strDescription = AcademyConstants.STR_PARCEL_POST;
				inputServiceLevels.add(strDescription);
			}
			//End WN-1118 Split out the FedEx Smartpost SCAC
			else if(strDescription.contains("strSmartPost"))
			{
				strDescription = strSmartPost;
				inputServiceLevels.add(strDescription);
			}
			else if(strDescription.contains("2nd Day Air"))
			{
				strDescription = "2nd Day Air";
				inputServiceLevels.add(strDescription);
			}
			else if(strDescription.contains("Next Day Air"))
			{
				strDescription = "Next Day Air";
				inputServiceLevels.add(strDescription);
			}
			//START : STL-1334 Add USPS to Agile Rate Shop Call During UG/ DG	
			else if(strDescription.contains(strPriorityMailAgile))
			{
				strDescription = strPriorityMailSterling;
				inputServiceLevels.add(strDescription);
			}
			//END : STL-1334 Add USPS to Agile Rate Shop Call During UG/ DG	

			//getting the codevalue for each LOS
			log.verbose("***outgetCommonCodeListXML***" + XMLUtil.getXMLString(outgetCommonCodeListXML));
			XPathExpression expr= xpath.compile("CommonCodeList/CommonCode[@CodeValue=\"" + strDescription + "\"]");
			Element eleCommonCode = (Element) expr.evaluate(outgetCommonCodeListXML, XPathConstants.NODE);
			
			//START : STL-1587 U/D fails for old shipments when agile returns multiple carriers (including Stander overnight)  in without filter mode
			//log.verbose("Ele CommonCode is ::::::"+ XMLUtil.getElementXMLString(eleCommonCode));
			//END : STL-1587 U/D fails for old shipments when agile returns multiple carriers (including Stander overnight)  in without filter mode
			
			//getting the CodeShortDescription to each code value and adding to the arraylist
			if(!YFCObject.isVoid(eleCommonCode))
			{
				String strCodeShortDescription = eleCommonCode.getAttribute("CodeShortDescription");
				log.verbose("strCodeShortDescription"+ strCodeShortDescription);
				rateValues.add(Integer.parseInt(strCodeShortDescription)); 					
			}				
		}

		//getting the maximum value from the arraylist
		int maxValue = Collections.max(rateValues);
		log.verbose("maxValue in Collections"+ maxValue);
		//and getting the LOS of the code value
		XPathExpression expr1= xpath.compile("CommonCodeList/CommonCode[@CodeShortDescription=\"" + String.valueOf(maxValue) + "\"]");
		NodeList commonCodeNL = (NodeList) expr1.evaluate(outgetCommonCodeListXML, XPathConstants.NODESET);
		log.verbose("***length***"+commonCodeNL.getLength());
		for(int j=0; j<commonCodeNL.getLength(); j++)
		{
			Element eleCommonCode = (Element)commonCodeNL.item(j);
			String strCodeValue = eleCommonCode.getAttribute("CodeValue");
			log.verbose("***CodeValue***"+strCodeValue);
			if(inputServiceLevels.contains(strCodeValue))
			{
				log.verbose("Assigning Code value as Final Service Level");
				finalServiceLevel = strCodeValue;

				/*//checking for Ground
					if(strCodeValue.equals("Ground"))
					{
						//returns Ground if the Company name exist
						if(!("".equals(XMLUtil.getString(docShipment, "Shipment/ToAddress/@Company"))))
						{
							finalServiceLevel = "Ground";
						}
					}
					//checking for Home Delivery
					else if(strCodeValue.equals("Home Delivery"))
					{
						//returns Home Delivery if the Company doesn't exist
						if("".equals(XMLUtil.getString(docShipment, "Shipment/ToAddress/@Company")))
						{
							finalServiceLevel = "Home Delivery";
						}
					}
					else
					{
						finalServiceLevel = strCodeValue;
					}	*/					
			}
		}
	}
	else
	{
		log.verbose("Assigning blank value as Final Service Level");
		finalServiceLevel = "";
	}




	//returning the LOS which has the highest codevalue
	log.verbose("***finalServiceLevel***"+finalServiceLevel);
	return finalServiceLevel;
}

}
