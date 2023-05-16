<xsl:stylesheet version="1.0" 
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output omit-xml-declaration="yes" indent="yes"/>

    <xsl:variable name="PrintDocument" select="//PrintDocument/InputData/AcademyMergedDocument/EnvironmentDocument/Container/@PrintDocumentId"/>

    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="PrintDocuments/PrintDocument/@BeforeChildrenPrintDocumentId">
        <xsl:attribute name="BeforeChildrenPrintDocumentId">
            <xsl:value-of select="$PrintDocument"/>
        </xsl:attribute>
    </xsl:template>
</xsl:stylesheet>