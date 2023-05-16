<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
	xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:gif="http://ecom.academy.com/GiftCardService/">
<xsl:output method="xml"/>	
<xsl:template match="gif:GiftCardChargeResponse">
   <Payment>
	    <xsl:choose>
			<xsl:when test="normalize-space(@ErrorCode)">
			    <xsl:attribute name="ResponseCode">
			    	<xsl:value-of select="'DECLINED'" />		
			    </xsl:attribute>
			    <xsl:attribute name="AuthReturnCode">
			    	<xsl:value-of select="@ErrorCode" />		
			    </xsl:attribute>
			    <xsl:attribute name="AuthReturnMessage">
			    	<xsl:value-of select="@ErrorDescription" />		
			    </xsl:attribute>
			</xsl:when>
			<xsl:otherwise>
			    <xsl:attribute name="ResponseCode">
	    			<xsl:value-of select="'APPROVED'" />
	    		</xsl:attribute>
			    <xsl:attribute name="AuthorizationAmount">
					<xsl:value-of select="@AmountCharged" />
	    		</xsl:attribute>
	    		<xsl:attribute name="AuthorizationId">
		    		<xsl:value-of select="@GiftCardNo" />
	    		</xsl:attribute>
	    		<xsl:attribute name="TranAmount">
		    		<xsl:value-of select="@AmountCharged" />
	    		</xsl:attribute>
			</xsl:otherwise>
		</xsl:choose>
  </Payment>
</xsl:template>
<xsl:template match="soapenv:Fault">
   <Payment>
	    <xsl:attribute name="ResponseCode">
	    	<xsl:value-of select="'SERVICE_UNAVAILABLE'" />		
	    </xsl:attribute>
	    <xsl:attribute name="AuthReturnCode">
	    	<xsl:value-of select="faultcode" />		
	    </xsl:attribute>
	    <xsl:attribute name="AuthReturnMessage">
	    	<xsl:value-of select="faultstring" />		
	    </xsl:attribute>
  </Payment>
</xsl:template>
</xsl:stylesheet>