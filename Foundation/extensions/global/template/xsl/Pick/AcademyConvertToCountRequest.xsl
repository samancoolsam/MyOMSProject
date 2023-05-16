<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="ExecutionExceptionFlow">
    
    
        <xsl:element name="CreateCountRequest">
         
            <xsl:attribute name="EnterpriseCode">
               <xsl:value-of select="Task/@EnterpriseKey"/>
            </xsl:attribute>


            <xsl:attribute name="ItemID">
               <xsl:value-of select="Task/Inventory/@ItemId"/>
            </xsl:attribute>
            
            
            <xsl:attribute name="LocationId">
               <xsl:value-of select="Task/@SourceLocationId"/>
            </xsl:attribute>
            
            
            <xsl:attribute name="Node">
               <xsl:value-of select="Task/@OrganizationCode"/>
            </xsl:attribute>
            
            
            <xsl:attribute name="ProductClass">
               <xsl:value-of select="Task/Inventory/@ProductClass"/>
            </xsl:attribute>
            
            
            <xsl:attribute name="UnitOfMeasure">
               <xsl:value-of select="Task/Inventory/@UnitOfMeasure"/>
            </xsl:attribute>
            
    
        </xsl:element>
        
    </xsl:template>
    
</xsl:stylesheet>