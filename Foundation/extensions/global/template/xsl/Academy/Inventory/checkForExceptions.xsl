<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
   <xsl:template match="Inventory">
      <Inventory>
         <xsl:copy-of select="@*[not(.='')]" />
         <xsl:choose>
            <xsl:when test="count(UnprocessedItems/InventoryItem)&gt;0">
               <xsl:attribute name="ProcessExceptions">
                  <xsl:value-of select="'Y'" />
               </xsl:attribute>
               <xsl:copy-of select="./UnprocessedItems/."/>
            </xsl:when>
         </xsl:choose>
      </Inventory>
   </xsl:template>
</xsl:stylesheet>

