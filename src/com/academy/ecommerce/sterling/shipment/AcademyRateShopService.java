package com.academy.ecommerce.sterling.shipment;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.academy.ecommerce.sterling.util.AcademyServiceUtil;
import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.ycp.greex.library.IsVoid;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfc.util.YFCDateUtils;
import com.yantra.yfs.japi.YFSEnvironment;

public class AcademyRateShopService
{
	private Properties props;

	private Document document = null;
	
	private Document calendarDayDocument = null;
	
	String strContainerWeight = null;
	
	double totalWeightofMultiboxItems = 0.0;     ///variable added to handle weight of multibo sku as strContainerWeight will  be zero and caluclation not done anywhere before

	/**
	 * 
	 * Instance of logger
	 * 
	 */
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyRateShopService.class);

	public void setProperties(Properties props)
	{
		this.props = props;

	}
	public Document academyRateShopService(YFSEnvironment env, Document inDoc)throws Exception
	{
		log.verbose("*** academyRateShopService input doc *** "	+ XMLUtil.getXMLString(inDoc));
		document = AcademyServiceUtil.getSOAPMessageTemplateForRateShop();
		Element elePierbridgeRateShopRequestdocument = document.getDocumentElement();
		//Element elePierbridgeRateShopRequestdocument = (Element) XMLUtil.getElementsByTagName(eleRoot, "PierbridgeRateShopRequest").get(0);
		log.verbose("*** elePierbridgeRateShopRequestdocument *** "	+ XMLUtil.getElementXMLString(elePierbridgeRateShopRequestdocument));
		if (!YFCObject.isVoid(elePierbridgeRateShopRequestdocument))
		{
			
			//Checking if the RateShop call is for a multibox item 
			String isMultiBoxSku = inDoc.getDocumentElement().getAttribute("isMultiBoxSku");
			String itemID        = isMultiBoxSku.equalsIgnoreCase("Y") ? inDoc.getDocumentElement().getAttribute("ExtnItemID") : null; 
			String mbFactor      = isMultiBoxSku.equalsIgnoreCase("Y") ?inDoc.getDocumentElement().getAttribute("MbFactor") : "0";
			String pickQuanity   = inDoc.getDocumentElement().getAttribute("ExtnPickQuantity");
			
			setDefaultProperties(env, inDoc, elePierbridgeRateShopRequestdocument, mbFactor,itemID,pickQuanity);  ///mbfactor value is added to acomodate the multibox sku

			/* Prepare the Sender input */
			Element eleSender = (Element) XMLUtil.getElementsByTagName(elePierbridgeRateShopRequestdocument, "Sender").get(0);
			if (!YFCObject.isVoid(eleSender))
			{
				prepareSenderInformationForShipment(inDoc, eleSender);
			}
			/* Prepare the Receiver input */
			Element eleReceiver = (Element) XMLUtil.getElementsByTagName(elePierbridgeRateShopRequestdocument, "Receiver").get(0);
			if (!YFCObject.isVoid(eleReceiver))
			{
				prepareReceiverInformationForShipment(inDoc, eleReceiver);
			}
		}
		log.verbose("*** elePierbridgeRateShopRequestdocument *** "	+ XMLUtil.getElementXMLString(elePierbridgeRateShopRequestdocument));
		return document;
	}
	
	private void setDefaultProperties(YFSEnvironment env, Document inDoc, Element elePierbridgeRateShopRequestdocument,String mbFactor,String itemID,String pickQuantity)throws Exception
	{
		
		log.verbose("Inside the setDefaultProperties method");
		log.verbose("*** mbFactor is *** "	+mbFactor); 
		String strOverrideDefaultUser="";  //Added for Enhancement#OMNI 444
		String strCarrierAccountPrefix=""; //Added for Enhancement#OMNI 444
		Element shipment = inDoc.getDocumentElement();
		YFCDocument doc = YFCDocument.getDocumentFor(inDoc);
		YFCElement eleShipment = doc.getDocumentElement();
		YFCDate dtExpectedShipmentDate = eleShipment.getDateAttribute("ExpectedShipmentDate");
		String sd = dtExpectedShipmentDate.getString("MM/dd/yyyy");
		String expectedShipmentDate = shipment.getAttribute("ExpectedShipmentDate");
		String weight = inDoc.getDocumentElement().getAttribute("TotalWeight");
		strContainerWeight = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_EXTN_TOTAL_WEIGHT);//Added for STL-1548
		String strStatus = shipment.getAttribute(AcademyConstants.ATTR_STATUS);//Added for STL-1334
		
		//Start WN-1838 Sterling to send DIM weight to Agile
		String strContainerHeight = "";
		String strContainerLenght = "";
		String strContainerWidth = "";
		String strNodeTypesForDIM = "";
		String strExtnSetDIMValues = "";
		Element eleHeight = null;
		Element eleLength = null;
		Element eleWidth = null;
		///taken to new method to handle multiboxsku
		//End WN-1838 Sterling to send DIM weight to Agile
		
		//START : STL-1606
		String strNodeType = "";
		String strNodeTypesForUSPS = props.getProperty(AcademyConstants.ATTR_NODE_TYPES_FOR_USPS);
		Element eleShipNode = (Element) shipment.getElementsByTagName(AcademyConstants.SHIP_NODE).item(0);
		if (!YFCObject.isVoid(eleShipNode)) {
			strNodeType = eleShipNode.getAttribute(AcademyConstants.ATTR_NODE_TYPE);
		}
		//END : STL-1606
		
		//START Added for STL-1563
		String strWeightConstraints = props.getProperty(AcademyConstants.STR_RATE_GROUP_WEIGHT_CONSTARINT);
		double dTotalWeight = Double.parseDouble(weight);
		double dWeightConstraints = Double.parseDouble(strWeightConstraints);
		//END Added for STL-1563
		YFCElement eleAdditionalDates = null;		
		YFCDate requestedDate = null;
		if(doc.getElementsByTagName("AdditionalDates").getLength()>0)
		{
			eleAdditionalDates = (YFCElement)eleShipment.getElementsByTagName("AdditionalDates").item(0);
			YFCElement eleAdditionalDate = (YFCElement)eleAdditionalDates.getElementsByTagName("AdditionalDate").item(0);
			if("ACADEMY_DELIVERY_DATE".equals(eleAdditionalDate.getAttribute("DateTypeId")))
			{
				requestedDate = eleAdditionalDate.getDateAttribute("RequestedDate");
				log.verbose("*** requestedDate *** "	+requestedDate);
			}
		}

		Element eleUserName = (Element) XMLUtil.getElementsByTagName(elePierbridgeRateShopRequestdocument, "UserName").get(0);
		/*eleUserName.setTextContent(props.getProperty("UserName"));*/
		
		/* Enhancement#OMNI 444  Start Change for adding Actual Store as UserName */
		
		strOverrideDefaultUser = props.getProperty(AcademyConstants.STR_OVRRD_USER);
		strCarrierAccountPrefix=props.getProperty(AcademyConstants.STR_CARRIER_ACCOUNT);
		log.verbose("*** serviceArgDefaultUser *** "	+ strOverrideDefaultUser);
		String shipNodeValue=XMLUtil.getString(inDoc, AcademyConstants.XPATH_SHIPNODE );
		log.verbose("*** shipNodeValue *** "	+shipNodeValue);
		if(strOverrideDefaultUser.equals("Y")){
			
			if(!YFCObject.isVoid(shipNodeValue)){
				eleUserName.setTextContent(strCarrierAccountPrefix.concat(shipNodeValue));
				log.verbose("*** eleUserNameStore *** "	+XMLUtil.getElementXMLString(eleUserName));
			}
		}else{
			eleUserName.setTextContent(props.getProperty(AcademyConstants.STR_USERNAME));
			log.verbose("*** eleUserNameDefault *** "	+XMLUtil.getElementXMLString(eleUserName));
		}
		
		/* Enhancement#OMNI 444 If strOverrideDefaultUser="Y" then added Actual Store as UserName*/
		
		Element eleRateGroup = (Element) XMLUtil.getElementsByTagName(elePierbridgeRateShopRequestdocument, "RateGroup").get(0);
		//START : STL-1563 Adding constraint for weight
		//START : STL-1334 Add USPS to Agile Rate Shop Call During UG/ DG
		//eleRateGroup.setTextContent(props.getProperty("RateGroup"));
		//STL-1606 Modified the if condition for adding constraint for NodeType. 
		//if((AcademyConstants.STATUS_READY_FOR_BACKROOM_PICK_VAL.equals(strStatus)) && dTotalWeight <= dWeightConstraints){
		
		//END : STL-1334 Add USPS to Agile Rate Shop Call During UG/ DG
		//END : STL-1563 Adding constraint for weight
		
		if(!YFCObject.isVoid(inDoc.getDocumentElement().getAttribute("Exceptional")) && "Y".equals(inDoc.getDocumentElement().getAttribute("Exceptional")))
		{
			//Element eleFilterMode = (Element) XMLUtil.getElementsByTagName(elePierbridgeRateShopRequestdocument, "FilterMode").get(0);
			//eleFilterMode.setTextContent(props.getProperty("FilterMode"));
		}
		else
		{
			Element eleFilterMode = (Element) XMLUtil.getElementsByTagName(elePierbridgeRateShopRequestdocument, "FilterMode").get(0);
			eleFilterMode.setTextContent(props.getProperty("FilterMode"));
		}
		
		Element eleRequiredDate = (Element) XMLUtil.getElementsByTagName(elePierbridgeRateShopRequestdocument, "RequiredDate").get(0);
		String rd = requestedDate.getString("MM/dd/yyyy");
		
		// OMNI 4211 Utilize WCS Promise Date in Sterling(Rate Shop) - Start	
		String strInitialPromiseDate = null;
		String strRequiredDate = null;
		String strWCSPromiseDate = null;
		strWCSPromiseDate = props.getProperty(AcademyConstants.STR_WCS_PROMISE_DATE);
		log.verbose("*** strWCSPromiseDate *** "	+strWCSPromiseDate);
		if(doc.getElementsByTagName(AcademyConstants.ELE_SHIPMENT_LINES).getLength()>0) {
			strInitialPromiseDate = XMLUtil.getAttributeFromXPath(inDoc, AcademyConstants.XPATH_EXTN_INITIAL_PROMISEDATE);
			log.verbose("*** promiseDate *** "	+ strInitialPromiseDate);	
		}
		if(!YFCObject.isVoid(strInitialPromiseDate) && strWCSPromiseDate.equals(AcademyConstants.STR_YES) ){	
			DateFormat dbDateTimeFormatter = new SimpleDateFormat(AcademyConstants.STR_DATE_TIME_PATTERN);//
			DateFormat formatter = new SimpleDateFormat(AcademyConstants.STR_PRINT_SHIPPING_LABEL_DATE);// MM/dd/yyyy
			strRequiredDate = formatter.format(dbDateTimeFormatter.parse(strInitialPromiseDate));
			log.verbose("***inside iff strRequiredDate *** "	+ strRequiredDate);
			eleRequiredDate.setTextContent(strRequiredDate);
		} else {
			log.verbose("***inside else rd *** "	+ rd);
			eleRequiredDate.setTextContent(rd);	
		}
		// OMNI 4211 Utilize WCS Promise Date in Sterling(Rate Shop) - End
		
		Element eleShipDate = (Element) XMLUtil.getElementsByTagName(elePierbridgeRateShopRequestdocument, "ShipDate").get(0);
		YFCDate y = new YFCDate();
		
		String strSCAC = eleShipment.getAttribute("SCAC");// added for the STL-360
		String shipNode = eleShipment.getAttribute("ShipNode");// added for the STL-360
		if(!YFCObject.isVoid(inDoc.getDocumentElement().getAttribute("UpgradeShipments")) && "Y".equals(inDoc.getDocumentElement().getAttribute("UpgradeShipments")))
		{
			YFCDate newDate = YFCDateUtils.getNewDate(y, Integer.parseInt("1"));
			//String nd = newDate.getString("MM/dd/yyyy");
			// Start change for STL-360
			calendarDayDocument = getCalendarDayDetails(env, newDate, strSCAC);
			String workingDay = getWorkingDays(calendarDayDocument);
			eleShipDate.setTextContent(workingDay);
			//End Change for STL-360
		}
		else
		{
			//String nd = y.getString("MM/dd/yyyy");
			
			//Start change for STL-360
			calendarDayDocument = getCalendarDayDetails(env, y, strSCAC);
			String workingDay = getWorkingDays(calendarDayDocument);
			eleShipDate.setTextContent(workingDay);
			//End Change for STL-360
		}
		
		/*YFCDate dateCurrent = new YFCDate();
		YFCDate d2 = YFCDateUtils.stripTime(dateCurrent);
		Document getCommonCodeListInputXML = XMLUtil.createDocument("CommonCode");
		getCommonCodeListInputXML.getDocumentElement().setAttribute("CodeType", "CUT_OF_TIME");
		Document outXML = AcademyUtil.invokeAPI(env, "getCommonCodeList", getCommonCodeListInputXML);
		NodeList commonCodeList1 = outXML.getElementsByTagName("CommonCode");
		String strCodeShortDescription = "";

		if (commonCodeList1 != null && !YFCObject.isVoid(commonCodeList1))
		{
			int iLength1 = commonCodeList1.getLength();
			for (int k = 0; k < iLength1; k++)
			{
				Element commonCode = (Element) commonCodeList1.item(k);
				String codeValue = commonCode.getAttribute("CodeValue");
				if ("CutOf".equals(codeValue))
				{
					strCodeShortDescription = commonCode.getAttribute("CodeShortDescription");
				}
			}
		}

		int cutoff = Integer.parseInt(strCodeShortDescription);
		
		YFCDateUtils.addHours(d2, cutoff);
		if(dtExpectedShipmentDate.lte(d2, false))
		{
			eleShipDate.setTextContent(sd);
		}
		else
		{
			YFCDate nextDay = YFCDateUtils.getNewDate(dtExpectedShipmentDate, 1);
			String nd = nextDay.getString("MM/dd/yyyy");
			eleShipDate.setTextContent(nd);
		}*/
		
		

		//Element eleServiceType = (Element) XMLUtil.getElementsByTagName(elePierbridgeRateShopRequestdocument, "ServiceType").get(0);
		//eleServiceType.setTextContent(props.getProperty("ServiceType"));
		
		///call seperate method to set the dimensions inside packages for multibox sku.
		if(!mbFactor.isEmpty() && !(mbFactor == null) && !mbFactor.equalsIgnoreCase("0")){   
			setDIMForMultibox(env, inDoc, elePierbridgeRateShopRequestdocument, mbFactor,itemID,pickQuantity);
		}
		else{
		Element elePackagesTypes = (Element) XMLUtil.getElementsByTagName(elePierbridgeRateShopRequestdocument, "Packages").get(0);
		Element elePackage = (Element) XMLUtil.getElementsByTagName(elePackagesTypes, "Package").get(0);
		Element eleWeight = (Element) XMLUtil.getElementsByTagName(elePackage, "Weight").get(0);
			
		//STL-1548 [Start]
		if(!YFCObject.isVoid(strContainerWeight)){
			eleWeight.setTextContent(strContainerWeight);	
		}
		else{
			eleWeight.setTextContent(weight);
		}
		//STL-1548 [End]
		

		//Start WN-1838 Sterling to send DIM weight to Agile		
		strExtnSetDIMValues = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_EXTN_SET_DIM_VALUES);
		log.verbose("*** ExtnSetDIMValues ***" +strExtnSetDIMValues);
		
		if(AcademyConstants.STR_YES.equalsIgnoreCase(strExtnSetDIMValues)) {
			
			strContainerHeight = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_EXTN_UNIT_HEIGHT);
			strContainerLenght = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_EXTN_UNIT_LENGTH);
			strContainerWidth = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_EXTN_UNIT_WIDTH);
			
			eleHeight = (Element) XMLUtil.getElementsByTagName(elePackage, AcademyConstants.ATTR_HEIGHT).get(0);
			eleLength = (Element) XMLUtil.getElementsByTagName(elePackage, AcademyConstants.ATTR_LENGTH).get(0);
			eleWidth = (Element) XMLUtil.getElementsByTagName(elePackage, AcademyConstants.ATTR_WIDTH).get(0);
			
			if(!YFCObject.isVoid(strContainerHeight)){
				eleHeight.setTextContent(strContainerHeight);
			}
			if(!YFCObject.isVoid(strContainerLenght)){
				eleLength.setTextContent(strContainerLenght);	
			}
			if(!YFCObject.isVoid(strContainerWidth)){
				eleWidth.setTextContent(strContainerWidth);	
			}
		}
		}
		//End WN-1838 Sterling to send DIM weight to Agile
		
		//Begin: OMNI-8714
		Element eleToAddress = (Element) inDoc.getDocumentElement().getElementsByTagName(AcademyConstants.ELEM_TOADDRESS).item(0);
		String extnIsPOBOXADDRESS = null;
		if(null != eleToAddress) {
			Element elePOExtn = (Element)eleToAddress.getElementsByTagName(AcademyConstants.ELE_EXTN).item(0);		
			extnIsPOBOXADDRESS = elePOExtn.getAttribute(AcademyConstants.ATTR_EXTN_IS_PO_BOX);
		}		
		//End: OMNI-8714
		
		
		///strContainerWeight will be null for multibox sku, so we will set it to totalWeight which is calculated in  setDIMForMultibox
		////TODO:
		dTotalWeight =!YFCObject.isVoid(strContainerWeight)? Double.parseDouble(strContainerWeight): totalWeightofMultiboxItems;	

		/* OMNI-45551 - Start */
		// Use RateGroup 2 for Agile Rate shop as this contains only FEDX carrier
		// service codes for STS2.0 Shipments
		String strdocType = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_DOC_TYPE);
		if (AcademyConstants.TRANSFER_ORDER_DOCUMENT_TYPE.equalsIgnoreCase(strdocType)) {
			eleRateGroup.setTextContent(props.getProperty(AcademyConstants.STR_RATE_GROUP_FEDX));
		}
		/* OMNI-45551 - End */
		else if(AcademyConstants.STR_YES.equals(extnIsPOBOXADDRESS)) {
			eleRateGroup.setTextContent(props.getProperty(AcademyConstants.STR_RATE_GROUP_FOR_STANDARD_PO_BOX));
		}
		else if((strNodeTypesForUSPS.contains(strNodeType)) && dTotalWeight <= dWeightConstraints) {
			eleRateGroup.setTextContent(props.getProperty(AcademyConstants.STR_RATE_GROUP_FOR_WMS));
		}
		else{
			eleRateGroup.setTextContent(props.getProperty(AcademyConstants.STR_RATE_GROUP));
		}
		
		log.verbose("*** elePierbridgeRateShopRequestdocument *** "	+XMLUtil.getElementXMLString(elePierbridgeRateShopRequestdocument));
	}
	///For multibox sku
	
	
	
	// Start change for STL-360
	private String getWorkingDays(Document calendarDayDocument) throws ParseException {
		// TODO Auto-generated method stub
		String fedxWorkingDay = "";
		NodeList datesNodeList = calendarDayDocument.getElementsByTagName("Date");
		for (int i = 0; i < datesNodeList.getLength(); i++) {
			Element dateElement = (Element)datesNodeList.item(i);
			String type = dateElement.getAttribute("Type");
			//type=0 for Off Day
			//type=3 for Edited Off Day
			if(!(type.equals("0") || type.equals("3"))){
				SimpleDateFormat fromInput = new SimpleDateFormat("yyyy-MM-dd");
				fedxWorkingDay = dateElement.getAttribute("Date");
				SimpleDateFormat xmlFormat = new SimpleDateFormat("MM/dd/yyyy");
				fedxWorkingDay = xmlFormat.format(fromInput.parse(fedxWorkingDay));
				break;
			}			
		}
		return fedxWorkingDay;
	}
	
	private Document getCalendarDayDetails(YFSEnvironment env, YFCDate date, String strSCAC) throws Exception {
		// TODO Auto-generated method stub
		log.verbose("*** getCalendarDayDetails Start *** ");
		//Creating Input Doc for getCalendarDayDetails 
		Document inDoc = XMLUtil.createDocument("Calendar");
		Element calendarElement = inDoc.getDocumentElement();
	//	String strCalendarID = getSCACCalendarID(strSCAC);
		calendarElement.setAttribute("CalendarId", "SHIPPING_CALENDAR");
		calendarElement.setAttribute("OrganizationCode", AcademyConstants.PRIMARY_ENTERPRISE);
		
		YFCDate weekDate = YFCDateUtils.getNewDate(date, Integer.parseInt("7"));
		calendarElement.setAttribute("FromDate", date.getString("yyyy-MM-dd"));
		calendarElement.setAttribute("ToDate", weekDate.getString("yyyy-MM-dd"));
		
		//Setting output template
		Document outputTemplate = getOutputTemplate();
		env.setApiTemplate(AcademyConstants.GET_CAL_DAY_DTLS_API, outputTemplate);
		
		//Calling getCalendarDayDetails API 
		log.verbose("getCalendarDayDetails input Doc" + XMLUtil.serialize(inDoc.getDocumentElement()));
		calendarDayDocument = AcademyUtil.invokeAPI(env, AcademyConstants.GET_CAL_DAY_DTLS_API, inDoc);
		env.clearApiTemplate(AcademyConstants.GET_CAL_DAY_DTLS_API);
		log.verbose("*** getCalendarDayDetails END *** ");
		return calendarDayDocument;
	}
	
	private String getSCACCalendarID(String strSCAC) {
		// TODO Auto-generated method stub
		String carrierCalID = "";
		if(strSCAC.equals("USPS-Endicia")) {
			//For USPS-Endicia
			carrierCalID = AcademyConstants.USPS_ENDICIA_CLA_ID;
		} else if(strSCAC.equals("USPS-Letter")) {
			//For USPS-Letter
			carrierCalID = AcademyConstants.USPS_LETTER_CLA_ID;
		} else {
			// Appending " Calendar" for FEDX and SmartPost
			carrierCalID = strSCAC + " Calendar";
		}
		return carrierCalID;
	}
	
	private Document getOutputTemplate() throws ParserConfigurationException {
		// TODO Auto-generated method stub
		Document outputTemplate = XMLUtil.createDocument("Calendar");
		Element calendarElement = outputTemplate.getDocumentElement();
		calendarElement.setAttribute("CalendarId", "");
		calendarElement.setAttribute("FromDate", "");
		calendarElement.setAttribute("OrganizationCode", "");
		calendarElement.setAttribute("ToDate", "");
		
		Element datesElement = XMLUtil.createElement(outputTemplate, "Dates", false);
		Element dateElement = XMLUtil.createElement(outputTemplate, "Date", true);
		dateElement.setAttribute("Date", "");
		dateElement.setAttribute("DayOfMonth", "");
		dateElement.setAttribute("Type", "");
		
		datesElement.appendChild(dateElement);
		calendarElement.appendChild(datesElement);
		return outputTemplate;
	}
	// End Change for STL-360
	
	private void prepareSenderInformationForShipment(Document inDoc, Element eleSender) throws Exception
	{
		String strOverrideSender=""; //Added for Enhancement#OMNI 444
		Element eleSentBy = (Element) XMLUtil.getElementsByTagName(eleSender, "SentBy").get(0);
		Element eleCity = (Element) XMLUtil.getElementsByTagName(eleSender, "City").get(0);
		Element eleRegion = (Element) XMLUtil.getElementsByTagName(eleSender, "Region").get(0);
		Element elePostalCode = (Element) XMLUtil.getElementsByTagName(eleSender, "PostalCode").get(0);
		Element eleCountry = (Element) XMLUtil.getElementsByTagName(eleSender, "Country").get(0);
		// Start : OMNI-324 : Changes for Agile Stabilization
		Element eleNodeType = (Element) XMLUtil.getElementsByTagName(eleSender, AcademyConstants.ATTR_NODE_TYPE).get(0);
		// End : OMNI-324 : Changes for Agile Stabilization

		eleSentBy.setTextContent(XMLUtil.getString(inDoc, "Shipment/FromAddress/@AddressLine1"));
		eleCity.setTextContent(XMLUtil.getString(inDoc, "Shipment/FromAddress/@City"));
		eleRegion.setTextContent(XMLUtil.getString(inDoc, "Shipment/FromAddress/@State"));
		elePostalCode.setTextContent(XMLUtil.getString(inDoc, "Shipment/FromAddress/@ZipCode"));
		eleCountry.setTextContent(XMLUtil.getString(inDoc, "Shipment/FromAddress/@Country"));
		
		/*Enhancement#OMNI 444 Start Change for adding OverrideType to Sender Element */
		/* Start chnages to Override Type  */
		strOverrideSender  = props.getProperty(AcademyConstants.PROP_OVERRIDE_SENDER);
		 if(strOverrideSender.equals("Y")){
			// Element eleOverrideType = inDoc.createElement("OverrideType");
			 Element eleOverrideType = (Element) XMLUtil.getElementsByTagName(eleSender,AcademyConstants.STR_OVRRD_TYPE).get(0);//Enhancement#OMNI 444 for overrideType Element
				eleOverrideType.setTextContent(AcademyConstants.STR_OVRRD_ALL);
				//	eleSender.appendChild(eleOverrideType);
			}
		
		 /* Enhancement#OMNI 444 if strOverrideType="Y" then  added  OverrideType element to sender Element*/
		
		// Start : OMNI-324 : Changes for Agile Stabilization
        eleNodeType.setTextContent(XMLUtil.getString(inDoc, AcademyConstants.XPATH_SHIPMENT_NODE_TYPE));
		// End : OMNI-324 : Changes for Agile Stabilization
		log.verbose("*** eleSender *** "	+XMLUtil.getElementXMLString(eleSender));
	}
	
	private void prepareReceiverInformationForShipment(Document inDoc, Element eleReceiver) throws Exception
	{
		Element eleCompany = (Element) XMLUtil.getElementsByTagName(eleReceiver, "CompanyName").get(0);
		eleCompany.setTextContent(XMLUtil.getString(inDoc, "Shipment/ToAddress/@Company"));
		
		if("".equals(XMLUtil.getString(inDoc, "Shipment/ToAddress/@Company")))
		{
			Element eleResidential = (Element) XMLUtil.getElementsByTagName(eleReceiver, "Residential").get(0);
			eleResidential.setTextContent("1");
		}
		else
		{
			Element eleResidential = (Element) XMLUtil.getElementsByTagName(eleReceiver, "Residential").get(0);
			eleResidential.setTextContent("0");
		}

		Element eleStreet = (Element) XMLUtil.getElementsByTagName(eleReceiver,	"Street").get(0);
		eleStreet.setTextContent(XMLUtil.getString(inDoc, "Shipment/ToAddress/@AddressLine1"));

		Element eleCity = (Element) XMLUtil.getElementsByTagName(eleReceiver, "City").get(0);
		eleCity.setTextContent(XMLUtil.getString(inDoc, "Shipment/ToAddress/@City"));

		Element eleRegion = (Element) XMLUtil.getElementsByTagName(eleReceiver,	"Region").get(0);
		eleRegion.setTextContent(XMLUtil.getString(inDoc, "Shipment/ToAddress/@State"));

		Element elePostalCode = (Element) XMLUtil.getElementsByTagName(eleReceiver, "PostalCode").get(0);
		elePostalCode.setTextContent(XMLUtil.getString(inDoc, "Shipment/ToAddress/@ZipCode"));

		Element eleCountry = (Element) XMLUtil.getElementsByTagName(eleReceiver, "Country").get(0);
		eleCountry.setTextContent(XMLUtil.getString(inDoc, "Shipment/ToAddress/@Country"));
		log.verbose("*** eleReceiver *** "	+XMLUtil.getElementXMLString(eleReceiver));
	}		
	
	
	private void setDIMForMultibox(YFSEnvironment env, Document inDoc, Element elePierbridgeRateShopRequestdocument,String mbFactor,String ItemID, String pickQuantity)throws Exception{
		
		
		Document getAcadMultiboxLookupListInput = XMLUtil.createDocument("AcadMultiboxLookup");
		getAcadMultiboxLookupListInput.getDocumentElement().setAttribute("ItemID", ItemID);
		Document getAcadMultiboxLookupListOutput = AcademyUtil.invokeService(env, "AcademyGetMultiboxLookupList", getAcadMultiboxLookupListInput);
		
		///Multiplying the package elements to accodmodate mbFactor number of packages
		Element elePackagesTypes = (Element) XMLUtil.getElementsByTagName(elePierbridgeRateShopRequestdocument, "Packages").get(0);
		Element elePackage = (Element) XMLUtil.getElementsByTagName(elePackagesTypes, "Package").get(0);
		int mbFactorInteger =  Integer.parseInt(mbFactor);
		for(int i = 0; i < mbFactorInteger - 1; i++){
			Node newPakcageNode = elePackage.cloneNode(true);
			elePackagesTypes.appendChild(newPakcageNode);
		}
		
		NodeList lookupList     =  getAcadMultiboxLookupListOutput.getElementsByTagName("AcadMultiboxLookup");
//		List eleWeightList      =  XMLUtil.getElementsByTagName(elePackage, AcademyConstants.ATTR_WEIGHT);
//		List eleHeightList      =  XMLUtil.getElementsByTagName(elePackage, AcademyConstants.ATTR_HEIGHT);
//		List eleLengthList      =  XMLUtil.getElementsByTagName(elePackage, AcademyConstants.ATTR_LENGTH);
//		List eleWidthList       =  XMLUtil.getElementsByTagName(elePackage, AcademyConstants.ATTR_WIDTH);
		
		
		
		List eleWeightList      =  XMLUtil.getElementsByTagName(elePackagesTypes, AcademyConstants.ATTR_WEIGHT);
		List eleHeightList      =  XMLUtil.getElementsByTagName(elePackagesTypes, AcademyConstants.ATTR_HEIGHT);
		List eleLengthList      =  XMLUtil.getElementsByTagName(elePackagesTypes, AcademyConstants.ATTR_LENGTH);
		List eleWidthList       =  XMLUtil.getElementsByTagName(elePackagesTypes, AcademyConstants.ATTR_WIDTH);
		
		for(int i= 0; i < lookupList.getLength(); i++){

			Element multiBoxLookupElement = (Element) lookupList.item(i);
			String strContainerLenght     = multiBoxLookupElement.getAttribute("Length");
			String strContainerWeight     = multiBoxLookupElement.getAttribute("Weight");
			String strContainerHeight     = multiBoxLookupElement.getAttribute("Height");
			String strContainerWidth      = multiBoxLookupElement.getAttribute("Width");

			Element eleWeight = (Element) eleWeightList.get(i);
			Element eleHeight = (Element) eleHeightList.get(i);
			Element eleWidth  = (Element) eleWidthList.get(i);
			Element eleLength = (Element) eleLengthList.get(i);


			if(!YFCObject.isVoid(strContainerHeight)){
				eleHeight.setTextContent(strContainerHeight);
			}
			if(!YFCObject.isVoid(strContainerLenght)){
				eleLength.setTextContent(strContainerLenght);	
			}
			if(!YFCObject.isVoid(strContainerWidth)){
				eleWidth.setTextContent(strContainerWidth);	
			}
			if(!YFCObject.isVoid(strContainerWeight)){
				eleWeight.setTextContent(strContainerWeight);
				totalWeightofMultiboxItems = + Double.parseDouble(strContainerWeight);     	
			}

		}
		
		///To handle the multiple pickquantiyi
		//fetch all the  Package element, repeat each one agin and again
		

		if ( !YFCObject.isVoid(pickQuantity)){
		 log.verbose("Cloning each  Package element " + pickQuantity+"   times");
			double pickQuantityDouble = Double.parseDouble(pickQuantity);
			int pickQuantityInt = (int) pickQuantityDouble;
	        NodeList packageList =  elePierbridgeRateShopRequestdocument.getElementsByTagName("Package");
	        final int PACKAGE_LIST_LENGTH           =  packageList.getLength();
	        if(PACKAGE_LIST_LENGTH>0)
	        {
	
			for(int j = 0; j <  PACKAGE_LIST_LENGTH; j++){
				log.verbose("******************PACKAGE_LIST_LENGTH********"+ PACKAGE_LIST_LENGTH);
				log.verbose("Iterating through package elements: "+ packageList.getLength());
				Element packageElement  =  (Element) packageList.item(j);
				for(int k = 0; k < pickQuantityInt -1; k++){
					log.verbose("Entering the inner loop to clone package elements");
					Node clonedPackage = packageElement.cloneNode(true);
					elePackagesTypes.appendChild(clonedPackage);
				}
			}
	        }
			log.verbose("Exiting the loop");
			log.verbose("Pacckages created is" + XMLUtil.getElementXMLString(elePackagesTypes));
			totalWeightofMultiboxItems = totalWeightofMultiboxItems*pickQuantityDouble;
		}
//=======
//		if ( !YFCObject.isVoid(pickQuantity)){
//			int pickQuantityInt = Integer.parseInt(pickQuantity);
//			NodeList packageList =  elePierbridgeRateShopRequestdocument.getElementsByTagName("Package");
//			for(int j = 0; j <  packageList.getLength(); j++){
//				Element packageElement  =  (Element) packageList.item(j);
//				for(int k = 0; k < pickQuantityInt -1; k++){
//					
//					Node clonedPackage = packageElement.cloneNode(true);
//					elePackagesTypes.appendChild(clonedPackage);
//				}
//			}
//			totalWeightofMultiboxItems = totalWeightofMultiboxItems*pickQuantityInt;
//		}

		
		
		
		
		
		log.verbose("Pacckages created is" + XMLUtil.getElementXMLString(elePackagesTypes));
		
		
		
		
	}
}
