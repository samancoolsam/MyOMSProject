<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0">

    <xsl:template match="/">
	<Promise>
	<xsl:copy-of select="Promise/@*"/>
	<xsl:copy-of select="Promise/ReservationParameters"/>
	<ShipToAddress>
	<xsl:copy-of select="Promise/ShipToAddress/@*[name() !='ZipCode']"/>
		<xsl:variable name="ZIPCODE">
	<xsl:value-of select="Promise/ShipToAddress/@ZipCode"/>
	</xsl:variable>
	<xsl:variable name="ZIPCODE_TRIM">
	<xsl:value-of select="substring($ZIPCODE,'1','5')"/>
	</xsl:variable>
	<xsl:attribute name="ZipCode">
	<xsl:value-of select="$ZIPCODE_TRIM"/>
	</xsl:attribute>
	</ShipToAddress>
	<PromiseLines>
	<xsl:for-each select="Promise/PromiseLines/PromiseLine">
	<PromiseLine>
	<xsl:copy-of select="@*"/>
	<ShipToAddress>
	<xsl:copy-of select="ShipToAddress/@*[name() !='ZipCode']"/>
	<xsl:variable name="ZIPCODE">
	<xsl:value-of select="ShipToAddress/@ZipCode"/>
	</xsl:variable>
	<xsl:variable name="ZIPCODE_TRIM">
	<xsl:value-of select="substring($ZIPCODE,'1','5')"/>
	</xsl:variable>
	<xsl:attribute name="ZipCode">
	<xsl:value-of select="$ZIPCODE_TRIM"/>
	</xsl:attribute>
	</ShipToAddress>
	</PromiseLine>
	</xsl:for-each>
	</PromiseLines>
	</Promise>
        
    </xsl:template>
    
</xsl:stylesheet>
<!-- Stylus Studio meta-information - (c) 2004-2009. Progress Software Corporation. All rights reserved.

<metaInformation>
	<scenarios>
		<scenario default="yes" name="Scenario1" userelativepaths="yes" externalpreview="no" url="AcademyReservation.xml" htmlbaseurl="" outputurl="" processortype="saxon8" useresolver="yes" profilemode="0" profiledepth="" profilelength="" urlprofilexml=""
		          commandline="" additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext="" validateoutput="no" validator="internal" customvalidator="">
			<advancedProp name="sInitialMode" value=""/>
			<advancedProp name="bXsltOneIsOkay" value="true"/>
			<advancedProp name="bSchemaAware" value="true"/>
			<advancedProp name="bXml11" value="false"/>
			<advancedProp name="iValidation" value="0"/>
			<advancedProp name="bExtensions" value="true"/>
			<advancedProp name="iWhitespace" value="0"/>
			<advancedProp name="sInitialTemplate" value=""/>
			<advancedProp name="bTinyTree" value="true"/>
			<advancedProp name="xsltVersion" value="2.0"/>
			<advancedProp name="bWarnings" value="true"/>
			<advancedProp name="bUseDTD" value="false"/>
			<advancedProp name="iErrorHandling" value="fatal"/>
		</scenario>
	</scenarios>
	<MapperMetaTag>
		<MapperInfo srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/>
		<MapperBlockPosition></MapperBlockPosition>
		<TemplateContext></TemplateContext>
		<MapperFilter side="source"></MapperFilter>
	</MapperMetaTag>
</metaInformation>
-->