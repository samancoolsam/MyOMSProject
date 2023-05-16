<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="/Order">
        <xsl:element name="Order">
        
        
          <xsl:attribute name="OrderHeaderKey">
                <xsl:value-of select="@OrderHeaderKey"/>
       	     </xsl:attribute>            
       	 
       	   <xsl:attribute name="ReadUncommitted">
                <xsl:text>Y</xsl:text>
       	     </xsl:attribute> 
            
         </xsl:element>
        
         
    </xsl:template>
</xsl:stylesheet>