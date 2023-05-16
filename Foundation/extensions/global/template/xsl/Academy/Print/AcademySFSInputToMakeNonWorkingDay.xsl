<?xml version="1.0" ?> 
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
   <StoreCalendar>
                <xsl:attribute name="CalendarId">
                <xsl:value-of select="Manifest/@Scac" /> 
                </xsl:attribute>

                <xsl:attribute name="OrganizationCode">
                <xsl:value-of select="Manifest/@ShipNode" /> 
                </xsl:attribute>  
 
  </StoreCalendar>
</xsl:template>
</xsl:stylesheet>
