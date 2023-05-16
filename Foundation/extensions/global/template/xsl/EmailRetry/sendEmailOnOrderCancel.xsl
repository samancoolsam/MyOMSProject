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
<body style="margin:0;padding:0">
<div style="width:768px">
	<p>
		<a name="0.1_graphic03"></a>
		<font size="2" face="Arial">
			<!--<img src="http://mail.google.com/mail/?name=d33be9805ff33117.jpg&amp;attid=0.1&amp;disp=vahi&amp;view=att&amp;th=123752c4c537e91f" height="1" width="1" alt="Your browser may not support display of this image.">-->
 		</font>
		<br></br>
	</p>
	<p>

		<img
			src="http://assets.academy.com/mgen/64/10099564.jpg" border="0" alt="">
		</img>

	</p>


	<p>
		<WBR>
			<font size="3" face="Arial">
				<b>*** Order/Item Cancellation ***</b>
			</font>
		</WBR>
	</p>
	<!--  <br></br> -->
	<p>
		<font size="2" face="Arial">
			<b>Dear <xsl:value-of select="/Order/PersonInfoBillTo/@FirstName" />&#160;<xsl:value-of select="/Order/PersonInfoBillTo/@LastName" /></b>,
		</font>
		<br></br>
	</p>
	<p>
		<font size="2" face="Arial">			
			 Thank you for your recent order. Each merchandise item listed below has been canceled either by your request or because we are unable to fulfill the item. As such, your order total has been adjusted to reflect the removal of each canceled merchandise item. We’ve issued a full refund amount to your payment card. Credit Card refunds typically reflect within 3-5 business days, though this can vary based on bank processing times. Gift Card refunds typically reflect within 48 hours. If you no longer have your original gift card, please contact Customer Care for assistance with replacing your gift card.
		</font> 
	</p>
	<p>
		<font size="2" face="Arial"></font>
	</p>
	<a name="0.1_table01"></a>
	<div align="left">
		<table width="684" border="1" cellspacing="0">
			<tr>
				<td>
					<table width="684" border="0" cellspacing="0">						
						<tr valign="middle" style="text-align:center">
							<td bgcolor="#3366CC" width="56%" style="text-align:left">
								<font color="#FFFFFF" size="2" face="Arial"><b>&#160;ITEM DESCRIPTION</b>
								</font>
							</td>
							<td bgcolor="#3366CC" width="10%">
								<font color="#FFFFFF" size="2" face="Arial">
									<b>ORDER QTY</b>
								</font>
							</td>
							<td bgcolor="#3366CC" width="10%">
								<font color="#FFFFFF" size="2" face="Arial">
									<b>CANCEL QTY</b>
								</font>
							</td>																					
						</tr>
			
						<xsl:for-each select="/Order/OrderLines/OrderLine">
							<tr valign="middle" style="text-align:center">
								<td height="35" style="text-align:left">
									<font size="2" face="Arial">
										<xsl:value-of select="Item/@ItemDesc" />
										(<xsl:value-of select="Item/@ItemID" />)
									</font>
								</td>
								<td>
									<font size="2" face="Arial">
										<xsl:value-of select="format-number(@OriginalOrderedQty, '0')" />
									</font>
								</td>
								<td>
									<font size="2" face="Arial">					
									<xsl:choose>
									<xsl:when test="@OrderedQty='0.00'">
										<xsl:value-of select="format-number(@OriginalOrderedQty, '0')" />
									</xsl:when>
									<xsl:otherwise>										
										<xsl:value-of select="format-number((@OriginalOrderedQty)-(@OrderedQty), '0')" />
									</xsl:otherwise>
									</xsl:choose>						
									</font>
								</td>																					
							</tr>
						</xsl:for-each>
					</table>
				</td>
			</tr>
		</table>		
		<br/>
		<table width="684" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td>
					<font size="2" face="Arial">
						Thank you for shopping at Academy Sports + Outdoors.
					</font>
				</td>
			</tr>
		</table>
		<br/>
			<table width="684">
				<tr>
					<td>
					<font size="2" face="Arial">
					If you need further assistance, please contact Customer Service at <a href="http://www.academy.com/contactus" target="_blank"><u>www.academy.com/contactus</u></a>. Also, we are available 24/7 via our online chat or you may contact our Customer Service Center at 1-888-922-2336 between the hours of 7am and 12am Sunday through Saturday. Any of our Customer Service 
Associates will be glad to assist you. 						 
					</font>
				</td>
			</tr>
		</table>
				<br/>
				<table width="684">
				<tr>
					<td>
					<font size="2" face="Arial">
						<b>Please note:</b>	This email message was sent from a notification-only address that cannot accept incoming e-mail. <br />Please do not reply to this message.
					</font>
				</td>
			</tr>
		</table>
	</div>
</div>
</body>
</html>
</xsl:template>
</xsl:stylesheet>