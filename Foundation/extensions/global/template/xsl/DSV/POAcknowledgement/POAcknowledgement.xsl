<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:template match="/">
   
   <xsl:element name="OrderStatusChange">
   
   		<xsl:attribute name="BaseDropStatus">
                     <xsl:text>3200.001</xsl:text>
   		</xsl:attribute>
   		
   		<xsl:attribute name="DocumentType">
                     <xsl:text>0005</xsl:text>
   		</xsl:attribute>
   		
   		<xsl:attribute name="EnterpriseCode">
                     <xsl:text>Academy_Direct</xsl:text>
   		</xsl:attribute>
   		
   		<xsl:attribute name="OrderNo">
                    <xsl:value-of select="/CustomerServiceUpdate/CustomerService/Header/OrderNumber" />
   		</xsl:attribute>
   		
   		<xsl:attribute name="TransactionId">
                     <xsl:text>EXTN_PO_Acknowledged.0005.ex</xsl:text>
   		</xsl:attribute>
   		
   </xsl:element>
   	 </xsl:template>
   	 </xsl:stylesheet>