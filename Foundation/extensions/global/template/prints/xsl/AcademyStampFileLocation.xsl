<?xml version = "1.0" encoding = "UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
<xsl:output indent="yes"/>
	<xsl:template match="Container">
	
		<xsl:element name="Container">
	<xsl:attribute name="WaveKey">
								<xsl:value-of select="Container/@WaveKey"/>
							</xsl:attribute>
		</xsl:element>

</xsl:template>
</xsl:stylesheet>