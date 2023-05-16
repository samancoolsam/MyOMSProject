<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="InventorySnapShot">
    
    
        <xsl:element name="InventorySync">
         
            <xsl:attribute name="Node">
               <xsl:value-of select="ShipNode/@ShipNode"/>
            </xsl:attribute>

            <xsl:attribute name="TimeStamp">
               <xsl:value-of select="@TimeStamp"/>
            </xsl:attribute>            
                       
	<xsl:for-each select="ShipNode/Item">
            
  		 <xsl:element name="Item">
  		 
		 <xsl:attribute name="ItemID">
		 
               <xsl:value-of select="@ItemID"/>
               
            </xsl:attribute>
            
            <xsl:attribute name="Quantity">
            
               <xsl:value-of select="SupplyDetails/@Quantity"/>
               
            </xsl:attribute>     
                    </xsl:element> 
</xsl:for-each>
             
	

        </xsl:element>
        
    </xsl:template>
    
</xsl:stylesheet>