<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml"
		doctype-system='Update_Academy_OrderStatus_10.dtd' indent="yes" />
	<!-- This XSL is Especially for the Alternate Store Converted Line  -->
	<xsl:template match="/">
		<Update_Academy_OrderStatus version="1.0">
			<ControlArea>
				<Verb value="Update" />
				<Noun value="Academy_OrderStatus" />
			</ControlArea>
			<DataArea>
				<OrderShipping>
					<OrderStatusHeader Versioning="FALSE"
						UpdateMode="P">
						<OrderNumber type="ByWCS">
							<xsl:value-of select="//Order/@OrderNo" />
						</OrderNumber>
					</OrderStatusHeader>
					<OrderStatusItem Versioning="FALSE">
						<ItemNumber type="ByWCS">
							<xsl:value-of
								select="//OrderLine/Extn/@ExtnWCOrderItemIdentifier" />
						</ItemNumber>
						<QuantityInfo>
							<RequestedQuantity>
								<xsl:value-of
									select="//OrderLine/@OriginalOrderedQty" />
							</RequestedQuantity>
							<ConfirmedQuantity>
								<xsl:value-of select="//OrderLine/@OrderedQty" />
							</ConfirmedQuantity>
						</QuantityInfo>
						<Status StatusCondition="G" StatusFlag="G"></Status>
						<ShippingInfo ShipCondition="SC" ShipModeFlag="G">
							<PickedUpDate>
								<xsl:variable name="readyToPickDate"
									select="//OrderLine/Extn/@ExtnInitialPromiseDate" />
								<xsl:if test="$readyToPickDate !=''">
									<xsl:variable name="newreadyToPickDate"
										select="substring($readyToPickDate,0,string-length($readyToPickDate)-5)" />
									<xsl:choose>
										<xsl:when
											test="string-length($newreadyToPickDate)='19'">
											<xsl:value-of
												select="concat($newreadyToPickDate, '.000')" />
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of
												select="substring(concat($newreadyToPickDate, '000'), 1, 23)" />
										</xsl:otherwise>
									</xsl:choose>
								</xsl:if>
							</PickedUpDate>
						</ShippingInfo>
						<AlternatePickupStore>
							<ShipNode>
								<xsl:value-of select="//OrderLine/@ShipNode" />
							</ShipNode>
						</AlternatePickupStore>
						<CustomerField></CustomerField>
						<CustomerField></CustomerField>
					</OrderStatusItem>
				</OrderShipping>
			</DataArea>
		</Update_Academy_OrderStatus>
	</xsl:template>
</xsl:stylesheet>