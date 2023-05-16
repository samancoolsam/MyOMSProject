<?xml version='1.0'?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
<Shipment>
<xsl:copy-of select="/Shipment/Containers/*"/>	
</Shipment>
</xsl:template>
</xsl:stylesheet>