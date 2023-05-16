package com.academy.ecommerce.sterling.shipment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

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
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class AcademyValidateFutureManifest implements YIFCustomApi {
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyValidateFutureManifest.class);
	private Document outDoc = null;

	private Properties props;

	public Document validateFutureManifest(YFSEnvironment env, Document inDoc) throws YFCException {
		log.beginTimer(" Begining of AcademyValidateFutureManifest validateFutureManifest()");
		try {
			
			log.verbose("AcademyValidateFutureManifest_InXML: " + XMLUtil.getXMLString(inDoc));
			Element eleContainer = (Element) inDoc.getDocumentElement();
			String strScac = "";
			strScac = eleContainer.getAttribute(AcademyConstants.ATTR_SCAC);
			String strShipmentKey = eleContainer.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			String strShipmentContainerKey = eleContainer.getAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY);
			
			// Fetch Ship Node and Shipment Type
			String shipNode = "";
			String shipmentType = "";
			NodeList containerShipmentList = eleContainer.getElementsByTagName(AcademyConstants.ELE_SHIPMENT);
			if(containerShipmentList.getLength() > 0) {
				//Fetch the attribute value of ShipNode
				log.verbose("Input Document has a Shipment Elem!");
				Element shipmentElem = (Element) containerShipmentList.item(0);
				shipNode = shipmentElem.getAttribute(AcademyConstants.SHIP_NODE);
				shipmentType = shipmentElem.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE);				
			}
			
			if (YFCObject.isVoid(strShipmentKey) && !YFCObject.isVoid(strShipmentContainerKey)) {
				log.verbose("Inside Bulk Flow - Validate future manifest");
				Document docGetShipContList = XMLUtil.createDocument(AcademyConstants.ELE_CONTAINER);
				docGetShipContList.getDocumentElement().setAttribute(AcademyConstants.ATTR_SHIPMENT_CONT_KEY, strShipmentContainerKey);
				Document outTempGetShipContList = YFCDocument.parse("<Containers> <Container ShipmentContainerKey=\"\" ShipmentKey=\"\" /> </Containers>")
						.getDocument();
				env.setApiTemplate("getShipmentContainerList", outTempGetShipContList);
				Document docOutputGetShipContList = AcademyUtil.invokeAPI(env, "getShipmentContainerList", docGetShipContList);
				env.clearApiTemplates();

				log.verbose("getShipmentContainerList API output: " + XMLUtil.getXMLString(docOutputGetShipContList));
				strShipmentKey = ((Element) docOutputGetShipContList.getDocumentElement().getElementsByTagName("Container").item(0))
						.getAttribute(AcademyConstants.ATTR_SHIPMENT_KEY);
			}
			log.verbose("shipment key:" + strShipmentKey);
			
			log.verbose("Initial Values---");
			log.verbose("Shipment SCAC:" + strScac);
			log.verbose("Ship Node:" + shipNode);
			log.verbose("Shipment Type:" + shipmentType);
			
			// Fetch the values from DB if the values are not available as part of the input document.
			if (YFCObject.isVoid(strScac) || YFCObject.isVoid(shipNode) || YFCObject.isVoid(shipmentType)) {
				// Call getShipmentList API to fetch the data.
				if (!YFCObject.isVoid(strShipmentKey)) {
					Document docOutputGetShipList = getShipmentList(env, strShipmentKey);
					Element getShipmentListShipment = (Element) docOutputGetShipList.getDocumentElement().getElementsByTagName("Shipment").item(0);
					if (YFCObject.isVoid(strScac)) {
						strScac = getShipmentListShipment.getAttribute(AcademyConstants.ATTR_SCAC);
					}
					if (YFCObject.isVoid(shipNode)) {
						shipNode = getShipmentListShipment.getAttribute(AcademyConstants.SHIP_NODE);
					}
					if (YFCObject.isVoid(shipmentType)) {
						shipmentType = getShipmentListShipment.getAttribute(AcademyConstants.ATTR_SHIPMENT_TYPE);
					}	
				}
			}
			
			Document docGetItemListOp = null;
			String strCorrugationItemKey = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_CORRUGATION_ITEM_KEY);			
			log.verbose("strCorrugationItemKey: " + strCorrugationItemKey);
			
			// Corrugation key will be available for the Conveyable(Con), AMMO, Gift Card(GC) and Gift Card Overnight(GCO) Shipment types.
			
			if (!YFCObject.isVoid(strCorrugationItemKey)) {
				
				//Invoke getItemList API to get the Container Type and max weight allowed for container. 				
				Document docGetItemListOpTemplate =YFCDocument.parse("<ItemList>" +
												"<Item ItemKey=\"\" ItemID=\"\">" +
													"<PrimaryInformation ItemType=\"\" />" +
													"<ContainerInformation MaxCntrWeight=\"\" />" +
												"</Item>" +
											"</ItemList>").getDocument();
			
				//Prepare input document for getItemList API
				Document docGetItemListIn = XMLUtil.createDocument(AcademyConstants.ELEM_ITEM);
				docGetItemListIn.getDocumentElement().setAttribute(AcademyConstants.ATTR_ITEM_KEY, strCorrugationItemKey);
				log.verbose("Start-getItemList Input XML");
				log.verbose(XMLUtil.getXMLString(docGetItemListIn));
				log.verbose("End-getItemList Input XML");
				//Invoke getItemList API
				env.setApiTemplate(AcademyConstants.API_GET_ITEM_LIST, docGetItemListOpTemplate);
				docGetItemListOp = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_ITEM_LIST, docGetItemListIn);			
				env.clearApiTemplate(AcademyConstants.API_GET_ITEM_LIST);
				
				log.verbose("getItemList_OutDoc:" + XMLUtil.getXMLString(docGetItemListOp));
			}
			
			log.verbose("Final Values---");
			log.verbose("Shipment SCAC:" + strScac);
			log.verbose("Ship Node:" + shipNode);
			log.verbose("Shipment Type:" + shipmentType);
			
			//Start - STL-733 Changed - Check if valid container is selected based on ammo or non-ammo shipment.
			//If ammo shipment, then check for the maximum weight allowed in container and actual weight of the container.
			validateContainer(env, inDoc, shipmentType, docGetItemListOp);
			//End - STL-733 Changes.
			
			//SFS: getting calendar from store for the carrier if available
			//Start: Process API getOrganizationHierarchy
			//Create root element Organization
			Document docGetOrgDtls = XMLUtil.createDocument("Organization");
			//Fetch attribute value for OrganizationCode
			docGetOrgDtls.getDocumentElement().setAttribute("OrganizationCode", shipNode);
			//Set the template for getOrganizationHierarchy
			Document outTempGetOrgDtls = YFCDocument.parse("<Organization OrganizationCode=\"\"><Calendars> <Calendar CalendarKey=\"\" CalendarId=\"\"/></Calendars></Organization>").getDocument();
			env.setApiTemplate("getOrganizationHierarchy", outTempGetOrgDtls);
			//Invoke Api getOrganizationHierarchy
			Document docOutputOrgDtls = AcademyUtil.invokeAPI(env, "getOrganizationHierarchy", docGetOrgDtls);
			//Clear template
			env.clearApiTemplates();
			//End: Process API getOrganizationHierarchy
			//Fetch the NodeList of element Calender
			NodeList calendarList = docOutputOrgDtls.getDocumentElement().getElementsByTagName("Calendar");
			//Declare the variable
			String strCalendarKey = "";
			//Loop through the NodeList record
			for (int i=0;i<calendarList.getLength();i++)
			{
				//Fetch the element Calender
				Element calendar = (Element) calendarList.item(i);
				//Fetch the attribute value of CalendarId
				String calendarId = calendar.getAttribute("CalendarId");
				//Check if the value of CalendarId =SCAC value
				if(calendarId.equalsIgnoreCase(strScac))
				{
					//Fetch the attribute value of CalendarKey
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
				//Map the attribute value for OrganizationCode
				docGetOrgDtls.getDocumentElement().setAttribute("OrganizationCode", strScac);
				//Set the template for getOrganizationHierarchy
				outTempGetOrgDtls = YFCDocument.parse("<Organization OrganizationCode=\"\" BusinessCalendarKey=\"\" />").getDocument();
				env.setApiTemplate("getOrganizationHierarchy", outTempGetOrgDtls);
				//Invoke API getOrganizationHierarchy
				docOutputOrgDtls = AcademyUtil.invokeAPI(env, "getOrganizationHierarchy", docGetOrgDtls);
				//Clear the template
				env.clearApiTemplates();
				//End: Process API getOrganizationHierarchy
				// obtaining CalendarKey from Organization, if any
				strCalendarKey = docOutputOrgDtls.getDocumentElement().getAttribute("BusinessCalendarKey");
				log.verbose("Calendar Key for Organization : " + strCalendarKey);
			}

			//SFS: getting calendar from store for the carrier if available, end

			// preparing input XML for getTrackingNoForPrintLabel API in
			// AcademyGetTrackingNo service
			// outDoc = XMLUtil.createDocument("Container");
			// inDoc.getDocumentElement().setAttribute("ShipmentContainerKey",
			// strShipmentContainerKey);

			if (!YFCObject.isVoid(strCalendarKey)) {
				String sManifestDate = getActualManifestDate(env, strCalendarKey);
				Element eleManifest = inDoc.createElement("Manifest");
				eleManifest.setAttribute("ManifestDate", sManifestDate);
				inDoc.getDocumentElement().appendChild(eleManifest);
			}

			// gift card changes starts					
			if(docGetItemListOp != null) {
				// Corrugation item key isn't empty.
				Element eleOutItemList = docGetItemListOp.getDocumentElement();
				NodeList itemList = eleOutItemList.getElementsByTagName("Item");
				Element eleOutItem = (Element) itemList.item(0);
				String strItemID = eleOutItem.getAttribute("ItemID");
				log.verbose("**** before if - ItemID is 31 ****" + strItemID);
				//Begin: OMNI-8714
				String strStdPoBox = null;				
				Element eleExtn = (Element) XPathUtil.getNode(eleContainer,
						"/Container/Shipment/ToAddress/Extn");
				if(null != eleExtn) {
					strStdPoBox = eleExtn.getAttribute(AcademyConstants.ATTR_EXTN_IS_PO_BOX);
				}
				
				String strEnableC31ContainerCheckstdStdPOBox = props.getProperty(AcademyConstants.STR_ENABLE_C31_CONTAINER_CHECK_STD_PO_BOX);
				
				if (AcademyConstants.STR_PRIORITY_MAIL.equals(eleContainer.getAttribute("CarrierServiceCode"))
						&& (!AcademyConstants.STR_YES.equals(strStdPoBox) 
								|| (AcademyConstants.STR_YES.equals(strStdPoBox) 
										&& AcademyConstants.STR_YES.equals(strEnableC31ContainerCheckstdStdPOBox)))) {
				//End: OMNI-8714	
					if ("C31".equals(strItemID)) {
						log.verbose("**** after if - ItemID is 31 ****" + strItemID);
						Document docGetShpDtls = XMLUtil.createDocument("Shipment");
						docGetShpDtls.getDocumentElement().setAttribute("ShipmentKey", strShipmentKey);
						Document docGetShipmentDetailsTemplate = null;
						Element eleShipmentLines = null;
						Element eleShipmentLine = null;
						Element eleOrderLine = null;
						Element eleOrderRelease = null;

						docGetShipmentDetailsTemplate = XMLUtil.createDocument("Shipment");
						eleShipmentLines = docGetShipmentDetailsTemplate.createElement("ShipmentLines");
						docGetShipmentDetailsTemplate.getDocumentElement().appendChild(eleShipmentLines);
						eleShipmentLine = docGetShipmentDetailsTemplate.createElement("ShipmentLine");
						eleShipmentLines.appendChild(eleShipmentLine);
						eleOrderLine = docGetShipmentDetailsTemplate.createElement("OrderLine");
						eleShipmentLine.appendChild(eleOrderLine);
						eleOrderRelease = docGetShipmentDetailsTemplate.createElement("OrderRelease");
						eleShipmentLine.appendChild(eleOrderRelease);
						env.setApiTemplate("getShipmentDetails", docGetShipmentDetailsTemplate);
						log.verbose("**** GetItemDtails Template ****" + XMLUtil.getXMLString(docGetShipmentDetailsTemplate));
						Document docOutputShpDtls = AcademyUtil.invokeAPI(env, "getShipmentDetails", docGetShpDtls);
						log.verbose("**** GetShipmentDetails output ****" + XMLUtil.getXMLString(docOutputShpDtls));
						env.clearApiTemplates();

						Document docChangeShipment = XMLUtil.createDocument("Shipment");
						Element eleChangeShipment = docChangeShipment.getDocumentElement();
						eleChangeShipment.setAttribute("ShipmentKey", strShipmentKey);
						eleChangeShipment.setAttribute("CarrierServiceCode", AcademyConstants.STR_FIRST_CLASS_MAIL);
						Element eleChangeShipmentLines = docChangeShipment.createElement("ShipmentLines");
						eleChangeShipment.appendChild(eleChangeShipmentLines);

						Element eleShipment = docOutputShpDtls.getDocumentElement();
						NodeList dateShipmentLineList = XPathUtil.getNodeList(eleShipment, "/Shipment/ShipmentLines/ShipmentLine");
						if (dateShipmentLineList != null && !YFCObject.isVoid(dateShipmentLineList)) {
							log.verbose("**** inside if - shipment line exists ****");
							int numberOfNodes = dateShipmentLineList.getLength();

							for (int i = 0; i < numberOfNodes; i++) {
								log.verbose("**** looping through nodes ****");
								Element eleShipmentLine1 = (Element) dateShipmentLineList.item(i);
								String strShipmentLineKey = eleShipmentLine1.getAttribute("ShipmentLineKey");
								NodeList OrderLineList = eleShipmentLine1.getElementsByTagName("OrderLine");
								Element eleOrderLine1 = (Element) OrderLineList.item(0);
								String strOrderLineKey = eleOrderLine1.getAttribute("OrderLineKey");
								NodeList OrderReleaseList = eleShipmentLine1.getElementsByTagName("OrderRelease");
								Element eleOrderRelease1 = (Element) OrderReleaseList.item(0);
								String strOrderReleaseKey = eleOrderRelease1.getAttribute("OrderReleaseKey");

								Element eleChangeShipmentLine = docChangeShipment.createElement("ShipmentLine");
								eleChangeShipmentLines.appendChild(eleChangeShipmentLine);
								eleChangeShipmentLine.setAttribute("ShipmentLineKey", strShipmentLineKey);
								Element eleChangeOrderLine = docChangeShipment.createElement("OrderLine");
								eleChangeShipmentLine.appendChild(eleChangeOrderLine);
								eleChangeOrderLine.setAttribute("OrderLineKey", strOrderLineKey);
								eleChangeOrderLine.setAttribute("CarrierServiceCode", AcademyConstants.STR_FIRST_CLASS_MAIL);
								// Element eleChangeOrderRelease =
								// docChangeShipment.createElement("OrderRelease");
								// eleChangeShipmentLine.appendChild(eleChangeOrderRelease);
								// eleChangeOrderRelease.setAttribute("OrderReleaseKey",
								// strOrderReleaseKey);
								// eleChangeOrderRelease.setAttribute("CarrierServiceCode",
								// "First-Class Mail");
								log.verbose("**** inside for - shipment change input ****" + XMLUtil.getXMLString(docChangeShipment));
							}
							log.verbose("**** after for - final shipment change input ****" + XMLUtil.getXMLString(docChangeShipment));
						}
						log.verbose("**** after for - final shipment change input ****" + XMLUtil.getXMLString(docChangeShipment));
						AcademyUtil.invokeAPI(env, "changeShipment", docChangeShipment);

						eleContainer.setAttribute("CarrierServiceCode", AcademyConstants.STR_FIRST_CLASS_MAIL);
						NodeList shipmentList = eleContainer.getElementsByTagName("Shipment");
						Element eleShip = (Element) shipmentList.item(0);
						eleShip.setAttribute("CarrierServiceCode", AcademyConstants.STR_FIRST_CLASS_MAIL);
						log.verbose("**** changed Input to addContainerToManifest API ****" + XMLUtil.getXMLString(inDoc));
					}
				}
			}
			// gift card changes ends

			log.verbose("**** Input to addContainerToManifest API ****" + XMLUtil.getXMLString(inDoc));
			outDoc = AcademyUtil.invokeAPI(env, "addContainerToManifest", inDoc);
		} catch (Exception ex) {

			ex.printStackTrace();
			log.verbose(ex);
			String errorCodeDesc = null;
			String errorMsg = null;
			String strAgileSysErrorName = props.getProperty("ERROR_NAME");
			String strAgileSysErrorDesc = props.getProperty("ERROR_DESC");
			YFCException yfcEx = null;
			if (ex instanceof YFCException) {
				errorCodeDesc = ((YFCException) ex).getAttribute(YFCException.ERROR_CODE);
				//Start-STL-733
				errorMsg = ((YFCException) ex).getAttribute(YFCException.ERROR_MESSAGE);
				//End-STL-733
			} else if (ex instanceof YFSException) {
				errorCodeDesc = ((YFSException) ex).getErrorCode();
				errorMsg = ((YFSException) ex).getErrorDescription();
			}
			if (errorCodeDesc != null) {
				yfcEx = new YFCException();
				if (errorCodeDesc.equals("java.io.IOException")) {
					yfcEx.setAttribute(YFCException.ERROR_CODE, strAgileSysErrorName);
					yfcEx.setAttribute(YFCException.ERROR_DESCRIPTION, strAgileSysErrorDesc);
				} else if (errorCodeDesc.equals("java.net.UnknownHostException") || errorCodeDesc.equals("java.net.ConnectException")) {
					yfcEx.setAttribute(YFCException.ERROR_CODE, "EXTN_AGILE_ERROR");
					yfcEx.setAttribute(YFCException.ERROR_DESCRIPTION, "Agile Server is down");
				} else if (errorMsg != null) {
					yfcEx.setAttribute(YFCException.ERROR_CODE, errorCodeDesc);
					yfcEx.setAttribute(YFCException.ERROR_DESCRIPTION, errorMsg);
				}
			} else {
				yfcEx = new YFCException(ex);
			}
			throw yfcEx;
		}
		log.endTimer(" End of AcademyValidateFutureManifest validateFutureManifest()");
		return outDoc;
	}

	private String getActualManifestDate(YFSEnvironment env, String sCalendarKey) throws ParserConfigurationException, Exception {
		String fManifestDate = "";

		try {
			log.beginTimer(" Begining of AcademyValidateFutureManifest getActualManifestDate()Api");
			String strDateFormat = AcademyConstants.STR_SIMPLE_DATE_PATTERN;
			SimpleDateFormat sDateFormat = new SimpleDateFormat(strDateFormat);
			Calendar cal = Calendar.getInstance();
			String sCurrentDate = sDateFormat.format(cal.getTime()); // obtaining
			// the
			// current
			// Date
			// in
			// YYYY-MM-DD
			// format
			int currentHr = cal.get(Calendar.HOUR_OF_DAY);
			log.verbose("Current Hour : " + currentHr);

			int futureNoOfDays = 7; // set to days of the week
			cal.add(Calendar.DATE, futureNoOfDays);
			String sFutureDate = sDateFormat.format(cal.getTime()); // setting
			// the
			// future
			// date to a
			// week
			// after
			// current
			// date

			Date currentDate = sDateFormat.parse(sCurrentDate);
			Date futureDate = sDateFormat.parse(sFutureDate);
			log.verbose("Current Date : " + currentDate);
			log.verbose("Future Date : " + futureDate);

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

			// checking the current date details
			Element eleCurrentDate = (Element) nListDates.item(0);
			sShiftCurrentType = eleCurrentDate.getAttribute("Type");
			log.verbose("Current Date Type is " + sShiftCurrentType);
			sShiftCurrentDate = eleCurrentDate.getAttribute("Date");
			fManifestDate = sShiftCurrentDate; // setting ManifestDate to
			// current date by default

			if ("1".equals(sShiftCurrentType) || "2".equals(sShiftCurrentType)) {
				NodeList nListCurrentShifts = eleCurrentDate.getElementsByTagName("Shift");
				int iListCurrentShifts = nListCurrentShifts.getLength();

				for (int p = 0; p < iListCurrentShifts; p++) {
					eleCurrentShift = (Element) nListCurrentShifts.item(p);
					sShiftStartTime = eleCurrentShift.getAttribute("ShiftStartTime");
					sShiftEndTime = eleCurrentShift.getAttribute("ShiftEndTime");

					sShiftStartHr = sShiftStartTime.substring(0, 2);
					sShiftEndHr = sShiftEndTime.substring(0, 2);
					log.verbose("Shift Start Hour : " + sShiftStartHr);
					log.verbose("Shift End Hour : " + sShiftEndHr);

					int iStartHr = Integer.parseInt(sShiftStartHr);
					int iEndHr = Integer.parseInt(sShiftEndHr);
					if (iEndHr > currentHr) {
						log.verbose("Returning current Date");
						return fManifestDate;
					}
				}
			}

			// checking for a valid calendar shift date past the current date
			for (int i = 1; i < iListDates; i++) {
				eleDate = (Element) nListDates.item(i);
				sShiftDate = eleDate.getAttribute("Date");
				sShiftType = eleDate.getAttribute("Type"); // for Type=1 or
				// Type=2: Calendar
				// day is valid

				if ("1".equals(sShiftType) || "2".equals(sShiftType)) {
					fManifestDate = sShiftDate; // ManifestDate set to next
					// valid calendar shift date
					log.verbose("Future Date Type is " + sShiftCurrentType);
					log.verbose("Returning future Date");
					log.verbose("Future Manifest Date : " + fManifestDate);
					return fManifestDate;
				}
			}
			log.verbose("Future Manifest Date : " + fManifestDate);
			log.endTimer(" End of AcademyValidateFutureManifest getActualManifestDate()Api");
			return fManifestDate;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * STL-733
	 * This method checks if valid container is selected as per ammo or non-ammo shipment type.
	 * For ammo shipment, it also checks if the actual weight of the container is within the max weight allowed for the selected container.
	 * @param	env		YFSEnvironment	
	 * @param	inDoc	inDoc
	 * @throws	YFCException
	 */	
	public void validateContainer(YFSEnvironment env, Document inDoc, String shipmentType, Document docGetItemListOp) throws YFCException {
		
		log.beginTimer("AcademyValidateFutureManifest -> validateContainer");
		log.verbose("AcademyValidateFutureManifest_validateContainer_InDoc: " + XMLUtil.getXMLString(inDoc));
			
		XPath objXPath = XPathFactory.newInstance().newXPath();
		String strMaxCntrWeight = "";
		String strContainerType = "";
		String strXPathExpression = null;
		
		String strActualCntrWeight = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_ACTUAL_WEIGHT);		
		log.verbose("strActualCntrWeight: " + strActualCntrWeight);
					
		/*	Start STL-735: Check for Level Of Service i.e. SCAC and CarrierServiceCode for AMMO Shipment.
			Only FedEx Ground and FedEx Home Delivery is allowed for AMMO shipments
		*/
		//Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
		if(AcademyConstants.AMMO_SHIPMENT_TYPE.equals(shipmentType) || AcademyConstants.HAZMAT_SHIPMENT_TYPE.equals(shipmentType)) {
			//log.verbose("It's an AMMO Shipment!");
			log.verbose("It's an HAZMAT Shipment!");
			//validateAmmoLOS(env, inDoc);
			validateLOS(env, inDoc, shipmentType);
		}
		//End WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
		//End STL-735
		
		if (docGetItemListOp != null) {
		// To get the Container Type and max weight allowed for container.
		log.verbose("Fetch Container Type and max weight allowed for container.");	
		try {								
			//Fetch Container Type 
			strXPathExpression = "/ItemList/Item[1]/PrimaryInformation[1]";
			Element elePrimaryInformation = (Element)objXPath.compile(strXPathExpression).evaluate(docGetItemListOp, XPathConstants.NODE);
			strContainerType = elePrimaryInformation.getAttribute(AcademyConstants.ATTR_ITEM_TYPE);
			log.verbose("Container Type"+":"+strContainerType);
			//Fetch max weight allowed in container
			strXPathExpression = "/ItemList/Item[1]/ContainerInformation[1]";
			Element eleContainerInformation = (Element)objXPath.compile(strXPathExpression).evaluate(docGetItemListOp, XPathConstants.NODE);
			strMaxCntrWeight = eleContainerInformation.getAttribute(AcademyConstants.ATTR_MAX_CNTR_WEIGHT);
			log.verbose("strMaxCntrWeight: " + strMaxCntrWeight);
		}
		catch (Exception e){
			log.verbose("Exception occurred while extracting item details");
			log.verbose(e.getMessage());
			YFCException expItemDetails = new YFCException (e.getMessage());
			expItemDetails.setAttribute(YFCException.ERROR_CODE, "Container Detail Error");
			expItemDetails.setAttribute(YFCException.ERROR_DESCRIPTION, "Container details could not be fetched");
			expItemDetails.setAttribute(YFCException.ERROR_MESSAGE, "Container details could not be fetched");
			throw expItemDetails;
		}
		
		//If it is an ammo shipment. Container's ItemType should be AmmoContainer and actual weight should not be more than MaxCntrWeight value.
		//Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
		if(AcademyConstants.AMMO_SHIPMENT_TYPE.equals(shipmentType) || AcademyConstants.HAZMAT_SHIPMENT_TYPE.equals(shipmentType)) {
			log.verbose("*****Inside " +shipmentType +" shipmenttype condition");
			//End WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
			// check for Ammo container type
			if(!AcademyConstants.AMMO_CNTR_ITEM_TYPE.equals(strContainerType)) {
				log.verbose("*****Only ammo containers could be used for ammo/hazmat shipments ");
				YFCException expInvalidContainer = new YFCException("Only ammo containers could be used for ammo/hazmat shipments ");
				expInvalidContainer.setAttribute(YFCException.ERROR_CODE, "Invalid Container Selected");
				expInvalidContainer.setAttribute(YFCException.ERROR_DESCRIPTION, "Only ammo containers could be used for ammo/hazmat shipments ");
				expInvalidContainer.setAttribute(YFCException.ERROR_MESSAGE, "Only ammo containers could be used for ammo/hazmat shipments ");
				throw expInvalidContainer;				
			}
			log.verbose("AmmoContainer is selected for " +shipmentType +" shipmenttype");
			// check for Max Weight allowed for Ammo container
			if(Float.parseFloat(strMaxCntrWeight) < Float.parseFloat(strActualCntrWeight)) {
				log.verbose("*****Ammo containers cannot be processed if the actual weight is more than the max allowed weight");
				YFCException expInvalidContainer = new YFCException("Maximum weight allowed for the selected container is " + strMaxCntrWeight + " LBS");
				expInvalidContainer.setAttribute(YFCException.ERROR_CODE, "Max Allowed Weight Exceeded"); 
				expInvalidContainer.setAttribute(YFCException.ERROR_DESCRIPTION, "Maximum weight allowed for the selected container is " + strMaxCntrWeight + " LBS");
				expInvalidContainer.setAttribute(YFCException.ERROR_MESSAGE, "Maximum weight allowed for the selected container is " + strMaxCntrWeight + " LBS");
				throw expInvalidContainer;
			}			
			log.verbose("*****All " +shipmentType +" checks successful");
		}
		//If it is a non-ammo shipment. Any container could be used but ItemType as AmmoContainer
		else {
			log.verbose("*****Inside NON-AMMO shipmenttype condition");
			if(AcademyConstants.AMMO_CNTR_ITEM_TYPE.equals(strContainerType)) {
				log.verbose("*****Ammo containers could not be used for non-ammo/hazmat shipments");
				YFCException expInvalidContainer = new YFCException("Only non-ammo containers could be used for non-ammo/hazmat shipments.");
				expInvalidContainer.setAttribute(YFCException.ERROR_CODE, "Invalid Container Selected");
				expInvalidContainer.setAttribute(YFCException.ERROR_DESCRIPTION, "Only non-ammo containers could be used for non-ammo/hazmat shipments");
				expInvalidContainer.setAttribute(YFCException.ERROR_MESSAGE, "Only non-ammo containers could be used for non-ammo/hazmat shipments");
				throw expInvalidContainer;
			}
			log.verbose("*****Non-ammo checks successful");
		}
	}
	
	log.endTimer("AcademyValidateFutureManifest -> validateContainer");
	} //End of validateContainer()
	
	/**
	 * STL-735 This method is used to validate the Level Of Service for AMMO
	 * shipments. For AMMO shipments, only FedEx Ground or FedEx Home Delivery
	 * could be used, which is configured under common code AMMO_LOS.
	 * 
	 * @param env
	 *            YFSEnvironment
	 * @param inDoc
	 *            Document
	 * @throws YFCException
	 */
	public void validateLOS(YFSEnvironment env, Document inDoc, String shipmentType)
			throws YFCException {
		log.beginTimer("AcademyValidateFutureManifest->validateLOS");
		String strSCAC;
		String strCarrierServiceCode;
		//Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
		String strHazmatSCAC;
		String strHazmatCarrierServiceCode;
		//End WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
		String strXPathExpression;
		Document docGetCommonCodeListIn;
		Document docGetCommonCodeListOp;
		Document docGetCommonCodeListOpTemplate;
		XPath objXPath = XPathFactory.newInstance().newXPath();
		boolean isValidLOSForAmmo = false;
		try {
			// Fetch SCAC and Carrier Service Code from input Document.
			strSCAC = inDoc.getDocumentElement().getAttribute(
					AcademyConstants.ATTR_SCAC);
			strCarrierServiceCode = inDoc.getDocumentElement().getAttribute(
					AcademyConstants.CARRIER_SERVICE_CODE);
			log.verbose("Input SCAC: " + strSCAC);
			log.verbose("Input CarrierServiceCode: " + strCarrierServiceCode);

			//Prepare output template for getCommonCodeList API
			docGetCommonCodeListOpTemplate = YFCDocument
					.parse(
							"<CommonCodeList>"
									+ "<CommonCode CodeLongDescription=\"\" CodeShortDescription=\"\" CodeType=\"\" CodeValue=\"\" />"
									+ "</CommonCodeList>").getDocument();
			// Prepare input document for getCommonCodeList API
			docGetCommonCodeListIn = XMLUtil
					.createDocument(AcademyConstants.ELE_COMMON_CODE);
			
			//Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
			/*docGetCommonCodeListIn.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_CODE_TYPE,
					AcademyConstants.STR_AMMO_LOS);*/
			docGetCommonCodeListIn.getDocumentElement().setAttribute(
					AcademyConstants.ATTR_CODE_TYPE,
					AcademyConstants.STR_HAZMAT_LOS);
			//End WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
			
			log.verbose("Start-getCommonCodeList Input XML");
			log.verbose(XMLUtil.getXMLString(docGetCommonCodeListIn));
			log.verbose("End-getCommonCodeList Input XML");
			// Invoke getCommonCodeList API
			env.setApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST,
					docGetCommonCodeListOpTemplate);
			docGetCommonCodeListOp = AcademyUtil.invokeAPI(env,
					AcademyConstants.API_GET_COMMONCODE_LIST,
					docGetCommonCodeListIn);
			env.clearApiTemplate(AcademyConstants.API_GET_COMMONCODE_LIST);

			//Fetch CommonCode list from the output document of getCommonCodeList API
			strXPathExpression = "/CommonCodeList/CommonCode";
			NodeList commonCodeList = (NodeList) objXPath.compile(
					strXPathExpression).evaluate(docGetCommonCodeListOp,
					XPathConstants.NODESET);
			
			//Check SCAC and CarrierServiceCode 
			int noOfCommonCode = commonCodeList.getLength();
			for (int i = 0; i < noOfCommonCode; i++) {
				Element eleCommonCode = (Element) commonCodeList.item(i);
				/*Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
				There is no functional change here except rename Ammo with Hazmat*/
				strHazmatSCAC = eleCommonCode
						.getAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC);
				strHazmatCarrierServiceCode = eleCommonCode
						.getAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE);
				log.verbose("Fetched Common Code Details");
				log.verbose(shipmentType +" SCAC: " + strHazmatSCAC);
				log.verbose(shipmentType +" Carrier Service Code"
						+ strHazmatCarrierServiceCode);
				//Check if SCAC and CarrierServiceCode is found
				if (strHazmatSCAC.equals(strSCAC)
						&& (strHazmatCarrierServiceCode
								.equals(strCarrierServiceCode))) {
					log
							.verbose("Correct SCAC and Carrier Service selected for Hazmat.");
					isValidLOSForAmmo = true;
					break;
				//End WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
				}
			}
		} catch (Exception e) {
			log
					.verbose("Exception occurred while extracting Level Of Service details");
			log.verbose(e.getMessage());
			YFCException expLOSDetails = new YFCException(e.getMessage());
			expLOSDetails
					.setAttribute(YFCException.ERROR_CODE, "LOS Exception");
			expLOSDetails
					.setAttribute(YFCException.ERROR_DESCRIPTION,
							"Exception occurred while extracting Level Of Service details");
			expLOSDetails
					.setAttribute(YFCException.ERROR_MESSAGE,
							"Exception occurred while extracting Level Of Service details");
			throw expLOSDetails;
		}

		//Provided SCAC and CarrierServiceCode combination is not allowed for AMMO shipment.
		if (!isValidLOSForAmmo) {
			log
					.verbose("*****Invalid Level Of Service selected for " +shipmentType +" shipment");
			YFCException expInvalidLOS = new YFCException(
					"For Ammo/Hazmat Shipments, only Fedex Home Delivery and Ground LOS could be used");
			expInvalidLOS.setAttribute(YFCException.ERROR_CODE,
					"Invalid LOS for " +shipmentType +" shipment");
			expInvalidLOS.setAttribute(YFCException.ERROR_DESCRIPTION,
					"For Ammo/Hazmat Shipments, only Fedex Home Delivery and Fedex Ground LOS could be used");
			expInvalidLOS.setAttribute(YFCException.ERROR_MESSAGE,
					"For Ammo/Hazmat Shipments, only Fedex Home Delivery and Fedex Ground LOS could be used");
			throw expInvalidLOS;
		}

		log.endTimer("AcademyValidateFutureManifest->validateLOS");
	}//End of  validateAmmoLOS
	
	private Document getShipmentList(YFSEnvironment env, String strShipmentKey) throws Exception {
		
		Document docGetShipList = XMLUtil.createDocument("Shipment");
		docGetShipList.getDocumentElement().setAttribute("ShipmentKey", strShipmentKey);
		Document outTempGetShipList = YFCDocument.parse("<Shipments> <Shipment SCAC=\"\" ShipNode=\"\" ShipmentType=\"\" ShipmentKey=\"\" /> </Shipments>").getDocument();
		env.setApiTemplate("getShipmentList", outTempGetShipList);
		Document docOutputGetShipList = AcademyUtil.invokeAPI(env, "getShipmentList", docGetShipList);
		env.clearApiTemplates();
		log.verbose("getShipmentList API output: " + XMLUtil.getXMLString(docOutputGetShipList));
		
		return docOutputGetShipList;
	}

	public void setProperties(Properties props) {
		this.props = props;
	}

}
