<?xml version='1.0' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<Order>
			<xsl:copy-of select="Order/@*"/>
			<xsl:copy-of select="Payment/@*"/>
			<xsl:attribute name="DocumentType"><xsl:text>0001</xsl:text></xsl:attribute>
			<xsl:attribute name="EnterpriseCode"><xsl:text>Academy_Direct</xsl:text></xsl:attribute>
			<xsl:attribute name="SellerOrganizationCode"><xsl:text>Academy_Direct</xsl:text></xsl:attribute>
				     </Order>
	</xsl:template>
</xsl:stylesheet>