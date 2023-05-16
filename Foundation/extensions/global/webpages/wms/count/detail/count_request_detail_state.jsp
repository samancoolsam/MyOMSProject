<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@include file="/console/jsp/currencyutils.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.util.*" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/taskmanagement.js"></script>

<table width="100%" class="view">
<tr>
	
	<td class="detaillabel" ><yfc:i18n>Request_Type</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountRequest/@RequestType" />
    </td>	
	
	<td class="detaillabel" ><yfc:i18n>Pipeline_ID</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValueI18NDB binding="xml:/CountRequest/Pipeline/@PipelineId" />
    </td>	
	<td class="detaillabel" ><yfc:i18n>Status</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValueI18NDB binding="xml:/CountRequest/Status/@Description" />
    </td>
</tr>
<tr>
<% String sReasonCode = resolveValue("xml:/CountRequest/@CancellationReasonCode");
	if(!isVoid(sReasonCode)){ %>
		<td class="detaillabel" ><yfc:i18n>Cancellation_Reason_Code</yfc:i18n></td>
		<td class="protectedtext" nowrap="true">
			<yfc:getXMLValue binding="xml:/CountRequest/@CancellationReasonCode" />
		</td>	
	
	<%}%>
<% String sReasonText = resolveValue("xml:/CountRequest/@ReasonText");
	if(!isVoid(sReasonText)){ %>
		<td class="detaillabel" ><yfc:i18n>Reason_Text</yfc:i18n></td>
		<td class="protectedtext" nowrap="true">
			<yfc:getXMLValue binding="xml:/CountRequest/@ReasonText" />
		</td>	
<%}%>	
</tr>
</table>
