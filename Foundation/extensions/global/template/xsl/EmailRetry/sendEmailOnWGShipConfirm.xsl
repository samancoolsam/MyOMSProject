<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:lxslt="http://xml.apache.org/xslt"
version="1.0">


<!-- converts FROM 2001-12-31T12:00:00 TO new format June 26, 2009 -->
<xsl:template name="FormatDate">
	<xsl:param name="DateTime" />

	<xsl:variable name="year">
		<xsl:value-of select="substring($DateTime,1,4)" />
	</xsl:variable>
	
	<xsl:variable name="month-temp">
		<xsl:value-of select="substring-after($DateTime,'-')" />
	</xsl:variable>
	
	<xsl:variable name="month">
		<xsl:value-of select="substring-before($month-temp,'-')" />
	</xsl:variable>
	
	<xsl:variable name="day-temp">
		<xsl:value-of select="substring-after($month-temp,'-')" />
	</xsl:variable>
	
	<xsl:variable name="day">
		<xsl:value-of select="substring($day-temp,1,2)" />
	</xsl:variable>
	
	<xsl:variable name="time">
		<xsl:value-of select="substring-after($DateTime,'T')" />
	</xsl:variable>
	
	<xsl:variable name="hh">
		<xsl:value-of select="substring($time,1,2)" />
	</xsl:variable>
	
	<xsl:variable name="mm">
		<xsl:value-of select="substring($time,4,2)" />
	</xsl:variable>
	
	<xsl:variable name="ss">
		<xsl:value-of select="substring($time,7,2)" />
	</xsl:variable>
	
	<xsl:choose>
		<xsl:when test="$month = '01'">January</xsl:when>
		<xsl:when test="$month = '02'">February</xsl:when>
		<xsl:when test="$month = '03'">March</xsl:when>
		<xsl:when test="$month = '04'">April</xsl:when>
		<xsl:when test="$month = '05'">May</xsl:when>
		<xsl:when test="$month = '06'">June</xsl:when>
		<xsl:when test="$month = '07'">July</xsl:when>
		<xsl:when test="$month = '08'">August</xsl:when>
		<xsl:when test="$month = '09'">September</xsl:when>
		<xsl:when test="$month = '10'">October</xsl:when>
		<xsl:when test="$month = '11'">November</xsl:when>
		<xsl:when test="$month = '12'">December</xsl:when>
	</xsl:choose>

	<xsl:value-of select="' '"/>

	<xsl:value-of select="$day"/>

	<xsl:value-of select="', '"/>

	<xsl:value-of select="' '"/>

	<xsl:value-of select="$year"/>

	<!-- Time component is not needed
	
	<xsl:value-of select="' '"/>

	<xsl:value-of select="$hh"/>
	
	<xsl:value-of select="':'"/>

	<xsl:value-of select="$mm"/>
	
	<xsl:value-of select="':'"/>

	<xsl:value-of select="$ss"/>
	-->

</xsl:template>


<xsl:output method="html"/>
<xsl:template match="/">
<xsl:variable name="currShipKey" select="/Order/@CurrentShipmentKey" />
<xsl:text disable-output-escaping="yes"><![CDATA[<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">]]> </xsl:text>
<html>
<xsl:comment>CONTENT_TYPE=text/html; charset=UTF-8</xsl:comment>
<head>
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
		<font size="3" face="Arial">
			<b>*** Shipment Confirmation ***</b>
		</font>
	</WBR>
</p>

<table width="684" border="0" cellspacing="0">
	<tr>
		<td>
			<p>
				<font size="2" face="Arial">
				<b>Dear <xsl:value-of select="/Order/PersonInfoBillTo/@FirstName" />&#160;<xsl:value-of select="/Order/PersonInfoBillTo/@LastName" />,</b>
				</font>
				<br></br>
			</p>
			<p>
				<font size="2" face="Arial">
					This message is to notify you that your order has been shipped via Bulk Carrier Service - Delivered to Your Door
					<!-- Fix for STL 393 -->
					<!--
					<xsl:value-of select="concat(/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/@SCAC,'.')" />
					-->					
					To verify the transit status of your shipment, click on the tracking link below.
					<!-- Commented out the below part as a part of the JIRA STL-1703 Remove Ammo Text -->
					<!-- Added as a part of JIRA STL-1647 
					<br/><br/>Shipments containing ammunition items will require a signature by an adult upon delivery.<br/><br/>
					End JIRA STL-1647 -->
				</font>
			</p>
		</td>
	</tr>
</table>
<!-- <br></br> -->
			<table width="684" border="1" cellspacing="0">
				<tr valign="center">
					<td bgcolor="#3366CC" colspan="11" height="25">
						<font color="#FFFFFF" size="2" face="Arial">&#160;<b>IMPORTANT DELIVERY INFORMATION</b></font>
					</td>
				</tr>
				<tr valign="top">
					<td colspan="11">
						<table width="100%">
							<tr valign="middle">
								<td bgcolor="#EFEFEF" height="20">
									<font size="2" face="Arial">
										<b>Order #: <xsl:value-of select="/Order/@OrderNo" /></b>
									</font>
								</td>
							</tr>
						</table>
						<!-- <xsl:if test="/Order/PersonInfoShipTo[@IsSignatureRequired='Y']"> -->	
							<!-- <br></br>							
							<font size="2" face="Arial">
							<b>Signature Required</b> - This order requires an adult signature before it can be delivered.
						</font>			
						</xsl:if> -->						
						<table border="0" width="90%" cellspacing="0" cellpadding="0">
							<tr>
								<td>
									<table width="100%" border="0" cellspacing="0" cellpadding="0">
										<tr style="text-align:left">	
											<td width="22%" valign="top"><font size="2" face="Arial"><b>Ship To:</b></font></td>	
											<td>
												<font size="2" face="Arial">
													<xsl:value-of select="/Order/OrderLines/OrderLine[@DeliveryMethod='SHP']/PersonInfoShipTo/@FirstName" />&#160;<xsl:value-of select="/Order/OrderLines/OrderLine[@DeliveryMethod='SHP']/PersonInfoShipTo/@LastName" />
													<br></br>
													<xsl:value-of select="/Order/OrderLines/OrderLine[@DeliveryMethod='SHP']/PersonInfoShipTo/@AddressLine1"/>
													<br></br>
													<xsl:variable name="addressLine2" select="/Order/OrderLines/OrderLine[@DeliveryMethod='SHP']/PersonInfoShipTo/@AddressLine2" /> 
												 	<xsl:if test="$addressLine2!=''">
														<xsl:value-of select="$addressLine2"/><br/>
													</xsl:if>
													<xsl:value-of select="/Order/OrderLines/OrderLine[@DeliveryMethod='SHP']/PersonInfoShipTo/@City"/>, 
													<xsl:value-of select="/Order/OrderLines/OrderLine[@DeliveryMethod='SHP']/PersonInfoShipTo/@State"/>&#160;<xsl:value-of select="/Order/OrderLines/OrderLine[@DeliveryMethod='SHP']/PersonInfoShipTo/@ZipCode"/> 
												</font>
											</td>
										</tr>
									</table>
								</td>
								<td>
									<table width="100%" border="0" cellspacing="0" cellpadding="0">
										<tr style="text-align:left">	
											<td width="20%" valign="top"><font size="2" face="Arial"><b>Bill To:</b></font></td>	
											<td>
												<font size="2" face="Arial">
													<xsl:value-of select="/Order/PersonInfoBillTo/@FirstName" />&#160;<xsl:value-of select="/Order/PersonInfoBillTo/@LastName" />
													<br></br>
													<xsl:value-of select="/Order/PersonInfoBillTo/@AddressLine1"/>
													<br></br>
													<xsl:variable name="addressLine2" select="/Order/PersonInfoBillTo/@AddressLine2" /> 
												 	<xsl:if test="$addressLine2!=''">
														<xsl:value-of select="$addressLine2"/><br/>
													</xsl:if>
													<xsl:value-of select="/Order/PersonInfoBillTo/@City"/>, 
													<xsl:value-of select="/Order/PersonInfoBillTo/@State"/>&#160;<xsl:value-of select="/Order/PersonInfoBillTo/@ZipCode"/> 
												</font>
											</td>
										</tr>
									</table>
								</td>
							</tr>
						</table>
						<br></br>
						<table width="100%" border="0" cellspacing="0" cellpadding="0">
							<tr style="text-align:left">	
							<!-- Fix For STL-393 -->
								<td width="70%" valign="top"><font size="2" face="Arial">Shipping Method: </font></td>	
								<td valign="top">
									<font size="2" face="Arial">
									<xsl:text>Bulk Carrier Service - Delivered to Your Door </xsl:text>	
									<!--
										<xsl:variable name="scacAndService1" select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/@ScacAndService" />
											<b><xsl:value-of select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/@SCAC"/></b>
											-->
								<!-- Fix For STL-393 -->
								    </font>
								</td>
								
							</tr>
							<tr style="text-align:left">	
								<td width="18%" valign="top"><font size="2" face="Arial">Delivery Estimate: </font></td>	
								<td valign="top">
									<font size="2" face="Arial">
										<xsl:variable name="deliveryDate" select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/@ExpectedDeliveryDate" />
										<xsl:variable name="scacAndService" select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/@ScacAndService" />
										
										<xsl:attribute  name = "hasEstDate" >
										<xsl:value-of select="string-length($deliveryDate)&gt;'0'" />
										</xsl:attribute>
										<xsl:choose>
										
											<xsl:when test="string-length($deliveryDate)&gt;'0'">
												<xsl:call-template name="FormatDate">
													<xsl:with-param name="DateTime" select="$deliveryDate"/>
												</xsl:call-template>												
											</xsl:when>
																						
											<xsl:otherwise>
												<xsl:text>To Be Scheduled With Customer.</xsl:text>
											</xsl:otherwise>
										</xsl:choose>
									</font>
								</td>
							</tr>
							<tr style="text-align:left">	
								<td width="18%" valign="top"><font size="2" face="Arial">No. of Packages: </font></td>	
								<td valign="top">
									<font size="2" face="Arial">
										<xsl:value-of select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/Containers/@TotalNumberOfRecords" />
									</font>
								</td>
							</tr>
						</table>
						<table width="100%" border="0" cellpadding="0" cellspacing="0">
							<tr valign="top">
								<td width="12%" bgcolor="#FFCC00"><font size="2" face="Arial"><b>Tracking<br />Number(s):</b></font></td>
								<td width="10%" bgcolor="#FFCC00"></td>
								<td bgcolor="#FFCC00">
									<xsl:if test="/Order/OrderLines/OrderLine/ShipmentLines/ShipmentLine[@ShipmentKey=$currShipKey]/../../ItemDetails/Extn/@ExtnWhiteGloveEligible='Y'">
							     		<xsl:for-each select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]">
											<xsl:variable name="proNo" select="./@ProNo" />
											
											<!-- Start WN-1627 Phase 1 Tracking link update from old to Narvar Link -->
											<xsl:variable name="serviceCode" select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/@ExtnServiceCode" />
															<xsl:variable name="scac" select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/@ExtnSCAC" />
															<xsl:variable name="originZipCode" select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/ShipNode/ShipNodePersonInfo/@ZipCode" />
															<xsl:variable name="customerZipCode" select="/Order/PersonInfoShipTo/@ZipCode" />
															<a href="https://academy.narvar.com/academy/tracking/{$scac}?tracking_numbers={$proNo}&amp;service={$serviceCode}&amp;ozip={$originZipCode}&amp;dzip={$customerZipCode}" >
											 <!-- <a href="http://etracking.cevalogistics.com/eTrackResults.aspx?st=1&amp;sf=1&amp;sv={$proNo}" target="_blank"> -->
											 <!-- Start WN-1627 Phase 1 Tracking link update from old to Narvar Link -->
											 
												<font size="2" face="Arial" >
													<u><b><xsl:value-of select="$proNo"/></b></u>
												</font>
											</a>&#160;
										</xsl:for-each>	
						     		</xsl:if>
								</td>
							</tr>	
						</table>
						<br/>

			<table width="684" border="0" cellspacing="0" cellpadding="0">
				<tr border="1" valign="center" style="text-align:center">
					<td bgcolor="#3366CC" width="18%" style="text-align:left">
						<font color="#FFFFFF" size="2" face="Arial"><b>&#160;ITEM</b></font>
					</td>
					<td bgcolor="#3366CC" width="64%" style="text-align:left">
						<font color="#FFFFFF" size="2" face="Arial"><b>&#160;ITEM DESCRIPTION</b></font>
					</td>
					<td bgcolor="#3366CC" width="9%" >
						<font color="#FFFFFF" size="2" face="Arial"><b>ORDER QTY</b></font>
					</td>
					<td bgcolor="#3366CC" width="8%" >
						<font color="#FFFFFF" size="2" face="Arial"><b>SHIP QTY</b></font>
					</td>
				</tr>
				<!-- ONLY shipped items need to be listed -->
				
				<xsl:for-each select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/ShipmentLines/ShipmentLine">
					<xsl:variable name="orderLineKey" select="./@OrderLineKey" />
					<xsl:for-each select="/Order/OrderLines/OrderLine">
						<xsl:if test="./@OrderLineKey=$orderLineKey">
							<!-- <tr valign="top">
								<td>
									<p>&#160;</p>
								</td>
							</tr> -->
							<tr valign="top" style="text-align:center">
								<td style="text-align:left">
									<font size="2" face="Arial">&#160;
										<b><xsl:value-of select="ItemDetails/@ItemID" /></b>
									</font>
								</td>
								<td colspan="1" style="text-align:left">
									<font size="2" face="Arial">
										<xsl:value-of select="ItemDetails/PrimaryInformation/@Description" />
									</font>
								</td>
								<td>
									<font size="2" face="Arial">
										<xsl:value-of select="format-number(@OrderedQty, '0')" />
									</font>
								</td>
								<td>
									<font size="2" face="Arial">
										<xsl:value-of select="format-number(@InvoicedQty, '0')" />
									</font>
								</td>
							</tr>
						</xsl:if>
					</xsl:for-each>
				</xsl:for-each>
	
<!--
				<xsl:for-each select="/Order/OrderLines/OrderLine">
					<tr valign="top" style="text-align:center">
						<td>
							<font size="2" face="Arial">
								<b><xsl:value-of select="ItemDetails/@ItemID" /></b>
							</font>
						</td>
						<td colspan="1" style="text-align:left">
							<font size="2" face="Arial">
								<xsl:value-of select="ItemDetails/PrimaryInformation/@Description" />
							</font>
						</td>
						<td>
							<font size="2" face="Arial">
								<xsl:value-of select="format-number(@OrderedQty, '#')" />
							</font>
						</td>
						<td>
							<font size="2" face="Arial">
								<xsl:value-of select="format-number(@InvoicedQty, '#')" />
							</font>
						</td>
					</tr>
					<tr>
						<td>
							<p>&#160;</p>
						</td>
					</tr>
	
				</xsl:for-each>
-->		
				<!-- NOT NEEDED
				
				<tr valign="top">
					<td bgcolor="#3366CC" rowspan="2">
						<font color="#FFFFFF" size="2" face="Arial">
							<b>Summary</b>
						</font>
					</td>
					<td bgcolor="#3366CC">
						<font color="#FFFFFF" size="2" face="Arial">
							<b>Subtotal</b>
						</font>
					</td>
					<td bgcolor="#3366CC">
						<font color="#FFFFFF" size="2" face="Arial">
							<b>Discount</b>
						</font>
					</td>
					<td bgcolor="#3366CC">
						<font color="#FFFFFF" size="2" face="Arial">
							<b>Shipping</b>
						</font>
					</td>
					<td bgcolor="#3366CC">
						<font color="#FFFFFF" size="2" face="Arial">
							<b>Tax</b>
						</font>
					</td>
				
					<td bgcolor="#3366CC">
						<font color="#FFFFFF" size="2" face="Arial">
							<b>Total Amount</b>
						</font>
					</td>
					<td bgcolor="#3366CC" colspan="2">
						<font color="#FFFFFF" size="2" face="Arial">
							<b>Payment Information</b>
						</font>
					</td>
				</tr>
				<tr valign="top">
					<td>
						<font size="2" face="Arial">
							<b>$<xsl:value-of select="/Order/OverallTotals/@LineSubTotal" /></b>
						</font>
					</td>
				
					<td>
						<font size="2" face="Arial">
							<b>
								$
								<xsl:value-of select="/Order/OverallTotals/@GrandDiscount" />
							</b>
						</font>
					</td>
					<td>
						<font size="2" face="Arial">
							<b>
								$
								<xsl:value-of select="/Order/OverallTotals/@GrandCharges" />
							</b>
						</font>
					</td>
					<td>
						<font size="2" face="Arial">
							<b>
								$
								<xsl:value-of select="/Order/OverallTotals/@GrandTax" />
							</b>
						</font>
					</td>
					<td>
						<font size="2" face="Arial">
							<b>
								$
								<xsl:value-of select="/Order/OverallTotals/@GrandTotal" />
							</b>
						</font>
					</td>
					<td colspan="2">
					<xsl:for-each select="/Order/PaymentMethods/PaymentMethod">		
						<font size="2" face="Arial">
							<b>
							<xsl:if test="@PaymentType='GIFT_CARD'">
								GC (<xsl:value-of select="@SvcNo" />) $ <xsl:value-of select="@MaxChargeLimit" />
							</xsl:if>
							</b>
						</font>			
					</xsl:for-each>
					</td>
					</tr>
					<tr>
					<td colspan="6"></td>
					<td colspan="2">
						<xsl:for-each select="/Order/PaymentMethods/PaymentMethod">		
						<font size="2" face="Arial">
							<b>		
							<xsl:if test="@PaymentType='CREDIT_CARD'">
								Visa (<xsl:value-of select="@DisplayCreditCardNo" />) $ <xsl:value-of select="@MaxChargeLimit" />
							</xsl:if>								
							</b>
						</font>			
					</xsl:for-each>
				
					</td>
					</tr>
				-->
</table>
</td>
</tr>
</table>
<br/>

<!-- Added as a part of JIRA STL-1650 -->
<table width="700" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td>
			<font size="2" face="Arial"><a href="https://assemblersinc.net/academy/">If you would like any help assembling or installing your items, please contact our partners at Assemblers Inc</a></font>
		</td>
		<td>
			<a href="https://assemblersinc.net/academy/"><img src="https://assemblersinc.net/academy/Logo.png" border="0" width="70" height="70" alt="Assemblers, Inc."></img></a>
		</td>
	</tr>
</table>
<br/>
<!-- End JIRA STL-1650 -->

<table width="684" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td>
			<font size="2" face="Arial">Thank you for shopping at Academy Sports + Outdoors.</font>
		</td>
	</tr>
</table>

<br/>

<table width="684" border="0" cellspacing="0" cellpadding="0">
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

<table width="684" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td>

			<font size="2" face="Arial">
				<b>Please note:</b>This email message was sent from a notification-only address that cannot accept incoming email. <br />Please do not reply to this message.
			</font>
		</td>
	</tr>
</table>
</body>
</html>
</xsl:template>
</xsl:stylesheet>