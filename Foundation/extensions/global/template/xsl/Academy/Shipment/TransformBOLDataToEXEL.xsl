<?xml version='1.0' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:template match="/">
      <Shipment>
         <xsl:copy-of select="AcademyMergedDocument/EnvironmentDocument/Shipment/@*" />

         <ToAddress>
            <xsl:copy-of select="AcademyMergedDocument/EnvironmentDocument/Shipment/ToAddress/@*" />
         </ToAddress>

         <FromAddress>
            <xsl:copy-of select="AcademyMergedDocument/EnvironmentDocument/Shipment/FromAddress/@*" />
         </FromAddress>

         <BillToAddress>
            <xsl:copy-of select="AcademyMergedDocument/EnvironmentDocument/Shipment/BillToAddress/@*" />
         </BillToAddress>

         <OrderInvoiceList>
            <xsl:copy-of select="AcademyMergedDocument/EnvironmentDocument/Shipment/OrderInvoiceList/*" />
         </OrderInvoiceList>

         <ShipmentLines>
            <xsl:copy-of select="AcademyMergedDocument/EnvironmentDocument/Shipment/ShipmentLines/*" />
         </ShipmentLines>

         <AgentAddressLocation>

         	<Name>
				<xsl:value-of select="//PierbridgeRateResponse/Agent/Name" />
         	</Name>

         	<CompanyName>
				<xsl:value-of select="//PierbridgeRateResponse/Agent/CompanyName" />
         	</CompanyName>

         	<Street>
				<xsl:value-of select="//PierbridgeRateResponse/Agent/Street" />
         	</Street>

         	<Street2>
				<xsl:value-of select="//PierbridgeRateResponse/Agent/Street2" />
         	</Street2>

         	<City>
				<xsl:value-of select="//PierbridgeRateResponse/Agent/City" />
         	</City>

         	<Region>
				<xsl:value-of select="//PierbridgeRateResponse/Agent/Region" />
         	</Region>

         	<PostalCode>
				<xsl:value-of select="//PierbridgeRateResponse/Agent/PostalCode" />
         	</PostalCode>

         	<Phone>
				<xsl:value-of select="//PierbridgeRateResponse/Agent/Phone" />
         	</Phone>

         	<Fax>
				<xsl:value-of select="//PierbridgeRateResponse/Agent/Fax" />
         	</Fax>

         	<FUCode>
				<xsl:value-of select="//PierbridgeRateResponse/Agent/FUCode" />
         	</FUCode>
            
         	<Locale>
				<xsl:value-of select="//PierbridgeRateResponse/Agent/Locale" />
         	</Locale>

         	<Country>
				<xsl:value-of select="//PierbridgeRateResponse/Agent/Country" />
         	</Country>

         	<Other>
				<xsl:value-of select="//PierbridgeRateResponse/Agent/Other" />
         	</Other>

         </AgentAddressLocation>
      </Shipment>
   </xsl:template>
</xsl:stylesheet>

