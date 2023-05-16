<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:template match="/OrderRelease">
		<xsl:element name="TaskQueue">
			<xsl:attribute name="DataType">
				<xsl:value-of select="'OrderReleaseKey'" />
			</xsl:attribute>

			<xsl:attribute name="DataKey">
				<xsl:value-of select="@OrderReleaseKey" />
			</xsl:attribute>

			<xsl:attribute name="Operation">
				<xsl:value-of select="'Delete'" />
			</xsl:attribute>

			<xsl:variable name="DocumentType" select="@DocumentType" />            

			<xsl:choose>
				<xsl:when test="$DocumentType = '0001'">
					<xsl:attribute name="TransactionId">
						<xsl:value-of select="'CONSOLIDATE_TO_SHIPMENT'" />
					</xsl:attribute>
				</xsl:when>

				<xsl:otherwise>
					<xsl:attribute name="TransactionId">
						<xsl:value-of select="'CONSOLIDATE_TO_SHIPMENT.0006'" />
					</xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>