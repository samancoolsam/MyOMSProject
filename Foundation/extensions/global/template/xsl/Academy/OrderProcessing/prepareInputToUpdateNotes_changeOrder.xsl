<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:template match="/">
      <xsl:element name="Order">
         <xsl:choose>
            <xsl:when test="/Order/OrderLines/OrderLine/@DerivedFromOrderHeaderKey!=''">
               <xsl:variable name="enteredBy" select="/Order/@Createuserid" />

               <xsl:variable name="returnOrderNo" select="/Order/@OrderNo" />

               <xsl:attribute name="OrderHeaderKey">
                  <xsl:value-of select="/Order/OrderLines/OrderLine[@DerivedFromOrderHeaderKey!='']/@DerivedFromOrderHeaderKey" />
               </xsl:attribute>

               <xsl:attribute name="Override">
                  <xsl:value-of select="'Y'" />
               </xsl:attribute>

               <xsl:element name="Notes">
                  <xsl:element name="Note">
                     <xsl:attribute name="NoteText">
                        <xsl:value-of select="concat('The return order# ',$returnOrderNo)" />
                     </xsl:attribute>
                  </xsl:element>
               </xsl:element>
            </xsl:when>

            <xsl:otherwise>
               <xsl:attribute name="OrderHeaderKey">
               </xsl:attribute>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:element>
   </xsl:template>
</xsl:stylesheet>

