<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output indent="yes" />

   <xsl:template match="/Order">
      <Order>
         <xsl:attribute name="OrderHeaderKey">
            <xsl:value-of select="@OrderHeaderKey" />
         </xsl:attribute>

         <xsl:attribute name="Action">
            <xsl:value-of select="'MODIFY'" />
         </xsl:attribute>
         
                  
         <xsl:copy-of select = "./*"/>
         <OrderHoldTypes>
	<OrderHoldType>
	<xsl:attribute name="HoldType">

<!-- 
			<xsl:if test = "@ExtnUserGroupName='ASSOCIATE'">
	
            <xsl:value-of select="'ASSOCIATE_HOLD'" />
            </xsl:if>
            
            <xsl:if test = "@ExtnUserGroupName='SUPPORT_TEAM'">
	
            <xsl:value-of select="'SUPPORT_TEAM_HOLD'" />
            </xsl:if>
            
            <xsl:if test = "@ExtnUserGroupName='SUPERVISOR'">
	
            <xsl:value-of select="'SUPERVISOR_HOLD'" />
            </xsl:if>
            
            <xsl:if test = "@ExtnUserGroupName='OPERATIONS_MANAGER'">
	
            <xsl:value-of select="'OPER_MANAGER_HOLD'" />
            </xsl:if>
            
            <xsl:if test = "@ExtnUserGroupName='SITE_LEADER'">
	
            <xsl:value-of select="'SITE_LEAD_HOLD'" />
            </xsl:if>
  -->
  
			<xsl:if test = "@ExtnUserGroupName='CSA'">
	
            <xsl:value-of select="'ASSOCIATE_HOLD'" />
            </xsl:if>
            
            <xsl:if test = "@ExtnUserGroupName='AdminSupport'">
	
            <xsl:value-of select="'SUPPORT_TEAM_HOLD'" />
            </xsl:if>
            
            <xsl:if test = "@ExtnUserGroupName='Supervisors'">
	
            <xsl:value-of select="'SUPERVISOR_HOLD'" />
            </xsl:if>
            
            <xsl:if test = "@ExtnUserGroupName='MANAGER'">
	
            <xsl:value-of select="'OPER_MANAGER_HOLD'" />
            </xsl:if>
            
            <xsl:if test = "@ExtnUserGroupName='SENIOR_MANAGER'">
	
            <xsl:value-of select="'SITE_LEAD_HOLD'" />
            </xsl:if>
             
         </xsl:attribute>
         <xsl:attribute name="Status">
            <xsl:value-of select="'1100'" />
         </xsl:attribute>
	</OrderHoldType>
		</OrderHoldTypes>

      </Order>
   </xsl:template>
</xsl:stylesheet>

