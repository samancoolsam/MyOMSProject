<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.util.*" %>
<%
	YFCElement oCountResultList = (YFCElement) request.getAttribute("CountResultList");
	if (oCountResultList != null) {
		oCountResultList.setAttribute("VarianceType","No Variance");
		oCountResultList.setAttribute("VarianceTypeQryType","NE");
		double dCountAccuracy = 0;
		if (oCountResultList.getDoubleAttribute("TotalNumberOfRecords") != 0) { 
			dCountAccuracy =100 - oCountResultList.getDoubleAttribute("NoOfVarianceResults") * 100.00 / oCountResultList.getDoubleAttribute("TotalNumberOfRecords");
		}
		oCountResultList.setDoubleAttribute("CountAccuracy",dCountAccuracy);
	}

%>
<table width="100%" class="view">
	<tr>
		<td class="detaillabel" >
			<yfc:i18n>No_Of_Count_Results</yfc:i18n>
		</td>
		<td class="protectedtext" nowrap="true">
			<yfc:getXMLValue binding="xml:/CountResultList/@TotalNumberOfRecords" />
		</td>
		<td class="detaillabel" >
			<yfc:i18n>No_Of_Variances</yfc:i18n>
		</td>
		<td class="protectedtext" nowrap="true">
			<yfc:makeXMLInput name="CountResultKey" >
				<yfc:makeXMLKey binding="xml:/CountRequest/@CountRequestKey" value="xml:/CountRequest/@CountRequestKey" />
				<yfc:makeXMLKey binding="xml:/CountRequest/@Node" value="xml:/CountRequest/@Node" />
				<yfc:makeXMLKey binding="xml:/CountRequest/@VarianceType" value="xml:/CountResultList/@VarianceType" />
				<yfc:makeXMLKey binding="xml:/CountRequest/@VarianceTypeQryType" value="xml:/CountResultList/@VarianceTypeQryType" />
				<yfc:makeXMLKey binding="xml:/CountRequest/@Node" value="xml:/CountRequest/@Node" />
			</yfc:makeXMLInput>
			<%	if (getNumericValue("xml:/CountResultList/@NoOfVarianceResults") <= 								Double.parseDouble(getDefaultMaxRecords()) && getNumericValue("xml:/CountResultList/@NoOfVarianceResults") > 0) { %>
				<a href="javascript:showDetailForViewGroupId('count', 'YWMD083','<%=getParameter("CountResultKey")%>')">
				<yfc:getXMLValue binding="xml:/CountResultList/@NoOfVarianceResults"/></a>
			<%} else {%>
				<yfc:getXMLValue binding="xml:/CountResultList/@NoOfVarianceResults"/>
			<% } %>
		</td>
		<td class="detaillabel" >
			<yfc:i18n>Count_Accuracy</yfc:i18n>
		</td>
		<td class="protectedtext" nowrap="true">
			<yfc:getXMLValue binding="xml:/CountResultList/@CountAccuracy"/>
		</td>
	</tr>
	<input type="hidden" name="xml:/CountRequest/@ReasonCode" value=""/>
	<input type="hidden" name="xml:/CountRequest/@ReasonText" value=""/>
	<yfc:makeXMLInput name="AcceptVarianceKey" >
		<yfc:makeXMLKey binding="xml:/CountRequest/@CountRequestKey" value="xml:/CountRequest/@CountRequestKey" />
		<yfc:makeXMLKey binding="xml:/CountRequest/@Node" value="xml:/CountRequest/@Node" />
		<yfc:makeXMLKey binding="xml:/CountRequest/@SummaryTaskId" value="xml:/CountResultList/@SummaryTaskId" />
	</yfc:makeXMLInput>
	<input type="hidden" name="acceptVarianceKey" value="<%=getParameter("AcceptVarianceKey")%>"/>
</table>
