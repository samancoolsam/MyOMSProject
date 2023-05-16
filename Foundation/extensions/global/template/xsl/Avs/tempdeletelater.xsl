<?xml version='1.0' encoding='UTF-8' ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
   
   <xsl:template match="/">

      <xsl:variable name="PierbridgeAddressValidationResponse" select="AcademyMergedDocument/InputDocument/PierbridgeAddressValidationResponse/Status" />
      
      <xsl:choose>
            
      <xsl:when test="not($PierbridgeAddressValidationResponse)">
         <xsl:element name="PersonInfoList">
            <xsl:attribute name="Status">
               <xsl:value-of select="'AVS_DOWN'" />
            </xsl:attribute>

            <xsl:element name="PersonInfo">
               <xsl:attribute name="IsAddressVerified">
                  <xsl:value-of select="'N'" />
               </xsl:attribute>

               <xsl:attribute name="AVSReturnCode">
                  <xsl:value-of select="'AVS_DOWN'" />
               </xsl:attribute>
               
               <xsl:attribute name="AVSReturnCodeAvinash">
                  <xsl:value-of select="'AVS_DOWN_AVINASH'" />
               </xsl:attribute>
               
            </xsl:element>
            
            <xsl:element name="AvinashElement">
             <xsl:attribute name="StatusAvinash">
               <xsl:value-of select="'AVS_DOWN_StatusAvinash'" />
            </xsl:attribute>
            </xsl:element>
            
             <xsl:element name="AddressVerificationResponseMessages">
             	<xsl:element name="AddressVerificationResponseMessage">
             	  <xsl:attribute name="MessageCode">
               <xsl:value-of select="0" />
            </xsl:attribute>
            <xsl:attribute name="MessageText">
                           <xsl:value-of select="'AVS_DOWN'" />
                    </xsl:attribute>
             	</xsl:element>
              </xsl:element>
         </xsl:element>
      </xsl:when>

      <xsl:otherwise>
            
      <xsl:element name="PersonInfoList">
         <xsl:apply-templates select="OriginalAddressValidated" />

         <xsl:apply-templates select="//PierbridgeAddressValidationResponse/OriginalAddressValidated" />

         <xsl:variable name="AlternateAddresses" select="//PierbridgeAddressValidationResponse/AlternateAddresses/AlternateAddress" />

         <xsl:choose>
            <xsl:when test="not($AlternateAddresses)">
               <xsl:element name="PersonInfo">
                  <xsl:attribute name="IsAddressVerified">
                     <xsl:value-of select="'N'" />
                  </xsl:attribute>

                  <xsl:attribute name="AVSReturnCode">
                     <xsl:value-of select="'FAILED'" />
                  </xsl:attribute>
                  
                  <xsl:attribute name="AVSReturnCode_OtherwiseBlock">
                  <xsl:value-of select="'AVS_DOWN_OtherwiseBlock'" />
               </xsl:attribute>
               
               <xsl:element name="AvinashElement_OtherwiseBlock">
             <xsl:attribute name="StatusAvinash_OtherwiseBlock">
               <xsl:value-of select="'AVS_DOWN_StatusAvinash_OtherwiseBlock'" />
            </xsl:attribute>
            </xsl:element>

                  <xsl:element name="AddressVerificationResponseMessages">
                     <xsl:element name="AddressVerificationResponseMessage">
                        <xsl:apply-templates select="//PierbridgeAddressValidationResponse/Status" />
                     </xsl:element>
                  </xsl:element>
               </xsl:element>
            </xsl:when>

            <xsl:otherwise>
               <xsl:apply-templates select="//PierbridgeAddressValidationResponse/AlternateAddresses" />

               <xsl:element name="AddressVerificationResponseMessages">
                  <xsl:element name="AddressVerificationResponseMessage">
                     <xsl:apply-templates select="//PierbridgeAddressValidationResponse/Status" />
                  </xsl:element>
               </xsl:element>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:element>
      
      <xsl:element name="AvinashElement_OtherwiseBlock_Last">
             <xsl:attribute name="StatusAvinash_OtherwiseBlock_Last">
               <xsl:value-of select="'AVS_DOWN_StatusAvinash_OtherwiseBlock_Last'" />
            </xsl:attribute>
            </xsl:element>

      </xsl:otherwise>
         </xsl:choose>

   </xsl:template>

   <xsl:template match="Status">
      <xsl:for-each select="*">
         <xsl:if test="name(.)='Code'">
            <xsl:attribute name="MessageCode">
               <xsl:value-of select="(.)" />
            </xsl:attribute>
         </xsl:if>

         <xsl:if test="name(.)='Description'">
            <xsl:attribute name="MessageText">
               <xsl:if test="../Code='0'">
                  <xsl:value-of select="'INVALID ADDRESS'" />
               </xsl:if>

               <xsl:if test="../Code !='0'">
                  <xsl:value-of select="(.)" />
               </xsl:if>
            </xsl:attribute>
         </xsl:if>
      </xsl:for-each>
   </xsl:template>

<!-- ______________________________________________________________
Pierbridge returns with status failed, if the address validation failed. If yes, then stamp AVSReturnCode of the UE output to FAILED 
___________________________________________________________________
-->
   <xsl:template match="OriginalAddressValidated">
      <xsl:if test="../Status/Code='0'">
         <xsl:attribute name="IsAddressVerified">
            <xsl:value-of select="'N'" />
         </xsl:attribute>

         <xsl:attribute name="Status">
            <xsl:value-of select="'FAILED'" />
         </xsl:attribute>
      </xsl:if>

<!-- ______________________________________________________________
Pierbridge returns with status verified, if the address validation passes. If yes, then stamp AVSReturnCode of the UE output to VERIFIED 
___________________________________________________________________
-->
      <xsl:if test="../Status/Code='1'">
         <xsl:attribute name="IsAddressVerified">
            <xsl:value-of select="'Y'" />
         </xsl:attribute>

         <xsl:attribute name="Status">
            <xsl:value-of select="'VERIFIED'" />
         </xsl:attribute>
      </xsl:if>
   </xsl:template>

   <xsl:template match="AlternateAddresses">
      <xsl:for-each select="*">
         <xsl:element name="PersonInfo">
            <xsl:if test="../../Status/Code='1'">
               <xsl:if test="../../OriginalAddressValidated='1'">
                  <xsl:attribute name="AVSReturnCode">
                     <xsl:value-of select="'VERIFIED'" />
                  </xsl:attribute>
               </xsl:if>
            </xsl:if>

            <xsl:if test="../../Status/Code='0'">
               <xsl:if test="../../OriginalAddressValidated='0'">
                  <xsl:attribute name="AVSReturnCode">
                     <xsl:value-of select="'FAILED'" />
                  </xsl:attribute>
               </xsl:if>
            </xsl:if>

            <xsl:if test="../../Status/Code='1'">
               <xsl:if test="../../OriginalAddressValidated='0'">
                  <xsl:attribute name="AVSReturnCode">
                     <xsl:value-of select="'VERIFIED'" />
                  </xsl:attribute>
               </xsl:if>
            </xsl:if>

            <xsl:for-each select="*">
               <xsl:apply-templates select="//OriginalAddressValidated" />

               <xsl:if test="name(.)='Street'">
                  <xsl:attribute name="AddressLine1">
                     <xsl:value-of select="(.)" />
                  </xsl:attribute>
               </xsl:if>

               <xsl:if test="name(.)='Locale'">
                  <xsl:attribute name="AddressLine2">
                     <xsl:value-of select="(.)" />
                  </xsl:attribute>
               </xsl:if>

               <xsl:if test="name(.)='Other'">
                  <xsl:attribute name="AddressLine3">
                     <xsl:value-of select="(.)" />
                  </xsl:attribute>
               </xsl:if>

               <xsl:if test="name(.)='City'">
                  <xsl:attribute name="City">
                     <xsl:value-of select="(.)" />
                  </xsl:attribute>
               </xsl:if>

               <xsl:if test="name(.)='Country'">
                  <xsl:attribute name="Country">
                     <xsl:value-of select="../../../../../EnvironmentDocument/PersonInfo/@Country" />
                  </xsl:attribute>
               </xsl:if>

               <xsl:if test="name(.)='Region'">
                  <xsl:attribute name="State">
                     <xsl:value-of select="(.)" />
                  </xsl:attribute>
               </xsl:if>

               <xsl:if test="name(.)='PostalCode'">
                  <xsl:attribute name="ZipCode">
                     <xsl:value-of select="(.)" />
                  </xsl:attribute>
               </xsl:if>
               
<!--               <xsl:if test="name(.)='FirstName'"> -->
                  <xsl:attribute name="FirstName">
                     <xsl:value-of select="../../../../../EnvironmentDocument/PersonInfo/@FirstName" />
				  </xsl:attribute>
<!--               </xsl:if> -->
               
<!--			   <xsl:if test="name(.)='LastName'"> -->
                  <xsl:attribute name="LastName">
                     <xsl:value-of select="../../../../../EnvironmentDocument/PersonInfo/@LastName" />
                  </xsl:attribute>
<!--               </xsl:if> -->
				 <xsl:attribute name="Company">
                     <xsl:value-of select="../../../../../EnvironmentDocument/PersonInfo/@Company" />
                  </xsl:attribute>
                  
                  <xsl:attribute name="DayPhone">
                     <xsl:value-of select="../../../../../EnvironmentDocument/PersonInfo/@DayPhone" />
                  </xsl:attribute>
                  
                  <xsl:attribute name="EMailID">
                     <xsl:value-of select="../../../../../EnvironmentDocument/PersonInfo/@EMailID" />
                  </xsl:attribute>
                  
                   <xsl:attribute name="EveningPhone">
                     <xsl:value-of select="../../../../../EnvironmentDocument/PersonInfo/@EveningPhone" />
                  </xsl:attribute>
                  
                  <xsl:attribute name="Title">
                     <xsl:value-of select="../../../../../EnvironmentDocument/PersonInfo/@Title" />
                  </xsl:attribute>                  
               
            </xsl:for-each>

            <xsl:element name="Extn">
               <xsl:attribute name="ExtnIsAPOFPO">
                  <xsl:value-of select="../../../../EnvironmentDocument/PersonInfo/Extn/@ExtnIsAPOFPO" />
               </xsl:attribute>

               <xsl:attribute name="ExtnIsPOBOXADDRESS">
                  <xsl:value-of select="../../../../EnvironmentDocument/PersonInfo/Extn/@ExtnIsPOBOXADDRESS" />
               </xsl:attribute>

               <xsl:attribute name="ExtnIsSignatureRequired">
                  <xsl:value-of select="../../../../EnvironmentDocument/PersonInfo/Extn/@ExtnIsSignatureRequired" />
               </xsl:attribute>

               <xsl:attribute name="ExtnAddressNickName">
                  <xsl:value-of select="'N'" />
               </xsl:attribute>

               <xsl:attribute name="ExtnIsAddressVerified">
                  <xsl:value-of select="'N'" />
               </xsl:attribute>

               <xsl:attribute name="ExtnIsMilitary">
                  <xsl:value-of select="'N'" />
               </xsl:attribute>
            </xsl:element>
         </xsl:element>
      </xsl:for-each>
   </xsl:template>
</xsl:stylesheet>