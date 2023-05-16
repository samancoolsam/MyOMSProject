<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="/AcademyMergedDocument">
        <xsl:element name="Shipment">
          <!--
                     Following are the pickable Statuses
 1600.002.51 -GC Activated
 1600.002 - Shipment Invoiced
BulkGCProcessed - 1600.002.102
1600.002.52 - Pending Bulk GC Activated

Academy_Deliver_Shipment.0001.ex
Delivered But Pending GC Activation(1600.002.52.100)

............  Following are the corresponding Drop Statuses

shpment Delivered - 1600.002.100
1600.002.101 - Delivered & Bulk GC
1600.002.52.100 Delivered but Pending GC Activation -->
                     
                     <xsl:attribute name="BaseDropStatus">
                     
                     <xsl:choose>
		     <!-- EFP-21 Carrier Updates Consumption - Start-->
                     <xsl:when test="InputDocument/Shipments/Shipment/@Status='1600.002.51' or (InputDocument/Shipments/Shipment/@Status='1600.002') 
						or (InputDocument/Shipments/Shipment/@Status='1600.002.60') or (InputDocument/Shipments/Shipment/@Status='1600.002.70')
						or (InputDocument/Shipments/Shipment/@Status='1600.002.80') or (InputDocument/Shipments/Shipment/@Status='1600.002.90')">
		     <!-- EFP-21 Carrier Updates Consumption - end-->			
                     <xsl:value-of select="'1600.002.100'"/>
                     </xsl:when>
                     
                     <xsl:when test="InputDocument/Shipments/Shipment/@Status='1600.002.102'">
                     <xsl:value-of select="'1600.002.101'"/>
                     </xsl:when>
                     
                     <xsl:when test="InputDocument/Shipments/Shipment/@Status='1600.002.52'">
                     <xsl:value-of select="'1600.002.52.100'"/>
                     </xsl:when>
                     
                     <xsl:otherwise>
                     <xsl:value-of select="'InvalidPicking'"/>
                   </xsl:otherwise>
                     
                     </xsl:choose>
                     
             </xsl:attribute>
        
              
	<!--<xsl:attribute name="BaseDropStatus">
		<xsl:text>1600.002.100</xsl:text>
	</xsl:attribute> -->
	   <xsl:attribute name="TransactionId">
	      <xsl:text>Academy_Deliver_Shipment.0001.ex</xsl:text>
		</xsl:attribute>
            <xsl:attribute name="SellerOrganizationCode">
                <xsl:value-of select="InputDocument/Shipments/Shipment/@SellerOrganizationCode"/>
             </xsl:attribute>
	     <xsl:attribute name="ShipNode">
                <xsl:value-of select="InputDocument/Shipments/Shipment/@ShipNode"/>
             </xsl:attribute>
             <xsl:attribute name="ShipmentKey">
                <xsl:value-of select="InputDocument/Shipments/Shipment/@ShipmentKey"/>
             </xsl:attribute> 
             <xsl:attribute name="ShipmentNo">
                <xsl:value-of select="InputDocument/Shipments/Shipment/@ShipmentNo"/>
             </xsl:attribute> 
	     <xsl:element name="ShipmentStatusAudit">
	      <xsl:attribute name="StatusDate">
                <xsl:value-of select="EnvironmentDocument/Shipment/@DeliveryDate"/>
             </xsl:attribute> 
	  
	     </xsl:element>
       </xsl:element>
   </xsl:template>
</xsl:stylesheet>



