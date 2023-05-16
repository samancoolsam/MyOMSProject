package com.academy.som.closemanifest.rules;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.xml.xpath.XPathConstants;

import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.som.closemanifest.wizards.AcademyCloseManifestWizard;
import com.academy.som.closemanifest.wizards.AcademyCloseManifestWizardBehavior;
import com.academy.som.util.AcademyPCAConstants;
import com.academy.som.util.AcademyPCAErrorMessagesInterface;
import com.academy.som.util.logging.AcademySIMTraceUtil;
import com.yantra.yfc.rcp.IYRCWizardRule;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCDesktopUI;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCWizard;
import com.yantra.yfc.rcp.YRCXPathUtils;
import com.yantra.yfc.rcp.YRCXmlUtils;

public class AcademyCloseManifestEndRule 
implements IYRCWizardRule
{
  private static String PREVIOUS_PAGE = "CLOSE_MANIFEST";
  private HashMap modelMap;
  private String nextPage;
  private String CLASSNAME = "com.academy.som.closemanifest.rules.AcademyCloseManifestEndRule";
  private String manifestKeySeltd;
  private String manifestNoSeltd;
  private String ShipNode = "";
  
  public String execute(HashMap namespaceModelMap)
  {
    init(namespaceModelMap, PREVIOUS_PAGE);
    closeManifest();
    return this.nextPage;
  }
  
  private void init(HashMap namespaceModelMap, String nextPage)
  {
    this.modelMap = namespaceModelMap;
    this.nextPage = nextPage;
  }
  
  public static void setStatusMessage(final String statusMessage)
  {
	  PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable()
	    {
	      public void run()
	      {
	        AcademyCloseManifestWizard currentPage = (AcademyCloseManifestWizard)YRCDesktopUI.getCurrentPage();
	        AcademyCloseManifestWizardBehavior wizBehavior = currentPage.getMyBehavior();
	        //wizBehavior.setStatusMessage(this.val $ statusMessage);
	        wizBehavior.setStatusMessage(statusMessage);
	      }
	    });
  } 
  
  public static Element getModel(String modelName, HashMap namespaceModelMap)
  {
    Element model = null;
    Object modelObj = namespaceModelMap.get(modelName);
    if (modelObj != null) {
      model = (Element)modelObj;
    }
    return model;
  }
  
  public void closeManifest() {
		
	  final String methodName="closeManifest()";
	  AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		
	  Element openManifestModelElement = null;
	  String manifestNoXpath;
	  String manifestDateXpath;
	  String manifestDateTime;
	  String manifestDate;
	  String manifestScacXpath;
	  String manifestScac;
	  String errorMessage;
	  
		manifestKeySeltd = getModel(AcademyPCAConstants.SELTD_MANIFEST_OOB_MODEL, this.modelMap).getAttribute(AcademyPCAConstants.SELTD_MANIFEST_ATTR);
		
		if(!YRCPlatformUI.isVoid(manifestKeySeltd)){
			
			openManifestModelElement = getModel(AcademyPCAConstants.OPEN_MANIFEST_LIST_OOB_MODEL, this.modelMap); 

			manifestNoXpath = (new StringBuilder())
				.append(AcademyPCAConstants.MANIFEST_LIST_MANIFEST_ELE_XPATH)
				.append("[@").append(AcademyPCAConstants.MANIFEST_KEY_ATTR)
				.append("='").append(manifestKeySeltd).append("']/@")
				.append(AcademyPCAConstants.MANIFEST_NO_ATTR).toString();

			manifestDateXpath = (new StringBuilder())
				.append(AcademyPCAConstants.MANIFEST_LIST_MANIFEST_ELE_XPATH)
				.append("[@").append(AcademyPCAConstants.MANIFEST_KEY_ATTR)
				.append("='").append(manifestKeySeltd).append("']/@")
				.append(AcademyPCAConstants.MANIFEST_DATE_ATTR).toString();

			String ShipNodeXpath = (new StringBuilder()).append(AcademyPCAConstants.MANIFEST_LIST_MANIFEST_ELE_XPATH).
			append("[@").append(AcademyPCAConstants.MANIFEST_KEY_ATTR).append("='").append(manifestKeySeltd).append("']/@").
			append(AcademyPCAConstants.SHIPNODE_ATTR).toString();
			
			manifestNoSeltd = (String) YRCXPathUtils.evaluate(openManifestModelElement,
								manifestNoXpath, XPathConstants.STRING);
			
			ShipNode = (String) YRCXPathUtils.evaluate(openManifestModelElement, ShipNodeXpath, XPathConstants.STRING);

			manifestDateTime = (String) YRCXPathUtils.evaluate(openManifestModelElement,
								manifestDateXpath, XPathConstants.STRING);

			manifestDate = (manifestDateTime.split(AcademyPCAConstants.STRING_T))[0];

			if(isEligibleForClose(manifestKeySeltd)){//for manifests containing all shipments in status >= '1100.70.06.30'
				if(!isPastManifestDate(manifestDate)){
					if(!isFutureManifestDate(manifestDate)){//for Current Day Manifests
						manifestScacXpath = (new StringBuilder())
						.append(AcademyPCAConstants.MANIFEST_LIST_MANIFEST_ELE_XPATH)
						.append("[@").append(AcademyPCAConstants.MANIFEST_KEY_ATTR)
						.append("='").append(manifestKeySeltd).append("']/@")
						.append(AcademyPCAConstants.SCAC_ATTR).toString();
						
						manifestScac = (String) YRCXPathUtils.evaluate(openManifestModelElement,
						manifestScacXpath, XPathConstants.STRING);
						
						AcademySIMTraceUtil.logMessage("Calling command : "+AcademyPCAConstants.MAKE_NON_WORKING_COMMAND);
						callMakeNonWorkingService(prepareInputForMakeNonWorkingDay(manifestScac));
					}else{
						//for Future Day Manifests
						errorMessage = YRCPlatformUI.getString(AcademyPCAErrorMessagesInterface.FUTURE_MANIFEST_ERR_MSG_KEY);
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
				errorMessage = YRCPlatformUI.getString(AcademyPCAErrorMessagesInterface.CLOSE_MANIFEST_BKRM_ERR_MSG_KEY);
				showError(errorMessage);
				AcademySIMTraceUtil.logMessage("Error Message : '"+errorMessage+"' shown for Manifest No "+manifestNoSeltd);
				return;
			}
		} else {
		      setStatusMessage("Status_Select_A_Manifest");
		}
		

		
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
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
		Element openManifestModelElement = getModel(AcademyPCAConstants.OPEN_MANIFEST_LIST_OOB_MODEL, this.modelMap);
		String shipmentsListXpath = (new StringBuilder())
											.append(AcademyPCAConstants.MANIFEST_LIST_MANIFEST_ELE_XPATH)
											.append("[@").append(AcademyPCAConstants.MANIFEST_KEY_ATTR)
											.append("='").append(manifestKey).append("']/")
											.append(AcademyPCAConstants.SHIPMENTS_ELEMENT).append("/")
											.append(AcademyPCAConstants.SHIPMENT_ELEMENT).toString();
		
		NodeList shipmentNL = (NodeList)YRCXPathUtils.evaluate(openManifestModelElement, shipmentsListXpath, XPathConstants.NODESET);
		Element shipmentElement;
		String status;
		 
		for(int listIndex = 0; listIndex < shipmentNL.getLength(); listIndex++){
			shipmentElement = (Element)shipmentNL.item(listIndex);
			status = shipmentElement.getAttribute(AcademyPCAConstants.STATUS_ATTR);
			
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
	 * Method to call custom service "AcademySFSMakeNonWorkingDay"
	 * to make the carrier Non Working for Today
	 * @param storeCalendarInputDoc
	 * 			<br/> - Input Document to service to make the carrier Non Working for Today 
	 */
	private void callMakeNonWorkingService(Document storeCalendarInputDoc) {
		final String methodName="callMakeNonWorkingService(storeCalendarInputDoc)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		
		YRCApiContext context = new YRCApiContext();
		context.setFormId("com.academy.som.closemanifest.wizards.AcademyCloseManifestWizard");
		context.setApiName(AcademyPCAConstants.MAKE_NON_WORKING_COMMAND);
		context.setInputXml(storeCalendarInputDoc);
		YRCPlatformUI.callApi(context, null);
		
		if (context.getInvokeAPIStatus() > -1) {
			callChangeShipmentMultiApiForPack(manifestKeySeltd);
		}
		
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}	
	
	/**
	 * Method to show the error messages.
	 *
	 * @param errorMessage
	 * 			<br/> - The error message to be shown on the screen.
	 *
	 * @return YRCValidationResponse
	 * 			<br/> -  The object has the required error message in it
	 */
	private void showError(final String errorMessage) {
		YRCPlatformUI.showError(AcademyPCAConstants.STRING_ERROR, errorMessage);
	}	
	
	
	/**
	 * Method to call multiApi of changeShipment APIs for updating the status of
	 * shipments from "Ready To Ship" to "Shipment Packed" in order to make them
	 * eligible for closeManifest API call.
	 * @param manifestKey
	 * 			<br/> - ManifestKey attribute value of the manifest selected for close
	 */
	private void callChangeShipmentMultiApiForPack(String manifestKey) {
		final String methodName="callChangeShipmentMultiApiForPack(manifestKey)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		
		YRCApiContext context = new YRCApiContext();
		context.setFormId("com.academy.som.closemanifest.wizards.AcademyCloseManifestWizard");
		context.setApiName(AcademyPCAConstants.MULTI_API_CHNG_SHIPMENT_PACK_COMMAND);
		context.setInputXml(prepareMultiApiInputForChangeShipment(manifestKey));
		YRCPlatformUI.callApi(context,null);
		
		if (context.getInvokeAPIStatus() > -1) {
			callCloseManifestServ(manifestKeySeltd);
		}
		
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}	
	
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
	private Document prepareMultiApiInputForChangeShipment(String manifestKey){
		final String methodName="prepareMultiApiInputForChangeShipment(manifestKey)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		
		Document multiApiInputDoc = YRCXmlUtils.createDocument(AcademyPCAConstants.MULTI_API_ELEMENT);
		Element rootElement = multiApiInputDoc.getDocumentElement();
		
		Element openManifestModelElement = getModel(AcademyPCAConstants.OPEN_MANIFEST_LIST_OOB_MODEL, this.modelMap);
		String shipmentsListXpath = (new StringBuilder())
											.append(AcademyPCAConstants.MANIFEST_LIST_MANIFEST_ELE_XPATH)
											.append("[@").append(AcademyPCAConstants.MANIFEST_KEY_ATTR)
											.append("='").append(manifestKey).append("']/")
											.append(AcademyPCAConstants.SHIPMENTS_ELEMENT).append("/")
											.append(AcademyPCAConstants.SHIPMENT_ELEMENT).toString();
		
		NodeList shipmentNL = (NodeList)YRCXPathUtils.evaluate(openManifestModelElement, shipmentsListXpath, XPathConstants.NODESET);
		
		Element shipmentElement;
		Element apiElement;
		
		for(int listIndex = 0; listIndex < shipmentNL.getLength(); listIndex++){
			shipmentElement = (Element)shipmentNL.item(listIndex);
			
			if(shipmentElement.getAttribute(AcademyPCAConstants.STATUS_ATTR).trim()
					.equals(AcademyPCAConstants.STATUS_READY_FOR_CUSTOMER_VAL)){
				
				apiElement = prepareAPIElementForChangeShipment(multiApiInputDoc, 
											shipmentElement.getAttribute(AcademyPCAConstants.SHIPMENT_KEY_ATTR));
				rootElement.appendChild(apiElement);
			}
		}
		
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return multiApiInputDoc;
	}
	
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
	
	/**
     * Method to call the custom service for Closing the Manifest
     * selected
     */
	public void callCloseManifestServ(String manifestKeySeltd) {
		final String methodName="callCloseManifestServ()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);	
	
		YRCApiContext context = new YRCApiContext();
		context.setFormId("com.academy.som.closemanifest.wizards.AcademyCloseManifestWizard");
		context.setApiName(AcademyPCAConstants.MANAGE_TASK_Q_API);
		context.setInputXml(prepareInputForManageTasQ(manifestKeySeltd));
		YRCPlatformUI.callApi(context, null);
		
		if (context.getInvokeAPIStatus() > -1) {
			YRCPlatformUI.showInformation("Manifest "+manifestNoSeltd, YRCPlatformUI.getString(AcademyPCAConstants.CLOSE_MANIFEST_SUCCESS_MSG));
//			YRCPlatformUI.setMessage("Status_Manifest_Closed");
		}
		
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}	
	
	/**
	 * Method to prepare the Input document to call
	 * "manageTaskQueue" API to insert record into YFS_TASK_Q table
	 * @return Document
	 * 			<br/> - Input document for API "manageTaskQueue"
	 * 			<pre>
	 * 			{@code
	 * 				<TaskQueue DataKey="" DataType="" HoldFlag="" Operation="" TransactionKey="" Transactionid=""/>
	 * 			}
	 * 			</pre>
	 */
	private Document prepareInputForManageTasQ(String manifestKeySeltd) {
		final String methodName="prepareInputForCloseManifest()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		
		
		Document manageTaskQtInputDoc = YRCXmlUtils.createDocument(AcademyPCAConstants.ELE_TASK_QUEUE);
		Element rootElement = manageTaskQtInputDoc.getDocumentElement();
		rootElement.setAttribute(AcademyPCAConstants.ATTR_DATA_KEY, manifestKeySeltd);
		rootElement.setAttribute(AcademyPCAConstants.ATTR_DATA_TYPE, AcademyPCAConstants.MANIFEST_KEY_ATTR);
		rootElement.setAttribute(AcademyPCAConstants.ATTR_HOLDFLAG, AcademyPCAConstants.STRING_N);
		rootElement.setAttribute(AcademyPCAConstants.ATTR_OPERATION, AcademyPCAConstants.ACT_CREATE);
		rootElement.setAttribute(AcademyPCAConstants.TRANSACTION_KEY, AcademyPCAConstants.CLOSE_MANIFEST_TRANS_KEY);
		rootElement.setAttribute(AcademyPCAConstants.TRANSACTION_ID, AcademyPCAConstants.TRANS_ID_CLOSE_MANIFEST);
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return manageTaskQtInputDoc;
	}

}

