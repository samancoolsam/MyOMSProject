<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
	<xsl:template match="/">
		<xsl:element name="TrackingNumbers">
			<xsl:variable name="UPSURL" select="TrackingNumbers/@UPSURL"/>
	  		<xsl:variable name="USPSURL" select="TrackingNumbers/@USPSURL"/>
			<xsl:variable name="FEDXURL" select="TrackingNumbers/@FEDXURL"/>
			<xsl:variable name="SPURL" select="TrackingNumbers/@SPURL"/>
			<xsl:variable name="CEVAURL" select="TrackingNumbers/@CEVAURL"/>
				<xsl:for-each select="TrackingNumbers/TrackingNumber">
					<xsl:element name="TrackingNumber">
						<xsl:attribute name="RequestNo">
							<xsl:value-of select="@RequestNo"/>
						</xsl:attribute>
						<xsl:variable name="TNo" select="@TrackingNo"/>
						
							<xsl:if test="contains(@SCAC,'UPSN')">
								<xsl:attribute name="URL">
									<xsl:value-of select="concat($UPSURL,$TNo)"/>
								</xsl:attribute>
							</xsl:if>
							<xsl:if test="contains(@SCAC,'FEDX')">
								<xsl:attribute name="URL">
									<xsl:value-of select="concat($FEDXURL,$TNo)"/>
								</xsl:attribute>								
							</xsl:if>
							<xsl:if test="contains(@SCAC,'SmartPost')">
								<xsl:attribute name="URL">
									<xsl:value-of select="concat($SPURL,$TNo)"/>
								</xsl:attribute>
							</xsl:if>
							<xsl:if test="contains(@SCAC,'USPS')">
								<xsl:attribute name="URL">
									<xsl:value-of select="concat($USPSURL,$TNo)"/>
								</xsl:attribute>
							</xsl:if>
							<xsl:if test="contains(@SCAC,'CEVA')">
								<xsl:attribute name="URL">
									<xsl:value-of select="concat($CEVAURL,$TNo)"/>
								</xsl:attribute>
							</xsl:if>
				        </xsl:element>
			        </xsl:for-each>
		 </xsl:element>
	</xsl:template>
</xsl:stylesheet>