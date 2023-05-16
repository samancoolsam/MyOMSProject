<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output indent="yes" />

   <xsl:key name="avail" match="Item" use="@Availability" />

   <xsl:template match="/">
      <xsl:for-each select="key('avail','TRACK')">
         <InventoryNodeControl>
            <xsl:attribute name="ItemID">
               <xsl:value-of select="./@ItemID" />
            </xsl:attribute>

            <xsl:attribute name="ProductClass">
               <xsl:value-of select="./@ProductClass" />
            </xsl:attribute>

            <xsl:attribute name="Node">
               <xsl:value-of select="./@ShipNode" />
            </xsl:attribute>

            <xsl:attribute name="InventoryPictureCorrect">
               <xsl:value-of select="'Y'" />
            </xsl:attribute>

            <xsl:attribute name="OrganizationCode">
               <xsl:value-of select="./@OrganizationCode" />
            </xsl:attribute>

            <xsl:attribute name="UnitOfMeasure">
               <xsl:value-of select="./@UnitOfMeasure" />
            </xsl:attribute>

            <xsl:attribute name="NodeControlType">
               <xsl:value-of select="'ON_HOLD'" />
            </xsl:attribute>
         </InventoryNodeControl>
      </xsl:for-each>
   </xsl:template>
</xsl:stylesheet>

