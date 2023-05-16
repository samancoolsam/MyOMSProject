<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="/AcademyMergedDocument/InputDocument/Order">
   
     <xsl:element name="Order">
        <xsl:attribute name="Action">
                <xsl:text>MODIFY</xsl:text>     
		</xsl:attribute>
       <xsl:copy-of select="./@*" />

     <xsl:copy-of select="./*" />
         </xsl:element>
    </xsl:template>
</xsl:stylesheet>