<?xml version = "1.0" encoding = "UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
<xsl:output indent="yes"/>
	<xsl:template match="/">
	<PrintDocuments>
		 <xsl:attribute name="FlushToPrinter">
				<xsl:text>Y</xsl:text>
	 </xsl:attribute>
	 <xsl:attribute name="PrintName">
				<xsl:text>AcademyReturnLabelConveyable</xsl:text>
	 </xsl:attribute>
	 <PrintDocument>
	 <xsl:attribute name="BeforeChildrenPrintDocumentId">
				<xsl:text>ACAD_RETURN_LABEL_CONV</xsl:text>
			</xsl:attribute>
			
			<xsl:attribute name="DataElementPath">
				<xsl:text>xml:/Container</xsl:text>
			</xsl:attribute>
			<InputData>
					<xsl:copy-of select="Container"/>
			</InputData>
					<PrinterPreference>
						<xsl:attribute name="WorkStationId"><xsl:text>xml:/Container/PackLocation/@StationId</xsl:text></xsl:attribute>
						<xsl:attribute name="OrganizationCode"><xsl:text>005</xsl:text></xsl:attribute>
						<xsl:attribute name="PrinterId"><xsl:text>xml:/Container/@PrinterId</xsl:text></xsl:attribute>
					</PrinterPreference>
							<LabelPreference>
					
					</LabelPreference>
	 </PrintDocument>
	</PrintDocuments>
	</xsl:template>
</xsl:stylesheet>
