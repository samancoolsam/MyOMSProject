<?xml version='1.0' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<Order>
			<xsl:copy-of select="AcademyMergedDocument/EnvironmentDocument/Order/@*"/>
			<xsl:attribute name="DocumentType"><xsl:text>0001</xsl:text></xsl:attribute>
			<xsl:attribute name="EnterpriseCode"><xsl:text>Academy_Direct</xsl:text></xsl:attribute>
			<xsl:attribute name="SellerOrganizationCode"><xsl:text>Academy_Direct</xsl:text></xsl:attribute>
			<xsl:attribute name="PaymentStatus"><xsl:text>AUTHORIZED</xsl:text></xsl:attribute>
			<xsl:attribute name="BillToID">
				<xsl:value-of select="AcademyMergedDocument/InputDocument/CustomerList/Customer/@CustomerID"/>
			</xsl:attribute>
			
			<OrderHoldTypes>
			<!-- KER-12036 : Payment Migration Changes to support new Payment Type -->
			<xsl:if test="/AcademyMergedDocument/EnvironmentDocument/Order/@ExtnFraudCheck='Challenge'
			and ((count(AcademyMergedDocument/EnvironmentDocument/Order/PaymentMethods/PaymentMethod[@PaymentType='CREDIT_CARD']) &gt; 0) 
			or (count(AcademyMergedDocument/EnvironmentDocument/Order/PaymentMethods/PaymentMethod[@PaymentType='PayPal' or @PaymentType='Paypal']) &gt; 0) 
			or (count(AcademyMergedDocument/EnvironmentDocument/Order/PaymentMethods/PaymentMethod[@PaymentType='GIFT_CARD']) &gt; 0)
			or (count(AcademyMergedDocument/EnvironmentDocument/Order/PaymentMethods/PaymentMethod[@PaymentType='Credit_Card']) &gt; 0) 
			or (count(AcademyMergedDocument/EnvironmentDocument/Order/PaymentMethods/PaymentMethod[@PaymentType='Paypal']) &gt; 0) 
			or (count(AcademyMergedDocument/EnvironmentDocument/Order/PaymentMethods/PaymentMethod[@PaymentType='Apple_Pay']) &gt; 0)
			or (count(AcademyMergedDocument/EnvironmentDocument/Order/PaymentMethods/PaymentMethod[@PaymentType='Google_Pay']) &gt; 0) )">
				<OrderHoldType>
				  	<xsl:attribute name="HoldType"><xsl:text>ACADEMY_AWAIT_FRAUD</xsl:text></xsl:attribute>
					<xsl:attribute name="ReasonText"><xsl:text>Awaiting Fraud Checking</xsl:text></xsl:attribute>		          
                </OrderHoldType>
            </xsl:if>				  				  
				  
				   <xsl:if test="/AcademyMergedDocument/EnvironmentDocument/Order/@ExtnFraudCheck='Challenge_nored'">
				<OrderHoldType>
				  	<xsl:attribute name="HoldType"><xsl:text>ACADEMY_NO_RED_CHECK</xsl:text></xsl:attribute>
					<xsl:attribute name="ReasonText"><xsl:text>No RED check during order capture</xsl:text></xsl:attribute>		          
                  </OrderHoldType>
                  </xsl:if>
				  <xsl:if test="/AcademyMergedDocument/EnvironmentDocument/Order/@ExtnFraudCheck='review'">
				<OrderHoldType>
				  	<xsl:attribute name="HoldType"><xsl:text>ACADEMY_FRAUD_REVIEW</xsl:text></xsl:attribute>
					<xsl:attribute name="ReasonText"><xsl:text>Awaiting Fraud Checking</xsl:text></xsl:attribute>		          
                  </OrderHoldType>
                  </xsl:if>
				  <xsl:if test="/AcademyMergedDocument/EnvironmentDocument/Order/@ExtnFraudCheck='review_connectfail'">
				<OrderHoldType>
				  	<xsl:attribute name="HoldType"><xsl:text>ACADEMY_FRAUDNOCHECK</xsl:text></xsl:attribute>
					<xsl:attribute name="ReasonText"><xsl:text>Fraud Connect Fail during order capture</xsl:text></xsl:attribute>
                  </OrderHoldType>
                  </xsl:if>
		  <!-- FTP-9 : Start of Condition checks for Kount related fraud checks-->
				  <xsl:if test="/AcademyMergedDocument/EnvironmentDocument/Order/@ExtnFraudCheck='Review_Kount'">
				<OrderHoldType>
				  	<xsl:attribute name="HoldType"><xsl:text>FRAUD_REVIEW_KOUNT</xsl:text></xsl:attribute>
					<xsl:attribute name="ReasonText"><xsl:text>Review response from Kount</xsl:text></xsl:attribute>
                  </OrderHoldType>
                  </xsl:if>
				  				  <xsl:if test="/AcademyMergedDocument/EnvironmentDocument/Order/@ExtnFraudCheck='Connectfail_Kount'">
				<OrderHoldType>
				  	<xsl:attribute name="HoldType"><xsl:text>FRAUD_NOCHECK_KOUNT</xsl:text></xsl:attribute>
					<xsl:attribute name="ReasonText"><xsl:text>Fraud Connect Fail at Kount</xsl:text></xsl:attribute>
                  </OrderHoldType>
                  </xsl:if>
				  				  <xsl:if test="/AcademyMergedDocument/EnvironmentDocument/Order/@ExtnFraudCheck='Escalate_Kount'">
				<OrderHoldType>
				  	<xsl:attribute name="HoldType"><xsl:text>FRAUD_ESCALATE_KOUNT</xsl:text></xsl:attribute>
					<xsl:attribute name="ReasonText"><xsl:text>Escalate response from Kount</xsl:text></xsl:attribute>
                  </OrderHoldType>
                  </xsl:if>			  
		  <!-- FTP-9 : End of Condition checks for Kount related fraud checks-->				  
                  </OrderHoldTypes>
			
			<xsl:element name="Extn">
							<xsl:copy-of select="Extn/@*"/>
							
	<xsl:choose>
    <xsl:when test="/AcademyMergedDocument/EnvironmentDocument/Order/@ExtnExternalCustomerID!=''">
    <xsl:attribute name="ExtnExternalCustomerID">
							<xsl:value-of select="/AcademyMergedDocument/EnvironmentDocument/Order/@ExtnExternalCustomerID" />
							</xsl:attribute>
    </xsl:when>
    <xsl:otherwise>
    <xsl:attribute name="ExtnExternalCustomerID">
							<xsl:value-of select="AcademyMergedDocument/InputDocument/CustomerList/Customer/@ExternalCustomerID" />
							</xsl:attribute> 
    </xsl:otherwise>
  </xsl:choose>

<xsl:attribute name="ExtnIsWebOrder"><xsl:text>Y</xsl:text></xsl:attribute>
			<xsl:attribute name="ExtnWebFraudCheck">
				<xsl:value-of select="AcademyMergedDocument/EnvironmentDocument/Order/@ExtnFraudCheck"/>
			</xsl:attribute>
			<!-- FTP-9 : Start of Adding Extn attributes from Kount System -->
			<xsl:attribute name="ExtnTransactionID">
				<xsl:value-of select="AcademyMergedDocument/EnvironmentDocument/Order/@ExtnTransactionID"/>
			</xsl:attribute>
			<xsl:attribute name="ExtnIsOrderFromMoD">
				<xsl:value-of select="AcademyMergedDocument/EnvironmentDocument/Order/@ExtnIsOrderFromMoD"/>
			</xsl:attribute>                    
			<xsl:attribute name="ExtnEventTime">
				<xsl:value-of select="AcademyMergedDocument/EnvironmentDocument/Order/@ExtnEventTime"/>
			</xsl:attribute>                    			
		    	<!-- FTP-9 : End of Adding Extn attributes from Kount System -->
			    <!-- OMNI-95627: Consume ExtnOrderPromoThreshold - Start -->
				<xsl:attribute name="ExtnOrderPromoThreshold">
				<xsl:value-of select="AcademyMergedDocument/EnvironmentDocument/Order/@ExtnOrderPromoThreshold"/>
			</xsl:attribute> 
				<!-- OMNI-95627: Consume ExtnOrderPromoThreshold - End -->

			<!--OMNI-79112 changes start-->
			<xsl:attribute name="ExtnIsCurbsideOpted">
				<xsl:value-of select="AcademyMergedDocument/EnvironmentDocument/Order/@ExtnIsCurbsideOpted"/>
			</xsl:attribute>
			<!-- OMNI-79112 changes end-->
			
			</xsl:element>
			<OrderLines>
				<xsl:for-each select="AcademyMergedDocument/EnvironmentDocument/Order/OrderLines/OrderLine">
					<xsl:element name="OrderLine">
						<xsl:copy-of select="@*"/>
						<!-- OMNI 5001 EGC Order Fulfillment -->
						<xsl:if test="@LineType='EGC' or FulfillmentType='EGC'">
							<xsl:attribute name="ShipNode">
								<xsl:text>9999</xsl:text>
							</xsl:attribute>
						</xsl:if>
						<!-- OMNI 5001 End -->
					<Extn>
				<xsl:attribute name="ExtnWCOrderItemIdentifier">
                 			 <xsl:value-of select="Extn/@ExtnWCOrderItemIdentifier" />
							</xsl:attribute>
								<!-- BOPIS :: Start BOPIS-11 : ASO_Order Capture And Processing -->
								<xsl:if test="@DeliveryMethod='PICK'">
									<xsl:attribute name="ExtnIsAgeRestricted">
		                 			 <xsl:value-of select="Extn/@ExtnIsAgeRestricted" />
									</xsl:attribute> 
									<xsl:attribute name="ExtnAgeRestrictionCode">
		                 			 <xsl:value-of select="Extn/@ExtnAgeRestrictionCode" />
									</xsl:attribute>									
								</xsl:if>
								<!-- BOPIS :: End BOPIS-11 : ASO_Order Capture And Processing -->
				<!-- Start OMNI-2821 : Narvar: Send Narvar ASO Promise Date  -->	
				<xsl:attribute name="ExtnInitialPromiseDate">
					<xsl:value-of select="Extn/@ExtnInitialPromiseDate"/>
				</xsl:attribute>  
				<!-- End OMNI-2821 : Narvar: Send Narvar ASO Promise Date  -->
					<!-- OMNI-7242 -->
					<xsl:attribute name="ExtnStoreDeliveryDate">
						<xsl:value-of select="Extn/@ExtnStoreDeliveryDate"/>
					</xsl:attribute> 
					
					<xsl:attribute name="ExtnDCDepartureDate">
						<xsl:value-of select="Extn/@ExtnDCDepartureDate"/>
					</xsl:attribute> 
					<!-- OMNI-7242 -->
						<!-- OMNI-41994 : Signature Required Changes - Level of service -->
						<xsl:attribute name="ExtnIsSignatureRequired">
							<xsl:value-of select="Extn/@ExtnIsSignatureRequired"/>
						</xsl:attribute> 
						<!-- OMNI-41994 : Signature Required Changes - Level of service -->
			 <!-- OMNI-75056 -->
                  <xsl:attribute name="ExtnIsDeliveryFeeApplicable">
                       <xsl:value-of select="Extn/@ExtnIsDeliveryFeeApplicable"/>
                   </xsl:attribute>
              <!-- OMNI-75056 -->
				<!--OMNI-90271- FreeLineItem -Start-->
					<xsl:attribute name="ExtnPromoType">
						<xsl:value-of select="Extn/@ExtnPromoType" />
					</xsl:attribute>
					<xsl:attribute name="ExtnPromoParentId">
						<xsl:value-of select="Extn/@ExtnPromoParentId" />
					</xsl:attribute>
					<xsl:attribute name="ExtnPromoChildId">
						<xsl:value-of select="Extn/@ExtnPromoChildId" />
					</xsl:attribute>
					<xsl:attribute name="ExtnIsPromoItem">
						<xsl:value-of select="Extn/@ExtnIsPromoItem" />
					</xsl:attribute>
					<xsl:attribute name="ExtnIsGiftCard">
						<xsl:value-of select="Extn/@ExtnIsGiftCard" />
					</xsl:attribute>
                    <!--OMNI-90271- FreeLineItem -End-->
			        <!--OMNI-98471 start-->
			        <xsl:attribute name="ExtnIsAssemblyRequired">
						<xsl:value-of select="Extn/@ExtnIsAssemblyRequired" />
				    </xsl:attribute>
			       <!--OMNI-98471 End-->
				   <!-- OMNI-105592 -->
					<xsl:attribute name="ExtnHasFireArm02Items">
                       <xsl:value-of select="Extn/@ExtnhasFireArm02Items"/>
					</xsl:attribute>
				   <!-- OMNI-105592 -->
					</Extn>
					<!--Start OMNI-8684 STS Inventory Check Changes -->
					<xsl:if test="@FulfillmentType='STS'">
					<OrderLineSourcingControls> 
						<xsl:element name="OrderLineSourcingCntrl">
							<xsl:attribute name="InventoryCheckCode">
		                 			 <xsl:value-of select="'INFINV'" />
							</xsl:attribute> 
							<xsl:attribute name="Node">
		                 			 <xsl:value-of select="@ShipNode" />
							</xsl:attribute> 
						</xsl:element> 
					</OrderLineSourcingControls>
					</xsl:if>
					<!--End OMNI-8684 STS Inventory Check Changes -->
						<xsl:element name="Item">
							<xsl:copy-of select="Item/@*"/>
							<xsl:attribute name="ProductClass"><xsl:text>GOOD</xsl:text></xsl:attribute>
							<xsl:attribute name="UnitOfMeasure"><xsl:text>EACH</xsl:text></xsl:attribute>
						</xsl:element>
						<xsl:element name="LinePriceInfo">
							<xsl:copy-of select="LinePriceInfo/@*"/>
						</xsl:element>
						<xsl:element name="PersonInfoShipTo">
							<xsl:copy-of select="PersonInfoShipTo/@*"/>
							<xsl:if test="/AcademyMergedDocument/EnvironmentDocument/Order/@AllAddressVerified='N'">
				                  <xsl:attribute name="IsAddressVerified">
                     <xsl:value-of select="'N'" />
                  </xsl:attribute>
                  </xsl:if>
                  <Extn>
            	<xsl:attribute name="ExtnIsPOBOXADDRESS">
                 			 <xsl:value-of select="PersonInfoShipTo/@IsPOBox" />
							</xsl:attribute>
		<xsl:attribute name="ExtnIsSignatureRequired">
                 			 <xsl:value-of select="PersonInfoShipTo/@IsSignatureRequired" />
							</xsl:attribute>
		<xsl:attribute name="ExtnIsMilitary">
                 			 <xsl:value-of select="PersonInfoShipTo/@IsMilitary" />
							</xsl:attribute>
		<xsl:attribute name="ExtnAddressNickName">
                 			 <xsl:value-of select="'N'" />
							</xsl:attribute>
		<xsl:attribute name="ExtnIsAPOFPO">
                 			 <xsl:value-of select="PersonInfoShipTo/@ExtnIsAPOFPO" />
							</xsl:attribute>
		<xsl:attribute name="ExtnIsAddressVerified">
                 			 <xsl:value-of select="'N'" />
					</xsl:attribute>

                 </Extn>
						</xsl:element>
						<LineCharges>
							<xsl:for-each select="LineCharges/LineCharge">
								<xsl:element name="LineCharge">
									<xsl:copy-of select="@*"/>
								</xsl:element>
							</xsl:for-each>
						</LineCharges>
						<LineTaxes>
							<xsl:for-each select="LineTaxes/LineTax">
								<xsl:element name="LineTax">
									<xsl:copy-of select="@*"/>
								</xsl:element>
							</xsl:for-each>
						</LineTaxes>
						<Awards>
							<xsl:for-each select="Awards/Award">
								<xsl:element name="Award">
									<xsl:copy-of select="@*"/>
								</xsl:element>
							</xsl:for-each>
						</Awards>
							<xsl:if test="../../Instructions/Instruction/@InstructionType='GIFT'">
						<Instructions>
						<xsl:for-each select="../../Instructions/Instruction">
							<xsl:element name="Instruction">
									<xsl:attribute name="InstructionType">
                 						 <xsl:value-of select="'GIFT'" />
									</xsl:attribute>
										<xsl:attribute name="InstructionText">
                 						    <xsl:value-of select="@InstructionText" />
									</xsl:attribute>
								</xsl:element>
							</xsl:for-each>
						</Instructions>
						</xsl:if>
					</xsl:element>
				</xsl:for-each>
			</OrderLines>
			<PersonInfoBillTo>
				<xsl:copy-of select="AcademyMergedDocument/EnvironmentDocument/Order/PersonInfoBillTo/@*"/>
				
				<!--  IsAddressVarified in Bill To address will always be 'Y' -->
				<!-- <xsl:if test="AcademyMergedDocument/EnvironmentDocument/Order/@AllAddressVerified='N'"> -->
					<xsl:attribute name="IsAddressVerified">
                     	<xsl:value-of select="'Y'" />
                  	</xsl:attribute>
                 <!--  </xsl:if> -->
			<xsl:choose>
				<xsl:when test="string-length(concat(AcademyMergedDocument/EnvironmentDocument/Order/PersonInfoBillTo/@FirstName,AcademyMergedDocument/EnvironmentDocument/Order/PersonInfoBillTo/@LastName)) &gt; 40">
						<xsl:attribute name="Suffix">
									<xsl:value-of select="substring(concat(AcademyMergedDocument/EnvironmentDocument/Order/PersonInfoBillTo/@FirstName,AcademyMergedDocument/EnvironmentDocument/Order/PersonInfoBillTo/@LastName),0,40)" />
						</xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
						<xsl:attribute name="Suffix">
									<xsl:value-of select="concat(AcademyMergedDocument/EnvironmentDocument/Order/PersonInfoBillTo/@FirstName,AcademyMergedDocument/EnvironmentDocument/Order/PersonInfoBillTo/@LastName)" />
						</xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
                 <Extn>
					<xsl:attribute name="ExtnIsPOBOXADDRESS">
               			 <xsl:value-of select="AcademyMergedDocument/EnvironmentDocument/Order/PersonInfoBillTo/@IsPOBox" />
					</xsl:attribute>
					<xsl:attribute name="ExtnIsSignatureRequired">
               			 <xsl:value-of select="AcademyMergedDocument/EnvironmentDocument/Order/PersonInfoBillTo/@IsSignatureRequired" />
					</xsl:attribute>
					<xsl:attribute name="ExtnIsMilitary">
               			 <xsl:value-of select="AcademyMergedDocument/EnvironmentDocument/Order/PersonInfoBillTo/@IsMilitary" />
					</xsl:attribute>
					<xsl:attribute name="ExtnAddressNickName">
               			 <xsl:value-of select="'N'" />
					</xsl:attribute>
					<xsl:attribute name="ExtnIsAPOFPO">
               			 <xsl:value-of select="AcademyMergedDocument/EnvironmentDocument/Order/PersonInfoBillTo/@ExtnIsAPOFPO" />
					</xsl:attribute>
					<xsl:attribute name="ExtnIsAddressVerified">
               			 <xsl:value-of select="'N'" />
					</xsl:attribute>
                 </Extn>
			</PersonInfoBillTo>			
			<!-- PersonInfoMarkFor to capture alternate pickup person details : Start-->
			<xsl:if test="AcademyMergedDocument/EnvironmentDocument/Order/PersonInfoMarkFor">
			<PersonInfoMarkFor>
				<xsl:copy-of select="AcademyMergedDocument/EnvironmentDocument/Order/PersonInfoMarkFor/@*"/>
			</PersonInfoMarkFor>
			</xsl:if>
			<!-- PersonInfoMarkFor to capture alternate pickup person details : End-->			
			<PaymentMethods>
				<xsl:for-each select="AcademyMergedDocument/EnvironmentDocument/Order/PaymentMethods/PaymentMethod">
					<xsl:element name="PaymentMethod">
						<xsl:copy-of select="@*"/>
						<PaymentDetailsList>
						  <xsl:for-each select="PaymentDetailsList/PaymentDetails">
						  	<xsl:element name="PaymentDetails">
								<xsl:copy-of select="@*"/>
							</xsl:element>
						  </xsl:for-each>
						</PaymentDetailsList>
				    </xsl:element>
				</xsl:for-each>
			</PaymentMethods>
			<Promotions>
				<xsl:for-each select="AcademyMergedDocument/EnvironmentDocument/Order/Promotions/Promotion">
					<xsl:element name="Promotion">
						<xsl:copy-of select="@*"/>
					</xsl:element>
				</xsl:for-each>
			</Promotions>
			<Awards>
				<xsl:for-each select="AcademyMergedDocument/EnvironmentDocument/Order/Awards/Award">
					<xsl:element name="Award">
						<xsl:copy-of select="@*"/>
					</xsl:element>
				</xsl:for-each>
			</Awards>
			<HeaderCharges>
				<xsl:for-each select="AcademyMergedDocument/EnvironmentDocument/Order/HeaderCharges/HeaderCharge">
					<xsl:element name="HeaderCharge">
						<xsl:copy-of select="@*"/>
					</xsl:element>
				</xsl:for-each>
			</HeaderCharges>
			<HeaderTaxes>
				<xsl:for-each select="AcademyMergedDocument/EnvironmentDocument/Order/HeaderTaxes/HeaderTax">
					<xsl:element name="HeaderTax">
						<xsl:copy-of select="@*"/>
					</xsl:element>
				</xsl:for-each>
			</HeaderTaxes>
			<OrderLineRelationships>
				<xsl:for-each select="AcademyMergedDocument/EnvironmentDocument/Order/OrderLineRelationships/OrderLineRelationship">
					<xsl:element name="OrderLineRelationship">
						<xsl:copy-of select="@*"/>
						<ParentLine>
							<xsl:copy-of select="@*"/>
						</ParentLine>
						<ChildLine>
							<xsl:copy-of select="@*"/>
						</ChildLine>
					</xsl:element>
				</xsl:for-each>
			</OrderLineRelationships>
		</Order>
	</xsl:template>
</xsl:stylesheet><!-- Stylus Studio meta-information - (c) 2004-2009. Progress Software Corporation. All rights reserved.

<metaInformation>
	<scenarios>
		<scenario default="yes" name="Scenario1" userelativepaths="yes" externalpreview="no" url="in.xml" htmlbaseurl="" outputurl="" processortype="saxon8" useresolver="yes" profilemode="0" profiledepth="" profilelength="" urlprofilexml="" commandline=""
		          additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext="" validateoutput="no" validator="internal" customvalidator="">
			<advancedProp name="sInitialMode" value=""/>
			<advancedProp name="bXsltOneIsOkay" value="true"/>
			<advancedProp name="bSchemaAware" value="true"/>
			<advancedProp name="bXml11" value="false"/>
			<advancedProp name="iValidation" value="0"/>
			<advancedProp name="bExtensions" value="true"/>
			<advancedProp name="iWhitespace" value="0"/>
			<advancedProp name="sInitialTemplate" value=""/>
			<advancedProp name="bTinyTree" value="true"/>
			<advancedProp name="xsltVersion" value="2.0"/>
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