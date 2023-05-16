<?xml version='1.0'?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/Shipment">
		<PrintDocuments>
			<xsl:attribute name="FlushToPrinter">Y</xsl:attribute>
			<xsl:attribute name="PrintName">AcademyPrintPendingShipments</xsl:attribute>
			<PrintDocument>
				<xsl:attribute name="BeforeChildrenPrintDocumentId">
					<xsl:value-of select="@BeforeChildrenPrintDocumentId"/>
				</xsl:attribute>
				<xsl:attribute name="DataElementPath">xml:/Shipment</xsl:attribute>
				<InputData>
					<Shipment BeforeChildrenPrintDocumentId="{@BeforeChildrenPrintDocumentId}" CarrierServiceCode="{@CarrierServiceCode}" PageNumber="{@PageNumber}" PickticketNo="{@PickticketNo}" 
					PrinterId="{@PrinterId}" SCAC="{@SCAC}" ShipNode="{@ShipNode}" ShipmentKey="{@ShipmentKey}" ShipmentNo="{@ShipmentNo}">
						<ShipmentLines>
							<xsl:for-each select="ShipmentLines/ShipmentLine">
								<xsl:variable name="VarPogId" select="Extn/@ExtnPogId"/>
								<xsl:variable name="VarDepartment" select="Extn/@ExtnDepartment"/>
								<xsl:variable name="VarSection" select="Extn/@ExtnSection"/>
								<xsl:variable name="VarPogNumber" select="Extn/@ExtnPogNumber"/>
								<xsl:variable name="VarLiveDate" select="Extn/@ExtnLiveDate"/> 
								<ShipmentLine OrderNo="{@OrderNo}" Quantity="{@Quantity}" PogId="{$VarPogId}" Department="{$VarDepartment}" Section="{$VarSection}"
								 PogNumber="{$VarPogNumber}" LiveDate="{$VarLiveDate}" AvailableStockOnHand="{@AvailableStockOnHand}" ItemPrice="{@ItemPrice}" LastReceivedDate="{@LastReceivedDate}">
								 <OrderLine OrderLineKey="{OrderLine/@OrderLineKey}">
								 	<Item ItemID="{OrderLine/Item/@ItemID}" ItemBarcode="{OrderLine/Item/@ItemBarcode}" ItemShortDesc="{OrderLine/Item/@ItemShortDesc}" ImageURL="{OrderLine/Item/@ImageURL}" LongSKU="{OrderLine/Item/@LongSKU}" 
								 	TotalStock="{OrderLine/Item/@TotalStock}"/>
								 </OrderLine>
								</ShipmentLine> 
							</xsl:for-each>
						</ShipmentLines>
					</Shipment>
				</InputData>
				<PrinterPreference>
					<xsl:attribute name="PrinterId">
						<xsl:value-of select="@PrinterId"/>
					</xsl:attribute>
					<xsl:attribute name="OrganizationCode">
						<xsl:value-of select="@ShipNode"/>
					</xsl:attribute>
				</PrinterPreference>
			</PrintDocument>
		</PrintDocuments>
	</xsl:template>
</xsl:stylesheet>