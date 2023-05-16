<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/Shipment">
    <Shipment>
		<xsl:attribute name="FromShipNode">
			<xsl:text>005</xsl:text>
		</xsl:attribute>
		<xsl:attribute name="ShipmentNo">
			<xsl:value-of select="@ShipmentNo"/>
		</xsl:attribute>
		<xsl:attribute name="ToShipNode">
			<xsl:value-of select="@ShipNode"/>
		</xsl:attribute>						
        <ShipmentLines>
		<!--loop through all the shipment line element-->
            <xsl:for-each select="ShipmentLines/ShipmentLine">                     
	            <ShipmentLine>	            
	                <xsl:attribute name="ItemID">
						<xsl:value-of select="@ItemID"/>
					</xsl:attribute>
					<xsl:attribute name="ItemUPCCode">
						<xsl:value-of select="@GlobalItemID"/>
					</xsl:attribute>
					<xsl:attribute name="ProductClass">
						<xsl:value-of select="@ProductClass"/>
					</xsl:attribute>
					<xsl:attribute name="Quantity">
						<xsl:value-of select="@ActualQuantity"/>
					</xsl:attribute>
					<xsl:attribute name="UnitOfMeasure">
						<xsl:value-of select="@UnitOfMeasure"/>
					</xsl:attribute>
					<xsl:attribute name="UnitPrice">
						<xsl:value-of select="OrderLine/LinePriceInfo/@UnitPrice"/>
					</xsl:attribute>								
				</ShipmentLine >
            </xsl:for-each>                   
        </ShipmentLines>              
     </Shipment>    
</xsl:template>
</xsl:stylesheet>


