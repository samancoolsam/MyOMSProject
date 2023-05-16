<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:template match="/">
		<xsl:variable name="varPaymentType">
			<xsl:value-of select="Payment/@PaymentType" />
		</xsl:variable>
		<!--Start Changes STL-1449 -->
		<xsl:variable name="varOrderNo">
			<xsl:value-of select="Payment/@OrderNo" />
		</xsl:variable>

		<xsl:variable name="varRequestAmount">
			<xsl:value-of select="Payment/@TranAmount" />
		</xsl:variable>
		<!-- Start changes as part of STL:1560 -->
		<xsl:variable name="varReasonCode">		
			<xsl:choose>
				<xsl:when test="(Payment/@ReasonCode !='' )">       
					<xsl:value-of select="Payment/@ReasonCode" />
				</xsl:when>
				<xsl:when test="(Payment/@ReasonCode_CC !='' )">       
					<xsl:value-of select="Payment/@ReasonCode_CC" />
				</xsl:when>
				<xsl:when test="(Payment/@ReasonCode_PP !='')">       
					<xsl:value-of select="Payment/@ReasonCode_PP" />
				</xsl:when>
				<xsl:when test="(Payment/@ReasonCode_GC !='')">       
					<xsl:value-of select="Payment/@ReasonCode_GC" />
				</xsl:when>
			</xsl:choose>   
		</xsl:variable>
		
		<xsl:variable name="varErrorNo">		
			<xsl:choose>
				<xsl:when test="(Payment/@ErrorNo !='' )">       
					<xsl:value-of select="Payment/@ErrorNo" />
				</xsl:when>
				<xsl:when test="(Payment/@ErrorNo_CC !='' )">       
					<xsl:value-of select="Payment/@ErrorNo_CC" />
				</xsl:when>
				<xsl:when test="(Payment/@ErrorNo_PP !='')">       
					<xsl:value-of select="Payment/@ErrorNo_PP" />
				</xsl:when>
			</xsl:choose>   
		</xsl:variable>
		<!-- End changes as part of STL:1560 -->
		
		<!--End Changes STL-1449 -->

		<Inbox AssignedToUserKey="SYSTEM_USER" Consolidate="Y" ConsolidationWindow="FOREVER" Description="Payment Failed" ExceptionType="SETTLEMENT_FAILURE" FlowName="PaymentFailureAlert" ListDescription="&lt;HTML xmlns:lxslt=&quot;_http://xml.apache.org/xslt&quot;&gt;
      &lt;!--CONTENT_TYPE=text/html--&gt;
      &lt;BODY&gt;&lt;/BODY&gt;
      &lt;/HTML&gt;
      " QueueId="SettlementFailure">
			<!--Start Changes STL-1449 -->
			<xsl:attribute name="DetailDescription">
				<xsl:value-of select="concat('Settlement Failure for Order No: ',$varOrderNo,' :: PaymentType: ',$varPaymentType,' :: RequestAmount: ',$varRequestAmount,' :: ReasonCode: ',$varReasonCode,' :: ErrorNo: ',$varErrorNo,'')" />
			</xsl:attribute>
			<!--End Changes STL-1449 -->
			<xsl:attribute name="OrderHeaderKey">
				<xsl:value-of select="Payment/@OrderHeaderKey" />
			</xsl:attribute>

			<xsl:attribute name="OrderNo">
				<xsl:value-of select="Payment/@OrderNo" />
			</xsl:attribute>
			
			<!--START Changes WN-2088 different alert type for SOF-->
			<xsl:choose>
				<xsl:when test="Payment/@PaymentReference4='SOF'">						
					<xsl:attribute name="ExceptionType">
						<xsl:text>SETTLEMENT_FAILURE_SOF</xsl:text>
					</xsl:attribute>						
				</xsl:when>	
			</xsl:choose>  
			<!--End Changes WN-2088 different alert type for SOF -->
			
			<InboxReferencesList>
				<!--Start Changes STL1448 -->
				<xsl:choose>
				<xsl:when test="$varPaymentType='GIFT_CARD'">
						<InboxReferences Name="GiftCard No" ReferenceType="TEXT">
							<xsl:attribute name="Value">
								<xsl:value-of select="Payment/@SvcNo" />
							</xsl:attribute>
						</InboxReferences>
					</xsl:when>					
					<xsl:when test="$varPaymentType!='PayPal'">
						<InboxReferences Name="ErrorNo_CC" ReferenceType="TEXT">
							<xsl:attribute name="Value">
								<xsl:value-of select="Payment/@ErrorNo_CC" />
							</xsl:attribute>
						</InboxReferences>

						<InboxReferences Name="ReasonCode_CC" ReferenceType="TEXT">
							<xsl:attribute name="Value">
								<xsl:value-of select="Payment/@ReasonCode_CC" />
							</xsl:attribute>
						</InboxReferences>

						<InboxReferences Name="ErrorDesc_CC" ReferenceType="TEXT">
							<xsl:attribute name="Value">
								<xsl:value-of select="Payment/@ErrorDesc_CC" />
							</xsl:attribute>
						</InboxReferences>

						<InboxReferences Name="ReasonDesc_CC" ReferenceType="TEXT">
							<xsl:attribute name="Value">
								<xsl:value-of select="Payment/@ReasonDesc_CC" />
							</xsl:attribute>
						</InboxReferences>
					</xsl:when>

					<xsl:otherwise>
						<InboxReferences Name="ErrorNo_PP" ReferenceType="TEXT">
							<xsl:attribute name="Value">
								<xsl:value-of select="Payment/@ErrorNo_PP" />
							</xsl:attribute>
						</InboxReferences>

						<InboxReferences Name="ReasonCode_PP" ReferenceType="TEXT">
							<xsl:attribute name="Value">
								<xsl:value-of select="Payment/@ReasonCode_PP" />
							</xsl:attribute>
						</InboxReferences>

						<InboxReferences Name="ErrorDesc_PP" ReferenceType="TEXT">
							<xsl:attribute name="Value">
								<xsl:value-of select="Payment/@ErrorDesc_PP" />
							</xsl:attribute>
						</InboxReferences>

						<InboxReferences Name="ReasonDesc_PP" ReferenceType="TEXT">
							<xsl:attribute name="Value">
								<xsl:value-of select="Payment/@ReasonDesc_PP" />
							</xsl:attribute>
						</InboxReferences>
					</xsl:otherwise>
				</xsl:choose>

				<!--End Changes STL1448 -->
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

				<!--<InboxReferences Name="ReasonCode_CC" ReferenceType="TEXT"><xsl:attribute name="Value"><xsl:value-of select="Payment/@ReasonCode_CC"/></xsl:attribute></InboxReferences>-->
				<InboxReferences Name="ReasonDesc" ReferenceType="TEXT">
					<xsl:attribute name="Value">
						<xsl:value-of select="Payment/@ReasonDesc" />
					</xsl:attribute>
				</InboxReferences>

				<InboxReferences Name="ErrorNo" ReferenceType="TEXT">
					<xsl:attribute name="Value">
						<xsl:value-of select="Payment/@ErrorNo" />
					</xsl:attribute>
				</InboxReferences>

				<InboxReferences Name="OrderNo" ReferenceType="TEXT">
					<xsl:attribute name="Value">
						<xsl:value-of select="Payment/@OrderNo" />
					</xsl:attribute>
				</InboxReferences>

				<InboxReferences Name="RequestAmount" ReferenceType="TEXT">
					<xsl:attribute name="Value">
						<xsl:value-of select="Payment/@RequestAmount" />
					</xsl:attribute>
				</InboxReferences>

				<InboxReferences Name="TransactionId" ReferenceType="TEXT">
					<xsl:attribute name="Value">
						<xsl:value-of select="Payment/@TransactionId" />
					</xsl:attribute>
				</InboxReferences>
			</InboxReferencesList>
		</Inbox>
	</xsl:template>
</xsl:stylesheet>

