<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="/Shipment">
        <xsl:element name="Demand">
            <xsl:attribute name="OrderNo">
               <xsl:value-of select="ShipmentLines/ShipmentLine/@OrderNo"/>
            </xsl:attribute>
			 <xsl:attribute name="OrderHeaderKey">
               <xsl:value-of select="ShipmentLines/ShipmentLine/@OrderHeaderKey"/>
            </xsl:attribute>
             <xsl:attribute name="DocumentType">
               <xsl:text>0001</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="EnterpriseCode">
            	<xsl:text>Academy_Direct</xsl:text>
            </xsl:attribute>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>