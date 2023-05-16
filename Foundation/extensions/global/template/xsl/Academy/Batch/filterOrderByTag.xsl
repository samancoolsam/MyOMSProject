<?xml version='1.0' ?>
<!-- Adding this as a part of BOPIS-1244-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">	
		<StoreBatch>
			<xsl:attribute name="AssignedToUserId">
				<xsl:value-of select="StoreBatch/@AssignedToUserId" />
			</xsl:attribute>
			<xsl:attribute name="DisplayLocalizedFieldInLocale">
				<xsl:value-of select="StoreBatch/@DisplayLocalizedFieldInLocale" />
			</xsl:attribute>
			<xsl:attribute name="GetBatchesOfAllUsers">
				<xsl:value-of select="StoreBatch/@GetBatchesOfAllUsers" />
			</xsl:attribute>
			<xsl:attribute name="GetExistingBatches">
				<xsl:value-of select="StoreBatch/@GetExistingBatches" />
			</xsl:attribute>
			<xsl:attribute name="GetNewBatches">
				<xsl:value-of select="StoreBatch/@GetNewBatches" />
			</xsl:attribute>
			<xsl:attribute name="OrganizationCode">
				<xsl:value-of select="StoreBatch/@OrganizationCode" />
			</xsl:attribute>
				<xsl:attribute name="SkipValidations">
				<xsl:value-of select="StoreBatch/@SkipValidations" />
			</xsl:attribute>
			
			<xsl:copy-of select="StoreBatch/ShipmentIndexLookup" />
			<xsl:copy-of select="StoreBatch/ShipmentLine" />
			<xsl:copy-of select="StoreBatch/ComplexQuery" />
			<xsl:copy-of select="StoreBatch/ConfigureBatchBy" />
			
		</StoreBatch>
	</xsl:template>
</xsl:stylesheet>