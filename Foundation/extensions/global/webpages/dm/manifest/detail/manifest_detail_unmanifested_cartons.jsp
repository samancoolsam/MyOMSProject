<%@include file="/yfsjspcommon/yfsutil.jspf"%>

<%
	double dTotalNoOfContainers = getNumericValue("xml:/Manifest/@TotalNumberOfContainers");
	double dNoOfContainersManifested = getNumericValue("xml:/Manifest/@NumberOfContainersManifested");
	double dNoOfShipmentsManifestedCompletely = getNumericValue("xml:/Manifest/@NumberOfShipmentsManifestedCompletely");
	double dTotalNumberOfShipments = getNumericValue("xml:/Manifest/Shipments/@TotalNumberOfRecords");
	double dNoOfLoadsManifestedCompletely = getNumericValue("xml:/Manifest/@NumberOfLoadsManifestedCompletely");
	double dTotalNumberOfLoads = getNumericValue("xml:/Manifest/Loads/@TotalNumberOfRecords");
%>

<div style="overflow:auto">
<table class="table" editable="false" width="100%" cellspacing="0">
    <thead> 
        <tr>            
            <td class="tablecolumnheader"><yfc:i18n>Container_#</yfc:i18n></td>
			<td class="tablecolumnheader"><yfc:i18n>Container_Status</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Container_Location</yfc:i18n></td>
        </tr>
    </thead>
    <tbody>
	<%
		if(dTotalNoOfContainers != dNoOfContainersManifested){
			if(dNoOfShipmentsManifestedCompletely != dTotalNumberOfShipments){
			
	%>
		 <yfc:loopXML binding="xml:/Manifest/Shipments/@Shipment" id="Shipment">		
			<yfc:loopXML binding="xml:/Shipment/Containers/@Container" id="Container">
			<%
				String sManifestKey = resolveValue("xml:/Container/@ManifestKey");
				String sParentContainerGroup = resolveValue("xml:/Container/@ParentContainerGroup");
				if(	((YFCCommon.equals(sParentContainerGroup,"INVENTORY")) || (YFCCommon.isVoid(sParentContainerGroup)) ) 
					 && ((sManifestKey==null) || (YFCCommon.isVoid(sManifestKey))) ){
					
			%>
				<tr>
					<td class="tablecolumn">
						<yfc:getXMLValue binding="xml:/Container/@ContainerNo"/>
					</td>
					<td class="tablecolumn">
						<yfc:getXMLValueI18NDB binding="xml:/Container/Status/@Description"/>
					</td>
					<td class="tablecolumn">
						<yfc:getXMLValue binding="xml:/Container/@ContainerLocation"/>
					</td>
				</tr>
			<%
				}				
			%>
			</yfc:loopXML>
		</yfc:loopXML>
		<%
			}
			if(dNoOfLoadsManifestedCompletely != dTotalNumberOfLoads){
		%>
		 <yfc:loopXML binding="xml:/Manifest/Loads/@Load" id="Load">
			<yfc:loopXML binding="xml:/Load/LoadContainers/@Container" id="Container">
			<%
				String sManifestKey = resolveValue("xml:/Container/@ManifestKey");
				String sParentContainerGroup = resolveValue("xml:/Container/@ParentContainerGroup");
				if(	((YFCCommon.equals(sParentContainerGroup,"INVENTORY")) || (YFCCommon.isVoid(sParentContainerGroup)) ) 
					 && ((sManifestKey==null) || (YFCCommon.isVoid(sManifestKey))) ){			
			%>
				<tr>
					<td class="tablecolumn">
						<yfc:getXMLValue binding="xml:/Container/@ContainerNo"/>
					</td>
					<td class="tablecolumn">
						<yfc:getXMLValueI18NDB binding="xml:/Container/Status/@Description"/>
					</td>
					<td class="tablecolumn">
						<yfc:getXMLValue binding="xml:/Container/@ContainerLocation"/>
					</td>
				</tr>
			<%
				}				
			%>
			</yfc:loopXML>
		</yfc:loopXML>
	<%
			}
		}
	%>
   </tbody>
</table>
</div>
