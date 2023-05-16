<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
	<xsl:template match="/Customer">
			<xsl:element name="Customer">
			<xsl:attribute name="OrganizationCode">
				<xsl:text>Academy_Direct</xsl:text>
			</xsl:attribute>
			<xsl:attribute name="CustomerKey">
					<xsl:value-of select="@CustomerKey"/>
				</xsl:attribute>
				
				
			<xsl:if test="@CustomerKey=''">	
				
			 <xsl:attribute name="ExternalCustomerID">
					<xsl:value-of select="@ExternalCustomerID" />
				</xsl:attribute> 
				
			</xsl:if>
				
		   	    </xsl:element> 
   	 </xsl:template>
</xsl:stylesheet>