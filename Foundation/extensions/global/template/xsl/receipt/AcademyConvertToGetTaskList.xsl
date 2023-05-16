<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">

    <xsl:template match="/Receipt">

        <xsl:element name="Task">
		     
            
		  <xsl:element name="TaskReferences">
			<xsl:attribute name="ReceiptNo">
				<xsl:value-of select="@ReceiptNo"/>
			  </xsl:attribute>
		   </xsl:element>

                  <xsl:element name="Inventory">
			  <xsl:attribute name="ReceiptHeaderKey">
				<xsl:value-of select="@ReceiptHeaderKey"/>
			  </xsl:attribute>
		  </xsl:element>
             
        </xsl:element>
        
    </xsl:template>
    
</xsl:stylesheet>