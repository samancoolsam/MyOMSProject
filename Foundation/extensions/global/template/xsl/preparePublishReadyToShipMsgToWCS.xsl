<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!--OMNI-81782 Begin-->
<xsl:output method="xml" indent="no" doctype-system="Update_Academy_STSShipment_10.dtd"/>
<!--OMNI-81782 End-->
<xsl:template match="/">
<!--OMNI-81782 Begin-->
<Update_Academy_STSShipment version="1.0">
<DataArea>
<!--OMNI-81782 End-->
		<Shipment>
			<!--OMNI-81782 Begin-->
			<xsl:attribute name="ExtnWCOrderIdentifier">
			<xsl:value-of select="Shipment/Containers/Container/ContainerDetails/ContainerDetail/OrderLine/ChainedFromOrderLine/Order/@OrderNo"/>
			</xsl:attribute>
			<xsl:attribute name="OrderNo"> <xsl:value-of select="Shipment/@OrderNo"/> </xsl:attribute>
			<!--OMNI-81782 End-->
			<xsl:attribute name="ShipmentKey"> <xsl:value-of select="Shipment/@ShipmentKey"/> </xsl:attribute>
			<xsl:attribute name="ShipmentNo"><xsl:value-of select="Shipment/@ShipmentNo"/></xsl:attribute>
			<Containers>
				<xsl:for-each select="Shipment/Containers/Container">
					<Container>	
						<xsl:attribute name="ContainerNo"><xsl:value-of select="@ContainerNo"/></xsl:attribute>
						<xsl:attribute name="ShipmentKey"><xsl:value-of select="@ShipmentKey"/></xsl:attribute>
						<ContainerDetails>
							<ContainerDetail>
								<xsl:variable name="ShipmentLineKey">
									<xsl:value-of select="ContainerDetails/ContainerDetail/ShipmentLine/@ShipmentLineKey" />
								</xsl:variable>
								<xsl:variable name="OrderLineKey">
									<xsl:value-of select="ContainerDetails/ContainerDetail/OrderLine/@OrderLineKey" />
								</xsl:variable>
								<xsl:attribute name="ItemID"><xsl:value-of select="ContainerDetails/ContainerDetail/@ItemID"/></xsl:attribute>
								<xsl:attribute name="ProductClass"><xsl:value-of select="ContainerDetails/ContainerDetail/@ProductClass"/></xsl:attribute>
								<xsl:attribute name="Quantity"><xsl:value-of select="ContainerDetails/ContainerDetail/@Quantity"/></xsl:attribute>
								<xsl:attribute name="ShipmentKey"><xsl:value-of select="ContainerDetails/ContainerDetail/@ShipmentKey"/></xsl:attribute>
								<xsl:attribute name="ShipmentLineKey"><xsl:value-of select="ContainerDetails/ContainerDetail/@ShipmentLineKey"/></xsl:attribute>
								<xsl:attribute name="UnitOfMeasure"><xsl:value-of select="ContainerDetails/ContainerDetail/@UnitOfMeasure"/></xsl:attribute>
								<ShipmentLine>
									<xsl:attribute name="OrderLineKey"><xsl:value-of select="ContainerDetails/ContainerDetail/ShipmentLine/@OrderLineKey"/></xsl:attribute>
									<xsl:attribute name="OrderReleaseKey"><xsl:value-of select="ContainerDetails/ContainerDetail/ShipmentLine/@OrderReleaseKey"/></xsl:attribute>
								</ShipmentLine>
								<xsl:choose>
								<xsl:when test = "$OrderLineKey=''">
									<xsl:copy-of select="/Shipment/ShipmentLines/ShipmentLine[@ShipmentLineKey=$ShipmentLineKey]/OrderLine"/>
								</xsl:when>
				  				<xsl:otherwise>
				  				<xsl:copy-of select="ContainerDetails/ContainerDetail/OrderLine"  />
				  				</xsl:otherwise>
								</xsl:choose>
							</ContainerDetail>
						</ContainerDetails>
					</Container>
				</xsl:for-each>
			</Containers>
		</Shipment>
	<!--OMNI-81782 Begin-->
	</DataArea>
</Update_Academy_STSShipment>
<!--OMNI-81782 End-->
	</xsl:template>
</xsl:stylesheet>