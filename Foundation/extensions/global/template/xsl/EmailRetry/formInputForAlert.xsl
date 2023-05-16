<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lxslt="http://xml.apache.org/xslt"
                version="1.0">

<xsl:output indent="yes"/>
<xsl:template match="/">
    <xsl:apply-templates select="Order"/>
</xsl:template>

<xsl:template match="Order">
    <xsl:element name="Inbox">
        <xsl:attribute name="OrderHeaderKey" > 
            <xsl:value-of select="@OrderHeaderKey"/> 
        </xsl:attribute>
	        <xsl:attribute name="OrderNo" > 
            <xsl:value-of select="@OrderNo"/> 
        </xsl:attribute>
				
        <xsl:if test = "@TranIDForEmail='SEND_EMAIL_AFTER_CREATE.0001.ex'">
        <xsl:attribute name="ExceptionType" >
            <xsl:text>HAS_CREATE_EXCEPTIONS</xsl:text>
        </xsl:attribute>
        <xsl:attribute name="Description" >Email for Order <xsl:value-of select="@OrderNo"/> could not be sent in Created status.</xsl:attribute>
        </xsl:if>
	<xsl:if test = "@TranIDForEmail='SEND_EMAIL_ON_CANCEL.0001.ex'">
            <xsl:attribute name="ExceptionType" >
            <xsl:text>HAS_CANCEL_EXCEPTIONS</xsl:text>
        </xsl:attribute>
        <xsl:attribute name="Description" >Email for Order <xsl:value-of select="@OrderNo"/> could not be sent in Cancelled status.</xsl:attribute>
        </xsl:if>
	<xsl:if test = "@TranIDForEmail='SEND_EMAIL_RET_INVOICE.0003.ex'">
            <xsl:attribute name="ExceptionType" >
            <xsl:text>HAS_RETURN_INVOICE_EXCEPTIONS</xsl:text>
        </xsl:attribute>
        <xsl:attribute name="Description" >Email for Order <xsl:value-of select="@OrderNo"/> could not be sent in Return Invoiced status.</xsl:attribute>
        </xsl:if>
        <xsl:if test = "@TranIDForEmail='SEND_EMAIL_RET_REF.0003.ex'">
            <xsl:attribute name="ExceptionType" >
            <xsl:text>HAS_RETURN_REFUND_EXCEPTIONS</xsl:text>
        </xsl:attribute>
        <xsl:attribute name="Description" >Email for Order <xsl:value-of select="@OrderNo"/> could not be sent in Return Refund status.</xsl:attribute>
        </xsl:if>
	
	<xsl:if test = "@TranIDForEmail='SEND_EMAIL_ON_INVOICE.0001.ex'">
         <xsl:attribute name="ExceptionType" >
            <xsl:text>HAS_SHIPMENT_CONFIRM_EXCEPTIONS</xsl:text>
        </xsl:attribute>
        <xsl:attribute name="Description" >Email for Order <xsl:value-of select="@OrderNo"/> could not be sent in Shipment Confirmed status.</xsl:attribute>
        </xsl:if>
		
		<!-- Start WN-1560 Shipment Confirmation email - Wrong email IDs being passed -->
		<xsl:if test = "@TranIDForEmail='SEND_MAIL_WG_RET.0003.ex'">
         <xsl:attribute name="ExceptionType" >
            <xsl:text>HAS_WG_RETURN_EXCEPTIONS</xsl:text>
        </xsl:attribute>
        <xsl:attribute name="Description" >Email for Order <xsl:value-of select="@OrderNo"/> could not be sent in WG Return Created status.</xsl:attribute>
        </xsl:if>
		<!-- End WN-1560 Shipment Confirmation email - Wrong email IDs being passed -->	
       	
        <xsl:attribute name="InboxType" >
            <xsl:text>SEND_EMAIL_FAILURE</xsl:text>
        </xsl:attribute>
        <xsl:attribute name="OwnerKey" >
            <xsl:value-of select="@EnterpriseCode"/> 
        </xsl:attribute>
		

        <xsl:element name="InboxReferencesList">
                             
                <xsl:element name="InboxReferences" > 
                	<xsl:attribute name="InboxReferenceKey">
                        <xsl:value-of select="@OrderHeaderKey"/> 
                    </xsl:attribute>
                    <xsl:attribute name="ReferenceType">
                        <xsl:text>TEXT</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="Name">
                        <xsl:text>Order Email Alert</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="Value">
                        <xsl:value-of select="@OrderNo" />
                    </xsl:attribute>
                </xsl:element>
				
		<!-- Start WN-1560 Shipment Confirmation email - Wrong email IDs being passed -->	
		<xsl:choose>
            <xsl:when test="@InvalidEmailId='Y'">
				<xsl:element name="InboxReferences" > 
                	<xsl:attribute name="ReferenceType">
                        <xsl:text>TEXT</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="Name">
                        <xsl:text>Invalid Email Address</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="Value">
						 <xsl:value-of select="@InvalidEmailId" />
                    </xsl:attribute>
                </xsl:element>
			</xsl:when>
		</xsl:choose>
		<!-- End WN-1560 Shipment Confirmation email - Wrong email IDs being passed -->	
            
        </xsl:element>
    </xsl:element>
</xsl:template>

</xsl:stylesheet>
