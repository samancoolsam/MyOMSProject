<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lxslt="http://xml.apache.org/xslt"
                version="1.0">

<xsl:template match="/">

    <HTML>
    
        <xsl:comment>CONTENT_TYPE=text/html</xsl:comment>

        <BODY>  
                <p>
                    <b>
                        <xsl:text>Email for the Order could not be sent :</xsl:text>

                    </b>

                    <ul>

                        <xsl:for-each select="//Inbox/InboxReferencesList/InboxReferences"> 
                           <xsl:if test="normalize-space(@ReferenceType) != &quot;TraceXML&quot;">
								<xsl:variable name="Temp" select="@Value"/>
								<li>
									<xsl:value-of select="$Temp"/>
								</li>
							</xsl:if>
                       </xsl:for-each>

                    </ul>

                </p>

       </BODY>

    </HTML>
    
</xsl:template>

</xsl:stylesheet>
