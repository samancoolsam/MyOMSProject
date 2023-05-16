<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.util.*" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript">
yfcDoNotPromptForChanges(true); 
<%
	String crKey= resolveValue("xml:/CountRequest/@CountRequestKey");
	String crNode= resolveValue("xml:/CountRequest/@Node");
	String ReqType= (String)getParameter("requestType");
    if((!isVoid(crKey))){
	YFCDocument crDoc = YFCDocument.createDocument("CountRequest");
	crDoc.getDocumentElement().setAttribute("CountRequestKey",resolveValue("xml:/CountRequest/@CountRequestKey"));
	crDoc.getDocumentElement().setAttribute("Node",resolveValue("xml:/CountRequest/@Node"));

%>
	function changeToDetailView() {
          entityType = "count";
		  showDetailFor('<%=crDoc.getDocumentElement().getString(false)%>');
    }
	window.attachEvent("onload", changeToDetailView);

<%}%>
</script>
<table width="100%" class="view">
<jsp:include page="/yfsjspcommon/common_fields.jsp" flush="true">
		<jsp:param name="ScreenType" value="detail"/>
		<jsp:param name="ShowNode" value="true"/>
		<jsp:param name="ShowEnterpriseCode" value="true"/>
		<jsp:param name="ShowDocumentType" value="false"/>
		<jsp:param name="NodeBinding" value="xml:/CountRequest/@Node"/>
		<jsp:param name="RefreshOnNode" value="true"/>
		<jsp:param name="EnterpriseCodeBinding" value="xml:/CountRequest/@EnterpriseCode"/>
		<jsp:param name="NodeBinding" value="xml:/CountRequest/@Node"/>
		<jsp:param name="EnterpriseListForNodeField" value="true"/>
		<jsp:param name="RefreshOnEnterpriseCode" value="true"/>
		<jsp:param name="AcrossEnterprisesAllowed" value="true"/>
		
</jsp:include>
	<yfc:callAPI apiID="AP1"/>
	<td class="detaillabel" >
        <yfc:i18n>Count_Request_#</yfc:i18n>
    </td>
    <td nowrap="true">
        <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/CountRequest/@CountRequestNo","xml:/CountRequest/@CountRequestNo")%> />
    </td>
	<td class="detaillabel" >
        <yfc:i18n>Request_Type</yfc:i18n>
    </td>
 <%if(equals(ReqType,"PHYSICAL-COUNT")){%>
	 <td nowrap="true">
	 <select name="xml:/CountRequest/@RequestType" class="combobox" disabled >
			<yfc:loopOptions binding="xml:RequestType:/CommonCodeList/@CommonCode" 
				name="CodeShortDescription" value="CodeValue" selected="PHYSICAL-COUNT" isLocalized="Y"/>
	 </select>
    </td>
	<input type="hidden" name="xml:/CountRequest/@RequestType" value='PHYSICAL-COUNT'/>
 <%}else if(equals(ReqType,"CYCLE-COUNT")){%>
	<td nowrap="true">
	 <select name="xml:/CountRequest/@RequestType" class="combobox" disabled >
			<yfc:loopOptions binding="xml:RequestType:/CommonCodeList/@CommonCode" 
				name="CodeShortDescription" value="CodeValue" selected="DEFAULT" isLocalized="Y"/>
	 </select>
    </td>
	<input type="hidden" name="xml:/CountRequest/@RequestType" value='DEFAULT'/>
 <%}else{%>
	<td nowrap="true">
		<select name="xml:/CountRequest/@RequestType" class="combobox" >
			<yfc:loopOptions binding="xml:RequestType:/CommonCodeList/@CommonCode" 
				name="CodeShortDescription" value="CodeValue" selected="xml:/CountRequest/@RequestType" isLocalized="Y"/>
		</select>
    </td>
	<%}%>
</tr>
<input type="hidden" name="xml:/CountRequest/@Node" value='<%=resolveValue("xml:CommonFields:/CommonFields/@Node")%>'/>
</table>