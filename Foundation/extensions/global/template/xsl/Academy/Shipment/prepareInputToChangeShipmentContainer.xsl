<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
   <xsl:template match="/Containers">
      <xsl:element name="MultiApi">
         <xsl:for-each select="Container">
            <xsl:element name="API">
               <xsl:attribute name="Name">changeShipmentContainer</xsl:attribute>

               <xsl:element name="Input">
                  <xsl:element name="Container">
                     <xsl:attribute name="Action">Delete</xsl:attribute>

                     <xsl:attribute name="CancelRemovedQuantity">Y</xsl:attribute>

                     <xsl:attribute name="Override">Y</xsl:attribute>

                     <xsl:attribute name="ContainerNo">
                        <xsl:value-of select="@ContainerNo" />
                     </xsl:attribute>

                     <xsl:attribute name="ShipmentContainerKey">
                        <xsl:value-of select="@ShipmentContainerKey" />
                     </xsl:attribute>
                  </xsl:element>
               </xsl:element>
            </xsl:element>
         </xsl:for-each>
      </xsl:element>
   </xsl:template>
</xsl:stylesheet>

