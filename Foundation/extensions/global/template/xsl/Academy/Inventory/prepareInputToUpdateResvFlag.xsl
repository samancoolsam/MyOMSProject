<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output indent="yes" />

   <xsl:template match="/">
   		<InventoryItem>
			 <xsl:attribute name="ItemID">
				<xsl:value-of select="//@ItemID" />
			 </xsl:attribute>

			 <xsl:attribute name="ProductClass">
				<xsl:value-of select="//@ProductClass"/>
         	 </xsl:attribute>
         	 
         	 <xsl:attribute name="UnitOfMeasure">
			 	<xsl:value-of select="//@UnitOfMeasure"/>
         	 </xsl:attribute>  
			 
         	<AvailabilityChanges>		
            <AvailabilityChange>			
            <xsl:copy-of select="InventoryItem/AvailabilityChanges/AvailabilityChange[@Node ='']/@*" />
			</AvailabilityChange>
			</AvailabilityChanges>
      	</InventoryItem>
   </xsl:template>
</xsl:stylesheet>