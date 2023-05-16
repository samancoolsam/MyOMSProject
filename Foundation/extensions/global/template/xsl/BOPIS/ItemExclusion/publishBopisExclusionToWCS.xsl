<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" doctype-system='Update_Academy_StoreItemExclusion_10.dtd' indent="yes" />   
<!--    <xsl:output indent="yes" />  -->

   <xsl:template match="/">
      <Update_Academy_StoreItemExclusion version="1.0">
	  <xsl:variable name="Operation" select="Item/Extn/AcademyBopisItemExclusionList/AcademyBopisItemExclusion/@Operation" />
         <ControlArea>
            <Verb value="Update"/>
            <Noun value="Academy_StoreItemExclusion"/>
         </ControlArea>

         <DataArea>
		 
		 <BopisExclusion>
			<xsl:attribute name="ItemID">
				<xsl:value-of select="Item/@ItemID"/>
			</xsl:attribute>
			<xsl:choose>
                <xsl:when test="$Operation='Delete'">
                    <xsl:attribute name="Operation">
		                <xsl:value-of select="$Operation"/>
	                </xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
	                <xsl:attribute name="Operation">
		               <xsl:text>Add</xsl:text>
	                </xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
			<xsl:attribute name="StoreNo">
				<xsl:value-of select="Item/Extn/AcademyBopisItemExclusionList/AcademyBopisItemExclusion/@StoreNo"/>
			</xsl:attribute>
	      </BopisExclusion>
         </DataArea>
      </Update_Academy_StoreItemExclusion>
   </xsl:template>
</xsl:stylesheet>
