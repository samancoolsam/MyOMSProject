<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" />
<xsl:template match="Payment">
 <Payment>
	<xsl:attribute name="ResponseCode">
        <xsl:value-of select="'APPROVED'" />
    </xsl:attribute>
	<xsl:attribute name="AuthorizationId">
        <xsl:value-of select="@AuthorizationId" />
    </xsl:attribute>
	<xsl:attribute name="AuthorizationAmount">
        <xsl:value-of select="@RequestAmount" />
    </xsl:attribute>
    <xsl:attribute name="RequestID">
        <xsl:value-of select="@AuthorizationId" />
    </xsl:attribute>
    <xsl:attribute name="TranType">
        <xsl:value-of select="'CHARGE'" />
    </xsl:attribute>
    <xsl:attribute name="TranReturnCode">
        <xsl:value-of select="'100'" />
    </xsl:attribute>
	<xsl:attribute name="TranAmount">
        <xsl:value-of select="@RequestAmount" />
    </xsl:attribute>
 </Payment>
</xsl:template>
</xsl:stylesheet>