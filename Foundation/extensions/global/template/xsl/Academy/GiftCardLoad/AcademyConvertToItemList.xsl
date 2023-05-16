<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="Shipment">
    
    
        <xsl:element name="Item">
        <xsl:element name="Extn">
         <xsl:attribute name="ExtnIsGiftCard">
               <xsl:text>Y</xsl:text>
            </xsl:attribute>
        </xsl:element>
         <xsl:element name="ComplexQuery">
         
            <xsl:attribute name="Operator">
               <xsl:text>AND</xsl:text>
            </xsl:attribute>

         	   <xsl:element name="Or">
            
		            <xsl:for-each select="ShipmentLines/ShipmentLine">
		            
		             <xsl:element name="Exp">
		             <xsl:attribute name="Name">
		               <xsl:text>ItemID</xsl:text>
		            </xsl:attribute>
		            <xsl:attribute name="Value">
		                 <xsl:value-of select="@ItemID"/>
		            </xsl:attribute>
		             </xsl:element>
		             </xsl:for-each>
		             
             	</xsl:element>
             
             
             </xsl:element>

        </xsl:element>
        
    </xsl:template>
    
</xsl:stylesheet>