<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output method="xml" indent="yes" />

   <xsl:template match="CloseString">
<!-- #3439 enhancement changes, not using the below xml-->
<!--
<CloseString>
<xsl:attribute name="EodTransDateTime">
 <xsl:value-of select="@EodTransDateTime"/>
</xsl:attribute>
<xsl:attribute name="EodTransNo">
 <xsl:value-of select="@EodTransNo"/>
</xsl:attribute>
<xsl:attribute name="RegisterNo">
 <xsl:value-of select="@RegisterNo"/>
</xsl:attribute>
<xsl:attribute name="StoreNo">
 <xsl:value-of select="@StoreNo"/>
</xsl:attribute>
<xsl:attribute name="EodFinTranKey">
 <xsl:value-of select="@EodInvoiceNo"/>
</xsl:attribute>
</CloseString> 
-->
<!-- New requirement for #3439 is  -->
<!-- 
<AcadFinTranEOD AcademyEodFinTranDate="<DateInvoiced in Sterling for last invoice>" AcademyEodFinTranKey="<Last Invoice No>"/>
-->
      <AcadFinTranEOD>
         <xsl:attribute name="AcademyEodFinTranDate">
            <xsl:value-of select="@EodTransDateTime" />
         </xsl:attribute>

         <xsl:attribute name="AcademyEodFinTranKey">
            <xsl:value-of select="@EodInvoiceNo" />
         </xsl:attribute>
      </AcadFinTranEOD>
   </xsl:template>
</xsl:stylesheet>

