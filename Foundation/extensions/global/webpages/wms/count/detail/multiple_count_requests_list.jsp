<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfc.core.YFCObject" %>
<script language="javascript" src="/yantra/console/scripts/tools.js"></script>
<script language="javascript" src="/yantra/console/scripts/wmsim.js"></script>
<% 
	YFCElement apiOutput = (YFCElement)request.getAttribute("LocationList");
	if(YFCObject.equals(apiOutput.getAttribute("TotalNumberOfRecords","0"),"0")){%>
	<script language="javascript">
	alert("Invalid Locations Passed");
	window.close();
	</script>
	<%}	
	String sNode = request.getParameter("xml:/CountRequest/@Node");
	String sEntCode = request.getParameter("xml:/CountRequest/@EnterpriseCode");
	String sReqType = request.getParameter("xml:/CountRequest/@RequestType");	
	String sPriority = request.getParameter("xml:/CountRequest/@Priority");
	String sUserId = request.getParameter("xml:/CountRequest/@RequestingUserId");	
	String sfrmLocationId = request.getParameter("xml:/CountRequest/@FromLocationId");
	String stoLocationId = request.getParameter("xml:/CountRequest/@ToLocationId");
	String sStartDate = request.getParameter("xml:/CountRequest/@StartNoEarlierThan");
	String sFinishDate = request.getParameter("xml:/CountRequest/@FinishNoLaterThan");
	String sStartNoEarlierThan = getISODateValue(sStartDate);
	String sFinishNoLaterThan = getISODateValue(sFinishDate);
	
	YFCDocument oMultiAPIDoc = YFCDocument.createDocument("MultiApi");
	YFCElement oMutltiApiElement = oMultiAPIDoc.getDocumentElement();

	for (Iterator oIter = apiOutput.getChildren(); oIter.hasNext();){
		YFCElement oLocation = (YFCElement)oIter.next();		
		YFCElement oAPIElement = oMutltiApiElement.createChild("API");
		oAPIElement.setAttribute("Name","createCountRequest");
		YFCElement oInputElem = oAPIElement.createChild("Input");
		YFCElement oCountRequest = oInputElem.createChild("CountRequest");
		oCountRequest.setAttribute("DocumentType","3001");
		oCountRequest.setAttribute("Node",sNode);
		oCountRequest.setAttribute("LocationId",oLocation.getAttribute("LocationId"));
		oCountRequest.setAttribute("EnterpriseCode",sEntCode);
		oCountRequest.setAttribute("Priority",sPriority);
		oCountRequest.setAttribute("RequestingUserId",sUserId);
		oCountRequest.setAttribute("RequestType",sReqType);
		oCountRequest.setAttribute("StartNoEarlierThan",sStartNoEarlierThan);
		oCountRequest.setAttribute("FinishNoLaterThan",sFinishNoLaterThan);
	}

	YFCDocument oCountRequestListDoc = YFCDocument.createDocument("CountRequest");
	YFCElement oCountRequestListElement = oCountRequestListDoc.getDocumentElement();
	YFCElement tempElem = YFCDocument.parse("<CountRequestList TotalNumberOfRecords=\"\"><CountRequest Node=\"\" CountRequestNo=\"\" EnterpriseCode=\"\" FinishNoLaterThan=\"\" LocationId=\"\" Priority=\"\" RequestType=\"\" RequestingUserId=\"\" StartNoEarlierThan=\"\" Status=\"\"><Status Description=\"\" Status=\"\" /><Priority Description=\"\" Priority=\"\" /><RequestingUser RequestingUserId=\"\" UserName=\"\"/></CountRequest></CountRequestList>").getDocumentElement();
	oCountRequestListElement.setAttribute("EnterpriseCode",sEntCode);
	oCountRequestListElement.setAttribute("Node",sNode);
	oCountRequestListElement.setAttribute("FromLocationId",sfrmLocationId);
	oCountRequestListElement.setAttribute("ToLocationId",stoLocationId);
	oCountRequestListElement.setAttribute("LocationIdQryType","BETWEEN");
	oCountRequestListElement.setAttribute("IgnoreOrdering", "N");
	oCountRequestListElement.setAttribute("Status", "1100");
	YFCElement orderByElement = oCountRequestListElement.createChild("OrderBy");
	YFCElement orderByAttrElement = orderByElement.createChild("Attribute");
	orderByAttrElement.setAttribute("Name","CountRequestNo");
	orderByAttrElement.setAttribute("Desc","Y");
%>

<yfc:callAPI apiName="multiApi" inputElement="<%=oMutltiApiElement%>" outputNamespace="MultiApiOutput"/>
<yfc:callAPI apiName="getCountRequestList" inputElement="<%=oCountRequestListElement%>" templateElement="<%=tempElem%>" outputNamespace="CountRequestList"/>
	<table  editable="false" class="table">
		<thead>
			<tr>
				<td class="checkboxheader" sortable="no">
					<input type="checkbox" name="checkbox" value="checkbox" onclick="doCheckAll(this);"/>
				</td>
				<td class="tablecolumnheader"><yfc:i18n>Count_Request_#</yfc:i18n></td>				
				<td class="tablecolumnheader"><yfc:i18n>Location</yfc:i18n></td>
				<td class="tablecolumnheader"><yfc:i18n>Enterprise</yfc:i18n></td>
				<td class="tablecolumnheader"><yfc:i18n>Node</yfc:i18n></td>
				<td class="tablecolumnheader"><yfc:i18n>Priority</yfc:i18n></td>
				<td class="tablecolumnheader"><yfc:i18n>Start_No_Earlier_Than</yfc:i18n></td>
				<td class="tablecolumnheader"><yfc:i18n>Requesting_User</yfc:i18n></td>
				<td class="tablecolumnheader"><yfc:i18n>Status</yfc:i18n></td>
			</tr>
		</thead>
			<tbody>
			<yfc:loopXML binding="xml:CountRequestList:/CountRequestList/@CountRequest" id="CountRequest">
				<tr>
					<yfc:makeXMLInput name="countrequestkey">
						<yfc:makeXMLKey binding="xml:/CountRequest/@CountRequestKey" value="xml:/CountRequest/@CountRequestKey" />
						<yfc:makeXMLKey binding="xml:/CountRequest/@Node" value="xml:/CountRequest/@Node" />
					</yfc:makeXMLInput>
					<td class="checkboxcolumn">
						<input type="checkbox" value='<%=getParameter("countrequestkey")%>' name="EntityKey"/>
					</td>
						<td class="tablecolumn"><yfc:getXMLValue binding="xml:/CountRequest/@CountRequestNo"/></td>					
						<td class="tablecolumn"><yfc:getXMLValue binding="xml:/CountRequest/@LocationId"/></td>
						<td class="tablecolumn"><yfc:getXMLValue binding="xml:/CountRequest/@EnterpriseCode"/></td>			
						<td class="tablecolumn"><yfc:getXMLValue binding="xml:/CountRequest/@Node"/></td>						
						<td class="tablecolumn"><yfc:getXMLValueI18NDB binding="xml:/CountRequest/Priority/@Description"/></td>
						<td class="tablecolumn"><yfc:getXMLValue binding="xml:/CountRequest/@StartNoEarlierThan"/></td>
					<td class="tablecolumn"><yfc:getXMLValue binding="xml:/CountRequest/RequestingUser/@UserName"/></td>
					<td class="tablecolumn"><yfc:getXMLValueI18NDB binding="xml:/CountRequest/Status/@Description"/></td>
				</tr>
			</yfc:loopXML>
		</tbody>
	</table>
