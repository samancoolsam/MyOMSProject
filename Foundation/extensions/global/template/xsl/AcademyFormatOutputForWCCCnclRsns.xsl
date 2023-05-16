<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/OrderAuditList">
	<Order>
	<OrderLines>
	<xsl:for-each select="./OrderAudit/OrderAuditLevels/OrderAuditLevel/ModificationTypes/ModificationType[@Name='CANCEL']">
	<xsl:if test="../../OrderLine/@PrimeLineNo!=''">
	<OrderLine>
	<xsl:attribute name="PrimeLineNo">
	<xsl:value-of select="../../OrderLine/@PrimeLineNo"/>
	</xsl:attribute>
	<xsl:attribute name="CancelReason">
	<xsl:value-of select="../../../../@ReasonCode"/>
	</xsl:attribute>	
	</OrderLine>
	</xsl:if>
	</xsl:for-each>	
	</OrderLines>	
	</Order>
	</xsl:template>
</xsl:stylesheet>