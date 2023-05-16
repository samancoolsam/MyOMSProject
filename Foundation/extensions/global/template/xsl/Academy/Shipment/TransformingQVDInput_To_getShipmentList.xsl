<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="/">
        <xsl:element name="Shipment">
            <xsl:attribute name="EnterpriseCode">
                <xsl:text>Academy_Direct</xsl:text>
             </xsl:attribute>
	     <xsl:attribute name="ShipNode">
                <xsl:text></xsl:text>
             </xsl:attribute>
             <xsl:attribute name="DocumentType">
                <xsl:text>0001</xsl:text>
             </xsl:attribute> 
             
	      <!--<xsl:attribute name="Status">
                <xsl:text>1400</xsl:text>
             </xsl:attribute> -->
            <!-- <xsl:element name="Containers">
              <xsl:element name="Container"> -->
             <Containers>
             <Container>
             <xsl:attribute name="TrackingNo">
            <xsl:value-of select="./Shipment/@TrackingNo"/>
             </xsl:attribute>
             </Container>
             </Containers>
              </xsl:element>
   </xsl:template>
</xsl:stylesheet>
