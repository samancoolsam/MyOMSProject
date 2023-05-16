<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="/AdjustLocationInventory">
       <xsl:element name="Adjustment">
        <xsl:element name="Inventory">
         
            <xsl:attribute name="ShipNode">
               <xsl:value-of select="@Node"/>
            </xsl:attribute>

            <xsl:attribute name="ItemID">
               <xsl:value-of select="Source/Inventory/InventoryItem/@ItemID"/>
            </xsl:attribute>   
            
            <xsl:attribute name="ProductClass">
               <xsl:value-of select="Source/Inventory/InventoryItem/@ProductClass"/>
            </xsl:attribute> 
            
            <xsl:attribute name="UnitOfMeasure">
               <xsl:value-of select="Source/Inventory/InventoryItem/@UnitOfMeasure"/>
            </xsl:attribute>          
            
            <xsl:attribute name="AdjustmentReasonCode">
               <xsl:value-of select="Audit/@ReasonCode"/>
            </xsl:attribute>

            <xsl:attribute name="ExtnTimeStamp">
               <xsl:value-of select="@ExtnTimeStamp"/>
            </xsl:attribute>

             <xsl:attribute name="QuantityAdjusted">
               <xsl:value-of select="Source/Inventory/@Quantity"/>
            </xsl:attribute>
            
			<xsl:attribute name="UserId">
               <xsl:value-of select="@UserId"/>
        	</xsl:attribute>	
            
   
        </xsl:element>
        
        </xsl:element>
        
    </xsl:template>
    
</xsl:stylesheet>