<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
	<xsl:template match="/OrderRelease">      
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:attribute name="Override">
				<xsl:value-of select="'Y'"/>
			</xsl:attribute>
			<xsl:if test="/OrderRelease/OrderLines/OrderLine/Notes/Note/@ReasonCode='900'">
				<xsl:element name="Notes">
					<xsl:element name="Note">
						<xsl:attribute name="ReasonCode">
							<xsl:value-of select="/OrderRelease/OrderLines/OrderLine/Notes/Note/@ReasonCode"/>
						</xsl:attribute>
						<xsl:attribute name="NoteText">
							<xsl:value-of select="'Other Credit Declines'"/>
						</xsl:attribute>
					</xsl:element>				
			</xsl:element>
			</xsl:if>
			<xsl:if test="/OrderRelease/OrderLines/OrderLine/Notes/Note/@ReasonCode='901'">
				<xsl:element name="Notes">
					<xsl:element name="Note">
						<xsl:attribute name="ReasonCode">
							<xsl:value-of select="/OrderRelease/OrderLines/OrderLine/Notes/Note/@ReasonCode"/>
						</xsl:attribute>
						<xsl:attribute name="NoteText">
							<xsl:value-of select="'Fraud'"/>
						</xsl:attribute>
					</xsl:element>				
			</xsl:element>
			</xsl:if>
			<xsl:if test="/OrderRelease/OrderLines/OrderLine/Notes/Note/@ReasonCode='902'">
				<xsl:element name="Notes">
					<xsl:element name="Note">
						<xsl:attribute name="ReasonCode">
							<xsl:value-of select="/OrderRelease/OrderLines/OrderLine/Notes/Note/@ReasonCode"/>
						</xsl:attribute>
						<xsl:attribute name="NoteText">
							<xsl:value-of select="'Out of Stock'"/>
						</xsl:attribute>
					</xsl:element>				
			</xsl:element>
			</xsl:if>
			
			<xsl:if test="/OrderRelease/OrderLines/OrderLine/Notes/Note/@ReasonCode='903'">
				<xsl:element name="Notes">
					<xsl:element name="Note">
						<xsl:attribute name="ReasonCode">
							<xsl:value-of select="/OrderRelease/OrderLines/OrderLine/Notes/Note/@ReasonCode"/>
						</xsl:attribute>
						<xsl:attribute name="NoteText">
							<xsl:value-of select="'Cancelled at Customer request'"/>
						</xsl:attribute>
					</xsl:element>				
			</xsl:element>
			</xsl:if>
			<xsl:if test="/OrderRelease/OrderLines/OrderLine/Notes/Note/@ReasonCode='906'">
				<xsl:element name="Notes">
					<xsl:element name="Note">
						<xsl:attribute name="ReasonCode">
							<xsl:value-of select="/OrderRelease/OrderLines/OrderLine/Notes/Note/@ReasonCode"/>
						</xsl:attribute>
						<xsl:attribute name="NoteText">
							<xsl:value-of select="'Duplicate-don't send email'"/>
						</xsl:attribute>
					</xsl:element>				
			</xsl:element>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>