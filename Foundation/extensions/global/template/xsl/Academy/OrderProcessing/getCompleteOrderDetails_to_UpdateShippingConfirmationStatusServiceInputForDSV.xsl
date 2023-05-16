<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output method="xml" doctype-system='Update_Academy_OrderStatus_10.dtd' indent="yes" />

   <xsl:template match="/">
      <Update_Academy_OrderStatus version="1.0">
         <ControlArea>
            <Verb value="Update" />
            <Noun value="Academy_OrderStatus" />
         </ControlArea>

         <DataArea>
            <OrderShipping>
               <OrderStatusHeader Versioning="FALSE" UpdateMode="P">
                  <OrderNumber type="ByWCS">
                     <xsl:value-of select="OrderInvoice/@OrderNo" />
                  </OrderNumber>
               </OrderStatusHeader>
			   
	   	<!-- OMNI-2458: Tracking Number update to WCS for DSV Orders - Start -->
		<xsl:variable name="scac">			
			<xsl:choose>
				<xsl:when test="contains(OrderInvoice/Shipment/@SCAC, 'USPS')">
					<xsl:value-of select="'USPS'" />
				</xsl:when>
				<xsl:when test="OrderInvoice/Shipment/@SCAC = 'SmartPost'">
					<xsl:value-of select="'FEDX'" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="OrderInvoice/Shipment/@SCAC" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>	
		<!-- OMNI-2458: Tracking Number update to WCS for DSV Orders - END -->

               <xsl:for-each select="OrderInvoice/Shipment/ShipmentLines/ShipmentLine">
	       <!-- Each message to WCS will have information of only those items that got shipped in the shipment -->
                  <OrderStatusItem Versioning="FALSE">
                     <xsl:variable name="orderLineKey" select="OrderLine/@OrderLineKey" />

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

                     <Status StatusCondition="S" StatusFlag="I">
                     </Status>

                     <ShippingInfo ShipCondition="SC" ShipModeFlag="I">
                        <ActualShipDate>
                           <xsl:variable name="shipmentDate" select="../../@ActualShipmentDate" />

			   <!-- changing date format from 2010-08-18T16:08:28-04:00 to format 2010-05-02T20:04:26.077 -->
                           <xsl:variable name="newShipmentDate" select="substring($shipmentDate,0,string-length($shipmentDate)-5)" />

                           <xsl:choose>
                              <xsl:when test="string-length($newShipmentDate)='19'">
                                 <xsl:value-of select="concat($newShipmentDate, '.000')" />
                              </xsl:when>

                              <xsl:otherwise>
                                 <xsl:value-of select="substring(concat($newShipmentDate, '000'), 1, 23)" />
                              </xsl:otherwise>
                           </xsl:choose>
                        </ActualShipDate>
                     </ShippingInfo>

		     <!-- Invoice No -->
                     <CustomerField>
                        <xsl:value-of select="../../../@InvoiceNo" />
                     </CustomerField>

		     <!-- OMNI-2458: Tracking Number update to WCS for DSV Orders - Start -->
		     <!--Enabiling the Tracking Number update to WCS for DSV orders ::@Radhakrishna Mediboina-03/03/2020 -->
                     <CustomerField>
			 <!-- Blocking Tracking Number update to WCS for DSV Phase I - @10/27/2012/Uday -->
			 <!--<xsl:variable name="shipmentLineKey" select="./@ShipmentLineKey" />           
			 <xsl:variable name="trackingNumber" select="../../Containers/Container/ContainerDetails/
			 ContainerDetail[@ShipmentLineKey=$shipmentLineKey]/../../@TrackingNo" />
			 <xsl:value-of select="$trackingNumber" />
			 <xsl:variable name="blockTrackingNumber" select="' '"/>
			 <xsl:value-of select="$blockTrackingNumber"/>-->
						 
			 <xsl:variable name="shipmentLineKey" select="./@ShipmentLineKey" />
			 <xsl:variable name="trackingNumber" select="../../Containers/Container/ContainerDetails/ContainerDetail[@ShipmentLineKey=$shipmentLineKey]/../../@TrackingNo" />							
						
			<xsl:if test="//Shipment/@ShipmentNo!='' and $scac='EFW'">
				<xsl:value-of select="concat($scac,'::', //Shipment/@ShipmentNo)" />
			</xsl:if>				
			<xsl:if test="$trackingNumber!='' and $scac!='EFW' and $scac!='LOCAL'">
				<xsl:value-of select="concat($scac,'::', $trackingNumber)" />
			</xsl:if>
						
                     </CustomerField>
		     <!-- OMNI-2458: Tracking Number update to WCS for DSV Orders - END -->
                  </OrderStatusItem>
               </xsl:for-each>
            </OrderShipping>
         </DataArea>
      </Update_Academy_OrderStatus>
   </xsl:template>
</xsl:stylesheet>

<!-- Stylus Studio meta-information - (c) 2004-2008. Progress Software Corporation. All rights reserved.

<metaInformation>
	<scenarios/>
	<MapperMetaTag>
		<MapperInfo srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/>
		<MapperBlockPosition></MapperBlockPosition>
		<TemplateContext></TemplateContext>
		<MapperFilter side="source"></MapperFilter>
	</MapperMetaTag>
</metaInformation>
-->