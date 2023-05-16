<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
   <xsl:template match="/OrderRelease">
      <xsl:element name="OrderRelease">
         <xsl:attribute name="OrderReleaseKey">
             <xsl:value-of select="@OrderReleaseKey" />
         </xsl:attribute>
		 <xsl:element name="Extn">		 
			<xsl:attribute name="ExtnSharedInventory">
				<xsl:text>S</xsl:text>
			</xsl:attribute>
		 </xsl:element>
       </xsl:element>
   </xsl:template>
</xsl:stylesheet>

