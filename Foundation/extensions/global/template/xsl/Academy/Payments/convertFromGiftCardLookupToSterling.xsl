<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
	xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:gif="http://ecom.academy.com/GiftCardService/">
<xsl:output method="xml"/>	
<xsl:template match="gif:GiftCardBalanceResponse">
   <PaymentMethod>
	    <xsl:attribute name="FundsAvailable">
			<xsl:choose>
        		<xsl:when test="@Balance &gt; 0">
					<xsl:value-of select="@Balance"/>
        		</xsl:when>
        		<xsl:otherwise>
          			<xsl:value-of select="0"/>
        		</xsl:otherwise>
      		</xsl:choose>
	    </xsl:attribute>
	    <xsl:attribute name="ValidationResult">
			<xsl:value-of select="@ValidationResult"/>
	    </xsl:attribute>
        <xsl:attribute name="ErrorCode">
			<xsl:value-of select="@ErrorCode"/>
	    </xsl:attribute>
	    <xsl:attribute name="ErrorDescription">
			<xsl:value-of select="@ErrorDescription"/>
	    </xsl:attribute>
   </PaymentMethod>
</xsl:template>
<xsl:template match="soapenv:Fault">
   <PaymentMethod>
	    <xsl:attribute name="ErrorCode">
	    	<xsl:value-of select="faultcode" />		
	    </xsl:attribute>
	    <xsl:attribute name="ErrorDescription">
	    	<xsl:value-of select="faultstring" />		
	    </xsl:attribute>
  </PaymentMethod>
</xsl:template>
</xsl:stylesheet>