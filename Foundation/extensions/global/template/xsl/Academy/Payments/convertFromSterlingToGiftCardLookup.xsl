<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml"/>	
<xsl:template match="PaymentMethod">
   <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:gif="http://ecom.academy.com/GiftCardService/">
   		<soapenv:Header/>
   		<soapenv:Body>
   			<gif:GiftCardBalanceRequest>
	    		<xsl:attribute name="GiftCardNo">
		    		<xsl:value-of select="@SvcNo"/>
	    		</xsl:attribute>
	    		<xsl:attribute name="PIN">
					<xsl:value-of select="@PaymentReference1"/>
	    		</xsl:attribute>
	    		<xsl:attribute name="TerminalNumber">
		    		<xsl:value-of select="'200'"/>
	    		</xsl:attribute>
	    		<xsl:attribute name="User">
					<xsl:value-of select="'Sterling'"/>
	    		</xsl:attribute>
	    		<xsl:attribute name="OrderNo">
		    		<xsl:value-of select="' '"/>
	    		</xsl:attribute>
   			</gif:GiftCardBalanceRequest>
   		</soapenv:Body>
	</soapenv:Envelope>
</xsl:template>
</xsl:stylesheet>