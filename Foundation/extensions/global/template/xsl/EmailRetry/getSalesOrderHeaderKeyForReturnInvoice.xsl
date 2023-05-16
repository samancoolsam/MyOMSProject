<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output indent="yes" />
   <xsl:template match="/">
   
   <xsl:element  name = "Order">
   
   <xsl:attribute  name = "OrderHeaderKey" >
   <xsl:value-of select="/InvoiceDetail/InvoiceHeader/Order/OrderLines/OrderLine[1]/@DerivedFromOrderHeaderKey" />
   </xsl:attribute>
   </xsl:element>
   </xsl:template>
   </xsl:stylesheet>