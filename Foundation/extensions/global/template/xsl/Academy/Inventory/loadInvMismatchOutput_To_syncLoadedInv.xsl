<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output indent="yes" />

   <xsl:template match="/">
   		<Inventory>
			 <xsl:attribute name="ApplyDifferences">
				<xsl:value-of select="'Y'" />
			 </xsl:attribute>

			 <xsl:attribute name="ReasonCode">
				<xsl:value-of select="//@ReasonCode"/>
         	 </xsl:attribute>
         	 
         	 <xsl:attribute name="ReasonText">
			 	<xsl:value-of select="//@ReasonText"/>
         	 </xsl:attribute>
         	 
         	 <xsl:attribute name="ShipNode">
			 	<xsl:value-of select="//@ShipNode"/>
         	 </xsl:attribute>
         	 
     		 <xsl:attribute name="YantraMessageGroupID">
			 	<xsl:value-of select="//@YantraMessageGroupID"/>
         	 </xsl:attribute>
      	</Inventory>
   </xsl:template>
   
</xsl:stylesheet>