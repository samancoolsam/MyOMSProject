<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output method="xml" doctype-system='Update_Academy_OrderStatus_10.dtd' indent="yes" />

<!-- <xslutput indent="yes" />   -->
   <xsl:template match="/">
      <Update_Academy_OrderStatus version="1.0">
         <ControlArea>
            <Verb value="Update" />
            <Noun value="Academy_OrderStatus" />
         </ControlArea>
         <DataArea>
            <OrderStatus>
               <OrderStatusHeader Versioning="FALSE" UpdateMode="P">
                  <OrderNumber type="ByWCS">
                     <xsl:value-of select="Shipment/OrderInvoiceList/OrderInvoice/@OrderNo" />
                  </OrderNumber>
                 </OrderStatusHeader>
                  <xsl:for-each select="Shipment/ShipmentLines/ShipmentLine">
                     <xsl:if test="./OrderLine/OrderStatuses/OrderStatus/@StatusDescription='Order Delivered'">
                        <OrderStatusItem Versioning="FALSE">
                           <ItemNumber type="ByWCS">
                              <xsl:value-of select="./OrderLine/Extn/@ExtnWCOrderItemIdentifier" />
                           </ItemNumber>                       
                           <QuantityInfo>
                              <RequestedQuantity>
                                 <xsl:value-of select="OrderLine/@OriginalOrderedQty" />
                              </RequestedQuantity>
                              <ConfirmedQuantity>
                                 <xsl:value-of select="OrderLine/@OrderedQty" />
                              </ConfirmedQuantity>
                              <DeliveredQuantity>
                                 <xsl:value-of select="@Quantity" />
                              </DeliveredQuantity>
                           </QuantityInfo>
                           <Status StatusCondition="I" StatusFlag="I" />
                        <!--  Invoice No --> 
                            <CustomerField>
                            <xsl:for-each select="../../OrderInvoiceList/OrderInvoice">
                            <xsl:if test="@InvoiceType='SHIPMENT'">
                             <xsl:value-of select="@InvoiceNo" /> 
                       </xsl:if> 
                            <xsl:choose>
                            <xsl:when test="position()!=last() and position()!=1">
                              <xsl:text>, </xsl:text> 
                              </xsl:when>
                              </xsl:choose>
                             </xsl:for-each>
                             </CustomerField>    
                             </OrderStatusItem>							 
                     </xsl:if>

                  </xsl:for-each>
               
            </OrderStatus>
         </DataArea>
      </Update_Academy_OrderStatus>
   </xsl:template>
</xsl:stylesheet>

