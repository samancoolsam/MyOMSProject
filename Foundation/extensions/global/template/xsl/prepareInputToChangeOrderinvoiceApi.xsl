<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="Shipment">
        <xsl:element name="OrderInvoice">
			<xsl:attribute name="OrderInvoiceKey">
                <xsl:value-of select="./OrderInvoiceList/OrderInvoice/Extn/@ExtnOrderInvoiceKey"/>
            </xsl:attribute>
            <Extn>
             <xsl:attribute name="ExtnInvoiceNo">
                <xsl:value-of select="./OrderInvoiceList/OrderInvoice/Extn/@ExtnInvoiceNo"/>
             </xsl:attribute>
             </Extn>                            
            </xsl:element>
   </xsl:template>
</xsl:stylesheet>
