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
				<b><i>*** Order/Item Cancellation ***</i></b>
			</font>
		</WBR>
	</p>
	<!--  <br></br> -->
	<p>
		<font size="2" face="Arial">
			<b><i>Dear <xsl:value-of select="/Order/PersonInfoBillTo/@FirstName" />&#160;<xsl:value-of select="/Order/PersonInfoBillTo/@LastName" /></i></b>,
		</font>
		<br></br>
	</p>
	<p>
		<font size="2" face="Arial">			
			<i>You recently placed an order with Academy Sports + Outdoors <xsl:value-of select="/Order/@OrderNo"/> . Unfortunately, we are unable to process a portion of your order.</i> 
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
							<td bgcolor="#3366CC" width="10%" style="text-align:left">
								<font color="#FFFFFF" size="2" face="Arial"><b><i>&#160;ITEM</i></b>
								</font>
							</td>
							<td bgcolor="#3366CC" width="10%">
								<font color="#FFFFFF" size="2" face="Arial">
									<b><i>ITEM DESCRIPTION</i></b>
								</font>
							</td>
							<td bgcolor="#3366CC" width="10%">
								<font color="#FFFFFF" size="2" face="Arial">
									<b><i>ORDER QUANTITY</i></b>
								</font>
							</td>																					
						</tr>
			
						<xsl:for-each select="/Order/OrderLines/OrderLine">
							<tr valign="middle" style="text-align:center">
								<td height="35" style="text-align:left">
									<font size="2" face="Arial">
										<xsl:value-of select="Item/@ItemID"/>
									</font>
								</td>
								
								<td>
									<font size="2" face="Arial">
										<xsl:value-of select="Item/@ItemDesc" />
									</font>
								</td>
								<td>
									<font size="2" face="Arial">
										<xsl:value-of select="format-number(@OriginalOrderedQty, '0')" />
									</font>
								</td>
																													
							</tr>
						</xsl:for-each>
					</table>
				</td>
			</tr>
		</table>		
		<br/>
		<table width="684">
				<tr>
					<td>
					<font size="2" face="Arial">
					<i>At this time, the remainder of your order, as described above, will be canceled. We apologize for the inconvenience. 
If you need further assistance, please contact Customer Service at www.academy.com/contactus. You may also call us at 1-888-922-2336 or chat and an Academy Associate will gladly assist you to place a new order. </i>

				
					</font>
				</td>
			</tr>
		</table>
				<br/>
				<table width="684">
				<tr>
					<td>
					<font size="2" face="Arial">
						<b><i>Please note:</i></b>	<i>This email message was sent from a notification-only address that cannot accept incoming e-mail. <br />Please do not reply to this message.</i>
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