<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" doctype-system='Update_Academy_OrderStatus_10.dtd' indent="yes" />   
<!--    <xsl:output indent="yes" />  -->

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
				  	<xsl:value-of select="Receipt/ReceiptLines/ReceiptLine/OrderLine/DerivedFromOrder/@OrderNo" />
                  </OrderNumber>
               </OrderStatusHeader>

				<!--  message only for the items that got received -->
              <xsl:for-each select="Receipt/ReceiptLines/ReceiptLine">
							<OrderStatusItem Versioning="FALSE">
								<ItemNumber type="ByWCS">
		                        	<xsl:value-of select="OrderLine/DerivedFromOrderLine/Extn/@ExtnWCOrderItemIdentifier" />
		                     	</ItemNumber>
	
								<QuantityInfo>
			                    	<ActualReturnedQty>
										<xsl:value-of select="@Quantity" />
			                        </ActualReturnedQty>
								</QuantityInfo>
								
								<Status StatusCondition="r" StatusFlag="I"></Status>
								
								<RMAOrder>
									<xsl:variable name="orderNo" select="@OrderNo" />
									<xsl:choose>
				                     	<xsl:when test="starts-with($orderNo,'Y')">
				                     	</xsl:when>
				                     	<xsl:otherwise>
											<xsl:value-of select="$orderNo"/>
						                </xsl:otherwise>
									</xsl:choose>
								</RMAOrder>
							</OrderStatusItem>
				</xsl:for-each>
            </OrderStatus>
         </DataArea>
      </Update_Academy_OrderStatus>
   </xsl:template>
</xsl:stylesheet>
