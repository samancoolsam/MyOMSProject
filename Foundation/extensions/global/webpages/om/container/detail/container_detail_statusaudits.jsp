<%@include file="/yfsjspcommon/yfsutil.jspf"%>

<table class="table" width="100%">
<thead>
    <tr> 
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerStatusAudits/ContainerStatusAudit/@CreateUserName")%>">
            <yfc:i18n>Modified_By</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerStatusAudits/ContainerStatusAudit/@OldStatus")%>">
            <yfc:i18n>Old_Status</yfc:i18n>
        </td>
		<td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerStatusAudits/ContainerStatusAudit/@OldStatusDate")%>">
            <yfc:i18n>Old_Status_Date</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerStatusAudits/ContainerStatusAudit/@NewStatus")%>">
            <yfc:i18n>New_Status</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerStatusAudits/ContainerStatusAudit/@NewStatusDate")%>">
            <yfc:i18n>New_Status_Date</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerStatusAudits/ContainerStatusAudit/@ReasonCode")%>">
            <yfc:i18n>Reason_Code</yfc:i18n>
        </td>
		<td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/Container/ContainerStatusAudits/ContainerStatusAudit/@ReasonText")%>">
            <yfc:i18n>Reason_Text</yfc:i18n>
        </td>
    </tr>
</thead>
<tbody>
    <yfc:loopXML name="Container" binding="xml:/Container/ContainerStatusAudits/@ContainerStatusAudit" id="ContainerStatusAudit" > 
		<tr>
			<td class="tablecolumn">
                <%
                    String sUser = resolveValue("xml:/ContainerStatusAudit/@CreateUserName");
                    if(isVoid(sUser) )
                        sUser = resolveValue("xml:/ContainerStatusAudit/@Createuserid");
                %>
                <%=sUser%>
            </td>       
			<td class="tablecolumn">
				<yfc:getXMLValueI18NDB name="ContainerStatusAudit" binding="xml:/ContainerStatusAudit/OldStatus/@Description"/>
			</td>
			<td class="tablecolumn">
				<yfc:getXMLValue name="ContainerStatusAudit" binding="xml:/ContainerStatusAudit/@OldStatusDate"/>
			</td>
			<td class="tablecolumn">
				<yfc:getXMLValueI18NDB name="ContainerStatusAudit" binding="xml:/ContainerStatusAudit/NewStatus/@Description"/>
			</td>
			<td class="tablecolumn">
				<yfc:getXMLValue name="ContainerStatusAudit" binding="xml:/ContainerStatusAudit/@NewStatusDate"/>
			</td>
			<td class="tablecolumn">
				<yfc:getXMLValue name="ContainerStatusAudit" binding="xml:/ContainerStatusAudit/@ReasonCode"/>
			</td>
			<td class="tablecolumn">
				<yfc:getXMLValue name="ContainerStatusAudit" binding="xml:/ContainerStatusAudit/@ReasonText"/>
			</td>
		</tr>
    </yfc:loopXML> 
</tbody>
</table>
