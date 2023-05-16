package com.academy.som.searchshipment.screens;

/**
 * Created on Feb 27,2014
 *
 */

import java.util.ArrayList;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

//import com.academy.som.AcademyApplicationInitializer;
import com.academy.som.util.AcademyPCAConstants;
import com.academy.som.util.logging.AcademySIMTraceUtil;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCBaseBehavior;
import com.yantra.yfc.rcp.YRCDesktopUI;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCSharedTaskOutput;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCXmlUtils;

/**
 * Custom extension behavior class that overrides the functionality of
 * OOB class : "com.yantra.pca.ycd.rcp.tasks.common.advancedShipmentSearch.screens.YCDAdvancedShipmentSearchCriteriaPanel"
 * 
 * @author <a href="mailto:Kruthi.KM@cognizant.com">Kruthi K M</a>
 * © Copyright IBM Corp. All Rights Reserved.
 */
public class AcademySearchShipmentExtnBehavior extends YRCExtentionBehavior {

	private static String CLASSNAME = "AcademySearchShipmentExtnBehavior";

	/**
	 * This method initializes the behavior class.
	 */
	public void init() {
		//TODO: Write behavior init here.
		final String methodName = "init()";
		
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		
		//START: SHIN-6
		//Commenting as part of SHIN-6
		
		//Original logic as part of Shared Inventory 1.0
		/*Element ele = YRCPlatformUI.getUserElement();
		String strNode = ele.getAttribute("Node");
		if (!strNode.equals("001")) {
			
			hideField("extn_lblWarehouseContainer_Id",this);
			hideField("extn_txtWarehouseContainer_Id",this);
						
		}*/
		
		
		//Fetching the attribute IsSharedInventoryDC from UserNameSpace
		
		String strIsSharedInvNode = YRCPlatformUI.getUserElement()
				.getAttribute(AcademyPCAConstants.ATTR_IS_SHAREDINV_DC);

		AcademySIMTraceUtil.logMessage("IsSharedInvNode" + strIsSharedInvNode);
		
		//START: SHIN-22 commented the below logic due to the post window initializer class
		
		/*//Handling the case of UserNameSpace not having the attribute initialized
		if (YRCPlatformUI.isVoid(strIsSharedInvNode))
		{
			AcademySIMTraceUtil.logMessage(CLASSNAME+"::The value is void for strIsSharedInvNode so calling getShipNodeList");
			callGetShipNodeListAPI();
		}
		*/
		
		//If UserNameSpace was initialized with attribute, and strIsSharedInvNode is "N" then hide the field
		if (YRCPlatformUI.isVoid(strIsSharedInvNode)||AcademyPCAConstants.STRING_N.equals(strIsSharedInvNode)) {

		/* else if(AcademyPCAConstants.STRING_N.equals(strIsSharedInvNode)){
			END: SHIN-22   */
		
			AcademySIMTraceUtil.logMessage(CLASSNAME+"::The value is N for UserNameSpace attribute IsSharedInventoryDC");
			hideField("extn_lblWarehouseContainer_Id", this);
			hideField("extn_txtWarehouseContainer_Id", this);

		}
		
		// END: SHIN-6

		
		
		callGetManifestListAPI();
		callGetQueryTypeListAPI();
		callGetStatusListAPI();
		callPickTicketNoListService();
		//OMNI-6622 Changes
		callGetDocumentTypeListAPI();
	}
		
		
	public static void hideField(String fieldName, Object extnBehavior) {
		GridData gridData = new GridData();
		gridData.exclude = true;
		YRCBaseBehavior parentBehavior = (YRCBaseBehavior) extnBehavior;
		parentBehavior.setControlVisible(fieldName, false);
		parentBehavior.setControlLayoutData(fieldName, gridData);
	}
	
	/**
	 * Method to call the getManifestList API to populate the Manifest #
	 * combo with all Open Manifests in Advance Shipment search screen.
	 *
	 */
	private void callGetManifestListAPI() {
		String str = "callGetManifestListAPI()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, str);

		YRCApiContext context = new YRCApiContext();
		context.setFormId(getFormId());
		context.setApiName("GetManifestList");
		context.setInputXml(prepareInputForGetManifestList());
		callApi(context);

		AcademySIMTraceUtil.endMessage(CLASSNAME,str);
	}

	/**
	 * Method to prepare Input document for getManifestList API call
	 * to populate the Manifest # combo with Open Manifests
	 * @return Document
	 * 			<br/> - Input for populating Manifest #
	 * 			<pre>
	 * 			{@code
	 * 				<Manifest ShipNode="<<ShipNode of logged in User>>" ManifestClosedFlag="N">
	 * 					<Shipments>
	 * 						<Shipment Status="1400" StatusQryType="LE"/>
	 * 					</Shipments>
	 * 				</Manifest>
	 * 			}
	 * 			</pre>
	 */
	private Document prepareInputForGetManifestList() {
		String str = "prepareInputForGetManifestList()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, str);

		Document manifestListInputDoc = YRCXmlUtils.createDocument(AcademyPCAConstants.MANIFEST_ELEMENT);
		Element rootElement = manifestListInputDoc.getDocumentElement();
		rootElement.setAttribute(AcademyPCAConstants.SHIPNODE_ATTR, YRCPlatformUI.getUserElement()
				.getAttribute(AcademyPCAConstants.SHIPNODE_ATTR));
		rootElement.setAttribute(AcademyPCAConstants.MANIFEST_CLOSED_FLAG_ATTR, AcademyPCAConstants.STRING_N);
		Element shipmentsElement = manifestListInputDoc
				.createElement(AcademyPCAConstants.SHIPMENTS_ELEMENT);
		Element shipmentElement = manifestListInputDoc
				.createElement(AcademyPCAConstants.SHIPMENT_ELEMENT);
		shipmentElement.setAttribute(AcademyPCAConstants.STATUS_ATTR, AcademyPCAConstants.STATUS_SHIPPED_VAL);
		shipmentElement.setAttribute(AcademyPCAConstants.STATUS_QRY_TYPE_ATTR, AcademyPCAConstants.STATUS_QRY_TYPE_LE_VAL);
		shipmentsElement.appendChild(shipmentElement);
		rootElement.appendChild(shipmentsElement);

		AcademySIMTraceUtil.endMessage(CLASSNAME, str);
		return manifestListInputDoc;
	}

	/**
	 * Method to call OOB "getStatusList" API to populate the
	 * Shipment Status combo in Advance
	 * Shipment search screen
	 *
	 */
	private void callGetStatusListAPI() {
		String str = "callGetStatusListAPI()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, str);

		YRCApiContext context = new YRCApiContext();
		context.setFormId(getFormId());
		context.setApiName(AcademyPCAConstants.GET_STATUS_LIST_COMMAND);
		context.setInputXml(prepareInputForGetStatusList());
		callApi(context);

		AcademySIMTraceUtil.endMessage(CLASSNAME, str);
	}

	/**
	 * Method to prepare input to OOB "getStatusList" API 
	 * @return Document
	 * 			<br/> - Input for populating Shipment Status
	 * 			<pre>
	 * 			{@code
	 * 				<Status CallingOrganizationCode="Academy_Direct" DocumentType="0001" ProcessTypeKey="ORDER_DELIVERY">
	 * 					<ComplexQuery>
	 * 						<Or>
	 *                        <Exp Name="Status" Value="1100.70.06.10"/>
	 *                        <Exp Name="Status" Value="1100.70.06.30"/>
	 *                        <Exp Name="Status" Value="9000"/>
	 *                      </Or>
	 * 					</ComplexQuery>
	 * 				</Status>
	 * 			}
	 * 			</pre>
	 */
	private Document prepareInputForGetStatusList() {
		String str = "prepareInputForGetStatusList()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, str);

		Document statustListInputDoc = YRCXmlUtils.createDocument(AcademyPCAConstants.STATUS_ATTR);
		Element eleStatusListInput = statustListInputDoc.getDocumentElement();
		eleStatusListInput.setAttribute(AcademyPCAConstants.CALLING_ORG_CODE,
				AcademyPCAConstants.ENTERPRISE_CODE);
		eleStatusListInput.setAttribute(AcademyPCAConstants.DOCUMENT_TYPE_ATTR, AcademyPCAConstants.DOCUMENT_TYPE_ATTR_VAL);
		eleStatusListInput.setAttribute(AcademyPCAConstants.PROCESS_TYPE_KEY_ATTR, AcademyPCAConstants.PROCESS_TYPE_KEY_VAL);
		Element complexQueryEle = YRCXmlUtils.createChild(eleStatusListInput,
				AcademyPCAConstants.COMPLEX_QRY_ELEMENT);
		Element orEle = YRCXmlUtils.createChild(complexQueryEle, AcademyPCAConstants.COMPLEX_OR_ELEMENT);

		Element expEle = YRCXmlUtils.createChild(orEle, AcademyPCAConstants.COMPLEX_EXP_ELEMENT);
		expEle.setAttribute(AcademyPCAConstants.ATTR_NAME, AcademyPCAConstants.STATUS_ATTR);
		expEle.setAttribute(AcademyPCAConstants.ATTR_VALUE, AcademyPCAConstants.STATUS_READY_FOR_BACKROOM_PICK_VAL);
		Element expEle1 = YRCXmlUtils.createChild(orEle, AcademyPCAConstants.COMPLEX_EXP_ELEMENT);
		expEle1.setAttribute(AcademyPCAConstants.ATTR_NAME, AcademyPCAConstants.STATUS_ATTR);
		expEle1.setAttribute(AcademyPCAConstants.ATTR_VALUE, AcademyPCAConstants.STATUS_READY_FOR_CUSTOMER_VAL);
		Element expEle2 = YRCXmlUtils.createChild(orEle, AcademyPCAConstants.COMPLEX_EXP_ELEMENT);
		expEle2.setAttribute(AcademyPCAConstants.ATTR_NAME, AcademyPCAConstants.STATUS_ATTR);
		expEle2.setAttribute(AcademyPCAConstants.ATTR_VALUE,AcademyPCAConstants.STATUS_SHIPMENT_CANCELLED_VAL);

		orEle.appendChild(expEle);
		orEle.appendChild(expEle1);
		orEle.appendChild(expEle2);
		complexQueryEle.appendChild(orEle);
		eleStatusListInput.appendChild(complexQueryEle);
		AcademySIMTraceUtil.endMessage(CLASSNAME, str);
		return statustListInputDoc;

	}

	/**
	 *	Method to call custom view service to populate the
	 *  Pick ticket Number combo generated for reprint in 
	 *  Shipment search screen
	 *
	 */
	private void callPickTicketNoListService() {
		String str = "callPickTicketNoListService()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, str);

		YRCApiContext context = new YRCApiContext();
		context.setFormId(getFormId());
		context.setApiName(AcademyPCAConstants.GET_PICK_TICKET_NO_LIST_COMMAND);
		context.setInputXml(prepareInputForPickTicketNo());
		callApi(context);

		AcademySIMTraceUtil.endMessage(CLASSNAME, str);
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
		String str = "prepareInputForPickTicketNo()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, str);

		Document pickTicketNoInputDoc = YRCXmlUtils
				.createDocument(AcademyPCAConstants.SFS_PICK_TICKET_NO_ELEMENT);
		Element rootElement = pickTicketNoInputDoc.getDocumentElement();
		rootElement.setAttribute(AcademyPCAConstants.PICK_TICKET_NO_ATTR, AcademyPCAConstants.EMPTY_STRING);
		rootElement.setAttribute(AcademyPCAConstants.SHIPNODE_ATTR, YRCPlatformUI.getUserElement()
				.getAttribute(AcademyPCAConstants.SHIPNODE_ATTR));
		Element orderByElement = pickTicketNoInputDoc.createElement(AcademyPCAConstants.ORDER_BY_ELEMENT);
		Element attrElement = pickTicketNoInputDoc.createElement(AcademyPCAConstants.ATTRIBUTE_ELEMENT);
		attrElement.setAttribute(AcademyPCAConstants.NAME_ATTR, AcademyPCAConstants.PICK_TICKET_NO_ATTR);
		attrElement.setAttribute(AcademyPCAConstants.DESCENDING_ATTR, AcademyPCAConstants.STRING_Y);
		orderByElement.appendChild(attrElement);
		rootElement.appendChild(orderByElement);
		
		AcademySIMTraceUtil.endMessage(CLASSNAME, str);
		return pickTicketNoInputDoc;
	}

	public void callGetQueryTypeListAPI() {
		String str = "callGetQueryTypeListAPI()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, str);
		Document docQueryType = YRCXmlUtils.createDocument(AcademyPCAConstants.ELE_QUERY_TYPE);
		YRCApiContext context = new YRCApiContext();
		context.setApiName(AcademyPCAConstants.GET_QUERY_TYPELIST_COMMAND);
		context.setFormId(getFormId());
		context.setInputXml(docQueryType);
		callApi(context);
		AcademySIMTraceUtil.endMessage(CLASSNAME, str);
	}

	/**
	 * Method for modifying the Input to OOB API Calls
	 * @param apiContext
	 *          <br/> - The context in which API is called.
	 * @return boolean.
	 * 			<br/> - return false to stop the call
	 */
	public boolean preCommand(YRCApiContext context) {
		final String methodName = "preCommand(apiContext)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		if (context.getApiName().equals(AcademyPCAConstants.GET_SHIPMENT_LIST_COMMAND)) {
			//to modify the input to OOB getShipmentList command (called on click of Search button)

			Document docInputGetShipmentList = context.getInputXml();
			manageGetShipmentListInput(docInputGetShipmentList);
			context.setInputXml(docInputGetShipmentList);
			AcademySIMTraceUtil.logMessage("Modified Input for "
					+ context.getApiName() + " command: \n",
					docInputGetShipmentList.getDocumentElement());
		}
		return super.preCommand(context);
	}

	/**
	 * Method to modify the Input document to OOB getShipmentList API call
	 * @param docInputGetShipmentList
	 * 			<br/> - Input Document to getShipmentList API
	 * 			<pre>
	 * 			{@code
	 * 				<Shipment MaximumRecords="31" RequestedShipmentDateQryType="BETWEEN" 
	 * 					ShipNode="<<ShipNode of logged in User>>">
	 * 					<OrderBy>
	 * 						<Attribute Desc="N" Name="ShipmentNo"/>
	 * 					</OrderBy>
	 * 				</Shipment>
	 * 			}
	 * 			</pre>
	 * 			<br/> - Modified Input Document to getShipmentList API
	 * 			<pre>
	 * 			{@code
	 * 				<Shipment MaximumRecords="31" PickticketNo=""
	 * 					RequestedShipmentDateQryType="BETWEEN" ShipNode="<<ShipNode of logged in User>>"
	 * 					Status="1100.70.06.10">
	 * 					<OrderBy>
	 * 						<Attribute Name="Extn_ExtnSCACPriority"/>
	 * 						<Attribute Name="ShipmentNo"/>
	 * 					</OrderBy>
	 * 				</Shipment>
	 * 			}
	 * 			</pre>
	 * 			<br/> - MaximumRecords attribute will be blank for getShipmentList command
	 */
	private void manageGetShipmentListInput(Document docInputGetShipmentList) {
		final String methodName = "manageGetShipmentListInput(docInputGetShipmentList)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		String sStatus = getFieldValue(AcademyPCAConstants.CUSTOM_CMB_STATUS);
		String batchNoSelected = getFieldValue(AcademyPCAConstants.CUSTOM_CMB_BATCH_GRP);
		Element rootElement = docInputGetShipmentList.getDocumentElement();
		rootElement.setAttribute(AcademyPCAConstants.STATUS_ATTR, sStatus);
		rootElement.setAttribute(AcademyPCAConstants.PICK_TICKET_NO_ATTR,
				batchNoSelected);
		Element orderByElement = (Element) rootElement.getElementsByTagName(
				AcademyPCAConstants.ORDER_BY_ELEMENT).item(0);
		Element oldAttrEle = (Element) orderByElement.getElementsByTagName(
				AcademyPCAConstants.ATTRIBUTE_ELEMENT).item(0);
		orderByElement.removeChild(oldAttrEle);
		Element attrElement1 = docInputGetShipmentList
				.createElement(AcademyPCAConstants.ATTRIBUTE_ELEMENT);
		attrElement1.setAttribute(AcademyPCAConstants.NAME_ATTR,
				AcademyPCAConstants.EXTN_SCAC_PRIORITY_ATTR);
		Element attrElement2 = docInputGetShipmentList
				.createElement(AcademyPCAConstants.ATTRIBUTE_ELEMENT);
		attrElement2.setAttribute(AcademyPCAConstants.NAME_ATTR,
				AcademyPCAConstants.SHIPMENT_NO_ATTR);
		orderByElement.appendChild(attrElement1);
		orderByElement.appendChild(attrElement2);

		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}

	/**
	 * Superclass method called to modify the OOB screen models when set
	 * @param nameSpace
	 * 			<br/> - name of the OOB screen model
	 */
	public void postSetModel(String nameSpace) {
		final String methodName = "postSetModel(nameSpace)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		if (nameSpace.equals(AcademyPCAConstants.GET_SHIPMENT_LIST_INP_OOB_MODEL)) {
			//when OOB getShipmentList_input screen model is set
			AcademySIMTraceUtil.logMessage(methodName + " :: NameSpace "
					+ nameSpace + " :: BEGIN");
			//To Uncheck the Pick Up radio button.
			setFieldValue(AcademyPCAConstants.OOB_BTN_PICKUP, false);

			//gets the control of Pick Up radio button
			ArrayList aBtnPick = getControlByName(YRCDesktopUI.getCurrentPage()
					.getShell(),AcademyPCAConstants.OOB_BTN_PICKUP);
			Button btnPick = (Button) aBtnPick.get(0);
			//greys out the Pick Up option
			btnPick.setEnabled(false);
			//To check the Shipping radio button on screen load.
			setFieldValue(AcademyPCAConstants.OOB_BTN_SHIPPING, true);
			//reloads the screen with search options available for Shipping
			this.relayoutScreen();
			AcademySIMTraceUtil.logMessage(methodName + " :: NameSpace "
					+ nameSpace + " :: END");

		}
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}

	public ArrayList getControlByName(final Composite composite,
			final String name) {

		if (composite == null) {
			return null;
		}

		final ArrayList arrayList = new ArrayList();

		getControlByName(composite, name, arrayList);
		return arrayList;
	}

	private static void getControlByName(Composite composite, String name,
			ArrayList arrayList) {
		Control[] childControls = composite.getChildren();

		for (int i = 0; i < childControls.length; i++) {
			if (name.equals(childControls[i].getData("name"))) {
				arrayList.add(childControls[i]);
			}

			if (childControls[i] instanceof Composite) {
				getControlByName((Composite) childControls[i], name, arrayList);
			}
		}
	}

	/**
	 * Method for validating the text box.
	 */
	public YRCValidationResponse validateTextField(String fieldName,
			String fieldValue) {
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
	 * Method called when a button is clicked.
	 */
	public YRCValidationResponse validateButtonClick(String fieldName) {
		// TODO Validation required for the following controls.
		final String methodName = "validateButtonClick(fieldName)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		if (fieldName.equals(AcademyPCAConstants.EXTN_LABEL_LOOPUP)) {
			//to fetch the form ID in which the ItemSearch shared task is to be opened.
			Composite comp1 = YRCDesktopUI.getCurrentPage().getShell();
			//launch the ItemSearch shared task.
			YRCSharedTaskOutput sharedTaskOutput = YRCPlatformUI
					.launchSharedTask(comp1, AcademyPCAConstants.OOB_ADV_ITEM_SEARCH_SHARED_TASK);
			Element eleItemList = sharedTaskOutput.getOutput();
			String strItemID = eleItemList.getAttribute(AcademyPCAConstants.ITEM_ID_ATTR);
			//sets the ItemID field with the one selected in the ItemSearch pop up.
			setFieldValue(AcademyPCAConstants.EXTN_TXT_ITEMID, strItemID);

		}
		if (fieldName.equals("extn_btnReset")) {
			resetFields();
		}
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		// TODO Create and return a response.
		return super.validateButtonClick(fieldName);
	}

	/**
	 * This method resets all the Fields specified to blank
	 * when the user clicks on "Reset F9" button or hits the
	 * hot key F9
	 */
	public void resetFields() {
		String str = "resetFields())";
		AcademySIMTraceUtil.startMessage(CLASSNAME, str);
		setFieldValue("extn_cmbQueryTypeShipNo", "");
		setFieldValue("txtShipmentNo", "");
		setFieldValue("extn_cmbQueryTypeOrderNo", "");
		setFieldValue("txtOrderNo", "");
		setFieldValue("extn_cmbManifestNo", "");
		setFieldValue("txtShipmentNo", "");
		setFieldValue("extn_cmbQueryTypeItemID", "");
		setFieldValue("extn_txtItemID", "");
		setFieldValue("extn_cmbBatchGroupPrint", "");
		AcademySIMTraceUtil.endMessage(CLASSNAME, str);
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
	public YRCExtendedTableBindingData getExtendedTableBindingData(
			String tableName, ArrayList tableColumnNames) {
		// Create and return the binding data definition for the table.

		// The defualt super implementation does nothing.
		return super.getExtendedTableBindingData(tableName, tableColumnNames);
	}
	
	//START: SHIN-22 commented the below logic due to the post window initializer class
	
	/*// This callGetShipNodeListAPI() will invoke the getShipNodeList API

	public void callGetShipNodeListAPI() {
		String strMethodName = "callGetShipNodeListAPI()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, strMethodName);
		
		YRCApiContext context = new YRCApiContext();
		context.setFormId(getFormId());
		context.setApiName(AcademyPCAConstants.GET_SHIP_NODE_LIST_COMMAND);
		context.setInputXml(prepareInputForGetShipNodeList());
		YRCPlatformUI.callApi(context, this);
		
		AcademySIMTraceUtil.endMessage(CLASSNAME,strMethodName);
	}
	// The method will prepare the input for getshipNodeList API
	public Document prepareInputForGetShipNodeList() {
		String str = "prepareInputForGetShipNodeList()";
		
		AcademySIMTraceUtil.startMessage(CLASSNAME, str);
		Document getShipNodeListInputDoc = YRCXmlUtils
				.createDocument(AcademyPCAConstants.SHIPNODE_ATTR);
		Element eleRootElement = getShipNodeListInputDoc.getDocumentElement();
		eleRootElement.setAttribute(AcademyPCAConstants.SHIPNODE_ATTR,
				YRCPlatformUI.getUserElement().getAttribute(
						AcademyPCAConstants.ATTR_NODE));
		
		AcademySIMTraceUtil.endMessage(CLASSNAME,str);
		
		return getShipNodeListInputDoc;
		
		
	}*/
	
	//END: SHIN-22

	/**
	 * Superclass method to set the screen models for the custom API Calls
	 * and to handle failure of API calls
	 * @param context
	 * 			<br/> - the context in which the API is called
	 */
	public void handleApiCompletion(YRCApiContext context) {
		final String methodName = "handleApiCompletion(context)";
		
		//START: SHIN-22 commented the below logic due to the post window initializer class
		
		/*//START: SHIN-6
		Document shipNodeListDoc;
		Element eleshipNode;
		String strNodeType="";
		//END: SHIN-6*/	
		
		//END: SHIN-22
		
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		if (context.getInvokeAPIStatus() < 0) {
			//to handle API call failures
			AcademySIMTraceUtil.logMessage(context.getApiName()
					+ " call Failed");
			AcademySIMTraceUtil.logMessage("Error Output: \n", context
					.getOutputXml().getDocumentElement());
		} else {
			if (AcademyPCAConstants.GET_QUERY_TYPELIST_COMMAND.equals(context.getApiName())) {
				AcademySIMTraceUtil
				.logMessage("handleApiCompletion(context) :: "
						+ context.getApiName());
				Element indoc = context.getOutputXml().getDocumentElement();
				setExtentionModel(AcademyPCAConstants.EXTN_GET_QUERY_TYPE_LIST_MODEL, indoc);
				setFocus(AcademyPCAConstants.STR_SHIPMENT_NO);//STL-1718 It sets the focus on Shipment # text field on load of screen.
			}
			if (context.getApiName().equals(AcademyPCAConstants.GET_MANIFEST_LIST_COMMAND)) {
				AcademySIMTraceUtil
						.logMessage("handleApiCompletion(context) :: "
								+ context.getApiName());
				Document manifestListDoc = context.getOutputXml();
				setExtentionModel(AcademyPCAConstants.EXTN_OPEN_MANIFEST_LIST_MODEL,
						manifestListDoc.getDocumentElement());
			}
			if (context.getApiName().equals(AcademyPCAConstants.GET_STATUS_LIST_COMMAND)) {
				AcademySIMTraceUtil
						.logMessage("handleApiCompletion(context) :: "
								+ context.getApiName());
				Document statusListDoc = context.getOutputXml();
				setExtentionModel(AcademyPCAConstants.EXTN_GET_STATUS_LIST_MODEL, statusListDoc
						.getDocumentElement());
				setFieldValue(AcademyPCAConstants.CUSTOM_CMB_STATUS, AcademyPCAConstants.STATUS_READY_FOR_BACKROOM_PICK);

			}
			if (context.getApiName().equals(AcademyPCAConstants.GET_PICK_TICKET_NO_LIST_COMMAND)) {
				AcademySIMTraceUtil
						.logMessage("handleApiCompletion(context) :: "
								+ context.getApiName());
				Document pickTicketNoListDoc = context.getOutputXml();
				setExtentionModel(AcademyPCAConstants.EXTN_PICK_TICKET_NO_LIST_MODEL,
						pickTicketNoListDoc.getDocumentElement());
			}
					
			//START: SHIN-22 commented the below logic due to the post window initializer class
			
			/*//START: SHIN-6
			if (context.getApiName().equals(
					AcademyPCAConstants.GET_SHIP_NODE_LIST_COMMAND)) {
				AcademySIMTraceUtil
						.logMessage("handleApiCompletion(context) :: "
								+ context.getApiName());
				
				shipNodeListDoc = context.getOutputXml();
				eleshipNode = (Element) shipNodeListDoc
						.getElementsByTagName(AcademyPCAConstants.SHIPNODE_ATTR)
						.item(0);
				strNodeType = eleshipNode.getAttribute(AcademyPCAConstants.ATTR_NODE_TYPE);
			
				if (AcademyPCAConstants.ATTR_VAL_SHAREDINV_DC.equals(strNodeType)){
					AcademySIMTraceUtil.logMessage("The value is SharedInventoryDC for ShipNode NodeType");
				} else {
					AcademySIMTraceUtil.logMessage("The value is NOT SharedInventoryDC for ShipNode NodeType");
					hideField("extn_lblWarehouseContainer_Id", this);
					hideField("extn_txtWarehouseContainer_Id", this);
				}
			}
			//END: SHIN-6*/	
			
			//END: SHIN-22
		}
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
		super.handleApiCompletion(context);
	}

	/**
	 * This method is added as part of OMNI-6622. 
	 * This method enables the search using sales order and transfer order for STS
	 * @param context
	 * 	
	 */
	public void callGetDocumentTypeListAPI() {
		String str = "getDocumentTypeListAPI()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, str);
		Document docDocumentPrarams = YRCXmlUtils.createDocument(AcademyPCAConstants.ELE_DOCUMENT_PARAMS_LIST);
		
		Element eleShipFromStore = docDocumentPrarams.createElement(AcademyPCAConstants.ELE_DOCUMENT_PARAMS);
		eleShipFromStore.setAttribute(AcademyPCAConstants.DOCUMENT_TYPE_ATTR, AcademyPCAConstants.STR_SALES_ORDER_DOC_TYPE);
		eleShipFromStore.setAttribute(AcademyPCAConstants.STATS_DESC_ATTR, AcademyPCAConstants.STR_SHIP_TO_HOME);
		
		Element eleShipToStore = docDocumentPrarams.createElement(AcademyPCAConstants.ELE_DOCUMENT_PARAMS);
		eleShipToStore.setAttribute(AcademyPCAConstants.DOCUMENT_TYPE_ATTR, AcademyPCAConstants.STR_TRANSFER_ORDER_DOC_TYPE);
		eleShipToStore.setAttribute(AcademyPCAConstants.STATS_DESC_ATTR, AcademyPCAConstants.STR_SHIP_TO_STORE);
		
		docDocumentPrarams.getDocumentElement().appendChild(eleShipFromStore);
		docDocumentPrarams.getDocumentElement().appendChild(eleShipToStore);
		
		setExtentionModel(AcademyPCAConstants.MODEL_DOCUMENT_TYPE_OUTPUT, docDocumentPrarams.getDocumentElement());
		setFieldValue(AcademyPCAConstants.LABEL_EXTN_DOCUMENT_TYPE, AcademyPCAConstants.STR_SHIP_TO_HOME);
		AcademySIMTraceUtil.endMessage(CLASSNAME, str);
	}
	
	
}
