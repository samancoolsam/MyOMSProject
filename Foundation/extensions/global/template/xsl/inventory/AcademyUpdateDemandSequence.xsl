<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<Items>
			<xsl:copy-of select="Items/@*" />
			<xsl:for-each select="Items/Item">
				<Item>
					<xsl:copy-of select="@*" />
					<Demands>
						<xsl:for-each
							select="Demands/Demand[@DemandType!='ALLOCATED.ex']">
							<xsl:element name="Demand">
								<xsl:copy-of select="@*" />
							</xsl:element>
						</xsl:for-each>
						<xsl:for-each
							select="Demands/Demand[@DemandType='ALLOCATED.ex']">
							<xsl:element name="Demand">
								<xsl:copy-of select="@*" />
							</xsl:element>
						</xsl:for-each>
					</Demands>
				</Item>
			</xsl:for-each>
		</Items>
	</xsl:template>
</xsl:stylesheet>