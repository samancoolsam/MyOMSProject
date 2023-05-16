<%@include file="/yfsjspcommon/yfsutil.jspf" %>
<%@ include file="/dm/manifest/detail/manifest_detail_summary_include.jspf" %>

<table width="100%" class="view">
	<yfc:makeXMLInput name="manifestKey">
		<yfc:makeXMLKey binding="xml:/Manifest/@ManifestKey" value="xml:/Manifest/@ManifestKey" />
		<yfc:makeXMLKey binding="xml:/Manifest/@ManifestNo" value="xml:/Manifest/@ManifestNo" />
		<yfc:makeXMLKey binding="xml:/Manifest/@ShipNode" value="xml:/Manifest/@ShipNode" />
		<yfc:makeXMLKey binding="xml:/Manifest/@Scac" value="xml:/Manifest/@Scac" />
    </yfc:makeXMLInput>

	<yfc:makeXMLInput name="manifestPrintKey">
		<yfc:makeXMLKey binding="xml:/Print/Manifest/@ManifestKey" value="xml:/Manifest/@ManifestKey" />
		<yfc:makeXMLKey binding="xml:/Print/Manifest/@ShipNode" value="xml:/Manifest/@ShipNode" />
		<yfc:makeXMLKey binding="xml:/Print/Manifest/@SCAC" value="xml:/Manifest/@Scac" />	
		<yfc:makeXMLKey binding="xml:/Print/Manifest/@IsHazmat" value="xml:/Manifest/@IsHazmat" />	
	</yfc:makeXMLInput>
	<input type="hidden" name="PrintEntityKey" value='<%=getParameter("manifestPrintKey")%>'/>
	<input type="hidden" name="chkEntityKey" value='<%=getParameter("manifestKey")%>'/>
	<input type="hidden" <%=getTextOptions("xml:/Manifest/@ManifestClosedFlag", "xml:/Manifest/@ManifestClosedFlag")%>/>
    <tr>
        <td class="detaillabel" nowrap="true">
            <yfc:i18n>Total_#_Shipments</yfc:i18n> 
        </td>
        <td class="protectedtext">
	        <yfc:getXMLValue binding="xml:/Manifest/Shipments/@TotalNumberOfRecords"/>
		</td>		
	</tr>
	 <tr>
        <td class="detaillabel" nowrap="true">
            <yfc:i18n>Total_#_Loads</yfc:i18n> 
        </td>
        <td class="protectedtext">
	        <yfc:getXMLValue binding="xml:/Manifest/Loads/@TotalNumberOfRecords"/>
		</td>		
	</tr>
	<tr>
        <td class="detaillabel" nowrap="true">
            <yfc:i18n>Num_Partially_Manifested_Shipments</yfc:i18n> 
        </td>
        <td class="protectedtext" >
	        <yfc:getXMLValue binding="xml:/Manifest/@PartialManifestedShipments" />
		</td>		
	</tr>
	<tr>
        <td class="detaillabel" nowrap="true">
            <yfc:i18n>Num_Partially_Manifested_Loads</yfc:i18n> 
        </td>
        <td class="protectedtext" >
	        <yfc:getXMLValue binding="xml:/Manifest/@PartialManifestedLoads" />
		</td>		
	</tr>
	<tr>
        <td class="detaillabel" nowrap="true">
            <yfc:i18n>Total_#_Packages_Manifested</yfc:i18n> 
        </td>
		<td class="protectedtext" >
			<a <%=getDetailHrefOptions("L01",getParameter("manifestKey"),"")%> >
		        <yfc:getXMLValue binding="xml:/Manifest/@NumberOfContainersManifested"/>
			</a>
		</td>		
	</tr>
	<tr>
        <td class="detaillabel" nowrap="true">
            <yfc:i18n>Num_Packages_to_be_Manifested_to_close_Manifest</yfc:i18n> 
        </td>
		<td class="protectedtext" >
	        <yfc:getXMLValue binding="xml:/Manifest/@TotalPacksToManifest"/>
		</td>		
    </tr>
	<input type="hidden" name="xml:/Manifest/@DataElementPath" value="xml:/Manifest"/>
	<input type="hidden" name="xml:/Manifest/@ApiName" value="getManifestDetails"/>
</table>