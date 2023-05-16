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
 * Service to make the current Day as Working for a particular Calendar Id of the Store
 * 
 * @author <a href="mailto:KaushikN.Sanji@cognizant.com">Kaushik N Sanji</a>
 *
 */
public class AcademySFSMakeWorkingDay implements YIFCustomApi{
	
	public static final String CLASS_NAME = AcademySFSMakeWorkingDay.class.getName();
	private static YFCLogCategory log = YFCLogCategory.instance(AcademySFSMakeWorkingDay.class);
	
	public void setProperties(Properties properties) throws Exception {
	}

	/**
	 * Service method to make the current Day as Working for a particular Calendar Id of the Store
	 * @param env
	 * @param inXML
	 * 			<br/> - Input XML will be
	 * 			<pre>
	 * 			{@code
	 * 				<StoreCalendar OrganizationCode="Required" CalendarKey="" CalendarId="Required" Day=""/>
	 * 			}
	 * 			</pre>
	 * @return Document
	 * 			<br/> - changeCalendar API Call output XML when a change needs to be updated
	 * 			<br/>	else same as input XML
	 * 			
	 * @throws Exception
	 */
	public Document makeCalendarWorkingDay(YFSEnvironment env, Document inXML) throws Exception{
		log.debug(CLASS_NAME+" :makeCalendarWorkingDay:Entry");
		log.debug("\nInput XML to AcademySFSMakeWorkingDay API:\n"+YFCDocument.getDocumentFor(inXML));
		
		String organizationCode = inXML.getDocumentElement().getAttribute(AcademyConstants.ORG_CODE_ATTR);
		String calendarKey = inXML.getDocumentElement().getAttribute(AcademyConstants.CALENDAR_KEY_ATTR);
		String calendarId = inXML.getDocumentElement().getAttribute(AcademyConstants.CALENDAR_ID_ATTR);
		Document changeCalDoc = null;
		
		if(!YFCCommon.isVoid(organizationCode)){
			String reqDay = inXML.getDocumentElement().getAttribute(AcademyConstants.DAY_OFFSET_ATTR);
			String currentDateStr = null;
			if(!YFCCommon.isVoid(reqDay))
				currentDateStr = getTodaysDate(Integer.parseInt(reqDay));
			else
				currentDateStr = getTodaysDate(0);
			
			Document calDayDtlsDoc = AcademyUtil.invokeAPI(env, AcademyConstants.GET_CAL_DAY_DTLS_API, getCalDayDtlsInputDoc(calendarKey, calendarId, organizationCode, currentDateStr));
			log.debug("\nOutput XML of getCalendarDayDetails API Call:\n"+(calDayDtlsDoc!=null?YFCDocument.getDocumentFor(calDayDtlsDoc):"NULL"));
			
			if(calDayDtlsDoc!=null){
				Element dateElement = (Element)calDayDtlsDoc.getElementsByTagName(AcademyConstants.DATE_ELEMENT).item(0);
				String typeOfDay = dateElement.getAttribute(AcademyConstants.TYPE_OF_DAY_ATTR);
				
				if(typeOfDay.equals(AcademyConstants.TYPE_OFF_DAY) || typeOfDay.equals(AcademyConstants.TYPE_EDITED_OFF_DAY)){
					changeCalDoc = AcademyUtil.invokeAPI(env, AcademyConstants.CHANGE_CALENDAR_API, getChangeCalendarInputDoc(calDayDtlsDoc, currentDateStr));
					log.debug("\nOutput XML of changeCalendar API Call:\n"+(changeCalDoc!=null?YFCDocument.getDocumentFor(changeCalDoc):"NULL"));
				}
			}
		}
		
		if(changeCalDoc!=null){
			log.debug("\nOutput XML of AcademySFSMakeWorkingDay API:\n"+YFCDocument.getDocumentFor(changeCalDoc));
			log.debug(CLASS_NAME+" :makeCalendarWorkingDay:Exit");
			return changeCalDoc;
		}
		
		log.debug("\nOutput XML of AcademySFSMakeWorkingDay API:\n"+YFCDocument.getDocumentFor(inXML));
		log.debug(CLASS_NAME+" :makeCalendarWorkingDay:Exit");
		return inXML;
	}

	/**
	 * Method that prepares the input Document for changeCalendar API
	 * to change/update the Working Day shifts
	 * @param calDayDtlsDoc
	 * 			<br/> - Output of getCalendarDayDetails API Call
	 * @param currentDateStr
	 * 			<br/> - current date in the format "yyyyMMdd" 
	 * @return Document
	 * 			<br/> - for changeCalendar API Call
	 * 			<pre>
	 * 			{@code
	 *				<Calendar CalendarKey="" OrganizationCode="">
	 *					<CalendarDayExceptions>
	 *						<CalendarDayException Date="<<currentDateStr>>" ExceptionType="1">
	 *							<ExceptionShifts>
	 *								<ExceptionShift ShiftKey="" />
	 *								.								
	 *								.
	 *								.
	 *							</ExceptionShifts>
	 *						</CalendarDayException>
	 *					</CalendarDayExceptions>
	 *				</Calendar> 			
	 * 			}
	 * 			</pre>
	 */
	private Document getChangeCalendarInputDoc(Document calDayDtlsDoc, String currentDateStr) {
		log.debug(CLASS_NAME+" :getChangeCalendarInputDoc:Entry");
		Document changeCalInputDoc = YFCDocument.createDocument(AcademyConstants.CALENDAR_ELEMENT).getDocument();
		Element rootElement = changeCalInputDoc.getDocumentElement();
		rootElement.setAttribute(AcademyConstants.ORG_CODE_ATTR, 
						calDayDtlsDoc.getDocumentElement().getAttribute(AcademyConstants.ORG_CODE_ATTR));
		rootElement.setAttribute(AcademyConstants.CALENDAR_KEY_ATTR, 
				calDayDtlsDoc.getDocumentElement().getAttribute(AcademyConstants.CALENDAR_KEY_ATTR));
		
		Element calDayExcepsElement = changeCalInputDoc.createElement(AcademyConstants.CAL_DAY_EXCEPTIONS_ELEMENT);
		Element calDayExcepElement = changeCalInputDoc.createElement(AcademyConstants.CAL_DAY_EXCEPTION_ELEMENT);
		calDayExcepElement.setAttribute(AcademyConstants.DATE_ATTR, currentDateStr);
		calDayExcepElement.setAttribute(AcademyConstants.EXC_TYPE_OF_DAY_ATTR, AcademyConstants.TYPE_WORKING_DAY);
		Element excepShiftsElement = changeCalInputDoc.createElement(AcademyConstants.EXCEPTION_SHIFTS_ELEMENT);
		
		NodeList shiftNL = calDayDtlsDoc.getElementsByTagName(AcademyConstants.SHIFT_ELEMENT);
		for(int listIndex = 0 ; listIndex<shiftNL.getLength(); listIndex++){
			Element shiftNLElement = (Element)shiftNL.item(listIndex);
			Element excepShiftElement = changeCalInputDoc.createElement(AcademyConstants.EXCEPTION_SHIFT_ELEMENT);
			excepShiftElement.setAttribute(AcademyConstants.SHIFT_KEY_ATTR, shiftNLElement.getAttribute(AcademyConstants.SHIFT_KEY_ATTR));
			excepShiftsElement.appendChild(excepShiftElement);
		}
		
		calDayExcepElement.appendChild(excepShiftsElement);
		calDayExcepsElement.appendChild(calDayExcepElement);
		rootElement.appendChild(calDayExcepsElement);
		log.debug("\nInput XML to getChangeCalendarInputDoc API Call:\n"+YFCDocument.getDocumentFor(changeCalInputDoc));
		log.debug(CLASS_NAME+" :getChangeCalendarInputDoc:Exit");
		return changeCalInputDoc;
	}

	/**
	 * Method that prepares the input Document for getCalendarDayDetails API Call
	 * @param calendarKey
	 * 			<br/> - Calendar Key value fetched from service input XML
	 * @param calendarId
	 *			<br/> - Calendar Id value fetched from service input XML 
	 * @param organizationCode
	 * 			<br/> - Organizantion Code value fetched from service input XML
	 * @param currentDateStr
	 * 			<br/> - current date in the format "yyyyMMdd" 
	 * @return Document
	 * 			<br/> - for getCalendarDayDetails API Call
	 * 			<pre>
	 * 			{@code
	 * 				<Calendar ToDate="<<currentDateStr>>" OrganizationCode="" FromDate="<<currentDateStr>>" 
	 * 					CalendarKey="" CalendarId=""/>
	 * 			}
	 * 			</pre>
	 */
	private Document getCalDayDtlsInputDoc(String calendarKey, String calendarId, String organizationCode, String currentDateStr) {
		log.debug(CLASS_NAME+" :getCalendarDayDetails:Entry");
		Document calDayDtlsInputDoc = YFCDocument.createDocument(AcademyConstants.CALENDAR_ELEMENT).getDocument();
		Element rootElement = calDayDtlsInputDoc.getDocumentElement();
		rootElement.setAttribute(AcademyConstants.TO_DATE_ATTR, currentDateStr);
		rootElement.setAttribute(AcademyConstants.FROM_DATE_ATTR, currentDateStr);
		rootElement.setAttribute(AcademyConstants.ORG_CODE_ATTR, organizationCode);
		if(!YFCCommon.isVoid(calendarKey))
			rootElement.setAttribute(AcademyConstants.CALENDAR_KEY_ATTR, calendarKey);
		rootElement.setAttribute(AcademyConstants.CALENDAR_ID_ATTR, calendarId);
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
}
