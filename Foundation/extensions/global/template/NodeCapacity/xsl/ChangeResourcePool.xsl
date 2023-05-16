<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes"/>
	<xsl:template match="/">               
		<ResourcePool>
			<xsl:copy-of select="/ResourcePoolList/ResourcePool/@*"/>                 
			  
			<EffectiveDateRanges>
				<xsl:copy-of select="/ResourcePoolList/EffectiveDateRanges/@*"/> 
			  
				<EffectiveDateRange>  
					<xsl:copy-of select="/ResourcePoolList/EffectiveDateRange/@*"/>
					<CapacitiesPerSlot> 
						<xsl:copy-of select="/ResourcePoolList/CapacitiesPerSlot/@*"/>
						<CapacityPerSlot>
							<xsl:copy-of select="/ResourcePoolList/CapacityPerSlot/@*"/>
						</CapacityPerSlot>
					</CapacitiesPerSlot> 
				</EffectiveDateRange>  
			</EffectiveDateRanges>
		</ResourcePool>
	</xsl:template>
</xsl:stylesheet>