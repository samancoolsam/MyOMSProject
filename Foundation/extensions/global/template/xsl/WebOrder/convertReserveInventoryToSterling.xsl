<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<!-- Copy everything from input xml -->
   <xsl:template match="@*|node()">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()" />
      </xsl:copy>
   </xsl:template>

   <xsl:template match="Promise">
      <Promise>
         <xsl:copy-of select="@*" />

<!-- Adding CheckInventory Attr with Flag as 'Y'-->
         <xsl:attribute name="CheckInventory">Y</xsl:attribute>

         <xsl:apply-templates />
      </Promise>
   </xsl:template>
</xsl:stylesheet>

