<?xml version='1.0' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<MultiApi>
		
			<xsl:element name="API">
				<xsl:attribute name="Name">
					<xsl:text>changeShipment</xsl:text>
				</xsl:attribute>
				<xsl:element name="Input">
					<xsl:element name="Shipment">
						<xsl:attribute name="ShipmentKey">
							<xsl:value-of select="Shipment/@ShipmentKey"/>
						</xsl:attribute>
						<xsl:attribute name="BackroomPickRequired">
							<xsl:text>Y</xsl:text>
						</xsl:attribute>
					</xsl:element>
				</xsl:element>
				<Template>
					<Shipment ShipmentKey="" ShipmentNo="" ShipNode="" SellerOrganizationCode="" BackroomPickRequired=""/>
				</Template>
			</xsl:element>
			
			<xsl:element name="API">
				<xsl:attribute name="Name">
					<xsl:text>changeShipmentStatus</xsl:text>
				</xsl:attribute>
				<xsl:element name="Input">
					<xsl:element name="Shipment">
						<xsl:attribute name="ShipmentKey">
							<xsl:value-of select="Shipment/@ShipmentKey"/>
						</xsl:attribute>
						<xsl:attribute name="BaseDropStatus">
							<xsl:text>1100.70.06.10</xsl:text>
						</xsl:attribute>
						<xsl:attribute name="TransactionId">
							<xsl:text>YCD_CHECK_FOR_BACKROOM_PICK</xsl:text>
						</xsl:attribute>
					</xsl:element>
				</xsl:element>
			</xsl:element>
			
		</MultiApi>
	</xsl:template>
</xsl:stylesheet>