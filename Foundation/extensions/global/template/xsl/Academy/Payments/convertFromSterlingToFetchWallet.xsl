<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:cd="java://com.academy.util.common.ResourceUtil">

<xsl:variable name="Username" select="cd:get('WC_USER_NAME')" />

<xsl:variable name="password" select="cd:get('WC_USER_PASSWORD')" />

<xsl:variable name="storeId" select="cd:get('WC_STORE_ID')" />
   
   <xsl:output method="xml" />

   <xsl:template match="PaymentMethod">
      <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
         <soapenv:Header>
            <wsse:Security xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wsswssecurity-secext-1.0.xsd">
               <wsse:UsernameToken>
                  <wsse:Username>
						<xsl:value-of select="$Username" />
				  </wsse:Username>

                  <wsse:Password>
						<xsl:value-of select="$password" />
				  </wsse:Password>
               </wsse:UsernameToken>
            </wsse:Security>
         </soapenv:Header>

         <soapenv:Body>
            <wc:GetPaymentEntity xmlns:wc="http://www.ibm.com/xmlns/prod/WebSphereCommerce" xmlns:oa="http://www.openapplications.org/oagis/9" releaseID="9.0" version="9.1" versionID="9.1" xsi:schemaLocation="http://www.ibm.com/xmlns/prod/WebSphereCommerce ../BODs/GetPaymentEntity.xsd">
               <oa:ApplicationArea>
                  <oa:CreationDateTime></oa:CreationDateTime>

                  <oa:BODID></oa:BODID>
               </oa:ApplicationArea>

               <wc:DataArea>
                  <oa:Get>
                     <oa:Expression />
                  </oa:Get>

                  <wc:PaymentEntity>
                     <wc:PaymentContext>
                        <wc:ChannelId>-1</wc:ChannelId>

                        <wc:Locale>en_US</wc:Locale>

                        <wc:PaymentConfigurationGroupId>default</wc:PaymentConfigurationGroupId>

                        <wc:StoreId>
							<xsl:value-of select="$storeId" />
						</wc:StoreId>
                     </wc:PaymentContext>

                     <wc:PaymentInstruction>
                        <wc:Id>
                           <xsl:value-of select="./@WalletID" />
                        </wc:Id>
                     </wc:PaymentInstruction>
                  </wc:PaymentEntity>
               </wc:DataArea>
            </wc:GetPaymentEntity>
         </soapenv:Body>
      </soapenv:Envelope>
   </xsl:template>
</xsl:stylesheet>

