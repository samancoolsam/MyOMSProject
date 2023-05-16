<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes"/>
	<xsl:template match="/">               
		<Organization>
			<xsl:copy-of select="/OrganizationList/Organization/@*"/>
			<Node>
				<xsl:copy-of select="/OrganizationList/Node/@*"/> 
			</Node>
		</Organization>
	</xsl:template>
</xsl:stylesheet>