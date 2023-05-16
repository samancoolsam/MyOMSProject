<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml"
		doctype-system='Update_Academy_InventoryMonitor_10.dtd' indent="yes" />

	<xsl:template match="/">
		<xsl:copy-of select="/"/>
	</xsl:template>

</xsl:stylesheet>
