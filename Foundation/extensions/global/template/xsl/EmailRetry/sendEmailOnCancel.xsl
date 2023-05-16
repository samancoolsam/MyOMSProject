<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lxslt="http://xml.apache.org/xslt"
				xmlns:Ext="com.academy.util.common.AcademyUtil"
                version="1.0">
<xsl:output method="html"/>


<xsl:template name="formatdate">
	<xsl:param name="datestr" />
	<!-- input format ddmmyyyy 2010-07-13T23 -->
	<!-- output format dd/mm/yyyy -->
		<xsl:variable name="dd">		
			<xsl:value-of select="substring($datestr,9,2)" />
		</xsl:variable>
		<xsl:variable name="mm">
			<xsl:value-of select="substring($datestr,6,2)" />
		</xsl:variable>
		<xsl:variable name="yyyy">
			<xsl:value-of select="substring($datestr,1,4)" />
		</xsl:variable>		
		<xsl:value-of select="$mm" />
		<xsl:value-of select="'/'" />
		<xsl:value-of select="$dd" />
		<xsl:value-of select="'/'" />
		<xsl:value-of select="$yyyy" />
</xsl:template>

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
			This email confirms that one or more items on your order have been cancelled.
		</font>
		
		<xsl:if test="Order/OrderHoldTypes/OrderHoldType/@HoldType='AcademyFraudOrder'">
		<font size="2" face="Arial">
			(Order Was Cancelled Due To Fraud Check Failure).
		</font>
		 </xsl:if>
		 
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
						<tr valign="middle">
							<td bgcolor="#3366CC" colspan="13"
								height="25">
								<font color="#FFFFFF" size="2"
									face="Arial">
									<b>&#160;PURCHASE SUMMARY</b>
								</font>
								<font size="2" face="Arial"></font>
							</td>
						</tr>
						<tr valign="top">
							<td colspan="5" height="11">
								<xsl:variable name="emailID" select="/Order/@CustomerEMailID" />
								<font size="2" face="Arial">&#160;Email Address: 
								<a href="mailto:{$emailID}">
										<xsl:value-of select="/Order/@CustomerEMailID" />
									</a>
								</font>
							</td>
						</tr>
	
						<tr valign="top">
							<td bgcolor="#EFEFEF" colspan="5"
								height="22">
								<font size="2" face="Arial">
									<b>&#160;Original Order #: 
										<xsl:value-of
											select="/Order/@OrderNo" />
									</b>
								</font>
							</td>
						</tr>
						<tr valign="bottom">
							<td colspan="5">
								<font size="2" face="Arial">&#160;Cancellation Date:
									<xsl:variable name="sysdate" select="Ext:getSysDate()"/>
									<xsl:value-of select="$sysdate"/>
								</font>
							</td>
						</tr>
						<tr>
							<td>
							<p></p> 
							</td>
						</tr>
						<tr valign="top">
							<td colspan="5">																			
								<table border="0" cellpadding="0" align="left"> 
									<tr>
										<td colspan="1" width="60" valign="top">
											<font size="2" face="Arial"><b>&#160;Ship To:</b></font>						
											<br/>
										</td>
										<td colspan="2" valign="top" width="275"><font size="2" face="Arial">
							                <xsl:value-of select="/Order/PersonInfoShipTo/@FirstName" />&#160;
											<xsl:value-of select="/Order/PersonInfoShipTo/@LastName" /><br/>						
											<xsl:value-of select="/Order/PersonInfoShipTo/@AddressLine1" /><br/>
											<xsl:value-of select="/Order/PersonInfoShipTo/@AddressLine2" /><br/>
											<xsl:value-of select="/Order/PersonInfoShipTo/@City" />,
											<xsl:value-of select="/Order/PersonInfoShipTo/@State" />&#160;
											<xsl:value-of select="/Order/PersonInfoShipTo/@ZipCode" />
											</font>
										</td>
									</tr>
								</table>

								<table border="0" cellpadding="0" align="left"> 
									<tr>
										<td colspan="1" width="60" valign="top">
											<font size="2" face="Arial"><b>Bill To:</b></font>
											<br/>
										</td>
										<td colspan="2" width="275" valign="top">
											<font size="2" face="Arial">
											<xsl:value-of select="/Order/PersonInfoBillTo/@FirstName" />&#160;
											<xsl:value-of select="/Order/PersonInfoBillTo/@LastName" /><br/>
											<xsl:value-of select="/Order/PersonInfoBillTo/@AddressLine1" /><br/>
											<xsl:value-of select="/Order/PersonInfoBillTo/@AddressLine2" /><br/>
											<xsl:value-of select="/Order/PersonInfoBillTo/@City" />,
											<xsl:value-of select="/Order/PersonInfoBillTo/@State" />&#160;
											<xsl:value-of select="/Order/PersonInfoBillTo/@ZipCode" />
											</font>
										</td>
									</tr>
								</table>

							</td>
						</tr>
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
							<td bgcolor="#3366CC" width="10%">
								<font color="#FFFFFF" size="2" face="Arial">
									<b>UNIT PRICE</b>
								</font>
							</td>
							<td bgcolor="#3366CC" width="12%">
								<font color="#FFFFFF" size="2" face="Arial">
									<b>TOTAL</b>
								</font>
							</td>
						</tr>
			
						<xsl:for-each select="/Order/OrderLines/OrderLine">
							<tr valign="middle" style="text-align:center">
								<td height="35" style="text-align:left">
									<font size="2" face="Arial">
										<xsl:value-of select="ItemDetails/PrimaryInformation/@Description" />
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
										<xsl:text>-</xsl:text>
									</xsl:otherwise>
									</xsl:choose>						
									</font>
								</td>					
								<td>
									<font size="2" face="Arial">
										$<xsl:value-of select="LineOverallTotals/@UnitPrice" />							
									</font>
								</td>
								<td>
									<xsl:variable name="qty" select="@OriginalOrderedQty" />
									<xsl:variable name="price" select="LineOverallTotals/@UnitPrice" />							
									<xsl:choose>
										<xsl:when test="@OrderedQty='0.00'">
											<font color="#FF0000" size="2" face="Arial">
												$<xsl:value-of select="format-number(($qty * $price),'##,###.00')"/>
											</font>
										</xsl:when>
										<xsl:otherwise>
											<font size="2" face="Arial">
												$<xsl:value-of select="format-number(($qty * $price),'##,###.00')"/>
											</font>
										</xsl:otherwise>
									</xsl:choose>
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
						Thanks for shopping at Academy Sports + Outdoors.
					</font>
				</td>
			</tr>
		</table>
		<br/>
			<table width="684">
				<tr>
					<td>
					<font size="2" face="Arial">
						If you need further assistance, please contact Customer Service at <a href="http://www.academy.com/contactus" target="_blank"><u>www.academy.com/contactus</u></a>.
						You may also call us at 1-888-922-2336 and an Academy Associate will gladly assist you. 
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