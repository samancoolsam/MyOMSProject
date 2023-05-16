<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="Shipment">
    
    
    
        <xsl:element name="SerialList">
        
         	<xsl:for-each select="Containers/Container">
         	
         	<xsl:for-each select="ContainerDetails/ContainerDetail">
         	
         		<xsl:for-each select="ShipmentTagSerials/ShipmentTagSerial">
         		 <xsl:element name="GiftCardChargeRequest">
         		
			            <xsl:attribute name="GiftCardNo">
			               <xsl:value-of select="@SerialNo"/>
			            </xsl:attribute>
			
			            <xsl:attribute name="Amount">
			               <xsl:value-of select="../../ShipmentLine/OrderLine/LinePriceInfo/@UnitPrice"/>
			            </xsl:attribute>            
                       </xsl:element>  
	

	
</xsl:for-each>
	
</xsl:for-each>
	
</xsl:for-each>
             
	

        </xsl:element>
        
    </xsl:template>
    
</xsl:stylesheet>