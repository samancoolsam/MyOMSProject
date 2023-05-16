<?xml version='1.0' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes"/>
	<xsl:template match="/">
        <StoreBatch>
			<xsl:attribute name="BatchType">
				<xsl:value-of select="StoreBatch/@BatchType"/>
			</xsl:attribute>
			<xsl:attribute name="OrganizationCode">
				<xsl:value-of select="StoreBatch/@OrganizationCode"/>
			</xsl:attribute>
            <xsl:attribute name="Status">
				<xsl:value-of select="StoreBatch/@Status"/>
			</xsl:attribute>
            <xsl:attribute name="StoreBatchKey">
				<xsl:value-of select="StoreBatch/@StoreBatchKey"/>
			</xsl:attribute>
            <xsl:attribute name="StoreBatchNo">
				<xsl:value-of select="StoreBatch/@BatchNo"/>
			</xsl:attribute>			
		</StoreBatch>
		
	</xsl:template>
</xsl:stylesheet>

