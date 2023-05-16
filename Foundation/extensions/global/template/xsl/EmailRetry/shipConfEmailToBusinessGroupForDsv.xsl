<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:lxslt="http://xml.apache.org/xslt" version="1.0">
<xsl:output method="html"/>
<xsl:variable name="currShipKey" select="/Order/@CurrentShipmentKey" />

<xsl:template match="/">
<xsl:text disable-output-escaping="yes"><![CDATA[<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">]]> </xsl:text>
<html>
<xsl:comment>CONTENT_TYPE=text/html; charset=UTF-8</xsl:comment>
<head>
<style type="text/css">
table.myborder { border:1px solid #FFF;}
</style>

<xsl:text disable-output-escaping="yes">
<![CDATA[<META http-equiv="Content-Type" content="text/html; charset=ascii"]]>
</xsl:text>
</head>
<body style="margin:0;padding:0">
<table width="684" border="0" cellspacing="0">
	<tr width="40%">
		<td>
			<img src="http://assets.academy.com/mgen/64/10099564.jpg" border="0" alt=""></img>
		</td>
	</tr>
</table>
<p> 
	<WBR>
		<font size="5" face="Arial">
			<b>&#160;*** Purchase Order Shipment Confirmation ***</b>
		</font>
	</WBR>
</p>
<table width="684" border="0" cellspacing="0">
	<tr>
		<td>
			<p>
				<font size="3" face="Arial">
					<b>
						This message is to notify you that the below order has been shipped by the vendor. Please create an RMS PO for the shipment.
					</b>
				</font>
				<br></br>
			</p>
		</td>
	</tr>
</table>
<br/>
<table width="80%" border="0" cellpadding="0" cellspacing="0">
	<tr valign="top">
		<td width="12%" bgcolor="#FFCC00">
			<font size="2" face="Arial">
				<b>&#160;Order #:</b>
			</font>
		</td>
		<td bgcolor="#FFCC00">
			<font size="2" face="Arial" >
				<b>
					<xsl:value-of select="/Order/@OrderName" />
				</b>
			</font>
		</td>
	</tr>
</table>
<table border="2" width="80%" cellspacing="0" cellpadding="0">
	<tr border="1" valign="middle" style="text-align:center">
		<td bgcolor="#3366CC" width="7%" style="text-align:centre">
			<font color="#FFFFFF" size="2" face="Arial">
				<b>&#160;LINE NO</b>
			</font>
		</td>
		<td bgcolor="#3366CC" width="22%" style="text-align:left">
			<font color="#FFFFFF" size="2" face="Arial">
				<b>&#160;SHIPMENT KEY</b>
			</font>
		</td>
		<td bgcolor="#3366CC" width="22%" style="text-align:left">
			<font color="#FFFFFF" size="2" face="Arial">
				<b>&#160;VENDOR NUMBER</b>
			</font>
		</td>
		<td bgcolor="#3366CC" width="22%" style="text-align:left">
			<font color="#FFFFFF" size="2" face="Arial">
				<b>&#160;VENDOR NAME</b>
			</font>
		</td>
		<td bgcolor="#3366CC" width="18%" style="text-align:left">
			<font color="#FFFFFF" size="2" face="Arial">
				<b>&#160;ITEM</b>
			</font>
		</td>
		<td bgcolor="#3366CC" width="23%" style="text-align:left">
			<font color="#FFFFFF" size="2" face="Arial">
				<b>&#160;ITEM DESCRIPTION</b>
			</font>
		</td>
		<td bgcolor="#3366CC" width="11%" style="text-align:left">
			<font color="#FFFFFF" size="2" face="Arial">
				<b>&#160; UNIT PRICE</b>
			</font>
		</td>
		<td bgcolor="#3366CC" width="9%" style="text-align:left">
			<font color="#FFFFFF" size="2" face="Arial">
				<b>&#160; LINE QTY</b>
			</font>
		</td>
		<td bgcolor="#3366CC" width="10%" style="text-align:left">
			<font color="#FFFFFF" size="2" face="Arial">
				<b>&#160; LINE TOTAL</b>
			</font>
		</td>
	</tr>
	
	<xsl:for-each select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/ShipmentLines/ShipmentLine">
		<xsl:variable name="orderLineKey" select="./@OrderLineKey" />
		<xsl:for-each select="/Order/OrderLines/OrderLine">		
			<xsl:if test="./@OrderLineKey=$orderLineKey">
				<tr>
					<td align="center">
						<xsl:value-of select="./@PrimeLineNo" />
					</td>
					<td>
						<xsl:value-of select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/@ShipmentKey" />
					</td>
					<td>
						<xsl:value-of select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/ShipmentLines/ShipmentLine/OrderRelease/@SupplierName"/>
					</td>
					<td>
					<xsl:value-of select="/Order/Shipments/Shipment/@SellerOrganizationCode" />
						
					</td>
					<td align="left">
						<xsl:value-of select="ItemDetails/@ItemID" />
					</td>
					<td>
						<xsl:value-of select="ItemDetails/PrimaryInformation/@Description" />
					</td>
					<td align="center">
						<xsl:value-of select="LinePriceInfo/@UnitPrice" />
					</td>
					<!--Start changes as part of STL-1467-->
					<td align="center">
						<xsl:value-of select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/ShipmentLines/ShipmentLine[@OrderLineKey=$orderLineKey]/@Quantity" />
					</td>
					<!--Stop changes as part of STL-1467-->
					<td align="center">
						<xsl:variable name="varLineTotal" select="LinePriceInfo/@LineTotal" />
						<xsl:value-of select="$varLineTotal" />
					</td>
				</tr>
			</xsl:if>
		</xsl:for-each>		
	</xsl:for-each>
</table>

<table width="80%" class="myborder" cellpadding="0" cellspacing="0">
	<tr valign="top">
		<td width="18%" bgcolor="#3366CC">
			<font color="#FFFFFF" size="2" face="Arial">
				<b>&#160;FREIGHT CHARGES:</b>
			</font>
		</td>
		<td bgcolor="#3366CC">
			<font color="#FFFFFF" size="2" face="Arial" >
				<b>
					<xsl:value-of select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/@ActualFreightCharge"/>
				</b>
			</font>
		</td>
	</tr>
</table>

<table width="80%" class="myborder" cellpadding="0" cellspacing="0">
	<tr valign="top" >
		<td width="94%" style="text-align:left" bgcolor="#3366CC" >
			<font color="#FFFFFF" size="2" face="Arial">
				<b>&#160;OVERALL LINE TOTAL:</b>
			</font>
		</td>
		<td bgcolor="#3366CC">
			<font color="#FFFFFF" size="2" face="Arial" style="text-align:center">
				<b>
					<xsl:variable name="varSumLineTtl"  select="sum(/Order/OrderLines/OrderLine[@OrderLineKey=/Order/Shipments
/Shipment[@ShipmentKey=$currShipKey]/ShipmentLines/ShipmentLine/@OrderLineKey]/LinePriceInfo/@LineTotal)"/>
					<xsl:value-of select="format-number($varSumLineTtl, '0.00')" />
				</b>
			</font>
		</td>
	</tr>
</table>
<br/>
<table width="80%" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td>
			<font size="2" face="Arial"><b>Please note: </b>This email message was sent from a notification-only address that cannot accept incoming email.
				Please do not reply to this message.
			</font>
		</td>
	</tr>
</table>		
</body>
</html>
</xsl:template>
</xsl:stylesheet>