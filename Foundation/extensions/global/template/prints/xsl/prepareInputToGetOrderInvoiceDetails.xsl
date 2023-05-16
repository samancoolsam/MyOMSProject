<?xml version = "1.0" encoding = "UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
<xsl:output indent="yes"/>
	<xsl:template match="OrderInvoiceList">
	
		<xsl:element name="GetOrderInvoiceDetails">
	<xsl:attribute name="InvoiceNo">
								<xsl:value-of select="OrderInvoice/@InvoiceNo"/>
							</xsl:attribute>
								<xsl:attribute name="InvoiceKey">
								<xsl:value-of select="OrderInvoice/@OrderInvoiceKey"/>
							</xsl:attribute>
		</xsl:element>

</xsl:template>
</xsl:stylesheet>