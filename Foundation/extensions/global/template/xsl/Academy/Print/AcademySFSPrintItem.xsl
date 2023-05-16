<?xml version='1.0'?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/Page">
		<PrintDocuments>
			<xsl:attribute name="FlushToPrinter">Y</xsl:attribute>
			<xsl:attribute name="PrintName">AcademyPrintItemPickTickets</xsl:attribute>
			<PrintDocument>
				<xsl:attribute name="BeforeChildrenPrintDocumentId">
					<xsl:value-of select="@BeforeChildrenPrintDocumentId"/>
				</xsl:attribute>
				<xsl:attribute name="DataElementPath">xml:/Page</xsl:attribute>
				<InputData>
					<xsl:copy-of select="/"/>
				</InputData>
				<PrinterPreference>
					<xsl:attribute name="PrinterId">
						<xsl:value-of select="@PrinterId"/>
					</xsl:attribute>
					<xsl:attribute name="OrganizationCode">
						<xsl:value-of select="@ShipNode"/>
					</xsl:attribute>
				</PrinterPreference>
			</PrintDocument>
		</PrintDocuments>
	</xsl:template>
</xsl:stylesheet>