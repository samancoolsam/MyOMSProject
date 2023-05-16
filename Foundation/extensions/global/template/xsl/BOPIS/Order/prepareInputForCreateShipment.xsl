<?xml version='1.0' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
	
		<Shipment>
			<xsl:attribute name="Action"><xsl:text>Create</xsl:text></xsl:attribute>
			<xsl:attribute name="DocumentType"><xsl:text>0001</xsl:text></xsl:attribute>
			<xsl:attribute name="EnterpriseCode"><xsl:text>Academy_Direct</xsl:text></xsl:attribute>
			<xsl:variable name="OrderReleaseKey">
				<xsl:value-of select="OrderRelease/@OrderReleaseKey" />
			</xsl:variable>
			<xsl:attribute name="ShipNode">
				<xsl:value-of select="OrderRelease/@ShipNode"/>
			</xsl:attribute>
			<!-- Adding OrderDate as its required in createShipment UE for Calculating Bopis SLA::Begin -->
			<xsl:attribute name="OrderDate">
				<xsl:value-of select="OrderRelease/@OrderDate"/>
			</xsl:attribute>
			<!-- Adding OrderDate as its required in createShipment UE for Calculating Bopis SLA::End -->
			<xsl:attribute name="DeliveryMethod">
				<xsl:value-of select="OrderRelease/@DeliveryMethod"/>
			</xsl:attribute>
			<ShipmentLines>
			<xsl:for-each select="OrderRelease/OrderLine">
				<ShipmentLine>
					<xsl:attribute name="OrderLineKey">
						<xsl:value-of select="@OrderLineKey"/>
					</xsl:attribute>
					<xsl:attribute name="Quantity">
						<xsl:value-of select="@OrderedQty"/>
					</xsl:attribute>
					<xsl:attribute name="OrderReleaseKey">
						<xsl:value-of select="$OrderReleaseKey"/>
					</xsl:attribute>
					<xsl:attribute name="OrderHeaderKey">
						<xsl:value-of select="@OrderHeaderKey"/>
					</xsl:attribute>
				</ShipmentLine>
			</xsl:for-each>	
			</ShipmentLines>
		</Shipment>
	</xsl:template>
</xsl:stylesheet>