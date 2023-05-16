<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
<InventoryItem>
 <xsl:for-each select="InventoryItem/AvailabilityChange">
 <AvailabilityChange>
  	<xsl:attribute name="AlertRaisedOn">
                <xsl:value-of select="@AlertRaisedOn"/>
	</xsl:attribute>
	<xsl:attribute name="FirstFutureAvailableDate">
                <xsl:value-of select="@FirstFutureAvailableDate"/>
	</xsl:attribute>
	<xsl:attribute name="FutureAvailableQuantity">
                <xsl:value-of select="@FutureAvailableQuantity"/>
	</xsl:attribute>
	<xsl:attribute name="MonitorOption">
                <xsl:value-of select="@MonitorOption"/>
	</xsl:attribute>
	<xsl:attribute name="Node">
                <xsl:value-of select="@Node"/>
	</xsl:attribute>
	<xsl:attribute name="OnhandAvailableDate">
                <xsl:value-of select="@OnhandAvailableDate"/>
	</xsl:attribute>
	<xsl:attribute name="OnhandAvailableQuantity">
                <xsl:value-of select="@OnhandAvailableQuantity"/>
	</xsl:attribute>
	<Item>
		<xsl:attribute name="ItemID">
                <xsl:value-of select="../@ItemID"/>
	</xsl:attribute>
	</Item>
</AvailabilityChange>
</xsl:for-each>
</InventoryItem>
</xsl:template>
</xsl:stylesheet>
