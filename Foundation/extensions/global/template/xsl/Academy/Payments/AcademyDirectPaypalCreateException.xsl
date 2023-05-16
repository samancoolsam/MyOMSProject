<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:template match="/">
		<xsl:variable name="varPaymentType">
			<xsl:value-of select="Payment/@PaymentType" />
		</xsl:variable>
		
		<xsl:variable name="varOrderNo">
			<xsl:value-of select="Payment/@OrderNo" />
		</xsl:variable>

		<xsl:variable name="varRequestAmount">
			<xsl:value-of select="Payment/@TranAmount" />
		</xsl:variable>
		
		<xsl:variable name="varReasonCode">		
			<xsl:choose>
				<xsl:when test="(Payment/@ReasonCode !='' )">       
					<xsl:value-of select="Payment/@ReasonCode" />
				</xsl:when>
			</xsl:choose>   
		</xsl:variable>
		
		<xsl:variable name="varErrorNo">		
			<xsl:choose>
				<xsl:when test="(Payment/@ErrorNo !='' )">       
					<xsl:value-of select="Payment/@ErrorNo" />
				</xsl:when>
			</xsl:choose>   
		</xsl:variable>
		
		<Inbox AssignedToUserKey="SYSTEM_USER" Consolidate="Y" ConsolidationWindow="FOREVER" Description="Payment Failed" ExceptionType="SETTLEMENT_FAILURE" FlowName="PaymentFailureAlert" ListDescription="&lt;HTML xmlns:lxslt=&quot;_http://xml.apache.org/xslt&quot;&gt;
      &lt;!--CONTENT_TYPE=text/html--&gt;
      &lt;BODY&gt;&lt;/BODY&gt;
      &lt;/HTML&gt;
      " QueueId="SettlementFailure">
			<xsl:attribute name="Type">
				<xsl:value-of select="Payment/@InternalReturnMessage" />
			</xsl:attribute>
			<xsl:attribute name="DetailDescription">
				<xsl:value-of select="concat('Payment Transaction Failure for Order No: ',$varOrderNo,' :: PaymentType: ',$varPaymentType,' :: RequestAmount: ',$varRequestAmount,' :: ReasonCode: ',$varReasonCode,' :: ErrorNo: ',$varErrorNo,'')" />
			</xsl:attribute>			
			<xsl:attribute name="OrderHeaderKey">
				<xsl:value-of select="Payment/@OrderHeaderKey" />
			</xsl:attribute>
			<xsl:attribute name="OrderNo">
				<xsl:value-of select="Payment/@OrderNo" />
			</xsl:attribute>
			<xsl:choose>
				<xsl:when test="Payment/@PaymentReference4='SOF'">						
					<xsl:attribute name="ExceptionType">
						<xsl:text>SETTLEMENT_FAILURE_SOF</xsl:text>
					</xsl:attribute>						
				</xsl:when>	
			</xsl:choose>
			<InboxReferencesList>
				<InboxReferences Name="ErrorDesc" ReferenceType="TEXT">
					<xsl:attribute name="Value">
						<xsl:value-of select="Payment/@ErrorDesc" />
					</xsl:attribute>
				</InboxReferences>

				<InboxReferences Name="StatusCode" ReferenceType="TEXT">
					<xsl:attribute name="Value">
						<xsl:value-of select="Payment/@StatusCode" />
					</xsl:attribute>
				</InboxReferences>

				<InboxReferences Name="ReasonCode" ReferenceType="TEXT">
					<xsl:attribute name="Value">
						<xsl:value-of select="Payment/@ReasonCode" />
					</xsl:attribute>
				</InboxReferences>

				<InboxReferences Name="ReasonDesc" ReferenceType="TEXT">
					<xsl:attribute name="Value">
						<xsl:value-of select="Payment/@ReasonDesc" />
					</xsl:attribute>
				</InboxReferences>

				<!--<InboxReferences Name="ErrorNo" ReferenceType="TEXT">
					<xsl:attribute name="Value">
						<xsl:value-of select="Payment/@ErrorNo" />
					</xsl:attribute>
				</InboxReferences>-->

				<InboxReferences Name="OrderNo" ReferenceType="TEXT">
					<xsl:attribute name="Value">
						<xsl:value-of select="Payment/@OrderNo" />
					</xsl:attribute>
				</InboxReferences>

				<InboxReferences Name="RequestAmount" ReferenceType="TEXT">
					<xsl:attribute name="Value">
						<xsl:value-of select="Payment/@TranAmount" />
					</xsl:attribute>
				</InboxReferences>

				<InboxReferences Name="TransactionType" ReferenceType="TEXT">
					<xsl:attribute name="Value">
						<xsl:value-of select="Payment/@TransactionId" />
					</xsl:attribute>
				</InboxReferences>
			</InboxReferencesList>
			<ConsolidationTemplate>
				<Inbox  Description="" ExceptionType="" OrderHeaderKey="" QueueId="" Status=""/>
			</ConsolidationTemplate>
		</Inbox>
	</xsl:template>
</xsl:stylesheet>

