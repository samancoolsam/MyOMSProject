package com.academy.ecommerce.sterling.util;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$, $Date$
 */
public interface AcademyPCAConstants {
	public static final String PLUGIN_ID="com.academy.ecommerce.sterling";
	/* List of ALL API Names */
	/** DOCUMENT ME! */
	public static final String API_GET_COMMONCODELIST = "getCommonCodeList";
	/** DOCUMENT ME! */
	public static final String API_GET_CUSTOMERDETAILS_OUTPUT = "getCustomerDetails_output";
	/** DOCUMENT ME! */
	public static final String API_GET_COMMONCODELIST_FOR_GIFT = "getCommonCodeList_GiftPopUp";
	/** DOCUMENT ME! */
	public static final String API_GET_ITEMLIST_WITHOUT_PRICE = "getItemListForOrderingWithoutPrice";
	/** DOCUMENT ME! */
	public static final String API_GET_ITEMLIST_WITH_PRICE = "getItemListForOrderingWithPrice";
	/** DOCUMENT ME! */
	public static final String API_GET_FULFILLMENT_OPTIONS_FOR_LINES = "getFulfillmentOptionsForLines";
	/** DOCUMENT ME! */
	public static final String API_GET_CARRIER_SERVICE_LIST = "getCarrierServiceList";
	/** DOCUMENT ME! */
	public static final String API_GET_LOS_RESTRICTION_LIST_SERVICE="AcademyGetLosRestrictionListService";
	
	/* List of all Element Names */

	/** DOCUMENT ME! */
	public static final String ELE_COMMON_CODE = "CommonCode";

	/* List of All Attribute names */
	/** DOCUMENT ME! */
	public static final String ATTR_GET_COMMON_CODE_LIST="getCommonCodeList";
	/** DOCUMENT ME! */
	public static final String ATTR_ORGANIZATION_CODE = "DEFAULT";
	/** DOCUMENT ME! */
	public static final String IS_GIFT_FLAG = "GiftFlag";

	
	/** DOCUMENT ME! */
	public static final String ATTR_PERSON_INFO_KEY = "PersonInfoKey";
	/** DOCUMENT ME! */
	public static final String ATTR_PERSON_INFO = "PersonInfo";
	/** DOCUMENT ME! */
	public static final String ATTR_CHECKED = "@Checked";
	/** DOCUMENT ME! */
	public static final String ATTR_ITEM = "Item";
	/** DOCUMENT ME! */
	public static final String ATTR_ITEM_ID = "ItemID";
	/** DOCUMENT ME! */
	public static final String ATTR_EXTN = "Extn";
	/** DOCUMENT ME! */
	public static final String ATTR_ITEM_CHECK = "Item_CheckBox";
	/** DOCUMENT ME! */
	public static final String ATTR_ERROR = "Error";
	/** DOCUMENT ME! */
	public static final String ATTR_Y = "Y";
	/** DOCUMENT ME! */
	public static final String ATTR_N = "N";
	/** DOCUMENT ME! */
	public static final String ATTR_GIFT = "GIFT";
	/** DOCUMENT ME! */
	public static final String ATTR_MODEL_FOR_INITIALIZE = "ModelForInitialize";
	/** DOCUMENT ME! */
	public static final String ATTR_RECIPIENT_MESSAGE = "Recepient_Message";
	/** DOCUMENT ME! */
	public static final String ATTR_RECIPIENT_NAME = "Recepient_Name";
	/** DOCUMENT ME! */
	public static final String ATTR_SET_ITEM_AS_GIFT = "Set_Gift_Recipient_Ship";
	/** DOCUMENT ME! */
	public static final String ATTR_CLEAR_ITEM_AS_GIFT = "Clear_Gift_Recipient_Ship";
	/** DOCUMENT ME! */
	public static final String ATTR_COMMON_CODE = "CommonCode";
	/** DOCUMENT ME! */
	public static final String ATTR_CODE_SHORT_DESCRIPTION = "CodeShortDescription";
	/** DOCUMENT ME! */
	public static final String ATTR_CODE_LONG_DESCRIPTION = "CodeLongDescription";
	/** DOCUMENT ME! */
	public static final String ATTR_CODE_TYPE = "CodeType";
	/** DOCUMENT ME! */
	public static final String ATTR_INSTRUCTION_TYPE = "InstructionType";
	/** DOCUMENT ME! */
	public static final String ATTR_CODE_TYPE_VALUE = "TITLE";
	/** DOCUMENT ME! */
	public static final String ATTR_GIFT_MESSAGE = "GIFT_MESSAGE";
	/** DOCUMENT ME! */
	public static final String ATTR_RESULTS = "Results";
	/** DOCUMENT ME! */
	public static final String ATTR_REFRESH_ACTION = "com.yantra.yfc.rcp.internal.YRCRefreshScreenAction";
	/** DOCUMENT ME! */
	public static final String ATTR_PACK_LIST_TYPE = "PackListType";
	/** DOCUMENT ME! */
	public static final String ATTR_ORDER = "Order";
	/** DOCUMENT ME! */
	public static final String ATTR_ORDER_HEADER_KEY = "OrderHeaderKey";
	/** DOCUMENT ME! */
	public static final String ATTR_ORDER_LINE_KEY = "OrderLineKey";
	/** DOCUMENT ME! */
	public static final String ATTR_TAX_EXEMPTION_CERTIFICATE = "TaxExemptionCertificate";
	/** DOCUMENT ME! */
	public static final String ATTR_TAX_ID = "Tax ID";
	/** DOCUMENT ME! */
	public static final String ATTR_ITEM_DETAILS = "ItemDetails";
	/** DOCUMENT ME! */
	public static final String ATTR_SHIP_DATE = "ShipDate";
	/** DOCUMENT ME! */
	public static final String ATTR_ASSIGNMENT = "Assignment";
	/** DOCUMENT ME! */
	public static final String ATTR_CARRIER_SERVICE_LIST = "CarrierServiceList";
	/** DOCUMENT ME! */
	public static final String ATTR_CALLING_ORGANIZATION_CODE = "CallingOrganizationCode";
	/** DOCUMENT ME! */
	public static final String ATTR_USED_FOR_ORDERING = "UsedForOrdering";
	/** DOCUMENT ME! */
	public static final String ATTR_ADDRESS = "address";
	/** DOCUMENT ME! */
	public static final String ATTR_CUST_ADDRESS = "customerAddress";
	/** DOCUMENT ME! */
	public static final String ATTR_GET_CARRIER_SERVICE_LIST = "getCarrierServiceList";
	/** DOCUMENT ME! */
	public static final String ATTR_ORDERLINE_KEY = "@OrderLineKey";
	/** DOCUMENT ME! */
	public static final String ATTR_ITEM_PRIMARY_INFO = "PrimaryInformation";
	/** DOCUMENT ME! */
	public static final String ATTR_ITEM_IS_HAZMAT = "IsHazmat";
	/** DOCUMENT ME! */
	public static final String ELE_PERSON_INFO_SHIP_TO = "PersonInfoShipTo";
	/** DOCUMENT ME! */
	public static final String ATTR_CARRIER_SERVICE_CODE = "CarrierServiceCode";
	/** DOCUMENT ME! */
	public static final String ATTR_GET_SALES_ORDER_DETAILS = "getSalesOrderDetails";
	/** DOCUMENT ME! */
	public static final String ATTR_TAX_DETAILS = "TaxDetails";
	/** DOCUMENT ME! */
	public static final String ATTR_TAX_EXEMPT_ID = "TaxExemptId";
	/** DOCUMENT ME! */
	public static final String ATTR_TAX_EXEMPT_DETAILS = "TaxExemptDetails";
	/** DOCUMENT ME! */
	public static final String ATTR_ORDER_DETAILS = "OrderDetails";
	/** DOCUMENT ME! */
	public static final String ATTR_ORDER_LINES = "OrderLines";
	/** DOCUMENT ME! */
	public static final String ATTR_CHANGE_ORDER = "changeOrder";
	/** DOCUMENT ME! */
	public static final String ATTR_REQ_SHIP_DATE ="ReqShipDate";
	/** DOCUMENT ME! */
	public static final String ATTR_DELIVERY_DATE ="DeliveryDate";
	/** DOCUMENT ME! */
	public static final String ATTR_PROD_SHIP_DATE ="ProductShipDate";
	/** DOCUMENT ME! */
	public static final String ATTR_PROD_AVAIL_DATE ="ProductAvailDate";
	/** DOCUMENT ME! */
	public static final String ATTR_PROD_AVAILABLE_DATE ="ProductAvailableDate";
	/** DOCUMENT ME! */
	public static final String ATTR_PNL_CONTACT ="pnlContact";
	/** DOCUMENT ME! */
	public static final String ATTR_ORDER_COLLECTION_DETAILS="OrderCollectionDetails";
	/** DOCUMENT ME! */
	public static final String ATTR_PAYMENT_COLLECTION_DETAILS="Payment Collection Details";
	/** DOCUMENT ME! */
	public static final String ATTR_PAYMENT__DETAILS="Payment Details";
	/** DOCUMENT ME! */
	public static final String ATTR_ACADEMY_URL="http://academy.com/";
	/** DOCUMENT ME! */
	public static final String ATTR_BTN_APPLY_DELIVERY="btnApplyDelivery";
	/** DOCUMENT ME! */
	public static final String ATTR_ON_LOAD="OnLoad";
	/** DOCUMENT ME! */
	public static final String ATTR_CMB_SHIP_TO_ADDRESS="cmbShipToAddress";
	/** DOCUMENT ME! */
	public static final String ATTR_ADDRESS_LIST="AddressList";
	/** DOCUMENT ME! */
	public static final String ATTR_PROMOTIONS="Promotions";
	/** DOCUMENT ME! */
	public static final String ATTR_PROMOTION="Promotion";
	/** DOCUMENT ME! */
	public static final String ATTR_DESC= "Description";
	/** DOCUMENT ME! */
	public static final String ATTR_AWARDS= "Awards";
	/** DOCUMENT ME! */
	public static final String ATTR_AWARD= "Award";
	/** DOCUMENT ME! */
	public static final String ATTR_ACTION = "Action";
	/** DOCUMENT ME! */
	public static final String ATTR_MODIFY = "MODIFY";
	/** DOCUMENT ME! */
	public static final String ATTR_ORDER_NO= "OrderNo";
	/** DOCUMENT ME! */
	public static final String ATTR_CREATE= "Create";
	/** DOCUMENT ME! */
	public static final String ATTR_SHIPMENT= "Shipment";
	/** DOCUMENT ME! */
	public static final String ATTR_ORDER_RELEASE_KEY= "OrderReleaseKey";
	/** DOCUMENT ME! */
	public static final String ATTR_ORDER_RELEASES= "OrderReleases";
	/** DOCUMENT ME! */
	public static final String ATTR_ORDER_RELEASE= "OrderRelease";
	/** DOCUMENT ME! */
	public static final String ATTR_RELEASED= "Released";
	/** DOCUMENT ME! */
	public static final String ATTR_SHIPMENT_KEY= "ShipmentKey";
	/** DOCUMENT ME! */
	public static final String ATTR_SHIPMENT_LINES= "ShipmentLines";
	/** DOCUMENT ME! */
	public static final String ATTR_SHIPMENT_LINE= "ShipmentLine";
	/** DOCUMENT ME! */
	public static final String ATTR_SHIPMENT_LINE_KEY= "ShipmentLineKey";
	/** DOCUMENT ME! */
	public static final String ATTR_SHIPMENT_TAG_SERIALS= "ShipmentTagSerials";
	/** DOCUMENT ME! */
	public static final String ATTR_SHIPMENT_TAG_SERIAL= "ShipmentTagSerial";
	/** DOCUMENT ME! */
	public static final String ATTR_SHIPMENT_TYPE= "ShipmentType";
	/** DOCUMENT ME! */
	public static final String ATTR_SERIAL_NO= "SerialNo";
	/** DOCUMENT ME! */
	public static final String ATTR_PRO_NO= "ProNo";
	/** DOCUMENT ME! */
	public static final String ATTR_URL= "URL";
	/** DOCUMENT ME! */
	public static final String ATTR_TRACKING_NO= "TrackingNo";
	/** DOCUMENT ME! */
	public static final String ATTR_QUANTITY= "Quantity";
	/** DOCUMENT ME! */
	public static final String ATTR_CONFIRM_SHIPMENT="confirmShipment";
	/** DOCUMENT ME! */
	public static final String ATTR_EXIT="Exit";
	/** DOCUMENT ME! */
	public static final String ATTR_CHANGE_SHIPMENT="changeShipment";
	/** DOCUMENT ME! */
	public static final String ATTR_GIFT_CARD_NO_START="GiftCardNoStart";
	/** DOCUMENT ME! */
	public static final String ATTR_GIFT_CARD_NO_END="GiftCardNoEnd";
	/** DOCUMENT ME! */
	public static final String ATTR_SAVED="Saved";
	/** DOCUMENT ME! */
	public static final String ATTR_GET_COMPLETE_ORDER_DETAILS="getCompleteOrderDetails";
	/** DOCUMENT ME! */
	public static final String ATTR_ORDER_LINE_DETAILS= "OrderLineDetails";
	/** DOCUMENT ME! */
	public static final String ATTR_TAG_SERIALS = "TagSerials";
	
	/* List of all Extended attributes */
	/** DOCUMENT ME! */
	public static final String ATTR_IS_SIGNATURE_REQUIRED = "extn_ChkIsSignatureRequired";
	/** DOCUMENT ME! */
	public static final String ATTR_IS_POBOX_ADDRESS = "extn_ChkIsPOAddress";
	/** DOCUMENT ME! */
	public static final String ATTR_IS_APO_FPO = "extn_ChkIsAPOFPO";
	/** DOCUMENT ME! */
	public static final String ATTR_CORPORATE_CUSTOMER = "extn_ChkCorporateCustomer";
	/** DOCUMENT ME! */
	public static final String ATTR_COMPANY = "extn_TCompany";
	/** DOCUMENT ME! */
	public static final String ATTR_REQURIES_WEBPROFILEID = "extn_ChkRequiresWebprofileID";
	/** DOCUMENT ME! */
	public static final String ATTR_IS_RED_LINED = "extn_ChkIsRedLined";
	/** DOCUMENT ME! */
	public static final String ATTR_OPT_IN = "extn_OptIn";
	/** DOCUMENT ME! */
	public static final String ATTR_GIFT_OPTIONS_SHIP_POPUP = "extn_btnGiftOptionsPopUp";
	/** DOCUMENT ME! */
	public static final String ATTR_EXTN_ITEM_CHECK = "extn_Item_CheckBox";
	/** DOCUMENT ME! */
	public static final String ATTR_TBL_CLM_CHECK = "extn_tblClmCheck";
	/** DOCUMENT ME! */
	public static final String ATTR_DROP_SHIP_FLAG = "ExtnDropShipFlag";
	/** DOCUMENT ME! */
	public static final String ATTR_CONFIRM = "@Extn_Confirm";
	/** DOCUMENT ME! */
	public static final String ATTR_EXTN_COMMON_CODE = "Extn_CommonCode";
	/** DOCUMENT ME! */
	public static final String ATTR_WEB_ITEM_DETAILS = "extn_LnkWebItemDetails";
	/** DOCUMENT ME! */
	public static final String ATTR_ACADEMY = "Academy";
	/** DOCUMENT ME! */
	public static final String ATTR_WEBSITE_ACCESS = "Website access";
	/** DOCUMENT ME! */
	public static final String ATTR_EXTN_STREET_DATE = "ExtnStreetDate";
	/** DOCUMENT ME! */
	public static final String ATTR_ECOMMERCE = "ExtnECommerceFlag";
	/** DOCUMENT ME! */
	public static final String ATTR_EXTN_ECOMMERCE_VALUE = "02";
	/** DOCUMENT ME! */
	public static final String ATTR_STORE_ONLY_ITEM = "extn_TStoreOnlyItem";
	/** DOCUMENT ME! */
	public static final String ATTR_LBL_UNIT_PRICE = "labelUnitPrice";
	/** DOCUMENT ME! */
	public static final String ATTR_LBL_LIST_PRICE = "lblListPrice";
	/** DOCUMENT ME! */
	public static final String ATTR_LABEL_LIST_PRICE = "labelListPrice";
	/** DOCUMENT ME! */
	public static final String ATTR_TXT_LIST_PRICE = "extn_TListPrice";
	/** DOCUMENT ME! */
	public static final String ATTR_LBL_END_DATE = "extn_LblEndDate";
	/** DOCUMENT ME! */
	public static final String ATTR_END_DATE = "extn_TEndDate";
	/** DOCUMENT ME! */
	public static final String ATTR_TAX_EXEMPTID = "extn_LnkTaxExemptID";
	/** DOCUMENT ME! */
	public static final String ATTR_BTN_TAX_EXEMPTID = "extn_BtnTaxExemptID";
	/** DOCUMENT ME! */
	public static final String ATTR_TXT_TAX_EXEMPTID = "extn_TTaxExemptID";
	/** DOCUMENT ME! */
	public static final String ATTR_PAYMENT_DETAILS = "extn_LnkViewAuthorizationandChargeDetails";
	/** DOCUMENT ME! */
	public static final String ATTR_EXTN_ITEM_LIST = "ExtnItemList";
	/** DOCUMENT ME! */
	public static final String ATTR_EXT_FINAL_SHIP_DATE = "Extn_FinalShipDate";
	/** DOCUMENT ME! */
	public static final String ATTR_EXTN_CARRIER_SERVICE_LIST = "ExtnCarrierServiceList";
	/** DOCUMENT ME! */
	public static final String ATTR_EXTN_COMMON_CODE_LIST = "ExtnCommonCodeList";
	/** DOCUMENT ME! */
	public static final String ATTR_ITEM_IS_CONVEYABLE = "ExtnConveyable";
	/** DOCUMENT ME! */
	public static final String ATTR_ITEM_IS_GIFT_CARD = "ExtnIsGiftCard";
	/** DOCUMENT ME! */
	public static final String ATTR_ITEM_SHIP_ALONE = "ExtnShipAlone";
	/** DOCUMENT ME! */
	public static final String ATTR_ITEM_IS_KIT = "ExtnKit";
	/** DOCUMENT ME! */
	public static final String ATTR_ITEM_IS_WHITE_GLOVE = "ExtnWhiteGloveEligible";
	/** DOCUMENT ME! */
	public static final String ATTR_APO_FPO= "ExtnIsAPOFPO";
	/** DOCUMENT ME! */
	public static final String ATTR_PPO_BOX_ADDRESS = "ExtnIsPOBOXADDRESS";
	/** DOCUMENT ME! */
	public static final String ATTR_EXTN_LIST_CARRIER_SERVICE = "ExtnListCarrierService";
	/** DOCUMENT ME! */
	public static final String ATTR_EXTN_EXTENDED_ATTRIBUTES="ExtnAddressAttibutes";
	/** DOCUMENT ME! */
	public static final String ATTR_EXTN_ORDER_DETAILS="ExtnOrderDetails";
	/** DOCUMENT ME! */
	public static final String ATTR_EXTN_ORDER_DETAILS_FOR_CFO="ExtnOrderDetailsCFOScreen";
	/** DOCUMENT ME! */
	public static final String ATTR_EXTN_UPDATE_ORDER_DETAILS="ExtnUpdateOrderDetails";
	/** DOCUMENT ME! */
	public static final String ATTR_EXTN_LOS_RESTRICTION_MODEL="ExtnLOSRestrictionModel";
	/** DOCUMENT ME! */
	public static final String ATTR_EXTN_TEXT_RECIPIENT_NAME="extn_TRecipientName";
	/** DOCUMENT ME! */
	public static final String ATTR_EXTN_TEXT_RECIPIENT_MESSAGE= "extn_TRecipientMessage";
	/** DOCUMENT ME! */
	public static final String ATTR_EXTN_CMB_MESSAGES="extn_CmbRecipientMessages";
	/** DOCUMENT ME! */
	public static final String ATTR_RAD_SET_RECIPIENT= "rbtSetRecipient";
	/** DOCUMENT ME! */
	public static final String ATTR_RAD_CLEAR_RECIPIENT= "rbtnClearRecipient";
	/** DOCUMENT ME! */
	public static final String ATTR_EXTN_OUTPUT_COMMON_CODE= "ExtnOutputCommonCode";
	/** DOCUMENT ME! */
	public static final String ATTR_EXTN_TARGET_MODEL="Extn_targetModel";
	/** DOCUMENT ME! */
	public static final String ATTR_ACAD_RED_LINE_PERMISSION="ycdRCP0999";
	/** DOCUMENT ME! */
	public static final String ATTR_EXTN_CHKISREDLINED="extn_ChkIsRedLined";
	
	
	/* List of Error Messages */
	// TODO- to be moved to error.properties
	/** DOCUMENT ME! */
	public static final String GIFT_ERROR_MESSAGE = "Error_Message_Select_Atleast_One_Item";
	/** DOCUMENT ME! */
	public static final String TAX_ERROR_MESSAGE = "Enter a Valid Tax ID";

	/* List of all FORM ID Names */

	/** DOCUMENT ME! */
	public static final String FORM_ID_ORDERENTRYCONSUMERENTRY = "com.yantra.pca.ycd.rcp.tasks.customerConsumerEntry.wizards.YCDOrderEntryConsumerEntryWizard";
	/** DOCUMENT ME! */
	public static final String FORM_ID_PAYMENTINQUIRY = "com.yantra.pca.ycd.rcp.tasks.payment.wizards.YCDPaymentInquiryWizard";
	/** DOCUMENT ME! */
	public static final String FORM_ID_ORDERSUMMARY = "com.yantra.pca.ycd.rcp.tasks.orderSummary.wizards.YCDOrderSummaryWizard";
	/** DOCUMENT ME! */
	public static final String FORM_ID_CUSTOM_TAXIDPOPUP = "com.academy.custom.AcademyTaxIdPopUp";
	/** DOCUMENT ME! */
	public static final String FORM_ID_TAX_POP_UP = "com.academy.ecommerce.sterling.orderSummary.actions.TaxIdPopUpFormId";
	/** DOCUMENT ME! */
	public static final String FORM_ID_SHIPMENT_PANEL = "com.yantra.pca.ycd.rcp.tasks.common.fulfillmentSummary.screens.YCDShipmentPanel";
	/** DOCUMENT ME! */
	public static final String FORM_ID_ITEM_DETAILS = "com.yantra.pca.ycd.rcp.tasks.itemDetails.wizards.YCDItemDetailsWizard";
	/** DOCUMENT ME! */
	public static final String ACADEMY_TAX_ID_SUBMIT_ACTION = "com.academy.ecommerce.sterling.orderSummary.actions.TaxIdPopupSubmitAction";
	/** DOCUMENT ME! */
	public static final String GIFT_SHIP_APPLY_ACTIONID = "com.academy.ecommerce.sterling.fulfillmentSummary.actions.AcademyGiftShipApplyAction";
	/** DOCUMENT ME! */
	public static final String GIFT_SHIP_CLOSE_ACTIONID = "com.academy.ecommerce.sterling.fulfillmentSummary.actions.AcademyGiftShipCloseAction";
	/** DOCUMENT ME! */
	public static final String FORM_ID_GIFT_POP_UP = "com.yantra.pca.ycd.rcp.tasks.common.fulfillmentSummary.screens.YCDGiftShipPopup";
	/** DOCUMENT ME! */
	public static final String FORM_ID_ADDRESS_CAPTURE = "com.yantra.pca.ycd.rcp.tasks.addressCapture.wizards.YCDAddressCaptureWizard";;
	/** DOCUMENT ME! */
	public static final String FORM_ID_ADD_ITEM_PROMOTION_PANEL="com.academy.ecommerce.sterling.orderEntry.addItems.screens.AcademyPricingPromoPanel";
	/** DOCUMENT ME! */
	public static final String FORM_ID_LOAD_GIFT_CARD_PARENT="com.academy.ecommerce.sterling.orderSummary.extn.AcademyGiftCardLoad";
	/** DOCUMENT ME! */
	public static final String FORM_ID_FULFILLMENT_OPTIONS_CONTEXT = "AcademyFulfillmentOptionsContext";

	/* List of All XPath Expressions */
	/** DOCUMENT ME! */
	public static final String XPATH_COMMON_CODE_VALUE = "Extn_CommonCode:/CommonCodeList/CommonCode/@CodeValue";
	/** DOCUMENT ME! */
	public static final String XPATH_COMMON_CODE = "Extn_CommonCode:/CommonCodeList/CommonCode";
	/** DOCUMENT ME! */
	public static final String XPATH_ITEM_SHIP_ALONE = "ItemDetails/Extn/@ExtnShipAlone";
	/** DOCUMENT ME! */
	public static final String XPATH_ITEM_ITEMID = "Item/@ItemID";
	/** DOCUMENT ME! */
	public static final String XPATH_ORDERLINES_ORDERLINE = "OrderLines/OrderLine";
	/** DOCUMENT ME! */
	public static final String XPATH_PERSONINFO_PERSONINFOKEY = "PersonInfoShipTo/@PersonInfoKey";
	/** DOCUMENT ME! */
	public static final String XPATH_PERSONINFO_PO_BOX = "PersonInfoShipTo/Extn/@ExtnIsPOBOXADDRESS";
	/** DOCUMENT ME! */
	public static final String XPATH_ITEM_GIFT_CARD = "ItemDetails/Extn/@ExtnIsGiftCard";
	/** DOCUMENT ME! */
	public static final String XPATH_ORDERLINE_EXTN_TARGET_SHIP_ALONE = "OrderLine:/OrderLine/Item/Extn/@ExtnShipAlone";
	/** DOCUMENT ME! */
	public static final String XPATH_ITEM_EXTN_SOURCE_SHIP_ALONE = "ItemDetails:Item/Extn/@ExtnShipAlone";
	/** DOCUMENT ME! */
	public static final String XPATH_ORDERLINE_REQ_SHIP_DATE = "OrderLine:/OrderLine/@ReqShipDate";
	/** DOCUMENT ME! */
	public static final String XPATH_EXTN_SHIP_DATE = "Extn_FinalShipDate:Assignment/@ShipDate";
	/** DOCUMENT ME! */
	public static final String EXTN_COMBO_NAME = "extn_CmbCarrierService";
	/** DOCUMENT ME! */
	public static final String XPATH_SELECTED_ORDERLINES = "OrderLines/OrderLine[@Checked='Y']";
	/** DOCUMENT ME! */
	public static final String EXTN_SALES_ORDER_MODEL = "ExtnSalesOrderWithCarrier";
	/** DOCUMENT ME! */
	public static final String ELE_ACAD_LOS_REST = "ACADLosRest";
	/** DOCUMENT ME! */
	public static final String ATTR_LOS_VALUE = "LosValue";
	/** DOCUMENT ME! */
	public static final String ELE_ACAD_LOS_REST_LIST = "ACADLosRestList";
	/** DOCUMENT ME! */
	public static final String XPATH_ORDERLINE_REQ_DEL_DATE="OrderLine:/OrderLine/@ReqDeliveryDate";
	/**
	 * Constant for AcademyEncriptCreditCardInformation service/command
	 */
	public static final String ACADEMY_ENCRIPT_CC_COMMAND = "AcademyEncriptCreditCardInformation";
	/**
	 * Constant for AcademyEncriptCreditCardInformation service/command form ID
	 */
	public static final String ACADEMY_ENCRIPT_CC_COMMAND_FORMID = "com.yantra.pca.ycd.rcp.tasks.payment.screens.YCDPaymentConfirmationPanel";
	/**
	 * Constant for changeOrderForPaymentMethod API/command
	 */
	public static final String CHANGE_ORDER_FOR_PAYMENT_METHOD = "changeOrderForPaymentMethod";
	
	/** DOCUMENT ME! */
	public static final String ATTR_PRIME_LINE_NO= "PrimeLineNo";

	/*Added for CR 28*/
	public static final String ITEM_FORM_ID= "com.academy.ecommerce.sterling.itemDetails.screens.AcademyItemRestrictionsPanel";
	
	/*Added for CR 28*/
	public static final String ADDITIONAL_ATTRIBUTE= "AdditionalAttribute";
	
	/*Added for CR 28*/
	public static final String ADDITIONAL_ATTRIBUTE_VALUE= "Value";
	
	//START: STL-1129. User will not receive 0.00 balance Gift Card as Appeasement
	public static final String XPATH_AppeasementOffer= "AppeasementOffer[@Checked='"+ATTR_Y+"']";
	public static final String MODEL_APPEASMENT_OFFERS= "AppeasementOffers";
	public static final String ATTR_OFFER_AMOUNT= "OfferAmount";
	public static final String ATTR_ACADEMY_ORDER_HEADER_KEY= "AcademyOrderHeaderKey";
	//END: STL-1129. User will not receive 0.00 balance Gift Card as Appeasement
	
	//Start WN-2327 Not able to create appeasement for SOF orders
	public static final String XPATH_SOF_ORDERLINES = "/Order/OrderLines/OrderLine[@LineType='SOF']";
	//End WN-2327 Not able to create appeasement for SOF orders
	
	
}