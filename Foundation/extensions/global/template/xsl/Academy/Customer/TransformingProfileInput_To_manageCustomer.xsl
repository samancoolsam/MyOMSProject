<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
	<xsl:template match="/AcademyMergedDocument">
		<xsl:element name="Customer">
		 	<xsl:attribute name="CustomerType">
				<xsl:text>02</xsl:text>
			</xsl:attribute>
			<xsl:attribute name="ExternalCustomerID">
				<xsl:value-of select="EnvironmentDocument/Customer/@ExternalCustomerID"/>
			</xsl:attribute> 
			<xsl:attribute name="CustomerID">
				<xsl:value-of select="InputDocument/CustomerList/Customer/@CustomerID"/>
			</xsl:attribute> 
			<xsl:attribute name="CustomerKey">
				<xsl:value-of select="InputDocument/CustomerList/Customer/@CustomerKey"/>
			</xsl:attribute> 
			<xsl:attribute name="RegisteredDate">
				 <xsl:value-of select="EnvironmentDocument/Customer/@RegisteredDate"/>
			</xsl:attribute>  
			<xsl:attribute name="SyncTS">
				 <xsl:value-of select="EnvironmentDocument/Customer/@SyncTS"/>
			</xsl:attribute>  
			<xsl:attribute name="OrganizationCode">
				<xsl:text>Academy_Direct</xsl:text>
			</xsl:attribute>
			<xsl:attribute name="Operation">
				<xsl:text>Create</xsl:text>
			</xsl:attribute>
			
			 <Extn>
			<xsl:attribute name="ExtnTaxExemptID">
				<xsl:value-of select="EnvironmentDocument/Customer/Extn/@ExtnTaxExemptID"/>
			</xsl:attribute>
			  </Extn>
			
			   <xsl:copy-of select="EnvironmentDocument/Customer/CustomerContactList/."/>
   		 </xsl:element> 
           </xsl:template>
</xsl:stylesheet>
