<%@include file="/yfsjspcommon/yfsutil.jspf" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/wmsim.js"></script>

<%	String itemid = 	resolveValue("xml:/NodeInventory/LocationInventoryList/LocationInventory/InventoryItem/@ItemID");
YFCElement organizationInput = null;
	if (!isVoid(itemid)) { 
		organizationInput = YFCDocument.parse("<Organization OrganizationCode=\"" + resolveValue("xml:/NodeInventory/LocationInventoryList/LocationInventory/InventoryItem/@OrganizationCode") + "\" />").getDocumentElement();
	} else {
		organizationInput = YFCDocument.parse("<Organization OrganizationCode=\"" + resolveValue("xml:/AdjustLocationInventory/@EnterpriseCode") + "\" />").getDocumentElement();
	} 
YFCElement organizationTemplate = YFCDocument.parse("<OrganizationList> <Organization InventoryOrganizationCode=\"\"/> </OrganizationList>").getDocumentElement(); 
%>
<yfc:callAPI apiName="getOrganizationList" inputElement="<%=organizationInput%>" templateElement="<%=organizationTemplate%>" outputNamespace="OrganizationList"/>

<% String bindingPrefix = request.getParameter("Binding"); %>
<table width="100%" class="view">
<tr>
    <td class="detaillabel">
        <yfc:i18n>WMS_Reason_Code</yfc:i18n>
    </td>
    <td>
        <input type="text" class="unprotectedinput" <%=getTextOptions(buildBinding(bindingPrefix,"/@ReasonCode",""))%> />
        <input type="hidden" class="protectedinput" name="Status" value="" />
		<img class="lookupicon" name="search" onclick="callReasonCodeLookup('xml:/AdjustLocationInventory/Audit/@ReasonCode','xml:/AdjustLocationInventory/Audit/@ImplyIncrement','xml:/AdjustLocationInventory/Audit/@ImplyDecrement','adjustreasoncode','&xml:/AdjustmentReason/@Node=<%=resolveValue("xml:/NodeInventory/@Node")%>&xml:/AdjustmentReason/@EnterpriseCode=<%=resolveValue("xml:OrganizationList:/OrganizationList/Organization/@InventoryOrganizationCode")%>', 'Status'); setDefaultComboOption('Status');" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Reason_Code") %> />
    </td>
</tr> 
<tr>    
    <td class="detaillabel">
        <yfc:i18n>Reason_Text</yfc:i18n>
    </td>
    <td rowspan="3">
        <textarea class="unprotectedtextareainput" rows="3" cols="35" 																		<%=getTextAreaOptions(buildBinding(bindingPrefix,"/@ReasonText",""))%>>
		</textarea>
    </td>
</tr>
</table>
