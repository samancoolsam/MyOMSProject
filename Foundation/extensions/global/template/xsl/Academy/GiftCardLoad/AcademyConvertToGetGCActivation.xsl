<?xml version='1.0' encoding='utf-8' ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
   <xsl:output method="xml" />

   <xsl:template match="/">
      <Serials>
         <xsl:for-each select="/AcademyMergedDocument/InputDocument/ShipmentLines/ShipmentLine/ContainerDetails/ContainerDetail/ShipmentTagSerials/ShipmentTagSerial">
         <GiftCardActivation>
                     <xsl:attribute name="SvcNo">
                      <xsl:value-of select="./@SerialNo" />
                     </xsl:attribute>
                     <xsl:attribute name="RequestAmount">
                        <xsl:value-of select="/AcademyMergedDocument/EnvironmentDocument/ShipmentLine/@UnitPrice" />
                     </xsl:attribute>

                     <xsl:attribute name="ShipmentKey">
                        <xsl:value-of select="/AcademyMergedDocument/InputDocument/ShipmentLines/ShipmentLine/Shipment/@ShipmentKey" />
                     </xsl:attribute>

                     <xsl:attribute name="ShipmentLineKey">
                        <xsl:value-of select="/AcademyMergedDocument/InputDocument/ShipmentLines/ShipmentLine/@ShipmentLineKey" />
                     </xsl:attribute>

                     <xsl:attribute name="Status">
                        <xsl:value-of select="/AcademyMergedDocument/InputDocument/ShipmentLines/ShipmentLine/Shipment/@Status" />
                     </xsl:attribute>

                     <xsl:attribute name="ShipmentNo">
                        <xsl:value-of select="/AcademyMergedDocument/InputDocument/ShipmentLines/ShipmentLine/Shipment/@ShipmentNo" />
                     </xsl:attribute>
                     <xsl:attribute name="ChargeType">
                        <xsl:value-of select="'ActivatePhysicalCard'" />
                     </xsl:attribute>
		
                     <!--commented as no bulk activation flow use this XSL.
					 <xsl:attribute name="checkIsBulkGC">
                        <xsl:value-of select="'N'" />
                     </xsl:attribute>       -->           

				<xsl:attribute name="OrderNo">
                        <xsl:value-of select="/AcademyMergedDocument/InputDocument/ShipmentLines/ShipmentLine/@OrderNo" />
                     </xsl:attribute> 
         </GiftCardActivation>
         </xsl:for-each>
      </Serials>
   </xsl:template>
</xsl:stylesheet>