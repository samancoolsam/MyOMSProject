<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="/Shipment">
        <xsl:element name="Item">
            <xsl:attribute name="ItemID">
                <xsl:value-of select="ShipmentLines/ShipmentLine/@ItemID"/>
            </xsl:attribute>
            <xsl:attribute name="OrganizationCode">
                <xsl:text>Academy_Direct</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="UnitOfMeasure">
               <xsl:text>EACH</xsl:text>
            </xsl:attribute>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>