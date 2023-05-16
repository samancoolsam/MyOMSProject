<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">
    <xsl:template match="/AcademyMergedDocument">
    
        <xsl:element name="Shipment">
        
	            <xsl:attribute name="DoNotVerifyPalletContent">
	               <xsl:text>Y</xsl:text>
	            </xsl:attribute>
	            <xsl:attribute name="DocumentType">
	                <xsl:text>0006</xsl:text>
	            </xsl:attribute>
	            <xsl:attribute name="EnterpriseCode">
	                <xsl:text>Academy_Direct</xsl:text>
	            </xsl:attribute>
	            <xsl:attribute name="OrderAvailableOnSystem">
	                <xsl:text>N</xsl:text>
	            </xsl:attribute>
	            <xsl:attribute name="ReceivingNode">
	                <xsl:text>005</xsl:text>
	            </xsl:attribute>
	            <xsl:attribute name="SellerOrganizationCode">
	                <xsl:text>DEFAULT</xsl:text>
	            </xsl:attribute>
	            <xsl:attribute name="ShipNode">
	                <xsl:text>701</xsl:text>
	            </xsl:attribute>
	            <xsl:attribute name="ActualShipmentDate">
	               <xsl:value-of select="EnvironmentDocument/Shipment/Extn/@ActualShipmentDate"/>
	            </xsl:attribute>        
	            
            <xsl:element name="Containers">
            
         		<xsl:element name="Container">
         		
		         	<xsl:attribute name="ContainerNo">
		               <xsl:value-of select="EnvironmentDocument/Shipment/Extn/@ExtnASNContainer"/>
		            </xsl:attribute>
		            <xsl:attribute name="ContainerScm">
		               <xsl:value-of select="EnvironmentDocument/Shipment/Extn/@ExtnASNContainer"/>
		            </xsl:attribute>
		            <xsl:attribute name="ContainerType">
		                <xsl:text>Pallet</xsl:text>
		         	</xsl:attribute>
          
         				 <xsl:element name="ContainerDetails">
         				 
						          <xsl:if test="(InputDocument/Item/PrimaryInformation/@KitCode)='BUNDLE'">
							          
							           <xsl:for-each select="InputDocument/Item/Components/Component">
							           		<xsl:element name="ContainerDetail">
							            		<xsl:attribute name="ProductClass">
							                		<xsl:text>GOOD</xsl:text>
							            		</xsl:attribute>
							           			 <xsl:attribute name="UnitOfMeasure">
							              			  <xsl:text>EACH</xsl:text>
							           			 </xsl:attribute>
							             		<xsl:attribute name="ItemID">
							              			 <xsl:value-of select="@ComponentItemID"/>
							           			 </xsl:attribute> 
							           			 <xsl:attribute name="Quantity">
							            		   <xsl:value-of select="../../../../EnvironmentDocument/Shipment/ShipmentLines/ShipmentLine/@Quantity"/>
							           			 </xsl:attribute> 
							           			 
							           		<xsl:element name="ShipmentLine">
							                     <xsl:attribute name="ProductClass">
							               			 <xsl:text>GOOD</xsl:text>
							            		</xsl:attribute>
							           			<xsl:attribute name="UnitOfMeasure">
							                			<xsl:text>EACH</xsl:text>
							            		</xsl:attribute>
									            <xsl:attribute name="ItemID">
									               <xsl:value-of select="@ComponentItemID"/>
									            </xsl:attribute> 
							          			<xsl:attribute name="Quantity">
							             			  <xsl:value-of select="../../../../EnvironmentDocument/Shipment/ShipmentLines/ShipmentLine/@Quantity"/>
							               		 </xsl:attribute> 
								            	<xsl:attribute name="PrimeLineNo">
								               		<xsl:value-of select="../../../../EnvironmentDocument/Shipment/ShipmentLines/ShipmentLine/@PrimeLineNo"/>
								            	</xsl:attribute> 
							            	</xsl:element>
							           	</xsl:element>
							           </xsl:for-each>
							          </xsl:if>
							        
							        <xsl:if test="(InputDocument/Item/PrimaryInformation/@KitCode)=''">
							          
							           <xsl:for-each select="EnvironmentDocument/Shipment/ShipmentLines/ShipmentLine">
							           		<xsl:element name="ContainerDetail">
							            		<xsl:attribute name="ProductClass">
							                		<xsl:text>GOOD</xsl:text>
							            		</xsl:attribute>
							           			 <xsl:attribute name="UnitOfMeasure">
							              			  <xsl:text>EACH</xsl:text>
							           			 </xsl:attribute>
							             		<xsl:attribute name="ItemID">
							              			 <xsl:value-of select="@ItemID"/>
							           			 </xsl:attribute> 
							           			 <xsl:attribute name="Quantity">
							            		   <xsl:value-of select="@Quantity"/>
							           			 </xsl:attribute> 
							           			 
							           		<xsl:element name="ShipmentLine">
							                     <xsl:attribute name="ProductClass">
							               			 <xsl:text>GOOD</xsl:text>
							            		</xsl:attribute>
							           			<xsl:attribute name="UnitOfMeasure">
							                			<xsl:text>EACH</xsl:text>
							            		</xsl:attribute>
									            <xsl:attribute name="ItemID">
									               <xsl:value-of select="@ItemID"/>
									            </xsl:attribute> 
							          			<xsl:attribute name="Quantity">
							             			  <xsl:value-of select="@Quantity"/>
							               		 </xsl:attribute> 
								            	<xsl:attribute name="PrimeLineNo">
								               		<xsl:value-of select="@PrimeLineNo"/>
								            	</xsl:attribute> 
							            	</xsl:element>
							           	</xsl:element>
							           </xsl:for-each>
							          </xsl:if>
							</xsl:element>
       				</xsl:element>
       			</xsl:element>
       			
       			
       			<xsl:element name="ShipmentLines">
       			
       			<xsl:for-each select="EnvironmentDocument/Shipment/ShipmentLines/ShipmentLine">
       			
       			 <xsl:element name="ShipmentLine">
                    <xsl:attribute name="ProductClass">
		                <xsl:text>GOOD</xsl:text>
		            </xsl:attribute>
		            <xsl:attribute name="UnitOfMeasure">
		                <xsl:text>EACH</xsl:text>
		            </xsl:attribute>
		             <xsl:attribute name="ItemID">
		               <xsl:value-of select="@ItemID"/>
		            </xsl:attribute> 
		            <xsl:attribute name="Quantity">
		            	<xsl:value-of select="@Quantity"/>
		             </xsl:attribute> 
		             <xsl:attribute name="PrimeLineNo">
		               <xsl:value-of select="@PrimeLineNo"/>
		             </xsl:attribute> 
		                <xsl:attribute name="CustomerPoNo">
		               <xsl:value-of select="../../Extn/@ExtnASNContainer"/>
		             </xsl:attribute> 
            </xsl:element>
           </xsl:for-each>
       			 </xsl:element>
        
        </xsl:element>
         
    </xsl:template>
</xsl:stylesheet>