<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output indent="yes"/>
	<xsl:template match="Print">
		<!-- TODO: Auto-generated template -->
		<Shipment>
		<xsl:attribute name="ShipmentKey">
		<xsl:value-of select = "Shipment/@ShipmentKey" />
		</xsl:attribute>
		</Shipment>
	</xsl:template>
</xsl:stylesheet>