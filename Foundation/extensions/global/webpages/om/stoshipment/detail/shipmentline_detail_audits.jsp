<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@include file="shipment_audit_include.jspf"%>
<div style="height:130px;overflow:auto">
<table class="table" width="100%">
<thead>
    <tr> 
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/AuditList/Audit/@Modifyts")%>" nowrap="true">
            <yfc:i18n>Date</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/AuditList/Audit/@Modifyts")%>">
            <yfc:i18n>Modified_By</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/AuditList/Audit/AuditDetail/@HostInfo")%>">
            <yfc:i18n>Host_Info</yfc:i18n>
        </td>
		<td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/AuditList/Audit/AuditDetail/@AuditType")%>" nowrap="true">
            <yfc:i18n>Context</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/AuditList/Audit/AuditDetail/@AuditType")%>">
            <yfc:i18n>Line_#</yfc:i18n>
        </td>
		
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/AuditList/Audit/AuditDetail/Attributes/Attribute/@Name")%>">
            <yfc:i18n>Modification</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/AuditList/Audit/AuditDetail/Attributes/Attribute/@OldValue")%>">
            <yfc:i18n>Old_Value</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/AuditList/Audit/AuditDetail/Attributes/Attribute/@NewValue")%>">
            <yfc:i18n>New_Value</yfc:i18n>
        </td>
		<td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/AuditList/Audit/@ReaonCode")%>">
            <yfc:i18n>Reason_Code</yfc:i18n>
        </td>
		<td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/AuditList/Audit/@ReasonText")%>">
            <yfc:i18n>Reason_Text</yfc:i18n>
        </td>
    </tr>
</thead>
<tbody>
<%
 massageAuditListForShipment((YFCElement) request.getAttribute("LineAuditList"), null);
%>
    <yfc:loopXML binding="xml:LineAuditList:/AuditList/@Audit" id="Audit1" > 
<%
		String sNameToShow = "", sOldValue="", sNewValue="";
%>
		<yfc:loopXML name="Audit1" binding="xml:/Audit/AuditDetail/Attributes/@Attribute" id="Attribute">
<% 				
		sNameToShow = sNameToShow + resolveValue("xml:/Attribute/@Name") + "<BR>";
		sOldValue = sOldValue + resolveValue("xml:/Attribute/@OldValue") + "<BR>" ;
		sNewValue = sNewValue + resolveValue("xml:/Attribute/@NewValue") + "<BR>";			 					
%>
		</yfc:loopXML>
	    <tr>
			<td class="tablecolumn" nowrap="true">
                <yfc:getXMLValueI18NDB name="Audit1" 
				binding="xml:/Audit/@Modifyts"/>
            </td>       
			<td class="tablecolumn">
				<yfc:getXMLValueI18NDB name="Audit1" 
				binding="xml:/Audit/@Modifyuserid"/>
			</td>
			<td class="tablecolumn">
				<yfc:getXMLValueI18NDB name="Audit1" 
				binding="xml:/Audit/AuditDetail/@HostInfo"/>
			</td>
			<td class="tablecolumn" nowrap="true">
				<yfc:getXMLValue name="Audit1"
				binding="xml:/Audit/AuditDetail/@AuditType"/>
			</td>
            <td class="tablecolumn" style="width:<%= getUITableSize("xml:/AuditList/Audit/AuditDetail/@AuditType")%>">			
				<yfc:loopXML name="Audit1" binding="xml:/Audit/AuditDetail/IDs/@ID" id="ID" > 
	<% 
					String sName1=resolveValue("xml:/ID/@Name");
                    String sNew=resolveValue("xml:/ID/@Value");
					if("ShipmentLineNo".equals(sName1)){						
	%>
						<%=sNew%><BR>
	<%
					}					
	%>
			   </yfc:loopXML> 		
		    </td> 			
			<td class="tablecolumn">				
					<%=sNameToShow%>
			</td>
			<td class="tablecolumn">            
					<%=sOldValue%>            
			</td>
			<td class="tablecolumn">            
					<%=sNewValue%>
			</td>
			<td class="tablecolumn">
			    <yfc:getXMLValue name="Audit1" 
				binding="xml:/Audit/@ReasonCode"/>
			</td>
			<td class="tablecolumn">
			    <yfc:getXMLValue name="Audit1" 
				binding="xml:/Audit/@ReasonText"/>
			</td>
		</tr>

	</yfc:loopXML> 
</tbody>
</table>
</div>