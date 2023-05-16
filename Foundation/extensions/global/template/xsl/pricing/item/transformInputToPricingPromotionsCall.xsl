<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="/ItemList">
       
        
         <xsl:element name="Item">
          <xsl:attribute name="ItemID">
                <xsl:value-of select="Item/@ItemID"/>
       	     </xsl:attribute>            
       	 </xsl:element>
            
      
        
         
    </xsl:template>
</xsl:stylesheet>