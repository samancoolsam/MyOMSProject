package com.academy.ecommerce.sterling.shipment;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCDate;
import com.yantra.yfs.japi.YFSEnvironment;


/** This file will be used for WG shipping label print/reprint. 
 * @author Vimal, Created as part of STL-1532
 *
 */
public class AcademyPrintShippingLabelWG implements YIFCustomApi {
	
	Properties props;
	
	public void setProperties(Properties props) {
		this.props = props;
	}

  /**
   	* Instance of logger
    */
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPrintShippingLabelWG.class);
	
	/**To Print Shipping Label For WG Shipments
	 * @param env
	 * Yantra Environment Context.
	 * @param inDoc
	 * Input Document.
	 * @return
	 * @throws Exception
	 */
	public Document printShippingLabelWG(YFSEnvironment env, Document inDoc)
	throws Exception {
		
		log.verbose("***AcademyPrintShippingLabelWG.printShippingLabelWG() ***" + XMLUtil.getXMLString(inDoc));
		
		Document docShipmentIn = null;
		String strPrinterID = null;
		Element eleInput = null;
		Element eleContainer = null;
		Element eleContainersIn = null;
		Document docGetShipmentContainerListOutput = null;
		Document docShipment = null;
		Document docContainer = null;
		String strTemplate = null;
		String strContainer = null;
		Element eleShipment = null;
		String strContainerQunatity="";
		//EFP-17
		String strScac = null;
		String strIsEFWPrintShipLabelAllowed = null;
		//EFP-17
		
		docShipmentIn = (Document) env.getTxnObject(AcademyConstants.STR_WG_SHIPMENT_XML_TXN_OBJ);//Getting input XML for print flow
		
		//EFP-17
		strScac = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_SCAC);
		strIsEFWPrintShipLabelAllowed = props.getProperty(AcademyConstants.STR_EFW_PRINT_SHIPPING_LABEL);
		//EFP-17
		
		if(!YFCObject.isVoid(docShipmentIn)){
			eleShipment = null;
			eleShipment = docShipmentIn.getDocumentElement();
			log.verbose("***Input doc value***" + XMLUtil.getXMLString(docShipmentIn));
			strPrinterID=(String) env.getTxnObject(AcademyConstants.STR_WG_SHIPPING_PRINTER_ID);//Getting Printer ID
			log.verbose("Value PrinterID is: " + strPrinterID);
			eleShipment.setAttribute(AcademyConstants.ATTR_PRINTER_ID,strPrinterID);
			eleContainer = inDoc.getDocumentElement();
			strContainer = eleContainer.getAttribute(AcademyConstants.ATTR_CONTAINER_NO);//Getting Container No
			eleShipment.setAttribute(AcademyConstants.ATTR_CONTAINER_NO, strContainer);
			
			strContainerQunatity = calculateContainerQuantity(inDoc);
			eleShipment.setAttribute(AcademyConstants.ATTR_CONTAINER_QTY, strContainerQunatity);
			
			//EFP-17
			if(strScac.equals(AcademyConstants.CARRIER_EFW) 
					&& !YFCObject.isVoid(strIsEFWPrintShipLabelAllowed)
					&& AcademyConstants.STR_YES.equals(strIsEFWPrintShipLabelAllowed)
					){
				printShippingLabelEfw(env,docShipmentIn);//To Get Data For shipping Label EFW
			}else{
				printShippingLabel(env,docShipmentIn);//To Get Data For shipping Label CEVA
			}
			//EFP-17
					
		}
		
		//Checking if it's a reprint flow
		else{
			log.verbose("Is Reprint Flow : Y");
			
			eleInput = inDoc.getDocumentElement();
			strPrinterID=(String) env.getTxnObject(AcademyConstants.STR_WG_SHIPPING_REPRINTER_ID);//Getting Printer Id for reprint
			
			if(AcademyConstants.WG.equals(eleInput.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE))){//Checking If reprint flow is CEVA flow
				
				strTemplate = "<Containers>" //Setting Template for getShipmentContainerList API
										+"<Container>"
										+"<ContainerDetails>"
										+"<ContainerDetail/>"									
										+"</ContainerDetails>"
										+"<Shipment>"
										+"<FromAddress/>"
										+"<ToAddress/>"
										+"</Shipment>"
										+"</Container>"
										+"</Containers>";
				Document docGetShipmentContainerListTemplate = XMLUtil.getDocument(strTemplate);//Creating Doc for getShipmentContainerList API
				docContainer = XMLUtil.createDocument(AcademyConstants.ELE_CONTAINER);
				Element eleContainerKey = docContainer.getDocumentElement();
				eleContainerKey.setAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY, eleInput.getAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY));//Setting ShipmentContainerKey in Doc 
				
				env.setApiTemplate(AcademyConstants.API_GET_SHIPMENT_CONTAINER_LIST, docGetShipmentContainerListTemplate);//Setting Template
				log.verbose("getShipmentContainerList input Doc : " + XMLUtil.getXMLString(docContainer));
				docGetShipmentContainerListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_SHIPMENT_CONTAINER_LIST, docContainer);//Invoking getShipmentContainerList API to get the data
				env.clearApiTemplate(AcademyConstants.API_GET_SHIPMENT_CONTAINER_LIST);
				log.verbose("Output of getShipmentContainerList : " + XMLUtil.getXMLString(docGetShipmentContainerListOutput));
				
				eleContainersIn = docGetShipmentContainerListOutput.getDocumentElement();
				eleContainer = (Element) eleContainersIn.getElementsByTagName(AcademyConstants.ELE_CONTAINER).item(0);
				strContainer = eleContainer.getAttribute(AcademyConstants.ATTR_CONTAINER_NO);//Getting Container No
				
				strContainerQunatity = calculateContainerQuantity(XMLUtil.getDocumentForElement(eleContainer));

				eleShipment = (Element) docGetShipmentContainerListOutput.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_SHIPMENT).item(0);
				docShipment = XMLUtil.getDocumentForElement(eleShipment);
				docShipment.getDocumentElement().setAttribute(AcademyConstants.ATTR_PRINTER_ID, strPrinterID);//Setting Printer ID
				docShipment.getDocumentElement().setAttribute(AcademyConstants.ATTR_CONTAINER_NO, strContainer);
				docShipment.getDocumentElement().setAttribute(AcademyConstants.ATTR_CONTAINER_QTY, strContainerQunatity);
				log.verbose("Final Input Doc for print Shipping Label: " + XMLUtil.getXMLString(docShipment));
				
				//EFP-17
				if(strScac.equals(AcademyConstants.CARRIER_EFW) 
						&& !YFCObject.isVoid(strIsEFWPrintShipLabelAllowed)
						&& AcademyConstants.STR_YES.equals(strIsEFWPrintShipLabelAllowed)
						){
					printShippingLabelEfw(env,docShipment);//To Get Data For shipping Label EFW
				}else{
					printShippingLabel(env,docShipment);//To Get Data For shipping Label CEVA
				}
				//EFP-17			
				
			}
			else{
				log.verbose("Not a WG Flow");
			}
		}
		
		return inDoc;

		}
	
	/**To Calculate Container Quantity
	 * @param inDoc
	 * Input Document.
	 * @return
	 * Output Total Container Quantity
	 */
	private String calculateContainerQuantity(Document inDoc) {
		// TODO Auto-generated method stub
		Element eleContainer = inDoc.getDocumentElement();
		double packedQuantity = 0;
		String strQuantity = null;
		Element eleContainerDetail = null;
		
		NodeList  ContainerDetailList = eleContainer.getElementsByTagName(AcademyConstants.CONTAINER_DETL_ELEMENT);
		for (int iContainerDetail= 0; iContainerDetail < ContainerDetailList.getLength(); iContainerDetail++) {
			eleContainerDetail = (Element) ContainerDetailList.item(iContainerDetail);
			strQuantity =  eleContainerDetail.getAttribute(AcademyConstants.ATTR_QUANTITY);
			
			if (!YFCObject.isVoid(strQuantity))
			{
				packedQuantity = packedQuantity + Double.valueOf(strQuantity);
			}
		}
		return Integer.toString((int)packedQuantity);
	}

	/**To Create Input XML for Print Document Set
	 * @param env
	 * Yantra Environment Context.
	 * @param inDoc
	 * Input Document.
	 * @return
	 * @throws Exception
	 */
	private Document printShippingLabel(YFSEnvironment env, Document inputDoc) {
		log.verbose("AcademyPrintShippingLabelWG.printShippingLabel()");
		log.verbose("CEVA Input XML" + XMLUtil.getXMLString(inputDoc));

		Document docGetCevaZipCodeList = null;
		Element eleAcadCevaZipCodeList = null;
		Element eleAcadCevaZipCode = null;
		Element eleShipment = null;
		Element eleFromAddress = null;
		Element eleToAddress = null;
		NodeList nlAcadCevaZipCode = null;
		String strFromZipCode = null;
		String strToZipCode = null;
		String strFromDeliveryZoneCode = "";
		String strFromAirLocCode = "";
		String strToDeliveryZoneCode = "";
		String strToAirLocCode = "";
		String strPostCode = "";

		eleShipment = inputDoc.getDocumentElement();
		eleFromAddress = (Element) eleShipment.getElementsByTagName(AcademyConstants.FROM_ADDRESS).item(0);
		eleToAddress = (Element) eleShipment.getElementsByTagName(AcademyConstants.ELEM_TOADDRESS).item(0);

		strFromZipCode = eleFromAddress.getAttribute(AcademyConstants.ZIP_CODE);//Getting From address Zip Code
		strToZipCode = eleToAddress.getAttribute(AcademyConstants.ZIP_CODE);//Getting To address Zip Code
		// Splitting the zipcode values
		if(strFromZipCode.contains(AcademyConstants.STR_HYPHEN)){//Checking if Zipcode is 9 digit or 10 digit
			strFromZipCode = strFromZipCode.split(AcademyConstants.STR_HYPHEN)[0];
		}
		if(strToZipCode.contains(AcademyConstants.STR_HYPHEN)){
			strToZipCode = strToZipCode.split(AcademyConstants.STR_HYPHEN)[0];
		}

		docGetCevaZipCodeList = getCevaZipCodeDetails(env, strFromZipCode, strToZipCode);//Getting Data from custom table
		eleShipment.setAttribute(AcademyConstants.ATTR_LABEL, AcademyConstants.STR_LABEL_VALUE);//Setting Label Value
		//eleShipment.setAttribute(AcademyConstants.ATTR_NO_OF_PIECES, AcademyConstants.ATTR_NO_OF_PIECES_VALUE);//Setting Number Of Pieces
		eleShipment.setAttribute(AcademyConstants.ATTR_NO_OF_PIECES, eleShipment.getAttribute(AcademyConstants.ATTR_CONTAINER_QTY));//Setting Number Of Pieces
		eleAcadCevaZipCodeList = docGetCevaZipCodeList.getDocumentElement();
		nlAcadCevaZipCode = eleAcadCevaZipCodeList.getElementsByTagName(AcademyConstants.ELE_ACAD_CEVA);

		for(int iCount = 0; iCount<nlAcadCevaZipCode.getLength() ;iCount++){//Checking If Zip Code are present
			eleAcadCevaZipCode = (Element) nlAcadCevaZipCode.item(iCount);

			strPostCode = eleAcadCevaZipCode.getAttribute(AcademyConstants.ATTR_POST_CODE);

			if(strPostCode.equals(strFromZipCode)){
				strFromDeliveryZoneCode = eleAcadCevaZipCode.getAttribute(AcademyConstants.ATTR_DELIVERY_ZONE_CODE);
				strFromAirLocCode = eleAcadCevaZipCode.getAttribute(AcademyConstants.ATTR_AIR_LOC_CODE);
			}
			if(strPostCode.equals(strToZipCode)){
				strToDeliveryZoneCode = eleAcadCevaZipCode.getAttribute(AcademyConstants.ATTR_DELIVERY_ZONE_CODE);
				strToAirLocCode = eleAcadCevaZipCode.getAttribute(AcademyConstants.ATTR_AIR_LOC_CODE);
			}
		}
	
		eleFromAddress.setAttribute(AcademyConstants.ATTR_DELIVERY_ZONE_CODE, strFromDeliveryZoneCode);
		eleFromAddress.setAttribute(AcademyConstants.ATTR_AIR_LOC_CODE, strFromAirLocCode);
		eleToAddress.setAttribute(AcademyConstants.ATTR_DELIVERY_ZONE_CODE, strToDeliveryZoneCode);
		eleToAddress.setAttribute(AcademyConstants.ATTR_AIR_LOC_CODE, strToAirLocCode);
		
		if(nlAcadCevaZipCode.getLength()!=2){
			raiseZipCodeAlert(env,inputDoc);//Raiseing Alert	
		}
		
		inputDoc = caluculateExpectedDeliveryDate(env,inputDoc);//Calculating expected delivery date
		log.verbose("Input XML after calculating delivery date" + XMLUtil.getXMLString(inputDoc));

		try{
			log.verbose("CEVA Final XML" + XMLUtil.getXMLString(inputDoc));
			AcademyUtil.invokeService(env,AcademyConstants.API_ACADEMY_PRINT_WG_LABEL, inputDoc);//Invoking SERVICE to call print document set
		}
		catch (Exception e){			
			log.verbose("Exception occurred while invoking print document set");
			log.verbose(e.getMessage());
		}

		return inputDoc;	
	}

	/**To Calculate Delivery Data
	 * @param env
	 * Yantra Environment Context.
	 * @param inDoc
	 * Input Document.
	 * @return
	 * Output Doc
	 */
	private Document caluculateExpectedDeliveryDate(YFSEnvironment env, Document inputDoc) {
		// TODO Auto-generated method stub
		log.verbose("AcademyPrintShippingLabelWG.caluculateExpectedDeliveryDate()" + XMLUtil.getXMLString(inputDoc));
		Element calendar = null;
		String calendarId = null;
		Document docGetOrgDtls = null;
		Document outTempGetOrgDtls = null;
		NodeList calendarList = null;
		String strCalendarKey = "";
		String strDatePattern = null;
		String expectedDeliveryDate = "";
		NodeList shiftsNodeList = null;
		Element shiftsElement = null;
		Element datesElement = null;
		NodeList dateNodesList = null;	
		Element dateElement = null;		
		String isValid = null;
		int tempDaysCount = -1;
		int numberOfNodes = 0;
		NodeList shiftNodeList = null;
		
		String strBusinessDays = props.getProperty(AcademyConstants.STR_BUISNESS_DAYS);//Getting Buisness Days to add from props
		int numberOfBusinessDays = Integer.parseInt(strBusinessDays);
		String strIsShipmentCreatetsRequired = props.getProperty(AcademyConstants.STR_IS_SHIPMENT_CREATETS_REQUIRED);//Checking if Shipment Createts is required to calculate Delivery Data 
		String strShipmentCalendarId = props.getProperty(AcademyConstants.CALENDAR_ID);
		
		Element eleShipment = inputDoc.getDocumentElement();
		//String strScac = eleShipment.getAttribute(AcademyConstants.ATTR_SCAC);
		String strShipmentCreatets = eleShipment.getAttribute(AcademyConstants.STR_SHIPMENT_CREATETS);
		String strShipNode = eleShipment.getAttribute(AcademyConstants.SHIP_NODE);
		log.verbose("ActualShipmentDate - " + strShipmentCreatets);
		
		try {
			docGetOrgDtls = XMLUtil.createDocument(AcademyConstants.ORG_ELEMENT); 
            //Fetch attribute value for OrganizationCode 
            docGetOrgDtls.getDocumentElement().setAttribute(AcademyConstants.ORG_CODE_ATTR, strShipNode); 
            //Set the template for getOrganizationHierarchy 
            outTempGetOrgDtls = YFCDocument.parse("<Organization OrganizationCode=\"\"><Calendars> <Calendar CalendarKey=\"\" CalendarId=\"\"/></Calendars></Organization>").getDocument(); 
            env.setApiTemplate(AcademyConstants.GET_ORG_HIERARCHY_API, outTempGetOrgDtls); 
            //Invoke Api getOrganizationHierarchy 
            Document docOutputOrgDtls = AcademyUtil.invokeAPI(env, AcademyConstants.GET_ORG_HIERARCHY_API, docGetOrgDtls); 
            //Clear template 
            env.clearApiTemplates(); 
            //End: Process API getOrganizationHierarchy 
            //Fetch the NodeList of element Calender 
            calendarList = docOutputOrgDtls.getDocumentElement().getElementsByTagName(AcademyConstants.CALENDAR_ELEMENT); 
            //Loop through the NodeList record 
            for (int iCount=0;iCount<calendarList.getLength();iCount++) 
            { 
                    //Fetch the element Calender 
                    calendar = (Element) calendarList.item(iCount); 
                    //Fetch the attribute value of CalendarId 
                    calendarId = calendar.getAttribute(AcademyConstants.CALENDAR_ID_ATTR);
                    //Check if the value of CalendarId =SCAC value 
                    if(calendarId.equalsIgnoreCase(strShipmentCalendarId)) 
                    { 
                            //Fetch the attribute value of CalendarKey 
                            strCalendarKey = calendar.getAttribute(AcademyConstants.CALENDAR_KEY_ATTR); 
                            break; 
                    } 
            } 

		//current shipment date in new format
		//String strDeliveryDate = formatDate(strCarrierPickUpDate);
		//log.verbose("current shipment date in new format - " + strDeliveryDate);

		// getting list of next 15 days
		strDatePattern = AcademyConstants.STR_SIMPLE_DATE_PATTERN;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(strDatePattern);
		
		//Current date 
		Date currentDate;
		if (AcademyConstants.STR_YES.equalsIgnoreCase(strIsShipmentCreatetsRequired)) {
			currentDate = simpleDateFormat.parse(strShipmentCreatets);
		}
		else {
			currentDate = new Date();
		}
       		
		// find next 15 days from current shipment date
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentDate);
		int futureNoOfDays = 15;						
		cal.add(Calendar.DATE, futureNoOfDays);
		String strFutureDate = simpleDateFormat.format(cal.getTime());	
		log.verbose("future date - " + strFutureDate);
		Date futureDate = simpleDateFormat.parse(strFutureDate);

		log.verbose("preparing input to get calendar day details");
		YFCDocument docCalendarDayDtls = YFCDocument.createDocument(AcademyConstants.CALENDAR_ELEMENT);
		docCalendarDayDtls.getDocumentElement().setAttribute(AcademyConstants.CALENDAR_KEY_ATTR, strCalendarKey);
		docCalendarDayDtls.getDocumentElement().setDateTimeAttribute(AcademyConstants.FROM_DATE_ATTR, new YFCDate(currentDate));
		docCalendarDayDtls.getDocumentElement().setDateTimeAttribute(AcademyConstants.TO_DATE_ATTR, new YFCDate(futureDate));

		log.verbose("invoking api for calendar day details");
		Document docOutputCalendarDayDtls = AcademyUtil.invokeAPI(env, AcademyConstants.GET_CAL_DAY_DTLS_API, docCalendarDayDtls.getDocument());//Invoking getCalendarDayDetails
		
		log.verbose("********** calendar details : " + XMLUtil.getXMLString(docOutputCalendarDayDtls));
	
		datesElement = docOutputCalendarDayDtls.getDocumentElement();
		log.verbose(XMLUtil.getElementXMLString(datesElement));

		dateNodesList = XPathUtil.getNodeList(datesElement, "/Calendar/Dates/Date");
		
		if(dateNodesList != null && !YFCObject.isVoid(dateNodesList))
		{
			tempDaysCount = -1;
			numberOfNodes = dateNodesList.getLength();
			log.verbose("number of nodes : " + numberOfNodes);

			for(int iCount=0; iCount<numberOfNodes; iCount++)
			{
				dateElement = (Element)dateNodesList.item(iCount);
				log.verbose("******* date element : " + XMLUtil.getElementXMLString(dateElement));

				shiftsNodeList = dateElement.getElementsByTagName(AcademyConstants.ELE_SHIFTS);
				shiftsElement = (Element)shiftsNodeList.item(0);
				log.verbose("******* shift element : " + XMLUtil.getElementXMLString(shiftsElement));
				
				shiftNodeList = shiftsElement.getElementsByTagName(AcademyConstants.ELE_SHIFT);

				isValid = ((Element)shiftNodeList.item(0)).getAttribute(AcademyConstants.ATTR_VALID_FOR_DAY);

				if(isValid.equalsIgnoreCase(AcademyConstants.STR_YES))//Checking If working day or not
				{
					tempDaysCount++;
				}
				
				if(tempDaysCount == numberOfBusinessDays)//Checking If buisness days added to current date
				{
					expectedDeliveryDate = dateElement.getAttribute(AcademyConstants.DATE_ATTR);
					log.verbose("Expected Delivery date : " + expectedDeliveryDate);
					log.verbose("Expected Delivery date : " + expectedDeliveryDate);
					String deliveryDate = formatDate(expectedDeliveryDate);
					eleShipment.setAttribute(AcademyConstants.ATTR_EXPECTED_DELIVERY_DATE, deliveryDate);
					log.verbose(XMLUtil.getElementXMLString(eleShipment));
					break;
				}
			}
		}
		}
		catch (Exception e) 
		{
			log.verbose("Exception occured while calculating delivery date");
			log.verbose(e.getMessage());
		}
		
		return inputDoc;
	
	}
	
	/**To Format Date
	 * @param date
	 * Date
	 */
	private String formatDate(String date)
	{
		String fromDatePattern = AcademyConstants.STR_SIMPLE_DATE_PATTERN;
		String toDatePattern = AcademyConstants.STR_PRINT_SHIPPING_LABEL_DATE;
		try 
		{
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(fromDatePattern);
			Date parsedDate = simpleDateFormat.parse(date);
			simpleDateFormat.applyPattern(toDatePattern);
			date = simpleDateFormat.format(parsedDate);
		} 
		catch (Exception e) 
		{
			log.verbose("Exception occured while formatting date");
			log.verbose(e.getMessage());
		}
		
		return date;
	}
	

	/**
	 * To get data from custom table ACAD_CEVA_ZIP_CODE
	 * @param strFromZipCode
	 * @param strToZipCode
	 * @param env
	 * @return
	 */
	private Document getCevaZipCodeDetails(YFSEnvironment env,String strFromZipCode,String strToZipCode) {
		log.verbose("AcademyPrintShippingLabelWG.getCevaZipCodeDetails()");
		Document docOutput = null;
		Document docInput = null;		
		String strInputTemplate = "<AcadCevaZipCodeList>" +//Creating Complex query to get data for ZipCodes 
		"<ComplexQuery>" +
		"<Or>" +
		"<Exp Name='PostCode' Value='"+strFromZipCode+"'/>" +
		"<Exp Name='PostCode' Value='"+strToZipCode+"'/>" +
		"</Or>" +
		"</ComplexQuery>" +
		"</AcadCevaZipCodeList>";

		try {
			docInput = XMLUtil.getDocument(strInputTemplate);
			docOutput = AcademyUtil.invokeService(env,
					AcademyConstants.API_ACADEMY_GET_CEVA_ZIP_CODE, docInput);//Invoking custom API to get data from custom table

		} catch (Exception e) {
			log.verbose("Exception occurred while retriving data from custom table");
			log.verbose(e.getMessage());
		}
		return docOutput;
	}
	
	
	/**
	 * To Raise an alert if zip code is not present
	 * @param env 
	 * @param inputDoc
	 * Contains Zip Code which are not present in the table
	 */
	private void raiseZipCodeAlert(YFSEnvironment env, Document inputDoc) {
		log.verbose("AcademyPrintShippingLabelWG.raiseZipCodeAlert()" + XMLUtil.getXMLString(inputDoc));

		try{

			AcademyUtil.invokeService(env, AcademyConstants.API_ACADEMY_RAISE_MISSING_ZIP_CODE_ALERT, inputDoc);
			log.verbose("Alert has been raised  as Zip Code is not present in ACAD_CEVA_ZIP_CODE or " +
					"ACAD_EFW_ZIP_CODE table");	
		} catch (Exception e) {
			log.verbose("Exception occurred while invoking AcademyRaiseMissingZipCodeAlert API");
			log.verbose(e.getMessage());
		}
		
	}
	
	//EFP-17 EFW Carrier Integration - Start
	/**To Create Input XML for Print Document Set
	 * @param env
	 * Yantra Environment Context.
	 * @param inDoc
	 * Input Document.
	 * @return
	 * @throws Exception
	 */
	private Document printShippingLabelEfw(YFSEnvironment env, Document inputDoc) {
		log.verbose("AcademyPrintShippingLabelWG.printShippingLabelEfw()");
		log.verbose("EFW Input XML" + XMLUtil.getXMLString(inputDoc));

		Document docGetEfwZipCodeList = null;
		Element eleAcadEfwZipCodeList = null;
		Element eleAcadEfwZipCode = null;
		Element eleShipment = null;
		Element eleFromAddress = null;
		Element eleToAddress = null;
		NodeList nlAcadEfwZipCode = null;
		String strFromZipCode = null;
		String strToZipCode = null;
		String strFromDeliveryZoneCode = "";
		String strFromAirLocCode = "";
		String strToDeliveryZoneCode = "";
		String strToAirLocCode = "";
		String strPostCode = "";

		eleShipment = inputDoc.getDocumentElement();
		eleFromAddress = (Element) eleShipment.getElementsByTagName(AcademyConstants.FROM_ADDRESS).item(0);
		eleToAddress = (Element) eleShipment.getElementsByTagName(AcademyConstants.ELEM_TOADDRESS).item(0);

		strFromZipCode = eleFromAddress.getAttribute(AcademyConstants.ZIP_CODE);//Getting From address Zip Code
		strToZipCode = eleToAddress.getAttribute(AcademyConstants.ZIP_CODE);//Getting To address Zip Code
		// Splitting the zipcode values
		if(strFromZipCode.contains(AcademyConstants.STR_HYPHEN)){//Checking if Zipcode is 9 digit or 10 digit
			strFromZipCode = strFromZipCode.split(AcademyConstants.STR_HYPHEN)[0];
		}
		if(strToZipCode.contains(AcademyConstants.STR_HYPHEN)){
			strToZipCode = strToZipCode.split(AcademyConstants.STR_HYPHEN)[0];
		}

		docGetEfwZipCodeList = getEfwZipCodeDetails(env, strFromZipCode, strToZipCode);//Getting Data from custom table
		eleShipment.setAttribute(AcademyConstants.ATTR_LABEL, AcademyConstants.STR_LABEL_VALUE);//Setting Label Value
		//eleShipment.setAttribute(AcademyConstants.ATTR_NO_OF_PIECES, AcademyConstants.ATTR_NO_OF_PIECES_VALUE);//Setting Number Of Pieces
		eleShipment.setAttribute(AcademyConstants.ATTR_NO_OF_PIECES, eleShipment.getAttribute(AcademyConstants.ATTR_CONTAINER_QTY));//Setting Number Of Pieces
		eleAcadEfwZipCodeList = docGetEfwZipCodeList.getDocumentElement();
		nlAcadEfwZipCode = eleAcadEfwZipCodeList.getElementsByTagName(AcademyConstants.ELE_ACAD_EFW);

		for(int iCount = 0; iCount<nlAcadEfwZipCode.getLength() ;iCount++){//Checking If Zip Code are present
			eleAcadEfwZipCode = (Element) nlAcadEfwZipCode.item(iCount);

			strPostCode = eleAcadEfwZipCode.getAttribute(AcademyConstants.ATTR_POST_CODE);

			if(strPostCode.equals(strFromZipCode)){
				strFromDeliveryZoneCode = eleAcadEfwZipCode.getAttribute(AcademyConstants.ATTR_DELIVERY_ZONE_CODE);
				strFromAirLocCode = eleAcadEfwZipCode.getAttribute(AcademyConstants.ATTR_AIR_LOC_CODE);
			}
			if(strPostCode.equals(strToZipCode)){
				strToDeliveryZoneCode = eleAcadEfwZipCode.getAttribute(AcademyConstants.ATTR_DELIVERY_ZONE_CODE);
				strToAirLocCode = eleAcadEfwZipCode.getAttribute(AcademyConstants.ATTR_AIR_LOC_CODE);
			}
		}
	
		eleFromAddress.setAttribute(AcademyConstants.ATTR_DELIVERY_ZONE_CODE, strFromDeliveryZoneCode);
		eleFromAddress.setAttribute(AcademyConstants.ATTR_AIR_LOC_CODE, strFromAirLocCode);
		eleToAddress.setAttribute(AcademyConstants.ATTR_DELIVERY_ZONE_CODE, strToDeliveryZoneCode);
		eleToAddress.setAttribute(AcademyConstants.ATTR_AIR_LOC_CODE, strToAirLocCode);
		
		if(nlAcadEfwZipCode.getLength()!=2){
			raiseZipCodeAlert(env,inputDoc);//Raiseing Alert	
		}
		
		inputDoc = caluculateExpectedDeliveryDateEfw(env,inputDoc);//Calculating expected delivery date
		log.verbose("Input XML after calculating delivery date" + XMLUtil.getXMLString(inputDoc));

		try{
			log.verbose("CEVA Final XML" + XMLUtil.getXMLString(inputDoc));
			AcademyUtil.invokeService(env,AcademyConstants.API_ACADEMY_PRINT_WG_LABEL, inputDoc);//Invoking SERVICE to call print document set
		}
		catch (Exception e){			
			log.verbose("Exception occurred while invoking print document set");
			log.verbose(e.getMessage());
		}

		return inputDoc;	
	}

	/**To Calculate Delivery Data
	 * @param env
	 * Yantra Environment Context.
	 * @param inDoc
	 * Input Document.
	 * @return
	 * Output Doc
	 */
	private Document caluculateExpectedDeliveryDateEfw(YFSEnvironment env, Document inputDoc) {
		// TODO Auto-generated method stub
		log.verbose("AcademyPrintShippingLabelWG.caluculateExpectedDeliveryDateEfw()" + XMLUtil.getXMLString(inputDoc));
		Element calendar = null;
		String calendarId = null;
		Document docGetOrgDtls = null;
		Document outTempGetOrgDtls = null;
		NodeList calendarList = null;
		String strCalendarKey = "";
		String strDatePattern = null;
		String expectedDeliveryDate = "";
		NodeList shiftsNodeList = null;
		Element shiftsElement = null;
		Element datesElement = null;
		NodeList dateNodesList = null;	
		Element dateElement = null;		
		String isValid = null;
		int tempDaysCount = -1;
		int numberOfNodes = 0;
		NodeList shiftNodeList = null;
		
		String strBusinessDays = props.getProperty(AcademyConstants.STR_EFW_BUSINESS_DAYS);//Getting Buisness Days to add from props
		int numberOfBusinessDays = Integer.parseInt(strBusinessDays);
		String strIsShipmentCreatetsRequired = props.getProperty(AcademyConstants.STR_EFW_IS_SHIPMENT_CREATETS_REQ);//Checking if Shipment Createts is required to calculate Delivery Data 
		String strShipmentCalendarId = props.getProperty(AcademyConstants.STR_EFW_CALENDERID);
		
		Element eleShipment = inputDoc.getDocumentElement();
		//String strScac = eleShipment.getAttribute(AcademyConstants.ATTR_SCAC);
		String strShipmentCreatets = eleShipment.getAttribute(AcademyConstants.STR_SHIPMENT_CREATETS);
		String strShipNode = eleShipment.getAttribute(AcademyConstants.SHIP_NODE);
		log.verbose("ActualShipmentDate - " + strShipmentCreatets);
		
		try {
			docGetOrgDtls = XMLUtil.createDocument(AcademyConstants.ORG_ELEMENT); 
            //Fetch attribute value for OrganizationCode 
            docGetOrgDtls.getDocumentElement().setAttribute(AcademyConstants.ORG_CODE_ATTR, strShipNode); 
            //Set the template for getOrganizationHierarchy 
            outTempGetOrgDtls = YFCDocument.parse("<Organization OrganizationCode=\"\"><Calendars> <Calendar CalendarKey=\"\" CalendarId=\"\"/></Calendars></Organization>").getDocument(); 
            env.setApiTemplate(AcademyConstants.GET_ORG_HIERARCHY_API, outTempGetOrgDtls); 
            //Invoke Api getOrganizationHierarchy 
            Document docOutputOrgDtls = AcademyUtil.invokeAPI(env, AcademyConstants.GET_ORG_HIERARCHY_API, docGetOrgDtls); 
            //Clear template 
            env.clearApiTemplates(); 
            //End: Process API getOrganizationHierarchy 
            //Fetch the NodeList of element Calender 
            calendarList = docOutputOrgDtls.getDocumentElement().getElementsByTagName(AcademyConstants.CALENDAR_ELEMENT); 
            //Loop through the NodeList record 
            for (int iCount=0;iCount<calendarList.getLength();iCount++) 
            { 
                    //Fetch the element Calender 
                    calendar = (Element) calendarList.item(iCount); 
                    //Fetch the attribute value of CalendarId 
                    calendarId = calendar.getAttribute(AcademyConstants.CALENDAR_ID_ATTR);
                    //Check if the value of CalendarId =SCAC value 
                    if(calendarId.equalsIgnoreCase(strShipmentCalendarId)) 
                    { 
                            //Fetch the attribute value of CalendarKey 
                            strCalendarKey = calendar.getAttribute(AcademyConstants.CALENDAR_KEY_ATTR); 
                            break; 
                    } 
            } 

		//current shipment date in new format
		//String strDeliveryDate = formatDate(strCarrierPickUpDate);
		//log.verbose("current shipment date in new format - " + strDeliveryDate);

		// getting list of next 15 days
		strDatePattern = AcademyConstants.STR_SIMPLE_DATE_PATTERN;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(strDatePattern);
		
		//Current date 
		Date currentDate;
		if (AcademyConstants.STR_YES.equalsIgnoreCase(strIsShipmentCreatetsRequired)) {
			currentDate = simpleDateFormat.parse(strShipmentCreatets);
		}
		else {
			currentDate = new Date();
		}
       		
		// find next 15 days from current shipment date
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentDate);
		int futureNoOfDays = 15;						
		cal.add(Calendar.DATE, futureNoOfDays);
		String strFutureDate = simpleDateFormat.format(cal.getTime());	
		log.verbose("future date - " + strFutureDate);
		Date futureDate = simpleDateFormat.parse(strFutureDate);

		log.verbose("preparing input to get calendar day details");
		YFCDocument docCalendarDayDtls = YFCDocument.createDocument(AcademyConstants.CALENDAR_ELEMENT);
		docCalendarDayDtls.getDocumentElement().setAttribute(AcademyConstants.CALENDAR_KEY_ATTR, strCalendarKey);
		docCalendarDayDtls.getDocumentElement().setDateTimeAttribute(AcademyConstants.FROM_DATE_ATTR, new YFCDate(currentDate));
		docCalendarDayDtls.getDocumentElement().setDateTimeAttribute(AcademyConstants.TO_DATE_ATTR, new YFCDate(futureDate));

		log.verbose("invoking api for calendar day details");
		Document docOutputCalendarDayDtls = AcademyUtil.invokeAPI(env, AcademyConstants.GET_CAL_DAY_DTLS_API, docCalendarDayDtls.getDocument());//Invoking getCalendarDayDetails
		
		log.verbose("********** calendar details : " + XMLUtil.getXMLString(docOutputCalendarDayDtls));
	
		datesElement = docOutputCalendarDayDtls.getDocumentElement();
		log.verbose(XMLUtil.getElementXMLString(datesElement));

		dateNodesList = XPathUtil.getNodeList(datesElement, "/Calendar/Dates/Date");
		
		if(dateNodesList != null && !YFCObject.isVoid(dateNodesList))
		{
			tempDaysCount = -1;
			numberOfNodes = dateNodesList.getLength();
			log.verbose("number of nodes : " + numberOfNodes);

			for(int iCount=0; iCount<numberOfNodes; iCount++)
			{
				dateElement = (Element)dateNodesList.item(iCount);
				log.verbose("******* date element : " + XMLUtil.getElementXMLString(dateElement));

				shiftsNodeList = dateElement.getElementsByTagName(AcademyConstants.ELE_SHIFTS);
				shiftsElement = (Element)shiftsNodeList.item(0);
				log.verbose("******* shift element : " + XMLUtil.getElementXMLString(shiftsElement));
				
				shiftNodeList = shiftsElement.getElementsByTagName(AcademyConstants.ELE_SHIFT);

				isValid = ((Element)shiftNodeList.item(0)).getAttribute(AcademyConstants.ATTR_VALID_FOR_DAY);

				if(isValid.equalsIgnoreCase(AcademyConstants.STR_YES))//Checking If working day or not
				{
					tempDaysCount++;
				}
				
				if(tempDaysCount == numberOfBusinessDays)//Checking If buisness days added to current date
				{
					expectedDeliveryDate = dateElement.getAttribute(AcademyConstants.DATE_ATTR);
					log.verbose("Expected Delivery date : " + expectedDeliveryDate);
					log.verbose("Expected Delivery date : " + expectedDeliveryDate);
					String deliveryDate = formatDate(expectedDeliveryDate);
					eleShipment.setAttribute(AcademyConstants.ATTR_EXPECTED_DELIVERY_DATE, deliveryDate);
					log.verbose(XMLUtil.getElementXMLString(eleShipment));
					break;
				}
			}
		}
		}
		catch (Exception e) 
		{
			log.verbose("Exception occured while calculating delivery date");
			log.verbose(e.getMessage());
		}
		
		return inputDoc;
	
	}
	
	/**
	 * To get data from custom table ACAD_CEVA_ZIP_CODE
	 * @param strFromZipCode
	 * @param strToZipCode
	 * @param env
	 * @return
	 */
	private Document getEfwZipCodeDetails(YFSEnvironment env,String strFromZipCode,String strToZipCode) {
		log.verbose("AcademyPrintShippingLabelWG.getEFWZipCodeDetails()");
		Document docOutput = null;
		Document docInput = null;		
		String strInputTemplate = "<AcadEFWZipCodeList>" +//Creating Complex query to get data for ZipCodes 
		"<ComplexQuery>" +
		"<Or>" +
		"<Exp Name='PostCode' Value='"+strFromZipCode+"'/>" +
		"<Exp Name='PostCode' Value='"+strToZipCode+"'/>" +
		"</Or>" +
		"</ComplexQuery>" +
		"</AcadEFWZipCodeList>";

		try {
			docInput = XMLUtil.getDocument(strInputTemplate);
			docOutput = AcademyUtil.invokeService(env,
					AcademyConstants.API_ACADEMY_GET_EFW_ZIP_CODE, docInput);//Invoking custom API to get data from custom table

		} catch (Exception e) {
			log.verbose("Exception occurred while retriving data from custom table");
			log.verbose(e.getMessage());
		}
		return docOutput;
	}
	//EFP-17 EFW Carrier Integration - End	
}
