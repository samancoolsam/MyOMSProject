package com.academy.ecommerce.sterling.shipment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.TreeMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.ydm.agent.server.YDMRoutingAgent;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfc.util.YFCDateUtils;
import com.yantra.yfs.japi.YFSEnvironment;


/**
 * Description: Class AcademyYDMDetermineRoutingExternallyUEImpl gets the appropriate Carrier by calling agile. 
 * And if it is an upgrade, then it updates the carrier service code
 * ------------------Change History--------------------------------------
 * 30-Nov-2012	Cognizant	Downgrade Deactivated - STL-364 
 * 04-SEP-2013  Upgrade/Downgrade Activated.
 * -----------------------------------------------------------------------
 */
public class AcademyYDMDetermineRoutingExternallyUEImpl
{
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyYDMDetermineRoutingExternallyUEImpl.class);
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

	/**
	 * The method determineRoutingExternally calls agile and get the appropriate SCAC.
	 * Verifies whether it is a upgrade or downgrade. 
	 * If upgrade, it sets the attribute CarrierServiceCode and raises the alert
	 * 
	 * @param env
	 *            Yantra Environment Context.
	 * @param inXML
	 *            Input Document.
	 * @return routeShipment output doc
	 */
	public Document determineRoutingExternally(YFSEnvironment env, Document inXML)throws Exception
	{
		log.beginTimer(" Begin of AcademyYDMDetermineRoutingExternallyUEImpl->externalRouting ()");
		log.verbose("***inXML***" +XMLUtil.getXMLString(inXML));

		//getting the properties from the arguments
		strFEDEX = props.getProperty("FEDEX");
		strGround = props.getProperty("Ground");
		strHomeDelivery = props.getProperty("Home Delivery");
		strStandardOvernight = props.getProperty("Standard Overnight");
		strSmartPost = props.getProperty("Smart Post");
		str2Day = props.getProperty("2Day");

		Document changeShipmentOut = null;
		Document docChangeShipment = XMLUtil.createDocument("Shipment");
		boolean changeShipmentRequired = false;

		//getting the shipmentno from the UE input
		Element eleRoutables =  inXML.getDocumentElement();
		Element eleRoutable = (Element)eleRoutables.getElementsByTagName("Routable").item(0);
		String strShipmentNo = eleRoutable.getAttribute("ShipmentNo");

		//creating input for getShipmentList api call
		Document docGetShipmentList = XMLUtil.createDocument("Shipment");
		docGetShipmentList.getDocumentElement().setAttribute("ShipmentNo", strShipmentNo);

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
		String strShipmentKey = eleShipment.getAttribute("ShipmentKey");

		// Stamp ShipmentKey in the changeShipment Input Document.
		Element shipmentElem = docChangeShipment.getDocumentElement();
		shipmentElem.setAttribute("ShipmentKey", strShipmentKey);

		//getting the original SCAC and LOS
		Element eleExtnShipment = (Element)eleShipment.getElementsByTagName("Extn").item(0);
		String extnCarrierServiceCode = eleExtnShipment.getAttribute("ExtnOriginalShipmentLos");
		String extnSCAC = eleExtnShipment.getAttribute("ExtnOriginalShipmentScac");

		//getting the shipnode
		String strShipNode = eleShipment.getAttribute("ShipNode");

		//getting the deliverydate
		String strShipmentType = eleShipment.getAttribute("ShipmentType");
		Element eleAdditionalDates = (Element)eleShipment.getElementsByTagName("AdditionalDates").item(0);
		NodeList additionalDateNL = eleAdditionalDates.getElementsByTagName("AdditionalDate");
		String strRequestedDate = "";

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

		Element eleRoot = null;
		if (!YFCObject.isVoid(strRequestedDate)) {
			// eliminating store shipments, DSV shipments
			if ("005".equals(strShipNode)) {
				// eliminating WG and GC shipments
				//STL-735: Added a condition to avoid upgrade/downgrade logic for AMMO shipments.
				log.verbose("Shipment Type: " + strShipmentType);
				
				//Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
				if (!"WG".equals(strShipmentType) && !"GC".equals(strShipmentType) && !"GCO".equals(strShipmentType) && !AcademyConstants.AMMO_SHIPMENT_TYPE.equals(strShipmentType)
						&& !AcademyConstants.HAZMAT_SHIPMENT_TYPE.equals(strShipmentType)) {
					//End WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
					
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
							/*  OMNI-90977 - START - Remove Upgrade/downgrade alerts
							if (!extnCarrierServiceCode.equalsIgnoreCase("Standard Overnight") || ! extnShipUpgrdOrDowngrd.equalsIgnoreCase("D"))
							{

								log.verbose("Standard Overnight should not be Downgraded");
								// Start - Changes made as part of STL 683
								changeShipmentRequired = true;
								// End - Changes made as part of STL 683 

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
							}
						    OMNI-90977 - END - Remove Upgrade/downgrade alerts */
						}
					}
				}
			}
		}

		//Preparing the output of RoutingShipment UE
		Document docRoutingResults = XMLUtil.createDocument("RoutingResults");
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
		eleCarrierOptions.appendChild(eleCarrierOption);
		String strScac = "";
		String strCarrierService = "";
		if(eleRoot!=null)
		{
			//if the upgrade needs to be done then setting the CarrierServiceCode
			if("U".equals(eleRoot.getAttribute("ShipUpgrdOrDowngrd")))
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
			}
		}
		else
		{
			strScac = eleShipment.getAttribute("SCAC");
			/* Start - Changes made as part of STL 683 */
			// scac and service check was failing for FEDX instead of FedEx
			if (strScac != null && strScac.equalsIgnoreCase("FEDX")) {
			    strScac = "FedEx";
            }
			/* End - Changes made as part of STL 683 */
			strCarrierService = eleShipment.getAttribute("CarrierServiceCode");
		}
		String strScacAndService = strScac + " " + strCarrierService;
		eleCarrierOption.setAttribute("ScacAndServiceCode", strScacAndService);

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
			changeShipmentOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, docChangeShipment);
		}
		/* End - Changes made as part of STL 683 */

		return docRoutingResults;
	}

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

		//preparing the input to getCommonCodeList to check the priority of the original LOS
		Document getCommonCodeListInputXML = XMLUtil.createDocument("CommonCode");
		getCommonCodeListInputXML.getDocumentElement().setAttribute("CodeType", "PriorityCarrier");
		getCommonCodeListInputXML.getDocumentElement().setAttribute("CodeValue", inCarrierServiceCode);
		//getCommonCodeList api call
		Document outXML = AcademyUtil.invokeAPI(env, "getCommonCodeList", getCommonCodeListInputXML);

		NodeList commonCodeList = outXML.getElementsByTagName("CommonCode");
		String strCarrierPriority = "";

		if (commonCodeList != null && !YFCObject.isVoid(commonCodeList))
		{
			Element commonCode = (Element)commonCodeList.item(0);
			String codeValue = commonCode.getAttribute("CodeValue");
			if (inCarrierServiceCode.equals(codeValue))
			{
				strCarrierPriority = commonCode.getAttribute("CodeShortDescription");
				log.verbose("strCarrierPriority is: " + strCarrierPriority);
			}
		}

		//agile output will contain only one rate if we call with FilterMode
		if(rateLt.getLength()==1)
		{
			//fetching the SCAC from agile response
			tempRateElement = (Element)rateLt.item(0);
			String scac = tempRateElement.getElementsByTagName("SCAC").item(0).getTextContent();
			if(scac.contains(strFEDEX))
			{
				scac="FEDX";
			}
			if("UPS".equals(scac))
			{
				scac="UPSN";
			}

			//fetching the LOS from the agile response
			Element serviceType = (Element)tempRateElement.getElementsByTagName("ServiceType").item(0);
			String carrierService = serviceType.getElementsByTagName("Description").item(0).getTextContent();

			//all the below logic is to fetch the correct LOS according to Sterling
			if(carrierService.contains(strHomeDelivery))
			{
				if("".equals(XMLUtil.getString(docShipment, "Shipment/ToAddress/@Company")))
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
				}
			}
			else if(carrierService.contains(strGround))
			{
				if("".equals(XMLUtil.getString(docShipment, "Shipment/ToAddress/@Company")))
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
				}
			}			
			else if(carrierService.contains(strStandardOvernight))
			{
				carrierService = strStandardOvernight;
			}
			else if(carrierService.contains(str2Day))
			{
				carrierService = "2 Day";
			}
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

			//fetching the priority of the LOS returned by agile
			Document getCommonCodeListInputXML1 = XMLUtil.createDocument("CommonCode");
			getCommonCodeListInputXML1.getDocumentElement().setAttribute("CodeType", "PriorityCarrier");
			getCommonCodeListInputXML1.getDocumentElement().setAttribute("CodeValue", carrierService);
			Document outXML1 = AcademyUtil.invokeAPI(env, "getCommonCodeList", getCommonCodeListInputXML1);
			NodeList commonCodeList1 = outXML1.getElementsByTagName("CommonCode");
			String strCarrierPriority1 = "";

			if (commonCodeList1 != null && !YFCObject.isVoid(commonCodeList1))
			{
				Element commonCode = (Element)commonCodeList1.item(0);
				String codeValue = commonCode.getAttribute("CodeValue");
				if (carrierService.equals(codeValue))
				{
					strCarrierPriority1 = commonCode.getAttribute("CodeShortDescription");
					log.verbose("strCarrierPriority is: " + strCarrierPriority1);
				}

				//preparing the return doc
				outDoc.getDocumentElement().setAttribute("SCAC", scac);
				outDoc.getDocumentElement().setAttribute("CarrierService", carrierService);	

				//comparing the original LOS and the agile returned LOS to determine whether it is a upgrade or downgrade
				int i = Integer.parseInt(strCarrierPriority);			
				int i1 = Integer.parseInt(strCarrierPriority1);

				if(i>i1)
				{
					outDoc.getDocumentElement().setAttribute("ShipUpgrdOrDowngrd","D");
				}
				else if(i<i1)
				{
					outDoc.getDocumentElement().setAttribute("ShipUpgrdOrDowngrd","U");
				}
			}			
		}

		//if the agile returns blank response, calling the agile without filter option and getting the highest LOS
		else
		{
			Element eleShipment = docShipment.getDocumentElement();
			eleShipment.setAttribute("Exceptional", "Y");
			//calling AcademyRateShopService to get the agile response
			Document docPierbridgeResposnse = AcademyUtil.invokeService(env, "AcademyRateShopService", docShipment);

			//getting all the priority of the LOS from the common codes
			Document getCommonCodeListXML = XMLUtil.createDocument("CommonCode");
			getCommonCodeListXML.getDocumentElement().setAttribute("CodeType", "PriorityCarrier");
			Document outgetCommonCodeListXML = AcademyUtil.invokeAPI(env, "getCommonCodeList", getCommonCodeListXML);

			//calling the method getHighest to get the highest LOS from the agile response
			String strServiceLevel = getHighest(docShipment, docPierbridgeResposnse, outgetCommonCodeListXML);

			if(!("".equals(strServiceLevel)))
			{
				outDoc.getDocumentElement().setAttribute("SCAC", "FEDX");
				outDoc.getDocumentElement().setAttribute("CarrierService", strServiceLevel);

				//getting the priority of the highest value fetched earlier
				Document getCommonCodeListInputXML1 = XMLUtil.createDocument("CommonCode");
				getCommonCodeListInputXML1.getDocumentElement().setAttribute("CodeType", "PriorityCarrier");
				getCommonCodeListInputXML1.getDocumentElement().setAttribute("CodeValue", strServiceLevel);
				Document outXML1 = AcademyUtil.invokeAPI(env, "getCommonCodeList", getCommonCodeListInputXML1);
				NodeList commonCodeList1 = outXML1.getElementsByTagName("CommonCode");
				String strCarrierPriority1 = "";

				if (commonCodeList1 != null && !YFCObject.isVoid(commonCodeList1))
				{
					Element commonCode = (Element)commonCodeList1.item(0);
					String codeValue = commonCode.getAttribute("CodeValue");
					if (strServiceLevel.equals(codeValue))
					{
						strCarrierPriority1 = commonCode.getAttribute("CodeShortDescription");
						log.verbose("strCarrierPriority is: " + strCarrierPriority1);
					}
					int i = Integer.parseInt(strCarrierPriority);			
					int i1 = Integer.parseInt(strCarrierPriority1);

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

		NodeList rateLt = docPierbridgeResposnse.getElementsByTagName("Rate");
		if(rateLt.getLength()!=0)
		{
			ArrayList <Integer> rateValues = new ArrayList <Integer>();
			for(int i=0; i<rateLt.getLength(); i++)
			{
				Element eleRate = (Element)rateLt.item(i);
				Element eleServiceType = (Element)eleRate.getElementsByTagName("ServiceType").item(0);
				Element eleDescription = (Element)eleServiceType.getElementsByTagName("Description").item(0);
				String strDescription = eleDescription.getTextContent();

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

				//getting the codevalue for each LOS
				XPathExpression expr= xpath.compile("CommonCodeList/CommonCode[@CodeValue=\"" + strDescription + "\"]");
				Element eleCommonCode = (Element) expr.evaluate(outgetCommonCodeListXML, XPathConstants.NODE);
				//getting the CodeShortDescription to each code value and adding to the arraylist
				if(!YFCObject.isVoid(eleCommonCode))
				{
					String strCodeShortDescription = eleCommonCode.getAttribute("CodeShortDescription");
					rateValues.add(Integer.parseInt(strCodeShortDescription)); 					
				}				
			}

			//getting the maximum value from the arraylist
			int maxValue = Collections.max(rateValues);
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
					//checking for Ground
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
					}						
				}
			}
		}
		else
		{
			finalServiceLevel = "";
		}
		//returning the LOS which has the highest codevalue
		log.verbose("***finalServiceLevel***"+finalServiceLevel);
		return finalServiceLevel;
	}
}