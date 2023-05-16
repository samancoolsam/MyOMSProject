package com.academy.som.printpickticket.screens;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.academy.som.util.AcademyPCAConstants;
import com.academy.som.util.AcademyPCAErrorMessagesInterface;
import com.academy.som.util.logging.AcademySIMTraceUtil;
import com.sun.org.apache.xml.internal.security.utils.XMLUtils;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCBaseBehavior;
import com.yantra.yfc.rcp.YRCBehavior;
import com.yantra.yfc.rcp.YRCDesktopUI;
import com.yantra.yfc.rcp.YRCEditorInput;
import com.yantra.yfc.rcp.YRCEditorPart;
import com.yantra.yfc.rcp.YRCFormatResponse;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCWizardExtensionBehavior;
import com.yantra.yfc.rcp.YRCXmlUtils;
import java.util.Iterator;
import com.yantra.yfc.rcp.internal.YRCRelatedTasksManager;
import com.yantra.yfc.rcp.YRCRelatedTask;




/**
 * Custom wizard extension behavior class that overrides the functionality
 * of OOB Class: "com.yantra.pca.sop.rcp.tasks.outboundexecution.pickup.printpickticket.wizards.SOPOutboundPrintPickTicketWizard"
 * 
 * @author <a href="mailto:KaushikN.Sanji@cognizant.com">Kaushik N Sanji</a>
 * Copyright © 2006-2009 Sterling Commerce, Inc. All Rights Reserved.
 * 
 * Modified as part of Upgrade to SOM 9.2.1 
 * 
 */
public class AcademyPrintPickTicketForItemExtnWizardBehavior extends YRCWizardExtensionBehavior {

	private static String CLASSNAME = "AcademyPrintPickTicketForItemExtnWizardBehavior";

	HashMap<String, String> aMap = new HashMap<String, String>();

	private String strSinglePckstnCodeValue;

	private String strConcatShipNode;

	public IYRCComposite createPage(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void pageBeingDisposed(String arg0) {
		// TODO Auto-generated method stub
	}

	/**
	 * Called when a wizard page is about to be shown for the first time.
	 * @param pageBeingShown
	 * 			<br/> - Current wizard PageID being shown
	 */
	public void initPage(String pageBeingShown) {
		final String methodName="initPage(pageBeingShown)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		AcademySIMTraceUtil.logMessage("pageBeingShown"+pageBeingShown);
		// Start - Fix provided as part of SOM 9.3 upgrade - to hide Backroom pick tasks under general
		 ArrayList relatedTaskList = YRCRelatedTasksManager.getRelatedTasks("YCD_TASK_PRINT_PICK_TICKET");
		HashMap <YRCRelatedTask,String> tasksToBeRemoved = new HashMap <YRCRelatedTask,String>();

		for (Iterator iter = relatedTaskList.iterator(); iter.hasNext(); )
		{
		  YRCRelatedTask element = (YRCRelatedTask)iter.next();

		  if (element.getId().equals("YCD_TASK_BACKROOM_PICK"))
		  {
			tasksToBeRemoved.put(element, "");
		  }

		}
		for (Iterator iter = tasksToBeRemoved.keySet().iterator(); iter.hasNext(); )
		{
		  YRCRelatedTask element = (YRCRelatedTask)iter.next();
		  if (element.getId().equals("YCD_TASK_BACKROOM_PICK"))
		  {
		  relatedTaskList.remove(element);
		  }

		}

		// End - Fix provided as part of SOM 9.3 upgrade - to hide Backroom pick tasks under general
                                

		//disabling fields and actions on page load
		disableField(AcademyPCAConstants.CUSTOM_TXT_REPRINT_PAGE_NO);
		disableField(AcademyPCAConstants.CUSTOM_CMB_BATCH_GRP);
		disableField(AcademyPCAConstants.CUSTOM_BTN_REPRINT_SHIPMENT_F7);
		disableField(AcademyPCAConstants.CUSTOM_BTN_REPRINT_ITEM_F4);
		YRCPlatformUI.enableAction(AcademyPCAConstants.CUSTOM_ACTION_PRINT_ITEM_PICK_TICKET, false);
		YRCPlatformUI.enableAction(AcademyPCAConstants.CUSTOM_ACTION_REPRINT_SHIPMENT_PICK_TICKETS, false);
		disableField(AcademyPCAConstants.CUSTOM_TXT_BATCH_SIZE_PRINT);
		disableField(AcademyPCAConstants.CUSTOM_BTN_PRINT_SHIPMENT_PICK_TICKETS_F8);
		YRCPlatformUI.enableAction(AcademyPCAConstants.CUSTOM_ACTION_SHIPMENT_PICK_TICKETS, false);

		//setting the OOB Panel Header title with custom title
		setFieldValue(AcademyPCAConstants.OOB_LBL_PNL_HEADER, YRCPlatformUI.getString(AcademyPCAConstants.LBL_PNL_HEADER_MSG));
		//SFS2.0 001 printer Id
		getCommonCodeList("MUL_PACK_STORE");		
		//SFS2.0 001 printer Id
		//Controlling the Manifesting Day panel to be opened for a supervisor only
		if(YRCPlatformUI.getUserElement().getAttribute(AcademyPCAConstants.IS_SUPERVISOR_ATTR).equals(AcademyPCAConstants.STRING_N)){
			disableField(AcademyPCAConstants.CUSTOM_PNL_MANIFESTING_DAY);
			setControlVisible(AcademyPCAConstants.CUSTOM_PNL_MANIFESTING_DAY, false);
			this.relayoutScreen();
		}

		//SIM to SOM Conversion: START
		//To Populate the pending shipments count - extn_lblShipmentNo
		callGetShipmentListForPendingShipmentsCount();
		//SIM to SOM Conversion: END

		callCommonCodeForPrintBatchSize(); 
		callPickTicketNoListService(); 


		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		super.initPage(pageBeingShown);
	}

	/**
	 * This method hides the field and removes the space occupied by the field.
	 *
	 * @param fieldName 	- The the field to be hidden
	 * @param extnBehavior 	- The extnBehavior class of the screen
	 *
	 */
	public static void hideField(final String fieldName, final Object extnBehavior) {
		GridData gridData = new GridData();
		gridData.exclude = true;
		YRCBaseBehavior parentBehavior = ((YRCBaseBehavior) extnBehavior);
		parentBehavior.setControlVisible(fieldName, false);
		parentBehavior.setControlLayoutData(fieldName, gridData);
	}

	/**
	 *	Method to call custom view service to populate the
	 *  Pick ticket Number combo for reprint 
	 *
	 */
	private void callPickTicketNoListService() {
		final String methodName="callPickTicketNoListService()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		YRCApiContext context = new YRCApiContext();
		context.setFormId(getFormId());
		context.setApiName(AcademyPCAConstants.GET_PICK_TICKET_NO_LIST_COMMAND);
		context.setInputXml(prepareInputForPickTicketNo());
		callApi(context);

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}

	/**
	 * Method to prepare the Input document for populating the Pick Ticket No combo field
	 * @return Document
	 * 			<br/> - Input for getting Pick Ticket No List
	 * 			<pre>
	 * 			{@code
	 * 				<SFSShipmentPickTicketNo PickticketNo="" ShipNode="<<ShipNode of logged in User>>">
	 *					<OrderBy>
	 * 					  <Attribute Desc="Y" Name="PickticketNo"/>
	 *					</OrderBy>
	 *				</SFSShipmentPickTicketNo>
	 * 			}
	 * 			</pre>
	 */
	private Document prepareInputForPickTicketNo() {
		final String methodName="prepareInputForPickTicketNo()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		Document pickTicketNoInputDoc = YRCXmlUtils.createDocument(AcademyPCAConstants.SFS_PICK_TICKET_NO_ELEMENT);
		Element rootElement = pickTicketNoInputDoc.getDocumentElement();
		rootElement.setAttribute(AcademyPCAConstants.PICK_TICKET_NO_ATTR, AcademyPCAConstants.EMPTY_STRING);
		rootElement.setAttribute(AcademyPCAConstants.SHIPNODE_ATTR,
				YRCPlatformUI.getUserElement().getAttribute(AcademyPCAConstants.SHIPNODE_ATTR));
		Element orderByElement = pickTicketNoInputDoc.createElement(AcademyPCAConstants.ORDER_BY_ELEMENT);
		Element attrElement = pickTicketNoInputDoc.createElement(AcademyPCAConstants.ATTRIBUTE_ELEMENT);
		attrElement.setAttribute(AcademyPCAConstants.NAME_ATTR, AcademyPCAConstants.PICK_TICKET_NO_ATTR);
		attrElement.setAttribute(AcademyPCAConstants.DESCENDING_ATTR, AcademyPCAConstants.STRING_Y);
		orderByElement.appendChild(attrElement);
		rootElement.appendChild(orderByElement);

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return pickTicketNoInputDoc;
	}

	/**
	 * Method to call getCommonCodeList API for populating Batch size print combo
	 *
	 */
	private void callCommonCodeForPrintBatchSize() {
		final String methodName="callCommonCodeForPrintBatchSize()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		YRCApiContext context = new YRCApiContext();
		context.setFormId(getFormId());
		context.setApiName(AcademyPCAConstants.GET_PRINT_BATCH_SIZE_COMMAND);
		context.setInputXml(prepareInputForPrintBatchSize());
		callApi(context);

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}

	/**
	 * Method to prepare the Input document for getCommonCodeList API Call
	 * for populating the Print Batch Size combo
	 * @return Document
	 * 			<br/> - Input to getCommonCodeList API
	 * 			<pre>
	 * 			{@code
	 * 				<CommonCode CodeType="PRINT_BATCH_SIZ"/>
	 * 			}
	 * 			</pre>
	 */
	private Document prepareInputForPrintBatchSize() {
		final String methodName="prepareInputForPrintBatchSize()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		Document commonCodeInputDoc = YRCXmlUtils.createDocument(AcademyPCAConstants.COMMON_CODE_ELEMENT);
		Element rootElement = commonCodeInputDoc.getDocumentElement();
		rootElement.setAttribute(AcademyPCAConstants.CODE_TYPE_ATTR, AcademyPCAConstants.CODE_TYPE_VAL_FOR_PRINT_BATCH);

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return commonCodeInputDoc;
	}

	/**
	 * Method for modifying the Input to OOB API Calls
	 * @param context
	 *          <br/> - The context in which API is called.
	 * @return boolean.
	 * 			<br/> - return false to stop the call
	 */
	public boolean preCommand(YRCApiContext context) {
		final String methodName="preCommand(context)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		// Commenting out the Below Logic , since SOM does not contain the OOb Command as "GetShipmentList"

		//if(context.getApiName().equals(AcademyPCAConstants.GET_SHIPMENT_LIST_OOB_COMMAND)){
		//if(context.getApiName().equals(AcademyPCAConstants.GET_PENDING_SHIPMENT_LIST_COMMAND)){
		//AcademySIMTraceUtil.logMessage("Context API: "+context.getApiName());
		//Document shipmentListInput = context.getInputXml();
		/**
		 * OOB Input XML to getShipmentList API
		 * <pre>
		 * {@code
		 * 		<Shipment EnterpriseCode="DEFAULT" IsPickTicketPrinted="N"
		 *			ShipNode="<<ShipNode of logged in User>>" Status="1400" StatusQryType="LT">
		 *			<ShipmentLines>
		 *				<ShipmentLine/>
		 *			</ShipmentLines>
		 *		</Shipment>
		 * }
		 * </pre>
		 */
		/*Element rootElement = shipmentListInput.getDocumentElement();
			rootElement.setAttribute(AcademyPCAConstants.ENTERPRISE_CODE_ATTR, AcademyPCAConstants.EMPTY_STRING);
			rootElement.setAttribute(AcademyPCAConstants.STATUS_ATTR, AcademyPCAConstants.STATUS_READY_FOR_BACKROOM_PICK_VAL);
			rootElement.setAttribute(AcademyPCAConstants.STATUS_QRY_TYPE_ATTR, AcademyPCAConstants.STATUS_QRY_TYPE_EQ_VAL);
			context.setInputXml(shipmentListInput);
			AcademySIMTraceUtil.logMessage("Modified Input for "+context.getApiName()+" command: \n", rootElement);*/
		/**
		 * Modified Input XML to getShipmentList API
		 * <pre>
		 * {@code
		 * 		<Shipment EnterpriseCode="" IsPickTicketPrinted="N" ShipNode="<<ShipNode of logged in User>>"
		 *			Status="1100.70.06.10" StatusQryType="EQ">
		 *			<ShipmentLines>
		 *				<ShipmentLine/>
		 *			</ShipmentLines>
		 *		</Shipment>
		 * }
		 * </pre>
		 */
		//}

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return super.preCommand(context);
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

		if(AcademyPCAConstants.CUSTOM_RDBTN_PRINT_PICK_TICKET.equals(fieldName)){
			AcademySIMTraceUtil.logMessage("fieldName: "+fieldName);
			//to manage the fields corresponding to the current radio selection for extn_rdbPrintTicketsForItem field

			enableField(AcademyPCAConstants.CUSTOM_TXT_REPRINT_PAGE_NO);
			enableField(AcademyPCAConstants.CUSTOM_CMB_BATCH_GRP);
			enableField(AcademyPCAConstants.CUSTOM_BTN_REPRINT_SHIPMENT_F7);
			enableField(AcademyPCAConstants.CUSTOM_BTN_REPRINT_ITEM_F4);

			YRCPlatformUI.enableAction(AcademyPCAConstants.CUSTOM_ACTION_PRINT_ITEM_PICK_TICKET, true);
			YRCPlatformUI.enableAction(AcademyPCAConstants.CUSTOM_ACTION_REPRINT_SHIPMENT_PICK_TICKETS, true);
			


		}else if (AcademyPCAConstants.CUSTOM_BTN_REPRINT_ITEM_F4.equals(fieldName)) {
			enableField(AcademyPCAConstants.CUSTOM_BTN_REPRINT_ITEM_F4);
			String batchNoSelected = getFieldValue(AcademyPCAConstants.CUSTOM_CMB_BATCH_GRP);
			if(!isFieldBlank(batchNoSelected)){
				//if Pick ticket Batch Number is selected for reprint then call service for reprinting item pick tickets
				AcademySIMTraceUtil.logMessage(methodName+" :: Batch Number selected: "+batchNoSelected);
				callServiceForReprintItemPickTickets();
			}
			else{
				//if Pick ticket Batch Number is not selected for reprint then throw an error message
				AcademySIMTraceUtil.logMessage(methodName+" :: validateButtonClick(fieldName):: Batch Number selected: NULL");
				String errorMessage = YRCPlatformUI.getString(AcademyPCAErrorMessagesInterface.BATCH_NO_VAL_ERR_MSG_KEY);
				response = showError(errorMessage);
			}

		}else if (AcademyPCAConstants.CUSTOM_BTN_REPRINT_SHIPMENT_F7.equals(fieldName)) {
			enableField(AcademyPCAConstants.CUSTOM_BTN_REPRINT_SHIPMENT_F7);
			String batchNoSelected = getFieldValue(AcademyPCAConstants.CUSTOM_CMB_BATCH_GRP);
			if(!isFieldBlank(batchNoSelected)){
				//if Pick ticket Batch Number is selected for reprint then call service for reprinting shipment pick tickets
				AcademySIMTraceUtil.logMessage(methodName+" :: validateButtonClick(fieldName):: Batch Number selected: "+batchNoSelected);
				callServiceForReprintShipmentPickTickets();
			}
			else{
				//if Pick ticket Batch Number is not selected for reprint then throw an error message
				AcademySIMTraceUtil.logMessage(methodName+" :: validateButtonClick(fieldName):: Batch Number selected: NULL");
				String errorMessage = YRCPlatformUI.getString(AcademyPCAErrorMessagesInterface.BATCH_NO_VAL_ERR_MSG_KEY);
				response = showError(errorMessage);
			}

		}else if(AcademyPCAConstants.CUSTOM_BTN_RESET_PICK_TICKET.equals(fieldName)){
			enableField(AcademyPCAConstants.CUSTOM_BTN_RESET_PICK_TICKET);
			AcademySIMTraceUtil.logMessage(methodName+" :: Invoke service for  Reset Pick Tickets: ");
			callServiceForResetPickTickets();
		}
		else{
			//disable fields and action if current radio selection is other than extn_rdbPrintTicketsForItem field
			disableField(AcademyPCAConstants.CUSTOM_TXT_REPRINT_PAGE_NO);
			disableField(AcademyPCAConstants.CUSTOM_CMB_BATCH_GRP);
			disableField(AcademyPCAConstants.CUSTOM_BTN_REPRINT_SHIPMENT_F7);
			disableField(AcademyPCAConstants.CUSTOM_BTN_REPRINT_ITEM_F4);
			YRCPlatformUI.enableAction(AcademyPCAConstants.CUSTOM_ACTION_PRINT_ITEM_PICK_TICKET, false);
			YRCPlatformUI.enableAction(AcademyPCAConstants.CUSTOM_ACTION_REPRINT_SHIPMENT_PICK_TICKETS, false);
		}


		if(AcademyPCAConstants.CUSTOM_RDBTN_PRINT_SHIPMENT_PICK_TICKETS.equals(fieldName)){
			AcademySIMTraceUtil.logMessage("fieldName: "+fieldName);
			//to manage the fields corresponding to the current radio selection for extn_rdbPrintShipmentTickets field

			enableField(AcademyPCAConstants.CUSTOM_TXT_BATCH_SIZE_PRINT);
			enableField(AcademyPCAConstants.CUSTOM_BTN_PRINT_SHIPMENT_PICK_TICKETS_F8);
			YRCPlatformUI.enableAction(AcademyPCAConstants.CUSTOM_ACTION_SHIPMENT_PICK_TICKETS, true);
			/*//SFS2.0 001 Printer id
			getCommonCodeList("PICK_PRINTER_ID");
			//SFS2.0 001 Printer id
			 */			
		}else if (AcademyPCAConstants.CUSTOM_BTN_PRINT_SHIPMENT_PICK_TICKETS_F8.equals(fieldName)) {
			enableField(AcademyPCAConstants.CUSTOM_BTN_PRINT_SHIPMENT_PICK_TICKETS_F8);
			//if extn_btnPrintShipmentTicketsF8 clicked then call service for printing shipment and item pick tickets
			callServiceForShipmentPickTickets();

		}else{
			//disable fields and action if current radio selection is other than extn_rdbPrintShipmentTickets field
			disableField(AcademyPCAConstants.CUSTOM_BTN_PRINT_SHIPMENT_PICK_TICKETS_F8);
			disableField(AcademyPCAConstants.CUSTOM_TXT_BATCH_SIZE_PRINT);
			YRCPlatformUI.enableAction(AcademyPCAConstants.CUSTOM_ACTION_SHIPMENT_PICK_TICKETS, false);
		}
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return response;
	}
//	Start of SFS2.0 001 Printer id

	private void getCommonCodeList(String CodeType) {
		// TODO Auto-generated method stub
		AcademySIMTraceUtil.logMessage("Inside getCommonCodeList");
		YRCApiContext yrcApiContext = new YRCApiContext();
		yrcApiContext.setApiName("getCommonCodeList");
		Document inDoc = YRCXmlUtils.createDocument("CommonCode");		
		//Document outDocument = YRCXmlUtils.createDocument("CommonCodeList");
		yrcApiContext.setInputXml(getCommonCodeListInput(inDoc,CodeType).getOwnerDocument());
		//Element rootElement1=getCommonCodeListInput(inDoc,CodeType);
		//yrcApiContext.setInputXml(rootElement1.getOwnerDocument());
		yrcApiContext.setFormId(this.getFormId());
		AcademySIMTraceUtil.logMessage("formid"+ this.getFormId());
		callApi(yrcApiContext);
	}



	private Element getCommonCodeListInput(Document docInputgetCommonCodeList,String codeType) {
		AcademySIMTraceUtil.logMessage("Inside getCommonCodeListInput");
		final String methodName="getCommonCodeListInput(docInputgetCommonCodeList)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);		
		Element rootElement = docInputgetCommonCodeList.getDocumentElement();
		rootElement.setAttribute("CodeType", codeType);
		AcademySIMTraceUtil.logMessage("CodeType Is :" + codeType);
		AcademySIMTraceUtil.logMessage("INPUT to getCommonCodeList : " + YRCXmlUtils.getString(rootElement));
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return rootElement;
	}

//	Start of SFS2.0 001 Printer id
	/**
	 * Method to show the error messages.
	 *
	 * @param errorMessage
	 *            The error message to be shown on the screen.
	 *
	 * @return YRCValidationResponse
	 * 			<br/> - The object has the required error message in it
	 */

	private YRCValidationResponse showError(final String errorMessage) {
		final YRCFormatResponse response = new YRCFormatResponse(
				YRCValidationResponse.YRC_VALIDATION_ERROR, errorMessage, null);
		YRCPlatformUI.showError(AcademyPCAConstants.STRING_ERROR, errorMessage);
		return response;
	}

	/**
	 * Method to call custom service to print Shipment and Item
	 * Pick tickets
	 *
	 */
	public void callServiceForShipmentPickTickets() {
		final String methodName="callServiceForShipmentPickTickets()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		YRCApiContext context = new YRCApiContext();
		context.setFormId(getFormId());
		context.setApiName(AcademyPCAConstants.PRINT_SHIPMENT_PICK_TICKETS_SERVICE_COMMAND);
		Document doc=prepareInputForShipmentPickTickets();
		context.setInputXml(doc);// STL-1501 to avoid executing same logic twice.
		AcademySIMTraceUtil.logMessage("input to Shipment pick ticket"+YRCXmlUtils.getString(doc));
		callApi(context);

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}

	/**
	 * Method to prepare Input document for the custom service to
	 * print Shipment and Item Pick tickets
	 * @return Document
	 * 			<br/> - Input for printing Shipment and Item Pick tickets
	 * 			<pre>
	 * 			{@code
	 * 				<Print ShipNode="" MaximumRecords="<<extn_cmbBatchSizePrint>>" />
	 * 			}
	 * 			</pre>
	 */
	private Document prepareInputForShipmentPickTickets() {
		final String methodName="prepareInputForShipmentPickTickets()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		Document printInputDoc = YRCXmlUtils.createDocument(AcademyPCAConstants.PRINT_ELEMENT);
		Element rootElement = printInputDoc.getDocumentElement();
		rootElement.setAttribute(AcademyPCAConstants.SHIPNODE_ATTR, YRCPlatformUI.getUserElement().getAttribute(AcademyPCAConstants.SHIPNODE_ATTR));
		rootElement.setAttribute(AcademyPCAConstants.MAX_RECORDS_ATTR, getFieldValue(AcademyPCAConstants.CUSTOM_TXT_BATCH_SIZE_PRINT));
		//Start of SFS2.0 001 Printer id
		//AcademySIMTraceUtil.logMessage("printer id selected"+getFieldValue("extn_PrinterID_Combobox"));
		AcademySIMTraceUtil.logMessage("Printer_Id selected is "+getFieldValue("extn_PrinterIdCombo"));
		String strComboboxValue = getFieldValue("extn_PrinterIdCombo");
		if(strComboboxValue!=null)
		{
			rootElement.setAttribute("PrinterID",strComboboxValue);
			AcademySIMTraceUtil.logMessage("Printer_Id selected is "+strComboboxValue);
		}
		else{
			rootElement.setAttribute("PrinterID", strSinglePckstnCodeValue);
			AcademySIMTraceUtil.logMessage("Printer_Id selected is "+strSinglePckstnCodeValue);
			//disableField("extn_PrinterIdCombo");
		}
		//START : STL-1501 Print pick ticket for MO device
		Element eleUserNameSpaceModel = getModel(AcademyPCAConstants.USER_NAME_SPACE_MODEL);
		Document docLogin = YRCXmlUtils.createDocument(AcademyPCAConstants.ELE_LOGIN);
		Element eleLogin = docLogin.getDocumentElement();
		eleLogin.setAttribute(AcademyPCAConstants.ATTR_LOGIN_ID, eleUserNameSpaceModel.getAttribute(AcademyPCAConstants.ATTR_USER_LOGIN_ID));
		//rootElement.appendChild(eleLogin);
		YRCXmlUtils.importElement(rootElement, eleLogin);
		//END : STL-1501 Print pick ticket for MO device
				
		//Start of SFS2.0 001 Printer id
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return printInputDoc;
	}

	/**
	 * Method to the call the custom service to
	 * reprint Shipment pick tickets
	 *
	 */
	public void callServiceForReprintShipmentPickTickets() {
		final String methodName="callServiceForShipmentPickTickets()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		YRCApiContext context = new YRCApiContext();
		context.setFormId(getFormId());
		context.setApiName(AcademyPCAConstants.PRINT_SHIPMENT_PICK_TICKETS_SERVICE_COMMAND);
		Document outDoc=prepareInputForReprintBatch();
		context.setInputXml(prepareInputForReprintBatch());
		AcademySIMTraceUtil.logMessage("input to reprint"+YRCXmlUtils.getString(outDoc));
		callApi(context);


		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}

	/**
	 * Method to prepare Input document for the custom service to reprint
	 * Shipment and Item Pick tickets
	 * @return Document
	 * 			<br/> - Input for reprinting Shipment and Item pick tickets
	 * 			<pre>
	 * 			{@code
	 * 				<Print ShipNode="<<ShipNode of logged in User>>" PickticketNo="<<extn_cmbBatchGroupPrint>>" 
	 * 					   PageNo="<<extn_txtReprintPageNo>>" />
	 * 			}
	 * 			</pre>
	 */
	private Document prepareInputForReprintBatch() {
		final String methodName="prepareInputForReprintBatch()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		String strPickTicketPrinted = "Y";
		AcademySIMTraceUtil.logMessage("PickTicketPrinted");

		Document printInputDoc = YRCXmlUtils.createDocument(AcademyPCAConstants.PRINT_ELEMENT);
		Element rootElement = printInputDoc.getDocumentElement();
		rootElement.setAttribute(AcademyPCAConstants.SHIPNODE_ATTR, YRCPlatformUI.getUserElement().getAttribute(AcademyPCAConstants.SHIPNODE_ATTR));
		rootElement.setAttribute(AcademyPCAConstants.PICK_TICKET_NO_ATTR, getFieldValue(AcademyPCAConstants.CUSTOM_CMB_BATCH_GRP));
		rootElement.setAttribute(AcademyPCAConstants.PAGE_NO_ATTR, getFieldValue(AcademyPCAConstants.CUSTOM_TXT_REPRINT_PAGE_NO));
		rootElement.setAttribute("PickTicketPrinted",strPickTicketPrinted);
		/*AcademySIMTraceUtil.logMessage("printer id selected"+getFieldValue("extn_PrinterID_Combobox"));
		rootElement.setAttribute("PrinterID", getFieldValue("extn_PrinterID_Combobox"));*/
		
		//START : STL-1501 Print pick ticket for MO device
		Element eleUserNameSpaceModel = getModel(AcademyPCAConstants.USER_NAME_SPACE_MODEL);
		Document docLogin = YRCXmlUtils.createDocument(AcademyPCAConstants.ELE_LOGIN);
		Element eleLogin = docLogin.getDocumentElement();
		eleLogin.setAttribute(AcademyPCAConstants.ATTR_LOGIN_ID, eleUserNameSpaceModel.getAttribute(AcademyPCAConstants.ATTR_USER_LOGIN_ID));
		//rootElement.appendChild(eleLogin);
		YRCXmlUtils.importElement(rootElement, eleLogin);
		//END : STL-1501 Print pick ticket for MO device
		
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		return printInputDoc;
	}

	//SIM to SOM Conversion : START

	/**
	 * Method to prepare Input document for the getShipmentList API and invoking the API.
	 * Considering only the shipments which is in "Ready For Backroom Pick" status as pending Shipments for Printing. 
	 * @return Document
	 * 			<br/> - Input for getShipmentList
	 * 			<pre>
	 * 			{@code
	 * 				<Shipment ShipNode="<<ShipNode of logged in User>>" EnterpriseCode="" 
	 * 							Status="1100.70.06.10" StatusQryType="EQ"
	 * 					   		PickTicketPrinted="Y" PickTicketPrintedQryType="NE"/>
	 * 			}
	 * 			</pre>
	 */
	private void callGetShipmentListForPendingShipmentsCount() {
		final String methodName="prepareInputToGetPendingShipmentsCount()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		Document InputDoc = YRCXmlUtils.createDocument(AcademyPCAConstants.SHIPMENT_ELEMENT);
		Element rootElement = InputDoc.getDocumentElement();
		rootElement.setAttribute(AcademyPCAConstants.ENTERPRISE_CODE_ATTR, AcademyPCAConstants.EMPTY_STRING);
		rootElement.setAttribute(AcademyPCAConstants.STATUS_ATTR, AcademyPCAConstants.STATUS_READY_FOR_BACKROOM_PICK_VAL);
		rootElement.setAttribute(AcademyPCAConstants.STATUS_QRY_TYPE_ATTR, AcademyPCAConstants.STATUS_QRY_TYPE_EQ_VAL);
		rootElement.setAttribute(AcademyPCAConstants.PICK_TICKET_PRINTED, AcademyPCAConstants.STRING_Y);
		rootElement.setAttribute(AcademyPCAConstants.PICK_TICKET_PRINTED_QRY_TYPE_ATTR, AcademyPCAConstants.QRY_TYPE_NE_VAL);
		rootElement.setAttribute(AcademyPCAConstants.SHIPNODE_ATTR, YRCPlatformUI.getUserElement().getAttribute(AcademyPCAConstants.SHIPNODE_ATTR));

		//calling getShipmentList API
		YRCApiContext yrcApiContext = new YRCApiContext();
		yrcApiContext.setFormId(getFormId());
		yrcApiContext.setApiName(AcademyPCAConstants.GET_PENDING_SHIPMENT_LIST_COMMAND);
		yrcApiContext.setInputXml(InputDoc);
		AcademySIMTraceUtil.logMessage("formid"+ this.getFormId());
		callApi(yrcApiContext);
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}

	//SIM to SOM Conversion : END

	/**
	 * Method to validate a field's value is blank or not
	 * @param fieldValue
	 * 			<br/> - fieldName's getFieldValue()
	 * @return boolean
	 * 			<br/> - returns <b>true</b> if fieldValue is blank
	 * 			<br/> - returns <b>false</b> if fieldValue is present
	 */
	public boolean isFieldBlank(String fieldValue){
		if(fieldValue!=null && fieldValue.trim().length()!=0){
			return false;
		}else
			return true;
	}

	/**
	 * Method to call the custom service
	 * to reprint Item Pick tickets
	 *
	 */
	public void callServiceForReprintItemPickTickets() {
		final String methodName="callServiceForReprintItemPickTickets()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		YRCApiContext context = new YRCApiContext();
		context.setFormId(getFormId());
		context.setApiName(AcademyPCAConstants.PRINT_PICK_TICKETS_SERVICE_COMMAND);
		context.setInputXml(prepareInputForReprintBatch());
		callApi(context);

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}
	
	/**
	 * Added as part of STL-1493
	 * Method to call the custom service
	 * to Reset Pick tickets Printed Flag
	 *
	 */
	public void callServiceForResetPickTickets() {
		final String methodName="callServiceForResetPickTickets()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		YRCApiContext context = new YRCApiContext();
		context.setFormId(getFormId());
		context.setApiName(AcademyPCAConstants.RESET_PICK_TICKETS_FLAG_SERVICE_COMMAND);
		context.setInputXml(prepareInputForResetPickTicket());
		callApi(context);

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}
	
	/**
	 * Added as part of STL-1493
	 * Method to prepare input document for getShipmentList API
	 * Input for getShipmentList
	 * 
	 * <Shipment ShipNode="<<ShipNode of logged in User>>"/>	
	 * 			
	 */
	private Document prepareInputForResetPickTicket() {
		final String methodName="prepareInputForResetPickTicket()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		
		Document InputDoc = YRCXmlUtils.createDocument(AcademyPCAConstants.SHIPMENT_ELEMENT);
		Element rootElement = InputDoc.getDocumentElement();
		rootElement.setAttribute(AcademyPCAConstants.SHIPNODE_ATTR, YRCPlatformUI.getUserElement().getAttribute(AcademyPCAConstants.SHIPNODE_ATTR));
		
		return InputDoc;
	}

	/**
	 * Superclass method called to modify the OOB screen models when set
	 * @param nameSpace
	 * 			<br/> - name of the OOB screen model
	 */
	public void postSetModel(String nameSpace) {
		final String methodName="postSetModel(nameSpace)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		// Commenting out the Below logic, since SOM does not contain the OOB ShipmentListAll as screen model

		//if(nameSpace.equals(AcademyPCAConstants.EXTN_GET_PENDING_SHIPMENT_LIST_MODEL)){

		//	AcademySIMTraceUtil.logMessage(methodName + " :: NameSpace " + nameSpace + " :: BEGIN");

		//when OOB ShipmentListAll screen model is set 
//		int pendingRecords = Integer.parseInt(getModel(AcademyPCAConstants.EXTN_GET_PENDING_SHIPMENT_LIST_MODEL)
//		.getAttribute(AcademyPCAConstants.TOTAL_NO_OF_RECORDS_ATTR));

//		AcademySIMTraceUtil.logMessage("Pending records"+ pendingRecords);
//		setFieldValue(AcademyPCAConstants.CUSTOM_LBL_SHIPMENT_NO, 
//		AcademyPCAConstants.STRING_OPEN_BRACE + pendingRecords + AcademyPCAConstants.STRING_CLOSE_BRACE);

//		/**
//		* to manage the fields corresponding to the current radio selection for extn_rdbPrintShipmentTickets field
//		* based on the number of pending shipmentList records
//		*/
//		if(pendingRecords>0){
//		enableField(AcademyPCAConstants.CUSTOM_RDBTN_PRINT_SHIPMENT_PICK_TICKETS);
//		enableField(AcademyPCAConstants.CUSTOM_TXT_BATCH_SIZE_PRINT);
//		enableField(AcademyPCAConstants.CUSTOM_TXT_PRINT_SHIPMENT_PICK_TICKETS1);
//		enableField(AcademyPCAConstants.CUSTOM_LBL_SHIPMENT_NO);
//		enableField(AcademyPCAConstants.CUSTOM_TXT_PRINT_SHIPMENT_PICK_TICKETS2);
//		}else{
//		disableField(AcademyPCAConstants.CUSTOM_BTN_PRINT_SHIPMENT_PICK_TICKETS_F8);
//		disableField(AcademyPCAConstants.CUSTOM_RDBTN_PRINT_SHIPMENT_PICK_TICKETS);
//		disableField(AcademyPCAConstants.CUSTOM_TXT_BATCH_SIZE_PRINT);
//		disableField(AcademyPCAConstants.CUSTOM_TXT_PRINT_SHIPMENT_PICK_TICKETS1);
//		disableField(AcademyPCAConstants.CUSTOM_LBL_SHIPMENT_NO);
//		disableField(AcademyPCAConstants.CUSTOM_TXT_PRINT_SHIPMENT_PICK_TICKETS2);
//		YRCPlatformUI.enableAction(AcademyPCAConstants.CUSTOM_ACTION_SHIPMENT_PICK_TICKETS, false);

//		AcademySIMTraceUtil.logMessage(methodName + " :: NameSpace " + nameSpace + " :: END");
//		}


		super.postSetModel(nameSpace);
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
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
			if(context.getApiName().equals(AcademyPCAConstants.PRINT_PICK_TICKETS_SERVICE_COMMAND)){
				//to handle the command "AcademySFSPrintItemPickTickets"
				AcademySIMTraceUtil.logMessage(methodName+" :: "+context.getApiName());
				//refreshCurrentPage();
			}

			if(context.getApiName().equals(AcademyPCAConstants.PRINT_SHIPMENT_PICK_TICKETS_SERVICE_COMMAND)){
				//to handle the command "AcademySFSPrintShipmentPickTickets"
				AcademySIMTraceUtil.logMessage(methodName+" :: "+context.getApiName());
				//refreshCurrentPage();
				//Instead of Page refresh, just changing the count & Batch #
				callGetShipmentListForPendingShipmentsCount();
				callPickTicketNoListService(); 
			}

			if(context.getApiName().equals(AcademyPCAConstants.GET_PRINT_BATCH_SIZE_COMMAND)){
				//to handle the command "GetPrintBatchSizeCC"
				AcademySIMTraceUtil.logMessage(methodName+" :: "+context.getApiName());
				Document commonCodeDoc = context.getOutputXml();
				setExtentionModel(AcademyPCAConstants.EXTN_PRINT_BATCH_SIZE_CC_MODEL, commonCodeDoc.getDocumentElement());
			}

			//SIM to SOM Upgrade : START

			if(context.getApiName().equals(AcademyPCAConstants.GET_PENDING_SHIPMENT_LIST_COMMAND)){
				//to handle the command "GetPendingShipments"
				AcademySIMTraceUtil.logMessage(methodName+" :: "+context.getApiName());
				Document shipmentDoc = context.getOutputXml();
				AcademySIMTraceUtil.logMessage("output#####"+YRCXmlUtils.getString(shipmentDoc));
				// Logic to get the Pending Shipments from getShipmentList Output and populating in Screen.
				int pendingRecords = Integer.parseInt(((Element) shipmentDoc.getElementsByTagName("Shipments").item(0)).getAttribute(AcademyPCAConstants.TOTAL_NO_OF_RECORDS_ATTR));

				setFieldValue(AcademyPCAConstants.CUSTOM_LBL_SHIPMENT_NO, 
						AcademyPCAConstants.STRING_OPEN_BRACE + pendingRecords + AcademyPCAConstants.STRING_CLOSE_BRACE);

				/**
				 * to manage the fields corresponding to the current radio selection for extn_rdbPrintShipmentTickets field
				 * based on the number of pending shipmentList records
				 */
				if(pendingRecords>0){
					enableField(AcademyPCAConstants.CUSTOM_RDBTN_PRINT_SHIPMENT_PICK_TICKETS);
					enableField(AcademyPCAConstants.CUSTOM_TXT_BATCH_SIZE_PRINT);
					enableField(AcademyPCAConstants.CUSTOM_TXT_PRINT_SHIPMENT_PICK_TICKETS1);
					enableField(AcademyPCAConstants.CUSTOM_LBL_SHIPMENT_NO);
					enableField(AcademyPCAConstants.CUSTOM_TXT_PRINT_SHIPMENT_PICK_TICKETS2);
					enableField(AcademyPCAConstants.CUSTOM_BTN_PRINT_SHIPMENT_PICK_TICKETS_F8);
					ArrayList alButton = getControlByName(YRCDesktopUI.getCurrentPage().getShell(),AcademyPCAConstants.CUSTOM_RDBTN_PRINT_SHIPMENT_PICK_TICKETS);
					Button btnShip = (Button) alButton.get(0);
					btnShip.setSelection(true);

				}else{
					disableField(AcademyPCAConstants.CUSTOM_BTN_PRINT_SHIPMENT_PICK_TICKETS_F8);
					disableField(AcademyPCAConstants.CUSTOM_RDBTN_PRINT_SHIPMENT_PICK_TICKETS);
					disableField(AcademyPCAConstants.CUSTOM_TXT_BATCH_SIZE_PRINT);
					disableField(AcademyPCAConstants.CUSTOM_TXT_PRINT_SHIPMENT_PICK_TICKETS1);
					disableField(AcademyPCAConstants.CUSTOM_LBL_SHIPMENT_NO);
					disableField(AcademyPCAConstants.CUSTOM_TXT_PRINT_SHIPMENT_PICK_TICKETS2);
					YRCPlatformUI.enableAction(AcademyPCAConstants.CUSTOM_ACTION_SHIPMENT_PICK_TICKETS, false);

				}
				setExtentionModel("EXTN_GetShipmentList_OTNS", shipmentDoc.getDocumentElement());
			}

			//SIM to SOM Upgrade : END

			if(context.getApiName().equals(AcademyPCAConstants.GET_PICK_TICKET_NO_LIST_COMMAND)){
				//to handle the command "GetPickTicketNoList"
				AcademySIMTraceUtil.logMessage(methodName+" :: "+context.getApiName());
				Document pickTicketNoListDoc = context.getOutputXml();
				setExtentionModel(AcademyPCAConstants.EXTN_PICK_TICKET_NO_LIST_MODEL, pickTicketNoListDoc.getDocumentElement());

				//to fetch the Reprint Page Number text control in order to set a tooltip
				YRCBehavior bhvr = (YRCBehavior)YRCDesktopUI.getCurrentPage().getData("YRCBehavior");
				bhvr.getControl(AcademyPCAConstants.CUSTOM_TXT_REPRINT_PAGE_NO)
				.setToolTipText(YRCPlatformUI.getString(AcademyPCAConstants.REPRINT_PAGE_NO_TXT_TOOLTIP));
			}
			//SFS2.0 001 PrinterId

			//to handle the command "GetPickTicketNoList"
			/*
				Document getCommoncodeForPrinterIdOutDoc = context.getOutputXml();
				setExtentionModel("Extn_getCommonCodeList_PrinterID", getCommoncodeForPrinterIdOutDoc.getDocumentElement());
				AcademySIMTraceUtil.logMessage("getCommonCodeList Output" +YRCXmlUtils.getString(getCommoncodeForPrinterIdOutDoc));

			}*/
			//SFS2.0 001 PrinterId Sprint 3
			if(context.getApiName().equals("getCommonCodeList")){
				AcademySIMTraceUtil.logMessage(methodName+" :: "+context.getApiName());
				// to handle the command "getCommonCodeList"
				AcademySIMTraceUtil.logMessage("Context API: "
						+ context.getApiName());
				/*AcademySIMTraceUtil.logMessage("Get Common Code List : " + AcademyPCAConstants.GET_COMMON_CODE_LIST );*/
				Element reasonCodeOutput = context
				.getOutputXml().getDocumentElement();
				AcademySIMTraceUtil.logMessage("Api Output: \n", context
						.getOutputXml().getDocumentElement());
				Element eleCommonCode = (Element) reasonCodeOutput.getElementsByTagName("CommonCode").item(0);
				strSinglePckstnCodeValue = eleCommonCode.getAttribute("CodeValue");
				AcademySIMTraceUtil.logMessage("Single Pick Station Code Value is :"+strSinglePckstnCodeValue);
				String strCodeType = eleCommonCode.getAttribute("CodeType");
				if(strCodeType.equalsIgnoreCase("MUL_PACK_STORE"))
				{

					AcademySIMTraceUtil.logMessage("getCommonCodeList Output for MUL_PACK_STORE:" +YRCXmlUtils.getString(reasonCodeOutput));
					//Element eleMulPckStnCommonCodeList = (Element)reasonCodeOutput.getOwnerDocument();
					NodeList mulPckStnNL = reasonCodeOutput.getElementsByTagName("CommonCode");
					for(int i=0;i<mulPckStnNL.getLength();i++)
					{
						Element eleMulPckStnCommonCode = (Element)mulPckStnNL.item(i);
						AcademySIMTraceUtil.logMessage("inside for loop"+YRCXmlUtils.getString(eleMulPckStnCommonCode));
						String strCommonCodeValue = eleMulPckStnCommonCode.getAttribute("CodeValue");
						AcademySIMTraceUtil.logMessage("Multiple Pick Station Code Value is :"+strCommonCodeValue);
						String strCommonCodeShrDes = eleMulPckStnCommonCode.getAttribute("CodeShortDescription");
						AcademySIMTraceUtil.logMessage("Multiple Pick Station Description is :"+strCommonCodeShrDes);
						aMap.put(strCommonCodeValue,strCommonCodeShrDes);
						AcademySIMTraceUtil.logMessage("Hash MAP Value :"+aMap);
					} 

					/*  Iterator iterator = map.keySet().iterator();

					while(iterator.hasNext())
					{
						String key = (String) iterator.next();
						AcademySIMTraceUtil.logMessage("key ::"+ key);
						String value = map.get(key);
						AcademySIMTraceUtil.logMessage("value ::"+ value);
					}*/

					for (String key: aMap.keySet()){

						String value = aMap.get(key); 
						AcademySIMTraceUtil.logMessage(key + " " + value); 
					} 

					String shipNodeFromInput = YRCPlatformUI.getUserElement().getAttribute(AcademyPCAConstants.SHIPNODE_ATTR);
					AcademySIMTraceUtil.logMessage("ShipNode Value is::"+shipNodeFromInput);
					if(aMap.containsKey(shipNodeFromInput))
					{

						strConcatShipNode = shipNodeFromInput.concat("_SHP_NODE");
						AcademySIMTraceUtil.logMessage("CommonCodeType Is::"+strConcatShipNode);
						getCommonCodeList(strConcatShipNode);

						//set extensionmodel

					}else{
						AcademySIMTraceUtil.logMessage("CommonCodeType Is::PICK_PRINTER_ID");
						//setControlVisible("extn_PrinterIdCombo", false);
//						hideField("extn_PrinterIdCombo",this);
						GridData gridData = new GridData();
						setControlVisible("extn_PrinterIdCombo", false);
						setControlLayoutData("extn_PrinterIdCombo", gridData);
						getCommonCodeList("PICK_PRINTER_ID");
						//

					}
					AcademySIMTraceUtil.logMessage("concatenated string"+strConcatShipNode);

				}
				else if(strCodeType.equalsIgnoreCase(strConcatShipNode)){

					this.setExtentionModel("Extn_getCommonCodeList_PrinterID", reasonCodeOutput);
					AcademySIMTraceUtil.logMessage("getCommonCodeList Output With Multiple PickStation" +reasonCodeOutput);


				}

				AcademySIMTraceUtil.logMessage("The Node User has currently logged in has Single Pick Station:" +reasonCodeOutput);

				AcademySIMTraceUtil.logMessage("Editor Input::"+YRCDesktopUI.getCurrentPart().toString());
				//SFS2.0 001 PrinterId Sprint 3
				/*if(strCodeType.equalsIgnoreCase("LABELPRINTER_ID"))
				  {
					  this.setExtentionModel("Extn_Label_PrinterID_Output", reasonCodeOutput);
				  }*/

			}
			//SFS2.0 001 PrinterId
		}
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		super.handleApiCompletion(context);
	}

	/**
	 * Method to reload the current page
	 */
	private void refreshCurrentPage(){
		YRCEditorInput editorInput = (YRCEditorInput)((YRCEditorPart)YRCDesktopUI.getCurrentPart()).getEditorInput();
		AcademySIMTraceUtil.logMessage("Editor Input::"+editorInput.getInputObject());
		YRCPlatformUI.closeEditor(editorInput,AcademyPCAConstants.OOB_SHIPPING_PPT_EDITOR_ID, true);
		YRCPlatformUI.openEditor(AcademyPCAConstants.OOB_SHIPPING_PPT_EDITOR_ID, editorInput);
	}

	private static void getControlByName(Composite composite, String name, ArrayList arrayList)
	{
		Control[] childControls = composite.getChildren();

		for(int i=0; i < childControls.length; i++)
		{
			if(name.equals(childControls[i].getData("name")))
			{
				arrayList.add(childControls[i]);
			}

			if(childControls[i] instanceof Composite)
			{
				getControlByName((Composite) childControls[i], name, arrayList);
			}
		}
	}

	public ArrayList getControlByName(final Composite composite, final String name) {

		if (composite == null) {
			return null;
		}

		final ArrayList arrayList = new ArrayList();

		getControlByName(composite, name, arrayList);
		return arrayList;
	}


}
//TODO Validation required for a Radio control: extn_rdbPrintShipmentTickets
//TODO Validation required for a Radio control: extn_rdbPrintTicketsForItem
//TODO Validation required for a Button control: extn_btnPrintItemF4
//TODO Validation required for a Button control: extn_btnPrintShipmentF7
//TODO Validation required for a Button control: extn_btnPrintShipmentTicketsF8