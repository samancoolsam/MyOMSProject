<%@include file="/yfsjspcommon/yfsutil.jspf"%>

<script>
	function proceedtopopup() {
		if(document.all("xml:/Task/@Proceed").value!="")
		{
			var sVal = document.all("CountKey");
			showDetailFor(sVal.value);  
			//var sKey = document.all("CountKey");
			//yfcShowDetailPopupWithParams('YWMD089', "", 900, 800, " ", 'count', sKey.value,"");
			//yfcShowDetailPopup('YWMD091', "", 900,800,"",'count', 'CountKey');
		}else{
			alert(YFCMSG089);//Task Info not Passed
		}

	}
	function callApi() {
		if((document.all("xml:/Task/@TaskId").value!="")||(document.all("xml:/Task/@SourceLocationId").value!=""))
		{
			var containerForm= document.all("containerform");
			var hiddenCallApi = document.createElement("<INPUT type='hidden' name='xml:/Task/@CallApi' value='Y'/>");
			containerForm.insertBefore(hiddenCallApi);
			yfcChangeDetailView(getCurrentViewId());
		}else{
			alert(YFCMSG089);//Task Info not Passed
		}
	}
</script>

<%if(resolveValue("xml:/Task/@CallApi")!=""){

	YFCElement inElem = null;
	YFCElement tempElem = null;
	YFCDocument inDoc = YFCDocument.createDocument("Task");
	inElem = inDoc.getDocumentElement();
	inElem.setAttribute("TaskId", 		
		resolveValue("xml:/Task/@TaskId"));
	inElem.setAttribute("TaskStatus", 		
		"2000");
	inElem.setAttribute("TaskStatusQryType", 		
		"LT");
	inElem.setAttribute("SourceLocationId", 
		resolveValue("xml:/Task/@SourceLocationId"));
	inElem.setAttribute("IsSummaryTask","N");
	tempElem = YFCDocument.parse("<TaskList><Task><TaskReferences/></Task></TaskList>").getDocumentElement();
%>
	<yfc:callAPI apiName="getTaskList" inputElement="<%=inElem%>" 
	templateElement="<%=tempElem%>" outputNamespace=""/>
	<input type="hidden" name="xml:/Task/@Proceed" value="Y"/>
	<yfc:makeXMLInput name="CountKey">
		<yfc:makeXMLKey binding="xml:/CountRequest/@TaskKey" value="xml:/TaskList/Task/@TaskKey"/>
		<yfc:makeXMLKey binding="xml:/CountRequest/@CountRequestKey" value="xml:/TaskList/Task/TaskReferences/@CountRequestKey"/>
	</yfc:makeXMLInput>
	<input name="CountKey" type="hidden" value='<%=getParameter("CountKey")%>'/>
	<%if(!(resolveValue("xml:/TaskList/Task/TaskReferences/@CountRequestKey")=="")){%>
	<script>
		proceedtopopup();
	</script>
<%}else{%>
	<script>
		alert(YFCMSG090);//Task Not found for Passed Input
	</script>
<%}
}%>
<table class="view" width="100%">
<tr>
	<td class="detaillabel"  nowrap="true" >
		<yfc:i18n>Task_ID</yfc:i18n> 
	</td>
	<td class="searchcriteriacell" nowrap="true"  >
		<input type="text" class="unprotectedinput"  <%=getTextOptions("xml:/Task/@TaskId","xml:/Task/@TaskId","xml:/Task/@TaskId")%> />
	</td>
	<td class="detaillabel" nowrap="true" >
		<yfc:i18n>Location</yfc:i18n> 
	</td>
	<td class="searchcriteriacell" nowrap="true" >
	    <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Task/@SourceLocationId","xml:/Task/@SourceLocationId","xml:/Task/@SourceLocationId")%> />
	    <img class="lookupicon" onclick="callLookup(this,'location',' ')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Location") %> />
	</td>
</tr>
<tr>
	<td colspan="8" align="center">
		<input class="button" type="button" value="<%=getI18N("Proceed")%>" onclick="callApi();" />
	</td>
</tr>	
</table>
