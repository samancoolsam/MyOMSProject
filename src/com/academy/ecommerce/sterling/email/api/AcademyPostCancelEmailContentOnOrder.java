package com.academy.ecommerce.sterling.email.api;

/**#########################################################################################
*
* Project Name                : OMS_ESPPhase2_June2021
* Module                      : OMNI-20315
* Date                        : 20-APL-2021 
* Description				  : This class translates/updates Cancel EMail message xml posted to ESB queue.
* 								 
*
* #########################################################################################*/

import org.w3c.dom.Document;

import com.academy.ecommerce.sterling.cancel.AcademyPicktEmailTemplate;
import com.academy.ecommerce.sterling.email.util.AcademyEmailUtil;
import com.academy.ecommerce.sterling.shipment.AcademySFSLOSUpgradeDowngradeProcess;
import com.academy.ecommerce.sterling.util.AcademyCommonCode;
import com.academy.util.common.AcademyUtil;
import com.academy.util.common.StringUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.academy.util.xml.XPathUtil;
import com.ibm.icu.text.DateFormat;
import com.sterlingcommerce.baseutil.SCXmlUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class AcademyPostCancelEmailContentOnOrder {

	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPostCancelEmailContentOnOrder.class);

	/**
	 * This method customizes input xml to post cancel email message to ESB queue
	 * 
	 * @param inDoc
	 * @return docSMSMessage
	 * @throws Exception
	 */

	/*
	 * Sample Input xml:
	 * 
	 * <Order AllAddressesVerified="N"
	 * AuthorizationExpirationDate="2021-03-29T09:11:42-05:00" CarrierAccountNo=""
	 * CarrierServiceCode="" ChargeActualFreightFlag="N"
	 * CustomerEMailID="NagaShylaja.Garigipati@academy.com" CustomerPONo=""
	 * DeliveryCode="" Division="" DocumentType="0001" DraftOrderFlag="N"
	 * EmailText="We're sorry that the item(s) below have been canceled, as they were not picked up within the pick up window. The pending authorization on your credit card will be released within 7 business days. If a Gift Card was used, the funds will become available on the same card used for payment. We hope you visit us again at www.academy.com"
	 * EnterpriseCode="Academy_Direct" EntryType="" FreightTerms="" HoldFlag="N"
	 * HoldReasonCode="" IsBOPISEMailTemplateRequired="Y" MaxOrderStatus="9000"
	 * MaxOrderStatusDesc="Cancelled" MinOrderStatus="9000"
	 * MinOrderStatusDesc="Cancelled" Modifyprogid="ACAD_BOPIS_SHIP_MONITOR"
	 * NotifyAfterShipmentFlag="N" OrderDate="2021-02-24T12:09:49-06:00"
	 * OrderHeaderKey="202103290605072184370400" OrderName="" OrderNo="2021032901"
	 * OrderType="" OriginalTax="0.00" OriginalTotalAmount="151.53"
	 * OtherCharges="0.00" PaymentStatus="AUTHORIZED" PersonalizeCode=""
	 * PriorityCode="" PriorityNumber="0" Purpose="" SCAC="" ScacAndService=""
	 * ScacAndServiceKey="" SearchCriteria1="" SearchCriteria2=""
	 * SellerOrganizationCode="Academy_Direct" Status="Cancelled" TaxExemptFlag="N"
	 * TaxExemptionCertificate="" TaxJurisdiction="" TaxPayerId="" TermsCode=""
	 * TotalAdjustmentAmount="0.00" TransactionType="OnCancel" URL_ViewOrderDetails=
	 * "https://uat4www.academy.com/webapp/wcs/stores/servlet/UserAccountOrderStatus?orderId=2021032901&amp;zipCode=77449&amp;langId=-1&amp;storeId=10151&amp;catalogId=10051&amp;isSubmitted=true&amp;URL=NonAjaxOrderDetail&amp;errorViewName=GuestOrderStatusView&amp;splitshipstatus=true&amp;isDisplayLeftNav=false"
	 * URL_YouTube="https://www.youtube.com/user/academydotcom" isHistory="N"> <Extn
	 * ExtnTransactionID="DT900HYGKGH9"/> <PriceInfo ChangeInTotalAmount="-151.53"
	 * Currency="USD" EnterpriseCurrency="USD"
	 * ReportingConversionDate="2021-03-29T06:05:08-05:00"
	 * ReportingConversionRate="1.00" TotalAmount="0.00"/> <OrderLines> <OrderLine
	 * AllocationDate="2019-02-25" BackorderNotificationQty="0.00"
	 * CarrierAccountNo="" CarrierServiceCode="" ChangeInOrderedQty="-2.00"
	 * CustomerLinePONo="" CustomerPONo="" DeliveryCode="" DeliveryMethod="PICK"
	 * DepartmentCode="" FreightTerms="" FulfillmentType="BOPIS" HoldFlag="N"
	 * HoldReasonCode="" ImportLicenseNo="" InvoicedQty="0.00"
	 * IsFirmPredefinedNode="Y" ItemGroupCode="PROD" KitCode="" LineSeqNo="1.1"
	 * LineType="" MaxLineStatus="9000" MaxLineStatusDesc="Cancelled"
	 * MinLineStatus="9000" MinLineStatusDesc="Cancelled"
	 * Modifyprogid="ACAD_BOPIS_SHIP_MONITOR" OpenQty="0.00"
	 * OrderHeaderKey="202103290605072184370400"
	 * OrderLineKey="202103290605072184370401" OrderedQty="0.00"
	 * OriginalOrderedQty="2.00" OtherCharges="0.00" PackListType=""
	 * PersonalizeCode="" PersonalizeFlag="" PickableFlag="Y" PrimeLineNo="1"
	 * Purpose="" RemainingQty="0.00" ReqShipDate="2021-02-24T12:12:00-06:00"
	 * ReservationID="7305780453M" ReservationMandatory="N" ReservationPool=""
	 * SCAC="" ScacAndService="" ScacAndServiceKey="" ShipNode="033"
	 * ShipTogetherNo="" SplitQty="0.00" Status="Cancelled" StatusQuantity="0.00"
	 * SubLineNo="1" isHistory="N"> <Item CostCurrency="" CountryOfOrigin=""
	 * CustomerItem="022403976"
	 * CustomerItemDesc="CH L/S FR LWT TWILL SHIRT:NAVY:MEDIUM" ECCNNo=""
	 * HarmonizedCode="" ISBN="" ItemDesc="CH L/S FR LWT TWILL SHIRT:NAVY:MEDIUM"
	 * ItemID="022403976" ItemShortDesc="CH L/S FR LWT TWILL SHIRT:NAVY:MEDIUM"
	 * ItemWeight="1.10" ItemWeightUOM="LBS" ManufacturerItem="FRS003DNY-NAVY"
	 * ManufacturerItemDesc="" ManufacturerName="Carhartt" NMFCClass="" NMFCCode=""
	 * NMFCDescription="" ProductClass="GOOD" ProductLine="AcademyItemAttributes"
	 * ScheduleBCode="" SupplierItem="" SupplierItemDesc="" TaxProductCode="120"
	 * UPCCode="" UnitCost="41.00" UnitOfMeasure="EACH"/> <Extn
	 * ExtnWCOrderItemIdentifier="731193063"/> <LinePriceInfo
	 * ChangeInLineTotal="-151.53" LineTotal="0.00"/> <OrderDates/> <ItemDetails
	 * CanUseAsServiceTool="N" Createprogid="AcademyItemInterfaceServer"
	 * Createts="2012-03-20T23:11:25-05:00"
	 * Createuserid="AcademyItemInterfaceServer" DisplayItemId="022403976"
	 * GlobalItemID="00035481807817" InheritAttributesFromClassification="Y"
	 * IsShippingCntr="N" ItemGroupCode="PROD" ItemID="022403976"
	 * ItemKey="20120320231125257857978" Lockid="213"
	 * MaxModifyTS="2018-07-16T18:14:55-05:00"
	 * Modifyprogid="AcademyItemInterfaceServer"
	 * Modifyts="2018-07-16T18:14:55-05:00"
	 * Modifyuserid="AcademyItemInterfaceServer" OrganizationCode="DEFAULT"
	 * SyncTS="2018-07-16T00:00:00-05:00" UnitOfMeasure="EACH"> <PrimaryInformation
	 * AllowGiftWrap="N" AssumeInfiniteInventory="N" BundlePricingStrategy="PARENT"
	 * CapacityPerOrderedQty="0.00" CapacityQuantityStrategy="" CapacityUOM=""
	 * ColorCode="NAVY" ComputedUnitCost="0.00" CostCurrency=""
	 * CountryOfOrigin="DOMESTIC FOREIGN ORIGIN" CreditWOReceipt="N"
	 * DefaultProductClass="GOOD"
	 * Description="CH L/S FR LWT TWILL SHIRT:NAVY:MEDIUM"
	 * ExtendedDescription="The Carhartt Men s FlameResistant WorkDry Lightweight Twill Shirt is made of a blend of 88 cotton and 12 nylon. This flameresistant shirt features an antiodor fabric treatment and WorkDry material that wicks moisture away from the skin to help keep you feeling dry and comfortable. 2 chest pockets with flaps and button closures."
	 * ExtendedDisplayDescription="Carhartt Men's Flame-Resistant Work-Dry Lightweight Twill Shirt (022403976)"
	 * FixedCapacityQtyPerLine="0.00" FixedPricingQtyPerLine="0.00"
	 * ImageLocation="https://s7d2.scene7.com/is/image/academy/" ImageID="45465665"
	 * 
	 * InvoiceBasedOnActuals="N" InvolvesSegmentChange="N" IsAirShippingAllowed="Y"
	 * IsDeliveryAllowed="N" IsEligibleForShippingDiscount="Y" IsFreezerRequired="N"
	 * IsHazmat="N" IsModelItem="N" IsParcelShippingAllowed="Y" IsPickupAllowed="Y"
	 * IsReturnService="N" IsReturnable="Y" IsShippingAllowed="Y"
	 * IsStandaloneService="" IsSubOnOrderAllowed="" ItemType="NSANHV" KitCode=""
	 * ManufacturerItem="" ManufacturerItemDesc="" ManufacturerName="Carhartt"
	 * MasterCatalogID="" MaxOrderQuantity="0.00" MaxReturnWindow="365"
	 * MinOrderQuantity="0.00" MinimumCapacityQuantity="0.00"
	 * ModelItemUnitOfMeasure="EACH" NumSecondarySerials="0"
	 * OrderingQuantityStrategy="ENT" PricingQuantityConvFactor="0.00"
	 * PricingQuantityStrategy="" PricingUOM="" PricingUOMStrategy=""
	 * PrimaryEnterpriseCode="" PrimarySupplier=""
	 * ProductLine="AcademyItemAttributes" RequiresProdAssociation="N"
	 * ReturnShippingLabelLevel="P" ReturnWindow="365" RunQuantity="0.00"
	 * SerializedFlag="N" ServiceTypeID=""
	 * ShortDescription="CH L/S FR LWT TWILL SHIRT:NAVY:MEDIUM" SizeCode="MEDIUM"
	 * Status="3000" TaxableFlag="N" UnitCost="39.00" UnitHeight="1.90"
	 * UnitHeightUOM="IN" UnitLength="11.50" UnitLengthUOM="IN" UnitWeight="1.10"
	 * UnitWeightUOM="LBS" UnitWidth="8.40" UnitWidthUOM="IN"/> </ItemDetails>
	 * <LineOverallTotals Charges="0.00" Discount="0.00" ExtendedPrice="0.00"
	 * LineTotal="0.00" OptionPrice="0.00" PricingQty="0.00" Tax="0.00"
	 * UnitPrice="69.99"/> <StatusBreakupForCanceledQty> <CanceledFrom
	 * OrderLineScheduleKey="202103290642050884370405"
	 * OrderReleaseStatusKey="202103290911114084437091" Quantity="2.00"
	 * Status="1300" StatusDate="2021-03-29T09:11:40-05:00"
	 * StatusDescription="Backordered"> <Details
	 * ExpectedDeliveryDate="2021-02-24T12:12:00-06:00"
	 * ExpectedShipmentDate="2021-02-24T12:12:00-06:00" ShipNode="033"
	 * TagNumber=""/> </CanceledFrom> </StatusBreakupForCanceledQty> </OrderLine>
	 * </OrderLines> <PersonInfoShipTo AddressLine1="10441 Spring Green Blvd"
	 * AddressLine2="" AddressLine3="" AddressLine4="" AddressLine5=""
	 * AddressLine6="" AlternateEmailID="" Beeper="" City="Katy" Company="."
	 * Country="US" DayFaxNo="" DayPhone="6163501848" Department=""
	 * EMailID="NagaShylaja.Garigipati@academy.com" EveningFaxNo="" EveningPhone=""
	 * FirstName="Madhu" JobTitle="" LastName="Rima" MiddleName="" MobilePhone=""
	 * OtherPhone="" PersonID="" PersonInfoKey="202103240914392184006857" State="TX"
	 * Suffix="MadhuRima" Title="" ZipCode="77449"/> <PersonInfoBillTo
	 * AddressLine1="10441 Spring Green Blvd" AddressLine2="" AddressLine3=""
	 * AddressLine4="" AddressLine5="" AddressLine6="" AlternateEmailID="" Beeper=""
	 * City="Katy" Company="." Country="US" DayFaxNo="" DayPhone="6163501848"
	 * Department="" EMailID="NagaShylaja.Garigipati@academy.com" EveningFaxNo=""
	 * EveningPhone="" FirstName="Madhu" JobTitle="" LastName="Rima" MiddleName=""
	 * MobilePhone="" OtherPhone="" PersonID=""
	 * PersonInfoKey="202103240914392184006857" State="TX" Suffix="MadhuRima"
	 * Title="" ZipCode="77449"/> <PaymentMethods> <PaymentMethod
	 * CreditCardNo="0220991008051111" CreditCardType="Visa" PaymentReference1=""
	 * PaymentType="Credit_Card"/> </PaymentMethods> <OrderAudit
	 * AuditTransactionId=" " Modifyprogid="ACAD_BOPIS_SHIP_MONITOR"
	 * OrderAuditKey="202103290911422184437117"
	 * OrderHeaderKey="202103290605072184370400" ReasonCode="Customer Abandoned"
	 * ReasonText=" " Reference1=" " Reference2=" " Reference3=" " Reference4=" "
	 * XMLFlag="Y"> <Order EnterpriseCode="Academy_Direct" OrderNo="2021032901"/>
	 * <OrderAuditLevels> <OrderAuditLevel ModificationLevel="ORDER"
	 * ModificationLevelScreenName="Order" OrderLineKey=" " OrderReleaseKey=" ">
	 * <OrderAuditDetails> <OrderAuditDetail Action="" AuditType="OrderHeader">
	 * <Attributes> <Attribute ModificationType="OTHERS" Name="ApprovalCycle"
	 * NewValue="1" OldValue=""/> <Attribute ModificationType="OTHERS"
	 * Name="AuthorizationExpirationDate" NewValue="2021-03-29T09:11:42"
	 * OldValue="2021-04-05T06:15:02"/> <Attribute Name="TotalAmount"
	 * NewValue="0.00" OldValue="151.53"/> </Attributes> </OrderAuditDetail>
	 * </OrderAuditDetails> <ModificationTypes> <ModificationType Name="OTHERS"
	 * ScreenName="Change Other Attributes"/> </ModificationTypes>
	 * </OrderAuditLevel> <OrderAuditLevel ModificationLevel="ORDER_LINE"
	 * ModificationLevelScreenName="Line" OrderLineKey="202103290605072184370401"
	 * OrderReleaseKey=" "> <OrderLine PrimeLineNo="1" SubLineNo="1"/>
	 * <OrderAuditDetails> <OrderAuditDetail Action="" AuditType="OrderLine">
	 * <Attributes> <Attribute Name="ActualPricingQty" NewValue="0.00"
	 * OldValue="2.00"/> <Attribute Name="LineTotal" NewValue="0.00"
	 * OldValue="151.53"/> <Attribute Name="OrderedPricingQty" NewValue="0.00"
	 * OldValue="2.00"/> <Attribute Name="Tax" NewValue="0.00" OldValue="11.55"/>
	 * <Attribute Name="OrderedQty" NewValue="0.00" OldValue="2.00"/> </Attributes>
	 * </OrderAuditDetail> <OrderAuditDetail Action="" AuditType="LineTax"> <IDs>
	 * <ID Name="ChargeCategory" Value="TAXES"/> <ID Name="ChargeName"
	 * Value="Taxes"/> <ID Name="TaxName" Value="Merchandise"/> </IDs> <Attributes>
	 * <Attribute ModificationType="TAX" Name="Modifyprogid"
	 * NewValue="ACAD_BOPIS_SHIP_MONITOR" OldValue="SterlingHttpTester"/> <Attribute
	 * ModificationType="TAX" Name="Modifyts" NewValue="2021-03-29T09:11:42"
	 * OldValue="2021-03-29T06:05:08"/> <Attribute ModificationType="TAX" Name="Tax"
	 * NewValue="0.00" OldValue="11.55"/> <Attribute ModificationType="TAX"
	 * Name="Lockid" NewValue="1" OldValue="0"/> <Attribute ModificationType="TAX"
	 * Name="Modifyuserid" NewValue="AcademyBOPISCustomShipmentMonitor"
	 * OldValue="C0027824"/> </Attributes> </OrderAuditDetail> <OrderAuditDetail
	 * Action="Create" AuditType="Note"> <IDs> <ID Name="Tranid"
	 * Value="ORDER_CHANGE"/> <ID Name="SequenceNo" Value="1"/> </IDs> <Attributes>
	 * <Attribute Name="Createts" NewValue="2021-03-29T09:11:42" OldValue=""/>
	 * <Attribute Name="SequenceNo" NewValue="1" OldValue="0"/> <Attribute
	 * Name="Createuserid" NewValue="AcademyBOPISCustomShipmentMonitor"
	 * OldValue=""/> <Attribute Name="Createprogid"
	 * NewValue="ACAD_BOPIS_SHIP_MONITOR" OldValue=""/> <Attribute Name="NoteText"
	 * NewValue="Customer Abandoned" OldValue=""/> <Attribute Name="TableKey"
	 * NewValue="202103290605072184370401" OldValue=""/> <Attribute
	 * Name="ReasonCode" NewValue="Customer Abandoned" OldValue=""/> <Attribute
	 * Name="Tranid" NewValue="ORDER_CHANGE" OldValue=""/> <Attribute
	 * Name="Modifyuserid" NewValue="AcademyBOPISCustomShipmentMonitor"
	 * OldValue=""/> <Attribute Name="TableName" NewValue="YFS_ORDER_LINE"
	 * OldValue=""/> <Attribute Name="Modifyprogid"
	 * NewValue="ACAD_BOPIS_SHIP_MONITOR" OldValue=""/> <Attribute Name="Modifyts"
	 * NewValue="2021-03-29T09:11:42" OldValue=""/> <Attribute Name="NotesKey"
	 * NewValue="202103290911422184437118" OldValue=""/> </Attributes>
	 * </OrderAuditDetail> </OrderAuditDetails> <ModificationTypes>
	 * <ModificationType Name="CANCEL" ScreenName="Cancel"/> <ModificationType
	 * Name="ADD_NOTE" ScreenName="Add Note"/> </ModificationTypes>
	 * </OrderAuditLevel> </OrderAuditLevels> </OrderAudit> </Order>
	 * 
	 */
	public Document preparePostCancelEmailContent(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose(
				"AcademyPostBOPISEmailContentOnOrderCancel.prepareEmailContent()_InXML:" + XMLUtil.getXMLString(inDoc));
		// String IsBOPISEMailTemplateRequired =
		// inDoc.getDocumentElement().getAttribute("IsBOPISEMailTemplateRequired");
		Element eleIndoc = inDoc.getDocumentElement();
		String strCodeType=null;
		Document outputCommomCodeList=null;
		Element eleCommonCode=null;
		NodeList nleleCommonCode=null;
		boolean bFraudCancel = false;
		boolean bShortageCancel = false;
		
		String sReasonCode = XMLUtil.getAttributeFromXPath(inDoc, "Order/OrderAudit/@ReasonCode");
		String sCustomerCancel = XMLUtil.getAttributeFromXPath(inDoc, AcademyConstants.XPATH_ATTR_CUSTOMER_CANCEL);
		//Start : OMNI-72071 : Cancel Date contains the Past Date
		//String sCancelDate = XMLUtil.getAttributeFromXPath(inDoc, "//CanceledFrom/@StatusDate");
		String sCancelDate = XMLUtil.getAttributeFromXPath(inDoc, "Order/OrderAudit/@Modifyts");
		eleIndoc.setAttribute("CancelledDate", sCancelDate);
		//End : OMNI-72071 : Cancel Date contains the Past Date
		
		log.verbose("sReasonCode::::::::::::" + sReasonCode);
		// Auto Cancellation
		if (sReasonCode.equals("Customer Abandoned")) {
			eleIndoc.setAttribute("AutoCancelDate", sCancelDate);
			updateBOPISEMailTemplate(env, inDoc);
			
		} 
		// OMNI - 20306 Customer Cancellation--START
		else if (!YFCObject.isVoid(sCustomerCancel) && AcademyConstants.STR_TRUE.equalsIgnoreCase(sCustomerCancel)) {
			AcademyEmailUtil.updateMessageRef(env, eleIndoc, "CUSTOMER_CANCEL_MSG_ID_PROD",
					"CUSTOMER_CANCEL_MSG_ID_STG", "CUSTOMER_CANCEL_MSG_TYPE");
			updateEmailText(env, eleIndoc, "CUST_CANCEL_FULL_ORD_MSG_TEXT");
			// START OMNI-87539
            String sMinOrderStatus = XMLUtil.getAttributeFromXPath(inDoc, AcademyConstants.XPATH_ORDER_MINORDERSTATUS);
			String sMaxOrderStatus = XMLUtil.getAttributeFromXPath(inDoc, AcademyConstants.XPATH_ORDER_MAXORDERSTATUS);
			if (!YFCCommon.equalsIgnoreCase(AcademyConstants.VAL_CANCELLED_STATUS, sMinOrderStatus)
					&& !YFCCommon.equalsIgnoreCase(AcademyConstants.VAL_CANCELLED_STATUS, sMaxOrderStatus)) {
				eleIndoc.setAttribute(AcademyConstants.ATR_LISTRAK_TEXTCHANGE, AcademyConstants.ATTR_Y);
				log.verbose(" CustomerCancel - > TRUE :: OrderLevel Cancelation ");
			}else {
				eleIndoc.setAttribute(AcademyConstants.ATR_LISTRAK_TEXTCHANGE, AcademyConstants.ATTR_N);
				log.verbose(" CustomerCancel - > TRUE :: Order Cancelation ");
			}
		// END OMNI-87539
		} 
		// OMNI-20306 Customer Cancellation--END
		
		// OMNI-30972 Fraud Cancellation & OMNI-30980 Shortage Cancellation --START
		else if (!(StringUtil.isEmpty(sReasonCode))) {

			Document inputTocommonCodeList = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
			inputTocommonCodeList.getDocumentElement().setAttribute(AcademyConstants.ATTR_COMMON_CODE_VALUE, sReasonCode);
			inputTocommonCodeList.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_SHORT_DESC, sReasonCode);
			outputCommomCodeList = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMONCODE_LIST, inputTocommonCodeList);
			nleleCommonCode = outputCommomCodeList.getDocumentElement().getElementsByTagName(AcademyConstants.ELE_COMMON_CODE);
			if (nleleCommonCode.getLength() > 0) {
				for (int i = 0; i < nleleCommonCode.getLength(); i++) {
					eleCommonCode = (Element) nleleCommonCode.item(i);
					if (eleCommonCode != null && eleCommonCode.hasAttributes()) {
						strCodeType = eleCommonCode.getAttribute(AcademyConstants.ATTR_CODE_TYPE);
						log.verbose("Code Type From Commom Code:" + strCodeType);

						if (strCodeType.equalsIgnoreCase("FraudCancel")) {
							bFraudCancel = true;
							break;
						} else if (strCodeType.equalsIgnoreCase("ShortageCancel")) {
							bShortageCancel = true;
							break;
						}
						else {
							bShortageCancel = true;
						}
						
					} 
				}
			} 
			else {
				bShortageCancel = true;
			}
			
		} 
		else {
				bShortageCancel = true;
		} 
		if (bFraudCancel) {
			AcademyEmailUtil.updateMessageRef(env, eleIndoc, "FRAUD_CANCEL_MSG_ID_PROD", "FRAUD_CANCEL_MSG_ID_STAG",
					"FRAUD_CANCEL_MSG_TYPE");
			updateEmailText(env, eleIndoc, "FRAUD_CANCEL_MSG_TEXT");
		}

		else if (bShortageCancel){
			AcademyEmailUtil.updateMessageRef(env, eleIndoc, "SHORTAGE_CANCEL_MSG_ID_PROD",
					"SHORTAGE_CANCEL_MSG_ID_STAG", "SHORTAGE_CANCEL_MSG_TYPE");
			updateEmailText(env, eleIndoc, "SHORTAGE_CANCEL_MSG_TEXT");
		}
		// OMNI-30972 Fraud Cancellation & OMNI-30980 Shortage Cancellation --END
		sendEmail(env, inDoc);
		log.verbose(

				"AcademyPostBOPISEmailContentOnOrderCancel.prepareEmailContent()_returnDoc:"
						+ XMLUtil.getXMLString(inDoc));
		return inDoc;
	}

	/**
	 * This method updates Auto cancel email template XML to post to ESB queue.
	 * 
	 * @param inDoc
	 * @throws Exception
	 */
	private static void updateBOPISEMailTemplate(YFSEnvironment env, Document inDoc) throws Exception {
		log.verbose("Inside updateBOPISEMailTemplate Method");
		String sMsgIDProd = "BOPIS_AUTO_CNL_MSG_ID_PROD";
		String sMsgIDStg = "BOPIS_AUTO_CNL_MSG_ID_STG";
		String sMsgType = "BOPIS_AUTO_CNL_MSG_TYPE";
		String sMsgText = "BOPIS_AUTO_CNL_MSG_TEXT";
		Element eleIndoc = inDoc.getDocumentElement();
		AcademyEmailUtil.updateMessageRef(env, eleIndoc, sMsgIDProd, sMsgIDStg, sMsgType);
		updateEmailText(env, eleIndoc, sMsgText);
	}

	/**
	 * This method updates MessageID in input xml by performing a common code
	 * lookup.
	 * 
	 * @param strCodeType
	 * @throws Exception
	 */
	private static void updateEmailText(YFSEnvironment env, Element eleIndoc, String sMsgText) throws Exception {
		log.verbose("Inside updateEmailText Method");
		Document docGetReasonCodesInput = XMLUtil.createDocument(AcademyConstants.ELE_COMMON_CODE);
		docGetReasonCodesInput.getDocumentElement().setAttribute(AcademyConstants.ATTR_CODE_TYPE,
				AcademyConstants.STR_BOPIS_ORDER_CANCEL);
		docGetReasonCodesInput.getDocumentElement().setAttribute(AcademyConstants.ORGANIZATION_CODE,
				AcademyConstants.CONST_DEFAULT);
		Document docCommonCodeListOutput = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMMON_CODELIST,
				docGetReasonCodesInput);

		List<Node> eleOrderPickUp = XMLUtil.getElementListByXpath(docCommonCodeListOutput,
				"/CommonCodeList/*[contains(@CodeShortDescription,'" + sMsgText + "')]");

		String sEmailText = "";
		for (int i = 1; i <= eleOrderPickUp.size(); i++) {
			String strCodeValue = sMsgText + i;
			sEmailText = sEmailText + XMLUtil
					.getElementByXPath(docCommonCodeListOutput,
							"/CommonCodeList/CommonCode[@CodeValue='" + strCodeValue + "']")
					.getAttribute("CodeLongDescription");

		}
		log.verbose("EmailText::" + sEmailText);
		eleIndoc.setAttribute("EmailText", sEmailText);

	}

	/**
	 * this method will trigger send Email service
	 * 
	 * @param env
	 * @param inDoc
	 * @throws Exception
	 */
	private void sendEmail(YFSEnvironment env, Document inDoc) throws Exception {
		// send customer email
		log.verbose("Before Sending customer email :" + XMLUtil.getXMLString(inDoc));
		String customerEMailID = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID);
		if ((!YFCObject.isVoid(customerEMailID)) && customerEMailID.contains(",")) {
			customerEMailID = customerEMailID.substring(0, customerEMailID.indexOf(","));
			inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID, customerEMailID);
		}
		Document emailSentOutDoc = AcademyUtil.invokeService(env, "AcademyPostCancelEmailService", inDoc);
		log.verbose("Sent customer email :" + XMLUtil.getXMLString(emailSentOutDoc));
		// check and send alternate email
		if (AcademyEmailUtil.isAlternamePickUpCustomerEmailExist(inDoc.getDocumentElement())) {
			AcademyEmailUtil.setAlternamePickUpCustomerEmailExist(inDoc.getDocumentElement());
			log.verbose("Before Sending Alternate pickup email :" + XMLUtil.getXMLString(inDoc));
			emailSentOutDoc = AcademyUtil.invokeService(env, "AcademyPostCancelEmailService", inDoc);
			log.verbose("Sent Alternate pickup email :" + XMLUtil.getXMLString(emailSentOutDoc));
		}
	}
	
	public Document prepareEmailOnBopisCancel(YFSEnvironment env, Document inDoc) throws Exception {

		log.verbose("AcademyPostCancelEmailContentOnOrder.prepareEmailOnBopisCancel()_InXML start:" + XMLUtil.getXMLString(inDoc));
		Document orderDoc = callGetCompleteOrderDetails(env, inDoc);
		
		String sShipmentNo=inDoc.getDocumentElement().getAttribute("ShipmentNo");
		Element eleOrderDoc= orderDoc.getDocumentElement();
		removeOrderlines(env, eleOrderDoc,sShipmentNo);
		
		//Start : OMNI-72071 : Cancel Date contains the Past Date
		String strOLK = XMLUtil.getAttributeFromXPath(orderDoc, "Order/OrderLines/OrderLine/@OrderLineKey");
		Element eleOrderStatus = (Element) XPathUtil.getNode(orderDoc,
				"/Order/OrderStatuses/OrderStatus[@Status='9000' and @OrderLineKey='" +strOLK+"']");
		String strCancelDate = eleOrderStatus.getAttribute("StatusDate");
		//End : OMNI-72071 : Cancel Date contains the Past Date
		
		eleOrderDoc.setAttribute("CancelledDate", strCancelDate);
		AcademyEmailUtil.updateMessageRef(env, eleOrderDoc, "SHORTAGE_CANCEL_MSG_ID_PROD", "SHORTAGE_CANCEL_MSG_ID_STAG", "SHORTAGE_CANCEL_MSG_TYPE");
		updateEmailText(env, eleOrderDoc, "SHORTAGE_CANCEL_MSG_TEXT");
	    log.verbose("AcademyPostCancelEmailContentOnOrder.prepareEmailOnBopisCancel()_InXML end:" + XMLUtil.getXMLString(orderDoc));
	    sendBOPISCancelEmail(env, orderDoc);
	    return orderDoc;
	        
	}
	
	/**
	 * This method removes orderlines which are not cancelled
	 * @param inDoc
	 * @param eleIndoc
	 * @throws Exception
	 */
	
	public static void removeOrderlines(YFSEnvironment env,Element eleIndoc,String sShipmentNo) throws Exception {
		log.verbose("Inside removeOrderlines Method");
		NodeList currentShipmentLines = XPathUtil.getNodeList(eleIndoc, "/Order/Shipments/Shipment[@ShipmentNo='" + sShipmentNo + "']/ShipmentLines/ShipmentLine");
		Set<String> hsShipmentOrderLineKeys = new HashSet<String>();
		//Fetches the list of orderlinekey for the currentShipmentKey

		for (int i = 0; i < currentShipmentLines.getLength(); i++) {
			Element shipmentLine = (Element) currentShipmentLines.item(i);
			//fetch orderlineKey for current shipment line
			String strOrderlineKey =shipmentLine.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);
			//store orderlineKey in hashset
			hsShipmentOrderLineKeys.add(strOrderlineKey);

		}
		NodeList completeOrderLines =XPathUtil.getNodeList(eleIndoc, "/Order/OrderLines/OrderLine");
		for (int i = 0; i < completeOrderLines.getLength(); i++) {
			Element eleOrderline = (Element) completeOrderLines.item(i);
			//fetch orderlineKey for current orderline line
			String strOLK =eleOrderline.getAttribute(AcademyConstants.ATTR_ORDER_LINE_KEY);					
			if (!hsShipmentOrderLineKeys.contains(strOLK)) {					
				//remove current orderline from inDoc
				eleOrderline.getParentNode().removeChild(eleOrderline);
			}	
		}
	}
	
	private void sendBOPISCancelEmail(YFSEnvironment env, Document inDoc) throws Exception{
		// send customer email
		log.verbose("Before Sending BOPIS customer email :" + XMLUtil.getXMLString(inDoc));
		String customerEMailID = inDoc.getDocumentElement().getAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID);
		if ((!YFCObject.isVoid(customerEMailID)) && customerEMailID.contains(",")) {
			customerEMailID = customerEMailID.substring(0, customerEMailID.indexOf(","));
			inDoc.getDocumentElement().setAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID, customerEMailID);
		}
		Document emailSentOutDoc = AcademyUtil.invokeService(env, "AcademyPostBOPISCancelEmailService", inDoc);
		log.verbose("Sent customer email :" + XMLUtil.getXMLString(emailSentOutDoc));
		// check and send alternate email
		if (AcademyEmailUtil.isAlternamePickUpCustomerEmailExist(inDoc.getDocumentElement())) {
			AcademyEmailUtil.setAlternamePickUpCustomerEmailExist(inDoc.getDocumentElement());
			log.verbose("Before Sending Alternate pickup email :" + XMLUtil.getXMLString(inDoc));
			emailSentOutDoc = AcademyUtil.invokeService(env, "AcademyPostBOPISCancelEmailService", inDoc);
			log.verbose("Sent Alternate pickup email :" + XMLUtil.getXMLString(emailSentOutDoc));
		}
	
	}

	private Document callGetCompleteOrderDetails(YFSEnvironment env,Document inDoc) throws Exception {
		String sSalesOrderNo = XMLUtil.getAttributeFromXPath(inDoc, "Shipment/@OrderNo");
		Document outXML=null;
		Document inputXML=null;
		try {
			inputXML = XMLUtil.createDocument(AcademyConstants.ELE_ORDER);
			inputXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_ORDER_NO, sSalesOrderNo);
			inputXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_DOC_TYPE, AcademyConstants.SALES_DOCUMENT_TYPE);
			inputXML.getDocumentElement().setAttribute(AcademyConstants.ATTR_ENTERPRISE_CODE, AcademyConstants.PRIMARY_ENTERPRISE);
			Document templateDocGetCompleteOrderDetails = XMLUtil.getDocument("<Order DocumentType='' EnterpriseCode='' OrderHeaderKey=''  OrderNo=''  Status='' MaxOrderStatus='' CustomerEMailID=''>" + 
					"<PersonInfoBillTo ZipCode=''/>"+
					" 	<OrderStatuses><OrderStatus/></OrderStatuses>" +
					"		<OrderLines>" + 
					"			<OrderLine DeliveryMethod='' MaxLineStatus='' MinLineStatus='' OrderHeaderKey='' OrderLineKey='' OrderedQty='' OriginalOrderedQty='' "+
				    "             PrimeLineNo='' Status=''>" +
				    "<ItemDetails ItemID='' >"+
					"<PrimaryInformation Description='' ImageLocation='' ImageID=''/>"+
					"</ItemDetails>"+
					"</OrderLine>"+
					"</OrderLines>" + 
					"<Shipments><Shipment ShipmentNo='' ShipmentKey=''>"+
					"<ShipmentLines>"+
					"<ShipmentLine OrderLineKey='' ShipmentKey='' Quantity=''/>"+
					"</ShipmentLines></Shipment></Shipments>" + 
					"</Order>");
			
			env.setApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS, templateDocGetCompleteOrderDetails);	
			outXML = AcademyUtil.invokeAPI(env, AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS, inputXML);
			log.verbose("prepareEmailOnBopisCancel - getCompleteOrderDetails - " + XMLUtil.getXMLString(outXML));
			env.clearApiTemplate(AcademyConstants.API_GET_COMPLETE_ORDER_DETAILS);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		return outXML;
	}
	
	/**
	 * this method used to update alternate Email
	 * 
	 * @param eleIndoc
	 * @throws Exception
	 */
	public static void setAlternamePickUpCustomerEmailExist(Element eleIndoc) throws Exception {
		Element elePersonInfoMarkFor = SCXmlUtil.getChildElement(eleIndoc, AcademyConstants.ELE_PERSON_INFO_MARK_FOR);
		if (elePersonInfoMarkFor.hasAttribute(AcademyConstants.ATTR_EMAILID)) {
			String strAltEmailID = elePersonInfoMarkFor.getAttribute(AcademyConstants.ATTR_EMAILID);
			log.verbose("Alternate EmailId :" + strAltEmailID);
			eleIndoc.setAttribute(AcademyConstants.ATTR_CUST_EMAIL_ID, strAltEmailID);
		}
	}

	public static boolean isAlternamePickUpCustomerEmailExist(Element eleIndoc) throws Exception {
		Element elePersonInfoMarkFor = SCXmlUtil.getChildElement(eleIndoc, AcademyConstants.ELE_PERSON_INFO_MARK_FOR);
		return !YFCObject.isVoid(elePersonInfoMarkFor)
				&& elePersonInfoMarkFor.hasAttribute(AcademyConstants.ATTR_EMAILID) ? true : false;
	}

}
