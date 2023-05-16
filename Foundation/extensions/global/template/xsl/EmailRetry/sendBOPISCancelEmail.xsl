
<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:lxslt="http://xml.apache.org/xslt">

	<xsl:output method="xml" encoding="UTF-8"
		cdata-section-elements="CancelItemHTMLContent" />

	<xsl:template match="node() | @*">

		<xsl:copy>

			<xsl:apply-templates select="node() | @*" />

		</xsl:copy>

	</xsl:template>

	<xsl:template match="/Order">

		<xsl:copy>
			<xsl:apply-templates select="@*" />
			<xsl:apply-templates select="node()" />
			<xsl:if test="not(ProductInfo)">
				<ProductInfo>
					<Cancel>
						<CancelItemHTMLContent>
							<xsl:for-each select="/Order/OrderLines/OrderLine">	
							<xsl:variable name="OrderQty" select="./@OriginalOrderedQty" />
							<xsl:variable name="CancelQty" select="./@OriginalOrderedQty" />
							<xsl:variable name="ImageLoc" select="./ItemDetails/PrimaryInformation/@ImageLocation" />
							<xsl:variable name="ImageID" select="./ItemDetails/PrimaryInformation/@ImageID" /><![CDATA[
<table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation"><tr><td height="20" style="height: 20px; font-size: 0; line-height: 1;"> </td></tr></table>

<table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation">
  <tr>
    <th class="mob-width-70" width="150" align="center" style="font-weight:normal;" valign="top">
      <!-- Product Image - START -->
      <table align="center" width="150" border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td align="center" style="padding: 10px; border: 1px solid #cccccc;">
            <a href="" target="_blank" title="Product">
              <img src="]]><xsl:value-of select="concat($ImageLoc, $ImageID)" /><![CDATA[" width="116" alt="Product" border="0" style="display: block; max-width: 100%;"/>
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
            ]]><xsl:value-of select="ItemDetails/PrimaryInformation/@Description"/><![CDATA[
            </a>
          </td>
        </tr>
        <tr>
          <td class="tablet-copy-14 mobile-copy-14" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 18px; line-height: 20px; color: #6A6A6A;">
            <span style="font-weight: bold; color: #333333;">SKU:</span> 
            ]]><xsl:value-of select="ItemDetails/@ItemID" /><![CDATA[
          </td>
        </tr>
        <tr>
          <td class="tablet-copy-14 mobile-copy-14" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 18px; line-height: 20px; color: #6A6A6A; padding-top: 15px;">
            <span style="font-weight: bold; color: #333333;">Order QTY:</span> 
            ]]><xsl:value-of select="format-number($OrderQty, '0')" /><![CDATA[
          </td>
        </tr>
		<tr>
          <td class="tablet-copy-14 mobile-copy-14" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 18px; line-height: 20px; color: #6A6A6A; padding-top: 15px;">
            <span style="font-weight: bold; color: #333333;">Cancel QTY:</span> 
            ]]><xsl:value-of select="format-number($CancelQty, '0')" /><![CDATA[
          </td>
        </tr>
      </table>
    </th>
  </tr>
</table>]]></xsl:for-each></CancelItemHTMLContent>
					</Cancel>
				</ProductInfo>
			</xsl:if>
		</xsl:copy>

	</xsl:template>
</xsl:stylesheet>