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
<font size="3" face="Arial">
<b>*** Gift Card Activation  ***</b>
</font>
</WBR>
 </p>
<table width="684" border="0" cellspacing="0">
<tr valign="top"><td>
<font size="2"  face="Arial">
<b>Dear <xsl:value-of select="/Order/@FirstName"/>&#160;<xsl:value-of select="/Order/@LastName"/>, </b>
</font>
</td></tr></table>
<br/>
<table width="684">
<tr><td>
 <font size="2"  face="Arial" height="2">
Thank you for your gift card order. Please note that upon receipt your bulk
gift cards must be activated by calling Customer Service. This security feature
is for your protection on all large quantity purchases.
</font>
</td></tr>
</table>
<br/>
<!-- <div align="left"> -->
<table width="684" border="1" cellspacing="0">
<tr valign="top"><td bgcolor="#3366CC">
<font color="#FFFFFF" size="2"  face="Arial"><b>ACTIVATION INFORMATION</b></font><font size="2" face="Arial"> </font></td></tr>

<tr valign="top"><td color="#FFFFFF" >
<br/>

<font size="2"  face="Arial" color="#3366CC" width="38"> <b> &#160;&#160;&#160;
  (ACTIVATION CODE <xsl:value-of select="/Order/@GCActivationCode"/> ) &#160;&#160;&#160; Please call Customer Service to activate. (1-888-922-2336)
   </b> 
  </font>

</td>
  
</tr>
   
<tr valign="top" ><td><font size="2" face="Arial"><br/><b> &#160;&#160;&#160; Shipment #:  <xsl:value-of select="/Order/@ShipmentNo"/>&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160; Order #: 
<xsl:value-of select="/Order/@OrderNo"/>
</b></font></td>
  </tr>

</table>
<br/>
<table width="684" border="0">
<tr>
<td>
<font size="2"  face="Arial">Thank you for shopping at Academy Sports + Outdoors.
</font>
</td>
</tr>
</table>
<br/>
<table width="684" border="0">
<tr>
<td>
<font size="2"  face="Arial" width="388">If you need further assistance, please contact Customer Service at
</font><a href="http://www.academy.com/contactus" target="_blank"><font color="#0000FF" size="2" face="Arial"><u>www.academy.com/contactus</u></font></a><font size="2"  face="Arial"> (please allow up to 24 hours for a response). You may also call us at 1-888-922-2336 and an Academy Associate will gladly assist you.</font>
</td>
</tr>
</table>
<br/>

<table width="684" border="0">
<tr>
<td>
<font size="2"  face="Arial" width="388">
<b>Please note:</b> This e-mail message was sent from a notification-only address that cannot accept incoming 
email. <br />Please do not reply to this message.
</font>
</td>
</tr>
</table>
<br/>


</div>
</body>
</html>
</xsl:template>		
</xsl:stylesheet>