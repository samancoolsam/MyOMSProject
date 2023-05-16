<?xml version="1.0" encoding="UTF-8"?>

<!-- STL-947-Shared Inventory:: Preparing the Message which will publish the message to RMS sysstem from Sterling-->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:currenttimestamp="http://exslt.org/dates-and-times"
	extension-element-prefixes="currenttimestamp">
	<xsl:template match="/">

		<Shipment>
			<xsl:attribute name="ShipmentNo">
                <xsl:value-of select="/Shipment/@ShipmentNo"/>
            </xsl:attribute> 
            
			<!-- START WN-416 SI: Shipment creation msg sent to Exeter WMS w/ WG attribute -->
            <xsl:attribute name="ShipmentType">
                <xsl:value-of select="/Shipment/@ShipmentType"/>
            </xsl:attribute> 
			<!-- END WN-416 SI: Shipment creation msg sent to Exeter WMS w/ WG attribute -->
			<!-- Start : OMNI-6369 : STS Shipment message Changes  -->
			<xsl:variable name="varDocumentType" select="/Shipment/@DocumentType" />
			<xsl:if test="$varDocumentType ='0006' ">
				<xsl:attribute name="OrderNo">
					<xsl:value-of select="/Shipment/ShipmentLines/ShipmentLine/@OrderNo"/>
				</xsl:attribute> 
				<xsl:attribute name="FirstName">
					<xsl:value-of select="/Shipment/ShipmentLines/ShipmentLine/OrderLine/ChainedFromOrderLine/Order/PersonInfoBillTo/@FirstName"/>
				</xsl:attribute>
				<xsl:attribute name="LastName">
					<xsl:value-of select="/Shipment/ShipmentLines/ShipmentLine/OrderLine/ChainedFromOrderLine/Order/PersonInfoBillTo/@LastName"/>
				</xsl:attribute>
            </xsl:if>
			<!-- End : OMNI-6369 : STS Shipment message Changes  -->
			
			<xsl:attribute name="ShipNode">
                <xsl:value-of select="/Shipment/@ShipNode"/>
            </xsl:attribute> 
            
             <xsl:attribute name="CarrierServiceCode">
                <xsl:value-of select="/Shipment/@CarrierServiceCode"/>
            </xsl:attribute> 
			<!-- START STL-1705 Adding OrderDate to the Shipment Details message -->
			<xsl:attribute name="OrderDate">
                <xsl:value-of select="/Shipment/ShipmentLines/ShipmentLine/Order/@OrderDate"/>
            </xsl:attribute>
			<!-- END STL-1705 Adding OrderDate to the Shipment Details message  -->
            
            <Extn>
            	<xsl:attribute name="MessageType">
                	<xsl:text>XTsfCre</xsl:text>
            	</xsl:attribute> 
            	<xsl:attribute name="RMSTransferNo">
                	<xsl:value-of select="/Shipment/Extn/@ExtnRMSTransferNo"/>
            	</xsl:attribute>
            	<xsl:attribute name="FromLocationType">
                	<xsl:text>W</xsl:text>
            	</xsl:attribute>
            	<xsl:attribute name="ToLocationType">
                	<xsl:text>S</xsl:text>
            	</xsl:attribute>
				<!-- Start : OMNI-6369 : STS Shipment message Changes  -->
					<xsl:variable name="varReceivingNode" select="/Shipment/@ReceivingNode" />
					<xsl:choose>
					<xsl:when test="$varReceivingNode != '' ">
						<xsl:attribute name="TransferToLocation">
							<xsl:value-of select="/Shipment/@ReceivingNode"/>
						</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="TransferToLocation">
							<xsl:text>5</xsl:text>
						</xsl:attribute>
					</xsl:otherwise>
					</xsl:choose>
				<!-- End : OMNI-6369 : STS Shipment message Changes  -->
			
            	<xsl:attribute name="MsgTransmissionDate">
                	<xsl:value-of select="currenttimestamp:date-time()"/>
            	</xsl:attribute>
				
				<!-- Start : OMNI-7266 : Update TransferType for STS ORders -->
            	<xsl:if test="$varDocumentType ='0006' ">
					<xsl:attribute name="TransferType">
						<xsl:text>AIP</xsl:text>
					</xsl:attribute>
				</xsl:if>
				<xsl:if test="$varDocumentType ='0001' or $varDocumentType = ' ' or $varDocumentType = ''">
					<xsl:attribute name="TransferType">
						<xsl:text>EG</xsl:text>
					</xsl:attribute>
				</xsl:if>
				<!-- End : OMNI-7266 : Update TransferType for STS ORders -->
            				
            	<xsl:attribute name="TransferStatus">
                	<xsl:text>A</xsl:text>
            	</xsl:attribute>
            	<xsl:attribute name="UserId">
                	<xsl:text>Sterling</xsl:text>
            	</xsl:attribute>
            	<xsl:attribute name="freight_code">
                	<xsl:text>N</xsl:text>
            	</xsl:attribute> 
            </Extn>
            <ShipmentLines>
            <xsl:for-each select="/Shipment/ShipmentLines/ShipmentLine">
            
            	<ShipmentLine>
     				<xsl:attribute name="ItemID">
                		<xsl:value-of select="@ItemID"/>
            		</xsl:attribute> 
            		
            		<xsl:attribute name="Quantity">
                		<xsl:value-of select="@Quantity"/>
            		</xsl:attribute>
					
					<xsl:attribute name="ShipmentLineNo">
                		<xsl:value-of select="@ShipmentLineNo"/>
            		</xsl:attribute>
            		
					<!-- Start : OMNI-6369 : STS Shipment message Changes  -->
					<xsl:if test="$varDocumentType ='0006' ">
						<xsl:attribute name="PromisedPickupDate">
							<xsl:variable name="extnInitialPromiseDate" select="OrderLine/ChainedFromOrderLine/Extn/@ExtnInitialPromiseDate"/>
							<xsl:if test="$extnInitialPromiseDate !='' ">
								<xsl:value-of select="substring($extnInitialPromiseDate, 1, 10)" />
							</xsl:if>
						</xsl:attribute>
						<!-- Start : OMNI-7266 : STS Shipment message Changes  -->
						<xsl:attribute name="ExtnStoreDeliveryDate">
							<xsl:value-of select="OrderLine/ChainedFromOrderLine/Extn/@ExtnStoreDeliveryDate"/>
						</xsl:attribute> 
						<xsl:attribute name="ExtnDCDepartureDate">
							<xsl:value-of select="OrderLine/ChainedFromOrderLine/Extn/@ExtnDCDepartureDate"/>
						</xsl:attribute> 
						<!-- Ebd : OMNI-7266 : STS Shipment message Changes  -->
					</xsl:if>
					<!-- End : OMNI-6369 : STS Shipment message Changes  -->
					
            	</ShipmentLine>
            		
            </xsl:for-each>
            		
            </ShipmentLines>
                	

		</Shipment>
    </xsl:template>
</xsl:stylesheet>