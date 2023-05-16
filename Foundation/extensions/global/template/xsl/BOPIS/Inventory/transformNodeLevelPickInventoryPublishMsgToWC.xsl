<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml" doctype-system='Update_Academy_StoreInventoryMonitor_10.dtd' indent="yes" />
	
	<xsl:template match="/">
		<Update_Academy_StoreInventoryMonitor version="1.0">
		
			<ControlArea>
				<Verb value="Update"></Verb>
				<Noun value="Academy_StoreInventoryMonitor"></Noun>
			</ControlArea>
							
			<xsl:copy-of select="/Update_Academy_InventoryMonitor/DataArea"/>
					
		</Update_Academy_StoreInventoryMonitor>
	</xsl:template>
		
</xsl:stylesheet>
