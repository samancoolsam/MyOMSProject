<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="/AcademyMergedDocument">
        <xsl:element name="Container">
        
        <xsl:attribute name="AcceptOutOfSequenceUpdates">
                <xsl:text>Y</xsl:text>
             </xsl:attribute>
             
             <xsl:attribute name="Action">
                <xsl:text>Modify</xsl:text>
             </xsl:attribute>
             
             <xsl:attribute name="TransactionId">
                <xsl:text>Container_Delivered.5001.ex</xsl:text>
             </xsl:attribute>
             
                         
             <xsl:for-each select="InputDocument/Shipments/Shipment/Containers/Container">
              <xsl:if test="/AcademyMergedDocument/EnvironmentDocument/Shipment/@TrackingNo = @TrackingNo">
             
               <xsl:attribute name="ContainerNo">
                <xsl:value-of select="@ContainerNo"/>
             </xsl:attribute> 
             
             <xsl:attribute name="ShipmentContainerKey">
                <xsl:value-of select="@ShipmentContainerKey"/>
             </xsl:attribute>
             
             <xsl:attribute name="TrackingNo">
                <xsl:value-of select="@TrackingNo"/>
             </xsl:attribute>
                      
         </xsl:if>
             </xsl:for-each>
             
             
        </xsl:element>     
                        
   </xsl:template>
</xsl:stylesheet>

