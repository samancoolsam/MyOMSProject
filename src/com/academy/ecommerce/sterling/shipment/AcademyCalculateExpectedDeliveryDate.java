package com.academy.ecommerce.sterling.shipment;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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

public class AcademyCalculateExpectedDeliveryDate implements YIFCustomApi 
{
	private static final YFCLogCategory log = YFCLogCategory.instance(AcademyCalculateExpectedDeliveryDate.class);

	public Document calculateDeliveryDate(YFSEnvironment env, Document inXML)
	{
		try
		{
			Element inputXML = inXML.getDocumentElement();
			log.verbose("******* INPUT - " + XMLUtil.getElementXMLString(inputXML));
			log.verbose("******* INPUT - " + XMLUtil.getElementXMLString(inputXML));
			
			String currentShipmentKey = inputXML.getAttribute("CurrentShipmentKey");
			Element shipment = (Element)XPathUtil.getNode(inputXML, "/Order/Shipments/Shipment[@ShipmentKey=\"" + currentShipmentKey + "\"]");
			log.verbose("********* shipment element : " + XMLUtil.getElementXMLString(shipment));
			log.verbose("********* shipment element : " + XMLUtil.getElementXMLString(shipment));
			
			String scac = shipment.getAttribute("SCAC");
			String scacAndService = shipment.getAttribute("ScacAndService");
			String actualShipDate = shipment.getAttribute("ActualShipmentDate");
			log.verbose("scac - " + scac);
			log.verbose("ScacAndService - " + scacAndService);
			log.verbose("ActualShipmentDate - " + actualShipDate);
			
			int numberOfBusinessDays = getValueFromCommonCodes(env, scacAndService);
			log.verbose("No of Business Days : " + numberOfBusinessDays);
			log.verbose("No of Business Days : " + numberOfBusinessDays);

			if(numberOfBusinessDays > 0)
			{
				Document docGetOrgDtls = XMLUtil.createDocument("Organization");
				docGetOrgDtls.getDocumentElement().setAttribute("OrganizationCode", scac);
				Document outTempGetOrgDtls = YFCDocument.parse("<Organization OrganizationCode=\"\" BusinessCalendarKey=\"\" />").getDocument();
				env.setApiTemplate("getOrganizationHierarchy", outTempGetOrgDtls);
				Document docOutputOrgDtls = AcademyUtil.invokeAPI(env, "getOrganizationHierarchy", docGetOrgDtls);
				env.clearApiTemplates();
				
				log.verbose("organization list - " + XMLUtil.getXMLString(docOutputOrgDtls));
				log.verbose("organization list - " + XMLUtil.getXMLString(docOutputOrgDtls));
				String strCalendarKey = docOutputOrgDtls.getDocumentElement().getAttribute("BusinessCalendarKey");
				log.verbose("calendar key " + strCalendarKey);
		
				// current shipment date in new format
				String strCurrentDate = formatDate(actualShipDate, AcademyConstants.STR_DATE_TIME_PATTERN, AcademyConstants.STR_SIMPLE_DATE_PATTERN);
				log.verbose("current shipment date in new format - " + strCurrentDate);
		
				// getting list of next 15 days
				String strDatePattern = AcademyConstants.STR_SIMPLE_DATE_PATTERN;
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(strDatePattern);

				// actual shipment date
				Date currentDate = simpleDateFormat.parse(strCurrentDate);
				
				// find next 15 days from current shipment date
				Calendar cal = Calendar.getInstance();
				cal.setTime(currentDate);
				int futureNoOfDays = 15;						
				cal.add(Calendar.DATE, futureNoOfDays);
				String strFutureDate = simpleDateFormat.format(cal.getTime());	
				log.verbose("future date - " + strFutureDate);
				Date futureDate = simpleDateFormat.parse(strFutureDate);

				log.verbose("preparing input to get calendar day details");
				YFCDocument docCalendarDayDtls = YFCDocument.createDocument("Calendar");
				docCalendarDayDtls.getDocumentElement().setAttribute("CalendarKey", strCalendarKey);
				docCalendarDayDtls.getDocumentElement().setDateTimeAttribute("FromDate", new YFCDate(currentDate));
				docCalendarDayDtls.getDocumentElement().setDateTimeAttribute("ToDate", new YFCDate(futureDate));

				log.verbose("invoking api for calendar day details");
				Document docOutputCalendarDayDtls = AcademyUtil.invokeAPI(env, "getCalendarDayDetails", docCalendarDayDtls.getDocument());
				
				log.verbose("********** calendar details : " + XMLUtil.getXMLString(docOutputCalendarDayDtls));
				log.verbose("********** calendar details : " + XMLUtil.getXMLString(docOutputCalendarDayDtls));
			
				Element datesElement = docOutputCalendarDayDtls.getDocumentElement();
				log.verbose(XMLUtil.getElementXMLString(datesElement));

				NodeList dateNodesList = XPathUtil.getNodeList(datesElement, "/Calendar/Dates/Date");
				
				if(dateNodesList != null && !YFCObject.isVoid(dateNodesList))
				{
					String expectedDeliveryDate = "";
					int tempDaysCount = -1;
					int numberOfNodes = dateNodesList.getLength();
					log.verbose("number of nodes : " + numberOfNodes);

					for(int i=0; i<numberOfNodes; i++)
					{
						Element dateElement = (Element)dateNodesList.item(i);
						log.verbose("******* date element : " + XMLUtil.getElementXMLString(dateElement));

						NodeList shiftsNodeList = dateElement.getElementsByTagName("Shifts");
						Element shiftsElement = (Element)shiftsNodeList.item(0);
						log.verbose("******* shift element : " + XMLUtil.getElementXMLString(shiftsElement));
						
						NodeList shiftNodeList = shiftsElement.getElementsByTagName("Shift");

						String isValid = ((Element)shiftNodeList.item(0)).getAttribute("ValidForDay");

						if(isValid.equalsIgnoreCase("Y"))
						{
							tempDaysCount++;
						}
						
						if(tempDaysCount == numberOfBusinessDays)
						{
							expectedDeliveryDate = dateElement.getAttribute("Date");
							log.verbose("Expected Delivery date : " + expectedDeliveryDate);
							log.verbose("Expected Delivery date : " + expectedDeliveryDate);
							String deliveryDate = formatDate(expectedDeliveryDate, AcademyConstants.STR_SIMPLE_DATE_PATTERN, AcademyConstants.STR_DATE_TIME_PATTERN);
							shipment.setAttribute("ExpectedDeliveryDate", deliveryDate);
							log.verbose(XMLUtil.getElementXMLString(shipment));
							log.verbose(XMLUtil.getElementXMLString(shipment));
							break;
						}
					}
				}
			}
		} 
		catch (ParserConfigurationException e) 
		{
			e.printStackTrace();
		} 
		catch (SAXException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	
		log.verbose("************ output xml : " + XMLUtil.getXMLString(inXML));
		log.verbose("************ output xml : " + XMLUtil.getXMLString(inXML));
		return inXML;
	}

	// method to get number of business days from common code for given scacAndService
	private int getValueFromCommonCodes(YFSEnvironment env, String scacAndService)
	{
		int numberOfBusinessDays = 0;
		try 
		{
			// get number of business days need to deliver shipment from common code list
			Document getCommonCodeListInputXML = XMLUtil.createDocument("CommonCode");
			getCommonCodeListInputXML.getDocumentElement().setAttribute("CodeType", "SHP_DELVRY_DAYS");
			Document outXML=AcademyUtil.invokeAPI(env, "getCommonCodeList", getCommonCodeListInputXML);
			if(outXML != null)
			{
				NodeList commonCodeList = outXML.getElementsByTagName("CommonCode");
				if (commonCodeList!=null && !YFCObject.isVoid(commonCodeList)) 
				{
					log.verbose("CommonCodeList for SHP_DELIVRY_DAY : " + commonCodeList.toString());
					log.verbose("CommonCodeList for SHP_DELIVRY_DAY : " + commonCodeList.toString());
					int iLength = commonCodeList.getLength();
					for (int i = 0; i < iLength; i++) 
					{
						Element commonCode = (Element) commonCodeList.item(i);
						String codeValue = commonCode.getAttribute("CodeValue");
			    		log.verbose("SCACAndService : " + scacAndService);
			    		log.verbose("Code value : " + codeValue);
			    		log.verbose("SCACAndService : " + scacAndService);
			    		log.verbose("Code value : " + codeValue);
						if(scacAndService.equals(codeValue))
						{
							numberOfBusinessDays = Integer.parseInt(commonCode.getAttribute("CodeShortDescription"));
				    		log.verbose("number Of business days  : " + numberOfBusinessDays);
				    		log.verbose("number Of business days  : " + numberOfBusinessDays);
						}
					}
				}
			}
		}
		catch (ParserConfigurationException e) 
		{
			e.printStackTrace();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return numberOfBusinessDays;
	}

	// method to change the format of date
	private String formatDate(String date, String fromDatePattern, String toDatePattern)
	{
		try 
		{
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(fromDatePattern);
			Date parsedDate = simpleDateFormat.parse(date);
			simpleDateFormat.applyPattern(toDatePattern);
			date = simpleDateFormat.format(parsedDate);
		} 
		catch (ParseException e) 
		{
			e.printStackTrace();
		}
		
		return date;
	}
	
	public void setProperties(Properties arg0) throws Exception 
	{}
	
	public static void main(String[] args) throws Exception 
	{
		Document doc = YFCDocument.getDocumentFor(new File("C://Noname2.xml")).getDocument();
		Element docElement = doc.getDocumentElement();
	}
}
