<?xml version='1.0' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<OrderRelease>
			<xsl:copy-of select="OrderRelease/@*"/>
			<Order>
				<xsl:copy-of select="OrderRelease/Order/@*"/>
			</Order>
			<PersonInfoShipTo>
				<xsl:copy-of select="OrderRelease/PersonInfoShipTo/@*"/>
			</PersonInfoShipTo>
			<PackListPriceInfo>
				<xsl:copy-of select="OrderRelease/PackListPriceInfo/@*"/>
			</PackListPriceInfo>
			<OrderLines>
			<xsl:for-each select="OrderRelease/OrderLine">
				<xsl:copy>
    				 <xsl:copy-of  select="node()|@*"/>
  				</xsl:copy>
			</xsl:for-each>
			</OrderLines>
		</OrderRelease>
	</xsl:template>
</xsl:stylesheet>