<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
	<xsl:template match="/Customer">
		<xsl:element name="Order">
		 	<xsl:attribute name="DocumentType">
				<xsl:text>0001</xsl:text>
			</xsl:attribute>
			<xsl:attribute name="EnterpriseCode">
				<xsl:text>Academy_Direct</xsl:text>
			</xsl:attribute>
			<!-- Don't Search for Orders which are beyond the 'Included in Shipment' (3350) -->
			<xsl:attribute name="StatusQryType">
				<xsl:text>LT</xsl:text>
			</xsl:attribute>
			<xsl:attribute name="Status">
				<xsl:text>3351</xsl:text>
			</xsl:attribute> 
			<xsl:element name="Extn">
						 <xsl:attribute name="ExtnExternalCustomerID">
                     <xsl:choose>
                        <xsl:when test="(normalize-space(@ExternalCustomerID))!=''">
                           <xsl:value-of select="@ExternalCustomerID"/>
                        </xsl:when>
                        <xsl:otherwise>
                           <xsl:value-of select="'NOT'" />
                        </xsl:otherwise>
                     </xsl:choose>
                  </xsl:attribute>
			
			<!--<xsl:attribute name="ExtnExternalCustomerID">
				 <xsl:value-of select="@ExternalCustomerID"/>
			</xsl:attribute>  
		    --></xsl:element>
   		 </xsl:element> 
           </xsl:template>
</xsl:stylesheet>