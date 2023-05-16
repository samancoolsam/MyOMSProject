
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">

    <xsl:output indent="yes"/>
    
    <xsl:template match="MultiApi">
      <MultiApi>
			<xsl:element name="API">
				<xsl:attribute name="Name">
					<xsl:text>changeShipment</xsl:text>
				</xsl:attribute>
				<xsl:element name="Input">    
			    		<xsl:element name="Shipment">
         
 						<xsl:for-each select="/MultiApi/API[(normalize-space(@Name) = &quot;getRuleList&quot;)]" >
								<xsl:for-each select="Output/RulesList/Rules">
									<xsl:if test="@RuleSetFieldName='STORE_IS_BP_REQD'">
										<xsl:attribute name="IsBackroomPickRequired">
											<xsl:value-of select="@RuleSetValue"/>
										 </xsl:attribute>
									</xsl:if>
									<xsl:if test="@RuleSetFieldName='STORE_LOC_FRONT'">
										<xsl:attribute name="ShipmentSortLocationId">
											<xsl:value-of select="@RuleSetValue"/>
										</xsl:attribute>    										
									</xsl:if>	
								</xsl:for-each>
						</xsl:for-each> 

						<xsl:for-each select="/MultiApi/API[(normalize-space(@Name) = &quot;getShipmentDetails&quot;)]" >
	      		      				<xsl:for-each select="Output/Shipment" >		
	      								<xsl:attribute name="ShipmentKey">
	      							<xsl:value-of select="@ShipmentKey"/>
	      						</xsl:attribute>	      			
	      					</xsl:for-each>
						</xsl:for-each>      
              				</xsl:element>
				</xsl:element>
				<Template>	
					  <Shipment ShipmentKey="" ShipmentNo="" ShipNode="" SellerOrganizationCode="" IsBackroomPickRequired=""/>
				</Template>                                 
			</xsl:element>

			<xsl:element name="API">
				<xsl:attribute name="Name">
					<xsl:text>changeShipmentStatus</xsl:text>
				</xsl:attribute>
				<xsl:element name="Input">    
			    		<xsl:element name="Shipment">
         
 							<xsl:for-each select="/MultiApi/API[(normalize-space(@Name) = &quot;getRuleList&quot;)]" >
								<xsl:for-each select="Output/RulesList/Rules">
									<xsl:if test="@RuleSetFieldName='STORE_IS_BP_REQD'">
										<xsl:if test="@RuleSetValue='Y'">
											<xsl:attribute name="BaseDropStatus">
												<xsl:text>1100.70.06.10</xsl:text>
											</xsl:attribute>
										</xsl:if>
										<xsl:if test="@RuleSetValue='N'">
											<xsl:attribute name="BaseDropStatus">
												<xsl:text>1100.70.06.30</xsl:text>
											</xsl:attribute>
										</xsl:if>
									</xsl:if>
								</xsl:for-each>
							</xsl:for-each> 

							<xsl:for-each select="/MultiApi/API[(normalize-space(@Name) = &quot;getShipmentDetails&quot;)]" >
	      		      					<xsl:for-each select="Output/Shipment" >		
	      								<xsl:attribute name="ShipmentKey">
	      									<xsl:value-of select="@ShipmentKey"/>
	      								</xsl:attribute>
									<xsl:if test="@DocumentType='0006'">
										<xsl:attribute name="TransactionId">
	      										<xsl:text>SOP_CHECK_BACKROOM_PICK.0006</xsl:text>
	      									</xsl:attribute>
									</xsl:if>
									<xsl:if test="@DocumentType='0001'">
										<xsl:attribute name="TransactionId">
	      										<xsl:text>SOP_CHECK_BACKROOM_PICK_REQD</xsl:text>
	      									</xsl:attribute>
									</xsl:if>
	      							</xsl:for-each>
							</xsl:for-each>      
              				</xsl:element>
				</xsl:element>                                
			</xsl:element>
        </MultiApi>
    </xsl:template>

</xsl:stylesheet>