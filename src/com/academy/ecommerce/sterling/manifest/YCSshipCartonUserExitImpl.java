package com.academy.ecommerce.sterling.manifest;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.shared.ycp.YFSContext;
import com.yantra.ycs.japi.ue.YCSshipCartonUserExit;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;

/**
 * The userExit was implemented but was not being used.This UE is used to fix
 * the Special character issue in the agile input. The 9 digit ZipCode is
 * trimmed to 5 digit so that agile call for SmartPost would not fail. Fix
 * introduced as part of STL 365
 * 
 */
public class YCSshipCartonUserExitImpl implements YCSshipCartonUserExit {
	private static final YFCLogCategory log = YFCLogCategory
	.instance(YCSshipCartonUserExitImpl.class);

	public String shipCarton(YFSContext arg0, String arg1)
	throws YFSUserExitException {
		log
		.beginTimer(" Begining of YCSshipCartonUserExitImpl-> shipCarton()Api");
		YFSEnvironment env = arg0.getEnvironment();
		Document inDoc = null;

		inDoc = YFCDocument.getDocumentFor(arg1).getDocument();
		log.verbose("Input to the code :: " + XMLUtil.getXMLString(inDoc));
		String sConsigneePostalCode = "";
		String sZipcode = "";
		String sCarrier = "";
		Element inElem = inDoc.getDocumentElement();
		String sPrinterType = "Response";
		String sShipperPostalCode= null;
		String sPostalCode=null;
		String sShipperZipCode=null;
		String sShipZipcode= null;

		// STL 365

		if (!YFCObject.isVoid(inElem)) {
			sCarrier = inElem.getAttribute("Carrier");
			log.verbose("The carrier Service is ::" + sCarrier);
			Element PackageDetail = XMLUtil.getFirstElementByName(inElem,
					"PackageLevelDetail");
		//START:Changes for BOPIS-1888 : Trimming the zipcode to 5 digit for all carriers to avoid the Carrier Intgation failure with error due to 5+4 zipcode
		if ("SmartPost".equals(sCarrier)) {
				log.beginTimer(" The carrier Service is :: " + sCarrier);
				sConsigneePostalCode = PackageDetail
				.getAttribute("ConsigneePostalCode");
				if (!YFCObject.isVoid(sConsigneePostalCode)) {
					sZipcode = sConsigneePostalCode.substring(0, 5);
					log.beginTimer(" The trimmed PostalCode/ZipCode is :: "
							+ sZipcode);
					PackageDetail.setAttribute("ConsigneePostalCode", sZipcode);
				}
		       }
// END:Changes for BOPIS-1888 : Trimming the zipcode to 5 digit for all carriers to avoid the Carrier Intgation failure with error due to 5+4 zipcode		

				/*
				 * Start - OMNI-54149
				 * Fulfillment- Fix the City Name - special character apostrophe issue
				 * */
				if (sCarrier.startsWith("USPS")) {
					String sConsigneeCity = PackageDetail.getAttribute("ConsigneeCity");
					log.verbose("ConsigneeCity Before Change :: " + sConsigneeCity);
					log.verbose("Special Character List :: " + AcademyConstants.CITY_SPECIAL_CHAR);
					Pattern specialCharacters = Pattern.compile(AcademyConstants.CITY_SPECIAL_CHAR);
					Matcher mCityHasSplChar = specialCharacters.matcher(sConsigneeCity);
					if (!YFCObject.isVoid(sConsigneeCity) && mCityHasSplChar.find()) {
						String sCity = sConsigneeCity.replaceAll(AcademyConstants.CITY_SPECIAL_CHAR, " ");
						PackageDetail.setAttribute("ConsigneeCity", sCity);
						log.verbose("ConsigneeCity After Change :: " + sCity);
					}
				}
				//End - OMNI-54149
		}

		// STl 365

		// STL-1647 :: START

		// Below is the logic to stamp 'Adult Signature Required' attribute in
		// the input to the Pierbridge Adaptor
		HashMap<String, String> hmpSignReqdAtDelivery = new HashMap<String, String>();
		try {
			// Calling CommonCodeList with CodeType='SIGN_REQD_AT_DLVRY'. This
			// CommonCode will have CodeValues which will used in code to check
			// whether signature is required and for which all ShipmentTypes.

			hmpSignReqdAtDelivery = AcademyCommonCode
			.getCommonCodeListAsHashMap(env,
					AcademyConstants.COMMONCODETYPE_SIGN_REQD_AT_DLVRY,
					AcademyConstants.PRIMARY_ENTERPRISE);

			// Check if Signature is required
			if (AcademyConstants.STR_YES.equalsIgnoreCase(hmpSignReqdAtDelivery
					.get(AcademyConstants.SIGN_REQD))) {

				log.verbose("Signature Reqd is true ...");

				// Calling getShipmentList API to get the ShipmentType
				Element elePackageLevelDetail = XMLUtil.getFirstElementByName(inElem, AcademyConstants.ELEMENT_PACKAGE_LEVEL_DETAIL);
				String strShipmentKey = elePackageLevelDetail.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
				if (!YFCObject.isVoid(strShipmentKey)) {
					Document getShipmentListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
					getShipmentListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
					Document outputTemplate = YFCDocument.getDocumentFor("<Shipments><Shipment ShipmentType=''> </Shipment></Shipments>").getDocument();
					env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST,outputTemplate);
					log.verbose("getShipmentList API input - "+ XMLUtil.getXMLString(getShipmentListInDoc));
					Document getShipmentListOutDoc = AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_SHIPMENT_LIST,getShipmentListInDoc);
					env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
					log.verbose("getShipmentList API output - "+ XMLUtil.getXMLString(getShipmentListOutDoc));

					String strShipmentType = XPathUtil.getString(getShipmentListOutDoc.getDocumentElement(),"/Shipments/Shipment/@ShipmentType");
					log.verbose("ShipmentType - " + strShipmentType);
					// Checking if the ShipmentType needs Signature Reqd on
					// delivery from the list of shipments configured in the
					// common code
					if (!YFCObject.isVoid(strShipmentType)&& hmpSignReqdAtDelivery.containsKey(strShipmentType)) {
						log.verbose("ShipmentType needs Signature Reqd ...");
						Element eleSpecialServices = XMLUtil.getFirstElementByName(inElem,AcademyConstants.ELEMENT_SPECIAL_SERVICES);
						Element eleSpecialService = inDoc.createElement(AcademyConstants.ELEMENT_SPECIAL_SERVICE);
						eleSpecialService.setAttribute(AcademyConstants.ATTR_SERVICE,hmpSignReqdAtDelivery.get(strShipmentType));
						eleSpecialServices.appendChild(eleSpecialService);

					}
				}
			}

		} catch (Exception e) {	
			log.verbose("Exception inside YCSshipCartonUserExitImpl.shipCarton()");
			log.verbose("Exception in Signature Required");
			e.printStackTrace();
		}

		// STL-1647 :: END

		//EFP-8 Create Return Labels at Pack Station :: START
		//All the returns will be handled by FedEx Ground Shipping
		Element returnLabelsEle = XMLUtil.getFirstElementByName(inElem,"ReturnShippingLabels");
		//Start : OMNI-322 : Changes for Agile Stabilization
		//String strIsApoFpo = getIsAPOFPOFlag(env, inElem);
		HashMap<String, String> hmShipmentData = getIsAPOFPOFlag(env, inElem);
		String strIsApoFpo = hmShipmentData.get(AcademyConstants.ATTR_EXTN_IS_APO_FPO);
		String strNodeType = hmShipmentData.get(AcademyConstants.ATTR_NODE_TYPE);
		//End : OMNI-322 : Changes for Agile Stabilization
		if(AcademyConstants.STR_YES.equalsIgnoreCase(strIsApoFpo) 
				&& returnLabelsEle != null){
			XMLUtil.removeChild(inElem, returnLabelsEle);
			log.verbose("After APOFPO Check InDoc :: " + XMLUtil.getXMLString(inDoc));
			log.verbose("After APOFPO Check InEle:: " + XMLUtil.getElementXMLString(inElem));
			
		}
		if(returnLabelsEle != null &&
				!AcademyConstants.STR_YES.equalsIgnoreCase(strIsApoFpo)){
			Element eleReturnShippingLabel = (Element)returnLabelsEle.getElementsByTagName("ReturnShippingLabel").item(0);
			returnLabelsEle.setAttribute("Carrier", "FEDX");
			log.verbose("Successfully updated return carrier to FEDX");
			if(eleReturnShippingLabel!=null)
			{
				String strCurrentDate = "";
				eleReturnShippingLabel.setAttribute("UPSServiceType", "Ground");
				log.verbose("Successfully updated return serviceType to Ground");

				HashMap<String, String> hmpPickupDateOverrideReq;
				try {
					hmpPickupDateOverrideReq = AcademyCommonCode
					.getCommonCodeListAsHashMap(env,
							AcademyConstants.COMMONCODETYPE_RETURN_PICK_DATE,
							AcademyConstants.PRIMARY_ENTERPRISE);					

					if (AcademyConstants.STR_YES.equalsIgnoreCase(hmpPickupDateOverrideReq
							.get(AcademyConstants.OVERRIDE_PICK_DATE))) {

						log.verbose("Override PickupDate in Agile Return Label request is true...");

						Calendar cal = Calendar.getInstance();
						SimpleDateFormat sdf = new SimpleDateFormat(AcademyConstants.DATE_YYYYMMDD_FORMAT);
						strCurrentDate = sdf.format(cal.getTime());	
						//Setting the PickupDate to current date since agile does not give return label for future date.
						eleReturnShippingLabel.setAttribute("PickupDate", strCurrentDate);
					}

					//Setting the CLS address as ReturnTo Address in the Return shipping Label					
					HashMap<String, String> hmpPackageReturnAddress = AcademyCommonCode
					.getCommonCodeListAsHashMap(env,
							AcademyConstants.COMMONCODETYPE_ACAD_RETURN_ADDRESS,
							AcademyConstants.PRIMARY_ENTERPRISE);	

					eleReturnShippingLabel.setAttribute("ConsigneeAddress1",hmpPackageReturnAddress.get("ConsigneeAddress1"));
					eleReturnShippingLabel.setAttribute("ConsigneeAddress2",hmpPackageReturnAddress.get("ConsigneeAddress2"));
					eleReturnShippingLabel.setAttribute("ConsigneePhone",hmpPackageReturnAddress.get("ConsigneePhone"));
					eleReturnShippingLabel.setAttribute("ConsigneeCity",hmpPackageReturnAddress.get("ConsigneeCity"));
					eleReturnShippingLabel.setAttribute("ConsigneeStateProv",hmpPackageReturnAddress.get("ConsigneeStateProv"));
					eleReturnShippingLabel.setAttribute("ConsigneePostalCode",hmpPackageReturnAddress.get("ConsigneePostalCode"));
					eleReturnShippingLabel.setAttribute("ConsigneeAttention",hmpPackageReturnAddress.get("ConsigneeAttention"));
					eleReturnShippingLabel.setAttribute("ConsigneePhoneNo",hmpPackageReturnAddress.get("ConsigneePhoneNo"));
					eleReturnShippingLabel.setAttribute("ConsigneeCompanyName",hmpPackageReturnAddress.get("ConsigneeCompanyName"));
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
		}
			//EFP-8 Create Return Labels at Pack Station :: END	

			/*
			 * Element PackageDetail = XMLUtil.getFirstElementByName(inElem,
			 * "PackageLevelDetail"); Element eAccessorialRecord =
			 * XMLUtil.getFirstElementByName(inElem, "AccessorialRecord");
			 * if(!YFCObject.isVoid(PackageDetail)){
			 * PackageDetail.setAttribute("PBShipperReference",
			 * PackageDetail.getAttribute("ShipID"));
			 * PackageDetail.setAttribute("PackageActualWeight", "3.00");
			 * //PackageDetail.setAttribute("PickupDate", "20090911");
			 * eAccessorialRecord.setAttribute("PackageHeight", "1.00");
			 * eAccessorialRecord.setAttribute("PackageLength", "2.00");
			 * eAccessorialRecord.setAttribute("PackageWidth", "3.00");
			 * 
			 * String sShipperAccountNo =
			 * PackageDetail.getAttribute("ShipperAccountNumber"); String
			 * sShipmentKey = PackageDetail.getAttribute("ShipmentKey");
			 * if(!YFCObject.isVoid(sShipmentKey)){ //Call getTaskList API to check
			 * if any open outbound picking task is of type pack while pick. /*
			 * //Input <Task> <TaskReferences ShipmentKey="20090819172153273044"/>
			 * <TaskType ActivityGroupID="OUTBOUND_PICKING" PackWhilePick="N"/>
			 * </Task>
			 */

			/*
			 * Document outTaskDoc; try { outTaskDoc =
			 * CallgetTaskList(env,sShipmentKey); if(!YFCObject.isVoid(outTaskDoc)){
			 * Element outTaskDocElem = outTaskDoc.getDocumentElement(); String
			 * sTotalRecords = outTaskDocElem.getAttribute("TotalNumberOfRecords");
			 * 
			 * //If total number of records = 0 it means shipment is of type
			 * conveyable and print required is client.
			 * if(sTotalRecords.equals("0")){
			 * 
			 * sPrinterType = "Client"; sShipperAccountNo = "vipin_client";
			 * PackageDetail.setAttribute("ShipperAccountNumber",sShipperAccountNo); } } }
			 * catch (Exception e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); } } } //
			 */
		
		//Start: OMNI-322 : Changes for Agile stabilization
		//Below are variables which holds the Pierbridge URL from customer_overrides.properties file 
		
		log.verbose("NodeType for the given Store is :: " + strNodeType);
		String strURL = "";
		if (!YFCObject.isVoid(strNodeType)) {
			strURL = YFSSystem.getProperty(AcademyConstants.STR_PIER_BRIDGE_URL + AcademyConstants.STR_DOT + strNodeType);
			if (YFCObject.isVoid(strURL)) {
				strURL = YFSSystem.getProperty(AcademyConstants.STR_PIER_BRIDGE_URL + AcademyConstants.STR_DOT + AcademyConstants.STR_STORE);
			}
		} else {
			strURL = YFSSystem.getProperty(AcademyConstants.STR_PIER_BRIDGE_URL + AcademyConstants.STR_DOT + AcademyConstants.STR_STORE);
		}
		Element eleConnectionParameters = XMLUtil.getFirstElementByName(inElem, AcademyConstants.STR_CONNECTION_PARAMETERS);
		Element elePierbridgeParams = XMLUtil.getFirstElementByName(eleConnectionParameters, AcademyConstants.STR_PIERBRIDGE_PARAMS);
		elePierbridgeParams.setAttribute(AcademyConstants.STR_PB_SERVER_URL, strURL);
		
		//End: OMNI-322 : Changes for Agile stabilization
		
		// Start: OMNI-41994 - Signature Required Changes - Level of service
		// Check if Shipping Address is not PO Address and common code SIGN_REQD_INT_ORDER is Y
		if (AcademyConstants.STR_YES.equalsIgnoreCase(hmpSignReqdAtDelivery.get("SIGN_REQ_INT_ORDER"))
				&& !AcademyConstants.STR_YES.equalsIgnoreCase(strIsApoFpo)) {
			try {
				Element elePackageLevelDetail = XMLUtil.getFirstElementByName(inElem,
						AcademyConstants.ELEMENT_PACKAGE_LEVEL_DETAIL);
				String strShipmentKey = elePackageLevelDetail.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
				if (!YFCObject.isVoid(strShipmentKey)) {
					Document getShipmentListOutDoc = getShipmentList(env, strShipmentKey,false);
					// If one of the ShipmentLines has SignatureRequired as Y then check the Carrier
					// and set the Service attribute
					String signatureReq = XPathUtil.getString(getShipmentListOutDoc.getDocumentElement(),
							"//OrderLine/Extn[@ExtnIsSignatureRequired='Y']/@ExtnIsSignatureRequired");
					if (AcademyConstants.STR_YES.equalsIgnoreCase(signatureReq)) {
						log.verbose("Signature Required Service --" + signatureReq);
						Element eleSpecialServices = XMLUtil.getFirstElementByName(inElem,
								AcademyConstants.ELEMENT_SPECIAL_SERVICES);
						Element eleSpecialService = inDoc.createElement(AcademyConstants.ELEMENT_SPECIAL_SERVICE);
						// take service attribute values from common code
						String service = hmpSignReqdAtDelivery.get("USPS_SIGN_REQ");
						if (AcademyConstants.SCAC_FEDX.equalsIgnoreCase(sCarrier)) {
							service = hmpSignReqdAtDelivery.get("FEDX_SIGN_REQ");
						}
						log.verbose("Signature Required Service --" + service);
						eleSpecialService.setAttribute(AcademyConstants.ATTR_SERVICE, service);
						eleSpecialServices.appendChild(eleSpecialService);
					}
				}
			} catch (Exception e) {
				log.verbose("Error in Signature Required Changes");
				log.error(e);
			}
		}
		//End: OMNI-41994 - Signature Required Changes - Level of service
		
		//OMNI-52399
		//OMNI-66580 -  Start
		Element elePackageLevelDetail = XMLUtil.getFirstElementByName(inElem,
				AcademyConstants.ELEMENT_PACKAGE_LEVEL_DETAIL);
		String consigneeCompanyName = elePackageLevelDetail.getAttribute("ConsigneeCompanyName");
		String strShipmentKey = elePackageLevelDetail.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
		String salesOrderNumber = "";
		try {
			Document getShipmentListOutDoc = getShipmentList(env, strShipmentKey, true);
			if (AcademyConstants.STR_STS_COMPANY.equalsIgnoreCase(consigneeCompanyName)) {
				salesOrderNumber = XMLUtil.getAttributeFromXPath(getShipmentListOutDoc,
						AcademyConstants.XPATH_ORDERLINE_SALESORDERNUMBER);
				log.verbose("STS2.0 Sales Order Number - " + salesOrderNumber);
			} else {
				salesOrderNumber = XMLUtil.getAttributeFromXPath(getShipmentListOutDoc,
						AcademyConstants.XPATH_ORDERLINE_SO_NUMBER);
				log.verbose("STH Sales Order Number - " + salesOrderNumber);
			}
		} catch (ParserConfigurationException e) {
			log.verbose("Error while adding order number to STS2.0/STH ship label");
			log.error(e);
		} catch (Exception e) {
			log.verbose("Error while adding order number to STS2.0/STH ship label");
			log.error(e);
		}
		elePackageLevelDetail.setAttribute("PBShipperReference", salesOrderNumber);
		//OMNI-66580 -  End
		//END: OMNI-52399

		log.endTimer(" End of YCSshipCartonUserExitImpl-> shipCarton()Api");
		log.verbose("Output of the code :: " + XMLUtil.getXMLString(inDoc));
		return XMLUtil.getXMLString(inDoc);
		
	}
	
	/**
	 * @param env
	 * @param strShipmentKey
	 * @return
	 * @throws ParserConfigurationException
	 * @throws Exception
	 */
	private Document getShipmentList(YFSEnvironment env, String strShipmentKey,boolean salesOrderNumberReq)
			throws ParserConfigurationException, Exception {
		Document getShipmentListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);

		getShipmentListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY,
				strShipmentKey);
		Document outputTemplate ;
		if(salesOrderNumberReq) {
			outputTemplate= YFCDocument.getDocumentFor(
					"<Shipments><Shipment ShipmentKey=''><ShipmentLines><ShipmentLine ShipmentLineKey=''><OrderLine OrderLineKey=''><Order OrderNo='' OrderName=''/><Extn ExtnIsSignatureRequired=''/></OrderLine></ShipmentLine></ShipmentLines> </Shipment></Shipments>")
					.getDocument();
		}else {
			outputTemplate= YFCDocument.getDocumentFor(
				"<Shipments><Shipment ShipmentKey=''><ShipmentLines><ShipmentLine ShipmentLineKey=''><OrderLine OrderLineKey=''><Extn ExtnIsSignatureRequired=''/></OrderLine></ShipmentLine></ShipmentLines> </Shipment></Shipments>")
				.getDocument();
		}
		env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST, outputTemplate);
		log.verbose("Signature Required Service -- getShipmentList API input - "
				+ XMLUtil.getXMLString(getShipmentListInDoc));
		Document getShipmentListOutDoc = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_LIST,
				getShipmentListInDoc);
		env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
		log.verbose("Signature Required Service -- getShipmentList API output - "
				+ XMLUtil.getXMLString(getShipmentListOutDoc));
		return getShipmentListOutDoc;
	}
	public boolean shipCartonContinue(YFSContext arg0, String arg1)
	throws YFSUserExitException {
		// TODO Auto-generated method stub
		return true;
	}

	public String shipCartonOutXML(YFSContext arg0, String arg1)
	throws YFSUserExitException {
		// TODO Auto-generated method stub
		return arg1;
	}

	private Document CallgetTaskList(YFSEnvironment env, String shipmentKey)
	throws Exception {
		Document inDoc = XMLUtil.createDocument("Task");
		Element inElem = inDoc.getDocumentElement();
		Element TaskReferences = XMLUtil.createElement(inDoc, "TaskReferences",
				null);
		inElem.appendChild(TaskReferences);
		TaskReferences.setAttribute("ShipmentKey", shipmentKey);
		Element TaskType = XMLUtil.createElement(inDoc, "TaskType", null);
		inElem.appendChild(TaskType);
		TaskType.setAttribute("ActivityGroupID", "OUTBOUND_PICKING");
		TaskType.setAttribute("PackWhilePick", "Y");
		Document outTaskDoc = AcademyUtil.invokeService(env,
				"AcademyGetTaskList", inDoc);
		return outTaskDoc;
	}
	
	/**
	 * This method is updated as part of OMNI-322 : Agile Stabilization Upgrade. The existing logic
	 * is updated to fetch NodetType along with ExtnIsAPOFPO from shipment.
	 * 
	 * @param inElem
	 * @return hmShipmentData
	 * @throws Exception
	 */
	 
	// private String getIsAPOFPOFlag (YFSEnvironment env, Element inElem){
	private HashMap<String, String> getIsAPOFPOFlag(YFSEnvironment env,Element inElem) {
		// EFP-8 Code Changes to Ignore Return Label Creation for APOFPO - Start
		HashMap<String, String> hmShipmentData = new HashMap<String, String>();
		String strIsApoFpo = "";

		try {
			Element elePackageLevelDetail = XMLUtil.getFirstElementByName(inElem, AcademyConstants.ELEMENT_PACKAGE_LEVEL_DETAIL);
			String strShipmentKey = elePackageLevelDetail.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			
			if (!YFCObject.isVoid(strShipmentKey)) {
				Document getShipmentListInDoc = XMLUtil.createDocument(AcademyConstants.ELE_SHIPMENT);
				getShipmentListInDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_KEY, strShipmentKey);
				Document outputTemplate = YFCDocument
					.getDocumentFor("<Shipment ShipmentKey='' ShipmentNo=''><ShipNode ShipNode='' NodeType='' /><ToAddress PersonInfoKey=''><Extn ExtnIsAPOFPO=''/></ToAddress></Shipment>")
						.getDocument();
				env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST,outputTemplate);
				log.verbose("getShipmentList API input - "+ XMLUtil.getXMLString(getShipmentListInDoc));
				Document getShipmentListOutDoc = AcademyUtil.invokeAPI(env,AcademyConstants.API_GET_SHIPMENT_LIST,getShipmentListInDoc);
				env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_LIST);
				log.verbose("getShipmentList API output - "+ XMLUtil.getXMLString(getShipmentListOutDoc));

				strIsApoFpo = XPathUtil.getString(getShipmentListOutDoc.getDocumentElement(),
						"/Shipments/Shipment/ToAddress/Extn/@ExtnIsAPOFPO");
				//Start: OMNI-322 : Changes for Agile Stabilization
				String strNodeType = XPathUtil.getString(getShipmentListOutDoc.getDocumentElement(),AcademyConstants.XPATH_SHIPMENT_LIST_TYPE);
				hmShipmentData.put(AcademyConstants.ATTR_NODE_TYPE, strNodeType);
				//End: OMNI-322 : Changes for Agile Stabilization
				
				if(!YFCObject.isNull(strIsApoFpo) &&
						!YFCObject.isVoid(strIsApoFpo)){
					hmShipmentData.put(AcademyConstants.ATTR_EXTN_IS_APO_FPO, strIsApoFpo);
					log.verbose("STRING APOFPO :: "+ strIsApoFpo);
				}				
			}
		}catch(Exception e){
			log.verbose("Exception inside YCSshipCartonUserExitImpl.shipCarton()");
			log.verbose("Exception while APO-FPO Identification");
			e.printStackTrace();
		}
		//return strIsApoFpo;
		return hmShipmentData;
		//EFP-8 Code Changes to Ignore Return Label Creation for APOFPO - End
	}

}
