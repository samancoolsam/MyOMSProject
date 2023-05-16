<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="/Shipment">
    <xsl:variable name="ReceivingNode" select="@ReceivingNode" />
	<xsl:variable name="ShipNode" select="@ShipNode" />
        <xsl:element name="Shipment">
            <xsl:attribute name="OrderNo">
               <xsl:value-of select="/Shipment/ShipmentLines/ShipmentLine/OrderLine/ChainedFromOrderLine/Order/@OrderNo"/>
            </xsl:attribute>
			 <xsl:attribute name="ReceivingNode">
               <xsl:value-of select="@ReceivingNode"/>
            </xsl:attribute>
             <xsl:attribute name="ShipNode">
               <xsl:value-of select="@ShipNode"/>
            </xsl:attribute>
            <xsl:attribute name="ShipmentNo">
               <xsl:value-of select="@ShipmentNo"/>
            </xsl:attribute>
<xsl:element name="Containers">
<xsl:for-each select="/Shipment/Containers/Container">
 <xsl:element name="Container">
 <xsl:attribute name="ContainerNo">
               <xsl:value-of select="@ContainerNo"/>
            </xsl:attribute>
             <xsl:attribute name="ReceivingNode">
               <xsl:value-of select="$ReceivingNode"/>
            </xsl:attribute>
             <xsl:attribute name="ShipNode">
               <xsl:value-of select="$ShipNode"/>
            </xsl:attribute>
 <xsl:element name="ContainerDetails">
 <xsl:for-each select="./ContainerDetails/ContainerDetail">
  <xsl:element name="ContainerDetail">
  <xsl:attribute name="ItemID">
               <xsl:value-of select="@ItemID"/>
            </xsl:attribute>
             <xsl:attribute name="Quantity">
               <xsl:value-of select="@Quantity"/>
            </xsl:attribute>
  </xsl:element> </xsl:for-each> </xsl:element> 
             
  </xsl:element></xsl:for-each>
        </xsl:element> </xsl:element>
        
    </xsl:template>
    
</xsl:stylesheet>