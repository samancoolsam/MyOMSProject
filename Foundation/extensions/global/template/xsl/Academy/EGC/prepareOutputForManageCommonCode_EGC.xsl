<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="/CommonCode">
		<xsl:element name="EGC">
			<xsl:if test="(@CodeShortDescription)='Y'">
				<xsl:attribute name="EGC">
					<xsl:text>ON</xsl:text>
				</xsl:attribute>
				<xsl:attribute name="Response">
					<xsl:text>Success</xsl:text>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="(@CodeShortDescription)='N'">
				<xsl:attribute name="EGC">
					<xsl:text>OFF</xsl:text>
				</xsl:attribute>
				<xsl:attribute name="Response">
					<xsl:text>Success</xsl:text>
				</xsl:attribute>
			</xsl:if>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>