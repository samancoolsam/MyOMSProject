<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ include file="/console/jsp/modificationutils.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/modificationreason.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/om.js"></script>
<table class="view" width="100%">
<tr>
    <td class="detaillabel" ><yfc:i18n>Node</yfc:i18n></td>
    <td class="protectedtext"><yfc:getXMLValue binding="xml:/CountResult/@Node"/></td>
	<td class="detaillabel" ><yfc:i18n>Enterprise</yfc:i18n></td>
    <td class="protectedtext"><yfc:getXMLValue binding="xml:/CountResult/@OrganizationCode"/></td>
	<td class="detaillabel" ><yfc:i18n>Location</yfc:i18n></td>
	<td class="protectedtext"><yfc:getXMLValue binding="xml:/CountResult/@LocationId"/></td>

</tr>
<tr>
	
	<td class="detaillabel" ><yfc:i18n>Pallet_ID</yfc:i18n></td>
    <td class="protectedtext"><yfc:getXMLValue binding="xml:/CountResult/@PalletId"/></td>
	<td class="detaillabel" ><yfc:i18n>Case_ID</yfc:i18n></td>
    <td class="protectedtext"><yfc:getXMLValue binding="xml:/CountResult/@CaseId"/></td>
	<td class="detaillabel" ><yfc:i18n>Item_ID</yfc:i18n></td>
    <td class="protectedtext"><yfc:getXMLValue binding="xml:/CountResult/@ItemID"/></td>
</tr>

<tr>

	<td class="detaillabel" ><yfc:i18n>Item_Description</yfc:i18n></td>
    <td class="protectedtext"><yfc:getXMLValue binding="xml:/CountResult/@Description"/></td>
	<td class="detaillabel" ><yfc:i18n>Product_Class</yfc:i18n></td>
    <td class="protectedtext"><yfc:getXMLValue binding="xml:/CountResult/@ProductClass"/></td>

	<td class="detaillabel" ><yfc:i18n>Unit_Of_Measure</yfc:i18n></td>
    <td class="protectedtext"><yfc:getXMLValue binding="xml:/CountResult/@UnitOfMeasure"/></td> 
</tr>
</table>
