<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<!-- Output of findInventory Api  -->
         
    <xsl:template match="/">
    <InventorySupplies>
    <xsl:for-each select="Promise/InventorySupplies/Items/Item">

      <InventorySupply ItemID="{@ItemID}" OrganizationCode="{@OrganizationCode}" ProductClass="{@ProductClass}"  UnitOfMeasure="{@UnitOfMeasure}">
       
         <xsl:variable name="Item1" select="@ItemID"/>

                  
                   <xsl:attribute name="TotalAvailableQty" > <xsl:value-of select='sum(Nodes/Node/Supplies/Supply/@AvailableQuantity)'/>
                                </xsl:attribute>
                                
              <xsl:for-each select="Nodes/Node">
                  
                 <Supplies Node="{@Node}"  AvailableQty="{(Supplies/Supply/@AvailableQuantity)}"  />   
                 
                 </xsl:for-each>                
              
         <xsl:apply-templates />
      </InventorySupply>
          
       </xsl:for-each>
       
       </InventorySupplies>
       </xsl:template>

   </xsl:stylesheet>
