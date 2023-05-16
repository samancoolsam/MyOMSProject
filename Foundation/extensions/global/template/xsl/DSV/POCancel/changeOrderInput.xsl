<?xml version="1.0" ?> 
 <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 <xsl:template match="/">
 <Order>
 
 <xsl:attribute name="DocumentType">
			<xsl:value-of select="/Order/@DocumentType">
               </xsl:value-of>
		           </xsl:attribute>
				  
	  
				   
<xsl:attribute name="OrderNo">
			<xsl:value-of select="/Order/@OrderNo">
               </xsl:value-of>
		           </xsl:attribute>		


				   
 <xsl:attribute name="EnterpriseCode">
			<xsl:value-of select="/Order/@EnterpriseCode">
               </xsl:value-of>
		           </xsl:attribute>	

<OrderLines>
<xsl:for-each select="/Order/OrderLines/OrderLine">		
<xsl:if test="@FulfillmentType='DROP_SHIP'">
<OrderLine>		 
  
<xsl:variable name="OrderedQty" select="@OrderedQty"/>
<xsl:variable name="BackOrderedQty" select="StatusBreakupForBackOrderedQty/BackOrderedFrom/@BackOrderedQuantity"/>
 <xsl:variable name="Quantity" select="$OrderedQty - $BackOrderedQty"/>

 <xsl:attribute name="Action">
		<xsl:text>CANCEL</xsl:text>
             
		           </xsl:attribute>	
				   
<xsl:attribute name="PrimeLineNo">
			<xsl:value-of select="@PrimeLineNo">
               </xsl:value-of>
		           </xsl:attribute>	
 
<xsl:attribute name="OrderedQty">
			<xsl:value-of select="$Quantity">
               </xsl:value-of>
		           </xsl:attribute>						   
 
 <xsl:attribute name="SubLineNo">
			<xsl:value-of select="@SubLineNo">
               </xsl:value-of>
		           </xsl:attribute>	
	<Item>
	
<xsl:attribute name="ItemID">
			<xsl:value-of select="Item/@ItemID">
               </xsl:value-of>
		           </xsl:attribute>	
		           </Item>  
		   
		
</OrderLine>
</xsl:if>
</xsl:for-each>
</OrderLines>
</Order>
		</xsl:template>
</xsl:stylesheet>