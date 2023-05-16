<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lxslt="http://xml.apache.org/xslt"
                version="1.0">

	<xsl:output indent="yes"/>
	
	<xsl:template match="/">
	    <xsl:apply-templates select="Order"/>
	</xsl:template>
	
	<xsl:template match="Order">
		
		<xsl:element name="Inbox">
			<xsl:attribute name="OrderHeaderKey" > 
	        	<xsl:value-of select="@OrderHeaderKey"/> 
			</xsl:attribute>
	
		    <xsl:attribute name="OrderNo" > 
	        	<xsl:value-of select="@OrderNo"/> 
	        </xsl:attribute>
	
	        <xsl:attribute name="Description" >
	        	<xsl:value-of select="@OrderNo"/>
	        </xsl:attribute>		    
	
			<xsl:if test = "@ExtnUserGroupName='AdminSupport'">
	        	<xsl:attribute name="QueueId" >
	        		<xsl:value-of select="'ACADEMY_SUPPORT_TEAM_QUEUE'"/>
	        	</xsl:attribute>
	        </xsl:if>
	
			<xsl:if test = "@ExtnUserGroupName='Supervisors'">
		        <xsl:attribute name="QueueId" >
		        	<xsl:value-of select="'ACADEMY_SUPERVISOR_QUEUE'"/>
		        </xsl:attribute>
	        </xsl:if>
		
			<xsl:if test = "@ExtnUserGroupName='MANAGER'">
	        	<xsl:attribute name="QueueId" >
	        		<xsl:value-of select="'ACADEMY_OPS_MANAGER_QUEUE'"/>
	        	</xsl:attribute>
	        </xsl:if>
			
	        <!-- OMNI-7848: Gift Card Appeasements in Sterling WebCom :: START-->	        
			<!-- 
			<xsl:if test = "@ExtnUserGroupName='CSA'">
				<xsl:attribute name="QueueId" >
					<xsl:value-of select="'ACADEMY_ASSOCIATE_QUEUE'"/>
				</xsl:attribute>
			</xsl:if>
			
			<xsl:if test = "@ExtnUserGroupName='SENIOR_MANAGER'">
				<xsl:attribute name="QueueId" >
					<xsl:value-of select="'ACADEMY_SITE_LEAD_QUEUE'"/>
				</xsl:attribute>
			</xsl:if> 
			-->			
			<xsl:if test = "@ExtnUserGroupName='CSA' or @ExtnUserGroupName='SENIOR_MANAGER'">
				<xsl:attribute name="QueueId" >
					<xsl:value-of select="'ACADEMY_SITE_LEAD_QUEUE'"/>
				</xsl:attribute>
			</xsl:if>
			<!-- OMNI-7848: Gift Card Appeasements in Sterling WebCom :: END-->
			
	        <xsl:attribute name="ExceptionType" >
	            <xsl:text>APPROVAL_EXCEPTIONS</xsl:text>
	        </xsl:attribute>
	        
	        <xsl:attribute name="InboxType" >
	            <xsl:text>APPEASEMENT_APPROVAL_ALERT</xsl:text>
	        </xsl:attribute>
	        
			<!--
	        <xsl:attribute name="OwnerKey" >
	            <xsl:value-of select="@EnterpriseCode"/> 
	        </xsl:attribute>
			-->
	        
	        <xsl:element name="ConsolidationTemplate">
		        <xsl:element name="Inbox">
			        <xsl:attribute name="ExceptionType" >
			            <xsl:text>APPROVAL_EXCEPTIONS</xsl:text>
			        </xsl:attribute>			        			       
					
					<xsl:if test = "@ExtnUserGroupName='AdminSupport'">
			        	<xsl:attribute name="QueueId" >
			        		<xsl:value-of select="'ACADEMY_SUPPORT_TEAM_QUEUE'"/>
			        	</xsl:attribute>
			        </xsl:if>
					
					<xsl:if test = "@ExtnUserGroupName='Supervisors'">
			        	<xsl:attribute name="QueueId" >
			        		<xsl:value-of select="'ACADEMY_SUPERVISOR_QUEUE'"/>
			        	</xsl:attribute>
			        </xsl:if>
				
					<xsl:if test = "@ExtnUserGroupName='MANAGER'">
			        	<xsl:attribute name="QueueId" >
			        		<xsl:value-of select="'ACADEMY_OPS_MANAGER_QUEUE'"/>
			        	</xsl:attribute>
			        </xsl:if>
					
					<!-- OMNI-7848: Gift Card Appeasements in Sterling WebCom :: START-->
			        <!--
					<xsl:if test = "@ExtnUserGroupName='CSA'">
			        	<xsl:attribute name="QueueId" >
			        		<xsl:value-of select="'ACADEMY_ASSOCIATE_QUEUE'"/>
			        	</xsl:attribute>
			        </xsl:if>
					
			        <xsl:if test = "@ExtnUserGroupName='SENIOR_MANAGER'">
			        	<xsl:attribute name="QueueId" >
			        		<xsl:value-of select="'ACADEMY_SITE_LEAD_QUEUE'"/>
			        	</xsl:attribute>
			        </xsl:if>
					-->
					<xsl:if test = "@ExtnUserGroupName='CSA' or @ExtnUserGroupName='SENIOR_MANAGER'">
			        	<xsl:attribute name="QueueId" >
			        		<xsl:value-of select="'ACADEMY_SITE_LEAD_QUEUE'"/>
			        	</xsl:attribute>
			        </xsl:if>
					<!-- OMNI-7848: Gift Card Appeasements in Sterling WebCom :: END-->
					
		        </xsl:element>
	        </xsl:element>
	
	        <xsl:element name="InboxReferencesList">
				<xsl:element name="InboxReferences" > 
					<!-- let system generate inbox reference key	
	            	<xsl:attribute name="InboxReferenceKey">
	                	<xsl:value-of select="@OrderHeaderKey"/> 
					</xsl:attribute>
					-->
	                <xsl:attribute name="ReferenceType">
	                	<xsl:text>TEXT</xsl:text>
					</xsl:attribute>
					
	                <xsl:attribute name="Name">
	                	<xsl:text>Appeasement Approval Alert</xsl:text>
	                </xsl:attribute>
					
					<xsl:attribute name="Value">
	                	<xsl:value-of select="@OrderNo" />
					</xsl:attribute>
				</xsl:element>
			</xsl:element>
	    
	    </xsl:element>
	</xsl:template>
</xsl:stylesheet>
