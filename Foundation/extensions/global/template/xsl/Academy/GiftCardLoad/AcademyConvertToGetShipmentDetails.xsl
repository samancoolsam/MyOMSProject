<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="TaskQueue">
    
    
        <xsl:element name="Shipment">
         
            <xsl:attribute name="ShipmentKey">
               <xsl:value-of select="@DataKey"/>
            </xsl:attribute>

            

        </xsl:element>
        
    </xsl:template>
    
</xsl:stylesheet>