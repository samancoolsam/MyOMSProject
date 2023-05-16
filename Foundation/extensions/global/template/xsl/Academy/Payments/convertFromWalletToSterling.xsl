<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:wc="http://www.ibm.com/xmlns/prod/WebSphereCommerce" xmlns:oa="http://www.openapplications.org/oagis/9">
   <xsl:output method="xml" />

   <xsl:template match="/">
      <PaymentMethod>
         <xsl:attribute name="WalletID">

		 <!--<xsl:value-of select=	
		 		"soapenv:Envelope/soapenv:Body/wc:AcknowledgePaymentAction/wc:DataArea/wc:PaymentAction
		 		/wc:PaymentEntity/wc:PaymentInstruction/wc:Id" /> -->
            <xsl:value-of select="wc:AcknowledgePaymentAction/wc:DataArea/wc:PaymentAction/wc:PaymentEntity/wc:PaymentInstruction/wc:Id" />
         </xsl:attribute>
      </PaymentMethod>
   </xsl:template>
</xsl:stylesheet>

