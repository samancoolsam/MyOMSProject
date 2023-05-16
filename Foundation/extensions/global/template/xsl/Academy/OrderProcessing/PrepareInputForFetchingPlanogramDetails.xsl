<?xml version='1.0' encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
	
	<Shipment>
			<xsl:attribute name="ShipNode">
				<xsl:value-of select="Shipment/@ShipNode"/>
			</xsl:attribute>
				<xsl:attribute name="ShipmentKey">
				<xsl:value-of select="Shipment/@ShipmentKey"/>
			</xsl:attribute>
			
			<ShipmentLines>
			<xsl:for-each select="Shipment/ShipmentLines/ShipmentLine">
				<ShipmentLine>
					<xsl:attribute name="ShipmentLineNo">
						<xsl:value-of select="@ShipmentLineNo"/>
					</xsl:attribute>
						<xsl:attribute name="ItemID">
						<xsl:value-of select="@ItemID"/>
					</xsl:attribute>
				</ShipmentLine>
			</xsl:for-each>
            </ShipmentLines>			
		</Shipment>
		
	</xsl:template>
</xsl:stylesheet>

