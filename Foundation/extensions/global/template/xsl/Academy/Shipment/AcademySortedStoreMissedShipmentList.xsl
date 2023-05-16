<?xml version = "1.0" encoding = "UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/Shipments">
		<Shipments>
			<xsl:copy-of select="./@*"/>
			<PageData>
				<xsl:copy-of select="./PageData/@*"/>
			</PageData>
			<xsl:for-each select="/Shipments/Shipment">
				<xsl:sort select="@SortDate" data-type="number" order="descending"/>
				<Shipment>
					<xsl:copy-of select="./@*"/>
					<xsl:copy-of select="node()"/>
				</Shipment>
			</xsl:for-each>
		</Shipments>
	</xsl:template>
</xsl:stylesheet>