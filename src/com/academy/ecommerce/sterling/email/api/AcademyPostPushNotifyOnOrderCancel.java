package com.academy.ecommerce.sterling.email.api;

/**#########################################################################################
*
* Project Name                : OMS_ESPPhase2_June2021_Rel6
* Module                      : OMNI-38138,38155,38134 PostNotification Cancel
* Date                        : 24-MAY-2021 
* Description				  : This class prepares PostNotification Cancel message xml posted to  queue.
* 								 
*
* #########################################################################################*/

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.email.util.AcademyEmailUtil;
import com.academy.util.constants.AcademyConstants;
import com.academy.util.xml.XMLUtil;
import com.yantra.yfc.core.YFCObject;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;



public class AcademyPostPushNotifyOnOrderCancel {

	
	private static YFCLogCategory log = YFCLogCategory.instance(AcademyPostPushNotifyOnOrderCancel.class);

	/**
	 * This method customizes input xml to post post notification message to ESB queue
	 * 
	 * @param inDoc
	 * @return docSMSMessage
	 * @throws Exception
	 */

	/*
	 * Sample Input xml:
	 * "<Order AllAddressesVerified="N"
    AuthorizationExpirationDate="2021-05-19T05:52:32-05:00"
    CarrierAccountNo="" CarrierServiceCode=""
    ChargeActualFreightFlag="N"
    CustomerEMailID="sadhana.sarangan@academy.com" CustomerPONo=""
    DeliveryCode="" Division="" DocumentType="0001" DraftOrderFlag="N"
    EnterpriseCode="Academy_Direct" EntryType="" FreightTerms=""
    HoldFlag="N" HoldReasonCode="" IsBOPISEMailTemplateRequired="N"
    MaxOrderStatus="9000" MaxOrderStatusDesc="Cancelled"
    MinOrderStatus="9000" MinOrderStatusDesc="Cancelled"  MessageType="CustomerCancel"
    MessageID="" Modifyprogid="RestServlet" NotifyAfterShipmentFlag="N"
    OrderDate="2021-05-19T05:38:20-05:00"
    OrderHeaderKey="202105190541002850600021" OrderName=""
    OrderNo="731000007" OrderType="" OriginalTax="0.00"
    OriginalTotalAmount="74.38" OtherCharges="0.00"
    PaymentStatus="AUTHORIZED" PersonalizeCode="" PriorityCode=""
    PriorityNumber="0" Purpose="" SCAC="" ScacAndService=""
    ScacAndServiceKey="" SearchCriteria1="" SearchCriteria2=""
    SellerOrganizationCode="Academy_Direct" Status="Cancelled"
    TaxExemptFlag="N" TaxExemptionCertificate="" TaxJurisdiction=""
    TaxPayerId="" TermsCode="" TotalAdjustmentAmount="0.00"
    TransactionType="OnCancel" isHistory="N">
    <Extn ExtnTransactionID="D0X3039KQP0S"/>
    <PriceInfo ChangeInTotalAmount="-74.38" Currency="USD"
        EnterpriseCurrency="USD"
        ReportingConversionDate="2021-05-19T05:41:00-05:00"
        ReportingConversionRate="1.00" TotalAmount="0.00"/>
    <OrderLines>
        <OrderLine Action="CANCEL" AllocationDate="2019-05-20"
            BackorderNotificationQty="0.00" CarrierAccountNo=""
            CarrierServiceCode="Home Delivery"
            ChangeInOrderedQty="-1.00" CustomerLinePONo=""
            CustomerPONo="" DeliveryCode="" DeliveryMethod="SHP"
            DepartmentCode="" FreightTerms="" FulfillmentType="NON_BULK"
            HoldFlag="N" HoldReasonCode="" ImportLicenseNo=""
            InvoicedQty="0.00" IsFirmPredefinedNode="Y"
            ItemGroupCode="PROD" KitCode="" LineSeqNo="2.1" LineType=""
            MaxLineStatus="9000" MaxLineStatusDesc="Cancelled"
            MinLineStatus="9000" MinLineStatusDesc="Cancelled"
            Modifyprogid="RestServlet" OpenQty="0.00"
            OrderHeaderKey="202105190541002850600021"
            OrderLineKey="202105190541002850600023" OrderedQty="0.00"
            OriginalOrderedQty="1.00" OtherCharges="0.00"
            PackListType="" PersonalizeCode="" PersonalizeFlag=""
            PickableFlag="Y" PrimeLineNo="2" Purpose=""
            RemainingQty="0.00" ReqShipDate="2021-05-19T05:40:00-05:00"
            ReservationID="731000007" ReservationMandatory="N"
            ReservationPool="" SCAC="FEDX"
            ScacAndService="FedEx Home Delivery"
            ScacAndServiceKey="2004121615594445144" ShipTogetherNo=""
            SplitQty="0.00" Status="Cancelled" StatusQuantity="0.00"
            SubLineNo="1" isHistory="N">
            <Item CostCurrency="" CountryOfOrigin=""
                CustomerItem="122912180"
                CustomerItemDesc="Adidas M D2M Polo 2.0:Maroon:X Large"
                ECCNNo="" HarmonizedCode="" ISBN=""
                ItemDesc="Adidas M D2M Polo 2.0:Maroon:X Large"
                ItemID="122912180"
                ItemShortDesc="Adidas M D2M Polo 2.0:Maroon:X Large"
                ItemWeight="1.00" ItemWeightUOM="LBS"
                ManufacturerItem="GL0487" ManufacturerItemDesc=""
                ManufacturerName="ADIDAS AMERICA INC." NMFCClass=""
                NMFCCode="" NMFCDescription="" ProductClass="GOOD"
                ProductLine="AcademyItemAttributes" ScheduleBCode=""
                SupplierItem="" SupplierItemDesc="" TaxProductCode="120"
                UPCCode="" UnitCost="16.90" UnitOfMeasure="EACH"/>
            <Extn ExtnOriginalFulfillmentType="" ExtnWCOrderItemIdentifier="733333022"/>
            <LinePriceInfo ChangeInLineTotal="-37.19" LineTotal="0.00"/>
            <OrderDates>
                <OrderDate DateTypeId="ACADEMY_DELIVERY_DATE"
                    OrderHeaderKey="202105190541002850600021"
                    OrderLineKey="202105190541002850600023" OrderReleaseKey=""/>
                <OrderDate ActualDate="2500-01-01T00:00:00-06:00"
                    DateTypeId="YCD_FTC_CANCEL_DATE"
                    OrderHeaderKey="202105190541002850600021"
                    OrderLineKey="202105190541002850600023" OrderReleaseKey=""/>
                <OrderDate ActualDate="2021-06-18T00:00:00-05:00"
                    DateTypeId="YCD_FTC_FIRST_PROMISE_DATE"
                    OrderHeaderKey="202105190541002850600021"
                    OrderLineKey="202105190541002850600023" OrderReleaseKey=""/>
                <OrderDate DateTypeId="YCD_FTC_NEXT_PROMISE_DATE"
                    OrderHeaderKey="202105190541002850600021"
                    OrderLineKey="202105190541002850600023" OrderReleaseKey=""/>
                <OrderDate ActualDate="2021-06-18T00:00:00-05:00"
                    DateTypeId="YCD_FTC_PROMISE_DATE"
                    ExpectedDate="2021-06-18T00:00:00-05:00"
                    OrderHeaderKey="202105190541002850600021"
                    OrderLineKey="202105190541002850600023" OrderReleaseKey=""/>
            </OrderDates>
            <ItemDetails CanUseAsServiceTool="N"
                Createprogid="AcademyItemInterfaceServer"
                Createts="2019-09-26T12:03:28-05:00"
                Createuserid="AcademyItemInterfaceServer"
                DisplayItemId="122912180" GlobalItemID="0191984868646"
                InheritAttributesFromClassification="Y"
                IsShippingCntr="N" ItemGroupCode="PROD"
                ItemID="122912180" ItemKey="20190926120328224663832"
                Lockid="17" MaxModifyTS="2020-11-06T15:18:36-06:00"
                Modifyprogid="AcademyUpdateItemResInvInterfaceServer"
                Modifyts="2020-11-06T15:18:36-06:00"
                Modifyuserid="AcademyUpdateItemResInvInterfaceServer"
                OrganizationCode="DEFAULT"
                SyncTS="2020-03-27T00:00:00-05:00" UnitOfMeasure="EACH">
                <PrimaryInformation AllowGiftWrap="N"
                    AssumeInfiniteInventory="N"
                    BundlePricingStrategy="PARENT"
                    CapacityPerOrderedQty="0.00"
                    CapacityQuantityStrategy="" CapacityUOM=""
                    ColorCode="MAROON" ComputedUnitCost="0.00"
                    CostCurrency="" CountryOfOrigin=""
                    CreditWOReceipt="N" DefaultProductClass="GOOD"
                    Description="Adidas M D2M Polo 2.0:Maroon:X Large"
                    ExtendedDescription="Stay on the move with the adidas Men s Designed2Move Polo Shirt. Crafted with polyester doubleknit material, this polo provides a soft, comfortable feel. AEROREADY technology wicks away sweat to keep you cool and dry, and the button placket offers classic style."
                    ExtendedDisplayDescription="Adidas M D2M Polo 2019:Maroon:X Large (122912180)"
                    FixedCapacityQtyPerLine="0.00"
                    FixedPricingQtyPerLine="0.00" ImageID="20455319"
                    ImageLocation="https://s7d2.scene7.com/is/image/academy/"
                    InvoiceBasedOnActuals="N" InvolvesSegmentChange="N"
                    IsAirShippingAllowed="Y" IsDeliveryAllowed="N"
                    IsEligibleForShippingDiscount="Y"
                    IsFreezerRequired="N" IsHazmat="N" IsModelItem="N"
                    IsParcelShippingAllowed="Y" IsPickupAllowed="Y"
                    IsReturnService="N" IsReturnable="Y"
                    IsShippingAllowed="Y" IsStandaloneService=""
                    IsSubOnOrderAllowed="" ItemType="NSANHV" KitCode=""
                    ManufacturerItem="" ManufacturerItemDesc=""
                    ManufacturerName="ADIDAS AMERICA INC."
                    MasterCatalogID="" MaxOrderQuantity="0.00"
                    MaxReturnWindow="365" MinOrderQuantity="0.00"
                    MinimumCapacityQuantity="0.00"
                    ModelItemUnitOfMeasure="EACH"
                    NumSecondarySerials="0"
                    OrderingQuantityStrategy="ENT"
                    PricingQuantityConvFactor="0.00"
                    PricingQuantityStrategy="" PricingUOM=""
                    PricingUOMStrategy="" PrimaryEnterpriseCode=""
                    PrimarySupplier=""
                    ProductLine="AcademyItemAttributes"
                    RequiresProdAssociation="N"
                    ReturnShippingLabelLevel="P" ReturnWindow="365"
                    RunQuantity="0.00" SerializedFlag="N"
                    ServiceTypeID=""
                    ShortDescription="Adidas M D2M Polo 2.0:Maroon:X Large"
                    SizeCode="XLARGE" Status="3000" TaxableFlag="N"
                    UnitCost="16.90" UnitHeight="1.00"
                    UnitHeightUOM="IN" UnitLength="1.00"
                    UnitLengthUOM="IN" UnitWeight="1.00"
                    UnitWeightUOM="LBS" UnitWidth="1.00" UnitWidthUOM="IN"/>
            </ItemDetails>
            <LineOverallTotals Charges="0.00" Discount="0.00"
                ExtendedPrice="0.00" LineTotal="0.00" OptionPrice="0.00"
                PricingQty="0.00" Tax="0.00" UnitPrice="40.00"/>
            <StatusBreakupForCanceledQty>
                <CanceledFrom
                    OrderLineScheduleKey="202105190511461550604899"
                    OrderReleaseKey="202105190546152850604898"
                    OrderReleaseStatusKey="202105190515461550604900"
                    Quantity="1.00" Status="3200"
                    StatusDate="2021-05-19T05:46:15-05:00" StatusDescription="Released">
                    <Details
                        ExpectedDeliveryDate="2021-05-19T05:46:14-05:00"
                        ExpectedShipmentDate="2021-05-19T05:46:14-05:00"
                        ShipNode="701" TagNumber=""/>
                </CanceledFrom>
            </StatusBreakupForCanceledQty>
        </OrderLine>
        <OrderLine Action="CANCEL" AllocationDate="2019-05-20"
            BackorderNotificationQty="0.00" CarrierAccountNo=""
            CarrierServiceCode="Home Delivery"
            ChangeInOrderedQty="-1.00" CustomerLinePONo=""
            CustomerPONo="" DeliveryCode="" DeliveryMethod="SHP"
            DepartmentCode="" FreightTerms="" FulfillmentType="NON_BULK"
            HoldFlag="N" HoldReasonCode="" ImportLicenseNo=""
            InvoicedQty="0.00" IsFirmPredefinedNode="Y"
            ItemGroupCode="PROD" KitCode="" LineSeqNo="1.1" LineType=""
            MaxLineStatus="9000" MaxLineStatusDesc="Cancelled"
            MinLineStatus="9000" MinLineStatusDesc="Cancelled"
            Modifyprogid="RestServlet" OpenQty="0.00"
            OrderHeaderKey="202105190541002850600021"
            OrderLineKey="202105190541002850600022" OrderedQty="0.00"
            OriginalOrderedQty="1.00" OtherCharges="0.00"
            PackListType="" PersonalizeCode="" PersonalizeFlag=""
            PickableFlag="Y" PrimeLineNo="1" Purpose=""
            RemainingQty="0.00" ReqShipDate="2021-05-19T05:40:00-05:00"
            ReservationID="731000007" ReservationMandatory="N"
            ReservationPool="" SCAC="FEDX"
            ScacAndService="FedEx Home Delivery"
            ScacAndServiceKey="2004121615594445144" ShipTogetherNo=""
            SplitQty="0.00" Status="Cancelled" StatusQuantity="0.00"
            SubLineNo="1" isHistory="N">
            <Item CostCurrency="" CountryOfOrigin=""
                CustomerItem="122927373"
                CustomerItemDesc="Adidas M D2M Polo 2.0:Navy:Small"
                ECCNNo="" HarmonizedCode="" ISBN=""
                ItemDesc="Adidas M D2M Polo 2.0:Navy:Small"
                ItemID="122927373"
                ItemShortDesc="Adidas M D2M Polo 2.0:Navy:Small"
                ItemWeight="1.00" ItemWeightUOM="LBS"
                ManufacturerItem="GL0485" ManufacturerItemDesc=""
                ManufacturerName="ADIDAS AMERICA INC." NMFCClass=""
                NMFCCode="" NMFCDescription="" ProductClass="GOOD"
                ProductLine="AcademyItemAttributes" ScheduleBCode=""
                SupplierItem="" SupplierItemDesc="" TaxProductCode="120"
                UPCCode="" UnitCost="16.90" UnitOfMeasure="EACH"/>
            <Extn ExtnOriginalFulfillmentType="" ExtnWCOrderItemIdentifier="733333021"/>
            <LinePriceInfo ChangeInLineTotal="-37.19" LineTotal="0.00"/>
            <OrderDates>
                <OrderDate DateTypeId="ACADEMY_DELIVERY_DATE"
                    OrderHeaderKey="202105190541002850600021"
                    OrderLineKey="202105190541002850600022" OrderReleaseKey=""/>
                <OrderDate ActualDate="2500-01-01T00:00:00-06:00"
                    DateTypeId="YCD_FTC_CANCEL_DATE"
                    OrderHeaderKey="202105190541002850600021"
                    OrderLineKey="202105190541002850600022" OrderReleaseKey=""/>
                <OrderDate ActualDate="2021-06-18T00:00:00-05:00"
                    DateTypeId="YCD_FTC_FIRST_PROMISE_DATE"
                    OrderHeaderKey="202105190541002850600021"
                    OrderLineKey="202105190541002850600022" OrderReleaseKey=""/>
                <OrderDate DateTypeId="YCD_FTC_NEXT_PROMISE_DATE"
                    OrderHeaderKey="202105190541002850600021"
                    OrderLineKey="202105190541002850600022" OrderReleaseKey=""/>
                <OrderDate ActualDate="2021-06-18T00:00:00-05:00"
                    DateTypeId="YCD_FTC_PROMISE_DATE"
                    ExpectedDate="2021-06-18T00:00:00-05:00"
                    OrderHeaderKey="202105190541002850600021"
                    OrderLineKey="202105190541002850600022" OrderReleaseKey=""/>
            </OrderDates>
            <ItemDetails CanUseAsServiceTool="N"
                Createprogid="AcademyItemInterfaceServer"
                Createts="2019-09-30T18:38:22-05:00"
                Createuserid="AcademyItemInterfaceServer"
                DisplayItemId="122927373" GlobalItemID="0191984847276"
                InheritAttributesFromClassification="Y"
                IsShippingCntr="N" ItemGroupCode="PROD"
                ItemID="122927373" ItemKey="20190930183822265787854"
                Lockid="56" MaxModifyTS="2020-11-06T15:18:27-06:00"
                Modifyprogid="AcademyUpdateItemResInvInterfaceServer"
                Modifyts="2020-11-06T15:18:27-06:00"
                Modifyuserid="AcademyUpdateItemResInvInterfaceServer"
                OrganizationCode="DEFAULT"
                SyncTS="2020-03-27T00:00:00-05:00" UnitOfMeasure="EACH">
                <PrimaryInformation AllowGiftWrap="N"
                    AssumeInfiniteInventory="N"
                    BundlePricingStrategy="PARENT"
                    CapacityPerOrderedQty="0.00"
                    CapacityQuantityStrategy="" CapacityUOM=""
                    ColorCode="NAVY" ComputedUnitCost="0.00"
                    CostCurrency="" CountryOfOrigin=""
                    CreditWOReceipt="N" DefaultProductClass="GOOD"
                    Description="Adidas M D2M Polo 2.0:Navy:Small"
                    ExtendedDescription="Stay on the move with the adidas Men s Designed2Move Polo Shirt. Crafted with polyester doubleknit material, this polo provides a soft, comfortable feel. AEROREADY technology wicks away sweat to keep you cool and dry, and the button placket offers classic style."
                    ExtendedDisplayDescription="Adidas M D2M Polo 2.0:Navy:Small (122927373)"
                    FixedCapacityQtyPerLine="0.00"
                    FixedPricingQtyPerLine="0.00" ImageID="20463834"
                    ImageLocation="https://s7d2.scene7.com/is/image/academy/"
                    InvoiceBasedOnActuals="N" InvolvesSegmentChange="N"
                    IsAirShippingAllowed="Y" IsDeliveryAllowed="N"
                    IsEligibleForShippingDiscount="Y"
                    IsFreezerRequired="N" IsHazmat="N" IsModelItem="N"
                    IsParcelShippingAllowed="Y" IsPickupAllowed="Y"
                    IsReturnService="N" IsReturnable="Y"
                    IsShippingAllowed="Y" IsStandaloneService=""
                    IsSubOnOrderAllowed="" ItemType="NSANHV" KitCode=""
                    ManufacturerItem="" ManufacturerItemDesc=""
                    ManufacturerName="ADIDAS AMERICA INC."
                    MasterCatalogID="" MaxOrderQuantity="0.00"
                    MaxReturnWindow="365" MinOrderQuantity="0.00"
                    MinimumCapacityQuantity="0.00"
                    ModelItemUnitOfMeasure="EACH"
                    NumSecondarySerials="0"
                    OrderingQuantityStrategy="ENT"
                    PricingQuantityConvFactor="0.00"
                    PricingQuantityStrategy="" PricingUOM=""
                    PricingUOMStrategy="" PrimaryEnterpriseCode=""
                    PrimarySupplier=""
                    ProductLine="AcademyItemAttributes"
                    RequiresProdAssociation="N"
                    ReturnShippingLabelLevel="P" ReturnWindow="365"
                    RunQuantity="0.00" SerializedFlag="N"
                    ServiceTypeID=""
                    ShortDescription="Adidas M D2M Polo 2.0:Navy:Small"
                    SizeCode="SMALL" Status="3000" TaxableFlag="N"
                    UnitCost="16.90" UnitHeight="1.00"
                    UnitHeightUOM="IN" UnitLength="1.00"
                    UnitLengthUOM="IN" UnitWeight="1.00"
                    UnitWeightUOM="LBS" UnitWidth="1.00" UnitWidthUOM="IN"/>
            </ItemDetails>
            <LineOverallTotals Charges="0.00" Discount="0.00"
                ExtendedPrice="0.00" LineTotal="0.00" OptionPrice="0.00"
                PricingQty="0.00" Tax="0.00" UnitPrice="40.00"/>
            <StatusBreakupForCanceledQty>
                <CanceledFrom
                    OrderLineScheduleKey="202105190568461550604901"
                    OrderReleaseKey="202105190546152850604898"
                    OrderReleaseStatusKey="202105190522461550604902"
                    Quantity="1.00" Status="3200"
                    StatusDate="2021-05-19T05:46:15-05:00" StatusDescription="Released">
                    <Details
                        ExpectedDeliveryDate="2021-05-19T05:46:14-05:00"
                        ExpectedShipmentDate="2021-05-19T05:46:14-05:00"
                        ShipNode="701" TagNumber=""/>
                </CanceledFrom>
            </StatusBreakupForCanceledQty>
        </OrderLine>
    </OrderLines>
    <PersonInfoShipTo AddressLine1="1230 Grand West Boulevard"
        AddressLine2="" AddressLine3="" AddressLine4="" AddressLine5=""
        AddressLine6="" AlternateEmailID="" Beeper="" City="Katy"
        Company="" Country="US" DayFaxNo="" DayPhone="9489923737"
        Department="" EMailID="sadhana.sarangan@academy.com"
        EveningFaxNo="" EveningPhone="" FirstName="Sadhana" JobTitle=""
        LastName="S" MiddleName="" MobilePhone="" OtherPhone=""
        PersonID="" PersonInfoKey="202105180935002849953239" State="TX"
        Suffix="SadhanaS" Title="" ZipCode="77449"/>
    <PersonInfoBillTo AddressLine1="1230 Grand West Boulevard"
        AddressLine2="" AddressLine3="" AddressLine4="" AddressLine5=""
        AddressLine6="" AlternateEmailID="" Beeper="" City="Katy"
        Company="" Country="US" DayFaxNo="" DayPhone="9489923737"
        Department="" EMailID="sadhana.sarangan@academy.com"
        EveningFaxNo="" EveningPhone="" FirstName="Sadhana" JobTitle=""
        LastName="S" MiddleName="" MobilePhone="" OtherPhone=""
        PersonID="" PersonInfoKey="202105180935002849953239" State="TX"
        Suffix="SadhanaS" Title="" ZipCode="77449"/>
    <PaymentMethods>
        <PaymentMethod CreditCardNo="8936629524921111"
            CreditCardType="Visa" PaymentReference1="" PaymentType="Credit_Card"/>
    </PaymentMethods>
    <OrderAudit AuditTransactionId=" " Modifyprogid="RestServlet"
        OrderAuditKey="202105190552322850607330"
        OrderHeaderKey="202105190541002850600021"
        ReasonCode="Customer Requested - WCS" ReasonText=" "
        Reference1=" " Reference2=" " Reference3=" " Reference4=" " XMLFlag="Y">
        <Order EnterpriseCode="Academy_Direct" OrderNo="731000007"/>
        <OrderAuditLevels>
            <OrderAuditLevel ModificationLevel="ORDER"
                ModificationLevelScreenName="Order" OrderLineKey=" " OrderReleaseKey=" ">
                <OrderAuditDetails>
                    <OrderAuditDetail Action="" AuditType="OrderHeader">
                        <Attributes>
                            <Attribute ModificationType="OTHERS"
                                Name="ApprovalCycle" NewValue="1" OldValue=""/>
                            <Attribute ModificationType="OTHERS"
                                Name="AuthorizationExpirationDate"
                                NewValue="2021-05-19T05:52:32" OldValue="2021-05-26T00:00:00"/>
                            <Attribute Name="TotalAmount"
                                NewValue="0.00" OldValue="74.38"/>
                        </Attributes>
                    </OrderAuditDetail>
                </OrderAuditDetails>
                <ModificationTypes>
                    <ModificationType Name="OTHERS" ScreenName="Change Other Attributes"/>
                    <ModificationType Name="CANCEL" ScreenName="Cancel"/>
                </ModificationTypes>
            </OrderAuditLevel>
            <OrderAuditLevel ModificationLevel="ORDER_LINE"
                ModificationLevelScreenName="Line"
                OrderLineKey="202105190541002850600023" OrderReleaseKey=" ">
                <OrderLine PrimeLineNo="2" SubLineNo="1"/>
                <OrderAuditDetails>
                    <OrderAuditDetail Action="" AuditType="OrderLine">
                        <Attributes>
                            <Attribute Name="ActualPricingQty"
                                NewValue="0.00" OldValue="1.00"/>
                            <Attribute Name="LineTotal" NewValue="0.00" OldValue="37.19"/>
                            <Attribute Name="OrderedPricingQty"
                                NewValue="0.00" OldValue="1.00"/>
                            <Attribute Name="Tax" NewValue="0.00" OldValue="2.19"/>
                            <Attribute Name="OtherCharges"
                                NewValue="0.00" OldValue="-5.00"/>
                            <Attribute Name="OrderedQty" NewValue="0.00" OldValue="1.00"/>
                        </Attributes>
                    </OrderAuditDetail>
                    <OrderAuditDetail Action="" AuditType="LineCharge">
                        <IDs>
                            <ID Name="ChargeCategory" Value="Promotions"/>
                            <ID Name="ChargeName" Value="ShippingPromotion"/>
                        </IDs>
                        <Attributes>
                            <Attribute ModificationType="PRICE"
                                Name="ChargePerUnit" NewValue="0.00" OldValue="4.79"/>
                            <Attribute ModificationType="PRICE"
                                Name="Modifyprogid"
                                NewValue="RestServlet" OldValue="AcademyCreateOrderInterfaceServer"/>
                            <Attribute ModificationType="PRICE"
                                Name="Modifyts"
                                NewValue="2021-05-19T05:52:32" OldValue="2021-05-19T05:41:00"/>
                            <Attribute ModificationType="PRICE"
                                Name="ChargeAmount" NewValue="0.00" OldValue="4.79"/>
                            <Attribute ModificationType="PRICE"
                                Name="Lockid" NewValue="1" OldValue="0"/>
                            <Attribute ModificationType="PRICE"
                                Name="Modifyuserid" NewValue="svcornapi" OldValue="AcademyCreateOrderInterfaceServer"/>
                        </Attributes>
                    </OrderAuditDetail>
                    <OrderAuditDetail Action="" AuditType="LineCharge">
                        <IDs>
                            <ID Name="ChargeCategory" Value="Promotions"/>
                            <ID Name="ChargeName" Value="PROMO1"/>
                        </IDs>
                        <Attributes>
                            <Attribute ModificationType="PRICE"
                                Name="ChargePerUnit" NewValue="0.00" OldValue="5.00"/>
                            <Attribute ModificationType="PRICE"
                                Name="Modifyprogid"
                                NewValue="RestServlet" OldValue="AcademyCreateOrderInterfaceServer"/>
                            <Attribute ModificationType="PRICE"
                                Name="Modifyts"
                                NewValue="2021-05-19T05:52:32" OldValue="2021-05-19T05:41:00"/>
                            <Attribute ModificationType="PRICE"
                                Name="ChargeAmount" NewValue="0.00" OldValue="5.00"/>
                            <Attribute ModificationType="PRICE"
                                Name="Lockid" NewValue="1" OldValue="0"/>
                            <Attribute ModificationType="PRICE"
                                Name="Modifyuserid" NewValue="svcornapi" OldValue="AcademyCreateOrderInterfaceServer"/>
                        </Attributes>
                    </OrderAuditDetail>
                    <OrderAuditDetail Action="" AuditType="LineCharge">
                        <IDs>
                            <ID Name="ChargeCategory" Value="Shipping"/>
                            <ID Name="ChargeName" Value="ShippingCharge"/>
                        </IDs>
                        <Attributes>
                            <Attribute ModificationType="PRICE"
                                Name="ChargePerUnit" NewValue="0.00" OldValue="4.79"/>
                            <Attribute ModificationType="PRICE"
                                Name="Modifyprogid"
                                NewValue="RestServlet" OldValue="AcademyCreateOrderInterfaceServer"/>
                            <Attribute ModificationType="PRICE"
                                Name="Modifyts"
                                NewValue="2021-05-19T05:52:32" OldValue="2021-05-19T05:41:00"/>
                            <Attribute ModificationType="PRICE"
                                Name="ChargeAmount" NewValue="0.00" OldValue="4.79"/>
                            <Attribute ModificationType="PRICE"
                                Name="Lockid" NewValue="1" OldValue="0"/>
                            <Attribute ModificationType="PRICE"
                                Name="Modifyuserid" NewValue="svcornapi" OldValue="AcademyCreateOrderInterfaceServer"/>
                        </Attributes>
                    </OrderAuditDetail>
                    <OrderAuditDetail Action="" AuditType="LineTax">
                        <IDs>
                            <ID Name="ChargeCategory" Value="TAXES"/>
                            <ID Name="ChargeName" Value="Taxes"/>
                            <ID Name="TaxName" Value="Merchandise"/>
                        </IDs>
                        <Attributes>
                            <Attribute ModificationType="TAX"
                                Name="Modifyprogid"
                                NewValue="RestServlet" OldValue="AcademyCreateOrderInterfaceServer"/>
                            <Attribute ModificationType="TAX"
                                Name="Modifyts"
                                NewValue="2021-05-19T05:52:32" OldValue="2021-05-19T05:41:00"/>
                            <Attribute ModificationType="TAX" Name="Tax"
                                NewValue="0.00" OldValue="2.19"/>
                            <Attribute ModificationType="TAX"
                                Name="Lockid" NewValue="1" OldValue="0"/>
                            <Attribute ModificationType="TAX"
                                Name="Modifyuserid" NewValue="svcornapi" OldValue="AcademyCreateOrderInterfaceServer"/>
                        </Attributes>
                    </OrderAuditDetail>
                    <OrderAuditDetail Action="Create" AuditType="Note">
                        <IDs>
                            <ID Name="Tranid" Value="ORDER_CHANGE"/>
                            <ID Name="SequenceNo" Value="1"/>
                        </IDs>
                        <Attributes>
                            <Attribute Name="Createts"
                                NewValue="2021-05-19T05:52:32" OldValue=""/>
                            <Attribute Name="SequenceNo" NewValue="1" OldValue="0"/>
                            <Attribute Name="Createuserid"
                                NewValue="svcornapi" OldValue=""/>
                            <Attribute Name="Createprogid"
                                NewValue="RestServlet" OldValue=""/>
                            <Attribute Name="NoteText"
                                NewValue="Customer Requested" OldValue=""/>
                            <Attribute Name="TableKey"
                                NewValue="202105190541002850600023" OldValue=""/>
                            <Attribute Name="ReasonCode"
                                NewValue="Customer Requested - WCS" OldValue=""/>
                            <Attribute Name="Tranid"
                                NewValue="ORDER_CHANGE" OldValue=""/>
                            <Attribute Name="Modifyuserid"
                                NewValue="svcornapi" OldValue=""/>
                            <Attribute Name="ContactUser"
                                NewValue="1225049" OldValue=""/>
                            <Attribute Name="TableName"
                                NewValue="YFS_ORDER_LINE" OldValue=""/>
                            <Attribute Name="Modifyprogid"
                                NewValue="RestServlet" OldValue=""/>
                            <Attribute Name="Modifyts"
                                NewValue="2021-05-19T05:52:32" OldValue=""/>
                            <Attribute Name="NotesKey"
                                NewValue="202105190552322850607331" OldValue=""/>
                            <Attribute Name="ContactReference"
                                NewValue="WCS" OldValue=""/>
                        </Attributes>
                    </OrderAuditDetail>
                </OrderAuditDetails>
                <ModificationTypes>
                    <ModificationType Name="CANCEL" ScreenName="Cancel"/>
                    <ModificationType Name="ADD_NOTE" ScreenName="Add Note"/>
                </ModificationTypes>
            </OrderAuditLevel>
            <OrderAuditLevel ModificationLevel="ORDER_LINE"
                ModificationLevelScreenName="Line"
                OrderLineKey="202105190541002850600022" OrderReleaseKey=" ">
                <OrderLine PrimeLineNo="1" SubLineNo="1"/>
                <OrderAuditDetails>
                    <OrderAuditDetail Action="" AuditType="OrderLine">
                        <Attributes>
                            <Attribute Name="ActualPricingQty"
                                NewValue="0.00" OldValue="1.00"/>
                            <Attribute Name="LineTotal" NewValue="0.00" OldValue="37.19"/>
                            <Attribute Name="OrderedPricingQty"
                                NewValue="0.00" OldValue="1.00"/>
                            <Attribute Name="Tax" NewValue="0.00" OldValue="2.19"/>
                            <Attribute Name="OtherCharges"
                                NewValue="0.00" OldValue="-5.00"/>
                            <Attribute Name="OrderedQty" NewValue="0.00" OldValue="1.00"/>
                        </Attributes>
                    </OrderAuditDetail>
                    <OrderAuditDetail Action="" AuditType="LineCharge">
                        <IDs>
                            <ID Name="ChargeCategory" Value="Promotions"/>
                            <ID Name="ChargeName" Value="ShippingPromotion"/>
                        </IDs>
                        <Attributes>
                            <Attribute ModificationType="PRICE"
                                Name="ChargePerUnit" NewValue="0.00" OldValue="4.79"/>
                            <Attribute ModificationType="PRICE"
                                Name="Modifyprogid"
                                NewValue="RestServlet" OldValue="AcademyCrea"
	 * 
	 * 
	 */
	public Document preparePostNotificationMessage(YFSEnvironment env, Document inDoc) throws Exception {


		log.verbose("AcademyPostPushNotifyOnOrderCancel.preparePostNotificationMessage()_InXML start:" + XMLUtil.getXMLString(inDoc));

		  Element eleIndoc=inDoc.getDocumentElement();
		  String sReasonCode = XMLUtil.getAttributeFromXPath(inDoc, AcademyConstants.XPATH_ATTR_REASONCODE);
		  log.verbose("Reason Code is ::::::"+sReasonCode);
		  String sMaxOrderStatus =eleIndoc.getAttribute(AcademyConstants.ATTR_MAX_ORDER_STATUS);
		  log.verbose("MaxOrderStatus ::::::"+sMaxOrderStatus);
		  boolean partialCancellation = false;
		  if(!YFCObject.isVoid(sMaxOrderStatus)
	   				&& !sMaxOrderStatus.equalsIgnoreCase(AcademyConstants.VAL_CANCELLED_STATUS))

		  {
			  //if MaxOrderStaus is not 9000, set partialCancelleation value to true.
			  partialCancellation=true;
		  }

	      String sCustomerCancel = XMLUtil.getAttributeFromXPath(inDoc, AcademyConstants.XPATH_ATTR_CUSTOMER_CANCEL);
	      /*Element eleIndoc=inDoc.getDocumentElement();
	         if (!YFCObject.isVoid(sReasonCode)
	 				&& sReasonCode.equals(AcademyConstants.STR_KOUNT_DENY)) {
	        	log.verbose("Cancellation type:Kount_Deny");
	        	AcademyEmailUtil.updateMessageRef(env, eleIndoc, "GENERAL_CANCEL_MSG_ID_PROD", "GENERAL_CANCEL_MSG_ID_STG", "GENERAL_CANCEL_MSG_TYPE");
	        	return inDoc;
	         }*/
	      //Auto cancel -OMNI-38155
	         if (!YFCObject.isVoid(sReasonCode)
	  				&& sReasonCode.equals(AcademyConstants.STR_CUSTOMER_ABANDONED)) {
	         	log.verbose("Cancellation type:Customer Abondoned");
	         	AcademyEmailUtil.updateMessageRef(env, eleIndoc, "AUTO_CANCEL_MSG_ID_PROD", "AUTO_CANCEL_MSG_ID_STG", "AUTO_CANCEL_MSG_TYPE");
	         	return inDoc;
	          }
	      //customer cancel -OMNI-38134
	         if (!YFCObject.isVoid(sCustomerCancel)
	   				&& sCustomerCancel.equalsIgnoreCase(AcademyConstants.STR_TRUE)) {
	        	 //AcademyEmailUtil.updateMessageRef(env, eleIndoc, "CUSTOMER_CANCEL_MSG_ID_PROD", "CUSTOMER_CANCEL_MSG_ID_STG", "CUSTOMER_CANCEL_MSG_TYPE");
	        	 if(partialCancellation) {
	        		 AcademyEmailUtil.updateMessageRef(env, eleIndoc, "CUSTOMER_PARTIAL_CANCEL_MSG_ID_PROD", "CUSTOMER_PARTIAL_CANCEL_MSG_ID_STG", "CUSTOMER_PARTIAL_CANCEL_MSG_TYPE");	 
	        	 }else {
	        		 AcademyEmailUtil.updateMessageRef(env, eleIndoc, "CUSTOMER_CANCEL_MSG_ID_PROD", "CUSTOMER_CANCEL_MSG_ID_STG", "CUSTOMER_CANCEL_MSG_TYPE");
	        	 }
	        	 return inDoc;
	           }  

	         //AcademyEmailUtil.updateMessageRef(env, eleIndoc, "GENERAL_CANCEL_MSG_ID_PROD", "GENERAL_CANCEL_MSG_ID_STG", "GENERAL_CANCEL_MSG_TYPE");
	         
	       //Academy cancel  - OMNI-38138
	         else {

	        	 if(partialCancellation) {
	        		 AcademyEmailUtil.updateMessageRef(env, eleIndoc, "ACADEMY_PARTIAL_CANCEL_MSG_ID_PROD", "ACADEMY_PARTIAL_CANCEL_MSG_ID_STG", "ACADEMY_PARTIAL_CANCEL_MSG_TYPE");
		         }else {

		        	 AcademyEmailUtil.updateMessageRef(env, eleIndoc, "ACADEMY_CANCEL_MSG_ID_PROD", "ACADEMY_CANCEL_MSG_ID_STG", "ACADEMY_CANCEL_MSG_TYPE");
		         }
	         log.verbose("AcademyPostPushNotifyOnOrderCancel.preparePostNotificationMessage()_InXML end:" + XMLUtil.getXMLString(inDoc));
	         return inDoc;
	         }

	        

	}
	

}
