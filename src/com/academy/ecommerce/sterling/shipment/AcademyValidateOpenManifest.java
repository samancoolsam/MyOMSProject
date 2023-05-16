package com.academy.ecommerce.sterling.shipment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCDate;

public class AcademyValidateOpenManifest implements YIFCustomApi {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyValidateOpenManifest.class);
	private Document outDoc = null;

	public Document validateOpenManifest(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("**** Input to validateOpenManifest ****" + XMLUtil.getXMLString(inDoc));
		log.beginTimer(" Begining of AcademyValidateOpenManifest validateOpenManifest()Api");
		Element eleContainer = (Element) inDoc.getDocumentElement().getElementsByTagName("Container").item(0);
		Boolean isWebStoreFlow = false;
		
		if(YFCCommon.isVoid(eleContainer)) {
			eleContainer = inDoc.getDocumentElement();
			env.setTxnObject(AcademyConstants.A_IS_WEB_STORE_FLOW, "Y");
			isWebStoreFlow = true;
		}

		String strScac = eleContainer.getAttribute("SCAC");
		String strShipmentContainerKey = eleContainer.getAttribute("ShipmentContainerKey");

		//SFS: get calendar if configured for the store
		//Fetch the attribute value of ShipmentKey
		String strShipmentKey = eleContainer.getAttribute("ShipmentKey");
		//Declare variable shipnode
		String shipNode = "";
		//Fetch the NodeList of node Shipment
		NodeList containerShipmentList = eleContainer.getElementsByTagName("Shipment");
		//Check if NodeList record is greater than zero
		if(containerShipmentList.getLength() > 0)
		{
			//Fetch the attribute value of ShipNode
			shipNode = ((Element)containerShipmentList.item(0)).getAttribute("ShipNode");
		}
		//Check if ShipmentKey is void and ShipmentContainerKey is not void
		if (YFCObject.isVoid(strShipmentKey) && !YFCObject.isVoid(strShipmentContainerKey))
		{
			log.verbose("Inside Bulk Flow - Validate future manifest");
			//Start: Process API getShipmentContainerList
			//Create root element Container
			Document docGetShipContList = XMLUtil.createDocument("Container");
			//Set attribute value of ShipmentContainerKey
			docGetShipContList.getDocumentElement().setAttribute("ShipmentContainerKey", strShipmentContainerKey);
			//Set the template for getShipmentContainerList API
			Document outTempGetShipContList = YFCDocument.parse("<Containers> <Container ShipmentContainerKey=\"\" ShipmentKey=\"\" /> </Containers>")
					.getDocument();
			env.setApiTemplate("getShipmentContainerList", outTempGetShipContList);
			//Invoke API getShipmentContainerList
			Document docOutputGetShipContList = AcademyUtil.invokeAPI(env, "getShipmentContainerList", docGetShipContList);
			//Clear the template
			env.clearApiTemplates();
			//End: Process API getShipmentContainerList
			log.verbose("getShipmentContainerList API output: " + XMLUtil.getXMLString(docOutputGetShipContList));
			//Fetch attribute ShipmentKey from the element Container
			strShipmentKey = ((Element) docOutputGetShipContList.getDocumentElement().getElementsByTagName("Container").item(0))
					.getAttribute("ShipmentKey");

		}
		log.verbose("shipment key:" + strShipmentKey);
		//Check if SCAC or ShipNode is void and if ShipmentKey is not void
		if ((YFCObject.isVoid(strScac) || YFCObject.isVoid(shipNode)) && !YFCObject.isVoid(strShipmentKey)) {
			log.verbose("Finding SCAC for the shipment");
			//Start: Process API getShipmentList
			//Create element Shipment
			Document docGetShipList = XMLUtil.createDocument("Shipment");
			//Set attribute value of ShipmentKey
			docGetShipList.getDocumentElement().setAttribute("ShipmentKey", strShipmentKey);
			//Set the templete for getShipmentList
			Document outTempGetShipList = YFCDocument.parse("<Shipments> <Shipment SCAC=\"\" ShipNode=\"\" ShipmentKey=\"\" /> </Shipments>").getDocument();
			env.setApiTemplate("getShipmentList", outTempGetShipList);
			//Invoke API getShipmentList
			Document docOutputGetShipList = AcademyUtil.invokeAPI(env, "getShipmentList", docGetShipList);
			//Clear the template
			env.clearApiTemplates();
			//End: Process API getShipmentList
			log.verbose("getShipmentList API output: " + XMLUtil.getXMLString(docOutputGetShipList));
			//Fetch element Shipment
			Element getShipmentListShipment = (Element) docOutputGetShipList.getDocumentElement().getElementsByTagName("Shipment").item(0);
			//Fetch the attribute value of SCAC
			strScac = getShipmentListShipment.getAttribute("SCAC");
			//Fetch the attribute value of ShipNode
			shipNode = getShipmentListShipment.getAttribute("ShipNode");
		}
		//Start: Process API getOrganizationHierarchy
		//Create root element Organization
		Document docGetOrgDtls = XMLUtil.createDocument("Organization");
		//Map the value for OrganizationCode
		docGetOrgDtls.getDocumentElement().setAttribute("OrganizationCode", shipNode);
		//Set the template for  getOrganizationHierarchy
		Document outTempGetOrgDtls = YFCDocument.parse("<Organization OrganizationCode=\"\"><Calendars> <Calendar CalendarKey=\"\" CalendarId=\"\"/></Calendars></Organization>").getDocument();
		env.setApiTemplate("getOrganizationHierarchy", outTempGetOrgDtls);
		//Invoke getOrganizationHierarchyAPI
		Document docOutputOrgDtls = AcademyUtil.invokeAPI(env, "getOrganizationHierarchy", docGetOrgDtls);
		//Clear the template
		env.clearApiTemplates();
		//End: Process API getOrganizationHierarchy
		//Fetch the NodeList of Node Calender
		NodeList calendarList = docOutputOrgDtls.getDocumentElement().getElementsByTagName("Calendar");
		//Declare the variable
		String strCalendarKey = "";
		//Loop through the NodeList record
		for (int i=0;i<calendarList.getLength();i++)
		{
			//Fetch the element Calendar
			Element calendar = (Element) calendarList.item(i);
			//Fetch the element CalendarId
			String calendarId = calendar.getAttribute("CalendarId");
			//Check if the Value of calendarId=SCAC value
			if(calendarId.equalsIgnoreCase(strScac))
			{
				//Fetch the the attribute value of CalendarKey
				strCalendarKey = calendar.getAttribute("CalendarKey");
				break;
			}
		}
		//Check if CalendarKey is blank
		if(strCalendarKey.equals(""))
		{
			//Start: Process API getOrganizationHierarchy
			//Create root element Organization
			docGetOrgDtls = XMLUtil.createDocument("Organization");
			//Map the value for attribute OrganizationCode
			docGetOrgDtls.getDocumentElement().setAttribute("OrganizationCode", strScac);
			//Set the template for the API getOrganizationHierarchy
			outTempGetOrgDtls = YFCDocument.parse("<Organization OrganizationCode=\"\" BusinessCalendarKey=\"\" />").getDocument();
			env.setApiTemplate("getOrganizationHierarchy", outTempGetOrgDtls);
			//Invoke getOrganizationHierarchy API
			docOutputOrgDtls = AcademyUtil.invokeAPI(env, "getOrganizationHierarchy", docGetOrgDtls);
			//Clear the template
			env.clearApiTemplates();
			//End: Process API getOrganizationHierarchy
			// obtaining CalendarKey from Organization, if any
			strCalendarKey = docOutputOrgDtls.getDocumentElement().getAttribute("BusinessCalendarKey");
			log.verbose("Calendar Key for Organization : " + strCalendarKey);
		}

		//SFS: get calendar if configured for the store, end

		//preparing input XML for getTrackingNoForPrintLabel API in AcademyGetTrackingNo service
		outDoc = XMLUtil.createDocument("Container");
		outDoc.getDocumentElement().setAttribute("ShipmentContainerKey", strShipmentContainerKey);

		if(!YFCObject.isVoid(strCalendarKey)){
			String sManifestDate = getActualManifestDate(env, strCalendarKey);
			Element eleManifest = outDoc.createElement("Manifest");
			eleManifest.setAttribute("ManifestDate", sManifestDate);
			outDoc.getDocumentElement().appendChild(eleManifest);
		}
		log.endTimer(" End of AcademyValidateOpenManifest validateOpenManifest()Api");
		
		// BOPIS: Updated the code to print the packslip for bulk orders from webstore
		if(isWebStoreFlow) {
			env.setTxnObject("SFS_ShipNode", shipNode);			
		}
		
		return outDoc;
	}

	private String getActualManifestDate(YFSEnvironment env, String sCalendarKey) throws ParserConfigurationException, Exception {
		String fManifestDate = "";

		try{
			log.beginTimer(" Begining of AcademyValidateOpenManifest getActualManifestDate()Api");
		String strDateFormat = AcademyConstants.STR_SIMPLE_DATE_PATTERN;
		SimpleDateFormat sDateFormat = new SimpleDateFormat(strDateFormat);
		Calendar cal = Calendar.getInstance();
		String sCurrentDate = sDateFormat.format(cal.getTime());					//obtaining the current Date in YYYY-MM-DD format
		int currentHr = cal.get(Calendar.HOUR_OF_DAY);
		log.verbose("Current Hour : "+ currentHr);

		int futureNoOfDays = 7;							//set to days of the week
		cal.add(Calendar.DATE, futureNoOfDays);
		String sFutureDate = sDateFormat.format(cal.getTime());					//setting the future date to a week after current date

		Date currentDate = sDateFormat.parse(sCurrentDate);
		Date futureDate = sDateFormat.parse(sFutureDate);
		log.verbose("Current Date : "+ currentDate);
		log.verbose("Future Date : "+ futureDate);

		YFCDocument docCalendarDayDtls = YFCDocument.createDocument("Calendar");
		docCalendarDayDtls.getDocumentElement().setAttribute("CalendarKey", sCalendarKey);
		docCalendarDayDtls.getDocumentElement().setDateTimeAttribute("FromDate", new YFCDate(currentDate));
		docCalendarDayDtls.getDocumentElement().setDateTimeAttribute("ToDate", new YFCDate(futureDate));

		Document docOutputCalendarDayDtls = AcademyUtil.invokeAPI(env, "getCalendarDayDetails", docCalendarDayDtls.getDocument());
		NodeList nListDates = docOutputCalendarDayDtls.getElementsByTagName("Date");
		int iListDates = nListDates.getLength();

		String sShiftDate = "";
		String sShiftCurrentDate = "";
		String sShiftCurrentType = "";
		String sShiftStartTime = "";
		String sShiftEndTime = "";
		String sShiftStartHr = "";
		String sShiftEndHr = "";
		String sShiftType = "";
		Element eleDate = null;
		Element eleCurrentShift = null;

		//checking the current date details
		Element eleCurrentDate = (Element) nListDates.item(0);
		sShiftCurrentType = eleCurrentDate.getAttribute("Type");
		sShiftCurrentDate = eleCurrentDate.getAttribute("Date");
		fManifestDate = sShiftCurrentDate;									//setting ManifestDate to current date by default

		if("1".equals(sShiftCurrentType)|| "2".equals(sShiftCurrentType) ){
			NodeList nListCurrentShifts = eleCurrentDate.getElementsByTagName("Shift");
			int iListCurrentShifts = nListCurrentShifts.getLength();

			for(int p=0;p<iListCurrentShifts;p++){
				eleCurrentShift = (Element) nListCurrentShifts.item(p);
				sShiftStartTime = eleCurrentShift.getAttribute("ShiftStartTime");
				sShiftEndTime = eleCurrentShift.getAttribute("ShiftEndTime");

				sShiftStartHr = sShiftStartTime.substring(0,2);
				sShiftEndHr = sShiftEndTime.substring(0,2);

				int iStartHr = Integer.parseInt(sShiftStartHr);
				int iEndHr = Integer.parseInt(sShiftEndHr);
				if(iEndHr > currentHr){
					log.verbose("Returning current Date");
					return fManifestDate;
				}
			}
		}

		//checking for a valid calendar shift date past the current date
		for(int i=1;i < iListDates; i++){
			 eleDate = (Element) nListDates.item(i);
			 sShiftDate = eleDate.getAttribute("Date");
			 sShiftType = eleDate.getAttribute("Type");							//for Type=1: Calendar day is valid
			 if("1".equals(sShiftType)|| "2".equals(sShiftType)){
				 fManifestDate = sShiftDate;									//ManifestDate set to next valid calendar shift date
				 log.verbose("Returning future Date");
				 log.verbose("Future Manifest Date : " + fManifestDate);
				 return fManifestDate;
			 }
		}
		log.endTimer(" End of AcademyValidateOpenManifest getActualManifestDate()Api");
		return fManifestDate;
		}catch (Exception e){
			throw e;
		}
	}

	public void setProperties(Properties arg0) throws Exception {
	}

}
