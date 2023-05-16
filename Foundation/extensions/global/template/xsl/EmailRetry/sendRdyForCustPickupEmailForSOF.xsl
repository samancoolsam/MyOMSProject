<!--SOF :: Start WN-1814 Ready for Customer Pickup Email-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:lxslt="http://xml.apache.org/xslt" version="1.0">
	<xsl:output method="html"/>
	<xsl:template match="/">
		<xsl:variable name="currShipKey" select="/Order/@CurrentShipmentKey" />
		<xsl:text disable-output-escaping="yes">
			<![CDATA[<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">]]>
		</xsl:text>
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
							<img src="http://assets.academy.com/mgen/64/10099564.jpg" border="0" alt=""/>
						</td>
					</tr>
				</table>
				<p> 
					<WBR>
						<font size="3" face="Arial">
							<b>*** Your Order is Ready for Pickup  ***</b>
						</font>
					</WBR>
				</p>

				<table width="684" border="0" cellspacing="0">
					<tr>
						<td>
							<p>
								<font size="2" face="Arial">
									<b>Dear <xsl:value-of select="/Order/PersonInfoBillTo/@FirstName" />&#160;
										<xsl:value-of select="/Order/PersonInfoBillTo/@LastName" />,</b>
								</font>
								<br/>
							</p>
							<p>
								<font size="2" face="Arial">
					This email is to notify you that your order is Ready for Pickup. Please print this email or show it to a Team Member at the Gun Counter in your Pickup Store.						
								</font>
							</p>
						</td>
					</tr>
				</table>
				<table width="684" border="1" cellspacing="0">
					<tr valign="center">
						<td bgcolor="#3366CC" colspan="11" height="25">
							<font color="#FFFFFF" size="2" face="Arial">&#160;
								<b>IMPORTANT PICKUP INFORMATION</b>
							</font>
						</td>
					</tr>
					<tr valign="top">
						<td colspan="11">
							<table width="100%">
								<tr valign="middle">
									<td bgcolor="#EFEFEF" height="20">
										<font size="2" face="Arial">
											<b>Order # : <xsl:value-of select="/Order/@OrderNo" />
											</b>
											<br/>
											<b>Invoice # : <xsl:value-of select="/Order/OrderInvoiceList/OrderInvoice[@ShipmentKey=$currShipKey][@InvoiceType='SHIPMENT']/@InvoiceNo" />
											</b>
										</font>
									</td>
									<br/>
								</tr>
							</table>
							<br/>
							<table border="0" width="90%" cellspacing="0" cellpadding="0">
								<tr>
									<td>
										<table width="100%" border="0" cellspacing="0" cellpadding="0">
											<tr style="text-align:left">	
												<td width="22%" valign="top">
													<font size="2" face="Arial">
														<b>Pickup</b>
														<br/>
														<b>Information : </b>
													</font>
												</td>
												<td>
													<font size="2" face="Arial">
														<xsl:value-of select="/Order/PersonInfoBillTo/@FirstName" />&#160;<xsl:value-of select="/Order/PersonInfoBillTo/@LastName" />
														<br/>
														<xsl:value-of select="/Order/@CustomerEMailID"/>
														<br/>
														<xsl:value-of select="/Order/@CustomerPhoneNo"/> 
													</font>
												</td>
											</tr>
										</table>
									</td>
								</tr>
							</table>
							<br/>						
							<table border="0" width="90%" cellspacing="0" cellpadding="0">
								<tr>
									<td>
										<table width="100%" border="0" cellspacing="0" cellpadding="0">
											<tr style="text-align:left">	
												<td width="22%" valign="top">
													<font size="2" face="Arial">
														<b>Pickup Store:</b>
													</font>
												</td>	
												<td>
													<font size="2" face="Arial">
														<xsl:value-of select="/Order/OrderLines/OrderLine[@DeliveryMethod='SHP']/PersonInfoShipTo/@AddressLine2" />
														<br/>
														<xsl:value-of select="/Order/OrderLines/OrderLine[@DeliveryMethod='SHP']/PersonInfoShipTo/@AddressLine1"/>
														<br/>
														<xsl:value-of select="/Order/OrderLines/OrderLine[@DeliveryMethod='SHP']/PersonInfoShipTo/@City"/>, 
														<xsl:value-of select="/Order/OrderLines/OrderLine[@DeliveryMethod='SHP']/PersonInfoShipTo/@State"/>&#160;
														<xsl:value-of select="/Order/OrderLines/OrderLine[@DeliveryMethod='SHP']/PersonInfoShipTo/@ZipCode"/> 
														<br/>
														<xsl:value-of select="/Order/OrderLines/OrderLine[@DeliveryMethod='SHP']/PersonInfoShipTo/@DayPhone"/> 
													</font>
												</td>
											</tr>
										</table>
									</td>
									<td>
										<table width="100%" border="0" cellspacing="0" cellpadding="0">
											<tr style="text-align:left">	
												<td width="20%" valign="top">
													<font size="2" face="Arial">
														<b>Bill To:</b>
													</font>
												</td>	
												<td>
													<font size="2" face="Arial">
														<xsl:value-of select="/Order/PersonInfoBillTo/@FirstName" />&#160;<xsl:value-of select="/Order/PersonInfoBillTo/@LastName" />
														<br/>
														<xsl:value-of select="/Order/PersonInfoBillTo/@AddressLine1"/>
														<br/>
														<xsl:variable name="addressLine2" select="/Order/PersonInfoBillTo/@AddressLine2" /> 
														<xsl:if test="$addressLine2!=''">
															<xsl:value-of select="$addressLine2"/>
															<br/>
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
							<br/>					
							<br/>

							<table width="684" border="0" cellspacing="0" cellpadding="0">
								<tr border="1" valign="center" style="text-align:center">
									<td bgcolor="#3366CC" width="18%" style="text-align:left">
										<font color="#FFFFFF" size="2" face="Arial">
											<b>&#160;ITEM</b>
										</font>
									</td>
									<td bgcolor="#3366CC" width="64%" style="text-align:left">
										<font color="#FFFFFF" size="2" face="Arial">
											<b>&#160;ITEM DESCRIPTION</b>
										</font>
									</td>
									<td bgcolor="#3366CC" width="9%" >
										<font color="#FFFFFF" size="2" face="Arial">
											<b>ORDER QTY</b>
										</font>
									</td>
									<td bgcolor="#3366CC" width="8%" >
										<font color="#FFFFFF" size="2" face="Arial">
											<b>PICKUP QTY</b>
										</font>
									</td>
								</tr>

								<xsl:for-each select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/ShipmentLines/ShipmentLine">
									<xsl:variable name="orderLineKey" select="./@OrderLineKey" />
									<xsl:variable name="ShipQty" select="./@Quantity" />
									<xsl:for-each select="/Order/OrderLines/OrderLine">

										<xsl:if test="./@OrderLineKey=$orderLineKey">
											<tr valign="top" style="text-align:center">
												<td style="text-align:left">
													<font size="2" face="Arial">&#160;
														<b>
															<xsl:value-of select="ItemDetails/@ItemID" />
														</b>
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
													<font size="2" face="Arial">1</font>
												</td>
											</tr>
										</xsl:if>
									</xsl:for-each>
								</xsl:for-each>
							</table>
						</td>
					</tr>
				</table>
				<br/>

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
							<font size="2" face="Arial">Return Instructions - you must bring a copy of your Ready for Pickup email for the firearm you are returning to the Academy store to cancel your order.</font>
						</td>
					</tr>
				</table>

				<br/>

				<table width="684" border="0" cellspacing="0" cellpadding="0">
					<tr>
						<td>
							<font size="2" face="Arial">
			If you need further assistance, please contact Customer Service at <a href="http://www.academy.com/contactus" target="_blank">
									<u>www.academy.com/contactus</u>
								</a>.
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
<!--SOF :: End WN-1814 Ready for Customer Pickup Email-->