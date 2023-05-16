<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:lxslt="http://xml.apache.org/xslt" version="1.0">
	<xsl:output method="html" />
	<xsl:template match="/">
		<xsl:variable name="currShipKey" select="/Order/@CurrentShipmentKey" />
		<xsl:variable name="IconImagePath"	select="/Order/@IconImagePath" />
		<xsl:variable name="EmailType" select="/Order/@EmailType" />
		<xsl:variable name="URL_AcademyHomePage" select="/Order/@URL_AcademyHomePage" />
		<xsl:variable name="URL_CLOTHING" select="/Order/@URL_CLOTHING" />
		<xsl:variable name="URL_FOOTWEAR" select="/Order/@URL_FOOTWEAR" />
		<xsl:variable name="URL_SPORTS" select="/Order/@URL_SPORTS" />
		<xsl:variable name="URL_OUTDOORS" select="/Order/@URL_OUTDOORS" />
		<xsl:variable name="URL_ACCESSORIES" select="/Order/@URL_ACCESSORIES" />
		<xsl:variable name="URL_HOTDEALS" select="/Order/@URL_HOTDEALS" />
		<xsl:variable name="URL_CLEARANCE" select="/Order/@URL_CLEARANCE" />
		<xsl:variable name="URL_ViewOrderDetails" select="/Order/@URL_ViewOrderDetails" />
		<xsl:variable name="URL_LiveChat" select="/Order/@URL_LiveChat" />
		<xsl:variable name="URL_UpdateProfile" select="/Order/@URL_UpdateProfile" />
		<xsl:variable name="URL_PrivacyPolicy" select="/Order/@URL_PrivacyPolicy" />
		<xsl:variable name="URL_Facebook" select="/Order/@URL_Facebook" />
		<xsl:variable name="URL_Twitter" select="/Order/@URL_Twitter" />
		<xsl:variable name="URL_PinRest" select="/Order/@URL_PinRest" />
		<xsl:variable name="URL_YouTube" select="/Order/@URL_YouTube" />
		<xsl:variable name="URL_Instagram" select="/Order/@URL_Instagram" />
		<xsl:variable name="IMG_AcademyLOGO" select="/Order/@IMG_AcademyLOGO" />
		<xsl:variable name="IMG_NOIMAGE" select="/Order/@IMG_NOIMAGE" />
		<xsl:variable name="IMG_Facebook" select="/Order/@IMG_Facebook" />
		<xsl:variable name="IMG_Twitter" select="/Order/@IMG_Twitter" />
		<xsl:variable name="IMG_Pinrest" select="/Order/@IMG_Pinrest" />
		<xsl:variable name="IMG_Youtube" select="/Order/@IMG_Youtube" />
		<xsl:variable name="IMG_Instagram" select="/Order/@IMG_Instagram" />
		<!-- OMNI-6211 Pick Up Confirmation Email  Cancellation mail  changes- START-->
		<xsl:variable name="URL_GETANSWER" select="/Order/@URL_GETANSWER" />
		<xsl:variable name="URL_OrdPickFAQ" select="/Order/@URL_OrdPickFAQ" />
		<!-- OMNI-6211 Pick Up Confirmation Email  Cancellation mail  changes- END-->
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
		text-align: left ;
		margin-left: 25px !important;
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
	.FooterFont{
		font-size: 25px !important;
	}
	<!-- OMNI-6211 Pick Up Confirmation Email  Cancellation mail  changes- START-->
	.ReturnPos1{
		padding-left: 0 !important;
		margin-left: 0 !important;
		text-align: center !important;
	}
	<!-- OMNI-6211 Pick Up Confirmation Email  Cancellation mail  changes- END-->
	.desktopHide1{
		display: block !important;
		width:90%;
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
					<![CDATA[<meta name="viewport" content="width=device-width, initial-scale=1.0"></meta>]]>
				</xsl:text>
			</head>
			<body style="margin:0;padding:0">
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
									<!-- OMNI-6211 Pick Up Confirmation Email  Cancellation mail  changes- START-->
									<font style="font-size: 14px;color:gray;font-family: Verdana;">SHOES + BOOTS</font>
									<!-- OMNI-6211 Pick Up Confirmation Email  Cancellation mail  changes- END-->
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
					<table style="margin-left:10px;" border="0" class="SlideBarWidth" width="95%" cellpadding="0" cellspacing="0">
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
								<!-- OMNI-6211 Pick Up Confirmation Email  Cancellation mail  changes- START-->
								<font style="color: #333333 !important;" class="HeaderFont" size="8" face="Impact">
									<!-- <b>PART OR ALL OF YOUR ORDER HAS BEEN CANCELED</b>-->
										 <b>Important Order Update</b>
								<!-- OMNI-6211 Pick Up Confirmation Email  Cancellation mail  changes- END-->
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
					<br/>
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
					<br/>
					<table class="OrderNoTab OrderNoPos" style="margin-left:50px;" border="0">
						<tr class="Block">
							<td class="OrderNoTd">
								<font size="6" class="OrderNoFont" color="black" face="Impact">
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
								<xsl:variable name="Mon">
									<xsl:value-of select="substring(/Order/@OrderDate,6,2)" />
								</xsl:variable>

								<xsl:variable name="Date">
									<xsl:value-of select="substring(/Order/@OrderDate,9,2)" />
								</xsl:variable>

								<xsl:variable name="Year">
									<xsl:value-of select="substring(/Order/@OrderDate,3,2)" />
								</xsl:variable>
							<!-- OMNI-6211 Pick Up Confirmation Email  Cancellation mail  changes- START-->
							<!--	<xsl:choose>
									<xsl:when test="starts-with($Mon, '0') and starts-with($Date, '0')">
										<xsl:variable name="Month">
											<xsl:value-of select="substring(/Order/@OrderDate,7,1)" />
										</xsl:variable>
										<xsl:variable name="Dates">
											<xsl:value-of select="substring(/Order/@OrderDate,10,1)" />
										</xsl:variable>	
										<font size="3" class="font" id="text" color="black"> Order Date: <xsl:value-of select="concat($Month,'/', $Dates, '/', $Year)" />
										</font>
									</xsl:when>
									<xsl:when test="starts-with($Mon, '0') and not(starts-with($Date, '0'))">
										<xsl:variable name="Month">
											<xsl:value-of select="substring(/Order/@OrderDate,7,1)" />
										</xsl:variable>
										<font size="3" class="font" id="text" color="black"> Order Date: <xsl:value-of select="concat($Month,'/', $Date, '/', $Year)" />
										</font>
									</xsl:when>
									<xsl:when test="starts-with($Date, '0') and not(starts-with($Mon, '0'))">
										<xsl:variable name="Dates">
											<xsl:value-of select="substring(/Order/@OrderDate,10,1)" />
										</xsl:variable>
										<font size="3" class="font" id="text" color="black"> Order Date: <xsl:value-of select="concat($Mon,'/', $Dates, '/', $Year)" />
										</font>
									</xsl:when>
									<xsl:otherwise>
										<font size="3" class="font" id="text" color="black"> Order Date: <xsl:value-of select="concat($Mon,'/', $Date, '/', $Year)" />
										</font>
									</xsl:otherwise>
								</xsl:choose> -->
							<!-- OMNI-6211 Pick Up Confirmation Email  Cancellation mail  changes- END-->
							</td>
							<td class="HideLabel"/>
						</tr>
					</table>
					<br/>
					<table border="0" class="ItemTab ItemPos" style="width:75%;margin-left:50px;">
						<tr>
							<td class="ItemPos ItemTab">
								<font size="4" class="ItemFont" face="Verdana">ITEMS CANCELED</font>
							</td>
						</tr>
						<xsl:for-each select="/Order/OrderLines/OrderLine">
							<xsl:variable name="ImageLoc" select="./ItemDetails/PrimaryInformation/@ImageLocation" />
							<xsl:variable name="ImageID" select="./ItemDetails/PrimaryInformation/@ImageID" />
							<tr>
								<td class="OrderNoTd Block" style="width:20%;" align="middle">
									<img class="ItemImageSize" alt="Image not Available" width="125" height="auto">
										<xsl:attribute name="onerror"> 
											<xsl:value-of select="$IMG_NOIMAGE" /> 
										</xsl:attribute>
										<xsl:attribute name="src">
											<xsl:value-of select="concat($ImageLoc, $ImageID)" />
										</xsl:attribute>
									</img> 
								</td>
								<td class="OrderNoTd InlineBlock CenterAlign" style="width:55%">
									<font id="text" class="ItemFont">
										<xsl:value-of select="ItemDetails/PrimaryInformation/@Description" />
									</font>
									<br/>
									<font class="ItemFont" id="text" >
										<b>SKU:
											<xsl:text disable-output-escaping="yes">
												<![CDATA[&nbsp;]]>
											</xsl:text>
										</b>
										<xsl:value-of select="ItemDetails/@ItemID" />
									</font>
									<br/>
									<font class="ItemFont" id="text" >
										<b>Quantity:
											<xsl:text disable-output-escaping="yes">
												<![CDATA[&nbsp;]]>
											</xsl:text>
										</b>
										<xsl:choose>
											<xsl:when test="@OrderedQty='0.00'">
												<xsl:value-of select="format-number(@OriginalOrderedQty, '0')" />
											</xsl:when>
											<xsl:otherwise>
												<xsl:value-of select="format-number((@OriginalOrderedQty)-(@OrderedQty), '0')" />
											</xsl:otherwise>
										</xsl:choose>
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
						</xsl:for-each>
					</table>
					<table style="margin-left:50px;" class="SlideBarWidth" border="0" width="80%" cellpadding="0" cellspacing="0">
						<tr>
							<td style="background:none; border-bottom: 2px solid #d8d8d8; height:1px; width:100%; margin:0px 0px 0px 0px;">
								<xsl:text disable-output-escaping="yes">
									<![CDATA[&nbsp;]]>
								</xsl:text>
							</td>
						</tr>
					</table>
					<br/>
					<table style="margin-left:50px;" class="vodPos vodTab" border="0">
						<tr>
							<td class="HeaderPos">
								<font class="FooterFont" id="text">
									<a style="color: #00008b;text-decoration: none !important;">
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
				<!-- OMNI-6211 Pick Up Confirmation Email  Cancellation mail  changes- START-->
					<table style="margin-left:15px;" class="vodPos vodTab" border="0" width="90%" cellpadding="0" cellspacing="0">
					<!--	<tr>
							<td class="ReturnPos" style="border-color:#0055a6;padding-left:40px;height:55px;">
								<font class="FooterFont" id="text">
									<a style="color: #0055a6;text-decoration: none !important;" >
										<xsl:attribute name="href"> 
											<xsl:value-of select="$URL_ReturnOrderInstructions" /> 
										</xsl:attribute>
									Return Order Instructions</a>
								</font>
							</td>
						</tr>  -->
						<tr/>
						 <tr/>
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
					<!-- OMNI-6211 Pick Up Confirmation Email  Cancellation mail  changes- END-->
					<br/>
					<table style="width:100%;margin-left:50px;" class="HeaderTab HeaderPos" border="0">
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
								<font id="text1" class="FooterFont">Need Help? <a style="color: #0055a6;text-decoration: none !important;">
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