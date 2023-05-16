<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:template match="/Shipment">
		<xsl:variable name="varShipmentNo">
			<xsl:value-of select="@ShipmentNo" />
		</xsl:variable>
		
		<Inbox AssignedToUserKey="SYSTEM_USER" Consolidate="Y" ConsolidationWindow="FOREVER" Description="Pending Line on Hard Declined Order" ExceptionType="PENDING_CANCEL_ON_DECLINE" FlowName="HardDeclinedCancelAlert" ListDescription="&lt;HTML xmlns:lxslt=&quot;_http://xml.apache.org/xslt&quot;&gt;
      &lt;!--CONTENT_TYPE=text/html--&gt;
      &lt;BODY&gt;&lt;/BODY&gt;
      &lt;/HTML&gt;
      " QueueId="PendingCancelOnDecline">
			<xsl:attribute name="DetailDescription">
				<xsl:value-of select="concat('Hard Decline Cancel Failure for Shipment No: ',$varShipmentNo,'')" />
			</xsl:attribute>
			<xsl:attribute name="ShipmentKey">
				<xsl:value-of select="@ShipmentKey" />
			</xsl:attribute>

			<xsl:attribute name="ShipmentNo">
				<xsl:value-of select="@ShipmentNo" />
			</xsl:attribute>
						
			<InboxReferencesList>
				<InboxReferences Name="ManifestNo" ReferenceType="TEXT">
					<xsl:attribute name="Value">
						<xsl:value-of select="@ManifestNo" />
					</xsl:attribute>
				</InboxReferences>
				<InboxReferences Name="Status" ReferenceType="TEXT">
					<xsl:attribute name="Value">
						<xsl:value-of select="@Status" />
					</xsl:attribute>
				</InboxReferences>
				<InboxReferences Name="ShipNode" ReferenceType="TEXT">
					<xsl:attribute name="Value">
						<xsl:value-of select="@ShipNode" />
					</xsl:attribute>
				</InboxReferences>
				<InboxReferences Name="IsPackProcessComplete" ReferenceType="TEXT">
					<xsl:attribute name="Value">
						<xsl:value-of select="@IsPackProcessComplete" />
					</xsl:attribute>
				</InboxReferences>
			</InboxReferencesList>
		</Inbox>
	</xsl:template>
	
	<xsl:template match="/Order">
		<xsl:variable name="varOrderNo">
			<xsl:value-of select="@OrderNo" />
		</xsl:variable>
		
		<Inbox AssignedToUserKey="SYSTEM_USER" Consolidate="Y" ConsolidationWindow="FOREVER" Description="Pending Line on Hard Declined Order" ExceptionType="PENDING_CANCEL_ON_DECLINE" FlowName="HardDeclinedCancelAlert" ListDescription="&lt;HTML xmlns:lxslt=&quot;_http://xml.apache.org/xslt&quot;&gt;
      &lt;!--CONTENT_TYPE=text/html--&gt;
      &lt;BODY&gt;&lt;/BODY&gt;
      &lt;/HTML&gt;
      " QueueId="PendingCancelOnDecline">
			<xsl:attribute name="DetailDescription">
				<xsl:value-of select="concat('Hard Decline Cancel Failure for Shipment No: ',$varOrderNo,'')" />
			</xsl:attribute>
			<xsl:attribute name="OrderHeaderKey">
				<xsl:value-of select="@OrderHeaderKey" />
			</xsl:attribute>

			<xsl:attribute name="OrderNo">
				<xsl:value-of select="@OrderNo" />
			</xsl:attribute>
						
			<InboxReferencesList>
				<InboxReferences Name="OrderDate" ReferenceType="TEXT">
					<xsl:attribute name="Value">
						<xsl:value-of select="@OrderDate" />
					</xsl:attribute>
				</InboxReferences>
				<InboxReferences Name="MinOrderStatusDesc" ReferenceType="TEXT">
					<xsl:attribute name="Value">
						<xsl:value-of select="@MinOrderStatusDesc" />
					</xsl:attribute>
				</InboxReferences>
				<InboxReferences Name="MaxOrderStatusDesc" ReferenceType="TEXT">
					<xsl:attribute name="Value">
						<xsl:value-of select="@MaxOrderStatusDesc" />
					</xsl:attribute>
				</InboxReferences>
				<InboxReferences Name="Status" ReferenceType="TEXT">
					<xsl:attribute name="Value">
						<xsl:value-of select="@Status" />
					</xsl:attribute>
				</InboxReferences>
			</InboxReferencesList>
		</Inbox>
	</xsl:template>
	
</xsl:stylesheet>

