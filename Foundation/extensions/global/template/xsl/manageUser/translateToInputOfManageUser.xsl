<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output indent="yes" />
   <xsl:template match="/User">
   
   <xsl:if test = "@Action='Create'">
      <xsl:element name="MultiApi">
   <xsl:element name="API">
   <xsl:attribute name="Name">
                <xsl:text>createUserHierarchy</xsl:text>
            </xsl:attribute>
            <xsl:element name="Input">
            
            <xsl:element name="User">
            <xsl:attribute name="Username">
               <xsl:value-of select="@Name" /> 
            </xsl:attribute>
            <xsl:attribute name="Localecode">
               <xsl:value-of select="'en_US_EST'" /> 
            </xsl:attribute>
            <xsl:attribute name="Loginid">
               <xsl:value-of select="@LoginID" /> 
            </xsl:attribute>
            <xsl:attribute name="MenuId">
               <xsl:value-of select="@MenuID" /> 
            </xsl:attribute>
            <xsl:attribute name="Password">
               <xsl:value-of select="'abc'" /> 
            </xsl:attribute>
            <xsl:if test = "@Activateflag='Y'">
            <xsl:attribute name="Activateflag">
               <xsl:value-of select="'Y'" /> 
            </xsl:attribute>
            </xsl:if>
            
            <xsl:element name="ContactPersonInfo">
            <xsl:attribute name="DayPhone">
               <xsl:value-of select="@DayPhone" /> 
            </xsl:attribute>
            <xsl:attribute name="EmailID">
               <xsl:value-of select="@EmailID" /> 
            </xsl:attribute>
            <xsl:attribute name="JobTitle">
               <xsl:value-of select="@JobTitle" /> 
            </xsl:attribute>
            <xsl:attribute name="DepartmentCode">
               <xsl:value-of select="@DepartmentCode" /> 
            </xsl:attribute>
            </xsl:element>
            
            <xsl:element name="UserGroupLists">
            
            <xsl:for-each select = "/User/UserGroups/UserGroup">
            <xsl:element name="UserGroupList">
            <xsl:attribute name="UsergroupId">
               <xsl:value-of select="@UserGroupId" /> 
            </xsl:attribute>
            </xsl:element>
            </xsl:for-each>
            </xsl:element>
            
            </xsl:element>
            </xsl:element>
            </xsl:element>
            </xsl:element>
   </xsl:if>
   <xsl:if test = "@Action='Update'">
      
   <xsl:choose>
   
   
   	<xsl:when test="not(@PreviousLoginId='')">
   	<xsl:element name="MultiApi">
   <xsl:element name="API">
   <xsl:attribute name="Name">
                <xsl:text>deleteUserHierarchy</xsl:text>
            </xsl:attribute>
            <xsl:element name="Input">
            
            <xsl:element name="User">
             <xsl:attribute name="Loginid">
               <xsl:value-of select="@PreviousLoginId" /> 
            </xsl:attribute>
             
            </xsl:element>
            </xsl:element>
            </xsl:element>
            <xsl:element name="API">
   <xsl:attribute name="Name">
                <xsl:text>createUserHierarchy</xsl:text>
            </xsl:attribute>
            <xsl:element name="Input">
            
            <xsl:element name="User">
            <xsl:attribute name="Username">
               <xsl:value-of select="@Name" /> 
            </xsl:attribute>
            <xsl:attribute name="Localecode">
               <xsl:value-of select="'en_US_EST'" /> 
            </xsl:attribute>
            <xsl:attribute name="Loginid">
               <xsl:value-of select="@LoginID" /> 
            </xsl:attribute>
            <xsl:attribute name="MenuId">
               <xsl:value-of select="@MenuID" /> 
            </xsl:attribute>
            <xsl:attribute name="Password">
               <xsl:value-of select="'abc'" /> 
            </xsl:attribute>
            <xsl:if test = "@Activateflag='Y'">
            <xsl:attribute name="Activateflag">
               <xsl:value-of select="'Y'" /> 
            </xsl:attribute>
            </xsl:if>

            <xsl:element name="ContactPersonInfo">
            <xsl:attribute name="DayPhone">
               <xsl:value-of select="@DayPhone" /> 
            </xsl:attribute>
            <xsl:attribute name="EmailID">
               <xsl:value-of select="@EmailID" /> 
            </xsl:attribute>
            <xsl:attribute name="JobTitle">
               <xsl:value-of select="@JobTitle" /> 
            </xsl:attribute>
            <xsl:attribute name="DepartmentCode">
               <xsl:value-of select="@DepartmentCode" /> 
            </xsl:attribute>
            </xsl:element>
            
            <xsl:element name="UserGroupLists">
            
            <xsl:for-each select = "/User/UserGroups/UserGroup">
            <xsl:element name="UserGroupList">
            <xsl:attribute name="UsergroupId">
               <xsl:value-of select="@UserGroupId" /> 
            </xsl:attribute>
            </xsl:element>
            </xsl:for-each>
            </xsl:element>
            
            </xsl:element>
            </xsl:element>
            </xsl:element>
            </xsl:element>
   	</xsl:when>
     
   	<xsl:otherwise>
   	 
   	 <xsl:choose>
   	 	<xsl:when test="@Activateflag='Y'">
   	 		<xsl:element name="MultiApi">
   <xsl:element name="API">
   <xsl:attribute name="Name">
                <xsl:text>modifyUserHierarchy</xsl:text>
            </xsl:attribute>
            <xsl:element name="Input">
            
            <xsl:element name="User">
            <xsl:attribute name="Username">
               <xsl:value-of select="@Name" /> 
            </xsl:attribute>
            <xsl:attribute name="Localecode">
               <xsl:value-of select="'en_US_EST'" /> 
            </xsl:attribute>
            <xsl:attribute name="Loginid">
               <xsl:value-of select="@LoginID" /> 
            </xsl:attribute>
            <xsl:attribute name="MenuId">
               <xsl:value-of select="@MenuID" /> 
            </xsl:attribute>
            <xsl:attribute name="Password">
               <xsl:value-of select="'abc'" /> 
            </xsl:attribute>
            
            <xsl:element name="ContactPersonInfo">
            <xsl:attribute name="DayPhone">
               <xsl:value-of select="@DayPhone" /> 
            </xsl:attribute>
            <xsl:attribute name="EmailID">
               <xsl:value-of select="@EmailID" /> 
            </xsl:attribute>
            <xsl:attribute name="JobTitle">
               <xsl:value-of select="@JobTitle" /> 
            </xsl:attribute>
            <xsl:attribute name="DepartmentCode">
               <xsl:value-of select="@DepartmentCode" /> 
            </xsl:attribute>
            </xsl:element>
            
            <xsl:element name="UserGroupLists">
            
            <xsl:for-each select = "/User/UserGroups/UserGroup">
            <xsl:element name="UserGroupList">
            <xsl:attribute name="UsergroupId">
               <xsl:value-of select="@UserGroupId" /> 
            </xsl:attribute>
            </xsl:element>
            </xsl:for-each>
            </xsl:element>
            
            </xsl:element>
            </xsl:element>
            </xsl:element>
            </xsl:element>
   	 	</xsl:when>
   	   
   	 	<xsl:otherwise>
   	 	 <xsl:element name="MultiApi">
   <xsl:element name="API">
   <xsl:attribute name="Name">
                <xsl:text>modifyUserHierarchy</xsl:text>
            </xsl:attribute>
            <xsl:element name="Input">
            
            <xsl:element name="User">
            <xsl:attribute name="Username">
               <xsl:value-of select="@Name" /> 
            </xsl:attribute>
            <xsl:attribute name="Loginid">
               <xsl:value-of select="@LoginID" /> 
            </xsl:attribute>
             <xsl:attribute name="Activateflag">
               <xsl:value-of select="'N'" /> 
            </xsl:attribute>
            </xsl:element>
            </xsl:element>
            </xsl:element>
            </xsl:element>
   	 	</xsl:otherwise>
   	 </xsl:choose>
   	</xsl:otherwise>
   </xsl:choose>
      
   </xsl:if>
   
   
   </xsl:template>
</xsl:stylesheet>