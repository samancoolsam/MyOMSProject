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
	  
	  <!--Start WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation -->
		<xsl:variable name="varExtnIsHazmat">
            <xsl:value-of select="normalize-space(./@ExtnIsHazmat)" />
        </xsl:variable>
		<!--End WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation -->
      
        <!--Start of changes for STL-741-->
        <xsl:variable name="varHazmatClass">
            <xsl:value-of select="normalize-space(./@HazmatClass)" />
        </xsl:variable>
        
        <xsl:variable name="varIsHazmat">
            <xsl:choose>
                <xsl:when test="$varHazmatClass='C1' or $varExtnIsHazmat='Y'"> <!-- As a part of WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation -->
                    <xsl:value-of select="'N'" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="'N'" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <xsl:variable name="varExtnWhiteGloveEligible">
            <xsl:choose>
               <xsl:when test="$varHazmatClass='C1' or $varExtnIsHazmat='Y'"> <!--As a part of WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation -->
                    <xsl:value-of select="'N'" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="normalize-space(./@ExtnWhiteGloveEligible)" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <xsl:variable name="varConveyable">
            <xsl:choose>
               <xsl:when test="$varHazmatClass='C1' or $varExtnIsHazmat='Y'"> <!--As a part of WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation -->
                    <xsl:value-of select="'Y'" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="normalize-space(./@ExtnConveyable)" />
                </xsl:otherwise>
            </xsl:choose>
      </xsl:variable>
      <!--End of changes for STL-741-->

      <xsl:variable name="varLabelPick">
         <xsl:value-of select="./@ExtnLabelPick" />
      </xsl:variable>

      <xsl:variable name="varECommerce">
         <xsl:value-of select="./@ExtnEcommerceCode" />
      </xsl:variable>

      <xsl:variable name="varItemDivision">
         <xsl:value-of select="./@ExtnItemDivision" />
      </xsl:variable>

      <xsl:variable name="varGiftCard">
         <xsl:value-of select="./@ExtnIsGiftCard" />
      </xsl:variable>

      <xsl:variable name="varHighValue">
         <xsl:value-of select="./@ExtnHighValue" />
      </xsl:variable>
      
        <!--Start of changes for STL-741-->        
        <xsl:variable name="varShipAlone">
            <xsl:choose>
                <xsl:when test="$varHazmatClass='C1'">
                    <xsl:value-of select="'N'" />
                </xsl:when>
				<xsl:when test="$varExtnIsHazmat='Y'"> <!--As a part of WN-214, WN-216, WN-217, WN-211, WN-419 Hazmat Implementation -->
                    <xsl:value-of select="'Y'" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="normalize-space(./@ExtnShipAlone)" />
                </xsl:otherwise>            
            </xsl:choose>
        </xsl:variable>
        <!--End of changes for STL-741-->
        
      
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
         <xsl:value-of select="'DEFAULT'" />
      </xsl:variable>

      <xsl:variable name="varNo">
         <xsl:value-of select="'N'" />
      </xsl:variable>
      
      <xsl:variable name="varEASTag">
            <xsl:value-of select="./@ExtnEASTag" />
      </xsl:variable>
	  <!--Added for DSV-->
	  	  <xsl:variable name="varEcommerceFlag">
         <xsl:value-of select="./@ExtnEcommerceCode" />
      </xsl:variable>



<!-- End of Variable definition -->
      <xsl:for-each select="/Items/Item">
         <Item>
            <xsl:variable name="countAssociations">
               <xsl:value-of select="count(./Associations/Association)" />
            </xsl:variable>
	    <xsl:copy-of select="./@ItemID" />
	      <xsl:variable name="ITEM_ID">
               <xsl:value-of select="./@ItemID" />
            </xsl:variable>

            

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

            <xsl:attribute name="GlobalItemID">
               <xsl:value-of select="./@GTIN" />
            </xsl:attribute>

            <xsl:attribute name="SyncTS">
               <xsl:value-of select="./@SyncTS" />
            </xsl:attribute>

            <PrimaryInformation>
               <xsl:attribute name="Status">
                  <xsl:choose>
                     <xsl:when test="normalize-space(./@ExtnEcommerceCode)='05'">
                        <xsl:value-of select="$varHeldStatus" />
                     </xsl:when>

                     <xsl:otherwise>
                        <xsl:value-of select="$varPublishedStatus" />
                     </xsl:otherwise>
                  </xsl:choose>
               </xsl:attribute>
               
               <!-- Start OMNI-13566 Update Item as Infinite if it is eGC Item -->
               <xsl:variable name="vareGiftCardFlag">
         				<xsl:value-of select="./@ExtneGiftCardFlag" />
      		   </xsl:variable>
      				
                <xsl:if test="($vareGiftCardFlag='Y') or ($vareGiftCardFlag='N')">
	               	<xsl:attribute name="AssumeInfiniteInventory">
	               		<xsl:choose>                     
	                     <xsl:when test="($vareGiftCardFlag='Y')" >
	                        	<xsl:value-of select="'Y'" />
	                     </xsl:when>
						 <xsl:otherwise  >
	                        <xsl:value-of select="'N'" />
	                     </xsl:otherwise>
	                  </xsl:choose>
	               	</xsl:attribute>
               </xsl:if>
                <!-- End OMNI-13566 Update Item as Infinite if it is eGC Item -->

               <xsl:attribute name="ProductLine">
                  <xsl:value-of select="'AcademyItemAttributes'" />
               </xsl:attribute>
			   <xsl:attribute name="ImageID">
                  <xsl:value-of select="./PrimaryInformation/@ImageID" />
               </xsl:attribute>
			   <xsl:attribute name="ImageLocation">
                  <xsl:value-of select="./PrimaryInformation/@ImageLocation" />
               </xsl:attribute>

               <xsl:attribute name="CountryOfOrigin">
                  <xsl:value-of select="./@ExtnCountryOfOriginDescription" />
               </xsl:attribute>

               <xsl:attribute name="DefaultProductClass">
                  <xsl:value-of select="'GOOD'" />
               </xsl:attribute>

               <xsl:attribute name="ItemType">
                  <xsl:choose>
                     <xsl:when test="($varHighValue='Y')">
                        <xsl:choose>
                           <xsl:when test="($varShipAlone='Y')">
                              <xsl:value-of select="'SAHV'" />
                           </xsl:when>

                           <xsl:otherwise>
                              <xsl:value-of select="'NSAHV'" />
                           </xsl:otherwise>
                        </xsl:choose>
                     </xsl:when>

                     <xsl:otherwise>
                        <xsl:choose>
                           <xsl:when test="($varShipAlone='Y')">
                              <xsl:value-of select="'SANHV'" />
                           </xsl:when>

                           <xsl:otherwise>
                              <xsl:value-of select="'NSANHV'" />
                           </xsl:otherwise>
                        </xsl:choose>
                     </xsl:otherwise>
                  </xsl:choose>
               </xsl:attribute>

               <xsl:attribute name="Description">
                  <xsl:value-of select="./@Description" />
               </xsl:attribute>

               <xsl:attribute name="ShortDescription">
                  <xsl:value-of select="substring(./@Description, 1, 100)" />
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

<!-- start changes:CR#34 -->
               <!-- <xsl:attribute name="ModelItemUnitOfMeasure">
                  <xsl:value-of select="./@ModelItemUnitOfMeasure" />
               </xsl:attribute> -->
            
            <xsl:attribute name="ModelItemUnitOfMeasure">EACH</xsl:attribute>
<!-- end changes -->

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

        <!-- <xsl:attribute name="UnitHeight">
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
               </xsl:attribute>   -->       

<!--            Since hazmat is OOS for BR1     
            <xsl:attribute name="IsHazmat">
                  <xsl:if test="string-length(normalize-space(./@HazmatClass)) &gt; 0">
                     <xsl:value-of select="'Y'" />
                  </xsl:if>
               </xsl:attribute> -->
                
                <!--Start of changes for STL-741-->
                <xsl:attribute name="IsHazmat">
                    <xsl:value-of select="$varIsHazmat" />
                </xsl:attribute>
                <!--End of changes for STL-741-->
                

               <xsl:attribute name="SizeCode">
                  <xsl:value-of select="/Items/Item/@SizeCode" />
               </xsl:attribute>

               <xsl:attribute name="ColorCode">
                  <xsl:value-of select="/Items/Item/@ColorCode" />
               </xsl:attribute>

               <xsl:attribute name="KitCode">
                  <xsl:choose>
                     <xsl:when test="normalize-space(./@ExtnKit)='Y'">
                        <xsl:value-of select="'BUNDLE'" />
                     </xsl:when>
                  </xsl:choose>
               </xsl:attribute>

               <xsl:attribute name="SerializedFlag">
                  <xsl:value-of select="./@IsSerialTracked" />
               </xsl:attribute>
               
               <!-- Attribute ReturnShippingLabelLevel added for Enh 9056 -->
               <!-- EFP -8: Return Label Print Printing for all items except Hazmat Class (C1)-->
			   <!-- <xsl:attribute name="ReturnShippingLabelLevel">
                  <xsl:choose>                     
                     <xsl:when test="$varConveyable='Y'"> -->
                        <!--Fix reverted back of Enh 9056. return Labels not required by Academy 
							<xsl:value-of select="'N'" />
                     </xsl:when>
                     <xsl:otherwise>
                        <xsl:value-of select="'N'" />
                     </xsl:otherwise>
                  </xsl:choose>
               </xsl:attribute> -->
			   <xsl:attribute name="IsReturnable">
                  <xsl:choose>                     
                     <xsl:when test="starts-with($varHazmatClass, 'A')" >
                        	<xsl:value-of select="'N'" />
                     </xsl:when>
					 <xsl:when test="starts-with($varHazmatClass, 'B')" >
                        	<xsl:value-of select="'N'" />
                     </xsl:when>
					 <xsl:when test="starts-with($varHazmatClass, 'C')" >
                        	<xsl:value-of select="'N'" />
                     </xsl:when>
					 <xsl:otherwise>
                        <xsl:value-of select="'Y'" />
                     </xsl:otherwise>
                  </xsl:choose>
               </xsl:attribute>
			   <xsl:attribute name="ReturnShippingLabelLevel">
                  <xsl:choose>                     
                     <xsl:when test="starts-with($varHazmatClass, 'A')" >
                        	<xsl:value-of select="'N'" />
                     </xsl:when>
					 <xsl:when test="starts-with($varHazmatClass, 'B')" >
                        	<xsl:value-of select="'N'" />
                     </xsl:when>
					 <xsl:when test="starts-with($varHazmatClass, 'C')" >
                        	<xsl:value-of select="'N'" />
                     </xsl:when>
                     <xsl:otherwise>
                        <xsl:value-of select="'P'" />
                     </xsl:otherwise>
                  </xsl:choose>
               </xsl:attribute>
			   <!-- EFP -8: Return Label Print Printing for all items except Hazmat Class (C1)-->
				<!-- start changes:Item Feed Changes for BOPIS Implementation -->

				<xsl:attribute name="IsPickupAllowed">
                  <xsl:value-of select="./@IsPickupAllowed" />
               </xsl:attribute>
		<!-- End changes:Item Feed Changes for BOPIS Implementation -->	   
            </PrimaryInformation>
<!-- start changes:CR#34 -->
            <AdditionalAttributeList>
            <xsl:for-each select="./ItemRestrictions/ItemRestriction">
                  <AdditionalAttribute>
                     <xsl:attribute name="Name">
                        <xsl:value-of select="concat('ItemRestrictionState', position())" />
                     </xsl:attribute>
 
                     <xsl:attribute name="Value">
                        <xsl:value-of select="./@ExtnItemRestrictionState" />
                     </xsl:attribute>
					 
					  <xsl:attribute name="IntegerValue">
						<xsl:value-of select="0" />
					 </xsl:attribute>
                  </AdditionalAttribute>
               </xsl:for-each>
		  <!-- start changes:Item Feed Changes for BOPIS Implementation -->
			 <xsl:for-each select="./AdditionalAttributeList/AdditionalAttribute">
                  <AdditionalAttribute>
                     <xsl:copy-of select="@*" />
                  </AdditionalAttribute>
               </xsl:for-each>  
		<!-- End changes:Item Feed Changes for BOPIS Implementation -->
            </AdditionalAttributeList>
<!-- end changes -->
  <!-- start changes:Item Feed Changes for BOPIS Implementation -->
  <SafetyFactorDefinitions>
   <xsl:attribute name="Reset">
                       <xsl:value-of select="$resetValue" />
                     </xsl:attribute>
   <xsl:for-each select="./SafetyFactorDefinitions/SafetyFactorDefinition">
                  <SafetyFactorDefinition>
                     <xsl:copy-of select="@*" />
                  </SafetyFactorDefinition>
               </xsl:for-each>  
  </SafetyFactorDefinitions>
    
<!-- End changes:Item Feed Changes for BOPIS Implementation -->



<!-- STL 1288 Changes : START-->
            <!-- <InventoryParamters>
               <xsl:attribute name="IsFifoTracked">
                  <xsl:value-of select="'Y'" />
               </xsl:attribute>
            </InventoryParamters> -->
<!-- STL 1288 Changes : END-->
            <ClassificationCodes>
<!-- start changes:CR#34 Removed the field VelocityCode  -->
              <!-- <xsl:attribute name="VelocityCode">
                  <xsl:value-of select="./@Velocity" />
               </xsl:attribute> -->
<!-- end changes -->

                <!--Start of changes for STL-741-->
                <xsl:attribute name="HazmatClass">
                    <xsl:value-of select="$varHazmatClass" />
                </xsl:attribute>
                <!--End of changes for STL-741-->

               <xsl:attribute name="Model">
                  <xsl:value-of select="./@Model" />
               </xsl:attribute>

<!-- start changes:CR#34 added attribute -->
               <xsl:attribute name="TaxProductCode">
                  <xsl:value-of select="./@TaxProductCode" />
               </xsl:attribute>

              <xsl:attribute name="NMFCClass">
                  <xsl:value-of select="./@NMFCClass" />
               </xsl:attribute>

              <xsl:attribute name="NMFCCode">
                  <xsl:value-of select="./@NMFCCode" />
               </xsl:attribute>
<!-- end changes -->
               <xsl:attribute name="StorageType">
<!-- start changes for DSV -->
                <xsl:choose>
                   <xsl:when test="($varEcommerceFlag='04')">
                        <xsl:value-of select="'DSV'" />
                   </xsl:when> 
                <xsl:otherwise>
                  <xsl:choose>
                     <xsl:when test="($varConveyable='Y')">
                        <xsl:choose>
                           <xsl:when test="($varGiftCard='Y')">
                              <xsl:value-of select="'CONGC'" />
                           </xsl:when>

                           <xsl:otherwise>
                              <xsl:choose>
                                 <xsl:when test="($varLabelPick='Y')">
                                    <xsl:value-of select="'CONLP'" />
                                 </xsl:when>

                                 <xsl:otherwise>
                                    <xsl:if test="($varItemDivision='05')">
                                       <xsl:value-of select="'CONFW'" />
                                    </xsl:if>

                                    <xsl:if test="($varItemDivision='01')">
                                       <xsl:value-of select="'CONHG'" />
                                    </xsl:if>

                                    <xsl:if test="($varItemDivision='02')">
                                       <xsl:value-of select="'CONHG'" />
                                    </xsl:if>

                                    <xsl:if test="($varItemDivision='06')">
                                       <xsl:value-of select="'CONSG'" />
                                    </xsl:if>
                                 </xsl:otherwise>
                              </xsl:choose>
                           </xsl:otherwise>
                        </xsl:choose>
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
                </xsl:otherwise>
               </xsl:choose>
                </xsl:attribute>
            </ClassificationCodes>

            <!-- AlternateUOMList Added for non kit bulk items as part of enhancement 9057 -->
            
            <!--<xsl:if test="normalize-space(./@ExtnConveyable)='N'">-->
            <!--                
                normalize-space(./@ExtnConveyable) replaced with $varConveyable
                normalize-space(./@ExtnShipAlone) replaced with $varShipAlone
                in the below condition as  part of STL-741
            -->
            <xsl:if test="$varConveyable='N' and $varShipAlone='Y'">
               <AlternateUOMList>
                  <AlternateUOM>
                     <xsl:attribute name="UnitOfMeasure">
                        <xsl:value-of select="'FCASE'" />
                     </xsl:attribute>
                     <xsl:attribute name="Quantity">
                        <xsl:value-of select="'1'" />
                     </xsl:attribute>
                     <!-- Start of Fix done as part of Bugzilla Issue# 11897 -->
                     <xsl:attribute name="Height">
                          <xsl:value-of select="./@UnitHeight" />
                         </xsl:attribute>
                      <xsl:attribute name="HeightUOM">
                         <xsl:value-of select="./@UnitHeightUOM" />
                         </xsl:attribute>
                         <xsl:attribute name="Length">
                        <xsl:value-of select="./@UnitLength" />
                         </xsl:attribute>
                         <xsl:attribute name="LengthUOM">
                         <xsl:value-of select="./@UnitLengthUOM" />
                         </xsl:attribute>
                         <xsl:attribute name="Weight">
                          <xsl:value-of select="./@UnitWeight" />
                         </xsl:attribute>
                         <xsl:attribute name="WeightUOM">
                          <xsl:value-of select="./@UnitWeightUOM" />
                         </xsl:attribute>
                         <xsl:attribute name="Width">
                         <xsl:value-of select="./@UnitWidth" />
                         </xsl:attribute>
                         <xsl:attribute name="WidthUOM">
                        <xsl:value-of select="./@UnitWidthUOM" />
                         </xsl:attribute>
                        <!-- End of Fix done as part of Bugzilla Issue# 11897 -->
                  </AlternateUOM>
               </AlternateUOMList>
            </xsl:if>
         <!-- AlternateUOMList Added for non kit bulk items as part of enhancement 9057 -->

         <!-- Fix for Defect #4172, When item type is changed from bulk to conveyable, reset AlternateUMOList -->
         <!--normalize-space(./@ExtnConveyable) replaced with $varConveyable as part of STL-741-->
         <xsl:if test="$varConveyable='Y'">
            <AlternateUOMList>
                   <xsl:attribute name="Reset">
                       <xsl:value-of select="$resetValue" />
                     </xsl:attribute>
            </AlternateUOMList>
         </xsl:if>

            <xsl:if test="string-length(./@ExtnInstructionsForShipping) &gt; 0 or string-length(./@ExtnShippingRestrictions) &gt; 0 or ($varEASTag='Y')">
               <ItemInstructionList>
                  <xsl:attribute name="Reset">
                     <xsl:value-of select="$resetValue" />
                  </xsl:attribute>

                  <xsl:if test="string-length(./@ExtnInstructionsForShipping) &gt; 0">
                     <ItemInstruction>
                        <xsl:attribute name="InstructionType">
                           <xsl:value-of select="'SHIPPING'" />
                        </xsl:attribute>

                        <xsl:attribute name="InstructionText">
                           <xsl:value-of select="./@ExtnInstructionsForShipping" />
                        </xsl:attribute>

                        <xsl:attribute name="SeqNo">
                           <xsl:value-of select="'1'" />
                        </xsl:attribute>
                     </ItemInstruction>
                  </xsl:if>

                  <xsl:if test="string-length(./@ExtnShippingRestrictions) &gt; 0">
                     <ItemInstruction>
                        <xsl:attribute name="InstructionType">
                           <xsl:value-of select="'CARRIER'" />
                        </xsl:attribute>

                        <xsl:attribute name="InstructionText">
                           <xsl:value-of select="./@ExtnShippingRestrictions" />
                        </xsl:attribute>

                        <xsl:attribute name="SeqNo">
                           <xsl:value-of select="'2'" />
                        </xsl:attribute>
                     </ItemInstruction>
                  </xsl:if>
                  <xsl:if test="($varEASTag='Y')">
                     <ItemInstruction>
                     <xsl:attribute name="InstructionType">
                        <xsl:value-of select="'PACK'" />
                     </xsl:attribute>

                     <xsl:attribute name="InstructionText">
                        <xsl:value-of select="'Remove Tag Before Adding To Shipping Container'" />
                     </xsl:attribute>

                     <xsl:attribute name="SeqNo">
                        <xsl:value-of select="'3'" />
                     </xsl:attribute>
                     </ItemInstruction>
                  </xsl:if>
               </ItemInstructionList>
            </xsl:if>
            
            <ItemAliasList>
               <xsl:attribute name="Reset">
                  <xsl:value-of select="$resetValue" />
               </xsl:attribute>

               <xsl:for-each select="./Aliases/Alias">
                  <ItemAlias>
                     <xsl:copy-of select="@*" />
                  </ItemAlias>
               </xsl:for-each>
            </ItemAliasList>

         <!--xsl:if test="($countAssociations &gt; 0)">
               <AssociationList>
                  <xsl:copy-of select="./Associations/Association/." />
               </AssociationList>
            </xsl:if-->
            
            <!-- Fix for Bug 8372 -->
            <xsl:if test="($countAssociations &gt; 0)">
               <AssociationList>
                  
                     <xsl:for-each select="./Associations/Association">
                        <Association>
                           <xsl:attribute name="AssociateItemID">
                              <xsl:value-of select="./@AssociateItemID" />
                           </xsl:attribute>
      
                           <xsl:attribute name="AssociationType">
                              <xsl:if test="normalize-space(./@AssociationType)='Cross-Sell'">
                                 <xsl:value-of select="'CrossSell'" />
                              </xsl:if>
      
                              <xsl:if test="normalize-space(./@AssociationType)='Up-Sell'">
                                 <xsl:value-of select="'UpSell'" />
                              </xsl:if>
                           </xsl:attribute>
                           </Association>
                     </xsl:for-each>
                  
               </AssociationList>
            </xsl:if>

            <Extn>

<!-- start changes:CR#34 -->        
            <!-- <xsl:attribute name="ExtnHighValue">
                  <xsl:value-of select="./@ExtnHighValue" />
               </xsl:attribute> -->
            
         <xsl:attribute name="ExtnHighValue">N</xsl:attribute>
<!-- end changes -->   

                <!--Start of changes for STL-741-->
                <xsl:attribute name="ExtnShipAlone">
                    <xsl:value-of select="$varShipAlone" />
                </xsl:attribute>
                <xsl:attribute name="ExtnConveyable">
                    <xsl:value-of select="$varConveyable" />                    
                </xsl:attribute>
                <!--End of changes for STL-741-->
				
				 <!--Start changes made for STL-934(Checkout Funnel)-->
		
				<xsl:choose>			
                  <xsl:when test="$varConveyable='N' and $varShipAlone='N' and $varExtnWhiteGloveEligible='N'">
				  	<xsl:attribute name="ExtnIsContSplitAllowed">
                     <xsl:value-of select="'Y'" />  
					    </xsl:attribute>
                   </xsl:when>					 
				  <xsl:otherwise>				   
                       <xsl:attribute name="ExtnIsContSplitAllowed">
                     <xsl:value-of select="'N'" />  
					    </xsl:attribute>                  			  
				   </xsl:otherwise>    
			     </xsl:choose>
				 <!--End changes made for STL-934(Checkout Funnel)-->

               <xsl:attribute name="ExtnKit">
                  <xsl:value-of select="./@ExtnKit" />
               </xsl:attribute>

<!--                <xsl:if test="string-length(normalize-space(./@ExtnHazmatName)) &gt; 0">
                  <xsl:attribute name="ExtnHazmatName">
                     <xsl:value-of select="./@ExtnHazmatName" />
                  </xsl:attribute>
               </xsl:if>
 -->
               <xsl:attribute name="ExtnWhiteGloveEligible">
                  <xsl:value-of select="$varExtnWhiteGloveEligible" />
               </xsl:attribute>

<!-- start changes:CR#34 Remove the attribute ExtnStreetDate-->
               <!-- <xsl:attribute name="ExtnStreetDate">
                  <xsl:value-of select="./@ExtnStreetDate" />
               </xsl:attribute> -->
<!-- end changes -->

               <xsl:attribute name="ExtnIsGiftCard">
                  <xsl:value-of select="./@ExtnIsGiftCard" />
               </xsl:attribute>

               <xsl:attribute name="ExtnLabelPick">
                  <xsl:value-of select="./@ExtnLabelPick" />
               </xsl:attribute>

               <xsl:attribute name="ExtnECommerceFlag">
                  <xsl:value-of select="./@ExtnEcommerceCode" />
               </xsl:attribute>

               <xsl:attribute name="ExtnEcommerceDescription">
                  <xsl:value-of select="./@ExtnEcommerceDescription" />
               </xsl:attribute>

               <xsl:attribute name="ExtnDropShipFlag">
                  <xsl:choose>
                     <xsl:when test="normalize-space(./@ExtnEcommerceCode)='04'">
                        <xsl:value-of select="'Y'" />
                     </xsl:when>
                  </xsl:choose>
               </xsl:attribute>

<!-- start changes:CR#34 removing ExtnQuantityRestrictions field -->
               <!-- <xsl:attribute name="ExtnQuantityRestrictions">
                  <xsl:value-of select="./@ExtnQuantityRestriction" />
               </xsl:attribute> -->
<!-- end changes -->
               <xsl:attribute name="ExtnEASTag">
                  <xsl:value-of select="./@ExtnEASTag" />
               </xsl:attribute>

<!-- start changes:CR#34 remving the fields ExtnRebate, ExtnRebateStartDate, ExtnRebateEndDate -->
            <!--    <xsl:attribute name="ExtnRebate">
                  <xsl:value-of select="./@ExtnRebate" />
               </xsl:attribute>

               <xsl:attribute name="ExtnRebateStartDate">
                  <xsl:value-of select="./@ExtnRebateStartDate" />
               </xsl:attribute>

               <xsl:attribute name="ExtnRebateEndDate">
                  <xsl:value-of select="./@ExtnRebateEndDate" />
               </xsl:attribute> -->
<!-- End changes -->

<!-- start changes for bug id:- 10365 -->
            <xsl:attribute name="ExtnIPItemDescription">
                  <xsl:value-of select="./@ExtnIPItemDescription" />
               </xsl:attribute>
<!-- End changes -->
               <xsl:attribute name="ExtnItemDivision">
                  <xsl:value-of select="./@ExtnItemDivision" />
               </xsl:attribute>

               <xsl:attribute name="ExtnItemDivisionName">
                  <xsl:value-of select="./@ExtnItemDivisionName" />
               </xsl:attribute>

               <xsl:attribute name="ExtnCountryOfOriginDescription">
                  <xsl:value-of select="./@ExtnCountryOfOriginDescription" />
               </xsl:attribute>

               <xsl:attribute name="ExtnSizeCodeDescription">
                  <xsl:value-of select="./@ExtnSizeCodeDescription" />
               </xsl:attribute>

               <xsl:attribute name="ExtnColorCodeDescription">
                  <xsl:value-of select="./@ExtnColorCodeDescription" />
               </xsl:attribute>
         <!-- Start for the fix Academy Bugzilla 652. ExtnVendorColorName truncated to 40 char  -->
               <xsl:attribute name="ExtnVendorColorName">
                  <xsl:value-of select="substring(./@ExtnVendorColorName, 1, 40)" />
               </xsl:attribute>
               
               <!-- start changes for bug id:- 2724 -->
               <xsl:attribute name="ExtnClass">
                  <xsl:value-of select="./@ExtnClass" />
               </xsl:attribute>
               <xsl:attribute name="ExtnClassDescription">
                  <xsl:value-of select="./@ExtnClassDescription" />
               </xsl:attribute>
               <xsl:attribute name="ExtnColorDescription">
                  <xsl:value-of select="./@ExtnColorDescription" />
               </xsl:attribute>
               <xsl:attribute name="ExtnDepartment">
                  <xsl:value-of select="./@ExtnDepartment" />
               </xsl:attribute>
               <xsl:attribute name="ExtnDepartmentName">
                  <xsl:value-of select="./@ExtnDepartmentName" />
               </xsl:attribute>
               <xsl:attribute name="ExtnEcommerceCode">
                  <xsl:value-of select="./@ExtnEcommerceCode" />
               </xsl:attribute>
               <xsl:attribute name="ExtnFAKClass">
                  <xsl:value-of select="./@ExtnFAKClass" />
               </xsl:attribute>
               <xsl:attribute name="ExtnHazmatName">
                  <xsl:value-of select="./@ExtnHazmatName" />
               </xsl:attribute>
               <xsl:attribute name="ExtnInstructionsForShipping">
                  <xsl:value-of select="./@ExtnInstructionsForShipping" />
               </xsl:attribute>
               <xsl:attribute name="ExtnNMFCDescription">
                  <xsl:value-of select="./@ExtnNMFCDescription" />
               </xsl:attribute>
               <xsl:attribute name="ExtnShippingRestrictions">
                  <xsl:value-of select="./@ExtnShippingRestrictions" />
               </xsl:attribute>
               <xsl:attribute name="ExtnStyle">
                  <xsl:value-of select="./@ExtnStyle" />
               </xsl:attribute>
               <xsl:attribute name="ExtnTaxCategory">
                  <xsl:value-of select="./@ExtnTaxCategory" />
               </xsl:attribute>
               <xsl:attribute name="ExtnVendor">
                  <xsl:value-of select="./@ExtnVendor" />
               </xsl:attribute>
               <!-- End changes: 2724 -->
			   
			   <!--START: SHIN-5-->
			   <xsl:if test="@ExtnShpFromDC!=''">	
			   <xsl:attribute name="ExtnShpFromDC">
                  <xsl:value-of select="./@ExtnShpFromDC" />
			   </xsl:attribute> 
			   </xsl:if>
			   <xsl:if test="@ExtnShipFromDQ!=''">	
			   <xsl:attribute name="ExtnShipFromDQ">
                  <xsl:value-of select="./@ExtnShipFromDQ" />
			   </xsl:attribute> 
			   </xsl:if>

		       <!--END: SHIN-5-->    
			   
               <!-- Start changes: SFS -->
               
               <xsl:attribute name="ExtnShipFromStoreItem">
                  <xsl:value-of select="./@ExtnShipFromStoreItem" />
               </xsl:attribute>
               <xsl:attribute name="ExtnItemImageUrl">
                  <xsl:value-of select="./@ExtnItemImageUrl" />
               </xsl:attribute>
			   
			   <!-- Variable for storing the filename of the URL(@ExtnItemImageUrl) -->
               <xsl:variable name="ImageFileName">
               		<xsl:call-template name="getFileName">
               			<xsl:with-param name="filename" select="./@ExtnItemImageUrl"/>
               			<xsl:with-param name="separator" select="'/'"/>
               		</xsl:call-template>
               </xsl:variable>
               <xsl:attribute name="ExtnItemImageName">
                  <xsl:value-of select="$ImageFileName" />
               </xsl:attribute>
               <!-- End changes: SFS -->
               <!-- OMNI-6614: STS Start-->
			   <xsl:if test="@ExtnShipToStoreItem!=''">
				   <xsl:attribute name="ExtnShipToStoreItem">
					  <xsl:value-of select="./@ExtnShipToStoreItem" />
				   </xsl:attribute>
			   </xsl:if>
	       <!-- OMNI-6614: STS End-->
             <!-- OMNI-61443: STS3 Start-->
               <xsl:attribute name="ExtnSTSPrimarySource">
                  <xsl:value-of select="./@ExtnSTSPrimarySource" />
               </xsl:attribute>	    
	       <!-- OMNI-61443: STS3 End-->
               <!-- Variable for Storing the Loftware path - starts //Modified by Karthik  --> 
               <xsl:variable name="ITEM_IMAGE_URL">
                  <xsl:value-of select="./@ExtnItemImageUrl" />
               </xsl:variable>			   
               
               <xsl:choose>
					<xsl:when test="(normalize-space($ITEM_IMAGE_URL) != '')">	
					
					<!-- Start changes: Adobe 7 Migration -->
                    <!-- <xsl:variable name="URL_AFTER_DOT_COM">
                        <xsl:value-of select="(substring-after(substring-after($ITEM_IMAGE_URL,'.com'),'/'))"/>
                     </xsl:variable>
					 <xsl:variable name="URL_AFTER_SLASH">
					 <xsl:value-of select="(substring-after($URL_AFTER_DOT_COM,'/'))"/>
					 </xsl:variable>						 
                     <xsl:variable name="IMAGE_URL">
                        <xsl:value-of select="(substring-before($URL_AFTER_SLASH,'/'))"/>
                     </xsl:variable> -->       

					<xsl:attribute name="ExtnItemImageName">
						<xsl:value-of select="concat($ImageFileName,'.jpg')" />
					</xsl:attribute>
					
					<xsl:variable name="URL_AFTER_ACADEMY">
						<xsl:value-of select="(substring-after($ITEM_IMAGE_URL,'academy/'))"/>
					</xsl:variable>
					
					<xsl:variable name="IMAGE_URL">
						<xsl:value-of select="(substring($URL_AFTER_ACADEMY, string-length($URL_AFTER_ACADEMY)-1 ))"/>
					</xsl:variable>
					<!-- End changes: Adobe 7 Migration -->
					
                     <xsl:attribute name="ExtnImageLocalPath">
                        <xsl:value-of select="concat(concat(concat(@ExtnImageLocalPath,'/'),$IMAGE_URL),'/')"/>
                     </xsl:attribute> 
                  </xsl:when>	
				  
                  <xsl:otherwise>
					
					<!-- Start changes: Adobe 7 Migration -->
					<xsl:attribute name="ExtnItemImageName">
						  <xsl:value-of select="''" />
					</xsl:attribute>
					<!-- End changes: Adobe 7 Migration -->
					
					 <xsl:variable name="IMAGE_NAME">
                        <xsl:value-of select="./@ExtnTempImage" />
                     </xsl:variable>
                     <xsl:variable name="FILE_NAME">
                        <xsl:value-of select="concat('/',$IMAGE_NAME)" />
                     </xsl:variable>		
                     <xsl:attribute name="ExtnImageLocalPath">
                        <xsl:value-of select="concat(@ExtnImageLocalPath,$FILE_NAME)"/>
                     </xsl:attribute>			
                  </xsl:otherwise>
               </xsl:choose>
               <!-- Variable for Storing the Loftware path - Ends //Modified by Karthik  -->    
              
	       <!-- Start OMNI-13566 Update the Flag From PIM for eGC Items -->
               <xsl:attribute name="ExtneGiftCardFlag">
	            <xsl:value-of select="./@ExtneGiftCardFlag" />
	       </xsl:attribute>
	       <!-- End OMNI-13566 Update the Flag From PIM for eGC Items -->
               
            </Extn>
            
            <!-- Start changes: SFS -->
                        
            <InventoryParameters>            
               <xsl:choose>
                  <xsl:when test="normalize-space(./@ExtnShipFromStoreItem)='Y'">
                     <xsl:attribute name="OnhandSafetyFactorQuantity">
                        <xsl:value-of select="./@OnhandSafetyFactorQuantity" />
                     </xsl:attribute>
                     <xsl:attribute name="OnhandSafetyFactorPercentage">
                        <xsl:value-of select="''" />
                     </xsl:attribute>
                  </xsl:when>
                  <xsl:otherwise>
                     <xsl:attribute name="OnhandSafetyFactorQuantity">
                        <xsl:value-of select="''" />
                     </xsl:attribute>
                     <xsl:attribute name="OnhandSafetyFactorPercentage">
                        <xsl:value-of select="'100'" />
                     </xsl:attribute>
                  </xsl:otherwise>
               </xsl:choose>
	       <xsl:attribute name="ATPRule">
                        <xsl:value-of select="'Academy ATP Rule'" />
                     </xsl:attribute>
                     <!-- jira 1141 -->
                     <xsl:if test="./@IsModelItem = 'Y'">
						<xsl:attribute name="InventoryMonitorRule"></xsl:attribute>
					 </xsl:if>
					 <!-- Begin: OMNI-35058 - Extra condition added to stamp Inventory Monitor Rule if item is not model and eGiftCard item -->
					 <xsl:if test="(./@IsModelItem = 'N') and (./@ExtneGiftCardFlag != 'Y')">
					 <!-- End: OMNI-35058 - Extra condition added to stamp Inventory Monitor Rule if item is not model and eGiftCard item -->
                     <xsl:attribute name="InventoryMonitorRule">
                        <xsl:value-of select="'Academy_ATP_Monitor'" />
                     </xsl:attribute>
					 <xsl:attribute name="NodeLevelInventoryMonitorRule">
					  <!-- Start changes for STL1482 -->
					 <xsl:value-of select="'Academy_ATP_Monitor'" />
					  <!-- End changes for STL1482 -->
					 </xsl:attribute> 
					 </xsl:if>
					 <!-- end of jira 1141 -->
            </InventoryParameters>

            <!-- End changes: SFS -->
            
         </Item>
      </xsl:for-each>
   </xsl:template>
   
   <!-- Start changes: SFS -->
   <!-- For extracting the filename from the URL(@ExtnItemImageUrl) -->
   <xsl:template name="getFileName">
   	<xsl:param name="filename"/>
   	<xsl:param name="separator"/>
   	<xsl:choose>
   		<xsl:when test="contains($filename, $separator)">
   			<xsl:call-template name="getFileName">
               	<xsl:with-param name="filename" select="substring-after($filename,$separator)"/>
               	<xsl:with-param name="separator" select="$separator"/>
            </xsl:call-template>
   		</xsl:when>
   		<xsl:otherwise>
   			<xsl:value-of select="$filename"/>
   		</xsl:otherwise>
   	</xsl:choose>
   </xsl:template>
   <!-- End changes: SFS -->
   
</xsl:stylesheet><!-- Stylus Studio meta-information - (c) 2004-2008. Progress Software Corporation. All rights reserved.

<metaInformation>
	<scenarios>
		<scenario default="yes" name="Scenario1" userelativepaths="yes" externalpreview="no" url="PIM_IP_UAT2.xml" htmlbaseurl="" outputurl="" processortype="saxon8" useresolver="no" profilemode="0" profiledepth="" profilelength="" urlprofilexml=""
		          commandline="" additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext="" validateoutput="no" validator="internal" customvalidator="">
			<advancedProp name="sInitialMode" value=""/>
			<advancedProp name="bXsltOneIsOkay" value="true"/>
			<advancedProp name="bSchemaAware" value="false"/>
			<advancedProp name="bXml11" value="false"/>
			<advancedProp name="iValidation" value="0"/>
			<advancedProp name="bExtensions" value="true"/>
			<advancedProp name="iWhitespace" value="0"/>
			<advancedProp name="sInitialTemplate" value=""/>
			<advancedProp name="bTinyTree" value="true"/>
			<advancedProp name="bWarnings" value="true"/>
			<advancedProp name="bUseDTD" value="false"/>
			<advancedProp name="iErrorHandling" value="fatal"/>
		</scenario>
	</scenarios>
	<MapperMetaTag>
		<MapperInfo srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/>
		<MapperBlockPosition></MapperBlockPosition>
		<TemplateContext></TemplateContext>
		<MapperFilter side="source"></MapperFilter>
	</MapperMetaTag>
</metaInformation>
-->