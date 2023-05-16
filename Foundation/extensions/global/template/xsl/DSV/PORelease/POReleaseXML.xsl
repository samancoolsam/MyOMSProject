<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		
		<!-- WN-2561 :: Fix for SOF -->
		<xsl:variable name="varStoreNo">
            <xsl:if test="string-length(/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@FirstName) &gt; 0 and starts-with(/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@FirstName,'Store')">
            	<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@FirstName" />
            </xsl:if>
            <xsl:if test="string-length(/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@Company) &gt; 0 and starts-with(/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@Company,'Store')">
            	<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@Company" />
            </xsl:if>
        </xsl:variable>
		<!-- WN-2561 :: End Fix for SOF -->

		<OrderRelease>
			<xsl:attribute name="CarrierServiceCode">
				<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/@CarrierServiceCode">
				</xsl:value-of>
			</xsl:attribute>

			<xsl:attribute name="CustomerPONo">
				<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/@CustomerPoNo">
				</xsl:value-of>
			</xsl:attribute>

			<xsl:attribute name="EnterpriseCode">
				<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/@EnterpriseCode">
				</xsl:value-of>
			</xsl:attribute>
			<xsl:attribute name="OrderDate">
				<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/@OrderDate">
				</xsl:value-of>
			</xsl:attribute>
			<xsl:attribute name="OrderHeaderKey">
				<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/@OrderHeaderKey">
				</xsl:value-of>
			</xsl:attribute>
			<xsl:attribute name="OrderReleaseKey">
				<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/@OrderReleaseKey">
				</xsl:value-of>
			</xsl:attribute>
			<xsl:attribute name="ReleaseNo">
				<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/@ReleaseNo">
				</xsl:value-of>
			</xsl:attribute>

			<xsl:attribute name="ReqDeliveryDate">
				<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/@ReqDeliveryDate">
				</xsl:value-of>
			</xsl:attribute>

			<xsl:attribute name="ReqShipDate">
				<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/@ReqShipDate">
				</xsl:value-of>
			</xsl:attribute>

			<xsl:attribute name="SCAC">
				<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/@SCAC">
				</xsl:value-of>
			</xsl:attribute>

			<xsl:attribute name="SalesOrderNo">
				<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/@OrderName">
				</xsl:value-of>
			</xsl:attribute>

			<xsl:attribute name="ShipNode">
				<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/@ShipNode">
				</xsl:value-of>
			</xsl:attribute>



			<Order>


				<xsl:attribute name="OrderNo">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/Order/@OrderNo">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="EnterpriseCode">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/Order/@EnterpriseCode">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="OrderHeaderKey">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/Order/@OrderHeaderKey">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="DocumentType">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/Order/@DocumentType">
					</xsl:value-of>
				</xsl:attribute>
			</Order>

			<PersonInfoShipTo>
				<xsl:attribute name="AddressLine1">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@AddressLine1">
					</xsl:value-of>
				</xsl:attribute>
				
				<!-- WN-2561 :: Fix for SOF -->
				<xsl:attribute name="AddressLine2">
					<xsl:choose>
						<xsl:when test="normalize-space(/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/@SCAC)='ShipToStore'">
							<xsl:value-of select="normalize-space(concat(/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@AddressLine2, ' ', $varStoreNo))" />
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@AddressLine2" />
						</xsl:otherwise>
					</xsl:choose>
				</xsl:attribute>
				<!-- WN-2561 :: End Fix for SOF -->
				
				<xsl:attribute name="AddressLine3">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@AddressLine3">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="AddressLine4">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@AddressLine4">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="AddressLine5">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@AddressLine5">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="AddressLine6">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@AddressLine6">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="City">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@City">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="Country">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@Country">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="Company">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@Company">
					</xsl:value-of>
				</xsl:attribute>
				
				<!-- WN-2561 :: End Fix for SOF -->
				<xsl:attribute name="DayPhone">
				<xsl:choose>
                	<xsl:when test="normalize-space(/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/@SCAC)='ShipToStore'">
                    	<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Output/OrderList/Order/PersonInfoBillTo/@DayPhone" />
                    </xsl:when>
                    <xsl:otherwise>
						<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@DayPhone" />
					</xsl:otherwise>
				</xsl:choose>
				</xsl:attribute>
				<!-- WN-2561 :: End Fix for SOF -->
				
				<xsl:attribute name="EMailID">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@EMailID">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="EveningPhone">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@EveningPhone">
					</xsl:value-of>
				</xsl:attribute>
				
				<!-- WN-2561 :: Fix for SOF -->
				<xsl:attribute name="FirstName">
				<xsl:choose>
                	<xsl:when test="normalize-space(/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/@SCAC)='ShipToStore'">
                    	<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Output/OrderList/Order/PersonInfoBillTo/@FirstName" />
                    </xsl:when>
                    <xsl:otherwise>
						<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@FirstName" />
					</xsl:otherwise>
				</xsl:choose>
				</xsl:attribute>
				<!-- WN-2561 :: End Fix for SOF -->
				
				<xsl:attribute name="JobTitle">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@JobTitle">
					</xsl:value-of>
				</xsl:attribute>
				
				<!-- WN-2561 :: Fix for SOF -->
				<xsl:attribute name="LastName">
				<xsl:choose>
                	<xsl:when test="normalize-space(/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/@SCAC)='ShipToStore'">
                    	<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Output/OrderList/Order/PersonInfoBillTo/@LastName" />
                    </xsl:when>
                    <xsl:otherwise>
						<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@LastName" />
					</xsl:otherwise>
				</xsl:choose>
				</xsl:attribute>
				
				<xsl:attribute name="MiddleName">
				<xsl:choose>
                	<xsl:when test="normalize-space(/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/@SCAC)='ShipToStore'">
                    	<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Output/OrderList/Order/PersonInfoBillTo/@MiddleName" />
                    </xsl:when>
                    <xsl:otherwise>
						<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@MiddleName" />
					</xsl:otherwise>
				</xsl:choose>
				</xsl:attribute>
				<!-- WN-2561 :: End Fix for SOF -->
							
				<xsl:attribute name="MobilePhone">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@MobilePhone">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="State">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@State">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="Suffix">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@Suffix">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="Title">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@Title">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="ZipCode">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/PersonInfoShipTo/@ZipCode">
					</xsl:value-of>
				</xsl:attribute>
			</PersonInfoShipTo>

			<PersonInfoBillTo>
				<xsl:attribute name="AddressLine1">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Output/OrderList/Order/PersonInfoBillTo/@AddressLine1">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="AddressLine2">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Output/OrderList/Order/PersonInfoBillTo/@AddressLine2">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="AddressLine3">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Output/OrderList/Order/PersonInfoBillTo/@AddressLine3">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="AddressLine4">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Output/OrderList/Order/PersonInfoBillTo/@AddressLine4">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="AddressLine5">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Output/OrderList/Order/PersonInfoBillTo/@AddressLine5">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="AddressLine6">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Output/OrderList/Order/PersonInfoBillTo/@AddressLine6">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="City">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Output/OrderList/Order/PersonInfoBillTo/@City">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="Country">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Output/OrderList/Order/PersonInfoBillTo/@Country">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="Company">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Output/OrderList/Order/PersonInfoBillTo/@Company">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="DayPhone">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Output/OrderList/Order/PersonInfoBillTo/@DayPhone">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="EMailID">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Output/OrderList/Order/PersonInfoBillTo/@EMailID">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="EveningPhone">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Output/OrderList/Order/PersonInfoBillTo/@EveningPhone">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="FirstName">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Output/OrderList/Order/PersonInfoBillTo/@FirstName">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="JobTitle">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Output/OrderList/Order/PersonInfoBillTo/@JobTitle">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="LastName">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Output/OrderList/Order/PersonInfoBillTo/@LastName">
					</xsl:value-of>
				</xsl:attribute>

				<xsl:attribute name="MiddleName">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Output/OrderList/Order/PersonInfoBillTo/@MiddleName">
					</xsl:value-of>
				</xsl:attribute>

				<xsl:attribute name="MobilePhone">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Output/OrderList/Order/PersonInfoBillTo/@MobilePhone">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="State">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Output/OrderList/Order/PersonInfoBillTo/@State">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="Suffix">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Output/OrderList/Order/PersonInfoBillTo/@Suffix">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="Title">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Output/OrderList/Order/PersonInfoBillTo/@Title">
					</xsl:value-of>
				</xsl:attribute>
				<xsl:attribute name="ZipCode">
					<xsl:value-of select="/CustomMultiAPI/AcademyGetOrderList/Output/OrderList/Order/PersonInfoBillTo/@ZipCode">
					</xsl:value-of>
				</xsl:attribute>
			</PersonInfoBillTo>


			<OrderLines>

				<xsl:for-each select="/CustomMultiAPI/AcademyGetOrderList/Input/OrderRelease/OrderLine">


					<OrderLine>

						<xsl:attribute name="OrderedQty">
							<xsl:value-of select="@OrderedQty">
							</xsl:value-of>
						</xsl:attribute>

						<xsl:attribute name="PrimeLineNo">
							<xsl:value-of select="@PrimeLineNo">
							</xsl:value-of>
						</xsl:attribute>

						<Item>

							<xsl:attribute name="ItemDesc">
								<!--STL-1437::Changed @ItemDesc to @ItemShortDesc -->
								<xsl:value-of select="Item/@ItemShortDesc">
								<!--END-->
								</xsl:value-of>
							</xsl:attribute>

							<xsl:attribute name="ItemID">
								<xsl:value-of select="Item/@ItemID">
								</xsl:value-of>
							</xsl:attribute>

							<xsl:attribute name="ManufacturerItem">
								<xsl:value-of select="Item/@ManufacturerItem">
								</xsl:value-of>
							</xsl:attribute>

							<xsl:attribute name="ItemShortDesc">
								<xsl:value-of select="Item/@ItemShortDesc">
								</xsl:value-of>
							</xsl:attribute>

							<xsl:attribute name="ItemWeight">
								<xsl:value-of select="Item/@ItemWeight">
								</xsl:value-of>
							</xsl:attribute>

							<xsl:attribute name="ItemWeightUOM">
								<xsl:value-of select="Item/@ItemWeightUOM">
								</xsl:value-of>
							</xsl:attribute>

							<xsl:attribute name="UnitCost">
								<xsl:value-of select="Item/@UnitCost">
								</xsl:value-of>
							</xsl:attribute>

							<xsl:attribute name="UnitOfMeasure">
								<xsl:value-of select="Item/@UnitOfMeasure">
								</xsl:value-of>
							</xsl:attribute>
						</Item>

						<Instructions>
							<xsl:attribute name="NumberOfInstructions">
								<xsl:value-of select="Instructions/Instruction/@NumberOfInstructions">
								</xsl:value-of>
							</xsl:attribute>

							<Instruction>
								<xsl:attribute name="InstructionText">
									<xsl:value-of select="Instructions/Instruction/@InstructionText">
									</xsl:value-of>
								</xsl:attribute>

								<xsl:attribute name="InstructionType">
									<xsl:value-of select="Instructions/Instruction/@InstructionType">
									</xsl:value-of>
								</xsl:attribute>

								<xsl:attribute name="InstructionURL">
									<xsl:value-of select="Instructions/Instruction/@InstructionURL">
									</xsl:value-of>
								</xsl:attribute>

								<xsl:attribute name="SequenceNo">
									<xsl:value-of select="Instructions/Instruction/@SequenceNo">
									</xsl:value-of>
								</xsl:attribute>
							</Instruction>
						</Instructions>

						<OrderStatuses>
							<OrderStatus>

								<xsl:attribute name="StatusDate">
									<xsl:value-of select="OrderStatuses/OrderStatus/@StatusDate">
									</xsl:value-of>
								</xsl:attribute>

								<xsl:attribute name="StatusDescription">
									<xsl:value-of select="OrderStatuses/OrderStatus/@StatusDescription">
									</xsl:value-of>
								</xsl:attribute>

								<xsl:attribute name="StatusQty">
									<xsl:value-of select="OrderStatuses/OrderStatus/@StatusQty">
									</xsl:value-of>
								</xsl:attribute>
							</OrderStatus>
						</OrderStatuses>

						<xsl:copy-of select="LinePriceInfo"/>

						<xsl:copy-of select="LineCharges"/>


						<xsl:copy-of select="LineTaxes"/>
					</OrderLine>
				</xsl:for-each>
			</OrderLines>
		</OrderRelease>
	</xsl:template>
</xsl:stylesheet>

