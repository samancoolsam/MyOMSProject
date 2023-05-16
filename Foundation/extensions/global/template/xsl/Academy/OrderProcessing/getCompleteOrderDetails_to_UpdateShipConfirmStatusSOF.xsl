<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" doctype-system='Update_Academy_OrderStatus_10.dtd' indent="yes" />   
   	<xsl:template match="/">
      <Update_Academy_OrderStatus version="1.0">
         <ControlArea>
            <Verb value="Update"/>
            <Noun value="Academy_OrderStatus"/>
         </ControlArea>

         <DataArea>
            <OrderShipping>
               <OrderStatusHeader Versioning="FALSE" UpdateMode="P">
                  <OrderNumber type="ByWCS">
				  	<xsl:value-of select="Shipment/ShipmentLines/ShipmentLine[1]/@OrderNo" />
                  </OrderNumber>
               </OrderStatusHeader>
		

		<!-- Start STL-1547 -->
		<xsl:variable name="scac">			
			<xsl:choose>
				<xsl:when test="contains(Shipment/@SCAC, 'USPS')">
					<xsl:value-of select="'USPS'" />
				</xsl:when>
				<xsl:when test="Shipment/@SCAC = 'SmartPost'">
					<xsl:value-of select="'FEDX'" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="Shipment/@SCAC" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>	
		<!-- End STL-1547 -->
	
               <xsl:for-each select="Shipment/ShipmentLines/ShipmentLine">
               <xsl:if test="@Quantity != 0.00">
					<!-- Each message to WCS will have information of only those items that got shipped in the shipment -->
					<OrderStatusItem Versioning="FALSE">
						<xsl:variable name="orderLineKey" select="OrderLine/@OrderLineKey"/>
					<!--	<xsl:variable name="OrderLineKey" select="../../../../InputDocument/Order/OrderLines/OrderLine/@OrderLineKey"/> -->
						<ItemNumber type="ByWCS">
							<xsl:value-of select="OrderLine/Extn/@ExtnWCOrderItemIdentifier" />
						</ItemNumber>

						<QuantityInfo>
                			<RequestedQuantity>
                   				<xsl:value-of select="OrderLine/@OriginalOrderedQty" />
                			</RequestedQuantity>

                			<ConfirmedQuantity>
								<xsl:value-of select="OrderLine/@OrderedQty" />
                			</ConfirmedQuantity>

                			<ShippedQuantity>
								<xsl:value-of select="@Quantity" />
                			</ShippedQuantity>
             			</QuantityInfo>

             			<Status StatusFlag="I">
							<xsl:attribute name="StatusCondition">
									<xsl:value-of select="../../@StatusCondition"/>
							</xsl:attribute>
						</Status>
             
						<ShippingInfo ShipCondition="SC" ShipModeFlag="I">
							<ActualShipDate></ActualShipDate>
						</ShippingInfo>							
<!-- Invoice No -->
						<CustomerField>
							<!-- <xsl:value-of select="../../OrderInvoiceList/OrderInvoice[@InvoiceType='SHIPMENT']/@InvoiceNo" /> -->
						</CustomerField>

						<CustomerField></CustomerField>

					</OrderStatusItem>
				</xsl:if>
				</xsl:for-each>

            </OrderShipping>
         </DataArea>

      </Update_Academy_OrderStatus>
   </xsl:template>
</xsl:stylesheet>