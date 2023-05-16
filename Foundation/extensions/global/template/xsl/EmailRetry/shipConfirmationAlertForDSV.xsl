<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:lxslt="http://xml.apache.org/xslt"
version="1.0">
<xsl:output method="html"/>
<xsl:template match="/">
Ship confirmation email for the order, <xsl:value-of select="/Order/@OrderNo" />, is sent to Business for IP PO CReation. 
Please click on the shipment no, <xsl:value-of select="/Order/Shipments/Shipment/@ShipmentNo" />, from the 'References' section to see the line level details.
</xsl:template>
</xsl:stylesheet>