
package com.academy.som.closemanifest.wizards;

/**
 * Created on Aug 06,2012
 * Updated on Mar 24,2014
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.xml.xpath.XPathConstants;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.som.util.AcademyPCAConstants;
import com.academy.som.util.AcademyPCAErrorMessagesInterface;
import com.academy.som.util.logging.AcademySIMTraceUtil;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCBehavior;
import com.yantra.yfc.rcp.YRCConstants;
import com.yantra.yfc.rcp.YRCDesktopUI;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCFormatResponse;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCWizardExtensionBehavior;
import com.yantra.yfc.rcp.YRCXPathUtils;
import com.yantra.yfc.rcp.YRCXmlUtils;

/**
 * Custom wizard extension behavior class that overrides the functionality
 * of OOB Class: "com.yantra.pca.sop.rcp.tasks.outboundexecution.shipout.closemanifest.wizards.SOPCloseManifestWizard"
 * 
 * @author <a href="mailto:KaushikN.Sanji@cognizant.com">Kaushik N Sanji</a>
 * Copyright © 2006-2009 Sterling Commerce, Inc. All Rights Reserved.
 */
public class AcademyCloseManifestExtnWizardBehavior extends YRCWizardExtensionBehavior {

	private static String CLASSNAME = "AcademyCloseManifestExtnWizardBehavior";

	private String manifestNoSeltd = "";
	private String manifestKeySeltd = null;
	

	/**
	 * This method initializes the behavior class.
	 */
	public void init() {
	}

	public IYRCComposite createPage(String pageIdToBeShown) {
		//TODO
		return null;
	}

	public void pageBeingDisposed(String pageToBeDisposed) {
		//TODO
	}

	/**
	 * Called when a wizard page is about to be shown for the first time.
	 *
	 */
	public void initPage(String pageBeingShown) {
		//form ID need to change
		
		
		//com.academy.som.closemanifest.wizards.AcademyCloseManifestWizard
		 if (pageBeingShown.equals("com.academy.som.closemanifest.wizardpages.AcademyCloseManifest"))
			 
			{
				callAcadcloseManifestSimService();
			}
	}


	/**
	 * Method to call custom service "AcademySFSMakeNonWorkingDay"
	 * to make the carrier Non Working for Today
	 * @param storeCalendarInputDoc
	 * 			<br/> - Input Document to service to make the carrier Non Working for Today 
	 */
	private void callMakeNonWorkingService(Document storeCalendarInputDoc) {
		final String methodName="callMakeNonWorkingService(storeCalendarInputDoc)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		YRCApiContext context = new YRCApiContext();
		context.setFormId(getFormId());
		context.setApiName(AcademyPCAConstants.MAKE_NON_WORKING_COMMAND);
		context.setInputXml(storeCalendarInputDoc);
		callApi(context);

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}

	/**
	 * Method to prepare the Input document for custom service  
	 * "AcademySFSMakeNonWorkingDay" to make carrier Non Working for Today
	 * @param Scac
	 * 			<br/> - Carrier to be made Non Working in the store based
	 * 					on manifest that is to be closed
	 * @return Document
	 * 			<br/> - Input Document for updating the changes
	 * 			<pre>
	 * 			{@code
	 * 				<StoreCalendar OrganizationCode="<<ShipNode of logged in User>>"
	 * 									   CalendarId="<<Scac>>" />
	 * 			}
	 * 			</pre>
	 */
	private Document prepareInputForMakeNonWorkingDay(String Scac) {
		final String methodName="prepareInputForMakeNonWorkingDay(Scac)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		Document storeCalendarInputDoc = YRCXmlUtils.createDocument(AcademyPCAConstants.STORE_CALENDAR_ELEMENT);
		Element storeCalElement = storeCalendarInputDoc.getDocumentElement();
		storeCalElement.setAttribute(AcademyPCAConstants.ORG_CODE_ATTR, 
				YRCPlatformUI.getUserElement().getAttribute(AcademyPCAConstants.SHIPNODE_ATTR));
		storeCalElement.setAttribute(AcademyPCAConstants.CALENDAR_ID_ATTR, 
				Scac);

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return storeCalendarInputDoc;
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
	 * Superclass method to set the screen models for the custom API Calls
	 * and to handle failure of API calls
	 * @param context
	 * 			<br/> - the context in which the API is called
	 */
	public void handleApiCompletion(YRCApiContext context) {
		final String methodName="handleApiCompletion(context)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		String strApiName = context.getApiName();
		if(context.getInvokeAPIStatus()<0){
			//to handle API call failures
			AcademySIMTraceUtil.logMessage(context.getApiName()+" call Failed");
			AcademySIMTraceUtil.logMessage("Error Output: \n", context.getOutputXml().getDocumentElement());
		}else{
			//to handle successful API calls

			if(strApiName.equals(AcademyPCAConstants.GET_USER_LOCALE_COMMAND)){
				//to handle the command "GetUserLocale"
				AcademySIMTraceUtil.logMessage(methodName+" :: "+context.getApiName());
				Element localeElement = (Element) context.getOutputXml().getElementsByTagName(AcademyPCAConstants.LOCALE_ELEMENT).item(0);
				setExtentionModel(AcademyPCAConstants.EXTN_USER_LOCALE_MODEL, localeElement);
				showManifestDateInUserLocale();
			}

			/*
			if(strApiName.equals(AcademyPCAConstants.MAKE_NON_WORKING_COMMAND)){
				//to handle the command "MakeNonWorking"
				AcademySIMTraceUtil.logMessage(methodName+" :: "+context.getApiName());
				AcademySIMTraceUtil.logMessage("Calling command : "+AcademyPCAConstants.MULTI_API_CHNG_SHIPMENT_PACK_COMMAND);
				callChangeShipmentMultiApiForPack(manifestKeySeltd);
			}
			 */
			/*
			if(strApiName.equals(AcademyPCAConstants.MULTI_API_CHNG_SHIPMENT_PACK_COMMAND)){
				//to handle the command "MultiApiChangeShipmentPack"
				AcademySIMTraceUtil.logMessage(methodName+" :: "+context.getApiName());
				AcademySIMTraceUtil.logMessage("Calling command : "+AcademyPCAConstants.ACAD_CLOSE_MANIFEST_COMMAND);
				callCloseManifestServ();
			}*/

			if(strApiName.equals(AcademyPCAConstants.ACAD_CLOSE_MANIFEST_COMMAND)){
				//to handle the command "AcademySFSCloseManifest"
				AcademySIMTraceUtil.logMessage(methodName+" :: "+context.getApiName());
				this.showNextPage();
				YRCPlatformUI.showInformation("Manifest "+manifestNoSeltd, YRCPlatformUI.getString(AcademyPCAConstants.CLOSE_MANIFEST_SUCCESS_MSG));
				//Check the Success msg
				YRCPlatformUI.setMessage(AcademyPCAConstants.EMPTY_STRING);
			}
			 
			
			if (context.getApiName().equals(AcademyPCAConstants.GET_TASK_Q_DATA_LIST)) {

				Document outdoc = context.getOutputXml();
				setExtentionModel(AcademyPCAConstants.EXTN_ACAD_CLOSE_MANIFEST_SIM_MODEL, outdoc.getDocumentElement());
			}
		}
	}

	/**
	 * Superclass method called to modify the OOB screen models when set
	 * @param nameSpace
	 * 			<br/> - name of the OOB screen model
	 */
	public void postSetModel(String nameSpace) {
		final String methodName="postSetModel(nameSpace)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		if(nameSpace.equals(AcademyPCAConstants.OPEN_MANIFEST_LIST_OOB_MODEL)){
			AcademySIMTraceUtil.logMessage(methodName + " :: NameSpace " + nameSpace + " :: BEGIN");
			//when OOB OpenManifestList screen model is set

			callLocaleListForUserLocale();

			AcademySIMTraceUtil.logMessage(methodName + " :: NameSpace " + nameSpace + " :: END");
		}

		super.postSetModel(nameSpace);
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}

	/**
	 * Method that appends the Manifest No attribute value with Manifest Date
	 * in User Locale Date format for display.
	 *
	 */
	private void showManifestDateInUserLocale() {
		final String methodName="showManifestDateInUserLocale()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		YRCBehavior bhvr = (YRCBehavior)YRCDesktopUI.getCurrentPage().getData("YRCBehavior");
		Composite pnlManifestListOOB = (Composite) bhvr.getControl(AcademyPCAConstants.OOB_PNL_MANIFEST_LIST);
		callAcadcloseManifestSimService();
		ArrayList<Button> rdbBtnList = new ArrayList<Button>(); //arraylist to fetch all the radio buttons in "pnlManifestList"
		getRadioBtnCtrls(pnlManifestListOOB.getChildren(), rdbBtnList);

		Element openManifestModelElement = getModel(AcademyPCAConstants.OPEN_MANIFEST_LIST_OOB_MODEL); 
		
		// Retrieve the manifest list thats been stored in the YFS_TASK_Q table -
		Element getTaskQueueDataListElement = getExtentionModel(AcademyPCAConstants.EXTN_ACAD_CLOSE_MANIFEST_SIM_MODEL);
		

		// Form an array list with the list of manifest keys.
		ArrayList<String> manifestKeyList = new ArrayList<String>();
		if(null != getTaskQueueDataListElement){
			NodeList getTaskQueueDataListElementList = getTaskQueueDataListElement.getElementsByTagName(AcademyPCAConstants.ELE_TASK_QUEUE);
			if (null != getTaskQueueDataListElementList) {
				int acadCloseManifestSimElemLength = getTaskQueueDataListElementList.getLength();
				for (int counter = 0; counter < acadCloseManifestSimElemLength; counter++) {
					Element TaskQueueElement = (Element) getTaskQueueDataListElementList.item(counter);
					String DataKey = TaskQueueElement.getAttribute(AcademyPCAConstants.ATTR_DATA_KEY);
					if (DataKey != null) {
						manifestKeyList.add(DataKey);
					}
				}
			}
		}
		
		boolean bFlag = true;

		for(Button btnCtrl: rdbBtnList){
			String manifestKey = (String)btnCtrl.getData(YRCConstants.YRC_CONTROL_NAME);
			String manifestOptText = btnCtrl.getText();

			if(bFlag){
				btnCtrl.setSelection(true);
				bFlag=false;
			}

			String manifestDateXpath = (new StringBuilder())
			.append(AcademyPCAConstants.MANIFEST_LIST_MANIFEST_ELE_XPATH)
			.append("[@").append(AcademyPCAConstants.MANIFEST_KEY_ATTR)
			.append("='").append(manifestKey).append("']/@")
			.append(AcademyPCAConstants.MANIFEST_DATE_ATTR).toString();

			String manifestDateTime = (String) YRCXPathUtils.evaluate(openManifestModelElement,
					manifestDateXpath, XPathConstants.STRING);

			String manifestDate = (manifestDateTime.split(AcademyPCAConstants.STRING_T))[0];

			if (!YRCPlatformUI.isVoid(manifestDate)) {
				if (manifestKeyList != null && manifestKeyList.contains(manifestKey)) {
					btnCtrl.setEnabled(false);
					btnCtrl.setText(manifestOptText.replace("with", "for " + getUserLocaleDateFormat(manifestDate) + " with") + AcademyPCAConstants.STATUS_CLOSURE_IN_PROGRESS);
				} else {
					btnCtrl.setText(manifestOptText.replace("with", "for " + getUserLocaleDateFormat(manifestDate) + " with") + AcademyPCAConstants.STATUS_OPEN);
				}
			}
		}
		//this.relayoutScreen();

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}
	
	
	private void callAcadcloseManifestSimService() {
		YRCApiContext context = new YRCApiContext();
		context.setFormId(getFormId());
		context.setApiName(AcademyPCAConstants.GET_TASK_Q_DATA_LIST);
		context.setInputXml(prepareInputForgetTaskQueueDataList());
		callApi(context);
		
	}
	// Preparing the input document for AcadCloseManifestSim
	private Document prepareInputForgetTaskQueueDataList() {

		final String methodName = "prepareInputForgetTaskQueueDataList()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		Document getTaskQueueDataListInputDoc = YRCXmlUtils.createDocument(AcademyPCAConstants.ELE_GET_TASK_Q_DATA_INPUT);
		Element eleGetTaskQueueDataInput = getTaskQueueDataListInputDoc.getDocumentElement();
		eleGetTaskQueueDataInput.setAttribute(AcademyPCAConstants.TRANSACTION_ID, AcademyPCAConstants.TRANS_ID_CLOSE_MANIFEST);
		System.out.println("GetTaskQueueDataInput"+ YRCXmlUtils.getString(getTaskQueueDataListInputDoc));
		return getTaskQueueDataListInputDoc;
	}
	

	/**
	 * Method to fetch all the radio buttons in the OOB panel "pnlManifestList"
	 * into the Arraylist "rdbBtnList"
	 * @param childCtrls
	 * 			<br/> - Child controls of composite
	 * @param rdbBtnList
	 * 			<br/> - Arraylist for storing the Radio buttons
	 */
	private void getRadioBtnCtrls(Control[] childCtrls, ArrayList<Button> rdbBtnList) {
		final String methodName="getRadioBtnCtrls(childCtrls, rdbBtnList)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		for(Control fieldCtrl: childCtrls){
			String ctrlName = (String) fieldCtrl.getData(YRCConstants.YRC_CONTROL_NAME);
			if(!YRCPlatformUI.isVoid(ctrlName)){
				if(fieldCtrl instanceof Composite){
					getRadioBtnCtrls(((Composite) fieldCtrl).getChildren(), rdbBtnList);
				}
				if(fieldCtrl instanceof Button){
					rdbBtnList.add((Button)fieldCtrl);
				}
			}
		}

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}

	/**
	 * Method to change the Date format to the logged in User's Locale format
	 * @param dateStr
	 * 			<br/> - Date String in format "yyyy-MM-dd" to be reformatted to User's Locale format
	 * @return String
	 * 			<br/> - Date String in User's Locale format
	 */
	private String getUserLocaleDateFormat(String dateStr){
		final String methodName="getUserLocaleDateFormat(dateStr)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		SimpleDateFormat formatter = new SimpleDateFormat(AcademyPCAConstants.STRING_SIMPLE_DATE_PATTERN);
		Date dateParsed = null;
		try {
			dateParsed = formatter.parse(dateStr);
		} catch (ParseException e) {
			AcademySIMTraceUtil.logErrorMessage(CLASSNAME, methodName, e.getClass().getName(), e.getMessage());
		}
		Element localeElement = getExtentionModel(AcademyPCAConstants.EXTN_USER_LOCALE_MODEL);
		//fetching the User's Locale format
		formatter = new SimpleDateFormat(localeElement.getAttribute(AcademyPCAConstants.DATE_FORMAT_LOCALE_ATTR));
		String formattedLocaleDate = formatter.format(dateParsed); //fetching the formatted date 
		AcademySIMTraceUtil.logMessage(dateStr+" in Locale format is "+formattedLocaleDate);
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return formattedLocaleDate;
	}

	/**
	 * Method to check whether the manifest Date is a future date
	 * with respect to system's date.
	 * @param dateStr
	 * 			<br/> - ManifestDate attribute value in the format "yyyy-MM-dd"
	 * @return boolean
	 * 			<br/> - returns <b>True</b> if Manifest date is future date
	 * 			<br/> - returns <b>False</b> if Manifest date is not a future date
	 */
	private boolean isFutureManifestDate(String dateStr){
		final String methodName="isFutureManifestDate(dateStr)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		boolean isFutureDay = false;

		SimpleDateFormat formatter = new SimpleDateFormat(AcademyPCAConstants.STRING_SIMPLE_DATE_PATTERN);
		Date manifestDate = null;
		try {
			manifestDate = formatter.parse(dateStr);
		} catch (ParseException e) {
			AcademySIMTraceUtil.logErrorMessage(CLASSNAME, methodName, e.getClass().getName(), e.getMessage());
		}
		isFutureDay = manifestDate.after(new Date()); 
		AcademySIMTraceUtil.logMessage(dateStr+" is Future Date? : "+isFutureDay);
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return isFutureDay;
	}

	/**
	 * Method to check whether the manifest Date is a past date
	 * with respect to system's date.
	 * @param dateStr
	 * 			<br/> - ManifestDate attribute value in the format "yyyy-MM-dd"
	 * @return boolean
	 * 			<br/> - returns <b>True</b> if Manifest date is past date
	 * 			<br/> - returns <b>False</b> if Manifest date is not a past date
	 */
	private boolean isPastManifestDate(String dateStr){
		final String methodName="isPastManifestDate(dateStr)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		boolean isPastDay = false;

		SimpleDateFormat formatter = new SimpleDateFormat(AcademyPCAConstants.STRING_SIMPLE_DATE_PATTERN);
		Date manifestDate = null;
		Date now = null;
		try {
			manifestDate = formatter.parse(dateStr);
			now = formatter.parse(formatter.format(new Date()));
		} catch (ParseException e) {
			AcademySIMTraceUtil.logErrorMessage(CLASSNAME, methodName, e.getClass().getName(), e.getMessage());
		}
		Calendar rightNow = Calendar.getInstance();
		rightNow.setTime(now);
		isPastDay = manifestDate.before(rightNow.getTime());
		AcademySIMTraceUtil.logMessage(dateStr+" is Past Date? : "+isPastDay);
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return isPastDay;
	}

	/**
	 * Method called when a Button is clicked
	 * @param fieldname
	 * 			<br/> - name of the field to be validated
	 * @return YRCValidationResponse
	 * 			<br/> - The response object of the superclass that holds the status
	 * 					of validation and related message
	 */
	public YRCValidationResponse validateButtonClick(String fieldName) {
		final String methodName="validateButtonClick(fieldName)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		YRCValidationResponse response = super.validateButtonClick(fieldName);

		/*
		if(fieldName.equals(AcademyPCAConstants.OOB_BTN_CLOSE_MANIFEST)){
			//for button "btnClose"
			AcademySIMTraceUtil.logMessage("fieldName: "+fieldName);

			manifestKeySeltd = getTargetModel(AcademyPCAConstants.SELTD_MANIFEST_OOB_MODEL)
										.getAttribute(AcademyPCAConstants.SELTD_MANIFEST_ATTR);

			if(!YRCPlatformUI.isVoid(manifestKeySeltd)){
				Element openManifestModelElement = getModel(AcademyPCAConstants.OPEN_MANIFEST_LIST_OOB_MODEL); 

				String manifestNoXpath = (new StringBuilder())
												.append(AcademyPCAConstants.MANIFEST_LIST_MANIFEST_ELE_XPATH)
												.append("[@").append(AcademyPCAConstants.MANIFEST_KEY_ATTR)
												.append("='").append(manifestKeySeltd).append("']/@")
												.append(AcademyPCAConstants.MANIFEST_NO_ATTR).toString();

				String manifestDateXpath = (new StringBuilder())
												.append(AcademyPCAConstants.MANIFEST_LIST_MANIFEST_ELE_XPATH)
												.append("[@").append(AcademyPCAConstants.MANIFEST_KEY_ATTR)
												.append("='").append(manifestKeySeltd).append("']/@")
												.append(AcademyPCAConstants.MANIFEST_DATE_ATTR).toString();

				manifestNoSeltd = (String) YRCXPathUtils.evaluate(openManifestModelElement,
																manifestNoXpath, XPathConstants.STRING);

				String manifestDateTime = (String) YRCXPathUtils.evaluate(openManifestModelElement,
																manifestDateXpath, XPathConstants.STRING);
				String manifestDate = (manifestDateTime.split(AcademyPCAConstants.STRING_T))[0];

				if(isEligibleForClose(manifestKeySeltd)){//for manifests containing all shipments in status >= '1100.70.06.30'
					if(!isPastManifestDate(manifestDate)){
						if(!isFutureManifestDate(manifestDate)){//for Current Day Manifests
							String manifestScacXpath = (new StringBuilder())
														.append(AcademyPCAConstants.MANIFEST_LIST_MANIFEST_ELE_XPATH)
														.append("[@").append(AcademyPCAConstants.MANIFEST_KEY_ATTR)
														.append("='").append(manifestKeySeltd).append("']/@")
														.append(AcademyPCAConstants.SCAC_ATTR).toString();
							String manifestScac = (String) YRCXPathUtils.evaluate(openManifestModelElement,
														manifestScacXpath, XPathConstants.STRING);
							AcademySIMTraceUtil.logMessage("Calling command : "+AcademyPCAConstants.MAKE_NON_WORKING_COMMAND);
							callMakeNonWorkingService(prepareInputForMakeNonWorkingDay(manifestScac));
						}else{
							//for Future Day Manifests
							String errorMessage = YRCPlatformUI.getString(AcademyPCAErrorMessagesInterface.FUTURE_MANIFEST_ERR_MSG_KEY);
							response = showError(errorMessage);
							AcademySIMTraceUtil.logMessage("Error Message : '"+errorMessage+"' shown for Manifest Date "+manifestDate);
						}
					}else{
						//for Past Day Manifests
						AcademySIMTraceUtil.logMessage("Manifest being closed for a past date");
						callChangeShipmentMultiApiForPack(manifestKeySeltd);
					}
				}else{
					//for manifests containing any shipment in status < '1100.70.06.30'
					String errorMessage = YRCPlatformUI.getString(AcademyPCAErrorMessagesInterface.CLOSE_MANIFEST_BKRM_ERR_MSG_KEY);
					response = showError(errorMessage);
					AcademySIMTraceUtil.logMessage("Error Message : '"+errorMessage+"' shown for Manifest No "+manifestNoSeltd);
				}
			}
		}
		 */

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return response;
	}

	/**
	 * Method to check whether any of the shipments in the manifest
	 * selected is in status >= 1100.70.06.30 (Ready To Ship) for closing the manifest.
	 * @param manifestKey
	 * 			<br/> - ManifestKey attribute value of the manifest selected for close
	 * @return boolean
	 * 			<br/> - returns <b>True</b> if all Manifest shipments are in status >= 1100.70.06.30 (Ready To Ship)
	 * 			<br/> - returns <b>False</b> if any Manifest shipments are in status = 1100.70.06.10 (Ready For Backroom Pick)
	 */
	private boolean isEligibleForClose(String manifestKey) {
		final String methodName="isEligibleForClose(manifestKey)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		boolean isEligible = true;
		Element openManifestModelElement = getModel(AcademyPCAConstants.OPEN_MANIFEST_LIST_OOB_MODEL);
		String shipmentsListXpath = (new StringBuilder())
		.append(AcademyPCAConstants.MANIFEST_LIST_MANIFEST_ELE_XPATH)
		.append("[@").append(AcademyPCAConstants.MANIFEST_KEY_ATTR)
		.append("='").append(manifestKey).append("']/")
		.append(AcademyPCAConstants.SHIPMENTS_ELEMENT).append("/")
		.append(AcademyPCAConstants.SHIPMENT_ELEMENT).toString();

		NodeList shipmentNL = (NodeList)YRCXPathUtils.evaluate(openManifestModelElement, shipmentsListXpath, XPathConstants.NODESET);

		for(int listIndex = 0; listIndex < shipmentNL.getLength(); listIndex++){
			Element shipmentElement = (Element)shipmentNL.item(listIndex);
			String status = shipmentElement.getAttribute(AcademyPCAConstants.STATUS_ATTR);

			if(status.trim().equals(AcademyPCAConstants.STATUS_READY_FOR_BACKROOM_PICK_VAL)){
				isEligible = false;
				break;
			}
		}

		AcademySIMTraceUtil.logMessage(methodName+" : "+isEligible+" returned");
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return isEligible;
	}

	/**
	 * Method to call the custom service for Closing the Manifest
	 * selected
	 */
	/*
	public void callCloseManifestServ() {
		final String methodName="callCloseManifestServ()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		manifestKeySeltd = getModel(AcademyPCAConstants.SELTD_MANIFEST_OOB_MODEL).getAttribute(AcademyPCAConstants.SELTD_MANIFEST_ATTR);

		if(!YRCPlatformUI.isVoid(manifestKeySeltd)){
			Element openManifestModelElement = getModel(AcademyPCAConstants.OPEN_MANIFEST_LIST_OOB_MODEL); 

			String manifestNoXpath = (new StringBuilder())
				.append(AcademyPCAConstants.MANIFEST_LIST_MANIFEST_ELE_XPATH)
				.append("[@").append(AcademyPCAConstants.MANIFEST_KEY_ATTR)
				.append("='").append(manifestKeySeltd).append("']/@")
				.append(AcademyPCAConstants.MANIFEST_NO_ATTR).toString();

			String manifestDateXpath = (new StringBuilder())
				.append(AcademyPCAConstants.MANIFEST_LIST_MANIFEST_ELE_XPATH)
				.append("[@").append(AcademyPCAConstants.MANIFEST_KEY_ATTR)
				.append("='").append(manifestKeySeltd).append("']/@")
				.append(AcademyPCAConstants.MANIFEST_DATE_ATTR).toString();

			manifestNoSeltd = (String) YRCXPathUtils.evaluate(openManifestModelElement,
								manifestNoXpath, XPathConstants.STRING);

			String manifestDateTime = (String) YRCXPathUtils.evaluate(openManifestModelElement,
								manifestDateXpath, XPathConstants.STRING);

			String manifestDate = (manifestDateTime.split(AcademyPCAConstants.STRING_T))[0];

			if(isEligibleForClose(manifestKeySeltd)){//for manifests containing all shipments in status >= '1100.70.06.30'
				if(!isPastManifestDate(manifestDate)){
					if(!isFutureManifestDate(manifestDate)){//for Current Day Manifests
						String manifestScacXpath = (new StringBuilder())
						.append(AcademyPCAConstants.MANIFEST_LIST_MANIFEST_ELE_XPATH)
						.append("[@").append(AcademyPCAConstants.MANIFEST_KEY_ATTR)
						.append("='").append(manifestKeySeltd).append("']/@")
						.append(AcademyPCAConstants.SCAC_ATTR).toString();

						String manifestScac = (String) YRCXPathUtils.evaluate(openManifestModelElement,
						manifestScacXpath, XPathConstants.STRING);

						AcademySIMTraceUtil.logMessage("Calling command : "+AcademyPCAConstants.MAKE_NON_WORKING_COMMAND);
						callMakeNonWorkingService(prepareInputForMakeNonWorkingDay(manifestScac));
					}else{
						//for Future Day Manifests
						String errorMessage = YRCPlatformUI.getString(AcademyPCAErrorMessagesInterface.FUTURE_MANIFEST_ERR_MSG_KEY);
						showError(errorMessage);
						AcademySIMTraceUtil.logMessage("Error Message : '"+errorMessage+"' shown for Manifest Date "+manifestDate);
						return;
					}
				}else{
					//for Past Day Manifests
					AcademySIMTraceUtil.logMessage("Manifest being closed for a past date");
					callChangeShipmentMultiApiForPack(manifestKeySeltd);
				}
			}else{

				//for manifests containing any shipment in status < '1100.70.06.30'
				String errorMessage = YRCPlatformUI.getString(AcademyPCAErrorMessagesInterface.CLOSE_MANIFEST_BKRM_ERR_MSG_KEY);
				showError(errorMessage);
				AcademySIMTraceUtil.logMessage("Error Message : '"+errorMessage+"' shown for Manifest No "+manifestNoSeltd);
				return;
			}
		}


		YRCApiContext context = new YRCApiContext();
		context.setFormId(getFormId());
		context.setApiName(AcademyPCAConstants.ACAD_CLOSE_MANIFEST_COMMAND);
		context.setInputXml(prepareInputForCloseManifest());
		callApi(context);

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}
	 */
	/**
	 * Method to prepare the Input document for custom service
	 * "AcademySFSCloseManifest" to close the manifest selected
	 * @return Document
	 * 			<br/> - Input document for service "AcademySFSCloseManifest"
	 * 			<pre>
	 * 			{@code
	 * 				<Manifest ManifestKey="" />
	 * 			}
	 * 			</pre>
	 */
	/*
	private Document prepareInputForCloseManifest() {
		final String methodName="prepareInputForCloseManifest()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		Document closeManifestInputDoc = YRCXmlUtils.createDocument(AcademyPCAConstants.MANIFEST_ELEMENT);
		Element rootElement = closeManifestInputDoc.getDocumentElement();
		rootElement.setAttribute(AcademyPCAConstants.MANIFEST_KEY_ATTR, manifestKeySeltd);

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return closeManifestInputDoc;
	}
	 */

	/**
	 * Method to show the error messages.
	 *
	 * @param errorMessage
	 * 			<br/> - The error message to be shown on the screen.
	 *
	 * @return YRCValidationResponse
	 * 			<br/> -  The object has the required error message in it
	 */
	private YRCValidationResponse showError(final String errorMessage) {
		final YRCFormatResponse response = new YRCFormatResponse(
				YRCValidationResponse.YRC_VALIDATION_ERROR, errorMessage, null);
		YRCPlatformUI.showError(AcademyPCAConstants.STRING_ERROR, errorMessage);
		return response;
	}

	/**
	 * Method to call multiApi of changeShipment APIs for updating the status of
	 * shipments from "Ready To Ship" to "Shipment Packed" in order to make them
	 * eligible for closeManifest API call.
	 * @param manifestKey
	 * 			<br/> - ManifestKey attribute value of the manifest selected for close
	 */
	/*
	private void callChangeShipmentMultiApiForPack(String manifestKey) {
		final String methodName="callChangeShipmentMultiApiForPack(manifestKey)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		YRCApiContext context = new YRCApiContext();
		context.setFormId(getFormId());
		context.setApiName(AcademyPCAConstants.MULTI_API_CHNG_SHIPMENT_PACK_COMMAND);
		context.setInputXml(prepareMultiApiInputForChangeShipment(manifestKey));
		callApi(context);

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}
	 */
	/**
	 * Method that prepares and returns the multiApi Input document
	 * for "MultiApiChangeShipmentPack" command
	 * @param manifestKey
	 * 			<br/> - ManifestKey attribute value of the manifest selected for close
	 * @return Document
	 * 			<br/> - Input document for "MultiApiChangeShipmentPack" command
	 * 			<pre>
	 * 			{@code
	 * 				<MultiApi>
	 * 					<API Name="changeShipment">
	 * 						<Input>
	 * 							<Shipment ShipmentKey="" ShipmentPackComplete="Y" />
	 * 						</Input>
	 * 					</API>
	 * 					.	
	 * 					.
	 * 				</MultiApi>
	 * 			}
	 * 			</pre>
	 */
	/*
	private Document prepareMultiApiInputForChangeShipment(String manifestKey){
		final String methodName="prepareMultiApiInputForChangeShipment(manifestKey)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		Document multiApiInputDoc = YRCXmlUtils.createDocument(AcademyPCAConstants.MULTI_API_ELEMENT);
		Element rootElement = multiApiInputDoc.getDocumentElement();

		Element openManifestModelElement = getModel(AcademyPCAConstants.OPEN_MANIFEST_LIST_OOB_MODEL);
		String shipmentsListXpath = (new StringBuilder())
											.append(AcademyPCAConstants.MANIFEST_LIST_MANIFEST_ELE_XPATH)
											.append("[@").append(AcademyPCAConstants.MANIFEST_KEY_ATTR)
											.append("='").append(manifestKey).append("']/")
											.append(AcademyPCAConstants.SHIPMENTS_ELEMENT).append("/")
											.append(AcademyPCAConstants.SHIPMENT_ELEMENT).toString();

		NodeList shipmentNL = (NodeList)YRCXPathUtils.evaluate(openManifestModelElement, shipmentsListXpath, XPathConstants.NODESET);

		for(int listIndex = 0; listIndex < shipmentNL.getLength(); listIndex++){
			Element shipmentElement = (Element)shipmentNL.item(listIndex);

			if(shipmentElement.getAttribute(AcademyPCAConstants.STATUS_ATTR).trim()
					.equals(AcademyPCAConstants.STATUS_READY_FOR_CUSTOMER_VAL)){

				Element apiElement = prepareAPIElementForChangeShipment(multiApiInputDoc, 
											shipmentElement.getAttribute(AcademyPCAConstants.SHIPMENT_KEY_ATTR));
				rootElement.appendChild(apiElement);
			}
		}

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return multiApiInputDoc;
	}
	 */
	/**
	 * Method to prepare the API element for multiApi API call
	 * of "MultiApiChangeShipmentPack" command
	 * @param multiApiInputDoc
	 * 			<br/> - Input document for multiApi API call
	 * @param shipmentKey
	 * 			<br/> - ShipmentKey attribute value for which the changeShipment is to be called
	 * @return Element
	 * 			<br/> - the API Element for multiApi call
	 * 			<pre>
	 * 			{@code
	 * 				<API Name="changeShipment">
	 * 					<Input>
	 * 						<Shipment ShipmentKey="" ShipmentPackComplete="Y" />
	 * 					</Input>
	 * 				</API>
	 * 			}
	 * 			</pre>
	 */
	/*
	private Element prepareAPIElementForChangeShipment(Document multiApiInputDoc, String shipmentKey) {
		final String methodName="prepareAPIElementForChangeShipment(multiApiInputDoc, shipmentKey)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		Element apiElement = multiApiInputDoc.createElement(AcademyPCAConstants.API_ELEMENT);

		apiElement.setAttribute(AcademyPCAConstants.NAME_ATTR, AcademyPCAConstants.CHANGE_SHIPMENT_API);
		Element inputElement = multiApiInputDoc.createElement(AcademyPCAConstants.MULTI_API_INPUT_ELEMENT);

		Element shipmentElement = multiApiInputDoc.createElement(AcademyPCAConstants.SHIPMENT_ELEMENT);
		shipmentElement.setAttribute(AcademyPCAConstants.SHIPMENT_KEY_ATTR, shipmentKey);
		shipmentElement.setAttribute(AcademyPCAConstants.SHIPMENT_PACK_COMPLT_ATTR, AcademyPCAConstants.STRING_Y);

		inputElement.appendChild(shipmentElement);
		apiElement.appendChild(inputElement);

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return apiElement;
	}
	 */
	/**
	 * Method for validating the text box.
	 */
	public YRCValidationResponse validateTextField(String fieldName, String fieldValue) {
		// TODO Validation required for the following controls.

		// TODO Create and return a response.
		return super.validateTextField(fieldName, fieldValue);
	}

	/**
	 * Method for validating the combo box entry.
	 */
	public void validateComboField(String fieldName, String fieldValue) {
		// TODO Validation required for the following controls.

		// TODO Create and return a response.
		super.validateComboField(fieldName, fieldValue);
	}

	/**
	 * Method called when a link is clicked.
	 */
	public YRCValidationResponse validateLinkClick(String fieldName) {
		// TODO Validation required for the following controls.

		// TODO Create and return a response.
		return super.validateLinkClick(fieldName);
	}

	/**
	 * Create and return the binding data for advanced table columns added to the tables.
	 */
	public YRCExtendedTableBindingData getExtendedTableBindingData(String tableName, ArrayList tableColumnNames) {
		// Create and return the binding data definition for the table.

		// The defualt super implementation does nothing.
		return super.getExtendedTableBindingData(tableName, tableColumnNames);
	}
}
