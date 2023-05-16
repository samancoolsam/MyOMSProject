<?xml version = "1.0" encoding = "UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
   <xsl:output indent="yes" />

   <xsl:template match="ContainerList">
      <xsl:element name="Container">
         <xsl:attribute name="GenerateContainerScm">Y</xsl:attribute>

         <xsl:attribute name="ShipmentContainerKey">
            <xsl:value-of select="Container/@ShipmentContainerKey" />
         </xsl:attribute>
      </xsl:element>
   </xsl:template>
</xsl:stylesheet>

