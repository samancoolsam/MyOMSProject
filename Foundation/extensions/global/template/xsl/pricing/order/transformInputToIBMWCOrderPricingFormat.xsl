<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
  <xsl:output indent="yes"/>
    <xsl:template match="Order">
     <xsl:element name="Order">
     <xsl:copy-of select="./@*" />
         
         <xsl:element name="OrderLines">
         
         
           <xsl:for-each select="OrderLines/OrderLine">
           <xsl:choose>
           <xsl:when test = "@KitQty">
           
           	 </xsl:when>
           	 
           	 <xsl:otherwise>
           
             <xsl:element name="OrderLine">
             
             <xsl:copy-of select="@*"/>
             
            <xsl:element name="LinePriceInfo">
            
            
           <xsl:choose>
           <xsl:when test ="(LinePriceInfo/@IsPriceLocked)='N'">
            <xsl:attribute name="UnitPrice">
                <xsl:text></xsl:text>
            </xsl:attribute>
            
            <xsl:attribute name="TaxableFlag">
                <xsl:value-of select="LinePriceInfo/@TaxableFlag"/>
            </xsl:attribute>
            
            <xsl:attribute name="ListPrice">
                <xsl:value-of select="LinePriceInfo/@ListPrice"/>
            </xsl:attribute>
            
            <xsl:attribute name="LineTotal">
                <xsl:value-of select="LinePriceInfo/@LineTotal"/>
            </xsl:attribute>
            
            <xsl:attribute name="IsPriceLocked">
                <xsl:value-of select="LinePriceInfo/@IsPriceLocked"/>
            </xsl:attribute>
                      </xsl:when>
           
           <xsl:otherwise>
           <xsl:copy-of select="LinePriceInfo/@*"/>
           </xsl:otherwise>
           </xsl:choose>
            
          
         </xsl:element>
             </xsl:element>
            </xsl:otherwise>
    
           </xsl:choose>
            </xsl:for-each>
           
           
            </xsl:element>
                 
                 
                 
                 
        </xsl:element>
        
         
          
    
        
         
            
      
        
         
    </xsl:template>
</xsl:stylesheet>