 <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:lxslt="http://xml.apache.org/xslt" version="1.0">
  <xsl:output method="html" /> 
 <xsl:template match="/">
  <xsl:variable name="HoldOrderNo" select="/Order/@OrderNo" />
    <xsl:variable name="HoldType" select="/Order/OrderHoldTypes/OrderHoldType/@HoldType" />
 <xsl:text disable-output-escaping="yes">
 <![CDATA[ <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
  ]]> 
  </xsl:text>
 <html>
  <xsl:comment>CONTENT_TYPE=text/html; charset=UTF-8</xsl:comment> 
 <head>
 <xsl:text disable-output-escaping="yes">
 <![CDATA[ <META http-equiv="Content-Type" content="text/html; charset=ascii"
  ]]> 
  </xsl:text>
  </head>
 <body style="margin:0;padding:0">
 <p>
 <WBR>
 <font size="3" face="Arial">
  <b>The Order: &#160;</b> 
  <b><xsl:value-of select="/Order/@OrderNo" /> </b>
  <b> is stuck in &#160;</b>
  <b><xsl:value-of select="/Order/OrderHoldTypes/OrderHoldType/@HoldType" /></b>
  <b> for more than 72 hours</b> 
  </font>
  </WBR>
  </p>
  </body>
  </html>
  </xsl:template>
  </xsl:stylesheet>
