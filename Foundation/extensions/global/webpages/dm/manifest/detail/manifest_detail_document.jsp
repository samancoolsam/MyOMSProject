<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%String addressLine="";%>
<table width="100%" class="view">
	<tr>
		<td colspan="2" align="left" class="protectedtext">        
			<font face="Arial Narrow" size="5"><center />
			<b><yfc:i18n>Manifest_Document</yfc:i18n></b>
			</font>
		</td>
	</tr>
	<tr><td colspan="2">&nbsp;&nbsp;&nbsp;</td></tr>
	<tr>
		<td  class="protectedtext" align="left">
			<font face="Arial Narrow" size="2">
			<b><yfc:i18n>Ship_From</yfc:i18n></b><br/>
			<yfc:getXMLValue name="Manifest" binding="xml:/Manifest/ShipNode/ShipNodePersonInfo/@FirstName"/> &nbsp;
			<yfc:getXMLValue name="Manifest" binding="xml:/Manifest/ShipNode/ShipNodePersonInfo/@LastName"/> <br/>
			<% addressLine =getValue("Manifest","xml:/Manifest/ShipNode/ShipNodePersonInfo/@AddressLine1");
			if(!isVoid(addressLine)){%>
				<yfc:getXMLValue name="Manifest" binding="xml:/Manifest/ShipNode/ShipNodePersonInfo/@AddressLine1"/><br/>
			<% } 
			addressLine =getValue("Manifest","xml:/Manifest/ShipNode/ShipNodePersonInfo/@AddressLine2");
			if(!isVoid(addressLine)){%>
				<yfc:getXMLValue name="Manifest" binding="xml:/Manifest/ShipNode/ShipNodePersonInfo/@AddressLine2"/><br/>
			<% } 
			addressLine =getValue("Manifest","xml:/Manifest/ShipNode/ShipNodePersonInfo/@AddressLine3");
			if(!isVoid(addressLine)){%>
				<yfc:getXMLValue name="Manifest" binding="xml:/Manifest/ShipNode/ShipNodePersonInfo/@AddressLine3"/><br/>
			<% } 
			addressLine =getValue("Manifest","xml:/Manifest/ShipNode/ShipNodePersonInfo/@AddressLine4");
			if(!isVoid(addressLine)){%>
				<yfc:getXMLValue name="Manifest" binding="xml:/Manifest/ShipNode/ShipNodePersonInfo/@AddressLine4"/><br/>
			<% } 
			addressLine =getValue("Manifest","xml:/Manifest/ShipNode/ShipNodePersonInfo/@AddressLine5");
			if(!isVoid(addressLine)){%>
				<yfc:getXMLValue name="Manifest" binding="xml:/Manifest/ShipNode/ShipNodePersonInfo/@AddressLine5"/><br/>
			<% } 
			addressLine =getValue("Manifest","xml:/Manifest/ShipNode/ShipNodePersonInfo/@AddressLine6");
			if(!isVoid(addressLine)){%>
				<yfc:getXMLValue name="Manifest" binding="xml:/Manifest/ShipNode/ShipNodePersonInfo/@AddressLine6"/><br/>
			<% } %>
			<yfc:getXMLValue name="Manifest" binding="xml:/Manifest/ShipNode/ShipNodePersonInfo/@City"/> &nbsp;
			<yfc:getXMLValue name="Manifest" binding="xml:/Manifest/ShipNode/ShipNodePersonInfo/@State"/> &nbsp;
			<yfc:getXMLValue name="Manifest" binding="xml:/Manifest/ShipNode/ShipNodePersonInfo/@ZipCode"/></br>
			<yfc:getXMLValue name="Manifest" binding="xml:/Manifest/ShipNode/ShipNodePersonInfo/@Country"/>
			</font>
		</td>
		<td class="protectedtext" align="left">
			<font face="Arial Narrow" size="2">
			<b><yfc:i18n>Manifest_#</yfc:i18n></b> &nbsp; 
				<yfc:getXMLValue name="Manifest" binding="xml:/Manifest/@ManifestNo"/> <br/>
			<b><yfc:i18n>Manifest_Date</yfc:i18n></b> &nbsp; 
				<yfc:getXMLValue name="Manifest" binding="xml:/Manifest/@ManifestDate"/> <br/>
			</font>
		</td>
	</tr>
	<tr><td colspan="2">&nbsp;&nbsp;&nbsp;</td></tr>
	<tr>
		<td colspan="2" >
			<font face="Arial Narrow" size="2">
			<table width="100%" border="2"  cellspacing="0" cellpadding="0">
				<tr> 
					<td class="protectedtext"><b><yfc:i18n>Container_#</yfc:i18n></b></td>
					<td class="protectedtext"><b><yfc:i18n>Reference_#</yfc:i18n></b></td>
					<td class="protectedtext"><b><yfc:i18n>Tracking_#</yfc:i18n></b></td>
					<td class="protectedtext"><b><yfc:i18n>Consignee_Address</yfc:i18n></b></td>
					<td class="protectedtext"><b><yfc:i18n>Weight</yfc:i18n></b></td>
					<td class="protectedtext"><b><yfc:i18n>Charge</yfc:i18n></b></td>
				</tr>
				<yfc:loopXML name="Manifest" binding="xml:/Manifest/Shipments/@Shipment" id="Shipment" > 
			    <yfc:loopXML  binding="xml:/Shipment/Containers/@Container" id="Container" > 
				<tr>
					<td class="protectedtext">
						<yfc:getXMLValue name="Container" binding="xml:/Container/@ContainerNo"/><BR />
					</td>
					<td class="protectedtext">
						<yfc:getXMLValue name="Container" binding="xml:/Container/@ShipmentContainerKey"/><BR />  
					</td>
					<td class="protectedtext">
						<yfc:getXMLValue name="Container" binding="xml:/Container/@TrackingNo"/><BR />  
					</td>
					<td class="protectedtext">
						<yfc:getXMLValue name="Shipment" binding="xml:/Shipment/ToAddress/@FirstName"/> &nbsp;
						<yfc:getXMLValue name="Shipment" binding="xml:/Shipment/ToAddress/@LastName"/> <br/>
						<% addressLine =getValue("Shipment","xml:/Shipment/ToAddress/@AddressLine1");
						if(!isVoid(addressLine)){%>
							<yfc:getXMLValue name="Shipment" binding="xml:/Shipment/ToAddress/@AddressLine1"/><br/>
						<% } 
						addressLine =getValue("Shipment","xml:/Shipment/ToAddress/@AddressLine2");
						if(!isVoid(addressLine)){%>
							<yfc:getXMLValue name="Shipment" binding="xml:/Shipment/ToAddress/@AddressLine2"/><br/>
						<% } 
						addressLine =getValue("Shipment","xml:/Shipment/ToAddress/@AddressLine3");
						if(!isVoid(addressLine)){%>
							<yfc:getXMLValue name="Shipment" binding="xml:/Shipment/ToAddress/@AddressLine3"/><br/>
						<% } 
						addressLine =getValue("Shipment","xml:/Shipment/ToAddress/@AddressLine4");
						if(!isVoid(addressLine)){%>
							<yfc:getXMLValue name="Shipment" binding="xml:/Shipment/ToAddress/@AddressLine4"/><br/>
						<% } 
						addressLine =getValue("Shipment","xml:/Shipment/ToAddress/@AddressLine5");
						if(!isVoid(addressLine)){%>
							<yfc:getXMLValue name="Shipment" binding="xml:/Shipment/ToAddress/@AddressLine5"/><br/>
						<% } 
						addressLine =getValue("Shipment","xml:/Shipment/ToAddress/@AddressLine6");
						if(!isVoid(addressLine)){%>
							<yfc:getXMLValue name="Shipment" binding="xml:/Shipment/ToAddress/@AddressLine6"/><br/>
						<% } %>
						<yfc:getXMLValue name="Shipment" binding="xml:/Shipment/ToAddress/@City"/> 
						<yfc:getXMLValue name="Shipment" binding="xml:/Shipment/ToAddress/@State"/> 
						<yfc:getXMLValue name="Shipment" binding="xml:/Shipment/ToAddress/@ZipCode"/> 
					</td>
					<td class="protectedtext">
						<yfc:getXMLValue name="Container" binding="xml:/Container/@ContainerGrossWeight"/>&nbsp;
						<yfc:getXMLValue name="Container" binding="xml:/Container/@ContainerGrossWeightUOM"/> 
					</td>
					<td class="protectedtext">
						<yfc:getXMLValue name="Container" binding="xml:/Container/@ActualFreightCharge"/><BR /> 
					</td>
				</tr>
				</yfc:loopXML> 
				</yfc:loopXML> 
			</table>
			</font>
		</td>
	</tr>
</table>
