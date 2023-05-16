<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lxslt="http://xml.apache.org/xslt"
                version="1.0">
<xsl:output method="html"/>
<xsl:template match="/">
<xsl:text disable-output-escaping="yes"><![CDATA[   
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">]]>
</xsl:text>
<html>
<xsl:comment>CONTENT_TYPE=text/html; charset=UTF-8</xsl:comment>
<head>
<xsl:text disable-output-escaping="yes">
<![CDATA[<META http-equiv="Content-Type" content="text/html; charset=ascii"]]>
</xsl:text>
<title></title>
<style type="text/css" media="print">.hide{display:none}</style>
                </head>
<body>
<br/>
<br/>
<table width="684" border="0" cellspacing="0" cellpadding="0">
<tr>
                                <td>
                                                <font size="2" face="Arial">
                                                The following are inventory_item_key for the list of items that are with out LPN# :
                                                </font>
                                </td>
                </tr>
</table>
<br/>
<br/>
<br/>


			<table width="684" border="1" cellspacing="0" cellpadding="0">
				<tr valign="top" style="text-align:center">
					<td bgcolor="#3366CC" width="20%" colspan="4" style="text-align:center">
						<font color="#FFFFFF" size="2" face="Arial"><b>INVENTORY ITEM KEY</b></font>
					</td>
					<td bgcolor="#3366CC" width="20%" colspan="2" style="text-align:center">
						<font color="#FFFFFF" size="2" face="Arial"><b>LOCATION ID</b></font>
					</td>
					<td bgcolor="#3366CC" width="10%" colspan="2" >
						<font color="#FFFFFF" size="2" face="Arial"><b>PEND OUT QTY</b></font>
					</td>
					<td bgcolor="#3366CC" width="10%" colspan="2" >
						<font color="#FFFFFF" size="2" face="Arial"><b>QUANTITY</b></font>
					</td>
				</tr>
				<!-- ONLY items without LPN# is needed to be listed -->
				
				<xsl:for-each select="/NodeInventory/LocationInventoryList/LocationInventory">
					
							<tr valign="top" style="text-align:center" >
								<td width="20%" colspan="4" style="text-align:center">
									<font size="2" colspan="4" face="Arial">
										<b><xsl:value-of select="@InventoryItemKey" /></b>
									</font>
								</td>
								<td width="20%" colspan="2" style="text-align:center">
									<font size="2" colspan="2" face="Arial">
										<xsl:value-of select="@LocationId" />
									</font>
								</td>
								<td width="10%" colspan="2" >
									<font size="2" face="Arial">
										<xsl:value-of select="format-number(@PendOutQty, '0')" />
									</font>
								</td>
								<td width="10%" colspan="2" >
									<font size="2" face="Arial">
										<xsl:value-of select="format-number(@Quantity, '0')" />
									</font>
								</td>
							</tr>
					</xsl:for-each>
			</table>
		
<br/>
<br/>
<br/>
<table width="684" border="0" cellspacing="0" cellpadding="0">
                <tr>
                                <td>
                                                <font size="2" face="Arial">
                                                                <b>Please note:</b>This email message was sent from a notification-only address that cannot accept incoming email. <br />Please do not reply to this message.
                                                </font>
                                </td>
                </tr>
</table>
</body>
</html>
</xsl:template>
</xsl:stylesheet>