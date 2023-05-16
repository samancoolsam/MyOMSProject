<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@include file="/console/jsp/currencyutils.jspf" %>
<%@ page import="com.yantra.wms.util.WMSNumberFormat" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.util.*" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/wmsim.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/countrequest.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/modificationreason.js"></script>


<table width="100%" class="view">
<input type="hidden" name="xml:/CountRequest/@EnterpriseCode" value='<%=resolveValue("xml:/CountResultList/CountRequest/@EnterpriseCode")%>' />
<tr>
	<td class="detaillabel" ><yfc:i18n>Count_Request_#</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountResultList/CountRequest/@CountRequestNo" />
        <input type="hidden" <%=getTextOptions("xml:/CountResultList/CountRequest/@CountRequestNo",
		"xml:/CountResultList/CountRequest/@CountRequestNo")%> />
    </td>
	<td class="detaillabel" ><yfc:i18n>Status</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValueI18NDB binding="xml:/CountResultList/CountRequest/Status/@Description" />
    </td>
    <td class="detaillabel" ><yfc:i18n>Count_Iteration</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountResultList/@CountIteration" />
        <input type="hidden" <%=getTextOptions("xml:/CountResultList/@CountIteration",
		"xml:/CountResultList/@CountIteration")%> />
    </td>	
</tr>
<tr>
	<td class="detaillabel" ><yfc:i18n>No_Of_Locations_In_Variance</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountResultList/@NoOfLocationsInVariance" />
        <input type="hidden" <%=getTextOptions("xml:/CountResultList/@NoOfLocationsInVariance",		
		"xml:/CountResultList/@NoOfLocationsInVariance")%> />
    </td>
	<td class="detaillabel" ><yfc:i18n>No_Of_Items_In_Variance</yfc:i18n></td>
		<td class="protectedtext" nowrap="true">
			<yfc:getXMLValue binding="xml:/CountResultList/@NoOfItemsInVariance" />
			<input type="hidden" <%=getTextOptions("xml:/CountResultList/@NoOfItemsInVariance",
				"xml:/CountResultList/@NoOfItemsInVariance")%> />
	</td>

<% String NoOfItem = resolveValue("xml:/CountResultList/@NoOfItemsInVariance");
if(!isVoid(NoOfItem) && Integer.parseInt(NoOfItem) == 1){ %>

	<td class="detaillabel" ><yfc:i18n>Item_ID</yfc:i18n></td>
	<td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountResultList/SummaryResultList/@ItemID" />
		<input type="hidden" <%=getTextOptions("xml:/CountResultList/SummaryResultList/@ItemID",
			"xml:/CountResultList/SummaryResultList/@ItemID")%> />
	</td>
	</tr>
	<tr>
	<td class="detaillabel" ><yfc:i18n>Net_Variance_Quantity</yfc:i18n></td>
	<td class="protectedtext" nowrap="true">
	<%=WMSNumberFormat.getSign(resolveValue("xml:/CountResultList/@NetVarianceQuantity"))%>
	<% String netVarValue = WMSNumberFormat.getUnsignedValue(resolveValue("xml:/CountResultList/@NetVarianceQuantity")); 
		double dnetVarValue = Double.parseDouble(netVarValue);%>
	<%=getLocalizedStringFromDouble(getLocale(), dnetVarValue)%>
		<input type="hidden" <%=getTextOptions("xml:/CountResultList/@NetVarianceQuantity",
			"xml:/CountResultList/@NetVarianceQuantity")%> />
	</td>
	<td class="detaillabel" ><yfc:i18n>Net_Variance_Value</yfc:i18n></td>
	<td class="protectedtext">
	<%= WMSNumberFormat.getSignedString(resolveValue("xml:/CountResultList/@NetVarianceValue")) %>
		<input type="hidden" <%=getTextOptions("xml:/CountResultList/@NetVarianceValue",
			"xml:/CountResultList/@NetVarianceValue")%> />
	</td>
	<td class="detaillabel" ><yfc:i18n>Currency</yfc:i18n></td>
	<td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountResultList/SummaryResultList/CountResult/@Currency" />
		<input type="hidden" <%=getTextOptions("xml:/CountResultList/SummaryResultList/CountResult/@Currency",
			"xml:/CountResultList/SummaryResultList/CountResult/@Currency")%> />
	</td>
<%}%>	
</tr>
<yfc:makeXMLInput name="requestkey">
	<yfc:makeXMLKey binding="xml:/CountRequest/@CountRequestKey" 										value="xml:/CountResultList/SummaryResultList/CountResult/@CountRequestKey" />
</yfc:makeXMLInput>
<yfc:makeXMLInput name="locnInvAudkey">
	<yfc:makeXMLKey binding="xml:/LocationInventoryAudit/@CountRequestKey" 										value="xml:/CountResultList/SummaryResultList/CountResult/@CountRequestKey" />
	<yfc:makeXMLKey binding="xml:/LocationInventoryAudit/@FromCreatets" value="xml:/CountResultList/SummaryResultList/CountResult/@Createts" />

</yfc:makeXMLInput>
<input type="hidden" value='<%=getParameter("locnInvAudkey")%>' name="locnInvAudkey"/>
<input type="hidden" value='<%=getParameter("requestkey")%>' name="RequestKey"/>
<input type="hidden" name="xml:/CountRequest/@CancellationReasonCode"/>
<input type="hidden" name="xml:/CountRequest/@CountRequestKey" value='<%=resolveValue("xml:/CountResultList/SummaryResultList/CountResult/@CountRequestKey")%>' />
<input type="hidden" name="xml:/CountRequest/@Node" value='<%=resolveValue("xml:/CountResultList/SummaryResultList/CountResult/@Node")%>' />

	<input type="hidden" name="xml:/CountRequest/@ReasonCode"/>
	<input type="hidden" name="xml:/CountRequest/@CountRequestNo"/>
	<input type="hidden" name="xml:/CountRequest/@ReasonText"/>
	<input type="hidden" name="xml:/CountRequest/@DocumentType"/>
	<input type="hidden" name="xml:/CountRequest/@Priority"/>
	<input type="hidden" name="xml:/CountRequest/@RequestingUserId"/>
	<input type="hidden" name="xml:/CountRequest/@StartNoEarlierThan_YFCDATE"/>
	<input type="hidden" name="xml:/CountRequest/@StartNoEarlierThan_YFCTIME"/>
	<input type="hidden" name="xml:/CountRequest/@FinishNoLaterThan_YFCDATE"/>
	<input type="hidden" name="xml:/CountRequest/@FinishNoLaterThan_YFCTIME"/>	
	<input type="hidden" name="xml:/CountRequest/@StartNoEarlierThan"/>
	<input type="hidden" name="xml:/CountRequest/@FinishNoLaterThan"/>
	<input type="hidden" name="xml:/CountRequest/@RequestType" value="<%=resolveValue("xml:/CountResultList/SummaryResultList/CountResult/CountRequest/@RequestType")%>"/>
	<input type="hidden" name="xml:/CountRequest/@CountProgramName" value="<%=resolveValue("xml:/CountResultList/SummaryResultList/CountResult/CountRequest/@CountProgramName")%>"/>

</table>
