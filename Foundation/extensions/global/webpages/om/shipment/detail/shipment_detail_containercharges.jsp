<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ include file="/console/jsp/modificationutils.jspf" %>

<table class="table" cellspacing="0" width="100%">
<thead>
    <tr>
        <td class="tablecolumnheader"><yfc:i18n>Container_#</yfc:i18n></td>
		<td class="numerictablecolumnheader"> <yfc:i18n>Actual_Freight_Charge</yfc:i18n></td>   
    </tr>
</thead>
<tbody>
    <yfc:loopXML name="Shipment" binding="xml:/Shipment/Containers/@Container" id="Container"> 
    <tr>
        <td class="tablecolumn">
			<yfc:getXMLValue binding="xml:/Container/@ContainerNo"/>
            <input type="hidden" <%=yfsGetTextOptions("xml:/Shipment/Containers/Container_"  + ContainerCounter + "/@ShipmentContainerKey" ,"xml:/Container/@ShipmentContainerKey", "xml:/Shipment/AllowedModifications")%>/>
        </td>
        <td class="numerictablecolumn">
            <yfc:getXMLValue binding="xml:/CurrencyList/Currency/@PrefixSymbol"/>&nbsp;
                <input type="text" <%=yfsGetTextOptions("xml:/Shipment/Containers/Container_"  + ContainerCounter + "/@ActualFreightCharge" ,"xml:/Container/@ActualFreightCharge", "xml:/Shipment/AllowedModifications")%>/>
                &nbsp;<yfc:getXMLValue binding="xml:/CurrencyList/Currency/@PostfixSymbol"/>
        </td>
    </tr>                                                        
    </yfc:loopXML>
</tbody>
</table>
