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
<div style="width:778px">
  <p><a name="0.1_graphic03"></a>
  <font size="2" face="Arial">
	  <!--<img src="http://mail.google.com/mail/?name=d33be9805ff33117.jpg&amp;attid=0.1&amp;disp=vahi&amp;view=att&amp;th=1237430d3d17e3c9" height="1" width="1" alt="Your browser may not support display of this image.">-->
  </font>
 <br>
 </br>
 </p>
 <p>

<img src="http://assets.academy.com/mgen/64/10099564.jpg" border="0" alt=""></img>

</p>

<p> 
<WBR>
<table><tr><td>
<font size="3" face="Arial">
<b>*** Specialized Delivery Return ***</b>
</font>
</td></tr></table>
<!-- <br> -->
<!-- </br> -->
</WBR>
 </p>

<table width="697">
	<tr>
		<td>
			<p>
				<font size="2" face="Arial">
					<b>Dear <xsl:value-of select="/Order/PersonInfoBillTo/@FirstName"/>&#160;<xsl:value-of select="/Order/PersonInfoBillTo/@LastName"/>,</b>
				</font>
				<br></br>
			</p>
			<p>
				<font size="2" face="Arial" width="388">Thank you for contacting Academy Sports + Outdoors. This is a notification that we have created a return order for the item(s) you requested.  Item(s) received will be credited to the payment method used in placing your order, less applicable shipping costs.  If you paid with a gift card, a replacement gift card will be shipped to you using the address listed below. You will receive a final email notification containing the total amount of your credit once your return has completed processing.
				</font>
 				<br></br>
 			</p>
 			<p>
				<font size="2" face="Arial">
					<b>Please note the following important information:</b>
				</font>
			</p>

			<font size="2" face="Arial">
				<ul>
					<p><li>Academy's specialized delivery carrier will contact you directly to schedule a pickup appointment within two (2) business days.</li></p>
					<p><li>Business days are Monday-Friday, excluding federal holidays within the United States.</li></p>
					<p><li>Typically, the specialized delivery carrier requires a minimum of a four-hour window for the pickup, and an adult (over 18) must be available to sign for the pickup.</li></p>
					<p><li>The item will be picked up at your designated shipping address shown below.</li></p>
					<p><li>At the time of pickup, the item should be in its original packaging, and located in an easily accessible, dry area.</li></p>
					<!--<STL-376 Update Specialized Delivery Return email>-->
					<p><li>If you are not contacted by our delivery carrier within two (2) business days, please contact our Customer Service at 1-888-922-2336 for assistance. You may also contact Customer Service at <font color="#0000FF" size="2" face="Arial"><a href="http://www.academy.com/contactus" target="_blank"><u>www.academy.com/contactus</u></a></font>.</li></p>
				</ul> 
			</font>
		</td>
	</tr>
</table>
<a name="0.1_table01"></a>
<div align="left">
	<table width="697" border="1" cellspacing="0">
		<tr>
			<td>
				<table width="697" border="0" cellspacing="1">
					<tr valign="top">
						<td bgcolor="#3366CC" colspan="11" height="11">
							<font color="#FFFFFF" size="2" face="Arial"><b>RETURN SUMMARY</b></font>
						</td>
					</tr>
			
					<tr valign="top">
						<td colspan="11" height="11">
							<xsl:variable name="TNo"><xsl:value-of select="/Order/@CustomerEMailID" /></xsl:variable>
							<font size="2" face="Arial">Email Address:  <a href="{$TNo}"><xsl:value-of select="/Order/@CustomerEMailID" /></a></font>
						</td>
					</tr>
			
					<tr valign="top">
						<td bgcolor="#EFEFEF" colspan="11" height="35">
							<table width="100%" border="0" cellspacing="0" cellpadding="0">
								<tr>
									<td valign="middle"><font size="2" face="Arial"><b>Original Sales Order #: <xsl:value-of select="/Order/OrderLines/OrderLine/DerivedFromOrder/@OrderNo"/></b></font></td>
									<td valign="middle"><font size="2" face="Arial"><b>New Return Order #: <xsl:value-of select="/Order/@OrderNo"/></b></font></td>	
								</tr>
							</table>
						</td>
					</tr>
					<tr>
						<td>
							<p>&#160;</p>
						</td>
					</tr>
					<tr>
						<td colspan="11">
							<table width="100%" border="0" cellspacing="0" cellpadding="0">
								<tr style="text-align:left">	
									<td colspan="5" valign="top"><font size="2" face="Arial"><b>Pick-up Address: </b></font></td>	
									<td>
										<font size="2" face="Arial">
											<xsl:value-of select="/Order/PersonInfoBillTo/@FirstName"/>&#160;<xsl:value-of select="/Order/PersonInfoBillTo/@LastName"/><br/>
											 <xsl:value-of select="/Order/PersonInfoShipTo/@AddressLine1"/><br/>
											 
											 <xsl:variable name="addressLine2" select="/Order/PersonInfoShipTo/@AddressLine2" /> 
											 
											 <xsl:if test="$addressLine2!=''">
											<xsl:value-of select="/Order/PersonInfoShipTo/@AddressLine2"/><br/>
											</xsl:if>
											 
											 <xsl:value-of select="/Order/PersonInfoShipTo/@City"/>, 
											<xsl:value-of select="/Order/PersonInfoShipTo/@State"/>&#160;<xsl:value-of select="/Order/PersonInfoShipTo/@ZipCode"/> 
										</font>
									</td>
								</tr>
							</table>
							<br/>
							<xsl:variable name="createdDate" select="/Order/@OrderDate"/>
							<font size="2" face="Arial"><b>Created on: <xsl:value-of select="substring($createdDate,6,2)"/>/<xsl:value-of select="substring($createdDate,9,2)"/>/<xsl:value-of select="substring($createdDate,0,5)"/></b></font>
						</td>
					</tr>
			
					<tr valign="center" style="text-align:center">
						<td bgcolor="#3366CC" colspan="9" height="0" style="text-align:left"><font color="#FFFFFF" size="2" face="Arial"><b>ITEM DESCRIPTION</b></font></td>
						<td width="10%" bgcolor="#3366CC"><font color="#FFFFFF" size="2" face="Arial"><b>ORDER QTY</b></font></td>
						<td width="10%" bgcolor="#3366CC"><font color="#FFFFFF" size="2" face="Arial"><b>RETURN QTY</b></font></td>
						<td></td>
					</tr>
					<tr>
						<td>
							<p>&#160;</p>
						</td>
					</tr>

					<xsl:for-each select = "/Order/OrderLines/OrderLine">
					<tr valign="top" style="text-align:center">
	
						<td colspan="9" height="1" style="text-align:left">
							<font size="2" face="Arial"><xsl:value-of select="ItemDetails/PrimaryInformation/@Description"/></font>
						</td>
						<td>
							
							<font size="2" face="Arial"><xsl:value-of select="format-number(@OriginalOrderedQty, '#,###')"/></font>
						</td>
						<td>
							
							<font size="2" face="Arial"><xsl:value-of select="format-number(@OrderedQty, '#,###')"/></font>
							
						</td>					
					</tr>
					
					<tr>
						<td>
							
						</td>
					</tr>
					</xsl:for-each>
					<tr valign="top" style="text-align:center">
						
						<td bgcolor="#3366CC" colspan="11" height="15"></td>
						
					</tr>
					
				</table>
			</td>
		</tr>
	</table>
</div>
<br/>

<table width="684">
	<tr>
		<td>
		    <font size="2" face="Arial">The Academy Sports + Outdoors Team.</font>
		</td>
	</tr>
</table>
<br/>
<table width="684">
	<tr>
		<td>
			<!-- <font size="2" face="Arial">Thank you for shopping at <a href="http://academy.com" target="_blank">academy.com</a>, 
			</font>
			<br/><br/> -->
		</td>
	</tr>
</table>
<br/>
<table width="684">
	<tr>
		<td>
			<font size="2" face="Arial" width="388">
				<b>Please note:</b> This email message was sent from a notification-only address that cannot accept incoming mail. <br />
				Please do not reply to this message.
			</font>
		</td>
	</tr>
</table>
</div>
</body>
</html>
</xsl:template>		
</xsl:stylesheet>