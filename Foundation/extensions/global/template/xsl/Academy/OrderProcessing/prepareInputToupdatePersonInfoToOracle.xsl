<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="Order">
        <xsl:element name="PersonInfoBillTo">
			<xsl:attribute name="AddressLine1">
                <xsl:value-of select="./PersonInfoBillTo/@AddressLine1"/>
            </xsl:attribute>
			<xsl:attribute name="AddressLine2">
                <xsl:value-of select="./PersonInfoBillTo/@AddressLine2"/>
            </xsl:attribute>
			<xsl:attribute name="AddressLine3">
                <xsl:value-of select="./PersonInfoBillTo/@AddressLine3"/>
            </xsl:attribute>
			<xsl:attribute name="AddressLine4">
                <xsl:value-of select="./PersonInfoBillTo/@AddressLine4"/>
            </xsl:attribute>
			<xsl:attribute name="AddressLine5">
                <xsl:value-of select="./PersonInfoBillTo/@AddressLine5"/>
            </xsl:attribute>
			<xsl:attribute name="AddressLine6">
                <xsl:value-of select="./PersonInfoBillTo/@AddressLine6"/>
            </xsl:attribute>
			<xsl:attribute name="AlternateEmailID">
                <xsl:value-of select="./PersonInfoBillTo/@AlternateEmailID"/>
            </xsl:attribute>
			<xsl:attribute name="Beeper">
                <xsl:value-of select="./PersonInfoBillTo/@Beeper"/>
            </xsl:attribute>
			<xsl:attribute name="City">
                <xsl:value-of select="./PersonInfoBillTo/@City"/>
            </xsl:attribute>
			<xsl:attribute name="Company">
                <xsl:value-of select="./PersonInfoBillTo/@Company"/>
            </xsl:attribute>
			<xsl:attribute name="DayFaxNo">
                <xsl:value-of select="./PersonInfoBillTo/@DayFaxNo"/>
            </xsl:attribute>
			<xsl:attribute name="DayPhone">
                <xsl:value-of select="./PersonInfoBillTo/@DayPhone"/>
            </xsl:attribute>
			<xsl:attribute name="Department">
                <xsl:value-of select="./PersonInfoBillTo/@Department"/>
            </xsl:attribute>
			<xsl:attribute name="EMailID">
                <xsl:value-of select="./PersonInfoBillTo/@EMailID"/>
            </xsl:attribute>
			<xsl:attribute name="EveningFaxNo">
                <xsl:value-of select="./PersonInfoBillTo/@EveningFaxNo"/>
            </xsl:attribute>
			<xsl:attribute name="EveningPhone">
                <xsl:value-of select="./PersonInfoBillTo/@EveningPhone"/>
            </xsl:attribute>
			<xsl:attribute name="FirstName">
                <xsl:value-of select="./PersonInfoBillTo/@FirstName"/>
            </xsl:attribute>
			<xsl:attribute name="JobTitle">
                <xsl:value-of select="./PersonInfoBillTo/@JobTitle"/>
            </xsl:attribute>
			<xsl:attribute name="LastName">
                <xsl:value-of select="./PersonInfoBillTo/@LastName"/>
            </xsl:attribute>
			<xsl:attribute name="MiddleName">
                <xsl:value-of select="./PersonInfoBillTo/@MiddleName"/>
            </xsl:attribute>
			<xsl:attribute name="MobilePhone">
                <xsl:value-of select="./PersonInfoBillTo/@MobilePhone"/>
            </xsl:attribute>
			<xsl:attribute name="OtherPhone">
                <xsl:value-of select="./PersonInfoBillTo/@OtherPhone"/>
            </xsl:attribute>
			<xsl:attribute name="PersonID">
                <xsl:value-of select="./PersonInfoBillTo/@PersonID"/>
            </xsl:attribute>
			<xsl:attribute name="State">
                <xsl:value-of select="./PersonInfoBillTo/@State"/>
            </xsl:attribute>
			<xsl:attribute name="Suffix">
                <xsl:value-of select="./PersonInfoBillTo/@Suffix"/>
            </xsl:attribute>
			<xsl:attribute name="Title">
                <xsl:value-of select="./PersonInfoBillTo/@Title"/>
            </xsl:attribute>
			<xsl:attribute name="ZipCode">
                <xsl:value-of select="./PersonInfoBillTo/@ZipCode"/>
            </xsl:attribute>                                       
            </xsl:element>
   </xsl:template>
</xsl:stylesheet>
