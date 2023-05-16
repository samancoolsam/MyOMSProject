<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" doctype-system='Update_Academy_OrderStatus_10.dtd' indent="yes" />   
<!-- <xsl:output indent="yes" />  -->   

   <xsl:template match="/">
      <Update_Academy_OrderStatus version="1.0">

         <ControlArea>
            <Verb value="Update"/>
            <Noun value="Academy_OrderStatus"/>
         </ControlArea>

         <DataArea>
            <OrderStatus>
            
               <OrderStatusHeader Versioning="FALSE" UpdateMode="P">
                  <OrderNumber type="ByWCS">
				  	<xsl:value-of select="Order/@OrderName" />
                  </OrderNumber>
               </OrderStatusHeader>

               <xsl:for-each select="Order/OrderLines/OrderLine">
               		<!--  message to WCS will have information of only those orderline that got acknowledged. -->
						<OrderStatusItem Versioning="FALSE">
							<ItemNumber type="ByWCS">
	                        	<xsl:value-of select="Extn/@ExtnWCOrderItemIdentifier" />
	                     	</ItemNumber>
	                     	
							<QuantityInfo>
                			<RequestedQuantity>
                   				<xsl:value-of select="@OriginalOrderedQty" />
                			</RequestedQuantity>

                			<ConfirmedQuantity>
								<xsl:value-of select="@OrderedQty" />
                			</ConfirmedQuantity>
                		
                			<AcknowledgedQuantity>
								<xsl:value-of select="OrderStatuses/OrderStatus/@StatusQty"/>
                			</AcknowledgedQuantity>
							</QuantityInfo>
							<Status StatusCondition="A" StatusFlag="I"></Status>
						</OrderStatusItem>
					
               </xsl:for-each>
               
            </OrderStatus>
         </DataArea>

      </Update_Academy_OrderStatus>
   </xsl:template>
</xsl:stylesheet>