<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
	<xsl:template match="/AcademyMergedDocument">
           <xsl:element name="OrderList">
	      <xsl:for-each select="InputDocument/OrderList/Order">
		<xsl:element name="Order">
		 	<xsl:attribute name="OrderHeaderKey">
		 		<xsl:value-of select="@OrderHeaderKey"/>
			</xsl:attribute>
			<xsl:attribute name="SelectMethod">
		 		<xsl:value-of select="'WAIT'"/>
			</xsl:attribute>
			<xsl:attribute name="Override"><xsl:value-of select="'Y'" /></xsl:attribute>
			<xsl:attribute name="BillToID">
				<xsl:value-of select="../../../EnvironmentDocument/Customer/@CustomerID"/>
			</xsl:attribute> 
			</xsl:element> 
		 </xsl:for-each>
		</xsl:element>
       </xsl:template>
</xsl:stylesheet>