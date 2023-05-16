<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
   <xsl:output method="xml" doctype-system='Update_Academy_CustomerSync_10.dtd' indent="yes" />

   <xsl:template match="/">
      <Update_Academy_CustomerSync version="1.0">
         <ControlArea>
            <Verb value='Update'>
            </Verb>

            <Noun value='Academy_CustomerSync'>
            </Noun>
         </ControlArea>

         <DataArea>
            <xsl:value-of select="Customer" />

            <Customer>
               <Registration>
                  <LogonInfo>
                     <LogonID>
                        <xsl:value-of select="Customer/CustomerContactList/CustomerContact/@EmailID" />
                     </LogonID>
                  </LogonInfo>

                  <StoreInfo>
                     <StoreName>Academy</StoreName>
                  </StoreInfo>

                  <xsl:variable name="RegisteredDateTranslate" select="Customer/@RegisteredDate" />

<!--<xsl:variable name="LastUpdateDateTranslate" select="Customer/@SyncTS" />-->
                  <xsl:variable name="LastUpdateDateTranslate">
                     <xsl:choose>
                        <xsl:when test="Customer/@SyncTS!=''">
                           <xsl:value-of select="Customer/@SyncTS" />
                        </xsl:when>

                        <xsl:otherwise>
                           <xsl:value-of select="Customer/@MaxModifyTS" />
                        </xsl:otherwise>
                     </xsl:choose>
                  </xsl:variable>

                  <xsl:variable name="LastUpdateDateTranslate1" select="translate($LastUpdateDateTranslate,'T',' ')" />

<!-- <xsl:variable name="LastUpdateDateTranslate2" select="substring($LastUpdateDateTranslate1,0,15)" /> -->
                  <xsl:variable name="RegisteredDateTranslate1" select="translate($RegisteredDateTranslate,'T',' ')" />

                  <xsl:variable name="additional" select="'.000000'" />

                  <RegistrationDate>
<!-- <xsl:value-of select="Customer/@RegisteredDate" /> -->
                     <xsl:value-of select="concat(substring($RegisteredDateTranslate1,0,20),$additional)" />
                  </RegistrationDate>

                  <LastUpdateDate>
<!-- <xsl:value-of select="Customer/@SyncTS" /> -->
                     <xsl:value-of select="concat(substring($LastUpdateDateTranslate1,0,20),$additional)" />
                  </LastUpdateDate>
               </Registration>

               <xsl:variable name="varCustomerAddress" select="Customer/CustomerContactList/CustomerContact/CustomerAdditionalAddressList/CustomerAdditionalAddress" />

               <xsl:for-each select="/Customer/CustomerContactList/CustomerContact/CustomerAdditionalAddressList/CustomerAdditionalAddress">
                  <xsl:choose>
                     <xsl:when test="./PersonInfo/Extn/@ExtnAddressNickName='DEFAULT' or (position()='1')">
                        <AddressInfo>
                           <AddressType>
                              <xsl:choose>
                                 <xsl:when test="./@IsBillTo='Y' and (./@IsShipTo='Y')">
                                    <xsl:value-of select="'SB'" />
                                 </xsl:when>

                                 <xsl:when test="./@IsBillTo='Y'">
                                    <xsl:value-of select="'B'" />
                                 </xsl:when>

                                 <xsl:when test="./@IsShipTo='Y'">
                                    <xsl:value-of select="'S'" />
                                 </xsl:when>
                              </xsl:choose>
                           </AddressType>

                           <PersonName>
                              <LastName>
                                 <xsl:value-of select="/Customer/CustomerContactList/CustomerContact/@LastName" />
                              </LastName>

                              <FirstName>
                                 <xsl:value-of select="/Customer/CustomerContactList/CustomerContact/@FirstName" />
                              </FirstName>
                           </PersonName>

                           <Address self="0">
                              <xsl:attribute name="primary">
                                 <xsl:choose>
                                    <xsl:when test="./@IsDefaultShipTo='Y' and (./@IsDefaultBillTo='Y')">
                                       <xsl:value-of select="1" />
                                    </xsl:when>

                                    <xsl:otherwise>
                                       <xsl:value-of select="0" />
                                    </xsl:otherwise>
                                 </xsl:choose>
                              </xsl:attribute>

                              <AddressLine>
                                 <xsl:value-of select="./PersonInfo/@AddressLine1" />
                              </AddressLine>

                              <AddressLine>
                                 <xsl:value-of select="./PersonInfo/@AddressLine2" />
                              </AddressLine>

                              <City>
                                 <xsl:value-of select="./PersonInfo/@City" />
                              </City>

                              <State>
                                 <xsl:value-of select="./PersonInfo/@State" />
                              </State>

                              <ZipCode>
                                 <xsl:value-of select="./PersonInfo/@ZipCode" />
                              </ZipCode>

                              <Country>
                                 <xsl:value-of select="./PersonInfo/@Country" />
                              </Country>
                           </Address>

                           <ContactInfo>
                              <xsl:if test="normalize-space(./PersonInfo/@DayPhone) != ''">
                                 <Telephone type="PRI" publish="1">
                                    <xsl:value-of select="./PersonInfo/@DayPhone" />
                                 </Telephone>
                              </xsl:if>

                              <xsl:if test="normalize-space(./PersonInfo/@MobilePhone) != ''">
                                 <Telephone type="ALT" publish="1">
                                    <xsl:value-of select="./PersonInfo/@MobilePhone" />
                                 </Telephone>
                              </xsl:if>

                              <ReceiveEmail>
                                 <xsl:attribute name="value">
                                    <xsl:choose>
                                       <xsl:when test="/Customer/CustomerContactList/CustomerContact/Extn/@ExtnOptIn='Y'">
                                          <xsl:value-of select="'TRUE'" />
                                       </xsl:when>

                                       <xsl:otherwise>
                                          <xsl:value-of select="'FALSE'" />
                                       </xsl:otherwise>
                                    </xsl:choose>
                                 </xsl:attribute>
                              </ReceiveEmail>
                           </ContactInfo>

                           <AddressField>
                              <xsl:choose>
                                 <xsl:when test="./PersonInfo/Extn/@ExtnIsPOBOXADDRESS='Y'">
                                    <xsl:value-of select="1" />
                                 </xsl:when>

                                 <xsl:otherwise>
                                    <xsl:value-of select="0" />
                                 </xsl:otherwise>
                              </xsl:choose>
                           </AddressField>

                           <AddressField>
                              <xsl:choose>
                                 <xsl:when test="./PersonInfo/Extn/@ExtnIsSignatureRequired='Y'">
                                    <xsl:value-of select="1" />
                                 </xsl:when>

                                 <xsl:otherwise>
                                    <xsl:value-of select="0" />
                                 </xsl:otherwise>
                              </xsl:choose>
                           </AddressField>

                           <AddressField>
                              <xsl:choose>
                                 <xsl:when test="./PersonInfo/Extn/@ExtnIsAddressVerified='Y'">
                                    <xsl:value-of select="1" />
                                 </xsl:when>

                                 <xsl:otherwise>
                                    <xsl:value-of select="0" />
                                 </xsl:otherwise>
                              </xsl:choose>
                           </AddressField>

                           <IsMilitary>
                              <xsl:choose>
                                 <xsl:when test="./PersonInfo/Extn/@ExtnIsMilitary='Y'">
                                    <xsl:value-of select="1" />
                                 </xsl:when>

                                 <xsl:otherwise>
                                    <xsl:value-of select="0" />
                                 </xsl:otherwise>
                              </xsl:choose>
                           </IsMilitary>
                        </AddressInfo>
                     </xsl:when>

                     <xsl:otherwise>
                        <SecondaryAddressInfo>
                           <AddressNickName>
                              <xsl:if test="./PersonInfo/Extn/@ExtnAddressNickName!=''">
                                 <xsl:value-of select="./PersonInfo/Extn/@ExtnAddressNickName" />
                              </xsl:if>

                              <xsl:if test="./PersonInfo/Extn/@ExtnAddressNickName='N' or ./PersonInfo/Extn/@ExtnAddressNickName='' or not(./PersonInfo/Extn/@ExtnAddressNickName)">
                                 <xsl:value-of select="concat('Shipping',position())" />
                              </xsl:if>
                           </AddressNickName>

                           <AddressType>
                              <xsl:choose>
                                 <xsl:when test="./@IsBillTo='Y' and (./@IsShipTo='Y')">
                                    <xsl:value-of select="'SB'" />
                                 </xsl:when>

                                 <xsl:when test="./@IsBillTo='Y'">
                                    <xsl:value-of select="'B'" />
                                 </xsl:when>

                                 <xsl:when test="./@IsShipTo='Y'">
                                    <xsl:value-of select="'S'" />
                                 </xsl:when>
                              </xsl:choose>
                           </AddressType>

                           <PersonName>
                              <LastName>
                                 <xsl:value-of select="./PersonInfo/@LastName" />
                              </LastName>

                              <FirstName>
                                 <xsl:value-of select="./PersonInfo/@FirstName" />
                              </FirstName>
                           </PersonName>

                           <Address self="0">
                              <xsl:attribute name="primary">
                                 <xsl:choose>
                                    <xsl:when test="./@IsDefaultShipTo='Y' and (./@IsDefaultBillTo='Y')">
                                       <xsl:value-of select="1" />
                                    </xsl:when>

                                    <xsl:otherwise>
                                       <xsl:value-of select="0" />
                                    </xsl:otherwise>
                                 </xsl:choose>
                              </xsl:attribute>

                              <AddressLine>
                                 <xsl:value-of select="./PersonInfo/@AddressLine1" />
                              </AddressLine>

                              <AddressLine>
                                 <xsl:value-of select="./PersonInfo/@AddressLine2" />
                              </AddressLine>

                              <City>
                                 <xsl:value-of select="./PersonInfo/@City" />
                              </City>

                              <State>
                                 <xsl:value-of select="./PersonInfo/@State" />
                              </State>

                              <ZipCode>
                                 <xsl:value-of select="./PersonInfo/@ZipCode" />
                              </ZipCode>

                              <Country>
                                 <xsl:value-of select="./PersonInfo/@Country" />
                              </Country>
                           </Address>

                           <ContactInfo>
                              <xsl:if test="normalize-space(./PersonInfo/@DayPhone) != ''">
                                 <Telephone type="PRI" publish="1">
                                    <xsl:value-of select="./PersonInfo/@DayPhone" />
                                 </Telephone>
                              </xsl:if>

                              <xsl:if test="normalize-space(./PersonInfo/@MobilePhone) != ''">
                                 <Telephone type="ALT" publish="1">
                                    <xsl:value-of select="./PersonInfo/@MobilePhone" />
                                 </Telephone>
                              </xsl:if>
                           </ContactInfo>

                           <AddressField>
                              <xsl:choose>
                                 <xsl:when test="./PersonInfo/Extn/@ExtnIsPOBOXADDRESS='Y'">
                                    <xsl:value-of select="1" />
                                 </xsl:when>

                                 <xsl:otherwise>
                                    <xsl:value-of select="0" />
                                 </xsl:otherwise>
                              </xsl:choose>
                           </AddressField>

                           <AddressField>
                              <xsl:choose>
                                 <xsl:when test="./PersonInfo/Extn/@ExtnIsSignatureRequired='Y'">
                                    <xsl:value-of select="1" />
                                 </xsl:when>

                                 <xsl:otherwise>
                                    <xsl:value-of select="0" />
                                 </xsl:otherwise>
                              </xsl:choose>
                           </AddressField>

                           <AddressField>
                              <xsl:choose>
                                 <xsl:when test="./PersonInfo/Extn/@ExtnIsAddressVerified='Y'">
                                    <xsl:value-of select="1" />
                                 </xsl:when>

                                 <xsl:otherwise>
                                    <xsl:value-of select="0" />
                                 </xsl:otherwise>
                              </xsl:choose>
                           </AddressField>

                           <IsMilitary>
                              <xsl:choose>
                                 <xsl:when test="./PersonInfo/Extn/@ExtnIsMilitary='Y'">
                                    <xsl:value-of select="1" />
                                 </xsl:when>

                                 <xsl:otherwise>
                                    <xsl:value-of select="0" />
                                 </xsl:otherwise>
                              </xsl:choose>
                           </IsMilitary>
                        </SecondaryAddressInfo>
                     </xsl:otherwise>
                  </xsl:choose>
               </xsl:for-each>

               <Profile type="C">
                  <Personal>
                     <SterlingUsersId>
                        <xsl:value-of select="/Customer/@CustomerKey" />
                     </SterlingUsersId>

                     <WCSUsersId>
                        <xsl:value-of select="/Customer/@ExternalCustomerID" />
                     </WCSUsersId>

                     <TaxPayerId>
                        <xsl:value-of select="/Customer/Extn/@ExtnTaxExemptID" />
                     </TaxPayerId>

                     <RedLine>
                        <xsl:choose>
                           <xsl:when test="/Customer/CustomerContactList/CustomerContact/Extn/@ExtnIsRedLined='Y'">
                              <xsl:value-of select="'1'" />
                           </xsl:when>

                           <xsl:otherwise>
                              <xsl:value-of select="'0'" />
                           </xsl:otherwise>
                        </xsl:choose>
                     </RedLine>
                  </Personal>

                  <Demographics>
                     <CompanyName>
                        <xsl:value-of select="($varCustomerAddress)/PersonInfo/@Company" />
                     </CompanyName>
                  </Demographics>
               </Profile>
            </Customer>
         </DataArea>
      </Update_Academy_CustomerSync>
   </xsl:template>
</xsl:stylesheet>

