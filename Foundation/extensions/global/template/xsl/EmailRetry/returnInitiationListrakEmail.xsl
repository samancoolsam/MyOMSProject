<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:lxslt="http://xml.apache.org/xslt">

	<xsl:output method="xml" indent="yes"
		cdata-section-elements="ReturnItemHTMLContent PaymentHTMLContent" encoding="UTF-8" />
	<xsl:template match="node() | @*">
		<xsl:copy>
			<xsl:apply-templates select="node() | @*" />
		</xsl:copy>
	</xsl:template>
	<xsl:template match="/Order/ProductInfo/Return">

		<xsl:copy>
			<xsl:copy-of select="*" />			
			<xsl:if test="not(ReturnItemHTMLContent)">
				<ReturnItemHTMLContent>
					<xsl:if test="/Order/ProductInfo/Return/ItemInfo/@ItemID!= ''">
					<xsl:for-each select="/Order/ProductInfo/Return/ItemInfo">
					<xsl:variable name="OrderQty" select="./@OrderQty" />
					<xsl:variable name="ImageLoc" select="./@ImageLoc" /><xsl:variable name="ImageID" select="./@ImageID" /> 
					<xsl:variable name="UnitPrice" select="./@UnitPrice" />
					<![CDATA[<table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation">
					<tr><td height="20" style="height: 20px; font-size: 0; line-height: 1;"> </td></tr></table>
					<table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation"><tr>
					<th class="mob-width-70" width="150" align="center" style="font-weight:normal;" valign="top">
					  <!-- Product Image - START -->
					  <table align="center" width="150" border="0" cellpadding="0" cellspacing="0">
						<tr>
						  <td align="center" style="padding: 10px; border: 1px solid #cccccc;">
							<a href="" target="_blank" title="Product">
							  <img src="]]><xsl:value-of select="concat($ImageLoc, $ImageID)"/><![CDATA[" width="116" alt="Product" border="0" style="display: block; max-width: 100%;"/>
							</a>
						  </td>
						</tr>                                 
					  </table>
					</th>
					<th width="20" style="width: 20px; font-size: 0; line-height: 1;"> </th>
					<th valign="top" align="center" style="font-weight:normal;">
					  <table width="100%" align="center" border="0" cellpadding="0" cellspacing="0">
						<tr>
						  <td class="tablet-copy-16 mobile-copy-14" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 20px; line-height: 20px; color: #4B4B4C; font-weight: 600; padding: 10px 15px 15px 0;">
							<a href="" target="_blank" title="Product" style="color: #4B4B4C; text-decoration: none;">
							]]><xsl:value-of select="./@Description"/><![CDATA[
							</a>
						  </td>
						</tr>
						<tr>
						  <td class="mobile-copy-14" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 18px; line-height: 20px; color: #6A6A6A; padding-top: 15px;">
							<span style="font-weight: bold; color: #333333;">QTY:</span> ]]><xsl:value-of select="format-number($OrderQty, '0')"/><![CDATA[
						  </td>
						  <td class="mobile-copy-14" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 20px; line-height: 20px; color: #333333; font-weight: 600; padding: 15px 0;">
							 -$]]><xsl:value-of select="format-number(($OrderQty * ./@UnitPrice),'#,###.00')"/><![CDATA[
						  </td>
						</tr>
						<tr>
						  <td class="tablet-copy-14 mobile-copy-14" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 18px; line-height: 20px; color: #6A6A6A;">
							<span style="font-weight: bold; color: #333333;">SKU:</span> ]]><xsl:value-of select="./@ItemID"/><![CDATA[
						  </td>
						</tr>						
						<tr>
						  <td class="mobile-copy-14" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 20px; line-height: 20px; color: #333333; font-weight: 600; padding: 15px 0;">
							]]>Amount Paid<![CDATA[
						  </td>
						  <td class="mobile-copy-14" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 20px; line-height: 20px; color: #333333; font-weight: 600; padding: 15px 0;">
							 -$]]><xsl:value-of select="./@LineTotal"/><![CDATA[
						  </td>
						</tr>
					  </table>
					</th>
					</tr>
					</table>]]>
					</xsl:for-each>
					</xsl:if>
				</ReturnItemHTMLContent>
			</xsl:if>
			<xsl:if test="not(PaymentHTMLContent)">
                <PaymentHTMLContent>
                    <!-- OMNI-65601 : START -->
                    <xsl:for-each select="/Order/OrderLines/OrderLine[1]/DerivedFromOrder/PaymentMethods/PaymentMethod">
                        <xsl:variable name="CreditCardNo" select="./@DisplayCreditCardNo" />
                        <xsl:variable name="CreditCardType" select="./@CreditCardType" />
                        <xsl:choose>
                            <xsl:when test="$CreditCardType='Klarna'">
                            <![CDATA[
								<table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation">            
								  <tr>
									<td valign="top" width="80%" align="center" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 18px; font-weight: 500; color: #333333; text-align: left; padding-top: 15px; padding-bottom: 15px;">
									<strong style="font-weight: bold">
							]]><xsl:value-of	select="./@CreditCardType" />
							<![CDATA[
									</td>
								  </tr>
							  </table>
							 ]]>
                            </xsl:when>
                            <xsl:otherwise>    
								<![CDATA[
									<table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation">            
									  <tr>
										<td valign="top" width="80%" align="center" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 18px; font-weight: 500; color: #333333; text-align: left; padding-top: 15px; padding-bottom: 15px;">
										<strong style="font-weight: bold">
								]]><xsl:value-of select="./@CreditCardType" />
								<![CDATA[
								</strong> ending in
									]]>							
								<xsl:choose>
								<xsl:when test="normalize-space($CreditCardNo) != ''">					 <![CDATA[]]>   <xsl:value-of select="$CreditCardNo"/>
								</xsl:when>
								<xsl:otherwise>					
								<![CDATA[]]> 	<xsl:value-of select="./@DisplaySvcNo" />
								</xsl:otherwise>
								</xsl:choose>
								<![CDATA[
									</td>
								  </tr>
							  </table>
							 ]]>       
                            </xsl:otherwise>
                        </xsl:choose>
                   </xsl:for-each>
                    <!-- OMNI-65601 : END -->
                </PaymentHTMLContent>
            </xsl:if>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>