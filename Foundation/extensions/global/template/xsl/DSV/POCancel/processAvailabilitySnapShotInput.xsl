<?xml version="1.0" ?> 
<!-- 
The xml from this xsl will be the input to processAvailabilitySnapShot API which markes the node 'Dirty' 
i.e inventory for that item is made '0' for that specific node 
Input to this XSL will be the output of CustomMultiAPI component
Sample XML format
<CustomMultiAPI>
<AcademyGetOrderReleaseList>
<Input>
</Input>
<Output>
</Output>
</AcademyGetOrderReleaseList>
</CustomMultiAPI>  -->
<!-- start DSV  09/27/2012 -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
<AvailabilitySnapShot>
<ShipNode>
<xsl:attribute name="ShipNode"><xsl:value-of select="CustomMultiAPI/AcademyGetOrderReleaseList/Output/OrderReleaseList/OrderRelease/@ShipNode"/></xsl:attribute>
<!-- loop thru every orderline  anf fetch the item ID to make the ONHAND Inventory for the item and ShipNode Combination as 0 
InventoryOrganizationCode,Quantity,SupplyType,ProductCode and UOM are hardcoded with the below values -->
<xsl:for-each select="CustomMultiAPI/AcademyGetOrderReleaseList/Input/OrderRelease/OrderLines/OrderLine">
<xsl:if test="Notes/Note/@ReasonCode= '902'">
<Item>
<xsl:attribute name="InventoryOrganizationCode">DEFAULT</xsl:attribute>
<xsl:attribute name="ItemID"><xsl:value-of select="Item/@ItemID" /></xsl:attribute>
<xsl:attribute name="ProductClass">GOOD</xsl:attribute>
<xsl:attribute name="UnitOfMeasure">EACH</xsl:attribute>
<AvailabilityDetails>
<xsl:attribute name="Quantity">0</xsl:attribute>
<xsl:attribute name="SupplyType">ONHAND</xsl:attribute>
</AvailabilityDetails>
</Item>
</xsl:if>
</xsl:for-each>
</ShipNode>
</AvailabilitySnapShot>
</xsl:template>
</xsl:stylesheet>