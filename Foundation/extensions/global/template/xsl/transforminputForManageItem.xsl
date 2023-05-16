<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output indent="yes" />

   <xsl:template match="Items">
      <xsl:apply-templates select="Item" />
   </xsl:template>

   <xsl:template match="Item">
<!-- Start of Variable definition -->
      <xsl:variable name="resetValue">
         <xsl:value-of select="'Y'" />
      </xsl:variable>

      <xsl:variable name="varConveyable">
         <xsl:value-of select="./@ExtnConveyable" />
      </xsl:variable>

      <xsl:variable name="varFootwear">
         <xsl:value-of select="./@ExtnFootware" />
      </xsl:variable>

      <xsl:variable name="varSoftGoods">
         <xsl:value-of select="./@ExtnSoftgoods" />
      </xsl:variable>

      <xsl:variable name="varHardGoods">
         <xsl:value-of select="./@ExtnHardgoods" />
      </xsl:variable>

      <xsl:variable name="varLabelPick">
         <xsl:value-of select="./@ExtnLP" />
      </xsl:variable>

      <xsl:variable name="varGiftCard">
         <xsl:value-of select="./@ExtnIsGiftCard" />
      </xsl:variable>

      <xsl:variable name="varHighValue">
         <xsl:value-of select="./@ExtnIsGiftCard" />
      </xsl:variable>

      <xsl:variable name="varShipAlone">
         <xsl:value-of select="./@ExtnIsGiftCard" />
      </xsl:variable>

      <xsl:variable name="varPublishedStatus">
         <xsl:value-of select="'3000'" />
      </xsl:variable>

      <xsl:variable name="varHeldStatus">
         <xsl:value-of select="'2000'" />
      </xsl:variable>

      <xsl:variable name="varUOM">
         <xsl:value-of select="'EACH'" />
      </xsl:variable>

      <xsl:variable name="varOrgCode">
         <xsl:value-of select="'Academy_Direct'" />
      </xsl:variable>

      <xsl:variable name="varNo">
         <xsl:value-of select="'N'" />
      </xsl:variable>

<!-- End of Variable definition -->
      <xsl:for-each select="/Items/Item">
         <Item>
            <xsl:copy-of select="./@ItemID" />

            <xsl:attribute name="Action">
               <xsl:choose>
                  <xsl:when test="normalize-space(./@ExtnAction)='Delete'">
                     <xsl:value-of select="./@ExtnAction" />
                  </xsl:when>
               </xsl:choose>
            </xsl:attribute>

            <xsl:attribute name="CanUseAsServiceTool">
               <xsl:value-of select="$varNo" />
            </xsl:attribute>

            <xsl:attribute name="UnitOfMeasure">
               <xsl:value-of select="$varUOM" />
            </xsl:attribute>

            <xsl:attribute name="OrganizationCode">
               <xsl:value-of select="$varOrgCode" />
            </xsl:attribute>

            <xsl:attribute name="SyncTime">
               <xsl:value-of select="./@SyncTS" />
            </xsl:attribute>

            <PrimaryInformation>
               <xsl:attribute name="Status">
                  <xsl:choose>
                     <xsl:when test="normalize-space(./@ExtnPublish)='Y'">
                        <xsl:value-of select="$varPublishedStatus" />
                     </xsl:when>

                     <xsl:otherwise>
                        <xsl:value-of select="$varHeldStatus" />
                     </xsl:otherwise>
                  </xsl:choose>
               </xsl:attribute>

               <xsl:attribute name="ProductLine">
                  <xsl:value-of select="'AcademyItemAttributes'" />
               </xsl:attribute>

               <xsl:attribute name="DefaultProductClass">
                  <xsl:value-of select="'Good'" />
               </xsl:attribute>

               <xsl:attribute name="ItemType">
                  <xsl:choose>
                     <xsl:when test="($varHighValue='Y')">
                        <xsl:choose>
                           <xsl:when test="($varShipAlone='Y')">
                              <xsl:value-of select="'SAHVAL'" />
                           </xsl:when>

                           <xsl:otherwise>
                              <xsl:value-of select="'NSAHVAL'" />
                           </xsl:otherwise>
                        </xsl:choose>
                     </xsl:when>

                     <xsl:otherwise>
                        <xsl:choose>
                           <xsl:when test="($varShipAlone='Y')">
                              <xsl:value-of select="'SALVAL'" />
                           </xsl:when>

                           <xsl:otherwise>
                              <xsl:value-of select="'NSALVAL'" />
                           </xsl:otherwise>
                        </xsl:choose>
                     </xsl:otherwise>
                  </xsl:choose>
               </xsl:attribute>

               <xsl:attribute name="Description">
                  <xsl:value-of select="./@Description" />
               </xsl:attribute>
               
               <xsl:attribute name="ShortDescription">
                  <xsl:value-of select="./@Description" />
               </xsl:attribute>

               <xsl:attribute name="ExtendedDescription">
                  <xsl:value-of select="./@ExtendedDescription" />
               </xsl:attribute>

               <xsl:attribute name="ManufacturerName">
                  <xsl:value-of select="./@ManufacturerName" />
               </xsl:attribute>

               <xsl:attribute name="IsModelItem">
                  <xsl:value-of select="./@IsModelItem" />
               </xsl:attribute>

               <!--xsl:attribute name="Model">
                  <xsl:value-of select="./@Model" />
               </xsl:attribute-->

               <xsl:attribute name="ModelItemUnitOfMeasure">
                  <xsl:value-of select="./@ModelItemUnitOfMeasure" />
               </xsl:attribute>

               <xsl:attribute name="IsSerialTracked">
                  <xsl:value-of select="./@IsSerialTracked" />
               </xsl:attribute>

               <xsl:attribute name="UnitCost">
                  <xsl:value-of select="./@UnitCost" />
               </xsl:attribute>

               <xsl:attribute name="UnitHeight">
                  <xsl:value-of select="./@UnitHeight" />
               </xsl:attribute>

               <xsl:attribute name="UnitHeightUOM">
                  <xsl:value-of select="./@UnitHeightUOM" />
               </xsl:attribute>

               <xsl:attribute name="UnitLength">
                  <xsl:value-of select="./@UnitLength" />
               </xsl:attribute>

               <xsl:attribute name="UnitLengthUOM">
                  <xsl:value-of select="./@UnitLengthUOM" />
               </xsl:attribute>

               <xsl:attribute name="UnitWeight">
                  <xsl:value-of select="./@UnitWeight" />
               </xsl:attribute>

               <xsl:attribute name="UnitWeightUOM">
                  <xsl:value-of select="./@UnitWeightUOM" />
               </xsl:attribute>

               <xsl:attribute name="UnitWidth">
                  <xsl:value-of select="./@UnitWidth" />
               </xsl:attribute>

               <xsl:attribute name="UnitWidthUOM">
                  <xsl:value-of select="./@UnitWidthUOM" />
               </xsl:attribute>

               <xsl:attribute name="CountryOfOrigin">
                  <xsl:value-of select="./@CountryOfOrigin" />
               </xsl:attribute>

               <xsl:attribute name="IsHazmat">
                  <xsl:value-of select="./@IsHazmat" />
               </xsl:attribute>

               <xsl:attribute name="SizeCode">
                  <xsl:value-of select="/Items/Item/@ExtnSize" />
               </xsl:attribute>

               <xsl:attribute name="ColorCode">
                  <xsl:value-of select="/Items/Item/@ExtnColor" />
               </xsl:attribute>
            </PrimaryInformation>

            <InventoryParameters>
               <xsl:attribute name="OnhandSafetyFactorQuantity">
                  <xsl:value-of select="./@OnhandSafetyFactorQuantity" />
               </xsl:attribute>
            </InventoryParameters>

            <ClassificationCodes>
               <xsl:attribute name="VelocityCode">
                  <xsl:value-of select="./@Velocity" />
               </xsl:attribute>
               
               <xsl:attribute name="Model">
                  <xsl:value-of select="./@Model" />
               </xsl:attribute>

               <xsl:attribute name="StorageType">
                  <xsl:choose>
                     <xsl:when test="($varConveyable='Y')">
                        <xsl:if test="($varGiftCard='Y')">
                           <xsl:value-of select="'CONGC'" />
                        </xsl:if>

                        <xsl:if test="($varFootwear='Y')">
                           <xsl:value-of select="'CONFW'" />
                        </xsl:if>

                        <xsl:if test="($varHardGoods='Y')">
                           <xsl:value-of select="'CONHG'" />
                        </xsl:if>

                        <xsl:if test="($varSoftGoods='Y')">
                           <xsl:value-of select="'CONSG'" />
                        </xsl:if>

                        <xsl:if test="($varLabelPick='Y')">
                           <xsl:value-of select="'CONLP'" />
                        </xsl:if>
                     </xsl:when>

                     <xsl:otherwise>
                        <xsl:if test="($varLabelPick='Y')">
                           <xsl:value-of select="'NCONLP'" />
                        </xsl:if>

                        <xsl:if test="($varLabelPick!='Y')">
                           <xsl:value-of select="'NCONNLP'" />
                        </xsl:if>
                     </xsl:otherwise>
                  </xsl:choose>
               </xsl:attribute>
            </ClassificationCodes>

            <ItemInstructions>
               <xsl:attribute name="Reset">
                  <xsl:value-of select="$resetValue" />
               </xsl:attribute>

               <ItemInstruction>
                  <xsl:copy-of select="./Instructions/Instruction/@*" />
               </ItemInstruction>
            </ItemInstructions>

            <ItemAliasList>
               <ItemAlias>
                  <xsl:copy-of select="./Aliases/Alias/@*" />
               </ItemAlias>
            </ItemAliasList>

            <AssociationList>
               <xsl:copy-of select="./Associations/Association/." />
            </AssociationList>

            <Extn>
               <xsl:attribute name="ExtnHighValue">
                  <xsl:value-of select="./@ExtnHighValue" />
               </xsl:attribute>

               <xsl:attribute name="ExtnComponentQty">
                  <xsl:value-of select="./@ExtnComponentQty" />
               </xsl:attribute>

               <xsl:attribute name="ExtnConveyable">
                  <xsl:value-of select="./@ExtnConveyable" />
               </xsl:attribute>

               <xsl:attribute name="ExtnKit">
                  <xsl:value-of select="./@ExtnKit" />
               </xsl:attribute>

               <xsl:attribute name="ExtnComponentQty">
                  <xsl:value-of select="./@ExtnComponentQty" />
               </xsl:attribute>

               <xsl:attribute name="ExtnWhiteGloveEligible">
                  <xsl:value-of select="./@ExtnWhiteGloveEligible" />
               </xsl:attribute>

               <xsl:attribute name="ExtnStreetDate">
                  <xsl:value-of select="./@ExtnStreetDate" />
               </xsl:attribute>

               <xsl:attribute name="ExtnPublish">
                  <xsl:value-of select="./@ExtnPublish" />
               </xsl:attribute>

               <xsl:attribute name="ExtnIsGiftCard">
                  <xsl:value-of select="./@ExtnIsGiftCard" />
               </xsl:attribute>

               <xsl:attribute name="ExtnFootware">
                  <xsl:value-of select="./@ExtnFootware" />
               </xsl:attribute>

               <xsl:attribute name="ExtnHardgoods">
                  <xsl:value-of select="./@ExtnHardgoods" />
               </xsl:attribute>

               <xsl:attribute name="ExtnLP">
                  <xsl:value-of select="./@ExtnLP" />
               </xsl:attribute>

               <xsl:attribute name="ExtnEcommerceFlag">
                  <xsl:value-of select="./@ExtnEcommerceFlag" />
               </xsl:attribute>

               <xsl:attribute name="ExtnDSVFlag">
                  <xsl:choose>
                     <xsl:when test="normalize-space(/Items/Item/@ExtnEcommerceFlag)='Drop Ship'">
                        <xsl:value-of select="./@ExtnEcommerceFlag" />
                     </xsl:when>
                  </xsl:choose>
               </xsl:attribute>

               <xsl:attribute name="ExtnPrimarySku">
                  <xsl:value-of select="./@ExtnPrimarySku" />
               </xsl:attribute>

               <xsl:attribute name="ExtnSize">
                  <xsl:value-of select="/Items/Item/@ExtnSize" />
               </xsl:attribute>

               <xsl:attribute name="ExtnColor">
                  <xsl:value-of select="/Items/Item/@ExtnColor" />
               </xsl:attribute>
            </Extn>
         </Item>
      </xsl:for-each>
   </xsl:template>
</xsl:stylesheet>

