<%@include file="/yfsjspcommon/yfsutil.jspf"%>

<table class="table" width="100%">
<thead>
    <tr> 
            <td class="tablecolumnheader"><yfc:i18n>Container_#</yfc:i18n></td>
			<td class="tablecolumnheader"><yfc:i18n>Status</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Container_Type</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Tracking_#</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Container_SCM</yfc:i18n></td>
	</tr>
</thead>
<tbody>
    <yfc:loopXML name="Container" binding="xml:/Container/ChildContainers/@ChildContainer" id="ChildContainer"  > 
            <tr>
                <yfc:makeXMLInput name="containerKey">
                    <yfc:makeXMLKey binding="xml:/Container/@ShipmentContainerKey" value="xml:/ChildContainer/@ShipmentContainerKey" />
                    <yfc:makeXMLKey binding="xml:/Container/@ShipmentKey" value="xml:/ChildContainer/@ShipmentKey" />
                </yfc:makeXMLInput>
                <td class="tablecolumn">
					<a <%=getDetailHrefOptions("L01",getParameter("containerKey"),"")%> >
						<yfc:getXMLValue binding="xml:/ChildContainer/@ContainerNo"/>
					</a>
				</td>
                <td class="tablecolumn">
                    <yfc:getXMLValueI18NDB binding="xml:/ChildContainer/Status/@Description"/>
                </td>
				<td class="tablecolumn">
					<yfc:getXMLValue binding="xml:/ChildContainer/@ContainerType"/>
				</td>
				<td class="tablecolumn">
					<yfc:getXMLValue binding="xml:/ChildContainer/@TrackingNo"/>
				</td>
				<td class="tablecolumn">
					<yfc:getXMLValue binding="xml:/ChildContainer/@ContainerScm"/>
				</td>
            </tr>
    </yfc:loopXML> 
</tbody>
</table>
 