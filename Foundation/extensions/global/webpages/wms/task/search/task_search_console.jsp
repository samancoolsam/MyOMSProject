<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ include file="/console/jsp/modificationutils.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/om.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/taskmanagement.js"></script>
<%	String defaultParentOrg = "xml:/Task/@EnterpriseCode";	if(isShipNodeUser()){		defaultParentOrg = "xml:CurrentOrganization:/Organization/@PrimaryEnterpriseKey";		}%>
<table class="view">
	 <tr>
		<td>
			<input type="hidden" <%=getTextOptions("xml:/Task/@StartNoEarlierThanQryType1","xml:/Task/@StartNoEarlierThanQryType1","BETWEEN")%>/>
		</td>
    </tr>
	<jsp:include page="/yfsjspcommon/common_fields.jsp" flush="true">
		<jsp:param name="ShowDocumentType" value="false"/>
		<jsp:param name="ShowNode" value="true"/>
		<jsp:param name="RefreshOnNode" value="true"/>
		<jsp:param name="RefreshOnEnterpriseCode" value="true"/>
		<jsp:param name="NodeBinding" value="xml:/Task/@Node"/>
		<jsp:param name="EnterpriseCodeBinding" value="<%=defaultParentOrg%>"/>
	</jsp:include>
	<%if(!isVoid(resolveValue("xml:CommonFields:/CommonFields/@Node") ) )	{	%>
		<yfc:callAPI apiID="AP1"/>
	<%}%>

    <tr>
        <td class="searchlabel" >
			<table>
				<tr>
					<td class="searchcriteriacell" nowrap="true" colspan="2">
						<div class="portlet" style="height:162px;overflow:auto" >
							<table class="table" width="100%" cellspacing="0" cellpadding="0" border="0">
								<thead>
									<tr>
										<td class="checkboxheader" sortable="no">
							                <input type="checkbox" name="checkbox" value="checkbox" onclick="doCheckAll(this);"/>
										</td>
										<td class="tablecolumnheader" ><yfc:i18n>Task_Type</yfc:i18n>
										</td>
									</tr>
								</thead>
								<tbody>
									<%if(!isVoid(resolveValue("xml:CommonFields:/CommonFields/@Node") ) )	{	%>
										<yfc:loopXML binding="xml:/TaskTypeList/@TaskType" id="TaskType">
										<tr>
											<td>
												<% boolean foundInSavedSearch=false; %>
												<yfc:loopXML binding="xml:yfcSearchCriteria:/Task/TaskTypeList/@TaskType" id="SavedSearchTaskType">
													<% String zone = getValue("SavedSearchTaskType", "xml:/TaskType/@TaskType");
														if (equals(zone, getValue("TaskType", "xml:/TaskType/@TaskType"))) {
															foundInSavedSearch=true;
														}
													%>
												</yfc:loopXML>

												<% if (foundInSavedSearch) { %>
													<input yfcSuppressInitCheckBox="true" class="checkbox" checked type="checkbox" <%=getCheckBoxOptions("xml:/Task/EligibleTaskTypeList/EligibleTaskType_" + TaskTypeCounter + "/@TaskType","xml:/TaskType/@TaskType","xml:/TaskType/@TaskType")%> />
												<% } else { 
													String sTaskType = getValue("TaskType","xml:/TaskType/@TaskType");
													%>
													
													<input yfcSuppressInitCheckBox="true" class="checkbox" type="checkbox" <%=getCheckBoxOptions("xml:/Task/EligibleTaskTypeList/EligibleTaskType_" + TaskTypeCounter + "/@TaskType","xml:/Task/EligibleTaskTypeList/EligibleTaskType_" + TaskTypeCounter + "/@TaskType",sTaskType)%> />
												<% } %>
											</td>
											<td class="tablecolumn">
												<yfc:getXMLValueI18NDB binding="xml:/TaskType/@TaskTypeName"/>&nbsp;
											</td>
										</tr>
										</yfc:loopXML>
									<%}%>
								</tbody>
							</table>
						</div>
					</td>
				</tr>
			</table>
        </td>
    </tr>

	<tr>
        <td class="searchlabel" >
            <yfc:i18n>Start_No_Earlier_Than</yfc:i18n>
        </td>
    </tr>
	<tr>
        <td nowrap="true">
			<%	YFCDate oDate = new YFCDate(); 
				oDate.setEndOfDay();
			%>
            <input class="dateinput" type="text" <%=getTextOptions("xml:/Task/@FromStartNoEarlierThan_YFCDATE")%>/>
			<img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
			<input class="dateinput" type="text" <%=getTextOptions("xml:/Task/@FromStartNoEarlierThan_YFCTIME")%>/>
            <img class="lookupicon" name="search" onclick="invokeTimeLookup(this);return false" <%=getImageOptions(YFSUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup") %> />
            <yfc:i18n>To</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td>
            <input class="dateinput" type="text" <%=getTextOptions("xml:/Task/@ToStartNoEarlierThan_YFCDATE", "xml:/Task/@ToStartNoEarlierThan_YFCDATE", oDate.getString(getLocale(), false))%>/>
            <img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
			<input class="dateinput" type="text" <%=getTextOptions("xml:/Task/@ToStartNoEarlierThan_YFCTIME", "xml:/Task/@ToStartNoEarlierThan_YFCTIME", oDate.getString(getLocale().getTimeFormat()))%>/>
            <img class="lookupicon" name="search" onclick="invokeTimeLookup(this);return false"	<%=getImageOptions(YFSUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup") %> />
        </td>
    </tr>
</table>