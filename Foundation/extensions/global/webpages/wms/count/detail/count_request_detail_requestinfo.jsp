<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@include file="/console/jsp/currencyutils.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.util.*" %>
<%int iCount=0;%>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/countrequest.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/taskmanagement.js"></script>
<table width="100%" class="view">
<%if(!isVoid(resolveValue("xml:/CountRequest/@CountRequestKey"))){%>
<tr>
	<td class="detaillabel" ><yfc:i18n>Node</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountRequest/@Node" />
        <input type="hidden" <%=getTextOptions("xml:/CountRequest/@Node","xml:/CountRequest/@Node")%> />
    </td>
	<td class="detaillabel" ><yfc:i18n>Enterprise</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountRequest/@EnterpriseCode" />
        <input type="hidden" <%=getTextOptions("xml:/CountRequest/@EnterpriseCode","xml:/CountRequest/@EnterpriseCode")%> />
    </td>
	<td class="detaillabel" ><yfc:i18n>Count_Request_#</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountRequest/@CountRequestNo" />
        <input type="hidden" <%=getTextOptions("xml:/CountRequest/@CountRequestNo","xml:/CountRequest/@CountRequestNo")%> />
    </td>		
</tr>
<tr>
	<td class="detaillabel" ><yfc:i18n>Count_Program_Name</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountRequest/@CountProgramName" />
        <input type="hidden" <%=getTextOptions("xml:/CountRequest/@CountProgramName","xml:/CountRequest/@CountProgramName")%> />
    </td>	
	<td class="detaillabel" ><yfc:i18n>Request_Type</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountRequest/@RequestTypeDescription" />
    </td>	
	
	<td class="detaillabel" ><yfc:i18n>Pipeline_ID</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValueI18NDB binding="xml:/CountRequest/Pipeline/@PipelineId" />
    </td>	
</tr>
<tr>
	<td class="detaillabel" ><yfc:i18n>Status</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValueI18NDB binding="xml:/CountRequest/Status/@Description" />
    </td>
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
	<yfc:makeXMLInput name="countrequestkey">
		<yfc:makeXMLKey binding="xml:/CountRequest/@CountRequestNo" value="xml:/CountRequest/@CountRequestNo" />
		<yfc:makeXMLKey binding="xml:/CountRequest/@CountRequestKey" value="xml:/CountRequest/@CountRequestKey" />
		<yfc:makeXMLKey binding="xml:/CountRequest/@Node" value="xml:/CountRequest/@Node" />
		<yfc:makeXMLKey binding="xml:/CountRequest/@DocumentType" value="xml:/CountRequest/@DocumentType" />		
	</yfc:makeXMLInput>
	<td class="checkboxcolumn">
		<input type="hidden" value='<%=getParameter("countrequestkey")%>' name="countRequestKey"/>
	</td>	
</tr>
<%}else{%>
<jsp:include page="/yfsjspcommon/common_fields.jsp" flush="true">
		<jsp:param name="ScreenType" value="detail"/>
		<jsp:param name="ShowNode" value="true"/>
		<jsp:param name="ShowEnterpriseCode" value="true"/>
		<jsp:param name="EnterpriseCodeBinding" value="xml:/CountRequest/@EnterpriseCode"/>
		<jsp:param name="ShowDocumentType" value="false"/>
		<jsp:param name="NodeBinding" value="xml:/CountRequest/@Node"/>
		<jsp:param name="RefreshOnNode" value="true"/>
		<jsp:param name="RefreshOnEnterpriseCode" value="false"/>
        <jsp:param name="EnterpriseListForNodeField" value="true"/>
</jsp:include>
<yfc:makeXMLInput name="countrequestkey">
		<yfc:makeXMLKey binding="xml:/CountRequest/@CountRequestNo" value="xml:/CountRequest/@CountRequestNo" />
		<yfc:makeXMLKey binding="xml:/CountRequest/@CountRequestKey" value="xml:/CountRequest/@CountRequestKey" />
		<yfc:makeXMLKey binding="xml:/CountRequest/@Node" value="xml:/CountRequest/@Node" />
		<yfc:makeXMLKey binding="xml:/CountRequest/@DocumentType" value="xml:/CountRequest/@DocumentType" />		
	</yfc:makeXMLInput>
	<td class="checkboxcolumn">
		<input type="hidden" value='<%=getParameter("countrequestkey")%>' name="countRequestKey"/>
	</td>	
<tr>
	<td class="detaillabel" >
        <yfc:i18n>Count_Request_#</yfc:i18n>
    </td>
    <td nowrap="true">
        <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/CountRequest/@CountRequestNo","")%> />
    </td>	
	<td/>
	<td/>
	<input type="hidden" name="xml:/CountRequest/@Node" value='<%=resolveValue("xml:CommonFields:/CommonFields/@Node")%>'/>	
</tr>
<%}%>
<input type="hidden" name="xml:/CountRequest/@CountCancellationReasonCode"/>
<input type="hidden" name="xml:/CountRequest/@CountReasonText"/>
<input type="hidden" name="xml:/CountRequest/@CountRequestKey" 												value='<%=resolveValue("xml:/CountRequest/@CountRequestKey")%>' />
</table>
