<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%
	String shipmentStatus=getValue("Shipment","xml:/Shipment/@Status");
%>
<table width="100%" class="view">
	<yfc:makeXMLInput name="TaskShipmentKey" >
			<yfc:makeXMLKey binding="xml:/Task/TaskReferences/@ShipmentKey" value="xml:/Shipment/@ShipmentKey" />
	</yfc:makeXMLInput>
	<tr>
		<td class="detaillabel" >
			<yfc:i18n>Execution_Status</yfc:i18n>
		</td>
		<td class="protectedtext" >
			<%=getI18N((resolveValue("xml:/Shipment/@ExecutionStatus")))%>
		</td>
		 <td class="detaillabel" >
			<yfc:i18n>Pack_And_Hold</yfc:i18n> 
		 </td>
		<td>
			<input type="checkbox"   <%=getCheckBoxOptions("xml:/Shipment/@PackAndHold","xml:/Shipment/@PackAndHold", "Y")%>  yfcCheckedValue='Y' yfcUnCheckedValue='N'/>							
		</td>
	</tr>
	<tr>
		<td class="detaillabel" >
			<yfc:i18n>Shipment_Sort_Lane</yfc:i18n>
		</td>
		<td class="protectedtext" >
			<yfc:getXMLValue  binding="xml:/Shipment/@ShipmentSortLocationId"/>	
		</td>	
		<td class="detaillabel" >
			<yfc:i18n>Carrier_Sort_Lane</yfc:i18n>
		</td>
		<td class="protectedtext" >
			<yfc:getXMLValue  binding="xml:/Shipment/@CarrierSortLocationId"/>	
		</td>	
	</tr>
	<tr>
	 <td class="detaillabel" >
		<yfc:i18n>Wave_#</yfc:i18n> 
	 </td>
 	<input type="hidden" name="xml:/Shipment/@WaveNo" value='<%=resolveValue("xml:/Shipment/@WaveNo")%>' />	
	<td  class="protectedtext">
		<a 		  
onclick="yfcShowListPopupWithParams('', '', '900', '500', '', 'wave',encodeURI('&xml:/Wave/@Node='+document.all('xml:/Shipment/@ShipNode').value+'&xml:/Wave/@WaveNo='+document.all('xml:/Shipment/@WaveNo').value));return false;" href="">
		<% if(equals("MULTIPLE",resolveValue("xml:/Shipment/@WaveNo"))){%>
			<yfc:i18n><yfc:getXMLValue  binding="xml:/Shipment/@WaveNo"/></yfc:i18n> 		
		<%}else{%>
			<yfc:getXMLValue  binding="xml:/Shipment/@WaveNo"/>
		<%}%>
		</a>
	</td>
	 <td class="detaillabel" >
		<yfc:i18n>Has_Shortage</yfc:i18n> 
	 </td>
 	<td  class="protectedtext">
		 <yfc:i18n><yfc:getXMLValue  binding="xml:/Shipment/@HasNodeExceptions"/></yfc:i18n>	
	</td>
	</tr>
	<tr>
	     <td class="detaillabel" >
			<yfc:i18n>Packed_Quantity</yfc:i18n> 
		 </td>
		 <td  class="protectedtext">
			<yfc:getXMLValue  binding="xml:/Shipment/@PlacedQuantity"/>	
		 </td>
		 <%  if( "1100.70.06.10".equals(shipmentStatus) || "1100".equals(shipmentStatus) || "1100.70.06.20".equals(shipmentStatus) || "1100.70.06.50".equals(shipmentStatus) || "1100.70.06.70".equals(shipmentStatus) ) { %>
			 <td class="detaillabel">
				<yfc:i18n>Override_Upgrade_Or_Downgrade</yfc:i18n> 
			 </td>
			 <td>
				<input type="checkbox" id="CheckOverrideUOrD"
				<%=getCheckBoxOptions("xml:/Shipment/Extn/@ExtnOverrideUOrD","xml:/Shipment/Extn/@ExtnOverrideUOrD", "Y")%>  
				yfcCheckedValue='Y' yfcUnCheckedValue='N' />
			 </td>
		<% } %>	
	</tr>	
</table>
