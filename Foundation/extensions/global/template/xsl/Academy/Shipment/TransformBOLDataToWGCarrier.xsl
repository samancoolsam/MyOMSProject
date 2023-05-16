<?xml version='1.0' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:template match="/">
      <Shipment>
         <xsl:copy-of select="Shipment/@*" />

         <ToAddress>
            <xsl:copy-of select="Shipment/ToAddress/@*" />
         </ToAddress>

         <FromAddress>
            <xsl:copy-of select="Shipment/FromAddress/@*" />
         </FromAddress>

         <BillToAddress>
            <xsl:copy-of select="Shipment/BillToAddress/@*" />
         </BillToAddress>

         <OrderInvoiceList>
            <xsl:copy-of select="Shipment/OrderInvoiceList/*" />
         </OrderInvoiceList>

         <ShipmentLines>
            <xsl:copy-of select="Shipment/ShipmentLines/*" />
         </ShipmentLines>
      </Shipment>
   </xsl:template>
</xsl:stylesheet>
