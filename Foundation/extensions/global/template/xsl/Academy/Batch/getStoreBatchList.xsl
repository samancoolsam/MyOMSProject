<?xml version='1.0' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">	
		<StoreBatchList>
			<xsl:attribute name="TotalNumberOfRecords">
				<xsl:value-of select="StoreBatchList/@TotalNumberOfRecords" />
			</xsl:attribute>
			<xsl:for-each select="StoreBatchList/StoreBatch">
				<StoreBatch>
					<xsl:attribute name="AssignedToUserId">
						<xsl:value-of select="@AssignedToUserId"/>
					</xsl:attribute>
					<xsl:attribute name="BatchNo">
						<xsl:value-of select="@BatchNo"/>
					</xsl:attribute>
					<xsl:attribute name="BatchType">
						<xsl:value-of select="@BatchType"/>
					</xsl:attribute>
					<xsl:attribute name="DeliveryMethod">
						<xsl:value-of select="@DeliveryMethod"/>
					</xsl:attribute>
					<xsl:attribute name="OldestExpShpDate">
						<xsl:value-of select="@OldestExpShpDate"/>
					</xsl:attribute>
					<xsl:attribute name="ShipmentDeliveryMethod">
						<xsl:value-of select="@ShipmentDeliveryMethod"/>
					</xsl:attribute>
					<xsl:attribute name="Status">
						<xsl:value-of select="@Status"/>
					</xsl:attribute>
					<xsl:attribute name="StoreBatchKey">
						<xsl:value-of select="@StoreBatchKey"/>
					</xsl:attribute>
					<xsl:attribute name="TotalNumberOfItems">
						<xsl:value-of select="@TotalNumberOfItems"/>
					</xsl:attribute>
					<xsl:attribute name="TotalNumberOfShipments">
						<xsl:value-of select="@TotalNumberOfShipments"/>
					</xsl:attribute>
					<ShipmentLines>
						<xsl:for-each select="ShipmentLines/ShipmentLine">
							<ShipmentLine>
								<xsl:attribute name="ShipmentLineKey">
									<xsl:value-of select="@ShipmentLineKey"/>
								</xsl:attribute>
							</ShipmentLine>	 
						</xsl:for-each>
					</ShipmentLines>
					<StoreBatchConfigList>
						<xsl:for-each select="StoreBatchConfigList/StoreBatchConfig">
							<StoreBatchConfig>
								<xsl:attribute name="Entity">
									<xsl:value-of select="@Entity"/>
								</xsl:attribute>
								<xsl:attribute name="Name">
									<xsl:value-of select="@Name"/>
								</xsl:attribute>
								<xsl:attribute name="Value">
									<xsl:value-of select="@Value"/>
								</xsl:attribute>
							</StoreBatchConfig>
						</xsl:for-each>
					</StoreBatchConfigList>
					<DepartmentList>
						<xsl:for-each select="DepartmentList/Department">
							<Department>
								<xsl:attribute name="DepartmentCode">
									<xsl:value-of select="@DepartmentCode"/>
								</xsl:attribute>
							</Department>
						</xsl:for-each>
					</DepartmentList>
				</StoreBatch>
			</xsl:for-each>	
		</StoreBatchList>
	</xsl:template>
</xsl:stylesheet>