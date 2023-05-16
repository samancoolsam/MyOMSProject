<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
	<xsl:template match="/">
		<xsl:element name="Shipment">
						<xsl:attribute name="TrackingNo">
							<xsl:value-of select="TrackingNumbers/TrackingNumber/@TrackingNo"/>
						</xsl:attribute>
			 </xsl:element>
	</xsl:template>
</xsl:stylesheet>