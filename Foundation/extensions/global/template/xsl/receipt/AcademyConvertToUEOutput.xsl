<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">

    <xsl:template match="/AcademyMergedDocument">
       
	<xsl:element name="Receipt">
	
		  
			<xsl:attribute name="CanReceiptBeClosed">
				   <xsl:text>N</xsl:text>
			 </xsl:attribute> 

		   <xsl:if test ="(InputDocument/LPNs/@TotalNumberOfRecords)=0"> 
		
		   <xsl:attribute name="CanReceiptBeClosed">
					  <xsl:text>Y</xsl:text>
			  </xsl:attribute>   
		
		 
</xsl:if>
		 

	   
		
        
		    
	<xsl:attribute name="ReceiptHeaderKey">
	      <xsl:value-of select="EnvironmentDocument/Task/Inventory/@ReceiptHeaderKey"/>
	 </xsl:attribute>
	  <xsl:attribute name="ReceiptNo">
	       <xsl:value-of select="EnvironmentDocument/Task/TaskReferences/@ReceiptNo"/>
	   </xsl:attribute>         
        </xsl:element>
        
    </xsl:template>
    
</xsl:stylesheet>