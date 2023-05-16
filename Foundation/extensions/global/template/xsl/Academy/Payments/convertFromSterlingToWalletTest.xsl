<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml"/>	
<xsl:template match="PaymentMethod">
   <PaymentMethod>
	    <xsl:attribute name="CreditCardExpDate">
		    <xsl:choose>
				<xsl:when test="contains(@CreditCardExpDate, '/')">
	    			<xsl:value-of select="substring-before(@CreditCardExpDate, '/')"/>
	    			<xsl:value-of select="substring-after(@CreditCardExpDate, '/')"/>
 				</xsl:when>
   				<xsl:otherwise>
					<xsl:value-of select="@CreditCardExpDate"/>
   				</xsl:otherwise>
			</xsl:choose>
	    </xsl:attribute>
	    <xsl:attribute name="CreditCardNo">
		    <xsl:value-of select="@CreditCardNo"/>
	    </xsl:attribute>
	    <xsl:attribute name="CreditCardType">
		    <xsl:value-of select="@CreditCardType"/>
	    </xsl:attribute>
	    <xsl:attribute name="CustomerID">
		    <xsl:value-of select="''"/>
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
</xsl:stylesheet>