package com.academy.ecommerce.sterling.shipment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyWGCarrierOverrideUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.core.YFSObject;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

/**
 * @author kparvath
 * 
 * Service Name: AcademySFSCreateContainersAndPrintService
 * 
 * Implements the LOS Upgrade/Downgrade logic for Store orders.
 * 
 * /*STL-721*/ 

public class AcademySFSLOSUpgradeDowngradeProcess {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademySFSLOSUpgradeDowngradeProcess.class);
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
	//START : OMNI-9573
	String strAlternateOvernightService = "";
	//END: OMNI-9573
	
	//START : STL-1606 Add USPS to Agile Rate Shop Call During UG/ DG
	String strUSPSAgile = null;
	String strUSPSSterling = null;
	String strPriorityMailAgile = null;
	String strPriorityMailSterling = null;
	String strUSPSWeightConstraint = null;
	String strUSPSScacShipmentType = null;
	String strLOSForUSPSFirstClass = null;
	String strNodeType = "";
	String strNodeTypesForUSPS = null;
	//END : STL-1606 Add USPS to Agile Rate Shop Call During UG/ DG
	
	//Start STL-1671 Shipments less than 9 lb Shall Ship from USPS First Class
	String strUSPSWeightConstraintForPriorityMail = null;
	String strUSPSScacShipmentTypeForPriorityMail = null;
	String strLOSForUSPSPriorityMail = null;
	String strNodeTypesForUSPSPriorityMail = null;
	//END STL-1671 Shipments less than 9 lb Shall Ship from USPS First Class
	
	//EFP-17 EFW Carrier Integration - Start
	String strIsWGCarrierOverrideRequired = null;
	//EFP-17 EFW Carrier Integration - End
	
	//Multibox SKU
	boolean isMultiBoxSku = false;
	int     mbFactor      = 0;
	HashMap<String,Integer> multiBoxMap = new HashMap<String,Integer>();
	
	// OMNI-446 - Begin
	String strTurnOffDowngrade = null;
	String strDisableRateShopForCarrierService = null;
	String strDisableDowngradeForCarrierService = null;
	// OMNI-446 - End
	
	//Begin: OMNI-8714
	String strEnableRateShopForStandardPOBox = null;
	String strNewSCACForStdPOBox = null;
	String strNewCarrierServiceCodeForStdPOBox = null;
	//End: OMNI-8714
	//OMNI-100148 Begin
	String strIsDIMSEnabledForVP=null;
	//OMNI-100148 End
	String strDisableFM2ForCarrierService = "";
	
	public Document shipmentLOSUpgrade(YFSEnvironment env, Document inXML)throws Exception{ 
		
		
		isMultiBoxSku = isItemMultiBoxSku(env, inXML);
		if(isMultiBoxSku){
			
			mbFactor = getMbFactor(env, inXML);
			multiBoxMap.put(XPathUtil.getString(inXML, "Shipment/ShipmentLines/ShipmentLine/OrderLine/Item/@ItemID"), mbFactor);
		}
		//Start WN-187 SOM: After one container is packed, if there are more units to be packed then U/D should be ignored
		String strSkipUpgradeDowngrade = null;
		String strManifestNo = null;
		//End WN-187 SOM: After one container is packed, if there are more units to be packed then U/D should be ignored
		
		//Start WN-1838 Sterling to send DIM weight to Agile
		Element eleShipNodes = null;
		String strNodeType = "";
		String strNodeTypesforDIM = "";
		//End WN-1838 Sterling to send DIM weight to Agile
		
		//Start STL-1548
		String strMaxContainerWeight = props.getProperty(AcademyConstants.STR_MAX_CONTAINER_WEIGHT);
		double dMaxContainerWeight = Double.parseDouble(strMaxContainerWeight);
		String strIgnoreScacForContWeight = props.getProperty(AcademyConstants.STR_IGNORE_SCAC_FOR_CONT_WEIGHT);
		//End STL-1548	
		log.beginTimer(" Begin of AcademySFSLOSUpgradeDowngradeProcess->shipmentLOSUpgrade ()");
		if(log.isVerboseEnabled() ) {
			log.verbose("***inXML***" +XMLUtil.getXMLString(inXML));
		}
		strFEDEX = props.getProperty("FEDEX");
		strGround = props.getProperty("Ground");
		strHomeDelivery = props.getProperty("Home Delivery");
		strStandardOvernight = props.getProperty("Standard Overnight");
		strAlternateOvernightService = props.getProperty("AltOvernightService");

		strSmartPost = props.getProperty("Smart Post");
		str2Day = props.getProperty("2Day");
		
		//EFP-17 Start
		strIsWGCarrierOverrideRequired = props.getProperty(AcademyConstants.STR_IS_WG_CARRIER_OVERRIDE_REQUIRED);
		//EFP-17 End
		
		//START : STL-1606 Add USPS to Agile Rate Shop Call During UG/ DG
		strUSPSAgile = props.getProperty(AcademyConstants.STR_USPS_AGILE); 
		strUSPSSterling = props.getProperty(AcademyConstants.STR_USPS_STERLING);
		strPriorityMailAgile = props.getProperty(AcademyConstants.STR_PRIORITY_MAIL_AGILE);
		strPriorityMailSterling = props.getProperty(AcademyConstants.STR_PRIORITY_MAIL_STERLING);
		
		strUSPSWeightConstraint = props.getProperty(AcademyConstants.STR_USPS_WEIGHT_CONSTRAINT);
		strUSPSScacShipmentType = props.getProperty(AcademyConstants.STR_USPS_SCAC_SHIPMENT_TYPE);
		strLOSForUSPSFirstClass = props.getProperty(AcademyConstants.STR_LOS_FOR_USPS_FIRST_CLASS);		
		strNodeTypesForUSPS = props.getProperty(AcademyConstants.ATTR_NODE_TYPES_FOR_USPS);
		//END : STL-1606 Add USPS to Agile Rate Shop Call During UG/ DG
		
		//Start STL-1671 Shipments less than 9 lb Shall Ship from USPS First Class
		strUSPSWeightConstraintForPriorityMail = props.getProperty(AcademyConstants.STR_USPS_WEIGHT_CONSTRAINT_PRIORITY_MAIL);
		strUSPSScacShipmentTypeForPriorityMail = props.getProperty(AcademyConstants.STR_USPS_SCAC_SHIPMENT_TYPE_PRIORITY_MAIL);
		strLOSForUSPSPriorityMail = props.getProperty(AcademyConstants.STR_LOS_FOR_USPS_PRIORITY_MAIL);
		strNodeTypesForUSPSPriorityMail = props.getProperty(AcademyConstants.ATTR_NODE_TYPES_FOR_USPS_PRIORITY_MAIL);
		boolean isLOSModificationRequire = true;
		if(log.isVerboseEnabled() ) {
			log.verbose("USPSWeightConstraintForPriorityMail:  " +strUSPSWeightConstraintForPriorityMail);
			log.verbose("USPSScacShipmentTypeForPriorityMail:  " +strUSPSScacShipmentTypeForPriorityMail);
			log.verbose("LOSForUSPSPriorityMail:  " +strLOSForUSPSPriorityMail);	
		}

		//END STL-1671 Shipments less than 9 lb Shall Ship from USPS First Class
		
		//Start WN-187 SOM: After one container is packed, if there are more units to be packed then U/D should be ignored
		strSkipUpgradeDowngrade = props.getProperty(AcademyConstants.SKIP_UPGRADE_DOWNGRADE);
		//End WN-187 SOM: After one container is packed, if there are more units to be packed then U/D should be ignored
	
		// OMNI-446 - Begin
		strTurnOffDowngrade = props.getProperty(AcademyConstants.STR_TURN_OFF_DOWNGRADE);
		strDisableRateShopForCarrierService = props.getProperty(AcademyConstants.STR_DISABLE_RATESHOP_FOR_CARRIER_SERVICE);
		strDisableDowngradeForCarrierService = props.getProperty(AcademyConstants.STR_DISABLE_DOWNGRADE_FOR_CARRIER_SERVICE);
		// OMNI-446 - End
		
		//Begin: OMNI-8714
		strEnableRateShopForStandardPOBox = props.getProperty(AcademyConstants.STR_ENABLE_RATESHOP_FOR_STANDARD_PO_BOX);
		strNewSCACForStdPOBox = props.getProperty(AcademyConstants.STR_NEW_SCAC_FOR_STANDARD_PO_BOX);;
		strNewCarrierServiceCodeForStdPOBox = props.getProperty(AcademyConstants.STR_NEW_CARRIER_SERVICE_CODE_FOR_STANDARD_PO_BOX);
		//End: OMNI-8714
		
		//Begin: OMNI-9573 -- Carriers configured in the below service argument will be enabled for Non-Filter Mode rate shop call 
		//only provided it is not configured as part of strDisableRateShopForCarrierService
		strDisableFM2ForCarrierService = props.getProperty(AcademyConstants.STR_DISABLE_FILTERMODE2_FOR_CARRIER_SERVICE);
		//End: OMNI-9573
		
		Document changeShipmentOut = null;
		Document docChangeShipment = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		boolean changeShipmentRequired = false;
		
		String strShipmentNo = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_SHIPMENT_NO);
		String strScac = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_SCAC);//STL-1548

		Document docGetShipmentList = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
		Element getShipmentListEle = docGetShipmentList.getDocumentElement();
		
		
		
		if(!YFCObject.isVoid(env.getTxnObject(AcademyConstants.A_IS_WEB_STORE_FLOW))
				&& YFCCommon.equalsIgnoreCase(AcademyConstants.STR_YES, (String)env.getTxnObject(AcademyConstants.A_IS_WEB_STORE_FLOW)))
		{
			Element rootEle = inXML.getDocumentElement();
			getShipmentListEle.setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,
					rootEle.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY));
		}
		else
		{
			getShipmentListEle.setAttribute(AcademyConstants.ATTR_SHIPMENT_NO, strShipmentNo);
		}
			
			

		env.setApiTemplate("getShipmentList", "global/template/api/getShipmentListForRateShop.xml");
		Document outShipmentListDoc = AcademyUtil.invokeAPI(env, "getShipmentList", docGetShipmentList);
		env.clearApiTemplate("getShipmentList");
		if(log.isVerboseEnabled() ) {
			log.verbose("***outShipmentListDoc***" +XMLUtil.getXMLString(outShipmentListDoc));
		}
		
		//Start WN-187 SOM: After one container is packed, if there are more units to be packed then U/D should be ignored
		strManifestNo = XMLUtil.getString(outShipmentListDoc, AcademyConstants.XPATH_SHIPMENT_MANIFESTNO);		
		if (!YFCObject.isVoid(strManifestNo) && AcademyConstants.STR_YES.equals(strSkipUpgradeDowngrade)) {
			log.info("!!!Shipment "+ strShipmentNo +" already added to manifest, hence ignoring U/D logic");
			return inXML;
		}
		//End WN-187 SOM: After one container is packed, if there are more units to be packed then U/D should be ignored

		Element eleToAddress = null;		
		if(outShipmentListDoc.getDocumentElement().getElementsByTagName("ToAddress").getLength()>0)
			eleToAddress = (Element)outShipmentListDoc.getDocumentElement().getElementsByTagName("ToAddress").item(0);

		Element eleExtn = (Element)eleToAddress.getElementsByTagName("Extn").item(0);
		String extnIsAPOFPO = eleExtn.getAttribute("ExtnIsAPOFPO");
		String extnIsPOBOXADDRESS = eleExtn.getAttribute("ExtnIsPOBOXADDRESS");

		Element eleShipment = (Element)outShipmentListDoc.getDocumentElement().getElementsByTagName("Shipment").item(0);
		String strShipmentKey = eleShipment.getAttribute("ShipmentKey");
		// Stamp ShipmentKey in the changeShipment Input Document.
		Element shipmentElem = docChangeShipment.getDocumentElement();
		shipmentElem.setAttribute("ShipmentKey", strShipmentKey);		
		
		//Start WN-1838 Sterling to send DIM weight to Agile
		eleShipNodes = (Element) eleShipment.getElementsByTagName(AcademyConstants.SHIP_NODE).item(0);
		strNodeType = eleShipNodes.getAttribute(AcademyConstants.ATTR_NODE_TYPE);
		if(log.isVerboseEnabled() ) {
			log.verbose("Node type is :" + strNodeType);
		}
		strNodeTypesforDIM = props.getProperty(AcademyConstants.STR_NODE_TYPES_FOR_DIM);
		if(log.isVerboseEnabled() ) {
			log.verbose("NodeTypesForDIM is :" + strNodeTypesforDIM);
		}
		
		if(strNodeTypesforDIM.contains(strNodeType) && !isMultiBoxSku ){ //Dimension setting for the multibox sku is done in the AcademyrateShopService class.
			if(log.isVerboseEnabled() ) {	
				log.verbose("Calling SetContainerForDimensions");
			}
			    setContainerDimensions(env,inXML,eleShipment);
		}		
		//End WN-1838 Sterling to send DIM weight to Agile
		
		//STL-1548 [Start]
		String strWeight = null;
		if (!isMultiBoxSku){
			strWeight = calculateWeight(inXML);	
		}else{
			strWeight = Double.toString(heaviestMultiBoxItem(env, inXML));                 //Integer.toString(heaviestMultiBoxItem(env, inXML));   ////Setting the weight as zero for multibox and calcilation done in RateShopClass
		}
		if(!strIgnoreScacForContWeight.contains(strScac) && (Double.parseDouble(strWeight) > dMaxContainerWeight)){
			YFSException ex = new YFSException();
			ex.setErrorCode("AcademyConstants.STR_ERROR_CODE_12");
			ex.setErrorDescription("Container weight("+strWeight+") is more than "+ dMaxContainerWeight +" lbs.");
			throw ex;
		}else{
			if(log.isVerboseEnabled() ) {
				log.verbose("Weight configured ::"+strWeight);	
			}
			eleShipment.setAttribute(AcademyConstants.ATTR_EXTN_TOTAL_WEIGHT,strWeight);
			
		}		
		
//		if(!strIgnoreScacForContWeight.contains(strScac) && (Double.parseDouble(strWeight) > dMaxContainerWeight)){
//			YFSException ex = new YFSException();
//			ex.setErrorCode("AcademyConstants.STR_ERROR_CODE_12");
//			ex.setErrorDescription("Container weight("+strWeight+") is more than "+ dMaxContainerWeight +" lbs.");
//			throw ex;
//		}else{
//			log.verbose("Weight configured ::"+strWeight);
//			eleShipment.setAttribute(AcademyConstants.ATTR_EXTN_TOTAL_WEIGHT,strWeight);
//			
//		}		
		//STL-1548 [End]
		
		// STL-904
		String strCompany=eleToAddress.getAttribute(AcademyConstants.ATTR_COMPANY);	
		String strShipmentType=eleShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE);
		// STL-904	
		
		
		Element eleExtnShipment = (Element)eleShipment.getElementsByTagName("Extn").item(0);
		String extnCarrierServiceCode = eleExtnShipment.getAttribute("ExtnOriginalShipmentLos");
		String extnSCAC = eleExtnShipment.getAttribute("ExtnOriginalShipmentScac");
		
		
		//STL-1035 
		String strExtnInvoiceNo= eleExtnShipment.getAttribute(AcademyConstants.ATTR_EXTN_INVOICE_NO);
		if(log.isVerboseEnabled() ) {
			log.verbose("ExtnInvoiceNo : "+ strExtnInvoiceNo);
		}
		if (!YFCObject.isVoid(strExtnInvoiceNo)) {
			Element eleExtnInvoice=(Element) inXML.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
			eleExtnInvoice.setAttribute(AcademyConstants.ATTR_EXTN_INVOICE_NO,strExtnInvoiceNo);
		}				
		//STL-1035
		
		// Start Changes for STL-721
		String extnOverrideUOrD = eleExtnShipment.getAttribute(AcademyConstants.EXTN_ATTR_OVERRIDE_UPGD_OR_DGD);
		if (!YFCObject.isVoid(extnOverrideUOrD) && !extnOverrideUOrD.equalsIgnoreCase(AcademyConstants.STR_YES)) {
			if(log.isVerboseEnabled() ) {
				log.verbose("AcademySFSLOSUpgradeDowngradeProcess_extnOverrideUOrD: " + extnOverrideUOrD);
			}
			// End changes for STL-721

			String strCarrierServiceCode = eleShipment.getAttribute("CarrierServiceCode");
			Document docShipmentForDelryDate = XMLUtil.getDocumentForElement(eleShipment);

//			Document updatedShipmentDoc = AcademyUtil.invokeService(env, "AcademySFSCalculateExpDeliveryDate", docShipmentForDelryDate);

//			log.verbose("**********updatedShipmentDoc ::"+ XMLUtil.getXMLString(updatedShipmentDoc));

			Element eleAdditionalDates = (Element)eleShipment.getElementsByTagName("AdditionalDates").item(0);
			NodeList additionalDateNL = eleAdditionalDates.getElementsByTagName("AdditionalDate");
			String strRequestedDate = "";

			for(int i=0; i<additionalDateNL.getLength(); i++)
			{
				Element eleAdditionalDate = (Element)additionalDateNL.item(i);
				if(log.isVerboseEnabled() ) {
					log.verbose("eleOrderDate - " +XMLUtil.getElementXMLString(eleAdditionalDate));
				}
				if("ACADEMY_DELIVERY_DATE".equals(eleAdditionalDate.getAttribute("DateTypeId")))
				{
					if(log.isVerboseEnabled() ) {
						log.verbose("***inside if***");
					}
					strRequestedDate = eleAdditionalDate.getAttribute("RequestedDate");
					
					if(log.isVerboseEnabled() ) {
						log.verbose("@@@@@strRequestedDate :: "+ strRequestedDate);
					}
				}
			}						
			
			//Start STL-1606 Shipments less than 1 lb Shall Ship USPS First Class
			//Checking Shipment Weight and Carrier service code
			Element eleShipNode = (Element) eleShipment.getElementsByTagName(AcademyConstants.SHIP_NODE).item(0);
			if (!YFCObject.isVoid(eleShipNode)) {
				strNodeType = eleShipNode.getAttribute(AcademyConstants.ATTR_NODE_TYPE);
			}
			String strExtnTotalWeight = eleShipment.getAttribute(AcademyConstants.ATTR_EXTN_TOTAL_WEIGHT);
			
			if (!(YFCObject.isVoid(strUSPSWeightConstraint)) && strNodeTypesForUSPS.contains(strNodeType)
					&& strUSPSScacShipmentType.contains(strShipmentType)
					&& strLOSForUSPSFirstClass.contains(strCarrierServiceCode)
					&& Double.parseDouble(strExtnTotalWeight) <= Double.parseDouble(strUSPSWeightConstraint)) {
				if(log.isVerboseEnabled() ) {
					log.verbose("Weight configured ::"+strExtnTotalWeight);
				}
				shipmentElem.setAttribute(AcademyConstants.ATTR_SCAC, AcademyConstants.STR_USPS_SCAC);
				shipmentElem.setAttribute(AcademyConstants.CARRIER_SERVICE_CODE, AcademyConstants.STR_CARRIER_SERVICE_CODE);
				changeShipmentRequired = true;
				strCarrierServiceCode = AcademyConstants.STR_CARRIER_SERVICE_CODE;				
			}
			// End STL-1606 Shipments less than 1 lb Shall Ship USPS First Class
			//Start STL-1671 Shipments less than 9 lb Shall Ship USPS First Class
			else if (!(YFCObject.isVoid(strUSPSWeightConstraintForPriorityMail)) && strNodeTypesForUSPSPriorityMail.contains(strNodeType)
					&& strUSPSScacShipmentTypeForPriorityMail.contains(strShipmentType)
					&& strLOSForUSPSPriorityMail.contains(strCarrierServiceCode)
					&& Double.parseDouble(strExtnTotalWeight) <= Double.parseDouble(strUSPSWeightConstraintForPriorityMail)) {
				
				if(log.isVerboseEnabled() ) {
					log.verbose("Weight configured ::"+strExtnTotalWeight);
					log.verbose("Setting USPS Priority Mail");			
				}

				shipmentElem.setAttribute(AcademyConstants.ATTR_SCAC, strUSPSSterling);
				shipmentElem.setAttribute(AcademyConstants.CARRIER_SERVICE_CODE, strPriorityMailSterling);
				changeShipmentRequired = true;
				isLOSModificationRequire = false;
			}//END STL-1671 Shipments less than 9 lb Shall Ship USPS First Class
			
			String scacToCompare = extnSCAC;
			String losToCompare = extnCarrierServiceCode;
			Element eleRoot = null;
			if (!YFCObject.isVoid(strRequestedDate)) {
				//STL-735: Added a condition to avoid upgrade/downgrade logic for AMMO shipments.
				if(log.isVerboseEnabled() ) {
					log.verbose("Shipment Type: " + strShipmentType);					
				}
				//Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
				if (!"WG".equals(strShipmentType) && !"GC".equals(strShipmentType) && !"GCO".equals(strShipmentType) 
						&& !AcademyConstants.AMMO_SHIPMENT_TYPE.equals(strShipmentType) 
						&& !AcademyConstants.HAZMAT_SHIPMENT_TYPE.equals(strShipmentType)
						&& !AcademyConstants.STR_CARRIER_SERVICE_CODE.equals(strCarrierServiceCode) //STL-1606 Added a condition to avoid upgrade/downgrade logic for shipments less then 1 lbs
						&& (isLOSModificationRequire) //STL-1671 
						//End WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
						//&& ! "Standard Overnight".equals(strCarrierServiceCode)//Commented as part of STL-1477
						// OMNI-446 - Begin
						&& ((AcademyConstants.STR_YES.equalsIgnoreCase(strTurnOffDowngrade)
								&& !strDisableRateShopForCarrierService.contains(strCarrierServiceCode))
								|| !AcademyConstants.STR_YES.equalsIgnoreCase(strTurnOffDowngrade))
						// OMNI-446 - End
						) {
					if(log.isVerboseEnabled() ) {
						log.verbose("inside upgrade/downgrade");
					}
					if (!"Y".equals(extnIsAPOFPO) && 
							(!"Y".equals(extnIsPOBOXADDRESS) ||
						//Begin: OMNI-8714			
									(AcademyConstants.STR_YES.equals(extnIsPOBOXADDRESS) && 
											AcademyConstants.STR_YES.equals(strEnableRateShopForStandardPOBox)))) {
						//End: OMNI-8714
						Document docShipment = XMLUtil.getDocumentForElement(eleShipment);
						if(log.isVerboseEnabled() ) {
							log.verbose("****The docShipment file which is going to AcademyRateShopService is" + XMLUtil.getXMLString(docShipment));
						}
			                ///Setting the attributes required for altering the RateShop request for multibox sku, which is done in AcadeRateShopService class.
							String multiboxFlag =  isMultiBoxSku ? "Y" : "N";
							docShipment.getDocumentElement().setAttribute("isMultiBoxSku", multiboxFlag);
							docShipment.getDocumentElement().setAttribute("MbFactor", Integer.toString(mbFactor)); 
							docShipment.getDocumentElement().setAttribute("ExtnPickQuantity", XPathUtil.getString(inXML, "Shipment/ShipmentLines/ShipmentLine/@PickQuantity"));
							docShipment.getDocumentElement().setAttribute("ExtnItemID", XPathUtil.getString(inXML, "Shipment/ShipmentLines/ShipmentLine/OrderLine/Item/@ItemID"));
							eleShipment.setAttribute("MbFactor",Integer.toString(mbFactor));
							
					
						//START : OMNI-9573 Condition to check if the carrier service is part of strDisableFM2ForCarrierService
						//If Yes, enable Non-Filter mde call else enable FilterMode2 Call
					    if(strDisableFM2ForCarrierService.contains(strCarrierServiceCode)) {
							docShipment.getDocumentElement().setAttribute("Exceptional", "Y");
					    }
					    //END : OMNI-9573
					    
						Document docPierbridgeResposnse = AcademyUtil.invokeService(env, "AcademyRateShopService", docShipment);
						
						//START : OMNI-12015 Changed argument from extnCarrierServiceCode,extnScac to strCarrierServiceCode,strScac
						Document resultDoc = getCheapestCarrierService(env, strScac, strCarrierServiceCode, docShipment, docPierbridgeResposnse);
						//END : OMNI-12015
						if (!YFCObject.isNull(resultDoc)) {
							eleRoot = resultDoc.getDocumentElement();
							Element eleOutShipment = docChangeShipment.getDocumentElement();
							eleOutShipment.setAttribute("ShipmentKey", strShipmentKey);
							eleOutShipment.setAttribute("SCAC", eleRoot.getAttribute("SCAC"));
							eleOutShipment.setAttribute("CarrierServiceCode", eleRoot.getAttribute("CarrierService"));
							//Start- OMNI-9573
							String extnShipUpgrdOrDowngrd = null;
							Element eleoutExtn = docChangeShipment.createElement("Extn");
							eleoutExtn.setAttribute("ExtnShipUpgrdOrDowngrd", eleRoot.getAttribute("ShipUpgrdOrDowngrd"));
							eleOutShipment.appendChild(eleoutExtn);
							extnShipUpgrdOrDowngrd = eleoutExtn.getAttribute("ExtnShipUpgrdOrDowngrd");
							//OMNI-9573
						
							if(log.isVerboseEnabled() ) {
								log.verbose("CarrierServiceCode :: " + strCarrierServiceCode);
								log.verbose("extnShipUpgrdOrDowngrd :: " + extnShipUpgrdOrDowngrd);
								log.verbose("Standard Overnight should not be Downgraded");
							}

							/* Start - Changes made as part of STL 683 */
							changeShipmentRequired = true;
							/* End - Changes made as part of STL 683 */
							
							/*  OMNI-90977 - START - Remove Upgrade/downgrade alerts
							//OMNI-9573 Added condition for flag "C" when Standard Overnight is changed to Alternate Service Overnight
							if("U".equals(eleRoot.getAttribute("ShipUpgrdOrDowngrd")) || "D".equals(eleRoot.getAttribute("ShipUpgrdOrDowngrd"))
									|| "C".equals(eleRoot.getAttribute("ShipUpgrdOrDowngrd")))
							{
								if(log.isVerboseEnabled() ) {
									log.verbose("Raising Alert");
								}
								Document alertDoc = XMLUtil.createDocument("Shipment");
								Element eleAlertShipment = alertDoc.getDocumentElement();
								eleAlertShipment.setAttribute("ShipmentNo", eleShipment.getAttribute("ShipmentNo"));
								eleAlertShipment.setAttribute("ShipmentKey", strShipmentKey);
								eleAlertShipment.setAttribute("SCAC", eleShipment.getAttribute("SCAC"));
								eleAlertShipment.setAttribute("CarrierServiceCode", eleShipment.getAttribute("CarrierServiceCode"));
								eleAlertShipment.setAttribute("ChangedSCAC", eleRoot.getAttribute("SCAC"));
								eleAlertShipment.setAttribute("ChangedCarrierServiceCode", eleRoot.getAttribute("CarrierService"));
								eleAlertShipment.setAttribute("UpgradedOrDowngraded", eleRoot.getAttribute("ShipUpgrdOrDowngrd"));
								if(log.isVerboseEnabled() ) {
									log.verbose("***alertDoc***"+XMLUtil.getXMLString(alertDoc));
								}
								AcademyUtil.invokeService(env, "RaiseAlertForChangeInLOS", alertDoc);
							}
							//}
							 OMNI-90977 - END - Remove Upgrade/downgrade alerts */

							inXML.getDocumentElement().setAttribute("SCAC", eleRoot.getAttribute("SCAC"));
							inXML.getDocumentElement().setAttribute("CarrierServiceCode", eleRoot.getAttribute("CarrierService"));														
							 					
							
							scacToCompare = eleRoot.getAttribute("SCAC");
							losToCompare = eleRoot.getAttribute("CarrierService");
						}
					}
				}
			}
			/* Start - Changes made as part of STL 683 */
			docChangeShipment = AcademyUtil.updateCompanyToDot(scacToCompare, losToCompare, docChangeShipment, outShipmentListDoc);
			if (docChangeShipment.getDocumentElement().hasAttribute("ChangeShipmentRequired")) {
				changeShipmentRequired = true;
				log.debug("AcademyUtil.updateCompanyToDot()_changeShipmentRequired:" + changeShipmentRequired);
			}
			
			// STL-904 Start Ammo change - check if ShipmentType="AMMO" and address is Commercial then set SCAC="FEDX" and CarrierServiceCode="Ground"
			String ammoChangesRequired = "";
			ammoChangesRequired = validateSFSAmmoShipmentType(strShipmentKey, docChangeShipment,strCompany,strShipmentType);		
			if (ammoChangesRequired.equalsIgnoreCase("True")) {
				changeShipmentRequired = true;
			}			
			// STL-904 End Ammo change
			
			//EFP-17 Start
			log.debug("Shipment Type before WG Changes::"+strShipmentType);
			if(!YFCObject.isVoid(strIsWGCarrierOverrideRequired) 
					&& !YFCObject.isNull(strIsWGCarrierOverrideRequired)
					&& AcademyConstants.STR_YES.equalsIgnoreCase(strIsWGCarrierOverrideRequired)
					&& AcademyConstants.DSV_SHIPMENT_TYPE_WHITEGLOVE.equals(strShipmentType)){
				
				log.debug("strIsWGCarrierOverrideRequired::"+strIsWGCarrierOverrideRequired);
				
				String strZipCode = eleToAddress.getAttribute(AcademyConstants.ZIP_CODE);
				String strShipNode = eleShipment.getAttribute(AcademyConstants.SHIP_NODE);
				String strCarrSvcCode = eleShipment.getAttribute(AcademyConstants.CARRIER_SERVICE_CODE);
				String newScac = AcademyWGCarrierOverrideUtil.findCarrierForWG(env, strZipCode, strCarrSvcCode, strShipNode);
				docChangeShipment.getDocumentElement().setAttribute(AcademyConstants.ATTR_SCAC, newScac);
				changeShipmentRequired = true;
			}
			//EFP-17 End
			
			if (changeShipmentRequired) {
				log.debug("AcademyUtil.updateCompanyToDot()_changeShipment_InXML:" + XMLUtil.getXMLString(docChangeShipment));
				changeShipmentOut = AcademyUtil.invokeAPI(env, AcademyConstants.API_CHANGE_SHIPMENT, docChangeShipment);
			}
			/* End - Changes made as part of STL 683 */
		}
		log.endTimer(" End of AcademySFSLOSUpgradeDowngradeProcess->shipmentLOSUpgrade ()");
		if(log.isVerboseEnabled() ) {
			log.verbose("*** Updated inXML***" +XMLUtil.getXMLString(inXML));			
		}

		return inXML;
	}
	
	private String validateSFSAmmoShipmentType(String shipmentKey, Document docChangeShipment,String strCompany, String strShipmentType)  {				
		
		//Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
		if((AcademyConstants.AMMO_SHIPMENT_TYPE.equals(strShipmentType) || AcademyConstants.HAZMAT_SHIPMENT_TYPE.equals(strShipmentType)) && 
				 !YFCObject.isVoid(strCompany)) {
			//End WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
			docChangeShipment.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, shipmentKey);
			docChangeShipment.getDocumentElement().setAttribute(AcademyConstants.ATTR_SCAC, AcademyConstants.SCAC_FEDX);
			docChangeShipment.getDocumentElement().setAttribute(AcademyConstants.CARRIER_SERVICE_CODE, AcademyConstants.LOS_GROUND);
			if(log.isVerboseEnabled() ) {
				log.verbose("***docChangeShipment_validateSFSAmmoShipmentType()***" +XMLUtil.getXMLString(docChangeShipment));
			}
						
			return "True";
		}
		return "";				
	}
	
	//Start WN-1838 Sterling to send DIM weight to Agile
	
	/**Calculate and set Container dimension for Agile call
	 * @param env
	 * @param inXML
	 * @param eleShipment
	 * @return
	 * @throws Exception
	 */
	public Element setContainerDimensions(YFSEnvironment env,Document inXML, Element eleShipment) throws Exception {
		if(log.isVerboseEnabled() ) {
			log.verbose("Inside setContainerDimensions()");
		}

		Document docGetItemListOut = null;
		Element eleContainerPrimaryInfo = null;
		Element eleShipmentIn = null;
		String strHeight = "";
		String strLength = "";
		String strWidth = "";
		String strContainerType = "";
		String strContainerTypeForDIM = "";
		//OMNI-100148 Begin
		strIsDIMSEnabledForVP=props.getProperty(AcademyConstants.IS_DIMS_ENABLED_FOR_VP);
		log.verbose("DIMS Enabled for Vendor Package : "+strIsDIMSEnabledForVP);
		//OMNI-100148 End
		eleShipmentIn = inXML.getDocumentElement();
		strContainerType = eleShipmentIn.getAttribute(AcademyConstants.ATTR_CONATINER_TYPE);
		strContainerTypeForDIM = props.getProperty(AcademyConstants.ATTR_CONATINER_TYPE_FOR_DIM);
		///String strContainerTypeForMultiBox = props.getProperty(AcademyConstants.ATTR_CONATINER_TYPE_FOR_MB);
		//OMNI-99264 Begin
		if(!YFSObject.isVoid(strContainerType) && (!strContainerTypeForDIM.contains(strContainerType))){
			log.verbose("*** Container Type ** " +strContainerType);
			docGetItemListOut = callGetItemList (env, strContainerType);
			eleContainerPrimaryInfo = (Element) docGetItemListOut.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_PRIMARY_INFO).item(0);
			strHeight = eleContainerPrimaryInfo.getAttribute(AcademyConstants.ATTR_UNIT_HEIGHT);
			strLength = eleContainerPrimaryInfo.getAttribute(AcademyConstants.ATTR_UNIT_LENGTH);
			strWidth = eleContainerPrimaryInfo.getAttribute(AcademyConstants.ATTR_UNIT_WIDTH);
				
			eleShipment.setAttribute(AcademyConstants.ATTR_EXTN_UNIT_HEIGHT, strHeight);
			eleShipment.setAttribute(AcademyConstants.ATTR_EXTN_UNIT_LENGTH, strLength);
			eleShipment.setAttribute(AcademyConstants.ATTR_EXTN_UNIT_WIDTH, strWidth);
			eleShipment.setAttribute(AcademyConstants.ATTR_EXTN_SET_DIM_VALUES,AcademyConstants.STR_YES);
			}
			else if(!YFSObject.isVoid(strContainerType)	&& !YFSObject.isVoid(strIsDIMSEnabledForVP)
			&& strContainerType.equalsIgnoreCase(AcademyConstants.STR_VENDOR_PACKAGE)
			&& strIsDIMSEnabledForVP.equalsIgnoreCase(AcademyConstants.STR_YES))
		{
			Element eleShipmentLine = (Element)inXML.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE).item(0);
			String strItemID=eleShipmentLine.getAttribute(AcademyConstants.ATTR_ITEM_ID);
			docGetItemListOut = callGetItemList (env,strItemID);		
			eleContainerPrimaryInfo = (Element) docGetItemListOut.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_PRIMARY_INFO).item(0);
			strHeight = eleContainerPrimaryInfo.getAttribute(AcademyConstants.ATTR_UNIT_HEIGHT);
			strLength = eleContainerPrimaryInfo.getAttribute(AcademyConstants.ATTR_UNIT_LENGTH);
			strWidth = eleContainerPrimaryInfo.getAttribute(AcademyConstants.ATTR_UNIT_WIDTH);
				
			eleShipment.setAttribute(AcademyConstants.ATTR_EXTN_UNIT_HEIGHT, strHeight);
			eleShipment.setAttribute(AcademyConstants.ATTR_EXTN_UNIT_LENGTH, strLength);
			eleShipment.setAttribute(AcademyConstants.ATTR_EXTN_UNIT_WIDTH, strWidth);
			eleShipment.setAttribute(AcademyConstants.ATTR_EXTN_SET_DIM_VALUES,AcademyConstants.STR_YES);
		}
		//OMNI-99264 End
			
		
		if(log.isVerboseEnabled() ) {
			log.verbose("***inXML with Container Dimensions***" + XMLUtil.getXMLString(inXML));
			log.verbose("**************eleShipment with container dimensions****" +XMLUtil.getXMLString(eleShipment.getOwnerDocument()));
		}
		return eleShipment;		
	}
	
	/** get selected container details.
	 * @param env
	 * @param strContainerType
	 * @return
	 * @throws Exception
	 */
	public Document callGetItemList(YFSEnvironment env,String strContainerType) throws Exception{
		
		Document docGetItemListInput =  null;
		Document docGetItemListOutput =  null;
		
		docGetItemListInput = XMLUtil.createDocument(AcademyConstants.ELEM_ITEM);
		docGetItemListInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_ITEM_ID, strContainerType);
		
		if(log.isVerboseEnabled() ) {
			log.verbose("***GetItemList***" + XMLUtil.getXMLString(docGetItemListInput));
		}
		env.setApiTemplate(AcademyConstants.GET_ITEM_LIST_API, AcademyConstants.STR_XPATH_GET_ITEM_LIST_TEMPLATE);
		docGetItemListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.GET_ITEM_LIST_API, docGetItemListInput);
		env.clearApiTemplate(AcademyConstants.GET_ITEM_LIST_API);
		
		if(log.isVerboseEnabled() ) {
			log.verbose("***docGetItemListOutput***" +XMLUtil.getXMLString(docGetItemListOutput));
		}
		
		return docGetItemListOutput;
		
	}
	//End WN-1838 Sterling to send DIM weight to Agile
	
	//STL-1548 Start 
	/**
	 * Calculating total container weight based on picked quantity and item weight.
	 * @param inXML
	 * @return Container Weight
	 */
	public String calculateWeight(Document inXML)
	{
		 Element elePayloadDoc = inXML.getDocumentElement();		 
		 Element eleShipmentLinesDetails = null;
		 Element elePrimaryInfo = null;
		 NodeList nlPrimaryInfo = null;
		 NodeList nlShipmentLines = null;
		 String strPickQty = null;
		 String strWeight = null;
		 Double dTotalWeight = 0.0;
		 String strContainerWeight = null;
		 Double dContainerWeight=0.0;
		 
		//START: STL-1682 USPS - Actual Weight
		 String strWeightRefernce = props.getProperty(AcademyConstants.STR_WEIGHT_PREFERNCE); 
		 if(log.isVerboseEnabled() ) {
			 log.verbose("LOSForUSPSPriorityMail:  " +strWeightRefernce);
		 }
		 //START STL-1723 Exceed Maximum Allowed CI for Vendor Package
		 strContainerWeight = inXML.getDocumentElement().getAttribute(AcademyConstants.ATTR_CONTAINER_GROSS_WEIGHT);
		 
		 //START STL-1731 to avoid number format exception in case of complete Short pick.
		 //dContainerWeight = Double.parseDouble(strContainerWeight);
		 dContainerWeight = (!YFSObject.isVoid(strContainerWeight) ? Double.parseDouble(strContainerWeight) : 0.0);
		 //END STL-1731
		 if (!YFSObject.isVoid(strWeightRefernce) && AcademyConstants.ATTR_CONTAINER_GROSS_WEIGHT.equalsIgnoreCase(strWeightRefernce)&& dContainerWeight>0) {
			 if(log.isVerboseEnabled() ) {
				 log.verbose("	 ::"+strContainerWeight);
			 }
			 return strContainerWeight;
		 }
		//END STL-1723 Exceed Maximum Allowed CI for Vendor Package
		 else 
		 {//END: STL-1682 USPS - Actual Weight
			 nlShipmentLines = elePayloadDoc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINE);	
			 for(int iCount=0;iCount<nlShipmentLines.getLength();iCount++)
			 {
				 eleShipmentLinesDetails = (Element)nlShipmentLines.item(iCount);
				 strPickQty = eleShipmentLinesDetails.getAttribute(AcademyConstants.ELE_PICK_QTY);
				 nlPrimaryInfo = eleShipmentLinesDetails.getElementsByTagName(AcademyConstants.ELE_PRIMARY_INFO);
				 elePrimaryInfo = (Element)nlPrimaryInfo.item(0);
				 strWeight = elePrimaryInfo.getAttribute(AcademyConstants.ELE_UNIT_WEIGHT);
				 dTotalWeight = dTotalWeight+(Double.parseDouble(strWeight)*Double.parseDouble(strPickQty));
			 }

			 strContainerWeight = Double.toString(dTotalWeight);
			 if(log.isVerboseEnabled() ) {
				 log.verbose("Item Weight ::"+strContainerWeight);
			 }
		 }
			  
		 return strContainerWeight;
	}
	//STL-1548 End

	public Document getCheapestCarrierService(YFSEnvironment env, String inSCAC, String inCarrierServiceCode, Document docShipment, Document peirbridgeResponse) throws Exception{
		if(log.isVerboseEnabled() ) {
			log.verbose("getCheapestCarrierService :: inSCAC "+ inSCAC);
			log.verbose("getCheapestCarrierService :: inCarrierServiceCode "+ inCarrierServiceCode);
		 }
		
		String scac = null;
		String strCode = null;
		NodeList rateLt = peirbridgeResponse.getElementsByTagName("Rate");
		Element tempRateElement = null;
		Document outDoc = YFCDocument.createDocument("Out").getDocument();
		
		//Begin: OMNI-8714
		String extnIsPOBOXADDRESS = "";
		Element eleToAddress = (Element) docShipment.getDocumentElement().getElementsByTagName(AcademyConstants.ELEM_TOADDRESS).item(0);
		if(null != eleToAddress) {
			Element eleExtn = (Element)eleToAddress.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);
			extnIsPOBOXADDRESS = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_IS_PO_BOX);
		}
		//End: OMNI-8714
		
		
		Document getCommonCodeListInputXML = XMLUtil.createDocument("CommonCode");
		getCommonCodeListInputXML.getDocumentElement().setAttribute("CodeType", "PriorityCarrier");
		getCommonCodeListInputXML.getDocumentElement().setAttribute("CodeValue", inCarrierServiceCode);
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
				if(log.isVerboseEnabled() ) {
					log.verbose("strCarrierPriority is: inCarrierServiceCode" + strCarrierPriority);
				}
			}
		}
		String carrierService = "";
		//START : STL-1481 : Added the logic to handle the error response from Agile. while upgrading		
		if(log.isVerboseEnabled() ) {
			log.verbose("before rateLt.getLength() == 1");
		}
		if(rateLt.getLength() == 1)
		{
			if(log.isVerboseEnabled() ) {
				log.verbose("Inside rateLt.getLength() == 1");
			}
			strCode  = peirbridgeResponse.getElementsByTagName(AcademyConstants.ELE_AGILE_CODE).item(0).getTextContent();
			if(log.isVerboseEnabled() ) {
				log.verbose("Status Code from Agile response"+strCode);
			}
			//Agile Status Code=1=Success Code=0=Error
			if(AcademyConstants.STR_AGILE_CODE_VALUE_SUCCESS.equals(strCode))
			{
				tempRateElement = (Element)rateLt.item(0);
				scac = tempRateElement.getElementsByTagName(AcademyConstants.ATTR_SCAC).item(0).getTextContent();
				//Start- OMNI-9573 
				Element serviceType = (Element)tempRateElement.getElementsByTagName("ServiceType").item(0);
				carrierService = serviceType.getElementsByTagName("Description").item(0).getTextContent();
				log.verbose("Carrier service returned from Agile Call  : " + carrierService);
				//END : OMNI-9573
			}
		}
		//OMNI-9573 Condition added to check Alternate Service Overnight was returned, if Yes, invoke Non-Filter Mode
		if(!YFSObject.isVoid(scac) && !carrierService.contains(strAlternateOvernightService))
		{
			if(log.isVerboseEnabled() ) {
				log.verbose("scac:\t"+scac);
			}
			/*tempRateElement = (Element)rateLt.item(0);
			String scac = tempRateElement.getElementsByTagName("SCAC").item(0).getTextContent();*/
			//END : STL-1481 : Added the logic to handle the error response from Agile.
			if(scac.contains(strFEDEX))
			{
				scac="FEDX";
			}
			if("UPS".equals(scac))
			{
				scac="UPSN";
			}
			//START : STL-1606 Add USPS to Agile Rate Shop Call During UG/ DG
			if(scac.contains(strUSPSAgile))
			{
				scac=strUSPSSterling;
			}
			//END : STL-1606 Add USPS to Agile Rate Shop Call During UG/ DG
			
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

					//carrierService = strGround;
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
					}

					//carrierService = strHomeDelivery;
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
			//START : STL-1606 Add USPS to Agile Rate Shop Call During UG/ DG
			else if(carrierService.contains(strPriorityMailAgile)){
				carrierService = strPriorityMailSterling;
			}
			//END : STL-1606 Add USPS to Agile Rate Shop Call During UG/ DG

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
					if(log.isVerboseEnabled() ) {
						log.verbose("strCarrierPriority is: carrierService" + strCarrierPriority1);
					}
				}
				outDoc.getDocumentElement().setAttribute("SCAC", scac);
				outDoc.getDocumentElement().setAttribute("CarrierService", carrierService);	

				int i = Integer.parseInt(strCarrierPriority);			
				int i1 = Integer.parseInt(strCarrierPriority1);

				if(i>i1)
				{
					
					// OMNI-446 - Begin
					if(!AcademyConstants.STR_YES.equalsIgnoreCase(strTurnOffDowngrade)
							|| (AcademyConstants.STR_YES.equalsIgnoreCase(strTurnOffDowngrade)
									&& !strDisableDowngradeForCarrierService.contains(inCarrierServiceCode))) {
						log.verbose("if(i>i1)");
						outDoc.getDocumentElement().setAttribute("ShipUpgrdOrDowngrd","D");
					}else {
						
						log.verbose("Stopping Downgrade::");
						outDoc = null;
					}
					// OMNI-446 - End
				}
				else if(i<i1)
				{
					log.verbose("	else if(i<i1)");
					outDoc.getDocumentElement().setAttribute("ShipUpgrdOrDowngrd","U");
				}
				
				//Begin: OMNI-8714
				if(AcademyConstants.STR_YES.equals(extnIsPOBOXADDRESS)
						&& strNewSCACForStdPOBox.contains(scac)
						&& strNewCarrierServiceCodeForStdPOBox.contains(carrierService)) {
					
					outDoc.getDocumentElement().setAttribute("ShipUpgrdOrDowngrd","C");
				}
				//End: OMNI-8714
			}
			//int carrierLength = carrierService.length();
			//String strCarrier = carrierService.substring(0,carrierLength-2);
			//int iFirstSpaceOccu = strCarrier.indexOf(" ");
			//strCarrier = strCarrier.substring(++iFirstSpaceOccu);


		}
		else
		{
			if(log.isVerboseEnabled() ) {
				log.verbose("Agile gave more than one suggestions on LOS");
			}
			
			//START : OMNI-9573 Ignore rate shop call in Non-Filter mode as it is already invoked previously 

			Document docPierbridgeResposnse = null;
			
			if(!strDisableFM2ForCarrierService.contains(inCarrierServiceCode)) {
				Element eleShipment = docShipment.getDocumentElement();
				eleShipment.setAttribute("Exceptional", "Y");	
				docPierbridgeResposnse = AcademyUtil.invokeService(env, "AcademyRateShopService", docShipment);
			} else {
				docPierbridgeResposnse = peirbridgeResponse;
			}
			
			String strServiceLevel = "";
			if(carrierService.contains(strAlternateOvernightService) || inCarrierServiceCode.equalsIgnoreCase(strStandardOvernight)) {
				strServiceLevel = getAvailableOvernightService(inCarrierServiceCode, docPierbridgeResposnse);
				if(YFCCommon.isVoid(strServiceLevel)) {
					log.verbose("*********** strServiceLevel returned from getAvailableOvernightService() is blank"
							+ "\nChecking for available carrier services from PriorityNoFilterMode CommonCode");
				}
				log.verbose("*********** strServiceLevel returned from getAvailableOvernightService()" + strServiceLevel);
			}
			
			if(YFCCommon.isVoid(strServiceLevel)){
				//END : OMNI-9573

				Document getCommonCodeListXML = XMLUtil.createDocument("CommonCode");
				//Start: changes as part of STL-1477
				/*getCommonCodeListXML.getDocumentElement().setAttribute("CodeType", "PriorityCarrier");*/
				if(AcademyConstants.STR_YES.equals(extnIsPOBOXADDRESS) &&
						AcademyConstants.STR_YES.equals(strEnableRateShopForStandardPOBox)) {
					getCommonCodeListXML.getDocumentElement().setAttribute("CodeType", AcademyConstants.COMMON_CODE_PRIORITY_NO_FILTER_MODE_STD_PO_BOX);
				}else {
					getCommonCodeListXML.getDocumentElement().setAttribute("CodeType", AcademyConstants.COMMON_CODE_PRIORITY_NO_FILTER_MODE);
				}

				//End: changes as part of STL-1477
				Document outgetCommonCodeListXML = AcademyUtil.invokeAPI(env, "getCommonCodeList", getCommonCodeListXML);

				if(log.isVerboseEnabled() ) {
					log.verbose("calling getHighest method");
				}
				strServiceLevel = getHighest(docShipment, docPierbridgeResposnse, outgetCommonCodeListXML);
				
				if(log.isVerboseEnabled() ) {
					log.verbose("*********** strServiceLevel returned from getHighest()" + strServiceLevel);
					log.verbose("after calling getHighest method");				
				}
			}
			if(!("".equals(strServiceLevel)))
			{
				//START : STL-1606 Add USPS to Agile Rate Shop Call During UG/ DG
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
				if(log.isVerboseEnabled() ) {
					log.verbose("SCAC is :::"+ scac);					
				}
				outDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_SCAC, scac);
				//END : STL-1606 Add USPS to Agile Rate Shop Call During UG/ DG	
				outDoc.getDocumentElement().setAttribute("CarrierService", strServiceLevel);
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
						if(log.isVerboseEnabled() ) {
							log.verbose("strCarrierPriority is: " + strCarrierPriority1);							
						}
					}
					int i = Integer.parseInt(strCarrierPriority);			
					int i1 = Integer.parseInt(strCarrierPriority1);

					if(i>i1)
					{
						// OMNI-446 - Begin
						if(!AcademyConstants.STR_YES.equalsIgnoreCase(strTurnOffDowngrade)
								|| (AcademyConstants.STR_YES.equalsIgnoreCase(strTurnOffDowngrade)
										&& !strDisableDowngradeForCarrierService.contains(inCarrierServiceCode))) {
							log.verbose("if(i>i1)");
							outDoc.getDocumentElement().setAttribute("ShipUpgrdOrDowngrd","D");
						}else {
							
							log.verbose("Stopping Downgrade::");
							outDoc = null;
						}
						// OMNI-446 - End
					}
					else if(i<i1)
					{
						log.verbose("else if(i<i1)");
						outDoc.getDocumentElement().setAttribute("ShipUpgrdOrDowngrd","U");
					}
					
					//Begin: OMNI-8714
					if(AcademyConstants.STR_YES.equals(extnIsPOBOXADDRESS)
							&& strNewSCACForStdPOBox.contains(scac)
							&& strNewCarrierServiceCodeForStdPOBox.contains(strServiceLevel)) {
						
						outDoc.getDocumentElement().setAttribute("ShipUpgrdOrDowngrd","C");
					}
					//End: OMNI-8714
					
					//START : OMNI-9573 Update flag as "C" if Standard Overnight is updated to Alternate Service Overnight 
					String strOriginalServiceLevel = docShipment.getDocumentElement().getAttribute("CarrierServiceCode");
					
					if(strOriginalServiceLevel.equalsIgnoreCase(strStandardOvernight) && strServiceLevel.equalsIgnoreCase(strAlternateOvernightService)) {
						outDoc.getDocumentElement().setAttribute("ShipUpgrdOrDowngrd","C");
					}
					//END : OMNI-9573
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

	private String getAvailableOvernightService(String inCarrierServiceCode, Document docPierbridgeResposnse) {
		String strCode = null;

		NodeList rateLt = docPierbridgeResposnse.getElementsByTagName("Rate");
		if(rateLt.getLength() != 0) {
			strCode  = docPierbridgeResposnse.getElementsByTagName(AcademyConstants.ELE_AGILE_CODE).item(0).getTextContent();
			log.verbose("Status Code from Agile response"+strCode);			
		}

		//Agile Status Code=1=Success Code=0=Error
		if(AcademyConstants.STR_AGILE_CODE_VALUE_SUCCESS.equals(strCode))
		{
			for(int i=0; i<rateLt.getLength(); i++)
			{
				Element eleRate = (Element)rateLt.item(i);
				Element eleServiceType = (Element)eleRate.getElementsByTagName("ServiceType").item(0);
				Element eleDescription = (Element)eleServiceType.getElementsByTagName("Description").item(0);
				String strDescription = eleDescription.getTextContent();
				if(strDescription.contains(strStandardOvernight)) {
					strDescription = strStandardOvernight;
					inputServiceLevels.add(strDescription);
					break;
				}
				else if(strDescription.contains(strAlternateOvernightService)) {
					strDescription = strAlternateOvernightService;
					inputServiceLevels.add(strDescription);
				}

			}
		}
		
		if (inputServiceLevels.contains(strStandardOvernight)) {
			finalServiceLevel = strStandardOvernight;			
		} else if(inputServiceLevels.contains(strAlternateOvernightService)) {
			finalServiceLevel = strAlternateOvernightService;				
		} else	{
			log.verbose("Assigning blank value as Final Service Level");
			finalServiceLevel = "";
		}
		log.verbose("***finalServiceLevel***" + finalServiceLevel);
		
		return finalServiceLevel;
	}


	public String getHighest(Document docShipment,Document docPierbridgeResposnse,Document outgetCommonCodeListXML) throws Exception 
	{
		log.verbose("inside getHighest method ***********");
		XPath xpath = XPathFactory.newInstance().newXPath();
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
			ArrayList <Integer> rateValues = new ArrayList <Integer>();
			for(int i=0; i<rateLt.getLength(); i++)
			{
				Element eleRate = (Element)rateLt.item(i);
				Element eleServiceType = (Element)eleRate.getElementsByTagName("ServiceType").item(0);
				Element eleDescription = (Element)eleServiceType.getElementsByTagName("Description").item(0);
				String strDescription = eleDescription.getTextContent();
				//log.verbose("***strDescription***"+strDescription);

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

				//START : STL-1606 Add USPS to Agile Rate Shop Call During UG/ DG	
				else if(strDescription.contains(strPriorityMailAgile))
				{
					strDescription = strPriorityMailSterling;
					inputServiceLevels.add(strDescription);
				}
				
				//END : STL-1606 Add USPS to Agile Rate Shop Call During UG/ DG
				XPathExpression expr= xpath.compile("CommonCodeList/CommonCode[@CodeValue=\"" + strDescription + "\"]");
				Element eleCommonCode = (Element) expr.evaluate(outgetCommonCodeListXML, XPathConstants.NODE);
				if(!YFCObject.isVoid(eleCommonCode))
				{
					String strCodeShortDescription = eleCommonCode.getAttribute("CodeShortDescription");
					log.verbose("***strCodeShortDescription***"+strCodeShortDescription);
					rateValues.add(Integer.parseInt(strCodeShortDescription)); 					
				}				
			}


			int maxValue = Collections.max(rateValues);
			
			XPathExpression expr1= xpath.compile("CommonCodeList/CommonCode[@CodeShortDescription=\"" + String.valueOf(maxValue) + "\"]");
			NodeList commonCodeNL = (NodeList) expr1.evaluate(outgetCommonCodeListXML, XPathConstants.NODESET);
			log.verbose("***length***"+commonCodeNL.getLength());
			for(int j=0; j<commonCodeNL.getLength(); j++)
			{
				Element eleCommonCode = (Element)commonCodeNL.item(j);
				String strCodeValue = eleCommonCode.getAttribute("CodeValue");
				log.verbose("***CodeValue***" + strCodeValue);
				if (inputServiceLevels.contains(strCodeValue))
				{
					log.verbose("Assigning Code value as Final Service Level");
					finalServiceLevel = strCodeValue;
					
									
					/*if (strCodeValue.equals("Ground")) {
						// log.verbose("inside Ground");
						if (!("".equals(XMLUtil.getString(docShipment, "Shipment/ToAddress/@Company")))) {
							// log.verbose("***Company inside
							// Ground***"+XMLUtil.getString(docShipment,
							// "Shipment/ToAddress/@Company"));
							//finalServiceLevel = "Ground";
						}
						/*
					 * else { //log.verbose("***Company inside
					 * Ground***"+XMLUtil.getString(docShipment,
					 * "Shipment/ToAddress/@Company")); finalServiceLevel =
					 * "Home Delivery"; }
					 */
					/*} else if (strCodeValue.equals("Home Delivery")) {
						// log.verbose("inside Home Delivery");
						if ("".equals(XMLUtil.getString(docShipment, "Shipment/ToAddress/@Company"))) {
							// log.verbose("***Company inside Home
							// Delivery***"+XMLUtil.getString(docShipment,
							// "Shipment/ToAddress/@Company"));
							//finalServiceLevel = "Home Delivery";
						}
						/*
					 * else { //log.verbose("***Company inside
					 * Ground***"+XMLUtil.getString(docShipment,
					 * "Shipment/ToAddress/@Company")); finalServiceLevel =
					 * "Ground"; }
					 */
					/*}
					else {
						finalServiceLevel = strCodeValue;
					}*/						
				}
			}
		}
		else
		{
			log.verbose("Assigning blank value as Final Service Level");
			finalServiceLevel = "";
		}
		log.verbose("***finalServiceLevel***" + finalServiceLevel);
		return finalServiceLevel;
	}
	
///Multibox SKU
	
	
	private boolean isItemMultiBoxSku(YFSEnvironment env, Document inXML)throws Exception{
		
		log.verbose("Checking if the item is a multibox item");
		String flag = XPathUtil.getString(inXML, "Shipment/ShipmentLines/ShipmentLine/OrderLine/Item/Extn/@ExtnMultibox");
		log.verbose("Multibox flag is "+flag);
		return flag.equalsIgnoreCase("Y") ? true : false;	
	}
	
	private int getMbFactor(YFSEnvironment env, Document inXML) throws Exception{
		
		log.verbose("Finding the MbFactor");
		String   itemID = XPathUtil.getString(inXML, "Shipment/ShipmentLines/ShipmentLine/OrderLine/Item/@ItemID");
		Document inputDoc = XMLUtil.createDocument("AcadMultiboxLookup");
		inputDoc.getDocumentElement().setAttribute("ItemID", itemID);
		Document getMbFactorOut =  AcademyUtil.invokeService(env, "AcademyGetMultiboxLookupList", inputDoc);
		Element  acadMultiboxLookupElement = (Element) getMbFactorOut.getDocumentElement().getElementsByTagName("AcadMultiboxLookup").item(0);
		String   mbFactor= acadMultiboxLookupElement.getAttribute("MbFactor");
		log.verbose("MbFactor for " + itemID + "is " + mbFactor);
		return   Integer.parseInt(mbFactor);

	}
//	private Element setContainerDimensionsForMultibox(YFSEnvironment env,Document inXML, Element eleShipment) throws Exception {
//		log.verbose("Inside setContainerDimensionsForMultibox()");
//				
//		String   itemID = XPathUtil.getString(inXML, "Shipment/ShipmentLines/ShipmentLine/OrderLine/Item/@ItemID");
//		Document inputDoc = XMLUtil.createDocument("AcadMultiboxLookup");
//        inputDoc.getDocumentElement().setAttribute("ItemID", itemID);
//        Element getMbFactorOutElement =  AcademyUtil.invokeService(env, "getAcadMultiboxLookup", inputDoc).getDocumentElement();
//        
//        String strHeight = getMbFactorOutElement.getAttribute("Height");
//        String strLength = getMbFactorOutElement.getAttribute("Width");
//        String strWidth  = getMbFactorOutElement.getAttribute("Length");
//        String strWeight = getMbFactorOutElement.getAttribute("Weight");
//			
//		eleShipment.setAttribute(AcademyConstants.ATTR_EXTN_UNIT_HEIGHT, strHeight);
//		eleShipment.setAttribute(AcademyConstants.ATTR_EXTN_UNIT_LENGTH, strLength);
//		eleShipment.setAttribute(AcademyConstants.ATTR_EXTN_UNIT_WIDTH, strWidth);
//		eleShipment.setAttribute(AcademyConstants.ATTR_EXTN_SET_DIM_VALUES,AcademyConstants.STR_YES);
//			
//		log.verbose("***inXML with Container Dimensions***" + XMLUtil.getXMLString(inXML));
//		return eleShipment;		
//	}
	
//	private String calculateWeightForMultiBoString(YFSEnvironment env,Document inXML) throws Exception{
//		
//		log.verbose("Inside calculateWeightForMultiBoString method ");
//		String   itemID = XPathUtil.getString(inXML, "Shipment/ShipmentLines/ShipmentLine/OrderLine/Item/@ItemID");
//		Document inputDoc = XMLUtil.createDocument("AcadMultiboxLookup");
//		inputDoc.getDocumentElement().setAttribute("ItemID", itemID);
//		Document getMbFactorOut =  AcademyUtil.invokeService(env, "getAcadMultiboxLookup", inputDoc);
//		String   weight = getMbFactorOut.getDocumentElement().getAttribute("Weight");
//		log.verbose("Weight calculated as "+ weight);
//		return weight;
//	}
	
	
	private Element setIdentifiersForMultibox(YFSEnvironment env,Document inXML, Element eleShipment) throws Exception {
		log.verbose("Inside setIdentifiersForMultibox()");		
		String   itemID = XPathUtil.getString(inXML, "Shipment/ShipmentLines/ShipmentLine/OrderLine/Item/@ItemID");
		eleShipment.setAttribute("ExtnItemID", itemID);
		eleShipment.setAttribute("MbFactor",Integer.toString(mbFactor));
		eleShipment.setAttribute("ExtnPickQuantity", XPathUtil.getString(inXML, "Shipment/ShipmentLines/ShipmentLine/@PickQuantity"));
		if(log.isVerboseEnabled() ) {
			log.verbose("***eleShipment with Mulitbox Identifiers***" + XMLUtil.getXMLString(eleShipment.getOwnerDocument()));
		}
		return eleShipment;		
	}
	
		
	
private double heaviestMultiBoxItem(YFSEnvironment env, Document inXML) throws Exception{
	    double   weight= 0;
	    double heaviestWeight = 0;
		String   itemID = XPathUtil.getString(inXML, "Shipment/ShipmentLines/ShipmentLine/OrderLine/Item/@ItemID");
		Document inputDoc = XMLUtil.createDocument("AcadMultiboxLookup");
		inputDoc.getDocumentElement().setAttribute("ItemID", itemID);
		Document getMbFactorOut =  AcademyUtil.invokeService(env, "AcademyGetMultiboxLookupList", inputDoc);
		NodeList  acadMultiboxLookupList =getMbFactorOut.getDocumentElement().getElementsByTagName("AcadMultiboxLookup");
		for(int i = 0; i < acadMultiboxLookupList.getLength() ; i++){
			Element acadMultiboxLookupElement = (Element) acadMultiboxLookupList.item(i);
			 weight=Double.parseDouble(acadMultiboxLookupElement.getAttribute("Weight")); 
			 if(weight>heaviestWeight){
				 heaviestWeight = weight;
			 }
		}
		log.verbose("Heaviest weight is " +heaviestWeight);
		return   heaviestWeight;

	}
	
}