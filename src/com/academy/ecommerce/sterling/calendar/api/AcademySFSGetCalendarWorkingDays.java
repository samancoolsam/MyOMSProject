package com.academy.ecommerce.sterling.calendar.api;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.w3c.dom.Document;

import com.academy.util.common.AcademyUtil;
import com.academy.util.constants.AcademyConstants;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

/**
 * Service to retrieve the Calendar working details for set of days mentioned for a 
 * particular Store
 * 
 * @author <a href="mailto:KaushikN.Sanji@cognizant.com">Kaushik N Sanji</a>
 *
 */
public class AcademySFSGetCalendarWorkingDays implements YIFCustomApi{
	public static final String CLASS_NAME = AcademySFSGetCalendarWorkingDays.class.getName();
	private static YFCLogCategory log = YFCLogCategory.instance(AcademySFSGetCalendarWorkingDays.class);

	public void setProperties(Properties properties) throws Exception {
	}

	/**
	 * Service method to retrieve the Calendar working details for set of days mentioned for a Store
	 * DayOffset attributes are Integers representing the Number of Days from the current Day to be added to
	 * the current Date.
	 * @param env
	 * @param inXML
	 * 		<br/> -  Input XML will be
	 * 		<pre>
	 * 		{@code
	 * 			<StoreCalendar OrganizationCode="Required" FromDayOffset="" ToDayOffset=""/>
	 * 		}
	 * 		</pre>
	 * @return Document
	 * 		<br/> - Output XML format will be
	 * 		<pre>
	 * 		{@code
	 * 			<OrgWorkingDays>
	 * 				<OrgWorkingDay Date="yyyy-MM-dd">
	 * 					<WorkingCalendar CalendarId="UPS" CalendarKey="" WorkingDay="N" /> 
	 * 	  				<WorkingCalendar CalendarId="UPSN" CalendarKey="" WorkingDay="Y" /> 
	 *				</OrgWorkingDay>
	 *				<OrgWorkingDay Date="yyyy-MM-dd">
	 * 					<WorkingCalendar CalendarId="UPS" CalendarKey="" WorkingDay="Y" /> 
	 * 	  				<WorkingCalendar CalendarId="UPSN" CalendarKey="" WorkingDay="N" /> 
	 *				</OrgWorkingDay>
	 *				.
	 *				.
	 *				.
	 *			</OrgWorkingDays>
	 * 		}
	 * 		</pre>
	 * @throws Exception
	 */
	public Document getWorkingDayDetails(YFSEnvironment env, Document inXML) throws Exception{
		log.debug(CLASS_NAME+" :getWorkingDayDetails:Entry");
		log.debug("\nInput XML to AcademySFSGetCalendarWorkingDays API:\n"+YFCDocument.getDocumentFor(inXML));
		
		Document orgWorkingDaysDoc = YFCDocument.createDocument(AcademyConstants.ORG_WORKING_DAYS_ELEMENT).getDocument();
		String organizationCode = inXML.getDocumentElement().getAttribute(AcademyConstants.ORG_CODE_ATTR);
		
		if(!YFCCommon.isVoid(organizationCode)){
			String fromDayStr = inXML.getDocumentElement().getAttribute(AcademyConstants.FROM_DAY_OFFSET_ATTR);
			String toDayStr = inXML.getDocumentElement().getAttribute(AcademyConstants.TO_DAY_OFFSET_ATTR);
			int fromDayOffset = 0;
			int toDayOffset = 0;
			if(!YFCCommon.isVoid(fromDayStr))
				fromDayOffset = Integer.parseInt(fromDayStr);
			if(!YFCCommon.isVoid(toDayStr))
				toDayOffset = Integer.parseInt(toDayStr);
			if(toDayOffset == 0 && toDayOffset!=fromDayOffset)
				toDayOffset = fromDayOffset;
			
			Document storeCalendarInputDoc = YFCDocument.createDocument(AcademyConstants.STORE_CALENDAR_ELEMENT).getDocument();
			storeCalendarInputDoc.getDocumentElement().setAttribute(AcademyConstants.ORG_CODE_ATTR, organizationCode);
			for(int index = fromDayOffset; index <= toDayOffset; index++){
				storeCalendarInputDoc.getDocumentElement().setAttribute(AcademyConstants.DAY_OFFSET_ATTR, String.valueOf(index));
				Document orgWorkingDayDoc = AcademyUtil.invokeService(env, AcademyConstants.CAL_WORKING_DAY_SERV, storeCalendarInputDoc);
				orgWorkingDayDoc.getDocumentElement().setAttribute(AcademyConstants.DATE_ATTR, getTodaysDate(index));
				orgWorkingDaysDoc.getDocumentElement().appendChild(orgWorkingDaysDoc.importNode(orgWorkingDayDoc.getDocumentElement(), true));
			}
		}
		
		log.debug("\nOutput XML of AcademySFSGetCalendarWorkingDays API:\n"+YFCDocument.getDocumentFor(orgWorkingDaysDoc));
		log.debug(CLASS_NAME+" :getWorkingDayDetails:Exit");
		return orgWorkingDaysDoc;
	}
	
	/**
	 * Method that generates and returns the current system date+offset(if >0) in the format "yyyy-MM-dd"
	 * @param offset
	 * 		<br/> - Number of Days to be added to the current day.<br/> '0' for current day. 
	 * @return String
	 * 		<br/> - current date in the format "yyyy-MM-dd"
	 */
	private String getTodaysDate(int offset) {
		log.debug(CLASS_NAME+" :getTodaysDate:Entry");
		SimpleDateFormat formatter =  new SimpleDateFormat(AcademyConstants.STR_SIMPLE_DATE_PATTERN);
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
