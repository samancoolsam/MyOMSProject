<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
				xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" >
<xsl:output method="xml"/>	
<xsl:template match="PaymentMethod">
   <PaymentMethod>
		<xsl:attribute name="HasError">
	    	<xsl:value-of select="'N'" />		
	    </xsl:attribute>
	    <xsl:attribute name="CreditCardExpDate">
		    <xsl:value-of select="@CreditCardExpDate"/>
	    </xsl:attribute>
	    <xsl:attribute name="CreditCardNo">
		    <xsl:value-of select="@CreditCardNo"/>
	    </xsl:attribute>
   	    <xsl:attribute name="CreditCardType">
		    <xsl:value-of select="@CreditCardType"/>
	    </xsl:attribute>
   	    <xsl:attribute name="CustomerID">
		    <xsl:value-of select="@CustomerID"/>
	    </xsl:attribute>
   	    <xsl:attribute name="DisplayCreditCardNo">
		    <xsl:value-of select="@DisplayCreditCardNo"/>
	    </xsl:attribute>
   	    <xsl:attribute name="FirstName">
		    <xsl:value-of select="@FirstName"/>
	    </xsl:attribute>
   	    <xsl:attribute name="LastName">
		    <xsl:value-of select="@LastName"/>
	    </xsl:attribute>
   	    <xsl:attribute name="MiddleName">
		    <xsl:value-of select="@MiddleName"/>
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