<?xml version = "1.0" encoding = "UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
	<xsl:template match="Print | Container">
	<PrintDocuments FlushToPrinter="Y" PrintName="ShipmentBOL">
		<xsl:variable name="SCAC">
			<xsl:choose>
				<xsl:when test="name()=&quot;Print&quot;">
					<xsl:value-of select="Container/@SCAC"/>
				</xsl:when>
				<xsl:when test="name()=&quot;Container&quot;">
					<xsl:value-of select="@SCAC"/>
				</xsl:when>
			</xsl:choose>
		</xsl:variable>
			<PrintDocument>
				<xsl:attribute name="BeforeChildrenPrintDocumentId">
				<xsl:text>ACAD_VICS_BOL</xsl:text>
			</xsl:attribute>
			<xsl:attribute name="BeforeChildrenLabelFormatId">
				<xsl:text>xml:/Shipment/LabelFormat/@LabelFormatId</xsl:text>
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
						<xsl:attribute name="UserId"><xsl:value-of select="ContainerActivities/ContainerActivity/@Modifyuserid"/></xsl:attribute>
						<xsl:attribute name="PrinterId">
						<xsl:text>xml:/AcademyMergedDocument/EnvironmentDocument/Container/@PrinterId</xsl:text>
						</xsl:attribute>
						<xsl:attribute name="WorkStationId">
						<xsl:text>xml:/Container/@ContainerEquipment</xsl:text>
						</xsl:attribute>
						<xsl:attribute name="OrganizationCode">
						<xsl:text>005</xsl:text></xsl:attribute>
						<!--<xsl:choose>
							<xsl:when test="PackLocation/@StationId and not (PackLocation/@StationId = &quot;&quot;)">
								<xsl:attribute name="WorkStationId"><xsl:value-of select="PackLocation/@StationId"/></xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="WorkStationId"><xsl:text>xml:/Container/@ContainerEquipment</xsl:text></xsl:attribute>
							</xsl:otherwise>
						</xsl:choose>
						<xsl:attribute name="OrganizationCode"><xsl:text>005</xsl:text></xsl:attribute> !-->
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
					<xsl:text>AcademyGetDataForWhiteGloveBOL</xsl:text>
				</xsl:attribute>
				<Container>
					<xsl:choose>
						<xsl:when test="name()=&quot;Print&quot;">
							<xsl:copy-of select="Container/@*"/>
						</xsl:when>
						<xsl:when test="name()=&quot;Container&quot;">
							<xsl:copy-of select="@*" /> 
						</xsl:when>
					</xsl:choose>	
				</Container>
		         </InputData> 
		    	</PrintDocument>
</PrintDocuments>
</xsl:template>
</xsl:stylesheet>
