<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="/TaskList">

     <xsl:element name="MultiApi">

<xsl:for-each select="Task">

 <xsl:element name="API">
     <xsl:attribute name="Name">
                <xsl:text>moveLocationInventory</xsl:text>
            </xsl:attribute> 
	     <xsl:element name="Input">

    <xsl:element name="MoveLocationInventory">
         
            <xsl:attribute name="EnterpriseCode">
               <xsl:text>Academy_Direct</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="Node">
                <xsl:text>005</xsl:text>
            </xsl:attribute>            
            
             <xsl:element name="Source">
			    <xsl:attribute name="LocationId">
				<xsl:value-of select="@TargetLocationId"/>
			    </xsl:attribute>     
           
                 
					     <xsl:element name="Inventory">
					     
							    <xsl:attribute name="Quantity">
								 <xsl:value-of select="Inventory/@Quantity"/>
							    </xsl:attribute>     
							    
							       <xsl:attribute name="ReceiptHeaderKey">
								 <xsl:value-of select="Inventory/@ReceiptHeaderKey"/>
							    </xsl:attribute>  

							   <!--  Fix done for issue#3208 -->
							    <!--
								<xsl:attribute name="InventoryStatus">
								 <xsl:value-of select="Inventory/@InventoryStatus"/>
							    </xsl:attribute>  
							    -->
					   
					    
								   <xsl:element name="InventoryItem">
					    
									   <xsl:attribute name="ItemID">
									 <xsl:value-of select="Inventory/@ItemId"/>
								    </xsl:attribute>  
									   <xsl:attribute name="ProductClass">
									 <xsl:value-of select="Inventory/@ProductClass"/>
								    </xsl:attribute>  
									   <xsl:attribute name="UnitOfMeasure">
									<xsl:value-of select="Inventory/@UnitOfMeasure"/>
								    </xsl:attribute>  
					    
								 </xsl:element>
					    
						 </xsl:element>
	     
			 </xsl:element>
           
				  <xsl:element name="Destination">
						    
						     <xsl:attribute name="PalletId">
							 <xsl:value-of select="Inventory/@SourcePalletId"/>
						    </xsl:attribute>  
							   <xsl:attribute name="LocationId">
							<xsl:value-of select="@TargetLocationId"/>
						    </xsl:attribute> 
						     </xsl:element>
						     
							   <xsl:element name="Audit">
						    
						     <xsl:attribute name="ReasonCode">
							 <xsl:text>EXCEPTION</xsl:text>
						    </xsl:attribute>  
							   <xsl:attribute name="ReasonText">
							<xsl:text>This adjustment is for Exception location</xsl:text>
						    </xsl:attribute> 
					  </xsl:element>
 </xsl:element>
 </xsl:element>
 </xsl:element>
     
	      </xsl:for-each>

        </xsl:element>


    </xsl:template>
</xsl:stylesheet>