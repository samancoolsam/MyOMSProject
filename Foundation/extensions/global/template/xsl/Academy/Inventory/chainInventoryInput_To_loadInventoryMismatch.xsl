<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output indent="yes" />

   <xsl:template match="/">
      <Items>
         <xsl:attribute name="ApplyDifferences">
            <xsl:value-of select="'Y'" />
         </xsl:attribute>

         <xsl:attribute name="ReasonCode">
            <xsl:value-of select="'INVENTORY_LOAD'" />
         </xsl:attribute>

         <xsl:attribute name="ReasonText">
            <xsl:value-of select="'INVENTORY_LOAD'" />
         </xsl:attribute>

         <xsl:attribute name="ShipNode">
            <xsl:value-of select="//@ShipNode" />
         </xsl:attribute>

         <xsl:attribute name="YantraMessageGroupID">
            <xsl:value-of select="//@YantraMessageGroupID" />
         </xsl:attribute>

         <xsl:for-each select="Item">
            <Item>
               <xsl:attribute name="InventoryOrganizationCode">
                  <xsl:value-of select="'Academy_Direct'" />
               </xsl:attribute>

               <xsl:attribute name="ItemID">
                  <xsl:value-of select="./@ItemID" />
               </xsl:attribute>

               <xsl:attribute name="ProductClass">
                  <xsl:value-of select="'GOOD'" />
               </xsl:attribute>

               <xsl:attribute name="UnitOfMeasure">
                  <xsl:value-of select="'EACH'" />
               </xsl:attribute>
               
               <xsl:attribute name="LastItemID">
                  <xsl:value-of select="./@LastItemID" />
               </xsl:attribute>               
               

               <Supplies>
                  <Supply>
                     <xsl:attribute name="AvailabilityType">
                        <xsl:value-of select="'TRACK'" />
                     </xsl:attribute>

                     <xsl:attribute name="Quantity">
                        <xsl:value-of select="./@Quantity" />
                     </xsl:attribute>

                     <xsl:attribute name="SupplyType">
                        <xsl:value-of select="'ONHAND'" />
                     </xsl:attribute>
                  </Supply>
               </Supplies>
            </Item>
         </xsl:for-each>
      </Items>
   </xsl:template>
</xsl:stylesheet>

