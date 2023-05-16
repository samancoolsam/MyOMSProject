<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >
<xsl:output method="xml" indent="yes" />	
<xsl:template match="CloseString">
   <AcadFinTranEOD>
   		<xsl:attribute name="AcademyEodFinTranDate">
			<xsl:value-of select="@EodTransDate"/>
	    </xsl:attribute>
	    <xsl:attribute name="AcademyEodTranDateTime">
			<xsl:value-of select="@EodTransDateTime"/>
	    </xsl:attribute>
	    <xsl:attribute name="AcademyEodFinTranKey">
			<xsl:value-of select="@EodTransKey"/>
	    </xsl:attribute>
	    <xsl:attribute name="AcademyEodFinTransNo">
			<xsl:value-of select="@EodTransNo"/>
	    </xsl:attribute>
	    <xsl:attribute name="AcademyEodFinRegisterNo">
			<xsl:value-of select="@RegisterNo"/>
	    </xsl:attribute>
	    <xsl:attribute name="AcademyEodFinStoreNo">
			<xsl:value-of select="@StoreNo"/>
	    </xsl:attribute>
   </AcadFinTranEOD>
</xsl:template>
</xsl:stylesheet>