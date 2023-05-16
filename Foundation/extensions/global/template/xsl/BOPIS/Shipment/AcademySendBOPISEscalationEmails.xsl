<!--
######################################################################################################################################
OBJECTIVE: Prepare BOPIS escalation email body template.
######################################################################################################################################
Version         Date            Modified By       					Description
######################################################################################################################################
1.0             20/08/2019      Radhakrishna Mediboina(CTS-POD)    	POD: OMNI-436
######################################################################################################################################
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:lxslt="http://xml.apache.org/xslt" version="1.0">
	<xsl:output method="html" /> 
	<xsl:template match="/">
		<xsl:text disable-output-escaping="yes">
			<![CDATA[ <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"> ]]> 
		</xsl:text>
		<html>
			<xsl:comment>CONTENT_TYPE=text/html; charset=UTF-8</xsl:comment> 
			<head>
				<xsl:text disable-output-escaping="yes">
					<![CDATA[ <META http-equiv="Content-Type" content="text/html; charset=ascii" ]]> 
				</xsl:text>
			</head>
			<body style="margin:0;padding:10px">
				<br/>
				<table width="684" border="0" cellspacing="5">
				 <tr>
					<td>
						<p>
							<WBR>
								<font size="3" face="Arial">
									Store 
									<b>#<xsl:value-of select="/Shipment/@ShipNode"/></b>
									has an escalated pickup order. See details below:
								</font>
							</WBR>
						</p>
					</td>
				 </tr>
				</table>
				<br/>
				<table width="444" border="0" cellspacing="5">
				 <tr>
					<td>
						Order Number 
					</td>
			        <td>
						<b>:</b>
					</td>
					<td>
						<b><xsl:value-of select="/Shipment/ShipmentLines/ShipmentLine/Order/@OrderNo"/></b>
					</td>
				 </tr>
				 <tr>
					<td>
						Order Date 
					</td>
					<td>
						<b>:</b>
					</td>
					<td>
						<b><xsl:value-of select="substring(/Shipment/@OrderDate,1,10)"/></b>
					</td>
					<td>
						<b><xsl:value-of select="substring(/Shipment/@OrderDate,12,8)"/></b>
					</td>					
				 </tr>
				 <tr>
					<td>
						Time Received at Store
					</td>
			        <td>
						<b>:</b>
					</td>					
					<td>
						<b><xsl:value-of select="substring(/Shipment/@Createts,1,10)"/></b>
					</td>
					<td>
						<b><xsl:value-of select="substring(/Shipment/@Createts,12,8)"/></b>
					</td>
				 </tr>
				</table>
				<br/><br/><br/>
				<table width="684" border="0" cellspacing="5">
				 <tr>
					<td>
						<p>
							<WBR>
								<font size="2" face="Arial" color="#5E5A59">
									This is an automated email. Please do not reply.
								</font>
							</WBR>
						</p>
					</td>
				 </tr>
				</table>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
