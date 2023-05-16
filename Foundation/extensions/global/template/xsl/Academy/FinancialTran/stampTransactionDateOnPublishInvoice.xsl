<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<InvoiceDetail>
			<InvoiceHeader>
				
				<!--The below tag, <xsl:copy-of select="InvoiceDetail/InvoiceHeader/@*"/>
				will copy all attribute present under InvoiceDetail/InvoiceHeader of the input XML as is to the output xml document.  -->
				<xsl:copy-of select="/InvoiceDetail/InvoiceHeader/@*"/>

				<!--The below tag, 	<xsl:attribute name="TransactionDate"> creates a new attribute and the 
				choose-when-otherwise control flow checks for the value of InvoiceType attribute (InvoiceDetail/InvoiceHeader/@InvoiceType) and assigns 
				value to the attribute, TransactionDate.   -->
				<xsl:attribute name="TransactionDate">
					<xsl:choose>
						<xsl:when test="InvoiceDetail/InvoiceHeader/@InvoiceType='CREDIT_MEMO'">
						  <!-- If the Invoice Type is CREDIT_MEMO, then loop through all the OrderHoldType tags and checks for holdType attribute. If the hold is from Associate
                                                                           or Senior Manager and if Status of the Hold is Resolved then assigns  the value of Modifyts to the new attribute TransactionDate-->
						   <xsl:for-each select="/InvoiceDetail/InvoiceHeader/Order/OrderHoldTypes/OrderHoldType">										   
						     <xsl:if test="((@HoldType='SITE_LEAD_HOLD') or (@HoldType ='ASSOCIATE_HOLD'))">
							     <xsl:if test="(@Status ='1300')">
							    <xsl:value-of select="@Modifyts"/>
								</xsl:if>
						     </xsl:if>
						   </xsl:for-each>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="InvoiceDetail/InvoiceHeader/@DateInvoiced"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:attribute>
				
				<!--The below code copies all the node and its atttributes from the input xml to the 
				output xml document except /InvoiceDetail/InvoiceHeader/Order/OrderHoldTypes. -->
				<xsl:apply-templates select="node() | @*" />
			</InvoiceHeader>
		</InvoiceDetail>
	</xsl:template>


	<!--To copy all the child node & attributes of /InvoiceDetail/InvoiceHeader 
	node except /InvoiceDetail/InvoiceHeader/Order/OrderHoldTypes..  -->
	<xsl:template match="node() | @*">
   		<xsl:copy-of select="/InvoiceDetail/InvoiceHeader/Extn"/>
   		
		<!-- to fetch the attributes of <Order> node and its child elements except <OrderHoldTypes> -->
   		<Order>
   			<xsl:copy-of select="/InvoiceDetail/InvoiceHeader/Order/@*"/>
    		<xsl:copy-of select="/InvoiceDetail/InvoiceHeader/Order/PriceInfo"/>
    		<xsl:copy-of select="/InvoiceDetail/InvoiceHeader/Order/PersonInfoBillTo"/>
    		<xsl:copy-of select="/InvoiceDetail/InvoiceHeader/Order/AdditionalAddresses"/>
    		<xsl:copy-of select="/InvoiceDetail/InvoiceHeader/Order/References"/>
    	</Order>
		
	    <xsl:copy-of select="/InvoiceDetail/InvoiceHeader/Shipment"/>
		<xsl:copy-of select="/InvoiceDetail/InvoiceHeader/LineDetails"/>
		<xsl:copy-of select="/InvoiceDetail/InvoiceHeader/TotalSummary"/>
		<xsl:copy-of select="/InvoiceDetail/InvoiceHeader/HeaderCharges"/>
		<xsl:copy-of select="/InvoiceDetail/InvoiceHeader/HeaderTaxes"/>
		<xsl:copy-of select="/InvoiceDetail/InvoiceHeader/CollectionDetails"/>
	</xsl:template>
</xsl:stylesheet>