<?xml version='1.0' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:template match="/">
      <Shipment>
         <xsl:copy-of select="Shipment/@*" />

         <Containers>
            <xsl:for-each select="Shipment/Containers/Container">
               <Container>
                  <xsl:copy-of select="./@*" />

                  <ContainerDetails>
                     <xsl:for-each select="./ContainerDetails/ContainerDetail">
                        <ContainerDetail>
                           <xsl:copy-of select="./@*" />
                        </ContainerDetail>
                     </xsl:for-each>
                  </ContainerDetails>
               </Container>
            </xsl:for-each>
         </Containers>

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