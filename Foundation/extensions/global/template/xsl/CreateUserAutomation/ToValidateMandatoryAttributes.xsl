<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
<xsl:choose>
<!--Action Attribute and action value validation-->
<xsl:when test="User[not(@Action)]">
<Error>
				<xsl:attribute name="ErrorDescription">
					<xsl:text>Action Attribute is Missing</xsl:text>
				</xsl:attribute>
				</Error>
				
</xsl:when> 
<xsl:when test="not(contains(User/@Action, 'Create')) and not(contains(User/@Action, 'Modify')) and not(contains(User/@Action, 'Deactivate')) ">
<Error>
				<xsl:attribute name="ErrorDescription">
					<xsl:text>Action Value is Missing</xsl:text>
				</xsl:attribute>
				</Error>
</xsl:when>
<xsl:otherwise>

<!--if request is for creation validate Loginid,Username,Organizationkey,Role-->

<xsl:if test = "User/@Action='Create'">
<xsl:choose>
<xsl:when test="User[not(@Loginid)]">
<Error>
				<xsl:attribute name="ErrorDescription">
					<xsl:text>Loginid Attribute is Missing</xsl:text>
				</xsl:attribute>
				</Error>
</xsl:when> 
<xsl:when test="User/@Loginid = ''">
<Error>
				<xsl:attribute name="ErrorDescription">
					<xsl:text>Loginid Value is Missing</xsl:text>
				</xsl:attribute>
				</Error>
</xsl:when>
<xsl:when test="User[not(@Username)]">
<Error>
				<xsl:attribute name="ErrorDescription">
					<xsl:text>Username Attribute is Missing</xsl:text>
				</xsl:attribute>
				</Error>
</xsl:when> 
<xsl:when test="User/@Username = ''">
<Error>
				<xsl:attribute name="ErrorDescription">
					<xsl:text>Username Value is Missing</xsl:text>
				</xsl:attribute>
				</Error>
</xsl:when>
<xsl:when test="User[not(@OrganizationKey)]">
<Error>
				<xsl:attribute name="ErrorDescription">
					<xsl:text>Organizationkey Attribute is Missing</xsl:text>
				</xsl:attribute>
				</Error>
</xsl:when> 
<xsl:when test="User/@OrganizationKey = ''">
<Error>
				<xsl:attribute name="ErrorDescription">
					<xsl:text>Organizationkey Value is Missing</xsl:text>
				</xsl:attribute>
				</Error>
</xsl:when>
<xsl:when test="User[not(@Role)]">
<Error>
				<xsl:attribute name="ErrorDescription">
					<xsl:text>Role Attribute is Missing</xsl:text>
				</xsl:attribute>
				</Error>
</xsl:when> 
<xsl:when test="not(contains(User/@Role, 'Manager')) and not(contains(User/@Role, 'Non_Manager'))">
<Error>
				<xsl:attribute name="ErrorDescription">
					<xsl:text>Role Value is Missing</xsl:text>
				</xsl:attribute>
				</Error>
</xsl:when> 
<xsl:otherwise>
<xsl:copy-of select="node()"/>
</xsl:otherwise>
</xsl:choose>
</xsl:if>

<!--if request is for modification validate Loginid,Username,Organizationkey,Role-->

<xsl:if test = "User/@Action='Modify'">
<xsl:choose>
<xsl:when test="User[not(@Loginid)]">
<Error>
				<xsl:attribute name="ErrorDescription">
					<xsl:text>Loginid Attribute is Missing</xsl:text>
				</xsl:attribute>
				</Error>
</xsl:when> 
<xsl:when test="User/@Loginid = ''">
<Error>
				<xsl:attribute name="ErrorDescription">
					<xsl:text>Loginid Value is Missing</xsl:text>
				</xsl:attribute>
				</Error>
</xsl:when>
<xsl:when test="User[not(@Username)]">
<Error>
				<xsl:attribute name="ErrorDescription">
					<xsl:text>Username Attribute is Missing</xsl:text>
				</xsl:attribute>
				</Error>
</xsl:when> 
<xsl:when test="User/@Username = ''">
<Error>
				<xsl:attribute name="ErrorDescription">
					<xsl:text>Username Value is Missing</xsl:text>
				</xsl:attribute>
				</Error>
</xsl:when>
<xsl:when test="User[not(@OrganizationKey)]">
<Error>
				<xsl:attribute name="ErrorDescription">
					<xsl:text>OrganizationKey Attribute is Missing</xsl:text>
				</xsl:attribute>
				</Error>
</xsl:when> 
<xsl:when test="User/@OrganizationKey = ''">
<Error>
				<xsl:attribute name="ErrorDescription">
					<xsl:text>OrganizationKey value is Missing</xsl:text>
				</xsl:attribute>
				</Error>
</xsl:when>
<xsl:when test="User[not(@Role)]">
<Error>
				<xsl:attribute name="ErrorDescription">
					<xsl:text>Role Attribute is Missing</xsl:text>
				</xsl:attribute>
				</Error>
</xsl:when> 
<xsl:when test="not(contains(User/@Role, 'Manager')) and not(contains(User/@Role, 'Non_Manager'))">
<Error>
				<xsl:attribute name="ErrorDescription">
					<xsl:text>Role Value is Missing</xsl:text>
				</xsl:attribute>
				</Error>
</xsl:when> 
<xsl:otherwise>
<xsl:copy-of select="node()"/>
</xsl:otherwise>
</xsl:choose>
</xsl:if>
<!--if request is for deactivation validate Loginid,Organizationkey-->
<xsl:if test = "User/@Action='Deactivate'">
<xsl:choose>
<xsl:when test="User[not(@Loginid)]">
<Error>
				<xsl:attribute name="ErrorDescription">
					<xsl:text>Loginid Attribute is Missing</xsl:text>
				</xsl:attribute>
				</Error>
</xsl:when> 
<xsl:when test="User/@Loginid = ''">
<Error>
				<xsl:attribute name="ErrorDescription">
					<xsl:text>Loginid Value is Missing</xsl:text>
				</xsl:attribute>
				</Error>
</xsl:when>
<xsl:when test="User[not(@OrganizationKey)]">
<Error>
				<xsl:attribute name="ErrorDescription">
					<xsl:text>OrganizationKey Attribute is Missing</xsl:text>
				</xsl:attribute>
				</Error>
</xsl:when> 
<xsl:when test="User/@OrganizationKey = ''">
<Error>
				<xsl:attribute name="ErrorDescription">
					<xsl:text>OrganizationKey value is Missing</xsl:text>
				</xsl:attribute>
				</Error>
</xsl:when>
<xsl:otherwise>
<xsl:copy-of select="node()"/>
</xsl:otherwise>
</xsl:choose>
</xsl:if>
</xsl:otherwise>
</xsl:choose>
</xsl:template>
</xsl:stylesheet>