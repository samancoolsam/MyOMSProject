<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:currenttimestamp="http://exslt.org/dates-and-times"
	extension-element-prefixes="currenttimestamp">
	<xsl:template match="/">
		<xsl:element name="Shipment">
			<xsl:attribute name="ShipmentNo">
  <xsl:value-of select="Shipment/@ShipmentNo" /> 
  </xsl:attribute>
			<xsl:attribute name="ShipNode">
  <xsl:value-of select="Shipment/@ShipNode" /> 
  </xsl:attribute>
			<xsl:attribute name="CarrierServiceCode">
  <xsl:value-of select="Shipment/@CarrierServiceCode" /> 
  </xsl:attribute>
			<Extn>
				<xsl:attribute name="MessageType">
  <xsl:value-of select="'XTsfMod'" /> 
  </xsl:attribute>
				<xsl:attribute name="RMSTransferNo">
  <xsl:value-of select="Shipment/Extn/@ExtnRMSTransferNo" /> 
  </xsl:attribute>
				<xsl:attribute name="FromLocationType">
  <xsl:value-of select="'W'" /> 
  </xsl:attribute>
				<xsl:attribute name="ToLocationType">
  <xsl:value-of select="'S'" /> 
  </xsl:attribute>
<!-- Start : OMNI-6372 : STS Shipment message Changes -->
	<xsl:variable name="varReceivingNode" select="/Shipment/@ReceivingNode" />
	<xsl:if test="$varReceivingNode != '' ">
		<xsl:attribute name="TransferToLocation">
				<xsl:value-of select="/Shipment/@ReceivingNode" />
		</xsl:attribute>
	</xsl:if>
	<xsl:if test="$varReceivingNode = ' ' or $varReceivingNode = ''">
		<xsl:attribute name="TransferToLocation">
  				<xsl:value-of select="'5'" /> 
  		</xsl:attribute>
	</xsl:if>
<!-- Start : OMNI-6372 : STS Shipment message Changes -->
				<xsl:attribute name="MsgTransmissionDate">
  <xsl:value-of select="currenttimestamp:date-time()" /> 
  </xsl:attribute>
<!-- Start : OMNI-6372 : Update TransferType for STS Orders -->
	<xsl:variable name="varDocumentType" select="/Shipment/@DocumentType" />
	<xsl:if test="$varDocumentType ='0006'" >
		<xsl:attribute name="TransferType">
				<xsl:value-of select="'AIP'" /> 
		</xsl:attribute>
	</xsl:if>
	<xsl:if
		test="$varDocumentType ='0001' or $varDocumentType = ' ' or $varDocumentType = ''">
		<xsl:attribute name="TransferType">
  				<xsl:value-of select="'EG'" /> 
  		</xsl:attribute>
	</xsl:if>
<!-- End : OMNI-6372 : Update TransferType for STS Orders -->
				<xsl:attribute name="TransferStatus">
  <xsl:value-of select="'C'" /> 
  </xsl:attribute>
				<xsl:attribute name="UserId">
  <xsl:value-of select="'Sterling'" /> 
  </xsl:attribute>
<xsl:attribute name="freight_code">
                	<xsl:text>N</xsl:text>
            	</xsl:attribute> 
			</Extn>
			<ShipmentLines>
				<xsl:for-each select="/Shipment/ShipmentLines/ShipmentLine">
					<ShipmentLine>
						<xsl:attribute name="ShipmentLineNo">
                		<xsl:value-of select="@ShipmentLineNo" />
            		</xsl:attribute>
						<xsl:attribute name="ItemID">
                		<xsl:value-of select="@ItemID" />
            		</xsl:attribute>
						<xsl:attribute name="Quantity">
                		<xsl:value-of select="@Quantity" />
            		</xsl:attribute>
					</ShipmentLine>
				</xsl:for-each>
			</ShipmentLines>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>