<?xml version = "1.0" encoding = "UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
<xsl:output indent="yes"/>
	<xsl:template match="/">
	<PrintDocuments>
		 <xsl:attribute name="FlushToPrinter">
				<xsl:text>Y</xsl:text>
	 </xsl:attribute>
	 <xsl:attribute name="PrintName">
				<xsl:text>AcademyPrintSeperatorPage</xsl:text>
	 </xsl:attribute>
	 <PrintDocument>
	 <xsl:attribute name="BeforeChildrenPrintDocumentId">
				<xsl:text>ACAD_PRINT_SEPERATOR_PAGE</xsl:text>
			</xsl:attribute>
			<xsl:attribute name="DataElementPath">
				<xsl:text>xml:/Batch</xsl:text>
			</xsl:attribute>
			<InputData>
					<Batch>
					<xsl:attribute name="PrintSeperatorPage">
							<xsl:text>Print</xsl:text>
						</xsl:attribute>
					</Batch>
			</InputData>
					<PrinterPreference>
						<xsl:attribute name="OrganizationCode"><xsl:text>xml:/Batch/@Node</xsl:text></xsl:attribute>
						<xsl:attribute name="PrinterId"><xsl:text>xml:/Batch/@PrinterId</xsl:text></xsl:attribute>
					</PrinterPreference>
			 </PrintDocument>
	</PrintDocuments>
	</xsl:template>
</xsl:stylesheet>
