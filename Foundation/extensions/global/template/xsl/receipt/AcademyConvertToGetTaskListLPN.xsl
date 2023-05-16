<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="/AcademyMergedDocument/EnvironmentDocument/Task">
        <xsl:element name="Task">
	
            <xsl:attribute name="TargetZoneId">
              <xsl:value-of select="@TargetZoneId"/>
            </xsl:attribute>    
           
            
             <xsl:element name="TaskReferences">
<xsl:attribute name="ReceiptNo">
			   <xsl:value-of select="TaskReferences/@ReceiptNo"/>
			  </xsl:attribute>
	      </xsl:element>
                <xsl:element name="Inventory">
                
			  <xsl:attribute name="ReceiptHeaderKey">
			  
			   <xsl:value-of select="Inventory/@ReceiptHeaderKey"/>
			  </xsl:attribute>
			   <xsl:attribute name="QuantityQryType">
			  
			   <xsl:text>GT</xsl:text>
			  </xsl:attribute>
			   <xsl:attribute name="Quantity">
			   <xsl:text>0</xsl:text>
			   
			  </xsl:attribute>
             </xsl:element>
             
             <xsl:element name="TaskType">
             
             <xsl:attribute name="ActivityCode">
             
			   <xsl:value-of select="TaskType/@ActivityCode"/>
			  </xsl:attribute>
             </xsl:element>
        </xsl:element>
        
    </xsl:template>
    
</xsl:stylesheet>