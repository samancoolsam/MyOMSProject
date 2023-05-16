<?xml version="1.0" ?> 
<!--This XSL will form the input to getOrderReleaseList API -->
<!-- start DSV  09/27/2012 -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
<Order>
    <xsl:attribute name="OrderNo">
    <xsl:value-of select="OrderRelease/@OrderName"/>
    </xsl:attribute>
   </Order> 
    </xsl:template>
</xsl:stylesheet>
