<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@include file="/console/jsp/currencyutils.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.util.*" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/countrequest.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/taskmanagement.js"></script>
<script language="javascript">
	yfcDoNotPromptForChanges(true);
</script>
<%
   YFCElement enterpriseTemplate = YFCDocument.parse("<OrganizationList TotalNumberOfRecords=\"\"><Organization OrganizationCode=\"\" OrganizationKey=\"\" OrganizationName=\"\"/></OrganizationList>").getDocumentElement();
   YFCElement enterpriseInput = YFCDocument.parse("<Organization OrganizationKey=\""+resolveValue("xml:/Task/@EnterpriseKey")+"\"><RelatedOrgList><OrgEnterprise OrganizationKey=\"" + resolveValue("xml:/Task/@Node") + "\"/></RelatedOrgList></Organization>").getDocumentElement(); %>
	
	<yfc:callAPI apiName="getOrganizationList" inputElement="<%=enterpriseInput%>" templateElement="<%=enterpriseTemplate%>" outputNamespace="EnterpriseList"/>
<%
		YFCDocument countDoc = YFCDocument.createDocument("CountResult");
		YFCElement countInXML = countDoc.getDocumentElement();
		countInXML.setAttribute("CountRequestKey",getValue("Count","xml:/CountRequest/@CountRequestKey"));
		countInXML.setAttribute("Node",getValue("Count","xml:/CountRequest/@Node"));
		countInXML.setAttribute("LatestSummaryTask","Y");				
		YFCElement countTemplate = YFCDocument.parse("<CountResultList CountIteration=\"\"/>").getDocumentElement();
%>
		<yfc:callAPI apiName="getCountResultList" inputElement='<%=countInXML%>' templateElement="<%=countTemplate%>"  outputNamespace="Result"/>
	
<table width="100%" class="view">
<tr>
	<td class="detaillabel" ><yfc:i18n>Task_ID</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/Task/@TaskId" />
		 <input type="hidden" <%=getTextOptions("xml:/TaskList/Task/@TaskKey","xml:/Task/@TaskKey")%>/>
		 <input type="hidden" <%=getTextOptions("xml:/TaskList/Task/@TaskId","xml:/Task/@TaskId")%>/>
		 <input type="hidden" <%=getTextOptions("xml:/Task/@TaskKey","xml:/Task/@TaskKey")%>/>
		 <input type="hidden" <%=getTextOptions("xml:/Task/@EnterpriseKey","xml:/Task/@EnterpriseKey")%>/>
		 <input type="hidden" <%=getTextOptions("xml:/Task/@TaskId","xml:/Task/@TaskId")%>/>
		 <input type="hidden" <%=getTextOptions("xml:/RecordCountResult/@TaskId","xml:/Task/@TaskId")%>/>
		 <input type="hidden" <%=getTextOptions("xml:/RecordCountResult/@SummaryTaskId","xml:/Task/@ParentTaskId")%>/>
		 <input type="hidden" <%=getTextOptions("xml:/RecordCountResult/@Node","xml:Count:/CountRequest/@Node")%>/>
		 <input type="hidden" <%=getTextOptions("xml:/RecordCountResult/@LocationId","xml:/Task/@SourceLocationId")%>/>
		 <input type="hidden" <%=getTextOptions("xml:/RecordCountResult/@CountRequestKey", "xml:Count:/CountRequest/@CountRequestKey")%>/>


	</td>
	<td class="detaillabel" ><yfc:i18n>Task_Type</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/Task/@TaskType" />
    </td>
	<td class="detaillabel" ><yfc:i18n>Count_Iteration</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:makeXMLInput name="countResultKey" >
				<yfc:makeXMLKey binding="xml:/CountResult/CountRequest/@CountIteration" value="xml:Result:/CountResultList/@CountIteration" />
				<yfc:makeXMLKey binding="xml:/CountResult/CountRequest/@CountRequestNo" value="xml:Count:/CountRequest/@CountRequestNo" />
				<yfc:makeXMLKey binding="xml:/CountResult/@Node" value="xml:Count:/CountRequest/@Node" />
		</yfc:makeXMLInput>
		<%
		boolean bShowCountResults = false;
		if(!isVoid(resolveValue("xml:Count:/CountRequest/@Status"))){
			if(resolveValue("xml:Count:/CountRequest/@Status").startsWith("1200") || resolveValue("xml:Count:/CountRequest/@Status").startsWith("1300")){
				bShowCountResults = true;	
			}

		}%>
			<%if(bShowCountResults){%>
				<a <%=getDetailHrefOptions("L01",getParameter("countResultKey"),"")%> >
			<%}%>
			<yfc:getXMLValue binding="xml:Result:/CountResultList/@CountIteration" />
			<%if(bShowCountResults){%>
				</a>
			<%}%>
	   </td>	
		
<%		int iMandate = 0;
		if (getNumericValue("xml:Result:/CountResultList/@CountIteration") > 1) {
			iMandate = 1;
		}	%>
		<input type="hidden" name="MandateCountAttr" value="<%=iMandate%>"/>
    </td>
</tr>
<tr>
	<td class="detaillabel" ><yfc:i18n>Node</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:Count:/CountRequest/@Node" />
    </td>
	<td class="detaillabel" ><yfc:i18n>Enterprise</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:Count:/CountRequest/@EnterpriseCode" />
    </td>
	<td class="detaillabel" ><yfc:i18n>Count_Request_#</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:Count:/CountRequest/@CountRequestNo" />
    </td>		
</tr>
<tr>
	<td class="detaillabel" ><yfc:i18n>Zone</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:Count:/CountRequest/@ZoneId" />
    </td>
	<td class="detaillabel" ><yfc:i18n>Location</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/Task/@SourceLocationId" />
    </td>
	<td class="detaillabel" >
        <yfc:i18n>User</yfc:i18n>
    </td>
    <td nowrap="true">
        <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/TaskList/@UserId","xml:CurrentUser:/User/@Loginid")%>/>
    </td>		
</tr>
<tr>
	<td class="detaillabel" ><yfc:i18n>Pallet_ID</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:Count:/CountRequest/@PalletId" />
    </td>	
	<td class="detaillabel" ><yfc:i18n>Case_ID</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:Count:/CountRequest/@CaseId" />
    </td>	
	<td class="detaillabel" ><yfc:i18n>Receipt_#</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:Count:/CountRequest/Receipt/@ReceiptNo"/>
    </td>	
</tr>
<tr>
	<td class="detaillabel" ><yfc:i18n>Item_ID</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/Task/Inventory/@ItemId" />
    </td>	
	<td class="detaillabel" ><yfc:i18n>UOM</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:Count:/CountRequest/@UnitOfMeasure" />
    </td>	
	<td class="detaillabel" ><yfc:i18n>Product_Class</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:Count:/CountRequest/@ProductClass"/>
    </td>	
</tr>
<tr>
	<td class="detaillabel" ><yfc:i18n>Task_Status</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValueI18NDB binding="xml:/Task/@TaskStatusDesc" />
    </td>	
	<td class="detaillabel" ></td>
		<td class="protectedtext" nowrap="true">
	</td>	
	<td class="detaillabel" ></td>
	    <td class="protectedtext" nowrap="true">
    </td>	
</tr>
<%if(!isVoid(resolveValue("xml:Count:/CountRequest/@EnterpriseCode"))){%>
<yfc:callAPI apiID="AP1"/>
<%
		YFCElement elemClassificationPurpose = (YFCElement) request.getAttribute("ClassificationPurpose");
		Map mp = new HashMap();
		if((!isVoid(elemClassificationPurpose))&&elemClassificationPurpose.hasChildNodes()){
%>
<tr>
	<%
				int iCounter = 1;
                String root[] = new String[1];
                root[0] = "ClassificationPurposeCode";
                elemClassificationPurpose.sortChildren(root, true);
				for(Iterator j = elemClassificationPurpose.getChildren();j.hasNext();){
					YFCElement childElem = (YFCElement)j.next();
					if(!isVoid(childElem.getAttribute("ClassificationPurposeCode"))){
					%>
					<td class="detaillabel">
				        <yfc:i18n> <%=childElem.getAttribute("AttributeName")%></yfc:i18n>
				    </td>
					<%String sAttrBinding = "xml:Count:/CountRequest/@ItemClassification"+String.valueOf(iCounter);
						++iCounter; 
						%>
					<td nowrap="true">
						<input type="text" class="protectedtext" <%=getTextOptions(sAttrBinding)%> />
					</td>
					<%}
				}
%>
</tr>
<%}
}%>
</table>
