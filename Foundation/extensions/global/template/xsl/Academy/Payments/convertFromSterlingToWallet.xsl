<!--Input xml for this xsl

<PaymentMethod CreditCardExpDate="0411" CreditCardNo="4342234523538989"
    CreditCardType="VISA" CustomerID="00323423"
    DisplayCreditCardNo="8989" FirstName="John" LastName="Doe" MiddleName=""/>
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:soapenc="http://schemas.xmlsoap.org/soap/encoding/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:cd="java://com.academy.util.common.ResourceUtil" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <xsl:param name="date" select="'default value'" />

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
            <wc:ProcessPaymentAction xmlns:wc="http://www.ibm.com/xmlns/prod/WebSphereCommerce" xmlns:oa="http://www.openapplications.org/oagis/9" releaseID="9.0" version="9.1" versionID="9.1" xsi:schemaLocation="http://www.ibm.com/xmlns/prod/WebSphereCommerce ../BODs/ProcessPaymentAction.xsd">
               <oa:ApplicationArea>
                  <oa:CreationDateTime></oa:CreationDateTime>

                  <oa:BODID></oa:BODID>
               </oa:ApplicationArea>

               <wc:DataArea>
                  <oa:Process />

                  <wc:PaymentAction>
                     <wc:PaymentContext>
                        <wc:ChannelId>-1</wc:ChannelId>

                        <wc:Locale>en_US</wc:Locale>

                        <wc:PaymentConfigurationGroupId>default</wc:PaymentConfigurationGroupId>

                        <wc:StoreId>
							<xsl:value-of select="$storeId" />
						</wc:StoreId>

                     </wc:PaymentContext>

                     <wc:Action>createPaymentInstruction</wc:Action>

                     <wc:PaymentEntity>
                        <wc:PaymentInstruction>
                           <wc:OrderId>1</wc:OrderId>

                           <wc:Amount>1</wc:Amount>

                           <wc:Currency>USD</wc:Currency>

                           <wc:PaymentSystemName>SimpleOffline</wc:PaymentSystemName>

                           <wc:ExtendedData>
                              <wc:NameValuePair>
                                 <wc:Name>payment_method</wc:Name>

                                 <wc:Value>
                                    <xsl:value-of select="./@CreditCardType" />
                                 </wc:Value>
                              </wc:NameValuePair>

                              <wc:NameValuePair>
                                 <wc:Name>cc_brand</wc:Name>

                                 <wc:Value>
                                    <xsl:value-of select="./@CreditCardType" />
                                 </wc:Value>
                              </wc:NameValuePair>

                              <wc:NameValuePair>
                                 <wc:Name>account</wc:Name>

                                 <wc:Value>
                                    <xsl:value-of select="./@CreditCardNo" />
                                 </wc:Value>
                              </wc:NameValuePair>

                              <wc:NameValuePair>
                                 <wc:Name>expire_month</wc:Name>

                                 <wc:Value>
                                    <xsl:value-of select='substring(./@CreditCardExpDate,1,2)' />
                                 </wc:Value>
                              </wc:NameValuePair>

<!-- TODO: correct the value of year from yy to yyyy-->
                              <wc:NameValuePair>
                                 <wc:Name>expire_year</wc:Name>

                                 <wc:Value>
                                    <xsl:value-of select='concat("20",substring(./@CreditCardExpDate,3,4))' />
                                 </wc:Value>
                              </wc:NameValuePair>

                           </wc:ExtendedData>
                        </wc:PaymentInstruction>
                     </wc:PaymentEntity>
                  </wc:PaymentAction>
               </wc:DataArea>
            </wc:ProcessPaymentAction>
         </soapenv:Body>
      </soapenv:Envelope>
   </xsl:template>
</xsl:stylesheet>

