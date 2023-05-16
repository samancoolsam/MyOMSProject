<?xml version='1.0' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:template match="@*|node()">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()" />
      </xsl:copy>
   </xsl:template>

   <xsl:template match="//Order/Shipments/Shipment">
  <xsl:copy>
    <xsl:attribute name="ProNo">
      <xsl:value-of select="@ShipmentNo"/>
    </xsl:attribute>
    <xsl:apply-templates select="@*[not(local-name()='ProNo')]|node()"/>
  </xsl:copy>
</xsl:template> 
</xsl:stylesheet>

