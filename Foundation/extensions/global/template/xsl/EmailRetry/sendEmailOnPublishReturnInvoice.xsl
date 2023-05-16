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
<title></title><style type="text/css" media="print">.hide{display:none}</style>
</head>
<body style="margin:0;padding:0">
<div style="width:768px">
<p><a name="0.1_graphic03"></a><a name="0.1_OLE_LINK3"></a><a name="0.1_OLE_LINK4"></a><font size="2" face="Arial"><!--<img src="http://mail.google.com/mail/?name=d33be9805ff33117.jpg&amp;attid=0.1&amp;disp=vahi&amp;view=att&amp;th=1239e407eda57298" height="1" width="1" alt="Your browser may not support display of this image.">-->
</font></p>
<p>
<img src="http://assets.academy.com/mgen/64/10099564.jpg"  border="0" alt=""></img>
</p>
<p><WBR> 
<font size="3" face="Arial"><b>*** Receipt of Return ***</b></font>
</WBR>
<br>
</br>
 </p>
<p><font size="2" face="Arial"><b>Dear <xsl:value-of select="/AcademyMergedDocument/EnvironmentDocument/InvoiceDetail/InvoiceHeader/Order/PersonInfoBillTo/@FirstName"/>&#160;<xsl:value-of select="/AcademyMergedDocument/EnvironmentDocument/InvoiceDetail/InvoiceHeader/Order/PersonInfoBillTo/@LastName"/> , </b></font> <br></br></p>
<table width="684" border="0" cellspacing="0" cellpadding="0">
<tr>
<td>
<p><font size="2" face="Arial">This is notification that we have received the item(s) you returned in our returns center.  The item(s) received will be credited to the method of payment used in placing your order.  If you paid with a gift card, a replacement card will be shipped to you at no charge, in the amount for items returned, less any applicable shipping costs.
</font> </p>
</td>
</tr>
</table>
<br/>
<a name="0.1_table01"></a>
<div align="left">
<table width="684" border="2" cellspacing="0" cellpadding="0">
<tr valign="top"><td bgcolor="#3366CC" colspan="9" height="11"><font color="#FFFFFF" size="2" face="Arial"><b>RETURN SUMMARY</b></font></td></tr>
<!-- <tr valign="top"><td colspan="7" height="11">

<xsl:variable name="TNo">mailto:<xsl:value-of select="/AcademyMergedDocument/EnvironmentDocument/Order/@CustomerEMailID" /></xsl:variable>
<font size="2" face="Arial">E-mail Address: <a href="{$TNo}"><xsl:value-of select="/AcademyMergedDocument/EnvironmentDocument/Order/@CustomerEMailID" /></a> </font></td></tr> -->

<!-- Fixed Bug 3384. Displaying the sales order instead of Return Order-->
<tr valign="top"><td bgcolor="#EFEFEF" colspan="9" height="11"><font size="2" face="Arial"><b>Original Sales Order #: <xsl:value-of select="/AcademyMergedDocument/EnvironmentDocument/InvoiceDetail/InvoiceHeader/Order/OrderLines/OrderLine/DerivedFromOrder/@OrderNo"/></b></font>
  </td></tr>
  <!-- Keeping it ready if asked for a change. All you have to do is just uncomment the below row! -->
   <tr valign="top"><td bgcolor="#EFEFEF" colspan="9" height="11">
   <xsl:variable name="receivedDate" select="/AcademyMergedDocument/EnvironmentDocument/InvoiceDetail/InvoiceHeader/Order/@OrderDate"/>
   <font size="2" face="Arial"><b>Received : <xsl:value-of select="substring($receivedDate,9,2)"/>/<xsl:value-of select="substring($receivedDate,6,2)"/>/<xsl:value-of select="substring($receivedDate,0,5)"/></b></font>
  </td></tr>
    
<!--tr valign="top">
<td colspan="7" height="23">
<font size="2" face="Arial">Free shipping on your next order for qualified items.</font>
</td>
</tr-->
<tr>
<td colspan="7">
<!-- <table width="90%" border="0" cellpadding="0" cellspacing="0">
<tr valign="top">
<td valign="top" align="left"><font size="2" face="Arial"><b>Order Ship To:</b></font></td>
<td valign="top" align="left">
<font size="2" face="Arial"><xsl:value-of select="/AcademyMergedDocument/EnvironmentDocument/Order/PersonInfoShipTo/@FirstName"/>&#160;<xsl:value-of select="/AcademyMergedDocument/EnvironmentDocument/Order/PersonInfoShipTo/@LastName"/>
</font>
</td>
<td valign="top" align="left">
<font size="2" face="Arial"><b>Bill to: </b></font></td>
<td valign="top" align="left"><font size="2" face="Arial"><xsl:value-of select="/AcademyMergedDocument/EnvironmentDocument/Order/PersonInfoBillTo/@FirstName"/><xsl:value-of select="/AcademyMergedDocument/EnvironmentDocument/Order/PersonInfoBillTo/@LastName"/></font>
<font size="2" face="Arial"><xsl:value-of select="/AcademyMergedDocument/EnvironmentDocument/Order/PersonInfoBillTo/@AddressLine1"/><xsl:value-of select="/AcademyMergedDocument/EnvironmentDocument/Order/PersonInfoBillTo/@AddressLine2"/></font><br/>
<font size="2" face="Arial"><xsl:value-of select="/AcademyMergedDocument/EnvironmentDocument/Order/PersonInfoBillTo/@AddressLine3"/><xsl:value-of select="/AcademyMergedDocument/EnvironmentDocument/Order/PersonInfoBillTo/@City"/>, <br/><xsl:value-of select="/AcademyMergedDocument/EnvironmentDocument/Order/PersonInfoBillTo/@State"/> , <xsl:value-of select="/AcademyMergedDocument/EnvironmentDocument/Order/PersonInfoBillTo/@ZipCode"/> </font>
</td></tr>

</table> -->
</td>
</tr>  
  
<!-- <tr valign="top"><td colspan="7" height="8"><font size="2" face="Arial"><b>Received on :<xsl:value-of select="/AcademyMergedDocument/EnvironmentDocument/Order/@OrderDate"/></b></font></td></tr> -->
<tr valign="top">
  <td width="60%" bgcolor="#3366CC" height="1"><font color="#FFFFFF" size="2" face="Arial"><b>ITEM DESCRIPTION</b></font></td>
  <td width="10%" align="center" bgcolor="#3366CC"><font color="#FFFFFF" size="2" face="Arial"><b>ORDER QTY</b></font></td>
  <td width="10%" align="center" bgcolor="#3366CC"><font color="#FFFFFF" size="2" face="Arial"><b>RETURN QTY</b></font></td>
  <td width="10%" align="center" bgcolor="#3366CC"><font color="#FFFFFF" size="2" face="Arial"><b>UNIT PRICE</b></font></td>
  <td width="10%" align="center" bgcolor="#3366CC"><font color="#FFFFFF" size="2" face="Arial"><b>TOTAL</b></font></td>
</tr>

<xsl:for-each  select = "/AcademyMergedDocument/EnvironmentDocument/InvoiceDetail/InvoiceHeader/Order/OrderLines/OrderLine">
<tr style="text-align:center" valign="top">
	<td width="60%" height="1" align="left"><br/><font size="2" face="Arial"><xsl:value-of select="Item/@ItemDesc"/></font></td>
	<td width="10%" height="1"><br/><xsl:variable name = "derOrdLineKey" select = "@DerivedFromOrderLineKey" />
  	  	 <xsl:variable name = "orderUnit" select = "/AcademyMergedDocument/InputDocument/Order/OrderLines/OrderLine[@OrderLineKey=$derOrdLineKey]/@OrderedQty" /> 
  	<font size="2" face="Arial"><xsl:value-of select="format-number($orderUnit,'#,###')" /></font></td>
   <td width="10%" height="1"><br/><font size="2" face="Arial"><xsl:value-of select="format-number(@OrderedQty,'#,###')"/></font></td>
  <td width="10%" height="1"><br/><font color="#FF0000" size="2" face="Arial">($<xsl:value-of select="LineOverallTotals/@UnitPrice"/>)</font></td>
  <td width="10%" height="1"><br/><font color="#FF0000" size="2" face="Arial">($<xsl:value-of select="LineOverallTotals/@ExtendedPrice"/>)</font></td> 
  </tr>
  </xsl:for-each>
<!--tr>
<td colspan="3"><font size="2" face="Arial">RETURN SHIPPING CHARGES</font></td>
<td></td>
<td></td>
<td><font size="2" face="Arial">($12.99)</font></td>
<td><br></br><font size="2" face="Arial">($12.99)</font></td>
</tr-->
</table>
<table width="684" border="2" cellspacing="0" cellpadding="0">
<xsl:for-each  select = "AcademyMergedDocument/InputDocument/Order/ReturnOrders/ReturnOrder">
<xsl:variable name = "returnOrderNumber" select = "/AcademyMergedDocument/EnvironmentDocument/InvoiceDetail/InvoiceHeader/Order/@OrderNo" /> 
<xsl:if test = "@OrderNo=$returnOrderNumber">
<tr style="text-align:center" valign="top">
	<!-- <td bgcolor="#3366CC" rowspan="2"><font color="#FFFFFF" size="2" face="Arial"><b>SUMMARY</b></font></td> -->
  	<td  bgcolor="#3366CC"><font color="#FFFFFF" size="2" face="Arial"><b>SUBTOTAL</b></font></td>
  	<td  bgcolor="#3366CC"><font color="#FFFFFF" size="2" face="Arial"><b>DISCOUNT</b></font></td>
  	<td  bgcolor="#3366CC"><font color="#FFFFFF" size="2" face="Arial"><b>SHIPPING</b></font></td>
  	<td  bgcolor="#3366CC"><font color="#FFFFFF" size="2" face="Arial"><b>TAX</b></font></td>
  	<td  bgcolor="#3366CC"><font color="#FFFFFF" size="2" face="Arial"><b>TOTAL</b></font></td>
   <td colspan="4" bgcolor="#3366CC"><font color="#FFFFFF" size="2" face="Arial"><b>PAYMENT INFORMATION</b></font></td>
</tr>
<tr valign="top">
<td  align="center"><font color="#FF0000" size="2" face="Arial"><b>($<xsl:value-of select="OverallTotals/@LineSubTotal"/>)</b></font></td>
<td  align="center"><font size="2" face="Arial"><b>$<xsl:value-of select="OverallTotals/@GrandDiscount"/>
</b></font></td>
<td  align="center"><font color="#FF0000" size="2" face="Arial"><b>($<xsl:value-of select="OverallTotals/@GrandCharges"/>)</b></font></td>
<td  align="center"><font color="#FF0000" size="2" face="Arial"><b>($<xsl:value-of select="OverallTotals/@GrandTax"/>)</b></font></td>
<td  align="center"><font color="#FF0000" size="2" face="Arial"><b>($<xsl:value-of select="OverallTotals/@GrandTotal"/>)</b></font></td>  
<td colspan="5" align="center">
<!-- <xsl:for-each select = "/AcademyMergedDocument/EnvironmentDocument/Order/PaymentMethods/PaymentMethod">
<font size="2" face="Arial"><b>
	<xsl:if test = "@ChargeType='CHARGE'">Credit Card (<xsl:value-of select="@DisplayCreditCardNo"/>) 
		$<xsl:value-of select="@TotalRefundedAmount"/>
	</xsl:if>
</b></font>
</xsl:for-each>-->
<table border="2">
		
<xsl:for-each select = "/AcademyMergedDocument/EnvironmentDocument/InvoiceDetail/InvoiceHeader/CollectionDetails/CollectionDetail">  
		<tr><td>
		<font size="2" face="Arial">
		<b>	
		
<xsl:variable name="giftCardNum" select="/AcademyMergedDocument/InputDocument/Order/PaymentMethods/PaymentMethod[@PaymentType='GIFT_CARD']/@SvcNo" />

	<!-- KER-12036 : Payment Migration Changes to support new Payment Type -->
	<!-- <xsl:variable name="creditCardNum" select="/AcademyMergedDocument/InputDocument/Order/PaymentMethods/PaymentMethod[@PaymentType='CREDIT_CARD']/@DisplayCreditCardNo" /> -->
		<xsl:variable name="creditCardNum">
		<xsl:choose>
        		<xsl:when test="/AcademyMergedDocument/InputDocument/Order/PaymentMethods/PaymentMethod[@PaymentType='CREDIT_CARD']/@DisplayCreditCardNo != ''">
          			<xsl:value-of select="/AcademyMergedDocument/InputDocument/Order/PaymentMethods/PaymentMethod[@PaymentType='CREDIT_CARD']/@DisplayCreditCardNo"/>
        		</xsl:when>
       	 		<xsl:otherwise>
				<xsl:value-of select="/AcademyMergedDocument/InputDocument/Order/PaymentMethods/PaymentMethod[@PaymentType='Credit_Card']/@DisplayCreditCardNo"/>
        		</xsl:otherwise>
      		</xsl:choose>		
		</xsl:variable>
	
		<xsl:if test = "@ChargeType='CHARGE'">Credit Card (<xsl:value-of select="$creditCardNum"/>) 
			  $<xsl:value-of select="substring(@AmountCollected,2)"/>
					</xsl:if>
		<xsl:if test = "@ChargeType='TRANSFER_OUT'">Gift Card (<xsl:value-of select="$giftCardNum"/>)
			$<xsl:value-of select="substring(@AmountCollected,2)"/>
		</xsl:if>
		</b>
		</font>
		</td></tr>
		</xsl:for-each>
		</table>
</td> 


</tr>
 </xsl:if>
<tr>
	<!-- <td colspan="5"/>
	<xsl:for-each select = "/AcademyMergedDocument/EnvironmentDocument/InvoiceDetail/InvoiceHeader/CollectionDetails">  
		
		<font size="2" face="Arial">
		<b>		
		<xsl:if test = "@ChargeType='TRANSFER_OUT'">Gift Card (<xsl:value-of select="@SvcNo"/>) 
			$<xsl:value-of select="@TotalRefundedAmount"/>
		</xsl:if>
		<xsl:if test = "@ChargeType='TRANSFER_OUT'">Gift Card (<xsl:value-of select="@SvcNo"/>) 
			$<xsl:value-of select="@TotalRefundedAmount"/>
		</xsl:if>
		</b>
		</font>
		</xsl:for-each>
	</td> -->
</tr>
</xsl:for-each>
</table>

<br/>

<table width="684" >
	<tr>
		<td>
		    <font size="2" face="Arial">The Academy Sports + Outdoors Team </font> 
		</td>
	</tr>
</table>

<br/>

<table width="684" >
	<tr>
		<td>
		    <font size="2" face="Arial" width="388">If you need further assistance, please contact Customer Service at
</font><font color="#0000FF" size="2" face="Arial"><a href="http://www.academy.com/contactus" target="_blank"><u>www.academy.com/contactus</u></a></font>.<font size="2" face="Arial"> You may also call us at 1-888-922-2336 and an Academy Associate will gladly assist you.</font>
		</td>
	</tr>
</table>

<br/>

<table width="684" >
	<tr>
		<td>
			<font size="2" face="Arial" width="388">
				<b>Please note:</b> This email message was sent from a notification-only address that cannot accept incoming mail. <br/>Please do not reply to this message.
			</font>
		</td>
	</tr>
</table>
</div>
</div></body></html>
</xsl:template>		
</xsl:stylesheet>