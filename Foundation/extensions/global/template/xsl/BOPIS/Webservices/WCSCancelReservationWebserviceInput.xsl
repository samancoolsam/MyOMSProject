<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
         <xsl:copy-of select="/AcademyCancelReservationService/CancelReservation"/> 
         <xsl:copy-of select="/CancelReservation"/> 
    </xsl:template>
</xsl:stylesheet>