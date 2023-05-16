<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/moverequest.js"></script>
<%boolean bUpdateCount = (YFCCommon.equals(resolveValue("xml:/Task/@TaskStatus"), "2000") && !YFCCommon.equals(resolveValue("xml:/CountRequest/@Status"), "2000") ); 
//Count Request is not completed, task is completed 
%> 
<%String sPureLPN = "N";%>
<div style="height:150px;overflow:auto">
<table width="100%" border="0" cellspacing="0"     class="table">
<thead>
    <tr>
		<td class="checkboxheader" sortable="no" ><br />
        </td>
		<td sortable="no" class="tablecolumnheader" nowrap="true"   >
            <yfc:i18n>Enterprise</yfc:i18n>
        </td>
		<td sortable="no" class="tablecolumnheader"  nowrap="true"  >
            <yfc:i18n>Pallet_ID</yfc:i18n>
        </td>
        <td sortable="no" class="tablecolumnheader"  nowrap="true"  >
            <yfc:i18n>Case_ID</yfc:i18n>
        </td>
		<td sortable="no" class="tablecolumnheader" nowrap="true"  >
            <yfc:i18n>Variance_Type</yfc:i18n>
        </td>
	</tr>
</thead>
<tbody>

<%if(!bUpdateCount){%>
	<yfc:loopXML binding="xml:Inventory:/NodeInventory/LPNList/@LPN" id="LPN" > 
    <tr>
	    
		<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_99"+LPNCounter+"/@PalletId", "xml:/LPN/@PalletId")%>/>
	    <input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_99"+LPNCounter+"/@CaseId", "xml:/LPN/@CaseId")%>/>	   
		<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_99"+LPNCounter+"/@OrganizationCode", "xml:/LPN/@OrganizationCode")%>/>
	    <td class="tablecolumn"></td>
		<td class="tablecolumn"><yfc:getXMLValue  binding="xml:/LPN/@OrganizationCode"/></td>     
		<td class="tablecolumn"> <yfc:getXMLValue binding="xml:/LPN/@PalletId"/></td>
        <td class="tablecolumn"><yfc:getXMLValue binding="xml:/LPN/@CaseId"/></td>
		<td class="tablecolumn" >
		<%String sVarianceType="xml:/RecordCountResult/CountResult_99"+LPNCounter+"/@VarianceType";%>
			 <select class="combobox" name="<%=sVarianceType%>">
               <option value="No Variance">No Variance
               <option value="Missing">Missing
	         </select>
	    </td>    
    </tr>
    </yfc:loopXML> 			
	<%}else{%>
		<yfc:loopXML binding="xml:CountResultList:CountResultList/@SummaryResultList" id="SummaryResultList" > 
		<% sPureLPN = resolveValue("xml:/SummaryResultList/CountResult/@PureLPN"); 
		System.out.println("sPureLPN @@@@@@@@@@"+sPureLPN);%>
		<% if(equals(sPureLPN,"Y")) {%>
    <tr>
		
		<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_11"+SummaryResultListCounter+"/@PalletId", "xml:/SummaryResultList/CountResult/@PalletId")%>/>
	    <input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_11"+SummaryResultListCounter+"/@CaseId", "xml:/SummaryResultList/CountResult/@CaseId")%>/>	   
		<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_11"+SummaryResultListCounter+"/@OrganizationCode", "xml:/SummaryResultList/CountResult/@OrganizationCode")%>/>
		<input type="hidden" <%=getTextOptions("xml:/RecordCountResult/CountResult_11"+SummaryResultListCounter+"/@VarianceType", "xml:/SummaryResultList/CountResult/@VarianceType")%>/>
		
	    <td class="tablecolumn"></td>
		<td class="tablecolumn"><yfc:getXMLValue  binding="xml:/SummaryResultList/CountResult/@OrganizationCode"/></td>     
		<td class="tablecolumn"> <yfc:getXMLValue binding="xml:/SummaryResultList/CountResult/@PalletId"/></td>
        <td class="tablecolumn"><yfc:getXMLValue binding="xml:/SummaryResultList/CountResult/@CaseId"/></td>
		<td class="tablecolumn"><yfc:getXMLValue binding="xml:/SummaryResultList/CountResult/@VarianceType"/></td>		
    </tr>
	<%}%>
    </yfc:loopXML> 		
	<%}%>

	
</tbody>
<% 
	if(!YFCCommon.equals((String)request.getAttribute("SaveAllowed"),"N") ){%>

<tfoot>
    <tr style='display:none' TemplateRow="true">
      	 <td class="checkboxcolumn" >
        </td>
		<td class="tablecolumn" nowrap="true">
			 <select class="combobox" name="xml:/RecordCountResult/CountResult_1/@OrganizationCode">
					<yfc:loopOptions binding="xml:EnterpriseList:/OrganizationList/@Organization" name="OrganizationCode"
                value="OrganizationCode"/>
			</select>
		</td>
		<td nowrap="true" class="tablecolumn"  >
			<input type="text" class="unprotectedinput"  <%=getTextOptions("xml:/RecordCountResult/CountResult_1/@PalletId")%>/> 
        </td>
        <td nowrap="true" class="tablecolumn">
            <input type="text" class="unprotectedinput"  <%=getTextOptions("xml:/RecordCountResult/CountResult_1/@CaseId")%>/> 
        </td>		
  		
		<td class="tablecolumn" >
			 <select class="combobox" name="xml:/RecordCountResult/CountResult_1/@VarianceType">
				<option value="New">New
	         </select>
        </td>
		
	</tr>
	<tr>
    	<td nowrap="true" colspan="15">
    		<jsp:include page="/common/editabletbl.jsp" flush="true"/>
    	</td>
    </tr>
</tfoot>
<%}%>
</table>
</div>
