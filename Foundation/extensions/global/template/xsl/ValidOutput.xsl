<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="/">
        <xsl:element name="Order">
             <xsl:attribute name="Response">
               <xsl:value-of select="'VALID'"/>
            </xsl:attribute>
              
              
              <xsl:attribute name="CustomerPhoneNo">
       		<xsl:choose>
				<xsl:when test="AcademyMergedDocument/EnvironmentDocument/Order/@CustomerPhoneNo=''">
                                       <xsl:value-of select="AcademyMergedDocument/InputDocument/OrderList/Order/@CustomerPhoneNo" />
                                    </xsl:when>
                                 
				<xsl:otherwise>
						<xsl:value-of select="AcademyMergedDocument/EnvironmentDocument/Order/@CustomerPhoneNo" />
					</xsl:otherwise>
				</xsl:choose>
                 </xsl:attribute>
          
		</xsl:element>    
		</xsl:template>
</xsl:stylesheet>