<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:lxslt="http://xml.apache.org/xslt">

	<xsl:output method="xml" indent="yes"
		cdata-section-elements="RFCPItemHTMLContent CancelledItemHTMLContent" encoding="UTF-8"/>
	<xsl:template match="node() | @*">
		<xsl:copy>
			<xsl:apply-templates select="node() | @*" />
		</xsl:copy>
	</xsl:template>
	<xsl:template match="/Order/ProductInfo/RFCP">

	<xsl:copy>
        <xsl:copy-of select="*"/>
        <xsl:if test="not(RFCPItemHTMLContent)">
     <RFCPItemHTMLContent><xsl:if test="/Order/ProductInfo/RFCP/ItemInfo/@ItemID!= ''"><xsl:for-each select="/Order/ProductInfo/RFCP/ItemInfo"><xsl:variable name="OrderQty" select="./@OrderQty" /><xsl:variable name="ImageLoc" select="./@ImageLoc" /><xsl:variable name="ImageID" select="./@ImageID" /> <xsl:variable name="UnitPrice" select="./@UnitPrice" /><![CDATA[<table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation"><tr><td height="20" style="height: 20px; font-size: 0; line-height: 1;"> </td></tr></table><table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation"><tr>
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
          <td class="tablet-copy-14 mobile-copy-14" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 18px; line-height: 20px; color: #6A6A6A;">
            <span style="font-weight: bold; color: #333333;">SKU:</span> ]]><xsl:value-of select="./@ItemID"/><![CDATA[
          </td>
        </tr>
        <tr>
          <td class="tablet-copy-14 mobile-copy-14" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 18px; line-height: 20px; color: #6A6A6A; padding-top: 15px;">
            <span style="font-weight: bold; color: #333333;">QTY:</span> ]]><xsl:value-of select="format-number($OrderQty, '0')"/><![CDATA[
          </td>
        </tr>
        <tr>
          <td class="mobile-copy-18" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 20px; line-height: 20px; color: #333333; font-weight: 600; padding: 15px 0;">
             $]]><xsl:value-of select="./@UnitPrice"/><![CDATA[
          </td>
        </tr>
      </table>
    </th>
  </tr>
</table>]]></xsl:for-each></xsl:if>
</RFCPItemHTMLContent>
        </xsl:if>
    </xsl:copy> 	
</xsl:template>

<xsl:template match="/Order/ProductInfo/Cancelled">
<xsl:copy>
        <xsl:copy-of select="*"/>
        <xsl:if test="not(CancelledItemHTMLContent)"><CancelledItemHTMLContent>	 <xsl:if test="/Order/ProductInfo/Cancelled/ItemInfo/@ItemID!= ''"><xsl:for-each select="/Order/ProductInfo/Cancelled/ItemInfo"><xsl:variable name="OrderQty" select="./@OrderQty" /><xsl:variable name="CancelQty" select="./@CancelQty" /><xsl:variable name="ImageLoc" select="./@ImageLoc" /><xsl:variable name="ImageID" select="./@ImageID" /><xsl:variable name="UnitPrice" select="./@UnitPrice" /><![CDATA[<table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation"><tr><td height="20" style="height: 20px; font-size: 0; line-height: 1;"> </td></tr></table><table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation">
  <tr>
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
            ]]><xsl:value-of select="./@Description"/><![CDATA[</a>
          </td>
        </tr>
        <tr>
          <td class="tablet-copy-14 mobile-copy-14" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 18px; line-height: 20px; color: #6A6A6A;">
            <span style="font-weight: bold; color: #333333;">SKU:</span> ]]><xsl:value-of select="./@ItemID"/><![CDATA[
          </td>
        </tr>
        <tr>
          <td class="tablet-copy-14 mobile-copy-14" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 18px; line-height: 20px; color: #6A6A6A; padding-top: 15px;">
            <span style="font-weight: bold; color: #333333;">QTY:</span> ]]><xsl:value-of select="format-number($CancelQty, '0')" /><![CDATA[</td>
        </tr>
        <tr>
          <td class="tablet-copy-14 mobile-copy-14" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 18px; line-height: 20px; color: #6A6A6A; padding-top: 15px;">
            <span style="font-weight: bold; color: #333333;">$</span><b>]]><xsl:value-of select="./@UnitPrice" /><![CDATA[</b></td>
        </tr>
        <tr>
      </table>
    </th>
  </tr>
</table>]]></xsl:for-each>
</xsl:if></CancelledItemHTMLContent>	
 </xsl:if>
 </xsl:copy> 		
</xsl:template>
</xsl:stylesheet>