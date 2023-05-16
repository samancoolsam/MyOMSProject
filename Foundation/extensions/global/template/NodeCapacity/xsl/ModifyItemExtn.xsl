<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes"/>
	<xsl:template match="/">               
		<ItemList>
			<xsl:for-each select="/ItemList/Item">
			<Item>
			<xsl:attribute name="Action">
                <xsl:value-of select="'Modify'"/>
            </xsl:attribute>
			<xsl:attribute name="ItemID">
                <xsl:value-of select="@ItemID"/>
            </xsl:attribute>
			<xsl:attribute name="UnitOfMeasure">
                <xsl:value-of select="'EACH'"/>
            </xsl:attribute>
			<xsl:attribute name="OrganizationCode">
                <xsl:value-of select="'DEFAULT'"/>
            </xsl:attribute>
			<Extn>
			<xsl:attribute name="ExtnWatermark">
                <xsl:value-of select="@ExtnWatermark"/>
            </xsl:attribute>
			</Extn>
			</Item>
			</xsl:for-each>
		</ItemList>
	</xsl:template>
</xsl:stylesheet>