<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <xsl:output method="xml" doctype-system="Update_Academy_InventoryMonitor_10.dtd" indent="yes" />

   <xsl:template match="/">
      <xsl:variable name="ONHAND_QTY">
         <xsl:value-of select="format-number(//UnavailableLines/UnavailableLine/@AssignedQty,'#######')" />
      </xsl:variable>

      <xsl:variable name="NODE">
         <xsl:value-of select="'005'" />
      </xsl:variable>

      <Update_Academy_InventoryMonitor>
         <xsl:copy-of select="Update_Academy_InventoryMonitor/@*" />

         <xsl:copy-of select="Update_Academy_InventoryMonitor/ControlArea" />

         <DataArea>
            <InventoryMonitor>
               <AvailabilityChange>
                  <xsl:copy-of select="Update_Academy_InventoryMonitor/DataArea/InventoryMonitor/AvailabilityChange/@* [name()!='OnhandAvailableQuantity' and name() !='Node']" />

                  <xsl:attribute name="Node">
                     <xsl:value-of select="$NODE" />
                  </xsl:attribute>

                  <xsl:attribute name="OnhandAvailableQuantity">
                     <xsl:choose>
                        <xsl:when test="($ONHAND_QTY='NaN')">
                           <xsl:value-of select="0.00" />
                        </xsl:when>

                        <xsl:otherwise>
                           <xsl:choose>
                              <xsl:when test="($ONHAND_QTY='')">
                                 <xsl:value-of select="0.00" />
                              </xsl:when>
                              <xsl:otherwise>
                                 <xsl:value-of select="$ONHAND_QTY" />
                              </xsl:otherwise>
                           </xsl:choose>                          
                        </xsl:otherwise>
                     </xsl:choose>
                  </xsl:attribute>

                  <xsl:copy-of select="Update_Academy_InventoryMonitor/DataArea/InventoryMonitor/AvailabilityChange/Item" />
               </AvailabilityChange>
            </InventoryMonitor>
         </DataArea>
      </Update_Academy_InventoryMonitor>
   </xsl:template>
</xsl:stylesheet>

<!-- Stylus Studio meta-information - (c) 2004-2009. Progress Software Corporation. All rights reserved.

<metaInformation>
    <scenarios>
        <scenario default="yes" name="Scenario1" userelativepaths="yes" externalpreview="no" url="Inventory_Consolidate.xml" htmlbaseurl="" outputurl="" processortype="saxon8" useresolver="yes" profilemode="0" profiledepth="" profilelength=""
                  urlprofilexml="" commandline="" additionalpath="" additionalclasspath="" postprocessortype="none" postprocesscommandline="" postprocessadditionalpath="" postprocessgeneratedext="" validateoutput="no" validator="internal"
                  customvalidator="">
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

