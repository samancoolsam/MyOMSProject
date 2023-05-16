<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes" />

	<xsl:template match="/">
		<xsl:apply-templates select="Order" />
	</xsl:template>

	<xsl:template match="Order">
		<Order>
			<xsl:attribute name="DocumentType">
            	<xsl:value-of select="@DocumentType" />
         	</xsl:attribute>

			<xsl:attribute name="OrderType">
            	<xsl:value-of select="'APPEASEMENT'" />
         	</xsl:attribute>

			<xsl:attribute name="EnterpriseCode">
	        	<xsl:value-of select="@EnterpriseCode" />
			</xsl:attribute>
		
			<xsl:attribute name="BillToID">
	        	<xsl:value-of select="@BillToID" />
			</xsl:attribute>
		
			<PriceInfo>
				<xsl:attribute name="Currency">
					<xsl:value-of select="'USD'" />
	        	</xsl:attribute>
	        </PriceInfo>
	
			<PersonInfoBillTo>
	        	<xsl:copy-of select="PersonInfoBillTo/@*" />
			</PersonInfoBillTo>
	
			<PersonInfoShipTo>
	        	<xsl:copy-of select="OrderLines/OrderLine/PersonInfoShipTo/@*" />
			</PersonInfoShipTo>
	
			<References>
				<Reference>
       				<xsl:attribute name="Name">
          				<xsl:value-of select="'AppeasementParentOrderNo'" />
       				</xsl:attribute>
       				<xsl:attribute name="Value">
          				<xsl:value-of select="@OrderNo" />
       				</xsl:attribute>
       			</Reference>
       			<Reference>
       				<xsl:attribute name="Name">
          				<xsl:value-of select="'AppeasementParentOrderHeaderKey'" />
       				</xsl:attribute>
       				<xsl:attribute name="Value">
          				<xsl:value-of select="@OrderHeaderKey" />
       				</xsl:attribute>
       			</Reference>
       			<Reference>
       				<xsl:attribute name="Name">
          				<xsl:value-of select="'AppeasementReasonCode'" />
       				</xsl:attribute>
       				<xsl:attribute name="Value">
          				<xsl:value-of select="@AppeasementDescription" />
       				</xsl:attribute>
				</Reference>
			</References>
			
			<OrderLines>
    			<OrderLine>
       				<xsl:attribute name="PrimeLineNo">
          				<xsl:value-of select="'1'" />
       				</xsl:attribute>

       				<xsl:attribute name="SubLineNo">
          				<xsl:value-of select="'1'" />
       				</xsl:attribute>

       				<xsl:attribute name="OrderedQty">
          				<xsl:value-of select="'1'" />
       				</xsl:attribute>

       				<xsl:attribute name="DeliveryMethod">
          				<xsl:value-of select="'SHP'" />
       				</xsl:attribute>

       				<xsl:attribute name="SCAC">
          				<xsl:value-of select="'USPS-Letter'" />
       				</xsl:attribute>

       				<xsl:attribute name="ScacAndService">
          				<xsl:value-of select="'USPS-Letter - Priority'" />
       				</xsl:attribute>

       				<xsl:attribute name="CarrierServiceCode">
          				<xsl:value-of select="'Priority Mail'" />
       				</xsl:attribute>
					<!--OMNI-5008 EGC Appeasement - START-->
					<xsl:variable name = "eGCEnabled" select = "@IsEGCEnabled" />
					<xsl:if test = "$eGCEnabled='Y'">
						<xsl:attribute name="FulfillmentType">
             				<xsl:value-of select="'EGC'" />
          				</xsl:attribute>
						<xsl:attribute name="LineType">
             				<xsl:value-of select="'EGC'" />
          				</xsl:attribute>
						<xsl:attribute name="ShipNode">
             				<xsl:value-of select="'9999'" />
          				</xsl:attribute>
					</xsl:if>
					<!-- OMNI-5008 EGC Appeasement - END-->	
       				<Item>
          				<xsl:attribute name="ItemID">
             				<xsl:value-of select="@GCItemID" />
          				</xsl:attribute>

          				<xsl:attribute name="UnitOfMeasure">
             				<xsl:value-of select="'EACH'" />
          				</xsl:attribute>

          				<xsl:attribute name="ProductClass">
             				<xsl:value-of select="'GOOD'" />
          				</xsl:attribute>
					</Item>
       
       				<xsl:variable name = "offeramount" select = "AppeasementOffer/@OfferAmount" />
       				
       				<LinePriceInfo>
       					<xsl:attribute name="DisplayUnitPrice">
             				<xsl:value-of select="$offeramount" />
          				</xsl:attribute>
          				
          				<xsl:attribute name="UnitPrice">
             				<xsl:value-of select="$offeramount" />
          				</xsl:attribute>
       				</LinePriceInfo>	

					<LineCharges>
						<LineCharge>
							<xsl:attribute name="ChargeCategory">
								<xsl:value-of select="'CUSTOMER_APPEASEMENT'" />
          					</xsl:attribute>
							
							<xsl:attribute name="ChargeName">
		        				<xsl:value-of select="@AppeasementCategory" />
							</xsl:attribute>
		        			
		        			<xsl:attribute name="ChargePerLine">
		        				<xsl:value-of select="$offeramount" />
							</xsl:attribute>

		        			<xsl:attribute name="Reference">
		        				<xsl:value-of select="@AppeasementDescription" />
							</xsl:attribute>
						</LineCharge>
					</LineCharges>
					<PersonInfoShipTo>
						<xsl:copy-of select="OrderLines/OrderLine/PersonInfoShipTo/@*" />
					</PersonInfoShipTo>
				</OrderLine>
			</OrderLines>
	         
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
					<xsl:attribute name="NoteText">
						<xsl:value-of select="Notes/Note/@NoteText" />
					</xsl:attribute>
				</Note>
			</Notes>
	
			<OrderHoldTypes>
				<OrderHoldType>
					<xsl:attribute name="HoldType">
						<xsl:if test = "@ExtnUserGroupName='CSA'">
							<xsl:value-of select="'ASSOCIATE_HOLD'" />
						</xsl:if>
						
						<xsl:if test = "@ExtnUserGroupName='AdminSupport'">
							<xsl:value-of select="'SUPPORT_TEAM_HOLD'" />
						</xsl:if>
						
						<xsl:if test = "@ExtnUserGroupName='Supervisors'">
							<xsl:value-of select="'SUPERVISOR_HOLD'" />
						</xsl:if>
						
						<xsl:if test = "@ExtnUserGroupName='MANAGER'">
							<xsl:value-of select="'OPER_MANAGER_HOLD'" />
						</xsl:if>
						
						<xsl:if test = "@ExtnUserGroupName='SENIOR_MANAGER'">
							<xsl:value-of select="'SITE_LEAD_HOLD'" />
						</xsl:if>
					</xsl:attribute>

					<xsl:attribute name="Status">
						<xsl:value-of select="'1100'" />
					</xsl:attribute>
				</OrderHoldType>
			</OrderHoldTypes>

		</Order>
	</xsl:template>
</xsl:stylesheet>

