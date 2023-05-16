<?xml version='1.0' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:template match="/">
      <Order>
         <xsl:copy-of select="Order/@*" />

			<OrderLines>
				<xsl:copy-of select="Order/OrderLines/*" />
			</OrderLines>
	         <PersonInfoShipTo>
	            <xsl:copy-of select="Order/PersonInfoShipTo/@*" />
	         </PersonInfoShipTo>
	
	         <PersonInfoBillTo>
	            <xsl:copy-of select="Order/PersonInfoBillTo/@*" />
	         </PersonInfoBillTo>
	
	         <PersonInfoMarkFor>
	            <xsl:copy-of select="Order/PersonInfoMarkFor/@*" />
	         </PersonInfoMarkFor>

	         <PersonInfoContact>
	            <xsl:copy-of select="Order/PersonInfoContact/@*" />
	         </PersonInfoContact>
	         
	         <ToAddress>
	         	<xsl:copy-of select="Order/CorporatePersonInfo/@*"/>
	         </ToAddress>

	         <OrderInvoiceList>
	            <xsl:copy-of select="Order/OrderInvoiceList/*" />
	         </OrderInvoiceList>

	
	        <!-- <AgentAddressLocation>
	
	         	<Name>
					<xsl:value-of select="//PierbridgeRateResponse/Agent/Name" />
	         	</Name>
	
	         	<Company>
					<xsl:value-of select="//PierbridgeRateResponse/Agent/CompanyName" />
	         	</Company>
	
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
	
	         </AgentAddressLocation>-->
	         
      </Order>
   </xsl:template>
</xsl:stylesheet>

