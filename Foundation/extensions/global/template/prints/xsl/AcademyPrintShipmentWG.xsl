<?xml version = "1.0" encoding = "UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
	<xsl:template match="Print | Shipment">
	<PrintDocuments FlushToPrinter="Y" PrintName="AcademyPackLabelWG">
		<xsl:variable name="SCAC">
			<xsl:choose>
				<xsl:when test="name()=&quot;Print&quot;">
					<xsl:value-of select="Shipment/@SCAC"/>
				</xsl:when>
				<xsl:when test="name()=&quot;Shipment&quot;">
					<xsl:value-of select="@SCAC"/>
				</xsl:when>
			</xsl:choose>
		</xsl:variable>
			<PrintDocument>
				<xsl:attribute name="BeforeChildrenPrintDocumentId">
				<xsl:text>ACAD_PRINT_SHIPPING_LABEL_WG</xsl:text>
			</xsl:attribute>
			<xsl:attribute name="DataElementPath">
				<xsl:text>xml:/Shipment</xsl:text>
			</xsl:attribute>
			
			<xsl:choose>
				<xsl:when test="name()=&quot;Print&quot;">
					<xsl:copy-of select="PrinterPreference"/>
					<xsl:copy-of select="LabelPreference"/>
				</xsl:when>
				<xsl:when test="name()=&quot;Shipment&quot;">
					<PrinterPreference>
						<xsl:attribute name="WorkStationId">
						<xsl:value-of select="@WorkStationId"/>
						</xsl:attribute> 
						<xsl:attribute name="PrinterId">
						<xsl:value-of select="@PrinterId"/>
						</xsl:attribute> 
						<xsl:attribute name="OrganizationCode">
						<xsl:value-of select="@ShipNode"/></xsl:attribute>
					</PrinterPreference>
					<LabelPreference>
					</LabelPreference>
				</xsl:when>
			</xsl:choose>
			
 			<InputData>
								<Shipment>
							<xsl:copy-of select="@*" />
												<FromAddress>
							<xsl:copy-of select="FromAddress/@*" />
				</FromAddress>
									<ToAddress>
							<xsl:copy-of select="ToAddress/@*" />
				</ToAddress>
				</Shipment>
		         </InputData> 
				 
		    	</PrintDocument>
</PrintDocuments>
</xsl:template>
</xsl:stylesheet>
