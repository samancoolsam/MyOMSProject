<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<% 
   String sAppCode = resolveValue("xml:/CurrentEntity/@ApplicationCode");
   boolean outboundShipment = false;
   if(equals(sAppCode,"omd")){
       outboundShipment = true;
   }
%>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/dm.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/modificationreason.js"></script>

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
			if(outboundShipment) { %>
		
			<yfc:makeXMLKey binding="xml:/Container/PackLocation/@Node" value="xml:/Container/Shipment/@ShipNode" />

	   <% } %>


	<yfc:makeXMLKey binding="xml:/Container/PackLocation/@LocationId" value="xml:/Container/LPN/LPNLocation/@LocationId" />
    </yfc:makeXMLInput>
	<yfc:makeXMLInput name="PackHSDEEntityKey">
		<yfc:makeXMLKey binding="xml:/Container/@ShipmentContainerKey" value="xml:/Container/@ShipmentContainerKey" />
		<yfc:makeXMLKey binding="xml:/Container/@ShipmentKey" value="xml:/Container/@ShipmentKey" />
		<yfc:makeXMLKey binding="xml:/Container/@ContainerScm" value="xml:/Container/@ContainerScm" />
		<yfc:makeXMLKey binding="xml:/Container/@ContainerNo" value="xml:/Container/@ContainerNo" />
		<yfc:makeXMLKey binding="xml:/Container/PackLocation/@Node" value="xml:/Container/Shipment/@ShipNode" />
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
	<input type="hidden" name="packHSDEEntityKey" value='<%=getParameter("PackHSDEEntityKey")%>'/>
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
			<yfc:i18n>Container_Type</yfc:i18n> 
		</td>
		<td class="protectedtext">
			<yfc:i18n><yfc:getXMLValue name="Container" binding="xml:/Container/@ContainerType"/></yfc:i18n> 
		</td>
		<td class="detaillabel" >
			<yfc:i18n>Container_Group</yfc:i18n> 
		</td>
		<td class="protectedtext">
			<yfc:getXMLValue name="Container" binding="xml:/Container/@ContainerGroup"/>
		</td>
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
					<yfc:i18n>Parent_Container_Group</yfc:i18n>
				</td>
				<td class="protectedtext">
					<yfc:getXMLValue name="Container" binding="xml:/Container/ParentContainer/@ContainerGroup"/>
				</td>
			</tr>
<%	}	%>

	<tr>
		<td class="detaillabel" >
			<yfc:i18n>Container_SCM</yfc:i18n>
		</td>
		<td class="protectedtext">
		    <%=replaceBlanksWithNoBlockSpaces(resolveValue("xml:/Container/@ContainerScm"))%>
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
		<%if(!isVoid(resolveValue("xml:/Container/Load/@LoadNo")) || !isVoid(resolveValue("xml:/Container/ParentContainer/Load/@LoadNo"))) {	%>
		</td>
				<td class="detaillabel" >
			<yfc:i18n>Load_#</yfc:i18n>
		</td>
		<td class="protectedtext">
		<%if(!isVoid(resolveValue("xml:/Container/Load/@LoadNo")))  { %>
			<yfc:makeXMLInput name="LoadKey" >
				<yfc:makeXMLKey binding="xml:/Load/@LoadKey" value="xml:/Container/Load/@LoadNo" />
			</yfc:makeXMLInput>
			<a <%=getDetailHrefOptions("L03",getParameter("LoadKey"),"")%> >
				<yfc:getXMLValue name="Container" binding="xml:/Container/Load/@LoadNo"/>
			</a>
		<%		}else{	%>
			<yfc:makeXMLInput name="LoadKey" >
				<yfc:makeXMLKey binding="xml:/Load/@LoadKey" value="xml:/Container/ParentContainer/Load/@LoadNo" />
			</yfc:makeXMLInput>
			<a <%=getDetailHrefOptions("L03",getParameter("LoadKey"),"")%> >
				<yfc:getXMLValue name="Container" binding="xml:/Container/ParentContainer/Load/@LoadNo"/>
			</a>
		<%		} %>
		</td>
		<%		}	%>
	</tr>

	<tr>
		<%if(equals(sAppCode, "oms")) {%>
    		<td class="detaillabel" >
    			<yfc:i18n>Received</yfc:i18n> 
    		</td>
            <td class="protectedtext">
                <yfc:getXMLValue name="Container" binding="xml:/Container/@IsReceived"/>
            </td>
        <%}%>
        
		<input type="hidden" name="xml:/Container/@DataElementPath" value="xml:/Container"/>
		<input type="hidden" name="xml:/Container/@ApiName" value="getShipmentContainerDetails"/>
		<input type="hidden" 
		<%=getTextOptions("xml:/Shipment/@SCAC","xml:/Container/Shipment/@SCAC")
		%>/>
		<input type="hidden" 
		<%=getTextOptions("xml:/Shipment/@IsShipmentLevelIntegration","xml:/Container/Shipment/@IsShipmentLevelIntegration")
		%>/>
		<input type="hidden" 
		<%=getTextOptions("xml:/Shipment/@AllCntrsPrinted","xml:/Container/Shipment/@AllCntrsPrinted")
		%>/>
	</tr>
</table>