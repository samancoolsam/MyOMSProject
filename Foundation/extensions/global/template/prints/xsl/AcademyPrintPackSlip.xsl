<?xml version = "1.0" encoding = "UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
<xsl:output indent="yes"/>
	<xsl:template match="Print | Container">
		<PrintDocuments>
			<xsl:attribute name="FlushToPrinter">
				<xsl:text>Y</xsl:text>
			</xsl:attribute>
			<xsl:attribute name="PrintName">
				<xsl:text>AcademyPackSlipConv</xsl:text>
			</xsl:attribute>
			<PrintDocument>
			<xsl:attribute name="BeforeChildrenPrintDocumentId">
				<xsl:text>xml:/AcademyMergedDocument/EnvironmentDocument/Container/@PrintDocumentId</xsl:text>
			</xsl:attribute>
			<xsl:attribute name="BeforeChildrenLabelFormatId">
				<xsl:text>xml:/AcademyMergedDocument/EnvironmentDocument/Container/@LabelFormatId</xsl:text>
			</xsl:attribute>
			<xsl:attribute name="DataElementPath">
				<xsl:text>xml:/Container</xsl:text>
			</xsl:attribute>
			<xsl:choose>
				<xsl:when test="name()=&quot;Print&quot;">
					<xsl:copy-of select="PrinterPreference"/>
					<xsl:copy-of select="LabelPreference"/>
				</xsl:when>
				<xsl:when test="name()=&quot;Container&quot;">
					<PrinterPreference>
						<xsl:attribute name="UsergroupId"/>
						<!--Start changes as part of STL-1466-->
						<xsl:attribute name="UserId"><xsl:value-of select="./@Createuserid"/></xsl:attribute>
						<!--End changes as part of STL-1466-->
						<xsl:attribute name="PrinterId">
						<xsl:text>xml:/AcademyMergedDocument/EnvironmentDocument/Container/@PrinterId</xsl:text>
						</xsl:attribute>
						<xsl:choose>
							<xsl:when test="PackLocation/@StationId and not (PackLocation/@StationId = &quot;&quot;)">
								<xsl:attribute name="WorkStationId"><xsl:value-of select="PackLocation/@StationId"/></xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="WorkStationId"><xsl:text>xml:/Container/@ContainerEquipment</xsl:text></xsl:attribute>
							</xsl:otherwise>
						</xsl:choose>
						<xsl:attribute name="OrganizationCode"><xsl:text>005</xsl:text></xsl:attribute>
					</PrinterPreference>
					<LabelPreference>
						<xsl:attribute name="BuyerOrganizationCode">
							<xsl:text>xml:/Container/Shipment/@BuyerOrganizationCode</xsl:text>
						</xsl:attribute>
						<xsl:if test="PackLocation/@StationType and not (PackLocation/@StationType = &quot;&quot;)">
							<xsl:attribute name="EquipmentType">
								<xsl:value-of select="PackLocation/@StationType"/>
							</xsl:attribute>
						</xsl:if>
						<xsl:attribute name="Node">
							<xsl:value-of select="PackLocation/@Node"/>
						</xsl:attribute>
					</LabelPreference>
				</xsl:when>
			</xsl:choose>
			<KeyAttributes>
				<KeyAttribute>
					<xsl:attribute name="Name"><xsl:text>ShipmentContainerKey</xsl:text></xsl:attribute>
				</KeyAttribute>	
			</KeyAttributes>
				<InputData>
					<xsl:attribute name="FlowName">
						<xsl:text>AcademyGetDataForPackSlip</xsl:text>
					</xsl:attribute>			
						<xsl:choose>
							<xsl:when test="name()=&quot;Print&quot;">
								<Container>
								<xsl:copy-of select="Container/@*" /> 
								</Container>
							</xsl:when>
							<xsl:when test="name()=&quot;Container&quot;">
								<xsl:copy-of select="." /> 
							</xsl:when>
						</xsl:choose>
			</InputData>
	</PrintDocument>
</PrintDocuments>
</xsl:template>
</xsl:stylesheet>