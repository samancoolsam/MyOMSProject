<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<% 
   String sAppCode = resolveValue("xml:/CurrentEntity/@ApplicationCode");
%>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/dm.js"></script>

<table width="100%" class="view">
	<yfc:makeXMLInput name="ContainerKey">
		<yfc:makeXMLKey binding="xml:/Container/@ShipmentContainerKey" value="xml:/Container/@ShipmentContainerKey" />
		<yfc:makeXMLKey binding="xml:/Container/@ShipmentKey" value="xml:/Container/@ShipmentKey" />
		<yfc:makeXMLKey binding="xml:/Container/@ContainerScm" value="xml:/Container/@ContainerScm" />
		<yfc:makeXMLKey binding="xml:/Container/@ContainerNo" value="xml:/Container/@ContainerNo" />
    </yfc:makeXMLInput>
	<yfc:makeXMLInput name="packEntityKey">
		<yfc:makeXMLKey binding="xml:/Container/@ShipmentContainerKey" value="xml:/Container/@ShipmentContainerKey" />
		<yfc:makeXMLKey binding="xml:/Container/@ShipmentKey" value="xml:/Container/@ShipmentKey" />
		<yfc:makeXMLKey binding="xml:/Container/@ContainerScm" value="xml:/Container/@ContainerScm" />
		<yfc:makeXMLKey binding="xml:/Container/@ContainerNo" value="xml:/Container/@ContainerNo" />

		<yfc:makeXMLKey binding="xml:/Container/PackLocation/@Node" value="xml:/Container/Shipment/@ReceivingNode" />

		<%  
			String docType = resolveValue("xml:/Container/Shipment/@DocumentType");
			if( "0001".equals(docType) ) { %>
		
			<yfc:makeXMLKey binding="xml:/Container/PackLocation/@Node" value="xml:/Container/Shipment/@ShipNode" />

	   <% } %>


		<yfc:makeXMLKey binding="xml:/Container/PackLocation/@LocationId" value="xml:/Container/LPN/LPNLocation/@LocationId" />
    </yfc:makeXMLInput>
	<yfc:makeXMLInput name="containerPrintKey">
		<yfc:makeXMLKey binding="xml:/Print/Container/@ShipmentContainerKey" value="xml:/Container/@ShipmentContainerKey" />
		<yfc:makeXMLKey binding="xml:/Print/Container/@ShipNode" value="xml:/Container/Shipment/@ShipNode" />
		<yfc:makeXMLKey binding="xml:/Print/Container/@SCAC" value="xml:/Container/Shipment/@SCAC" />
		<yfc:makeXMLKey binding="xml:/Print/Container/@EnterpriseCode" value="xml:/Container/Shipment/@EnterpriseCode" />
		<yfc:makeXMLKey binding="xml:/Print/Container/@BuyerOrganizationCode" value="xml:/Container/Shipment/@BuyerOrganizationCode" />
		<yfc:makeXMLKey binding="xml:/Print/Container/@SellerOrganizationCode" value="xml:/Container/Shipment/@SellerOrganizationCode" />
	</yfc:makeXMLInput>
	
	<input type="hidden" name="packEntityKey" value='<%=getParameter("packEntityKey")%>'/>
	<input type="hidden" name="PrintEntityKey" value='<%=getParameter("containerPrintKey")%>'/>
	<tr>
		<td class="detaillabel" >
			<yfc:i18n>Container_#</yfc:i18n>
		</td>
		<td class="protectedtext">
			<yfc:getXMLValue name="Container" binding="xml:/Container/@ContainerNo"/>
			<input type="hidden" <%=getTextOptions("xml:/Container/@ShipmentContainerKey")%>/>
			<input type="hidden" <%=getTextOptions("xml:/Container/@ShipmentKey")%>/>
			<input type="hidden" <%=getTextOptions("xml:/Container/@TransactionId")%>/>
		</td>
		<td class="detaillabel" >
			<yfc:i18n>Shipment_#</yfc:i18n>
		</td>
		<td class="protectedtext">
			<yfc:makeXMLInput name="ShipmentKey" >
				<yfc:makeXMLKey binding="xml:/Shipment/@ShipmentKey" value="xml:/Container/Shipment/@ShipmentKey" />
			</yfc:makeXMLInput>
			<a <%=getDetailHrefOptions("L01",getParameter("ShipmentKey"),"")%> >
				<yfc:getXMLValue name="Container" binding="xml:/Container/Shipment/@ShipmentNo"/>
			</a>
		</td>
		<td class="detaillabel" >
			<yfc:i18n>Container_Type</yfc:i18n> 
		</td>
		<td class="protectedtext">
			<yfc:i18n><yfc:getXMLValue name="Container" binding="xml:/Container/@ContainerType"/></yfc:i18n> 
		</td>
	
	</tr>
	<tr>
		<td class="detaillabel" >
			<yfc:i18n>Ship_Date</yfc:i18n>
		</td>
		<td class="protectedtext">
			<yfc:getXMLValue name="Container" binding="xml:/Container/@ShipDate"/>
		</td>
		<td class="detaillabel" >
			<yfc:i18n>Container_Group</yfc:i18n> 
		</td>
		<td class="protectedtext">
			<yfc:getXMLValue name="Container" binding="xml:/Container/@ContainerGroup"/>
		</td>
		
		<%if(equals(sAppCode, "oms")) {%>
    		<td class="detaillabel" >
    			<yfc:i18n>Received</yfc:i18n> 
    		</td>
            <td class="protectedtext">
                <yfc:getXMLValue name="Container" binding="xml:/Container/@IsReceived"/>
            </td>
        <%}%>
        
		<%if(equals(resolveValue("xml:/CurrentEntity/@ApplicationCode"),"omd")){%>
		<td class="detaillabel" >
			<yfc:i18n>Manifested</yfc:i18n> 
		</td>
	    <td class="protectedtext">
			<%if(!isVoid(resolveValue("xml:/Container/@ManifestKey"))){%>
				<yfc:i18n>Y</yfc:i18n>  
			  <%}else{%>
				<yfc:i18n>N</yfc:i18n>  
			  <%}%>
        </td>
		<%}else{%>
		<td/>
		<td/>
		<%}%>
		<input type="hidden" name="xml:/Container/@DataElementPath" value="xml:/Container"/>
		<input type="hidden" name="xml:/Container/@ApiName" value="getShipmentContainerDetails"/>
	</tr>
<%	if(!isVoid(resolveValue("xml:Container:/Container/@ParentContainerKey"))) {%>
			<yfc:makeXMLInput name="parentcontainerKey">
				<yfc:makeXMLKey binding="xml:/Container/@ShipmentContainerKey" value="xml:/Container/@ParentContainerKey" />
				<yfc:makeXMLKey binding="xml:/Container/@ShipmentKey" value="xml:/Container/Shipment/@ShipmentKey" />
			</yfc:makeXMLInput>
			<tr>
				<td class="detaillabel" >
					<yfc:i18n>Parent_Container_#</yfc:i18n>
				</td>
				<td class="protectedtext">
					<a <%=getDetailHrefOptions("L02",getParameter("parentcontainerKey"),"")%> >
						<yfc:getXMLValue name="Container" binding="xml:/Container/@ParentContainerNo"/>
					</a>
				</td>
				<td class="detaillabel" >
					<yfc:i18n>Parent_Container_Type</yfc:i18n>
				</td>
				<td class="protectedtext">
					<yfc:getXMLValue name="Container" binding="xml:/Container/ParentContainer/@ContainerType"/>
				</td>
				<td class="detaillabel" >
					<yfc:i18n>Remove_quantity_from_shipment_line</yfc:i18n>
				</td>
				<td nowrap="true">
					<input type="checkbox" <%=getCheckBoxOptions("xml:/Container/@RemoveFromShipmentLine", "xml:/Container/@RemoveFromShipmentLine", "Y")%>>
				</td>
			</tr>
<%	} else {	%>
			<tr>
				<td>
				</td>
				<td>
				</td>
				<td>
				</td>
				<td>
				</td>
				<td class="detaillabel" >
					<yfc:i18n>Remove_quantity_from_shipment_line</yfc:i18n>
				</td>
				<td nowrap="true">
					<input type="checkbox" <%=getCheckBoxOptions("xml:/Container/@RemoveFromShipmentLine", "xml:/Container/@RemoveFromShipmentLine", "Y")%>>
				</td>
			</tr>
<%	}	%>



</table>