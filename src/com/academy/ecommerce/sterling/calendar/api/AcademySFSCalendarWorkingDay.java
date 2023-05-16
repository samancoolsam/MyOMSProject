package com.academy.ecommerce.sterling.calendar.api;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * Service to retrieve the Calendar working details for the day configured for a 
 * particular Store
 * 
 * @author <a href="mailto:KaushikN.Sanji@cognizant.com">Kaushik N Sanji</a>
 *
 */
public class AcademySFSCalendarWorkingDay implements YIFCustomApi{
	public static final String CLASS_NAME = AcademySFSCalendarWorkingDay.class.getName();
	private static YFCLogCategory log = YFCLogCategory.instance(AcademySFSCalendarWorkingDay.class);
	
	public void setProperties(Properties properties) throws Exception {
	}
	
	/**
	 * Service method to retrieve the Calendar working details for the day configured for a Store
	 * @param env
	 * @param inXML
	 * 		<br/> -  Input XML will be
	 * 		<pre>
	 * 		{@code
	 * 			<StoreCalendar OrganizationCode="Required" Day=""/>
	 * 		}
	 * 		</pre>
	 * @return Document
	 * 		<br/> - Output XML format will be
	 * 		<pre>
	 * 		{@code
	 * 			<OrgWorkingDay>
	 * 				<WorkingCalendar CalendarId="UPS" CalendarKey="" WorkingDay="N" /> 
	 * 	  			<WorkingCalendar CalendarId="UPSN" CalendarKey="" WorkingDay="Y" /> 
	 *			</OrgWorkingDay>
	 * 		}
	 * 		</pre>
	 * @throws Exception
	 */
	public Document getWorkingDayDetails(YFSEnvironment env, Document inXML) throws Exception{
		log.debug(CLASS_NAME+" :getWorkingDayDetails:Entry");
		log.debug("\nInput XML to AcademySFSCalendarWorkingDay API:\n"+YFCDocument.getDocumentFor(inXML));
		
		Document orgWorkingDayDoc = YFCDocument.createDocument(AcademyConstants.ORG_WORKING_DAY_ELEMENT).getDocument();
		String organizationCode = inXML.getDocumentElement().getAttribute(AcademyConstants.ORG_CODE_ATTR);
		
		if(!YFCCommon.isVoid(organizationCode)){
			String reqDay = inXML.getDocumentElement().getAttribute(AcademyConstants.DAY_OFFSET_ATTR);
			
			env.setApiTemplate(AcademyConstants.GET_ORG_HIERARCHY_API, getOrgHierarchyOutputTemplate());
			Document orgHierarchyDoc = AcademyUtil.invokeAPI(env, AcademyConstants.GET_ORG_HIERARCHY_API, getOrgHierarchyInputDoc(organizationCode));
			env.clearApiTemplate(AcademyConstants.GET_ORG_HIERARCHY_API);
			log.debug("\nOutput XML of getOrganizationHierarchy API Call:\n"+(orgHierarchyDoc!=null?YFCDocument.getDocumentFor(orgHierarchyDoc):"NULL"));
			
			if(orgHierarchyDoc!=null){
				NodeList calendarNL = orgHierarchyDoc.getElementsByTagName(AcademyConstants.CALENDAR_ELEMENT);
				
				for(int listIndex = 0; listIndex<calendarNL.getLength(); listIndex++){
					Element calendarElement = (Element)calendarNL.item(listIndex);
					String calendarKey = calendarElement.getAttribute(AcademyConstants.CALENDAR_KEY_ATTR);
					
					Document calDayDtlsDoc = AcademyUtil.invokeAPI(env, AcademyConstants.GET_CAL_DAY_DTLS_API, getCalDayDtlsInputDoc(calendarKey, organizationCode, reqDay));
					log.debug("\nOutput XML of getCalendarDayDetails API Call:\n"+(calDayDtlsDoc!=null?YFCDocument.getDocumentFor(calDayDtlsDoc):"NULL"));
					
					if(calDayDtlsDoc!=null){
						Element dateElement = (Element)calDayDtlsDoc.getElementsByTagName(AcademyConstants.DATE_ELEMENT).item(0);
						String typeOfDay = dateElement.getAttribute(AcademyConstants.TYPE_OF_DAY_ATTR);
						
						Element workingCalElement = orgWorkingDayDoc.createElement(AcademyConstants.WORKING_CALENDAR_ELEMENT);
						workingCalElement.setAttribute(AcademyConstants.CALENDAR_ID_ATTR, 
								calDayDtlsDoc.getDocumentElement().getAttribute(AcademyConstants.CALENDAR_ID_ATTR));
						workingCalElement.setAttribute(AcademyConstants.CALENDAR_KEY_ATTR, 
								calDayDtlsDoc.getDocumentElement().getAttribute(AcademyConstants.CALENDAR_KEY_ATTR));
						
						String workingDayFlag = AcademyConstants.STR_NO;
						if(typeOfDay.equals(AcademyConstants.TYPE_WORKING_DAY) || typeOfDay.equals(AcademyConstants.TYPE_EDITED_WORKING_DAY)){
							workingDayFlag = AcademyConstants.STR_YES;
						}
						
						workingCalElement.setAttribute(AcademyConstants.WORKING_DAY_FLAG_ATTR, workingDayFlag);
						orgWorkingDayDoc.getDocumentElement().appendChild(workingCalElement);
					}
					
				}
			}
		}
		log.debug("\nOutput XML of AcademySFSCalendarWorkingDay API:\n"+YFCDocument.getDocumentFor(orgWorkingDayDoc));
		log.debug(CLASS_NAME+" :getWorkingDayDetails:Exit");
		return orgWorkingDayDoc;
	}

	/**
	 * Method that prepares the input Document for getCalendarDayDetails API Call
	 * @param calendarKey
	 * 			<br/> - Calendar Key value fetched from getOrganizationHierarchy API Call output
	 * @param organizationCode
	 * 			<br/> - Organizantion Code value fetched from service input XML
	 * @param reqDay
	 * 			<br/> - Number of Days to be offset from the current day 
	 * @return Document
	 * 			<br/> - for getCalendarDayDetails API Call
	 */
	private Document getCalDayDtlsInputDoc(String calendarKey, String organizationCode, String reqDay) {
		log.debug(CLASS_NAME+" :getCalendarDayDetails:Entry");
		Document calDayDtlsInputDoc = YFCDocument.createDocument(AcademyConstants.CALENDAR_ELEMENT).getDocument();
		Element rootElement = calDayDtlsInputDoc.getDocumentElement();
		
		String currentDateStr = null;
		if(!YFCCommon.isVoid(reqDay))
			currentDateStr = getTodaysDate(Integer.parseInt(reqDay));
		else
			currentDateStr = getTodaysDate(0);
		
		rootElement.setAttribute(AcademyConstants.TO_DATE_ATTR, currentDateStr);
		rootElement.setAttribute(AcademyConstants.FROM_DATE_ATTR, currentDateStr);
		rootElement.setAttribute(AcademyConstants.ORG_CODE_ATTR, organizationCode);
		rootElement.setAttribute(AcademyConstants.CALENDAR_KEY_ATTR, calendarKey);
		log.debug("\nInput XML to getCalendarDayDetails API Call:\n"+YFCDocument.getDocumentFor(calDayDtlsInputDoc));
		log.debug(CLASS_NAME+" :getCalendarDayDetails:Exit");
		return calDayDtlsInputDoc;
	}
	
	/**
	 * Method that generates and returns the current system date+offset(if >0) in the format "yyyyMMdd"
	 * @param offset
	 * 		<br/> - Number of Days to be added to the current day.<br/> '0' for current day. 
	 * @return String
	 * 		<br/> - current date in the format "yyyyMMdd"
	 */
	private String getTodaysDate(int offset) {
		log.debug(CLASS_NAME+" :getTodaysDate:Entry");
		SimpleDateFormat formatter =  new SimpleDateFormat(AcademyConstants.DATE_YYYYMMDD_FORMAT);
		formatter.setLenient(false);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		if(offset>0){
			calendar.add(Calendar.DATE, offset);
		}
		log.debug(CLASS_NAME+" :getTodaysDate:Exit");
		return formatter.format(calendar.getTime());
	}

	/**
	 * Method that prepares the input document for getOrganizationHierarchy API Call
	 * @param organizantionCode
	 * 			<br/> - Organizantion Code value fetched from service input XML
	 * @return Document
	 * 			<br/> - for getOrganizationHierarchy API Call
	 */
	private Document getOrgHierarchyInputDoc(String organizantionCode) {
		log.debug(CLASS_NAME+" :getOrgHierarchyInputDoc:Entry");
		Document orgHierarchyInputDoc = YFCDocument.createDocument(AcademyConstants.ORG_ELEMENT).getDocument();
		Element rootElement = orgHierarchyInputDoc.getDocumentElement();
		rootElement.setAttribute(AcademyConstants.ORG_CODE_ATTR, organizantionCode);
		log.debug("\nInput XML to getOrganizationHierarchy API Call:\n"+YFCDocument.getDocumentFor(orgHierarchyInputDoc));
		log.debug(CLASS_NAME+" :getOrgHierarchyInputDoc:Exit");
		return orgHierarchyInputDoc;
	}

	/**
	 * Method that prepares the template for the output of getOrganizationHierarchy API Call
	 * @return Document
	 * 				<br/> - template for getOrganizationHierarchy API output 
	 * @throws Exception
	 */
	private Document getOrgHierarchyOutputTemplate() throws Exception{
		log.debug(CLASS_NAME+" :getOrgHierarchyOutputTemplate:Entry");
		String templateStr = "<Organization OrganizationCode=\"\"> " +
								"<Calendars>" +
									"<Calendar CalendarDescription=\"\" CalendarId=\"\" CalendarKey=\"\"/>" +
								"</Calendars>" +
							 "</Organization>";
		
		Document templateDoc = YFCDocument.parse(templateStr).getDocument();
		log.debug(CLASS_NAME+" :getOrgHierarchyOutputTemplate:Exit");
		return templateDoc;
	}
}
