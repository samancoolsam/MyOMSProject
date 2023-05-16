<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" doctype-system='Update_Academy_OrderStatus_10.dtd' indent="yes" />   
<!--    <xsl:output indent="yes" />  -->

   <xsl:template match="OrderList">
      <Update_Academy_OrderStatus version="1.0">
         <ControlArea>
            <Verb value="Update"/>
            <Noun value="Academy_OrderStatus"/>
         </ControlArea>

         <DataArea>
            <OrderConfirm>
               <OrderStatusHeader Versioning="FALSE" UpdateMode="P" >
                  <OrderNumber type="ByWCS">
				  	<xsl:value-of select="Order/@OrderNo"/>
                  </OrderNumber>

                  <Status StatusCondition="C" StatusFlag="O"/>
                  
               </OrderStatusHeader>

               <xsl:for-each select="Order/OrderLines/OrderLine">
                  <OrderStatusItem Versioning="FALSE">
                     <ItemNumber type="ByWCS">
                        <xsl:value-of select="./Extn/@ExtnWCOrderItemIdentifier" />
                     </ItemNumber>

                     <Status StatusCondition="C" StatusFlag="I"></Status>

                  </OrderStatusItem>
               </xsl:for-each>
            </OrderConfirm>
         </DataArea>
      </Update_Academy_OrderStatus>
   </xsl:template>
</xsl:stylesheet>
