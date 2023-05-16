<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- 
This XSL was added as part of OMNI-2821 : Narvar: Send Narvar ASO Promise Date 
This XSL converts the ExtnInitialPromiseDate filed to Narvar readable format
-->
<xsl:template match="Shipment">
<Shipment>
<xsl:copy-of select="@*"/>
<xsl:copy-of select = "node()[not(name(.)='ShipmentLines')]" />
<ShipmentLines>
<xsl:for-each select='/Shipment/ShipmentLines/ShipmentLine'>
<ShipmentLine>
<xsl:copy-of select="@*"/>
<xsl:copy-of select= "node()[not(name(.)='OrderLine')]"/>
<xsl:for-each select='./OrderLine'>
<OrderLine>
<xsl:copy-of select="@*"/>
<xsl:copy-of select="node()[not(name(.)='Extn')]"/>
<Extn>
<xsl:copy-of select="./Extn/@*[not(name()='ExtnInitialPromiseDate')]"/>
<xsl:variable name="extnInitialPromiseDate" select="./Extn/@ExtnInitialPromiseDate"/>
<xsl:attribute name="ExtnInitialPromiseDate">
<xsl:if test="$extnInitialPromiseDate !='' ">
<xsl:value-of select="substring($extnInitialPromiseDate, 1, 10)" />
</xsl:if>
</xsl:attribute>
</Extn>
</OrderLine>
</xsl:for-each>
</ShipmentLine>
</xsl:for-each>
</ShipmentLines>
</Shipment>
</xsl:template>
</xsl:stylesheet>
