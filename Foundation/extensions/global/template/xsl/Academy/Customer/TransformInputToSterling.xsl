<?xml version='1.0' encoding='ISO-8859-1' ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<!--<xsl:output method="xml" doctype-system='Update_Academy_CustomerSync_10.dtd' indent="yes" />-->
   <xsl:template match="/Update_Academy_CustomerSync/DataArea/Customer">
      <Customer CustomerType="02" OrganizationCode="Academy_Direct">
         <xsl:attribute name="CustomerKey">
            <xsl:value-of select="Profile/Personal/SterlingUsersId" />
         </xsl:attribute>

         <xsl:variable name="varTelTypeContact" select="./AddressInfo/ContactInfo/Telephone/@type" />

         <xsl:variable name="varTelContact" select="./AddressInfo/ContactInfo/Telephone" />

         <xsl:attribute name="ExternalCustomerID">
            <xsl:value-of select="Profile/Personal/WCSUsersId" />
         </xsl:attribute>

<!-- changing to out-of-box DateTime format FollowupDate="2010-08-03T01:32:42-04:00" 2449-7-20T14:57:27-241000-->
         <xsl:variable name="LastUpdateDateTranslate1" select="translate(Registration/LastUpdateDate,' ','T')" />

         <xsl:variable name="RegisteredDate1" select="translate(Registration/RegistrationDate,' ','T')" />

         <xsl:attribute name="SyncTS">
            <xsl:value-of select="$LastUpdateDateTranslate1" />
         </xsl:attribute>

         <xsl:attribute name="RegisteredDate">
            <xsl:value-of select="$RegisteredDate1" />
         </xsl:attribute>

         <Extn>
            <xsl:attribute name="ExtnTaxExemptID">
               <xsl:value-of select="/Update_Academy_CustomerSync/DataArea/Customer/Profile/Personal/TaxPayerId" />
            </xsl:attribute>
         </Extn>

         <CustomerContactList>
            <CustomerContact>
               <xsl:attribute name="LastName">
                  <xsl:value-of select="AddressInfo/PersonName/LastName" />
               </xsl:attribute>

               <xsl:attribute name="FirstName">
                  <xsl:value-of select="AddressInfo/PersonName/FirstName" />
               </xsl:attribute>

               <xsl:attribute name="UserID">
                  <xsl:value-of select="Registration/LogonInfo/LogonID" />
               </xsl:attribute>

               <xsl:attribute name="EmailID">
                  <xsl:value-of select="Registration/LogonInfo/LogonID" />
               </xsl:attribute>

               <xsl:attribute name="CustomerContactID">
                  <xsl:value-of select="Registration/LogonInfo/LogonID" />
               </xsl:attribute>

               <xsl:attribute name="Company">
                  <xsl:value-of select="/Update_Academy_CustomerSync/DataArea/Customer/Profile/Demographics/CompanyName" />
               </xsl:attribute>

               <xsl:if test="$varTelTypeContact = 'PRI'">
                  <xsl:attribute name="DayPhone">
                     <xsl:value-of select="($varTelContact[1])" />
                  </xsl:attribute>
               </xsl:if>

               <xsl:if test="$varTelTypeContact = 'ALT'">
                  <xsl:attribute name="MobilePhone">
                     <xsl:value-of select="($varTelContact[2])" />
                  </xsl:attribute>
               </xsl:if>

               <Extn>
                  <xsl:attribute name="ExtnRequiresWebprofileID">
                     <xsl:value-of select="'Y'" />
                  </xsl:attribute>

                  <xsl:attribute name="ExtnOptIn">
                     <xsl:choose>
                        <xsl:when test="/Update_Academy_CustomerSync/DataArea/Customer/AddressInfo/ContactInfo/ReceiveEmail/@value='TRUE' or /Update_Academy_CustomerSync/DataArea/Customer/AddressInfo/ContactInfo/ReceiveEmail/@value='True' or /Update_Academy_CustomerSync/DataArea/Customer/AddressInfo/ContactInfo/ReceiveEmail/@value='true'">
                           <xsl:value-of select="'Y'" />
                        </xsl:when>

                        <xsl:otherwise>
                           <xsl:value-of select="'N'" />
                        </xsl:otherwise>
                     </xsl:choose>
                  </xsl:attribute>

                  <xsl:attribute name="ExtnCompanyName">
                     <xsl:value-of select="/Update_Academy_CustomerSync/DataArea/Customer/Profile/Demographics/CompanyName" />
                  </xsl:attribute>

                  <xsl:choose>
                     <xsl:when test="/Update_Academy_CustomerSync/DataArea/Customer/Profile/Personal/RedLine='1'">
                        <xsl:attribute name="ExtnIsRedLined">
                           <xsl:value-of select="'Y'" />
                        </xsl:attribute>
                     </xsl:when>

                     <xsl:otherwise>
                        <xsl:attribute name="ExtnIsRedLined">
                           <xsl:value-of select="'N'" />
                        </xsl:attribute>
                     </xsl:otherwise>
                  </xsl:choose>
               </Extn>

               <CustomerAdditionalAddressList>
                  <xsl:attribute name="Reset">
                     <xsl:value-of select="'Y'" />
                  </xsl:attribute>

<!-- testing -->
                  <xsl:for-each select="AddressInfo">
                     <xsl:variable name="varAddressType" select="./AddressType" />

                     <xsl:variable name="varPrimary" select="./Address/@Primary" />

                     <xsl:variable name="varTelType" select="./ContactInfo/Telephone/@type" />

                     <xsl:variable name="varTel" select="./ContactInfo/Telephone" />

                     <CustomerAdditionalAddress>
                        <xsl:choose>
                           <xsl:when test="./Address/@primary = '1' and $varAddressType = 'SB'">
                              <xsl:attribute name="IsDefaultBillTo">
                                 <xsl:value-of select="'Y'" />
                              </xsl:attribute>

                              <xsl:attribute name="IsDefaultShipTo">
                                 <xsl:value-of select="'Y'" />
                              </xsl:attribute>
                           </xsl:when>

                           <xsl:otherwise>
                              <xsl:attribute name="IsDefaultBillTo">
                                 <xsl:value-of select="'N'" />
                              </xsl:attribute>

                              <xsl:attribute name="IsDefaultShipTo">
                                 <xsl:value-of select="'N'" />
                              </xsl:attribute>
                           </xsl:otherwise>
                        </xsl:choose>

                        <xsl:choose>
                           <xsl:when test="$varAddressType = 'B'">
                              <xsl:attribute name="IsBillTo">
                                 <xsl:value-of select="'Y'" />
                              </xsl:attribute>

                              <xsl:attribute name="IsShipTo">
                                 <xsl:value-of select="'N'" />
                              </xsl:attribute>
                           </xsl:when>

                           <xsl:otherwise>
                              <xsl:if test="$varAddressType = 'S'">
                                 <xsl:attribute name="IsBillTo">
                                    <xsl:value-of select="'N'" />
                                 </xsl:attribute>

                                 <xsl:attribute name="IsShipTo">
                                    <xsl:value-of select="'Y'" />
                                 </xsl:attribute>
                              </xsl:if>

                              <xsl:if test="$varAddressType = 'SB'">
                                 <xsl:attribute name="IsBillTo">
                                    <xsl:value-of select="'Y'" />
                                 </xsl:attribute>

                                 <xsl:attribute name="IsShipTo">
                                    <xsl:value-of select="'Y'" />
                                 </xsl:attribute>
                              </xsl:if>
                           </xsl:otherwise>
                        </xsl:choose>

                        <PersonInfo>
                           <xsl:attribute name="AddressLine1">
                              <xsl:value-of select="./Address/AddressLine[1]" />
                           </xsl:attribute>

                           <xsl:attribute name="AddressLine2">
                              <xsl:value-of select="./Address/AddressLine[2]" />
                           </xsl:attribute>

                           <xsl:attribute name="AddressLine3">
                              <xsl:value-of select="./Address/AddressLine[3]" />
                           </xsl:attribute>

                           <xsl:if test="$varTelType = 'PRI'">
                              <xsl:attribute name="DayPhone">
                                 <xsl:value-of select="($varTel[1])" />
                              </xsl:attribute>
                           </xsl:if>

                           <xsl:if test="$varTelType = 'ALT'">
                              <xsl:attribute name="MobilePhone">
                                 <xsl:value-of select="($varTel[2])" />
                              </xsl:attribute>
                           </xsl:if>

                           <xsl:attribute name="Company">
                              <xsl:value-of select="../Profile/Demographics/CompanyName" />
                           </xsl:attribute>

                           <xsl:attribute name="EMailID">
                              <xsl:value-of select="./ContactInfo/Email[1]" />
                           </xsl:attribute>

                           <xsl:attribute name="DayFaxNo">
                              <xsl:value-of select="./ContactInfo/Fax[1]" />
                           </xsl:attribute>

                           <xsl:attribute name="Country">
                              <xsl:value-of select="./Address/Country" />
                           </xsl:attribute>

                           <xsl:attribute name="ZipCode">
                              <xsl:value-of select="./Address/ZipCode" />
                           </xsl:attribute>

                           <xsl:attribute name="State">
                              <xsl:value-of select="./Address/State" />
                           </xsl:attribute>

                           <xsl:attribute name="City">
                              <xsl:value-of select="./Address/City" />
                           </xsl:attribute>

                           <xsl:attribute name="MiddleName">
                              <xsl:value-of select="./PersonName/MiddleName" />
                           </xsl:attribute>

                           <xsl:attribute name="FirstName">
                              <xsl:value-of select="./PersonName/FirstName" />
                           </xsl:attribute>

                           <xsl:attribute name="LastName">
                              <xsl:value-of select="./PersonName/LastName" />
                           </xsl:attribute>

                           <xsl:attribute name="Title">
                              <xsl:value-of select="./PersonName/Title" />
                           </xsl:attribute>

                           <Extn>
                              <xsl:attribute name="ExtnIsMilitary">
                                 <xsl:choose>
                                    <xsl:when test="./IsMilitary='1'">
                                       <xsl:value-of select="'Y'" />
                                    </xsl:when>

                                    <xsl:otherwise>
                                       <xsl:value-of select="'N'" />
                                    </xsl:otherwise>
                                 </xsl:choose>
                              </xsl:attribute>

                              <xsl:attribute name="ExtnIsPOBOXADDRESS">
                                 <xsl:choose>
                                    <xsl:when test="./AddressField[1]='1'">
                                       <xsl:value-of select="'Y'" />
                                    </xsl:when>

                                    <xsl:otherwise>
                                       <xsl:value-of select="'N'" />
                                    </xsl:otherwise>
                                 </xsl:choose>
                              </xsl:attribute>

                              <xsl:attribute name="ExtnIsSignatureRequired">
                                 <xsl:choose>
                                    <xsl:when test="./AddressField[2]='1'">
                                       <xsl:value-of select="'Y'" />
                                    </xsl:when>

                                    <xsl:otherwise>
                                       <xsl:value-of select="'N'" />
                                    </xsl:otherwise>
                                 </xsl:choose>
                              </xsl:attribute>

                              <xsl:attribute name="ExtnIsAddressVerified">
                                 <xsl:choose>
                                    <xsl:when test="./AddressField[3]='1'">
                                       <xsl:value-of select="'Y'" />
                                    </xsl:when>

                                    <xsl:otherwise>
                                       <xsl:value-of select="'N'" />
                                    </xsl:otherwise>
                                 </xsl:choose>
                              </xsl:attribute>

                              <xsl:attribute name="ExtnAddressNickName">
                                 <xsl:value-of select="'DEFAULT'" />
                              </xsl:attribute>

                              <xsl:attribute name="ExtnIsAPOFPO">
                                 <xsl:value-of select="'N'" />
                              </xsl:attribute>
                           </Extn>
                        </PersonInfo>
                     </CustomerAdditionalAddress>
                  </xsl:for-each>

<!-- testing -->
                  <xsl:for-each select="SecondaryAddressInfo">
                     <xsl:variable name="varAddressType" select="./AddressType" />

                     <xsl:variable name="varPrimary" select="./Address/@Primary" />

                     <xsl:variable name="varTelType" select="./ContactInfo/Telephone/@type" />

                     <xsl:variable name="varTel" select="./ContactInfo/Telephone" />

                     <CustomerAdditionalAddress>
                        <xsl:choose>
                           <xsl:when test="./Address/@primary = '1' and $varAddressType = 'SB'">
                              <xsl:attribute name="IsDefaultBillTo">
                                 <xsl:value-of select="'Y'" />
                              </xsl:attribute>

                              <xsl:attribute name="IsDefaultShipTo">
                                 <xsl:value-of select="'Y'" />
                              </xsl:attribute>

                              <xsl:attribute name="IsShipTo">
                                 <xsl:value-of select="'Y'" />
                              </xsl:attribute>

                              <xsl:attribute name="IsBillTo">
                                 <xsl:value-of select="'Y'" />
                              </xsl:attribute>
                           </xsl:when>

                           <xsl:otherwise>
                              <xsl:attribute name="IsDefaultBillTo">
                                 <xsl:value-of select="'N'" />
                              </xsl:attribute>

                              <xsl:attribute name="IsDefaultShipTo">
                                 <xsl:value-of select="'N'" />
                              </xsl:attribute>
                           </xsl:otherwise>
                        </xsl:choose>

                        <xsl:choose>
                           <xsl:when test="$varAddressType = 'B'">
                              <xsl:attribute name="IsBillTo">
                                 <xsl:value-of select="'Y'" />
                              </xsl:attribute>

                              <xsl:attribute name="IsShipTo">
                                 <xsl:value-of select="'N'" />
                              </xsl:attribute>
                           </xsl:when>

                           <xsl:otherwise>
                              <xsl:if test="$varAddressType = 'S'">
                                 <xsl:attribute name="IsBillTo">
                                    <xsl:value-of select="'N'" />
                                 </xsl:attribute>

                                 <xsl:attribute name="IsShipTo">
                                    <xsl:value-of select="'Y'" />
                                 </xsl:attribute>
                              </xsl:if>

                              <xsl:if test="$varAddressType = 'SB'">
                                 <xsl:attribute name="IsBillTo">
                                    <xsl:value-of select="'Y'" />
                                 </xsl:attribute>

                                 <xsl:attribute name="IsShipTo">
                                    <xsl:value-of select="'Y'" />
                                 </xsl:attribute>
                              </xsl:if>
                           </xsl:otherwise>
                        </xsl:choose>

                        <PersonInfo>
                           <xsl:attribute name="AddressLine1">
                              <xsl:value-of select="./Address/AddressLine[1]" />
                           </xsl:attribute>

                           <xsl:attribute name="AddressLine2">
                              <xsl:value-of select="./Address/AddressLine[2]" />
                           </xsl:attribute>

                           <xsl:attribute name="AddressLine3">
                              <xsl:value-of select="./Address/AddressLine[3]" />
                           </xsl:attribute>

                           <xsl:if test="$varTelType = 'PRI'">
                              <xsl:attribute name="DayPhone">
                                 <xsl:value-of select="($varTel[1])" />
                              </xsl:attribute>
                           </xsl:if>

                           <xsl:if test="$varTelType = 'ALT'">
                              <xsl:attribute name="MobilePhone">
                                 <xsl:value-of select="($varTel[2])" />
                              </xsl:attribute>
                           </xsl:if>

                           <xsl:attribute name="Company">
                              <xsl:value-of select="../Profile/Demographics/CompanyName" />
                           </xsl:attribute>

                           <xsl:attribute name="EMailID">
                              <xsl:value-of select="./ContactInfo/Email[1]" />
                           </xsl:attribute>

                           <xsl:attribute name="DayFaxNo">
                              <xsl:value-of select="./ContactInfo/Fax[1]" />
                           </xsl:attribute>

                           <xsl:attribute name="Country">
                              <xsl:value-of select="./Address/Country" />
                           </xsl:attribute>

                           <xsl:attribute name="ZipCode">
                              <xsl:value-of select="./Address/ZipCode" />
                           </xsl:attribute>

                           <xsl:attribute name="State">
                              <xsl:value-of select="./Address/State" />
                           </xsl:attribute>

                           <xsl:attribute name="City">
                              <xsl:value-of select="./Address/City" />
                           </xsl:attribute>

                           <xsl:attribute name="MiddleName">
                              <xsl:value-of select="./PersonName/MiddleName" />
                           </xsl:attribute>

                           <xsl:attribute name="FirstName">
                              <xsl:value-of select="./PersonName/FirstName" />
                           </xsl:attribute>

                           <xsl:attribute name="LastName">
                              <xsl:value-of select="./PersonName/LastName" />
                           </xsl:attribute>

                           <xsl:attribute name="Title">
                              <xsl:value-of select="./PersonName/Title" />
                           </xsl:attribute>

                           <Extn>
                              <xsl:attribute name="ExtnIsMilitary">
                                 <xsl:choose>
                                    <xsl:when test="./IsMilitary='1'">
                                       <xsl:value-of select="'Y'" />
                                    </xsl:when>

                                    <xsl:otherwise>
                                       <xsl:value-of select="'N'" />
                                    </xsl:otherwise>
                                 </xsl:choose>
                              </xsl:attribute>

                              <xsl:attribute name="ExtnIsPOBOXADDRESS">
                                 <xsl:choose>
                                    <xsl:when test="./AddressField[1]='1'">
                                       <xsl:value-of select="'Y'" />
                                    </xsl:when>

                                    <xsl:otherwise>
                                       <xsl:value-of select="'N'" />
                                    </xsl:otherwise>
                                 </xsl:choose>
                              </xsl:attribute>

                              <xsl:attribute name="ExtnIsSignatureRequired">
                                 <xsl:choose>
                                    <xsl:when test="./AddressField[2]='1'">
                                       <xsl:value-of select="'Y'" />
                                    </xsl:when>

                                    <xsl:otherwise>
                                       <xsl:value-of select="'N'" />
                                    </xsl:otherwise>
                                 </xsl:choose>
                              </xsl:attribute>

                              <xsl:attribute name="ExtnIsAddressVerified">
                                 <xsl:choose>
                                    <xsl:when test="./AddressField[3]='1'">
                                       <xsl:value-of select="'Y'" />
                                    </xsl:when>

                                    <xsl:otherwise>
                                       <xsl:value-of select="'N'" />
                                    </xsl:otherwise>
                                 </xsl:choose>
                              </xsl:attribute>

                              <xsl:attribute name="ExtnAddressNickName">
                                 <xsl:value-of select="./AddressNickName" />
                              </xsl:attribute>

                              <xsl:attribute name="ExtnIsAPOFPO">
                                 <xsl:value-of select="'N'" />
                              </xsl:attribute>
                           </Extn>
                        </PersonInfo>
                     </CustomerAdditionalAddress>
                  </xsl:for-each>
               </CustomerAdditionalAddressList>
            </CustomerContact>
         </CustomerContactList>
      </Customer>
   </xsl:template>
</xsl:stylesheet>

