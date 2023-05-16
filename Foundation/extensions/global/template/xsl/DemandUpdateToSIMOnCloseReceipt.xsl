<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="Container/Shipment">
    <xsl:variable name="ReceivingNode" select="@ReceivingNode" />
	<xsl:variable name="ShipNode" select="@ShipNode" />
        <xsl:element name="Shipment">
            <xsl:attribute name="OrderNo">
               <xsl:value-of select="/Container/ContainerDetails/ContainerDetail/ShipmentLine/OrderLine/ChainedFromOrderLine/Order/@OrderNo"/>
            </xsl:attribute>
			 <xsl:attribute name="ReceivingNode">
               <xsl:value-of select="$ReceivingNode"/>
            </xsl:attribute>
             <xsl:attribute name="ShipNode">
               <xsl:value-of select="$ShipNode"/>
            </xsl:attribute>
            <xsl:attribute name="ShipmentNo">
               <xsl:value-of select="@ShipmentNo"/>
            </xsl:attribute>
            <!-- OMNI-66585 Start -->
            <xsl:if test="(/Container/@ShrinkFlag)!=''">
             <xsl:attribute name="ShrinkFlag">
               <xsl:value-of select="/Container/@ShrinkFlag"/>
            </xsl:attribute>
            </xsl:if>
            <!-- OMNI-66585 End -->
            
<xsl:element name="Containers">
 <xsl:element name="Container">
 <xsl:attribute name="ContainerNo">
               <xsl:value-of select="/Container/@ContainerNo"/>
            </xsl:attribute>
             <xsl:attribute name="ReceivingNode">
               <xsl:value-of select="$ReceivingNode"/>
            </xsl:attribute>
             <xsl:attribute name="ShipNode">
               <xsl:value-of select="$ShipNode"/>
            </xsl:attribute>
 <xsl:element name="ContainerDetails">
 <xsl:for-each select="/Container/ContainerDetails/ContainerDetail">
  <xsl:element name="ContainerDetail">
  <xsl:attribute name="ItemID">
               <xsl:value-of select="@ItemID"/>
            </xsl:attribute>
             <xsl:attribute name="Quantity">
               <xsl:value-of select="@Quantity"/>
            </xsl:attribute>
  </xsl:element> </xsl:for-each> </xsl:element> 
             
  </xsl:element> </xsl:element> 
        </xsl:element>
         
       </xsl:template>
    
</xsl:stylesheet>