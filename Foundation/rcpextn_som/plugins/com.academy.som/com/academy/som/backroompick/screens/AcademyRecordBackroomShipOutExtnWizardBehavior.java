package com.academy.som.backroompick.screens;

import com.academy.som.util.AcademyPCAConstants;
import com.academy.som.util.logging.AcademySIMTraceUtil;
import com.sun.xml.internal.ws.util.xml.XmlUtil;
import com.yantra.yfc.rcp.IYRCCellModifier;
import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.IYRCTableColumnTextProvider;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCBaseBehavior;
import com.yantra.yfc.rcp.YRCBehavior;
import com.yantra.yfc.rcp.YRCCellModifier;
import com.yantra.yfc.rcp.YRCDesktopUI;
import com.yantra.yfc.rcp.YRCDialog;
import com.yantra.yfc.rcp.YRCEditorInput;
import com.yantra.yfc.rcp.YRCEditorPart;
import com.yantra.yfc.rcp.YRCEvent;
import com.yantra.yfc.rcp.YRCExtendedCellModifier;
import com.yantra.yfc.rcp.YRCExtendedTableBindingData;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import com.yantra.yfc.rcp.YRCFormatResponse;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCTableBindingData;
import com.yantra.yfc.rcp.YRCTblClmBindingData;
import com.yantra.yfc.rcp.YRCUIUtils;
import com.yantra.yfc.rcp.YRCValidationResponse;
import com.yantra.yfc.rcp.YRCWizard;
import com.yantra.yfc.rcp.YRCWizardExtensionBehavior;
import com.yantra.yfc.rcp.YRCXPathUtils;
import com.yantra.yfc.rcp.YRCXmlUtils;
import com.yantra.yfc.rcp.internal.YRCApiCaller;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.xpath.XPathConstants;

import org.eclipse.jface.viewers.deferred.SetModel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.yantra.yfc.rcp.YRCConstants;
import java.awt.Button;
import org.eclipse.swt.widgets.Control;

public class AcademyRecordBackroomShipOutExtnWizardBehavior extends
		YRCWizardExtensionBehavior {
	private String weight;

	private String containerType;

	private static String CLASSNAME = "AcademyRecordBackroomShipOutExtnWizardBehavior";

	private String pageBeingShown;

	private boolean multiBoxFlag = false;

	public boolean completePickDone;

	private boolean noPickDone;

	private boolean shipDtlsModelSet;

	private String strPackslipPrinterId;

	private String strSinglePckstnCodeValue;

	private String strConcatShipNode;

	private String strCommonComboValue;

	private String strComboboxValue;

	HashMap<String, String> packMap = new HashMap<String, String>();

	String strSelectedRadioButton = "";

	String strSelectedButton = "";

	String strContainerId = "";

	int thresholdQuantity = 0;

	// START STL-1678 enable UPC base search for Backroom pick
	String strIsUPCBaseBPSearchEnable = YRCPlatformUI
			.getUserElement()
			.getAttribute(AcademyPCAConstants.ATTR_IS_UPC_BASE_BP_SEARCH_ENABLE);

	// END STL-1678 enable UPC base search for Backroom pick

	// START: SHIN-6
	String strIsSharedInvNode = YRCPlatformUI.getUserElement().getAttribute(
			AcademyPCAConstants.ATTR_IS_SHAREDINV_DC);

	/*
	 * START: SHIN-22 commented the below logic due to the post window
	 * initializer class String strInvocationFlag=""; END: SHIN-22
	 */

	String strExtnExeterContainerId = "";

	// END: SHIN-6

	// STL-733
	private String strShipmentType = null;

	// STL-733
	// START : STL-1300 : validate ExtnExeterContainerId. Block backroom pick if
	// ExtnExeterContainerId not received.
	private boolean isExtnExeterContainerIdNotReceived = false;

	private String XPATH_EXTN_EXETER_CONTAINER_ID = "/Shipment/Extn/@ExtnExeterContainerId";

	// END : STL-1300 : validate ExtnExeterContainerId. Block backroom pick if
	// ExtnExeterContainerId not received.
	// STL-679: Making Reason Code as mandatory in case of Inventory Shortage:
	// START
	String strShortpickReasonCode = "";

	// STL-679: Making Reason Code as mandatory in case of Inventory Shortage:
	// END

	// START WN-2980 GC Activation and fulfillment for SI DCs
	private boolean isSerialScannedRequired = false;

	String strSetFocusToField = "";

	String strSerialScanForShipmentLineKey = "";

	// END WN-2980 GC Activation and fulfillment for SI DCs

	double thresholdPickQuantity = 0;

	HashMap<String, String> scannedItemMap = new HashMap<String, String>();
	
	//OMNI-7980
	boolean isSTSAmmoOrHazmatShipment = false;
	//OMNI-7980
	
	//Start - OMNI-65686 Pack Station - Auto select container size - RCP SOM
	boolean showVendorPackage = false;
	//End - OMNI-65686 Pack Station - Auto select container size - RCP SOM
	
	public void init() {
		// Start - Lines included as part SOM client 9.3 upgrade-Defect 9
		YRCEditorInput yRCEditorInput = (YRCEditorInput)((YRCEditorPart)YRCDesktopUI.getCurrentPart()).getEditorInput();
		yRCEditorInput.getXml().setAttribute("BackroomPickRequired", "Y");
		// End - Lines included as part SOM client 9.3 upgrade-Defect 9
	}

	public String getExtnNextPage(String currentPageId) {
		return null;
	}

	public IYRCComposite createPage(String pageIdToBeShown) {
		return null;
	}

	public void pageBeingDisposed(String pageToBeDisposed) {
		String str1 = "pageBeingDisposed(pageToBeDisposed)";
		AcademySIMTraceUtil.startMessage(CLASSNAME,
				"pageBeingDisposed(pageToBeDisposed)");

		// Changed the Form IDs since the FormID's are different in SOM.
		if (pageToBeDisposed
				.equals("com.yantra.pca.ycd.rcp.tasks.backroompick.wizardpages.YCDBackroomPickShortageReasons")) {
			YRCPlatformUI.enableAction("BACKROOM_PICK_NEXT_ACTION", true);
		}
		// STL-1718 Begin
		/*
		 * This also fix the OOB bug. When user clicks on "Previous" button of
		 * Backroom pick screen and same shipment search will not work. Below
		 * code is for "Previous" button of backroom pick screen
		 */
		if (pageToBeDisposed
				.equals(AcademyPCAConstants.STR_BACKROOM_PICK_WIZARD)) {
			refreshBackroomSearchScreen();
		}
		// STL-1718 End
		AcademySIMTraceUtil.endMessage(CLASSNAME,
				"pageBeingDisposed(pageToBeDisposed)");
	}

	public void initPage(String pageBeingShown) {
		String str1 = "initPage(pageBeingShown)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, "initPage(pageBeingShown)");

		multiBoxFlag = false;
		setControlEditable("tblBackroomPickTable", true);
		setControlEditable("extn_customPickQuantity", true);
		/*
		 * START: SHIN-22 commented the below logic due to the post window
		 * initializer class strInvocationFlag=""; END: SHIN-22
		 */

		isExtnExeterContainerIdNotReceived = false;
		strIsSharedInvNode = YRCPlatformUI.getUserElement().getAttribute(
				AcademyPCAConstants.ATTR_IS_SHAREDINV_DC);

		// START WN-2980 GC Activation and fulfillment for SI DCs
		if (AcademyPCAConstants.STRING_Y.equals(strIsSharedInvNode)) {
			disableField(AcademyPCAConstants.CUSTOM_TXT_SERIALNO_SCAN);
			disableField(AcademyPCAConstants.CUSTOM_TXT_LAST_SCANNED_SERIALNO);
		} else {
			hideField(AcademyPCAConstants.CUSTOM_LBL_SERIALNO, this);
			hideField(AcademyPCAConstants.CUSTOM_TXT_SERIALNO_SCAN, this);
			hideField(AcademyPCAConstants.CUSTOM_LBL_LAST_SCANNED_SERIALNO,
					this);
			hideField(AcademyPCAConstants.CUSTOM_TXT_LAST_SCANNED_SERIALNO,
					this);
			// EFP-7 : Start
			hideField("extn_UnusedTxtBox_07", this);
			hideField("extn_UnusedTxtBox_08", this);
			hideField("extn_UnusedTxtBox_09", this);
			hideField("extn_UnusedTxtBox_10", this);
			hideField("extn_UnusedTxtBox_11", this);
			hideField("extn_UnusedTxtBox_12", this);
			// EFP-7 : End
		}
		// END WN-2980 GC Activation and fulfillment for SI DCs

		AcademySIMTraceUtil.logMessage(CLASSNAME + ":" + pageBeingShown
				+ "::IsSharedInvNode" + strIsSharedInvNode);

        //Jira: 65686 Pack Station - Auto select container size - RCP SOM
         getCommonCodeList(AcademyPCAConstants.TGL_RCP_WEB_SOM_UI);
		 //End :65686 Pack Station - Auto select container size - RCP SOM
		
		// START STL-1678 enable UPC base search for Backroom pick
		AcademySIMTraceUtil.logMessage(CLASSNAME + ":" + pageBeingShown
				+ "::strIsUPCBaseBPSearchEnable :: "
				+ strIsUPCBaseBPSearchEnable);
		// END STL-1678 enable UPC base search for Backroom pick

		// Changed the Form IDs since the FormID's are different in SOM.
		if ((pageBeingShown
				.equals("com.yantra.pca.ycd.rcp.tasks.backroompick.wizards.YCDBackroompickWizard"))
				|| (pageBeingShown
						.equals("com.yantra.pca.ycd.rcp.tasks.backroompick.wizardpages.YCDBackroomPick"))
				|| (pageBeingShown
						.equals("com.yantra.pca.ycd.rcp.tasks.backroompick.wizardpages.YCDBackroomPickShipmentSearch"))) {
			this.pageBeingShown = pageBeingShown;
			AcademySIMTraceUtil.logMessage("for pageBeingShown: "
					+ pageBeingShown + " :: START");

			YRCPlatformUI.enableAction("BACKROOM_PICK_NEXT_ACTION", true);
			disableUnusedTextBoxes();
			callApiForContainerTypes();
			// EFP-7 : Start
			getContainerPriority();
			getContainerTheshold();
			// EFP-7 : End
			AcademySIMTraceUtil.logMessage("for pageBeingShown: "
					+ pageBeingShown + " :: END");
		} else {
			YRCPlatformUI.enableAction("BACKROOM_PICK_NEXT_ACTION", false);
		}
		// Start-STL-950 Added for Shared Inventory Project
		if (pageBeingShown
				.equals("com.yantra.pca.ycd.rcp.tasks.backroompick.wizardpages.YCDBackroomPickShipmentSearch")) {
				//OMNI-66084 BEGIN
			addEventHandler("extn_txtWarehouseContId",31);
			//OMNI-66084 END

			// START: SHIN-6
			// Commenting as part of SHIN-6

			/*
			 * Element ele = YRCPlatformUI.getUserElement(); String strNode =
			 * ele.getAttribute("Node"); if (!strNode.equals("001")) {
			 * setControlVisible("extn_lblWarehouseContId", false);
			 * setControlVisible("extn_txtWarehouseContId", false); }
			 */

			// START: SHIN-22 commented the below logic due to the post window
			// initializer class
			// Handling the case of UserNameSpace not having the attribute
			// initialized
			/*
			 * if (YRCPlatformUI.isVoid(strIsSharedInvNode)) {
			 * AcademySIMTraceUtil.logMessage(CLASSNAME+":"+pageBeingShown+"::The
			 * value is void for strIsSharedInvNode so calling
			 * getShipNodeList");
			 * strInvocationFlag=AcademyPCAConstants.STR_VAL_FIRST;
			 * callGetShipNodeListAPI(); }
			 */
			// END: SHIN-22
			// If UserNameSpace was initialized with attribute, and
			// strIsSharedInvNode is "N" then hide the field
			// START: SHIN-22 Uncomment below line having void check and comment
			// the line without void check
			if (YRCPlatformUI.isVoid(strIsSharedInvNode)
					|| AcademyPCAConstants.STRING_N.equals(strIsSharedInvNode)) {
				// else
				// if(AcademyPCAConstants.STRING_N.equals(strIsSharedInvNode)){
				// END: SHIN-22

				AcademySIMTraceUtil
						.logMessage(CLASSNAME
								+ ":"
								+ pageBeingShown
								+ "::The value is N for UserNameSpace attribute IsSharedInventoryDC");
				setControlVisible("extn_lblWarehouseContId", false);
				setControlVisible("extn_txtWarehouseContId", false);
			}
			// START STL-1678 enable UPC base search for Backroom pick
			if (AcademyPCAConstants.STRING_Y.equals(strIsUPCBaseBPSearchEnable)) {
				setControlVisible(AcademyPCAConstants.STR_EXTN_LBL_UPC_CODE,
						true);
				setControlVisible(AcademyPCAConstants.STR_EXTN_TXT_UPC_CODE,
						true);
			}// END STL-1678 enable UPC base search for Backroom pick

			// END: SHIN-6
		}
		// End-STL-950 Added for Shared Inventory Project
		// Changed the Form IDs since the FormID's are different in SOM.
		if (pageBeingShown
				.equals("com.yantra.pca.ycd.rcp.tasks.backroompick.wizardpages.YCDBackroomPick")) {
			this.shipDtlsModelSet = false;

			addEventHandler("txtItemID", 31);
			addEventHandler("text2", 31);
			addEventHandler("extn_TxtContnrBarcde", 31);
			// start OMNI-443 Enable ENTER key for SerialNo field
			addEventHandler("extn_txtSerialNo", 31);
			// end OMNI-443 Enable ENTER key for SerialNo field
			getCommonCodeList("MUL_PACK_STORE");
			getCommonCodeList("PICK_QTY_THRESHOLD");
			
			//Start - OMNI-65686 Pack Station - Auto select container size - RCP SOM
	         getCommonCodeList(AcademyPCAConstants.TGL_RCP_WEB_SOM_UI);
	        //End - OMNI-65686 Pack Station - Auto select container size - RCP SOM

			// Start-Added as part of Shared Inventory
			// START: SHIN-6
			// Commenting as part of SHIN-6

			/*
			 * Element ele = YRCPlatformUI.getUserElement(); String strNode =
			 * ele.getAttribute("Node"); if (!strNode.equals("001")) {
			 * hideField("extn_lblWarehouseCont_Id_ShipDtls", this);
			 * hideField("extn_txtWarehouseCont_Id_ShipDtls", this); }
			 */
			// End-Added as part of Shared Inventory
			// START: SHIN-22 commented the below logic due to the post window
			// initializer class
			/*
			 * //Handling the case of UserNameSpace not having the attribute
			 * initialized if (YRCPlatformUI.isVoid(strIsSharedInvNode)) {
			 * AcademySIMTraceUtil.logMessage(CLASSNAME+":"+pageBeingShown+"::The
			 * value is void for strIsSharedInvNode so calling
			 * getShipNodeList");
			 * strInvocationFlag=AcademyPCAConstants.STR_VAL_SECOND;
			 * callGetShipNodeListAPI(); }
			 */

			// END: SHIN-22
			// If UserNameSpace was initialized with attribute, and
			// strIsSharedInvNode is "N" then hide the field
			// START SHIN-22 Uncomment The below line and comment the line which
			// doesnt check for void
			if (YRCPlatformUI.isVoid(strIsSharedInvNode)
					|| AcademyPCAConstants.STRING_N.equals(strIsSharedInvNode)) {
				// if (AcademyPCAConstants.STRING_N.equals(strIsSharedInvNode))
				// {
				// END SHIN-22
				AcademySIMTraceUtil
						.logMessage(CLASSNAME
								+ ":"
								+ pageBeingShown
								+ "::The value is N for UserNameSpace attribute IsSharedInventoryDC");
				hideField("extn_lblWarehouseCont_Id_ShipDtls", this);
				hideField("extn_txtWarehouseCont_Id_ShipDtls", this);
			}
			// END: SHIN-6

			// EFP-5 Start

			// EFP-5 End
		}
		// Changed the Form IDs since the FormID's are different in SOM.
		if (pageBeingShown
				.equals("com.yantra.pca.ycd.rcp.tasks.backroompick.wizardpages.YCDBackroomPickShortageReasons")) {
			setFieldValue("rdbSearchOnOrder", Boolean.valueOf(true));

			getCommonCodeList("INV_SHRT_RSN");

			// Setting the default value as "BP_Inventory_Shortage" for the
			// String "strSelectedRadioButton"
			// as "Inventory Shortage" Radio button will select when patge load

			strSelectedRadioButton = "BP_Inventory_Shortage";
		}

		AcademySIMTraceUtil.endMessage(CLASSNAME, "initPage(pageBeingShown)");
	}

	private void getCommonCodeList(String CodeType) {
		AcademySIMTraceUtil
				.logMessage("Inside getCommonCodeList for Pack station");
		YRCApiContext yrcApiContext = new YRCApiContext();
		yrcApiContext.setApiName("getCommonCodeList");
		Document inDoc = YRCXmlUtils.createDocument("CommonCode");

		yrcApiContext.setInputXml(getCommonCodeListInput(inDoc, CodeType)
				.getOwnerDocument());
		AcademySIMTraceUtil.logMessage("Code Type for Pack station: "
				+ CodeType);
		yrcApiContext.setFormId(getFormId());

		callApi(yrcApiContext);
	}

	private Element getCommonCodeListInput(Document docInputgetCommonCodeList,
			String codeType) {
		AcademySIMTraceUtil
				.logMessage("Inside getCommonCodeListInput for Pack station");
		String str = "getCommonCodeListInput(docInputgetCommonCodeList)";
		AcademySIMTraceUtil.startMessage(CLASSNAME,
				"getCommonCodeListInput(docInputgetCommonCodeList)");
		Element rootElement = docInputgetCommonCodeList.getDocumentElement();
		rootElement.setAttribute("CodeType", codeType);
		AcademySIMTraceUtil
				.logMessage("INPUT to getCommonCodeList for Pack station : "
						+ YRCXmlUtils.getString(rootElement));
		AcademySIMTraceUtil.endMessage(CLASSNAME,
				"getCommonCodeListInput(docInputgetCommonCodeList)");
		return rootElement;
	}

	private void callApiForContainerTypes() {
		String str = "callApiForContainerTypes()";
		AcademySIMTraceUtil.startMessage(CLASSNAME,
				"callApiForContainerTypes()");

		YRCApiContext context = new YRCApiContext();
		context.setApiName("getItemList");
		context.setFormId(getFormId());
		context.setInputXml(prepareInputForContainerTypes());
		callApi(context);

		AcademySIMTraceUtil.endMessage(CLASSNAME, "callApiForContainerTypes()");
	}

	private Document prepareInputForContainerTypes() {
		String str = "prepareInputForContainerTypes()";
		AcademySIMTraceUtil.startMessage(CLASSNAME,
				"prepareInputForContainerTypes()");

		Document itemInputDoc = YRCXmlUtils.createDocument("Item");
		// STL - 733
		Element eleItem = itemInputDoc.getDocumentElement();
		// STL - 733
		itemInputDoc.getDocumentElement().setAttribute("IsShippingCntr", "Y");
		
		//OMNI-7980 : Begin
		isSTSAmmoOrHazmatShipment = isSTSAmmoOrHazmatShipment();		
		AcademySIMTraceUtil.logMessage("STS AMMO or HAZMAT :: " + isSTSAmmoOrHazmatShipment);
		//OMNI-7980 : End
		
		// Start - STL-733 - Specific containers for AMMO items
		// check if Container Filtering is required based on ShipmentType
		final boolean bFilterRequired = ammoContainerFilterRequired();
		if (bFilterRequired) {

			// Add Item Type.
			Element elePrimaryInfo = YRCXmlUtils.createChild(eleItem,
					"PrimaryInformation");
			elePrimaryInfo.setAttribute("ItemType", "AmmoContainer");

			// check ShipmentType.
			AcademySIMTraceUtil.logMessage("shipment type" + strShipmentType);

			// Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat
			// Implementation
			if (AcademyPCAConstants.AMMO_SHIPMENT_TYPE
					.equalsIgnoreCase(strShipmentType)
					|| AcademyPCAConstants.HAZMAT_SHIPMENT_TYPE
							.equalsIgnoreCase(strShipmentType)
							//OMNI-7980
							|| isSTSAmmoOrHazmatShipment) {
							//OMNI-7980
				// End WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat
				// Implementation

				elePrimaryInfo.setAttribute("ItemTypeQryType", "EQ");
			} else {

				elePrimaryInfo.setAttribute("ItemTypeQryType", "NE");
			}
		}
		// End - STL-733 - Specific containers for AMMO items

		AcademySIMTraceUtil.endMessage(CLASSNAME,
				"prepareInputForContainerTypes()");
		return itemInputDoc;
	}

	/**
	 * STL-733 - Specific containers for AMMO items This method invokes
	 * AmmoContainerFilterRequired getCommonCodeList.
	 * 
	 * @return
	 */
	private boolean ammoContainerFilterRequired() {

		String str = "ammoContainerFilterRequired()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, str);

		// create getCommonCodeList api input.
		Document docCommonCode = YRCXmlUtils.createDocument("CommonCode");
		Element eleCommonCode = docCommonCode.getDocumentElement();
		eleCommonCode.setAttribute("CodeType", "CNTNR_FLTR_REQ");

		// create Api Context Object
		YRCApiContext yrcApiContext = new YRCApiContext();
		yrcApiContext.setApiName("getCommonCodeList");
		yrcApiContext.setInputXml(docCommonCode);
		yrcApiContext.setFormId(getFormId());

		// create Api Caller.
		YRCApiCaller yrcApiCaller = new YRCApiCaller(yrcApiContext, true);
		yrcApiCaller.invokeApi();

		// get the Api Output
		Document docCommonCodeListO = yrcApiContext.getOutputXml();
		Element eleCommonCodeListO = docCommonCodeListO.getDocumentElement();
		Element eleCommonCodeO = YRCXmlUtils.getChildElement(
				eleCommonCodeListO, "CommonCode");
		String strCodeShrtDesc = eleCommonCodeO
				.getAttribute("CodeShortDescription");

		// return boolean
		boolean bFilterRequired;
		if ("Y".equalsIgnoreCase(strCodeShrtDesc)) {

			bFilterRequired = true;
		} else {

			bFilterRequired = false;
		}

		AcademySIMTraceUtil.endMessage(CLASSNAME, str);

		return bFilterRequired;

	} // End of ammoContainerFilterRequired()

	private void callServiceForShipmentContainerProcessing(
			Element shipmentElement) {
		String str = "callServiceForShipmentContainerProcessing(shipmentElement)";
		AcademySIMTraceUtil.startMessage(CLASSNAME,
				"callServiceForShipmentContainerProcessing(shipmentElement)");

		YRCApiContext context = new YRCApiContext();
		context.setApiName("CreateContainersAndPrintService");
		context.setFormId(getFormId());
		context.setInputXml(shipmentElement.getOwnerDocument());
		callApi(context);

		AcademySIMTraceUtil.endMessage(CLASSNAME,
				"callServiceForShipmentContainerProcessing(shipmentElement)");
	}

	public boolean preCommand(YRCApiContext context) {
		String str1 = "preCommand(context)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, "preCommand(context)");
		// Start-STL-950 Added as part of Shared Inventory
		strContainerId = getFieldValue("extn_txtWarehouseContId");
		Element eleGetShipmentListInputDoc = null;
		AcademySIMTraceUtil.logMessage("context.getApiName():: "
				+ context.getApiName());
		if (context.getApiName().equals("getShipmentList")) {
			AcademySIMTraceUtil.logMessage("input xml to getShipmentList::"
					+ YRCXmlUtils.getString(context.getInputXml()));
			eleGetShipmentListInputDoc = context.getInputXml()
					.getDocumentElement();
			Element ele = eleGetShipmentListInputDoc.getOwnerDocument()
					.createElement("Extn");
			eleGetShipmentListInputDoc.appendChild(ele);
			ele.setAttribute("ExtnExeterContainerId", strContainerId);

			// START: SHIN-21 validate if IsSharedInventoryDC is Y then set the
			// attribute "Status" to the getShipmentList input document
			if (isSharedInvNode()) {
				eleGetShipmentListInputDoc.setAttribute(
						AcademyPCAConstants.STATUS_ATTR,
						AcademyPCAConstants.STATUS_READY_FOR_BACKROOM_PICK_VAL);

				// START STL-1678 invoke getShipmentList based on UPC/ItemId
				if (AcademyPCAConstants.STRING_Y
						.equals(strIsUPCBaseBPSearchEnable)) {
					String strUPCCode = getFieldValue(AcademyPCAConstants.STR_EXTN_TXT_UPC_CODE);

					if (!YRCPlatformUI.isVoid(strUPCCode)) {
						eleGetShipmentListInputDoc.setAttribute(
								AcademyPCAConstants.ATTR_UPC_CODE, strUPCCode);
						eleGetShipmentListInputDoc.setAttribute(
								AcademyPCAConstants.SHIPNODE_ATTR,
								YRCPlatformUI.getUserElement().getAttribute(
										AcademyPCAConstants.SHIPNODE_ATTR));
					}
				}
				// END STL-1678 invoke getShipmentList
			}
			// END: SHIN-21

			AcademySIMTraceUtil.logMessage("input to getShipmentList"
					+ YRCXmlUtils.getString(eleGetShipmentListInputDoc));
		}
		// End-STL-950 Added as part of Shared Inventory
		if (context.getApiName().equals("translateBarCode")) {

			Document barcodeInputDoc = context.getInputXml();
			// START : STL-1300 : validate ExtnExeterContainerId. Block backroom
			// pick if ExtnExeterContainerId not received.
			if (isExtnExeterContainerIdNotReceived) {
				YRCPlatformUI
						.setMessage("ERROR_EXTN_EXETER_CONTAINER_ID_IS_NOT_RECEIVED");
				showError("ERROR_EXTN_EXETER_CONTAINER_ID_IS_NOT_RECEIVED");
				return false;
			}
			// END : STL-1300 : validate ExtnExeterContainerId. Block backroom
			// pick if ExtnExeterContainerId not received.
			Element rootElement = barcodeInputDoc.getDocumentElement();
			rootElement.setAttribute("BarCodeType", "SFSItem");
			Element ctxtInfoElement = (Element) rootElement
					.getElementsByTagName("ContextualInfo").item(0);
			ctxtInfoElement.setAttribute("OrganizationCode", "DEFAULT");
			context.setInputXml(barcodeInputDoc);
			AcademySIMTraceUtil.logMessage("Modified Input for "
					+ context.getApiName() + " command: \n", rootElement);

		}

		AcademySIMTraceUtil.endMessage(CLASSNAME, "preCommand(context)");
		return super.preCommand(context);
	}

	public void postCommand(YRCApiContext context) {
		AcademySIMTraceUtil.logMessage("context.getApiName() in postCommand::"
				+ context.getApiName());

		if (context.getApiName().equals("getShipmentDetails")) {
			context.getOutputXml().getDocumentElement().setAttribute(
					"BackroomPickRequired", "Y");
		}
		// EFP-7 : Start
		if (context.getApiName().equals("translateBarCode")) {

			System.out
					.println("*******************************Post commandcalled**translate barcode");
			AcademySIMTraceUtil.logMessage("PostCommand : translateBarCode::");

			AcademySIMTraceUtil.logMessage("translateBarCode Api Output::"
					+ AcademySIMTraceUtil.getElementXMLString(context
							.getOutputXml().getDocumentElement()));
			
			// Start OMNI-443 Enable SerialNo field for scanning GC Item using UPC/ItemID
			if (AcademyPCAConstants.STRING_Y.equals(strIsSharedInvNode)) {

			double dBackroomPickedQuantity = 0;

			AcademySIMTraceUtil
					.logMessage(" start of Enable SerialNo for scanning UPC/ItemID");

			String strItemID = null;

			String strTotalNumberOfRecords = (String) YRCXPathUtils.evaluate(
					context.getOutputXml().getDocumentElement(),
					AcademyPCAConstants.BARCODE_TOTALNOOFRECORDS_ATTR_XPATH,
					XPathConstants.STRING);

			AcademySIMTraceUtil.logMessage("strTotalNumberOfRecords "
					+ strTotalNumberOfRecords);

			if (AcademyPCAConstants.STR_ZERO.equals(strTotalNumberOfRecords)) {

				strItemID = (String) YRCXPathUtils.evaluate(context
						.getOutputXml().getDocumentElement(),
						"/BarCode/@BarCodeData", XPathConstants.STRING);

				AcademySIMTraceUtil.logMessage(" Inside Iff strItemID ::: "
						+ strItemID);

			} else {

				strItemID = (String) YRCXPathUtils
						.evaluate(
								context.getOutputXml().getDocumentElement(),
								"/BarCode/Translations/Translation/ItemContextualInfo/@ItemID",
								XPathConstants.STRING);

				AcademySIMTraceUtil.logMessage(" Inside Else strItemID ::: "
						+ strItemID);
			}

			AcademySIMTraceUtil.logMessage("strItemID ::: " + strItemID);

			System.out.println("strItemID ::: " + strItemID);

			Element eleGetShipmentDetailsModel = getModel(AcademyPCAConstants.GET_SHIPMENT_DETAILS_OOB_MODEL);

			NodeList nlShipmentLine = (NodeList) YRCXPathUtils.evaluate(
					eleGetShipmentDetailsModel,
					"/Shipment/ShipmentLines/ShipmentLine[@ItemID='"
							+ strItemID
							+ "' and OrderLine/Item/Extn/@ExtnIsGiftCard='Y']",
					XPathConstants.NODESET);

			AcademySIMTraceUtil.logMessage("nlShipmentLine ::: "
					+ nlShipmentLine);

			for (int iShipmentLineCounter = 0; (iShipmentLineCounter < nlShipmentLine
					.getLength() && !isSerialScannedRequired); iShipmentLineCounter++) {

				Element XMLeleShipmentLine = (Element) nlShipmentLine
						.item(iShipmentLineCounter);

				if (!YRCPlatformUI.isVoid(XMLeleShipmentLine)) {

					AcademySIMTraceUtil
							.logMessage(" Inside iff XMLeleShipmentLine ::: "
									+ XMLeleShipmentLine);

					strSerialScanForShipmentLineKey = XMLeleShipmentLine
							.getAttribute(AcademyPCAConstants.SHIPMENT_LINE_KEY_ATTR);

					String strBackroomPickedQuantity = XMLeleShipmentLine
							.getAttribute(AcademyPCAConstants.BACKROOM_PICKED_QUANTITY_ATTR);

					String strQuantity = XMLeleShipmentLine
							.getAttribute(AcademyPCAConstants.QUANTITY_ATTR);

					String strPickQuantity = XMLeleShipmentLine
							.getAttribute(AcademyPCAConstants.PICK_QUANTITY_ATTR);

					double dQuantity = Double.valueOf(strQuantity);

					double dPickQuantity = Double.valueOf(strPickQuantity);

					if (!YRCPlatformUI.isVoid(strBackroomPickedQuantity)) {

						dBackroomPickedQuantity = Double
								.valueOf(strBackroomPickedQuantity);

					}

					if (dQuantity == (dBackroomPickedQuantity + dPickQuantity)) {

						AcademySIMTraceUtil
								.logMessage("Line Completely Picked!!!!");

						setFocus(AcademyPCAConstants.OOB_TXT_SCAN_ITEMID);

					}

					else {

						isSerialScannedRequired = true;

						disableField(AcademyPCAConstants.OOB_TXT_SCAN_ITEMID);

						disableField(AcademyPCAConstants.CUSTOM_OVERRIDE_BTN_NEXT);

						disableField(AcademyPCAConstants.OOB_BTN_CONFIRM);

						enableField(AcademyPCAConstants.CUSTOM_TXT_SERIALNO_SCAN);

						setFocus(AcademyPCAConstants.CUSTOM_TXT_SERIALNO_SCAN);

						strSetFocusToField = AcademyPCAConstants.CUSTOM_TXT_SERIALNO_SCAN;
						AcademySIMTraceUtil
								.logMessage(" Field Enabled for ItemID");

					}
				}
			}
			if (!isSerialScannedRequired) {

				disableField(AcademyPCAConstants.CUSTOM_TXT_SERIALNO_SCAN);
			}
		}
		
		// End OMNI-443 of Enable SerialNo for scanning GC item using UPC/ItemID	
			String strContainerType = this
					.getFieldValue("extn_TxtScanContnrType");

			if ("VendorPackageMultibox".equalsIgnoreCase(strContainerType)) {
				this.multiBoxFlag = true;
			}

			if (!this.multiBoxFlag) {

				AcademySIMTraceUtil
						.logMessage("getContainerAPI  called since item is not multibox sku");
				System.out
						.println("getContainerAPI not called since item is a multibox sku");
				getContainerType(context.getOutputXml().getDocumentElement());
			} else {
				AcademySIMTraceUtil
						.logMessage("getContainerAPI not called since item is a multibox sku");
			}

		}

		if (context.getApiName().equals("translateBarCodeForContainerType")) {
			AcademySIMTraceUtil
					.logMessage("PostCommand : translateBarCodeForContainerType::");

			AcademySIMTraceUtil
					.logMessage("translateBarCodeForContainerType Api Output::"
							+ AcademySIMTraceUtil.getElementXMLString(context
									.getOutputXml().getDocumentElement()));

			String strTotalNumberOfRecords = (String) YRCXPathUtils.evaluate(
					context.getOutputXml().getDocumentElement(),
					AcademyPCAConstants.BARCODE_TOTALNOOFRECORDS_ATTR_XPATH,
					XPathConstants.STRING);

			if (AcademyPCAConstants.STR_ZERO.equals(strTotalNumberOfRecords)) {
				AcademySIMTraceUtil.logMessage("The Value Entered is Invalid");
				showError("ERROR_EXTN_INVALID_CONTAINER_BARCODE");
				this.setFieldValue("extn_TxtScanContnrType", "");
				this.setFieldValue("extn_TxtContnrBarcde", "");

			}

		}
		// EFP-7 : End
		
		
	}

	// START: SHIN-22 commented the below logic due to the post window
	// initializer class

	/*
	 * //START: SHIN-6 // This callGetShipNodeListAPI() will invoke the
	 * getShipNodeList API
	 * 
	 * public void callGetShipNodeListAPI() { String strMethodName =
	 * "callGetShipNodeListAPI()"; AcademySIMTraceUtil.startMessage(CLASSNAME,
	 * strMethodName);
	 * 
	 * YRCApiContext context = new YRCApiContext();
	 * context.setFormId(getFormId());
	 * context.setApiName(AcademyPCAConstants.GET_SHIP_NODE_LIST_COMMAND);
	 * context.setInputXml(prepareInputForGetShipNodeList());
	 * YRCPlatformUI.callApi(context, this);
	 * 
	 * AcademySIMTraceUtil.endMessage(CLASSNAME,strMethodName); } // The method
	 * will prepare the input for getshipNodeList API public Document
	 * prepareInputForGetShipNodeList() { String str =
	 * "prepareInputForGetShipNodeList()";
	 * 
	 * AcademySIMTraceUtil.startMessage(CLASSNAME, str); Document
	 * getShipNodeListInputDoc = YRCXmlUtils
	 * .createDocument(AcademyPCAConstants.SHIPNODE_ATTR); Element
	 * eleRootElement = getShipNodeListInputDoc.getDocumentElement();
	 * eleRootElement.setAttribute(AcademyPCAConstants.SHIPNODE_ATTR,
	 * YRCPlatformUI.getUserElement().getAttribute(
	 * AcademyPCAConstants.ATTR_NODE));
	 * 
	 * AcademySIMTraceUtil.endMessage(CLASSNAME,str);
	 * 
	 * return getShipNodeListInputDoc;
	 *  }
	 * 
	 * //END: SHIN-6
	 */

	// END: SHIN-22
	public void handleApiCompletion(YRCApiContext context) {
		String str1 = "handleApiCompletion()";

		/*
		 * START: SHIN-22 commented the below logic due to the post window
		 * initializer class //START: SHIN-6 Document shipNodeListDoc; Element
		 * eleshipNode; //declaring the strNodeType in class level //String
		 * strNodeType=""; //END: SHIN-6 //END: SHIN-22
		 */

		AcademySIMTraceUtil.startMessage(CLASSNAME, str1);

		AcademySIMTraceUtil
				.logMessage("Handle API Method context.getApiName():: "
						+ context.getApiName());
		if (context.getInvokeAPIStatus() < 0) {
			AcademySIMTraceUtil.logMessage(context.getApiName()
					+ " call Failed");
			AcademySIMTraceUtil.logMessage("Error Output: \n", context
					.getOutputXml().getDocumentElement());
		} else {

			AcademySIMTraceUtil.logMessage("Else context.getApiName():: "
					+ context.getApiName());

			if (context.getApiName().equals("getItemList")) {
				AcademySIMTraceUtil.logMessage("Context API: "
						+ context.getApiName());
				AcademySIMTraceUtil.logMessage("getItemList Output: \n",
						context.getOutputXml().getDocumentElement());

				Document vendorPkgDoc = YRCXmlUtils
						.createFromString("<Item ItemID='VendorPackage'><PrimaryInformation ShortDescription='VENDOR PACKAGE'/></Item>");
				Node ndVendorPkg = context.getOutputXml().importNode(
						vendorPkgDoc.getDocumentElement(), true);
				// STL - 733
				// context.getOutputXml().getDocumentElement().appendChild(ndVendorPkg);

				// Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat
				// Implementation
				if (!(AcademyPCAConstants.AMMO_SHIPMENT_TYPE
						.equals(strShipmentType) || AcademyPCAConstants.HAZMAT_SHIPMENT_TYPE
						.equals(strShipmentType)
						//OMNI-7980
						|| isSTSAmmoOrHazmatShipment)) {
						//OMNI-7980
					context.getOutputXml().getDocumentElement().appendChild(
							ndVendorPkg);
				}
				//OMNI-23764
				context.getOutputXml().getDocumentElement().appendChild(
						ndVendorPkg);
				// End WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat
				// Implementation
				// STL - 733
				setExtentionModel("Extn_getItemList_Output", context
						.getOutputXml().getDocumentElement());

				// STL-1309 : Defaulting the cursor on Item ID filed in Back
				// room Pick screen : START
				YRCBehavior bhvr = (YRCBehavior) YRCDesktopUI.getCurrentPage()
						.getData("YRCBehavior");
				Composite cmpScanModeCtrlOOB = (Composite) bhvr
						.getControl(AcademyPCAConstants.CMP_SCAN_MODE_CNTRLS);

				ArrayList controlList = new ArrayList();
				if (cmpScanModeCtrlOOB != null
						&& cmpScanModeCtrlOOB.getChildren() != null) { // STL-1718
					// if
					// condition
					// added
					getFieldCtrls(cmpScanModeCtrlOOB.getChildren(), controlList);
				}
				// STL-1309 : Defaulting the cursor on Item ID filed in Back
				// room Pick screen : END
				// STL-1718 Begin
				Composite cmpstShipmentSearchOOB = (Composite) bhvr
						.getControl(AcademyPCAConstants.STR_COMP_SHIPMENT_SEARCH);
				if (cmpstShipmentSearchOOB != null
						&& cmpstShipmentSearchOOB.getChildren() != null) {
					getFieldCtrls(cmpstShipmentSearchOOB.getChildren(),
							controlList);
				}
				// STL-1718 End
			}

			if (context.getApiName().equals("CreateContainersAndPrintService")) {
				AcademySIMTraceUtil.logMessage("Context Service: "
						+ context.getApiName());
				AcademySIMTraceUtil.logMessage("Service Output: \n", context
						.getOutputXml().getDocumentElement());

				if (strSelectedButton.equals("extn_btnPickNext")) {
					AcademySIMTraceUtil
							.logMessage("calling OOB Next Action : com.yantra.sop.SOPNextRecordBackroomAction :: START");

					YRCPlatformUI
							.fireAction(AcademyPCAConstants.BTN_NEXT_OOB_ACTION_ID);
					AcademySIMTraceUtil
							.logMessage("calling OOB Next Action : com.yantra.sop.SOPNextRecordBackroomAction :: END");
					refreshBackroomSearchScreen();// STL-1718
				} else if (strSelectedButton.equals("extn_btnPickConfirm")) {
					AcademySIMTraceUtil
							.logMessage("calling OOB Confirm Action : com.yantra.pca.ycd.rcp.tasks.backroompick.actions.YCDBackroomPickConfirmAction :: START");

					YRCPlatformUI
							.fireAction("com.yantra.pca.ycd.rcp.tasks.backroompick.actions.YCDBackroomPickConfirmAction");
					AcademySIMTraceUtil
							.logMessage("calling OOB Confirm Action : com.yantra.pca.ycd.rcp.tasks.backroompick.actions.YCDBackroomPickConfirmAction :: END");
					refreshBackroomSearchScreen();// STL-1718
				}

				// START:Fixed for STL-1276
				setFieldValue("txtShipmentSearchShipmentNo", "");
				if (!YRCPlatformUI
						.isVoid(getExtentionModel("Extn_ItemDetail_List"))) {
					setExtentionModel("Extn_ItemDetail_List", null);
				}
				// END for STL-1276
			}

			if (context.getApiName().equals("getCommonCodeList")) {
				AcademySIMTraceUtil.logMessage("Inside handle api");
				AcademySIMTraceUtil.logMessage("Context API: "
						+ context.getApiName());

				Element reasonCodeOutput = context.getOutputXml()
						.getDocumentElement();
				AcademySIMTraceUtil.logMessage("Api Output: \n", context
						.getOutputXml().getDocumentElement());
				Element eleCommonCode = (Element) reasonCodeOutput
						.getElementsByTagName("CommonCode").item(0);

				// START STL-1664 setting strSinglePckstnCodeValue only for Code
				// type PACK_PRINTER_ID, else this value is setting for all
				// common ode call.
				String strCodeType = eleCommonCode
						.getAttribute(AcademyPCAConstants.CODE_TYPE_ATTR);

				if (strCodeType.equalsIgnoreCase("PICK_QTY_THRESHOLD")) {

					String thresholdPickQuantityString = eleCommonCode
							.getAttribute("CodeValue");
					this.thresholdPickQuantity = (thresholdPickQuantityString
							.isEmpty() || thresholdPickQuantityString == null) ? 0
							: Double.parseDouble(thresholdPickQuantityString);
					AcademySIMTraceUtil
							.logMessage("Threshold Quantity for Pikc Quantity being editable is "
									+ thresholdPickQuantity);
				}
                
				//Start - OMNI-65686 Pack Station - Auto select container size - RCP SOM
				if (AcademyPCAConstants.TGL_RCP_WEB_SOM_UI.equalsIgnoreCase(strCodeType)) {
					NodeList ndCommonCode = reasonCodeOutput.getElementsByTagName("CommonCode");
					for (int i = 0; i < ndCommonCode.getLength(); i++) {
						Element eCommonCode = (Element) ndCommonCode.item(i);
						String codeValue = eCommonCode.getAttribute("CodeValue");
						String codeShortDesc = eCommonCode.getAttribute("CodeShortDescription");
						AcademySIMTraceUtil.logMessage("Dropdown Container flag is " + codeValue + ":" + codeShortDesc);
						if (AcademyPCAConstants.STR_CONTAINER_DROP_DOWN_RCP.equalsIgnoreCase(codeValue)) {
							if (AcademyPCAConstants.STRING_N.equalsIgnoreCase(codeShortDesc)) {
								showVendorPackage = true;
								hideField("extn_cmbContainerType_scan", this);
							} else {
								hideField("extn_lblVendorContainerType", this);
							}
						}
					}
				}
				//End - OMNI-65686 Pack Station - Auto select container size - RCP SOM

				
				if (AcademyPCAConstants.STR_PACK_PRINTER_ID
						.equalsIgnoreCase(strCodeType)) {
					this.strSinglePckstnCodeValue = eleCommonCode
							.getAttribute("CodeValue");
					AcademySIMTraceUtil
							.logMessage("Single PACK Station Code Value is :"
									+ this.strSinglePckstnCodeValue);
				}
				// String strCodeType =
				// eleCommonCode.getAttribute("CodeType");// Moving this to
				// before single pack station validation
				// END STL-1664 setting strSinglePckstnCodeValue only for Code
				// type PACK_PRINTER_ID, else this value is setting for all
				// common ode call.
				if (strCodeType.equalsIgnoreCase("INV_SHRT_RSN")) {
					setExtentionModel("Extn_inv_shrt_rsn_Output",
							reasonCodeOutput);
				}
				if (strCodeType.equalsIgnoreCase("MUL_PACK_STORE")) {
					AcademySIMTraceUtil
							.logMessage("getCommonCodeList Output for MUL_PACK_STORE for Pack station:"
									+ YRCXmlUtils.getString(reasonCodeOutput));

					NodeList mulPckStnNL = reasonCodeOutput
							.getElementsByTagName("CommonCode");
					Element eleMulPckStnCommonCode;
					for (int i = 0; i < mulPckStnNL.getLength(); i++) {
						eleMulPckStnCommonCode = (Element) mulPckStnNL.item(i);
						AcademySIMTraceUtil
								.logMessage("inside for loop for Pack station"
										+ YRCXmlUtils
												.getString(eleMulPckStnCommonCode));
						String strCommonCodeValue = eleMulPckStnCommonCode
								.getAttribute("CodeValue");
						AcademySIMTraceUtil
								.logMessage("Multiple PACK Station Code Value is :"
										+ strCommonCodeValue);
						String strCommonCodeShrDes = eleMulPckStnCommonCode
								.getAttribute("CodeShortDescription");
						AcademySIMTraceUtil
								.logMessage("Multiple PACK Station Description is :"
										+ strCommonCodeShrDes);
						this.packMap.put(strCommonCodeValue,
								strCommonCodeShrDes);
						AcademySIMTraceUtil
								.logMessage("Hash MAP Value for Pack station :"
										+ this.packMap);
					}
					for (String key : this.packMap.keySet()) {
						String value = (String) this.packMap.get(key);
						AcademySIMTraceUtil.logMessage(key + " " + value);
					}
					String shipNodeFromInput = YRCPlatformUI.getUserElement()
							.getAttribute("ShipNode");
					AcademySIMTraceUtil.logMessage("ShipNode Value is::"
							+ shipNodeFromInput);
					if (this.packMap.containsKey(shipNodeFromInput)) {
						this.strConcatShipNode = shipNodeFromInput
								.concat("_PACK_STN");
						AcademySIMTraceUtil
								.logMessage("CommonCodeType for Pack station Is::"
										+ this.strConcatShipNode);
						getCommonCodeList(this.strConcatShipNode);
					} else {
						AcademySIMTraceUtil
								.logMessage("CommonCodeType Is::PACK_PRINTER_ID");
						getCommonCodeList("PACK_PRINTER_ID");
						hideField("extn_PrinterIdComboBox", this);
						hideField("extn_lblPrinter_Id", this);
						AcademySIMTraceUtil
								.logMessage("The Node User has currently logged in has Single Pack Station:"
										+ YRCXmlUtils
												.getString(reasonCodeOutput));
					}

					AcademySIMTraceUtil.logMessage("concatenated string"
							+ this.strConcatShipNode);
				} else if (strCodeType.equalsIgnoreCase(this.strConcatShipNode)) {
					setExtentionModel("Extn_PackSlip_PrinterID_Output",
							reasonCodeOutput);
					AcademySIMTraceUtil
							.logMessage("getCommonCodeList Output With Multiple PAckStation"
									+ reasonCodeOutput);

					// START WN-2346 Retain user selected pack station
					String strDefaultPrinter = YRCPlatformUI.getUserElement()
							.getAttribute(
									AcademyPCAConstants.ATTR_DEFAULT_PRINTER);
					if (!YRCPlatformUI.isVoid(strDefaultPrinter)) {
						setFieldValue(
								AcademyPCAConstants.CUSTOM_COMBO_PRINTER_ID,
								strDefaultPrinter);
					}
					// END WN-2346 Retain user selected pack station
				}

				AcademySIMTraceUtil
						.logMessage("The Node User has currently logged in has Single Pack Station:"
								+ YRCXmlUtils.getString(reasonCodeOutput));
			}

			if (context.getApiName().equals("getItemDetailsList")) {
				AcademySIMTraceUtil.logMessage("Context API: "
						+ context.getApiName());
				AcademySIMTraceUtil.logMessage("getItemList Output: \n",
						context.getOutputXml().getDocumentElement());
				setExtentionModel("Extn_ItemDetail_List", context
						.getOutputXml().getDocumentElement());

				Element eleItemList = context.getOutputXml()
						.getDocumentElement();
				Element eleShipmentDetailsModel = getModel("getShipmentDetails_output");
				NodeList nlItemList = eleItemList.getElementsByTagName("Item");
				for (int listIndex = 0; listIndex < nlItemList.getLength(); listIndex++) {
					Element eleItem = (Element) nlItemList.item(listIndex);
					String strItemID = eleItem.getAttribute("ItemID");

					// START WN-2980 GC Activation and fulfillment for SI DCs
					/*
					 * Element eleShipmentItemDtlNode = (Element)
					 * YRCXPathUtils.evaluate(eleShipmentDetailsModel,
					 * "/Shipment/ShipmentLines/ShipmentLine/OrderLine/Item[@ItemID='" +
					 * strItemID + "']", XPathConstants.NODE);
					 */

					NodeList nlShipmentItemDtlNode = (NodeList) YRCXPathUtils
							.evaluate(eleShipmentDetailsModel,
									"/Shipment/ShipmentLines/ShipmentLine/OrderLine/Item[@ItemID='"
											+ strItemID + "']",
									XPathConstants.NODESET);
					Element eleShipmentItemDtlNode = null;
					for (int iItemCounter = 0; iItemCounter < nlShipmentItemDtlNode
							.getLength(); iItemCounter++) {
						eleShipmentItemDtlNode = (Element) nlShipmentItemDtlNode
								.item(iItemCounter);
						// END WN-2980 GC Activation and fulfillment for SI DCs

						if (!YRCPlatformUI.isVoid(eleShipmentItemDtlNode)) {
							NodeList nlChildNodes = eleItem.getChildNodes();
							for (int i = 0; i < nlChildNodes.getLength(); i++) {
								Node ndChildNode = eleShipmentDetailsModel
										.getOwnerDocument().importNode(
												nlChildNodes.item(i), true);
								eleShipmentItemDtlNode.appendChild(ndChildNode);
							}
						}
					}
				}
				AcademySIMTraceUtil.logMessage("ShipmentDetails Model : \n",
						eleShipmentDetailsModel);
			}

			// START: SHIN-22 commented the below logic due to the post window
			// initializer class

			/*
			 * //START: SHIN-6 if (context.getApiName().equals(
			 * AcademyPCAConstants.GET_SHIP_NODE_LIST_COMMAND)) {
			 * AcademySIMTraceUtil .logMessage("handleApiCompletion(context) :: " +
			 * context.getApiName());
			 * 
			 * shipNodeListDoc = context.getOutputXml(); eleshipNode = (Element)
			 * shipNodeListDoc
			 * .getElementsByTagName(AcademyPCAConstants.SHIPNODE_ATTR)
			 * .item(0); strNodeType =
			 * eleshipNode.getAttribute(AcademyPCAConstants.ATTR_NODE_TYPE);
			 * 
			 * if
			 * (AcademyPCAConstants.ATTR_VAL_SHAREDINV_DC.equals(strNodeType)){
			 * AcademySIMTraceUtil.logMessage(CLASSNAME+"::The value is
			 * SharedInventoryDC for ShipNode NodeType");
			 * 
			 * if(AcademyPCAConstants.STR_VAL_THIRD.equals(strInvocationFlag) &&
			 * YRCPlatformUI.isVoid(strExtnExeterContainerId)){
			 * isExtnExeterContainerIdNotReceived = true;
			 * showError("ERROR_EXTN_EXETER_CONTAINER_ID_IS_NOT_RECEIVED"); } }
			 * else { AcademySIMTraceUtil.logMessage(CLASSNAME+"::The value is
			 * NOT SharedInventoryDC for ShipNode NodeType");
			 * if(AcademyPCAConstants.STR_VAL_FIRST.equals(strInvocationFlag)){
			 * setControlVisible("extn_lblWarehouseContId", false);
			 * setControlVisible("extn_txtWarehouseContId", false); } else
			 * if(AcademyPCAConstants.STR_VAL_SECOND.equals(strInvocationFlag)){
			 * hideField("extn_lblWarehouseCont_Id_ShipDtls", this);
			 * hideField("extn_txtWarehouseCont_Id_ShipDtls", this); } } }
			 * //END: SHIN-6
			 */

			// END: SHIN-22
			//
			if (context.getApiName().equals("AcademySFSTranslateBarCodeForSOM")) {
				AcademySIMTraceUtil
						.logMessage("Start handleApiCompletion :: AcademySFSTranslateBarCodeForSOM");
			}
			//

			// Start WN-2980 GC Activation and fulfillment for SI DCs
			if (context.getApiName().equals(
					AcademyPCAConstants.TRANSLATE_BARCODE_API)) {
				AcademySIMTraceUtil
						.logMessage("Start handleApiCompletion :: translateBarCodeForSerialNo");

				Element eleOutTranslateBarCode = context.getOutputXml()
						.getDocumentElement();
				AcademySIMTraceUtil.logMessage("translateBarCode API Output : "
						+ AcademySIMTraceUtil
								.getElementXMLString(eleOutTranslateBarCode));

				String strTotalNumberOfRecords = (String) YRCXPathUtils
						.evaluate(
								eleOutTranslateBarCode,
								AcademyPCAConstants.BARCODE_TOTALNOOFRECORDS_ATTR_XPATH,
								XPathConstants.STRING);

				String strBarcodeType = (String) YRCXPathUtils.evaluate(
						eleOutTranslateBarCode, "/BarCode/@BarCodeType",
						XPathConstants.STRING);

				if (AcademyPCAConstants.STR_ZERO
						.equals(strTotalNumberOfRecords)) {
					AcademySIMTraceUtil
							.logMessage("The Value Entered is Invalid");
					showError("ERROR_EXTN_INVALID_SERIALNO");
				} else {
					AcademySIMTraceUtil.logMessage("Valid SerialNo scanned");
					stampGiftCardSerialNo();
				}
				AcademySIMTraceUtil
						.logMessage("End handleApiCompletion :: translateBarCodeForSerialNo");
			}
			// End WN-2980 GC Activation and fulfillment for SI DCs

			if (context.getApiName().equals("translateBarCode")) {

				AcademySIMTraceUtil
						.logMessage("Start handleApiCompletion :: translateBarCode");

				Element eleOutTranslateBarCode = context.getOutputXml()
						.getDocumentElement();
				AcademySIMTraceUtil.logMessage("translateBarCode API Output : "
						+ AcademySIMTraceUtil
								.getElementXMLString(eleOutTranslateBarCode));

				String strTotalNumberOfRecords = (String) YRCXPathUtils
						.evaluate(
								eleOutTranslateBarCode,
								AcademyPCAConstants.BARCODE_TOTALNOOFRECORDS_ATTR_XPATH,
								XPathConstants.STRING);

				String strBarcodeType = (String) YRCXPathUtils.evaluate(
						eleOutTranslateBarCode, "/BarCode/@BarCodeType",
						XPathConstants.STRING);

				if (AcademyPCAConstants.STR_ZERO
						.equals(strTotalNumberOfRecords)) {
					AcademySIMTraceUtil
							.logMessage("The Value Entered is Invalid");
					showError("ERROR_EXTN_INVALID_SERIALNO");
				} else if ("Item".equals(strBarcodeType)) {
					AcademySIMTraceUtil
							.logMessage("Container Scanning is completed");
					stampContainerType(eleOutTranslateBarCode);

				}

				AcademySIMTraceUtil
						.logMessage("End handleApiCompletion :: translateBarCode");

			}

			// EFP-5 : Start
			if (context.getApiName().equals("translateBarCodeForContainerType")) {
				
				this.setFieldValue("extn_ContainerSelected", " ");
				this.setFieldValue("extn_TxtScanContnrType", " ");
				
				AcademySIMTraceUtil
						.logMessage("Start handleApiCompletion :: translateBarCodeForContainerType");

				Element eleOutTranslateBarCode = context.getOutputXml()
						.getDocumentElement();

				if (null != eleOutTranslateBarCode) {

					AcademySIMTraceUtil
							.logMessage("translateBarCodeForContainerType API Output ::"
									+ AcademySIMTraceUtil
											.getElementXMLString(eleOutTranslateBarCode));

					stampContainerType(eleOutTranslateBarCode);

				}
				
				//
				YRCBehavior bhvr = (YRCBehavior) YRCDesktopUI.getCurrentPage()
					.getData("YRCBehavior");
				Composite cmpScanModeCtrlOOB = (Composite) bhvr
					.getControl(AcademyPCAConstants.CMP_SCAN_MODE_CNTRLS);
				//.getControl("extn_cmbContainerType_scan");
				ArrayList controlList = new ArrayList();
				
				if (cmpScanModeCtrlOOB != null
						&& cmpScanModeCtrlOOB.getChildren() != null) { 
					getFieldCtrlsCustom(cmpScanModeCtrlOOB.getChildren(), controlList);
				}
				//
			}
			// EFP-5 : End

			// EFP-7 : Start
			if (context.getApiName().equals("AcadGetDPLookup")) {

				AcademySIMTraceUtil
						.logMessage("Start handleApiCompletion :: AcadGetDPLookup");
				
				//OMNI-82218 - Starts - Commenting out below two lines
				//this.setFieldValue("extn_ContainerSelected", " ");
				//this.setFieldValue("extn_TxtScanContnrType", " ");
				//OMNI-82218 - Ends

				if (null != context.getOutputXml()) {

					Element eleOutDPLoopkup = context.getOutputXml()
							.getDocumentElement();

					if (null != eleOutDPLoopkup) {

						AcademySIMTraceUtil
								.logMessage("AcadGetDPLookup API Output : "
										+ AcademySIMTraceUtil
												.getElementXMLString(eleOutDPLoopkup));

						String strContainer = (String) YRCXPathUtils.evaluate(
								eleOutDPLoopkup,
								"//AcadDirectedPackagingLookup/@Container",
								XPathConstants.STRING);

						if (!YRCPlatformUI.isVoid(strContainer)
								&& null != strContainer) {

							AcademySIMTraceUtil
									.logMessage("Suggested Container : "
											+ strContainer);

							Element eleContainerPriority = getExtentionModel("Extn_containerPriority");

							if (null != eleContainerPriority) {
								
								//OMNI-82218 - XPath Correction - Starts
								String strContnrPriority = (String) YRCXPathUtils
										.evaluate(eleContainerPriority,
												"//AcadDPContainerPriorityList/AcadDPContainerPriority[@Container='"
														+ strContainer
														+ "']/@Priority",
												XPathConstants.STRING);
								//OMNI-82218 - XPath Correction - Ends

								if (null != strContnrPriority) {

									AcademySIMTraceUtil
											.logMessage("Suggested Container Priority: "
													+ strContnrPriority);

									// String strPrevScannedContainer =
									// getFieldValue("extn_ContainerSelected");
									String strPrevScannedContainer = getFieldValue("extn_ContainerSelected");

									if (null != strPrevScannedContainer
											&& !YRCPlatformUI
													.isVoid(strPrevScannedContainer)) {

										AcademySIMTraceUtil
												.logMessage("Prev Container Type: "
														+ strPrevScannedContainer);
										//OMNI-82218 - XPath Correction - Starts
										String strPrevContnrPriority = (String) YRCXPathUtils
												.evaluate(
														eleContainerPriority,
														"//AcadDPContainerPriorityList/AcadDPContainerPriority[@Container='"
																+ strPrevScannedContainer
																+ "']/@Priority",
														XPathConstants.STRING);
										//OMNI-82218 - XPath Correction - Ends

										if (null != strPrevContnrPriority
												&& !YRCPlatformUI
														.isVoid(strPrevContnrPriority)) {

											AcademySIMTraceUtil
													.logMessage("Prev Container Priority: "
															+ strPrevContnrPriority);

											if (Integer
													.parseInt(strContnrPriority) > Integer
													.parseInt(strPrevContnrPriority)) {

												AcademySIMTraceUtil
														.logMessage("Setting New Container Type::");

												String strContainerVal = getItemDesc(strContainer);

												if (null != strContainerVal
														&& !YRCPlatformUI
																.isVoid(strContainerVal)) {
													this
															.setFieldValue(
																	"extn_TxtScanContnrType",
																	strContainerVal);
													this
															.setFieldValue(
																	"extn_ContainerSelected",
																	strContainer);
												} else {
													if (AcademyPCAConstants.AMMO_SHIPMENT_TYPE
															.equalsIgnoreCase(this.strShipmentType)
															|| AcademyPCAConstants.HAZMAT_SHIPMENT_TYPE
																	.equalsIgnoreCase(this.strShipmentType)
																	//OMNI-7980
																	|| isSTSAmmoOrHazmatShipment) {
																	//OMNI-7980

														this
																.setFieldValue(
																		"extn_TxtScanContnrType",
																		"Select an Ammo container !");
														
														this.setFieldValue("extn_ContainerSelected", " ");
														
													} else {
														this
																.setFieldValue(
																		"extn_TxtScanContnrType",
																		"Select a Non-Ammo container !");
														
														this.setFieldValue("extn_ContainerSelected", " ");
													}
												}
											}
										}
									} else {

										AcademySIMTraceUtil
												.logMessage("Setting Container Type::");
										String strContainerVal = getItemDesc(strContainer);

										if (null != strContainerVal
												&& !YRCPlatformUI
														.isVoid(strContainerVal)) {
											this.setFieldValue(
													"extn_TxtScanContnrType",
													strContainerVal);
											this.setFieldValue(
													"extn_ContainerSelected",
													strContainer);
										} else {
											if (AcademyPCAConstants.AMMO_SHIPMENT_TYPE
													.equalsIgnoreCase(this.strShipmentType)
													|| AcademyPCAConstants.HAZMAT_SHIPMENT_TYPE
															.equalsIgnoreCase(this.strShipmentType)
															//OMNI-7980
															|| isSTSAmmoOrHazmatShipment) {
															//OMNI-7980

												this
														.setFieldValue(
																"extn_TxtScanContnrType",
																"Select an Ammo container !");
												
												this.setFieldValue("extn_ContainerSelected", " ");
												
											} else {
												this
														.setFieldValue(
																"extn_TxtScanContnrType",
																"Select a Non-Ammo container !");
												
												this.setFieldValue("extn_ContainerSelected", " ");
												
											}
										}
									}
								}else{
									this.setFieldValue("extn_ContainerSelected", " ");
									this.setFieldValue("extn_TxtScanContnrType", " ");
								}
							}
						}else{
							this.setFieldValue("extn_ContainerSelected", " ");
							this.setFieldValue("extn_TxtScanContnrType", " ");
						}
					}
				}

			}

			if (context.getApiName().equals("AcadListDPContainerPriority")) {
				AcademySIMTraceUtil
						.logMessage("Start handleApiCompletion :: AcadListDPContainerPriority");

				Element eleOutDPContnrPriority = context.getOutputXml()
						.getDocumentElement();
				if (null != eleOutDPContnrPriority) {

					AcademySIMTraceUtil
							.logMessage("AcadListDPContainerPriority API Output : "
									+ AcademySIMTraceUtil
											.getElementXMLString(eleOutDPContnrPriority));

					setExtentionModel("Extn_containerPriority", context
							.getOutputXml().getDocumentElement());
				}
			}

			if (context.getApiName().equals("AcadGetContainerThreshold")) {
				AcademySIMTraceUtil
						.logMessage("Start handleApiCompletion :: AcadListDPContainerPriority");

				Element eleOutDPContnrThreshold = context.getOutputXml()
						.getDocumentElement();
				if (null != eleOutDPContnrThreshold) {

					AcademySIMTraceUtil
							.logMessage("AcadGetContainerThreshold API Output : "
									+ AcademySIMTraceUtil
											.getElementXMLString(eleOutDPContnrThreshold));

					setExtentionModel("Extn_containerThreshold", context
							.getOutputXml().getDocumentElement());
				}
			}
			// EFP-7 : End
		}

		// /multibox SKU

		if (context.getApiName().equals("checkMultiboxItem")) {

			AcademySIMTraceUtil
					.logMessage("Start handleApiCompletion :: checkMultiboxItem");

			AcademySIMTraceUtil.logMessage("checkMultiboxItem API Output : "
					+ AcademySIMTraceUtil.getElementXMLString(context
							.getOutputXml().getDocumentElement()));

			multiBoxFlag = findMultiBoxFlagValue(context.getOutputXml()
					.getDocumentElement());
			if (multiBoxFlag) {
				setFieldsForMultiBox();

			}
		}

		// multibox sku
		AcademySIMTraceUtil.endMessage(CLASSNAME, "handleApiCompletion()");
		super.handleApiCompletion(context);
	}

	// Start WN-2980 GC Activation and fulfillment for SI DCs
	private void stampGiftCardSerialNo() {
		AcademySIMTraceUtil.logMessage("Entering stampGiftCardSerialNo()");
		Element eleGetShipmentDetailsModel = getModel(AcademyPCAConstants.GET_SHIPMENT_DETAILS_OOB_MODEL);
		String strItemID = getFieldValue(AcademyPCAConstants.OOB_TXT_LAST_SCANNED_ITEMID);
		String strSerialNo = getFieldValue(AcademyPCAConstants.CUSTOM_TXT_SERIALNO_SCAN);
		Element eleShipmentTagSerial = null;

		Element eleShipmentLine = (Element) YRCXPathUtils.evaluate(
				eleGetShipmentDetailsModel,
				"/Shipment/ShipmentLines/ShipmentLine[@ShipmentLineKey='"
						+ strSerialScanForShipmentLineKey + "']",
				XPathConstants.NODE);

		if (!YRCPlatformUI.isVoid(eleShipmentLine)) {
			double dBackroomPickedQuantity = 0;
			double dSerialScanCount = 0;
			String strBackroomPickedQuantity = eleShipmentLine
					.getAttribute(AcademyPCAConstants.BACKROOM_PICKED_QUANTITY_ATTR);
			String strPickedQuantity = eleShipmentLine
					.getAttribute(AcademyPCAConstants.PICK_QUANTITY_ATTR);
			String strQuantity = eleShipmentLine
					.getAttribute(AcademyPCAConstants.QUANTITY_ATTR);
			double dQuantity = Double.valueOf(strQuantity);
			String strSerialScanCount = eleShipmentLine
					.getAttribute(AcademyPCAConstants.SERIAL_SCAN_COUNT_ATTR);

			if (!YRCPlatformUI.isVoid(strBackroomPickedQuantity)) {
				dBackroomPickedQuantity = Double
						.valueOf(strBackroomPickedQuantity);
			}

			if (!YRCPlatformUI.isVoid(strSerialScanCount)) {
				dSerialScanCount = Double.valueOf(strSerialScanCount);

				/**
				 * If we scan serial number(S1) for 2nd item (Item2) and then
				 * scan 1st item(Item1), then last scanned shipment line's
				 * /ShipmentLine/@SerialScanCount getting stamped to the first
				 * line. As a workaround, we are resetting SerialScanCount
				 * attribute for 1st item scan Note: This looks like a Product
				 * issue.
				 */
				if (AcademyPCAConstants.ONE_DECIMAL_VAL
						.equals(strPickedQuantity)) {
					eleShipmentTagSerial = (Element) YRCXPathUtils.evaluate(
							eleShipmentLine,
							"ShipmentTagSerials/ShipmentTagSerial[@ShipmentLineKey='"
									+ strSerialScanForShipmentLineKey + "']",
							XPathConstants.NODE);
					if (YRCPlatformUI.isVoid(eleShipmentTagSerial))
						dSerialScanCount = 0;
				}
			}

			enableField(AcademyPCAConstants.OOB_TXT_SCAN_ITEMID);
			enableField(AcademyPCAConstants.CUSTOM_OVERRIDE_BTN_NEXT);
			enableField(AcademyPCAConstants.OOB_BTN_CONFIRM);

			dSerialScanCount++;

			eleShipmentLine.setAttribute(
					AcademyPCAConstants.SERIAL_SCAN_COUNT_ATTR, Double
							.toString(dSerialScanCount));
			eleShipmentLine.setAttribute(
					AcademyPCAConstants.PICK_QUANTITY_ATTR, Double
							.toString(dSerialScanCount));

			repopulateModel(AcademyPCAConstants.GET_SHIPMENT_DETAILS_OOB_MODEL);

			if (dQuantity == (dBackroomPickedQuantity + dSerialScanCount)) {
				disableField(AcademyPCAConstants.CUSTOM_TXT_SERIALNO_SCAN);
				setFocus(AcademyPCAConstants.OOB_TXT_SCAN_ITEMID);
				strSetFocusToField = AcademyPCAConstants.OOB_TXT_SCAN_ITEMID;
			} else {
				setFocus(AcademyPCAConstants.CUSTOM_TXT_SERIALNO_SCAN);
				strSetFocusToField = AcademyPCAConstants.CUSTOM_TXT_SERIALNO_SCAN;
			}

			Element eleShipmentTagSerials = (Element) eleShipmentLine
					.getElementsByTagName(
							AcademyPCAConstants.SHIPMENT_TAG_SERIALS_ELEMENT)
					.item(0);

			if (YRCPlatformUI.isVoid(eleShipmentTagSerials)) {
				eleShipmentTagSerials = YRCXmlUtils.createChild(
						eleShipmentLine,
						AcademyPCAConstants.SHIPMENT_TAG_SERIALS_ELEMENT);
			}
			eleShipmentTagSerial = YRCXmlUtils.createChild(
					eleShipmentTagSerials,
					AcademyPCAConstants.SHIPMENT_TAG_SERIAL_ELEMENT);
			eleShipmentTagSerial.setAttribute(
					AcademyPCAConstants.SERIAL_NO_ATTR, strSerialNo);
			eleShipmentTagSerial.setAttribute(
					AcademyPCAConstants.QUANTITY_ATTR,
					AcademyPCAConstants.ONE_DECIMAL_VAL);
			eleShipmentTagSerial
					.setAttribute(
							AcademyPCAConstants.SHIPMENT_LINE_KEY_ATTR,
							eleShipmentLine
									.getAttribute(AcademyPCAConstants.SHIPMENT_LINE_KEY_ATTR));
		}

		setFieldValue(AcademyPCAConstants.CUSTOM_TXT_LAST_SCANNED_SERIALNO,
				getFieldValue(AcademyPCAConstants.CUSTOM_TXT_SERIALNO_SCAN));
		setFieldValue(AcademyPCAConstants.OOB_TXT_SCAN_ITEMID, "");
		setFieldValue(AcademyPCAConstants.CUSTOM_TXT_SERIALNO_SCAN, "");
		isSerialScannedRequired = false;

		AcademySIMTraceUtil.logMessage("Exiting stampGiftCardSerialNo()");
	}

	// End WN-2980 GC Activation and fulfillment for SI DCs

	public static void hideField(String fieldName, Object extnBehavior) {
		GridData gridData = new GridData();
		gridData.exclude = true;
		YRCBaseBehavior parentBehavior = (YRCBaseBehavior) extnBehavior;
		parentBehavior.setControlVisible(fieldName, false);
		parentBehavior.setControlLayoutData(fieldName, gridData);
	}

    // Start OMNI-65686
    //hideFieldWithoutGridData - method created to hide the container drop down selection box
    public static void hideFieldWithoutGridData(String fieldName, Object extnBehavior) {
        GridData gridData = new GridData();
		gridData.exclude = false;
        YRCBaseBehavior parentBehavior = (YRCBaseBehavior) extnBehavior;
        parentBehavior.setControlVisible(fieldName, false);
        parentBehavior.setControlLayoutData(fieldName, gridData);
    }
    //End OMNI-65686


	protected void handleEvent(String fieldName, YRCEvent event) {
		String str1 = "handleEvent(fieldName, event)";
		AcademySIMTraceUtil.startMessage(CLASSNAME,
				"handleEvent(fieldName, event)");
//OMNI-66084 BEGIN
		if (this.pageBeingShown
				.equals("com.yantra.pca.ycd.rcp.tasks.backroompick.wizardpages.YCDBackroomPickShipmentSearch")) {
			if (fieldName.equals("extn_txtWarehouseContId") && event.keyCode == 13) {
				YRCPlatformUI
						.fireAction("com.yantra.pca.ycd.rcp.tasks.backroompick.actions.YCDBackroomPickSearchAction");
			}
		}
		//OMNI-66084 END
		if ((this.pageBeingShown
				.equals("com.yantra.pca.ycd.rcp.tasks.backroompick.wizardpages.YCDBackroomPick"))) {
			
			if ((fieldName.equals("txtItemID"))
					&& (!isFieldBlank(getFieldValue(fieldName)))
					&& ((event.detail == 16) || (event.keyCode == 13))){
				
				String scannedItemId = getFieldValue("txtItemID");
				System.out.println("Scanned ItemID is" + scannedItemId);
				AcademySIMTraceUtil
						.logMessage("Calling the callApiForMultiboxCheck for itemID:"
								+ scannedItemId);
				callApiForMultiboxCheck(scannedItemId);

				managePickedFieldsForScan(getModel("getShipmentDetails_output"));
				
			}
			
			AcademySIMTraceUtil.logMessage("Scan barcode UPC - Test 1::");
			
			 // start OMNI-443 enable ENTER key for SerialNo field
			 
			 AcademySIMTraceUtil.logMessage("handleEvent serialNo fieldName :: " + fieldName);
			AcademySIMTraceUtil.logMessage("handleEvent serialNo getFieldValue :: " + getFieldValue(fieldName));
			AcademySIMTraceUtil.logMessage("handleEvent serialNo event.keyCode :: " + event.keyCode);
			AcademySIMTraceUtil.logMessage("handleEvent serialNo event.detail :: " + event.detail);
			 
			if ((fieldName.equals("extn_txtSerialNo")) && (!isFieldBlank(getFieldValue(fieldName))) && ((event.detail == 16) || (event.keyCode == 13) || (event.keyCode == 16777296))) {

				String scannedSerialNo = getFieldValue("extn_txtSerialNo");
				System.out.println("Scanned SerialNo is" + scannedSerialNo);
				AcademySIMTraceUtil
					.logMessage("Calling the callApiForMultiboxCheck for SerialNo:"
							+ scannedSerialNo);
				String strItemID = getFieldValue("txtLastScannedItem");
				String strSerialNo = scannedSerialNo;

				Element eleGetShipmentDetailsModel = getModel("getShipmentDetails_output");
				String strShipNode = (String) YRCXPathUtils.evaluate(
					eleGetShipmentDetailsModel, "/Shipment/@ShipNode",
					XPathConstants.STRING);
				invokeTranslateBarCodeForSerialNo(strSerialNo, strItemID,
					strShipNode);

			}
		// end OMNI-443 enable ENTER key for SerialNo field
			
			if((fieldName.equals("extn_TxtContnrBarcde"))
					&& (!isFieldBlank(getFieldValue(fieldName)))
					&& ((event.detail == 16) || (event.keyCode == 13))){
				
				AcademySIMTraceUtil.logMessage("Scan barcode UPC::");
				
				AcademySIMTraceUtil.logMessage("fieldName : " + fieldName);
				String strContainerUPCCode = this
						.getFieldValue("extn_TxtContnrBarcde");
				//OMNI-69284 BEGIN
				invokeTranslateBarcodeForContainers(strContainerUPCCode.toUpperCase());
				//OMNI-69284 END
			}
			
			AcademySIMTraceUtil.logMessage("Scan barcode UPC - Test 2::");
		}

		if ((this.pageBeingShown
				.equals("com.yantra.pca.ycd.rcp.tasks.backroompick.wizardpages.YCDBackroomPick"))
				&& (fieldName.equals("text2"))
				&& (!isFieldBlank(getFieldValue(fieldName)))
				&& (event.detail == 16)) {
			managePickedQtyForItemScanned(getModel("getShipmentDetails_output"));
		}

		AcademySIMTraceUtil.endMessage(CLASSNAME,
				"handleEvent(fieldName, event)");
	}

	private void managePickedQtyForItemScanned(Element shipmentElement) {
		String str1 = "managePickedQtyForItemScanned(shipmentElement)";
		AcademySIMTraceUtil.startMessage(CLASSNAME,
				"managePickedQtyForItemScanned(shipmentElement)");

		AcademySIMTraceUtil
				.logMessage("managePickedQtyForItemScanned(shipmentElement) :: shipDtlsModelSet = "
						+ this.shipDtlsModelSet + " :: BEGIN");

		NodeList shipmentLineNL = (NodeList) YRCXPathUtils.evaluate(
				shipmentElement, "/Shipment/ShipmentLines/ShipmentLine",
				XPathConstants.NODESET);

		for (int listIndex = 0; listIndex < shipmentLineNL.getLength(); listIndex++) {
			Element shipmentLineElement = (Element) shipmentLineNL
					.item(listIndex);

			String itemID = shipmentLineElement.getAttribute("ItemID");

			if (getFieldValue("txtLastItem").equals(itemID)) {
				double pickedQuantityVal = 0.0D;
				String pickedQuantity = shipmentLineElement
						.getAttribute("PickedQuantity1");
				if (!isFieldBlank(pickedQuantity)) {
					pickedQuantityVal = Double.parseDouble(pickedQuantity);
				}
				double backroomPickedQtyVal = Double
						.parseDouble(shipmentLineElement
								.getAttribute("BackroomPickedQuantity"));
				double quantityOrderedVal = Double
						.parseDouble(shipmentLineElement
								.getAttribute("Quantity"));
				if (pickedQuantityVal > 0.0D) {
					if (!this.shipDtlsModelSet) {
						if (quantityOrderedVal >= backroomPickedQtyVal
								+ pickedQuantityVal) {
							continue;
						}
						double correctedVal = 1.0D;
						shipmentLineElement.setAttribute("PickedQuantity1",
								String.valueOf(correctedVal));
						repopulateModel("ShipmentDetails");
					} else {
						this.shipDtlsModelSet = false;
					}
				}
			}
		}

		AcademySIMTraceUtil
				.logMessage("managePickedQtyForItemScanned(shipmentElement) :: shipDtlsModelSet = "
						+ this.shipDtlsModelSet + " :: END");
		AcademySIMTraceUtil.endMessage(CLASSNAME,
				"managePickedQtyForItemScanned(shipmentElement)");
	}

	private void managePickedFieldsForScan(Element shipmentElement) {
		String str1 = "managePickedFieldsForScan(shipmentElement)";
		AcademySIMTraceUtil.startMessage(CLASSNAME,
				"managePickedFieldsForScan(shipmentElement)");

		NodeList shipmentLineNL = (NodeList) YRCXPathUtils.evaluate(
				shipmentElement, "/Shipment/ShipmentLines/ShipmentLine",
				XPathConstants.NODESET);

		for (int listIndex = 0; listIndex < shipmentLineNL.getLength(); listIndex++) {
			Element shipmentLineElement = (Element) shipmentLineNL
					.item(listIndex);

			double pickedQuantityVal = 0.0D;
			String pickedQuantity = shipmentLineElement
					.getAttribute("PickedQuantity1");
			if (!isFieldBlank(pickedQuantity)) {
				pickedQuantityVal = Double.parseDouble(pickedQuantity);
			}
			String strBackroomPickedQuantity = shipmentLineElement
					.getAttribute("BackroomPickedQuantity");
			if (strBackroomPickedQuantity.equals("")) {
				strBackroomPickedQuantity = "0.0";
			}
			double backroomPickedQtyVal = Double
					.parseDouble(strBackroomPickedQuantity);
			double quantityOrderedVal = Double.parseDouble(shipmentLineElement
					.getAttribute("Quantity"));
			if ((pickedQuantityVal <= 0.0D)
					|| (quantityOrderedVal > backroomPickedQtyVal
							+ pickedQuantityVal)) {
				continue;
			}
			double correctedVal = quantityOrderedVal - backroomPickedQtyVal;
			shipmentLineElement.setAttribute("PickedQuantity1", String
					.valueOf(correctedVal));
			repopulateModel("ShipmentDetails");
		}

		AcademySIMTraceUtil.endMessage(CLASSNAME,
				"managePickedFieldsForScan(shipmentElement)");
	}

	public void postSetModel(String nameSpace) {
		String str1 = "postSetModel(nameSpace)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, "postSetModel(nameSpace)");
		AcademySIMTraceUtil.logMessage("List of namespaces in post set model::"
				+ nameSpace);

		if (nameSpace.equals("ShortageResolution_input")) {
			AcademySIMTraceUtil
					.logMessage("postSetModel(nameSpace) :: NameSpace "
							+ nameSpace + " :: BEGIN");

			Element shipmentElement = getModel(nameSpace);

			shipmentElement.setAttribute("ContainerType", this.containerType);
			shipmentElement.setAttribute("ContainerGrossWeight", this.weight);
			shipmentElement.setAttribute("IsCompletePick", "N");
			String itemKeyXpathStr = "/ItemList/Item[@ItemID='"
					+ this.containerType + "']/@" + "ItemKey";
			String cntrTypeKey = (String) YRCXPathUtils.evaluate(
					getModel("Extn_getItemList_Output"), itemKeyXpathStr,
					XPathConstants.STRING);
			shipmentElement.setAttribute("ContainerTypeKey", cntrTypeKey);
			// creating <Extn/> under the <Shipment> since that is not exist in
			// OOB SOM Model
			Element eleExtn = shipmentElement.getOwnerDocument().createElement(
					"Extn");

			shipmentElement.appendChild(eleExtn);
			AcademySIMTraceUtil.logMessage("postSetModel(" + nameSpace
					+ "): \n", shipmentElement);
			repopulateModel(nameSpace);

			AcademySIMTraceUtil
					.logMessage("postSetModel(nameSpace) :: NameSpace "
							+ nameSpace + " :: END");
		}

		if (nameSpace.equals("getShipmentDetails_output")) {
			AcademySIMTraceUtil
					.logMessage("postSetModel(nameSpace) :: NameSpace "
							+ nameSpace + " :: BEGIN");

			// Start - - STL-733 - Specific containers for AMMO items

			if ((pageBeingShown
					.equals("com.yantra.pca.ycd.rcp.tasks.backroompick.wizards.YCDBackroompickWizard"))
					|| (pageBeingShown
							.equals("com.yantra.pca.ycd.rcp.tasks.backroompick.wizardpages.YCDBackroomPick"))
					|| (pageBeingShown
							.equals("com.yantra.pca.ycd.rcp.tasks.backroompick.wizardpages.YCDBackroomPickShipmentSearch"))) {
				
								
				AcademySIMTraceUtil.logMessage("Check For AMMO/HAZMAT");
				final Element eleShipment = this
						.getModel("getShipmentDetails_output");
				AcademySIMTraceUtil.logMessage("eleShipment :"
						+ YRCXmlUtils.getString(eleShipment));

				/*
				 * NodeList shipmentLineNL =
				 * eleShipment.getElementsByTagName("ShipmentLine");
				 * 
				 * for (int i=0;i<shipmentLineNL.getLength();i++) { Element
				 * shipmentEle = (Element) shipmentLineNL.item(i); String
				 * currentPickQty = shipmentEle.getAttribute("PickQuantity");
				 * 
				 * AcademySIMTraceUtil.logMessage("OLD Current Value::"+
				 * currentPickQty); if (currentPickQty.equals("0")) {
				 * StringBuilder strPickQty = new StringBuilder(currentPickQty);
				 * strPickQty.append(".00");
				 * shipmentEle.setAttribute("PickQuantity",
				 * String.valueOf(strPickQty)); } else { StringBuilder
				 * strPickQty = new StringBuilder(currentPickQty);
				 * strPickQty.append("0");
				 * shipmentEle.setAttribute("PickQuantity",
				 * String.valueOf(strPickQty)); }
				 * 
				 * AcademySIMTraceUtil.logMessage("changed current Value::"+
				 * shipmentEle.getAttribute("PickQuantity")); }
				 */

				repopulateModel("getShipmentDetails_output");
				strShipmentType = eleShipment.getAttribute("ShipmentType");
				//Start : OMNI-6616 : Changes for STS
				if(AcademyPCAConstants.STR_STS.equals(strShipmentType)) {
					strShipmentType  = (String) YRCXPathUtils.evaluate(
							eleShipment, "/Shipment/ShipmentLines/ShipmentLine/OrderLine/@FulfillmentType",
							XPathConstants.STRING);
					AcademySIMTraceUtil.logMessage("Updated ShipmentType :: " + strShipmentType);
				}
				//End : OMNI-6616 : Changes for STS

				// START : STL-1300 : validate ExtnExeterContainerId. Block
				// backroom pick if ExtnExeterContainerId not received.
				/*
				 * if(!isExtnExeterContainerIdNotReceived &&
				 * isExtnExeterContainerIdNotReceived(eleShipment)){
				 * isExtnExeterContainerIdNotReceived = true;
				 * showError("ERROR_EXTN_EXETER_CONTAINER_ID_IS_NOT_RECEIVED"); }
				 */
				if (!isExtnExeterContainerIdNotReceived) {
					isExtnExeterContainerIdNotReceived(eleShipment);
				}
				// END : STL-1300 : validate ExtnExeterContainerId. Block
				// backroom pick if ExtnExeterContainerId not received.

				if (!YRCPlatformUI.isVoid(strShipmentType)) {

					callApiForContainerTypes();
				}
			}
			// End - STL-733 - Specific containers for AMMO items

			if (this.pageBeingShown
					.equals("com.yantra.pca.ycd.rcp.tasks.backroompick.wizardpages.YCDBackroomPick")) {
				if (!this.shipDtlsModelSet)
					this.shipDtlsModelSet = true;
				else {
					this.shipDtlsModelSet = false;
				}

				Element eleExtnItemDetailList = getModel("Extn_ItemDetail_List");

				if (YRCPlatformUI.isVoid(eleExtnItemDetailList)) {
					Element eleShipmentDtls = getModel(nameSpace);
					AcademySIMTraceUtil.logMessage("ShipmentDetailModel",
							eleShipmentDtls);
					// creating EXTN Element under <Shipment> , since SOM OOB
					// MOdel does not conatin
					Element eleExtn = eleShipmentDtls.getOwnerDocument()
							.createElement("Extn");

					eleShipmentDtls.appendChild(eleExtn);

					repopulateModel(nameSpace);
					AcademySIMTraceUtil
							.logMessage("Shipment Dtls after EXTN ::"
									+ eleShipmentDtls);

					NodeList nlItemList = eleShipmentDtls
							.getElementsByTagName("Item");

					callApiForItemDetails(nlItemList);
				}

			}
			setControlEditable("extn_clmCustomPick", true);
			AcademySIMTraceUtil
					.logMessage("postSetModel(nameSpace) :: NameSpace "
							+ nameSpace + " :: END");
		}

		AcademySIMTraceUtil.endMessage(CLASSNAME, "postSetModel(nameSpace)");
		super.postSetModel(nameSpace);
	}

	// Start WN-2980 GC Activation and fulfillment for SI DCs
	private void invokeTranslateBarCodeForSerialNo(String strSerialNo,
			String strItemID, String strShipNode) {
		AcademySIMTraceUtil
				.logMessage("Entering prepareTranslateBarCodeForSerialNoInput");

		Document docInTranslateBarCode = YRCXmlUtils
				.createDocument(AcademyPCAConstants.BARCODE_ELEMENT);
		Element eleInTranslateBarCode = docInTranslateBarCode
				.getDocumentElement();
		eleInTranslateBarCode.setAttribute(
				AcademyPCAConstants.BARCODE_DATA_ATTR, strSerialNo);
		eleInTranslateBarCode.setAttribute(
				AcademyPCAConstants.BARCODE_TYPE_ATTR,
				AcademyPCAConstants.BARCODE_TYPE_SERIALSCAN_VAL);
		Element eleContextualInfo = YRCXmlUtils.createChild(
				eleInTranslateBarCode,
				AcademyPCAConstants.CONTEXTUAL_INFO_ELEMENT);
		eleContextualInfo.setAttribute(
				AcademyPCAConstants.ENTERPRISE_CODE_ATTR,
				AcademyPCAConstants.ENTERPRISE_CODE);
		eleContextualInfo.setAttribute(AcademyPCAConstants.ORG_CODE_ATTR,
				strShipNode);
		Element eleItemContextualInfo = YRCXmlUtils.createChild(
				eleInTranslateBarCode,
				AcademyPCAConstants.ITEM_CONTEXTUAL_INFO_ELEMENT);
		eleItemContextualInfo.setAttribute(
				AcademyPCAConstants.INVENTORY_UOM_ATTR,
				AcademyPCAConstants.STR_EACH);
		eleItemContextualInfo.setAttribute(AcademyPCAConstants.ITEM_ID_ATTR,
				strItemID);
		AcademySIMTraceUtil
				.logMessage("translateBarCodeForSerialNo API Input : "
						+ AcademySIMTraceUtil
								.getElementXMLString(eleInTranslateBarCode));

		YRCApiContext context = new YRCApiContext();
		context.setApiName(AcademyPCAConstants.TRANSLATE_BARCODE_API);
		context.setFormId(getFormId());
		context.setInputXml(docInTranslateBarCode);
		AcademySIMTraceUtil
				.logMessage("Before translateBarCodeForSerialNo API call");
		callApi(context);
		AcademySIMTraceUtil
				.logMessage("After translateBarCodeForSerialNo API call");
	}

	// End WN-2980 GC Activation and fulfillment for SI DCs

	public YRCValidationResponse validateTextField(String fieldName,
			String fieldValue) {
		String str1 = "validateTextField(fieldName,fieldValue)";
		AcademySIMTraceUtil.startMessage(CLASSNAME,
				"validateTextField(fieldName,fieldValue)");
		YRCValidationResponse response = super.validateButtonClick(fieldName);
		//start OMNI-443 Commented to scan GC item using UPC (this part add in postCommand() method)
		// START WN-2980 GC Activation and fulfillment for SI DCs
		/*if (fieldName.equals(AcademyPCAConstants.OOB_TXT_SCAN_ITEMID)) {
			if (AcademyPCAConstants.STRING_Y.equals(strIsSharedInvNode)) {
				double dBackroomPickedQuantity = 0;
				String strItemID = getFieldValue(AcademyPCAConstants.OOB_TXT_SCAN_ITEMID);
				Element eleGetShipmentDetailsModel = getModel(AcademyPCAConstants.GET_SHIPMENT_DETAILS_OOB_MODEL);

				NodeList nlShipmentLine = (NodeList) YRCXPathUtils
						.evaluate(
								eleGetShipmentDetailsModel,
								"/Shipment/ShipmentLines/ShipmentLine[@ItemID='"
										+ strItemID
										+ "' and OrderLine/Item/Extn/@ExtnIsGiftCard='Y']",
								XPathConstants.NODESET);

				for (int iShipmentLineCounter = 0; (iShipmentLineCounter < nlShipmentLine
						.getLength() && !isSerialScannedRequired); iShipmentLineCounter++) {
					Element XMLeleShipmentLine = (Element) nlShipmentLine
							.item(iShipmentLineCounter);
					if (!YRCPlatformUI.isVoid(XMLeleShipmentLine)) {
						strSerialScanForShipmentLineKey = XMLeleShipmentLine
								.getAttribute(AcademyPCAConstants.SHIPMENT_LINE_KEY_ATTR);

						String strBackroomPickedQuantity = XMLeleShipmentLine
								.getAttribute(AcademyPCAConstants.BACKROOM_PICKED_QUANTITY_ATTR);
						String strQuantity = XMLeleShipmentLine
								.getAttribute(AcademyPCAConstants.QUANTITY_ATTR);
						String strPickQuantity = XMLeleShipmentLine
								.getAttribute(AcademyPCAConstants.PICK_QUANTITY_ATTR);
						double dQuantity = Double.valueOf(strQuantity);
						double dPickQuantity = Double.valueOf(strPickQuantity);

						if (!YRCPlatformUI.isVoid(strBackroomPickedQuantity)) {
							dBackroomPickedQuantity = Double
									.valueOf(strBackroomPickedQuantity);
						}

						if (dQuantity == (dBackroomPickedQuantity + dPickQuantity)) {
							AcademySIMTraceUtil
									.logMessage("Line Completely Picked!!!!");

							setFocus(AcademyPCAConstants.OOB_TXT_SCAN_ITEMID);
						} else {
							isSerialScannedRequired = true;
							disableField(AcademyPCAConstants.OOB_TXT_SCAN_ITEMID);
							disableField(AcademyPCAConstants.CUSTOM_OVERRIDE_BTN_NEXT);
							disableField(AcademyPCAConstants.OOB_BTN_CONFIRM);

							enableField(AcademyPCAConstants.CUSTOM_TXT_SERIALNO_SCAN);

							setFocus(AcademyPCAConstants.CUSTOM_TXT_SERIALNO_SCAN);
							strSetFocusToField = AcademyPCAConstants.CUSTOM_TXT_SERIALNO_SCAN;
						}
					}
				}
				if (!isSerialScannedRequired) {
					disableField(AcademyPCAConstants.CUSTOM_TXT_SERIALNO_SCAN);
				}
			}
		}*/ // end OMNI-443 Commented to scan GC item using UPC (this part add in postCommand() method)
		if (fieldName.equals(AcademyPCAConstants.CUSTOM_TXT_SERIALNO_SCAN)) {
			String strItemID = getFieldValue(AcademyPCAConstants.OOB_TXT_LAST_SCANNED_ITEMID);
			String strSerialNo = fieldValue;
			Element eleGetShipmentDetailsModel = getModel(AcademyPCAConstants.GET_SHIPMENT_DETAILS_OOB_MODEL);
			String strShipNode = (String) YRCXPathUtils.evaluate(
					eleGetShipmentDetailsModel,
					AcademyPCAConstants.SHIPMENT_SHIPNODE_ATTR_XPATH,
					XPathConstants.STRING);

			Element eleIsDuplicateSerialNo = (Element) YRCXPathUtils
					.evaluate(
							eleGetShipmentDetailsModel,
							"/Shipment/ShipmentLines/ShipmentLine/ShipmentTagSerials/ShipmentTagSerial[@SerialNo='"
									+ strSerialNo + "']", XPathConstants.NODE);

			if (!YRCPlatformUI.isVoid(eleIsDuplicateSerialNo)) {
				String errorMessage = YRCPlatformUI
						.getString("ERROR_EXTN_DUPLICATE_SERIALNO")
						+ strSerialNo;
				response = showError(errorMessage);
				AcademySIMTraceUtil.logMessage("Duplicate SerialNo Scanned");
				return response;
			} else {
				AcademySIMTraceUtil.logMessage("Scanned SerialNo is unique");
				// start OMNI-443 Commented as we are already invoking this in handleEvnet() methodwhile enabling ENTER key for SerialNo field
			//	invokeTranslateBarCodeForSerialNo(strSerialNo, strItemID,strShipNode);
			// end OMNI-443
			}
		}
		// END WN-2980 GC Activation and fulfillment for SI DCs

		if ((fieldName.equals("extn_txtWeight"))
				|| (fieldName.equals("extn_txtWeight_scan"))) {
			AcademySIMTraceUtil.logMessage("fieldName : " + fieldName);
			if (!isFieldBlank(fieldValue)) {
				if (!fieldValue.matches("^(\\+)?[0-9]*(\\.[0-9]+)?$")) {
					String errorMessage = YRCPlatformUI
							.getString("CONTAINER_WEIGHT_VAL_ERR_MSG_KEY");
					response = showError(errorMessage);
					setFocus(fieldName);
					setFieldValue(fieldName, "");
				}
			}
		}
		/*
		 * if (fieldName.equals("extn_txtUPCCode")){ showError("Happy"); }
		 */

		// EFP-5 - Start
/*		if (fieldName.equals("extn_TxtContnrBarcde")) {
			AcademySIMTraceUtil.logMessage("fieldName : " + fieldName);
			String strContainerUPCCode = this
					.getFieldValue("extn_TxtContnrBarcde");
			invokeTranslateBarcodeForContainers(strContainerUPCCode);

		}*/
		// EFP-5 - End

		AcademySIMTraceUtil.endMessage(CLASSNAME,
				"validateTextField(fieldName,fieldValue)");
		return response;
	}

	private YRCValidationResponse showError(String errorMessage) {
		YRCFormatResponse response = new YRCFormatResponse(3, errorMessage,
				null);
		YRCPlatformUI.showError("Error", errorMessage);
		return response;
	}

	public YRCValidationResponse validateButtonClick(String fieldName) {
		String str1 = "validateButtonClick(fieldName)";
		AcademySIMTraceUtil.startMessage(CLASSNAME,
				"validateButtonClick(fieldName)");

		YRCValidationResponse response = super.validateButtonClick(fieldName);
		strSelectedButton = fieldName;
		if (fieldName.equals("extn_btnPickNext")) {
			AcademySIMTraceUtil
					.logMessage("validateButtonClick(fieldName) called for : "
							+ fieldName);
			AcademySIMTraceUtil.logMessage("this.completePickDone::"
					+ this.completePickDone);

			// Start WN-2980 GC Activation and fulfillment for SI DCs
			removeDuplicateSerialTags();
			// End WN-2980 GC Activation and fulfillment for SI DCs

			// START WN-2346 Retain user selected pack station
			String strSelectedPackStation = getFieldValue(AcademyPCAConstants.CUSTOM_COMBO_PRINTER_ID);
			if (!YRCPlatformUI.isVoid(strSelectedPackStation)) {
				YRCPlatformUI.getUserElement().setAttribute(
						AcademyPCAConstants.ATTR_DEFAULT_PRINTER,
						strSelectedPackStation);
				AcademySIMTraceUtil.logMessage("DefaultPrinter::"
						+ strSelectedPackStation);
			}// END WN-2346 Retain user selected pack station

			// Start - STL-733 - Specific containers for AMMO items
			// Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat
			// Implementation
			if (AcademyPCAConstants.AMMO_SHIPMENT_TYPE
					.equalsIgnoreCase(strShipmentType)
					|| AcademyPCAConstants.HAZMAT_SHIPMENT_TYPE
							.equalsIgnoreCase(strShipmentType)
							//OMNI-7980
							|| isSTSAmmoOrHazmatShipment) {
							//OMNI-7980
				// End WN-214, WN-216, WN-217, WN-211, WN-419 Implementation
				final String strWeight = this
						.getFieldValue("extn_txtWeight_scan");

				// shn-2
				// final String strContainerType =
				// this.getFieldValue("extn_cmbContainerType_scan");
				/*
				 * final String strContainerType = this
				 * .getFieldValue("extn_TxtScanContnrType");
				 */
				final String strContainerType = this
						.getFieldValue("extn_ContainerSelected");
					
				Element eleItemList = this.getModel("Extn_getItemList_Output");
				String strMaxCntrWeight = "";
				
				if("VendorPackage".equalsIgnoreCase(strContainerType)){
					
					strMaxCntrWeight = "0";
				}else{
					
					NodeList itemsList = eleItemList.getElementsByTagName("Item");
					int noOfItems = itemsList.getLength();

					for (int counter = 0; counter < noOfItems; counter++) {
						Element eleItem = (Element) itemsList.item(counter);
						String itemID = eleItem.getAttribute("ItemID");
						if (itemID.equals(strContainerType)) {
							Element eleContainerInformation = (Element) eleItem
									.getElementsByTagName("ContainerInformation")
									.item(0);
							strMaxCntrWeight = eleContainerInformation
									.getAttribute("MaxCntrWeight");
							break;
						}
					}
					// Start changes for STL-903
					if (!"".equals(strWeight) && null != strWeight
							&& !"".equals(strMaxCntrWeight)
							&& null != strMaxCntrWeight) {
						if (Float.parseFloat(strWeight) > Float
								.parseFloat(strMaxCntrWeight)) {
							YRCPlatformUI.showError("Error",
									"Maximum weight allowed for the selected Container Type is "
											+ strMaxCntrWeight + " LBS");
							response = new YRCFormatResponse(3, "Header", null);
							return response;
						}
					}
					// End changes
				}
				


			}
			// End - STL-733 - Specific containers for AMMO items

			if ((doProceedAction()) && (!this.completePickDone)) {
				AcademySIMTraceUtil.logMessage("Calling OOB NEXT BUtton");
				AcademySIMTraceUtil
						.logMessage("calling OOB Next Action : com.yantra.sop.SOPNextRecordBackroomAction :: START");

				YRCPlatformUI
						.fireAction(AcademyPCAConstants.BTN_NEXT_OOB_ACTION_ID);
				AcademySIMTraceUtil
						.logMessage("calling OOB Next Action : com.yantra.sop.SOPNextRecordBackroomAction :: END");
			}

			// else
			// {
			// AcademySIMTraceUtil.logMessage("Complete Pick Done************");
			// // YRCEditorInput editorInput =
			// (YRCEditorInput)((YRCEditorPart)YRCDesktopUI.getCurrentPart()).getEditorInput();
			// // AcademySIMTraceUtil.logMessage("Editor
			// Input::"+editorInput.getInputObject());
			// //
			// YRCPlatformUI.closeEditor("com.yantra.pca.ycd.rcp.editors.YCDShipmentEditor",
			// true);
			// //
			// YRCPlatformUI.openEditor("com.yantra.pca.ycd.rcp.editors.YCDShipmentEditor",
			// editorInput);
			//    	  
			// // showPreviousPage();
			// }

		}

		if (fieldName.equals("bttn1ShortageResolutionReason")) {
			strSelectedRadioButton = getFieldValue("bttn1ShortageResolutionReason");
			AcademySIMTraceUtil.logMessage("strSelectedRadioButton 1:: "
					+ strSelectedRadioButton);

		}
		if (fieldName.equals("bttn2ShortageResolution")) {
			strSelectedRadioButton = getFieldValue("bttn2ShortageResolution");
			AcademySIMTraceUtil.logMessage("strSelectedRadioButton 2:: "
					+ strSelectedRadioButton);

		}
		if (fieldName.equals("extn_btnPickConfirm")) {
			AcademySIMTraceUtil
					.logMessage("validateButtonClick(fieldName) called for : "
							+ fieldName);
			AcademySIMTraceUtil.logMessage("strSelectedRadioButton 3::"
					+ strSelectedRadioButton);
			doConfirmAction();
			// STL-1700
			removeShipmentKey();
		}

		// EFP-5 - Start
		if (fieldName.equals("extn_RadioContnrType")) {
			AcademySIMTraceUtil
					.logMessage("validateButtonClick(fieldName) called for : "
							+ fieldName);
			setFieldValue("extn_RadioContnrBarCde", false);
			setFocus("extn_cmbContainerType_scan");
			this.disableField("extn_TxtContnrBarcde");
			this.enableField("extn_cmbContainerType_scan");
			this.setFieldValue("extn_TxtScanContnrType", "");
			this.setFieldValue("extn_TxtContnrBarcde", "");
			this.setFieldValue("extn_ContainerSelected", "");

			//Start - OMNI-65686 Pack Station - Auto select container size - RCP SOM
			if (showVendorPackage) {
				this.setFieldValue("extn_txtWeight_scan", "");
				this.disableField("extn_txtWeight_scan");
				this.setFieldValue("extn_TxtScanContnrType", "VendorPackage");
				this.setFieldValue("extn_ContainerSelected", "VendorPackage");
			}
			//End - OMNI-65686 Pack Station - Auto select container size - RCP SOM

		}

		if (fieldName.equals("extn_RadioContnrBarCde")) {
			AcademySIMTraceUtil
					.logMessage("validateButtonClick(fieldName) called for : "
							+ fieldName);
			setFieldValue("extn_RadioContnrType", false);
			setFocus("extn_TxtContnrBarcde");
			setFieldValue("extn_cmbContainerType_scan", "");
			this.disableField("extn_cmbContainerType_scan");
			this.enableField("extn_TxtContnrBarcde");
			this.setFieldValue("extn_TxtScanContnrType", "");
			this.setFieldValue("extn_ContainerSelected", "");
			//Start : OMNI-69360 : Enable Weight field on toggle
			this.setFieldValue("extn_txtWeight_scan", "");
			this.enableField("extn_txtWeight_scan");
			//End : OMNI-69360 : Enable Weight field on toggle
		}
		// EFP-5 - End

		AcademySIMTraceUtil.endMessage(CLASSNAME,
				"validateButtonClick(fieldName)");
		return response;
	}

	// Start WN-2980 GC Activation and fulfillment for SI DCs
	/**
	 * If we scan serial number(S1) for 2nd item (Item2) and then scan 1st
	 * item(Item1), the first scanned serial number(In this case its S1) is
	 * getting tag to both Item1 and Item2. Note: If we scanned serial number
	 * for first item first, then there is no issue. However, first item might
	 * not be GC Item. Also same issue might be there for will pick scenario.
	 */
	private void removeDuplicateSerialTags() {
		Element eleGetShipmentDetailsModel = getModel(AcademyPCAConstants.GET_SHIPMENT_DETAILS_OOB_MODEL);
		if (eleGetShipmentDetailsModel.getElementsByTagName(
				AcademyPCAConstants.SHIPMENT_TAG_SERIALS_ELEMENT).getLength() > 0) {
			Element eleShipmentLine = (Element) eleGetShipmentDetailsModel
					.getElementsByTagName(AcademyPCAConstants.SHIPMENTLINE_ATTR)
					.item(0);
			String strShipmentLineKey = eleShipmentLine
					.getAttribute(AcademyPCAConstants.SHIPMENT_LINE_KEY_ATTR);
			int iShipmentTagSerialSize = 0;
			Element eleShipmentTagSerial = null;

			NodeList nlShipmentTagSerial = eleShipmentLine
					.getElementsByTagName(AcademyPCAConstants.SHIPMENT_TAG_SERIAL_ELEMENT);
			iShipmentTagSerialSize = nlShipmentTagSerial.getLength();
			AcademySIMTraceUtil.logMessage("Before modifying model:: "
					+ AcademySIMTraceUtil
							.getElementXMLString(eleGetShipmentDetailsModel));

			for (int iSerialTagCounter = 0; iSerialTagCounter < iShipmentTagSerialSize; iSerialTagCounter++) {
				eleShipmentTagSerial = (Element) nlShipmentTagSerial
						.item(iSerialTagCounter);

				if (!strShipmentLineKey
						.equals(eleShipmentTagSerial
								.getAttribute(AcademyPCAConstants.SHIPMENT_LINE_KEY_ATTR))) {

					/* */
					eleShipmentTagSerial.getParentNode().removeChild(
							eleShipmentTagSerial);
					iSerialTagCounter--;
					iShipmentTagSerialSize--;
				}
			}
			AcademySIMTraceUtil.logMessage("After modifying model:: "
					+ AcademySIMTraceUtil
							.getElementXMLString(eleGetShipmentDetailsModel));
		}
	}

	// End WN-2980 GC Activation and fulfillment for SI DCs

	public void doConfirmAction() {
		String str1 = "doConfirmAction()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, "doConfirmAction()");
		AcademySIMTraceUtil.logMessage("User selected ::"
				+ strSelectedRadioButton);
		// Element shipmentElement = getModel("ShipmentDetailsForResolution");
		// -- This Model is not available in SOM

		Element shipmentElement = getModel("ShortageResolution_input");
		AcademySIMTraceUtil.logMessage("shipmentElement:::::::"
				+ AcademySIMTraceUtil.getElementXMLString(shipmentElement));

		// since ShortageResolution_input model does not contain the Shipment #
		// , ShipNode and Shipment Key, updating the same to shipmentElement
		// from Model "getShipmentDetails_input"
		Element eleGetShipmentDetailsModel = getModel("getShipmentDetails_output");
		AcademySIMTraceUtil.logMessage("eleGetShipmentDetailsModel:::::::"
				+ AcademySIMTraceUtil
						.getElementXMLString(eleGetShipmentDetailsModel));

		shipmentElement.setAttribute("ShipmentNo", eleGetShipmentDetailsModel
				.getAttribute("ShipmentNo"));
		shipmentElement.setAttribute("ShipmentKey", eleGetShipmentDetailsModel
				.getAttribute("ShipmentKey"));
		shipmentElement.setAttribute("ShipNode", eleGetShipmentDetailsModel
				.getAttribute("ShipNode"));

		// STL-1737 setting DocumentType as this is must to make Vertex call
		// inside changeShipment UE
		shipmentElement.setAttribute(AcademyPCAConstants.DOCUMENT_TYPE_ATTR,
				eleGetShipmentDetailsModel
						.getAttribute(AcademyPCAConstants.DOCUMENT_TYPE_ATTR));

		// STL - 987 to get the Status in Shortage Resolution Input
		shipmentElement.setAttribute("Status", eleGetShipmentDetailsModel
				.getAttribute("Status"));

		// String strRadioSeltd =
		// getTargetModel("SelectedRadioButton").getAttribute("radioSelection");
		// --not exist in SOM
		shipmentElement.setAttribute("radioSelection", strSelectedRadioButton);

		AcademySIMTraceUtil.logMessage(" Modified shipmentElement:::::::"
				+ AcademySIMTraceUtil.getElementXMLString(shipmentElement));
		AcademySIMTraceUtil
				.logMessage(
						"validateButtonClick(extn_btnPickConfirm): doConfirmAction(): \n",
						shipmentElement);

		// if (strRadioSeltd.equals("InventoryShortage")) -- Name is different
		// in SOM
		if (strSelectedRadioButton.equals("BP_Inventory_Shortage")) {
			AcademySIMTraceUtil
					.logMessage("validateButtonClick(extn_btnPickConfirm): AcademyShortageResolutionConfirmPopup called :: BEGIN");

			// STL-679: Making Reason Code as mandatory in case of Inventory
			// Shortage: START
			if (YRCPlatformUI.isVoid(strShortpickReasonCode)) {
				showError("ERROR_EXTN_SHORTPICK_REASON_CODE");
			} else {
				// STL-679: Making Reason Code as mandatory in case of Inventory
				// Shortage: END
				AcademyShortageResolutionConfirmPopup resoPopup = new AcademyShortageResolutionConfirmPopup(
						new Shell(Display.getDefault()), 0);
				YRCDialog popupDialog = new YRCDialog(resoPopup, 300, 150,
						"Confirm Shortage Resolution", null);
				if (!YRCDialog.isDialogOpen()) {
					popupDialog.open();
				}

				AcademySIMTraceUtil
						.logMessage("validateButtonClick(extn_btnPickConfirm): AcademyShortageResolutionConfirmPopup called :: END");

				if (resoPopup.getSelectionConfirm()) {
					AcademySIMTraceUtil
							.logMessage("validateButtonClick(extn_btnPickConfirm): doConfirmAction(): Service called");
					shipmentElement.setAttribute("PrintPackId",
							strCommonComboValue);
					AcademySIMTraceUtil
							.logMessage("Printer Id selected in previous screen for inventoruy shortage is"
									+ strCommonComboValue);

					// STL-679: get the Reason Code selected and stamp it on
					// Shipment and send it to
					// AcademySFSCreateContainersAndPrintService
					AcademySIMTraceUtil.logMessage("Reason For Shortage:: "
							+ strShortpickReasonCode);
					shipmentElement.setAttribute("ExtnShortpickReasonCode",
							strShortpickReasonCode);

					callServiceForShipmentContainerProcessing(shipmentElement);
				}
			} // STL-679: Making Reason Code as mandatory in case of Inventory
			// Shortage
		}
		// else if(strRadioSeltd.equals("PickLater")) -- Name is different in
		// SOM
		else if (strSelectedRadioButton.equals("BP_Will_Pick_Later")) {
			AcademySIMTraceUtil
					.logMessage("validateButtonClick(extn_btnPickConfirm): doConfirmAction(): Service called for Will Pick Later");
			shipmentElement.setAttribute("PrintPackId", strCommonComboValue);
			AcademySIMTraceUtil
					.logMessage("Printer Id selected in previous screen for will pick later is"
							+ strCommonComboValue);
			callServiceForShipmentContainerProcessing(shipmentElement);
		}
		
		this.setFieldValue("extn_ContainerSelected", " ");
		this.setFieldValue("extn_TxtScanContnrType", " ");
		this.setFieldValue("extn_cmbContainerType_scan", "");
		
		AcademySIMTraceUtil.endMessage(CLASSNAME, "doConfirmAction()");
	}

	public boolean doProceedAction() {
		String str1 = "doProceedAction()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, "doProceedAction()");
		boolean proceedToNext = true;

		this.noPickDone = false;
		this.completePickDone = false;

		if (this.pageBeingShown
				.equals("com.yantra.pca.ycd.rcp.tasks.backroompick.wizards.YCDBackroompick")) {
			this.weight = getFieldValue("extn_txtWeight");
			// this.containerType = getFieldValue("extn_cmbContainerType");
			this.containerType = getFieldValue("extn_ContainerSelected");
		} else if (this.pageBeingShown
				.equals("com.yantra.pca.ycd.rcp.tasks.backroompick.wizardpages.YCDBackroomPick")) {
			this.weight = getFieldValue("extn_txtWeight_scan");
			// shn-3
			// this.containerType =
			// getFieldValue("extn_cmbContainerType_scan");
			this.containerType = getFieldValue("extn_ContainerSelected");
		}

		AcademySIMTraceUtil.logMessage("Container Weight: " + this.weight);
		AcademySIMTraceUtil.logMessage("Container Type: " + this.containerType);
		AcademySIMTraceUtil.logMessage("Multi Box: " + this.multiBoxFlag);
		AcademySIMTraceUtil
				.logMessage("Shipment Type: " + this.strShipmentType);
		
		Element shipmentElement = getModel("getShipmentDetails_output");
		AcademySIMTraceUtil.logMessage("getShipmentDetails_output:::::::"
				+ AcademySIMTraceUtil.getElementXMLString(shipmentElement));
		NodeList shipmentLineNL = (NodeList) YRCXPathUtils.evaluate(
				shipmentElement, "/Shipment/ShipmentLines/ShipmentLine",
				XPathConstants.NODESET);

		if ("VendorPackage".equalsIgnoreCase(this.containerType)) {
			this.weight = "0";
		}
		if ("VendorPackageMultibox".equalsIgnoreCase(this.containerType)) {
			System.out.println("Container type is VendorPackageMultibox");
			AcademySIMTraceUtil
					.logMessage("Container type is VendorPackageMultibox");
			this.weight = "0";
		}
		// start OMNI-443 send pop up alert if user tries to proceed next without giving SerialNo
		if (isSerialScannedRequired) {

			String strSerialNo = getFieldValue(AcademyPCAConstants.CUSTOM_TXT_SERIALNO_SCAN);

			String strLastScannedSerialNo = getFieldValue(AcademyPCAConstants.CUSTOM_TXT_LAST_SCANNED_SERIALNO);

			if (isFieldBlank(strSerialNo)
					|| isFieldBlank(strLastScannedSerialNo)) {

				String errorMessage = "Please Enter Valid Serial Number.";

				showError(errorMessage);

				proceedToNext = false;

				setFocus(AcademyPCAConstants.CUSTOM_TXT_SERIALNO_SCAN);
				return proceedToNext;
			}
		}
		// end OMNI-443 send pop up alert if user tries to proceed next without giving SerialNo

		AcademySIMTraceUtil.logMessage("Calling for WhiteGlove check.");
		for (int i = 0; i < shipmentLineNL.getLength(); i++) {
			Element eleShipmentLine = (Element) shipmentLineNL.item(i);

			if (eleShipmentLine != null) {
				Element XMLeleItem = (Element) YRCXPathUtils.evaluate(
						eleShipmentLine, "OrderLine/Item", XPathConstants.NODE);
				String isWhiteGlove = isWhiteGlove(XMLeleItem);
				if ("Y".equalsIgnoreCase(isWhiteGlove) && !multiBoxFlag) { // //multibox
																			// items
																			// will
																			// have
																			// Vendor
																			// package
																			// Multibox
																			// as
																			// container
																			// Type
					if (!"VendorPackage".equalsIgnoreCase(this.containerType)) {
						String errorMessage = "Must use Vendor Packaging for WG.";
						showError(errorMessage);
						proceedToNext = false;
						AcademySIMTraceUtil
								.logMessage("Must use Vendor Packaging for WG. Exiting button process action.");
						return proceedToNext;
					}
				}
			}
		}
		AcademySIMTraceUtil.logMessage("Done with WhiteGlove check.");

		if ((!isFieldBlank(this.weight)) && (!isFieldBlank(this.containerType))) {
			AcademySIMTraceUtil
					.logMessage("Weight and Container Type are not Blank");
			if (isAnyPickedQuantityEntered(shipmentLineNL)) {
				proceedToNext = true;
			} else
				proceedToNext = false;

			if (("VendorPackage".equalsIgnoreCase(this.containerType))
					&& (proceedToNext)) {
				for (int i = 0; i < shipmentLineNL.getLength(); i++) {
					Element eleShipmentLine = (Element) shipmentLineNL.item(i);

					String pickedQuantity = eleShipmentLine
							.getAttribute("PickedQuantity1");
					if (isFieldBlank(pickedQuantity)) {
						continue;
					}
					if (pickedQuantity.equals("0.0")) {
						continue;
					}
					Element eleItem = (Element) YRCXPathUtils.evaluate(
							eleShipmentLine, "OrderLine/Item",
							XPathConstants.NODE);
					String strItemType = getItemType(eleItem);
					if ("CON".equalsIgnoreCase(strItemType)) {
						String errorMessage = YRCPlatformUI
								.getString("VENDOR_PKG_CON_PICK_ERROR_MSG_KEY");
						showError(errorMessage);
						proceedToNext = false;
						break;
					}
					if (!"BULK".equalsIgnoreCase(strItemType)) {
						continue;
					}
					if (Double.valueOf(pickedQuantity).intValue() > 1) {
						String errorMessage = YRCPlatformUI
								.getString("VENDOR_PKG_MULTI_QTY_BLK_PICK_ERROR_MSG_KEY");
						showError(errorMessage);
						proceedToNext = false;
						break;
					}

				}

			}

			if ((validatePickForCall(shipmentLineNL))
					&& (proceedToNext == Boolean.TRUE.booleanValue())) {
				AcademySIMTraceUtil
						.logMessage("Printer_Id selected for Pack station is 1"
								+ getFieldValue("extn_PrinterIdComboBox"));
				String strComboboxValue = getFieldValue("extn_PrinterIdComboBox");
				if (strComboboxValue != null) {
					shipmentElement.setAttribute("PrintPackId",
							strComboboxValue);
					AcademySIMTraceUtil
							.logMessage("Printer_Id selected for Pack station is 2"
									+ strComboboxValue);
				} else {
					shipmentElement.setAttribute("PrintPackId",
							this.strSinglePckstnCodeValue);
					AcademySIMTraceUtil
							.logMessage("Printer_Id selected is for Pack station 3"
									+ this.strSinglePckstnCodeValue);
				}

				shipmentElement.setAttribute("ContainerType",
						this.containerType);
				shipmentElement.setAttribute("ContainerGrossWeight",
						this.weight);
				shipmentElement.setAttribute("IsCompletePick", "Y");
				String itemKeyXpathStr = "/ItemList/Item[@ItemID='"
						+ this.containerType + "']/@" + "ItemKey";
				String cntrTypeKey = (String) YRCXPathUtils.evaluate(
						getModel("Extn_getItemList_Output"), itemKeyXpathStr,
						XPathConstants.STRING);
				shipmentElement.setAttribute("ContainerTypeKey", cntrTypeKey);

				AcademySIMTraceUtil
						.logMessage("validateButtonClick(extn_btnPickNext): doProceedAction(): Service called");
				this.completePickDone = true;
				AcademySIMTraceUtil
						.logMessage("Modified Shipment Elememt***********"
								+ AcademySIMTraceUtil
										.getElementXMLString(shipmentElement));
				callServiceForShipmentContainerProcessing(shipmentElement);
			}
			if ((!validatePickForCall(shipmentLineNL))
					&& (proceedToNext == Boolean.TRUE.booleanValue())) {
				AcademySIMTraceUtil
						.logMessage("Going in to the If condition of Will pick later / Inv shortage");
				try {
					strComboboxValue = getFieldValue("extn_PrinterIdComboBox");
				} catch (Exception e) {
					e.printStackTrace();
				}
				AcademySIMTraceUtil
						.logMessage("Get Feild Value of Pack station is:"
								+ strComboboxValue);
				if (strComboboxValue != null) {
					strCommonComboValue = strComboboxValue;
					AcademySIMTraceUtil
							.logMessage("Get Feild Value of Pack station is: 1"
									+ strCommonComboValue);

				} else {
					strCommonComboValue = strSinglePckstnCodeValue;
					AcademySIMTraceUtil
							.logMessage("Get Feild Value of Pack station is: 2"
									+ strCommonComboValue);
				}

				AcademySIMTraceUtil
						.logMessage("Printer_Id selected is for Pack station "
								+ strCommonComboValue);
			}
		} else if (isAnyPickedQuantityEntered(shipmentLineNL)) {

			AcademySIMTraceUtil.logMessage("QTY Pick check");

			if (this.noPickDone) {
				proceedToNext = true;
			} else {

				AcademySIMTraceUtil.logMessage("Shipment Type::"
						+ this.strShipmentType);
				AcademySIMTraceUtil.logMessage("Container Type::"
						+ this.containerType);

				if ((AcademyPCAConstants.AMMO_SHIPMENT_TYPE
						.equalsIgnoreCase(this.strShipmentType) || AcademyPCAConstants.HAZMAT_SHIPMENT_TYPE
						.equalsIgnoreCase(this.strShipmentType) 
						//OMNI-7980
						|| isSTSAmmoOrHazmatShipment)
						//OMNI-7980
						&& !isValidAmmoNonAmmoContainer(this.containerType,
								true)
						&& !"VendorPackageMultibox"
								.equalsIgnoreCase(this.containerType)
						&& !"VendorPackage"
								.equalsIgnoreCase(this.containerType)) {

					String errorMessage = "Please select a Ammo Container to proceed";
					showError(errorMessage);

				} else if (!AcademyPCAConstants.AMMO_SHIPMENT_TYPE
						.equalsIgnoreCase(this.strShipmentType)
						&& !AcademyPCAConstants.HAZMAT_SHIPMENT_TYPE
								.equalsIgnoreCase(this.strShipmentType)
						&& !isValidAmmoNonAmmoContainer(this.containerType,
								false)
						&& !"VendorPackageMultibox"
								.equalsIgnoreCase(this.containerType)
						&& !"VendorPackage"
								.equalsIgnoreCase(this.containerType)
						//OMNI-7980		
						&& !isSTSAmmoOrHazmatShipment) {
						//OMNI-7980
					
					String errorMessage = "Please select a Non-Ammo Container to proceed";
					this.setFieldValue("extn_ContainerSelected", " ");
					this.setFieldValue("extn_TxtScanContnrType", " ");
					showError(errorMessage);

				} else {

					String errorMessage = YRCPlatformUI
							.getString("CONTAINER_WEIGHT_TYPE_ERR_MSG_KEY");
					this.setFieldValue("extn_ContainerSelected", " ");
					this.setFieldValue("extn_TxtScanContnrType", " ");
					showError(errorMessage);
				}

				proceedToNext = false;
			}
		} else {

			AcademySIMTraceUtil.logMessage("QTY Pick check - 1");

			String errorMessage = YRCPlatformUI
					.getString("CONTAINER_WEIGHT_TYPE_ERR_MSG_KEY");
			showError(errorMessage);
			proceedToNext = false;
		}

		AcademySIMTraceUtil.logMessage(
				"validateButtonClick(extn_btnPickNext): doProceedAction(): \n",
				shipmentElement);
		AcademySIMTraceUtil.endMessage(CLASSNAME, "doProceedAction()");
		AcademySIMTraceUtil.logMessage("doProceedAction() : " + proceedToNext
				+ " returned");
		return proceedToNext;
	}

	private String getItemType(Element eleItem) {
		String strItemType = null;
		Element eleClassificationCode = (Element) eleItem.getElementsByTagName(
				"ClassificationCodes").item(0);
		Element eleExtn = (Element) eleItem.getElementsByTagName("Extn")
				.item(0);
		String strStorageType = eleClassificationCode
				.getAttribute("StorageType");
		String strExtnWhiteGloveEligible = eleExtn
				.getAttribute("ExtnWhiteGloveEligible");
		if (strStorageType.startsWith("CON")) {
			strItemType = "CON";
		}
		if ((strStorageType.startsWith("NCON"))
				&& ("N".equalsIgnoreCase(strExtnWhiteGloveEligible))) {
			strItemType = "BULK";
		}
		return strItemType;
	}

	private String isWhiteGlove(Element eleItem) {
		String strExtnWhiteGloveEligible = "N";
		if (eleItem != null) {
			Element eleExtn = (Element) eleItem.getElementsByTagName("Extn")
					.item(0);
			if (eleExtn != null) {
				strExtnWhiteGloveEligible = eleExtn
						.getAttribute("ExtnWhiteGloveEligible");
			}
			AcademySIMTraceUtil.logMessage("isWhiteGlove : "
					+ strExtnWhiteGloveEligible);
		}
		return strExtnWhiteGloveEligible;
	}

	private boolean isPickedUOMEntered(Element shipmentLineElement) {
		String str1 = "isPickedUOMEntered(shipmentLineElement)";
		AcademySIMTraceUtil.startMessage(CLASSNAME,
				"isPickedUOMEntered(shipmentLineElement)");

		String pickedUOM = shipmentLineElement.getAttribute("PickQtyUOM");
		if (isFieldBlank(pickedUOM)) {
			AcademySIMTraceUtil.endMessage(CLASSNAME,
					"isPickedUOMEntered(shipmentLineElement)");
			AcademySIMTraceUtil
					.logMessage("isPickedUOMEntered(shipmentLineElement) : False returned");
			return false;
		}
		AcademySIMTraceUtil.endMessage(CLASSNAME,
				"isPickedUOMEntered(shipmentLineElement)");
		AcademySIMTraceUtil
				.logMessage("isPickedUOMEntered(shipmentLineElement) : True returned");
		return true;
	}

	private boolean isAnyPickedQuantityEntered(NodeList shipmentLineNL) {
		String str1 = "isAnyPickedQuantityEntered(shipmentLineNL)";
		AcademySIMTraceUtil.startMessage(CLASSNAME,
				"isAnyPickedQuantityEntered(shipmentLineNL)");

		boolean pickCheck = false;
		boolean uomCheck = false;

		for (int listIndex = 0; listIndex < shipmentLineNL.getLength(); listIndex++) {
			Element shipmentLineElement = (Element) shipmentLineNL
					.item(listIndex);
			String pickedQuantity = shipmentLineElement
					.getAttribute("PickQuantity");
			if (isFieldBlank(pickedQuantity)) {
				continue;
			}
			if (pickedQuantity.equals("0"))
				continue;
			if ((!uomCheck) && (!isPickedUOMEntered(shipmentLineElement))) {
				String errorMessage = YRCPlatformUI
						.getString("UOM_NOT_PICKED_ERR_MSG_KEY");
				showError(errorMessage);
				uomCheck = true;
			}
			pickCheck = true;
		}

		AcademySIMTraceUtil.endMessage(CLASSNAME,
				"isAnyPickedQuantityEntered(shipmentLineNL)");
		if (uomCheck) {
			AcademySIMTraceUtil
					.logMessage("isAnyPickedQuantityEntered(shipmentLineNL) : "
							+ (!uomCheck) + " returned");
			return !uomCheck;
		}
		if ((pickCheck == uomCheck)
				&& (pickCheck == Boolean.FALSE.booleanValue())) {
			AcademySIMTraceUtil
					.logMessage("isAnyPickedQuantityEntered(shipmentLineNL) : "
							+ (!pickCheck) + " returned");
			this.noPickDone = (!pickCheck);
			return !pickCheck;
		}
		AcademySIMTraceUtil
				.logMessage("isAnyPickedQuantityEntered(shipmentLineNL) : "
						+ pickCheck + " returned");
		return pickCheck;
	}

	private boolean validatePickForCall(NodeList shipmentLineNL) {
		String str1 = "validatePickForCall(shipmentLineNL)";
		AcademySIMTraceUtil.startMessage(CLASSNAME,
				"validatePickForCall(shipmentLineNL)");

		boolean validatePick = false;

		for (int listIndex = 0; listIndex < shipmentLineNL.getLength(); listIndex++) {
			Element shipmentLineElement = (Element) shipmentLineNL
					.item(listIndex);

			double pickedQuantityVal = 0.0D;
			String pickedQuantity = shipmentLineElement
					.getAttribute("PickQuantity");// SIM: PickedQuantity1
			if (!isFieldBlank(pickedQuantity)) {
				pickedQuantityVal = Double.parseDouble(pickedQuantity);
			}
			String strBackroomPickedQuantity = shipmentLineElement
					.getAttribute("BackroomPickedQuantity");
			if (strBackroomPickedQuantity.equals("")) {
				AcademySIMTraceUtil
						.logMessage("BackroomPickedQuantity is blank");
				strBackroomPickedQuantity = "0.00";
			}
			double backroomPickedQtyVal = Double
					.parseDouble(strBackroomPickedQuantity);
			double quantityOrderedVal = Double.parseDouble(shipmentLineElement
					.getAttribute("Quantity"));

			if (quantityOrderedVal == backroomPickedQtyVal + pickedQuantityVal) {
				AcademySIMTraceUtil.logMessage("[" + listIndex
						+ "] pickedQuantityVal: " + pickedQuantityVal
						+ " backroomPickedQtyVal " + backroomPickedQtyVal
						+ " quantityOrderedVal " + quantityOrderedVal);

				validatePick = true;
			} else {
				validatePick = false;
				break;
			}
		}
		AcademySIMTraceUtil.endMessage(CLASSNAME,
				"validatePickForCall(shipmentLineNL)");
		AcademySIMTraceUtil.logMessage("validatePickForCall(shipmentLineNL) : "
				+ validatePick + " returned");
		return validatePick;
	}

	private boolean isFieldBlank(String fieldValue) {
		return (fieldValue == null) || (fieldValue.trim().length() == 0);
	}

	public YRCValidationResponse validateLinkClick(String fieldName) {
		return super.validateLinkClick(fieldName);
	}

	public void validateComboField(String fieldName, String fieldValue) {
		if ("extn_cmbContainerType_scan".equalsIgnoreCase(fieldName)) {
			if ("VendorPackage".equalsIgnoreCase(fieldValue)) {
				setFieldValue("extn_txtWeight_scan", "");
				disableField("extn_txtWeight_scan");
				setFieldValue("extn_TxtScanContnrType", fieldValue);

			} else {
				enableField("extn_txtWeight_scan");
				setFieldValue("extn_TxtScanContnrType", getItemDesc(fieldValue));

			}

			setFieldValue("extn_ContainerSelected", fieldValue);

		}
		// STL-679: Making Reason Code as mandatory in case of Inventory
		// Shortage: START
		if (fieldName.equals("extn_InvShortage_Reason_Code")) {
			strShortpickReasonCode = getFieldValue("extn_InvShortage_Reason_Code");
			AcademySIMTraceUtil.logMessage("Reason Code selected :: "
					+ strShortpickReasonCode);
		}
		// STL-679: Making Reason Code as mandatory in case of Inventory
		// Shortage: END
		super.validateComboField(fieldName, fieldValue);
	}

	private Document prepareGetItemListInput(NodeList nlItemList) {
		AcademySIMTraceUtil.startMessage(CLASSNAME,
				"prepareGetItemListInput(nlItemList)");

		Document getItemListInputDoc = YRCXmlUtils.createDocument("Item");
		Element rootElement = getItemListInputDoc.getDocumentElement();

		if (nlItemList.getLength() == 1) {
			Element eleItem = (Element) nlItemList.item(0);
			rootElement.setAttribute("ItemID", eleItem.getAttribute("ItemID"));
		} else {
			Element complexQryElement = getItemListInputDoc
					.createElement("ComplexQuery");
			complexQryElement.setAttribute("Operator", "AND");
			Element complexAndElement = getItemListInputDoc
					.createElement("And");
			Element complexOrElement = getItemListInputDoc.createElement("Or");

			for (int listIndex = 0; listIndex < nlItemList.getLength(); listIndex++) {
				Element eleItem = (Element) nlItemList.item(listIndex);
				Element expElement = getItemListInputDoc.createElement("Exp");
				expElement.setAttribute("Name", "ItemID");
				expElement
						.setAttribute("Value", eleItem.getAttribute("ItemID"));
				complexOrElement.appendChild(expElement);
			}

			complexAndElement.appendChild(complexOrElement);
			complexQryElement.appendChild(complexAndElement);
			rootElement.appendChild(complexQryElement);
		}

		AcademySIMTraceUtil.logMessage("prepareGetItemListInput",
				getItemListInputDoc);

		AcademySIMTraceUtil.endMessage(CLASSNAME,
				"prepareGetItemListInput(nlItemList)");
		return getItemListInputDoc;
	}

	private void callApiForItemDetails(NodeList nlItemList) {
		String str = "callApiForItemDetails()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, "callApiForItemDetails()");

		YRCApiContext context = new YRCApiContext();
		context.setApiName("getItemDetailsList");
		context.setFormId(getFormId());
		context.setInputXml(prepareGetItemListInput(nlItemList));
		callApi(context);

		AcademySIMTraceUtil.endMessage(CLASSNAME, "callApiForItemDetails()");
	}

	// STL-1309 : Defaulting the cursor on Item ID filed in Back room Pick
	// screen : START
	private void getFieldCtrls(Control[] childCtrls,
			ArrayList<Button> rdbBtnList) {
		final String methodName = "getFieldCtrls(childCtrls, rdbBtnList)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);

		// Start WN-2980 GC Activation and fulfillment for SI DCs
		if (!YRCPlatformUI.isVoid(strSetFocusToField)) {
			setFocus(strSetFocusToField);
		} else {// END WN-2980 GC Activation and fulfillment for SI DCs
			for (Control fieldCtrl : childCtrls) {
				String ctrlName = (String) fieldCtrl
						.getData(YRCConstants.YRC_CONTROL_NAME);

				if (!YRCPlatformUI.isVoid(ctrlName)) {
					if (ctrlName.equals(AcademyPCAConstants.TXT_ITEMID)) {
						setFocus(ctrlName);
					}
					// STL-1718 Begin
					if (ctrlName
							.equals(AcademyPCAConstants.STR_SHIPMENT_SEARCH_NO)
							|| ctrlName
									.equals(AcademyPCAConstants.STR_WAREHOUSE_CONT_ID)) {
						if (strIsSharedInvNode
								.equalsIgnoreCase(AcademyPCAConstants.STRING_Y)) {
							setFocus(AcademyPCAConstants.STR_WAREHOUSE_CONT_ID);
						} else {
							setFocus(AcademyPCAConstants.STR_SHIPMENT_SEARCH_NO);
						}
					}
					// STL-1718 End
				}
			}
		}// WN-2980 GC Activation and fulfillment for SI DCs
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}

	// STL-1309 : Defaulting the cursor on Item ID filed in Back room Pick
	// screen : END

	// START : STL-1300 : validate ExtnExeterContainerId. Block backroom pick if
	// ExtnExeterContainerId not received.
	
	//EFP-5
	private void getFieldCtrlsCustom(Control[] childCtrls,
			ArrayList<Button> rdbBtnList) {
		
		final String methodName = "getFieldCtrlsCustom(childCtrls, rdbBtnList)";
		AcademySIMTraceUtil.startMessage(CLASSNAME, methodName);
		
		for (Control fieldCtrl : childCtrls) {
			String ctrlName = (String) fieldCtrl
					.getData(YRCConstants.YRC_CONTROL_NAME);

			if (!YRCPlatformUI.isVoid(ctrlName)) {
				
								
				if(ctrlName.equals("extn_TxtContnrBarcde")){
					
					AcademySIMTraceUtil.logMessage("Control Name is - "+ctrlName);
					setFocus(ctrlName);
				}
				
				if (ctrlName.equals("extn_CompositeContnr_BarCde")) {
					
					AcademySIMTraceUtil.logMessage("Control Name is - "+ctrlName);
					
					YRCBehavior bhvr = (YRCBehavior) YRCDesktopUI.getCurrentPage()
						.getData("YRCBehavior");
					Composite cmpScanModeCtrlOOB = (Composite) bhvr
						.getControl("extn_CompositeContnr_BarCde");
				
					ArrayList controlList = new ArrayList();
				
					if (cmpScanModeCtrlOOB != null
							&& cmpScanModeCtrlOOB.getChildren() != null) { 
						getFieldCtrlsCustom(cmpScanModeCtrlOOB.getChildren(), controlList);
					}
				}
			}
		}
		AcademySIMTraceUtil.endMessage(CLASSNAME, methodName);
	}
	//EFP-5
	
	/**
	 * Validate ExtnExeterContainerId received from Exeter system or not.
	 * 
	 * @param eleShipment
	 */
	private void isExtnExeterContainerIdNotReceived(Element eleShipment) {

		/*
		 * STL-1685 Begin The if condition is added to retain the value of
		 * strExtnExeterContainerId object which is stored while loading of Back
		 * room pick screen. In partial pick scenario (on will pick screen)it's
		 * value becomes null as the getShipmentDetails_output doesn't contain
		 * the Extn element. If the user clicks on previous button of inventory
		 * short/will pick screen then
		 * ERROR_EXTN_EXETER_CONTAINER_ID_IS_NOT_RECEIVED was displayed because
		 * strExtnExeterContainerId is null or blank. Below template override is
		 * NOT working for inventory shortage/will pick screen
		 * /extensions/global/template/com.yantra.pca.ycd.rcp/com.yantra.pca.ycd.rcp.tasks.backroompick.wizards.YCDBackroompickWizard/namespaces/getShipmentDetails_output.xml
		 */
		if (YRCPlatformUI.isVoid(strExtnExeterContainerId)) {
			strExtnExeterContainerId = (String) YRCXPathUtils.evaluate(
					eleShipment, XPATH_EXTN_EXETER_CONTAINER_ID,
					XPathConstants.STRING);
		}// STL-1685 End

		// START: SHIN-6
		// Commented as part of SHIN-6
		/*
		 * String
		 * strNode=eleShipment.getAttribute(AcademyPCAConstants.SHIPNODE_ATTR);
		 * 
		 * if(AcademyPCAConstants.NODE_001.equals(strNode) &&
		 * YRCPlatformUI.isVoid(strExtnExeterContainerId)){ return true; }
		 */

		// START: SHIN-22 commented the below logic due to the post window
		// initializer class
		/*
		 * if (YRCPlatformUI.isVoid(strIsSharedInvNode)) {
		 * AcademySIMTraceUtil.logMessage("The value is void for
		 * strIsSharedInvNode so calling getShipNodeList");
		 * strInvocationFlag=AcademyPCAConstants.STR_VAL_THIRD;
		 * callGetShipNodeListAPI(); }
		 */
		// If UserNameSpace was initialized with attribute, and
		// strIsSharedInvNode is "N" then hide the field
		// else
		// END: SHIN-22
		if (AcademyPCAConstants.STRING_Y.equals(strIsSharedInvNode)
				&& YRCPlatformUI.isVoid(strExtnExeterContainerId)) {
			isExtnExeterContainerIdNotReceived = true;
			showError("ERROR_EXTN_EXETER_CONTAINER_ID_IS_NOT_RECEIVED");
		}
		// END: SHIN-6

		// return false;
	}

	// END : STL-1300 : validate ExtnExeterContainerId. Block backroom pick if
	// ExtnExeterContainerId not received.

	// Start - Changes made for SHIN-21
	// Fetch the user element IsSharedInventoryDC and validate whether
	// IsSharedInventoryDC is Y
	public boolean isSharedInvNode() {

		String traceMethod = "isSharedInvNode()";
		AcademySIMTraceUtil.startMessage(CLASSNAME, traceMethod);
		// Fetch the ship node type from user element.
		String strSharedInvDC = YRCPlatformUI.getUserElement().getAttribute(
				AcademyPCAConstants.ATTR_IS_SHAREDINV_DC);
		AcademySIMTraceUtil.logMessage("strSharedInvDC::" + strSharedInvDC);
		if (!YRCPlatformUI.isVoid(strSharedInvDC)) {
			// Shared Inventory Container DC Flag has been set in user element
			AcademySIMTraceUtil.logMessage("strSharedInvDC is Y");
			if (strSharedInvDC.equalsIgnoreCase(AcademyPCAConstants.STRING_Y)) {
				return true;
			}
		}

		AcademySIMTraceUtil.endMessage(CLASSNAME, traceMethod);
		return false;

	}

	// End - Changes made for SHIN-21
	// STL-1700 Begin
	/**
	 * When a user performs will pick using shipment no or containerID through
	 * "Record Backroom Pick" task and next try to search same shipment no or
	 * containerID then it remains in same screen and "Backroom pick" screen is
	 * not displayed. It's a product issue and to by pass this we are setting
	 * ShipmentKey as blank so that product can make getShipmentDetails API
	 * call.
	 * 
	 * Product code :
	 * YCDBackroomPickShipmentSearchBehavior.handleApiCompletion() method Below
	 * if condition will fail for ShipmentKey as blank so that product can call
	 * toCallGetShipmentDetails() method if
	 * ((!YRCPlatformUI.isVoid(editorInputOfOpenShipment)) &&
	 * ((editorInputOfOpenShipment instanceof YRCEditorInput)))
	 * 
	 * Product code :
	 * YCDBackroomPickShortageReasonsBehavior.handleApiCompletion() method Added
	 * YRCPlatformUI.equals(YRCXmlUtils.getAttribute(eleShipment,
	 * "FromShipmentDetails"), "Y") condition so that it doesn't make
	 * ShipmentKey as blank for backroom pick through "Search Shipment" task on
	 * click of Confirm btn.
	 * 
	 * Shipment element in "Record Backroom Pick" flow on click of Confirm btn
	 * <Shipment ChangeTaskName="YCD_TASK_BACKROOM_PICK"
	 * SellerOrganizationCode="Academy_Direct" ShipNode="701"
	 * ShipmentKey="201701100046062423451935" ShipmentNo="102946292"/>
	 * 
	 * Shipment element in "Search Shipment" flow on click of Confirm btn
	 * <Shipment FromShipmentDetails="Y" ShipmentKey="201701100046062423451935"
	 * ShipmentNo="102946292" ShipmentType="WG" Status="1100.70.06.10" ...> ...
	 * ... </Shipment>
	 */
	private void removeShipmentKey() {

		YRCEditorInput editorInput = (YRCEditorInput) ((YRCEditorPart) YRCDesktopUI
				.getCurrentPart()).getEditorInput();
		Element eleShipment = editorInput.getXml();
		if (!YRCPlatformUI.equals(YRCXmlUtils.getAttribute(eleShipment,
				AcademyPCAConstants.ATTR_FROM_SHIPMENT_DETAILS),
				AcademyPCAConstants.STRING_Y)
				&& !YRCPlatformUI.isVoid(YRCXmlUtils.getAttribute(eleShipment,
						AcademyPCAConstants.SHIPMENT_KEY_ATTR))) {
			YRCXmlUtils.setAttribute(eleShipment,
					AcademyPCAConstants.SHIPMENT_KEY_ATTR,
					AcademyPCAConstants.EMPTY_STRING);
		}
	}

	// STL-1700 End

	// STL-1718 Begin
	/**
	 * This method close and opens the YCDShipmentEditor if packing is not done
	 * from Advanced Shipment search screen
	 * 
	 */
	private void refreshBackroomSearchScreen() {
		YRCEditorInput editorInput = (YRCEditorInput) ((YRCEditorPart) YRCDesktopUI
				.getCurrentPart()).getEditorInput();
		Element eleShipment = editorInput.getXml();
		// FromShipmentDetails attribute is used to identify if the packing is
		// done from Shipment search or Advanced Shipment search screen
		if (!YRCPlatformUI.equals(YRCXmlUtils.getAttribute(eleShipment,
				AcademyPCAConstants.ATTR_FROM_SHIPMENT_DETAILS),
				AcademyPCAConstants.STRING_Y)) {
			// Below code is commented as this was closing PrintTicket editor
			// which has same editor id as Record Backroom Pick.
			// YRCPlatformUI.closeEditor(AcademyPCAConstants.OOB_SHIPPING_PPT_EDITOR_ID,
			// false);
			YRCPlatformUI.closeEditor(editorInput,
					AcademyPCAConstants.OOB_SHIPPING_PPT_EDITOR_ID, false);
			Element eleInput = YRCXmlUtils.createDocument(
					AcademyPCAConstants.SHIPMENT_ELEMENT).getDocumentElement();
			eleInput.setAttribute(AcademyPCAConstants.SHIPMENT_KEY_ATTR,
					AcademyPCAConstants.EMPTY_STRING);
			YRCEditorInput edInput = new YRCEditorInput(eleInput, new String[] {
					AcademyPCAConstants.SHIPMENT_KEY_ATTR,
					AcademyPCAConstants.SHIPMENT_NO_ATTR },
					AcademyPCAConstants.STR_BACKROOM_PICK_TASK);
			YRCPlatformUI.openEditor(
					AcademyPCAConstants.OOB_SHIPPING_PPT_EDITOR_ID, edInput);
		}
	}// STL-1718 End

	// EFP-5 : Start
	private void invokeTranslateBarcodeForContainers(String strBarcodeData) {

		Document barcodeInputDoc = YRCXmlUtils.createDocument("BarCode");
		Element rootElement = barcodeInputDoc.getDocumentElement();
		rootElement.setAttribute("BarCodeType", "ContainerType");
		rootElement.setAttribute("BarCodeData", strBarcodeData);
		Element ctxtInfoElement = barcodeInputDoc
				.createElement("ContextualInfo");
		ctxtInfoElement.setAttribute("OrganizationCode", "DEFAULT");
		rootElement.appendChild(ctxtInfoElement);
		AcademySIMTraceUtil.logMessage(
				"translateBarCodeForContainerType Input: \n", rootElement);

		YRCApiContext yrcApiContext = new YRCApiContext();
		yrcApiContext.setApiName("translateBarCodeForContainerType");
		yrcApiContext.setInputXml(barcodeInputDoc);
		yrcApiContext.setFormId(getFormId());
		callApi(yrcApiContext);
		AcademySIMTraceUtil
				.logMessage("translateBarCodeForContainerType API is invoked");

	}

	private void stampContainerType(Element eleInput) {

		if (null != eleInput) {
			String strContainerType = (String) YRCXPathUtils
					.evaluate(
							eleInput,
							"/BarCode/Translations/Translation/ItemContextualInfo/@ItemID",
							XPathConstants.STRING);

			String strItemDesc = getItemDesc(strContainerType);
			if (null != strItemDesc && !YRCPlatformUI.isVoid(strItemDesc)) {
				this.setFieldValue("extn_TxtScanContnrType", strItemDesc);
				this.setFieldValue("extn_ContainerSelected", strContainerType);
			} else {
				if (AcademyPCAConstants.AMMO_SHIPMENT_TYPE
						.equalsIgnoreCase(this.strShipmentType)
						|| AcademyPCAConstants.HAZMAT_SHIPMENT_TYPE
								.equalsIgnoreCase(this.strShipmentType)
								//OMNI-7980
								|| isSTSAmmoOrHazmatShipment) {
								//OMNI-7980
					
					this.setFieldValue("extn_TxtScanContnrType",
							"Select an Ammo container !");
					this.setFieldValue("extn_ContainerSelected", " ");
					
				} else {
					this.setFieldValue("extn_TxtScanContnrType",
							"Select a Non-Ammo container !");
					this.setFieldValue("extn_ContainerSelected", " ");
				}
			}

			this.setFieldValue("extn_TxtContnrBarcde", "");
		}
	}

	// EFP-5 : End

	// EFP-7 : Start
	private void getContainerType(Element eleBarCodeOut) {

		Element eleShipmentDetails = null;

		eleShipmentDetails = getModel("getShipmentDetails_output");

		if (null != eleShipmentDetails && null != eleBarCodeOut) {

			AcademySIMTraceUtil.logMessage("Shipment Details Model::"
					+ AcademySIMTraceUtil
							.getElementXMLString(eleShipmentDetails));

			String strTotalNumberOfRecords = (String) YRCXPathUtils.evaluate(
					eleBarCodeOut,
					AcademyPCAConstants.BARCODE_TOTALNOOFRECORDS_ATTR_XPATH,
					XPathConstants.STRING);

			String strScannenItemId = null;

			if (AcademyPCAConstants.STR_ZERO.equals(strTotalNumberOfRecords)) {

				strScannenItemId = (String) YRCXPathUtils.evaluate(
						eleBarCodeOut, "/BarCode/@BarCodeData",
						XPathConstants.STRING);
			} else {
				strScannenItemId = (String) YRCXPathUtils
						.evaluate(
								eleBarCodeOut,
								"/BarCode/Translations/Translation/ItemContextualInfo/@ItemID",
								XPathConstants.STRING);
			}

			if (null != strScannenItemId
					&& !YRCPlatformUI.isVoid(strScannenItemId)) {

				AcademySIMTraceUtil.logMessage("Scanned Item Id::"
						+ strScannenItemId);

				NodeList nlShipmentLineNode = (NodeList) YRCXPathUtils
						.evaluate(eleShipmentDetails,
								"//Shipment/ShipmentLines/ShipmentLine[@ItemID='"
										+ strScannenItemId + "']",
								XPathConstants.NODESET);

				if (null != nlShipmentLineNode) {

					AcademySIMTraceUtil.logMessage("nodelist length::"
							+ nlShipmentLineNode.getLength());

					for (int j = 0; j < nlShipmentLineNode.getLength(); j++) {

						Element eleShipmentLine = (Element) nlShipmentLineNode
								.item(j);

						AcademySIMTraceUtil.logMessage("Shipment Line::"
								+ AcademySIMTraceUtil
										.getElementXMLString(eleShipmentLine));

						if (null != eleShipmentLine) {

							AcademySIMTraceUtil
									.logMessage("ShipmentLine Element"
											+ AcademySIMTraceUtil
													.getElementXMLString(eleShipmentLine));

							String strShipmentLineQty = eleShipmentLine
									.getAttribute("Quantity");

							strShipmentLineQty = strShipmentLineQty
									.split("\\.")[0];

							AcademySIMTraceUtil
									.logMessage("Shipment line Qty::"
											+ strShipmentLineQty);

							String strPickQty = eleShipmentLine
									.getAttribute("PickQuantity");

							strPickQty = strPickQty.split("\\.")[0];

							AcademySIMTraceUtil
									.logMessage("Shipment line Pick Qty::"
											+ strPickQty);

							if (Integer.parseInt(strShipmentLineQty) > Integer
									.parseInt(strPickQty)) {
								
								//OMNI-82217 - XPath Correction - Starts
								String strItemSizeCode = (String) YRCXPathUtils
										.evaluate(
												eleShipmentLine,
												"OrderLine/Item/Extn/@ExtnSizeCodeDescription",
												XPathConstants.STRING);

								AcademySIMTraceUtil.logMessage("Size Code::"
										+ strItemSizeCode);

								String strItemShipAlone = (String) YRCXPathUtils
										.evaluate(
												eleShipmentLine,
												"OrderLine/Item/Extn/@ExtnShipAlone",
												XPathConstants.STRING);

								AcademySIMTraceUtil.logMessage("Ship alone::"
										+ strItemShipAlone);

								String strItemMultiBox = (String) YRCXPathUtils
										.evaluate(
												eleShipmentLine,
												"OrderLine/Item/Extn/@ExtnMultibox",
												XPathConstants.STRING);

								AcademySIMTraceUtil.logMessage("Multibox ::"
										+ strItemMultiBox);

								String strItemClass = (String) YRCXPathUtils
										.evaluate(
												eleShipmentLine,
												"OrderLine/Item/Extn/@ExtnClass",
												XPathConstants.STRING);

								AcademySIMTraceUtil.logMessage("Class ::"
										+ strItemClass);
								//OMNI-82217 - Ends 

								if (!AcademyPCAConstants.STRING_Y
										.equalsIgnoreCase(strItemMultiBox)) {

									AcademySIMTraceUtil
											.logMessage("Not a MultiBox::");

									if (AcademyPCAConstants.STRING_Y
											.equalsIgnoreCase(strItemShipAlone)) {

										AcademySIMTraceUtil
												.logMessage("It is ShipAlone");

										getSuitableContainer(strItemClass,
												strItemSizeCode,
												AcademyPCAConstants.STRING_Y,
												AcademyPCAConstants.STRING_N,
												AcademyPCAConstants.STRING_N);
									}

									if (AcademyPCAConstants.STRING_N
											.equalsIgnoreCase(strItemShipAlone)) {

										AcademySIMTraceUtil
												.logMessage("Not a ShipAlone::");

										String strLow = "N";
										String strMedium = "N";
										String strHigh = "N";

										String strThreshold = getThresholdForShipmentLine(strShipmentLineQty);

										if ("Low"
												.equalsIgnoreCase(strThreshold)) {
											strLow = "Y";
										} else if ("Medium"
												.equalsIgnoreCase(strThreshold)) {
											strMedium = "Y";
										} else if ("High"
												.equalsIgnoreCase(strThreshold)) {
											strHigh = "Y";
										}
										getSuitableContainer(strItemClass,
												strItemSizeCode, strLow,
												strMedium, strHigh);
									}
								} else {
									AcademySIMTraceUtil
											.logMessage("It is MultiBox::");
								}

								break;
							}

						}

					}

				}
			}
		}
	}

	private void getSuitableContainer(String strClass, String strSize,
			String strLow, String strMedium, String strHigh) {

		if (!YRCPlatformUI.isVoid(strClass) && !YRCPlatformUI.isVoid(strSize)
				&& !YRCPlatformUI.isVoid(strLow)
				&& !YRCPlatformUI.isVoid(strMedium)
				&& !YRCPlatformUI.isVoid(strHigh) && null != strClass
				&& null != strSize && null != strLow && null != strMedium
				&& null != strHigh) {

			Document DPlookupInputDoc = YRCXmlUtils
					.createDocument("AcadDirectedPackagingLookup");
			Element rootElement = DPlookupInputDoc.getDocumentElement();
			rootElement.setAttribute("PkgClass", strClass);
			rootElement.setAttribute("Size", strSize);
			rootElement.setAttribute("Low", strLow);
			rootElement.setAttribute("Medium", strMedium);
			rootElement.setAttribute("High", strHigh);
			AcademySIMTraceUtil.logMessage("AcadGetDPLookup Input: \n",
					rootElement);

			YRCApiContext yrcApiContext = new YRCApiContext();
			yrcApiContext.setApiName("AcadGetDPLookup");
			yrcApiContext.setInputXml(DPlookupInputDoc);
			yrcApiContext.setFormId(getFormId());
			callApi(yrcApiContext);
			AcademySIMTraceUtil.logMessage("AcadGetDPLookup API is invoked");
		}
	}

	private void getContainerPriority() {

		Document DPlookupInputDoc = YRCXmlUtils
				.createDocument("AcadDPContainerPriority");
		Element rootElement = DPlookupInputDoc.getDocumentElement();
		AcademySIMTraceUtil.logMessage("AcadListDPContinerPriority Input: \n",
				rootElement);

		YRCApiContext yrcApiContext = new YRCApiContext();
		yrcApiContext.setApiName("AcadListDPContainerPriority");
		yrcApiContext.setInputXml(DPlookupInputDoc);
		yrcApiContext.setFormId(getFormId());
		callApi(yrcApiContext);
		AcademySIMTraceUtil
				.logMessage("AcadListDPContinerPriority API is invoked");

	}

	private void getContainerTheshold() {

		Document CommonCodeInDoc = YRCXmlUtils.createDocument("CommonCode");
		Element rootElement = CommonCodeInDoc.getDocumentElement();
		rootElement.setAttribute("CodeType", "CONTAINER_THRESHOLD");
		AcademySIMTraceUtil.logMessage("AcadGetContainerThreshold Input: \n",
				rootElement);

		YRCApiContext yrcApiContext = new YRCApiContext();
		yrcApiContext.setApiName("AcadGetContainerThreshold");
		yrcApiContext.setInputXml(CommonCodeInDoc);
		yrcApiContext.setFormId(getFormId());
		callApi(yrcApiContext);
		AcademySIMTraceUtil
				.logMessage("AcadGetContainerThreshold API is invoked");

	}

	private String getThresholdForShipmentLine(String strShipLineQty) {

		String strThresholdShipLine = null;
		Element eleContnrThreshold = getExtentionModel("Extn_containerThreshold");

		if (null != eleContnrThreshold) {
			NodeList nlCommonCode = (NodeList) YRCXPathUtils.evaluate(
					eleContnrThreshold, "//CommonCodeList/CommonCode",
					XPathConstants.NODESET);

			if (null != nlCommonCode) {
				for (int i = 0; i < nlCommonCode.getLength(); i++) {

					AcademySIMTraceUtil.logMessage("Inside For loop::" + i);

					Element eleCommonCode = (Element) nlCommonCode.item(i);

					if (null != eleCommonCode) {

						String strThresholdVal = eleCommonCode
								.getAttribute(AcademyPCAConstants.CODE_SHORT_DESC_ATTR);

						AcademySIMTraceUtil.logMessage("Threshold Type::"
								+ strThresholdVal);

						String strCodeVal = eleCommonCode
								.getAttribute(AcademyPCAConstants.CODE_VALUE_ATTR);

						AcademySIMTraceUtil.logMessage("Threshold Val::"
								+ strCodeVal);

						String[] strThresholdArray = strThresholdVal.split("-");
						String strMin = "";
						String strMax = "";

						if (strThresholdArray.length > 1) {
							strMin = strThresholdArray[0];
							strMax = strThresholdArray[1];
						} else {
							strMin = strThresholdArray[0];
						}

						if (!YRCPlatformUI.isVoid(strMin)
								&& null != strMin
								&& (YRCPlatformUI.isVoid(strMax) || null == strMax)) {

							if (Integer.parseInt(strShipLineQty) == Integer
									.parseInt(strMin)) {
								strThresholdShipLine = strCodeVal;
								AcademySIMTraceUtil
										.logMessage("Set Threshold ::"
												+ strThresholdShipLine);
								break;
							}

						} else if (!YRCPlatformUI.isVoid(strMin)
								&& null != strMin
								&& !YRCPlatformUI.isVoid(strMax)
								&& null != strMax) {

							if (Integer.parseInt(strShipLineQty) >= Integer
									.parseInt(strMin)
									&& Integer.parseInt(strShipLineQty) <= Integer
											.parseInt(strMax)) {

								strThresholdShipLine = strCodeVal;
								AcademySIMTraceUtil
										.logMessage("Set Threshold ::"
												+ strThresholdShipLine);
								break;
							}
						}
					}
				}
			}
		}

		return strThresholdShipLine;
	}

	private void disableUnusedTextBoxes() {
		
		AcademySIMTraceUtil
		.logMessage("Inside disable method::");
		
		this.setFieldValue("extn_ContainerSelected", "");
		this.setFieldValue("extn_TxtScanContnrType", "");
		this.setFieldValue("extn_cmbContainerType_scan", "");
		disableField("extn_cmbContainerType_scan");
		setFieldValue("extn_RadioContnrBarCde", true);
		setFieldValue("extn_RadioContnrType", false);
		this.enableField("extn_TxtContnrBarcde");
		
	}

	// EFP-7

	// //multibox sku
	private Document callApiForMultiboxCheck(String ItemID) {
		System.out.println("Inside callApiForMultiboxCheck with itemID "
				+ ItemID);
		AcademySIMTraceUtil
				.logMessage("Inside callApiForMultiboxCheck with itemID "
						+ ItemID);
		YRCApiContext context = new YRCApiContext();
		context.setApiName("checkMultiboxItem");
		// context.setApiName("getItemList");
		context.setFormId(getFormId());
		context.setInputXml(prepareInputForMultiBoxItem(ItemID));
		// Document outputTemplate = prepareOutputXmlForMultiBox();
		// context.setOutputXml(outputTemplate);
		callApi(context);
		AcademySIMTraceUtil.endMessage(CLASSNAME, "callApiForMultiboxCheck()");
		AcademySIMTraceUtil
				.logMessage("Exiting callApiForMultiboxCheck: Output : "
						+ YRCXmlUtils.getString(context.getOutputXml()));
		return context.getOutputXml();
	}

	private Document prepareInputForMultiBoxItem(String ItemID) {
		System.out.println("Inside method prepareInputForMultiBoxItem "
				+ ItemID);
		String itemID = getFieldValue("txtItemID");
		Document itemInputDoc = YRCXmlUtils.createDocument("Item");
		itemInputDoc.getDocumentElement().setAttribute("ItemID", ItemID);
		AcademySIMTraceUtil.endMessage(CLASSNAME,
				"prepareInputForMultiBoxItem()");
		return itemInputDoc;
	}

	private void setFieldsForMultiBox() {

		if (multiBoxFlag) {
			System.out.println("Inside the if condition of multibox flag");
			setFieldValue("extn_TxtScanContnrType", "VendorPackageMultibox");
			setFieldValue("extn_ContainerSelected", "VendorPackageMultibox");
			hideField("extn_lblSerialNo", this);
			hideField("extn_txtSerialNo", this);
			hideField("extn_lblLastScannedSerialNo", this);
			hideField("extn_txtLastScannedSerialNo", this);
			hideField("extn_txtSerialNo", this);
			hideField("extn_lblWeight_scan", this);
			hideField("extn_txtWeight_scan", this);
			hideField("extn_lblContainerType_scan", this);
			hideField("extn_cmbContainerType_scan", this);
			hideField("extn_TxtContnrBarcde", this);
			hideField("extn_cmbContainerType_scan", this);
			hideField("extn_RadioContnrType", this);
			hideField("extn_RadioContnrBarCde", this);
			hideField("extn_UnusedTxtBox_07", this);
			hideField("extn_UnusedTxtBox_08", this);
			hideField("extn_UnusedTxtBox_09", this);
			hideField("extn_UnusedTxtBox_10", this);
			hideField("extn_UnusedTxtBox_11", this);
			hideField("extn_UnusedTxtBox_12", this);
			hideField("extn_UnusedTxtBox_13", this);
			hideField("extn_UnusedTxtBox_20", this);
			hideField("extn_UnusedTxtBox_21", this);
			hideField("extn_ContainerSelected", this);
			hideField("extn_CompositeContnr_BarCde", this);
		}

	}

	private boolean findMultiBoxFlagValue(Element element) {
		System.out.println("FindMultiBoxFlag called");
		AcademySIMTraceUtil.logMessage("FindMultiBoxFlag called");
		Element itemElement = (Element) element.getElementsByTagName("Item")
				.item(0);
		Element extnElement = (Element) itemElement
				.getElementsByTagName("Extn").item(0);
		String multiBoxFlag = extnElement.getAttribute("ExtnMultibox");
		AcademySIMTraceUtil.logMessage("MultiboxFlag value is " + multiBoxFlag);
		return multiBoxFlag.equalsIgnoreCase("Y") ? true : false;

	}

	private Document findMBfactor() {
		String itemId = getFieldValue("txtItemID");
		YRCApiContext context = new YRCApiContext();
		context.setApiName("getAcadMultiboxLookup");
		context.setFormId(getFormId());
		context.setInputXml(prepareInputForMBFactor(itemId));
		callApi(context);
		return context.getOutputXml();
	}

	private Document prepareInputForMBFactor(String itemID) {
		Document inputDoc = YRCXmlUtils.createDocument("AcadMultiboxLookup");
		inputDoc.getDocumentElement().setAttribute("ItemID", itemID);
		AcademySIMTraceUtil.endMessage(CLASSNAME,
				"prepareInputForMultiBoxItem()");
		return inputDoc;
	}

	public YRCExtendedTableBindingData getExtendedTableBindingData(
			final String tableName, final ArrayList tableColumnNames) {

		if ("tblBackroomPickTable".equals(tableName)) {

			for (int i = 0; i < tableColumnNames.size(); i++) {
				String controlName = (String) tableColumnNames.get(i);
				if ("extn_clmCustomPick".equals(controlName)) {

					final YRCTblClmBindingData clmBindingData = new YRCTblClmBindingData();
					clmBindingData.setAttributeBinding("@PickQuantity");
					clmBindingData.setColumnBinding("PickQuantity");
					clmBindingData.setName("extn_clmCustomPick");

					HashMap<String, YRCTblClmBindingData> bindingDataMap = new HashMap<String, YRCTblClmBindingData>();
					bindingDataMap.put("extn_clmCustomPick", clmBindingData);
					System.out.println("clmBindingData set");

					// /to make column editable
					HashMap<String, String> cellTypes = new HashMap<String, String>();
					cellTypes.put("extn_clmCustomPick",
							YRCConstants.YRC_TEXT_BOX_CELL_EDITOR);

					YRCExtendedCellModifier cellModifier = new YRCExtendedCellModifier() {

						@Override
						public boolean allowModify(String property,
								String value, Element element) {
							String scannedItemId = getFieldValue("txtLastScannedItem");
							String itemID = YRCXmlUtils.getAttributeValue(
									element, "/ShipmentLine/@ItemID");
							boolean allowModify = false;
							if (!scannedItemId.isEmpty()
									&& scannedItemId != null
									&& itemID.equalsIgnoreCase(scannedItemId)) {
								String itemQuantity = YRCXmlUtils
										.getAttributeValue(element,
												"/ShipmentLine/@Quantity");
								System.out.println("itemQuantity is"
										+ itemQuantity + "itemID is" + itemID);
								double itemQuantityInt = Double
										.parseDouble(itemQuantity);
								if (itemQuantityInt > thresholdPickQuantity) {
									allowModify = true;
								}
								System.out.println("allowModify is "
										+ allowModify);
							}
							return allowModify;
						}

						@Override
						public String getModifiedValue(String property,
								String value, Element element) {
							return value;
						}

						@Override
						public YRCValidationResponse validateModifiedValue(
								String property, String value, Element element) {
							double originalQuantity = Double
									.parseDouble(YRCXmlUtils.getAttributeValue(
											element, "/ShipmentLine/@Quantity"));
							double enteredQuantity = 0.0;
							if (value.isEmpty() || value == null || value == "") {
								enteredQuantity = 0.0;
							} else {
								enteredQuantity = Double.parseDouble(value);
							}
							String prevPickQuantity = YRCXmlUtils
									.getAttributeValue(element,
											"/ShipmentLine/@BackroomPickedQuantity");
							double previousPickQuantity = 0.0;
							if (prevPickQuantity.isEmpty()
									|| prevPickQuantity == null
									|| prevPickQuantity == "") {
								previousPickQuantity = 0.0;
							} else {
								previousPickQuantity = Double
										.parseDouble(prevPickQuantity);
							}
							double maxAllowedQuantity = originalQuantity
									- previousPickQuantity;
							YRCValidationResponse response = null;

							if (enteredQuantity == 0) {

								response = new YRCValidationResponse(
										YRCValidationResponse.YRC_VALIDATION_ERROR,
										"Minimum Pick Quantity is 1.0");
							}
							// double quot = enteredQuantity%1;

							else if ((enteredQuantity % 1) != 0.0) {

								response = new YRCValidationResponse(
										YRCValidationResponse.YRC_VALIDATION_ERROR,
										"Decimal numbers not permitted");
							}

							else if (enteredQuantity <= maxAllowedQuantity) {
								response = new YRCValidationResponse(
										YRCValidationResponse.YRC_VALIDATION_OK,
										"Status message");
							} else {
								response = new YRCValidationResponse(
										YRCValidationResponse.YRC_VALIDATION_ERROR,
										"Over Picked");
							}
							return response;
						}
					};

					YRCExtendedTableBindingData extTblBindingData = new YRCExtendedTableBindingData();
					extTblBindingData.setCellTypes(cellTypes);
					extTblBindingData.setTableColumnBindingsMap(bindingDataMap);
					extTblBindingData.setCellModifier(cellModifier);
					setControlData("extTblBindingData",
							YRCConstants.YRC_TABLE_BINDING_DEFINATION,
							extTblBindingData);
					return extTblBindingData;
				}
			}

		}

		return super.getExtendedTableBindingData(tableName, tableColumnNames);
	}

	// EFP-5
	private String getItemDesc(String strItemId) {

		String strItemDesc = null;
		Element eleItemList = getExtentionModel("Extn_getItemList_Output");

		if (null != eleItemList) {

			AcademySIMTraceUtil.logMessage("Inside getItemDesc:::");

			NodeList nlItemDetails = (NodeList) YRCXPathUtils.evaluate(
					eleItemList,
					"//ItemList/Item[@ItemID='" + strItemId + "']",
					XPathConstants.NODESET);

			if (null != nlItemDetails) {

				AcademySIMTraceUtil.logMessage("NodeList not null:::");

				Element eleItem = (Element) nlItemDetails.item(0);

				if (null != eleItem) {

					NodeList nlPrimaryInfo = eleItem
							.getElementsByTagName("PrimaryInformation");

					if (null != nlPrimaryInfo) {

						Element elePrimaryInfo = (Element) nlPrimaryInfo
								.item(0);
						strItemDesc = elePrimaryInfo
								.getAttribute("Description");
					}
				}
			}

		}

		return strItemDesc;
	}

	private boolean isValidAmmoNonAmmoContainer(String strContainer,
			boolean isAmmoShipment) {

		boolean isValidContainer = false;
		Element eleItemList = getExtentionModel("Extn_getItemList_Output");

		if (null != eleItemList) {

			AcademySIMTraceUtil.logMessage("Inside getItemDesc:::");

			NodeList nlItemDetails = (NodeList) YRCXPathUtils.evaluate(
					eleItemList, "//ItemList/Item[@ItemID='" + strContainer
							+ "']", XPathConstants.NODESET);

			if (null != nlItemDetails) {

				AcademySIMTraceUtil.logMessage("NodeList not null:::");

				Element eleItem = (Element) nlItemDetails.item(0);

				if (null != eleItem) {

					NodeList nlPrimaryInfo = eleItem
							.getElementsByTagName("PrimaryInformation");

					if (null != nlPrimaryInfo) {

						Element elePrimaryInfo = (Element) nlPrimaryInfo
								.item(0);
						String strItemType = elePrimaryInfo
								.getAttribute("ItemType");

						if (isAmmoShipment
								&& "AmmoContainer"
										.equalsIgnoreCase(strItemType)) {
							isValidContainer = true;
						} else if (!isAmmoShipment
								&& !"AmmoContainer"
										.equalsIgnoreCase(strItemType)) {
							isValidContainer = true;
						}
					}
				}
			}

		}
		return isValidContainer;
	}
	// EFP-5
	
	//OMNI-7980 : Begin
	private boolean isSTSAmmoOrHazmatShipment(){
		
		AcademySIMTraceUtil.logMessage("Begin: isSTSAmmoOrHazmatShipment : ");
		boolean isAmmoHazmatShipment = false;
		Element eleShipment = null;
		try{
			eleShipment = getModel(AcademyPCAConstants.GET_SHIPMENT_DETAILS_OOB_MODEL);
			if(!YRCPlatformUI.isVoid(eleShipment)){
				AcademySIMTraceUtil.logMessage("Shipment Details :: "+YRCXmlUtils.getString(eleShipment));
				
				String shipmentType = eleShipment.getAttribute("ShipmentType");
				AcademySIMTraceUtil.logMessage("Shipment Type"+ shipmentType);
				
				if(AcademyPCAConstants.STR_STS.equals(shipmentType)){
					NodeList nlOrderLines = eleShipment.getElementsByTagName(AcademyPCAConstants.ELE_ORDER_LINE);
					
					for(int iOL = 0;iOL<nlOrderLines.getLength();iOL++){
						AcademySIMTraceUtil.logMessage("Order Line - "+iOL);
						
						Element eleOrderLine = (Element)nlOrderLines.item(iOL);
						String strLineType = eleOrderLine.getAttribute(AcademyPCAConstants.ATTR_LINE_TYPE);
						
						if(AcademyPCAConstants.AMMO_SHIPMENT_TYPE.equals(strLineType) 
								|| AcademyPCAConstants.HAZMAT_SHIPMENT_TYPE.equals(strLineType)){
							isAmmoHazmatShipment = true;
							AcademySIMTraceUtil.logMessage("set 'isAmmoHazmatShipment' to TRUE");
						}
						
					}
				}
				
				
			}
			
		}catch(Exception e){
			AcademySIMTraceUtil.logMessage("Exception caught :: "+e);
		}
		
		AcademySIMTraceUtil.logMessage("End: isSTSAmmoOrHazmatShipment : ");
		return isAmmoHazmatShipment;
	}
	//OMNI-7980 : End

}
