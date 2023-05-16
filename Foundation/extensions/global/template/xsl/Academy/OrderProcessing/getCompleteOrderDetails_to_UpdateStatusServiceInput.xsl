<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output indent="yes" />

   <xsl:template match="/">
      <Update_WCS_OrderStatus version="3.0">
         <ControlArea>
            <Verb value="Update">Verb</Verb>

            <Noun value="WCS_OrderStatus">Noun</Noun>
         </ControlArea>

         <DataArea>
            <OrderShipping>
               <OrderStatusHeader UpdateMode="C" Versioning="FALSE">
                  <OrderNumber type="ByWCS">
                     <xsl:text>0.0</xsl:text>
                  </OrderNumber>

                  <TotalPriceInfo>
                     <xsl:attribute name="Currency">
                        <xsl:text>USD</xsl:text>
                     </xsl:attribute>

                     <TotalNetPrice>
                        <xsl:text>0.0</xsl:text>
                     </TotalNetPrice>

                     <TotalTaxPrice>
                        <xsl:text>0.0</xsl:text>
                     </TotalTaxPrice>

                     <TotalShippingPrice>
                        <xsl:text>0.0</xsl:text>
                     </TotalShippingPrice>

                     <TotalTaxOnShippingPrice>
                        <xsl:text>0.0</xsl:text>
                     </TotalTaxOnShippingPrice>

                     <TotalSellingPrice>
                        <xsl:text>0.0</xsl:text>
                     </TotalSellingPrice>
                  </TotalPriceInfo>

                  <ShippingInfo ShipCondition="SC" ShipModeFlag="O">
                     <RequestedShipDate>
                        <xsl:value-of select="Order/Shipments/Shipment/@RequestedShipmentDate" />
                     </RequestedShipDate>

                     <ScheduledShipDate>
                        <xsl:value-of select="Order/Shipments/Shipment/@RequestedShipmentDate" />
                     </ScheduledShipDate>

                     <ActualShipDate>
                        <xsl:value-of select="Order/Shipments/Shipment/@ActualDeliveryDate" />
                     </ActualShipDate>
                  </ShippingInfo>
               </OrderStatusHeader>

               <xsl:for-each select="Order/OrderLines/OrderLine">
                  <OrderStatusItem Versioning="FALSE">
                     <ItemNumber type="ByWCS">
                        <xsl:value-of select="./ItemDetails/@ItemID" /><!-- ORDER ITEM IDENTIFIER, template-->
                     </ItemNumber>

                     <SerialNumber>
                        <xsl:value-of select="./ItemDetails/@ItemID" />
                     </SerialNumber>

                     <ProductNumberByMerchant />

                     <QuantityInfo>
                        <RequestedQuantity>
                           <xsl:value-of select="./@OriginalOrderedQty" />
                        </RequestedQuantity>

                        <ConfirmedQuantity>
                           <xsl:value-of select="./@OrderedQty" />
                        </ConfirmedQuantity>

                        <ShippedQuantity>
                           <xsl:value-of select="./OrderLineTranQuantity/@ShippedQuantity" />
                        </ShippedQuantity>
                     </QuantityInfo>

                     <ItemUnitPrice />

                     <Status StatusCondition="S" StatusFlag="O">
            
                     <xsl:choose>
                     	<xsl:when test="./@Status='Created'">
                     		<xsl:text>C</xsl:text>
                     	</xsl:when>
                       
                     	<xsl:otherwise>

		                     <xsl:choose>
		                     	<xsl:when test="./@Status='Cancelled'">
		                     		<xsl:text>X</xsl:text>
		                     	</xsl:when>
		                       
		                     	<xsl:otherwise>
		                     	 
				                     <xsl:choose>
				                     	<xsl:when test="./@Status='Shipped'">
				                     		<xsl:text>S</xsl:text>
				                     	</xsl:when>
				                       
				                     	<xsl:otherwise>

						                     <xsl:choose>
						                     	<xsl:when test="./@Status='Delivered'">
						                     		<xsl:text>I</xsl:text>
						                     	</xsl:when>
						                       
						                     	<xsl:otherwise>
						                     	 <xsl:text>1</xsl:text>
						                     	</xsl:otherwise>
						                     </xsl:choose>   
				                     	 
				                     	</xsl:otherwise>
				                     </xsl:choose>   

		                     	</xsl:otherwise>
		                     </xsl:choose>   

                     	</xsl:otherwise>
                     </xsl:choose>   
                        
                     </Status>

                     <Comment>
                     <xsl:choose>
                     	<xsl:when test="./@Status='Created'">
                     		<xsl:text>C</xsl:text>
                     	</xsl:when>
                       
                     	<xsl:otherwise>

		                     <xsl:choose>
		                     	<xsl:when test="./@Status='Cancelled'">
		                     		<xsl:text>X</xsl:text>
		                     	</xsl:when>
		                       
		                     	<xsl:otherwise>
		                     	 
				                     <xsl:choose>
				                     	<xsl:when test="./@Status='Shipped'">
				                     		<xsl:text>S</xsl:text>
				                     	</xsl:when>
				                       
				                     	<xsl:otherwise>

						                     <xsl:choose>
						                     	<xsl:when test="./@Status='Delivered'">
						                     		<xsl:text>I</xsl:text>
						                     	</xsl:when>
						                       
						                     	<xsl:otherwise>
						                     	 <xsl:text>1</xsl:text>
						                     	</xsl:otherwise>
						                     </xsl:choose>   
				                     	 
				                     	</xsl:otherwise>
				                     </xsl:choose>   

		                     	</xsl:otherwise>
		                     </xsl:choose>   

                     	</xsl:otherwise>
                     </xsl:choose>   
                     
                     </Comment>



<!-- Tracking Number -->

                     <CustomerField>
                     
                    
                     <xsl:variable  name = "shipContKey" select="./Containers/Container/@ShipmentContainerKey" />
                      
                     <xsl:for-each select="//Order/Containers/Container/ContainerDetails/ContainerDetail[./@ShipmentContainerKey=$shipContKey]" >
                     	<xsl:value-of select="../../@TrackingNo" />
                     
                     <xsl:choose>
                     	<xsl:when test="position()=last()">
                     		<xsl:text></xsl:text>
                     	</xsl:when>
                       
                     	<xsl:otherwise>
                     	 	<xsl:text>,</xsl:text>
                     	</xsl:otherwise>
                     </xsl:choose>
                     
                     </xsl:for-each>
                     
                     </CustomerField>
                     
<!-- Carrier Information -->
                    <CustomerField>
                      
	                     <xsl:variable  name = "shipLineKey" select="./ShipmentLines/ShipmentLine/@ShipmentLineKey" />
    		                 <xsl:for-each select="//Order/Shipments/Shipment/ShipmentLines/ShipmentLine[./@ShipmentLineKey=$shipLineKey]" >
            		         	<xsl:value-of select="../../@SCAC" />
                    	
                    				 <xsl:choose>
				                     	<xsl:when test="position()=last()">
				                     		<xsl:text></xsl:text>
				                     	</xsl:when>
                       
				                     	<xsl:otherwise>
	    			                 	 	<xsl:text>,</xsl:text>
	                			     	</xsl:otherwise>
                     				</xsl:choose>

		                     </xsl:for-each>	
                    	
                    </CustomerField>
<!-- Invoice No -->
                     <CustomerField1>
                     	<OrderLineKey>
                     		<xsl:value-of select="./@OrderLineKey" />
                     	</OrderLineKey>
                     	<OrderHeaderKey>
                     		<xsl:value-of select="./@OrderHeaderKey" />
                     	</OrderHeaderKey>
                     </CustomerField1>

                  </OrderStatusItem>
               </xsl:for-each>
            </OrderShipping>
         </DataArea>
      </Update_WCS_OrderStatus>
   </xsl:template>
</xsl:stylesheet>
