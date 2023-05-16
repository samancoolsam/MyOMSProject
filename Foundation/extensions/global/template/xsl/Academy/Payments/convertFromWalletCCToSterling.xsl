<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:wc="http://www.ibm.com/xmlns/prod/WebSphereCommerce" xmlns:oa="http://www.openapplications.org/oagis/9">
   <xsl:output method="xml" />

   <xsl:template match="wc:ShowPaymentEntity">
      <PaymentMethod>
         <!--<xsl:for-each select="soapenv:Envelope/soapenv:Body/wc:ShowPaymentEntity/wc:DataArea/wc:PaymentEntity/wc:PaymentInstruction/wc:ExtendedData/wc:NameValuePair"> -->
         <xsl:for-each select="wc:DataArea/wc:PaymentEntity/wc:PaymentInstruction/wc:ExtendedData/wc:NameValuePair">         
            <xsl:variable name="AttributeName" select="wc:Name" />

            <xsl:if test="$AttributeName='billto_country'">
               <xsl:attribute name="BillToCountry">
                  <xsl:value-of select="wc:Value" />
               </xsl:attribute>
            </xsl:if>

            <xsl:if test="$AttributeName='BillToStateProvince'">
               <xsl:attribute name="billto_stateprovince">
                  <xsl:value-of select="wc:Value" />
               </xsl:attribute>
            </xsl:if>

            <xsl:if test="$AttributeName='billto_address1'">
               <xsl:attribute name="BillToAddress1">
                  <xsl:value-of select="wc:Value" />
               </xsl:attribute>
            </xsl:if>

            <xsl:if test="$AttributeName='billto_city'">
               <xsl:attribute name="BillToCity">
                  <xsl:value-of select="wc:Value" />
               </xsl:attribute>
            </xsl:if>

            <xsl:if test="$AttributeName='billto_zipcode'">
               <xsl:attribute name="BillToZipCode">
                  <xsl:value-of select="wc:Value" />
               </xsl:attribute>
            </xsl:if>

            <xsl:if test="$AttributeName='account'">
               <xsl:attribute name="CreditCardNo">
                  <xsl:value-of select="wc:Value" />
               </xsl:attribute>
            </xsl:if>

            <xsl:if test="$AttributeName='expire_year'">
               <xsl:attribute name="CreditCardExpYear">
                  <xsl:value-of select="wc:Value" />
               </xsl:attribute>
            </xsl:if>

            <xsl:if test="$AttributeName='expire_month'">
               <xsl:attribute name="CreditCardExpMonth">
                  <xsl:value-of select="wc:Value" />
               </xsl:attribute>
            </xsl:if>
            
            <xsl:if test="$AttributeName='cc_brand'">
               <xsl:attribute name="CreditCardType">
                  <xsl:value-of select="wc:Value" />
               </xsl:attribute>
            </xsl:if>

            <xsl:if test="$AttributeName='payment_method'">
               <xsl:attribute name="PaymentMethod">
                  <xsl:value-of select="wc:Value" />
               </xsl:attribute>
            </xsl:if>

            <xsl:if test="$AttributeName='account'">
               <xsl:attribute name="DisplayCreditCardNo">
                  <xsl:value-of select="wc:Value" />
               </xsl:attribute>
            </xsl:if>

            <xsl:if test="$AttributeName=''">
               <xsl:attribute name="FirstName">
                  <xsl:value-of select="wc:Value" />
               </xsl:attribute>
            </xsl:if>

            <xsl:if test="$AttributeName=''">
               <xsl:attribute name="LastName">
                  <xsl:value-of select="wc:Value" />
               </xsl:attribute>
            </xsl:if>

            <xsl:if test="$AttributeName=''">
               <xsl:attribute name="MiddleName">
                  <xsl:value-of select="wc:Value" />
               </xsl:attribute>
            </xsl:if>

            <xsl:if test="$AttributeName=''">
               <xsl:attribute name="CustomerID">
                  <xsl:value-of select="wc:Value" />
               </xsl:attribute>
            </xsl:if>
         </xsl:for-each>
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