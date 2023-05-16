<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:template match="/">
      <PaymentMethod xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:wc="http://www.ibm.com/xmlns/prod/WebSphereCommerce" xmlns:oa="http://www.openapplications.org/oagis/9">
         <xsl:attribute name="CreditCardExpDate">
            <xsl:value-of select="concat(PaymentMethod/@CreditCardExpMonth,substring(PaymentMethod/@CreditCardExpYear,3))" />
         </xsl:attribute>
         
         

         <xsl:copy-of select="PaymentMethod/*" />


         <xsl:copy-of select="PaymentMethod/@*" />

         <xsl:attribute name="DisplayCreditCardNo">
            <xsl:value-of select="substring(PaymentMethod/@DisplayCreditCardNo,13)" />
         </xsl:attribute>

      </PaymentMethod>
   </xsl:template>
</xsl:stylesheet>

