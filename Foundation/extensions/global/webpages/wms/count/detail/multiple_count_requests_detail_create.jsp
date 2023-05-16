<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@include file="/console/jsp/currencyutils.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.util.*" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/taskmanagement.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/wmsim.js"></script>
<%
	YFCDate nowDate = new YFCDate();	
	String ReqType= (String)getParameter("requestType");
%>
<script language="javascript">
yfcDoNotPromptForChanges(true); 
function validateRangeNew(view){		
	key = "<Location";	
	var temp = document.all("xml:/CountRequest/@ToLocation").value;				
	var temp2 = document.all("xml:/CountRequest/@FromLocation").value;
	var temp3 = document.all("xml:/Location/@Node").value;
	var temp4 = document.all("xml:/CountRequest/@ZoneId").value;
	var temp5 = document.all("xml:/CountRequest/@AisleNumber").value;
	var temp6 = document.all("xml:/CountRequest/@BayNumber").value;
	var temp7 = document.all("xml:/CountRequest/@LevelNumber").value;
	if(window.document.getElementById("locRadio").checked){
		if(temp=="" && temp2=="" && temp4==""){
			alert("Invalid_Criteria");
			return false;
		}
		key += " FromLocation =\""+temp2 +"\" ToLocation =\""+temp+"\" Node =\""+temp3+"\" ZoneId =\""+temp4+"\"/>";			
	}else if(window.document.getElementById("aisleRadio").checked){			
		if(temp5=="" && temp6=="" && temp7=="" && temp4==""){
			alert("Invalid_Criteria");
			return false;
		}
		key += " ZoneId =\""+temp4+"\" AisleNumber =\""+temp5+"\" BayNumber =\""+temp6+"\" LevelNumber =\""+temp7+"\" Node =\""+temp3+"\"/>";	
	}		
	yfcShowDetailPopupWithParams(view, "", "550", "275", "","count", key,"");
	return true;				
}

function resetForInvalidLocations(){
	if(document.all("xml:/AgentCriteria/@IsClicked").value == "Y"){
		document.all("xml:/AgentCriteria/@IsClicked").value="N";
	}
}

function sendMessageToJMS(){
	var containerForm= document.all("containerform");
	if(document.all("xml:/AgentCriteria/@IsClicked").value != "Y"){
			var hiddenCallApi = document.createElement("<Input type='hidden' name='xml:/AgentCriteria/@CallApi' value='Y'/>");
			document.all("xml:/AgentCriteria/@IsClicked").value="Y";
			containerForm.insertBefore(hiddenCallApi);
			yfcChangeDetailView(getCurrentViewId());
		}else{
			alert(WMSMSG146);
			return false;
		}
}

function showAisleBayLevel(){
	window.document.getElementById("Aisle").style.display = "";
	window.document.getElementById("Location").style.display = "none";
}

function hideAisleBayLevel(){
	window.document.getElementById("Location").style.display = "";
	window.document.getElementById("Aisle").style.display = "none";
}

</script>
<script language="javascript">
	window.attachEvent("onload", setCorrectStyle);
	function setCorrectStyle(){
		if(window.document.getElementById("aisleRadio")){
			if(window.document.getElementById("aisleRadio").checked){
				showAisleBayLevel();
			}
		}
	}
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
<tr>
</tr>

<tr>
	<yfc:callAPI apiID="AP1"/>
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
	<yfc:callAPI apiID="AP2"/>
	<td class="detaillabel" ><yfc:i18n>Priority</yfc:i18n></td>
	<td nowrap="true">
        <select name="xml:/CountRequest/@Priority" class="combobox">
                <yfc:loopOptions binding="xml:Priority:/CommonCodeList/@CommonCode" 
                    name="CodeShortDescription" isLocalized="Y" value="CodeValue" selected="3"/>
        </select>
    </td>
	<td class="detaillabel" >
        <yfc:i18n>Requesting_User_ID</yfc:i18n>
    </td>
    <td nowrap="true">
        <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/CountRequest/@RequestingUserId","xml:CurrentUser:/User/@Loginid")%> />
    </td>
</tr>
<tr>
	<td class="detaillabel" >
        <yfc:i18n>Start_No_Earlier_Than</yfc:i18n>
    </td>
	<td nowrap="true" >
		<input class="dateinput" type="text" <%=getTextOptions("xml:/CountRequest/@StartNoEarlierThan_YFCDATE","xml:/CountRequest/@StartNoEarlierThan_YFCDATE",nowDate.getString(getLocale().getDateFormat()))%>/>
		<img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
		<input class="dateinput" type="text" <%=getTextOptions("xml:/CountRequest/@StartNoEarlierThan_YFCTIME", "xml:/CountRequest/@StartNoEarlierThan_YFCTIME")%>/>
		<img class="lookupicon" name="search" onclick="invokeTimeLookup(this);return false" <%=getImageOptions(YFSUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup") %> />
	</td>	
	<td class="detaillabel" >
        <yfc:i18n>Finish_No_Later_Than</yfc:i18n>
    </td>
    <td nowrap="true" >
        <input class="dateinput" type="text" <%=getTextOptions("xml:/CountRequest/@FinishNoLaterThan_YFCDATE","xml:/CountRequest/@FinishNoLaterThan_YFCDATE")%>/>
        <img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
		<input class="dateinput" type="text" <%=getTextOptions("xml:/CountRequest/@FinishNoLaterThan_YFCTIME","xml:/CountRequest/@FinishNoLaterThan_YFCTIME")%>/>
		<img class="lookupicon" name="search" onclick="invokeTimeLookup(this);return false" <%=getImageOptions(YFSUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup") %>/>
	</td>
	<td class="detaillabel" >
        <yfc:i18n>Zone</yfc:i18n>
    </td>
    <td nowrap="true">
		<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/CountRequest/@ZoneId","xml:/CountRequest/@ZoneId")%> />
        <img class="lookupicon" onclick="callListLookup(this,'zone','&xml:/Zone/@Node=<%=resolveValue("xml:CommonFields:/CommonFields/@Node")%>')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Zone") %> />
    </td>
</tr>

<tr>
</tr>
	
<tr>
	<td colspan="2">
		<table class="view" height="100%" width="100%" >
		<tr>
			<td class="detaillabel">
				<input type="radio" id="locRadio" checked="true" onclick="hideAisleBayLevel()" <%=getRadioOptions("xml:/CountProgram/@CountProgramLevel","xml:/CountProgram/@CountProgramLevel","Y")%>>
				</input>
			</td>
			<td class="detaillabel" align="right">
				<yfc:i18n>By_Location</yfc:i18n>
			</td>
		</tr>
		<tr>
			<td class="detaillabel">
				<input type="radio" id="aisleRadio" onclick="showAisleBayLevel()" <%=getRadioOptions("xml:/CountProgram/@CountProgramLevel","xml:/CountProgram/@CountProgramLevel","N")%>>
				</input>
			</td>
			<td class="detaillabel">
				<yfc:i18n>By_Aisle_Bay_Level</yfc:i18n>
			</td>
		</tr>
		</table>
	</td>
	
	<td colspan="4">
	<table class="view" height="100%" width="100%" >
	<tr id="Location" style="display:">
		<td class="detaillabel" >
			<yfc:i18n>From_Location</yfc:i18n>
		</td>
		<td nowrap="true">
			<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/CountRequest/@FromLocation","xml:/CountRequest/@FromLocation")%> />
			<img class="lookupicon" onclick="callLookup(this,'location','xml:/Location/@Node=<%=resolveValue("xml:CommonFields:/CommonFields/@Node")%>', 'xml:/Location/@ZoneId=<%=resolveValue("xml:/CountRequest/@ZoneId")%>')" 
			<%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Location") %> />
		</td>
		<td class="detaillabel" >
			<yfc:i18n>To_Location</yfc:i18n>
		</td>
		<td nowrap="true">
			<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/CountRequest/@ToLocation","xml:/CountRequest/@ToLocation")%> />
			<img class="lookupicon" onclick="callLookup(this,'location','xml:/Location/@Node=<%=resolveValue("xml:CommonFields:/CommonFields/@Node")%>', 'xml:/Location/@ZoneId=<%=resolveValue("xml:/CountRequest/@ZoneId")%>')" 
			<%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Location") %> />
		</td>
	</tr>
	<tr style="display:" height="20px">
			<td/><td/><td/><td/>
	</tr>
	<tr id="Aisle" style="display:none">
		<td class="detaillabel" nowrap="true" sortable="no" style="width:<%=getUITableSize("xml:/CountRequest/@AisleNumber")%>">
        <yfc:i18n>Aisle_Number</yfc:i18n>
		</td>
		<td nowrap="true">
			<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/CountRequest/@AisleNumber","xml:/CountRequest/@AisleNumber")%> />			
		</td>
		<td class="detaillabel" nowrap="true" sortable="no" style="width:<%=getUITableSize("xml:/CountRequest/@BayNumber")%>">
			<yfc:i18n>Bay_Number</yfc:i18n>
		</td>
		<td nowrap="true">
			<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/CountRequest/@BayNumber","xml:/CountRequest/@BayNumber")%> />			
		</td>
		<td class="detaillabel" nowrap="true" sortable="no" style="width:<%=getUITableSize("xml:/CountRequest/@LevelNumber")%>">
			<yfc:i18n>Level_Number</yfc:i18n>
		</td>
		<td nowrap="true">
			<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/CountRequest/@LevelNumber","xml:/CountRequest/@LevelNumber")%> />		   
		</td>
	</tr>
	</table>
	</td>
</tr>
<tr>
    <td>
        <input type="hidden" name="xml:/AgentCriteria/@IsClicked" value='<%=request.getParameter("xml:/AgentCriteria/@IsClicked")%>'/>
	</td>
</tr>
<tr>
	<td colspan="8" align="center">
		<input class="button" type="button" value="<%=getI18N("Create_Count_Request")%>" onclick="sendMessageToJMS();" />
	</td>
</tr>
<input type="hidden" name="xml:/CountRequest/@Node" value='<%=resolveValue("xml:CommonFields:/CommonFields/@Node")%>'/>
<input type="hidden" name="xml:/Location/@Node" value='<%=resolveValue("xml:CommonFields:/CommonFields/@Node")%>'/>
	<%if(equals(resolveValue("xml:/AgentCriteria/@CallApi"),"Y")){
		String sNode = request.getParameter("xml:/CountRequest/@Node");
		String sFromLocation = request.getParameter("xml:/CountRequest/@FromLocation");
		String sToLocation = request.getParameter("xml:/CountRequest/@ToLocation");
		String sAisleNumber = request.getParameter("xml:/CountRequest/@AisleNumber");
		String sBayNumber = request.getParameter("xml:/CountRequest/@BayNumber");
		String sLevelNumber = request.getParameter("xml:/CountRequest/@LevelNumber");
		String sZoneId = request.getParameter("xml:/CountRequest/@ZoneId");		
		String sCountProgmLevel = request.getParameter("xml:/CountProgram/@CountProgramLevel");		
		YFCDocument inDoc = YFCDocument.createDocument("Location");
		YFCElement inElem = inDoc.getDocumentElement();
		if(!isVoid(sZoneId)){
			inElem.setAttribute("ZoneId", sZoneId);	
		}
		if(equals(sCountProgmLevel,"Y")){
			if(!isVoid(sFromLocation) && !isVoid(sToLocation)){
				inElem.setAttribute("FromLocationId", sFromLocation);
				inElem.setAttribute("ToLocationId", sToLocation);
				inElem.setAttribute("LocationIdQryType", "BETWEEN");
			}
		}else if((equals(sCountProgmLevel,"N"))){
			if(!isVoid(sAisleNumber) || !isVoid(sBayNumber) || !isVoid(sLevelNumber)){
				inElem.setAttribute("AisleNumber", sAisleNumber);
				inElem.setAttribute("BayNumber", sBayNumber);
				inElem.setAttribute("LevelNumber", sLevelNumber);
			}
		}
		inElem.setAttribute("Node", sNode);		
		YFCElement template = YFCDocument.parse("<Locations TotalNumberOfRecords=\"\"/>").getDocumentElement();		
		%>
			<yfc:callAPI apiName="getLocationList" inputElement="<%=inElem%>" templateElement="<%=template%>" outputNamespace="LocationList"/>
		<%
		String sTotalNumberOfRecords = "";
		YFCElement apiOutput = (YFCElement)request.getAttribute("LocationList");		
		if(!isVoid(apiOutput)){
			sTotalNumberOfRecords = apiOutput.getAttribute("TotalNumberOfRecords","0");			
		}
		if(equals(sTotalNumberOfRecords,"0")){%>
			<script language="javascript">
				alert("Invalid Locations Passed");
				resetForInvalidLocations();
			</script>
		<%}else if(equals(sTotalNumberOfRecords,"")){
			//do nothing, backend will throw error here
		}else{
			YFCDocument doc = YFCDocument.createDocument("Message");
			YFCElement JMSMessageElement = doc.getDocumentElement();
			JMSMessageElement.setAttribute("FlowName", "COUNT_REQ_FOR_LOCN_RANGE");
			JMSMessageElement.setAttribute("TransactionKey", "COUNT_REQ_FOR_LOCN_RANGE");
			YFCElement AgentDetailsElement = JMSMessageElement.createChild("AgentDetails");
			YFCElement MessageXMLElement = AgentDetailsElement.createChild("MessageXml");
			MessageXMLElement.setAttribute("DocumentType", "3001");
			MessageXMLElement.setAttribute("Action", "Get");			
			YFCElement countRequestElem = getRequestDOM().getChildElement("CountRequest");			
			if(countRequestElem != null){
				if(equals(sCountProgmLevel, "Y")){
					countRequestElem.removeAttribute("AisleNumber");
					countRequestElem.removeAttribute("BayNumber");
					countRequestElem.removeAttribute("LevelNumber");
				}else if(equals(sCountProgmLevel, "N")){
					countRequestElem.removeAttribute("FromLocation");
					countRequestElem.removeAttribute("ToLocation");
				}
				MessageXMLElement.setAttributes(countRequestElem.getAttributes());
			}
		%>
			<yfc:callAPI serviceName="CreateCountRequestForLocationRange" inputElement="<%=JMSMessageElement%>" 
				 outputNamespace="outCreateCountRequestForLocationRange"/> 
		<%}		
	}%>
</table>