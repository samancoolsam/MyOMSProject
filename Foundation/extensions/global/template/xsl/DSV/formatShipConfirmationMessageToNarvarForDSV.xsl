<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="Shipment">
<Shipment>
<xsl:copy-of select="@*"/>
<xsl:copy-of select = "node()[not(name(.)='ShipmentLines')][not(name(.)='OrderInvoiceList')]" />
<ShipmentLines>
<xsl:for-each select='/OrderInvoice/Shipment/ShipmentLines/ShipmentLine'>
<ShipmentLine>
<xsl:copy-of select="@*"/>
<xsl:copy-of select= "node()[not(name(.)='OrderLine')]"/>
<xsl:for-each select='./OrderLine'>
<OrderLine>
<xsl:copy-of select="@*[not(name()='OrderLineKey')][not(name()='OrderedQty')][not(name()='OriginalOrderedQty')][not(name()='ShippedQuantity')]"/>
<xsl:copy-of select="node()[not(name(.)='Extn')]"/>
<Extn>
<xsl:variable name="extnInitialPromiseDate" select="./Extn/@ExtnInitialPromiseDate"/>
<xsl:attribute name="ExtnInitialPromiseDate">
<xsl:if test="$extnInitialPromiseDate !='' ">
<xsl:value-of select="substring($extnInitialPromiseDate, 1, 10)" />
</xsl:if>
</xsl:attribute>
</Extn>
</OrderLine>
</xsl:for-each>
</ShipmentLine>
</xsl:for-each>
</ShipmentLines>
<OrderInvoiceList>
<xsl:for-each select="/OrderInvoice/Shipment/OrderInvoiceList/OrderInvoice">
<xsl:variable name="vExtnInvoiceNo">
<xsl:value-of select="./Extn/@ExtnInvoiceNo"/>
</xsl:variable>
<xsl:if test="$vExtnInvoiceNo != ''"> 
<OrderInvoice>
<xsl:copy-of select="@*"/>
<Extn>
<xsl:copy-of select="./Extn/@*[not(name()='ExtnInvoiceNo')]"/>
<xsl:attribute name="ExtnInvoiceNo">
<xsl:value-of select="/OrderInvoice/@InvoiceNo"/>
</xsl:attribute>
</Extn>
</OrderInvoice>
</xsl:if> 
</xsl:for-each>
</OrderInvoiceList>
</Shipment>
</xsl:template>
</xsl:stylesheet>
