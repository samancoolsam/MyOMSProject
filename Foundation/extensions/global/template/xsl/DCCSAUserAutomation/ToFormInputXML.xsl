<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output indent="yes" />
   <xsl:template match="/User">
   <!--XML creation for new user manager or non manager-->
   <xsl:if test = "@Action='Create'">
   <xsl:element name="User">
   <xsl:attribute name="Action">
               <xsl:value-of select="@Action" /> 
            </xsl:attribute>
    <xsl:attribute name="Username">
               <xsl:value-of select="@Username" /> 
            </xsl:attribute>
			<xsl:attribute name="Loginid">
               <xsl:value-of select="@Loginid" /> 
            </xsl:attribute>
			<xsl:attribute name="OrganizationKey">
               <xsl:value-of select="@OrganizationKey" /> 
            </xsl:attribute>
   <xsl:attribute name="Password">
               <xsl:value-of select="'password'" /> 
            </xsl:attribute>
			<xsl:attribute name="Activateflag">
               <xsl:value-of select="'Y'" /> 
            </xsl:attribute>
			<xsl:attribute name="MenuId">
               <xsl:if test = "@Role='DCUSER'">
               <xsl:value-of select="'ACADEMY_SOM_MENU'" /> 
			   </xsl:if>
			   <xsl:if test = "@Role='CSAUSER'">
               <xsl:value-of select="'ACADEMY_CSA_MENU'" /> 
			   </xsl:if> 
            </xsl:attribute>
			<xsl:attribute name="Localecode">
               <xsl:value-of select="'en_US_CST'" /> 
            </xsl:attribute>
			<xsl:attribute name="Theme">
               <xsl:value-of select="'sapphire'" /> 
            </xsl:attribute>
			<xsl:element name="UserGroupLists">
			<xsl:attribute name="Reset">
               <xsl:value-of select="'Y'" /> 
            </xsl:attribute>
			<xsl:element name="UserGroupList">
			<xsl:attribute name="UsergroupId">
			<xsl:if test = "@Role='DCUSER'">
               <xsl:value-of select="'ACADEMY_STORE_USER'" /> 
			   </xsl:if>
			   <xsl:if test = "@Role='CSAUSER'">
               <xsl:value-of select="'CSA'" /> 
			   </xsl:if>
            </xsl:attribute>
			</xsl:element>
			 </xsl:element>
			 </xsl:element>
			</xsl:if>
			<!--XML creation for user modification, manager or non manager-->
			<xsl:if test = "@Action='Modify'">
   <xsl:element name="User">
   <xsl:attribute name="Action">
               <xsl:value-of select="@Action" /> 
            </xsl:attribute>
    <xsl:attribute name="Username">
               <xsl:value-of select="@Username" /> 
            </xsl:attribute>
			<xsl:attribute name="Loginid">
               <xsl:value-of select="@Loginid" /> 
            </xsl:attribute>
			<xsl:attribute name="OrganizationKey">
               <xsl:value-of select="@OrganizationKey" /> 
            </xsl:attribute>
   <xsl:attribute name="Password">
               <xsl:value-of select="'password'" /> 
            </xsl:attribute>
			<xsl:attribute name="Activateflag">
               <xsl:value-of select="'Y'" /> 
            </xsl:attribute>
			<xsl:attribute name="MenuId">
               <xsl:if test = "@Role='DCUSER'">
               <xsl:value-of select="'ACADEMY_SOM_MENU'" /> 
			   </xsl:if>
			   <xsl:if test = "@Role='CSAUSER'">
               <xsl:value-of select="'ACADEMY_CSA_MENU'" /> 
			   </xsl:if> 
            </xsl:attribute>
			<xsl:attribute name="Localecode">
               <xsl:value-of select="'en_US_CST'" /> 
            </xsl:attribute>
			<xsl:attribute name="Theme">
               <xsl:value-of select="'sapphire'" /> 
            </xsl:attribute>
			<xsl:element name="UserGroupLists">
			<xsl:attribute name="Reset">
               <xsl:value-of select="'Y'"/> 
			   </xsl:attribute>
			<xsl:element name="UserGroupList">
			<xsl:attribute name="UsergroupId">
			<xsl:if test = "@Role='DCUSER'">
               <xsl:value-of select="'ACADEMY_STORE_USER'" /> 
			   </xsl:if>
			   <xsl:if test = "@Role='CSAUSER'">
               <xsl:value-of select="'CSA'" /> 
			   </xsl:if>
            </xsl:attribute>
			</xsl:element>
			 </xsl:element>
			 </xsl:element>
			</xsl:if>
			<!--XML creation for user Deactivation-->
			<xsl:if test = "@Action='Deactivate'">
   <xsl:element name="User">
   <xsl:attribute name="Action">
               <xsl:value-of select="@Action" /> 
            </xsl:attribute>
			<xsl:attribute name="Loginid">
               <xsl:value-of select="@Loginid" /> 
            </xsl:attribute>
			<xsl:attribute name="OrganizationKey">
               <xsl:value-of select="@OrganizationKey" /> 
            </xsl:attribute>
			<xsl:attribute name="Activateflag">
               <xsl:value-of select="'N'" /> 
            </xsl:attribute>
			</xsl:element>
			 </xsl:if>
     </xsl:template>
</xsl:stylesheet>