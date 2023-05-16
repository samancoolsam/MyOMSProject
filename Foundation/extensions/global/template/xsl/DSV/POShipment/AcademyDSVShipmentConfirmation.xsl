<?xml version = "1.0" encoding = "UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output indent="yes" />
	<xsl:template match="/Shipment">
		<xsl:element name="Shipment">
			<xsl:attribute name="OrderNo">
				<xsl:value-of select="./@OrderNo" />
			</xsl:attribute>
			<xsl:attribute name="ShipmentKey">
				<xsl:value-of select="./@ShipmentKey" />
			</xsl:attribute>
			<xsl:attribute name="MessageType">
				<xsl:text>DSDReceipt</xsl:text>
			</xsl:attribute>
				
		<ShipmentLines>				
			<xsl:for-each select="/Shipment/ShipmentLines/ShipmentLine">
				<ShipmentLine>
					<xsl:attribute name="ItemID">
						<xsl:value-of select="@ItemID" />
					</xsl:attribute>
					<xsl:attribute name="Quantity">
						<xsl:value-of select="@Quantity" />
					</xsl:attribute>
					<xsl:attribute name="ShipmentLineNo">
						<xsl:value-of select="@ShipmentLineNo" />
					</xsl:attribute>
				</ShipmentLine>
			</xsl:for-each>
		</ShipmentLines>
</xsl:element>				
	</xsl:template>
</xsl:stylesheet>