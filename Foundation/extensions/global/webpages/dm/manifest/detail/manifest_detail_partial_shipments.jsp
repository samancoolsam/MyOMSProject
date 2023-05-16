<%@include file="/yfsjspcommon/yfsutil.jspf"%>


<table class="table" editable="false" width="100%" cellspacing="0">
    <thead> 
        <tr>            
            <td class="tablecolumnheader"><yfc:i18n>Shipment_#</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Shipment_Status</yfc:i18n></td>
        </tr>
    </thead>
    <tbody>
	 <yfc:loopXML binding="xml:/Manifest/Shipments/@Shipment" id="Shipment">		
			<%
				String sManifestComplete = resolveValue("xml:/Shipment/@ShipmentManifestComplete");
				if(YFCCommon.equals(sManifestComplete,"N")){				
			%>
				<tr>
					<yfc:makeXMLInput name="shipmentKey">
						<yfc:makeXMLKey binding="xml:/Shipment/@ShipmentKey" value="xml:/Shipment/@ShipmentKey" />
						<yfc:makeXMLKey binding="xml:/Shipment/@ShipmentNo" value="xml:/Shipment/@ShipmentNo" />
						<yfc:makeXMLKey binding="xml:/Shipment/@ShipNode" value="xml:/Shipment/@ShipNode" />
					</yfc:makeXMLInput>
					<td class="tablecolumn">
						<a <%=getDetailHrefOptions("L01",getParameter("shipmentKey"),"")%> >
							<yfc:getXMLValue binding="xml:/Shipment/@ShipmentNo"/>				
						</a>
					</td>
					<td class="tablecolumn">
						<yfc:getXMLValueI18NDB binding="xml:/Shipment/Status/@Description"/>
					</td>
				</tr>
			<%
				}				
			%>
		</yfc:loopXML>
	
   </tbody>
</table>
