<!--SOF :: Start WN-1814 Ready for Customer Pickup Email -->
<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:lxslt="http://xml.apache.org/xslt" version="1.0"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:fopUtil="java://com.yantra.pca.ycd.fop.YCDFOPUtils"
	xmlns:emailformatters="java://com.academy.ecommerce.sterling.bopis.print.utils.AcademyPrintUtils">
	<xsl:output method="html" />
	<xsl:template match="/">
		<xsl:variable name="currShipKey" select="/Order/@CurrentShipmentKey" />
		<xsl:variable name="IconImagePath" select="/Order/@IconImagePath" />
		<xsl:variable name="EmailType" select="/Order/@EmailType" />
		<xsl:variable name="OrdNo" select="/Order/@OrderNo" />
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
		<!--BOPIS-1451 - Start -->
		<xsl:variable name="URL_BarcodeAPI" select="/Order/@URL_BarcodeAPI" />
		<xsl:variable name="URL_BarcodeAPI1" select="/Order/@URL_BarcodeAPI1" />
		<!--BOPIS-1451 - End -->
		<!--OMNI- 5184, 5185 Curbside: Order ready for pick up Email, Curbside:  Order ready for pick up Reminder Email - START --> 
		<xsl:variable name="IsCurbSidePickupEnabled" select="/Order/@IsCurbSidePickupEnabled" />
		<!--OMNI- 5184, 5185 Curbside: Order ready for pick up Email, Curbside:  Order ready for pick up Reminder Email - END --> 
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
	.ReturnPos{
		padding-left:0px !important;
		padding-top: 10px !important;
		padding-bottom: 10px !important;
	}
	.ReturnPos1{
		padding-left: 0 !important;
		margin-left: 0 !important;
		text-align: center !important;
	}
	.FooterFont{
		font-size: 25px !important;
	}
	.PadTop{
		padding-top : 25px;
	}
	.UnBold{
		font-weight: normal !important;
	}
	.BarcodePos{
		text-align: center;
		margin-left:25px !important;
	}
	.desktopHide1{
		display: block !important;
		width:90%;
	}
	.noPadding{
		padding-left: 0px !important;
		padding-bottom:20px !important;
	}
	.buttonFont{
			font-family: Roboto,Helvetica, Arial, sans-serif;
			font-size:21px !important;
			text-align: right !important;
			margin: 0px !important;
			padding:0px !important;
			color:#333333;
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
						#Boldtext {
							font-family:Roboto,Helvetica, Arial, sans-serif;
							padding-bottom:16px;
							font-size: 16px;
							font-style: normal;
							font-stretch: normal;
							line-height: 1.25;
							letter-spacing: normal;
							color: #333333;
							padding-top:0;
							margin-bottom:0;
							font-weight : bolder;
						}

						#text {
							font-family:Roboto,Helvetica, Arial, sans-serif;
							padding-bottom:16px;
							font-size: 16px;
							font-weight: normal;
							font-style: normal;
							font-stretch: normal;
							line-height: 1.25;
							letter-spacing: normal;
							color: #333333;
							padding-top:0;
							margin-bottom:0
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
							color: #333333;
							padding-top:0;
							margin-bottom:0
							}
						.im{
							color: #333333 !important;
							}
						.desktopHide1 {
							display: none;
							}
				</style>
				<xsl:text disable-output-escaping="yes">
					<![CDATA[<META http-equiv="Content-Type" content="text/html; charset=ascii"></META>]]>
					<![CDATA[<meta name="x-apple-disable-message-reformatting" ></meta>]]>
					<![CDATA[<meta name="viewport" content="width=device-width, initial-scale=1"></meta>]]>					
				</xsl:text>
			</head>
			<body link="gray" style="margin:0;padding:0;">
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
									<font style="font-size: 14px;color:gray;font-family: Verdana;">FOOTWEAR</font>
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
					<br />
					<table class="HeaderTab HeaderPos" style="width:80%;margin-left:50px;" border="0">
						<tr>
							<td class="HeaderPos">
								<xsl:choose>
									<xsl:when test="$EmailType = 'REMRFCP'">
										<font style="color: #333333 !important;" class="HeaderFont" size="8" face="Impact">
											<b>YOUR ORDER IS WAITING FOR YOU</b>
										</font>
									</xsl:when>
									<xsl:otherwise>
										<font style="color: #333333 !important;" class="HeaderFont" size="8" face="Impact">
											<b>YOUR ORDER IS GOOD TO GO!</b>
										</font>
									</xsl:otherwise>
								</xsl:choose>
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
					<table class="HeaderTab HeaderPos" style="margin-left:50px;width:80%;" border="0">
						<tr>
							<td>
								<!-- <font class="font" style="font-family:Roboto,Helvetica, Arial, sans-serif;color: #262626;font-weight:bold" size="5.5" face="Impact">
									<p class="CenterAlign">
										<b>STORE PICKUP INSTRUCTIONS</b>
									</p>
								</font> -->
									<!--OMNI- 5184, 5185 Curbside: Order ready for pick up Email, Curbside:  Order ready for pick up Reminder Email - START --> 
								<xsl:choose>
								<xsl:when test ="$IsCurbSidePickupEnabled = 'Y'">
								<!-- <font class="font" style="font-family:Arial, sans-serif;color: #262626;font-weight:bold" size="4" face="Impact">
								<b><h2>Optional Curbside Pickup Available At Your Store</h2></b>
								</font>
								<br></br> -->
								<font id="text" class="InstrFont">
								<p><b>IN STORE PICKUP INSTRUCTIONS</b></p>
									<ol class="InstrFont">
										<li>Bring your valid U.S. government issued ID to the store.</li>
										<p></p>
										<li>Bring your confirmation email or order number with you.</li>
									</ol>
								<br></br>
								<b>CURBSIDE PICKUP INSTRUCTIONS</b>
								<ol class="InstrFont">
										<li>Bring your valid U.S. government issued ID to the store.</li>
										<p></p>
										<li>Bring your confirmation email or order number with you.</li>
										<p></p>
										<li>Upon arrival, park in one of the designated parking spots.</li>
										<p></p>
										<li>Call the store upon arrival.</li>
										<p></p>
										<li>An Academy Team member will bring your order out to you.</li>
									</ol>
								</font>
								</xsl:when>
								<xsl:otherwise>
									<font id="text" class="InstrFont">
									<ol class="InstrFont">
										<li>Bring your confirmation, your order number, or the barcode in this email with you.</li>
										<p></p>
										<li>You will be asked to present your valid U.S. government issued ID at customer service.</li>
									</ol>
								</font>
								</xsl:otherwise>
								</xsl:choose>
									<!--OMNI- 5184, 5185 Curbside: Order ready for pick up Email, Curbside:  Order ready for pick up Reminder Email - END --> 
							</td>
						</tr>
					</table>
					<table style="margin-left:50px;" border="0" class="SlideBarWidth" width="80%" cellpadding="0" cellspacing="0">
						<tr>
							<td style="background:none; border-bottom: 2px solid #d8d8d8; height:1px; width:100%; margin:0px 0px 0px 0px;">
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
							</td>
						</tr>
					</table>
					<br/>
					<br/>
					<table class="OrderNoTab OrderNoPos" style="margin-left:50px;" border="0">
						<tr class="Block">
							<td class="OrderNoTd">
								<font size="6" color="black" class="OrderNoFont" face="Impact">
									<b>
										ORDER #:
										<xsl:value-of select="/Order/@OrderNo" />
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
								<xsl:variable name="Mon">
									<xsl:value-of select="substring(/Order/@OrderDate,6,2)" />
								</xsl:variable>
								<xsl:variable name="Date">
									<xsl:value-of select="substring(/Order/@OrderDate,9,2)" />
								</xsl:variable>

								<xsl:variable name="Year">
									<xsl:value-of select="substring(/Order/@OrderDate,3,2)" />
								</xsl:variable>
								<xsl:choose>
									<xsl:when
										test="starts-with($Mon, '0') and starts-with($Date, '0')">
										<xsl:variable name="Month">
											<xsl:value-of select="substring(/Order/@OrderDate,7,1)" />
										</xsl:variable>
										<xsl:variable name="Dates">
											<xsl:value-of select="substring(/Order/@OrderDate,10,1)" />
										</xsl:variable>
										<font class="font" size="3" id="text" color="black">
											Order Date:
											<xsl:value-of select="concat($Month,'/', $Dates, '/', $Year)" />
										</font>
									</xsl:when>
									<xsl:when
										test="starts-with($Mon, '0') and not(starts-with($Date, '0'))">
										<xsl:variable name="Month">
											<xsl:value-of select="substring(/Order/@OrderDate,7,1)" />
										</xsl:variable>
										<font class="font" size="3" id="text" color="black">
											Order Date:
											<xsl:value-of select="concat($Month,'/', $Date, '/', $Year)" />
										</font>
									</xsl:when>
									<xsl:when test="starts-with($Date, '0') and not(starts-with($Mon, '0'))">
										<xsl:variable name="Dates">
											<xsl:value-of select="substring(/Order/@OrderDate,10,1)" />
										</xsl:variable>
										<font size="3" class="font" id="text" color="black">
											Order Date:
											<xsl:value-of select="concat($Mon,'/', $Dates, '/', $Year)" />
										</font>
									</xsl:when>
									<xsl:otherwise>
										<font size="3" id="text" class="font" color="black">
											Order Date:
											<xsl:value-of select="concat($Mon,'/', $Date, '/', $Year)" />
										</font>
									</xsl:otherwise>
								</xsl:choose>
							</td>
							<td class="HideLabel" />
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
								<font class="font" size="4" style="font-family: Verdana;" color="black">
									<font class="LinePos Font" style="margin-top:100px;border-bottom: 3px solid;border-bottom-color:#0055a6;padding-bottom: 5px;">PICK</font>UP INFORMATION</font>
							</td>
						</tr>
					</table>
					<xsl:variable name="currDate" select="/Order/@CurrentDate" />
					<xsl:variable name="maxPickupDate" select="/Order/@PickUpUntilDate" />
					<table class="PickupPos AddressTab" style="margin-left:50px;" border="0">
						<tr>
							<td class="Block AddressPos" style="padding-left:70px">
								<font style="line-height: 1.5;" id="text" size="4" class="font">
									<b>
													Ready for Pickup on
										<xsl:if test="starts-with(substring-after($currDate,' '),'0')">
											<xsl:value-of select="concat(substring($currDate,1,3),' ',substring($currDate,6))" />
										</xsl:if>
										<xsl:if test="not(starts-with(substring-after($currDate,' '),'0'))">
											<xsl:value-of select="$currDate" />
										</xsl:if>
									</b>
								</font>
								<br/>
								<font class="font" style="line-height: 1.5;" id="text" size="4">
												You have until
									<b>
										<xsl:if test="starts-with(substring-after($maxPickupDate,' '),'0')">
											<xsl:value-of select="concat(substring($maxPickupDate,1,3),' ',substring($maxPickupDate,6))" />
										</xsl:if>
										<xsl:if test="not(starts-with(substring-after($maxPickupDate,' '),'0'))">
											<xsl:value-of select="$maxPickupDate" />
										</xsl:if>
									</b> to
								</font>
								<br />
								<font class="font" style="line-height: 1.5;" id="text" size="4">
												pick up your order before 
								</font>
								<br />
								<font class="font" style="line-height: 1.5;" id="text" size="4">
												it is canceled.
								</font>
								<br/>
								<font class="font" style="line-height: 1.5;" id="text" size="4">
									<b>
										<xsl:value-of select="concat(/Order/PersonInfoBillTo/@FirstName,' ',/Order/PersonInfoBillTo/@LastName)" />
									</b>
								</font>
								<br/>
								<font class="font" style="line-height: 1.5;" id="text" size="4">
									<xsl:value-of select="/Order/PersonInfoBillTo/@EMailID" />
								</font>
								<br/>
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
								<br/>
								<xsl:if test="/Order/PersonInfoMarkFor/@FirstName != ''">
									<font class="font" id="text" size="4">
										<b>Alternate Pickup Person</b>
									</font>
								</xsl:if>
								<br/>
								<font class="font" style="line-height: 1.5;" id="text" size="4">
									<xsl:value-of select="concat(/Order/PersonInfoMarkFor/@FirstName,' ',/Order/PersonInfoMarkFor/@LastName)" />
								</font>
								<br/>
								<font class="font" style="line-height: 1.5;" id="text" size="4">
									<xsl:value-of select="/Order/PersonInfoMarkFor/@EMailID" />
								</font>
							</td>
							<td class="AddressPos InlineBlock PadTop" valign="top" style="padding-left:70px;">
								<font class="font" style="line-height: 1.5;" id="text" size="4">
									<b>Pickup Location</b>
								</font>
								<br/>
								<font class="font UnBold" style="line-height: 1.5;" id="Boldtext" size="4">
									<xsl:value-of select="/Order/OrderLines/OrderLine[@DeliveryMethod='PICK']/PersonInfoShipTo/@LastName" />
								</font>
								<br/>
								<font class="font" style="line-height: 1.5;" id="text" size="4">
									<xsl:value-of select="/Order/OrderLines/OrderLine[@DeliveryMethod='PICK']/PersonInfoShipTo/@AddressLine1" />
									<br />
									<xsl:value-of select="concat(/Order/OrderLines/OrderLine[@DeliveryMethod='PICK']/PersonInfoShipTo/@City,', ',/Order/OrderLines/OrderLine[@DeliveryMethod='PICK']/PersonInfoShipTo/@State,' ',/Order/OrderLines/OrderLine[@DeliveryMethod='PICK']/PersonInfoShipTo/@ZipCode)" />
									<br />
									<xsl:value-of select="/Order/OrderLines/OrderLine[@DeliveryMethod='PICK']/PersonInfoShipTo/@DayPhone" />
								</font>
								<br/>
								<a style="color: black;text-decoration: none !important;">
									<xsl:attribute name="href"> 
										<xsl:value-of select="$URL_STORELOCATOR" /> 
									</xsl:attribute>
									<font class="font" style="line-height: 1.5;" id="text" >
											Please check your selected
									</font>
									<br />
									<font class="font" style="line-height: 1.5;" id="text" >
											store hours before visiting.
									</font>
								</a>
							</td>
						</tr>
					</table>
					<table style="margin-left:50px;" border="0" class="SlideBarWidth" width="80%" cellpadding="0" cellspacing="0">
						<tr>
							<td style="background:none; border-bottom: 2px solid #d8d8d8; height:1px; width:100%; margin:0px 0px 0px 0px;">
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
							</td>
						</tr>
					</table>
					<table border="0" class="ItemTab ItemPos" style="margin-left:50px">
						<tr>
							<td class="ItemTab ItemPos" style="height:75px;">
								<font class="ItemFont" style="font-family: Verdana;" size="4" color="black">ITEMS FOR PICKUP</font>
							</td>
						</tr>
					</table>
					<table class="ItemTab ItemPos" style="width:56%;margin-left:50px;" border="0">
						<xsl:for-each
							select="/Order/Shipments/Shipment[@ShipmentKey=$currShipKey]/ShipmentLines/ShipmentLine">
							<xsl:variable name="orderLineKey" select="./@OrderLineKey" />
							<xsl:variable name="ShipQty" select="./@Quantity" />
							<xsl:for-each select="/Order/OrderLines/OrderLine">
								<xsl:variable name="OrderQty" select="./@OrderedQty" />
								<xsl:if
									test="./@OrderLineKey=$orderLineKey and $OrderQty &gt; 0">
									<xsl:variable name="ImageLoc" select="./ItemDetails/PrimaryInformation/@ImageLocation" />
									<xsl:variable name="ImageID" select="./ItemDetails/PrimaryInformation/@ImageID" />
									<tr>
										<td class="OrderNoTd Block" style="width:20%;" align="middle">
											<img class="ItemImageSize" onerror="this.src='https://content.academy.com/weblib/images/favicon/touch-icon-60px.png'" 
										alt="Image not Available" width="125" height="auto" >
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
											<font id="text" class="ItemFont" >
												<b>Quantity:
													<xsl:text disable-output-escaping="yes">
														<![CDATA[&nbsp;]]>
													</xsl:text>
												</b>
												<xsl:value-of select="format-number($ShipQty, '0')" />
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
					<table style="margin-left:50px;" border="0" class="SlideBarWidth" width="80%" cellpadding="0" cellspacing="0">
						<tr>
							<td style="background:none; border-bottom: 2px solid #d8d8d8; height:1px; width:100%; margin:0px 0px 0px 0px;">
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
							</td>
						</tr>
					</table>
					<br />
					<table style="margin-left:50px;" class="vodPos vodTab" border="0" width="57%" cellpadding="0" cellspacing="0">
						<tr>
							<td class="HeaderPos" style="height:10px;" valign="bottom">
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
					<table style="margin-left:50px;" border="0" class="SlideBarWidth" width="80%" cellpadding="0" cellspacing="0">
						<tr>
							<td style="background:none; border-bottom: 2px solid #d8d8d8; height:1px; width:100%; margin:0px 0px 0px 0px;">
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
							</td>
						</tr>
					</table>
					<br />
					<table style="margin-left:50px;width:75%" class="BarcodePos vodTab" border="0">
						<tr>
							<td class="BarcodePos" align="center">
								<xsl:variable name="OrderNo"
									select="/Order/@OrderNo" />
								<img class="BarcodeSize" height="100" width="300">
									<xsl:attribute name="src">
										<!--BOPIS-1451 - Start -->
										<xsl:value-of select="concat($URL_BarcodeAPI,$OrdNo,$URL_BarcodeAPI1)" />
										<!--BOPIS-1451 - End -->
									</xsl:attribute> 
								</img>
							</td>
						</tr>
					</table>
					<table style="margin-left:50px;" border="0" class="SlideBarWidth" width="80%" cellpadding="0" cellspacing="0">
						<tr>
							<td style="background:none; border-bottom: 2px solid #d8d8d8; height:1px; width:100%; margin:0px 0px 0px 0px;">
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
							</td>
						</tr>
					</table>
					<table style="margin-left:15px;" class="QuesBannerTab QuesBannerPos" border="0" width="90%" cellpadding="0" cellspacing="0">
						<tr>
							<td class="ReturnPos" style="border-color:#0055a6;padding-left:40px;height:55px;">
								<font class="FooterFont" id="text">
									<a style="color: #0055a6;text-decoration: none !important;" >
										<xsl:attribute name="href"> 
											<xsl:value-of select="$URL_ReturnOrderInstructions" /> 
										</xsl:attribute>
									Return Order Instructions</a>
								</font>
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
											<b>ORDER PICKUP FAQS</b>
										</font>
									</button>
								</a>
								<br/>
								<br/>
							</td>
						</tr>
					</table>
					<table style="width:100%;margin-left:50px;" class="HeaderTab HeaderPos" border="0">
						<tr>
							<td>
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
							</td>
						</tr>
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