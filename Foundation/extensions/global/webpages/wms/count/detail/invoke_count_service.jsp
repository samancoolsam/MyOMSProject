<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/wmsim.js"></script>
<script language="javascript">

function callSaveFn() {

	var containerForm= document.all("containerform");
	if(document.all("xml:/AgentCriteria/@IsClicked").value != "Y"){
		var hiddenCallApi = document.createElement("<INPUT type='hidden' name='xml:/AgentCriteria/@CallApi' value='Y'/>");
		document.all("xml:/AgentCriteria/@IsClicked").value="Y";
		containerForm.insertBefore(hiddenCallApi);	

	yfcChangeDetailView(getCurrentViewId());	
	}
	else
	{
		alert(WMSMSG146);
		return false;
	}
	
	
}
</script>

<table class="view" width="100%" >
<jsp:include page="/yfsjspcommon/common_fields.jsp" flush="true">
		<jsp:param name="ScreenType" value="detail"/>
		<jsp:param name="ShowDocumentType" value="true"/>
		<jsp:param name="ShowNode" value="true"/>
		<jsp:param name="NodeBinding" value="xml:/AgentCriteria/@Node"/>
        <jsp:param name="RefreshOnNode" value="true"/>
		<jsp:param name="ShowEnterpriseCode" value="false"/>

</jsp:include>
<tr>
	<td class="detaillabel" >
        <yfc:i18n>Agent_Criteria</yfc:i18n>
    </td>
   <td nowrap="true">
		<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/AgentCriteria/@AgentCriteriaId","xml:/AgentCriteria/@AgentCriteriaId")%> />
		<img class="lookupicon" name="search" onclick="callListLookup(this,'countprogram',
		'&xml:/AgentCriteria/@TransactionKey=<%=resolveValue("EXECUTE_COUNT_PROGRAM")%>')" 
		<%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Agent_Criteria") %> />
    </td>
</tr>
<tr>
    <td>
        <input type="hidden" name="xml:/AgentCriteria/@IsClicked" value='<%=request.getParameter("xml:/AgentCriteria/@IsClicked")%>'/>
	</td>
</tr>
<tr>
    <td colspan="8" align="center">
       <input class="button" type="button" value="<%=getI18N("Proceed")%>" onclick="callSaveFn();" />
    </td>
</tr>
<% 
String sAgentCriteria = resolveValue("xml:/AgentCriteria/@AgentCriteriaId");
if(equals(resolveValue("xml:/AgentCriteria/@CallApi"),"Y") &&!isVoid(sAgentCriteria)){%>
<% 	
	YFCElement elemTriggerService = YFCDocument.parse("<RequestExecCntPrgm  criteria=\""+sAgentCriteria +"\" Node=\""+ resolveValue("xml:/AgentCriteria/@Node")+"\" createAdditionalCountRequest=\"Y\" />").getDocumentElement();
	 %>
 <yfc:callAPI serviceName="RequestExecCntPrgm" inputElement="<%=elemTriggerService%>" 
					 outputNamespace="outRequestExecCntPrgm"/>  
<%}
%>
</table> 