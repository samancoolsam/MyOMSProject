<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:lxslt="http://xml.apache.org/xslt" version="1.0"
	xmlns:java="http://xml.apache.org/xslt/java" xmlns:fopUtil="java://com.yantra.pca.ycd.fop.YCDFOPUtils"
	xmlns:emailformatters="java://com.academy.ecommerce.sterling.bopis.print.utils.AcademyPrintUtils">
	<xsl:output method="html"/>
	<xsl:template match="/">
		<xsl:variable name="URL_AcademyHomePage" select="/Order/@URL_AcademyHomePage" />
		<xsl:variable name="URL_CLOTHING" select="/Order/@URL_CLOTHING" />
		<xsl:variable name="URL_FOOTWEAR" select="/Order/@URL_FOOTWEAR" />
		<xsl:variable name="URL_SPORTS" select="/Order/@URL_SPORTS" />
		<xsl:variable name="URL_OUTDOORS" select="/Order/@URL_OUTDOORS" />
		<xsl:variable name="URL_ACCESSORIES" select="/Order/@URL_ACCESSORIES" />
		<xsl:variable name="URL_HOTDEALS" select="/Order/@URL_HOTDEALS" />
		<xsl:variable name="URL_CLEARANCE" select="/Order/@URL_CLEARANCE" />
		<xsl:variable name="URL_STORELOCATOR" select="/Order/@URL_STORELOCATOR" />
		<xsl:variable name="URL_AssemblyInstructions" select="/Order/@URL_AssemblyInstructions" />
		<xsl:variable name="URL_ViewOrderDetails" select="/Order/@URL_ViewOrderDetails" />
		<xsl:variable name="URL_ReturnOrderInstructions" select="/Order/@URL_ReturnOrderInstructions" />
		<xsl:variable name="URL_GETANSWER" select="/Order/@URL_GETANSWER" />
		<xsl:variable name="URL_OrdPickFAQ" select="/Order/@URL_OrdPickFAQ" />
		<xsl:variable name="URL_LiveChat" select="/Order/@URL_LiveChat" />
		<xsl:variable name="URL_UpdateProfile" select="/Order/@URL_UpdateProfile" />
		<xsl:variable name="URL_PrivacyPolicy" select="/Order/@URL_PrivacyPolicy" />
		<xsl:variable name="URL_Facebook" select="/Order/@URL_Facebook" />
		<xsl:variable name="URL_Twitter" select="/Order/@URL_Twitter" />
		<xsl:variable name="URL_PinRest" select="/Order/@URL_PinRest" />
		<xsl:variable name="URL_YouTube" select="/Order/@URL_YouTube" />
		<xsl:variable name="URL_Instagram" select="/Order/@URL_Instagram" />
		<xsl:variable name="IMG_AcademyLOGO" select="/Order/@IMG_AcademyLOGO" />
		<xsl:variable name="IMG_HOME" select="/Order/@IMG_HOME" />
		<xsl:variable name="IMG_NOIMAGE" select="/Order/@IMG_NOIMAGE" />
		<xsl:variable name="IMG_GetAnswer" select="/Order/@IMG_GetAnswer" />
		<xsl:variable name="IMG_OrdPickFAQ" select="/Order/@IMG_OrdPickFAQ" />
		<xsl:variable name="IMG_Facebook" select="/Order/@IMG_Facebook" />
		<xsl:variable name="IMG_Twitter" select="/Order/@IMG_Twitter" />
		<xsl:variable name="IMG_Pinrest" select="/Order/@IMG_Pinrest" />
		<xsl:variable name="IMG_Youtube" select="/Order/@IMG_Youtube" />
		<xsl:variable name="IMG_Instagram" select="/Order/@IMG_Instagram" />
		<xsl:variable name="URL_TakeSurvey" select="/Order/@URL_TakeSurvey" />	
		<xsl:variable name="IMG_TakeSurvey" select="/Order/@IMG_TakeSurvey" />
		<!--BOPIS-1451 - Start -->
		<xsl:variable name="URL_BarcodeAPI" select="/Order/@URL_BarcodeAPI" />
		<xsl:variable name="URL_BarcodeAPI1" select="/Order/@URL_BarcodeAPI1" />
		<!--BOPIS-1451 - End -->
		<!-- Start : OMNI-9414 : Multiple emails for STS and BOPIS -->
		<xsl:variable name="varCurrentShipmentKey" select="/Order/@CurrentShipmentKey" />
		<!-- End : OMNI-9414 : Multiple emails for STS and BOPIS -->

		<xsl:text disable-output-escaping="yes">
			<![CDATA[<!DOCTYPE html>]]>
		</xsl:text>
		<html>
			<xsl:comment>CONTENT_TYPE=text/html; charset=UTF-8</xsl:comment>
			<head>
				<!-- Yahoo App Android will strip this -->
			</head>
			<head>
				<link href="https://fonts.googleapis.com/css?family=Roboto" rel="stylesheet" type="text/css" />
				<style type="text/css">
					@import url(https://fonts.googleapis.com/css?family=Roboto);
				</style>
				<style>
@media only screen and (max-width: 850px) {
	.AcadLogoPos{
		text-align: left;
	}
	.AcadLogoSize{
		width: 350px !important;
		height: auto;
	}
	.AcadLogoTab{
		width: 50% !important;
		margin-left: 100px !important;
	}
	.HideLabel{
		display: none;
	}
	.SlideBarWidth{
		margin-left: 25px !important;
		width: 90%;
	}
	.HeaderPos{
		text-align: left !important;
		margin-left:25px !important;
	}
	.HeaderTab{
		width: 90% !important;
	}
	.HeaderFont{
		font-size: 38px;
		color: #333333 !important;
	}
	.OrderNoPos{
		text-align: left;
		margin-left:25px !important;
	}
	.OrderNoTab{
		width: 90% !important;
	}
	.OrderNoFont{
		font-size: 35px;
	}
	.Block{
		display: block;
	}
	.InlineBlock{
		display: inline-block;
	}
	.OrderNoTd{
		width:100% !important;
	}
	.MainDiv{
		width:600px !important;
	}
	.Margin{
		margin-left:125px !important;
	}
	.CenterAlign{
		text-align: left;
	}
	.ItemPos{
		text-align: left;
		margin-left:25px !important;
	}
	.ItemTab{
		width: 90% !important;
	}
	.ItemImageSize{
		width: 250px !important;
		height: auto;
	}
	.ItemFont{
		font-size: 30px !important;
		line-height: 1.20 !important;
	}
	.vodPos{
		text-align: left;
		margin-left:25px !important;
	}
	.vodTab{
		width: 90% !important;
	}
	.font{
		font-size: 35px !important;
		line-height: 1.20 !important;
	}
	.QuesBannerTab{
		width: 90% !important;
	}
	.QuesBannerPos{
		text-align: left;
		margin-left:25px !important;
	}
	.PickupTab{
		width: 90% !important;
	}
	.PickupPos{
		text-align: left !important;
		margin-left: 25px !important;
	}
	.AddressTab{
		width: 90% !important;
	}
	.AddressPos{
		text-align: left !important;
		padding-left: 1px !important;
		width: 100% !important;
	}
	.LinePos{
		margin-top:100px;
		border-bottom: 0px solid !important;
		border-bottom-color:#0055a6;
		padding-bottom: 10px;
	}
	.InstrFont{
		font-size: 35px !important;
		line-height: 1.20 !important;
	}
	.BarcodeSize{
		width: 450px !important;
		height: 200;
	}
	.totalTab{
		width: 90% !important;
	}
	.totalPos{
		text-align: left !important;
		margin-left: 25px !important;
	}
	.totalTd{
		width: 75% !important;
	}
	.TotalPos{
		text-align: left;
		margin-left:25px !important;
	}
	.TotalTab{
		width: 90% !important;
	}
	.TotalAlign{
		text-align: left !important;
		padding-left:60px !important;
		width:85% !important;
	}
	.TotalValueAlign{
		text-align: right !important;
		width: 30% !important;
		padding-right: 65px !important;
	}
	.desktopHide{
		display: inline-block !important;
		width:120%;
	}
	.mobileHide{
		display: none;
	}

	.desktopHide1{
		display: block !important;
		width:90%;
	}
	.FooterFont{
		font-size: 25px !important;
	}
	<!-- OMNI-6211 Pick Up Confirmation Email - START-->
	.ReturnPos1{
		padding-left: 0 !important;
		margin-left: 0 !important;
		text-align: center !important;
	}
	.buttonFont{
			font-family: Roboto,Helvetica, Arial, sans-serif;
			font-size:21px !important;
			text-align: right !important;
			margin: 0px !important;
			padding:0px !important;
			color:#333333;
	}
	<!-- OMNI-6211 Pick Up Confirmation Email - START-->
	.BarcodePos{
		text-align: center;
		margin-left:25px !important;
	}
}
				</style>
				<style>
			@media screen yahoo and (min-width:850px){
				#Atom{
					overflow-x: hidden !important;
					overflow-y: hidden !important;
					overflow: hidden !important;
				}
				.iy_A {
					overflow-x: hidden !important;
					overflow-y: hidden !important;
					overflow: hidden !important;
				}
				div{
					overflow-x: hidden !important;
					overflow-y: hidden !important;
					overflow: hidden !important;
				}
			}
				</style>
				<style type="text/css">
					#text {
						font-family:Roboto,Helvetica, Arial, sans-serif;
						padding-bottom:16px;
						font-size: 16px;
						font-weight: normal;
						font-style: normal;
						font-stretch: normal;
						line-height: 1.25;
						letter-spacing: normal;
						color: #333333;padding-top:0;margin-bottom:0
						}

						#text1 {
						font-family:Roboto,Helvetica, Arial, sans-serif;
						padding-bottom:16px;
						font-size: 14.5px;
						font-weight: normal;
						font-style: normal;
						font-stretch: normal;
						line-height: 1.25;
						letter-spacing: normal;
						color: #333333;padding-top:0;margin-bottom:0
						}
						.desktopHide {
							display: none;
						}
						.desktopHide1 {
							display: none;
						}
						.im{
						color: #333333 !important;
						}
				</style>
				<xsl:text disable-output-escaping="yes">
					<![CDATA[<META http-equiv="Content-Type" content="text/html; charset=ascii"></META>]]>
					<![CDATA[<meta name="x-apple-disable-message-reformatting" ></meta>]]>
					<![CDATA[<meta name="viewport" content="width=device-width, initial-scale=1.0"></meta>]]>
				</xsl:text>
			</head>
			<body link="#d8d8d8" style="margin:0;padding:0">
				<xsl:variable name="format" select="translate(/Order/OrderLines/OrderLine/LineOverallTotals/@UnitPrice, '123456789', '000000000')" />
				<div class="MainDiv" style="width:810px;margin: auto;">
					<table class="AcadLogoTab" style="margin-left:50px;" width="90%" border="0" cellspacing="0">
						<tr>
							<td>
								<br/>
							</td>
						</tr>
						<tr>
							<td class="">
								<a>
									<xsl:attribute name="href"> 
										<xsl:value-of select="$URL_AcademyHomePage" /> 
									</xsl:attribute>
									<img class="AcadLogoSize" border="0" alt="" height="auto" width="235">
										<xsl:attribute name="src"> 
											<xsl:value-of select="$IMG_AcademyLOGO" /> 
										</xsl:attribute>
									</img>
								</a>
							</td>
						</tr>
						<tr>
							<td class="HideLabel">
								<br/>
							</td>
						</tr>
						<tr>
							<td class="HideLabel">
								<a style="color: #00008b;text-decoration: none !important;">
									<xsl:attribute name="href"> 
										<xsl:value-of select="$URL_CLOTHING" /> 
									</xsl:attribute>
									<font style="font-size: 14px;color:gray;font-family: Verdana;">CLOTHING</font>
								</a>
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
								<a style="color: #00008b;text-decoration: none !important;">
									<xsl:attribute name="href"> 
										<xsl:value-of select="$URL_FOOTWEAR" /> 
									</xsl:attribute>
									<!-- OMNI-6211 Pick Up Confirmation Email - START-->
									<font style="font-size: 14px;color:gray;font-family: Verdana;">SHOES + BOOTS</font>
									<!-- OMNI-6211 Pick Up Confirmation Email - END-->
								</a>
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
								<a style="color: #00008b;text-decoration: none !important;">
									<xsl:attribute name="href"> 
										<xsl:value-of select="$URL_SPORTS" /> 
									</xsl:attribute>
									<font style="font-size: 14px;color:gray;font-family: Verdana;">SPORTS</font>
								</a>
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
								<a style="color: #00008b;text-decoration: none !important;">
									<xsl:attribute name="href"> 
										<xsl:value-of select="$URL_OUTDOORS" /> 
									</xsl:attribute>
									<font style="font-size: 14px;color:gray;font-family: Verdana;">OUTDOORS</font>
								</a>
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
								<a style="color: #00008b;text-decoration: none !important;">
									<xsl:attribute name="href"> 
										<xsl:value-of select="$URL_ACCESSORIES" /> 
									</xsl:attribute>
									<font style="font-size: 14px;color:gray;font-family: Verdana;">ACCESSORIES</font>
								</a>
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
								<a style="color: #00008b;text-decoration: none !important;">
									<xsl:attribute name="href"> 
										<xsl:value-of select="$URL_HOTDEALS" /> 
									</xsl:attribute>
									<font style="font-size: 14px;color:gray;font-family: Verdana;">
										<nobr>HOT DEALS</nobr>
									</font>
								</a>
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
								<a style="color: #00008b;text-decoration: none !important;">
									<xsl:attribute name="href"> 
										<xsl:value-of select="$URL_CLEARANCE" /> 
									</xsl:attribute>
									<font style="font-size: 14px;color:gray;font-family: Verdana;">CLEARANCE</font>
								</a>
							</td>
						</tr>
					</table>
					<table class="SlideBarWidth" style="margin-left:10px;" border="0" width="95%" cellpadding="0" cellspacing="0">
						<tr>
							<td style="background:none; border-bottom: 2px solid #d8d8d8; height:1px; width:100%; margin:0px 0px 0px 0px;">
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
							</td>
						</tr>
					</table>
					<br/>
					<table class="HeaderTab HeaderPos" style="width:80%;margin-left:50px;" border="0">
						<tr>
							<td class="HeaderPos">
								<font style="color: #333333 !important;" class="HeaderFont" size="8" face="Impact">
									<b>YOU'RE ALL SET!</b>
								</font>
							</td>
						</tr>
						<tr>
							<td class="HeaderPos" style="text-align: justify;text-justify: inter-word;">
								<font class="font" id="text">
									<xsl:value-of select="/Order/@EmailText" />
								</font>
							</td>
						</tr>
					</table>
					<!-- 
					Changes to remove Survey Section BOPIS-1884
					<table class="SlideBarWidth" style="margin-left:50px;" border="0" width="80%" cellpadding="0" cellspacing="0">
						<tr>
							<td style="background:none; border-bottom: 2px solid #d8d8d8; height:1px; width:100%; margin:0px 0px 0px 0px;">
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
							</td>
						</tr>
					</table>
					<br/>
					<table class="HeaderTab HeaderPos" style="width:80%;margin-left:50px;" border="0">
						<tr>
							<td class="HeaderPos" style="height:25px;">
								<font class="font" id="text">Thanks for shopping with us! We'd love to hear your feedback.</font>
							</td>
						</tr>
						<tr>
							<td class="HeaderPos" style="height:75px;" align="center">
								<a style="color: white;text-decoration: none !important;">
									<xsl:attribute name="href"> 
										<xsl:value-of select="$URL_TakeSurvey" /> 
									</xsl:attribute>
									<img>
										<xsl:attribute name="src"> 
											<xsl:value-of select="$IMG_TakeSurvey" /> 
										</xsl:attribute>
									</img>
								</a>
							</td>
						</tr>
					</table>
					-->					
					<br/>
					<table class="SlideBarWidth" style="margin-left:50px;" border="0" width="80%" cellpadding="0" cellspacing="0">
						<tr>
							<td style="background:none; border-bottom: 1px solid #d8d8d8; height:1px; width:100%; margin:0px 0px 0px 0px;"/>
						</tr>
					</table>
					<br/>
					<table class="OrderNoTab OrderNoPos" style="margin-left:50px;" border="0">
						<tr class="Block">
							<td class="OrderNoTd">
								<font size="6" color="black" class="OrderNoFont" face="Impact">
									<b>ORDER #: <xsl:value-of select="/Order/@OrderNo" />
									</b>
								</font>
							</td>
							<td class="HideLabel"/>
							<td class="HideLabel"/>
							<td class="HideLabel"/>
							<td class="HideLabel"/>
							<td class="HideLabel"/>
							<td class="HideLabel"/>
							<td class="InlineBlock" valign="bottom">
						<!-- OMNI-6211 Pick Up Confirmation Email - START-->
						<!--		<xsl:variable name="Mon">
									<xsl:value-of select="substring(/Order/@OrderDate,6,2)" />
								</xsl:variable>
								<xsl:variable name="Date">
									<xsl:value-of select="substring(/Order/@OrderDate,9,2)" />
								</xsl:variable>
								<xsl:variable name="Year">
									<xsl:value-of select="substring(/Order/@OrderDate,3,2)" />
								</xsl:variable>
								<xsl:choose>
									<xsl:when test="starts-with($Mon, '0') and starts-with($Date, '0')">
										<xsl:variable name="Month">
											<xsl:value-of select="substring(/Order/@OrderDate,7,1)" />
										</xsl:variable>
										<xsl:variable name="Dates">
											<xsl:value-of select="substring(/Order/@OrderDate,10,1)" />
										</xsl:variable>	
										<font class="font" size="3" id="text" color="black"> Order Date: <xsl:value-of select="concat($Month,'/', $Dates, '/', $Year)" />
										</font>
									</xsl:when>
									<xsl:when test="starts-with($Mon, '0') and not(starts-with($Date, '0'))">
										<xsl:variable name="Month">
											<xsl:value-of select="substring(/Order/@OrderDate,7,1)" />
										</xsl:variable>
										<font class="font" size="3" id="text" color="black"> Order Date: <xsl:value-of select="concat($Month,'/', $Date, '/', $Year)" />
										</font>
									</xsl:when>
									<xsl:when test="starts-with($Date, '0') and not(starts-with($Mon, '0'))">
										<xsl:variable name="Dates">
											<xsl:value-of select="substring(/Order/@OrderDate,10,1)" />
										</xsl:variable>
										<font class="font" size="3" id="text" color="black"> Order Date: <xsl:value-of select="concat($Mon,'/', $Dates, '/', $Year)" />
										</font>
									</xsl:when>
									<xsl:otherwise>
										<font class="font" size="3" id="text" color="black"> Order Date: <xsl:value-of select="concat($Mon,'/', $Date, '/', $Year)" />
										</font>
									</xsl:otherwise>
								</xsl:choose>  -->
					<!-- OMNI-6211 Pick Up Confirmation Email - END-->
							</td>
							<td class="HideLabel"/>
						</tr>
					</table>
					<br/>
					<table class="PickupTab PickupPos" style="margin-left:50px;" border="0">
						<tr>
							<td class="HideLabel" style="width:25%;" align="left">
								<img width="auto" height="auto">
									<xsl:attribute name="src"> 
										<xsl:value-of select="$IMG_HOME" /> 
									</xsl:attribute>
								</img>
							</td>
							<td>
								<font class="font" size="4" id="text" color="black">
									<font class="LinePos" style="margin-top:100px;border-bottom: 3px solid;border-bottom-color:#0055a6;padding-bottom: 5px;">PICK</font> UP INFORMATION</font>
							</td>
						</tr>
					</table>	
					<table class="PickupPos AddressTab" border="0" style="margin-left:110px">
						<tr>
							<td class="Block AddressPos" valign="top" width="50%">
								<font class="font" style="line-height:1.5;" id="text" size="4">
									<b>Pick Up Date</b>
									<br/>
									<!-- Start : OMNI-9414 : Multiple emails for STS and BOPIS -->
									<xsl:variable name="pickDate" select="/Order/Shipments/Shipment[@ShipmentKey=$varCurrentShipmentKey]/ShipmentStatusAudits/ShipmentStatusAudit[@NewStatus='1400']/@NewStatusDate" />
									<!-- End : OMNI-9414 : Multiple emails for STS and BOPIS -->
									<xsl:variable name="Mon1">
										<xsl:value-of select="substring($pickDate,6,2)" />
									</xsl:variable>
									<xsl:variable name="Date1">
										<xsl:value-of select="substring($pickDate,9,2)" />
									</xsl:variable>
									<xsl:variable name="Year1">
										<xsl:value-of select="substring($pickDate,3,2)" />
									</xsl:variable>
									<xsl:choose>
										<xsl:when test="starts-with($Mon1, '0') and starts-with($Date1, '0')">
											<xsl:variable name="Month1">
												<xsl:value-of select="substring($pickDate,7,1)" />
											</xsl:variable>
											<xsl:variable name="Dates1">
												<xsl:value-of select="substring($pickDate,10,1)" />
											</xsl:variable>
											<xsl:value-of select="concat($Month1,'/', $Dates1, '/', $Year1)" />
										</xsl:when>
										<xsl:when test="starts-with($Mon1, '0') and not(starts-with($Date1, '0'))">
											<xsl:variable name="Month1">
												<xsl:value-of select="substring($pickDate,7,1)" />
											</xsl:variable>
											<xsl:value-of select="concat($Month1,'/', $Date1, '/', $Year1)" />
										</xsl:when>
										<xsl:when test="starts-with($Date1, '0') and not(starts-with($Mon1, '0'))">
											<xsl:variable name="Dates1">
												<xsl:value-of select="substring($pickDate,10,1)" />
											</xsl:variable>
											<xsl:value-of select="concat($Mon1,'/', $Dates1, '/', $Year1)" />
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="concat($Mon1,'/', $Date1, '/', $Year1)" />
										</xsl:otherwise>
									</xsl:choose>
				at 
									<xsl:if test="substring($pickDate,12,2) &gt; 12">
										<xsl:variable name="pHour" select="substring($pickDate,12,2) - 12" />
										<xsl:variable name="pMin" select="substring($pickDate,15,2)" />
										<xsl:value-of select="concat($pHour,':',$pMin,'pm')" />
									</xsl:if>
									<xsl:if test="substring($pickDate,12,2) &lt; 12">
										<xsl:variable name="pHour" select="substring($pickDate,12,2)" />
										<xsl:variable name="pMin" select="substring($pickDate,15,2)" />
										<xsl:value-of select="concat($pHour,':',$pMin,'am')" />
									</xsl:if>

									<xsl:if test="substring($pickDate,12,2) = 12">
										<xsl:variable name="pHour" select="substring($pickDate,12,2)" />
										<xsl:variable name="pMin" select="substring($pickDate,15,2)" />
										<xsl:value-of select="concat($pHour,':',$pMin,'pm')" />
									</xsl:if>
									by
									<br/>
									<xsl:choose>
										<xsl:when test="/Order/Shipments/Shipment/Extn/@ExtnShipmentPickedBy='Primary'">
											<xsl:value-of select="concat(/Order/PersonInfoBillTo/@FirstName,' ',/Order/PersonInfoBillTo/@LastName)" />
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="concat(/Order/PersonInfoMarkFor/@FirstName,' ',/Order/PersonInfoMarkFor/@LastName)" />
										</xsl:otherwise>
									</xsl:choose>
								</font>
							</td>
							<td class="AddressPos InlineBlock">
								<font class="font" style="line-height:1.5;" id="text" size="4">
									<b>
										<xsl:value-of select="/Order/OrderLines/OrderLine[@DeliveryMethod='PICK']/PersonInfoShipTo/@LastName" />
									</b>
									<br/>
									<xsl:if test="/Order/OrderLines/OrderLine[@DeliveryMethod='PICK']/PersonInfoShipTo/@AddressLine1 != ''">
										<xsl:value-of select="/Order/OrderLines/OrderLine[@DeliveryMethod='PICK']/PersonInfoShipTo/@AddressLine1" />
									</xsl:if>
									<br/>
									<xsl:value-of select="concat(/Order/OrderLines/OrderLine[@DeliveryMethod='PICK']/PersonInfoShipTo/@City,', ',/Order/OrderLines/OrderLine[@DeliveryMethod='PICK']/PersonInfoShipTo/@State,' ',/Order/OrderLines/OrderLine[@DeliveryMethod='PICK']/PersonInfoShipTo/@ZipCode)" />
									<br/>
									<xsl:value-of select="/Order/OrderLines/OrderLine[@DeliveryMethod='PICK']/PersonInfoShipTo/@DayPhone" />
									<br/>
									<br/>
									<b>
										<xsl:value-of select="concat(/Order/PersonInfoBillTo/@FirstName,' ',/Order/PersonInfoBillTo/@LastName)" />
									</b>
									<br/>
									<xsl:value-of select="/Order/PersonInfoBillTo/@EMailID" />				
								</font>
							</td>
						</tr>
					</table>
					<table class="SlideBarWidth" style="margin-left:50px;" border="0" width="80%" cellpadding="0" cellspacing="0">
						<tr>
							<td style="background:none; border-bottom: 2px solid #d8d8d8; height:1px; width:100%; margin:0px 0px 0px 0px;">
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
							</td>
						</tr>
					</table>
					<br/>
					<table class="ItemTab ItemPos" border="0" style="margin-left:50px">
						<tr>
							<td class="ItemTab ItemPos" >
								<font class="ItemFont" id="text" size="4" color="black">ITEMS FOR PICK UP</font>
							</td>
						</tr>
					</table>

					<table border="0" class="ItemTab ItemPos" style="width:75%;margin-left:50px;">
						<!-- Start : OMNI-9414 : Multiple emails for STS and BOPIS -->
						<xsl:for-each select="/Order/Shipments/Shipment[@ShipmentKey=$varCurrentShipmentKey]/ShipmentLines/ShipmentLine">
						<!-- End : OMNI-9414 : Multiple emails for STS and BOPIS -->
							<xsl:variable name="orderLineKey" select="./@OrderLineKey" />
							<xsl:variable name="ShipQty" select="./@Quantity" />
							<xsl:for-each select="/Order/OrderLines/OrderLine">
								<xsl:variable name="OrderQty" select="./@OrderedQty" />
								<xsl:if test="./@OrderLineKey=$orderLineKey and $OrderQty &gt; 0">
									<xsl:variable name="ImageLoc" select="./ItemDetails/PrimaryInformation/@ImageLocation" />
									<xsl:variable name="ImageID" select="./ItemDetails/PrimaryInformation/@ImageID" />
									<tr>
										<td class="OrderNoTd Block" style="width:20%;" align="middle">
											<img class="ItemImageSize" onerror="this.src='https://content.academy.com/weblib/images/favicon/touch-icon-60px.png'" alt="Image not Available" width="125" height="auto">
												<xsl:attribute name="src">
													<xsl:value-of select="concat($ImageLoc, $ImageID)" />
												</xsl:attribute>
											</img> 
										</td>
										<td class="OrderNoTd InlineBlock CenterAlign" style="width:56%">
											<font class="ItemFont" id="text">
												<xsl:value-of select="ItemDetails/PrimaryInformation/@Description" />
											</font>
											<br/>
											<font class="ItemFont" id="text">
												<b>SKU:
													<xsl:text disable-output-escaping="yes">
														<![CDATA[&nbsp;]]>
													</xsl:text>
												</b>
												<xsl:value-of select="ItemDetails/@ItemID" />
											</font>
											<br/>
											<font class="ItemFont" id="text">
												<b>Quantity:
													<xsl:text disable-output-escaping="yes">
														<![CDATA[&nbsp;]]>
													</xsl:text>
												</b>
												<xsl:value-of select="format-number($ShipQty, '0')" />
											</font>
										</td>
										<td class="HideLabel" align="right" valign="center">
											<font id="text">
												$ <xsl:value-of select="LineOverallTotals/@UnitPrice" />
											</font>
										</td>
									</tr>
									<tr>
										<td>
											<br/>
											<br class="desktopHide1" />
											<br class="desktopHide1" />
										</td>
									</tr>
								</xsl:if>
							</xsl:for-each>
						</xsl:for-each>
					</table>
					<table class="SlideBarWidth" style="margin-left:50px;" border="0" width="80%" cellpadding="0" cellspacing="0">
						<tr>
							<td style="background:none; border-bottom: 2px solid #d8d8d8; height:1px; width:100%; margin:0px 0px 0px 0px;">
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
							</td>
						</tr>
					</table>
					<br/>
					<table class="TotalTab TotalPos" style="width:80%;margin-left:50px;border-collapse: collapse;" border="0">
						<tr>
							<td class="HideLabel"/>
							<td class="TotalAlign" style="padding-left:50px;">
								<font size="4" class="ItemFont" style="font-family: Verdana;">Subtotal:</font>
							</td>
							<td class="TotalValueAlign" align="right">
								<font class="ItemFont" size="4" style="font-size: 23px;font-family: Verdana;padding-left:50px;">
									<!-- Start : OMNI-9414 : Multiple emails for STS and BOPIS -->
									<xsl:choose>
										<xsl:when test="number(sum(/Order/Shipments/Shipment[@ShipmentKey=$varCurrentShipmentKey]/ShipmentLines/ShipmentLine/OrderLine[@DeliveryMethod='PICK' and @OrderedQty &gt; 0]/LineOverallTotals/@ExtendedPrice)) &lt; '1'">
											<xsl:value-of select="concat('$','0',format-number(sum(/Order/Shipments/Shipment[@ShipmentKey=$varCurrentShipmentKey]/ShipmentLines/ShipmentLine/OrderLine[@DeliveryMethod='PICK' and @OrderedQty &gt; 0]/LineOverallTotals/@ExtendedPrice),'#,###.00'))" />
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="concat('$',format-number(sum(/Order/Shipments/Shipment[@ShipmentKey=$varCurrentShipmentKey]/ShipmentLines/ShipmentLine/OrderLine[@DeliveryMethod='PICK' and
									@OrderedQty &gt; 0]/LineOverallTotals/@ExtendedPrice),'#,###.00'))"/>
										</xsl:otherwise>
									</xsl:choose> 
									<!-- End : OMNI-9414 : Multiple emails for STS and BOPIS -->
								</font>
							</td>
						</tr>
						<!-- Start : OMNI-9414 : Multiple emails for STS and BOPIS -->
						<xsl:if test="number(sum(/Order/Shipments/Shipment[@ShipmentKey=$varCurrentShipmentKey]/ShipmentLines/ShipmentLine/OrderLine[@DeliveryMethod='PICK' and @OrderedQty &gt; 0]/LineOverallTotals/@Discount)) != '0'">
							<tr>
								<td class="HideLabel"/>
								<td class="TotalAlign" style="padding-left:50px;">
									<font class="ItemFont" size="4" style="font-family: Verdana;" color="green">
									Discount:
									</font>
								</td>
								<td class="TotalValueAlign" style="padding-left:40px;" align="right">
									<font class="ItemFont" style="font-family: Verdana;font-size: 23px;" color="green">
										<xsl:choose>
											<xsl:when test="number(sum(/Order/Shipments/Shipment[@ShipmentKey=$varCurrentShipmentKey]/ShipmentLines/ShipmentLine/OrderLine[@DeliveryMethod='PICK' and @OrderedQty &gt; 0]/LineOverallTotals/@Discount)) &lt; '1'">
												<xsl:value-of select="concat('-$','0',format-number(sum(/Order/Shipments/Shipment[@ShipmentKey=$varCurrentShipmentKey]/ShipmentLines/ShipmentLine/OrderLine[@DeliveryMethod='PICK' and @OrderedQty &gt; 0]/LineOverallTotals/@Discount),'#,###.00'))" />
											</xsl:when>
											<xsl:otherwise>
												<xsl:value-of select="concat('-$',format-number(sum(/Order/Shipments/Shipment[@ShipmentKey=$varCurrentShipmentKey]/ShipmentLines/ShipmentLine/OrderLine[@DeliveryMethod='PICK' and
									@OrderedQty &gt; 0]/LineOverallTotals/@Discount),'#,###.00'))"/>
											</xsl:otherwise>
										</xsl:choose>
									</font>
								</td>
							</tr>
						</xsl:if>
						<!-- End : OMNI-9414 : Multiple emails for STS and BOPIS -->
						<tr>
							<td class="HideLabel"/>
							<td class="TotalAlign" style="padding-left:50px;">
								<font class="ItemFont" size="4" style="font-family: Verdana;">Store Pick Up:</font>
							</td>
							<td class="TotalValueAlign" align="right" style="padding-left:65px;">
								<font class="ItemFont" size="4" style="font-size: 23px;font-family: Verdana;">
									<b>
										FREE</b>
								</font>
							</td>
						</tr>
						<tr>
							<td class="HideLabel" style="border-bottom: 2px solid #d8d8d8"/>
							<td class="TotalAlign" style="padding-left:50px;border-bottom: 2px solid #d8d8d8;height:40px;" valign="top">
								<font class="ItemFont" size="4" style="font-family: Verdana;">Taxes:</font>
							</td>
							<td class="TotalValueAlign" style="border-bottom: 2px solid #d8d8d8" align="right" valign="top">
								<font class="ItemFont" size="4" style="font-size: 23px;font-family: Verdana;padding-left:50px;">
									<!-- Start : OMNI-9414 : Multiple emails for STS and BOPIS -->
									<xsl:choose>
										<xsl:when test="number(sum(/Order/Shipments/Shipment[@ShipmentKey=$varCurrentShipmentKey]/ShipmentLines/ShipmentLine/OrderLine[@DeliveryMethod='PICK' and @OrderedQty &gt; 0]/LineOverallTotals/@Tax)) &lt; '1'">
											<xsl:value-of select="concat('$','0',format-number(sum(/Order/OrderLines/OrderLine[@DeliveryMethod='PICK' and @OrderedQty &gt; 0]/LineOverallTotals/@Tax),'#,###.00'))" />
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="concat('$',format-number(sum(/Order/Shipments/Shipment[@ShipmentKey=$varCurrentShipmentKey]/ShipmentLines/ShipmentLine/OrderLine[@DeliveryMethod='PICK' and
									@OrderedQty &gt; 0]/LineOverallTotals/@Tax),'#,###.00'))"/>
										</xsl:otherwise>
									</xsl:choose>
									<!-- End : OMNI-9414 : Multiple emails for STS and BOPIS -->
								</font>
							</td>
						</tr>
						<!-- Start : OMNI-9414 : Multiple emails for STS and BOPIS -->
						<xsl:variable name="stotal" select="sum(/Order/Shipments/Shipment[@ShipmentKey=$varCurrentShipmentKey]/ShipmentLines/ShipmentLine/OrderLine[@DeliveryMethod='PICK' and @OrderedQty &gt; 0]/LineOverallTotals/@ExtendedPrice)" />
						<xsl:variable name="Discount" select="sum(/Order/Shipments/Shipment[@ShipmentKey=$varCurrentShipmentKey]/ShipmentLines/ShipmentLine/OrderLine[@DeliveryMethod='PICK' and @OrderedQty &gt; 0]/LineOverallTotals/@Discount)" />
						<xsl:variable name="tax" select="sum(/Order/Shipments/Shipment[@ShipmentKey=$varCurrentShipmentKey]/ShipmentLines/ShipmentLine/OrderLine[@DeliveryMethod='PICK' and @OrderedQty &gt; 0]/LineOverallTotals/@Tax)" />
						<!-- End : OMNI-9414 : Multiple emails for STS and BOPIS -->
						<xsl:variable name="total" select="$stotal - $Discount + $tax" />
						<tr>
							<td class="mobileHide" style="height:50px;" valign="bottom">
								<font id="text">
									<a style="color: #0055a6;text-decoration: none !important;">
										<xsl:attribute name="href"> 
											<xsl:value-of select="$URL_ViewOrderDetails" /> 
										</xsl:attribute>
									View Order Details
									</a>
								</font>
							</td>
							<td class="TotalAlign" style="padding-left:50px;" valign="bottom" width="34%">
								<font class="ItemFont" size="4" style="font-family: Verdana;">
									<b>Total:</b>
								</font>
							</td>
							<td valign="bottom" class="TotalValueAlign" align="right" style="padding-left:50px;">
								<font class="ItemFont" size="4" style="font-size: 23px;font-family: Verdana;">
									<b>
										<xsl:choose>
											<xsl:when test="number($total) &lt; '1'">
												<xsl:value-of select="concat('$','0',format-number($total,'#,###.00'))" />
											</xsl:when>
											<xsl:otherwise>
												<xsl:value-of select="concat('$',format-number($total,'#,###.00'))"/>
											</xsl:otherwise>
										</xsl:choose>
									</b>
								</font>
							</td>
						</tr>
					</table>

					<hr style="border-top: 1px solid #d8d8d8;margin-left: 25px !important;background:none; border-bottom: 1px solid #d8d8d8; height:0px; width:90%; margin:0px 0px 0px 0px;" class="desktopHide1" />
					<!-- Mobile View VOD - Start -->
					<table class="desktopHide1 ItemPos">
						<tr> 
							<td style="padding-top: 25px !important" >
								<font class="FooterFont" id="text">
									<a style="color: #0055a6;text-decoration: none !important;">
										<xsl:attribute name="href"> 
											<xsl:value-of select="$URL_ViewOrderDetails" /> 
										</xsl:attribute>
									View Order Details
									</a>
								</font>
							</td>
						</tr>
					</table>
					<!-- Mobile View VOD - End -->

					<table class="SlideBarWidth" style="margin-left:50px;" border="0" width="80%" cellpadding="0" cellspacing="0">
						<tr>
							<td style="background:none; border-bottom: 2px solid #d8d8d8; height:1px; width:100%; margin:0px 0px 0px 0px;">
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
							</td>
						</tr>
					</table>
					<br/>
					<!-- Start : OMNI-9414 : Multiple emails for STS and BOPIS -->
					<xsl:variable name="ShipKey" select="/Order/Shipments/Shipment[@ShipmentKey=$varCurrentShipmentKey]/@ShipmentKey" />
					<!-- End : OMNI-9414 : Multiple emails for STS and BOPIS -->
					<xsl:variable name="InvoiceNo" select="/Order/OrderInvoiceList/OrderInvoice[@ShipmentKey=$ShipKey and @InvoiceType='SHIPMENT']/@InvoiceNo" />
					<table class="BarcodePos vodTab" style="margin-left:50px;width:75%" border="0">
						<tr>
							<td class="BarcodePos" align="center">
								<xsl:variable name="OrderNo" select="/Order/@OrderNo" />
								<img class="BarcodeSize" height="100" width="300">
									<xsl:attribute name="src"> 
										<!--<xsl:value-of select="emailformatters:genBarCodetag($InvoiceNo,'code128','png')" />-->
										<!--BOPIS-1451 - Start -->
										<xsl:value-of select="concat($URL_BarcodeAPI,$InvoiceNo,$URL_BarcodeAPI1)" />
										<!--BOPIS-1451 - End -->
									</xsl:attribute> 
								</img>
							</td>
						</tr>
					</table>
					<table class="SlideBarWidth" style="margin-left:50px;" border="0" width="80%" cellpadding="0" cellspacing="0">
						<tr>
							<td style="background:none; border-bottom: 2px solid #d8d8d8; height:1px; width:100%; margin:0px 0px 0px 0px;">
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
							</td>
						</tr>
					</table>
					<br/>
					<!-- OMNI-6211 Pick Up Confirmation Email - START-->
					<table class="vodPos vodTab" style="margin-left:50px;width:90%" border="0"  cellpadding="0" cellspacing="0">
					<!-- OMNI-6211 Pick Up Confirmation Email - END-->
						<tr>
							<td class="HeaderPos">
								<font class="FooterFont" id="text">Note that the total includes only the items that were picked up in the store.<br/>
									<a style="color: #0055a6;text-decoration: none !important;">
										<xsl:attribute name="href"> 
											<xsl:value-of
											select="$URL_ReturnOrderInstructions" /> 
										</xsl:attribute>
									Return Order Instructions</a>
								</font>
						<!-- OMNI-6211 Pick Up Confirmation Email - START-->
								<br/>
							<br/>
							</td>
						</tr>
						<tr>
							<td class="ReturnPos1" style="padding-top:25px;padding-left:40px;background-color:#0055a6">
								<font size="7" color="white" face="Impact">
									<b>
											QUESTIONS ABOUT
										<br class="desktopHide1" />
											YOUR ORDER?
									</b>
								</font>
							</td>
						</tr>
						<tr>
							<td style="background-color:#0055a6">
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
							</td>
						</tr> 
						<tr>
							<td class="ReturnPos1" style="text-align:center;background-color:#0055a6;height:85px;" valign="top">
								<a>
									<xsl:attribute name="href"> 
										<xsl:value-of select="$URL_GETANSWER" /> 
									</xsl:attribute>
									<!--<img>
										<xsl:attribute name="src"> 
											<xsl:value-of select="$IMG_GetAnswer" /> 
										</xsl:attribute>
									</img>-->
									<button style="outline-style: none;height:63px;width:250px; border-radius:50px;background-color:white;border: 3px solid white;cursor: pointer;">
										<font class="buttonFont" style="font-family: Roboto,Helvetica, Arial, sans-serif;font-size:17px;color:#333333;">
											<b>GET ANSWERS</b>
										</font>
									</button>
								</a>
								<br class="desktopHide1"/>
								<br class="desktopHide1"/>
								<a>
									<xsl:attribute name="href"> 
										<xsl:value-of select="$URL_OrdPickFAQ" /> 
									</xsl:attribute>
									<!--<img class="noPadding" style="padding-left:40px;">
										<xsl:attribute name="src"> 
											<xsl:value-of select="$IMG_OrdPickFAQ" /> 
										</xsl:attribute>
									</img>-->
									<button class="ReturnPos1" style="margin-left:50px;outline-style: none;height:63px;width:250px; border-radius:50px;background-color:#0055a6;border: 3px solid white;cursor: pointer;">
										<font class="buttonFont" style="font-family: Roboto,Helvetica, Arial, sans-serif;font-size:17px;color:white;">
											<b>Customer Care</b>
										</font>
									</button>
								</a>
								<br/>
								<br/>
							</td>
						</tr>
					</table>		
				<!-- OMNI-6211 Pick Up Confirmation Email - START-->
					<br/>
					<table class="HeaderTab HeaderPos" style="width:95%;margin-left:50px;" border="0">
						<tr/>
						<tr/>
						<tr/>
						<tr/>
						<tr/>
						<tr/>
						<tr/>
						<tr/>
						<tr/>
						<tr>
							<td class="OrderNoTd Block" style="width:40%;">
								<font class="FooterFont" id="text1">Need Help? <a style="color: #0055a6;text-decoration: none !important;">
										<xsl:attribute name="href"> 
											<xsl:value-of select="$URL_LiveChat" /> 
										</xsl:attribute>
								Live Chat</a> with our Customer Care,<br/>
								or call us at 1-888-922-2336.<br/>
									<br/>
				Academy Sports + Outdoors<br/>
				1800 N Mason Rd<br/>
				Katy, TX 77449<br/>
									<br/>
									<a style="color: #0055a6;text-decoration: none !important;">
										<xsl:attribute name="href"> 
											<xsl:value-of select="$URL_UpdateProfile" /> 
										</xsl:attribute>
									Update Profile</a> | <a style="color: #0055a6;text-decoration: none !important;">
										<xsl:attribute name="href"> 
											<xsl:value-of select="$URL_PrivacyPolicy" /> 
										</xsl:attribute>
									Privacy Policy</a>
								</font>
							</td>
							<td class="InlineBlock" align="center" valign="top">
								<a target="_blank" title="follow me on Facebook"
									style="color: #00008b">
									<xsl:attribute name="href"> 
										<xsl:value-of select="$URL_Facebook" /> 
									</xsl:attribute>
									<img alt="Academy Facebook" height="35" border="0"
										width="35">
										<xsl:attribute name="src"> 
											<xsl:value-of select="$IMG_Facebook" /> 
										</xsl:attribute>
									</img>
								</a>
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
								<a target="_blank" title="follow me on twitter"
									style="color: #00008b">
									<xsl:attribute name="href"> 
										<xsl:value-of select="$URL_Twitter" /> 
									</xsl:attribute>
									<img alt="Academy Twitter" height="35" border="0"
										width="35">
										<xsl:attribute name="src"> 
											<xsl:value-of select="$IMG_Twitter" /> 
										</xsl:attribute>
									</img>
								</a>
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
								<a target="_blank" title="follow me on Pinterest"
									style="color: #00008b">
									<xsl:attribute name="href"> 
										<xsl:value-of select="$URL_PinRest" /> 
									</xsl:attribute>
									<img alt="Academy Pinterest" height="35" border="0"
										width="35">
										<xsl:attribute name="src"> 
											<xsl:value-of select="$IMG_Pinrest" /> 
										</xsl:attribute>
									</img>
								</a>
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
								<a target="_blank" title="follow me on Youtube"
									style="color: #00008b">
									<xsl:attribute name="href"> 
										<xsl:value-of select="$URL_YouTube" /> 
									</xsl:attribute>
									<img alt="Academy Youtube" height="35" border="0"
										width="35">
										<xsl:attribute name="src"> 
											<xsl:value-of select="$IMG_Youtube" /> 
										</xsl:attribute>
									</img>
								</a>
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
								<a target="_blank" title="follow me on Instagram"
									style="color: #00008b">
									<xsl:attribute name="href"> 
										<xsl:value-of select="$URL_Instagram" /> 
									</xsl:attribute>
									<img alt="Academy Instagram" height="35" border="0"
										width="35">
										<xsl:attribute name="src"> 
											<xsl:value-of select="$IMG_Instagram" /> 
										</xsl:attribute>
									</img>
								</a>
							</td>
						</tr>
					</table>
				</div>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>