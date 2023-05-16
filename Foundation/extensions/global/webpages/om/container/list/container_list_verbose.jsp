<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ include file="/console/jsp/modificationutils.jspf" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/dm.js"></script>
<% 
   String sFromLoadConsole = resolveValue("xml:yfcSearchCriteria:/Container/@FromLoadConsole");   
   String sAppCode = resolveValue("xml:/CurrentEntity/@ApplicationCode");
   boolean outboundShipment = false;
   if(equals(sAppCode,"omd")){
       outboundShipment = true;
   }

%>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/dm.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/modificationreason.js"></script>

<table class="table" editable="false" width="100%" cellspacing="0">
    <thead> 
        <tr>
            <td sortable="no" class="checkboxheader">
                <input type="checkbox" name="checkbox" value="checkbox" onclick="doCheckAll(this);"/>
            </td>
            <td class="tablecolumnheader"><yfc:i18n>Container_#</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Shipment_#</yfc:i18n></td>
			<td class="tablecolumnheader"><yfc:i18n>Status</yfc:i18n></td>
	<%if(outboundShipment) {%>
			<td class="tablecolumnheader"><yfc:i18n>Manifested</yfc:i18n></td>
	<%}%>
            <td class="tablecolumnheader"><yfc:i18n>Container_Type</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Tracking_#</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Container_SCM</yfc:i18n></td>
			<td class="tablecolumnheader"><yfc:i18n>Ship_Date</yfc:i18n></td>
		<%if(equals("Y",sFromLoadConsole)){%>
			<td class="tablecolumnheader"><yfc:i18n>Loaded_Trailer_No</yfc:i18n></td>
		<%}%>
		<%if(!outboundShipment) {%>
    		<td class="tablecolumnheader">
                <yfc:i18n>Received</yfc:i18n>
            </td>
		<%}%>
			<input type="hidden" name="xml:/Container/@DataElementPath" value="xml:/Container"/>
			<input type="hidden" name="xml:/Container/@ApiName" value="getShipmentContainerDetails"/>
        </tr>
    </thead>
    <tbody>
        <yfc:loopXML binding="xml:/Containers/@Container" id="Container">
            <tr>
                <yfc:makeXMLInput name="containerKey">
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
                <yfc:makeXMLInput name="packKey">
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
					<yfc:makeXMLKey binding="xml:/Print/Container/@SCAC" value="xml:/Container/@SCAC" />
					<yfc:makeXMLKey binding="xml:/Print/Container/@EnterpriseCode" value="xml:/Container/Shipment/@EnterpriseCode" />
					<yfc:makeXMLKey binding="xml:/Print/Container/@BuyerOrganizationCode" value="xml:/Container/Shipment/@BuyerOrganizationCode" />
					<yfc:makeXMLKey binding="xml:/Print/Container/@SellerOrganizationCode" value="xml:/Container/Shipment/@SellerOrganizationCode" />
				</yfc:makeXMLInput>
                <td class="checkboxcolumn"> 
                    <input type="checkbox" value='<%=getParameter("containerKey")%>' name="EntityKey" PrintEntityKey='<%=getParameter("containerPrintKey")%>' PackHSDEKey='<%=getParameter("packKey")%>'/>
                </td>
                <td class="tablecolumn"><a href="javascript:showDetailFor('<%=getParameter("containerKey")%>');">
                    <yfc:getXMLValue binding="xml:/Container/@ContainerNo"/>
				</td>
				<td class="tablecolumn">
					<yfc:getXMLValue binding="xml:/Container/Shipment/@ShipmentNo"/>
				</td>
                <td class="tablecolumn">
                    <yfc:getXMLValueI18NDB binding="xml:/Container/Status/@Description"/>
                </td>
	<%if(outboundShipment) {%>
				<td class="tablecolumn">
					<% String isManifested= (!isVoid(resolveValue("xml:/Container/@ManifestKey"))) ? "Y" :""; 
					%>
                    <%=isManifested%>
                </td>
	<%}%>
				<td class="tablecolumn">
					<yfc:getXMLValue binding="xml:/Container/@ContainerType"/>
				</td>
				<td class="tablecolumn">
                   <%=replaceBlanksWithNoBlockSpaces(resolveValue("xml:/Container/@TrackingNo"))%>					
				</td>
				<td class="tablecolumn">
					<%=replaceBlanksWithNoBlockSpaces(resolveValue("xml:/Container/@ContainerScm"))%>
				</td>
                <td class="tablecolumn">
					<yfc:getXMLValue binding="xml:/Container/@ShipDate"/>
				</td>
			<%if(equals(sFromLoadConsole,"Y")){%>
				<td class="tablecolumn">
					<yfc:getXMLValue binding="xml:/Container/@LoadedTrailerNo"/>
				</td>
			<%}%>
			<%if(!outboundShipment) {%>
    		     <td class="tablecolumn">
    		         <yfc:getXMLValue binding="xml:/Container/@IsReceived"/>
                 </td>
            <%}%>
            </tr>
        </yfc:loopXML> 
   </tbody>
   <input type="hidden" <%=getTextOptions("xml:/Shipment/@DocumentType","xml:yfcSearchCriteria:/Container/Shipment/@DocumentType")
	%>/>
   <input type="hidden" <%=getTextOptions("xml:/Shipment/@OrderAvailableOnSystem","xml:/Container/Shipment/@OrderAvailableOnSystem")
	%>/>
	<input type="hidden" 
	<%=getTextOptions("xml:/Shipment/@SCAC","xml:/Container/@SCAC")
	%>/>
	<input type="hidden" 
	<%=getTextOptions("xml:/Shipment/@IsShipmentLevelIntegration","xml:/Container/Shipment/@IsShipmentLevelIntegration")
	%>/>
	<input type="hidden" 
	<%=getTextOptions("xml:/Shipment/@AllCntrsPrinted","xml:/Container/Shipment/@AllCntrsPrinted")
	%>/>
</table>