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

	<xsl:variable name="varOrderNo">
		<xsl:value-of select="/Order/@SalesOrderNo" />
	</xsl:variable>

	<xsl:variable name="varZipCode">
		<xsl:value-of select="/Order/OrderLines/OrderLine[@DeliveryMethod='SHP']/PersonInfoShipTo/@ZipCode"/> 
	</xsl:variable>


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
						This message is to notify you that your order has been shipped via 
						<xsl:value-of select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/@SCAC" />
						<!-- Commented out the below part as a part of the JIRA STL-1703 Remove Ammo Text -->
						<!-- Added as a part of JIRA STL-1647 
						<br/><br/>Shipments containing ammunition items will require a signature by an adult upon delivery.<br/><br/>
						End JIRA STL-1647 -->						
					</font>
				</p>
			</td>
		</tr>
	</table>
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
								<b>Order #: <xsl:value-of select="$varOrderNo" /></b>
							</font>
						</td>
					</tr>
				</table>
				<table border="0" width="90%" cellspacing="0" cellpadding="0">
					<tr>
						<td>
							<table width="100%" border="0" cellspacing="0" cellpadding="0">
								<tr style="text-align:left">	
									<td width="22%" valign="top"><font size="2" face="Arial"><b>Ship To:</b></font></td>	
									<td>
										<font size="2" face="Arial">
											<xsl:value-of select="/Order/OrderLines/OrderLine[@DeliveryMethod='SHP']/PersonInfoShipTo/@FirstName" />
											&#160;<xsl:value-of select="/Order/OrderLines/OrderLine[@DeliveryMethod='SHP']/PersonInfoShipTo/@LastName" />
											<br></br>
											<xsl:value-of select="/Order/OrderLines/OrderLine[@DeliveryMethod='SHP']/PersonInfoShipTo/@AddressLine1"/>
											<br></br>
											<xsl:variable name="addressLine2" select="/Order/OrderLines/OrderLine[@DeliveryMethod='SHP']/PersonInfoShipTo/@AddressLine2" /> 
											<xsl:if test="$addressLine2!=''">
												<xsl:value-of select="$addressLine2"/><br/>
											</xsl:if>
											<xsl:value-of select="/Order/OrderLines/OrderLine[@DeliveryMethod='SHP']/PersonInfoShipTo/@City"/>, 
											<xsl:value-of select="/Order/OrderLines/OrderLine[@DeliveryMethod='SHP']/PersonInfoShipTo/@State"/>
											&#160;<xsl:value-of select="/Order/OrderLines/OrderLine[@DeliveryMethod='SHP']/PersonInfoShipTo/@ZipCode"/> 
										</font>
									</td>
								</tr>
							</table>
						</td>
						<td>
						<table width="100%" border="0" cellspacing="0" cellpadding="0">
							<tr style="text-align:left">	
								<td width="22%" valign="top"><font size="2" face="Arial"><b>Bill To:</b></font></td>	
								<td>
									<font size="2" face="Arial">
										<xsl:value-of select="/Order/PersonInfoBillTo/@FirstName" />
										&#160;<xsl:value-of select="/Order/PersonInfoBillTo/@LastName" />
										<br></br>
										<xsl:value-of select="/Order/PersonInfoBillTo/@AddressLine1"/>
										<br></br>
										<xsl:variable name="addressLine2" select="/Order/PersonInfoBillTo/@AddressLine2" /> 
										<xsl:if test="$addressLine2!=''">
											<xsl:value-of select="$addressLine2"/><br/>
										</xsl:if>
										<xsl:value-of select="/Order/PersonInfoBillTo/@City"/>, 
										<xsl:value-of select="/Order/PersonInfoBillTo/@State"/>
										&#160;<xsl:value-of select="/Order/PersonInfoBillTo/@ZipCode"/> 
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
					<td width="18%" valign="top">
						<font size="2" face="Arial">Shipping Method: </font>
					</td>	
					<td valign="top">
						<font size="2" face="Arial">
						<!-- Shipping Method needs to be displayed as 'SCAC - CarrierServiceCode' -->
							<xsl:variable name="varSCAC" select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/@SCAC" />
							<xsl:variable name="varCarrierServiceCode" select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/@CarrierServiceCode" />										
							<xsl:value-of select="concat($varSCAC,' - ', $varCarrierServiceCode)" />								
						</font>
					</td>
				</tr>
				<tr style="text-align:left">	
					<td width="18%" valign="top">
						<font size="2" face="Arial">Delivery Estimate: </font>
					</td>	
					<td valign="top">
						<font size="2" face="Arial">
							<b>
								<xsl:value-of select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/@EstimatedDeliveryDate" />
							</b>
						</font>
					</td>
				</tr>
				<tr style="text-align:left">	
					<td width="18%" valign="top">
						<font size="2" face="Arial">No. of Packages: </font>
					</td>	
					<td valign="top">
						<font size="2" face="Arial">
							<xsl:value-of select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/Containers/@TotalNumberOfRecords" />
						</font>
					</td>
				</tr>
			</table>
			<table width="100%" border="0" cellpadding="0" cellspacing="0">
				<tr valign="top">
					<td width="20%" bgcolor="#FFCC00">
						<font size="2" face="Arial"><b>Tracking Number:</b></font>
					</td>
					<td width="10%" bgcolor="#FFCC00"></td>
					<td bgcolor="#FFCC00">
					
						<xsl:for-each select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/Containers/Container">
						<xsl:variable name="currTrackingNo" select="./@TrackingNo" />
						
						<!-- Start WN-1627 Phase 1 Tracking link update from old to Narvar Link -->
						<xsl:variable name="serviceCode" select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/@ExtnServiceCode" />
						<xsl:variable name="scac" select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/@ExtnSCAC" />
						<xsl:variable name="originZipCode" select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/ShipNode/ShipNodePersonInfo/@ZipCode" />
						<xsl:variable name="customerZipCode" select="/Order/PersonInfoShipTo/@ZipCode" />
						<xsl:choose>
						<xsl:when test="normalize-space($scac) != ''">
							<a href="https://academy.narvar.com/academy/tracking/{$scac}?tracking_numbers={$currTrackingNo}&amp;service={$serviceCode}&amp;ozip={$originZipCode}&amp;dzip={$customerZipCode}" >
							<font size="2" face="Arial" >
								<u><b><xsl:value-of select="@TrackingNo"/></b></u>
							</font>
							</a>&#160;
						</xsl:when>
						<!-- <xsl:when test="@SCAC='UPSN'">
							<a href="http://wwwapps.ups.com/WebTracking/processRequest?HTMLVersion=5.0&amp;Requester=NES&amp;AgreeToTermsAndConditions=yes&amp;loc=en_US&amp;tracknum={$currTrackingNo}" target="_blank">
							<font size="2" face="Arial" >
								<u><b><xsl:value-of select="@TrackingNo"/></b></u>
							</font>
							</a>&#160;
						</xsl:when>
						<xsl:when test="@SCAC='FEDX'">
							<a href="http://www.fedex.com/Tracking?language=english&amp;cntry_code=us&amp;tracknumbers={$currTrackingNo}" target="_blank"> 
							<font size="2" face="Arial" >
								<u><b><xsl:value-of select="@TrackingNo"/></b></u>
							</font>
							</a>&#160;
						</xsl:when>
						<xsl:when test="@SCAC='SmartPost'">
							<a href="http://www.fedex.com/Tracking?language=english&amp;cntry_code=us&amp;tracknumbers={$currTrackingNo}" target="_blank"> 
							<font size="2" face="Arial" >
								<u><b><xsl:value-of select="@TrackingNo"/></b></u>
							</font>
							</a>&#160;
						</xsl:when>
						<xsl:when test="@SCAC='CEVA'">
							<a href="http://etracking.cevalogistics.com/eTrackResults.aspx?st=1&amp;sf=1&amp;sv={$currTrackingNo}" target="_blank">
							<font size="2" face="Arial" >
								<u><b><xsl:value-of select="@TrackingNo"/></b></u>
							</font>
							</a>&#160;
						</xsl:when> -->
						<!-- End WN-1627 Phase 1 Tracking link update from old to Narvar Link -->
						
						<xsl:otherwise>
						<xsl:value-of select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/Containers/Container/@TrackingNo" />&#160;
						</xsl:otherwise>
						</xsl:choose>			
						</xsl:for-each>	
						
					</td>
				</tr>
				<tr></tr>
				<tr valign="top">
					<td width="12%" bgcolor="#FFCC00">
						<font size="2" face="Arial">
							<b>Invoice Number:</b>
						</font>
					</td>
					<td width="10%" bgcolor="#FFCC00"></td>
					<td bgcolor="#FFCC00"> 
						<font size="2" face="Arial" >
							<b>
								<xsl:value-of select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/@InvoiceNo" />
							</b>
						</font>
					</td>
				</tr>
			</table>
			<table width="684" border="0" cellspacing="0" cellpadding="0">
				<tr border="1" valign="center" style="text-align:center">
					<td bgcolor="#3366CC" width="18%" style="text-align:left">
						<font color="#FFFFFF" size="2" face="Arial"><b>&#160;ITEM</b></font>
					</td>
					<td bgcolor="#3366CC" width="55%" style="text-align:left">
						<font color="#FFFFFF" size="2" face="Arial"><b>&#160;ITEM DESCRIPTION</b></font>
					</td>
					<td bgcolor="#3366CC" width="12%" >
						<font color="#FFFFFF" size="2" face="Arial"><b>ORDER QTY</b></font>
					</td>
					<td bgcolor="#3366CC" width="18%" >
						<font color="#FFFFFF" size="2" face="Arial"><b>SHIP QTY</b></font>
					</td>
				</tr>
				
						
				<xsl:for-each select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/ShipmentLines/ShipmentLine">
					<xsl:variable name="orderLineKey" select="./@OrderLineKey" />
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
										<font size="2" face="Arial">
											<xsl:value-of select="format-number(@ShippedQty, '0')"  />
										</font>
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
	<table width="684" border="0" cellspacing="0" cellpadding="0">
		<tr>
			<td>
				<font size="2" face="Arial">
				
							
					Please click the below link to print the invoice.<br/>
					<a href="https://www.academy.com/webapp/wcs/stores/servlet/UserAccountOrderStatus?orderId={translate($varOrderNo,' ','')}&amp;zipCode={translate($varZipCode,' ','')}&amp;langId=-1&amp;storeId=10151&amp;catalogId=10051&amp;isSubmitted=true&amp;URL=GuestOrderDetailView&amp;errorViewName=GuestOrderStatusView&amp;x=118&amp;y=14" target="_blank">
						<font size="2" face="Arial" >
							<u><b>https://www.academy.com/webapp/wcs/stores/servlet/UserAccountOrderStatus?orderId=<xsl:value-of select="translate($varOrderNo,' ','')"/></b></u>
						</font>
					</a>
				</font>
				
			</td>
		</tr>
		<br/>
		</table>
		<table width="684" border="0" cellspacing="0" cellpadding="0">
		<tr>
			<td>
				<font size="2" face="Arial">			
				If you need further assistance, please contact Customer Service at <a href="http://www.academy.com/contactus" target="_blank"><u>www.academy.com/contactus</u></a>.
				You may also call us at 1-888-922-2336 and an Academy Associate will gladly assist you. 
				</font>
			</td>
		</tr>
		<br/>
	</table>
	<table width="684" border="0" cellspacing="0" cellpadding="0">
		<tr><br/>
			<td>
				<font size="2" face="Arial">
					<b>Please note: </b>Please keep this email for returns.<br/><br/>
					This email message was sent from a notification-only address that cannot accept incoming email. <br />Please do not reply to this message. 
				</font>
			</td>
		</tr>
	</table>
	</body>
	</html>
	</xsl:template>
	</xsl:stylesheet>