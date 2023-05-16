	
/*
 * Created on Jul 13,2012
 *
 */
package com.academy.som.printpickticket.screens;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.som.util.AcademyPCAConstants;
import com.academy.som.util.logging.AcademySIMTraceUtil;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCXmlUtils;

/**
 * Custom Behavior class done for managing the 
 * External Panel : "com.academy.sfs.printpickticket.screens.AcademyManifestingDayPanel"
 *
 * @author <a href="mailto:KaushikN.Sanji@cognizant.com">Kaushik N Sanji</a>
 * Copyright © 2005, 2006 Sterling Commerce, Inc. All Rights Reserved.
 */
 
public class AcademyManifestingDayPanelBehavior extends YRCBehavior {

	private static String CLASSNAME = "AcademyManifestingDayPanelBehavior";
	
	private AcademyManifestingDayPanel ownerForm; //External Panel composite object
	
	private boolean storeCalChanged = false; //flag to check whether any changes are done to update the Carriers Working Today
	
	/**
	 * Constructor for the behavior class. 
	 * @param ownerComposite
	 * 			<br/> - Composite of External Panel
	 * @param formId
	 * 			<br/> - FORM ID of External Panel : "com.academy.sfs.printpickticket.screens.AcademyManifestingDayPanel"
	 */
    public AcademyManifestingDayPanelBehavior(Composite ownerComposite, String formId) {
        super(ownerComposite, formId);
        final String methodName="AcademyManifestingDayPanelBehavior(ownerComposite, formId)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		ownerForm = (AcademyManifestingDayPanel)ownerComposite;
		
        init();
        callLocaleListForUserLocale();
        AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
    }
    
	/**
	 * This method initializes the behavior class.
	 */    
	public void init() {
		//TODO: write behavior init here
	}
    
	/**
	 * Method to call getLocaleList API to fetch the
	 * logged in User Locale Details
	 */
	private void callLocaleListForUserLocale() {
		final String methodName="callLocaleListForUserLocale()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		
		YRCApiContext context = new YRCApiContext();
		context.setFormId(getFormId());
		context.setApiName(AcademyPCAConstants.GET_USER_LOCALE_COMMAND);
		context.setInputXml(prepareInputForLocaleList());
		callApi(context);
		
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}
	
	/**
	 * Method to prepare the Input Document for getLocaleList API to fetch
	 * the logged in User Locale Details
	 * @return Document
	 * 			<br/> - Input to getLocaleList API
	 * 			<pre>
	 * 			{@code
	 * 				<Locale Localecode="<<Localecode of logged in User>>"/>
	 * 			}
	 * 			</pre>
	 */
	private Document prepareInputForLocaleList() {
		final String methodName="prepareInputForLocaleList()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		
		Document localeListInputDoc = YRCXmlUtils.createDocument(AcademyPCAConstants.LOCALE_ELEMENT);
		Element rootElement = localeListInputDoc.getDocumentElement();
		rootElement.setAttribute(AcademyPCAConstants.LOCALE_CODE_ATTR, 
						YRCPlatformUI.getUserElement().getAttribute(AcademyPCAConstants.LOCALE_CODE_ATTR));
		
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return localeListInputDoc;
	}

	/**
	 * Method to call the custom service for fetching the 
	 * Working Day details of all the Carriers in the Store 
	 * for a set of days 
	 */
	private void callGetOrgWorkingDaysService() {
		final String methodName="callGetOrgWorkingDaysService()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		
		YRCApiContext context = new YRCApiContext();
		context.setFormId(getFormId());
		context.setApiName(AcademyPCAConstants.GET_CAL_WORKING_DAYS_COMMAND);
		context.setInputXml(prepareInputForOrgWorkingDaysService());
		callApi(context);
		
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}

	/**
	 * Method to prepare the Input document for the custom service
	 * to fetch the Working Day details of all the Carriers in the Store
	 * for a set of days
	 * @return Document
	 * 			<br/> - to fetch the Working Day details of the Carriers
	 * 			<pre>
	 * 			{@code
	 * 				<StoreCalendar OrganizationCode="<<ShipNode of logged in User>>"
	 * 					FromDayOffset="<<START_DAY_OFFSET>>" ToDayOffset="<<END_DAY_OFFSET>>" />
	 * 			}
	 * 			</pre>
	 */
	private Document prepareInputForOrgWorkingDaysService() {
		final String methodName="prepareInputForOrgWorkingDaysService()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		
		Document storeCalendarInputDoc = YRCXmlUtils.createDocument(AcademyPCAConstants.STORE_CALENDAR_ELEMENT);
		Element rootElement = storeCalendarInputDoc.getDocumentElement();
		rootElement.setAttribute(AcademyPCAConstants.ORG_CODE_ATTR, 
				YRCPlatformUI.getUserElement().getAttribute(AcademyPCAConstants.SHIPNODE_ATTR));
		rootElement.setAttribute(AcademyPCAConstants.FROM_DAY_OFFSET_ATTR, YRCPlatformUI.getString(AcademyPCAConstants.START_DAY_OFFSET).trim());
		rootElement.setAttribute(AcademyPCAConstants.TO_DAY_OFFSET_ATTR, YRCPlatformUI.getString(AcademyPCAConstants.END_DAY_OFFSET).trim());
		
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return storeCalendarInputDoc;
	}

	/**
	 * Superclass method to set the screen models for the custom API Calls
	 * and to handle failure of API calls
	 * @param context
	 * 			<br/> - the context in which the API is called
	 */
	public void handleApiCompletion(YRCApiContext context) {
		final String methodName="handleApiCompletion(context)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		
		if(context.getInvokeAPIStatus()<0){
			//to handle API call failures
			AcademySIMTraceUtil.logMessage(context.getApiName()+" call Failed");
			AcademySIMTraceUtil.logMessage("Error Output: \n", context.getOutputXml().getDocumentElement());
		}else{
			//to handle successful API calls
			if(context.getApiName().equals(AcademyPCAConstants.GET_CAL_WORKING_DAYS_COMMAND)){
				//to handle the command "GetCalendarWorkingDays"
				AcademySIMTraceUtil.logMessage(methodName+" :: "+context.getApiName());
				Document orgWorkingDayDoc = context.getOutputXml();
				/**
				 * Output XML of command "GetCalendarWorkingDays"
				 * <pre>
				 * {@code
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
				 * }
				 * </pre>
				 */
				setModel(AcademyPCAConstants.STORE_WORKING_CAL_MODEL, orgWorkingDayDoc.getDocumentElement());
				createWorkingCalendarTable();
			}
			
			if(context.getApiName().equals(AcademyPCAConstants.MULTI_API_CAL_UPDATE_COMMAND)){
				//to handle the command "MultiApiCalendarUpdate"
				AcademySIMTraceUtil.logMessage(methodName+" :: "+context.getApiName());
				repopulateModel(AcademyPCAConstants.STORE_WORKING_CAL_MODEL);
				//setting the success message in status line
				YRCPlatformUI.setMessage(AcademyPCAConstants.MANIFEST_DAY_CHANGE_SUCCESS_MSG);
			}
			
			if(context.getApiName().equals(AcademyPCAConstants.GET_USER_LOCALE_COMMAND)){
				//to handle the command "GetUserLocale"
				AcademySIMTraceUtil.logMessage(methodName+" :: "+context.getApiName());
				Element localeElement = (Element) context.getOutputXml().getElementsByTagName(AcademyPCAConstants.LOCALE_ELEMENT).item(0);
				setModel(AcademyPCAConstants.USER_LOCALE_MODEL, localeElement);
				callGetOrgWorkingDaysService();
			}
		}
		
		super.handleApiCompletion(context);
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}

	/**
	 * Method to generate the tabulation data of Carriers Working Info in
	 * the Group "grpManifestingDay" dynamically
	 *
	 */
	private void createWorkingCalendarTable() {
		final String methodName="createWorkingCalendarTable()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		
		//calling the Composite's method to generate the tabulation data
		ownerForm.createWorkingCalendarTable(getModel(AcademyPCAConstants.STORE_WORKING_CAL_MODEL));
		ownerForm.getParent().getParent().layout(true, true); //to relayout the screen after fields are generated

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}

	/**
	 * Method to save the changes done to the Carriers Working Info
	 * @param orgWorkingDayChangedElement
	 * 			<br/> - Changed Target model of "GetCalendarWorkingDays" command output
	 */
	public void doSaveAction(Element orgWorkingDayChangedElement) {
		final String methodName="doSaveAction(orgWorkingDayChangedElement)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		
		//preparing the input to call a custom service for updating the changes
		Document multiApiInputDoc = prepareMultiApiInputForCalUpdate(orgWorkingDayChangedElement);
		if(storeCalChanged){ //if any Carriers Working today Info is changed
			callMultiApiForCalendarUpdate(multiApiInputDoc); //call the service for updation
			setModel(AcademyPCAConstants.STORE_WORKING_CAL_MODEL, YRCXmlUtils.getCopy(orgWorkingDayChangedElement));
			storeCalChanged = false; //reset the flag
		}
		
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}

	/**
	 * Method to reset the changes done to the Carriers Working Info
	 * Over a set of days
	 */
	public void doResetAction() {
		final String methodName="doResetAction()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		
		//calling the Composite's method to reset the tabulation data
		ownerForm.resetGrpManifestingDay(getModel(AcademyPCAConstants.STORE_WORKING_CAL_MODEL));
		
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}
	
	/**
	 * Method to call a custom multiApi of custom services to update
	 * the changes done to the Carriers working Info over a set of days
	 * @param multiApiInputDoc
	 * 			<br/> - Input Document to multiApi for updating the changes 
	 */
	private void callMultiApiForCalendarUpdate(Document multiApiInputDoc) {
		final String methodName="callMultiApiForCalendarUpdate(orgWorkingDayChangedElement)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		
		YRCApiContext context = new YRCApiContext();
		context.setFormId(getFormId());
		context.setApiName(AcademyPCAConstants.MULTI_API_CAL_UPDATE_COMMAND);
		context.setInputXml(multiApiInputDoc);
		callApi(context);
		
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}

	/**
	 * Method to prepare the Input document for custom service to update the 
	 * changes done to the Carriers working Info over a set of days
	 * @param orgWorkingDayChangedElement
	 * 			<br/> - Changed Target model of "GetCalendarWorkingDays" command output
	 * @return Document
	 * 			<br/> - Input Document for updating the changes
	 * 			<pre>
	 * 			{@code
	 * 				<MultiApi>
	 * 					<API FlowName="AcademySFSMakeWorkingDay" >
	 * 						<!-- when WorkingDay="Y" -->
	 * 						<Input>
	 * 							<StoreCalendar OrganizationCode="<<ShipNode of logged in User>>"
	 * 									   CalendarId="" CalendarKey="" Day=""/>
	 * 						</Input>
	 * 					</API>
	 * 					<API FlowName="AcademySFSMakeNonWorkingDay" >
	 * 						<!-- when WorkingDay="N" -->
	 * 						<Input>
	 * 							<StoreCalendar OrganizationCode="<<ShipNode of logged in User>>"
	 * 									   CalendarId="" CalendarKey="" Day=""/>
	 * 						</Input>
	 * 					</API>
	 * 				</MultiApi>
	 * 			}
	 * 			</pre>
	 */
	private Document prepareMultiApiInputForCalUpdate(Element orgWorkingDayChangedElement) {
		final String methodName="prepareMultiApiInputForCalUpdate(orgWorkingDayChangedElement)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		
		Document multiApiInputDoc = YRCXmlUtils.createDocument(AcademyPCAConstants.MULTI_API_ELEMENT);
		Element rootElement = multiApiInputDoc.getDocumentElement();
		
		Element orgWorkingDayElement = getModel(AcademyPCAConstants.STORE_WORKING_CAL_MODEL);
		//fetching the WorkingCalendar Element NodeList of original "GetCalendarWorkingDays" command output
		NodeList workingCalNL = orgWorkingDayElement.getElementsByTagName(AcademyPCAConstants.WORKING_CALENDAR_ELEMENT);
		//fetching the WorkingCalendar Element NodeList of modified "GetCalendarWorkingDays" command output
		NodeList chngdWorkingCalNL = orgWorkingDayChangedElement.getElementsByTagName(AcademyPCAConstants.WORKING_CALENDAR_ELEMENT);
		
		for(int listIndex = 0; listIndex<chngdWorkingCalNL.getLength(); listIndex++){
			Element workingCalElement = (Element)workingCalNL.item(listIndex);
			Element chngdWorkingCalElement = (Element)chngdWorkingCalNL.item(listIndex);
			
			//fetching the original WorkingDay flag attribute
			String workingDayFlag = workingCalElement.getAttribute(AcademyPCAConstants.WORKING_DAY_FLAG_ATTR);
			//fetching the modified WorkingDay flag attribute
			String chngdWorkingDayFlag = chngdWorkingCalElement.getAttribute(AcademyPCAConstants.WORKING_DAY_FLAG_ATTR);
			
			if(!(workingDayFlag.equals(chngdWorkingDayFlag))){ //compare to check if WorkingDay flag is modified
				storeCalChanged = true; //if modified, set the flag as true
				if(chngdWorkingDayFlag.equals(AcademyPCAConstants.STRING_Y)){ 
					//if WorkingDay="Y" make API element for "AcademySFSMakeWorkingDay" service
					Element apiElement = prepareAPIElementForMultiApi(multiApiInputDoc, chngdWorkingCalElement, AcademyPCAConstants.MAKE_WORKING_DAY_SERV);
					rootElement.appendChild(apiElement);
				}else if(chngdWorkingDayFlag.equals(AcademyPCAConstants.STRING_N)){ 
					//if WorkingDay="N" make API element for "AcademySFSMakeNonWorkingDay" service
					Element apiElement = prepareAPIElementForMultiApi(multiApiInputDoc, chngdWorkingCalElement, AcademyPCAConstants.MAKE_NON_WORKING_DAY_SERV);
					rootElement.appendChild(apiElement);
				}
			}
		}
		
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return multiApiInputDoc;
	}

	/**
	 * Method to prepare the API element for multiApi call to update the changes 
	 * made to Carriers Working Info over a set of days  
	 * @param multiApiInputDoc
	 * 			<br/> - Input document for multiApi API call
	 * @param chngdWorkingCalElement
	 * 			<br/> - modified WorkingCalendar Element
	 * @param flowName
	 * 			<br/> - Service name to be called
	 * @return Element
	 * 			<br/> - the API Element for multiApi call
	 * 			<pre>
	 * 			{@code
	 * 					<API FlowName="<<flowName>>" >
	 * 						<Input>
	 * 							<StoreCalendar OrganizationCode="<<ShipNode of logged in User>>"
	 * 									   CalendarId="" CalendarKey="" Day=""/>
	 * 						</Input>
	 * 					</API>
	 * 			}
	 * 			</pre>
	 */
	private Element prepareAPIElementForMultiApi(Document multiApiInputDoc, Element chngdWorkingCalElement, String flowName) {
		final String methodName="prepareAPIElementForMultiApi(multiApiInputDoc, chngdWorkingCalElement, flowName)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		
		Element apiElement = multiApiInputDoc.createElement(AcademyPCAConstants.API_ELEMENT);
		apiElement.setAttribute(AcademyPCAConstants.FLOW_NAME_ATTR, flowName);
		Element inputElement = multiApiInputDoc.createElement(AcademyPCAConstants.MULTI_API_INPUT_ELEMENT);
		
		Element storeCalElement = multiApiInputDoc.createElement(AcademyPCAConstants.STORE_CALENDAR_ELEMENT);
		storeCalElement.setAttribute(AcademyPCAConstants.ORG_CODE_ATTR, 
				YRCPlatformUI.getUserElement().getAttribute(AcademyPCAConstants.SHIPNODE_ATTR));
		storeCalElement.setAttribute(AcademyPCAConstants.CALENDAR_ID_ATTR, 
				chngdWorkingCalElement.getAttribute(AcademyPCAConstants.CALENDAR_ID_ATTR));
		storeCalElement.setAttribute(AcademyPCAConstants.CALENDAR_KEY_ATTR, 
				chngdWorkingCalElement.getAttribute(AcademyPCAConstants.CALENDAR_KEY_ATTR));
		
		Element chngdOrgWorkingDayElement = (Element)chngdWorkingCalElement.getParentNode();
		String dateStr = chngdOrgWorkingDayElement.getAttribute(AcademyPCAConstants.DATE_ATTR);
		storeCalElement.setAttribute(AcademyPCAConstants.DAY_OFFSET_ATTR, 
											String.valueOf(getDiffInDaysFromCurrentDay(dateStr)));
		
		inputElement.appendChild(storeCalElement);
		apiElement.appendChild(inputElement);
		
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return apiElement;
	}

	/**
	 * Method that calculates and returns the difference in days from the current day
	 * @param dateStr
	 * 			<br/> - Date String in format "yyyy-MM-dd" to be reformatted to User's Locale format
	 * @return int
	 * 			<br/> - Integer value of the difference in days
	 */
	public int getDiffInDaysFromCurrentDay(String dateStr){
		final String methodName="getDiffInDaysFromCurrentDay(dateStr)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		SimpleDateFormat formatter = new SimpleDateFormat(AcademyPCAConstants.STRING_SIMPLE_DATE_PATTERN);
		Date dateOffset = null;
		try {
			 dateOffset = formatter.parse(dateStr);
		} catch (ParseException e) {
			AcademySIMTraceUtil.logErrorMessage(CLASSNAME, methodName, e.getClass().getName(), e.getMessage());
		}
		Calendar offsetCal = Calendar.getInstance();
		offsetCal.setTime(dateOffset);
		Calendar todayCal = Calendar.getInstance();
		todayCal.setTime(new Date());
		
		int diffInDays = offsetCal.get(Calendar.DATE) - todayCal.get(Calendar.DATE); //gives the difference in days
		AcademySIMTraceUtil.logMessage("Difference in Days for "+dateStr+" from today is "+diffInDays);
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return diffInDays;
	}
	
	/**
	 * Method to change the Date format to the logged in User's Locale format
	 * @param dateStr
	 * 			<br/> - Date String in format "yyyy-MM-dd" to be reformatted to User's Locale format
	 * @return String
	 * 			<br/> - Date String in User's Locale format
	 */
	public String getUserLocaleDateFormat(String dateStr){
		final String methodName="getUserLocaleDateFormat(dateStr)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		SimpleDateFormat formatter = new SimpleDateFormat(AcademyPCAConstants.STRING_SIMPLE_DATE_PATTERN);
		Date dateParsed = null;
		try {
			 dateParsed = formatter.parse(dateStr);
		} catch (ParseException e) {
			AcademySIMTraceUtil.logErrorMessage(CLASSNAME, methodName, e.getClass().getName(), e.getMessage());
		}
		//fetching the User's Locale format
		formatter = new SimpleDateFormat(getModel(AcademyPCAConstants.USER_LOCALE_MODEL).getAttribute(AcademyPCAConstants.DATE_FORMAT_LOCALE_ATTR));
		String formattedLocaleDate = formatter.format(dateParsed); //fetching the formatted date
		AcademySIMTraceUtil.logMessage(dateStr+" in Locale format is "+formattedLocaleDate);
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return formattedLocaleDate;
	}
}
