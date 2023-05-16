<!-- Added this xsl as part of OMNI-42008 - OMS Ability to view/print BOPIS/STS Pick-up Slip in My Account.
    To send invoice number in shipment updates to wcs for BOPIS,STS, STS1.1 orders. Added invoice as part of <CustomerField> element -->

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
				  	<xsl:value-of select="OrderInvoice/@OrderNo" />
                  </OrderNumber>
               </OrderStatusHeader>
		
		<xsl:variable name="deliveryMethod" select="OrderInvoice/Shipment/@DeliveryMethod" />
		
		<xsl:variable name="invoiceNo" select="OrderInvoice/@InvoiceNo" />
	
		<xsl:variable name="actualShipmentDate" />
		
	
	

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
		
						
						
               <xsl:for-each select="OrderInvoice/Shipment/ShipmentLines/ShipmentLine">
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
						
						<!--Status -->
							<xsl:choose>
								<xsl:when test="$deliveryMethod='PICK'">
										<Status StatusCondition="V" StatusFlag="I"></Status>						
								</xsl:when>
								<xsl:otherwise>
										<Status StatusCondition="S" StatusFlag="I"></Status>						
								</xsl:otherwise>
						</xsl:choose>
             			
             
						
								<!-- changing date format from 2010-08-18T16:08:28-04:00 to format 2010-05-02T20:04:26.077 -->

								<!--<xsl:variable name="newShipmentDate" select="substring($shipmentDate,0,string-length($shipmentDate)-5)"/>
								<xsl:choose>
									<xsl:when test="string-length($newShipmentDate)='19'">
										<xsl:value-of select="concat($newShipmentDate, '.000')" />
						     		</xsl:when>
							     	<xsl:otherwise>
										<xsl:value-of select="substring(concat($newShipmentDate, '000'), 1, 23)" />
							     	</xsl:otherwise>
								</xsl:choose> -->
								
						<ShippingInfo ShipCondition="SC" ShipModeFlag="I">
							<ActualShipDate>
								<xsl:value-of select="$actualShipmentDate"/>
							</ActualShipDate>
					
							<PickedUpDate>
								<xsl:for-each select="../../ShipmentStatusAudits/ShipmentStatusAudit[@NewStatus='1400']">
									<xsl:variable name="pickupDate" select="./@NewStatusDate"/>
									<xsl:if test="$pickupDate !=''">
										<xsl:variable name="newpickupDate" select="substring($pickupDate,0,string-length($pickupDate)-5)"/>
											<xsl:choose>
												<xsl:when test="string-length($newpickupDate)='19'">
													<xsl:value-of select="concat($newpickupDate, '.000')" />
												</xsl:when>
												<xsl:otherwise>
													<xsl:value-of select="substring(concat($newpickupDate, '000'), 1, 23)" />
												</xsl:otherwise>
										</xsl:choose>
									</xsl:if>	
									
								</xsl:for-each>	
								
							</PickedUpDate>  
							</ShippingInfo> 
							
<!-- Invoice No -->

						<CustomerField>
							<xsl:value-of select="$invoiceNo"/>
						</CustomerField>

<!--
						<CustomerField1>
	                    	<OrderLineKey>
	                    		<xsl:value-of select="OrderLine/@OrderLineKey" />
	                    	</OrderLineKey>
	                    	<OrderHeaderKey>
	                    		<xsl:value-of select="OrderLine/@OrderHeaderKey" />
	                    	</OrderHeaderKey>
	                    </CustomerField1>
-->


					
<!-- Tracking Number -->
						<CustomerField>
							<xsl:variable name="shipmentLineKey" select="./@ShipmentLineKey"/>
							<!-- Start STL-1547 -->
							<!-- 
							<xsl:for-each select="../../Containers/Container">
								<xsl:choose>
									<xsl:when test="../../ShipmentLines/ShipmentLine[@OrderLineKey=$orderLineKey]/Item/Extn/@ExtnWhiteGloveEligible='Y'">
								 	 	<xsl:value-of select="../../@ProNo" />
								 	</xsl:when>
								 	<xsl:otherwise>
										<xsl:variable name="trackingNumber" select="ContainerDetails/ContainerDetail[@ShipmentLineKey=$shipmentLineKey]/../../@TrackingNo"/>
								 	 	<xsl:value-of select="$trackingNumber" />
								 	</xsl:otherwise>
								</xsl:choose>
							-->
								
								
							<xsl:for-each select="../../Containers/Container[ContainerDetails/ContainerDetail/@ShipmentLineKey=$shipmentLineKey]">
								<xsl:variable name="trackingNumber" select="./@TrackingNo"/>
								
								<xsl:if test="//Shipment/@ShipmentNo!='' and $scac='EFW'">
									<xsl:value-of select="concat($scac,'::', //Shipment/@ShipmentNo)" />
								</xsl:if>
								
								<xsl:if test="$trackingNumber!='' and $scac!='EFW'">
											<xsl:value-of select="concat($scac,'::', $trackingNumber)" />
								</xsl:if>
								

								<!-- End STL-1547 -->
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

					</OrderStatusItem>
				</xsl:if>
				</xsl:for-each>

            </OrderShipping>
         </DataArea>

      </Update_Academy_OrderStatus>
   </xsl:template>
</xsl:stylesheet>