<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.util.*" %>
<%@ page import="com.yantra.wms.util.WMSNumberFormat" %>
<%@ include file="/console/jsp/modificationutils.jspf" %>
<%@ include file="/yfsjspcommon/yfsutil.jspf"%>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/countrequest.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/modificationreason.js"></script>
<div style="width:980px;overflow:auto">
<table  editable="false" class="table">
	<thead>
		<tr>
			<td class="checkboxheader" sortable="no">
					<input type="checkbox" name="checkbox" value="checkbox" onclick="doCheckAll(this);"/>
			</td>
			<td class="tablecolumnheader" style="width:30px" sortable="no"><yfc:i18n>Details</yfc:i18n></td>
			<td class="tablecolumnheader" sortable="no"><yfc:i18n>Organization</yfc:i18n></td>
			<td class="tablecolumnheader" sortable="no"><yfc:i18n>Location</yfc:i18n></td>		
			<td class="tablecolumnheader" sortable="no"><yfc:i18n>Original_Location</yfc:i18n></td>	
			<td class="tablecolumnheader" sortable="no"><yfc:i18n>Parent_Case_ID</yfc:i18n></td>	
			<td class="tablecolumnheader" sortable="no"><yfc:i18n>Pallet_ID</yfc:i18n></td>				
			<td class="tablecolumnheader" sortable="no"><yfc:i18n>Case_ID</yfc:i18n></td>				
			<td class="tablecolumnheader" sortable="no"><yfc:i18n>Item_ID</yfc:i18n></td>
			<td class="tablecolumnheader" sortable="no"><yfc:i18n>Description</yfc:i18n></td>
			<td class="tablecolumnheader" sortable="no"><yfc:i18n>PC</yfc:i18n></td>
			<td class="tablecolumnheader" sortable="no"><yfc:i18n>UOM</yfc:i18n></td>			
			<td class="tablecolumnheader" sortable="no"><yfc:i18n>Net_Variance_Quantity</yfc:i18n></td>
			<td class="tablecolumnheader" sortable="no"><yfc:i18n>Net_Variance_Value</yfc:i18n></td>
			<td class="tablecolumnheader" sortable="no"><yfc:i18n>Currency</yfc:i18n></td>				
		</tr>
	</thead>
	<tbody>
	<%String className="oddrow";%>
		<yfc:loopXML binding="xml:/CountResultList/@SummaryResultList" id="SummaryResultList">
		    
			<% if( !isVoid(resolveValue("xml:/SummaryResultList/@ItemID"))){ %>
			
			<tr class='<%=className%>'>
				<yfc:makeXMLInput name="SummaryResultKey">
					<yfc:makeXMLKey binding="xml:/CountRequest/@EnterpriseCode" value="xml:/SummaryResultList/@OrganizationCode" />
					<yfc:makeXMLKey binding="xml:/CountRequest/@Node" 
						value="xml:/SummaryResultList/CountResult/@Node" />
					<yfc:makeXMLKey binding="xml:/CountRequest/@LocationId" 
						value="xml:/SummaryResultList/@LocationId" />
					<yfc:makeXMLKey binding="xml:/CountRequest/@OriginalLocation" 
						value="xml:/SummaryResultList/CountResult/@OriginalLocation" />
						<%if(!isVoid(resolveValue("xml:/SummaryResultList/CountResult/@CaseId"))){%>
					<yfc:makeXMLKey binding="xml:/CountRequest/@CaseId" 
						value="xml:/SummaryResultList/CountResult/@CaseId" />
						<%}else if(isVoid(resolveValue("xml:/SummaryResultList/CountResult/@CaseId"))&& !isVoid(resolveValue("xml:/SummaryResultList/CountResult/@PalletId"))){%>
					<yfc:makeXMLKey binding="xml:/CountRequest/@PalletId" value="xml:/SummaryResultList/CountResult/@PalletId" />
						<%}%>
					<yfc:makeXMLKey binding="xml:/CountRequest/@ItemID" 
						value="xml:/SummaryResultList/@ItemID" />
					<yfc:makeXMLKey binding="xml:/CountRequest/@UnitOfMeasure" 
						value="xml:/SummaryResultList/@UnitOfMeasure" />
					<yfc:makeXMLKey binding="xml:/CountRequest/@ProductClass" 
						value="xml:/SummaryResultList/@ProductClass" />					
					<yfc:makeXMLKey binding="xml:/CountRequest/@RequestType" 
						value="xml:/SummaryResultList/CountResult/CountRequest/@RequestType" />					
					<yfc:makeXMLKey binding="xml:/CountRequest/@CountProgramName" 
						value="xml:/SummaryResultList/CountResult/CountRequest/@CountProgramName" />
					<yfc:makeXMLKey binding="xml:/CountRequest/@OriginalCountRequestNo" 
						value="xml:/SummaryResultList/CountResult/CountRequest/@CountRequestNo" />
					<yfc:makeXMLKey binding="xml:/CountRequest/@OriginalTaskId" 
						value="xml:/SummaryResultList/CountResult/@TaskId" />
				</yfc:makeXMLInput>
				<td class="checkboxcolumn">
					<input type="checkbox" value='<%=getParameter("SummaryResultKey")%>' onclick="doCheckAllLocal(this);" name="createCountKey"/>
				</td>					

				<td>
				<img onclick="expandCollapseDetails('optionSet_<%=SummaryResultListCounter%>','<%=getI18N("Click_To_See_Details")%>','<%=getI18N("Click_To_Hide_Details")%>','<%=YFSUIBackendConsts.FOLDER_COLLAPSE%>','<%=YFSUIBackendConsts.FOLDER_EXPAND%>')"  style="cursor:hand" <%=getImageOptions(YFSUIBackendConsts.FOLDER,"Click_To_See_Details")%> />
				</td>
				<td class="tablecolumn">
					<yfc:getXMLValue binding="xml:/SummaryResultList/@OrganizationCode"/>
				</td>
				<td class="tablecolumn">
					<yfc:getXMLValue binding="xml:/SummaryResultList/@LocationId"/>
				</td>
				<% if (isVoid(resolveValue("xml:/SummaryResultList/CountResult/@SerialNo"))) { %>
				<td class="tablecolumn">
					<yfc:getXMLValue binding="xml:/SummaryResultList/CountResult/@OriginalLocation"/>
				</td>
				<% } %>
				<td class="tablecolumn">
					<yfc:getXMLValue binding="xml:/SummaryResultList/CountResult/@ParentCaseId"/>
				</td>
				<td class="tablecolumn">
					<yfc:getXMLValue binding="xml:/SummaryResultList/CountResult/@PalletId"/>
				</td>
				<td class="tablecolumn">
					<yfc:getXMLValue binding="xml:/SummaryResultList/CountResult/@CaseId"/>
				</td>
				<td class="tablecolumn">
					<yfc:getXMLValue binding="xml:/SummaryResultList/@ItemID"/>
				</td>
				<td class="tablecolumn">
					<yfc:getXMLValue binding="xml:/SummaryResultList/CountResult/Item/@Description"/>
				</td>
				<td class="tablecolumn">
					<yfc:getXMLValue binding="xml:/SummaryResultList/@ProductClass"/>
				</td>
				<td class="tablecolumn">
					<yfc:getXMLValue binding="xml:/SummaryResultList/@UnitOfMeasure"/>
				</td>			
				<td class="numerictablecolumn">
				<% String netVarQty = WMSNumberFormat.getUnsignedValue(resolveValue("xml:/SummaryResultList/@NetVarianceQuantity")); 
				double dnetVarQty = Double.parseDouble(netVarQty);%>	
				<%=getLocalizedStringFromDouble(getLocale(), dnetVarQty)%>
				<%=WMSNumberFormat.getSign(resolveValue("xml:/SummaryResultList/@NetVarianceQuantity"))%>
				</td>
				<td class="numerictablecolumn">
				<% String netVarValue = WMSNumberFormat.getUnsignedValue(resolveValue("xml:/SummaryResultList/@NetVarianceValue")); 
				double dnetVarValue = Double.parseDouble(netVarValue);%>
				<%=getLocalizedStringFromDouble(getLocale(), dnetVarValue)%>
				<%=WMSNumberFormat.getSign(resolveValue("xml:/SummaryResultList/@NetVarianceValue"))%>
				</td>
				<td class="numerictablecolumn">
				   <yfc:getXMLValue   binding="xml:/SummaryResultList/CountResult/@Currency"/>
				</td>
				
		        <tr id='<%="optionSet_"+SummaryResultListCounter%>' class='<%=className%>' 
					style="display:none" name="Damd">
					<td colspan="13" >
						<jsp:include page="/wms/count/detail/count_detail_includeresults.jsp" flush="true">
							<jsp:param name="optionSetBelongingToLine" value='<%=String.valueOf(SummaryResultListCounter)%>'/>
						</jsp:include>
					</td>
				</tr>
			</tr>
		<%}%>
		</yfc:loopXML>
	</tbody>
</table>
</div>