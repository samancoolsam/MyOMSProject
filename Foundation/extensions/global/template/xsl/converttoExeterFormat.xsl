<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="/Receipt">
        <xsl:element name="Receipt">
         
            <xsl:attribute name="ReceiptNo">
               <xsl:value-of select="@ReceiptNo"/>
            </xsl:attribute>

            <xsl:attribute name="ReceivingDock">
               <xsl:value-of select="@ReceivingDock"/>
            </xsl:attribute>            
            
            <xsl:attribute name="ReceiptDate">
               <xsl:value-of select="@ReceiptDate"/>
            </xsl:attribute>

            <xsl:attribute name="ExtnASNContainer">
               <xsl:value-of select="ReceiptLines/ReceiptLine/@PalletId"/>
            </xsl:attribute>

            <xsl:attribute name="ExtnTimeStamp">
               <xsl:value-of select="@ReceiptDate"/>
            </xsl:attribute>

             <xsl:attribute name="ReceivingNode">
               <xsl:value-of select="@ReceivingNode"/>
            </xsl:attribute>

             <xsl:attribute name="ShipNode">
               <xsl:text>701</xsl:text>
            </xsl:attribute>
	    
	<xsl:attribute name="DispositionCode">
               <xsl:value-of select="ReceiptLines/ReceiptLine/@DispositionCode"/>
        </xsl:attribute>
	
            
   <xsl:element name="Shipment">
	 <xsl:attribute name="ShipmentNo">
               <xsl:value-of select="Shipment/@ShipmentNo"/>
            </xsl:attribute>
                    </xsl:element> 

             <xsl:element name="ReceiptLines">
                <xsl:choose>
           <xsl:when test = "(ReceiptLines/ReceiptLine/@IsBundle)='Y'">
	              <xsl:element name="ReceiptLine">
                  
             <xsl:attribute name="PrimeLineNo">
               <xsl:value-of select="ReceiptLines/ReceiptLine/@PrimeLineNo"/>
            </xsl:attribute>
	    
	    	   
            <xsl:attribute name="ItemID">
               <xsl:value-of select="ReceiptLines/ReceiptLine/@ItemID"/>
            </xsl:attribute>            
            
            <xsl:attribute name="ReceiptQuantity">
               <xsl:value-of select="ReceiptLines/ReceiptLine/@Quantity"/>
            </xsl:attribute>
            <xsl:attribute name="ShipmentQuantity">
               <xsl:value-of select="ReceiptLines/ReceiptLine/@OriginalQuantity"/>
            </xsl:attribute>
          
           <xsl:element name="DiscrepancyList">
             <xsl:for-each select="ReceiptLines/ReceiptLine/ShipmentDiscrepancy">
             
              <xsl:element name="Discrepancy">
              
               <xsl:attribute name="DiscrQty">
               <xsl:value-of select="@Quantity"/>
            </xsl:attribute>
            
            <xsl:attribute name="DiscrType">
               <xsl:value-of select="@DiscrepancyCode"/>
            </xsl:attribute>
            
               <xsl:attribute name="UserId">
               <xsl:value-of select="@CheckedBy"/>
            </xsl:attribute>
            
               <xsl:attribute name="Notes">
               <xsl:value-of select="@Notes"/>
            </xsl:attribute>
              	</xsl:element>
            </xsl:for-each>
            
           	</xsl:element>
         
         
				</xsl:element> 
				  </xsl:when>
				  <xsl:otherwise>
                 <xsl:for-each select="ReceiptLines/ReceiptLine">

                  <xsl:element name="ReceiptLine">
                  
             <xsl:attribute name="PrimeLineNo">
               <xsl:value-of select="@PrimeLineNo"/>

            </xsl:attribute>

	

            <xsl:attribute name="ItemID">
               <xsl:value-of select="@ItemID"/>
            </xsl:attribute>            
            
            <xsl:attribute name="ReceiptQuantity">
               <xsl:value-of select="@Quantity"/>
            </xsl:attribute>

            <xsl:attribute name="ShipmentQuantity">
               <xsl:value-of select="@OriginalQuantity"/>
            </xsl:attribute>
          
           <xsl:element name="DiscrepancyList">
             <xsl:for-each select="ShipmentDiscrepancy">
             
              <xsl:element name="Discrepancy">
              
               <xsl:attribute name="DiscrQty">
               <xsl:value-of select="@Quantity"/>
            </xsl:attribute>
            
            <xsl:attribute name="DiscrType">
               <xsl:value-of select="@DiscrepancyCode"/>
            </xsl:attribute>


            
               <xsl:attribute name="UserId">
               <xsl:value-of select="@CheckedBy"/>
            </xsl:attribute>
            
               <xsl:attribute name="Notes">
               <xsl:value-of select="@Notes"/>
            </xsl:attribute>
              	</xsl:element>
            </xsl:for-each>
            
           	</xsl:element>
         
         
				</xsl:element> 
				  
				 </xsl:for-each>
</xsl:otherwise>
    
 </xsl:choose>


				         
             </xsl:element>

        </xsl:element>
        
    </xsl:template>
    
</xsl:stylesheet>