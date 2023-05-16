<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="/Receipt">
        <xsl:element name="LPN">
			  <xsl:attribute name="PalletId">
			   <xsl:value-of select="ReceiptLines/ReceiptLine/@PalletId"/>
			  </xsl:attribute>
			  
			  <xsl:if test ="(ReceiptLines/@TotalNumberOfRecords)=0"> 
			   <xsl:attribute name="PalletId">
					  <xsl:text>0</xsl:text>
			  </xsl:attribute>   
		</xsl:if>
     </xsl:element>
   </xsl:template>  
</xsl:stylesheet>