<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
		xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" >
<xsl:output method="xml"/>	
<xsl:template match="PaymentMethod">
   <PaymentMethod>
		<xsl:attribute name="HasError">
	    	<xsl:value-of select="'N'" />		
	    </xsl:attribute>
	    <xsl:attribute name="WalletID">
		    <xsl:value-of select="@WalletID"/>
	    </xsl:attribute>
   </PaymentMethod>
</xsl:template>
<xsl:template match="soapenv:Fault">
   <PaymentMethod>
	    <xsl:attribute name="HasError">
	    	<xsl:value-of select="'Y'" />		
	    </xsl:attribute>
	    <xsl:attribute name="ErrorCode">
	    	<xsl:value-of select="faultcode" />		
	    </xsl:attribute>
	    <xsl:attribute name="ErrorDescription">
	    	<xsl:value-of select="faultstring" />		
	    </xsl:attribute>
  </PaymentMethod>
</xsl:template>
</xsl:stylesheet>