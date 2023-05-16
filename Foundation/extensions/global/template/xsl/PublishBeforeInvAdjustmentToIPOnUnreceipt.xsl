<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"> 

                <xsl:template match="/">
                            <xsl:apply-templates select="Receipt"/>

                </xsl:template>                

                <xsl:template match="Receipt">

                                <xsl:element name="Receipt">

                                            <xsl:copy-of select = "@*" />

                                            <xsl:copy-of select = "node()[not(name(.)='ReceiptLines')]" />

                                            <xsl:apply-templates select="ReceiptLines"/>

                                </xsl:element>

                </xsl:template>                

                <xsl:template match="ReceiptLines">

                                <xsl:element name="ReceiptLines">

                                        <xsl:copy-of select="node()[@PickLine='Y']" />

                                </xsl:element>

                </xsl:template>                

</xsl:stylesheet>

