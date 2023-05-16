<?xml version = "1.0" encoding = "UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
<xsl:output indent="yes"/>
	<xsl:template match="Container">
	
		<xsl:element name="OrderInvoice">
	<xsl:attribute name="InvoiceType">
								<xsl:value-of select="'PRO_FORMA'"/>
							</xsl:attribute>
								<xsl:attribute name="ShipmentKey">
								<xsl:value-of select="Shipment/@ShipmentKey"/>
							</xsl:attribute>
		</xsl:element>

</xsl:template>
</xsl:stylesheet>