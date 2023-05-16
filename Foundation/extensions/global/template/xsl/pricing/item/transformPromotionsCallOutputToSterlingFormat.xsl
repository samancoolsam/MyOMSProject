<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="/ItemList">
        <xsl:element name="ItemList">
        
         <xsl:element name="Item">
          <xsl:attribute name="ItemID">
                <xsl:value-of select="Item/@ItemID"/>
       	     </xsl:attribute> 
       	          <xsl:attribute name="OrganizationCode">
               <xsl:text>DEFAULT</xsl:text>
       	     </xsl:attribute>     
       	          <xsl:attribute name="UnitOfMeasure">
                <xsl:text>EACH</xsl:text>
       	     </xsl:attribute>   

       	      <xsl:element name="PromotionList">
       	    <xsl:if test="(Item/@ExtnIsShipPromoEligble)='Y'">  
       	      <xsl:element name="Promotion">
       	      
       	      <xsl:attribute name="URL">
                <xsl:value-of select="Item/@WebsiteDisplayURL"/>
       	     </xsl:attribute> 
       	                    <xsl:attribute name="LongDescription">
               <xsl:text>Free Shipping</xsl:text>
       	     </xsl:attribute>     
       	          <xsl:attribute name="ShortDescription">
                <xsl:text>Free Shipping</xsl:text>
       	     </xsl:attribute>   
       	                   </xsl:element>
			   </xsl:if>
       	                    </xsl:element>
       	 </xsl:element>
       	 
            
         </xsl:element>
        
         
    </xsl:template>
</xsl:stylesheet>