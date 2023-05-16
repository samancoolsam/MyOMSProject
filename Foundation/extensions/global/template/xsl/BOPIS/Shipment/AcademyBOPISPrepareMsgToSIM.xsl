<!--
######################################################################################################################################
OBJECTIVE: Prepare input xml for publishing message to SIM
######################################################################################################################################
Version         Date            Modified By       Description
######################################################################################################################################
1.0             12/06/2018      Abhishek Aggarwal    Form input xml to publish to SIM.
2.0             24/08/2018      Abhishek Aggarwal    BOPIS-1121
3.0             03/10/2018      Abhishek Aggarwal    BOPIS-1456
######################################################################################################################################
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output indent="yes"/>
<xsl:template match="Shipment">
		<xsl:variable name="MessageType" select="@MessageType"/> 
		<xsl:variable name="OrderNo" select="ShipmentLines/ShipmentLine/@OrderNo"/>
		<xsl:variable name="NodeType" select="@NodeType"/>
		<xsl:variable name="ReceivingNode" select="@ReceivingNode"/>
		
		<Shipment OrderNo="{$OrderNo}" USERID="{@UserID}" ReservationType="{@ReservationType}" ShipmentNo="{@ShipmentNo}">
		<!-- Start : OMNI-8622 : Customer Reserve message to contain StoreNo  -->
			<xsl:variable name="varDocumentType" select="/Shipment/@DocumentType" />
			<xsl:if test="$varDocumentType ='0006' ">
				<xsl:attribute name="ShipNode">
					<xsl:value-of select="/Shipment/@ReceivingNode"/>
				</xsl:attribute> 
				<xsl:choose>
				<!-- Start : OMNI-47445: For STS2.0 Sales Order No will be sent in OrderNo Attribute 
					Transfer Order ShipmentNo will be sent in ShipmentNo Attribute.  -->
				 <xsl:when test="$NodeType='Store'and $ReceivingNode!=''" >
				 <xsl:attribute name="OrderNo">
					<xsl:value-of select="/Shipment/@OrderName"/>
				 </xsl:attribute>
				 <xsl:attribute name="ShipmentNo">
					<xsl:value-of select="/Shipment/@ShipmentNo"/>
				 </xsl:attribute>
				 </xsl:when>
				 <!-- End :OMNI-47445: For STS2.0 -->
				 <xsl:otherwise>
				<!--Start : OMNI-9523 : For STS ShipmentNo will be sent in OrderNo Attribute 
					ExtnRMSTransferNo will be sent in ShipmentNo Attribute. -->
				<xsl:attribute name="OrderNo">
					<xsl:value-of select="/Shipment/@ShipmentNo"/>
				</xsl:attribute>
				<xsl:attribute name="ShipmentNo">
					<xsl:value-of select="/Shipment/Extn/@ExtnRMSTransferNo"/>
				</xsl:attribute>
				<!--End : OMNI-9523-->
				</xsl:otherwise>
				</xsl:choose>
               </xsl:if>
			<xsl:if test="$varDocumentType = '0001'">
				<xsl:attribute name="ShipNode">
					<xsl:value-of select="/Shipment/@ShipNode"/>
				</xsl:attribute>
			</xsl:if>
			<!-- End : OMNI-8622 : Customer Reserve message to contain StoreNo  -->		
			<ShipmentLines>
			<xsl:choose>
				<xsl:when test="$MessageType='FULFILL'">
					<xsl:for-each select="ShipmentLines/ShipmentLine"> 
						<xsl:variable name="Quantity" select="@Quantity"/>
							<xsl:choose>
								<xsl:when test="$Quantity!=0">
									<ShipmentLine ItemID="{@ItemID}" Quantity="{$Quantity}" Action="{$MessageType}"/>
								</xsl:when>
								<xsl:otherwise>
									<!--DO Nothing: do not send zero quantity lines to SIM in fulfill message -->
								</xsl:otherwise>
							</xsl:choose>
					</xsl:for-each>
				</xsl:when>
				<xsl:otherwise>
					<xsl:for-each select="ShipmentLines/ShipmentLine">
						<xsl:variable name="ShrtgQty" select="@ShortageQty"/> 
						<xsl:variable name="Quantity" select="@Quantity"/>
						<xsl:variable name="Qty">
							<xsl:choose>
								<xsl:when test="$MessageType='CANCEL_RESERVATION'">
									<xsl:value-of select="$ShrtgQty" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="$Quantity" />
								</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						<xsl:variable name="ExtnMsgToSIM" select="Extn/@ExtnMsgToSIM"/>
						<xsl:choose>
							<xsl:when test="$MessageType='CANCEL_RESERVATION' and $ExtnMsgToSIM!='Y'">
						    </xsl:when>
						    <xsl:otherwise>
								<xsl:if test="$Qty!=0">
									<ShipmentLine ItemID="{@ItemID}" Quantity="{$Qty}" Action="{$MessageType}"/>
								</xsl:if>	
							</xsl:otherwise>	
						</xsl:choose>
					</xsl:for-each>
				</xsl:otherwise>
			</xsl:choose>
			</ShipmentLines>
		</Shipment>
</xsl:template>
</xsl:stylesheet>