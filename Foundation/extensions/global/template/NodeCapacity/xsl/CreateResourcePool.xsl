<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<!-- identity template -->
	<xsl:template match="node() | @*">
		<xsl:copy>
			<xsl:apply-templates select="node() | @*" />
		</xsl:copy>
	</xsl:template>

	<!-- template for the document element -->
	<xsl:template match="/*">
		<xsl:apply-templates select="node()" />
	</xsl:template>
</xsl:stylesheet>