<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output indent="yes" />

   <xsl:template match="/AcademyMergedDocument/EnvironmentDocument/Order">
      <Order>
         <xsl:attribute name="OrderHeaderKey">
            <xsl:value-of select="@OrderHeaderKey" />
         </xsl:attribute>

         <xsl:attribute name="Action">
            <xsl:value-of select="'MODIFY'" />
         </xsl:attribute>

         <Notes>
            <Note>
            <xsl:attribute name="ContactUser">
                        <xsl:value-of select="Notes/Note/@ContactUser" />
                     </xsl:attribute>
                     <xsl:attribute name="Priority">
                        <xsl:value-of select="Notes/Note/@Priority" />
                     </xsl:attribute>
                     <xsl:attribute name="ReasonCode">
                        <xsl:value-of select="Notes/Note/@ReasonCode" />
                     </xsl:attribute>
                     
                     <xsl:variable name = "notetxt" select = "concat(Notes/Note/@NoteText,'The Gift Card Order No is ',/AcademyMergedDocument/InputDocument/Order/@OrderNo)" />
                     <xsl:attribute name="NoteText">
                        <xsl:value-of select="$notetxt" />
                     </xsl:attribute>
            </Note>
         </Notes>

         <Extn>
            <ACADOrderAppeasementList>
               <xsl:for-each select="/AcademyMergedDocument/EnvironmentDocument/Order/OrderLines/OrderLine">
                  <ACADOrderAppeasement>
                     <xsl:attribute name="AcademyOrderHeaderKey">
                        <xsl:value-of select="../../@OrderHeaderKey" />
                     </xsl:attribute>

                     <xsl:attribute name="AcademyOrderLineKey">
                        <xsl:value-of select="@OrderLineKey" />
                     </xsl:attribute>

                     <xsl:attribute name="UserId">
                        <xsl:value-of select="../../@UserId" />
                     </xsl:attribute>

                     <xsl:variable name="appCategory" select="../../@AppeasementCategory" />

                     <xsl:attribute name="AppeasementReason">
                        <xsl:value-of select="../../@AppeasementCategory" />
                     </xsl:attribute>

			<xsl:attribute name="AppeasementDescription">
                        <xsl:value-of select="../../@AppeasementDescription" />
                     </xsl:attribute>

		     <xsl:attribute name="OfferType">
                        <xsl:value-of select="../../AppeasementOffer/@OfferType" />
                     </xsl:attribute>

                     <xsl:variable name="discountpercent" select="../../AppeasementOffer/@DiscountPercent" />
			<xsl:attribute name="AppeasementPercent">
                           <xsl:value-of select="$discountpercent" />
                        </xsl:attribute>
                     <xsl:if test="$appCategory='Shipping'">
                        <xsl:variable name="lineshipCharge" select="LineCharges/LineCharge[@ChargeCategory='Shipping']/@ChargeAmount" />

                        <xsl:variable name="appAmnt" select="../../AppeasementOffer/@OfferAmount" />

                        <xsl:attribute name="AppeasementAmount">
                           <xsl:value-of select="format-number($appAmnt,'.##' )" />
                        </xsl:attribute>
                     </xsl:if>

                     <xsl:if test="$appCategory='Merchandise'">
                        <xsl:variable name="lineTotal" select="LineOverallTotals/@ExtendedPrice" />

                        <xsl:variable name="appAmnt1" select="../../AppeasementOffer/@OfferAmount" />

                        <xsl:attribute name="AppeasementAmount">
                           <xsl:value-of select="format-number($appAmnt1,'.##' )" />
                        </xsl:attribute>
                     </xsl:if>
                  </ACADOrderAppeasement>
               </xsl:for-each>
            </ACADOrderAppeasementList>
         </Extn>
      </Order>
   </xsl:template>
</xsl:stylesheet>

