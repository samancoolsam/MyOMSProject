<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="/Task">
        <xsl:element name="LPN">
	
                    
           
                
			  <xsl:attribute name="PalletId">
			   <xsl:value-of select="Inventory/@SourcePalletId"/>
			  </xsl:attribute>
             
             
        </xsl:element>
        
    </xsl:template>
    
</xsl:stylesheet>