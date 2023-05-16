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
				  	<xsl:value-of select="OrderRelease/@OrderName" />
                  </OrderNumber>
               </OrderStatusHeader>
               <xsl:for-each select="OrderRelease/OrderLine">
               		<!--  message to WCS will have information of only those orderline that got PO Released. -->
						<OrderStatusItem Versioning="FALSE">
							<ItemNumber type="ByWCS">
	                        	<xsl:value-of select="ChainedFromOrderLine/Extn/@ExtnWCOrderItemIdentifier" />
	                     	</ItemNumber>
							<QuantityInfo>
                			<RequestedQuantity>
                   				<xsl:value-of select="number(@OrderedQty)" />
                			</RequestedQuantity>
							</QuantityInfo>
							<Status StatusCondition="POR" StatusFlag="I"/> <!-- Status condition value to be confirmed by WCS to denote PO Released  -->
							<xsl:variable name="orderNo" select="/OrderRelease/Order/@OrderNo"/>

							<CustomerField> <!-- This is to contain the PO (always starts with Y) no which can be used to send back to OMS in future -->
							<xsl:choose>
								<xsl:when test="/OrderRelease/Shipnode/Organization/@XrefOrganizationCode='CHUB'">
									<xsl:value-of select="concat('CHUB:', $orderNo)" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="concat('RADIAL:', $orderNo)" />
								</xsl:otherwise>
							</xsl:choose>
							</CustomerField> 
							<CustomerField>  <!-- Contains the Primeline no for each PO which is used for PO cancel to CHUB  -->
								<xsl:value-of select="@PrimeLineNo" />
							</CustomerField> 
						</OrderStatusItem>
               </xsl:for-each>
            </OrderStatus>
         </DataArea>
      </Update_Academy_OrderStatus>
   </xsl:template>
</xsl:stylesheet>