package com.academy.som.util;

/**
 * This interface holds all the constants that are used in this project for easy
 * management of values.
 * 
 * @author <a href="mailto:KaushikN.Sanji@cognizant.com">Kaushik N Sanji</a>
 *         Copyright © 2006-2009 Sterling Commerce, Inc. All Rights Reserved.
 */
public interface AcademyPCAConstants {

	public final String STRING_ERROR = "Error";

	public final String EMPTY_STRING = "";

	public final String STRING_WHITE_SPACE = " ";

	public final String STRING_OPEN_BRACE = "(";

	public final String STRING_CLOSE_BRACE = ")";

	public final String TOTAL_NO_OF_RECORDS_ATTR = "TotalNumberOfRecords";

	public final String ENTERPRISE_CODE_ATTR = "EnterpriseCode";

	public final String ORDER_BY_ELEMENT = "OrderBy";

	public final String ATTRIBUTE_ELEMENT = "Attribute";

	public final String NAME_ATTR = "Name";

	public final String DESCENDING_ATTR = "Desc";

	public final String STRING_T = "T";

	public final String STRING_SIMPLE_DATE_PATTERN = "yyyy-MM-dd";

	// Added for Record Backroom Pick wizards :: START
	public final String RECORD_BACKROOM_SHIP_OUT_PAGEID = "com.yantra.pca.sop.rcp.tasks.outboundexecution.pickup.recordbackroompick.wizardpages.SOPOBERecordBackroomPickShipOut";

	public final String RECORD_BACKROOM_SCAN_MODE_PAGEID = "com.yantra.pca.sop.rcp.tasks.outboundexecution.pickup.recordbackroompick.wizardpages.SOPRecordBackroomPickScanMode";

	public final String RECORD_BACKROOM_SHORTAGE_PAGEID = "com.yantra.pca.sop.rcp.tasks.outboundexecution.pickup.recordbackroompick.wizardpages.SOPRecordBackroomShortageResolution";

	public final String RECORD_BACKROOM_SEARCH_PAGEID = "com.yantra.pca.sop.rcp.tasks.outboundexecution.pickup.recordbackroompick.wizardpages.SOPRecordBackroomPickSearch";

	public final String GET_ITEM_LIST_API = "getItemList";

	public final String ITEM_ELEMENT = "Item";

	public final String ITEM_LIST_ITEM_ELEMENT_XPATH = "/ItemList/Item";

	public final String ITEM_KEY_ATTR = "ItemKey";

	public final String CNTR_TYPE_KEY_ATTR = "ContainerTypeKey";

	public final String IS_SHIP_CNTR_ATTR = "IsShippingCntr";

	public final String CONTAINERIZATION_SERVICE_COMMAND = "CreateContainersAndPrintService";

	public final String EXTN_GET_ITEM_LIST_MODEL = "Extn_getItemList_Output";

	public final String SHIPMENT_RESOLUTION_OOB_MODEL = "ShipmentDetailsForResolution";

	public final String CONTAINER_TYPE_ATTR = "ContainerType";

	public final String CONTAINER_GROSS_WT_ATTR = "ContainerGrossWeight";

	public final String IS_COMPLETE_PICK_FLAG_ATTR = "IsCompletePick";

	public final String STRING_N = "N";

	public final String STRING_Y = "Y";

	public final String CUSTOM_TXT_WEIGHT = "extn_txtWeight";

	public final String CUSTOM_TXT_WEIGHT_SCAN = "extn_txtWeight_scan";

	public final String CUSTOM_CMB_CONTAINER_TYPE = "extn_cmbContainerType";

	public final String CUSTOM_CMB_CONTAINER_TYPE_SCAN = "extn_cmbContainerType_scan";

	public final String CUSTOM_OVERRIDE_BTN_NEXT = "extn_btnPickNext";

	public final String CUSTOM_OVERRIDE_BTN_CONFIRM = "extn_btnPickConfirm";

	public final String SHIPMENT_DETAILS_OOB_MODEL = "ShipmentDetails";

	public final String SHIPMENT_OOB_MODEL = "Shipment";

	public final String SEL_RADIO_OOB_MODEL = "SelectedRadioButton";

	public final String RADIO_SEL_ATTR = "radioSelection";

	public final String SHIPMENT_SHIPMENTLINE_XPATH = "/Shipment/ShipmentLines/ShipmentLine";

	public final String PICKED_UOM1_ATTR = "PickedUOM1";

	public final String PICKED_QUANTITY1_ATTR = "PickedQuantity1";

	public final String BACKROOM_PICKED_QUANTITY_ATTR = "BackroomPickedQuantity";

	public final String QUANTITY_ATTR = "Quantity";

	public final String ZERO_DECIMAL_VAL = "0.0";

	public final String BTN_NEXT_OOB_ACTION_ID = "com.yantra.pca.ycd.rcp.tasks.backroompick.actions.YCDBackroomPickNextAction";
	
	public final String BTN_CONFIRM_OOB_ACTION_ID = "com.yantra.pca.ycd.rcp.tasks.backroompick.actions.YCDBackroomPickConfirmAction";

	public final String CUSTOM_OVERRIDE_BTN_NEXT_ACTION_ID = "BACKROOM_PICK_NEXT_ACTION";

	public final String DECIMAL_NUMBER_REGEX = "^(\\+)?[0-9]*(\\.[0-9]+)?$";

	public final String OOB_TXT_LAST_SCAN_QTY = "text2";

	public final String OOB_TXT_SCAN_ITEMID = "txtItemID";

	public final String OOB_TXT_LAST_SCAN_ITEM = "txtLastItem";

	public final String OOB_RDBTN_SHORTAGE_RES = "rdbSearchOnOrder";

	public final String CNF_RESOLUTION_TXT_MSG = "CNF_RESOLUTION_TXT_MSG";

	public final String CUSTOM_BTN_CNF_RESO_VAL = "BTN_CNF_RESO";

	public final String CUSTOM_BTN_CNL_RESO_VAL = "BTN_CNL_RESO";

	public final String SHORTAGE_RESOLUTION_POPUP_TITLE = "Confirm Shortage Resolution";

	public final String SHORTAGE_RESO_TYPE_INVENTORY = "InventoryShortage";

	public final String OOB_TRANSLATE_BARCODE_COMMAND = "TranslateBarCode";

	public final String BARCODE_TYPE_ATTR = "BarCodeType";

	public final String BARCODE_TYPE_SFSITEM_VAL = "SFSItem";

	public final String CONTXTUAL_INFO_ELEMENT = "ContextualInfo";

	public final String ORG_CODE_ATTR = "OrganizationCode";

	public final String ORG_CODE_DEFAULT_VAL = "DEFAULT";

	// Added for Record Backroom Pick wizards :: END

	// Added for Search Shipment wizards :: START
	public final String SHIPDTL_SHIPMENT_KEY_ATTR_XPATH = "/ShipmentDetail/Shipment/@ShipmentKey";

	public final String SHIPDTL_SHIPNODE_ATTR_XPATH = "/ShipmentDetail/Shipment/@ShipNode";

	public final String SHIPDTL_MANIFEST_KEY_ATTR_XPATH = "/ShipmentDetail/Shipment/@ManifestKey";

	public final String REPRINT_SHIPMENT_PICK_TICKET_SERVICE_COMMAND = "AcademySFSPrintShipmentPickTicket";

	public final String REPRINT_INVOICE_SERVICE_COMMAND = "AcademySFSRePrintInvoice";

	public final String REPRINT_SHIP_LABEL_SERVICE_COMMAND = "AcademySFSRePrintShippingLabel";
	
	public final String REPRINT_SHIP_BOL_SERVICE_COMMAND = "AcademySFSReprintShipmentBOL";

	public final String REPRINT_PACK_LIST_SERVICE_COMMAND = "AcademySFSRePrintPackList";

	public final String AUTO_CORRECT_SHPMNT_SERVICE_COMMAND = "AcademySFSAutoCorrectShipmentError";

	public final String AUTO_CORRECT_MANIFEST_SERVICE_COMMAND = "AcademySFSAutoCorrectManifestError";

	public final String PRINT_ELEMENT = "Print";

	public final String SHIPMENT_KEY_ATTR = "ShipmentKey";

	public final String SHIP_STATUS_LIST_PPT_OOB_MODEL = "nmspStatusList_Input";

	public final String STATUS_READY_FOR_BACKROOM_PICK_VAL = "1100.70.06.10";

	public final String STATUS_READY_FOR_BACKROOM_PICK = "Ready For Backroom Pick";

	public final String STATUS_READY_FOR_CUSTOMER_VAL = "1100.70.06.30";

	public final String STATUS_READY_FOR_CUSTOMER = "Ready To Ship";

	public final String STATUS_DESC_ATTR = "Description";

	public final String STATUS_ATTR = "Status";

	public final String SHIPMENT_STATUS_ELE = "Status";

	public final String OOB_GET_SHIPMENT_LIST_COMMAND = "shpSearch_GetShipmentList";

	public final String OOB_GET_SHIPMENT_LIST_NEXT_COMMAND = "shpSearch_GetShipmentListNext";

	public final String CUSTOM_CMB_STATUS = "extn_cmbStatus";
	
	//Start of SFS2.0 STL-536 fix
	
	public final String GET_COMMON_CODE_LIST = "getCommonCodeList";
	
	public final String EXTN_INV_SHRT_RSN = "Extn_inv_shrt_rsn_Output";
	
	//	End of SFS2.0 STL-536 fix	
	
	public final String STATUS_OPEN_STATUSLIST_XPATH = "/StatusList/Status[@Status='1100']";

	public final String STATUS_SHIP_INVOICE_STATUSLIST_XPATH = "/StatusList/Status[@Status='1400']";

	public final String PICK_TICKET_NO_ATTR = "PickticketNo";

	public final String OOB_GENERIC_SEARCH_SHIPMENT_PAGEID = "com.yantra.pca.sop.rcp.tasks.outboundexecution.searchshipment.wizardpages.SOPGenericShipmentSearch";

	public final String OOB_GENERIC_SHIPMENT_DTLS_PAGEID = "com.yantra.pca.sop.rcp.tasks.outboundexecution.searchshipment.wizardpages.SOPGenericShipmentDetails";

	public final String SHIPMENT_NO_ATTR = "ShipmentNo";

	public final String EXTN_SCAC_PRIORITY_ATTR = "Extn_ExtnSCACPriority";	

	public final String SHIPMENT_CNTR_KEY_ATTR = "ShipmentContainerKey";

	public final String CNTR_SEL_POPUP_TITLE = "Choose Container";

	public final String SHIPMENT_PICK_TKT_SUCCESS_MSG = "SHIPMENT_PICK_TKT_SUCCESS_MSG";

	public final String REPRINT_PACK_LIST_SUCCESS_MSG = "REPRINT_PACK_LIST_SUCCESS_MSG";

	public final String REPRINT_INVOICE_SUCCESS_MSG = "REPRINT_INVOICE_SUCCESS_MSG";

	public final String REPRINT_SHIP_LABEL_SUCCESS_MSG = "REPRINT_SHIP_LABEL_SUCCESS_MSG";

	public final String AUTO_CORRECT_SHIP_SUCCESS_MSG = "AUTO_CORRECT_SHIP_SUCCESS_MSG";

	public final String AUTO_CORRECT_MANIFEST_SUCCESS_MSG = "AUTO_CORRECT_MANIFEST_SUCCESS_MSG";

	public final String OOB_GENERIC_SEARCH_SHIPMENT_DTLS_PAGEID = "com.yantra.pca.sop.rcp.tasks.outboundexecution.searchshipment.wizardpages.SOPGenericShipmentDetails";

	public final String CONTAINER_ELEMENT = "Container";

	public final String SHIPMENT_TYPE_ATTR = "ShipmentType";

	public final String CUSTOM_BTN_CNTR_SELECT_VAL = "BTN_SELECT_CNTR";

	public final String SHIPDTL_SHIPMENT_TYPE_ATTR_XPATH = "/ShipmentDetail/Shipment/Shipment/@ShipmentType";

	public final String GET_MANIFEST_LIST_COMMAND = "GetManifestList";

	public final String MANIFEST_ELEMENT = "Manifest";

	public final String SHIPMENTS_ELEMENT = "Shipments";

	public final String SHIPMENT_ELEMENT = "Shipment";

	public final String MANIFEST_CLOSED_FLAG_ATTR = "ManifestClosedFlag";

	public final String STATUS_SHIPPED_VAL = "1400";

	public final String STATUS_QRY_TYPE_LE_VAL = "LE";

	public final String EXTN_OPEN_MANIFEST_LIST_MODEL = "Extn_openManifestList_Output";

	public final String SHIPMENT_TYPE_CC_COMMAND = "GetShipmentTypeCC";

	public final String EXTN_SHIPMENT_TYPE_CC_MODEL = "Extn_getShipmentTypeCC_Output";

	public final String CODE_TYPE_VAL_FOR_SHIPMENT_TYPE = "SHIPMENT_TYPE";

	public final String COMMONCODE_ELEMENT_XPATH = "/CommonCodeList/CommonCode";

	public final String CUSTOM_TXT_SHIPMENT_TYPE = "extn_txtShipmentType";

	public final String MANIFEST_KEY_ATTR = "ManifestKey";

	public final String SHIPDTL_SHIPMENT_STATUS_ATTR_XPATH = "/ShipmentDetail/Shipment/@Status";

	public final String SHIPDTL_EXCEPTION_TASKS_GRP_ID = "Exception Tasks";

	public final String SHIPDTL_REPRINT_PACKSLIP_TASK_ID = "Reprint_Packslip_Label_Task";

	public final String SHIPDTL_REPRINT_SHIPPING_TASK_ID = "Reprint_Shipping_Label_Task";

	public final String SHIPDTL_REPRINT_INVOICE_TASK_ID = "Reprint_Shipment_Invoice_Task";

	public final String SHIPDTL_REPRINT_SHIPMENT_TASK_ID = "Reprint_Shipment_PickTicket_Task";

	public final String SHIPDTL_SHIPMENT_SHIPMENT_ELE_XPATH = "/ShipmentDetail/Shipment/Shipment";

	// Added for Search Shipment wizards :: END

	// Added for Print Pick Ticket wizards :: START
	public final String CUSTOM_BTN_REPRINT_ITEM_F4 = "extn_btnPrintItemF4";

	public final String CUSTOM_BTN_REPRINT_SHIPMENT_F7 = "extn_btnPrintShipmentF7";

	public final String CUSTOM_BTN_PRINT_SHIPMENT_PICK_TICKETS_F8 = "extn_btnPrintShipmentTicketsF8";

	public final String CUSTOM_ACTION_PRINT_ITEM_PICK_TICKET = "PRINT_ITEM_PICK_TICKET_ACTION";


	public final String CUSTOM_ACTION_SHIPMENT_PICK_TICKETS = "PRINT_SHIPMENT_PICK_TICKETS_ACTION";

	public final String CUSTOM_ACTION_REPRINT_SHIPMENT_PICK_TICKETS = "REPRINT_SHIPMENT_PICK_TICKETS_ACTION";

	public final String CUSTOM_RDBTN_PRINT_PICK_TICKET = "extn_rdbPrintTicketsForItem";

	public final String CUSTOM_RDBTN_PRINT_SHIPMENT_PICK_TICKETS = "extn_rdbPrintShipmentTickets";

	public final String CUSTOM_TXT_PRINT_SHIPMENT_PICK_TICKETS1 = "extn_txtPrintShipmentTickets1";

	public final String CUSTOM_TXT_PRINT_SHIPMENT_PICK_TICKETS2 = "extn_txtPrintShipmentTickets2";

	public final String CUSTOM_LBL_SHIPMENT_NO = "extn_lblShipmentNo";

	public final String PRINT_PICK_TICKETS_SERVICE_COMMAND = "AcademySFSPrintItemPickTickets";

	public final String PRINT_SHIPMENT_PICK_TICKETS_SERVICE_COMMAND = "AcademySFSPrintShipmentPickTickets";

	public final String ITEM_ID_ATTR = "ItemID";

	public final String SHIPNODE_ATTR = "ShipNode";

	public final String OOB_LBL_PNL_HEADER = "lblPanelHeader";

	public final String LBL_PNL_HEADER_MSG = "LBL_PNL_HEADER_MSG";

	public final String SHIPMENT_LIST_PPT_OOB_MODEL = "ShipmentListAll";

	public final String GET_SHIPMENT_LIST_OOB_COMMAND = "GetShipmentList";

	public final String STATUS_QRY_TYPE_ATTR = "StatusQryType";

	public final String STATUS_QRY_TYPE_EQ_VAL = "EQ";

	//public final String OOB_SHIPPING_PPT_EDITOR_ID = "com.yantra.pca.sop.rcp.tasks.outboundexecution.pickup.editors.SOPOBEShippingEditor";

	//Changed the Editor Name since the Editor name is different in SOM
	public final String OOB_SHIPPING_PPT_EDITOR_ID = "com.yantra.pca.ycd.rcp.editors.YCDShipmentEditor";
	
	public final String GET_PRINT_BATCH_SIZE_COMMAND = "GetPrintBatchSizeCC";

	public final String COMMON_CODE_ELEMENT = "CommonCode";

	public final String CODE_TYPE_ATTR = "CodeType";

	public final String CODE_VALUE_ATTR = "CodeValue";

	public final String CODE_SHORT_DESC_ATTR = "CodeShortDescription";

	public final String CODE_LONG_DESC_ATTR = "CodeLongDescription";

	public final String CODE_TYPE_VAL_FOR_PRINT_BATCH = "PRINT_BATCH_SIZ";

	public final String EXTN_PRINT_BATCH_SIZE_CC_MODEL = "Extn_printBatchSizeCC_Output";

	public final String CUSTOM_TXT_BATCH_SIZE_PRINT = "extn_cmbBatchSizePrint";

	public final String MAX_RECORDS_ATTR = "MaximumRecords";

	public final String CUSTOM_CMB_BATCH_GRP = "extn_cmbBatchGroupPrint";

	public final String GET_PRINT_BATCH_GRP_COMMAND = "GetPrintBatchGroupServ";

	public final String EXTN_PRINT_BATCH_GRP_MODEL = "Extn_printBatchGroup_Output";

	public final String CUSTOM_TXT_REPRINT_PAGE_NO = "extn_txtReprintPageNo";

	public final String PAGE_NO_ATTR = "PageNo";

	public final String REPRINT_PAGE_NO_TXT_TOOLTIP = "REPRINT_PAGE_NO_TXT_TOOLTIP";

	public final String GET_PICK_TICKET_NO_LIST_COMMAND = "GetPickTicketNoList";

	public final String SFS_PICK_TICKET_NO_ELEMENT = "SFSShipmentPickTicketNo";

	public final String EXTN_PICK_TICKET_NO_LIST_MODEL = "Extn_pickTicketNoList_Output";

	public final String GET_SHIP_STORE_STATS_COMMAND = "GetStoreStatsCC";

	public final String SHIP_STORE_STATS_CC_MODEL = "StoreStatsCC_Output";

	public final String CODE_TYPE_VAL_FOR_STORE_STATS = "STORE_STATS";

	public final String GET_STORE_OP_STATS_LIST_COMMAND = "GetStoreOpStatsList";

	public final String STORE_OP_STATS_ELEMENT = "SFSStoreStats";

	public final String STATS_DESC_ATTR = "Description";

	public final String STATS_COUNT_ATTR = "Count";

	public final String STORE_OP_STATS_MODEL = "StoreOpStats_Output";

	public final String COUNT_SIZE_ZERO = "0";

	public final String STORE_OP_STATS_ELEMENT_XPATH = "/SFSStoreStatsList/SFSStoreStats";

	public final String CUSTOM_PNL_MANIFESTING_DAY = "extn_pnlManifestingDay";

	public final String IS_SUPERVISOR_ATTR = "IsSupervisor";

	public final String WORKING_CALENDAR_ELEMENT = "WorkingCalendar";

	public final String ORG_WORKING_DAY_ELEMENT = "OrgWorkingDay";

	public final String ORG_WORKING_DAYS_ELEMENT = "OrgWorkingDays";

	public final String DAY_OFFSET_ATTR = "Day";

	public final String DATE_ATTR = "Date";

	public final String FROM_DAY_OFFSET_ATTR = "FromDayOffset";

	public final String TO_DAY_OFFSET_ATTR = "ToDayOffset";

	public final String CALENDAR_KEY_ATTR = "CalendarKey";

	public final String CALENDAR_ID_ATTR = "CalendarId";

	public final String WORKING_DAY_FLAG_ATTR = "WorkingDay";

	public final String GET_CAL_WORKING_DAYS_COMMAND = "GetCalendarWorkingDays";

	public final String STORE_CALENDAR_ELEMENT = "StoreCalendar";

	public final String STORE_WORKING_CAL_MODEL = "StoreWorkingCalendar_Output";

	public final String MULTI_API_CAL_UPDATE_COMMAND = "MultiApiCalendarUpdate";

	public final String MULTI_API_ELEMENT = "MultiApi";

	public final String MULTI_API_INPUT_ELEMENT = "Input";

	public final String API_ELEMENT = "API";

	public final String FLOW_NAME_ATTR = "FlowName";

	public final String MAKE_WORKING_DAY_SERV = "AcademySFSMakeWorkingDay";

	public final String MAKE_NON_WORKING_DAY_SERV = "AcademySFSMakeNonWorkingDay";

	public final String MANIFEST_DAY_GRP_TITLE = "MANIFEST_DAY_GRP_TITLE";

	public final String CALENDAR_ID_LABEL_TEXT = "CALENDAR_ID_LABEL_TEXT";

	public final String WORKING_DAY_LABEL_TEXT = "WORKING_DAY_LABEL_TEXT";

	public final String NON_WORKING_DAY_LABEL_TEXT = "NON_WORKING_DAY_LABEL_TEXT";

	public final String BTN_SAVE_MANIFEST_DAY = "BTN_SAVE_MANIFEST_DAY";

	public final String BTN_RESET_MANIFEST_DAY = "BTN_RESET_MANIFEST_DAY";

	public final String MANIFEST_DAY_CHANGE_SUCCESS_MSG = "MANIFEST_DAY_CHANGE_SUCCESS_MSG";

	public final String GET_USER_LOCALE_COMMAND = "GetUserLocale";

	public final String USER_LOCALE_MODEL = "UserLocale_Output";

	public final String LOCALE_ELEMENT = "Locale";

	public final String LOCALE_CODE_ATTR = "Localecode";

	public final String DATE_FORMAT_LOCALE_ATTR = "DateFormat";

	public final String START_DAY_OFFSET = "START_DAY_OFFSET";

	public final String END_DAY_OFFSET = "END_DAY_OFFSET";

	// Added for Print Pick Ticket wizards :: END

	// Added for Close Manifest wizards :: START

	public final String MANIFEST_NO_ATTR = "ManifestNo";

	public final String MANIFEST_DATE_ATTR = "ManifestDate";

	public final String OPEN_MANIFEST_LIST_OOB_MODEL = "OpenManifestList";

	public final String SELTD_MANIFEST_OOB_MODEL = "SelectedManifestForCloseOperation";

	public final String SELTD_MANIFEST_ATTR = "SelectedManifest";

	public final String EXTN_USER_LOCALE_MODEL = "Extn_UserLocale_Output";

	public final String OOB_BTN_CLOSE_MANIFEST = "btnClose";

	public final String ACAD_CLOSE_MANIFEST_COMMAND = "AcademySFSCloseManifest";

	public final String CLOSE_MANIFEST_SUCCESS_MSG = "CLOSE_MANIFEST_SUCCESS_MSG";

	public final String MANIFEST_LIST_MANIFEST_ELE_XPATH = "/Manifests/Manifest";

	public final String MAKE_NON_WORKING_COMMAND = "MakeNonWorking";

	public final String SCAC_ATTR = "Scac";

	public final String OOB_PNL_MANIFEST_LIST = "pnlManifestList";

	public final String CHANGE_SHIPMENT_API = "changeShipment";

	public final String MULTI_API_CHNG_SHIPMENT_PACK_COMMAND = "MultiApiChangeShipmentPack";

	public final String SHIPMENT_PACK_COMPLT_ATTR = "ShipmentPackComplete";

	// Added for Close Manifest wizards :: END

	// Ship From DC :: Start
	public final String VENDOR_PKG_XML = "<Item ItemID='VendorPackage'><PrimaryInformation ShortDescription='VENDOR PACKAGE'/></Item>";

	public final String VENDOR_PKG_STR = "VendorPackage";

	public static final String COMPLEX_QRY_ELEMENT = "ComplexQuery";

	public static final String COMPLEX_OPERATOR_ATTR = "Operator";

	public static final String COMPLEX_OPERATOR_AND_VAL = "AND";

	public static final String COMPLEX_AND_ELEMENT = "And";

	public static final String COMPLEX_OR_ELEMENT = "Or";

	public static final String COMPLEX_EXP_ELEMENT = "Exp";

	public static final String ITEMLIST_ITEM_ELE_XPATH = "/ItemList/Item";

	public static final String ATTR_NAME = "Name";

	public static final String ATTR_VALUE = "Value";

	public final String GET_ITEM_DETAILS_LIST_API = "getItemDetailsList";

	public final String SHIPMENT_DETAILS_ITEMID_XPATH = "/Shipment/ShipmentLines/ShipmentLine/OrderLine/Item";

	public final String EXTN_ITEM_DETAIL_LIST_MODEL = "Extn_ItemDetail_List";

	public final String ELE_CLASSIFICATION_CODES = "ClassificationCodes";

	public final String ELE_EXTN = "Extn";

	public final String ATTR_STORAGE_TYPE = "StorageType";

	public final String ATTR_WG_ELIGIBLE = "ExtnWhiteGloveEligible";
	// Ship From DC :: END
	
	// SIM to SOM Upgrade : START
	
	//Pick Ticket Screen
	
	public final String GET_PENDING_SHIPMENT_LIST_COMMAND="GetPendingShipments";
	
	public final String EXTN_GET_PENDING_SHIPMENT_LIST_MODEL = "EXTN_GetShipmentList_OTNS";
	
	public final String PICK_TICKET_PRINTED="PickTicketPrinted";
	
	public final String PICK_TICKET_PRINTED_QRY_TYPE_ATTR = "PickTicketPrintedQryType";
	
	public final String QRY_TYPE_NE_VAL = "NE";
	
	//Search Shipment Screen
	
	public final String GET_STATUS_LIST_COMMAND="GetStatusList";
	
	public final String CALLING_ORG_CODE="CallingOrganizationCode";
	
	public final String ENTERPRISE_CODE="Academy_Direct";
	
	public final String DOCUMENT_TYPE_ATTR="DocumentType";
	
	public final String DOCUMENT_TYPE_ATTR_VAL="0001";
	
	public final String PROCESS_TYPE_KEY_ATTR="ProcessTypeKey";
	
	public final String PROCESS_TYPE_KEY_VAL="ORDER_DELIVERY";
	
	public final String STATUS_SHIPMENT_CANCELLED_VAL="9000";
	
	public final String ELE_QUERY_TYPE="QueryType";
	
	public final String GET_QUERY_TYPELIST_COMMAND="getQueryTypeList";
	
	public final String GET_SHIPMENT_LIST_COMMAND="getShipmentList";
	
	public final String GET_SHIPMENT_LIST_INP_OOB_MODEL="getShipmentList_input";
	
	public final String OOB_BTN_PICKUP="bttnPick";
	
	public final String OOB_BTN_SHIPPING="bttnShip";
	
	public final String EXTN_GET_STATUS_LIST_MODEL="Extn_getStatusList_Output";
	
	public final String EXTN_GET_QUERY_TYPE_LIST_MODEL="Extn_getQueryTypeList_Output";
	
	public final String EXTN_LABEL_LOOPUP="extn_lblLookUp";
	
	public final String OOB_ADV_ITEM_SEARCH_SHARED_TASK="YCDAdvItemSearchSharedTask";
	
	public final String EXTN_TXT_ITEMID="extn_txtItemID";
	
	public final String SHIPDTL_INSTORE_TASKS_GRP_ID="In_Store_Tasks";
	
	public final String SHIPDTL_PICK_TICKET_TASKS_ID="YCD_TASK_PRINT_PICK_TICKET";
	
	public final String SHIPDTL_ADVSHP_SEARCH_TASKS_ID="YCD_TASK_ADVANCED_SHIPMENT_SEARCH";
	//SIM to SOM Upgrade : END
	
	//constants modified as part of upgrade-start
	public final String OOB_SEARCH_SHIPMNT_DTLS_MODEL = "Shipment";
	
	/* Constants added for STL - 689-Close Manifest Story */
	public final String SERVICE_ACAD_CLOSE_MANIFEST_SIM_LIST = "AcademyExtnGetCloseManifestSIMList";
	public final String ACAD_CLOSE_MANIFEST_SIM = "ACADCloseManifestSim";
	public final String EXTN_ACAD_CLOSE_MANIFEST_SIM_MODEL = "Extn_Acad_Close_Manifest_Output";
	public final String CLOSE_MANIFEST_IN_PROGRSS_MSG = "Closure_In_Progress";
	public final String STATUS_CLOSURE_IN_PROGRESS = "    Closure in progress";
	public final String STATUS_OPEN = "    Open";
	/* Constants added for STL - 689-Close Manifest Story */
	
	/* Start- Constants added for STL-685 */
	public final String EXTN_CANCEL_SHIPMENT = "extn_cancel";
	public final String ACAD_CANCEL_SHIPMENT_SERVICE = "AcademyCancelShipmentService";
	public final String OOB_SHIPMENT_DETAILS_EDITOR_ID = "com.yantra.pca.sop.rcp.tasks.outboundexecution.searchshipment.editors.SOPOBEShipmentSearchDetailEditor";
	/* End- Constants added for STL-685 */
	
	 //Start STL-737
	public final String SHIPDTL_REPRINT_ORMD_TASK_ID = "Reprint_ORMD_Task";
	public final String AMMO_SHIPMENT_TYPE = "AMMO";
	public final String REPRINT_ORMD_SERVICE_COMMAND = "AcademySFSRePrintORMDLabel";
	public final String REPRINT_ORMD_SUCCESS_MSG = "REPRINT_ORMD_SUCCESS_MSG";
	//End STL-737
	
	//Start STL-925,926
	public final String PRINT_PICK_TICKET_GROUP_ID = "Shipping_Tasks";
	//End STL-925,926
	public final String PACK_STATION_FOR_REPRITN = "RePrintPackStn";

	//START: Added Constants for STL 1309
	public final String CMP_SCAN_MODE_CNTRLS = "cmpstScanModeControls";
	public final String TXT_ITEMID = "txtItemID";
	//END: Added Constants for STL 1309
	
	//START : Added Constants for STL-1300 
	public final String NODE_001 = "001";
	public final String ATTR_NODE = "Node";
	public final String ATTR_EXTN_EXETER_CONTAINER_ID = "ExtnExeterContainerId";
	//END : Added Constants for STL-1300 
	
	//START : Added Constants for STL-1311 
	public final String OOB_SHIPMENT_DTLS_FORM_ID = "com.yantra.pca.ycd.rcp.tasks.shipmentTracking.screens.YCDShipmentDetails";
	//START : Added Constants for STL-1311
	
	//START: SHIN-6
	public final String EXTN_GET_SHIP_NODE_LIST_MODEL = "Extn_getShipNodeList_Output";
	public final String GET_SHIP_NODE_LIST_COMMAND = "GetShipNodeList";
	public final String ATTR_NODE_TYPE="NodeType";
	public final String ATTR_IS_SHAREDINV_DC="IsSharedInventoryDC";
	public final String ATTR_VAL_SHAREDINV_DC="SharedInventoryDC";
	/*START: SHIN-22 
	 * public final String STR_VAL_FIRST="First";
	public final String STR_VAL_SECOND="Second";
	public final String STR_VAL_THIRD="Third";
	END: SHIN-22*/
	//END: SHIN-6
	
	//Started STL-1456
	public final String MANAGE_TASK_Q_API = "manageTaskQueue";
	public final String GET_TASK_Q_DATA_LIST = "getTaskQueueDataList";
	public final String TRANSACTION_KEY = "TransactionKey";
	public final String TRANSACTION_ID = "TransactionId";
	public final String TRANS_ID_CLOSE_MANIFEST = "ACAD_CLOSE_MANIFEST.2008.ex";
	public final String ATTR_OPERATION = "Operation";
	public final String ACT_CREATE = "Create";
	public final String ATTR_HOLDFLAG = "HoldFlag";
	public final String ATTR_DATA_KEY = "DataKey";
	public final String ATTR_DATA_TYPE = "DataType";
	public final String CLOSE_MANIFEST_TRANS_KEY = "20160107053414314198";
	public final String ELE_TASK_QUEUE = "TaskQueue";
	public final String ELE_GET_TASK_Q_DATA_INPUT= "GetTaskQueueDataInput";
	//End STL-1456
	
	//START : STL-1501
	public final String USER_NAME_SPACE_MODEL = "UserNameSpace";
	public static final String ATTR_LOGIN_ID = "LoginID";
	public static final String ATTR_USER_LOGIN_ID = "Loginid";
	public static final String ATTR_PASSWORD = "Password";
	public static final String ELE_LOGIN = "Login";
	//END : STL-1501
	
	//START : STL-1493
	public final String CUSTOM_ACTION_RESET_PICK_TICKET = "PRINT_RESET_PICK_TICKET_ACTION";
	public final String RESET_PICK_TICKETS_FLAG_SERVICE_COMMAND = "AcademySFSResetPickTickets";
	public final String CUSTOM_BTN_RESET_PICK_TICKET = "extn_btnReset";
	//END : STL-1493

	//START STL-1664
	public final String STR_PACK_PRINTER_ID = "PACK_PRINTER_ID";
	//END STL-1664
	
	//START STL-1678
	public final String STR_EXTN_LBL_UPC_CODE = "extn_lblUPCCode";
	public final String STR_EXTN_TXT_UPC_CODE = "extn_txtUPCCode";
	public final String ATTR_UPC_CODE = "UPCCode";
	public final String STR_ZERO = "0";
	public final String ATTR_IS_UPC_BASE_BP_SEARCH_ENABLE = "IsUPCBaseBPSearchEnable";
	public final String STR_COMMON_CODE_UPC_SEARCH_BP = "UPC_SEARCH_BP";
	//END STL-1678
	
	//START STL-1700
	public final String ATTR_FROM_SHIPMENT_DETAILS = "FromShipmentDetails";
	//END STL-1700

	//START STL-1718
	public final String STR_SHIPMENT_NO = "txtShipmentNo";
	public final String STR_BACKROOM_PICK_TASK = "YCD_TASK_BACKROOM_PICK";
	public final String STR_BACKROOM_PICK_WIZARD = "com.yantra.pca.ycd.rcp.tasks.backroompick.wizardpages.YCDBackroomPick";
	public final String STR_SHIPMENT_SEARCH_NO = "txtShipmentSearchShipmentNo";
	public final String STR_WAREHOUSE_CONT_ID = "extn_txtWarehouseContId";
	public final String STR_YRC_BEHAVIOR = "YRCBehavior";
	public final String STR_COMP_SHIPMENT_SEARCH = "cmpstShipmentsearch";
	//END STL-1718
	
	//START WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
	public final String HAZMAT_SHIPMENT_TYPE = "HAZMAT";
	//END WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation
	
	//START WN-2346 Retain user selected pack station
	public final String CUSTOM_COMBO_PRINTER_ID = "extn_PrinterIdComboBox";
	public final String ATTR_DEFAULT_PRINTER = "DefaultPrinter";
	//END WN-2346 Retain user selected pack station
	
	//Start WN-2980 GC Activation and fulfillment for SI DCs
	public final String CUSTOM_TXT_SERIALNO_SCAN = "extn_txtSerialNo";
	public final String CUSTOM_TXT_LAST_SCANNED_SERIALNO = "extn_txtLastScannedSerialNo";
	public final String CUSTOM_LBL_SERIALNO = "extn_lblSerialNo";
	public final String CUSTOM_LBL_LAST_SCANNED_SERIALNO = "extn_lblLastScannedSerialNo";
	public final String TRANSLATE_BARCODE_API = "translateBarCodeForSerialNo";
	public final String BARCODE_TOTALNOOFRECORDS_ATTR_XPATH = "/BarCode/Translations/@TotalNumberOfRecords";
	public final String GET_SHIPMENT_DETAILS_OOB_MODEL = "getShipmentDetails_output";
	public final String OOB_TXT_LAST_SCANNED_ITEMID = "txtLastScannedItem";
	public final String PICK_QUANTITY_ATTR = "PickQuantity";
	public final String SERIAL_SCAN_COUNT_ATTR = "SerialScanCount";
	public final String OOB_BTN_CONFIRM = "btnConfirm";
	public final String SHIPMENT_TAG_SERIALS_ELEMENT = "ShipmentTagSerials";
	public final String SHIPMENT_TAG_SERIAL_ELEMENT = "ShipmentTagSerial";
	public final String SERIAL_NO_ATTR = "SerialNo";
	public final String SHIPMENT_LINE_KEY_ATTR = "ShipmentLineKey";
	public final String ONE_DECIMAL_VAL = "1.0";
	public final String BARCODE_ELEMENT = "BarCode";
	public final String CONTEXTUAL_INFO_ELEMENT = "ContextualInfo";
	public final String ITEM_CONTEXTUAL_INFO_ELEMENT = "ItemContextualInfo";
	public final String BARCODE_DATA_ATTR = "BarCodeData";
	public final String BARCODE_TYPE_SERIALSCAN_VAL = "SerialScan";
	public final String INVENTORY_UOM_ATTR = "InventoryUOM";
	public final String STR_EACH = "EACH";
	public final String SHIPMENT_SHIPNODE_ATTR_XPATH = "/Shipment/@ShipNode";
	public final String SHIPMENTLINE_ATTR = "ShipmentLine";	
	//End WN-1717 GC Activation and fulfillment for SI DCs
	
	//Start OMNI-6622 : Changes for STS 
	public final String ELE_DOCUMENT_PARAMS = "DocumentParams";
	public final String ELE_DOCUMENT_PARAMS_LIST = "DocumentParamsList";
	public final String LABEL_EXTN_DOCUMENT_TYPE = "extn_cmbDocumentType";
	public final String MODEL_DOCUMENT_TYPE_OUTPUT = "Extn_getDocumentTypeList_Output";
	public final String STR_SALES_ORDER_DOC_TYPE = "0001";
	public final String STR_SHIP_TO_HOME = "Ship To Home";
	public final String STR_SHIP_TO_STORE = "Ship To Store";
	public final String STR_TRANSFER_ORDER_DOC_TYPE = "0006";
	//End : OMNI-6622 : Changes for STS
	
	//Start OMNI-6616 : Changes for STS 
	public final String STR_STS = "STS";
	//End : OMNI-6616 : Changes for STS
	
	//OMNI-7980 : Begin
	public final String SHIPDTL_AUTO_CORRECT_MANIFEST_TASK_ID = "Auto_Correct_Manifest_Task";
	public final String SHIPDTL_AUTO_CORRECT_SHIPMENT_TASK_ID = "Auto_Correct_Shipment_Task";
	public final String SHIPDTL_REPRINT_SHIPMENT_PICK_TICKET_TASK_ID = "Reprint_Shipment_PickTicket_Task";
	public final String SHIPDTL_REPRINT_SHIPMENT_BOL_TASK_ID = "Reprint_Shipment_BOL_Task";
	public final String ELE_ORDER_LINE = "OrderLine";
	public final String ATTR_LINE_TYPE = "LineType";
	//OMNI-7980 : End
	//Start 65686
	public static final String STR_CONTAINER_DROP_DOWN_RCP = "CONTAINER_DROP_DOWN_RCP";
	public final String TGL_RCP_WEB_SOM_UI = "TGL_RCP_WEB_SOM_UI";
    //End 65686
	// start OMNI-65673 
    public final String WG_SHIPMENT_TYPE = "WG";
    // end OMNI-65673
}
