<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="*">
        <xsl:element name="CommonCode">
	        <xsl:attribute name="Action">
	            <xsl:text>MODIFY</xsl:text>
	        </xsl:attribute>
            <xsl:attribute name="OrganizationCode">
				<xsl:text>Academy_Direct</xsl:text>
	        </xsl:attribute>
			<xsl:attribute name="CodeType">
	            <xsl:text>EGC_ENABLED</xsl:text>
	        </xsl:attribute>
			<xsl:attribute name="CodeValue">
	            <xsl:text>EGC_FLAG</xsl:text>
	        </xsl:attribute>
			<xsl:attribute name="CodeShortDescription">
				<xsl:if test="(@EGC)='ON'">
					<xsl:text>Y</xsl:text>
				</xsl:if>
				<xsl:if test="(@EGC)='OFF'">
					<xsl:text>N</xsl:text>
				</xsl:if>
	        </xsl:attribute>
			<xsl:attribute name="CodeLongDescription">
				<xsl:if test="(@EGC)='ON'">
					<xsl:text>Y</xsl:text>
				</xsl:if>
				<xsl:if test="(@EGC)='OFF'">
					<xsl:text>N</xsl:text>
				</xsl:if>
	        </xsl:attribute>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>