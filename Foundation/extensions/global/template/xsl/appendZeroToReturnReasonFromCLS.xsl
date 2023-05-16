<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
<!-- This xsl will check the ReturnReason sent by CLS,if it is a single digit,then prefix 0 to the ReturnReason-->
    <Order>
	<!--copy all the attributes under Order element-->
		<xsl:copy-of select="/Order/@*"/>
             <OrderLines>
			 <!--loop through all the OrderLine element-->
                <xsl:for-each select="/Order/OrderLines/OrderLine">
                <OrderLine>
                     <xsl:copy-of select="@*|node()"/>      
                     <xsl:variable name="ReasonCodelen">
                     <xsl:value-of select="@ReturnReason" />
                     </xsl:variable>
		       <!--if ReturnReason does not contain 0 and string length is less that 2 then concatenate 0 to the ReturnReason value coming from CLS-->
                <xsl:if test="not(contains(@ReturnReason, '0')) and string-length($ReasonCodelen)&lt;2">
				<xsl:attribute name="ReturnReason">
						<xsl:value-of select="concat('0',@ReturnReason)"/>
				</xsl:attribute>
                </xsl:if>			
				</OrderLine>
                </xsl:for-each>                   
            </OrderLines>              
     </Order>    
</xsl:template>
</xsl:stylesheet>


