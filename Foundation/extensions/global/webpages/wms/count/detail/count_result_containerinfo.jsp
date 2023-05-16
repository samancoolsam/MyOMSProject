<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.util.*" %>
<%@ include file="/console/jsp/modificationutils.jspf" %>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/im.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/wmsim.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<div style="width:862px;overflow:auto">
	<table  editable="false" class="table">
		<thead>
			<tr>
				<td class="checkboxheader" sortable="no">
					<input type="checkbox" name="checkbox" value="checkbox" onclick="doCheckAll(this);"/>
				</td>
				<td class="tablecolumnheader"><yfc:i18n>Location</yfc:i18n></td>
				<td class="tablecolumnheader"><yfc:i18n>Organization</yfc:i18n></td>
				<td class="tablecolumnheader"><yfc:i18n>Parent_Case_ID</yfc:i18n></td>
				<td class="tablecolumnheader"><yfc:i18n>Pallet_ID</yfc:i18n></td>
				<td class="tablecolumnheader"><yfc:i18n>Case_ID</yfc:i18n></td>				
				<td class="tablecolumnheader"><yfc:i18n>Variance_Type</yfc:i18n></td>
				<td class="tablecolumnheader"><yfc:i18n>Count_Entered_By</yfc:i18n></td>
				<td class="tablecolumnheader"><yfc:i18n>Count_Entered_Date</yfc:i18n></td>
			</tr>
		</thead>
		<tbody>
		<yfc:loopXML binding="xml:/CountResultList/@SummaryResultList" id="SummaryResultList">
			<yfc:loopXML binding="xml:/SummaryResultList/@CountResult" id="CountResult">
			<% if(( !isVoid(resolveValue("xml:/SummaryResultList/CountResult/@CaseId"))
			        ||!isVoid(resolveValue("xml:/SummaryResultList/CountResult/@PalletId")))&(isVoid(resolveValue("xml:/SummaryResultList/CountResult/@ItemID")))) {%>
				<tr>					
				<yfc:makeXMLInput name="containerresultkey">
					<yfc:makeXMLKey binding="xml:/CountRequest/@EnterpriseCode" 											value="xml:/CountResult/@OrganizationCode" />
					<yfc:makeXMLKey binding="xml:/CountRequest/@Node" 
						value="xml:/CountResult/@Node" />
					<yfc:makeXMLKey binding="xml:/CountRequest/@LocationId" 
						value="xml:/SummaryResultList/@LocationId" />
						<%if(!isVoid(resolveValue("xml:/CountResult/@CaseId"))){%>
					<yfc:makeXMLKey binding="xml:/CountRequest/@CaseId" 
						value="xml:/CountResult/@CaseId" />
						<%}else if(isVoid(resolveValue("xml:/CountResult/@CaseId"))&& !isVoid(resolveValue("xml:/CountResult/@PalletId"))){%>
					<yfc:makeXMLKey binding="xml:/CountRequest/@PalletId"
						value="xml:/CountResult/@PalletId" />
						<%}%>
					<yfc:makeXMLKey binding="xml:/CountRequest/@ItemID"
						value="xml:/CountResult/@ItemID" />
					<yfc:makeXMLKey binding="xml:/CountRequest/@UnitOfMeasure" 
						value="xml:/CountResult/@UnitOfMeasure" />
					<yfc:makeXMLKey binding="xml:/CountRequest/@ProductClass" 
						value="xml:/CountResult/@ProductClass" />
				</yfc:makeXMLInput>
				<yfc:makeXMLInput name="varianceKey">					
					<yfc:makeXMLKey binding="xml:/CountRequest/CountResultList/CountResult/@CountResultKey" 	value="xml:/CountResult/@CountResultKey" />
					<yfc:makeXMLKey binding="xml:/CountRequest/@SummaryTaskId" value="xml:/CountResult/@SummaryTaskId" />
					<yfc:makeXMLKey binding="xml:/CountRequest/@CountRequestKey" value="xml:/CountResult/@CountRequestKey" />
					<yfc:makeXMLKey binding="xml:/CountRequest/@Node" value="xml:/CountResult/@Node" />
				</yfc:makeXMLInput>
				
				<td class="checkboxcolumn">
					<input type="checkbox" name="VarianceKey" yfcMultiSelectCounter='<%=SummaryResultListCounter.toString()+CountResultCounter.toString()%>' yfcMultiSelectValue1='<%=getValue("CountResult", "xml:/CountResult/@CountResultKey")%>'
					yHiddenInputName1="VarianceKey_<%=SummaryResultListCounter.toString()+CountResultCounter.toString()%>" 
					yHiddenInputName2="ContainerResultKey_<%=SummaryResultListCounter.toString()+CountResultCounter.toString()%>" />
		            <input type="hidden" value='<%=getParameter("varianceKey")%>' name="VarianceKey_<%=SummaryResultListCounter.toString()+CountResultCounter.toString()%>" />
					<input type="hidden" value='<%=getParameter("containerresultkey")%>' name="ContainerResultKey_<%=SummaryResultListCounter.toString()+CountResultCounter.toString()%>"/> 			
	
				</td>				
				<td class="tablecolumn"><yfc:getXMLValue binding="xml:/CountResult/@LocationId"/></td>
				<td class="tablecolumn"><yfc:getXMLValue binding="xml:/CountResult/@OrganizationCode"/></td>
				<td class="tablecolumn"><yfc:getXMLValue binding="xml:/CountResult/@ParentCaseId"/></td>
				<td class="tablecolumn"><yfc:getXMLValue binding="xml:/CountResult/@PalletId"/></td>
				<td class="tablecolumn"><yfc:getXMLValue binding="xml:/CountResult/@CaseId"/></td>
				<td class="tablecolumn"><yfc:getXMLValue binding="xml:/CountResult/@VarianceType"/></td>
				<td class="tablecolumn"><yfc:getXMLValue binding="xml:/CountResult/@Createuserid"/></td>
				<td class="tablecolumn"><yfc:getXMLValue binding="xml:/CountResult/@Createts"/></td>
			   </tr>	
			   <%}%>
			</yfc:loopXML>
		</yfc:loopXML>
	</tbody>
</table>
</div>
