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

<img src="http://assets.academy.com/mgen/64/10099564.jpg" width="350" height="350" border="0" alt=""></img>

</p>


<p> 
<WBR>
<font size="2" face="Arial">
<b>**** ORDER CONFIRMATION ****</b>
</font>
 <br>
</br>
</WBR>
 </p>
<p>
<font size="2" face="Arial">
<b>Dear <xsl:value-of select="/Order/PersonInfoBillTo/@FirstName"/>&#160;<xsl:value-of select="/Order/PersonInfoBillTo/@LastName"/> , </b>
</font>
 <br>
 </br>
 </p>
<p>
<font size="2" face="Arial" width="388">Thank you for shopping at <a href="http://academy.com" target="_blank">academy.com</a>! 
This email is sent to notify you that your order has been received. 
You will receive a second email from us when your order has shipped. 
For your records, we have included a detailed description of your order 
below.
</font>
 <br>
 </br>
 </p>
<a name="0.1_table01"></a>
<div align="left">
<table width="697" border="2" cellspacing="0">
<tr valign="top"><td bgcolor="#3366CC" colspan="13" height="11"><font color="#FFFFFF" size="2" face="Arial"><b>Purchase Summary:</b></font><font size="2" face="Arial"> </font></td></tr>
<tr valign="top"><td colspan="13" height="11">

<xsl:variable name="TNo">mailto:<xsl:value-of select="/Order/@CustomerEMailID" /></xsl:variable>

<font size="2" face="Arial">E-mail Address:  

<a href="{$TNo}"><xsl:value-of select="/Order/@CustomerEMailID" /></a></font></td></tr>

<tr valign="top"><td bgcolor="#EFEFEF" colspan="13" height="11"><font size="2" face="Arial"><b>Order #: <xsl:value-of select="/Order/@OrderNo"/>  
    </b></font></td></tr>
<tr valign="top"><td colspan="13" height="11">
  <font size="2" face="Arial">Order Total:  $<xsl:value-of select="/Order/OverallTotals/@GrandTotal"/></font></td></tr>
<tr valign="top"><td colspan="13" height="23"><font size="2" face="Arial">Free 
  shipping on your next order for qualified items.</font>
   <br>
   </br> 
</td>
</tr>
<tr>
<td colspan="7">
	<table width="70%" border="0" cellspacing="0" cellpadding="0">
	<tr>
	<td valign="top"><font size="2" face="Arial"><b>Order Ship To:</b></font></td>
	<td valign="top">
	<font size="2" face="Arial"><xsl:value-of select="/Order/PersonInfoShipTo/@FirstName"/>&#160;<xsl:value-of select="/Order/PersonInfoShipTo/@LastName"/></font>
	</td>	
	<td valign="top"><font size="2" face="Arial"><b>Bill to:</b></font></td>	
	<td valign="top">
	<font size="2" face="Arial">
	<xsl:value-of select="/Order/PersonInfoBillTo/@FirstName"/>
	<xsl:value-of select="/Order/PersonInfoBillTo/@LastName"/>
	</font>
	<font size="2" face="Arial"><xsl:value-of select="/Order/PersonInfoBillTo/@AddressLine1"/>
	<xsl:value-of select="/Order/PersonInfoBillTo/@AddressLine2"/></font>
	<font size="2" face="Arial"><xsl:value-of select="/Order/PersonInfoBillTo/@AddressLine3"/><br/><xsl:value-of select="/Order/PersonInfoBillTo/@City"/>	,<br/><xsl:value-of select="/Order/PersonInfoBillTo/@State"/>,<xsl:value-of select="/Order/PersonInfoBillTo/@ZipCode"/> 
	</font>
  	</td>
  	</tr>
	</table>
</td>
</tr>
<tr valign="top"><td bgcolor="#3366CC" colspan="4" height="1">
<font color="#FFFFFF" size="2" face="Arial"><b>Item</b></font></td>
  <td bgcolor="#3366CC"><font color="#FFFFFF" size="2" face="Arial"><b>Order 
  Qty</b></font></td>
  <td bgcolor="#3366CC"><font color="#FFFFFF" size="2" face="Arial"><b>Price</b></font></td>
  <td bgcolor="#3366CC"><font color="#FFFFFF" size="2" face="Arial"><b>Subtotal</b></font></td></tr>
<xsl:for-each select = "/Order/OrderLines/OrderLine">
<tr valign="top">
<td colspan="4" height="1">
 <br><font size="2" face="Arial"><xsl:value-of select="ItemDetails/PrimaryInformation/@Description"/></font>
  <p><font color="#FF0000" size="2" face="Arial">Shipping method – <xsl:value-of select="@CarrierServiceCode"/></font>
  </p>
  <p><font color="#0000FF" size="2" face="Arial">
  <xsl:if test = "Instructions/Instruction[@InstructionType='GIFT']">  
  (Gift Message – <xsl:value-of select="Instructions/Instruction[@InstructionType='GIFT']/@InstructionText"/>)
  </xsl:if>
  </font>
  </p>
   </br>
   </td>
	  <td>
	   <br>
	   </br>
	   <font size="2" face="Arial"><xsl:value-of select="@OrderedQty"/></font>
	  </td>
  	  <td> 
	  <br>
	  </br>
	  <font size="2" face="Arial">$<xsl:value-of select="LineOverallTotals/@UnitPrice"/></font>
	  </td>
	  <td>
	  <br>
	  </br><font size="2" face="Arial">$<xsl:value-of select="LineOverallTotals/@ExtendedPrice"/></font>
	  </td>
  </tr>
  
  </xsl:for-each>

	<tr valign="top">
		<td bgcolor="#3366CC" rowspan="2"><font color="#FFFFFF" size="2" face="Arial"><b>Summary</b></font></td>
	  	<td bgcolor="#3366CC"><font color="#FFFFFF" size="2" face="Arial"><b>Subtotal</b></font></td>
	  	<td bgcolor="#3366CC"><font color="#FFFFFF" size="2" face="Arial"><b>Discount</b></font></td>
		<td bgcolor="#3366CC"><font color="#FFFFFF" size="2" face="Arial"><b>Shipping</b></font></td>
		<td bgcolor="#3366CC"><font color="#FFFFFF" size="2" face="Arial"><b>Tax</b></font></td>
		<td bgcolor="#3366CC"><font color="#FFFFFF" size="2" face="Arial"><b>Total 
		Amount</b></font></td>
		<td bgcolor="#3366CC"><font color="#FFFFFF" size="2" face="Arial"><b>Payment 
		Information</b></font></td>
	</tr>
	<tr valign="top">
		<td><font size="2" face="Arial"><b>$<xsl:value-of select="/Order/OverallTotals/@LineSubTotal"/></b></font></td>
		<td><font size="2" face="Arial"><b>$<xsl:value-of select="/Order/OverallTotals/@GrandDiscount"/></b></font></td>
		<td><font size="2" face="Arial"><b>$<xsl:value-of select="/Order/OverallTotals/@GrandCharges"/></b></font></td>
		<td><font size="2" face="Arial"><b>$<xsl:value-of select="/Order/OverallTotals/@GrandTax"/></b></font></td>
		<td><font size="2" face="Arial"><b>$<xsl:value-of select="/Order/OverallTotals/@GrandTotal"/></b></font></td>
		
		<td><font size="2" face="Arial"><b>
		<xsl:for-each select = "/Order/PaymentMethods/PaymentMethod">  
		<!-- KER-12036 : Payment Migration Changes to support new Payment Type -->
		<xsl:if test = "@PaymentType='CREDIT_CARD' or @PaymentType='Credit_Card'">Visa (<xsl:value-of select="@DisplayCreditCardNo"/>) 
		$<xsl:value-of select="@MaxChargeLimit"/>
		</xsl:if>
		</xsl:for-each>
		</b>
		</font>
		</td>								
	</tr>
	<tr>		
		<xsl:for-each select = "/Order/PaymentMethods/PaymentMethod">  
		<td colspan="6"/>
		<td><font size="2" face="Arial">
		<b>		
		<xsl:if test = "@PaymentType='GIFT_CARD'">GC (<xsl:value-of select="@SvcNo"/>) 
		$<xsl:value-of select="@MaxChargeLimit"/>
		</xsl:if>
		</b>
		</font>
		</td>
		</xsl:for-each>
	</tr>
		
	<!--  Added for PayPal Implementation-->
	<tr>		
		<xsl:for-each select = "/Order/PaymentMethods/PaymentMethod">  
		<td colspan="6"/>
		<td><font size="2" face="Arial">
		<b>		
		<!-- KER-12036 : Payment Migration Changes to support new Payment Type -->
		<xsl:if test = "@PaymentType='PayPal' or @PaymentType='Paypal'">PayPal (<xsl:value-of select="@DisplayCreditCardNo"/>) 
		$<xsl:value-of select="@MaxChargeLimit"/>
		</xsl:if>
		</b>
		</font>
		</td>
		</xsl:for-each>
	</tr>
		
<!--<tr valign="top"><td colspan="2" height="14">
  <font size="2" face="Arial"><b>GC (5641) 
  $50.00</b></font></td></tr>

<tr valign="top"><td colspan="2" height="12">
  <font size="2" face="Arial"><b>GC (5642) 
  $50.00</b></font></td></tr>-->
</table>
</div>
<br/><br/>

<table width="697">
<tr>
<td>
<font size="2" face="Arial" width="388">Should you have any questions, please 
visit the &quot;Customer Service FAQ&quot; section at </font><a href="http://www.academy.com/faq" target="_blank"><font color="#0000FF" size="2" face="Arial"><u>www.academy.com/faq</u></font></a><font size="2" face="Arial">, or simply send an email to </font><a href="mailto:customerservice@academy.com" target="_blank"><font color="#0000FF" size="2" face="Arial"><u>customerservice@academy.com</u></font></a><font size="2" face="Arial"> (please allow 24 hours for response). You may 
also call us at 1 888 922 2336 and an Academy Associate will gladly 
assist you.</font>
<br/><br/>
<font size="2" face="Arial">Thank you for shopping at <a href="http://academy.com" target="_blank">academy.com</a>.
</font>
<br/>
<font size="2" face="Arial">The Academy Sports + Outdoors Team</font>
<br/><br/>
<font size="2" face="Arial" width="388">
<b>Please note:</b> This e-mail message 
was sent from a notification-only address that cannot accept incoming 
e-mail. Please do not reply to this message.
</font>
</td>
</tr>
</table>

</div>
</body>
</html>
</xsl:template>		
</xsl:stylesheet>