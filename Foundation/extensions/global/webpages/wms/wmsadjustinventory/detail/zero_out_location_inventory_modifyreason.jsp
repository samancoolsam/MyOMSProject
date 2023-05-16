<%@include file="/yfsjspcommon/yfsutil.jspf" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/wmsim.js"></script>

<%	
YFCElement organizationInput = null;
	
		organizationInput = YFCDocument.parse("<Organization OrganizationCode=\"" + resolveValue("xml:/ZeroOutLocationInventory/@EnterpriseCode") + "\" />").getDocumentElement();
	 
YFCElement organizationTemplate = YFCDocument.parse("<OrganizationList> <Organization InventoryOrganizationCode=\"\"/> </OrganizationList>").getDocumentElement(); 
%>
<yfc:callAPI apiName="getOrganizationList" inputElement="<%=organizationInput%>" templateElement="<%=organizationTemplate%>" outputNamespace="OrganizationList"/>


<table width="100%" class="view">
<tr>
    <td class="detaillabel">
        <yfc:i18n>WMS_Reason_Code</yfc:i18n>
    </td>
    <td>
        <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/ZeroOutLocationInventory/Audit/@ReasonCode","xml:/ZeroOutLocationInventory/Audit/@ReasonCode")%> />
		<img class="lookupicon" name="search" onclick="callListLookup(this,'adjustreasoncode','&xml:/AdjustmentReason/@Node=<%=resolveValue("xml:/ZeroOutLocationInventory/@Node")%>&xml:/AdjustmentReason/@EnterpriseCode=<%=resolveValue("xml:OrganizationList:/OrganizationList/Organization/@InventoryOrganizationCode")%>')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Reason_Code") %> />
    </td>
	<td>
		</td>
		<td>
		</td>
		<td>
		</td>
		<td>
		</td>
</tr> 
<tr>    
    <td class="detaillabel">
        <yfc:i18n>Reason_Text</yfc:i18n>
    </td>
    <td rowspan="3">
        <textarea class="unprotectedtextareainput" rows="3" cols="35" 																		<%=getTextOptions("xml:/ZeroOutLocationInventory/Audit/@ReasonText","xml:/ZeroOutLocationInventory/Audit/@ReasonText")%>></textarea>
    </td>
	<td>
		</td>
		<td>
		</td>
		<td>
		</td>
		<td>
		</td>
</tr>
</table>