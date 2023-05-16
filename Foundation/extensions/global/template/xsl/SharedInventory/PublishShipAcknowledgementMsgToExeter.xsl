<?xml version="1.0" encoding="UTF-8" ?>
<!--This xsl will filter the ADD_TO_CONTAINER.ON_CONTAINER_PACK_PROCESS_COMPLETE event xml
to the Exeter required format -->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:currenttimestamp="http://exslt.org/dates-and-times"
	extension-element-prefixes="currenttimestamp">
	<xsl:template match="/">
		<xsl:element name="Shipment">
			<xsl:attribute name="ShipmentNo">
  <xsl:value-of select="Container/Shipment/@ShipmentNo" /> 
  </xsl:attribute>
			<xsl:attribute name="ShipNode">
  <xsl:value-of select="Container/Shipment/@ShipNode" /> 
  </xsl:attribute>
			<Extn>
				<xsl:attribute name="MessageType">
  <xsl:value-of select="'ShpAck'" /> 
  </xsl:attribute>
				<xsl:attribute name="RMSTransferNo">
  <xsl:value-of select="Container/Shipment/Extn/@ExtnRMSTransferNo" /> 
  </xsl:attribute>
				<xsl:attribute name="ContainerId">
  <xsl:value-of select="Container/Shipment/Extn/@ExtnExeterContainerId" /> 
  </xsl:attribute>
				<xsl:attribute name="Receipt_date">
  <xsl:value-of select="currenttimestamp:date-time()" /> 
  </xsl:attribute>
			</Extn>
			<ShipmentLines>
				<xsl:for-each select="/Container/ContainerDetails/ContainerDetail">
					<ShipmentLine>
						<xsl:attribute name="ShipmentLineNo">
  <xsl:value-of select="ShipmentLine/@ShipmentLineNo" /> 
  </xsl:attribute>
						<xsl:attribute name="ItemID">
  <xsl:value-of select="ShipmentLine/@ItemID" /> 
  </xsl:attribute>
							<xsl:attribute name="Quantity">
  <xsl:value-of select="ShipmentLine/@Quantity" /> 
  </xsl:attribute>
					</ShipmentLine>
				</xsl:for-each>
			</ShipmentLines>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>