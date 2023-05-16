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
  <p><a name="0.1_graphic03"></a>
  <font size="2" face="Arial"> 
  </font>
 <br>
 </br>
 </p>
 <p>

<img src="http://assets.academy.com/mgen/64/10099564.jpg" border="0" alt=""></img>
</p>
<p> 
<WBR>
<font size="3" face="Arial">
<b>**** Shipment Exceeding Weight  ****</b>
</font>
</WBR>
 </p>
<table width="684" border="0" cellspacing="0">
<tr valign="top"><td>
</td></tr></table>
<br/>
<table width="684">
<tr><td>
 <font size="2"  face="Arial" height="2">
The Shipment has exceeded the Academy maximum shipment limit. Shipment details are as below :
</font>
</td></tr>
</table>
<br/>
<!-- <div align="left"> -->
<!-- Start STL-1336 : adding order No along with Shipment No -->
<table border="2" width="80%" cellspacing="0" cellpadding="0">
	<tr border="1" valign="middle" style="text-align:center">
		<td bgcolor="#3366CC" width="7%" style="text-align:centre">
			<font color="#FFFFFF" size="2" face="Arial">
				<b>&#160;ORDER #</b>
			</font>
		</td>
		<td bgcolor="#3366CC" width="22%" style="text-align:left">
			<font color="#FFFFFF" size="2" face="Arial">
				<b>&#160;SHIPMENT #</b>
			</font>
		</td>
	</tr>
	
	
	<tr>
		<td align="left">
			<xsl:value-of select="/Shipment/ShipmentLines/ShipmentLine/@OrderNo" />
		</td>					
		<td align="left">
			<xsl:value-of select="/Shipment/@ShipmentNo" />
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
<!-- END STL-1336 : adding order No along with Shipment No -->
</div>
</body>
</html>
</xsl:template>		
</xsl:stylesheet>