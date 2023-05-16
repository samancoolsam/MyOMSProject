<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml"
		doctype-system='Update_Academy_InventoryMonitor_10.dtd' indent="yes" />

	<xsl:template match="/">

		<Update_Academy_InventoryMonitor version="1.0">

			<ControlArea>

				<Verb value="Update">

				</Verb>
				<Noun value="Academy_InventoryMonitor">

				</Noun>

			</ControlArea>
			<DataArea>

				<InventoryMonitor>
					<!--Start changes STL 1482 -->		
					<xsl:for-each select="/InventoryItem/AvailabilityChanges/AvailabilityChange">
							<xsl:if test="@Node='' and not (@DeliveryMethod='PICK')"> 
					<AvailabilityChange>					
					
						<xsl:attribute name="AlertRaisedOn" >
							<xsl:value-of select="@AlertRaisedOn" />
						</xsl:attribute>	
						<xsl:attribute name="FirstFutureAvailableDate" >
							<xsl:value-of select="@FirstFutureAvailableDate" />
						</xsl:attribute>	
						<xsl:attribute name="FutureAvailableQuantity" >
							<xsl:value-of select="@FutureAvailableQuantity" />
						</xsl:attribute>	
						<xsl:attribute name="MonitorOption" >
							<xsl:value-of select="@MonitorOption" />
						</xsl:attribute>	
						<xsl:attribute name="OnhandAvailableDate" >
							<xsl:value-of select="@OnhandAvailableDate" />
						</xsl:attribute>	
						<xsl:attribute name="OnhandAvailableQuantity" >
							<xsl:value-of select="@OnhandAvailableQuantity" />
						</xsl:attribute>	
						<xsl:attribute name="Node" >
							<xsl:value-of select="'005'" />
						</xsl:attribute>
						
						<Item>						
   							 <xsl:attribute name="ItemID">
     						 <xsl:value-of select="/InventoryItem/@ItemID" />
   							 </xsl:attribute>
  						</Item>	
							
					</AvailabilityChange>
				</xsl:if>
				
					<!--Start : BOPIS-114 – OMS-WCS Inventory Management (RTAM) -->
					<!--For BOPIS(DeliveryMethod='PICK') items, only publish node level inventory picture-->
					<xsl:if test="@DeliveryMethod='PICK' and not (@Node='' or @Node='001' or @Node='701' or @Node='711')">
						<AvailabilityChange>
							<xsl:attribute name="AlertRaisedOn" >
								<xsl:value-of select="@AlertRaisedOn" />
							</xsl:attribute>	
							<xsl:attribute name="FirstFutureAvailableDate" >
								<xsl:value-of select="@FirstFutureAvailableDate" />
							</xsl:attribute>	
							<xsl:attribute name="FutureAvailableQuantity" >
								<xsl:value-of select="@FutureAvailableQuantity" />
							</xsl:attribute>	
							<xsl:attribute name="MonitorOption" >
								<xsl:value-of select="@MonitorOption" />
							</xsl:attribute>	
							<xsl:attribute name="OnhandAvailableDate" >
								<xsl:value-of select="@OnhandAvailableDate" />
							</xsl:attribute>	
							<xsl:attribute name="OnhandAvailableQuantity" >
								<xsl:value-of select="@OnhandAvailableQuantity" />
							</xsl:attribute>	
							<xsl:attribute name="Node" >
								<xsl:value-of select="@Node" />
							</xsl:attribute>
							<xsl:attribute name="DeliveryMethod" >
								<xsl:value-of select="@DeliveryMethod" />
							</xsl:attribute>
							<Item>						
 								<xsl:attribute name="ItemID">
   						 			<xsl:value-of select="/InventoryItem/@ItemID" />
 							 	</xsl:attribute>
	  						</Item>
					</AvailabilityChange>
				</xsl:if>
				<!--End : BOPIS-114 – OMS-WCS Inventory Management (RTAM) -->
				
				<!-- OMNI-6363 : STS - Begin -->
				<xsl:if test="@DeliveryMethod='SHP' and (@Node='001' or @Node='701' or @Node='711')">
						<AvailabilityChange>
							<xsl:attribute name="AlertRaisedOn" >
								<xsl:value-of select="@AlertRaisedOn" />
							</xsl:attribute>	
							<xsl:attribute name="FirstFutureAvailableDate" >
								<xsl:value-of select="@FirstFutureAvailableDate" />
							</xsl:attribute>	
							<xsl:attribute name="FutureAvailableQuantity" >
								<xsl:value-of select="@FutureAvailableQuantity" />
							</xsl:attribute>	
							<xsl:attribute name="MonitorOption" >
								<xsl:value-of select="@MonitorOption" />
							</xsl:attribute>	
							<xsl:attribute name="OnhandAvailableDate" >
								<xsl:value-of select="@OnhandAvailableDate" />
							</xsl:attribute>	
							<xsl:attribute name="OnhandAvailableQuantity" >
								<xsl:value-of select="@OnhandAvailableQuantity" />
							</xsl:attribute>	
							<xsl:attribute name="Node" >
								<xsl:value-of select="@Node" />
							</xsl:attribute>
							<xsl:attribute name="DeliveryMethod" >
								<xsl:value-of select="@DeliveryMethod" />
							</xsl:attribute>
							<Item>						
 								<xsl:attribute name="ItemID">
   						 			<xsl:value-of select="/InventoryItem/@ItemID" />
 							 	</xsl:attribute>
	  						</Item>
					</AvailabilityChange>
				</xsl:if>
				<!-- OMNI-6363 : STS - End -->
			</xsl:for-each>
			<!--End changes STL 1482 -->		
					
		</InventoryMonitor>

	</DataArea>

	</Update_Academy_InventoryMonitor>

	</xsl:template>

</xsl:stylesheet>
