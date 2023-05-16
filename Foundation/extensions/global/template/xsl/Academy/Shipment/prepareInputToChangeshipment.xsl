<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="/AcademyMergedDocument">
        <xsl:element name="Shipment">
                                                            
            <xsl:attribute name="SellerOrganizationCode">
                <xsl:text>Academy_Direct</xsl:text>
				
             </xsl:attribute>
                     <xsl:attribute name="ShipNode">
                <xsl:value-of select="InputDocument/Shipments/Shipment/@ShipNode"/>
             </xsl:attribute>
			 
             <xsl:attribute name="ShipmentKey">
                <xsl:value-of select="InputDocument/Shipments/Shipment/@ShipmentKey"/>
             </xsl:attribute> 
			 
            <xsl:attribute name="ShipmentNo">
             <xsl:value-of select="InputDocument/Shipments/Shipment/@ShipmentNo"/>
             </xsl:attribute> 
			 
            <xsl:attribute name="Status">
             <xsl:value-of select="InputDocument/Shipments/Shipment/@Status"/>
             </xsl:attribute>  
			 
            <Containers>
             <Container>
			 
              <xsl:for-each select="InputDocument/Shipments/Shipment/Containers/Container">
              <xsl:if test="/AcademyMergedDocument/EnvironmentDocument/Shipment/@TrackingNo = @TrackingNo">
			                                                  
			<xsl:attribute name="ContainerNo">
            <xsl:value-of select="@ContainerNo"/>
            </xsl:attribute>
			
            <xsl:attribute name="ShipmentContainerKey">
            <xsl:value-of select="@ShipmentContainerKey"/>
            </xsl:attribute>
			
            <xsl:attribute name="TrackingNo">
            <xsl:value-of select="@TrackingNo"/>
            </xsl:attribute>    
			
             <xsl:attribute name="Status">
             <xsl:text>1300.100</xsl:text>
             </xsl:attribute>
			 
             </xsl:if>
             </xsl:for-each>
			 
             </Container>
             </Containers>
                                                
              </xsl:element>
   </xsl:template>
</xsl:stylesheet>
