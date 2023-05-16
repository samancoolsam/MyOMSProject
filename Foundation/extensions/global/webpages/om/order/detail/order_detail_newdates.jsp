<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ include file="/console/jsp/modificationutils.jspf" %>
<%@include file="/om/order/detail/order_detail_newdates_include.jspf" %>
<script language="javascript">
window.attachEvent("onload", IgnoreChangeNames);
</script>
<%	
	String sDateTypeId ="" ; 
%>
<table class="table" width="100%">
<thead>
    <tr> 
		<td class="tablecolumnheader" style="width:30px"><BR /></td>
        <td class="tablecolumnheader">
            <yfc:i18n>Date_Type</yfc:i18n>
        </td>
		<td class="tablecolumnheader" sortable="no">
            <yfc:i18n>Requested</yfc:i18n>
        </td>
		<td class="tablecolumnheader" sortable="no">
            <yfc:i18n>Expected</yfc:i18n>
        </td>
		<td class="tablecolumnheader" sortable="no">
            <yfc:i18n>Actual</yfc:i18n>
        </td>
    </tr>
</thead> 
<tbody>
    <yfc:loopXML name="DateTypeList" binding="xml:/DateTypeList/@DateType" id="DateType"> 
    <tr>
		<td class="tablecolumn">
			<%if (equals(resolveValue("xml:DateType:/DateType/@IsMilestone"),"Y")) {%>
				<img class="columnicon" <%=getImageOptions(YFSUIBackendConsts.MILESTONE_COLUMN, "Milestone")%>>
			<%}%>
		</td>
        <td class="tablecolumn">
		<%
			String sReqDate = "";
			String sExpDate ="";
			String sActDate = "";
			sDateTypeId = resolveValue("xml:DateType:/DateType/@DateTypeId");
			YFCDate tmpDate = (YFCDate)oMap.get(sDateTypeId + "Req");
			if(tmpDate != null)
				sReqDate = tmpDate.getString(getLocale(),true);
			tmpDate = (YFCDate)oMap.get(sDateTypeId + "Exp");
			if(tmpDate != null)
				sExpDate = tmpDate.getString(getLocale(),true);
	
			tmpDate = (YFCDate)oMap.get(sDateTypeId + "Act");
			if(tmpDate != null)
				sActDate = tmpDate.getString(getLocale(),true);
		%>
			<yfc:i18n><yfc:getXMLValue name="DateType" binding="xml:/DateType/@DateTypeId" /></yfc:i18n>
			<input type="hidden" <%=getTextOptions("xml:/Order/OrderDates/OrderDate_" + DateTypeCounter + "/@DateTypeId","xml:DateType:/DateType/@DateTypeId")%> />
        </td>
        <td class="tablecolumn">
			<% if(equals(resolveValue("xml:DateType:/DateType/@RequestedFlag"),"Y")) {%>
		        <input type="text" <%=yfsGetTextOptions("xml:/Order/OrderDates/OrderDate_" + DateTypeCounter + 	"/@RequestedDate_YFCDATE",getDateOrTimePart("YFCDATE",sReqDate),"xml:/Order/AllowedModifications")%>/>
				<img class="lookupicon" name="Date_Lookup" onclick="invokeCalendar(this);return false" 
				<%=yfsGetImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar","xml:/Order/OrderDates/OrderDate/@RequestedDate_YFCDATE","xml:/Order/AllowedModifications") %> />
				<input type="text" <%=yfsGetTextOptions("xml:/Order/OrderDates/OrderDate_" + DateTypeCounter + 	"/@RequestedDate_YFCTIME",getDateOrTimePart("YFCTIME",sReqDate),"xml:/Order/AllowedModifications")%>/>
	            <img class="lookupicon" name="Time_Lookup" onclick="invokeTimeLookup(this);return false" 
				<%=yfsGetImageOptions(YFSUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup","xml:/Order/OrderDates/OrderDate/@RequestedDate_YFCTIME","xml:/Order/AllowedModifications")  %> />
			<%}%>
        </td>
        <td class="tablecolumn">
			<% if(equals(resolveValue("xml:DateType:/DateType/@ExpectedFlag"),"Y")) {%>
		        <input type="text" <%=yfsGetTextOptions("xml:/Order/OrderDates/OrderDate_" + DateTypeCounter + "/@ExpectedDate_YFCDATE",getDateOrTimePart("YFCDATE",sExpDate),"xml:/Order/AllowedModifications")%>/>
				<img class="lookupicon" name="Date_Lookup" onclick="invokeCalendar(this);return false" <%=yfsGetImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar","xml:/Order/OrderDates/OrderDate/@ExpectedDate_YFCDATE","xml:/Order/AllowedModifications") %> />
				<input type="text" <%=yfsGetTextOptions("xml:/Order/OrderDates/OrderDate_" + DateTypeCounter + "/@ExpectedDate_YFCTIME",getDateOrTimePart("YFCTIME",sExpDate),"xml:/Order/AllowedModifications")%>/>
	            <img class="lookupicon" name="Time_Lookup" onclick="invokeTimeLookup(this);return false" 
				<%=yfsGetImageOptions(YFSUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup","xml:/Order/OrderDates/OrderDate/@ExpectedDate_YFCTIME","xml:/Order/AllowedModifications") %> />
			<%}%>
        </td>
		<td class="tablecolumn">
			<% if(equals(resolveValue("xml:DateType:/DateType/@IsMilestone"),"Y") ) { %>
				<input type="text"  class="protectedinput"  contentEditable="false" <%=yfsGetTextOptions("xml:/Order/OrderDates/OrderDate_" + DateTypeCounter + "/@ActualDate_YFCDATE",getDateOrTimePart("YFCDATE",sActDate),"xml:/Order/AllowedModifications")%>/>
				<input type="text" class="protectedinput"  contentEditable="false" <%=yfsGetTextOptions("xml:/Order/OrderDates/OrderDate_" + DateTypeCounter + "/@ActualDate_YFCTIME",getDateOrTimePart("YFCTIME",sActDate),"xml:/Order/AllowedModifications")%>/>
			<% } else if(equals(resolveValue("xml:DateType:/DateType/@ActualFlag"),"Y") ) {%>
				<input type="text" <%=yfsGetTextOptions("xml:/Order/OrderDates/OrderDate_" + DateTypeCounter + "/@ActualDate_YFCDATE",getDateOrTimePart("YFCDATE",sActDate),"xml:/Order/AllowedModifications")%>/>
				<img class="lookupicon" name="Date_Lookup" onclick="invokeCalendar(this);return false" <%=yfsGetImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar","xml:/Order/OrderDates/OrderDate/@ACtualDate_YFCDATE","xml:/Order/AllowedModifications") %> />
				<input type="text" <%=yfsGetTextOptions("xml:/Order/OrderDates/OrderDate_" + DateTypeCounter + "/@ActualDate_YFCTIME",getDateOrTimePart("YFCTIME",sActDate),"xml:/Order/AllowedModifications")%>/>
	            <img class="lookupicon" name="Time_Lookup" onclick="invokeTimeLookup(this);return false" 
				<%=yfsGetImageOptions(YFSUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup","xml:/Order/OrderDates/OrderDate/@ActualDate_YFCTIME","xml:/Order/AllowedModifications") %> />
			<%}%>
		</td>
    </tr>
    </yfc:loopXML> 
</tbody>
</table>
