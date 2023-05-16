<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="/AcademyMergedDocument">
        <xsl:element name="ItemList">
        
         <xsl:element name="Item">
          <xsl:attribute name="ItemID">
                <xsl:value-of select="InputDocument/ItemList/Item/@ItemID"/>
       	     </xsl:attribute> 
       	          <xsl:attribute name="OrganizationCode">
             <xsl:value-of select="InputDocument/ItemList/Item/@OrganizationCode"/>
       	     </xsl:attribute>     
       	          <xsl:attribute name="UnitOfMeasure">
            <xsl:value-of select="InputDocument/ItemList/Item/@UnitOfMeasure"/>
       	     </xsl:attribute>   
	          	          <xsl:attribute name="ExtnIsShipPromoElgble">
            <xsl:value-of select="EnvironmentDocument/ItemList/Item/@ExtnIsShipPromoEligble"/>
       	     </xsl:attribute>   
	          	          <xsl:attribute name="ExtnEndDate">
            <xsl:value-of select="EnvironmentDocument/ItemList/Item/@ExtnEndDate"/>
       	     </xsl:attribute>   
       	         <xsl:element name="Extn">
		    <xsl:attribute name="ExtnEndDate">
            <xsl:value-of select="EnvironmentDocument/ItemList/Item/@ExtnEndDate"/>
       	     </xsl:attribute>   
		 </xsl:element>
	      
	      <xsl:element name="ComputedPrice">
       	     <xsl:attribute name="ListPrice">
                <xsl:value-of select="EnvironmentDocument/ItemList/Item/ComputedPrice/@ListPrice"/>
       	     </xsl:attribute> 
	     	<xsl:attribute name="RetailPrice">
                <xsl:value-of select="EnvironmentDocument/ItemList/Item/ComputedPrice/@RetailPrice"/>
       	     </xsl:attribute> 
	<xsl:attribute name="UnitPrice">
                <xsl:value-of select="EnvironmentDocument/ItemList/Item/ComputedPrice/@UnitPrice"/>
       	     </xsl:attribute> 
       	      <xsl:element name="QuantityRangePriceList">
	      <xsl:element name="QuantityRangePrice">

 <xsl:element name="Region">
	
	<xsl:attribute name="RegionDescription">
                <xsl:value-of select="EnvironmentDocument/ItemList/Item/@WebsiteDisplayURL"/>
       	     </xsl:attribute> 

    </xsl:element>

       	        </xsl:element>
       	     
       	                    
       	                   </xsl:element>
	
       	                    </xsl:element>
      
	 </xsl:element>
       	 
            
         </xsl:element>
        
         
    </xsl:template>
</xsl:stylesheet>