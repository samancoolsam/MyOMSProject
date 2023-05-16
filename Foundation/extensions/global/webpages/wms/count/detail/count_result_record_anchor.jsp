<%@ include file="/yfsjspcommon/yfsutil.jspf" %>
<%	YFCElement root1 = getRequestDOM();
	YFCElement recordResultElem = root1.getChildElement("RecordCountResult");

	//Code below disables save button depending on the status of the Task
	String sSaveAllowed = "Y";
	boolean bAllowModForUpdate = false;
	boolean bTaskCompleted = false;
	String sTaskStatus = "";
	String sParentTaskIdFromCount = "";

	boolean bIsLatestSummaryTask = true;

	YFCElement taskElem = (YFCElement)request.getAttribute("Task");
	if (taskElem != null)
	{ 
		sTaskStatus = taskElem.getAttribute("TaskStatus");
		if(YFCCommon.equals(sTaskStatus,"2000")){
			bTaskCompleted = true;
		}

		sParentTaskIdFromCount = taskElem.getAttribute("ParentTaskId");//68124
	}

	YFCElement countReqElement = (YFCElement)request.getAttribute("Count");
	YFCElement allowedModsElem = countReqElement.getChildElement("AllowedModifications");

		 YFCNodeList allowedModList = allowedModsElem.getElementsByTagName("Modification");
		 if(allowedModList!=null){
			 for(int i=0; i<allowedModList.getLength(); i++){
				YFCElement allowedModElem = (YFCElement)allowedModList.item(i);
				String sModificationType = allowedModElem.getAttribute("ModificationType");
				boolean bThroughOverride = allowedModElem.getBooleanAttribute("ThroughOverride");
				if(equals(sModificationType, "UPDATE_COUNT") && !bThroughOverride){
					bAllowModForUpdate = true;
					break;
				}
			 }
		 }
		//68124
		YFCElement taskListElem = YFCDocument.createDocument("Task").getDocumentElement();
		taskListElem.setAttribute("IsSummaryTask", "Y");
		YFCElement taskRefElement = taskListElem.createChild("TaskReferences");
		taskRefElement.setAttribute("CountRequestKey", countReqElement.getAttribute("CountRequestKey"));

		YFCElement taskListTemplElem = YFCDocument.createDocument("Task").getDocumentElement();
		taskListTemplElem.setAttribute("Modifyts", "");
		taskListTemplElem.setAttribute("IsSummaryTask", "");
		taskListTemplElem.setAttribute("TaskId", "");
		
		%><yfc:callAPI apiName="getTaskList" inputElement='<%=taskListElem%>' 
			templateElement='<%=taskListTemplElem%>' 
			outputNamespace="GetTaskList"/><%

			YFCElement getTaskListElem = (YFCElement) request.getAttribute("GetTaskList");

			YFCDate modifyDateOfCurrentSummary = null;
			YFCDate modifyDateOfOtherSummary = null;
			YFCNodeList taskElemsList = getTaskListElem.getElementsByTagName("Task");

			for(int i=0;i<taskElemsList.getLength();i++){
				YFCElement taskElement=(YFCElement)taskElemsList.item(i);
				String sSummaryTask = taskElement.getAttribute("TaskId");
				if(YFCCommon.equals(sSummaryTask, sParentTaskIdFromCount)){
					modifyDateOfCurrentSummary = taskElement.getDateTimeAttribute("Modifyts");
				}
			}

			for(int i=0;i<taskElemsList.getLength();i++){
				YFCElement taskElement=(YFCElement)taskElemsList.item(i);
				String sSummaryTask = taskElement.getAttribute("TaskId");
				if(!YFCCommon.equals(sSummaryTask, sParentTaskIdFromCount)){
					modifyDateOfOtherSummary = taskElement.getDateTimeAttribute("Modifyts");
				}
				if(modifyDateOfOtherSummary!=null ){
					if(modifyDateOfOtherSummary.compareTo(modifyDateOfCurrentSummary) > 0){
						bIsLatestSummaryTask = false;
						break;
					}
				}
			}

		if(!bTaskCompleted){
			bAllowModForUpdate=true;
		}

	if(YFCCommon.equals(sTaskStatus,"9000") || !bAllowModForUpdate || !bIsLatestSummaryTask){
		sSaveAllowed="N";
	}

	request.setAttribute("SaveAllowed",sSaveAllowed);
	request.setAttribute("RecordCountResult",recordResultElem);

%>
<script language="Javascript">
function mandateCountAttrEntry() {
	if (document.all("xml:/TaskList/@UserId") == null || document.all("xml:/TaskList/@UserId").value == null || document.all("xml:/TaskList/@UserId").value =="") {
		alert(YFCMSG087);//User Id is Mandatory
		return false;
	}
	if (processRecordCountResults()) {
		if (document.all("MandateCountAttr")) {
			var oMandateCountAttr = document.all("MandateCountAttr");
			if (oMandateCountAttr.value == 1) {
				return checkEnteredValue();
			}
		}

		
	<%	if( YFCCommon.equals(sTaskStatus, "2000") || YFCCommon.equals(sTaskStatus, "9000")){ %>
			alert(YFCMSG091);
			return false;
	<%	} %>

		return true;
	
	}
	return false;
}
function mandateCountAttrEntryForUpdate() {
	if (document.all("xml:/TaskList/@UserId") == null || document.all("xml:/TaskList/@UserId").value == null || document.all("xml:/TaskList/@UserId").value =="") {
		alert(YFCMSG087);//User Id is Mandatory
		return false;
	}
	if (processRecordCountResults()) {
		if (document.all("MandateCountAttr")) {
			var oMandateCountAttr = document.all("MandateCountAttr");
			if (oMandateCountAttr.value == 1) {
				return checkEnteredValue();
			}
		}
		return true;
	}
	return false;
}

function checkEnteredValue() {
	var childNodes = window.document.getElementById("CountResult");    
    var trNodes = childNodes.getElementsByTagName("TR");
    for (var i=0;i<trNodes.length;i++){
        var trNode = trNodes.item(i);
		var tdNodes = trNode.getElementsByTagName("SELECT");
		for (var j=0;j<tdNodes.length;j++){
			var tdNode = tdNodes.item(j);
			if (tdNode.name.indexOf('@ProductClass') > 0 || tdNode.name.indexOf('@InventoryStatus') > 0) {
				if (tdNode.value == '') {
					alert(YFCMSG046);
					return false;
				}
			}
		}
    }
	return true;
}

function processRecordCountResults()
{
	<% if(YFCCommon.equals(sSaveAllowed,"Y")) {%>
		processSaveRecordsForCountResults();
		return true;
	<% } else { %>
		alert(YFCMSG091);//You cannot modify a completed or cancelled count taskrequest
		return false;
	<%}%>
}

function mandateInventoryAttrs()
{

}
</script>
<%if(!isVoid(resolveValue("xml:Count:/CountRequest/@EnterpriseCode"))){%>
	<yfc:callAPI apiID='AP7'/>
<%}%>
<table class="anchor" cellpadding="7px"  cellSpacing="0">
 <tr>
	<td colspan="3" valign="top">
		<jsp:include page="/yfc/innerpanel.jsp" flush="true" >
			<jsp:param name="CurrentInnerPanelID" value="I01"/>
		</jsp:include>
	</td>
</tr>
<tr>
     <td colspan="3" valign="top">
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I02"/>
        </jsp:include>
    </td>
</tr>
<tr>
     <td colspan="3" valign="top">
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I03"/>
        </jsp:include>
    </td>
</tr>
</table>
	