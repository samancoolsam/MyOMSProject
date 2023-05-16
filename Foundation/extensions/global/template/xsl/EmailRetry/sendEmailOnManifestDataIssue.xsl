<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:lxslt="http://xml.apache.org/xslt"
version="1.0">

<xsl:output method="html"/>
<xsl:template match="/">
<xsl:text disable-output-escaping="yes"><![CDATA[<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">]]> </xsl:text>
<html>
<xsl:comment>CONTENT_TYPE=text/html; charset=UTF-8</xsl:comment>
<head>
<xsl:text disable-output-escaping="yes">
<![CDATA[<META http-equiv="Content-Type" content="text/html; charset=ascii"]]>
</xsl:text>
</head>
<body style="margin:0;padding:0">
<BR></BR>
<p> 
	<font size="3" face="Arial">
		<b>*** Manifest Data Issue ***</b><BR></BR>
	</font>
</p>

<table width="684" border="0" cellspacing="0">
	<tr>
		<td>
			<p>
			<font size="2" face="Arial">
					This message is to notify you that below Manifest have data issue and can not be closed unless data is corrected. Details are as below :
			</font>
			</p>
		</td>
	</tr>
</table>
<BR></BR>
<P></P>
<table width="684" border="1" cellspacing="0">
	<tr valign="center">
		<td bgcolor="#3366CC" colspan="11" height="25">
			<font color="#FFFFFF" size="2" face="Arial">&#160;<b>Manifest Details</b></font>
		</td>
	</tr>
	<tr valign="top">
		<td colspan="11">
			<table width="100%">
				<tr valign="middle">
					<td bgcolor="#EFEFEF" height="20">
						<font size="2" face="Arial">
							<b>Manifest #: <xsl:value-of select="/Manifest/@ManifestNo" /></b>
						</font>
					</td>
					<td bgcolor="#EFEFEF" height="20">
						<font size="2" face="Arial">
							<b>Ship node #: <xsl:value-of select="/Manifest/@ShipNode" /></b>
						</font>
					</td>
					<td bgcolor="#EFEFEF" height="20">
						<font size="2" face="Arial">
							<b>Manifest Date: <xsl:value-of select="/Manifest/@ManifestDate" /></b>
						</font>
					</td>	
				</tr>
			</table>
		</td>
	</tr>
	<tr valign="center">
		<td bgcolor="#EFEFEF" colspan="11" height="25">
			<font size="2" face="Arial">&#160;<b>Manifest Issue :  <xsl:value-of select="/Manifest/@ManifestIssue" /></b></font>
		</td>
	</tr>
</table>
<BR></BR>
<P></P>
<xsl:variable name="shipmentIssue" select="/Manifest/@ShipmentIssue" /> 
<xsl:if test="$shipmentIssue='true'">
	<table width="684" border="0" cellspacing="0" cellpadding="0">
		<tr border="1" valign="center" style="text-align:center">
			<td bgcolor="#3366CC" width="20%" style="text-align:left">
				<font color="#FFFFFF" size="2" face="Arial"><b>Shipment No</b></font>
			</td>
			<td bgcolor="#3366CC" width="50%" style="text-align:left">
				<font color="#FFFFFF" size="2" face="Arial"><b>Shipment Description</b></font>
			</td>
			<td bgcolor="#3366CC" width="15%" style="text-align:left">
				<font color="#FFFFFF" size="2" face="Arial"><b>Shipment Quantity</b></font>
			</td>
			<td bgcolor="#3366CC" width="15%" style="text-align:left">
				<font color="#FFFFFF" size="2" face="Arial"><b>Packed Quantity</b></font>
			</td>
		</tr>
		<xsl:for-each select="/Manifest/Shipments/Shipment">
						<tr border="1" valign="center" style="text-align:center">
							<td bgcolor="#EFEFEF" width="20%" style="text-align:left">
								<font size="2" face="Arial">
									<b><xsl:value-of select="@ShipmentNo" /></b>
								</font>
							</td>
							<td bgcolor="#EFEFEF" width="50%" style="text-align:left">
								<font size="2" face="Arial">
									<b><xsl:value-of select="@StatusMessage" /></b>
								</font>
							</td>
							<td bgcolor="#EFEFEF" width="15%" style="text-align:left">
								<font size="2" face="Arial">
									<b><xsl:value-of select="@ShipmentLineQuantity" /></b>
								</font>
							</td>
							<td bgcolor="#EFEFEF" width="15%" style="text-align:left">
								<font size="2" face="Arial">
									<b><xsl:value-of select="@ShipmentPackedQuantity" /></b>
								</font>
							</td>						
						</tr>
		</xsl:for-each>
	</table>
</xsl:if>
<BR></BR>
<P></P>

<BR></BR>
<P></P>
<table width="684" border="0" cellspacing="0">
	<tr>
		<td>
			<p>
			<font size="2" face="Arial">
					Please correct the Manifest Data or get Manifest Data corrected by support ticket and close the Manifest from the SOM console.
			</font>
			</p>
		</td>
	</tr>
</table>
<BR></BR>
</body>
</html>
</xsl:template>
</xsl:stylesheet>