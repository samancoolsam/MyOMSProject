<?xml version='1.0' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<Order>
			<xsl:copy-of select="Order/@*"/>
			<xsl:attribute name="DocumentType"><xsl:text>0001</xsl:text></xsl:attribute>
			<xsl:attribute name="EnterpriseCode"><xsl:text>Academy_Direct</xsl:text></xsl:attribute>
			<xsl:attribute name="SellerOrganizationCode"><xsl:text>Academy_Direct</xsl:text></xsl:attribute>
			<xsl:attribute name="Override"><xsl:text>Y</xsl:text></xsl:attribute>
			<xsl:attribute name="SelectMethod"><xsl:text>WAIT</xsl:text></xsl:attribute>
			 <xsl:if test="Order/@FraudResult='Deny'">
			<xsl:attribute name="Action"><xsl:text>CANCEL</xsl:text></xsl:attribute>
			</xsl:if>
			
			<OrderHoldTypes>
			<xsl:if test="Order/@FraudResult='Accept'">
				<xsl:if test="Order/@HoldType='FraudHold'">
							<OrderHoldType>
									 <xsl:attribute name="HoldType">
									 <xsl:text>ACADEMY_AWAIT_FRAUD</xsl:text>
									 </xsl:attribute>
									 <xsl:attribute name="ReasonText">
									 <xsl:value-of select="Order/@ReasonText"/>
									 </xsl:attribute>
									 <xsl:attribute name="Status">
									 <xsl:value-of select="Order/@Status"/>
									 </xsl:attribute>
							</OrderHoldType>
				</xsl:if>
				
				<xsl:if test="Order/@HoldType='RedHold'">
							<OrderHoldType>
									 <xsl:attribute name="HoldType">
									 <xsl:text>ACADEMY_NO_RED_CHECK</xsl:text>
									 </xsl:attribute>
									 <xsl:attribute name="ReasonText">
									 <xsl:value-of select="Order/@ReasonText"/>
									 </xsl:attribute>
									 <xsl:attribute name="Status">
									 <xsl:value-of select="Order/@Status"/>
									 </xsl:attribute>
							</OrderHoldType>
				</xsl:if>
			</xsl:if>
				 </OrderHoldTypes>
		     </Order>
	</xsl:template>
</xsl:stylesheet>