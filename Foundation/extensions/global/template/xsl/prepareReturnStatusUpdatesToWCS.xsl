<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml"
		doctype-system='Update_Academy_OrderStatus_10.dtd' indent="yes" />
	<xsl:template match="/">
		<Update_Academy_OrderStatus version="1.0">
			<ControlArea>
				<Verb value="Update" />
				<Noun value="Academy_OrderStatus" />
			</ControlArea>
			<DataArea>
				<OrderStatus>
					<xsl:variable name="messageType"
						select="Order/@MessageType" />
					<xsl:variable name="publishPaymentInfoFlag"
						select="Order/@PublishPaymentInfoFlag" />
					<OrderStatusHeader Versioning="FALSE"
						UpdateMode="P">
						<OrderNumber>
							<xsl:attribute name="type">
                       <xsl:value-of
								select="/Order/@InitiatedBy" />
                     </xsl:attribute>
							<xsl:value-of
								select="Order/OrderLines/OrderLine/DerivedFromOrder/@OrderNo" />
						</OrderNumber>

						<xsl:if test="$messageType='ReturnInitiated'">
							<ReturnDate>
								<xsl:attribute name="ReturnDate">
                                <xsl:value-of
									select="Order/@OrderReturnDate" />
                                </xsl:attribute>
							</ReturnDate>
						</xsl:if>
					</OrderStatusHeader>


					<xsl:for-each select="Order/OrderLines/OrderLine">
						<OrderStatusItem Versioning="FALSE">
							<ItemNumber>
								<xsl:attribute name="type">
                                    <xsl:value-of
									select="/Order/@InitiatedBy" />
                                </xsl:attribute>
								<xsl:attribute name="ItemID">
                                <xsl:value-of
									select="ItemDetails/@ItemID" />
                                </xsl:attribute>
								<xsl:value-of select="@ExtnWCOrderItemIdentifier" />
							</ItemNumber>
							<QuantityInfo>
								<xsl:choose>
									<xsl:when test="$messageType='ReturnInitiated'">
										<ReturnedQty>
											<xsl:value-of select="@OrderedQty" />
										</ReturnedQty>
									</xsl:when>
									<xsl:otherwise>
										<ActualReturnedQty>
											<xsl:value-of select="@OrderedQty" />
										</ActualReturnedQty>
									</xsl:otherwise>
								</xsl:choose>
							</QuantityInfo>
							<Status StatusFlag="I">
								<xsl:attribute name="StatusCondition">
                                <xsl:value-of
									select="/Order/@StatusCondition" />
                                </xsl:attribute>
							</Status>
							<xsl:if test="$publishPaymentInfoFlag='Y'">
								<RefundAmount>
									<ItemAmount>
										<xsl:value-of
											select="LineOverallTotals/@ExtendedPrice" />
									</ItemAmount>
									<Taxes>
										<xsl:value-of select="LineOverallTotals/@Tax" />
									</Taxes>
									<AdjustmentAmount>
										<xsl:value-of
											select="LineOverallTotals/@Discount" />
									</AdjustmentAmount>
									<Charges>
										<xsl:value-of select="LineOverallTotals/@Charges" />
									</Charges>
									<RefundAmount>
										<xsl:value-of
											select="LineOverallTotals/@LineTotal" />
									</RefundAmount>
								</RefundAmount>
							</xsl:if>

							<RMAOrder>
								<xsl:value-of select="/Order/@OrderNo" />
							</RMAOrder>
							<xsl:if test="$messageType='ReturnInitiated'">
								<ReturnReason>
									<ReturnReasonCode>
										<xsl:attribute name="ReturnReasonCode">
                                        <xsl:value-of
											select="@ReturnReason" />
                                        </xsl:attribute>
									</ReturnReasonCode>
								</ReturnReason>
							</xsl:if>
						</OrderStatusItem>
					</xsl:for-each>
				</OrderStatus>
			</DataArea>
		</Update_Academy_OrderStatus>
	</xsl:template>
</xsl:stylesheet>




 