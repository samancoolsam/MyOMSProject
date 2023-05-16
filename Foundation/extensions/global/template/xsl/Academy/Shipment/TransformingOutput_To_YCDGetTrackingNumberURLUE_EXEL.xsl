<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
   <xsl:template match="AcademyMergedDocument">
      <xsl:element name="TrackingNumbers">
         <xsl:variable name="EXELURL" select="InputDocument/Shipments/@EXELURL" />

         <xsl:for-each select="EnvironmentDocument/TrackingNumbers/TrackingNumber">
            <xsl:if test="(@SCAC = 'CEVA')">
            <xsl:element name="TrackingNumber">
               <xsl:attribute name="RequestNo">
                  <xsl:value-of select="@RequestNo" />
               </xsl:attribute>

               <xsl:variable name="ProNo" select="../../../InputDocument/Shipments/Shipment/@ProNo" />

               		<xsl:attribute name="URL">
                  		<xsl:value-of select="concat($EXELURL,$ProNo)" />
               		</xsl:attribute>
            </xsl:element>
                           </xsl:if>
         </xsl:for-each>
      </xsl:element>
   </xsl:template>
</xsl:stylesheet>

