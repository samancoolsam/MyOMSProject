<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:output indent="yes"/>
    <xsl:template match="Shipment">
        <MultiApi>
			<xsl:element name="API">
				<xsl:attribute name="Name">
					<xsl:text>getRuleList</xsl:text>
				</xsl:attribute>
				<xsl:element name="Input">    
			    		<xsl:element name="Rules">				    
				    		<xsl:attribute name="OrganizationCode">
							<xsl:value-of select="@ShipNode"/>
				    		</xsl:attribute>
					</xsl:element>
				</xsl:element>
				<Template>
					<RuleList>
					   <Rules DocumentType="" OrganizationCode="" RuleSetFieldDescription="" RuleSetFieldName="" RuleSetValue="" RulesKey=""/>
					</RuleList>
				</Template>                                 
			</xsl:element>

			<xsl:element name="API">
				<xsl:attribute name="Name">
					<xsl:text>getShipmentDetails</xsl:text>
				</xsl:attribute>
				<xsl:element name="Input">    
			    	<xsl:element name="Shipment">				    
				    	<xsl:attribute name="ShipmentKey">
							<xsl:value-of select="@ShipmentKey"/>
				    	</xsl:attribute>
				</xsl:element>
				</xsl:element>
				<Template>	
					   <Shipment ShipmentKey="" IsBackroomPickRequired="" DocumentType=""/>
				</Template>                                 
			</xsl:element>
		   </MultiApi>
    </xsl:template>
    
</xsl:stylesheet>
