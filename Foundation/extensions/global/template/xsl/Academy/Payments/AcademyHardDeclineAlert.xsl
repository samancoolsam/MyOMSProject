<?xml version="1.0" encoding="UTF-8"?>
<!-- Licensed Materials - Property of IBM
IBM Sterling Call Center and Store
(C) Copyright IBM Corp. 2006, 2011 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
  --> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lxslt="http://xml.apache.org/xslt"
                version="1.0">

<xsl:template match="/">
<xsl:element name="Order">
	<xsl:attribute name="OrderNo"><xsl:value-of select="/Order/@OrderNo"/></xsl:attribute>
	<xsl:attribute name="EnterpriseCode"><xsl:value-of select="/Order/@EnterpriseCode"/></xsl:attribute>	
	<xsl:attribute name="OrderHeaderKey"><xsl:value-of select="/Order/@OrderHeaderKey"/></xsl:attribute>
	<xsl:attribute name="PaymentType"><xsl:value-of select="/Order/CollectionFailureDetails/@PaymentType"/></xsl:attribute>    
	<xsl:attribute name="RequestAmount"><xsl:value-of select="/Order/CollectionFailureDetails/@RequestAmount"/></xsl:attribute>
		
	<xsl:element name="PersonInfoBillTo">
		<xsl:attribute name="DayPhone"><xsl:value-of select="/Order/PersonInfoBillTo/@DayPhone"/></xsl:attribute>
		<xsl:attribute name="LastName"><xsl:value-of select="/Order/PersonInfoBillTo/@LastName"/></xsl:attribute>	
		<xsl:attribute name="FirstName"><xsl:value-of select="/Order/PersonInfoBillTo/@FirstName"/></xsl:attribute>	
		<xsl:attribute name="EveningPhone"><xsl:value-of select="/Order/PersonInfoBillTo/@EveningPhone"/></xsl:attribute>	
		<xsl:attribute name="MobilePhone"><xsl:value-of select="/Order/PersonInfoBillTo/@MobilePhone"/></xsl:attribute>	
	</xsl:element>
	
	<xsl:element name="CollectionFailureDetails">
		<xsl:attribute name="AuthReturnMessage"><xsl:value-of select="/Order/CollectionFailureDetails/@AuthReturnMessage"/></xsl:attribute>			
	</xsl:element>


</xsl:element>


</xsl:template>

</xsl:stylesheet>
