<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:lxslt="http://xml.apache.org/xslt">

	<xsl:output method="xml" indent="yes"
		cdata-section-elements="ShippedItemHTMLContent PaymentHTMLContent ProcessingItemHTMLContent CancelledItemHTMLContent ShippingCarrierHTMLContent TrackingNumberHTMLContent" encoding="UTF-8" />
	<xsl:template match="node() | @*">
		<xsl:copy>
			<xsl:apply-templates select="node() | @*" />
		</xsl:copy>
	</xsl:template>
	<xsl:template match="/Order/ProductInfo/Shipped">

		<xsl:copy>
			<xsl:copy-of select="*" />
			<xsl:if test="not(ShippedItemHTMLContent)">
			<xsl:if test="/Order/ProductInfo/Shipped/Package/ItemInfo/@ItemID!= ''">
				<ShippedItemHTMLContent>
				<xsl:for-each select="/Order/ProductInfo/Shipped/Package">
				<xsl:if test="./@LineBreak= 'Y'"><![CDATA[<hr/>]]></xsl:if>
					<![CDATA[<table class="stack" width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation">
  <tr>
    <th class="tablet-copy-14 mobile-copy-14" valign="middle" align="center" style="text-align: left; font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 18px; line-height: 26px; color: #707070;">
      <span style="font-weight: 600">Package  ]]><xsl:value-of select="./@PackageNo" /><![CDATA[ of ]]><xsl:value-of select="../@ShipmentCount" /><![CDATA[</span> &nbsp;&nbsp;&nbsp; Items (]]><xsl:value-of select="./@ItemCount"/><![CDATA[)
    </th>
    <th class="tablet-copy-16 mobile-copy-14" valign="middle" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 18px; line-height: 26px; color: #0255CC; text-align: right; font-weight: 700;">
      Arrives by ]]><xsl:value-of select="./ItemInfo/@PromiseDate" /><![CDATA[
    </th>
  </tr>
</table>]]> <xsl:for-each select="./ItemInfo">
						<xsl:variable name="InvoiceQty"
							select="./@InvoicedQty" />
						<xsl:variable name="ImageLoc" select="./@ImageLoc" />
						<xsl:variable name="ImageID" select="./@ImageID" />
						<xsl:variable name="UnitPrice" select="./@UnitPrice" /><![CDATA[<table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation"><tr><td height="20" style="height: 20px; font-size: 0; line-height: 1;"> </td></tr></table><table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation"><tr>
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
            ]]><xsl:value-of select="./@Description" /><![CDATA[
            </a>
          </td>
        </tr>
        <tr>
          <td class="tablet-copy-14 mobile-copy-14" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 18px; line-height: 20px; color: #6A6A6A;">
            <span style="font-weight: bold; color: #333333;">SKU:</span> ]]><xsl:value-of select="./@ItemID" /><![CDATA[
          </td>
        </tr>
        <tr>
          <td class="tablet-copy-14 mobile-copy-14" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 18px; line-height: 20px; color: #6A6A6A; padding-top: 15px;">
            <span style="font-weight: bold; color: #333333;">QTY:</span> ]]><xsl:value-of select="format-number($InvoiceQty, '0')" /><![CDATA[
          </td>
        </tr>
        <tr>
          <td class="mobile-copy-18" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 20px; line-height: 20px; color: #333333; font-weight: 600; padding: 15px 0;">
             $]]><xsl:value-of select="./@UnitPrice" /><![CDATA[
          </td>
        </tr>
      </table>
    </th>
  </tr>
</table>]]></xsl:for-each><![CDATA[
            <!-- Content section - START -->
            <table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation">
              <tr>
                <td align="center" style="padding-left: 20px; padding-right: 20px;">
                  <table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation">
                    <tr>
                      <td class="" align="center" style="background-color: #ffffff; border-radius: 8px; padding: 30px 20px 50px 20px;">
                        <table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation">
                          <tr>
                            <td class="mobile-copy-24 tablet-copy-24" align="center" style="font-family: 'Poppins', Arial, Helvetica, sans-serif; font-size: 30px; line-height: 36px; font-weight: 600; color: #073463; padding-bottom: 25px;">
                              <img src="https://barcode.listrakbi.com/?showCode=true&format=code128&code=]]><xsl:value-of select="./@InvoiceNumber" /><![CDATA[&foreground=000000&background=FFFFFF&padding=0&borderWidth=0&fontFamily=Verdana&fontSize=12&fontStyle=0&barHeight=50" alt="Barcode" style="display: block; max-width:100%" border="0">
                            </td>
                          </tr>
                          <tr>
                            <td class="tablet-copy-16 mobile-copy-16 tablet-pad-lr-0 mob-pad-lr-0" align="center" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-weight: 400; font-size: 21px; line-height: 26px; color: #4B4B4C; padding-left: 100px; padding-right: 100px;">
Present this barcode to the team member in-store when returning items or making exchanges.For questions about returns, please review our <a href="https://www.academy.com/return-policy?trk_msg=F7TPT6UD53A4NFIKMLUALC4TH4&trk_contact=QTNA8QLET8Q8Q80GNFR6FSFA4K&trk_module=tra&trk_sid=T4CHTNLHL7RG00959JH0NAIFOK&trk_link=L0JOTTFQ9K649EIOM5QRK1KN10&ogemid=be14fcd26f939b916fc06f37316f3a66d9ad96f53ebf687d63d8e77f241da9fe&mi_u=be14fcd26f939b916fc06f37316f3a66d9ad96f53ebf687d63d8e77f241da9fe&utm_source=listrak&utm_medium=email&utm_term=Return+Policy&utm_campaign=Transactional&utm_content=Partial+Fulfillment+-+Ship+to+Home" target="_blank" title="Return Policy">Return Policy</a>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>]]>
            <!-- Content section - END-->
</xsl:for-each>
				</ShippedItemHTMLContent>
			</xsl:if>
			
			<xsl:if test="not(PaymentHTMLContent)">
				<PaymentHTMLContent>
                    <!-- OMNI-65601 : START -->
				    <xsl:for-each select="/Order/PaymentMethods/PaymentMethod">
						<xsl:variable name="CreditCardNo" select="./@DisplayCreditCardNo" />
						<xsl:variable name="CreditCardType" select="./@CreditCardType" />
						<xsl:choose>
							<xsl:when test="$CreditCardType='Klarna'">
							<![CDATA[<strong style="font-weight: bold">]]><xsl:value-of	select="./@CreditCardType" />
							</xsl:when>
							<xsl:otherwise>
								<![CDATA[<strong style="font-weight: bold">]]><xsl:value-of select="./@CreditCardType" /><![CDATA[</strong> ending in ]]><xsl:choose><xsl:when test="normalize-space($CreditCardNo) != ''"><xsl:value-of select="$CreditCardNo"/></xsl:when><xsl:otherwise><xsl:value-of select="./@DisplaySvcNo" /></xsl:otherwise></xsl:choose><![CDATA[<br>]]>		
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
                    <!-- OMNI-65601 : END -->
				</PaymentHTMLContent>
			</xsl:if>
			
			<xsl:if test="not(ShippingCarrierHTMLContent)">
				<ShippingCarrierHTMLContent>
				<xsl:for-each select="/Order/ProductInfo/Shipped/ShippingCarrierInfo">
					<![CDATA[<text>]]><xsl:value-of select="./@ShippingCarrier" /><![CDATA[<br/></text>]]></xsl:for-each>		
				</ShippingCarrierHTMLContent>
			</xsl:if>
			
			<xsl:if test="not(TrackingNumberHTMLContent)">
				<TrackingNumberHTMLContent>
				<xsl:for-each select="/Order/ProductInfo/Shipped/TrackingInfo">
					<![CDATA[<a href="]]><xsl:value-of select="./@TrackingURL" /><![CDATA[" target="_blank" title="TrackingURL">]]><xsl:value-of select="./@TrackingNumber" /><![CDATA[</a><br/>]]></xsl:for-each>		
				</TrackingNumberHTMLContent>
			</xsl:if> </xsl:if>
		</xsl:copy>
	</xsl:template>
	
<xsl:template match="/Order/ProductInfo/Processing">

		<xsl:copy>
			<xsl:copy-of select="*" />
			<xsl:if test="not(ProcessingItemHTMLContent)">
				<ProcessingItemHTMLContent>
					<xsl:if
						test="/Order/ProductInfo/Processing/ItemInfo/@ItemID!= ''">
					
					<![CDATA[<table class="stack" width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation">
  <tr>
    <th class="tablet-copy-14 mobile-copy-14" valign="middle" align="center" style="text-align: left; font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 18px; line-height: 26px; color: #707070;">
      <span style="font-weight: 600">Package ]]><xsl:value-of select="/Order/ProductInfo/Shipped/@ShipmentCount"/><![CDATA[ of ]]><xsl:value-of select="/Order/ProductInfo/Shipped/@ShipmentCount"/><![CDATA[</span> &nbsp;&nbsp;&nbsp; Items (]]><xsl:value-of select="/Order/@ProcesingItemCount" /><![CDATA[)
    </th>
    <th class="tablet-copy-16 mobile-copy-14" valign="middle" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 18px; line-height: 26px; color: #0255CC; text-align: right; font-weight: 700;">
      TBD
    </th>
  </tr>
</table>]]><xsl:for-each select="/Order/ProductInfo/Processing/ItemInfo">
						<xsl:variable name="OrderQty"
							select="./@OrderQty" />
						<xsl:variable name="ImageLoc" select="./@ImageLoc" />
						<xsl:variable name="ImageID" select="./@ImageID" />
						<xsl:variable name="UnitPrice" select="./@UnitPrice" /><![CDATA[<table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation"><tr><td height="20" style="height: 20px; font-size: 0; line-height: 1;"> </td></tr></table><table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation"><tr>
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
            ]]><xsl:value-of select="./@Description" /><![CDATA[
            </a>
          </td>
        </tr>
        <tr>
          <td class="tablet-copy-14 mobile-copy-14" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 18px; line-height: 20px; color: #6A6A6A;">
            <span style="font-weight: bold; color: #333333;">SKU:</span> ]]><xsl:value-of select="./@ItemID" /><![CDATA[
          </td>
        </tr>
        <tr>
          <td class="tablet-copy-14 mobile-copy-14" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 18px; line-height: 20px; color: #6A6A6A; padding-top: 15px;">
            <span style="font-weight: bold; color: #333333;">QTY:</span> ]]><xsl:value-of select="format-number($OrderQty, '0')" /><![CDATA[
          </td>
        </tr>
        <tr>
          <td class="mobile-copy-18" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 20px; line-height: 20px; color: #333333; font-weight: 600; padding: 15px 0;">
             $]]><xsl:value-of select="./@UnitPrice" /><![CDATA[
          </td>
        </tr>
      </table>
    </th>
  </tr>
</table>]]></xsl:for-each>
					</xsl:if>
				</ProcessingItemHTMLContent>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	
	
	
<xsl:template match="/Order/ProductInfo/Cancelled">

		<xsl:copy>
			<xsl:copy-of select="*" />
			<xsl:if test="not(CancelledItemHTMLContent)">
				<CancelledItemHTMLContent>
					<xsl:if
						test="/Order/ProductInfo/Cancelled/ItemInfo/@ItemID!= ''">
					<xsl:for-each select="/Order/ProductInfo/Cancelled/ItemInfo">
						<xsl:variable name="OrderQty"
							select="./@OrderQty" />
						<xsl:variable name="ImageLoc" select="./@ImageLoc" />
						<xsl:variable name="ImageID" select="./@ImageID" />
						<xsl:variable name="UnitPrice" select="./@UnitPrice" /><![CDATA[<table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation"><tr><td height="20" style="height: 20px; font-size: 0; line-height: 1;"> </td></tr></table><table width="100%" cellpadding="0" cellspacing="0" border="0" role="presentation"><tr>
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
            ]]><xsl:value-of select="./@Description" /><![CDATA[
            </a>
          </td>
        </tr>
        <tr>
          <td class="tablet-copy-14 mobile-copy-14" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 18px; line-height: 20px; color: #6A6A6A;">
            <span style="font-weight: bold; color: #333333;">SKU:</span> ]]><xsl:value-of select="./@ItemID" /><![CDATA[
          </td>
        </tr>
        <tr>
          <td class="tablet-copy-14 mobile-copy-14" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 18px; line-height: 20px; color: #6A6A6A; padding-top: 15px;">
            <span style="font-weight: bold; color: #333333;">QTY:</span> ]]><xsl:value-of select="format-number($OrderQty, '0')" /><![CDATA[
          </td>
        </tr>
        <tr>
          <td class="mobile-copy-18" style="font-family: 'Hind', Arial, Helvetica, sans-serif; font-size: 20px; line-height: 20px; color: #333333; font-weight: 600; padding: 15px 0;">
             $]]><xsl:value-of select="./@UnitPrice" /><![CDATA[
          </td>
        </tr>
      </table>
    </th>
  </tr>
</table>]]></xsl:for-each>
					</xsl:if>
				</CancelledItemHTMLContent>
			</xsl:if>
		</xsl:copy>
	</xsl:template>
	
	
</xsl:stylesheet>