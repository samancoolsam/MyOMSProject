<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output indent="yes" />
   <xsl:template match="/">
	 <xsl:copy-of select="ItemList/Item/AssociationList/."/>
   </xsl:template>
</xsl:stylesheet>

